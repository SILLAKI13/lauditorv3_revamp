package com.digicoffer.lauditor.Groups

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.ItemClickListener
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.DescriptionValidation
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.digicoffer.lauditor.Groups.Adapters.GroupAdapters
import com.digicoffer.lauditor.Groups.Adapters.SearchAdapter
import com.digicoffer.lauditor.Groups.Adapters.ViewGroupsAdpater
import com.digicoffer.lauditor.Groups.Models.ActionModel
import com.digicoffer.lauditor.Groups.Models.GroupModel
import com.digicoffer.lauditor.Groups.Models.SearchDo
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Objects

class Groups : Fragment(), AsyncTaskCompleteListener, ViewGroupsAdpater.InterfaceListener, View.OnClickListener {
    var rv_select_team_members: RecyclerView? = null
    var rv_view_groups: RecyclerView? = null
    var rv_activity_log: RecyclerView? = null
    var et_search: TextInputEditText? = null
    var tv_description: TextInputEditText? = null
    var tv_search_name: TextInputEditText? = null
    var tv_Client_name_id: TextInputEditText? = null
    var et_search_delete: TextInputEditText? = null
    var et_search_message: TextInputEditText? = null

    var cv_delete_team: CardView? = null
    var GroupsAdapter: CommonSpinnerAdapter<String>? = null
    var isDelete: Boolean = false
    var is_group_name_new: Boolean = true

    var description_name: TextView? = null
    var category_id: TextView? = null
    var team_member_id: TextView? = null
    var from_id: TextView? = null
    var to_id: TextView? = null
    var client_id_name: TextView? = null
    var search_name: TextView? = null
    var group_name_delete: TextView? = null
    var tv_from_date: AppCompatButton? = null
    var tv_to_date: AppCompatButton? = null
    var tv_selected_members: TextInputLayout? = null
    private var mViewModel: NewModel? = null
    var btn_save_selected_tm: AppCompatButton? = null
    var itemClickListener: ItemClickListener? = null
    var new_viewGroupModel: ViewGroupModel? = null

    var tv_search_message: TextInputLayout? = null
    var tv_select_team_members: TextInputLayout? = null
    var new_itemClickListener: ViewGroupsItemClickListener? = null
    var group_head: String = ""
    var group_name: String = ""
    var searchList: ArrayList<SearchDo> = ArrayList()
    var sp_category: Spinner? = null
    var sp_team_member: Spinner? = null
    var selected_category: String = ""
    var selected_tm: String = ""
    var actions_List: ArrayList<ActionModel> = ArrayList()
    var tv_create_group: TextView? = null
    var tv_view_group: TextView? = null
    var tv_add_tm: TextView? = null
    var tv_practice_head: TextView? = null
    var tv_group_name: TextInputEditText? = null
    var tv_group_description: TextInputEditText? = null
    var selectedTMArrayList: ArrayList<GroupModel> = ArrayList()
    var TMArrayList: ArrayList<GroupModel> = ArrayList()

    var viewGroupModelArrayList: ArrayList<ViewGroupModel> = ArrayList()
    var viewGroupNameArrayList: ArrayList<ViewGroupModel> = ArrayList()
    var viewGroupMembersList: ArrayList<ViewGroupModel> = ArrayList()
    var updateGroupMembersList: ArrayList<ViewGroupModel> = ArrayList()
    var assignGroupsList: ArrayList<GroupModel> = ArrayList()
    private var chk_select_all: CheckBox? = null
    var cv_groups: CardView? = null
    var cv_details: CardView? = null
    var cv_activity_log: CardView? = null
    var adapter: GroupAdapters? = null
    var adapter_delete: ViewGroupsAdpater? = null
    var adapter_view_groups: ViewGroupsAdpater? = null
    var progress_dialog: Dialog? = null
    var v: View? = null
    var ll_deleteGroups: LinearLayout? = null
    var tv_sp_groups: LinearLayout? = null
    var ll_Delete_view: LinearLayout? = null
    var sp_groupslist: ListView? = null
    var clear_icon: ImageView? = null
    var dropdown_icon: ImageView? = null
    var group_head_name: TextView? = null
    var select_ghead: TextView? = null
    var tv_delete_groups: TextView? = null
    var tv_documents: TextView? = null
    var tv_matters: TextView? = null
    var tv_relationships: TextView? = null
    var tv_members: TextView? = null
    var btn_cancel: AppCompatButton? = null
    var btn_save: AppCompatButton? = null
    var btn_cancel_edit: AppCompatButton? = null
    var btn_update: AppCompatButton? = null
    var btn_cancel_gal: AppCompatButton? = null
    var btn_search_gal: AppCompatButton? = null
    var et_search_tm: TextInputEditText? = null
    var tv_label: TextView? = null
    var tv_switchCreate: LinearLayout? = null
    var tv_switchView: LinearLayout? = null
    var ll_Views: LinearLayoutCompat? = null
    var ll_tm: LinearLayoutCompat? = null
    var ll_select_all: LinearLayoutCompat? = null
    var ll_buttons: LinearLayoutCompat? = null
    var ll_group_list: LinearLayoutCompat? = null
    var ll_select_tm: LinearLayoutCompat? = null
    var ll_edit_groups: LinearLayoutCompat? = null
    var selectedGroupName: String = ""
    var tl_et_search: View? = null

    companion object {
        @JvmField
        var FLAG: String = ""

        @JvmField
        var TM_TYPE: String = ""
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
        v = inflater.inflate(R.layout.groups, container, false)
        return v
    }

    @SuppressLint("MissingInflatedId")
    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
        rv_select_team_members = v.findViewById(R.id.rv_selected_tm)
        rv_view_groups = v.findViewById(R.id.rv_view_group)
        tl_et_search = v.findViewById(R.id.tl_et_search)
        et_search = tl_et_search?.findViewById(R.id.et_Search)
        et_search?.addTextChangedListener(Validation(et_search))

        ll_deleteGroups = v.findViewById(R.id.ll_deleteGroups)
        ll_Delete_view = v.findViewById(R.id.ll_Delete_view)
        tv_documents = v.findViewById(R.id.tv_documents)
        tv_matters = v.findViewById(R.id.tv_matters)
        tv_relationships = v.findViewById(R.id.tv_relationships)
        tv_members = v.findViewById(R.id.tv_members)
        ll_Views = v.findViewById(R.id.ll_Views)
        tv_sp_groups = v.findViewById(R.id.tv_sp_groups)
        tv_delete_groups = tv_sp_groups?.findViewById(R.id.tv_spinner_view)
        tv_delete_groups?.setHint(R.string.select_group_name)
        clear_icon = tv_sp_groups?.findViewById(R.id.img_clear_icon)
        dropdown_icon = tv_sp_groups?.findViewById(R.id.img_dropdown_icon)

        sp_groupslist = v.findViewById(R.id.sp_groupslist)
        tv_group_name = v.findViewById(R.id.tv_group_name)
        tv_group_name?.setHint(R.string.group_name)
        select_ghead = v.findViewById(R.id.select_ghead)
        select_ghead?.setTextSize(DynamicUtils.twenty.toFloat())
        select_ghead?.setText(R.string.select_head_of_the_group)
        tv_group_description = v.findViewById(R.id.tv_description)
        ll_edit_groups = v.findViewById(R.id.ll_edit_buttons)
        cv_groups = v.findViewById(R.id.cv_details)
        tv_label = v.findViewById(R.id.tv_label)

        val tl_search_appointments = v.findViewById<View>(R.id.tl_et_search_delete)
        et_search_delete = tl_search_appointments.findViewById(R.id.et_Search)
        et_search_delete?.setHint(R.string.search_groups)
        et_search_delete?.addTextChangedListener(Validation(et_search_delete))
        group_name_delete = v.findViewById(R.id.group_name_delete)
        group_name_delete?.gravity = Gravity.START
        group_name_delete?.typeface = Typeface.DEFAULT_BOLD
        cv_delete_team = v.findViewById(R.id.cv_delete_team)
        tv_switchCreate = v.findViewById(R.id.tv_switchCreate)
        tv_switchView = v.findViewById(R.id.tv_switchView)

        search_name = v.findViewById(R.id.search_name)
        search_name?.setText(R.string.search)
        client_id_name = v.findViewById(R.id.client_id_name)
        client_id_name?.setText(R.string.client)

        from_id = v.findViewById(R.id.from_id)
        from_id?.setText(R.string.from)
        to_id = v.findViewById(R.id.to_id)
        to_id?.setText(R.string.to)
        tv_search_name = v.findViewById(R.id.tv_search_name)
        tv_search_name?.setHint(R.string.search)
        tv_Client_name_id = v.findViewById(R.id.tv_Client_name_id)
        tv_Client_name_id?.setHint(R.string.client)

        tv_group_description?.setHint(R.string.description)
        description_name = v.findViewById(R.id.description_name)
        description_name?.setText(R.string.description)
        category_id = v.findViewById(R.id.category_id)
        category_id?.setText(R.string.category)
        team_member_id = v.findViewById(R.id.team_member_id)
        team_member_id?.setText(R.string.team_members)

        tv_select_team_members = v.findViewById(R.id.filledTextField3)
        tv_search_message = v.findViewById(R.id.search_message)
        tv_from_date = v.findViewById(R.id.btn_from_date)
        tv_from_date?.setHint(R.string.from)
        tv_to_date = v.findViewById(R.id.btn_to_date)
        tv_to_date?.setHint(R.string.to)

        val tl_et_search_tm = v.findViewById<View>(R.id.tl_et_search_tm)
        et_search_tm = tl_et_search_tm.findViewById(R.id.et_Search)
        et_search_tm?.setHint(R.string.search_team_members)
        et_search_tm?.addTextChangedListener(Validation(et_search_tm))
        group_head_name = v.findViewById(R.id.group_head_name)
        cv_details = v.findViewById(R.id.cv_details_2)
        btn_cancel_gal = v.findViewById(R.id.btn_cancel_activity_log)

        cv_activity_log = v.findViewById(R.id.cv_details_activity_log)
        ll_tm = v.findViewById(R.id.linearLayoutCompat1)
        btn_cancel_edit = v.findViewById(R.id.btn_cancel_edit)

        btn_update = v.findViewById(R.id.btn_update)
        btn_update?.setText(R.string.update)
        ll_select_all = v.findViewById(R.id.ll_select_all)
        ll_group_list = v.findViewById(R.id.ll_Views)
        ll_select_tm = v.findViewById(R.id.linearLayoutCompat2)
        ll_buttons = v.findViewById(R.id.ll_buttons)
        tv_create_group = v.findViewById(R.id.tv_create_group)
        tv_create_group?.setText(R.string.create_group)
        tv_view_group = v.findViewById(R.id.tv_view_group)
        tv_view_group?.setText(R.string.view_group)
        tv_add_tm = v.findViewById(R.id.add_tm)
        tv_add_tm?.setText(R.string.add_team_member)
        tv_practice_head = v.findViewById(R.id.add_phead)
        tv_practice_head?.setText(R.string.add_practice_head)
        sp_category = v.findViewById(R.id.sp_category)

        btn_cancel = v.findViewById(R.id.btn_cancel)
        chk_select_all = v.findViewById(R.id.chk_select_all)
        btn_save = v.findViewById(R.id.btn_save)

        tv_group_name?.addTextChangedListener(Validation(tv_group_name))
        tv_group_description?.addTextChangedListener(DescriptionValidation(tv_group_description))

        rv_view_groups?.let { AndroidUtils.setupEdgePaddingBehavior(it) }
        tv_view_group?.setOnClickListener { ViewGroupsData() }

        if (Constants.ROLE == "GH") {
            ViewGroupsData()
            tv_create_group?.visibility = View.GONE
            AndroidUtils.setupModuleView(
                tv_switchView,
                getString(R.string.create_group),
                true, true, false, requireContext(), getString(R.string.list_groups)
            ) {
                if (!Constants.is_active) {
                    AndroidUtils.showRenewalPopup(requireActivity())
                } else {
                    CreateGroupData()
                }
            }
        } else {
            if (Constants.isCreate) {
                CreateGroupData()
            } else {
                ViewGroupsData()
            }
            tv_create_group?.visibility = View.VISIBLE
        }

        tv_practice_head?.setOnClickListener {
            if (assignGroupsList.isEmpty()) {
                select_ghead?.visibility = View.GONE
                AndroidUtils.showAlert("Please check the member selection", requireActivity(), "Info")
            } else {
                select_ghead?.visibility = View.VISIBLE
                et_search_tm?.setText("")
                assignGroupHead(TMArrayList)
                ll_select_all?.visibility = View.GONE
                tv_practice_head?.setTextColor(requireContext().resources.getColor(R.color.white))
                tv_add_tm?.setTextColor(requireContext().resources.getColor(R.color.black))
                tv_add_tm?.background = requireActivity().resources.getDrawable(R.drawable.button_left_background)
                tv_practice_head?.background = requireActivity().resources.getDrawable(R.drawable.button_right_green_count)
            }
        }

        AndroidUtils.setupModuleView(
            tv_switchCreate,
            getString(R.string.view_group),
            false, true, requireContext(), getString(R.string.create_group)
        ) {
            ViewGroupsData()
        }

        AndroidUtils.setupModuleView(
            tv_switchView,
            getString(R.string.create_group),
            true, true, requireContext(), getString(R.string.list_groups)
        ) {
            if (!Constants.is_active) {
                AndroidUtils.showRenewalPopup(requireActivity())
            } else {
                CreateGroupData()
            }
        }

        tv_create_group?.setOnClickListener {
            if (!Constants.is_active) {
                AndroidUtils.showRenewalPopup(requireActivity())
            } else {
                CreateGroupData()
            }
        }

        tv_add_tm?.setOnClickListener {
            group_head = ""
            tv_add_tm?.setTextColor(requireContext().resources.getColor(R.color.white))
            tv_practice_head?.setTextColor(requireContext().resources.getColor(R.color.black))
            tv_practice_head?.background = requireActivity().resources.getDrawable(R.drawable.button_right_background)
            tv_add_tm?.background = requireActivity().resources.getDrawable(R.drawable.button_left_green_background)
            ll_select_all?.visibility = View.VISIBLE
            select_ghead?.visibility = View.GONE
            chk_select_all?.isChecked = false
            if (TMArrayList.isNotEmpty()) {
                TM_TYPE = "TM"
                loadRecylcerview(TMArrayList, TM_TYPE)
            } else {
                adapter?.selectOrDeselectAll(false)
                adapter?.getList_item()?.clear()
                selectedTMArrayList.clear()
                assignGroupsList.clear()
                rv_select_team_members?.removeAllViews()
                callMembersWebservice()
            }
        }

        tv_group_name?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.toString()?.let {
                    if (it.contains("\n")) {
                        tv_group_name?.setText(it.replace("\n", ""))
                        tv_group_name?.text?.length?.let { len -> tv_group_name?.setSelection(len) }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        et_search_tm?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.toString()?.let {
                    if (it.contains("\n")) {
                        et_search_tm?.setText(it.replace("\n", ""))
                        et_search_tm?.text?.length?.let { len -> et_search_tm?.setSelection(len) }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        et_search_delete?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.toString()?.let {
                    if (it.contains("\n")) {
                        et_search_delete?.setText(it.replace("\n", ""))
                        et_search_delete?.text?.length?.let { len -> et_search_delete?.setSelection(len) }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        et_search?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.toString()?.let {
                    if (it.contains("\n")) {
                        et_search?.setText(it.replace("\n", ""))
                        et_search?.text?.length?.let { len -> et_search?.setSelection(len) }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        hideTM()
        FLAG = "first_click"
        tv_select_team_members?.setOnClickListener {
            if (FLAG != "second_click") {
                val gname = tv_group_name?.text?.toString()?.trim() ?: ""
                val gdesc = tv_group_description?.text?.toString()?.trim() ?: ""
                if (gname.isEmpty() && gdesc.isEmpty()) {
                    AndroidUtils.showAlert("Please check the Group Name , Description", requireActivity())
                } else if (gname.isEmpty()) {
                    AndroidUtils.showAlert("Please check the Group Name", requireActivity())
                } else if (gdesc.isEmpty()) {
                    AndroidUtils.showAlert("Please check the Description", requireActivity())
                } else {
                    unhideTM()
                    callMembersWebservice()
                }
            } else {
                FLAG = "first_click"
                hideTM()
            }
        }
    }

    private fun AddButtonsClickable() {
        btn_update?.setOnClickListener {
            val gname = tv_group_name?.text?.toString()?.trim() ?: ""
            val gdesc = tv_group_description?.text?.toString()?.trim() ?: ""
            if (gname.isEmpty() && gdesc.isEmpty()) {
                AndroidUtils.showAlert("Please check the Group Name , Description", requireActivity())
            } else if (gname.isEmpty()) {
                AndroidUtils.showAlert("Please check the Group Name", requireActivity())
            } else if (gdesc.isEmpty()) {
                AndroidUtils.showAlert("Please check the Description", requireActivity())
            } else {
                val arraylist = ArrayList<GroupModel>()
                callCreateGroupWebservice(gname, gdesc, arraylist, "")
            }
        }
        btn_cancel_edit?.setOnClickListener {
            tv_group_name?.setText("")
            tv_group_description?.setText("")
            assignGroupsList.clear()
            selectedTMArrayList.clear()
            mViewModel?.setData(getString(R.string.view_group))
            group_head = ""
            group_name = ""
            reverse_data()
            ViewGroupsData()
        }
    }

    private fun CreateGroupData() {
        unhide_delete_data()
        ll_deleteGroups?.visibility = View.GONE
        tv_view_group?.background = requireContext().resources.getDrawable(R.drawable.button_right_background)
        tv_create_group?.background = requireContext().resources.getDrawable(R.drawable.button_left_green_background)
        assignGroupsList.clear()
        chk_select_all?.isChecked = false
        cv_groups?.visibility = View.VISIBLE
        setViewModelData(getString(R.string.create_group))
        tv_switchView?.visibility = View.GONE
        tv_switchCreate?.visibility = View.VISIBLE
        ll_edit_groups?.visibility = View.VISIBLE
        btn_save?.setText(R.string.save)
        btn_cancel?.setText(R.string.cancel)
        btn_save?.alpha = 1.0f
        btn_save?.isEnabled = true
        btn_update?.alpha = 1.0f
        btn_update?.isEnabled = true

        tv_create_group?.setTextColor(requireContext().resources.getColor(R.color.white))
        tv_view_group?.setTextColor(requireContext().resources.getColor(R.color.black))
        tv_add_tm?.setTextColor(requireContext().resources.getColor(R.color.white))
        tv_practice_head?.setTextColor(requireContext().resources.getColor(R.color.black))

        ll_buttons?.visibility = View.VISIBLE
        rv_view_groups?.visibility = View.GONE
        tl_et_search?.visibility = View.GONE
        hideTM()
        viewGroupModelArrayList.clear()
        rv_view_groups?.removeAllViews()
        AddButtonsClickable()
    }

    private fun setViewModelData(data: String) {
        mViewModel?.setData(data)
    }

    private fun unhideTM() {
        ll_tm?.visibility = View.VISIBLE
        cv_details?.visibility = View.VISIBLE
        ll_edit_groups?.visibility = View.GONE
        select_ghead?.visibility = View.GONE
        ll_select_all?.visibility = View.VISIBLE
        btn_update?.setText(R.string.update)
    }

    private fun hideTM() {
        ll_tm?.visibility = View.GONE
        et_search_tm?.setText("")
        cv_details?.visibility = View.GONE
        ll_edit_groups?.visibility = View.VISIBLE
        select_ghead?.visibility = View.VISIBLE
        group_head_name?.visibility = View.GONE
        group_head_name?.text = ""
        btn_update?.setText(R.string.save)
        updateGroupMembersList.clear()
        selectedTMArrayList.clear()
        actions_List.clear()
        assignGroupsList.clear()
        searchList.clear()
        viewGroupMembersList.clear()
        viewGroupModelArrayList.clear()
    }

    private fun ViewGroupsData() {
        mViewModel?.setData("View Groups")
        setViewModelData(getString(R.string.view_group))

        if (Constants.ROLE == "GH") {
            tv_view_group?.background = requireContext().resources.getDrawable(R.drawable.full_green_background)
        } else {
            tv_view_group?.background = requireContext().resources.getDrawable(R.drawable.button_right_green_background)
        }
        tv_view_group?.setTextColor(requireContext().resources.getColor(R.color.white))
        tv_switchView?.visibility = View.VISIBLE
        tv_switchCreate?.visibility = View.GONE
        tv_create_group?.setTextColor(requireContext().resources.getColor(R.color.black))
        tv_create_group?.background = requireContext().resources.getDrawable(R.drawable.button_left_background)
        tv_group_name?.setText("")
        tv_group_description?.setText("")
        cv_groups?.visibility = View.GONE
        ll_tm?.visibility = View.GONE
        tv_label?.visibility = View.GONE
        select_ghead?.visibility = View.GONE
        ll_select_all?.visibility = View.GONE
        selectedTMArrayList.clear()
        rv_select_team_members?.removeAllViews()
        cv_details?.visibility = View.GONE
        rv_view_groups?.visibility = View.VISIBLE
        tl_et_search?.visibility = View.VISIBLE
        et_search?.setText("")
        et_search?.setHint(R.string.search_groups)
        callViewGroupsWebservice()
    }

    private fun callMembersWebservice() {
        try {
            val postdata = JSONObject()
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(
                this, requireContext(), WebServiceHelper.RestMethodType.GET,
                "v3/member/groups", "Get Members", postdata.toString()
            )
        } catch (e: Exception) {
            progress_dialog?.let { if (it.isShowing) AndroidUtils.dismiss_dialog(it) }
        }
    }

    private fun callViewGroupsWebservice() {
        try {
            val postdata = JSONObject()
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(
                this, requireContext(), WebServiceHelper.RestMethodType.GET,
                "v3/groups", "Get Groups", postdata.toString()
            )
        } catch (e: Exception) {
            progress_dialog?.let { if (it.isShowing) AndroidUtils.dismiss_dialog(it) }
        }
    }

    private fun loadViewGroupsRecylerview(TAG_TYPE: String, viewGroupModelArrayList: ArrayList<ViewGroupModel>, button: Button?) {
        if (button != null) {
            button.alpha = 0.5f
            button.isEnabled = false
        }
        if (Objects.equals(TAG_TYPE, "VG")) {
            adapter_view_groups = ViewGroupsAdpater(
                viewGroupModelArrayList, requireContext(), this, TAG_TYPE,
                new_itemClickListener, this@Groups
            )
            rv_view_groups?.let {
                adapter_view_groups?.setRecyclerView(it)
                it.adapter = adapter_view_groups
                AndroidUtils.LoadingRecyclerview(it, requireContext())
                AndroidUtils.setupBottomSpacerFooter(it, resources.getDimensionPixelSize(R.dimen.twentyeight_dp))
            }
        } else {
            try {
                rv_select_team_members?.layoutManager = GridLayoutManager(requireContext(), 1)
                if (Objects.equals(TAG_TYPE, "DG")) {
                    tv_practice_head?.visibility = View.GONE
                    adapter_view_groups = ViewGroupsAdpater(
                        viewGroupModelArrayList, requireContext(), this, TAG_TYPE,
                        new_itemClickListener, this@Groups, button
                    )
                    rv_select_team_members?.adapter = adapter_view_groups
                } else {
                    tv_practice_head?.visibility = View.VISIBLE
                    adapter_view_groups = ViewGroupsAdpater(
                        viewGroupMembersList, requireContext(), this, TAG_TYPE,
                        new_itemClickListener, this@Groups, button
                    )
                    rv_select_team_members?.adapter = adapter_view_groups
                    if (viewGroupMembersList.isEmpty()) {
                        rv_select_team_members?.visibility = View.GONE
                    } else {
                        rv_select_team_members?.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                Log.e("Tag", "Error " + e.message)
                e.fillInStackTrace()
            }
        }
        new_itemClickListener = object : ViewGroupsItemClickListener {
            override fun onClick(s: String) {
                rv_view_groups?.post {
                    group_head = s
                }
            }
        }

        et_search_tm?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                adapter_view_groups?.filter?.filter(et_search_tm?.text?.toString() ?: "")
            }
        })

        et_search?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {
                adapter_view_groups?.filter?.filter(et_search?.text?.toString() ?: "")
            }
        })
    }

    private fun loadRecylcerview(selectedTMArrayList: ArrayList<GroupModel>, tmType: String) {
        tv_create_group?.setTextColor(requireContext().resources.getColor(R.color.white))
        tv_view_group?.setTextColor(requireContext().resources.getColor(R.color.black))
        FLAG = "second_click"
        if (Objects.equals(tmType, "TM")) {
            select_ghead?.visibility = View.GONE
            ll_select_all?.visibility = View.VISIBLE

            tv_add_tm?.setTextColor(requireContext().resources.getColor(R.color.white))
            tv_practice_head?.setTextColor(requireContext().resources.getColor(R.color.black))
            tv_practice_head?.background = requireContext().resources.getDrawable(R.drawable.button_right_background)
            tv_add_tm?.background = requireContext().resources.getDrawable(R.drawable.button_left_green_background)
            rv_select_team_members?.removeAllViews()
            rv_select_team_members?.layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = GroupAdapters(selectedTMArrayList, tmType, itemClickListener, this@Groups)
            rv_select_team_members?.adapter = adapter
        } else {
            ll_select_all?.visibility = View.GONE
            select_ghead?.visibility = View.VISIBLE
            tv_add_tm?.background = requireContext().resources.getDrawable(R.drawable.button_left_background)
            tv_practice_head?.background = requireContext().resources.getDrawable(R.drawable.button_right_green_background)

            tv_add_tm?.setTextColor(requireContext().resources.getColor(R.color.black))
            tv_practice_head?.setTextColor(requireContext().resources.getColor(R.color.white))
            rv_select_team_members?.removeAllViews()
            rv_select_team_members?.layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = GroupAdapters(selectedTMArrayList, tmType, itemClickListener, this@Groups)
            rv_select_team_members?.adapter = adapter
        }

        et_search_tm?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {
                adapter?.filter?.filter(et_search_tm?.text?.toString() ?: "")
            }
        })

        itemClickListener = ItemClickListener { s ->
            rv_select_team_members?.post {
                group_head = s
            }
        }

        chk_select_all?.setOnClickListener {
            adapter?.selectOrDeselectAll(chk_select_all?.isChecked ?: false)
        }

        btn_save?.setOnClickListener {
            try {
                if (Objects.equals(tmType, "TM")) {
                    et_search_tm?.setText("")
                    group_head = ""
                    group_name = ""
                    assignGroupHead(TMArrayList)
                } else {
                    val gname = tv_group_name?.text?.toString()?.trim() ?: ""
                    val gdesc = tv_group_description?.text?.toString()?.trim() ?: ""
                    if (gname.isEmpty() && gdesc.isEmpty()) {
                        AndroidUtils.showAlert("Please check the Group Name , Description", requireActivity())
                    } else if (gname.isEmpty()) {
                        AndroidUtils.showAlert("Please check the Group Name", requireActivity())
                    } else if (gdesc.isEmpty()) {
                        AndroidUtils.showAlert("Please check the Description", requireActivity())
                    } else if (group_head == "") {
                        AndroidUtils.showAlert("Please select a group head", requireActivity())
                    } else {
                        ll_tm?.visibility = View.GONE
                        ll_select_all?.visibility = View.GONE
                        select_ghead?.visibility = View.GONE
                        tl_et_search?.visibility = View.GONE
                        callCreateGroupWebservice(gname, gdesc, assignGroupsList, group_head)
                    }
                }
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        }

        btn_cancel?.setOnClickListener {
            tv_add_tm?.setTextColor(requireContext().resources.getColor(R.color.white))
            tv_practice_head?.setTextColor(requireContext().resources.getColor(R.color.black))
            tv_practice_head?.background = requireContext().resources.getDrawable(R.drawable.button_right_background)
            tv_add_tm?.background = requireContext().resources.getDrawable(R.drawable.button_left_green_background)
            tv_group_name?.setText("")
            tv_group_description?.setText("")
            group_head = ""
            group_name = ""
            FLAG = "first_click"
            hideTM()
            chk_select_all?.isChecked = false
            select_ghead?.visibility = View.GONE
        }
    }

    private fun callCreateGroupWebservice(tv_group_name: String, tv_group_description: String, list_item: ArrayList<GroupModel>, group_head: String) {
        try {
            val postData = JSONObject()
            val members = JSONArray()
            for (i in 0 until list_item.size) {
                val model = list_item[i]
                if (model.isChecked) {
                    members.put(model.id)
                }
            }
            if (members.length() != 0 && group_head != "") {
                postData.put("name", tv_group_name)
                postData.put("description", tv_group_description)
                postData.put("groupHead", group_head)
                postData.put("members", members)
                WebServiceHelper.callHttpWebService(
                    this, requireContext(), WebServiceHelper.RestMethodType.POST,
                    "v3/group", "Create Groups", postData.toString()
                )
            } else if (members.length() != 0 && group_head == "") {
                AndroidUtils.showAlert("Please select a group head", requireActivity())
            } else {
                postData.put("name", tv_group_name)
                postData.put("description", tv_group_description)
                postData.put("groupHead", group_head)
                postData.put("members", members)
                WebServiceHelper.callHttpWebService(
                    this, requireContext(), WebServiceHelper.RestMethodType.POST,
                    "v3/group", "Create Groups", postData.toString()
                )
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    fun selectedtmlist(list_item: ArrayList<GroupModel>) {
        val btnSaveSelectedTm = btn_save_selected_tm
        if (btnSaveSelectedTm != null) {
            btnSaveSelectedTm.isEnabled = false
            btnSaveSelectedTm.alpha = 0.5f

            var isDifferent = false
            val updateGroupIds = ArrayList<String>()
            for (updateGroupModel in updateGroupMembersList) {
                updateGroupIds.add(updateGroupModel.group_id)
            }

            for (groupModel in list_item) {
                if (groupModel.isChecked) {
                    if (!updateGroupIds.contains(groupModel.id)) {
                        isDifferent = true
                        break
                    }
                }
            }

            if (!isDifferent) {
                for (updateGroupModel in updateGroupMembersList) {
                    var found = false
                    for (groupModel in list_item) {
                        if (groupModel.isChecked && groupModel.id == updateGroupModel.group_id) {
                            found = true
                            break
                        }
                    }
                    if (!found) {
                        isDifferent = true
                        break
                    }
                }
            }

            if (isDifferent) {
                btnSaveSelectedTm.isEnabled = true
                btnSaveSelectedTm.alpha = 1.0f
            }
        }
    }

    private fun assignGroupHead(list_item: ArrayList<GroupModel>) {
        assignGroupsList.clear()
        for (i in 0 until list_item.size) {
            val groupModel = list_item[i]
            if (groupModel.isChecked) {
                assignGroupsList.add(groupModel)
            }
        }
        TM_TYPE = "GH"
        if (assignGroupsList.isNotEmpty()) {
            val gname = tv_group_name?.text?.toString()?.trim() ?: ""
            val gdesc = tv_group_description?.text?.toString()?.trim() ?: ""
            if (gname.isEmpty() && gdesc.isEmpty()) {
                AndroidUtils.showAlert("Please check the Group Name , Description", requireActivity())
            } else if (gname.isEmpty()) {
                AndroidUtils.showAlert("Please check the Group Name", requireActivity())
            } else if (gdesc.isEmpty()) {
                AndroidUtils.showAlert("Please check the Description", requireActivity())
            } else {
                loadRecylcerview(assignGroupsList, TM_TYPE)
            }
        } else {
            AndroidUtils.showAlert("Please select atleast one team member", requireActivity())
        }
    }

    override fun onClick(view: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        progress_dialog?.let { if (it.isShowing) AndroidUtils.dismiss_dialog(it) }
        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent)
                when (httpResult.requestType) {
                    "Get Groups" -> {
                        val data = result.getJSONArray("data")
                        loadViewGroups(data)
                    }
                    "Get Members" -> {
                        val data = result.getJSONObject("data")
                        val users = data.getJSONArray("users")
                        loadMembers(users)
                    }
                    "Get Team Members" -> {
                        val data = result.getJSONObject("data")
                        val users = data.getJSONArray("users")
                        loadTeamMembers(users)
                    }
                    "Create Groups" -> {
                        if (result.has("errors")) {
                            val jsonArray = result.getJSONArray("errors")
                            val iserror = jsonArray.getJSONObject(0)
                            val msg = iserror.getString("msg")
                            AndroidUtils.showAlert(msg, requireActivity())
                        } else {
                            chk_select_all?.isChecked = false
                            group_head = ""
                            group_name = ""
                            ViewGroupsData()
                            mViewModel?.setData("View Groups")
                            AndroidUtils.showAlert(result.getString("msg"), requireActivity(), "")
                        }
                    }
                    "Update Groups" -> {
                        if (httpResult.status_code == 200) {
                            AndroidUtils.showAlert(result.getString("msg"), requireActivity(), "")
                            unhideData()
                            ViewGroupsData()
                            clear_list()
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), requireActivity(), "")
                        }
                    }
                    "Delete Groups" -> {
                        ViewGroupsData()
                        AndroidUtils.showAlert(result.getString("msg"), requireActivity(), "")
                        viewGroupModelArrayList.clear()
                        viewGroupMembersList.clear()
                        selectedTMArrayList.clear()
                        adapter?.getList_item()?.clear()
                        updateGroupMembersList.clear()
                    }
                    "Group Counts" -> {
                        if (httpResult.status_code == 200) {
                            val counts = result.optJSONObject("counts")
                            if (counts != null) {
                                tv_documents?.text = "${counts.optInt("documents")} Documents"
                                tv_matters?.text = "${counts.optInt("matters")} Matters"
                                tv_relationships?.text = "${counts.optInt("relationships")} Relationships"
                                tv_members?.text = "${counts.optInt("members")} Members"
                            }
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), requireActivity(), "")
                        }
                    }
                    "Update Group Head" -> {
                        if (httpResult.status_code == 200) {
                            AndroidUtils.showAlert(result.getString("msg"), requireActivity(), "")
                            mViewModel?.setData("View Groups")
                            unhideData()
                            viewGroupMembersList.clear()
                            viewGroupModelArrayList.clear()
                            ViewGroupsData()
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), requireActivity(), "")
                        }
                    }
                    "Search Results" -> {
                        val data = result.getJSONArray("data")
                        loadSearchResults(data)
                    }
                }
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        } else if (httpResult.result == WebServiceHelper.ServiceCallStatus.Failed) {
            try {
                val result = JSONObject(httpResult.responseContent)
                AndroidUtils.showErrorAlert(result.optString("msg"), requireActivity())
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        } else {
            AndroidUtils.showErrorAlert(httpResult.responseContent.toString(), requireActivity())
        }
    }

    private fun loadTeamMembers(users: JSONArray) {
        val ghName = new_viewGroupModel?.group_head_name ?: ""
        selectedTMArrayList.clear()
        for (i in 0 until users.length()) {
            val jsonObject = users.getJSONObject(i)
            val groupModel = GroupModel()
            groupModel.id = jsonObject.getString("id")
            groupModel.name = jsonObject.getString("name")

            val newViewGroupModel = new_viewGroupModel
            if (newViewGroupModel != null) {
                groupModel.isenabled = groupModel.id != newViewGroupModel.group_head_id
            }

            for (k in 0 until updateGroupMembersList.size) {
                if (groupModel.id == updateGroupMembersList[k].group_id) {
                    groupModel.isChecked = true
                }
            }

            if (groupModel.name != ghName) {
                selectedTMArrayList.add(groupModel)
            }
        }

        val name = new_viewGroupModel?.group_head_name ?: ""
        if (selectedTMArrayList.isNotEmpty() && name.isNotEmpty()) {
            group_head_name?.visibility = View.VISIBLE
            group_head_name?.text = "$name - Group Head"
        } else {
            group_head_name?.visibility = View.GONE
            group_head_name?.text = ""
        }

        TM_TYPE = "TM"
        loadTeamRecyclerview(selectedTMArrayList, TM_TYPE)
    }

    private fun loadTeamRecyclerview(selectedTMArrayList: ArrayList<GroupModel>, tmType: String) {
        btn_save_selected_tm = v?.findViewById(R.id.btn_save)
        btn_save_selected_tm?.let {
            it.alpha = 0.5f
            it.isEnabled = false
        }
        rv_select_team_members?.removeAllViews()
        rv_select_team_members?.layoutManager = GridLayoutManager(requireContext(), 1)
        adapter = GroupAdapters(selectedTMArrayList, tmType, itemClickListener, this@Groups, btn_save_selected_tm)
        rv_select_team_members?.adapter = adapter

        et_search_tm?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {
                adapter?.filter?.filter(et_search_tm?.text?.toString() ?: "")
            }
        })

        chk_select_all?.setOnClickListener {
            val isChecked = chk_select_all?.isChecked ?: false
            if (isChecked) {
                adapter?.selectOrDeselectAll(true)
                btn_save_selected_tm?.alpha = 1.0f
                btn_save_selected_tm?.isEnabled = true
            } else {
                adapter?.selectOrDeselectAll(false)
                btn_save_selected_tm?.alpha = 0.5f
                btn_save_selected_tm?.isEnabled = false
            }
        }

        val btn_cancel_selected_tm = v?.findViewById<AppCompatButton>(R.id.btn_cancel)
        btn_cancel_selected_tm?.setOnClickListener {
            unhideData()
            select_ghead?.visibility = View.GONE
            ll_select_all?.visibility = View.GONE
            chk_select_all?.isChecked = false
            et_search_tm?.setText("")
            ViewGroupsData()
            clear_list()
        }

        btn_save_selected_tm?.alpha = 0.5f
        btn_save_selected_tm?.setOnClickListener {
            val update_type = "UGM"
            try {
                new_viewGroupModel?.let { model ->
                    callUpdateGroups("", "", model.id, update_type, selectedTMArrayList)
                }
            } catch (e: JSONException) {
                e.fillInStackTrace()
            }
        }
    }

    private fun loadMembers(users: JSONArray) {
        selectedTMArrayList.clear()
        TMArrayList.clear()
        for (i in 0 until users.length()) {
            val jsonObject = users.getJSONObject(i)
            val groupModel = GroupModel()
            groupModel.id = jsonObject.getString("id")
            groupModel.name = jsonObject.getString("name")
            groupModel.isenabled = true
            TMArrayList.add(groupModel)
        }
        TM_TYPE = "TM"
        loadRecylcerview(TMArrayList, TM_TYPE)
    }

    private fun loadViewGroups(data: JSONArray) {
        viewGroupModelArrayList.clear()
        viewGroupNameArrayList.clear()
        for (i in 0 until data.length()) {
            val jsonObject = data.getJSONObject(i)
            val viewGroupModel = ViewGroupModel()
            viewGroupModel.id = jsonObject.optString("id")
            val dateStr = jsonObject.optString("created")
            viewGroupModel.isIsdisabled = jsonObject.optBoolean("isdisabled")
            if (dateStr.isNotEmpty()) {
                val date_new = AndroidUtils.stringToDateTimeDefault(dateStr, "MMM dd, yyyy, hh:mm a")
                val created = AndroidUtils.getDateToString(date_new, "MMM dd, yyyy | hh:mm a")
                viewGroupModel.created = created
            } else {
                viewGroupModel.created = ""
            }
            val members = jsonObject.optJSONArray("members")
            viewGroupModel.members = members
            viewGroupModel.memberCount = jsonObject.optString("memberCount")
            viewGroupModel.description = jsonObject.optString("description")
            viewGroupModel.name = jsonObject.optString("name")

            val group_head_obj = jsonObject.optJSONObject("groupHead")
            if (group_head_obj != null) {
                viewGroupModel.group_head_id = group_head_obj.optString("id")
                viewGroupModel.group_head_name = group_head_obj.optString("name")
                viewGroupModel.owner_name = group_head_obj.optString("name")
            }
            viewGroupModelArrayList.add(viewGroupModel)
        }

        viewGroupModelArrayList.sortWith(Comparator { a, b ->
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

        viewGroupNameArrayList.addAll(viewGroupModelArrayList)
        Log.i("ArrayList", "info " + viewGroupModelArrayList.toString())
        loadViewGroupsRecylerview("VG", viewGroupModelArrayList, null)
    }

    override fun EditGroup(viewGroupModel: ViewGroupModel) {
        btn_update?.setText(R.string.update)
        hideData()
        tv_group_name?.setText(viewGroupModel.name)
        tv_group_description?.setText(viewGroupModel.description)
        ll_deleteGroups?.visibility = View.GONE
        ll_edit_groups?.visibility = View.VISIBLE
        btn_cancel_edit?.setOnClickListener {
            mViewModel?.setData(getString(R.string.view_group))
            unhideData()
            ViewGroupsData()
        }
        btn_update?.setOnClickListener {
            val gname = tv_group_name?.text?.toString()?.trim() ?: ""
            val gdesc = tv_group_description?.text?.toString()?.trim() ?: ""
            if (gname.isEmpty() && gdesc.isEmpty()) {
                AndroidUtils.showAlert("Please check the Group Name , Description", requireActivity())
            } else if (gname.isEmpty()) {
                AndroidUtils.showAlert("Please check the Group Name", requireActivity())
            } else if (gdesc.isEmpty()) {
                AndroidUtils.showAlert("Please check the Description", requireActivity())
            } else {
                try {
                    val update_type = "EG"
                    val new_list = ArrayList<GroupModel>()
                    callUpdateGroups(gname, gdesc, viewGroupModel.id, update_type, new_list)
                } catch (ex: JSONException) {
                    ex.fillInStackTrace()
                }
            }
        }
    }

    override fun DeleteGroup(viewGroupModel: ViewGroupModel, itemsArrayList: ArrayList<ViewGroupModel>) {
        hideData()
        hidedelete_data()
        group_head_name?.visibility = View.GONE
        group_head = ""
        group_name = ""
        isDelete = true
        AndroidUtils.DisplaySpinnerView(sp_groupslist, tv_delete_groups, group_head, dropdown_icon, clear_icon, false, GroupsAdapter, "Search Groups")
        et_search?.setText("")
        et_search_tm?.setText("")
        et_search_delete?.setText("")
        ll_deleteGroups?.visibility = View.VISIBLE
        for (i in 0 until itemsArrayList.size) {
            if (viewGroupModel.name == itemsArrayList[i].name) {
                itemsArrayList.removeAt(i)
                break
            }
        }
        ll_Delete_view?.visibility = View.VISIBLE
        callGroupsCounts(viewGroupModel.id)
        group_name_delete?.text = viewGroupModel.name
        tv_group_name?.setText(viewGroupModel.name)
        tv_group_name?.isEnabled = false
        tv_group_description?.setText(viewGroupModel.description)
        tv_group_description?.isEnabled = false
        val btn_delete = v?.findViewById<AppCompatButton>(R.id.btn_update)
        val btn_cancel_btn = v?.findViewById<AppCompatButton>(R.id.btn_cancel_edit)
        btn_delete?.setText(requireContext().resources.getString(R.string.delete))
        btn_delete?.isEnabled = false
        btn_delete?.alpha = 0.4f
        Log.i("TAG", "INFO$itemsArrayList")

        val nameList = ArrayList<String>()
        for (item in itemsArrayList) {
            nameList.add(item.name)
        }

        GroupsAdapter = CommonSpinnerAdapter(requireActivity(), nameList)
        sp_groupslist?.adapter = GroupsAdapter

        tv_sp_groups?.setOnClickListener {
            val isVisible = sp_groupslist?.visibility == View.VISIBLE
            AndroidUtils.DisplaySpinnerView(sp_groupslist, tv_delete_groups, group_name, dropdown_icon, clear_icon, !isVisible, GroupsAdapter, "Search Groups")
            AndroidUtils.display_listview(isDelete, sp_groupslist)
            isDelete = !isDelete
        }

        sp_groupslist?.setOnItemClickListener { _, _, position, _ ->
            group_head = itemsArrayList[position].id
            group_name = GroupsAdapter?.getItem(position)?.toString() ?: ""
            AndroidUtils.DisplaySpinnerView(sp_groupslist, tv_delete_groups, group_name, dropdown_icon, clear_icon, false, GroupsAdapter, "Search Groups")
            btn_delete?.alpha = 1.0f
            btn_delete?.isEnabled = true
            isDelete = true
        }

        clear_icon?.setOnClickListener {
            AndroidUtils.DisplaySpinnerView(sp_groupslist, tv_delete_groups, group_head, dropdown_icon, clear_icon, false, GroupsAdapter, "Search Groups")
            btn_delete?.alpha = 0.4f
            btn_delete?.isEnabled = false
            isDelete = true
        }

        btn_cancel_btn?.setOnClickListener {
            mViewModel?.setData(getString(R.string.view_group))
            group_head = ""
            group_name = ""
            reverse_data()
            ViewGroupsData()
            loadViewGroupsRecylerview("VG", itemsArrayList, null)
        }

        btn_delete?.setOnClickListener {
            if (group_head.isEmpty()) {
                AndroidUtils.showAlert("Please select atleast one Group", requireActivity())
            } else {
                Delete_Popup(viewGroupModel, itemsArrayList)
            }
        }
    }

    private fun Delete_Popup(viewGroupModel: ViewGroupModel, itemsArrayList: ArrayList<ViewGroupModel>) {
        try {
            val dialogBuilder = AlertDialog.Builder(requireContext())
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.delete_relationship, null)
            val tv_confirmation = view.findViewById<TextView>(R.id.tv_confirmation)
            val header_name = view.findViewById<TextView>(R.id.header_name)
            header_name.text = getString(R.string.confirmation)
            header_name.textSize = DynamicUtils.twenty.toFloat()
            header_name.typeface = Typeface.DEFAULT_BOLD
            header_name.setTextColor(Color.BLACK)
            val close_documents = view.findViewById<ImageView>(R.id.close_documents)
            val ConfirmDelText = getString(R.string.delete_group) + viewGroupModel.name + "?"

            tv_confirmation.text = ConfirmDelText

            val bt_yes = view.findViewById<AppCompatButton>(R.id.btn_yes)
            val btn_no = view.findViewById<AppCompatButton>(R.id.btn_No)
            val dialog = dialogBuilder.create()

            btn_no.setOnClickListener {
                dialog.dismiss()
                ViewGroupsData()
            }
            close_documents.setOnClickListener {
                dialog.dismiss()
            }
            bt_yes.setOnClickListener {
                dialog.dismiss()
                unhideData()
                reverse_data()
                ViewGroupsData()
                try {
                    callDeleteGroups(viewGroupModel.id)
                    loadViewGroupsRecylerview("VG", itemsArrayList, null)
                } catch (ex: JSONException) {
                    ex.fillInStackTrace()
                }
            }
            dialog.setView(view)
            dialog.show()
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, requireActivity())
        }
    }

    override fun CGH(viewGroupModel: ViewGroupModel, itemsArrayList: ArrayList<ViewGroupModel>) {
        val btn_save_CGH = v?.findViewById<AppCompatButton>(R.id.btn_save)
        unhide_delete_data()
        btn_save?.setText(R.string.update)
        hide_CGH_UGM_data()
        et_search_tm?.visibility = View.VISIBLE
        try {
            viewGroupMembersList.clear()
            group_head_name?.text = "${viewGroupModel.group_head_name} - Group Head"
            if (!viewGroupModel.group_head_name.isEmpty()) {
                group_head_name?.visibility = View.VISIBLE
            } else {
                group_head_name?.visibility = View.GONE
            }
            tv_group_name?.setText(viewGroupModel.name)
            val ghname = viewGroupModel.group_head_name
            tv_group_description?.setText(viewGroupModel.description)
            val mtag = "CGH"

            val members = viewGroupModel.members
            if (members != null) {
                for (i in 0 until members.length()) {
                    val viewGroupModel_1 = ViewGroupModel()
                    val jsonObject = members.getJSONObject(i)
                    viewGroupModel_1.group_name = jsonObject.getString("name")
                    viewGroupModel_1.group_id = jsonObject.getString("id")
                    if (viewGroupModel_1.group_name != ghname) {
                        viewGroupMembersList.add(viewGroupModel_1)
                    }
                }
            }
            loadViewGroupsRecylerview(mtag, viewGroupModelArrayList, btn_save_CGH)
            val btn_cancel_CGH = v?.findViewById<AppCompatButton>(R.id.btn_cancel)
            btn_cancel_CGH?.setOnClickListener {
                mViewModel?.setData("View Groups")
                unhideData()
                viewGroupMembersList.clear()
                viewGroupModelArrayList.clear()
                ViewGroupsData()
            }

            btn_save_CGH?.setOnClickListener {
                mViewModel?.setData("View Groups")
                if (group_head.isEmpty()) {
                    AndroidUtils.showAlert("Please select a Group Head", requireActivity())
                } else {
                    try {
                        callUpdateGroupHeadWebservice(viewGroupModel.id, group_head)
                    } catch (ex: JSONException) {
                        ex.fillInStackTrace()
                    }
                }
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
    }

    override fun UGM(viewGroupModel: ViewGroupModel) {
        new_viewGroupModel = viewGroupModel
        et_search?.setText("")
        et_search_tm?.setText("")
        unhide_delete_data()
        btn_save?.setText(R.string.update)
        hide_CGH_UGM_data()
        ll_select_all?.visibility = View.VISIBLE
        tv_label?.visibility = View.VISIBLE
        select_ghead?.visibility = View.GONE
        val members = viewGroupModel.members
        if (members != null) {
            for (i in 0 until members.length()) {
                val viewGroupModel_1 = ViewGroupModel()
                val jsonObject = members.getJSONObject(i)
                viewGroupModel_1.group_name = jsonObject.getString("name")
                viewGroupModel_1.group_id = jsonObject.getString("id")
                updateGroupMembersList.add(viewGroupModel_1)
            }
        }
        callViewGroupMembersWebservice()
    }

    override fun GAL(viewGroupModel: ViewGroupModel) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.group_activity_log)
        dialog.window?.setBackgroundDrawable(requireContext().getDrawable(R.color.pearl))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(true)

        val spCategory = dialog.findViewById<Spinner>(R.id.sp_category)
        val category_id_txt = dialog.findViewById<TextView>(R.id.category_id)
        val client_id_name_txt = dialog.findViewById<TextView>(R.id.client_id_name)
        val team_member_id_txt = dialog.findViewById<TextView>(R.id.team_member_id)
        category_id_txt.text = getString(R.string.category)
        client_id_name_txt.text = getString(R.string.client)
        team_member_id_txt.text = getString(R.string.team_member)
        val search_name_txt = dialog.findViewById<TextView>(R.id.search_name)
        search_name_txt.text = getString(R.string.search)
        val et_search_name = dialog.findViewById<TextInputEditText>(R.id.et_search_name)
        val et_client_name = dialog.findViewById<TextInputEditText>(R.id.et_client_name)
        val et_team_member = dialog.findViewById<TextInputEditText>(R.id.et_team_member)
        val tvFromDate = dialog.findViewById<Button>(R.id.btn_from_date)
        val tvToDate = dialog.findViewById<Button>(R.id.btn_to_date)
        rv_activity_log = dialog.findViewById(R.id.rv_view_activity_log)
        val cancelIcon = dialog.findViewById<ImageView>(R.id.cancelIcon)
        val from_id_txt = dialog.findViewById<TextView>(R.id.from_id)
        val to_id_txt = dialog.findViewById<TextView>(R.id.to_id)
        from_id_txt.text = getString(R.string.from)
        to_id_txt.text = getString(R.string.to)
        val btnSearch = dialog.findViewById<Button>(R.id.btn_update_activity_log)
        btnSearch.text = getString(R.string.search)
        val tl_search_appointments = dialog.findViewById<View>(R.id.tl_et_search_message)
        et_search_message = tl_search_appointments.findViewById(R.id.et_Search)

        et_search_message?.setHint(R.string.search)
        et_search_name.setHint(R.string.search)

        et_client_name.setHint(R.string.client)
        et_team_member.setHint(R.string.team_member)
        tvFromDate.setHint(R.string.from)
        tvToDate.setHint(R.string.to)

        val actionsList = ArrayList<ActionModel>()
        val viewGroupMembersList_local = ArrayList<ViewGroupModel>()

        actionsList.add(ActionModel("Authorization"))
        actionsList.add(ActionModel("Groups"))
        actionsList.add(ActionModel("Team Members"))
        actionsList.add(ActionModel("Relationships"))
        actionsList.add(ActionModel("Share"))
        actionsList.add(ActionModel("Documents"))
        actionsList.add(ActionModel("Merge PDF"))
        actionsList.add(ActionModel("Matters"))
        actionsList.add(ActionModel("Timesheets"))

        val selectedCategory = arrayOf("")
        selectedCategory[0] = actionsList[1].name
        val spinnerAdapter = CommonSpinnerAdapter(requireActivity(), actionsList)
        spCategory.adapter = spinnerAdapter
        var defaultPosition = 0
        for (i in 0 until actionsList.size) {
            if (actionsList[i].name.equals("Groups", ignoreCase = true)) {
                defaultPosition = i
                break
            }
        }
        spCategory.setSelection(defaultPosition)
        selectedCategory[0] = actionsList[defaultPosition].name

        try {
            val members = viewGroupModel.members
            if (members != null) {
                for (i in 0 until members.length()) {
                    val jsonObject = members.getJSONObject(i)
                    val member = ViewGroupModel()
                    member.group_name = jsonObject.getString("name")
                    member.group_id = jsonObject.getString("id")
                    viewGroupMembersList_local.add(member)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                selectedCategory[0] = actionsList[i].name
            }
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        tvFromDate.setOnClickListener { datepicker(tvFromDate) }
        tvToDate.setOnClickListener { datepicker(tvToDate) }

        btnSearch.setOnClickListener {
            try {
                callSearchResultsWebservice(
                    selectedCategory[0],
                    et_team_member.text.toString(),
                    et_client_name.text.toString(),
                    tvFromDate.text.toString(),
                    et_search_name.text.toString(),
                    tvToDate.text.toString(),
                    viewGroupModel.id
                )
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        cancelIcon.setOnClickListener { dialog.dismiss() }
        callSearchResultsWebservice(
            selectedCategory[0],
            et_team_member.text.toString(),
            et_client_name.text.toString(),
            tvFromDate.text.toString(),
            et_search_name.text.toString(),
            tvToDate.text.toString(),
            viewGroupModel.id
        )
        dialog.show()
    }

    private fun loadSearchResults(data: JSONArray) {
        searchList.clear()
        for (i in 0 until data.length()) {
            val searchDo = SearchDo()
            val jsonObject = data.getJSONObject(i)
            searchDo.category = jsonObject.getString("category")
            searchDo.timestamp = jsonObject.getString("timestamp")
            searchDo.msg = jsonObject.getString("msg")
            searchList.add(searchDo)
        }
        loadSearchRecyclerview()
    }

    private fun loadSearchRecyclerview() {
        rv_activity_log?.layoutManager = GridLayoutManager(requireContext(), 1)
        val adapterSearch = SearchAdapter(searchList)
        rv_activity_log?.adapter = adapterSearch
        et_search_message?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                adapterSearch.filter.filter(et_search_message?.text?.toString() ?: "")
            }
        })
    }

    private fun callSearchResultsWebservice(selected_category: String, selected_tm: String, client: String, from_date: String, search: String, to_date: String, id: String) {
        val postdate = JSONObject()
        postdate.put("category", selected_category)
        postdate.put("client", client)
        postdate.put("fromDate", from_date)
        postdate.put("search", search)
        postdate.put("tm", selected_tm)
        postdate.put("toDate", to_date)
        WebServiceHelper.callHttpWebService(
            this, requireContext(), WebServiceHelper.RestMethodType.PUT,
            "v3/auditlogs/$id", "Search Results", postdate.toString()
        )
    }

    private fun datepicker(bt_date: Button) {
        val myCalendar = Calendar.getInstance()
        val date = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val myFormat = "dd-MM-yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            bt_date.text = sdf.format(myCalendar.time)
        }
        val datePickerDialog = DatePickerDialog(
            requireActivity(), date,
            myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun callViewGroupMembersWebservice() {
        try {
            val postdata = JSONObject()
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(
                this, requireContext(), WebServiceHelper.RestMethodType.GET,
                "v3/member/groups", "Get Team Members", postdata.toString()
            )
        } catch (e: Exception) {
            progress_dialog?.let { if (it.isShowing) AndroidUtils.dismiss_dialog(it) }
        }
    }

    private fun callUpdateGroupHeadWebservice(id: String, group_head: String) {
        try {
            val postData = JSONObject()
            postData.put("groupHead", group_head)
            Log.i("Tag", "Info:$id")
            WebServiceHelper.callHttpWebService(
                this, requireContext(), WebServiceHelper.RestMethodType.PATCH,
                "v3/group/$id", "Update Group Head", postData.toString()
            )
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun hide_CGH_UGM_data() {
        ll_group_list?.visibility = View.GONE
        ll_tm?.visibility = View.GONE
        select_ghead?.visibility = View.GONE
        ll_select_all?.visibility = View.GONE
        ll_select_tm?.visibility = View.VISIBLE
        rv_view_groups?.visibility = View.GONE
        rv_select_team_members?.visibility = View.VISIBLE
        tl_et_search?.visibility = View.GONE
        cv_groups?.visibility = View.GONE
        cv_details?.visibility = View.VISIBLE
        ll_edit_groups?.visibility = View.GONE
        tv_select_team_members?.visibility = View.GONE
    }

    private fun callDeleteGroups(id: String) {
        try {
            val postData = JSONObject()
            WebServiceHelper.callHttpWebService(
                this, requireContext(), WebServiceHelper.RestMethodType.DELETE,
                "v3/group/$id/$group_head", "Delete Groups", postData.toString()
            )
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun callGroupsCounts(id: String) {
        try {
            val postData = JSONObject()
            WebServiceHelper.callHttpWebService(
                this, requireContext(), WebServiceHelper.RestMethodType.GET,
                "v3/group/resources/counts/$id", "Group Counts", postData.toString()
            )
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun callUpdateGroups(group_name: String, description: String, id: String, update_type: String, list_item: ArrayList<GroupModel>) {
        try {
            val postData = JSONObject()
            val jsonArray = JSONArray()
            if (Objects.equals(update_type, "EG")) {
                postData.put("name", group_name)
                postData.put("description", description)
                WebServiceHelper.callHttpWebService(
                    this, requireContext(), WebServiceHelper.RestMethodType.PATCH,
                    "v3/group/$id", "Update Groups", postData.toString()
                )
            } else if (Objects.equals(update_type, "UGM")) {
                for (i in 0 until list_item.size) {
                    val model = list_item[i]
                    if (model.isChecked) {
                        jsonArray.put(model.id)
                    }
                }
                val groupHeadId = new_viewGroupModel?.group_head_id ?: ""
                if (groupHeadId.isNotEmpty()) {
                    var alreadyIncluded = false
                    for (i in 0 until jsonArray.length()) {
                        if (groupHeadId == jsonArray.getString(i)) {
                            alreadyIncluded = true
                            break
                        }
                    }
                    if (!alreadyIncluded) {
                        jsonArray.put(groupHeadId)
                    }
                }
                postData.put("members", jsonArray)
                WebServiceHelper.callHttpWebService(
                    this, requireContext(), WebServiceHelper.RestMethodType.PATCH,
                    "v3/group/$id", "Update Groups", postData.toString()
                )
            }
            Log.i("TAG", "Object:$postData:$id")
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun clear_list() {
        viewGroupModelArrayList.clear()
        viewGroupMembersList.clear()
        selectedTMArrayList.clear()
        adapter?.getList_item()?.clear()
        updateGroupMembersList.clear()
    }

    private fun hideData() {
        ll_group_list?.visibility = View.GONE
        ll_tm?.visibility = View.GONE
        select_ghead?.visibility = View.GONE
        ll_select_all?.visibility = View.GONE
        ll_select_tm?.visibility = View.VISIBLE
        rv_view_groups?.visibility = View.GONE
        rv_select_team_members?.visibility = View.VISIBLE
        tl_et_search?.visibility = View.GONE
        cv_groups?.visibility = View.VISIBLE
        ll_edit_groups?.visibility = View.VISIBLE
        tv_select_team_members?.visibility = View.GONE
    }

    private fun reverse_data() {
        tv_group_description?.isEnabled = true
        tv_group_name?.isEnabled = true
        btn_update?.visibility = View.VISIBLE
        btn_cancel_edit?.visibility = View.VISIBLE
        cv_details?.visibility = View.GONE
        unhideData()
    }

    fun hidedelete_data() {
        et_search_delete?.visibility = View.VISIBLE
        et_search_tm?.visibility = View.GONE
    }

    fun unhide_delete_data() {
        et_search_delete?.visibility = View.GONE
        et_search_tm?.visibility = View.VISIBLE
    }

    fun unhide_cgroup() {
        ll_edit_groups?.visibility = View.VISIBLE
    }

    private fun unhideData() {
        ll_group_list?.visibility = View.VISIBLE
        ll_tm?.visibility = View.VISIBLE
        ll_select_all?.visibility = View.VISIBLE
        select_ghead?.visibility = View.GONE
        ll_select_tm?.visibility = View.VISIBLE
        rv_view_groups?.visibility = View.VISIBLE
        tl_et_search?.visibility = View.VISIBLE
        cv_groups?.visibility = View.GONE
        ll_edit_groups?.visibility = View.GONE
        tv_select_team_members?.visibility = View.VISIBLE
        cv_activity_log?.visibility = View.GONE
    }

    fun check_select_all(check_status: Boolean) {
        chk_select_all?.isChecked = check_status
    }

    fun page_name(action_list: String) {
        val model = mViewModel ?: return
        when (action_list) {
            "Edit Group Info" -> model.setData("Edit Group Info")
            "Assign Group" -> model.setData("Assign Group")
            "Update Group Members List" -> model.setData("Update Group Members List")
            "Update Group Head" -> model.setData("Update Group Head")
            "Group Activity Log" -> model.setData("Group Activity Log")
            else -> model.setData("View Groups")
        }
    }
}
