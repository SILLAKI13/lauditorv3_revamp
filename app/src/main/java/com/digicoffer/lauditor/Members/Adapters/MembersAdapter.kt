package com.digicoffer.lauditor.Members.Adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Groups.Models.ActionModel
import com.digicoffer.lauditor.Members.Members
import com.digicoffer.lauditor.Members.MembersModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import org.json.JSONException
import java.util.ArrayList
import java.util.HashMap

class MembersAdapter : RecyclerView.Adapter<MembersAdapter.ViewHolder>, Filterable {

    var itemsArrayList = ArrayList<MembersModel>()
    var listItems = ArrayList<MembersModel>()
    var mcontext: Context? = null
    var members: Members? = null
    private var expandedPosition = -1
    var eventListener: EventListener? = null

    interface EventListener {
        fun EditMember(membersModel: MembersModel)

        @Throws(JSONException::class)
        fun UpdateGroupAccess(membersModel: MembersModel)

        fun ResetPassword(membersModel: MembersModel)

        fun DeleteMember(membersModel: MembersModel)

        fun Upgrade(membersModel: MembersModel)
    }

    constructor(members_list: ArrayList<MembersModel>) {
        this.itemsArrayList = ArrayList(members_list)
        this.listItems = ArrayList(members_list)
    }

    constructor(
        itemsArrayList: ArrayList<MembersModel>,
        context: Context?,
        listener: EventListener?,
        members: Members?
    ) {
        this.itemsArrayList = ArrayList(itemsArrayList)
        this.listItems = ArrayList(itemsArrayList)
        this.mcontext = context
        this.eventListener = listener
        this.members = members
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence?.toString() ?: ""
                val resultList = ArrayList<MembersModel>()
                if (charString.isEmpty()) {
                    resultList.addAll(listItems)
                } else {
                    for (row in listItems) {
                        if (AndroidUtils.isNull(row.name).lowercase().contains(charString.lowercase()) ||
                            AndroidUtils.isNull(row.designation).lowercase().contains(charString.lowercase()) ||
                            AndroidUtils.isNull(row.email).lowercase().contains(charString.lowercase())
                        ) {
                            resultList.add(row)
                        }
                    }
                }
                val filterResults = FilterResults()
                filterResults.count = resultList.size
                filterResults.values = resultList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
                itemsArrayList = (filterResults?.values as? ArrayList<MembersModel>) ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_members, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val membersModel = itemsArrayList[position]

        val localActionsList = ArrayList<ActionModel>()
        if (Constants.ROLE == "GH") {
            localActionsList.add(ActionModel("Edit Member Info"))
            localActionsList.add(ActionModel("Delete Member"))
            localActionsList.add(ActionModel("Reset Password"))
            localActionsList.add(ActionModel("Upgrade as Practice Partner"))
        } else {
            localActionsList.add(ActionModel("Edit Member Info"))
            localActionsList.add(ActionModel("Update Group Access"))
            localActionsList.add(ActionModel("Reset Password"))
            localActionsList.add(ActionModel("Delete Member"))
            localActionsList.add(ActionModel("Upgrade as Practice Partner"))
        }

        if (membersModel.isdisabled) {
            holder.custom_spinner_cardview.isEnabled = false
            holder.custom_spinner_cardview.alpha = 0.5f
        } else {
            holder.custom_spinner_cardview.isEnabled = true
            holder.custom_spinner_cardview.alpha = 1.0f
        }

        holder.tv_member_name.text = if (isValidString(membersModel.name)) membersModel.name else ""

        holder.tv_member_type.text = "Designation : "
        holder.tv_litigation.text = if (isValidString(membersModel.designation)) membersModel.designation else ""

        holder.tv_currency_type.text = "Currency : "
        val symbol = getCurrencySymbolFromString(if (isValidString(membersModel.currency)) membersModel.currency else "")
        holder.tv_currency.text = if (isValidString(membersModel.currency)) membersModel.currency else ""

        holder.tv_rate_label.text = "Rate : "
        holder.tv_rate_value.text = if (isValidString(membersModel.defaultRate)) symbol + membersModel.defaultRate else "0"

        holder.tv_email_label.text = "Email : "
        holder.tv_email_id.text = if (isValidString(membersModel.email)) membersModel.email else ""

        val spinner_adapter = CommonSpinnerAdapter(mcontext as? Activity, localActionsList)
        holder.sp_action.adapter = spinner_adapter
        AndroidUtils.setDynamicHeight(holder.sp_action)

        val isExpanded = position == expandedPosition
        holder.action_list_card.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.sp_action.visibility = if (isExpanded) View.VISIBLE else View.GONE

        holder.custom_spinner_cardview.setOnClickListener(null)
        holder.sp_action.onItemClickListener = null

        holder.custom_spinner_cardview.setOnClickListener {
            val previousExpanded = expandedPosition
            expandedPosition = if (expandedPosition == position) -1 else position

            if (members != null && members?.rv_view_members != null) {
                members?.rv_view_members?.post {
                    if (previousExpanded != -1) notifyItemChanged(previousExpanded)
                    notifyItemChanged(position)
                }
            } else {
                if (previousExpanded != -1) notifyItemChanged(previousExpanded)
                notifyItemChanged(position)
            }
        }

        holder.sp_action.setOnItemClickListener { parent, view, pos, id ->
            spinner_adapter.setSelectedPosition(pos)
            val selectedAction = localActionsList[pos].name.trim()

            expandedPosition = -1
            if (members != null && members?.rv_view_members != null) {
                members?.rv_view_members?.post { notifyItemChanged(position) }
            } else {
                notifyItemChanged(position)
            }

            when (selectedAction) {
                "Edit Member Info" -> {
                    members?.model_name("Edit Member")
                    eventListener?.EditMember(membersModel)
                }
                "Update Group Access" -> {
                    members?.model_name("Update Group Access")
                    try {
                        eventListener?.UpdateGroupAccess(membersModel)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                "Reset Password" -> {
                    eventListener?.ResetPassword(membersModel)
                }
                "Delete Member" -> {
                    members?.model_name("Delete Member")
                    eventListener?.DeleteMember(membersModel)
                }
                "Upgrade as Practice Partner" -> {
                    members?.model_name("Upgrade as Practice Partners")
                    eventListener?.Upgrade(membersModel)
                }
            }
        }
    }

    private fun isValidString(value: String?): Boolean {
        return value != null && value != "null" && value.trim().isNotEmpty()
    }

    override fun getItemCount(): Int {
        return itemsArrayList.size
    }

    companion object {
        private val currencySymbols = HashMap<String, String>().apply {
            put("USD", "$")
            put("EUR", "€")
            put("JPY", "¥")
            put("GBP", "£")
            put("AUD", "A$")
            put("CAD", "C$")
            put("CHF", "CHF")
            put("KWD", "KD")
            put("BHD", "BD")
            put("INR", "₹")
        }
    }

    private fun getCurrencySymbolFromString(currencyString: String?): String {
        if (currencyString == null) return ""
        val start = currencyString.indexOf("(")
        val end = currencyString.indexOf(")")
        if (start != -1 && end != -1 && end > start) {
            val code = currencyString.substring(start + 1, end).uppercase()
            val symbol = currencySymbols[code]
            return symbol ?: code
        }
        return currencyString
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var designation: TextView = itemView.findViewById(R.id.designation)
        var tv_member_name: TextView = itemView.findViewById(R.id.tv_member_name)
        var tv_member_type: TextView = itemView.findViewById(R.id.tv_currency_type)
        var tv_litigation: TextView = itemView.findViewById(R.id.tv_litigation)
        var tv_currency_type: TextView = itemView.findViewById(R.id.tv_currency_type)
        var tv_currency: TextView = itemView.findViewById(R.id.tv_currency)
        var tv_rate_label: TextView = itemView.findViewById(R.id.tv_rate_label)
        var tv_rate_value: TextView = itemView.findViewById(R.id.tv_rate_value)
        var tv_email_label: TextView = itemView.findViewById(R.id.tv_email_label)
        var tv_email_id: TextView = itemView.findViewById(R.id.tv_email_id)
        var custom_spinner_cardview: ImageView = itemView.findViewById(R.id.custom_spinner_cardview)
        var sp_action: ListView = itemView.findViewById(R.id.list_client)
        var action_list_card: CardView = itemView.findViewById(R.id.action_list_card)

        init {
            designation.text = "Designation :"
        }
    }
}
