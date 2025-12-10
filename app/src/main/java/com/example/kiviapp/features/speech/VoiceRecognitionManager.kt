package com.example.kiviapp.features.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

// Escucha al usuario y convierte su voz en texto para enviárselo a la IA

class VoiceRecognitionManager(context: Context) {

    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES") // Español
    }

    // Esta función recibe un "callback" (una función para devolver la respuesta)
    fun startListening(onResult: (String) -> Unit) {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { Log.d("KIVI_VOICE", "Escuchando...") }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { Log.d("KIVI_VOICE", "Procesando voz...") }
            override fun onError(error: Int) {
                Log.e("KIVI_VOICE", "Error de voz código: $error")
                // Truco: Si falla, le mandamos un texto de error para que la app se destrabe
                onResult("No te escuché bien, intenta de nuevo.")
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spokenText = matches[0]
                    Log.d("KIVI_VOICE", "Usuario dijo: $spokenText")
                    onResult(spokenText) // Devolvemos el texto escuchado
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(speechIntent)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
    }
}