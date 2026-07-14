package com.digicoffer.lauditor.CommonFiles.DateUtils

import android.app.DatePickerDialog
import android.content.Context
import android.widget.TextView
import com.digicoffer.lauditor.AuditTrails.AuditTrails
import com.digicoffer.lauditor.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DateUtilsEndDate(private val auditTrails: AuditTrails) {

    fun setOnDateSelectedListener(listener: OnDateSelectedListenerEndDate) {
        dateSelectedListener = listener
    }

    interface OnDateSelectedListenerEndDate {
        fun onDateSelectedEndDate(selectedDate: String?, FLAG: String?)
    }

    companion object {
        private var dateSelectedListener: OnDateSelectedListenerEndDate? = null

        @JvmStatic
        fun showDatePickerDialog(context: Context, textView: TextView, context1: Context, FLAG: String) {
            val myCalendar = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateLabel(textView, myCalendar, context1, FLAG)
            }

            val datePickerDialog = DatePickerDialog(
                context,
                R.style.DatePickerStyle,
                dateSetListener,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "OK") { _, _ -> }
            datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancel") { _, _ ->
                textView.text = ""
            }

            datePickerDialog.show()
        }

        private fun updateLabel(textView: TextView, calendar: Calendar, context: Context, FLAG: String) {
            val myFormat = "MMM dd,yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            textView.text = sdf.format(calendar.time)
            dateSelectedListener?.onDateSelectedEndDate(textView.text.toString(), FLAG)
        }

        @JvmStatic
        fun stringToDate(dateString: String?): Date? {
            if (dateString.isNullOrEmpty()) return null
            val dateFormat = SimpleDateFormat("MMM dd,yyyy", Locale.US)
            return try {
                dateFormat.parse(dateString)
            } catch (e: ParseException) {
                e.fillInStackTrace()
                null
            }
        }
    }
}
