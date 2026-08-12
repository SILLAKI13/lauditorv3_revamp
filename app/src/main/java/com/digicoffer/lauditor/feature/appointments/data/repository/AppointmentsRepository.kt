package com.digicoffer.lauditor.feature.appointments.data.repository

import android.content.Context
import android.view.View
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume

class AppointmentsRepository(private val context: Context) {

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

    suspend fun cancelAppointment(appointmentId: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
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
                "v3/appointments/$appointmentId/cancel",
                "Cancel_Appointments",
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

    suspend fun deleteAppointment(appointmentId: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
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
                "v3/appointments/$appointmentId/delete",
                "Delete_Appointment",
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

    suspend fun fetchHistory(clientId: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
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
                "v3/appointment/history?client_id=$clientId",
                "Appointments_History",
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

    suspend fun addNote(appointmentId: String, note: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            val postData = JSONObject().apply {
                put("note", note)
                put("note_details", "")
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
                "v3/appointments/$appointmentId/notes",
                "Appointments_History_Notes",
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

    suspend fun updateNote(appointmentId: String, noteId: String, note: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            val postData = JSONObject().apply {
                put("note_id", noteId)
                put("note", note)
                put("note_details", "")
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
                "v3/appointments/$appointmentId/notes",
                "Update_Notes",
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

    suspend fun deleteNote(appointmentId: String, noteId: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            val postData = JSONObject().apply {
                put("note_id", noteId)
            }
            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        continuation.resume(httpResult)
                    }
                    override fun onClick(view: View) {}
                },
                context,
                WebServiceHelper.RestMethodType.DELETE,
                "v3/appointments/$appointmentId/notes",
                "Delete_Notes",
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
