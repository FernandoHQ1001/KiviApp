package com.example.kiviapp.features.ui.activities.settings

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kiviapp.R   // <-- import explícito de R
import com.google.android.material.card.MaterialCardView

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Botón atrás
        findViewById<ImageButton>(R.id.btnBackSettings).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Navegación a cada pantalla
        findViewById<MaterialCardView>(R.id.cardSonidoVolumen).setOnClickListener {
            startActivity(Intent(this, SoundSettingsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardTamanoTexto).setOnClickListener {
            startActivity(Intent(this, TextSizeSettingsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardApariencia).setOnClickListener {
            startActivity(Intent(this, AppearanceSettingsActivity::class.java))
        }

        aplicarTema()
        aplicarTamanos()
    }

    override fun onResume() {
        super.onResume()
        aplicarTema()
        aplicarTamanos()
    }

    private fun aplicarTema() {

        val root = findViewById<ScrollView>(R.id.rootSettings)

        val colorFondo = KiviSettings.getBackgroundColor(this)
        val colorTexto = KiviSettings.getPrimaryTextColor(this)
        val colorSecundario = KiviSettings.getSecondaryTextColor(this)
        val colorTema = KiviSettings.getThemeColor(this)
        val colorCard = KiviSettings.getCardColor(this)

        val temaState = ColorStateList.valueOf(colorTema)
        val iconColor = ColorStateList.valueOf(KiviSettings.getIconColor(this))

        // Fondo general
        root.setBackgroundColor(colorFondo)

        // Botón atrás
        findViewById<ImageButton>(R.id.btnBackSettings).imageTintList = iconColor

        // Títulos de sección
        findViewById<TextView>(R.id.tvTituloConfig).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvMultimedia).setTextColor(colorSecundario)
        findViewById<TextView>(R.id.tvAccesibilidad).setTextColor(colorSecundario)
        findViewById<TextView>(R.id.tvPersonalizacion).setTextColor(colorSecundario)

        // Cards
        val cards = listOf(
            findViewById<MaterialCardView>(R.id.cardSonidoVolumen),
            findViewById<MaterialCardView>(R.id.cardTamanoTexto),
            findViewById<MaterialCardView>(R.id.cardApariencia)
        )

        cards.forEach {
            it.setCardBackgroundColor(colorCard)
            it.strokeColor = colorTema
        }

        // Íconos con color de tema
        findViewById<ImageView>(R.id.iconSonido).imageTintList = temaState
        findViewById<ImageView>(R.id.iconSonidoNext).imageTintList = temaState

        findViewById<TextView>(R.id.iconTexto).setTextColor(colorTema)
        findViewById<ImageView>(R.id.iconTextoNext).imageTintList = temaState

        findViewById<ImageView>(R.id.iconApariencia).imageTintList = temaState
        findViewById<ImageView>(R.id.iconAparienciaNext).imageTintList = temaState

        // Textos dentro de las cards
        findViewById<TextView>(R.id.tvSonidoTitulo).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvSonidoDescripcion).setTextColor(colorSecundario)

        findViewById<TextView>(R.id.tvTextoTitulo).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvTextoDescripcion).setTextColor(colorSecundario)

        findViewById<TextView>(R.id.tvAparienciaTitulo).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvAparienciaDescripcion).setTextColor(colorSecundario)
    }

    private fun aplicarTamanos() {

        fun size(base: Float): Float = KiviSettings.getScaledTextSize(this, base)

        // TÍTULOS DE SECCIÓN
        findViewById<TextView>(R.id.tvTituloConfig).textSize = size(22f)
        findViewById<TextView>(R.id.tvMultimedia).textSize = size(14f)
        findViewById<TextView>(R.id.tvAccesibilidad).textSize = size(14f)
        findViewById<TextView>(R.id.tvPersonalizacion).textSize = size(14f)

        // CARDS — TÍTULOS Y DESCRIPCIONES
        findViewById<TextView>(R.id.tvSonidoTitulo).textSize = size(16f)
        findViewById<TextView>(R.id.tvSonidoDescripcion).textSize = size(12f)

        findViewById<TextView>(R.id.tvTextoTitulo).textSize = size(16f)
        findViewById<TextView>(R.id.tvTextoDescripcion).textSize = size(12f)

        findViewById<TextView>(R.id.tvAparienciaTitulo).textSize = size(16f)
        findViewById<TextView>(R.id.tvAparienciaDescripcion).textSize = size(12f)

        // ICONO "T" (tamaño de texto)
        findViewById<TextView>(R.id.iconTexto).textSize = size(22f)
    }
}
