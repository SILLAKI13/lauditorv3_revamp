package com.digicoffer.lauditor.TimeSheets.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.TimeSheets.Models.TMModel;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;

import java.util.ArrayList;

public class TeamMembersTSAdapter extends RecyclerView.Adapter<TeamMembersTSAdapter.MyviewHolder> implements Filterable {
    ArrayList<TMModel> teamList = new ArrayList<>();
    ArrayList<TMModel> itemsList = new ArrayList<>();
    String status;
    Context context;

    public TeamMembersTSAdapter(Context context, ArrayList<TMModel> teamList, String sStatus) {
        this.teamList = teamList;
        this.itemsList = teamList;
        this.status = sStatus;
        this.context = context;
    }

    @NonNull
    @Override
    public TeamMembersTSAdapter.MyviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weekly_timesheets, parent, false);
        return new TeamMembersTSAdapter.MyviewHolder(view);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    teamList = itemsList;
                } else {
                    ArrayList<TMModel> filteredList = new ArrayList<>();
                    for (TMModel row : itemsList) {
//                            if (row.isChecked()){
//                                row.setChecked(false);
//                            }else
//                            {
//                                row.setChecked(true  );
//                            }
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (AndroidUtils.isNull(row.getName()).toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    teamList = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.count = teamList.size();
                filterResults.values = teamList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                teamList = (ArrayList<TMModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull TeamMembersTSAdapter.MyviewHolder holder, int position) {
        TMModel tmModel = teamList.get(position);
        holder.ll_total_hours.setVisibility(View.VISIBLE);
        holder.tv_matter_name.setText(tmModel.getName());
        holder.tv_matter_billable.setText(R.string.billable);
        holder.tv_matter_task_name.setText(R.string.non_billable);
        holder.tv_matter_hours.setText(tmModel.getTb() + " Hour");
        holder.tv_matter_minutes.setText(tmModel.getTnb() + " Hour");
        holder.tv_total_hours.setText(tmModel.getTotal() + " Hours");

    }

    @Override
    public int getItemCount() {
        return teamList.size();
    }

    public class MyviewHolder extends RecyclerView.ViewHolder {
        private TextView tv_matter_name, tv_matter_hours, tv_matter_minutes, tv_matter_billable, tv_matter_task_name, tv_total_hours, total_hours_id;
        LinearLayout ll_total_hours;
        CardView cardView;
        LinearLayoutCompat week_linear;

        public MyviewHolder(@NonNull View itemView) {

            super(itemView);
            week_linear = itemView.findViewById(R.id.week_linear);
            week_linear.setPadding(30, 10, 10, 10);
            tv_matter_billable = itemView.findViewById(R.id.tv_matter_billable);
            tv_matter_hours = itemView.findViewById(R.id.tv_matter_hours);
            tv_matter_minutes = itemView.findViewById(R.id.tv_matter_minutes);
            tv_matter_name = itemView.findViewById(R.id.tv_matter_name);
            tv_matter_task_name = itemView.findViewById(R.id.tv_matter_task_name);
            tv_total_hours = itemView.findViewById(R.id.tv_total_hours);
            ll_total_hours = itemView.findViewById(R.id.ll_total_hours);
            total_hours_id = itemView.findViewById(R.id.total_hours_id);
            tv_matter_name.setTextColor(context.getResources().getColor(R.color.blue));
            tv_matter_name.setTextSize(DynamicUtils.fifteen);
            total_hours_id.setText(R.string.total_hours);

            cardView = itemView.findViewById(R.id.cardview_week);
            CardView.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 10, 0, 10);
            cardView.setLayoutParams(params);
            //Green count Color

            tv_total_hours.setTextColor(context.getResources().getColor(R.color.black));
            tv_total_hours.setTextSize(DynamicUtils.twenty);
            //Orange Color
            tv_matter_hours.setTextColor(context.getResources().getColor(R.color.black));
            tv_matter_minutes.setTextColor(context.getResources().getColor(R.color.black));
            //Blue Text Color
            tv_matter_task_name.setTextColor(context.getResources().getColor(R.color.blue));
            tv_matter_task_name.setTextSize(17);
            tv_matter_billable.setTextColor(context.getResources().getColor(R.color.blue));
            tv_matter_billable.setTextSize(17);
            total_hours_id.setTextColor(context.getResources().getColor(R.color.blue));
            total_hours_id.setTextSize(17);
        }
    }
}
