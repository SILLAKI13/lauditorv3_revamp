package com.digicoffer.lauditor.feature.invoice.presentation.state

import android.net.Uri
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceClient
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceLineItem
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceListItem
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceMatter
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceMode

sealed interface InvoiceUiEvent {
    object LoadInvoices : InvoiceUiEvent
    data class SearchQueryChanged(val query: String) : InvoiceUiEvent
    data class SwitchMode(val mode: InvoiceMode) : InvoiceUiEvent

    data class ViewDetails(val invoice: InvoiceListItem) : InvoiceUiEvent
    data class EditInvoice(val invoice: InvoiceListItem) : InvoiceUiEvent
    data class SendReminder(val invoice: InvoiceListItem) : InvoiceUiEvent
    data class PromptChangeStatus(val invoice: InvoiceListItem) : InvoiceUiEvent
    object ConfirmChangeStatus : InvoiceUiEvent
    data class PromptDeleteInvoice(val invoice: InvoiceListItem) : InvoiceUiEvent
    object ConfirmDeleteInvoice : InvoiceUiEvent
    data class PromptShareInvoice(val invoice: InvoiceListItem) : InvoiceUiEvent
    data class ConfirmShareInvoice(val message: String) : InvoiceUiEvent

    data class DownloadInvoice(val docId: String) : InvoiceUiEvent
    object DownloadUrlOpened : InvoiceUiEvent

    // Form editing
    data class InvoiceCodeChanged(val code: String) : InvoiceUiEvent
    data class InvoiceDateSelected(val display: String, val api: String) : InvoiceUiEvent
    data class DueDateSelected(val display: String, val api: String) : InvoiceUiEvent
    data class FirmGstChanged(val gst: String) : InvoiceUiEvent
    data class FirmPanChanged(val pan: String) : InvoiceUiEvent
    data class ClientGstChanged(val gst: String) : InvoiceUiEvent
    data class ClientPanChanged(val pan: String) : InvoiceUiEvent
    data class BillToChanged(val address: String) : InvoiceUiEvent
    data class NotesChanged(val notes: String) : InvoiceUiEvent
    data class CurrencySelected(val currency: String) : InvoiceUiEvent
    data class LogoSelected(val uri: Uri) : InvoiceUiEvent
    object RemoveLogo : InvoiceUiEvent
    data class ClientSelected(val client: InvoiceClient?) : InvoiceUiEvent
    data class ClientNameChanged(val name: String) : InvoiceUiEvent
    data class MatterSelected(val matter: InvoiceMatter?) : InvoiceUiEvent
    data class ClientFirstNameChanged(val name: String) : InvoiceUiEvent
    data class ClientLastNameChanged(val name: String) : InvoiceUiEvent
    data class ClientEmailChanged(val email: String) : InvoiceUiEvent
    data class CountryCodeChanged(val code: String) : InvoiceUiEvent
    data class ClientPhoneChanged(val phone: String) : InvoiceUiEvent

    // Bank Details & Taxes
    data class BankDetailsChanged(val details: String) : InvoiceUiEvent
    data class SaveBankDetailsChecked(val checked: Boolean) : InvoiceUiEvent
    data class BankDetailOptionSelected(val option: Int) : InvoiceUiEvent
    data class Tax1PercentChanged(val tax: String) : InvoiceUiEvent
    data class Tax2PercentChanged(val tax: String) : InvoiceUiEvent

    object AddLineItem : InvoiceUiEvent
    data class RemoveLineItem(val index: Int) : InvoiceUiEvent
    data class UpdateLineItem(val index: Int, val item: InvoiceLineItem) : InvoiceUiEvent
    data class DiscountPercentChanged(val discount: String) : InvoiceUiEvent
    data class TaxPercentChanged(val tax: String) : InvoiceUiEvent

    object SaveInvoiceClicked : InvoiceUiEvent
    data class ConfirmEditChoice(val replaceOriginal: Boolean) : InvoiceUiEvent

    object DismissDialogs : InvoiceUiEvent
    object DismissAlert : InvoiceUiEvent
}
