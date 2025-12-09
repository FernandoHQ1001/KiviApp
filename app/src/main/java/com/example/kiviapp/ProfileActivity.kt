package com.example.kiviapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class ProfileActivity : AppCompatActivity() {

    private val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val btnBack = findViewById<ImageButton>(R.id.btnBackProfile)
        val tvNombre = findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = findViewById<TextView>(R.id.tvProfileEmail)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val usuario = auth.currentUser
        if (usuario == null) {
            // Si no hay usuario logueado, mandar al login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Email directo de FirebaseAuth
        tvEmail.text = usuario.email ?: "Sin email"

        // Leer datos adicionales de Firestore
        val userId = usuario.uid
        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nombre = doc.getString("nombre") ?: ""
                    val apellido = doc.getString("apellido") ?: ""
                    tvNombre.text = "$nombre $apellido"
                } else {
                    tvNombre.text = "Usuario sin datos"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando perfil", Toast.LENGTH_SHORT).show()
            }
    }
}
