package com.example.kiviapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Referencias con los IDs del nuevo diseño oscuro
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnLoginAction = findViewById<Button>(R.id.btnLoginAction)

        // Botón volver
        btnBack.setOnClickListener { finish() }

        // Botón Iniciar Sesión
        btnLoginAction.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString().trim()

            // Validación simple: Que no estén vacíos
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                // SIMULACIÓN DE ÉXITO
                Toast.makeText(this, "¡Bienvenido de nuevo!", Toast.LENGTH_SHORT).show()
                irAlInicio()
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun irAlInicio() {
        val intent = Intent(this, MainActivity::class.java)
        // Borramos el historial para que al dar "Atrás" no vuelva al login
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}