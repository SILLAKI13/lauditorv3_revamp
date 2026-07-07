package com.digicoffer.lauditor.TimeSheets.ViewModels;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.TimeSheets.Models.DateModel;
import com.digicoffer.lauditor.TimeSheets.Models.WeekDateInfo;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Disabled_view;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class TimeSheets extends Fragment {
    private NewModel mViewModel;
    TextView tv_aggregated_ts, tv_my_ts, tv_ns_timesheet, tv_submitted, tv_week, tv_month, from_id, to_id, tv_to_date_timesheet, tv_from_date_timesheet;
    LinearLayoutCompat ll_timesheet_type, ll_submitted_type, ll_week_month;
    private static final int DIRECTION_PREVIOUS = -1;
    String s = "";
    String endDate, startDate;
    String isweek = "week";
    private static final int DIRECTION_NEXT = 1;
    private ArrayList<DateModel> datesList = new ArrayList<>();
    //    private ArrayAdapter<DateModel> weekAdapter = new ArrayAdapter<DateModel>(getContext(), android.R.layout.simple_spinner_item, datesList);
    Calendar calendar_week = Calendar.getInstance();
    Calendar calendar_month = Calendar.getInstance();
    WeekDateInfo weekDateInfo;
    //    ImageView iv_calendar;
    View tl_search_matter;
    TextInputEditText et_search_matter;
    private static String main_button_status = "";
    private static String non_main_button_status = "";

    //    getWeekDateRange(calendar);
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.timesheet, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(NewModel.class);
        mViewModel.setData(getString(R.string.time_sheets));
        tl_search_matter=view.findViewById(R.id.tl_search_matter);
        et_search_matter = tl_search_matter.findViewById(R.id.et_Search);
        ll_week_month = view.findViewById(R.id.ll_week_month);
        et_search_matter.setHint(R.string.type_to_search);
        et_search_matter.addTextChangedListener(new Validation(et_search_matter));
        ll_submitted_type = view.findViewById(R.id.ll_submitted_type);
        ll_timesheet_type = view.findViewById(R.id.ll_timesheet_type);
        tv_aggregated_ts = view.findViewById(R.id.tv_aggregated_ts);
        from_id = view.findViewById(R.id.from_id);
        to_id = view.findViewById(R.id.to_id);
//        iv_calendar = view.findViewById(R.id.iv_calendar);
        tv_aggregated_ts.setText(R.string.aggregated_timesheets);
        tv_my_ts = view.findViewById(R.id.tv_my_ts);
        from_id.setText(R.string.from);
        from_id.setTextColor(requireContext().getColor(R.color.Blue_text_color));
        to_id.setText(R.string.to);
        to_id.setTextColor(requireContext().getColor(R.color.Blue_text_color));
        tv_my_ts.setText(R.string.my_timesheets);
        tv_ns_timesheet = view.findViewById(R.id.tv_ns_timesheet);
        tv_ns_timesheet.setText(R.string.team_members);
        tv_submitted = view.findViewById(R.id.tv_submitted);
        tv_submitted.setText(R.string.projects);
        tv_week = view.findViewById(R.id.tv_week);
        tv_week.setText(R.string.week);
        tv_month = view.findViewById(R.id.tv_month);
        tv_month.setText(R.string.month);

        tv_from_date_timesheet = view.findViewById(R.id.tv_from_date_timesheet);
        tv_from_date_timesheet.setMaxLines(1);
        tv_from_date_timesheet.setTextSize(DynamicUtils.fifteen);
        tv_from_date_timesheet.setAutoSizeTextTypeUniformWithConfiguration(1, 15, 1, TypedValue.COMPLEX_UNIT_SP);
        tv_to_date_timesheet = view.findViewById(R.id.tv_to_date_timesheet);
        tv_to_date_timesheet.setTextSize(DynamicUtils.fifteen);
        tv_to_date_timesheet.setMaxLines(1);
        tv_to_date_timesheet.setAutoSizeTextTypeUniformWithConfiguration(1, 15, 1, TypedValue.COMPLEX_UNIT_SP);

        //Check is the month view or week view and assign start date an end date accordingly...
        if (Objects.equals(isweek, "month")) {
            month_range(calendar_month);
            tv_from_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(startDate));
            tv_to_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(endDate));
        } else {
            weekDateInfo = getWeekDateRange(calendar_week);
            if (weekDateInfo != null && weekDateInfo.getWeekDates() != null && !weekDateInfo.getWeekDates().isEmpty()) {
                tv_from_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(weekDateInfo.getWeekDates().get(0)));
                tv_to_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(weekDateInfo.getWeekDates().get(weekDateInfo.getWeekDates().size() - 1)));
            }
        }
        if (Constants.ROLE.equals("SU") || (Constants.ROLE.equals("GH"))) {
//            main_button_status = "Aggregated";
//            non_main_button_status = "TM";
            main_button_status = "MyTimeSheets";
            non_main_button_status = "NS";
            ll_timesheet_type.setVisibility(GONE);
//            loadFragment(tv_from_date_timesheet.getText().toString(), weekDateInfo);
        } else {
            ll_timesheet_type.setVisibility(GONE);
            main_button_status = "MyTimeSheets";
            tl_search_matter.setVisibility(GONE);
            non_main_button_status = "NS";
//            loadFragment(tv_from_date_timesheet.getText().toString(), weekDateInfo);
        }
        if (Constants.ts_card_clicked) {
            if (Constants.Timesheet_Card.equals("Agts")) {
                if (Constants.ROLE.equals("SU") || (Constants.ROLE.equals("GH"))) {
                    non_main_button_status = "TM";
                    loadAggregatedTimesheets(AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet.getText().toString()), weekDateInfo);
                } else {
                    non_main_button_status = "NS";
                    loadMyTimeSheets(AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet.getText().toString()), weekDateInfo);
                }
            } else {
                main_button_status = "MyTimeSheets";
                if (Constants.is_ts_submitted) {
                    non_main_button_status = "SU";
                    loadMyTimeSheets(AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet.getText().toString()), weekDateInfo);
                } else {
                    non_main_button_status = "NS";
                    loadMyTimeSheets(AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet.getText().toString()), weekDateInfo);
                }
            }
        } else {
            non_main_button_status = "NS";
            loadMyTimeSheets(AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet.getText().toString()), weekDateInfo);
        }
        ImageButton iv_next_week = view.findViewById(R.id.iv_next_week);
        iv_next_week.setImageDrawable(requireContext().getDrawable(R.drawable.baseline_arrow_forward_ios_24));
        ImageButton iv_previous_week = view.findViewById(R.id.iv_previous_week);

        tv_aggregated_ts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main_button_status = "Aggregated";
                if (Objects.equals(isweek, "month"))
                    month_range(calendar_month);
                else
                    weekDateInfo = getWeekDateRange(calendar_week);
                loadAggregatedTimesheets(AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet.getText().toString()), weekDateInfo);
//                loadWeek();.
            }
        });
        tv_my_ts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main_button_status = "MyTimeSheets";
                loadMyTimeSheets(AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet.getText().toString()), weekDateInfo);
            }
        });
        tv_submitted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tv_submitted.getText().toString().equals("Projects")) {
                    non_main_button_status = "Project";
                    mViewModel.setData(getString(R.string.aggregated_timesheets));
                } else {
                    mViewModel.setData(getString(R.string.time_submit));
                    non_main_button_status = "Submitted";
                }
                loadSubmittedTimesheets(AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet.getText().toString()), weekDateInfo);
            }
        });
        tv_ns_timesheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tv_ns_timesheet.getText().toString().equals("Team Members")) {
                    mViewModel.setData(getString(R.string.aggregated_timesheets));
                    non_main_button_status = "TM";
                } else {
                    mViewModel.setData(getString(R.string.time_sheet_entry));
                    non_main_button_status = "NS";
                }
                loadNsTimesheets(AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet.getText().toString()), weekDateInfo);
            }
        });
        tv_week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_search_matter.setText("");
                calendar_week = Calendar.getInstance();
                loadWeek();
            }
        });
        tv_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_search_matter.setText("");
                loadMonth();
            }
        });
        iv_next_week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //Check is the month view or week view and assign start date an end date accordingly...
                    if (Objects.equals(isweek, "month")) {
                        calendar_month.add(Calendar.MONTH, 1);
                        month_range(calendar_month);
                        tv_from_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(startDate));
                        tv_to_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(endDate));
                    } else {
                        calendar_week.add(Calendar.WEEK_OF_YEAR, 1);
                        weekDateInfo = getWeekDateRange(calendar_week);
                        if (weekDateInfo != null && weekDateInfo.getWeekDates() != null && !weekDateInfo.getWeekDates().isEmpty()) {
                            tv_from_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(weekDateInfo.getWeekDates().get(0)));
                            tv_to_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(weekDateInfo.getWeekDates().get(weekDateInfo.getWeekDates().size() - 1)));
                        }
                    }
                    loadFragment(AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet.getText().toString()), weekDateInfo);
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            }
        });

        tv_to_date_timesheet.setOnClickListener(v -> {

            // ✅ Allow click ONLY in My Timesheets
            if (!"MyTimeSheets".equals(main_button_status)) {
                return; // do nothing
            }
            calendar_week = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(
                    getContext(),
                    (view1, year, month, dayOfMonth) -> {

                        // Update the week calendar based on selected date
                        calendar_week.set(Calendar.YEAR, year);
                        calendar_week.set(Calendar.MONTH, month);
                        calendar_week.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // Recompute week range
                        weekDateInfo = getWeekDateRange(calendar_week);

                        if (weekDateInfo != null &&
                                weekDateInfo.getWeekDates() != null &&
                                !weekDateInfo.getWeekDates().isEmpty()) {

                            tv_from_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(weekDateInfo.getWeekDates().get(0)));
                            tv_to_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(
                                    weekDateInfo.getWeekDates().get(weekDateInfo.getWeekDates().size() - 1))
                            );
                        }

                        // Reload fragment based on newly selected range
                        loadFragment(AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet.getText().toString()), weekDateInfo);

                    },
                    calendar_week.get(Calendar.YEAR),
                    calendar_week.get(Calendar.MONTH),
                    calendar_week.get(Calendar.DAY_OF_MONTH)
            );

            dialog.show();
        });
        tv_from_date_timesheet.setOnClickListener(v -> {

            // ✅ Allow click ONLY in My Timesheets
            if (!"MyTimeSheets".equals(main_button_status)) {
                return; // do nothing
            }
            calendar_week = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(
                    getContext(),
                    (view1, year, month, dayOfMonth) -> {

                        // Update the week calendar based on selected date
                        calendar_week.set(Calendar.YEAR, year);
                        calendar_week.set(Calendar.MONTH, month);
                        calendar_week.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // Recompute week range
                        weekDateInfo = getWeekDateRange(calendar_week);

                        if (weekDateInfo != null &&
                                weekDateInfo.getWeekDates() != null &&
                                !weekDateInfo.getWeekDates().isEmpty()) {

                            tv_from_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(weekDateInfo.getWeekDates().get(0)));
                            tv_to_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(
                                    weekDateInfo.getWeekDates().get(weekDateInfo.getWeekDates().size() - 1))
                            );
                        }

                        // Reload fragment based on newly selected range
                        loadFragment(AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet.getText().toString()), weekDateInfo);

                    },
                    calendar_week.get(Calendar.YEAR),
                    calendar_week.get(Calendar.MONTH),
                    calendar_week.get(Calendar.DAY_OF_MONTH)
            );

            dialog.show();
        });

        iv_previous_week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Check is the month view or week view and assign start date an end date accordingly...
                if (Objects.equals(isweek, "month")) {
                    calendar_month.add(Calendar.MONTH, -1);
                    month_range(calendar_month);
                    tv_from_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(startDate));
                    tv_to_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(endDate));
                } else {
                    calendar_week.add(Calendar.WEEK_OF_YEAR, -1);
                    weekDateInfo = getWeekDateRange(calendar_week);
                    if (weekDateInfo != null && weekDateInfo.getWeekDates() != null && !weekDateInfo.getWeekDates().isEmpty()) {
                        tv_from_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(weekDateInfo.getWeekDates().get(0)));
                        tv_to_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(weekDateInfo.getWeekDates().get(weekDateInfo.getWeekDates().size() - 1)));
                    }
                }
                loadFragment(AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet.getText().toString()), weekDateInfo);
            }
        });
//        main_button_status = "MyTS";
//        loadMyTimeSheets(s, weekDateInfo);
        return view;
    }

    private void loadAggregatedTimesheets(String s, WeekDateInfo weekDateInfo) {
        tv_month.setEnabled(true);
        tv_aggregated_ts.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        tv_aggregated_ts.setTextColor(Color.WHITE);
        tv_my_ts.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
        tv_my_ts.setTextColor(Color.BLACK);
        tv_ns_timesheet.setVisibility(VISIBLE);
        tv_ns_timesheet.setText(R.string.team_members);
        tv_submitted.setText(R.string.projects);
//        mViewModel.setData(getString(R.string.time_sheets));
//        non_main_button_status = "TM";
        if (!"solo".equals(Constants.CATEGORY)) {
            if ((non_main_button_status.equals("TM")) || (non_main_button_status.equals("NS"))) {
                loadTMFragment(s, isweek);
                Log.d("ssssss", s);
            } else {
                loadProjectFragment(s, weekDateInfo, isweek);
            }
        } else {
            non_main_button_status = "Project";
            tv_ns_timesheet.setVisibility(GONE);
            loadProjectFragment(s, weekDateInfo, isweek);
            tv_submitted.setTextColor(Color.WHITE);
            tv_submitted.setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.rounder_button_green));
        }
        if (Objects.equals(isweek, "month")) {
            month_ui();
        } else {
            week_ui();
        }
        mViewModel.setData(getString(R.string.aggregated_timesheets));
    }
//    private void loadMyTimeSheets(String s, WeekDateInfo weekDateInfo) {
//        tv_aggregated_ts.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
//        tv_my_ts.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_count));
//        tv_ns_timesheet.setText("Not Submitted");
//        tv_submitted.setText("Submitted");
//        if (non_main_button_status.equals("NS")){
//            loadNsTimesheets(s,weekDateInfo);
//        }else {
//            loadSubmittedTimesheets(s, weekDateInfo);
//        }

    /// /        if (non_main_button_status)
//    }
    private void loadMyTimeSheets(String s, WeekDateInfo weekDateInfo) {
//        non_main_button_status = "NS";
        tv_month.setEnabled(false);
        tv_aggregated_ts.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_aggregated_ts.setTextColor(Color.BLACK);
        tv_my_ts.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_count));
        tv_my_ts.setTextColor(Color.WHITE);
        tv_ns_timesheet.setText(R.string.not_submitted);
        tv_submitted.setText(R.string.submitted);
        loadWeek();
        tv_ns_timesheet.setVisibility(VISIBLE);
//        loadNsTimesheets(s,weekDateInfo);
        if ((non_main_button_status.equals("NS")) || ((non_main_button_status.equals("TM")))) {
            loadNsTimesheets(s, weekDateInfo);
            Log.d("ssssss", s);
        } else {
            loadSubmittedTimesheets(s, weekDateInfo);
        }
        week_ui();
        mViewModel.setData(getString(R.string.time_sheet_entry));
//        loadWeek();
//        if (non_main_button_status)
    }

    private void loadNsTimesheets(String s, WeekDateInfo weekDateInfo) {
        tv_ns_timesheet.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        tv_ns_timesheet.setTextColor(Color.WHITE);
        tv_submitted.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
        tv_submitted.setTextColor(Color.BLACK);
//        loadFragment();
        if (tv_ns_timesheet.getText().toString().equals("Not Submitted")) {
            loadNsFragment(s, weekDateInfo);
        } else {
            loadTMFragment(s, isweek);
        }
    }

    private void week_ui() {
        if (tv_ns_timesheet.getText().toString().equals("Not Submitted")) {
            tv_week.setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.rectangular_button_green_count));
            tv_week.setTextColor(Color.WHITE);
        } else {
            ll_week_month.setVisibility(VISIBLE);
//            iv_calendar.setVisibility(GONE);
            tv_from_date_timesheet.setBackground(requireContext().getDrawable(R.drawable.background_transparent));
            tv_to_date_timesheet.setBackground(requireContext().getDrawable(R.drawable.background_transparent));
            tv_month.setVisibility(VISIBLE);
            tv_week.setTextColor(Color.WHITE);
            tv_week.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
            tv_month.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
            tv_month.setTextColor(Color.BLACK);
        }
    }

    private void loadWeek() {
        week_ui();
        isweek = "week";
//        Check is the month view or week view and assign start date an end date accordingly...
        if (Objects.equals(isweek, "month")) {
            month_range(calendar_month);
            tv_from_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(startDate));
            tv_to_date_timesheet.setText(endDate);
        } else {
            weekDateInfo = getWeekDateRange(calendar_week);
            if (weekDateInfo.getWeekDates() != null && !weekDateInfo.getWeekDates().isEmpty()) {
                tv_from_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(weekDateInfo.getWeekDates().get(0)));
                tv_to_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(weekDateInfo.getWeekDates().get(weekDateInfo.getWeekDates().size() - 1)));
            }
        }
        if (tv_ns_timesheet.getText().toString().equals("Not Submitted")) {
//            loadNsTimesheets(s,weekDateInfo);
        } else {
            if ((non_main_button_status.equals("TM")) || (non_main_button_status.equals("NS"))) {
                loadTMFragment(AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet.getText().toString()), isweek);
                Log.d("ssssss", tv_from_date_timesheet.getText().toString());
            } else {
                loadProjectFragment(AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet.getText().toString()), weekDateInfo, isweek);
            }
        }
    }

    private void month_ui() {
        tv_week.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_week.setTextColor(Color.BLACK);
        tv_month.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_count));
        tv_month.setTextColor(Color.WHITE);
    }

    private void loadMonth() {
        month_ui();
        calendar_month = Calendar.getInstance();
//        Check is the month view or week view and assign start date an end date accordingly...
        isweek = "month";
        if (Objects.equals(isweek, "month")) {
            month_range(calendar_month);
            tv_from_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(startDate));
            tv_to_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(endDate));
        } else {
            weekDateInfo = getWeekDateRange(calendar_week);
            if (weekDateInfo != null && weekDateInfo.getWeekDates() != null && !weekDateInfo.getWeekDates().isEmpty()) {
                tv_from_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(weekDateInfo.getWeekDates().get(0)));
                tv_to_date_timesheet.setText(AndroidUtils.formatToMMMddYYYY(weekDateInfo.getWeekDates().get(weekDateInfo.getWeekDates().size() - 1)));
            }
        }
        if ((non_main_button_status.equals("TM")) || (non_main_button_status.equals("NS"))) {
            loadTMFragment(AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet.getText().toString()), isweek);
            Log.d("ssssss", tv_from_date_timesheet.getText().toString());
        } else {
            loadProjectFragment(AndroidUtils.convertAnyDateToDDMMYYYY(tv_from_date_timesheet.getText().toString()), weekDateInfo, isweek);
        }
    }

    private void loadSubmittedTimesheets(String s, WeekDateInfo weekDateInfo) {
        tv_submitted.setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.button_right_green_count));
        tv_submitted.setTextColor(Color.WHITE);
        tv_ns_timesheet.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_ns_timesheet.setTextColor(Color.BLACK);
        loadSubmittedFragment(s, weekDateInfo);
    }

    private void loadSubmittedFragment(String s, WeekDateInfo weekDateInfo) {
        if (tv_submitted.getText().toString().equals("Submitted")) {
            ll_week_month.setVisibility(GONE);
//            iv_calendar.setVisibility(VISIBLE);
            tv_from_date_timesheet.setBackground(requireContext().getDrawable(R.drawable.light_grey_bg));
            tv_to_date_timesheet.setBackground(requireContext().getDrawable(R.drawable.light_grey_bg));
            tv_month.setVisibility(GONE);
            Bundle bundle = new Bundle();
            bundle.putString("date", s);
//            AndroidUtils.showToast(s, getContext());
            bundle.putStringArrayList("weekDates", weekDateInfo.getWeekDates());
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            SubmittedTimeSheets nonSubmittedTimesheets = new SubmittedTimeSheets();
            nonSubmittedTimesheets.setArguments(bundle);
            ft.replace(R.id.child_container_timesheets, nonSubmittedTimesheets);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
//            loadSubmittedFragment(s,weekDateInfo);
        } else {
            loadProjectFragment(s, weekDateInfo, isweek);
        }
    }

    private void loadFragment(String s, WeekDateInfo weekDateInfo) {
        if (main_button_status != null && main_button_status.equals("Aggregated")) {
            loadAggregatedTimesheets(s, weekDateInfo);
        } else {
            loadMyTimeSheets(s, weekDateInfo);
        }

    }

    public void Frozen_view() {
        main_button_status = "MyTimeSheets";
        non_main_button_status = "NS";
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        String frozenText = getString(R.string.timesheet_already_submitted_please_select_other_week);
        Disabled_view disabled_view = new Disabled_view(frozenText, false);
        ft.add(R.id.child_container_timesheets, disabled_view);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void load_month(String s) {
        {
            Bundle bundle = new Bundle();
            bundle.putString("date", s);
//        bundle.putStringArrayList("weekDates", weekDateInfo.getWeekDates());
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            AGS_TeamMembers nonSubmittedTimesheets = new AGS_TeamMembers(et_search_matter, et_search_matter.getText().toString());
            nonSubmittedTimesheets.setArguments(bundle);
            ft.replace(R.id.child_container_timesheets, nonSubmittedTimesheets);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    private void loadTMFragment(String s, String isweek) {
        Bundle bundle = new Bundle();
        bundle.putString("date", s);
        bundle.putString("isweek", isweek);
        tv_ns_timesheet.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        tv_ns_timesheet.setTextColor(Color.WHITE);
        tv_submitted.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
        tv_submitted.setTextColor(Color.BLACK);
//        bundle.putStringArrayList("weekDates", weekDateInfo.getWeekDates());
//        AndroidUtils.showToast(s,getContext());
        tl_search_matter.setVisibility(VISIBLE);
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        AGS_TeamMembers nonSubmittedTimesheets = new AGS_TeamMembers(et_search_matter, et_search_matter.getText().toString());
        nonSubmittedTimesheets.setArguments(bundle);
        ft.replace(R.id.child_container_timesheets, nonSubmittedTimesheets);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void loadNsFragment(String s, WeekDateInfo weekDateInfo) {
        tl_search_matter.setVisibility(GONE);
        Bundle bundle = new Bundle();
        bundle.putString("date", s);
        ll_week_month.setVisibility(GONE);
//        iv_calendar.setVisibility(VISIBLE);
        tv_from_date_timesheet.setBackground(requireContext().getDrawable(R.drawable.light_grey_bg));
        tv_to_date_timesheet.setBackground(requireContext().getDrawable(R.drawable.light_grey_bg));
        tv_month.setVisibility(GONE);
        tv_week.setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.rectangular_button_green_count));
        bundle.putStringArrayList("weekDates", weekDateInfo.getWeekDates());
//        AndroidUtils.showToast(s,getContext());
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        NonSubmittedTimesheets nonSubmittedTimesheets = new NonSubmittedTimesheets(null);
        nonSubmittedTimesheets.setArguments(bundle);
        ft.replace(R.id.child_container_timesheets, nonSubmittedTimesheets);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void loadProjectFragment(String s, WeekDateInfo weekDateInfo, String isweek) {
        et_search_matter.setVisibility(GONE);
        ll_week_month.setVisibility(VISIBLE);
//        iv_calendar.setVisibility(GONE);
        tv_from_date_timesheet.setBackground(requireContext().getDrawable(R.drawable.background_transparent));
        tv_to_date_timesheet.setBackground(requireContext().getDrawable(R.drawable.background_transparent));
        tv_month.setVisibility(VISIBLE);
        Bundle bundle = new Bundle();
        bundle.putString("date", s);
        bundle.putString("isweek", isweek);
        bundle.putStringArrayList("weekDates", weekDateInfo.getWeekDates());
//        AndroidUtils.showToast(s,getContext());
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        AGS_Projects nonSubmittedTimesheets = new AGS_Projects();
        nonSubmittedTimesheets.setArguments(bundle);
        ft.replace(R.id.child_container_timesheets, nonSubmittedTimesheets);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
    }

    //Method to get the start and end date of chosen month
    private void month_range(Calendar month_calendar) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        month_calendar.set(Calendar.DAY_OF_MONTH, 1);
//        int st_date=month_calendar.get(Calendar.DATE);
        startDate = format.format(month_calendar.getTime());
        Log.d("stttt+date", startDate);

        month_calendar.set(Calendar.DAY_OF_MONTH, month_calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
//        int ed_date=month_calendar.get(Calendar.DAY_OF_MONTH);
        endDate = format.format(month_calendar.getTime());
        Log.d("edddd+date", endDate);
    }

    private WeekDateInfo getWeekDateRange(Calendar calendar) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

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
        return new WeekDateInfo(startDate + " - " + endDate, weekDates);
    }
}
