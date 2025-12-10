package com.example.kiviapp

import android.content.res.ColorStateList
import android.media.AudioManager
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kiviapp.R
import com.google.android.material.card.MaterialCardView

class SoundSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sound_settings)

        val tvCerrar = findViewById<TextView>(R.id.tvCerrarSonido)
        val seekGeneral = findViewById<SeekBar>(R.id.seekVolGeneral)
        val seekVoz = findViewById<SeekBar>(R.id.seekVolVoz)
        val tvGeneral = findViewById<TextView>(R.id.tvVolGeneral)
        val tvVoz = findViewById<TextView>(R.id.tvVolVoz)

        tvCerrar.setOnClickListener { finish() }

        // Cargar valores guardados
        val volGeneral = KiviSettings.getVolGeneral(this)
        val volVoz = KiviSettings.getVolVoz(this)

        seekGeneral.progress = volGeneral
        seekVoz.progress = volVoz
        tvGeneral.text = "$volGeneral%"
        tvVoz.text = "$volVoz%"

        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        // Volumen general
        seekGeneral.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                tvGeneral.text = "$progress%"
                KiviSettings.setVolGeneral(this@SoundSettingsActivity, progress)

                val nuevoVol = (progress / 100f * maxVol).toInt()
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nuevoVol, 0)
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Volumen de voz (TTS)
        seekVoz.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                tvVoz.text = "$progress%"
                KiviSettings.setVolVoz(this@SoundSettingsActivity, progress)
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

    private fun aplicarTema() {
        val root = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.rootSound)
        val card = findViewById<MaterialCardView>(R.id.cardSoundRoot)

        // Colores dinámicos desde KiviSettings
        val colorFondo = KiviSettings.getBackgroundColor(this)
        val colorCard = KiviSettings.getCardColor(this)
        val colorTexto = KiviSettings.getPrimaryTextColor(this)
        val colorSecundario = KiviSettings.getSecondaryTextColor(this)
        val colorTema = KiviSettings.getThemeColor(this)
        val temaState = ColorStateList.valueOf(colorTema)

        // Fondo global
        root.setBackgroundColor(colorFondo)

        // Card
        card.setCardBackgroundColor(colorCard)

        // Textos principales
        findViewById<TextView>(R.id.tvTituloSonido).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvCerrarSonido).setTextColor(colorTexto)

        // Textos de secciones
        findViewById<TextView>(R.id.tvTituloVolGeneral).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvTituloVolVoz).setTextColor(colorTexto)

        // Porcentajes
        findViewById<TextView>(R.id.tvVolGeneral).setTextColor(colorSecundario)
        findViewById<TextView>(R.id.tvVolVoz).setTextColor(colorSecundario)

        // Color tema en sliders
        val seekGeneral = findViewById<SeekBar>(R.id.seekVolGeneral)
        val seekVoz = findViewById<SeekBar>(R.id.seekVolVoz)

        seekGeneral.progressTintList = temaState
        seekGeneral.thumbTintList = temaState

        seekVoz.progressTintList = temaState
        seekVoz.thumbTintList = temaState
    }

    private fun aplicarTamanos() {
        fun size(base: Float): Float = KiviSettings.getScaledTextSize(this, base)

        // Header
        findViewById<TextView>(R.id.tvTituloSonido).textSize = size(20f)
        findViewById<TextView>(R.id.tvCerrarSonido).textSize = size(18f)

        // Etiquetas de cada sección
        findViewById<TextView>(R.id.tvTituloVolGeneral).textSize = size(16f)
        findViewById<TextView>(R.id.tvTituloVolVoz).textSize = size(16f)

        // Porcentajes
        findViewById<TextView>(R.id.tvVolGeneral).textSize = size(14f)
        findViewById<TextView>(R.id.tvVolVoz).textSize = size(14f)
    }
}
