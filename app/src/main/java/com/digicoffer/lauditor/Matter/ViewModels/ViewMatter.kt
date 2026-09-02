package com.digicoffer.lauditor.Matter.ViewModels

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.Matter.Adapters.ViewMatterAdapter
import com.digicoffer.lauditor.Matter.Models.GroupsModel
import com.digicoffer.lauditor.Matter.Models.HistoryModel
import com.digicoffer.lauditor.Matter.Models.MatterModel
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.Members.Adapters.GroupsAdapter
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.feature.matter.presentation.screen.MatterListingScreen
import com.digicoffer.lauditor.feature.matter.presentation.viewmodel.MatterViewModel
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.Comparator
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.Objects
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class ViewMatter : Fragment(), AsyncTaskCompleteListener, ViewMatterAdapter.InterfaceListener {
    var linear_notes: LinearLayout? = null
    var ll_nav_buttons: LinearLayout? = null
    var tl_search_matter: CardView? = null
    var rv_matter_list: RecyclerView? = null
    var cv_client_details: CardView? = null
    var acls = JSONArray()
    var btn_send_request: Button? = null
    var groupname = ""
    @get:JvmName("getMatter_id_property")
    @set:JvmName("setMatter_id_property")
    var matter_id: String? = null
    var dialog: Dialog? = null
    var newGrouplist = ArrayList<ViewGroupModel>()
    var groups_id = ""
    var OldGroupsList = ArrayList<ViewGroupModel>()
    var viewMatterModel1: ViewMatterModel? = null
    var rv_group_update: RecyclerView? = null
    var et_search_matter: TextInputEditText? = null
    var groupid = ""
    var progressDialog: Dialog? = null
    var Prev_Cursor = ""
    var Next_Cursor = ""
    var btn_Delete: Button? = null
    var btn_prev: AppCompatButton? = null
    var btn_next: AppCompatButton? = null
    var btn_search: AppCompatButton? = null
    var matterList = ArrayList<ViewMatterModel>()
    var historyList = ArrayList<HistoryModel>()
    var groupsArrayList = ArrayList<ViewGroupModel>()
    var originalList = ArrayList<ViewGroupModel>()
    var checkedList = ArrayList<ViewGroupModel>()
    var clients = JSONArray()
    var documents = JSONArray()
    var advocates = JSONArray()
    var members = JSONArray()
    var existing_documents = ArrayList<ViewMatterModel>()
    var timesheets = ArrayList<ViewMatterModel>()
    var existing_members = ArrayList<ViewMatterModel>()
    var groupsAdapter: GroupsAdapter? = null
    var groupsList = ArrayList<ViewGroupModel>()
    var oldGrouplist = ArrayList<ViewGroupModel>()
    var et_search_members: TextInputEditText? = null
    var matter: Matter? = null
    var TimeLineId = ""
    var Header_name = ""
    var Matter_id = ""
    var Matter_Status = ""
    var Matter_Title = ""
    var Case_Number = ""
    var groupsModel: GroupsModel? = null
    private var v: Activity? = null
    var con_id: LinearLayout? = null
    var matterArraylist: ArrayList<MatterModel>? = null

    companion object {
        @JvmField
        var FLAG = ""
    }

    private lateinit var matterViewModel: MatterViewModel

    private fun findParentScrollView(view: View?): android.widget.ScrollView? {
        var current = view
        while (current != null) {
            if (current is android.widget.ScrollView) {
                return current
            }
            current = current.parent as? View
        }
        return null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        matter = parentFragment as? Matter
        matterViewModel = ViewModelProvider(this)[MatterViewModel::class.java]

        val composeView = ComposeView(requireContext()).apply {
            setContent {
                com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme {
                    MatterListingScreen(
                        viewModel = matterViewModel,
                        onEditMatterClick = { model ->
                            com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.Matter_CreateOrViewDetails = "Edit Matter Info"
                            com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.matterDate = model.created
                            Edit_Matter_Info(model)
                        },
                        onViewTimelineClick = { model ->
                            com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.Matter_CreateOrViewDetails = "View Timeline"
                            com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.matterDate = model.created
                            View_Details(model, ArrayList(matterViewModel.uiState.value.matterList))
                        }
                    )
                }
            }
        }

        composeView.post {
            findParentScrollView(composeView)?.isFillViewport = true
        }

        return composeView
    }

    override fun onResume() {
        super.onResume()
        if (::matterViewModel.isInitialized) {
            matterViewModel.fetchMatters()
        }
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    fun callMatterListWebservice() {
        try {
            progressDialog = AndroidUtils.get_progress(activity)
            val postdata = JSONObject()
            val matterType = (Constants.MATTER_TYPE ?: "").lowercase(Locale.ROOT)
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v2/matter/$matterType?paginate=true",
                "Matter List",
                postdata.toString()
            )
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.fillInStackTrace()
        }
    }

    fun callFilteredMatterListWebservice(NavPosition: String, id: String) {
        try {
            val matterType = (Constants.MATTER_TYPE ?: "").lowercase(Locale.ROOT)
            var url = "v2/matter/$matterType?paginate=true"
            if (NavPosition.isNotEmpty()) {
                url = "v2/matter/$matterType?$NavPosition=$id&paginate=true"
            } else if (et_search_matter?.text?.toString()?.isNotEmpty() == true) {
                url = "v2/matter/$matterType?$NavPosition=$id&paginate=true&search=" + et_search_matter?.text?.toString()
            } else {
                url = "v2/matter/$matterType?paginate=true"
            }
            progressDialog = AndroidUtils.get_progress(activity)
            val postdata = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                url,
                "Matter List",
                postdata.toString()
            )
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.fillInStackTrace()
        }
    }

    fun callEditMatterInfo(viewMatterModel: ViewMatterModel) {
        try {
            progressDialog = AndroidUtils.get_progress(activity)
            val postdata = JSONObject()
            val matterType = (Constants.MATTER_TYPE ?: "").lowercase(Locale.ROOT)
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v2/matter/$matterType/${viewMatterModel.id}",
                "Edit Matter",
                postdata.toString()
            )
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.fillInStackTrace()
        }
    }

    override fun onClick(view: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progressDialog != null && progressDialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progressDialog)
        }
        try {
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                val result = JSONObject(httpResult.responseContent)
                if (httpResult.requestType == "Remove Groups") {
                    if (result.has("counts")) {
                        val jsonObject = result.optJSONObject("counts")
                        val doc_count = jsonObject?.optString("documents") ?: ""
                        val rel_count = jsonObject?.optString("relationships") ?: ""

                        loadNewList(groupsArrayList)
                        oldGrouplist.clear()
                        for (i in originalList.indices) {
                            if (!originalList[i].isChecked) {
                                oldGrouplist.add(originalList[i])
                            }
                        }
                        checkedList.clear()
                        for (i in groupsArrayList.indices) {
                            if (groupsArrayList[i].isChecked) {
                                checkedList.add(groupsArrayList[i])
                            }
                        }
                        if (oldGrouplist.isEmpty()) {
                            AndroidUtils.showAlert(
                                "To update matter groups, the client should be linked to more than one group.\nPlease assign the client to more groups.",
                                activity,
                                "Info"
                            )
                        } else {
                            if (newGrouplist.isNotEmpty()) {
                                GroupsAssignPopup(groupname, rel_count, doc_count, newGrouplist)
                            }
                        }
                    }
                } else if (httpResult.requestType == "Alter Groups") {
                    try {
                        val successmsg = result.getString("msg")
                        AndroidUtils.showAlert(successmsg, activity)

                        loadNewList(groupsArrayList)
                        for (group in groupsArrayList) {
                            for (checked in checkedList) {
                                if (group.group_id == checked.group_id) {
                                    group.isChecked = true
                                    group.can_delete = false
                                    break
                                }
                            }
                        }

                        for (group in groupsArrayList) {
                            if (group.group_id == groups_id) {
                                group.isChecked = false
                                group.can_delete = true
                                continue
                            }
                            if (isGroupInNewList(group.group_id)) {
                                group.isChecked = true
                                group.can_delete = false
                            }
                        }

                        groupsAdapter?.notifyDataSetChanged()
                        callUpdateGroupsWebservice(groupsArrayList)

                    } catch (e: Exception) {
                        AndroidUtils.showAlert(e.message, activity)
                    }
                } else {
                    val error = result.getBoolean("error")
                    if (httpResult.requestType == "Groups") {
                        val data = result.getJSONArray("groups")
                        loadGroupsData(data)
                    } else if (httpResult.requestType == "Matter List") {
                        if (error) {
                            val msg = result.getString("msg")
                            AndroidUtils.showAlert(msg, activity)
                        } else {
                            Prev_Cursor = result.optString("prev_cursor")
                            Next_Cursor = result.optString("next_cursor")
                            if (Prev_Cursor.isEmpty() || Prev_Cursor == "null") {
                                AndroidUtils.ToggleButton(0, btn_prev)
                            } else {
                                AndroidUtils.ToggleButton(1, btn_prev)
                            }
                            if (Next_Cursor.isEmpty() || Next_Cursor == "null") {
                                AndroidUtils.ToggleButton(0, btn_next)
                            } else {
                                AndroidUtils.ToggleButton(1, btn_next)
                            }
                            val matters = result.optJSONArray("matters")
                            try {
                                if (matters != null) {
                                    loadMattersList(matters)
                                }
                            } catch (e: Exception) {
                                AndroidUtils.showAlert(e.message, activity)
                                e.fillInStackTrace()
                            }
                        }
                    } else if (httpResult.requestType == "Edit Matter") {
                        if (error) {
                            val msg = result.getString("msg")
                            AndroidUtils.showAlert(msg, activity)
                        } else {
                            val matters = result.optJSONObject("matter")
                            try {
                                if (matters != null) {
                                    loadeditmatter(matters)
                                }
                            } catch (e: Exception) {
                                AndroidUtils.showAlert(e.message, activity)
                                e.fillInStackTrace()
                            }
                        }
                    } else if (httpResult.requestType == "TimeLine") {
                        if (error) {
                            val msg = result.getString("msg")
                            AndroidUtils.showAlert(msg, activity)
                        } else {
                            loadHistory(result)
                        }
                    } else if (httpResult.requestType == "Notes") {
                        historyList.clear()
                        val iserror = result.optBoolean("error")
                        val updatemsg = result.getString("msg")
                        if (!iserror) {
                            AndroidUtils.showAlert(updatemsg, activity)
                            callTimeLineWebservice()
                        } else {
                            AndroidUtils.showAlert(updatemsg, activity)
                        }
                    } else if (httpResult.requestType == "Update Groups") {
                        val iserror = result.optBoolean("error")
                        if (!iserror) {
                            val updatemsg = result.getString("msg")
                            if (!Constants.isAlterPopup) {
                                AndroidUtils.showAlert(updatemsg, activity)
                            }
                            Log.d("successmsg", updatemsg)
                            groupsAdapter?.notifyDataSetChanged()
                            load_selected_groups(groupsArrayList)
                            Constants.isAlterPopup = false
                            callFilteredMatterListWebservice("", "")
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), activity)
                        }
                    } else if (httpResult.requestType == "Update Matter" || httpResult.requestType == "matter_update") {
                        val msg = result.getString("msg")
                        Log.d("Message", msg)
                        if (error) {
                            AndroidUtils.showAlert(msg, activity)
                        } else {
                            rv_matter_list?.removeAllViews()
                            callFilteredMatterListWebservice("", "")
                            AndroidUtils.showAlert(msg, activity, "Success")
                        }
                    } else if (httpResult.requestType == "Delete Matter") {
                        val msg = result.getString("msg")
                        if (error) {
                            AndroidUtils.showAlert(msg, activity)
                        } else {
                            rv_matter_list?.removeAllViews()
                            callFilteredMatterListWebservice("", "")
                            AndroidUtils.showAlert(msg, activity, "Success")
                        }
                    }
                }
            } else if (httpResult.result == WebServiceHelper.ServiceCallStatus.Failed) {
                try {
                    val result = JSONObject(httpResult.responseContent)
                    if (result.optBoolean("error")) {
                        AndroidUtils.showErrorAlert(result.optString("msg"), activity)
                    }
                } catch (e: Exception) {
                    e.fillInStackTrace()
                }
            } else {
                AndroidUtils.showErrorAlert(httpResult.responseContent.toString(), activity)
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
    }

    private fun loadNewList(originalList: ArrayList<ViewGroupModel>) {
        newGrouplist.clear()
        for (group in originalList) {
            if (group.group_id != groups_id) {
                newGrouplist.add(group)
            }
        }
    }

    private fun isGroupInNewList(groupId: String): Boolean {
        for (model in newGrouplist) {
            if (model.group_id == groupId && model.isChecked) {
                return true
            }
        }
        return false
    }

    private fun loadGroupsData(data: JSONArray) {
        try {
            Constants.groupsList_Access.clear()
            for (i in 0 until data.length()) {
                val jsonObject = data.getJSONObject(i)
                val groupsModel = GroupsModel()
                groupsModel.group_id = jsonObject.getString("id")
                groupsModel.group_name = jsonObject.getString("name")
                if (jsonObject.optString("name") != "AAM" && jsonObject.optString("name") != "SuperUser") {
                    Constants.groupsList_Access.add(groupsModel)
                }
            }
            if (viewMatterModel1 != null) {
                load_groups_view(viewMatterModel1!!)
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    private fun openViewGroupsPopup() {
        val builder = AlertDialog.Builder(context)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.update_groups_popup, null)
        val header = view.findViewById<TextView>(R.id.header_name)
        header.text = (viewMatterModel1?.title ?: "") + " - Update Group(S)"
        val ll_groups = view.findViewById<LinearLayout>(R.id.ll_groups)
        rv_group_update = view.findViewById(R.id.rv_group_update)
        val btn_cancel_save = view.findViewById<AppCompatButton>(R.id.btn_cancel_save)
        val btn_create = view.findViewById<AppCompatButton>(R.id.btn_create)
        btn_create.setText(R.string.update)
        btn_create.isEnabled = false
        btn_create.alpha = 0.5f
        btn_send_request = btn_create
        val close_details = view.findViewById<ImageView>(R.id.close_details)
        et_search_members = view.findViewById(R.id.et_search_members)
        et_search_members?.setHint(R.string.groups)
        et_search_members?.addTextChangedListener(Validation(et_search_members))
        val chk_select_all = view.findViewById<CheckBox>(R.id.chk_select_all)
        Constants.isAlterPopup = false
        groupsAdapter = GroupsAdapter(groupsArrayList, this)
        rv_group_update?.layoutManager = LinearLayoutManager(context)
        rv_group_update?.adapter = groupsAdapter
        et_search_members?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                groupsAdapter?.filter?.filter(s)
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
        rv_group_update?.refreshDrawableState()

        val dialog = builder.create()
        btn_cancel_save.setOnClickListener {
            groupsArrayList.clear()
            dialog.dismiss()
        }
        btn_create.setOnClickListener {
            dialog.dismiss()
            callUpdateGroupsWebservice(groupsArrayList)
        }
        close_details.setOnClickListener {
            groupsArrayList.clear()
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.setView(view)
        dialog.show()
    }

    fun loadNewGroups(list_item: ArrayList<ViewGroupModel>) {
        val acls = JSONArray()
        if (list_item.isNotEmpty()) {
            for (i in list_item.indices) {
                val viewGroupModel = list_item[i]
                if (viewGroupModel.isChecked) {
                    acls.put(viewGroupModel.id)
                }
            }
        }
        originalList.clear()
        originalList.addAll(list_item)
        if (acls.length() == 0) {
            btn_Delete?.alpha = 0.5f
            btn_Delete?.isEnabled = false
        } else {
            btn_Delete?.alpha = 1.0f
            btn_Delete?.isEnabled = true
        }
    }

    fun checkRemoveGroups(groups_id: String, groupname: String, groupsList: ArrayList<ViewGroupModel>) {
        try {
            this.groupname = groupname
            this.groups_id = groups_id
            OldGroupsList = groupsList
            progressDialog = AndroidUtils.get_progress(activity)
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/matter/groups/$Matter_id/$groups_id",
                "Remove Groups",
                jsonObject.toString()
            )
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun GroupsAssignPopup(
        group_name: String,
        rel_count: String,
        doc_count: String,
        groupsList1: ArrayList<ViewGroupModel>
    ) {
        try {
            val dialogBuilder = AlertDialog.Builder(context)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.assign_to_other_group, null)

            val tv_update_group = view.findViewById<TextView>(R.id.tv_update_group)
            tv_update_group.setText(R.string.update_group)

            btn_Delete = view.findViewById(R.id.Delete)
            btn_Delete?.setText(R.string.delete)
            btn_Delete?.alpha = 0.5f
            btn_Delete?.isEnabled = false

            val tv_warning_msg = view.findViewById<TextView>(R.id.tv_warning_msg)
            val tv_assign_group = view.findViewById<TextView>(R.id.tv_assign_group)
            tv_assign_group.setAutoSizeTextTypeUniformWithConfiguration(1, 16, 1, TypedValue.COMPLEX_UNIT_SP)
            tv_assign_group.setText(R.string.assign_to_another_groups)
            val rv_groups_view = view.findViewById<RecyclerView>(R.id.rv_groups_view)

            val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            rv_groups_view.layoutManager = layoutManager
            Constants.isAlterPopup = true
            if (groupsList1.isNotEmpty()) {
                val groupsList = ArrayList<ViewGroupModel>()
                for (i in groupsList1.indices) {
                    val viewGroupModel = groupsList1[i]
                    viewGroupModel.isChecked = false
                    groupsList.add(viewGroupModel)
                }
            }
            val groupsAdapter1 = GroupsAdapter(groupsList1, this)
            rv_groups_view.adapter = groupsAdapter1
            val et_search_members = view.findViewById<TextInputEditText>(R.id.tv_search_groups)
            et_search_members.addTextChangedListener(Validation(et_search_members))

            val groupNameStr = SpannableString("'$group_name'")
            groupNameStr.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireActivity(), R.color.black)), 0, groupNameStr.length, 0)
            groupNameStr.setSpan(StyleSpan(Typeface.BOLD), 0, groupNameStr.length, 0)
            groupNameStr.setSpan(AbsoluteSizeSpan(18, true), 0, groupNameStr.length, 0)

            val msgBuilder = SpannableStringBuilder()
            msgBuilder.append("This ")
                .append(groupNameStr)
                .append(" group currently contains ")

            val documentText = SpannableString("$doc_count Documents. ")
            documentText.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireActivity(), R.color.Blue_text_color)), 0, documentText.length, 0)
            msgBuilder.append(documentText)
            msgBuilder.append("Before updating, please assign them to another group.")
            tv_warning_msg.setText(msgBuilder, TextView.BufferType.SPANNABLE)

            val btn_Cancel = view.findViewById<AppCompatButton>(R.id.Cancel)
            dialog = progressDialog

            btn_Cancel.setOnClickListener {
                progressDialog?.dismiss()
                Constants.isAlterPopup = false
                rv_groups_view.removeAllViews()
                unCheckList()
            }
            val iv_close = view.findViewById<ImageView>(R.id.iv_close)
            iv_close.setOnClickListener {
                progressDialog?.dismiss()
                Constants.isAlterPopup = false
                rv_groups_view.removeAllViews()
                unCheckList()
            }
            btn_Delete?.setOnClickListener {
                Constants.isAlterPopup = true
                progressDialog?.dismiss()
                AlterGroups()
            }
            et_search_members.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    groupsAdapter1.filter.filter(s)
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable) {}
            })
            rv_groups_view.refreshDrawableState()

            val dialog = dialogBuilder.create()
            progressDialog = dialog
            dialog.setView(view)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    private fun unCheckList() {
        try {
            for (group in groupsArrayList) {
                var shouldBeChecked = false
                for (checked in checkedList) {
                    if (group.group_id == checked.group_id) {
                        shouldBeChecked = true
                        break
                    }
                }
                group.isChecked = shouldBeChecked
            }
            groupsAdapter?.notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e("unCheckList", "Error updating checked states: " + e.message)
        }
    }

    fun AlterGroups() {
        try {
            val jsonObject = JSONObject()
            if (newGrouplist.isNotEmpty()) {
                for (i in newGrouplist.indices) {
                    val viewGroupModel = newGrouplist[i]
                    if (viewGroupModel.isChecked) {
                        acls.put(viewGroupModel.group_id)
                    }
                }
            }
            jsonObject.put("new_groups", acls)
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.PATCH,
                "v3/matter/groups/$Matter_id/$groups_id",
                "Alter Groups",
                jsonObject.toString()
            )
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    fun load_selected_groups(list_item: ArrayList<ViewGroupModel>) {
        val acls = JSONArray()
        if (list_item.isNotEmpty()) {
            for (i in list_item.indices) {
                val viewGroupModel = list_item[i]
                if (viewGroupModel.isChecked) {
                    acls.put(viewGroupModel.id)
                }
            }
        }
        if (acls.length() == 0) {
            btn_send_request?.alpha = 0.5f
            btn_send_request?.isEnabled = false
        } else {
            btn_send_request?.alpha = 1.0f
            btn_send_request?.isEnabled = true
        }
    }

    private fun loadGroupsRecylerview() {
        FLAG = "second_click"
        rv_group_update?.layoutManager = GridLayoutManager(context, 1)
        groupsAdapter = GroupsAdapter(groupsList)
        rv_group_update?.adapter = groupsAdapter
        et_search_members?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                groupsAdapter?.filter?.filter(et_search_members?.text.toString())
            }
        })
    }

    private fun callUpdateGroupsWebservice(groupsList: ArrayList<ViewGroupModel>) {
        progressDialog = AndroidUtils.get_progress(activity)
        try {
            val postdata = JSONObject()
            val group_acls = JSONArray()
            for (i in groupsList.indices) {
                val viewGroupModel = groupsList[i]
                if (viewGroupModel.isChecked) {
                    group_acls.put(groupsList[i].group_id)
                }
            }
            if (group_acls.length() == 0) {
                AndroidUtils.showAlert("Please select atleast one group", activity)
            } else {
                postdata.put("group_acls", group_acls)
                val matterType = (Constants.MATTER_TYPE ?: "").lowercase(Locale.ROOT)
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.PUT,
                    "matter/$matterType/$Matter_id/acls",
                    "Update Groups",
                    postdata.toString()
                )
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun loadHistory(result: JSONObject) {
        try {
            historyList.clear()
            val id = result.optString("id")
            val title = result.optString("title")
            val jsonArray = result.optJSONArray("history")
            if (jsonArray != null) {
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.optJSONObject(i) ?: continue
                    val historyModel = HistoryModel()
                    if (jsonObject.has("allday")) {
                        historyModel.allday = jsonObject.getBoolean("allday")
                    } else {
                        historyModel.allday = true
                    }
                    historyModel.id = jsonObject.optString("id")
                    historyModel.description = jsonObject.optString("description")
                    historyModel.event_type = jsonObject.optString("event_type")
                    historyModel.from_ts = jsonObject.optString("from_ts")
                    historyModel.to_ts = jsonObject.optString("to_ts")
                    historyModel.title = jsonObject.optString("title")
                    historyModel.notes = jsonObject.optString("notes")
                    historyModel.notes_list = jsonObject.optJSONArray("notes_list") ?: JSONArray()
                    historyList.add(historyModel)
                }
            }
        } catch (e: JSONException) {
            AndroidUtils.showAlert(e.message, activity)
            e.fillInStackTrace()
        }
        if (matter != null && viewMatterModel1 != null) {
            matter!!.View_Details(historyList, this, Header_name, viewMatterModel1!!)
            con_id?.visibility = View.GONE
        }
    }

    fun openViewDetailsPopUp() {
        try {
            if (viewMatterModel1 != null) {
                matter?.View_Details(viewMatterModel1!!, this, historyList, Header_name)
            }
            con_id?.visibility = View.GONE
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    fun openTimeline() {
        try {
            if (viewMatterModel1 != null) {
                matter?.View_Details(viewMatterModel1!!, this, historyList, Header_name)
            }
            con_id?.visibility = View.GONE
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    fun callEditNotesWebservice(id: String, notes: String) {
        progressDialog = AndroidUtils.get_progress(activity)
        try {
            val postdata = JSONObject()
            postdata.put("notes", notes)
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.PUT,
                "event/notes/$id",
                "Notes",
                postdata.toString()
            )
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
        }
    }

    private fun loadeditmatter(jsonObject: JSONObject) {
        try {
            val viewMatterModel = ViewMatterModel()
            viewMatterModel.id = jsonObject.optString("id")
            Constants.Matter_id = ""
            Constants.Matter_id = viewMatterModel.id ?: ""
            if (jsonObject.has("caseNumber")) {
                viewMatterModel.caseNumber = jsonObject.optString("caseNumber")
            }
            if (jsonObject.has("caseType")) {
                viewMatterModel.casetype = jsonObject.optString("caseType")
            }
            val clientIds = JSONArray()
            val clientsArray = jsonObject.optJSONArray("clients") ?: JSONArray()

            if (clientsArray.length() > 0) {
                for (i in 0 until clientsArray.length()) {
                    val clientObject = clientsArray.optJSONObject(i)
                    if (clientObject != null) {
                        val clientId = clientObject.optString("id", "")
                        clientIds.put(clientId)
                    }
                }
            }

            viewMatterModel.clients = clientsArray

            if (jsonObject.has("corporate")) {
                var CorpclientId = ""
                val CorpclientsArray = jsonObject.optJSONArray("corporate") ?: JSONArray()

                if (CorpclientsArray.length() > 0) {
                    for (i in 0 until CorpclientsArray.length()) {
                        val clientObject = CorpclientsArray.optJSONObject(i)
                        if (clientObject != null) {
                            CorpclientId = clientObject.optString("id", "")
                        }
                    }
                }
                viewMatterModel.corporate = CorpclientsArray
                viewMatterModel.corpId = CorpclientId
                clientIds.put(CorpclientId)
            }

            viewMatterModel.created = jsonObject.optString("created_on")

            viewMatterModel.clients_list = clientIds
            if (jsonObject.has("courtName")) {
                viewMatterModel.courtName = jsonObject.optString("courtName")
            }
            if (jsonObject.has("isdisabled")) {
                viewMatterModel.isdisabled = jsonObject.optBoolean("isdisabled")
            }
            if (jsonObject.has("date_of_filling")) {
                viewMatterModel.date_of_filling = AndroidUtils.formatToMMMddYYYY(jsonObject.optString("date_of_filling"))
            }
            if (jsonObject.has("closedate")) {
                viewMatterModel.closedate = AndroidUtils.formatToMMMddYYYY(jsonObject.optString("closedate"))
            }
            if (jsonObject.has("matterNumber")) {
                viewMatterModel.matterNumber = jsonObject.optString("matterNumber")
            }
            if (jsonObject.has("matterType")) {
                viewMatterModel.matterType = jsonObject.optString("matterType")
            }
            if (jsonObject.has("owner")) {
                val jsonObject1 = jsonObject.optJSONObject("owner")
                if (jsonObject1 != null) {
                    Constants.owner_id = jsonObject1.optString("id")
                    Constants.owner_name = jsonObject1.optString("name")
                }
            }
            if (jsonObject.has("startdate")) {
                viewMatterModel.startdate = AndroidUtils.formatToMMMddYYYY(jsonObject.optString("startdate"))
            }
            if (jsonObject.has("timesheets")) {
                viewMatterModel.timesheets = jsonObject.optJSONArray("timesheets") ?: JSONArray()
            }
            if (jsonObject.has("created_date")) {
                viewMatterModel.created_date = AndroidUtils.formatToMMMddYYYY(jsonObject.optString("created_date"))
            }
            if (jsonObject.has("created_date")) {
                viewMatterModel.created = AndroidUtils.formatToMMMddYYYY(jsonObject.optString("created_date"))
            }
            if (jsonObject.has("matter_id")) {
                viewMatterModel.matter_id = jsonObject.optString("matter_id")
            }
            viewMatterModel.description = jsonObject.optString("description")
            viewMatterModel.documents = jsonObject.optJSONArray("documents") ?: JSONArray()
            viewMatterModel.groupAcls = jsonObject.optJSONArray("groupAcls") ?: JSONArray()
            viewMatterModel.groups = jsonObject.optJSONArray("groups") ?: JSONArray()
            if (jsonObject.has("hearingDateDetails")) {
                viewMatterModel.hearingDateDetails = jsonObject.optJSONObject("hearingDateDetails") ?: JSONObject()
            }
            viewMatterModel.is_editable = jsonObject.optBoolean("is_editable")
            if (jsonObject.has("judges")) {
                viewMatterModel.judges = jsonObject.optString("judges")
            }
            if (jsonObject.has("matterClosedDate")) {
                viewMatterModel.matterClosedDate = jsonObject.optString("matterClosedDate")
            }
            viewMatterModel.members = jsonObject.optJSONArray("members") ?: JSONArray()
            if (jsonObject.has("nextHearingDate")) {
                viewMatterModel.nextHearingDate = jsonObject.optString("nextHearingDate")
            }
            if (jsonObject.has("opponentAdvocates")) {
                viewMatterModel.opponentAdvocates = jsonObject.optJSONArray("opponentAdvocates") ?: JSONArray()
            }
            viewMatterModel.owner = jsonObject.optJSONObject("owner") ?: JSONObject()
            viewMatterModel.priority = jsonObject.optString("priority")
            viewMatterModel.status = jsonObject.optString("status")
            val tagsObject = jsonObject.optJSONObject("tags")
            val tagsArray = JSONArray()
            if (tagsObject != null) {
                val keys = tagsObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    try {
                        tagsArray.put(tagsObject.getString(key))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
            viewMatterModel.tags_list = tagsArray
            if (jsonObject.has("tempClients")) {
                viewMatterModel.tempClients = jsonObject.optJSONArray("tempClients") ?: JSONArray()
            }
            if (jsonObject.has("timesheets")) {
                viewMatterModel.timesheets = jsonObject.optJSONArray("timesheets") ?: JSONArray()
            }
            viewMatterModel.temporaryClients = jsonObject.optJSONArray("temporaryClients") ?: JSONArray()
            viewMatterModel.title = jsonObject.optString("title")
            viewMatterModel1 = viewMatterModel
            if (Constants.Matter_CreateOrViewDetails == "View Timeline") {
                openViewDetailsPopUp()
            } else if (Constants.Matter_CreateOrViewDetails == "Close/Reopen") {
                Close_Matter(viewMatterModel)
            } else {
                callTimeLineWebservice()
            }
        } catch (e: JSONException) {
            AndroidUtils.showAlert(e.message, activity)
            e.fillInStackTrace()
        }
    }

    private fun loadMattersList(matters: JSONArray) {
        try {
            matterList.clear()
            for (i in 0 until matters.length()) {
                val jsonObject = matters.optJSONObject(i) ?: continue

                val viewMatterModel = ViewMatterModel()
                viewMatterModel.id = jsonObject.optString("id")
                viewMatterModel.created = jsonObject.optString("created_on")
                if (jsonObject.has("caseNumber")) {
                    viewMatterModel.caseNumber = jsonObject.optString("caseNumber")
                }
                if (jsonObject.has("caseType")) {
                    viewMatterModel.casetype = jsonObject.optString("caseType")
                }
                viewMatterModel.clients = jsonObject.optJSONArray("clients") ?: JSONArray()
                if (jsonObject.has("corporate")) {
                    viewMatterModel.corporate = jsonObject.optJSONArray("corporate") ?: JSONArray()
                }

                val client = viewMatterModel.clients
                val clientNamesList = ArrayList<String>()

                if (client.length() > 0) {
                    for (j in 0 until client.length()) {
                        val item = client.get(j)
                        if (item is JSONArray) {
                            val clientArray = item
                            for (k in 0 until clientArray.length()) {
                                if (!clientArray.isNull(k)) {
                                    val clientObj = clientArray.optJSONObject(k)
                                    if (clientObj != null && clientObj.has("name") && clientObj.optString("name").isNotEmpty()) {
                                        clientNamesList.add(clientObj.optString("name"))
                                    }
                                }
                            }
                        } else if (item is JSONObject) {
                            val clientObj = item
                            if (clientObj.has("name") && clientObj.optString("name").isNotEmpty()) {
                                clientNamesList.add(clientObj.optString("name"))
                            }
                        }
                    }
                }

                val corp = viewMatterModel.corporate
                if (clientNamesList.isEmpty() && corp.length() > 0) {
                    for (j in 0 until corp.length()) {
                        val corpClient = corp.optJSONObject(j)
                        if (corpClient != null) {
                            val corpName = corpClient.optString("name")
                            if (corpName.isNotEmpty()) {
                                clientNamesList.add(corpName)
                            }
                        }
                    }
                }

                val client_name = TextUtils.join(", ", clientNamesList)
                viewMatterModel.client_name = client_name
                if (jsonObject.has("courtName")) {
                    viewMatterModel.courtName = jsonObject.optString("courtName")
                }
                if (jsonObject.has("isdisabled")) {
                    viewMatterModel.isdisabled = jsonObject.optBoolean("isdisabled")
                }
                if (jsonObject.has("date_of_filling")) {
                    viewMatterModel.date_of_filling = AndroidUtils.formatToMMMddYYYY(jsonObject.optString("date_of_filling"))
                }
                if (jsonObject.has("closedate")) {
                    viewMatterModel.closedate = AndroidUtils.formatToMMMddYYYY(jsonObject.optString("closedate"))
                }
                if (jsonObject.has("matterNumber")) {
                    viewMatterModel.matterNumber = jsonObject.optString("matterNumber")
                }
                if (jsonObject.has("matterType")) {
                    viewMatterModel.matterType = jsonObject.optString("matterType")
                }
                if (jsonObject.has("startdate")) {
                    viewMatterModel.startdate = AndroidUtils.formatToMMMddYYYY(jsonObject.optString("startdate"))
                }
                if (jsonObject.has("timesheets")) {
                    viewMatterModel.timesheets = jsonObject.optJSONArray("timesheets") ?: JSONArray()
                }
                if (jsonObject.has("created_date")) {
                    viewMatterModel.created_date = AndroidUtils.formatToMMMddYYYY(jsonObject.optString("created_date"))
                }
                if (jsonObject.has("matter_id")) {
                    viewMatterModel.matter_id = jsonObject.optString("matter_id")
                }
                viewMatterModel.description = jsonObject.optString("description")
                viewMatterModel.documents = jsonObject.optJSONArray("documents") ?: JSONArray()
                viewMatterModel.groupAcls = jsonObject.optJSONArray("groupAcls") ?: JSONArray()
                viewMatterModel.groups = jsonObject.optJSONArray("groups") ?: JSONArray()
                if (jsonObject.has("hearingDateDetails")) {
                    viewMatterModel.hearingDateDetails = jsonObject.optJSONObject("hearingDateDetails") ?: JSONObject()
                }
                viewMatterModel.is_editable = jsonObject.optBoolean("is_editable")
                if (jsonObject.has("judges")) {
                    viewMatterModel.judges = jsonObject.optString("judges")
                }
                if (jsonObject.has("matterClosedDate")) {
                    viewMatterModel.matterClosedDate = jsonObject.optString("matterClosedDate")
                }
                viewMatterModel.members = jsonObject.optJSONArray("members") ?: JSONArray()
                if (jsonObject.has("nextHearingDate")) {
                    viewMatterModel.nextHearingDate = jsonObject.optString("nextHearingDate")
                }
                if (jsonObject.has("opponentAdvocates")) {
                    viewMatterModel.opponentAdvocates = jsonObject.optJSONArray("opponentAdvocates") ?: JSONArray()
                }
                viewMatterModel.owner = jsonObject.optJSONObject("owner") ?: JSONObject()
                val owner = viewMatterModel.owner
                if (owner.length() != 0) {
                    val owner_name = owner.optString("name")
                    val owner_id = owner.optString("id")
                    viewMatterModel.owner_name = owner_name
                } else {
                    viewMatterModel.owner_name = " "
                }
                viewMatterModel.priority = jsonObject.optString("priority")
                viewMatterModel.status = jsonObject.optString("status")
                val tagsObject = jsonObject.optJSONObject("tags")
                val tagsArray = JSONArray()
                if (tagsObject != null) {
                    val keys = tagsObject.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        try {
                            tagsArray.put(tagsObject.getString(key))
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
                viewMatterModel.tags_list = tagsArray
                if (jsonObject.has("tempClients")) {
                    viewMatterModel.tempClients = jsonObject.optJSONArray("tempClients") ?: JSONArray()
                }
                if (jsonObject.has("timesheets")) {
                    viewMatterModel.timesheets = jsonObject.optJSONArray("timesheets") ?: JSONArray()
                }
                viewMatterModel.temporaryClients = jsonObject.optJSONArray("temporaryClients") ?: JSONArray()
                viewMatterModel.title = jsonObject.optString("title")

                matterList.add(viewMatterModel)
            }

            try {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
                Collections.sort(matterList, Comparator { o1, o2 ->
                    try {
                        val d1 = dateFormat.parse(o1.created)
                        val d2 = dateFormat.parse(o2.created)
                        if (d1 != null && d2 != null) {
                            return@Comparator d2.compareTo(d1)
                        }
                        0
                    } catch (e: ParseException) {
                        0
                    }
                })
            } catch (e: Exception) {
                e.fillInStackTrace()
            }

            if (matter != null) {
                if (matterList.isEmpty()) {
                    matter!!.showEmptyState(false)
                } else {
                    matter!!.hideEmptyState()
                    loadMatterRecyclerview()
                }
            } else {
                loadMatterRecyclerview()
            }

        } catch (e: JSONException) {
            AndroidUtils.showAlert(e.message, activity)
            e.fillInStackTrace()
        }
    }

    private fun loadMatterRecyclerview() {
        try {
            rv_matter_list?.removeAllViews()
            val viewMatterAdapter = ViewMatterAdapter(matterList, requireContext(), this)
            rv_matter_list?.adapter = viewMatterAdapter
            rv_matter_list?.let { AndroidUtils.LoadingRecyclerview(it, context) }
            rv_matter_list?.let { AndroidUtils.setupBottomSpacerFooter(it, resources.getDimensionPixelSize(R.dimen.twentyeight_dp)) }
            rv_matter_list?.let { viewMatterAdapter.setRecyclerView(it) }
            viewMatterAdapter.filter.filter(et_search_matter?.text?.toString() ?: "")
            et_search_matter?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.isEmpty()) {
                        callFilteredMatterListWebservice("", "")
                    }
                }
                override fun afterTextChanged(s: Editable) {}
            })
            btn_search?.setOnClickListener {
                callFilteredMatterListWebservice("", "")
            }
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    private fun callTimeLineWebservice() {
        try {
            progressDialog = AndroidUtils.get_progress(activity)
            val postdata = JSONObject()
            val calendar: Calendar = GregorianCalendar()
            val timeZone = calendar.timeZone
            val offset = timeZone.rawOffset
            val hours = TimeUnit.MILLISECONDS.toMinutes(offset.toLong())
            val timezoneoffset = -1 * hours
            val matterType = (Constants.MATTER_TYPE ?: "").lowercase(Locale.ROOT)
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "matter/$matterType/$TimeLineId/history/$timezoneoffset",
                "TimeLine",
                postdata.toString()
            )
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.fillInStackTrace()
        }
    }

    override fun DeleteMatter(viewMatterModel: ViewMatterModel, itemsArrayList: ArrayList<ViewMatterModel>) {
        try {
            val dialogBuilder = AlertDialog.Builder(activity)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.delete_relationship, null)
            val tv_confirmation = view.findViewById<TextView>(R.id.tv_confirmation)
            val close_details = view.findViewById<ImageView>(R.id.close_documents)
            tv_confirmation.text = "Are you sure you want to Delete Document?"
            val bt_yes = view.findViewById<AppCompatButton>(R.id.btn_yes)
            val btn_no = view.findViewById<AppCompatButton>(R.id.btn_No)
            val dialog = dialogBuilder.create()
            close_details.setOnClickListener {
                groupsArrayList.clear()
                dialog.dismiss()
            }
            btn_no.setOnClickListener { dialog.dismiss() }
            bt_yes.setOnClickListener {
                dialog.dismiss()
                callDeleteMatterWebService(viewMatterModel)
            }
            dialog.setView(view)
            dialog.show()
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    override fun Edit_Matter_Info(viewMatterModel: ViewMatterModel) {
        matter_id = viewMatterModel.id
        TimeLineId = viewMatterModel.id ?: ""
        Header_name = viewMatterModel.title ?: ""
        Constants.Matter_title = viewMatterModel.title ?: ""
        viewMatterModel1 = viewMatterModel
        if (Constants.Matter_CreateOrViewDetails == "Edit Matter Info") {
            callTimeLineWebservice()
        } else {
            callEditMatterInfo(viewMatterModel)
        }
    }

    private fun callDeleteMatterWebService(viewMatterModel: ViewMatterModel) {
        progressDialog = AndroidUtils.get_progress(activity)
        try {
            val postdata = JSONObject()
            val matterType = (Constants.MATTER_TYPE ?: "").lowercase(Locale.ROOT)
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.DELETE,
                "matter/$matterType/delete/${viewMatterModel.id}",
                "Delete Matter",
                postdata.toString()
            )
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.fillInStackTrace()
        }
    }

    @SuppressLint("ResourceType")
    override fun View_Details(viewMatterModel: ViewMatterModel, itemsArrayList: ArrayList<ViewMatterModel>) {
        TimeLineId = viewMatterModel.id ?: ""
        Header_name = viewMatterModel.title ?: ""
        Constants.Matter_title = ""
        Constants.Matter_title = viewMatterModel.title ?: ""
        matter_id = viewMatterModel.id
        callEditMatterInfo(viewMatterModel)
    }

    fun callGroupsWebservice(viewMatterModel: ViewMatterModel) {
        try {
            viewMatterModel1 = viewMatterModel
            progressDialog = AndroidUtils.get_progress(activity)
            val postdata = JSONObject()
            val clients = JSONArray()
            val client = viewMatterModel.clients
            var client_value = JSONObject()
            if (client != null && client.length() > 0) {
                for (i in 0 until client.length()) {
                    client_value = client.getJSONObject(i)
                    if (client_value.has("id")) {
                        val jsonObject = JSONObject()
                        jsonObject.put("id", client_value.getString("id"))
                        jsonObject.put("name", client_value.getString("name"))
                        jsonObject.put("type", client_value.getString("type"))
                        clients.put(jsonObject)
                    }
                }
            }
            val corp = viewMatterModel.corporate
            if (corp != null && corp.length() > 0) {
                for (i in 0 until corp.length()) {
                    val CorpClient_value = corp.getJSONObject(i)
                    if (CorpClient_value.has("id")) {
                        val jsonObject = JSONObject()
                        jsonObject.put("id", CorpClient_value.getString("id"))
                        jsonObject.put("name", CorpClient_value.getString("name"))
                        jsonObject.put("type", CorpClient_value.getString("type"))
                        clients.put(jsonObject)
                    }
                }
            }
            postdata.put("attachment_type", "groups")
            postdata.put("clients", clients)
            postdata.put("mode", "edit")
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.PUT,
                "matter/attachments",
                "Groups",
                postdata.toString()
            )
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    override fun Update_Group(viewMatterModel: ViewMatterModel) {
        callGroupsWebservice(viewMatterModel)
    }

    private fun load_groups_view(viewMatterModel: ViewMatterModel) {
        groupsArrayList.clear()
        originalList.clear()
        checkedList.clear()
        Matter_id = viewMatterModel.id ?: ""
        for (j in Constants.groupsList_Access.indices) {
            val fullGroupList = Constants.groupsList_Access[j]
            val viewgroypModel = ViewGroupModel()
            viewgroypModel.group_id = fullGroupList.group_id ?: ""
            viewgroypModel.group_name = fullGroupList.group_name ?: ""
            viewgroypModel.name = fullGroupList.group_name ?: ""
            viewgroypModel.isChecked = false
            val groups = viewMatterModel.groups
            if (groups != null) {
                for (i in 0 until groups.length()) {
                    val jsonObject = groups.optJSONObject(i)
                    if (jsonObject != null && jsonObject.optString("id").equals(viewgroypModel.group_id, ignoreCase = true)) {
                        viewgroypModel.isChecked = true
                        viewgroypModel.can_delete = false
                    }
                }
            }
            groupsArrayList.add(viewgroypModel)
            originalList.add(viewgroypModel)
            if (viewgroypModel.isChecked) {
                checkedList.add(viewgroypModel)
            }
        }
        openViewGroupsPopup()
    }

    private fun callgroupsWebservice(groupsArrayList: ArrayList<ViewGroupModel>) {
        progressDialog = AndroidUtils.get_progress(activity)
        try {
            val postdata = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/groups",
                "Groups",
                postdata.toString()
            )
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, activity)
            e.fillInStackTrace()
        }
    }

    override fun Close_Matter(viewMatterModel: ViewMatterModel) {
        clients = JSONArray()
        members = JSONArray()
        documents = JSONArray()
        advocates = JSONArray()

        try {
            val mems = viewMatterModel.members
            if (mems != null) {
                for (i in 0 until mems.length()) {
                    val tm = mems.getJSONObject(i)
                    val member = JSONObject()
                    member.put("id", tm.getString("id"))
                    members.put(member)
                }
            }
            val docs = viewMatterModel.documents
            if (docs != null) {
                for (i in 0 until docs.length()) {
                    val tm = docs.getJSONObject(i)
                    val doc = JSONObject()
                    doc.put("docid", tm.getString("docid"))
                    doc.put("doctype", tm.getString("doctype"))
                    doc.put("user_id", tm.getString("user_id"))
                    documents.put(doc)
                }
            }
            val advs = viewMatterModel.opponentAdvocates
            if (advs != null) {
                for (i in 0 until advs.length()) {
                    val tm = advs.getJSONObject(i)
                    val advocate = JSONObject()
                    advocate.put("name", tm.get("name"))
                    advocate.put("email", tm.get("email"))
                    advocate.put("phone", tm.get("phone"))
                    advocates.put(advocate)
                }
            }
            val clis = viewMatterModel.clients
            if (clis != null) {
                for (i in 0 until clis.length()) {
                    val tm = clis.getJSONObject(i)
                    val client = JSONObject()
                    client.put("id", tm.get("id"))
                    client.put("type", tm.get("type"))
                    clients.put(client)
                }
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
        openCloseMatterPopup(viewMatterModel)
    }

    private fun openCloseMatterPopup(viewMatterModel: ViewMatterModel) {
        try {
            val dialogBuilder = AlertDialog.Builder(activity)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.delete_relationship, null)
            val header_name = view.findViewById<TextView>(R.id.header_name)
            header_name.setTextColor(Color.BLACK)
            val close_documents = view.findViewById<ImageView>(R.id.close_documents)
            val tv_confirmation = view.findViewById<TextView>(R.id.tv_confirmation)
            val reopen_msg = "Are you sure you want to reopen this matter?"
            val close_msg = "Are you sure you want to close this matter?"
            if (viewMatterModel.status == "Closed") {
                tv_confirmation.text = reopen_msg
                Matter_Status = "Active"
            } else {
                tv_confirmation.text = close_msg
                Matter_Status = "Closed"
            }
            val bt_yes = view.findViewById<AppCompatButton>(R.id.btn_yes)
            val btn_no = view.findViewById<AppCompatButton>(R.id.btn_No)
            val dialog = dialogBuilder.create()
            btn_no.setOnClickListener { dialog.dismiss() }
            close_documents.setOnClickListener { dialog.dismiss() }
            bt_yes.setOnClickListener {
                try {
                    dialog.dismiss()
                    callCloseMatterWebService(viewMatterModel, Matter_Status)
                } catch (e: Exception) {
                    e.fillInStackTrace()
                    AndroidUtils.showAlert(e.message, activity)
                }
            }
            dialog.setView(view)
            dialog.show()
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    private fun callCloseMatterWebService(viewMatterModel: ViewMatterModel, matter_Status: String) {
        progressDialog = AndroidUtils.get_progress(activity)
        try {
            val postdata = JSONObject()
            postdata.put("status", matter_Status)
            val matterType = (Constants.MATTER_TYPE ?: "").lowercase(Locale.ROOT)
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.PATCH,
                "v2/matter/$matterType/${viewMatterModel.id}",
                "matter_update",
                postdata.toString()
            )
            Log.d("Update_Matter", postdata.toString())
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
                AndroidUtils.showAlert(e.message, activity)
            }
            e.fillInStackTrace()
        }
    }

    override fun ReopenMatter(viewMatterModel: ViewMatterModel) {}
}
