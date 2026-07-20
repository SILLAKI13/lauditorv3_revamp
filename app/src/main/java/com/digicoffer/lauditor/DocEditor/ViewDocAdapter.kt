package com.digicoffer.lauditor.DocEditor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Groups.Models.ActionModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import java.util.ArrayList
import java.util.Locale

class ViewDocAdapter(
    private val mcontext: Context,
    private var itemsArrayList: ArrayList<DocListingModel>,
    private val eventListener: InterfaceListener,
    private val docEditor: DocEditor,
    private val isView: Boolean
) : RecyclerView.Adapter<ViewDocAdapter.MyViewHolder>(), Filterable {

    private val actions_List = ArrayList<ActionModel>()
    private var spinner_adapter: CommonSpinnerAdapter<ActionModel>? = null
    private val list_item: ArrayList<DocListingModel> = itemsArrayList
    private var expandedPosition = -1 // no item expanded

    interface InterfaceListener {
        fun OpenDoc(docListingModel: DocListingModel)
        fun DeleteDoc(documentname: String, docid: String)
        fun ViewDoc(documentname: String, docid: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = if (isView) {
            LayoutInflater.from(parent.context).inflate(R.layout.view_list_doceditor, parent, false)
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.open_doc_file_view, parent, false)
        }
        return MyViewHolder(view, isView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val docListingModel = itemsArrayList[position]

        if (isView) {
            holder.tv_description.text = docListingModel.documentname
            holder.tv_created_date.text = AndroidUtils.formatDateToReadable(docListingModel.`get$date`())
            holder.tv_created_by.text = "Created By : "
            holder.created_date?.text = "Created Date : "
            holder.tv_created_by_name?.text = Constants.NAME

            actions_List.clear()
            actions_List.add(ActionModel("View"))
            actions_List.add(ActionModel("Delete"))

            spinner_adapter = CommonSpinnerAdapter(mcontext as Activity, actions_List)
            holder.sp_action?.adapter = spinner_adapter

            val isExpanded = position == expandedPosition
            holder.sp_action?.visibility = if (isExpanded) View.VISIBLE else View.GONE

            holder.custom_spinner_cardview?.setOnClickListener {
                if (expandedPosition == position) {
                    expandedPosition = -1
                } else {
                    val oldPos = expandedPosition
                    expandedPosition = position
                    if (oldPos != -1) notifyItemChanged(oldPos)
                }
                notifyItemChanged(position)
            }

            holder.sp_action?.let { AndroidUtils.setDynamicHeight(it) }

            holder.sp_action?.setOnItemClickListener { _, _, i, _ ->
                spinner_adapter?.setSelectedPosition(i)
                val actionModel = actions_List[i]
                val actionName = actionModel.name

                when (actionName) {
                    "View" -> eventListener.ViewDoc(docListingModel.documentname ?: "", docListingModel.docid ?: "")
                    "Delete" -> eventListener.DeleteDoc(docListingModel.documentname ?: "", docListingModel.docid ?: "")
                }

                expandedPosition = -1
                notifyItemChanged(position)
            }
        } else {
            holder.tv_description.text = docListingModel.documentname
            holder.tv_created_by.text = "Created By: " + Constants.NAME
            holder.tv_created_date.text = "Created Date: " + AndroidUtils.formatDateToReadable(docListingModel.`get$date`())
            holder.ll_open_doc_view?.setOnClickListener {
                eventListener.OpenDoc(docListingModel)
            }
        }
    }

    override fun getItemCount(): Int {
        return itemsArrayList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    itemsArrayList = list_item
                } else {
                    val filteredList = ArrayList<DocListingModel>()
                    for (row in list_item) {
                        val docName = AndroidUtils.isNull(row.documentname).lowercase(Locale.getDefault())
                        val docDate = AndroidUtils.isNull(row.`get$date`()).lowercase(Locale.getDefault())
                        val searchStr = charString.lowercase(Locale.getDefault())
                        if (docName.contains(searchStr) || docDate.contains(searchStr)) {
                            filteredList.add(row)
                        }
                    }
                    itemsArrayList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.count = itemsArrayList.size
                filterResults.values = itemsArrayList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                itemsArrayList = filterResults.values as ArrayList<DocListingModel>
                notifyDataSetChanged()
            }
        }
    }

    class MyViewHolder(itemView: View, isView: Boolean) : RecyclerView.ViewHolder(itemView) {
        var tv_description: TextView = if (isView) itemView.findViewById(R.id.tv_description) else itemView.findViewById(R.id.tv_file_name)
        var tv_created_by: TextView = itemView.findViewById(R.id.tv_created_by)
        var tv_created_date: TextView = itemView.findViewById(R.id.tv_created_date)
        var tv_created_by_name: TextView? = if (isView) itemView.findViewById(R.id.tv_created_by_name) else null
        var created_date: TextView? = if (isView) itemView.findViewById(R.id.created_date) else null
        var sp_action: ListView? = if (isView) itemView.findViewById(R.id.sp_action) else null
        var custom_spinner_cardview: CardView? = if (isView) itemView.findViewById(R.id.custom_spinner_cardview) else null
        var ll_open_doc_view: LinearLayout? = if (isView) null else itemView.findViewById(R.id.ll_open_doc_view)
    }
}
