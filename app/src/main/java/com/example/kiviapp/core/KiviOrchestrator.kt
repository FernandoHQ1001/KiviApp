package com.example.kiviapp.core

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.example.kiviapp.KiviSettings
import com.example.kiviapp.features.ai.GeminiIntegration
import com.example.kiviapp.features.speech.TextToSpeechManager
import com.example.kiviapp.features.speech.VoiceRecognitionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KiviOrchestrator(private val context: Context) {

    // Instanciamos las "Personas" (Módulos)
    private val cerebro = GeminiIntegration()
    private val boca = TextToSpeechManager(context)
    private val oido = VoiceRecognitionManager(context)

    // Interfaz para comunicarse con la Pantalla (UI)
    interface KiviListener {
        fun onEstadoCambiado(texto: String) // Para actualizar el texto en pantalla
        fun onKiviHablando(texto: String)   // Cuando Kivi dice algo
        fun onError(mensaje: String)
    }

    private var listener: KiviListener? = null

    fun setListener(nuevoListener: KiviListener) {
        listener = nuevoListener
    }

    // --- ACCIONES ---

    fun saludar() {
        listener?.onEstadoCambiado("Iniciando Kivi...")
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val saludo = cerebro.getResponse("Saluda soy Kivi.")
                comunicarRespuesta(saludo)
            } catch (e: Exception) {
                listener?.onError("Error inicio: ${e.message}")
            }
        }
    }

    // Detectar palabras de peligro en la respuesta de la IA
    private fun detectarPeligro(texto: String): Boolean {
        val alertas = listOf(
            "peligro",
            "cuidado",
            "riesgo",
            "obstáculo",
            "obstaculo",
            "auto",
            "carro",
            "vehículo",
            "vehiculo",
            "hueco",
            "agujero",
            "escalera",
            "perro",
            "caída",
            "caida"
        )

        val lower = texto.lowercase()
        return alertas.any { palabra -> lower.contains(palabra) }
    }

    // Vibración (suave o fuerte)
    private fun vibrar(fuerte: Boolean = false) {
        // Si el usuario desactivó el feedback háptico, no vibra
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

    fun empezarEscucha(alEscuchar: (String) -> Unit) {
        listener?.onEstadoCambiado("👂 Escuchando...")
        oido.startListening { texto ->
            alEscuchar(texto)
        }
    }

    fun detenerEscucha() {
        listener?.onEstadoCambiado("⏳ Procesando...")
        oido.stopListening()
    }

    // El cerebro central que decide qué hacer
    fun procesarPregunta(textoUsuario: String, foto: Bitmap?) {
        listener?.onEstadoCambiado("🧠 Pensando...")

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val respuesta: String

                // Lógica inteligente de OCR o Visión
                if (foto != null) {
                    var prompt = textoUsuario
                    val minus = textoUsuario.lowercase()
                    if (minus.contains("lee") || minus.contains("dice") || minus.contains("texto")) {
                        prompt = "$textoUsuario. (IMPORTANTE: Transcribe el texto de la imagen)."
                    }
                    respuesta = cerebro.getImageResponse(prompt, foto)
                } else {
                    respuesta = cerebro.getResponse(textoUsuario)
                }

                // Limpieza de formato
                val limpia = respuesta.replace("*", "").replace("#", "")
                comunicarRespuesta(limpia)

            } catch (e: Exception) {
                listener?.onError(e.message ?: "Error desconocido")
                boca.speak("Tuve un problema.")
            }
        }
    }

    private fun comunicarRespuesta(texto: String) {

        // 🔎 Si el usuario activó "Alerta de obstáculos" y el texto parece peligroso:
        if (KiviSettings.isObstacleAlertEnabled(context) && detectarPeligro(texto)) {
            vibrar(true)  // vibración intensa
        }

        listener?.onEstadoCambiado("KIVI: $texto")
        boca.speak(texto)
        listener?.onKiviHablando(texto)
    }

    fun liberarRecursos() {
        boca.shutdown()
    }

    fun decir(texto: String) {
        boca.speak(texto)
    }
}
