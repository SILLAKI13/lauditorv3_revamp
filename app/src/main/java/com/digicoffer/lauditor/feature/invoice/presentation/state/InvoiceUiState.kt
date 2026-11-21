package com.digicoffer.lauditor.feature.invoice.presentation.state

import android.net.Uri
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceAddress
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceClient
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceLineItem
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceListItem
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceMatter
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceMode
import com.digicoffer.lauditor.feature.invoice.data.model.ReminderInfo

data class InvoiceEditSnapshot(
    val invoiceDate: String = "",
    val dueDate: String = "",
    val clientName: String = "",
    val clientEmail: String = "",
    val clientPhone: String = "",
    val countryCode: String = "+1",
    val billTo: String = "",
    val notes: String = "",
    val lineItems: List<InvoiceLineItem> = emptyList(),
    val discountPercent: String = "",
    val tax1Percent: String = "",
    val tax2Percent: String = "",
    val taxPercent: String = "",
    val bankDetails: String = "",
    val saveBankDetailsForFuture: Boolean = false,
    val hasPendingLogoUpload: Boolean = false
)

data class InvoiceUiState(
    val isLoading: Boolean = false,
    val invoices: List<InvoiceListItem> = emptyList(),
    val searchQuery: String = "",
    val mode: InvoiceMode = InvoiceMode.LIST,
    val editingInvoiceId: String? = null,
    val editingDocId: String? = null,
    val isUSAccount: Boolean = true,

    // Form fields
    val invoiceCode: String = "",
    val invoiceSequence: Int = 0,
    val invoiceDate: String = "",
    val invoiceDateApi: String = "",
    val dueDate: String = "",
    val dueDateApi: String = "",
    val firmGstNo: String = "",
    val firmPanNo: String = "",
    val clientGstNo: String = "",
    val clientPanNo: String = "",
    val billTo: String = "",
    val notes: String = "",
    val currencyCode: String = "USD",
    val includeLogo: Boolean = true,
    val logoUrl: String = "",
    val logoUri: Uri? = null,
    val hasPendingLogoUpload: Boolean = false,
    val selectedClient: InvoiceClient? = null,
    val selectedMatter: InvoiceMatter? = null,
    val availableClients: List<InvoiceClient> = emptyList(),
    val availableMatters: List<InvoiceMatter> = emptyList(),
    val lineItems: List<InvoiceLineItem> = listOf(InvoiceLineItem()),
    val discountPercent: String = "",
    val discountAmount: Double = 0.0,
    val taxPercent: String = "",
    val taxAmount: Double = 0.0,
    val tax1Percent: String = "",
    val tax2Percent: String = "",

    // Client contact & bank fields
    val clientFirstName: String = "",
    val clientLastName: String = "",
    val clientEmail: String = "",
    val countryCode: String = "+1",
    val clientPhone: String = "",
    val clientContactName: String = "",
    val clientContactEmail: String = "",
    val clientContactPhone: String = "",

    val bankDetails: String = "",
    val saveBankDetailsForFuture: Boolean = false,
    val selectedBankDetailOption: Int = 0, // 0 = Saved / From profile, 1 = Free text / Add new
    val savedBankDetailsTitle: String = "Chase Bank Branch ••6789",
    val savedBankDetailsContent: String = "Chase Bank Branch\nAccount name: Gokberk US Lawyer\nAccount no.: 000123456789\nRouting no.: 021000021\nAccount type: Checking\nSWIFT: CHASUS33\nBank address: 270 Park Avenue, New York, NY 10017",

    val reminder: ReminderInfo = ReminderInfo(),
    val fromAddress: InvoiceAddress = InvoiceAddress(),
    val footerWebsite: String = "www.dev.lauditor.com",
    val footerEmail: String = "",

    // Dialog states
    val statusChangeTarget: Pair<InvoiceListItem, String>? = null,
    val deleteTarget: InvoiceListItem? = null,
    val shareTarget: InvoiceListItem? = null,
    val showReplaceConfirmDialog: Boolean = false,
    val initialEditSnapshot: InvoiceEditSnapshot? = null,
    val alertMessage: String? = null,
    val successMessage: String? = null,
    val downloadUrlToOpen: String? = null
) {
    val subTotal: Double
        get() = lineItems.sumOf { it.amount }

    val hasEditChanges: Boolean
        get() {
            val snap = initialEditSnapshot ?: return false
            val currentClientName = (clientContactName.ifBlank { "$clientFirstName $clientLastName".trim() }).ifBlank { selectedClient?.name ?: "" }
            val currentEmail = clientContactEmail.ifBlank { clientEmail }
            val currentPhone = clientContactPhone.ifBlank { clientPhone }
            return snap.invoiceDate != invoiceDate ||
                    snap.dueDate != dueDate ||
                    snap.clientName != currentClientName ||
                    snap.clientEmail != currentEmail ||
                    snap.clientPhone != currentPhone ||
                    snap.countryCode != countryCode ||
                    snap.billTo != billTo ||
                    snap.notes != notes ||
                    snap.discountPercent != discountPercent ||
                    snap.tax1Percent != tax1Percent ||
                    snap.tax2Percent != tax2Percent ||
                    snap.taxPercent != taxPercent ||
                    snap.bankDetails != bankDetails ||
                    snap.saveBankDetailsForFuture != saveBankDetailsForFuture ||
                    snap.hasPendingLogoUpload != hasPendingLogoUpload ||
                    snap.lineItems != lineItems
        }

    val parsedDiscountPct: Double
        get() = discountPercent.toDoubleOrNull() ?: 0.0

    val calculatedDiscountAmount: Double
        get() = if (mode == InvoiceMode.VIEW && discountAmount > 0.0) {
            discountAmount
        } else {
            subTotal * parsedDiscountPct / 100.0
        }

    val afterDiscount: Double
        get() = (subTotal - calculatedDiscountAmount).coerceAtLeast(0.0)

    val parsedTaxPct: Double
        get() = taxPercent.toDoubleOrNull() ?: 0.0

    val calculatedTaxAmount: Double
        get() = if (mode == InvoiceMode.VIEW && taxAmount > 0.0) {
            taxAmount
        } else {
            afterDiscount * parsedTaxPct / 100.0
        }

    val parsedTax1Pct: Double
        get() = tax1Percent.toDoubleOrNull() ?: 0.0

    val tax1Amount: Double
        get() = afterDiscount * parsedTax1Pct / 100.0

    val parsedTax2Pct: Double
        get() = tax2Percent.toDoubleOrNull() ?: 0.0

    val tax2Amount: Double
        get() = afterDiscount * parsedTax2Pct / 100.0

    val total: Double
        get() = if (isUSAccount) afterDiscount else (afterDiscount + calculatedTaxAmount)

    val grandTotal: Double
        get() = if (isUSAccount) {
            afterDiscount + tax1Amount + tax2Amount
        } else {
            total
        }

    val currencySymbol: String
        get() = if (isUSAccount) "$" else when (currencyCode.uppercase()) {
            "USD" -> "$"
            "INR" -> "₹"
            "EUR" -> "€"
            "GBP" -> "£"
            "AUD" -> "A$"
            "CAD" -> "C$"
            "JPY" -> "¥"
            "CHF" -> "Fr"
            "BHD" -> "BD"
            "KWD" -> "KD"
            else -> "$currencyCode "
        }

    val filteredInvoices: List<InvoiceListItem>
        get() {
            val q = searchQuery.trim().lowercase()
            if (q.isEmpty()) return invoices
            return invoices.filter {
                it.name.lowercase().contains(q) ||
                        it.invoiceNo.lowercase().contains(q) ||
                        it.createdBy.lowercase().contains(q) ||
                        it.status.lowercase().contains(q)
            }
        }
}
