package com.example.kiviapp

import android.content.Context
import androidx.annotation.ColorInt
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase

object KiviSettings {

    // --------------------------------------------------------------
    // FIRESTORE
    // --------------------------------------------------------------
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private fun currentUserId(): String? = Firebase.auth.currentUser?.uid

    // Nombre del campo anidado dentro del documento del usuario
    private const val FIRESTORE_SETTINGS_FIELD = "settings"

    // --------------------------------------------------------------
    // PREFERENCIAS POR USUARIO (UID) - LOCAL
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
        syncToCloud(context)
    }

    fun isDarkMode(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DARK_MODE, true) // por defecto oscuro

    // --------------------------------------------------------------
    // COLOR PRINCIPAL (tema)
    // --------------------------------------------------------------
    private const val KEY_THEME_COLOR = "theme_color"

    fun setThemeColorName(context: Context, name: String) {
        prefs(context).edit().putString(KEY_THEME_COLOR, name).apply()
        syncToCloud(context)
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
        syncToCloud(context)
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
        syncToCloud(context)
    }

    fun getVolGeneral(context: Context): Int =
        prefs(context).getInt(KEY_VOL_GENERAL, 70)

    fun setVolVoz(context: Context, value: Int) {
        prefs(context).edit().putInt(KEY_VOL_VOZ, value).apply()
        syncToCloud(context)
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
        syncToCloud(context)
    }

    fun isVoiceEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_VOICE_ENABLED, true)

    fun setHapticEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_HAPTIC_ENABLED, enabled).apply()
        syncToCloud(context)
    }

    fun isHapticEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HAPTIC_ENABLED, true)

    // --------------------------------------------------------------
    // DETECCIÓN DE OBSTÁCULOS
    // --------------------------------------------------------------
    private const val KEY_OBSTACLE_ALERT = "obstacle_alert_main"
    private const val KEY_OBSTACLE_FLOOR = "obstacle_floor"
    private const val KEY_OBSTACLE_HEAD  = "obstacle_head"

    fun setObstacleAlertEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_OBSTACLE_ALERT, enabled).apply()
        syncToCloud(context)
    }

    fun isObstacleAlertEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_OBSTACLE_ALERT, true)

    fun setObstacleFloorEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_OBSTACLE_FLOOR, enabled).apply()
        syncToCloud(context)
    }

    fun isObstacleFloorEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_OBSTACLE_FLOOR, true)

    fun setObstacleHeadEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_OBSTACLE_HEAD, enabled).apply()
        syncToCloud(context)
    }

    fun isObstacleHeadEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_OBSTACLE_HEAD, true)

    // --------------------------------------------------------------
    // IDIOMA DE VOZ
    // --------------------------------------------------------------
    private const val KEY_VOICE_LANGUAGE = "voice_language"

    // "es" por defecto
    fun getVoiceLanguage(context: Context): String =
        prefs(context).getString(KEY_VOICE_LANGUAGE, "es") ?: "es"

    fun setVoiceLanguage(context: Context, langCode: String) {
        prefs(context).edit().putString(KEY_VOICE_LANGUAGE, langCode).apply()
        syncToCloud(context)
    }

    // --------------------------------------------------------------
    // MAPA DE SETTINGS (para subir/bajar de Firebase)
    // --------------------------------------------------------------
    private fun buildSettingsMap(context: Context): Map<String, Any> {
        return mapOf(
            KEY_DARK_MODE to isDarkMode(context),
            KEY_THEME_COLOR to getThemeColorName(context),
            KEY_TEXT_SIZE to getTextSizeLevel(context),
            KEY_VOL_GENERAL to getVolGeneral(context),
            KEY_VOL_VOZ to getVolVoz(context),
            KEY_VOICE_ENABLED to isVoiceEnabled(context),
            KEY_HAPTIC_ENABLED to isHapticEnabled(context),
            KEY_OBSTACLE_ALERT to isObstacleAlertEnabled(context),
            KEY_OBSTACLE_FLOOR to isObstacleFloorEnabled(context),
            KEY_OBSTACLE_HEAD to isObstacleHeadEnabled(context),
            KEY_VOICE_LANGUAGE to getVoiceLanguage(context)
        )
    }

    private fun applySettingsMap(context: Context, data: Map<String, Any>) {
        val editor = prefs(context).edit()

        (data[KEY_DARK_MODE] as? Boolean)?.let {
            editor.putBoolean(KEY_DARK_MODE, it)
        }
        (data[KEY_THEME_COLOR] as? String)?.let {
            editor.putString(KEY_THEME_COLOR, it)
        }
        (data[KEY_TEXT_SIZE] as? Long ?: data[KEY_TEXT_SIZE] as? Int)?.let {
            editor.putInt(KEY_TEXT_SIZE, it.toInt())
        }
        (data[KEY_VOL_GENERAL] as? Long ?: data[KEY_VOL_GENERAL] as? Int)?.let {
            editor.putInt(KEY_VOL_GENERAL, it.toInt())
        }
        (data[KEY_VOL_VOZ] as? Long ?: data[KEY_VOL_VOZ] as? Int)?.let {
            editor.putInt(KEY_VOL_VOZ, it.toInt())
        }
        (data[KEY_VOICE_ENABLED] as? Boolean)?.let {
            editor.putBoolean(KEY_VOICE_ENABLED, it)
        }
        (data[KEY_HAPTIC_ENABLED] as? Boolean)?.let {
            editor.putBoolean(KEY_HAPTIC_ENABLED, it)
        }
        (data[KEY_OBSTACLE_ALERT] as? Boolean)?.let {
            editor.putBoolean(KEY_OBSTACLE_ALERT, it)
        }
        (data[KEY_OBSTACLE_FLOOR] as? Boolean)?.let {
            editor.putBoolean(KEY_OBSTACLE_FLOOR, it)
        }
        (data[KEY_OBSTACLE_HEAD] as? Boolean)?.let {
            editor.putBoolean(KEY_OBSTACLE_HEAD, it)
        }
        (data[KEY_VOICE_LANGUAGE] as? String)?.let {
            editor.putString(KEY_VOICE_LANGUAGE, it)
        }

        editor.apply()
    }

    // --------------------------------------------------------------
    // SINCRONIZAR A FIREBASE (SUBIR)
    // --------------------------------------------------------------
    fun syncToCloud(context: Context) {
        val uid = currentUserId() ?: return

        val settingsMap = buildSettingsMap(context)
        val wrapper = mapOf(
            FIRESTORE_SETTINGS_FIELD to settingsMap
        )

        db.collection("usuarios")
            .document(uid)
            .set(wrapper, SetOptions.merge())
    }

    // --------------------------------------------------------------
    // CARGAR DESDE FIREBASE (BAJAR)
    // --------------------------------------------------------------
    fun loadFromCloud(context: Context, onComplete: (Boolean) -> Unit) {
        val uid = currentUserId() ?: run {
            onComplete(false)
            return
        }

        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val settingsAny = snapshot.get(FIRESTORE_SETTINGS_FIELD)
                if (settingsAny is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    val settingsMap = settingsAny as Map<String, Any>
                    applySettingsMap(context, settingsMap)
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    // --------------------------------------------------------------
    // RESET LOCAL (no borra Firebase)
    // --------------------------------------------------------------
    fun resetCurrentUserSettings(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
