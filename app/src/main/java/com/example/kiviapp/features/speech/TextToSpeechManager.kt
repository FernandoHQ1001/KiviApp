package com.example.kiviapp.features.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

// Se encarga de convertir el texto de la IA en audio
class TextToSpeechManager(context: Context) {

    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        // Inicializamos el motor de voz de Android
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Configuramos el idioma a Español
                val result = tts?.setLanguage(Locale("es", "ES"))

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("KIVI_TTS", "El idioma español no está instalado en este celular.")
                } else {
                    isReady = true
                }
            } else {
                Log.e("KIVI_TTS", "Falló la inicialización de voz.")
            }
        }
    }

    fun speak(text: String) {
        if (isReady) {
            // Hablamos el texto
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.e("KIVI_TTS", "La voz aún no está lista.")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}