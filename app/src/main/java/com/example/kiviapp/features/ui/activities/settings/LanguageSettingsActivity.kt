package com.example.kiviapp.features.ui.activities.settings

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kiviapp.features.ui.activities.settings.KiviSettings
import com.example.kiviapp.R
import com.example.kiviapp.features.ui.activities.WelcomeActivity
import com.example.kiviapp.features.ui.activities.base.BaseActivity

class LanguageSettingsActivity : BaseActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var rv: RecyclerView
    private lateinit var adapter: SettingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_settings)

        sharedPreferences = getSharedPreferences("KiviAppPrefs", MODE_PRIVATE)

        findViewById<ImageButton>(R.id.btnBackLanguage).setOnClickListener { finish() }

        rv = findViewById(R.id.rvLanguageSettings)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = SettingsAdapter(this, buildRows())
        rv.adapter = adapter

        aplicarTemaPantalla()
        aplicarTamanosPantalla()
    }

    override fun onResume() {
        super.onResume()
        // Re-crear rows para que el subtítulo muestre el idioma actual
        adapter = SettingsAdapter(this, buildRows())
        rv.adapter = adapter

        aplicarTemaPantalla()
        aplicarTamanosPantalla()
    }

    private fun buildRows(): List<SettingsRow> {
        val currentLanguage = sharedPreferences.getString("app_language", "es") ?: "es"
        val languageName = when (currentLanguage) {
            "es" -> getString(R.string.language_spanish)
            "en" -> getString(R.string.language_english)
            "pt" -> getString(R.string.language_portuguese)
            else -> getString(R.string.language_spanish)
        }

        // IMPORTANTE: tu string interface_language_desc la estabas usando con formato.
        // Si NO la tienes como string con %1$s, usa directamente un subtítulo fijo.
        val interfazSubtitle = try {
            getString(R.string.interface_language_desc, languageName)
        } catch (_: Exception) {
            // fallback por si ese string no es formateable
            "$languageName"
        }

        return listOf(
            SettingsRow.Header(getString(R.string.language)),

            SettingsRow.Item(
                id = "interface_language",
                iconRes = android.R.drawable.ic_menu_sort_by_size,
                title = getString(R.string.interface_language),
                subtitle = interfazSubtitle,
                onClick = { showLanguageSelectionDialog() }
            ),

            SettingsRow.Item(
                id = "voice_language",
                iconRes = android.R.drawable.ic_btn_speak_now,
                title = getString(R.string.voice_language),
                subtitle = getString(R.string.voice_language_desc),
                onClick = { startActivity(Intent(this, VoiceLanguageActivity::class.java)) }
            )
        )
    }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf(
            getString(R.string.language_spanish),
            getString(R.string.language_english),
            getString(R.string.language_portuguese)
        )
        val languageCodes = arrayOf("es", "en", "pt")

        val currentLanguage = sharedPreferences.getString("app_language", "es")
        val currentIndex = languageCodes.indexOf(currentLanguage).let { if (it < 0) 0 else it }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.interface_language))
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                val selectedLanguage = languageCodes[which]
                sharedPreferences.edit().putString("app_language", selectedLanguage).apply()
                dialog.dismiss()

                // Reiniciar app para aplicar cambios (como ya lo estabas haciendo)
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun aplicarTemaPantalla() {
        val root = findViewById<ConstraintLayout>(R.id.rootLanguageSettings)

        val colorFondo = KiviSettings.getBackgroundColor(this)
        val colorTexto = KiviSettings.getPrimaryTextColor(this)
        val colorSec = KiviSettings.getSecondaryTextColor(this)
        val iconState = ColorStateList.valueOf(KiviSettings.getIconColor(this))

        root.setBackgroundColor(colorFondo)

        findViewById<TextView>(R.id.tvTituloLanguageConfig).setTextColor(colorTexto)
        findViewById<ImageButton>(R.id.btnBackLanguage).imageTintList = iconState
        findViewById<View>(R.id.dividerLanguage).setBackgroundColor(colorSec)
    }

    private fun aplicarTamanosPantalla() {
        fun size(base: Float) = KiviSettings.getScaledTextSize(this, base)
        findViewById<TextView>(R.id.tvTituloLanguageConfig).textSize = size(22f)
    }
}
