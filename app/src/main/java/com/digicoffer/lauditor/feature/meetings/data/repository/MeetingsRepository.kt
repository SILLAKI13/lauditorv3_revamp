package com.digicoffer.lauditor.feature.meetings.data.repository

import android.content.Context
import android.view.View
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume

class MeetingsRepository(private val context: Context) {

    suspend fun fetchWeeklyEvents(timezoneOffset: Long, startDate: String, endDate: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
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
                "v3/events/$timezoneOffset/W$startDate-$endDate",
                "Events_List",
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

    suspend fun fetchMonthlyEvents(timezoneOffset: Long, dateStr: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
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
                "v3/events/$timezoneOffset/M$dateStr",
                "Events_List",
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

    suspend fun fetchAppointments(startDate: String, endDate: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
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
                "v3/appointments?schedule_after=$startDate&schedule_before=$endDate",
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

    suspend fun updateEventRsvp(eventId: String, response: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            val postData = JSONObject().apply {
                put("rsvp_response", response)
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
                "v3/event/response/$eventId",
                "Event_rsvp",
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

    suspend fun updateAppointmentRsvp(appointmentId: String, status: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            val postData = JSONObject().apply {
                put("rsvp_status", status)
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
                "v3/appointments/$appointmentId/rsvp",
                "Appointment_rsvp",
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

    suspend fun fetchEventDetails(eventId: String, timezoneOffset: Long): HttpResultDo = suspendCancellableCoroutine { continuation ->
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
                "v3/event/$eventId/$timezoneOffset",
                "EVENT DETAILS",
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
}
