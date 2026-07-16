package com.digicoffer.lauditor.Relationships.Adapter

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Relationships.Model.SharedDocumentsDo
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import java.util.Objects

class SharedDocumentsAdapter(
    var sharedList: ArrayList<SharedDocumentsDo>,
    var Shared_tag: String,
    var mContext: Context,
    var eventListener: EventListener?,
    var rel_id: String?,
    shared_client_id: String,
    var mActivity: FragmentActivity,
    var relationshipsAdapter: RelationshipsAdapter?,
    var highLightId: String?
) : RecyclerView.Adapter<SharedDocumentsAdapter.ViewHolder>(), Filterable {

    @get:JvmName("getActualListItem")
    var list_item: ArrayList<SharedDocumentsDo> = sharedList
    var updated_shared_list: ArrayList<SharedDocumentsDo> = ArrayList()
    var client_id: String = shared_client_id
    var alertDialog: AlertDialog? = null
    var selectedList: ArrayList<SharedDocumentsDo> = ArrayList()
    var doc_name: String = ""
    var selectedPosition: Int = -1



    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence?.toString() ?: ""
                if (charString.isEmpty()) {
                    sharedList = list_item
                } else {
                    val filteredList = ArrayList<SharedDocumentsDo>()
                    for (row in list_item) {
                        if (AndroidUtils.isNull(row.name).lowercase().contains(charString.lowercase())) {
                            filteredList.add(row)
                        }
                    }
                    sharedList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.count = sharedList.size
                filterResults.values = sharedList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
                sharedList = (filterResults?.values as? ArrayList<SharedDocumentsDo>) ?: ArrayList()
                check_allselected()
                notifyDataSetChanged()
            }
        }
    }

    interface EventListener {
        fun CopyDocument(sharedDocumentsDo: SharedDocumentsDo)
        fun viewDocument(sharedDocumentsDo: SharedDocumentsDo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (Objects.equals(Shared_tag, "withme")) {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.doc_sharewithus_layout, parent, false)
            ViewHolder(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.listing_rel_doc_layout, parent, false)
            ViewHolder(itemView)
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sharedDocumentsDo = sharedList[position]
        if (Objects.equals(Shared_tag, "withme")) {
            holder.tv_doc_name.text = sharedDocumentsDo.name
            holder.tv_file_name?.text = sharedDocumentsDo.description
            holder.tv_doc_date?.text = sharedDocumentsDo.created
            if (sharedDocumentsDo.has_Confidential) {
                val matterName = sharedDocumentsDo.matter_details_name ?: ""
                if (matterName.isNotEmpty()) {
                    holder.ll_confidential?.visibility = View.VISIBLE
                    holder.tv_matter_details_name?.text = matterName
                } else {
                    holder.ll_confidential?.visibility = View.GONE
                }
            } else {
                holder.ll_confidential?.visibility = View.GONE
            }
        } else {
            holder.cb_documents?.isChecked = sharedList[position].isChecked
            holder.cb_documents?.tag = position
            holder.tv_doc_name.text = sharedDocumentsDo.name
            check_allselected()

            holder.iv_remove_doc?.setOnClickListener {
                try {
                    updated_shared_list.clear()
                    val sharedDocumentsDo1 = sharedList[holder.adapterPosition]
                    sharedDocumentsDo1.id = sharedDocumentsDo.id
                    sharedDocumentsDo1.name = sharedDocumentsDo.name
                    updated_shared_list.add(sharedDocumentsDo1)

                    val remove = JSONArray()
                    val jsonObject1 = JSONObject()
                    jsonObject1.put("docid", sharedDocumentsDo.id)
                    jsonObject1.put("doctype", "general")
                    remove.put(jsonObject1)
                    Log.d("remove_doc_size", "${remove.length()}...")
                    remove_popup(updated_shared_list, remove)
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
            }
            if (sharedDocumentsDo.has_Confidential) {
                val matterName = sharedDocumentsDo.matter_details_name ?: ""
                if (matterName.isNotEmpty()) {
                    holder.ll_confidential?.visibility = View.VISIBLE
                    holder.tv_matter_details_name?.text = matterName
                } else {
                    holder.ll_confidential?.visibility = View.GONE
                }
            } else {
                holder.ll_confidential?.visibility = View.GONE
            }
            holder.cb_documents?.setOnClickListener {
                val pos = holder.cb_documents?.tag as Int
                val item = sharedList[pos]
                item.isChecked = !item.isChecked

                if (item.isChecked) {
                    if (!selectedList.contains(item)) selectedList.add(item)
                } else {
                    selectedList.remove(item)
                }

                check_allselected()

                // Update parent if needed
                if (Shared_tag == "byme") {
                    relationshipsAdapter?.selected_unsharedocsList = selectedList
                } else {
                    relationshipsAdapter?.selected_sharedocsList = selectedList
                }
            }
        }

        if (highLightId != null && highLightId!!.isNotEmpty() && Shared_tag == "withme" && sharedDocumentsDo.id == highLightId) {
            holder.document_layout.background = mContext.getDrawable(R.drawable.rectangular_button_green_count)
            // 🔵 Revert back to white after 5 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                holder.document_layout.background = mContext.getDrawable(R.drawable.rectangular_white_background)
            }, 5000)
        }

        holder.document_layout.setOnClickListener {
            relationshipsAdapter?.View_doc(sharedDocumentsDo.id ?: "", sharedDocumentsDo)
        }
    }

    fun selectOrDeselectAll(isChecked: Boolean): Boolean {
        selectedList.clear() // clear previous selections
        for (item in sharedList) {
            item.isChecked = isChecked
            if (isChecked) selectedList.add(item)
        }
        notifyDataSetChanged()
        return isChecked
    }



    fun check_allselected() {
        var allSelected = true
        for (groupModel in sharedList) {
            if (!groupModel.isChecked) {
                allSelected = false
                break
            }
        }
        if (sharedList.isEmpty()) {
            allSelected = false
        }
        relationshipsAdapter?.check_select_all(allSelected)
    }

    private fun remove_popup(updated_shared_list: ArrayList<SharedDocumentsDo>, remove: JSONArray) {
        try {
            val dialogBuilder = AlertDialog.Builder(mContext)
            val inflater = mActivity.layoutInflater
            val view = inflater.inflate(R.layout.share_document_popup, null)
            val rv_remove_documents = view.findViewById<RecyclerView>(R.id.rv_remove_documents)
            val layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
            rv_remove_documents.layoutManager = layoutManager

            val documentsAdapter = UnshareDocumentAdapter(this.updated_shared_list, relationshipsAdapter)
            rv_remove_documents.adapter = documentsAdapter

            val tv_share_documents = view.findViewById<TextView>(R.id.tv_share_documents)
            val btn_cancel_share = view.findViewById<Button>(R.id.btn_cancel_share)
            val btn_ok_share = view.findViewById<Button>(R.id.btn_ok_share)

            if (Shared_tag == "byme") {
                tv_share_documents.text = "Documents Unshare"
                btn_ok_share.text = "Unshare"
            } else {
                tv_share_documents.text = "Documents Share"
                btn_ok_share.text = "Share"
            }
            btn_cancel_share.setOnClickListener {
                alertDialog?.dismiss()
            }
            btn_ok_share.setOnClickListener {
                try {
                    relationshipsAdapter?.callUnsharedDocumentWebservice(rel_id ?: "", remove, "", client_id, true)
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
                alertDialog?.dismiss()
            }
            val dialog = dialogBuilder.create()
            alertDialog = dialog
            dialog.setView(view)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override fun getItemCount(): Int {
        return sharedList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cb_documents: CheckBox? = itemView.findViewById(R.id.cb_documents)
        val tv_tm_name: TextView? = itemView.findViewById(R.id.tv_tm_name)
        val tv_doc_name: TextView = itemView.findViewById(R.id.tv_doc_name)
        val tv_file_name: TextView? = itemView.findViewById(R.id.tv_file_name)
        val tv_doc_date: TextView? = itemView.findViewById(R.id.tv_doc_date)
        val confidential_txt: TextView? = itemView.findViewById(R.id.confidential_txt)
        val tv_matter_details_name: TextView? = itemView.findViewById(R.id.tv_matter_details_name)
        val document_layout: LinearLayout = itemView.findViewById(R.id.document_layout)
        val ll_confidential: LinearLayout? = itemView.findViewById(R.id.ll_confidential)
        var iv_remove_doc: ImageView? = null
        val iv_view: ImageButton? = itemView.findViewById(R.id.iv_view)
        val iv_copy: ImageButton? = itemView.findViewById(R.id.iv_copy)

        init {
            if (Shared_tag == "withme") {
                tv_doc_name.setTextColor(mContext.resources.getColor(R.color.Blue_text_color, null))
            } else {
                tv_doc_name.gravity = Gravity.START
                tv_doc_name.textSize = DynamicUtils.eighteen.toFloat()
                iv_remove_doc = itemView.findViewById(R.id.iv_remove_doc)
                if (Shared_tag == "byme") {
                    iv_remove_doc?.visibility = View.VISIBLE
                } else {
                    iv_remove_doc?.visibility = View.GONE
                }
            }
            confidential_txt?.setTextColor(mContext.getColor(R.color.blue))
            confidential_txt?.setText(R.string.confidential)
            tv_matter_details_name?.textSize = 12f
        }
    }
}
