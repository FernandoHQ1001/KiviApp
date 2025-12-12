package com.example.kiviapp.features.ui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.kiviapp.R

class TutorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        val videoView = findViewById<VideoView>(R.id.videoViewTutorial)
        val btnOmitir = findViewById<Button>(R.id.btnOmitir)

        // 1. Cargar el video desde la carpeta res/raw
        // Asegúrate de poner tu archivo de video (ej: tutorial.mp4) en la carpeta res/raw
        val path = "android.resource://" + packageName + "/" + R.raw.tutorial_video
        videoView.setVideoURI(Uri.parse(path))

        // 2. Iniciar reproducción automática
        videoView.start()

        // 3. Cuando el video termine, ir al Main
        videoView.setOnCompletionListener {
            irAMainActivity()
        }

        // 4. Botón para saltar el video
        btnOmitir.setOnClickListener {
            videoView.stopPlayback()
            irAMainActivity()
        }
    }

    private fun irAMainActivity() {
        // Guardar que ya vio el tutorial para no mostrarlo de nuevo
        val prefs = getSharedPreferences("KiviPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("tutorial_visto", true).apply()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}