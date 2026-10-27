package com.digicoffer.lauditor.Dashboard.NewRevampViewModels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.Appointments.ViewModels.Appointments
import com.digicoffer.lauditor.Chat.ViewModels.Chat
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.Groups.Groups
import com.digicoffer.lauditor.Matter.ViewModels.Matter
import com.digicoffer.lauditor.Meetings.ViewModels.Meetings
import com.digicoffer.lauditor.Notifications.Models.Navigation
import com.digicoffer.lauditor.Notifications.Notifications
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.TimeSheets.ViewModels.TimeSheets
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.feature.dashboard.data.repository.DashboardRepository
import com.digicoffer.lauditor.feature.dashboard.presentation.screen.DashboardRoute
import com.digicoffer.lauditor.feature.dashboard.presentation.state.DashboardUiEvent
import com.digicoffer.lauditor.feature.dashboard.presentation.viewmodel.DashboardViewModel

class Dashboard : Fragment(), AsyncTaskCompleteListener, View.OnClickListener {

    companion object {
        private const val TAG = "DashboardFragment"
        private const val AUTH_REQUEST_CODE = 1001
    }

    private lateinit var viewModel: DashboardViewModel
    private var mViewModel: NewModel? = null
    private var menuHighlightListener: MenuHighlightListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
        mViewModel?.setData(getString(R.string.lauditor))

        val repository = DashboardRepository(requireContext())
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory).get(DashboardViewModel::class.java)

        Constants.dashboard_en = this

        viewModel.onEvent(DashboardUiEvent.LoadDashboard(isRefresh = false))

        return ComposeView(requireContext()).apply {
            setContent {
                LauditorTheme {
                    DashboardRoute(
                        viewModel = viewModel,
                        onNotificationNavigation = { nav ->
                            Constants.isFromNotification = true
                            AndroidUtils.setupNotificationHandler(requireContext(), nav)
                        },
                        onCardNavigation = { cardType, nav, subType ->
                            handleNavigation(cardType, nav, subType)
                        },
                        onPaySubscription = { planLabel, validityText ->
                            launchPaySubscriptionPage(planLabel, validityText)
                        },
                        modifier = Modifier
                    )
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MenuHighlightListener) {
            menuHighlightListener = context
        }
    }

    private fun handleNavigation(
        cardType: Int,
        navigation: Navigation?,
        subType: String?
    ) {
        try {
            menuHighlightListener?.highlightMenuForCardType(cardType)

            if (navigation != null && !navigation.route_name.isNullOrEmpty()) {
                Constants.isFromNotification = true
                AndroidUtils.setupNotificationHandler(requireContext(), navigation)
                return
            }

            when (cardType) {
                DashboardItem.TYPE_MATTER -> {
                    Constants.is_CreateMatter = false
                    Constants.isCreate = false
                    Constants.matterFilterType = subType ?: ""
                    Page_Navigation(Matter())
                }
                DashboardItem.TYPE_BILLABLE,
                DashboardItem.TYPE_APPROX_REVENUE -> Page_Navigation(TimeSheets())
                DashboardItem.TYPE_HIRING -> Page_Navigation(Groups())
                DashboardItem.TYPE_NOTIFICATION -> Page_Navigation(Notifications())
                DashboardItem.TYPE_MESSAGES -> Page_Navigation(Chat())
                DashboardItem.TYPE_MEETING -> Page_Navigation(Meetings())
                DashboardItem.TYPE_APPOINTMENT -> Page_Navigation(Appointments())
                DashboardItem.TYPE_SUBSCRIPTION -> {
                    launchPaySubscriptionPage(null, null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleNavigation error", e)
        }
    }

    fun refreshChatCounts() {
        if (::viewModel.isInitialized) {
            viewModel.onEvent(DashboardUiEvent.RefreshChatCounts)
        }
    }

    fun Page_Navigation(fragment: Fragment) {
        Constants.mainActivity?.navigation_items(fragment)
    }

    fun launchPaySubscriptionPage(email: String?, userCount: String?) {
        try {
            val userEmail = email ?: ""
            val count = userCount ?: ""
            startActivityForResult(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://staging.payment.digicoffer.com/renew?useremail=$userEmail&users=$count")
                ),
                AUTH_REQUEST_CODE
            )
        } catch (e: Exception) {
            Log.e(TAG, "launchPaySubscriptionPage error", e)
        }
    }

    interface MenuHighlightListener {
        fun highlightMenuForCardType(cardType: Int)
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) { /* handled in repository */ }
    override fun onClick(view: View) { /* no-op */ }
}
