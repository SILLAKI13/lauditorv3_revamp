package com.digicoffer.lauditor.feature.invoice.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceClient
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceDetail
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceLineItem
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceListItem
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceMatter
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceMode
import com.digicoffer.lauditor.feature.invoice.data.repository.InvoiceRepository
import com.digicoffer.lauditor.feature.invoice.presentation.state.InvoiceUiEvent
import com.digicoffer.lauditor.feature.invoice.presentation.state.InvoiceUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InvoiceViewModel(
    private val repository: InvoiceRepository,
    private val context: Context? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState: StateFlow<InvoiceUiState> = _uiState.asStateFlow()

    init {
        loadInvoices()
        loadClients()
        loadDefaultLogo()
    }

    fun onEvent(event: InvoiceUiEvent) {
        when (event) {
            is InvoiceUiEvent.LoadInvoices -> loadInvoices()
            is InvoiceUiEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is InvoiceUiEvent.SwitchMode -> handleSwitchMode(event.mode)

            is InvoiceUiEvent.ViewDetails -> loadInvoiceDetails(event.invoice, isViewOnly = true)
            is InvoiceUiEvent.EditInvoice -> loadInvoiceDetails(event.invoice, isViewOnly = false)

            is InvoiceUiEvent.PromptChangeStatus -> handlePromptChangeStatus(event.invoice)
            is InvoiceUiEvent.ConfirmChangeStatus -> executeChangeStatus()

            is InvoiceUiEvent.PromptDeleteInvoice -> {
                _uiState.update { it.copy(deleteTarget = event.invoice) }
            }
            is InvoiceUiEvent.ConfirmDeleteInvoice -> executeDeleteInvoice()

            is InvoiceUiEvent.PromptShareInvoice -> handlePromptShare(event.invoice)
            is InvoiceUiEvent.ConfirmShareInvoice -> executeShareInvoice(event.message)

            is InvoiceUiEvent.DownloadInvoice -> executeDownloadInvoice(event.docId)
            is InvoiceUiEvent.DownloadUrlOpened -> {
                _uiState.update { it.copy(downloadUrlToOpen = null) }
            }

            // Form field events
            is InvoiceUiEvent.InvoiceCodeChanged -> {
                _uiState.update { it.copy(invoiceCode = event.code) }
            }
            is InvoiceUiEvent.InvoiceDateSelected -> {
                _uiState.update { it.copy(invoiceDate = event.display, invoiceDateApi = event.api) }
            }
            is InvoiceUiEvent.DueDateSelected -> {
                _uiState.update { it.copy(dueDate = event.display, dueDateApi = event.api) }
            }
            is InvoiceUiEvent.FirmGstChanged -> {
                _uiState.update { it.copy(firmGstNo = event.gst) }
            }
            is InvoiceUiEvent.FirmPanChanged -> {
                _uiState.update { it.copy(firmPanNo = event.pan) }
            }
            is InvoiceUiEvent.ClientGstChanged -> {
                _uiState.update { it.copy(clientGstNo = event.gst) }
            }
            is InvoiceUiEvent.ClientPanChanged -> {
                _uiState.update { it.copy(clientPanNo = event.pan) }
            }
            is InvoiceUiEvent.BillToChanged -> {
                _uiState.update { it.copy(billTo = event.address) }
            }
            is InvoiceUiEvent.NotesChanged -> {
                _uiState.update { it.copy(notes = event.notes) }
            }
            is InvoiceUiEvent.CurrencySelected -> {
                _uiState.update { it.copy(currencyCode = event.currency) }
            }
            is InvoiceUiEvent.LogoSelected -> {
                _uiState.update {
                    it.copy(
                        logoUri = event.uri,
                        includeLogo = true,
                        hasPendingLogoUpload = true
                    )
                }
            }
            is InvoiceUiEvent.RemoveLogo -> {
                _uiState.update {
                    it.copy(
                        logoUri = null,
                        logoUrl = "",
                        includeLogo = false,
                        hasPendingLogoUpload = false
                    )
                }
            }
            is InvoiceUiEvent.ClientSelected -> handleClientSelected(event.client)
            is InvoiceUiEvent.MatterSelected -> {
                _uiState.update { it.copy(selectedMatter = event.matter) }
            }
            is InvoiceUiEvent.AddLineItem -> {
                val updated = _uiState.value.lineItems.toMutableList()
                updated.add(InvoiceLineItem())
                _uiState.update { it.copy(lineItems = updated) }
            }
            is InvoiceUiEvent.RemoveLineItem -> {
                val current = _uiState.value.lineItems
                if (current.size > 1 && event.index in current.indices) {
                    val updated = current.toMutableList().apply { removeAt(event.index) }
                    _uiState.update { it.copy(lineItems = updated) }
                }
            }
            is InvoiceUiEvent.UpdateLineItem -> {
                val current = _uiState.value.lineItems
                if (event.index in current.indices) {
                    val updated = current.toMutableList().apply { set(event.index, event.item) }
                    _uiState.update { it.copy(lineItems = updated) }
                }
            }
            is InvoiceUiEvent.DiscountPercentChanged -> {
                _uiState.update { it.copy(discountPercent = event.discount) }
            }
            is InvoiceUiEvent.TaxPercentChanged -> {
                _uiState.update { it.copy(taxPercent = event.tax) }
            }
            is InvoiceUiEvent.SaveInvoiceClicked -> handleSaveInvoiceClicked()
            is InvoiceUiEvent.ConfirmEditChoice -> executeSaveInvoice(event.replaceOriginal)
            is InvoiceUiEvent.DismissDialogs -> {
                _uiState.update {
                    it.copy(
                        statusChangeTarget = null,
                        deleteTarget = null,
                        shareTarget = null,
                        showReplaceConfirmDialog = false,
                        alertMessage = null,
                        successMessage = null
                    )
                }
            }
        }
    }

    private fun loadInvoices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.getInvoiceList()
            result.fold(
                onSuccess = { list ->
                    _uiState.update { it.copy(isLoading = false, invoices = list) }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            alertMessage = err.message ?: "Failed to load invoices"
                        )
                    }
                }
            )
        }
    }

    private fun loadClients() {
        viewModelScope.launch {
            val result = repository.getClients()
            result.onSuccess { clients ->
                _uiState.update { it.copy(availableClients = clients) }
            }
        }
    }

    private fun loadDefaultLogo() {
        viewModelScope.launch {
            val result = repository.getInvoiceLogo()
            result.onSuccess { url ->
                if (url.isNotEmpty()) {
                    _uiState.update { it.copy(logoUrl = url, includeLogo = true) }
                }
            }
        }
    }

    private fun handleSwitchMode(targetMode: InvoiceMode) {
        when (targetMode) {
            InvoiceMode.LIST -> {
                _uiState.update {
                    it.copy(
                        mode = InvoiceMode.LIST,
                        editingInvoiceId = null,
                        editingDocId = null
                    )
                }
                loadInvoices()
            }
            InvoiceMode.CREATE -> {
                val sdfDisplay = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
                val sdfApi = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                val today = Date()

                _uiState.update {
                    it.copy(
                        mode = InvoiceMode.CREATE,
                        editingInvoiceId = null,
                        editingDocId = null,
                        invoiceDate = sdfDisplay.format(today),
                        invoiceDateApi = sdfApi.format(today),
                        dueDate = "",
                        dueDateApi = "",
                        firmGstNo = "",
                        firmPanNo = "",
                        clientGstNo = "",
                        clientPanNo = "",
                        billTo = "",
                        notes = "",
                        currencyCode = "INR",
                        includeLogo = true,
                        logoUri = null,
                        hasPendingLogoUpload = false,
                        selectedClient = null,
                        selectedMatter = null,
                        availableMatters = emptyList(),
                        lineItems = listOf(InvoiceLineItem()),
                        discountPercent = "",
                        taxPercent = ""
                    )
                }
                fetchSuggestedInvoiceId()
                loadDefaultLogo()
            }
            InvoiceMode.EDIT, InvoiceMode.VIEW -> {
                _uiState.update { it.copy(mode = targetMode) }
            }
        }
    }

    private fun fetchSuggestedInvoiceId() {
        viewModelScope.launch {
            val result = repository.suggestInvoiceId()
            result.onSuccess { (code, seq) ->
                _uiState.update { it.copy(invoiceCode = code, invoiceSequence = seq) }
            }
        }
    }

    private fun loadInvoiceDetails(item: InvoiceListItem, isViewOnly: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.getInvoiceDetail(item.id)
            result.fold(
                onSuccess = { detail ->
                    val client = _uiState.value.availableClients.find { it.id == detail.clientId }
                        ?: if (detail.clientId.isNotEmpty()) {
                            InvoiceClient(detail.clientId, detail.name, detail.clientType)
                        } else null

                    val matter = if (detail.matterId.isNotEmpty()) {
                        InvoiceMatter(detail.matterId, detail.matterName, detail.matterType)
                    } else null

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            mode = if (isViewOnly) InvoiceMode.VIEW else InvoiceMode.EDIT,
                            editingInvoiceId = detail.id,
                            editingDocId = detail.docId,
                            invoiceCode = detail.invoiceNo,
                            invoiceSequence = detail.invoiceSequence,
                            invoiceDate = detail.date,
                            invoiceDateApi = detail.date,
                            dueDate = detail.dueDate,
                            dueDateApi = detail.dueDate,
                            firmGstNo = detail.firmGstNo,
                            firmPanNo = detail.firmPanNo,
                            clientGstNo = detail.clientGstNo,
                            clientPanNo = detail.clientPanNo,
                            billTo = detail.billTo,
                            notes = detail.notes,
                            currencyCode = if (detail.currencyCode.isNotEmpty()) detail.currencyCode else "INR",
                            includeLogo = detail.includeLogo,
                            logoUrl = detail.logoUrl,
                            logoUri = null,
                            hasPendingLogoUpload = false,
                            selectedClient = client,
                            selectedMatter = matter,
                            lineItems = if (detail.lineItems.isNotEmpty()) detail.lineItems else listOf(InvoiceLineItem()),
                            discountPercent = if (detail.discountPercent > 0) detail.discountPercent.toInt().toString() else "",
                            taxPercent = if (detail.taxPercent > 0) detail.taxPercent.toInt().toString() else ""
                        )
                    }

                    if (client != null) {
                        loadMattersForClient(client.id)
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            alertMessage = err.message ?: "Failed to load invoice details"
                        )
                    }
                }
            )
        }
    }

    private fun handleClientSelected(client: InvoiceClient?) {
        _uiState.update {
            it.copy(
                selectedClient = client,
                selectedMatter = null,
                availableMatters = emptyList()
            )
        }
        if (client != null) {
            loadMattersForClient(client.id)
        }
    }

    private fun loadMattersForClient(clientId: String) {
        viewModelScope.launch {
            val result = repository.getMatters(clientId)
            result.onSuccess { matters ->
                _uiState.update { it.copy(availableMatters = matters) }
            }
        }
    }

    private fun handlePromptChangeStatus(invoice: InvoiceListItem) {
        val current = invoice.status.lowercase(Locale.ROOT)
        val targetStatus = when (current) {
            "raised" -> "collected"
            "collected" -> "raised"
            "cancelled" -> "raised"
            else -> "raised"
        }
        _uiState.update { it.copy(statusChangeTarget = Pair(invoice, targetStatus)) }
    }

    private fun executeChangeStatus() {
        val target = _uiState.value.statusChangeTarget ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusChangeTarget = null) }
            val result = repository.changeInvoiceStatus(target.first.id, target.second)
            result.fold(
                onSuccess = { msg ->
                    _uiState.update { it.copy(isLoading = false, successMessage = msg) }
                    loadInvoices()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            alertMessage = err.message ?: "Failed to update status"
                        )
                    }
                }
            )
        }
    }

    private fun executeDeleteInvoice() {
        val target = _uiState.value.deleteTarget ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, deleteTarget = null) }
            val result = repository.deleteInvoice(target.id)
            result.fold(
                onSuccess = { msg ->
                    _uiState.update { it.copy(isLoading = false, successMessage = msg) }
                    loadInvoices()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            alertMessage = err.message ?: "Failed to delete invoice"
                        )
                    }
                }
            )
        }
    }

    private fun handlePromptShare(invoice: InvoiceListItem) {
        if (!invoice.canShare) {
            _uiState.update { it.copy(alertMessage = "You do not have permission to share this invoice.") }
            return
        }
        if (invoice.relId.isEmpty()) {
            _uiState.update { it.copy(alertMessage = "No relationship ID found for this invoice.") }
            return
        }
        if (invoice.docId.isEmpty()) {
            _uiState.update { it.copy(alertMessage = "No document ID found for this invoice.") }
            return
        }
        _uiState.update { it.copy(shareTarget = invoice) }
    }

    private fun executeShareInvoice(message: String) {
        val target = _uiState.value.shareTarget ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, shareTarget = null) }
            val result = repository.shareInvoice(target.relId, target.docId, message)
            result.fold(
                onSuccess = { msg ->
                    _uiState.update { it.copy(isLoading = false, successMessage = msg) }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            alertMessage = err.message ?: "Failed to share invoice"
                        )
                    }
                }
            )
        }
    }

    private fun executeDownloadInvoice(docId: String) {
        if (docId.isEmpty()) {
            _uiState.update { it.copy(alertMessage = "Invoice document ID not found.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.getDownloadInvoiceUrl(docId)
            result.fold(
                onSuccess = { url ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            downloadUrlToOpen = url,
                            successMessage = "Download link ready. Opening document..."
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            alertMessage = err.message ?: "Failed to download invoice"
                        )
                    }
                }
            )
        }
    }

    private fun handleSaveInvoiceClicked() {
        val s = _uiState.value
        if (s.invoiceDate.isBlank()) {
            _uiState.update { it.copy(alertMessage = "Please select Invoice Date.") }
            return
        }
        if (s.dueDate.isBlank()) {
            _uiState.update { it.copy(alertMessage = "Please select Due Date.") }
            return
        }
        if (s.selectedClient == null) {
            _uiState.update { it.copy(alertMessage = "Please select a Client.") }
            return
        }
        if (s.billTo.isBlank()) {
            _uiState.update { it.copy(alertMessage = "Please enter Client Address.") }
            return
        }
        val validItems = s.lineItems.filter { it.name.isNotBlank() || it.unitPrice > 0 }
        if (validItems.isEmpty()) {
            _uiState.update { it.copy(alertMessage = "Please add at least one line item with description.") }
            return
        }

        if (s.mode == InvoiceMode.EDIT) {
            _uiState.update { it.copy(showReplaceConfirmDialog = true) }
        } else {
            executeSaveInvoice(replaceOriginal = false)
        }
    }

    private fun executeSaveInvoice(replaceOriginal: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showReplaceConfirmDialog = false) }

            // 1. Upload logo if pending
            if (_uiState.value.hasPendingLogoUpload && _uiState.value.logoUri != null && context != null) {
                try {
                    val file = uriToFile(context, _uiState.value.logoUri!!)
                    if (file != null) {
                        repository.uploadLogo(file)
                    }
                } catch (e: Exception) {
                    // Non-fatal, proceed with invoice save
                }
            }

            // 2. Build payload
            val s = _uiState.value
            val payload = JSONObject()

            payload.put("invoice_code", s.invoiceCode)
            payload.put("firm_gst_no", s.firmGstNo)
            payload.put("firm_pan_no", s.firmPanNo)
            payload.put("client_gst_no", s.clientGstNo)
            payload.put("client_pan_no", s.clientPanNo)
            payload.put("name", s.selectedClient?.name ?: "Invoice")
            payload.put("billto", s.billTo)
            payload.put("notes", s.notes)
            payload.put("currency_code", s.currencyCode)
            payload.put("include_logo", s.includeLogo)

            if (s.mode != InvoiceMode.EDIT) {
                payload.put("invoice_sequence", s.invoiceSequence)
            } else {
                payload.put("replace_original", replaceOriginal)
            }

            val infoObj = JSONObject().apply {
                put("date", if (s.invoiceDateApi.isNotBlank()) s.invoiceDateApi else s.invoiceDate)
                put("duedate", if (s.dueDateApi.isNotBlank()) s.dueDateApi else s.dueDate)
            }
            payload.put("info", infoObj)

            val invoiceItems = JSONArray()
            s.lineItems.forEach { item ->
                if (item.name.isNotBlank() || item.unitPrice > 0) {
                    val itObj = JSONObject().apply {
                        put("name", item.name)
                        put("unitPrice", item.unitPrice.toInt())
                        put("quantity", if (item.quantity > 0) item.quantity else 1)
                    }
                    invoiceItems.put(itObj)
                }
            }
            payload.put("invoice_items", invoiceItems)

            val taxObj = JSONObject().apply {
                put("tax_type", "Tax")
                put("amount", s.subTotal)
                put("tax", s.parsedTaxPct)
            }
            payload.put("tax_items", JSONArray().put(taxObj))

            val discObj = JSONObject().apply {
                put("discount_type", "Discount")
                put("amount", s.subTotal)
                put("discount", s.parsedDiscountPct)
            }
            payload.put("discounts", JSONArray().put(discObj))

            val clientObj = JSONObject().apply {
                put("id", s.selectedClient?.id ?: "")
                put("type", s.selectedClient?.type ?: "consumer")
            }
            payload.put("client", JSONArray().put(clientObj))

            val mattersArr = JSONArray()
            if (s.selectedMatter != null) {
                val mObj = JSONObject().apply {
                    put("id", s.selectedMatter.id)
                    put("name", s.selectedMatter.title)
                    put("type", s.selectedMatter.type)
                }
                mattersArr.put(mObj)
            }
            payload.put("matters", mattersArr)

            // 3. Dispatch Create or Patch
            val result = if (s.mode == InvoiceMode.EDIT && s.editingInvoiceId != null) {
                repository.patchInvoice(s.editingInvoiceId, payload)
            } else {
                repository.createInvoice(payload)
            }

            result.fold(
                onSuccess = { msg ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = msg,
                            mode = InvoiceMode.LIST
                        )
                    }
                    loadInvoices()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            alertMessage = err.message ?: "Failed to save invoice"
                        )
                    }
                }
            )
        }
    }

    private fun uriToFile(ctx: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream? = ctx.contentResolver.openInputStream(uri)
            val file = File(ctx.cacheDir, "inv_logo_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            outputStream.flush()
            outputStream.close()
            inputStream?.close()
            file
        } catch (e: Exception) {
            null
        }
    }
}
