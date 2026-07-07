package com.digicoffer.lauditor.Members.Adapters;

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

import com.digicoffer.lauditor.Relationships.Adapter.RelationshipsAdapter;
import com.digicoffer.lauditor.Relationships.ClientRelationship;
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel;
import com.digicoffer.lauditor.Matter.ViewModels.ViewMatter;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import java.util.ArrayList;
import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> implements Filterable {
    ArrayList<ViewGroupModel> groupsList = new ArrayList<>();
    ArrayList<ViewGroupModel> list_item = new ArrayList<>();
    ArrayList<ViewGroupModel> filtered_List = new ArrayList<>();
    RelationshipsAdapter relationshipsAdapter;
    ViewMatter viewMatter;
    ClientRelationship clientRelationship;

    public GroupsAdapter(ArrayList<ViewGroupModel> groupsList) {
        this.groupsList = groupsList;
        this.list_item = groupsList;
        filtered_List = groupsList;
    }

    public GroupsAdapter(ArrayList<ViewGroupModel> groupsList, ViewMatter viewMatter) {
        this.groupsList = groupsList;
        this.list_item = groupsList;
        filtered_List = groupsList;
        this.viewMatter = viewMatter;
    }

    public GroupsAdapter(ArrayList<ViewGroupModel> groupsList, RelationshipsAdapter relationshipsAdapter, ClientRelationship clientRelationship) {
        this.groupsList = groupsList;
        this.list_item = groupsList;
        filtered_List = groupsList;
        this.relationshipsAdapter = relationshipsAdapter;
        this.clientRelationship = clientRelationship;
    }

    @NonNull
    @Override
    public GroupsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_team_members, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupsAdapter.ViewHolder holder, int position) {
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.width = RecyclerView.LayoutParams.MATCH_PARENT;
        holder.itemView.setLayoutParams(layoutParams);
        groupsList = list_item;
        ViewGroupModel groupModel = filtered_List.get(position);
        holder.cb_team_members.setChecked(filtered_List.get(position).isChecked());
        holder.cb_team_members.setTag(position);

//            holder.cb_team_members.isChecked() = itemsArrayList.get(position).isChecked();
        holder.cb_team_members.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                ViewGroupModel group = filtered_List.get(position);
                boolean isChecked = group.isChecked();

                // Check if relationshipsAdapter is not null
                if (relationshipsAdapter != null) {
                    // If the checkbox is being unchecked

                    // Update the adapter with the new list of selected groups
                    if (Constants.isAlterPopup) {
                        group.setChecked(!isChecked);
                        relationshipsAdapter.loadNewGroups(filtered_List);
                    } else {
                        if (isChecked) {
                            // Check if the group can be deleted
                            if (!group.isCan_delete()) {
                                // If the group cannot be deleted, prevent unchecking
                                AndroidUtils.showAlert("Matters are associated with this Group. So you cannot delete this group", clientRelationship.getActivity());
                                // Keep the checkbox checked
                                holder.cb_team_members.setChecked(true);// Exit since we don't want to proceed with unchecked logic
                            } else {
                                if (group.isCan_assign_docs()) {
                                    group.setChecked(true);
                                    relationshipsAdapter.checkRemoveGroups(group.getId(), group.getName(), groupsList);
                                    notifyItemChanged(position);
                                } else {
                                    // Proceed to uncheck and remove the group if it can be deleted
                                    group.setChecked(false);
                                    groupsList.get(position).setChecked(false);
                                    relationshipsAdapter.load_selected_groups(groupsList);
                                }
                            }
                        } else {
                            // If the checkbox is checked, allow the usual behavior
                            group.setChecked(true);
                            relationshipsAdapter.load_selected_groups(groupsList);
                        }
                    }
                } else if (viewMatter != null) {
                    // If the checkbox is being unchecked

                    // Update the adapter with the new list of selected groups
                    if (Constants.isAlterPopup) {
                        group.setChecked(!isChecked);
                        viewMatter.loadNewGroups(groupsList);
                    } else {
                        if (isChecked) {
                            // Check if the group can be deleted
//                            if (!group.isCan_delete()) {
//                                // If the group cannot be deleted, prevent unchecking
//                                AndroidUtils.showAlert("Matters are associated with this Group. So you cannot delete this group", viewMatter.getActivity());
//                                // Keep the checkbox checked
//                                holder.cb_team_members.setChecked(true);// Exit since we don't want to proceed with unchecked logic
//                            } else {
//                                if (group.isCan_assign_docs()) {
                            if (!group.isCan_delete()) {
                                group.setChecked(true);
                                viewMatter.checkRemoveGroups(group.getGroup_id(), group.getName(), groupsList);
                                notifyItemChanged(position);
                            } else {
                                group.setChecked(false);
                                viewMatter.load_selected_groups(groupsList);
                            }
//                                notifyItemChanged(position);
//                                } else {
//                                    // Proceed to uncheck and remove the group if it can be deleted
//                                    group.setChecked(false);
//                                    filtered_List.get(position).setChecked(false);
//                                }
//                            }
                        } else {
                            // If the checkbox is checked, allow the usual behavior
                            group.setChecked(true);
                            viewMatter.load_selected_groups(groupsList);
                        }
                    }
                    //..
//                    {
//                        group.setChecked(true);
//                        if (isChecked) {
//                            viewMatter.checkRemoveGroups(group.getGroup_id(), group.getName(), groupsList);
//                            notifyItemChanged(position);
//                        } else {
////                            group.setChecked(false);
//                            // If the checkbox is checked, allow the usual behavior
//                        }
//                    }
                } else {
                    // If relationshipsAdapter is null, just toggle the checkbox as usual
                    group.setChecked(!isChecked);
                    holder.cb_team_members.setChecked(!isChecked);
                }
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
    public void resetGroupTemporaryStates() {
        for (ViewGroupModel group : groupsList) {
            // Optional: only reset groups that were unchecked before popup
            // e.g., if (!group.isChecked()) { ... }
            group.setChecked(group.isChecked()); // Keep original checked state
        }
        notifyDataSetChanged();
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

    @Override
    public int getItemCount() {
        return filtered_List.size();
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                List<ViewGroupModel> filteredList;

                if (charString.isEmpty()) {
                    filteredList = new ArrayList<>(list_item);
                } else {
                    filteredList = new ArrayList<>();
                    for (ViewGroupModel row : list_item) {
                        String name = row.getName();
                        if (name != null && name.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.count = filteredList.size();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                groupsList = (ArrayList<ViewGroupModel>) filterResults.values;
                filtered_List = groupsList;
                notifyDataSetChanged();
            }
        };
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
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
