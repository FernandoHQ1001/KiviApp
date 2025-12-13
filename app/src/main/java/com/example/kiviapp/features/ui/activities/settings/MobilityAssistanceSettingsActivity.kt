package com.example.kiviapp.features.ui.activities.settings

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.kiviapp.R
import com.example.kiviapp.features.ui.activities.base.BaseActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial

class MobilityAssistanceSettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobility_assistance_settings)

        // Cerrar
        findViewById<TextView>(R.id.tvCerrarMobility).setOnClickListener { finish() }

        val switchVoz = findViewById<SwitchMaterial>(R.id.switchVozKivi)
        val switchHaptico = findViewById<SwitchMaterial>(R.id.switchHaptico)

        // Cargar valores guardados
        switchVoz.isChecked = KiviSettings.isVoiceEnabled(this)
        switchHaptico.isChecked = KiviSettings.isHapticEnabled(this)

        // Guardar cambios
        switchVoz.setOnCheckedChangeListener { _, isChecked ->
            KiviSettings.setVoiceEnabled(this, isChecked)
        }

        switchHaptico.setOnCheckedChangeListener { _, isChecked ->
            KiviSettings.setHapticEnabled(this, isChecked)
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
        val root = findViewById<ConstraintLayout>(R.id.rootMobilitySettings)
        val cardRoot = findViewById<MaterialCardView>(R.id.cardMobilitySettingsRoot)
        val cardOptions = findViewById<MaterialCardView>(R.id.cardMobilityOptions)

        val colorFondo = KiviSettings.getBackgroundColor(this)
        val colorCard = KiviSettings.getCardColor(this)
        val colorTexto = KiviSettings.getPrimaryTextColor(this)
        val colorSecundario = KiviSettings.getSecondaryTextColor(this)
        val colorTema = KiviSettings.getThemeColor(this)
        val temaState = ColorStateList.valueOf(colorTema)

        root.setBackgroundColor(colorFondo)
        cardRoot.setCardBackgroundColor(colorCard)
        cardOptions.setCardBackgroundColor(colorFondo)

        // Títulos
        findViewById<TextView>(R.id.tvTituloMobility).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvCerrarMobility).setTextColor(colorTexto)

        // Divider
        val divider = findViewById<View>(R.id.viewMobilityDivider)
        divider.setBackgroundColor(colorSecundario)

        // Textos de opciones
        findViewById<TextView>(R.id.tvLabelVoz).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvSubLabelVoz).setTextColor(colorSecundario)
        findViewById<TextView>(R.id.tvLabelHaptico).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvSubLabelHaptico).setTextColor(colorSecundario)

        // Switches
        val switchVoz = findViewById<SwitchMaterial>(R.id.switchVozKivi)
        val switchHaptico = findViewById<SwitchMaterial>(R.id.switchHaptico)

        switchVoz.thumbTintList = temaState
        switchVoz.trackTintList = temaState

        switchHaptico.thumbTintList = temaState
        switchHaptico.trackTintList = temaState
    }

    private fun aplicarTamanos() {
        fun size(base: Float) = KiviSettings.getScaledTextSize(this, base)

        findViewById<TextView>(R.id.tvTituloMobility).textSize = size(20f)

        findViewById<TextView>(R.id.tvLabelVoz).textSize = size(16f)
        findViewById<TextView>(R.id.tvSubLabelVoz).textSize = size(12f)
        findViewById<TextView>(R.id.tvLabelHaptico).textSize = size(16f)
        findViewById<TextView>(R.id.tvSubLabelHaptico).textSize = size(12f)
    }
}
