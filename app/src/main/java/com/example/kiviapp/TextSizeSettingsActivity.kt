package com.example.kiviapp

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kiviapp.R
import com.google.android.material.card.MaterialCardView

class TextSizeSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_size_settings)

        val tvCerrar = findViewById<TextView>(R.id.tvCerrarTexto)
        val tvNivel = findViewById<TextView>(R.id.tvTextoNivel)
        val seek = findViewById<SeekBar>(R.id.seekTextSize)

        // Cargar valor guardado
        val nivelGuardado = KiviSettings.getTextSizeLevel(this)
        seek.progress = nivelGuardado
        tvNivel.text = textoNivel(nivelGuardado)

        tvCerrar.setOnClickListener { finish() }

        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                tvNivel.text = textoNivel(progress)
                KiviSettings.setTextSizeLevel(this@TextSizeSettingsActivity, progress)
                // Actualizar tamaños en vivo
                aplicarTamanos()
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        aplicarTema()
        aplicarTamanos()
    }

    override fun onResume() {
        super.onResume()
        aplicarTema()
        aplicarTamanos()
    }

    private fun textoNivel(nivel: Int): String = when (nivel) {
        0 -> "Pequeño"
        2 -> "Grande"
        else -> "Mediano"
    }

    private fun aplicarTema() {

        val root = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.rootTextSize)
        val card = findViewById<MaterialCardView>(R.id.cardTextSizeRoot)

        // Colores desde KiviSettings
        val colorFondo = KiviSettings.getBackgroundColor(this)
        val colorCard = KiviSettings.getCardColor(this)
        val colorTexto = KiviSettings.getPrimaryTextColor(this)
        val colorSecundario = KiviSettings.getSecondaryTextColor(this)
        val colorTema = KiviSettings.getThemeColor(this)
        val temaState = ColorStateList.valueOf(colorTema)

        // Fondo
        root.setBackgroundColor(colorFondo)
        card.setCardBackgroundColor(colorCard)

        // Títulos
        findViewById<TextView>(R.id.tvTituloTexto).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvCerrarTexto).setTextColor(colorTexto)

        // Subtítulo y nivel actual
        findViewById<TextView>(R.id.tvSubtituloTamano).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvTextoNivel).setTextColor(colorSecundario)

        // Color del seekbar
        val seek = findViewById<SeekBar>(R.id.seekTextSize)
        seek.progressTintList = temaState
        seek.thumbTintList = temaState
    }

    private fun aplicarTamanos() {
        fun size(base: Float): Float = KiviSettings.getScaledTextSize(this, base)

        // Header
        findViewById<TextView>(R.id.tvTituloTexto).textSize = size(20f)
        findViewById<TextView>(R.id.tvCerrarTexto).textSize = size(18f)

        // Subtítulo y texto de nivel
        findViewById<TextView>(R.id.tvSubtituloTamano).textSize = size(16f)
        findViewById<TextView>(R.id.tvTextoNivel).textSize = size(16f)
    }
}
