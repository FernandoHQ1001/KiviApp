package com.example.kiviapp.features.ui.activities

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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kiviapp.core.KiviOrchestrator
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import android.content.res.ColorStateList
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.kiviapp.features.ui.activities.base.BaseActivity
import com.example.kiviapp.features.ui.activities.settings.KiviSettings
import com.example.kiviapp.features.ui.activities.settings.LanguageSettingsActivity
import com.example.kiviapp.R
import com.example.kiviapp.features.ui.activities.settings.SettingsActivity
import com.example.kiviapp.features.ui.activities.settings.VoiceNavigationActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


/*
 * Pantalla principal de Kivi.
 * Permite al usuario:
 * - Hablar con la IA (voz)
 * - Tomar fotos para análisis visual
 * - Acceder a perfil, configuración y navegación por voz

 * Funciona como puente entre la UI y el KiviOrchestrator.
 */
class MainActivity : BaseActivity(), KiviOrchestrator.KiviListener {

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

    /*
     * Launcher para capturar fotos con la cámara
     */
    private val tomarFotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imagen = result.data?.extras?.get("data") as? Bitmap
                if (imagen != null) {
                    fotoActual = imagen
                    imgFoto.visibility = View.VISIBLE
                    imgFoto.setImageBitmap(imagen)

                    txtEstado.text = getString(R.string.photo_ready)
                    orquestador.decir(getString(R.string.photo_ready))
                }
            }
        }

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
            startActivity(Intent(this, AccountActionsActivity::class.java))
        }

        btnVoiceNav.setOnClickListener {
            startActivity(Intent(this, VoiceNavigationActivity::class.java))
        }

        // Botón Idioma → abre pantalla de Idioma
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

        // Saludo eliminado: ahora se hace automáticamente cuando el TTS esté listo

        // BOTÓN PRUEBA DE VOZ
        val btnPruebaVoz = findViewById<Button>(R.id.btnPruebaVoz)
        btnPruebaVoz.setOnClickListener {
            orquestador.decir("Esta es una prueba de voz. Si me escuchas, todo funciona.")
        }
    }

    override fun onResume() {
        super.onResume()
        aplicarTema()
        aplicarTamanoTexto()
    }

    // =========================================================
    // CALLBACKS DEL ORQUESTADOR
    // =========================================================
    override fun onEstadoCambiado(texto: String) {
        txtEstado.text = texto
    }

    override fun onKiviHablando(texto: String) {
        runOnUiThread {
            txtEstado.text = texto
        }
    }

    override fun onError(mensaje: String) {
        txtEstado.text = getString(R.string.error_format, mensaje)
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        resetearBotonEscuchar()
    }

    // -----------------------------------------------------------------------------------------
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

    // -----------------------------------------------------------------------------------------
    // TEMA
    private fun aplicarTema() {
        val root = findViewById<ConstraintLayout>(R.id.rootMain)

        val colorFondo = KiviSettings.getBackgroundColor(this)
        val colorCard = KiviSettings.getCardColor(this)
        val colorTexto = KiviSettings.getPrimaryTextColor(this)
        val colorSecundario = KiviSettings.getSecondaryTextColor(this)
        val colorTema = KiviSettings.getThemeColor(this)
        val temaState = ColorStateList.valueOf(colorTema)

        root.setBackgroundColor(colorFondo)

        val cardMain = findViewById<MaterialCardView>(R.id.cardMain)
        cardMain.setCardBackgroundColor(colorCard)

        imgFoto.setBackgroundColor(colorCard)
        txtEstado.setTextColor(colorTexto)

        val tvHeader = findViewById<TextView>(R.id.tvHeader)
        tvHeader.setTextColor(colorSecundario)

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

        (btnEscuchar as MaterialButton).backgroundTintList = temaState
        btnEscuchar.setTextColor(0xFFFFFFFF.toInt())

        (btnCamara as MaterialButton).strokeColor = temaState
        btnCamara.setTextColor(colorTema)
        (btnCamara as MaterialButton).iconTint = temaState
    }

    // -----------------------------------------------------------------------------------------
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
        btnEscuchar.text = getString(R.string.btn_stop)
        btnEscuchar.setBackgroundColor(getColor(android.R.color.holo_red_light))

        orquestador.empezarEscucha { texto ->
            txtEstado.text = getString(R.string.you_said, texto)
            orquestador.procesarPregunta(texto, fotoActual)
            resetearBotonEscuchar()
        }
    }

    private fun resetearBotonEscuchar() {
        estaEscuchando = false
        btnEscuchar.text = getString(R.string.btn_talk)

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

    // -----------------------------------------------------------------------------------------
    // CONFIRMACIÓN DE CERRAR SESIÓN
    private fun mostrarDialogoCerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout_title))
            .setMessage(getString(R.string.logout_message))
            .setPositiveButton(getString(R.string.btn_yes)) { _: DialogInterface, _: Int ->

                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

                val googleClient = GoogleSignIn.getClient(this, gso)
                googleClient.signOut()

                Firebase.auth.signOut()

                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    // -----------------------------------------------------------------------------------------
    override fun onDestroy() {
        orquestador.liberarRecursos()
        super.onDestroy()
    }
}