package com.digicoffer.lauditor.Meetings.ViewModels

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Meetings.Models.DocumentsDo
import com.digicoffer.lauditor.Meetings.Models.RelationshipsDO
import com.digicoffer.lauditor.Meetings.Models.TeamDo
import com.digicoffer.lauditor.R
import java.util.ArrayList

class CommonRelationshipsAdapter(
    var tmList: ArrayList<TeamDo>,
    var TAG: String = "TM",
    var individual_list: ArrayList<RelationshipsDO>,
    var entity_client_list: ArrayList<RelationshipsDO>,
    var entity_corp_client_list: ArrayList<RelationshipsDO>,
    var documents_list: ArrayList<DocumentsDo>,
    private val createEvent: CreateEvent
) : RecyclerView.Adapter<CommonRelationshipsAdapter.Viewholder>() {

    var sharedList: ArrayList<TeamDo> = ArrayList()
    var list_item: ArrayList<TeamDo> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.select_team_members, parent, false)
        return Viewholder(itemView)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        if (TAG == "TM") {
            val teamModel = tmList[position]
            holder.cb_documents.isChecked = tmList[position].isChecked
            holder.cb_documents.tag = position
            holder.tv_tm_name.text = teamModel.name
            holder.cb_documents.setOnClickListener {
                val pos = holder.cb_documents.tag as Int
                val list = tmList[pos]
                list.isChecked = !list.isChecked
                if (list.isChecked) {
                    if (!createEvent.selected_tm_list.contains(list)) {
                        createEvent.selected_tm_list.add(list)
                    }
                } else {
                    createEvent.selected_tm_list.remove(list)
                }
                createEvent.selected_Team(tmList)
            }
        } else if (TAG == "INDIVIDUAL") {
            val relationshipsDO = individual_list[position]
            holder.cb_documents.isChecked = individual_list[position].isChecked
            holder.cb_documents.tag = position
            holder.tv_tm_name.text = relationshipsDO.name
            holder.cb_documents.setOnClickListener {
                val pos = holder.cb_documents.tag as Int
                val list = individual_list[pos]
                list.isChecked = !list.isChecked
                if (list.isChecked) {
                    if (!createEvent.selected_individual_list.contains(list)) {
                        createEvent.selected_individual_list.add(list)
                    }
                } else {
                    createEvent.selected_individual_list.remove(list)
                }
                createEvent.selected_individual(individual_list)
            }
        } else if (TAG == "Documents") {
            val documentsDo = documents_list[position]
            holder.cb_documents.isChecked = documents_list[position].isChecked
            holder.cb_documents.tag = position
            holder.tv_tm_name.text = documentsDo.name
            holder.cb_documents.setOnClickListener {
                val pos = holder.cb_documents.tag as Int
                val doc = documents_list[pos]
                doc.isChecked = !doc.isChecked
                if (doc.isChecked) {
                    if (!createEvent.selected_documents_list.contains(doc)) {
                        createEvent.selected_documents_list.add(doc)
                    }
                } else {
                    createEvent.selected_documents_list.remove(doc)
                }
                createEvent.selected_documents(documents_list)
            }
        } else if (TAG == "CORPORATE") {
            val relationshipsDO = entity_corp_client_list[position]
            holder.cb_documents.isChecked = entity_corp_client_list[position].isChecked
            holder.cb_documents.tag = position
            holder.tv_tm_name.text = relationshipsDO.name
            holder.cb_documents.setOnClickListener {
                val pos = holder.cb_documents.tag as Int
                val list = entity_corp_client_list[pos]
                list.isChecked = !list.isChecked
                if (list.isChecked) {
                    if (!createEvent.selected_entity_corp_client_list.contains(list)) {
                        createEvent.selected_entity_corp_client_list.add(list)
                    }
                } else {
                    createEvent.selected_entity_corp_client_list.remove(list)
                }
                createEvent.selected_corporate(entity_corp_client_list)
            }
        } else {
            val relationshipsDO = entity_client_list[position]
            holder.cb_documents.isChecked = entity_client_list[position].isChecked
            holder.cb_documents.tag = position
            holder.tv_tm_name.text = relationshipsDO.name
            holder.cb_documents.setOnClickListener {
                val pos = holder.cb_documents.tag as Int
                val list = entity_client_list[pos]
                list.isChecked = !list.isChecked
                if (list.isChecked) {
                    if (!createEvent.selected_entity_client_list.contains(list)) {
                        createEvent.selected_entity_client_list.add(list)
                    }
                } else {
                    createEvent.selected_entity_client_list.remove(list)
                }
                createEvent.selected_entity_clients(entity_client_list)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (TAG == "TM") {
            tmList.size
        } else if (TAG == "INDIVIDUAL") {
            individual_list.size
        } else if (TAG == "Documents") {
            documents_list.size
        } else if (TAG == "CORPORATE") {
            entity_corp_client_list.size
        } else {
            entity_client_list.size
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_tm_name: TextView = itemView.findViewById(R.id.tv_tm_name)
        val list_line: View = itemView.findViewById(R.id.list_line)
        val select_tm_layout: LinearLayout = itemView.findViewById(R.id.select_tm_layout)
        val cb_documents: CheckBox = itemView.findViewById(R.id.chk_selected)

        init {
            list_line.visibility = View.VISIBLE
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 0)
            select_tm_layout.layoutParams = params
            select_tm_layout.setBackgroundResource(R.drawable.background_transparent)
        }
    }
}
