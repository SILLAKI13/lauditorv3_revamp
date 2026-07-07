package com.digicoffer.lauditor.AuditTrails.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.AuditTrails.Model.AuditsModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AuditsAdapter extends RecyclerView.Adapter<AuditsAdapter.MyViewHolder> implements Filterable {
    ArrayList<AuditsModel> filtered_list = new ArrayList<>();
    ArrayList<AuditsModel> itemList = new ArrayList<>();
    private List<AuditsModel> auditsList;
    int item_position = 0;

    public AuditsAdapter(ArrayList<AuditsModel> auditsList) {
        this.filtered_list = auditsList;
        this.itemList = auditsList;
    }


    @NonNull
    @Override
    public AuditsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.audit_recyclerview, parent, false);
        return new AuditsAdapter.MyViewHolder(itemView);
    }

    public void updateData(List<AuditsModel> newData) {
        this.auditsList = newData;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull AuditsAdapter.MyViewHolder holder, int position) {
//            item_position = position;
        AuditsModel auditsModel = filtered_list.get(position);
        switch (auditsModel.getName()) {
            case "AUTH":
                holder.tv_category_name.setText(R.string.authentication_c);
                loadHiddenData(holder, auditsModel);
                break;
            case "TEAM MEMBER":
                holder.tv_category_name.setText(R.string.team_member_c);
                loadHiddenData(holder, auditsModel);
                break;
            case "RELATIONSHIP":
                holder.tv_category_name.setText(R.string.relationship_c);
                loadHiddenData(holder, auditsModel);
                break;
            case "RELATIONSHIP INVITE":
                holder.tv_category_name.setText(R.string.relationship_invite_c);
                loadHiddenData(holder, auditsModel);
                break;
            case "SHARE":
                holder.tv_category_name.setText(R.string.share_c);
                loadHiddenData(holder, auditsModel);
                break;
            case "DOCUMENT":
                holder.tv_category_name.setText(R.string.document_c);
                loadHiddenData(holder, auditsModel);
                break;
            case "LEGAL MATTER":
                holder.tv_category_name.setText(R.string.legal_matter_c);
                loadHiddenData(holder, auditsModel);
                break;
            case "GENERAL MATTER":
                holder.tv_category_name.setText(R.string.general_matter_c);
                loadHiddenData(holder, auditsModel);
                break;
            case "GROUPS":
                holder.tv_category_name.setText(R.string.group_c);
                loadHiddenData(holder, auditsModel);
                break;
            default:
                holder.tv_category_name.setText(auditsModel.getName());
                loadHiddenData(holder, auditsModel);
                break;
        }
//            holder.tv_category_name.setText(auditsModel.getName());
//            loadHiddenData(holder,auditsModel);
//            else if(){
//                removeItem(position);
//            }
    }

    private void loadHiddenData(MyViewHolder holder, AuditsModel auditsModel) {
        holder.tv_audit_matter.setText(auditsModel.getMessage());
        holder.tv_timestamp.setText(auditsModel.getTimestamp());
//        notifyItemChanged(item_position);
//        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        filtered_list.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString().toLowerCase().trim(); // Convert to lowercase and trim
                if (charString.isEmpty()) {
                    filtered_list = itemList;
                } else {
                    ArrayList<AuditsModel> filteredList = new ArrayList<>();
                    for (AuditsModel row : itemList) {
                        // Use a Pattern with case-insensitive flag for matching
                        Pattern pattern = Pattern.compile(Pattern.quote(charString), Pattern.CASE_INSENSITIVE);

                        if (pattern.matcher(AndroidUtils.isNull(row.getMessage()).toLowerCase()).find()
                                || pattern.matcher(AndroidUtils.isNull(row.getTimestamp()).toLowerCase()).find()
                                || pattern.matcher(AndroidUtils.isNull(row.getName()).toLowerCase()).find()) {
                            filteredList.add(row);
                        }
                    }
                    filtered_list = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.count = filtered_list.size();
                filterResults.values = filtered_list;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filtered_list = (ArrayList<AuditsModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public void clearData() {
        filtered_list.clear();
        notifyDataSetChanged();
    }

    public void setData(ArrayList<AuditsModel> newData) {
        filtered_list = newData;
        notifyDataSetChanged(); // Notify the adapter that the dataset has changed
    }

    @Override
    public int getItemCount() {
        return filtered_list.size();
//        notifyDataSetChanged();
//        notifyItemChanged(item_position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_category_name, tv_timestamp, tv_audit_matter;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_category_name = itemView.findViewById(R.id.tv_category_name);
            tv_timestamp = itemView.findViewById(R.id.tv_timestamp);
            tv_audit_matter = itemView.findViewById(R.id.tv_audit_matter);
        }
    }
}
