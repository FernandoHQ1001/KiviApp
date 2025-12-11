package com.example.kiviapp.features.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.kiviapp.KiviSettings
import java.util.Locale

// Se encarga de convertir el texto de la IA en audio
class TextToSpeechManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var pendingText: String? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {

                val langCode = KiviSettings.getVoiceLanguage(context) // "es" o "en"
                val locale = when (langCode) {
                    "en" -> Locale("en", "US")
                    else -> Locale("es", "ES")
                }
                val result = tts?.setLanguage(locale)

                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    Log.e("KIVI_TTS", "El idioma seleccionado no está instalado en este celular.")
                } else {
                    isReady = true

                    // Si había algo pendiente por decir antes de que iniciara
                    pendingText?.let {
                        speak(it)
                        pendingText = null
                    }
                }
            } else {
                Log.e("KIVI_TTS", "Falló la inicialización de voz.")
            }
        }
    }

    fun speak(text: String) {

        // 1) Si la voz está desactivada en Configuración, no hablamos
        if (!KiviSettings.isVoiceEnabled(context)) {
            Log.d("KIVI_TTS", "Voz desactivada por usuario. No se reproducirá audio.")
            return
        }

        // 2) Si TTS aún no está listo, guardamos el texto para decirlo luego
        if (!isReady) {
            Log.e("KIVI_TTS", "La voz aún no está lista. Guardando texto temporalmente.")
            pendingText = text
            return
        }

        // 3) Hablamos normalmente
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
        pendingText = null
    }
}
