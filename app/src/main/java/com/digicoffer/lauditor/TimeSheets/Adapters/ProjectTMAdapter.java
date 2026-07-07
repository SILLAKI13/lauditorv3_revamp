package com.digicoffer.lauditor.TimeSheets.Adapters;

import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Filter;
import android.widget.Filterable;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.TimeSheets.Models.ProjectTMModel;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;

import java.util.ArrayList;

public class ProjectTMAdapter extends RecyclerView.Adapter<ProjectTMAdapter.MyViewHolder> implements Filterable {

    private ArrayList<ProjectTMModel> projectTmList = new ArrayList<>();
    private ArrayList<ProjectTMModel> projectTmListFull = new ArrayList<>(); // Full backup list
    Context context;

    public ProjectTMAdapter(Context context1, ArrayList<ProjectTMModel> projectTmList) {
        this.context = context1;
        this.projectTmList = new ArrayList<>(projectTmList);
        this.projectTmListFull = new ArrayList<>(projectTmList);
    }

    @NonNull
    @Override
    public ProjectTMAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weekly_timesheets, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectTMAdapter.MyViewHolder holder, int position) {
        ProjectTMModel projectTMModel = projectTmList.get(position);
        holder.ll_total_hours.setVisibility(VISIBLE);
        holder.tv_matter_name.setText(projectTMModel.getName());
        holder.tv_matter_billable.setText(R.string.billable);
        holder.tv_matter_task_name.setText(R.string.non_billable);

        String billableHour = projectTMModel.getBillableHours() + " Hour";
        String nonBillableHour = projectTMModel.getNonBillablehours() + " Hour";
        String totalHours = projectTMModel.getTotal() + " Hours";

        holder.tv_matter_hours.setText(billableHour);
        holder.tv_matter_minutes.setText(nonBillableHour);
        holder.tv_total_hours.setText(totalHours);
    }

    @Override
    public int getItemCount() {
        return projectTmList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<ProjectTMModel> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(projectTmListFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (ProjectTMModel model : projectTmListFull) {
                        if (model.getName() != null && model.getName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(model);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                projectTmList.clear();
                projectTmList.addAll((ArrayList<ProjectTMModel>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_matter_name, tv_matter_hours, tv_matter_minutes,
                tv_matter_billable, tv_matter_task_name, tv_total_hours, total_hours_id, tv_tm_label;
        LinearLayout ll_total_hours;
        LinearLayoutCompat llc_week;
        View week_line;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_matter_billable = itemView.findViewById(R.id.tv_matter_billable);
            llc_week = itemView.findViewById(R.id.llc_week);
            tv_matter_hours = itemView.findViewById(R.id.tv_matter_hours);
            tv_matter_minutes = itemView.findViewById(R.id.tv_matter_minutes);
            tv_matter_name = itemView.findViewById(R.id.tv_matter_name);
            tv_matter_task_name = itemView.findViewById(R.id.tv_matter_task_name);
            tv_total_hours = itemView.findViewById(R.id.tv_total_hours);
            ll_total_hours = itemView.findViewById(R.id.ll_total_hours);
            total_hours_id = itemView.findViewById(R.id.total_hours_id);
            week_line = itemView.findViewById(R.id.week_line);
            tv_tm_label = itemView.findViewById(R.id.tv_tm_label);
            tv_tm_label.setVisibility(VISIBLE);
            tv_tm_label.setText(R.string.team_members__);
            // Styling
            tv_matter_name.setTextColor(Color.BLACK);
            tv_total_hours.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
            tv_total_hours.setTextSize(DynamicUtils.twentyFive);
            tv_matter_hours.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
            tv_matter_minutes.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
            tv_matter_task_name.setTextColor(itemView.getContext().getResources().getColor(R.color.Primary_new));
            tv_matter_task_name.setTextSize(DynamicUtils.twenty);
            tv_matter_billable.setTextColor(itemView.getContext().getResources().getColor(R.color.Primary_new));
            tv_matter_billable.setTextSize(DynamicUtils.twenty);
            total_hours_id.setTextColor(itemView.getContext().getResources().getColor(R.color.Primary_new));
            total_hours_id.setTextSize(DynamicUtils.twenty);
            total_hours_id.setText(R.string.total_hours);

            week_line.setVisibility(VISIBLE);

            // Set margins programmatically
            LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(
                    LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(40, 20, 20, 20); // Adjust margins
            llc_week.setLayoutParams(layoutParams);
        }
    }
}
