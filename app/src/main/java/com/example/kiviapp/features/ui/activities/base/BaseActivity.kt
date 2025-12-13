package com.example.kiviapp.features.ui.activities.base

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import java.util.*

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val sharedPreferences = newBase.getSharedPreferences("KiviAppPrefs", MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("app_language", "es") ?: "es"
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = android.content.res.Configuration()
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
}
