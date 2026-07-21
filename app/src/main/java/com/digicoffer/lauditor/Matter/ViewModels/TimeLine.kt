package com.digicoffer.lauditor.Matter.ViewModels

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.Matter.Models.HistoryModel
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.R
import com.google.android.material.textfield.TextInputEditText
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Locale

class TimeLine() : Fragment() {
    var historyList = ArrayList<HistoryModel>()
    var viewMatter = ViewMatter()
    var header_name = ""
    var matter: Matter? = null
    var viewMatterModel: ViewMatterModel? = null

    constructor(
        historyList1: ArrayList<HistoryModel>,
        viewMatter1: ViewMatter,
        header_name1: String,
        matter1: Matter?,
        viewMatterModel: ViewMatterModel?
    ) : this() {
        historyList = historyList1
        viewMatter = viewMatter1
        header_name = header_name1
        matter = matter1
        this.viewMatterModel = viewMatterModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.timeline_notes, container, false)
        val builder = AlertDialog.Builder(context)
        val close_details = view.findViewById<ImageView>(R.id.close_details)
        val ll_timeline = view.findViewById<LinearLayout>(R.id.ll_timeLine)
        val tv_header_name = view.findViewById<TextView>(R.id.header_name)
        tv_header_name.text = header_name
        val dialog = builder.create()
        ll_timeline.removeAllViews()

        for (i in historyList.indices) {
            try {
                val view_timeLine = LayoutInflater.from(context).inflate(R.layout.matter_timeline, null)
                val tv_timeline_title = view_timeLine.findViewById<TextView>(R.id.tv_timeline_title)
                tv_timeline_title.setText(R.string.matter_timeline)
                val notes_layout = view_timeLine.findViewById<LinearLayout>(R.id.notes_layout)
                notes_layout.visibility = View.GONE

                val tv_timeline_date = view_timeLine.findViewById<TextView>(R.id.tv_timeline_date)
                tv_timeline_date.setText(R.string.date)
                val rb_corporate_notes = view_timeLine.findViewById<RadioButton>(R.id.rb_corporate_notes)
                val corp = viewMatterModel?.corporate
                if (corp != null && corp.length() > 0) {
                    if (historyList[i].from_ts != null
                        && historyList[i].to_ts != null
                        && historyList[i].from_ts == historyList[i].to_ts
                    ) {
                        notes_layout.visibility = View.GONE
                    } else {
                        notes_layout.visibility = View.VISIBLE
                    }
                } else {
                    notes_layout.visibility = View.GONE
                }
                rb_corporate_notes.setText(R.string.corporate_notes)
                val rb_lauditor_notes = view_timeLine.findViewById<RadioButton>(R.id.rb_lauditor_notes)

                val ll_empty_notes = view_timeLine.findViewById<LinearLayout>(R.id.ll_empty_notes)
                val ll_edit_notes = view_timeLine.findViewById<LinearLayout>(R.id.ll_edit_notes)
                val tv_edit_notes = view_timeLine.findViewById<TextInputEditText>(R.id.tv_edit_notes)
                val btn_cancel_save = view_timeLine.findViewById<AppCompatButton>(R.id.btn_cancel_save)
                val btn_create = view_timeLine.findViewById<AppCompatButton>(R.id.btn_create)
                val tv_view_notes = view_timeLine.findViewById<TextInputEditText>(R.id.tv_view_notes)
                val linear_notes = view_timeLine.findViewById<LinearLayout>(R.id.linear_notes)
                val iv_view_timeLine = view_timeLine.findViewById<ImageView>(R.id.iv_view)
                val simple_icon = view_timeLine.findViewById<ImageView>(R.id.simple_icon)
                val normal_notes = view_timeLine.findViewById<TextView>(R.id.normal_notes)

                val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(150))
                tv_edit_notes.filters = filters
                tv_edit_notes.setHint(R.string.notes)
                val iv_edit_notes = view_timeLine.findViewById<ImageView>(R.id.iv_notes)
                val ll_icons = view_timeLine.findViewById<LinearLayout>(R.id.ll_icons)
                val ll_add_notes = view_timeLine.findViewById<LinearLayout>(R.id.ll_add_notes)
                val tv_add_notes = view_timeLine.findViewById<TextView>(R.id.tv_add_notes)
                val et_add_notes = view_timeLine.findViewById<TextInputEditText>(R.id.et_add_notes)
                val btn_add_cancel = view_timeLine.findViewById<Button>(R.id.btn_add_cancel)
                val btn_add_create = view_timeLine.findViewById<Button>(R.id.btn_add_create)
                et_add_notes.filters = filters
                et_add_notes.setHint(R.string.notes)
                val allday = historyList[i].allday
                if (allday) {
                    ll_icons.visibility = View.GONE
                } else {
                    ll_icons.visibility = View.VISIBLE
                }
                val ll_notes_items = view_timeLine.findViewById<LinearLayout>(R.id.ll_notes_items)
                val notes_List = historyList[i].notes_list
                if (notes_List != null && notes_List.length() > 0) {
                    for (j in 0 until notes_List.length()) {
                        val noteObj = notes_List.getJSONObject(j)
                        val noteText = noteObj.optString("notes", "")
                        val addedBy = noteObj.optString("added_by", "")
                        val addOn = noteObj.optString("add_on", "")
                        val firm = noteObj.optString("firm_name", "")

                        val noteItem = LayoutInflater.from(context).inflate(R.layout.timeline_notes_layout, ll_notes_items, false)

                        val person_icon = noteItem.findViewById<TextView>(R.id.person_icon)
                        val person_name = if (addedBy.isNotEmpty()) addedBy.substring(0, 1) else ""
                        person_icon.text = person_name
                        val tvAddedBy = noteItem.findViewById<TextView>(R.id.tv_note_added_by)
                        val tvAddedOn = noteItem.findViewById<TextView>(R.id.tv_note_added_on)
                        val tvNoteFirm = noteItem.findViewById<TextView>(R.id.tv_note_firm)

                        tvAddedBy.text = addedBy
                        tvAddedOn.text = addOn
                        tvNoteFirm.text = noteText

                        ll_notes_items.addView(noteItem)
                    }
                }

                val historyNotes = historyList[i].notes
                if (historyNotes != null && historyNotes.isNotEmpty() && historyNotes != "null") {
                    val notes_text = historyNotes
                    normal_notes.text = "$notes_text...."

                    simple_icon.visibility = View.GONE
                    linear_notes.visibility = View.VISIBLE
                    ll_icons.visibility = View.VISIBLE
                } else {
                    ll_icons.visibility = View.GONE
                    linear_notes.visibility = View.GONE

                    if (historyList[i].from_ts != null
                        && historyList[i].to_ts != null
                        && historyList[i].from_ts == historyList[i].to_ts
                    ) {
                        simple_icon.visibility = View.GONE
                    } else {
                        simple_icon.visibility = View.VISIBLE
                    }

                    normal_notes.text = "...."
                }

                ll_empty_notes.visibility = View.VISIBLE
                normal_notes.visibility = View.VISIBLE

                simple_icon.tag = i
                simple_icon.setOnClickListener { v ->
                    try {
                        var position = 0
                        if (v.tag is Int) {
                            position = v.tag as Int
                            val childView = ll_timeline.getChildAt(position)
                            if (ll_add_notes.visibility == View.VISIBLE) {
                                ll_add_notes.visibility = View.GONE
                            } else {
                                ll_add_notes.visibility = View.VISIBLE
                            }

                            val historyModel = historyList[position]
                            val hNotes = historyModel.notes
                            if (hNotes != null && hNotes.isNotEmpty() && hNotes != "null") {
                                et_add_notes.setText(hNotes)
                            } else {
                                et_add_notes.setText("")
                            }
                            btn_add_create.isEnabled = false
                            btn_add_create.alpha = 0.5f
                            btn_add_cancel.setOnClickListener {
                                ll_edit_notes.visibility = View.GONE
                                tv_view_notes.visibility = View.GONE
                                ll_add_notes.visibility = View.GONE
                                ClearSelectedView(iv_edit_notes, iv_view_timeLine)
                            }
                            et_add_notes.addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                                override fun afterTextChanged(s: Editable) {
                                    if (s.toString().isNotEmpty()) {
                                        btn_add_create.isEnabled = true
                                        btn_add_create.alpha = 1.0f
                                    } else {
                                        btn_add_create.isEnabled = false
                                        btn_add_create.alpha = 0.5f
                                    }
                                }
                            })
                            btn_add_create.setOnClickListener {
                                if (et_add_notes.text.toString().isEmpty()) {
                                    AndroidUtils.showAlert("Please enter the Notes", activity)
                                } else {
                                    ll_add_notes.visibility = View.GONE
                                    dialog.dismiss()
                                    viewMatter.callEditNotesWebservice(historyModel.id ?: "", et_add_notes.text.toString().trim())
                                }
                            }
                        }
                    } catch (e: Exception) {
                        AndroidUtils.showAlert(e.message, activity)
                    }
                }

                val spannableString = SpannableString(normal_notes.text.toString())
                spannableString.setSpan(UnderlineSpan(), 0, normal_notes.length(), 0)
                normal_notes.text = spannableString

                iv_edit_notes.tag = i
                iv_view_timeLine.tag = i
                rb_lauditor_notes.isChecked = true
                rb_lauditor_notes.buttonTintList = ColorStateList.valueOf(resources.getColor(R.color.light_blue))
                rb_lauditor_notes.setTextColor(resources.getColor(R.color.light_blue))

                rb_corporate_notes.isChecked = false
                rb_corporate_notes.buttonTintList = ColorStateList.valueOf(resources.getColor(R.color.black))
                rb_corporate_notes.setTextColor(resources.getColor(R.color.black))

                rb_corporate_notes.setOnClickListener {
                    rb_lauditor_notes.isChecked = false
                    rb_lauditor_notes.setTextColor(resources.getColor(R.color.black))
                    rb_lauditor_notes.buttonTintList = ColorStateList.valueOf(resources.getColor(R.color.black))

                    rb_corporate_notes.buttonTintList = ColorStateList.valueOf(resources.getColor(R.color.light_blue))
                    rb_corporate_notes.setTextColor(resources.getColor(R.color.light_blue))

                    ll_notes_items.visibility = View.VISIBLE
                    linear_notes.visibility = View.GONE
                    simple_icon.visibility = View.GONE
                    ll_empty_notes.visibility = View.GONE
                    ll_add_notes.visibility = View.GONE
                }

                rb_lauditor_notes.tag = i
                rb_corporate_notes.tag = i
                rb_lauditor_notes.setOnClickListener { v ->
                    rb_corporate_notes.isChecked = false
                    rb_corporate_notes.setTextColor(resources.getColor(R.color.black))
                    rb_corporate_notes.buttonTintList = ColorStateList.valueOf(resources.getColor(R.color.black))

                    rb_lauditor_notes.buttonTintList = ColorStateList.valueOf(resources.getColor(R.color.light_blue))
                    rb_lauditor_notes.setTextColor(resources.getColor(R.color.light_blue))
                    linear_notes.visibility = View.VISIBLE
                    ll_notes_items.visibility = View.GONE

                    if (v.tag is Int) {
                        val position = v.tag as Int
                        val hNotes = historyList[position].notes
                        if (hNotes != null && hNotes.isNotEmpty() && hNotes != "null") {
                            val notes_text = hNotes
                            normal_notes.text = "$notes_text...."

                            simple_icon.visibility = View.GONE
                            linear_notes.visibility = View.VISIBLE
                            ll_icons.visibility = View.VISIBLE
                            ll_empty_notes.visibility = View.VISIBLE
                            normal_notes.text = "$notes_text...."
                        } else {
                            ll_icons.visibility = View.GONE
                            linear_notes.visibility = View.GONE

                            if (historyList[position].from_ts != null
                                && historyList[position].to_ts != null
                                && historyList[position].from_ts == historyList[position].to_ts
                            ) {
                                simple_icon.visibility = View.GONE
                            } else {
                                simple_icon.visibility = View.VISIBLE
                            }

                            normal_notes.text = "...."
                        }
                    }
                }

                ClearSelectedView(iv_edit_notes, iv_view_timeLine)
                iv_edit_notes.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.black), PorterDuff.Mode.SRC_IN)
                iv_view_timeLine.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.black), PorterDuff.Mode.SRC_IN)
                iv_edit_notes.setOnClickListener { v ->
                    try {
                        if (v.tag is Int) {
                            val position = v.tag as Int
                            if (ll_edit_notes.visibility == View.VISIBLE) {
                                ClearSelectedView(iv_edit_notes, iv_view_timeLine)
                                ll_empty_notes.visibility = View.VISIBLE
                                ll_edit_notes.visibility = View.GONE
                                tv_view_notes.visibility = View.GONE
                            } else {
                                EditClicked(iv_edit_notes, iv_view_timeLine)
                                ll_empty_notes.visibility = View.GONE
                                ll_edit_notes.visibility = View.VISIBLE
                                tv_view_notes.visibility = View.GONE
                            }

                            val historyModel = historyList[position]
                            val hNotes = historyModel.notes
                            if (hNotes != null && hNotes.isNotEmpty() && hNotes != "null") {
                                tv_edit_notes.setText(hNotes)
                            } else {
                                tv_edit_notes.setText("")
                            }
                            btn_create.isEnabled = false
                            btn_create.alpha = 0.5f
                            tv_edit_notes.addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                                override fun afterTextChanged(s: Editable) {
                                    if (s.toString().isNotEmpty()) {
                                        btn_create.isEnabled = true
                                        btn_create.alpha = 1.0f
                                    } else {
                                        btn_add_create.isEnabled = false
                                        btn_add_create.alpha = 0.5f
                                    }
                                }
                            })
                            btn_cancel_save.setOnClickListener {
                                ll_empty_notes.visibility = View.VISIBLE
                                ll_edit_notes.visibility = View.GONE
                                tv_view_notes.visibility = View.GONE
                                ll_icons.visibility = View.VISIBLE
                                ClearSelectedView(iv_edit_notes, iv_view_timeLine)
                            }
                            btn_create.setOnClickListener {
                                if (tv_edit_notes.text.toString().isEmpty()) {
                                    AndroidUtils.showAlert("Please enter the Notes", activity)
                                    tv_edit_notes.requestFocus()
                                } else {
                                    dialog.dismiss()
                                    viewMatter.callEditNotesWebservice(historyModel.id ?: "", tv_edit_notes.text.toString().trim())
                                }
                            }
                        }
                    } catch (e: Exception) {
                        AndroidUtils.showAlert(e.message, activity)
                    }
                }

                iv_view_timeLine.setOnClickListener { v ->
                    try {
                        if (v.tag is Int) {
                            val position = v.tag as Int
                            if (tv_view_notes.visibility == View.VISIBLE) {
                                ClearSelectedView(iv_edit_notes, iv_view_timeLine)
                                ll_empty_notes.visibility = View.VISIBLE
                                ll_edit_notes.visibility = View.GONE
                                tv_view_notes.visibility = View.GONE
                            } else {
                                ViewClicked(iv_edit_notes, iv_view_timeLine)
                                ll_empty_notes.visibility = View.GONE
                                ll_edit_notes.visibility = View.GONE
                                tv_view_notes.visibility = View.VISIBLE
                            }

                            val historyModel = historyList[position]
                            tv_view_notes.setText(historyModel.notes)
                        }
                    } catch (e: Exception) {
                        AndroidUtils.showAlert(e.message, activity)
                    }
                }

                tv_timeline_title.text = historyList[i].title
                val inputDateStr = historyList[i].from_ts ?: ""

                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
                    val outputFormat = SimpleDateFormat("MMMM dd, yyyy | hh:mm a", Locale.ENGLISH)

                    val date = inputFormat.parse(inputDateStr)
                    if (date != null) {
                        val formattedDate = outputFormat.format(date)
                        tv_timeline_date.text = formattedDate
                    }
                } catch (e: ParseException) {
                    e.printStackTrace()
                }

                ll_timeline.addView(view_timeLine)
            } catch (ex: Exception) {
                AndroidUtils.showAlert(ex.message, activity)
            }
        }

        close_details.setOnClickListener {
            matter?.loadViewUI()
        }

        return view
    }

    private fun ClearSelectedView(iv_edit_notes: ImageView, iv_view_timeLine: ImageView) {
        val ctx = context ?: return
        iv_edit_notes.setColorFilter(ContextCompat.getColor(ctx, android.R.color.black), PorterDuff.Mode.SRC_IN)
        iv_view_timeLine.setColorFilter(ContextCompat.getColor(ctx, android.R.color.black), PorterDuff.Mode.SRC_IN)
    }

    private fun EditClicked(iv_edit_notes: ImageView, iv_view_timeLine: ImageView) {
        val ctx = context ?: return
        iv_edit_notes.setColorFilter(ContextCompat.getColor(ctx, android.R.color.holo_green_dark), PorterDuff.Mode.SRC_IN)
        iv_view_timeLine.setColorFilter(ContextCompat.getColor(ctx, android.R.color.black), PorterDuff.Mode.SRC_IN)
    }

    private fun ViewClicked(iv_edit_notes: ImageView, iv_view_timeLine: ImageView) {
        val ctx = context ?: return
        iv_edit_notes.setColorFilter(ContextCompat.getColor(ctx, android.R.color.black), PorterDuff.Mode.SRC_IN)
        iv_view_timeLine.setColorFilter(ContextCompat.getColor(ctx, android.R.color.holo_green_dark), PorterDuff.Mode.SRC_IN)
    }
}
