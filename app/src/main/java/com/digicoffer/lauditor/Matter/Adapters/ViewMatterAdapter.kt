package com.digicoffer.lauditor.Matter.Adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Groups.Models.ActionModel
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.R
import java.util.ArrayList

class ViewMatterAdapter(
    itemsArrayList: ArrayList<ViewMatterModel>,
    var context: Context,
    var eventListener: InterfaceListener
) : RecyclerView.Adapter<ViewMatterAdapter.MyViewHolder>(), Filterable {

    var itemsArrayList: ArrayList<ViewMatterModel> = ArrayList(itemsArrayList)
    var list_item: ArrayList<ViewMatterModel> = ArrayList(itemsArrayList)

    private var expandedPosition = -1
    private var recyclerView: RecyclerView? = null

    fun setRecyclerView(rv: RecyclerView) {
        this.recyclerView = rv
    }

    private fun safeNotify(position: Int) {
        val r = Runnable {
            if (position >= 0 && position < itemCount) {
                notifyItemChanged(position)
            }
        }
        if (recyclerView != null) recyclerView!!.post(r)
        else Handler(Looper.getMainLooper()).post(r)
    }

    private fun safeNotifyDataSetChanged() {
        if (recyclerView != null) {
            recyclerView!!.post { notifyDataSetChanged() }
        } else {
            Handler(Looper.getMainLooper()).post { notifyDataSetChanged() }
        }
    }

    fun collapseExpanded() {
        if (expandedPosition == -1) return
        val pos = expandedPosition
        expandedPosition = -1
        safeNotify(pos)
    }

    interface InterfaceListener {
        fun View_Details(
            viewMatterModel: ViewMatterModel,
            itemsArrayList: ArrayList<ViewMatterModel>
        )

        fun DeleteMatter(
            viewMatterModel: ViewMatterModel,
            itemsArrayList: ArrayList<ViewMatterModel>
        )

        fun Edit_Matter_Info(viewMatterModel: ViewMatterModel)
        fun Update_Group(viewMatterModel: ViewMatterModel)
        fun Close_Matter(viewMatterModel: ViewMatterModel)
        fun ReopenMatter(viewMatterModel: ViewMatterModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_matter_recyclerview_design, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val model = itemsArrayList[position]

        try {
            holder.tv_owner_name.text = model.owner_name
            holder.tv_matter_title.text = model.title
            holder.tv_case_number.text = model.matter_id
            holder.tv_client_name.text = model.client_name
            holder.typeLayout.visibility = GONE

            if (Constants.MATTER_TYPE == "Legal") {
                holder.tv_date_of_filling.text = model.date_of_filling
                if (model.casetype.isNotEmpty()) {
                    holder.typeLayout.visibility = VISIBLE
                    holder.tv_matter_type.text = model.casetype
                }
            } else {
                holder.tv_date_of_filling.text = model.startdate
                if (model.matterType.isNotEmpty()) {
                    holder.typeLayout.visibility = VISIBLE
                    holder.tv_matter_type.text = model.matterType
                }
            }

            val tagsArray = model.tags_list
            if (tagsArray != null && tagsArray.length() > 0) {
                val sb = StringBuilder()
                for (i in 0 until tagsArray.length()) {
                    val tag = tagsArray.optString(i)
                    if (!TextUtils.isEmpty(tag)) {
                        if (sb.isNotEmpty()) sb.append(", ")
                        sb.append(tag)
                    }
                }
                if (sb.isNotEmpty()) {
                    holder.tv_tag_name.text = sb.toString()
                    holder.tv_tag_name.visibility = VISIBLE
                    holder.taglayout.visibility = VISIBLE
                } else {
                    holder.tv_tag_name.visibility = GONE
                    holder.taglayout.visibility = GONE
                }
            } else {
                holder.tv_tag_name.visibility = GONE
                holder.taglayout.visibility = GONE
            }

            holder.iv_initiated.visibility = GONE
            when (model.status) {
                "Active" -> {
                    holder.tv_initiated.text = "Active"
                    holder.tv_initiated.setTextColor(
                        ContextCompat.getColor(context, R.color.completed_text)
                    )
                    holder.tv_initiated.setBackgroundResource(R.drawable.completed_badge)
                }

                "Closed" -> {
                    holder.tv_initiated.text = "Closed"
                    holder.tv_initiated.setTextColor(
                        ContextCompat.getColor(context, R.color.cancelled_text)
                    )
                    holder.tv_initiated.setBackgroundResource(R.drawable.cancelled_badge)
                }

                else -> {
                    holder.tv_initiated.text = "Pending"
                    holder.tv_initiated.setTextColor(
                        ContextCompat.getColor(context, R.color.pending_text)
                    )
                    holder.tv_initiated.setBackgroundResource(R.drawable.pending_badge)
                }
            }

            val itemActions = ArrayList<ActionModel>()
            itemActions.add(ActionModel("Edit Matter"))
            itemActions.add(ActionModel("View Timeline"))
            if ("Closed" == model.status) {
                itemActions.add(ActionModel("Reopen Matter"))
            } else if ("pending".equals(model.status, ignoreCase = true)
                || "active".equals(model.status, ignoreCase = true)
            ) {
                itemActions.add(ActionModel("Close Matter"))
            }

            if (model.isdisabled || !model.is_editable) {
                holder.custom_spinner_cardview.isEnabled = false
                holder.custom_spinner_cardview.alpha = 0.5f
            } else {
                holder.custom_spinner_cardview.isEnabled = true
                holder.custom_spinner_cardview.alpha = 1.0f
            }

            val isExpanded = (position == expandedPosition)

            holder.custom_spinner_cardview.setOnClickListener(null)
            holder.sp_action.onItemClickListener = null

            if (isExpanded) {
                val itemAdapter = CommonSpinnerAdapter(context as Activity, itemActions)
                holder.sp_action.adapter = itemAdapter
                holder.sp_action.post { AndroidUtils.setDynamicHeight(holder.sp_action) }
                holder.action_list_card.visibility = VISIBLE
                holder.sp_action.visibility = VISIBLE
            } else {
                holder.sp_action.adapter = null
                holder.action_list_card.visibility = GONE
                holder.sp_action.visibility = GONE
            }

            holder.custom_spinner_cardview.setOnClickListener { v ->
                val cur = holder.bindingAdapterPosition
                if (cur == RecyclerView.NO_POSITION) return@setOnClickListener
                val prev = expandedPosition
                expandedPosition = if (expandedPosition == cur) -1 else cur
                if (prev != -1 && prev != cur) safeNotify(prev)
                safeNotify(cur)
            }

            holder.sp_action.onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
                val cur = holder.bindingAdapterPosition
                if (cur == RecyclerView.NO_POSITION) return@OnItemClickListener

                val actionName = itemActions[pos].name

                expandedPosition = -1
                safeNotify(cur)

                Handler(Looper.getMainLooper()).post {
                    dispatchAction(actionName, model)
                }
            }

        } catch (e: Exception) {
            AndroidUtils.showToast(e.message, context)
            Log.d("Matter Details", e.message ?: "")
            e.fillInStackTrace()
        }
    }

    private fun dispatchAction(action: String, model: ViewMatterModel) {
        when (action) {
            "Edit Matter" -> {
                Constants.Matter_CreateOrViewDetails = "View Timeline"
                Constants.matterDate = model.created
                eventListener.View_Details(model, itemsArrayList)
            }

            "View Timeline" -> {
                Constants.Matter_CreateOrViewDetails = "Edit Matter Info"
                Constants.matterDate = model.created
                eventListener.Edit_Matter_Info(model)
            }

            "Close Matter", "Reopen Matter" -> {
                Constants.Matter_CreateOrViewDetails = "Close/Reopen"
                eventListener.Close_Matter(model)
            }
        }
    }

    override fun getItemCount(): Int {
        return itemsArrayList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(cs: CharSequence): FilterResults {
                val q = cs.toString()
                val result: ArrayList<ViewMatterModel>
                if (q.isEmpty()) {
                    result = ArrayList(list_item)
                } else {
                    result = ArrayList()
                    for (row in list_item) {
                        if (AndroidUtils.isNull(row.title).lowercase()
                                .contains(q.lowercase())
                            || AndroidUtils.isNull(row.client_name).lowercase()
                                .contains(q.lowercase())
                            || AndroidUtils.isNull(row.owner_name).lowercase()
                                .contains(q.lowercase())
                        ) {
                            result.add(row)
                        }
                    }
                }
                val fr = FilterResults()
                fr.count = result.size
                fr.values = result
                return fr
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(cs: CharSequence, fr: FilterResults) {
                itemsArrayList = fr.values as ArrayList<ViewMatterModel>
                notifyDataSetChanged()
            }
        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_matter_title: TextView = itemView.findViewById(R.id.tv_matter_title)
        val tv_case_number: TextView = itemView.findViewById(R.id.tv_case_number)
        val tv_date_of_filling: TextView = itemView.findViewById(R.id.tv_date_of_filling)
        val tv_client_name: TextView = itemView.findViewById(R.id.tv_client_name)
        val tv_owner_name: TextView = itemView.findViewById(R.id.tv_owner_name)
        val tv_initiated: TextView = itemView.findViewById(R.id.tv_initiated)
        val filed: TextView = itemView.findViewById(R.id.filed)
        val textView: TextView = itemView.findViewById(R.id.textView)
        val owner: TextView = itemView.findViewById(R.id.owner)
        val tag: TextView = itemView.findViewById(R.id.tag)
        val tv_tag_name: TextView = itemView.findViewById(R.id.tv_tag_name)
        val matter_type: TextView = itemView.findViewById(R.id.matter_type)
        val tv_matter_type: TextView = itemView.findViewById(R.id.tv_matter_type)
        val iv_initiated: ImageView = itemView.findViewById(R.id.iv_initiated)
        val custom_spinner_cardview: ImageView = itemView.findViewById(R.id.custom_spinner_cardview)
        val sp_action: ListView = itemView.findViewById(R.id.list_client)
        val action_list_card: CardView = itemView.findViewById(R.id.action_list_card)
        val action_layout: LinearLayout? = itemView.findViewById(R.id.action_layout)
        val taglayout: LinearLayout = itemView.findViewById(R.id.taglayout)
        val typeLayout: LinearLayout = itemView.findViewById(R.id.typeLayout)
        val ll_owner: LinearLayout = itemView.findViewById(R.id.ll_owner)

        init {
            tv_matter_title.setText(R.string.matter_title)
            tv_matter_title.setTextColor(android.graphics.Color.BLACK)

            tv_case_number.setText(R.string.case_number)

            textView.setText(R.string.client_)
            owner.setText(R.string.owner)
            tag.setText(R.string.tag_)
            matter_type.setText(R.string.type_)
            filed.setText(R.string.filed)

            tv_client_name.text = ""
            tv_client_name.maxLines = 2
            tv_client_name.gravity = Gravity.START

            tv_tag_name.text = ""
            tv_owner_name.setText(R.string.owner_name)
            tv_initiated.setText(R.string.pending)

            if ("solo" == Constants.CATEGORY) {
                ll_owner.visibility = GONE
            } else {
                ll_owner.visibility = VISIBLE
            }
        }
    }
}
