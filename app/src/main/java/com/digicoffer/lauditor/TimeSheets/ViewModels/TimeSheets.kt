package com.digicoffer.lauditor.TimeSheets.ViewModels

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.TimeSheets.Models.DateModel
import com.digicoffer.lauditor.TimeSheets.Models.WeekDateInfo
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Disabled_view
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale
import java.util.Objects

class TimeSheets : Fragment() {
    private var mViewModel: NewModel? = null
    var tv_aggregated_ts: TextView? = null
    var tv_my_ts: TextView? = null
    var tv_ns_timesheet: TextView? = null
    var tv_submitted: TextView? = null
    var tv_week: TextView? = null
    var tv_month: TextView? = null
    var from_id: TextView? = null
    var to_id: TextView? = null
    var tv_to_date_timesheet: TextView? = null
    var tv_from_date_timesheet: TextView? = null
    var ll_timesheet_type: LinearLayoutCompat? = null
    var ll_submitted_type: LinearLayoutCompat? = null
    var ll_week_month: LinearLayoutCompat? = null
    var s = ""
    var endDate: String? = null
    var startDate: String? = null
    var isweek = "week"
    private val datesList = ArrayList<DateModel>()
    var calendar_week: Calendar = Calendar.getInstance()
    var calendar_month: Calendar = Calendar.getInstance()
    var weekDateInfo: WeekDateInfo? = null
    var tl_search_matter: View? = null
    var et_search_matter: TextInputEditText? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.timesheet, container, false)
        mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
        mViewModel?.setData(getString(R.string.time_sheets))
        tl_search_matter = view.findViewById(R.id.tl_search_matter)
        et_search_matter = tl_search_matter?.findViewById(R.id.et_Search)
        ll_week_month = view.findViewById(R.id.ll_week_month)
        et_search_matter?.setHint(R.string.type_to_search)
        et_search_matter?.addTextChangedListener(Validation(et_search_matter))
        ll_submitted_type = view.findViewById(R.id.ll_submitted_type)
        ll_timesheet_type = view.findViewById(R.id.ll_timesheet_type)
        tv_aggregated_ts = view.findViewById(R.id.tv_aggregated_ts)
        from_id = view.findViewById(R.id.from_id)
        to_id = view.findViewById(R.id.to_id)
        tv_aggregated_ts?.setText(R.string.aggregated_timesheets)
        tv_my_ts = view.findViewById(R.id.tv_my_ts)
        from_id?.setText(R.string.from)
        from_id?.setTextColor(requireContext().getColor(R.color.Blue_text_color))
        to_id?.setText(R.string.to)
        to_id?.setTextColor(requireContext().getColor(R.color.Blue_text_color))
        tv_my_ts?.setText(R.string.my_timesheets)
        tv_ns_timesheet = view.findViewById(R.id.tv_ns_timesheet)
        tv_ns_timesheet?.setText(R.string.team_members)
        tv_submitted = view.findViewById(R.id.tv_submitted)
        tv_submitted?.setText(R.string.projects)
        tv_week = view.findViewById(R.id.tv_week)
        tv_week?.setText(R.string.week)
        tv_month = view.findViewById(R.id.tv_month)
        tv_month?.setText(R.string.month)

        tv_from_date_timesheet = view.findViewById(R.id.tv_from_date_timesheet)
        tv_from_date_timesheet?.maxLines = 1
        tv_from_date_timesheet?.textSize = DynamicUtils.fifteen.toFloat()
        tv_from_date_timesheet?.setAutoSizeTextTypeUniformWithConfiguration(
            1,
            15,
            1,
            TypedValue.COMPLEX_UNIT_SP
        )
        tv_to_date_timesheet = view.findViewById(R.id.tv_to_date_timesheet)
        tv_to_date_timesheet?.textSize = DynamicUtils.fifteen.toFloat()
        tv_to_date_timesheet?.maxLines = 1
        tv_to_date_timesheet?.setAutoSizeTextTypeUniformWithConfiguration(
            1,
            15,
            1,
            TypedValue.COMPLEX_UNIT_SP
        )

        if (isweek == "month") {
            month_range(calendar_month)
            tv_from_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(startDate)
            tv_to_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(endDate)
        } else {
            weekDateInfo = getWeekDateRange(calendar_week)
            if (weekDateInfo != null && weekDateInfo!!.weekDates != null && weekDateInfo!!.weekDates!!.isNotEmpty()) {
                tv_from_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(weekDateInfo!!.weekDates!![0])
                tv_to_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(
                    weekDateInfo!!.weekDates!![weekDateInfo!!.weekDates!!.size - 1]
                )
            }
        }
        if (Constants.ROLE == "SU" || Constants.ROLE == "GH") {
            main_button_status = "MyTimeSheets"
            non_main_button_status = "NS"
            ll_timesheet_type?.visibility = View.GONE
        } else {
            ll_timesheet_type?.visibility = View.GONE
            main_button_status = "MyTimeSheets"
            tl_search_matter?.visibility = View.GONE
            non_main_button_status = "NS"
        }
        if (Constants.ts_card_clicked) {
            if (Constants.Timesheet_Card == "Agts") {
                if (Constants.ROLE == "SU" || Constants.ROLE == "GH") {
                    non_main_button_status = "TM"
                    loadAggregatedTimesheets(
                        AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet?.text.toString()),
                        weekDateInfo
                    )
                } else {
                    non_main_button_status = "NS"
                    loadMyTimeSheets(
                        AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet?.text.toString()),
                        weekDateInfo
                    )
                }
            } else {
                main_button_status = "MyTimeSheets"
                if (Constants.is_ts_submitted) {
                    non_main_button_status = "SU"
                    loadMyTimeSheets(
                        AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet?.text.toString()),
                        weekDateInfo
                    )
                } else {
                    non_main_button_status = "NS"
                    loadMyTimeSheets(
                        AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet?.text.toString()),
                        weekDateInfo
                    )
                }
            }
        } else {
            non_main_button_status = "NS"
            loadMyTimeSheets(
                AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet?.text.toString()),
                weekDateInfo
            )
        }
        val iv_next_week = view.findViewById<ImageButton>(R.id.iv_next_week)
        iv_next_week.setImageDrawable(requireContext().getDrawable(R.drawable.baseline_arrow_forward_ios_24))
        val iv_previous_week = view.findViewById<ImageButton>(R.id.iv_previous_week)

        tv_aggregated_ts?.setOnClickListener {
            main_button_status = "Aggregated"
            if (isweek == "month") {
                month_range(calendar_month)
            } else {
                weekDateInfo = getWeekDateRange(calendar_week)
            }
            loadAggregatedTimesheets(
                AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet?.text.toString()),
                weekDateInfo
            )
        }
        tv_my_ts?.setOnClickListener {
            main_button_status = "MyTimeSheets"
            loadMyTimeSheets(
                AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet?.text.toString()),
                weekDateInfo
            )
        }
        tv_submitted?.setOnClickListener {
            if (tv_submitted?.text.toString() == "Projects") {
                non_main_button_status = "Project"
                mViewModel?.setData(getString(R.string.aggregated_timesheets))
            } else {
                mViewModel?.setData(getString(R.string.time_submit))
                non_main_button_status = "Submitted"
            }
            loadSubmittedTimesheets(
                AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet?.text.toString()),
                weekDateInfo
            )
        }
        tv_ns_timesheet?.setOnClickListener {
            if (tv_ns_timesheet?.text.toString() == "Team Members") {
                mViewModel?.setData(getString(R.string.aggregated_timesheets))
                non_main_button_status = "TM"
            } else {
                mViewModel?.setData(getString(R.string.time_sheet_entry))
                non_main_button_status = "NS"
            }
            loadNsTimesheets(
                AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet?.text.toString()),
                weekDateInfo
            )
        }
        tv_week?.setOnClickListener {
            et_search_matter?.setText("")
            calendar_week = Calendar.getInstance()
            loadWeek()
        }
        tv_month?.setOnClickListener {
            et_search_matter?.setText("")
            loadMonth()
        }
        iv_next_week.setOnClickListener {
            try {
                if (isweek == "month") {
                    calendar_month.add(Calendar.MONTH, 1)
                    month_range(calendar_month)
                    tv_from_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(startDate)
                    tv_to_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(endDate)
                } else {
                    calendar_week.add(Calendar.WEEK_OF_YEAR, 1)
                    weekDateInfo = getWeekDateRange(calendar_week)
                    if (weekDateInfo != null && weekDateInfo!!.weekDates != null && weekDateInfo!!.weekDates!!.isNotEmpty()) {
                        tv_from_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(weekDateInfo!!.weekDates!![0])
                        tv_to_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(
                            weekDateInfo!!.weekDates!![weekDateInfo!!.weekDates!!.size - 1]
                        )
                    }
                }
                loadFragment(
                    AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet?.text.toString()),
                    weekDateInfo
                )
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        }

        tv_to_date_timesheet?.setOnClickListener {
            if (main_button_status != "MyTimeSheets") {
                return@setOnClickListener
            }
            calendar_week = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar_week.set(Calendar.YEAR, year)
                    calendar_week.set(Calendar.MONTH, month)
                    calendar_week.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    weekDateInfo = getWeekDateRange(calendar_week)
                    if (weekDateInfo != null && weekDateInfo!!.weekDates != null && weekDateInfo!!.weekDates!!.isNotEmpty()) {
                        tv_from_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(weekDateInfo!!.weekDates!![0])
                        tv_to_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(
                            weekDateInfo!!.weekDates!![weekDateInfo!!.weekDates!!.size - 1]
                        )
                    }
                    loadFragment(
                        AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet?.text.toString()),
                        weekDateInfo
                    )
                },
                calendar_week.get(Calendar.YEAR),
                calendar_week.get(Calendar.MONTH),
                calendar_week.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        tv_from_date_timesheet?.setOnClickListener {
            if (main_button_status != "MyTimeSheets") {
                return@setOnClickListener
            }
            calendar_week = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar_week.set(Calendar.YEAR, year)
                    calendar_week.set(Calendar.MONTH, month)
                    calendar_week.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    weekDateInfo = getWeekDateRange(calendar_week)
                    if (weekDateInfo != null && weekDateInfo!!.weekDates != null && weekDateInfo!!.weekDates!!.isNotEmpty()) {
                        tv_from_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(weekDateInfo!!.weekDates!![0])
                        tv_to_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(
                            weekDateInfo!!.weekDates!![weekDateInfo!!.weekDates!!.size - 1]
                        )
                    }
                    loadFragment(
                        AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet?.text.toString()),
                        weekDateInfo
                    )
                },
                calendar_week.get(Calendar.YEAR),
                calendar_week.get(Calendar.MONTH),
                calendar_week.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        iv_previous_week.setOnClickListener {
            if (isweek == "month") {
                calendar_month.add(Calendar.MONTH, -1)
                month_range(calendar_month)
                tv_from_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(startDate)
                tv_to_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(endDate)
            } else {
                calendar_week.add(Calendar.WEEK_OF_YEAR, -1)
                weekDateInfo = getWeekDateRange(calendar_week)
                if (weekDateInfo != null && weekDateInfo!!.weekDates != null && weekDateInfo!!.weekDates!!.isNotEmpty()) {
                    tv_from_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(weekDateInfo!!.weekDates!![0])
                    tv_to_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(
                        weekDateInfo!!.weekDates!![weekDateInfo!!.weekDates!!.size - 1]
                    )
                }
            }
            loadFragment(
                AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet?.text.toString()),
                weekDateInfo
            )
        }
        return view
    }

    private fun loadAggregatedTimesheets(s: String, weekDateInfo: WeekDateInfo?) {
        tv_month?.isEnabled = true
        tv_aggregated_ts?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_left_green_background))
        tv_aggregated_ts?.setTextColor(Color.WHITE)
        tv_my_ts?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_right_background))
        tv_my_ts?.setTextColor(Color.BLACK)
        tv_ns_timesheet?.visibility = View.VISIBLE
        tv_ns_timesheet?.setText(R.string.team_members)
        tv_submitted?.setText(R.string.projects)

        if ("solo" != Constants.CATEGORY) {
            if (non_main_button_status == "TM" || non_main_button_status == "NS") {
                loadTMFragment(s, isweek)
                Log.d("ssssss", s)
            } else {
                loadProjectFragment(s, weekDateInfo, isweek)
            }
        } else {
            non_main_button_status = "Project"
            tv_ns_timesheet?.visibility = View.GONE
            loadProjectFragment(s, weekDateInfo, isweek)
            tv_submitted?.setTextColor(Color.WHITE)
            tv_submitted?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.rounder_button_green))
        }
        if (isweek == "month") {
            month_ui()
        } else {
            week_ui()
        }
        mViewModel?.setData(getString(R.string.aggregated_timesheets))
    }

    private fun loadMyTimeSheets(s: String, weekDateInfo: WeekDateInfo?) {
        tv_month?.isEnabled = false
        tv_aggregated_ts?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_left_background))
        tv_aggregated_ts?.setTextColor(Color.BLACK)
        tv_my_ts?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_right_green_count))
        tv_my_ts?.setTextColor(Color.WHITE)
        tv_ns_timesheet?.setText(R.string.not_submitted)
        tv_submitted?.setText(R.string.submitted)
        loadWeek()
        tv_ns_timesheet?.visibility = View.VISIBLE

        if (non_main_button_status == "NS" || non_main_button_status == "TM") {
            loadNsTimesheets(s, weekDateInfo)
            Log.d("ssssss", s)
        } else {
            loadSubmittedTimesheets(s, weekDateInfo)
        }
        week_ui()
        mViewModel?.setData(getString(R.string.time_sheet_entry))
    }

    private fun loadNsTimesheets(s: String, weekDateInfo: WeekDateInfo?) {
        tv_ns_timesheet?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_left_green_background))
        tv_ns_timesheet?.setTextColor(Color.WHITE)
        tv_submitted?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_right_background))
        tv_submitted?.setTextColor(Color.BLACK)
        if (tv_ns_timesheet?.text.toString() == "Not Submitted") {
            loadNsFragment(s, weekDateInfo)
        } else {
            loadTMFragment(s, isweek)
        }
    }

    private fun week_ui() {
        if (tv_ns_timesheet?.text.toString() == "Not Submitted") {
            tv_week?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.rectangular_button_green_count))
            tv_week?.setTextColor(Color.WHITE)
        } else {
            ll_week_month?.visibility = View.VISIBLE
            tv_from_date_timesheet?.setBackground(requireContext().getDrawable(R.drawable.background_transparent))
            tv_to_date_timesheet?.setBackground(requireContext().getDrawable(R.drawable.background_transparent))
            tv_month?.visibility = View.VISIBLE
            tv_week?.setTextColor(Color.WHITE)
            tv_week?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_left_green_background))
            tv_month?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_right_background))
            tv_month?.setTextColor(Color.BLACK)
        }
    }

    private fun loadWeek() {
        week_ui()
        isweek = "week"
        if (isweek == "month") {
            month_range(calendar_month)
            tv_from_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(startDate)
            tv_to_date_timesheet?.text = endDate
        } else {
            weekDateInfo = getWeekDateRange(calendar_week)
            if (weekDateInfo != null && weekDateInfo!!.weekDates != null && weekDateInfo!!.weekDates!!.isNotEmpty()) {
                tv_from_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(weekDateInfo!!.weekDates!![0])
                tv_to_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(
                    weekDateInfo!!.weekDates!![weekDateInfo!!.weekDates!!.size - 1]
                )
            }
        }
        if (tv_ns_timesheet?.text.toString() == "Not Submitted") {
            // no-op
        } else {
            if (non_main_button_status == "TM" || non_main_button_status == "NS") {
                loadTMFragment(
                    AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet?.text.toString()),
                    isweek
                )
                Log.d("ssssss", tv_from_date_timesheet?.text.toString())
            } else {
                loadProjectFragment(
                    AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet?.text.toString()),
                    weekDateInfo,
                    isweek
                )
            }
        }
    }

    private fun month_ui() {
        tv_week?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_left_background))
        tv_week?.setTextColor(Color.BLACK)
        tv_month?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_right_green_count))
        tv_month?.setTextColor(Color.WHITE)
    }

    private fun loadMonth() {
        month_ui()
        calendar_month = Calendar.getInstance()
        isweek = "month"
        if (isweek == "month") {
            month_range(calendar_month)
            tv_from_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(startDate)
            tv_to_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(endDate)
        } else {
            weekDateInfo = getWeekDateRange(calendar_week)
            if (weekDateInfo != null && weekDateInfo!!.weekDates != null && weekDateInfo!!.weekDates!!.isNotEmpty()) {
                tv_from_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(weekDateInfo!!.weekDates!![0])
                tv_to_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(
                    weekDateInfo!!.weekDates!![weekDateInfo!!.weekDates!!.size - 1]
                )
            }
        }
        if (non_main_button_status == "TM" || non_main_button_status == "NS") {
            loadTMFragment(
                AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet?.text.toString()),
                isweek
            )
            Log.d("ssssss", tv_from_date_timesheet?.text.toString())
        } else {
            loadProjectFragment(
                AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet?.text.toString()),
                weekDateInfo,
                isweek
            )
        }
    }

    private fun loadSubmittedTimesheets(s: String, weekDateInfo: WeekDateInfo?) {
        tv_submitted?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_right_green_count))
        tv_submitted?.setTextColor(Color.WHITE)
        tv_ns_timesheet?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_left_background))
        tv_ns_timesheet?.setTextColor(Color.BLACK)
        loadSubmittedFragment(s, weekDateInfo)
    }

    private fun loadSubmittedFragment(s: String, weekDateInfo: WeekDateInfo?) {
        if (tv_submitted?.text.toString() == "Submitted") {
            ll_week_month?.visibility = View.GONE
            tv_from_date_timesheet?.setBackground(requireContext().getDrawable(R.drawable.light_grey_bg))
            tv_to_date_timesheet?.setBackground(requireContext().getDrawable(R.drawable.light_grey_bg))
            tv_month?.visibility = View.GONE
            val bundle = Bundle().apply {
                putString("date", s)
                putStringArrayList("weekDates", weekDateInfo?.weekDates)
            }
            val ft = childFragmentManager.beginTransaction()
            val nonSubmittedTimesheets = SubmittedTimeSheets().apply {
                arguments = bundle
            }
            ft.replace(R.id.child_container_timesheets, nonSubmittedTimesheets)
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            ft.addToBackStack(null)
            ft.commit()
        } else {
            loadProjectFragment(s, weekDateInfo, isweek)
        }
    }

    private fun loadFragment(s: String, weekDateInfo: WeekDateInfo?) {
        if (main_button_status == "Aggregated") {
            loadAggregatedTimesheets(s, weekDateInfo)
        } else {
            loadMyTimeSheets(s, weekDateInfo)
        }
    }

    fun Frozen_view() {
        main_button_status = "MyTimeSheets"
        non_main_button_status = "NS"
        val ft = childFragmentManager.beginTransaction()
        val frozenText = getString(R.string.timesheet_already_submitted_please_select_other_week)
        val disabled_view = Disabled_view(frozenText, false)
        ft.add(R.id.child_container_timesheets, disabled_view)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.addToBackStack(null)
        ft.commit()
    }

    private fun load_month(s: String) {
        val bundle = Bundle().apply {
            putString("date", s)
        }
        val ft = childFragmentManager.beginTransaction()
        val nonSubmittedTimesheets = AGS_TeamMembers(et_search_matter, et_search_matter?.text.toString()).apply {
            arguments = bundle
        }
        ft.replace(R.id.child_container_timesheets, nonSubmittedTimesheets)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.addToBackStack(null)
        ft.commit()
    }

    private fun loadTMFragment(s: String, isweek: String) {
        val bundle = Bundle().apply {
            putString("date", s)
            putString("isweek", isweek)
        }
        tv_ns_timesheet?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_left_green_background))
        tv_ns_timesheet?.setTextColor(Color.WHITE)
        tv_submitted?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_right_background))
        tv_submitted?.setTextColor(Color.BLACK)
        tl_search_matter?.visibility = View.VISIBLE
        val ft = childFragmentManager.beginTransaction()
        val nonSubmittedTimesheets = AGS_TeamMembers(et_search_matter, et_search_matter?.text.toString()).apply {
            arguments = bundle
        }
        ft.replace(R.id.child_container_timesheets, nonSubmittedTimesheets)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.addToBackStack(null)
        ft.commit()
    }

    private fun loadNsFragment(s: String, weekDateInfo: WeekDateInfo?) {
        tl_search_matter?.visibility = View.GONE
        val bundle = Bundle().apply {
            putString("date", s)
            putStringArrayList("weekDates", weekDateInfo?.weekDates)
        }
        ll_week_month?.visibility = View.GONE
        tv_from_date_timesheet?.setBackground(requireContext().getDrawable(R.drawable.light_grey_bg))
        tv_to_date_timesheet?.setBackground(requireContext().getDrawable(R.drawable.light_grey_bg))
        tv_month?.visibility = View.GONE
        tv_week?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.rectangular_button_green_count))
        val ft = childFragmentManager.beginTransaction()
        val nonSubmittedTimesheets = NonSubmittedTimesheets(null).apply {
            arguments = bundle
        }
        ft.replace(R.id.child_container_timesheets, nonSubmittedTimesheets)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.addToBackStack(null)
        ft.commit()
    }

    fun loadProjectFragment(s: String, weekDateInfo: WeekDateInfo?, isweek: String) {
        et_search_matter?.visibility = View.GONE
        ll_week_month?.visibility = View.VISIBLE
        tv_from_date_timesheet?.setBackground(requireContext().getDrawable(R.drawable.background_transparent))
        tv_to_date_timesheet?.setBackground(requireContext().getDrawable(R.drawable.background_transparent))
        tv_month?.visibility = View.VISIBLE
        val bundle = Bundle().apply {
            putString("date", s)
            putString("isweek", isweek)
            putStringArrayList("weekDates", weekDateInfo?.weekDates)
        }
        val ft = childFragmentManager.beginTransaction()
        val nonSubmittedTimesheets = AGS_Projects().apply {
            arguments = bundle
        }
        ft.replace(R.id.child_container_timesheets, nonSubmittedTimesheets)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.addToBackStack(null)
        ft.commit()
    }

    private fun month_range(month_calendar: Calendar) {
        val format = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        month_calendar.set(Calendar.DAY_OF_MONTH, 1)
        startDate = format.format(month_calendar.time)
        Log.d("stttt+date", startDate!!)

        month_calendar.set(Calendar.DAY_OF_MONTH, month_calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        endDate = format.format(month_calendar.time)
        Log.d("edddd+date", endDate!!)
    }

    private fun getWeekDateRange(calendar: Calendar): WeekDateInfo {
        val format = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DATE, -1)
        }
        val startDate = format.format(calendar.time)
        val weekDates = ArrayList<String>()
        for (i in 0..6) {
            val date = format.format(calendar.time)
            weekDates.add(date)
            calendar.add(Calendar.DATE, 1)
        }
        calendar.add(Calendar.DATE, -1)
        val endDate = format.format(calendar.time)
        return WeekDateInfo("$startDate - $endDate", weekDates)
    }

    companion object {
        @JvmField
        var main_button_status: String = ""

        @JvmField
        var non_main_button_status: String = ""
    }
}
