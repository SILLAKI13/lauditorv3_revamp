package com.digicoffer.lauditor.Invoice.Adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Groups.Models.ActionModel
import com.digicoffer.lauditor.Invoice.Models.InvoiceModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import java.util.ArrayList
import java.util.Locale

class ViewInvoiceAdapter(
    var itemsArrayList: ArrayList<InvoiceModel>,
    private val context: Context,
    private val actionListener: InvoiceActionListener
) : RecyclerView.Adapter<ViewInvoiceAdapter.InvoiceViewHolder>(), Filterable {

    val listItem: ArrayList<InvoiceModel> = ArrayList(itemsArrayList)
    private var expandedPosition = -1
    private var recyclerView: RecyclerView? = null

    fun setRecyclerView(recyclerView: RecyclerView?) {
        this.recyclerView = recyclerView
    }

    interface InvoiceActionListener {
        fun onViewDetails(invoiceModel: InvoiceModel)
        fun onEditInvoice(invoiceModel: InvoiceModel)
        fun onChangeStatus(invoiceModel: InvoiceModel)
        fun onShareInvoice(invoiceModel: InvoiceModel)
        fun onDeleteInvoice(invoiceModel: InvoiceModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_invoice_recyclerview, parent, false)
        return InvoiceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        val model = itemsArrayList[position]

        try {
            val displayName = if (model.name != null && model.name.isNotEmpty()) model.name else ""
            holder.tv_client_name.text = displayName

            holder.tv_invoice_no.text = model.invoice_no
            holder.tv_create_date.text = model.date
            holder.tv_due_date.text = model.dueDate
            holder.tv_created_by.text = model.createdby

            var status = model.status ?: ""
            if (status.isNotEmpty()) {
                status = status.substring(0, 1).uppercase() + status.substring(1)
            }
            holder.tv_invoice_status.text = status

            if (status.equals("collected", ignoreCase = true)) {
                holder.tv_invoice_status.setTextColor(
                    ContextCompat.getColor(context, R.color.completed_text)
                )
                holder.tv_invoice_status.setBackgroundResource(R.drawable.completed_badge)
            } else {
                holder.tv_invoice_status.setTextColor(
                    ContextCompat.getColor(context, R.color.scheduled_text)
                )
                holder.tv_invoice_status.setBackgroundResource(R.drawable.scheduled_badge)
            }

            if (model.isdisabled) {
                holder.iv_action_btn.isEnabled = false
                holder.iv_action_btn.alpha = 0.5f
            } else {
                holder.iv_action_btn.isEnabled = true
                holder.iv_action_btn.alpha = 1.0f
            }

            val localActions = ArrayList<ActionModel>()
            localActions.add(ActionModel("View Details"))
            localActions.add(ActionModel("Edit Invoice"))
            localActions.add(ActionModel("Change Status"))
            if (model.can_share) {
                localActions.add(ActionModel("Share"))
            }
            localActions.add(ActionModel("Delete"))

            val isExpanded = (position == expandedPosition)
            holder.lv_invoice_actions.visibility = if (isExpanded) View.VISIBLE else View.GONE

            val localAdapter = CommonSpinnerAdapter(context as Activity, localActions)
            holder.lv_invoice_actions.adapter = localAdapter
            holder.lv_invoice_actions.post {
                AndroidUtils.setDynamicHeight(holder.lv_invoice_actions)
            }

            holder.iv_action_btn.setOnClickListener {
                val currentPos = holder.bindingAdapterPosition
                if (expandedPosition == currentPos) {
                    expandedPosition = -1
                    notifyItemChanged(currentPos)
                } else {
                    val oldPos = expandedPosition
                    expandedPosition = currentPos
                    if (oldPos != -1) notifyItemChanged(oldPos)
                    notifyItemChanged(currentPos)
                }
            }

            holder.lv_invoice_actions.setOnItemClickListener { parent, view, pos, id ->
                val selectedAction = localActions[pos].name
                when (selectedAction) {
                    "View Details" -> actionListener.onViewDetails(model)
                    "Edit Invoice" -> actionListener.onEditInvoice(model)
                    "Change Status" -> actionListener.onChangeStatus(model)
                    "Share" -> actionListener.onShareInvoice(model)
                    "Delete" -> actionListener.onDeleteInvoice(model)
                    else -> Log.w("ViewInvoiceAdapter", "Unknown action: $selectedAction")
                }
                expandedPosition = -1
                notifyItemChanged(holder.bindingAdapterPosition)
            }

        } catch (e: Exception) {
            AndroidUtils.showToast(e.message, context)
            Log.e("ViewInvoiceAdapter", e.message ?: "")
        }
    }

    override fun getItemCount(): Int {
        return itemsArrayList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val query = charSequence?.toString()?.lowercase()?.trim() ?: ""
                val filtered = ArrayList<InvoiceModel>()
                if (query.isEmpty()) {
                    filtered.addAll(listItem)
                } else {
                    for (item in listItem) {
                        val matchName = item.name != null && item.name.lowercase().contains(query)
                        val matchInvNo = item.invoice_no != null && item.invoice_no.lowercase().contains(query)
                        val matchCreatedBy = item.createdby != null && item.createdby.lowercase().contains(query)
                        val matchStatus = item.status != null && item.status.lowercase().contains(query)
                        if (matchName || matchInvNo || matchCreatedBy || matchStatus) {
                            filtered.add(item)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filtered
                results.count = filtered.size
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
                itemsArrayList = filterResults?.values as? ArrayList<InvoiceModel> ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    class InvoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_client_name: TextView = itemView.findViewById(R.id.tv_client_name)
        val tv_invoice_no: TextView = itemView.findViewById(R.id.tv_invoice_no)
        val tv_create_date: TextView = itemView.findViewById(R.id.tv_create_date)
        val tv_due_date: TextView = itemView.findViewById(R.id.tv_due_date)
        val tv_created_by: TextView = itemView.findViewById(R.id.tv_created_by)
        val tv_invoice_status: TextView = itemView.findViewById(R.id.tv_invoice_status)
        val iv_action_btn: ImageView = itemView.findViewById(R.id.iv_action_btn)
        val lv_invoice_actions: ListView = itemView.findViewById(R.id.lv_invoice_actions)

        init {
            lv_invoice_actions.isNestedScrollingEnabled = false
            lv_invoice_actions.isFocusable = false
            lv_invoice_actions.descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        }
    }
}
