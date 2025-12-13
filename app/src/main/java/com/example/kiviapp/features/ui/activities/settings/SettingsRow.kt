package com.example.kiviapp.features.ui.activities.settings

import androidx.annotation.DrawableRes

sealed class SettingsRow {
    data class Header(val title: String) : SettingsRow()

    data class Item(
        val id: String,
        @DrawableRes val iconRes: Int,
        val title: String,
        val subtitle: String,
        val onClick: () -> Unit
    ) : SettingsRow()
}
