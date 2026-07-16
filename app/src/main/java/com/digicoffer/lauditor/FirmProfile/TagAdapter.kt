package com.digicoffer.lauditor.FirmProfile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.R
import org.json.JSONArray

class TagAdapter(private val list: JSONArray?, private val style: Style) :
    RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    enum class Style {
        PRACTICE, SERVICE, LANGUAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_language, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.tvTag.text = list?.optString(position, "") ?: ""
        when (style) {
            Style.PRACTICE, Style.SERVICE, Style.LANGUAGE -> {
                holder.tvTag.setBackgroundResource(R.drawable.chip_pale_blue)
            }
        }
    }

    override fun getItemCount(): Int {
        return list?.length() ?: 0
    }

    class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTag: TextView = itemView.findViewById(R.id.tvLanguage)
    }

    companion object {
        private const val CARD_HEIGHT_DP = 60
    }
}
