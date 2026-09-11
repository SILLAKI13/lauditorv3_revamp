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
        val isAggregated = (Constants.Timesheet_Card == "Agts" && (Constants.ROLE == "SU" || Constants.ROLE == "GH")) || viewModel.uiState.value.mainTab == "Aggregated"
        mViewModel?.setData(getString(if (isAggregated) R.string.aggregated_timesheets else R.string.time_sheets))

        // Check if navigated from navigation bundle / dashboard card click
        if (Constants.ts_card_clicked) {
            if (Constants.Timesheet_Card == "Agts") {
                if (Constants.ROLE == "SU" || Constants.ROLE == "GH") {
                    viewModel.onEvent(TimesheetsUiEvent.MainTabSelected("Aggregated"))
                    viewModel.onEvent(TimesheetsUiEvent.SubTabSelected("TM"))
                } else {
                    viewModel.onEvent(TimesheetsUiEvent.MainTabSelected("MyTimeSheets"))
                    viewModel.onEvent(TimesheetsUiEvent.SubTabSelected("NS"))
                }
            } else {
                viewModel.onEvent(TimesheetsUiEvent.MainTabSelected("MyTimeSheets"))
                val sub = if (Constants.is_ts_submitted) "Submitted" else "NS"
                viewModel.onEvent(TimesheetsUiEvent.SubTabSelected(sub))
            }
            Constants.ts_card_clicked = false
        } else {
            // Fetch initial data
            viewModel.onEvent(TimesheetsUiEvent.LoadCurrentTabTimesheets)
        }

        val composeView = view.findViewById<ComposeView>(R.id.compose_parent_timesheets)
        val showAggregated = Constants.ROLE == "SU" || Constants.ROLE == "GH"

        composeView.setContent {
            val uiState by viewModel.uiState.collectAsState()
            LaunchedEffect(uiState.mainTab) {
                val isAggregatedTab = uiState.mainTab == "Aggregated"
                mViewModel?.setData(getString(if (isAggregatedTab) R.string.aggregated_timesheets else R.string.time_sheets))
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
