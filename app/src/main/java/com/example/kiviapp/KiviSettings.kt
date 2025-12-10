package com.example.kiviapp

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

object KiviSettings {

    // --------------------------------------------------------------
    // PREFERENCIAS POR USUARIO (UID)
    // --------------------------------------------------------------
    private fun prefs(context: Context) =
        context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)

    private fun getPrefsName(): String {
        val user = Firebase.auth.currentUser
        return if (user != null) {
            "kivi_prefs_${user.uid}"       // preferencias personales del usuario
        } else {
            "kivi_prefs_default"           // antes de iniciar sesión (welcome/login)
        }
    }

    // --------------------------------------------------------------
    // MODO OSCURO
    // --------------------------------------------------------------
    private const val KEY_DARK_MODE = "dark_mode"

    fun setDarkMode(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun isDarkMode(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DARK_MODE, true) // por defecto oscuro


    // --------------------------------------------------------------
    // COLOR PRINCIPAL (tema)
    // --------------------------------------------------------------
    private const val KEY_THEME_COLOR = "theme_color"

    fun setThemeColorName(context: Context, name: String) {
        prefs(context).edit().putString(KEY_THEME_COLOR, name).apply()
    }

    fun getThemeColorName(context: Context): String =
        prefs(context).getString(KEY_THEME_COLOR, "Azul") ?: "Azul"

    @ColorInt
    fun getThemeColor(context: Context): Int {
        return when (getThemeColorName(context)) {
            "Morado" -> 0xFF7E57C2.toInt()     // morado suave
            "Verde"  -> 0xFF43A047.toInt()     // verde material
            else     -> 0xFF2979FF.toInt()     // azul por defecto
        }
    }


    // --------------------------------------------------------------
    // COLORES SEGÚN MODO OSCURO
    // --------------------------------------------------------------
    fun getBackgroundColor(context: Context): Int =
        if (isDarkMode(context)) 0xFF1A232E.toInt() else 0xFFF5F5F5.toInt()

    fun getCardColor(context: Context): Int =
        if (isDarkMode(context)) 0xFF2C3A47.toInt() else 0xFFFFFFFF.toInt()

    fun getPrimaryTextColor(context: Context): Int =
        if (isDarkMode(context)) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()

    fun getSecondaryTextColor(context: Context): Int =
        if (isDarkMode(context)) 0xFF90A4AE.toInt() else 0xFF546E7A.toInt()

    fun getIconColor(context: Context): Int =
        if (isDarkMode(context)) 0xFFFFFFFF.toInt() else 0xFF37474F.toInt()


    // --------------------------------------------------------------
    // TAMAÑO DE TEXTO
    // --------------------------------------------------------------
    private const val KEY_TEXT_SIZE = "text_size"   // 0 pequeño - 1 mediano - 2 grande

    fun setTextSizeLevel(context: Context, level: Int) {
        prefs(context).edit().putInt(KEY_TEXT_SIZE, level).apply()
    }

    fun getTextSizeLevel(context: Context): Int =
        prefs(context).getInt(KEY_TEXT_SIZE, 1)

    /** Devuelve un tamaño escalado a partir del base */
    fun getScaledTextSize(context: Context, baseSize: Float): Float {
        return when (getTextSizeLevel(context)) {
            0 -> baseSize * 0.80f    // pequeño
            2 -> baseSize * 1.30f    // grande
            else -> baseSize         // mediano
        }
    }


    // --------------------------------------------------------------
    // VOLUMEN GENERAL / VOLUMEN VOZ
    // --------------------------------------------------------------
    private const val KEY_VOL_GENERAL = "vol_general"
    private const val KEY_VOL_VOZ = "vol_voz"

    fun setVolGeneral(context: Context, value: Int) {
        prefs(context).edit().putInt(KEY_VOL_GENERAL, value).apply()
    }

    fun getVolGeneral(context: Context): Int =
        prefs(context).getInt(KEY_VOL_GENERAL, 70)

    fun setVolVoz(context: Context, value: Int) {
        prefs(context).edit().putInt(KEY_VOL_VOZ, value).apply()
    }

    fun getVolVoz(context: Context): Int =
        prefs(context).getInt(KEY_VOL_VOZ, 70)


    // --------------------------------------------------------------
    // RESETEAR CONFIG DE USUARIO (OPCIONAL)
    // --------------------------------------------------------------
    fun resetCurrentUserSettings(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
