package com.example.kiviapp.features.ai.models


// Modelo de datos que representa la respuesta de la IA
data class AIResponse(
    val text: String, // texto limpio que se mostrará al usuario
    val raw: String // repuesta original de la IA
)