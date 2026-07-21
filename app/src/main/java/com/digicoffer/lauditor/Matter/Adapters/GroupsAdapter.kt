package com.digicoffer.lauditor.Matter.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Matter.Models.ClientsModel
import com.digicoffer.lauditor.Matter.Models.GroupsModel
import com.digicoffer.lauditor.Matter.Models.TeamModel
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.Matter.OldViewModels.GCT
import com.digicoffer.lauditor.Matter.ViewModels.GCT_En
import com.digicoffer.lauditor.R
import java.util.ArrayList

class GroupsAdapter : RecyclerView.Adapter<GroupsAdapter.Viewholder> {
    var sharedList: ArrayList<GroupsModel> = ArrayList()
    var list_item: ArrayList<GroupsModel> = ArrayList()
    var clientsList: ArrayList<ClientsModel> = ArrayList()
    var tmList: ArrayList<TeamModel> = ArrayList()
    var groupsList: ArrayList<ViewMatterModel> = ArrayList()
    var gct: GCT? = null
    var gct_en: GCT_En? = null
    var TAG: String = "Groups"

    constructor(
        sharedList: ArrayList<GroupsModel>,
        clientsList: ArrayList<ClientsModel>,
        teamList: ArrayList<TeamModel>,
        groups_list: ArrayList<ViewMatterModel>,
        Tag: String,
        gct: GCT_En?
    ) {
        this.sharedList = sharedList
        this.list_item = sharedList
        this.clientsList = clientsList
        this.tmList = teamList
        this.groupsList = groups_list
        this.TAG = Tag
        this.gct_en = gct
    }

    constructor(
        sharedList: ArrayList<GroupsModel>,
        clientsList: ArrayList<ClientsModel>,
        teamList: ArrayList<TeamModel>,
        groups_list: ArrayList<ViewMatterModel>,
        Tag: String,
        gct: GCT?
    ) {
        this.sharedList = sharedList
        this.list_item = sharedList
        this.clientsList = clientsList
        this.tmList = teamList
        this.groupsList = groups_list
        this.TAG = Tag
        this.gct = gct
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.select_team_members, parent, false)
        return Viewholder(itemView)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        if (TAG == "Groups") {
            val groupsModel = sharedList[position]
            holder.cb_documents.isChecked = sharedList[position].isChecked
            holder.cb_documents.tag = position
            holder.tv_tm_name.text = groupsModel.group_name
            holder.cb_documents.setOnClickListener {
                val clickedgroup = sharedList[(holder.cb_documents.tag as Int)]
                clickedgroup.isChecked = !clickedgroup.isChecked

                if (clickedgroup.isChecked) {
                    if (gct != null && !gct!!.temporary_groups_list.contains(clickedgroup)) {
                        gct!!.temporary_groups_list.add(clickedgroup)
                    }
                } else {
                    if (gct != null) {
                        val actualPos = gct!!.selected_groups_list.indexOf(clickedgroup)
                        if (actualPos != -1) {
                            gct!!.checkingRemovalLogic(actualPos)
                        }
                    }
                }

                gct?.loadGroupsText()
            }
        } else if (TAG == "Clients") {
            val clientsModel = clientsList[position]
            holder.cb_documents.isChecked = clientsList[position].isChecked
            holder.cb_documents.tag = position
            holder.tv_tm_name.text = clientsModel.client_name
            holder.cb_documents.setOnClickListener {
                val pos = holder.cb_documents.tag as Int
                val clickedClient = clientsList[pos]

                clickedClient.isChecked = !clickedClient.isChecked

                if (clickedClient.isChecked) {
                    if (gct != null && !gct!!.temporary_clients_list.contains(clickedClient)) {
                        gct!!.temporary_clients_list.add(clickedClient)
                    }
                } else {
                    if (gct != null) {
                        gct!!.temporary_clients_list.remove(clickedClient)
                        gct!!.selected_clients_list.remove(clickedClient)
                        gct!!.loadClients()
                    }
                }
                gct?.loadClientsText()
            }
        } else if (TAG == "TM") {
            val teamModel = tmList[position]
            holder.cb_documents.isChecked = tmList[position].isChecked
            holder.cb_documents.tag = position
            holder.tv_tm_name.text = teamModel.tm_name
            holder.cb_documents.setOnClickListener {
                val pos = holder.cb_documents.tag as Int
                val clickedtm = tmList[pos]

                clickedtm.isChecked = !clickedtm.isChecked

                if (clickedtm.isChecked) {
                    if (gct_en != null && !gct_en!!.temporary_tm_list.contains(clickedtm)) {
                        gct_en!!.temporary_tm_list.add(clickedtm)
                    }
                } else {
                    if (gct_en != null) {
                        gct_en!!.selected_tm_list.remove(clickedtm)
                        gct_en!!.temporary_tm_list.remove(clickedtm)
                        gct_en!!.loadTeam()
                    }
                }

                gct_en?.loadTeamText()
            }
        } else if (TAG == "UGM") {
            val viewMatterModel = groupsList[position]
            holder.cb_documents.isChecked = groupsList[position].isChecked
            holder.cb_documents.tag = position
            holder.tv_tm_name.text = viewMatterModel.group_name
            holder.cb_documents.setOnClickListener {
                val pos = holder.cb_documents.tag as Int
                groupsList[pos].isChecked = !groupsList[pos].isChecked
            }
        }
    }

    fun getClientsList_item(): ArrayList<ClientsModel> = clientsList

    override fun getItemCount(): Int {
        return when (TAG) {
            "Groups" -> sharedList.size
            "Clients" -> clientsList.size
            "UGM" -> groupsList.size
            else -> tmList.size
        }
    }

    override fun getItemId(position: Int): Long = position.toLong()
    override fun getItemViewType(position: Int): Int = position

    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_tm_name: TextView = itemView.findViewById(R.id.tv_tm_name)
        val cb_documents: CheckBox = itemView.findViewById(R.id.chk_selected)
        val list_line: View = itemView.findViewById(R.id.list_line)
        val select_tm_layout: LinearLayout = itemView.findViewById(R.id.select_tm_layout)

        init {
            list_line.visibility = View.VISIBLE
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 0)
            select_tm_layout.layoutParams = params
            select_tm_layout.setBackgroundResource(R.drawable.background_transparent)
        }
    }
}
