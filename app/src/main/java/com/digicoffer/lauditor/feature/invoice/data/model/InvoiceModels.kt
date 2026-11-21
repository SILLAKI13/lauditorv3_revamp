package com.digicoffer.lauditor.feature.invoice.data.model

import java.io.Serializable

enum class InvoiceMode {
    LIST,
    CREATE,
    EDIT,
    VIEW
}

data class ReminderInfo(
    val canSend: Boolean = false,
    val retryAfter: Int = 0,
    val lastSentAt: String = "",
    val count: Int = 0
) : Serializable

data class InvoiceAddress(
    val houseFlatNo: String = "",
    val street: String = "",
    val cityTown: String = "",
    val state: String = "",
    val country: String = "",
    val zipcode: String = ""
) : Serializable

data class InvoiceListItem(
    val id: String = "",
    val name: String = "",
    val invoiceNo: String = "",
    val createdBy: String = "",
    val status: String = "raised",
    val date: String = "",
    val dueDate: String = "",
    val created: String = "",
    val isDisabled: Boolean = false,
    val clientId: String = "",
    val clientType: String = "consumer",
    val docId: String = "",
    val documentStatus: String = "",
    val relId: String = "",
    val canShare: Boolean = false,
    val reminder: ReminderInfo = ReminderInfo()
) : Serializable

data class InvoiceLineItem(
    val name: String = "",
    val unitPrice: Double = 0.0,
    val quantity: Int = 1,
    val rateText: String = if (unitPrice > 0.0) {
        if (unitPrice % 1.0 == 0.0) unitPrice.toInt().toString() else unitPrice.toString()
    } else "",
    val quantityText: String = if (quantity > 0) quantity.toString() else "1"
) : Serializable {
    val amount: Double
        get() {
            val effectiveQty = if (quantity > 0) quantity else (quantityText.toIntOrNull() ?: 1)
            return unitPrice * effectiveQty
        }
}

data class InvoiceDetail(
    val id: String = "",
    val name: String = "",
    val invoiceNo: String = "",
    val invoiceSequence: Int = 0,
    val status: String = "",
    val firmGstNo: String = "",
    val firmPanNo: String = "",
    val clientGstNo: String = "",
    val clientPanNo: String = "",
    val billTo: String = "",
    val notes: String = "",
    val currencyCode: String = "USD",
    val includeLogo: Boolean = true,
    val logoUrl: String = "",
    val date: String = "",
    val dueDate: String = "",
    val lineItems: List<InvoiceLineItem> = emptyList(),
    val taxPercent: Double = 0.0,
    val taxAmount: Double = 0.0,
    val tax1Percent: Double = 0.0,
    val tax2Percent: Double = 0.0,
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    val clientId: String = "",
    val clientType: String = "consumer",
    val clientContactName: String = "",
    val clientContactEmail: String = "",
    val clientContactPhone: String = "",
    val countryCode: String = "",
    val bankDetails: String = "",
    val bankDetailsSaved: Boolean = false,
    val reminder: ReminderInfo = ReminderInfo(),
    val fromAddress: InvoiceAddress = InvoiceAddress(),
    val matterId: String = "",
    val matterName: String = "",
    val matterType: String = "",
    val docId: String = "",
    val documentStatus: String = "",
    val relId: String = "",
    val canShare: Boolean = false
) : Serializable

data class InvoiceClient(
    val id: String,
    val name: String,
    val type: String = "consumer",
    val email: String = "",
    val phone: String = "",
    val countryCode: String = ""
) : Serializable

data class InvoiceMatter(
    val id: String,
    val title: String,
    val type: String = "legal"
) : Serializable
