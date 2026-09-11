package com.digicoffer.lauditor.feature.timesheets.data.repository

import android.content.Context
import android.view.View
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume
import java.util.ArrayList

class TimesheetsRepository(private val context: Context) {

    suspend fun fetchTimesheets(date: String, submitted: Boolean): HttpResultDo = suspendCancellableCoroutine { continuation ->
        if (com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.TOKEN == "mock_token_123") {
            android.util.Log.d("TIMESHEET_DEBUG", "fetchTimesheets mock response for date=$date, submitted=$submitted")
            continuation.resume(HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Success
                responseContent = getMockTimesheetsJson(date, submitted)
            })
            return@suspendCancellableCoroutine
        }
        try {
            val url = if (date.isEmpty()) {
                if (submitted) "v3/user/timesheets?submitted=true" else "v3/user/timesheets"
            } else {
                "v3/user/timesheets/$date?submitted=$submitted"
            }
            android.util.Log.d("TIMESHEET_DEBUG", "fetchTimesheets calling GET url: $url")
            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        android.util.Log.d("TIMESHEET_DEBUG", "fetchTimesheets onAsyncTaskComplete result=${httpResult.result}, response=${httpResult.responseContent}")
                        continuation.resume(httpResult)
                    }
                    override fun onClick(view: View) {}
                },
                context,
                WebServiceHelper.RestMethodType.GET,
                url,
                "TimeSheets",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            android.util.Log.e("TIMESHEET_DEBUG", "fetchTimesheets exception: ${e.message}", e)
            continuation.resume(createFailedResult(e))
        }
    }

    suspend fun fetchTasks(matterType: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        if (com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.TOKEN == "mock_token_123") {
            continuation.resume(HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Success
                responseContent = getMockTasksJson()
            })
            return@suspendCancellableCoroutine
        }
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
                "v3/event/tasks/$matterType",
                "Tasks",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            continuation.resume(createFailedResult(e))
        }
    }

    suspend fun saveTimesheet(data: JSONObject): HttpResultDo = suspendCancellableCoroutine { continuation ->
        if (com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.TOKEN == "mock_token_123") {
            continuation.resume(HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Success
                responseContent = "{\"error\": false, \"msg\": \"Mock save successful\"}"
            })
            return@suspendCancellableCoroutine
        }
        try {
            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        continuation.resume(httpResult)
                    }
                    override fun onClick(view: View) {}
                },
                context,
                WebServiceHelper.RestMethodType.POST,
                "v3/user/timesheets",
                "SAVE TIMESHEETS",
                data.toString()
            )
        } catch (e: Exception) {
            continuation.resume(createFailedResult(e))
        }
    }

    suspend fun updateTimesheet(data: JSONObject): HttpResultDo = suspendCancellableCoroutine { continuation ->
        if (com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.TOKEN == "mock_token_123") {
            continuation.resume(HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Success
                responseContent = "{\"error\": false, \"msg\": \"Mock update successful\"}"
            })
            return@suspendCancellableCoroutine
        }
        try {
            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        continuation.resume(httpResult)
                    }
                    override fun onClick(view: View) {}
                },
                context,
                WebServiceHelper.RestMethodType.PUT,
                "v3/user/timesheets",
                "Edit Timesheet",
                data.toString()
            )
        } catch (e: Exception) {
            continuation.resume(createFailedResult(e))
        }
    }

    suspend fun deleteTimesheet(data: JSONObject): HttpResultDo = suspendCancellableCoroutine { continuation ->
        if (com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.TOKEN == "mock_token_123") {
            continuation.resume(HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Success
                responseContent = "{\"error\": false, \"msg\": \"Mock delete successful\"}"
            })
            return@suspendCancellableCoroutine
        }
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
                "v3/user/timesheets",
                "Delete TimeSheet",
                data.toString()
            )
        } catch (e: Exception) {
            continuation.resume(createFailedResult(e))
        }
    }

    suspend fun submitTimesheets(date: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        if (com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.TOKEN == "mock_token_123") {
            continuation.resume(HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Success
                responseContent = "{\"error\": false, \"msg\": \"Mock submit successful\"}"
            })
            return@suspendCancellableCoroutine
        }
        try {
            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        continuation.resume(httpResult)
                    }
                    override fun onClick(view: View) {}
                },
                context,
                WebServiceHelper.RestMethodType.POST,
                "v3/user/timesheets/freeze/$date",
                "Submit TimeSheet",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            continuation.resume(createFailedResult(e))
        }
    }

    suspend fun fetchAggregatedTeamMembers(date: String, isweek: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        if (com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.TOKEN == "mock_token_123") {
            continuation.resume(HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Success
                responseContent = getMockTeamMembersJson(isweek)
            })
            return@suspendCancellableCoroutine
        }
        try {
            val cleanDate = date.replace("-", "")
            val url = if (isweek == "week") {
                if (cleanDate.isEmpty()) "matter/timesheets/tms-all-weekly" else "matter/timesheets/tms-all-weekly-$cleanDate"
            } else {
                if (cleanDate.isEmpty()) "matter/timesheets/tms-all-monthly" else "matter/timesheets/tms-all-monthly-$cleanDate"
            }
            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        continuation.resume(httpResult)
                    }
                    override fun onClick(view: View) {}
                },
                context,
                WebServiceHelper.RestMethodType.GET,
                url,
                "Team Members",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            continuation.resume(createFailedResult(e))
        }
    }

    suspend fun fetchAggregatedProjects(date: String, isweek: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        if (com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.TOKEN == "mock_token_123") {
            continuation.resume(HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Success
                responseContent = getMockProjectsJson()
            })
            return@suspendCancellableCoroutine
        }
        try {
            val cleanDate = date.replace("-", "")
            val url = if (isweek == "week") {
                if (cleanDate.isEmpty()) "matter/timesheets/project-all-weekly" else "matter/timesheets/project-all-weekly-$cleanDate"
            } else {
                if (cleanDate.isEmpty()) "matter/timesheets/project-all-monthly" else "matter/timesheets/project-all-monthly-$cleanDate"
            }
            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        continuation.resume(httpResult)
                    }
                    override fun onClick(view: View) {}
                },
                context,
                WebServiceHelper.RestMethodType.GET,
                url,
                "Projects",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            continuation.resume(createFailedResult(e))
        }
    }

    private fun createFailedResult(e: Exception): HttpResultDo {
        return HttpResultDo().apply {
            result = WebServiceHelper.ServiceCallStatus.Exception
            responseContent = e.message ?: "Exception occurred"
        }
    }

    private fun getMockTimesheetsJson(date: String, submitted: Boolean): String {
        val format = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
        val tempCal = java.util.Calendar.getInstance()
        while (tempCal.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.MONDAY) {
            tempCal.add(java.util.Calendar.DATE, -1)
        }
        val weekDates = ArrayList<String>()
        for (i in 0..6) {
            weekDates.add(format.format(tempCal.time))
            tempCal.add(java.util.Calendar.DATE, 1)
        }

        val mon = weekDates[0]
        val tue = weekDates[1]
        val wed = weekDates[2]
        val thu = weekDates[3]
        val fri = weekDates[4]
        val sat = weekDates[5]
        val sun = weekDates[6]

        val isFrozenValue = submitted

        return """
        {
          "dates": {
            "isFrozen": ${isFrozenValue}
          },
          "timesheetList": {
            "wTotal": "8 Hours 0 Minutes",
            "weekTotal": {
              "Mon": "2",
              "Tue": "4",
              "Wed": "0",
              "Thu": "2",
              "Fri": "0",
              "Sat": "0",
              "Sun": "0"
            },
            "activeProjects": [
              {
                "matterName": "Lexiz Patent Application",
                "matterId": "matter_patent",
                "is_editable": true,
                "matterType": "legal"
              },
              {
                "matterName": "General Consultation",
                "matterId": "matter_consult",
                "is_editable": true,
                "matterType": "general"
              }
            ],
            "matters": [
              {
                "matterName": "Lexiz Patent Application",
                "matterId": "matter_patent",
                "is_editable": true,
                "matterType": "legal",
                "tasks": [
                  {
                    "taskName": "Document Review",
                    "billing": "billable",
                    "Mon": {
                      "hours": "2",
                      "minutes": "0",
                      "taskId": "task_log_mon_1",
                      "matterId": "matter_patent",
                      "isLinkedWithCalendar": false,
                      "permissions": {
                        "editProject": true,
                        "editTask": true,
                        "editStatus": true,
                        "editDate": true,
                        "editHours": true,
                        "editMinutes": true
                      }
                    },
                    "Tue": {
                      "hours": "4",
                      "minutes": "0",
                      "taskId": "task_log_tue_1",
                      "matterId": "matter_patent",
                      "isLinkedWithCalendar": false,
                      "permissions": {
                        "editProject": true,
                        "editTask": true,
                        "editStatus": true,
                        "editDate": true,
                        "editHours": true,
                        "editMinutes": true
                      }
                    }
                  }
                ]
              },
              {
                "matterName": "General Consultation",
                "matterId": "matter_consult",
                "is_editable": true,
                "matterType": "general",
                "tasks": [
                  {
                    "taskName": "Client Intake",
                    "billing": "nonbillable",
                    "Thu": {
                      "hours": "2",
                      "minutes": "0",
                      "taskId": "task_log_thu_1",
                      "matterId": "matter_consult",
                      "isLinkedWithCalendar": false,
                      "permissions": {
                        "editProject": true,
                        "editTask": true,
                        "editStatus": true,
                        "editDate": true,
                        "editHours": true,
                        "editMinutes": true
                      }
                    }
                  }
                ]
              }
            ]
          }
        }
        """.trimIndent()
    }

    private fun getMockTasksJson(): String {
        return """
        {
          "tasks": [
            {
              "displayValue": "Document Review",
              "returnValue": "doc_review"
            },
            {
              "displayValue": "Client Intake",
              "returnValue": "client_intake"
            },
            {
              "displayValue": "Case Research",
              "returnValue": "case_research"
            }
          ]
        }
        """.trimIndent()
    }

    private fun getMockTeamMembersJson(isweek: String): String {
        return if (isweek == "month") {
            """
            {
              "timesheets": [
                {
                  "name": "Jane Smith",
                  "bw1": "10", "bw2": "8", "bw3": "12", "bw4": "14", "bw5": "5",
                  "tb": "49",
                  "nbw1": "2", "nbw2": "4", "nbw3": "3", "nbw4": "1", "nbw5": "0",
                  "tnb": "10",
                  "total": "59 Hours"
                },
                {
                  "name": "Bob Johnson",
                  "bw1": "5", "bw2": "6", "bw3": "4", "bw4": "5", "bw5": "2",
                  "tb": "22",
                  "nbw1": "1", "nbw2": "0", "nbw3": "2", "nbw4": "1", "nbw5": "0",
                  "tnb": "4",
                  "total": "26 Hours"
                }
              ]
            }
            """.trimIndent()
        } else {
            """
            {
              "timesheets": [
                {
                  "id": "tm_jane",
                  "name": "Jane Smith",
                  "tb": "12",
                  "tnb": "2",
                  "total": "14 Hours"
                },
                {
                  "id": "tm_bob",
                  "name": "Bob Johnson",
                  "tb": "6",
                  "tnb": "1",
                  "total": "7 Hours"
                }
              ]
            }
            """.trimIndent()
        }
    }

    private fun getMockProjectsJson(): String {
        return """
        {
          "timesheets": {
            "grandTotal": {
              "billable": "18",
              "nonbillable": "3",
              "total": "21 Hours"
            },
            "data": [
              {
                "caseNo": "CASE-101",
                "projectName": "Lexiz Patent Application",
                "matterId": "matter_patent",
                "clientNames": ["Client Alpha", "Client Beta"],
                "teamMembers": [
                  {
                    "name": "Jane Smith",
                    "billableHours": "12",
                    "nonBillablehours": "2",
                    "total": "14"
                  },
                  {
                    "name": "Bob Johnson",
                    "billableHours": "6",
                    "nonBillablehours": "1",
                    "total": "7"
                  }
                ]
              }
            ]
          }
        }
        """.trimIndent()
    }
}
