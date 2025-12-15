package com.example.kiviapp.features.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.kiviapp.features.ui.activities.settings.KiviSettings

// Escucha al usuario y convierte su voz en texto para enviárselo a la IA
class VoiceRecognitionManager(context: Context) {

    private val appContext = context.applicationContext

    // Motor de reconocimiento de voz de Android
    private val speechRecognizer: SpeechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(appContext)

    /**
     * Crea el Intent de reconocimiento de voz
     * configurando el idioma según preferencias del usuario
     */
    private fun createSpeechIntent(): Intent {
        val langCode = KiviSettings.getVoiceLanguage(appContext)  // Obtiene el idioma configurado
        val langTag = if (langCode == "en") "en-US" else "es-ES"  // Traduce el código a un tag compatible con Android

        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)  // Modelo libre para reconocer lenguaje natural
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)  // Idioma principal
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag) // Preferencia de idioma
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, langTag) // Asegura que solo se use ese idioma
        }
    }


    // Esta función recibe un "callback" (una función para devolver la respuesta)
    fun startListening(onResult: (String) -> Unit) {

        val speechIntent = createSpeechIntent()

        // Configura el listener de eventos de voz
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { Log.d("KIVI_VOICE", "Escuchando...") }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { Log.d("KIVI_VOICE", "Procesando voz...") }
            override fun onError(error: Int) {
                Log.e("KIVI_VOICE", "Error de voz código: $error")
                onResult("No te escuché bien, intenta de nuevo.")
            }

            override fun onResults(results: Bundle?) {
                // Obtiene las posibles transcripciones
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                // Si hay resultados, se toma el más probable
                if (!matches.isNullOrEmpty()) {
                    val spokenText = matches[0]
                    Log.d("KIVI_VOICE", "Usuario dijo: $spokenText")
                    onResult(spokenText)  // Devuelve el texto reconocido
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        // Inicia el reconocimiento de voz
        speechRecognizer.startListening(speechIntent)
    }

    // Detiene la escucha del micrófono
    fun stopListening() {
        speechRecognizer.stopListening()
    }
}
