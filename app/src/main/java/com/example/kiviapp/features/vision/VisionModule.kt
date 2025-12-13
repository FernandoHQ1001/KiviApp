package com.example.kiviapp.features.vision

import android.graphics.Bitmap
import com.example.kiviapp.features.vision.models.VisionResult

class VisionModule(
    private val objectDetection: ObjectDetector = ObjectDetector(),
    private val ocrProcessor: OCRProcessor = OCRProcessor(),
    private val colorAnalyzer: ColorAnalyzer = ColorAnalyzer()
) {

    fun analyzeImage(
        bitmap: Bitmap,
        callback: (VisionResult) -> Unit,
        detectObjects: Boolean = true,
        detectText: Boolean = true,
        analyzeColor: Boolean = true
    ) {

        if (detectObjects) {
            objectDetection.detect(bitmap) { objects ->
                val labels = objects.flatMap { obj ->
                    obj.labels.map { it.text }
                }
                if (labels.isNotEmpty()) {
                    callback(VisionResult.Objects(labels))
                }
            }
        }

        if (detectText) {
            ocrProcessor.readText(bitmap) { text ->
                if (text.isNotBlank()) {
                    callback(VisionResult.Text(text))
                }
            }
        }

        if (analyzeColor) {
            val color = colorAnalyzer.analyze(bitmap)
            callback(VisionResult.Color(color.hex))
        }
    }
}
