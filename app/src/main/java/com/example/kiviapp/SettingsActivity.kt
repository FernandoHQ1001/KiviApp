package com.example.kiviapp

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnBack = findViewById<ImageButton>(R.id.btnBackSettings)
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // De momento solo podrías mostrar un Toast o abrir futuras pantallas
        val cardSonido = findViewById<MaterialCardView>(R.id.cardSonidoVolumen)
        val cardTexto = findViewById<MaterialCardView>(R.id.cardTamanoTexto)
        val cardApariencia = findViewById<MaterialCardView>(R.id.cardApariencia)

        // Aquí luego puedes crear nuevas activities para cada opción
        cardSonido.setOnClickListener { /* TODO */ }
        cardTexto.setOnClickListener { /* TODO */ }
        cardApariencia.setOnClickListener { /* TODO */ }
    }
}
