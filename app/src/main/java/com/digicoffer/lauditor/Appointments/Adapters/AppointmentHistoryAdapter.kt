package com.digicoffer.lauditor.Appointments.Adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

class AppointmentHistoryAdapter(
    private val appointmentList: ArrayList<AppointmentModel>,
    private val context: Context,
    private val noteActionListener: OnNoteActionListener?
) : RecyclerView.Adapter<AppointmentHistoryAdapter.ViewHolder>() {

    var noteId: String = ""

    interface OnNoteActionListener {
        fun onSaveNote(appointment: AppointmentModel, note: String, position: Int)
        fun onCancelNote(appointment: AppointmentModel, position: Int)
        fun onEditNote(appointment: AppointmentModel, noteId: String, noteText: String, position: Int)
        fun onDeleteNote(appointment: AppointmentModel, noteId: String, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointmentList[position]

        // Hide timeline line for last item
        if (position == appointmentList.size - 1) {
            holder.timelineLine.visibility = View.INVISIBLE
        } else {
            holder.timelineLine.visibility = View.VISIBLE
        }

        // Set appointment title (count from end)
        val appointmentNumber = appointmentList.size - position
        val title = getOrdinal(appointmentNumber) + " Appointment"
        holder.tvAppointmentTitle.text = title

        // Format and set date/time
        val formattedDateTime = formatDateTime(appointment.appointment_from, appointment.appointment_to)
        holder.tvAppointmentDateTime.text = formattedDateTime

        // Set payment info
        if (appointment.payment != null) {
            val paymentInfo = "Payment: " + appointment.payment!!.label
            holder.tvPaymentInfo.text = paymentInfo
        }

        // Set status
        holder.tvStatusBadge.text = appointment.appointment_status
        // Set status badge AND timeline dot color
        setStatusBadgeStyle(holder.tvStatusBadge, holder.timelineDot, appointment.appointment_status)

        // Handle note section visibility
        if (appointment.isNoteExpanded) {
            holder.llNoteSection.visibility = View.VISIBLE
            holder.btnAddNote.visibility = View.GONE
            holder.etNoteInput.setText(appointment.tempNote)
            holder.etNoteInput.setSelection(holder.etNoteInput.text.length)
        } else {
            holder.llNoteSection.visibility = View.GONE
            holder.btnAddNote.visibility = View.VISIBLE
        }

        // Add Note button click
        holder.btnAddNote.setOnClickListener {
            val curPos = holder.bindingAdapterPosition
            if (curPos != RecyclerView.NO_POSITION && curPos >= 0 && curPos < appointmentList.size) {
                appointmentList[curPos].isNoteExpanded = true
                notifyItemChanged(curPos)
            }
        }

        holder.llNotesContainer.removeAllViews()

        if (appointment.isNoteExpanded) {
            // Editing Mode → Hide previous notes
            holder.llNotesContainer.visibility = View.GONE
            holder.btnAddNote.visibility = View.GONE
        } else if (appointment.notes != null && appointment.notes.length() > 0) {
            // Show previous notes only if NOT in edit mode
            holder.llNotesContainer.visibility = View.VISIBLE
            holder.btnAddNote.visibility = View.GONE

            for (i in 0 until appointment.notes.length()) {
                try {
                    val noteObj = appointment.notes.getJSONObject(i)
                    val noteText = noteObj.optString("note")
                    val createdOn = noteObj.optString("created_on")

                    val noteView = LayoutInflater.from(context)
                        .inflate(R.layout.appointments_notes_item, holder.llNotesContainer, false)

                    val tvNoteText: TextView = noteView.findViewById(R.id.tv_note_text)
                    val tvNoteDate: TextView = noteView.findViewById(R.id.tv_note_date)

                    tvNoteText.text = noteText
                    tvNoteDate.text = formatNoteDate(createdOn)
                    val ivEdit: ImageView = noteView.findViewById(R.id.iv_edit_note)
                    val ivDelete: ImageView = noteView.findViewById(R.id.iv_delete_note)

                    noteId = noteObj.optString("id")
                    val localNoteId = noteObj.optString("id")

                    ivEdit.setOnClickListener {
                        val curPos = holder.bindingAdapterPosition
                        if (curPos != RecyclerView.NO_POSITION && curPos >= 0 && curPos < appointmentList.size) {
                            val apt = appointmentList[curPos]
                            apt.isNoteExpanded = true
                            apt.isEditingNote = true
                            apt.editingNoteId = localNoteId
                            apt.tempNote = noteText
                            notifyItemChanged(curPos)
                        }
                    }

                    ivDelete.setOnClickListener {
                        val curPos = holder.bindingAdapterPosition
                        if (curPos != RecyclerView.NO_POSITION && curPos >= 0 && curPos < appointmentList.size) {
                            AndroidUtils.showConfirmationDialog(
                                context, "Delete Note", "Are you sure you want to delete this note?",
                                object : AndroidUtils.OnConfirmListener {
                                    override fun onSave() {
                                        noteActionListener?.onDeleteNote(appointmentList[curPos], localNoteId, curPos)
                                    }
                                    override fun onCancel() {}
                                }
                            )
                        }
                    }

                    holder.llNotesContainer.addView(noteView)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        AndroidUtils.ToggleButton(0, holder.btnSaveNote)

        // Character counter
        holder.etNoteInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                holder.tvCharCounter.text = "$length/500"
                val curPos = holder.bindingAdapterPosition
                if (curPos != RecyclerView.NO_POSITION && curPos >= 0 && curPos < appointmentList.size) {
                    appointmentList[curPos].tempNote = s?.toString() ?: ""
                }
                if (s.isNullOrEmpty()) {
                    AndroidUtils.ToggleButton(0, holder.btnSaveNote)
                } else {
                    AndroidUtils.ToggleButton(1, holder.btnSaveNote)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Cancel button
        holder.btnCancelNote.setOnClickListener {
            val curPos = holder.bindingAdapterPosition
            if (curPos != RecyclerView.NO_POSITION && curPos >= 0 && curPos < appointmentList.size) {
                val apt = appointmentList[curPos]
                apt.isNoteExpanded = false
                apt.tempNote = ""
                holder.etNoteInput.setText("")
                notifyItemChanged(curPos)

                noteActionListener?.onCancelNote(apt, curPos)
            }
        }

        holder.btnSaveNote.setOnClickListener {
            val curPos = holder.bindingAdapterPosition
            if (curPos != RecyclerView.NO_POSITION && curPos >= 0 && curPos < appointmentList.size) {
                val noteText = holder.etNoteInput.text.toString().trim()
                if (noteText.isEmpty()) return@setOnClickListener

                val apt = appointmentList[curPos]
                if (apt.isEditingNote) {
                    noteActionListener?.onEditNote(apt, apt.editingNoteId, noteText, curPos)
                } else {
                    noteActionListener?.onSaveNote(apt, noteText, curPos)
                }

                // Reset UI
                apt.isNoteExpanded = false
                apt.isEditingNote = false
                apt.editingNoteId = ""
                apt.tempNote = ""

                holder.etNoteInput.setText("")
                notifyItemChanged(curPos)
            }
        }
    }

    override fun getItemCount(): Int {
        return appointmentList.size
    }

    private fun formatDateTime(fromDateTime: String?, toDateTime: String?): String {
        if (fromDateTime.isNullOrEmpty() || toDateTime.isNullOrEmpty()) return ""
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

            val fromDate = inputFormat.parse(fromDateTime)
            val toDate = inputFormat.parse(toDateTime)

            if (fromDate != null && toDate != null) {
                val date = dateFormat.format(fromDate)
                val fromTime = timeFormat.format(fromDate)
                val toTime = timeFormat.format(toDate)
                return "$date | $fromTime - $toTime"
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun getOrdinal(number: Int): String {
        val suffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
        return when (number % 100) {
            11, 12, 13 -> "${number}th"
            else -> number.toString() + suffixes[number % 10]
        }
    }

    private fun formatNoteDate(dateTime: String?): String {
        if (dateTime.isNullOrEmpty()) return ""
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.ENGLISH)
            val date = inputFormat.parse(dateTime)
            if (date != null) {
                return outputFormat.format(date)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun setStatusBadgeStyle(badge: TextView, timelineDot: View, status: String?) {
        val normalizedStatus = status?.lowercase()?.trim() ?: "unknown"

        if ("completed" == normalizedStatus) {
            badge.text = "Completed"
            badge.setBackgroundResource(R.drawable.completed_badge)
            badge.setTextColor(ContextCompat.getColor(context, R.color.completed_text))
            timelineDot.setBackgroundResource(R.drawable.green_circular)
        } else if ("cancelled" == normalizedStatus || "canceled" == normalizedStatus) {
            badge.text = "Cancelled"
            badge.setBackgroundResource(R.drawable.cancelled_badge)
            badge.setTextColor(ContextCompat.getColor(context, R.color.cancelled_text))
            timelineDot.setBackgroundResource(R.drawable.red_circular)
        } else if ("upcoming" == normalizedStatus || "ongoing" == normalizedStatus) {
            badge.text = "Upcoming"
            badge.setBackgroundResource(R.drawable.scheduled_badge)
            badge.setTextColor(ContextCompat.getColor(context, R.color.scheduled_text))
            timelineDot.setBackgroundResource(R.drawable.circle_blue_bg)
        } else if ("payment_pending" == normalizedStatus) {
            badge.text = "Payment Pending"
            badge.setBackgroundResource(R.drawable.pending_badge)
            badge.setTextColor(ContextCompat.getColor(context, R.color.payment_pending_color))
            timelineDot.setBackgroundResource(R.drawable.orange_circular)
        } else {
            badge.text = status ?: "Unknown"
            badge.setBackgroundResource(R.drawable.scheduled_badge)
            badge.setTextColor(ContextCompat.getColor(context, R.color.scheduled_text))
            timelineDot.setBackgroundResource(R.drawable.circle_blue_bg)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timelineDot: View = itemView.findViewById(R.id.timeline_dot)
        val timelineLine: View = itemView.findViewById(R.id.timeline_line)
        val tvAppointmentTitle: TextView = itemView.findViewById(R.id.tv_appointment_title)
        val tvAppointmentDateTime: TextView = itemView.findViewById(R.id.tv_appointment_datetime)
        val tvPaymentInfo: TextView = itemView.findViewById(R.id.tv_payment_info)
        val tvStatusBadge: TextView = itemView.findViewById(R.id.tv_status_badge)
        val tvCharCounter: TextView = itemView.findViewById(R.id.tv_char_counter)
        val btnAddNote: AppCompatButton = itemView.findViewById(R.id.btn_add_note)
        val btnCancelNote: AppCompatButton = itemView.findViewById(R.id.btn_cancel_note)
        val btnSaveNote: AppCompatButton = itemView.findViewById(R.id.btn_save_note)
        val llNoteSection: LinearLayout = itemView.findViewById(R.id.ll_note_section)
        val etNoteInput: EditText = itemView.findViewById(R.id.et_note_input)
        val llNotesContainer: LinearLayout = itemView.findViewById(R.id.llNotesContainer)
    }
}
