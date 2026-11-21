package com.digicoffer.lauditor.feature.invoice.data.repository

import android.content.Context
import android.view.View
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceAddress
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceClient
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceDetail
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceLineItem
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceListItem
import com.digicoffer.lauditor.feature.invoice.data.model.InvoiceMatter
import com.digicoffer.lauditor.feature.invoice.data.model.ReminderInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.coroutines.resume

class InvoiceRepository(private val context: Context) {

    private suspend fun executeHttpRequest(
        method: WebServiceHelper.RestMethodType,
        url: String,
        requestType: String,
        payload: String = JSONObject().toString()
    ): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        continuation.resume(httpResult)
                    }
                    override fun onClick(view: View) {}
                },
                context,
                method,
                url,
                requestType,
                payload
            )
        } catch (e: Exception) {
            continuation.resume(HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Exception
                responseContent = e.message ?: "Network error"
            })
        }
    }

    private suspend fun executeHttpUploadRequest(
        url: String,
        requestType: String,
        file: File,
        vararg formParams: String
    ): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            WebServiceHelper.callHttpUploadWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        continuation.resume(httpResult)
                    }
                    override fun onClick(view: View) {}
                },
                context,
                WebServiceHelper.RestMethodType.POST,
                url,
                requestType,
                file,
                *formParams
            )
        } catch (e: Exception) {
            continuation.resume(HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Exception
                responseContent = e.message ?: "Upload error"
            })
        }
    }

    suspend fun getInvoiceList(): Result<List<InvoiceListItem>> = withContext(Dispatchers.IO) {
        try {
            val response = executeHttpRequest(
                method = WebServiceHelper.RestMethodType.GET,
                url = "v3/invoice",
                requestType = "Invoice List"
            )
            if (response.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(response.responseContent)
                if (json.optBoolean("error", false)) {
                    Result.failure(Exception(json.optString("msg", "Failed to fetch invoices")))
                } else {
                    val data = json.optJSONArray("data") ?: JSONArray()
                    val list = mutableListOf<InvoiceListItem>()
                    for (i in 0 until data.length()) {
                        val obj = data.optJSONObject(i) ?: continue
                        val status = obj.optString("status")
                        val remObj = obj.optJSONObject("reminder")
                        val reminder = if (remObj != null) {
                            ReminderInfo(
                                canSend = remObj.optBoolean("can_send", false),
                                retryAfter = remObj.optInt("retry_after", 0),
                                lastSentAt = remObj.optString("last_sent_at", ""),
                                count = remObj.optInt("count", 0)
                            )
                        } else {
                            ReminderInfo(
                                canSend = status.equals("raised", ignoreCase = true)
                            )
                        }

                        list.add(
                            InvoiceListItem(
                                id = obj.optString("id"),
                                name = obj.optString("name"),
                                invoiceNo = obj.optString("invoice_no"),
                                createdBy = obj.optString("createdby"),
                                status = status,
                                date = obj.optString("date"),
                                dueDate = obj.optString("dueDate"),
                                created = obj.optString("created"),
                                isDisabled = obj.optBoolean("isdisabled", false),
                                clientId = obj.optString("client_id"),
                                clientType = obj.optString("client_type", "consumer"),
                                docId = obj.optString("docid"),
                                documentStatus = obj.optString("document_status"),
                                relId = obj.optString("rel_id"),
                                canShare = obj.optBoolean("can_share", false),
                                reminder = reminder
                            )
                        )
                    }
                    Result.success(list)
                }
            } else {
                Result.failure(Exception(response.responseContent))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInvoiceDetail(id: String): Result<InvoiceDetail> = withContext(Dispatchers.IO) {
        try {
            val response = executeHttpRequest(
                method = WebServiceHelper.RestMethodType.GET,
                url = "v3/invoice/$id",
                requestType = "Invoice Detail"
            )
            if (response.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(response.responseContent)
                if (json.optBoolean("error", false)) {
                    Result.failure(Exception(json.optString("msg", "Failed to load invoice detail")))
                } else {
                    val obj = json.optJSONObject("invoice")
                        ?: return@withContext Result.failure(Exception("Invoice object not found"))

                    var clientId = ""
                    var clientType = "consumer"
                    val clientArray = obj.optJSONArray("client")
                    if (clientArray != null && clientArray.length() > 0) {
                        val cObj = clientArray.optJSONObject(0)
                        if (cObj != null) {
                            clientId = cObj.optString("id")
                            clientType = cObj.optString("type", "consumer")
                        }
                    }

                    var matterId = ""
                    var matterName = ""
                    var matterType = ""
                    val mattersArray = obj.optJSONArray("matters")
                    if (mattersArray != null && mattersArray.length() > 0) {
                        val mObj = mattersArray.optJSONObject(0)
                        if (mObj != null) {
                            matterId = mObj.optString("id")
                            matterName = mObj.optString("name")
                            matterType = mObj.optString("type")
                        }
                    }

                    val lineItems = mutableListOf<InvoiceLineItem>()
                    val itemsArray = obj.optJSONArray("items") ?: obj.optJSONArray("invoice_items")
                    if (itemsArray != null) {
                        for (i in 0 until itemsArray.length()) {
                            val it = itemsArray.optJSONObject(i) ?: continue
                            lineItems.add(
                                InvoiceLineItem(
                                    name = it.optString("name"),
                                    unitPrice = it.optDouble("unit_price", it.optDouble("unitPrice", 0.0)),
                                    quantity = it.optInt("no_of_units", it.optInt("quantity", 1))
                                )
                            )
                        }
                    }

                    var taxPercent = 0.0
                    var taxAmount = 0.0
                    var tax1Percent = 0.0
                    var tax2Percent = 0.0
                    val taxItems = obj.optJSONArray("tax_items")
                    if (taxItems != null && taxItems.length() > 0) {
                        for (i in 0 until taxItems.length()) {
                            val t = taxItems.optJSONObject(i) ?: continue
                            val taxType = t.optString("tax_type", "").replace(" ", "").lowercase()
                            val taxVal = t.optDouble("tax", 0.0)
                            when (taxType) {
                                "tax1" -> tax1Percent = taxVal
                                "tax2" -> tax2Percent = taxVal
                                else -> {
                                    if (i == 0) {
                                        taxPercent = taxVal
                                        taxAmount = t.optDouble("amount", 0.0)
                                        tax1Percent = taxVal
                                    } else if (i == 1) {
                                        tax2Percent = taxVal
                                    }
                                }
                            }
                        }
                    }

                    var discountPercent = 0.0
                    var discountAmount = 0.0
                    val discounts = obj.optJSONArray("discounts")
                    if (discounts != null && discounts.length() > 0) {
                        val d = discounts.optJSONObject(0)
                        if (d != null) {
                            discountPercent = d.optDouble("discount", 0.0)
                            discountAmount = d.optDouble("amount", 0.0)
                        }
                    }

                    val logo = obj.optString(
                        "logo_url",
                        obj.optString("logoUrl", obj.optString("logo", ""))
                    )

                    val contactObj = obj.optJSONObject("client_contact")
                    val contactName = contactObj?.optString("name", "") ?: ""
                    val contactEmail = (contactObj?.optString("email", "") ?: "").ifBlank { obj.optString("client_email", "") }
                    val contactPhone = (contactObj?.optString("phone", "") ?: "").ifBlank { obj.optString("client_phone", "") }
                    val contactCountryCode = (contactObj?.optString("country_code", "") ?: "").ifBlank { obj.optString("country_code", "") }

                    val bankDetails = obj.optString("bank_details", "")
                    val bankDetailsSaved = obj.optBoolean("bank_details_saved", obj.optBoolean("save_bank_details", false))

                    val status = obj.optString("status")
                    val remObj = obj.optJSONObject("reminder")
                    val reminder = if (remObj != null) {
                        ReminderInfo(
                            canSend = remObj.optBoolean("can_send", false),
                            retryAfter = remObj.optInt("retry_after", 0),
                            lastSentAt = remObj.optString("last_sent_at", ""),
                            count = remObj.optInt("count", 0)
                        )
                    } else {
                        ReminderInfo(canSend = status.equals("raised", ignoreCase = true))
                    }

                    val fromAddrObj = obj.optJSONObject("from_address")
                    val fromAddress = if (fromAddrObj != null) {
                        InvoiceAddress(
                            houseFlatNo = fromAddrObj.optString("house_flat_no", ""),
                            street = fromAddrObj.optString("street", ""),
                            cityTown = fromAddrObj.optString("city_town", ""),
                            state = fromAddrObj.optString("state", ""),
                            country = fromAddrObj.optString("country", ""),
                            zipcode = fromAddrObj.optString("zipcode", "")
                        )
                    } else {
                        InvoiceAddress()
                    }

                    val currency = obj.optString(
                        "currency_code",
                        obj.optString("currency", "USD")
                    )

                    val detail = InvoiceDetail(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        invoiceNo = obj.optString("invoice_no"),
                        invoiceSequence = obj.optInt("invoice_sequence", 0),
                        status = status,
                        firmGstNo = obj.optString("firm_gst_no"),
                        firmPanNo = obj.optString("firm_pan_no"),
                        clientGstNo = obj.optString("client_gst_no"),
                        clientPanNo = obj.optString("client_pan_no"),
                        billTo = obj.optString("billto"),
                        notes = obj.optString("notes"),
                        currencyCode = currency,
                        includeLogo = obj.optBoolean("include_logo", true),
                        logoUrl = logo,
                        date = obj.optString("date"),
                        dueDate = obj.optString("duedate", obj.optString("dueDate")),
                        lineItems = lineItems,
                        taxPercent = taxPercent,
                        taxAmount = taxAmount,
                        tax1Percent = tax1Percent,
                        tax2Percent = tax2Percent,
                        discountPercent = discountPercent,
                        discountAmount = discountAmount,
                        clientId = clientId,
                        clientType = clientType,
                        clientContactName = contactName,
                        clientContactEmail = contactEmail,
                        clientContactPhone = contactPhone,
                        countryCode = contactCountryCode,
                        bankDetails = bankDetails,
                        bankDetailsSaved = bankDetailsSaved,
                        reminder = reminder,
                        fromAddress = fromAddress,
                        matterId = matterId,
                        matterName = matterName,
                        matterType = matterType,
                        docId = obj.optString("docid"),
                        documentStatus = obj.optString("document_status"),
                        relId = obj.optString("rel_id"),
                        canShare = obj.optBoolean("can_share", false)
                    )
                    Result.success(detail)
                }
            } else {
                Result.failure(Exception(response.responseContent))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun suggestInvoiceId(): Result<Pair<String, Int>> = withContext(Dispatchers.IO) {
        try {
            val response = executeHttpRequest(
                method = WebServiceHelper.RestMethodType.GET,
                url = "v3/invoice/suggest-id",
                requestType = "Suggest Invoice Id"
            )
            if (response.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(response.responseContent)
                val data = json.optJSONObject("data")
                if (data != null) {
                    val code = data.optString("invoice_code", "")
                    val seq = data.optInt("invoice_sequence", 0)
                    Result.success(Pair(code, seq))
                } else {
                    Result.failure(Exception("Failed to suggest invoice ID"))
                }
            } else {
                Result.failure(Exception(response.responseContent))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInvoiceLogo(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = executeHttpRequest(
                method = WebServiceHelper.RestMethodType.GET,
                url = "invoice/logoget",
                requestType = "Get Invoice Logo"
            )
            if (response.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(response.responseContent)
                val data = json.optJSONObject("data")
                if (data != null && !data.optBoolean("error", false)) {
                    val url = data.optString("url", "")
                    Result.success(url)
                } else {
                    Result.success("")
                }
            } else {
                Result.success("")
            }
        } catch (e: Exception) {
            Result.success("")
        }
    }

    suspend fun uploadLogo(file: File): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = executeHttpUploadRequest(
                url = "invoice/logoup",
                requestType = "Upload Logo",
                file = file,
                JSONObject().toString()
            )
            if (response.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(response.responseContent)
                if (!json.optBoolean("error", false)) {
                    Result.success(true)
                } else {
                    Result.failure(Exception(json.optString("msg", "Failed to upload logo")))
                }
            } else {
                Result.failure(Exception(response.responseContent))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractErrorMessage(responseContent: String?, defaultMsg: String): String {
        if (responseContent.isNullOrBlank()) return defaultMsg
        try {
            val json = JSONObject(responseContent)
            val errorsArray = json.optJSONArray("errors")
            if (errorsArray != null && errorsArray.length() > 0) {
                val firstErr = errorsArray.optJSONObject(0)
                if (firstErr != null) {
                    val msg = firstErr.optString("msg").ifBlank { firstErr.optString("message") }
                    if (msg.isNotBlank()) return msg
                } else {
                    val errStr = errorsArray.optString(0)
                    if (errStr.isNotBlank()) return errStr
                }
            }
            val msg = json.optString("msg").ifBlank { json.optString("message") }
            if (msg.isNotBlank()) return msg

            val errorField = json.opt("error")
            if (errorField is String && errorField.isNotBlank()) return errorField
        } catch (_: Exception) {}
        return defaultMsg
    }

    suspend fun createInvoice(payload: JSONObject): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = executeHttpRequest(
                method = WebServiceHelper.RestMethodType.POST,
                url = "v3/invoice",
                requestType = "Create Invoice",
                payload = payload.toString()
            )
            if (response.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(response.responseContent ?: "{}")
                if (!json.optBoolean("error", false)) {
                    Result.success(json.optString("msg", "Invoice created successfully."))
                } else {
                    Result.failure(Exception(extractErrorMessage(response.responseContent, "Failed to create invoice.")))
                }
            } else {
                Result.failure(Exception(extractErrorMessage(response.responseContent, "Failed to create invoice.")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun patchInvoice(id: String, payload: JSONObject): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = executeHttpRequest(
                method = WebServiceHelper.RestMethodType.PATCH,
                url = "v3/invoice/$id",
                requestType = "Edit Invoice",
                payload = payload.toString()
            )
            if (response.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(response.responseContent ?: "{}")
                if (!json.optBoolean("error", false)) {
                    Result.success(json.optString("msg", "Invoice updated successfully."))
                } else {
                    Result.failure(Exception(extractErrorMessage(response.responseContent, "Failed to update invoice.")))
                }
            } else {
                Result.failure(Exception(extractErrorMessage(response.responseContent, "Failed to update invoice.")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changeInvoiceStatus(id: String, status: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("status", status)
            }
            val response = executeHttpRequest(
                method = WebServiceHelper.RestMethodType.PATCH,
                url = "v3/invoice/$id",
                requestType = "Change Invoice Status",
                payload = payload.toString()
            )
            if (response.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(response.responseContent ?: "{}")
                if (!json.optBoolean("error", false)) {
                    Result.success(json.optString("msg", "Status updated successfully."))
                } else {
                    Result.failure(Exception(extractErrorMessage(response.responseContent, "Failed to update status.")))
                }
            } else {
                Result.failure(Exception(extractErrorMessage(response.responseContent, "Failed to update status.")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteInvoice(id: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = executeHttpRequest(
                method = WebServiceHelper.RestMethodType.DELETE,
                url = "v3/invoice/$id",
                requestType = "Delete Invoice"
            )
            if (response.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(response.responseContent ?: "{}")
                if (!json.optBoolean("error", false)) {
                    Result.success(json.optString("msg", "Invoice deleted successfully."))
                } else {
                    Result.failure(Exception(extractErrorMessage(response.responseContent, "Failed to delete invoice.")))
                }
            } else {
                Result.failure(Exception(extractErrorMessage(response.responseContent, "Failed to delete invoice.")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendInvoiceReminder(id: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = executeHttpRequest(
                method = WebServiceHelper.RestMethodType.POST,
                url = "v3/invoice/$id/remind",
                requestType = "Send Invoice Reminder",
                payload = JSONObject().toString()
            )
            if (response.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(response.responseContent ?: "{}")
                if (!json.optBoolean("error", false)) {
                    Result.success(json.optString("msg", "Reminder sent successfully."))
                } else {
                    Result.failure(Exception(extractErrorMessage(response.responseContent, "Failed to send reminder.")))
                }
            } else {
                Result.failure(Exception(extractErrorMessage(response.responseContent, "Failed to send reminder.")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createClientByEmail(
        email: String,
        firstName: String,
        lastName: String,
        countryCode: String,
        mobile: String
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("email", email)
                put("first_name", firstName)
                put("last_name", lastName)
                put("country_code", if (countryCode.startsWith("+")) countryCode else "+$countryCode")
                put("mobile", mobile)
            }
            val response = executeHttpRequest(
                method = WebServiceHelper.RestMethodType.POST,
                url = "v3/relationship/client/by-email",
                requestType = "Create Client By Email",
                payload = payload.toString()
            )
            if (response.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(response.responseContent)
                if (!json.optBoolean("error", false)) {
                    Result.success(json)
                } else {
                    Result.failure(Exception(extractErrorMessage(response.responseContent, "Failed to register client.")))
                }
            } else {
                Result.failure(Exception(extractErrorMessage(response.responseContent, "Failed to register client.")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun shareInvoice(relId: String, docId: String, message: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val docObj = JSONObject().apply {
                put("docid", docId)
                put("doctype", "general")
                put("matters", JSONArray())
            }
            val payload = JSONObject().apply {
                put("add", JSONArray().put(docObj))
                put("message", message)
            }
            val response = executeHttpRequest(
                method = WebServiceHelper.RestMethodType.PUT,
                url = "v2/relationship/$relId/docs/share?module=invoice",
                requestType = "Share Invoice",
                payload = payload.toString()
            )
            if (response.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(response.responseContent ?: "{}")
                if (!json.optBoolean("error", false)) {
                    Result.success(json.optString("msg", "Invoice shared successfully."))
                } else {
                    Result.failure(Exception(extractErrorMessage(response.responseContent, "Failed to share invoice.")))
                }
            } else {
                Result.failure(Exception(extractErrorMessage(response.responseContent, "Failed to share invoice.")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDownloadInvoiceUrl(docId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = executeHttpRequest(
                method = WebServiceHelper.RestMethodType.GET,
                url = "v3/document/$docId/download?module=invoice",
                requestType = "Download Invoice"
            )
            if (response.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(response.responseContent)
                val data = json.optJSONObject("data")
                val url = data?.optString("url", "") ?: ""
                if (url.isNotEmpty()) {
                    Result.success(url)
                } else {
                    Result.failure(Exception("Download URL not found."))
                }
            } else {
                Result.failure(Exception(response.responseContent))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getConsumers(searchText: String = ""): Result<List<InvoiceClient>> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("search", searchText.trim())
                put("searchText", searchText.trim())
            }
            val response = executeHttpRequest(
                method = WebServiceHelper.RestMethodType.POST,
                url = "v3/relationship/search/consumer",
                requestType = "Search Consumer",
                payload = payload.toString()
            )
            if (response.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(response.responseContent ?: "{}")
                if (json.optBoolean("error", false)) {
                    Result.failure(Exception(json.optString("msg", "Failed to search consumers")))
                } else {
                    val dataArray = json.optJSONArray("data")
                        ?: json.optJSONArray("consumers")
                        ?: json.optJSONArray("relationships")
                        ?: JSONArray()
                    val list = mutableListOf<InvoiceClient>()
                    for (i in 0 until dataArray.length()) {
                        val obj = dataArray.optJSONObject(i) ?: continue
                        val id = obj.optString("id", obj.optString("_id", ""))
                        val fName = obj.optString("first_name", "").trim()
                        val lName = obj.optString("last_name", "").trim()
                        val rawName = obj.optString("name", "").trim()
                        val fullName = when {
                            rawName.isNotEmpty() -> rawName
                            fName.isNotEmpty() && lName.isNotEmpty() -> "$fName $lName"
                            fName.isNotEmpty() -> fName
                            lName.isNotEmpty() -> lName
                            else -> obj.optString("email", "")
                        }
                        val email = obj.optString("email", "")
                        val phone = obj.optString("mobile", obj.optString("phone", ""))
                        val countryCode = obj.optString("country_code", obj.optString("countryCode", ""))
                        if (id.isNotEmpty() || fullName.isNotEmpty()) {
                            list.add(
                                InvoiceClient(
                                    id = id,
                                    name = fullName,
                                    type = "consumer",
                                    email = email,
                                    phone = phone,
                                    countryCode = countryCode
                                )
                            )
                        }
                    }
                    Result.success(list)
                }
            } else {
                Result.failure(Exception(response.responseContent ?: "Failed to get consumers"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClients(): Result<List<InvoiceClient>> = withContext(Dispatchers.IO) {
        try {
            val clients = mutableListOf<InvoiceClient>()

            // 1. All regular clients
            val res1 = executeHttpRequest(
                method = WebServiceHelper.RestMethodType.GET,
                url = "v3/client/all/list",
                requestType = "Clients List"
            )
            if (res1.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(res1.responseContent)
                val data = json.optJSONObject("data")
                val rels = data?.optJSONArray("relationships")
                if (rels != null) {
                    for (i in 0 until rels.length()) {
                        val obj = rels.optJSONObject(i) ?: continue
                        clients.add(
                            InvoiceClient(
                                id = obj.optString("id"),
                                name = obj.optString("name"),
                                type = obj.optString("type", "consumer")
                            )
                        )
                    }
                }
            }

            // 2. Corporate clients
            val res2 = executeHttpRequest(
                method = WebServiceHelper.RestMethodType.GET,
                url = "v3/corporate/list",
                requestType = "Corp Clients List"
            )
            if (res2.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(res2.responseContent)
                val data = json.optJSONObject("data") ?: json
                val rels = data.optJSONArray("relationships")
                if (rels != null) {
                    for (i in 0 until rels.length()) {
                        val obj = rels.optJSONObject(i) ?: continue
                        val id = obj.optString("id")
                        // Avoid duplicates if already present
                        if (clients.none { it.id == id }) {
                            clients.add(
                                InvoiceClient(
                                    id = id,
                                    name = obj.optString("name"),
                                    type = if (obj.optString("type") == "consumer") "consumer" else "corporate"
                                )
                            )
                        }
                    }
                }
            }

            Result.success(clients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMatters(clientId: String): Result<List<InvoiceMatter>> = withContext(Dispatchers.IO) {
        try {
            val response = executeHttpRequest(
                method = WebServiceHelper.RestMethodType.GET,
                url = "v3/matter/all/$clientId",
                requestType = "Matters List"
            )
            if (response.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(response.responseContent)
                if (json.optBoolean("error", false)) {
                    Result.failure(Exception(json.optString("msg", "Failed to load matters")))
                } else {
                    val matters = json.optJSONArray("matterList") ?: JSONArray()
                    val list = mutableListOf<InvoiceMatter>()
                    for (i in 0 until matters.length()) {
                        val obj = matters.optJSONObject(i) ?: continue
                        list.add(
                            InvoiceMatter(
                                id = obj.optString("id"),
                                title = obj.optString("title"),
                                type = obj.optString("type", "legal")
                            )
                        )
                    }
                    Result.success(list)
                }
            } else {
                Result.failure(Exception(response.responseContent))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
