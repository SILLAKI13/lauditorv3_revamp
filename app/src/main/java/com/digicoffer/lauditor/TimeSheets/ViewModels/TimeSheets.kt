package com.digicoffer.lauditor.TimeSheets.ViewModels

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.feature.timesheets.data.repository.TimesheetsRepository
import com.digicoffer.lauditor.feature.timesheets.presentation.screen.TimesheetsRoute
import com.digicoffer.lauditor.feature.timesheets.presentation.state.TimesheetsUiEvent
import com.digicoffer.lauditor.feature.timesheets.presentation.viewmodel.TimesheetsViewModel
import java.util.Calendar

class TimeSheets : Fragment() {
    private var mViewModel: NewModel? = null
    private lateinit var viewModel: TimesheetsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.timesheet, container, false)
        val repository = TimesheetsRepository(requireContext())
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TimesheetsViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory).get(TimesheetsViewModel::class.java)

        mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
        val isAggregated = Constants.Timesheet_Card == "Agts" && (Constants.ROLE == "SU" || Constants.ROLE == "GH")
        
        // Always properly configure the tabs based on the active navigation request
        if (isAggregated) {
            viewModel.onEvent(TimesheetsUiEvent.MainTabSelected("Aggregated"))
            viewModel.onEvent(TimesheetsUiEvent.SubTabSelected("TM"))
            mViewModel?.setData(getString(R.string.aggregated_timesheets))
        } else {
            viewModel.onEvent(TimesheetsUiEvent.MainTabSelected("MyTimeSheets"))
            val sub = if (Constants.is_ts_submitted) "Submitted" else "NS"
            viewModel.onEvent(TimesheetsUiEvent.SubTabSelected(sub))
            mViewModel?.setData(getString(if (sub == "Submitted") R.string.time_submit else R.string.time_sheet_entry))
        }
        Constants.ts_card_clicked = false

        val composeView = view.findViewById<ComposeView>(R.id.compose_parent_timesheets)
        val showAggregated = Constants.ROLE == "SU" || Constants.ROLE == "GH"

        composeView.setContent {
            val uiState by viewModel.uiState.collectAsState()
            LaunchedEffect(uiState.mainTab, uiState.subTab) {
                val title = if (uiState.mainTab == "Aggregated") {
                    getString(R.string.aggregated_timesheets)
                } else {
                    if (uiState.subTab == "Submitted" || uiState.subTab == "SU") {
                        getString(R.string.time_submit)
                    } else {
                        getString(R.string.time_sheet_entry)
                    }
                }
                mViewModel?.setData(title)
            }
            LauditorTheme {
                TimesheetsRoute(
                    viewModel = viewModel,
                    onDatePickerClick = { showDatePicker() },
                    showAggregatedTabs = showAggregated
                )
            }
        }

        return view
    }

    private fun showDatePicker() {
        val calendarWeek = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                viewModel.setDateFromDatePicker(year, month, dayOfMonth)
            },
            calendarWeek.get(Calendar.YEAR),
            calendarWeek.get(Calendar.MONTH),
            calendarWeek.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun Frozen_view() {
        viewModel.onEvent(TimesheetsUiEvent.MainTabSelected("MyTimeSheets"))
        viewModel.onEvent(TimesheetsUiEvent.SubTabSelected("NS"))
    }

    companion object {
        @JvmField
        var main_button_status: String = ""

        @JvmField
        var non_main_button_status: String = ""
    }
}
