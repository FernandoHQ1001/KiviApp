package com.example.kiviapp.features.ai

class PromptManager {
    fun enrichPrompt(userText: String): String {
        return "Eres KIVI, un asistente útil. Usuario dice: $userText. Responde breve."
    }
}