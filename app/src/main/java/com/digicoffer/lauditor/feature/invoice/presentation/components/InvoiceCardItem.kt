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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.cards.AppCard
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceListItem
import java.util.Locale

private val GillSans = FontFamily(Font(R.font.gill_sans_regular))

@Composable
fun InvoiceCardItem(
    invoice: InvoiceListItem,
    onViewDetails: () -> Unit,
    onEdit: () -> Unit,
    onChangeStatus: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

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
            // Header: Client Name, Status Badge, 3-dots Menu
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

                Spacer(modifier = Modifier.width(8.dp))

                // Status chip
                val statusText = invoice.status.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                }
                val (statusBg, statusTextColor) = when (invoice.status.lowercase(Locale.ROOT)) {
                    "collected" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
                    "cancelled" -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828))
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
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Details", fontFamily = GillSans, fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                onViewDetails()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Invoice", fontFamily = GillSans, fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Change Status", fontFamily = GillSans, fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                onChangeStatus()
                            }
                        )
                        if (invoice.canShare) {
                            DropdownMenuItem(
                                text = { Text("Share", fontFamily = GillSans, fontSize = 14.sp) },
                                onClick = {
                                    menuExpanded = false
                                    onShare()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red, fontFamily = GillSans, fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
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
