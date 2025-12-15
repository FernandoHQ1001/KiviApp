package com.example.kiviapp.features.ui.activities.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kiviapp.R
import com.example.kiviapp.features.ui.activities.MainActivity
import com.example.kiviapp.features.ui.activities.base.BaseActivity

/*
 * Pantalla que permite configurar:
 * - Idioma de la interfaz
 * - Idioma de la voz
 *
 * Ambos se manejan por separado para mejorar accesibilidad.
 */

class LanguageSettingsActivity : BaseActivity() {

    // Lista visual de opciones
    private lateinit var rv: RecyclerView

    // Adaptador reutilizable para settings
    private lateinit var adapter: SettingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_settings)

        // Inicializa el RecyclerView
        rv = findViewById(R.id.rvLanguageSettings)
        rv.layoutManager = LinearLayoutManager(this)

        // Crea y asigna el adaptador
        adapter = SettingsAdapter(this, buildRows())
        rv.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        // refrescar subtítulos con idioma actual
        adapter = SettingsAdapter(this, buildRows())
        rv.adapter = adapter
    }

    /*
     * Construye dinámicamente las filas de configuración
     */
    private fun buildRows(): List<SettingsRow> {
        // Idiomas actuales guardados
        val appLang = KiviSettings.getAppLanguage(this)
        val voiceLang = KiviSettings.getVoiceLanguage(this)

        // Traduce el código de idioma a texto visible
        fun name(code: String): String = when (code) {
            "es" -> getString(R.string.language_spanish)
            "en" -> getString(R.string.language_english)
            "pt" -> getString(R.string.language_portuguese)
            else -> getString(R.string.language_spanish)
        }

        return listOf(
            // Título de la sección
            SettingsRow.Header(getString(R.string.language)),

            // ✅ Idioma Interfaz
            SettingsRow.Item(
                id = "interface_language",
                iconRes = android.R.drawable.ic_menu_sort_by_size,
                title = getString(R.string.interface_language),
                subtitle = name(appLang),
                onClick = { showInterfaceLanguageDialog() }
            ),

            // ✅ Idioma Voz
            SettingsRow.Item(
                id = "voice_language",
                iconRes = android.R.drawable.ic_btn_speak_now,
                title = getString(R.string.voice_language),
                subtitle = name(voiceLang),
                onClick = { showVoiceLanguageDialog() }
            )
        )
    }

    /*
     * Muestra diálogo para cambiar el idioma de la interfaz
     * Requiere reiniciar la app para aplicar el Locale
     */
    private fun showInterfaceLanguageDialog() {
        val languageCodes = arrayOf("es", "en", "pt")
        val languageNames = arrayOf(
            getString(R.string.language_spanish),
            getString(R.string.language_english),
            getString(R.string.language_portuguese)
        )

        // Idioma actual
        val current = KiviSettings.getAppLanguage(this)
        val currentIndex = languageCodes.indexOf(current).let { if (it < 0) 0 else it }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.interface_language))
            .setSingleChoiceItems(languageNames, currentIndex) { dialog, which ->
                val selected = languageCodes[which]
                dialog.dismiss()

                // ✅ guarda local + global + firebase
                KiviSettings.setAppLanguage(this, selected)

                // ✅ Reinicia task para aplicar Locale en toda la app
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    /*
     * Muestra diálogo para cambiar el idioma de la voz
     * No requiere reiniciar la app
     */
    private fun showVoiceLanguageDialog() {
        val languageCodes = arrayOf("es", "en", "pt")
        val languageNames = arrayOf(
            getString(R.string.language_spanish),
            getString(R.string.language_english),
            getString(R.string.language_portuguese)
        )

        // Idioma de voz actual
        val current = KiviSettings.getVoiceLanguage(this)
        val currentIndex = languageCodes.indexOf(current).let { if (it < 0) 0 else it }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.voice_language))
            .setSingleChoiceItems(languageNames, currentIndex) { dialog, which ->
                val selected = languageCodes[which]
                dialog.dismiss()

                // ✅ solo cambia voz (no requiere reinicio completo)
                KiviSettings.setVoiceLanguage(this, selected)

                // refrescar lista para que cambie subtítulo
                adapter = SettingsAdapter(this, buildRows())
                rv.adapter = adapter
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
