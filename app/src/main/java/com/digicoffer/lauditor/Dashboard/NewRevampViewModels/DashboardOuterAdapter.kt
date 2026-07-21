package com.digicoffer.lauditor.Dashboard.NewRevampViewModels

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.R
import java.util.ArrayList

class DashboardOuterAdapter(
    private val context: Context,
    private val listener: DashboardCardAdapter.CardActionListener
) : RecyclerView.Adapter<DashboardOuterAdapter.SectionHolder>() {

    private var sections: List<DashboardSection> = ArrayList()

    fun submitSections(newSections: List<DashboardSection>?) {
        this.sections = newSections ?: ArrayList()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = sections.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard_section, parent, false)
        return SectionHolder(v)
    }

    override fun onBindViewHolder(holder: SectionHolder, position: Int) {
        holder.bind(sections[position])
    }

    inner class SectionHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val tvTitle: TextView = v.findViewById(R.id.tv_section_title)
        private val tvBadge: TextView = v.findViewById(R.id.tv_section_badge)
        private val rvCards: RecyclerView = v.findViewById(R.id.rv_section_cards)
        private val llm: LinearLayoutManager = LinearLayoutManager(
            itemView.context,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        init {
            rvCards.layoutManager = llm
        }

        fun bind(section: DashboardSection) {
            tvTitle.text = section.title

            // Section badge: always visible, shows card count
            tvBadge.text = section.cardCount().toString()
            tvBadge.visibility = View.VISIBLE

            val adapter = DashboardCardAdapter(context, listener)
            adapter.submitList(section.items)
            rvCards.adapter = adapter
        }
    }
}
