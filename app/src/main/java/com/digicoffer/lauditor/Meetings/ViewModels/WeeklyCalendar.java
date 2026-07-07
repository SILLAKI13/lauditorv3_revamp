package com.digicoffer.lauditor.Meetings.ViewModels;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.EventDay;
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel;
import com.digicoffer.lauditor.Appointments.Models.PaymentModel;
import com.digicoffer.lauditor.Meetings.Models.Day;
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO;
import com.digicoffer.lauditor.Meetings.Models.Events_Do;
import com.digicoffer.lauditor.Meetings.Models.InviteesInternal_Model;
import com.digicoffer.lauditor.Meetings.Adapter.WeeklyCalendarAdapter;
import com.digicoffer.lauditor.Matter.Models.DocumentsModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.TimeSheets.Models.WeekDateInfo;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DrawableUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class WeeklyCalendar extends Fragment implements View.OnClickListener, AsyncTaskCompleteListener, Events_Adapter.EventListener, WeeklyCalendarAdapter.OnDaySelectedListener {
    Calendar calendar = Calendar.getInstance();
    WeekDateInfo weekDateInfo;
    String start_date, eventId = "";
    String end_date;
    String filter = "";
    Calendar cal;
    private Meetings.FilterType currentFilter = Meetings.FilterType.ALL;
    String recurring_edit_choice;
    private WeeklyCalendarAdapter adapter;
    private List<Day> days = new ArrayList<>();
    Dialog progress_dialog;
    String Current_month = "";
    RecyclerView rv_week_dates;
    private EventDetailsListener eventDetailsListener;
    AlertDialog ad_dialog;
    String event_creation_date = "";
    String event_end_date = "";
    String Currenr_date = "";
    RecyclerView rv_displayEvents;
    String Current_day = "";
    Events_Adapter events_adapter;
    TextView tv_from_date_timesheet, tv_to_date_timesheet;
    ArrayList<Event_Details_DO> event_details_list = new ArrayList<Event_Details_DO>();
    final Calendar mCalendar = Calendar.getInstance();
    ArrayList<Events_Do> events_list = new ArrayList<Events_Do>();
    ArrayList<Events_Do> Individual_events_list = new ArrayList<Events_Do>();
    ArrayList<Events_Do> selected_date_events = new ArrayList<Events_Do>();
    ArrayList<AppointmentModel> appointments_list = new ArrayList<>();
    ArrayList<AppointmentModel> selected_date_appointments = new ArrayList<>();
    List<EventDay> events = new ArrayList<>();
    View meetings;
    ArrayList<InviteesInternal_Model> inviteesInternalArrayList = new ArrayList<>();
    Context mcontext;
    DocumentsModel documentsModel = new DocumentsModel();
    List<DocumentsModel> documentsList = new ArrayList<>();

    // Empty state views
    LinearLayout layout_empty_state;
    TextView tv_empty_subtitle;

    public WeeklyCalendar(View meetings, Meetings.FilterType currentFilter) {
        this.currentFilter = currentFilter;
        this.meetings = meetings;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.weekly_calendar, container, false);
        ImageButton iv_next_week = v.findViewById(R.id.iv_next_week);
        iv_next_week.setImageDrawable(getContext().getDrawable(R.drawable.baseline_arrow_forward_ios_24));
        ImageButton iv_previous_week = v.findViewById(R.id.iv_previous_week);
        rv_displayEvents = (RecyclerView) v.findViewById(R.id.rv_events);
        rv_week_dates = (RecyclerView) v.findViewById(R.id.rv_week_dates);
        tv_from_date_timesheet = (TextView) v.findViewById(R.id.tv_from_date_timesheet);
        tv_to_date_timesheet = (TextView) v.findViewById(R.id.tv_to_date_timesheet);
        tv_to_date_timesheet.setTextColor(getContext().getColor(R.color.Blue_text_color));
        tv_from_date_timesheet.setTextColor(getContext().getColor(R.color.Blue_text_color));

        // Bind empty state views
        layout_empty_state = v.findViewById(R.id.layout_empty_state);
        tv_empty_subtitle = v.findViewById(R.id.tv_empty_subtitle);

        if (Constants.isFromNotification) {
            handleNotificationNavigation();
        }
        CurrentWeek();
        Currenr_date = AndroidUtils.getDateToString(Calendar.getInstance().getTime(), "dd-MM-yyyy");
        callEventListwebservice(filter);
        Constants.isfwd_or_isbwd = false;
        adapter = new WeeklyCalendarAdapter(days, this::onDaySelected, getContext());
        rv_week_dates.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rv_week_dates.setAdapter(adapter);
        iv_previous_week.setOnClickListener(view -> showPreviousWeek());
        iv_next_week.setOnClickListener(view -> showNextWeek());
        return v;
    }

    private void handleNotificationNavigation() {
        Bundle bundle = Constants.notificationBundle;
        String route = bundle.getString(Constants.NavKeys.ROUTE_NAME);
        if (route != null) {
            eventId = bundle.getString(Constants.NavKeys.EVENT_ID, "");
            assert eventId != null;
            if (!eventId.isEmpty())
                callEventDetailsWebservice();
        }
        Constants.isFromNotification = false;
        Constants.notificationBundle.clear();
    }

    private void CurrentWeek() {
        cal = Calendar.getInstance();
        int firstDayOfWeek = cal.getFirstDayOfWeek();

        while (cal.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
            cal.add(Calendar.DATE, -1);
        }

        SimpleDateFormat d1 = new SimpleDateFormat("ddMMyyyy", Locale.US);
        start_date = d1.format(cal.getTime());
        Log.d("F_date", "" + start_date);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        String fromDate = dateFormat.format(cal.getTime());

        days.clear();
        for (int i = 0; i < 7; i++) {
            String date = dateFormat.format(cal.getTime());
            days.add(new Day(date));
            cal.add(Calendar.DATE, 1);
        }

        end_date = d1.format(cal.getTime());
        Log.d("L_date", "" + end_date);

        cal.add(Calendar.DATE, -1);
        String toDate = dateFormat.format(cal.getTime());

        tv_from_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(fromDate));
        tv_to_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(toDate));
    }

    @Override
    public void onClick(View v) {

    }

    private void showPreviousWeek() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        SimpleDateFormat d1 = new SimpleDateFormat("ddMMyyyy", Locale.US);
        cal = Calendar.getInstance();

        try {
            Date currentDate = dateFormat.parse(days.get(0).getDate());
            cal.setTime(currentDate);
            cal.add(Calendar.WEEK_OF_YEAR, -1);

            start_date = d1.format(cal.getTime());
            String fromDate = dateFormat.format(cal.getTime());

            days.clear();
            for (int i = 0; i < 7; i++) {
                String date = dateFormat.format(cal.getTime());
                days.add(new Day(date));
                cal.add(Calendar.DATE, 1);
            }

            cal.add(Calendar.DATE, -1);
            end_date = d1.format(cal.getTime());
            String toDate = dateFormat.format(cal.getTime());

            Log.d("PREV_WEEK", "Start: " + start_date + " | End: " + end_date);

            tv_from_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(fromDate));
            tv_to_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(toDate));

            Constants.isfwd_or_isbwd = true;
            adapter.notifyDataSetChanged();
            events_list.clear();
            selected_date_events.clear();

            String myFormat = "MMM dd, yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            filter = sdf.format(cal.getTime());
            Currenr_date = AndroidUtils.getDateToString(Calendar.getInstance().getTime(), "dd-MM-yyyy");
            callEventListwebservice(filter);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void showNextWeek() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        SimpleDateFormat d1 = new SimpleDateFormat("ddMMyyyy", Locale.US);
        cal = Calendar.getInstance();

        try {
            Date currentDate = dateFormat.parse(days.get(0).getDate());
            cal.setTime(currentDate);
            cal.add(Calendar.WEEK_OF_YEAR, 1);

            start_date = d1.format(cal.getTime());
            String fromDate = dateFormat.format(cal.getTime());

            days.clear();
            for (int i = 0; i < 7; i++) {
                String date = dateFormat.format(cal.getTime());
                days.add(new Day(date));
                cal.add(Calendar.DATE, 1);
            }

            cal.add(Calendar.DATE, -1);
            end_date = d1.format(cal.getTime());
            String toDate = dateFormat.format(cal.getTime());

            Log.d("NEXT_WEEK", "Start: " + start_date + " | End: " + end_date);

            tv_from_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(fromDate));
            tv_to_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(toDate));

            Constants.isfwd_or_isbwd = true;
            adapter.notifyDataSetChanged();
            events_list.clear();
            selected_date_events.clear();

            String myFormat = "MMM dd, yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            filter = sdf.format(cal.getTime());
            Currenr_date = AndroidUtils.getDateToString(Calendar.getInstance().getTime(), "dd-MM-yyyy");
            callEventListwebservice(filter);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public interface EventDetailsListener {
        void onEventDetailsPassed(ArrayList<Event_Details_DO> event_details_list, String calendar_Type);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            Log.d("Interface", "Interface Called");
            eventDetailsListener = (EventDetailsListener) getParentFragment();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void callEventListwebservice(String filter) {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONObject postData = new JSONObject();
        try {
            Date event_date = AndroidUtils.stringToDateTimeDefault(filter, "MMM dd, yyyy");
            event_creation_date = AndroidUtils.getDateToString(event_date, "MMyyyy");
            Current_day = AndroidUtils.getDateToString(event_date, "dd");
            Current_month = AndroidUtils.getDateToString(event_date, "MM");
            Calendar calendar = new GregorianCalendar();
            TimeZone timeZone = calendar.getTimeZone();
            int offset = timeZone.getRawOffset();
            long hours = TimeUnit.MILLISECONDS.toMinutes(offset);
            long timezoneoffset = (-1) * (hours);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/events/" + timezoneoffset + "/" + "W" + start_date + "-" + end_date, "Events_List", postData.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void callAppointmentListWebService() {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONObject postData = new JSONObject();
        try {
            String startDate = AndroidUtils.convertAnyDateToYYYYMMDD(start_date);
            String endDate = AndroidUtils.convertAnyDateToYYYYMMDD(end_date);
            Date event_date = AndroidUtils.stringToDateTimeDefault(filter, "MMM dd, yyyy");
            event_creation_date = AndroidUtils.getDateToString(event_date, "MMyyyy");
            Current_day = AndroidUtils.getDateToString(event_date, "dd");
            Current_month = AndroidUtils.getDateToString(event_date, "MM");
            Calendar calendar = new GregorianCalendar();
            TimeZone timeZone = calendar.getTimeZone();
            int offset = timeZone.getRawOffset();
            long hours = TimeUnit.MILLISECONDS.toMinutes(offset);
            long timezoneoffset = (-1) * (hours);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/appointments?schedule_after=" + startDate + "&schedule_before=" + endDate, "Appointments_List", postData.toString());
            Log.d("AppoinemtmentListCall", "v3/appointments?schedule_after=" + startDate + "&schedule_before=" + endDate);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private WeekDateInfo getWeekDateRange(Calendar calendar) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DATE, -1);
        }
        String startDate = format.format(calendar.getTime());

        ArrayList<String> weekDates = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            String date = format.format(calendar.getTime());
            weekDates.add(date);
            calendar.add(Calendar.DATE, 1);
        }
        calendar.add(Calendar.DATE, -1);
        String endDate = format.format(calendar.getTime());
        WeekDateInfo weekDateInfo = new WeekDateInfo(startDate + " - " + endDate, weekDates);
        return weekDateInfo;
    }

    public void callEventDetailsWebservice() {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONObject postData = new JSONObject();
        Calendar calendar = new GregorianCalendar();
        TimeZone timeZone = calendar.getTimeZone();
        int offset = timeZone.getRawOffset();
        long hours = TimeUnit.MILLISECONDS.toMinutes(offset);
        long timezoneoffset = (-1) * (hours);
        WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/event/" + eventId + "/" + timezoneoffset, "INDIVIDUAL EVENT DETAILS", postData.toString());
    }

    private void load_event_details(JSONObject event_details) {
        Event_Details_DO event_details_do;
        event_details_list.clear();
        Individual_events_list.clear();
        try {
            JSONObject jsonObject = event_details;
            Events_Do events_do = new Events_Do();
            events_do.setDescription(jsonObject.getString("description"));
            events_do.setEvent_Name(jsonObject.getString("title"));
            events_do.setDialin(jsonObject.getString("dialin"));
            events_do.setEvent_type(jsonObject.getString("event_type"));
            events_do.setLocation(jsonObject.getString("location"));
            if (jsonObject.has("matter_id")) {
                events_do.setMatter_id(jsonObject.getString("matter_id"));
            }
            if (jsonObject.has("matter_name")) {
                events_do.setMatter_name(jsonObject.getString("matter_name"));
            }
            if (jsonObject.has("matter_type")) {
                events_do.setMatter_type(jsonObject.getString("matter_type"));
            }
            events_do.setRecurring(jsonObject.getBoolean("isrecurring"));
            events_do.setMeeting_link(jsonObject.getString("meeting_link"));
            events_do.setIs_linked_with_timesheet(jsonObject.optBoolean("is_linked_with_timesheet"));
            events_do.setNotes(jsonObject.getString("notes"));
            events_do.setRepeat_interval(jsonObject.getString("repeat_interval"));
            events_do.setTimezone_location(jsonObject.getString("timezone_location"));
            events_do.setTimezone_offset(jsonObject.getString("timezone_offset"));
            events_do.setAttachments(jsonObject.getJSONArray("attachments"));
            events_do.setInvitees_external(jsonObject.getJSONArray("invitees_external"));
            events_do.setInvitees_internal(jsonObject.getJSONArray("invitees_internal"));
            events_do.setNotifications(jsonObject.getJSONArray("notifications"));
            events_do.setEvent_start_time(jsonObject.getString("from_ts"));
            events_do.setEvent_end_time(jsonObject.getString("to_ts"));
            events_do.setAll_day(jsonObject.getBoolean("allday"));
            events_do.setEvent_id(jsonObject.getString("id"));
            events_do.setOwner(jsonObject.getBoolean("owner"));
            events_do.setIs_linked_with_timesheet(jsonObject.optBoolean("is_linked_with_timesheet"));
            String from_ts = events_do.getEvent_start_time();
            String to_ts = events_do.getEvent_end_time();
            Date event_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss");
            String event_start_time = AndroidUtils.getDateToString(event_date, "HH:mm a");
            events_do.setConverted_Start_time(event_start_time);
            Date event_date2 = AndroidUtils.stringToDateTimeDefault(to_ts, "yyyy-MM-dd'T'HH:mm:ss");
            String event_end_time = AndroidUtils.getDateToString(event_date2, "HH:mm a");
            events_do.setCOnverted_End_time(event_end_time);
            String converted_from_ts = AndroidUtils.getDateToString(event_date, "dd-MM-yyyy");
            String converted_to_ts = AndroidUtils.getDateToString(event_date2, "dd-MM-yyyy");
            String converted_day = AndroidUtils.getDateToString(event_date, "dd");
            int year = Integer.parseInt(AndroidUtils.getDateToString(event_date, "yyyy"));
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, event_date.getMonth(), event_date.getDate());
            events.add(new EventDay(calendar, DrawableUtils.getThreeDots(getContext())));
            Individual_events_list.add(events_do);

            event_details_do = new Event_Details_DO();
            event_details_do.setEvent_type(event_details.getString("event_type"));
            event_details_do.setId(event_details.getString("id"));
            event_details_do.setTitle(event_details.getString("title"));
            event_details_do.setDescription(event_details.getString("description"));
            event_details_do.setFrom_ts(event_details.getString("from_ts"));
            event_details_do.setAll_day(event_details.getBoolean("allday"));
            event_details_do.setIs_linked_with_timesheet(event_details.optBoolean("is_linked_with_timesheet"));
            String event_date_forevents = AndroidUtils.getDateToString(event_date, "dd-MM-yyyy");
            event_details_do.setDate(event_date_forevents);
            event_details_do.setRecurring(event_details.getBoolean("isrecurring"));
            event_details_do.setRepeat_interval(event_details.getString("repeat_interval"));
            event_details_do.setLocation(event_details.getString("location"));
            event_details_do.setDialin(event_details.getString("dialin"));
            event_details_do.setTo_ts(event_details.getString("to_ts"));
            event_details_do.setOffset(event_details.getString("timezone_offset"));
            event_details_do.setOffset_location(event_details.getString("timezone_location"));
            event_details_do.setConverted_Start_time(event_start_time);
            event_details_do.setOwner(event_details.getBoolean("owner"));
            event_details_do.setMeeting_link(event_details.getString("meeting_link"));
            event_details_do.setConverted_End_time(event_end_time);
            event_details_do.setRepeat_interval(event_details.getString("repeat_interval"));
            event_details_do.setNotifications(event_details.getJSONArray("notifications"));
            event_details_do.setOwner_name(event_details.getString("owner_name"));
            event_details_do.setAttachments(event_details.getJSONArray("attachments"));
            event_details_do.setTeam_name(event_details.getJSONArray("invitees_internal"));
            event_details_do.setTm_name(event_details.getJSONArray("invitees_external"));
            event_details_do.setCorporate(event_details.optJSONArray("invitees_corporate"));
            if (event_details.has("invitees_consumer_external")) {
                Log.d("ArrayListLog", event_details.getJSONArray("invitees_consumer_external").toString());
                event_details_do.setConsumer_external(event_details.getJSONArray("invitees_consumer_external"));
            }
            if (event_details.has("matter_name")) {
                event_details_do.setMatter_name(event_details.getString("matter_name"));
            }
            if (event_details.has("matter_id")) {
                event_details_do.setMatter_id(event_details.getString("matter_id"));
            }
            if (event_details.has("matter_type")) {
                event_details_do.setMatter_type(event_details.getString("matter_type"));
            }
            if (event_details.has("timesheet_added")) {
                event_details_do.setTimesheet_added(event_details.optBoolean("timesheet_added"));
            }
            event_details_list.add(event_details_do);

            showIndividualEventPopup(event_details_list, Individual_events_list);
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    private void showIndividualEventPopup(ArrayList<Event_Details_DO> event_details_list, ArrayList<Events_Do> eventsList) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getLayoutInflater();
            final View dialog = inflater.inflate(R.layout.recyclerview, null);
            RecyclerView rv_displayEvents = dialog.findViewById(R.id.rv_view_members);
            final AlertDialog dialogLayout = builder.create();
            dialogLayout.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialogLayout.setView(dialog);
            dialogLayout.show();
            rv_displayEvents.setLayoutManager(new GridLayoutManager(getContext(), 1));
            for (Events_Do event : events_list) {
                Log.d("Event List", event.getEvent_Name());
            }
            Log.d("Event_size.", "" + events_list.size());
            Events_Adapter events_adapter = new Events_Adapter(eventsList, this, getContext(), getActivity(), event_details_list, dialogLayout);
            rv_displayEvents.setAdapter(events_adapter);
            rv_displayEvents.setLayoutAnimation(
                    AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fall_down)
            );
            rv_displayEvents.scheduleLayoutAnimation();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void load_Nothing(Events_Adapter.MyViewHolder holder) {
        holder.tv_yes.setTextColor(Color.BLACK);
        holder.tv_no.setTextColor(Color.BLACK);
        holder.tv_maybe.setTextColor(Color.BLACK);
        holder.tv_yes.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_left_round_background));
        holder.tv_no.setBackgroundDrawable(mcontext.getDrawable(R.drawable.radiobutton_centre_background));
        holder.tv_maybe.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_right_round_background));
    }

    private void load_Yes(Events_Adapter.MyViewHolder holder) {
        holder.tv_yes.setTextColor(Color.WHITE);
        holder.tv_no.setTextColor(Color.BLACK);
        holder.tv_maybe.setTextColor(Color.BLACK);
        holder.tv_yes.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_left_green_round_background));
        holder.tv_no.setBackgroundDrawable(mcontext.getDrawable(R.drawable.radiobutton_centre_background));
        holder.tv_maybe.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_right_round_background));
    }

    private void load_Maybe(Events_Adapter.MyViewHolder holder) {
        holder.tv_yes.setTextColor(Color.BLACK);
        holder.tv_no.setTextColor(Color.BLACK);
        holder.tv_maybe.setTextColor(Color.WHITE);
        holder.tv_yes.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_left_round_background));
        holder.tv_no.setBackgroundDrawable(mcontext.getDrawable(R.drawable.radiobutton_centre_background));
        holder.tv_maybe.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_right_green_round_background));
    }

    private void load_No(Events_Adapter.MyViewHolder holder) {
        holder.tv_yes.setTextColor(Color.BLACK);
        holder.tv_no.setTextColor(Color.WHITE);
        holder.tv_maybe.setTextColor(Color.BLACK);
        holder.tv_yes.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_left_round_background));
        holder.tv_no.setBackgroundDrawable(mcontext.getDrawable(R.drawable.radiobutton_centre_green_background));
        holder.tv_maybe.setBackgroundDrawable(mcontext.getDrawable(R.drawable.button_right_round_background));
    }

    private void loadRsvpView(Events_Do events_do, Events_Adapter.MyViewHolder holder) {
        if (events_do.getUserRsvp().toLowerCase(Locale.ROOT).equals("yes")) {
            load_Yes(holder);
        } else if (events_do.getUserRsvp().toLowerCase(Locale.ROOT).equals("maybe")) {
            load_Maybe(holder);
        } else if (events_do.getUserRsvp().toLowerCase(Locale.ROOT).equals("no")) {
            load_No(holder);
        } else {
            load_Nothing(holder);
        }
    }

    private void loadAppointments(JSONArray appointments) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    appointments_list.clear();
                    selected_date_appointments.clear();

                    for (int i = 0; i < appointments.length(); i++) {
                        JSONObject jsonObject = appointments.optJSONObject(i);

                        AppointmentModel appointmentModel = new AppointmentModel();

                        appointmentModel.setId(jsonObject.optString("id"));
                        appointmentModel.setClient_id(jsonObject.optString("client_id"));
                        appointmentModel.setGuid(jsonObject.optString("guid"));
                        appointmentModel.setClient_name(jsonObject.optString("client_name"));
                        appointmentModel.setAppointment_from(jsonObject.optString("appointment_from"));
                        appointmentModel.setAppointment_to(jsonObject.optString("appointment_to"));
                        appointmentModel.setConsultation_mode(jsonObject.optString("consultation_mode"));
                        appointmentModel.setAppointment_status(jsonObject.optString("appointment_status"));
                        appointmentModel.setMeeting_room_id(jsonObject.optString("meeting_room_id"));
                        appointmentModel.setMeeting_room_expires_at(jsonObject.optString("meeting_room_expires_at"));
                        appointmentModel.setRsvp_status(jsonObject.optString("rsvp_status"));
                        appointmentModel.setCreated_at(jsonObject.optString("created_at"));
                        if (jsonObject.has("client_profile_pic")) {
                            appointmentModel.setClient_profile_pic(jsonObject.optString("client_profile_pic", ""));
                        }
                        if (jsonObject.has("services_offered")) {
                            appointmentModel.setServices_offered(jsonObject.optJSONArray("services_offered"));
                        }

                        if (jsonObject.has("payment")) {
                            JSONObject paymentObject = jsonObject.getJSONObject("payment");
                            PaymentModel payment = new PaymentModel();
                            payment.setStatus(paymentObject.optString("status"));
                            payment.setAmount_paid(paymentObject.optString("amount_paid"));
                            payment.setCurrency(paymentObject.optString("currency"));
                            payment.setSymbol(paymentObject.optString("symbol"));
                            payment.setLabel(paymentObject.optString("label"));
                            appointmentModel.setPayment(payment);
                        }

                        String from_ts = appointmentModel.getAppointment_from();
                        Date appointment_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss");
                        String converted_from_ts = AndroidUtils.getDateToString(appointment_date, "dd-MM-yyyy");

                        String to_ts = appointmentModel.getAppointment_to();
                        Date appointment_end_date = AndroidUtils.stringToDateTimeDefault(to_ts, "yyyy-MM-dd'T'HH:mm:ss");
                        String converted_to_ts = AndroidUtils.getDateToString(appointment_end_date, "dd-MM-yyyy");

                        int year = Integer.parseInt(AndroidUtils.getDateToString(appointment_date, "yyyy"));
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, appointment_date.getMonth(), appointment_date.getDate());

                        if (converted_from_ts.contains(Currenr_date)) {
                            selected_date_appointments.add(appointmentModel);
                        }

                        for (Day day : days) {
                            if (converted_from_ts.contains(day.getDate()) || converted_to_ts.contains(day.getDate())) {
                                appointments_list.add(appointmentModel);
                                break;
                            }
                        }
                    }

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Collections.sort(selected_date_appointments, new Comparator<AppointmentModel>() {
                                    @Override
                                    public int compare(AppointmentModel a1, AppointmentModel a2) {
                                        if (a1.getAppointment_from() == null || a2.getAppointment_from() == null)
                                            return 0;
                                        return a1.getAppointment_from().compareTo(a2.getAppointment_from());
                                    }
                                });

                                setEventsPerDay();
                                adapter.notifyDataSetChanged();
                                loadRecyclerView();

                                Log.d("Appointments_Loaded", "Appointments for week: " + appointments_list.size());

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AndroidUtils.showToast(e.getMessage(), getContext());
                            Log.e("LoadAppointmentsException", e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    private void setEventsPerDay() {
        Log.d("SetEventsPerDay", "Starting - Events: " + events_list.size() + ", Appointments: " + appointments_list.size() + ", Filter: " + currentFilter);

        for (Day day : days) {
            List<EventDay> dayEvents = new ArrayList<>();
            int eventCount = 0;
            int appointmentCount = 0;

            if (currentFilter == Meetings.FilterType.ALL || currentFilter == Meetings.FilterType.MY_MEETINGS) {
                for (Events_Do event : events_list) {
                    try {
                        String from_ts = event.getEvent_start_time();
                        Date event_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss");
                        String converted_from_ts = AndroidUtils.getDateToString(event_date, "dd-MM-yyyy");

                        if (converted_from_ts.equals(day.getDate())) {
                            int year = Integer.parseInt(AndroidUtils.getDateToString(event_date, "yyyy"));
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(year, event_date.getMonth(), event_date.getDate());
                            dayEvents.add(new EventDay(calendar, DrawableUtils.getThreeDots(getContext())));
                            eventCount++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (currentFilter == Meetings.FilterType.ALL || currentFilter == Meetings.FilterType.APPOINTMENTS) {
                for (AppointmentModel appointment : appointments_list) {
                    try {
                        String from_ts = appointment.getAppointment_from();
                        Date appointment_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss");
                        String converted_from_ts = AndroidUtils.getDateToString(appointment_date, "dd-MM-yyyy");

                        if (converted_from_ts.equals(day.getDate())) {
                            int year = Integer.parseInt(AndroidUtils.getDateToString(appointment_date, "yyyy"));
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(year, appointment_date.getMonth(), appointment_date.getDate());
                            dayEvents.add(new EventDay(calendar, DrawableUtils.getThreeDots(getContext())));
                            appointmentCount++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            day.setEvents(dayEvents);

            Log.d("DayEvents", "Day: " + day.getDate() + " | Events: " + eventCount + ", Appointments: " + appointmentCount + ", Total: " + dayEvents.size());
        }
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        try {
            if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    if (httpResult.getRequestType().equals("Events_List")) {
                        if (!result.getBoolean("error")) {
                            JSONArray jsonArray = result.getJSONArray("events");
                            loadEvents(jsonArray);
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), getActivity());
                        }
                        callAppointmentListWebService();
                    } else if (httpResult.getRequestType().equals("Appointments_List")) {
                        if (!result.getBoolean("error")) {
                            JSONArray jsonArray = result.getJSONArray("appointments");
                            loadAppointments(jsonArray);
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), getActivity());
                        }
                    } else if (httpResult.getRequestType().equals("INDIVIDUAL EVENT DETAILS")) {
                        if (result.has("error")) {
                            if (!result.optBoolean("error")) {
                                event_details_list.clear();
                                load_event_details(Objects.requireNonNull(result.optJSONObject("event")));
                            } else {
                                AndroidUtils.showAlert(result.optString("msg"), getActivity());
                            }
                        } else {
                            AndroidUtils.showAlert(result.optString("msg"), getActivity());
                        }
                    } else if (httpResult.getRequestType().equals("EVENT DETAILS")) {
                        if (!result.getBoolean("error")) {
                            event_details_list.clear();
                        }
                    } else if (httpResult.getRequestType().equals("EVENT_DELETE")) {
                        AndroidUtils.showToast("Event Deleted Successfully", getContext());
                        progress_dialog.dismiss();
                        event_details_list.clear();
                        events.clear();
                        callEventListwebservice(filter);
                    }
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            } else if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Failed) {
                if (progress_dialog != null && progress_dialog.isShowing())
                    AndroidUtils.dismiss_dialog(progress_dialog);
                try {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    AndroidUtils.showErrorAlert(result.optString("msg"), getActivity());
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            } else {
                if (progress_dialog != null && progress_dialog.isShowing())
                    AndroidUtils.dismiss_dialog(progress_dialog);
                AndroidUtils.showErrorAlert(httpResult.getResponseContent().toString(), getActivity());
            }
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void loadEvents(JSONArray jsonArray) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Events_Do events_do;
                    events_list.clear();
                    selected_date_events.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        events_do = new Events_Do();
                        events_do.setDescription(jsonObject.getString("description"));
                        events_do.setEvent_Name(jsonObject.getString("title"));
                        events_do.setDialin(jsonObject.getString("dialin"));
                        events_do.setEvent_type(jsonObject.getString("event_type"));
                        events_do.setLocation(jsonObject.getString("location"));
                        if (jsonObject.has("matter_id")) {
                            events_do.setMatter_id(jsonObject.getString("matter_id"));
                        }
                        if (jsonObject.has("matter_name")) {
                            events_do.setMatter_name(jsonObject.getString("matter_name"));
                        }
                        if (jsonObject.has("matter_type")) {
                            events_do.setMatter_type(jsonObject.getString("matter_type"));
                        }
                        events_do.setRecurring(jsonObject.getBoolean("isrecurring"));
                        events_do.setMeeting_link(jsonObject.getString("meeting_link"));
                        events_do.setNotes(jsonObject.getString("notes"));
                        events_do.setIs_linked_with_timesheet(jsonObject.optBoolean("is_linked_with_timesheet"));
                        events_do.setRepeat_interval(jsonObject.getString("repeat_interval"));
                        events_do.setTimezone_location(jsonObject.getString("timezone_location"));
                        events_do.setTimezone_offset(jsonObject.getString("timezone_offset"));
                        events_do.setAttachments(jsonObject.getJSONArray("attachments"));
                        events_do.setInvitees_external(jsonObject.getJSONArray("invitees_external"));
                        events_do.setInvitees_internal(jsonObject.getJSONArray("invitees_internal"));
                        events_do.setNotifications(jsonObject.getJSONArray("notifications"));
                        events_do.setEvent_start_time(jsonObject.getString("from_ts"));
                        events_do.setEvent_end_time(jsonObject.getString("to_ts"));
                        events_do.setAll_day(jsonObject.getBoolean("allday"));
                        events_do.setEvent_id(jsonObject.getString("id"));
                        events_do.setOwner(jsonObject.getBoolean("owner"));

                        String from_ts = events_do.getEvent_start_time();
                        String to_ts = events_do.getEvent_end_time();
                        Date event_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss");
                        String event_start_time = AndroidUtils.getDateToString(event_date, "HH:mm a");
                        events_do.setConverted_Start_time(event_start_time);

                        Date event_date2 = AndroidUtils.stringToDateTimeDefault(to_ts, "yyyy-MM-dd'T'HH:mm:ss");
                        String event_end_time = AndroidUtils.getDateToString(event_date2, "HH:mm a");
                        events_do.setCOnverted_End_time(event_end_time);

                        String converted_from_ts = AndroidUtils.getDateToString(event_date, "dd-MM-yyyy");
                        String converted_to_ts = AndroidUtils.getDateToString(event_date2, "dd-MM-yyyy");

                        if (converted_from_ts.toString().contains(Currenr_date)) {
                            selected_date_events.add(events_do);
                        }

                        for (Day day : days) {
                            if (converted_from_ts.contains(day.getDate()) || converted_to_ts.toString().contains(day.getDate())) {
                                events_list.add(events_do);
                                break;
                            }
                        }
                    }

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Collections.sort(selected_date_events, new Comparator<Events_Do>() {
                                    @Override
                                    public int compare(Events_Do eventDay, Events_Do t1) {
                                        if (eventDay.getConverted_Start_time() == null || t1.getConverted_Start_time() == null)
                                            return 0;
                                        return eventDay.getConverted_Start_time().compareTo(t1.getConverted_Start_time());
                                    }
                                });

                                adapter.notifyDataSetChanged();
                                loadRecyclerView();

                                Log.d("Events_Loaded", "Events for week: " + events_list.size());

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AndroidUtils.showToast(e.getMessage(), getContext());
                            Log.e("LoadPageException", e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    public void applyFilter(Meetings.FilterType filterType) {
        this.currentFilter = filterType;
        filterDataByType();
        setEventsPerDay();
        adapter.notifyDataSetChanged();
        try {
            loadRecyclerView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterDataByType() {
        selected_date_events.clear();
        selected_date_appointments.clear();

        for (Events_Do event : events_list) {
            try {
                String from_ts = event.getEvent_start_time();
                Date event_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss");
                String converted_from_ts = AndroidUtils.getDateToString(event_date, "dd-MM-yyyy");

                if (converted_from_ts.equals(Currenr_date)) {
                    if (currentFilter == Meetings.FilterType.ALL || currentFilter == Meetings.FilterType.MY_MEETINGS) {
                        selected_date_events.add(event);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (AppointmentModel appointment : appointments_list) {
            try {
                String from_ts = appointment.getAppointment_from();
                Date appointment_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss");
                String converted_from_ts = AndroidUtils.getDateToString(appointment_date, "dd-MM-yyyy");

                if (converted_from_ts.equals(Currenr_date)) {
                    if (currentFilter == Meetings.FilterType.ALL || currentFilter == Meetings.FilterType.APPOINTMENTS) {
                        selected_date_appointments.add(appointment);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Collections.sort(selected_date_events, new Comparator<Events_Do>() {
            @Override
            public int compare(Events_Do e1, Events_Do e2) {
                if (e1.getConverted_Start_time() == null || e2.getConverted_Start_time() == null)
                    return 0;
                return e1.getConverted_Start_time().compareTo(e2.getConverted_Start_time());
            }
        });

        Collections.sort(selected_date_appointments, new Comparator<AppointmentModel>() {
            @Override
            public int compare(AppointmentModel a1, AppointmentModel a2) {
                if (a1.getAppointment_from() == null || a2.getAppointment_from() == null)
                    return 0;
                return a1.getAppointment_from().compareTo(a2.getAppointment_from());
            }
        });
    }

    private void runOnUiThread(Runnable loadPageException) {
    }

    private void updateEmptyState() {
        boolean isEmpty = selected_date_events.isEmpty() && selected_date_appointments.isEmpty();
        if (isEmpty) {
            layout_empty_state.setVisibility(View.VISIBLE);
            rv_displayEvents.setVisibility(View.GONE);
            switch (currentFilter) {
                case MY_MEETINGS:
                    tv_empty_subtitle.setText("No meetings found for this date.");
                    break;
                case APPOINTMENTS:
                    tv_empty_subtitle.setText("No appointments found for this date.");
                    break;
                default:
                    tv_empty_subtitle.setText("There are no events for this date.");
                    break;
            }
        } else {
            layout_empty_state.setVisibility(View.GONE);
            rv_displayEvents.setVisibility(View.VISIBLE);
        }
    }

    private void loadRecyclerView() throws Exception {
        try {
            updateEmptyState();

            events_adapter = new Events_Adapter(
                    selected_date_events,
                    selected_date_appointments,
                    this,
                    getContext(),
                    getActivity()
            );

            rv_displayEvents.setAdapter(events_adapter);
            AndroidUtils.LoadingRecyclerview(rv_displayEvents, getContext());
        } catch (Exception e) {
            AndroidUtils.showToast(e.getMessage(), getContext());
            e.printStackTrace();
        }
    }

    @Override
    public void onEvent(ArrayList<Event_Details_DO> event_details_list) {
        if (eventDetailsListener != null) {
            Log.d("EventsList", event_details_list.toString());
            String Calendar_Type = "Weekly";
            eventDetailsListener.onEventDetailsPassed(event_details_list, Calendar_Type);
        }
    }

    @Override
    public void delete_events(Events_Do events_do) {
        delete_event(events_do);
    }

    @Override
    public void load_events() {
        callEventListwebservice(filter);
    }

    String event_delete_scope = "DELETE_EVENT_ONLY";

    public void callDeleteEventwebservice(String id, String recurring_choice, Boolean isevent_delete_scope) {
        if (isevent_delete_scope) {
            AndroidUtils.showConfirmationDialog(getContext(), "Confirmation", "This event has an associated timesheet entry. " +
                            "Do you want to update the timesheet too?", requireContext().getString(R.string.delete_both), requireContext().getString(R.string.delete_event_only), new AndroidUtils.OnConfirmListener() {
                        @Override
                        public void onSave() {
                            event_delete_scope = "DELETE_BOTH";
                            callDeleteEventWebservice(id, recurring_choice);
                        }

                        @Override
                        public void onCancel() {
                            event_delete_scope = "DELETE_EVENT_ONLY";
                            callDeleteEventWebservice(id, recurring_choice);
                        }
                    }
            );
        } else {
            event_delete_scope = "DELETE_EVENT_ONLY";
            callDeleteEventWebservice(id, recurring_choice);
        }
    }

    public void callDeleteEventWebservice(String id, String recurring_choice) {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONObject postData = new JSONObject();
        try {
            if (!(recurring_choice == null)) {
                postData.put("choice", recurring_choice);
                if (!event_delete_scope.isEmpty())
                    postData.put("event_delete_scope", event_delete_scope);
            }
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.DELETE, "v3/event/" + id, "EVENT_DELETE", postData.toString());
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    public void delete_recurring_event(String event_id, Boolean event_delete_scope) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getLayoutInflater();
            final View dialogLayout = inflater.inflate(R.layout.edit_recurring_choice, null);
            final TextView edit_event_dialog = dialogLayout.findViewById(R.id.edit_event_dialog);
            edit_event_dialog.setText(R.string.delete_recurring_event);
            final LinearLayout ll_only_this = dialogLayout.findViewById(R.id.radio_delete_only_this);
            final CheckBox delete_only_this = ll_only_this.findViewById(R.id.chk_select_all);
            final TextView delete_only_this_txt = ll_only_this.findViewById(R.id.chk_select_all_text);
            ll_only_this.setBackground(getContext().getDrawable(R.drawable.background_transparent));
            delete_only_this_txt.setText(R.string.this_event);
            delete_only_this_txt.setTextSize(DynamicUtils.twenty);
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
                    delete_only_this.setChecked(false);
                    delete_following.setChecked(false);
                }
            });

            final AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progress_dialog = dialog;
            btn_close_event.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progress_dialog.dismiss();
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (delete_only_this.isChecked() || delete_following.isChecked() || delete_all.isChecked()) {
                        progress_dialog.dismiss();
                        callDeleteEventwebservice(event_id, recurring_edit_choice, event_delete_scope);
                    } else {
                        AndroidUtils.showAlert("Please choose one of the Delete recurring event", getActivity());
                    }
                }
            });
            dialog.setView(dialogLayout);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void delete_event(Events_Do events_do) {
        AndroidUtils.Delete_Popup(getActivity(), "Are you sure do you want to delete " + events_do.getEvent_Name() + " ?", "Delete_Event", events_do.getEvent_id(), this, null, meetings, events_do.isRecurring(), events_do.isIs_linked_with_timesheet());
    }

    @Override
    public void delete(String event_id, boolean recur) {

    }

    @Override
    public void onDaySelected(Day day) {
        String selectedDate = day.getDate();
        Log.d("DaySelected", "Selected date: " + selectedDate);

        Currenr_date = selectedDate;

        filterDataByType();

        try {
            loadRecyclerView();
            Log.d("FilteredData", "Events: " + selected_date_events.size() + ", Appointments: " + selected_date_appointments.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}