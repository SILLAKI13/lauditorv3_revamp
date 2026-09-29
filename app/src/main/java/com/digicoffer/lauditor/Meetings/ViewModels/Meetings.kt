package com.digicoffer.lauditor.Meetings.ViewModels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.feature.meetings.data.repository.MeetingsRepository
import com.digicoffer.lauditor.feature.meetings.presentation.screen.MeetingsScreen
import com.digicoffer.lauditor.feature.meetings.presentation.state.MeetingsUiEvent
import com.digicoffer.lauditor.feature.meetings.presentation.viewmodel.MeetingsViewModel
import java.util.ArrayList

class Meetings : Fragment(), AsyncTaskCompleteListener, View.OnClickListener {

    private var mViewModel: NewModel? = null
    private lateinit var viewModel: MeetingsViewModel

    enum class FilterType {
        ALL,
        MY_MEETINGS,
        APPOINTMENTS
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
        setupOnBackPressed()
    }

    private fun setupOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (viewModel.uiState.value.isCreateMode) {
                        viewModel.onEvent(MeetingsUiEvent.CloseForm)
                    } else if (isEnabled) {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val title = getString(R.string.meetings)
        mViewModel?.setData(title)
        requireActivity().title = title

        val repository = MeetingsRepository(requireContext())
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MeetingsViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory).get(MeetingsViewModel::class.java)

        return ComposeView(requireContext()).apply {
            setContent {
                LauditorTheme {
                    MeetingsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    override fun onClick(view: View) {}
    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {}

    // Legacy helper methods retained for backward compatibility
    fun loadView() {
        if (::viewModel.isInitialized) {
            viewModel.onEvent(MeetingsUiEvent.CloseForm)
            viewModel.onEvent(MeetingsUiEvent.LoadInitialData)
        }
    }

    fun loadCreateEvent(event_details_list: ArrayList<Event_Details_DO>?) {
        if (::viewModel.isInitialized) {
            if (!event_details_list.isNullOrEmpty()) {
                viewModel.onEvent(MeetingsUiEvent.OpenEditEvent(event_details_list[0]))
            } else {
                viewModel.onEvent(MeetingsUiEvent.OpenCreateEvent)
            }
        }
    }
}
