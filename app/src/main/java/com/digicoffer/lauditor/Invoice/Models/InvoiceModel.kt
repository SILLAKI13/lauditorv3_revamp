package com.digicoffer.lauditor.Invoice.Models

import java.io.Serializable
import java.util.ArrayList

class InvoiceModel : Serializable {
    var id: String = ""
    var name: String = ""
    var invoice_no: String = ""
    var createdby: String = ""
    var status: String = ""
    var date: String = ""
    var dueDate: String = ""
    var created: String = ""

    @get:JvmName("isIsdisabled")
    @set:JvmName("setIsdisabled")
    var isdisabled: Boolean = false

    var client_id: String = ""
    var MatterId: String = ""
    var MatterName: String = ""
    var matter_type: String = ""
    var client_type: String = "consumer"
    var docid: String = ""
    var document_status: String = ""
    var rel_id: String = ""

    @get:JvmName("isCan_share")
    @set:JvmName("setCan_share")
    var can_share: Boolean = false

    var invoiceCode: String = ""
    var invoiceSequence: Int = 0
    var firmGstNo: String = ""
    var firmPanNo: String = ""
    var clientGstNo: String = ""
    var clientPanNo: String = ""
    var billto: String = ""
    var notes: String = ""
    var lineItems: ArrayList<LineItemModel> = ArrayList()
    var currencyCode: String = "INR"
    var taxAmount: Double = 0.0
    var discountAmount: Double = 0.0

    @get:JvmName("isIncludeLogo")
    @set:JvmName("setIncludeLogo")
    var includeLogo: Boolean = true

    var invoiceId: String = ""
    var documentId: String = ""

    var logoUrl: String? = ""
        get() = field ?: ""
        set(value) {
            field = value ?: ""
        }

    constructor()
}
