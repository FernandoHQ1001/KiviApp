package com.example.kiviapp.features.ui.activities.settings

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kiviapp.features.ui.activities.settings.KiviSettings
import com.example.kiviapp.R
import com.example.kiviapp.features.ui.activities.base.BaseActivity

class VoiceNavigationActivity : BaseActivity() {

    private lateinit var adapter: SettingsAdapter
    private lateinit var rv: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_navigation)

        findViewById<ImageButton>(R.id.btnBackVoiceNav).setOnClickListener { finish() }

        rv = findViewById(R.id.rvVoiceNav)
        rv.layoutManager = LinearLayoutManager(this)

        val rows = buildRows()
        adapter = SettingsAdapter(this, rows)
        rv.adapter = adapter

        aplicarTemaPantalla()
        aplicarTamanosPantalla()
    }

    override fun onResume() {
        super.onResume()
        aplicarTemaPantalla()
        aplicarTamanosPantalla()
        adapter.refreshTheme()
    }

    private fun buildRows(): List<SettingsRow> {
        return listOf(
            // Si quieres header:
            // SettingsRow.Header(getString(R.string.voice_nav_title)),

            SettingsRow.Item(
                id = "voice_mobility",
                iconRes = android.R.drawable.ic_btn_speak_now,
                title = getString(R.string.mobility_assistance),
                subtitle = getString(R.string.mobility_assistance_subtitle),
                onClick = {
                    startActivity(Intent(this, MobilityAssistanceSettingsActivity::class.java))
                }
            ),
            SettingsRow.Item(
                id = "voice_obstacles",
                iconRes = android.R.drawable.ic_menu_compass,
                title = getString(R.string.obstacle_detection),
                subtitle = getString(R.string.obstacle_detection_subtitle),
                onClick = {
                    startActivity(Intent(this, ObstacleDetectionSettingsActivity::class.java))
                }
            )
        )
    }

    private fun aplicarTemaPantalla() {
        val root = findViewById<ConstraintLayout>(R.id.rootVoiceNav)

        val colorFondo = KiviSettings.getBackgroundColor(this)
        val colorTexto = KiviSettings.getPrimaryTextColor(this)
        val colorSec = KiviSettings.getSecondaryTextColor(this)
        val iconState = ColorStateList.valueOf(KiviSettings.getIconColor(this))

        root.setBackgroundColor(colorFondo)

        findViewById<TextView>(R.id.tvVoiceNavTitle).setTextColor(colorTexto)
        findViewById<ImageButton>(R.id.btnBackVoiceNav).imageTintList = iconState

        findViewById<View>(R.id.viewVoiceNavDivider).setBackgroundColor(colorSec)
    }

    private fun aplicarTamanosPantalla() {
        fun size(base: Float) = KiviSettings.getScaledTextSize(this, base)
        findViewById<TextView>(R.id.tvVoiceNavTitle).textSize = size(22f)
    }
}
