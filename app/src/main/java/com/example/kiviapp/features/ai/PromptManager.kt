package com.example.kiviapp.features.ai

// Enriquece el texto del usuario antes de enviarlo a la IA
class PromptManager {
    fun enrichPrompt(text: String): String {
        return "Usuario dice: $text"
    }
}