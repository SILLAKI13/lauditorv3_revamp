package com.digicoffer.lauditor.Matter.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Matter.Models.DocumentsModel
import com.digicoffer.lauditor.Matter.OldViewModels.MatterDocuments
import com.digicoffer.lauditor.Matter.ViewModels.MatterDocuments_En
import com.digicoffer.lauditor.R
import java.util.ArrayList

class DocumentsAdapter : RecyclerView.Adapter<DocumentsAdapter.Viewholder> {

    var documentsList: ArrayList<DocumentsModel> = ArrayList()
    var matterDocuments: MatterDocuments? = null
    var matterDocuments_en: MatterDocuments_En? = null
    var btn_create: AppCompatButton? = null

    constructor(
        documentsList: ArrayList<DocumentsModel>,
        matterDocuments: MatterDocuments?,
        btn_create: AppCompatButton?
    ) {
        this.matterDocuments = matterDocuments
        this.documentsList = documentsList
        this.btn_create = btn_create
    }

    constructor(
        documentsList: ArrayList<DocumentsModel>,
        matterDocuments: MatterDocuments_En?,
        btn_create: AppCompatButton?
    ) {
        this.matterDocuments_en = matterDocuments
        this.documentsList = documentsList
        this.btn_create = btn_create
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.select_team_members, parent, false)
        return Viewholder(itemView)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val documentsModel = documentsList[position]
        holder.cb_documents.isChecked = documentsList[position].isChecked
        holder.cb_documents.tag = position
        holder.tv_tm_name.text = documentsModel.name

        holder.cb_documents.setOnClickListener {
            val pos = holder.cb_documents.tag as Int
            val clickedtm = documentsList[pos]

            clickedtm.isChecked = !clickedtm.isChecked

            if (clickedtm.isChecked) {
                if (matterDocuments != null && !matterDocuments!!.tempSelectedDocuments.contains(clickedtm)) {
                    matterDocuments!!.tempSelectedDocuments.add(clickedtm)
                }
            } else {
                if (matterDocuments != null) {
                    matterDocuments!!.selected_documents_list.remove(clickedtm)
                    matterDocuments!!.tempSelectedDocuments.remove(clickedtm)
                    if (!Constants.create_matter) {
                        btn_create?.isEnabled = true
                        btn_create?.alpha = 1.0f
                    }
                    matterDocuments!!.loadSelectedDocuments(Array(matterDocuments!!.selected_documents_list.size) { "" })
                }
            }
            matterDocuments?.DocumentsText()
        }
    }

    override fun getItemCount(): Int {
        return documentsList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

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
