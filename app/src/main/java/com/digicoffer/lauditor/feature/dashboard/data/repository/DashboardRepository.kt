package com.digicoffer.lauditor.feature.dashboard.data.repository

import android.content.Context
import android.util.Log
import android.view.View
import com.digicoffer.lauditor.CommonFiles.ChatService.ConversationMetaApi
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.VitacapeExtention
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.unreadclient_list
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.unreadcount_from_list
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.unreadteam_list
import com.digicoffer.lauditor.Dashboard.DahboardModels.UnreadCountModel
import com.digicoffer.lauditor.Dashboard.NewRevampViewModels.DashboardItem
import com.digicoffer.lauditor.Dashboard.NewRevampViewModels.DashboardParser
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.feature.dashboard.data.model.DashboardCardItemModel
import com.digicoffer.lauditor.feature.dashboard.data.model.DashboardSectionModel
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList
import kotlin.coroutines.resume

class DashboardRepository(private val context: Context) {

    companion object {
        private const val TAG = "DashboardRepository"
        private const val REQ_DASHBOARD = "DASHBOARD_ALL"
        private const val REQ_CHAT_LIST = "CHAT_LIST"
    }

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val message: String, val isNoInternet: Boolean = false) : Result<Nothing>()
    }

    suspend fun fetchDashboard(): Result<List<DashboardSectionModel>> =
        suspendCancellableCoroutine { continuation ->
            Constants.check_url()
            if (Constants.base_URL.isNullOrEmpty() && !Constants.PROF_URL.isNullOrEmpty()) {
                Constants.base_URL = Constants.PROF_URL
            }

            val endpoint = Constants.dashboardAllEndpoint ?: run {
                continuation.resume(Result.Error("Dashboard endpoint not configured"))
                return@suspendCancellableCoroutine
            }

            try {
                WebServiceHelper.callHttpWebService(
                    object : AsyncTaskCompleteListener {
                        override fun onClick(view: View) {}

                        override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                            if (httpResult.result != WebServiceHelper.ServiceCallStatus.Success) {
                                val isNoInternet = (httpResult.result == WebServiceHelper.ServiceCallStatus.Exception) &&
                                        (httpResult.responseContent == Constants.NO_INTERNET_MSG ||
                                                httpResult.responseContent.contains(Constants.NO_INTERNET_MSG, ignoreCase = true))

                                val errorMsg = if (isNoInternet) {
                                    Constants.NO_INTERNET_MSG
                                } else {
                                    httpResult.errorMessage?.takeIf { it.isNotBlank() }
                                        ?: "Something went wrong, please try again."
                                }
                                continuation.resume(Result.Error(errorMsg, isNoInternet))
                                return
                            }

                            try {
                                val json = JSONObject(httpResult.responseContent)
                                if (json.optBoolean("error", false)) {
                                    val msg = json.optString("msg", "Failed to fetch dashboard")
                                    continuation.resume(Result.Error(msg))
                                    return
                                }

                                val items = DashboardParser.parse(json)
                                val sections = buildSections(items)
                                continuation.resume(Result.Success(sections))
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing dashboard json", e)
                                continuation.resume(Result.Error("Something went wrong, please try again."))
                            }
                        }
                    },
                    context,
                    WebServiceHelper.RestMethodType.GET,
                    endpoint,
                    REQ_DASHBOARD,
                    JSONObject().toString()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error invoking dashboard API", e)
                continuation.resume(Result.Error(e.message ?: "Failed to load dashboard"))
            }
        }

    suspend fun fetchChatList(): Result<Boolean> =
        suspendCancellableCoroutine { continuation ->
            val endpoint = Constants.chatlistEndpoint ?: run {
                continuation.resume(Result.Error("Chat list endpoint not configured"))
                return@suspendCancellableCoroutine
            }

            try {
                WebServiceHelper.callHttpWebService(
                    object : AsyncTaskCompleteListener {
                        override fun onClick(view: View) {}

                        override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                            if (httpResult.result != WebServiceHelper.ServiceCallStatus.Success) {
                                continuation.resume(Result.Error("Failed to fetch chat list"))
                                return
                            }

                            try {
                                val result = JSONObject(httpResult.responseContent)
                                processChatList(result) {
                                    continuation.resume(Result.Success(true))
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing chat list json", e)
                                continuation.resume(Result.Error("Chat parse error"))
                            }
                        }
                    },
                    context,
                    WebServiceHelper.RestMethodType.GET,
                    endpoint,
                    REQ_CHAT_LIST,
                    JSONObject().toString()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error calling chat list API", e)
                continuation.resume(Result.Error(e.message ?: "Failed to call chat list"))
            }
        }

    private fun processChatList(result: JSONObject, onComplete: () -> Unit) {
        val teamArray = result.optJSONArray("team") ?: JSONArray()
        val clientArray = result.optJSONArray("clients") ?: JSONArray()

        unreadteam_list.clear()
        val teamChatList = ArrayList<String>()
        for (i in 0 until teamArray.length()) {
            val m = UnreadCountModel()
            val listId = teamArray.optString(i)
            m.fromjid = listId
            unreadteam_list.add(m)
            teamChatList.add(listId)
        }

        unreadclient_list.clear()
        val clientChatList = ArrayList<String>()
        for (i in 0 until clientArray.length()) {
            val m = UnreadCountModel()
            val listId = clientArray.optString(i)
            m.fromjid = listId
            unreadclient_list.add(m)
            clientChatList.add(listId)
        }

        val totalChatList = ArrayList<String>()
        val ext = VitacapeExtention ?: ""
        for (id in teamChatList) {
            totalChatList.add(id + ext)
        }
        for (id in clientChatList) {
            totalChatList.add(id + ext)
        }

        val totalArray = JSONArray()
        for (item in totalChatList) {
            totalArray.put(item)
        }
        Constants.totalchatclientlist = totalArray

        ConversationMetaApi.fetch(context, totalChatList, object : ConversationMetaApi.MetaCallback {
            override fun onMetaReady(metaMap: Map<String, ConversationMetaApi.ConversationMeta>) {
                Constants.unreadList.clear()
                unreadcount_from_list.clear()

                for (jid in totalChatList) {
                    val guid = if (ext.isNotEmpty()) jid.replace(ext, "") else jid
                    val meta = metaMap[guid] ?: continue

                    val unreadCountModel = UnreadCountModel()
                    unreadCountModel.fromjid = guid
                    unreadCountModel.count = meta.unreadCount
                    Constants.unreadList.add(unreadCountModel)
                    unreadcount_from_list.add(unreadCountModel)
                }
                onComplete()
            }
        })
    }

    private fun buildSections(items: List<DashboardItem>): List<DashboardSectionModel> {
        val role = Constants.ROLE
        val category = Constants.CATEGORY

        val todayItems = ArrayList<DashboardCardItemModel>()
        val analyticsItems = ArrayList<DashboardCardItemModel>()
        val metricsItems = ArrayList<DashboardCardItemModel>()

        for (item in items) {
            if (!shouldShowCard(item.type, role, category)) continue
            val cardModel = DashboardCardItemModel(
                type = item.type,
                data = item.data,
                navigation = item.navigation
            )
            when (item.type) {
                DashboardItem.TYPE_MEETING,
                DashboardItem.TYPE_APPOINTMENT,
                DashboardItem.TYPE_MESSAGES,
                DashboardItem.TYPE_NOTIFICATION -> todayItems.add(cardModel)

                DashboardItem.TYPE_APPOINTMENT_TREND,
                DashboardItem.TYPE_REVENUE_TREND,
                DashboardItem.TYPE_APPOINTMENT_REVENUE_TREND,
                DashboardItem.TYPE_MATTER,
                DashboardItem.TYPE_STORAGE -> analyticsItems.add(cardModel)

                DashboardItem.TYPE_BILLABLE,
                DashboardItem.TYPE_APPROX_REVENUE,
                DashboardItem.TYPE_SUBSCRIPTION,
                DashboardItem.TYPE_HIRING -> metricsItems.add(cardModel)
            }
        }

        val result = ArrayList<DashboardSectionModel>()
        if (todayItems.isNotEmpty()) {
            result.add(
                DashboardSectionModel(
                    sectionId = DashboardSectionModel.SECTION_TODAY,
                    title = "Today's Activities",
                    items = todayItems
                )
            )
        }
        if (analyticsItems.isNotEmpty()) {
            result.add(
                DashboardSectionModel(
                    sectionId = DashboardSectionModel.SECTION_ANALYTICS,
                    title = "Analytics Overview",
                    items = analyticsItems
                )
            )
        }
        if (metricsItems.isNotEmpty()) {
            result.add(
                DashboardSectionModel(
                    sectionId = DashboardSectionModel.SECTION_METRICS,
                    title = "Business Metrics",
                    items = metricsItems
                )
            )
        }
        return result
    }

    private fun shouldShowCard(type: Int, role: String?, category: String?): Boolean {
        val allowed = ArrayList<Int>()
        when (role) {
            "SU" -> {
                allowed.addAll(
                    listOf(
                        DashboardItem.TYPE_MEETING, DashboardItem.TYPE_APPOINTMENT,
                        DashboardItem.TYPE_MESSAGES, DashboardItem.TYPE_NOTIFICATION,
                        DashboardItem.TYPE_APPOINTMENT_TREND, DashboardItem.TYPE_REVENUE_TREND,
                        DashboardItem.TYPE_APPOINTMENT_REVENUE_TREND,
                        DashboardItem.TYPE_MATTER, DashboardItem.TYPE_STORAGE,
                        DashboardItem.TYPE_BILLABLE, DashboardItem.TYPE_APPROX_REVENUE,
                        DashboardItem.TYPE_SUBSCRIPTION
                    )
                )
                if ("solo" != category) allowed.add(DashboardItem.TYPE_HIRING)
            }
            "AAM" -> allowed.addAll(
                listOf(
                    DashboardItem.TYPE_MEETING, DashboardItem.TYPE_APPOINTMENT,
                    DashboardItem.TYPE_MESSAGES, DashboardItem.TYPE_NOTIFICATION,
                    DashboardItem.TYPE_APPOINTMENT_TREND, DashboardItem.TYPE_REVENUE_TREND,
                    DashboardItem.TYPE_APPOINTMENT_REVENUE_TREND,
                    DashboardItem.TYPE_STORAGE, DashboardItem.TYPE_APPROX_REVENUE,
                    DashboardItem.TYPE_SUBSCRIPTION
                )
            )
            "GH" -> allowed.addAll(
                listOf(
                    DashboardItem.TYPE_MEETING, DashboardItem.TYPE_APPOINTMENT,
                    DashboardItem.TYPE_MESSAGES, DashboardItem.TYPE_NOTIFICATION,
                    DashboardItem.TYPE_APPOINTMENT_TREND, DashboardItem.TYPE_REVENUE_TREND,
                    DashboardItem.TYPE_APPOINTMENT_REVENUE_TREND,
                    DashboardItem.TYPE_MATTER, DashboardItem.TYPE_STORAGE,
                    DashboardItem.TYPE_BILLABLE, DashboardItem.TYPE_APPROX_REVENUE
                )
            )
            "TM" -> allowed.addAll(
                listOf(
                    DashboardItem.TYPE_MEETING, DashboardItem.TYPE_APPOINTMENT,
                    DashboardItem.TYPE_MESSAGES, DashboardItem.TYPE_NOTIFICATION,
                    DashboardItem.TYPE_APPOINTMENT_TREND, DashboardItem.TYPE_REVENUE_TREND,
                    DashboardItem.TYPE_APPOINTMENT_REVENUE_TREND,
                    DashboardItem.TYPE_MATTER, DashboardItem.TYPE_BILLABLE
                )
            )
        }
        return allowed.contains(type)
    }
}
