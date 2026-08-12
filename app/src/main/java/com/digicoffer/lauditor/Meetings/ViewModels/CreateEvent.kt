package com.digicoffer.lauditor.Meetings.ViewModels

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.graphics.Color
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.TimeZonesDO
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.Matter.ViewModels.Matter
import com.digicoffer.lauditor.Meetings.Models.*
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.google.android.material.textfield.TextInputEditText
import android.util.Log
import android.app.AlertDialog
import android.text.format.DateFormat
import org.json.JSONObject
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import android.app.DatePickerDialog
import android.widget.DatePicker
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.json.JSONArray
import org.json.JSONException
import androidx.fragment.app.FragmentTransaction
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

class CreateEvent() : Fragment(), AsyncTaskCompleteListener, View.OnClickListener {

    companion object {
        private var ADAPTER_TAG = ""
    }

    private var Event_name = ""
    private var eventId = ""
    var isFirstTime: Boolean = true
    var event_update_scope: String = "UPDATE_EVENT_ONLY"
    var iscb_timesheet_checked: Boolean = true
    private lateinit var error_msg: TextView
    private lateinit var error_msg1: TextView
    private var progressDialog: Dialog? = null
    private var existing_task: String? = null
    lateinit var chk_select_all_text: TextView
    private var existing_date: String? = null
    private var meetingRoomId = ""
    private var meetingLink = ""
    private var existing_start_time: String? = null
    private var existing_end_time: String? = null
    private var isExistingAllday: Boolean = false
    private var existing_time_zone: String? = null
    private var existing_repetetion: String? = null
    private var existing_meeting_link: String? = null
    private var existing_dialin: String? = null
    private var existing_location: String? = null
    private var existing_description: String? = null
    var is_linked_with_timesheet: Boolean = false
    private var existing_entityName: String? = null
    private var existing_entity_id: String? = null
    var recurring_edit_choice: String? = null
    private var existing_events_list: ArrayList<Event_Details_DO> = ArrayList()
    var event_id: String? = null
    var time_format: String = ""
    var time_value: String = ""
    lateinit var at_family_members: MultiAutoCompleteTextView
    lateinit var cv_meeting_details: CardView
    lateinit var cv_add_clients: CardView
    private var is_notify_clicked: Boolean = true
    private var is_timenotify: Boolean = true
    var startMinute: Int = 0
    val Repetetions: ArrayList<String> = ArrayList()
    var notification: JSONArray = JSONArray()
    lateinit var ll_client_team_members: LinearLayout
    lateinit var ll_documents_list: LinearLayout
    lateinit var ll_attach_document: LinearLayout
    lateinit var ll_selected_entites: LinearLayout
    lateinit var ll_selected_individual: LinearLayout
    lateinit var ll_individual_list: LinearLayout
    lateinit var ll_selected_team_members: LinearLayout
    lateinit var ll_selected_entity_client: LinearLayout
    lateinit var ll_selected_teammembers: LinearLayout
    lateinit var ll_add_to_timesheet: LinearLayout
    lateinit var ll_selected_corp_clients: LinearLayout
    lateinit var ll_selected_corp_clients_view: LinearLayout
    lateinit var ll_corp_client_team_members: LinearLayout
    private var selectedValues: ArrayList<String> = ArrayList()
    var selected_hour_type: String = ""
    private var timeZonesTaskCompleted: Boolean = false
    lateinit var cb_all_day: CheckBox
    lateinit var cb_add_to_timesheet: CheckBox
    var isAllDay: Boolean = false
    var isAddTimesheet: Boolean = true
    private var matterTaskCompleted: Boolean = false
    private var tmTaskCompleted: Boolean = false
    var isRecurring: Boolean = false
    val AM_PM: Array<String?> = arrayOfNulls(1)
    var offset: Int = 0
    private lateinit var btn_add_tm: Button
    private lateinit var btn_attach_document: Button
    private lateinit var btn_create_event: Button
    private lateinit var btn_add_clients: Button
    private lateinit var btn_assigned_clients: Button
    private lateinit var btn_individual: Button
    var start_time: Int = 0
    var close_time: LocalTime = LocalTime.ofSecondOfDay(0)
    var entities_list: ArrayList<RelationshipsDO> = ArrayList()
    var Corp_client_list: ArrayList<RelationshipsDO> = ArrayList()
    var Corp_Team_members_list: ArrayList<RelationshipsDO> = ArrayList()
    var individual_list: ArrayList<RelationshipsDO> = ArrayList()
    var selected_individual_list: ArrayList<RelationshipsDO> = ArrayList()
    var selected_documents_list: ArrayList<DocumentsDo> = ArrayList()
    var selected_entity_client_list: ArrayList<RelationshipsDO> = ArrayList()
    var selected_entity_corp_client_list: ArrayList<RelationshipsDO> = ArrayList()
    var new_selected_client_list: ArrayList<RelationshipsDO> = ArrayList()
    var entity_client_list: ArrayList<RelationshipsDO> = ArrayList()
    var entity_corp_client_list: ArrayList<RelationshipsDO> = ArrayList()
    var selected_tm_list: ArrayList<TeamDo> = ArrayList()
    private lateinit var at_add_tm: TextView
    private lateinit var at_attach_document: TextView
    private lateinit var tv_cb_all_day: TextView
    private lateinit var at_assigned_client: TextView
    private lateinit var at_individual: TextView
    private lateinit var at_assigned_corp_client: TextView
    private lateinit var sp_add_team_member: Spinner
    private lateinit var sp_add_entity: Spinner
    private lateinit var sp_client_team_members: Spinner
    private lateinit var tv_meeting_link: TextInputEditText
    private lateinit var tv_dialing_number: TextInputEditText
    private lateinit var tv_location: TextInputEditText
    private lateinit var tv_description: TextInputEditText
    private lateinit var tv_message: TextInputEditText
    private lateinit var add_notification: AppCompatButton
    private lateinit var btn_cancel_event: AppCompatButton
    private lateinit var btn_save_timesheet: AppCompatButton
    lateinit var tv_project_name: TextView
    lateinit var tv_no_matter: TextView
    lateinit var tv_matter_name: TextView
    lateinit var tv_task_name: TextView
    lateinit var tv_date_name: TextView
    lateinit var tv_time_zone: TextView
    lateinit var tv_repetetion: TextView
    lateinit var tv_meeting_link_name: TextView
    lateinit var tv_meeting_link_names: TextView
    lateinit var tv_attendeee: TextView
    lateinit var tv_notify: TextView
    lateinit var tv_dial_in_number: TextView
    lateinit var tv_location_name: TextView
    lateinit var tv_description_name: TextView
    lateinit var tv_message_name: TextView
    lateinit var add_tm: TextView
    lateinit var add_entities: TextView
    lateinit var tv_assigned_client: TextView
    lateinit var tv_assigned_corp_client: TextView
    lateinit var tv_individual: TextView
    lateinit var tv_selected_individual: TextView
    lateinit var tv_selected_tm: TextView
    lateinit var tv_selected_document: TextView
    lateinit var tv_name: TextView
    lateinit var tv_selected_clients: TextView
    lateinit var tv_selected_corp_clients: TextView
    lateinit var tv_sp_project: TextView
    lateinit var tv_sp_matter_name: TextView
    lateinit var tv_sp_task_name: TextView
    lateinit var tv_sp_time_zone: TextView
    lateinit var tv_sp_repetetion: TextView
    lateinit var Time_duration: TextView
    lateinit var tv_sp_entity: TextView
    lateinit var tv_attach_document: TextView
    lateinit var tv_sp_corp_clients: TextView
    lateinit var add_corp_clients: TextView
    lateinit var tv_sp_minutes: TextView
    lateinit var sp_project: ListView
    lateinit var sp_matter_name: ListView
    lateinit var sp_task: ListView
    lateinit var sp_time_zone: ListView
    lateinit var sp_repetetion: ListView
    lateinit var sp_entity: ListView
    lateinit var sp_corp_clients: ListView
    var ischecked_project: Boolean = true
    var ischecked_matter: Boolean = true
    var ischecked_task: Boolean = true
    var ischecked_time: Boolean = true
    var ischecked_repetetion: Boolean = true
    var is_notify: Boolean = true
    var is_entity: Boolean = true
    var is_corp: Boolean = true
    var is_clicked_team: Boolean = true
    var is_clicked_clients: Boolean = true
    var is_clicked_corp_clients: Boolean = true
    var is_clicked_individuals: Boolean = true
    var is_clicked_documents: Boolean = true
    lateinit var tv_event_creation_date: AppCompatButton
    lateinit var tv_event_start_time: AppCompatButton
    lateinit var tv_event_end_time: AppCompatButton
    private var selected_project: String? = null
    private var selected_task: String? = null
    private var entity_id = ""
    var corp_client_id = ""
    var corp_client_name: String? = null
    var entity_name: String? = null
    var timesPosHash: Hashtable<String, Int> = Hashtable()
    var timeZonesList: ArrayList<TimeZonesDO> = ArrayList()
    private lateinit var ll_project: LinearLayout
    private lateinit var ll_selected_documents: LinearLayout
    private lateinit var ll_matter_name: LinearLayout
    private lateinit var ll_message: LinearLayout
    private lateinit var ll_task: LinearLayout
    private lateinit var ll_repetetion: LinearLayout
    private lateinit var ll_add_notification: LinearLayout
    private lateinit var ll_add_tm: LinearLayout
    private lateinit var ll_add_entities: LinearLayout
    private lateinit var ll_individual: LinearLayout
    private lateinit var ll_documents_view: LinearLayout
    private lateinit var ll_assign_clients: LinearLayout
    private lateinit var ll_add_corp_clients: LinearLayout
    private lateinit var ll_corp_clients: LinearLayout
    private lateinit var ll_corp_tm: LinearLayout
    private lateinit var ll_entity_tm: LinearLayout
    lateinit var ll_time: LinearLayoutCompat
    lateinit var img_dropdown_sp_project: ImageView
    lateinit var img_dropdown_sp_mattername: ImageView
    lateinit var img_dropdown_sp_task: ImageView
    lateinit var img_dropdown_sp_timezone: ImageView
    lateinit var img_dropdown_sp_repetetion: ImageView
    lateinit var img_dropdown_sp_entity: ImageView
    lateinit var img_dropdown_sp_corp_clients: ImageView
    lateinit var img_clear_sp_project: ImageView
    lateinit var img_clear_sp_mattername: ImageView
    lateinit var img_clear_sp_task: ImageView
    lateinit var img_clear_sp_timezone: ImageView
    lateinit var img_clear_sp_repetetion: ImageView
    lateinit var img_clear_sp_entity: ImageView
    lateinit var img_clear_sp_corp_clients: ImageView
    lateinit var ll_sp_project: LinearLayout
    lateinit var ll_sp_mattername: LinearLayout
    lateinit var ll_sp_task: LinearLayout
    lateinit var ll_sp_timezone: LinearLayout
    lateinit var ll_sp_repetetion: LinearLayout
    lateinit var ll_sp_entity: LinearLayout
    lateinit var ll_sp_corp_clients: LinearLayout
    var projectList: ArrayList<CalendarDo> = ArrayList()
    var matterList: ArrayList<ViewMatterModel> = ArrayList()
    var documents_list: ArrayList<DocumentsDo> = ArrayList()
    var teamList: ArrayList<TeamDo> = ArrayList()
    var legalTaksList: ArrayList<TaskDo> = ArrayList()
    private var repeat_interval: String? = null
    private var event_creation_date: String? = null
    private var event_starting_date: String? = null
    private var event_end_time: String? = null
    private var timezone_location: String? = null
    private var matter_id: String? = null
    private var matter_name: String? = null
    private var matter_legal = ""
    private var meetings: Meetings? = null

    // Add these as class-level fields in CreateEvent.java:
    lateinit var spinner_adapter: CommonSpinnerAdapter<*>      // for sp_project
    lateinit var matterSpinnerAdapter: CommonSpinnerAdapter<*> // for sp_matter_name
    lateinit var taskSpinnerAdapter: CommonSpinnerAdapter<*>   // for sp_task
    lateinit var timezoneAdapter: CommonSpinnerAdapter<*>      // for sp_time_zone
    lateinit var repetitionAdapter: CommonSpinnerAdapter<*>    // for sp_repetetion
    lateinit var entityAdapter: CommonSpinnerAdapter<*>        // for sp_entity
    lateinit var corpAdapter: CommonSpinnerAdapter<*>          // for sp_corp_clients

    var minutes_list: ArrayList<MinutesDO> = ArrayList()
    lateinit var rv_groups_view: RecyclerView
    lateinit var rv_clients_view: RecyclerView
    lateinit var rv_individuals_view: RecyclerView
    lateinit var rv_documents_view: RecyclerView
    lateinit var rv_corp_client_view: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        callTimeZoneWebservice()
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("WrongViewCast")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.create_event, container, false)
        sp_project = view.findViewById(R.id.sp_project1)!!
        ll_time = view.findViewById(R.id.ll_time)!!
        chk_select_all_text = view.findViewById(R.id.tv_add_to_timesheet)!!
        chk_select_all_text.setText(R.string.add_to_timesheet)
        cb_add_to_timesheet = view.findViewById(R.id.cb_add_to_timesheet)!!
        ll_add_to_timesheet = view.findViewById(R.id.ll_add_to_timesheet)!!
        ll_add_to_timesheet.visibility = View.GONE
        sp_matter_name = view.findViewById(R.id.sp_matter_name)!!
        at_individual = view.findViewById(R.id.at_individual)!!
        at_individual.setHint(R.string.select_individuals)
        sp_task = view.findViewById(R.id.sp_task)!!
        sp_task.visibility = View.GONE
        ll_matter_name = view.findViewById(R.id.ll_matter_name)!!
        ll_matter_name.visibility = View.GONE
        at_add_tm = view.findViewById(R.id.at_add_groups)!!
        at_add_tm.setHint(R.string.select_team_members)
        add_tm = view.findViewById(R.id.add_groups)!!
        add_tm.setText(R.string.add_team_members)
        btn_create_event = view.findViewById(R.id.btn_create_event)!!
        btn_create_event?.setOnClickListener(this)
        btn_create_event.setText(R.string.save)
        ll_message = view.findViewById(R.id.ll_message)!!
        tv_message = view.findViewById(R.id.tv_message)!!
        tv_message_name = view.findViewById(R.id.tv_message_name)!!
        tv_message_name.setText(R.string.message)
        tv_message.setHint(R.string.message)
        at_attach_document = view.findViewById(R.id.at_attach_document)!!
        at_attach_document.setHint(R.string.select_documents)

        tv_cb_all_day = view.findViewById(R.id.tv_cb_all_day)!!
        tv_cb_all_day.setText(R.string.all_day)
        cb_all_day = view.findViewById(R.id.cb_all_day)!!

        btn_cancel_event = view.findViewById(R.id.btn_cancel_event)!!
        Time_duration = view.findViewById(R.id.Time_duration)!!
        Time_duration.setText(R.string.duration)
        tv_project_name = view.findViewById(R.id.tv_project_name)!!
        tv_project_name.setText(R.string.event_type)
        ll_sp_project = view.findViewById(R.id.tv_sp_project)!!
        tv_sp_project = ll_sp_project.findViewById(R.id.tv_spinner_view)!!
        tv_sp_project.setHint(R.string.select_event_type)
        img_clear_sp_project = ll_sp_project.findViewById(R.id.img_clear_icon)!!
        img_dropdown_sp_project = ll_sp_project.findViewById(R.id.img_dropdown_icon)!!
        sp_project.visibility = View.GONE

        tv_no_matter = view.findViewById(R.id.tv_no_matter)!!
        tv_no_matter.setText(R.string.no_matters_found)
        tv_no_matter.visibility = View.GONE
        tv_matter_name = view.findViewById(R.id.tv_matter_name)!!
        tv_matter_name.setText(R.string.matter_name)
        ll_sp_mattername = view.findViewById(R.id.tv_sp_matter_name)!!
        tv_sp_matter_name = ll_sp_mattername.findViewById(R.id.tv_spinner_view)!!
        tv_sp_matter_name.setHint(R.string.select_matter_name)
        tv_sp_project.setHint(R.string.select_event_type)
        img_clear_sp_mattername = ll_sp_mattername.findViewById(R.id.img_clear_icon)!!
        img_dropdown_sp_mattername = ll_sp_mattername.findViewById(R.id.img_dropdown_icon)!!
        sp_matter_name.visibility = View.GONE
        sp_matter_name.isNestedScrollingEnabled = true
        tv_task_name = view.findViewById(R.id.tv_task_name)!!
        tv_task_name.setText(R.string.task)
        ll_sp_task = view.findViewById(R.id.tv_sp_task_name)!!
        tv_sp_task_name = ll_sp_task.findViewById(R.id.tv_spinner_view)!!
        tv_sp_task_name.setHint(R.string.select_task)
        img_clear_sp_task = ll_sp_task.findViewById(R.id.img_clear_icon)!!
        img_dropdown_sp_task = ll_sp_task.findViewById(R.id.img_dropdown_icon)!!

        sp_time_zone = view.findViewById(R.id.sp_time_zone)!!
        sp_time_zone.visibility = View.GONE
        ll_sp_timezone = view.findViewById(R.id.tv_sp_time_zone)!!
        tv_sp_time_zone = ll_sp_timezone.findViewById(R.id.tv_spinner_view)!!
        tv_sp_time_zone.setHint(R.string.select_timezone)
        img_clear_sp_timezone = ll_sp_timezone.findViewById(R.id.img_clear_icon)!!
        img_dropdown_sp_timezone = ll_sp_timezone.findViewById(R.id.img_dropdown_icon)!!

        ll_sp_repetetion = view.findViewById(R.id.tv_sp_repetetion)!!
        tv_sp_repetetion = ll_sp_repetetion.findViewById(R.id.tv_spinner_view)!!
        tv_sp_repetetion.setHint(R.string.select_repetition)
        img_clear_sp_repetetion = ll_sp_repetetion.findViewById(R.id.img_clear_icon)!!
        img_dropdown_sp_repetetion = ll_sp_repetetion.findViewById(R.id.img_dropdown_icon)!!

        tv_sp_repetetion.setText(R.string.none)
        img_clear_sp_repetetion.visibility = View.VISIBLE
        img_dropdown_sp_repetetion.visibility = View.GONE
        sp_repetetion = view.findViewById(R.id.sp_repetetion)!!
        sp_repetetion.visibility = View.GONE
        tv_date_name = view.findViewById(R.id.tv_date_name)!!
        tv_date_name.setText(R.string.date)
        tv_time_zone = view.findViewById(R.id.tv_time_zone)!!
        tv_time_zone.setText(R.string.time_zone)
        tv_repetetion = view.findViewById(R.id.tv_repetetion)!!
        tv_repetetion.setText(R.string.repetition)
        tv_meeting_link_name = view.findViewById(R.id.tv_meeting_link_name)!!
        tv_meeting_link_name.setText(R.string.meeting_link)

        tv_attendeee = view.findViewById(R.id.tv_attendeee)!!
        tv_attendeee.setText("Attendees")
        tv_notify = view.findViewById(R.id.tv_notify)!!
        tv_notify.setText("Notify Me")
        tv_meeting_link_names = view.findViewById(R.id.tv_meeting_link_names)!!
        tv_meeting_link_names.setText("Meeting Details")
        tv_dial_in_number = view.findViewById(R.id.tv_dial_in_number)!!
        tv_dial_in_number.setText(R.string.dial_in_number)
        tv_location_name = view.findViewById(R.id.tv_location_name)!!
        tv_location_name.setText(R.string.location)
        tv_description_name = view.findViewById(R.id.tv_description_name)!!
        tv_description_name.setText(R.string.meeting_agenda)

        ll_sp_corp_clients = view.findViewById(R.id.ll_sp_corp_clients)!!
        tv_sp_corp_clients = ll_sp_corp_clients.findViewById(R.id.tv_spinner_view)!!
        tv_sp_corp_clients.setHint(R.string.select_corporate_client)
        img_clear_sp_corp_clients = ll_sp_corp_clients.findViewById(R.id.img_clear_icon)!!
        img_dropdown_sp_corp_clients = ll_sp_corp_clients.findViewById(R.id.img_dropdown_icon)!!
        sp_corp_clients = view.findViewById(R.id.sp_corp_clients)!!
        sp_corp_clients.visibility = View.GONE

        ll_sp_entity = view.findViewById(R.id.tv_sp_entities)!!
        tv_sp_entity = ll_sp_entity.findViewById(R.id.tv_spinner_view)!!
        tv_sp_entity.setHint(R.string.select_entity)
        img_clear_sp_entity = ll_sp_entity.findViewById(R.id.img_clear_icon)!!
        img_dropdown_sp_entity = ll_sp_entity.findViewById(R.id.img_dropdown_icon)!!

        sp_entity = view.findViewById(R.id.sp_entities)!!
        sp_entity.visibility = View.GONE
        cv_meeting_details = view.findViewById(R.id.cv_meeting_details)!!
        cv_add_clients = view.findViewById(R.id.cv_add_clients)!!
        cv_add_clients.visibility = View.GONE

        loadcheckboxData()
        ll_attach_document = view.findViewById(R.id.ll_attach_document)!!
        btn_attach_document = view.findViewById(R.id.btn_attach_document)!!
        btn_attach_document?.setOnClickListener(this)
        ll_add_notification = view.findViewById(R.id.ll_add_notification)!!

        rv_groups_view = view.findViewById(R.id.rv_groups_view)!!
        rv_groups_view?.background = context?.getDrawable(R.drawable.rectangle_light_grey_bg)
        rv_groups_view.visibility = View.GONE
        rv_corp_client_view = view.findViewById(R.id.rv_corp_clients_view)!!
        rv_corp_client_view?.background = context?.getDrawable(R.drawable.rectangle_light_grey_bg)
        rv_corp_client_view.visibility = View.GONE
        rv_clients_view = view.findViewById(R.id.rv_clients_view)!!
        rv_clients_view.visibility = View.GONE
        rv_clients_view?.background = context?.getDrawable(R.drawable.rectangle_light_grey_bg)
        rv_individuals_view = view.findViewById(R.id.rv_individuals_view)!!
        rv_individuals_view.visibility = View.GONE
        rv_individuals_view?.background = context?.getDrawable(R.drawable.rectangle_light_grey_bg)
        rv_documents_view = view.findViewById(R.id.rv_documents_view)!!
        rv_documents_view.visibility = View.GONE
        rv_documents_view?.background = context?.getDrawable(R.drawable.rectangle_light_grey_bg)


        add_entities = view.findViewById(R.id.add_entities)!!
        add_entities.setText(R.string.add_entity)
        add_corp_clients = view.findViewById(R.id.add_corp_clients)!!
        add_corp_clients.setText(R.string.add_corporate_client)

        add_notification = view.findViewById(R.id.add_notification)!!
        add_notification?.setOnClickListener(this)
        add_notification.setText("+ Add Notification")
        ll_documents_list = view.findViewById(R.id.ll_documents_list)!!
        at_assigned_client = view.findViewById(R.id.at_assigned_client)!!
        at_assigned_client.setHint(R.string.select_client_team_members)
        at_assigned_corp_client = view.findViewById(R.id.at_assigned_corp_client)!!
        at_assigned_corp_client.setHint(R.string.select_corporate_team_members)
        tv_assigned_corp_client = view.findViewById(R.id.tv_assigned_corp_client)!!
        tv_assigned_corp_client.setText(R.string.corporate_team_members)
        tv_assigned_client = view.findViewById(R.id.tv_assigned_client)!!
        tv_assigned_client.setText(R.string.client_team_members)
        tv_individual = view.findViewById(R.id.tv_individual)!!
        tv_individual.setText(R.string.add_individuals)
        tv_selected_clients = view.findViewById(R.id.tv_selected_clients)!!
        tv_selected_clients.setText(R.string.selected_entities)
        tv_selected_corp_clients = view.findViewById(R.id.tv_selected_corp_clients)!!
        tv_selected_corp_clients.setText(R.string.selected_corporate_tm)
        tv_selected_individual = view.findViewById(R.id.tv_selected_individual)!!
        tv_selected_individual.setText(R.string.selected_individuals)
        tv_selected_tm = view.findViewById(R.id.tv_selected_tm)!!
        tv_selected_tm.setText(R.string.selected_client_team_members)
        tv_selected_document = view.findViewById(R.id.tv_selected_document)!!
        tv_selected_document.setText(R.string.selected_documents)

        tv_name = view.findViewById(R.id.tv_name)!!
        tv_name.setText(R.string.selected_tm)
        tv_attach_document = view.findViewById(R.id.tv_attach_document)!!
        tv_attach_document.setText(R.string.add_documents)

        btn_add_tm = view.findViewById(R.id.btn_add_groups)!!
        ll_selected_teammembers = view.findViewById(R.id.selected_groups)!!
        btn_individual = view.findViewById(R.id.btn_individual)!!
        ll_selected_individual = view.findViewById(R.id.selected_individual)!!
        ll_individual_list = view.findViewById(R.id.ll_individual_list)!!
        btn_individual?.setOnClickListener(this)
        btn_add_tm?.setOnClickListener(this)
        ll_selected_entity_client = view.findViewById(R.id.selected_tm)!!
        ll_client_team_members = view.findViewById(R.id.ll_client_team_members)!!
        ll_corp_client_team_members = view.findViewById(R.id.ll_corp_client_team_members)!!
        ll_selected_entites = view.findViewById(R.id.ll_selected_entites)!!
        ll_selected_corp_clients = view.findViewById(R.id.ll_selected_corp_clients)!!
        ll_selected_corp_clients_view = view.findViewById(R.id.ll_selected_corp_clients_view)!!
        ll_selected_team_members = view.findViewById(R.id.ll_selected_team_members)!!
        btn_assigned_clients = view.findViewById(R.id.btn_assigned_clients)!!
        btn_assigned_clients?.setOnClickListener(this)

        //Invisible Add Button in all selecting member field.
        btn_attach_document.visibility = View.GONE
        btn_individual.visibility = View.GONE
        btn_add_tm.visibility = View.GONE
        btn_assigned_clients.visibility = View.GONE

        tv_event_creation_date = view.findViewById(R.id.tv_event_creation_date)!!
        tv_event_creation_date.setHint(R.string.date)
        tv_event_start_time = view.findViewById(R.id.tv_event_start_time)!!
        tv_event_start_time?.setOnClickListener(this)
        tv_event_start_time?.hint = ""
        tv_event_end_time = view.findViewById(R.id.tv_event_end_time)!!
        tv_event_end_time?.setOnClickListener(this)
        tv_event_end_time?.hint = ""
        ll_selected_documents = view.findViewById(R.id.selected_attached_documents)!!
        tv_meeting_link = view.findViewById(R.id.tv_meeting_link)!!

        tv_meeting_link.setHint(R.string.meeting_link)
        tv_meeting_link.setText(meetingLink)
        tv_dialing_number = view.findViewById(R.id.tv_dialing_number)!!
        tv_dialing_number.setHint(R.string.dial_in_number)
        tv_dialing_number?.inputType = InputType.TYPE_CLASS_NUMBER
        tv_location = view.findViewById(R.id.tv_location)!!
        tv_location.setHint(R.string.location)
        tv_description = view.findViewById(R.id.tv_description)!!
        tv_description.setHint(R.string.meeting_agenda)
        ll_project = view.findViewById(R.id.ll_project)!!

        ll_task = view.findViewById(R.id.ll_task)!!
        ll_task.visibility = View.GONE
        ll_repetetion = view.findViewById(R.id.ll_repetetion)!!
        ll_repetetion.visibility = View.VISIBLE
        ll_add_tm = view.findViewById(R.id.ll_add_groups)!!
        ll_assign_clients = view.findViewById(R.id.ll_assign_clients)!!
        ll_assign_clients.visibility = View.GONE
        ll_corp_tm = view.findViewById(R.id.ll_corp_tm)!!
        ll_entity_tm = view.findViewById(R.id.ll_entity_tm)!!
        ll_corp_clients = view.findViewById(R.id.ll_corp_clients)!!
        ll_corp_clients.visibility = View.GONE
        ll_add_entities = view.findViewById(R.id.ll_add_entities)!!
        ll_add_corp_clients = view.findViewById(R.id.ll_add_corp_clients)!!

        ll_documents_view = view.findViewById(R.id.ll_documents_view)!!
        ll_individual = view.findViewById(R.id.ll_individual)!!

        cv_add_clients.visibility = View.GONE
        ll_add_tm.visibility = View.GONE
        ll_add_entities.visibility = View.GONE
        ll_individual.visibility = View.GONE
        ll_documents_view.visibility = View.VISIBLE
        tv_meeting_link?.addTextChangedListener(Validation(tv_meeting_link))
        tv_dialing_number?.addTextChangedListener(Validation(tv_dialing_number))
        tv_location?.addTextChangedListener(Validation(tv_location))
        tv_description?.addTextChangedListener(Validation(tv_description))
        tv_message?.addTextChangedListener(Validation(tv_message))
        tv_description?.maxLines = 10
        tv_message?.maxLines = 10
        
        if (Constants.is_meeting != "Edit") {
            if (selectedValues.isEmpty()) {
                is_notify_clicked = false
                NotificationPopup()
            }
        }

        img_clear_sp_task.setOnClickListener {
            AndroidUtils.DisplaySpinnerView(sp_task, tv_sp_task_name, selected_task, img_dropdown_sp_task, img_clear_sp_task, false, taskSpinnerAdapter, "Search Task")
            ischecked_task = true
        }
        img_clear_sp_project.setOnClickListener {
            AndroidUtils.DisplaySpinnerView(sp_project, tv_sp_project, selected_project, img_dropdown_sp_project, img_clear_sp_project, false, spinner_adapter, "Search Project")
            ll_task.visibility = View.GONE
            selected_task = ""
            tv_sp_task_name.setText("")
            matter_name = ""
            tv_sp_matter_name.setText("")
            load_clear_list()
            hide_all_list()
            clear_selected_list()
            ll_matter_name.visibility = View.GONE
            tv_no_matter.visibility = View.GONE
            hide_members()
            ll_add_to_timesheet.visibility = View.GONE
            ischecked_project = true
        }
        img_clear_sp_timezone?.setOnClickListener {
            AndroidUtils.DisplaySpinnerView(sp_time_zone, tv_sp_time_zone, timezone_location, img_dropdown_sp_timezone, img_clear_sp_timezone, false, timezoneAdapter, "Search TimeZone")
            ischecked_time = true
        }
        img_clear_sp_repetetion?.setOnClickListener {
            AndroidUtils.DisplaySpinnerView(sp_repetetion, tv_sp_repetetion, repeat_interval, img_dropdown_sp_repetetion, img_clear_sp_repetetion, false, repetitionAdapter, "Search Repetition")
            ischecked_repetetion = true
        }
        img_clear_sp_mattername?.setOnClickListener {
            AndroidUtils.DisplaySpinnerView(sp_matter_name, tv_sp_matter_name, matter_name, img_dropdown_sp_mattername, img_clear_sp_mattername, false, matterSpinnerAdapter, "Search Matter")
            load_clear_list()
            cv_add_clients.visibility = View.GONE
            ischecked_matter = true
        }
        img_clear_sp_corp_clients?.setOnClickListener {
            corp_client_id = ""
            if (::corpAdapter.isInitialized) {
                AndroidUtils.DisplaySpinnerView(sp_corp_clients, tv_sp_corp_clients, corp_client_id, img_dropdown_sp_corp_clients, img_clear_sp_corp_clients, false, corpAdapter, "Search Corp Client")
            } else {
                tv_sp_corp_clients.text = ""
                img_clear_sp_corp_clients.visibility = View.GONE
                img_dropdown_sp_corp_clients.visibility = View.VISIBLE
            }
            is_corp = true
        }
        img_clear_sp_entity?.setOnClickListener {
            if (::entityAdapter.isInitialized) {
                AndroidUtils.DisplaySpinnerView(sp_entity, tv_sp_entity, entity_id, img_dropdown_sp_entity, img_clear_sp_entity, false, entityAdapter, "Search Entity")
            } else {
                tv_sp_entity.text = ""
                img_clear_sp_entity.visibility = View.GONE
                img_dropdown_sp_entity.visibility = View.VISIBLE
            }
            is_entity = true
        }
        ll_sp_task.setOnClickListener {
            if (!::taskSpinnerAdapter.isInitialized) {
                AndroidUtils.display_listview(ischecked_task, sp_task)
                ischecked_task = !ischecked_task
                return@setOnClickListener
            }
            val isVisible = sp_task.visibility == View.VISIBLE
            AndroidUtils.DisplaySpinnerView(sp_task, tv_sp_task_name,
                    tv_sp_task_name.text.toString(),
                    img_dropdown_sp_task, img_clear_sp_task,
                    !isVisible, taskSpinnerAdapter, "Search Task")
        }
        ll_sp_project.setOnClickListener {
            if (!::spinner_adapter.isInitialized) {
                AndroidUtils.display_listview(ischecked_project, sp_project)
                ischecked_project = !ischecked_project
                return@setOnClickListener
            }
            val isVisible = sp_project.visibility == View.VISIBLE
            AndroidUtils.DisplaySpinnerView(sp_project, tv_sp_project,
                    tv_sp_project.text.toString(),
                    img_dropdown_sp_project, img_clear_sp_project,
                    !isVisible, spinner_adapter, "Search Project")
        }

        ll_sp_mattername?.setOnClickListener {
            if (!::matterSpinnerAdapter.isInitialized) {
                AndroidUtils.display_listview(ischecked_matter, sp_matter_name)
                ischecked_matter = !ischecked_matter
                return@setOnClickListener
            }
            val isVisible = sp_matter_name.visibility == View.VISIBLE
            AndroidUtils.DisplaySpinnerView(sp_matter_name, tv_sp_matter_name,
                    tv_sp_matter_name.text.toString(),
                    img_dropdown_sp_mattername, img_clear_sp_mattername,
                    !isVisible, matterSpinnerAdapter, "Search Matter")
        }

        ll_sp_timezone?.setOnClickListener {
            if (!::timezoneAdapter.isInitialized) {
                AndroidUtils.display_listview(ischecked_time, sp_time_zone)
                ischecked_time = !ischecked_time
                return@setOnClickListener
            }
            val isVisible = sp_time_zone.visibility == View.VISIBLE
            AndroidUtils.DisplaySpinnerView(sp_time_zone, tv_sp_time_zone,
                    tv_sp_time_zone.text.toString(),
                    img_dropdown_sp_timezone, img_clear_sp_timezone,
                    !isVisible, timezoneAdapter, "Search TimeZone")
        }

        ll_sp_repetetion?.setOnClickListener {
            if (!::repetitionAdapter.isInitialized) {
                AndroidUtils.display_listview(ischecked_repetetion, sp_repetetion)
                ischecked_repetetion = !ischecked_repetetion
                return@setOnClickListener
            }
            val isVisible = sp_repetetion.visibility == View.VISIBLE
            AndroidUtils.DisplaySpinnerView(sp_repetetion, tv_sp_repetetion,
                    tv_sp_repetetion.text.toString(),
                    img_dropdown_sp_repetetion, img_clear_sp_repetetion,
                    !isVisible, repetitionAdapter, "Search Repetetion")
        }
        
        ll_sp_corp_clients?.setOnClickListener {
            if (!::corpAdapter.isInitialized || Corp_client_list.isEmpty()) {
                if (is_corp) {
                    if (Corp_client_list.isNotEmpty()) {
                        sp_corp_clients.visibility = View.VISIBLE
                    }
                } else {
                    sp_corp_clients.visibility = View.GONE
                }
                is_corp = !is_corp
                return@setOnClickListener
            }
            val isVisible = sp_corp_clients.visibility == View.VISIBLE
            AndroidUtils.DisplaySpinnerView(sp_corp_clients, tv_sp_corp_clients,
                    tv_sp_corp_clients.text.toString(),
                    img_dropdown_sp_corp_clients, img_clear_sp_corp_clients,
                    !isVisible, corpAdapter, "Search Corp Client")
            is_corp = !is_corp
        }
        
        ll_sp_entity?.setOnClickListener {
            if (!::entityAdapter.isInitialized || entities_list.isEmpty()) {
                if (is_entity) {
                    if (entities_list.isNotEmpty()) {
                        sp_entity.visibility = View.VISIBLE
                    }
                } else {
                    sp_entity.visibility = View.GONE
                }
                is_entity = !is_entity
                return@setOnClickListener
            }
            val isVisible = sp_entity.visibility == View.VISIBLE
            AndroidUtils.DisplaySpinnerView(sp_entity, tv_sp_entity,
                    tv_sp_entity.text.toString(),
                    img_dropdown_sp_entity, img_clear_sp_entity,
                    !isVisible, entityAdapter, "Search Entity")
            is_entity = !is_entity
        }
        
        at_add_tm?.setOnClickListener {
            if (is_clicked_team) {
                rv_groups_view.visibility = View.VISIBLE
                TeamMembersPopup()
            } else {
                rv_groups_view.visibility = View.GONE
            }
            is_clicked_team = !is_clicked_team
        }
        
        btn_cancel_event?.setOnClickListener {
            loadClearedLists()
            selectedValues.clear()
            meetings?.loadView()
        }
        
        at_individual?.setOnClickListener {
            if (is_clicked_individuals) {
                rv_individuals_view.visibility = View.VISIBLE
                load_individual_Popup()
            } else {
                rv_individuals_view.visibility = View.GONE
            }
            is_clicked_individuals = !is_clicked_individuals
        }
        
        at_assigned_corp_client?.setOnClickListener {
            if (is_clicked_corp_clients) {
                rv_corp_client_view.visibility = View.VISIBLE
                loadentity_corp_clients_popup()
            } else {
                rv_corp_client_view.visibility = View.GONE
            }
            is_clicked_corp_clients = !is_clicked_corp_clients
        }
        
        at_assigned_client?.setOnClickListener {
            if (is_clicked_clients) {
                rv_clients_view.visibility = View.VISIBLE
                loadEntityClientPopup()
            } else {
                rv_clients_view.visibility = View.GONE
            }
            is_clicked_clients = !is_clicked_clients
        }
        
        at_attach_document?.setOnClickListener {
            if (is_clicked_documents) {
                rv_documents_view.visibility = View.VISIBLE
                load_documents_Popup()
            } else {
                rv_documents_view.visibility = View.GONE
            }
            is_clicked_documents = !is_clicked_documents
        }
        
        projectList.clear()
        if (Constants.ROLE == "AAM") {
            projectList.add(CalendarDo("Overhead"))
            projectList.add(CalendarDo("Others"))
            projectList.add(CalendarDo("Reminders"))
        } else {
            projectList.add(CalendarDo("Legal Matter"))
            projectList.add(CalendarDo("General Matter"))
            projectList.add(CalendarDo("Overhead"))
            projectList.add(CalendarDo("Others"))
            projectList.add(CalendarDo("Reminders"))
        }
        legalTaksList.clear()

        spinner_adapter = CommonSpinnerAdapter(context as Activity, projectList)
        sp_project.adapter = spinner_adapter
        AndroidUtils.LoadList(sp_project, requireContext(), projectList.size, true)
        
        sp_project.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val selectedProjectItem = parent.getItemAtPosition(position) as CalendarDo
            selected_project = selectedProjectItem.projectName
            AndroidUtils.DisplaySpinnerView(sp_project, tv_sp_project, selected_project, img_dropdown_sp_project, img_clear_sp_project, false, spinner_adapter, "Search Project")
            ischecked_project = true
            ll_task.visibility = View.VISIBLE
            selected_task = ""
            tv_sp_task_name.setText("")
            at_assigned_corp_client.setText("")
            matter_name = ""
            tv_sp_matter_name.setText("")
            load_clear_list()
            hide_all_list()
            clear_selected_list()
            loadProjectData(selected_project!!)
        }
        
        loadRepetetions()
        tv_event_creation_date.setText(
                SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())
        )
        setStartAndEndTime()
        callCreateMeetingLinkWebservice()
        
        if (Constants.isFromNotification) {
            handleNotificationNavigation()
        }
        
        tv_event_creation_date?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateMeetingLinkFromCurrentValues()
            }
        })
        return view
    }
    
    // Abstract methods to ensure it satisfies requirements (as missing in chunk)

private fun handleNotificationNavigation() {
    val bundle = Constants.notificationBundle
    val route = bundle.getString(Constants.NavKeys.ROUTE_NAME)!!
    if (route != null) {
        eventId = bundle.getString(Constants.NavKeys.EVENT_ID, "")!!
    }
    Constants.isFromNotification = false
    Constants.notificationBundle.clear()
    // Get highlight IDs first (common for all routes)
//        callEventDetailsWebservice();
}

private fun loadcheckboxData() {
    cb_all_day.setOnCheckedChangeListener { _, isChecked ->
        if (isChecked) { // All Day mode selected
            ll_time.visibility = View.GONE
            selectedValues.clear()
            ll_add_notification.removeAllViews()
            display_duration("00:00", "23:59")
            isAllDay = true
        } else { // All Day mode unchecked
            ll_add_notification.removeAllViews()
            selectedValues.clear()
            ll_time.visibility = View.VISIBLE
            isAllDay = false

            // Set start and end times
            setStartAndEndTime()
        }
    }

    cb_add_to_timesheet.setOnCheckedChangeListener { _, _ ->
        cb_add_to_timesheet.isChecked = iscb_timesheet_checked
        iscb_timesheet_checked = !iscb_timesheet_checked
        isAddTimesheet = cb_add_to_timesheet.isChecked
    }
}

private fun callTimeZoneWebservice() {
    try {
        progressDialog = AndroidUtils.get_progress(requireActivity())
        val postData = JSONObject()
        WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "event/timezones", "TIMEZONES", postData.toString())
    } catch (e: Exception) {
        if (progressDialog != null && progressDialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progressDialog)
        }
        e.printStackTrace()
    }
}

private fun callCreateMeetingLinkWebservice() {
    try {
        val meetingLinkUrl: String
        if (Constants.ISPRODUCTION) {
            meetingLinkUrl = "https://avchat.digicoffer.com/api/v1/create-meeting-link"
        } else if (Constants.IS_STAGING) {
            meetingLinkUrl = "https://staging.api.avchat.digicoffer.com/api/v1/create-meeting-link"
        } else {
            meetingLinkUrl = "https://devapi.testavchat.digicoffer.com/api/v1/create-meeting-link"
        }
        Log.d("AVChat_MeetingLink", "Calling URL: $meetingLinkUrl")
        progressDialog = AndroidUtils.get_progress(requireActivity())
        val postData = JSONObject()

        WebServiceHelper.callHttpWebService(
            this,
            requireContext(),
            WebServiceHelper.RestMethodType.GET,
            meetingLinkUrl,
            "CREATE_MEETING_LINK",
            postData.toString()
        )

    } catch (e: Exception) {
        if (progressDialog != null && progressDialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progressDialog)
        }
        Log.e("AVChat_MeetingLink", "Exception: " + e.message)
        e.printStackTrace()
    }
}

//    private void setStartAndEndTime() {
//        Calendar calendar = Calendar.getInstance();
//        int hour = calendar.get(Calendar.HOUR_OF_DAY);
//        int minute = calendar.get(Calendar.MINUTE);
//        // Round minute to the nearest 5-minute interval
//        minute = (int) ((Math.round(minute / 15.0)) * 15);
//        if (minute == 60) {
//            minute = 0;
//            hour++; // Increment hour if it overflows
//        }
//
//        String currentTime = String.format("%02d:%02d", hour, minute);
//        tv_event_start_time.setText(currentTime);
//
//        // Set end time 30 minutes after start time
//        calendar.set(Calendar.HOUR_OF_DAY, hour);
//        calendar.set(Calendar.MINUTE, minute);
//        calendar.add(Calendar.MINUTE, 30);
//
//        String endTime = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
//        tv_event_end_time.setText(endTime);
//
//        // Update duration
//        display_duration(currentTime, endTime);
//    }
private fun setStartAndEndTime() {
    val calendar = Calendar.getInstance()
    var hour = calendar.get(Calendar.HOUR_OF_DAY)
    var minute = calendar.get(Calendar.MINUTE)

    // Round up to the next 15-minute interval
    minute = ((minute / 15) + 1) * 15
    if (minute == 60) {
        minute = 0
        hour++
        if (hour == 24) {
            hour = 0 // Reset to midnight
        }
    }

    // Set start time
    val currentTime = String.format("%02d:%02d", hour, minute)
    tv_event_start_time.setText(currentTime)

    // Set end time 30 minutes after start time
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    calendar.add(Calendar.MINUTE, 30)

    val endTime = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    tv_event_end_time.setText(endTime)

    // Update duration
    updateMeetingLinkFromCurrentValues()
    display_duration(currentTime, endTime)
}

private fun showCustomTimePicker(isStart: Boolean) {
    val builder = AlertDialog.Builder(requireContext())
    val view = LayoutInflater.from(requireContext()).inflate(R.layout.custom_time_picker, null)

    val npHour = view.findViewById<NumberPicker>(R.id.np_hour)
    val npMinute = view.findViewById<NumberPicker>(R.id.np_minute)
    val npAmPm = view.findViewById<NumberPicker>(R.id.np_am_pm)
    val btn_ok = view.findViewById<Button>(R.id.btn_ok)
    val btn_cancel = view.findViewById<Button>(R.id.btn_cancel)
    val iv_close = view.findViewById<ImageView>(R.id.iv_close)


    val calendar = Calendar.getInstance()
    val is24Hour = DateFormat.is24HourFormat(requireContext())
    var hour: Int
    var minute: Int

    // Get existing time from TextView
    val targetTextView = if (isStart) tv_event_start_time else tv_event_end_time
    if (targetTextView.text != null && targetTextView.text.toString().isNotEmpty()) {
        try {
            val timeParts = targetTextView.text.toString().split(":")
            hour = timeParts[0].toInt()
            minute = timeParts[1].toInt()
        } catch (e: Exception) {
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
        }
    } else {
        hour = calendar.get(Calendar.HOUR_OF_DAY)
        minute = calendar.get(Calendar.MINUTE)
    }

    // Round minute to nearest 15-minute interval
//        minute = (int) ((Math.round(minute / 15.0)) * 15);
//        if (minute == 60) {
//            minute = 0;
//            hour++; // Increment hour if it overflows
//        }
    minute = (Math.round(minute / 15.0) * 15).toInt()
    if (minute == 60) {
        minute = 0
        hour++ // Increment hour if it overflows
    }

    // Setup hour picker
    if (is24Hour) {
        npHour.minValue = 0
        npHour.maxValue = 23
    } else {
        npHour.minValue = 1
        npHour.maxValue = 12
        npAmPm.visibility = View.VISIBLE
        npAmPm.minValue = 0
        npAmPm.maxValue = 1
        npAmPm.displayedValues = arrayOf("AM", "PM")
        npAmPm.value = if (hour >= 12) 1 else 0 // AM = 0, PM = 1

        if (hour > 12) hour -= 12
        else if (hour == 0) hour = 12
    }
    npHour.value = hour
    npHour.wrapSelectorWheel = false

    // Setup minutes picker (15-minute intervals)
    npMinute.minValue = 0
    npMinute.maxValue = 3 // Update max value to match the number of items in the array
    val minuteValues = arrayOf("00", "15", "30", "45")
//        String[] minuteValues = {"00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"};
    npMinute.displayedValues = minuteValues
    npMinute.value = minute / 15 // Adjusted for 5-minute intervals
    npMinute.wrapSelectorWheel = false

    val dialog = builder.create()
    dialog.setView(view)
    dialog.show()

    if (dialog.window != null) {
        dialog.window!!.setBackgroundDrawable(context?.getDrawable(R.drawable.rectangular_white_background))
        dialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    iv_close.setOnClickListener { dialog.dismiss() }
    btn_cancel.setOnClickListener { dialog.dismiss() }
    btn_ok.setOnClickListener {
        var selectedHour = npHour.value
        val selectedMinute = minuteValues[npMinute.value].toInt()
//
//                if (!is24Hour) {
//                    if (npAmPm.getValue() == 1) { // PM selected
//                        selectedHour += 12;
//                        if (selectedHour == 24) selectedHour = 0; // Midnight case
//                    }
//                }

        if (!is24Hour) {
            if (npAmPm.value == 1) { // PM selected
                if (selectedHour != 12) selectedHour += 12
            } else {
                if (selectedHour == 12) selectedHour = 0 // Midnight
            }
        }

        val time = String.format("%02d:%02d", selectedHour, selectedMinute)

        if (isStart) {
            start_time = selectedHour
            startMinute = selectedMinute
            tv_event_start_time.setText(time)

            // Set default end time 30 minutes later
            val startCal = Calendar.getInstance()
            startCal.set(Calendar.HOUR_OF_DAY, selectedHour)
            startCal.set(Calendar.MINUTE, selectedMinute)

            val endCal = startCal.clone() as Calendar
            endCal.add(Calendar.MINUTE, 30)

            val endTimeFormatted = String.format("%02d:%02d", endCal.get(Calendar.HOUR_OF_DAY), endCal.get(Calendar.MINUTE))
            tv_event_end_time.setText(endTimeFormatted)
        } else {
            val startTimeMinutes = start_time * 60 + startMinute
            val endTimeMinutes = selectedHour * 60 + selectedMinute

            // Ensure end time is after start time
            if (start_time == selectedHour && endTimeMinutes <= startTimeMinutes) {
                val startCal = Calendar.getInstance()
                startCal.set(Calendar.HOUR_OF_DAY, start_time)
                startCal.set(Calendar.MINUTE, startMinute)

                val endCal = startCal.clone() as Calendar
                endCal.add(Calendar.MINUTE, 15) // Default to 15 minutes later

                val endTimeFormatted = String.format("%02d:%02d",
                    endCal.get(Calendar.HOUR_OF_DAY), endCal.get(Calendar.MINUTE))
                tv_event_end_time.setText(endTimeFormatted)
            } else {
                tv_event_end_time.setText(time)
            }
        }
        display_duration(tv_event_start_time.text.toString(), tv_event_end_time.text.toString())
        updateMeetingLinkFromCurrentValues() // ← add this
        dialog.dismiss()
    }
}


//    private void Show_Time_picker(boolean is_start, String title) {
//        Calendar calendar = Calendar.getInstance();
//        int hour, minute;
//        boolean is24Hour = Constants.is24HoursFormat(getContext()); // Check 24-hour format
//
//        // Determine which TextView to check for existing time
//        TextView targetTextView = is_start ? tv_event_start_time : tv_event_end_time;
//
//        if (targetTextView.getText() != null && !targetTextView.getText().toString().isEmpty()) {
//            try {
//                // Parse the time from TextView if available
//                String[] timeParts = targetTextView.getText().toString().split(":");
//                hour = Integer.parseInt(timeParts[0]);
//                minute = Integer.parseInt(timeParts[1]);
//            } catch (Exception e) {
//                // In case of error, fall back to the current time
//                hour = calendar.get(Calendar.HOUR_OF_DAY);
//                minute = calendar.get(Calendar.MINUTE);
//            }
//        } else {
//            // Default to current time if no time is set
//            hour = calendar.get(Calendar.HOUR_OF_DAY);
//            minute = calendar.get(Calendar.MINUTE);
//        }
//
//        TimePickerDialog timePickerDialog = new TimePickerDialog(
//                getContext(),
//                android.R.style.Theme_Holo_Light_Dialog,
//                new TimePickerDialog.OnTimeSetListener() {
//                    @Override
//                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                        view.clearFocus();
//
//                        // Ensure correct time retrieval for different API levels
//                        if (Build.VERSION.SDK_INT >= 23) {
//                            hourOfDay = view.getHour();
//                            minute = view.getMinute();
//                        } else {
//                            hourOfDay = view.getCurrentHour();
//                            minute = view.getCurrentMinute();
//                        }
//
//                        // Format the selected time
//                        String time = String.format("%02d:%02d", hourOfDay, minute);
//
//                        if (is_start) {
//                            start_time = hourOfDay;
//                            startMinute = minute;
//                            tv_event_start_time.setText(time);
//
//                            // Set default end time 30 minutes later
//                            Calendar startCal = Calendar.getInstance();
//                            startCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
//                            startCal.set(Calendar.MINUTE, minute);
//
//                            Calendar endCal = (Calendar) startCal.clone();
//                            endCal.add(Calendar.MINUTE, 30);
//
//                            String endTimeFormatted = String.format("%02d:%02d",
//                                    endCal.get(Calendar.HOUR_OF_DAY), endCal.get(Calendar.MINUTE));
//                            tv_event_end_time.setText(endTimeFormatted);
//                        } else {
//                            int startTimeMinutes = start_time * 60 + startMinute;
//                            int endTimeMinutes = hourOfDay * 60 + minute;
//
//                            // Ensure end time is after start time
//                            if (endTimeMinutes <= startTimeMinutes) {
//                                Calendar startCal = Calendar.getInstance();
//                                startCal.set(Calendar.HOUR_OF_DAY, start_time);
//                                startCal.set(Calendar.MINUTE, startMinute);
//
//                                Calendar endCal = (Calendar) startCal.clone();
//                                endCal.add(Calendar.MINUTE, 15); // Default to 15 minutes later
//
//                                String endTimeFormatted = String.format("%02d:%02d",
//                                        endCal.get(Calendar.HOUR_OF_DAY), endCal.get(Calendar.MINUTE));
//                                tv_event_end_time.setText(endTimeFormatted);
//                            } else {
//                                tv_event_end_time.setText(time);
//                            }
//                        }
//
//                        // Update duration display
//                        display_duration(tv_event_start_time.getText().toString(), tv_event_end_time.getText().toString());
//                    }
//                },
//                hour,
//                minute,
//                is24Hour
//        );
//
//        timePickerDialog.setTitle(title);
//
//        if (timePickerDialog.getWindow() != null) {
//            timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        }
//
//        timePickerDialog.show();
//    }


//    private void Show_Time_picker(boolean is_start, String title) {
//        Calendar calendar = Calendar.getInstance();
//        int hour = calendar.get(Calendar.HOUR_OF_DAY);
//        int minute = calendar.get(Calendar.MINUTE);
//        boolean is24Hour = Constants.is24HoursFormat(getContext());
//
//        TimePickerDialog timePickerDialog = new TimePickerDialog(
//                getContext(),
//                android.R.style.Theme_Holo_Light_Dialog,
//                new TimePickerDialog.OnTimeSetListener() {
//                    @Override
//                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                        // Ensure correct time is set even if only part of the time is changed manually
//                        view.clearFocus();
//                        hourOfDay = view.getCurrentHour();
//                        minute = view.getCurrentMinute();
//
//                        // Determine AM/PM
//                        boolean isPM = hourOfDay >= 12;
//                        String amPm = isPM ? " PM" : " AM";
//
//                        // Convert to 12-hour format
//                        int displayHour = (hourOfDay % 12);
//                        if (displayHour == 0) {
//                            displayHour = 12; // Midnight or Noon
//                        }
//
//                        String time = String.format("%02d:%02d%s", displayHour, minute, amPm);
//
//                        if (is_start) {
//                            start_time = hourOfDay; // Use 24-hour format internally
//                            startMinute = minute;
//                            tv_event_start_time.setText(time);
//
////                            if (tv_event_end_time.getText().toString().isEmpty()) {
//                            // Calculate end time as 30 minutes later
//                            LocalTime startLocalTime = LocalTime.of(hourOfDay, minute);
//                            LocalTime endLocalTime = startLocalTime.plusMinutes(30);
//
//                            // Determine end time AM/PM
//                            int endHour = endLocalTime.getHour();
//                            int endMinute = endLocalTime.getMinute();
//                            String endAmPm = (endHour >= 12) ? " PM" : " AM";
//                            int displayEndHour = (endHour % 12);
//                            if (displayEndHour == 0) {
//                                displayEndHour = 12; // Midnight or Noon
//                            }
//
//                            String endTimeFormatted = String.format("%02d:%02d%s", displayEndHour, endMinute, endAmPm);
//                            tv_event_end_time.setText(endTimeFormatted);
////                            }
//                        } else {
//                            // Validate end time
//                            int startTimeMinutes = start_time * 60 + startMinute;
//                            int endTimeMinutes = hourOfDay * 60 + minute;
//
//                            tv_event_end_time.setText(time);
//                        }
//
//                        // Update duration display
//                        display_duration(tv_event_start_time.getText().toString(), tv_event_end_time.getText().toString());
//                    }
//                },
//                hour,
//                minute,
//                is24Hour
//        );
//
//        timePickerDialog.setTitle(title);
//        Objects.requireNonNull(timePickerDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        timePickerDialog.show();
//    }
//    private void Show_Time_picker(boolean is_start, String title) {
//        Calendar calendar = Calendar.getInstance();
//        int hour = calendar.get(Calendar.HOUR_OF_DAY);
//        int minute = calendar.get(Calendar.MINUTE);
//        boolean is24Hour = Constants.is24HoursFormat(getContext());
//        TimePickerDialog timePickerDialog = new TimePickerDialog(
//                getContext(),
//                android.R.style.Theme_Holo_Light_Dialog,
//                new TimePickerDialog.OnTimeSetListener() {
//                    @Override
//                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                        // Determine AM/PM
//                        boolean isPM = hourOfDay >= 12;
//                        String amPm = isPM ? " PM" : " AM";
//
//                        // Convert to 12-hour format
//                        int displayHour = (hourOfDay % 12);
//                        if (displayHour == 0) {
//                            displayHour = 12; // Midnight or Noon
//                        }
//
//                        String time = String.format("%02d:%02d%s", displayHour, minute, amPm);
//
//                        if (is_start) {
//                            start_time = displayHour;
//                            startMinute = minute;
//                            tv_event_start_time.setText(time);
//
//                            if (tv_event_end_time.getText().toString().isEmpty()) {
//                                // Calculate end time as 30 minutes later
//                                LocalTime startLocalTime = LocalTime.of(hourOfDay, minute);
//                                LocalTime endLocalTime = startLocalTime.plusMinutes(30);
//
//                                // Determine end time AM/PM
//                                int endHour = endLocalTime.getHour();
//                                int endMinute = endLocalTime.getMinute();
//                                String endAmPm = (endHour >= 12) ? " PM" : " AM";
//                                int displayEndHour = (endHour % 12);
//                                if (displayEndHour == 0) {
//                                    displayEndHour = 12; // Midnight or Noon
//                                }
//
//                                String endTimeFormatted = String.format("%02d:%02d%s", displayEndHour, endMinute, endAmPm);
//                                tv_event_end_time.setText(endTimeFormatted);
//                            }
//                        } else {
//                            // Validate end time
//                            int startTimeMinutes = start_time * 60 + startMinute;
//                            int endTimeMinutes = hourOfDay * 60 + minute;
//
////                            if (endTimeMinutes < startTimeMinutes) {
////                                AndroidUtils.showAlert("Please select End Time within this Date", getContext());
////                                tv_event_end_time.setText("");
////                                Time_duration.setText(R.string.duration);
////                            } else {
//                            tv_event_end_time.setText(time);

    /// /                            }
//                        }
//
//                        // Update duration display
//                        display_duration(tv_event_start_time.getText().toString(), tv_event_end_time.getText().toString());
//                    }
//                },
//                hour,
//                minute,
//                is24Hour
//        );
//
//        timePickerDialog.setTitle(title);
//        Objects.requireNonNull(timePickerDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        timePickerDialog.show();
//    }
override fun onClick(view: View) {
    when (view.id) {
//            case R.id.tv_event_creation_date:
//                tv_event_creation_date.setOnClickListener(v -> showDatePicker(tv_event_creation_date, true));
//                break;
        R.id.tv_event_start_time -> {
            if (tv_event_creation_date.text.toString() == "") {
                AndroidUtils.showAlert("Please select the date first", activity)
            } else {
                showCustomTimePicker(true)
            }
        }
        R.id.tv_event_end_time -> {
            if (tv_event_creation_date.text.toString() == "") {
                AndroidUtils.showAlert("Please select the date first", activity)
            } else if (tv_event_start_time.text.toString() == "") {
                AndroidUtils.showAlert("Please Select the Start Time", activity)
            } else {
                showCustomTimePicker(false)
            }
        }
        R.id.add_notification -> {
            is_notify_clicked = false
            if (!is_notify_clicked) {
                NotificationPopup()
            }
        }
//            case R.id.btn_cancel_timesheet:
//                AndroidUtils.showToast("Event is not Created", getContext());
//                loadClearedLists();
//                meetings.loadView();
        R.id.btn_create_event -> createEvent()
    }//End of Switch Case...
}

private fun createEvent() {
    var notify_has_error = false
    var isTimeError = false
    val alphabeticPattern = Pattern.compile(".*[a-zA-Z].*")
    val numericPattern = Pattern.compile(".*[0-9].*")

    if (selectedValues.isNotEmpty()) {
        if (!is_timenotify) {
            notify_has_error = true
        }

        // Check each entry in selectedValues for both alphabetic and numeric characters
        for (value in selectedValues) {
            val hasAlphabetic = alphabeticPattern.matcher(value)
            val hasNumeric = numericPattern.matcher(value)

            // If an entry does not have both alphabetic and numeric characters, set the error flag
            if (!(hasAlphabetic.find() && hasNumeric.find())) {
                notify_has_error = true
                break  // Exit the loop as soon as an invalid entry is found
            }
        }
    } else {
        notify_has_error = false  // No entries, so no error
    }

    isTimeError = tv_event_start_time.text.toString().isEmpty() && (!isAllDay)


    Log.d("selected_values_size", "" + selectedValues.size)
    if (matter_legal != "reminders") {
        val msg = java.lang.StringBuilder("Please check the")

        // 1. No project selected at all
        if (matter_legal.isEmpty()) {
            AndroidUtils.showAlert("Please check the Project", activity)
            return
        }

        if (matter_legal == "general" || matter_legal == "legal") {

            // 2. No matters found (tv_no_matter visible)
            if (tv_no_matter.visibility == View.VISIBLE) {
                Constants.create_matter = true
                Constants.MATTER_TYPE = if (matter_legal == "general") "General" else "Legal"
                AndroidUtils.showReDirectionPopup(requireActivity(), Matter(),
                        "Please create a matter to create an event. Click here to create matter.")
                return
            }

            // 3. Matter name not selected
            if (tv_sp_matter_name.text.toString().isEmpty()) {
                AndroidUtils.showAlert("Please check the Matter Name", activity)
                return
            }

            // 4. Task, date, time, timezone, notification checks
            if (tv_sp_task_name.text.toString().trim().isEmpty()
                    || tv_event_creation_date.text.toString().trim().isEmpty()
                    || isTimeError
                    || tv_sp_time_zone.text.toString().trim().isEmpty()
                    || notify_has_error) {

                if (tv_sp_task_name.text.toString().trim().isEmpty()) {
                    msg.append(" Task")
                }
                if (tv_event_creation_date.text.toString().trim().isEmpty()) {
                    msg.append(if (msg.toString() == "Please check the") " Date" else ", Date")
                }
                if (isTimeError) {
                    msg.append(if (msg.toString() == "Please check the") " Time" else ", Time")
                }
                if (tv_sp_time_zone.text.toString().trim().isEmpty()) {
                    msg.append(if (msg.toString() == "Please check the") " Timezone" else ", Timezone")
                }
                if (notify_has_error) {
                    msg.append(if (msg.toString() == "Please check the") " Notification" else ", Notification")
                }
                AndroidUtils.showAlert(msg.toString(), activity)
            } else {
                EventCreation() // ← legal/general happy path
            }

        } else {
            // overhead / others
            if (tv_sp_task_name.text.toString().trim().isEmpty()
                    || tv_event_creation_date.text.toString().trim().isEmpty()
                    || isTimeError
                    || tv_sp_time_zone.text.toString().trim().isEmpty()
                    || notify_has_error) {

                if (tv_sp_task_name.text.toString().trim().isEmpty()) {
                    msg.append(" Task")
                }
                if (tv_event_creation_date.text.toString().trim().isEmpty()) {
                    msg.append(if (msg.toString() == "Please check the") " Date" else ", Date")
                }
                if (isTimeError) {
                    msg.append(if (msg.toString() == "Please check the") " Time" else ", Time")
                }
                if (tv_sp_time_zone.text.toString().trim().isEmpty()) {
                    msg.append(if (msg.toString() == "Please check the") " Timezone" else ", Timezone")
                }
                if (notify_has_error) {
                    msg.append(if (msg.toString() == "Please check the") " Notification" else ", Notification")
                }
                AndroidUtils.showAlert(msg.toString(), activity)
            } else {
                EventCreation()
            }
        }

    } else {
        //Date,time,timezone,message
        if (tv_message.text!!.toString().trim().isEmpty() || tv_event_creation_date.text!!.toString().trim().isEmpty() || isTimeError || tv_sp_time_zone.text.toString().trim().isEmpty() || notify_has_error) {
            // Check if member name is empty
//                            hasErrors = true;
            //..
            var msg = "Please check the"
            if (tv_sp_project.text!!.toString().isEmpty()) {
                msg = "$msg Project"
            }
            if (tv_message.text!!.toString().isEmpty()) {
                if (msg == "Please check the") {
                    msg = "$msg Message"
                } else {
                    msg = "$msg, Message"
                }
            }
            if (tv_event_creation_date.text!!.toString().isEmpty()) {
//                    AndroidUtils.showAlert(msg + ", Task", getContext());
                if (msg == "Please check the") {
                    msg = "$msg Date"
                } else {
                    msg = "$msg,Date"
                }
            }
            if (isTimeError) {
                if (msg == "Please check the") {
                    msg = "$msg Time"
                } else {
                    msg = "$msg, Time"
                }
//                                    AndroidUtils.showAlert("Start Time is required", getContext());
            }
            if (tv_sp_time_zone.text!!.toString().isEmpty()) {
                if (msg == "Please check the") {
                    msg = "$msg Time Zone"
                } else {
                    msg = "$msg, Time Zone"
                }
            }
//                        if (Objects.equals(Constants.is_meeting, "Create")) {
            if (notify_has_error) {
                if (msg == "Please check the") {
                    msg = "$msg Notification"
                } else {
                    msg = "$msg, Notification"
                }
            }
//                        }
            AndroidUtils.showAlert(msg, activity)
            // Validate email format
            //..
        } else {
            EventCreation()
        }
    }
}

private fun EventCreation() {
    if (Constants.is_meeting == "Create")
        callCreateEventWebservice()
    else {
        event_update_scope = "UPDATE_EVENT_ONLY"
        if ((existing_repetetion == "None") || (tv_sp_repetetion.text.toString() == "None") || (!isRecurring)) {
            CheckUpdateScope("all")
        } else {
            update_event()
        }
    }
}

private fun getDatePickerDialog(): DatePickerDialog {
    val myCalendar = Calendar.getInstance()
    val date = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        myCalendar.set(Calendar.YEAR, year)
        myCalendar.set(Calendar.MONTH, month)
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        tv_event_creation_date.setText(sdf.format(myCalendar.time))
    }
    return DatePickerDialog(activity!!, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH))
}

private fun display_duration(start_time: String, end_time: String) {
    try {
        // Parse start time in HH:mm (24-hour) format
        val startParts = start_time.split(":")
        if (startParts.size != 2) {
            Time_duration.setText("Invalid start time format")
            return
        }

        val startHour = startParts[0].toInt()
        val startMinute = startParts[1].toInt()

        // Parse end time in HH:mm (24-hour) format
        val endParts = end_time.split(":")
        if (endParts.size != 2) {
            Time_duration.setText("Invalid end time format")
            return
        }

        val endHour = endParts[0].toInt()
        val endMinute = endParts[1].toInt()

        // Calculate total minutes from midnight for both start and end time
        val startTotalMinutes = startHour * 60 + startMinute
        val endTotalMinutes = endHour * 60 + endMinute

        // Calculate the duration in minutes
        val durationMinutes: Int
        if (endTotalMinutes < startTotalMinutes) {
            // Handle the case where end time is the next day
            durationMinutes = (24 * 60 - startTotalMinutes) + endTotalMinutes
        } else {
            durationMinutes = endTotalMinutes - startTotalMinutes
        }

        // Convert duration into hours and minutes
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60

        // Display the result
        val durationText: String
        if (hours == 0) {
            durationText = String.format("Duration: %d min", minutes)
        } else {
            durationText = String.format("Duration: %d hr, %d min", hours, minutes)
        }
        Time_duration.setText(durationText)
    } catch (e: NumberFormatException) {
        Time_duration.setText("Invalid time format")
        Log.d("T_Duration", "Invalid time format: " + e.message)
    }
}

//    private void display_duration(String start_time, String end_time) {
//        try {
//            // Remove AM/PM and parse start time
//            var start_time = start_time
//            val isStartAM = start_time.contains("AM")
//            start_time = start_time.replace(" AM", "").replace(" PM", "")
//            val startParts = start_time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//
//            if (startParts.size != 2) {
//                Time_duration.setText("Invalid start time format")
//                return
//            }
//
//            var startHour = startParts[0].toInt()
//            val startMinute = startParts[1].toInt()
//
//            if (startHour < 12) {
//                startHour += 12
//            }
//            if (isStartAM && startHour == 12) {
//                startHour = 0
//            }
//
//            // Remove AM/PM and parse end time
//            var end_time = end_time
//            val isEndAM = end_time.contains("AM")
//            end_time = end_time.replace(" AM", "").replace(" PM", "")
//            val endParts = end_time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//
//            if (endParts.size != 2) {
//                Time_duration.setText("Invalid end time format")
//                return
//            }
//
//            var endHour = endParts[0].toInt()
//            val endMinute = endParts[1].toInt()
//
//            if (!isEndAM && endHour < 12) {
//                endHour += 12
//            }
//            if (isEndAM && endHour == 12) {
//                endHour = 0
//            }
//
//            // Calculate duration
//            val startTotalMinutes = startHour * 60 + startMinute
//            val endTotalMinutes = endHour * 60 + endMinute
//
//            val durationMinutes: Int
//            if (endTotalMinutes < startTotalMinutes) {
//                // Handle case where end time is on the next day
//                durationMinutes = (24 * 60 - startTotalMinutes) + endTotalMinutes
//            } else {
//                durationMinutes = endTotalMinutes - startTotalMinutes
//            }
//
//            val hours = durationMinutes / 60
//            val minutes = durationMinutes % 60
//
//            val durationText = String.format("Duration: %d Hours, %d Minutes", hours, minutes)
//            Time_duration.setText(durationText)
//        } catch (e: NumberFormatException) {
//            Time_duration.setText(R.string.duration)
//            Log.d("T_Duration", "Invalid time format")
//        }
//    }

    fun selected_documents(list_item: ArrayList<DocumentsDo>) {
//        selected_documents_list.clear()
//        for (i in list_item.indices) {
//            val documentsDo = list_item[i]
//            if (documentsDo.isChecked) {
//                if (!selected_documents_list.contains(documentsDo)) {
//                    selected_documents_list.add(documentsDo)
//                }
//            }
//        }
        if (selected_documents_list.isEmpty()) {
            ll_selected_documents.visibility = View.GONE
            at_attach_document.setText("")
        } else {
            loadSelectedDocuments()
        }
//    is_clicked_documents = true
//    rv_documents_view.visibility = View.GONE
    }

    private fun Documents_Popup() {
        try {
            //            documents_list.clear()
            if (documents_list.isEmpty()) {
                for (i in matterList.indices) {
                    if (matter_id == matterList[i].id) {
                        val documents = matterList[i].documents
                        for (j in 0 until documents.length()) {
                            val documentsDo = DocumentsDo()
                            val jsonObject = documents.optJSONObject(j)
                            documentsDo.docid = jsonObject.optString("docid")
                            documentsDo.doctype = jsonObject.optString("doctype")
                            documentsDo.name = jsonObject.optString("name")
//                            documentsDo.user_id = jsonObject.getString("user_id")!!
                            documents_list.add(documentsDo)
                        }
                    }
                }
            }
            if (documents_list.isEmpty()) {
                ll_documents_view.visibility = View.GONE
            } else {
                ll_documents_view.visibility = View.VISIBLE
            }
            load_documents_Popup()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun load_documents_Popup() {
        try {
            if (documents_list.isNotEmpty()) {
//                rv_documents_view.visibility = View.GONE
//                is_clicked_documents = true
//                AndroidUtils.showToast("No document to show", requireContext())
//            } else {
//                rv_documents_view.visibility = View.VISIBLE
                for (i in documents_list.indices) {
                    val documentsDo = documents_list[i]
                    documentsDo.isChecked = false
                    for (j in selected_documents_list.indices) {
                        if (documents_list[i].docid == selected_documents_list[j].docid) {
                            documentsDo.isChecked = true
//                        selected_groups_list.set(j,documentsModel)
                        }
                    }
                }
//            selected_tm_list.clear()
                val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                rv_documents_view.layoutManager = layoutManager
                // rv_documents_view.setHasFixedSize(true)
                ADAPTER_TAG = "Documents"
                val documentsAdapter = CommonRelationshipsAdapter(teamList, ADAPTER_TAG, individual_list, entity_client_list, entity_corp_client_list, documents_list, this)
                rv_documents_view.adapter = documentsAdapter
                //..
                AndroidUtils.LoadList(rv_documents_view, requireContext(), documents_list.size, true)
                //..
//                btn_attach_document.setOnClickListener(object : View.OnClickListener {
//                    override fun onClick(view: View) {
//                        //Clearing the existing chosen documents and re added the documents.
//                        selected_documents_list.clear()
//                        for (i in documentsAdapter.documents_list.indices) {
//                            val documentsDo = documentsAdapter.documents_list[i]
//                            if (documentsDo.isChecked) {
//                                if (!selected_documents_list.contains(documentsDo)) {
//                                    selected_documents_list.add(documentsDo)
//                                }
////                        AndroidUtils.showAlert(selected_individual_list.toString(),getContext())
////                        new_selected_individual_list.add(teamModel)
//                                //                           jsonArray.put(selected_documents_list.get(i).group_name)
//                            }
//                        }
//
//                        loadSelectedDocuments()
//                        is_clicked_documents = true
//                        rv_documents_view.visibility = View.GONE
////                    loadSelectedIndividual()
//                    }
//                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AndroidUtils.showAlert(e.message ?: "", requireActivity())
        }
    }

    private fun loadSelectedDocuments() {
        val value = arrayOfNulls<String>(selected_documents_list.size)
        for (i in selected_documents_list.indices) {
//                                value += "," + family_members.get(i)
//                               value.add(family_members.get(i))
            value[i] = selected_documents_list[i].name
        }
        val str = java.lang.String.join(",", *value)
        at_attach_document.setText(str)
        if (selected_documents_list.isEmpty()) {
            ll_selected_documents.visibility = View.GONE
        } else {
//            ll_selected_documents.visibility = View.VISIBLE
        }

        ll_documents_list.removeAllViews()
        for (i in selected_documents_list.indices) {
            val view_opponents = LayoutInflater.from(requireContext()).inflate(R.layout.edit_opponent_advocate, null)
            val tv_opponent_name = view_opponents.findViewById<TextView>(R.id.tv_opponent_name)
            tv_opponent_name.setText(selected_documents_list[i].name)
            val iv_edit_opponent = view_opponents.findViewById<ImageView>(R.id.iv_edit_opponent)
            val iv_remove_opponent = view_opponents.findViewById<ImageView>(R.id.iv_remove_opponent)
            iv_remove_opponent.tag = i
            iv_remove_opponent.setOnClickListener { v ->
                try {
                    val position = v.tag as Int

                    // Remove the view at the specified position
                    ll_documents_list.removeViewAt(position)

                    // Remove the corresponding item from the list
                    val teamModel = selected_documents_list.removeAt(position)
                    teamModel.isChecked = false

                    // Update the tags of the remaining views
                    for (j in 0 until ll_documents_list.childCount) {
                        val iv_remove = ll_documents_list.getChildAt(j).findViewById<ImageView>(R.id.iv_remove_opponent)
                        if (iv_remove != null) {
                            iv_remove.tag = j
                        }
                    }

                    // Update the attached document text
                    val stringBuilder = java.lang.StringBuilder()
                    for (model in selected_documents_list) {
                        stringBuilder.append(model.name).append(",")
                    }

                    if (stringBuilder.length > 0) {
                        stringBuilder.deleteCharAt(stringBuilder.length - 1) // Remove the last comma
                    }

                    at_attach_document.setText(stringBuilder.toString())

                    // Show or hide the selected documents layout
                    if (selected_documents_list.isEmpty()) {
                        ll_selected_documents.visibility = View.GONE
                        at_attach_document.setText("")
                    } else {
//                            ll_selected_documents.visibility = View.VISIBLE
                    }
                    rv_documents_view.adapter?.notifyDataSetChanged()

                } catch (e: Exception) {
                    e.printStackTrace() // Log the error
                    AndroidUtils.showAlert(e.message ?: "", requireActivity())
                }
            }
            iv_edit_opponent.visibility = View.GONE
            ll_documents_list.addView(view_opponents)
        }
    }

    private fun callCreateEventWebservice() {
        try {
            val doctype = "doctype"
            val docid = "docid"
            val postData = JSONObject()
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val selected_team_member = JSONArray()
            val selected_clients_list = JSONArray()
            val selected_corp_clients_list = JSONArray()
            val individual_array = JSONArray()
            val added_notification_list = JSONArray()
            val time_sheets = JSONArray()
            val existing_attachments = JSONArray()
            for (i in selectedValues.indices) {
//            NotifyMeDo notifyMeDo = notifyme_list.get(i)
                Log.d("Notify_array1", selectedValues[i])
                added_notification_list.put(selectedValues[i])
            }
            for (i in 0 until added_notification_list.length()) {
                Log.d("Notify_array2", "" + added_notification_list[i])
            }
            var selected_docs: JSONObject
            for (j in selected_documents_list.indices) {
                selected_docs = JSONObject()
                val attachDocumentsDO = selected_documents_list[j]
                selected_docs.put(doctype, attachDocumentsDO.doctype)
                selected_docs.put(docid, attachDocumentsDO.docid)
                existing_attachments.put(selected_docs)
            }

            for (i in selected_tm_list.indices) {
                val addTeamMembersDo = selected_tm_list[i]
                selected_team_member.put(addTeamMembersDo.id)
            }
            for (i in selected_entity_client_list.indices) {
                val addClientsDo = selected_entity_client_list[i]
                selected_clients_list.put(addClientsDo.id)
            }
            for (i in selected_entity_corp_client_list.indices) {
                val addClientsDo = selected_entity_corp_client_list[i]
                selected_corp_clients_list.put(addClientsDo.id)
            }
            for (i in selected_individual_list.indices) {
                val relationshipsDO = selected_individual_list[i]
                individual_array.put(relationshipsDO.id)
            }

            val event_date = AndroidUtils.stringToDateTimeDefault(tv_event_creation_date.text.toString(), "MMM dd, yyyy")
            event_creation_date = AndroidUtils.getDateToString(event_date, "yyyy-MM-dd")
            var event_start_date: Date? = null
            var event_date2: Date? = null
            var start_time = tv_event_start_time.text.toString()
            if (start_time.isEmpty()) {
                start_time = "00:00"
                event_start_date = AndroidUtils.stringToDateTimeDefault(start_time, "HH:mm")

            } else {
                event_start_date = AndroidUtils.stringToDateTimeDefault(start_time, "HH:mm")
            }
            event_starting_date = AndroidUtils.getDateToString(event_start_date, event_creation_date + "'T'HH:mm:ss")
            var end_time = tv_event_end_time.text.toString()
            if (end_time.isEmpty()) {
                end_time = "00:00"
                event_date2 = AndroidUtils.stringToDateTimeDefault(end_time, "HH:mm")
            } else {
                event_date2 = AndroidUtils.stringToDateTimeDefault(end_time, "HH:mm")
            }
            var duration_timesheet = ""
            if (event_start_date != null && event_date2 != null) {

                val differenceInMilliSeconds = Math.abs(event_start_date.time - event_date2.time)
                val differenceInHours = (differenceInMilliSeconds / (60 * 60 * 1000)) % 24
                val differenceInMinutes = (differenceInMilliSeconds / (60 * 1000)) % 60
                val differenceInSeconds = (differenceInMilliSeconds / 1000) % 60
                val duration = differenceInHours.toString() + ":" + differenceInMinutes
                val hours = AndroidUtils.stringToDateTimeDefault(duration, "HH:mm")
                duration_timesheet = AndroidUtils.getDateToString(hours, "HH:mm")
            }
            event_end_time = AndroidUtils.getDateToString(event_date2, event_creation_date + "'T'HH:mm:ss")

            if (isAddTimesheet) {
                val time_sheet_obj: JSONObject
//                        for (int i = 0; i < time_sheets.length(); i++) {
                time_sheet_obj = JSONObject()
                time_sheet_obj.put("date", event_creation_date)
                if (isAllDay) {
                    time_sheet_obj.put("duration", "23:59")
                    event_starting_date = event_creation_date + "T00:00:00"
                    event_end_time = event_creation_date + "T23:59:59"
                } else {
                    if (duration_timesheet.isEmpty()) {
                        time_sheet_obj.put("duration", "30:00")
                    } else {
                        time_sheet_obj.put("duration", duration_timesheet)
                    }
                }
                time_sheet_obj.put("eventtitle", selected_task)
                time_sheet_obj.put("addedby", Constants.NAME)
                time_sheet_obj.put("user_id", Constants.USER_ID)
                if ((matter_legal != "legal" && matter_legal != "general")) {
                    time_sheet_obj.put("matter_id", tv_sp_project.text.toString())
                    time_sheet_obj.put("matter_type", matter_legal)
                }
                time_sheets.put(time_sheet_obj)
            }
            val multiplied_offset = (-1) * offset
            val dateInput = event_creation_date // "Jan 31, 2026"

// Parse WITHOUT time manipulation
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = inputFormat.parse(dateInput)

// Now build exact UTC output WITHOUT converting timezone
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            if (date != null) {
                cal.time = date
            }

// Force UTC midnight (don’t let Java auto-convert)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            utcFormat.timeZone = TimeZone.getTimeZone("UTC")
            val finalDate = utcFormat.format(cal.time)

            postData.put("date", finalDate)

            postData.put("attachments", existing_attachments)
//            } catch (ParseException e) {
//                e.printStackTrace()
//            } catch (JSONException e) {
//                e.printStackTrace()
//            }
            postData.put("invitees_corporate", selected_corp_clients_list)
            postData.put("invitees_internal", selected_team_member)
            postData.put("invitees_external", selected_clients_list)
            postData.put("invitees_consumer_external", individual_array)
            if (matter_legal == "legal" || matter_legal == "general") {
                postData.put("title", matter_name + " - " + tv_sp_task_name.text.toString())
            } else if (matter_legal == "overhead" || matter_legal == "others") {
                postData.put("title", tv_sp_task_name.text.toString())
            } else {
                postData.put("title", matter_legal)
            }

            postData.put("notifications", added_notification_list)
//            postData.put("description", tv_description.getText().toString())
            postData.put("timezone_location", timezone_location)
            postData.put("timezone_offset", multiplied_offset)
            var repetetion = ""
            if (tv_sp_repetetion.text.toString().lowercase(Locale.ROOT) == "bi-weekly") {
                repetetion = "biweekly"
            } else if (tv_sp_repetetion.text.toString() == "None") {
                repetetion = ""
            } else {
                repetetion = tv_sp_repetetion.text.toString().lowercase(Locale.ROOT)
            }
            Log.d("Repetation", repetetion)
            postData.put("repeat_interval", repetetion)
            postData.put("meeting_link", Objects.requireNonNull(tv_meeting_link.text).toString())
            postData.put("allday", isAllDay)
            postData.put("from_ts", event_starting_date)
            postData.put("to_ts", event_end_time)
            if (matter_legal != "reminders") {
                postData.put("dialin", Objects.requireNonNull(tv_dialing_number.text).toString())
                postData.put("location", Objects.requireNonNull(tv_location.text).toString())
                postData.put("addtimesheet", isAddTimesheet)
                postData.put("timesheets", time_sheets)
                postData.put("description", Objects.requireNonNull(tv_description.text).toString())
            } else {
                postData.put("description", tv_message.text.toString())
            }
            if (matter_legal == "legal" || matter_legal == "general") {
                postData.put("matter_type", matter_legal)
                postData.put("matter_id", matter_id)
            }
            postData.put("event_type", matter_legal)

            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.POST, "v3/events", "CREATE_EVENT", postData.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//    private void NotificationPopup() {
//        View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.add_calendar_notification, null);
//        LinearLayout tv_notify_layout = view_opponents.findViewById(R.id.tv_notify_layout)!!;
//        TextView tv_sp_minutes = tv_notify_layout.findViewById(R.id.tv_spinner_view)!!;
////        ImageView sp_notify_dropdown_icon = tv_notify_layout.findViewById(R.id.img_dropdown_icon)!!;
////        ImageView sp_notify_clear_icon = tv_notify_layout.findViewById(R.id.img_clear_icon)!!;
//
//        ListView sp_minutes = view_opponents.findViewById(R.id.sp_minutes)!!;
//        TextInputEditText tv_numbers = view_opponents.findViewById(R.id.tv_numbers)!!;
//        tv_numbers.setInputType(InputType.TYPE_CLASS_NUMBER);
//        ArrayList<MinutesDO> minutes_list = new ArrayList<>();
//        TextView error_msg = view_opponents.findViewById(R.id.error_msg)!!;
//        TextView error_msg1 = view_opponents.findViewById(R.id.error_msg1)!!;
//        error_msg.setTextColor(requireContext().getResources().getColor(R.color.Red));
//        error_msg.setTextSize(12);
//        error_msg1.setTextColor(requireContext().getResources().getColor(R.color.Red));
//        error_msg1.setTextSize(12);
//        error_msg1.setVisibility(View.GONE);
////        error_msg.setVisibility(View.GONE);
//        minutes_list.add(new MinutesDO("Minutes"));
//        minutes_list.add(new MinutesDO("Hours"));
//        minutes_list.add(new MinutesDO("Days"));
//        minutes_list.add(new MinutesDO("Weeks"));
//        sp_minutes.setVisibility(View.GONE);
//        tv_notify_layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AndroidUtils.display_listview(is_notify, sp_minutes);
//                is_notify = !is_notify;
//            }
//        });
//        ImageView iv_delete_notification = view_opponents.findViewById(R.id.iv_delete_notification)!!;
////        tv_numbers.setText(R.string._10);
//        if (!selectedValues.isEmpty()) {
//            if (Objects.equals(Constants.is_meeting, "Edit")) {
//                tv_sp_minutes.setText(AndroidUtils.CapitalizeFirstLetter(time_value));
//                tv_numbers.setText(time_format);
////                sp_notify_clear_icon.setVisibility(View.VISIBLE);
////                sp_notify_dropdown_icon.setVisibility(View.GONE);
////                error_msg.setVisibility(View.VISIBLE);
////                error_msg.setText("This Field is Required");
//            }
//        }
//        // If it's the first time and tv_sp_minutes is empty, set the default value "minutes"
//        if (!is_notify_clicked) {
////            if (selectedValues.isEmpty()) {
//            tv_sp_minutes.setText(R.string.minutes);
//            tv_numbers.setText(R.string._10);
////            sp_notify_clear_icon.setVisibility(View.VISIBLE);
////            sp_notify_dropdown_icon.setVisibility(View.GONE);
////            }
////                if (isFirstTime && tv_sp_minutes.getText().toString().isEmpty()) {
////                    tv_sp_minutes.setText(R.string.minutes);
////                    tv_numbers.setText(R.string._10);
////                    sp_notify_clear_icon.setVisibility(View.VISIBLE);
////                    sp_notify_dropdown_icon.setVisibility(View.GONE);
//////                    is_timenotify = false;
//////                    is_notify = true;
////                    isFirstTime = false;  // Set the flag to false so this block won't execute again
////                }
////            } else {
////                is_notify = true;
////                tv_sp_minutes.setText(R.string.minutes);
////                tv_numbers.setText(R.string._10);
////                sp_notify_clear_icon.setVisibility(View.GONE);
////                sp_notify_dropdown_icon.setVisibility(View.VISIBLE);
////                is_timenotify = false;
////                error_msg1.setVisibility(View.VISIBLE);
////                error_msg1.setText("This Field is Required");
////                error_msg.setVisibility(View.VISIBLE);
////                error_msg.setText("This Field is Required");// Clear the text if it's still "Minutes"
////            }
//        }
//        final int position = ll_add_notification.getChildCount();
//// Set the position as a tag to the view
//        view_opponents.setTag(position);
//
//        final CommonSpinnerAdapter spinner_adapter = new CommonSpinnerAdapter((Activity) getContext(), minutes_list);
//        sp_minutes.setAdapter(spinner_adapter);
//        //..
//        AndroidUtils.LoadList(sp_minutes, getContext(), minutes_list.size(), true);
//        //..
//        sp_minutes.setOnItemClickListener((parent, view, i, id) -> {
//            selected_hour_type = minutes_list.get(i).getName();
//            int viewPosition = (int) view_opponents.getTag(); // Get the position from the view tag
//
//            // Retrieve the current `tv_number` value
////                String currentNumber = Objects.requireNonNull(tv_numbers.getText()).toString();
//            String time_value = "";
//            int notify_number = 0;
//            if (!Objects.requireNonNull(tv_numbers.getText()).toString().isEmpty()) {
//                notify_number = Integer.parseInt(Objects.requireNonNull(tv_numbers.getText()).toString());
//            } else {
//                notify_number = 0;
//            }
//            if (tv_sp_minutes.getText().toString().equals("Minutes")) {
//                if ((notify_number) > 60) {
//                    tv_numbers.setText("60");
//                } else if (tv_numbers.getText().toString().isEmpty()) {
//                    time_value = "10";
//                }
//            } else if (tv_sp_minutes.getText().toString().equals("Hours")) {
////                        error_msg.setVisibility(View.VISIBLE);
////                        error_msg.setText("Must Between 1 to 24 Hours");
//                if ((notify_number) > 24) {
//                    tv_numbers.setText("24");
//                } else if (tv_numbers.getText().toString().isEmpty()) {
//                    time_value = "1";
//                }
//            } else if (tv_sp_minutes.getText().toString().equals("Days")) {
//                if ((notify_number) > 31) {
//                    tv_numbers.setText("31");
//                } else if (tv_numbers.getText().toString().isEmpty()) {
//                    time_value = "1";
//                }
//            } else if (tv_sp_minutes.getText().toString().equals("Weeks")) {
//                if ((notify_number) > 4) {
//                    tv_numbers.setText("4");
//                } else if (tv_numbers.getText().toString().isEmpty()) {
//                    time_value = "1";
//                }
//            }
//
//            // Update `selectedValues` with both `tv_number` and `sp_minutes`
//            if (viewPosition >= 0 && viewPosition < selectedValues.size()) {
//                if (tv_numbers.getText().toString().isEmpty()) {
//                    selectedValues.set(viewPosition, time_value + "-" + selected_hour_type.lowercase(Locale.ROOT));
//                } else {
//                    selectedValues.set(viewPosition, tv_numbers.getText().toString() + "-" + selected_hour_type.lowercase(Locale.ROOT));
//                }
//            }
//
//            // Update the spinner view display
//            sp_minutes.setVisibility(View.GONE);
//            tv_sp_minutes.setText(selected_hour_type);
////                AndroidUtils.DisplaySpinnerView(sp_minutes, tv_sp_minutes, selected_hour_type, sp_notify_dropdown_icon, sp_notify_clear_icon, true);
//            is_notify = true;
//
//            // Verify the update
//            Log.d("Updated selectedValues after sp_minutes selection", selectedValues.toString());
//        });
//
////        selected_notify_value = tv_sp_minutes.getText().toString();
//        if (tv_sp_minutes.getText().toString().isEmpty()) {
//            error_msg1.setVisibility(View.VISIBLE);
//            error_msg1.setText("This field is required");
//            is_timenotify = false;
//        }
//        tv_sp_minutes.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (count > 0) {
//                    error_msg1.setVisibility(View.GONE);
//                    is_timenotify = false;
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                is_timenotify = true;
//                int notify_number;
//                if (!(Objects.requireNonNull(tv_numbers.getText()).toString().isEmpty())) {
//                    notify_number = Integer.parseInt(Objects.requireNonNull(tv_numbers.getText()).toString());
//                } else {
//                notify_number = 0;
//            }
//                if ((tv_sp_minutes.getText().toString().equals("Minutes")) && ((notify_number) > 60)) {
////                        error_msg.setVisibility(View.VISIBLE);
////                        error_msg.setText("Must Between 1 to 60 Minutes");
////                        tv_numbers.setText("60");
//                } else if ((tv_sp_minutes.getText().toString().equals("Hours")) && ((notify_number) > 24)) {
////                        error_msg.setVisibility(View.VISIBLE);
////                        error_msg.setText("Must Between 1 to 24 Hours");
//                    tv_numbers.setText("24");
//                } else if ((tv_sp_minutes.getText().toString().equals("Days")) && ((notify_number) > 31)) {
////                        error_msg.setVisibility(View.VISIBLE);
////                        error_msg.setText("Must Between 1 to 31 Days");
//                    tv_numbers.setText("31");
//                } else if ((tv_sp_minutes.getText().toString().equals("Weeks")) && ((notify_number) > 4)) {
////                        error_msg.setVisibility(View.VISIBLE);
////                        error_msg.setText("Must Between 1 to 4 Weeks");
//                    tv_numbers.setText("4");
//                } else {
//                    error_msg.setVisibility(View.GONE);
//                    is_timenotify = true;
//                }
////                } else {
////                    is_timenotify = false;
////                    error_msg.setVisibility(View.VISIBLE);
////                    error_msg.setText("This Field is Required");
////                }
//            }
//        });
//        // Add a flag to track if it's the first interaction
//// Inside the onClickListener for tv_notify_layout (which handles the spinner dropdown visibility)
//        tv_notify_layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Now show the spinner as usual
//                AndroidUtils.display_listview(is_notify, sp_minutes);
//                is_notify = !is_notify;
//            }
//        });
//
////        sp_notify_clear_icon.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                AndroidUtils.DisplaySpinnerView(sp_minutes, tv_sp_minutes, selected_hour_type, sp_notify_dropdown_icon, sp_notify_clear_icon, false);
////                if (position >= 0 && position < selectedValues.size()) {
////                    selectedValues.set(position, Objects.requireNonNull(tv_numbers.getText()) + "-" + "");
////                }
////                error_msg1.setVisibility(View.VISIBLE);
////                error_msg1.setText("This field is required");
////                is_timenotify = false;
////                is_notify = true;
////            }
////        });
//        tv_numbers.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                // Do nothing
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                // Do nothing
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                //..
////                is_timenotify = false;
//                String time_value = "";
//                int notify_number = 0;
//                if (!Objects.requireNonNull(tv_numbers.getText()).toString().isEmpty()) {
//                    notify_number = Integer.parseInt(Objects.requireNonNull(tv_numbers.getText()).toString());
//                } else {
//                    notify_number = 0;
//                }
//                if (tv_sp_minutes.getText().toString().equals("Minutes")) {
//                    if ((notify_number) > 60) {
//                        tv_numbers.setText("60");
//                    } else if (tv_numbers.getText().toString().isEmpty()) {
//                        time_value = "10";
//                    }
//                } else if (tv_sp_minutes.getText().toString().equals("Hours")) {
////                        error_msg.setVisibility(View.VISIBLE);
////                        error_msg.setText("Must Between 1 to 24 Hours");
//                    if ((notify_number) > 24) {
//                        tv_numbers.setText("24");
//                    } else if (tv_numbers.getText().toString().isEmpty()) {
//                        time_value = "1";
//                    }
//                } else if (tv_sp_minutes.getText().toString().equals("Days")) {
//                    if ((notify_number) > 31) {
//                        tv_numbers.setText("31");
//                    } else if (tv_numbers.getText().toString().isEmpty()) {
//                        time_value = "1";
//                    }
//                } else if (tv_sp_minutes.getText().toString().equals("Weeks")) {
//                    if ((notify_number) > 4) {
//                        tv_numbers.setText("4");
//                    } else if (tv_numbers.getText().toString().isEmpty()) {
//                        time_value = "1";
//                    }
//                } else {
////                    error_msg.setVisibility(View.GONE);
////                    is_timenotify = true;
//                }
////                } else {
////                    is_timenotify = false;
//////                    error_msg.setVisibility(View.VISIBLE);
//////                    error_msg.setText("This Field is Required");
////                }
//                //..
//                int viewPosition = (int) view_opponents.getTag(); // Get the correct position
//                String selectedMinutes = tv_sp_minutes.getText().toString().lowercase(Locale.ROOT);
//
//                // Combine and update `tv_number` and `sp_minutes` in `selectedValues`
//                if (viewPosition >= 0 && viewPosition < selectedValues.size()) {
//                    if (tv_numbers.getText().toString().isEmpty()) {
//                        selectedValues.set(viewPosition, time_value + "-" + selectedMinutes);
//                    } else {
//                        selectedValues.set(viewPosition, editable.toString() + "-" + selectedMinutes);
//                    }
//                }
//
//                // Verify the update
//                Log.d("Updated selectedValues after tv_numbers input", selectedValues.toString());
//            }
//        });
//
//        iv_delete_notification.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Retrieve the position from the view's tag
//                int position = (int) view_opponents.getTag();
//
//                // Remove the view from the LinearLayout
//                ll_add_notification.removeView(view_opponents);
//
//                // Remove the corresponding item from selectedValues
//                if (position >= 0 && position < selectedValues.size()) {
//                    // Remove the exact value in selectedValues that corresponds to this view
//                    String removedValue = selectedValues.get(position);
//                    selectedValues.remove(removedValue);  // Remove the actual item, not by position
//
//                    // Optional: Use position-based removal if above line doesn’t work
//                    // selectedValues.remove(position);
//                }
//
//                // Reassign tags to reflect updated positions
//                for (int i = 0; i < ll_add_notification.getChildCount(); i++) {
//                    View childView = ll_add_notification.getChildAt(i);
//                    childView.setTag(i); // Update tag with the new position
//                }
//
//                // Log or display the updated selectedValues list for debugging
//                Log.d("Updated selectedValues", selectedValues.toString());
//            }
//        });
//
//// Add the new view to the LinearLayout and update selectedValues
//        ll_add_notification.addView(view_opponents);
//
//// Add new entry to selectedValues only if it's a new notification
//        if (!is_notify_clicked) {
//            String time_value = "";
//            int notify_number = 0;
//            if (!Objects.requireNonNull(tv_numbers.getText()).toString().isEmpty()) {
//                notify_number = Integer.parseInt(Objects.requireNonNull(tv_numbers.getText()).toString());
//            } else {
//                notify_number = 0;
//            }
//            if (tv_sp_minutes.getText().toString().equals("Minutes")) {
//                if ((notify_number) > 60) {
//                    tv_numbers.setText("60");
//                } else if (tv_numbers.getText().toString().isEmpty()) {
//                    time_value = "10";
//                }
//            } else if (tv_sp_minutes.getText().toString().equals("Hours")) {
////                        error_msg.setVisibility(View.VISIBLE);
////                        error_msg.setText("Must Between 1 to 24 Hours");
//                if ((notify_number) > 24) {
//                    tv_numbers.setText("24");
//                } else if (tv_numbers.getText().toString().isEmpty()) {
//                    time_value = "1";
//                }
//            } else if (tv_sp_minutes.getText().toString().equals("Days")) {
//                if ((notify_number) > 31) {
//                    tv_numbers.setText("31");
//                } else if (tv_numbers.getText().toString().isEmpty()) {
//                    time_value = "1";
//                }
//            } else if (tv_sp_minutes.getText().toString().equals("Weeks")) {
//                if ((notify_number) > 4) {
//                    tv_numbers.setText("4");
//                } else if (tv_numbers.getText().toString().isEmpty()) {
//                    time_value = "1";
//                }
//            }
//            if (tv_numbers.getText().toString().isEmpty()) {
//                selectedValues.add(time_value + "-" + tv_sp_minutes.getText().toString().lowercase(Locale.ROOT));
//            } else {
//                selectedValues.add(tv_numbers.getText().toString() + "-" + tv_sp_minutes.getText().toString().lowercase(Locale.ROOT));
//            }
//        }
//    }

//            }
//
//            String newValue = Objects.requireNonNull(tv_numbers.getText().toString()) + "-" + tv_sp_minutes.getText().toString().lowercase(Locale.ROOT);
//            selectedValues.add(newValue);
//
//            if (tv_numbers.getText().toString().isEmpty()) {
//                selectedValues.add(time_value + "-" + tv_sp_minutes.getText().toString().lowercase(Locale.ROOT));
//            } else {
//                selectedValues.add(tv_numbers.getText().toString() + "-" + tv_sp_minutes.getText().toString().lowercase(Locale.ROOT));
//            }
//            // Set the exact value as a tag to help with direct matching
//            view_opponents.setTag(selectedValues.size() - 1);
//        }
//    }

    private fun NotificationPopup() {
        val view_opponents = LayoutInflater.from(requireContext()).inflate(R.layout.add_calendar_notification, null)
        val tv_notify_layout = view_opponents.findViewById<LinearLayout>(R.id.tv_notify_layout)
        val tv_sp_minutes = tv_notify_layout.findViewById<TextView>(R.id.tv_spinner_view)
        val error_msg = view_opponents.findViewById<TextView>(R.id.error_msg)
        val error_msg1 = view_opponents.findViewById<TextView>(R.id.error_msg1)
        val tv_numbers = view_opponents.findViewById<TextInputEditText>(R.id.tv_numbers)
        val sp_minutes = view_opponents.findViewById<ListView>(R.id.sp_minutes)
        val iv_delete_notification = view_opponents.findViewById<ImageView>(R.id.iv_delete_notification)

        tv_numbers.inputType = InputType.TYPE_CLASS_NUMBER
        error_msg.setTextColor(requireContext().resources.getColor(R.color.Red))
        error_msg.textSize = 12f
        error_msg1.setTextColor(requireContext().resources.getColor(R.color.Red))
        error_msg1.textSize = 12f
        error_msg1.visibility = View.GONE
        sp_minutes.visibility = View.GONE

        if (isAllDay) {
            minutes_list.clear()
            minutes_list.add(MinutesDO("Days"))
            minutes_list.add(MinutesDO("Weeks"))
            AndroidUtils.LoadList(sp_minutes, requireContext(), minutes_list.size, true)
        } else {
            minutes_list.clear()
            minutes_list.add(MinutesDO("Minutes"))
            minutes_list.add(MinutesDO("Hours"))
            minutes_list.add(MinutesDO("Days"))
            minutes_list.add(MinutesDO("Weeks"))
            AndroidUtils.LoadList(sp_minutes, requireContext(), minutes_list.size, true)
        }

        if (selectedValues.isNotEmpty() && Constants.is_meeting == "Edit") {
            tv_sp_minutes.setText(AndroidUtils.CapitalizeFirstLetter(time_value))
            tv_numbers.setText(time_format)
        }

        if (!is_notify_clicked) {
            tv_sp_minutes.setText(minutes_list[0].name)
            tv_numbers.setText(R.string._10)
        }

        val position = ll_add_notification.childCount
        view_opponents.tag = position

        val spinner_adapter = CommonSpinnerAdapter(context as Activity, minutes_list)
        sp_minutes.adapter = spinner_adapter
        AndroidUtils.LoadList(sp_minutes, requireContext(), minutes_list.size, true)

        tv_notify_layout.setOnClickListener {
            AndroidUtils.display_listview(is_notify, sp_minutes)
            is_notify = !is_notify
        }

        sp_minutes.setOnItemClickListener { _, view, i, _ ->
            selected_hour_type = minutes_list[i].name
            val viewPosition = view_opponents.tag as Int

            var time_value = ""
            tv_sp_minutes.setText(selected_hour_type)
            if (tv_numbers.text?.toString()?.isNotEmpty() == true) {
                var notify_number = getSafeInt(tv_numbers.text.toString())
                notify_number = enforceMaxLimit(tv_sp_minutes.text.toString(), notify_number)
                tv_numbers.setText(notify_number.toString())
            } else {
                tv_numbers.setText("")
            }

            if (viewPosition in 0 until selectedValues.size) {
                time_value = if (tv_numbers.text.toString().isEmpty()) getDefaultTimeValue(tv_sp_minutes.text.toString()) else tv_numbers.text.toString()
                selectedValues[viewPosition] = "$time_value-${selected_hour_type.lowercase(Locale.ROOT)}"
            }

            sp_minutes.visibility = View.GONE
            is_notify = true
            Log.d("Updated selectedValues after sp_minutes selection", selectedValues.toString())
        }

//        tv_sp_minutes.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                if (count > 0) {
//                    error_msg1.visibility = View.GONE
//                    is_timenotify = false
//                }
//            }
//
//            override fun afterTextChanged(s: Editable) {
//                is_timenotify = true
//                if (tv_numbers.text?.toString()?.isNotEmpty() == true) {
//                    var notify_number = getSafeInt(tv_numbers.text.toString())
//                    notify_number = enforceMaxLimit(tv_sp_minutes.text.toString(), notify_number)
//                    tv_numbers.setText(notify_number.toString())
//                } else {
//                    tv_numbers.setText("")
//                }
//                error_msg.visibility = View.GONE
//            }
//        })


        tv_numbers.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                val currentInput = editable.toString().trim()
                val notify_number = getSafeInt(currentInput)
                val enforcedValue = enforceMaxLimit(tv_sp_minutes.text.toString(), notify_number)
                val enforcedStr = enforcedValue.toString()

                // Always show "0" if input is invalid or empty
                if (currentInput != enforcedStr) {
                    tv_numbers.removeTextChangedListener(this) // avoid infinite loop
                    if (enforcedStr == "0") {
                        tv_numbers.setText("")
                    } else {
                        tv_numbers.setText(enforcedStr)
                    }
//                    tv_numbers.setSelection(enforcedStr.length)
                    tv_numbers.addTextChangedListener(this)
//                return
                }

                val viewPosition = view_opponents.tag as Int
                val selectedMinutes = tv_sp_minutes.text.toString().lowercase(Locale.ROOT)
                val value = if (currentInput.isEmpty())
                    getDefaultTimeValue(tv_sp_minutes.text.toString())
                else
                    enforcedStr

                if (viewPosition in 0 until selectedValues.size) {
                    selectedValues[viewPosition] = "$value-$selectedMinutes"
                }

                Log.d("Updated selectedValues after tv_numbers input", selectedValues.toString())
            }
        })


        iv_delete_notification.setOnClickListener {
            val pos = view_opponents.tag as Int
            ll_add_notification.removeView(view_opponents)

            if (pos in 0 until selectedValues.size) {
                val removedValue = selectedValues[pos]
                selectedValues.remove(removedValue)
            }

            for (i in 0 until ll_add_notification.childCount) {
                ll_add_notification.getChildAt(i).tag = i
            }

            Log.d("Updated selectedValues", selectedValues.toString())
        }

        ll_add_notification.addView(view_opponents)

        if (!is_notify_clicked) {
            var time_value = ""
            var notify_number = getSafeInt(tv_numbers.text.toString())
            notify_number = enforceMaxLimit(tv_sp_minutes.text.toString(), notify_number)
            tv_numbers.setText(notify_number.toString())
            time_value = if (tv_numbers.text.toString().isEmpty())
                getDefaultTimeValue(tv_sp_minutes.text.toString())
            else
                tv_numbers.text.toString()
            selectedValues.add("$time_value-${tv_sp_minutes.text.toString().lowercase(Locale.ROOT)}")
        }
    }

    // Helper: Parse integer safely
    private fun getSafeInt(value: String): Int {
        return try {
            Integer.parseInt(value)
        } catch (e: NumberFormatException) {
            0
        }
    }

    // Helper: Return max limit based on type
    private fun enforceMaxLimit(type: String, value: Int): Int {
        return when (type) {
            "Minutes" -> Math.min(value, 60)
            "Hours" -> Math.min(value, 24)
            "Days" -> Math.min(value, 31)
            "Weeks" -> Math.min(value, 4)
            else -> value
        }
    }

    // Helper: Get default value
    private fun getDefaultTimeValue(type: String): String {
        return when (type) {
            "Minutes" -> "10"
            "Hours", "Days", "Weeks" -> "1"
            else -> "0"
        }
    }

    private fun loadnewInput(tv_numbers: TextInputEditText, view: View, tv_sp_minutes: TextView) {
        val parentView = tv_numbers.parent as View
        val inputFieldValueTextView = parentView.findViewById<TextView>(R.id.tv_numbers)
        inputFieldValueTextView.setText(tv_numbers.text.toString())
        val position = findPositionInParent(view, ll_add_notification)
        if (position in 0 until selectedValues.size) {
            // Update the input field value in the ArrayList
            selectedValues[position] = "${tv_numbers.text}-${tv_sp_minutes.text.toString().lowercase(Locale.ROOT)}"
        }
    }

    private fun findPositionInParent(view: View, parent: ViewGroup): Int {
        val position = parent.indexOfChild(view)
        if (position >= 0) {
            return position
        } else {
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                if (child is ViewGroup) {
                    val childPosition = findPositionInParent(view, child)
                    if (childPosition >= 0) {
                        return childPosition // Return the childPosition if it's >= 0
                    }
                }
            }
        }
        return -1 // Return -1 if the view is not found in the parent or its children
    }

    fun selected_corporate(list_item: ArrayList<RelationshipsDO>) {
        // Create a list of existing IDs in the selected list for faster lookup
//        HashSet<String> selectedIds = new HashSet<>();
//        for (RelationshipsDO item : selected_entity_corp_client_list) {
//            selectedIds.add(item.getId());
//        }
//
//        for (RelationshipsDO teamModel : list_item) {
//            if (teamModel.isChecked()) {
//                // Add the item only if its ID is not already in the selected list
//                if (!selectedIds.contains(teamModel.getId())) {
//                    selected_entity_corp_client_list.add(teamModel);
//                    selectedIds.add(teamModel.getId());
//                }
//            } else {
//                // Remove the item if it is unchecked (by matching the ID)
//                for (int i = 0; i < selected_entity_corp_client_list.size(); i++) {
//                    if (selected_entity_corp_client_list.get(i).getId().equals(teamModel.getId())) {
//                        selected_entity_corp_client_list.remove(i);
//                        break;
//                    }
//                }
//            }
//        }

        // Update the UI based on the state of the selected list
        if (selected_entity_corp_client_list.isEmpty()) {
            ll_selected_corp_clients_view.visibility = View.GONE
            at_assigned_corp_client.setText("")
        } else {
            loadselectedCorpClients()
        }
//        loadSelectedCorpList();
//        }
        //.
//        selected_entity_client_list.clear();
//        for (int i = 0; i < list_item.size(); i++) {
//            RelationshipsDO teamModel = list_item.get(i);
//            if (teamModel.isChecked()) {
//                if (!selected_entity_corp_client_list.contains(teamModel)) {
//                    selected_entity_corp_client_list.add(teamModel);
//                }
//            } else if (!teamModel.isChecked()) {
//                selected_entity_corp_client_list.remove(teamModel);
//            }
//        }
//        if (selected_entity_corp_client_list.isEmpty()) {
//            ll_selected_corp_clients.setVisibility(View.GONE);
//            at_assigned_corp_client.setText("");
//        } else {
//            loadselectedCorpClients();
//        }
//        rv_clients_view.setVisibility(View.GONE);
//        is_clicked_clients = true;
        //.
    }

    private fun loadselectedCorpClients() {
        val value = arrayOfNulls<String>(selected_entity_corp_client_list.size)
        for (i in selected_entity_corp_client_list.indices) {
//                                value += "," + family_members.get(i);
//                               value.add(family_members.get(i));
            value[i] = selected_entity_corp_client_list[i].name
        }
        val str = value.joinToString(",")
        at_assigned_corp_client.setText(str)
        loadSelectedCorpList()
        ll_corp_client_team_members.removeAllViews()
        for (i in selected_entity_corp_client_list.indices) {
            val view_opponents = LayoutInflater.from(requireContext()).inflate(R.layout.edit_opponent_advocate, null)
            val tv_opponent_name = view_opponents.findViewById<TextView>(R.id.tv_opponent_name)
            tv_opponent_name.setText(selected_entity_corp_client_list[i].name)
            val iv_edit_opponent = view_opponents.findViewById<ImageView>(R.id.iv_edit_opponent)
            val iv_remove_opponent = view_opponents.findViewById<ImageView>(R.id.iv_remove_opponent)
            iv_remove_opponent.tag = i
            iv_remove_opponent.setOnClickListener { v ->
                try {
                    val position = v.tag as Int
                    ll_corp_client_team_members.removeViewAt(position)
//                            ll_selected_groups.addView(view_opponents,position);
                    val teamModel = selected_entity_corp_client_list.removeAt(position)
                    teamModel.isChecked = false
//                            selected_entity_client_list.remove(position);
                    //..
                    for (j in 0 until ll_corp_client_team_members.childCount) {
                        val iv_remove = ll_corp_client_team_members.getChildAt(j).findViewById<ImageView>(R.id.iv_remove_opponent)
                        if (iv_remove != null) {
                            iv_remove.tag = j
                        }
                    }

                    val str1 = value.joinToString(",")
                    at_assigned_corp_client.setText(str1)
                    val stringBuilder = java.lang.StringBuilder()
                    for (model in selected_entity_corp_client_list) {
                        stringBuilder.append(model.name).append(",")
                    }

                    if (stringBuilder.isNotEmpty()) {
                        stringBuilder.deleteCharAt(stringBuilder.length - 1)
                    }
                    val strn = stringBuilder.toString()
                    at_assigned_corp_client.setText(strn)
                    loadSelectedCorpList()
                    //..
//                            selected_groups_list.set(position, groupsModel);
                } catch (e: Exception) {
                    e.printStackTrace()
                    AndroidUtils.showAlert(e.message ?: "", requireActivity())
                }
            }
            iv_edit_opponent.visibility = View.GONE
            ll_corp_client_team_members.addView(view_opponents)
        }
    }

    private fun loadSelectedCorpList() {
        if (selected_entity_corp_client_list.isNotEmpty()) {
            ll_selected_corp_clients_view.visibility = View.VISIBLE
        } else {
            at_assigned_corp_client.setText("")
            ll_selected_corp_clients_view.visibility = View.GONE
        }
    }

    fun selected_individual(list_item: ArrayList<RelationshipsDO>) {
//        for (int i = 0; i < list_item.size(); i++) {
//            RelationshipsDO teamModel = list_item.get(i);
//            if (teamModel.isChecked()) {
//                if (!selected_individual_list.contains(teamModel)) {
//                    selected_individual_list.add(teamModel);
//                }
//            }
//        }
        if (selected_individual_list.isEmpty()) {
            ll_selected_individual.visibility = View.GONE
            at_individual.setText("")
        } else {
            loadSelectedIndividual()
        }
    }

    private fun load_individual_Popup() {
        try {
            if (individual_list.isNotEmpty()) {
//                is_clicked_individuals = true;
//                rv_individuals_view.setVisibility(View.GONE);
//                AndroidUtils.showToast("No Individuals to show", getContext());
//            } else {
                rv_individuals_view.visibility = View.VISIBLE
                for (i in individual_list.indices) {
                    val teamModel = individual_list[i]
                    teamModel.isChecked = false
                    for (j in selected_individual_list.indices) {
                        if (individual_list[i].id == selected_individual_list[j].id) {
                            teamModel.isChecked = true
//                        selected_groups_list.set(j,documentsModel);
                        }
                    }
                }
//                selected_individual_list.clear();
                val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                rv_individuals_view.layoutManager = layoutManager
                //rv_individuals_view.setHasFixedSize(true);
                ADAPTER_TAG = "INDIVIDUAL"
                val documentsAdapter = CommonRelationshipsAdapter(teamList, ADAPTER_TAG, individual_list, entity_client_list, entity_corp_client_list, documents_list, this)
                rv_individuals_view.adapter = documentsAdapter
                //..
                AndroidUtils.LoadList(rv_individuals_view, requireContext(), individual_list.size, true)
                //..
//                btn_individual.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                    }
//                });
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AndroidUtils.showAlert(e.message ?: "", requireActivity())
        }
    }

    private fun loadSelectedIndividual() {
        val value = arrayOfNulls<String>(selected_individual_list.size)
        for (i in selected_individual_list.indices) {
            value[i] = selected_individual_list[i].name
        }
        val str = value.joinToString(",")
        at_individual.setText(str)
//        ll_selected_individual.setVisibility(View.VISIBLE);
        ll_individual_list.removeAllViews()
        for (i in selected_individual_list.indices) {
            val view_opponents = LayoutInflater.from(requireContext()).inflate(R.layout.edit_opponent_advocate, null)
            val tv_opponent_name = view_opponents.findViewById<TextView>(R.id.tv_opponent_name)
            tv_opponent_name.setText(selected_individual_list[i].name)
            val iv_edit_opponent = view_opponents.findViewById<ImageView>(R.id.iv_edit_opponent)
            val iv_remove_opponent = view_opponents.findViewById<ImageView>(R.id.iv_remove_opponent)
            iv_remove_opponent.tag = i
            iv_remove_opponent.setOnClickListener { v ->
                try {
                    //..
                    val position = v.tag as Int
                    // Remove the view at the specified position
                    ll_individual_list.removeViewAt(position)
                    // Remove the corresponding item from the list
                    val clientsModel = selected_individual_list.removeAt(position)
                    clientsModel.isChecked = false
                    // Update the tags of the remaining views
                    for (j in 0 until ll_individual_list.childCount) {
                        val iv_remove = ll_individual_list.getChildAt(j).findViewById<ImageView>(R.id.iv_remove_opponent)
                        if (iv_remove != null) {
                            iv_remove.tag = j
                        }
                    }
                    val str1 = value.joinToString(",")
                    at_individual.setText(str1)
                    // Update at_add_tm text
                    val stringBuilder = java.lang.StringBuilder()
                    for (model in selected_individual_list) {
                        stringBuilder.append(model.name).append(",")
                    }
                    if (stringBuilder.isNotEmpty()) {
                        stringBuilder.deleteCharAt(stringBuilder.length - 1)
                    }
                    val strn = stringBuilder.toString()
                    at_individual.setText(strn)
                    if (selected_individual_list.isEmpty()) {
                        ll_selected_individual.visibility = View.GONE
                    } else {
//                            ll_selected_individual.setVisibility(View.VISIBLE);
                    }
                    //..
                } catch (e: Exception) {
                    e.printStackTrace()
                    AndroidUtils.showAlert(e.message ?: "", requireActivity())
                }
            }
            iv_edit_opponent.visibility = View.GONE
            ll_individual_list.addView(view_opponents)
        }
    }


    private fun callClientsWebservice() {
        try {
            entities_list.clear()
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val postdata = JSONObject()
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v2/relationship/client/list", "Client List", postdata.toString())
        } catch (e: Exception) {
            AndroidUtils.showToast(e.message ?: "", requireContext())
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.printStackTrace()
        }
    }

    fun selected_Team(list_item: ArrayList<TeamDo>) {
//    relationshipsAdapter.setSelected_unsharedocsList(sharedList);
        //clear existing chosen Team members and re-added in a list.
//        selected_tm_list.clear();
//        for (int i = 0; i < list_item.size(); i++) {
//            TeamDo teamModel = list_item.get(i);
////        SharedDocumentsDo groupModel = list_item.get(i);
//            if (teamModel.isChecked()) {
//                if (!selected_tm_list.contains(teamModel)) {
//                    selected_tm_list.add(teamModel);
//                }
//            }
//        }
        for (i in selected_tm_list.indices) {
            Log.d("List_Item", selected_tm_list[i].name ?: "")
        }
        if (selected_tm_list.isEmpty()) {
            ll_selected_teammembers.visibility = View.GONE
            at_add_tm.setText("")
        } else {
            loadSelectedTM()
        }
//        rv_groups_view.setVisibility(View.GONE);
//        is_clicked_team = true;
    }

    private fun TeamMembersPopup() {
        try {
            if (teamList.isEmpty()) {
                for (i in matterList.indices) {
                    if (matter_id == matterList[i].id) {
                        val team_members = matterList[i].members
                        for (j in 0 until team_members.length()) {
                            val jsonObject = team_members.getJSONObject(j)
                            val memberId = jsonObject.getString("id")!!

                            // Check if already exists
                            var alreadyExists = false
                            for (existing in teamList) {
                                if (existing.id == memberId) {
                                    alreadyExists = true
                                    break
                                }
                            }

                            if (!alreadyExists) {
                                val teamModel = TeamDo()
                                teamModel.id = memberId
                                teamModel.name = jsonObject.getString("name")!!
                                teamList.add(teamModel)
                            }
                        }
                    }
                }
            }

            if (teamList.isEmpty()) {
                ll_add_tm.visibility = View.GONE
            } else {
                if ("solo" != Constants.CATEGORY) {
                    ll_add_tm.visibility = View.VISIBLE
                }
            }
            for (i in teamList.indices) {
                val teamModel = teamList[i]
                teamModel.isChecked = false
                for (j in selected_tm_list.indices) {
                    if (teamList[i].id == selected_tm_list[j].id) {
                        teamModel.isChecked = true
//                        selected_groups_list.set(j,documentsModel);
                    }
                }
            }
            if (teamList.isNotEmpty()) {
//                rv_groups_view.setVisibility(View.GONE);
//                AndroidUtils.showToast("No Team Members to View", getContext());
//                is_clicked_team = true;
//            } else {
                val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                rv_groups_view.layoutManager = layoutManager
                // rv_groups_view.setHasFixedSize(true);
                ADAPTER_TAG = "TM"

                val documentsAdapter = CommonRelationshipsAdapter(teamList, ADAPTER_TAG, individual_list, entity_client_list, entity_corp_client_list, documents_list, this)
                rv_groups_view.adapter = documentsAdapter
                //..
                AndroidUtils.LoadList(rv_groups_view, requireContext(), teamList.size, true)
                //..
//                btn_add_tm.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                        for (int i = 0; i < selected_tm_list.size(); i++) {
//                            Log.d("List_Item", selected_tm_list.get(i).getName());
//                        }
//                        if (selected_tm_list.isEmpty()) {
//                            selected_groups.setVisibility(View.GONE);
//                            at_add_tm.setText("");
//                        } else {
//                            loadSelectedTM();
//                        }
//                        rv_groups_view.setVisibility(View.GONE);
//                        is_clicked_team = true;
//                    }
//                });
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AndroidUtils.showAlert(e.message ?: "", requireActivity())
        }
    }

    private fun loadSelectedClients() {
        val value = arrayOfNulls<String>(selected_entity_client_list.size)
        for (i in selected_entity_client_list.indices) {
//                                value += "," + family_members.get(i);
//                               value.add(family_members.get(i));
            value[i] = selected_entity_client_list[i].name
        }
        val str = value.joinToString(",")
        at_assigned_client.setText(str)
        if (selected_entity_client_list.isNotEmpty()) {
            ll_selected_entity_client.visibility = View.VISIBLE
        } else {
            ll_selected_entity_client.visibility = View.GONE
        }
        ll_client_team_members.removeAllViews()
        for (i in selected_entity_client_list.indices) {
            val view_opponents = LayoutInflater.from(requireContext()).inflate(R.layout.edit_opponent_advocate, null)
            val tv_opponent_name = view_opponents.findViewById<TextView>(R.id.tv_opponent_name)
            tv_opponent_name.setText(selected_entity_client_list[i].name)
            val iv_edit_opponent = view_opponents.findViewById<ImageView>(R.id.iv_edit_opponent)
            val iv_remove_opponent = view_opponents.findViewById<ImageView>(R.id.iv_remove_opponent)
            iv_remove_opponent.tag = i
            iv_remove_opponent.setOnClickListener { v ->
                try {
                    val position = v.tag as Int
                    ll_client_team_members.removeViewAt(position)
//                            ll_selected_groups.addView(view_opponents,position);
                    val teamModel = selected_entity_client_list.removeAt(position)
                    teamModel.isChecked = false
//                            selected_entity_client_list.remove(position);
                    //..
                    for (j in 0 until ll_client_team_members.childCount) {
                        val iv_remove = ll_client_team_members.getChildAt(j).findViewById<ImageView>(R.id.iv_remove_opponent)
                        if (iv_remove != null) {
                            iv_remove.tag = j
                        }
                    }

                    val str1 = value.joinToString(",")
                    at_assigned_client.setText(str1)
                    val stringBuilder = java.lang.StringBuilder()
                    for (model in selected_entity_client_list) {
                        stringBuilder.append(model.name).append(",")
                    }

                    if (stringBuilder.isNotEmpty()) {
                        stringBuilder.deleteCharAt(stringBuilder.length - 1)
                    }
                    val strn = stringBuilder.toString()
                    at_assigned_client.setText(strn)
                    if (selected_entity_client_list.isNotEmpty()) {
                        ll_selected_entity_client.visibility = View.VISIBLE
                    } else {
                        ll_selected_entity_client.visibility = View.GONE
                    }
                    //..
//                            selected_groups_list.set(position, groupsModel);
                } catch (e: Exception) {
                    e.printStackTrace()
                    AndroidUtils.showAlert(e.message ?: "", requireActivity())
                }
            }
            iv_edit_opponent.visibility = View.GONE
            ll_client_team_members.addView(view_opponents)
        }
    }

    private fun loadSelectedTM() {
        val value = arrayOfNulls<String>(selected_tm_list.size)
        for (i in selected_tm_list.indices) {
            value[i] = selected_tm_list[i].name

        }
        for (i in selected_tm_list.indices) {
            Log.d("List_Item_tm", selected_tm_list[i].name.toString())
        }
        val str = value.joinToString(",")
        at_add_tm.setText(str)
//        ll_selected_teammembers.setVisibility(View.VISIBLE);
        ll_selected_team_members.removeAllViews()
        for (i in selected_tm_list.indices) {
            val view_opponents = LayoutInflater.from(requireContext()).inflate(R.layout.edit_opponent_advocate, null)
            val tv_opponent_name = view_opponents.findViewById<TextView>(R.id.tv_opponent_name)
            tv_opponent_name.setText(selected_tm_list[i].name)
            val iv_edit_opponent = view_opponents.findViewById<ImageView>(R.id.iv_edit_opponent)
            val iv_remove_opponent = view_opponents.findViewById<ImageView>(R.id.iv_remove_opponent)
            iv_remove_opponent.tag = i
//            iv_remove_opponent.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    try {
//                        int position = (Integer) v.getTag();
//
//                        // Remove the item from the list and update its state
//                        TeamDo teamModel = selected_tm_list.get(position);
//                        teamModel.isChecked = false;
//                        selected_tm_list.remove(position);
//
//                        // Remove the view from the layout
//                        ll_selected_team_members.removeViewAt(position);
//
//                        // Update the tags for the remaining items
//                        for (int j = 0; j < ll_selected_team_members.getChildCount(); j++) {
//                            ImageView iv_remove = ll_selected_team_members.getChildAt(j).findViewById(R.id.iv_remove_opponent)!!;
//                            if (iv_remove != null) {
//                                iv_remove.setTag(j);
//                            }
//                        }
//
//                        // Update the text and visibility
//                        StringBuilder stringBuilder = new StringBuilder();
//                        for (TeamDo model : selected_tm_list) {
//                            stringBuilder.append(model.getName()).append(",");
//                        }
//                        if (stringBuilder.length() > 0) {
//                            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
//                        }
//                        at_add_tm.setText(stringBuilder.toString());
//
//                        ll_selected_teammembers.setVisibility(selected_tm_list.isEmpty() ? View.GONE : View.VISIBLE);
//
//                        // Refresh RecyclerView or update your adapter
//                        if (rv_groups_view.getAdapter() instanceof CommonRelationshipsAdapter) {
//                            CommonRelationshipsAdapter adapter = (CommonRelationshipsAdapter) rv_groups_view.getAdapter();
//                            adapter.notifyDataSetChanged(); // Notify adapter of data changes
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        AndroidUtils.showAlert(e.getMessage(), getActivity());
//                    }
//                }
//            });
            iv_remove_opponent.setOnClickListener { v ->
                try {
                    val position = v.tag as Int

                    // Get the team member being removed
                    val teamModel = selected_tm_list[position]

                    // Find the same team member in tmList and update its checked state
                    for (model in teamList) {
                        if (model.id == teamModel.id) {
                            model.isChecked = false
                            break
                        }
                    }

                    // Remove the item from selected_tm_list
                    selected_tm_list.removeAt(position)

                    // Remove the view from the layout
                    ll_selected_team_members.removeViewAt(position)

                    // Update the tags for the remaining items
                    for (j in 0 until ll_selected_team_members.childCount) {
                        val iv_remove = ll_selected_team_members.getChildAt(j).findViewById<ImageView>(R.id.iv_remove_opponent)
                        if (iv_remove != null) {
                            iv_remove.tag = j
                        }
                    }

                    // Update the text and visibility
                    val stringBuilder = java.lang.StringBuilder()
                    for (model in selected_tm_list) {
                        stringBuilder.append(model.name).append(",")
                    }
                    if (stringBuilder.isNotEmpty()) {
                        stringBuilder.deleteCharAt(stringBuilder.length - 1)
                    }
                    at_add_tm.setText(stringBuilder.toString())

                    ll_selected_teammembers.visibility = if (selected_tm_list.isEmpty()) View.GONE else View.VISIBLE

                    // Notify RecyclerView Adapter about the change
                    if (rv_groups_view.adapter is CommonRelationshipsAdapter) {
                        val adapter = rv_groups_view.adapter as CommonRelationshipsAdapter
                        adapter.notifyDataSetChanged() // Notify adapter of data changes
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    AndroidUtils.showAlert(e.message ?: "", requireActivity())
                }
            }

            iv_edit_opponent.visibility = View.GONE
            ll_selected_team_members.addView(view_opponents)
        }
    }

    private fun loadProjectData_edit(selected_project: String) {
        when (selected_project) {
            "legal" -> {
                callProjectWebservice(matter_legal)
                callProjectWebservice(matter_legal)
                loadRepetetions()
                try {
                    loadEntitiesSpinnerData()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                legalTaksList.add(TaskDo("Case Filling"))
                legalTaksList.add(TaskDo("Consultation"))
                legalTaksList.add(TaskDo("Creating Legal Breifs"))
                legalTaksList.add(TaskDo("Meeting with client"))
                legalTaksList.add(TaskDo("Hearing"))
                loadTaskList(legalTaksList)
            }
            "general" -> {
                legalTaksList.clear()
                loadRepetetions()
                callProjectWebservice(matter_legal)
                try {
                    loadEntitiesSpinnerData()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
//                TaskDo consultation_general = new TaskDo("Consultation");
//                TaskDo draft_agreements = new TaskDo("Draft agreements");
//                TaskDo fwa = new TaskDo("Filling with authorities");
//                TaskDo mwc_general = new TaskDo("Meeting with client");
//                TaskDo paf = new TaskDo("Prepare annual fillings");
                legalTaksList.add(TaskDo("Consultation"))
                legalTaksList.add(TaskDo("Draft agreements"))
                legalTaksList.add(TaskDo("Filling with authorities"))
                legalTaksList.add(TaskDo("Meeting with client"))
                legalTaksList.add(TaskDo("Prepare annual fillings"))
                loadTaskList(legalTaksList)
            }
            "overhead" -> {
                legalTaksList.clear()
                loadRepetetions()
                if (Constants.ROLE != "AAM")
                    callClientsWebservice()
//                matter_legal = ""
//                TaskDo conference = new TaskDo("Conference");
//                TaskDo holidays = new TaskDo("Holidays");
//                TaskDo research = new TaskDo("Research");
//                TaskDo training = new TaskDo("Training");
//                TaskDo vacation = new TaskDo("Vacation");
                legalTaksList.add(TaskDo("Conference"))
                legalTaksList.add(TaskDo("Holidays"))
                legalTaksList.add(TaskDo("Research"))
                legalTaksList.add(TaskDo("Training"))
                legalTaksList.add(TaskDo("Vacation"))
                loadTaskList(legalTaksList)
            }
            "others" -> {
                if (Constants.ROLE != "AAM")
                    callClientsWebservice()
                loadRepetetions()
                legalTaksList.clear()
                legalTaksList.add(TaskDo("Business Development"))
                legalTaksList.add(TaskDo("Personal"))
                legalTaksList.add(TaskDo("Doctor Appointment"))
                legalTaksList.add(TaskDo("Lunch/Dinner"))
                legalTaksList.add(TaskDo("Misc"))
                loadTaskList(legalTaksList)
            }
            "reminders" -> {
                legalTaksList.clear()
                hideAlldetails()
                if (Constants.ROLE != "AAM")
                    callClientsWebservice()
                loadRepetetions()
            }
        }
    }

    private fun loadProjectData(selected_project: String) {
        when (selected_project) {
            "Legal Matter" -> {
                loadClearedLists()
                unHideMatterDetails()
                matter_legal = "legal"
                callProjectWebservice(matter_legal)
//                hide_documents();
//                callClientsWebservice();
//                TaskDo caseFilling = new TaskDo("Case Filling");
//                TaskDo consultation = new TaskDo("Consultation");
//                TaskDo clb = new TaskDo("Creating Legal Breifs");
//                TaskDo mwc = new TaskDo("Meeting with client");
//                TaskDo hearing = new TaskDo("Hearing");
                legalTaksList.add(TaskDo("Case Filling"))
                legalTaksList.add(TaskDo("Consultation"))
                legalTaksList.add(TaskDo("Creating Legal Briefs"))
                legalTaksList.add(TaskDo("Meeting with client"))
                legalTaksList.add(TaskDo("Hearing"))
                loadTaskList(legalTaksList)
            }
            "General Matter" -> {
                legalTaksList.clear()
                unHideMatterDetails()
                loadClearedLists()
                matter_legal = "general"
                callProjectWebservice(matter_legal)
//                hide_documents();
//                TaskDo consultation_general = new TaskDo("Consultation");
//                TaskDo draft_agreements = new TaskDo("Draft agreements");
//                TaskDo fwa = new TaskDo("Filling with authorities");
//                TaskDo mwc_general = new TaskDo("Meeting with client");
//                TaskDo paf = new TaskDo("Prepare annual fillings");
                legalTaksList.add(TaskDo("Consultation"))
                legalTaksList.add(TaskDo("Draft agreements"))
                legalTaksList.add(TaskDo("Filling with authorities"))
                legalTaksList.add(TaskDo("Meeting with client"))
                legalTaksList.add(TaskDo("Prepare annual fillings"))
                loadTaskList(legalTaksList)
            }
            "Overhead" -> {
                legalTaksList.clear()
                hideMatterDetails()
                loadClearedLists()
                callTeamMemberWebservice()
                matter_legal = "overhead"
//                hide_documents();
//                matter_legal = ""
//                TaskDo conference = new TaskDo("Conference");
//                TaskDo holidays = new TaskDo("Holidays");
//                TaskDo research = new TaskDo("Research");
//                TaskDo training = new TaskDo("Training");
//                TaskDo vacation = new TaskDo("Vacation");
                legalTaksList.add(TaskDo("Conference"))
                legalTaksList.add(TaskDo("Holidays"))
                legalTaksList.add(TaskDo("Research"))
                legalTaksList.add(TaskDo("Training"))
                legalTaksList.add(TaskDo("Vacation"))
                loadTaskList(legalTaksList)
                HideClientView()
                load_member_view()
//                ll_add_to_timesheet.setVisibility(VISIBLE);
            }
            "Others" -> {
                legalTaksList.clear()
                hideMatterDetails()
                loadClearedLists()
                callTeamMemberWebservice()
//                display_check_list();
                matter_legal = "others"
//                hide_documents();
//                TaskDo business_development = new TaskDo("Business Development");
//                TaskDo personal = new TaskDo("Personal");
//                TaskDo doctor_appointment = new TaskDo("Doctor Appointment");
//                TaskDo lunch_dinner = new TaskDo("Lunch/Dinner");
//                TaskDo misc = new TaskDo("Misc");
                legalTaksList.add(TaskDo("Business Development"))
                legalTaksList.add(TaskDo("Personal"))
                legalTaksList.add(TaskDo("Doctor Appointment"))
                legalTaksList.add(TaskDo("Lunch/Dinner"))
                legalTaksList.add(TaskDo("Misc"))
                loadTaskList(legalTaksList)
                HideClientView()
                load_member_view()
//                ll_add_to_timesheet.setVisibility(VISIBLE);
            }
            "Reminders" -> {
                ll_add_to_timesheet.visibility = View.GONE
                legalTaksList.clear()
                hideAlldetails()
                loadClearedLists()
                callTeamMemberWebservice()
//                display_check_list();
                matter_legal = "reminders"
                HideClientView()
                load_member_view()
            }
        }
    }

//    private void checkAAM() {
//        if (matter_legal.equals("overhead") || matter_legal.equals("others") || (matter_legal.equals("reminders"))) {
//            HideClientView();
//        }
//    }

    private fun HideClientView() {
        if (Constants.ROLE == "AAM") {
            ll_add_corp_clients.visibility = View.GONE
            ll_add_entities.visibility = View.GONE
            ll_individual.visibility = View.GONE
        }
        display_members()
    }

    private fun loadClearedLists() {
        legalTaksList.clear()
        matterList.clear()
        entity_client_list.clear()
        individual_list.clear()
        documents_list.clear()
        entities_list.clear()
        Corp_client_list.clear()
        Corp_Team_members_list.clear()
        selected_tm_list.clear()
        corp_client_id = ""
        entity_id = ""
        teamList.clear()
        entity_corp_client_list.clear()
        //
        img_clear_sp_entity.visibility = View.GONE
        img_dropdown_sp_entity.visibility = View.VISIBLE
        img_clear_sp_task.visibility = View.GONE
        img_dropdown_sp_task.visibility = View.VISIBLE
        if (::corpAdapter.isInitialized) {
            img_clear_sp_corp_clients.performClick()
        }
        selected_entity_corp_client_list.clear()
        ll_corp_clients.visibility = View.GONE
        ll_assign_clients.visibility = View.GONE
//        ll_client_team_members.setVisibility(View.GONE);
        ll_selected_teammembers.visibility = View.GONE
        ll_selected_corp_clients_view.visibility = View.GONE
        ll_selected_individual.visibility = View.GONE
//        at_assigned_corp_client.setText("");
//        ll_selected_entity_client.setVisibility(View.GONE);
        at_assigned_client.setText("")
    }

    private fun load_member_view() {
        cv_add_clients.visibility = View.VISIBLE
        if ("solo" != Constants.CATEGORY) {
            ll_add_tm.visibility = View.VISIBLE
        }
        ll_add_entities.visibility = View.VISIBLE
        ll_individual.visibility = View.VISIBLE
        ll_documents_view.visibility = View.GONE
    }

    private fun load_clear_list() {
        at_add_tm.setText("")
        at_assigned_client.setText("")
        at_individual.setText("")
        at_attach_document.setText("")
        tv_sp_entity.setText("")
        at_assigned_corp_client.setText("")

//        Corp_client_list.clear();
//        entity_client_list.clear();
//        individual_list.clear();
//        documents_list.clear();
//        entities_list.clear();
//        teamList.clear();
//
//        selected_documents_list.clear();
//        selected_entity_client_list.clear();
//        selected_individual_list.clear();
//        selected_entity_corp_client_list.clear();
//        selected_tm_list.clear();

//        legalTaksList.clear();
//        matterList.clear();
        entity_client_list.clear()
        individual_list.clear()
        documents_list.clear()
        entities_list.clear()
        Corp_client_list.clear()
        Corp_Team_members_list.clear()
        selected_tm_list.clear()
        corp_client_id = ""
        entity_id = ""
        teamList.clear()
        entity_corp_client_list.clear()
        //
        img_clear_sp_entity.visibility = View.GONE
        img_dropdown_sp_entity.visibility = View.VISIBLE
//        img_clear_sp_task.setVisibility(View.GONE);
//        img_dropdown_sp_task.setVisibility(View.VISIBLE);
        if (::corpAdapter.isInitialized) {
            img_clear_sp_corp_clients.performClick()
        }
        selected_entity_corp_client_list.clear()
        ll_corp_clients.visibility = View.GONE
        ll_assign_clients.visibility = View.GONE
//        ll_client_team_members.setVisibility(View.GONE);
        ll_selected_teammembers.visibility = View.GONE
        ll_selected_corp_clients_view.visibility = View.GONE
        ll_selected_individual.visibility = View.GONE
//        at_assigned_corp_client.setText("");
//        ll_selected_entity_client.setVisibility(View.GONE);
    }

    private fun clear_selected_list() {
        Corp_client_list.clear()
        selected_documents_list.clear()
        selected_entity_client_list.clear()
        selected_individual_list.clear()
        selected_entity_corp_client_list.clear()
        selected_tm_list.clear()
        ll_selected_corp_clients.removeAllViews()
        ll_selected_team_members.removeAllViews()
        ll_individual_list.removeAllViews()
        ll_documents_list.removeAllViews()
        ll_selected_entites.removeAllViews()
        ll_client_team_members.removeAllViews()
        ll_selected_entity_client.visibility = View.GONE
    }

    private fun display_members() {
        if (teamList.isNotEmpty() || entities_list.isNotEmpty() || individual_list.isNotEmpty() || documents_list.isNotEmpty()) {
            cv_add_clients.visibility = View.VISIBLE
        } else {
            cv_add_clients.visibility = View.GONE
        }
    }

    private fun hide_members() {
        img_clear_sp_mattername.visibility = View.GONE
        img_dropdown_sp_mattername.visibility = View.VISIBLE
        cv_add_clients.visibility = View.GONE
//        ll_add_tm.setVisibility(View.GONE);
        ll_add_entities.visibility = View.GONE
        ll_individual.visibility = View.GONE
        ll_documents_view.visibility = View.GONE
    }

    private fun loadRepetetions() {
        Repetetions.clear()
        Repetetions.add("None")
        Repetetions.add("Daily")
        Repetetions.add("Weekly")
        Repetetions.add("Bi-Weekly")
        Repetetions.add("Monthly")
        Repetetions.add("Yearly")
        repetitionAdapter = CommonSpinnerAdapter(context as Activity, Repetetions)
        sp_repetetion.adapter = repetitionAdapter
        AndroidUtils.LoadList(sp_repetetion, requireContext(), Repetetions.size, true)
        //..
        sp_repetetion.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            repeat_interval = parent.getItemAtPosition(position) as String
            AndroidUtils.DisplaySpinnerView(sp_repetetion, tv_sp_repetetion, repeat_interval, img_dropdown_sp_repetetion, img_clear_sp_repetetion, false, repetitionAdapter, "Search Repetetion")
            if (repeat_interval == "None") {
                isAddTimesheet = true
                if (Constants.ROLE == "AAM") {
                    ll_add_to_timesheet.visibility = View.GONE
                    isAddTimesheet = false
                } else {
                    if (matter_legal != "reminders")
                        ll_add_to_timesheet.visibility = View.VISIBLE
                }
            } else {
                isAddTimesheet = false
                ll_add_to_timesheet.visibility = View.GONE
            }
            ischecked_repetetion = true
        }
    }

    private fun hideAlldetails() {
        ll_matter_name.visibility = View.GONE
        tv_no_matter.visibility = View.GONE
        ll_message.visibility = View.VISIBLE
        cv_meeting_details.visibility = View.GONE
        ll_task.visibility = View.GONE
        ll_documents_view.visibility = View.GONE
    }

    private fun hideMatterDetails() {
        cv_meeting_details.visibility = View.VISIBLE
        ll_matter_name.visibility = View.GONE
        tv_no_matter.visibility = View.GONE
        ll_message.visibility = View.GONE
        cb_add_to_timesheet.isChecked = isAddTimesheet
//        isAddTimesheet = isAddTimesheet;
        if (Constants.ROLE == "AAM") {
            ll_add_to_timesheet.visibility = View.GONE
            isAddTimesheet = false
        } else {
            if (matter_legal != "reminders")
                ll_add_to_timesheet.visibility = View.VISIBLE
        }
        ll_task.visibility = View.VISIBLE
//        ll_documents_view.setVisibility(View.GONE);
    }

    private fun unHideMatterDetails() {
        cv_meeting_details.visibility = View.VISIBLE
        ll_matter_name.visibility = View.VISIBLE
        tv_no_matter.visibility = View.GONE
        img_clear_sp_mattername.visibility = View.GONE
        img_dropdown_sp_mattername.visibility = View.VISIBLE
        ll_message.visibility = View.GONE
        cb_add_to_timesheet.isChecked = isAddTimesheet
//        isAddTimesheet = true;
        ll_task.visibility = View.VISIBLE
//        ll_documents_view.setVisibility(View.VISIBLE);
        cv_add_clients.visibility = View.GONE
    }

    private fun loadTaskList(legalTaksList: ArrayList<TaskDo>) {
        taskSpinnerAdapter = CommonSpinnerAdapter(context as Activity, legalTaksList)
        sp_task.adapter = taskSpinnerAdapter
        //..
        sp_task.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedTaskItem = parent.getItemAtPosition(position) as TaskDo
            selected_task = selectedTaskItem.taskName
            AndroidUtils.DisplaySpinnerView(sp_task, tv_sp_task_name, selected_task, img_dropdown_sp_task, img_clear_sp_task, false, taskSpinnerAdapter, "Search Task")
            ischecked_task = true
        }
    }

    private fun callProjectWebservice(selected_project: String) {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val postdata = JSONObject()
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v3/matter/$selected_project?module=meetings&status=active", "Matter List", postdata.toString())
        } catch (e: Exception) {
            AndroidUtils.showToast(e.message ?: "", requireContext())
            if (progressDialog?.isShowing == true) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.printStackTrace()
        }
    }

    private fun callCorpClientsWebservice() {
        try {
//            https://api.staging.digicoffer.com/professional/v3/corporate/list
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val postdata = JSONObject()
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v3/corporate/list", "Corp Client List", postdata.toString())
        } catch (e: Exception) {
            AndroidUtils.showToast(e.message ?: "", requireContext())
            if (progressDialog?.isShowing == true) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.printStackTrace()
        }
    }

    private fun callTeamMemberWebservice() {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val postdata = JSONObject()
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v3/event/tms", "Team List", postdata.toString())
        } catch (e: Exception) {
            AndroidUtils.showToast(e.message ?: "", requireContext())
            if (progressDialog?.isShowing == true) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.printStackTrace()
        }
    }

    private fun updateMeetingLinkFromCurrentValues() {
        if (meetingRoomId.isNullOrEmpty()) return
        if (tv_meeting_link == null) return

        val fromTime = tv_event_start_time.text.toString()
        val toTime = tv_event_end_time.text.toString()

        var date = ""
        try {
            val inputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val parsedDate = inputFormat.parse(tv_event_creation_date.text.toString())
            date = outputFormat.format(parsedDate)
        } catch (e: Exception) {
            date = tv_event_creation_date.text.toString()
        }

        meetingLink = AndroidUtils.getAVChatUrl(meetingRoomId, fromTime, toTime, date, Constants.NAME)
        tv_meeting_link.setText(meetingLink)
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progressDialog?.isShowing == true) {
            AndroidUtils.dismiss_dialog(progressDialog)
        }
        try {
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                val result = JSONObject(httpResult.responseContent)
                if (httpResult.requestType == "Matter List") {
                    if (!timeZonesTaskCompleted) {
                        return
                    }
                    if (result.optBoolean("error")) {
                        tv_no_matter.visibility = View.VISIBLE
                        ll_matter_name.visibility = View.GONE
                        AndroidUtils.showAlert(result.optString("msg"), activity)
                    } else {
                        val matters = result.getJSONArray("matters")
                        matterTaskCompleted = true
                        loadMattersList(matters)
                    }
                } else if (httpResult.requestType == "TIMEZONES") {
                    if (!result.optBoolean("error")) {
                        val jsonArray = result.getJSONArray("timezones")
                        load_timezones(jsonArray)
                        timeZonesTaskCompleted = true
                        if (Constants.is_meeting == "Edit") {
                            for (i in existing_events_list.indices) {
                                val event_details_do = existing_events_list[i]
                                matter_legal = event_details_do.event_type ?: ""
                                Log.d("Created_Event_Type", matter_legal)
                                is_linked_with_timesheet = event_details_do.is_linked_with_timesheet
                                existing_description = event_details_do.description
                                existing_date = event_details_do.date
                                existing_dialin = event_details_do.dialin
                                existing_location = event_details_do.location
                                existing_end_time = event_details_do.converted_End_time
                                existing_start_time = event_details_do.converted_Start_time
                                existing_meeting_link = event_details_do.meeting_link
                                isRecurring = event_details_do.isRecurring
                                existing_repetetion = AndroidUtils.CapitalizeFirstLetter(event_details_do.repeat_interval ?: "")
                                if (existing_repetetion == "Biweekly") {
                                    existing_repetetion = "Bi-Weekly"
                                }
                                if (existing_repetetion.isNullOrEmpty()) {
                                    existing_repetetion = "None"
                                }
                                event_id = event_details_do.id
                                matter_id = event_details_do.matter_id
                                matter_name = event_details_do.matter_name
                                val title = event_details_do.title
                                if (title != null && title.contains("-")) {
                                    val splitStrings = title.split(" - ").toTypedArray()
                                    val firstString = splitStrings[0]
                                    val secondString = splitStrings[1]
                                    existing_task = secondString
                                } else {
                                    existing_task = title
                                }
                                existing_time_zone = event_details_do.offset_location
                                isAddTimesheet = event_details_do.timesheet_added
                                isExistingAllday = event_details_do.all_day
                                isAllDay = event_details_do.all_day
                            }
                            check_event()
                            loadProjectData(Event_name)
                            loadExistingData()
                            cb_all_day.isChecked = isAllDay
                        }
                    } else {
                        AndroidUtils.showAlert("Something went wrong", activity)
                    }
                } else if (httpResult.requestType == "Team List") {
                    if (result.optBoolean("error")) {
                        AndroidUtils.showAlert(result.optString("msg"), activity)
                    } else {
                        val jsonArray = result.getJSONArray("users")
                        loadTeamList(jsonArray)
                        tmTaskCompleted = true
                    }
                    if (Constants.ROLE != "AAM") callClientsWebservice()
                } else if (httpResult.requestType == "Client List") {
                    if (result.optBoolean("error")) {
                        AndroidUtils.showAlert(result.optString("msg"), activity)
                    } else {
                        val jsonObject = result.getJSONObject("data")
                        val jsonArray = jsonObject.getJSONArray("relationships")
                        loadRelationshipsList(jsonArray)
                        callCorpClientsWebservice()
                    }
                } else if (httpResult.requestType == "Corp Client List") {
                    if (result.optBoolean("error")) {
                        AndroidUtils.showAlert(result.optString("msg"), activity)
                    } else {
                        val jsonArray = result.getJSONArray("relationships")
                        loadcorpclientslist(jsonArray)
                    }
                    if (entity_id.isNotEmpty()) {
                        callEntityClientWebservice(entity_id)
                    } else if (corp_client_id.isNotEmpty()) {
                        callEntityCorporateClientWebservice(corp_client_id)
                    }
                } else if (httpResult.requestType == "Entity Client List") {
                    if (result.optBoolean("error", false)) {
                        AndroidUtils.showAlert(result.optString("msg"), activity)
                    } else {
                        val jsonArray = result.getJSONArray("users")
                        loadEntity_Clients(jsonArray)
                    }
                    if (corp_client_id.isNotEmpty()) {
                        callEntityCorporateClientWebservice(corp_client_id)
                    }
                } else if (httpResult.requestType == "Entity Corp Client List") {
                    if (result.optBoolean("error", false)) {
                        AndroidUtils.showAlert(result.optString("msg"), activity)
                    } else {
                        val jsonArray = result.getJSONArray("users")
                        loadEntity_Corp_Clients(jsonArray)
                    }
                } else if (httpResult.requestType == "EDIT_EVENT") {
                    if (result.optBoolean("error", false)) {
                        AndroidUtils.showAlert(result.optString("msg"), activity)
                    } else if (result.has("errors")) {
                        val errors = result.getJSONArray("errors")
                        var error_msg = ""
                        for (i in 0 until errors.length()) {
                            val error = errors.getJSONObject(i)
                            error_msg = error.getString("msg")!!
                        }
                        AndroidUtils.showAlert(error_msg, activity)
                    } else {
                        AndroidUtils.showAlert(result.getString("msg"), activity)
                        loadClearedLists()
                        progressDialog?.dismiss()
                        meetings?.loadView()
                    }
                } else if (httpResult.requestType == "CREATE_EVENT") {
                    if (result.has("errors")) {
                        val jsonArray = result.getJSONArray("errors")
                        val iserror = jsonArray!!.getJSONObject(0)
                        val FieldName = iserror.getString("field")!!
                        val msg = iserror.getString("msg")!!
                        AndroidUtils.showAlert(msg, activity)
                    } else if (result.optBoolean("error", false)) {
                        AndroidUtils.showAlert(result.optString("msg"), activity)
                    } else {
                        AndroidUtils.showAlert(result.optString("msg"), activity)
                        loadClearedLists()
                        meetings?.loadView()
                    }
                } else if (httpResult.requestType == "CREATE_MEETING_LINK") {
                    meetingRoomId = result.optString("roomId", "")

                    if (meetingRoomId?.isNotEmpty() == true && tv_meeting_link != null) {
                        val fromTime = tv_event_start_time.text.toString()
                        val toTime = tv_event_end_time.text.toString()

                        // Convert "May 25, 2026" → "2026-05-25"
                        var date = ""
                        try {
                            val inputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            val parsedDate = inputFormat.parse(tv_event_creation_date.text.toString())
                            date = outputFormat.format(parsedDate)
                        } catch (e: Exception) {
                            date = tv_event_creation_date.text.toString()
                            Log.e("AVChat_Date", "Date parse failed: " + e.message)
                        }

                        meetingLink = AndroidUtils.getAVChatUrl(meetingRoomId, fromTime, toTime, date, Constants.NAME)
                        tv_meeting_link.setText(meetingLink)
                        Log.d("AVChat_MeetingLink", "Final Join URL: " + meetingLink)
                    }
                }
            } else {
                val result = JSONObject(httpResult.responseContent)
                if (result.optBoolean("error")) {
                    AndroidUtils.showAlert(result.optString("msg"), activity)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun loadCreatEvent() {
        try {
            val ft = childFragmentManager.beginTransaction()
            val nonSubmittedTimesheets = Meetings()
            ft.replace(R.id.id_framelayout, nonSubmittedTimesheets)
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            ft.addToBackStack(null)
            ft.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(JSONException::class)
    private fun loadEntity_Corp_Clients(jsonArray: JSONArray) {
        entity_corp_client_list.clear()
        for (i in 0 until jsonArray!!.length()) {
            val relationshipsDO = RelationshipsDO()
            val jsonObject = jsonArray!!.getJSONObject(i)
            relationshipsDO.id = corp_client_id + "_" + jsonObject.getString("id")
            relationshipsDO.name = corp_client_name + " - " + jsonObject.getString("name")
            entity_corp_client_list.add(relationshipsDO)
        }
        if (entity_corp_client_list.isEmpty()) {
            ll_corp_clients.visibility = View.GONE
            ll_corp_tm.visibility = View.GONE
        } else {
            ll_corp_clients.visibility = View.VISIBLE
            ll_corp_tm.visibility = View.VISIBLE
        }
//        loadSelectedClients();
    }

    @Throws(JSONException::class)
    private fun loadEntity_Clients(jsonArray: JSONArray) {
        entity_client_list.clear()
        for (i in 0 until jsonArray!!.length()) {
            val relationshipsDO = RelationshipsDO()
            val jsonObject = jsonArray!!.getJSONObject(i)
            relationshipsDO.id = entity_id + "_" + jsonObject.getString("id")
            relationshipsDO.name = entity_name + " - " + jsonObject.getString("name")
            entity_client_list.add(relationshipsDO)
        }
        if (entity_client_list.isEmpty()) {
            ll_assign_clients.visibility = View.GONE
            ll_entity_tm.visibility = View.GONE
        } else {
            ll_entity_tm.visibility = View.VISIBLE
            ll_assign_clients.visibility = View.VISIBLE
        }
        Log.d("Size", "" + entity_client_list.size)
//        loadSelectedClients();
    }

    fun selected_entity_clients(list_item: ArrayList<RelationshipsDO>) {
        // Create a list of existing IDs in the selected list for faster lookup
//        HashSet<String> selectedIds = new HashSet<>();
//        for (RelationshipsDO item : selected_entity_client_list) {
//            selectedIds.add(item.getId());
//        }
//
//        for (RelationshipsDO teamModel : list_item) {
//            if (teamModel.isChecked()) {
//                // Add the item only if its ID is not already in the selected list
//                if (!selectedIds.contains(teamModel.getId())) {
//                    selected_entity_client_list.add(teamModel);
//                    selectedIds.add(teamModel.getId());
//                }
//            } else {
//                // Remove the item if it is unchecked (by matching the ID)
//                for (int i = 0; i < selected_entity_client_list.size(); i++) {
//                    if (selected_entity_client_list.get(i).getId().equals(teamModel.getId())) {
//                        selected_entity_client_list.remove(i);
//                        break;
//                    }
//                }
//            }
//        }

        // Update the UI based on the state of the selected list
        if (selected_entity_client_list.isEmpty()) {
            ll_selected_entity_client.visibility = View.GONE
            at_assigned_client.setText("")
        } else {
            loadSelectedClients()
        }
    }

//    public void selected_entity_clients(ArrayList<RelationshipsDO> list_item) {
////        selected_entity_client_list.clear();
//        for (int i = 0; i < list_item.size(); i++) {
//            RelationshipsDO teamModel = list_item.get(i);
//            if (teamModel.isChecked()) {
//                if (!selected_entity_client_list.contains(teamModel)) {
//                    selected_entity_client_list.add(teamModel);
//                }
//            } else if (!teamModel.isChecked()) {
//                selected_entity_client_list.remove(teamModel);
//            }
//        }
//        if (selected_entity_client_list.isEmpty()) {
//            ll_selected_entity_client.setVisibility(View.GONE);
//            at_assigned_client.setText("");
//        } else {
//            loadSelectedClients();
//        }

    //        rv_clients_view.setVisibility(View.GONE);
    //        is_clicked_clients = true;
//    }
    private fun loadentity_corp_clients_popup() {
        try {
            if (entity_corp_client_list.isNotEmpty()) {
//                rv_clients_view.setVisibility(View.GONE);
//                is_clicked_clients = true;
//                AndroidUtils.showToast("No Clients to show", getContext());
//            } else {
                for (i in entity_corp_client_list.indices) {
                    val teamModel = entity_corp_client_list[i]
                    teamModel.isChecked = false
                    for (j in selected_entity_corp_client_list.indices) {
                        if (entity_corp_client_list[i].id == selected_entity_corp_client_list[j].id) {
                            teamModel.isChecked = true
                        }
                    }
                }
//                selected_entity_client_list.clear();
                val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                rv_corp_client_view.layoutManager = layoutManager
                //rv_corp_client_view.setHasFixedSize(true);
                ADAPTER_TAG = "CORPORATE"
                val documentsAdapter = CommonRelationshipsAdapter(teamList, ADAPTER_TAG, individual_list, entity_client_list, entity_corp_client_list, documents_list, this)
                rv_corp_client_view.adapter = documentsAdapter
                //..
                AndroidUtils.LoadList(rv_corp_client_view, requireContext(), entity_corp_client_list.size, true)
                //..
//                btn_assigned_clients.setOnClickListener(new View.OnClickListener() {

//                    override fun onClick(view: View) {
//                        for (i in 0 until documentsAdapter.entity_client_list.size) {
//                            val teamModel: RelationshipsDO = documentsAdapter.entity_client_list[i]
//                            if (teamModel.isChecked) {
//                                if (!selected_entity_client_list.contains(teamModel)) {
//                                    selected_entity_client_list.add(teamModel)
//                                }
//                            }
//                        }
//                        loadSelectedClients()
//                        rv_clients_view.visibility = View.GONE
//                        is_clicked_clients = true
//                    }
//                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AndroidUtils.showAlert(e.message ?: "", requireActivity())
        }
    }

    private fun loadEntityClientPopup() {
        try {
            if (entity_client_list.isNotEmpty()) {
//                rv_clients_view.visibility = View.GONE
//                is_clicked_clients = true
//                AndroidUtils.showToast("No Clients to show", requireContext())
//            } else {
                for (i in 0 until entity_client_list.size) {
                    val teamModel: RelationshipsDO = entity_client_list[i]
                    teamModel.isChecked = false
                    for (j in 0 until selected_entity_client_list.size) {
                        if (entity_client_list[i].id == selected_entity_client_list[j].id) {
                            teamModel.isChecked = true
                        }
                    }
                }
//                selected_entity_client_list.clear()
                val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                rv_clients_view.layoutManager = layoutManager
                // rv_clients_view.setHasFixedSize(true)
                ADAPTER_TAG = "ENTITY"
                val documentsAdapter = CommonRelationshipsAdapter(teamList, ADAPTER_TAG, individual_list, entity_client_list, entity_corp_client_list, documents_list, this)
                rv_clients_view.adapter = documentsAdapter
                //..
                AndroidUtils.LoadList(rv_clients_view, requireContext(), entity_client_list.size, true)
                //..
//                btn_assigned_clients.setOnClickListener(object : View.OnClickListener {
//                    override fun onClick(view: View) {
//                        for (i in 0 until documentsAdapter.entity_client_list.size) {
//                            val teamModel: RelationshipsDO = documentsAdapter.entity_client_list[i]
//                            if (teamModel.isChecked) {
//                                if (!selected_entity_client_list.contains(teamModel)) {
//                                    selected_entity_client_list.add(teamModel)
//                                }
//                            }
//                        }
//                        loadSelectedClients()
//                        rv_clients_view.visibility = View.GONE
//                        is_clicked_clients = true
//                    }
//                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AndroidUtils.showAlert(e.message ?: "", requireActivity())
        }
    }

    @Throws(JSONException::class)
    private fun loadcorpclientslist(jsonArray: JSONArray) {
        Corp_client_list.clear()
        for (i in 0 until jsonArray!!.length()) {
            val relationshipsDO = RelationshipsDO()
            val jsonObject = jsonArray!!.getJSONObject(i)
            relationshipsDO.id = jsonObject.getString("id")!!
            relationshipsDO.name = jsonObject.getString("name")!!
            relationshipsDO.type = jsonObject.getString("type")!!
            Corp_client_list.add(relationshipsDO)
        }
        if (Corp_client_list.isEmpty()) {
            ll_add_corp_clients.visibility = View.GONE
        } else {
            ll_add_corp_clients.visibility = View.VISIBLE
        }
        loadEntitiesSpinnerData()
    }

    @Throws(JSONException::class)
    private fun loadRelationshipsList(jsonArray: JSONArray) {
        individual_list.clear()
        entities_list.clear()
        for (i in 0 until jsonArray!!.length()) {
            val relationshipsDO = RelationshipsDO()
            val jsonObject = jsonArray!!.getJSONObject(i)
            if (jsonObject.getString("type") == "consumer") {
                relationshipsDO.id = jsonObject.getString("id")!!
                relationshipsDO.name = jsonObject.getString("name")!!
                relationshipsDO.type = jsonObject.getString("type")!!
                individual_list.add(relationshipsDO)
            } else {
                relationshipsDO.id = jsonObject.getString("id")!!
                relationshipsDO.name = jsonObject.getString("name")!!
                relationshipsDO.type = jsonObject.getString("type")!!
                entities_list.add(relationshipsDO)
            }
        }
        if (entities_list.isEmpty()) {
            ll_add_entities.visibility = View.GONE
        } else {
            ll_add_entities.visibility = View.VISIBLE
        }
        if (individual_list.isEmpty()) {
            ll_individual.visibility = View.GONE
        } else {
            ll_individual.visibility = View.VISIBLE
        }
        loadEntitiesSpinnerData()
    }

    @Throws(JSONException::class)
    private fun loadEntitiesSpinnerData() {
        if (entities_list.isEmpty()) {
            for (i in 0 until matterList.size) {
                if (matter_id == matterList[i].id) {
                    val entities = matterList[i].clients
                    if (entities != null) {
                        for (j in 0 until entities.length()) {
                            val relationshipsDO = RelationshipsDO()
                            val jsonObject = entities.getJSONObject(j)
                            if (jsonObject.getString("type") == "consumer") {
                                relationshipsDO.id = jsonObject.getString("id")!!
                                relationshipsDO.name = jsonObject.getString("name")!!
                                relationshipsDO.type = jsonObject.getString("type")!!
                                individual_list.add(relationshipsDO)
                            } else {
                                relationshipsDO.id = jsonObject.getString("id")!!
                                relationshipsDO.name = jsonObject.getString("name")!!
                                relationshipsDO.type = jsonObject.getString("type")!!
                                entities_list.add(relationshipsDO)
                            }
                        }
                    }
                    val corporate = matterList[i].corporate
                    run {
                        if (matterList[i].corp_has_value) {
                            if (corporate != null) {
                                for (j in 0 until corporate.length()) {
                                    val relationshipsDO = RelationshipsDO()
                                    val jsonObject = corporate.getJSONObject(j)
                                    relationshipsDO.id = jsonObject.getString("id")!!
                                    relationshipsDO.name = jsonObject.getString("name")!!
                                    relationshipsDO.type = jsonObject.getString("type")!!
                                    Corp_client_list.add(relationshipsDO)
                                }
                            }
                        }
                    }
                }
            }
            if (Corp_client_list.isEmpty()) {
                ll_add_corp_clients.visibility = View.GONE
            } else {
                ll_add_corp_clients.visibility = View.VISIBLE
            }
            if (entities_list.isEmpty()) {
                ll_entity_tm.visibility = View.GONE
                ll_add_entities.visibility = View.GONE
            } else {
                ll_add_entities.visibility = View.VISIBLE
            }
            if (individual_list.isEmpty()) {
                ll_individual.visibility = View.GONE
            } else {
                ll_individual.visibility = View.VISIBLE
            }
            TeamMembersPopup()
            Documents_Popup()
        }
        corpAdapter = CommonSpinnerAdapter(activity, Corp_client_list)
        sp_corp_clients.adapter = corpAdapter
        entityAdapter = CommonSpinnerAdapter(activity, entities_list)
        sp_entity.adapter = entityAdapter
        //..
        AndroidUtils.LoadList(sp_corp_clients, requireContext(), Corp_client_list.size, true)
        //..
        sp_corp_clients.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedCorpClientItem = parent.getItemAtPosition(position) as RelationshipsDO
            corp_client_id = selectedCorpClientItem.id ?: ""
            corp_client_name = selectedCorpClientItem.name
//                selected_entity_corp_client_list.clear()
            callEntityCorporateClientWebservice(corp_client_id)
            AndroidUtils.DisplaySpinnerView(sp_corp_clients, tv_sp_corp_clients, corp_client_name, img_dropdown_sp_corp_clients, img_clear_sp_corp_clients, false, corpAdapter, "Search Corp Client")
            is_corp = true
        }
        //..
        AndroidUtils.LoadList(sp_entity, requireContext(), entities_list.size, true)
        //..
//        callTimeZoneWebservice()
        sp_entity.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedEntityItem = parent.getItemAtPosition(position) as RelationshipsDO
            entity_id = selectedEntityItem.id ?: ""
            entity_name = selectedEntityItem.name
            callEntityClientWebservice(entity_id)
            AndroidUtils.DisplaySpinnerView(sp_entity, tv_sp_entity, entity_name, img_dropdown_sp_entity, img_clear_sp_entity, false, entityAdapter, "Search Entity")
            is_entity = true
        }
    }

    private fun callEntityCorporateClientWebservice(id: String) {
        try {
            //https://api.staging.digicoffer.com/professional/related/entities/tms/64f18b78fffd8f4f4623ea3f
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val postdata = JSONObject()
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "related/entities/tms/$id", "Entity Corp Client List", postdata.toString())
        } catch (e: Exception) {
            AndroidUtils.showToast(e.message ?: "", requireContext())
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.printStackTrace()
        }
    }

    private fun callEntityClientWebservice(id: String) {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val postdata = JSONObject()
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "related/entities/tms/$id", "Entity Client List", postdata.toString())
        } catch (e: Exception) {
            AndroidUtils.showToast(e.message ?: "", requireContext())
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.printStackTrace()
        }
    }

    @Throws(JSONException::class)
    private fun loadTeamList(jsonArray: JSONArray) {
        teamList.clear()
        for (i in 0 until jsonArray!!.length()) {
            val jsonObject = jsonArray!!.getJSONObject(i)
            val teamDo = TeamDo()
            teamDo.id = jsonObject.getString("id")!!
            teamDo.name = jsonObject.getString("name")!!
            teamList.add(teamDo)
        }
        if (teamList.isEmpty()) {
            ll_add_tm.visibility = View.GONE
        } else {
            if ("solo" != Constants.CATEGORY) {
                ll_add_tm.visibility = View.VISIBLE
            }
        }
        TeamMembersPopup()
    }

//    private fun loadTeamSpinner() {
//        val matterAutocompleteAdapter = AutocompleteAdapter(requireContext(), 1, teamList)
//
//        /// /        callLegalwebservice()
//        at_family_members.inputType = InputType.TYPE_NULL
//        at_family_members.threshold = 0
//        at_family_members.setAdapter(matterAutocompleteAdapter)
//        at_family_members.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
//        at_family_members.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
//            val selectedItem = matterAutocompleteAdapter.getItem(position)
//            matterAutocompleteAdapter.remove(selectedItem)
//            matterAutocompleteAdapter.notifyDataSetChanged()
//        }
//    }

    @Throws(JSONException::class)
    private fun load_timezones(jsonArray: JSONArray) {
        var timeZonesDO: TimeZonesDO
        timeZonesList.clear()
        timesPosHash.clear()
        for (i in 0 until jsonArray!!.length()) {
            timeZonesDO = TimeZonesDO()
            timeZonesDO.NAME = jsonArray!!.getJSONArray(i)[1].toString()
            timeZonesDO.GMT = jsonArray!!.getJSONArray(i)[0].toString()
            timeZonesList.add(timeZonesDO)
            timesPosHash[jsonArray!!.getJSONArray(i)[0].toString()] = i
        }
        loadTimeZonespinner()
    }

    private fun loadTimeZonespinner() {
        timezoneAdapter = CommonSpinnerAdapter(activity, timeZonesList)
        sp_time_zone.adapter = timezoneAdapter
        AndroidUtils.LoadList(sp_time_zone, requireContext(), timeZonesList.size, true)

        val calendar: Calendar = GregorianCalendar()
//        val hours = TimeUnit.MILLISECONDS.toMinutes(offset1)
        for (j in 0 until timeZonesList.size) {
            val timezone_offset = timeZonesList[j].name
            if (timezone_offset == "(GMT+05:30) India Standard Time - Kolkata") {
                sp_time_zone.setSelection(j)
                timezone_location = timeZonesList[j].name
                offset = timeZonesList[j].GMT?.toInt() ?: 0
                AndroidUtils.DisplaySpinnerView(sp_time_zone, tv_sp_time_zone, timezone_location, img_dropdown_sp_timezone, img_clear_sp_timezone, false, timezoneAdapter, "Search TimeZone")
            }
        }
//        callTimeZoneWebservice()
        sp_time_zone.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedTimeZoneItem = parent.getItemAtPosition(position) as TimeZonesDO
            offset = selectedTimeZoneItem.GMT?.toInt() ?: 0
            timezone_location = selectedTimeZoneItem.name ?: ""
            Log.d("Timezone", timezone_location ?: "")
            AndroidUtils.DisplaySpinnerView(sp_time_zone, tv_sp_time_zone, timezone_location, img_dropdown_sp_timezone, img_clear_sp_timezone, false, timezoneAdapter, "Search TimeZone")
            ischecked_time = true
        }
    }

    @Throws(JSONException::class)
    private fun loadMattersList(matters: JSONArray) {
        matterList.clear()
//        AndroidUtils.showAlert(matters.toString(),requireContext())
        for (i in 0 until matters.length()) {
            val jsonObject = matters.getJSONObject(i)
            val viewMatterModel = ViewMatterModel()
            viewMatterModel.id = jsonObject.optString("id")
            viewMatterModel.title = jsonObject.optString("title")
            viewMatterModel.status = jsonObject.optString("status")
            viewMatterModel.documents = jsonObject.optJSONArray("documents")
            viewMatterModel.clients = jsonObject.optJSONArray("clients")
            viewMatterModel.members = jsonObject.optJSONArray("members")
            viewMatterModel.corporate = jsonObject.optJSONArray("corporate")
            if (jsonObject.has("corporate")) {
                viewMatterModel.corp_has_value = true
            } else {
                viewMatterModel.corp_has_value = false
            }
//            viewMatterModel.casetype = jsonObject.getString("caseType")!!
            matterList.add(viewMatterModel)
        }
        if (matterList.isEmpty()) {
            tv_no_matter.visibility = View.VISIBLE
            ll_matter_name.visibility = View.GONE
            Constants.create_matter = true
            if (matter_legal == "general") {
                Constants.MATTER_TYPE = "General"
            } else {
                Constants.MATTER_TYPE = "Legal"
            }
            AndroidUtils.showReDirectionPopup(requireActivity(), Matter(),
                "Please create a matter to create an event. Click here to create matter.")
        } else {
            tv_no_matter.visibility = View.GONE
            ll_matter_name.visibility = View.VISIBLE
        }
//        AndroidUtils.showAlert(matters.toString(),requireContext())
        loadMatterSpinnerList(matterList)
        if (Constants.is_meeting == "Edit") {
            loadEntitiesSpinnerData()
            if (entity_id.isNotEmpty()) {
                callEntityClientWebservice(entity_id)
            } else if (corp_client_id.isNotEmpty()) {
                callEntityCorporateClientWebservice(corp_client_id)
            }
        }
    }

    private fun loadMatterSpinnerList(matterList: ArrayList<ViewMatterModel>) {
        matterSpinnerAdapter = CommonSpinnerAdapter(context as Activity, matterList)
        sp_matter_name.adapter = matterSpinnerAdapter
        //..
        AndroidUtils.LoadList(sp_matter_name, requireContext(), matterList.size, true)
        //..
        sp_matter_name.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
            val selectedMatterItem = adapterView.getItemAtPosition(position) as ViewMatterModel
            matter_id = selectedMatterItem.id
            matter_name = selectedMatterItem.title
            tv_sp_matter_name.setText(matter_name)
            ll_assign_clients.visibility = View.GONE

            load_clear_list()
            clear_selected_list()
            if (matterList[position].status == "Closed") {
                ll_add_to_timesheet.visibility = View.GONE
                isAddTimesheet = false
            } else {
//                    isAddTimesheet = true
                if (Constants.ROLE == "AAM") {
                    ll_add_to_timesheet.visibility = View.GONE
                    isAddTimesheet = false
                } else {
                    if (matter_legal != "reminders")
                        ll_add_to_timesheet.visibility = View.VISIBLE
                }
            }
            ll_selected_teammembers.visibility = View.GONE
            ll_selected_entity_client.visibility = View.GONE //clients
            ll_selected_individual.visibility = View.GONE
            ll_selected_documents.visibility = View.GONE
            try {
                loadEntitiesSpinnerData()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            hide_all_list()
            AndroidUtils.DisplaySpinnerView(sp_matter_name, tv_sp_matter_name, matter_name, img_dropdown_sp_mattername, img_clear_sp_mattername, false, matterSpinnerAdapter, "Search Matter")
            display_members()
            ischecked_matter = true
        }
    }

    constructor(meetings1: Meetings, event_details_list: ArrayList<Event_Details_DO>?) : this() {
        meetings = meetings1
        existing_events_list = event_details_list ?: ArrayList()
    }

    private fun CheckUpdateScope(recurring_edit_choice: String) {
//        if ((!existing_repetetion.equals("None")) || (!tv_sp_repetetion.getText().toString().equals("None"))) {
//            AndroidUtils.showConfirmationDialog(getContext(), "Confirmation", "Are you sure you want to update this event?", "", "", new AndroidUtils.OnConfirmListener() {
//                        @Override
//                        public void onSave() {
////                            event_update_scope = "UPDATE_BOTH"
//                            event_update_scope = "UPDATE_EVENT_ONLY"
//                            callCreateEventWebservice_edit(recurring_edit_choice)
//                        }
//
//                        @Override
//                        public void onCancel() {
////                            event_update_scope = "UPDATE_EVENT_ONLY"
////                            callCreateEventWebservice_edit(recurring_edit_choice)
//                        }
//                    }
//            )
//        } else
        if (is_linked_with_timesheet && isAddTimesheet) {
            AndroidUtils.showConfirmationDialog(requireContext(), "Confirmation", "This event has an associated timesheet entry. " +
                    "Do you want to update the timesheet too?", requireContext().getString(R.string.update_both), requireContext().getString(R.string.update_event_only), object : AndroidUtils.OnConfirmListener {
                override fun onSave() {
                    event_update_scope = "UPDATE_BOTH"
                    callCreateEventWebservice_edit(recurring_edit_choice)
                }

                override fun onCancel() {
                    event_update_scope = "UPDATE_EVENT_ONLY"
                    callCreateEventWebservice_edit(recurring_edit_choice)
                }
            })
        } else {
            AndroidUtils.showConfirmationDialog(requireContext(), "Confirmation", "Are you sure you want to update this event?", "", "", object : AndroidUtils.OnConfirmListener {
                override fun onSave() {
//                            event_update_scope = "UPDATE_BOTH"
                    event_update_scope = "UPDATE_EVENT_ONLY"
                    callCreateEventWebservice_edit(recurring_edit_choice)
                }

                override fun onCancel() {
//                            event_update_scope = "UPDATE_EVENT_ONLY"
//                            callCreateEventWebservice_edit(recurring_edit_choice)
                }
            })
        }
    }

    private fun callCreateEventWebservice_edit(recurring_edit_choice: String) {
        try {
            val doctype = "doctype"
            val docid = "docid"
            val postData = JSONObject()
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val selected_team_member = JSONArray()
            val selected_clients_list = JSONArray()
            val selected_corp_clients_list = JSONArray()
            val individual_array = JSONArray()
            val added_notification_list = JSONArray()
            val time_sheets = JSONArray()
            val existing_attachments = JSONArray()
            for (i in 0 until selectedValues.size) {
//            NotifyMeDo notifyMeDo = notifyme_list.get(i)
                added_notification_list.put(selectedValues[i])
            }

            var selected_docs: JSONObject
            for (j in 0 until selected_documents_list.size) {
                selected_docs = JSONObject()
                val attachDocumentsDO = selected_documents_list[j]
                selected_docs.put(doctype, attachDocumentsDO.doctype)
                selected_docs.put(docid, attachDocumentsDO.docid)
                existing_attachments.put(selected_docs)
            }

            for (i in 0 until selected_tm_list.size) {
                val addTeamMembersDo = selected_tm_list[i]
                selected_team_member.put(addTeamMembersDo.id)
            }
            for (i in 0 until selected_entity_client_list.size) {
                val addClientsDo = selected_entity_client_list[i]
                selected_clients_list.put(addClientsDo.id)
            }
            for (i in 0 until selected_entity_corp_client_list.size) {
                val addClientsDo = selected_entity_corp_client_list[i]
                selected_corp_clients_list.put(addClientsDo.id)
            }
            for (i in 0 until selected_individual_list.size) {
                val relationshipsDO = selected_individual_list[i]
                individual_array.put(relationshipsDO.id)
            }
            //...

            val event_date: Date? = AndroidUtils.stringToDateTimeDefault(tv_event_creation_date.text.toString(), "MMM dd, yyyy")
            event_creation_date = AndroidUtils.getDateToString(event_date, "yyyy-MM-dd")
            var event_start_date: Date? = null
            var event_date2: Date? = null
            var start_time = tv_event_start_time.text.toString()
            if (start_time.isEmpty()) {
                start_time = "00:00"
                event_start_date = AndroidUtils.stringToDateTimeDefault(start_time, "HH:mm")

            } else {
                event_start_date = AndroidUtils.stringToDateTimeDefault(start_time, "HH:mm")
            }
            event_starting_date = AndroidUtils.getDateToString(event_start_date, event_creation_date + "'T'HH:mm:ss")
            var end_time = tv_event_end_time.text.toString()
            if (end_time.isEmpty()) {
                end_time = "00:00"
                event_date2 = AndroidUtils.stringToDateTimeDefault(end_time, "HH:mm")
            } else {
                event_date2 = AndroidUtils.stringToDateTimeDefault(end_time, "HH:mm")
            }
            var duration_timesheet = ""
            if (event_start_date != null && event_date2 != null) {

                val differenceInMilliSeconds = Math.abs(event_start_date.time - event_date2.time)
                val differenceInHours = (differenceInMilliSeconds / (60 * 60 * 1000)) % 24
                val differenceInMinutes = (differenceInMilliSeconds / (60 * 1000)) % 60
                val differenceInSeconds = (differenceInMilliSeconds / 1000) % 60
                val duration = "$differenceInHours:$differenceInMinutes"
                val hours = AndroidUtils.stringToDateTimeDefault(duration, "HH:mm")
                duration_timesheet = AndroidUtils.getDateToString(hours, "HH:mm")
            }
            event_end_time = AndroidUtils.getDateToString(event_date2, event_creation_date + "'T'HH:mm:ss")

            //......

            if (isAddTimesheet) {
                val time_sheet_obj = JSONObject()
                time_sheet_obj.put("date", event_creation_date)
                if (isAllDay) {
                    time_sheet_obj.put("duration", "23:59")
                    event_starting_date = event_creation_date + "T00:00:00"
                    event_end_time = event_creation_date + "T23:59:59"
                } else {
                    if (duration_timesheet.isEmpty()) {
                        time_sheet_obj.put("duration", "30:00")
                    } else {
                        time_sheet_obj.put("duration", duration_timesheet)
                    }
                }
                time_sheet_obj.put("eventtitle", tv_sp_task_name.text.toString())
                time_sheet_obj.put("addedby", Constants.NAME)
                time_sheet_obj.put("user_id", Constants.USER_ID)
                if (matter_legal != "legal" && matter_legal != "general") {
                    time_sheet_obj.put("matter_id", tv_sp_project.text.toString())
                    time_sheet_obj.put("matter_type", matter_legal)
                }
                time_sheets.put(time_sheet_obj)
            }
            val multiplied_offset = -1 * offset
            val dateInput = event_creation_date // "yyyy-MM-dd"

// Parse in UTC (prevents shifting)
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateInput)

// Now set exact UTC midnight
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            if (date != null) {
                cal.time = date
            }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

// Output
            val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            utcFormat.timeZone = TimeZone.getTimeZone("UTC")
            val finalDate = utcFormat.format(cal.time)

            postData.put("date", finalDate)

            postData.put("attachments", existing_attachments)
            postData.put("invitees_internal", selected_team_member)
            postData.put("invitees_corporate", selected_corp_clients_list)
            postData.put("invitees_external", selected_clients_list)
            postData.put("invitees_consumer_external", individual_array)
            if (matter_legal == "legal" || matter_legal == "general") {
                postData.put("title", matter_name + " - " + tv_sp_task_name.text.toString())
                postData.put("dialin", tv_dialing_number.text.toString())
                postData.put("location", tv_location.text.toString())
                postData.put("addtimesheet", isAddTimesheet)
                postData.put("timesheets", time_sheets)
                postData.put("description", tv_description.text.toString())
            } else if (matter_legal == "overhead" || matter_legal == "others") {
                postData.put("dialin", tv_dialing_number.text.toString())
                postData.put("location", tv_location.text.toString())
                postData.put("addtimesheet", isAddTimesheet)
                postData.put("timesheets", time_sheets)
                postData.put("description", tv_description.text.toString())
                postData.put("title", tv_sp_task_name.text.toString())
            } else {
                postData.put("title", tv_sp_task_name.text.toString())
                postData.put("description", tv_message.text.toString())
            }
//            if (is_linked_with_timesheet) {
            postData.put("event_update_scope", event_update_scope)
//            }
            postData.put("notifications", added_notification_list)
//            postData.put("description", tv_description.getText().toString())
            postData.put("timezone_location", timezone_location)
            postData.put("timezone_offset", multiplied_offset)
            var repetetion = ""
            if (tv_sp_repetetion.text.toString().lowercase(Locale.ROOT) == "bi-weekly") {
                repetetion = "biweekly"
            } else if (tv_sp_repetetion.text.toString() == "None") {
                repetetion = ""
            } else {
                repetetion = tv_sp_repetetion.text.toString().lowercase(Locale.ROOT)
            }
            Log.d("Repetation", repetetion)
            postData.put("repeat_interval", repetetion)
            postData.put("meeting_link", (tv_meeting_link.text!!).toString())
            postData.put("allday", isAllDay)
            postData.put("recurrent_edit_choice", recurring_edit_choice)
            postData.put("from_ts", event_starting_date)
            postData.put("to_ts", event_end_time)
            if (matter_legal == "legal" || matter_legal == "general") {
                postData.put("matter_type", matter_legal)
                postData.put("matter_id", matter_id)
            }
            postData.put("event_type", matter_legal)
            Log.d("Event_Edit", postData.toString())
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.PUT, "v3/event/" + event_id + "/" + multiplied_offset, "EDIT_EVENT", postData.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun update_event() {
        try {
            val builder = AlertDialog.Builder(activity)
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.edit_recurring_choice, null)
            val ll_only_this: LinearLayout = dialogLayout.findViewById(R.id.radio_delete_only_this)!!
            ll_only_this.background = context?.getDrawable(R.drawable.background_transparent)
            if (existing_repetetion == tv_sp_repetetion.text.toString()) {
                ll_only_this.visibility = View.VISIBLE
            } else {
                ll_only_this.visibility = View.GONE
            }
            val ll_all: LinearLayout = dialogLayout.findViewById(R.id.radio_delete_all)!!
            val delete_all: CheckBox = ll_all.findViewById(R.id.chk_select_all)!!
            val delete_all_txt: TextView = ll_all.findViewById(R.id.chk_select_all_text)!!
            ll_all.background = context?.getDrawable(R.drawable.background_transparent)
            delete_all_txt.setText(R.string.all_events)
            delete_all_txt.textSize = DynamicUtils.twenty.toFloat()
            val ll_following: LinearLayout = dialogLayout.findViewById(R.id.radio_delete_ts_fe)!!
            val delete_following: CheckBox = ll_following.findViewById(R.id.chk_select_all)!!
            val delete_following_txt: TextView = ll_following.findViewById(R.id.chk_select_all_text)!!
            ll_following.background = context?.getDrawable(R.drawable.background_transparent)
            delete_following_txt.setText(R.string.this_and_following_events)
            delete_following_txt.textSize = DynamicUtils.twenty.toFloat()
            val delete: Button = dialogLayout.findViewById(R.id.delete_event)!!
            val btn_close_event: Button = dialogLayout.findViewById(R.id.btn_close_event)!!
            btn_close_event.setTextColor(Color.RED)
            btn_close_event.setText(R.string.cancel)

            val delete_only_this: CheckBox = ll_only_this.findViewById(R.id.chk_select_all)!!
            val delete_only_this_txt: TextView = ll_only_this.findViewById(R.id.chk_select_all_text)!!
            ll_only_this.background = context?.getDrawable(R.drawable.background_transparent)
            delete_only_this_txt.setText(R.string.this_event)
            delete_only_this_txt.textSize = DynamicUtils.twenty.toFloat()

            delete_only_this.setOnClickListener {
                delete_only_this.isChecked = true
                recurring_edit_choice = "this"
                delete_all.isChecked = false
                delete_following.isChecked = false
            }
            delete_following.setOnClickListener {
                delete_following.isChecked = true
                recurring_edit_choice = "forward"
                delete_all.isChecked = false
                delete_only_this.isChecked = false
            }
            delete_all.setOnClickListener {
                delete_all.isChecked = true
                recurring_edit_choice = "all"
                delete_following.isChecked = false
                delete_only_this.isChecked = false
            }

            val dialog = builder.create()
            progressDialog = dialog

            btn_close_event.setOnClickListener { progressDialog?.dismiss() }
            delete.setOnClickListener {
                if (delete_following.isChecked || delete_all.isChecked || delete_only_this.isChecked) {
                    progressDialog?.dismiss()
                    CheckUpdateScope(recurring_edit_choice ?: "this")
                } else {
                    AndroidUtils.showAlert("Please choose one of the edit recurring event", activity)
                }
            }
            dialog.setView(dialogLayout)
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadExistingData() {
        btn_create_event.setText(R.string.update)
        cv_add_clients.visibility = View.VISIBLE
        ll_add_entities.visibility = View.VISIBLE
        ll_individual.visibility = View.VISIBLE
        if ("solo" != Constants.CATEGORY) {
            ll_add_tm.visibility = View.VISIBLE
        }
        hide_documents()
        ll_sp_project.isEnabled = false
        tv_sp_project.isClickable = false
        ll_sp_mattername.isEnabled = false
        tv_sp_matter_name.isClickable = false
        //...
        tv_sp_project.setText(Event_name)
        tv_sp_matter_name.setText(matter_name)
        if (tv_sp_project.text.toString().isNotEmpty())
            ll_project.visibility = View.VISIBLE
        if (tv_sp_matter_name.text.toString().isNotEmpty()) {
            ll_matter_name.visibility = View.VISIBLE
            tv_no_matter.visibility = View.GONE
        }

        ll_assign_clients.visibility = View.VISIBLE
        if (matter_legal == "reminders") {
            tv_message.setText(existing_description)
            ll_task.visibility = View.GONE
        } else {
            ll_task.visibility = View.VISIBLE
        }
        //event
        img_clear_sp_project.isEnabled = false
        img_clear_sp_project.visibility = View.VISIBLE
        img_dropdown_sp_project.visibility = View.GONE
        //matter
        img_clear_sp_mattername.isEnabled = false
        img_clear_sp_mattername.visibility = View.VISIBLE
        img_dropdown_sp_mattername.visibility = View.GONE
        //task
        img_clear_sp_task.visibility = View.VISIBLE
        img_dropdown_sp_task.visibility = View.GONE

        ll_repetetion.visibility = View.VISIBLE
        tv_sp_task_name.setText(existing_task)
        tv_description.setText(existing_description)
        tv_sp_repetetion.setText(existing_repetetion)
        img_dropdown_sp_repetetion.visibility = View.GONE
        img_clear_sp_repetetion.visibility = View.VISIBLE
        tv_meeting_link.setText(existing_meeting_link)
        tv_event_creation_date.setText(AndroidUtils.formatToMMMddYYYY(existing_date))

        tv_location.setText(existing_location)
        tv_dialing_number.setText(existing_dialin)
        tv_event_creation_date.setText(AndroidUtils.formatToMMMddYYYY(existing_date))
//        if (isAddTimesheet) {
//        isAddTimesheet = true
        cb_add_to_timesheet.isChecked = isAddTimesheet
//        cb_add_to_timesheet.setEnabled(false)
        if (existing_repetetion == "None") {
            if (Constants.ROLE == "AAM") {

                isAddTimesheet = false
                ll_add_to_timesheet.visibility = View.GONE
            } else {
                if (matter_legal == "reminders" || matter_legal == "overhead" || matter_legal == "others")
                    ll_add_to_timesheet.visibility = View.GONE
                else ll_add_to_timesheet.visibility = View.VISIBLE
            }
//            isAddTimesheet = true;
        } else {
//            isAddTimesheet = false;
            ll_add_to_timesheet.visibility = View.GONE
        }
//        }
        for (i in timeZonesList.indices) {
            if (existing_time_zone == timeZonesList[i].name) {
                sp_time_zone.setSelection(i)
                timezone_location = timeZonesList[i].name ?: ""
                offset = timeZonesList[i].GMT?.toInt() ?: 0
            }
        }
        tv_sp_time_zone.setText(timezone_location)

        for (i in Repetetions.indices) {
            if (existing_repetetion == Repetetions[i]) {
                sp_repetetion.setSelection(i)
            }
        }
        try {
            for (i in existing_events_list.indices) {
                val documents = existing_events_list[i].attachments
                if (documents != null) {
                    for (j in 0 until documents.length()) {
                        val documentsDo = DocumentsDo()
                        val jsonObject = documents.getJSONObject(j)
                        documentsDo.docid = jsonObject.getString("docid")!!
                        documentsDo.doctype = jsonObject.getString("doctype")!!
                        documentsDo.name = jsonObject.getString("name")!!
                        selected_documents_list.add(documentsDo)
                    }
                }
            }
            if (selected_documents_list.isNotEmpty()) {
                loadSelectedDocuments()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        try {
            for (i in existing_events_list.indices) {
                val team_members = existing_events_list[i].team_name
                if (team_members != null) {
                    for (j in 0 until team_members.length()) {
                        val teamModel = TeamDo()
                        val jsonObject = team_members.getJSONObject(j)
                        teamModel.id = jsonObject.getString("id")!!
                        teamModel.name = jsonObject.getString("name")!!
                        selected_tm_list.add(teamModel)
                    }
                }
            }
            if (selected_tm_list.isNotEmpty()) {
                loadSelectedTM()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            for (i in existing_events_list.indices) {
                val clients = existing_events_list[i].tm_name
                if (clients != null) {
                    for (j in 0 until clients.length()) {
                        val relationshipsDO = RelationshipsDO()
                        val jsonObject = clients.getJSONObject(j)
                        entity_id = jsonObject.getString("entityId")!!
                        relationshipsDO.id = jsonObject.getString("entityId")!! + "_" + jsonObject.getString("tmId")
                        entity_name = jsonObject.getString("entityName")!!
                        val ent_name_str = entity_name ?: ""
                        if (ent_name_str.isNotEmpty())
                            relationshipsDO.name = ent_name_str + " - " + jsonObject.getString("tmName")
                        else
                            relationshipsDO.name = jsonObject.getString("tmName")!!
                        selected_entity_client_list.add(relationshipsDO)
                    }
                }
            }

            for (i in existing_events_list.indices) {
                val corporate = existing_events_list[i].corporate
                if (corporate != null) {
                    for (j in 0 until corporate.length()) {
                        val relationshipsDO = RelationshipsDO()
                        val jsonObject = corporate.getJSONObject(j)
                        corp_client_id = jsonObject.getString("entityId")!!
                        relationshipsDO.id = jsonObject.getString("entityId")!! + "_" + jsonObject.getString("tmId")
                        relationshipsDO.name = jsonObject.getString("tmName")!!
                        corp_client_name = jsonObject.getString("entityName")!!
                        val corp_name_str = corp_client_name ?: ""
                        if (corp_name_str.isNotEmpty())
                            relationshipsDO.name = corp_name_str + " - " + jsonObject.getString("tmName")
                        else
                            relationshipsDO.name = jsonObject.getString("tmName")!!
                        selected_entity_corp_client_list.add(relationshipsDO)
                    }
                }
            }
            val ent_name_str = entity_name
            if (ent_name_str != null && ent_name_str.isNotEmpty()) {
                tv_sp_entity.setText(ent_name_str)
                img_clear_sp_entity.visibility = View.VISIBLE
                img_dropdown_sp_entity.visibility = View.GONE
                Log.d("Entity_id", entity_id)
            }
            val corp_name_str = corp_client_name
            if (corp_name_str != null && corp_name_str.isNotEmpty()) {
                tv_sp_corp_clients.setText(corp_name_str)
                img_clear_sp_corp_clients.visibility = View.VISIBLE
                img_dropdown_sp_corp_clients.visibility = View.GONE
                Log.d("Entity_id", corp_client_id)
            }

            if (selected_entity_client_list.isNotEmpty()) {
                ll_entity_tm.visibility = View.VISIBLE
                loadSelectedClients()
            } else {
                ll_entity_tm.visibility = View.GONE
            }
            if (selected_entity_corp_client_list.isNotEmpty()) {
                ll_corp_clients.visibility = View.VISIBLE
                loadselectedCorpClients()
            } else {
                ll_corp_clients.visibility = View.GONE
            }
//            if (!selected_entity_corp_client_list.isEmpty()) {
//                ll_corp_clients.setVisibility(View.VISIBLE);
//                ll_corp_tm.setVisibility(View.GONE);
//                loadselectedCorpClients();
//            }
            Log.d("selected_Corpoarte_client", "" + selected_entity_corp_client_list.size)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            for (i in existing_events_list.indices) {
                val individuals = existing_events_list[i].consumer_external
                if (individuals != null) {
                    for (j in 0 until individuals.length()) {
                        val relationshipsDO = RelationshipsDO()
                        val jsonObject = individuals.getJSONObject(j)
                        relationshipsDO.id = jsonObject.getString("entityId")!!
                        relationshipsDO.name = jsonObject.getString("tmName")!!
                        relationshipsDO.type = jsonObject.getString("tmId")!!
                        selected_individual_list.add(relationshipsDO)
                    }
                }
            }
            loadSelectedIndividual()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (selected_documents_list.isEmpty()) {
            ll_selected_documents.visibility = View.GONE
        } else {
//            ll_selected_documents.visibility = View.VISIBLE
        }
        if (selected_tm_list.isEmpty()) {
            ll_selected_teammembers.visibility = View.GONE
        } else {
//            ll_selected_teammembers.visibility = View.VISIBLE
        }
        if (selected_entity_client_list.isEmpty()) {
            ll_selected_entity_client.visibility = View.GONE
        } else {
            ll_selected_entity_client.visibility = View.VISIBLE
        }
        if (selected_individual_list.isEmpty()) {
            ll_selected_individual.visibility = View.GONE
        } else {
//            ll_selected_individual.visibility = View.VISIBLE
        }
        //Device Compatibility Issue with Null Value...
        var st_time = ""
        var ed_time = ""
        if (isExistingAllday) {
            cb_all_day.isChecked = true
        }
        if (isExistingAllday) {
            ll_time.visibility = View.GONE
            isAllDay = true
            display_duration("0:00", "23:59")
            cb_all_day.isChecked = true
        } else {
            tv_event_start_time.setText(existing_start_time)
            tv_event_end_time.setText(existing_end_time)
            display_duration(requireNotNull(tv_event_start_time.text).toString(), requireNotNull(tv_event_end_time.text).toString())
            ll_time.visibility = View.VISIBLE
            isAllDay = false
            cb_all_day.isChecked = false
        }
        try {
            for (i in existing_events_list.indices) {
                val notification = existing_events_list[i].notifications
                if (notification != null && notification.length() > 0) {
                    for (j in 0 until notification.length()) {
                        selectedValues.add(notification.get(j).toString())
                        val value_notify = notification.getString(j)!!
                        val time = value_notify.split("-").toTypedArray()
                        time_format = time[0]
                        time_value = time[1]
                        NotificationPopup()
                        Log.d("selected_Values1", "" + selectedValues.size)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        if ((!existing_start_time.contains("AM")) || (!existing_start_time.contains("PM"))) {
//            if (existing_start_time.contains("am"))
//                st_time = existing_start_time.replace("am", "AM");
//            if (existing_start_time.contains("pm"))
//                st_time = existing_start_time.replace("pm", "PM");
//            tv_event_start_time.setText(st_time);
//        } else {
//            tv_event_start_time.setText(existing_start_time);
//        }
//        if ((!existing_end_time.contains("AM")) || (!existing_end_time.contains("PM"))) {
//            if (existing_end_time.contains("am"))
//                ed_time = existing_end_time.replace("am", "AM");
//            if (existing_end_time.contains("pm"))
//                ed_time = existing_end_time.replace("pm", "PM");
//            tv_event_end_time.setText(ed_time);
//        } else {
//            tv_event_end_time.setText(existing_end_time);
//        }
        HideClientView()
    }

    private fun check_event() {
        when (matter_legal) {
            "legal" -> Event_name = "Legal Matter"
            "general" -> Event_name = "General Matter"
            "overhead" -> Event_name = "Overhead"
            "others" -> Event_name = "Others"
            "reminders" -> Event_name = "Reminders"
        }
    }

    private fun display_check_list() {
        if (teamList.isEmpty() && entities_list.isEmpty() && individual_list.isEmpty() && documents_list.isEmpty() && entity_client_list.size == 0) {
            cv_add_clients.visibility = View.GONE
        } else {
            cv_add_clients.visibility = View.VISIBLE
        }
        if (documents_list.isEmpty()) {
            ll_documents_view.visibility = View.GONE
        } else {
            ll_documents_view.visibility = View.VISIBLE
        }
        if (teamList.isEmpty()) {
            ll_add_tm.visibility = View.GONE
        } else {
            if ("solo" != Constants.CATEGORY) {
                ll_add_tm.visibility = View.VISIBLE
            }
        }
        if (entities_list.isEmpty()) {
            ll_add_entities.visibility = View.GONE
        } else {
            ll_add_entities.visibility = View.VISIBLE
        }
        if (individual_list.isEmpty()) {
            ll_individual.visibility = View.GONE
        } else {
            ll_individual.visibility = View.VISIBLE
        }
    }

    private fun hide_documents() {
        if (matter_legal == "legal" || matter_legal == "general") {
            ll_documents_view.visibility = View.VISIBLE
        } else {
            ll_documents_view.visibility = View.GONE
        }
    }

    private fun hide_all_list() {
        ischecked_project = true
        ischecked_matter = true
        ischecked_task = true
        ischecked_repetetion = true
        ischecked_time = true
        is_clicked_team = true
        is_clicked_clients = true
        is_clicked_documents = true
        is_clicked_individuals = true
        is_entity = true

        sp_time_zone.visibility = View.GONE
        sp_repetetion.visibility = View.GONE
        sp_project.visibility = View.GONE
        sp_task.visibility = View.GONE
        sp_matter_name.visibility = View.GONE
        rv_groups_view.visibility = View.GONE
        rv_clients_view.visibility = View.GONE
        sp_entity.visibility = View.GONE
        rv_individuals_view.visibility = View.GONE
        rv_documents_view.visibility = View.GONE
    }

    private fun display_selected_popup(corp: String, contains_data: Boolean) {
        if (corp == "corp") {
            if (contains_data)
                ll_corp_tm.visibility = View.VISIBLE
            else
                ll_corp_tm.visibility = View.GONE
        } else {
            if (contains_data)
                ll_entity_tm.visibility = View.VISIBLE
            else
                ll_entity_tm.visibility = View.GONE
        }
    }
}

