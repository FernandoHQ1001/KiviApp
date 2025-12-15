package com.example.kiviapp.features.ui.activities.settings

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kiviapp.features.ui.activities.settings.KiviSettings
import com.example.kiviapp.R
import com.google.android.material.card.MaterialCardView

/*
 * Adaptador del RecyclerView que muestra
 * las opciones de configuración de Kivi.
 * Soporta encabezados y elementos clickeables.
 */
class SettingsAdapter(
    private val context: Context,
    private val rows: List<SettingsRow>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    /*
     * Define el tipo de vista según la fila
     */
    override fun getItemViewType(position: Int): Int {
        return when (rows[position]) {
            is SettingsRow.Header -> TYPE_HEADER
            is SettingsRow.Item -> TYPE_ITEM
        }
    }

    /*
     * Crea el ViewHolder correspondiente
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_setting_header, parent, false)
                HeaderVH(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_setting_card, parent, false)
                ItemVH(view)
            }
        }
    }

    override fun getItemCount(): Int = rows.size

    /*
     * Asigna los datos a cada fila
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is SettingsRow.Header -> (holder as HeaderVH).bind(row)
            is SettingsRow.Item -> (holder as ItemVH).bind(row)
        }
    }

    /*
     * Fuerza refresco visual del tema
     */
    fun refreshTheme() {
        notifyDataSetChanged()
    }

    /*
     * ViewHolder para encabezados
     */
    private inner class HeaderVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader: TextView = itemView.findViewById(R.id.tvHeader)

        fun bind(row: SettingsRow.Header) {
            tvHeader.text = row.title

            val colorSecundario = KiviSettings.getSecondaryTextColor(context)
            tvHeader.setTextColor(colorSecundario)

            tvHeader.textSize = KiviSettings.getScaledTextSize(context, 14f)
        }
    }

    /*
     * ViewHolder para ítems de configuración
     */
    private inner class ItemVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.cardRoot)
        private val icon: ImageView = itemView.findViewById(R.id.icon)
        private val iconNext: ImageView = itemView.findViewById(R.id.iconNext)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)

        fun bind(row: SettingsRow.Item) {
            icon.setImageResource(row.iconRes)
            tvTitle.text = row.title
            tvSubtitle.text = row.subtitle

            // --- Tema ---
            val colorCard = KiviSettings.getCardColor(context)
            val colorTexto = KiviSettings.getPrimaryTextColor(context)
            val colorSecundario = KiviSettings.getSecondaryTextColor(context)
            val colorTema = KiviSettings.getThemeColor(context)

            card.setCardBackgroundColor(colorCard)
            card.strokeColor = colorTema

            val temaState = ColorStateList.valueOf(colorTema)
            tvTitle.setTextColor(colorTexto)
            tvSubtitle.setTextColor(colorSecundario)

            icon.imageTintList = temaState
            iconNext.imageTintList = temaState

            // --- Tamaños ---
            tvTitle.textSize = KiviSettings.getScaledTextSize(context, 16f)
            tvSubtitle.textSize = KiviSettings.getScaledTextSize(context, 12f)

            // Click
            card.setOnClickListener { row.onClick() }
        }
    }
}
