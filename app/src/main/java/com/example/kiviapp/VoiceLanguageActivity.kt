package com.example.kiviapp

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class VoiceLanguageActivity : AppCompatActivity() {

    private lateinit var tvIdiomaActual: TextView

    // Idiomas de voz disponibles
    private val idiomas = listOf(
        "Español" to "es",
        "English" to "en"
        // añade más si quieres: "Français" to "fr", etc.
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_language)

        val tvCerrar = findViewById<TextView>(R.id.tvCerrarVoiceLanguage)
        tvCerrar.setOnClickListener { finish() }

        tvIdiomaActual = findViewById(R.id.tvIdiomaActualVoz)

        // Mostrar idioma actual guardado
        val langCode = KiviSettings.getVoiceLanguage(this)
        val nombreIdioma = idiomas.firstOrNull { it.second == langCode }?.first ?: "Español"
        tvIdiomaActual.text = nombreIdioma

        val cardSelector =
            findViewById<MaterialCardView>(R.id.cardSelectorIdiomaVoz)

        cardSelector.setOnClickListener {
            mostrarDialogoIdiomas()
        }
    }

    private fun mostrarDialogoIdiomas() {
        val nombres = idiomas.map { it.first }.toTypedArray()

        val langCodeActual = KiviSettings.getVoiceLanguage(this)
        var checkedItem = idiomas.indexOfFirst { it.second == langCodeActual }
        if (checkedItem < 0) checkedItem = 0

        AlertDialog.Builder(this)
            .setTitle("Selecciona un idioma de voz")
            .setSingleChoiceItems(nombres, checkedItem) { dialog, which ->
                val (nombre, code) = idiomas[which]
                KiviSettings.setVoiceLanguage(this, code)
                tvIdiomaActual.text = nombre
                Toast.makeText(this, "Idioma de voz cambiado a $nombre", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
