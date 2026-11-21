package com.digicoffer.lauditor.feature.invoice.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.dialogs.AppConfirmationDialog
import com.digicoffer.lauditor.core.ui.common.dialogs.AppDialog
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceListItem
import java.util.Locale

private val GillSans = FontFamily(Font(R.font.gill_sans_regular))

@Composable
fun InvoiceStatusConfirmDialog(
    invoice: InvoiceListItem,
    targetStatus: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val fromStatus = invoice.status.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
    }
    val toStatus = targetStatus.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
    }

    AppConfirmationDialog(
        title = "Change Invoice Status",
        message = "Are you sure you want to change the invoice status from\n$fromStatus to $toStatus?",
        confirmText = "Yes",
        dismissText = "No",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun InvoiceDeleteConfirmDialog(
    invoice: InvoiceListItem,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AppConfirmationDialog(
        title = "Delete Invoice",
        message = "Are you sure you want to delete invoice ${invoice.invoiceNo}?",
        confirmText = "Yes",
        dismissText = "No",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun InvoiceShareDialog(
    invoice: InvoiceListItem,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var message by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Share With: ${invoice.name}",
                    color = Color(0xFF1A3A6B),
                    fontSize = 17.sp,
                    fontFamily = GillSans,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Via: Lauditor (Secure)",
                    color = Color(0xFF1A3A6B),
                    fontSize = 14.sp,
                    fontFamily = GillSans
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = { Text("Message", fontFamily = GillSans, color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedBorderColor = Color(0xFF004D87),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.DarkGray
                        )
                    ) {
                        Text("Cancel", fontFamily = GillSans)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = { onSend(message.trim()) },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF004D87),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Send", fontFamily = GillSans)
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceReplaceConfirmDialog(
    onReplaceOriginal: () -> Unit,
    onSaveBoth: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Save Invoice Changes",
                    color = Color(0xFF1A3A6B),
                    fontSize = 18.sp,
                    fontFamily = GillSans,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Do you want to replace the original invoice or save both as separate records?",
                    color = Color(0xFF334155),
                    fontSize = 14.sp,
                    fontFamily = GillSans
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onReplaceOriginal,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF757575),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Replace Original",
                            fontFamily = GillSans,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = onSaveBoth,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF004D87),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Save Both",
                            fontFamily = GillSans,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
                ) {
                    Text("Cancel", fontFamily = GillSans, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun InvoiceFeedbackDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AppDialog(
        title = title,
        confirmText = "OK",
        onConfirm = onDismiss,
        onDismiss = onDismiss,
        content = {
            Text(
                text = message,
                color = Color(0xFF333333),
                fontSize = 15.sp,
                fontFamily = GillSans,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}
