package com.digicoffer.lauditor.Meetings.MonthCalander;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.digicoffer.lauditor.Appointments.Models.AppointmentModel;
import com.digicoffer.lauditor.Appointments.Models.PaymentModel;
import com.digicoffer.lauditor.Meetings.ViewModels.Events_Adapter;
import com.digicoffer.lauditor.Meetings.ViewModels.Meetings;
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO;
import com.digicoffer.lauditor.Meetings.Models.Events_Do;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class CalendarMonthView extends Fragment implements AsyncTaskCompleteListener, View.OnClickListener, Events_Adapter.EventListener, CalendarDayAdapter.OnDateSelectedListener {

    private ViewPager2 viewPager;
    private RecyclerView rv_monthView;
    private LocalDate currentDate = LocalDate.now();
    private LocalDate currentMonth = LocalDate.now();
    private String Currenr_date = "";
    TextView tv_month;
    private Meetings.FilterType currentFilter = Meetings.FilterType.ALL;
    ArrayList<AppointmentModel> appointments_list = new ArrayList<>();
    ArrayList<AppointmentModel> selected_date_appointments = new ArrayList<>();
    private EventDetailsListener eventDetailsListener;
    String recurring_edit_choice = "this";
    private String event_creation_date = "";
    private String filter = "";
    private MonthPagerAdapter adapter;
    private Dialog progress_dialog;
    private final ArrayList<Events_Do> selected_date_events = new ArrayList<>();
    private final ArrayList<Events_Do> events_list = new ArrayList<>();
    private boolean isViewPagerInitialized = false;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());
    View meetings;

    // ─── Empty state views ────────────────────────────────────────────────────
    private LinearLayout layout_empty_state;
    private TextView tv_empty_subtitle;


    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    public CalendarMonthView(View meetings, Meetings.FilterType currentFilter) {
        this.currentFilter = currentFilter;
        this.meetings = meetings;
    }


    // ─────────────────────────────────────────────────────────────────────────
    // onCreateView
    // ─────────────────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.custom_month_calander, container, false);

        viewPager = view.findViewById(R.id.viewPager);
        rv_monthView = view.findViewById(R.id.rv_monthView);
        ImageButton previous_img = view.findViewById(R.id.iv_previous_week);
        ImageButton forward_img = view.findViewById(R.id.iv_forward_week);
        tv_month = view.findViewById(R.id.tv_month);

        // Bind empty state views
        layout_empty_state = view.findViewById(R.id.layout_empty_state);
        tv_empty_subtitle = view.findViewById(R.id.tv_empty_subtitle);

        tv_month.setTypeface(Typeface.DEFAULT_BOLD);
        tv_month.setGravity(Gravity.CENTER);
        tv_month.setText(currentMonth.format(formatter));

        previous_img.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem > 0) {
                viewPager.setCurrentItem(currentItem - 1, true);
            }
        });

        forward_img.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            viewPager.setCurrentItem(currentItem + 1, true);
        });

        final Calendar mCalendar = Calendar.getInstance();
        events_list.clear();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        filter = sdf.format(mCalendar.getTime());
        adjustViewPagerHeight(currentMonth);

        // Hide empty state before initial load
        hideEmptyState();

        callEventListwebservice(filter);
        return view;
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Empty state helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Toggles the events RecyclerView and empty-state visibility.
     *
     * @param isEmpty true  → no events/appointments → hide list, show empty state
     *                false → data exists            → show list, hide empty state
     */
    private void updateEmptyState(boolean isEmpty) {
        if (layout_empty_state == null || rv_monthView == null) return;

        if (isEmpty) {
            rv_monthView.setVisibility(View.GONE);
            layout_empty_state.setVisibility(View.VISIBLE);
        } else {
            rv_monthView.setVisibility(View.VISIBLE);
            layout_empty_state.setVisibility(View.GONE);
        }
    }

    /**
     * Hides the empty state and restores the RecyclerView.
     * Call before any new data load so there is no stale flash.
     */
    private void hideEmptyState() {
        if (layout_empty_state != null) layout_empty_state.setVisibility(View.GONE);
        if (rv_monthView != null) rv_monthView.setVisibility(View.VISIBLE);
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Filter helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void filterDataByType() {
        selected_date_events.clear();
        selected_date_appointments.clear();

        if (currentFilter == Meetings.FilterType.ALL || currentFilter == Meetings.FilterType.MY_MEETINGS) {
            for (Events_Do event : events_list) {
                if (isSameSelectedDate(event.getEvent_start_time())) {
                    selected_date_events.add(event);
                }
            }
        }

        if (currentFilter == Meetings.FilterType.ALL || currentFilter == Meetings.FilterType.APPOINTMENTS) {
            for (AppointmentModel appointment : appointments_list) {
                if (isSameSelectedDate(appointment.getAppointment_from())) {
                    selected_date_appointments.add(appointment);
                }
            }
        }

        sortLists();
    }

    private boolean isSameSelectedDate(String timestamp) {
        try {
            Date date = AndroidUtils.stringToDateTimeDefault(timestamp, "yyyy-MM-dd'T'HH:mm:ss");
            String d = AndroidUtils.getDateToString(date, "yyyy-MM-dd");
            return d.equals(Currenr_date);
        } catch (Exception e) {
            return false;
        }
    }

    private void sortLists() {
        Collections.sort(selected_date_events,
                Comparator.comparing(
                        Events_Do::getConverted_Start_time,
                        Comparator.nullsLast(String::compareTo)
                )
        );

        Collections.sort(selected_date_appointments,
                Comparator.comparing(
                        AppointmentModel::getAppointment_from,
                        Comparator.nullsLast(String::compareTo)
                )
        );
    }


    // ─────────────────────────────────────────────────────────────────────────
    // ViewPager height
    // ─────────────────────────────────────────────────────────────────────────

    private int getMonthRowCount(LocalDate monthDate) {
        LocalDate firstDay = monthDate.withDayOfMonth(1);
        int firstDayOfWeek = firstDay.getDayOfWeek().getValue(); // 1(Mon)–7(Sun)
        int daysInMonth = monthDate.lengthOfMonth();
        int offset = (firstDayOfWeek == 7) ? 0 : firstDayOfWeek;
        int totalCells = offset + daysInMonth;
        return (int) Math.ceil(totalCells / 7.0);
    }

    private void adjustViewPagerHeight(LocalDate monthDate) {
        int rowCount = getMonthRowCount(monthDate);
        int cellHeight = (int) getResources().getDimension(R.dimen.calendar_day_height);
        int newHeight = cellHeight * rowCount;
        ViewGroup.LayoutParams params = viewPager.getLayoutParams();
        params.height = newHeight;
        viewPager.setLayoutParams(params);
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Data loading
    // ─────────────────────────────────────────────────────────────────────────

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
                        String converted_from_ts = AndroidUtils.getDateToString(appointment_date, "yyyy-MM-dd");

                        String to_ts = appointmentModel.getAppointment_to();
                        Date appointment_end_date = AndroidUtils.stringToDateTimeDefault(to_ts, "yyyy-MM-dd'T'HH:mm:ss");
                        String converted_to_ts = AndroidUtils.getDateToString(appointment_end_date, "yyyy-MM-dd");

                        if (converted_from_ts.contains(Currenr_date) || converted_to_ts.contains(Currenr_date)) {
                            selected_date_appointments.add(appointmentModel);
                        }

                        appointments_list.add(appointmentModel);
                    }

                } catch (final Exception e) {
                    e.fillInStackTrace();
                }
            }
        }).start();
    }

    private void setupViewPager() {
        adapter = new MonthPagerAdapter(this, currentDate, events_list, appointments_list, currentFilter, this);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(MonthPagerAdapter.START_POSITION, false);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                LocalDate selectedMonth = currentDate.plusMonths(position - MonthPagerAdapter.START_POSITION);
                currentMonth = selectedMonth;
                adjustViewPagerHeight(selectedMonth);

                String formattedDate = AndroidUtils.getDateToString(
                        AndroidUtils.localDateToDate(selectedMonth),
                        "MMM dd, yyyy"
                );
                filter = formattedDate;

                // Hide empty state before loading new month data
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> hideEmptyState());
                }

                callEventListwebservice(formattedDate);
            }
        });
    }

    private void loadRecyclerView() {
        boolean hasEvents = !selected_date_events.isEmpty();
        boolean hasAppointments = !selected_date_appointments.isEmpty();
        boolean isEmpty = !hasEvents && !hasAppointments;

        Events_Adapter eventsAdapter = new Events_Adapter(
                selected_date_events,
                selected_date_appointments,
                this,
                getContext(),
                getActivity()
        );

        rv_monthView.setAdapter(eventsAdapter);
        AndroidUtils.LoadingRecyclerview(rv_monthView, getContext());
        AndroidUtils.setupBottomSpacerFooter(rv_monthView, getResources().getDimensionPixelSize(R.dimen.twentyeight_dp));

        updateEmptyState(isEmpty);
    }

    public void applyFilter(Meetings.FilterType filterType) {
        this.currentFilter = filterType;

        // Update the pager adapter's filter
        if (adapter != null) {
            adapter.updateFilter(currentFilter);
        }

        // Hide stale empty state before re-filtering
        hideEmptyState();

        // Re-filter the current selected date's data
        filterDataByType();

        // Reload RecyclerView (will call updateEmptyState internally)
        loadRecyclerView();
    }

    private void loadEvents(JSONArray jsonArray) {
        new Thread(() -> {
            try {
                events_list.clear();
                selected_date_events.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Events_Do events_do = new Events_Do();
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

                    Date event_date = AndroidUtils.stringToDateTimeDefault(
                            events_do.getEvent_start_time(), "yyyy-MM-dd'T'HH:mm:ss");
                    Date event_end_date = AndroidUtils.stringToDateTimeDefault(
                            events_do.getEvent_end_time(), "yyyy-MM-dd'T'HH:mm:ss");

                    events_do.setConverted_Start_time(
                            AndroidUtils.getDateToString(event_date, "HH:mm a"));
                    events_do.setCOnverted_End_time(
                            AndroidUtils.getDateToString(event_end_date, "HH:mm a"));
                    events_do.setConverted_date(
                            AndroidUtils.getDateToString(event_date, "yyyy-MM-dd"));

                    String converted_from_ts = AndroidUtils.getDateToString(event_date, "dd-MM-yyyy");
                    String converted_to_ts = AndroidUtils.getDateToString(event_end_date, "dd-MM-yyyy");

                    if (converted_from_ts.contains(Currenr_date) || converted_to_ts.contains(Currenr_date)) {
                        selected_date_events.add(events_do);
                    }

                    events_list.add(events_do);
                }

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    try {
                        selected_date_events.sort(
                                Comparator.comparing(
                                        Events_Do::getConverted_Start_time,
                                        Comparator.nullsLast(String::compareTo)
                                )
                        );

                        events_list.sort(
                                Comparator.comparing(
                                        Events_Do::getConverted_Start_time,
                                        Comparator.nullsLast(String::compareTo)
                                )
                        );

                        // Load RecyclerView — will call updateEmptyState internally
                        loadRecyclerView();

                        if (!isViewPagerInitialized) {
                            setupViewPager();
                            isViewPagerInitialized = true;
                        } else if (adapter != null) {
                            adapter.updateEventsList(events_list);
                        }

                        tv_month.setText(currentMonth.format(formatter));

                    } catch (Exception e) {
                        e.fillInStackTrace();
                    }
                });

            } catch (Exception e) {
                e.fillInStackTrace();
            }
        }).start();
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Webservice calls
    // ─────────────────────────────────────────────────────────────────────────

    private void callEventListwebservice(String filter) {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONObject postData = new JSONObject();
        try {
            Date event_date = AndroidUtils.stringToDateTimeDefault(filter, "MMM dd, yyyy");
            event_creation_date = AndroidUtils.getDateToString(event_date, "MMyyyy");
            Currenr_date = AndroidUtils.getDateToString(event_date, "yyyy-MM-dd");

            Calendar calendar = new GregorianCalendar();
            TimeZone timeZone = calendar.getTimeZone();
            int offset = timeZone.getRawOffset();
            long hours = TimeUnit.MILLISECONDS.toMinutes(offset);
            long timezoneoffset = (-1) * hours;

            WebServiceHelper.callHttpWebService(
                    this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/events/" + timezoneoffset + "/" + "M" + event_creation_date,
                    "Events_List",
                    postData.toString()
            );
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void callAppointmentListWebService(String filter) {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONObject postData = new JSONObject();
        try {
            String[] monthDates = getMonthStartAndEndDates(filter);
            if (monthDates != null) {
                String startDate = monthDates[0];
                String endDate = monthDates[1];

                Date event_date = AndroidUtils.stringToDateTimeDefault(filter, "MMM dd, yyyy");
                event_creation_date = AndroidUtils.getDateToString(event_date, "MMyyyy");
                Currenr_date = AndroidUtils.getDateToString(event_date, "yyyy-MM-dd");

                Calendar calendar = new GregorianCalendar();
                TimeZone timeZone = calendar.getTimeZone();
                int offset = timeZone.getRawOffset();
                long hours = TimeUnit.MILLISECONDS.toMinutes(offset);
                long timezoneoffset = (-1) * hours;

                WebServiceHelper.callHttpWebService(
                        this, getContext(),
                        WebServiceHelper.RestMethodType.GET,
                        "v3/appointments?schedule_after=" + startDate + "&schedule_before=" + endDate,
                        "Appointments_List",
                        postData.toString()
                );

                Log.d("AppoinemtmentListCall",
                        "v3/appointments?schedule_after=" + startDate + "&schedule_before=" + endDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] getMonthStartAndEndDates(String filter) {
        try {
            Date filterDate = AndroidUtils.stringToDateTimeDefault(filter, "MMM dd, yyyy");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(filterDate);

            calendar.set(Calendar.DAY_OF_MONTH, 1);
            String startDate = AndroidUtils.getDateToString(calendar.getTime(), "yyyy-MM-dd");

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            String endDate = AndroidUtils.getDateToString(calendar.getTime(), "yyyy-MM-dd");

            return new String[]{startDate, endDate};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Webservice response
    // ─────────────────────────────────────────────────────────────────────────

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
                        callAppointmentListWebService(filter);

                    } else if (httpResult.getRequestType().equals("Appointments_List")) {
                        if (!result.getBoolean("error")) {
                            JSONArray jsonArray = result.getJSONArray("appointments");
                            loadAppointments(jsonArray);
                            if (adapter != null) {
                                adapter.updateBothLists(events_list, appointments_list);
                            }
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), getActivity());
                        }

                    } else if (httpResult.getRequestType().equals("EVENT_DELETE")) {
                        AndroidUtils.showAlert("Event Deleted Successfully", getActivity());
                        progress_dialog.dismiss();
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
                AndroidUtils.showErrorAlert(
                        httpResult.getResponseContent().toString(), getActivity());
            }

        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Delete event helpers
    // ─────────────────────────────────────────────────────────────────────────

    String event_delete_scope = "DELETE_EVENT_ONLY";

    public void callDeleteEventwebservice(String id, String recurring_choice, Boolean isevent_delete_scope) {
        if (isevent_delete_scope) {
            AndroidUtils.showConfirmationDialog(
                    getContext(), "Confirmation",
                    "This event has an associated timesheet entry. Do you want to update the timesheet too?",
                    requireContext().getString(R.string.delete_both),
                    requireContext().getString(R.string.delete_event_only),
                    new AndroidUtils.OnConfirmListener() {
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
            if (recurring_choice != null) {
                postData.put("choice", recurring_choice);
                postData.put("event_delete_scope", event_delete_scope);
            }
            WebServiceHelper.callHttpWebService(
                    this, getContext(),
                    WebServiceHelper.RestMethodType.DELETE,
                    "v3/event/" + id,
                    "EVENT_DELETE",
                    postData.toString()
            );
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    public void delete_recurring_event(String event_id, Boolean isevent_delete_scope) {
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

            delete_only_this.setOnClickListener(v -> {
                delete_only_this.setChecked(true);
                recurring_edit_choice = "this";
                delete_all.setChecked(false);
                delete_following.setChecked(false);
            });

            delete_following.setOnClickListener(v -> {
                delete_following.setChecked(true);
                recurring_edit_choice = "forward";
                delete_all.setChecked(false);
                delete_only_this.setChecked(false);
            });

            delete_all.setOnClickListener(v -> {
                delete_all.setChecked(true);
                recurring_edit_choice = "all";
                delete_only_this.setChecked(false);
                delete_following.setChecked(false);
            });

            final AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progress_dialog = dialog;

            btn_close_event.setOnClickListener(v -> progress_dialog.dismiss());

            delete.setOnClickListener(v -> {
                if (delete_only_this.isChecked() || delete_following.isChecked() || delete_all.isChecked()) {
                    progress_dialog.dismiss();
                    callDeleteEventwebservice(event_id, recurring_edit_choice, isevent_delete_scope);
                } else {
                    AndroidUtils.showAlert("Please choose one of the Delete recurring event", getActivity());
                }
            });

            dialog.setView(dialogLayout);
            dialog.show();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // EventDetailsListener interface
    // ─────────────────────────────────────────────────────────────────────────

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


    // ─────────────────────────────────────────────────────────────────────────
    // Events_Adapter.EventListener callbacks
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onEvent(ArrayList<Event_Details_DO> event_details_list) {
        if (eventDetailsListener != null) {
            Log.d("EventsList", event_details_list.toString());
            eventDetailsListener.onEventDetailsPassed(event_details_list, "Weekly");
        }
    }

    @Override
    public void delete_events(Events_Do events_do) {
        AndroidUtils.Delete_Popup(
                getActivity(),
                "Are you sure do you want to delete " + events_do.getEvent_Name() + " ?",
                "Delete_Event",
                events_do.getEvent_id(),
                null,
                this,
                meetings,
                events_do.isRecurring(),
                events_do.isIs_linked_with_timesheet()
        );
    }

    @Override
    public void load_events() {
        callEventListwebservice(filter);
    }

    @Override
    public void delete(String event_id, boolean recur) {
    }

    @Override
    public void onClick(View view) {
    }


    // ─────────────────────────────────────────────────────────────────────────
    // CalendarDayAdapter.OnDateSelectedListener callback
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onDateSelected(
            LocalDate selectedDate,
            ArrayList<Events_Do> filteredEvents,
            ArrayList<AppointmentModel> filteredAppointments
    ) {
        // Update to selected date
        Currenr_date = selectedDate.toString(); // yyyy-MM-dd

        // Update filtered lists with what was passed from the adapter
        selected_date_events.clear();
        selected_date_events.addAll(filteredEvents);

        selected_date_appointments.clear();
        selected_date_appointments.addAll(filteredAppointments);

        // Sort
        sortLists();

        // Reload RecyclerView — will call updateEmptyState internally
        loadRecyclerView();

        Log.d("DateSelected",
                "Date: " + Currenr_date
                        + ", Events: " + filteredEvents.size()
                        + ", Appointments: " + filteredAppointments.size());
    }
}