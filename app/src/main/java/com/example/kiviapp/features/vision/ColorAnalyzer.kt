package com.example.kiviapp.features.vision

import android.graphics.Bitmap

data class ColorResult(
    val r: Int,
    val g: Int,
    val b: Int,
    val hex: String
)

class ColorAnalyzer {

    fun analyze(bitmap: Bitmap): ColorResult {
        val x = bitmap.width / 2
        val y = bitmap.height / 2

        val pixel = bitmap.getPixel(x, y)

        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF

        return ColorResult(
            r, g, b,
            String.format("#%02X%02X%02X", r, g, b)
        )
    }
}
