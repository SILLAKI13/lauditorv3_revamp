package com.digicoffer.lauditor.feature.invoice.presentation.components

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.cards.AppCard
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceMode
import com.digicoffer.lauditor.feature.invoice.presentation.state.InvoiceUiEvent
import com.digicoffer.lauditor.feature.invoice.presentation.state.InvoiceUiState
import java.util.Locale

private val GillSans = FontFamily(Font(R.font.gill_sans_regular))
private val BrandBlue = Color(0xFF0073CF)

@Composable
fun InvoiceViewDetailsContent(
    uiState: InvoiceUiState,
    onEvent: (InvoiceUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val currencySymbol = uiState.currencySymbol
    val clientDisplayName = uiState.clientContactName.ifBlank {
        uiState.selectedClient?.name ?: ""
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
            // Main Container Card
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isTablet) 20.dp else 12.dp),
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
                    // 1. Top Section: Logo + Metadata fields
                    if (isTablet) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Logo Box (left)
                            Column(modifier = Modifier.width(130.dp)) {
                                Text(
                                    text = "LOGO",
                                    color = BrandBlue,
                                    fontFamily = GillSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                ViewLogoBox(
                                    uiState = uiState,
                                    context = context,
                                    modifier = Modifier.fillMaxWidth().height(120.dp)
                                )
                            }

                            // Metadata Fields (right)
                            Column(modifier = Modifier.weight(1f)) {
                                // Row 1: Invoice No, Invoice Date, Due Date
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ReadonlyField(
                                        label = "Invoice No",
                                        value = uiState.invoiceCode,
                                        modifier = Modifier.weight(1f)
                                    )
                                    ReadonlyField(
                                        label = "Invoice Date",
                                        value = uiState.invoiceDate,
                                        modifier = Modifier.weight(1f)
                                    )
                                    ReadonlyField(
                                        label = "Due Date",
                                        value = uiState.dueDate,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Row 2: Client name, Email, Phone
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ReadonlyField(
                                        label = "Client name",
                                        value = clientDisplayName,
                                        modifier = Modifier.weight(1f)
                                    )
                                    ReadonlyField(
                                        label = "Email",
                                        value = uiState.clientContactEmail,
                                        modifier = Modifier.weight(1f)
                                    )
                                    ReadonlyField(
                                        label = "Phone",
                                        value = uiState.clientContactPhone,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Row 3: Client Address (optional)
                                ReadonlyField(
                                    label = "Client Address (optional)",
                                    value = uiState.billTo,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        // Mobile: Stacked layout with 2-column dates
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "LOGO",
                                color = BrandBlue,
                                fontFamily = GillSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ViewLogoBox(
                                uiState = uiState,
                                context = context,
                                modifier = Modifier.size(width = 130.dp, height = 75.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            ReadonlyField(
                                label = "Invoice No",
                                value = uiState.invoiceCode,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ReadonlyField(
                                    label = "Invoice Date",
                                    value = uiState.invoiceDate,
                                    modifier = Modifier.weight(1f)
                                )
                                ReadonlyField(
                                    label = "Due Date",
                                    value = uiState.dueDate,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            ReadonlyField(
                                label = "Client name",
                                value = clientDisplayName,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ReadonlyField(
                                label = "Email",
                                value = uiState.clientContactEmail,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ReadonlyField(
                                label = "Phone",
                                value = uiState.clientContactPhone,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ReadonlyField(
                                label = "Client Address (optional)",
                                value = uiState.billTo,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 2. Service Items Section
                    Text(
                        text = "Service items",
                        color = BrandBlue,
                        fontFamily = GillSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isTablet) {
                        // Desktop/Tablet Table
                        ViewTableServiceItems(uiState = uiState, currencySymbol = currencySymbol)
                    } else {
                        // Mobile Cards for Service Items
                        ViewMobileServiceItems(uiState = uiState, currencySymbol = currencySymbol)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. Bank details + Calculation Summary
                    if (isTablet) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                ViewBankDetails(uiState = uiState)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                ViewCalculations(uiState = uiState, currencySymbol = currencySymbol)
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            ViewBankDetails(uiState = uiState)
                            Spacer(modifier = Modifier.height(16.dp))
                            ViewCalculations(uiState = uiState, currencySymbol = currencySymbol)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 4. Additional Details
                    Text(
                        text = "Additional Details",
                        color = BrandBlue,
                        fontFamily = GillSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (uiState.notes.isNotBlank()) uiState.notes else "-",
                                fontFamily = GillSans,
                                fontSize = 13.sp,
                                color = Color(0xFF1E293B)
                            )
                        }
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

                    // 6. Action buttons: Back and Download
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isTablet) Arrangement.End else Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onEvent(InvoiceUiEvent.SwitchMode(InvoiceMode.LIST)) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE2E8F0),
                                contentColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = if (isTablet) Modifier.width(130.dp).height(44.dp) else Modifier.weight(1f).height(44.dp)
                        ) {
                            Text(
                                text = "Back",
                                fontFamily = GillSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        }

                        if (isTablet) {
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        Button(
                            onClick = {
                                if (!uiState.editingDocId.isNullOrEmpty()) {
                                    onEvent(InvoiceUiEvent.DownloadInvoice(uiState.editingDocId))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = if (isTablet) Modifier.width(130.dp).height(44.dp) else Modifier.weight(1f).height(44.dp)
                        ) {
                            Text(
                                text = "Download",
                                fontFamily = GillSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))

            // 7. Footer Banner (Full Width Solid Blue Bar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandBlue)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Website: ${uiState.footerWebsite}",
                    color = Color.White,
                    fontFamily = GillSans,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                val emailText = uiState.footerEmail.ifBlank { "gokberk0404@gmail.com" }
                Text(
                    text = "Email: $emailText",
                    color = Color.White,
                    fontFamily = GillSans,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
}

@Composable
private fun ViewLogoBox(
    uiState: InvoiceUiState,
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.includeLogo && uiState.logoUrl.isNotBlank()) {
            AndroidView(
                factory = { ctx ->
                    ImageView(ctx).apply {
                        scaleType = ImageView.ScaleType.FIT_CENTER
                    }
                },
                update = { imageView ->
                    Glide.with(context)
                        .load(uiState.logoUrl)
                        .placeholder(R.drawable.rectangle_grey_background)
                        .into(imageView)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        } else {
            Text(
                text = "No Logo",
                fontFamily = GillSans,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ViewBankDetails(uiState: InvoiceUiState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Bank details for payment",
            color = BrandBlue,
            fontFamily = GillSans,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                .background(Color(0xFFF8FAFC))
                .padding(10.dp)
        ) {
            Text(
                text = if (uiState.bankDetails.isNotBlank()) uiState.bankDetails else "No bank details provided.",
                fontFamily = GillSans,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
private fun ViewCalculations(
    uiState: InvoiceUiState,
    currencySymbol: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Sub Total
        SummaryLine(
            label = "Sub Total",
            value = "$currencySymbol${String.format(Locale.getDefault(), "%.2f", uiState.subTotal)}"
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Discount
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Discount",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF1E293B)
            )
            val discPercentLabel = if (uiState.discountPercent.isNotBlank()) "${uiState.discountPercent} %" else "0 %"
            Text(
                text = discPercentLabel,
                fontFamily = GillSans,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
            Text(
                text = "- $currencySymbol${String.format(Locale.getDefault(), "%.2f", uiState.calculatedDiscountAmount)}",
                fontFamily = GillSans,
                fontSize = 13.sp,
                color = Color(0xFF1E293B)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = BrandBlue, thickness = 2.dp)
        Spacer(modifier = Modifier.height(8.dp))

        // Total
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = BrandBlue
            )
            Text(
                text = "$currencySymbol${String.format(Locale.getDefault(), "%.2f", uiState.total)}",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = BrandBlue
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = BrandBlue, thickness = 2.dp)
        Spacer(modifier = Modifier.height(8.dp))

        // Grand Total
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Grand Total",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = BrandBlue
            )
            Text(
                text = "$currencySymbol${String.format(Locale.getDefault(), "%.2f", uiState.grandTotal)}",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = BrandBlue
            )
        }
    }
}

@Composable
private fun ViewMobileServiceItems(
    uiState: InvoiceUiState,
    currencySymbol: String
) {
    if (uiState.lineItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .background(Color.White)
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No items", fontFamily = GillSans, fontSize = 13.sp, color = Color.Gray)
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            Text(
                                text = "$currencySymbol${String.format(Locale.getDefault(), "%.2f", item.amount)}",
                                fontFamily = GillSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.name.ifBlank { "-" },
                            fontFamily = GillSans,
                            fontSize = 13.sp,
                            color = Color(0xFF1E293B)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val unitPriceFormatted = if (item.unitPrice % 1.0 == 0.0) {
                            item.unitPrice.toInt().toString()
                        } else {
                            String.format(Locale.getDefault(), "%.2f", item.unitPrice)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Rate: $currencySymbol$unitPriceFormatted",
                                fontFamily = GillSans,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "Quantity: ${item.quantity}",
                                fontFamily = GillSans,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewTableServiceItems(
    uiState: InvoiceUiState,
    currencySymbol: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
    ) {
        // Header Row
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
                modifier = Modifier.weight(2f)
            )
            Text(
                text = "Rate - ${uiState.currencyCode}",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1.2f)
            )
            Text(
                text = "Quantity",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Amount",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1.2f)
            )
        }

        // Item Rows
        if (uiState.lineItems.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "No items", fontFamily = GillSans, fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            uiState.lineItems.forEachIndexed { index, item ->
                val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}",
                        fontFamily = GillSans,
                        fontSize = 12.sp,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.width(45.dp)
                    )
                    Text(
                        text = item.name.ifBlank { "-" },
                        fontFamily = GillSans,
                        fontSize = 12.sp,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.weight(2f)
                    )
                    val unitPriceFormatted = if (item.unitPrice % 1.0 == 0.0) {
                        item.unitPrice.toInt().toString()
                    } else {
                        String.format(Locale.getDefault(), "%.2f", item.unitPrice)
                    }
                    Text(
                        text = "$currencySymbol$unitPriceFormatted",
                        fontFamily = GillSans,
                        fontSize = 12.sp,
                        textAlign = TextAlign.End,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.weight(1.2f)
                    )
                    Text(
                        text = "${item.quantity}",
                        fontFamily = GillSans,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$currencySymbol${String.format(Locale.getDefault(), "%.2f", item.amount)}",
                        fontFamily = GillSans,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.weight(1.2f)
                    )
                }
                if (index < uiState.lineItems.size - 1) {
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun ReadonlyField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = BrandBlue,
            fontFamily = GillSans,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (value.isNotBlank()) value else "-",
                fontFamily = GillSans,
                fontSize = 13.sp,
                color = Color(0xFF1E293B),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun SummaryLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = GillSans,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF1E293B)
        )
        Text(
            text = value,
            fontFamily = GillSans,
            fontSize = 13.sp,
            color = Color(0xFF1E293B)
        )
    }
}
