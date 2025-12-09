package com.example.kiviapp.features.ai

import android.graphics.Bitmap
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class GeminiIntegration {

    // CLAVE
    private val apiKey = "Pn".trim()

    // MODELO: Usamos el 2.5 Flash
    private val modelName = "gemini-2.5-flash"

    // --- FUNCIONES PÚBLICAS ---

    // 1. Para hablar (Solo texto)
    suspend fun getResponse(prompt: String): String {
        return sendRequest(prompt, null)
    }

    // 2. Para ver (Texto + Imagen)
    suspend fun getImageResponse(prompt: String, image: Bitmap): String {
        return sendRequest(prompt, image)
    }

    // --- LÓGICA PRIVADA (El motor) ---

    private suspend fun sendRequest(prompt: String, image: Bitmap?): String = withContext(Dispatchers.IO) {
        try {
            // URL a v1beta (Necesario para modelos 2.0+)
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey")

            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            // PREPARAMOS EL SUPER PROMPT (PERSONALIDAD KIVI)
            // Le decimos quién es, tanto si lee texto como si mira una foto.
            val superPrompt = """
                Actúa como Kivi, un asistente leal y empático para personas con discapacidad visual.
                
                REGLAS OBLIGATORIAS:
                1. RESPUESTAS CORTAS: Máximo 2 frases. Sé directo.
                2. CERO FORMATO: No uses asteriscos (*), ni guiones, ni negritas.
                3. CONTEXTO: Si te envío una imagen, descríbela enfocándote en lo práctico (obstáculos, objetos, textos).
                
                El usuario te dice: "$prompt"
            """.trimIndent()

            // CONSTRUIMOS EL JSON
            val jsonBody = JSONObject()
            val contentsArray = JSONArray()
            val contentObject = JSONObject()
            val partsArray = JSONArray()

            // PARTE A: EL TEXTO (Siempre va)
            val textPart = JSONObject()
            textPart.put("text", superPrompt)
            partsArray.put(textPart)

            // PARTE B: LA IMAGEN (Solo si enviamos una)
            if (image != null) {
                val imagePart = JSONObject()
                val inlineData = JSONObject()
                inlineData.put("mime_type", "image/jpeg")
                inlineData.put("data", bitmapToBase64(image)) // Convertimos foto a código
                imagePart.put("inline_data", inlineData)
                partsArray.put(imagePart)
            }

            contentObject.put("parts", partsArray)
            contentsArray.put(contentObject)
            jsonBody.put("contents", contentsArray)

            // ENVIAR A GOOGLE
            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(jsonBody.toString())
            writer.flush()
            writer.close()

            // LEER RESPUESTA
            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                return@withContext parseResponse(response.toString())
            } else {
                // ERROR
                val errorStream = conn.errorStream ?: conn.inputStream
                val reader = BufferedReader(InputStreamReader(errorStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                return@withContext "ERROR GOOGLE ($responseCode): $sb"
            }

        } catch (e: Exception) {
            return@withContext "Fallo de conexión: ${e.message}"
        }
    }

    // Convertir Bitmap (Imagen Android) a String Base64 (Texto para Google)
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Bajamos calidad a 80 para que sea rápido
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun parseResponse(jsonResponse: String): String {
        return try {
            val json = JSONObject(jsonResponse)
            val candidates = json.getJSONArray("candidates")
            val content = candidates.getJSONObject(0).getJSONObject("content")
            val parts = content.getJSONArray("parts")
            parts.getJSONObject(0).getString("text")
        } catch (e: Exception) {
            "Recibí imagen pero no pude leer la descripción."
        }
    }
}