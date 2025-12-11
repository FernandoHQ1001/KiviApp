package com.example.kiviapp

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial

class ObstacleDetectionSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_obstacle_detection_settings)

        // Botón cerrar (la X)
        findViewById<TextView>(R.id.tvCerrarObstaculos).setOnClickListener {
            finish()
        }

        val switchMain   = findViewById<SwitchMaterial>(R.id.switchObstacleMain)
        val switchFloor  = findViewById<SwitchMaterial>(R.id.switchObstacleFloor)
        val switchHead   = findViewById<SwitchMaterial>(R.id.switchObstacleHead)

        // Cargar valores guardados
        val mainEnabled  = KiviSettings.isObstacleAlertEnabled(this)
        val floorEnabled = KiviSettings.isObstacleFloorEnabled(this)
        val headEnabled  = KiviSettings.isObstacleHeadEnabled(this)

        switchMain.isChecked  = mainEnabled
        switchFloor.isChecked = floorEnabled
        switchHead.isChecked  = headEnabled

        // Si la alerta principal está apagada, deshabilitamos los otros switches visualmente
        actualizarEstadoHijos(mainEnabled, switchFloor, switchHead)

        // Listeners para guardar cambios
        switchMain.setOnCheckedChangeListener { _, isChecked ->
            KiviSettings.setObstacleAlertEnabled(this, isChecked)
            actualizarEstadoHijos(isChecked, switchFloor, switchHead)
        }

        switchFloor.setOnCheckedChangeListener { _, isChecked ->
            KiviSettings.setObstacleFloorEnabled(this, isChecked)
        }

        switchHead.setOnCheckedChangeListener { _, isChecked ->
            KiviSettings.setObstacleHeadEnabled(this, isChecked)
        }

        aplicarTema()
        aplicarTamanos()
    }

    override fun onResume() {
        super.onResume()
        aplicarTema()
        aplicarTamanos()
    }

    private fun actualizarEstadoHijos(
        mainOn: Boolean,
        switchFloor: SwitchMaterial,
        switchHead: SwitchMaterial
    ) {
        switchFloor.isEnabled = mainOn
        switchHead.isEnabled  = mainOn
    }

    private fun aplicarTema() {
        val root       = findViewById<ConstraintLayout>(R.id.rootObstacleSettings)
        val cardRoot   = findViewById<MaterialCardView>(R.id.cardObstacleRoot)
        val cardMain   = findViewById<MaterialCardView>(R.id.cardObstacleMain)
        val cardFloor  = findViewById<MaterialCardView>(R.id.cardObstacleFloor)
        val cardHead   = findViewById<MaterialCardView>(R.id.cardObstacleHead)

        val colorFondo      = KiviSettings.getBackgroundColor(this)
        val colorCard       = KiviSettings.getCardColor(this)
        val colorTexto      = KiviSettings.getPrimaryTextColor(this)
        val colorSecundario = KiviSettings.getSecondaryTextColor(this)
        val colorTema       = KiviSettings.getThemeColor(this)
        val temaState       = ColorStateList.valueOf(colorTema)

        root.setBackgroundColor(colorFondo)
        cardRoot.setCardBackgroundColor(colorCard)
        cardMain.setCardBackgroundColor(colorFondo)
        cardFloor.setCardBackgroundColor(colorFondo)
        cardHead.setCardBackgroundColor(colorFondo)

        // Divider
        val divider = findViewById<android.view.View>(R.id.viewObstacleDivider)
        divider.setBackgroundColor(colorSecundario)

        // Títulos
        findViewById<TextView>(R.id.tvTituloObstaculos).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvCerrarObstaculos).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvTiposAlerta).setTextColor(colorTexto)

        // Textos de cada sección
        findViewById<TextView>(R.id.tvLabelObstacleMain).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvSubLabelObstacleMain).setTextColor(colorSecundario)

        findViewById<TextView>(R.id.tvLabelObstacleFloor).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvSubLabelObstacleFloor).setTextColor(colorSecundario)

        findViewById<TextView>(R.id.tvLabelObstacleHead).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvSubLabelObstacleHead).setTextColor(colorSecundario)

        // Switches con color de tema
        val switchMain   = findViewById<SwitchMaterial>(R.id.switchObstacleMain)
        val switchFloor  = findViewById<SwitchMaterial>(R.id.switchObstacleFloor)
        val switchHead   = findViewById<SwitchMaterial>(R.id.switchObstacleHead)

        switchMain.thumbTintList  = temaState
        switchMain.trackTintList  = temaState
        switchFloor.thumbTintList = temaState
        switchFloor.trackTintList = temaState
        switchHead.thumbTintList  = temaState
        switchHead.trackTintList  = temaState
    }

    private fun aplicarTamanos() {
        fun size(base: Float) = KiviSettings.getScaledTextSize(this, base)

        findViewById<TextView>(R.id.tvTituloObstaculos).textSize   = size(20f)
        findViewById<TextView>(R.id.tvTiposAlerta).textSize        = size(14f)

        findViewById<TextView>(R.id.tvLabelObstacleMain).textSize      = size(16f)
        findViewById<TextView>(R.id.tvSubLabelObstacleMain).textSize   = size(12f)
        findViewById<TextView>(R.id.tvLabelObstacleFloor).textSize     = size(14f)
        findViewById<TextView>(R.id.tvSubLabelObstacleFloor).textSize  = size(12f)
        findViewById<TextView>(R.id.tvLabelObstacleHead).textSize      = size(14f)
        findViewById<TextView>(R.id.tvSubLabelObstacleHead).textSize   = size(12f)
    }
}
