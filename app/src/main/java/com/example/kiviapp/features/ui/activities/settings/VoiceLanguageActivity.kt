package com.example.kiviapp.features.ui.activities.settings

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.kiviapp.R
import com.example.kiviapp.features.ui.activities.base.BaseActivity
import com.google.android.material.card.MaterialCardView

class VoiceLanguageActivity : BaseActivity() {

    private lateinit var tvIdiomaActual: TextView

    // Idiomas de voz disponibles
    private val idiomas = listOf(
        "Español" to "es",
        "English" to "en",
        "Português" to "pt"
        // añade más si quieres: "Français" to "fr", etc.
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_language)

        val tvCerrar = findViewById<TextView>(R.id.tvCerrarVoiceLanguage)
        tvCerrar.setOnClickListener { finish() }

        tvIdiomaActual = findViewById(R.id.tvIdiomaActualVoz)

        // Mostrar idioma actual guardado
        val langCode = KiviSettings.getVoiceLanguage(this)
        val nombreIdioma = when (langCode) {
            "es" -> getString(R.string.language_spanish)
            "en" -> getString(R.string.language_english)
            "pt" -> getString(R.string.language_portuguese)
            else -> getString(R.string.language_spanish)
        }

        tvIdiomaActual.text = nombreIdioma

        val cardSelector =
            findViewById<MaterialCardView>(R.id.cardSelectorIdiomaVoz)

        cardSelector.setOnClickListener {
            mostrarDialogoIdiomas()
        }

        aplicarTema()
        aplicarTamanos()
    }

    override fun onResume() {
        super.onResume()
        aplicarTema()
        aplicarTamanos()
    }

    private fun mostrarDialogoIdiomas() {
        val nombres = arrayOf(
            getString(R.string.language_spanish),
            getString(R.string.language_english),
            getString(R.string.language_portuguese)
        )
        val codes = arrayOf("es", "en", "pt")

        val langCodeActual = KiviSettings.getVoiceLanguage(this)
        var checkedItem = codes.indexOf(langCodeActual)
        if (checkedItem < 0) checkedItem = 0

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.voice_language))
            .setSingleChoiceItems(nombres, checkedItem) { dialog, which ->
                val code = codes[which]
                KiviSettings.setVoiceLanguage(this, code)
                tvIdiomaActual.text = nombres[which]
                Toast.makeText(
                    this,
                    getString(R.string.voice_language_changed_to, nombres[which]),
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }


    // --------------------------------------------------------------
    // TEMA / COLORES
    // --------------------------------------------------------------
    private fun aplicarTema() {
        val root = findViewById<ConstraintLayout>(R.id.rootVoiceLanguage)
        val cardRoot = findViewById<MaterialCardView>(R.id.cardVoiceLanguageRoot)
        val cardSelector = findViewById<MaterialCardView>(R.id.cardSelectorIdiomaVoz)

        val colorFondo = KiviSettings.getBackgroundColor(this)
        val colorCard = KiviSettings.getCardColor(this)
        val colorTexto = KiviSettings.getPrimaryTextColor(this)
        val colorSecundario = KiviSettings.getSecondaryTextColor(this)
        val colorTema = KiviSettings.getThemeColor(this)

        root.setBackgroundColor(colorFondo)
        cardRoot.setCardBackgroundColor(colorCard)

        // Header
        val tvTitulo = findViewById<TextView>(R.id.tvTituloVoiceLanguage)
        val tvCerrar = findViewById<TextView>(R.id.tvCerrarVoiceLanguage)
        tvTitulo.setTextColor(colorTexto)
        tvCerrar.setTextColor(colorTexto)

        // Descripción
        val tvDescripcion = findViewById<TextView>(R.id.tvDescripcionVoiceLanguage)
        tvDescripcion.setTextColor(colorSecundario)
        tvDescripcion.text = getString(R.string.select_voice_language_description)

        // Selector de idioma
        cardSelector.setCardBackgroundColor(KiviSettings.getBackgroundColor(this))
        cardSelector.strokeColor = colorTema

        tvIdiomaActual.setTextColor(colorTexto)

        val iconDrop = findViewById<ImageView>(R.id.iconDropIdiomaVoz)
        iconDrop.imageTintList =
            ColorStateList.valueOf(colorTema)
    }

    // --------------------------------------------------------------
    // TAMAÑO DE TEXTO
    // --------------------------------------------------------------
    private fun aplicarTamanos() {
        fun size(base: Float): Float = KiviSettings.getScaledTextSize(this, base)

        findViewById<TextView>(R.id.tvTituloVoiceLanguage).textSize = size(20f)
        findViewById<TextView>(R.id.tvCerrarVoiceLanguage).textSize = size(18f)
        findViewById<TextView>(R.id.tvDescripcionVoiceLanguage).textSize = size(14f)
        findViewById<TextView>(R.id.tvIdiomaActualVoz).textSize = size(16f)
    }
}
