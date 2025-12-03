package com.example.kiviapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Referencias a TODOS los campos del nuevo diseño
        val btnBack = findViewById<ImageButton>(R.id.btnBackRegister)
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etApellido = findViewById<EditText>(R.id.etApellido)
        val etEmail = findViewById<EditText>(R.id.etRegEmail)
        val etFecha = findViewById<EditText>(R.id.etFecha)
        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val etPass = findViewById<EditText>(R.id.etRegPass)
        val btnRegister = findViewById<Button>(R.id.btnRegisterAction)
        val txtGoLogin = findViewById<TextView>(R.id.txtGoLogin)

        // Botón volver
        btnBack.setOnClickListener { finish() }

        // Texto "¿Ya tienes cuenta?"
        txtGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Botón Registrarse
        btnRegister.setOnClickListener {
            // Obtenemos el texto de cada campo
            val nombre = etNombre.text.toString().trim()
            val apellido = etApellido.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val fecha = etFecha.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val pass = etPass.text.toString().trim()

            // Validamos que NADA esté vacío
            if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() ||
                fecha.isEmpty() || telefono.isEmpty() || pass.isEmpty()) {

                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // SIMULACIÓN DE ÉXITO
            Toast.makeText(this, "¡Cuenta creada! Bienvenido $nombre", Toast.LENGTH_SHORT).show()
            irAlInicio()
        }
    }

    private fun irAlInicio() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}