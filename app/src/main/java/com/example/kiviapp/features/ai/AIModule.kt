package com.example.kiviapp.features.ai

import com.example.kiviapp.features.ai.models.AIResponse

/* Centraliza el flujo de la IA en Kivi
    * Se encarga de:
    * - Preparar el prompt
    * - Enviar la solicitud a Gemini
    * - Procesar la respuesta
*/
class AIModule {
    // Encargado de comunicarse directamente con la IA
    private val gemini = GeminiIntegration()

    // Se encarga de mejorar y adaptar el texto del usuario
    private val prompt = PromptManager()

    // Procesa y limpia la respuesta cruda de la IA
    private val processor = ResponseProcessor()

    // Procesa la entrada del usuario y devuelve una respuesta estructurada.
    suspend fun processUserInput(text: String): AIResponse {
        val enriched = prompt.enrichPrompt(text) // enriquece el texto del usuario para hacerlo más claro para la IA
        val rawResponse = gemini.getResponse(enriched) // envía el prompt a la IA y obtiene la respuesta cruda
        return processor.process(rawResponse) // procesa la respuesta cruda y la convierte en un objeto AIResponse
    }
}