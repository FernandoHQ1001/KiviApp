package com.example.kiviapp.features.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.example.kiviapp.features.ui.activities.base.BaseActivity
import com.example.kiviapp.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


/*
 * Pantalla inicial de la aplicación.
  - Verifica si el usuario ya está autenticado.
  - Si hay sesión activa, redirige directamente al MainActivity.
  - Si no, muestra opciones de Login y Registro.
 */
class WelcomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --------------------------------------------------
        // VERIFICACIÓN DE SESIÓN ACTIVA
        // --------------------------------------------------
        // Firebase mantiene la sesión del usuario incluso
        // después de cerrar la app.
        val usuarioActual = Firebase.auth.currentUser

        if (usuarioActual != null) {
            // Hay un usuario autenticado
            // Saltamos directamente a la pantalla principal
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Cerramos WelcomeActivity
            return // Detenemos la carga de esta pantalla
        }

        // --------------------------------------------------
        // NO HAY SESIÓN → MOSTRAR BIENVENIDA
        // --------------------------------------------------
        setContentView(R.layout.activity_welcome)

        // Botones
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        // Ir a Login
        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Ir a Registro
        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}