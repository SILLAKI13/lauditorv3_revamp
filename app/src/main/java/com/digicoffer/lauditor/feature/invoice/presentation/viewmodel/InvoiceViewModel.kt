package com.digicoffer.lauditor.feature.invoice.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceAddress
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceClient
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceDetail
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceLineItem
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceListItem
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceMatter
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceMode
import com.digicoffer.lauditor.feature.invoice.data.model.ReminderInfo
import com.digicoffer.lauditor.feature.invoice.data.repository.InvoiceRepository
import com.digicoffer.lauditor.feature.invoice.presentation.state.InvoiceEditSnapshot
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

    private val isIndiaAccount: Boolean
        get() = Constants.isIndiaCountry(Constants.Country) ||
                Constants.CurrencySymbol == "₹" ||
                Constants.CountryCode == "+91"

    private val isUS: Boolean
        get() = !isIndiaAccount

    private val _uiState = MutableStateFlow(InvoiceUiState(isUSAccount = !isIndiaAccount))
    val uiState: StateFlow<InvoiceUiState> = _uiState.asStateFlow()

    private fun toApiDateFormat(dateStr: String): String {
        if (dateStr.isBlank()) return ""
        val trimmed = dateStr.trim()
        if (trimmed.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
            return trimmed
        }
        val formats = listOf(
            "MMM dd, yyyy",
            "MMM d, yyyy",
            "dd-MM-yyyy",
            "dd/MM/yyyy",
            "MM/dd/yyyy",
            "yyyy/MM/dd",
            "yyyy-MM-dd",
            "d MMM yyyy",
            "dd MMM yyyy"
        )
        for (fmt in formats) {
            try {
                val sdf = SimpleDateFormat(fmt, Locale.ENGLISH)
                sdf.isLenient = false
                val parsed = sdf.parse(trimmed)
                if (parsed != null) {
                    val outSdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                    return outSdf.format(parsed)
                }
            } catch (_: Exception) {}
        }
        return trimmed
    }

    private fun toDisplayDateFormat(dateStr: String): String {
        if (dateStr.isBlank()) return ""
        val trimmed = dateStr.trim()
        val formats = listOf(
            "yyyy-MM-dd",
            "dd-MM-yyyy",
            "dd/MM/yyyy",
            "MM/dd/yyyy",
            "yyyy/MM/dd",
            "MMM dd, yyyy",
            "MMM d, yyyy"
        )
        for (fmt in formats) {
            try {
                val sdf = SimpleDateFormat(fmt, Locale.ENGLISH)
                sdf.isLenient = false
                val parsed = sdf.parse(trimmed)
                if (parsed != null) {
                    val outSdf = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
                    return outSdf.format(parsed)
                }
            } catch (_: Exception) {}
        }
        return trimmed
    }

    init {
        _uiState.update { it.copy(isUSAccount = isUS) }
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
            is InvoiceUiEvent.SendReminder -> executeSendReminder(event.invoice)

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
            is InvoiceUiEvent.ClientNameChanged -> handleClientNameChanged(event.name)
            is InvoiceUiEvent.MatterSelected -> {
                _uiState.update { it.copy(selectedMatter = event.matter) }
            }
            is InvoiceUiEvent.ClientFirstNameChanged -> {
                _uiState.update { it.copy(clientFirstName = event.name) }
            }
            is InvoiceUiEvent.ClientLastNameChanged -> {
                _uiState.update { it.copy(clientLastName = event.name) }
            }
            is InvoiceUiEvent.ClientEmailChanged -> {
                _uiState.update { it.copy(clientEmail = event.email) }
            }
            is InvoiceUiEvent.CountryCodeChanged -> {
                val formattedCode = if (event.code.startsWith("+")) event.code else "+${event.code}"
                _uiState.update {
                    it.copy(
                        countryCode = formattedCode,
                        clientContactPhone = if (it.clientPhone.isNotBlank()) "$formattedCode ${it.clientPhone}" else ""
                    )
                }
            }
            is InvoiceUiEvent.ClientPhoneChanged -> {
                val digits = event.phone.filter { it.isDigit() }.take(10)
                _uiState.update {
                    it.copy(
                        clientPhone = digits,
                        clientContactPhone = if (digits.isNotBlank()) "${it.countryCode} $digits" else ""
                    )
                }
            }

            // Bank Details & Taxes
            is InvoiceUiEvent.BankDetailsChanged -> {
                val clamped = if (event.details.length > 500) event.details.take(500) else event.details
                _uiState.update { it.copy(bankDetails = clamped) }
            }
            is InvoiceUiEvent.SaveBankDetailsChecked -> {
                _uiState.update { it.copy(saveBankDetailsForFuture = event.checked) }
            }
            is InvoiceUiEvent.BankDetailOptionSelected -> handleBankDetailOptionSelected(event.option)
            is InvoiceUiEvent.Tax1PercentChanged -> {
                _uiState.update { it.copy(tax1Percent = event.tax) }
            }
            is InvoiceUiEvent.Tax2PercentChanged -> {
                _uiState.update { it.copy(tax2Percent = event.tax) }
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
            is InvoiceUiEvent.DismissAlert -> {
                _uiState.update { it.copy(alertMessage = null) }
            }
        }
    }

    private var consumerSearchJob: kotlinx.coroutines.Job? = null

    private fun handleClientNameChanged(name: String) {
        val parts = name.trim().split(" ", limit = 2)
        _uiState.update {
            it.copy(
                clientContactName = name,
                clientFirstName = parts.getOrNull(0) ?: name,
                clientLastName = parts.getOrNull(1) ?: ""
            )
        }
        if (isUS) {
            consumerSearchJob?.cancel()
            if (name.trim().isNotEmpty()) {
                consumerSearchJob = viewModelScope.launch {
                    kotlinx.coroutines.delay(250)
                    val result = repository.getConsumers(name.trim())
                    result.onSuccess { consumers ->
                        _uiState.update { it.copy(availableClients = consumers) }
                    }
                }
            }
        }
    }

    private fun handleBankDetailOptionSelected(option: Int) {
        _uiState.update {
            if (option == 0) {
                it.copy(
                    selectedBankDetailOption = 0,
                    bankDetails = it.savedBankDetailsContent.take(500)
                )
            } else {
                it.copy(
                    selectedBankDetailOption = 1,
                    bankDetails = ""
                )
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
            if (isUS) {
                val consumerResult = repository.getConsumers("")
                consumerResult.fold(
                    onSuccess = { consumers ->
                        if (consumers.isNotEmpty()) {
                            _uiState.update { it.copy(availableClients = consumers) }
                        } else {
                            repository.getClients().onSuccess { clients ->
                                _uiState.update { it.copy(availableClients = clients) }
                            }
                        }
                    },
                    onFailure = {
                        val result = repository.getClients()
                        result.onSuccess { clients ->
                            _uiState.update { it.copy(availableClients = clients) }
                        }
                    }
                )
            } else {
                val result = repository.getClients()
                result.onSuccess { clients ->
                    _uiState.update { it.copy(availableClients = clients) }
                }
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
                        isUSAccount = isUS,
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
                        currencyCode = if (isUS) "USD" else "INR",
                        includeLogo = true,
                        logoUri = null,
                        hasPendingLogoUpload = false,
                        selectedClient = null,
                        selectedMatter = null,
                        availableMatters = emptyList(),
                        lineItems = listOf(InvoiceLineItem()),
                        discountPercent = "",
                        discountAmount = 0.0,
                        taxPercent = "",
                        taxAmount = 0.0,
                        tax1Percent = "",
                        tax2Percent = "",
                        clientFirstName = "",
                        clientLastName = "",
                        clientEmail = "",
                        countryCode = if (isUS) "+1" else "+91",
                        clientPhone = "",
                        clientContactName = "",
                        clientContactEmail = "",
                        clientContactPhone = "",
                        bankDetails = it.savedBankDetailsContent,
                        selectedBankDetailOption = 0,
                        saveBankDetailsForFuture = false,
                        reminder = ReminderInfo(),
                        fromAddress = InvoiceAddress(),
                        footerWebsite = "www.dev.lauditor.com",
                        footerEmail = Constants.Email ?: ""
                    )
                }
                fetchSuggestedInvoiceId()
                loadDefaultLogo()
                loadClients()
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

                    val cName = detail.clientContactName.ifBlank { detail.name }
                    val nameParts = cName.trim().split(" ", limit = 2)
                    val fName = nameParts.getOrNull(0) ?: ""
                    val lName = nameParts.getOrNull(1) ?: ""

                    val currentLogo = if (detail.logoUrl.isNotBlank()) {
                        detail.logoUrl
                    } else {
                        _uiState.value.logoUrl
                    }

                    val displayDate = toDisplayDateFormat(detail.date)
                    val apiDate = toApiDateFormat(detail.date)
                    val displayDueDate = toDisplayDateFormat(detail.dueDate)
                    val apiDueDate = toApiDateFormat(detail.dueDate)
                    val bankDetailStr = (detail.bankDetails.ifBlank { _uiState.value.savedBankDetailsContent }).take(500)
                    val items = if (detail.lineItems.isNotEmpty()) detail.lineItems else listOf(InvoiceLineItem())
                    val t1Str = if (detail.tax1Percent > 0.0) {
                        if (detail.tax1Percent % 1.0 == 0.0) detail.tax1Percent.toInt().toString() else detail.tax1Percent.toString()
                    } else if (detail.taxPercent > 0.0) {
                        if (detail.taxPercent % 1.0 == 0.0) detail.taxPercent.toInt().toString() else detail.taxPercent.toString()
                    } else ""
                    val t2Str = if (detail.tax2Percent > 0.0) {
                        if (detail.tax2Percent % 1.0 == 0.0) detail.tax2Percent.toInt().toString() else detail.tax2Percent.toString()
                    } else ""
                    val discStr = if (detail.discountPercent > 0.0) {
                        if (detail.discountPercent % 1.0 == 0.0) detail.discountPercent.toInt().toString() else detail.discountPercent.toString()
                    } else ""
                    val taxStr = if (detail.taxPercent > 0.0) {
                        if (detail.taxPercent % 1.0 == 0.0) detail.taxPercent.toInt().toString() else detail.taxPercent.toString()
                    } else ""

                    val rawPhone = detail.clientContactPhone
                    val (detectedCode, phoneDigits) = extractPhoneAndCountryCode(
                        rawPhone = rawPhone,
                        explicitCountryCode = detail.countryCode,
                        fallbackCountryCode = if (isUS) "+1" else "+91"
                    )

                    val snapshot = if (!isViewOnly) {
                        InvoiceEditSnapshot(
                            invoiceDate = displayDate,
                            dueDate = displayDueDate,
                            clientName = cName,
                            clientEmail = detail.clientContactEmail,
                            clientPhone = phoneDigits,
                            countryCode = detectedCode,
                            billTo = detail.billTo,
                            notes = detail.notes,
                            lineItems = items.map { it.copy() },
                            discountPercent = discStr,
                            tax1Percent = t1Str,
                            tax2Percent = t2Str,
                            taxPercent = taxStr,
                            bankDetails = bankDetailStr,
                            saveBankDetailsForFuture = detail.bankDetailsSaved,
                            hasPendingLogoUpload = false
                        )
                    } else null

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            mode = if (isViewOnly) InvoiceMode.VIEW else InvoiceMode.EDIT,
                            isUSAccount = isUS,
                            editingInvoiceId = detail.id,
                            editingDocId = detail.docId,
                            invoiceCode = detail.invoiceNo,
                            invoiceSequence = detail.invoiceSequence,
                            invoiceDate = displayDate,
                            invoiceDateApi = apiDate,
                            dueDate = displayDueDate,
                            dueDateApi = apiDueDate,
                            firmGstNo = detail.firmGstNo,
                            firmPanNo = detail.firmPanNo,
                            clientGstNo = detail.clientGstNo,
                            clientPanNo = detail.clientPanNo,
                            billTo = detail.billTo,
                            notes = detail.notes,
                            currencyCode = if (detail.currencyCode.isNotEmpty()) detail.currencyCode else (if (isUS) "USD" else "INR"),
                            includeLogo = detail.includeLogo,
                            logoUrl = currentLogo,
                            logoUri = null,
                            hasPendingLogoUpload = false,
                            selectedClient = client,
                            selectedMatter = matter,
                            lineItems = items,
                            discountPercent = discStr,
                            discountAmount = detail.discountAmount,
                            taxPercent = taxStr,
                            taxAmount = detail.taxAmount,
                            tax1Percent = t1Str,
                            tax2Percent = t2Str,
                            countryCode = detectedCode,
                            clientContactName = cName,
                            clientContactEmail = detail.clientContactEmail,
                            clientContactPhone = if (phoneDigits.isNotBlank()) "$detectedCode $phoneDigits" else "",
                            clientFirstName = fName,
                            clientLastName = lName,
                            clientEmail = detail.clientContactEmail,
                            clientPhone = phoneDigits,
                            bankDetails = bankDetailStr,
                            saveBankDetailsForFuture = detail.bankDetailsSaved,
                            initialEditSnapshot = snapshot,
                            reminder = detail.reminder,
                            fromAddress = detail.fromAddress,
                            footerWebsite = "www.dev.lauditor.com",
                            footerEmail = Constants.Email ?: ""
                        )
                    }

                    if (currentLogo.isBlank()) {
                        loadDefaultLogo()
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

    private fun executeSendReminder(invoice: InvoiceListItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.sendInvoiceReminder(invoice.id)
            result.fold(
                onSuccess = { msg ->
                    // Optimistically mark reminder as sent
                    val updatedInvoices = _uiState.value.invoices.map { item ->
                        if (item.id == invoice.id) {
                            item.copy(
                                reminder = item.reminder.copy(canSend = false, count = item.reminder.count + 1)
                            )
                        } else item
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            invoices = updatedInvoices,
                            successMessage = if (msg.isNotBlank()) msg else "Reminder sent successfully."
                        )
                    }
                    loadInvoices()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            alertMessage = err.message ?: "Failed to send reminder"
                        )
                    }
                }
            )
        }
    }

    private fun handleClientSelected(client: InvoiceClient?) {
        val name = client?.name ?: ""
        val nameParts = name.trim().split(" ", limit = 2)
        val rawPhone = client?.phone ?: ""
        val (code, digits) = extractPhoneAndCountryCode(
            rawPhone = rawPhone,
            explicitCountryCode = client?.countryCode ?: "",
            fallbackCountryCode = if (_uiState.value.isUSAccount) "+1" else "+91"
        )
        _uiState.update {
            it.copy(
                selectedClient = client,
                clientContactName = name,
                clientFirstName = nameParts.getOrNull(0) ?: "",
                clientLastName = nameParts.getOrNull(1) ?: "",
                clientEmail = if (!client?.email.isNullOrBlank()) client!!.email else it.clientEmail,
                countryCode = if (rawPhone.isNotBlank() || !client?.countryCode.isNullOrBlank()) code else it.countryCode,
                clientPhone = if (rawPhone.isNotBlank()) digits else it.clientPhone,
                clientContactEmail = if (!client?.email.isNullOrBlank()) client!!.email else it.clientContactEmail,
                clientContactPhone = if (rawPhone.isNotBlank()) "$code $digits" else it.clientContactPhone,
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
        val hasClient = s.selectedClient != null || s.clientContactName.isNotBlank() || s.clientFirstName.isNotBlank()
        if (!hasClient) {
            _uiState.update { it.copy(alertMessage = "Please enter Client Name.") }
            return
        }
        if (s.clientPhone.isNotBlank() && s.clientPhone.length != 10) {
            _uiState.update { it.copy(alertMessage = "Phone number must be 10 digits.") }
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

            val s = _uiState.value

            val fullName = s.clientContactName.ifBlank {
                "${s.clientFirstName} ${s.clientLastName}".trim()
            }.ifBlank {
                s.selectedClient?.name ?: "Invoice"
            }

            val nameParts = fullName.trim().split(" ", limit = 2)
            val firstName = nameParts.getOrNull(0) ?: fullName
            val lastName = nameParts.getOrNull(1) ?: ""

            val email = s.clientEmail.trim()
            val mobile = s.clientPhone.trim()
            val countryCode = s.countryCode.trim().ifBlank { if (s.isUSAccount) "+1" else "+91" }

            var createdClientId = s.selectedClient?.id ?: ""

            // 2. In CREATE mode, trigger client POST call to relationship/client/by-email
            if (s.mode == InvoiceMode.CREATE && email.isNotBlank()) {
                try {
                    val clientResult = repository.createClientByEmail(
                        email = email,
                        firstName = firstName,
                        lastName = lastName,
                        countryCode = countryCode,
                        mobile = mobile
                    )
                    clientResult.onSuccess { clientJson ->
                        val dataObj = clientJson.optJSONObject("data")
                            ?: clientJson.optJSONObject("client")
                            ?: clientJson
                        val newId = dataObj.optString("id", "")
                        if (newId.isNotBlank()) {
                            createdClientId = newId
                        }
                    }
                } catch (e: Exception) {
                    // Proceed to save invoice
                }
            }

            // 3. Build invoice payload
            val payload = JSONObject()

            payload.put("invoice_code", s.invoiceCode)
            if (!s.isUSAccount) {
                payload.put("firm_gst_no", s.firmGstNo)
                payload.put("firm_pan_no", s.firmPanNo)
                payload.put("client_gst_no", s.clientGstNo)
                payload.put("client_pan_no", s.clientPanNo)
                payload.put("currency_code", s.currencyCode)
            } else {
                if (s.mode != InvoiceMode.EDIT) {
                    payload.put("firm_gst_no", "")
                    payload.put("firm_pan_no", "")
                    payload.put("client_gst_no", "")
                    payload.put("client_pan_no", "")
                    payload.put("currency_code", "USD")
                }
            }
            payload.put("name", fullName)
            if (s.billTo.isNotBlank() || !s.isUSAccount) {
                payload.put("billto", s.billTo)
            }
            if (s.notes.isNotBlank() || !s.isUSAccount) {
                payload.put("notes", s.notes)
            }
            payload.put("include_logo", s.includeLogo)

            if (s.mode != InvoiceMode.EDIT) {
                payload.put("invoice_sequence", s.invoiceSequence)
            } else {
                payload.put("replace_original", replaceOriginal)
            }

            val infoObj = JSONObject().apply {
                val dateVal = toApiDateFormat(if (s.invoiceDateApi.isNotBlank()) s.invoiceDateApi else s.invoiceDate)
                val dueDateVal = toApiDateFormat(if (s.dueDateApi.isNotBlank()) s.dueDateApi else s.dueDate)
                put("date", dateVal)
                put("duedate", dueDateVal)
            }
            payload.put("info", infoObj)

            val invoiceItems = JSONArray()
            s.lineItems.forEach { item ->
                if (item.name.isNotBlank() || item.unitPrice > 0) {
                    val itObj = JSONObject().apply {
                        put("name", item.name)
                        put("unitPrice", if (item.unitPrice % 1.0 == 0.0) item.unitPrice.toInt() else item.unitPrice)
                        put("quantity", if (item.quantity > 0) item.quantity else 1)
                    }
                    invoiceItems.put(itObj)
                }
            }
            payload.put("invoice_items", invoiceItems)

            // Taxes
            val taxItems = JSONArray()
            if (s.isUSAccount) {
                val t1 = s.tax1Percent.toDoubleOrNull() ?: 0.0
                if (t1 > 0.0) {
                    taxItems.put(JSONObject().apply {
                        put("tax_type", "Tax1")
                        val amt = if (s.afterDiscount > 0.0) s.afterDiscount else s.subTotal
                        put("amount", if (amt % 1.0 == 0.0) amt.toInt() else amt)
                        put("tax", if (t1 % 1.0 == 0.0) t1.toInt() else t1)
                    })
                }
                val t2 = s.tax2Percent.toDoubleOrNull() ?: 0.0
                if (t2 > 0.0) {
                    taxItems.put(JSONObject().apply {
                        put("tax_type", "Tax2")
                        val amt = if (s.afterDiscount > 0.0) s.afterDiscount else s.subTotal
                        put("amount", if (amt % 1.0 == 0.0) amt.toInt() else amt)
                        put("tax", if (t2 % 1.0 == 0.0) t2.toInt() else t2)
                    })
                }
            } else {
                if (s.parsedTaxPct > 0.0) {
                    taxItems.put(JSONObject().apply {
                        put("tax_type", "Tax")
                        val amt = s.afterDiscount
                        put("amount", if (amt % 1.0 == 0.0) amt.toInt() else amt)
                        put("tax", if (s.parsedTaxPct % 1.0 == 0.0) s.parsedTaxPct.toInt() else s.parsedTaxPct)
                    })
                }
            }
            payload.put("tax_items", taxItems)

            if (s.parsedDiscountPct > 0.0) {
                val discObj = JSONObject().apply {
                    put("discount_type", "Discount")
                    put("amount", if (s.subTotal % 1.0 == 0.0) s.subTotal.toInt() else s.subTotal)
                    put("discount", if (s.parsedDiscountPct % 1.0 == 0.0) s.parsedDiscountPct.toInt() else s.parsedDiscountPct)
                }
                payload.put("discounts", JSONArray().put(discObj))
            }

            val clientObj = JSONObject().apply {
                put("id", createdClientId)
                put("type", s.selectedClient?.type ?: "consumer")
            }
            payload.put("client", JSONArray().put(clientObj))

            if (email.isNotBlank()) {
                payload.put("client_email", email)
            }
            if (mobile.isNotBlank()) {
                payload.put("client_phone", mobile)
            }
            val formattedCountryCode = if (countryCode.startsWith("+")) countryCode else "+$countryCode"
            payload.put("country_code", formattedCountryCode)

            val clientContact = JSONObject().apply {
                put("name", fullName)
                put("email", email)
                put("phone", mobile)
                put("country_code", formattedCountryCode)
            }
            payload.put("client_contact", clientContact)

            if (s.bankDetails.isNotBlank()) {
                payload.put("bank_details", s.bankDetails)
                payload.put("save_bank_details", s.saveBankDetailsForFuture)
                payload.put("bank_details_saved", s.saveBankDetailsForFuture)
            }

            if (s.isUSAccount) {
                val remindersObj = JSONObject().apply {
                    val daysBefore = JSONArray().apply {
                        put(45); put(30); put(15); put(7); put(3)
                    }
                    put("days_before", daysBefore)
                    put("daily_until_due", true)
                }
                payload.put("reminders", remindersObj)
            }

            val mattersArr = JSONArray()
            if (!s.isUSAccount && s.selectedMatter != null) {
                val mObj = JSONObject().apply {
                    put("id", s.selectedMatter.id)
                    put("name", s.selectedMatter.title)
                    put("type", s.selectedMatter.type)
                }
                mattersArr.put(mObj)
            }
            payload.put("matters", mattersArr)

            // 4. Dispatch Create or Patch
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
                    // Trigger invoice GET call after create
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

    private fun extractPhoneAndCountryCode(
        rawPhone: String,
        explicitCountryCode: String,
        fallbackCountryCode: String
    ): Pair<String, String> {
        val trimmedPhone = rawPhone.trim()
        val code = when {
            explicitCountryCode.isNotBlank() -> {
                if (explicitCountryCode.startsWith("+")) explicitCountryCode else "+$explicitCountryCode"
            }
            trimmedPhone.startsWith("+91") -> "+91"
            trimmedPhone.startsWith("+1") -> "+1"
            else -> fallbackCountryCode
        }

        val phoneWithoutPrefix = when {
            trimmedPhone.startsWith("+91") -> trimmedPhone.removePrefix("+91")
            trimmedPhone.startsWith("+1") -> trimmedPhone.removePrefix("+1")
            code.isNotBlank() && trimmedPhone.startsWith(code) -> trimmedPhone.removePrefix(code)
            else -> trimmedPhone
        }

        val digits = phoneWithoutPrefix.filter { it.isDigit() }.takeLast(10)
        return Pair(code, digits)
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
