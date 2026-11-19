package com.digicoffer.lauditor.feature.email.data.repository

import android.content.Context
import android.view.View
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Documents.Models.ClientsModel
import com.digicoffer.lauditor.Documents.Models.DocumentsModel
import com.digicoffer.lauditor.Documents.Models.MattersModel
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.feature.email.data.model.EmailAttachmentItem
import com.digicoffer.lauditor.feature.email.data.model.EmailLabelInfo
import com.digicoffer.lauditor.feature.email.data.model.EmailMessageItem
import com.digicoffer.lauditor.feature.email.data.model.LauditorAttachedDoc
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume

class EmailRepository(private val context: Context) {

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

    private suspend fun executeEmailHttpRequest(
        method: WebServiceHelper.RestMethodType,
        url: String,
        requestType: String,
        payload: String = JSONObject().toString()
    ): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            WebServiceHelper.callEmailHttpWebService(
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

    suspend fun fetchAuthUrl(isGmail: Boolean): Result<String> {
        val path = if (isGmail) {
            "${Constants.EMAIL_BASE_URL}gmail/authurl?authtoken=${Constants.TOKEN}"
        } else {
            "${Constants.EMAIL_BASE_URL}authurl?authtoken=${Constants.TOKEN}"
        }
        val result = executeHttpRequest(WebServiceHelper.RestMethodType.GET, path, "auth")
        return if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val json = JSONObject(result.responseContent ?: "{}")
                val url = json.optString("url")
                if (url.isNotEmpty()) Result.success(url) else Result.failure(Exception("Empty auth URL received"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception(result.responseContent ?: "Failed to get auth URL"))
        }
    }

    suspend fun fetchLabel(isGmail: Boolean, labelId: String = "INBOX"): Result<EmailLabelInfo> {
        val path = if (isGmail) {
            "${Constants.EMAIL_BASE_URL}gmail/label/${Constants.TOKEN}?labelid=$labelId"
        } else {
            "${Constants.EMAIL_GET_URL}label/${Constants.TOKEN}?labelid=$labelId"
        }
        val result = executeHttpRequest(WebServiceHelper.RestMethodType.GET, path, "Label")
        return if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val json = JSONObject(result.responseContent ?: "{}")
                if (isGmail) {
                    val data = json.optJSONObject("data") ?: json
                    Result.success(
                        EmailLabelInfo(
                            id = data.optString("id", labelId),
                            name = data.optString("name", "Inbox"),
                            type = data.optString("type"),
                            messagesTotal = data.optInt("messagesTotal", 0),
                            messagesUnread = data.optInt("messagesUnread", 0),
                            threadsTotal = data.optInt("threadsTotal", 0),
                            threadsUnread = data.optInt("threadsUnread", 0)
                        )
                    )
                } else {
                    Result.success(
                        EmailLabelInfo(
                            id = json.optString("id", labelId),
                            name = json.optString("displayName", "Inbox"),
                            type = json.optString("type"),
                            messagesTotal = json.optInt("totalItemCount", 0),
                            messagesUnread = json.optInt("unreadItemCount", 0),
                            threadsTotal = json.optInt("sizeInBytes", 0),
                            threadsUnread = json.optInt("unreadItemCount", 0)
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception(result.responseContent ?: "Failed to load inbox label"))
        }
    }

    suspend fun fetchMessages(
        isGmail: Boolean,
        labelId: String,
        nextPageToken: String?
    ): Result<Pair<List<EmailMessageItem>, String?>> {
        val path = if (isGmail) {
            var url = "${Constants.EMAIL_BASE_URL}${Constants.gmail_messages}${Constants.TOKEN}?rows=10"
            if (!nextPageToken.isNullOrEmpty()) {
                url += "&nextpagetoken=$nextPageToken"
            }
            url
        } else {
            var url = "${Constants.EMAIL_GET_URL}messages/${Constants.TOKEN}?labelid=$labelId&rows=10"
            if (!nextPageToken.isNullOrEmpty()) {
                url += if (nextPageToken.startsWith("http")) "&$nextPageToken" else "&nextpagetoken=$nextPageToken"
            }
            url
        }

        val result = executeHttpRequest(WebServiceHelper.RestMethodType.GET, path, "messages_rows")
        return if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val json = JSONObject(result.responseContent ?: "{}")
                val messagesList = mutableListOf<EmailMessageItem>()
                var nextToken: String? = null

                if (isGmail) {
                    val messagesArray = json.optJSONArray("messages") ?: JSONArray()
                    for (i in 0 until messagesArray.length()) {
                        val mObj = messagesArray.getJSONObject(i)
                        val attachmentsList = mutableListOf<EmailAttachmentItem>()
                        val attArray = mObj.optJSONArray("attachments")
                        if (attArray != null) {
                            for (j in 0 until attArray.length()) {
                                val aObj = attArray.getJSONObject(j)
                                val filename = aObj.optString("filename")
                                val partId = aObj.optString("partId")
                                if (filename.isNotEmpty() && partId.isNotEmpty()) {
                                    val bodyObj = aObj.optJSONObject("body")
                                    val size = bodyObj?.optInt("size") ?: 0
                                    attachmentsList.add(
                                        EmailAttachmentItem(
                                            partId = partId,
                                            filename = filename,
                                            mimeType = aObj.optString("mimeType"),
                                            size = size
                                        )
                                    )
                                }
                            }
                        }
                        messagesList.add(
                            EmailMessageItem(
                                msgId = mObj.optString("msgId"),
                                from = mObj.optString("from"),
                                to = mObj.optString("to"),
                                subject = mObj.optString("subject"),
                                attachments = attachmentsList
                            )
                        )
                    }
                    nextToken = json.optString("nextPageToken").takeIf { it.isNotEmpty() }
                } else {
                    val valuesArray = json.optJSONArray("value") ?: JSONArray()
                    for (i in 0 until valuesArray.length()) {
                        val mObj = valuesArray.getJSONObject(i)
                        val fromObj = mObj.optJSONObject("from")?.optJSONObject("emailAddress")
                        val fromName = fromObj?.optString("name") ?: ""
                        val toRecipients = mObj.optJSONArray("toRecipients")
                        val toName = if (toRecipients != null && toRecipients.length() > 0) {
                            toRecipients.getJSONObject(0).optJSONObject("emailAddress")?.optString("name") ?: ""
                        } else ""
                        val msgId = mObj.optString("id")
                        val hasAttachments = mObj.optBoolean("hasAttachments", false)

                        val attachmentsList = mutableListOf<EmailAttachmentItem>()
                        if (hasAttachments && msgId.isNotEmpty()) {
                            // fetch attachments for message
                            val attResult = fetchMessageAttachments(msgId)
                            if (attResult.isSuccess) {
                                attachmentsList.addAll(attResult.getOrDefault(emptyList()))
                            }
                        }

                        messagesList.add(
                            EmailMessageItem(
                                msgId = msgId,
                                from = fromName,
                                to = toName,
                                subject = mObj.optString("subject"),
                                attachments = attachmentsList
                            )
                        )
                    }
                    nextToken = json.optString("@odata.nextLink").takeIf { it.isNotEmpty() }
                }
                Result.success(Pair(messagesList, nextToken))
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception(result.responseContent ?: "Failed to load messages"))
        }
    }

    suspend fun fetchMessageAttachments(msgId: String): Result<List<EmailAttachmentItem>> {
        val path = "${Constants.EMAIL_GET_URL}message/detail/${Constants.TOKEN}/$msgId"
        val result = executeHttpRequest(WebServiceHelper.RestMethodType.GET, path, "messages_attachment_$msgId")
        return if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val json = JSONObject(result.responseContent ?: "{}")
                val responseObj = json.optJSONObject("attachments") ?: json
                val arr = responseObj.optJSONArray("value") ?: JSONArray()
                val list = mutableListOf<EmailAttachmentItem>()
                for (j in 0 until arr.length()) {
                    val attObj = arr.getJSONObject(j)
                    list.add(
                        EmailAttachmentItem(
                            partId = attObj.optString("id"),
                            filename = attObj.optString("name"),
                            mimeType = attObj.optString("contentType"),
                            size = attObj.optInt("size", 0)
                        )
                    )
                }
                Result.success(list)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception(result.responseContent ?: "Failed to fetch attachments"))
        }
    }

    suspend fun sendEmail(
        isGmail: Boolean,
        toEmails: List<String>,
        subject: String,
        body: String,
        documents: List<LauditorAttachedDoc>
    ): Result<String> {
        val url = if (isGmail) {
            "${Constants.EMAIL_BASE_URL}${Constants.sending_mail}${Constants.TOKEN}"
        } else {
            "${Constants.EMAIL_GET_URL}${Constants.sending_mail}${Constants.TOKEN}"
        }

        val emailJson = JSONObject().apply {
            val toArray = JSONArray()
            toEmails.forEach { toArray.put(it.trim()) }
            put("toEmail", toArray)
            put("subject", if (subject.isEmpty()) "(no Subject)" else subject)
            put("body", body)

            val docsArray = JSONArray()
            documents.forEach { doc ->
                docsArray.put(JSONObject().apply {
                    put("id", doc.id)
                    put("name", doc.name)
                    put("filename", doc.filename)
                    put("path", doc.pathUrl)
                })
            }
            put("documents", docsArray)
        }

        val result = executeEmailHttpRequest(
            WebServiceHelper.RestMethodType.POST,
            url,
            "sending_email",
            emailJson.toString()
        )

        return if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val json = JSONObject(result.responseContent ?: "{}")
                val msg = json.optString("message", "Email sent successfully")
                Result.success(msg)
            } catch (e: Exception) {
                Result.success("Email sent successfully")
            }
        } else {
            Result.failure(Exception(result.responseContent ?: "Mail not sent successfully"))
        }
    }

    suspend fun uploadAttachmentToLauditor(
        category: String,
        clientIds: List<String>,
        matterId: String?,
        groupIds: List<String>,
        msgId: String,
        partId: String
    ): Result<String> {
        val uploadUrl = Constants.EMAIL_UPLOAD_URL ?: ""
        val token = Constants.TOKEN ?: ""
        val url = "$uploadUrl${Constants.mail_document}$token/$msgId?partid=$partId"

        val json = JSONObject().apply {
            put("category", category)
            val clientsArray = JSONArray()
            clientIds.forEach { clientsArray.put(it) }
            put("clientids", clientsArray)

            val mattersArray = JSONArray()
            if (!matterId.isNullOrEmpty()) {
                mattersArray.put(matterId)
                mattersArray.put(matterId)
            }
            put("matters", mattersArray)

            val groupsArray = JSONArray()
            groupIds.forEach { groupsArray.put(it) }
            put("groupids", groupsArray)

            put("enableDownload", true)
        }

        val result = executeEmailHttpRequest(
            WebServiceHelper.RestMethodType.POST,
            url,
            "uploaded file",
            json.toString()
        )

        return if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val resObj = JSONObject(result.responseContent ?: "{}")
                Result.success(resObj.optString("message", "Document attached successfully"))
            } catch (e: Exception) {
                Result.success("Document attached successfully")
            }
        } else {
            Result.failure(Exception(result.responseContent ?: "Upload failed"))
        }
    }

    suspend fun fetchClients(): Result<List<ClientsModel>> {
        val result = executeHttpRequest(WebServiceHelper.RestMethodType.GET, "v3/client/all/list", "Clients List")
        if (result.result != WebServiceHelper.ServiceCallStatus.Success) {
            return Result.failure(Exception(result.responseContent ?: "Failed to load clients"))
        }

        val clientsList = mutableListOf<ClientsModel>()
        try {
            val json = JSONObject(result.responseContent ?: "{}")
            val data = json.optJSONObject("data")
            val rels = data?.optJSONArray("relationships") ?: JSONArray()
            for (i in 0 until rels.length()) {
                val obj = rels.getJSONObject(i)
                clientsList.add(
                    ClientsModel().apply {
                        id = obj.optString("id")
                        name = obj.optString("name")
                        type = obj.optString("type")
                    }
                )
            }

            // Also load corporate clients
            val corpResult = executeHttpRequest(WebServiceHelper.RestMethodType.GET, "v3/corporate/list", "Corp Clients List")
            if (corpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                val corpJson = JSONObject(corpResult.responseContent ?: "{}")
                val corpData = corpJson.optJSONObject("data")
                val corpRels = corpData?.optJSONArray("relationships") ?: corpJson.optJSONArray("relationships") ?: JSONArray()
                for (i in 0 until corpRels.length()) {
                    val obj = corpRels.getJSONObject(i)
                    clientsList.add(
                        ClientsModel().apply {
                            id = obj.optString("id")
                            name = obj.optString("name")
                            type = "corporate"
                        }
                    )
                }
            }
            return Result.success(clientsList)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun fetchMatters(): Result<List<MattersModel>> {
        val result = executeHttpRequest(WebServiceHelper.RestMethodType.GET, "v2/matter/list", "Legal Matter")
        return if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val json = JSONObject(result.responseContent ?: "{}")
                val mattersArray = json.optJSONArray("matters") ?: JSONArray()
                val list = mutableListOf<MattersModel>()
                for (i in 0 until mattersArray.length()) {
                    val obj = mattersArray.getJSONObject(i)
                    list.add(
                        MattersModel().apply {
                            id = obj.optString("id")
                            title = obj.optString("title")
                            type = obj.optString("type")
                        }
                    )
                }
                Result.success(list)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception(result.responseContent ?: "Failed to load matters"))
        }
    }

    suspend fun fetchGroups(): Result<List<DocumentsModel>> {
        val result = executeHttpRequest(WebServiceHelper.RestMethodType.GET, "v3/groups", "Groups")
        return if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val json = JSONObject(result.responseContent ?: "{}")
                val data = json.optJSONArray("data") ?: JSONArray()
                val list = mutableListOf<DocumentsModel>()
                for (i in 0 until data.length()) {
                    val obj = data.getJSONObject(i)
                    val name = obj.optString("name")
                    if (name != "AAM" && name != "SuperUser") {
                        list.add(
                            DocumentsModel().apply {
                                group_id = obj.optString("id")
                                group_name = name
                            }
                        )
                    }
                }
                Result.success(list)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception(result.responseContent ?: "Failed to load groups"))
        }
    }

    suspend fun fetchClientGroups(clientId: String, clientType: String, matterId: String?): Result<List<DocumentsModel>> {
        val json = JSONObject().apply {
            val clients = JSONArray().apply {
                put(JSONObject().apply {
                    put("id", clientId)
                    put("type", clientType)
                })
            }
            put("clients", clients)
            put("matterid", matterId ?: "")
        }
        val result = executeHttpRequest(
            WebServiceHelper.RestMethodType.PUT,
            "v3/documents/groupslist",
            "Client Groups",
            json.toString()
        )
        return if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val resJson = JSONObject(result.responseContent ?: "{}")
                val data = resJson.optJSONArray("data") ?: JSONArray()
                val list = mutableListOf<DocumentsModel>()
                for (i in 0 until data.length()) {
                    val obj = data.getJSONObject(i)
                    val name = obj.optString("name")
                    if (name != "AAM" && name != "SuperUser") {
                        list.add(
                            DocumentsModel().apply {
                                group_id = obj.optString("id")
                                group_name = name
                                isGroupChecked = true
                            }
                        )
                    }
                }
                Result.success(list)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception(result.responseContent ?: "Failed to load client groups"))
        }
    }

    suspend fun filterDocuments(
        category: String,
        clientId: String?,
        matterId: String?,
        groupIds: List<String>?
    ): Result<List<ViewDocumentsModel>> {
        val json = JSONObject().apply {
            put("category", category)
            put("showPdfDocs", false)
            if (category == "client") {
                put("clients", clientId ?: "")
                if (!matterId.isNullOrEmpty()) {
                    put("matters", matterId)
                }
                put("groups", JSONObject.NULL)
            } else {
                put("clients", "")
                put("matters", "")
                val groupsArray = JSONArray()
                groupIds?.forEach { groupsArray.put(it) }
                put("groups", groupsArray)
            }
        }

        val result = executeHttpRequest(
            WebServiceHelper.RestMethodType.PUT,
            "v3/document/filter",
            "Display documents",
            json.toString()
        )

        return if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val resJson = JSONObject(result.responseContent ?: "{}")
                val data = resJson.optJSONArray("data") ?: JSONArray()
                val list = mutableListOf<ViewDocumentsModel>()
                for (i in 0 until data.length()) {
                    val obj = data.getJSONObject(i)
                    list.add(
                        ViewDocumentsModel().apply {
                            id = obj.optString("id")
                            name = obj.optString("name")
                            filename = obj.optString("filename")
                            description = obj.optString("description")
                            uploaded_by = obj.optString("uploaded_by")
                            created = obj.optString("created")
                            expiration_date = obj.optString("expiration_date")
                            content_type = obj.optString("content_type")
                            is_disabled = obj.optBoolean("is_disabled")
                            is_encrypted = obj.optBoolean("is_encrypted")
                            is_password = obj.optBoolean("is_password")
                        }
                    )
                }
                Result.success(list)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception(result.responseContent ?: "Failed to filter documents"))
        }
    }

    suspend fun fetchDocumentViewUrl(docId: String): Result<String> {
        val result = executeHttpRequest(
            WebServiceHelper.RestMethodType.GET,
            "v3/document/$docId/view",
            "view_document"
        )
        return if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val json = JSONObject(result.responseContent ?: "{}")
                val data = json.optJSONObject("data")
                val url = data?.optString("url") ?: ""
                if (url.isNotEmpty()) Result.success(url) else Result.failure(Exception("View URL not found"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception(result.responseContent ?: "Failed to load document view URL"))
        }
    }
}
