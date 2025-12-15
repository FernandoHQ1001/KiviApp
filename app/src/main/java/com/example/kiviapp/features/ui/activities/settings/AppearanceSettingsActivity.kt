package com.example.kiviapp.features.ui.activities.settings

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.kiviapp.R
import com.example.kiviapp.features.ui.activities.base.BaseActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial

/*
 * Pantalla de configuración visual de Kivi.
 * Permite personalizar colores, modo oscuro
 * y tamaños de texto para mejorar la accesibilidad.
 */

class AppearanceSettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appearance_settings)

        // Referencias a elementos de la interfaz
        val tvCerrar = findViewById<TextView>(R.id.tvCerrarApariencia)
        val switchDark = findViewById<SwitchMaterial>(R.id.switchDarkMode)
        val spinnerColor = findViewById<Spinner>(R.id.spinnerColor)

        // ----- Cerrar -----
        tvCerrar.setOnClickListener { finish() }

        // ----- Modo oscuro -----
        // Aplica el estado guardado
        switchDark.isChecked = KiviSettings.isDarkMode(this)

        // Escucha cambios del switch
        switchDark.setOnCheckedChangeListener { _, isChecked ->
            KiviSettings.setDarkMode(this, isChecked) // Guarda preferencia
            // Aplica el modo a toda la app
            val modo = if (isChecked)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO

            AppCompatDelegate.setDefaultNightMode(modo)
        }

        // ----- Colores -----
        val opciones = listOf(getString(R.string.color_blue), getString(R.string.color_purple), getString(R.string.color_green)) // Opciones visibles al usuario
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opciones
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerColor.adapter = adapter

        // seleccionar el que está guardado
        val actual = KiviSettings.getThemeColorName(this)
        val index = opciones.indexOf(actual).let { if (it == -1) 0 else it }
        spinnerColor.setSelection(index)

        spinnerColor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Traduce posición a nombre de color
                val seleccionado = when (position) {
                    1 -> "Morado"
                    2 -> "Verde"
                    else -> "Azul"
                }
                // Guarda el color
                KiviSettings.setThemeColorName(this@AppearanceSettingsActivity, seleccionado)
                // Reaplicar tema para que el color se vea al instante
                aplicarTema()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Aplica tema y tamaños al iniciar
        aplicarTema()
        aplicarTamanos()
    }

    override fun onResume() {
        super.onResume()
        aplicarTema()
        aplicarTamanos()
    }

    /*
     * Aplica colores de fondo, tarjetas, texto
     * y componentes según la configuración del usuario
     */

    private fun aplicarTema() {
        val root = findViewById<ConstraintLayout>(R.id.rootAppearance)
        val card = findViewById<MaterialCardView>(R.id.cardAppearanceRoot)

        // Colores desde KiviSettings
        val colorFondo = KiviSettings.getBackgroundColor(this)
        val colorCard = KiviSettings.getCardColor(this)
        val colorTexto = KiviSettings.getPrimaryTextColor(this)
        val colorSecundario = KiviSettings.getSecondaryTextColor(this)
        val colorTema = KiviSettings.getThemeColor(this)
        val temaState = ColorStateList.valueOf(colorTema)

        // Aplica colores
        root.setBackgroundColor(colorFondo)
        card.setCardBackgroundColor(colorCard)

        // Textos principales
        findViewById<TextView>(R.id.tvTituloAppearance).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvCerrarApariencia).setTextColor(colorTexto)

        // Etiquetas
        findViewById<TextView>(R.id.tvModoOscuroLabel).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvTemaColorLabel).setTextColor(colorTexto)

        // Switch de modo oscuro con color de tema
        val switchDark = findViewById<SwitchMaterial>(R.id.switchDarkMode)
        switchDark.thumbTintList = temaState
        switchDark.trackTintList = temaState

        // Spinner con borde/fondo del color de tema
        val spinnerColor = findViewById<Spinner>(R.id.spinnerColor)
        spinnerColor.backgroundTintList = temaState
    }

    /*
     * Aplica tamaños de texto escalados
     * según la configuración de accesibilidad
     */
    private fun aplicarTamanos() {
        fun size(base: Float): Float = KiviSettings.getScaledTextSize(this, base)

        // Header
        findViewById<TextView>(R.id.tvTituloAppearance).textSize = size(20f)
        findViewById<TextView>(R.id.tvCerrarApariencia).textSize = size(18f)

        // Etiquetas
        findViewById<TextView>(R.id.tvModoOscuroLabel).textSize = size(16f)
        findViewById<TextView>(R.id.tvTemaColorLabel).textSize = size(14f)
    }
}
