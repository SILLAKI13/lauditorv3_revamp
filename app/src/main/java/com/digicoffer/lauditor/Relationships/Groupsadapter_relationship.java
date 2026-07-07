package com.digicoffer.lauditor.Relationships;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Groups.Models.ViewGroupModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;

import java.util.ArrayList;

public class Groupsadapter_relationship extends RecyclerView.Adapter<Groupsadapter_relationship.ViewHolder> implements Filterable {
    ArrayList<ViewGroupModel> groupsList = new ArrayList<>();
    ArrayList<ViewGroupModel> list_item = new ArrayList<>();
    ArrayList<ViewGroupModel> filtered_list = new ArrayList<>();
    ClientRelationship clientRelationship;

    public Groupsadapter_relationship(ArrayList<ViewGroupModel> groupsList, ClientRelationship clientRelationship) {
        this.groupsList = groupsList;
        this.list_item = groupsList;
        filtered_list = groupsList;
        this.clientRelationship = clientRelationship;
    }

    @NonNull
    @Override
    public Groupsadapter_relationship.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_team_members, parent, false);
        return new Groupsadapter_relationship.ViewHolder(itemView);
    }


    public void onBindViewHolder(@NonNull Groupsadapter_relationship.ViewHolder holder, int position) {
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.width = RecyclerView.LayoutParams.MATCH_PARENT;
        holder.itemView.setLayoutParams(layoutParams);
        ViewGroupModel groupModel = filtered_list.get(position);
        groupsList = list_item;
        holder.cb_team_members.setChecked(filtered_list.get(position).isChecked());
        holder.cb_team_members.setTag(position);

//            holder.cb_team_members.isChecked() = itemsArrayList.get(position).isChecked();
//        holder.cb_team_members.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Integer pos = (Integer) holder.cb_team_members.getTag();
//                if (filtered_list.get(pos).isChecked()) {
//                    filtered_list.get(pos).setChecked(false);
////                        itemsArrayList.add(itemsArrayList.get(pos));
//                } else {
//                    filtered_list.get(pos).setChecked(true);
////                        itemsArrayList.remove(itemsArrayList.get(pos));
//                }
//                if (clientRelationship != null) {
//                    clientRelationship.load_selected_groups(filtered_list);
//                }
//            }
//        });
        holder.cb_team_members.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer pos = (Integer) holder.cb_team_members.getTag();

                boolean newState = !filtered_list.get(pos).isChecked();
                filtered_list.get(pos).setChecked(newState);

                if (clientRelationship != null) {
                    clientRelationship.load_selected_groups(filtered_list);
                }

                // ↓↓↓ ADD THIS ↓↓↓
                clientRelationship.updateSelectAllState(areAllItemsSelected());
            }
        });

//            holder.cb_team_members.setChecked(true);
        if (groupModel.getName() != null) {
            holder.tv_tm_name.setText(groupModel.getName());
        } else {
            holder.tv_tm_name.setText(groupModel.getGroup_name());
        }
        holder.select_tm_layout.setLayoutParams(layoutParams);
    }

    public void selectOrDeselectAll(boolean isChecked) {
        for (int i = 0; i < list_item.size(); i++) {
//            if (list_item.get(i).isIsenabled())
//            if (list_item.get(i).isIsenabled()) {
            list_item.get(i).setChecked(isChecked);
//            }else {
//                list_item.get(i).setChecked(false);
//            }
        }
        notifyDataSetChanged();
    }

    public ArrayList<ViewGroupModel> getList_item() {
        return groupsList;
    }
    public boolean areAllItemsSelected() {
        for (ViewGroupModel item : filtered_list) {
            if (!item.isChecked()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getItemCount() {
        return filtered_list.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    groupsList = list_item;
                } else {
                    ArrayList<ViewGroupModel> filteredList = new ArrayList<>();
                    for (ViewGroupModel row : list_item) {
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
                    groupsList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.count = groupsList.size();
                filterResults.values = groupsList;
                return filterResults;
            }
            public boolean areAllItemsSelected() {
                for (ViewGroupModel item : filtered_list) {
                    if (!item.isChecked()) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                groupsList = (ArrayList<ViewGroupModel>) filterResults.values;
                filtered_list = groupsList;
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cb_team_members;
        TextView tv_tm_name;

        LinearLayout select_tm_layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cb_team_members = itemView.findViewById(R.id.chk_selected);
            tv_tm_name = itemView.findViewById(R.id.tv_tm_name);
            select_tm_layout = itemView.findViewById(R.id.select_tm_layout);
        }
    }
}
