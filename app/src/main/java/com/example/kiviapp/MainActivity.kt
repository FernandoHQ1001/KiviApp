package com.example.kiviapp

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kiviapp.core.KiviOrchestrator
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import android.content.res.ColorStateList

import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


class MainActivity : AppCompatActivity(), KiviOrchestrator.KiviListener {

    // UI
    private lateinit var txtEstado: TextView
    private lateinit var btnEscuchar: Button
    private lateinit var btnCamara: Button
    private lateinit var imgFoto: ImageView

    // Orquestador
    private lateinit var orquestador: KiviOrchestrator

    // Estado
    private var estaEscuchando = false
    private var fotoActual: Bitmap? = null

    // Permisos
    private val PERMISO_MICROFONO = 100
    private val PERMISO_CAMARA = 101

    private val tomarFotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imagen = result.data?.extras?.get("data") as? Bitmap
                if (imagen != null) {
                    fotoActual = imagen
                    imgFoto.visibility = android.view.View.VISIBLE
                    imgFoto.setImageBitmap(imagen)

                    txtEstado.text = "Foto lista. Pregúntame."
                    orquestador.decir("Foto lista. Pregúntame.")
                }
            }
        }

    // ---------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencias UI
        txtEstado = findViewById(R.id.txtEstado)
        btnEscuchar = findViewById(R.id.btnEscuchar)
        btnCamara = findViewById(R.id.btnCamara)
        imgFoto = findViewById(R.id.imgFoto)

        aplicarTema()
        aplicarTamanoTexto()

        // BOTONES SUPERIORES
        val btnPerfil = findViewById<ImageButton>(R.id.btnPerfil)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)
        val btnVoiceNav = findViewById<ImageButton>(R.id.btnVoiceNav)
        val btnIdioma = findViewById<ImageButton>(R.id.btnIdioma)

        btnPerfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnLogout.setOnClickListener {
            mostrarDialogoCerrarSesion()
        }

        btnVoiceNav.setOnClickListener {
            startActivity(Intent(this, VoiceNavigationActivity::class.java))
        }

        // 🆕 Botón Idioma → abre pantalla de Idioma
        btnIdioma.setOnClickListener {
            startActivity(Intent(this, LanguageSettingsActivity::class.java))
        }

        // 2. Iniciar Orquestador
        orquestador = KiviOrchestrator(this)
        orquestador.setListener(this)

        // BOTÓN ESCUCHAR
        btnEscuchar.setOnClickListener {
            if (estaEscuchando) {
                orquestador.detenerEscucha()
                resetearBotonEscuchar()
            } else {
                verificarPermisoMicrofono()
            }
        }

        // BOTÓN CÁMARA
        btnCamara.setOnClickListener { verificarPermisoCamara() }

        // Saludo inicial
        orquestador.saludar()
    }

    override fun onResume() {
        super.onResume()
        aplicarTema()
        aplicarTamanoTexto()
    }

    // ---------------------------------------------------------------------------------------------
    // RESPUESTAS DESDE EL ORQUESTADOR
    override fun onEstadoCambiado(texto: String) {
        txtEstado.text = texto
    }

    override fun onKiviHablando(texto: String) {}

    override fun onError(mensaje: String) {
        txtEstado.text = "Error: $mensaje"
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        resetearBotonEscuchar()
    }

    // ---------------------------------------------------------------------------------------------
    // TAMAÑO DE TEXTO
    private fun aplicarTamanoTexto() {
        val nivel = KiviSettings.getTextSizeLevel(this)
        val factor = when (nivel) {
            0 -> 0.80f
            2 -> 1.30f
            else -> 1.00f
        }

        txtEstado.textSize = 20f * factor
        btnEscuchar.textSize = 16f * factor
        btnCamara.textSize = 16f * factor
    }

    // ---------------------------------------------------------------------------------------------
    // TEMA
    private fun aplicarTema() {
        val root = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.rootMain)

        val colorFondo = KiviSettings.getBackgroundColor(this)
        val colorCard = KiviSettings.getCardColor(this)
        val colorTexto = KiviSettings.getPrimaryTextColor(this)
        val colorSecundario = KiviSettings.getSecondaryTextColor(this)
        val colorTema = KiviSettings.getThemeColor(this)
        val temaState = ColorStateList.valueOf(colorTema)

        root.setBackgroundColor(colorFondo)

        // 🔹 Card principal (imagen + texto)
        val cardMain = findViewById<MaterialCardView>(R.id.cardMain)
        cardMain.setCardBackgroundColor(colorCard)

        // Imagen dentro del card (fondo acorde al tema)
        imgFoto.setBackgroundColor(colorCard)

        // Texto dentro del card
        txtEstado.setTextColor(colorTexto)

        // Header
        val tvHeader = findViewById<TextView>(R.id.tvHeader)
        tvHeader.setTextColor(colorSecundario)

        // Iconos superiores
        val iconColor = KiviSettings.getIconColor(this)
        val iconState = ColorStateList.valueOf(iconColor)

        val btnPerfil = findViewById<ImageButton>(R.id.btnPerfil)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)
        val btnVoiceNav = findViewById<ImageButton>(R.id.btnVoiceNav)
        val btnIdioma = findViewById<ImageButton>(R.id.btnIdioma)

        btnPerfil.imageTintList = iconState
        btnSettings.imageTintList = iconState
        btnLogout.imageTintList = iconState
        btnVoiceNav.imageTintList = iconState
        btnIdioma.imageTintList = iconState

        // Botón escuchar
        (btnEscuchar as MaterialButton).backgroundTintList = temaState
        btnEscuchar.setTextColor(0xFFFFFFFF.toInt())

        // Botón cámara
        (btnCamara as MaterialButton).strokeColor = temaState
        btnCamara.setTextColor(colorTema)
        (btnCamara as MaterialButton).iconTint = temaState
    }

    // ---------------------------------------------------------------------------------------------
    // PERMISOS
    private fun verificarPermisoMicrofono() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISO_MICROFONO
            )
        } else {
            activarModoEscucha()
        }
    }

    private fun activarModoEscucha() {
        estaEscuchando = true
        btnEscuchar.text = "🛑 DETENER"
        btnEscuchar.setBackgroundColor(getColor(android.R.color.holo_red_light))

        orquestador.empezarEscucha { texto ->
            txtEstado.text = "Tú: $texto"
            orquestador.procesarPregunta(texto, fotoActual)
            resetearBotonEscuchar()
        }
    }

    private fun resetearBotonEscuchar() {
        estaEscuchando = false
        btnEscuchar.text = "HABLAR CON KIVI 🎤"

        val colorTema = KiviSettings.getThemeColor(this)
        (btnEscuchar as MaterialButton).backgroundTintList =
            ColorStateList.valueOf(colorTema)
    }

    private fun verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                PERMISO_CAMARA
            )
        } else abrirCamara()
    }

    private fun abrirCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            tomarFotoLauncher.launch(intent)
        } catch (_: Exception) {}
    }

    // ---------------------------------------------------------------------------------------------
    // CONFIRMACIÓN DE CERRAR SESIÓN
    private fun mostrarDialogoCerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Seguro que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _: DialogInterface, _: Int ->

                // 1) Cerrar sesión en Google (si estaba usando Google)
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

                val googleClient = GoogleSignIn.getClient(this, gso)
                googleClient.signOut()

                // 2) Cerrar sesión en Firebase
                Firebase.auth.signOut()

                // 3) Volver a la pantalla de bienvenida
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    // ---------------------------------------------------------------------------------------------
    override fun onDestroy() {
        orquestador.liberarRecursos()
        super.onDestroy()
    }
}
