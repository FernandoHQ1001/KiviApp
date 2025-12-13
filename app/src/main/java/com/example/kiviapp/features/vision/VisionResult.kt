package com.example.kiviapp.features.vision.models

sealed class VisionResult {
    data class Objects(val list: List<String>) : VisionResult()
    data class Text(val text: String) : VisionResult()
    data class Color(val hex: String) : VisionResult()
}
