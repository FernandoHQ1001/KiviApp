package com.example.kiviapp.features.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {

    private val executor = Executors.newSingleThreadExecutor()

    fun startCamera(onFrame: (Bitmap) -> Unit) {
        val providerFuture = ProcessCameraProvider.getInstance(context)

        providerFuture.addListener({
            val provider = providerFuture.get()

            val preview = Preview.Builder().build()

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(executor) { image ->
                val bitmap = image.toBitmap()
                if (bitmap != null) onFrame(bitmap)
                image.close()
            }

            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis
            )

        }, ContextCompat.getMainExecutor(context))
    }

    // -------------------------------------------------------
    // EXTENSIÓN CORRECTA PARA CONVERTIR ImageProxy → Bitmap
    // -------------------------------------------------------
    private fun ImageProxy.toBitmap(): Bitmap? {
        return try {
            // Extraer buffers Y, U, V
            val yBuffer = planes[0].buffer
            val uBuffer = planes[1].buffer
            val vBuffer = planes[2].buffer

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)

            // Copiar datos YUV en el arreglo final
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            // Convertir a JPEG
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, width, height), 90, out)

            val jpegBytes = out.toByteArray()

            // Decodificar a Bitmap
            BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)

        } catch (e: Exception) {
            Log.e("CameraManager", "Error converting ImageProxy to Bitmap", e)
            null
        }
    }
}
