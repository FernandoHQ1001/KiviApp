package com.example.kiviapp

import android.content.Context
import androidx.annotation.ColorInt
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
            "kivi_prefs_default"           // antes de iniciar sesión
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
            "Morado" -> 0xFF7E57C2.toInt()
            "Verde"  -> 0xFF43A047.toInt()
            else     -> 0xFF2979FF.toInt()   // Azul por defecto
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
    // MOVILIDAD: voz / háptico
    // --------------------------------------------------------------
    private const val KEY_VOICE_ENABLED = "voice_enabled"
    private const val KEY_HAPTIC_ENABLED = "haptic_enabled"

    fun setVoiceEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_VOICE_ENABLED, enabled).apply()
    }

    fun isVoiceEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_VOICE_ENABLED, true)

    fun setHapticEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_HAPTIC_ENABLED, enabled).apply()
    }

    fun isHapticEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HAPTIC_ENABLED, true)

    // --------------------------------------------------------------
    // DETECCIÓN DE OBSTÁCULOS
    // --------------------------------------------------------------
    private const val KEY_OBSTACLE_ALERT = "obstacle_alert_main"
    private const val KEY_OBSTACLE_FLOOR = "obstacle_floor"
    private const val KEY_OBSTACLE_HEAD  = "obstacle_head"

    // ON/OFF general
    fun setObstacleAlertEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_OBSTACLE_ALERT, enabled).apply()
    }

    fun isObstacleAlertEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_OBSTACLE_ALERT, true)

    // Obstáculos a nivel del suelo
    fun setObstacleFloorEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_OBSTACLE_FLOOR, enabled).apply()
    }

    fun isObstacleFloorEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_OBSTACLE_FLOOR, true)

    // Obstáculos a la altura de la cabeza
    fun setObstacleHeadEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_OBSTACLE_HEAD, enabled).apply()
    }

    fun isObstacleHeadEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_OBSTACLE_HEAD, true)

    // --------------------------------------------------------------
    // RESET
    // --------------------------------------------------------------
    fun resetCurrentUserSettings(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
