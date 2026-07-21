package com.digicoffer.lauditor.Matter.ViewModels

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.TooltipCompat
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.DescriptionValidation
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.digicoffer.lauditor.Matter.Models.AdvocateModel
import com.digicoffer.lauditor.Matter.Models.MatterModel
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.HashMap
import java.util.List
import java.util.Locale
import java.util.Objects
import java.util.regex.Pattern

class MatterInformation_En : Fragment(), View.OnClickListener, AsyncTaskCompleteListener {
    var et_matter_title: TextInputEditText? = null
    var et_matter_num: TextInputEditText? = null
    var et_matter_description: TextInputEditText? = null
    var et_court: TextInputEditText? = null
    var et_judge: TextInputEditText? = null
    var et_matter_tags: TextInputEditText? = null
    var et_matter_type: TextInputEditText? = null
    var tv_start_date: AppCompatButton? = null
    var tv_end_date: AppCompatButton? = null
    var tv_dof: AppCompatButton? = null
    var tv_create_date: AppCompatButton? = null
    private val pagebuttons: MutableList<TextView> = ArrayList()
    var sp_case_type: ListView? = null
    var isCancelClicked: Boolean = false
    var matter_type: TextView? = null
    var et_case_type: TextView? = null
    var defaultTxt: TextView? = null
    var tv_high_priority: TextView? = null
    var tv_medium_priority: TextView? = null
    var tv_low_priority: TextView? = null
    var tv_status_active: TextView? = null
    var tv_status_pending: TextView? = null
    var Title: TextView? = null
    var datefill: TextView? = null
    var create_date: TextView? = null
    var start_date: TextView? = null
    var closedate: TextView? = null
    var court: TextView? = null
    var judge: TextView? = null
    var priority: TextView? = null
    var status: TextView? = null
    var addopponentadvocate: TextView? = null
    var tv_matter_tags: TextView? = null
    var matter_title: TextView? = null
    var matter_date: TextView? = null
    var btn_add_advocate: Button? = null
    var btn_cancel_edit: Button? = null
    private var currentPage = 1
    private var isInitialLoad = true
    var matterModel = MatterModel()
    var tag_list = ArrayList<String>()
    var caseTypeList = ArrayList<String>()
    private var editPosition = -1
    var edit_matter_page_icon: ShapeableImageView? = null
    var edit_matter_page_txt: TextView? = null
    private var end_index = 0
    var isChangesOccured = false
    private var pageNumber = 0
    var isCaseTypeChecked = true
    private var progress_dialog: Dialog? = null
    var ad_name = ""
    var ad_email = ""
    var ad_phone = ""
    var clients = JSONArray()
    var documents = JSONArray()
    var members = JSONArray()
    var groups = JSONArray()
    var group_acls = JSONArray()
    var advocates = JSONArray()
    var tags = JSONArray()
    var iv_remove_matter: ImageView? = null
    var existing_clients: JSONArray? = null
    var exisiting_group_acls: JSONArray? = null
    var existing_corp_clients: JSONArray? = null
    var existing_temp_list: JSONArray? = null
    var existing_members: JSONArray? = null
    var existing_groups_list: JSONArray? = null
    var existing_clients_list: JSONArray? = null
    var existing_tm_list: JSONArray? = null
    var existing_documents: JSONArray? = null
    var existing_documents_list: JSONArray? = null
    var existing_tags_list = JSONArray()

    var ll_case_type: LinearLayout? = null
    var ll_page_navigaiton: LinearLayout? = null
    var ll_matter_id: LinearLayout? = null
    var pageNumberLayout: LinearLayout? = null
    var ll_selected_advocates: LinearLayout? = null
    var tl_case_type: LinearLayout? = null
    var ll_case_num: LinearLayout? = null
    var ll_matter_type: LinearLayout? = null
    var scrollView: HorizontalScrollView? = null
    var advocates_list = ArrayList<AdvocateModel>()

    var matterArraylist: ArrayList<MatterModel>? = null
    var matter: Matter? = null
    var m_c_number: TextView? = null
    var m_c_type: TextView? = null
    var description_name: TextView? = null
    var matter_id: TextView? = null
    var btn_cancel_save: AppCompatButton? = null
    var btn_add_tag: AppCompatButton? = null
    var btn_create: AppCompatButton? = null
    var ll_add_advocate: LinearLayout? = null
    var ll_added_tags: LinearLayout? = null
    var ll_date: LinearLayout? = null
    var ll_start_date: LinearLayout? = null
    var ll_end_date: LinearLayout? = null
    var ll_court: LinearLayout? = null
    var ll_judge: LinearLayout? = null
    var ll_dof: LinearLayout? = null
    var ll_create_date: LinearLayout? = null
    var ll_matter_tags: LinearLayout? = null
    var other_information: LinearLayout? = null
    var et_matter_tag: TextInputEditText? = null
    var ll_matter_title: RelativeLayout? = null
    var ll_header: LinearLayoutCompat? = null
    var existing_opponents: JSONArray? = null
    var cv_client_details: CardView? = null
    var advocate_title: TextView? = null
    var advocate_email: TextView? = null
    var advocate_phone: TextView? = null
    var CASE_PRIORITY = "High"
    var STATUS = "Active"
    var oldStatus = ""
    var oldPriority = ""
    var isMoreDetailsChecked: Boolean = true

    var ll_opponent_advocate: LinearLayout? = null
    var ll_matterDate: LinearLayout? = null
    var et_advocate_name: TextInputEditText? = null
    var et_advocate_email: TextInputEditText? = null
    var et_advocate_phone: TextInputEditText? = null
    var et_matter_id: TextInputEditText? = null
    var btn_cancel_tag: AppCompatButton? = null
    var btn_save_tag: AppCompatButton? = null
    var btn_add_more_info: TextView? = null
    var iv_backward_button: ImageView? = null
    var iv_forward_button: ImageView? = null
    var iv_remove_tag: ImageView? = null
    var img_clear_icon: ImageView? = null
    private var mDateSetListener: DatePickerDialog.OnDateSetListener? = null
    var tv_response: TextView? = null
    var tv_reponse_email: TextView? = null
    var tv_response_phone: TextView? = null
    private val finalMyCalendar = Calendar.getInstance()
    var viewMatterModel1: ViewMatterModel? = null

    companion object {
        private const val TAG = "MatterInformation"
    }

    @SuppressLint("MissingInflatedId", "SetTextI18n", "ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.matter_information_en, container, false)
        matter_date = view.findViewById(R.id.matter_date)
        ll_matter_type = view.findViewById(R.id.ll_matter_type)
        iv_remove_matter = view.findViewById(R.id.iv_remove_matter)
        matter_type = view.findViewById(R.id.matter_type)
        ll_case_type = view.findViewById(R.id.ll_case_type)
        matter_type?.setText(R.string.matter_type)
        defaultTxt = view.findViewById(R.id.defaultTxt)
        defaultTxt?.setText(R.string.auto_generated_txt)
        defaultTxt?.setTextColor(requireContext().getColor(R.color.light_blue))
        ll_matterDate = view.findViewById(R.id.ll_matterDate)
        ll_matterDate?.visibility = View.GONE
        ll_matter_id = view.findViewById(R.id.ll_matter_id)
        ll_matter_id?.alpha = 0.6f
        et_matter_title = view.findViewById(R.id.et_matter_title)
        matter_title = view.findViewById(R.id.matter_title)
        edit_matter_page_icon = view.findViewById(R.id.edit_matter_page_icon)
        edit_matter_page_txt = view.findViewById(R.id.edit_matter_page_txt)
        et_matter_title?.addTextChangedListener(Validation(et_matter_title))
        et_matter_title?.setHint(R.string.case_title)
        ll_header = view.findViewById(R.id.ll_header)
        ll_opponent_advocate = view.findViewById(R.id.ll_opponent_advocate)
        ll_case_num = view.findViewById(R.id.ll_case_num)
        ll_opponent_advocate?.visibility = View.GONE
        btn_save_tag = view.findViewById(R.id.btn_save_tag)
        ll_page_navigaiton = view.findViewById(R.id.ll_page_navigaiton)
        ll_page_navigaiton?.visibility = View.GONE
        ll_selected_advocates = view.findViewById(R.id.ll_selected_advocates)
        scrollView = view.findViewById(R.id.PageScrollView)
        pageNumberLayout = view.findViewById(R.id.pageNumberLayout)
        iv_backward_button = view.findViewById(R.id.iv_backward_button)
        iv_backward_button?.visibility = View.VISIBLE
        iv_forward_button = view.findViewById(R.id.iv_forward_button)
        iv_forward_button?.setImageDrawable(requireContext().getDrawable(R.drawable.baseline_arrow_forward_ios_24))
        btn_cancel_tag = view.findViewById(R.id.btn_cancel_tag)
        iv_remove_tag = view.findViewById(R.id.iv_remove_tag)
        btn_cancel_tag?.setText(R.string.remove)
        et_advocate_name = view.findViewById(R.id.et_advocate_name)
        et_advocate_name?.addTextChangedListener(Validation(et_advocate_name))
        et_advocate_email = view.findViewById(R.id.et_advocate_email)
        et_advocate_email?.addTextChangedListener(Validation(et_advocate_email))
        et_advocate_phone = view.findViewById(R.id.et_advocate_phone)
        matter_id = view.findViewById(R.id.matter_id)
        matter_id?.setText(R.string.matter_number)
        et_matter_id = view.findViewById(R.id.et_matter_id)
        et_matter_id?.isEnabled = false

        et_matter_id?.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                TooltipCompat.setTooltipText(et_matter_id!!, "This field is read-only")
                et_matter_id?.performLongClick()
                return@setOnTouchListener true
            }
            false
        }

        et_advocate_phone?.addTextChangedListener(Validation(et_advocate_phone))
        val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(10))
        et_advocate_phone?.filters = filters
        advocate_title = view.findViewById(R.id.advocate_title)
        advocate_title?.setText(R.string.name)
        advocate_email = view.findViewById(R.id.advocate_email)
        advocate_email?.setText(R.string.email)
        advocate_phone = view.findViewById(R.id.advocate_phone)
        advocate_phone?.setText(R.string.phone_number)

        et_matter_title?.setTextSize(TypedValue.COMPLEX_UNIT_SP, DynamicUtils.fifteen.toFloat())
        et_matter_num = view.findViewById(R.id.et_matter_num)
        if (Constants.MATTER_TYPE == "Legal") {
            ll_case_num?.visibility = View.VISIBLE
            et_matter_num?.setHint(R.string.case_number)
        } else {
            ll_case_num?.visibility = View.GONE
            et_matter_num?.setHint(R.string.matter_number)
        }
        et_matter_num?.addTextChangedListener(Validation(et_matter_num))

        et_matter_num?.setTextSize(TypedValue.COMPLEX_UNIT_SP, DynamicUtils.fifteen.toFloat())
        btn_cancel_edit = view.findViewById(R.id.btn_cancel_edit)
        tl_case_type = view.findViewById(R.id.tl_case_type)
        et_matter_type = view.findViewById(R.id.et_matter_type)
        tl_case_type?.visibility = View.VISIBLE
        et_matter_type?.visibility = View.GONE
        et_case_type = tl_case_type?.findViewById(R.id.tv_spinner_view)
        val img_dropdown_icon = tl_case_type?.findViewById<ImageView>(R.id.img_dropdown_icon)
        sp_case_type = view.findViewById(R.id.sp_case_type)
        if (Constants.MATTER_TYPE == "Legal") {
            et_case_type?.setHint(R.string.select_case_type)
            ll_matter_type?.visibility = View.GONE
            ll_case_type?.visibility = View.VISIBLE
        } else {
            ll_matter_type?.visibility = View.VISIBLE
            ll_case_type?.visibility = View.GONE
            et_matter_type?.setHint(R.string.matter_type)
            et_case_type?.setHint(R.string.select_case_type)
        }
        et_case_type?.setOnClickListener {
            if (isCaseTypeChecked) sp_case_type?.visibility = View.VISIBLE
            else sp_case_type?.visibility = View.GONE
            isCaseTypeChecked = !isCaseTypeChecked
        }
        img_dropdown_icon?.setOnClickListener {
            if (isCaseTypeChecked) sp_case_type?.visibility = View.VISIBLE
            else sp_case_type?.visibility = View.GONE
            isCaseTypeChecked = !isCaseTypeChecked
        }
        description_name = view.findViewById(R.id.description_name)
        description_name?.setText(R.string.description)

        Title = view.findViewById(R.id.Title_name)
        Title?.setText(R.string.case_title)
        datefill = view.findViewById(R.id.datefill)
        datefill?.setText(R.string.date_of_filing)

        create_date = view.findViewById(R.id.create_date)
        create_date?.setText(R.string.created_date)

        start_date = view.findViewById(R.id.start_date)
        start_date?.setText(R.string.start_date)
        closedate = view.findViewById(R.id.closedate)
        closedate?.setText(R.string.close_date)
        court = view.findViewById(R.id.court)
        court?.setText(R.string.court)
        judge = view.findViewById(R.id.judge)
        judge?.setText(R.string.judge_s)
        priority = view.findViewById(R.id.priority)
        priority?.setText(R.string.priority)
        status = view.findViewById(R.id.status)
        status?.setText(R.string.status)
        addopponentadvocate = view.findViewById(R.id.addopponentadvocate)
        addopponentadvocate?.setText(R.string.opponent_advocates)
        addopponentadvocate?.setTextSize(TypedValue.COMPLEX_UNIT_SP, DynamicUtils.eighteen.toFloat())
        ll_court = view.findViewById(R.id.ll_court)
        ll_judge = view.findViewById(R.id.ll_judge)
        ll_dof = view.findViewById(R.id.ll_dof)
        ll_create_date = view.findViewById(R.id.ll_create_date)
        ll_matter_title = view.findViewById(R.id.ll_matter_title)
        btn_add_more_info = view.findViewById(R.id.btn_add_more_info)
        other_information = view.findViewById(R.id.other_information)
        ll_matter_tags = view.findViewById(R.id.ll_matter_tags)
        tv_matter_tags = view.findViewById(R.id.tv_matter_tags)
        tv_matter_tags?.setText(R.string.matter_tags)
        btn_add_tag = view.findViewById(R.id.btn_add_tag)
        btn_add_tag?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.rectangular_button_green_count))
        et_matter_tags = view.findViewById(R.id.et_matter_tag)
        val tag_filters = arrayOf<InputFilter>(InputFilter.LengthFilter(30))
        et_matter_tags?.filters = tag_filters
        et_matter_tags?.maxLines = 1
        et_matter_tags?.setHint(R.string.matter_tags)
        m_c_number = view.findViewById(R.id.m_c_number)
        m_c_number?.setText(R.string.case_number)
        m_c_type = view.findViewById(R.id.m_c_type)
        m_c_type?.setText(R.string.case_type)
        ll_end_date = view.findViewById(R.id.ll_end_date)
        ll_start_date = view.findViewById(R.id.ll_start_date)
        ll_date = view.findViewById(R.id.ll_date)
        tv_start_date = view.findViewById(R.id.tv_start_date)
        tv_start_date?.setHint(R.string.start_date)
        tv_start_date?.setTextSize(TypedValue.COMPLEX_UNIT_SP, DynamicUtils.fifteen.toFloat())
        tv_end_date = view.findViewById(R.id.tv_end_date)
        tv_end_date?.setHint(R.string.close_date)
        tv_end_date?.setTextSize(TypedValue.COMPLEX_UNIT_SP, DynamicUtils.fifteen.toFloat())
        cv_client_details = view.findViewById(R.id.cv_client_details)
        et_matter_description = view.findViewById(R.id.et_matter_description)
        et_matter_description?.setHint(R.string.description)
        et_matter_description?.addTextChangedListener(DescriptionValidation(et_matter_description))
        val descFilters = arrayOf<InputFilter>(InputFilter.LengthFilter(300))
        et_matter_description?.filters = descFilters
        et_matter_description?.maxLines = 10
        tv_dof = view.findViewById(R.id.tv_dof)
        tv_dof?.setHint(R.string.date_of_filing)
        tv_dof?.setTextSize(TypedValue.COMPLEX_UNIT_SP, DynamicUtils.fifteen.toFloat())

        tv_create_date = view.findViewById(R.id.tv_create_date)
        tv_create_date?.setHint(R.string.created_date)
        tv_create_date?.setTextSize(TypedValue.COMPLEX_UNIT_SP, DynamicUtils.fifteen.toFloat())
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
        val todayDate = sdf.format(Date())
        tv_create_date?.text = todayDate
        et_court = view.findViewById(R.id.et_court)
        et_court?.setHint(R.string.court)
        et_court?.setTextSize(TypedValue.COMPLEX_UNIT_SP, DynamicUtils.fifteen.toFloat())
        et_court?.addTextChangedListener(Validation(et_court))
        et_judge = view.findViewById(R.id.et_judge)
        et_judge?.setHint(R.string.judge_s)
        et_judge?.setTextSize(TypedValue.COMPLEX_UNIT_SP, DynamicUtils.fifteen.toFloat())
        et_judge?.addTextChangedListener(Validation(et_judge))
        tv_high_priority = view.findViewById(R.id.tv_high_priority)
        tv_high_priority?.setOnClickListener(this)
        tv_medium_priority = view.findViewById(R.id.tv_medium_priority)
        tv_medium_priority?.setOnClickListener(this)
        tv_low_priority = view.findViewById(R.id.tv_low_priority)
        tv_low_priority?.setOnClickListener(this)
        tv_status_pending = view.findViewById(R.id.tv_status_pending)
        tv_status_pending?.setOnClickListener(this)
        tv_status_active = view.findViewById(R.id.tv_status_active)
        tv_status_active?.setOnClickListener(this)
        btn_add_advocate = view.findViewById(R.id.btn_add_advocate)
        btn_add_advocate?.setOnClickListener(this)
        btn_cancel_save = view.findViewById(R.id.btn_cancel_save)
        btn_cancel_save?.setOnClickListener(this)
        ll_added_tags = view.findViewById(R.id.ll_added_tags)
        btn_create = view.findViewById(R.id.btn_create)
        btn_create?.setText(R.string.save_next)
        btn_create?.setOnClickListener(this)
        ll_add_advocate = view.findViewById(R.id.ll_add_advocate)
        ll_add_advocate?.visibility = View.GONE
        matter = parentFragment as? Matter
        tv_start_date?.inputType = InputType.TYPE_NULL
        tv_end_date?.inputType = InputType.TYPE_NULL
        tv_dof?.inputType = InputType.TYPE_NULL
        tv_response = view.findViewById(R.id.response_name)
        tv_reponse_email = view.findViewById(R.id.response_email)
        tv_response_phone = view.findViewById(R.id.response_phone)
        Constants.matterInformation_en = this
        val sdf1 = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val todayDate1 = sdf1.format(Date())

        tv_create_date?.text = todayDate1
        tv_create_date?.setOnClickListener { AndroidUtils.showDatePicker(tv_create_date, true, true, false, null) }
        if (Constants.MATTER_TYPE == "Legal") {
            ll_case_num?.visibility = View.VISIBLE
            tl_case_type?.visibility = View.VISIBLE
            et_matter_type?.visibility = View.GONE
            m_c_number?.setText(R.string.case_number)
            m_c_type?.setText(R.string.case_type)
            ll_court?.visibility = View.VISIBLE
            ll_judge?.visibility = View.VISIBLE
            ll_dof?.visibility = View.VISIBLE
            ll_date?.visibility = View.GONE
            ll_start_date?.visibility = View.GONE
            ll_end_date?.visibility = View.GONE
            ll_header?.visibility = View.VISIBLE
            tv_dof?.setOnClickListener { AndroidUtils.showDatePicker(tv_dof, true, true, true, null) }
        } else {
            ll_case_num?.visibility = View.GONE
            tl_case_type?.visibility = View.GONE
            et_matter_type?.visibility = View.VISIBLE
            m_c_number?.setText(R.string.matter_number)
            m_c_type?.setText(R.string.matter_type)
            ll_court?.visibility = View.GONE
            ll_judge?.visibility = View.GONE
            ll_dof?.visibility = View.GONE
            ll_date?.visibility = View.VISIBLE
            ll_start_date?.visibility = View.VISIBLE
            ll_end_date?.visibility = View.VISIBLE
            ll_header?.visibility = View.GONE
            initDatePickers()
        }
        loadActiveUI()
        loadHighPriorityUI()
        edit_matter_page_txt?.visibility = View.GONE
        ll_matter_title?.visibility = View.VISIBLE
        if (Constants.create_matter) {
            matter_title?.visibility = View.GONE
            iv_remove_matter?.visibility = View.GONE
        } else {
            matter_title?.visibility = View.VISIBLE
            iv_remove_matter?.visibility = View.VISIBLE
        }
        edit_matter_page_icon?.visibility = View.GONE
        loadAllDetails()
        btn_add_more_info?.visibility = View.VISIBLE
        other_information?.visibility = View.GONE
        btn_cancel_save?.setText(R.string.save_later)
        btn_create?.setText(R.string.save_next)

        iv_forward_button?.setOnClickListener {
            val totalPages = pagebuttons.size
            if (currentPage < totalPages - 1) {
                currentPage++
                if (currentPage < advocates_list.size) {
                    val model = advocates_list[currentPage]
                    EditAdvocateUI(model.advocate_name ?: "", model.email ?: "", model.number ?: "", currentPage)
                    loadOpponentsList(false)
                } else {
                    RefreshAdvocateView()
                    loadOpponentsList(true)
                }
                UpdatePageButton(currentPage)
            } else {
                Log.d("page_index_f", "Reached the last visible page")
            }
        }

        iv_backward_button?.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                val advocateModel = advocates_list[currentPage]
                EditAdvocateUI(advocateModel.advocate_name ?: "", advocateModel.email ?: "", advocateModel.number ?: "", currentPage)
                UpdatePageButton(currentPage)
            } else {
                Log.d("page_index_p", "Index ended")
            }
        }

        iv_remove_tag?.setOnClickListener {
            RefreshAdvocateView()
            loadSelectedView()
        }
        btn_cancel_tag?.setOnClickListener {
            if (currentPage < advocates_list.size) {
                advocates_list.removeAt(currentPage)
                if (advocates_list.isEmpty()) {
                    currentPage = 0
                    et_advocate_name?.setText("")
                    et_advocate_email?.setText("")
                    et_advocate_phone?.setText("")
                } else {
                    currentPage = Math.max(0, currentPage - 1)
                    val model = advocates_list[currentPage]
                    et_advocate_name?.setText(model.advocate_name)
                    et_advocate_email?.setText(model.email)
                    et_advocate_phone?.setText(model.number)
                }
                loadOpponentsList(true)
            } else {
                if (advocates_list.isNotEmpty()) {
                    currentPage = advocates_list.size - 1
                    val model = advocates_list[currentPage]
                    et_advocate_name?.setText(model.advocate_name)
                    et_advocate_email?.setText(model.email)
                    et_advocate_phone?.setText(model.number)
                } else {
                    currentPage = 0
                    et_advocate_name?.setText("")
                    et_advocate_email?.setText("")
                    et_advocate_phone?.setText("")
                    ll_opponent_advocate?.visibility = View.GONE
                }
                loadOpponentsList(false)
            }

            tv_response?.visibility = View.GONE
            tv_reponse_email?.visibility = View.GONE
            tv_response_phone?.visibility = View.GONE

            btn_save_tag?.alpha = 1.0f
            btn_save_tag?.isEnabled = true
            AddMatterDetails()
        }

        iv_remove_matter?.setOnClickListener {
            if (isChangesOccured) {
                AndroidUtils.showConfirmation(
                    requireActivity(),
                    requireContext().getString(R.string.leavepage),
                    requireContext().getString(R.string.changes_you_made_may_not_be_saved),
                    requireContext().getString(R.string.leave),
                    object : AndroidUtils.OnConfirmListener {
                        override fun onSave() {
                            if (!Constants.create_matter) {
                                nav_view_matter()
                            } else matter?.loadViewUI()
                        }
                        override fun onCancel() {}
                    }
                )
            } else {
                if (!Constants.create_matter) {
                    nav_view_matter()
                } else matter?.loadViewUI()
            }
        }
        btn_save_tag?.setOnClickListener {
            if (!validateFields()) return@setOnClickListener

            if (currentPage < advocates_list.size) {
                val advocateModel = AdvocateModel()
                advocateModel.advocate_name = et_advocate_name?.text.toString().trim()
                advocateModel.email = et_advocate_email?.text.toString().trim()
                advocateModel.number = et_advocate_phone?.text.toString().trim()
                advocates_list[currentPage] = advocateModel
                loadSelectedView()
            } else {
                val advocateModel = AdvocateModel()
                advocateModel.advocate_name = et_advocate_name?.text.toString().trim()
                advocateModel.email = et_advocate_email?.text.toString().trim()
                advocateModel.number = et_advocate_phone?.text.toString().trim()
                advocates_list.add(advocateModel)
                currentPage = advocates_list.size
                AddMatterDetails()
                loadSelectedView()
            }

            val jsonArray = JSONArray()
            for (model in advocates_list) {
                try {
                    val jsonObject = JSONObject()
                    jsonObject.put("name", model.advocate_name)
                    jsonObject.put("email", model.email)
                    jsonObject.put("phone", model.number)
                    jsonArray.put(jsonObject)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            matterModel.opponent_advocate = jsonArray
            loadMatterDetails()
            AddMatterDetails()
        }
        btn_add_more_info?.setOnClickListener {
            if (isMoreDetailsChecked) {
                callCaseType()
                other_information?.visibility = View.VISIBLE
            } else other_information?.visibility = View.GONE
            isMoreDetailsChecked = !isMoreDetailsChecked
        }
        et_matter_tags?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                AddMatterDetails()
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                AddMatterDetails()
            }
        })

        btn_add_tag?.setOnClickListener {
            val newTag = et_matter_tags?.text.toString().trim()
            if (newTag.isEmpty()) {
                Toast.makeText(context, "Enter a tag", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (editPosition >= 0 && editPosition < tag_list.size) {
                tag_list[editPosition] = newTag
                editPosition = -1
            } else {
                if (!tag_list.contains(newTag)) {
                    tag_list.add(newTag)
                } else {
                    Toast.makeText(context, "Tag already added", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                btn_add_tag?.setText(R.string.add)
            }

            et_matter_tags?.setText("")
            btn_add_tag?.setText(R.string.add)
            loadMatterTags()
        }
        btn_create?.setOnClickListener {
            isCancelClicked = false
            saveMatterInformation()
        }
        if (!Constants.Matter_id.isNullOrEmpty()) {
            callEditMatterInfo()
        } else {
            if (et_matter_id?.text.toString().isEmpty()) getmatterId()
        }
        return view
    }

    private fun callEditMatterInfo() {
        try {
            progress_dialog = AndroidUtils.get_progress(activity)
            val postdata = JSONObject()
            val matterType = (Constants.MATTER_TYPE ?: "").lowercase(Locale.ROOT)
            val matterId = (Constants.Matter_id ?: "")
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v2/matter/$matterType/$matterId",
                "Edit Matter",
                postdata.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            e.fillInStackTrace()
        }
    }

    private fun loadMatterTags() {
        ll_added_tags?.removeAllViews()
        for (i in tag_list.indices) {
            val view_added_tags = LayoutInflater.from(context).inflate(R.layout.displays_documents_list, null)
            val ll_tags = view_added_tags.findViewById<LinearLayout>(R.id.ll_tags)
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(10, 10, 10, 10)
            ll_tags.layoutParams = params
            val tv_tag_document_name = view_added_tags.findViewById<TextView>(R.id.tv_document_name)
            val iv_edit_tag = view_added_tags.findViewById<ImageView>(R.id.iv_edit_meta)
            val iv_remove_tag = view_added_tags.findViewById<ImageView>(R.id.iv_cancel)
            iv_edit_tag.visibility = View.VISIBLE
            val tag_name = tag_list[i]
            tv_tag_document_name.text = tag_name

            val finalI = i
            iv_remove_tag.setOnClickListener {
                tag_list.removeAt(finalI)

                if (tag_list.isEmpty()) {
                    editPosition = -1
                    btn_add_tag?.setText(R.string.add)
                }

                loadMatterTags()
            }

            iv_edit_tag.setOnClickListener {
                btn_add_tag?.setText(R.string.update)
                et_matter_tags?.setText(tag_list[finalI])
                et_matter_tags?.setSelection(et_matter_tags?.text?.length ?: 0)
                editPosition = finalI
            }

            ll_added_tags?.addView(view_added_tags)
        }
    }

    private fun getAdvocateDisplayText(model: AdvocateModel): String {
        val name = model.advocate_name
        if (!name.isNullOrEmpty() && name.trim().isNotEmpty()) {
            return name
        }
        val email = model.email
        if (!email.isNullOrEmpty() && email.trim().isNotEmpty()) {
            return email
        }
        val num = model.number
        if (!num.isNullOrEmpty() && num.trim().isNotEmpty()) {
            return num
        }
        return ""
    }

    private fun loadSelectedView() {
        ll_opponent_advocate?.visibility = View.GONE
        ll_selected_advocates?.visibility = View.VISIBLE
        ll_selected_advocates?.removeAllViews()
        for (i in advocates_list.indices) {
            val view_opponents = LayoutInflater.from(context).inflate(R.layout.edit_opponent_advocate, null)
            if (view_opponents != null) {
                val tv_opponent_name = view_opponents.findViewById<TextView>(R.id.tv_opponent_name)
                val iv_remove_opponent = view_opponents.findViewById<ImageView>(R.id.iv_remove_opponent)
                val iv_edit_opponent = view_opponents.findViewById<ImageView>(R.id.iv_edit_opponent)
                iv_edit_opponent.setImageDrawable(requireContext().getDrawable(R.drawable.edit_new_icon_))

                if (tv_opponent_name != null && iv_remove_opponent != null) {
                    val model = advocates_list[i]
                    tv_opponent_name.text = getAdvocateDisplayText(model)

                    iv_edit_opponent.tag = i
                    iv_edit_opponent.setOnClickListener { v ->
                        val position = v.tag as Int
                        if (position < advocates_list.size) {
                            val advocateModel = advocates_list[position]
                            currentPage = position
                            EditAdvocateUI(advocateModel.advocate_name ?: "", advocateModel.email ?: "", advocateModel.number ?: "", position)
                        }
                    }

                    iv_remove_opponent.tag = i
                    iv_remove_opponent.setOnClickListener { v ->
                        try {
                            val position = v.tag as Int
                            ll_selected_advocates?.removeViewAt(position)
                            advocates_list.removeAt(position)
                            val selectedAdv = ll_selected_advocates
                            if (selectedAdv != null) {
                                for (j in 0 until selectedAdv.childCount) {
                                    val iv_remove = selectedAdv.getChildAt(j).findViewById<ImageView>(R.id.iv_remove_opponent)
                                    iv_remove?.tag = j
                                }
                            }
                        } catch (e: Exception) {
                            e.fillInStackTrace()
                            AndroidUtils.showAlert(e.message, activity)
                        }
                    }
                    iv_remove_opponent.visibility = View.VISIBLE
                }
            }
            ll_selected_advocates?.addView(view_opponents)
        }
    }

    private fun checkFilledDetails(): Boolean {
        val fields = arrayOf(
            et_matter_title, et_matter_num, et_matter_description, et_court, et_judge, et_matter_tags
        )

        for (field in fields) {
            if (field != null && field.text != null && field.text.toString().trim().isNotEmpty()) {
                return true
            }
        }
        return false
    }

    private fun loadAllDetails() {
        val mat = matter
        if (mat != null) {
            matterArraylist = mat.matter_arraylist
            val list = matterArraylist
            if (list != null && list.isNotEmpty()) {
                for (i in list.indices) {
                    matterModel = list[i]

                    if (matterModel.group_acls != null) {
                        exisiting_group_acls = matterModel.group_acls
                    }
                    if (matterModel.clients != null) {
                        existing_clients = matterModel.clients
                    }
                    if (matterModel.members != null) {
                        existing_members = matterModel.members
                    }
                    if (matterModel.corp_clients_list != null) {
                        existing_corp_clients = matterModel.corp_clients_list
                    }
                    if (matterModel.groups_list != null) {
                        existing_groups_list = matterModel.groups_list
                    }
                    if (matterModel.temp_clients_list != null) {
                        existing_temp_list = matterModel.temp_clients_list
                    }
                    if (matterModel.clients_list != null) {
                        existing_clients_list = matterModel.clients_list
                    }
                    if (matterModel.members_list != null) {
                        existing_tm_list = matterModel.members_list
                    }
                    if (matterModel.documents != null) {
                        existing_documents = matterModel.documents
                    }
                    if (matterModel.documents_list != null) {
                        existing_documents_list = matterModel.documents_list
                    }
                    if (matterModel.tags_list != null) {
                        existing_tags_list = matterModel.tags_list ?: JSONArray()
                    }
                    tag_list.clear()
                    if (existing_tags_list.length() > 0) {
                        for (j in 0 until existing_tags_list.length()) {
                            try {
                                tag_list.add(existing_tags_list.getString(j))
                            } catch (e: JSONException) {
                                throw RuntimeException(e)
                            }
                        }
                    }
                    if (!Constants.GeneratedMatterId.isNullOrEmpty()) {
                        et_matter_id?.setText(Constants.GeneratedMatterId)
                    }

                    loadMatterTags()

                    if (matterModel.matter_title != null) {
                        et_matter_title?.setText(matterModel.matter_title)
                    } else {
                        et_matter_title?.setText("")
                    }
                    if (matterModel.case_number != null) {
                        et_matter_num?.setText(matterModel.case_number)
                    } else {
                        et_matter_num?.setText("")
                    }
                    if (matterModel.case_type != null) {
                        et_case_type?.text = matterModel.case_type
                        et_matter_type?.setText(matterModel.case_type)
                    } else {
                        et_matter_type?.setText("")
                        et_case_type?.text = ""
                    }
                    if (matterModel.description != null) {
                        et_matter_description?.setText(matterModel.description)
                    } else {
                        et_matter_description?.setText("")
                    }
                    if (matterModel.date_of_filing != null) {
                        tv_dof?.text = matterModel.date_of_filing
                    } else {
                        tv_dof?.setOnClickListener { AndroidUtils.showDatePicker(tv_dof, true, true, true, null) }
                    }
                    if (matterModel.created_date != null) {
                        tv_create_date?.text = matterModel.created_date
                    } else {
                        tv_create_date?.setOnClickListener { AndroidUtils.showDatePicker(tv_create_date, true, true, false, null) }
                    }
                    if (matterModel.start_date != null) {
                        tv_start_date?.text = matterModel.start_date
                    } else {
                        tv_start_date?.setOnClickListener { AndroidUtils.showDatePicker(tv_start_date, true) }
                        if (tv_start_date != null && tv_end_date != null) {
                            AndroidUtils.validateStartAndEndDates(requireContext(), tv_start_date!!, tv_end_date!!, "MMM dd, yyyy")
                        }
                    }
                    if (matterModel.end_date != null) {
                        tv_end_date?.text = matterModel.end_date
                    } else {
                        tv_end_date?.setOnClickListener { AndroidUtils.showDatePicker(tv_end_date, false) }
                        if (tv_start_date != null && tv_end_date != null) {
                            AndroidUtils.validateStartAndEndDates(requireContext(), tv_start_date!!, tv_end_date!!, "MMM dd, yyyy")
                        }
                    }
                    if (matterModel.court != null) {
                        et_court?.setText(matterModel.court)
                    } else {
                        et_court?.setText("")
                    }
                    if (matterModel.judge != null) {
                        et_judge?.setText(matterModel.judge)
                    } else {
                        et_judge?.setText("")
                    }
                    if (matterModel.case_priority != null) {
                        CASE_PRIORITY = matterModel.case_priority!!
                    }
                    if (matterModel.status != null) {
                        STATUS = matterModel.status!!
                    }

                    if (Constants.create_matter) {
                        if (list[i].opponent_advocate != null) {
                            advocates_list.clear()
                            existing_opponents = list[i].opponent_advocate
                            val opps = existing_opponents
                            if (opps != null && opps.length() > 0) {
                                for (j in 0 until opps.length()) {
                                    try {
                                        val jsonObject = opps.getJSONObject(j)
                                        val advocateModel = AdvocateModel()
                                        advocateModel.advocate_name = jsonObject.getString("name")
                                        advocateModel.number = jsonObject.getString("phone")
                                        advocateModel.email = jsonObject.getString("email")
                                        advocates_list.add(advocateModel)
                                    } catch (e: JSONException) {
                                        e.fillInStackTrace()
                                    }
                                }
                                loadSelectedView()
                            }
                        }
                    }
                }
                when (CASE_PRIORITY) {
                    "High" -> loadHighPriorityUI()
                    "Medium" -> loadMediumPriorityUI()
                    else -> loadLowPriorityUI()
                }
                if (STATUS == "Active") {
                    loadActiveUI()
                } else {
                    loadPendingUI()
                }
            }
            isInitialLoad = false
        }
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (isInitialLoad && !Constants.create_matter) return
                AddMatterDetails()
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        }

        et_matter_title?.addTextChangedListener(watcher)
        et_matter_num?.addTextChangedListener(watcher)
        et_matter_type?.addTextChangedListener(watcher)
        et_matter_tags?.addTextChangedListener(watcher)
        et_matter_description?.addTextChangedListener(watcher)
        tv_dof?.addTextChangedListener(watcher)
        tv_create_date?.addTextChangedListener(watcher)
        tv_start_date?.addTextChangedListener(watcher)
        tv_end_date?.addTextChangedListener(watcher)
        et_court?.addTextChangedListener(watcher)
        et_judge?.addTextChangedListener(watcher)
    }

    private fun load_existing_advocates() {
        advocates_list.clear()

        val vm = viewMatterModel1 ?: return
        val advocates = vm.opponentAdvocates
        if (advocates != null) {
            for (i in 0 until advocates.length()) {
                var client_value: JSONObject? = null
                try {
                    client_value = advocates.getJSONObject(i)
                    ad_name = client_value.getString("name")
                    ad_email = client_value.getString("email")
                    ad_phone = client_value.getString("phone")
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
                val advocateModel = AdvocateModel()
                advocateModel.advocate_name = ad_name
                advocateModel.email = ad_email
                advocateModel.number = ad_phone
                advocates_list.add(advocateModel)
                loadMatterDetails()
            }
            if (advocates_list.isNotEmpty()) {
                loadSelectedView()
            } else {
                ll_opponent_advocate?.visibility = View.GONE
            }
        }
    }

    private fun UpdateMatterInfo() {
        progress_dialog = AndroidUtils.get_progress(activity)
        try {
            val postdata = JSONObject()
            for (i in advocates_list.indices) {
                val jsonObject = JSONObject()
                val advocateModel = advocates_list[i]
                jsonObject.put("name", advocateModel.advocate_name)
                jsonObject.put("email", advocateModel.email)
                jsonObject.put("phone", advocateModel.number)
                advocates.put(jsonObject)
            }
            postdata.put("affidavit_filing_date", "")
            postdata.put("affidavit_isfiled", "")
            if (Constants.MATTER_TYPE == "Legal") {
                val dofText = tv_dof?.text.toString().trim()
                if (dofText.isEmpty()) {
                    postdata.put("case_number", et_matter_num?.text.toString())
                    postdata.put("judges", et_judge?.text.toString())
                    postdata.put("case_type", et_case_type?.text.toString())
                    postdata.put("opponent_advocates", advocates)
                    postdata.put("court_name", et_court?.text.toString())
                    postdata.put("date_of_filling", "")
                } else {
                    postdata.put("case_number", et_matter_num?.text.toString())
                    postdata.put("judges", et_judge?.text.toString())
                    postdata.put("case_type", et_case_type?.text.toString())
                    postdata.put("opponent_advocates", advocates)
                    postdata.put("court_name", et_court?.text.toString())
                    val date_of_filling = AndroidUtils.convertAnyDateToDDMMYYYY(dofText)
                    postdata.put("date_of_filling", date_of_filling)
                }
            } else {
                val closeDate = tv_end_date?.text.toString()
                val startDate = tv_start_date?.text.toString()

                if (closeDate.trim().isEmpty()) {
                    postdata.put("closedate", "")
                } else {
                    val date_of_filling = AndroidUtils.convertAnyDateToDDMMYYYY(closeDate)
                    postdata.put("closedate", date_of_filling)
                }
                if (startDate.trim().isEmpty()) {
                    postdata.put("startdate", "")
                } else {
                    val date_of_filling = AndroidUtils.convertAnyDateToDDMMYYYY(startDate)
                    postdata.put("startdate", date_of_filling)
                }
                postdata.put("matter_type", et_matter_type?.text.toString())
                postdata.put("matter_number", et_matter_num?.text.toString())
            }
            postdata.put("matter_id", et_matter_id?.text.toString())
            val date_of_filling = AndroidUtils.convertAnyDateToDDMMYYYY(tv_create_date?.text.toString().trim())
            postdata.put("created_date", date_of_filling)
            val tagsObject = JSONObject()

            for (i in tag_list.indices) {
                try {
                    tagsObject.put(i.toString(), tag_list[i])
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            postdata.put("description", et_matter_description?.text.toString())
            postdata.put("priority", CASE_PRIORITY)
            postdata.put("status", STATUS)
            postdata.put("title", et_matter_title?.text.toString())
            postdata.put("tags", tagsObject)
            val matterType = (Constants.MATTER_TYPE ?: "").lowercase(Locale.ROOT)
            val matterId = (Constants.Matter_id ?: "")
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.PATCH,
                "v2/matter/$matterType/$matterId",
                "Update Matter",
                postdata.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                progress_dialog!!.dismiss()
                AndroidUtils.showAlert(e.message, activity)
            }
            e.fillInStackTrace()
        }
    }

    private fun CreateMatterInfo() {
        progress_dialog = AndroidUtils.get_progress(activity)
        try {
            val postdata = JSONObject()
            for (i in advocates_list.indices) {
                val jsonObject = JSONObject()
                val advocateModel = advocates_list[i]
                jsonObject.put("name", advocateModel.advocate_name)
                jsonObject.put("email", advocateModel.email)
                jsonObject.put("phone", advocateModel.number)
                advocates.put(jsonObject)
            }
            postdata.put("affidavit_filing_date", "")
            postdata.put("affidavit_isfiled", "")
            if (Constants.MATTER_TYPE == "Legal") {
                val dofText = tv_dof?.text.toString().trim()
                if (dofText.isEmpty()) {
                    postdata.put("case_number", et_matter_num?.text.toString())
                    postdata.put("judges", et_judge?.text.toString())
                    postdata.put("case_type", et_case_type?.text.toString())
                    postdata.put("opponent_advocates", advocates)
                    postdata.put("court_name", et_court?.text.toString())
                    postdata.put("date_of_filling", "")
                } else {
                    postdata.put("case_number", et_matter_num?.text.toString())
                    postdata.put("judges", et_judge?.text.toString())
                    postdata.put("case_type", et_case_type?.text.toString())
                    postdata.put("opponent_advocates", advocates)
                    postdata.put("court_name", et_court?.text.toString())
                    val date_of_filling = AndroidUtils.convertAnyDateToDDMMYYYY(dofText)
                    postdata.put("date_of_filling", date_of_filling)
                }
            } else {
                val closeDate = tv_end_date?.text.toString()
                val startDate = tv_start_date?.text.toString()

                if (closeDate.trim().isEmpty()) {
                    postdata.put("closedate", "")
                } else {
                    val date_of_filling = AndroidUtils.convertAnyDateToDDMMYYYY(closeDate)
                    postdata.put("closedate", date_of_filling)
                }
                if (startDate.trim().isEmpty()) {
                    postdata.put("startdate", "")
                } else {
                    val date_of_filling = AndroidUtils.convertAnyDateToDDMMYYYY(startDate)
                    postdata.put("startdate", date_of_filling)
                }
                postdata.put("matter_type", et_matter_type?.text.toString())
                postdata.put("matter_number", et_matter_num?.text.toString())
            }
            postdata.put("matter_id", et_matter_id?.text.toString())
            val date_of_filling = AndroidUtils.convertAnyDateToDDMMYYYY(tv_create_date?.text.toString().trim())
            postdata.put("created_date", date_of_filling)
            val tagsObject = JSONObject()

            for (i in tag_list.indices) {
                try {
                    tagsObject.put(i.toString(), tag_list[i])
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            postdata.put("description", et_matter_description?.text.toString())
            postdata.put("priority", CASE_PRIORITY)
            postdata.put("status", STATUS)
            postdata.put("title", et_matter_title?.text.toString())
            postdata.put("tags", tagsObject)
            val matterType = (Constants.MATTER_TYPE ?: "").lowercase(Locale.ROOT)
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.POST,
                "v2/matter/$matterType",
                "Create Matter",
                postdata.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                progress_dialog!!.dismiss()
                AndroidUtils.showAlert(e.message, activity)
            }
            e.fillInStackTrace()
        }
    }

    private fun callCaseType() {
        progress_dialog = AndroidUtils.get_progress(activity)
        try {
            val postdata = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v2/matter/casetypes".lowercase(Locale.ROOT),
                "Case Type",
                postdata.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                progress_dialog!!.dismiss()
                AndroidUtils.showAlert(e.message, activity)
            }
            e.fillInStackTrace()
        }
    }

    private fun getmatterId() {
        progress_dialog = AndroidUtils.get_progress(activity)
        try {
            val postdata = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v2/generate/matterid".lowercase(Locale.ROOT),
                "Matter Id",
                postdata.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                progress_dialog!!.dismiss()
                AndroidUtils.showAlert(e.message, activity)
            }
            e.fillInStackTrace()
        }
    }

    fun CheckUnique() {
        progress_dialog = AndroidUtils.get_progress(activity)
        try {
            val postdata = JSONObject()
            if (!Constants.Matter_id.isNullOrEmpty()) {
                postdata.put("matter_id", Constants.Matter_id)
            }
            postdata.put("title", et_matter_title?.text.toString())
            postdata.put("type", (Constants.MATTER_TYPE ?: "").lowercase(Locale.ROOT))
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.POST,
                "matter/check/unique",
                "Unique",
                postdata.toString()
            )
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun initDatePickers() {
        tv_start_date?.setOnClickListener { AndroidUtils.showDatePicker(tv_start_date, true) }
        tv_end_date?.setOnClickListener { AndroidUtils.showDatePicker(tv_end_date, false) }
        if (tv_start_date != null && tv_end_date != null) {
            AndroidUtils.validateStartAndEndDates(requireContext(), tv_start_date!!, tv_end_date!!, "MMM dd, yyyy")
        }
    }

    private fun updateDateInViews(year: Int, month: Int, dayOfMonth: Int) {
        val df = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val selectedCalendar = Calendar.getInstance()
        selectedCalendar.set(year, month - 1, dayOfMonth)
        val formattedDate = df.format(selectedCalendar.time)
        tv_end_date?.text = formattedDate
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_high_priority -> {
                PriorityStatus()
                loadHighPriorityUI()
            }
            R.id.tv_medium_priority -> {
                PriorityStatus()
                loadMediumPriorityUI()
            }
            R.id.tv_low_priority -> {
                PriorityStatus()
                loadLowPriorityUI()
            }
            R.id.tv_status_pending -> {
                Status()
                loadPendingUI()
            }
            R.id.tv_status_active -> {
                Status()
                loadActiveUI()
            }
            R.id.btn_add_advocate -> {
                RefreshAdvocateView()
                currentPage = advocates_list.size
                loadAdvocateUI()
            }
            R.id.btn_create -> {
                isCancelClicked = false
                saveMatterInformation()
            }
            R.id.btn_cancel_save -> {
                isCancelClicked = true
                saveMatterInformation()
            }
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progress_dialog != null && progress_dialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }
        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent)
                if (httpResult.requestType == "Update Matter") {
                    val message = result.getString("msg")
                    if (!result.getBoolean("error")) {
                        loadMatterDetails()
                        AndroidUtils.showToast(message, activity)
                        val mat = matter
                        if (mat != null) {
                            if (isCancelClicked) {
                                mat.loadViewUI()
                            } else {
                                mat.loadGCT()
                            }
                        }
                        AddMatterDetails()
                        Constants.GeneratedMatterId = et_matter_id?.text.toString()
                        Constants.GeneratedMatterTitle = et_matter_title?.text.toString()
                    } else {
                        AndroidUtils.showAlert(message, activity, "")
                    }
                } else if (httpResult.requestType == "Create Matter") {
                    if (!result.getBoolean("error")) {
                        Constants.Matter_id = result.optString("matter_id")
                        val message = result.getString("msg")
                        loadMatterDetails()
                        AndroidUtils.showToast(message, activity)
                        val mat = matter
                        if (mat != null) {
                            if (isCancelClicked) {
                                mat.loadViewUI()
                            } else {
                                mat.loadGCT()
                            }
                        }
                        AddMatterDetails()
                        Constants.GeneratedMatterId = et_matter_id?.text.toString()
                        Constants.GeneratedMatterTitle = et_matter_title?.text.toString()
                    } else {
                        val message = result.getString("msg")
                        AndroidUtils.showAlert(message, activity, "")
                    }
                } else if (httpResult.requestType == "Edit Matter") {
                    if (result.getBoolean("error")) {
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
                } else if (httpResult.requestType == "Unique") {
                    if (!result.getBoolean("error")) {
                        if (matter != null) {
                            if (Constants.Matter_id.isNullOrEmpty()) {
                                CreateMatterInfo()
                            } else {
                                UpdateMatterInfo()
                            }
                        } else if (!Constants.create_matter) {
                            UpdateMatterInfo()
                        }
                    } else {
                        val message = result.getString("msg")
                        AndroidUtils.showAlert(message, activity, "")
                    }
                } else if (httpResult.requestType == "Case Type") {
                    if (!result.getBoolean("error")) {
                        val jsonArray = result.getJSONArray("data")
                        caseTypeList.clear()
                        for (i in 0 until jsonArray.length()) {
                            caseTypeList.add(jsonArray.getString(i))
                        }
                        if (caseTypeList.isNotEmpty()) {
                            loadCaseTypes()
                        }
                    } else {
                        val message = result.getString("msg")
                        AndroidUtils.showAlert(message, activity, "")
                    }
                } else if (httpResult.requestType == "Matter Id") {
                    if (!result.getBoolean("error")) {
                        val matter_id = result.optString("matter_id")
                        et_matter_id?.setText(matter_id)
                    } else {
                        val message = result.optString("msg")
                        AndroidUtils.showAlert(message, activity, "")
                    }
                }
            } catch (e: Exception) {
                if (progress_dialog != null && progress_dialog!!.isShowing) {
                    AndroidUtils.dismiss_dialog(progress_dialog)
                }
                e.fillInStackTrace()
            }
        } else {
            try {
                val result = JSONObject(httpResult.responseContent)
                val message = "Request Failed, Try Again"
                if (result.has("error")) {
                    AndroidUtils.showAlert(message, activity)
                } else {
                    AndroidUtils.showAlert(message, activity)
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

    private fun loadEditMatterDetails() {
        isInitialLoad = true
        matter_date?.text = viewMatterModel1?.created ?: ""
        if (Constants.create_matter) {
            matter_title?.visibility = View.GONE
            iv_remove_matter?.visibility = View.GONE
        } else {
            matter_title?.visibility = View.VISIBLE
            iv_remove_matter?.visibility = View.VISIBLE
        }
        val vm = viewMatterModel1 ?: return
        et_matter_title?.setText(vm.title)
        matter_title?.text = et_matter_title?.text.toString()
        if (Constants.MATTER_TYPE == "Legal") {
            et_matter_num?.setText(vm.caseNumber)
        } else {
            et_matter_num?.setText(vm.matterNumber)
        }
        if (Constants.MATTER_TYPE == "Legal") {
            et_case_type?.text = vm.casetype
        } else {
            et_matter_type?.setText(vm.matterType)
        }
        et_matter_description?.setText(vm.description)
        et_court?.setText(vm.courtName)
        et_judge?.setText(vm.judges)
        if (vm.created_date.isNullOrEmpty() || vm.created_date.trim().isEmpty()) {
            tv_create_date?.text = ""
        } else {
            try {
                tv_create_date?.text = vm.created_date
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        }
        Constants.GeneratedMatterId = vm.matter_id ?: ""
        Constants.GeneratedMatterTitle = vm.title ?: ""
        if (!Constants.GeneratedMatterId.isNullOrEmpty()) {
            et_matter_id?.setText(Constants.GeneratedMatterId)
        }
        if (Constants.MATTER_TYPE == "Legal") {
            val dofStr = vm.date_of_filling ?: ""
            if (dofStr.trim().isEmpty()) {
                tv_dof?.text = ""
            } else {
                try {
                    tv_dof?.text = dofStr
                } catch (e: Exception) {
                    e.fillInStackTrace()
                }
            }
        } else {
            val inputDate = vm.startdate ?: ""
            val inputDate2 = vm.closedate ?: ""
            if (inputDate.trim().isEmpty()) {
                tv_start_date?.text = ""
            } else {
                tv_start_date?.text = inputDate
            }
            if (inputDate2.trim().isEmpty()) {
                tv_end_date?.text = ""
            } else {
                tv_end_date?.text = inputDate2
            }
        }

        try {
            groups = vm.groups ?: JSONArray()
            group_acls = vm.groupAcls ?: JSONArray()
            val vmClients = vm.clients
            if (vmClients != null) {
                for (i in 0 until vmClients.length()) {
                    val client_list = JSONObject()
                    val jsonObject = vmClients.getJSONObject(i)
                    client_list.put("id", jsonObject.getString("id"))
                    client_list.put("type", jsonObject.getString("type"))
                    clients.put(client_list)
                }
            }
            val corpId = vm.corpId ?: ""
            if (corpId.isNotEmpty()) {
                val client_list = JSONObject()
                client_list.put("id", corpId)
                client_list.put("type", "corporate")
                clients.put(client_list)
            }
            val vmDocs = vm.documents
            if (vmDocs != null) {
                for (i in 0 until vmDocs.length()) {
                    val document_list = JSONObject()
                    val jsonObject = vmDocs.getJSONObject(i)
                    document_list.put("docid", jsonObject.getString("docid"))
                    document_list.put("doctype", jsonObject.getString("doctype"))
                    document_list.put("user_id", jsonObject.getString("user_id"))
                    documents.put(document_list)
                }
            }
            val vmMembers = vm.members
            if (vmMembers != null) {
                for (i in 0 until vmMembers.length()) {
                    val member_list = JSONObject()
                    val jsonObject = vmMembers.getJSONObject(i)
                    member_list.put("id", jsonObject.getString("id"))
                    members.put(member_list)
                }
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
        if (vm.priority != null) {
            CASE_PRIORITY = vm.priority ?: ""
            oldPriority = CASE_PRIORITY
        }
        if (vm.status != null) {
            STATUS = vm.status ?: ""
            oldStatus = STATUS
        }
        when (CASE_PRIORITY) {
            "High" -> loadHighPriorityUI()
            "Medium" -> loadMediumPriorityUI()
            else -> loadLowPriorityUI()
        }
        if (STATUS == "Active") {
            loadActiveUI()
        } else {
            loadPendingUI()
        }
        if (Constants.MATTER_TYPE == "Legal") {
            ll_date?.visibility = View.GONE
            ll_start_date?.visibility = View.GONE
            ll_end_date?.visibility = View.GONE
            ll_dof?.visibility = View.VISIBLE
            ll_header?.visibility = View.VISIBLE
            btn_create?.setText(R.string.save_next)
            load_existing_advocates()
            tv_dof?.setOnClickListener { AndroidUtils.showDatePicker(tv_dof, true, true, true, null) }
        } else {
            ll_date?.visibility = View.VISIBLE
            ll_start_date?.visibility = View.VISIBLE
            ll_end_date?.visibility = View.VISIBLE
            ll_dof?.visibility = View.GONE
            ll_header?.visibility = View.GONE
            btn_create?.setText(R.string.save_next)
            initDatePickers()
        }

        et_matter_title?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                matter_title?.text = s.toString()
            }
        })
        if (vm.tags_list != null) {
            existing_tags_list = vm.tags_list ?: JSONArray()
        }
        tag_list.clear()
        if (existing_tags_list.length() > 0) {
            for (j in 0 until existing_tags_list.length()) {
                try {
                    tag_list.add(existing_tags_list.getString(j))
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
            }
        }
        loadMatterTags()
        callCaseType()
        btn_add_more_info?.visibility = View.VISIBLE
        other_information?.visibility = View.GONE
        isInitialLoad = false
        isChangesOccured = false
    }

    private fun loadeditmatter(jsonObject: JSONObject) {
        viewMatterModel1 = ViewMatterModel()
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
            loadEditMatterDetails()
        } catch (e: JSONException) {
            AndroidUtils.showAlert(e.message, activity)
            e.fillInStackTrace()
        }
    }

    private fun loadCaseTypes() {
        val adapter = CommonSpinnerAdapter(requireActivity(), caseTypeList)
        sp_case_type?.adapter = adapter
        AndroidUtils.LoadList(sp_case_type, context, caseTypeList.size, true)
        sp_case_type?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            et_case_type?.text = caseTypeList[position]
            isCaseTypeChecked = true
            sp_case_type?.visibility = View.GONE
            AddMatterDetails()
        }
    }

    private fun nav_view_matter() {
        Constants.is_CreateMatter = false
        Constants.isCreate = false
        val fragment: Fragment = Matter()
        val fragmentManager = requireActivity().supportFragmentManager
        val ft = fragmentManager.beginTransaction()
        ft.replace(R.id.id_framelayout, fragment)
        ft.commit()
    }

    fun saveMatterInformation() {
        val msg: String
        if (!Constants.create_matter) {
            msg = "Please enter the title"
            if (et_matter_title?.text.toString().trim().isEmpty()) {
                AndroidUtils.showAlert(msg, activity)
                et_matter_title?.requestFocus()
            } else if (et_matter_description?.text.toString().length > 300) {
                AndroidUtils.showAlert("Please check the description field size..", activity)
            } else {
                CheckUnique()
            }
        } else {
            msg = "Please Check the Title"
            if (et_matter_title?.text.toString().trim().isEmpty()) {
                AndroidUtils.showAlert(msg, activity)
                et_matter_title?.requestFocus()
            } else if (et_matter_description?.text.toString().length > 300) {
                AndroidUtils.showAlert("Please check the description field size..", activity)
            } else {
                CheckUnique()
            }
        }
    }

    private fun loadAdvocateUI() {
        try {
            loadOpponentsList(true)
            ll_opponent_advocate?.visibility = View.VISIBLE
            RefreshAdvocateView()

            et_advocate_name?.setHint(R.string.name)
            et_advocate_name?.setTextSize(TypedValue.COMPLEX_UNIT_SP, DynamicUtils.fifteen.toFloat())

            et_advocate_email?.setHint(R.string.email)
            et_advocate_email?.setTextSize(TypedValue.COMPLEX_UNIT_SP, DynamicUtils.fifteen.toFloat())

            et_advocate_phone?.inputType = InputType.TYPE_CLASS_PHONE
            et_advocate_phone?.setHint(R.string.phone_number)
            et_advocate_phone?.setTextSize(TypedValue.COMPLEX_UNIT_SP, DynamicUtils.fifteen.toFloat())
            et_advocate_phone?.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(10))

            val etName = et_advocate_name
            if (etName != null) addValidationWatcher(etName, tv_response!!, "Please enter the name.", null, null)
            val etEmail = et_advocate_email
            if (etEmail != null) addValidationWatcher(etEmail, tv_reponse_email!!, "Please enter the email address", Patterns.EMAIL_ADDRESS, "Please enter a valid email address")
            val etPhone = et_advocate_phone
            if (etPhone != null) addValidationWatcher(etPhone, tv_response_phone!!, "Please enter the phone number", Pattern.compile("^\\d{10}$"), "Please enter a 10 digit valid mobile number")

        } catch (e: Exception) {
            e.printStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    private fun addValidationWatcher(
        editText: EditText,
        errorView: TextView,
        emptyError: String,
        pattern: Pattern?,
        patternError: String?
    ) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val value = s.toString().trim()
                if (value.isEmpty()) {
                    errorView.text = emptyError
                    errorView.visibility = View.VISIBLE
                } else if (pattern != null && !pattern.matcher(value).matches()) {
                    errorView.text = patternError
                    errorView.visibility = View.VISIBLE
                } else {
                    errorView.visibility = View.GONE
                }
            }
        })
    }

    private fun validateFields(): Boolean {
        var valid = true

        val name = et_advocate_name?.text.toString().trim()
        val email = et_advocate_email?.text.toString().trim()
        val phone = et_advocate_phone?.text.toString().trim()

        if (name.isEmpty()) {
            tv_response?.text = "Please enter the name."
            tv_response?.visibility = View.VISIBLE
            valid = false
        }

        if (email.isEmpty() || !AndroidUtils.isValidEmail(email)) {
            tv_reponse_email?.text = "Please enter a valid email address."
            tv_reponse_email?.visibility = View.VISIBLE
            valid = false
        }

        if (!phone.matches(Regex("^\\d{10}$"))) {
            tv_response_phone?.text = "Please enter a 10 digit valid mobile number."
            tv_response_phone?.visibility = View.VISIBLE
            valid = false
        }

        return valid
    }

    private fun loadEditedData(adv_name: String, adv_email: String, adv_phone: String, position: Int) {
        val advocateModel = AdvocateModel()
        advocateModel.advocate_name = adv_name
        advocateModel.email = adv_email
        advocateModel.number = adv_phone
        advocates_list[position] = advocateModel
        matterModel.opponent_advocate = JSONArray()
        val jsonArray = JSONArray()
        try {
            for (i in advocates_list.indices) {
                val jsonObject = JSONObject()
                jsonObject.put("name", advocateModel.advocate_name)
                jsonObject.put("email", advocateModel.email)
                jsonObject.put("phone", advocateModel.number)
                jsonArray.put(jsonObject)
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
        matterModel.opponent_advocate = jsonArray
        loadMatterDetails()
    }

    private fun loadOpponentsList(isNewEntry: Boolean) {
        pageNumberLayout?.removeAllViews()
        pagebuttons.clear()

        var totalButtons = advocates_list.size
        if (isNewEntry) {
            totalButtons += 1
        }

        for (i in 0 until totalButtons) {
            val view_opponents = LayoutInflater.from(context).inflate(R.layout.weekly_view_dates, null)
            val pageButton = view_opponents.findViewById<TextView>(R.id.textDay)
            pageButton.text = (i + 1).toString()
            pageButton.setPadding(20, 15, 20, 15)

            if (i == currentPage) {
                pageButton.setTextColor(requireActivity().getColor(R.color.white))
                pageButton.background = requireActivity().getDrawable(R.drawable.rectangular_button_green_count)
            } else {
                pageButton.setTextColor(requireActivity().getColor(R.color.black))
                pageButton.background = requireActivity().getDrawable(R.drawable.background_transparent)
            }

            val finalI = i
            pageButton.setOnClickListener {
                currentPage = finalI
                if (currentPage < advocates_list.size) {
                    val advocateModel = advocates_list[currentPage]
                    EditAdvocateUI(advocateModel.advocate_name ?: "", advocateModel.email ?: "", advocateModel.number ?: "", currentPage)
                } else {
                    loadAdvocateUI()
                }
                UpdatePageButton(currentPage)
            }

            pagebuttons.add(pageButton)
            pageNumberLayout?.addView(view_opponents)
        }
    }

    private fun UpdatePageButton(currentPage: Int) {
        for (i in pagebuttons.indices) {
            val pageButton = pagebuttons[i]
            if (i == currentPage) {
                pageButton.setTextColor(requireActivity().getColor(R.color.white))
                pageButton.background = requireActivity().getDrawable(R.drawable.rectangular_button_green_count)
            } else {
                pageButton.setTextColor(requireActivity().getColor(R.color.black))
                pageButton.background = requireActivity().getDrawable(R.drawable.background_transparent)
            }
        }
    }

    private fun EditAdvocateUI(advocate_name: String, email: String, number: String, position: Int) {
        try {
            ll_opponent_advocate?.visibility = View.VISIBLE
            RefreshAdvocateView()
            et_advocate_phone?.inputType = InputType.TYPE_CLASS_PHONE

            et_advocate_name?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    if (s.toString().trim().isEmpty()) {
                        tv_response?.visibility = View.VISIBLE
                        tv_response?.text = "Please enter the name."
                    }
                }
            })
            et_advocate_email?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    if (!et_advocate_email?.text.toString().trim().matches(Regex(Patterns.EMAIL_ADDRESS.pattern()))) {
                        tv_reponse_email?.text = "Please enter a valid email address"
                        et_advocate_email?.visibility = View.VISIBLE
                    }
                }
            })
            et_advocate_phone?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    if (s.toString().trim().isEmpty() || s.length < 10) {
                        tv_response_phone?.text = "Please enter a 10 digit valid mobile number."
                        tv_response_phone?.visibility = View.VISIBLE
                    }
                }
            })

            et_advocate_name?.setText(advocate_name)
            et_advocate_email?.setText(email)
            et_advocate_phone?.setText(number)
        } catch (e: Exception) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    private fun clearAdvocateView() {
        tv_response?.visibility = View.GONE
        tv_reponse_email?.visibility = View.GONE
        tv_response_phone?.visibility = View.GONE
    }

    private fun RefreshAdvocateView() {
        ll_selected_advocates?.visibility = View.GONE
        et_advocate_name?.setText("")
        et_advocate_email?.setText("")
        et_advocate_phone?.setText("")

        tv_response?.visibility = View.GONE
        tv_reponse_email?.visibility = View.GONE
        tv_response_phone?.visibility = View.GONE
    }

    private fun loadActiveUI() {
        STATUS = "Active"
        tv_status_active?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.button_left_green_round_background))
        tv_status_active?.setTextColor(Color.WHITE)
        tv_status_pending?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.button_right_round_background))
        tv_status_pending?.setTextColor(Color.BLACK)
        Status()
    }

    private fun loadPendingUI() {
        STATUS = "Pending"
        tv_status_active?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.button_left_round_background))
        tv_status_active?.setTextColor(Color.BLACK)
        tv_status_pending?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.button_right_green_round_background))
        tv_status_pending?.setTextColor(Color.WHITE)
        Status()
    }

    private fun loadLowPriorityUI() {
        CASE_PRIORITY = "Low"
        tv_high_priority?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.button_left_round_background))
        tv_high_priority?.setTextColor(Color.BLACK)
        tv_medium_priority?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.radiobutton_centre_background))
        tv_medium_priority?.setTextColor(Color.BLACK)
        tv_low_priority?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.button_right_green_round_background))
        tv_low_priority?.setTextColor(Color.WHITE)
        PriorityStatus()
    }

    private fun loadMediumPriorityUI() {
        CASE_PRIORITY = "Medium"
        tv_high_priority?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.button_left_round_background))
        tv_high_priority?.setTextColor(Color.BLACK)
        tv_medium_priority?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.radiobutton_centre_green_background))
        tv_medium_priority?.setTextColor(Color.WHITE)
        tv_low_priority?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.button_right_round_background))
        tv_low_priority?.setTextColor(Color.BLACK)
        PriorityStatus()
    }

    private fun loadHighPriorityUI() {
        CASE_PRIORITY = "High"
        tv_high_priority?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.button_left_green_round_background))
        tv_high_priority?.setTextColor(Color.WHITE)
        tv_medium_priority?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.radiobutton_centre_background))
        tv_medium_priority?.setTextColor(Color.BLACK)
        tv_low_priority?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.button_right_round_background))
        tv_low_priority?.setTextColor(Color.BLACK)
        PriorityStatus()
    }

    private fun PriorityStatus() {
        AddMatterDetails()
    }

    private fun AddMatterDetails() {
        matterModel.matter_title = et_matter_title?.text.toString()
        matterModel.case_number = et_matter_num?.text.toString()
        matterModel.case_type = if (Constants.MATTER_TYPE == "Legal") et_case_type?.text.toString() else et_matter_type?.text.toString()
        matterModel.description = et_matter_description?.text.toString()
        matterModel.date_of_filing = tv_dof?.text.toString()
        matterModel.created_date = tv_create_date?.text.toString()
        matterModel.start_date = tv_start_date?.text.toString()
        matterModel.end_date = tv_end_date?.text.toString()
        matterModel.court = et_court?.text.toString()
        matterModel.judge = et_judge?.text.toString()
        matterModel.case_priority = CASE_PRIORITY
        matterModel.status = STATUS
        existing_corp_clients?.let { matterModel.corp_clients_list = it }
        existing_temp_list?.let { matterModel.temp_clients_list = it }
        existing_clients?.let { matterModel.clients = it }
        exisiting_group_acls?.let { matterModel.group_acls = it }
        existing_members?.let { matterModel.members = it }
        existing_groups_list?.let { matterModel.groups_list = it }
        existing_clients_list?.let { matterModel.clients_list = it }
        existing_tm_list?.let { matterModel.members_list = it }
        existing_documents?.let { matterModel.documents = it }
        existing_documents_list?.let { matterModel.documents_list = it }

        val jsonArray = JSONArray()
        try {
            for (i in advocates_list.indices) {
                val advocateModel = advocates_list[i]
                val jsonObject = JSONObject()
                jsonObject.put("name", advocateModel.advocate_name)
                jsonObject.put("email", advocateModel.email)
                jsonObject.put("phone", advocateModel.number)
                jsonArray.put(jsonObject)
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
        existing_tags_list = JSONArray()
        for (i in tag_list.indices) {
            existing_tags_list.put(tag_list[i])
        }
        matterModel.tags_list = existing_tags_list
        Constants.currentPage = currentPage
        matterModel.opponent_advocate = jsonArray

        loadMatterDetails()
    }

    private fun loadMatterDetails() {
        val list = matterArraylist
        if (list != null) {
            if (list.isEmpty()) {
                list.add(matterModel)
            } else {
                list[0] = matterModel
            }
        }
    }

    private fun Status() {
        AddMatterDetails()
    }
}
