package com.example.kiviapp.features.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.kiviapp.R
import com.example.kiviapp.features.ui.activities.base.BaseActivity
import com.example.kiviapp.features.ui.activities.settings.KiviSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


/*
 * Pantalla de registro de nuevos usuarios.
 * Permite crear una cuenta con email y contraseña,
 * guarda los datos personales en Firestore
 * y configura los valores iniciales de accesibilidad.
 */
class RegisterActivity : BaseActivity() {

    // -----------------------------
    // FIREBASE
    // -----------------------------
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar Firebase
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        // -----------------------------
        // REFERENCIAS A VISTAS
        // -----------------------------
        val btnBack = findViewById<ImageButton>(R.id.btnBackRegister)
        val btnRegister = findViewById<Button>(R.id.btnRegisterAction)
        val txtGoLogin = findViewById<TextView>(R.id.txtGoLogin)

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etApellido = findViewById<EditText>(R.id.etApellido)
        val etEmail = findViewById<EditText>(R.id.etRegEmail)
        val etFecha = findViewById<EditText>(R.id.etFecha)
        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val etPass = findViewById<EditText>(R.id.etRegPass)

        btnBack.setOnClickListener { finish() }

        // Ir a login si el usuario ya tiene cuenta
        txtGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // -----------------------------
        // REGISTRO DE USUARIO
        // -----------------------------
        btnRegister.setOnClickListener {
            // Obtener datos ingresados
            val nombre = etNombre.text.toString().trim()
            val apellido = etApellido.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val fecha = etFecha.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val pass = etPass.text.toString().trim()

            // Validación básica
            if (nombre.isNotEmpty() && apellido.isNotEmpty() && email.isNotEmpty() &&
                fecha.isNotEmpty() && telefono.isNotEmpty() && pass.isNotEmpty()
            ) {

                // Validar longitud de contraseña
                if (pass.length < 6) {
                    Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Crear usuario en Firebase Authentication
                auth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid

                            // Datos personales para Firestor
                            val datosUsuario = hashMapOf(
                                "id" to userId,
                                "nombre" to nombre,
                                "apellido" to apellido,
                                "email" to email,
                                "fechaNacimiento" to fecha,
                                "telefono" to telefono
                            )

                            // Guardar datos en Firestore
                            if (userId != null) {
                                db.collection("usuarios").document(userId)
                                    .set(datosUsuario)
                                    .addOnSuccessListener {

                                        //  defaults primera vez (incluye app_language global)
                                        KiviSettings.setTutorialVisto(this, false)
                                        KiviSettings.setAppLanguage(this, "es")
                                        KiviSettings.syncToCloud(this)

                                        Toast.makeText(this, "¡Bienvenido, $nombre!", Toast.LENGTH_SHORT).show()
                                        irAInicio()
                                    }
                                    .addOnFailureListener {

                                        // igual inicializamos settings
                                        KiviSettings.setTutorialVisto(this, false)
                                        KiviSettings.setAppLanguage(this, "es")
                                        KiviSettings.syncToCloud(this)

                                        Toast.makeText(this, "Cuenta creada, pero hubo error guardando datos.", Toast.LENGTH_LONG).show()
                                        irAInicio()
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }

            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*
     * Decide a dónde ir después del registro:
     * - Tutorial (si es nuevo usuario)
     * - Pantalla principal
     */
    private fun irAInicio() {
        // ✅ como es nuevo usuario, normalmente tutorial_visto=false
        val next = if (!KiviSettings.isTutorialVisto(this)) {
            Intent(this, TutorialActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }

        next.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(next)
    }
}
