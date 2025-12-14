package com.example.kiviapp.core

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.example.kiviapp.R
import com.example.kiviapp.features.ai.GeminiIntegration
import com.example.kiviapp.features.speech.TextToSpeechManager
import com.example.kiviapp.features.speech.VoiceRecognitionManager
import com.example.kiviapp.features.ui.activities.settings.KiviSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KiviOrchestrator(private val context: Context) {

    private val cerebro = GeminiIntegration()
    private val boca = TextToSpeechManager(context)
    private val oido = VoiceRecognitionManager(context)

    interface KiviListener {
        fun onEstadoCambiado(texto: String)
        fun onKiviHablando(texto: String)
        fun onError(mensaje: String)
    }

    private var listener: KiviListener? = null

    init {
        // ✅ Escuchamos cuando el TTS esté listo
        boca.onTtsReady = {
            Log.d("KIVI", "✅ TTS está listo para hablar")
            decir(context.getString(R.string.kivi_greeting))
        }
    }

    fun setListener(nuevoListener: KiviListener) {
        listener = nuevoListener
    }

    // ----------------------------------------------------------
    // SALUDO
    // ----------------------------------------------------------
    fun saludar() {
        listener?.onEstadoCambiado(context.getString(R.string.kivi_ready))
        decir(context.getString(R.string.kivi_greeting))
    }

    // ----------------------------------------------------------
    // VIBRACIÓN
    // ----------------------------------------------------------
    private fun vibrar(fuerte: Boolean = false) {
        if (!KiviSettings.isHapticEnabled(context)) return

        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val duracion: Long = if (fuerte) 180L else 60L

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    duracion,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duracion)
        }
    }

    private data class DangerFlags(
        val suelo: Boolean,
        val cabeza: Boolean
    )

    private fun detectarPeligroEnTexto(respuestaIA: String): DangerFlags {
        val lower = respuestaIA.lowercase()

        var peligroSuelo = false
        var peligroCabeza = false

        if (lower.contains("ambos_peligros")) {
            peligroSuelo = true
            peligroCabeza = true
        }
        if (lower.contains("peligro_suelo")) {
            peligroSuelo = true
        }
        if (lower.contains("peligro_cabeza")) {
            peligroCabeza = true
        }

        if (lower.contains("hueco") ||
            lower.contains("bache") ||
            lower.contains("escalera") ||
            lower.contains("escalones") ||
            lower.contains("desnivel") ||
            lower.contains("pozo") ||
            lower.contains("obstáculo en el suelo") ||
            lower.contains("obstaculo en el suelo")
        ) {
            peligroSuelo = true
        }

        if (lower.contains("rama") ||
            lower.contains("ramas") ||
            lower.contains("letrero") ||
            lower.contains("señal") ||
            lower.contains("señales") ||
            lower.contains("marco bajo") ||
            lower.contains("objeto a la altura de la cabeza")
        ) {
            peligroCabeza = true
        }

        return DangerFlags(
            suelo = peligroSuelo,
            cabeza = peligroCabeza
        )
    }

    fun empezarEscucha(alEscuchar: (String) -> Unit) {
        listener?.onEstadoCambiado(context.getString(R.string.kivi_listening))
        oido.startListening { texto ->
            alEscuchar(texto)
        }
    }

    fun detenerEscucha() {
        listener?.onEstadoCambiado(context.getString(R.string.kivi_processing))
        oido.stopListening()
    }

    // ----------------------------------------------------------
    // PROCESAR PREGUNTA (TEXTO + IMAGEN)
    // ----------------------------------------------------------
    fun procesarPregunta(textoUsuario: String, foto: Bitmap?) {
        listener?.onEstadoCambiado(context.getString(R.string.kivi_thinking))

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val langCode = KiviSettings.getVoiceLanguage(context)

                val respuesta: String = if (foto != null) {

                    // ✅ PROMPT HARDCODEADO (OK)
                    val prompt = """
                        Eres Kivi, un asistente para personas con discapacidad visual.
                        Analiza esta imagen y responde en el idioma configurado para el usuario.
                        
                        1) Describe lo más importante que veas en la escena.
                        2) Si existe algún riesgo u obstáculo para caminar, descríbelo claramente
                           (por ejemplo: "hay un hueco delante", "hay una escalera hacia abajo",
                           "hay una rama a la altura de la cabeza", etc.).
                        3) Además, al final de tu respuesta añade SIEMPRE UNA de estas marcas, en MAYÚSCULAS:
                           - PELIGRO_SUELO   → si hay huecos, escalones, baches, escaleras, objetos bajos, etc.
                           - PELIGRO_CABEZA  → si hay ramas, letreros, marcos bajos u objetos a la altura de la cabeza.
                           - AMBOS_PELIGROS  → si hay peligro tanto en el suelo como a la altura de la cabeza.
                           - SIN_PELIGRO     → si no ves ningún riesgo para la persona.
                        
                        Es MUY IMPORTANTE que pongas SOLO UNA de estas marcas exactamente al final de la respuesta.
                        No expliques la marca, solo escríbela en una nueva línea al final.
                        
                        Pregunta del usuario: "$textoUsuario"
                    """.trimIndent()

                    cerebro.getImageResponse(prompt, foto, langCode)
                } else {
                    cerebro.getResponse(textoUsuario, langCode)
                }

                val flags = detectarPeligroEnTexto(respuesta)

                val alertasActivadas = KiviSettings.isObstacleAlertEnabled(context)
                val sueloActivado = KiviSettings.isObstacleFloorEnabled(context)
                val cabezaActivado = KiviSettings.isObstacleHeadEnabled(context)

                val hayPeligroSuelo = alertasActivadas && sueloActivado && flags.suelo
                val hayPeligroCabeza = alertasActivadas && cabezaActivado && flags.cabeza

                var advertencia: String? = null

                if (hayPeligroSuelo || hayPeligroCabeza) {
                    vibrar(true)

                    advertencia = when {
                        hayPeligroSuelo && hayPeligroCabeza ->
                            context.getString(R.string.kivi_warning_both)

                        hayPeligroSuelo ->
                            context.getString(R.string.kivi_warning_floor)

                        hayPeligroCabeza ->
                            context.getString(R.string.kivi_warning_head)

                        else -> null
                    }
                }

                val sinCodigos = respuesta
                    .replace("PELIGRO_SUELO", "", ignoreCase = true)
                    .replace("PELIGRO_CABEZA", "", ignoreCase = true)
                    .replace("AMBOS_PELIGROS", "", ignoreCase = true)
                    .replace("SIN_PELIGRO", "", ignoreCase = true)
                    .replace("*", "")
                    .replace("#", "")
                    .trim()

                val textoFinal = if (advertencia != null) {
                    if (sinCodigos.isNotBlank()) {
                        "$advertencia\n\n$sinCodigos"
                    } else {
                        advertencia
                    }
                } else {
                    sinCodigos.ifBlank { respuesta }
                }

                comunicarRespuesta(textoFinal)

            } catch (e: Exception) {
                Log.e("KIVI", "Error procesando pregunta: ${e.message}", e)
                listener?.onError(e.message ?: context.getString(R.string.kivi_error_unknown))
                decir(context.getString(R.string.kivi_error_generic))
            }
        }
    }

    private fun comunicarRespuesta(texto: String) {
        listener?.onKiviHablando(texto)
        boca.speak(texto)
    }

    fun liberarRecursos() {
        boca.shutdown()
    }

    fun decir(texto: String) {
        boca.speak(texto)
    }
}
