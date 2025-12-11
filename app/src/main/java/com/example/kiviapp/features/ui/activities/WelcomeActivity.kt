package com.example.kiviapp.features.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.kiviapp.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- VERIFICACIÓN DE SESIÓN ---
        // Preguntamos a Firebase: "¿Hay alguien logueado?"
        val usuarioActual = Firebase.auth.currentUser

        if (usuarioActual != null) {
            // SÍ -> Saltamos directo a Kivi
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return // Detenemos la carga de esta pantalla
        }

        // NO -> Mostramos la pantalla de bienvenida normal
        setContentView(R.layout.activity_welcome)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}