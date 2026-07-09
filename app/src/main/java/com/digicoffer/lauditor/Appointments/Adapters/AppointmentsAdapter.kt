package com.digicoffer.lauditor.Appointments.Adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.Chat.ViewModels.Chat
import com.digicoffer.lauditor.Groups.Models.ActionModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.Date
import java.util.Locale
import java.util.Objects

class AppointmentsAdapter(
    var itemsArrayList: ArrayList<AppointmentModel>,
    private val context: Context,
    private val eventListener: InterfaceListener,
    private val highlightIds: String?,
    private val activity: Activity
) : RecyclerView.Adapter<AppointmentsAdapter.MyViewHolder>(), Filterable {

    val listItem: ArrayList<AppointmentModel> = ArrayList(itemsArrayList)
    private var expandedPosition = -1
    private var recyclerView: RecyclerView? = null

    fun setRecyclerView(rv: RecyclerView) {
        this.recyclerView = rv
        rv.clipChildren = false
        rv.clipToPadding = false
        if (rv.parent is ViewGroup) {
            (rv.parent as ViewGroup).clipChildren = false
            (rv.parent as ViewGroup).clipToPadding = false
        }
    }

    interface InterfaceListener {
        fun ViewAppointmentHistory(
            appointmentModel: AppointmentModel,
            itemsArrayList: ArrayList<AppointmentModel>
        )
        fun CancelAppointment(appointmentModel: AppointmentModel)
        fun DeleteAppointment(appointmentModel: AppointmentModel)
    }

    private fun safeNotify(position: Int) {
        val r = Runnable {
            if (position in 0 until itemCount) {
                notifyItemChanged(position)
            }
        }
        if (recyclerView != null) {
            recyclerView!!.post(r)
        } else {
            Handler(Looper.getMainLooper()).post(r)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.appointment_card_listing, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val appointmentModel = itemsArrayList[position]

        try {
            // ── Client Name ──────────────────────────────────────────────────────
            holder.tv_client_name.text = appointmentModel.client_name

            // ── Date and Time ────────────────────────────────────────────────────
            val formattedDateTime = formatAppointmentDateTime(
                appointmentModel.appointment_from,
                appointmentModel.appointment_to
            )
            holder.tv_appointment_datetime.text = formattedDateTime

            // ── Payment Status ───────────────────────────────────────────────────
            val paymentStatus = appointmentModel.payment?.status ?: ""
            val amountPaid = appointmentModel.payment?.amount_paid ?: ""
            val symbol = appointmentModel.payment?.symbol ?: ""

            if ("paid".equals(paymentStatus, ignoreCase = true)) {
                holder.tv_payment_status.text = "Paid - $symbol$amountPaid"
                holder.tv_payment_status.setTextColor(
                    ContextCompat.getColor(context, R.color.payment_paid_color)
                )
            } else {
                holder.tv_payment_status.text = "Pending - $symbol" + "0"
                holder.tv_payment_status.setTextColor(
                    ContextCompat.getColor(context, R.color.payment_pending_color)
                )
            }

            // ── Appointment Status ───────────────────────────────────────────────
            val appointmentStatus = (appointmentModel.appointment_status ?: "")
                .lowercase(Locale.ROOT)

            // Chat icon: enabled within 1 hour OR completed
            if (AndroidUtils.isWithinOneHour(appointmentModel.appointment_from)
                || appointmentStatus.equals("completed", ignoreCase = true)) {
                AndroidUtils.ToggleButton(1, holder.iv_chat)
            } else {
                AndroidUtils.ToggleButton(0, holder.iv_chat)
            }

            // Video call icon: enabled within 1 minute
            if (AndroidUtils.isWithinOneMinute(appointmentModel.appointment_from)) {
                AndroidUtils.ToggleButton(1, holder.iv_video_call)
            } else {
                AndroidUtils.ToggleButton(0, holder.iv_video_call)
            }

            // ── Status badge + icon visibility per status ────────────────────────
            if ("completed".equals(appointmentStatus, ignoreCase = true)) {
                holder.tv_appointment_status.text = "Completed"
                holder.tv_appointment_status.setBackgroundResource(R.drawable.completed_badge)
                holder.tv_appointment_status.setTextColor(
                    ContextCompat.getColor(context, R.color.completed_text)
                )
                holder.iv_chat.visibility = View.VISIBLE
                holder.iv_video_call.visibility = View.GONE

            } else if ("cancelled".equals(appointmentStatus, ignoreCase = true)
                || "canceled".equals(appointmentStatus, ignoreCase = true)) {
                holder.tv_appointment_status.text = "Cancelled"
                holder.tv_appointment_status.setBackgroundResource(R.drawable.cancelled_badge)
                holder.tv_appointment_status.setTextColor(
                    ContextCompat.getColor(context, R.color.cancelled_text)
                )
                holder.iv_chat.visibility = View.GONE
                holder.iv_video_call.visibility = View.GONE

            } else if ("upcoming".equals(appointmentStatus, ignoreCase = true)
                || "ongoing".equals(appointmentStatus, ignoreCase = true)) {
                holder.tv_appointment_status.text = AndroidUtils.CapitalizeFirstLetter(appointmentStatus)
                holder.tv_appointment_status.setBackgroundResource(R.drawable.scheduled_badge)
                holder.tv_appointment_status.setTextColor(
                    ContextCompat.getColor(context, R.color.blue_dark)
                )
                holder.iv_chat.visibility = View.VISIBLE
                holder.iv_video_call.visibility = View.VISIBLE

            } else if ("payment_pending".equals(appointmentStatus, ignoreCase = true)) {
                holder.tv_appointment_status.text = "Payment Pending"
                holder.tv_appointment_status.setBackgroundResource(R.drawable.pending_badge)
                holder.tv_appointment_status.setTextColor(
                    ContextCompat.getColor(context, R.color.pending_text)
                )
                holder.iv_chat.visibility = View.GONE
                holder.iv_video_call.visibility = View.GONE

            } else {
                // Default / scheduled
                holder.tv_appointment_status.text = "Scheduled"
                holder.tv_appointment_status.setBackgroundResource(R.drawable.scheduled_badge)
                holder.tv_appointment_status.setTextColor(
                    ContextCompat.getColor(context, R.color.scheduled_text)
                )
                holder.iv_chat.visibility = View.GONE
                holder.iv_video_call.visibility = View.GONE
            }

            // ── Profile image ────────────────────────────────────────────────────
            AndroidUtils.loadProfileImage(
                context,
                appointmentModel.client_profile_pic,
                holder.iv_profile,
                holder.person_icon,
                appointmentModel.client_name
            )

            // ── Video call click ─────────────────────────────────────────────────
            holder.iv_video_call.setOnClickListener {
                val parts = AndroidUtils.extractDateTimeParts(
                    appointmentModel.appointment_from,
                    appointmentModel.appointment_to
                )
                if (parts != null) {
                    val date = parts[0]
                    val fromTime = parts[1]
                    val toTime = parts[2]
                    val url = AndroidUtils.getAVChatUrl(
                        appointmentModel.meeting_room_id,
                        fromTime,
                        toTime,
                        date,
                        appointmentModel.client_name
                    )
                    AndroidUtils.loadAVChatView(context, activity, url)
                    Log.d("AVCHAT_URL", url)
                }
            }

            // ── Chat click ───────────────────────────────────────────────────────
            holder.iv_chat.setOnClickListener {
                Constants.isClient_chat = true
                Constants.pendingChatJid = appointmentModel.guid
                Constants.pendingChatName = appointmentModel.client_name
                Constants.pendingChatSource = "appointment"
                Constants.mainActivity?.navigation_items(Chat())
            }

            // ── Card click → history ─────────────────────────────────────────────
            holder.appointment_card.setOnClickListener {
                eventListener.ViewAppointmentHistory(appointmentModel, itemsArrayList)
            }

            // ── Three-dot action menu ────────────────────────────────────────────
            bindActionMenu(holder, appointmentModel, position, appointmentStatus)

            // ── Highlight ────────────────────────────────────────────────────────
            applyHighlight(holder, appointmentModel)

        } catch (e: Exception) {
            AndroidUtils.showToast(e.message, context)
            Log.d("Appointment Details", e.message ?: "")
        }
    }

    private fun bindActionMenu(
        holder: MyViewHolder,
        appointmentModel: AppointmentModel,
        position: Int,
        appointmentStatus: String
    ) {
        val isUpcomingOrOngoing = "upcoming".equals(appointmentStatus, ignoreCase = true)
            || "ongoing".equals(appointmentStatus, ignoreCase = true)

        val cancelEnabled = !AndroidUtils.isWithinTwoHours(appointmentModel.appointment_from)

        val canDelete = "completed".equals(appointmentStatus, ignoreCase = true)
            || "cancelled".equals(appointmentStatus, ignoreCase = true)
            || "canceled".equals(appointmentStatus, ignoreCase = true)

        val itemActions = ArrayList<ActionModel>()

        // History — always present
        itemActions.add(ActionModel("History"))

        // Cancel — shown for upcoming/ongoing
        if (isUpcomingOrOngoing) {
            val cancelAction = ActionModel("Cancel")
            cancelAction.isEnabled = cancelEnabled
            itemActions.add(cancelAction)
        }

        // Delete — shown for completed or cancelled
        if (canDelete) {
            itemActions.add(ActionModel("Delete"))
        }

        holder.custom_spinner_cardview.setOnClickListener(null)
        holder.sp_action.onItemClickListener = null

        val isExpanded = (position == expandedPosition)

        if (isExpanded) {
            val itemAdapter = CommonSpinnerAdapter(context as Activity, itemActions)
            holder.sp_action.adapter = itemAdapter
            holder.sp_action.post { AndroidUtils.setDynamicHeight(holder.sp_action) }
            holder.action_list_card.visibility = View.VISIBLE
            holder.sp_action.visibility = View.VISIBLE
            holder.action_list_card.bringToFront()
            holder.action_list_card.invalidate()
        } else {
            holder.sp_action.adapter = null
            holder.action_list_card.visibility = View.GONE
            holder.sp_action.visibility = View.GONE
        }

        // Three-dot tap: toggle expand / collapse
        holder.custom_spinner_cardview.setOnClickListener {
            val cur = holder.bindingAdapterPosition
            if (cur == RecyclerView.NO_POSITION) return@setOnClickListener
            val prev = expandedPosition
            expandedPosition = if (expandedPosition == cur) -1 else cur
            if (prev != -1 && prev != cur) safeNotify(prev)
            safeNotify(cur)
        }

        // Action item selected
        holder.sp_action.setOnItemClickListener { parent, view, pos, id ->
            val cur = holder.bindingAdapterPosition
            if (cur == RecyclerView.NO_POSITION) return@setOnItemClickListener

            val selectedAction = itemActions[pos]
            if (!selectedAction.isEnabled) return@setOnItemClickListener

            val actionName = selectedAction.name
            expandedPosition = -1
            safeNotify(cur)

            Handler(Looper.getMainLooper()).post {
                dispatchAction(actionName, appointmentModel)
            }
        }
    }

    private fun dispatchAction(action: String, appointmentModel: AppointmentModel) {
        when (action) {
            "History" -> eventListener.ViewAppointmentHistory(appointmentModel, itemsArrayList)
            "Cancel" -> eventListener.CancelAppointment(appointmentModel)
            "Delete" -> eventListener.DeleteAppointment(appointmentModel)
        }
    }

    private fun applyHighlight(holder: MyViewHolder, model: AppointmentModel) {
        if (highlightIds != null && highlightIds.contains(model.id)) {
            holder.appointment_card.background = ContextCompat.getDrawable(
                holder.itemView.context,
                R.drawable.blue_stroke_card
            )
            Handler(Looper.getMainLooper()).postDelayed({
                holder.appointment_card.background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.rectangular_white_background
                )
            }, 5000)
        } else {
            holder.appointment_card.background = ContextCompat.getDrawable(
                holder.itemView.context,
                R.drawable.rectangular_white_background
            )
        }
    }

    fun sortAppointmentsByDate() {
        Collections.sort(itemsArrayList) { a1, a2 ->
            try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
                val date1 = format.parse(a1.appointment_from)
                val date2 = format.parse(a2.appointment_from)
                if (date1 != null && date2 != null) {
                    date2.compareTo(date1) // descending
                } else {
                    0
                }
            } catch (e: ParseException) {
                e.printStackTrace()
                0
            }
        }
        notifyDataSetChanged()
    }

    private fun formatAppointmentDateTime(fromDateTime: String?, toDateTime: String?): String {
        if (fromDateTime.isNullOrEmpty() || toDateTime.isNullOrEmpty()) return ""
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
            val timeFormat = SimpleDateFormat("h:mm a", Locale.ENGLISH)

            val fromDate = inputFormat.parse(fromDateTime)
            val toDate = inputFormat.parse(toDateTime)

            if (fromDate != null && toDate != null) {
                val date = dateFormat.format(fromDate)
                val fromTime = timeFormat.format(fromDate)
                val toTime = timeFormat.format(toDate)
                return "$date • $fromTime - $toTime"
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
    }

    override fun getItemCount(): Int {
        return itemsArrayList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence?.toString() ?: ""
                if (charString.isEmpty()) {
                    itemsArrayList = ArrayList(listItem)
                } else {
                    val filteredList = ArrayList<AppointmentModel>()
                    for (row in listItem) {
                        val clientName = AndroidUtils.isNull(row.client_name)
                        if (clientName.lowercase(Locale.ROOT).contains(charString.lowercase(Locale.ROOT))) {
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
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
                itemsArrayList = filterResults?.values as? ArrayList<AppointmentModel> ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_client_name: TextView = itemView.findViewById(R.id.tv_client_name)
        val iv_calendar_icon: ImageView = itemView.findViewById(R.id.iv_calendar_icon)
        val iv_video_call: ImageView = itemView.findViewById(R.id.iv_video_call)
        val iv_chat: ImageView = itemView.findViewById(R.id.iv_chat)
        val person_icon: TextView = itemView.findViewById(R.id.person_icon)
        val iv_profile: ImageView = itemView.findViewById(R.id.iv_profile)
        val tv_appointment_datetime: TextView = itemView.findViewById(R.id.tv_appointment_datetime)
        val tv_payment_status: TextView = itemView.findViewById(R.id.tv_payment_status)
        val tv_appointment_status: TextView = itemView.findViewById(R.id.tv_appointment_status)
        val action_layout: LinearLayout = itemView.findViewById(R.id.action_layout)
        val appointment_card: LinearLayout = itemView.findViewById(R.id.appointment_card)

        // Three-dot menu
        val custom_spinner_cardview: ImageView = itemView.findViewById(R.id.custom_spinner_cardview) // ⋮ button
        val action_list_card: CardView = itemView.findViewById(R.id.action_list_card)
        val sp_action: ListView = itemView.findViewById(R.id.list_actions) // dropdown list

        init {
            tv_payment_status.visibility = View.VISIBLE
        }
    }
}
