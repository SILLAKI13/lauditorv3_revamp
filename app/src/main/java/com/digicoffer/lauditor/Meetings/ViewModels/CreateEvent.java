package com.digicoffer.lauditor.Meetings.ViewModels;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.getAVChatUrl;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.showDatePicker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Meetings.Models.CalendarDo;
import com.digicoffer.lauditor.Meetings.Models.DocumentsDo;
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO;
import com.digicoffer.lauditor.Meetings.Models.MinutesDO;
import com.digicoffer.lauditor.Meetings.Models.RelationshipsDO;
import com.digicoffer.lauditor.Meetings.Models.TaskDo;
import com.digicoffer.lauditor.Meetings.Models.TeamDo;
import com.digicoffer.lauditor.Matter.ViewModels.Matter;
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.TimeZonesDO;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateEvent extends Fragment implements AsyncTaskCompleteListener, View.OnClickListener {
    private static String ADAPTER_TAG = "";
    private String Event_name = "", eventId = "";
    boolean isFirstTime = true;
    String event_update_scope = "UPDATE_EVENT_ONLY";
    boolean iscb_timesheet_checked = true;
    private TextView error_msg, error_msg1;
    private Dialog progressDialog;
    private String existing_task;
    TextView chk_select_all_text;
    private String existing_date;
    private String meetingRoomId = "";
    private String meetingLink = "";
    private String existing_start_time;
    private String existing_end_time;
    private boolean isExistingAllday;
    private String existing_time_zone;
    private String existing_repetetion;
    private String existing_meeting_link;
    private String existing_dialin;
    private String existing_location;
    private String existing_description;
    boolean is_linked_with_timesheet = false;
    private String existing_entityName;
    private String existing_entity_id;
    String recurring_edit_choice;
    private ArrayList<Event_Details_DO> existing_events_list = new ArrayList<>();
    String event_id;
    String time_format = "", time_value = "";
    MultiAutoCompleteTextView at_family_members;
    CardView cv_meeting_details, cv_add_clients;
    private boolean is_notify_clicked = true;
    private boolean is_timenotify = true;
    int startMinute = 0;
    final ArrayList<String> Repetetions = new ArrayList<>();
    JSONArray notification = new JSONArray();
    LinearLayout ll_client_team_members, ll_documents_list, ll_attach_document, ll_selected_entites, ll_selected_individual, ll_individual_list, ll_selected_team_members, ll_selected_entity_client, ll_selected_teammembers, ll_add_to_timesheet, ll_selected_corp_clients, ll_selected_corp_clients_view, ll_corp_client_team_members;
    private ArrayList<String> selectedValues = new ArrayList<>();
    String selected_hour_type = "";
    private boolean timeZonesTaskCompleted = false;
    CheckBox cb_all_day, cb_add_to_timesheet;
    boolean isAllDay = false;
    boolean isAddTimesheet = true;
    private boolean matterTaskCompleted = false;
    private boolean tmTaskCompleted = false;
    boolean isRecurring = false;
    final String[] AM_PM = new String[1];
    int offset;
    private Button btn_add_tm, btn_attach_document, btn_create_event, btn_add_clients, btn_assigned_clients, btn_individual;
    int start_time = 0;
    LocalTime close_time = LocalTime.ofSecondOfDay(0);
    ArrayList<RelationshipsDO> entities_list = new ArrayList<>();
    ArrayList<RelationshipsDO> Corp_client_list = new ArrayList<>();
    ArrayList<RelationshipsDO> Corp_Team_members_list = new ArrayList<>();
    ArrayList<RelationshipsDO> individual_list = new ArrayList<>();
    ArrayList<RelationshipsDO> selected_individual_list = new ArrayList<>();
    ArrayList<DocumentsDo> selected_documents_list = new ArrayList<>();
    ArrayList<RelationshipsDO> selected_entity_client_list = new ArrayList<>();
    ArrayList<RelationshipsDO> selected_entity_corp_client_list = new ArrayList<>();
    ArrayList<RelationshipsDO> new_selected_client_list = new ArrayList<>();
    ArrayList<RelationshipsDO> entity_client_list = new ArrayList<>();
    ArrayList<RelationshipsDO> entity_corp_client_list = new ArrayList<>();
    ArrayList<TeamDo> selected_tm_list = new ArrayList<>();
    private TextView at_add_tm, at_attach_document, tv_cb_all_day, at_assigned_client, at_individual, at_assigned_corp_client;
    private Spinner sp_add_team_member, sp_add_entity, sp_client_team_members;
    private TextInputEditText tv_meeting_link, tv_dialing_number, tv_location, tv_description, tv_message;
    private AppCompatButton add_notification, btn_cancel_event, btn_save_timesheet;
    TextView tv_project_name, tv_no_matter, tv_matter_name, tv_task_name, tv_date_name, tv_time_zone, tv_repetetion, tv_meeting_link_name, tv_meeting_link_names, tv_attendeee, tv_notify, tv_dial_in_number, tv_location_name, tv_description_name, tv_message_name, add_tm, add_entities, tv_assigned_client, tv_assigned_corp_client, tv_individual, tv_selected_individual, tv_selected_tm, tv_selected_document, tv_name, tv_selected_clients, tv_selected_corp_clients;
    TextView tv_sp_project, tv_sp_matter_name, tv_sp_task_name, tv_sp_time_zone, tv_sp_repetetion, Time_duration, tv_sp_entity, tv_attach_document, tv_sp_corp_clients, add_corp_clients, tv_sp_minutes;
    ListView sp_project, sp_matter_name, sp_task, sp_time_zone, sp_repetetion, sp_entity, sp_corp_clients;
    boolean ischecked_project = true, ischecked_matter = true, ischecked_task = true, ischecked_time = true, ischecked_repetetion = true, is_notify = true, is_entity = true, is_corp = true, is_clicked_team = true, is_clicked_clients = true, is_clicked_corp_clients = true, is_clicked_individuals = true, is_clicked_documents = true;
    AppCompatButton tv_event_creation_date, tv_event_start_time, tv_event_end_time;
    //    ScrollView scrollView;
    private String selected_project;
    private String selected_task;
    private String entity_id = "";
    String corp_client_id = "";
    String corp_client_name;
    String entity_name;
    Hashtable<String, Integer> timesPosHash = new Hashtable<>();
    ArrayList<TimeZonesDO> timeZonesList = new ArrayList<TimeZonesDO>();
    private LinearLayout ll_project, ll_selected_documents, ll_matter_name, ll_message, ll_task, ll_repetetion, ll_add_notification, ll_add_tm, ll_add_entities, ll_individual, ll_documents_view, ll_assign_clients, ll_add_corp_clients, ll_corp_clients, ll_corp_tm, ll_entity_tm;
    LinearLayoutCompat ll_time;
    ImageView img_dropdown_sp_project, img_dropdown_sp_mattername, img_dropdown_sp_task, img_dropdown_sp_timezone, img_dropdown_sp_repetetion, img_dropdown_sp_entity, img_dropdown_sp_corp_clients;
    ImageView img_clear_sp_project, img_clear_sp_mattername, img_clear_sp_task, img_clear_sp_timezone, img_clear_sp_repetetion, img_clear_sp_entity, img_clear_sp_corp_clients;
    LinearLayout ll_sp_project, ll_sp_mattername, ll_sp_task, ll_sp_timezone, ll_sp_repetetion, ll_sp_entity, ll_sp_corp_clients;
    ArrayList<CalendarDo> projectList = new ArrayList<>();
    ArrayList<ViewMatterModel> matterList = new ArrayList<>();
    ArrayList<DocumentsDo> documents_list = new ArrayList<>();
    ArrayList<TeamDo> teamList = new ArrayList<>();
    ArrayList<TaskDo> legalTaksList = new ArrayList<>();
    private String repeat_interval;
    private String event_creation_date;
    private String event_starting_date;
    private String event_end_time;
    private String timezone_location;
    private String matter_id;
    private String matter_name;
    private String matter_legal = "";
    private Meetings meetings;
    //.
    // Add these as class-level fields in CreateEvent.java:
    CommonSpinnerAdapter spinner_adapter;      // for sp_project
    CommonSpinnerAdapter matterSpinnerAdapter; // for sp_matter_name
    CommonSpinnerAdapter taskSpinnerAdapter;   // for sp_task
    CommonSpinnerAdapter timezoneAdapter;      // for sp_time_zone
    CommonSpinnerAdapter repetitionAdapter;    // for sp_repetetion
    CommonSpinnerAdapter entityAdapter;        // for sp_entity
    CommonSpinnerAdapter corpAdapter;          // for sp_corp_clients
    //..
    ArrayList<MinutesDO> minutes_list = new ArrayList<>();
    RecyclerView rv_groups_view, rv_clients_view, rv_individuals_view, rv_documents_view, rv_corp_client_view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        callTimeZoneWebservice();
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("WrongViewCast")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_event, container, false);
        sp_project = view.findViewById(R.id.sp_project1);
//        scrollView = view.findViewById(R.id.scrollView);
        ll_time = view.findViewById(R.id.ll_time);
        chk_select_all_text = view.findViewById(R.id.tv_add_to_timesheet);
        chk_select_all_text.setText(R.string.add_to_timesheet);
        cb_add_to_timesheet = view.findViewById(R.id.cb_add_to_timesheet);
        ll_add_to_timesheet = view.findViewById(R.id.ll_add_to_timesheet);
        ll_add_to_timesheet.setVisibility(GONE);
        sp_matter_name = view.findViewById(R.id.sp_matter_name);
        at_individual = view.findViewById(R.id.at_individual);
        at_individual.setHint(R.string.select_individuals);
        sp_task = view.findViewById(R.id.sp_task);
        sp_task.setVisibility(GONE);
        ll_matter_name = view.findViewById(R.id.ll_matter_name);
        ll_matter_name.setVisibility(GONE);
        at_add_tm = view.findViewById(R.id.at_add_groups);
        at_add_tm.setHint(R.string.select_team_members);
        add_tm = view.findViewById(R.id.add_groups);
        add_tm.setText(R.string.add_team_members);
        btn_create_event = view.findViewById(R.id.btn_create_event);
        btn_create_event.setOnClickListener(this);
        btn_create_event.setText(R.string.save);
        ll_message = view.findViewById(R.id.ll_message);
        tv_message = view.findViewById(R.id.tv_message);
        tv_message_name = view.findViewById(R.id.tv_message_name);
        tv_message_name.setText(R.string.message);
        tv_message.setHint(R.string.message);
        at_attach_document = view.findViewById(R.id.at_attach_document);
        at_attach_document.setHint(R.string.select_documents);

        tv_cb_all_day = view.findViewById(R.id.tv_cb_all_day);
        tv_cb_all_day.setText(R.string.all_day);
        cb_all_day = view.findViewById(R.id.cb_all_day);

        btn_cancel_event = view.findViewById(R.id.btn_cancel_event);
        Time_duration = view.findViewById(R.id.Time_duration);
        Time_duration.setText(R.string.duration);
//        Time_duration.setVisibility(View.GONE);
        tv_project_name = view.findViewById(R.id.tv_project_name);
        tv_project_name.setText(R.string.event_type);
        ll_sp_project = view.findViewById(R.id.tv_sp_project);
        tv_sp_project = ll_sp_project.findViewById(R.id.tv_spinner_view);
        tv_sp_project.setHint(R.string.select_event_type);
        img_clear_sp_project = ll_sp_project.findViewById(R.id.img_clear_icon);
        img_dropdown_sp_project = ll_sp_project.findViewById(R.id.img_dropdown_icon);
//        list_scroll_project = view.findViewById(R.id.list_scroll_project);
        sp_project.setVisibility(GONE);

        tv_no_matter = view.findViewById(R.id.tv_no_matter);
        tv_no_matter.setText(R.string.no_matters_found);
        tv_no_matter.setVisibility(GONE);
        tv_matter_name = view.findViewById(R.id.tv_matter_name);
        tv_matter_name.setText(R.string.matter_name);
        ll_sp_mattername = view.findViewById(R.id.tv_sp_matter_name);
        tv_sp_matter_name = ll_sp_mattername.findViewById(R.id.tv_spinner_view);
        tv_sp_matter_name.setHint(R.string.select_matter_name);
        tv_sp_project.setHint(R.string.select_event_type);
        img_clear_sp_mattername = ll_sp_mattername.findViewById(R.id.img_clear_icon);
        img_dropdown_sp_mattername = ll_sp_mattername.findViewById(R.id.img_dropdown_icon);
//        list_scroll_matter = view.findViewById(R.id.list_scroll_matter);
        sp_matter_name.setVisibility(GONE);
        sp_matter_name.setNestedScrollingEnabled(true);
//        sp_project.setNestedScrollingEnabled(true);
        tv_task_name = view.findViewById(R.id.tv_task_name);
        tv_task_name.setText(R.string.task);
        ll_sp_task = view.findViewById(R.id.tv_sp_task_name);
        tv_sp_task_name = ll_sp_task.findViewById(R.id.tv_spinner_view);
        tv_sp_task_name.setHint(R.string.select_task);
        img_clear_sp_task = ll_sp_task.findViewById(R.id.img_clear_icon);
        img_dropdown_sp_task = ll_sp_task.findViewById(R.id.img_dropdown_icon);

        sp_time_zone = view.findViewById(R.id.sp_time_zone);
        sp_time_zone.setVisibility(GONE);
        ll_sp_timezone = view.findViewById(R.id.tv_sp_time_zone);
        tv_sp_time_zone = ll_sp_timezone.findViewById(R.id.tv_spinner_view);
        tv_sp_time_zone.setHint(R.string.select_timezone);
        img_clear_sp_timezone = ll_sp_timezone.findViewById(R.id.img_clear_icon);
        img_dropdown_sp_timezone = ll_sp_timezone.findViewById(R.id.img_dropdown_icon);

        ll_sp_repetetion = view.findViewById(R.id.tv_sp_repetetion);
        tv_sp_repetetion = ll_sp_repetetion.findViewById(R.id.tv_spinner_view);
        tv_sp_repetetion.setHint(R.string.select_repetition);
        img_clear_sp_repetetion = ll_sp_repetetion.findViewById(R.id.img_clear_icon);
        img_dropdown_sp_repetetion = ll_sp_repetetion.findViewById(R.id.img_dropdown_icon);

        tv_sp_repetetion.setText(R.string.none);
        img_clear_sp_repetetion.setVisibility(VISIBLE);
        img_dropdown_sp_repetetion.setVisibility(GONE);
        sp_repetetion = view.findViewById(R.id.sp_repetetion);
        sp_repetetion.setVisibility(GONE);
        tv_date_name = view.findViewById(R.id.tv_date_name);
        tv_date_name.setText(R.string.date);
        tv_time_zone = view.findViewById(R.id.tv_time_zone);
        tv_time_zone.setText(R.string.time_zone);
        tv_repetetion = view.findViewById(R.id.tv_repetetion);
        tv_repetetion.setText(R.string.repetition);
        tv_meeting_link_name = view.findViewById(R.id.tv_meeting_link_name);
        tv_meeting_link_name.setText(R.string.meeting_link);

        tv_attendeee = view.findViewById(R.id.tv_attendeee);
        tv_attendeee.setText("Attendees");
        tv_notify = view.findViewById(R.id.tv_notify);
        tv_notify.setText("Notify Me");
        tv_meeting_link_names = view.findViewById(R.id.tv_meeting_link_names);
        tv_meeting_link_names.setText("Meeting Details");
        tv_dial_in_number = view.findViewById(R.id.tv_dial_in_number);
        tv_dial_in_number.setText(R.string.dial_in_number);
        tv_location_name = view.findViewById(R.id.tv_location_name);
        tv_location_name.setText(R.string.location);
        tv_description_name = view.findViewById(R.id.tv_description_name);
        tv_description_name.setText(R.string.meeting_agenda);

        ll_sp_corp_clients = view.findViewById(R.id.ll_sp_corp_clients);
        tv_sp_corp_clients = ll_sp_corp_clients.findViewById(R.id.tv_spinner_view);
        tv_sp_corp_clients.setHint(R.string.select_corporate_client);
        img_clear_sp_corp_clients = ll_sp_corp_clients.findViewById(R.id.img_clear_icon);
        img_dropdown_sp_corp_clients = ll_sp_corp_clients.findViewById(R.id.img_dropdown_icon);
        sp_corp_clients = view.findViewById(R.id.sp_corp_clients);
        sp_corp_clients.setVisibility(GONE);

        ll_sp_entity = view.findViewById(R.id.tv_sp_entities);
        tv_sp_entity = ll_sp_entity.findViewById(R.id.tv_spinner_view);
        tv_sp_entity.setHint(R.string.select_entity);
        img_clear_sp_entity = ll_sp_entity.findViewById(R.id.img_clear_icon);
        img_dropdown_sp_entity = ll_sp_entity.findViewById(R.id.img_dropdown_icon);

        sp_entity = view.findViewById(R.id.sp_entities);
        sp_entity.setVisibility(GONE);
//       isAllDay = cb_all_day.isChecked();
        cv_meeting_details = view.findViewById(R.id.cv_meeting_details);
        cv_add_clients = view.findViewById(R.id.cv_add_clients);
        cv_add_clients.setVisibility(GONE);

        loadcheckboxData();
        ll_attach_document = view.findViewById(R.id.ll_attach_document);
        btn_attach_document = view.findViewById(R.id.btn_attach_document);
        btn_attach_document.setOnClickListener(this);
        ll_add_notification = view.findViewById(R.id.ll_add_notification);

        rv_groups_view = view.findViewById(R.id.rv_groups_view);
        rv_groups_view.setBackground(getContext().getDrawable(R.drawable.rectangle_light_grey_bg));
        rv_groups_view.setVisibility(GONE);
        rv_corp_client_view = view.findViewById(R.id.rv_corp_clients_view);
        rv_corp_client_view.setBackground(getContext().getDrawable(R.drawable.rectangle_light_grey_bg));
        rv_corp_client_view.setVisibility(GONE);
        rv_clients_view = view.findViewById(R.id.rv_clients_view);
        rv_clients_view.setVisibility(GONE);
        rv_clients_view.setBackground(getContext().getDrawable(R.drawable.rectangle_light_grey_bg));
        rv_individuals_view = view.findViewById(R.id.rv_individuals_view);
        rv_individuals_view.setVisibility(GONE);
        rv_individuals_view.setBackground(getContext().getDrawable(R.drawable.rectangle_light_grey_bg));
        rv_documents_view = view.findViewById(R.id.rv_documents_view);
        rv_documents_view.setVisibility(GONE);
        rv_documents_view.setBackground(getContext().getDrawable(R.drawable.rectangle_light_grey_bg));


        add_entities = view.findViewById(R.id.add_entities);
        add_entities.setText(R.string.add_entity);
        add_corp_clients = view.findViewById(R.id.add_corp_clients);
        add_corp_clients.setText(R.string.add_corporate_client);

        add_notification = view.findViewById(R.id.add_notification);
        add_notification.setOnClickListener(this);
        add_notification.setText("+ Add Notification");
        ll_documents_list = view.findViewById(R.id.ll_documents_list);
        at_assigned_client = view.findViewById(R.id.at_assigned_client);
        at_assigned_client.setHint(R.string.select_client_team_members);
        at_assigned_corp_client = view.findViewById(R.id.at_assigned_corp_client);
        at_assigned_corp_client.setHint(R.string.select_corporate_team_members);
        tv_assigned_corp_client = view.findViewById(R.id.tv_assigned_corp_client);
        tv_assigned_corp_client.setText(R.string.corporate_team_members);
        tv_assigned_client = view.findViewById(R.id.tv_assigned_client);
        tv_assigned_client.setText(R.string.client_team_members);
        tv_individual = view.findViewById(R.id.tv_individual);
        tv_individual.setText(R.string.add_individuals);
        tv_selected_clients = view.findViewById(R.id.tv_selected_clients);
        tv_selected_clients.setText(R.string.selected_entities);
        tv_selected_corp_clients = view.findViewById(R.id.tv_selected_corp_clients);
        tv_selected_corp_clients.setText(R.string.selected_corporate_tm);
        tv_selected_individual = view.findViewById(R.id.tv_selected_individual);
        tv_selected_individual.setText(R.string.selected_individuals);
        tv_selected_tm = view.findViewById(R.id.tv_selected_tm);
        tv_selected_tm.setText(R.string.selected_client_team_members);
        tv_selected_document = view.findViewById(R.id.tv_selected_document);
        tv_selected_document.setText(R.string.selected_documents);

        tv_name = view.findViewById(R.id.tv_name);
        tv_name.setText(R.string.selected_tm);
        tv_attach_document = view.findViewById(R.id.tv_attach_document);
        tv_attach_document.setText(R.string.add_documents);

        btn_add_tm = view.findViewById(R.id.btn_add_groups);
        ll_selected_teammembers = view.findViewById(R.id.selected_groups);
        btn_individual = view.findViewById(R.id.btn_individual);
        ll_selected_individual = view.findViewById(R.id.selected_individual);
        ll_individual_list = view.findViewById(R.id.ll_individual_list);
        btn_individual.setOnClickListener(this);
        btn_add_tm.setOnClickListener(this);
//        btn_add_clients = view.findViewById(R.id.btn_add_clients);
//        btn_add_clients.setOnClickListener(this);
        ll_selected_entity_client = view.findViewById(R.id.selected_tm);
        ll_client_team_members = view.findViewById(R.id.ll_client_team_members);
        ll_corp_client_team_members = view.findViewById(R.id.ll_corp_client_team_members);
        ll_selected_entites = view.findViewById(R.id.ll_selected_entites);
        ll_selected_corp_clients = view.findViewById(R.id.ll_selected_corp_clients);
        ll_selected_corp_clients_view = view.findViewById(R.id.ll_selected_corp_clients_view);
        ll_selected_team_members = view.findViewById(R.id.ll_selected_team_members);
        btn_assigned_clients = view.findViewById(R.id.btn_assigned_clients);
        btn_assigned_clients.setOnClickListener(this);

        //Invisible Add Button in all selecting member field.
        btn_attach_document.setVisibility(GONE);
        btn_individual.setVisibility(GONE);
        btn_add_tm.setVisibility(GONE);
        btn_assigned_clients.setVisibility(GONE);

        tv_event_creation_date = view.findViewById(R.id.tv_event_creation_date);
//        tv_event_creation_date.setOnClickListener(this);
        tv_event_creation_date.setHint(R.string.date);
        tv_event_start_time = view.findViewById(R.id.tv_event_start_time);
        tv_event_start_time.setOnClickListener(this);
        tv_event_start_time.setHint("");
        tv_event_end_time = view.findViewById(R.id.tv_event_end_time);
        tv_event_end_time.setOnClickListener(this);
        tv_event_end_time.setHint("");
        ll_selected_documents = view.findViewById(R.id.selected_attached_documents);
        tv_meeting_link = view.findViewById(R.id.tv_meeting_link);

        tv_meeting_link.setHint(R.string.meeting_link);
        tv_meeting_link.setText(meetingLink);
        tv_dialing_number = view.findViewById(R.id.tv_dialing_number);
        tv_dialing_number.setHint(R.string.dial_in_number);
        tv_dialing_number.setInputType(InputType.TYPE_CLASS_NUMBER);
        tv_location = view.findViewById(R.id.tv_location);
        tv_location.setHint(R.string.location);
        tv_description = view.findViewById(R.id.tv_description);
        tv_description.setHint(R.string.meeting_agenda);
        ll_project = view.findViewById(R.id.ll_project);

        ll_task = view.findViewById(R.id.ll_task);
        ll_task.setVisibility(GONE);
        ll_repetetion = view.findViewById(R.id.ll_repetetion);
        ll_repetetion.setVisibility(VISIBLE);
        ll_add_tm = view.findViewById(R.id.ll_add_groups);
        ll_assign_clients = view.findViewById(R.id.ll_assign_clients);
        ll_assign_clients.setVisibility(GONE);
        ll_corp_tm = view.findViewById(R.id.ll_corp_tm);
        ll_entity_tm = view.findViewById(R.id.ll_entity_tm);
        ll_corp_clients = view.findViewById(R.id.ll_corp_clients);
        ll_corp_clients.setVisibility(GONE);
        ll_add_entities = view.findViewById(R.id.ll_add_entities);
        ll_add_corp_clients = view.findViewById(R.id.ll_add_corp_clients);

//        display_check_list();
//        cv_add_clients.setVisibility(View.VISIBLE);
//        ll_assign_clients = view.findViewById(R.id.ll_assign_team_members);

        ll_documents_view = view.findViewById(R.id.ll_documents_view);
        ll_individual = view.findViewById(R.id.ll_individual);

        cv_add_clients.setVisibility(GONE);
        ll_add_tm.setVisibility(GONE);
        ll_add_entities.setVisibility(GONE);
        ll_individual.setVisibility(GONE);
        ll_documents_view.setVisibility(VISIBLE);
        tv_meeting_link.addTextChangedListener(new Validation(tv_meeting_link));
        tv_dialing_number.addTextChangedListener(new Validation(tv_dialing_number));
        tv_location.addTextChangedListener(new Validation(tv_location));
        tv_description.addTextChangedListener(new Validation(tv_description));
        tv_message.addTextChangedListener(new Validation(tv_message));
        tv_description.setMaxLines(10);
        tv_message.setMaxLines(10);
        //Notification popup display with 1 value
        if (!Objects.equals(Constants.is_meeting, "Edit")) {
            if (selectedValues.isEmpty()) {
                is_notify_clicked = false;
                NotificationPopup();
            }
        }

        //Clear icons :
        img_clear_sp_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.DisplaySpinnerView(sp_task, tv_sp_task_name, selected_task, img_dropdown_sp_task, img_clear_sp_task, false, taskSpinnerAdapter, "Search Task");
                ischecked_task = true;
            }
        });
        img_clear_sp_project.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.DisplaySpinnerView(sp_project, tv_sp_project, selected_project, img_dropdown_sp_project, img_clear_sp_project, false, spinner_adapter, "Search Project");
                ll_task.setVisibility(GONE);
                selected_task = "";
                tv_sp_task_name.setText("");
                matter_name = "";
                tv_sp_matter_name.setText("");
                load_clear_list();
                hide_all_list();
                clear_selected_list();
                ll_matter_name.setVisibility(GONE);
                tv_no_matter.setVisibility(GONE);
                hide_members();
//                loadProjectData(selected_project);
                ll_add_to_timesheet.setVisibility(GONE);
                ischecked_project = true;
            }
        });
        img_clear_sp_timezone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.DisplaySpinnerView(sp_time_zone, tv_sp_time_zone, timezone_location, img_dropdown_sp_timezone, img_clear_sp_timezone, false, timezoneAdapter, "Search TimeZone");
                ischecked_time = true;
            }
        });
        img_clear_sp_repetetion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.DisplaySpinnerView(sp_repetetion, tv_sp_repetetion, repeat_interval, img_dropdown_sp_repetetion, img_clear_sp_repetetion, false, repetitionAdapter, "Search Repetition");
                ischecked_repetetion = true;
            }
        });
        img_clear_sp_mattername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.DisplaySpinnerView(sp_matter_name, tv_sp_matter_name, matter_name, img_dropdown_sp_mattername, img_clear_sp_mattername, false, matterSpinnerAdapter, "Search Matter");
                load_clear_list();
                cv_add_clients.setVisibility(GONE);
//                hide_all_list();
                ischecked_matter = true;
            }
        });
        img_clear_sp_corp_clients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                corp_client_id = "";
                AndroidUtils.DisplaySpinnerView(sp_corp_clients, tv_sp_corp_clients, corp_client_id, img_dropdown_sp_corp_clients, img_clear_sp_corp_clients, false, corpAdapter, "Search Corp Client");
//                ll_corp_clients.setVisibility(View.GONE);
                is_corp = true;
            }
        });
        img_clear_sp_entity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.DisplaySpinnerView(sp_entity, tv_sp_entity, entity_id, img_dropdown_sp_entity, img_clear_sp_entity, false, entityAdapter, "Search Entity");
                is_entity = true;
            }
        });
        ll_sp_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (taskSpinnerAdapter == null) {
                    AndroidUtils.display_listview(ischecked_task, sp_task);
                    ischecked_task = !ischecked_task;
                    return;
                }
                boolean isVisible = sp_task.getVisibility() == VISIBLE;
                AndroidUtils.DisplaySpinnerView(sp_task, tv_sp_task_name,
                        tv_sp_task_name.getText().toString(),
                        img_dropdown_sp_task, img_clear_sp_task,
                        !isVisible, taskSpinnerAdapter, "Search Task");
            }
        });
        // REPLACE ll_sp_project click listener:
        ll_sp_project.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinner_adapter == null) {
                    AndroidUtils.display_listview(ischecked_project, sp_project);
                    ischecked_project = !ischecked_project;
                    return;
                }
                boolean isVisible = sp_project.getVisibility() == VISIBLE;
                AndroidUtils.DisplaySpinnerView(sp_project, tv_sp_project,
                        tv_sp_project.getText().toString(),
                        img_dropdown_sp_project, img_clear_sp_project,
                        !isVisible, spinner_adapter, "Search Project");
            }
        });

// REPLACE ll_sp_mattername click listener:
        ll_sp_mattername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (matterSpinnerAdapter == null) {
                    AndroidUtils.display_listview(ischecked_matter, sp_matter_name);
                    ischecked_matter = !ischecked_matter;
                    return;
                }
                boolean isVisible = sp_matter_name.getVisibility() == VISIBLE;
                AndroidUtils.DisplaySpinnerView(sp_matter_name, tv_sp_matter_name,
                        tv_sp_matter_name.getText().toString(),
                        img_dropdown_sp_mattername, img_clear_sp_mattername,
                        !isVisible, matterSpinnerAdapter, "Search Matter");
            }
        });

// REPLACE ll_sp_timezone click listener:
        ll_sp_timezone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timezoneAdapter == null) {
                    AndroidUtils.display_listview(ischecked_time, sp_time_zone);
                    ischecked_time = !ischecked_time;
                    return;
                }
                boolean isVisible = sp_time_zone.getVisibility() == VISIBLE;
                AndroidUtils.DisplaySpinnerView(sp_time_zone, tv_sp_time_zone,
                        tv_sp_time_zone.getText().toString(),
                        img_dropdown_sp_timezone, img_clear_sp_timezone,
                        !isVisible, timezoneAdapter, "Search TimeZone");
            }
        });

// REPLACE ll_sp_repetetion click listener:
        ll_sp_repetetion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repetitionAdapter == null) {
                    AndroidUtils.display_listview(ischecked_repetetion, sp_repetetion);
                    ischecked_repetetion = !ischecked_repetetion;
                    return;
                }
                boolean isVisible = sp_repetetion.getVisibility() == VISIBLE;
                AndroidUtils.DisplaySpinnerView(sp_repetetion, tv_sp_repetetion,
                        tv_sp_repetetion.getText().toString(),
                        img_dropdown_sp_repetetion, img_clear_sp_repetetion,
                        !isVisible, repetitionAdapter, "Search Repetetion");
            }
        });
        ll_sp_corp_clients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (corpAdapter == null || Corp_client_list.isEmpty()) {
                    if (is_corp) {
                        if (!Corp_client_list.isEmpty()) {
                            sp_corp_clients.setVisibility(VISIBLE);
                        }
                    } else {
                        sp_corp_clients.setVisibility(GONE);
                    }
                    is_corp = !is_corp;
                    return;
                }
                boolean isVisible = sp_corp_clients.getVisibility() == VISIBLE;
                AndroidUtils.DisplaySpinnerView(sp_corp_clients, tv_sp_corp_clients,
                        tv_sp_corp_clients.getText().toString(),
                        img_dropdown_sp_corp_clients, img_clear_sp_corp_clients,
                        !isVisible, corpAdapter, "Search Corp Client");
                is_corp = !is_corp;
            }
        });
        ll_sp_entity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (entityAdapter == null || entities_list.isEmpty()) {
                    if (is_entity) {
                        if (!entities_list.isEmpty()) {
                            sp_entity.setVisibility(VISIBLE);
                        }
                    } else {
                        sp_entity.setVisibility(GONE);
                    }
                    is_entity = !is_entity;
                    return;
                }
                boolean isVisible = sp_entity.getVisibility() == VISIBLE;
                AndroidUtils.DisplaySpinnerView(sp_entity, tv_sp_entity,
                        tv_sp_entity.getText().toString(),
                        img_dropdown_sp_entity, img_clear_sp_entity,
                        !isVisible, entityAdapter, "Search Entity");
                is_entity = !is_entity;
            }
        });
        at_add_tm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_clicked_team) {
                    //Recycler view is not displaying after Selecting a event type as Legal matter/General matter.
                    rv_groups_view.setVisibility(VISIBLE);
                    TeamMembersPopup();
//                    if (!(matterList.isEmpty())) {
//                        TeamMembersPopup();
//                    }
                } else {
                    rv_groups_view.setVisibility(GONE);
                }
                is_clicked_team = !is_clicked_team;
            }
        });
        btn_cancel_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadClearedLists();
                selectedValues.clear();
                meetings.loadView();
            }
        });
        at_individual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_clicked_individuals) {
                    rv_individuals_view.setVisibility(VISIBLE);
                    load_individual_Popup();
                } else {
                    rv_individuals_view.setVisibility(GONE);
                }
                is_clicked_individuals = !is_clicked_individuals;
            }
        });
        at_assigned_corp_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_clicked_corp_clients) {
                    rv_corp_client_view.setVisibility(VISIBLE);
                    loadentity_corp_clients_popup();
                } else {
                    rv_corp_client_view.setVisibility(GONE);
                }
                is_clicked_corp_clients = !is_clicked_corp_clients;
            }
        });
        at_assigned_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_clicked_clients) {
                    rv_clients_view.setVisibility(VISIBLE);
                    loadEntityClientPopup();
                } else {
                    rv_clients_view.setVisibility(GONE);
                }
                is_clicked_clients = !is_clicked_clients;
            }
        });
        at_attach_document.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_clicked_documents) {
                    rv_documents_view.setVisibility(VISIBLE);
                    load_documents_Popup();
                } else {
                    rv_documents_view.setVisibility(GONE);
                }
                is_clicked_documents = !is_clicked_documents;
            }
        });
        projectList.clear();
        if (Constants.ROLE.equals("AAM")) {
            projectList.add(new CalendarDo("Overhead"));
            projectList.add(new CalendarDo("Others"));
            projectList.add(new CalendarDo("Reminders"));
        } else {
            projectList.add(new CalendarDo("Legal Matter"));
            projectList.add(new CalendarDo("General Matter"));
            projectList.add(new CalendarDo("Overhead"));
            projectList.add(new CalendarDo("Others"));
            projectList.add(new CalendarDo("Reminders"));
        }
        legalTaksList.clear();

//        callTimeZoneWebservice();
        spinner_adapter = new CommonSpinnerAdapter((Activity) getContext(), projectList);
        sp_project.setAdapter(spinner_adapter);
        //..
        AndroidUtils.LoadList(sp_project, getContext(), projectList.size(), true);
        //..
        sp_project.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CalendarDo selectedProjectItem = (CalendarDo) parent.getItemAtPosition(position);
                selected_project = selectedProjectItem.getProjectName();
                AndroidUtils.DisplaySpinnerView(sp_project, tv_sp_project, selected_project, img_dropdown_sp_project, img_clear_sp_project, false, spinner_adapter, "Search Project");
                ischecked_project = true;
                ll_task.setVisibility(VISIBLE);
                selected_task = "";
                tv_sp_task_name.setText("");
                at_assigned_corp_client.setText("");
                matter_name = "";
                tv_sp_matter_name.setText("");
                load_clear_list();
                hide_all_list();
                clear_selected_list();
                loadProjectData(selected_project);
            }
        });
        loadRepetetions();
        // PRESET TODAY'S DATE
        tv_event_creation_date.setText(
                new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new Date())
        );
        setStartAndEndTime();
        // Call for BOTH Create and Edit
        callCreateMeetingLinkWebservice();
        if (Constants.isFromNotification) {
            handleNotificationNavigation();
        }
        tv_event_creation_date.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                updateMeetingLinkFromCurrentValues();
            }
        });
        return view;
    }

    private void handleNotificationNavigation() {
        Bundle bundle = Constants.notificationBundle;
        String route = bundle.getString(Constants.NavKeys.ROUTE_NAME);
        if (route != null) {
            eventId = bundle.getString(Constants.NavKeys.EVENT_ID, "");
        }
        Constants.isFromNotification = false;
        Constants.notificationBundle.clear();
        // Get highlight IDs first (common for all routes)
//        callEventDetailsWebservice();
    }

    private void loadcheckboxData() {
        cb_all_day.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) { // All Day mode selected
                    ll_time.setVisibility(GONE);
                    selectedValues.clear();
                    ll_add_notification.removeAllViews();
                    display_duration("00:00", "23:59");
                    isAllDay = true;
                } else { // All Day mode unchecked
                    ll_add_notification.removeAllViews();
                    selectedValues.clear();
                    ll_time.setVisibility(VISIBLE);
                    isAllDay = false;

                    // Set start and end times
                    setStartAndEndTime();
                }
            }
        });


        cb_add_to_timesheet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cb_add_to_timesheet.setChecked(iscb_timesheet_checked);
                iscb_timesheet_checked = !iscb_timesheet_checked;
                isAddTimesheet = cb_add_to_timesheet.isChecked();
            }
        });
    }

    private void callTimeZoneWebservice() {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postData = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "event/timezones", "TIMEZONES", postData.toString());
        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    private void callCreateMeetingLinkWebservice() {
        try {
            String meetingLinkUrl;
            if (Constants.ISPRODUCTION) {
                meetingLinkUrl = "https://avchat.digicoffer.com/api/v1/create-meeting-link";
            } else if (Constants.IS_STAGING) {
                meetingLinkUrl = "https://staging.api.avchat.digicoffer.com/api/v1/create-meeting-link";
            } else {
                meetingLinkUrl = "https://devapi.testavchat.digicoffer.com/api/v1/create-meeting-link";
            }
            Log.d("AVChat_MeetingLink", "Calling URL: " + meetingLinkUrl);
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postData = new JSONObject();

            WebServiceHelper.callHttpWebService(
                    this,
                    getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    meetingLinkUrl,
                    "CREATE_MEETING_LINK",
                    postData.toString()
            );

        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            Log.e("AVChat_MeetingLink", "Exception: " + e.getMessage());
            e.fillInStackTrace();
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
    private void setStartAndEndTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Round up to the next 15-minute interval
        minute = ((minute / 15) + 1) * 15;
        if (minute == 60) {
            minute = 0;
            hour++;
            if (hour == 24) {
                hour = 0; // Reset to midnight
            }
        }

        // Set start time
        String currentTime = String.format("%02d:%02d", hour, minute);
        tv_event_start_time.setText(currentTime);

        // Set end time 30 minutes after start time
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.add(Calendar.MINUTE, 30);

        String endTime = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        tv_event_end_time.setText(endTime);

        // Update duration
        updateMeetingLinkFromCurrentValues();
        display_duration(currentTime, endTime);
    }


    private void showCustomTimePicker(final boolean isStart) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_time_picker, null);

        NumberPicker npHour = view.findViewById(R.id.np_hour);
        NumberPicker npMinute = view.findViewById(R.id.np_minute);
        NumberPicker npAmPm = view.findViewById(R.id.np_am_pm);
        Button btn_ok = view.findViewById(R.id.btn_ok);
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        ImageView iv_close = view.findViewById(R.id.iv_close);


        Calendar calendar = Calendar.getInstance();
        boolean is24Hour = DateFormat.is24HourFormat(getContext());
        int hour, minute;

        // Get existing time from TextView
        TextView targetTextView = isStart ? tv_event_start_time : tv_event_end_time;
        if (targetTextView.getText() != null && !targetTextView.getText().toString().isEmpty()) {
            try {
                String[] timeParts = targetTextView.getText().toString().split(":");
                hour = Integer.parseInt(timeParts[0]);
                minute = Integer.parseInt(timeParts[1]);
            } catch (Exception e) {
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);
            }
        } else {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }

        // Round minute to nearest 15-minute interval
//        minute = (int) ((Math.round(minute / 15.0)) * 15);
//        if (minute == 60) {
//            minute = 0;
//            hour++; // Increment hour if it overflows
//        }
        minute = (int) ((Math.round(minute / 15.0)) * 15);
        if (minute == 60) {
            minute = 0;
            hour++; // Increment hour if it overflows
        }

        // Setup hour picker
        if (is24Hour) {
            npHour.setMinValue(0);
            npHour.setMaxValue(23);
        } else {
            npHour.setMinValue(1);
            npHour.setMaxValue(12);
            npAmPm.setVisibility(VISIBLE);
            npAmPm.setMinValue(0);
            npAmPm.setMaxValue(1);
            npAmPm.setDisplayedValues(new String[]{"AM", "PM"});
            npAmPm.setValue(hour >= 12 ? 1 : 0); // AM = 0, PM = 1

            if (hour > 12) hour -= 12;
            else if (hour == 0) hour = 12;
        }
        npHour.setValue(hour);
        npHour.setWrapSelectorWheel(false);

        // Setup minutes picker (15-minute intervals)
        npMinute.setMinValue(0);
        npMinute.setMaxValue(3); // Update max value to match the number of items in the array
        String[] minuteValues = {"00", "15", "30", "45"};
//        String[] minuteValues = {"00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"};
        npMinute.setDisplayedValues(minuteValues);
        npMinute.setValue(minute / 15); // Adjusted for 5-minute intervals
        npMinute.setWrapSelectorWheel(false);

        AlertDialog dialog = builder.create();
        dialog.setView(view);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.rectangular_white_background));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedHour = npHour.getValue();
                int selectedMinute = Integer.parseInt(minuteValues[npMinute.getValue()]);
//
//                if (!is24Hour) {
//                    if (npAmPm.getValue() == 1) { // PM selected
//                        selectedHour += 12;
//                        if (selectedHour == 24) selectedHour = 0; // Midnight case
//                    }
//                }

                if (!is24Hour) {
                    if (npAmPm.getValue() == 1) { // PM selected
                        if (selectedHour != 12) selectedHour += 12;
                    } else {
                        if (selectedHour == 12) selectedHour = 0; // Midnight
                    }
                }

                String time = String.format("%02d:%02d", selectedHour, selectedMinute);

                if (isStart) {
                    start_time = selectedHour;
                    startMinute = selectedMinute;
                    tv_event_start_time.setText(time);

                    // Set default end time 30 minutes later
                    Calendar startCal = Calendar.getInstance();
                    startCal.set(Calendar.HOUR_OF_DAY, selectedHour);
                    startCal.set(Calendar.MINUTE, selectedMinute);

                    Calendar endCal = (Calendar) startCal.clone();
                    endCal.add(Calendar.MINUTE, 30);

                    String endTimeFormatted = String.format("%02d:%02d", endCal.get(Calendar.HOUR_OF_DAY), endCal.get(Calendar.MINUTE));
                    tv_event_end_time.setText(endTimeFormatted);
                } else {
                    int startTimeMinutes = start_time * 60 + startMinute;
                    int endTimeMinutes = selectedHour * 60 + selectedMinute;

                    // Ensure end time is after start time
                    if (start_time == selectedHour && endTimeMinutes <= startTimeMinutes) {
                        Calendar startCal = Calendar.getInstance();
                        startCal.set(Calendar.HOUR_OF_DAY, start_time);
                        startCal.set(Calendar.MINUTE, startMinute);

                        Calendar endCal = (Calendar) startCal.clone();
                        endCal.add(Calendar.MINUTE, 15); // Default to 15 minutes later

                        String endTimeFormatted = String.format("%02d:%02d",
                                endCal.get(Calendar.HOUR_OF_DAY), endCal.get(Calendar.MINUTE));
                        tv_event_end_time.setText(endTimeFormatted);
                    } else {
                        tv_event_end_time.setText(time);
                    }
                }
                display_duration(tv_event_start_time.getText().toString(), tv_event_end_time.getText().toString());
                updateMeetingLinkFromCurrentValues(); // ← add this
                dialog.dismiss();
            }
        });
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
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.tv_event_creation_date:
//                tv_event_creation_date.setOnClickListener(v -> showDatePicker(tv_event_creation_date, true));
//                break;
            case R.id.tv_event_start_time:
                if (tv_event_creation_date.getText().equals("")) {
                    AndroidUtils.showAlert("Please select the date first", getActivity());
                } else {
                    showCustomTimePicker(true);
                }
                break;
            case R.id.tv_event_end_time:
                if (tv_event_creation_date.getText().equals("")) {
                    AndroidUtils.showAlert("Please select the date first", getActivity());
                } else if (tv_event_start_time.getText().equals("")) {
                    AndroidUtils.showAlert("Please Select the Start Time", getActivity());
                } else {
                    showCustomTimePicker(false);
                }
                break;
            case R.id.add_notification:
                is_notify_clicked = false;
                if (!is_notify_clicked) {
                    NotificationPopup();
                }
                break;
//            case R.id.btn_cancel_timesheet:
//                AndroidUtils.showToast("Event is not Created", getContext());
//                loadClearedLists();
//                meetings.loadView();
            case R.id.btn_create_event:
                createEvent();
                break;
        }//End of Switch Case...
    }

    private void createEvent() {
        boolean notify_has_error = false;
        boolean isTimeError = false;
        Pattern alphabeticPattern = Pattern.compile(".*[a-zA-Z].*");
        Pattern numericPattern = Pattern.compile(".*[0-9].*");

        if (!selectedValues.isEmpty()) {
            if (!is_timenotify) {
                notify_has_error = true;
            }

            // Check each entry in selectedValues for both alphabetic and numeric characters
            for (String value : selectedValues) {
                Matcher hasAlphabetic = alphabeticPattern.matcher(value);
                Matcher hasNumeric = numericPattern.matcher(value);

                // If an entry does not have both alphabetic and numeric characters, set the error flag
                if (!(hasAlphabetic.find() && hasNumeric.find())) {
                    notify_has_error = true;
                    break;  // Exit the loop as soon as an invalid entry is found
                }
            }
        } else {
            notify_has_error = false;  // No entries, so no error
        }

        isTimeError = tv_event_start_time.getText().toString().isEmpty() && (!isAllDay);


        Log.d("selected_values_size", "" + selectedValues.size());
        if (!matter_legal.equals("reminders")) {
            StringBuilder msg = new StringBuilder("Please check the");

            // 1. No project selected at all
            if (matter_legal.isEmpty()) {
                AndroidUtils.showAlert("Please check the Project", getActivity());
                return;
            }

            if (matter_legal.equals("general") || matter_legal.equals("legal")) {

                // 2. No matters found (tv_no_matter visible)
                if (tv_no_matter.getVisibility() == VISIBLE) {
                    Constants.create_matter = true;
                    Constants.MATTER_TYPE = matter_legal.equals("general") ? "General" : "Legal";
                    AndroidUtils.showReDirectionPopup(getActivity(), new Matter(),
                            "Please create a matter to create an event. Click here to create matter.");
                    return;
                }

                // 3. Matter name not selected
                if (tv_sp_matter_name.getText().toString().isEmpty()) {
                    AndroidUtils.showAlert("Please check the Matter Name", getActivity());
                    return;
                }

                // 4. Task, date, time, timezone, notification checks
                if (tv_sp_task_name.getText().toString().trim().isEmpty()
                        || tv_event_creation_date.getText().toString().trim().isEmpty()
                        || isTimeError
                        || tv_sp_time_zone.getText().toString().trim().isEmpty()
                        || notify_has_error) {

                    if (tv_sp_task_name.getText().toString().trim().isEmpty()) {
                        msg.append(" Task");
                    }
                    if (tv_event_creation_date.getText().toString().trim().isEmpty()) {
                        msg.append(msg.toString().equals("Please check the") ? " Date" : ", Date");
                    }
                    if (isTimeError) {
                        msg.append(msg.toString().equals("Please check the") ? " Time" : ", Time");
                    }
                    if (tv_sp_time_zone.getText().toString().trim().isEmpty()) {
                        msg.append(msg.toString().equals("Please check the") ? " Timezone" : ", Timezone");
                    }
                    if (notify_has_error) {
                        msg.append(msg.toString().equals("Please check the") ? " Notification" : ", Notification");
                    }
                    AndroidUtils.showAlert(msg.toString(), getActivity());
                } else {
                    EventCreation(); // ← legal/general happy path
                }

            } else {
                // overhead / others
                if (tv_sp_task_name.getText().toString().trim().isEmpty()
                        || tv_event_creation_date.getText().toString().trim().isEmpty()
                        || isTimeError
                        || tv_sp_time_zone.getText().toString().trim().isEmpty()
                        || notify_has_error) {

                    if (tv_sp_task_name.getText().toString().trim().isEmpty()) {
                        msg.append(" Task");
                    }
                    if (tv_event_creation_date.getText().toString().trim().isEmpty()) {
                        msg.append(msg.toString().equals("Please check the") ? " Date" : ", Date");
                    }
                    if (isTimeError) {
                        msg.append(msg.toString().equals("Please check the") ? " Time" : ", Time");
                    }
                    if (tv_sp_time_zone.getText().toString().trim().isEmpty()) {
                        msg.append(msg.toString().equals("Please check the") ? " Timezone" : ", Timezone");
                    }
                    if (notify_has_error) {
                        msg.append(msg.toString().equals("Please check the") ? " Notification" : ", Notification");
                    }
                    AndroidUtils.showAlert(msg.toString(), getActivity());
                } else {
                    EventCreation();
                }
            }

        } else {
            //Date,time,timezone,message
            if (Objects.requireNonNull(tv_message.getText()).toString().trim().isEmpty() || Objects.requireNonNull(tv_event_creation_date.getText()).toString().trim().isEmpty() || isTimeError || tv_sp_time_zone.getText().toString().trim().isEmpty() || (notify_has_error)) {
                // Check if member name is empty
//                            hasErrors = true;
                //..
                String msg = "Please check the";
                if (Objects.requireNonNull(tv_sp_project.getText()).toString().isEmpty()) {
                    msg = msg + " Project";
                }
                if (Objects.requireNonNull(tv_message.getText()).toString().isEmpty()) {
                    if (msg.equals("Please check the")) {
                        msg = msg + " Message";
                    } else {
                        msg = msg + ", Message";
                    }
                }
                if (Objects.requireNonNull(tv_event_creation_date.getText()).toString().isEmpty()) {
//                    AndroidUtils.showAlert(msg + ", Task", getContext());
                    if (msg.equals("Please check the")) {
                        msg = msg + " Date";
                    } else {
                        msg = msg + ",Date";
                    }
                }
                if (isTimeError) {
                    if (msg.equals("Please check the")) {
                        msg = msg + " Time";
                    } else {
                        msg = msg + ", Time";
                    }
//                                    AndroidUtils.showAlert("Start Time is required", getContext());
                }
                if (Objects.requireNonNull(tv_sp_time_zone.getText()).toString().isEmpty()) {
                    if (msg.equals("Please check the")) {
                        msg = msg + " Time Zone";
                    } else {
                        msg = msg + ", Time Zone";
                    }
                }
//                        if (Objects.equals(Constants.is_meeting, "Create")) {
                if (notify_has_error) {
                    if (msg.equals("Please check the")) {
                        msg = msg + " Notification";
                    } else {
                        msg = msg + ", Notification";
                    }
                }
//                        }
                AndroidUtils.showAlert(msg, getActivity());
                // Validate email format
                //..
            } else {
                EventCreation();
            }
        }
    }

    private void EventCreation() {
        if (Objects.equals(Constants.is_meeting, "Create"))
            callCreateEventWebservice();
        else {
            event_update_scope = "UPDATE_EVENT_ONLY";
            if ((existing_repetetion.equals("None")) || (tv_sp_repetetion.getText().toString().equals("None")) || (!isRecurring)) {
                CheckUpdateScope("all");
            } else {
                update_event();
            }
        }
    }

    private @NonNull DatePickerDialog getDatePickerDialog() {
        final Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

            private void updateLabel() {
                String myFormat = "dd-MM-yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                tv_event_creation_date.setText(sdf.format(myCalendar.getTime()));
            }
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
        return datePickerDialog;
    }

    private void display_duration(String start_time, String end_time) {
        try {
            // Parse start time in HH:mm (24-hour) format
            String[] startParts = start_time.split(":");
            if (startParts.length != 2) {
                Time_duration.setText("Invalid start time format");
                return;
            }

            int startHour = Integer.parseInt(startParts[0]);
            int startMinute = Integer.parseInt(startParts[1]);

            // Parse end time in HH:mm (24-hour) format
            String[] endParts = end_time.split(":");
            if (endParts.length != 2) {
                Time_duration.setText("Invalid end time format");
                return;
            }

            int endHour = Integer.parseInt(endParts[0]);
            int endMinute = Integer.parseInt(endParts[1]);

            // Calculate total minutes from midnight for both start and end time
            int startTotalMinutes = startHour * 60 + startMinute;
            int endTotalMinutes = endHour * 60 + endMinute;

            // Calculate the duration in minutes
            int durationMinutes;
            if (endTotalMinutes < startTotalMinutes) {
                // Handle the case where end time is the next day
                durationMinutes = (24 * 60 - startTotalMinutes) + endTotalMinutes;
            } else {
                durationMinutes = endTotalMinutes - startTotalMinutes;
            }

            // Convert duration into hours and minutes
            int hours = durationMinutes / 60;
            int minutes = durationMinutes % 60;

            // Display the result
            String durationText = "";
            if (hours == 0) {
                durationText = String.format("Duration: %d min", minutes);
            } else {
                durationText = String.format("Duration: %d hr, %d min", hours, minutes);
            }
            Time_duration.setText(durationText);
        } catch (NumberFormatException e) {
            Time_duration.setText("Invalid time format");
            Log.d("T_Duration", "Invalid time format: " + e.getMessage());
        }
    }

//    private void display_duration(String start_time, String end_time) {
//        try {
//            // Remove AM/PM and parse start time
//            boolean isStartAM = start_time.contains("AM");
//            start_time = start_time.replace(" AM", "").replace(" PM", "");
//            String[] startParts = start_time.split(":");
//
//            if (startParts.length != 2) {
//                Time_duration.setText("Invalid start time format");
//                return;
//            }
//
//            int startHour = Integer.parseInt(startParts[0]);
//            int startMinute = Integer.parseInt(startParts[1]);
//
//            if (startHour < 12) {
//                startHour += 12;
//            }
//            if (isStartAM && startHour == 12) {
//                startHour = 0;
//            }
//
//            // Remove AM/PM and parse end time
//            boolean isEndAM = end_time.contains("AM");
//            end_time = end_time.replace(" AM", "").replace(" PM", "");
//            String[] endParts = end_time.split(":");
//
//            if (endParts.length != 2) {
//                Time_duration.setText("Invalid end time format");
//                return;
//            }
//
//            int endHour = Integer.parseInt(endParts[0]);
//            int endMinute = Integer.parseInt(endParts[1]);
//
//            if (!isEndAM && endHour < 12) {
//                endHour += 12;
//            }
//            if (isEndAM && endHour == 12) {
//                endHour = 0;
//            }
//
//            // Calculate duration
//            int startTotalMinutes = startHour * 60 + startMinute;
//            int endTotalMinutes = endHour * 60 + endMinute;
//
//            int durationMinutes;
//            if (endTotalMinutes < startTotalMinutes) {
//                // Handle case where end time is on the next day
//                durationMinutes = (24 * 60 - startTotalMinutes) + endTotalMinutes;
//            } else {
//                durationMinutes = endTotalMinutes - startTotalMinutes;
//            }
//
//            int hours = durationMinutes / 60;
//            int minutes = durationMinutes % 60;
//
//            String durationText = String.format("Duration: %d Hours, %d Minutes", hours, minutes);
//            Time_duration.setText(durationText);
//        } catch (NumberFormatException e) {
//            Time_duration.setText(R.string.duration);
//            Log.d("T_Duration", "Invalid time format");
//        }
//    }

    public void selected_documents(ArrayList<DocumentsDo> list_item) {
//        selected_documents_list.clear();
//        for (int i = 0; i < list_item.size(); i++) {
//            DocumentsDo documentsDo = list_item.get(i);
//            if (documentsDo.isChecked()) {
//                if (!selected_documents_list.contains(documentsDo)) {
//                    selected_documents_list.add(documentsDo);
//                }
//            }
//        }
        if (selected_documents_list.isEmpty()) {
            ll_selected_documents.setVisibility(GONE);
            at_attach_document.setText("");
        } else {
            loadSelectedDocuments();
        }
//    is_clicked_documents = true;
//    rv_documents_view.setVisibility(View.GONE);
    }

    private void Documents_Popup() {
        try {
            //            documents_list.clear();
            if (documents_list.isEmpty()) {
                for (int i = 0; i < matterList.size(); i++) {
                    if (matter_id.equals(matterList.get(i).getId())) {
                        JSONArray documents = matterList.get(i).getDocuments();
                        for (int j = 0; j < documents.length(); j++) {
                            DocumentsDo documentsDo = new DocumentsDo();
                            JSONObject jsonObject = documents.optJSONObject(j);
                            documentsDo.setDocid(jsonObject.optString("docid"));
                            documentsDo.setDoctype(jsonObject.optString("doctype"));
                            documentsDo.setName(jsonObject.optString("name"));
//                            documentsDo.setUser_id(jsonObject.getString("user_id"));
                            documents_list.add(documentsDo);
                        }
                    }
                }
            }
            if (documents_list.isEmpty()) {
                ll_documents_view.setVisibility(GONE);
            } else {
                ll_documents_view.setVisibility(VISIBLE);
            }
            load_documents_Popup();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void load_documents_Popup() {
        try {
            if (!documents_list.isEmpty()) {
//                rv_documents_view.setVisibility(View.GONE);
//                is_clicked_documents = true;
//                AndroidUtils.showToast("No document to show", getContext());
//            } else {
//                rv_documents_view.setVisibility(View.VISIBLE);
                for (int i = 0; i < documents_list.size(); i++) {
                    DocumentsDo documentsDo = documents_list.get(i);
                    documentsDo.setChecked(false);
                    for (int j = 0; j < selected_documents_list.size(); j++) {
                        if (documents_list.get(i).getDocid().matches(selected_documents_list.get(j).getDocid())) {
                            documentsDo.setChecked(true);
//                        selected_groups_list.set(j,documentsModel);
                        }
                    }
                }
//            selected_tm_list.clear();
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                rv_documents_view.setLayoutManager(layoutManager);
                // rv_documents_view.setHasFixedSize(true);
                ADAPTER_TAG = "Documents";
                CommonRelationshipsAdapter documentsAdapter = new CommonRelationshipsAdapter(teamList, ADAPTER_TAG, individual_list, entity_client_list, entity_corp_client_list, documents_list, this);
                rv_documents_view.setAdapter(documentsAdapter);
                //..
                AndroidUtils.LoadList(rv_documents_view, getContext(), documents_list.size(), true);
                //..
//                btn_attach_document.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        //Clearing the existing chosen documents and re added the documents.
//                        selected_documents_list.clear();
//                        for (int i = 0; i < documentsAdapter.getDocuments_list().size(); i++) {
//                            DocumentsDo documentsDo = documentsAdapter.getDocuments_list().get(i);
//                            if (documentsDo.isChecked()) {
//                                if (!selected_documents_list.contains(documentsDo)) {
//                                    selected_documents_list.add(documentsDo);
//                                }
////                        AndroidUtils.showAlert(selected_individual_list.toString(),getContext());
////                        new_selected_individual_list.add(teamModel);
//                                //                           jsonArray.put(selected_documents_list.get(i).getGroup_name());
//                            }
//                        }
//
//                        loadSelectedDocuments();
//                        is_clicked_documents = true;
//                        rv_documents_view.setVisibility(View.GONE);
////                    loadSelectedIndividual();
//                    }
//                });
            }
        } catch (Exception e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void loadSelectedDocuments() {
        String[] value = new String[selected_documents_list.size()];
        for (int i = 0; i < selected_documents_list.size(); i++) {
//                                value += "," + family_members.get(i);
//                               value.add(family_members.get(i));
            value[i] = selected_documents_list.get(i).getName();
        }
        String str = String.join(",", value);
        at_attach_document.setText(str);
        if (selected_documents_list.isEmpty()) {
            ll_selected_documents.setVisibility(GONE);
        } else {
//            ll_selected_documents.setVisibility(View.VISIBLE);
        }

        ll_documents_list.removeAllViews();
        for (int i = 0; i < selected_documents_list.size(); i++) {
            View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.edit_opponent_advocate, null);
            TextView tv_opponent_name = view_opponents.findViewById(R.id.tv_opponent_name);
            tv_opponent_name.setText(selected_documents_list.get(i).getName());
            ImageView iv_edit_opponent = view_opponents.findViewById(R.id.iv_edit_opponent);
            ImageView iv_remove_opponent = view_opponents.findViewById(R.id.iv_remove_opponent);
            iv_remove_opponent.setTag(i);
            iv_remove_opponent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int position = (int) v.getTag();

                        // Remove the view at the specified position
                        ll_documents_list.removeViewAt(position);

                        // Remove the corresponding item from the list
                        DocumentsDo teamModel = selected_documents_list.remove(position);
                        teamModel.setChecked(false);

                        // Update the tags of the remaining views
                        for (int j = 0; j < ll_documents_list.getChildCount(); j++) {
                            ImageView iv_remove = ll_documents_list.getChildAt(j).findViewById(R.id.iv_remove_opponent);
                            if (iv_remove != null) {
                                iv_remove.setTag(j);
                            }
                        }

                        // Update the attached document text
                        StringBuilder stringBuilder = new StringBuilder();
                        for (DocumentsDo model : selected_documents_list) {
                            stringBuilder.append(model.getName()).append(",");
                        }

                        if (stringBuilder.length() > 0) {
                            stringBuilder.deleteCharAt(stringBuilder.length() - 1); // Remove the last comma
                        }

                        at_attach_document.setText(stringBuilder.toString());

                        // Show or hide the selected documents layout
                        if (selected_documents_list.isEmpty()) {
                            ll_selected_documents.setVisibility(GONE);
                            at_attach_document.setText("");
                        } else {
//                            ll_selected_documents.setVisibility(View.VISIBLE);
                        }
                        rv_documents_view.getAdapter().notifyDataSetChanged();

                    } catch (Exception e) {
                        e.printStackTrace(); // Log the error
                        AndroidUtils.showAlert(e.getMessage(), getActivity());
                    }
                }
            });
            iv_edit_opponent.setVisibility(GONE);
            ll_documents_list.addView(view_opponents);
        }
    }

    private void callCreateEventWebservice() {
        try {
            String doctype = "doctype";
            String docid = "docid";
            JSONObject postData = new JSONObject();
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONArray selected_team_member = new JSONArray();
            JSONArray selected_clients_list = new JSONArray();
            JSONArray selected_corp_clients_list = new JSONArray();
            JSONArray individual_array = new JSONArray();
            JSONArray added_notification_list = new JSONArray();
            JSONArray time_sheets = new JSONArray();
            JSONArray existing_attachments = new JSONArray();
            for (int i = 0; i < selectedValues.size(); i++) {
//            NotifyMeDo notifyMeDo = notifyme_list.get(i);
                Log.d("Notify_array1", selectedValues.get(i));
                added_notification_list.put(selectedValues.get(i));
            }
            for (int i = 0; i < added_notification_list.length(); i++) {
                Log.d("Notify_array2", "" + added_notification_list.get(i));
            }
            JSONObject selected_docs;
            for (int j = 0; j < selected_documents_list.size(); j++) {
                selected_docs = new JSONObject();
                DocumentsDo attachDocumentsDO = selected_documents_list.get(j);
                selected_docs.put(doctype, attachDocumentsDO.getDoctype());
                selected_docs.put(docid, attachDocumentsDO.getDocid());
                existing_attachments.put(selected_docs);
            }

            for (int i = 0; i < selected_tm_list.size(); i++) {
                TeamDo addTeamMembersDo = selected_tm_list.get(i);
                selected_team_member.put(addTeamMembersDo.getId());
            }
            for (int i = 0; i < selected_entity_client_list.size(); i++) {
                RelationshipsDO addClientsDo = selected_entity_client_list.get(i);
                selected_clients_list.put(addClientsDo.getId());
            }
            for (int i = 0; i < selected_entity_corp_client_list.size(); i++) {
                RelationshipsDO addClientsDo = selected_entity_corp_client_list.get(i);
                selected_corp_clients_list.put(addClientsDo.getId());
            }
            for (int i = 0; i < selected_individual_list.size(); i++) {
                RelationshipsDO relationshipsDO = selected_individual_list.get(i);
                individual_array.put(relationshipsDO.getId());
            }

            Date event_date = AndroidUtils.stringToDateTimeDefault(tv_event_creation_date.getText().toString(), "MMM dd, yyyy");
            event_creation_date = AndroidUtils.getDateToString(event_date, "yyyy-MM-dd");
            Date event_start_date = null;
            Date event_date2 = null;
            String start_time = tv_event_start_time.getText().toString();
            if (start_time.isEmpty() || start_time == null) {
                start_time = "00:00";
                event_start_date = AndroidUtils.stringToDateTimeDefault(start_time, "HH:mm");

            } else {
                event_start_date = AndroidUtils.stringToDateTimeDefault(start_time, "HH:mm");
            }
            event_starting_date = AndroidUtils.getDateToString(event_start_date, event_creation_date + "'T'HH:mm:ss");
            String end_time = tv_event_end_time.getText().toString();
            if (end_time.isEmpty() || end_time == null) {
                end_time = "00:00";
                event_date2 = AndroidUtils.stringToDateTimeDefault(end_time, "HH:mm");
            } else {
                event_date2 = AndroidUtils.stringToDateTimeDefault(end_time, "HH:mm");
            }
            String duration_timesheet = "";
            if (event_start_date != null && event_date2 != null) {

                long differenceInMilliSeconds = Math.abs(event_start_date.getTime() - event_date2.getTime());
                long differenceInHours = (differenceInMilliSeconds / (60 * 60 * 1000)) % 24;
                long differenceInMinutes
                        = (differenceInMilliSeconds / (60 * 1000)) % 60;
                long differenceInSeconds
                        = (differenceInMilliSeconds / 1000) % 60;
                String duration = differenceInHours + ":" + differenceInMinutes;
                Date hours = AndroidUtils.stringToDateTimeDefault(duration, "HH:mm");
                duration_timesheet = AndroidUtils.getDateToString(hours, "HH:mm");
            }
            event_end_time = AndroidUtils.getDateToString(event_date2, event_creation_date + "'T'HH:mm:ss");

            if (isAddTimesheet) {
                JSONObject time_sheet_obj;
//                        for (int i = 0; i < time_sheets.length(); i++) {
                time_sheet_obj = new JSONObject();
                time_sheet_obj.put("date", event_creation_date);
                if (isAllDay) {
                    time_sheet_obj.put("duration", "23:59");
                    event_starting_date = event_creation_date + "T00:00:00";
                    event_end_time = event_creation_date + "T23:59:59";
                } else {
                    if (duration_timesheet.isEmpty()) {
                        time_sheet_obj.put("duration", "30:00");
                    } else {
                        time_sheet_obj.put("duration", duration_timesheet);
                    }
                }
                time_sheet_obj.put("eventtitle", selected_task);
                time_sheet_obj.put("addedby", Constants.NAME);
                time_sheet_obj.put("user_id", Constants.USER_ID);
                if ((!matter_legal.equals("legal") && (!matter_legal.equals("general")))) {
                    time_sheet_obj.put("matter_id", tv_sp_project.getText().toString());
                    time_sheet_obj.put("matter_type", matter_legal);
                }
                time_sheets.put(time_sheet_obj);
            }
            int multiplied_offset = (-1) * (offset);
            String dateInput = event_creation_date; // "Jan 31, 2026"

// Parse WITHOUT time manipulation
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = inputFormat.parse(dateInput);

// Now build exact UTC output WITHOUT converting timezone
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTime(date);

// Force UTC midnight (don’t let Java auto-convert)
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String finalDate = utcFormat.format(cal.getTime());

            postData.put("date", finalDate);

            postData.put("attachments", existing_attachments);
//            } catch (ParseException e) {
//                e.fillInStackTrace();
//            } catch (JSONException e) {
//                e.fillInStackTrace();
//            }
            postData.put("invitees_corporate", selected_corp_clients_list);
            postData.put("invitees_internal", selected_team_member);
            postData.put("invitees_external", selected_clients_list);
            postData.put("invitees_consumer_external", individual_array);
            if (matter_legal.equals("legal") || matter_legal.equals("general")) {
                postData.put("title", matter_name + " - " + tv_sp_task_name.getText().toString());
            } else if (matter_legal.equals("overhead") || (matter_legal.equals("others"))) {
                postData.put("title", tv_sp_task_name.getText().toString());
            } else {
                postData.put("title", matter_legal);
            }

            postData.put("notifications", added_notification_list);
//            postData.put("description", tv_description.getText().toString());
            postData.put("timezone_location", timezone_location);
            postData.put("timezone_offset", multiplied_offset);
            String repetetion = "";
            if (tv_sp_repetetion.getText().toString().toLowerCase(Locale.ROOT).equals("bi-weekly")) {
                repetetion = "biweekly";
            } else if (tv_sp_repetetion.getText().toString().equals("None")) {
                repetetion = "";
            } else {
                repetetion = tv_sp_repetetion.getText().toString().toLowerCase(Locale.ROOT);
            }
            Log.d("Repetation", repetetion);
            postData.put("repeat_interval", repetetion);
            postData.put("meeting_link", Objects.requireNonNull(tv_meeting_link.getText()).toString());
            postData.put("allday", isAllDay);
            postData.put("from_ts", event_starting_date);
            postData.put("to_ts", event_end_time);
            if (!matter_legal.equals("reminders")) {
                postData.put("dialin", Objects.requireNonNull(tv_dialing_number.getText()).toString());
                postData.put("location", Objects.requireNonNull(tv_location.getText()).toString());
                postData.put("addtimesheet", isAddTimesheet);
                postData.put("timesheets", time_sheets);
                postData.put("description", Objects.requireNonNull(tv_description.getText()).toString());
            } else {
                postData.put("description", tv_message.getText().toString());
            }
            if (matter_legal.equals("legal") || matter_legal.equals("general")) {
                postData.put("matter_type", matter_legal);
                postData.put("matter_id", matter_id);
            }
            postData.put("event_type", matter_legal);

            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v3/events", "CREATE_EVENT", postData.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    //    private void NotificationPopup() {
//        View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.add_calendar_notification, null);
//        LinearLayout tv_notify_layout = view_opponents.findViewById(R.id.tv_notify_layout);
//        TextView tv_sp_minutes = tv_notify_layout.findViewById(R.id.tv_spinner_view);
////        ImageView sp_notify_dropdown_icon = tv_notify_layout.findViewById(R.id.img_dropdown_icon);
////        ImageView sp_notify_clear_icon = tv_notify_layout.findViewById(R.id.img_clear_icon);
//
//        ListView sp_minutes = view_opponents.findViewById(R.id.sp_minutes);
//        TextInputEditText tv_numbers = view_opponents.findViewById(R.id.tv_numbers);
//        tv_numbers.setInputType(InputType.TYPE_CLASS_NUMBER);
//        ArrayList<MinutesDO> minutes_list = new ArrayList<>();
//        TextView error_msg = view_opponents.findViewById(R.id.error_msg);
//        TextView error_msg1 = view_opponents.findViewById(R.id.error_msg1);
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
//        ImageView iv_delete_notification = view_opponents.findViewById(R.id.iv_delete_notification);
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
//                    selectedValues.set(viewPosition, time_value + "-" + selected_hour_type.toLowerCase(Locale.ROOT));
//                } else {
//                    selectedValues.set(viewPosition, tv_numbers.getText().toString() + "-" + selected_hour_type.toLowerCase(Locale.ROOT));
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
//                String selectedMinutes = tv_sp_minutes.getText().toString().toLowerCase(Locale.ROOT);
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
//

    /// /            String newValue = Objects.requireNonNull(tv_numbers.getText().toString()) + "-" + tv_sp_minutes.getText().toString().toLowerCase(Locale.ROOT);
    /// /            selectedValues.add(newValue);
//
//            if (tv_numbers.getText().toString().isEmpty()) {
//                selectedValues.add(time_value + "-" + tv_sp_minutes.getText().toString().toLowerCase(Locale.ROOT));
//            } else {
//                selectedValues.add(tv_numbers.getText().toString() + "-" + tv_sp_minutes.getText().toString().toLowerCase(Locale.ROOT));
//            }
//            // Set the exact value as a tag to help with direct matching
//            view_opponents.setTag(selectedValues.size() - 1);
//        }
//    }
    private void NotificationPopup() {
        View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.add_calendar_notification, null);
        LinearLayout tv_notify_layout = view_opponents.findViewById(R.id.tv_notify_layout);
        TextView tv_sp_minutes = tv_notify_layout.findViewById(R.id.tv_spinner_view);
        TextView error_msg = view_opponents.findViewById(R.id.error_msg);
        TextView error_msg1 = view_opponents.findViewById(R.id.error_msg1);
        TextInputEditText tv_numbers = view_opponents.findViewById(R.id.tv_numbers);
        ListView sp_minutes = view_opponents.findViewById(R.id.sp_minutes);
        ImageView iv_delete_notification = view_opponents.findViewById(R.id.iv_delete_notification);

        tv_numbers.setInputType(InputType.TYPE_CLASS_NUMBER);
        error_msg.setTextColor(requireContext().getResources().getColor(R.color.Red));
        error_msg.setTextSize(12);
        error_msg1.setTextColor(requireContext().getResources().getColor(R.color.Red));
        error_msg1.setTextSize(12);
        error_msg1.setVisibility(GONE);
        sp_minutes.setVisibility(GONE);

        if (isAllDay) {
            minutes_list.clear();
            minutes_list.add(new MinutesDO("Days"));
            minutes_list.add(new MinutesDO("Weeks"));
            AndroidUtils.LoadList(sp_minutes, getContext(), minutes_list.size(), true);
        } else {
            minutes_list.clear();
            minutes_list.add(new MinutesDO("Minutes"));
            minutes_list.add(new MinutesDO("Hours"));
            minutes_list.add(new MinutesDO("Days"));
            minutes_list.add(new MinutesDO("Weeks"));
            AndroidUtils.LoadList(sp_minutes, getContext(), minutes_list.size(), true);
        }

        if (!selectedValues.isEmpty() && Constants.is_meeting.equals("Edit")) {
            tv_sp_minutes.setText(AndroidUtils.CapitalizeFirstLetter(time_value));
            tv_numbers.setText(time_format);
        }

        if (!is_notify_clicked) {
            tv_sp_minutes.setText(minutes_list.get(0).getName());
            tv_numbers.setText(R.string._10);
        }

        int position = ll_add_notification.getChildCount();
        view_opponents.setTag(position);

        CommonSpinnerAdapter spinner_adapter = new CommonSpinnerAdapter((Activity) getContext(), minutes_list);
        sp_minutes.setAdapter(spinner_adapter);
        AndroidUtils.LoadList(sp_minutes, getContext(), minutes_list.size(), true);

        tv_notify_layout.setOnClickListener(v -> {
            AndroidUtils.display_listview(is_notify, sp_minutes);
            is_notify = !is_notify;
        });

        sp_minutes.setOnItemClickListener((parent, view, i, id) -> {
            selected_hour_type = minutes_list.get(i).getName();
            int viewPosition = (int) view_opponents.getTag();

            String time_value = "";
            tv_sp_minutes.setText(selected_hour_type);
            if (!Objects.requireNonNull(tv_numbers.getText()).toString().isEmpty()) {
                int notify_number = getSafeInt(Objects.requireNonNull(tv_numbers.getText()).toString());
                notify_number = enforceMaxLimit(tv_sp_minutes.getText().toString(), notify_number);
                tv_numbers.setText(String.valueOf(notify_number));
            } else {
                tv_numbers.setText("");
            }

            if (viewPosition >= 0 && viewPosition < selectedValues.size()) {
                time_value = tv_numbers.getText().toString().isEmpty() ? getDefaultTimeValue(tv_sp_minutes.getText().toString()) : tv_numbers.getText().toString();
                selectedValues.set(viewPosition, time_value + "-" + selected_hour_type.toLowerCase(Locale.ROOT));
            }

            sp_minutes.setVisibility(GONE);
            is_notify = true;
            Log.d("Updated selectedValues after sp_minutes selection", selectedValues.toString());
        });

//        tv_sp_minutes.addTextChangedListener(new TextWatcher() {
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (count > 0) {
//                    error_msg1.setVisibility(View.GONE);
//                    is_timenotify = false;
//                }
//            }
//
//            public void afterTextChanged(Editable s) {
//                is_timenotify = true;
//                if (!Objects.requireNonNull(tv_numbers.getText()).toString().isEmpty()) {
//                    int notify_number = getSafeInt(Objects.requireNonNull(tv_numbers.getText()).toString());
//                    notify_number = enforceMaxLimit(tv_sp_minutes.getText().toString(), notify_number);
//                    tv_numbers.setText(String.valueOf(notify_number));
//                } else {
//                    tv_numbers.setText("");
//                }
//                error_msg.setVisibility(View.GONE);
//            }
//        });


        tv_numbers.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void afterTextChanged(Editable editable) {
                String currentInput = editable.toString().trim();
                int notify_number = getSafeInt(currentInput);
                int enforcedValue = enforceMaxLimit(tv_sp_minutes.getText().toString(), notify_number);
                String enforcedStr = String.valueOf(enforcedValue);

                // Always show "0" if input is invalid or empty
                if (!currentInput.equals(enforcedStr)) {
                    tv_numbers.removeTextChangedListener(this); // avoid infinite loop
                    if (enforcedStr.equals("0")) {
                        tv_numbers.setText("");
                    } else {
                        tv_numbers.setText(enforcedStr);
                    }
//                    tv_numbers.setSelection(enforcedStr.length());
                    tv_numbers.addTextChangedListener(this);
//                return;
                }

                int viewPosition = (int) view_opponents.getTag();
                String selectedMinutes = tv_sp_minutes.getText().toString().toLowerCase(Locale.ROOT);
                String value = currentInput.isEmpty() ?
                        getDefaultTimeValue(tv_sp_minutes.getText().toString()) :
                        enforcedStr;

                if (viewPosition >= 0 && viewPosition < selectedValues.size()) {
                    selectedValues.set(viewPosition, value + "-" + selectedMinutes);
                }

                Log.d("Updated selectedValues after tv_numbers input", selectedValues.toString());
            }
        });


        iv_delete_notification.setOnClickListener(v -> {
            int pos = (int) view_opponents.getTag();
            ll_add_notification.removeView(view_opponents);

            if (pos >= 0 && pos < selectedValues.size()) {
                String removedValue = selectedValues.get(pos);
                selectedValues.remove(removedValue);
            }

            for (int i = 0; i < ll_add_notification.getChildCount(); i++) {
                ll_add_notification.getChildAt(i).setTag(i);
            }

            Log.d("Updated selectedValues", selectedValues.toString());
        });

        ll_add_notification.addView(view_opponents);

        if (!is_notify_clicked) {
            String time_value = "";
            int notify_number = getSafeInt(Objects.requireNonNull(tv_numbers.getText()).toString());
            notify_number = enforceMaxLimit(tv_sp_minutes.getText().toString(), notify_number);
            tv_numbers.setText(String.valueOf(notify_number));
            time_value = tv_numbers.getText().toString().isEmpty() ?
                    getDefaultTimeValue(tv_sp_minutes.getText().toString()) :
                    tv_numbers.getText().toString();
            selectedValues.add(time_value + "-" + tv_sp_minutes.getText().toString().toLowerCase(Locale.ROOT));
        }
    }

    // Helper: Parse integer safely
    private int getSafeInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Helper: Return max limit based on type
    private int enforceMaxLimit(String type, int value) {
        switch (type) {
            case "Minutes":
                return Math.min(value, 60);
            case "Hours":
                return Math.min(value, 24);
            case "Days":
                return Math.min(value, 31);
            case "Weeks":
                return Math.min(value, 4);
            default:
                return value;
        }
    }

    // Helper: Get default value
    private String getDefaultTimeValue(String type) {
        switch (type) {
            case "Minutes":
                return "10";
            case "Hours":
            case "Days":
            case "Weeks":
                return "1";
            default:
                return "0";
        }
    }


    private void loadnewInput(TextInputEditText tv_numbers, View view, TextView tv_sp_minutes) {
        View parentView = (View) tv_numbers.getParent();
        TextView inputFieldValueTextView = parentView.findViewById(R.id.tv_numbers);
        inputFieldValueTextView.setText(Objects.requireNonNull(tv_numbers.getText()).toString());
        int position = findPositionInParent(view, ll_add_notification);
        if (position >= 0 && position < selectedValues.size()) {
            // Update the input field value in the ArrayList
            selectedValues.set(position, tv_numbers.getText().toString() + "-" + tv_sp_minutes.getText().toString().toLowerCase(Locale.ROOT));
        }
    }

    private int findPositionInParent(View view, ViewGroup parent) {
        int position = parent.indexOfChild(view);
        if (position >= 0) {
            return position;
        } else {
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                if (child instanceof ViewGroup) {
                    int childPosition = findPositionInParent(view, (ViewGroup) child);
                    if (childPosition >= 0) {
                        return childPosition; // Return the childPosition if it's >= 0
                    }
                }
            }
        }
        return -1; // Return -1 if the view is not found in the parent or its children
    }

    public void selected_corporate(ArrayList<RelationshipsDO> list_item) {
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
            ll_selected_corp_clients_view.setVisibility(GONE);
            at_assigned_corp_client.setText("");
        } else {
            loadselectedCorpClients();
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

    private void loadselectedCorpClients() {
        String[] value = new String[selected_entity_corp_client_list.size()];
        for (int i = 0; i < selected_entity_corp_client_list.size(); i++) {
//                                value += "," + family_members.get(i);
//                               value.add(family_members.get(i));
            value[i] = selected_entity_corp_client_list.get(i).getName();
        }
        String str = String.join(",", value);
        at_assigned_corp_client.setText(str);
        loadSelectedCorpList();
        ll_corp_client_team_members.removeAllViews();
        for (int i = 0; i < selected_entity_corp_client_list.size(); i++) {
            View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.edit_opponent_advocate, null);
            TextView tv_opponent_name = view_opponents.findViewById(R.id.tv_opponent_name);
            tv_opponent_name.setText(selected_entity_corp_client_list.get(i).getName());
            ImageView iv_edit_opponent = view_opponents.findViewById(R.id.iv_edit_opponent);
            ImageView iv_remove_opponent = view_opponents.findViewById(R.id.iv_remove_opponent);
            iv_remove_opponent.setTag(i);
            iv_remove_opponent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int position = (Integer) v.getTag();
                        ll_corp_client_team_members.removeViewAt(position);
//                            ll_selected_groups.addView(view_opponents,position);
                        RelationshipsDO teamModel = selected_entity_corp_client_list.remove(position);
                        teamModel.setChecked(false);
//                            selected_entity_client_list.remove(position);
                        //..
                        for (int j = 0; j < ll_corp_client_team_members.getChildCount(); j++) {
                            ImageView iv_remove = ll_corp_client_team_members.getChildAt(j).findViewById(R.id.iv_remove_opponent);
                            if (iv_remove != null) {
                                iv_remove.setTag(j);
                            }
                        }

                        String str = String.join(",", value);
                        at_assigned_corp_client.setText(str);
                        StringBuilder stringBuilder = new StringBuilder();
                        for (RelationshipsDO model : selected_entity_corp_client_list) {
                            stringBuilder.append(model.getName()).append(",");
                        }

                        if (stringBuilder.length() > 0) {
                            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                        }
                        String strn = stringBuilder.toString();
                        at_assigned_corp_client.setText(strn);
                        loadSelectedCorpList();
                        //..
//                            selected_groups_list.set(position, groupsModel);
                    } catch (Exception e) {
                        e.fillInStackTrace();
                        AndroidUtils.showAlert(e.getMessage(), getActivity());
                    }
                }
            });
            iv_edit_opponent.setVisibility(GONE);
            ll_corp_client_team_members.addView(view_opponents);
        }
    }

    private void loadSelectedCorpList() {
        if (!selected_entity_corp_client_list.isEmpty()) {
            ll_selected_corp_clients_view.setVisibility(VISIBLE);
        } else {
            at_assigned_corp_client.setText("");
            ll_selected_corp_clients_view.setVisibility(GONE);
        }
    }

    public void selected_individual(ArrayList<RelationshipsDO> list_item) {
//        for (int i = 0; i < list_item.size(); i++) {
//            RelationshipsDO teamModel = list_item.get(i);
//            if (teamModel.isChecked()) {
//                if (!selected_individual_list.contains(teamModel)) {
//                    selected_individual_list.add(teamModel);
//                }
//            }
//        }
        if (selected_individual_list.isEmpty()) {
            ll_selected_individual.setVisibility(GONE);
            at_individual.setText("");
        } else {
            loadSelectedIndividual();
        }
    }

    private void load_individual_Popup() {
        try {
            if (!individual_list.isEmpty()) {
//                is_clicked_individuals = true;
//                rv_individuals_view.setVisibility(View.GONE);
//                AndroidUtils.showToast("No Individuals to show", getContext());
//            } else {
                rv_individuals_view.setVisibility(VISIBLE);
                for (int i = 0; i < individual_list.size(); i++) {
                    RelationshipsDO teamModel = individual_list.get(i);
                    teamModel.setChecked(false);
                    for (int j = 0; j < selected_individual_list.size(); j++) {
                        if (individual_list.get(i).getId().matches(selected_individual_list.get(j).getId())) {
                            teamModel.setChecked(true);
//                        selected_groups_list.set(j,documentsModel);
                        }
                    }
                }
//                selected_individual_list.clear();
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                rv_individuals_view.setLayoutManager(layoutManager);
                //rv_individuals_view.setHasFixedSize(true);
                ADAPTER_TAG = "INDIVIDUAL";
                CommonRelationshipsAdapter documentsAdapter = new CommonRelationshipsAdapter(teamList, ADAPTER_TAG, individual_list, entity_client_list, entity_corp_client_list, documents_list, this);
                rv_individuals_view.setAdapter(documentsAdapter);
                //..
                AndroidUtils.LoadList(rv_individuals_view, getContext(), individual_list.size(), true);
                //..
//                btn_individual.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                    }
//                });
            }
        } catch (Exception e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void loadSelectedIndividual() {
        String[] value = new String[selected_individual_list.size()];
        for (int i = 0; i < selected_individual_list.size(); i++) {
            value[i] = selected_individual_list.get(i).getName();
        }
        String str = String.join(",", value);
        at_individual.setText(str);
//        ll_selected_individual.setVisibility(View.VISIBLE);
        ll_individual_list.removeAllViews();
        for (int i = 0; i < selected_individual_list.size(); i++) {
            View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.edit_opponent_advocate, null);
            TextView tv_opponent_name = view_opponents.findViewById(R.id.tv_opponent_name);
            tv_opponent_name.setText(selected_individual_list.get(i).getName());
            ImageView iv_edit_opponent = view_opponents.findViewById(R.id.iv_edit_opponent);
            ImageView iv_remove_opponent = view_opponents.findViewById(R.id.iv_remove_opponent);
            iv_remove_opponent.setTag(i);
            iv_remove_opponent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        //..
                        int position = (int) v.getTag();
                        // Remove the view at the specified position
                        ll_individual_list.removeViewAt(position);
                        // Remove the corresponding item from the list
                        RelationshipsDO clientsModel = selected_individual_list.remove(position);
                        clientsModel.setChecked(false);
                        // Update the tags of the remaining views
                        for (int j = 0; j < ll_individual_list.getChildCount(); j++) {
                            ImageView iv_remove = ll_individual_list.getChildAt(j).findViewById(R.id.iv_remove_opponent);
                            if (iv_remove != null) {
                                iv_remove.setTag(j);
                            }
                        }
                        String str = String.join(",", value);
                        at_individual.setText(str);
                        // Update at_add_tm text
                        StringBuilder stringBuilder = new StringBuilder();
                        for (RelationshipsDO model : selected_individual_list) {
                            stringBuilder.append(model.getName()).append(",");
                        }
                        if (stringBuilder.length() > 0) {
                            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                        }
                        String strn = stringBuilder.toString();
                        at_individual.setText(strn);
                        if (selected_individual_list.isEmpty()) {
                            ll_selected_individual.setVisibility(GONE);
                        } else {
//                            ll_selected_individual.setVisibility(View.VISIBLE);
                        }
                        //..
                    } catch (Exception e) {
                        e.fillInStackTrace();
                        AndroidUtils.showAlert(e.getMessage(), getActivity());
                    }
                }
            });
            iv_edit_opponent.setVisibility(GONE);
            ll_individual_list.addView(view_opponents);
        }
    }


    private void callClientsWebservice() {
        try {
            entities_list.clear();
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v2/relationship/client/list", "Client List", postdata.toString());
        } catch (Exception e) {
            AndroidUtils.showToast(e.getMessage(), getContext());
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    public void selected_Team(ArrayList<TeamDo> list_item) {
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
        for (int i = 0; i < selected_tm_list.size(); i++) {
            Log.d("List_Item", selected_tm_list.get(i).getName());
        }
        if (selected_tm_list.isEmpty()) {
            ll_selected_teammembers.setVisibility(GONE);
            at_add_tm.setText("");
        } else {
            loadSelectedTM();
        }
//        rv_groups_view.setVisibility(View.GONE);
//        is_clicked_team = true;
    }

    private void TeamMembersPopup() {
        try {
            if (teamList.isEmpty()) {
                for (int i = 0; i < matterList.size(); i++) {
                    if (matter_id.equals(matterList.get(i).getId())) {
                        JSONArray team_members = matterList.get(i).getMembers();
                        for (int j = 0; j < team_members.length(); j++) {
                            JSONObject jsonObject = team_members.getJSONObject(j);
                            String memberId = jsonObject.getString("id");

                            // Check if already exists
                            boolean alreadyExists = false;
                            for (TeamDo existing : teamList) {
                                if (existing.getId().equals(memberId)) {
                                    alreadyExists = true;
                                    break;
                                }
                            }

                            if (!alreadyExists) {
                                TeamDo teamModel = new TeamDo();
                                teamModel.setId(memberId);
                                teamModel.setName(jsonObject.getString("name"));
                                teamList.add(teamModel);
                            }
                        }
                    }
                }
            }

            if (teamList.isEmpty()) {
                ll_add_tm.setVisibility(GONE);
            } else {
                if (!"solo".equals(Constants.CATEGORY)) {
                    ll_add_tm.setVisibility(VISIBLE);
                }
            }
            for (int i = 0; i < teamList.size(); i++) {
                TeamDo teamModel = teamList.get(i);
                teamModel.setChecked(false);
                for (int j = 0; j < selected_tm_list.size(); j++) {
                    if (teamList.get(i).getId().matches(selected_tm_list.get(j).getId())) {
                        teamModel.setChecked(true);
//                        selected_groups_list.set(j,documentsModel);
                    }
                }
            }
            if (!teamList.isEmpty()) {
//                rv_groups_view.setVisibility(View.GONE);
//                AndroidUtils.showToast("No Team Members to View", getContext());
//                is_clicked_team = true;
//            } else {
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                rv_groups_view.setLayoutManager(layoutManager);
                // rv_groups_view.setHasFixedSize(true);
                ADAPTER_TAG = "TM";

                CommonRelationshipsAdapter documentsAdapter = new CommonRelationshipsAdapter(teamList, ADAPTER_TAG, individual_list, entity_client_list, entity_corp_client_list, documents_list, this);
                rv_groups_view.setAdapter(documentsAdapter);
                //..
                AndroidUtils.LoadList(rv_groups_view, getContext(), teamList.size(), true);
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
        } catch (Exception e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void loadSelectedClients() {
        String[] value = new String[selected_entity_client_list.size()];
        for (int i = 0; i < selected_entity_client_list.size(); i++) {
//                                value += "," + family_members.get(i);
//                               value.add(family_members.get(i));
            value[i] = selected_entity_client_list.get(i).getName();
        }
        String str = String.join(",", value);
        at_assigned_client.setText(str);
        if (!selected_entity_client_list.isEmpty()) {
            ll_selected_entity_client.setVisibility(VISIBLE);
        } else {
            ll_selected_entity_client.setVisibility(GONE);
        }
        ll_client_team_members.removeAllViews();
        for (int i = 0; i < selected_entity_client_list.size(); i++) {
            View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.edit_opponent_advocate, null);
            TextView tv_opponent_name = view_opponents.findViewById(R.id.tv_opponent_name);
            tv_opponent_name.setText(selected_entity_client_list.get(i).getName());
            ImageView iv_edit_opponent = view_opponents.findViewById(R.id.iv_edit_opponent);
            ImageView iv_remove_opponent = view_opponents.findViewById(R.id.iv_remove_opponent);
            iv_remove_opponent.setTag(i);
            iv_remove_opponent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int position = (Integer) v.getTag();
                        ll_client_team_members.removeViewAt(position);
//                            ll_selected_groups.addView(view_opponents,position);
                        RelationshipsDO teamModel = selected_entity_client_list.remove(position);
                        teamModel.setChecked(false);
//                            selected_entity_client_list.remove(position);
                        //..
                        for (int j = 0; j < ll_client_team_members.getChildCount(); j++) {
                            ImageView iv_remove = ll_client_team_members.getChildAt(j).findViewById(R.id.iv_remove_opponent);
                            if (iv_remove != null) {
                                iv_remove.setTag(j);
                            }
                        }

                        String str = String.join(",", value);
                        at_assigned_client.setText(str);
                        StringBuilder stringBuilder = new StringBuilder();
                        for (RelationshipsDO model : selected_entity_client_list) {
                            stringBuilder.append(model.getName()).append(",");
                        }

                        if (stringBuilder.length() > 0) {
                            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                        }
                        String strn = stringBuilder.toString();
                        at_assigned_client.setText(strn);
                        if (!selected_entity_client_list.isEmpty()) {
                            ll_selected_entity_client.setVisibility(VISIBLE);
                        } else {
                            ll_selected_entity_client.setVisibility(GONE);
                        }
                        //..
//                            selected_groups_list.set(position, groupsModel);
                    } catch (Exception e) {
                        e.fillInStackTrace();
                        AndroidUtils.showAlert(e.getMessage(), getActivity());
                    }
                }
            });
            iv_edit_opponent.setVisibility(GONE);
            ll_client_team_members.addView(view_opponents);
        }
    }

    private void loadSelectedTM() {
        String[] value = new String[selected_tm_list.size()];
        for (int i = 0; i < selected_tm_list.size(); i++) {
            value[i] = selected_tm_list.get(i).getName();

        }
        for (int i = 0; i < selected_tm_list.size(); i++) {
            Log.d("List_Item_tm", selected_tm_list.get(i).getName().toString());
        }
        String str = String.join(",", value);
        at_add_tm.setText(str);
//        ll_selected_teammembers.setVisibility(View.VISIBLE);
        ll_selected_team_members.removeAllViews();
        for (int i = 0; i < selected_tm_list.size(); i++) {
            View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.edit_opponent_advocate, null);
            TextView tv_opponent_name = view_opponents.findViewById(R.id.tv_opponent_name);
            tv_opponent_name.setText(selected_tm_list.get(i).getName());
            ImageView iv_edit_opponent = view_opponents.findViewById(R.id.iv_edit_opponent);
            ImageView iv_remove_opponent = view_opponents.findViewById(R.id.iv_remove_opponent);
            iv_remove_opponent.setTag(i);
//            iv_remove_opponent.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    try {
//                        int position = (Integer) v.getTag();
//
//                        // Remove the item from the list and update its state
//                        TeamDo teamModel = selected_tm_list.get(position);
//                        teamModel.setChecked(false);
//                        selected_tm_list.remove(position);
//
//                        // Remove the view from the layout
//                        ll_selected_team_members.removeViewAt(position);
//
//                        // Update the tags for the remaining items
//                        for (int j = 0; j < ll_selected_team_members.getChildCount(); j++) {
//                            ImageView iv_remove = ll_selected_team_members.getChildAt(j).findViewById(R.id.iv_remove_opponent);
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
            iv_remove_opponent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int position = (Integer) v.getTag();

                        // Get the team member being removed
                        TeamDo teamModel = selected_tm_list.get(position);

                        // Find the same team member in tmList and update its checked state
                        for (TeamDo model : teamList) {
                            if (model.getId().equals(teamModel.getId())) {
                                model.setChecked(false);
                                break;
                            }
                        }

                        // Remove the item from selected_tm_list
                        selected_tm_list.remove(position);

                        // Remove the view from the layout
                        ll_selected_team_members.removeViewAt(position);

                        // Update the tags for the remaining items
                        for (int j = 0; j < ll_selected_team_members.getChildCount(); j++) {
                            ImageView iv_remove = ll_selected_team_members.getChildAt(j).findViewById(R.id.iv_remove_opponent);
                            if (iv_remove != null) {
                                iv_remove.setTag(j);
                            }
                        }

                        // Update the text and visibility
                        StringBuilder stringBuilder = new StringBuilder();
                        for (TeamDo model : selected_tm_list) {
                            stringBuilder.append(model.getName()).append(",");
                        }
                        if (stringBuilder.length() > 0) {
                            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                        }
                        at_add_tm.setText(stringBuilder.toString());

                        ll_selected_teammembers.setVisibility(selected_tm_list.isEmpty() ? GONE : VISIBLE);

                        // Notify RecyclerView Adapter about the change
                        if (rv_groups_view.getAdapter() instanceof CommonRelationshipsAdapter) {
                            CommonRelationshipsAdapter adapter = (CommonRelationshipsAdapter) rv_groups_view.getAdapter();
                            adapter.notifyDataSetChanged(); // Notify adapter of data changes
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        AndroidUtils.showAlert(e.getMessage(), getActivity());
                    }
                }
            });

            iv_edit_opponent.setVisibility(GONE);
            ll_selected_team_members.addView(view_opponents);
        }
    }

    private void loadProjectData_edit(String selected_project) {
        switch (selected_project) {
            case "legal":
                callProjectWebservice(matter_legal);
                callProjectWebservice(matter_legal);
                loadRepetetions();
                try {
                    loadEntitiesSpinnerData();
                } catch (JSONException e) {
                    e.fillInStackTrace();
                }
                legalTaksList.add(new TaskDo("Case Filling"));
                legalTaksList.add(new TaskDo("Consultation"));
                legalTaksList.add(new TaskDo("Creating Legal Breifs"));
                legalTaksList.add(new TaskDo("Meeting with client"));
                legalTaksList.add(new TaskDo("Hearing"));
                loadTaskList(legalTaksList);
                break;
            case "general":
                legalTaksList.clear();
                loadRepetetions();
                callProjectWebservice(matter_legal);
                try {
                    loadEntitiesSpinnerData();
                } catch (JSONException e) {
                    e.fillInStackTrace();
                }
//                TaskDo consultation_general = new TaskDo("Consultation");
//                TaskDo draft_agreements = new TaskDo("Draft agreements");
//                TaskDo fwa = new TaskDo("Filling with authorities");
//                TaskDo mwc_general = new TaskDo("Meeting with client");
//                TaskDo paf = new TaskDo("Prepare annual fillings");
                legalTaksList.add(new TaskDo("Consultation"));
                legalTaksList.add(new TaskDo("Draft agreements"));
                legalTaksList.add(new TaskDo("Filling with authorities"));
                legalTaksList.add(new TaskDo("Meeting with client"));
                legalTaksList.add(new TaskDo("Prepare annual fillings"));
                loadTaskList(legalTaksList);
                break;
            case "overhead":
                legalTaksList.clear();
                loadRepetetions();
                if (!Constants.ROLE.equals("AAM"))
                    callClientsWebservice();
//                matter_legal = ""
//                TaskDo conference = new TaskDo("Conference");
//                TaskDo holidays = new TaskDo("Holidays");
//                TaskDo research = new TaskDo("Research");
//                TaskDo training = new TaskDo("Training");
//                TaskDo vacation = new TaskDo("Vacation");
                legalTaksList.add(new TaskDo("Conference"));
                legalTaksList.add(new TaskDo("Holidays"));
                legalTaksList.add(new TaskDo("Research"));
                legalTaksList.add(new TaskDo("Training"));
                legalTaksList.add(new TaskDo("Vacation"));
                loadTaskList(legalTaksList);
                break;
            case "others":
                if (!Constants.ROLE.equals("AAM"))
                    callClientsWebservice();
                loadRepetetions();
                legalTaksList.clear();
                legalTaksList.add(new TaskDo("Business Development"));
                legalTaksList.add(new TaskDo("Personal"));
                legalTaksList.add(new TaskDo("Doctor Appointment"));
                legalTaksList.add(new TaskDo("Lunch/Dinner"));
                legalTaksList.add(new TaskDo("Misc"));
                loadTaskList(legalTaksList);
                break;
            case "reminders":
                legalTaksList.clear();
                hideAlldetails();
                if (!Constants.ROLE.equals("AAM"))
                    callClientsWebservice();
                loadRepetetions();
                break;
        }
    }

    private void loadProjectData(String selected_project) {
        switch (selected_project) {
            case "Legal Matter":
                loadClearedLists();
                unHideMatterDetails();
                matter_legal = "legal";
                callProjectWebservice(matter_legal);
//                hide_documents();
//                callClientsWebservice();
//                TaskDo caseFilling = new TaskDo("Case Filling");
//                TaskDo consultation = new TaskDo("Consultation");
//                TaskDo clb = new TaskDo("Creating Legal Breifs");
//                TaskDo mwc = new TaskDo("Meeting with client");
//                TaskDo hearing = new TaskDo("Hearing");
                legalTaksList.add(new TaskDo("Case Filling"));
                legalTaksList.add(new TaskDo("Consultation"));
                legalTaksList.add(new TaskDo("Creating Legal Briefs"));
                legalTaksList.add(new TaskDo("Meeting with client"));
                legalTaksList.add(new TaskDo("Hearing"));
                loadTaskList(legalTaksList);
                break;
            case "General Matter":
                legalTaksList.clear();
                unHideMatterDetails();
                loadClearedLists();
                matter_legal = "general";
                callProjectWebservice(matter_legal);
//                hide_documents();
//                TaskDo consultation_general = new TaskDo("Consultation");
//                TaskDo draft_agreements = new TaskDo("Draft agreements");
//                TaskDo fwa = new TaskDo("Filling with authorities");
//                TaskDo mwc_general = new TaskDo("Meeting with client");
//                TaskDo paf = new TaskDo("Prepare annual fillings");
                legalTaksList.add(new TaskDo("Consultation"));
                legalTaksList.add(new TaskDo("Draft agreements"));
                legalTaksList.add(new TaskDo("Filling with authorities"));
                legalTaksList.add(new TaskDo("Meeting with client"));
                legalTaksList.add(new TaskDo("Prepare annual fillings"));
                loadTaskList(legalTaksList);
                break;
            case "Overhead":
                legalTaksList.clear();
                hideMatterDetails();
                loadClearedLists();
                callTeamMemberWebservice();
                matter_legal = "overhead";
//                hide_documents();
//                matter_legal = ""
//                TaskDo conference = new TaskDo("Conference");
//                TaskDo holidays = new TaskDo("Holidays");
//                TaskDo research = new TaskDo("Research");
//                TaskDo training = new TaskDo("Training");
//                TaskDo vacation = new TaskDo("Vacation");
                legalTaksList.add(new TaskDo("Conference"));
                legalTaksList.add(new TaskDo("Holidays"));
                legalTaksList.add(new TaskDo("Research"));
                legalTaksList.add(new TaskDo("Training"));
                legalTaksList.add(new TaskDo("Vacation"));
                loadTaskList(legalTaksList);
                HideClientView();
                load_member_view();
//                ll_add_to_timesheet.setVisibility(VISIBLE);
                break;
            case "Others":
                legalTaksList.clear();
                hideMatterDetails();
                loadClearedLists();
                callTeamMemberWebservice();
//                display_check_list();
                matter_legal = "others";
//                hide_documents();
//                TaskDo business_development = new TaskDo("Business Development");
//                TaskDo personal = new TaskDo("Personal");
//                TaskDo doctor_appointment = new TaskDo("Doctor Appointment");
//                TaskDo lunch_dinner = new TaskDo("Lunch/Dinner");
//                TaskDo misc = new TaskDo("Misc");
                legalTaksList.add(new TaskDo("Business Development"));
                legalTaksList.add(new TaskDo("Personal"));
                legalTaksList.add(new TaskDo("Doctor Appointment"));
                legalTaksList.add(new TaskDo("Lunch/Dinner"));
                legalTaksList.add(new TaskDo("Misc"));
                loadTaskList(legalTaksList);
                HideClientView();
                load_member_view();
//                ll_add_to_timesheet.setVisibility(VISIBLE);
                break;
            case "Reminders":
                ll_add_to_timesheet.setVisibility(GONE);
                legalTaksList.clear();
                hideAlldetails();
                loadClearedLists();
                callTeamMemberWebservice();
//                display_check_list();
                matter_legal = "reminders";
                HideClientView();
                load_member_view();
                break;
        }
    }

//    private void checkAAM() {
//        if (matter_legal.equals("overhead") || matter_legal.equals("others") || (matter_legal.equals("reminders"))) {
//            HideClientView();
//        }
//    }

    private void HideClientView() {
        if (Constants.ROLE.equals("AAM")) {
            ll_add_corp_clients.setVisibility(GONE);
            ll_add_entities.setVisibility(GONE);
            ll_individual.setVisibility(GONE);
        }
        display_members();
    }

    private void loadClearedLists() {
        legalTaksList.clear();
        matterList.clear();
        entity_client_list.clear();
        individual_list.clear();
        documents_list.clear();
        entities_list.clear();
        Corp_client_list.clear();
        Corp_Team_members_list.clear();
        selected_tm_list.clear();
        corp_client_id = "";
        entity_id = "";
        teamList.clear();
        entity_corp_client_list.clear();
        //
        img_clear_sp_entity.setVisibility(GONE);
        img_dropdown_sp_entity.setVisibility(VISIBLE);
        img_clear_sp_task.setVisibility(GONE);
        img_dropdown_sp_task.setVisibility(VISIBLE);
        img_clear_sp_corp_clients.performClick();
        selected_entity_corp_client_list.clear();
        ll_corp_clients.setVisibility(GONE);
        ll_assign_clients.setVisibility(GONE);
//        ll_client_team_members.setVisibility(View.GONE);
        ll_selected_teammembers.setVisibility(GONE);
        ll_selected_corp_clients_view.setVisibility(GONE);
        ll_selected_individual.setVisibility(GONE);
//        at_assigned_corp_client.setText("");
//        ll_selected_entity_client.setVisibility(View.GONE);
        at_assigned_client.setText("");
    }

    private void load_member_view() {
        cv_add_clients.setVisibility(VISIBLE);
        if (!"solo".equals(Constants.CATEGORY)) {
            ll_add_tm.setVisibility(VISIBLE);
        }
        ll_add_entities.setVisibility(VISIBLE);
        ll_individual.setVisibility(VISIBLE);
        ll_documents_view.setVisibility(GONE);
    }

    private void load_clear_list() {
        at_add_tm.setText("");
        at_assigned_client.setText("");
        at_individual.setText("");
        at_attach_document.setText("");
        tv_sp_entity.setText("");
        at_assigned_corp_client.setText("");

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
        entity_client_list.clear();
        individual_list.clear();
        documents_list.clear();
        entities_list.clear();
        Corp_client_list.clear();
        Corp_Team_members_list.clear();
        selected_tm_list.clear();
        corp_client_id = "";
        entity_id = "";
        teamList.clear();
        entity_corp_client_list.clear();
        //
        img_clear_sp_entity.setVisibility(GONE);
        img_dropdown_sp_entity.setVisibility(VISIBLE);
//        img_clear_sp_task.setVisibility(View.GONE);
//        img_dropdown_sp_task.setVisibility(View.VISIBLE);
        img_clear_sp_corp_clients.performClick();
        selected_entity_corp_client_list.clear();
        ll_corp_clients.setVisibility(GONE);
        ll_assign_clients.setVisibility(GONE);
//        ll_client_team_members.setVisibility(View.GONE);
        ll_selected_teammembers.setVisibility(GONE);
        ll_selected_corp_clients_view.setVisibility(GONE);
        ll_selected_individual.setVisibility(GONE);
//        at_assigned_corp_client.setText("");
//        ll_selected_entity_client.setVisibility(View.GONE);
    }

    private void clear_selected_list() {
        Corp_client_list.clear();
        selected_documents_list.clear();
        selected_entity_client_list.clear();
        selected_individual_list.clear();
        selected_entity_corp_client_list.clear();
        selected_tm_list.clear();
        ll_selected_corp_clients.removeAllViews();
        ll_selected_team_members.removeAllViews();
        ll_individual_list.removeAllViews();
        ll_documents_list.removeAllViews();
        ll_selected_entites.removeAllViews();
        ll_client_team_members.removeAllViews();
        ll_selected_entity_client.setVisibility(GONE);
    }

    private void display_members() {
        if ((!teamList.isEmpty()) || (!entities_list.isEmpty()) || (!individual_list.isEmpty()) || (!documents_list.isEmpty())) {
            cv_add_clients.setVisibility(VISIBLE);
        } else {
            cv_add_clients.setVisibility(GONE);
        }
    }

    private void hide_members() {
        img_clear_sp_mattername.setVisibility(GONE);
        img_dropdown_sp_mattername.setVisibility(VISIBLE);
        cv_add_clients.setVisibility(GONE);
//        ll_add_tm.setVisibility(View.GONE);
        ll_add_entities.setVisibility(GONE);
        ll_individual.setVisibility(GONE);
        ll_documents_view.setVisibility(GONE);
    }

    private void loadRepetetions() {
        Repetetions.clear();
        Repetetions.add("None");
        Repetetions.add("Daily");
        Repetetions.add("Weekly");
        Repetetions.add("Bi-Weekly");
        Repetetions.add("Monthly");
        Repetetions.add("Yearly");
        repetitionAdapter = new CommonSpinnerAdapter((Activity) getContext(), Repetetions);
        sp_repetetion.setAdapter(repetitionAdapter);
        AndroidUtils.LoadList(sp_repetetion, getContext(), Repetetions.size(), true);
        //..
        sp_repetetion.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                repeat_interval = (String) parent.getItemAtPosition(position);
                AndroidUtils.DisplaySpinnerView(sp_repetetion, tv_sp_repetetion, repeat_interval, img_dropdown_sp_repetetion, img_clear_sp_repetetion, false, repetitionAdapter, "Search Repetetion");
                if (repeat_interval.equals("None")) {
                    isAddTimesheet = true;
                    if (Constants.ROLE.equals("AAM")) {
                        ll_add_to_timesheet.setVisibility(GONE);
                        isAddTimesheet = false;
                    } else {
                        if (!matter_legal.equals("reminders"))
                            ll_add_to_timesheet.setVisibility(VISIBLE);
                    }
                } else {
                    isAddTimesheet = false;
                    ll_add_to_timesheet.setVisibility(GONE);
                }
                ischecked_repetetion = true;
            }
        });
    }

    private void hideAlldetails() {
        ll_matter_name.setVisibility(GONE);
        tv_no_matter.setVisibility(GONE);
        ll_message.setVisibility(VISIBLE);
        cv_meeting_details.setVisibility(GONE);
        ll_task.setVisibility(GONE);
        ll_documents_view.setVisibility(GONE);
    }

    private void hideMatterDetails() {
        cv_meeting_details.setVisibility(VISIBLE);
        ll_matter_name.setVisibility(GONE);
        tv_no_matter.setVisibility(GONE);
        ll_message.setVisibility(GONE);
        cb_add_to_timesheet.setChecked(isAddTimesheet);
//        isAddTimesheet = isAddTimesheet;
        if (Constants.ROLE.equals("AAM")) {
            ll_add_to_timesheet.setVisibility(GONE);
            isAddTimesheet = false;
        } else {
            if (!matter_legal.equals("reminders"))
                ll_add_to_timesheet.setVisibility(VISIBLE);
        }
        ll_task.setVisibility(VISIBLE);
//        ll_documents_view.setVisibility(View.GONE);
    }

    private void unHideMatterDetails() {
        cv_meeting_details.setVisibility(VISIBLE);
        ll_matter_name.setVisibility(VISIBLE);
        tv_no_matter.setVisibility(GONE);
        img_clear_sp_mattername.setVisibility(GONE);
        img_dropdown_sp_mattername.setVisibility(VISIBLE);
        ll_message.setVisibility(GONE);
        cb_add_to_timesheet.setChecked(isAddTimesheet);
//        isAddTimesheet = true;
        ll_task.setVisibility(VISIBLE);
//        ll_documents_view.setVisibility(View.VISIBLE);
        cv_add_clients.setVisibility(GONE);
    }

    private void loadTaskList(ArrayList<TaskDo> legalTaksList) {
        taskSpinnerAdapter = new CommonSpinnerAdapter((Activity) getContext(), legalTaksList);
        sp_task.setAdapter(taskSpinnerAdapter);
        //..
        sp_task.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TaskDo selectedTaskItem = (TaskDo) parent.getItemAtPosition(position);
                selected_task = selectedTaskItem.getTaskName();
                AndroidUtils.DisplaySpinnerView(sp_task, tv_sp_task_name, selected_task, img_dropdown_sp_task, img_clear_sp_task, false, taskSpinnerAdapter, "Search Task");
                ischecked_task = true;
            }
        });
    }

    private void callProjectWebservice(String selected_project) {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/matter/" + selected_project + "?module=meetings&status=active", "Matter List", postdata.toString());
        } catch (Exception e) {
            AndroidUtils.showToast(e.getMessage(), getContext());
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    private void callCorpClientsWebservice() {
        try {

//            https://api.staging.digicoffer.com/professional/v3/corporate/list
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/corporate/list", "Corp Client List", postdata.toString());
        } catch (Exception e) {
            AndroidUtils.showToast(e.getMessage(), getContext());
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    private void callTeamMemberWebservice() {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/event/tms", "Team List", postdata.toString());
        } catch (Exception e) {
            AndroidUtils.showToast(e.getMessage(), getContext());
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    private void updateMeetingLinkFromCurrentValues() {
        if (meetingRoomId == null || meetingRoomId.isEmpty()) return;
        if (tv_meeting_link == null) return;

        String fromTime = tv_event_start_time.getText().toString();
        String toTime = tv_event_end_time.getText().toString();

        String date = "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date parsedDate = inputFormat.parse(tv_event_creation_date.getText().toString());
            date = outputFormat.format(parsedDate);
        } catch (Exception e) {
            date = tv_event_creation_date.getText().toString();
        }

        meetingLink = getAVChatUrl(meetingRoomId, fromTime, toTime, date, Constants.NAME);
        tv_meeting_link.setText(meetingLink);
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progressDialog.isShowing() && progressDialog != null) {
            AndroidUtils.dismiss_dialog(progressDialog);
        }
        try {
            if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                if (httpResult.getRequestType().equals("Matter List")) {
                    if (!timeZonesTaskCompleted) {
                        return;
                    }
                    if (result.optBoolean("error")) {
                        tv_no_matter.setVisibility(VISIBLE);
                        ll_matter_name.setVisibility(GONE);
                        AndroidUtils.showAlert(result.optString("msg"), getActivity());
                    } else {
                        JSONArray matters = result.getJSONArray("matters");
                        matterTaskCompleted = true;
                        loadMattersList(matters);
                    }
                } else if (httpResult.getRequestType().equals("TIMEZONES")) {
                    if (!result.optBoolean("error")) {
                        JSONArray jsonArray = result.getJSONArray("timezones");
                        load_timezones(jsonArray);
                        timeZonesTaskCompleted = true;
                        if (Objects.equals(Constants.is_meeting, "Edit")) {
                            for (int i = 0; i < existing_events_list.size(); i++) {
                                Event_Details_DO event_details_do = existing_events_list.get(i);
                                matter_legal = event_details_do.getEvent_type();
                                Log.d("Created_Event_Type", matter_legal);
                                is_linked_with_timesheet = event_details_do.isIs_linked_with_timesheet();
                                existing_description = event_details_do.getDescription();
                                existing_date = event_details_do.getDate();
                                existing_dialin = event_details_do.getDialin();
                                existing_location = event_details_do.getLocation();
                                existing_end_time = event_details_do.getConverted_End_time();
                                existing_start_time = event_details_do.getConverted_Start_time();
                                existing_meeting_link = event_details_do.getMeeting_link();
                                isRecurring = event_details_do.isRecurring();
                                existing_repetetion = AndroidUtils.CapitalizeFirstLetter(event_details_do.getRepeat_interval());
                                if (existing_repetetion.equals("Biweekly")) {
                                    existing_repetetion = "Bi-Weekly";
                                }
                                if (existing_repetetion.isEmpty()) {
                                    existing_repetetion = "None";
                                }
                                event_id = event_details_do.getId();
                                matter_id = event_details_do.getMatter_id();
                                matter_name = event_details_do.getMatter_name();
                                String title = event_details_do.getTitle();
                                if (title.contains("-")) {
                                    String[] splitStrings = title.split(" - ");
                                    String firstString = splitStrings[0];
                                    String secondString = splitStrings[1];
                                    existing_task = secondString;
                                } else {
                                    existing_task = title;
                                }
                                existing_time_zone = event_details_do.getOffset_location();
                                isAddTimesheet = event_details_do.isTimesheet_added();
                                isExistingAllday = event_details_do.isAll_day();
                                isAllDay = event_details_do.isAll_day();
                            }
                            check_event();
                            loadProjectData(Event_name);
                            loadExistingData();
                            cb_all_day.setChecked(isAllDay);
                        }
                    } else {
                        AndroidUtils.showAlert("Something went wrong", getActivity());
                    }
                } else if (httpResult.getRequestType().equals("Team List")) {
                    if (result.optBoolean("error")) {
                        AndroidUtils.showAlert(result.optString("msg"), getActivity());
                    } else {
                        JSONArray jsonArray = result.getJSONArray("users");
                        loadTeamList(jsonArray);
                        tmTaskCompleted = true;
                    }
                    if (!Constants.ROLE.equals("AAM"))
                        callClientsWebservice();
                } else if (httpResult.getRequestType().equals("Client List")) {
                    if (result.optBoolean("error")) {
                        AndroidUtils.showAlert(result.optString("msg"), getActivity());
                    } else {
                        JSONObject jsonObject = result.getJSONObject("data");
                        JSONArray jsonArray = jsonObject.getJSONArray("relationships");
                        loadRelationshipsList(jsonArray);
                        callCorpClientsWebservice();
                    }
                } else if (httpResult.getRequestType().equals("Corp Client List")) {
                    if (result.optBoolean("error")) {
                        AndroidUtils.showAlert(result.optString("msg"), getActivity());
                    } else {
                        JSONArray jsonArray = result.getJSONArray("relationships");
                        loadcorpclientslist(jsonArray);
                    }
                    if (!entity_id.isEmpty()) {
                        callEntityClientWebservice(entity_id);
                    } else if (!corp_client_id.isEmpty()) {
                        callEntityCorporateClientWebservice(corp_client_id);
                    }
                } else if (httpResult.getRequestType().equals("Entity Client List")) {
                    if (result.optBoolean("error", false)) {
                        AndroidUtils.showAlert(result.optString("msg"), getActivity());
                    } else {
                        JSONArray jsonArray = result.getJSONArray("users");
                        loadEntity_Clients(jsonArray);
                    }
                    if (!corp_client_id.isEmpty()) {
                        callEntityCorporateClientWebservice(corp_client_id);
                    }
                } else if (httpResult.getRequestType().equals("Entity Corp Client List")) {
                    if (result.optBoolean("error", false)) {
                        AndroidUtils.showAlert(result.optString("msg"), getActivity());
                    } else {
                        JSONArray jsonArray = result.getJSONArray("users");
                        loadEntity_Corp_Clients(jsonArray);
                    }
                } else if (httpResult.getRequestType().equals("EDIT_EVENT")) {
                    if (result.optBoolean("error", false)) {
                        AndroidUtils.showAlert(result.optString("msg"), getActivity());
                    } else if (result.has("errors")) {
                        JSONArray errors = result.getJSONArray("errors");
                        String error_msg = "";
                        for (int i = 0; i < errors.length(); i++) {
                            JSONObject error = errors.getJSONObject(i);
                            error_msg = error.getString("msg");
                        }
                        AndroidUtils.showAlert(error_msg, getActivity());
                    } else {
                        AndroidUtils.showAlert(result.getString("msg"), getActivity());
                        loadClearedLists();
                        progressDialog.dismiss();
                        meetings.loadView();
                    }
                } else if (httpResult.getRequestType().equals("CREATE_EVENT")) {
                    if (result.has("errors")) {
                        JSONArray jsonArray = result.getJSONArray("errors");
                        JSONObject iserror = jsonArray.getJSONObject(0);
                        String FieldName = iserror.getString("field");
                        String msg = iserror.getString("msg");
                        AndroidUtils.showAlert(msg, getActivity());
                    } else if (result.optBoolean("error", false)) {
                        AndroidUtils.showAlert(result.optString("msg"), getActivity());
                    } else {
                        AndroidUtils.showAlert(result.optString("msg"), getActivity());
                        loadClearedLists();
                        meetings.loadView();
                    }
                } else if (httpResult.getRequestType().equals("CREATE_MEETING_LINK")) {
                    meetingRoomId = result.optString("roomId", "");

                    if (!meetingRoomId.isEmpty() && tv_meeting_link != null) {
                        String fromTime = tv_event_start_time.getText().toString();
                        String toTime = tv_event_end_time.getText().toString();

                        // Convert "May 25, 2026" → "2026-05-25"
                        String date = "";
                        try {
                            SimpleDateFormat inputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                            Date parsedDate = inputFormat.parse(tv_event_creation_date.getText().toString());
                            date = outputFormat.format(parsedDate);
                        } catch (Exception e) {
                            date = tv_event_creation_date.getText().toString();
                            Log.e("AVChat_Date", "Date parse failed: " + e.getMessage());
                        }

                        meetingLink = getAVChatUrl(meetingRoomId, fromTime, toTime, date, Constants.NAME);
                        tv_meeting_link.setText(meetingLink);
                        Log.d("AVChat_MeetingLink", "Final Join URL: " + meetingLink);
                    }
                }
            } else {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                if (result.optBoolean("error")) {
                    AndroidUtils.showAlert(result.optString("msg"), getActivity());
                }
            }
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    private void loadCreatEvent() {
        try {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            Meetings nonSubmittedTimesheets = new Meetings();
            ft.replace(R.id.id_framelayout, nonSubmittedTimesheets);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void loadEntity_Corp_Clients(JSONArray jsonArray) throws JSONException {
        entity_corp_client_list.clear();
        for (int i = 0; i < jsonArray.length(); i++) {
            RelationshipsDO relationshipsDO = new RelationshipsDO();
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            relationshipsDO.setId(corp_client_id + "_" + jsonObject.getString("id"));
            relationshipsDO.setName(corp_client_name + " - " + jsonObject.getString("name"));
            entity_corp_client_list.add(relationshipsDO);
        }
        if (entity_corp_client_list.isEmpty()) {
            ll_corp_clients.setVisibility(GONE);
            ll_corp_tm.setVisibility(GONE);
        } else {
            ll_corp_clients.setVisibility(VISIBLE);
            ll_corp_tm.setVisibility(VISIBLE);
        }
//        loadSelectedClients();
    }

    private void loadEntity_Clients(JSONArray jsonArray) throws JSONException {
        entity_client_list.clear();
        for (int i = 0; i < jsonArray.length(); i++) {
            RelationshipsDO relationshipsDO = new RelationshipsDO();
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            relationshipsDO.setId(entity_id + "_" + jsonObject.getString("id"));
            relationshipsDO.setName(entity_name + " - " + jsonObject.getString("name"));
            entity_client_list.add(relationshipsDO);
        }
        if (entity_client_list.isEmpty()) {
            ll_assign_clients.setVisibility(GONE);
            ll_entity_tm.setVisibility(GONE);
        } else {
            ll_entity_tm.setVisibility(VISIBLE);
            ll_assign_clients.setVisibility(VISIBLE);
        }
        Log.d("Size", "" + entity_client_list.size());
//        loadSelectedClients();
    }

    public void selected_entity_clients(ArrayList<RelationshipsDO> list_item) {
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
            ll_selected_entity_client.setVisibility(GONE);
            at_assigned_client.setText("");
        } else {
            loadSelectedClients();
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

    /// /        rv_clients_view.setVisibility(View.GONE);
    /// /        is_clicked_clients = true;
//    }
    private void loadentity_corp_clients_popup() {
        try {
            if (!entity_corp_client_list.isEmpty()) {
//                rv_clients_view.setVisibility(View.GONE);
//                is_clicked_clients = true;
//                AndroidUtils.showToast("No Clients to show", getContext());
//            } else {
                for (int i = 0; i < entity_corp_client_list.size(); i++) {
                    RelationshipsDO teamModel = entity_corp_client_list.get(i);
                    teamModel.setChecked(false);
                    for (int j = 0; j < selected_entity_corp_client_list.size(); j++) {
                        if (entity_corp_client_list.get(i).getId().matches(selected_entity_corp_client_list.get(j).getId())) {
                            teamModel.setChecked(true);
                        }
                    }
                }
//                selected_entity_client_list.clear();
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                rv_corp_client_view.setLayoutManager(layoutManager);
                //rv_corp_client_view.setHasFixedSize(true);
                ADAPTER_TAG = "CORPORATE";
                CommonRelationshipsAdapter documentsAdapter = new CommonRelationshipsAdapter(teamList, ADAPTER_TAG, individual_list, entity_client_list, entity_corp_client_list, documents_list, this);
                rv_corp_client_view.setAdapter(documentsAdapter);
                //..
                AndroidUtils.LoadList(rv_corp_client_view, getContext(), entity_corp_client_list.size(), true);
                //..
//                btn_assigned_clients.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        for (int i = 0; i < documentsAdapter.getEntity_client_list().size(); i++) {
//                            RelationshipsDO teamModel = documentsAdapter.getEntity_client_list().get(i);
//                            if (teamModel.isChecked()) {
//                                if (!selected_entity_client_list.contains(teamModel)) {
//                                    selected_entity_client_list.add(teamModel);
//                                }
//                            }
//                        }
//                        loadSelectedClients();
//                        rv_clients_view.setVisibility(View.GONE);
//                        is_clicked_clients = true;
//                    }
//                });
            }
        } catch (Exception e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void loadEntityClientPopup() {
        try {
            if (!entity_client_list.isEmpty()) {
//                rv_clients_view.setVisibility(View.GONE);
//                is_clicked_clients = true;
//                AndroidUtils.showToast("No Clients to show", getContext());
//            } else {
                for (int i = 0; i < entity_client_list.size(); i++) {
                    RelationshipsDO teamModel = entity_client_list.get(i);
                    teamModel.setChecked(false);
                    for (int j = 0; j < selected_entity_client_list.size(); j++) {
                        if (entity_client_list.get(i).getId().matches(selected_entity_client_list.get(j).getId())) {
                            teamModel.setChecked(true);
                        }
                    }
                }
//                selected_entity_client_list.clear();
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                rv_clients_view.setLayoutManager(layoutManager);
                // rv_clients_view.setHasFixedSize(true);
                ADAPTER_TAG = "ENTITY";
                CommonRelationshipsAdapter documentsAdapter = new CommonRelationshipsAdapter(teamList, ADAPTER_TAG, individual_list, entity_client_list, entity_corp_client_list, documents_list, this);
                rv_clients_view.setAdapter(documentsAdapter);
                //..
                AndroidUtils.LoadList(rv_clients_view, getContext(), entity_client_list.size(), true);
                //..
//                btn_assigned_clients.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        for (int i = 0; i < documentsAdapter.getEntity_client_list().size(); i++) {
//                            RelationshipsDO teamModel = documentsAdapter.getEntity_client_list().get(i);
//                            if (teamModel.isChecked()) {
//                                if (!selected_entity_client_list.contains(teamModel)) {
//                                    selected_entity_client_list.add(teamModel);
//                                }
//                            }
//                        }
//                        loadSelectedClients();
//                        rv_clients_view.setVisibility(View.GONE);
//                        is_clicked_clients = true;
//                    }
//                });
            }
        } catch (Exception e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void loadcorpclientslist(JSONArray jsonArray) throws JSONException {
        Corp_client_list.clear();
        for (int i = 0; i < jsonArray.length(); i++) {
            RelationshipsDO relationshipsDO = new RelationshipsDO();
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            relationshipsDO.setId(jsonObject.getString("id"));
            relationshipsDO.setName(jsonObject.getString("name"));
            relationshipsDO.setType(jsonObject.getString("type"));
            Corp_client_list.add(relationshipsDO);
        }
        if (Corp_client_list.isEmpty()) {
            ll_add_corp_clients.setVisibility(GONE);
        } else {
            ll_add_corp_clients.setVisibility(VISIBLE);
        }
        loadEntitiesSpinnerData();
    }

    private void loadRelationshipsList(JSONArray jsonArray) throws JSONException {
        individual_list.clear();
        entities_list.clear();
        for (int i = 0; i < jsonArray.length(); i++) {
            RelationshipsDO relationshipsDO = new RelationshipsDO();
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.getString("type").equals("consumer")) {
                relationshipsDO.setId(jsonObject.getString("id"));
                relationshipsDO.setName(jsonObject.getString("name"));
                relationshipsDO.setType(jsonObject.getString("type"));
                individual_list.add(relationshipsDO);
            } else {
                relationshipsDO.setId(jsonObject.getString("id"));
                relationshipsDO.setName(jsonObject.getString("name"));
                relationshipsDO.setType(jsonObject.getString("type"));
                entities_list.add(relationshipsDO);
            }
        }
        if (entities_list.isEmpty()) {
            ll_add_entities.setVisibility(GONE);
        } else {
            ll_add_entities.setVisibility(VISIBLE);
        }
        if (individual_list.isEmpty()) {
            ll_individual.setVisibility(GONE);
        } else {
            ll_individual.setVisibility(VISIBLE);
        }
        loadEntitiesSpinnerData();
    }

    private void loadEntitiesSpinnerData() throws JSONException {
        if (entities_list.isEmpty()) {
            for (int i = 0; i < matterList.size(); i++) {
                if (matter_id.equals(matterList.get(i).getId())) {
                    JSONArray entities = matterList.get(i).getClients();
                    for (int j = 0; j < entities.length(); j++) {
                        RelationshipsDO relationshipsDO = new RelationshipsDO();
                        JSONObject jsonObject = entities.getJSONObject(j);
                        if (jsonObject.getString("type").equals("consumer")) {
                            relationshipsDO.setId(jsonObject.getString("id"));
                            relationshipsDO.setName(jsonObject.getString("name"));
                            relationshipsDO.setType(jsonObject.getString("type"));
                            individual_list.add(relationshipsDO);
                        } else {
                            relationshipsDO.setId(jsonObject.getString("id"));
                            relationshipsDO.setName(jsonObject.getString("name"));
                            relationshipsDO.setType(jsonObject.getString("type"));
                            entities_list.add(relationshipsDO);
                        }
                    }
                    JSONArray corporate = matterList.get(i).getCorporate();
                    {
                        if (matterList.get(i).isCorp_has_value()) {
                            for (int j = 0; j < corporate.length(); j++) {
                                RelationshipsDO relationshipsDO = new RelationshipsDO();
                                JSONObject jsonObject = corporate.getJSONObject(j);
                                relationshipsDO.setId(jsonObject.getString("id"));
                                relationshipsDO.setName(jsonObject.getString("name"));
                                relationshipsDO.setType(jsonObject.getString("type"));
                                Corp_client_list.add(relationshipsDO);
                            }
                        }
                    }
                }
            }
            if (Corp_client_list.isEmpty()) {
                ll_add_corp_clients.setVisibility(GONE);
            } else {
                ll_add_corp_clients.setVisibility(VISIBLE);
            }
            if (entities_list.isEmpty()) {
                ll_entity_tm.setVisibility(GONE);
                ll_add_entities.setVisibility(GONE);
            } else {
                ll_add_entities.setVisibility(VISIBLE);
            }
            if (individual_list.isEmpty()) {
                ll_individual.setVisibility(GONE);
            } else {
                ll_individual.setVisibility(VISIBLE);
            }
            TeamMembersPopup();
            Documents_Popup();
        }
        corpAdapter = new CommonSpinnerAdapter(getActivity(), Corp_client_list);
        sp_corp_clients.setAdapter(corpAdapter);
        entityAdapter = new CommonSpinnerAdapter(getActivity(), entities_list);
        sp_entity.setAdapter(entityAdapter);
        //..
        AndroidUtils.LoadList(sp_corp_clients, getContext(), Corp_client_list.size(), true);
        //..
        sp_corp_clients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RelationshipsDO selectedCorpClientItem = (RelationshipsDO) parent.getItemAtPosition(position);
                corp_client_id = selectedCorpClientItem.getId();
                corp_client_name = selectedCorpClientItem.getName();
//                selected_entity_corp_client_list.clear();
                callEntityCorporateClientWebservice(corp_client_id);
                AndroidUtils.DisplaySpinnerView(sp_corp_clients, tv_sp_corp_clients, corp_client_name, img_dropdown_sp_corp_clients, img_clear_sp_corp_clients, false, corpAdapter, "Search Corp Client");
                is_corp = true;
            }
        });
        //..
        AndroidUtils.LoadList(sp_entity, getContext(), entities_list.size(), true);
        //..
//        callTimeZoneWebservice();
        sp_entity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RelationshipsDO selectedEntityItem = (RelationshipsDO) parent.getItemAtPosition(position);
                entity_id = selectedEntityItem.getId();
                entity_name = selectedEntityItem.getName();
                callEntityClientWebservice(entity_id);
                AndroidUtils.DisplaySpinnerView(sp_entity, tv_sp_entity, entity_name, img_dropdown_sp_entity, img_clear_sp_entity, false, entityAdapter, "Search Entity");
                is_entity = true;
            }
        });
    }

    private void callEntityCorporateClientWebservice(String id) {
        try {
            //https://api.staging.digicoffer.com/professional/related/entities/tms/64f18b78fffd8f4f4623ea3f
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "related/entities/tms/" + id, "Entity Corp Client List", postdata.toString());
        } catch (Exception e) {
            AndroidUtils.showToast(e.getMessage(), getContext());
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    private void callEntityClientWebservice(String id) {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "related/entities/tms/" + id, "Entity Client List", postdata.toString());
        } catch (Exception e) {
            AndroidUtils.showToast(e.getMessage(), getContext());
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    private void loadTeamList(JSONArray jsonArray) throws JSONException {
        teamList.clear();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            TeamDo teamDo = new TeamDo();
            teamDo.setId(jsonObject.getString("id"));
            teamDo.setName(jsonObject.getString("name"));
            teamList.add(teamDo);
        }
        if (teamList.isEmpty()) {
            ll_add_tm.setVisibility(GONE);
        } else {
            if (!"solo".equals(Constants.CATEGORY)) {
                ll_add_tm.setVisibility(VISIBLE);
            }
        }
        TeamMembersPopup();
    }

//    private void loadTeamSpinner() {
//        final AutocompleteAdapter matterAutocompleteAdapter = new AutocompleteAdapter(getContext(), 1, teamList);

    /// /        callLegalwebservice();
//        at_family_members.setInputType(InputType.TYPE_NULL);
//        at_family_members.setThreshold(0);
//        at_family_members.setAdapter(matterAutocompleteAdapter);
//        at_family_members.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
//        at_family_members.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                TeamDo selectedItem = matterAutocompleteAdapter.getItem(position);
//                matterAutocompleteAdapter.remove(selectedItem);
//                matterAutocompleteAdapter.notifyDataSetChanged();
//            }
//        });
//    }
    private void load_timezones(JSONArray jsonArray) throws JSONException {
        TimeZonesDO timeZonesDO;
        timeZonesList.clear();
        timesPosHash.clear();
        for (int i = 0; i < jsonArray.length(); i++) {
            timeZonesDO = new TimeZonesDO();
            timeZonesDO.setNAME(String.valueOf(jsonArray.getJSONArray(i).get(1)));
            timeZonesDO.setGMT(String.valueOf(jsonArray.getJSONArray(i).get(0)));
            timeZonesList.add(timeZonesDO);
            timesPosHash.put(String.valueOf(jsonArray.getJSONArray(i).get(0)), i);
        }
        loadTimeZonespinner();
    }

    private void loadTimeZonespinner() {
        timezoneAdapter = new CommonSpinnerAdapter(getActivity(), timeZonesList);
        sp_time_zone.setAdapter(timezoneAdapter);
        AndroidUtils.LoadList(sp_time_zone, getContext(), timeZonesList.size(), true);

        Calendar calendar = new GregorianCalendar();
//        final long hours = TimeUnit.MILLISECONDS.toMinutes(offset1);
        for (int j = 0; j < timeZonesList.size(); j++) {
            String timezone_offset = timeZonesList.get(j).getNAME();
            if (timezone_offset.equals("(GMT+05:30) India Standard Time - Kolkata")) {
                sp_time_zone.setSelection(j);
                timezone_location = (timeZonesList.get(j).getNAME());
                offset = Integer.parseInt(timeZonesList.get(j).getGMT());
                AndroidUtils.DisplaySpinnerView(sp_time_zone, tv_sp_time_zone, timezone_location, img_dropdown_sp_timezone, img_clear_sp_timezone, false, timezoneAdapter, "Search TimeZone");
            }
        }
//        callTimeZoneWebservice();
        sp_time_zone.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TimeZonesDO selectedTimeZoneItem = (TimeZonesDO) parent.getItemAtPosition(position);
                offset = Integer.parseInt(selectedTimeZoneItem.getGMT());
                timezone_location = selectedTimeZoneItem.getNAME();
                Log.d("Timezone", timezone_location);
                AndroidUtils.DisplaySpinnerView(sp_time_zone, tv_sp_time_zone, timezone_location, img_dropdown_sp_timezone, img_clear_sp_timezone, false, timezoneAdapter, "Search TimeZone");
                ischecked_time = true;
            }
        });
    }

    private void loadMattersList(JSONArray matters) throws JSONException {
        matterList.clear();
//        AndroidUtils.showAlert(matters.toString(),getContext());
        for (int i = 0; i < matters.length(); i++) {
            JSONObject jsonObject = matters.getJSONObject(i);
            ViewMatterModel viewMatterModel = new ViewMatterModel();
            viewMatterModel.setId(jsonObject.optString("id"));
            viewMatterModel.setTitle(jsonObject.optString("title"));
            viewMatterModel.setStatus(jsonObject.optString("status"));
            viewMatterModel.setDocuments(jsonObject.optJSONArray("documents"));
            viewMatterModel.setClients(jsonObject.optJSONArray("clients"));
            viewMatterModel.setMembers(jsonObject.optJSONArray("members"));
            viewMatterModel.setCorporate(jsonObject.optJSONArray("corporate"));
            if (jsonObject.has("corporate")) {
                viewMatterModel.setCorp_has_value(true);
            } else {
                viewMatterModel.setCorp_has_value(false);
            }
//            viewMatterModel.setCasetype(jsonObject.getString("caseType"));
            matterList.add(viewMatterModel);
        }
        if (matterList.isEmpty()) {
            tv_no_matter.setVisibility(VISIBLE);
            ll_matter_name.setVisibility(GONE);
            Constants.create_matter = true;
            if (matter_legal.equals("general")) {
                Constants.MATTER_TYPE = "General";
            } else {
                Constants.MATTER_TYPE = "Legal";
            }
            AndroidUtils.showReDirectionPopup(getActivity(), new Matter(),
                    "Please create a matter to create an event. Click here to create matter.");
        } else {
            tv_no_matter.setVisibility(GONE);
            ll_matter_name.setVisibility(VISIBLE);
        }
//        AndroidUtils.showAlert(matters.toString(),getContext());
        loadMatterSpinnerList(matterList);
        if (Objects.equals(Constants.is_meeting, "Edit")) {
            loadEntitiesSpinnerData();
            if (!entity_id.isEmpty()) {
                callEntityClientWebservice(entity_id);
            } else if (!corp_client_id.isEmpty()) {
                callEntityCorporateClientWebservice(corp_client_id);
            }
        }
    }

    private void loadMatterSpinnerList(ArrayList<ViewMatterModel> matterList) {

        matterSpinnerAdapter = new CommonSpinnerAdapter((Activity) getContext(), matterList);
        sp_matter_name.setAdapter(matterSpinnerAdapter);
        //..
        AndroidUtils.LoadList(sp_matter_name, getContext(), matterList.size(), true);
        //..
        sp_matter_name.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ViewMatterModel selectedMatterItem = (ViewMatterModel) adapterView.getItemAtPosition(position);
                matter_id = selectedMatterItem.getId();
                matter_name = selectedMatterItem.getTitle();
                tv_sp_matter_name.setText(matter_name);
                ll_assign_clients.setVisibility(GONE);

                load_clear_list();
                clear_selected_list();
                if (matterList.get(position).getStatus().equals("Closed")) {
                    ll_add_to_timesheet.setVisibility(GONE);
                    isAddTimesheet = false;
                } else {
//                    isAddTimesheet = true;
                    if (Constants.ROLE.equals("AAM")) {
                        ll_add_to_timesheet.setVisibility(GONE);
                        isAddTimesheet = false;
                    } else {
                        if (!matter_legal.equals("reminders"))
                            ll_add_to_timesheet.setVisibility(VISIBLE);
                    }
                }
                ll_selected_teammembers.setVisibility(GONE);
                ll_selected_entity_client.setVisibility(GONE);//clients
                ll_selected_individual.setVisibility(GONE);
                ll_selected_documents.setVisibility(GONE);
                try {
                    loadEntitiesSpinnerData();
                } catch (JSONException e) {
                    e.fillInStackTrace();
                }
                hide_all_list();
                AndroidUtils.DisplaySpinnerView(sp_matter_name, tv_sp_matter_name, matter_name, img_dropdown_sp_mattername, img_clear_sp_mattername, false, matterSpinnerAdapter, "Search Matter");
                display_members();
                ischecked_matter = true;
            }
        });
    }

    public CreateEvent(Meetings meetings1, ArrayList<Event_Details_DO> event_details_list) {
        meetings = meetings1;
        existing_events_list = event_details_list;
    }

    private void CheckUpdateScope(String recurring_edit_choice) {
//        if ((!existing_repetetion.equals("None")) || (!tv_sp_repetetion.getText().toString().equals("None"))) {
//            AndroidUtils.showConfirmationDialog(getContext(), "Confirmation", "Are you sure you want to update this event?", "", "", new AndroidUtils.OnConfirmListener() {
//                        @Override
//                        public void onSave() {
////                            event_update_scope = "UPDATE_BOTH";
//                            event_update_scope = "UPDATE_EVENT_ONLY";
//                            callCreateEventWebservice_edit(recurring_edit_choice);
//                        }
//
//                        @Override
//                        public void onCancel() {
////                            event_update_scope = "UPDATE_EVENT_ONLY";
////                            callCreateEventWebservice_edit(recurring_edit_choice);
//                        }
//                    }
//            );
//        } else
        if (is_linked_with_timesheet && isAddTimesheet) {
            AndroidUtils.showConfirmationDialog(getContext(), "Confirmation", "This event has an associated timesheet entry. " +
                            "Do you want to update the timesheet too?", requireContext().getString(R.string.update_both), requireContext().getString(R.string.update_event_only), new AndroidUtils.OnConfirmListener() {
                        @Override
                        public void onSave() {
                            event_update_scope = "UPDATE_BOTH";
                            callCreateEventWebservice_edit(recurring_edit_choice);
                        }

                        @Override
                        public void onCancel() {
                            event_update_scope = "UPDATE_EVENT_ONLY";
                            callCreateEventWebservice_edit(recurring_edit_choice);
                        }
                    }
            );
        } else {
            AndroidUtils.showConfirmationDialog(getContext(), "Confirmation", "Are you sure you want to update this event?", "", "", new AndroidUtils.OnConfirmListener() {
                        @Override
                        public void onSave() {
//                            event_update_scope = "UPDATE_BOTH";
                            event_update_scope = "UPDATE_EVENT_ONLY";
                            callCreateEventWebservice_edit(recurring_edit_choice);
                        }

                        @Override
                        public void onCancel() {
//                            event_update_scope = "UPDATE_EVENT_ONLY";
//                            callCreateEventWebservice_edit(recurring_edit_choice);
                        }
                    }
            );
        }
    }

    private void callCreateEventWebservice_edit(String recurring_edit_choice) {
        try {
            String doctype = "doctype";
            String docid = "docid";
            JSONObject postData = new JSONObject();
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONArray selected_team_member = new JSONArray();
            JSONArray selected_clients_list = new JSONArray();
            JSONArray selected_corp_clients_list = new JSONArray();
            JSONArray individual_array = new JSONArray();
            JSONArray added_notification_list = new JSONArray();
            JSONArray time_sheets = new JSONArray();
            JSONArray existing_attachments = new JSONArray();
            for (int i = 0; i < selectedValues.size(); i++) {
//            NotifyMeDo notifyMeDo = notifyme_list.get(i);
                added_notification_list.put(selectedValues.get(i));
            }

            JSONObject selected_docs;
            for (int j = 0; j < selected_documents_list.size(); j++) {
                selected_docs = new JSONObject();
                DocumentsDo attachDocumentsDO = selected_documents_list.get(j);
                selected_docs.put(doctype, attachDocumentsDO.getDoctype());
                selected_docs.put(docid, attachDocumentsDO.getDocid());
                existing_attachments.put(selected_docs);
            }

            for (int i = 0; i < selected_tm_list.size(); i++) {
                TeamDo addTeamMembersDo = selected_tm_list.get(i);
                selected_team_member.put(addTeamMembersDo.getId());
            }
            for (int i = 0; i < selected_entity_client_list.size(); i++) {
                RelationshipsDO addClientsDo = selected_entity_client_list.get(i);
                selected_clients_list.put(addClientsDo.getId());
            }
            for (int i = 0; i < selected_entity_corp_client_list.size(); i++) {
                RelationshipsDO addClientsDo = selected_entity_corp_client_list.get(i);
                selected_corp_clients_list.put(addClientsDo.getId());
            }
            for (int i = 0; i < selected_individual_list.size(); i++) {
                RelationshipsDO relationshipsDO = selected_individual_list.get(i);
                individual_array.put(relationshipsDO.getId());
            }
            //...

            Date event_date = AndroidUtils.stringToDateTimeDefault(tv_event_creation_date.getText().toString(), "MMM dd, yyyy");
            event_creation_date = AndroidUtils.getDateToString(event_date, "yyyy-MM-dd");
            Date event_start_date = null;
            Date event_date2 = null;
            String start_time = tv_event_start_time.getText().toString();
            if (start_time.isEmpty() || start_time == null) {
                start_time = "00:00";
                event_start_date = AndroidUtils.stringToDateTimeDefault(start_time, "HH:mm");

            } else {
                event_start_date = AndroidUtils.stringToDateTimeDefault(start_time, "HH:mm");
            }
            event_starting_date = AndroidUtils.getDateToString(event_start_date, event_creation_date + "'T'HH:mm:ss");
            String end_time = tv_event_end_time.getText().toString();
            if (end_time.isEmpty() || end_time == null) {
                end_time = "00:00";
                event_date2 = AndroidUtils.stringToDateTimeDefault(end_time, "HH:mm");
            } else {
                event_date2 = AndroidUtils.stringToDateTimeDefault(end_time, "HH:mm");
            }
            String duration_timesheet = "";
            if (event_start_date != null && event_date2 != null) {

                long differenceInMilliSeconds = Math.abs(event_start_date.getTime() - event_date2.getTime());
                long differenceInHours = (differenceInMilliSeconds / (60 * 60 * 1000)) % 24;
                long differenceInMinutes
                        = (differenceInMilliSeconds / (60 * 1000)) % 60;
                long differenceInSeconds
                        = (differenceInMilliSeconds / 1000) % 60;
                String duration = differenceInHours + ":" + differenceInMinutes;
                Date hours = AndroidUtils.stringToDateTimeDefault(duration, "HH:mm");
                duration_timesheet = AndroidUtils.getDateToString(hours, "HH:mm");
            }
            event_end_time = AndroidUtils.getDateToString(event_date2, event_creation_date + "'T'HH:mm:ss");

            //......

            if (isAddTimesheet) {
                JSONObject time_sheet_obj;
                time_sheet_obj = new JSONObject();
                time_sheet_obj.put("date", event_creation_date);
                if (isAllDay) {
                    time_sheet_obj.put("duration", "23:59");
                    event_starting_date = event_creation_date + "T00:00:00";
                    event_end_time = event_creation_date + "T23:59:59";
                } else {
                    if (duration_timesheet.isEmpty()) {
                        time_sheet_obj.put("duration", "30:00");
                    } else {
                        time_sheet_obj.put("duration", duration_timesheet);
                    }
                }
                time_sheet_obj.put("eventtitle", tv_sp_task_name.getText().toString());
                time_sheet_obj.put("addedby", Constants.NAME);
                time_sheet_obj.put("user_id", Constants.USER_ID);
                if ((!matter_legal.equals("legal") && (!matter_legal.equals("general")))) {
                    time_sheet_obj.put("matter_id", tv_sp_project.getText().toString());
                    time_sheet_obj.put("matter_type", matter_legal);
                }
                time_sheets.put(time_sheet_obj);
            }
            int multiplied_offset = (-1) * (offset);
            String dateInput = event_creation_date; // "yyyy-MM-dd"

// Parse in UTC (prevents shifting)
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(dateInput);

// Now set exact UTC midnight
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

// Output
            SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String finalDate = utcFormat.format(cal.getTime());

            postData.put("date", finalDate);

            postData.put("attachments", existing_attachments);
            postData.put("invitees_internal", selected_team_member);
            postData.put("invitees_corporate", selected_corp_clients_list);
            postData.put("invitees_external", selected_clients_list);
            postData.put("invitees_consumer_external", individual_array);
            if (matter_legal.equals("legal") || matter_legal.equals("general")) {
                postData.put("title", matter_name + " - " + tv_sp_task_name.getText().toString());
                postData.put("dialin", tv_dialing_number.getText().toString());
                postData.put("location", tv_location.getText().toString());
                postData.put("addtimesheet", isAddTimesheet);
                postData.put("timesheets", time_sheets);
                postData.put("description", tv_description.getText().toString());
            } else if (matter_legal.equals("overhead") || (matter_legal.equals("others"))) {
                postData.put("dialin", tv_dialing_number.getText().toString());
                postData.put("location", tv_location.getText().toString());
                postData.put("addtimesheet", isAddTimesheet);
                postData.put("timesheets", time_sheets);
                postData.put("description", tv_description.getText().toString());
                postData.put("title", tv_sp_task_name.getText().toString());
            } else {
                postData.put("title", tv_sp_task_name.getText().toString());
                postData.put("description", tv_message.getText().toString());
            }
//            if (is_linked_with_timesheet) {
            postData.put("event_update_scope", event_update_scope);
//            }
            postData.put("notifications", added_notification_list);
//            postData.put("description", tv_description.getText().toString());
            postData.put("timezone_location", timezone_location);
            postData.put("timezone_offset", multiplied_offset);
            String repetetion = "";
            if (tv_sp_repetetion.getText().toString().toLowerCase(Locale.ROOT).equals("bi-weekly")) {
                repetetion = "biweekly";
            } else if (tv_sp_repetetion.getText().toString().equals("None")) {
                repetetion = "";
            } else {
                repetetion = tv_sp_repetetion.getText().toString().toLowerCase(Locale.ROOT);
            }
            Log.d("Repetation", repetetion);
            postData.put("repeat_interval", repetetion);
            postData.put("meeting_link", Objects.requireNonNull(tv_meeting_link.getText()).toString());
            postData.put("allday", isAllDay);
            postData.put("recurrent_edit_choice", recurring_edit_choice);
            postData.put("from_ts", event_starting_date);
            postData.put("to_ts", event_end_time);
            if (matter_legal.equals("legal") || matter_legal.equals("general")) {
                postData.put("matter_type", matter_legal);
                postData.put("matter_id", matter_id);
            }
            postData.put("event_type", matter_legal);
            Log.d("Event_Edit", postData.toString());
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "v3/event/" + event_id + "/" + multiplied_offset, "EDIT_EVENT", postData.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void update_event() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getLayoutInflater();
            final View dialogLayout = inflater.inflate(R.layout.edit_recurring_choice, null);
            final LinearLayout ll_only_this = dialogLayout.findViewById(R.id.radio_delete_only_this);
            ll_only_this.setBackground(getContext().getDrawable(R.drawable.background_transparent));
            if (existing_repetetion.equals(tv_sp_repetetion.getText().toString())) {
                ll_only_this.setVisibility(VISIBLE);
            } else {
                ll_only_this.setVisibility(GONE);
            }
            final LinearLayout ll_all = dialogLayout.findViewById(R.id.radio_delete_all);
            final CheckBox delete_all = ll_all.findViewById(R.id.chk_select_all);
            final TextView delete_all_txt = ll_all.findViewById(R.id.chk_select_all_text);
            ll_all.setBackground(getContext().getDrawable(R.drawable.background_transparent));
            delete_all_txt.setText(R.string.all_events);
            delete_all_txt.setTextSize(DynamicUtils.twenty);
            final LinearLayout ll_following = dialogLayout.findViewById(R.id.radio_delete_ts_fe);
            final CheckBox delete_following = ll_following.findViewById(R.id.chk_select_all);
            final TextView delete_following_txt = ll_following.findViewById(R.id.chk_select_all_text);
            ll_following.setBackground(getContext().getDrawable(R.drawable.background_transparent));
            delete_following_txt.setText(R.string.this_and_following_events);
            delete_following_txt.setTextSize(DynamicUtils.twenty);
            final Button delete = dialogLayout.findViewById(R.id.delete_event);
            final Button btn_close_event = dialogLayout.findViewById(R.id.btn_close_event);
            btn_close_event.setTextColor(Color.RED);
            btn_close_event.setText(R.string.cancel);

            final CheckBox delete_only_this = ll_only_this.findViewById(R.id.chk_select_all);
            final TextView delete_only_this_txt = ll_only_this.findViewById(R.id.chk_select_all_text);
            ll_only_this.setBackground(getContext().getDrawable(R.drawable.background_transparent));
            delete_only_this_txt.setText(R.string.this_event);
            delete_only_this_txt.setTextSize(DynamicUtils.twenty);

            delete_only_this.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delete_only_this.setChecked(true);
                    recurring_edit_choice = "this";
                    delete_all.setChecked(false);
                    delete_following.setChecked(false);
                }
            });
            delete_following.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delete_following.setChecked(true);
                    recurring_edit_choice = "forward";
                    delete_all.setChecked(false);
                    delete_only_this.setChecked(false);
                }
            });
            delete_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delete_all.setChecked(true);
                    recurring_edit_choice = "all";
                    delete_following.setChecked(false);
                    delete_only_this.setChecked(false);
                }
            });

            final AlertDialog dialog = builder.create();
            progressDialog = dialog;

            btn_close_event.setOnClickListener(v -> progressDialog.dismiss());
            delete.setOnClickListener(view -> {
                if (delete_following.isChecked() || delete_all.isChecked() || delete_only_this.isChecked()) {
                    progressDialog.dismiss();
                    CheckUpdateScope(recurring_edit_choice);
                } else {
                    AndroidUtils.showAlert("Please choose one of the edit recurring event", getActivity());
                }
            });
            dialog.setView(dialogLayout);
            dialog.show();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void loadExistingData() {
        btn_create_event.setText(R.string.update);
        cv_add_clients.setVisibility(VISIBLE);
        ll_add_entities.setVisibility(VISIBLE);
        ll_individual.setVisibility(VISIBLE);
        if (!"solo".equals(Constants.CATEGORY)) {
            ll_add_tm.setVisibility(VISIBLE);
        }
        hide_documents();
        ll_sp_project.setEnabled(false);
        tv_sp_project.setClickable(false);
        ll_sp_mattername.setEnabled(false);
        tv_sp_matter_name.setClickable(false);
        //...
        tv_sp_project.setText(Event_name);
        tv_sp_matter_name.setText(matter_name);
        if (!tv_sp_project.getText().toString().isEmpty())
            ll_project.setVisibility(VISIBLE);
        if (!tv_sp_matter_name.getText().toString().isEmpty()) {
            ll_matter_name.setVisibility(VISIBLE);
            tv_no_matter.setVisibility(GONE);
        }

        ll_assign_clients.setVisibility(VISIBLE);
        if (Objects.equals(matter_legal, "reminders")) {
            tv_message.setText(existing_description);
            ll_task.setVisibility(GONE);
        } else {
            ll_task.setVisibility(VISIBLE);
        }
        //event
        img_clear_sp_project.setEnabled(false);
        img_clear_sp_project.setVisibility(VISIBLE);
        img_dropdown_sp_project.setVisibility(GONE);
        //matter
        img_clear_sp_mattername.setEnabled(false);
        img_clear_sp_mattername.setVisibility(VISIBLE);
        img_dropdown_sp_mattername.setVisibility(GONE);
        //task
        img_clear_sp_task.setVisibility(VISIBLE);
        img_dropdown_sp_task.setVisibility(GONE);

        ll_repetetion.setVisibility(VISIBLE);
        tv_sp_task_name.setText(existing_task);
        tv_description.setText(existing_description);
        tv_sp_repetetion.setText(existing_repetetion);
        img_dropdown_sp_repetetion.setVisibility(GONE);
        img_clear_sp_repetetion.setVisibility(VISIBLE);
        tv_meeting_link.setText(existing_meeting_link);
        tv_event_creation_date.setText(AndroidUtils.formatToMMMddYYYY(existing_date));

        tv_location.setText(existing_location);
        tv_dialing_number.setText(existing_dialin);
        tv_event_creation_date.setText(AndroidUtils.formatToMMMddYYYY(existing_date));
//        if (isAddTimesheet) {
//        isAddTimesheet = true;
        cb_add_to_timesheet.setChecked(isAddTimesheet);
//        cb_add_to_timesheet.setEnabled(false);
        if (existing_repetetion.equals("None")) {
            if (Constants.ROLE.equals("AAM")) {
                isAddTimesheet = false;
                ll_add_to_timesheet.setVisibility(GONE);
            } else {
                if ((matter_legal.equals("reminders")) || (matter_legal.equals("overhead")) || (matter_legal.equals("others")))
                    ll_add_to_timesheet.setVisibility(GONE);
                else ll_add_to_timesheet.setVisibility(VISIBLE);
            }
//            isAddTimesheet = true;
        } else {
//            isAddTimesheet = false;
            ll_add_to_timesheet.setVisibility(GONE);
        }
//        }
        for (int i = 0; i < timeZonesList.size(); i++) {
            if (existing_time_zone.equals(timeZonesList.get(i).getNAME())) {
                sp_time_zone.setSelection(i);
                timezone_location = (timeZonesList.get(i).getNAME());
                offset = Integer.parseInt(timeZonesList.get(i).getGMT());
            }
        }
        tv_sp_time_zone.setText(timezone_location);

        for (int i = 0; i < Repetetions.size(); i++) {
            if (existing_repetetion.equals(Repetetions.get(i))) {
                sp_repetetion.setSelection(i);
            }
        }
        try {
            for (int i = 0; i < existing_events_list.size(); i++) {
                JSONArray documents = existing_events_list.get(i).getAttachments();
                for (int j = 0; j < documents.length(); j++) {
                    DocumentsDo documentsDo = new DocumentsDo();
                    JSONObject jsonObject = documents.getJSONObject(j);
                    documentsDo.setDocid(jsonObject.getString("docid"));
                    documentsDo.setDoctype(jsonObject.getString("doctype"));
                    documentsDo.setName(jsonObject.getString("name"));
//                    documentsDo.setUser_id(jsonObject.getString("user_id"));
                    selected_documents_list.add(documentsDo);
                }
            }
            if (!selected_documents_list.isEmpty()) {
                loadSelectedDocuments();
            }
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
        try {
            for (int i = 0; i < existing_events_list.size(); i++) {
                JSONArray team_members = existing_events_list.get(i).getTeam_name();
                for (int j = 0; j < team_members.length(); j++) {
                    TeamDo teamModel = new TeamDo();
                    JSONObject jsonObject = team_members.getJSONObject(j);
                    teamModel.setId(jsonObject.getString("id"));
                    teamModel.setName(jsonObject.getString("name"));
                    selected_tm_list.add(teamModel);
                }
            }
            if (!selected_tm_list.isEmpty()) {
                loadSelectedTM();
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        try {
            for (int i = 0; i < existing_events_list.size(); i++) {
                JSONArray clients = existing_events_list.get(i).getTm_name();
                for (int j = 0; j < clients.length(); j++) {
                    RelationshipsDO relationshipsDO = new RelationshipsDO();
                    JSONObject jsonObject = clients.getJSONObject(j);
                    entity_id = jsonObject.getString("entityId");
                    relationshipsDO.setId(jsonObject.getString("entityId") + "_" + jsonObject.getString("tmId"));
                    entity_name = jsonObject.getString("entityName");
                    if (!entity_name.isEmpty())
                        relationshipsDO.setName(entity_name + " - " + jsonObject.getString("tmName"));
                    else
                        relationshipsDO.setName(jsonObject.getString("tmName"));
                    selected_entity_client_list.add(relationshipsDO);
                }
            }

            for (int i = 0; i < existing_events_list.size(); i++) {
                JSONArray corporate = existing_events_list.get(i).getCorporate();
                for (int j = 0; j < corporate.length(); j++) {
                    RelationshipsDO relationshipsDO = new RelationshipsDO();
                    JSONObject jsonObject = corporate.getJSONObject(j);
                    corp_client_id = jsonObject.getString("entityId");
                    relationshipsDO.setId(jsonObject.getString("entityId") + "_" + jsonObject.getString("tmId"));
                    relationshipsDO.setName(jsonObject.getString("tmName"));
                    corp_client_name = jsonObject.getString("entityName");
                    if (!corp_client_name.isEmpty())
                        relationshipsDO.setName(corp_client_name + " - " + jsonObject.getString("tmName"));
                    else
                        relationshipsDO.setName(jsonObject.getString("tmName"));
                    selected_entity_corp_client_list.add(relationshipsDO);
                }
            }
            if (entity_name != null)
                if (!entity_name.isEmpty()) {
                    tv_sp_entity.setText(entity_name);
                    img_clear_sp_entity.setVisibility(VISIBLE);
                    img_dropdown_sp_entity.setVisibility(GONE);
//                callEntityClientWebservice(entity_id);
                    Log.d("Entity_id", entity_id);
                }
            if (corp_client_name != null)
                if (!corp_client_name.isEmpty()) {
                    tv_sp_corp_clients.setText(corp_client_name);
                    img_clear_sp_corp_clients.setVisibility(VISIBLE);
                    img_dropdown_sp_corp_clients.setVisibility(GONE);
//                callEntityClientWebservice(entity_id);
                    Log.d("Entity_id", corp_client_id);
                }

            if (!selected_entity_client_list.isEmpty()) {
                ll_entity_tm.setVisibility(VISIBLE);
                loadSelectedClients();
            } else {
                ll_entity_tm.setVisibility(GONE);
            }
            if (!selected_entity_corp_client_list.isEmpty()) {
                ll_corp_clients.setVisibility(VISIBLE);
                loadselectedCorpClients();
            } else {
                ll_corp_clients.setVisibility(GONE);
            }
//            if (!selected_entity_corp_client_list.isEmpty()) {
//                ll_corp_clients.setVisibility(View.VISIBLE);
//                ll_corp_tm.setVisibility(View.GONE);
//                loadselectedCorpClients();
//            }
            Log.d("selected_Corpoarte_client", "" + selected_entity_corp_client_list.size());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        try {
            for (int i = 0; i < existing_events_list.size(); i++) {
                Log.d("Selected_Documents", existing_events_list.get(i).getConsumer_external().toString());
                JSONArray individuals = existing_events_list.get(i).getConsumer_external();
                for (int j = 0; j < individuals.length(); j++) {
                    RelationshipsDO relationshipsDO = new RelationshipsDO();
                    JSONObject jsonObject = individuals.getJSONObject(j);
                    relationshipsDO.setId(jsonObject.getString("entityId"));
                    relationshipsDO.setName(jsonObject.getString("tmName"));
                    relationshipsDO.setType(jsonObject.getString("tmId"));
                    selected_individual_list.add(relationshipsDO);
                }
            }
            loadSelectedIndividual();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        if (selected_documents_list.isEmpty()) {
            ll_selected_documents.setVisibility(GONE);
        } else {
//            ll_selected_documents.setVisibility(View.VISIBLE);
        }
        if (selected_tm_list.isEmpty()) {
            ll_selected_teammembers.setVisibility(GONE);
        } else {
//            ll_selected_teammembers.setVisibility(View.VISIBLE);
        }
        if (selected_entity_client_list.isEmpty()) {
            ll_selected_entity_client.setVisibility(GONE);
        } else {
            ll_selected_entity_client.setVisibility(VISIBLE);
        }
        if (selected_individual_list.isEmpty()) {
            ll_selected_individual.setVisibility(GONE);
        } else {
//            ll_selected_individual.setVisibility(View.VISIBLE);
        }
        //Device Compatibility Issue with Null Value...
        String st_time = "";
        String ed_time = "";
        if (isExistingAllday) {
            cb_all_day.setChecked(true);
        }
        if (isExistingAllday) {
            ll_time.setVisibility(GONE);
            isAllDay = true;
            display_duration("0:00", "23:59");
            cb_all_day.setChecked(true);
        } else {
            tv_event_start_time.setText(existing_start_time);
            tv_event_end_time.setText(existing_end_time);
            display_duration(Objects.requireNonNull(tv_event_start_time.getText()).toString(), Objects.requireNonNull(tv_event_end_time.getText()).toString());
            ll_time.setVisibility(VISIBLE);
            isAllDay = false;
            cb_all_day.setChecked(false);
        }
        try {
            for (int i = 0; i < existing_events_list.size(); i++) {
                JSONArray notification = existing_events_list.get(i).getNotifications();
                if (notification.length() > 0) {
                    for (int j = 0; j < notification.length(); j++) {
                        selectedValues.add(notification.get(j).toString());
                        String value_notify = notification.getString(j);
                        String[] time = value_notify.split("-");
                        time_format = time[0];
                        time_value = time[1];
                        NotificationPopup();
                        Log.d("selected_Values1", "" + selectedValues.size());
                    }
                }
//                else {
//                    Log.d("selected_Values2", "" + selectedValues.size());
//                    isFirstTime = true;
//                    is_notify_clicked = false;
//                    NotificationPopup();
//                }
            }
        } catch (Exception e) {
            e.fillInStackTrace();
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
        HideClientView();
    }

    private void check_event() {
        switch (matter_legal) {
            case "legal":
                Event_name = "Legal Matter";
                break;
            case "general":
                Event_name = "General Matter";
                break;
            case "overhead":
                Event_name = "Overhead";
                break;
            case "others":
                Event_name = "Others";
                break;
            case "reminders":
                Event_name = "Reminders";
                break;
        }
    }

    private void display_check_list() {
        if ((teamList.isEmpty()) && (entities_list.isEmpty()) && individual_list.isEmpty() && documents_list.isEmpty() && (entity_client_list.size() == 0)) {
            cv_add_clients.setVisibility(GONE);
        } else {
            cv_add_clients.setVisibility(VISIBLE);
        }
        if (documents_list.isEmpty()) {
            ll_documents_view.setVisibility(GONE);
        } else {
            ll_documents_view.setVisibility(VISIBLE);
        }
        if (teamList.isEmpty()) {
            ll_add_tm.setVisibility(GONE);
        } else {
            if (!"solo".equals(Constants.CATEGORY)) {
                ll_add_tm.setVisibility(VISIBLE);
            }
        }
        if (entities_list.isEmpty()) {
            ll_add_entities.setVisibility(GONE);
        } else {
            ll_add_entities.setVisibility(VISIBLE);
        }
        if (individual_list.isEmpty()) {
            ll_individual.setVisibility(GONE);
        } else {
            ll_individual.setVisibility(VISIBLE);
        }
    }

    private void hide_documents() {
        if ((matter_legal.equals("legal")) || (matter_legal.equals("general"))) {
            ll_documents_view.setVisibility(VISIBLE);
        } else {
            ll_documents_view.setVisibility(GONE);
        }
    }

    private void hide_all_list() {
        ischecked_project = true;
        ischecked_matter = true;
        ischecked_task = true;
        ischecked_repetetion = true;
        ischecked_time = true;
        is_clicked_team = true;
        is_clicked_clients = true;
        is_clicked_documents = true;
        is_clicked_individuals = true;
        is_entity = true;

        sp_time_zone.setVisibility(GONE);
        sp_repetetion.setVisibility(GONE);
        sp_project.setVisibility(GONE);
        sp_task.setVisibility(GONE);
        sp_matter_name.setVisibility(GONE);
        rv_groups_view.setVisibility(GONE);
        rv_clients_view.setVisibility(GONE);
        sp_entity.setVisibility(GONE);
        rv_individuals_view.setVisibility(GONE);
        rv_documents_view.setVisibility(GONE);
    }

    private void display_selected_popup(String corp, boolean contains_data) {
        if (corp.equals("corp")) {
            if (contains_data)
                ll_corp_tm.setVisibility(VISIBLE);
            else
                ll_corp_tm.setVisibility(GONE);
        } else {
            if (contains_data)
                ll_entity_tm.setVisibility(VISIBLE);
            else
                ll_entity_tm.setVisibility(GONE);
        }
    }
}