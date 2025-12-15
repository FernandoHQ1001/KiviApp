package com.example.kiviapp.features.ui.activities.settings

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kiviapp.features.ui.activities.settings.KiviSettings
import com.example.kiviapp.R
import com.example.kiviapp.features.ui.activities.TutorialActivity
import com.example.kiviapp.features.ui.activities.base.BaseActivity

/*
 * Pantalla principal de configuración de Kivi.
 * Desde aquí se accede a todas las opciones
 * de personalización y accesibilidad.
 */
class SettingsActivity : BaseActivity() {

    // Adaptador del menú de configuraciones
    private lateinit var adapter: SettingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Back
        findViewById<ImageButton>(R.id.btnBackSettings).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Lista de filas (headers + items)
        val rows = buildRows()

        // RecyclerView
        val rv = findViewById<RecyclerView>(R.id.rvSettings)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = SettingsAdapter(this, rows)
        rv.adapter = adapter

        // Aplica tema y tamaños accesibles
        aplicarTema()
        aplicarTamanos()
    }

    override fun onResume() {
        super.onResume()
        aplicarTema()
        aplicarTamanos()
        adapter.refreshTheme()
    }

    /*
     * Construye las secciones y opciones del menú
     */
    private fun buildRows(): List<SettingsRow> {
        return listOf(
            // -----------------------------
            // Multimedia
            // -----------------------------
            SettingsRow.Header(getString(R.string.settings_multimedia)),
            SettingsRow.Item(
                id = "sound",
                iconRes = android.R.drawable.ic_lock_silent_mode_off,
                title = getString(R.string.settings_sound_volume),
                subtitle = getString(R.string.settings_sound_description),
                onClick = { startActivity(Intent(this, SoundSettingsActivity::class.java)) }
            ),

            // -----------------------------
            // Accesibilidad
            // -----------------------------
            SettingsRow.Header(getString(R.string.settings_accessibility)),
            SettingsRow.Item(
                id = "text_size",
                iconRes = android.R.drawable.ic_menu_sort_by_size,
                title = getString(R.string.settings_text_size),
                subtitle = getString(R.string.settings_text_description),
                onClick = { startActivity(Intent(this, TextSizeSettingsActivity::class.java)) }
            ),

            // -----------------------------
            // Personalización
            // -----------------------------
            SettingsRow.Header(getString(R.string.settings_personalization)),
            SettingsRow.Item(
                id = "appearance",
                iconRes = android.R.drawable.ic_menu_gallery,
                title = getString(R.string.settings_appearance),
                subtitle = getString(R.string.settings_appearance_description),
                onClick = { startActivity(Intent(this, AppearanceSettingsActivity::class.java)) }
            ),

            // -----------------------------
            // Ayuda
            // -----------------------------
            SettingsRow.Header(getString(R.string.settings_help)),
            SettingsRow.Item(
                id = "tutorial",
                iconRes = android.R.drawable.ic_menu_help,
                title = getString(R.string.settings_tutorial),
                subtitle = getString(R.string.settings_tutorial_description),
                onClick = { startActivity(Intent(this, TutorialActivity::class.java)) }
            )
        )
    }

    /*
     * Aplica colores del tema a la pantalla
     */
    private fun aplicarTema() {
        val root = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.rootSettings)

        val colorFondo = KiviSettings.getBackgroundColor(this)
        val colorTexto = KiviSettings.getPrimaryTextColor(this)
        val iconColor = ColorStateList.valueOf(KiviSettings.getIconColor(this))

        // Fondo
        root.setBackgroundColor(colorFondo)

        // Icono volver y título
        findViewById<ImageButton>(R.id.btnBackSettings).imageTintList = iconColor
        findViewById<TextView>(R.id.tvTituloConfig).setTextColor(colorTexto)
    }

    /*
     * Aplica tamaños de texto accesibles
     */
    private fun aplicarTamanos() {
        fun size(base: Float): Float = KiviSettings.getScaledTextSize(this, base)
        findViewById<TextView>(R.id.tvTituloConfig).textSize = size(22f)
    }
}
