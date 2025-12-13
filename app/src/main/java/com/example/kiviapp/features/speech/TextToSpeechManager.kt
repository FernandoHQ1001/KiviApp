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

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var pendingText: String? = null

    // ✅ Callback que se ejecuta cuando TTS está listo
    var onTtsReady: (() -> Unit)? = null

    init {
        tts = TextToSpeech(appContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d("KIVI_TTS", "TTS inicializado correctamente")
            isReady = true
            setLanguage()

            // ✅ Notificamos que está listo
            onTtsReady?.invoke()

            pendingText?.let {
                speak(it)
                pendingText = null
            }
        } else {
            Log.e("KIVI_TTS", "Falló la inicialización de voz.")
            isReady = false
        }
    }

    private fun setLanguage() {
        val langCode = KiviSettings.getVoiceLanguage(appContext)
        val locale = when (langCode) {
            "en" -> Locale.forLanguageTag("en-US")
            else -> Locale.forLanguageTag("es-ES")
        }

        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("KIVI_TTS", "El idioma $langCode no está disponible en este dispositivo.")
        } else {
            Log.d("KIVI_TTS", "Idioma TTS aplicado: $langCode")
        }
    }

    fun speak(text: String) {
        if (!KiviSettings.isVoiceEnabled(appContext)) {
            Log.d("KIVI_TTS", "Voz desactivada por usuario.")
            return
        }

        if (isReady) {
            setLanguage()
            Log.d("KIVI_TTS", "🔊 Hablando: $text")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
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