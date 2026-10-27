package com.digicoffer.lauditor.feature.dashboard.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.Dashboard.DahboardModels.CardDataModels
import com.digicoffer.lauditor.Notifications.Models.Navigation
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.dialogs.AppDialog
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.feature.dashboard.data.model.DashboardCardItemModel
import com.digicoffer.lauditor.feature.dashboard.data.model.DashboardSectionModel
import com.digicoffer.lauditor.feature.dashboard.presentation.components.AppointmentCard
import com.digicoffer.lauditor.feature.dashboard.presentation.components.AppointmentTrendCard
import com.digicoffer.lauditor.feature.dashboard.presentation.components.ApproxRevenueCard
import com.digicoffer.lauditor.feature.dashboard.presentation.components.BillableHoursCard
import com.digicoffer.lauditor.feature.dashboard.presentation.components.DashboardDateHeader
import com.digicoffer.lauditor.feature.dashboard.presentation.components.DashboardSectionHeader
import com.digicoffer.lauditor.feature.dashboard.presentation.components.DataStorageCard
import com.digicoffer.lauditor.feature.dashboard.presentation.components.HiringKpiCard
import com.digicoffer.lauditor.feature.dashboard.presentation.components.MatterStatusCard
import com.digicoffer.lauditor.feature.dashboard.presentation.components.MeetingCard
import com.digicoffer.lauditor.feature.dashboard.presentation.components.MessagesCard
import com.digicoffer.lauditor.feature.dashboard.presentation.components.NotificationCard
import com.digicoffer.lauditor.feature.dashboard.presentation.components.RevenueTrendCard
import com.digicoffer.lauditor.feature.dashboard.presentation.components.SubscriptionCard
import com.digicoffer.lauditor.feature.dashboard.presentation.state.DashboardUiEvent
import com.digicoffer.lauditor.feature.dashboard.presentation.state.DashboardUiState
import com.digicoffer.lauditor.feature.dashboard.presentation.viewmodel.DashboardViewModel

private val GillSansRegular = FontFamily(Font(R.font.gill_sans_regular))

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel,
    onNotificationNavigation: (Navigation) -> Unit,
    onCardNavigation: (cardType: Int, navigation: Navigation?, subType: String?) -> Unit,
    onPaySubscription: (planLabel: String?, validityText: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.pendingNavigation) {
        val nav = uiState.pendingNavigation
        if (nav != null) {
            onNotificationNavigation(nav)
            viewModel.onEvent(DashboardUiEvent.NavigationHandled)
        }
    }

    LaunchedEffect(uiState.pendingCardTypeNavigation) {
        val nav = uiState.pendingCardTypeNavigation
        if (nav != null) {
            onCardNavigation(nav.cardType, nav.navigation, nav.subType)
            viewModel.onEvent(DashboardUiEvent.NavigationHandled)
        }
    }

    LaunchedEffect(uiState.pendingSubscriptionPay) {
        val sub = uiState.pendingSubscriptionPay
        if (sub != null) {
            onPaySubscription(sub.planLabel, sub.validityText)
            viewModel.onEvent(DashboardUiEvent.NavigationHandled)
        }
    }

    DashboardScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE4F2FF))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Date Header
            DashboardDateHeader(dateInfo = uiState.currentDateInfo)

            // Outer Vertical Sections List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                items(uiState.sections) { section ->
                    DashboardSectionView(
                        section = section,
                        uiState = uiState,
                        onEvent = onEvent
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }

        // Loader Overlay
        if (uiState.isLoading) {
            AppLoader()
        }

        // Alert Dialog
        if (!uiState.alertMessage.isNullOrEmpty()) {
            AppDialog(
                title = if (uiState.alertTitle.isNullOrBlank()) "Alert" else uiState.alertTitle!!,
                onConfirm = { onEvent(DashboardUiEvent.DismissAlert) },
                onDismiss = { onEvent(DashboardUiEvent.DismissAlert) }
            ) {
                Text(
                    text = uiState.alertMessage!!,
                    fontFamily = GillSansRegular,
                    fontSize = 15.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun DashboardSectionView(
    section: DashboardSectionModel,
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        // Section Title & Badge
        DashboardSectionHeader(
            title = section.title,
            count = section.cardCount
        )

        // Horizontal Card Carousel
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 10.dp)
        ) {
            items(section.items) { item ->
                DashboardCardItem(
                    cardItem = item,
                    uiState = uiState,
                    onEvent = onEvent
                )
            }
        }
    }
}

@Composable
private fun DashboardCardItem(
    cardItem: DashboardCardItemModel,
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit
) {
    when (cardItem.type) {
        DashboardCardItemModel.TYPE_MEETING -> {
            MeetingCard(
                data = cardItem.data as CardDataModels.MeetingCardData,
                onClick = {
                    onEvent(
                        DashboardUiEvent.OnCardClick(
                            cardType = cardItem.type,
                            navigation = cardItem.navigation
                        )
                    )
                }
            )
        }
        DashboardCardItemModel.TYPE_APPOINTMENT -> {
            AppointmentCard(
                data = cardItem.data as CardDataModels.AppointmentCardData,
                onClick = {
                    onEvent(
                        DashboardUiEvent.OnCardClick(
                            cardType = cardItem.type,
                            navigation = cardItem.navigation
                        )
                    )
                }
            )
        }
        DashboardCardItemModel.TYPE_MESSAGES -> {
            MessagesCard(
                data = cardItem.data as CardDataModels.MessagesCardData,
                clientCount = uiState.clientChatCount,
                teamCount = uiState.teamChatCount,
                userRole = uiState.userRole,
                userCategory = uiState.userCategory,
                onCardClick = {
                    onEvent(
                        DashboardUiEvent.OnCardClick(
                            cardType = cardItem.type,
                            navigation = cardItem.navigation
                        )
                    )
                },
                onClientClick = {
                    onEvent(DashboardUiEvent.SetClientChatActive(true))
                    onEvent(
                        DashboardUiEvent.OnCardClick(
                            cardType = cardItem.type,
                            navigation = cardItem.navigation
                        )
                    )
                },
                onTeamClick = {
                    onEvent(DashboardUiEvent.SetClientChatActive(false))
                    onEvent(
                        DashboardUiEvent.OnCardClick(
                            cardType = cardItem.type,
                            navigation = cardItem.navigation
                        )
                    )
                }
            )
        }
        DashboardCardItemModel.TYPE_NOTIFICATION -> {
            NotificationCard(
                data = cardItem.data as CardDataModels.NotificationCardData,
                onClick = {
                    onEvent(
                        DashboardUiEvent.OnCardClick(
                            cardType = cardItem.type,
                            navigation = cardItem.navigation
                        )
                    )
                }
            )
        }
        DashboardCardItemModel.TYPE_REVENUE_TREND,
        DashboardCardItemModel.TYPE_APPOINTMENT_REVENUE_TREND -> {
            RevenueTrendCard(
                data = cardItem.data as CardDataModels.TrendCardData,
                onClick = {
                    onEvent(
                        DashboardUiEvent.OnCardClick(
                            cardType = cardItem.type,
                            navigation = cardItem.navigation
                        )
                    )
                }
            )
        }
        DashboardCardItemModel.TYPE_APPOINTMENT_TREND -> {
            AppointmentTrendCard(
                data = cardItem.data as CardDataModels.TrendCardData,
                onClick = {
                    onEvent(
                        DashboardUiEvent.OnCardClick(
                            cardType = cardItem.type,
                            navigation = cardItem.navigation
                        )
                    )
                }
            )
        }
        DashboardCardItemModel.TYPE_MATTER -> {
            MatterStatusCard(
                data = cardItem.data as CardDataModels.MatterCardData,
                onNavigateWithSubType = { subType ->
                    onEvent(
                        DashboardUiEvent.OnCardClick(
                            cardType = cardItem.type,
                            navigation = cardItem.navigation,
                            subType = subType
                        )
                    )
                }
            )
        }
        DashboardCardItemModel.TYPE_STORAGE -> {
            DataStorageCard(
                data = cardItem.data as CardDataModels.StorageCardData,
                onClick = {
                    onEvent(
                        DashboardUiEvent.OnCardClick(
                            cardType = cardItem.type,
                            navigation = cardItem.navigation
                        )
                    )
                }
            )
        }
        DashboardCardItemModel.TYPE_BILLABLE -> {
            BillableHoursCard(
                data = cardItem.data as CardDataModels.BillableCardData,
                onClick = {
                    onEvent(
                        DashboardUiEvent.OnCardClick(
                            cardType = cardItem.type,
                            navigation = cardItem.navigation
                        )
                    )
                }
            )
        }
        DashboardCardItemModel.TYPE_APPROX_REVENUE -> {
            ApproxRevenueCard(
                data = cardItem.data as CardDataModels.ApproxRevenueCardData,
                onClick = {
                    onEvent(
                        DashboardUiEvent.OnCardClick(
                            cardType = cardItem.type,
                            navigation = cardItem.navigation
                        )
                    )
                }
            )
        }
        DashboardCardItemModel.TYPE_SUBSCRIPTION -> {
            SubscriptionCard(
                data = cardItem.data as CardDataModels.SubscriptionCardData,
                onPayNow = {
                    onEvent(
                        DashboardUiEvent.OnPaySubscription(
                            planLabel = (cardItem.data as CardDataModels.SubscriptionCardData).planLabel,
                            validityText = (cardItem.data as CardDataModels.SubscriptionCardData).validTill
                        )
                    )
                },
                onClick = {
                    onEvent(
                        DashboardUiEvent.OnCardClick(
                            cardType = cardItem.type,
                            navigation = cardItem.navigation
                        )
                    )
                }
            )
        }
        DashboardCardItemModel.TYPE_HIRING -> {
            HiringKpiCard(
                data = cardItem.data as CardDataModels.HiringCardData,
                onClick = {
                    onEvent(
                        DashboardUiEvent.OnCardClick(
                            cardType = cardItem.type,
                            navigation = cardItem.navigation
                        )
                    )
                }
            )
        }
    }
}
