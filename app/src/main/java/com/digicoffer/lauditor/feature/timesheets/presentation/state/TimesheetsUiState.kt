package com.digicoffer.lauditor.feature.timesheets.presentation.state

import com.digicoffer.lauditor.TimeSheets.Models.EventsModel
import com.digicoffer.lauditor.TimeSheets.Models.Month_Model
import com.digicoffer.lauditor.TimeSheets.Models.ProjectTMModel
import com.digicoffer.lauditor.TimeSheets.Models.ProjectsModel
import com.digicoffer.lauditor.TimeSheets.Models.TMModel
import com.digicoffer.lauditor.TimeSheets.Models.TSMatterModel
import com.digicoffer.lauditor.TimeSheets.Models.TaskModel
import com.digicoffer.lauditor.TimeSheets.Models.TasksModel
import com.digicoffer.lauditor.TimeSheets.Models.WeekDateInfo
import com.digicoffer.lauditor.TimeSheets.Models.WeekTotalModel

data class TimesheetsUiState(
    // 1. Parent Shell states
    val mainTab: String = "MyTimeSheets", // "MyTimeSheets" or "Aggregated"
    val subTab: String = "NS",           // "NS", "SU", "TM", "Project"
    val isWeek: String = "week",          // "week" or "month"
    val fromDateString: String = "",
    val toDateString: String = "",
    val weekDateInfo: WeekDateInfo? = null,
    val searchQuery: String = "",

    // 2. Loading and feedback states
    val isLoading: Boolean = false,
    val alertTitle: String? = null,
    val alertMessage: String? = null,
    val toastMessage: String? = null,

    // 3. My Timesheets - General lists and totals
    val isFrozen: Boolean = false,
    val weekTotalString: String = "",
    val weekTotals: WeekTotalModel? = null,
    val timesheetsList: List<TaskModel> = emptyList(), // non-submitted/submitted logs
    val matterList: List<TSMatterModel> = emptyList(), // Project/Matter spinner
    val activeProjectsList: List<TSMatterModel> = emptyList(), // Active Projects spinner

    // 4. Form inputs for add/edit log
    val isFormVisible: Boolean = false,
    val selectedMatter: TSMatterModel? = null,
    val selectedTask: TasksModel? = null,
    val selectedStatus: String = "Billable",
    val selectedDate: String = "",
    val hours: String = "",
    val minutes: String = "",
    val description: String = "",
    val isBillable: Boolean = true,
    val isEditMode: Boolean = false,
    val editingLogId: String = "",
    val taskList: List<TasksModel> = emptyList(), // fetched dynamically based on selected matter

    // 5. Aggregated - Team Members list
    val teamMembersList: List<TMModel> = emptyList(),
    val monthlyTeamMembersList: List<Month_Model> = emptyList(),

    // 6. Aggregated - Projects list
    val aggregatedProjectsList: List<ProjectsModel> = emptyList(),
    val aggregatedGrandTotalBillable: String = "0",
    val aggregatedGrandTotalNonBillable: String = "0",
    val aggregatedGrandTotalTotal: String = "0",
    
    // Project selected filters in aggregated view
    val selectedProject: ProjectsModel? = null,
    val selectedProjectTeamMember: ProjectTMModel? = null
)
