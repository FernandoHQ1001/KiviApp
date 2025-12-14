package com.example.kiviapp.features.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.kiviapp.R
import com.example.kiviapp.features.ui.activities.base.BaseActivity
import com.example.kiviapp.features.ui.activities.settings.KiviSettings
import com.google.android.material.button.MaterialButton

class TutorialActivity : BaseActivity() {

    private var player: ExoPlayer? = null

    private lateinit var playerView: PlayerView
    private lateinit var btnPlay: MaterialButton
    private lateinit var overlayPlay: View
    private lateinit var tvHintPlay: TextView
    private lateinit var btnContinuar: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        playerView = findViewById(R.id.playerViewTutorial)
        btnPlay = findViewById(R.id.btnPlay)
        overlayPlay = findViewById(R.id.overlayPlay)
        tvHintPlay = findViewById(R.id.tvHintPlay)
        btnContinuar = findViewById(R.id.btnContinuar)

        aplicarTema()
        aplicarTamanos()

        initPlayer()

        btnPlay.setOnClickListener {
            overlayPlay.visibility = View.GONE
            tvHintPlay.visibility = View.GONE
            btnPlay.visibility = View.GONE
            player?.play()
            playerView.showController()
        }

        btnContinuar.setOnClickListener {
            irAMain()
        }
    }

    private fun initPlayer() {
        val rawRes = getTutorialVideoRes()
        val uri = Uri.parse("android.resource://$packageName/$rawRes")

        player = ExoPlayer.Builder(this).build().also { exo ->
            playerView.player = exo
            exo.setMediaItem(MediaItem.fromUri(uri))
            exo.prepare()
            exo.playWhenReady = false
        }

        player?.addListener(object : androidx.media3.common.Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == androidx.media3.common.Player.STATE_ENDED) {
                    irAMain()
                }
            }
        })

        overlayPlay.visibility = View.VISIBLE
        tvHintPlay.visibility = View.VISIBLE
        btnPlay.visibility = View.VISIBLE
    }

    private fun getTutorialVideoRes(): Int {
        val lang = KiviSettings.getAppLanguage(this)
        return when {
            lang.startsWith("en") -> R.raw.tutorial_en
            else -> R.raw.tutorial_es
        }
    }

    private fun irAMain() {
        // ✅ Marca tutorial visto (local + firebase)
        KiviSettings.setTutorialVisto(this, true)

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }

    private fun aplicarTema() {
        val root = findViewById<View>(R.id.rootTutorial)

        val colorFondo = KiviSettings.getBackgroundColor(this)
        val colorCard = KiviSettings.getCardColor(this)
        val colorTexto = KiviSettings.getPrimaryTextColor(this)
        val colorSec = KiviSettings.getSecondaryTextColor(this)
        val colorTema = KiviSettings.getThemeColor(this)

        root.setBackgroundColor(colorFondo)

        findViewById<TextView>(R.id.tvTituloTutorial).setTextColor(colorTexto)
        findViewById<TextView>(R.id.tvSubtituloTutorial).setTextColor(colorSec)
        tvHintPlay.setTextColor(colorTexto)

        val card = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardTutorialVideo)
        card.setCardBackgroundColor(colorCard)
        card.strokeColor = colorTema

        btnPlay.backgroundTintList = android.content.res.ColorStateList.valueOf(colorTema)
        btnPlay.iconTint = android.content.res.ColorStateList.valueOf(0xFFFFFFFF.toInt())

        btnContinuar.backgroundTintList = android.content.res.ColorStateList.valueOf(colorTema)
        btnContinuar.setTextColor(0xFFFFFFFF.toInt())
    }

    private fun aplicarTamanos() {
        fun size(base: Float) = KiviSettings.getScaledTextSize(this, base)

        findViewById<TextView>(R.id.tvTituloTutorial).textSize = size(24f)
        findViewById<TextView>(R.id.tvSubtituloTutorial).textSize = size(14f)
        tvHintPlay.textSize = size(14f)
        btnContinuar.textSize = size(14f)
    }
}
