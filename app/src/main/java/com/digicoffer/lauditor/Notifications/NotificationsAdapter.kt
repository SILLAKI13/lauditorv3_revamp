package com.digicoffer.lauditor.Notifications

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Notifications.Models.NotificationsDo
import com.digicoffer.lauditor.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.TreeMap

class NotificationsAdapter(
    notificationList: ArrayList<NotificationsDo>,
    private val eventListener: EventListener?,
    private val mcontext: Notifications,
    var highlightIds: String?
) : RecyclerView.Adapter<NotificationsAdapter.GroupViewHolder>(), Filterable {

    private val list_item: ArrayList<NotificationsDo> = ArrayList(notificationList)
    private var filtered_list: ArrayList<NotificationsDo> = ArrayList(notificationList)
    private var groupedList: ArrayList<DateGroup> = ArrayList()

    interface EventListener {
        fun onEvent(id: String)
        fun onSelectAllChanged(allSelected: Boolean)
    }

    init {
        sortNotificationsDescending()
        buildGroupedList()
    }

    private fun sortNotificationsDescending() {
        if (filtered_list.isEmpty()) {
            return
        }
        filtered_list.sortWith { n1, n2 ->
            val date1 = parseDate(n1.timestamp)
            val date2 = parseDate(n2.timestamp)
            if (date1 == null && date2 == null) return@sortWith 0
            if (date1 == null) return@sortWith 1
            if (date2 == null) return@sortWith -1
            date2.compareTo(date1)
        }
    }

    private fun parseDate(timestamp: String?): Date? {
        if (timestamp.isNullOrEmpty()) return null
        return try {
            val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            isoFmt.parse(timestamp)
        } catch (e: ParseException) {
            null
        }
    }

    private fun buildGroupedList() {
        groupedList = ArrayList()
        if (filtered_list.isEmpty()) {
            return
        }

        val grouped = TreeMap<String, ArrayList<NotificationsDo>> { date1, date2 ->
            val d1 = parseDateKey(date1)
            val d2 = parseDateKey(date2)
            if (d1 == null && d2 == null) return@TreeMap 0
            if (d1 == null) return@TreeMap 1
            if (d2 == null) return@TreeMap -1
            d2.compareTo(d1)
        }

        for (notification in filtered_list) {
            val dateKey = getDateKey(notification.timestamp)
            if (!grouped.containsKey(dateKey)) {
                grouped[dateKey] = ArrayList()
            }
            grouped[dateKey]?.add(notification)
        }

        for ((dateKey, items) in grouped) {
            groupedList.add(DateGroup(dateKey, items, items.size))
        }
    }

    private fun getDateKey(timestamp: String?): String {
        if (timestamp.isNullOrEmpty()) return "Unknown Date"
        return try {
            val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val keyFmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }
            val date = isoFmt.parse(timestamp)
            if (date != null) keyFmt.format(date) else "Unknown Date"
        } catch (e: ParseException) {
            "Unknown Date"
        }
    }

    private fun parseDateKey(dateKey: String?): Date? {
        if (dateKey == null || dateKey == "Unknown Date") return null
        return try {
            val keyFmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            keyFmt.parse(dateKey)
        } catch (e: ParseException) {
            null
        }
    }

    private fun formatTimeOnly(timestamp: String?): String {
        if (timestamp.isNullOrEmpty()) return ""
        return try {
            val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val outFmt = SimpleDateFormat("hh:mm a", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }
            val date = isoFmt.parse(timestamp)
            if (date != null) outFmt.format(date) else ""
        } catch (e: ParseException) {
            ""
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_view, parent, false)
        return GroupViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val dateGroup = groupedList[position]

        holder.tv_date_header.text = dateGroup.dateKey
        holder.tv_count_header.text = String.format("(%d)", dateGroup.count)

        holder.notifications_container.removeAllViews()

        for (i in dateGroup.notifications.indices) {
            val notification = dateGroup.notifications[i]
            val rowView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.notification_item_row, holder.notifications_container, false)

            bindNotificationRow(rowView, notification, i == dateGroup.notifications.size - 1)

            rowView.setOnClickListener {
                if ("unread".equals(notification.status, ignoreCase = true)) {
                    mcontext.readSingleNotification(notification.id, notification)
                } else {
                    mcontext.openNotificationNavigation(notification.navigation)
                }
            }

            holder.notifications_container.addView(rowView)
        }

        applyHighlightToCard(holder, dateGroup)
    }

    private fun bindNotificationRow(rowView: View, notification: NotificationsDo, isLastItem: Boolean) {
        val tv_message = rowView.findViewById<TextView>(R.id.tv_messageView)
        val tv_timestamp = rowView.findViewById<TextView>(R.id.tv_timestampView)
        val checkbox = rowView.findViewById<CheckBox>(R.id.chk_selected)
        val view_unread_indicator = rowView.findViewById<View>(R.id.view_unread_indicator)
        val view_indicator_space = rowView.findViewById<View>(R.id.view_indicator_space)
        val divider = rowView.findViewById<View>(R.id.divider)

        tv_message.text = notification.message
        tv_timestamp.text = formatTimeOnly(notification.timestamp)

        if (divider != null) {
            divider.visibility = if (isLastItem) View.GONE else View.VISIBLE
        }

        val isUnread = "unread".equals(notification.status, ignoreCase = true)

        if (isUnread) {
            view_unread_indicator?.visibility = View.VISIBLE
            view_indicator_space?.visibility = View.GONE
            tv_message.setTextColor(
                ContextCompat.getColor(mcontext.requireContext(), R.color.Primary_new)
            )
        } else {
            view_unread_indicator?.visibility = View.GONE
            view_indicator_space?.visibility = View.VISIBLE
            tv_message.setTextColor(
                ContextCompat.getColor(mcontext.requireContext(), R.color.dark_text)
            )
        }

        checkbox.isChecked = notification.isChecked
        checkbox.setOnClickListener {
            notification.isChecked = checkbox.isChecked
            eventListener?.onSelectAllChanged(areAllItemsSelected())
            mcontext.load_list(filtered_list)
            notifyDataSetChanged()
        }
    }

    private fun applyHighlightToCard(holder: GroupViewHolder, dateGroup: DateGroup) {
        var shouldHighlight = false
        for (notification in dateGroup.notifications) {
            if (highlightIds != null && highlightIds!!.contains(notification.id ?: "")) {
                shouldHighlight = true
                break
            }
        }

        if (shouldHighlight) {
            holder.card_view1.background = mcontext.resources.getDrawable(R.drawable.blue_stroke_card)
            holder.card_view1.cardElevation = 8f

            Handler(Looper.getMainLooper()).postDelayed({
                holder.card_view1.background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.rectangular_white_background
                )
            }, 5000)
        } else {
            holder.card_view1.setCardBackgroundColor(
                mcontext.resources.getColor(android.R.color.white)
            )
            holder.card_view1.cardElevation = 4f
        }
    }

    override fun getItemCount(): Int {
        return groupedList.size
    }

    fun areAllItemsSelected(): Boolean {
        for (n in filtered_list) {
            if (!n.isChecked) return false
        }
        return filtered_list.isNotEmpty()
    }

    fun selectOrDeselectAll(isChecked: Boolean) {
        for (n in filtered_list) {
            n.isChecked = isChecked
        }
        sortNotificationsDescending()
        buildGroupedList()
        notifyDataSetChanged()
        eventListener?.onSelectAllChanged(isChecked && filtered_list.isNotEmpty())
    }

    fun getList_item(): ArrayList<NotificationsDo> {
        return list_item
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(query: CharSequence?): FilterResults {
                val result = ArrayList<NotificationsDo>()
                if (query.isNullOrBlank()) {
                    result.addAll(list_item)
                } else {
                    val lower = query.toString().lowercase(Locale.getDefault()).trim()
                    for (n in list_item) {
                        val msg = n.message?.lowercase(Locale.getDefault()) ?: ""
                        if (msg.contains(lower)) {
                            result.add(n)
                        }
                    }
                }
                val fr = FilterResults()
                fr.values = result
                fr.count = result.size
                return fr
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(query: CharSequence?, results: FilterResults) {
                filtered_list = results.values as ArrayList<NotificationsDo>
                sortNotificationsDescending()
                buildGroupedList()
                notifyDataSetChanged()
            }
        }
    }

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card_view1: CardView = itemView.findViewById(R.id.card_view1)
        val tv_date_header: TextView = itemView.findViewById(R.id.tv_date_header)
        val tv_count_header: TextView = itemView.findViewById(R.id.tv_count_header)
        val notifications_container: LinearLayout = itemView.findViewById(R.id.notifications_container)
        val message_list: LinearLayout = itemView.findViewById(R.id.message_list)
    }

    private class DateGroup(
        val dateKey: String,
        val notifications: ArrayList<NotificationsDo>,
        val count: Int
    )
}
