package com.digicoffer.lauditor.feature.invoice.presentation.components

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.cards.AppCard
import com.digicoffer.lauditor.core.ui.common.dropdowns.DropdownSelectorField
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceClient
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceLineItem
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceMatter
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceMode
import com.digicoffer.lauditor.feature.invoice.presentation.state.InvoiceUiEvent
import com.digicoffer.lauditor.feature.invoice.presentation.state.InvoiceUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val GillSans = FontFamily(Font(R.font.gill_sans_regular))
private val CURRENCIES = listOf("INR", "USD", "EUR", "GBP", "AUD", "CAD", "JPY", "CHF", "BHD", "KWD")

@Composable
fun InvoiceFormContent(
    uiState: InvoiceUiState,
    onEvent: (InvoiceUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEditable = uiState.mode != InvoiceMode.VIEW
    val scrollState = rememberScrollState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onEvent(InvoiceUiEvent.LogoSelected(uri))
        }
    }

    val currencySymbol = getCurrencySymbol(uiState.currencyCode)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Main Form Card
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White,
            shape = RoundedCornerShape(8.dp),
            elevation = 3.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 1. Logo Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val hasLogo = uiState.includeLogo && (uiState.logoUri != null || uiState.logoUrl.isNotBlank())
                    if (hasLogo) {
                        Box(
                            modifier = Modifier
                                .size(width = 130.dp, height = 65.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    ImageView(ctx).apply {
                                        scaleType = ImageView.ScaleType.FIT_CENTER
                                    }
                                },
                                update = { imageView ->
                                    val target = uiState.logoUri ?: uiState.logoUrl
                                    Glide.with(context)
                                        .load(target)
                                        .placeholder(R.drawable.rectangle_grey_background)
                                        .into(imageView)
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            if (isEditable) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(22.dp)
                                        .background(Color.Red, RoundedCornerShape(11.dp))
                                        .clickable { onEvent(InvoiceUiEvent.RemoveLogo) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove Logo",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    } else if (isEditable) {
                        Box(
                            modifier = Modifier
                                .size(width = 130.dp, height = 65.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF004D87), RoundedCornerShape(6.dp))
                                .background(Color(0xFFF1F5F9))
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+ Upload Logo",
                                color = Color(0xFF004D87),
                                fontSize = 13.sp,
                                fontFamily = GillSans,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(1.dp))
                    }

                    // Currency selector
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Currency",
                            fontSize = 12.sp,
                            fontFamily = GillSans,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        DropdownSelectorField(
                            items = CURRENCIES,
                            selectedItem = uiState.currencyCode,
                            onItemSelected = { onEvent(InvoiceUiEvent.CurrencySelected(it)) },
                            itemToLabel = { it },
                            placeholder = "Currency",
                            enabled = isEditable,
                            modifier = Modifier.width(110.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // 2. Invoice Details Grid (Invoice #, Date, Due Date)
                Text(
                    text = "Invoice Information",
                    fontSize = 15.sp,
                    fontFamily = GillSans,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A6B)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.invoiceCode,
                    onValueChange = { onEvent(InvoiceUiEvent.InvoiceCodeChanged(it)) },
                    label = { Text("Invoice Number", fontFamily = GillSans) },
                    enabled = isEditable,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = formFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DateFieldItem(
                        label = "Invoice Date",
                        displayDate = uiState.invoiceDate,
                        enabled = isEditable,
                        onDateSelected = { disp, api ->
                            onEvent(InvoiceUiEvent.InvoiceDateSelected(disp, api))
                        },
                        modifier = Modifier.weight(1f)
                    )

                    DateFieldItem(
                        label = "Due Date",
                        displayDate = uiState.dueDate,
                        enabled = isEditable,
                        onDateSelected = { disp, api ->
                            onEvent(InvoiceUiEvent.DueDateSelected(disp, api))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Tax Identifiers (Firm GST / PAN)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.firmGstNo,
                        onValueChange = { onEvent(InvoiceUiEvent.FirmGstChanged(it)) },
                        label = { Text("Firm GST No", fontFamily = GillSans) },
                        enabled = isEditable,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        colors = formFieldColors()
                    )

                    OutlinedTextField(
                        value = uiState.firmPanNo,
                        onValueChange = { onEvent(InvoiceUiEvent.FirmPanChanged(it)) },
                        label = { Text("Firm PAN No", fontFamily = GillSans) },
                        enabled = isEditable,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        colors = formFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // 4. Client & Matter Section
                Text(
                    text = "Client & Matter Details",
                    fontSize = 15.sp,
                    fontFamily = GillSans,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A6B)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Client selector
                DropdownSelectorField(
                    items = uiState.availableClients,
                    selectedItem = uiState.selectedClient,
                    onItemSelected = { onEvent(InvoiceUiEvent.ClientSelected(it)) },
                    itemToLabel = { it.name },
                    placeholder = "Select Client *",
                    enabled = isEditable,
                    showSearch = true,
                    onClearSelection = if (isEditable) { { onEvent(InvoiceUiEvent.ClientSelected(null)) } } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                // Matter selector (shown if matters available or matter selected)
                if (uiState.availableMatters.isNotEmpty() || uiState.selectedMatter != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    DropdownSelectorField(
                        items = uiState.availableMatters,
                        selectedItem = uiState.selectedMatter,
                        onItemSelected = { onEvent(InvoiceUiEvent.MatterSelected(it)) },
                        itemToLabel = { it.title },
                        placeholder = "Select Matter (Optional)",
                        enabled = isEditable,
                        showSearch = true,
                        onClearSelection = if (isEditable) { { onEvent(InvoiceUiEvent.MatterSelected(null)) } } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Client GST & PAN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.clientGstNo,
                        onValueChange = { onEvent(InvoiceUiEvent.ClientGstChanged(it)) },
                        label = { Text("Client GST No", fontFamily = GillSans) },
                        enabled = isEditable,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        colors = formFieldColors()
                    )

                    OutlinedTextField(
                        value = uiState.clientPanNo,
                        onValueChange = { onEvent(InvoiceUiEvent.ClientPanChanged(it)) },
                        label = { Text("Client PAN No", fontFamily = GillSans) },
                        enabled = isEditable,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        colors = formFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Client Address with 150 char count
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.billTo,
                        onValueChange = {
                            if (it.length <= 150) onEvent(InvoiceUiEvent.BillToChanged(it))
                        },
                        label = { Text("Client Address *", fontFamily = GillSans) },
                        enabled = isEditable,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 4,
                        shape = RoundedCornerShape(6.dp),
                        colors = formFieldColors()
                    )
                    Text(
                        text = "${uiState.billTo.length}/150",
                        fontSize = 11.sp,
                        fontFamily = GillSans,
                        color = Color.Gray,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp, end = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // 5. Line Items Table
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Invoice Items",
                        fontSize = 15.sp,
                        fontFamily = GillSans,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A3A6B)
                    )

                    if (isEditable) {
                        TextButton(
                            onClick = { onEvent(InvoiceUiEvent.AddLineItem) },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF004D87))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Item", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Item", fontFamily = GillSans, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                uiState.lineItems.forEachIndexed { index, item ->
                    LineItemEditorRow(
                        index = index,
                        item = item,
                        currencySymbol = currencySymbol,
                        isEditable = isEditable,
                        canDelete = isEditable && uiState.lineItems.size > 1,
                        onUpdate = { updated -> onEvent(InvoiceUiEvent.UpdateLineItem(index, updated)) },
                        onDelete = { onEvent(InvoiceUiEvent.RemoveLineItem(index)) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 6. Notes / Additional details
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = {
                            if (it.length <= 250) onEvent(InvoiceUiEvent.NotesChanged(it))
                        },
                        label = { Text("Additional Details / Notes", fontFamily = GillSans) },
                        enabled = isEditable,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(6.dp),
                        colors = formFieldColors()
                    )
                    Text(
                        text = "${uiState.notes.length}/250",
                        fontSize = 11.sp,
                        fontFamily = GillSans,
                        color = Color.Gray,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp, end = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 7. Calculations Card (Subtotal, Discount, Tax, Total)
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(6.dp),
                    elevation = 0.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        CalculationRow(
                            label = "Sub Total",
                            value = "$currencySymbol ${String.format(Locale.getDefault(), "%.2f", uiState.subTotal)}"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Discount Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Discount", fontFamily = GillSans, fontSize = 14.sp, color = Color(0xFF334155))
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedTextField(
                                    value = uiState.discountPercent,
                                    onValueChange = { onEvent(InvoiceUiEvent.DiscountPercentChanged(it)) },
                                    enabled = isEditable,
                                    placeholder = { Text("0", fontFamily = GillSans) },
                                    modifier = Modifier.width(65.dp).height(46.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(4.dp),
                                    colors = formFieldColors()
                                )
                                Text(text = " (%)", fontFamily = GillSans, fontSize = 13.sp, color = Color.Gray)
                            }
                            Text(
                                text = "- $currencySymbol ${String.format(Locale.getDefault(), "%.2f", uiState.discountAmount)}",
                                fontFamily = GillSans,
                                fontSize = 14.sp,
                                color = Color(0xFFD32F2F)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tax Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Tax", fontFamily = GillSans, fontSize = 14.sp, color = Color(0xFF334155))
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedTextField(
                                    value = uiState.taxPercent,
                                    onValueChange = { onEvent(InvoiceUiEvent.TaxPercentChanged(it)) },
                                    enabled = isEditable,
                                    placeholder = { Text("0", fontFamily = GillSans) },
                                    modifier = Modifier.width(65.dp).height(46.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(4.dp),
                                    colors = formFieldColors()
                                )
                                Text(text = " (%)", fontFamily = GillSans, fontSize = 13.sp, color = Color.Gray)
                            }
                            Text(
                                text = "+ $currencySymbol ${String.format(Locale.getDefault(), "%.2f", uiState.taxAmount)}",
                                fontFamily = GillSans,
                                fontSize = 14.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = Color(0xFFCBD5E1), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Final Total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total",
                                fontFamily = GillSans,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A3A6B)
                            )
                            Text(
                                text = "$currencySymbol ${String.format(Locale.getDefault(), "%.2f", uiState.total)}",
                                fontFamily = GillSans,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A3A6B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Thank you banner
                Text(
                    text = "THANK YOU FOR YOUR BUSINESS!",
                    fontFamily = GillSans,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF004D87),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 8. Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val cancelLabel = if (uiState.mode == InvoiceMode.VIEW) "Back to List" else "Cancel"
                    OutlinedButton(
                        onClick = { onEvent(InvoiceUiEvent.SwitchMode(InvoiceMode.LIST)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
                    ) {
                        Text(cancelLabel, fontFamily = GillSans, fontSize = 15.sp)
                    }

                    if (uiState.mode == InvoiceMode.VIEW) {
                        Button(
                            onClick = {
                                if (uiState.editingDocId != null) {
                                    onEvent(InvoiceUiEvent.DownloadInvoice(uiState.editingDocId))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF004D87),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Download", fontFamily = GillSans, fontSize = 15.sp)
                        }
                    } else {
                        val saveLabel = if (uiState.mode == InvoiceMode.EDIT) "Update" else "Save"
                        Button(
                            onClick = { onEvent(InvoiceUiEvent.SaveInvoiceClicked) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF004D87),
                                contentColor = Color.White
                            )
                        ) {
                            Text(saveLabel, fontFamily = GillSans, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LineItemEditorRow(
    index: Int,
    item: InvoiceLineItem,
    currencySymbol: String,
    isEditable: Boolean,
    canDelete: Boolean,
    onUpdate: (InvoiceLineItem) -> Unit,
    onDelete: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFFFBFCFD),
        shape = RoundedCornerShape(6.dp),
        elevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Item #${index + 1}",
                    fontFamily = GillSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF1A3A6B)
                )

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Item",
                            tint = Color.Red,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Item Name / Description
            OutlinedTextField(
                value = item.name,
                onValueChange = {
                    if (it.length <= 250) onUpdate(item.copy(name = it))
                },
                label = { Text("Description", fontFamily = GillSans) },
                enabled = isEditable,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = formFieldColors()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rate
                OutlinedTextField(
                    value = if (item.unitPrice > 0) item.unitPrice.toString() else "",
                    onValueChange = {
                        val price = it.toDoubleOrNull() ?: 0.0
                        onUpdate(item.copy(unitPrice = price))
                    },
                    label = { Text("Rate ($currencySymbol)", fontFamily = GillSans) },
                    enabled = isEditable,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    colors = formFieldColors()
                )

                // Quantity
                OutlinedTextField(
                    value = if (item.quantity > 0) item.quantity.toString() else "1",
                    onValueChange = {
                        val qty = it.toIntOrNull() ?: 1
                        onUpdate(item.copy(quantity = qty))
                    },
                    label = { Text("Qty", fontFamily = GillSans) },
                    enabled = isEditable,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.6f),
                    shape = RoundedCornerShape(4.dp),
                    colors = formFieldColors()
                )

                // Computed Amount
                Column(
                    modifier = Modifier.weight(0.8f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(text = "Amount", fontFamily = GillSans, fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$currencySymbol ${String.format(Locale.getDefault(), "%.2f", item.amount)}",
                        fontFamily = GillSans,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }
            }
        }
    }
}

@Composable
private fun DateFieldItem(
    label: String,
    displayDate: String,
    enabled: Boolean,
    onDateSelected: (display: String, api: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
            val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            onDateSelected(displayFormat.format(cal.time), apiFormat.format(cal.time))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Box(modifier = modifier) {
        OutlinedTextField(
            value = displayDate,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontFamily = GillSans) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = label,
                    tint = if (enabled) Color(0xFF004D87) else Color.Gray
                )
            },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { datePickerDialog.show() },
            shape = RoundedCornerShape(6.dp),
            colors = formFieldColors()
        )
        // Click overlay to trigger picker cleanly
        if (enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { datePickerDialog.show() }
            )
        }
    }
}

@Composable
private fun CalculationRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontFamily = GillSans, fontSize = 14.sp, color = Color(0xFF334155))
        Text(text = value, fontFamily = GillSans, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
    }
}

@Composable
private fun formFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFFF8FAFC),
    unfocusedContainerColor = Color(0xFFF8FAFC),
    disabledContainerColor = Color(0xFFF1F5F9),
    focusedBorderColor = Color(0xFF004D87),
    unfocusedBorderColor = Color(0xFFCBD5E1),
    disabledBorderColor = Color(0xFFE2E8F0),
    focusedTextColor = Color(0xFF1E293B),
    unfocusedTextColor = Color(0xFF1E293B),
    disabledTextColor = Color(0xFF64748B)
)

private fun getCurrencySymbol(code: String): String {
    return when (code) {
        "INR" -> "₹"
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "JPY" -> "¥"
        "AUD" -> "A$"
        "CAD" -> "C$"
        "CHF" -> "Fr"
        "BHD" -> "BD"
        "KWD" -> "KD"
        else -> "$code "
    }
}
