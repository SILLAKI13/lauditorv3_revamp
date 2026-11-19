package com.digicoffer.lauditor.feature.invoice.presentation.state

import android.net.Uri
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceClient
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceLineItem
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceListItem
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceMatter
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceMode

data class InvoiceUiState(
    val isLoading: Boolean = false,
    val invoices: List<InvoiceListItem> = emptyList(),
    val searchQuery: String = "",
    val mode: InvoiceMode = InvoiceMode.LIST,
    val editingInvoiceId: String? = null,
    val editingDocId: String? = null,

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
    val currencyCode: String = "INR",
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
    val taxPercent: String = "",

    // Dialog states
    val statusChangeTarget: Pair<InvoiceListItem, String>? = null,
    val deleteTarget: InvoiceListItem? = null,
    val shareTarget: InvoiceListItem? = null,
    val showReplaceConfirmDialog: Boolean = false,
    val alertMessage: String? = null,
    val successMessage: String? = null,
    val downloadUrlToOpen: String? = null
) {
    val subTotal: Double
        get() = lineItems.sumOf { it.amount }

    val parsedDiscountPct: Double
        get() = discountPercent.toDoubleOrNull() ?: 0.0

    val discountAmount: Double
        get() = subTotal * parsedDiscountPct / 100.0

    val afterDiscount: Double
        get() = subTotal - discountAmount

    val parsedTaxPct: Double
        get() = taxPercent.toDoubleOrNull() ?: 0.0

    val taxAmount: Double
        get() = afterDiscount * parsedTaxPct / 100.0

    val total: Double
        get() = afterDiscount + taxAmount

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
