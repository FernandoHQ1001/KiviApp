package com.example.kiviapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Iniciamos Firebase Auth
        auth = Firebase.auth

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnLoginAction = findViewById<Button>(R.id.btnLoginAction)

        btnBack.setOnClickListener { finish() }

        btnLoginAction.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {

                // --- LOGIN REAL CON FIREBASE ---
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "¡Bienvenido de nuevo!", Toast.LENGTH_SHORT).show()

                            // 1) Cargar configuraciones desde Firebase
                            KiviSettings.loadFromCloud(this) {
                                // 2) Vayas o no con settings remotos, pasamos al inicio
                                irAInicio()
                            }

                        } else {
                            Toast.makeText(
                                this,
                                "Error: Correo o contraseña incorrectos",
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
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
