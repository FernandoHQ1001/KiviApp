package com.example.kiviapp.features.ui.activities.base

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

/*
   Clase base para todas las Activities de Kivi.
   Se encarga de aplicar el idioma configurado
 */
open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Accede a las preferencias globales de la app
        val appPrefs = newBase.getSharedPreferences("KiviAppPrefs", Context.MODE_PRIVATE)

        // Obtiene el idioma guardado (por defecto español)
        val langCode = appPrefs.getString("app_language", "es") ?: "es"

        val locale = Locale(langCode)
        Locale.setDefault(locale)

        // Crea una nueva configuración con el idioma seleccionado
        val config = android.content.res.Configuration(newBase.resources.configuration)
        config.setLocale(locale)

        // Aplica el nuevo contexto con el idioma correcto
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }
}
