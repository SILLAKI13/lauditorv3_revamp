package com.digicoffer.lauditor.TimeSheets.Adapters;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.TimeSheets.Models.TaskModel;
import com.digicoffer.lauditor.TimeSheets.ViewModels.TimeSheets;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;

import java.util.ArrayList;

public class WeeklyTSAdapter extends RecyclerView.Adapter<WeeklyTSAdapter.MyViewHolder> {
    ArrayList<TaskModel> eventsList = new ArrayList<>();
    private boolean issubmitted;
    TimeSheets timeSheets = new TimeSheets();
    Context context_timesheets;
    InterfaceListener eventListener;
    String date, timesheet_delete_scope = "DELETE_TIMESHEET_ONLY";
    String matter_type;

    public WeeklyTSAdapter(ArrayList<TaskModel> eventsModels, boolean issubmitted, Context context_timesheets, InterfaceListener interfaceListener, String date, String matter_type) {
        this.eventsList = eventsModels;
        this.issubmitted = issubmitted;
        this.context_timesheets = context_timesheets;
        this.eventListener = interfaceListener;
        this.date = date;
        this.matter_type = matter_type;
    }

    @NonNull
    @Override
    public WeeklyTSAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weekly_timesheets, parent, false);
        return new WeeklyTSAdapter.MyViewHolder(view);
    }

    public interface InterfaceListener {

        void DeleteEvent_Timesheet(TaskModel taskModel);

        void EditEvent_Timesheet(TaskModel eventsModels, String date, String matter_type);

    }

    @Override
    public void onBindViewHolder(@NonNull WeeklyTSAdapter.MyViewHolder holder, int position) {
        TaskModel eventsModel = eventsList.get(position);
        if (!(eventsModel.getTaskid() == null)) {
            holder.tv_matter_task_name.setText(eventsModel.getTask_name());
            holder.tv_matter_name.setText(eventsModel.getTask_matter_name());
            String hour = eventsModel.getHours() + " Hour";
            String minutes = eventsModel.getMinutes() + " Min";
            holder.tv_matter_hours.setText(hour);
            holder.tv_matter_minutes.setText(minutes);
            holder.tv_matter_billable.setText(AndroidUtils.CapitalizeFirstLetter(eventsModel.getTask_billing()));
        }
        if (holder.tv_matter_name.getText().toString().isEmpty()) {
            holder.cardView.setVisibility(View.GONE);
            holder.edit_del_timesheets.setVisibility(View.GONE);
        } else {
            if (eventsModel.getIs_editable()) {
                holder.edit_timesheets.setVisibility(View.VISIBLE);
            } else {
                holder.edit_timesheets.setVisibility(View.GONE);
            }
            if (!issubmitted)
                holder.edit_del_timesheets.setVisibility(View.VISIBLE);
            else
                holder.edit_del_timesheets.setVisibility(View.GONE);

            holder.cardView.setVisibility(View.VISIBLE);
        }
        holder.edit_timesheets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                AndroidUtils.showToast("Edit_timesheet", v.getContext());
                Log.d("Edit_timesheets", "Edit_timesheets");
                eventListener.EditEvent_Timesheet(eventsModel, date, matter_type);
            }
        });
        holder.delete_timesheets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                AndroidUtils.showToast("Delete_timesheet", v.getContext()

                eventListener.DeleteEvent_Timesheet(eventsModel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_matter_name, tv_matter_hours, tv_matter_minutes, tv_matter_billable, tv_matter_task_name, tv_total_hours, total_hours_id;
        LinearLayout ll_total_hours, edit_del_timesheets;
        CardView cardView;
        ImageView edit_timesheets, delete_timesheets;
        LinearLayoutCompat week_linear;

        public MyViewHolder(@NonNull View itemView) {

            super(itemView);
            week_linear = itemView.findViewById(R.id.week_linear);
            week_linear.setPadding(30, 10, 10, 10);
            edit_timesheets = itemView.findViewById(R.id.edit_timesheets);
            delete_timesheets = itemView.findViewById(R.id.delete_timesheets);
            edit_del_timesheets = itemView.findViewById(R.id.edit_del_timesheets);
            tv_matter_billable = itemView.findViewById(R.id.tv_matter_billable);
            tv_matter_billable.setAutoSizeTextTypeUniformWithConfiguration(1, 15, 1, TypedValue.COMPLEX_UNIT_SP);
            tv_matter_hours = itemView.findViewById(R.id.tv_matter_hours);
            tv_matter_minutes = itemView.findViewById(R.id.tv_matter_minutes);
            tv_matter_name = itemView.findViewById(R.id.tv_matter_name);
            tv_matter_task_name = itemView.findViewById(R.id.tv_matter_task_name);
            tv_matter_task_name.setAutoSizeTextTypeUniformWithConfiguration(1, 15, 1, TypedValue.COMPLEX_UNIT_SP);
            tv_total_hours = itemView.findViewById(R.id.tv_total_hours);
            ll_total_hours = itemView.findViewById(R.id.ll_total_hours);
            ll_total_hours.setVisibility(View.GONE);
            total_hours_id = itemView.findViewById(R.id.total_hours_id);
//            tv_matter_name.setTextColor(Color.BLACK);
            total_hours_id.setText(R.string.total_hours);

            cardView = itemView.findViewById(R.id.cardview_week);
//            CardView.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            params.setMargins(10, 10, 0, 10);
//            cardView.setLayoutParams(params);
            //Green count Color
            tv_total_hours.setTextColor(context_timesheets.getResources().getColor(R.color.black));
            tv_total_hours.setTextSize(DynamicUtils.twenty);
            //Orange Color
            tv_matter_hours.setTextColor(context_timesheets.getResources().getColor(R.color.black));
            tv_matter_minutes.setTextColor(context_timesheets.getResources().getColor(R.color.black));
            //Blue Text Color
            tv_matter_task_name.setTextColor(context_timesheets.getResources().getColor(R.color.Primary_new));
            tv_matter_billable.setTextColor(context_timesheets.getResources().getColor(R.color.Primary_new));
            total_hours_id.setTextColor(context_timesheets.getResources().getColor(R.color.Primary_new));


        }
    }
}
