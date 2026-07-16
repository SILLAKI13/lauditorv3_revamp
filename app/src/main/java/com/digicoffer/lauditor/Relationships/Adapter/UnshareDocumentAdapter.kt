package com.digicoffer.lauditor.Relationships.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Relationships.Model.SharedDocumentsDo
import java.util.ArrayList

class UnshareDocumentAdapter(
    var sharedList: ArrayList<SharedDocumentsDo>,
    var relationshipsAdapter: RelationshipsAdapter?
) : RecyclerView.Adapter<UnshareDocumentAdapter.ViewHolder>() {

    @get:JvmName("getActualListItem")
    var list_item: ArrayList<SharedDocumentsDo> = ArrayList()
    var Shared_tag: String = ""
    var mContext: Context? = null
    var mActivity: FragmentActivity? = null
    var client_id: String = ""
    var rel_id: String = ""

    fun getList_item(): ArrayList<SharedDocumentsDo> {
        return sharedList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.share_document_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sharedDocumentsDo = sharedList[position]
        holder.tv_share_document.text = sharedDocumentsDo.name
    }

    override fun getItemCount(): Int {
        return sharedList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_share_document: TextView = itemView.findViewById(R.id.tv_share_document)

        init {
            tv_share_document.textSize = DynamicUtils.fifteen.toFloat()
        }
    }
}
