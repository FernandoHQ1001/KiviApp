package com.example.kiviapp.features.ui.activities.settings

import android.content.Intent
import android.content.res.ColorStateList
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import com.example.kiviapp.R
import com.example.kiviapp.features.ui.activities.WelcomeActivity
import com.example.kiviapp.features.ui.activities.base.BaseActivity
import com.google.android.material.card.MaterialCardView

class LanguageSettingsActivity : BaseActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_settings)

        sharedPreferences = getSharedPreferences("KiviAppPrefs", MODE_PRIVATE)

        val btnBack = findViewById<ImageButton>(R.id.btnBackLanguage)
        btnBack.setOnClickListener { finish() }

        // Idioma de la interfaz
        val cardIdiomaInterfaz = findViewById<MaterialCardView>(R.id.cardIdiomaInterfaz)
        cardIdiomaInterfaz.setOnClickListener {
            showLanguageSelectionDialog()
        }

        // 🔊 Idioma de voz → abre el selector
        val cardIdiomaVoz = findViewById<MaterialCardView>(R.id.cardIdiomaVoz)
        cardIdiomaVoz.setOnClickListener {
            startActivity(Intent(this, VoiceLanguageActivity::class.java))
        }

        aplicarTema()
        aplicarTamanos()
        updateCurrentLanguageDisplay()
    }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf(
            getString(R.string.language_spanish),
            getString(R.string.language_english),
            getString(R.string.language_portuguese)
        )
        val languageCodes = arrayOf("es", "en", "pt")

        val currentLanguage = sharedPreferences.getString("app_language", "es")
        val currentIndex = languageCodes.indexOf(currentLanguage)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.interface_language))
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                val selectedLanguage = languageCodes[which]
                sharedPreferences.edit().putString("app_language", selectedLanguage).apply()
                dialog.dismiss()

                // Reiniciar la aplicación para aplicar cambios
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun updateCurrentLanguageDisplay() {
        val currentLanguage = sharedPreferences.getString("app_language", "es")
        val tvIdiomaInterfazDescripcion = findViewById<TextView>(R.id.tvIdiomaInterfazDescripcion)

        val languageName = when (currentLanguage) {
            "es" -> getString(R.string.language_spanish)
            "en" -> getString(R.string.language_english)
            "pt" -> getString(R.string.language_portuguese)
            else -> getString(R.string.language_spanish)
        }

        tvIdiomaInterfazDescripcion.text = getString(R.string.interface_language_desc, languageName)
    }

    override fun onResume() {
        super.onResume()
        aplicarTema()
        aplicarTamanos()
        updateCurrentLanguageDisplay()
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