package com.digicoffer.lauditor.AuditTrails

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.digicoffer.lauditor.AuditTrails.ui.AuditTrailsScreen
import com.digicoffer.lauditor.AuditTrails.Model.AuditTrailsStateHolder
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
    
    private val stateHolder = AuditTrailsStateHolder()
    private var isDatePickerVisibleState by mutableStateOf(false)
    private var startDateTextState by mutableStateOf("")
    private var endDateTextState by mutableStateOf("")

    var categoryList = ArrayList<SpinnerItemModal>()
    var filterlist = ArrayList<AuditsModel>()
    var isListFiltered = false
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
    var pageItems = ArrayList<AuditsModel>()
    var tv_event_start_time: AppCompatButton? = null
    var tv_event_end_time: AppCompatButton? = null
    private var currentPage = 1
    var auditsList = ArrayList<AuditsModel>()
    var progress_dialog: Dialog? = null
    var CategoryType = ""
    var et_search_audit_list: TextInputEditText? = null
    private val itemsPerPage = 10
    var isAdvancedSearchEnabled = false
    private var mViewModel: NewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            // Programmatically instantiate views referenced by legacy filter/search routines to avoid NullPointerExceptions
            tv_event_start_time = AppCompatButton(requireContext())
            tv_event_end_time = AppCompatButton(requireContext())
            et_search_audit_list = TextInputEditText(requireContext())

            mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
            mViewModel?.setData(requireContext().getString(R.string.audit_trails))

            loadSpinnerData()

            return ComposeView(requireContext()).apply {
                setContent {
                    com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme {
                        val uiState = stateHolder.uiState.value
                        val categoryNames = categoryList.map { it.name ?: "" }

                        AuditTrailsScreen(
                            state = uiState,
                            categories = categoryNames,
                            onCategorySelected = { category ->
                                stateHolder.updateSelectedCategory(category)
                                handleCategorySelected(category)
                            },
                            onQueryChanged = { query ->
                                stateHolder.updateSearchQuery(query)
                                et_search_audit_list?.setText(query)
                                Searchfilter()
                            },
                            onStartDateClick = {
                                val tempTv = TextView(requireContext()).apply { text = startDateTextState }
                                AndroidUtils.showDatePicker(tempTv, true) {
                                    val dateStr = tempTv.text.toString()
                                    startDateTextState = dateStr
                                    stateHolder.updateDateRange(
                                        DateUtils.stringToDate(dateStr),
                                        stateHolder.uiState.value.endDate
                                    )
                                    loadnewPage(dateStr, "St Time")
                                    fetchpagedata()
                                }
                            },
                            onEndDateClick = {
                                val tempTv = TextView(requireContext()).apply { text = endDateTextState }
                                AndroidUtils.showDatePicker(tempTv, false) {
                                    val dateStr = tempTv.text.toString()
                                    endDateTextState = dateStr
                                    stateHolder.updateDateRange(
                                        stateHolder.uiState.value.startDate,
                                        DateUtils.stringToDate(dateStr)
                                    )
                                    loadnewPage(dateStr, "End Time")
                                    fetchpagedata()
                                }
                            },
                            onClearStartDate = {
                                startDateTextState = ""
                                tv_event_start_time?.setText("")
                                stateHolder.updateDateRange(null, stateHolder.uiState.value.endDate)
                                clearDates()
                            },
                            onClearEndDate = {
                                endDateTextState = ""
                                tv_event_end_time?.setText("")
                                stateHolder.updateDateRange(stateHolder.uiState.value.startDate, null)
                                clearDates()
                            },
                            onPageSelected = { page ->
                                currentPage = page
                                load_ChosenType_list()
                                stateHolder.updateCurrentPage(page)
                            },
                            onClearCategory = {
                                CategoryType = ""
                                stateHolder.updateSelectedCategory("")
                                isDatePickerVisibleState = false
                                startDateTextState = ""
                                endDateTextState = ""
                                tv_event_start_time?.setText("")
                                tv_event_end_time?.setText("")
                                stateHolder.updateDateRange(null, null)
                                et_search_audit_list?.setText("")
                                stateHolder.updateSearchQuery("")
                                loadGeneralList()
                            },
                            onAdvancedSearchToggle = {
                                isDatePickerVisibleState = !isDatePickerVisibleState
                                if (!isDatePickerVisibleState) {
                                    startDateTextState = ""
                                    endDateTextState = ""
                                    tv_event_start_time?.setText("")
                                    tv_event_end_time?.setText("")
                                    stateHolder.updateDateRange(null, null)
                                    clearDates()
                                }
                            },
                            isDatePickerVisible = isDatePickerVisibleState,
                            startDateText = startDateTextState,
                            endDateText = endDateTextState
                        )
                    }
                }
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun handleCategorySelected(selectedCategoryName: String) {
        try {
            et_search_audit_list?.setText("")
            stateHolder.updateSearchQuery("")
            startDate = null
            endDate = null
            tv_event_start_time?.setText("")
            tv_event_end_time?.setText("")
            startDateTextState = ""
            endDateTextState = ""
            stateHolder.updateDateRange(null, null)

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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clearDates() {
        sorted_list.clear()
        et_search_audit_list?.setText("")
        stateHolder.updateSearchQuery("")
        val FLAG = ""
        loadnewPage(null, FLAG)
        fetchpagedata()
    }

    private fun callAuditWebservice() {
        stateHolder.setLoading(true)
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
            stateHolder.setLoading(false)
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun loadSpinnerData() {
        categoryList.clear()
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

        if (autentication_list.isEmpty() && groups_list.isEmpty() && relationship_invite_list.isEmpty() && tm_list.isEmpty() && relationships_list.isEmpty() && share_list.isEmpty() && documents_list.isEmpty() && merge_pdf_list.isEmpty() && legal_matter_list.isEmpty() && general_matter_list.isEmpty()) {
            callAuditWebservice()
        }
    }

    override fun onClick(view: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        stateHolder.setLoading(false)
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
                    stateHolder.updateSearchQuery("")
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
        val totalPages = PaginationHelper.calculateTotalNoOfPages(sorted_list.size, 10)
        stateHolder.updateLists(sorted_list, pageItems, totalPages)
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
            syncPageData(page, sorted_list)
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
            loadGeneralList()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun loadGeneralList() {
        currentPage = 1
        syncPageData(currentPage, auditsList)
        setupPagination(auditsList)
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

    private fun syncPageData(page: Int, sorted_list: ArrayList<AuditsModel>) {
        try {
            Log.d("Sorted_list_new", sorted_list.size.toString())
            if (sorted_list.isNotEmpty()) {
                val startIndex = PaginationHelper.startIndexForCurrentPage(currentPage, 10)
                val endIndex = PaginationHelper.endIndexForCurrentPage(startIndex, sorted_list.size, 10)
                end_temp = endIndex
                pageItems = ArrayList(sorted_list.subList(startIndex, endIndex))

                val totalPages = PaginationHelper.calculateTotalNoOfPages(sorted_list.size, 10)
                stateHolder.updateLists(sorted_list, pageItems, totalPages)
                stateHolder.updateCurrentPage(currentPage)
            } else {
                pageItems.clear()
                stateHolder.updateLists(emptyList(), emptyList(), 0)
                stateHolder.updateCurrentPage(1)
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
                syncPageData(currentPage, filterlist)
            } else if (isAdvancedSearchEnabled) {
                syncPageData(currentPage, sorted_list)
            } else {
                syncPageData(currentPage, auditsList)
            }
        } else {
            if (isListFiltered) {
                syncPageData(currentPage, filterlist)
            } else {
                syncPageData(currentPage, sorted_list)
            }
        }
    }

    private fun Searchfilter() {
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
                syncPageData(currentPage, filterlist)
                setupPagination(filterlist)
            } else if (!isAdvancedSearchEnabled) {
                syncPageData(currentPage, auditsList)
                setupPagination(auditsList)
            } else {
                syncPageData(currentPage, sorted_list)
                setupPagination(sorted_list)
            }
        } else {
            if (searchText.isNotEmpty()) {
                syncPageData(currentPage, filterlist)
                setupPagination(filterlist)
            } else {
                syncPageData(currentPage, sorted_list)
                setupPagination(sorted_list)
            }
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
            currentPage = 1
            syncPageData(currentPage, sorted_list)
            setupPagination(sorted_list)
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
