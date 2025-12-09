package com.example.kiviapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kiviapp.core.KiviOrchestrator // Importamos el nuevo jefe

class MainActivity : AppCompatActivity(), KiviOrchestrator.KiviListener {

    // UI
    private lateinit var txtEstado: TextView
    private lateinit var btnEscuchar: Button
    private lateinit var btnCamara: Button
    private lateinit var imgFoto: ImageView

    // El Coordinador
    private lateinit var orquestador: KiviOrchestrator

    // Estado
    private var estaEscuchando = false
    private var fotoActual: Bitmap? = null

    // Permisos
    private val PERMISO_MICROFONO = 100
    private val PERMISO_CAMARA = 101

    private val tomarFotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imagen = result.data?.extras?.get("data") as? Bitmap
            if (imagen != null) {
                fotoActual = imagen
                imgFoto.visibility = android.view.View.VISIBLE
                imgFoto.setImageBitmap(imagen)

                // Mensaje visual
                txtEstado.text = "Foto lista. Pregúntame."

                // Le pedimos al orquestador que lo diga en voz alta
                orquestador.decir("Foto lista. Pregúntame.")
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. UI
        txtEstado = findViewById(R.id.txtEstado)
        btnEscuchar = findViewById(R.id.btnEscuchar)
        btnCamara = findViewById(R.id.btnCamara)
        imgFoto = findViewById(R.id.imgFoto)

        // 2. Iniciar Orquestador
        orquestador = KiviOrchestrator(this)
        orquestador.setListener(this) // Conectamos la pantalla al orquestador

        // 3. Botones
        btnEscuchar.setOnClickListener {
            if (estaEscuchando) {
                orquestador.detenerEscucha()
                resetearBotonEscuchar()
            } else {
                verificarPermisoMicrofono()
            }
        }

        btnCamara.setOnClickListener { verificarPermisoCamara() }

        // 4. Saludo
        orquestador.saludar()
    }

    // --- MÉTODOS DEL LISTENER (Lo que el Orquestador nos dice) ---
    override fun onEstadoCambiado(texto: String) {
        txtEstado.text = texto
    }

    override fun onKiviHablando(texto: String) {
        // Aquí podrías animar algo si quisieras
    }

    override fun onError(mensaje: String) {
        txtEstado.text = "Error: $mensaje"
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        resetearBotonEscuchar()
    }

    // --- LÓGICA DE UI ---

    private fun verificarPermisoMicrofono() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISO_MICROFONO)
        } else {
            activarModoEscucha()
        }
    }

    private fun activarModoEscucha() {
        estaEscuchando = true
        btnEscuchar.text = "🛑 DETENER"
        btnEscuchar.setBackgroundColor(getColor(android.R.color.holo_red_light))

        // Aquí pedimos escuchar
        orquestador.empezarEscucha { textoReconocido ->
            txtEstado.text = "Tú: $textoReconocido"

            // Enviamos el TEXTO + la FOTO ACTUAL (si existe)
            orquestador.procesarPregunta(textoReconocido, fotoActual)

            resetearBotonEscuchar()
        }
    }

    private fun resetearBotonEscuchar() {
        estaEscuchando = false
        btnEscuchar.text = "HABLAR CON KIVI 🎤"
        btnEscuchar.setBackgroundColor(getColor(com.google.android.material.R.color.design_default_color_primary))
    }

    private fun verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISO_CAMARA)
        } else {
            abrirCamara()
        }
    }

    private fun abrirCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try { tomarFotoLauncher.launch(intent) } catch (e: Exception) {}
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PERMISO_MICROFONO) activarModoEscucha()
            if (requestCode == PERMISO_CAMARA) abrirCamara()
        }
    }

    override fun onDestroy() {
        orquestador.liberarRecursos()
        super.onDestroy()
    }
}
