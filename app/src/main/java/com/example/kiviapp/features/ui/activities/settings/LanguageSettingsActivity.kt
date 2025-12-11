package com.example.kiviapp.features.ui.activities.settings

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kiviapp.R
import com.google.android.material.card.MaterialCardView

class LanguageSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_settings)

        val btnBack = findViewById<ImageButton>(R.id.btnBackLanguage)
        btnBack.setOnClickListener { finish() }

        // Idioma de la interfaz (por ahora sin lógica, solo UI)
        val cardIdiomaInterfaz =
            findViewById<MaterialCardView>(R.id.cardIdiomaInterfaz)

        cardIdiomaInterfaz.setOnClickListener {
            // TODO: futuro - cambiar textos de la UI
        }

        // 🔊 Idioma de voz → abre el selector
        val cardIdiomaVoz =
            findViewById<MaterialCardView>(R.id.cardIdiomaVoz)

        cardIdiomaVoz.setOnClickListener {
            startActivity(Intent(this, VoiceLanguageActivity::class.java))
        }

        aplicarTema()
        aplicarTamanos()
    }

    override fun onResume() {
        super.onResume()
        aplicarTema()
        aplicarTamanos()
    }

    // --------------------------------------------------------------
    // TEMA / COLORES
    // --------------------------------------------------------------
    private fun aplicarTema() {
        val root = findViewById<ScrollView>(R.id.rootLanguageSettings)

        val colorFondo = KiviSettings.getBackgroundColor(this)
        val colorCard = KiviSettings.getCardColor(this)
        val colorTexto = KiviSettings.getPrimaryTextColor(this)
        val colorSecundario = KiviSettings.getSecondaryTextColor(this)
        val colorTema = KiviSettings.getThemeColor(this)
        val iconColor = KiviSettings.getIconColor(this)

        root.setBackgroundColor(colorFondo)

        // Barra superior
        val tvTitulo = findViewById<TextView>(R.id.tvTituloLanguageConfig)
        val btnBack = findViewById<ImageButton>(R.id.btnBackLanguage)

        tvTitulo.setTextColor(colorTexto)
        btnBack.imageTintList = ColorStateList.valueOf(iconColor)

        // Sección idioma
        val tvSeccionIdioma = findViewById<TextView>(R.id.tvSeccionIdioma)
        tvSeccionIdioma.setTextColor(colorSecundario)

        // CARD: Idioma de la interfaz
        val cardInterfaz = findViewById<MaterialCardView>(R.id.cardIdiomaInterfaz)
        cardInterfaz.setCardBackgroundColor(colorCard)
        cardInterfaz.strokeColor = colorTema

        val iconInterfaz = findViewById<ImageView>(R.id.iconIdiomaInterfaz)
        val iconInterfazNext = findViewById<ImageView>(R.id.iconIdiomaInterfazNext)
        iconInterfaz.imageTintList = ColorStateList.valueOf(colorTema)
        iconInterfazNext.imageTintList = ColorStateList.valueOf(colorTema)

        findViewById<TextView>(R.id.tvIdiomaInterfazTitulo).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvIdiomaInterfazDescripcion).setTextColor(colorSecundario)

        // CARD: Idioma de voz
        val cardVoz = findViewById<MaterialCardView>(R.id.cardIdiomaVoz)
        cardVoz.setCardBackgroundColor(colorCard)
        cardVoz.strokeColor = colorTema

        val iconVoz = findViewById<ImageView>(R.id.iconIdiomaVoz)
        val iconVozNext = findViewById<ImageView>(R.id.iconIdiomaVozNext)
        iconVoz.imageTintList = ColorStateList.valueOf(colorTema)
        iconVozNext.imageTintList = ColorStateList.valueOf(colorTema)

        findViewById<TextView>(R.id.tvIdiomaVozTitulo).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvIdiomaVozDescripcion).setTextColor(colorSecundario)
    }

    // --------------------------------------------------------------
    // TAMAÑO DE TEXTO
    // --------------------------------------------------------------
    private fun aplicarTamanos() {
        fun size(base: Float): Float = KiviSettings.getScaledTextSize(this, base)

        // Título barra superior
        findViewById<TextView>(R.id.tvTituloLanguageConfig).textSize = size(22f)

        // Sección
        findViewById<TextView>(R.id.tvSeccionIdioma).textSize = size(14f)

        // Card Idioma interfaz
        findViewById<TextView>(R.id.tvIdiomaInterfazTitulo).textSize = size(16f)
        findViewById<TextView>(R.id.tvIdiomaInterfazDescripcion).textSize = size(12f)

        // Card Idioma voz
        findViewById<TextView>(R.id.tvIdiomaVozTitulo).textSize = size(16f)
        findViewById<TextView>(R.id.tvIdiomaVozDescripcion).textSize = size(12f)
    }
}
