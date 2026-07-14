package com.digicoffer.lauditor.AuditTrails

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.AuditTrails.Adapters.AuditsAdapter
import com.digicoffer.lauditor.AuditTrails.Adapters.PaginationHelper
import com.digicoffer.lauditor.AuditTrails.Model.AuditsModel
import com.digicoffer.lauditor.AuditTrails.Model.SpinnerItemModal
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.DateUtils.DateUtils
import com.digicoffer.lauditor.CommonFiles.DateUtils.DateUtilsEndDate
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern

class AuditTrails : Fragment(), AsyncTaskCompleteListener, DateUtils.OnDateSelectedListener, DateUtilsEndDate.OnDateSelectedListenerEndDate {
    
    var categoryList = ArrayList<SpinnerItemModal>()
    var categoryAdapter: CommonSpinnerAdapter<SpinnerItemModal>? = null
    private val pagebuttons = ArrayList<TextView>()
    var tv_name: TextView? = null
    var tv_advanced_search: TextView? = null
    var tv_history_act: TextView? = null
    var tv_category: TextView? = null
    var tv_sp_category: TextView? = null
    var ll_category: LinearLayout? = null
    var filterlist = ArrayList<AuditsModel>()
    var isListFiltered = false
    var iscategory_checked = true
    var img_dropdown_icon: ImageView? = null
    var img_clear_icon: ImageView? = null
    var scrollView: HorizontalScrollView? = null
    private var previousPageButton: Button? = null
    var audit_adapter: AuditsAdapter? = null
    var startDate: Date? = null
    var endDate: Date? = null
    var end_temp = 0
    var sorted_list = ArrayList<AuditsModel>()
    var autentication_list = ArrayList<AuditsModel>()
    var groups_list = ArrayList<AuditsModel>()
    var relationship_invite_list = ArrayList<AuditsModel>()
    var tm_list = ArrayList<AuditsModel>()
    var relationships_list = ArrayList<AuditsModel>()
    var share_list = ArrayList<AuditsModel>()
    var documents_list = ArrayList<AuditsModel>()
    var merge_pdf_list = ArrayList<AuditsModel>()
    var legal_matter_list = ArrayList<AuditsModel>()
    var general_matter_list = ArrayList<AuditsModel>()
    private var greenButtonTint: ColorStateList? = null
    private var whiteButtonTint: ColorStateList? = null
    var pageItems = ArrayList<AuditsModel>()
    var sp_category: ListView? = null
    var ll_page_navigation: LinearLayout? = null
    var ll_list: LinearLayoutCompat? = null
    var rv_audits: RecyclerView? = null
    var tv_event_start_time: AppCompatButton? = null
    var tv_event_end_time: AppCompatButton? = null
    private var currentPage = 1
    var auditsList = ArrayList<AuditsModel>()
    var progress_dialog: Dialog? = null
    var CategoryType = ""
    var et_search_audit_list: TextInputEditText? = null
    var datePickersLayout: LinearLayout? = null
    private val itemsPerPage = 10
    var pageNumberLayout: LinearLayout? = null
    var tv_list: TextView? = null
    var isAdvancedSearchEnabled = false
    var iv_forward_button: ImageView? = null
    var iv_backward_button: ImageView? = null
    var ib_start_mandatory: ImageView? = null
    var ib_end_mandatory: ImageView? = null
    var ib_cancel_button: ImageView? = null
    var ib_cancel_button_end_date: ImageView? = null
    private var isDatePickerVisible = false
    var rootView: View? = null
    private var mViewModel: NewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            rootView = inflater.inflate(R.layout.audit_trials, container, false)
            mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
            mViewModel?.setData(requireContext().getString(R.string.audit_trails))
            greenButtonTint = ColorStateList.valueOf(resources.getColor(R.color.blue))
            whiteButtonTint = ColorStateList.valueOf(resources.getColor(R.color.blue_pale))
            datePickersLayout = rootView?.findViewById(R.id.datePickersLayout)
            tv_advanced_search = rootView?.findViewById(R.id.tv_advancedSearch)
            rv_audits = rootView?.findViewById(R.id.rv_audits)
            tv_list = rootView?.findViewById(R.id.tv_list)
            rv_audits?.layoutManager = GridLayoutManager(requireContext(), 1)
            val from_date = rootView?.findViewById<TextView>(R.id.from_date)
            val to_date = rootView?.findViewById<TextView>(R.id.to_date)
            from_date?.setText(R.string.from)
            tv_list?.setTextColor(requireContext().getColor(R.color.grey_medium))
            tv_list?.setText(R.string.data_not_available)
            to_date?.setText(R.string.to)
            to_date?.visibility = View.VISIBLE
            ib_cancel_button = rootView?.findViewById(R.id.cancel_button)
            ib_cancel_button_end_date = rootView?.findViewById(R.id.cancel_button_end_date)
            ib_start_mandatory = rootView?.findViewById(R.id.mandatory)
            ib_end_mandatory = rootView?.findViewById(R.id.mandatory_end_date)
            ib_start_mandatory?.visibility = View.GONE
            ib_end_mandatory?.visibility = View.GONE
            ib_cancel_button?.visibility = View.GONE
            ib_cancel_button_end_date?.visibility = View.GONE
            val tl_search_audit_list = rootView?.findViewById<View>(R.id.tl_search_audit_list)
            et_search_audit_list = tl_search_audit_list?.findViewById(R.id.et_Search)
            et_search_audit_list?.setHint(R.string.search)
            ll_page_navigation = rootView?.findViewById(R.id.ll_page_navigaiton)
            ll_list = rootView?.findViewById(R.id.ll_list)
            val dateUtils = DateUtils(this)
            val dateUtilsEndDate = DateUtilsEndDate(this)
            dateUtilsEndDate.setOnDateSelectedListener(this)
            dateUtils.setOnDateSelectedListener(this)
            tv_event_start_time = rootView?.findViewById(R.id.tv_event_start_time)
            tv_event_start_time?.setHint(R.string.from)
            tv_event_end_time = rootView?.findViewById(R.id.tv_event_end_time)
            tv_event_end_time?.setHint(R.string.to)
            scrollView = rootView?.findViewById(R.id.scrollView)
            iv_forward_button = rootView?.findViewById(R.id.iv_forward_button)
            iv_backward_button = rootView?.findViewById(R.id.iv_backward_button)

            tv_history_act = rootView?.findViewById(R.id.tv_history_act)
            tv_category = rootView?.findViewById(R.id.tv_category)
            tv_history_act?.textSize = DynamicUtils.twenty.toFloat()

            tv_category?.setText(R.string.category)
            tv_history_act?.setText(R.string.history_of_actions)
            tv_advanced_search?.setText(R.string.advanced_search)
            ll_category = rootView?.findViewById(R.id.ll_category)
            tv_sp_category = ll_category?.findViewById(R.id.tv_spinner_view)
            tv_sp_category?.setText(R.string.select_category)
            sp_category = rootView?.findViewById(R.id.sp__category)
            sp_category?.visibility = View.GONE
            img_dropdown_icon = ll_category?.findViewById(R.id.img_dropdown_icon)
            img_clear_icon = ll_category?.findViewById(R.id.img_clear_icon)
            ll_page_navigation?.visibility = View.GONE

            ll_category?.setOnClickListener {
                AndroidUtils.display_listview(iscategory_checked, sp_category)
                iscategory_checked = !iscategory_checked
            }
            img_clear_icon?.setOnClickListener {
                AndroidUtils.DisplaySpinnerView(
                    sp_category,
                    tv_sp_category,
                    CategoryType,
                    img_dropdown_icon,
                    img_clear_icon,
                    false,
                    categoryAdapter,
                    "Search Category"
                )
                CategoryType = ""
                iscategory_checked = true
                isDatePickerVisible = false
                datePickersLayout?.visibility = View.GONE
                et_search_audit_list?.setText("")
                tv_event_start_time?.setText("")
                tv_event_end_time?.setText("")
                loadGeneralList()
            }
            tv_event_start_time?.setOnClickListener {
                AndroidUtils.showDatePicker(tv_event_start_time, true) {
                    val FLAG = "St Time"
                    loadnewPage(tv_event_start_time?.text.toString(), FLAG)
                    fetchpagedata()
                }
            }

            tv_event_end_time?.setOnClickListener {
                AndroidUtils.showDatePicker(tv_event_end_time, false) {
                    val FLAG = "End Time"
                    loadnewPage(tv_event_end_time?.text.toString(), FLAG)
                    fetchpagedata()
                }
            }
            ib_cancel_button?.setOnClickListener {
                tv_event_start_time?.setText("")
                clearDates()
            }
            ib_cancel_button_end_date?.setOnClickListener {
                tv_event_end_time?.setText("")
                clearDates()
            }
            iv_forward_button?.setOnClickListener {
                currentPage += 1
                val startIndex = PaginationHelper.startIndexForCurrentPage(currentPage, 10)
                val endIndex = if (isListFiltered) {
                    PaginationHelper.endIndexForCurrentPage(startIndex, filterlist.size, 10)
                } else if (isAdvancedSearchEnabled) {
                    PaginationHelper.endIndexForCurrentPage(startIndex, sorted_list.size, 10)
                } else if (CategoryType.isEmpty()) {
                    PaginationHelper.endIndexForCurrentPage(startIndex, auditsList.size, 10)
                } else {
                    PaginationHelper.endIndexForCurrentPage(startIndex, sorted_list.size, 10)
                }
                if (endIndex > end_temp) {
                    load_ChosenType_list()
                    UpdatePageButton(currentPage)
                } else {
                    currentPage -= 1
                }
                Log.d("current_page", "" + currentPage)
            }

            iv_backward_button?.setOnClickListener {
                currentPage -= 1
                if (currentPage > 0) {
                    load_ChosenType_list()
                    UpdatePageButton(currentPage)
                } else {
                    currentPage = 1
                }
            }

            pageNumberLayout = rootView?.findViewById(R.id.pageNumberLayout)
            loadSpinnerData()
            tv_advanced_search?.setOnClickListener {
                isDatePickerVisible = !isDatePickerVisible
                datePickersLayout?.visibility = if (isDatePickerVisible) View.VISIBLE else View.GONE
                if (!isDatePickerVisible) {
                    tv_event_start_time?.setText("")
                    tv_event_end_time?.setText("")
                    sorted_list.clear()
                    clearDates()
                }
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        return rootView
    }

    private fun clearDates() {
        sorted_list.clear()
        et_search_audit_list?.setText("")
        val FLAG = ""
        loadnewPage(null, FLAG)
        fetchpagedata()
    }

    private fun callAuditWebservice() {
        progress_dialog = AndroidUtils.get_progress(activity)
        val postData = JSONObject()
        try {
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/auditlogs",
                "Audit Logs",
                postData.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun loadSpinnerData() {
        categoryList.add(SpinnerItemModal("Authorization"))
        if ("solo" != Constants.CATEGORY) {
            categoryList.add(SpinnerItemModal("Groups"))
            categoryList.add(SpinnerItemModal("Team Members"))
        }
        categoryList.add(SpinnerItemModal("Relationships"))
        categoryList.add(SpinnerItemModal("Relationship Invite"))
        categoryList.add(SpinnerItemModal("Share"))
        categoryList.add(SpinnerItemModal("Documents"))
        categoryList.add(SpinnerItemModal("Merge PDF"))
        categoryList.add(SpinnerItemModal("Legal Matters"))
        categoryList.add(SpinnerItemModal("General Matters"))
        categoryAdapter = CommonSpinnerAdapter(activity, categoryList)
        sp_category?.adapter = categoryAdapter
        loadItemSelectedListner()
        if (autentication_list.isEmpty() && groups_list.isEmpty() && relationship_invite_list.isEmpty() && tm_list.isEmpty() && relationships_list.isEmpty() && share_list.isEmpty() && documents_list.isEmpty() && merge_pdf_list.isEmpty() && legal_matter_list.isEmpty() && general_matter_list.isEmpty()) {
            callAuditWebservice()
        }
    }

    private fun loadItemSelectedListner() {
        sp_category?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, i, id ->
            try {
                rv_audits?.removeAllViews()
                et_search_audit_list?.setText("")
                startDate = null
                endDate = null
                tv_event_start_time?.setText("")
                tv_event_end_time?.setText("")

                val selectedCategoryName = categoryList[i].name ?: ""
                if (selectedCategoryName.equals("Authorization", ignoreCase = true)) {
                    CategoryType = "AUTH"
                } else if (selectedCategoryName.equals("Groups", ignoreCase = true)) {
                    CategoryType = "GROUPS"
                } else if (selectedCategoryName.equals("Team Members", ignoreCase = true)) {
                    CategoryType = "TEAM MEMBER"
                } else if (selectedCategoryName.equals("Relationships", ignoreCase = true)) {
                    CategoryType = "RELATIONSHIP"
                } else if (selectedCategoryName.equals("Share", ignoreCase = true)) {
                    CategoryType = "SHARE"
                } else if (selectedCategoryName.equals("Relationship Invite", ignoreCase = true)) {
                    CategoryType = "RELATIONSHIP INVITE"
                } else if (selectedCategoryName.equals("Documents", ignoreCase = true)) {
                    CategoryType = "DOCUMENT"
                } else if (selectedCategoryName.equals("Merge PDF", ignoreCase = true)) {
                    CategoryType = "MERGE PDF"
                } else if (selectedCategoryName.equals("Legal Matters", ignoreCase = true)) {
                    CategoryType = "LEGAL MATTER"
                } else if (selectedCategoryName.equals("General Matters", ignoreCase = true)) {
                    CategoryType = "GENERAL MATTER"
                } else {
                    CategoryType = ""
                }
                Log.d("Category_type", CategoryType)
                Log.d("AuditSize", auditsList.size.toString())

                if (startDate != null || endDate != null) {
                    sorted_list.clear()
                    et_search_audit_list?.setText("")
                    val FLAG = "End Time"
                    loadnewPage(null, FLAG)
                } else {
                    isAdvancedSearchEnabled = false
                    tv_event_start_time?.setText("")
                    tv_event_end_time?.setText("")
                    et_search_audit_list?.setText("")
                    currentPage = 1
                    loadPage(currentPage, isAdvancedSearchEnabled)
                    if (CategoryType.isEmpty()) {
                        setupPagination(auditsList)
                    } else {
                        setupPagination(sorted_list)
                    }
                }
                UpdatePageButton(currentPage)

            } catch (e: Exception) {
                Log.d("Exception", e.message ?: "")
                throw RuntimeException(e)
            }
            AndroidUtils.DisplaySpinnerView(
                sp_category,
                tv_sp_category,
                categoryList[i].name,
                img_dropdown_icon,
                img_clear_icon,
                false,
                categoryAdapter,
                "Search Category"
            )
            iscategory_checked = true
        }
    }

    override fun onClick(view: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progress_dialog != null && progress_dialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }

        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent ?: "")
                Log.d("Request_Type", httpResult.requestType ?: "")

                if ("Audit Logs" == httpResult.requestType) {
                    val jsonArray = result.getJSONArray("data")
                    et_search_audit_list?.setText("")
                    loadNewAuditsData(jsonArray)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else if (httpResult.result == WebServiceHelper.ServiceCallStatus.Failed) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            try {
                val result = JSONObject(httpResult.responseContent ?: "")
                AndroidUtils.showErrorAlert(result.optString("msg"), activity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            AndroidUtils.showErrorAlert(
                httpResult.responseContent?.toString() ?: "",
                activity
            )
        }
    }

    private fun setupPagination(sorted_list: ArrayList<AuditsModel>) {
        pageNumberLayout?.removeAllViews()
        pagebuttons.clear()

        val totalPages = PaginationHelper.calculateTotalNoOfPages(sorted_list.size, 10)
        Log.d("total_pages", "$totalPages..${sorted_list.size}")
        if (totalPages == 0) {
            ll_list?.visibility = View.GONE
            tv_list?.visibility = View.VISIBLE
        } else {
            ll_list?.visibility = View.VISIBLE
            tv_list?.visibility = View.GONE
        }

        for (i in 1..totalPages) {
            val view_opponents = LayoutInflater.from(context).inflate(R.layout.page_number_layout, null)
            val pageButton = view_opponents.findViewById<Button>(R.id.page_number_button)
            pageButton.text = i.toString()
            val pageNumber = i
            pageButton.setOnClickListener {
                currentPage = pageNumber
                load_ChosenType_list()
                UpdatePageButton(currentPage)
                previousPageButton = pageButton
            }

            pageNumberLayout?.addView(view_opponents)
            pagebuttons.add(pageButton)
        }
    }

    private fun loadPage(page: Int, isAdvancedSearchEnabled: Boolean) {
        try {
            if (!isAdvancedSearchEnabled) {
                sorted_list.clear()
                when (CategoryType) {
                    "AUTH" -> sorted_list.addAll(autentication_list)
                    "GROUPS" -> sorted_list.addAll(groups_list)
                    "RELATIONSHIP INVITE" -> sorted_list.addAll(relationship_invite_list)
                    "TEAM MEMBER" -> sorted_list.addAll(tm_list)
                    "RELATIONSHIP" -> sorted_list.addAll(relationships_list)
                    "SHARE" -> sorted_list.addAll(share_list)
                    "DOCUMENT" -> sorted_list.addAll(documents_list)
                    "MERGE PDF" -> sorted_list.addAll(merge_pdf_list)
                    "LEGAL MATTER" -> sorted_list.addAll(legal_matter_list)
                    "GENERAL MATTER" -> sorted_list.addAll(general_matter_list)
                    else -> sorted_list.addAll(auditsList)
                }
            }
            loadRecyclerView(page, sorted_list)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadNewAuditsData(jsonArray: JSONArray) {
        try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val outputFormat = SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }
            clearLists()
            val EndIndex = Math.min(2500, jsonArray.length())
            for (i in 0 until EndIndex) {
                val auditsModel = AuditsModel()
                val jsonObject = jsonArray.getJSONObject(i)
                auditsModel.message = jsonObject.getString("message")
                auditsModel.name = jsonObject.getString("name")
                val rawTimestamp = jsonObject.getString("timestamp")
                val parsedDate = isoFormat.parse(rawTimestamp)
                val formattedTimestamp = if (parsedDate != null) outputFormat.format(parsedDate) else ""
                auditsModel.timestamp = formattedTimestamp

                if (auditsModel.message != "PROFILE UPDATE" && auditsModel.message != "DOCS COLLABORATION") {
                    auditsList.add(auditsModel)
                    val name = jsonObject.getString("name")
                    when (name) {
                        "AUTH" -> autentication_list.add(auditsModel)
                        "GROUPS" -> groups_list.add(auditsModel)
                        "RELATIONSHIP INVITE" -> relationship_invite_list.add(auditsModel)
                        "TEAM MEMBER" -> tm_list.add(auditsModel)
                        "RELATIONSHIP" -> relationships_list.add(auditsModel)
                        "SHARE" -> share_list.add(auditsModel)
                        "DOCUMENT" -> documents_list.add(auditsModel)
                        "MERGE PDF" -> merge_pdf_list.add(auditsModel)
                        "LEGAL MATTER" -> legal_matter_list.add(auditsModel)
                        "GENERAL MATTER" -> general_matter_list.add(auditsModel)
                    }
                }
            }
            Log.d("auth_size", "" + auditsList.size)
            ll_page_navigation?.visibility = View.VISIBLE
            loadGeneralList()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun loadGeneralList() {
        currentPage = 1
        loadRecyclerView(currentPage, auditsList)
        setupPagination(auditsList)
        UpdatePageButton(currentPage)
    }

    private fun clearLists() {
        autentication_list.clear()
        groups_list.clear()
        relationship_invite_list.clear()
        tm_list.clear()
        relationships_list.clear()
        share_list.clear()
        documents_list.clear()
        merge_pdf_list.clear()
        legal_matter_list.clear()
        general_matter_list.clear()
        auditsList.clear()
    }

    private fun loadRecyclerView(page: Int, sorted_list: ArrayList<AuditsModel>) {
        try {
            Log.d("Sorted_list_new", sorted_list.size.toString())
            if (sorted_list.isNotEmpty()) {
                val startIndex = PaginationHelper.startIndexForCurrentPage(currentPage, 10)
                val endIndex = PaginationHelper.endIndexForCurrentPage(startIndex, sorted_list.size, 10)
                end_temp = endIndex
                pageItems = ArrayList(sorted_list.subList(startIndex, endIndex))

                if (audit_adapter == null) {
                    audit_adapter = AuditsAdapter(sorted_list)
                    rv_audits?.adapter = audit_adapter
                    Log.d("sorted_list", "" + sorted_list.size)
                    audit_adapter?.setData(pageItems)
                    AndroidUtils.LoadingRecyclerview(rv_audits, context)
                    AndroidUtils.setupBottomSpacerFooter(
                        rv_audits,
                        resources.getDimensionPixelSize(R.dimen.twentyeight_dp)
                    )
                    et_search_audit_list?.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            Searchfilter()
                        }
                    })
                } else {
                    audit_adapter?.setData(pageItems)
                }
                UpdatePageButton(1)
            }
        } catch (e: Exception) {
            Log.d("Recyclervie_Exception", e.message ?: "")
            throw RuntimeException(e)
        }
    }

    override fun onDateSelected(selectedDate: String?, FLAG: String?) {
        loadnewPage(selectedDate, FLAG)
        fetchpagedata()
    }

    override fun onDateSelectedEndDate(selectedDate: String?, FLAG: String?) {
        loadnewPage(selectedDate, FLAG)
        fetchpagedata()
    }

    private fun fetchpagedata() {
        setupPagination(sorted_list)
        currentPage = 1
        isAdvancedSearchEnabled = true
        loadPage(currentPage, isAdvancedSearchEnabled)
    }

    private fun load_ChosenType_list() {
        if (CategoryType.isEmpty()) {
            if (isListFiltered) {
                loadRecyclerView(currentPage, filterlist)
            } else if (isAdvancedSearchEnabled) {
                loadRecyclerView(currentPage, sorted_list)
            } else {
                loadRecyclerView(currentPage, auditsList)
            }
        } else {
            if (isListFiltered) {
                loadRecyclerView(currentPage, filterlist)
            } else {
                loadRecyclerView(currentPage, sorted_list)
            }
        }
    }

    private fun Searchfilter() {
        rv_audits?.removeAllViews()
        filterlist.clear()
        val searchText = et_search_audit_list?.text?.toString() ?: ""
        when (CategoryType) {
            "AUTH" -> {
                if (!isAdvancedSearchEnabled) {
                    filterlist.addAll(filterList(autentication_list, searchText))
                } else {
                    filterlist.addAll(filterList(sorted_list, searchText))
                }
            }
            "GROUPS" -> {
                if (!isAdvancedSearchEnabled) {
                    filterlist.addAll(filterList(groups_list, searchText))
                } else {
                    filterlist.addAll(filterList(sorted_list, searchText))
                }
            }
            "TEAM MEMBER" -> {
                if (!isAdvancedSearchEnabled) {
                    filterlist.addAll(filterList(tm_list, searchText))
                } else {
                    filterlist.addAll(filterList(sorted_list, searchText))
                }
            }
            "RELATIONSHIP" -> {
                if (!isAdvancedSearchEnabled) {
                    filterlist.addAll(filterList(relationships_list, searchText))
                } else {
                    filterlist.addAll(filterList(sorted_list, searchText))
                }
            }
            "RELATIONSHIP INVITE" -> {
                if (!isAdvancedSearchEnabled) {
                    filterlist.addAll(filterList(relationship_invite_list, searchText))
                } else {
                    filterlist.addAll(filterList(sorted_list, searchText))
                }
            }
            "SHARE" -> {
                if (!isAdvancedSearchEnabled) {
                    filterlist.addAll(filterList(share_list, searchText))
                } else {
                    filterlist.addAll(filterList(sorted_list, searchText))
                }
            }
            "DOCUMENT" -> {
                if (!isAdvancedSearchEnabled) {
                    filterlist.addAll(filterList(documents_list, searchText))
                } else {
                    filterlist.addAll(filterList(sorted_list, searchText))
                }
            }
            "MERGE PDF" -> {
                if (!isAdvancedSearchEnabled) {
                    filterlist.addAll(filterList(merge_pdf_list, searchText))
                } else {
                    filterlist.addAll(filterList(sorted_list, searchText))
                }
            }
            "LEGAL MATTER" -> {
                if (!isAdvancedSearchEnabled) {
                    filterlist.addAll(filterList(legal_matter_list, searchText))
                } else {
                    filterlist.addAll(filterList(sorted_list, searchText))
                }
            }
            "GENERAL MATTER" -> {
                if (!isAdvancedSearchEnabled) {
                    filterlist.addAll(filterList(general_matter_list, searchText))
                } else {
                    filterlist.addAll(filterList(sorted_list, searchText))
                }
            }
            else -> {
                if (!isAdvancedSearchEnabled) {
                    filterlist.addAll(filterList(auditsList, searchText))
                } else {
                    filterlist.addAll(filterList(sorted_list, searchText))
                }
            }
        }
        currentPage = 1
        if (CategoryType.isEmpty()) {
            if (searchText.isNotEmpty()) {
                loadRecyclerView(currentPage, filterlist)
                setupPagination(filterlist)
            } else if (!isAdvancedSearchEnabled) {
                loadRecyclerView(currentPage, auditsList)
                setupPagination(auditsList)
            } else {
                loadRecyclerView(currentPage, sorted_list)
                setupPagination(sorted_list)
            }
            UpdatePageButton(currentPage)
        } else {
            if (searchText.isNotEmpty()) {
                loadRecyclerView(currentPage, filterlist)
                setupPagination(filterlist)
            } else {
                loadRecyclerView(currentPage, sorted_list)
                setupPagination(sorted_list)
            }
            UpdatePageButton(currentPage)
        }
    }

    private fun filterList(list: ArrayList<AuditsModel>, charString: String): ArrayList<AuditsModel> {
        isListFiltered = false
        if (charString.isEmpty()) {
            return list
        } else {
            val filteredList = ArrayList<AuditsModel>()
            val pattern = Pattern.compile(Pattern.quote(charString), Pattern.CASE_INSENSITIVE)

            for (row in list) {
                if (pattern.matcher(AndroidUtils.isNull(row.message).lowercase()).find()
                    || pattern.matcher(AndroidUtils.isNull(row.timestamp).lowercase()).find()
                    || pattern.matcher(AndroidUtils.isNull(row.name).lowercase()).find()
                ) {
                    filteredList.add(row)
                }
            }
            if (filteredList.isEmpty()) {
                return ArrayList()
            } else {
                isListFiltered = true
                return filteredList
            }
        }
    }

    private fun loadnewPage(selectedDate: String?, FLAG: String?) {
        try {
            sorted_list.clear()
            rv_audits?.removeAllViews()
            et_search_audit_list?.setText("")

            val startTimeString = tv_event_start_time?.text?.toString() ?: ""
            val endTimeString = tv_event_end_time?.text?.toString() ?: ""

            startDate = DateUtils.stringToDate(startTimeString)
            endDate = DateUtils.stringToDate(endTimeString)

            when (CategoryType) {
                "AUTH" -> loadAdvancedData(autentication_list)
                "GROUPS" -> loadAdvancedData(groups_list)
                "RELATIONSHIP INVITE" -> loadAdvancedData(relationship_invite_list)
                "TEAM MEMBER" -> loadAdvancedData(tm_list)
                "RELATIONSHIP" -> loadAdvancedData(relationships_list)
                "SHARE" -> loadAdvancedData(share_list)
                "DOCUMENT" -> loadAdvancedData(documents_list)
                "MERGE PDF" -> loadAdvancedData(merge_pdf_list)
                "LEGAL MATTER" -> loadAdvancedData(legal_matter_list)
                "GENERAL MATTER" -> loadAdvancedData(general_matter_list)
                else -> loadAdvancedData(auditsList)
            }

            filterByDateRange()

            audit_adapter = AuditsAdapter(sorted_list)
            rv_audits?.adapter = audit_adapter
            AndroidUtils.LoadingRecyclerview(rv_audits, context)
            AndroidUtils.setupBottomSpacerFooter(
                rv_audits,
                resources.getDimensionPixelSize(R.dimen.twentyeight_dp)
            )
            currentPage = 1
            UpdatePageButton(currentPage)
            audit_adapter?.updateData(sorted_list)
            et_search_audit_list?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    Searchfilter()
                }
            })
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun filterByDateRange() {
        try {
            val filteredList = ArrayList<AuditsModel>()
            val formattedParser = SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }

            for (auditsModel in sorted_list) {
                val formattedTimestamp = auditsModel.timestamp ?: ""
                val parsedDate = formattedParser.parse(formattedTimestamp)
                if (parsedDate != null && isDateInRange(parsedDate, startDate, endDate)) {
                    filteredList.add(auditsModel)
                }
            }
            sorted_list.clear()
            sorted_list.addAll(filteredList)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun UpdatePageButton(currentPage: Int) {
        for (i in pagebuttons.indices) {
            val pageButton = pagebuttons[i]
            if (currentPage >= 0) {
                if (i + 1 == currentPage) {
                    pageButton.setTextColor(requireActivity().getColor(R.color.white))
                    pageButton.background = requireActivity().getDrawable(R.drawable.blue_gradient_card)
                } else {
                    pageButton.setTextColor(requireActivity().getColor(R.color.blue))
                    pageButton.background = requireActivity().getDrawable(R.drawable.background_transparent)
                }
            }
        }
    }

    private fun loadAdvancedData(advanced_list: ArrayList<AuditsModel>) {
        sorted_list.clear()
        val formattedParser = SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }

        for (i in 0 until advanced_list.size) {
            try {
                val auditsModel = advanced_list[i]
                Log.d("Advanced_DATA", auditsModel.name ?: "")
                val formattedTimestamp = auditsModel.timestamp ?: ""
                val parsedDate = formattedParser.parse(formattedTimestamp)
                Log.d("Formatted_Output", formattedTimestamp)

                var isInRange = false
                if (startDate != null && endDate != null) {
                    isInRange = parsedDate != null && parsedDate.after(startDate) && parsedDate.before(endDate)
                } else if (startDate != null) {
                    isInRange = parsedDate != null && (parsedDate.after(startDate) || parsedDate == startDate)
                } else if (endDate != null) {
                    isInRange = parsedDate != null && (parsedDate.before(endDate) || parsedDate == endDate)
                } else {
                    isInRange = true
                }

                if (isInRange && (auditsModel.name ?: "").startsWith(CategoryType.uppercase(Locale.ROOT))) {
                    sorted_list.add(auditsModel)
                }
            } catch (e: java.text.ParseException) {
                e.printStackTrace()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

    private fun isDateInRange(date: Date, startDate: Date?, endDate: Date?): Boolean {
        return (startDate == null || !date.before(startDate)) &&
                (endDate == null || !date.after(endDate))
    }

}
