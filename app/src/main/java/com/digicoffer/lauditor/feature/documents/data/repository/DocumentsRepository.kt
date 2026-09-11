package com.digicoffer.lauditor.feature.documents.data.repository

import android.content.Context
import android.view.View
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.coroutines.resume

class DocumentsRepository(private val context: Context) {

    suspend fun fetchDocuments(type: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.GET,
            "v3/documents/$type",
            "VIEW_DOCUMENT",
            JSONObject().toString()
        )
    }

    suspend fun fetchDeletedDocuments(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.GET,
            "docs/deleted/list",
            "DELETED_DOCUMENT",
            JSONObject().toString()
        )
    }

    suspend fun uploadDocument(file: File, payloadJson: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        WebServiceHelper.callHttpUploadWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.POST,
            "v3/document/upload",
            "Upload Document",
            file,
            payloadJson
        )
    }

    suspend fun updateMetadata(id: String, name: String, desc: String, expDate: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val formattedExpDate = if (expDate.equals("NA", ignoreCase = true) || expDate.isBlank()) {
            ""
        } else {
            AndroidUtils.convertAnyDateToDDMMYYYY(expDate)
        }
        val payload = JSONObject().apply {
            put("name", name)
            put("description", desc)
            put("expiration_date", formattedExpDate)
        }
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.PUT,
            "v3/document/$id",
            "Update Documents",
            payload.toString()
        )
    }

    suspend fun updateTags(id: String, name: String, tags: JSONObject, isMergePdf: Boolean): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val payload = JSONObject().apply {
            put("name", name)
            put("tags", tags)
        }
        val url = if (isMergePdf) "v3/mergepdf/$id/tags" else "v3/document/tags/$id"
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.PUT,
            url,
            "Update Tags",
            payload.toString()
        )
    }

    suspend fun deleteDocument(id: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val payload = JSONObject().apply {
            val jsonArray = JSONArray().apply { put(id) }
            put("docids", jsonArray)
        }
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.POST,
            "v3/document/delete",
            "Delete Documents",
            payload.toString()
        )
    }

    suspend fun deleteMergeDocument(id: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.DELETE,
            "v3/mergepdf/$id",
            "Delete Merge Documents",
            JSONObject().toString()
        )
    }

    suspend fun restoreDeletedDocument(docId: String, docType: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val payload = JSONObject().apply {
            put("docid", docId)
            put("doctype", docType)
        }
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.POST,
            "docs/deleted/restore",
            "Deleted Documents",
            payload.toString()
        )
    }

    suspend fun permanentlyDeleteDocument(docId: String, docType: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val payload = JSONObject().apply {
            put("docid", docId)
            put("doctype", docType)
        }
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.POST,
            "docs/deleted/delete",
            "Deleted Documents",
            payload.toString()
        )
    }

    suspend fun decryptDocument(id: String, download: Boolean): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val payload = JSONObject().apply {
            put("docid", id)
            put("download", download)
        }
        val url = com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.decryptUrl ?: "v3/decrypt"
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.POST,
            url,
            "Decrypt Doc",
            payload.toString()
        )
    }

    suspend fun viewDocumentInfo(id: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.GET,
            "v3/document/$id/view",
            "Display Documents",
            JSONObject().toString()
        )
    }

    suspend fun downloadDocumentInfo(id: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.GET,
            "v3/document/$id/download",
            "Download Document",
            JSONObject().toString()
        )
    }

    suspend fun viewMergeDocumentInfo(id: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.GET,
            "v3/mergepdf/$id/view",
            "Display Documents",
            JSONObject().toString()
        )
    }

    suspend fun enableDocDownload(id: String, enable: Boolean): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val payload = JSONObject().apply {
            put("downloadDisabled", enable)
        }
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.PATCH,
            "v3/document/$id",
            if (enable) "Enabled Documents" else "Disabled Documents",
            payload.toString()
        )
    }

    suspend fun encryptDoc(id: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.POST,
            "v3/document/encrypt/$id",
            "Encrypt Documents",
            JSONObject().toString()
        )
    }

    suspend fun decryptDoc(id: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val payload = JSONObject().apply {
            put("get_file", false)
        }
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.POST,
            "v3/document/decrypt/$id",
            "Decrypt Documents",
            payload.toString()
        )
    }

    suspend fun fetchGroups(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.GET,
            "v3/groups",
            "Groups",
            JSONObject().toString()
        )
    }

    suspend fun fetchClients(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.GET,
            "v3/client/all/list",
            "Clients List",
            JSONObject().toString()
        )
    }

    suspend fun fetchCorpClients(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.GET,
            "v3/corporate/list",
            "Corp Clients List",
            JSONObject().toString()
        )
    }

    suspend fun fetchMatters(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.GET,
            "v2/matter/list",
            "Legal Matter",
            JSONObject().toString()
        )
    }

    suspend fun fetchClientGroups(clientsJson: String, matterId: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val payload = JSONObject().apply {
            put("clients", JSONArray(clientsJson))
            put("matterid", matterId)
        }
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.PUT,
            "v3/documents/groupslist",
            "Client Groups",
            payload.toString()
        )
    }

    suspend fun filterDocuments(
        category: String,
        clients: Any?,
        matters: Any?,
        groups: JSONArray?
    ): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val payload = JSONObject().apply {
            put("showPdfDocs", false)
            put("category", category)
            if (category == "client") {
                put("clients", clients ?: "")
                val hasMatters = when (matters) {
                    is JSONArray -> matters.length() > 0
                    is String -> matters.isNotEmpty()
                    else -> false
                }
                if (hasMatters) {
                    put("matters", matters)
                }
            } else {
                put("clients", "")
                put("matters", "")
                put("groups", groups ?: JSONObject.NULL)
            }
        }
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.PUT,
            "v3/document/filter",
            "Display FilterDocuments",
            payload.toString()
        )
    }
}
