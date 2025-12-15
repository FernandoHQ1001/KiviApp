package com.example.kiviapp.features.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.kiviapp.features.ui.activities.settings.KiviSettings
import java.util.Locale

/**
 * Convierte texto a voz respetando el idioma y activación configurados.
 */
class TextToSpeechManager(context: Context) : TextToSpeech.OnInitListener {

    private val appContext = context.applicationContext

    // Motor de texto a voz
    private var tts: TextToSpeech? = null

    // Indica si el motor TTS ya está listo
    private var isReady = false

    // Texto pendiente si se intenta hablar antes de que TTS esté listo
    private var pendingText: String? = null

    // ✅ Callback que se ejecuta cuando TTS está listo
    var onTtsReady: (() -> Unit)? = null

    // Inicializa el motor TTS
    init {
        tts = TextToSpeech(appContext, this)
    }

    /**
     * Se ejecuta automáticamente cuando el motor TTS se inicializa
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d("KIVI_TTS", "TTS inicializado correctamente")
            isReady = true

            applyLanguage() // ✅ aplica idioma actual

            // ✅ Notificamos que está listo
            onTtsReady?.invoke()

            // Si había texto pendiente, lo reproduce ahora
            pendingText?.let {
                speak(it)
                pendingText = null
            }
        } else {
            Log.e("KIVI_TTS", "Falló la inicialización de voz.")
            isReady = false
        }
    }

    /**
     * Aplica el idioma de voz según la configuración del usuario
     */
    private fun applyLanguage() {
        val langCode = KiviSettings.getVoiceLanguage(appContext)

        val locale = when (langCode) {
            "en" -> Locale.forLanguageTag("en-US")
            "pt" -> Locale.forLanguageTag("pt-BR") // o "pt-PT" si prefieres Portugal
            else -> Locale.forLanguageTag("es-ES")
        }

        val result = tts?.setLanguage(locale)

        // Si el idioma no está soportado, se usa español como respaldo
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("KIVI_TTS", "Idioma TTS NO soportado: $langCode ($locale). Usando fallback es-ES.")
            tts?.setLanguage(Locale.forLanguageTag("es-ES"))
        } else {
            Log.d("KIVI_TTS", "Idioma TTS aplicado: $langCode ($locale)")
        }
    }

    /**
     * Reproduce el texto en voz alta
     */
    fun speak(text: String) {
        // Verifica si el usuario tiene la voz activada
        if (!KiviSettings.isVoiceEnabled(appContext)) {
            Log.d("KIVI_TTS", "Voz desactivada por usuario.")
            return
        }

        if (isReady) {
            applyLanguage() // ✅ refresca idioma por si cambió en settings
            Log.d("KIVI_TTS", "🔊 Hablando: $text")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kivi_tts") // Reproduce el texto
        } else {
            // Si el TTS no está listo, guarda el texto
            Log.w("KIVI_TTS", "TTS no listo, guardando texto: $text")
            pendingText = text
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
