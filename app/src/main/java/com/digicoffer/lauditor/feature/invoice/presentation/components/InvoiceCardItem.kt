package com.digicoffer.lauditor.feature.invoice.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.cards.AppCard
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceListItem
import kotlinx.coroutines.launch
import java.util.Locale

private val GillSans = FontFamily(Font(R.font.gill_sans_regular))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCardItem(
    invoice: InvoiceListItem,
    onViewDetails: () -> Unit,
    onEdit: () -> Unit,
    onChangeStatus: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onSendReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showShareTooltip by remember { mutableStateOf(false) }
    val tooltipState = rememberTooltipState(isPersistent = false)
    val scope = rememberCoroutineScope()

    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        backgroundColor = Color.White,
        shape = RoundedCornerShape(8.dp),
        elevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Client Name, Status Badge, Bell Icon, 3-dots Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (invoice.name.isNotBlank()) invoice.name else "Invoice",
                    color = Color(0xFF1A3A6B),
                    fontSize = 16.sp,
                    fontFamily = GillSans,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Status chip
                val statusText = invoice.status.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                }
                val (statusBg, statusTextColor) = when (invoice.status.lowercase(Locale.ROOT)) {
                    "collected" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
                    "cancelled" -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828))
                    "raised" -> Pair(Color(0xFFEBF5FF), Color(0xFF0073CF))
                    else -> Pair(Color(0xFFFFF3E0), Color(0xFFE65100))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusTextColor,
                        fontSize = 12.sp,
                        fontFamily = GillSans,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Bell Icon for Reminder (in between badge and 3-dot menu icon)
                val isRaised = invoice.status.equals("raised", ignoreCase = true)
                val canSendReminder = isRaised && invoice.reminder.canSend
                val reminderTooltipText = if (canSendReminder) {
                    "Send email reminder to ${invoice.name.ifBlank { "Client" }}"
                } else {
                    "Reminder is not available for this invoice yet."
                }

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip(
                            containerColor = Color(0xFF333333),
                            contentColor = Color.White,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = reminderTooltipText,
                                fontFamily = GillSans,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    },
                    state = tooltipState
                ) {
                    IconButton(
                        onClick = {
                            if (canSendReminder) {
                                onSendReminder()
                            } else {
                                scope.launch { tooltipState.show() }
                            }
                        },
                        enabled = !invoice.isDisabled,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.notify_bell),
                            contentDescription = reminderTooltipText,
                            tint = if (canSendReminder) Color(0xFF0073CF) else Color(0xFFA0AEC0),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // 3-dots action dropdown
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        enabled = !invoice.isDisabled,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Actions",
                            tint = if (invoice.isDisabled) Color.LightGray else Color(0xFF1A3A6B)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = {
                            menuExpanded = false
                            showShareTooltip = false
                        },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Details", fontFamily = GillSans, fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                showShareTooltip = false
                                onViewDetails()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Invoice", fontFamily = GillSans, fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                showShareTooltip = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Change Status", fontFamily = GillSans, fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                showShareTooltip = false
                                onChangeStatus()
                            }
                        )

                        // Custom position provider to place tooltip right beside the Share text
                        val density = androidx.compose.ui.platform.LocalDensity.current
                        val spacingPx = with(density) { 8.dp.roundToPx() }
                        val paddingPx = with(density) { 12.dp.roundToPx() }
                        val shareTooltipPositionProvider = remember(density, spacingPx, paddingPx) {
                            object : PopupPositionProvider {
                                override fun calculatePosition(
                                    anchorBounds: IntRect,
                                    windowSize: IntSize,
                                    layoutDirection: LayoutDirection,
                                    popupContentSize: IntSize
                                ): IntOffset {
                                    // 1. Try placing to the left of the Share row
                                    var x = anchorBounds.left - popupContentSize.width - spacingPx
                                    if (x < paddingPx) {
                                        // If not enough room to the left, try right of anchor
                                        if (anchorBounds.right + popupContentSize.width + spacingPx <= windowSize.width - paddingPx) {
                                            x = anchorBounds.right + spacingPx
                                        } else {
                                            // Place above the anchor, right-aligned with the anchor
                                            x = (anchorBounds.right - popupContentSize.width).coerceIn(paddingPx, windowSize.width - popupContentSize.width - paddingPx)
                                        }
                                    }

                                    // 2. Vertically center with the Share row
                                    var y = anchorBounds.top + (anchorBounds.height - popupContentSize.height) / 2
                                    y = y.coerceIn(paddingPx, windowSize.height - popupContentSize.height - paddingPx)

                                    return IntOffset(x, y)
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            DropdownMenuItem(
                                text = { Text("Share", fontFamily = GillSans, fontSize = 14.sp) },
                                enabled = invoice.canShare,
                                colors = MenuDefaults.itemColors(
                                    textColor = Color(0xFF1E293B),
                                    disabledTextColor = Color(0xFFA0AEC0)
                                ),
                                onClick = {
                                    menuExpanded = false
                                    showShareTooltip = false
                                    onShare()
                                }
                            )

                            if (!invoice.canShare) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable {
                                            showShareTooltip = true
                                        }
                                )

                                if (showShareTooltip) {
                                    Popup(
                                        popupPositionProvider = shareTooltipPositionProvider,
                                        onDismissRequest = {
                                            showShareTooltip = false
                                        },
                                        properties = PopupProperties(
                                            focusable = false,
                                            dismissOnBackPress = true,
                                            dismissOnClickOutside = true
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .shadow(6.dp, RoundedCornerShape(6.dp))
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF333333))
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                                .widthIn(max = 210.dp)
                                        ) {
                                            Text(
                                                text = "Sharing is possible only with active clients",
                                                color = Color.White,
                                                fontFamily = GillSans,
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red, fontFamily = GillSans, fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                showShareTooltip = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Info rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoColumn(label = "Invoice No", value = invoice.invoiceNo, modifier = Modifier.weight(1f))
                InfoColumn(label = "Created By", value = invoice.createdBy, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoColumn(label = "Invoice Date", value = invoice.date, modifier = Modifier.weight(1f))
                InfoColumn(label = "Due Date", value = invoice.dueDate, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InfoColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = Color(0xFF64748B),
            fontSize = 11.sp,
            fontFamily = GillSans
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = if (value.isNotBlank()) value else "-",
            color = Color(0xFF1E293B),
            fontSize = 13.sp,
            fontFamily = GillSans,
            fontWeight = FontWeight.Medium
        )
    }
}
