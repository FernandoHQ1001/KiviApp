package com.example.kiviapp.features.ai

import android.graphics.Bitmap
import android.util.Base64 // codifica datos binarios en texto Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext // cambia temporalmente el contexto de ejecución
import org.json.JSONArray // representa un arreglo JSON
import org.json.JSONObject // representa un objeto JSON
import java.io.BufferedReader // leer la respuesta del servidor línea por línea
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader // interpretar correctamente la respuesta HTTP
import java.io.OutputStreamWriter // escribe datos en la conexión HTTP (enviar el JSON)
import java.net.HttpURLConnection // permite abrir una conexión HTTP
import java.net.URL // representa la dirección del servicio web

    /*
     * Clase encargada de la comunicación directa con la API de Google Gemini.
     * Permite enviar texto o texto + imagen y recibir respuestas accesibles.
     */
class GeminiIntegration {

    //  CLAVE DE LA API
    // TODO: REEMPLAZA "TU_API_KEY_AQUI" POR TU CLAVE REAL DE GOOGLE AI STUDIO
    private val apiKey = "AIzaSyCiFMh96rAWySM1b3EYSE7yJG_WA32QpvM"

    // MODELO: Usamos el 2.5 Flash
    private val modelName = "gemini-2.5-flash"

    // --- FUNCIONES PÚBLICAS ---

    // 1. Para hablar (Solo texto)
    suspend fun getResponse(prompt: String, langCode: String): String {
        return sendRequest(prompt, null, langCode)
    }

    // 2. Para ver (Texto + Imagen)
    suspend fun getImageResponse(prompt: String, image: Bitmap, langCode: String): String {
        return sendRequest(prompt, image, langCode)
    }

    // Compatibilidad si en algún lugar se llama sin idioma (usa español por defecto)
    suspend fun getResponse(prompt: String): String = getResponse(prompt, "es")
    suspend fun getImageResponse(prompt: String, image: Bitmap): String =
        getImageResponse(prompt, image, "es")

    // --- LÓGICA PRIVADA (El motor) ---

    // Envía la solicitud HTTP a Google Gemini.
    private suspend fun sendRequest(
        prompt: String,
        image: Bitmap?,
        langCode: String
    ): String = withContext(Dispatchers.IO) {
        try {

            // Verificación básica de API Key
            if (apiKey == "TU_API_KEY_AQUI") {
                return@withContext "API key no válida. Reemplaza el texto en GeminiIntegration.kt"
            }

            // URL del endpoint de Gemini
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey")

            // Configuración de la conexión HTTP
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            // Selección de idioma para la respuesta
            val nombreIdioma = if (langCode == "en") "inglés" else "español"

            // Prompt base enfocado en accesibilidad visual
            val superPrompt = """
                Actúa como Kivi, un asistente leal y empático para personas con discapacidad visual.
                
                DEBES RESPONDER SIEMPRE EN $nombreIdioma.
                
                REGLAS OBLIGATORIAS:
                1. RESPUESTAS CORTAS: Máximo 2 frases. Sé directo.
                2. CERO FORMATO: No uses asteriscos (*), ni guiones, ni negritas.
                3. CONTEXTO: Si te envío una imagen, descríbela enfocándote en lo práctico (obstáculos, objetos, textos).
                
                El usuario te dice: "$prompt"
            """.trimIndent()

            // -----------------------
            // Construcción del JSON
            // -----------------------
            val jsonBody = JSONObject()
            val contentsArray = JSONArray()
            val contentObject = JSONObject()
            val partsArray = JSONArray()

            // Parte de texto
            val textPart = JSONObject()
            textPart.put("text", superPrompt)
            partsArray.put(textPart)

            // Parte de imagen (si existe)
            if (image != null) {
                val imagePart = JSONObject()
                val inlineData = JSONObject()
                inlineData.put("mime_type", "image/jpeg")
                inlineData.put("data", bitmapToBase64(image))
                imagePart.put("inline_data", inlineData)
                partsArray.put(imagePart)
            }

            // Ensamblado final del cuerpo
            contentObject.put("parts", partsArray)
            contentsArray.put(contentObject)
            jsonBody.put("contents", contentsArray)

            // Envío de la solicitud
            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(jsonBody.toString())
            writer.flush()
            writer.close()

            // -----------------------
            // Lectura de respuesta
            // -----------------------
            val responseCode = conn.responseCode
            if (responseCode == 200) {
                // Respuesta exitosa
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                return@withContext parseResponse(response.toString())
            } else {
                // Manejo de errores HTTP
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
            // Error de red o conexión
            return@withContext "Fallo de conexión: ${e.message}"
        }
    }

    // Convierte una imagen Bitmap a Base64 (formato para la API de Gemini)
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    // Extrae el texto útil de la respuesta JSON de Gemini
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
