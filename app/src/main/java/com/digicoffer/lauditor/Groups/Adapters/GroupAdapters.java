package com.digicoffer.lauditor.Groups.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Groups.Models.GroupModel;
import com.digicoffer.lauditor.Groups.Groups;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.ItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupAdapters extends RecyclerView.Adapter<GroupAdapters.ViewHolder> implements Filterable {
    private ArrayList<GroupModel> itemsArrayList;
    private ArrayList<GroupModel> filtered_List;
    private final ArrayList<GroupModel> list_item;
    private final ItemClickListener itemClickListener;
    private final String mTag;
    String selectedGroupId = "";
    private final Groups group;
    Button btn_submit;

    public GroupAdapters(ArrayList<GroupModel> itemsArrayList, String Tag, ItemClickListener itemClickListener, Groups context) {
        this.itemsArrayList = itemsArrayList;
        this.list_item = new ArrayList<>(itemsArrayList);
        this.filtered_List = itemsArrayList;
        this.group = context;
        this.mTag = Tag;
        this.itemClickListener = itemClickListener;
    }

    public GroupAdapters(ArrayList<GroupModel> itemsArrayList, String Tag, ItemClickListener itemClickListener, Groups context, Button btn_submit) {
        this.itemsArrayList = itemsArrayList;
        this.list_item = new ArrayList<>(itemsArrayList);
        this.filtered_List = itemsArrayList;
        this.group = context;
        this.mTag = Tag;
        this.itemClickListener = itemClickListener;
        this.btn_submit = btn_submit;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (Objects.equals(mTag, "TM")) {
            View itemView = inflater.inflate(R.layout.select_team_members, parent, false);
            return new ViewHolder(itemView);
        } else {
            View itemView = inflater.inflate(R.layout.assign_group_head, parent, false);
            return new ViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final GroupModel groupModel = filtered_List.get(position);
        if (Objects.equals(mTag, "TM")) {
            holder.cb_team_members.setChecked(groupModel.isChecked());
            holder.cb_team_members.setTag(position);
            holder.cb_team_members.setEnabled(true);
            check_allselected();
            holder.cb_team_members.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = holder.getAdapterPosition();
                    boolean isChecked = filtered_List.get(position).isChecked();
                    filtered_List.get(position).setChecked(!isChecked);
                    check_allselected();
                    group.selectedtmlist(filtered_List);
                }
            });
            holder.tv_tm_name.setText(groupModel.getName());
        } else {
//            if (groupModel.isGroupHeadChecked()) {
//                selectedGroupId = groupModel.getId();
//            }
            holder.rb_tv_name.setText(groupModel.getName());
            holder.rb_group_head.setOnCheckedChangeListener(null); // Remove any previous listener
            holder.rb_group_head.setChecked(groupModel.getId().equals(selectedGroupId));
            holder.rb_group_head.setTag(groupModel.getId());

            holder.rb_group_head.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        selectedGroupId = groupModel.getId();
//                        groupModel.setGroupHeadChecked(true);
                        itemClickListener.onClick(groupModel.getId());
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }

    public ArrayList<GroupModel> getList_item() {
        return itemsArrayList;
    }

    @Override
    public int getItemCount() {
        return filtered_List.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                List<GroupModel> filteredList;

                if (charString.isEmpty()) {
                    filteredList = new ArrayList<>(list_item);
                } else {
                    filteredList = new ArrayList<>();
                    for (GroupModel row : list_item) {
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
                itemsArrayList = (ArrayList<GroupModel>) filterResults.values;
                filtered_List = itemsArrayList;
                if (!itemsArrayList.isEmpty())
                    check_allselected();
                notifyDataSetChanged();
            }
        };
    }

    public boolean selectOrDeselectAll(boolean isChecked) {
        for (GroupModel groupModel : filtered_List) {
            groupModel.setChecked(isChecked);
        }
        group.selectedtmlist(filtered_List);
        notifyDataSetChanged();
        return isChecked;
    }

    public void check_allselected() {
        boolean allSelected = true;
        for (GroupModel groupModel : itemsArrayList) {
            if (!groupModel.isChecked()) {
                allSelected = false;
                break;
            }
        }
        group.check_select_all(allSelected);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox cb_team_members;
        private final TextView tv_tm_name;
        private final TextView rb_tv_name;
        private final CheckBox rb_group_head;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cb_team_members = itemView.findViewById(R.id.chk_selected);
            tv_tm_name = itemView.findViewById(R.id.tv_tm_name);
            rb_tv_name = itemView.findViewById(R.id.rb_tv_name);
            rb_group_head = itemView.findViewById(R.id.rb_selected);
        }
    }
}
