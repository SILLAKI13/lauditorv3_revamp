package com.digicoffer.lauditor.Notifications

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
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NotificationCountApi
import com.digicoffer.lauditor.Notifications.Models.Navigation
import com.digicoffer.lauditor.Notifications.Models.NotificationsDo
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.feature.notifications.data.repository.NotificationsRepository
import com.digicoffer.lauditor.feature.notifications.presentation.screen.NotificationsRoute
import com.digicoffer.lauditor.feature.notifications.presentation.state.NotificationsUiEvent
import com.digicoffer.lauditor.feature.notifications.presentation.viewmodel.NotificationsViewModel
import org.json.JSONObject

class Notifications : Fragment(), AsyncTaskCompleteListener, View.OnClickListener, NotificationsAdapter.EventListener {

    // ViewModel and core components
    private lateinit var viewModel: NotificationsViewModel
    private var mViewModel: NewModel? = null

    // Legacy fields kept for backward compatibility (currently unused in Compose view)
    private var rv_notifications: androidx.recyclerview.widget.RecyclerView? = null
    private var tv_notification_count: android.widget.TextView? = null
    private var btn_delete_all: android.widget.ImageView? = null
    private var ib_read: android.widget.ImageView? = null
    private var chk_select_all: android.widget.CheckBox? = null
    private var et_Search: android.widget.EditText? = null
    private var layout_empty_state: android.widget.LinearLayout? = null
    private var adapter: NotificationsAdapter? = null
    private var progress_dialog: Dialog? = null
    private var selectedNotification: NotificationsDo? = null
    private var isAllSelected = false
    private val notificationList = ArrayList<NotificationsDo>()
    private var pendingHighlightIds = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Set layout title name inside Activity Model
        mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
        mViewModel?.setData("Notifications")

        val repository = NotificationsRepository(requireContext())
        val countApi = NotificationCountApi(requireContext())

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NotificationsViewModel(repository, countApi) as T
            }
        }
        viewModel = ViewModelProvider(this, factory).get(NotificationsViewModel::class.java)

        // Read and register pending highlight IDs from push navigation bundle
        val bundle = Constants.notificationBundle
        val highlightId = bundle.getString(Constants.NavKeys.APPOINTMENT_ID) ?: ""
        if (highlightId.isNotEmpty()) {
            viewModel.onEvent(NotificationsUiEvent.SetPendingHighlight(highlightId))
        }
        Constants.isFromNotification = false
        Constants.notificationBundle.clear()

        // Trigger initial API load
        viewModel.onEvent(NotificationsUiEvent.LoadNotifications)

        return ComposeView(requireContext()).apply {
            setContent {
                com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme {
                    NotificationsRoute(
                        viewModel = viewModel,
                        onNavigate = { nav ->
                            openNotificationNavigation(nav)
                        },
                        modifier = Modifier
                    )
                }
            }
        }
    }

    // Helper to open notification route navigation
    fun openNotificationNavigation(nav: Navigation?) {
        if (nav == null) {
            viewModel.onEvent(NotificationsUiEvent.LoadNotifications)
            return
        }
        AndroidUtils.setupNotificationHandler(requireContext(), nav)
    }

    // Legacy method interfaces retained for backward compatibility / reference
    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {}
    override fun onEvent(notification_id: String) {}
    override fun onSelectAllChanged(allSelected: Boolean) {}
    override fun onClick(view: View) {}

    fun readSingleNotification(id: String?, item: NotificationsDo) {}
    fun load_list(list: ArrayList<NotificationsDo>) {}
}
