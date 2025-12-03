package com.example.kiviapp.features.ai

import com.example.kiviapp.features.ai.models.AIResponse

class AIModule {
    private val gemini = GeminiIntegration()
    private val prompt = PromptManager()
    private val processor = ResponseProcessor()

    suspend fun processUserInput(text: String): AIResponse {
        val enriched = prompt.enrichPrompt(text)
        val rawResponse = gemini.getResponse(enriched)
        return processor.process(rawResponse)
    }
}