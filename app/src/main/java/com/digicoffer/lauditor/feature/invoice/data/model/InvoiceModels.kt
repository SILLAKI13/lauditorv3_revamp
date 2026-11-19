package com.digicoffer.lauditor.feature.invoice.data.model

import java.io.Serializable

enum class InvoiceMode {
    LIST,
    CREATE,
    EDIT,
    VIEW
}

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
    val canShare: Boolean = false
) : Serializable

data class InvoiceLineItem(
    val name: String = "",
    val unitPrice: Double = 0.0,
    val quantity: Int = 1
) : Serializable {
    val amount: Double
        get() = unitPrice * quantity
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
    val currencyCode: String = "INR",
    val includeLogo: Boolean = true,
    val logoUrl: String = "",
    val date: String = "",
    val dueDate: String = "",
    val lineItems: List<InvoiceLineItem> = emptyList(),
    val taxPercent: Double = 0.0,
    val discountPercent: Double = 0.0,
    val clientId: String = "",
    val clientType: String = "consumer",
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
    val type: String = "consumer"
) : Serializable

data class InvoiceMatter(
    val id: String,
    val title: String,
    val type: String = "legal"
) : Serializable
