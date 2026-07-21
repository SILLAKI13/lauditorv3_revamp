package com.digicoffer.lauditor.Meetings.ViewModels

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.Appointments.Models.PaymentModel
import com.digicoffer.lauditor.Meetings.Models.CalendarItem
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO
import com.digicoffer.lauditor.Meetings.Models.Events_Do
import com.digicoffer.lauditor.Meetings.Models.InviteesInternal_Model
import com.digicoffer.lauditor.Chat.ViewModels.Chat
import com.digicoffer.lauditor.Groups.Models.ActionModel
import com.digicoffer.lauditor.Matter.Models.DocumentsModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.PdfUtils.File_Content_Type
import com.digicoffer.lauditor.CommonFiles.PdfUtils.RetrievePDFfromUrl
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.github.barteksc.pdfviewer.PDFView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class Events_Adapter : RecyclerView.Adapter<Events_Adapter.MyViewHolder>, Filterable, View.OnClickListener, AsyncTaskCompleteListener {

    private var context: EventListener? = null
    var dialog: AlertDialog? = null
    private var isDetailsVisible = true
    private var currentFilter = Meetings.FilterType.ALL
    private val visibleItemPositions = SparseBooleanArray()
    var progress_dialog: Dialog? = null
    var event_details_list = ArrayList<Event_Details_DO>()
    var inviteesInternalArrayList = ArrayList<InviteesInternal_Model>()
    var mcontext: Context? = null
    var documentsModel = DocumentsModel()
    var documentsList: MutableList<DocumentsModel> = ArrayList()
    var list_item = ArrayList<CalendarItem>()
    var filtered_list = ArrayList<CalendarItem>()
    var event_details = JSONObject()
    var event_id: String? = null
    var isRecurring = false
    var isRsvpChanged = false
    var rsvp_value: String? = null
    var event_delete_scope = ""
    var activity: Activity? = null
    private var my_view_holder: MyViewHolder? = null
    var dialog1: Dialog? = null
    var list_item1 = ArrayList<Events_Do>()
    var filtered_list1 = ArrayList<Events_Do>()

    private var expandedPosition = -1
    private var recyclerView: RecyclerView? = null

    constructor(
        events_list: ArrayList<Events_Do>,
        mcontextListener: EventListener,
        context: Context,
        activity: Activity,
        event_details_list: ArrayList<Event_Details_DO>,
        dialog: Dialog
    ) {
        this.list_item1 = events_list
        this.filtered_list1 = events_list
        this.context = mcontextListener
        this.mcontext = context
        this.activity = activity
        this.event_details_list = event_details_list
        this.dialog1 = dialog
        this.filtered_list = ArrayList()
        for (event in events_list) {
            this.filtered_list.add(CalendarItem(event))
        }
        this.list_item = this.filtered_list
    }

    constructor(
        events_list: ArrayList<Events_Do>,
        mcontextListener: EventListener,
        context: Context,
        activity: Activity
    ) {
        this.filtered_list = ArrayList()
        for (event in events_list) {
            this.filtered_list.add(CalendarItem(event))
        }
        this.list_item = this.filtered_list
        this.context = mcontextListener
        this.mcontext = context
        this.activity = activity
    }

    constructor(
        events_list: ArrayList<Events_Do>,
        appointments_list: ArrayList<AppointmentModel>,
        mcontextListener: EventListener,
        context: Context,
        activity: Activity
    ) {
        this.filtered_list = ArrayList()
        for (event in events_list) {
            this.filtered_list.add(CalendarItem(event))
        }
        for (appointment in appointments_list) {
            this.filtered_list.add(CalendarItem(appointment))
        }
        this.list_item = this.filtered_list
        this.context = mcontextListener
        this.mcontext = context
        this.activity = activity
    }

    interface EventListener {
        fun onEvent(event_details_list: ArrayList<Event_Details_DO>)
        fun delete_events(events_do: Events_Do)
        fun load_events()
        fun delete(event_id: String, recur: Boolean)
    }

    override fun onClick(view: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progress_dialog != null && progress_dialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }
        try {
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val result = JSONObject(httpResult.responseContent)
                    if (httpResult.requestType == "EVENT DETAILS") {
                        if (!result.getBoolean("error")) {
                            event_details_list.clear()
                            load_event_details(result.getJSONObject("event"), event_id ?: "", isRecurring)
                        }
                    } else if (httpResult.requestType == "Cancel_Appointments") {
                        AndroidUtils.showAlert(result.getString("msg"), activity)
                        context?.load_events()
                    } else if (httpResult.requestType == "Event_rsvp") {
                        AndroidUtils.showAlert(result.getString("msg"), activity)
                        context?.load_events()
                    } else if (httpResult.requestType == "Appointment_rsvp") {
                        AndroidUtils.showAlert(result.getString("msg"), activity)
                        context?.load_events()
                    } else if (httpResult.requestType == "View Doc") {
                        val error = result.getBoolean("error")
                        if (!error) {
                            val url = result.getJSONObject("data").getString("url")
                            checkViewType(url, documentsModel)
                            Log.d("TAG_Image", url)
                        } else {
                            AndroidUtils.showAlert(result.optString("msg"), activity)
                        }
                    } else if (httpResult.requestType == "Decrypt Doc" || httpResult.requestType == "Other Doc View") {
                        val error = result.getBoolean("error")
                        if (!error) {
                            val url = result.getJSONObject("data").getString("url")
                            display_doc(url, documentsModel)
                            Log.d("TAG_Image", url)
                        } else {
                            AndroidUtils.showAlert(result.optString("msg"), activity)
                        }
                    }
                } catch (e: JSONException) {
                    e.fillInStackTrace()
                }
            } else if (httpResult.result == WebServiceHelper.ServiceCallStatus.Failed) {
                if (progress_dialog != null && progress_dialog!!.isShowing) {
                    AndroidUtils.dismiss_dialog(progress_dialog)
                }
                try {
                    val result = JSONObject(httpResult.responseContent)
                    AndroidUtils.showErrorAlert(result.optString("msg"), activity)
                } catch (e: Exception) {
                    e.fillInStackTrace()
                }
            } else {
                if (progress_dialog != null && progress_dialog!!.isShowing) {
                    AndroidUtils.dismiss_dialog(progress_dialog)
                }
                AndroidUtils.showErrorAlert(httpResult.responseContent.toString(), activity)
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    fun setRecyclerView(rv: RecyclerView) {
        this.recyclerView = rv
    }

    private fun safeNotify(position: Int) {
        val r = Runnable {
            if (position >= 0 && position < itemCount) notifyItemChanged(position)
        }
        if (recyclerView != null) {
            recyclerView!!.post(r)
        } else {
            android.os.Handler(android.os.Looper.getMainLooper()).post(r)
        }
    }

    private fun safeNotifyAll() {
        val r = Runnable { notifyDataSetChanged() }
        if (recyclerView != null) {
            recyclerView!!.post(r)
        } else {
            android.os.Handler(android.os.Looper.getMainLooper()).post(r)
        }
    }

    fun collapseExpanded() {
        if (expandedPosition == -1) return
        val pos = expandedPosition
        expandedPosition = -1
        safeNotify(pos)
    }

    private fun checkViewType(url: String, sharedDocumentsDo: DocumentsModel) {
        val isImage = File_Content_Type.isImage(sharedDocumentsDo.contentType)
        val isPDF = File_Content_Type.isPDF(sharedDocumentsDo.contentType)
        val isEncrypted = sharedDocumentsDo.added_encryption || sharedDocumentsDo.is_encrypted
        if (isEncrypted) {
            callDecryptApi(sharedDocumentsDo.docid ?: "")
        } else if (!isPDF && !isImage) {
            callOtherDocViewApi(sharedDocumentsDo.docid ?: "")
        } else {
            display_doc(url, sharedDocumentsDo)
        }
    }

    private fun display_doc(url: String, sharedDocumentsDo: DocumentsModel) {
        val act = activity ?: return
        val dialogBuilder = AlertDialog.Builder(act)
        val view = act.layoutInflater.inflate(R.layout.view_documents, null)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_pdf)
        val iv_image = view.findViewById<ImageView>(R.id.doc_image)
        val idPDFView = view.findViewById<PDFView>(R.id.idPDFView)
        val webView = view.findViewById<WebView>(R.id.doc_webview)
        val header = view.findViewById<TextView>(R.id.header_name)
        val iv_close_edit_docs = view.findViewById<ImageView>(R.id.close_edit_docs)
        val isImage = File_Content_Type.isImage(sharedDocumentsDo.contentType)
        header.text = sharedDocumentsDo.name
        val dialog = dialogBuilder.create()
        val pdfTask = arrayOfNulls<RetrievePDFfromUrl>(1)
        iv_close_edit_docs.setOnClickListener { v ->
            try {
                idPDFView?.recycle()
                if (pdfTask[0] != null) {
                    pdfTask[0]!!.cancelLoading()
                    pdfTask[0]!!.cancel(true)
                }
            } catch (ignored: Exception) {
            }
            dialog.dismiss()
        }
        val lowerUrl = url.lowercase(Locale.US)
        val urlIsPDF = lowerUrl.contains("application/pdf") || lowerUrl.contains(".pdf")
        if (urlIsPDF) {
            idPDFView.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            pdfTask[0] = RetrievePDFfromUrl(idPDFView, progressBar)
            pdfTask[0]!!.execute(url)
        } else {
            if (isImage) {
                iv_image.visibility = View.VISIBLE
                Glide.with(act).load(url)
                    .placeholder(R.drawable.progress_animation).centerCrop().into(iv_image)
            } else {
                idPDFView.visibility = View.VISIBLE
                progressBar.visibility = View.VISIBLE
                pdfTask[0] = RetrievePDFfromUrl(idPDFView, progressBar)
                pdfTask[0]!!.execute(url)
            }
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    fun callOtherDocViewApi(id: String) {
        val act = activity ?: return
        try {
            progress_dialog = AndroidUtils.get_progress(act)
            WebServiceHelper.callHttpWebService(
                this, act, WebServiceHelper.RestMethodType.GET,
                Constants.base_URL + "v3/document/" + id + "/view", "Other Doc View",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    override fun getFilter(): Filter? {
        return null
    }

    private fun load_event_details(event_details: JSONObject, event_id: String, isRecurring: Boolean) {
        val act = activity ?: return
        event_details_list.clear()
        this.event_details = event_details
        try {
            val event_details_do = Event_Details_DO()
            event_details_do.event_type = event_details.getString("event_type")
            event_details_do.id = event_details.getString("id")
            event_details_do.title = event_details.getString("title")
            event_details_do.description = event_details.getString("description")
            event_details_do.from_ts = event_details.getString("from_ts")
            event_details_do.all_day = event_details.getBoolean("allday")
            val from_ts = event_details_do.from_ts
            val event_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss")
            event_details_do.date = AndroidUtils.getDateToString(event_date, "dd-MM-yyyy")
            event_details_do.is_linked_with_timesheet = event_details.optBoolean("is_linked_with_timesheet")
            event_details_do.isRecurring = event_details.getBoolean("isrecurring")
            event_details_do.repeat_interval = event_details.getString("repeat_interval")
            event_details_do.location = event_details.getString("location")
            event_details_do.dialin = event_details.getString("dialin")
            event_details_do.to_ts = event_details.getString("to_ts")
            event_details_do.offset = event_details.getString("timezone_offset")
            event_details_do.offset_location = event_details.getString("timezone_location")
            event_details_do.converted_Start_time = AndroidUtils.getDateToString(event_date, "HH:mm")
            event_details_do.owner = event_details.getBoolean("owner")
            val to_ts = event_details_do.to_ts
            event_details_do.meeting_link = event_details.getString("meeting_link")
            val event_date2 = AndroidUtils.stringToDateTimeDefault(to_ts, "yyyy-MM-dd'T'HH:mm:ss")
            event_details_do.converted_End_time = AndroidUtils.getDateToString(event_date2, "HH:mm")
            event_details_do.notifications = event_details.getJSONArray("notifications")
            event_details_do.owner_name = event_details.getString("owner_name")
            event_details_do.attachments = event_details.getJSONArray("attachments")
            event_details_do.team_name = event_details.getJSONArray("invitees_internal")
            event_details_do.tm_name = event_details.getJSONArray("invitees_external")
            event_details_do.corporate = event_details.optJSONArray("invitees_corporate")
            if (event_details.has("invitees_consumer_external")) {
                Log.d("ArrayListLog", event_details.getJSONArray("invitees_consumer_external").toString())
                event_details_do.consumer_external = event_details.getJSONArray("invitees_consumer_external")
            }
            if (event_details.has("matter_name")) {
                event_details_do.matter_name = event_details.getString("matter_name")
            }
            if (event_details.has("matter_id")) {
                event_details_do.matter_id = event_details.getString("matter_id")
            }
            if (event_details.has("matter_type")) {
                event_details_do.matter_type = event_details.getString("matter_type")
            }
            if (event_details.has("timesheet_added")) {
                event_details_do.timesheet_added = event_details.optBoolean("timesheet_added")
            }
            event_details_list.add(event_details_do)
            if (FLAG == "MORE") {
                load_more_details()
            } else {
                context?.onEvent(event_details_list)
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
    }

    fun View_doc(doc_id: String) {
        val act = activity ?: return
        try {
            progress_dialog = AndroidUtils.get_progress(act)
            WebServiceHelper.callHttpWebService(
                this, act, WebServiceHelper.RestMethodType.GET,
                "v3/document/$doc_id/view", "View Doc", JSONObject().toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    fun callDecryptApi(id: String) {
        val act = activity ?: return
        try {
            progress_dialog = AndroidUtils.get_progress(act)
            val jsonObject = JSONObject()
            jsonObject.put("docid", id)
            jsonObject.put("download", false)
            WebServiceHelper.callHttpWebService(
                this, act, WebServiceHelper.RestMethodType.POST,
                Constants.decryptUrl ?: "", "Decrypt Doc", jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun load_more_details() {
        val holder = my_view_holder ?: return
        val ctx = holder.itemView.context
        holder.ll_documents.removeAllViews()
        holder.ll_team_members.removeAllViews()
        holder.ll_clients.removeAllViews()
        holder.ll_corp_clients.removeAllViews()

        for (i in 0 until event_details_list.size) {
            val events_do = event_details_list[i]

            // Description
            holder.event_description.text = events_do.description
            if (!events_do.description.isNullOrEmpty()) {
                if (events_do.event_type == "reminders") {
                    holder.tv_event_description.visibility = View.GONE
                } else {
                    holder.tv_event_description.visibility = View.VISIBLE
                }
                holder.event_description.visibility = View.VISIBLE
            } else {
                holder.tv_event_description.visibility = View.GONE
                holder.event_description.visibility = View.GONE
            }

            // Notification
            val notification = events_do.notifications
            if (notification != null && notification.length() > 0) {
                try {
                    val notificationText = StringBuilder()
                    for (j in 0 until notification.length()) {
                        val value_notify = notification.getString(j)
                        val time = value_notify.split("-")
                        val time_format = time[0].trim()
                        var time_value = if (time.size > 1) time[1].trim() else ""
                        if (time_value.isNotEmpty()) {
                            time_value = time_value.substring(0, 1).uppercase(Locale.US) + time_value.substring(1)
                        }
                        if (j > 0) notificationText.append(", ")
                        notificationText.append(time_format).append(" ").append(time_value).append(" before")
                    }
                    holder.event_notification.text = notificationText.toString()
                    holder.ll_notifications_view.visibility = View.VISIBLE
                    val tvNotifType = holder.ll_notifications_view.findViewById<TextView>(R.id.tv_notification_type)
                    tvNotifType?.visibility = View.VISIBLE
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else {
                holder.ll_notifications_view.visibility = View.GONE
            }

            // Documents
            val attachments = events_do.attachments
            if (attachments != null) {
                for (a in 0 until attachments.length()) {
                    try {
                        val att_obj = attachments.getJSONObject(a)
                        val view = LayoutInflater.from(ctx).inflate(R.layout.event_details_notifications, null)
                        val attchment_list = view.findViewById<TextView>(R.id.tv_event_notifications)
                        val iv_event_docView = view.findViewById<ImageView>(R.id.iv_event_docView)
                        val fl_avatar = view.findViewById<FrameLayout>(R.id.fl_avatar)

                        fl_avatar?.visibility = View.GONE
                        view.setBackgroundResource(R.drawable.rectangle_light_grey_bg)
                        iv_event_docView.visibility = View.VISIBLE

                        val document = DocumentsModel()
                        document.name = att_obj.optString("name")
                        document.docid = att_obj.optString("docid")
                        document.doctype = att_obj.optString("doctype")
                        document.is_encrypted = att_obj.optBoolean("is_encrypted")
                        document.is_password = att_obj.optBoolean("is_password")
                        document.added_encryption = att_obj.optBoolean("added_encryption")
                        document.contentType = att_obj.optString("content_type")

                        attchment_list.text = document.name
                        documentsList.add(document)
                        val pos = documentsList.size - 1

                        iv_event_docView.setOnClickListener {
                            Log.d("Clicked Position", "Position: $pos")
                            documentsModel = documentsList[pos]
                            View_doc(documentsModel.docid ?: "")
                        }
                        holder.ll_documents.addView(view)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // Organiser
            if (!events_do.owner_name.isNullOrEmpty()) {
                val view1 = LayoutInflater.from(ctx).inflate(R.layout.event_details_notifications, null)
                val owner_name = view1.findViewById<TextView>(R.id.tv_event_notifications)
                val iv_rsvp_org = view1.findViewById<ImageView>(R.id.iv_rsvp)
                val fl_av = view1.findViewById<FrameLayout>(R.id.fl_avatar)

                owner_name.text = events_do.owner_name + " (Organizer)"
                owner_name.setTypeface(null, Typeface.BOLD)

                if (fl_av != null) {
                    fl_av.visibility = View.VISIBLE
                    val tvInitials = fl_av.findViewById<TextView>(R.id.tv_avatar_initials)
                    tvInitials?.text = getInitials(events_do.owner_name)
                }
                iv_rsvp_org.visibility = View.VISIBLE
                iv_rsvp_org.setImageDrawable(ctx.getDrawable(R.drawable.ic_check_green))
                holder.ll_team_members.addView(view1)
            }

            // Team Member Subheader
            val teamNameArray = events_do.team_name
            if (teamNameArray != null && teamNameArray.length() > 0) {
                val subHeader = LayoutInflater.from(ctx).inflate(R.layout.event_details_notifications, null)
                val tvSubHeader = subHeader.findViewById<TextView>(R.id.tv_event_notifications)
                val fl_sh = subHeader.findViewById<FrameLayout>(R.id.fl_avatar)
                fl_sh?.visibility = View.GONE
                tvSubHeader.text = ctx.getString(R.string.team_members)
                tvSubHeader.setTypeface(null, Typeface.BOLD)
                tvSubHeader.setTextColor(Color.BLACK)
                holder.ll_team_members.addView(subHeader)
            }

            // Team member rows
            var yesCount = 0
            var noCount = 0
            var waitingCount = 0

            if (teamNameArray != null) {
                try {
                    for (a in 0 until teamNameArray.length()) {
                        val att_team_obj = teamNameArray.getJSONObject(a)
                        val view = LayoutInflater.from(ctx).inflate(R.layout.event_details_notifications, null)
                        val team_list = view.findViewById<TextView>(R.id.tv_event_notifications)
                        val iv_rsvp = view.findViewById<ImageView>(R.id.iv_rsvp)
                        val fl_av = view.findViewById<FrameLayout>(R.id.fl_avatar)

                        val memberName = att_team_obj.getString("name")
                        team_list.text = memberName

                        if (fl_av != null) {
                            fl_av.visibility = View.VISIBLE
                            val tvInitials = fl_av.findViewById<TextView>(R.id.tv_avatar_initials)
                            tvInitials?.text = getInitials(memberName)
                        }

                        val model = InviteesInternal_Model()
                        model.id = att_team_obj.getString("id")
                        model.name = memberName
                        model.rsvp = att_team_obj.getString("rsvp")

                        applyRsvpIcon(iv_rsvp, model.rsvp)

                        when (model.rsvp?.lowercase(Locale.ROOT)) {
                            "yes" -> yesCount++
                            "no" -> noCount++
                            else -> waitingCount++
                        }

                        holder.ll_team_members.addView(view)
                        Log.d("Team Members", att_team_obj.getString("name"))
                    }
                } catch (e: JSONException) {
                    e.fillInStackTrace()
                }
            }

            // Consumer external
            val consumerExt = events_do.consumer_external
            if (consumerExt != null) {
                for (a in 0 until consumerExt.length()) {
                    try {
                        Log.d("Clients", consumerExt.toString())
                        val att_team_obj = consumerExt.getJSONObject(a)
                        val view = LayoutInflater.from(ctx).inflate(R.layout.event_details_notifications, null)
                        val team_list = view.findViewById<TextView>(R.id.tv_event_notifications)
                        val iv_rsvp = view.findViewById<ImageView>(R.id.iv_rsvp)
                        val fl_av = view.findViewById<FrameLayout>(R.id.fl_avatar)

                        val name = att_team_obj.optString("tmName")
                        team_list.text = name

                        if (fl_av != null) {
                            fl_av.visibility = View.VISIBLE
                            val tvInitials = fl_av.findViewById<TextView>(R.id.tv_avatar_initials)
                            tvInitials?.text = getInitials(name)
                        }

                        val inviteesInternalModel = InviteesInternal_Model()
                        inviteesInternalModel.id = att_team_obj.optString("tmId")
                        inviteesInternalModel.name = name
                        inviteesInternalModel.rsvp = att_team_obj.optString("rsvp")

                        when (inviteesInternalModel.rsvp?.lowercase(Locale.ROOT)) {
                            "yes" -> yesCount++
                            "no" -> noCount++
                            else -> waitingCount++
                        }

                        applyRsvpIcon(iv_rsvp, inviteesInternalModel.rsvp)
                        holder.ll_clients.addView(view)
                    } catch (e: Exception) {
                        e.fillInStackTrace()
                    }
                }
            }

            // Entity maps
            val entityMap = HashMap<String, MutableList<InviteesInternal_Model>>()
            val entitycorpMap = HashMap<String, MutableList<InviteesInternal_Model>>()
            inviteesInternalArrayList.clear()

            populateEntityMap(events_do.tm_name, entityMap)
            populateEntityMap(events_do.corporate, entitycorpMap)

            for (groupList in entityMap.values) {
                for (m in groupList) {
                    when (m.rsvp?.lowercase(Locale.ROOT)) {
                        "yes" -> yesCount++
                        "no" -> noCount++
                        else -> waitingCount++
                    }
                }
            }

            for (groupList in entitycorpMap.values) {
                for (m in groupList) {
                    when (m.rsvp?.lowercase(Locale.ROOT)) {
                        "yes" -> yesCount++
                        "no" -> noCount++
                        else -> waitingCount++
                    }
                }
            }

            if (entityMap.isNotEmpty()) {
                load_Clients_view(entityMap, holder.ll_clients)
            }
            if (entitycorpMap.isNotEmpty()) {
                load_Clients_view(entitycorpMap, holder.ll_corp_clients)
            }

            updateRsvpSummary(yesCount, noCount, waitingCount)

            val clientSize = events_do.tm_name?.length() ?: 0
            val individualSize = events_do.consumer_external?.length() ?: 0
            val corpSize = events_do.corporate?.length() ?: 0
            val totClientSize = clientSize + individualSize
            val teamSize = events_do.team_name?.length() ?: 0
            val totalAttendees = teamSize + individualSize + clientSize + corpSize

            if (totClientSize > 0) {
                holder.clients.text = "$totClientSize " + ctx.getString(R.string.clients)
                holder.ll_clients_view.visibility = View.VISIBLE
            } else {
                holder.clients.setText(R.string.clients)
                holder.ll_clients_view.visibility = View.GONE
            }

            if (events_do.corporate != null && events_do.corporate!!.length() > 0) {
                holder.corp_clients.text = "${events_do.corporate!!.length()} " + ctx.getString(R.string.corporate_clients)
                holder.ll_corp_clients_view.visibility = View.VISIBLE
            } else {
                holder.corp_clients.setText(R.string.corporate_clients)
                holder.ll_corp_clients_view.visibility = View.GONE
            }

            if (totalAttendees > 0) {
                holder.team_members.text = "$totalAttendees " + ctx.getString(R.string.team_members)
                holder.ll_team_members_view.visibility = View.VISIBLE
            } else if (holder.ll_team_members.childCount > 0) {
                holder.team_members.text = "${holder.ll_team_members.childCount} " + ctx.getString(R.string.team_members)
                holder.ll_team_members_view.visibility = View.VISIBLE
            } else {
                holder.team_members.setText(R.string.team_members)
                holder.ll_team_members_view.visibility = View.GONE
            }

            if (holder.ll_documents.childCount > 0) {
                holder.documents.text = "${events_do.attachments!!.length()} " + ctx.getString(R.string.documents)
                holder.ll_documents_view.visibility = View.VISIBLE
            } else {
                holder.documents.setText(R.string.documents)
                holder.ll_documents_view.visibility = View.GONE
            }

            if (events_do.notifications != null && events_do.notifications!!.length() > 0) {
                holder.ll_notifications_view.visibility = View.VISIBLE
            } else {
                holder.ll_notifications_view.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.events_recyler_list, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        my_view_holder = holder
        val pos = holder.bindingAdapterPosition
        val calendarItem = filtered_list[pos]
        if (calendarItem.isEvent()) {
            bindEventView(holder, calendarItem.event!!, pos)
        } else if (calendarItem.isAppointment()) {
            bindAppointmentView(holder, calendarItem.appointment!!, pos)
        }
    }

    private fun bindEventView(holder: MyViewHolder, events_do: Events_Do, position: Int) {
        val ctx = holder.itemView.context
        val act = activity ?: return
        val from_ts = events_do.event_start_time ?: ""
        val event_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss")
        val amPmFormat = SimpleDateFormat("a", Locale.US)
        val amPm_from_ts = amPmFormat.format(event_date)
        val converted_date = AndroidUtils.getDateToString(event_date, "dd MMMM yyyy")
        var converted_time = AndroidUtils.getDateToString(event_date, "hh:mm")

        val to_ts = events_do.event_end_time ?: ""
        val end_date = AndroidUtils.stringToDateTimeDefault(to_ts, "yyyy-MM-dd'T'HH:mm:ss")
        var converted_end_time = AndroidUtils.getDateToString(end_date, "hh:mm")
        val amPmFormat_to_ts = SimpleDateFormat("a", Locale.US)
        val amPm_to_ts = amPmFormat_to_ts.format(end_date)

        if (converted_time == "00:00") converted_time = "12:00"
        if (converted_end_time == "00:00") converted_end_time = "12:00"

        holder.events_names.text = converted_date
        holder.statusView.setBackgroundDrawable(ctx.getDrawable(R.color.light_blue))
        holder.rl_actions.visibility = View.GONE

        // 3-dot action menu
        val itemActions = ArrayList<ActionModel>()
        itemActions.add(ActionModel("Edit"))
        itemActions.add(ActionModel("Delete"))
        val isExpanded = (position == expandedPosition)

        if (events_do.isOwner) {
            holder.custom_spinner_cardview.visibility = View.VISIBLE
            holder.custom_spinner_cardview.setOnClickListener(null)
            holder.sp_action.onItemClickListener = null

            if (isExpanded) {
                val itemAdapter = CommonSpinnerAdapter(act, itemActions)
                holder.sp_action.adapter = itemAdapter
                holder.sp_action.post { AndroidUtils.setDynamicHeight(holder.sp_action) }
                holder.action_list_card.visibility = View.VISIBLE
                holder.sp_action.visibility = View.VISIBLE
            } else {
                holder.sp_action.adapter = null
                holder.action_list_card.visibility = View.GONE
                holder.sp_action.visibility = View.GONE
            }

            holder.custom_spinner_cardview.setOnClickListener {
                val cur = holder.bindingAdapterPosition
                if (cur == RecyclerView.NO_POSITION) return@setOnClickListener
                val prev = expandedPosition
                expandedPosition = if (expandedPosition == cur) -1 else cur
                if (prev != -1 && prev != cur) safeNotify(prev)
                safeNotify(cur)
            }

            holder.sp_action.onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
                val cur = holder.bindingAdapterPosition
                if (cur == RecyclerView.NO_POSITION) return@OnItemClickListener
                val actionName = itemActions[pos].name
                expandedPosition = -1
                safeNotify(cur)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    dialog1?.dismiss()
                    if (!Constants.is_active) {
                        AndroidUtils.showRenewalPopup(act)
                    } else {
                        if (actionName == "Edit") {
                            FLAG = "VIEW"
                            callEventDetailsWebservice(events_do.event_id!!)
                            isRecurring = events_do.isRecurring
                        } else if (actionName == "Delete") {
                            context?.delete_events(events_do)
                        }
                    }
                }
            }

            holder.ib_view_events.visibility = View.GONE
            holder.ib_delete_events.visibility = View.GONE
            holder.ll_rsvp.visibility = View.GONE
        } else {
            holder.custom_spinner_cardview.visibility = View.GONE
            holder.action_list_card.visibility = View.GONE
            holder.ib_view_events.visibility = View.GONE
            holder.ib_delete_events.visibility = View.GONE
            holder.ll_rsvp.visibility = View.VISIBLE
            holder.tv_maybe.visibility = View.VISIBLE
        }

        holder.event_title.text = events_do.event_Name

        val title = events_do.event_Name ?: ""
        val existing_task = if (title.contains("-")) title.split(" - ")[0] else title
        val second_str = if (title.contains("-")) title.split(" - ")[1] else ""

        if (existing_task == "reminders") {
            holder.tv_matter_title.setText(R.string.reminders)
            holder.ll_event_details.visibility = View.GONE
            holder.event_title.setText(R.string.reminders)
        } else {
            holder.tv_matter_title.text = existing_task
            holder.ll_event_details.visibility = View.VISIBLE
        }

        if (events_do.isAll_day) {
            holder.event_time.setText(R.string.all_day)
        } else {
            holder.event_time.text = "$converted_time$amPm_from_ts - $converted_end_time$amPm_to_ts"
        }

        holder.ll_repetation.visibility = View.VISIBLE
        if (events_do.repeat_interval.isNullOrEmpty()) {
            holder.event_repetation.text = "None"
        } else {
            holder.event_repetation.text = AndroidUtils.CapitalizeFirstLetter(events_do.repeat_interval)
        }

        holder.event_timezone.text = events_do.timezone_location

        // Join Meeting Link
        val meetingLink = events_do.meeting_link
        if (!meetingLink.isNullOrEmpty()) {
            val finalMeetingLink = meetingLink.trim()
            holder.v_btn?.visibility = View.VISIBLE
            holder.tv_meeting_link.visibility = View.VISIBLE
            holder.tv_meeting_link.text = "Join Meeting"
            holder.tv_meeting_link.setTextColor(ctx.getColor(R.color.light_blue))
            holder.tv_meeting_link.paintFlags = holder.tv_meeting_link.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            holder.tv_meeting_link.setOnClickListener { v ->
                try {
                    val url = if (finalMeetingLink.startsWith("http://") || finalMeetingLink.startsWith("https://")) {
                        finalMeetingLink
                    } else {
                        "https://$finalMeetingLink"
                    }
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.setPackage("com.android.chrome")
                    try {
                        v.context.startActivity(intent)
                    } catch (ex: Exception) {
                        intent.setPackage(null)
                        v.context.startActivity(intent)
                    }
                } catch (e: Exception) {
                    Log.e("MeetingLink", "Failed to open: $finalMeetingLink", e)
                }
            }
        } else {
            holder.v_btn?.visibility = View.GONE
            holder.tv_meeting_link.visibility = View.GONE
            holder.tv_meeting_link.text = ""
            holder.tv_meeting_link.setOnClickListener(null)
        }

        // Dial-in
        val dialinValue = events_do.dialin
        if (!dialinValue.isNullOrEmpty()) {
            holder.tv_phone_dialin.text = dialinValue
            holder.tv_phone_dialin.visibility = View.VISIBLE
            holder.dialin.visibility = View.VISIBLE
        } else {
            holder.tv_phone_dialin.visibility = View.GONE
            holder.dialin.visibility = View.GONE
        }

        // Location
        val locationValue = events_do.location
        if (!locationValue.isNullOrEmpty()) {
            holder.tv_location.text = locationValue
            holder.tv_location.visibility = View.VISIBLE
            holder.location.visibility = View.VISIBLE
        } else {
            holder.tv_location.visibility = View.GONE
            holder.location.visibility = View.GONE
        }

        // View More / Less
        val isVisible = visibleItemPositions.get(position, false)
        holder.ll_view_more.visibility = if (isVisible) View.VISIBLE else View.GONE
        holder.bt_hide_details.setText(if (isVisible) R.string.view_less else R.string.view_more)
        holder.bt_hide_details.visibility = View.VISIBLE
        holder.ib_close_events.visibility = View.GONE

        if (dialog1 != null) {
            load_more_details()
            holder.ib_close_events.visibility = View.VISIBLE
            holder.bt_hide_details.visibility = View.GONE
            holder.ll_view_more.visibility = View.VISIBLE
        } else if (isVisible) {
            load_more_details()
            holder.ll_view_more.visibility = View.VISIBLE
        }

        holder.ib_close_events.setOnClickListener {
            dialog1?.dismiss()
        }

        holder.bt_hide_details.setOnClickListener {
            if (dialog1 != null) {
                dialog1?.dismiss()
                return@setOnClickListener
            }
            val currentTime = System.currentTimeMillis()
            if (currentTime - holder.lastClickTime < 300) return@setOnClickListener
            holder.lastClickTime = currentTime
            val currentPosition = holder.bindingAdapterPosition
            val isCurrentlyVisible = visibleItemPositions.get(currentPosition, false)
            notifyItemChanged(currentPosition)
            visibleItemPositions.put(currentPosition, !isCurrentlyVisible)
            if (!isCurrentlyVisible) {
                FLAG = "MORE"
                callEventDetailsWebservice(events_do.event_id!!)
            } else {
                FLAG = ""
            }
            Log.d("Events_Adapter", "Click registered for position: $currentPosition, Current State: $isCurrentlyVisible")
        }

        // RSVP buttons
        if (!events_do.isOwner) {
            holder.tv_yes.setOnClickListener {
                events_do.userRsvp = "Yes"
                call_choosen_rsvp(events_do.event_id!!, "Yes")
                load_Yes(holder)
            }
            holder.tv_no.setOnClickListener {
                events_do.userRsvp = "No"
                call_choosen_rsvp(events_do.event_id!!, "No")
                load_No(holder)
            }
            holder.tv_maybe.setOnClickListener {
                events_do.userRsvp = "Maybe"
                call_choosen_rsvp(events_do.event_id!!, "Maybe")
                load_Maybe(holder)
            }
        }

        setLoggedInUserRsvp(events_do, holder)
    }

    fun setFilter(filterType: Meetings.FilterType) {
        this.currentFilter = filterType
        applyFilter()
    }

    private fun applyFilter() {
        filtered_list.clear()
        for (item in list_item) {
            if (currentFilter == Meetings.FilterType.ALL) {
                filtered_list.add(item)
            } else if (currentFilter == Meetings.FilterType.MY_MEETINGS && item.isEvent()) {
                filtered_list.add(item)
            } else if (currentFilter == Meetings.FilterType.APPOINTMENTS && item.isAppointment()) {
                filtered_list.add(item)
            }
        }
        notifyDataSetChanged()
    }

    private fun bindAppointmentView(holder: MyViewHolder, appointment_do: AppointmentModel, position: Int) {
        val ctx = holder.itemView.context
        val act = activity ?: return
        val from_ts = appointment_do.appointment_from
        val appointment_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss")
        val amPmFormat = SimpleDateFormat("a", Locale.US)
        val amPm_from_ts = amPmFormat.format(appointment_date)
        val converted_date = AndroidUtils.getDateToString(appointment_date, "dd MMMM yyyy")
        var converted_time = AndroidUtils.getDateToString(appointment_date, "hh:mm")

        val to_ts = appointment_do.appointment_to
        val end_date = AndroidUtils.stringToDateTimeDefault(to_ts, "yyyy-MM-dd'T'HH:mm:ss")
        var converted_end_time = AndroidUtils.getDateToString(end_date, "hh:mm")
        val amPmFormat_to_ts = SimpleDateFormat("a", Locale.US)
        val amPm_to_ts = amPmFormat_to_ts.format(end_date)

        if (converted_time == "00:00") converted_time = "12:00"
        if (converted_end_time == "00:00") converted_end_time = "12:00"

        holder.events_names.text = converted_date
        holder.custom_spinner_cardview.visibility = View.GONE
        holder.action_list_card.visibility = View.GONE
        holder.ib_view_events.visibility = View.GONE
        holder.ib_delete_events.visibility = View.GONE
        holder.ll_rsvp.visibility = View.VISIBLE
        holder.tv_maybe.visibility = View.GONE
        holder.statusView.setBackgroundDrawable(ctx.getDrawable(R.color.yellow_clear))
        holder.event_title.text = appointment_do.client_name
        holder.event_time.text = "$converted_time$amPm_from_ts - $converted_end_time$amPm_to_ts"
        holder.ll_repetation.visibility = View.GONE
        holder.event_timezone.text = "Asia / Kolkata"
        holder.rl_actions.visibility = View.VISIBLE

        val appointmentStatus = appointment_do.appointment_status?.lowercase(Locale.ROOT) ?: ""

        if ("completed" == appointmentStatus) {
            holder.tv_appointment_status.text = "Completed"
            holder.tv_appointment_status.setBackgroundResource(R.drawable.completed_badge)
            holder.tv_appointment_status.setTextColor(ctx.resources.getColor(R.color.completed_text))
            holder.iv_chat.visibility = View.VISIBLE
            holder.iv_video_call.visibility = View.GONE
            holder.ll_rsvp.visibility = View.VISIBLE
        } else if ("cancelled" == appointmentStatus || "canceled" == appointmentStatus) {
            holder.tv_appointment_status.text = "Cancelled"
            holder.tv_appointment_status.setBackgroundResource(R.drawable.cancelled_badge)
            holder.tv_appointment_status.setTextColor(ctx.resources.getColor(R.color.cancelled_text))
            holder.iv_chat.visibility = View.GONE
            holder.iv_video_call.visibility = View.GONE
            holder.ll_rsvp.visibility = View.GONE
        } else if ("upcoming" == appointmentStatus || "ongoing" == appointmentStatus) {
            holder.tv_appointment_status.text = AndroidUtils.CapitalizeFirstLetter(appointmentStatus)
            holder.tv_appointment_status.setBackgroundResource(R.drawable.scheduled_badge)
            holder.tv_appointment_status.setTextColor(ctx.resources.getColor(R.color.scheduled_text))
            holder.iv_chat.visibility = View.VISIBLE
            holder.iv_video_call.visibility = View.VISIBLE
            holder.ll_rsvp.visibility = View.VISIBLE
        } else if ("payment_pending" == appointmentStatus) {
            holder.tv_appointment_status.text = "Payment Pending"
            holder.tv_appointment_status.setBackgroundResource(R.drawable.pending_badge)
            holder.tv_appointment_status.setTextColor(ctx.resources.getColor(R.color.pending_text))
            holder.iv_chat.visibility = View.GONE
            holder.iv_video_call.visibility = View.GONE
            holder.ll_rsvp.visibility = View.VISIBLE
        } else {
            holder.tv_appointment_status.text = "Scheduled"
            holder.tv_appointment_status.setBackgroundResource(R.drawable.scheduled_badge)
            holder.tv_appointment_status.setTextColor(ctx.resources.getColor(R.color.scheduled_text))
            holder.iv_chat.visibility = View.GONE
            holder.iv_video_call.visibility = View.GONE
            holder.ll_rsvp.visibility = View.VISIBLE
        }

        if (AndroidUtils.isWithinOneHour(appointment_do.appointment_from) || appointmentStatus == "completed") {
            AndroidUtils.ToggleButton(1, holder.iv_chat)
        } else {
            AndroidUtils.ToggleButton(0, holder.iv_chat)
        }

        if (AndroidUtils.isWithinOneMinute(appointment_do.appointment_from)) {
            AndroidUtils.ToggleButton(1, holder.iv_video_call)
        } else {
            AndroidUtils.ToggleButton(0, holder.iv_video_call)
        }

        holder.iv_video_call.setOnClickListener {
            val parts = AndroidUtils.extractDateTimeParts(appointment_do.appointment_from, appointment_do.appointment_to)
            if (parts != null) {
                val url = AndroidUtils.getAVChatUrl(appointment_do.meeting_room_id, parts[1], parts[2], parts[0], appointment_do.client_name)
                AndroidUtils.loadAVChatView(ctx, act, url)
                Log.d("AVCHAT_URL", url)
            }
        }

        holder.iv_chat.setOnClickListener {
            Constants.isClient_chat = true
            Constants.pendingChatJid = appointment_do.guid
            Constants.pendingChatName = appointment_do.client_name
            Constants.pendingChatSource = "appointment"
            Constants.mainActivity?.navigation_items(Chat())
        }

        val meetingLink = appointment_do.meeting_room_id
        if (!meetingLink.isNullOrEmpty()) {
            val finalMeetingLink = meetingLink.trim()
            holder.v_btn?.visibility = View.VISIBLE
            holder.tv_meeting_link.visibility = View.VISIBLE
            holder.tv_meeting_link.text = "Join Meeting"
            holder.tv_meeting_link.setTextColor(ctx.getColor(R.color.light_blue))
            holder.tv_meeting_link.paintFlags = holder.tv_meeting_link.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            holder.tv_meeting_link.setOnClickListener { v ->
                try {
                    val url = if (finalMeetingLink.startsWith("http://") || finalMeetingLink.startsWith("https://")) {
                        finalMeetingLink
                    } else {
                        "https://$finalMeetingLink"
                    }
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.setPackage("com.android.chrome")
                    try {
                        v.context.startActivity(intent)
                    } catch (ex: Exception) {
                        intent.setPackage(null)
                        v.context.startActivity(intent)
                    }
                } catch (e: Exception) {
                    Log.e("MeetingLink", "Failed to open: $finalMeetingLink", e)
                }
            }
        } else {
            holder.v_btn?.visibility = View.GONE
            holder.tv_meeting_link.visibility = View.GONE
            holder.tv_meeting_link.text = ""
            holder.tv_meeting_link.setOnClickListener(null)
        }

        holder.tv_phone_dialin.visibility = View.GONE
        holder.tv_location.visibility = View.GONE
        holder.dialin.visibility = View.GONE
        holder.location.visibility = View.GONE

        val payment = appointment_do.payment
        if (payment != null) {
            val paymentInfo = "${payment.status} - ${payment.symbol}${payment.amount_paid} ${payment.currency}"
            holder.event_description.text = paymentInfo
            holder.event_description.visibility = View.VISIBLE
            holder.tv_event_description.text = "Payment Status"
            holder.tv_event_description.visibility = View.VISIBLE
        } else {
            holder.event_description.visibility = View.GONE
            holder.tv_event_description.visibility = View.GONE
        }

        holder.bt_hide_details.visibility = View.GONE
        holder.ll_view_more.visibility = View.GONE

        holder.tv_yes.setOnClickListener {
            call_appointment_rsvp(appointment_do.id, "Yes")
            load_AppointmentYes(holder)
        }
        holder.tv_no.setOnClickListener {
            AndroidUtils.showConfirmationDialog(ctx, "Confirmation", "Are you sure you want to cancel this appointment? This action cannot be undone.", object : AndroidUtils.OnConfirmListener {
                override fun onSave() {
                    callCancelAppointments(appointment_do)
                    load_AppointmentNo(holder)
                }
                override fun onCancel() {}
            })
        }

        loadAppointmentRsvpView(appointment_do, holder)
    }

    private fun call_appointment_rsvp(appointmentId: String, rsvpValue: String) {
        val act = activity ?: return
        progress_dialog = AndroidUtils.get_progress(act)
        try {
            val postdata = JSONObject()
            postdata.put("rsvp_status", rsvpValue)
            WebServiceHelper.callHttpWebService(
                this, act, WebServiceHelper.RestMethodType.PATCH,
                "v3/appointments/$appointmentId/rsvp", "Appointment_rsvp", postdata.toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun callCancelAppointments(appointmentModel: AppointmentModel) {
        val act = activity ?: return
        try {
            progress_dialog = AndroidUtils.get_progress(act)
            val postData = JSONObject()
            WebServiceHelper.callHttpWebService(
                this, act, WebServiceHelper.RestMethodType.DELETE,
                "v3/appointments/${appointmentModel.id}/cancel", "Cancel_Appointments", postData.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            e.fillInStackTrace()
        }
    }

    private fun loadAppointmentRsvpView(appointment_do: AppointmentModel, holder: MyViewHolder) {
        val rsvp = appointment_do.rsvp_status?.lowercase(Locale.ROOT) ?: ""
        if (rsvp == "accepted") {
            load_AppointmentYes(holder)
        } else if (rsvp == "declined") {
            load_AppointmentNo(holder)
        } else {
            load_AppointmentNothing(holder)
        }
    }

    private fun load_Nothing(holder: MyViewHolder) {
        val ctx = holder.itemView.context
        holder.tv_yes.setTextColor(Color.BLACK)
        holder.tv_no.setTextColor(Color.BLACK)
        holder.tv_maybe.setTextColor(Color.BLACK)
        holder.tv_yes.setBackgroundDrawable(ctx.getDrawable(R.drawable.button_left_round_background))
        holder.tv_no.setBackgroundDrawable(ctx.getDrawable(R.drawable.radiobutton_centre_background))
        holder.tv_maybe.setBackgroundDrawable(ctx.getDrawable(R.drawable.button_right_round_background))
    }

    private fun load_Yes(holder: MyViewHolder) {
        val ctx = holder.itemView.context
        holder.tv_yes.setTextColor(Color.WHITE)
        holder.tv_no.setTextColor(Color.BLACK)
        holder.tv_maybe.setTextColor(Color.BLACK)
        holder.tv_yes.setBackgroundDrawable(ctx.getDrawable(R.drawable.button_left_green_round_background))
        holder.tv_no.setBackgroundDrawable(ctx.getDrawable(R.drawable.radiobutton_centre_background))
        holder.tv_maybe.setBackgroundDrawable(ctx.getDrawable(R.drawable.button_right_round_background))
    }

    private fun load_Maybe(holder: MyViewHolder) {
        val ctx = holder.itemView.context
        holder.tv_yes.setTextColor(Color.BLACK)
        holder.tv_no.setTextColor(Color.BLACK)
        holder.tv_maybe.setTextColor(Color.WHITE)
        holder.tv_yes.setBackgroundDrawable(ctx.getDrawable(R.drawable.button_left_round_background))
        holder.tv_no.setBackgroundDrawable(ctx.getDrawable(R.drawable.radiobutton_centre_background))
        holder.tv_maybe.setBackgroundDrawable(ctx.getDrawable(R.drawable.button_right_green_round_background))
    }

    private fun load_No(holder: MyViewHolder) {
        val ctx = holder.itemView.context
        holder.tv_yes.setTextColor(Color.BLACK)
        holder.tv_no.setTextColor(Color.WHITE)
        holder.tv_maybe.setTextColor(Color.BLACK)
        holder.tv_yes.setBackgroundDrawable(ctx.getDrawable(R.drawable.button_left_round_background))
        holder.tv_no.setBackgroundDrawable(ctx.getDrawable(R.drawable.radiobutton_centre_green_background))
        holder.tv_maybe.setBackgroundDrawable(ctx.getDrawable(R.drawable.button_right_round_background))
    }

    private fun load_AppointmentNothing(holder: MyViewHolder) {
        val ctx = holder.itemView.context
        holder.tv_yes.setTextColor(Color.BLACK)
        holder.tv_no.setTextColor(Color.BLACK)
        holder.tv_yes.setBackgroundDrawable(ctx.getDrawable(R.drawable.button_left_round_background))
        holder.tv_no.setBackgroundDrawable(ctx.getDrawable(R.drawable.button_right_round_background))
    }

    private fun load_AppointmentYes(holder: MyViewHolder) {
        val ctx = holder.itemView.context
        holder.tv_yes.setTextColor(Color.WHITE)
        holder.tv_no.setTextColor(Color.BLACK)
        holder.tv_yes.setBackgroundDrawable(ctx.getDrawable(R.drawable.button_left_green_round_background))
        holder.tv_no.setBackgroundDrawable(ctx.getDrawable(R.drawable.button_right_round_background))
    }

    private fun load_AppointmentNo(holder: MyViewHolder) {
        val ctx = holder.itemView.context
        holder.tv_yes.setTextColor(Color.BLACK)
        holder.tv_no.setTextColor(Color.WHITE)
        holder.tv_yes.setBackgroundDrawable(ctx.getDrawable(R.drawable.button_left_round_background))
        holder.tv_no.setBackgroundDrawable(ctx.getDrawable(R.drawable.button_right_green_round_background))
    }

    private fun loadRsvpView(events_do: Events_Do, holder: MyViewHolder) {
        when (events_do.userRsvp.lowercase(Locale.ROOT)) {
            "yes" -> load_Yes(holder)
            "maybe" -> load_Maybe(holder)
            "no" -> load_No(holder)
            else -> load_Nothing(holder)
        }
    }

    private fun load_Clients_view(entityMap: Map<String, List<InviteesInternal_Model>>, clients: LinearLayout) {
        val ctx = mcontext ?: return
        for ((entityName, tmList) in entityMap) {
            val headerView = LayoutInflater.from(ctx).inflate(R.layout.event_details_notifications, null)
            val tvHeader = headerView.findViewById<TextView>(R.id.tv_event_notifications)
            val fl_h = headerView.findViewById<FrameLayout>(R.id.fl_avatar)
            fl_h?.visibility = View.GONE
            tvHeader.text = entityName
            tvHeader.setTypeface(null, Typeface.BOLD)
            clients.addView(headerView)

            for (model in tmList) {
                val view = LayoutInflater.from(ctx).inflate(R.layout.event_details_notifications, null)
                val tvName = view.findViewById<TextView>(R.id.tv_event_notifications)
                val iv_rsvp = view.findViewById<ImageView>(R.id.iv_rsvp)
                val fl_av = view.findViewById<FrameLayout>(R.id.fl_avatar)

                tvName.text = model.name

                if (fl_av != null) {
                    fl_av.visibility = View.VISIBLE
                    val tvInitials = fl_av.findViewById<TextView>(R.id.tv_avatar_initials)
                    tvInitials?.text = getInitials(model.name)
                }

                applyRsvpIcon(iv_rsvp, model.rsvp)
                clients.addView(view)
            }
        }
    }

    private fun call_choosen_rsvp(id: String, rsvpValue: String) {
        val act = activity ?: return
        progress_dialog = AndroidUtils.get_progress(act)
        try {
            val postdata = JSONObject()
            postdata.put("rsvp_response", rsvpValue)
            WebServiceHelper.callHttpWebService(
                this, act, WebServiceHelper.RestMethodType.PUT,
                "v3/event/response/$id", "Event_rsvp", postdata.toString()
            )
            Log.d("Event_rsvp", postdata.toString())
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun callEventDetailsWebservice(id: String) {
        val act = activity ?: return
        progress_dialog = AndroidUtils.get_progress(act)
        val postData = JSONObject()
        val calendar = GregorianCalendar()
        val timeZone = calendar.timeZone
        val offset = timeZone.rawOffset
        val hours = TimeUnit.MILLISECONDS.toMinutes(offset.toLong())
        val timezoneoffset = -1 * hours
        event_id = id
        WebServiceHelper.callHttpWebService(
            this, act, WebServiceHelper.RestMethodType.GET,
            "v3/event/$event_id/$timezoneoffset", "EVENT DETAILS", postData.toString()
        )
    }

    private fun populateEntityMap(jsonArray: JSONArray?, entityMap: HashMap<String, MutableList<InviteesInternal_Model>>) {
        if (jsonArray == null) return
        try {
            for (c in 0 until jsonArray.length()) {
                val attClientObj = jsonArray.getJSONObject(c)
                val entityName = attClientObj.getString("entityName")
                val tmName = attClientObj.getString("tmName")
                val rsvp = attClientObj.optString("rsvp", "")

                val model = InviteesInternal_Model()
                model.id = attClientObj.optString("tmId")
                model.name = tmName
                model.rsvp = rsvp

                inviteesInternalArrayList.add(model)

                if (!entityMap.containsKey(entityName)) {
                    entityMap[entityName] = ArrayList()
                }
                entityMap[entityName]!!.add(model)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e("EntityMapError", "Error while populating entity map: " + e.message)
        }
    }

    private fun setLoggedInUserRsvp(eventDetails: Events_Do, holder: MyViewHolder) {
        try {
            var rsvpVal = ""

            val inviteesInt = eventDetails.invitees_internal
            if (inviteesInt != null) {
                for (i in 0 until inviteesInt.length()) {
                    val obj = inviteesInt.getJSONObject(i)
                    val name = obj.optString("tmName", obj.optString("name"))
                    if (Constants.NAME == name) {
                        rsvpVal = obj.optString("rsvp", "")
                        break
                    }
                }
            }
            if (rsvpVal.isEmpty()) {
                val inviteesExt = eventDetails.invitees_external
                if (inviteesExt != null) {
                    for (i in 0 until inviteesExt.length()) {
                        val obj = inviteesExt.getJSONObject(i)
                        if (Constants.NAME == obj.optString("tmName")) {
                            rsvpVal = obj.optString("rsvp", "")
                            break
                        }
                    }
                }
            }
            if (rsvpVal.isEmpty() && eventDetails.invitees_consumer_external != null) {
                val inviteesCons = eventDetails.invitees_consumer_external
                if (inviteesCons != null) {
                    for (i in 0 until inviteesCons.length()) {
                        val obj = inviteesCons.getJSONObject(i)
                        if (Constants.NAME == obj.optString("tmName")) {
                            rsvpVal = obj.optString("rsvp", "")
                            break
                        }
                    }
                }
            }
            eventDetails.userRsvp = rsvpVal
            loadRsvpView(eventDetails, holder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return filtered_list.size
    }

    private fun applyRsvpIcon(iv_rsvp: ImageView, rsvp: String?) {
        val ctx = mcontext ?: return
        if (rsvp.isNullOrEmpty()) {
            iv_rsvp.visibility = View.VISIBLE
            iv_rsvp.setImageDrawable(ctx.getDrawable(R.drawable.ic_info_grey))
            return
        }
        iv_rsvp.visibility = View.VISIBLE
        when (rsvp.lowercase(Locale.ROOT)) {
            "yes" -> iv_rsvp.setImageDrawable(ctx.getDrawable(R.drawable.ic_check_green))
            "no" -> iv_rsvp.setImageDrawable(ctx.getDrawable(R.drawable.icon_cancel))
            "maybe" -> iv_rsvp.setImageDrawable(ctx.getDrawable(R.drawable.timermaybe))
            else -> iv_rsvp.setImageDrawable(ctx.getDrawable(R.drawable.ic_info_grey))
        }
    }

    private fun getInitials(name: String?): String {
        if (name.isNullOrEmpty()) return "?"
        val parts = name.trim().split("\\s+".toRegex()).toTypedArray()
        if (parts.size == 1) {
            return parts[0].substring(0, Math.min(1, parts[0].length)).uppercase(Locale.US)
        }
        return (parts[0][0].toString() + parts[1][0]).uppercase(Locale.US)
    }

    private fun updateRsvpSummary(yes: Int, no: Int, waiting: Int) {
        val holder = my_view_holder ?: return
        val summaryRow = holder.itemView.findViewById<LinearLayout>(R.id.ll_rsvp_summary) ?: return

        val tvYes = summaryRow.findViewById<TextView>(R.id.tv_rsvp_yes_count)
        val tvNo = summaryRow.findViewById<TextView>(R.id.tv_rsvp_no_count)
        val tvWaiting = summaryRow.findViewById<TextView>(R.id.tv_rsvp_waiting_count)

        if (tvYes != null) {
            if (yes > 0) {
                tvYes.text = "$yes Yes"
                tvYes.visibility = View.VISIBLE
            } else {
                tvYes.visibility = View.GONE
            }
        }

        if (tvNo != null) {
            if (no > 0) {
                tvNo.text = "$no No"
                tvNo.visibility = View.VISIBLE
            } else {
                tvNo.visibility = View.GONE
            }
        }

        if (tvWaiting != null) {
            if (waiting > 0) {
                tvWaiting.text = "$waiting Waiting"
                tvWaiting.visibility = View.VISIBLE
            } else {
                tvWaiting.visibility = View.GONE
            }
        }

        summaryRow.visibility = if (yes > 0 || no > 0 || waiting > 0) View.VISIBLE else View.GONE
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @JvmField val events_names: TextView = itemView.findViewById(R.id.events_names)
        @JvmField val tv_meeting_link: TextView = itemView.findViewById(R.id.tv_meeting_link)
        @JvmField val tv_phone_dialin: TextView = itemView.findViewById(R.id.tv_phone_dialin)
        @JvmField val tv_location: TextView = itemView.findViewById(R.id.tv_location)
        @JvmField val tv_yes: TextView = itemView.findViewById(R.id.tv_yes)
        @JvmField val tv_no: TextView = itemView.findViewById(R.id.tv_no)
        @JvmField val tv_maybe: TextView = itemView.findViewById(R.id.tv_maybe)
        @JvmField val event_title: TextView = itemView.findViewById(R.id.event_title)
        @JvmField val event_time: TextView = itemView.findViewById(R.id.event_time)
        @JvmField val event_description: TextView = itemView.findViewById(R.id.event_description)
        @JvmField val tv_event_description: TextView = itemView.findViewById(R.id.tv_event_description)
        @JvmField val event_timezone: TextView = itemView.findViewById(R.id.event_timezone)
        @JvmField val tv_matter_title: TextView = itemView.findViewById(R.id.tv_matter_title)
        @JvmField val event_notification: TextView = itemView.findViewById(R.id.event_notification)
        @JvmField val tv_appointment_status: TextView = itemView.findViewById(R.id.tv_appointment_status)
        @JvmField val time: TextView? = itemView.findViewById(R.id.time)
        @JvmField val meeting_link: TextView = itemView.findViewById(R.id.tv_meeting_link)
        @JvmField val dialin: TextView = itemView.findViewById(R.id.dialin)
        @JvmField val location: TextView = itemView.findViewById(R.id.location)
        @JvmField val documents: TextView = itemView.findViewById(R.id.documents)
        @JvmField val team_members: TextView = itemView.findViewById(R.id.team_members)
        @JvmField val clients: TextView = itemView.findViewById(R.id.clients)
        @JvmField val corp_clients: TextView = itemView.findViewById(R.id.corp_clients)
        @JvmField val ib_delete_events: ImageView = itemView.findViewById(R.id.ib_delete_events)
        @JvmField val ib_view_events: ImageView = itemView.findViewById(R.id.ib_view_events)
        @JvmField val ib_close_events: ImageView = itemView.findViewById(R.id.ib_close_events)
        @JvmField val iv_video_call: ImageView = itemView.findViewById(R.id.iv_video_call)
        @JvmField val iv_chat: ImageView = itemView.findViewById(R.id.iv_chat)
        @JvmField val v_btn: ImageView? = itemView.findViewById(R.id.v_btn)
        @JvmField val statusView: View = itemView.findViewById(R.id.statusView)
        @JvmField val rl_actions: RelativeLayout = itemView.findViewById(R.id.rl_actions)
        @JvmField val bt_hide_details: TextView = itemView.findViewById(R.id.bt_hide_details)
        @JvmField val event_repetation: TextView = itemView.findViewById(R.id.event_repetation)
        @JvmField val ll_view_more: LinearLayout = itemView.findViewById(R.id.ll_view_more)
        @JvmField val ll_documents: LinearLayout = itemView.findViewById(R.id.ll_documents)
        @JvmField val ll_team_members: LinearLayout = itemView.findViewById(R.id.ll_team_members)
        @JvmField val ll_clients: LinearLayout = itemView.findViewById(R.id.ll_clients)
        @JvmField val ll_corp_clients: LinearLayout = itemView.findViewById(R.id.ll_corp_clients)
        @JvmField val ll_team_members_view: LinearLayout = itemView.findViewById(R.id.ll_team_members_view)
        @JvmField val ll_documents_view: LinearLayout = itemView.findViewById(R.id.ll_documents_view)
        @JvmField val ll_clients_view: LinearLayout = itemView.findViewById(R.id.ll_clients_view)
        @JvmField val ll_corp_clients_view: LinearLayout = itemView.findViewById(R.id.ll_corp_clients_view)
        @JvmField val ll_notifications_view: LinearLayout = itemView.findViewById(R.id.ll_notifications_view)
        @JvmField val ll_repetation: LinearLayout = itemView.findViewById(R.id.ll_repetation)
        @JvmField val ll_event_details: LinearLayout = itemView.findViewById(R.id.ll_event_details)
        @JvmField val custom_spinner_cardview: ImageView = itemView.findViewById(R.id.custom_spinner_cardview)
        @JvmField val action_list_card: CardView = itemView.findViewById(R.id.action_list_card)
        @JvmField val sp_action: ListView = itemView.findViewById(R.id.action_list)
        @JvmField val ll_rsvp: LinearLayout = itemView.findViewById(R.id.ll_rsvp)
        var lastClickTime: Long = 0

        init {
            val ctx = itemView.context
            if (meeting_link != null) meeting_link.setText(R.string.meeting_link)
            if (dialin != null) dialin.setText(R.string.join_by_phone)
            if (location != null) location.setText(R.string.location)
            if (documents != null) documents.setText(R.string.documents)
            if (team_members != null) team_members.setText(R.string.team_members)
            if (clients != null) clients.setText(R.string.client)
            if (event_title != null) event_title.setTextColor(Color.BLACK)
            if (tv_yes != null) tv_yes.setTextColor(Color.WHITE)
            if (tv_event_description != null) {
                tv_event_description.setText(R.string.meeting_agenda)
                tv_event_description.setTextColor(ctx.getColor(R.color.blue))
            }
            if (bt_hide_details != null) {
                bt_hide_details.setTextColor(ctx.getColor(R.color.light_blue))
                bt_hide_details.setText(R.string.view_more)
                bt_hide_details.textSize = 17f
            }
            if (ll_view_more != null) ll_view_more.visibility = View.GONE
        }
    }

    companion object {
        private var FLAG = ""
    }
}
