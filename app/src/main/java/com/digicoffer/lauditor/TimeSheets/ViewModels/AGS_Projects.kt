package com.digicoffer.lauditor.TimeSheets.ViewModels

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.TimeSheets.Adapters.ProjectAdapter
import com.digicoffer.lauditor.TimeSheets.Models.ProjectTMModel
import com.digicoffer.lauditor.TimeSheets.Models.ProjectsModel
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

class AGS_Projects : Fragment(), AsyncTaskCompleteListener, View.OnClickListener {
    private var sp_ags_project: ListView? = null
    private var sp_ags_tm: ListView? = null
    private var et_search_matter: TextInputEditText? = null
    private var proj_img_dropdown_icon: ImageView? = null
    private var proj_img_clear_icon: ImageView? = null
    private var tm_img_dropdown_icon: ImageView? = null
    private var tm_img_clear_icon: ImageView? = null
    private var tv_sp_project: TextView? = null
    private var tv_sp_team_member: TextView? = null
    private var project_id: TextView? = null
    private var team_member_id: TextView? = null
    private var date: String? = null
    private var isweek: String? = null
    private var team_member_layout: LinearLayout? = null
    private var llprojectLayout: LinearLayout? = null
    private var projectLayout: LinearLayout? = null
    private var ischecked_project = true
    private var ischecked_team_member = true
    private var rv_projects: RecyclerView? = null
    var projectAdapter: ProjectAdapter? = null
    private var tv_billable_hours: TextView? = null
    private var tv_non_billable_hours: TextView? = null
    private var tv_total_project_hours: TextView? = null
    private var hours_id1: TextView? = null
    private var hours_id2: TextView? = null
    private var non_billable_id: TextView? = null
    private var billable_id: TextView? = null
    private var total_hours_id: TextView? = null
    private var progress_dialog: Dialog? = null
    private val projectsList = ArrayList<ProjectsModel>()
    private val updated_projectList = ArrayList<ProjectsModel>()
    private var selected_project: String? = null
    private val projectTmList = ArrayList<ProjectTMModel>()
    private val updated_projectTmList = ArrayList<ProjectTMModel>()
    private var selected_tm: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.ags_projects, container, false)
        val bundle = arguments
        if (bundle != null) {
            date = bundle.getString("date")
            isweek = bundle.getString("isweek")
        }

        llprojectLayout = view.findViewById(R.id.llprojectLayout)
        sp_ags_project = view.findViewById(R.id.sp_ags_project)
        sp_ags_tm = view.findViewById(R.id.sp_ags_tm)
        sp_ags_project?.visibility = View.GONE
        sp_ags_tm?.visibility = View.GONE

        val project_view = view.findViewById<View>(R.id.tv_sp_project)
        tv_sp_project = project_view.findViewById(R.id.tv_spinner_view)
        proj_img_dropdown_icon = project_view.findViewById(R.id.img_dropdown_icon)
        proj_img_clear_icon = project_view.findViewById(R.id.img_clear_icon)

        val tm_view = view.findViewById<View>(R.id.tv_sp_team_member)
        tv_sp_team_member = tm_view.findViewById(R.id.tv_spinner_view)
        tm_img_dropdown_icon = tm_view.findViewById(R.id.img_dropdown_icon)
        tm_img_clear_icon = tm_view.findViewById(R.id.img_clear_icon)

        project_id = view.findViewById(R.id.project_id)
        projectLayout = view.findViewById(R.id.projectLayout)
        team_member_layout = view.findViewById(R.id.team_member_layout)
        team_member_id = view.findViewById(R.id.team_member_id)

        project_id?.setText(R.string.project)
        team_member_id?.setText(R.string.team_members)
        team_member_layout?.visibility = View.GONE
        val tl_search_matter = view.findViewById<View>(R.id.tl_search_matter)
        et_search_matter = tl_search_matter.findViewById(R.id.et_Search)
        et_search_matter?.setHint(R.string.search)

        rv_projects = view.findViewById(R.id.rv_projects)
        tv_billable_hours = view.findViewById(R.id.tv_billable_hours)
        hours_id1 = view.findViewById(R.id.hours_id1)
        hours_id2 = view.findViewById(R.id.hours_id2)
        non_billable_id = view.findViewById(R.id.non_billable_id)
        billable_id = view.findViewById(R.id.billable_id)
        total_hours_id = view.findViewById(R.id.total_hours_id)
        tv_non_billable_hours = view.findViewById(R.id.tv_non_billable_hours)
        tv_total_project_hours = view.findViewById(R.id.tv_total_project_hours)

        billable_id?.setTextColor(requireContext().getColor(R.color.Blue_text_color))
        non_billable_id?.setTextColor(requireContext().getColor(R.color.Blue_text_color))
        total_hours_id?.setTextColor(requireContext().getColor(R.color.Blue_text_color))
        non_billable_id?.setText(R.string.non_billable)
        billable_id?.setText(R.string.billable)
        total_hours_id?.setText(R.string.total_hours)
        hours_id1?.setText(R.string.hours)
        hours_id2?.setText(R.string.hours)

        project_view.setOnClickListener {
            if (ischecked_project) sp_ags_project?.visibility = View.VISIBLE
            else sp_ags_project?.visibility = View.GONE
            ischecked_project = !ischecked_project
        }

        tm_view.setOnClickListener {
            AndroidUtils.display_listview(ischecked_team_member, sp_ags_tm)
            ischecked_team_member = !ischecked_team_member
        }

        try {
            callProjectsWebService(isweek)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        projectAdapter = ProjectAdapter(ArrayList(), requireContext(), ArrayList())
        rv_projects?.adapter = projectAdapter
        AndroidUtils.LoadingRecyclerview(rv_projects, requireContext())
        AndroidUtils.setupBottomSpacerFooter(
            rv_projects,
            resources.getDimensionPixelSize(R.dimen.twentyeight_dp)
        )
        rv_projects?.setItemViewCacheSize(20)

        et_search_matter?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                projectAdapter?.filter?.filter(s)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        return view
    }

    private fun callProjectsWebService(isweek: String?) {
        progress_dialog = showLoadingDialog(requireActivity())
        val postdata = JSONObject()

        if ("week" == isweek) {
            if (date == null || date!!.isEmpty()) {
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "matter/timesheets/project-all-weekly",
                    "Projects",
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
                        "matter/timesheets/project-all-weekly-$outputDate",
                        "Projects",
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
                    "matter/timesheets/project-all-monthly",
                    "Projects",
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
                        "matter/timesheets/project-all-monthly-$outputDate",
                        "Projects",
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
        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent)
                if ("Projects" == httpResult.requestType) {
                    if (!result.getBoolean("error")) {
                        val jsonObject = result.optJSONObject("timesheets")
                        val grandtotal = jsonObject?.optJSONObject("grandTotal")
                        tv_billable_hours?.text = grandtotal?.optString("billable")
                        tv_non_billable_hours?.text = grandtotal?.optString("nonbillable")
                        tv_total_project_hours?.text = grandtotal?.optString("total") + " Hours"

                        val jsonArray = jsonObject?.optJSONArray("data")
                        if (jsonArray == null || jsonArray.length() == 0) {
                            llprojectLayout?.visibility = View.GONE
                        } else {
                            llprojectLayout?.visibility = View.VISIBLE
                            loadProjectsRecyclerview()
                        }
                        if (jsonArray != null) {
                            loadProjects(jsonArray)
                        }
                    } else {
                        AndroidUtils.showAlert(result.optString("msg"), activity)
                        ischecked_project = true
                        llprojectLayout?.visibility = View.GONE
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
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

    private fun loadProjects(jsonArray: JSONArray) {
        Thread {
            val tempList = ArrayList<ProjectsModel>()
            try {
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val model = ProjectsModel().apply {
                        caseNo = jsonObject.getString("caseNo")
                        projectName = jsonObject.getString("projectName")
                        clientNames = jsonObject.getJSONArray("clientNames")
                        matterId = jsonObject.getString("matterId")
                        teamMembers = jsonObject.getJSONArray("teamMembers")
                    }
                    tempList.add(model)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            Handler(requireContext().mainLooper).post {
                projectsList.clear()
                projectsList.addAll(tempList)

                if (projectsList.isEmpty()) {
                    sp_ags_project?.visibility = View.GONE
                    safeDismissProgressDialog()
                } else {
                    loadProjectsRecyclerview()
                    updated_projectTmList.clear()
                    loadRecyclerview(projectsList, updated_projectTmList)

                    rv_projects?.viewTreeObserver?.addOnGlobalLayoutListener(object :
                        ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            if (rv_projects != null && rv_projects!!.childCount > 0) {
                                safeDismissProgressDialog()
                                rv_projects?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                            }
                        }
                    })
                }
            }
        }.start()
    }

    private fun loadProjectsRecyclerview() {
        val spinner_adapter = CommonSpinnerAdapter(requireActivity(), projectsList)
        sp_ags_project?.adapter = spinner_adapter
        AndroidUtils.LoadList(sp_ags_project, requireContext(), projectsList.size, true)
        sp_ags_project?.setOnItemClickListener { parent, view, position, id ->
            projectTmList.clear()
            updated_projectList.clear()
            selected_project = projectsList[position].matterId
            val selected_project_name = projectsList[position].projectName
            tv_sp_project?.text = selected_project_name
            selected_tm = ""
            tv_sp_team_member?.text = selected_tm

            try {
                for (i in 0 until projectsList.size) {
                    if (projectsList[i].matterId == selected_project) {
                        val teamMembers = projectsList[i].teamMembers
                        if (teamMembers != null) {
                            for (j in 0 until teamMembers.length()) {
                                val jsonObject = teamMembers.getJSONObject(j)
                                val projectTMModel = ProjectTMModel().apply {
                                    billableHours = jsonObject.getString("billableHours")
                                    name = jsonObject.getString("name")
                                    nonBillablehours = jsonObject.getString("nonBillablehours")
                                    total = jsonObject.getString("total")
                                }
                                projectTmList.add(projectTMModel)
                            }
                        }
                    }
                }
                for (projectsModel in projectsList) {
                    if (projectsModel.matterId == selected_project) {
                        updated_projectList.add(projectsModel)
                    }
                }
                loadRecyclerview(updated_projectList, updated_projectTmList)
                tm_img_clear_icon?.visibility = View.GONE
                tm_img_dropdown_icon?.visibility = View.VISIBLE
                team_member_layout?.visibility = if (projectTmList.isEmpty()) View.GONE else View.VISIBLE
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            sp_ags_project?.visibility = View.GONE
            ischecked_project = true
            tv_sp_team_member?.visibility = View.VISIBLE
            proj_img_clear_icon?.visibility = View.VISIBLE
            proj_img_dropdown_icon?.visibility = View.GONE

            val status_adapter = CommonSpinnerAdapter(requireActivity(), projectTmList)
            sp_ags_tm?.adapter = status_adapter
            AndroidUtils.LoadList(sp_ags_tm, requireContext(), projectTmList.size, true)

            sp_ags_tm?.setOnItemClickListener { parent1, view1, position1, id1 ->
                selected_tm = projectTmList[position1].name
                updated_projectTmList.clear()
                tv_sp_team_member?.text = selected_tm
                sp_ags_tm?.visibility = View.GONE
                ischecked_team_member = true
                tm_img_clear_icon?.visibility = View.VISIBLE
                tm_img_dropdown_icon?.visibility = View.GONE

                for (projectTMModel in projectTmList) {
                    if (projectTMModel.name == selected_tm) {
                        updated_projectTmList.add(projectTMModel)
                    }
                }
                loadRecyclerview(updated_projectList, updated_projectTmList)
            }

            tm_img_clear_icon?.setOnClickListener {
                selected_tm = ""
                tv_sp_team_member?.text = selected_tm
                updated_projectTmList.clear()
                ischecked_team_member = true
                tm_img_clear_icon?.visibility = View.GONE
                tm_img_dropdown_icon?.visibility = View.VISIBLE
                loadRecyclerview(updated_projectList, updated_projectTmList)
            }

            proj_img_clear_icon?.setOnClickListener {
                selected_project = ""
                tv_sp_project?.text = ""
                selected_tm = ""
                tv_sp_team_member?.text = selected_tm
                ischecked_project = true
                ischecked_team_member = true
                proj_img_clear_icon?.visibility = View.GONE
                proj_img_dropdown_icon?.visibility = View.VISIBLE
                tm_img_clear_icon?.visibility = View.GONE
                tm_img_dropdown_icon?.visibility = View.VISIBLE
                team_member_layout?.visibility = View.GONE

                if (isAdded) {
                    progress_dialog = showLoadingDialog(requireActivity())
                    Thread {
                        val refreshedList = ArrayList(projectsList)
                        Handler(requireContext().mainLooper).post {
                            projectAdapter?.updateData(refreshedList, ArrayList())
                            projectAdapter?.notifyDataSetChanged()

                            rv_projects?.viewTreeObserver?.addOnGlobalLayoutListener(object :
                                ViewTreeObserver.OnGlobalLayoutListener {
                                override fun onGlobalLayout() {
                                    if (rv_projects != null && rv_projects!!.childCount > 0) {
                                        safeDismissProgressDialog()
                                        rv_projects?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                                    }
                                }
                            })
                        }
                    }.start()
                }
            }
        }
    }

    private fun safeDismissProgressDialog() {
        if (isAdded && progress_dialog != null && progress_dialog!!.isShowing) {
            progress_dialog!!.dismiss()
        }
    }

    private fun loadRecyclerview(
        updated_projectlist: ArrayList<ProjectsModel>,
        updated_projecttmList: ArrayList<ProjectTMModel>
    ) {
        projectAdapter?.updateData(updated_projectlist, updated_projecttmList)
        projectAdapter?.notifyDataSetChanged()
    }

    companion object {
        @JvmStatic
        fun showLoadingDialog(activity: Activity): AlertDialog? {
            var dialog: AlertDialog? = null
            try {
                val builder = AlertDialog.Builder(activity)
                val inflater = activity.layoutInflater
                val view = inflater.inflate(R.layout.loading, null)
                builder.setView(view)
                dialog = builder.create()
                if (dialog.window != null) {
                    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
                dialog.setCancelable(false)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
                val loadingImage = dialog.findViewById<ImageView>(R.id.imageView)
                val rotate = RotateAnimation(
                    0f, 360f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                ).apply {
                    duration = 1000
                    repeatCount = Animation.INFINITE
                    interpolator = LinearInterpolator()
                }
                loadingImage.startAnimation(rotate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return dialog
        }
    }
}
