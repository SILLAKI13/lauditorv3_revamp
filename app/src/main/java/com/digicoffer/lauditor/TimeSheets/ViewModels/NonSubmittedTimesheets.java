package com.digicoffer.lauditor.TimeSheets.ViewModels;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.TimeSheets.Adapters.TimeSheetsAdapter;
import com.digicoffer.lauditor.TimeSheets.Adapters.WeeklyTSAdapter;
import com.digicoffer.lauditor.TimeSheets.Models.EventsModel;
import com.digicoffer.lauditor.TimeSheets.Models.StatusModel;
import com.digicoffer.lauditor.TimeSheets.Models.TSMatterModel;
import com.digicoffer.lauditor.TimeSheets.Models.TaskModel;
import com.digicoffer.lauditor.TimeSheets.Models.TasksModel;
import com.digicoffer.lauditor.TimeSheets.Models.TimeSheetModel;
import com.digicoffer.lauditor.TimeSheets.Models.WeekModel;
import com.digicoffer.lauditor.TimeSheets.Models.WeekTotalModel;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class NonSubmittedTimesheets extends Fragment implements AsyncTaskCompleteListener, View.OnClickListener, WeeklyTSAdapter.InterfaceListener {
    private Dialog progressDialog;
    private LinearLayout task_layout;
    private TimeSheets timeSheets;
    boolean hasTimesheets = false;
    boolean isEdit = false;
    String weekTotal = "";
    String timesheet_update_scope = "UPDATE_TIMESHEET_ONLY";
    Boolean isLinkedWithCalendar = false;
    boolean isfrozen = false;
    private ArrayList<TimeSheetModel> timeSheetsList = new ArrayList<>();
    private ArrayList<TSMatterModel> matterList = new ArrayList<>();
    private ArrayList<TSMatterModel> activeProjectsList = new ArrayList<>();
    private ArrayList<TasksModel> tasksList = new ArrayList<>();
    private ArrayList<EventsModel> eventsList = new ArrayList<>();
    private ArrayList<StatusModel> statusList = new ArrayList<>();
    private ArrayList<WeekTotalModel> weektotalList = new ArrayList<>();
    private ArrayList<WeekModel> weeksList = new ArrayList<>();
    private String selected_matter = "";
    private String selected_matter_id = "";
    private String selected_matter_type = "";
    String current_date = "";
    String matter_name;
    private boolean isMatterTypeExists = false;
    private String selected_task = "";
    private String selected_status = "";
    private boolean date_status = false;
    private View view;
    private String selected_date = "";
    private ListView sp_project, sp_task, sp_status, sp_date;
    private TextInputEditText tv_hours;
    private RecyclerView rv_non_submitted_timesheets;
    private androidx.appcompat.widget.AppCompatButton btn_cancel_timesheet, btn_save_timesheet;
    androidx.appcompat.widget.AppCompatButton btn_submit_timesheet, bt_fifteen_minutes, bt_thirty_minutes, bt_forty_five_minutes;
    private String hoursString;
    private String minutesString;
    ImageView img_clear_icon1, img_dropdown_icon1, img_clear_icon2, img_dropdown_icon2, img_clear_icon3, img_dropdown_icon3, img_clear_icon4, img_dropdown_icon4;
    private TextView project_id, tv_sp_project, task_id, tv_sp_task, status_id, tv_sp_status, date_id, tv_sp_date, hours_id, minutes_id, tot_hours_id, tv_total_hours;
    String date = "";
    NestedScrollView scrollView;
    LinearLayout ll_minutes, project_view, task_view, status_view, date_view;
    String event_name = "", status = "", task_name = "", chosen_date = "", hours = "", minutes = "", event_task_id, event_id;
    private boolean issubmitted = false;
    ArrayList<TaskModel> timesheet_eventsModels;
    private boolean ischecked_project = true, ischecked_status = true, ischecked_task = true, ischecked_date = true;

    // Empty state views
    private CommonSpinnerAdapter projectSpinnerAdapter;
    private CommonSpinnerAdapter taskSpinnerAdapter;
    private CommonSpinnerAdapter statusSpinnerAdapter;
    private CommonSpinnerAdapter dateSpinnerAdapter;
    LinearLayout layout_empty_state;
    TextView tv_empty_subtitle;

    public NonSubmittedTimesheets(ArrayList<TaskModel> eventsModels) {
        this.timesheet_eventsModels = eventsModels;
    }

    @SuppressLint("WrongViewCast")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.not_submitted_timesheet, container, false);
        sp_project = view.findViewById(R.id.sp_project);
        scrollView = view.findViewById(R.id.scrollView);
        ll_minutes = view.findViewById(R.id.ll_minutes);
        sp_project.setVisibility(View.GONE);
        timeSheets = (TimeSheets) getParentFragment();
        sp_task = view.findViewById(R.id.sp_task);
        sp_task.setVisibility(View.GONE);
        sp_status = view.findViewById(R.id.sp_status);
        sp_status.setVisibility(View.GONE);
        sp_date = view.findViewById(R.id.sp_date);
        sp_date.setVisibility(View.GONE);
        rv_non_submitted_timesheets = view.findViewById(R.id.rv_non_submitted_timesheets);
        tv_hours = view.findViewById(R.id.tv_hours);
        tv_hours.setInputType(InputType.TYPE_CLASS_NUMBER);
        tv_hours.setHint(R.string.hours);
        tv_total_hours = view.findViewById(R.id.tv_total_hours);
        tv_total_hours.setHint(R.string.total__);
        btn_cancel_timesheet = view.findViewById(R.id.btn_cancel_timesheet);
        btn_submit_timesheet = view.findViewById(R.id.btn_submit_timesheet);
        btn_submit_timesheet.setText(R.string.submit);
        btn_submit_timesheet.setVisibility(View.GONE);
        project_id = view.findViewById(R.id.project_id);
        project_id.setText(R.string.project);

        // Bind empty state views
        layout_empty_state = view.findViewById(R.id.layout_empty_state);
        tv_empty_subtitle = view.findViewById(R.id.tv_empty_subtitle);

        project_view = view.findViewById(R.id.sp_project_layout);
        tv_sp_project = project_view.findViewById(R.id.tv_spinner_view);
        img_clear_icon1 = project_view.findViewById(R.id.img_clear_icon);
        img_dropdown_icon1 = project_view.findViewById(R.id.img_dropdown_icon);
        task_view = view.findViewById(R.id.sp_task_layout);
        tv_sp_task = task_view.findViewById(R.id.tv_spinner_view);
        img_clear_icon2 = task_view.findViewById(R.id.img_clear_icon);
        img_dropdown_icon2 = task_view.findViewById(R.id.img_dropdown_icon);
        status_view = view.findViewById(R.id.sp_status_layout);
        tv_sp_status = status_view.findViewById(R.id.tv_spinner_view);
        img_clear_icon3 = status_view.findViewById(R.id.img_clear_icon);
        img_dropdown_icon3 = status_view.findViewById(R.id.img_dropdown_icon);
        date_view = view.findViewById(R.id.sp_date_layout);
        tv_sp_date = date_view.findViewById(R.id.tv_spinner_view);
        img_clear_icon4 = date_view.findViewById(R.id.img_clear_icon);
        img_dropdown_icon4 = date_view.findViewById(R.id.img_dropdown_icon);
        task_id = view.findViewById(R.id.task_id);
        task_id.setText(R.string.task);
        task_layout = view.findViewById(R.id.task_layout);
        task_layout.setVisibility(View.GONE);
        status_id = view.findViewById(R.id.status_id);
        status_id.setText(R.string.status);
        date_id = view.findViewById(R.id.date_id);
        date_id.setText(R.string.date);
        hours_id = view.findViewById(R.id.hours_id);
        hours_id.setText(R.string.hours);
        minutes_id = view.findViewById(R.id.minutes_id);
        minutes_id.setText(R.string.minutes);
        tot_hours_id = view.findViewById(R.id.tot_hours_id);
        tot_hours_id.setText(R.string.total_hours);
        btn_save_timesheet = view.findViewById(R.id.btn_save_timesheet);
        Bundle bundle = getArguments();
        assert bundle != null;
        date = bundle.getString("date");
        ArrayList<String> weekDates = bundle.getStringArrayList("weekDates");
        assert weekDates != null;
        current_date = weekDates.get(0);
        bt_thirty_minutes = view.findViewById(R.id.bt_thirty_minutes);
        bt_forty_five_minutes = view.findViewById(R.id.bt_forty_five_minutes);
        bt_fifteen_minutes = view.findViewById(R.id.bt_fifteen_minutes);
        bt_fifteen_minutes.setOnClickListener(minutesButtonClickListener);
        bt_thirty_minutes.setOnClickListener(minutesButtonClickListener);
        bt_forty_five_minutes.setOnClickListener(minutesButtonClickListener);

        project_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!activeProjectsList.isEmpty()) {
                    boolean isVisible = sp_project.getVisibility() == View.VISIBLE;
                    AndroidUtils.DisplaySpinnerView(sp_project, tv_sp_project,
                            tv_sp_project.getText().toString(),
                            img_dropdown_icon1, img_clear_icon1,
                            !isVisible, projectSpinnerAdapter, "Search Project");
                }
            }
        });
        task_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tasksList.isEmpty()) {
                    boolean isVisible = sp_task.getVisibility() == View.VISIBLE;
                    AndroidUtils.DisplaySpinnerView(sp_task, tv_sp_task,
                            tv_sp_task.getText().toString(),
                            img_dropdown_icon2, img_clear_icon2,
                            !isVisible, taskSpinnerAdapter, "Search Task");
                }
            }
        });
        btn_cancel_timesheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear_date();
            }
        });
        status_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!statusList.isEmpty()) {
                    boolean isVisible = sp_status.getVisibility() == View.VISIBLE;
                    AndroidUtils.DisplaySpinnerView(sp_status, tv_sp_status,
                            tv_sp_status.getText().toString(),
                            img_dropdown_icon3, img_clear_icon3,
                            !isVisible, statusSpinnerAdapter, "Search Status");
                }
            }
        });
        date_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isVisible = sp_date.getVisibility() == View.VISIBLE;
                AndroidUtils.DisplaySpinnerView(sp_date, tv_sp_date,
                        tv_sp_date.getText().toString(),
                        img_dropdown_icon4, img_clear_icon4,
                        !isVisible, dateSpinnerAdapter, "Search Date");
            }
        });
        btn_save_timesheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Constants.is_active) {
                    AndroidUtils.showRenewalPopup(getActivity());
                } else {
                    String msg = "Please enter the";
                    if ((Objects.requireNonNull(tv_sp_project.getText()).toString().trim().isEmpty()) || (Objects.requireNonNull(tv_sp_task.getText()).toString().isEmpty()) || (Objects.requireNonNull(tv_sp_status.getText()).toString().isEmpty()) || (Objects.requireNonNull(tv_total_hours.getText()).toString().trim().isEmpty()) || (tv_total_hours.getText().toString().equals("00:00")) || (Objects.requireNonNull(tv_sp_date.getText()).toString().trim().isEmpty())) {
                        if ((Objects.requireNonNull(tv_sp_project.getText()).toString().isEmpty())) {
                            msg = msg + " Projects";
                        }
                        if (Objects.requireNonNull(tv_sp_task.getText()).toString().isEmpty()) {
                            if (msg.equals("Please enter the")) {
                                msg = msg + " Task";
                            } else {
                                msg = msg + ", Task";
                            }
                        }
                        if (Objects.requireNonNull(tv_sp_status.getText()).toString().isEmpty()) {
                            if (msg.equals("Please enter the")) {
                                msg = msg + " Status";
                            } else {
                                msg = msg + ", Status";
                            }
                        }
                        if (Objects.requireNonNull(tv_sp_date.getText()).toString().isEmpty()) {
                            if (msg.equals("Please enter the")) {
                                msg = msg + " Date";
                            } else {
                                msg = msg + ", Date";
                            }
                        }
                        if (Objects.requireNonNull(tv_total_hours.getText()).toString().trim().isEmpty() || (tv_total_hours.getText().toString().equals("00:00"))) {
                            if (msg.equals("Please enter the")) {
                                msg = msg + " Hours";
                            } else {
                                msg = msg + ", Hours";
                            }
                        }
                        AndroidUtils.showAlert(msg, timeSheets.getActivity());
                    } else {
                        try {
                            if (isEdit) {
                                if (isLinkedWithCalendar) {
                                    AndroidUtils.showConfirmationDialog(getContext(), "Confirmation", "This timesheet has an associated event entry. " +
                                                    "Do you want to update the event too?", requireContext().getString(R.string.update_both), requireContext().getString(R.string.update_timesheet_only), new AndroidUtils.OnConfirmListener() {
                                                @Override
                                                public void onSave() {
                                                    timesheet_update_scope = "UPDATE_BOTH";
                                                    callEditTimesheetwebservice();
                                                }

                                                @Override
                                                public void onCancel() {
                                                    timesheet_update_scope = "UPDATE_TIMESHEET_ONLY";
                                                    callEditTimesheetwebservice();
                                                }
                                            }
                                    );
                                } else {
                                    timesheet_update_scope = "UPDATE_TIMESHEET_ONLY";
                                    callEditTimesheetwebservice();
                                }
                            } else {
                                callSaveTimeSheetWebservice();
                            }
                        } catch (Exception e) {
                            e.fillInStackTrace();
                        }
                    }
                }
            }
        });
        tv_hours.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String hoursText = s.toString();
                if (!hoursText.isEmpty()) {
                    int hours = Integer.parseInt(hoursText);
                    if (hours > 24) {
                        tv_hours.removeTextChangedListener(this);
                        tv_hours.setSelection(tv_hours.getText().length());
                        tv_hours.addTextChangedListener(this);
                        tv_hours.setText("24");
                    } else if (hours == 24) {
                        minutesString = "00";
                        deselectAllMinuteButtons();
                        updateTotalTime();
                    }
                }
                updateTotalTime();
            }
        });

        if (date.isEmpty()) {
            callCurrentDateTimeSheetsWebservice(date, date_status);
        } else if (date == null) {
            callCurrentDateTimeSheetsWebservice(date, date_status);
        } else {
            callTimeSheetsWebservice(date);
        }

        btn_submit_timesheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callSubmitTimeSheetWebService();
            }
        });

        dateSpinnerAdapter = new CommonSpinnerAdapter(getActivity(), weekDates);
        sp_date.setAdapter(dateSpinnerAdapter);
        AndroidUtils.LoadList(sp_date, getContext(), weekDates.size(), true);
        sp_date.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                selected_date = selectedItem;
                AndroidUtils.DisplaySpinnerView(sp_date, tv_sp_date, selected_date, img_dropdown_icon4, img_clear_icon4, false, dateSpinnerAdapter, "Search Date");
                ischecked_date = true;
            }
        });

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEE MMM d,yyyy", Locale.ENGLISH);
        ArrayList<String> formattedDates = new ArrayList<>();
        for (String dateStr : weekDates) {
            try {
                Date date1 = inputFormat.parse(dateStr);
                String formattedDate = outputFormat.format(date1);
                formattedDates.add(formattedDate);
            } catch (ParseException e) {
                e.fillInStackTrace();
            }
        }
        for (String value : formattedDates) {
            weeksList.add(new WeekModel(value));
        }

        img_clear_icon1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tv_sp_project.getText().toString().isEmpty()) {
                    AndroidUtils.DisplaySpinnerView(sp_project, tv_sp_project, selected_matter, img_dropdown_icon1, img_clear_icon1, false, projectSpinnerAdapter, "Search Project");
                    ischecked_project = true;
                    clearDetails();
                }
            }
        });
        img_clear_icon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tv_sp_task.getText().toString().isEmpty()) {
                    AndroidUtils.DisplaySpinnerView(sp_task, tv_sp_task, selected_task, img_dropdown_icon2, img_clear_icon2, false, taskSpinnerAdapter, "Search Task");
                    ischecked_task = true;
                }
            }
        });
        img_clear_icon3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tv_sp_status.getText().toString().isEmpty()) {
                    AndroidUtils.DisplaySpinnerView(sp_status, tv_sp_status, selected_status, img_dropdown_icon3, img_clear_icon3, false, statusSpinnerAdapter, "Search Status");
                    ischecked_status = true;
                }
            }
        });
        img_clear_icon4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.DisplaySpinnerView(sp_date, tv_sp_date, selected_date, img_dropdown_icon4, img_clear_icon4, false, dateSpinnerAdapter, "Search Date");
                ischecked_date = true;
            }
        });

        if (!Constants.is_active) {
            AndroidUtils.showRenewalPopup(getActivity());
        }
        return view;
    }

    private void updateEmptyState() {
        if (eventsList.isEmpty()) {
            layout_empty_state.setVisibility(View.VISIBLE);
            rv_non_submitted_timesheets.setVisibility(View.GONE);
            btn_submit_timesheet.setVisibility(View.GONE);
            tv_empty_subtitle.setText("Add tasks or track time to begin.");
        } else {
            layout_empty_state.setVisibility(View.GONE);
            rv_non_submitted_timesheets.setVisibility(View.VISIBLE);
        }
    }

    private void callDeleteTimesheetwebservice(String timesheet_id, String timesheet_delete_scope) {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            postdata.put("id", timesheet_id);
            postdata.put("timesheet_delete_scope", timesheet_delete_scope);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.DELETE, "v3/user/timesheets", "Delete TimeSheet", postdata.toString());
        } catch (Exception e) {
            if (progressDialog.isShowing() && progressDialog != null) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
        }
    }

    private void callSubmitTimeSheetWebService() {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            Date new_date = inputFormat.parse(date);
            assert new_date != null;
            String outputDate = outputFormat.format(new_date);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v3/user/timesheets/freeze/" + outputDate, "Submit TimeSheet", postdata.toString());
        } catch (Exception e) {
            if (progressDialog.isShowing() && progressDialog != null) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
        }
    }

    private void callEditTimesheetwebservice() {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            postdata.put("id", event_name);
            postdata.put("action", "hours");
            postdata.put("billing", selected_status);
            SimpleDateFormat inputformat = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat outputformat = new SimpleDateFormat("MMM d, yyyy");
            try {
                Date date = inputformat.parse(selected_date);
                assert date != null;
                String formattedDate = outputformat.format(date);
                postdata.put("date", formattedDate);
            } catch (Exception e) {
                e.fillInStackTrace();
            }
            postdata.put("duration_hours", hoursString);
            if (Objects.equals(minutesString, "0")) {
                postdata.put("duration_minutes", "00");
            } else {
                postdata.put("duration_minutes", minutesString);
            }
            postdata.put("matter_id", event_id);
            postdata.put("timesheet_update_scope", timesheet_update_scope);
            postdata.put("matter_type", matter_name);
            postdata.put("title", task_name);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "v3/user/timesheets", "Edit Timesheet", postdata.toString());
            Log.d("Edit_Timesheet", postdata.toString());
        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    private void callSaveTimeSheetWebservice() {
        try {
            JSONObject postdata = new JSONObject();
            postdata.put("action", "hours");
            postdata.put("billing", selected_status.toLowerCase());
            SimpleDateFormat inputformat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            SimpleDateFormat outputformat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
            try {
                Date date = inputformat.parse(selected_date);
                assert date != null;
                String formattedDate = outputformat.format(date);
                postdata.put("date", formattedDate);
            } catch (Exception e) {
                e.fillInStackTrace();
            }
            postdata.put("duration_hours", hoursString);
            if (Objects.equals(minutesString, "0")) {
                postdata.put("duration_minutes", "00");
            } else {
                postdata.put("duration_minutes", minutesString);
            }
            postdata.put("matter_id", selected_matter_id);
            if (isMatterTypeExists) {
                postdata.put("matter_type", selected_matter_type);
            } else {
                postdata.put("matter_type", selected_matter);
            }
            postdata.put("title", selected_task);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v3/user/timesheets", "SAVE TIMESHEETS", postdata.toString());
        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    View.OnClickListener minutesButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String hoursText = tv_hours.getText().toString();
            int hours = 0;
            if (!hoursText.isEmpty()) {
                hours = Integer.parseInt(hoursText);
            }
            hoursString = hoursText;
            if (hours >= 24) {
                return;
            }
            AppCompatButton button = (AppCompatButton) v;
            boolean isCurrentlySelected = button.isSelected();
            deselectAllMinuteButtons();
            if (!isCurrentlySelected) {
                button.setSelected(true);
            }
            updateTotalTime();
        }
    };

    private void deselectAllMinuteButtons() {
        bt_fifteen_minutes.setSelected(false);
        bt_thirty_minutes.setSelected(false);
        bt_forty_five_minutes.setSelected(false);
    }

    private void updateTotalTime() {
        String hoursText = Objects.requireNonNull(tv_hours.getText()).toString();
        int hours;
        if (!hoursText.isEmpty()) {
            hours = Integer.parseInt(hoursText);
        } else {
            hours = Integer.parseInt("00");
        }
        hoursString = hoursText;
        int minutes = 0;
        if (bt_fifteen_minutes.isSelected()) {
            minutes = 15;
        } else if (bt_thirty_minutes.isSelected()) {
            minutes = 30;
        } else if (bt_forty_five_minutes.isSelected()) {
            minutes = 45;
        }
        minutesString = String.valueOf(minutes);
        selected_minutes(minutesString);
        String totalTimeText = String.format("%02d:%02d", hours, minutes);
        tv_total_hours.setText(totalTimeText);
    }

    private void clearSelectedMinutes() {
        bt_fifteen_minutes.setBackgroundDrawable(getContext().getDrawable(R.drawable.rectangle_light_grey));
        bt_thirty_minutes.setBackgroundDrawable(getContext().getDrawable(R.drawable.rectangle_light_grey));
        bt_forty_five_minutes.setBackgroundDrawable(getContext().getDrawable(R.drawable.rectangle_light_grey));
        bt_fifteen_minutes.setTextColor(Color.BLACK);
        bt_thirty_minutes.setTextColor(Color.BLACK);
        bt_forty_five_minutes.setTextColor(Color.BLACK);
    }

    private void selected_minutes(String minutes) {
        switch (minutes) {
            case "15":
                clearSelectedMinutes();
                bt_fifteen_minutes.setBackgroundDrawable(getContext().getDrawable(R.drawable.rectangular_button_green_count));
                bt_fifteen_minutes.setTextColor(Color.WHITE);
                break;
            case "30":
                clearSelectedMinutes();
                bt_thirty_minutes.setTextColor(Color.WHITE);
                bt_thirty_minutes.setBackgroundDrawable(getContext().getDrawable(R.drawable.rectangular_button_green_count));
                break;
            case "45":
                clearSelectedMinutes();
                bt_forty_five_minutes.setTextColor(Color.WHITE);
                bt_forty_five_minutes.setBackgroundDrawable(getContext().getDrawable(R.drawable.rectangular_button_green_count));
                break;
            default:
                clearSelectedMinutes();
                break;
        }
    }

    private void callCurrentDateTimeSheetsWebservice(String date, boolean date_status) {
        try {
            clearList();
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject data = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/user/timesheets", "TimeSheets", data.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void clear_date() {
        btn_save_timesheet.setText(R.string.save);
        isEdit = false;
        task_layout.setVisibility(View.GONE);
        tv_sp_project.setText("");
        tv_sp_task.setText("");
        tv_sp_status.setText("");
        tv_sp_date.setText("");
        tv_hours.setText("");
        tv_total_hours.setText("");
        clear_text(false);
    }

    private void clearList() {
        timeSheetsList.clear();
        weektotalList.clear();
        eventsList.clear();
        matterList.clear();
        activeProjectsList.clear();
        statusList.clear();
        tasksList.clear();
    }

    private void disableAllViews(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                disableAllViews(child);
            }
        } else {
            view.setEnabled(false);
        }
    }

    private void OpenPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void callTimeSheetsWebservice(String date) {
        try {
            clearList();
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject data = new JSONObject();
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            Date new_date = inputFormat.parse(date);
            String outputDate = outputFormat.format(new_date);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/user/timesheets/" + outputDate + "?submitted=false", "TimeSheets", data.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            view.setEnabled(enabled);
            if (view instanceof ViewGroup) {
                enableDisableViewGroup((ViewGroup) view, enabled);
            }
        }
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progressDialog != null && progressDialog.isShowing()) {
            AndroidUtils.dismiss_dialog(progressDialog);
        }
        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                if (httpResult.getRequestType().equals("TimeSheets")) {
                    JSONObject dates = result.getJSONObject("dates");
                    timeSheetsList.clear();
                    loadTimesheetData(dates, result);
                } else if (httpResult.getRequestType().equals("Tasks")) {
                    JSONArray tasks = result.getJSONArray("tasks");
                    tasksList.clear();
                    loadTasks(tasks);
                } else if (httpResult.getRequestType().equals("SAVE TIMESHEETS")) {
                    deselectAllMinuteButtons();
                    AndroidUtils.showAlert(result.getString("msg"), getActivity(), "");
                    tv_hours.setText("");
                    tv_total_hours.setText("");
                    minutesString = "";
                    selected_minutes(minutesString);
                    hoursString = "";
                    if (date.isEmpty()) {
                        callCurrentDateTimeSheetsWebservice(date, date_status);
                    } else {
                        callTimeSheetsWebservice(date);
                    }
                    clear_date();
                } else if (httpResult.getRequestType().equals("Submit TimeSheet")) {
                    deselectAllMinuteButtons();
                    AndroidUtils.showAlert(result.getString("msg"), getActivity(), "");
                    if (date.isEmpty()) {
                        callCurrentDateTimeSheetsWebservice(date, date_status);
                    } else {
                        callTimeSheetsWebservice(date);
                    }
                } else if (httpResult.getRequestType().equals("Delete TimeSheet")) {
                    AndroidUtils.showAlert(result.getString("msg"), getActivity(), "");
                    if (date.isEmpty()) {
                        callCurrentDateTimeSheetsWebservice(date, date_status);
                    } else {
                        callTimeSheetsWebservice(date);
                    }
                    clear_date();
                } else if (httpResult.getRequestType().equals("Edit Timesheet")) {
                    AndroidUtils.showAlert(result.getString("msg"), getActivity(), "");
                    deselectAllMinuteButtons();
                    if (date.isEmpty()) {
                        callCurrentDateTimeSheetsWebservice(date, date_status);
                    } else {
                        callTimeSheetsWebservice(date);
                    }
                    clear_date();
                    AndroidUtils.dismiss_dialog(progressDialog);
                }
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        } else if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Failed) {
            if (progressDialog != null && progressDialog.isShowing())
                AndroidUtils.dismiss_dialog(progressDialog);
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                AndroidUtils.showErrorAlert(result.optString("msg"), getActivity());
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        } else {
            if (progressDialog != null && progressDialog.isShowing())
                AndroidUtils.dismiss_dialog(progressDialog);
            AndroidUtils.showErrorAlert(httpResult.getResponseContent().toString(), getActivity());
        }
    }

    private void loadTasks(JSONArray tasks) throws JSONException {
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject jsonObject = tasks.getJSONObject(i);
            TasksModel tasksModel = new TasksModel();
            tasksModel.setDisplayValue(jsonObject.getString("displayValue"));
            tasksModel.setReturnValue(jsonObject.getString("returnValue"));
            tasksList.add(tasksModel);
        }
        taskSpinnerAdapter = new CommonSpinnerAdapter(getActivity(), tasksList);
        sp_task.setAdapter(taskSpinnerAdapter);
        AndroidUtils.LoadList(sp_task, getContext(), tasksList.size(), true);
        sp_task.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TasksModel selectedItem = (TasksModel) parent.getItemAtPosition(position);
                selected_task = selectedItem.getDisplayValue();
                AndroidUtils.DisplaySpinnerView(sp_task, tv_sp_task, selected_task, img_dropdown_icon2, img_clear_icon2, false, taskSpinnerAdapter, "Search Task");
                ischecked_task = true;
            }
        });
    }

    private boolean containsMatterType(ArrayList<TSMatterModel> matterList, String matterTypeToCheck) {
        for (TSMatterModel matter : matterList) {
            if (matter.getMatter_type() != null && matter.getMatter_type().equals(matterTypeToCheck)) {
                return true;
            }
        }
        return false;
    }

    private void loadTimesheetData(JSONObject dates, JSONObject result) throws JSONException {
        timeSheetsList.clear();
        eventsList.clear();
        TimeSheetModel timeSheetModel = new TimeSheetModel();
        timeSheetModel.setFrozen(dates.getBoolean("isFrozen"));
        isfrozen = dates.getBoolean("isFrozen");
        timeSheetModel.setCurrentWeek(dates.getString("currentWeek"));
        timeSheetModel.setNextWeek(dates.getString("nextWeek"));
        timeSheetModel.setPrevWeek(dates.getString("prevWeek"));
        JSONObject timesheets = result.optJSONObject("timesheetList");
        assert timesheets != null;
        JSONArray matters = timesheets.optJSONArray("matters");
        JSONArray activeProjects = timesheets.optJSONArray("activeProjects");
        JSONObject weektotal = timesheets.getJSONObject("weekTotal");
        WeekTotalModel weekModel = new WeekTotalModel();
        weekModel.setMon(weektotal.getString("Mon"));
        weekModel.setTue(weektotal.getString("Tue"));
        weekModel.setWed(weektotal.getString("Wed"));
        weekModel.setThu(weektotal.getString("Thu"));
        weekModel.setFri(weektotal.getString("Fri"));
        weekModel.setSat(weektotal.getString("Sat"));
        weekModel.setSun(weektotal.getString("Sun"));
        String wTotal = weektotal.optString("wTotal");
        weekTotal = wTotal;
        weektotalList.add(weekModel);
        for (int i = 0; i < matters.length(); i++) {
            JSONObject jsonObject = matters.getJSONObject(i);
            TSMatterModel matterModel = new TSMatterModel();
            matterModel.setMattername(jsonObject.optString("matterName"));
            matterModel.setMatterid(jsonObject.optString("matterId"));
            matterModel.setIseditable(jsonObject.optBoolean("is_editable"));
            matterModel.setTasks(jsonObject.optJSONArray("tasks"));
            if (jsonObject.has("matterType")) {
                matterModel.setMatter_type(jsonObject.optString("matterType"));
            }
            matterList.add(matterModel);
        }
        for (int i = 0; i < activeProjects.length(); i++) {
            JSONObject jsonObject = activeProjects.getJSONObject(i);
            TSMatterModel matterModel = new TSMatterModel();
            matterModel.setMattername(jsonObject.optString("matterName"));
            matterModel.setMatterid(jsonObject.optString("matterId"));
            matterModel.setIseditable(jsonObject.optBoolean("is_editable"));
            matterModel.setTasks(jsonObject.optJSONArray("tasks"));
            if (jsonObject.has("matterType")) {
                matterModel.setMatter_type(jsonObject.optString("matterType"));
            }
            activeProjectsList.add(matterModel);
        }
        timeSheetsList.add(timeSheetModel);
        for (int j = 0; j < matterList.size(); j++) {
            for (int m = 0; m < matterList.get(j).getTasks().length(); m++) {
                JSONObject jsonObject = matterList.get(j).getTasks().getJSONObject(m);
                EventsModel eventsModel = new EventsModel();
                if (jsonObject.has("billing")) {
                    eventsModel.setBilling(jsonObject.getString("billing"));
                }
                eventsModel.setTaskName(jsonObject.getString("taskName"));
                eventsModel.setTotal(jsonObject.getString("total"));
                eventsModel.setMon(jsonObject.getJSONObject("Mon"));
                eventsModel.setTue(jsonObject.getJSONObject("Tue"));
                eventsModel.setWed(jsonObject.getJSONObject("Wed"));
                eventsModel.setThu(jsonObject.getJSONObject("Thu"));
                eventsModel.setFri(jsonObject.getJSONObject("Fri"));
                eventsModel.setSat(jsonObject.getJSONObject("Sat"));
                eventsModel.setSun(jsonObject.getJSONObject("Sun"));
                eventsModel.setMatter_id(matterList.get(j).getMatterid());
                eventsModel.setMatter_name(matterList.get(j).getMattername());
                eventsModel.setIs_editable(matterList.get(j).getIseditable());
                if (jsonObject.has("matterType")) {
                    eventsModel.setMatter_type(jsonObject.getString("matterType"));
                }
                eventsList.add(eventsModel);
            }
        }
        for (int i = 0; i < timeSheetsList.size(); i++) {
            if (timeSheetsList.get(i).isFrozen()) {
                view.setAlpha(0.8f);
                issubmitted = true;
                disableAllViews(view);
                AlreadySubmittedTimeSheets();
                btn_submit_timesheet.setVisibility(View.GONE);
                rv_non_submitted_timesheets.setVisibility(View.GONE);
                project_view.setEnabled(false);
                status_view.setEnabled(false);
                date_view.setEnabled(false);
                tv_hours.setClickable(false);
            } else {
                // Project adapter
                projectSpinnerAdapter = new CommonSpinnerAdapter(getActivity(), activeProjectsList);
                sp_project.setAdapter(projectSpinnerAdapter);
                AndroidUtils.LoadList(sp_project, getContext(), activeProjectsList.size(), true);
                sp_project.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TSMatterModel selectedItem = (TSMatterModel) parent.getItemAtPosition(position);
                        selected_matter = selectedItem.getMattername();
                        selected_matter_id = selectedItem.getMatterid();
                        try {
                            selected_matter_type = selectedItem.getMatter_type();
                            isMatterTypeExists = containsMatterType(activeProjectsList, selected_matter_type);
                        } catch (NullPointerException e) {
                            e.fillInStackTrace();
                        }
                        if (selected_matter_type != null) {
                            callTaskWebservice(selected_matter_type);
                        } else {
                            callTaskWebservice(selected_matter_id.toLowerCase(Locale.ROOT));
                        }
                        AndroidUtils.DisplaySpinnerView(sp_project, tv_sp_project, selected_matter, img_dropdown_icon1, img_clear_icon1, false, projectSpinnerAdapter, "Search Project");
                        ischecked_project = true;
                        unhideProject();
                    }
                });

// Status adapter
                statusList.clear();
                StatusModel statusModel = new StatusModel(getString(R.string.billable));
                StatusModel statusModel1 = new StatusModel(getString(R.string.non_billable));
                statusList.add(statusModel);
                statusList.add(statusModel1);
                statusSpinnerAdapter = new CommonSpinnerAdapter(getActivity(), statusList);
                sp_status.setAdapter(statusSpinnerAdapter);
                AndroidUtils.LoadList(sp_status, getContext(), statusList.size(), true);
                sp_status.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        StatusModel selectedItem = (StatusModel) parent.getItemAtPosition(position);
                        selected_status = selectedItem.getName();
                        status = selected_status;
                        AndroidUtils.DisplaySpinnerView(sp_status, tv_sp_status, selected_status, img_dropdown_icon3, img_clear_icon3, false, statusSpinnerAdapter, "Search Status");
                        ischecked_status = true;
                    }
                });
                if (!weekTotal.isEmpty() && (!weekTotal.equals("0:0"))) {
                    btn_submit_timesheet.setVisibility(View.VISIBLE);
                    rv_non_submitted_timesheets.setVisibility(View.VISIBLE);
                    hasTimesheets = true;
                    break;
                } else {
                    hasTimesheets = false;
                    rv_non_submitted_timesheets.setVisibility(View.GONE);
                    btn_submit_timesheet.setVisibility(View.GONE);
                }
                issubmitted = false;
            }
        }

        // Show empty state if eventsList is empty and timesheet is not frozen
        updateEmptyState();

        try {
            if (!issubmitted) {
                loadTimesheetsRecyclerview();
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void unhideProject() {
        AndroidUtils.DisplaySpinnerView(sp_project, tv_sp_project, selected_matter, img_dropdown_icon1, img_clear_icon1, false, projectSpinnerAdapter, "Search Project");
        ischecked_project = true;
        clearDetails();
        task_layout.setVisibility(View.VISIBLE);
    }

    private void clearDetails() {
        AndroidUtils.DisplaySpinnerView(sp_task, tv_sp_task, selected_task, img_dropdown_icon2, img_clear_icon2, false, taskSpinnerAdapter, "Search Task");
        ischecked_task = true;
        AndroidUtils.DisplaySpinnerView(sp_status, tv_sp_status, selected_status, img_dropdown_icon3, img_clear_icon3, false, statusSpinnerAdapter, "Search Status");
        ischecked_status = true;
        AndroidUtils.DisplaySpinnerView(sp_date, tv_sp_date, selected_date, img_dropdown_icon4, img_clear_icon4, false, dateSpinnerAdapter, "Search Date");
        ischecked_date = true;
        task_layout.setVisibility(View.GONE);
        hoursString = "";
        selected_status = "";
        status = "";
        selected_date = "";
        tv_hours.setText("");
        minutesString = "";
        selected_minutes(minutesString);
        tv_total_hours.setText("");
    }

    private void loadTimesheetsRecyclerview() {
        rv_non_submitted_timesheets.setLayoutManager(new GridLayoutManager(getContext(), 1));
        TimeSheetsAdapter timeSheetsAdapter = new TimeSheetsAdapter(weeksList, eventsList, weektotalList, getContext(), issubmitted, this, timeSheets.getActivity());
        rv_non_submitted_timesheets.setAdapter(timeSheetsAdapter);
        AndroidUtils.setupBottomSpacerFooter(rv_non_submitted_timesheets, getResources().getDimensionPixelSize(R.dimen.twentyeight_dp));
        if (timeSheetsAdapter.getItemCount() > 0) {
            int lastPosition = timeSheetsAdapter.getItemCount() - 1;
            rv_non_submitted_timesheets.smoothScrollToPosition(lastPosition);
        }
        timeSheetsAdapter.notifyDataSetChanged();
    }

    private void callTaskWebservice(String selected_matter_type) {
        progressDialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/event/tasks/" + selected_matter_type, "Tasks", jsonObject.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void AlreadySubmittedTimeSheets() {
        timeSheets.Frozen_view();
    }

    String timesheet_delete_scope = "DELETE_TIMESHEET_ONLY";

    @Override
    public void DeleteEvent_Timesheet(TaskModel taskModel) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.delete_relationship, null);
            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
            ImageView close_details = view.findViewById(R.id.close_documents);
            tv_confirmation.setText(R.string.are_you_sure_you_want_to_delete_task);
            AppCompatButton bt_yes = view.findViewById(R.id.btn_yes);
            AppCompatButton btn_no = view.findViewById(R.id.btn_No);
            final AlertDialog dialog = dialogBuilder.create();
            close_details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            bt_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    if (taskModel.getLinkedWithCalendar()) {
                        AndroidUtils.showConfirmationDialog(getContext(), "Confirmation", "This timesheet has an associated event entry. " +
                                        "Do you want to update the event too?", requireContext().getString(R.string.delete_both), requireContext().getString(R.string.delete_timesheet_only), new AndroidUtils.OnConfirmListener() {
                                    @Override
                                    public void onSave() {
                                        timesheet_delete_scope = "DELETE_BOTH";
                                        callDeleteTimesheetwebservice(taskModel.getTaskid(), timesheet_delete_scope);
                                    }

                                    @Override
                                    public void onCancel() {
                                        timesheet_delete_scope = "DELETE_TIMESHEET_ONLY";
                                        callDeleteTimesheetwebservice(taskModel.getTaskid(), timesheet_delete_scope);
                                    }
                                }
                        );
                    } else {
                        timesheet_delete_scope = "DELETE_TIMESHEET_ONLY";
                        callDeleteTimesheetwebservice(taskModel.getTaskid(), timesheet_delete_scope);
                    }
                }
            });
            dialog.setView(view);
            dialog.show();
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void clear_text(boolean isEdit) {
        if (isEdit) {
            AndroidUtils.ToggleButton(1, project_view);
            AndroidUtils.ToggleButton(1, task_view);
            AndroidUtils.ToggleButton(1, status_view);
            AndroidUtils.ToggleButton(1, date_view);
            AndroidUtils.ToggleButton(1, tv_hours);
            AndroidUtils.ToggleButton(1, ll_minutes);
            project_view.setEnabled(false);
            task_view.setEnabled(true);
            status_view.setEnabled(true);
            date_view.setEnabled(true);
            tv_hours.setClickable(true);
            bt_fifteen_minutes.setEnabled(true);
            bt_thirty_minutes.setEnabled(true);
            bt_forty_five_minutes.setEnabled(true);
            img_clear_icon2.setVisibility(View.VISIBLE);
            img_dropdown_icon2.setVisibility(View.GONE);
            img_clear_icon1.setVisibility(View.VISIBLE);
            img_dropdown_icon1.setVisibility(View.GONE);
            img_clear_icon3.setVisibility(View.VISIBLE);
            img_dropdown_icon3.setVisibility(View.GONE);
            img_clear_icon4.setVisibility(View.VISIBLE);
            img_dropdown_icon4.setVisibility(View.GONE);
            img_clear_icon1.setEnabled(false);
            img_clear_icon2.setEnabled(true);
            img_clear_icon3.setEnabled(true);
        } else {
            AndroidUtils.ToggleButton(1, project_view);
            AndroidUtils.ToggleButton(1, task_view);
            AndroidUtils.ToggleButton(1, status_view);
            AndroidUtils.ToggleButton(1, date_view);
            AndroidUtils.ToggleButton(1, ll_minutes);
            AndroidUtils.ToggleButton(1, tv_hours);
            project_view.setEnabled(true);
            task_view.setEnabled(true);
            status_view.setEnabled(true);
            date_view.setEnabled(true);
            tv_hours.setEnabled(true);
            bt_fifteen_minutes.setEnabled(true);
            bt_thirty_minutes.setEnabled(true);
            bt_forty_five_minutes.setEnabled(true);
            img_clear_icon2.setVisibility(View.GONE);
            img_dropdown_icon2.setVisibility(View.VISIBLE);
            img_clear_icon1.setVisibility(View.GONE);
            img_dropdown_icon1.setVisibility(View.VISIBLE);
            img_clear_icon3.setVisibility(View.GONE);
            img_dropdown_icon3.setVisibility(View.VISIBLE);
            img_clear_icon4.setVisibility(View.GONE);
            img_dropdown_icon4.setVisibility(View.VISIBLE);
            img_clear_icon1.setEnabled(true);
            img_clear_icon2.setEnabled(true);
            img_clear_icon3.setEnabled(true);
        }
        this.isEdit = isEdit;
    }

    private int getMatterPosition(String matterId) {
        for (int i = 0; i < activeProjectsList.size(); i++) {
            if (activeProjectsList.get(i).getMatterid().equalsIgnoreCase(matterId)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void EditEvent_Timesheet(TaskModel eventsModels, String selected_date1, String matter_type) {
        task_layout.setVisibility(View.VISIBLE);
        event_name = eventsModels.getTaskid();
        status = eventsModels.getTask_billing();
        task_name = eventsModels.getTask_name();
        matter_name = eventsModels.getTask_matter_name();
        event_task_id = eventsModels.getTask_matter_id();
        event_id = eventsModels.getMatterid();
        hours = eventsModels.getHours();
        isLinkedWithCalendar = eventsModels.getLinkedWithCalendar();
        minutes = eventsModels.getMinutes();
        hoursString = hours;
        tv_hours.setText(hours);
        minutesString = minutes;
        selected_minutes(minutesString);
        selected_matter_type = matter_type;
        if (selected_matter_type != null) {
            callTaskWebservice(selected_matter_type);
        } else {
            callTaskWebservice(event_id);
        }
        selected_status = status;
        String value = selected_date1.substring(4);
        Log.d("Splitted", date + "./././" + value);
        SimpleDateFormat inputformat = new SimpleDateFormat("MMMM dd,yyyy");
        SimpleDateFormat outputformat = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date date = inputformat.parse(value);
            assert date != null;
            selected_date = outputformat.format(date);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        tv_sp_task.setText(task_name);
        int pos = getMatterPosition(event_id);
        if (pos != -1) {
            TSMatterModel model = activeProjectsList.get(pos);
            selected_matter = model.getMattername();
            selected_matter_id = model.getMatterid();
            selected_matter_type = model.getMatter_type();
            tv_sp_project.setText(selected_matter);
            if (selected_matter_type != null) {
                callTaskWebservice(selected_matter_type);
            } else {
                callTaskWebservice(selected_matter_id.toLowerCase(Locale.ROOT));
            }
        }
        tv_sp_date.setText(selected_date);
        btn_save_timesheet.setText(R.string.save);
        isEdit = true;
        tv_sp_status.setText(status);
        int hr = Integer.parseInt(hours);
        int min = Integer.parseInt(minutes);
        String totalTimeText = String.format("%02d:%02d", hr, min);
        tv_total_hours.setText(totalTimeText);
        clear_text(true);
        scrollView.post(() -> {
            scrollView.smoothScrollTo(0, project_view.getTop());
            tv_sp_project.requestFocus();
        });
        if (eventsModels.getEditProject()) {
            AndroidUtils.ToggleButton(1, project_view);
        } else {
            AndroidUtils.ToggleButton(0, project_view);
        }
        if (eventsModels.getEditTask()) {
            AndroidUtils.ToggleButton(1, task_view);
        } else {
            AndroidUtils.ToggleButton(0, task_view);
        }
        if (eventsModels.getEditStatus()) {
            AndroidUtils.ToggleButton(1, status_view);
        } else {
            AndroidUtils.ToggleButton(0, status_view);
        }
        if (eventsModels.getEditDate()) {
            AndroidUtils.ToggleButton(1, date_view);
        } else {
            AndroidUtils.ToggleButton(0, date_view);
        }
        if (eventsModels.getEditHours()) {
            AndroidUtils.ToggleButton(1, tv_hours);
        } else {
            AndroidUtils.ToggleButton(0, tv_hours);
        }
        if (!eventsModels.getEditMinutes()) {
            AndroidUtils.ToggleButton(0, ll_minutes);
            bt_fifteen_minutes.setEnabled(false);
            bt_thirty_minutes.setEnabled(false);
            bt_forty_five_minutes.setEnabled(false);
        } else {
            AndroidUtils.ToggleButton(1, ll_minutes);
            bt_fifteen_minutes.setEnabled(true);
            bt_thirty_minutes.setEnabled(true);
            bt_forty_five_minutes.setEnabled(true);
        }
        Log.d("Edited_timesheet", matter_name + "..." + event_name + "......" + status + "......" + task_name + "......" + event_task_id + "......" + event_id + "......" + hours + "......" + minutes + "......" + selected_date);
    }
}