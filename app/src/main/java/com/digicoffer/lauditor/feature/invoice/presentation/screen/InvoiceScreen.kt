package com.digicoffer.lauditor.feature.invoice.presentation.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.buttons.AppHeaderButton
import com.digicoffer.lauditor.core.ui.common.feedback.AppEmptyState
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.core.ui.common.search.AppSearchField
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceMode
import com.digicoffer.lauditor.feature.invoice.presentation.components.InvoiceCardItem
import com.digicoffer.lauditor.feature.invoice.presentation.components.InvoiceDeleteConfirmDialog
import com.digicoffer.lauditor.feature.invoice.presentation.components.InvoiceFeedbackDialog
import com.digicoffer.lauditor.feature.invoice.presentation.components.InvoiceFormContent
import com.digicoffer.lauditor.feature.invoice.presentation.components.InvoiceReplaceConfirmDialog
import com.digicoffer.lauditor.feature.invoice.presentation.components.InvoiceShareDialog
import com.digicoffer.lauditor.feature.invoice.presentation.components.InvoiceStatusConfirmDialog
import com.digicoffer.lauditor.feature.invoice.presentation.components.InvoiceViewDetailsContent
import com.digicoffer.lauditor.feature.invoice.presentation.state.InvoiceUiEvent
import com.digicoffer.lauditor.feature.invoice.presentation.state.InvoiceUiState
import com.digicoffer.lauditor.feature.invoice.presentation.viewmodel.InvoiceViewModel

private val GillSans = FontFamily(Font(R.font.gill_sans_regular))

@Composable
fun InvoiceRoute(
    viewModel: InvoiceViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.downloadUrlToOpen) {
        uiState.downloadUrlToOpen?.let { url ->
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (ignored: Exception) {}
            viewModel.onEvent(InvoiceUiEvent.DownloadUrlOpened)
        }
    }

    InvoiceScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
fun InvoiceScreen(
    uiState: InvoiceUiState,
    onEvent: (InvoiceUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE6F0FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val title = when (uiState.mode) {
                    InvoiceMode.LIST -> "Invoices"
                    InvoiceMode.CREATE -> "Create Invoice"
                    InvoiceMode.EDIT -> "Edit Invoice"
                    InvoiceMode.VIEW -> "Invoice Details"
                }

                Text(
                    text = title,
                    color = Color(0xFF004D87),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = GillSans
                )

                if (uiState.mode == InvoiceMode.LIST) {
                    AppHeaderButton(
                        text = "Create Invoice",
                        iconRes = null,
                        onClick = { onEvent(InvoiceUiEvent.SwitchMode(InvoiceMode.CREATE)) }
                    )
                } else {
                    AppHeaderButton(
                        text = "View Invoices",
                        iconRes = R.drawable.eye_icon,
                        onClick = { onEvent(InvoiceUiEvent.SwitchMode(InvoiceMode.LIST)) }
                    )
                }
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (uiState.mode == InvoiceMode.LIST) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search Field
                        AppSearchField(
                            value = uiState.searchQuery,
                            onValueChange = { onEvent(InvoiceUiEvent.SearchQueryChanged(it)) },
                            placeholder = "Search Invoices...",
                            onClearClick = { onEvent(InvoiceUiEvent.SearchQueryChanged("")) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        if (uiState.isLoading && uiState.invoices.isEmpty()) {
                            AppLoader(modifier = Modifier.fillMaxSize())
                        } else if (uiState.filteredInvoices.isEmpty()) {
                            AppEmptyState(
                                title = if (uiState.searchQuery.isBlank()) "No Invoices Yet!" else "No matching invoices found",
                                description = if (uiState.searchQuery.isBlank()) "There are no invoice(s) under here." else "Try searching with a different client name or invoice number.",
                                imageRes = R.drawable.empty_invoice,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                items(
                                    items = uiState.filteredInvoices,
                                    key = { it.id.ifEmpty { it.invoiceNo } }
                                ) { invoice ->
                                    InvoiceCardItem(
                                        invoice = invoice,
                                        onViewDetails = { onEvent(InvoiceUiEvent.ViewDetails(invoice)) },
                                        onEdit = { onEvent(InvoiceUiEvent.EditInvoice(invoice)) },
                                        onChangeStatus = { onEvent(InvoiceUiEvent.PromptChangeStatus(invoice)) },
                                        onShare = { onEvent(InvoiceUiEvent.PromptShareInvoice(invoice)) },
                                        onDelete = { onEvent(InvoiceUiEvent.PromptDeleteInvoice(invoice)) },
                                        onSendReminder = { onEvent(InvoiceUiEvent.SendReminder(invoice)) }
                                    )
                                }
                            }
                        }
                    }
                } else if (uiState.mode == InvoiceMode.VIEW) {
                    InvoiceViewDetailsContent(
                        uiState = uiState,
                        onEvent = onEvent
                    )
                } else {
                    InvoiceFormContent(
                        uiState = uiState,
                        onEvent = onEvent
                    )
                }

                if (uiState.isLoading && uiState.invoices.isNotEmpty()) {
                    AppLoader(modifier = Modifier.fillMaxSize())
                }
            }
        }

        // Active Dialogs
        uiState.statusChangeTarget?.let { (item, newStatus) ->
            InvoiceStatusConfirmDialog(
                invoice = item,
                targetStatus = newStatus,
                onConfirm = { onEvent(InvoiceUiEvent.ConfirmChangeStatus) },
                onDismiss = { onEvent(InvoiceUiEvent.DismissDialogs) }
            )
        }

        uiState.deleteTarget?.let { invoice ->
            InvoiceDeleteConfirmDialog(
                invoice = invoice,
                onConfirm = { onEvent(InvoiceUiEvent.ConfirmDeleteInvoice) },
                onDismiss = { onEvent(InvoiceUiEvent.DismissDialogs) }
            )
        }

        uiState.shareTarget?.let { invoice ->
            InvoiceShareDialog(
                invoice = invoice,
                onSend = { message -> onEvent(InvoiceUiEvent.ConfirmShareInvoice(message)) },
                onDismiss = { onEvent(InvoiceUiEvent.DismissDialogs) }
            )
        }

        if (uiState.showReplaceConfirmDialog) {
            InvoiceReplaceConfirmDialog(
                onReplaceOriginal = { onEvent(InvoiceUiEvent.ConfirmEditChoice(replaceOriginal = true)) },
                onSaveBoth = { onEvent(InvoiceUiEvent.ConfirmEditChoice(replaceOriginal = false)) },
                onDismiss = { onEvent(InvoiceUiEvent.DismissDialogs) }
            )
        }

        LaunchedEffect(uiState.alertMessage) {
            uiState.alertMessage?.let { msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                onEvent(InvoiceUiEvent.DismissAlert)
            }
        }

        uiState.successMessage?.let { msg ->
            InvoiceFeedbackDialog(
                title = "Success",
                message = msg,
                onDismiss = { onEvent(InvoiceUiEvent.DismissDialogs) }
            )
        }
    }
}
