package com.example.kiviapp.features.ui.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.example.kiviapp.features.ui.activities.base.BaseActivity
import com.example.kiviapp.features.ui.activities.settings.KiviSettings
import com.example.kiviapp.R
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


/*
 * Pantalla de perfil del usuario.
 * Muestra información básica:
  - Nombre completo
  - Correo electrónico
  - Foto de perfil

 * Permite navegar a la edición de información personal.
 * Los datos se obtienen desde Firebase Auth y Firestore.
 */
class ProfileActivity : BaseActivity() {

    // -----------------------------
    // FIREBASE
    // -----------------------------
    private val db = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth

    // -----------------------------
    // VISTAS
    // -----------------------------
    private lateinit var imgAvatar: ImageView
    private lateinit var tvNombre: TextView
    private lateinit var tvEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Botón volver
        val btnBack = findViewById<ImageButton>(R.id.btnBackProfile)

        // Referencias UI
        imgAvatar = findViewById(R.id.imgAvatar)
        tvNombre = findViewById(R.id.tvProfileName)
        tvEmail = findViewById(R.id.tvProfileEmail)

        btnBack.setOnClickListener { finish() }

        // Ir a Información Personal
        findViewById<MaterialCardView>(R.id.cardInfoPersonal).setOnClickListener {
            startActivity(Intent(this, PersonalInfoActivity::class.java))
        }

        cargarDatosUsuario()
        aplicarTema()
        aplicarTamanos()
    }

    override fun onResume() {
        super.onResume()

        // Se recargan datos por si el usuario editó su información
        aplicarTema()
        aplicarTamanos()
        cargarDatosUsuario()
    }

    // =========================================================
    // CARGAR DATOS DEL USUARIO
    // =========================================================
    private fun cargarDatosUsuario() {
        val user = auth.currentUser ?: return

        // Email viene directamente de Firebase Auth
        tvEmail.text = user.email ?: "Sin email"

        // Datos adicionales desde Firestore
        db.collection("usuarios").document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // Nombre completo
                    val nombre = doc.getString("nombre") ?: ""
                    val apellido = doc.getString("apellido") ?: ""
                    tvNombre.text = "$nombre $apellido"

                    // Foto de perfil (Base64)
                    val base64Foto = doc.getString("fotoPerfilBase64")
                    if (!base64Foto.isNullOrEmpty()) {
                        try {
                            val bytes = Base64.decode(base64Foto, Base64.DEFAULT)
                            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            imgAvatar.setImageBitmap(bmp)
                        } catch (e: Exception) {
                            // Imagen corrupta o error al decodificar
                            imgAvatar.setImageResource(android.R.drawable.ic_menu_camera)
                        }
                    } else {
                        // Sin foto de perfil
                        imgAvatar.setImageResource(android.R.drawable.ic_menu_camera)
                    }
                } else {
                    tvNombre.text = "Usuario sin datos"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando perfil", Toast.LENGTH_SHORT).show()
            }
    }

    // =========================================================
    // APLICAR TEMA (COLORES)
    // =========================================================
    private fun aplicarTema() {
        val root = findViewById<ScrollView>(R.id.rootProfile)
        val cardProfile = findViewById<MaterialCardView>(R.id.cardProfile)
        val cardInfo = findViewById<MaterialCardView>(R.id.cardInfoPersonal)

        val colorFondo = KiviSettings.getBackgroundColor(this)
        val colorCard = KiviSettings.getCardColor(this)
        val colorTexto = KiviSettings.getPrimaryTextColor(this)
        val colorSecundario = KiviSettings.getSecondaryTextColor(this)
        val colorTema = KiviSettings.getThemeColor(this)
        val temaState = ColorStateList.valueOf(colorTema)

        root.setBackgroundColor(colorFondo)
        cardProfile.setCardBackgroundColor(colorCard)
        cardInfo.setCardBackgroundColor(colorCard)
        cardInfo.strokeColor = colorTema

        // Textos
        findViewById<TextView>(R.id.tvProfileHeader).setTextColor(colorTexto)
        tvNombre.setTextColor(colorTexto)
        tvEmail.setTextColor(colorSecundario)

        findViewById<TextView>(R.id.tvCuenta).setTextColor(colorSecundario)
        findViewById<TextView>(R.id.tvInfoTitulo).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvInfoDescripcion).setTextColor(colorSecundario)

        // Íconos del card información personal
        val iconInfo = findViewById<ImageView>(R.id.iconInfoPersonal)
        val iconNext = findViewById<ImageView>(R.id.iconInfoNext)
        iconInfo.imageTintList = temaState
        iconNext.imageTintList = temaState
    }

    // =========================================================
    // APLICAR TAMAÑOS DE TEXTO (ACCESIBILIDAD)
    // =========================================================
    private fun aplicarTamanos() {
        fun size(base: Float) = KiviSettings.getScaledTextSize(this, base)

        findViewById<TextView>(R.id.tvProfileHeader).textSize = size(22f)
        tvNombre.textSize = size(18f)
        tvEmail.textSize = size(14f)
        findViewById<TextView>(R.id.tvCuenta).textSize = size(14f)
        findViewById<TextView>(R.id.tvInfoTitulo).textSize = size(16f)
        findViewById<TextView>(R.id.tvInfoDescripcion).textSize = size(12f)
    }
}
