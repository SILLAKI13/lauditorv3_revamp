package com.digicoffer.lauditor.feature.timesheets.presentation.state

import com.digicoffer.lauditor.TimeSheets.Models.ProjectsModel
import com.digicoffer.lauditor.TimeSheets.Models.ProjectTMModel
import com.digicoffer.lauditor.TimeSheets.Models.TSMatterModel
import com.digicoffer.lauditor.TimeSheets.Models.TaskModel
import com.digicoffer.lauditor.TimeSheets.Models.TasksModel

sealed interface TimesheetsUiEvent {
    // 1. Navigation/Tab events
    data class MainTabSelected(val tab: String) : TimesheetsUiEvent
    data class SubTabSelected(val tab: String) : TimesheetsUiEvent
    data class DateRangeTypeChanged(val type: String) : TimesheetsUiEvent
    object NextRange : TimesheetsUiEvent
    object PreviousRange : TimesheetsUiEvent
    data class DateBoundsChanged(val from: String, val to: String) : TimesheetsUiEvent
    data class SearchQueryChanged(val query: String) : TimesheetsUiEvent

    // 2. Fetch/Load actions
    object LoadCurrentTabTimesheets : TimesheetsUiEvent

    // 3. Form input change events
    data class ToggleFormVisibility(val visible: Boolean) : TimesheetsUiEvent
    data class MatterSelected(val matter: TSMatterModel?) : TimesheetsUiEvent
    data class TaskSelected(val task: TasksModel?) : TimesheetsUiEvent
    data class StatusSelected(val status: String) : TimesheetsUiEvent
    data class LogDateSelected(val date: String) : TimesheetsUiEvent
    data class HoursChanged(val hours: String) : TimesheetsUiEvent
    data class MinutesChanged(val minutes: String) : TimesheetsUiEvent
    data class DescriptionChanged(val desc: String) : TimesheetsUiEvent
    data class BillableChanged(val billable: Boolean) : TimesheetsUiEvent
    object ClearForm : TimesheetsUiEvent

    // 4. CRUD operations
    object SaveTimesheet : TimesheetsUiEvent
    data class EditTimesheet(val log: TaskModel) : TimesheetsUiEvent
    data class DeleteTimesheet(val log: TaskModel) : TimesheetsUiEvent
    object SubmitTimesheets : TimesheetsUiEvent

    // 5. Aggregated - Projects selection filters
    data class AggregatedProjectSelected(val project: ProjectsModel?) : TimesheetsUiEvent
    data class AggregatedProjectTMSelected(val tm: ProjectTMModel?) : TimesheetsUiEvent

    // 6. Feedback/Dialog dismiss
    object DismissDialogs : TimesheetsUiEvent
}
