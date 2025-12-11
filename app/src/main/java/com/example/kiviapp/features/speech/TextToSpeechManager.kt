package com.example.kiviapp.features.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.kiviapp.features.ui.activities.settings.KiviSettings
import java.util.Locale

// Se encarga de convertir el texto de la IA en audio
class TextToSpeechManager(context: Context) {

    private val appContext = context.applicationContext

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var pendingText: String? = null

    // Guardamos el último idioma aplicado para no reconfigurar a lo loco
    private var lastLangCode: String? = null

    init {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d("KIVI_TTS", "TTS inicializado correctamente")
                // Configuramos el idioma inicial desde ajustes
                aplicarIdiomaDesdeAjustes()
            } else {
                Log.e("KIVI_TTS", "Falló la inicialización de voz.")
            }
        }
    }

    /**
     * Lee KiviSettings y aplica el idioma actual al motor TTS
     * solo si es diferente al último aplicado.
     */
    private fun aplicarIdiomaDesdeAjustes() {
        val langCode = KiviSettings.getVoiceLanguage(appContext) // "es" o "en"

        // Si es el mismo que ya estaba, no hacemos nada
        if (langCode == lastLangCode && isReady) return

        val locale = when (langCode) {
            "en" -> Locale("en", "US")
            else -> Locale("es", "ES")
        }

        val result = tts?.setLanguage(locale)

        if (result == TextToSpeech.LANG_MISSING_DATA ||
            result == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            Log.e("KIVI_TTS", "El idioma seleccionado ($langCode) no está instalado en este celular.")
            isReady = false
        } else {
            Log.d("KIVI_TTS", "Idioma TTS aplicado: $langCode")
            lastLangCode = langCode
            isReady = true

            // Si había algo pendiente por decir antes de que estuviera listo
            pendingText?.let {
                speak(it)
                pendingText = null
            }
        }
    }

    fun speak(text: String) {

        // 1) Si la voz está desactivada en Configuración, no hablamos
        if (!KiviSettings.isVoiceEnabled(appContext)) {
            Log.d("KIVI_TTS", "Voz desactivada por usuario. No se reproducirá audio.")
            return
        }

        // 2) Asegurarnos de que el idioma actual del TTS coincide con KiviSettings
        aplicarIdiomaDesdeAjustes()

        // 3) Si TTS aún no está listo, guardamos el texto para decirlo luego
        if (!isReady) {
            Log.e("KIVI_TTS", "La voz aún no está lista. Guardando texto temporalmente.")
            pendingText = text
            return
        }

        // 4) Hablamos normalmente
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
        pendingText = null
        lastLangCode = null
    }
}
