package com.digicoffer.lauditor.Appointments.ViewModels

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.Appointments.Adapters.AppointmentsAdapter
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.Appointments.Models.PaymentModel
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.feature.appointments.data.repository.AppointmentsRepository
import com.digicoffer.lauditor.feature.appointments.presentation.screen.AppointmentsRoute
import com.digicoffer.lauditor.feature.appointments.presentation.state.AppointmentsUiEvent
import com.digicoffer.lauditor.feature.appointments.presentation.viewmodel.AppointmentsViewModel

class Appointments : Fragment(), AsyncTaskCompleteListener, View.OnClickListener, AppointmentsAdapter.InterfaceListener {

    private lateinit var viewModel: AppointmentsViewModel
    private var mViewModel: NewModel? = null

    // Legacy parameters kept for compilation mapping
    private var linear_notes: android.widget.LinearLayout? = null
    private var ll_nav_buttons: android.widget.LinearLayout? = null
    private var tl_search_matter: androidx.cardview.widget.CardView? = null
    private var rv_appointments_list: androidx.recyclerview.widget.RecyclerView? = null
    private var btn_send_request: android.widget.Button? = null
    private var progressDialog: Dialog? = null
    private var tl_search_appointments: View? = null
    private var btn_prev: androidx.appcompat.widget.AppCompatButton? = null
    private var btn_next: androidx.appcompat.widget.AppCompatButton? = null
    private var btn_search: androidx.appcompat.widget.AppCompatButton? = null
    private var et_search_appointments: com.google.android.material.textfield.TextInputEditText? = null
    private var ll_empty_state: android.widget.LinearLayout? = null
    private var ll_search_empty_state: android.widget.LinearLayout? = null
    private var selectedAppointment: AppointmentModel? = null
    private var pendingHighlightIds = ""
    private val masterAppointmentList = ArrayList<AppointmentModel>()
    private val filteredAppointmentList = ArrayList<AppointmentModel>()
    private val currentPageList = ArrayList<AppointmentModel>()
    private var currentPage = 0
    private var totalPages = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mViewModel?.setData(getString(R.string.appointments))
        requireActivity().title = getString(R.string.appointments)

        val repository = AppointmentsRepository(requireContext())
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppointmentsViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory).get(AppointmentsViewModel::class.java)

        // Pre-load notification high-priority highlights from navigation bundle
        val bundle = Constants.notificationBundle
        if (bundle != null) {
            val highlightId = bundle.getString(Constants.NavKeys.APPOINTMENT_ID) ?: ""
            if (highlightId.isNotEmpty()) {
                viewModel.onEvent(AppointmentsUiEvent.SetPendingHighlight(highlightId))
            }
            Constants.isFromNotification = false
            Constants.notificationBundle.clear()
        }

        // Trigger load
        viewModel.onEvent(AppointmentsUiEvent.LoadAppointments)

        return ComposeView(requireContext()).apply {
            setContent {
                com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme {
                    AppointmentsRoute(
                        viewModel = viewModel,
                        onChatClick = { appointmentModel ->
                            Constants.isClient_chat = true
                            Constants.pendingChatJid = appointmentModel.guid
                            Constants.pendingChatName = appointmentModel.client_name
                            Constants.pendingChatSource = "appointment"
                            Constants.mainActivity?.navigation_items(com.digicoffer.lauditor.Chat.ViewModels.Chat())
                        },
                        onVideoCallClick = { appointmentModel ->
                            val parts = AndroidUtils.extractDateTimeParts(
                                appointmentModel.appointment_from,
                                appointmentModel.appointment_to
                            )
                            if (parts != null) {
                                val date = parts[0]
                                val fromTime = parts[1]
                                val toTime = parts[2]
                                val url = AndroidUtils.getAVChatUrl(
                                    appointmentModel.meeting_room_id,
                                    fromTime,
                                    toTime,
                                    date,
                                    appointmentModel.client_name
                                )
                                AndroidUtils.loadAVChatView(requireContext(), requireActivity(), url)
                            }
                        },
                        modifier = Modifier
                    )
                }
            }
        }
    }

    // Stubs for AppointmentsAdapter.InterfaceListener to ensure adapter compiles cleanly
    override fun ViewAppointmentHistory(
        appointmentModel: AppointmentModel,
        itemsArrayList: ArrayList<AppointmentModel>
    ) {}

    override fun CancelAppointment(appointmentModel: AppointmentModel) {}
    override fun DeleteAppointment(appointmentModel: AppointmentModel) {}

    // Legacy method interfaces
    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {}
    override fun onClick(view: View) {}
}
