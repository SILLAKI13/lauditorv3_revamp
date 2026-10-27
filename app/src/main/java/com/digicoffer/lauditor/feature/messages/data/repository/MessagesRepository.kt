package com.digicoffer.lauditor.feature.messages.data.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnection
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnectionService
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatCountApi
import com.digicoffer.lauditor.CommonFiles.ChatService.ConversationMetaApi
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.feature.messages.data.model.ChatMessageItem
import com.digicoffer.lauditor.feature.messages.data.model.ConversationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.jivesoftware.smackx.mam.MamManager
import org.jivesoftware.smackx.sid.element.StanzaIdElement
import org.json.JSONArray
import org.json.JSONObject
import org.jxmpp.jid.impl.JidCreate
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

class MessagesRepository(private val context: Context) {

    suspend fun fetchClientRelationships(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        continuation.resume(httpResult)
                    }
                    override fun onClick(view: View) {}
                },
                context,
                WebServiceHelper.RestMethodType.GET,
                "v2/relationship/client/list?include_appointment_consumers=True",
                "CLIENT_RELATIONSHIP",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            val failedResult = HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Exception
                responseContent = e.message ?: ""
            }
            continuation.resume(failedResult)
        }
    }

    suspend fun fetchCorporateRelationships(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
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
                "CORPORATE_RELATIONSHIP",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            val failedResult = HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Exception
                responseContent = e.message ?: ""
            }
            continuation.resume(failedResult)
        }
    }

    suspend fun fetchRelationshipUsers(relId: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        continuation.resume(httpResult)
                    }
                    override fun onClick(view: View) {}
                },
                context,
                WebServiceHelper.RestMethodType.GET,
                "relationship/$relId/users/notify",
                relId,
                JSONObject().toString()
            )
        } catch (e: Exception) {
            val failedResult = HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Exception
                responseContent = e.message ?: ""
            }
            continuation.resume(failedResult)
        }
    }

    suspend fun fetchTeamMembers(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        continuation.resume(httpResult)
                    }
                    override fun onClick(view: View) {}
                },
                context,
                WebServiceHelper.RestMethodType.GET,
                "v3/members",
                "Get Members",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            val failedResult = HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Exception
                responseContent = e.message ?: ""
            }
            continuation.resume(failedResult)
        }
    }

    suspend fun fetchAppointments(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        continuation.resume(httpResult)
                    }
                    override fun onClick(view: View) {}
                },
                context,
                WebServiceHelper.RestMethodType.GET,
                "v3/appointments",
                "Appointments_List",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            val failedResult = HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Exception
                responseContent = e.message ?: ""
            }
            continuation.resume(failedResult)
        }
    }

    suspend fun fetchTeamGroups(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        continuation.resume(httpResult)
                    }
                    override fun onClick(view: View) {}
                },
                context,
                WebServiceHelper.RestMethodType.GET,
                "chatusers",
                "Team_Chat",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            val failedResult = HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Exception
                responseContent = e.message ?: ""
            }
            continuation.resume(failedResult)
        }
    }

    suspend fun fetchConversationMeta(chatJids: List<String>): Map<String, ConversationMetaApi.ConversationMeta> =
        suspendCancellableCoroutine { continuation ->
            if (chatJids.isEmpty()) {
                if (continuation.isActive) continuation.resume(emptyMap())
                return@suspendCancellableCoroutine
            }
            try {
                ConversationMetaApi.fetch(context, chatJids, object : ConversationMetaApi.MetaCallback {
                    override fun onMetaReady(metaMap: Map<String, ConversationMetaApi.ConversationMeta>) {
                        if (continuation.isActive) continuation.resume(metaMap)
                    }
                })
            } catch (e: Exception) {
                if (continuation.isActive) continuation.resume(emptyMap())
            }
        }

    fun resetUnreadCount(guid: String) {
        if (guid.isEmpty()) return
        try {
            ChatCountApi(context).callResetUnreadCount(guid)
        } catch (e: Exception) {
            Log.e("MessagesRepository", "Error resetting unread count: ${e.message}")
        }
    }

    suspend fun fetchArchiveMessages(
        contactJid: String,
        currentJid: String,
        contactName: String
    ): Result<List<ChatMessageItem>> = withContext(Dispatchers.IO) {
        try {
            var conn = ChatConnection.mConnection
            Log.d("MessagesRepository", "fetchArchiveMessages: starting for contactJid=$contactJid, currentJid=$currentJid, conn=$conn, isConnected=${conn?.isConnected}, isAuth=${conn?.isAuthenticated}")

            if (conn == null || !conn.isConnected || !conn.isAuthenticated) {
                try {
                    val chatConn = ChatConnection.getInstance(context)
                    chatConn.connect()
                    conn = ChatConnection.mConnection
                    Log.d("MessagesRepository", "fetchArchiveMessages after connect: isConnected=${conn?.isConnected}, isAuth=${conn?.isAuthenticated}")
                } catch (e: Exception) {
                    Log.e("MessagesRepository", "XMPP auto-connect error: ${e.message}", e)
                }
            }

            if (conn == null || !conn.isConnected || !conn.isAuthenticated) {
                Log.e("MessagesRepository", "fetchArchiveMessages aborting: XMPP not connected or not authenticated")
                return@withContext Result.failure(IllegalStateException("XMPP not connected or authenticated"))
            }

            val bareContact = if (contactJid.contains("@")) contactJid.split("@")[0] else contactJid
            val bareCurrent = if (currentJid.contains("@")) currentJid.split("@")[0] else currentJid
            val domain = Constants.XMPP_DOMAIN ?: "devchat.vitacape.com"

            val contactID = "$bareContact@$domain".trim()
            val currentID = "$bareCurrent@$domain".trim()

            Log.d("MessagesRepository", "MAM querying contactID=$contactID, currentID=$currentID")

            conn.replyTimeout = 15000L
            val jid = JidCreate.entityBareFrom(contactID)
            val mamManager = MamManager.getInstanceFor(conn)

            val mamQueryArgs = MamManager.MamQueryArgs.builder()
                .limitResultsToJid(jid)
                .setResultPageSizeTo(50)
                .queryLastPage()
                .build()

            val mamQuery = mamManager.queryArchive(mamQueryArgs)
            var forwardedList = mamQuery.page.forwarded

            if (forwardedList.isEmpty()) {
                try {
                    val fallbackArgs = MamManager.MamQueryArgs.builder()
                        .limitResultsToJid(jid)
                        .setResultPageSizeTo(50)
                        .build()
                    val fallbackQuery = mamManager.queryArchive(fallbackArgs)
                    if (fallbackQuery.page.forwarded.isNotEmpty()) {
                        forwardedList = fallbackQuery.page.forwarded
                    }
                } catch (ignored: Exception) {}
            }

            Log.d("MessagesRepository", "MAM query returned ${forwardedList.size} forwarded items for $contactID")

            if (forwardedList.isEmpty()) {
                return@withContext Result.success(emptyList())
            }

            val messages = mutableListOf<ChatMessageItem>()
            val dateTimeParser = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.ENGLISH)
            val timeFormatter = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
            val todayKeyFormat = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
            val todayKey = todayKeyFormat.format(Date())

            for (forwarded in forwardedList) {
                val msg = forwarded.forwardedStanza
                val stamp = forwarded.delayInformation?.stamp ?: Date()
                val body = msg.body
                if (body.isNullOrEmpty()) continue

                val from = if (msg.from != null) msg.from.toString().split("/")[0] else ""
                val formattedCreatedAt = dateTimeParser.format(stamp)

                var stanzaId: String? = null
                val sid = msg.getExtension(StanzaIdElement::class.java)
                if (sid != null) {
                    stanzaId = sid.id
                }
                if (stanzaId == null) {
                    stanzaId = msg.stanzaId
                }

                val fromBare = if (from.contains("@")) from.split("@")[0] else from
                val isSentByMe = from.equals(currentID, ignoreCase = true) ||
                        fromBare.equals(bareCurrent, ignoreCase = true)

                val senderName = if (isSentByMe) (Constants.NAME ?: "") else contactName.ifEmpty { bareContact }

                val msgDay = todayKeyFormat.format(stamp)
                val groupKey = if (todayKey == msgDay) "Today" else dateFormatter.format(stamp)
                val timeFormatted = timeFormatter.format(stamp)

                Log.d("MessagesRepository", "Parsed message: isSentByMe=$isSentByMe, from=$from, body=${body.take(20)} @ $formattedCreatedAt")

                messages.add(
                    ChatMessageItem(
                        id = stanzaId ?: "${stamp.time}_${from}_${messages.size}",
                        message = body,
                        isSentByMe = isSentByMe,
                        senderName = senderName,
                        createdAtFormatted = formattedCreatedAt,
                        dateGroupHeader = groupKey,
                        timeFormatted = timeFormatted,
                        isCurrentChat = false
                    )
                )
            }

            Log.d("MessagesRepository", "Returning ${messages.size} parsed messages for $contactID")
            Result.success(messages)
        } catch (e: Exception) {
            Log.e("MessagesRepository", "fetchArchiveMessages failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun sendMessage(text: String, contactJid: String, currentJid: String) {
        val firmName = Constants.FIRM_NAME ?: ""
        val constName = Constants.NAME ?: ""
        val constNameNew = Constants.NAME_NEW ?: ""
        val name = if (Constants.USER_ID == "admin") {
            firmName.ifEmpty { "Admin" }
        } else {
            val currentName = constName.ifEmpty { constNameNew }
            currentName.ifEmpty { firmName.ifEmpty { "User" } }
        }
        val subject = "$name ##$currentJid## #N#$name#N#"

        // Send message once on active connection
        ChatConnection.sendMessage(text, contactJid, subject)
    }

    fun sendChatState(contactJid: String, state: String) {
        ChatConnection.sendChatState(contactJid, state)
    }

    fun sendReadMarker(contactJid: String, stanzaId: String) {
        if (stanzaId.isNotEmpty()) {
            ChatConnection.sendDisplayedMarker(contactJid, stanzaId)
        }
    }
}
