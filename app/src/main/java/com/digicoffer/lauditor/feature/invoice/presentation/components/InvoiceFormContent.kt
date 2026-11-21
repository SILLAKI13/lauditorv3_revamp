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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.cards.AppCard
import com.digicoffer.lauditor.core.ui.common.dropdowns.DropdownSelectorField
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceLineItem
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceMode
import com.digicoffer.lauditor.feature.invoice.presentation.state.InvoiceUiEvent
import com.digicoffer.lauditor.feature.invoice.presentation.state.InvoiceUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val GillSans = FontFamily(Font(R.font.gill_sans_regular))
private val BrandBlue = Color(0xFF0073CF)
private val CURRENCIES = listOf("INR", "USD", "EUR", "GBP", "AUD", "CAD", "JPY", "CHF", "BHD", "KWD")

@Composable
fun InvoiceFormContent(
    uiState: InvoiceUiState,
    onEvent: (InvoiceUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isUSAccount) {
        USInvoiceFormContent(uiState = uiState, onEvent = onEvent, modifier = modifier)
    } else {
        IndiaInvoiceFormContent(uiState = uiState, onEvent = onEvent, modifier = modifier)
    }
}

// =========================================================================================
// US BASED INVOICE FORM (Pixel-perfect matching web screenshots)
// =========================================================================================

@Composable
private fun USInvoiceFormContent(
    uiState: InvoiceUiState,
    onEvent: (InvoiceUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onEvent(InvoiceUiEvent.LogoSelected(uri))
        }
    }

    var clientDropdownExpanded by remember { mutableStateOf(false) }
    val clientQuery = uiState.clientContactName

    val filteredClientSuggestions = remember(clientQuery, uiState.availableClients) {
        if (clientQuery.isBlank()) {
            emptyList()
        } else {
            val matching = uiState.availableClients.filter {
                it.name.contains(clientQuery, ignoreCase = true) ||
                it.email.contains(clientQuery, ignoreCase = true) ||
                it.phone.contains(clientQuery, ignoreCase = true)
            }
            if (matching.isNotEmpty()) matching else uiState.availableClients
        }
    }

    val hasLogo = uiState.includeLogo && (uiState.logoUri != null || uiState.logoUrl.isNotBlank())
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(if (isTablet) 20.dp else 12.dp)
    ) {
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
                    .padding(if (isTablet) 20.dp else 14.dp)
            ) {
                    // 1. TOP SECTION: Logo + Invoice No + Dates + Client info + Address
                    if (isTablet) {
                        // Tablet/Desktop: Side-by-side Logo and 3-column metadata
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Logo Box
                            Column(modifier = Modifier.width(135.dp)) {
                                Text(
                                    text = "LOGO",
                                    color = BrandBlue,
                                    fontFamily = GillSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                USLogoBox(
                                    hasLogo = hasLogo,
                                    uiState = uiState,
                                    context = context,
                                    onPickLogo = { imagePickerLauncher.launch("image/*") },
                                    onRemoveLogo = { onEvent(InvoiceUiEvent.RemoveLogo) },
                                    modifier = Modifier.fillMaxWidth().height(125.dp)
                                )
                            }

                            // Metadata Fields
                            Column(modifier = Modifier.weight(1f)) {
                                // Row 1: Invoice No*, Invoice Date*, Due Date*
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        USInvoiceNoHeader()
                                        Spacer(modifier = Modifier.height(3.dp))
                                        USInvoiceNoField(uiState, onEvent)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Invoice Date*",
                                            color = BrandBlue,
                                            fontFamily = GillSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        USDateFieldItem(
                                            displayDate = uiState.invoiceDate,
                                            placeholder = "Select",
                                            enabled = true,
                                            onDateSelected = { disp, api -> onEvent(InvoiceUiEvent.InvoiceDateSelected(disp, api)) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Due Date*",
                                            color = BrandBlue,
                                            fontFamily = GillSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        USDateFieldItem(
                                            displayDate = uiState.dueDate,
                                            placeholder = "Select",
                                            enabled = true,
                                            onDateSelected = { disp, api -> onEvent(InvoiceUiEvent.DueDateSelected(disp, api)) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Row 2: Client name*, Email*, Phone*
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        USClientNameField(
                                            uiState = uiState,
                                            clientDropdownExpanded = clientDropdownExpanded,
                                            filteredClientSuggestions = filteredClientSuggestions,
                                            onDropdownChange = { clientDropdownExpanded = it },
                                            onEvent = onEvent
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        USEmailField(uiState = uiState, onEvent = onEvent)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        USPhoneField(uiState = uiState, onEvent = onEvent)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Row 3: Client Address (optional)
                                USClientAddressField(uiState = uiState, onEvent = onEvent)
                            }
                        }
                    } else {
                        // Mobile: Clean mobile layout (Logo on top, no compressed horizontal squishing)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Logo Box at top
                            Text(
                                text = "LOGO",
                                color = BrandBlue,
                                fontFamily = GillSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            USLogoBox(
                                hasLogo = hasLogo,
                                uiState = uiState,
                                context = context,
                                onPickLogo = { imagePickerLauncher.launch("image/*") },
                                onRemoveLogo = { onEvent(InvoiceUiEvent.RemoveLogo) },
                                modifier = Modifier.size(width = 130.dp, height = 75.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Invoice No*
                            USInvoiceNoHeader()
                            Spacer(modifier = Modifier.height(3.dp))
                            USInvoiceNoField(uiState, onEvent)

                            Spacer(modifier = Modifier.height(10.dp))

                            // Invoice Date* & Due Date* side-by-side (2 columns)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Invoice Date*",
                                        color = BrandBlue,
                                        fontFamily = GillSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    USDateFieldItem(
                                        displayDate = uiState.invoiceDate,
                                        placeholder = "Select",
                                        enabled = true,
                                        onDateSelected = { disp, api -> onEvent(InvoiceUiEvent.InvoiceDateSelected(disp, api)) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Due Date*",
                                        color = BrandBlue,
                                        fontFamily = GillSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    USDateFieldItem(
                                        displayDate = uiState.dueDate,
                                        placeholder = "Select",
                                        enabled = true,
                                        onDateSelected = { disp, api -> onEvent(InvoiceUiEvent.DueDateSelected(disp, api)) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Client name*
                            USClientNameField(
                                uiState = uiState,
                                clientDropdownExpanded = clientDropdownExpanded,
                                filteredClientSuggestions = filteredClientSuggestions,
                                onDropdownChange = { clientDropdownExpanded = it },
                                onEvent = onEvent
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Email*
                            USEmailField(uiState = uiState, onEvent = onEvent)

                            Spacer(modifier = Modifier.height(10.dp))

                            // Phone*
                            USPhoneField(uiState = uiState, onEvent = onEvent)

                            Spacer(modifier = Modifier.height(10.dp))

                            // Client Address (optional)
                            USClientAddressField(uiState = uiState, onEvent = onEvent)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 2. SERVICE ITEMS SECTION
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Service items",
                            fontSize = 15.sp,
                            fontFamily = GillSans,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlue
                        )

                        Button(
                            onClick = { onEvent(InvoiceUiEvent.AddLineItem) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("+ Add", fontFamily = GillSans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isTablet) {
                        // Desktop/Tablet Table
                        USTableServiceItems(uiState = uiState, onEvent = onEvent)
                    } else {
                        // Mobile Cards for Service Items
                        USMobileServiceItems(uiState = uiState, onEvent = onEvent)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. BANK DETAILS & SUMMARY CALCULATIONS
                    if (isTablet) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                USBankDetailsSection(uiState = uiState, onEvent = onEvent)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                USCalculationsSection(uiState = uiState, onEvent = onEvent)
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            USBankDetailsSection(uiState = uiState, onEvent = onEvent)
                            Spacer(modifier = Modifier.height(16.dp))
                            USCalculationsSection(uiState = uiState, onEvent = onEvent)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 4. ADDITIONAL DETAILS
                    Text(
                        text = "Additional Details",
                        color = BrandBlue,
                        fontFamily = GillSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = uiState.notes,
                            onValueChange = {
                                if (it.length <= 250) onEvent(InvoiceUiEvent.NotesChanged(it))
                            },
                            placeholder = { Text("Please add billing information for clients", fontFamily = GillSans, fontSize = 12.sp, color = Color(0xFF94A3B8)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(6.dp),
                            colors = formFieldColors()
                        )
                        Text(
                            text = "${uiState.notes.length} / 250 characters",
                            fontFamily = GillSans,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 2.dp, end = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 5. THANK YOU FOR YOUR BUSINESS.
                    Text(
                        text = "THANK YOU FOR YOUR BUSINESS.",
                        fontFamily = GillSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BrandBlue,
                        textAlign = if (isTablet) TextAlign.Start else TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 6. ACTION BUTTONS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isTablet) Arrangement.End else Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onEvent(InvoiceUiEvent.SwitchMode(InvoiceMode.LIST)) },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E293B)),
                            modifier = if (isTablet) Modifier.width(130.dp).height(44.dp) else Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("Cancel", fontFamily = GillSans, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        }

                        if (isTablet) {
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        val isUpdateEnabled = if (uiState.mode == InvoiceMode.EDIT) uiState.hasEditChanges else true
                        Button(
                            onClick = { onEvent(InvoiceUiEvent.SaveInvoiceClicked) },
                            enabled = isUpdateEnabled,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandBlue,
                                contentColor = Color.White,
                                disabledContainerColor = BrandBlue.copy(alpha = 0.4f),
                                disabledContentColor = Color.White.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = if (isTablet) Modifier.width(130.dp).height(44.dp) else Modifier.weight(1f).height(44.dp)
                        ) {
                            Text(
                                text = if (uiState.mode == InvoiceMode.EDIT) "Update" else "Save",
                                fontFamily = GillSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
}

@Composable
private fun USLogoBox(
    hasLogo: Boolean,
    uiState: InvoiceUiState,
    context: android.content.Context,
    onPickLogo: () -> Unit,
    onRemoveLogo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (hasLogo) {
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )

            // Red Circular 'X' delete button in top-right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
                    .clickable { onRemoveLogo() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove Logo",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
                    .clickable { onPickLogo() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+ Upload Logo",
                    color = BrandBlue,
                    fontSize = 12.sp,
                    fontFamily = GillSans,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun USInvoiceNoHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Invoice No*",
            color = BrandBlue,
            fontFamily = GillSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
        Text(
            text = "Auto-generated (editable)",
            color = Color(0xFF64748B),
            fontFamily = GillSans,
            fontStyle = FontStyle.Italic,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun USInvoiceNoField(
    uiState: InvoiceUiState,
    onEvent: (InvoiceUiEvent) -> Unit
) {
    USTextInputField(
        value = uiState.invoiceCode,
        onValueChange = { onEvent(InvoiceUiEvent.InvoiceCodeChanged(it)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
    )
}

@Composable
private fun USClientNameField(
    uiState: InvoiceUiState,
    clientDropdownExpanded: Boolean,
    filteredClientSuggestions: List<com.digicoffer.lauditor.feature.invoice.data.model.InvoiceClient>,
    onDropdownChange: (Boolean) -> Unit,
    onEvent: (InvoiceUiEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Client name*",
            color = BrandBlue,
            fontFamily = GillSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            USTextInputField(
                value = uiState.clientContactName,
                onValueChange = {
                    onEvent(InvoiceUiEvent.ClientNameChanged(it))
                    onDropdownChange(it.isNotBlank())
                },
                placeholder = "Type a name to search or add",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // Inline suggestion list: avoids Popup/WindowManager window recreation that dismisses keyboard
            if (clientDropdownExpanded && filteredClientSuggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                        .verticalScroll(rememberScrollState())
                ) {
                    filteredClientSuggestions.forEachIndexed { index, client ->
                        val displayEmail = if (client.email.isNotBlank()) {
                            if (client.email.contains("*")) client.email
                            else com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.maskEmail(client.email)
                        } else ""
                        val displayPhone = if (client.phone.isNotBlank()) {
                            if (client.phone.contains("*")) client.phone
                            else com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.maskPhoneNumber(client.phone)
                        } else ""

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onEvent(InvoiceUiEvent.ClientSelected(client))
                                    onDropdownChange(false)
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = client.name,
                                fontFamily = GillSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            )
                            if (displayEmail.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = displayEmail,
                                    fontFamily = GillSans,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            if (displayPhone.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = displayPhone,
                                    fontFamily = GillSans,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                        if (index < filteredClientSuggestions.size - 1) {
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun USEmailField(
    uiState: InvoiceUiState,
    onEvent: (InvoiceUiEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Email*",
            color = BrandBlue,
            fontFamily = GillSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        USTextInputField(
            value = uiState.clientEmail,
            onValueChange = { onEvent(InvoiceUiEvent.ClientEmailChanged(it)) },
            placeholder = "client@example.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun USPhoneField(
    uiState: InvoiceUiState,
    onEvent: (InvoiceUiEvent) -> Unit
) {
    var countryCodeExpanded by remember { mutableStateOf(false) }
    val isEditable = uiState.mode != InvoiceMode.VIEW
    val currentCode = when {
        uiState.countryCode.contains("91") -> "+91"
        else -> "+1"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Phone*",
            color = BrandBlue,
            fontFamily = GillSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Country Code Dropdown Selector
            Box {
                Row(
                    modifier = Modifier
                        .width(78.dp)
                        .height(42.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                        .background(Color(0xFFF8FAFC))
                        .clickable(enabled = isEditable) { countryCodeExpanded = true }
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentCode,
                        fontFamily = GillSans,
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Country Code",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = countryCodeExpanded && isEditable,
                    onDismissRequest = { countryCodeExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    listOf(
                        "US +1" to "+1",
                        "India +91" to "+91"
                    ).forEach { (label, code) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = label,
                                    fontFamily = GillSans,
                                    fontSize = 13.sp,
                                    fontWeight = if (code == currentCode) FontWeight.Bold else FontWeight.Normal,
                                    color = Color(0xFF1E293B)
                                )
                            },
                            onClick = {
                                onEvent(InvoiceUiEvent.CountryCodeChanged(code))
                                countryCodeExpanded = false
                            }
                        )
                    }
                }
            }

            // Phone Number Input (Only digits, max 10)
            Box(modifier = Modifier.weight(1f)) {
                USTextInputField(
                    value = uiState.clientPhone,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(10)
                        onEvent(InvoiceUiEvent.ClientPhoneChanged(digits))
                    },
                    placeholder = "Enter 10-digit phone number",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEditable
                )
            }
        }
    }
}

@Composable
private fun USClientAddressField(
    uiState: InvoiceUiState,
    onEvent: (InvoiceUiEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Client Address (optional)",
            color = BrandBlue,
            fontFamily = GillSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        USTextInputField(
            value = uiState.billTo,
            onValueChange = {
                if (it.length <= 150) onEvent(InvoiceUiEvent.BillToChanged(it))
            },
            placeholder = "Street, City, State, ZIP",
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun USMobileServiceItems(
    uiState: InvoiceUiState,
    onEvent: (InvoiceUiEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        uiState.lineItems.forEachIndexed { index, item ->
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
                        .padding(12.dp)
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
                            color = BrandBlue
                        )
                        if (uiState.lineItems.size > 1) {
                            IconButton(
                                onClick = { onEvent(InvoiceUiEvent.RemoveLineItem(index)) },
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

                    Text(
                        text = "Description",
                        fontFamily = GillSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    USTextInputField(
                        value = item.name,
                        onValueChange = {
                            if (it.length <= 250) {
                                onEvent(InvoiceUiEvent.UpdateLineItem(index, item.copy(name = it)))
                            }
                        },
                        placeholder = "Enter description",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${item.name.length} / 250 characters",
                        fontFamily = GillSans,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Rate - USD
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Rate - USD",
                                fontFamily = GillSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            USTextInputField(
                                value = item.rateText,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                                        val price = input.toDoubleOrNull() ?: 0.0
                                        onEvent(InvoiceUiEvent.UpdateLineItem(index, item.copy(rateText = input, unitPrice = price)))
                                    }
                                },
                                leadingText = "$ ",
                                placeholder = "0",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Quantity
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Quantity",
                                fontFamily = GillSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            USTextInputField(
                                value = item.quantityText,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.matches(Regex("^\\d*$"))) {
                                        val qty = input.toIntOrNull() ?: 0
                                        onEvent(InvoiceUiEvent.UpdateLineItem(index, item.copy(quantityText = input, quantity = qty)))
                                    }
                                },
                                placeholder = "1",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Amount
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Amount",
                                fontFamily = GillSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "$${String.format(Locale.getDefault(), "%.2f", item.amount)}",
                                    fontFamily = GillSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun USTableServiceItems(
    uiState: InvoiceUiState,
    onEvent: (InvoiceUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
    ) {
        // Table Header Bar (Solid Blue)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandBlue)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sl.No.",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.width(45.dp)
            )
            Text(
                text = "Description",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.weight(2.5f)
            )
            Text(
                text = "Rate - USD",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Quantity",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Amount",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1.1f)
            )
            if (uiState.lineItems.size > 1) {
                Spacer(modifier = Modifier.width(28.dp))
            }
        }

        // Table Rows
        uiState.lineItems.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}",
                    fontFamily = GillSans,
                    fontSize = 12.sp,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.width(45.dp)
                )

                // Description Input with counter
                Column(modifier = Modifier.weight(2.5f)) {
                    USTextInputField(
                        value = item.name,
                        onValueChange = {
                            if (it.length <= 250) {
                                onEvent(InvoiceUiEvent.UpdateLineItem(index, item.copy(name = it)))
                            }
                        },
                        placeholder = "Enter description",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${item.name.length} / 250 characters",
                        fontFamily = GillSans,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Rate - USD
                USTextInputField(
                    value = item.rateText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                            val price = input.toDoubleOrNull() ?: 0.0
                            onEvent(InvoiceUiEvent.UpdateLineItem(index, item.copy(rateText = input, unitPrice = price)))
                        }
                    },
                    leadingText = "$ ",
                    placeholder = "0",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Quantity
                USTextInputField(
                    value = item.quantityText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*$"))) {
                            val qty = input.toIntOrNull() ?: 0
                            onEvent(InvoiceUiEvent.UpdateLineItem(index, item.copy(quantityText = input, quantity = qty)))
                        }
                    },
                    placeholder = "1",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Computed Amount
                Text(
                    text = "$${String.format(Locale.getDefault(), "%.2f", item.amount)}",
                    fontFamily = GillSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.End,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.weight(1.1f)
                )

                if (uiState.lineItems.size > 1) {
                    IconButton(
                        onClick = { onEvent(InvoiceUiEvent.RemoveLineItem(index)) },
                        modifier = Modifier.size(28.dp).padding(start = 4.dp)
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

            if (index < uiState.lineItems.size - 1) {
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun USBankDetailsSection(
    uiState: InvoiceUiState,
    onEvent: (InvoiceUiEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Bank details for payment",
            color = BrandBlue,
            fontFamily = GillSans,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "Pick saved details (from your profile or past invoices), edit them, or enter new ones.",
            fontFamily = GillSans,
            fontSize = 11.sp,
            color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Selection Cards (Chase Bank Branch / Add new details)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Saved details from profile
            val opt0Selected = uiState.selectedBankDetailOption == 0
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .border(
                        width = if (opt0Selected) 2.dp else 1.dp,
                        color = if (opt0Selected) BrandBlue else Color(0xFFCBD5E1),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .background(Color.White)
                    .clickable { onEvent(InvoiceUiEvent.BankDetailOptionSelected(0)) }
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = uiState.savedBankDetailsTitle,
                        fontFamily = GillSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "From profile",
                        fontFamily = GillSans,
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Card 2: + Add new details (Free text)
            val opt1Selected = uiState.selectedBankDetailOption == 1
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .border(
                        width = if (opt1Selected) 2.dp else 1.dp,
                        color = if (opt1Selected) BrandBlue else Color(0xFFCBD5E1),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .background(Color.White)
                    .clickable { onEvent(InvoiceUiEvent.BankDetailOptionSelected(1)) }
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = "+ Add new details",
                        fontFamily = GillSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Free text",
                        fontFamily = GillSans,
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Text Area for Bank details
        OutlinedTextField(
            value = uiState.bankDetails,
            onValueChange = {
                if (it.length <= 500) {
                    onEvent(InvoiceUiEvent.BankDetailsChanged(it))
                }
            },
            placeholder = {
                Text(
                    text = "Bank name, account name, account no., routing no., SWIFT...",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontFamily = GillSans
                )
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 6,
            shape = RoundedCornerShape(6.dp),
            colors = formFieldColors()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Checkbox: Save for reuse in future invoices & Character count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = uiState.saveBankDetailsForFuture,
                    onCheckedChange = { onEvent(InvoiceUiEvent.SaveBankDetailsChecked(it)) },
                    colors = CheckboxDefaults.colors(checkedColor = BrandBlue)
                )
                Text(
                    text = "Save for reuse in future invoices",
                    fontFamily = GillSans,
                    fontSize = 11.sp,
                    color = Color(0xFF334155)
                )
            }

            Text(
                text = "${uiState.bankDetails.length} / 500 characters",
                fontFamily = GillSans,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun USCalculationsSection(
    uiState: InvoiceUiState,
    onEvent: (InvoiceUiEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Sub Total
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sub Total",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$${String.format(Locale.getDefault(), "%.2f", uiState.subTotal)}",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.End,
                modifier = Modifier.width(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Discount Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Discount",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f)
            )
            USPercentInputField(
                value = uiState.discountPercent,
                onValueChange = { onEvent(InvoiceUiEvent.DiscountPercentChanged(it)) },
                modifier = Modifier.width(76.dp).height(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "- $${String.format(Locale.getDefault(), "%.2f", uiState.calculatedDiscountAmount)}",
                fontFamily = GillSans,
                fontSize = 13.sp,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.End,
                modifier = Modifier.width(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = BrandBlue, thickness = 2.dp)
        Spacer(modifier = Modifier.height(10.dp))

        // Total (Subtotal - Discount)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = BrandBlue,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$${String.format(Locale.getDefault(), "%.2f", uiState.total)}",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = BrandBlue,
                textAlign = TextAlign.End,
                modifier = Modifier.width(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tax 1 (optional)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tax 1 (optional)",
                fontFamily = GillSans,
                fontSize = 12.sp,
                color = Color(0xFF334155),
                modifier = Modifier.weight(1f)
            )
            USPercentInputField(
                value = uiState.tax1Percent,
                onValueChange = { onEvent(InvoiceUiEvent.Tax1PercentChanged(it)) },
                modifier = Modifier.width(76.dp).height(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "+ $${String.format(Locale.getDefault(), "%.2f", uiState.tax1Amount)}",
                fontFamily = GillSans,
                fontSize = 12.sp,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.End,
                modifier = Modifier.width(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tax 2 (optional)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tax 2 (optional)",
                fontFamily = GillSans,
                fontSize = 12.sp,
                color = Color(0xFF334155),
                modifier = Modifier.weight(1f)
            )
            USPercentInputField(
                value = uiState.tax2Percent,
                onValueChange = { onEvent(InvoiceUiEvent.Tax2PercentChanged(it)) },
                modifier = Modifier.width(76.dp).height(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "+ $${String.format(Locale.getDefault(), "%.2f", uiState.tax2Amount)}",
                fontFamily = GillSans,
                fontSize = 12.sp,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.End,
                modifier = Modifier.width(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = BrandBlue, thickness = 2.dp)
        Spacer(modifier = Modifier.height(10.dp))

        // Grand Total
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Grand Total",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = BrandBlue,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$${String.format(Locale.getDefault(), "%.2f", uiState.grandTotal)}",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = BrandBlue,
                textAlign = TextAlign.End,
                modifier = Modifier.width(80.dp)
            )
        }
    }
}

@Composable
private fun USPercentInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = { input ->
            val cleaned = input.filter { it.isDigit() || it == '.' }
            if (cleaned.count { it == '.' } <= 1) {
                val num = cleaned.toDoubleOrNull()
                if (cleaned.isEmpty() || num == null || num <= 100.0) {
                    onValueChange(cleaned)
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = GillSans,
            fontSize = 13.sp,
            color = Color(0xFF1E293B),
            textAlign = TextAlign.Start
        ),
        cursorBrush = SolidColor(BrandBlue),
        decorationBox = { innerTextField ->
            Row(
                modifier = modifier
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "%",
                    fontFamily = GillSans,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(end = 4.dp)
                )
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = "0",
                            fontFamily = GillSans,
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

// =========================================================================================
// INDIA BASED INVOICE FORM (Unchanged for India accounts)
// =========================================================================================

@Composable
private fun IndiaInvoiceFormContent(
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

    val currencySymbol = uiState.currencySymbol

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
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
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // 2. Invoice Details Grid
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
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
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
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
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
                                text = "- $currencySymbol ${String.format(Locale.getDefault(), "%.2f", uiState.calculatedDiscountAmount)}",
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
                                text = "+ $currencySymbol ${String.format(Locale.getDefault(), "%.2f", uiState.calculatedTaxAmount)}",
                                fontFamily = GillSans,
                                fontSize = 14.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 1.dp)
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

                    val saveLabel = if (uiState.mode == InvoiceMode.EDIT) "Update" else "Save"
                    val isSaveEnabled = if (uiState.mode == InvoiceMode.EDIT) uiState.hasEditChanges else true
                    Button(
                        onClick = { onEvent(InvoiceUiEvent.SaveInvoiceClicked) },
                        enabled = isSaveEnabled,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF004D87),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF004D87).copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        )
                    ) {
                        Text(saveLabel, fontFamily = GillSans, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

// =========================================================================================
// SHARED HELPER COMPOSABLES
// =========================================================================================

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
    placeholder: String = "",
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
            label = if (label.isNotBlank()) { { Text(label, fontFamily = GillSans) } } else null,
            placeholder = if (placeholder.isNotBlank()) { { Text(placeholder, fontFamily = GillSans, color = Color(0xFF94A3B8), fontSize = 12.sp) } } else null,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date",
                    tint = if (enabled) BrandBlue else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { datePickerDialog.show() },
            shape = RoundedCornerShape(6.dp),
            colors = formFieldColors(),
            singleLine = true
        )
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
private fun USDateFieldItem(
    displayDate: String,
    placeholder: String = "Select",
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

    USTextInputField(
        value = displayDate,
        onValueChange = {},
        placeholder = placeholder,
        enabled = enabled,
        readOnly = true,
        onClick = { datePickerDialog.show() },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Select Date",
                tint = if (enabled) BrandBlue else Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        },
        modifier = modifier
    )
}

@Composable
private fun USTextInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingText: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
            .background(Color(0xFFF8FAFC))
            .then(if (onClick != null && enabled) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingText != null) {
                Text(
                    text = leadingText,
                    fontFamily = GillSans,
                    fontSize = 13.sp,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        fontFamily = GillSans,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled && onClick == null,
                    readOnly = readOnly || onClick != null,
                    singleLine = singleLine,
                    keyboardOptions = keyboardOptions,
                    textStyle = TextStyle(
                        fontFamily = GillSans,
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B)
                    ),
                    cursorBrush = SolidColor(BrandBlue),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (trailingIcon != null) {
                trailingIcon()
            }
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
    focusedBorderColor = BrandBlue,
    unfocusedBorderColor = Color(0xFFCBD5E1),
    disabledBorderColor = Color(0xFFE2E8F0),
    focusedTextColor = Color(0xFF1E293B),
    unfocusedTextColor = Color(0xFF1E293B),
    disabledTextColor = Color(0xFF64748B)
)
