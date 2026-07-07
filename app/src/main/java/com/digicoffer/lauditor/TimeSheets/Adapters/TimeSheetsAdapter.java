package com.digicoffer.lauditor.TimeSheets.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.TimeSheets.Models.EventsModel;
import com.digicoffer.lauditor.TimeSheets.Models.TaskModel;
import com.digicoffer.lauditor.TimeSheets.Models.WeekModel;
import com.digicoffer.lauditor.TimeSheets.Models.WeekTotalModel;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;

import org.json.JSONException;

import java.util.ArrayList;


public class TimeSheetsAdapter extends RecyclerView.Adapter<TimeSheetsAdapter.MyViewHolder> {
    ArrayList<WeekModel> weeksList = new ArrayList<>();
    ArrayList<EventsModel> eventsList = new ArrayList<>();
    ArrayList<WeekTotalModel> weekTotalList = new ArrayList<>();
    ArrayList<TaskModel> monday = new ArrayList<>();
    ArrayList<TaskModel> tuesday = new ArrayList<>();
    ArrayList<TaskModel> wednessday = new ArrayList<>();
    ArrayList<TaskModel> thursday = new ArrayList<>();
    ArrayList<TaskModel> friday = new ArrayList<>();
    ArrayList<TaskModel> saturday = new ArrayList<>();
    ArrayList<TaskModel> sunday = new ArrayList<>();
    Activity activity;
    Context context;
    String date = "", matter_type;
    private boolean issubmitted;
    WeeklyTSAdapter.InterfaceListener interfaceListener;

    public TimeSheetsAdapter(ArrayList<WeekModel> weeksList, ArrayList<EventsModel> eventsModels, ArrayList<WeekTotalModel> weektotalList, Context cContext, boolean issubmitted, WeeklyTSAdapter.InterfaceListener interfaceListener1, Activity activity) {
        this.activity = activity;
        this.weeksList = weeksList;
        this.eventsList = eventsModels;
        this.weekTotalList = weektotalList;
        this.context = cContext;
        this.issubmitted = issubmitted;
        this.interfaceListener = interfaceListener1;
    }

    @NonNull
    @Override


    public TimeSheetsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.timesheets_recyclerview, parent, false);
        return new TimeSheetsAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSheetsAdapter.MyViewHolder holder, int position) {
        WeekModel weekModel = weeksList.get(position);
//        AndroidUtils.showAlert(String.valueOf(weeksList.size()), context);
        try {
            monday.clear();
            tuesday.clear();
            wednessday.clear();
            thursday.clear();
            friday.clear();
            saturday.clear();
            sunday.clear();
            for (int e = 0; e < eventsList.size(); e++) {
                EventsModel eventsModel = eventsList.get(e);

                //Check matter type value
                matter_type = eventsModel.getMatter_type();
//                for (int i = 0; i < eventsModel.getMon().length(); i++) {
                TaskModel taskModel = new TaskModel();
                if (eventsModel.getMon().has("taskId")) {
                    taskModel.setTaskid(eventsModel.getMon().getString("taskId"));
                }
                if (eventsModel.getMon().has("isLinkedWithCalendar")) {
                    taskModel.setLinkedWithCalendar(eventsModel.getMon().optBoolean("isLinkedWithCalendar"));
                }
                if (eventsModel.getMon().has("matterId")) {
                    taskModel.setMatterid(eventsModel.getMon().getString("matterId"));
                }
                taskModel.setMinutes(eventsModel.getMon().getString("minutes"));
                taskModel.setHours(eventsModel.getMon().getString("hours"));

                taskModel.setTask_name(eventsModel.getTaskName());
                if (!(eventsModel.getBilling() == null)) {
                    taskModel.setTask_billing(eventsModel.getBilling());
                }
                taskModel.setTask_matter_name(eventsModel.getMatter_name());
                taskModel.setIs_editable(eventsModel.getIs_editable());
                taskModel.setTask_matter_id(eventsModel.getMatter_id());
                if (eventsModel.getMon().has("permissions")) {
                    taskModel.setPermissions(eventsModel.getMon().getJSONObject("permissions"));
                    taskModel.setEditProject(taskModel.getPermissions().getBoolean("editProject"));
                    taskModel.setEditTask(taskModel.getPermissions().getBoolean("editTask"));
                    taskModel.setEditStatus(taskModel.getPermissions().getBoolean("editStatus"));
                    taskModel.setEditDate(taskModel.getPermissions().getBoolean("editDate"));
                    taskModel.setEditHours(taskModel.getPermissions().getBoolean("editHours"));
                    taskModel.setEditMinutes(taskModel.getPermissions().getBoolean("editMinutes"));
                }
//                taskModel.set
                if (!(taskModel.getHours().equals("0") && taskModel.getMinutes().equals("0"))) {
                    monday.add(taskModel);
                }


                TaskModel taskModel_tue = new TaskModel();
                if (eventsModel.getTue().has("taskId")) {
                    taskModel_tue.setTaskid(eventsModel.getTue().getString("taskId"));
                }
                if (eventsModel.getTue().has("permissions")) {
                    taskModel_tue.setPermissions(eventsModel.getTue().getJSONObject("permissions"));
                    taskModel_tue.setEditProject(taskModel_tue.getPermissions().getBoolean("editProject"));
                    taskModel_tue.setEditTask(taskModel_tue.getPermissions().getBoolean("editTask"));
                    taskModel_tue.setEditStatus(taskModel_tue.getPermissions().getBoolean("editStatus"));
                    taskModel_tue.setEditDate(taskModel_tue.getPermissions().getBoolean("editDate"));
                    taskModel_tue.setEditHours(taskModel_tue.getPermissions().getBoolean("editHours"));
                    taskModel_tue.setEditMinutes(taskModel_tue.getPermissions().getBoolean("editMinutes"));
                }
                if (eventsModel.getTue().has("isLinkedWithCalendar")) {
                    taskModel_tue.setLinkedWithCalendar(eventsModel.getTue().optBoolean("isLinkedWithCalendar"));
                }
                if (eventsModel.getTue().has("matterId")) {
                    taskModel_tue.setMatterid(eventsModel.getTue().getString("matterId"));
                }
                taskModel_tue.setMinutes(eventsModel.getTue().getString("minutes"));
                taskModel_tue.setHours(eventsModel.getTue().getString("hours"));

                taskModel_tue.setTask_name(eventsModel.getTaskName());
                if (!(eventsModel.getBilling() == null)) {
                    taskModel_tue.setTask_billing(eventsModel.getBilling());
                }
                taskModel_tue.setTask_matter_name(eventsModel.getMatter_name());
                taskModel_tue.setIs_editable(eventsModel.getIs_editable());
                taskModel_tue.setTask_matter_id(eventsModel.getMatter_id());
                if (!(taskModel_tue.getHours().equals("0") && taskModel_tue.getMinutes().equals("0"))) {
                    tuesday.add(taskModel_tue);
                }


                TaskModel taskModel_wed = new TaskModel();
                if (eventsModel.getWed().has("taskId")) {
                    taskModel_wed.setTaskid(eventsModel.getWed().getString("taskId"));
                }
                if (eventsModel.getWed().has("permissions")) {
                    taskModel_wed.setPermissions(eventsModel.getWed().getJSONObject("permissions"));
                    taskModel_wed.setEditProject(taskModel_wed.getPermissions().getBoolean("editProject"));
                    taskModel_wed.setEditTask(taskModel_wed.getPermissions().getBoolean("editTask"));
                    taskModel_wed.setEditStatus(taskModel_wed.getPermissions().getBoolean("editStatus"));
                    taskModel_wed.setEditDate(taskModel_wed.getPermissions().getBoolean("editDate"));
                    taskModel_wed.setEditHours(taskModel_wed.getPermissions().getBoolean("editHours"));
                    taskModel_wed.setEditMinutes(taskModel_wed.getPermissions().getBoolean("editMinutes"));
                }
                if (eventsModel.getWed().has("isLinkedWithCalendar")) {
                    taskModel_wed.setLinkedWithCalendar(eventsModel.getWed().optBoolean("isLinkedWithCalendar"));
                }
                if (eventsModel.getWed().has("matterId")) {
                    taskModel_wed.setMatterid(eventsModel.getWed().getString("matterId"));
                }
                taskModel_wed.setMinutes(eventsModel.getWed().getString("minutes"));
                taskModel_wed.setHours(eventsModel.getWed().getString("hours"));

                taskModel_wed.setTask_name(eventsModel.getTaskName());
                if (!(eventsModel.getBilling() == null)) {
                    taskModel_wed.setTask_billing(eventsModel.getBilling());
                }
                taskModel_wed.setTask_matter_name(eventsModel.getMatter_name());
                taskModel_wed.setIs_editable(eventsModel.getIs_editable());
                taskModel_wed.setTask_matter_id(eventsModel.getMatter_id());
                if (!(taskModel_wed.getHours().equals("0") && taskModel_wed.getMinutes().equals("0"))) {
                    wednessday.add(taskModel_wed);
                }

                TaskModel taskModel_thu = new TaskModel();
                if (eventsModel.getThu().has("taskId")) {
                    taskModel_thu.setTaskid(eventsModel.getThu().getString("taskId"));
                }
                if (eventsModel.getThu().has("permissions")) {
                    taskModel_thu.setPermissions(eventsModel.getThu().getJSONObject("permissions"));
                    taskModel_thu.setEditProject(taskModel_thu.getPermissions().getBoolean("editProject"));
                    taskModel_thu.setEditTask(taskModel_thu.getPermissions().getBoolean("editTask"));
                    taskModel_thu.setEditStatus(taskModel_thu.getPermissions().getBoolean("editStatus"));
                    taskModel_thu.setEditDate(taskModel_thu.getPermissions().getBoolean("editDate"));
                    taskModel_thu.setEditHours(taskModel_thu.getPermissions().getBoolean("editHours"));
                    taskModel_thu.setEditMinutes(taskModel_thu.getPermissions().getBoolean("editMinutes"));
                }
                if (eventsModel.getThu().has("isLinkedWithCalendar")) {
                    taskModel_thu.setLinkedWithCalendar(eventsModel.getThu().optBoolean("isLinkedWithCalendar"));
                }
                if (eventsModel.getThu().has("matterId")) {
                    taskModel_thu.setMatterid(eventsModel.getThu().getString("matterId"));
                }
                taskModel_thu.setMinutes(eventsModel.getThu().getString("minutes"));
                taskModel_thu.setHours(eventsModel.getThu().getString("hours"));

                taskModel_thu.setTask_name(eventsModel.getTaskName());
                if (!(eventsModel.getBilling() == null)) {
                    taskModel_thu.setTask_billing(eventsModel.getBilling());
                }
                taskModel_thu.setTask_matter_name(eventsModel.getMatter_name());
                taskModel_thu.setIs_editable(eventsModel.getIs_editable());
                taskModel_thu.setTask_matter_id(eventsModel.getMatter_id());
                if (!(taskModel_thu.getHours().equals("0") && taskModel_thu.getMinutes().equals("0"))) {
                    thursday.add(taskModel_thu);
                }
                TaskModel taskModel_fri = new TaskModel();
                if (eventsModel.getFri().has("taskId")) {
                    taskModel_fri.setTaskid(eventsModel.getFri().getString("taskId"));
                }
                if (eventsModel.getFri().has("permissions")) {
                    taskModel_fri.setPermissions(eventsModel.getFri().getJSONObject("permissions"));
                    taskModel_fri.setEditProject(taskModel_fri.getPermissions().getBoolean("editProject"));
                    taskModel_fri.setEditTask(taskModel_fri.getPermissions().getBoolean("editTask"));
                    taskModel_fri.setEditStatus(taskModel_fri.getPermissions().getBoolean("editStatus"));
                    taskModel_fri.setEditDate(taskModel_fri.getPermissions().getBoolean("editDate"));
                    taskModel_fri.setEditHours(taskModel_fri.getPermissions().getBoolean("editHours"));
                    taskModel_fri.setEditMinutes(taskModel_fri.getPermissions().getBoolean("editMinutes"));
                }
                if (eventsModel.getFri().has("isLinkedWithCalendar")) {
                    taskModel_fri.setLinkedWithCalendar(eventsModel.getFri().optBoolean("isLinkedWithCalendar"));
                }
                if (eventsModel.getFri().has("matterId")) {
                    taskModel_fri.setMatterid(eventsModel.getFri().getString("matterId"));
                }
                taskModel_fri.setMinutes(eventsModel.getFri().getString("minutes"));
                taskModel_fri.setHours(eventsModel.getFri().getString("hours"));
                taskModel_fri.setTask_name(eventsModel.getTaskName());
                if (!(eventsModel.getBilling() == null)) {
                    taskModel_fri.setTask_billing(eventsModel.getBilling());
                }
                taskModel_fri.setTask_matter_name(eventsModel.getMatter_name());
                taskModel_fri.setIs_editable(eventsModel.getIs_editable());
                taskModel_fri.setTask_matter_id(eventsModel.getMatter_id());
                if (!(taskModel_fri.getHours().equals("0") && taskModel_fri.getMinutes().equals("0"))) {
                    friday.add(taskModel_fri);
                }
//                for (int i = 0; i < eventsModel.getFri().length(); i++) {
                TaskModel taskModel_sat = new TaskModel();
                if (eventsModel.getSat().has("taskId")) {
                    taskModel_sat.setTaskid(eventsModel.getSat().getString("taskId"));
                }
                if (eventsModel.getSat().has("permissions")) {
                    taskModel_sat.setPermissions(eventsModel.getSat().getJSONObject("permissions"));
                    taskModel_sat.setEditProject(taskModel_sat.getPermissions().getBoolean("editProject"));
                    taskModel_sat.setEditTask(taskModel_sat.getPermissions().getBoolean("editTask"));
                    taskModel_sat.setEditStatus(taskModel_sat.getPermissions().getBoolean("editStatus"));
                    taskModel_sat.setEditDate(taskModel_sat.getPermissions().getBoolean("editDate"));
                    taskModel_sat.setEditHours(taskModel_sat.getPermissions().getBoolean("editHours"));
                    taskModel_sat.setEditMinutes(taskModel_sat.getPermissions().getBoolean("editMinutes"));
                }
                if (eventsModel.getSat().has("isLinkedWithCalendar")) {
                    taskModel_sat.setLinkedWithCalendar(eventsModel.getSat().optBoolean("isLinkedWithCalendar"));
                }
                if (eventsModel.getSat().has("matterId")) {
                    taskModel_sat.setMatterid(eventsModel.getSat().getString("matterId"));
                }
                taskModel_sat.setMinutes(eventsModel.getSat().getString("minutes"));
                taskModel_sat.setHours(eventsModel.getSat().getString("hours"));
                taskModel_sat.setTask_name(eventsModel.getTaskName());
                if (!(eventsModel.getBilling() == null)) {
                    taskModel_sat.setTask_billing(eventsModel.getBilling());
                }
                taskModel_sat.setTask_matter_name(eventsModel.getMatter_name());
                taskModel_sat.setIs_editable(eventsModel.getIs_editable());
                taskModel_sat.setTask_matter_id(eventsModel.getMatter_id());
                if (!(taskModel_sat.getHours().equals("0") && taskModel_sat.getMinutes().equals("0"))) {
                    saturday.add(taskModel_sat);
                }
//                }
//                for (int i = 0; i < eventsModel.getSun().length(); i++) {
                TaskModel taskModel_sun = new TaskModel();
                if (eventsModel.getSun().has("taskId")) {
                    taskModel_sun.setTaskid(eventsModel.getSun().getString("taskId"));
                }
                if (eventsModel.getSun().has("permissions")) {
                    taskModel_sun.setPermissions(eventsModel.getSun().getJSONObject("permissions"));
                    taskModel_sun.setEditProject(taskModel_sun.getPermissions().getBoolean("editProject"));
                    taskModel_sun.setEditTask(taskModel_sun.getPermissions().getBoolean("editTask"));
                    taskModel_sun.setEditStatus(taskModel_sun.getPermissions().getBoolean("editStatus"));
                    taskModel_sun.setEditDate(taskModel_sun.getPermissions().getBoolean("editDate"));
                    taskModel_sun.setEditHours(taskModel_sun.getPermissions().getBoolean("editHours"));
                    taskModel_sun.setEditMinutes(taskModel_sun.getPermissions().getBoolean("editMinutes"));
                }
                if (eventsModel.getSun().has("isLinkedWithCalendar")) {
                    taskModel_sun.setLinkedWithCalendar(eventsModel.getSun().optBoolean("isLinkedWithCalendar"));
                }
                if (eventsModel.getSun().has("matterId")) {
                    taskModel_sun.setMatterid(eventsModel.getSun().getString("matterId"));
                }
                taskModel_sun.setMinutes(eventsModel.getSun().getString("minutes"));
                taskModel_sun.setHours(eventsModel.getSun().getString("hours"));
                taskModel_sun.setTask_name(eventsModel.getTaskName());
                if (!(eventsModel.getBilling() == null)) {
                    taskModel_sun.setTask_billing(eventsModel.getBilling());
                }
                taskModel_sun.setTask_matter_name(eventsModel.getMatter_name());
                taskModel_sun.setIs_editable(eventsModel.getIs_editable());
                taskModel_sun.setTask_matter_id(eventsModel.getMatter_id());
                if (!(taskModel_sun.getHours().equals("0") && taskModel_sun.getMinutes().equals("0"))) {
                    sunday.add(taskModel_sun);
                }
            }
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
        holder.tv_date.setText(weekModel.getValue());
        date = weekModel.getValue();
//        holder.tv_total_hours.setText();
        String inputString = weekModel.getValue();
        String[] parts = inputString.split(" "); // Split the string by whitespace
        String dayOfWeek = parts[0];
        for (int i = 0; i < weekTotalList.size(); i++) {
            if (dayOfWeek.equals("Mon")) {
                holder.tv_total_hours.setText(weekTotalList.get(i).getMon() + " Hours");
            } else if (dayOfWeek.equals("Tue")) {
                holder.tv_total_hours.setText(weekTotalList.get(i).getTue() + " Hours");
            } else if (dayOfWeek.equals("Wed")) {
                holder.tv_total_hours.setText(weekTotalList.get(i).getWed() + " Hours");
            } else if (dayOfWeek.equals("Thu")) {
                holder.tv_total_hours.setText(weekTotalList.get(i).getThu() + " Hours");
            } else if (dayOfWeek.equals("Fri")) {
                holder.tv_total_hours.setText(weekTotalList.get(i).getFri() + " Hours");
            } else if (dayOfWeek.equals("Sat")) {
                holder.tv_total_hours.setText(weekTotalList.get(i).getSat() + " Hours");
            } else if (dayOfWeek.equals("Sun")) {
                holder.tv_total_hours.setText(weekTotalList.get(i).getSun() + " Hours");
            }
        }

//            for ()
        if (dayOfWeek.equals("Mon")) {
//        AndroidUtils.showAlert(eventsList.toString(),context);
            try {
                if (monday.size() != 0) {
                    loadRecyclerview(holder, monday, issubmitted);
                    holder.timesheet_layout.setVisibility(View.VISIBLE);
                } else {
                    holder.timesheet_layout.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                AndroidUtils.showAlert(e.getMessage(), activity);
                e.fillInStackTrace();
            }

        }
        if (dayOfWeek.equals("Tue")) {
//        AndroidUtils.showAlert(eventsList.toString(),context);
            try {
                if (tuesday.size() != 0) {
                    loadRecyclerview(holder, tuesday, issubmitted);
                    holder.timesheet_layout.setVisibility(View.VISIBLE);
                } else
                    holder.timesheet_layout.setVisibility(View.GONE);
            } catch (Exception e) {
                AndroidUtils.showAlert(e.getMessage(), activity);
                e.fillInStackTrace();
            }

        }
        if (dayOfWeek.equals("Wed")) {
//        AndroidUtils.showAlert(eventsList.toString(),context);
            try {
                if (wednessday.size() != 0) {
                    loadRecyclerview(holder, wednessday, issubmitted);
                    holder.timesheet_layout.setVisibility(View.VISIBLE);
                } else {
                    holder.timesheet_layout.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                AndroidUtils.showAlert(e.getMessage(), activity);
                e.fillInStackTrace();
            }

        }
        if (dayOfWeek.equals("Thu")) {
//        AndroidUtils.showAlert(eventsList.toString(),context);
            try {
                if (thursday.size() != 0) {
                    loadRecyclerview(holder, thursday, issubmitted);
                    holder.timesheet_layout.setVisibility(View.VISIBLE);
                } else {
                    holder.timesheet_layout.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                AndroidUtils.showAlert(e.getMessage(), activity);
                e.fillInStackTrace();
            }

        }
        if (dayOfWeek.equals("Fri")) {
//        AndroidUtils.showAlert(eventsList.toString(),context);
            try {
                if (friday.size() != 0) {
                    loadRecyclerview(holder, friday, issubmitted);
                    holder.timesheet_layout.setVisibility(View.VISIBLE);
                } else {
                    holder.timesheet_layout.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                AndroidUtils.showAlert(e.getMessage(), activity);
                e.fillInStackTrace();
            }

        }
        if (dayOfWeek.equals("Sat")) {
//        AndroidUtils.showAlert(eventsList.toString(),context);
            try {
                if (saturday.size() != 0) {
                    loadRecyclerview(holder, saturday, issubmitted);
                    holder.timesheet_layout.setVisibility(View.VISIBLE);
                } else {
                    holder.timesheet_layout.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                AndroidUtils.showAlert(e.getMessage(), activity);
                e.fillInStackTrace();
            }

        }
        if (dayOfWeek.equals("Sun")) {
//        AndroidUtils.showAlert(eventsList.toString(),context);
            try {
                if (sunday.size() != 0) {
                    loadRecyclerview(holder, sunday, issubmitted);
                    holder.timesheet_layout.setVisibility(View.VISIBLE);
                } else {
                    holder.timesheet_layout.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                AndroidUtils.showAlert(e.getMessage(), activity);
                e.fillInStackTrace();
            }

        }

//        holder.rv_time_sheets.setLayoutManager(new GridLayoutManager(context,1));
//        TaskAdapter weeklyTSAdapter= new TaskAdapter(monday);
//        holder.rv_time_sheets.setAdapter(weeklyTSAdapter);
//        holder.rv_time_sheets.setHasFixedSize(true);

    }

    private void loadRecyclerview(MyViewHolder holder, ArrayList<TaskModel> list, boolean issubmitted) throws Exception {
        holder.rv_time_sheets.setLayoutManager(new GridLayoutManager(context, 1));
        WeeklyTSAdapter weeklyTSAdapter = new WeeklyTSAdapter(list, issubmitted, context, interfaceListener, date, matter_type);
        holder.rv_time_sheets.setAdapter(weeklyTSAdapter);
        //holder.rv_time_sheets.setHasFixedSize(true);
        if (weeklyTSAdapter != null && weeklyTSAdapter.getItemCount() > 0) {
            int lastPosition = weeklyTSAdapter.getItemCount() - 1;
            holder.rv_time_sheets.smoothScrollToPosition(lastPosition);
        }
        weeklyTSAdapter.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return weeksList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_date, tv_total_hours, total_hours_id;
        LinearLayoutCompat timesheet_layout;
        RecyclerView rv_time_sheets;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_date = itemView.findViewById(R.id.tv_date);
            timesheet_layout = itemView.findViewById(R.id.ll_documents);
            rv_time_sheets = itemView.findViewById(R.id.rv_time_sheets);
            tv_total_hours = itemView.findViewById(R.id.tv_total_hours);
            total_hours_id = itemView.findViewById(R.id.total_hours_id);
            total_hours_id.setTextSize(DynamicUtils.twenty);
            tv_total_hours.setTextSize(DynamicUtils.twenty);
            tv_date.setTextColor(context.getColor(R.color.Primary_new));
            total_hours_id.setTextColor(context.getColor(R.color.Primary_new));
            total_hours_id.setText(R.string.total_hours);
            tv_total_hours.setTextColor(context.getColor(R.color.black));
//            tv_total_hours.setTextColor(Color.parseColor("#00B3A7"));
        }
    }
}
