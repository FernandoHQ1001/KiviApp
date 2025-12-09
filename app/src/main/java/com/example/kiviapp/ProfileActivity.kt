package com.example.kiviapp

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kiviapp.R
import com.google.android.material.card.MaterialCardView
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

        aplicarTema()
        aplicarTamanos()
    }

    override fun onResume() {
        super.onResume()
        aplicarTema()
        aplicarTamanos()
    }

    // ------------------ TEMA (COLORES) ------------------ //
    private fun aplicarTema() {
        val root = findViewById<ScrollView>(R.id.rootProfile)

        val colorFondo = KiviSettings.getBackgroundColor(this)
        val colorTexto = KiviSettings.getPrimaryTextColor(this)
        val colorSecundario = KiviSettings.getSecondaryTextColor(this)
        val colorTema = KiviSettings.getThemeColor(this)
        val colorCard = KiviSettings.getCardColor(this)
        val iconColor = ColorStateList.valueOf(KiviSettings.getIconColor(this))
        val temaState = ColorStateList.valueOf(colorTema)

        root.setBackgroundColor(colorFondo)

        // Botón atrás
        findViewById<ImageButton>(R.id.btnBackProfile).imageTintList = iconColor

        // Card usuario + card info
        findViewById<MaterialCardView>(R.id.cardUsuario).setCardBackgroundColor(colorCard)

        val cardInfo = findViewById<MaterialCardView>(R.id.cardInfoPersonal)
        cardInfo.setCardBackgroundColor(colorCard)
        cardInfo.strokeColor = colorTema

        // Avatar (fake circle cuadrado)
        findViewById<android.view.View>(R.id.viewAvatar).setBackgroundColor(colorFondo)

        // Títulos y textos
        findViewById<TextView>(R.id.tvTituloPerfil).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvProfileName).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvProfileEmail).setTextColor(colorSecundario)

        findViewById<TextView>(R.id.tvCuentaLabel).setTextColor(colorSecundario)

        findViewById<TextView>(R.id.tvInfoTitulo).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvInfoDescripcion).setTextColor(colorSecundario)

        // Iconos
        findViewById<ImageView>(R.id.iconInfoPersonal).imageTintList = temaState
        findViewById<ImageView>(R.id.iconInfoNext).imageTintList = temaState
    }

    // ------------------ TAMAÑO DE TEXTO ------------------ //
    private fun aplicarTamanos() {
        fun size(base: Float): Float = KiviSettings.getScaledTextSize(this, base)

        // Barra superior
        findViewById<TextView>(R.id.tvTituloPerfil).textSize = size(22f)

        // Card usuario
        findViewById<TextView>(R.id.tvProfileName).textSize = size(18f)
        findViewById<TextView>(R.id.tvProfileEmail).textSize = size(14f)

        // Sección cuenta
        findViewById<TextView>(R.id.tvCuentaLabel).textSize = size(14f)

        // Card info personal
        findViewById<TextView>(R.id.tvInfoTitulo).textSize = size(16f)
        findViewById<TextView>(R.id.tvInfoDescripcion).textSize = size(12f)
    }
}
