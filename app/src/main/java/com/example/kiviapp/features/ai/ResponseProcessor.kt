package com.example.kiviapp.features.ai

import com.example.kiviapp.features.ai.models.AIResponse

// Tranforma la respuesta cruda de la IA a limpio
class ResponseProcessor {
    fun process(raw: String): AIResponse {
        val clean = raw.replace("*", "").trim()
        return AIResponse(clean, clean)
    }
}