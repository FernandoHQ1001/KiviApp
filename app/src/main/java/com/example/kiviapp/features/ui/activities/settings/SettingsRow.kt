package com.example.kiviapp.features.ui.activities.settings

import androidx.annotation.DrawableRes

/*
 * Modelo que representa una fila del menú de configuración.
 * Puede ser un encabezado o un ítem interactivo.
 */
sealed class SettingsRow {

    /*
     * Header
     * Fila de título que separa secciones dentro del menú de configuración.
     */
    data class Header(val title: String) : SettingsRow()

    /*
     * Item
     * Fila clickeable que representa una opción de configuración.
     */
    data class Item(
        val id: String,  // Identificador único
        @DrawableRes val iconRes: Int, // Icono del ítem
        val title: String, // Texto principal
        val subtitle: String,   // Texto secundario
        val onClick: () -> Unit // Acción al tocar
    ) : SettingsRow()
}
