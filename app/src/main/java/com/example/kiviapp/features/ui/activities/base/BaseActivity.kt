package com.example.kiviapp.features.ui.activities.base

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // ✅ idioma SIEMPRE desde prefs global
        val appPrefs = newBase.getSharedPreferences("KiviAppPrefs", Context.MODE_PRIVATE)
        val langCode = appPrefs.getString("app_language", "es") ?: "es"

        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = android.content.res.Configuration(newBase.resources.configuration)
        config.setLocale(locale)

        super.attachBaseContext(newBase.createConfigurationContext(config))
    }
}
