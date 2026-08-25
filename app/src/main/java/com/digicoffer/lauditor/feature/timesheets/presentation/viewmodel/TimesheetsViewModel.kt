package com.digicoffer.lauditor.feature.timesheets.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.TimeSheets.Models.Month_Model
import com.digicoffer.lauditor.TimeSheets.Models.ProjectTMModel
import com.digicoffer.lauditor.TimeSheets.Models.ProjectsModel
import com.digicoffer.lauditor.TimeSheets.Models.TMModel
import com.digicoffer.lauditor.TimeSheets.Models.TSMatterModel
import com.digicoffer.lauditor.TimeSheets.Models.TaskModel
import com.digicoffer.lauditor.TimeSheets.Models.TasksModel
import com.digicoffer.lauditor.TimeSheets.Models.WeekDateInfo
import com.digicoffer.lauditor.TimeSheets.Models.WeekTotalModel
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.feature.timesheets.data.repository.TimesheetsRepository
import com.digicoffer.lauditor.feature.timesheets.presentation.state.TimesheetsUiEvent
import com.digicoffer.lauditor.feature.timesheets.presentation.state.TimesheetsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TimesheetsViewModel(
    private val repository: TimesheetsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimesheetsUiState())
    val uiState: StateFlow<TimesheetsUiState> = _uiState.asStateFlow()

    private var calendarWeek = Calendar.getInstance()
    private var calendarMonth = Calendar.getInstance()

    init {
        updateDateBounds()
    }

    fun onEvent(event: TimesheetsUiEvent) {
        when (event) {
            is TimesheetsUiEvent.MainTabSelected -> {
                _uiState.update { it.copy(mainTab = event.tab, searchQuery = "") }
                loadCurrentTabTimesheets()
            }
            is TimesheetsUiEvent.SubTabSelected -> {
                _uiState.update { it.copy(subTab = event.tab, searchQuery = "") }
                loadCurrentTabTimesheets()
            }
            is TimesheetsUiEvent.DateRangeTypeChanged -> {
                _uiState.update { it.copy(isWeek = event.type) }
                updateDateBounds()
                loadCurrentTabTimesheets()
            }
            is TimesheetsUiEvent.NextRange -> {
                if (_uiState.value.isWeek == "month") {
                    calendarMonth.add(Calendar.MONTH, 1)
                } else {
                    calendarWeek.add(Calendar.WEEK_OF_YEAR, 1)
                }
                updateDateBounds()
                loadCurrentTabTimesheets()
            }
            is TimesheetsUiEvent.PreviousRange -> {
                if (_uiState.value.isWeek == "month") {
                    calendarMonth.add(Calendar.MONTH, -1)
                } else {
                    calendarWeek.add(Calendar.WEEK_OF_YEAR, -1)
                }
                updateDateBounds()
                loadCurrentTabTimesheets()
            }
            is TimesheetsUiEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is TimesheetsUiEvent.LoadCurrentTabTimesheets -> {
                loadCurrentTabTimesheets()
            }
            is TimesheetsUiEvent.ToggleFormVisibility -> {
                _uiState.update { it.copy(isFormVisible = event.visible) }
                if (!event.visible) {
                    clearFormFields()
                }
            }
            is TimesheetsUiEvent.MatterSelected -> {
                _uiState.update { it.copy(selectedMatter = event.matter, selectedTask = null) }
                event.matter?.let {
                    fetchTasksForMatter(it)
                }
            }
            is TimesheetsUiEvent.TaskSelected -> {
                _uiState.update { it.copy(selectedTask = event.task) }
            }
            is TimesheetsUiEvent.StatusSelected -> {
                _uiState.update { it.copy(selectedStatus = event.status) }
            }
            is TimesheetsUiEvent.LogDateSelected -> {
                _uiState.update { it.copy(selectedDate = event.date) }
            }
            is TimesheetsUiEvent.HoursChanged -> {
                _uiState.update { it.copy(hours = event.hours) }
            }
            is TimesheetsUiEvent.MinutesChanged -> {
                _uiState.update { it.copy(minutes = event.minutes) }
            }
            is TimesheetsUiEvent.DescriptionChanged -> {
                _uiState.update { it.copy(description = event.desc) }
            }
            is TimesheetsUiEvent.BillableChanged -> {
                _uiState.update { it.copy(isBillable = event.billable) }
            }
            is TimesheetsUiEvent.ClearForm -> {
                clearFormFields()
            }
            is TimesheetsUiEvent.SaveTimesheet -> {
                saveOrUpdateTimesheet()
            }
            is TimesheetsUiEvent.EditTimesheet -> {
                populateFormForEdit(event.log)
            }
            is TimesheetsUiEvent.DeleteTimesheet -> {
                deleteTimesheetLog(event.log)
            }
            is TimesheetsUiEvent.SubmitTimesheets -> {
                submitWeeklyTimesheets()
            }
            is TimesheetsUiEvent.AggregatedProjectSelected -> {
                _uiState.update { it.copy(selectedProject = event.project, selectedProjectTeamMember = null) }
            }
            is TimesheetsUiEvent.AggregatedProjectTMSelected -> {
                _uiState.update { it.copy(selectedProjectTeamMember = event.tm) }
            }
            is TimesheetsUiEvent.DismissDialogs -> {
                _uiState.update { it.copy(alertTitle = null, alertMessage = null, toastMessage = null) }
            }
            else -> {}
        }
    }

    fun setDateFromDatePicker(year: Int, month: Int, day: Int) {
        calendarWeek.set(Calendar.YEAR, year)
        calendarWeek.set(Calendar.MONTH, month)
        calendarWeek.set(Calendar.DAY_OF_MONTH, day)
        updateDateBounds()
        loadCurrentTabTimesheets()
    }

    private fun updateDateBounds() {
        val state = _uiState.value
        if (state.isWeek == "month") {
            val bounds = getMonthRange(calendarMonth)
            _uiState.update {
                it.copy(
                    fromDateString = AndroidUtils.formatToMMMddYYYY(bounds.first) ?: "",
                    toDateString = AndroidUtils.formatToMMMddYYYY(bounds.second) ?: "",
                    weekDateInfo = null
                )
            }
        } else {
            val weekInfo = getWeekDateRange(calendarWeek)
            if (weekInfo.weekDates != null && weekInfo.weekDates!!.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        fromDateString = AndroidUtils.formatToMMMddYYYY(weekInfo.weekDates!![0]) ?: "",
                        toDateString = AndroidUtils.formatToMMMddYYYY(weekInfo.weekDates!![weekInfo.weekDates!!.size - 1]) ?: "",
                        weekDateInfo = weekInfo
                    )
                }
            }
        }
    }

    private fun loadCurrentTabTimesheets() {
        val state = _uiState.value
        if (state.mainTab == "Aggregated") {
            if (state.subTab == "TM") {
                loadAggregatedTeamMembers()
            } else {
                loadAggregatedProjects()
            }
        } else {
            val submitted = state.subTab == "Submitted"
            loadMyTimesheets(submitted)
        }
    }

    private fun loadMyTimesheets(submitted: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val dateStr = AndroidUtils.convertAnyDateToDDMMYYYY(_uiState.value.fromDateString) ?: ""
            val result = repository.fetchTimesheets(dateStr, submitted)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                parseMyTimesheetsResponse(result.responseContent ?: "")
            } else {
                _uiState.update {
                    it.copy(
                        alertTitle = "Error",
                        alertMessage = result.responseContent ?: "Failed to load timesheets"
                    )
                }
            }
        }
    }

    private fun parseMyTimesheetsResponse(response: String) {
        try {
            val root = JSONObject(response)
            val dates = root.optJSONObject("dates") ?: JSONObject()
            val isFrozen = dates.optBoolean("isFrozen", false)

            val timesheetListObj = root.optJSONObject("timesheetList") ?: JSONObject()
            val weekTotalObj = timesheetListObj.optJSONObject("weekTotal") ?: JSONObject()
            val wTotal = timesheetListObj.optString("wTotal", "")

            val weekTotalModel = WeekTotalModel().apply {
                Mon = weekTotalObj.optString("Mon", "0")
                Tue = weekTotalObj.optString("Tue", "0")
                Wed = weekTotalObj.optString("Wed", "0")
                Thu = weekTotalObj.optString("Thu", "0")
                Fri = weekTotalObj.optString("Fri", "0")
                Sat = weekTotalObj.optString("Sat", "0")
                Sun = weekTotalObj.optString("Sun", "0")
            }

            // Parse Matters (for form options)
            val mattersArr = timesheetListObj.optJSONArray("matters")
            val parsedMatters = mutableListOf<TSMatterModel>()
            if (mattersArr != null) {
                for (i in 0 until mattersArr.length()) {
                    val mObj = mattersArr.getJSONObject(i)
                    parsedMatters.add(TSMatterModel().apply {
                        mattername = mObj.optString("matterName")
                        matterid = mObj.optString("matterId")
                        iseditable = mObj.optBoolean("is_editable", true)
                        Tasks = mObj.optJSONArray("tasks")
                        matter_type = mObj.optString("matterType")
                    })
                }
            }

            // Parse Active Projects
            val activeProjArr = timesheetListObj.optJSONArray("activeProjects")
            val parsedActiveProjects = mutableListOf<TSMatterModel>()
            if (activeProjArr != null) {
                for (i in 0 until activeProjArr.length()) {
                    val pObj = activeProjArr.getJSONObject(i)
                    parsedActiveProjects.add(TSMatterModel().apply {
                        mattername = pObj.optString("matterName")
                        matterid = pObj.optString("matterId")
                        iseditable = pObj.optBoolean("is_editable", true)
                        Tasks = pObj.optJSONArray("tasks")
                        matter_type = pObj.optString("matterType")
                    })
                }
            }

            // Extract all task logs from matters
            val extractedLogs = mutableListOf<TaskModel>()
            parsedMatters.forEach { matter ->
                val tasks = matter.Tasks
                if (tasks != null) {
                    for (i in 0 until tasks.length()) {
                        val tObj = tasks.getJSONObject(i)
                        val billingVal = tObj.optString("billing", "")
                        val taskNameVal = tObj.optString("taskName", "")

                        // Map day-wise entries to TaskModel logs
                        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        days.forEach { day ->
                            val dayObj = tObj.optJSONObject(day)
                            if (dayObj != null) {
                                val hrs = dayObj.optString("hours", "0")
                                val mins = dayObj.optString("minutes", "0")
                                if (hrs != "0" || mins != "0") {
                                    val log = TaskModel().apply {
                                        taskid = dayObj.optString("taskId", "")
                                        matterid = dayObj.optString("matterId", "")
                                        hours = hrs
                                        minutes = mins
                                        Task_name = taskNameVal
                                        Task_billing = billingVal
                                        Task_matter_name = matter.mattername
                                        is_editable = matter.iseditable
                                        Task_matter_id = matter.matterid
                                        isLinkedWithCalendar = dayObj.optBoolean("isLinkedWithCalendar", false)
                                        
                                        // Specific Day info formatted as: "Mon 20-08-2026"
                                        val dayIndex = days.indexOf(day)
                                        val weekDates = _uiState.value.weekDateInfo?.weekDates
                                        val logDateStr = if (weekDates != null && dayIndex < weekDates.size) {
                                            "$day ${weekDates[dayIndex]}"
                                        } else {
                                            day
                                        }
                                        date = logDateStr
                                        
                                        val perms = dayObj.optJSONObject("permissions")
                                        if (perms != null) {
                                            editProject = perms.optBoolean("editProject", true)
                                            editTask = perms.optBoolean("editTask", true)
                                            editStatus = perms.optBoolean("editStatus", true)
                                            editDate = perms.optBoolean("editDate", true)
                                            editHours = perms.optBoolean("editHours", true)
                                            editMinutes = perms.optBoolean("editMinutes", true)
                                        }
                                    }
                                    extractedLogs.add(log)
                                }
                            }
                        }
                    }
                }
            }

            _uiState.update {
                it.copy(
                    isFrozen = isFrozen,
                    weekTotalString = wTotal,
                    weekTotals = weekTotalModel,
                    matterList = parsedMatters,
                    activeProjectsList = parsedActiveProjects,
                    timesheetsList = extractedLogs
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(alertTitle = "Parsing Exception", alertMessage = e.message) }
        }
    }

    private fun fetchTasksForMatter(matter: TSMatterModel) {
        viewModelScope.launch {
            val matterType = matter.matter_type ?: ""
            val param = if (matterType.isNotEmpty()) matterType else (matter.matterid ?: "").lowercase(Locale.ROOT)
            
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.fetchTasks(param)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val root = JSONObject(result.responseContent ?: "")
                    val tasksArr = root.optJSONArray("tasks") ?: JSONArray()
                    val parsedTasks = mutableListOf<TasksModel>()
                    for (i in 0 until tasksArr.length()) {
                        val tObj = tasksArr.getJSONObject(i)
                        parsedTasks.add(TasksModel().apply {
                            displayValue = tObj.optString("displayValue")
                            returnValue = tObj.optString("returnValue")
                        })
                    }
                    _uiState.update { it.copy(taskList = parsedTasks) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(alertTitle = "Parsing Error", alertMessage = e.message) }
                }
            }
        }
    }

    private fun saveOrUpdateTimesheet() {
        val state = _uiState.value
        if (state.selectedMatter == null) {
            _uiState.update { it.copy(alertTitle = "Validation Error", alertMessage = "Please select a matter") }
            return
        }
        if (state.selectedTask == null) {
            _uiState.update { it.copy(alertTitle = "Validation Error", alertMessage = "Please select a task") }
            return
        }
        if (state.selectedDate.isEmpty()) {
            _uiState.update { it.copy(alertTitle = "Validation Error", alertMessage = "Please select a date") }
            return
        }
        val hrs = state.hours.trim().toIntOrNull() ?: 0
        val mins = state.minutes.trim()
        if (hrs == 0 && (mins.isEmpty() || mins == "0")) {
            _uiState.update { it.copy(alertTitle = "Validation Error", alertMessage = "Duration cannot be zero") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val data = JSONObject()
            
            // Format selected date from: "Mon 20-08-2026" to "dd-MM-yyyy"
            val rawDate = state.selectedDate.split(" ").last()
            
            if (state.isEditMode) {
                // Update PUT payload
                data.put("id", state.editingLogId)
                data.put("action", "hours")
                data.put("billing", if (state.selectedStatus == "Billable") "billable" else "nonbillable")
                
                val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
                val dVal = inputFormat.parse(rawDate)
                val formattedDate = if (dVal != null) outputFormat.format(dVal) else rawDate
                data.put("date", formattedDate)
                data.put("duration_hours", hrs.toString())
                data.put("duration_minutes", if (mins.isEmpty() || mins == "0") "00" else mins)
                data.put("matter_id", state.selectedMatter.matterid)
                data.put("timesheet_update_scope", "UPDATE_TIMESHEET_ONLY")
                data.put("matter_type", state.selectedMatter.mattername)
                data.put("title", state.selectedTask.displayValue)
                
                val result = repository.updateTimesheet(data)
                _uiState.update { it.copy(isLoading = false) }
                if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val resObj = JSONObject(result.responseContent ?: "")
                    _uiState.update { it.copy(toastMessage = resObj.optString("msg", "Timesheet updated successfully"), isFormVisible = false) }
                    clearFormFields()
                    loadCurrentTabTimesheets()
                } else {
                    _uiState.update { it.copy(alertTitle = "Error", alertMessage = result.responseContent) }
                }
            } else {
                // Save POST payload
                data.put("action", "hours")
                data.put("billing", if (state.selectedStatus == "Billable") "billable" else "nonbillable")
                
                val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
                val dVal = inputFormat.parse(rawDate)
                val formattedDate = if (dVal != null) outputFormat.format(dVal) else rawDate
                data.put("date", formattedDate)
                data.put("duration_hours", hrs.toString())
                data.put("duration_minutes", if (mins.isEmpty() || mins == "0") "00" else mins)
                data.put("matter_id", state.selectedMatter.matterid)
                
                val isMatterTypeExists = state.activeProjectsList.any { it.matter_type == state.selectedMatter.matter_type }
                data.put("matter_type", if (isMatterTypeExists) state.selectedMatter.matter_type else state.selectedMatter.mattername)
                data.put("title", state.selectedTask.displayValue)
                
                val result = repository.saveTimesheet(data)
                _uiState.update { it.copy(isLoading = false) }
                if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val resObj = JSONObject(result.responseContent ?: "")
                    _uiState.update { it.copy(toastMessage = resObj.optString("msg", "Timesheet saved successfully"), isFormVisible = false) }
                    clearFormFields()
                    loadCurrentTabTimesheets()
                } else {
                    _uiState.update { it.copy(alertTitle = "Error", alertMessage = result.responseContent) }
                }
            }
        }
    }

    private fun populateFormForEdit(log: TaskModel) {
        val state = _uiState.value
        val matchedMatter = state.matterList.find { it.matterid == log.Task_matter_id }
        val matchedTask = TasksModel().apply {
            displayValue = log.Task_name
            returnValue = log.Task_name
        }
        _uiState.update {
            it.copy(
                isFormVisible = true,
                isEditMode = true,
                editingLogId = log.taskid ?: "",
                selectedMatter = matchedMatter,
                selectedTask = matchedTask,
                selectedStatus = if (log.Task_billing == "billable") "Billable" else "Non-Billable",
                selectedDate = log.date ?: "",
                hours = log.hours ?: "",
                minutes = log.minutes ?: "",
                description = "",
                isBillable = log.Task_billing == "billable"
            )
        }
        matchedMatter?.let {
            fetchTasksForMatter(it)
        }
    }

    private fun deleteTimesheetLog(log: TaskModel) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val data = JSONObject().apply {
                put("id", log.taskid)
                put("timesheet_delete_scope", "DELETE_TIMESHEET_ONLY")
            }
            val result = repository.deleteTimesheet(data)
            _uiState.update { it.copy(isLoading = false) }
            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                val resObj = JSONObject(result.responseContent ?: "")
                _uiState.update { it.copy(toastMessage = resObj.optString("msg", "Timesheet deleted successfully")) }
                loadCurrentTabTimesheets()
            } else {
                _uiState.update { it.copy(alertTitle = "Error", alertMessage = result.responseContent) }
            }
        }
    }

    private fun submitWeeklyTimesheets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val dateStr = AndroidUtils.convertAnyDateToDDMMYYYY(_uiState.value.fromDateString) ?: ""
            val result = repository.submitTimesheets(dateStr)
            _uiState.update { it.copy(isLoading = false) }
            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                val resObj = JSONObject(result.responseContent ?: "")
                _uiState.update { it.copy(toastMessage = resObj.optString("msg", "Timesheets submitted successfully")) }
                loadCurrentTabTimesheets()
            } else {
                _uiState.update { it.copy(alertTitle = "Error", alertMessage = result.responseContent) }
            }
        }
    }

    private fun loadAggregatedTeamMembers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val dateStr = AndroidUtils.convertAnyDateToDDMMYYYY(_uiState.value.fromDateString) ?: ""
            val result = repository.fetchAggregatedTeamMembers(dateStr, _uiState.value.isWeek)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val root = JSONObject(result.responseContent ?: "")
                    val listArr = root.optJSONArray("timesheets") ?: JSONArray()
                    if (_uiState.value.isWeek == "month") {
                        val list = mutableListOf<Month_Model>()
                        for (i in 0 until listArr.length()) {
                            val jsonObject = listArr.getJSONObject(i)
                            list.add(Month_Model().apply {
                                name = jsonObject.optString("name", "")
                                billable_week_1 = jsonObject.optString("bw1", "0")
                                billable_week_2 = jsonObject.optString("bw2", "0")
                                billable_week_3 = jsonObject.optString("bw3", "0")
                                billable_week_4 = jsonObject.optString("bw4", "0")
                                billable_week_5 = jsonObject.optString("bw5", "0")
                                billable_week_tot = jsonObject.optString("tb", "0")
                                non_billable_week_1 = jsonObject.optString("nbw1", "0")
                                non_billable_week_2 = jsonObject.optString("nbw2", "0")
                                non_billable_week_3 = jsonObject.optString("nbw3", "0")
                                non_billable_week_4 = jsonObject.optString("nbw4", "0")
                                non_billable_week_5 = jsonObject.optString("nbw5", "0")
                                non_billable_week_tot = jsonObject.optString("tnb", "0")
                                Total = jsonObject.optString("total", "0")
                            })
                        }
                        _uiState.update { it.copy(monthlyTeamMembersList = list, teamMembersList = emptyList()) }
                    } else {
                        val list = mutableListOf<TMModel>()
                        for (i in 0 until listArr.length()) {
                            val jsonObject = listArr.getJSONObject(i)
                            list.add(TMModel().apply {
                                id = jsonObject.optString("id", "")
                                name = jsonObject.optString("name", "")
                                tb = jsonObject.optString("tb", "0")
                                tnb = jsonObject.optString("tnb", "0")
                                total = jsonObject.optString("total", "0")
                            })
                        }
                        _uiState.update { it.copy(teamMembersList = list, monthlyTeamMembersList = emptyList()) }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(alertTitle = "Parsing Exception", alertMessage = e.message) }
                }
            } else {
                _uiState.update {
                    it.copy(
                        alertTitle = "API Request Failed (Team Members)",
                        alertMessage = "Status: ${result.result}\nResponse: ${result.responseContent}"
                    )
                }
            }
        }
    }

    private fun loadAggregatedProjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val dateStr = AndroidUtils.convertAnyDateToDDMMYYYY(_uiState.value.fromDateString) ?: ""
            val result = repository.fetchAggregatedProjects(dateStr, _uiState.value.isWeek)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val root = JSONObject(result.responseContent ?: "")
                    if (!root.optBoolean("error", false)) {
                        val timesheetsObj = root.optJSONObject("timesheets") ?: JSONObject()
                        val grandTotal = timesheetsObj.optJSONObject("grandTotal") ?: JSONObject()
                        
                        val billableHrs = grandTotal.optString("billable", "0")
                        val nonBillableHrs = grandTotal.optString("nonbillable", "0")
                        val totHrs = grandTotal.optString("total", "0")

                        val dataArr = timesheetsObj.optJSONArray("data") ?: JSONArray()
                        val parsedProjects = mutableListOf<ProjectsModel>()
                        for (i in 0 until dataArr.length()) {
                            val jsonObject = dataArr.getJSONObject(i)
                            parsedProjects.add(ProjectsModel().apply {
                                caseNo = jsonObject.optString("caseNo", "")
                                projectName = jsonObject.optString("projectName", "")
                                clientNames = jsonObject.optJSONArray("clientNames")
                                matterId = jsonObject.optString("matterId", "")
                                teamMembers = jsonObject.optJSONArray("teamMembers")
                            })
                        }
                        
                        _uiState.update {
                            it.copy(
                                aggregatedProjectsList = parsedProjects,
                                aggregatedGrandTotalBillable = billableHrs,
                                aggregatedGrandTotalNonBillable = nonBillableHrs,
                                aggregatedGrandTotalTotal = totHrs
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                alertTitle = "API Error",
                                alertMessage = root.optString("msg", "Error loading projects")
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(alertTitle = "Parsing Exception", alertMessage = e.message) }
                }
            } else {
                _uiState.update {
                    it.copy(
                        alertTitle = "API Request Failed",
                        alertMessage = "Status: ${result.result}\nURL: matter/timesheets/project-all-...\nResponse: ${result.responseContent}"
                    )
                }
            }
        }
    }

    private fun clearFormFields() {
        _uiState.update {
            it.copy(
                isEditMode = false,
                editingLogId = "",
                selectedMatter = null,
                selectedTask = null,
                selectedStatus = "Billable",
                selectedDate = "",
                hours = "",
                minutes = "",
                description = "",
                taskList = emptyList()
            )
        }
    }

    private fun getMonthRange(monthCal: Calendar): Pair<String, String> {
        val format = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val tempCal = monthCal.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        val start = format.format(tempCal.time)
        tempCal.set(Calendar.DAY_OF_MONTH, tempCal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val end = format.format(tempCal.time)
        return Pair(start, end)
    }

    private fun getWeekDateRange(weekCal: Calendar): WeekDateInfo {
        val format = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val tempCal = weekCal.clone() as Calendar
        while (tempCal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            tempCal.add(Calendar.DATE, -1)
        }
        val start = format.format(tempCal.time)
        val weekDates = ArrayList<String>()
        for (i in 0..6) {
            val date = format.format(tempCal.time)
            weekDates.add(date)
            tempCal.add(Calendar.DATE, 1)
        }
        tempCal.add(Calendar.DATE, -1)
        val end = format.format(tempCal.time)
        return WeekDateInfo("$start - $end", weekDates)
    }
}
