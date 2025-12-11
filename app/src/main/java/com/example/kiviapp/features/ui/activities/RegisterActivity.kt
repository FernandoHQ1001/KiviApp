package com.example.kiviapp.features.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kiviapp.features.ui.activities.settings.KiviSettings
import com.example.kiviapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // 1. Iniciamos las herramientas de Firebase
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        // Referencias
        val btnBack = findViewById<ImageButton>(R.id.btnBackRegister)
        val btnRegister = findViewById<Button>(R.id.btnRegisterAction)
        val txtGoLogin = findViewById<TextView>(R.id.txtGoLogin)

        // Campos de texto
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etApellido = findViewById<EditText>(R.id.etApellido)
        val etEmail = findViewById<EditText>(R.id.etRegEmail)
        val etFecha = findViewById<EditText>(R.id.etFecha)
        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val etPass = findViewById<EditText>(R.id.etRegPass)

        btnBack.setOnClickListener { finish() }

        txtGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnRegister.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val apellido = etApellido.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val fecha = etFecha.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val pass = etPass.text.toString().trim()

            // Validaciones
            if (nombre.isNotEmpty() && apellido.isNotEmpty() && email.isNotEmpty() &&
                fecha.isNotEmpty() && telefono.isNotEmpty() && pass.isNotEmpty()
            ) {

                if (pass.length < 6) {
                    Toast.makeText(
                        this,
                        "La contraseña debe tener al menos 6 caracteres",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                // --- AQUÍ OCURRE LA MAGIA DE FIREBASE ---

                // A. Crear usuario en Authentication (Email/Pass)
                auth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid

                            // B. Guardar datos personales en Firestore (Base de Datos)
                            val datosUsuario = hashMapOf(
                                "id" to userId,
                                "nombre" to nombre,
                                "apellido" to apellido,
                                "email" to email,
                                "fechaNacimiento" to fecha,
                                "telefono" to telefono
                            )

                            if (userId != null) {
                                db.collection("usuarios").document(userId)
                                    .set(datosUsuario)
                                    .addOnSuccessListener {
                                        // C. Sincronizar settings iniciales del usuario a Firebase
                                        KiviSettings.syncToCloud(this)

                                        Toast.makeText(
                                            this,
                                            "¡Bienvenido, $nombre!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        irAInicio()
                                    }
                                    .addOnFailureListener {
                                        // Si falla guardar datos, igual entramos pero avisamos
                                        Toast.makeText(
                                            this,
                                            "Cuenta creada, pero hubo error guardando datos.",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        // Igual subimos settings para que estén en la nube
                                        KiviSettings.syncToCloud(this)

                                        irAInicio()
                                    }
                            }
                        } else {
                            // Error al crear cuenta (ej: correo ya existe)
                            Toast.makeText(
                                this,
                                "Error: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(
                    this,
                    "Por favor completa todos los campos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun irAInicio() {
        val intent = Intent(this, MainActivity::class.java)
        // Borrar historial para que no pueda volver atrás al registro
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
