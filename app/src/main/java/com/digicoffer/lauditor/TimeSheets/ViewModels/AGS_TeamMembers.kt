package com.digicoffer.lauditor.TimeSheets.ViewModels

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.textfield.TextInputEditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.TimeSheets.Adapters.MonthlyTSAdapter
import com.digicoffer.lauditor.TimeSheets.Adapters.TeamMembersTSAdapter
import com.digicoffer.lauditor.TimeSheets.Models.Month_Model
import com.digicoffer.lauditor.TimeSheets.Models.TMModel
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import java.util.Objects

class AGS_TeamMembers @JvmOverloads constructor(
    var et_search_matter: TextInputEditText? = null,
    var search: String = ""
) : Fragment(), AsyncTaskCompleteListener, View.OnClickListener {

    private var progress_dialog: Dialog? = null
    var month: String? = null
    private val teamList = ArrayList<TMModel>()
    private val monthlist = ArrayList<Month_Model>()
    private var rv_time_sheets: RecyclerView? = null
    private var date: String? = null
    private var isweek: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.team_members_timesheets, container, false)
        val bundle = arguments
        if (bundle != null) {
            date = bundle.getString("date")
            isweek = bundle.getString("isweek")
        }

        rv_time_sheets = view.findViewById(R.id.rv_time_sheets)

        try {
            if (isweek == "month") {
                callTeamMembersWebservice("month")
            } else {
                callTeamMembersWebservice("week")
            }
        } catch (e: ParseException) {
            e.fillInStackTrace()
        }
        return view
    }

    private fun callTeamMembersWebservice(isweek: String) {
        progress_dialog = AndroidUtils.get_progress(requireActivity())
        val postdata = JSONObject()
        if (isweek == "week") {
            if (date == null || date!!.isEmpty()) {
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "matter/timesheets/tms-all-weekly",
                    "Team Members",
                    postdata.toString()
                )
            } else {
                try {
                    val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                    val outputFormat = SimpleDateFormat("ddMMyyyy", Locale.US)
                    val new_date = inputFormat.parse(date!!)
                    val outputDate = outputFormat.format(new_date!!)
                    WebServiceHelper.callHttpWebService(
                        this,
                        requireContext(),
                        WebServiceHelper.RestMethodType.GET,
                        "matter/timesheets/tms-all-weekly-$outputDate",
                        "Team Members",
                        postdata.toString()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            if (date == null || date!!.isEmpty()) {
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "matter/timesheets/tms-all-monthly",
                    "Team Members month",
                    postdata.toString()
                )
            } else {
                try {
                    val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                    val outputFormat = SimpleDateFormat("ddMMyyyy", Locale.US)
                    val new_date = inputFormat.parse(date!!)
                    val outputDate = outputFormat.format(new_date!!)
                    WebServiceHelper.callHttpWebService(
                        this,
                        requireContext(),
                        WebServiceHelper.RestMethodType.GET,
                        "matter/timesheets/tms-all-monthly-$outputDate",
                        "Team Members month",
                        postdata.toString()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onClick(view: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progress_dialog != null && progress_dialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }
        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent)
                if (httpResult.requestType == "Team Members") {
                    val jsonArray = result.getJSONArray("timesheets")
                    loadTmData(jsonArray)
                } else if (httpResult.requestType == "Team Members month") {
                    val jsonArray = result.getJSONArray("timesheets")
                    loadTm_month_Data(jsonArray)
                }
            } catch (e: JSONException) {
                e.fillInStackTrace()
            }
        } else if (httpResult.result == WebServiceHelper.ServiceCallStatus.Failed) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            try {
                val result = JSONObject(httpResult.responseContent)
                AndroidUtils.showErrorAlert(result.optString("msg"), activity)
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        } else {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            AndroidUtils.showErrorAlert(httpResult.responseContent.toString(), activity)
        }
    }

    private fun loadTm_month_Data(jsonArray: JSONArray) {
        Thread {
            try {
                monthlist.clear()
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val tmModel = Month_Model().apply {
                        name = jsonObject.getString("name")
                        billable_week_1 = jsonObject.getString("bw1")
                        billable_week_2 = jsonObject.getString("bw2")
                        billable_week_3 = jsonObject.getString("bw3")
                        billable_week_4 = jsonObject.getString("bw4")
                        billable_week_5 = jsonObject.getString("bw5")
                        billable_week_tot = jsonObject.getString("tb")
                        non_billable_week_1 = jsonObject.getString("nbw1")
                        non_billable_week_2 = jsonObject.getString("nbw2")
                        non_billable_week_3 = jsonObject.getString("nbw3")
                        non_billable_week_4 = jsonObject.getString("nbw4")
                        non_billable_week_5 = jsonObject.getString("nbw5")
                        non_billable_week_tot = jsonObject.getString("tnb")
                        Total = jsonObject.getString("total")
                    }
                    monthlist.add(tmModel)
                }
                Handler(Looper.getMainLooper()).post {
                    try {
                        loadRecyclerview_month()
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    }
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    AndroidUtils.showToast(e.message, context)
                    Log.e("LoadPageException", Objects.requireNonNull(e.message)!!)
                }
            }
        }.start()
    }

    private fun loadRecyclerview_month() {
        val status = "Team Members"
        try {
            if (date == null || date!!.isEmpty()) {
                val new_date = Date()
                val sdf = SimpleDateFormat("MMMM", Locale.getDefault())
                month = sdf.format(new_date)
            } else {
                val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                val new_date = inputFormat.parse(date!!)
                val sdf = SimpleDateFormat("MMMM", Locale.getDefault())
                month = sdf.format(new_date!!)
                Log.d("Month_name.", month!!)
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
        val teamMembersTSAdapter = MonthlyTSAdapter(requireContext(), monthlist, status, month ?: "")
        rv_time_sheets?.adapter = teamMembersTSAdapter
        AndroidUtils.LoadingRecyclerview(rv_time_sheets, requireContext())
        AndroidUtils.setupBottomSpacerFooter(
            rv_time_sheets,
            resources.getDimensionPixelSize(R.dimen.twentyeight_dp)
        )
        if (search.isNotEmpty()) {
            teamMembersTSAdapter.filter.filter(search)
        }
        et_search_matter?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {
                teamMembersTSAdapter.filter.filter(s)
            }
        })
        teamMembersTSAdapter.notifyDataSetChanged()
    }

    private fun loadTmData(jsonArray: JSONArray) {
        try {
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val tmModel = TMModel().apply {
                    if (jsonObject.has("id")) {
                        id = jsonObject.getString("id")
                    }
                    name = jsonObject.getString("name")
                    tb = jsonObject.getString("tb")
                    tnb = jsonObject.getString("tnb")
                    total = jsonObject.getString("total")
                }
                teamList.add(tmModel)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        loadRecyclerview()
    }

    private fun loadRecyclerview() {
        val status = "Team Members"
        val teamMembersTSAdapter = TeamMembersTSAdapter(requireContext(), teamList, status)
        rv_time_sheets?.adapter = teamMembersTSAdapter
        AndroidUtils.LoadingRecyclerview(rv_time_sheets, requireContext())
        AndroidUtils.setupBottomSpacerFooter(
            rv_time_sheets,
            resources.getDimensionPixelSize(R.dimen.twentyeight_dp)
        )
        if (search.isNotEmpty()) {
            teamMembersTSAdapter.filter.filter(search)
        }
        et_search_matter?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {
                teamMembersTSAdapter.filter.filter(s)
            }
        })
        teamMembersTSAdapter.notifyDataSetChanged()
    }
}
