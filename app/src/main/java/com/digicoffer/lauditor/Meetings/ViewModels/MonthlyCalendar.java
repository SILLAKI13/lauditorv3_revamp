package com.digicoffer.lauditor.Meetings.ViewModels;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO;
import com.digicoffer.lauditor.Meetings.Models.Events_Do;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DrawableUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MonthlyCalendar extends Fragment implements AsyncTaskCompleteListener, View.OnClickListener, Events_Adapter.EventListener {
    com.applandeo.materialcalendarview.CalendarView calendarView;
    Context thiscontext;
    Dialog progress_dialog;
    String filter = "";
    String event_id = "";
    String event_creation_date = "";
    Events_Adapter events_adapter;
    String Currenr_date = "";
    private EventDetailsListener eventDetailsListener;
    AlertDialog ad_dialog;
    String Current_day = "";
    String recurring_edit_choice;
    TextView tv_event_name, tv_event_description, tv_event_time, tv_event_repetetion, tv_event_date;
    Button btn_event_save;
    String Current_month = "";
    RecyclerView rv_displayEvents;

    ArrayList<Events_Do> events_list = new ArrayList<Events_Do>();
    List<EventDay> events = new ArrayList<>();
    Meetings meetings;
    ArrayList<Event_Details_DO> event_details_list = new ArrayList<Event_Details_DO>();
    View Meeting;

    public MonthlyCalendar(View Meeting) {
        this.Meeting = Meeting;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.month_view_calendar, container, false);
//        final ImageButton create_event = (ImageButton) v.findViewById(R.id.create_event);
        thiscontext = container.getContext();
//        create_event.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                callTimeZoneWebservice();
//            }
//        });

        calendarView = v.findViewById(R.id.prolificcalendarview);
        calendarView.setSelectionBackground(R.drawable.custom_selector);
        calendarView.setSwipeEnabled(true);
        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(@NonNull EventDay eventDay) {
                events_list.clear();
                java.util.Calendar myCalendar = eventDay.getCalendar();
                Updatelabel(myCalendar);
//                Calendar selectedDay = eventDay.getCalendar();
//                Calendar today = Calendar.getInstance();
//
//                List<EventDay> events = new ArrayList<>();
//
//                Drawable greenDrawable = new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.green_count_color));
//                Drawable blueDrawable = new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.Blue_text_color));
//
//                events.add(new EventDay(selectedDay, greenDrawable));
//                if (!isSameDay(selectedDay, today)) {
//                    events.add(new EventDay(today, blueDrawable));
//                }
//
//                calendarView.setEvents(events);

            }

        });
        calendarView.setForwardButtonImage(getResources().getDrawable(com.applandeo.materialcalendarview.R.drawable.ic_arrow_right));
        calendarView.setPreviousButtonImage(getResources().getDrawable(com.applandeo.materialcalendarview.R.drawable.ic_arrow_left));
        final java.util.Calendar mCalendar = Calendar.getInstance();

        calendarView.setOnPreviousPageChangeListener(() -> {
            events_list.clear();
            mCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH) - 1);
            String myFormat = "MMM dd, yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            filter = sdf.format(mCalendar.getTime());
            callEventListwebservice(filter);
        });
        calendarView.setOnForwardPageChangeListener(new OnCalendarPageChangeListener() {
            @Override
            public void onChange() {
                events_list.clear();
                mCalendar.set(mCalendar.MONTH, mCalendar.get(mCalendar.MONTH) + 1);
                String myFormat = "MMM dd, yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                filter = sdf.format(mCalendar.getTime());
                callEventListwebservice(filter);
            }
        });
        rv_displayEvents = (RecyclerView) v.findViewById(R.id.rv_events);
        callEventListwebservice(filter);
        meetings = (Meetings) getParentFragment();
//        Calendar today = Calendar.getInstance();
//        Drawable todayDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.today_highted_bg);
//        EventDay todayEvent = new EventDay(today, todayDrawable);

// If you already have other events:
//        events.add(todayEvent);
//        calendarView.setEvents(events);
        return v;
    }

    @Override
    public void onClick(View view) {

    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public interface EventDetailsListener {
        void onEventDetailsPassed(ArrayList<Event_Details_DO> event_details_list, String calendar_Type);
    }

    private void Updatelabel(Calendar myCalendar) {
        String myFormat = "MMM dd, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        filter = sdf.format(myCalendar.getTime());
        callEventListwebservice(filter);
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
            Currenr_date = AndroidUtils.getDateToString(event_date, "yyyy-MM-dd");
            Current_day = AndroidUtils.getDateToString(event_date, "dd");
            Current_month = AndroidUtils.getDateToString(event_date, "MM");
            Calendar calendar = new GregorianCalendar();
            TimeZone timeZone = calendar.getTimeZone();
            int offset = timeZone.getRawOffset();
            long hours = TimeUnit.MILLISECONDS.toMinutes(offset);
            long timezoneoffset = (-1) * (hours);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/events/" + timezoneoffset + "/" + "M" + event_creation_date, "Events_List", postData.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progress_dialog != null && progress_dialog.isShowing())
            AndroidUtils.dismiss_dialog(progress_dialog);
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
                    } else if (httpResult.getRequestType().equals("EVENT DETAILS")) {
                        if (!result.getBoolean("error")) {
                            event_details_list.clear();
//                            load_event_details(result.getJSONObject("event"), event_id);
                        }
                    } else if (httpResult.getRequestType().equals("EVENT_DELETE")) {
//                        if (!result.getBoolean("error")) {
                        AndroidUtils.showToast("Event Deleted Successfully", getContext());
//                            if (!(ad_dialog == null)) {
                        progress_dialog.dismiss();

//                            }
                        event_details_list.clear();
                        events.clear();
//                            rv_displayEvents.clea/
                        callEventListwebservice(filter);
//                        } else {
//                            AndroidUtils.showValidationALert("Alert", result.getString("msg"), getContext());
//                        }
                    }
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            }
            else if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Failed) {
                if (progress_dialog != null && progress_dialog.isShowing())
                    AndroidUtils.dismiss_dialog(progress_dialog);
                try {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
//                if (result.optBoolean("error")) {
                    AndroidUtils.showErrorAlert(result.optString("msg"), getActivity());
//                }
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            } else {
                if (progress_dialog != null && progress_dialog.isShowing())
                    AndroidUtils.dismiss_dialog(progress_dialog);
                AndroidUtils.showErrorAlert(httpResult.getResponseContent().toString(), getActivity());
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void loadEvents(JSONArray jsonArray) {
        //..
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Clear the sorted list
                    Events_Do events_do;
                    events_list.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        events_do = new Events_Do();
//                events_do.setDescription(jsonObject.getString("description"));
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
                        events_do.setRepeat_interval(jsonObject.optString("repeat_interval"));
                        events_do.setMeeting_link(jsonObject.getString("meeting_link"));
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
                        String from_ts = events_do.getEvent_start_time();
                        String to_ts = events_do.getEvent_end_time();
                        Date event_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss");
                        String event_start_time = AndroidUtils.getDateToString(event_date, "HH:mm a");
                        events_do.setConverted_Start_time(event_start_time);
                        Date event_date2 = AndroidUtils.stringToDateTimeDefault(to_ts, "yyyy-MM-dd'T'HH:mm:ss");
                        String event_end_time = AndroidUtils.getDateToString(event_date2, "HH:mm a");
                        events_do.setCOnverted_End_time(event_end_time);
                        String converted_from_ts = AndroidUtils.getDateToString(event_date, "yyyy-MM-dd");
                        String converted_to_ts = AndroidUtils.getDateToString(event_date2, "yyyy-MM-dd");
                        String converted_day = AndroidUtils.getDateToString(event_date, "dd");
                        int year = Integer.parseInt(AndroidUtils.getDateToString(event_date, "yyyy"));
//                System.out.println(events_do.getEvent_Name() + ";" + event_date.getDate() + "-" + event_date.getMonth() + "-" +year);
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, event_date.getMonth(), event_date.getDate());
                        events.add(new EventDay(calendar, DrawableUtils.getThreeDots(getContext())));
//                events.add(new EventDay(calendar,DrawableUtils.getDayCircle(getContext(), R.color.blue,R.color.green )));
//                Log.d("From Start Date", converted_from_ts);
//                Log.d("Current Date",Currenr_date);
                        if (converted_from_ts.toString().contains(Currenr_date) || converted_to_ts.toString().contains(Currenr_date)) {
//                    events_do.setRecurring(jsonObject.getBoolean("isrecurring"));
//                    if (events_do.isRecurring()) {
//                        events_list.add(events_do);
//                    } else {
                            events_list.add(events_do);

//                    }
                        }
                    }
                    // Load RecyclerView on the UI thread
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Collections.sort(events_list, new Comparator<Events_Do>() {
                                    @Override
                                    public int compare(Events_Do eventDay, Events_Do t1) {
                                        if (eventDay.getConverted_Start_time() == null || t1.getConverted_Start_time() == null)
                                            return 0;
                                        return eventDay.getConverted_Start_time().compareTo(t1.getConverted_Start_time());
                                    }
                                });
                                calendarView.setEvents(events);
                                loadRecyclerView();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

                } catch (final Exception e) {
                    // Handle any exceptions
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
        //..
    }

    private void runOnUiThread(Runnable loadPageException) {
    }

    private void loadRecyclerView() throws Exception {
        try {
            for (Events_Do event : events_list) {
                Log.d("Event List", event.getEvent_Name());
            }
            events_adapter = new Events_Adapter(events_list, this, getContext(), getActivity());
            rv_displayEvents.setAdapter(events_adapter);
            AndroidUtils.LoadingRecyclerview(rv_displayEvents, getContext());
//            rv_displayEvents.setLayoutAnimation(
//                    AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fall_down)
//            );
//            rv_displayEvents.scheduleLayoutAnimation();
        } catch (Exception e) {
            AndroidUtils.showToast(e.getMessage(), getContext());
            e.fillInStackTrace();
        }
    }

    @Override
    public void onEvent(ArrayList<Event_Details_DO> event_details_list) {

        if (eventDetailsListener != null) {
            String Calendar_Type = "Monthly";
            Log.d("EventsList", event_details_list.toString());
            eventDetailsListener.onEventDetailsPassed(event_details_list, Calendar_Type);
        }
    }

    @Override
    public void delete_events(Events_Do events_do) {
//        if (recur) {
//            delete_recurring_event(event_id);
//        } else {
        delete_event(events_do);
//            callDeleteEventwebservice(event_id, recurring_edit_choice);
//        }

    }

    @Override
    public void load_events() {
        callEventListwebservice(filter);
    }

    private void delete_event(Events_Do events_do) {
        AndroidUtils.Delete_Popup(getActivity(), "Are you sure do you want to delete " + events_do.getEvent_Name() + " ?", "Delete_Event", events_do.getEvent_id(), null, null, Meeting, events_do.isRecurring(),events_do.isIs_linked_with_timesheet());
    }

    public void delete_recurring_event(String event_id) {
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
                        callDeleteEventwebservice(event_id, recurring_edit_choice);
                    } else {
                        AndroidUtils.showAlert("Please choose one of the Delete recurring event", getActivity());
                    }
                }
            });
            dialog.setView(dialogLayout);
            dialog.show();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    public void callDeleteEventwebservice(String id, String recurring_choice) {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONObject postData = new JSONObject();
        try {
            if (!(recurring_choice == null)) {
                postData.put("choice", recurring_choice);
            }
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.DELETE, "v3/event/" + id, "EVENT_DELETE", postData.toString());
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    @Override
    public void delete(String event_id, boolean recur) {

    }

}
