package com.example.kiviapp.core

// Accesp a servicios del sistema (vibración) y procesar imágenes
import android.content.Context // acceder a recursos y servicios de la app y configuraciones del usuario
import android.graphics.Bitmap // para enviar las imagenes
import android.os.VibrationEffect // vibraciones mas precisas
import android.os.Vibrator // controla la vibración

import android.os.Build // sesion de android
import android.util.Log // mensajes de depuracion
import com.example.kiviapp.R

import com.example.kiviapp.features.ai.GeminiIntegration // IA
import com.example.kiviapp.features.speech.TextToSpeechManager // Texto a voz
import com.example.kiviapp.features.speech.VoiceRecognitionManager // Reconocimiento de voz

import com.example.kiviapp.features.ui.activities.settings.KiviSettings // para las preferencias del usuario
import kotlinx.coroutines.CoroutineScope // para ejecutar tareas en segundo plano
import kotlinx.coroutines.Dispatchers // para el hilo de ejecución
import kotlinx.coroutines.launch // lanza la corrutina asincrona

/*  CLASE CENTRAL DEL PROYECTO

    Coordina todos los componentes principales del asistente
*/
class KiviOrchestrator(private val context: Context) {

    private val cerebro = GeminiIntegration()
    private val boca = TextToSpeechManager(context)
    private val oido = VoiceRecognitionManager(context)

    // Permite comunicar eventos con la interfaz gráfica
    interface KiviListener {
        fun onEstadoCambiado(texto: String)
        fun onKiviHablando(texto: String)
        fun onError(mensaje: String)
    }

    private var listener: KiviListener? = null

    // Cuando inicia Kivi, saluda
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
        listener?.onEstadoCambiado(context.getString(R.string.kivi_ready)) // cambia el estado
        decir(context.getString(R.string.kivi_greeting))
    }

    // ----------------------------------------------------------
    // VIBRACIÓN (para la retroalimentación háptica)
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

    // Flags para la detección de peligros
    private data class DangerFlags(
        val suelo: Boolean,
        val cabeza: Boolean
    )

    // Analiza la respuesta de la IA y detecta peligros
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

    // Cambia el estado a escuchando
    fun empezarEscucha(alEscuchar: (String) -> Unit) {
        listener?.onEstadoCambiado(context.getString(R.string.kivi_listening))
        oido.startListening { texto ->
            alEscuchar(texto)
        }
    }

    // Cambia de estado a procesando y detiene el micrófono
    fun detenerEscucha() {
        listener?.onEstadoCambiado(context.getString(R.string.kivi_processing))
        oido.stopListening()
    }

    /*
     * PROCESA LA PREGUNTA DEL USUARIO

     * Puede trabajar solo con texto o con texto + imagen.

     * Flujo general:
     * 1. Cambia el estado a "pensando"
     * 2. Envía la pregunta a la IA (con o sin imagen)
     * 3. Analiza la respuesta para detectar peligros
     * 4. Genera alertas (voz + vibración) si es necesario
     * 5. Comunica la respuesta final al usuario
     */
    fun procesarPregunta(textoUsuario: String, foto: Bitmap?) {
        listener?.onEstadoCambiado(context.getString(R.string.kivi_thinking)) // Informa a la interfaz que Kivi está "pensando"

        // Se ejecuta en el hilo principal usando corrutinas
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val langCode = KiviSettings.getVoiceLanguage(context) //obtiene el idioma configurado

                // Decide si usa IA con imagen o solo texto
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

                    // Envía texto + imagen a la IA
                    cerebro.getImageResponse(prompt, foto, langCode)
                } else {
                    // Envía solo el texto a la IA
                    cerebro.getResponse(textoUsuario, langCode)
                }

                // Analiza la respuesta de la IA para detectar peligros
                val flags = detectarPeligroEnTexto(respuesta)

                // Verifica qué alertas están habilitadas en la configuración
                val alertasActivadas = KiviSettings.isObstacleAlertEnabled(context)
                val sueloActivado = KiviSettings.isObstacleFloorEnabled(context)
                val cabezaActivado = KiviSettings.isObstacleHeadEnabled(context)

                // Determina si hay peligro real según configuración + análisis
                val hayPeligroSuelo = alertasActivadas && sueloActivado && flags.suelo
                val hayPeligroCabeza = alertasActivadas && cabezaActivado && flags.cabeza

                var advertencia: String? = null

                // Si se detecta algún peligro, se activa la vibración y el mensaje
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

                // Limpia la respuesta de la IA eliminando marcas técnicas
                val sinCodigos = respuesta
                    .replace("PELIGRO_SUELO", "", ignoreCase = true)
                    .replace("PELIGRO_CABEZA", "", ignoreCase = true)
                    .replace("AMBOS_PELIGROS", "", ignoreCase = true)
                    .replace("SIN_PELIGRO", "", ignoreCase = true)
                    .replace("*", "")
                    .replace("#", "")
                    .trim()

                // Construye el texto final que escuchará el usuario
                val textoFinal = if (advertencia != null) {
                    if (sinCodigos.isNotBlank()) {
                        // Advertencia + descripción
                        "$advertencia\n\n$sinCodigos"
                    } else {
                        // Solo advertencia
                        advertencia
                    }
                } else {
                    // Si no hay peligro, se usa la respuesta limpia
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

    // Comunica la respuesta final al usuario
    private fun comunicarRespuesta(texto: String) {
        listener?.onKiviHablando(texto) // cambia estado
        boca.speak(texto)
    }

    // Libera recursos de audio
    fun liberarRecursos() {
        boca.shutdown()
    }

    // Hace que Kivi hable
    fun decir(texto: String) {
        boca.speak(texto)
    }
}
