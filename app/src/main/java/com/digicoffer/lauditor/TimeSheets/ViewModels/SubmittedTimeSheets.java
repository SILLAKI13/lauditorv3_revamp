package com.digicoffer.lauditor.TimeSheets.ViewModels;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.TimeSheets.Adapters.TimeSheetsAdapter;
import com.digicoffer.lauditor.TimeSheets.Models.EventsModel;
import com.digicoffer.lauditor.TimeSheets.Models.StatusModel;
import com.digicoffer.lauditor.TimeSheets.Models.TSMatterModel;
import com.digicoffer.lauditor.TimeSheets.Models.TasksModel;
import com.digicoffer.lauditor.TimeSheets.Models.TimeSheetModel;
import com.digicoffer.lauditor.TimeSheets.Models.WeekModel;
import com.digicoffer.lauditor.TimeSheets.Models.WeekTotalModel;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SubmittedTimeSheets extends Fragment implements AsyncTaskCompleteListener {
    private View view;
    String date = "";
    boolean issubmitted = true;
    String current_date = "";
    private CardView cv_details_activity_log;
    private Dialog progressDialog;
    private ArrayList<TimeSheetModel> timeSheetsList = new ArrayList<>();
    private ArrayList<TSMatterModel> matterList = new ArrayList<>();
    private ArrayList<TasksModel> tasksList = new ArrayList<>();
    private ArrayList<EventsModel> eventsList = new ArrayList<>();
    private ArrayList<StatusModel> statusList = new ArrayList<>();
    private ArrayList<WeekTotalModel> weektotalList = new ArrayList<>();
    private ArrayList<WeekModel> weeksList = new ArrayList<>();
    RecyclerView rv_submitted_timesheets;

    // Empty state views
    LinearLayout layout_empty_state;
    TextView tv_empty_subtitle,tv_title;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.submitted_timesheets, container, false);
        Bundle bundle = getArguments();
        date = bundle.getString("date");
        ArrayList<String> weekDates = bundle.getStringArrayList("weekDates");
        current_date = weekDates.get(0);
        rv_submitted_timesheets = view.findViewById(R.id.rv_submitted_timesheets);

        // Bind empty state views
        layout_empty_state = view.findViewById(R.id.layout_empty_state);
        tv_empty_subtitle = view.findViewById(R.id.tv_empty_subtitle);
        tv_title = view.findViewById(R.id.tv_title);

        if (date.isEmpty()) {
            callCurrentDateTimeSheetsWebservice(date);
        } else if (date == null) {
            callCurrentDateTimeSheetsWebservice(date);
        } else {
            callTimeSheetsWebservice(date);
        }

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

        return view;
    }

    private void callCurrentDateTimeSheetsWebservice(String date) {
        try {
            clearList();
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject data = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/user/timesheets" + "?submitted=true", "TimeSheets", data.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
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
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/user/timesheets/" + outputDate + "?submitted=true", "TimeSheets", data.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void clearList() {
        timeSheetsList.clear();
        weektotalList.clear();
        eventsList.clear();
        matterList.clear();
        statusList.clear();
        tasksList.clear();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progressDialog.isShowing() && progressDialog != null) {
            AndroidUtils.dismiss_dialog(progressDialog);
        }
        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                if (httpResult.getRequestType().equals("TimeSheets")) {
                    JSONObject dates = result.getJSONObject("dates");
                    timeSheetsList.clear();
                    loadTimesheetData(dates, result);
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

    private void loadTimesheetData(JSONObject dates, JSONObject result) throws JSONException {
        timeSheetsList.clear();
        TimeSheetModel timeSheetModel = new TimeSheetModel();
        timeSheetModel.setFrozen(dates.getBoolean("isFrozen"));
        timeSheetModel.setCurrentWeek(dates.getString("currentWeek"));
        timeSheetModel.setNextWeek(dates.getString("nextWeek"));
        timeSheetModel.setPrevWeek(dates.getString("prevWeek"));
        JSONObject timesheets = result.getJSONObject("timesheetList");

        JSONArray matters = timesheets.getJSONArray("matters");
        JSONObject weektotal = timesheets.getJSONObject("weekTotal");
        WeekTotalModel weekModel = new WeekTotalModel();
        weekModel.setMon(weektotal.getString("Mon"));
        weekModel.setTue(weektotal.getString("Tue"));
        weekModel.setWed(weektotal.getString("Wed"));
        weekModel.setThu(weektotal.getString("Thu"));
        weekModel.setFri(weektotal.getString("Fri"));
        weekModel.setSat(weektotal.getString("Sat"));
        weekModel.setSun(weektotal.getString("Sun"));
        weektotalList.add(weekModel);

        for (int i = 0; i < matters.length(); i++) {
            JSONObject jsonObject = matters.getJSONObject(i);
            TSMatterModel matterModel = new TSMatterModel();
            matterModel.setMattername(jsonObject.getString("matterName"));
            matterModel.setMatterid(jsonObject.getString("matterId"));
            matterModel.setTasks(jsonObject.getJSONArray("tasks"));
            if (jsonObject.has("matterType")) {
                matterModel.setMatter_type(jsonObject.getString("matterType"));
            }
            matterList.add(matterModel);
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
                eventsList.add(eventsModel);
            }
        }

        if (timeSheetModel.isFrozen()) {
            updateEmptyState();
            loadTimesheetsRecyclerview();
        } else {
//            tv_title.setText("Timesheet Not Submitted PLease Select Other Week");
//            tv_empty_subtitle.setVisibility(View.GONE);
            // timesheet is not frozen — treat as empty / not submitted
            showEmptyState("Timesheets have not been submitted yet.");
        }
    }

    private void updateEmptyState() {
        if (eventsList.isEmpty()) {
            layout_empty_state.setVisibility(View.VISIBLE);
            rv_submitted_timesheets.setVisibility(View.GONE);
            tv_title.setText("Timesheet Not Submitted PLease Select Other Week");
            tv_empty_subtitle.setVisibility(View.GONE);
        } else {
            layout_empty_state.setVisibility(View.GONE);
            rv_submitted_timesheets.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState(String message) {
        layout_empty_state.setVisibility(View.VISIBLE);
        rv_submitted_timesheets.setVisibility(View.GONE);
        tv_empty_subtitle.setText(message);
    }

    private void loadTimesheetsRecyclerview() {
        TimeSheetsAdapter timeSheetsAdapter = new TimeSheetsAdapter(weeksList, eventsList, weektotalList, getContext(), issubmitted, null, getActivity());
        rv_submitted_timesheets.setAdapter(timeSheetsAdapter);
        AndroidUtils.LoadingRecyclerview(rv_submitted_timesheets, getContext());
        AndroidUtils.setupBottomSpacerFooter(rv_submitted_timesheets, getResources().getDimensionPixelSize(R.dimen.twentyeight_dp));
        if (timeSheetsAdapter != null && timeSheetsAdapter.getItemCount() > 0) {
            int lastPosition = timeSheetsAdapter.getItemCount() - 1;
            rv_submitted_timesheets.smoothScrollToPosition(lastPosition);
        }
    }
}