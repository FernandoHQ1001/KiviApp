package com.example.kiviapp.features.ui.activities.settings

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.kiviapp.R
import com.google.android.material.card.MaterialCardView

class VoiceNavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout. activity_voice_navigation)

        // Botón volver
        findViewById<ImageButton>(R.id.btnBackVoiceNav).setOnClickListener {
            finish()
        }

        // Cards
        val cardMobility = findViewById<MaterialCardView>(R.id.cardVoiceMobility)
        val cardObstacles = findViewById<MaterialCardView>(R.id.cardVoiceObstacles)

        cardMobility.setOnClickListener {
            val intent = Intent(this, MobilityAssistanceSettingsActivity::class.java)
            startActivity(intent)
        }

        cardObstacles.setOnClickListener {
            val intent = Intent(this, ObstacleDetectionSettingsActivity::class.java)
            startActivity(intent)
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
        val root = findViewById<ConstraintLayout>(R.id.rootVoiceNav)

        val cardMobility = findViewById<MaterialCardView>(R.id.cardVoiceMobility)
        val cardObstacles = findViewById<MaterialCardView>(R.id.cardVoiceObstacles)

        val colorFondo = KiviSettings.getBackgroundColor(this)
        val colorCard = KiviSettings.getCardColor(this)
        val colorTexto = KiviSettings.getPrimaryTextColor(this)
        val colorSecundario = KiviSettings.getSecondaryTextColor(this)
        val colorTema = KiviSettings.getThemeColor(this)
        val temaState = ColorStateList.valueOf(colorTema)
        val iconState = ColorStateList.valueOf(KiviSettings.getIconColor(this))

        root.setBackgroundColor(colorFondo)

        // Header
        val tvTitle = findViewById<TextView>(R.id.tvVoiceNavTitle)
        tvTitle.setTextColor(colorTexto)
        findViewById<ImageButton>(R.id.btnBackVoiceNav).imageTintList = iconState

        val divider = findViewById<View>(R.id.viewVoiceNavDivider)
        divider.setBackgroundColor(colorSecundario)

        // Cards
        cardMobility.setCardBackgroundColor(colorCard)
        cardMobility.strokeColor = colorTema
        cardObstacles.setCardBackgroundColor(colorCard)
        cardObstacles.strokeColor = colorTema

        // Textos de cards
        findViewById<TextView>(R.id.tvVoiceMobilityTitle).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvVoiceMobilitySubtitle).setTextColor(colorSecundario)
        findViewById<TextView>(R.id.tvVoiceObstaclesTitle).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvVoiceObstaclesSubtitle).setTextColor(colorSecundario)

        // Iconos
        findViewById<ImageView>(R.id.iconVoiceMobility).imageTintList = temaState
        findViewById<ImageView>(R.id.iconVoiceMobilityNext).imageTintList = temaState
        findViewById<ImageView>(R.id.iconVoiceObstacles).imageTintList = temaState
        findViewById<ImageView>(R.id.iconVoiceObstaclesNext).imageTintList = temaState
    }

    private fun aplicarTamanos() {
        fun size(base: Float) = KiviSettings.getScaledTextSize(this, base)

        findViewById<TextView>(R.id.tvVoiceNavTitle).textSize = size(22f)

        findViewById<TextView>(R.id.tvVoiceMobilityTitle).textSize = size(16f)
        findViewById<TextView>(R.id.tvVoiceMobilitySubtitle).textSize = size(12f)
        findViewById<TextView>(R.id.tvVoiceObstaclesTitle).textSize = size(16f)
        findViewById<TextView>(R.id.tvVoiceObstaclesSubtitle).textSize = size(12f)
    }
}
