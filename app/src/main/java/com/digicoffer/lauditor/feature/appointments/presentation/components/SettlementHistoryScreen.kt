package com.digicoffer.lauditor.feature.appointments.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.animation.fallDownItem
import com.digicoffer.lauditor.core.ui.common.badges.AppStatusBadge
import com.digicoffer.lauditor.core.ui.common.badges.AppStatusStyle
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.core.ui.common.foundation.AppProfileAvatar
import com.digicoffer.lauditor.core.ui.common.search.AppSearchField
import com.digicoffer.lauditor.feature.appointments.presentation.state.AppointmentsUiEvent
import com.digicoffer.lauditor.feature.appointments.presentation.state.AppointmentsUiState
import java.text.DecimalFormat
import java.util.Locale

@Composable
fun SettlementHistoryScreen(
    uiState: AppointmentsUiState,
    onEvent: (AppointmentsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var searchInput by remember(uiState.settlementSearchQuery) {
        mutableStateOf(uiState.settlementSearchQuery)
    }

    // Summary calculations
    val currencySymbol = uiState.rawSettlementList.firstOrNull { it.payment?.symbol?.isNotEmpty() == true }?.payment?.symbol ?: "₹"
    val decimalFormat = remember { DecimalFormat("#,##0.00") }

    val calculatedRefundedAmount = uiState.rawSettlementList.filter {
        val cleanStatus = it.appointment_status.lowercase(Locale.ROOT).trim()
        val rawPaymentStatus = it.payment?.status?.lowercase(Locale.ROOT)?.trim() ?: ""
        cleanStatus == "cancelled" || cleanStatus == "canceled" || rawPaymentStatus == "refunded"
    }.sumOf {
        it.payment?.amount_paid?.toDoubleOrNull() ?: 0.0
    }
    val totalRefundedAmount = uiState.settlementApiTotalRefunded ?: calculatedRefundedAmount

    val calculatedRefundInitiatedAmount = uiState.rawSettlementList.filter {
        val rawPaymentStatus = it.payment?.status?.lowercase(Locale.ROOT)?.trim() ?: ""
        rawPaymentStatus == "refund_initiated" || rawPaymentStatus == "refund initiated"
    }.sumOf {
        it.payment?.amount_paid?.toDoubleOrNull() ?: 0.0
    }
    val totalRefundInitiatedAmount = uiState.settlementApiTotalRefundInitiated ?: calculatedRefundInitiatedAmount

    val calculatedPaidAmount = uiState.rawSettlementList.filter {
        val cleanStatus = it.appointment_status.lowercase(Locale.ROOT).trim()
        val rawPaymentStatus = it.payment?.status?.lowercase(Locale.ROOT)?.trim() ?: ""
        val isCancelled = cleanStatus == "cancelled" || cleanStatus == "canceled"
        val isRefunded = isCancelled || rawPaymentStatus == "refunded"
        val isRefundInitiated = rawPaymentStatus == "refund_initiated" || rawPaymentStatus == "refund initiated"
        val isPending = rawPaymentStatus == "pending" || rawPaymentStatus == "payment_pending" || rawPaymentStatus == "payment pending" || cleanStatus == "payment_pending" || cleanStatus == "payment pending"
        !isRefunded && !isRefundInitiated && !isPending && (rawPaymentStatus == "paid" || (it.payment?.amount_paid?.toDoubleOrNull() ?: 0.0) > 0.0)
    }.sumOf {
        it.payment?.amount_paid?.toDoubleOrNull() ?: 0.0
    }
    val totalPaidAmount = uiState.settlementApiTotalPaid ?: calculatedPaidAmount

    val totalTransactionsCount = uiState.settlementApiTotalTransactions ?: uiState.rawSettlementList.size

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE4F2FF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header Row: Back button + "Settlement History - " + Client Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.back_arrow),
                    contentDescription = "Close Settlement History",
                    colorFilter = ColorFilter.tint(Color(0xFF004D87)),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onEvent(AppointmentsUiEvent.CloseSettlementHistory) }
                        .padding(6.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Settlement History - ",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF004D87),
                    fontFamily = FontFamily(Font(R.font.gill_sans_bold))
                )

                AppProfileAvatar(
                    imageUrl = uiState.settlementClientProfilePic,
                    name = uiState.settlementClientName,
                    size = 28.dp
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = uiState.settlementClientName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF004D87),
                    fontFamily = FontFamily(Font(R.font.gill_sans_bold)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Summary 2x2 Grid Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettlementSummaryCard(
                        title = "Total Paid",
                        value = "$currencySymbol ${decimalFormat.format(totalPaidAmount)}",
                        iconRes = R.drawable.ppaid,
                        modifier = Modifier.weight(1f)
                    )
                    SettlementSummaryCard(
                        title = "Total Transaction",
                        value = totalTransactionsCount.toString(),
                        iconRes = R.drawable.ptotal,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettlementSummaryCard(
                        title = "Refunded",
                        value = "$currencySymbol ${decimalFormat.format(totalRefundedAmount)}",
                        iconRes = R.drawable.prefund,
                        modifier = Modifier.weight(1f)
                    )
                    SettlementSummaryCard(
                        title = "Refund Initiated",
                        value = "$currencySymbol ${decimalFormat.format(totalRefundInitiatedAmount)}",
                        iconRes = R.drawable.prefundinitiated,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search Bar (Real-time search filtering)
            AppSearchField(
                value = searchInput,
                onValueChange = {
                    searchInput = it
                    onEvent(AppointmentsUiEvent.SettlementSearchQuerySubmitted(it))
                },
                placeholder = "Search by status or payment method",
                onClearClick = {
                    searchInput = ""
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onEvent(AppointmentsUiEvent.SettlementSearchQuerySubmitted(""))
                },
                onSearchKeyboardAction = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onEvent(AppointmentsUiEvent.SettlementSearchQuerySubmitted(searchInput))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            // Heading: Settlement History (<count>)
            Text(
                text = "Settlement History (${uiState.filteredSettlementList.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004D87),
                fontFamily = FontFamily(Font(R.font.gill_sans_bold)),
                modifier = Modifier.padding(vertical = 6.dp)
            )

            // Content: Empty State vs Cards List
            if (uiState.filteredSettlementList.isEmpty() && !uiState.isSettlementLoading) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.empty_appointments),
                        contentDescription = "No settlement history",
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Settlement History Found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF004D87),
                        fontFamily = FontFamily(Font(R.font.gill_sans_bold)),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    itemsIndexed(
                        items = uiState.currentSettlementPageList,
                        key = { _, item -> item.id }
                    ) { index, item ->
                        SettlementHistoryCard(
                            appointment = item,
                            modifier = Modifier.fallDownItem(
                                index = index,
                                triggerKey = uiState.settlementCurrentPage
                            )
                        )
                    }
                }

                // Bottom Pagination
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    val isPrevEnabled = uiState.settlementCurrentPage > 0
                    val isNextEnabled = uiState.settlementCurrentPage < uiState.settlementTotalPages - 1

                    Button(
                        onClick = { onEvent(AppointmentsUiEvent.SettlementPagePrev) },
                        enabled = isPrevEnabled,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF004D87),
                            disabledContainerColor = Color(0xFF004D87)
                        ),
                        modifier = Modifier
                            .height(38.dp)
                            .alpha(if (isPrevEnabled) 1.0f else 0.5f)
                    ) {
                        Text(
                            text = stringResource(id = R.string.prev_),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.gill_sans))
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = { onEvent(AppointmentsUiEvent.SettlementPageNext) },
                        enabled = isNextEnabled,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF004D87),
                            disabledContainerColor = Color(0xFF004D87)
                        ),
                        modifier = Modifier
                            .height(38.dp)
                            .alpha(if (isNextEnabled) 1.0f else 0.5f)
                    ) {
                        Text(
                            text = stringResource(id = R.string.next_),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.gill_sans))
                        )
                    }
                }
            }
        }

        if (uiState.isSettlementLoading) {
            AppLoader()
        }
    }
}

@Composable
private fun SettlementSummaryCard(
    title: String,
    value: String,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.height(72.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = Color(0xFF757575),
                    fontFamily = FontFamily(Font(R.font.gill_sans)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF004D87),
                    fontFamily = FontFamily(Font(R.font.gill_sans_bold)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SettlementHistoryCard(
    appointment: AppointmentModel,
    modifier: Modifier = Modifier
) {
    var showHint by remember { mutableStateOf(false) }

    val payment = appointment.payment
    val symbol = payment?.symbol?.ifEmpty { "₹" } ?: "₹"
    val amount = payment?.amount_paid ?: "0"
    val rawPaymentStatus = payment?.status?.lowercase(Locale.ROOT)?.trim() ?: ""
    val consultationMode = appointment.consultation_mode.ifEmpty { "Online" }
    val formattedDateTime = formatAppointmentDateTime(
        appointment.appointment_from,
        appointment.appointment_to
    )

    val cleanAppointmentStatus = appointment.appointment_status.lowercase(Locale.ROOT).trim()
    val isCancelled = cleanAppointmentStatus == "cancelled" || cleanAppointmentStatus == "canceled"
    val isRefunded = isCancelled || rawPaymentStatus == "refunded"
    val isRefundInitiated = rawPaymentStatus == "refund_initiated" || rawPaymentStatus == "refund initiated"

    val (displayStatus, statusStyle) = when {
        isRefunded -> Pair("Refunded", AppStatusStyle.ERROR)
        isRefundInitiated -> Pair("Refund Initiated", AppStatusStyle.WARNING)
        rawPaymentStatus == "paid" -> Pair("Paid", AppStatusStyle.SUCCESS)
        rawPaymentStatus == "pending" || rawPaymentStatus == "payment_pending" || rawPaymentStatus == "payment pending" -> Pair("Pending", AppStatusStyle.WARNING)
        else -> Pair(
            if (rawPaymentStatus.isNotEmpty()) rawPaymentStatus.replaceFirstChar { it.uppercase() } else "Paid",
            AppStatusStyle.SUCCESS
        )
    }

    val hintText = when {
        isRefunded -> "Invoice unavailable: This appointment was cancelled and the payment has been refunded"
        isRefundInitiated -> "Invoice unavailable: This appointment was cancelled and the refund process has been initiated"
        else -> null
    }

    // Keep hint logic intact, hidden for now as requested
    val isHintVisible = false

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Row 1: Client Name + (i) Info Hint Icon (if applicable) + Payment Status Chip
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = appointment.client_name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF004D87),
                    fontFamily = FontFamily(Font(R.font.gill_sans_bold)),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Info (i) Hint Icon & Tooltip for Refunded / Refund Initiated (hidden for now)
                    if (isHintVisible && hintText != null) {
                        Box {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(20.dp)
                                    .border(1.dp, Color(0xFF9E9E9E), CircleShape)
                                    .clickable { showHint = !showHint }
                            ) {
                                Text(
                                    text = "i",
                                    color = Color(0xFF757575),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily(Font(R.font.gill_sans_bold)),
                                    textAlign = TextAlign.Center
                                )
                            }

                            if (showHint) {
                                Popup(
                                    alignment = Alignment.TopEnd,
                                    offset = IntOffset(x = 0, y = 30),
                                    onDismissRequest = { showHint = false },
                                    properties = PopupProperties(
                                        focusable = true,
                                        dismissOnBackPress = true,
                                        dismissOnClickOutside = true
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .background(Color(0xFF2C2C2C), RoundedCornerShape(8.dp))
                                            .clickable { showHint = false }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = hintText,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily(Font(R.font.gill_sans)),
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Payment Status Badge (Reusing AppStatusBadge component)
                    AppStatusBadge(
                        text = displayStatus,
                        style = statusStyle
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Appointment Date / Time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img),
                    contentDescription = "Date-Time",
                    colorFilter = ColorFilter.tint(Color(0xFF757575)),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formattedDateTime,
                    fontSize = 13.sp,
                    color = Color(0xFF424242),
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 3: Amount Paid & Payment Method
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val displayAmount = if (isCancelled) {
                    val amt = if (amount.isNotEmpty() && amount != "0") amount else ""
                    if (amt.isNotEmpty()) "Refunded - $symbol$amt" else "Refunded"
                } else {
                    payment?.label?.takeIf { it.isNotBlank() } ?: "Amount: $symbol$amount"
                }
                Text(
                    text = displayAmount,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCancelled) Color(0xFFFF9800) else Color(0xFF2E7D32),
                    fontFamily = FontFamily(Font(R.font.gill_sans_bold))
                )

                val rawMethod = payment?.method?.trim() ?: ""
                val paymentMethod = when {
                    rawMethod.equals("upi", ignoreCase = true) -> "UPI"
                    rawMethod.equals("netbanking", ignoreCase = true) -> "Netbanking"
                    rawMethod.equals("card", ignoreCase = true) -> "Card"
                    rawMethod.isNotBlank() -> rawMethod.replaceFirstChar { it.uppercase() }
                    else -> "Netbanking"
                }

                Text(
                    text = paymentMethod,
                    fontSize = 13.sp,
                    color = Color(0xFF616161),
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
            }
        }
    }
}
