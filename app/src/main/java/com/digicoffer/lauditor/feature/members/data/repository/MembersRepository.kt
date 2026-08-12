package com.digicoffer.lauditor.feature.members.data.repository

import android.content.Context
import android.view.View
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume

class MembersRepository(private val context: Context) {

    suspend fun fetchMembers(): HttpResultDo = suspendCancellableCoroutine { continuation ->
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

    suspend fun fetchGroups(): HttpResultDo = suspendCancellableCoroutine { continuation ->
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
                "v3/groups",
                "Get Groups",
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

    suspend fun createMember(
        name: String,
        designation: String,
        defaultRate: String,
        currency: String,
        email: String,
        emailConfirm: String,
        groups: List<String>
    ): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            val postData = JSONObject().apply {
                put("name", name)
                put("designation", designation)
                put("defaultRate", defaultRate)
                put("currency", currency)
                put("email", email)
                put("emailConfirm", emailConfirm)
                put("groups", JSONArray(groups))
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
                "v3/member",
                "Create Members",
                postData.toString()
            )
        } catch (e: Exception) {
            val failedResult = HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Exception
                responseContent = e.message ?: ""
            }
            continuation.resume(failedResult)
        }
    }

    suspend fun updateMember(
        id: String,
        name: String,
        designation: String,
        defaultRate: String,
        currency: String,
        email: String,
        emailConfirm: String
    ): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            val postData = JSONObject().apply {
                put("name", name)
                put("designation", designation)
                put("defaultRate", defaultRate)
                put("currency", currency)
                put("email", email)
                put("emailConfirm", emailConfirm)
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
                "v3/member/$id",
                "Update Members",
                postData.toString()
            )
        } catch (e: Exception) {
            val failedResult = HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Exception
                responseContent = e.message ?: ""
            }
            continuation.resume(failedResult)
        }
    }

    suspend fun updateGroupAccess(id: String, groups: List<String>): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            val postData = JSONObject().apply {
                put("groups", JSONArray(groups))
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
                "v3/member/$id",
                "Update Group Access",
                postData.toString()
            )
        } catch (e: Exception) {
            val failedResult = HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Exception
                responseContent = e.message ?: ""
            }
            continuation.resume(failedResult)
        }
    }

    suspend fun resetPassword(memberId: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            val postData = JSONObject().apply {
                put("memberId", memberId)
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
                "v3/member/resetpwd",
                "Reset Password",
                postData.toString()
            )
        } catch (e: Exception) {
            val failedResult = HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Exception
                responseContent = e.message ?: ""
            }
            continuation.resume(failedResult)
        }
    }

    suspend fun deleteMember(id: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        continuation.resume(httpResult)
                    }
                    override fun onClick(view: View) {}
                },
                context,
                WebServiceHelper.RestMethodType.DELETE,
                "v3/member/$id",
                "Delete Member",
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

    suspend fun upgradePracticePartner(id: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            val postData = JSONObject().apply {
                put("id", id)
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
                "v3/convert/practice-partner",
                "Upgrade as Practice Partner",
                postData.toString()
            )
        } catch (e: Exception) {
            val failedResult = HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Exception
                responseContent = e.message ?: ""
            }
            continuation.resume(failedResult)
        }
    }
}
