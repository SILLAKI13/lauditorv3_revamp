package com.digicoffer.lauditor.Members

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.Members.Adapters.GroupsAdapter
import com.digicoffer.lauditor.Members.Adapters.MembersAdapter
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Disabled_view
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import java.util.Date

class Members : Fragment(), AsyncTaskCompleteListener, MembersAdapter.EventListener, View.OnClickListener {
    var tv_member_name: TextView? = null
    var frozen_PageText: TextView? = null
    var tv_designation: TextView? = null
    var tv_email: TextView? = null
    var tv_confirm_email: TextView? = null
    var tv_create_members: TextView? = null
    var tv_view_members: TextView? = null
    var tv_sp_default_currency: TextView? = null
    var assign_group: TextView? = null
    var sp_default_currency: ListView? = null
    var currencyadapter: CommonSpinnerAdapter<String>? = null
    var iscurrency_checked = true
    var child_container: FrameLayout? = null
    var total = ""
    var count = ""
    var tv_switchView: LinearLayout? = null
    var tv_switchCreate: LinearLayout? = null
    var response_email: TextView? = null
    var response_cemail: TextView? = null
    var tv_tot_license: TextView? = null
    var et_search_members: TextInputEditText? = null
    var et_search_teammember: TextInputEditText? = null
    var tv_default_rate: TextInputEditText? = null
    private var mViewModel: NewModel? = null
    var total_license = ""
    var tv_assign_groups: TextView? = null
    var name: TextView? = null
    var designation: TextView? = null
    var default_rate: TextView? = null
    var default_currency: TextView? = null
    var email: TextView? = null
    var confirm_email: TextView? = null
    var btn_cancel_members: AppCompatButton? = null
    var btn_save_members: AppCompatButton? = null
    var bt_cancel: AppCompatButton? = null
    var bt_save: AppCompatButton? = null
    var btn_cancel_save: AppCompatButton? = null
    var btn_create: AppCompatButton? = null
    var rv_selected_member: RecyclerView? = null
    var rv_view_members: RecyclerView? = null
    var img_dropdown_icon: ImageView? = null
    var img_clear_icon: ImageView? = null
    var default_currency1 = ""
    var cv_details: CardView? = null
    var cv_members_details: CardView? = null
    var groupsAdapter: GroupsAdapter? = null
    var updatedMembersList = ArrayList<ViewGroupModel>()
    var currency_list = ArrayList<String>()
    var members_list = ArrayList<MembersModel>()
    var TAG = ""
    var tl_search_members: View? = null
    var isCreateMatter = true
    var groupsList = ArrayList<ViewGroupModel>()
    var ll_buttons: LinearLayoutCompat? = null
    var ll_new_buttons: LinearLayoutCompat? = null
    var ll_save_buttons: LinearLayoutCompat? = null
    var ll_confirm_email: LinearLayout? = null
    var ll_sp_currency: LinearLayout? = null
    var progress_dialog: Dialog? = null
    var tl_search_teammember: View? = null

    companion object {
        var FLAG = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupOnBackPressed()
        super.onCreate(savedInstanceState)
    }

    private fun setupOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isEnabled) {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.create_members, container, false)
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
        tv_member_name = v.findViewById(R.id.tv_create_member_name)
        (tv_member_name as? EditText)?.addTextChangedListener(Validation(tv_member_name as? EditText))
        name = v.findViewById(R.id.name)
        tv_switchCreate = v.findViewById(R.id.tv_switchCreate)
        tv_switchView = v.findViewById(R.id.tv_switchView)
        name?.setText(R.string.name)
        child_container = v.findViewById(R.id.child_container)
        tv_tot_license = v.findViewById(R.id.tv_tot_license)
        tv_tot_license?.text = ""
        assign_group = v.findViewById(R.id.assign_group)
        assign_group?.setText(R.string.assign_groups_)
        designation = v.findViewById(R.id.designation)
        designation?.setText(R.string.desg)
        default_rate = v.findViewById(R.id.default_rate)
        default_rate?.setText(R.string.default_rate)
        default_currency = v.findViewById(R.id.default_currency)
        default_currency?.setText(R.string.default_currency)
        email = v.findViewById(R.id.email)
        email?.setText(R.string.email)
        confirm_email = v.findViewById(R.id.confirm_email)
        confirm_email?.setText(R.string.confirm_email)
        name = v.findViewById(R.id.name)
        name?.setText(R.string.name)
        tv_member_name?.setHint(R.string.name)
        tv_designation = v.findViewById(R.id.tv_designation)
        tv_designation?.setHint(R.string.desg)
        (tv_designation as? EditText)?.addTextChangedListener(Validation(tv_designation as? EditText))
        tv_email = v.findViewById(R.id.tv_email)
        tv_email?.setHint(R.string.email)
        (tv_email as? EditText)?.addTextChangedListener(Validation(tv_email as? EditText))
        rv_view_members = v.findViewById(R.id.rv_view_members)
        tv_confirm_email = v.findViewById(R.id.tv_confirm_email)
        tv_confirm_email?.setHint(R.string.confirm_email)
        (tv_confirm_email as? EditText)?.addTextChangedListener(Validation(tv_confirm_email as? EditText))
        tv_default_rate = v.findViewById(R.id.tv_default_rate)
        tv_default_rate?.setHint(R.string.default_rate)
        tv_default_rate?.maxLines = 1
        tv_default_rate?.addTextChangedListener(Validation(tv_default_rate))
        bt_save = v.findViewById(R.id.btn_update)
        bt_cancel = v.findViewById(R.id.btn_cancel_edit)
        tv_create_members = v.findViewById(R.id.tv_create_members)
        tv_create_members?.setText(R.string.create_member)
        tv_view_members = v.findViewById(R.id.tv_view_members)
        tl_search_members = v.findViewById(R.id.tl_search_members)
        et_search_members = tl_search_members?.findViewById(R.id.et_Search)
        et_search_members?.setHint(R.string.search_groups)
        et_search_members?.addTextChangedListener(Validation(et_search_members))
        tl_search_teammember = v.findViewById(R.id.tl_search_teammember)
        et_search_teammember = tl_search_teammember?.findViewById(R.id.et_Search)
        et_search_teammember?.setHint(R.string.search_team_members)
        et_search_teammember?.addTextChangedListener(Validation(et_search_teammember))
        sp_default_currency = v.findViewById(R.id.sp_default_currency)
        ll_sp_currency = v.findViewById(R.id.ll_sp_currency)
        img_clear_icon = ll_sp_currency?.findViewById(R.id.img_clear_icon)
        img_dropdown_icon = ll_sp_currency?.findViewById(R.id.img_dropdown_icon)
        tv_sp_default_currency = ll_sp_currency?.findViewById(R.id.tv_spinner_view)
        tv_sp_default_currency?.setHint(R.string.select_default_currency)
        btn_cancel_members = v.findViewById(R.id.btn_cancel_members)
        btn_save_members = v.findViewById(R.id.btn_save_members)
        cv_details = v.findViewById(R.id.cv_details)
        btn_cancel_save = v.findViewById(R.id.btn_cancel_save)
        btn_create = v.findViewById(R.id.btn_create)
        btn_create?.setOnClickListener(this)
        btn_cancel_save?.setOnClickListener(this)
        ll_save_buttons = v.findViewById(R.id.ll_save_buttons)
        ll_confirm_email = v.findViewById(R.id.ll_confirm_email)
        ll_buttons = v.findViewById(R.id.ll_buttons)
        cv_members_details = v.findViewById(R.id.cv_details_2)
        ll_new_buttons = v.findViewById(R.id.ll_edit_buttons)
        tv_assign_groups = v.findViewById(R.id.tv_assign_group)
        response_email = v.findViewById(R.id.response_email)
        response_cemail = v.findViewById(R.id.response_cemail)
        tv_assign_groups?.setOnClickListener {
            HideSelectGroups()
        }
        rv_selected_member = v.findViewById(R.id.rv_selected_member)

        if (Constants.ROLE == "GH") {
            tv_create_members?.visibility = View.GONE
            tv_view_members?.background = requireContext().getDrawable(R.drawable.full_green_background)
            ViewMembersData()
        } else {
            if (Constants.isCreate) {
                if (!Constants.is_active) {
                    AndroidUtils.showRenewalPopup(requireActivity())
                } else {
                    CreateMembers()
                }
            } else {
                ViewMembersData()
            }
        }
        ToggleViewUi()

        tv_create_members?.setOnClickListener {
            if (!Constants.is_active) {
                AndroidUtils.showRenewalPopup(requireActivity())
            } else {
                CreateMembers()
            }
        }
        AndroidUtils.setupModuleView(
            tv_switchCreate,
            getString(R.string.view_members),
            false, true, context, getString(R.string.create_members)
        ) {
            ViewMembersData()
        }
        AndroidUtils.setupModuleView(
            tv_switchView,
            getString(R.string.create_members),
            true, true, context, getString(R.string.list_members)
        ) {
            if (!Constants.is_active) {
                AndroidUtils.showRenewalPopup(requireActivity())
            } else {
                CreateMembers()
            }
        }
        tv_view_members?.setOnClickListener {
            ViewMembersData()
        }
        AndroidUtils.NumberFilter(tv_default_rate, false)
        tv_sp_default_currency?.setOnClickListener {
            val isVisible = sp_default_currency?.visibility == View.VISIBLE
            AndroidUtils.DisplaySpinnerView(
                sp_default_currency,
                tv_sp_default_currency,
                default_currency1,
                img_dropdown_icon,
                img_clear_icon,
                !isVisible,
                currencyadapter,
                "Search Currency"
            )
            iscurrency_checked = !iscurrency_checked
        }
        img_clear_icon?.setOnClickListener {
            AndroidUtils.DisplaySpinnerView(
                sp_default_currency,
                tv_sp_default_currency,
                default_currency1,
                img_dropdown_icon,
                img_clear_icon,
                false,
                currencyadapter,
                "Search Currency"
            )
            iscurrency_checked = true
        }
        currency_list = AndroidUtils.getCurrency_list()
        currencyadapter = CommonSpinnerAdapter(requireActivity(), currency_list)
        sp_default_currency?.adapter = currencyadapter
        default_currency1 = currency_list[0]
        tv_sp_default_currency?.text = currency_list[0]
        tv_sp_default_currency?.text = default_currency1
        img_clear_icon?.visibility = View.VISIBLE
        img_dropdown_icon?.visibility = View.GONE
        iscurrency_checked = true
        sp_default_currency?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            default_currency1 = currencyadapter?.getItem(position).toString()
            AndroidUtils.DisplaySpinnerView(
                sp_default_currency,
                tv_sp_default_currency,
                default_currency1,
                img_dropdown_icon,
                img_clear_icon,
                false,
                currencyadapter,
                "Search Currency"
            )
            iscurrency_checked = true
        }
        btn_save_members?.setOnClickListener {
            if (Validation()) {
                val tag = "Create"
                val id = ""
                callCreateMemberWebservice(
                    tv_member_name?.text.toString().trim(),
                    tv_designation?.text.toString().trim(),
                    tv_default_rate?.text.toString().trim(),
                    tv_email?.text.toString().trim(),
                    tv_confirm_email?.text.toString().trim(),
                    tag,
                    id
                )
            }
        }
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                CheckEmailAlert()
            }
        }
        tv_confirm_email?.addTextChangedListener(textWatcher)
        tv_email?.addTextChangedListener(textWatcher)
    }

    private fun CheckEmailAlert() {
        response_email?.visibility = View.GONE
        response_cemail?.visibility = View.GONE

        val emailStr = tv_email?.text?.toString()?.trim() ?: ""
        val confirmEmailStr = tv_confirm_email?.text?.toString()?.trim() ?: ""

        if (emailStr.isNotEmpty() && !AndroidUtils.isValidEmail(emailStr)) {
            response_email?.visibility = View.VISIBLE
            response_email?.text = Constants.email_info_alert
        }

        if (confirmEmailStr.isNotEmpty() && !AndroidUtils.isValidEmail(confirmEmailStr)) {
            response_cemail?.visibility = View.VISIBLE
            response_cemail?.text = Constants.email_info_alert
        }

        if (emailStr.isNotEmpty() && confirmEmailStr.isNotEmpty() &&
            Patterns.EMAIL_ADDRESS.matcher(emailStr).matches() &&
            Patterns.EMAIL_ADDRESS.matcher(confirmEmailStr).matches() &&
            emailStr != confirmEmailStr
        ) {
            response_cemail?.visibility = View.VISIBLE
            response_cemail?.text = Constants.cemail_alert
        }
    }

    private fun setViewModelData(data: String) {
        mViewModel?.setData(data)
    }

    private fun ToggleViewUi() {
        if (Constants.ROLE == "GH") {
            tv_create_members?.visibility = View.GONE
            AndroidUtils.setupModuleView(
                tv_switchView,
                getString(R.string.create_members),
                true, true, false, context, getString(R.string.list_members)
            ) {
                if (!Constants.is_active) {
                    AndroidUtils.showRenewalPopup(requireActivity())
                } else {
                    CreateMembers()
                }
            }
            tv_view_members?.background = requireContext().getDrawable(R.drawable.full_green_background)
        } else {
            tv_create_members?.visibility = View.VISIBLE
            tv_switchView?.visibility = View.VISIBLE
            tv_view_members?.background = requireContext().getDrawable(R.drawable.button_right_green_background)
        }
    }

    private fun clearData() {
        tv_member_name?.text = ""
        tv_designation?.text = ""
        btn_save_members?.setText(R.string.save)
        ll_save_buttons?.visibility = View.VISIBLE
        tv_default_rate?.setText("")
        tv_email?.text = ""
        tv_confirm_email?.text = ""
        members_list.clear()
        groupsList.clear()
        updatedMembersList.clear()
        rv_view_members?.visibility = View.GONE
        for (i in currency_list.indices) {
            if (currency_list[i] == "USDollar(USD)") {
                sp_default_currency?.setSelection(i)
                default_currency1 = currency_list[i]
                tv_sp_default_currency?.text = default_currency1
                img_clear_icon?.visibility = View.VISIBLE
                img_dropdown_icon?.visibility = View.GONE
                iscurrency_checked = true
            }
        }
    }

    private fun callGroupsWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val postdata = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/groups",
                "Get Groups",
                postdata.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun Validation(): Boolean {
        val emailStr = tv_email?.text?.toString()?.trim() ?: ""
        val confirmEmailStr = tv_confirm_email?.text?.toString()?.trim() ?: ""
        var hasErrors = false
        if (tv_member_name?.text?.toString()?.trim()?.isEmpty() == true ||
            tv_designation?.text?.toString()?.trim()?.isEmpty() == true ||
            tv_sp_default_currency?.text?.toString()?.isEmpty() == true ||
            tv_default_rate?.text?.toString()?.trim()?.isEmpty() == true ||
            tv_email?.text?.toString()?.trim()?.isEmpty() == true ||
            tv_confirm_email?.text?.toString()?.trim()?.isEmpty() == true
        ) {
            hasErrors = true
            var msg = "Please check the"
            if (tv_member_name?.text?.toString()?.trim()?.isEmpty() == true) {
                msg = "$msg Name"
            }
            if (tv_designation?.text?.toString()?.trim()?.isEmpty() == true) {
                msg = if (msg == "Please check the") "$msg Designation" else "$msg, Designation"
            }
            if (tv_sp_default_currency?.text?.toString()?.trim()?.isEmpty() == true) {
                msg = if (msg == "Please check the") "$msg Default Currency" else "$msg, Default Currency"
            }
            if (tv_default_rate?.text?.toString()?.trim()?.isEmpty() == true) {
                msg = if (msg == "Please check the") "$msg Default Rate" else "$msg, Default Rate"
            }
            if (tv_email?.text?.toString()?.trim()?.isEmpty() == true) {
                msg = if (msg == "Please check the") "$msg Email" else "$msg, Email"
            }
            if (tv_confirm_email?.text?.toString()?.trim()?.isEmpty() == true) {
                msg = if (msg == "Please check the") "$msg Confirm Email" else "$msg, Confirm Email"
            }
            AndroidUtils.showAlert(msg, requireActivity())
        } else {
            if (!AndroidUtils.isValidEmail(emailStr)) {
                AndroidUtils.showAlert(Constants.email_info_alert, requireActivity())
                hasErrors = true
            } else if (!AndroidUtils.isValidEmail(confirmEmailStr)) {
                AndroidUtils.showAlert(Constants.email_info_alert, requireActivity())
                hasErrors = true
            } else if (emailStr != confirmEmailStr) {
                AndroidUtils.showAlert(Constants.cemail_alert, requireActivity())
                hasErrors = true
            }
        }
        return !hasErrors
    }

    private fun CreateMembersData() {
        if (TAG == "UGA") {
            cv_details?.visibility = View.GONE
            ll_new_buttons?.visibility = View.GONE
            ll_buttons?.visibility = View.GONE
            cv_members_details?.visibility = View.VISIBLE
            tl_search_teammember?.visibility = View.GONE
            tv_tot_license?.visibility = View.GONE
            members_list.clear()
        } else {
            cv_details?.visibility = View.VISIBLE
            cv_members_details?.visibility = View.GONE
            members_list.clear()
        }
        if (TAG == "UGA") {
            callGroupsWebservice()
        }
        rv_view_members?.removeAllViews()
    }

    private fun HideSelectGroups() {
        if (FLAG != "second_click") {
            if (Validation()) {
                cv_members_details?.visibility = View.VISIBLE
                callGroupsWebservice()
                ll_save_buttons?.visibility = View.GONE
            }
        } else {
            FLAG = "first_click"
            cv_members_details?.visibility = View.GONE
            ll_save_buttons?.visibility = View.VISIBLE
            groupsList.clear()
        }
    }

    private fun CreateMembers() {
        clearData()
        tv_create_members?.background = requireContext().getDrawable(R.drawable.button_left_green_background)
        tv_view_members?.background = requireContext().getDrawable(R.drawable.button_right_background)
        tv_switchCreate?.visibility = View.VISIBLE
        tv_switchView?.visibility = View.GONE
        ll_confirm_email?.visibility = View.VISIBLE
        ll_new_buttons?.visibility = View.GONE
        TAG = "CM"
        isCreateMatter = true
        setViewModelData("Create Members")
        CreateMembersData()
        callViewGroupsWebservice()
        FLAG = "first_click"
        tl_search_teammember?.visibility = View.GONE
        tv_tot_license?.visibility = View.GONE
        tv_create_members?.setTextColor(requireContext().getColor(R.color.white))
        tv_view_members?.setTextColor(requireContext().getColor(R.color.black))
        rv_view_members?.visibility = View.GONE
    }

    private fun ViewMembersData() {
        tv_create_members?.setTextColor(requireContext().getColor(R.color.black))
        tv_view_members?.setTextColor(requireContext().getColor(R.color.white))
        ll_buttons?.visibility = View.VISIBLE
        isCreateMatter = false
        ToggleViewUi()
        tv_switchCreate?.visibility = View.GONE
        tv_switchView?.visibility = View.VISIBLE
        tv_create_members?.background = requireContext().getDrawable(R.drawable.button_left_background)
        cv_details?.visibility = View.GONE
        cv_members_details?.visibility = View.GONE
        callViewGroupsWebservice()
        et_search_members?.setText("")
        tl_search_teammember?.visibility = View.VISIBLE
        tv_tot_license?.visibility = View.VISIBLE
        et_search_teammember?.setText("")
        members_list.clear()
        groupsList.clear()
        updatedMembersList.clear()
        setViewModelData("View Members")
    }

    private fun callViewGroupsWebservice() {
        try {
            val postdata = JSONObject()
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/members",
                "Get Members",
                postdata.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun callCreateMemberWebservice(
        name: String,
        designation: String,
        default_rate: String,
        email: String,
        confirm_email: String,
        tag: String,
        id: String
    ) {
        try {
            val postdata = JSONObject()
            val groups = JSONArray()
            if (groupsAdapter != null) {
                for (i in 0 until groupsAdapter!!.listItems.size) {
                    val viewGroupModel = groupsAdapter!!.listItems[i]
                    if (viewGroupModel.isChecked) {
                        groups.put(viewGroupModel.id)
                    }
                }
            }

            progress_dialog = AndroidUtils.get_progress(requireActivity())
            if (tag == "Create" || tag == "Update") {
                postdata.put("currency", default_currency1)
                postdata.put("defaultRate", default_rate)
                postdata.put("designation", designation)
                postdata.put("email", email)
                postdata.put("emailConfirm", confirm_email)
                postdata.put("name", name)
            }

            if (tag == "Create" || tag == "UGA") {
                postdata.put("groups", groups)
            }
            if (tag == "RP") {
                postdata.put("memberId", id)
            }
            if (tag == "Upgrade as Practice Partner") {
                postdata.put("id", id)
            }

            if (tag == "Create") {
                Log.i("Tag", "Info:${postdata}")
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "v3/member",
                    "Create Members",
                    postdata.toString()
                )
            } else if (tag == "RP") {
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "v3/member/resetpwd",
                    "Reset Password",
                    postdata.toString()
                )
            } else if (tag == "Delete") {
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.DELETE,
                    "v3/member/$id",
                    "Delete Member",
                    postdata.toString()
                )
            } else if (tag == "Upgrade as Practice Partner") {
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.PATCH,
                    "v3/convert/practice-partner",
                    "Upgrade as Practice Partner",
                    postdata.toString()
                )
            } else {
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.PATCH,
                    "v3/member/$id",
                    "Update Members",
                    postdata.toString()
                )
            }
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, requireActivity())
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    override fun onClick(view: View?) {
        try {
            when (view?.id) {
                R.id.btn_create -> {
                    if (Validation()) {
                        val tag = "Create"
                        val id = ""
                        callCreateMemberWebservice(
                            tv_member_name?.text.toString().trim(),
                            tv_designation?.text.toString().trim(),
                            tv_default_rate?.text.toString().trim(),
                            tv_email?.text.toString().trim(),
                            tv_confirm_email?.text.toString().trim(),
                            tag,
                            id
                        )
                    }
                }
                R.id.btn_cancel_save -> {
                    clearData()
                }
            }
        } catch (e: Exception) {
            Log.e("TAG", "Error:${e.message}")
            e.printStackTrace()
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo?) {
        if (progress_dialog != null && progress_dialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }
        if (httpResult == null) return

        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent ?: "")

                when (httpResult.requestType) {
                    "Create Members" -> {
                        if (httpResult.status_code == 200) {
                            AndroidUtils.showAlert(result.getString("msg"), requireActivity())
                            ViewMembersData()
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), requireActivity())
                        }
                    }
                    "Update Members" -> {
                        if (httpResult.status_code == 200) {
                            AndroidUtils.showAlert(result.getString("msg"), requireActivity())
                            ViewMembersData()
                            ll_new_buttons?.visibility = View.GONE
                            tv_assign_groups?.visibility = View.VISIBLE
                            ll_save_buttons?.visibility = View.VISIBLE
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), requireActivity())
                        }
                    }
                    "Reset Password" -> {
                        val data = result.getJSONObject("data")
                        AndroidUtils.showAlert(data.getString("msg"), requireActivity())
                        ViewMembersData()
                    }
                    "Upgrade as Practice Partner" -> {
                        if (httpResult.status_code == 200) {
                            AndroidUtils.showAlert(result.getString("msg"), requireActivity())
                            ViewMembersData()
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), requireActivity())
                        }
                    }
                    "Delete Member" -> {
                        if (httpResult.status_code == 200) {
                            AndroidUtils.showAlert(result.getString("msg"), requireActivity())
                            ViewMembersData()
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), requireActivity())
                        }
                    }
                    "Get Members" -> {
                        val data = result.getJSONObject("data")
                        val users = data.getJSONArray("users")
                        total = data.getString("total")
                        count = data.getString("count")

                        val frozenText = getString(R.string.your_present_subscription_does_not_allow_creation_of_more_users)
                        val fm = childFragmentManager
                        val ft = fm.beginTransaction()
                        val disabledView = fm.findFragmentByTag("DISABLED_VIEW")

                        Constants.isSubscriptionEnded = false
                        if (total == count) {
                            Constants.isSubscriptionEnded = true
                        }

                        if (isCreateMatter) {
                            if (total == count) {
                                val view = Disabled_view(frozenText, true)
                                ft.add(R.id.child_container, view, "DISABLED_VIEW")
                                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                ft.addToBackStack(null)
                                ft.commit()

                                child_container?.alpha = 0.5f
                                tv_member_name?.isEnabled = false
                                tv_designation?.isEnabled = false
                                tv_sp_default_currency?.isEnabled = false
                                tv_default_rate?.isEnabled = false
                                tv_email?.isEnabled = false
                                tv_confirm_email?.isEnabled = false
                                tv_assign_groups?.isEnabled = false
                                btn_create?.isEnabled = false
                            }
                        } else {
                            if (disabledView != null) {
                                ft.remove(disabledView)
                                ft.commit()
                            }
                            child_container?.alpha = 1.0f
                            tv_member_name?.isEnabled = true
                            tv_designation?.isEnabled = true
                            tv_sp_default_currency?.isEnabled = true
                            tv_default_rate?.isEnabled = true
                            tv_email?.isEnabled = true
                            tv_confirm_email?.isEnabled = true
                            tv_assign_groups?.isEnabled = true
                            btn_create?.isEnabled = true
                            loadMembers(users)
                        }
                    }
                    "Get Groups" -> {
                        val data = result.getJSONArray("data")
                        loadViewGroups(data)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (httpResult.result == WebServiceHelper.ServiceCallStatus.Failed) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            try {
                val result = JSONObject(httpResult.responseContent ?: "")
                AndroidUtils.showErrorAlert(result.optString("msg"), requireActivity())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            AndroidUtils.showErrorAlert(
                httpResult.responseContent?.toString() ?: "",
                requireActivity()
            )
        }
    }

    private fun loadViewGroups(data: JSONArray) {
        groupsList.clear()

        for (i in 0 until data.length()) {
            val jsonObject = data.getJSONObject(i)
            val viewGroupModel = ViewGroupModel()
            viewGroupModel.id = jsonObject.optString("id")

            val date = jsonObject.optString("created")
            if (date.contains("T")) {
                val date_new = AndroidUtils.stringToDateTimeDefault(date, "yyyy-MM-dd'T'HH:mm:ss.SSS")
                val created = AndroidUtils.getDateToString(date_new, "MMM dd YYYY")
                viewGroupModel.created = created
            } else {
                if (date.isNotEmpty()) {
                    val date_new = AndroidUtils.stringToDateTimeDefault(date, "MMM dd, yyyy, hh:mm a")
                    val created = AndroidUtils.getDateToString(date_new, "MMM dd, yyyy | hh:mm a")
                    viewGroupModel.created = created
                } else {
                    viewGroupModel.created = ""
                }
            }

            viewGroupModel.isIsdisabled = jsonObject.optBoolean("isdisabled")
            viewGroupModel.members = jsonObject.optJSONArray("members")
            viewGroupModel.memberCount = jsonObject.optString("memberCount")
            viewGroupModel.description = jsonObject.optString("description")
            viewGroupModel.name = jsonObject.optString("name")

            val group_head = jsonObject.optJSONObject("groupHead")
            if (group_head != null) {
                viewGroupModel.group_head_id = group_head.optString("id")
                viewGroupModel.group_head_name = group_head.optString("name")
                viewGroupModel.owner_name = group_head.optString("name")
            }

            for (k in 0 until updatedMembersList.size) {
                if (viewGroupModel.id.matches(updatedMembersList[k].group_id.toRegex())) {
                    viewGroupModel.isChecked = true
                    break
                }
            }

            groupsList.add(viewGroupModel)
        }

        groupsList.sortWith(Comparator { a, b ->
            val nameA = a.name ?: ""
            val nameB = b.name ?: ""
            when {
                nameA.equals("AAM", ignoreCase = true) -> -1
                nameB.equals("AAM", ignoreCase = true) -> 1
                nameA.equals("SuperUser", ignoreCase = true) -> -1
                nameB.equals("SuperUser", ignoreCase = true) -> 1
                else -> 0
            }
        })

        Log.i("ArrayList", "info $groupsList")

        loadGroupsRecylerview()
        loadRecylcerview()
    }

    private fun loadGroupsRecylerview() {
        FLAG = "second_click"
        rv_selected_member?.layoutManager = GridLayoutManager(context, 1)
        groupsAdapter = GroupsAdapter(groupsList)
        rv_selected_member?.adapter = groupsAdapter
        et_search_members?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {
                groupsAdapter?.filter?.filter(et_search_members?.text?.toString() ?: "")
            }
        })
        if (TAG == "UGA") {
            btn_cancel_members?.setOnClickListener {
                val data = "View Members"
                setViewModelData(data)
                ViewMembersData()
            }
        } else {
            btn_cancel_members?.setOnClickListener {
                FLAG = "second_click"
                HideSelectGroups()
                clearData()
            }
        }
    }

    private fun loadMembers(users: JSONArray) {
        members_list.clear()
        for (i in 0 until users.length()) {
            val jsonObject = users.getJSONObject(i)
            val membersModel = MembersModel()
            membersModel.id = jsonObject.getString("id")
            membersModel.name = jsonObject.getString("name")
            membersModel.isdisabled = jsonObject.optBoolean("isdisabled")
            membersModel.currency = jsonObject.getString("currency")
            membersModel.defaultRate = jsonObject.getString("defaultRate")
            membersModel.designation = jsonObject.getString("designation")
            membersModel.email = jsonObject.getString("email")
            membersModel.lastLogin = jsonObject.getString("lastLogin")
            membersModel.groups = jsonObject.getJSONArray("groups")
            members_list.add(membersModel)
        }
        loadRecylcerview()
    }

    private fun loadRecylcerview() {
        tv_tot_license?.text = "Number of Licenses : $count out of $total"
        rv_view_members?.visibility = View.VISIBLE

        val adapter = MembersAdapter(members_list, context, this, this)
        rv_view_members?.adapter = adapter
        AndroidUtils.LoadingRecyclerview(rv_view_members, requireContext())
        AndroidUtils.setupBottomSpacerFooter(
            rv_view_members,
            resources.getDimensionPixelSize(R.dimen.twentyeight_dp)
        )

        et_search_teammember?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(s: CharSequence?, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                adapter.filter.filter(et_search_teammember?.text?.toString() ?: "")
            }
        })
    }

    override fun EditMember(membersModel: MembersModel) {
        CreateMembersData()
        rv_view_members?.visibility = View.GONE
        cv_details?.visibility = View.VISIBLE
        tl_search_teammember?.visibility = View.GONE
        tv_tot_license?.visibility = View.GONE
        ll_buttons?.visibility = View.GONE
        ll_save_buttons?.visibility = View.GONE
        tv_assign_groups?.visibility = View.GONE
        ll_new_buttons?.visibility = View.VISIBLE
        cv_members_details?.visibility = View.GONE
        ll_confirm_email?.visibility = View.VISIBLE
        img_dropdown_icon?.visibility = View.GONE
        iscurrency_checked = true
        img_clear_icon?.visibility = View.VISIBLE
        tv_member_name?.text = membersModel.name
        (tv_member_name as? EditText)?.addTextChangedListener(Validation(tv_member_name as? EditText))
        tv_email?.text = membersModel.email
        (tv_email as? EditText)?.addTextChangedListener(Validation(tv_email as? EditText))
        tv_default_rate?.setText(membersModel.defaultRate)
        tv_default_rate?.addTextChangedListener(Validation(tv_default_rate))
        tv_confirm_email?.visibility = View.VISIBLE
        tv_confirm_email?.text = tv_email?.text?.toString() ?: ""
        (tv_confirm_email as? EditText)?.addTextChangedListener(Validation(tv_confirm_email as? EditText))
        for (i in currency_list.indices) {
            if (currency_list[i] == membersModel.currency) {
                sp_default_currency?.setSelection(i)
                tv_sp_default_currency?.text = currency_list[i]
            }
        }
        tv_designation?.text = membersModel.designation
        bt_save?.setText(R.string.save)
        bt_save?.setOnClickListener {
            if (Validation()) {
                val tag = "Update"
                val id = membersModel.id ?: ""
                callCreateMemberWebservice(
                    tv_member_name?.text.toString().trim(),
                    tv_designation?.text.toString().trim(),
                    tv_default_rate?.text.toString().trim(),
                    tv_email?.text.toString().trim(),
                    tv_confirm_email?.text.toString().trim(),
                    tag,
                    id
                )
            }
        }
        bt_cancel?.setOnClickListener {
            UnHide()
            ViewMembersData()
            tv_assign_groups?.visibility = View.VISIBLE
            ll_save_buttons?.visibility = View.VISIBLE
        }
        rv_view_members?.removeAllViews()
        rv_selected_member?.removeAllViews()
    }

    @Throws(JSONException::class)
    override fun UpdateGroupAccess(membersModel: MembersModel) {
        TAG = "UGA"
        val groups = membersModel.groups
        if (groups != null) {
            for (i in 0 until groups.length()) {
                val viewGroupModel = ViewGroupModel()
                val jsonObject = groups.getJSONObject(i)
                viewGroupModel.group_id = jsonObject.getString("id")
                viewGroupModel.group_name = jsonObject.getString("name")
                updatedMembersList.add(viewGroupModel)
            }
        }
        CreateMembersData()
        btn_cancel_members?.setOnClickListener {
            val data = "View Members"
            setViewModelData(data)
            ViewMembersData()
        }
        btn_save_members?.setOnClickListener {
            val tag = "UGA"
            callCreateMemberWebservice("", "", "", "", "", tag, membersModel.id ?: "")
        }
        btn_save_members?.setText(R.string.save)
    }

    override fun ResetPassword(membersModel: MembersModel) {
        try {
            AndroidUtils.showConfirmationDialog(
                requireContext(),
                "Confirmation",
                getString(R.string.reset_password_tm) + membersModel.name + "?",
                membersModel.name,
                object : AndroidUtils.OnConfirmListener {
                    override fun onSave() {
                        val tag = "RP"
                        callCreateMemberWebservice("", "", "", "", "", tag, membersModel.id ?: "")
                    }

                    override fun onCancel() {
                        ViewMembersData()
                    }
                }
            )
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, requireActivity())
        }
    }

    override fun DeleteMember(membersModel: MembersModel) {
        try {
            AndroidUtils.showConfirmationDialog(
                requireContext(),
                "Confirmation",
                getString(R.string.delete_team_member) + membersModel.name + " ?",
                membersModel.name,
                object : AndroidUtils.OnConfirmListener {
                    override fun onSave() {
                        val tag = "Delete"
                        callCreateMemberWebservice("", "", "", "", "", tag, membersModel.id ?: "")
                    }

                    override fun onCancel() {
                        ViewMembersData()
                    }
                }
            )
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, requireActivity())
        }
    }

    override fun Upgrade(membersModel: MembersModel) {
        try {
            AndroidUtils.showConfirmationDialog(
                requireContext(),
                "Confirmation",
                getString(R.string.upgrade_members) + " " + membersModel.name + " " + "Practice Partner and provide Super User access" + " ?",
                membersModel.name,
                object : AndroidUtils.OnConfirmListener {
                    override fun onSave() {
                        val tag = "Upgrade as Practice Partner"
                        callCreateMemberWebservice("", "", "", "", "", tag, membersModel.id ?: "")
                    }

                    override fun onCancel() {
                        ViewMembersData()
                    }
                }
            )
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, requireActivity())
        }
    }

    private fun UnHide() {
        ll_buttons?.visibility = View.VISIBLE
        tv_confirm_email?.visibility = View.VISIBLE
        tl_search_members?.visibility = View.VISIBLE
        tl_search_teammember?.visibility = View.VISIBLE
        tv_tot_license?.visibility = View.VISIBLE
        assign_group?.visibility = View.VISIBLE
        tv_confirm_email?.text = ""
        tv_designation?.text = ""
        tv_email?.text = ""
        tv_member_name?.text = ""
        tv_default_rate?.setText("")
    }

    fun model_name(action_list: String?) {}
}
