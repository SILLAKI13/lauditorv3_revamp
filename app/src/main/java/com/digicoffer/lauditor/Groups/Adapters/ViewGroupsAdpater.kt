package com.digicoffer.lauditor.Groups.Adapters

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
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
import com.digicoffer.lauditor.Groups.Groups
import com.digicoffer.lauditor.Groups.Models.ActionModel
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.Groups.ViewGroupsItemClickListener
import com.digicoffer.lauditor.R
import org.json.JSONException
import java.util.ArrayList

class ViewGroupsAdpater : RecyclerView.Adapter<ViewGroupsAdpater.ViewHolder>, Filterable {

    private val masterList: ArrayList<ViewGroupModel>
    private var displayList: ArrayList<ViewGroupModel>

    private val eventListener: InterfaceListener
    private val mcontext: Context
    private val itemClickListener: ViewGroupsItemClickListener?

    private val btn_submit: Button?
    private var selectedGroupId = ""
    private val group: Groups

    private var expandedPosition = -1
    private var recyclerView: RecyclerView? = null

    companion object {
        @JvmField
        var mTag: String = ""

        private fun safe(s: String?): String {
            return s ?: ""
        }
    }

    fun setRecyclerView(rv: RecyclerView?) {
        this.recyclerView = rv
    }

    fun collapseExpanded() {
        if (expandedPosition == -1) return
        val pos = expandedPosition
        expandedPosition = -1
        safeNotifyItemChanged(pos)
    }

    private fun safeNotifyItemChanged(position: Int) {
        val r = Runnable {
            if (position in 0 until itemCount) {
                notifyItemChanged(position)
            }
        }
        val rv = recyclerView
        if (rv != null) {
            rv.post(r)
        } else {
            Handler(Looper.getMainLooper()).post(r)
        }
    }

    private fun safeNotifyDataSetChanged() {
        val r = Runnable { notifyDataSetChanged() }
        val rv = recyclerView
        if (rv != null) {
            rv.post(r)
        } else {
            Handler(Looper.getMainLooper()).post(r)
        }
    }

    constructor(
        itemsArrayList: ArrayList<ViewGroupModel>?,
        context: Context,
        eventListener: InterfaceListener,
        tag: String?,
        itemClickListener: ViewGroupsItemClickListener?,
        groups: Groups
    ) : this(itemsArrayList, context, eventListener, tag, itemClickListener, groups, null)

    constructor(
        itemsArrayList: ArrayList<ViewGroupModel>?,
        context: Context,
        eventListener: InterfaceListener,
        tag: String?,
        itemClickListener: ViewGroupsItemClickListener?,
        groups: Groups,
        btnSubmit: Button?
    ) {
        val src = itemsArrayList ?: ArrayList()
        this.masterList = ArrayList(src)
        this.displayList = ArrayList(src)
        this.mcontext = context
        this.group = groups
        this.eventListener = eventListener
        mTag = tag ?: ""
        this.itemClickListener = itemClickListener
        this.btn_submit = btnSubmit
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val query = charSequence?.toString()?.lowercase()?.trim() ?: ""
                val result = ArrayList<ViewGroupModel>()

                if (query.isEmpty()) {
                    result.addAll(masterList)
                } else {
                    for (row in masterList) {
                        val field = if ("CGH" == mTag) safe(row.group_name) else safe(row.name)
                        if (field.lowercase().contains(query)) {
                            result.add(row)
                        }
                    }
                }

                val fr = FilterResults()
                fr.values = result
                fr.count = result.size
                return fr
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                displayList = ArrayList(filterResults.values as List<ViewGroupModel>)
                expandedPosition = -1
                safeNotifyDataSetChanged()
            }
        }
    }

    interface InterfaceListener {
        fun EditGroup(viewGroupModel: ViewGroupModel)
        fun DeleteGroup(viewGroupModel: ViewGroupModel, itemsArrayList: ArrayList<ViewGroupModel>)
        fun CGH(viewGroupModel: ViewGroupModel, itemsArrayList: ArrayList<ViewGroupModel>)

        @Throws(JSONException::class)
        fun UGM(viewGroupModel: ViewGroupModel)

        @Throws(JSONException::class)
        fun GAL(viewGroupModel: ViewGroupModel)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutRes = when (mTag) {
            "VG" -> R.layout.view_groups
            "UGM" -> R.layout.select_team_members
            else -> R.layout.radio_button_layout
        }
        val itemView = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.i("ViewGroupsAdpater", "bind pos=$position tag=$mTag")
        if (position < 0 || position >= displayList.size) return
        val model = displayList[position]

        when (mTag) {
            "VG" -> bindVgRow(holder, model, position)
            "UGM" -> bindUgmRow(holder, model)
            else -> bindRadioRow(holder, model)
        }
    }

    private fun bindVgRow(holder: ViewHolder, model: ViewGroupModel, position: Int) {
        val itemName = safe(model.name)
        val hiresGroup = safe(Constants.hires_group)

        if (hiresGroup.isNotEmpty() && hiresGroup == itemName) {
            holder.action_cv?.background = ContextCompat.getDrawable(
                holder.itemView.context, R.drawable.blue_stroke_card
            )
            Handler(Looper.getMainLooper()).postDelayed({
                holder.action_cv?.background = ContextCompat.getDrawable(
                    holder.itemView.context, R.drawable.rectangular_white_background
                )
            }, 5000)
        } else {
            holder.action_cv?.background = ContextCompat.getDrawable(
                holder.itemView.context, R.drawable.rectangular_white_background
            )
        }

        holder.tv_user_type?.text = itemName
        holder.tv_owner_name?.text = safe(model.owner_name)
        holder.tv_date?.text = safe(model.created)
        holder.created_id?.setText(R.string.created_)
        holder.tv_owner_name?.setTextColor(Color.BLACK)
        holder.tv_date?.setTextColor(Color.BLACK)

        val memberCount = safe(model.memberCount)
        if (memberCount.isNotEmpty() && memberCount != "0") {
            holder.ll_members_count?.visibility = View.VISIBLE
            holder.tv_members_count?.text = memberCount
        } else {
            holder.ll_members_count?.visibility = View.GONE
        }

        val itemActions = ArrayList<ActionModel>()
        val role = safe(Constants.ROLE)

        if (itemName == "SuperUser" || itemName == "AAM") {
            itemActions.add(ActionModel("Update Group Members List"))
            itemActions.add(ActionModel("Group Activity Log"))
        } else {
            if (role == "GH") {
                itemActions.add(ActionModel("Edit Group Info"))
                itemActions.add(ActionModel("Group Activity Log"))
            } else {
                itemActions.add(ActionModel("Edit Group Info"))
                itemActions.add(ActionModel("Update Group Members List"))
                itemActions.add(ActionModel("Update Group Head"))
                itemActions.add(ActionModel("Delete Group"))
                itemActions.add(ActionModel("Group Activity Log"))
            }
        }

        val isExpanded = position == expandedPosition
        holder.custom_spinner_cardview?.setOnClickListener(null)
        holder.sp_action?.onItemClickListener = null

        if (isExpanded) {
            val itemAdapter = CommonSpinnerAdapter(mcontext as Activity, itemActions)
            holder.sp_action?.adapter = itemAdapter
            holder.sp_action?.post {
                holder.sp_action?.let { AndroidUtils.setDynamicHeight(it) }
            }
            holder.action_list_card?.visibility = View.VISIBLE
            holder.sp_action?.visibility = View.VISIBLE
        } else {
            holder.sp_action?.adapter = null
            holder.action_list_card?.visibility = View.GONE
            holder.sp_action?.visibility = View.GONE
        }

        holder.custom_spinner_cardview?.setOnClickListener {
            val cur = holder.bindingAdapterPosition
            if (cur == RecyclerView.NO_POSITION) return@setOnClickListener
            val prev = expandedPosition
            expandedPosition = if (expandedPosition == cur) -1 else cur

            if (prev != -1 && prev != cur) safeNotifyItemChanged(prev)
            safeNotifyItemChanged(cur)
        }

        holder.sp_action?.setOnItemClickListener { _, _, pos, _ ->
            val cur = holder.bindingAdapterPosition
            if (cur == RecyclerView.NO_POSITION) return@setOnItemClickListener
            if (pos < 0 || pos >= itemActions.size) return@setOnItemClickListener

            val actionName = itemActions[pos].name
            val snap = ArrayList(displayList)

            expandedPosition = -1
            safeNotifyItemChanged(cur)

            Handler(Looper.getMainLooper()).post {
                try {
                    when (actionName) {
                        "Edit Group Info" -> {
                            group.page_name("Edit Group Info")
                            eventListener.EditGroup(model)
                        }
                        "Delete Group" -> {
                            group.page_name("Assign Group")
                            eventListener.DeleteGroup(model, snap)
                        }
                        "Update Group Members List" -> {
                            group.page_name("Update Group Members List")
                            try {
                                eventListener.UGM(model)
                            } catch (e: Exception) {
                                e.fillInStackTrace()
                            }
                        }
                        "Update Group Head" -> {
                            group.page_name("Update Group Head")
                            Handler(Looper.getMainLooper()).postDelayed({
                                eventListener.CGH(model, snap)
                            }, 100)
                        }
                        "Group Activity Log" -> {
                            group.page_name("Group Activity Log")
                            try {
                                eventListener.GAL(model)
                            } catch (e: JSONException) {
                                e.fillInStackTrace()
                            }
                        }
                    }
                } catch (e: Exception) {
                    AndroidUtils.showAlert(e.message, group.activity)
                }
            }
        }
    }

    private fun bindUgmRow(holder: ViewHolder, model: ViewGroupModel) {
        holder.cb_team_members?.setOnCheckedChangeListener(null)
        holder.cb_team_members?.isChecked = model.isChecked
        holder.tv_tm_name?.text = safe(model.name)

        holder.cb_team_members?.setOnCheckedChangeListener { _, isChecked ->
            val p = holder.bindingAdapterPosition
            if (p == RecyclerView.NO_POSITION || p >= displayList.size) return@setOnCheckedChangeListener

            displayList[p].isChecked = isChecked

            val matchId = safe(displayList[p].id)
            if (matchId.isNotEmpty()) {
                for (m in masterList) {
                    if (matchId == safe(m.id)) {
                        m.isChecked = isChecked
                        break
                    }
                }
            }
        }
    }

    private fun bindRadioRow(holder: ViewHolder, model: ViewGroupModel) {
        val groupId = safe(model.group_id)
        val groupName = safe(model.group_name)

        holder.tv_gh_name?.text = groupName

        holder.rb_group_selected?.setOnCheckedChangeListener(null)
        holder.rb_group_selected?.isChecked = groupId == selectedGroupId
        holder.rb_group_selected?.tag = groupId

        holder.rb_group_selected?.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) return@setOnCheckedChangeListener
            selectedGroupId = groupId
            itemClickListener?.onClick(groupId)
            safeNotifyDataSetChanged()
            btn_submit?.let {
                it.isEnabled = true
                it.alpha = 1.0f
            }
        }
    }

    fun selectOrDeselectAll(isChecked: Boolean) {
        for (m in masterList) m.isChecked = isChecked
        for (m in displayList) m.isChecked = isChecked
        safeNotifyDataSetChanged()
    }

    fun getDisplayList(): ArrayList<ViewGroupModel> {
        return displayList
    }

    fun getMasterList(): ArrayList<ViewGroupModel> {
        return masterList
    }

    override fun getItemCount(): Int {
        return displayList.size
    }

    override fun getItemId(position: Int): Long {
        if (position < 0 || position >= displayList.size) {
            return RecyclerView.NO_ID
        }
        val item = displayList[position]
        val uid = if (safe(item.id).isNotEmpty()) item.id else item.name
        return safe(uid).hashCode().toLong()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // VG-only views
        var tv_user_type: TextView? = itemView.findViewById(R.id.tv_group_name)
        var tv_members: TextView? = itemView.findViewById(R.id.tv_members)
        var tv_members_count: TextView? = itemView.findViewById(R.id.tv_members_count)
        var action_cv: CardView? = itemView.findViewById(R.id.action_cv)
        var action_list_card: CardView? = itemView.findViewById(R.id.action_list_card)
        var ll_members_count: LinearLayout? = itemView.findViewById(R.id.ll_members_count)

        // Views present across layouts (null if not in current layout — safe)
        var tv_owner_name: TextView? = itemView.findViewById(R.id.tv_group_head)
        var tv_gh_name: TextView? = itemView.findViewById(R.id.tv_gh_name)
        var check_layout: LinearLayout? = itemView.findViewById(R.id.check_layout)
        var cb_team_members: CheckBox? = itemView.findViewById(R.id.chk_selected)
        var tv_date: TextView? = itemView.findViewById(R.id.tv_date)
        var created_id: TextView? = itemView.findViewById(R.id.created_id)
        var custom_spinner: TextView? = itemView.findViewById(R.id.custom_spinner)
        var custom_spinner_cardview: ImageView? = itemView.findViewById(R.id.custom_spinner_cardview)
        val sp_action: ListView? = itemView.findViewById(R.id.action_list)
        val cb_team_members_nonnull: CheckBox? = itemView.findViewById(R.id.chk_selected)
        val rb_group_selected: CheckBox? = itemView.findViewById(R.id.rb_group_selected)
        var tv_tm_name: TextView? = itemView.findViewById(R.id.tv_tm_name)
        var ll_owner: LinearLayout? = itemView.findViewById(R.id.ll_owner)

        init {
            if ("VG" == mTag) {
                tv_members?.let {
                    it.setText(R.string.number_of_members)
                    it.maxLines = 1
                }
                tv_members_count?.let {
                    it.setTextColor(ContextCompat.getColor(it.context, R.color.black))
                }
            }
        }
    }
}
