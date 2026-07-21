package com.digicoffer.lauditor.Meetings.Adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Meetings.Models.Day
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class WeeklyCalendarAdapter(
    private val days: List<Day>,
    private val listener: OnDaySelectedListener?,
    private val context: Context
) : RecyclerView.Adapter<WeeklyCalendarAdapter.DayViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_day_cell, parent, false)
        return DayViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]

        // Count both events and appointments
        var totalCount = 0

        // Count events from EventDay list
        if (day.events.isNotEmpty()) {
            totalCount = day.events.size
        }

        // Show event/appointment count if available
        if (totalCount > 0) {
            holder.dot.visibility = View.VISIBLE
            holder.dot.text = totalCount.toString()
        } else {
            holder.dot.visibility = View.GONE
            holder.dot.text = ""
        }

        // Parse and display the day
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        try {
            val parsedDate = dateFormat.parse(day.getDate())
            if (parsedDate != null) {
                val dayFormat = SimpleDateFormat("dd", Locale.US)
                val newDay = dayFormat.format(parsedDate)
                holder.textDay.text = newDay
            }
        } catch (e: ParseException) {
            Log.e("DateParsing", "Error parsing date: " + day.getDate(), e)
        }

        // Handle selection visuals
        holder.dayCard.setOnClickListener {
            val previousSelectedPosition = selectedPosition
            selectedPosition = holder.bindingAdapterPosition
            if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousSelectedPosition)
            }
            Constants.isfwd_or_isbwd = false
            notifyItemChanged(selectedPosition)
            listener?.onDaySelected(day)
        }

        if (day.isToday()) {
            holder.dayCard.background = context.getDrawable(R.drawable.rounder_button_blue)
            holder.textDay.setTextColor(Color.WHITE)
        } else {
            holder.dayCard.background = context.getDrawable(R.drawable.rectangle_light_grey)
            holder.textDay.setTextColor(Color.BLACK)
            if (selectedPosition == position) {
                if (!Constants.isfwd_or_isbwd) {
                    holder.dayCard.background = context.getDrawable(R.drawable.rectangular_button_green_count)
                    holder.textDay.setTextColor(Color.WHITE)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return days.size
    }

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textDay: TextView = itemView.findViewById(R.id.dateText)
        val dot: TextView = itemView.findViewById(R.id.dotView)
        val dayCard: LinearLayout = itemView.findViewById(R.id.dayCard)
    }

    // Interface to handle day selection
    interface OnDaySelectedListener {
        fun onDaySelected(day: Day)
    }
}
