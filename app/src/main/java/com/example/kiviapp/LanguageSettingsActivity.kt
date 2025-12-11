package com.example.kiviapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class LanguageSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_settings)

        val btnBack = findViewById<ImageButton>(R.id.btnBackLanguage)
        btnBack.setOnClickListener { finish() }

        // Por ahora "Idioma de la interfaz" no hace nada
        val cardIdiomaInterfaz =
            findViewById<MaterialCardView>(R.id.cardIdiomaInterfaz)

        cardIdiomaInterfaz.setOnClickListener {
            // TODO: futuro - cambiar textos de la UI
        }

        // 🔊 Idioma de voz → abre el selector
        val cardIdiomaVoz =
            findViewById<MaterialCardView>(R.id.cardIdiomaVoz)

        cardIdiomaVoz.setOnClickListener {
            startActivity(Intent(this, VoiceLanguageActivity::class.java))
        }

        // Region / Teclado los puedes conectar después
    }
}
