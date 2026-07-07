package com.digicoffer.lauditor.Relationships.Adapter;

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

import com.digicoffer.lauditor.Relationships.MemberModel;
import com.digicoffer.lauditor.R;

import java.util.ArrayList;

public class MemberSelectionAdapter extends RecyclerView.Adapter<MemberSelectionAdapter.ViewHolder> implements Filterable {

    private ArrayList<MemberModel> originalList;
    private ArrayList<MemberModel> filteredList;
    private final OnMemberSelectionChanged listener;

    public MemberSelectionAdapter(ArrayList<MemberModel> memberList, OnMemberSelectionChanged listener) {
        this.originalList = new ArrayList<>(memberList);
        this.filteredList = new ArrayList<>(memberList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemberSelectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.select_team_members, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberSelectionAdapter.ViewHolder holder, int position) {
        MemberModel member = filteredList.get(position);

        holder.tvMemberName.setText(member.getName());
        holder.cbMember.setChecked(member.isChecked());
        holder.cbMember.setOnClickListener(v -> {
            member.setChecked(holder.cbMember.isChecked());
            notifySelectionChanged(true); // ✅ Pass user interaction
        });

        holder.selectLayout.setOnClickListener(v -> {
            boolean isChecked = !holder.cbMember.isChecked();
            holder.cbMember.setChecked(isChecked);
            member.setChecked(isChecked);
            notifySelectionChanged(true); // ✅ Pass user interaction
        });

    }

    private void notifySelectionChanged(boolean userInteracted) {
        ArrayList<MemberModel> selected = new ArrayList<>();
        for (MemberModel member : originalList) {
            if (member.isChecked()) {
                selected.add(member);
            }
        }
        listener.onSelectionChanged(selected, userInteracted);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void selectOrDeselectAll(boolean isChecked) {
        for (MemberModel member : originalList) {
            member.setChecked(isChecked);
        }
        notifyDataSetChanged();
        notifySelectionChanged(false);
    }

    public ArrayList<MemberModel> getSelectedMembers() {
        ArrayList<MemberModel> selected = new ArrayList<>();
        for (MemberModel member : originalList) {
            if (member.isChecked()) {
                selected.add(member);
            }
        }
        return selected;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String search = constraint.toString().trim().toLowerCase();
                if (search.isEmpty()) {
                    filteredList = new ArrayList<>(originalList);
                } else {
                    ArrayList<MemberModel> result = new ArrayList<>();
                    for (MemberModel member : originalList) {
                        if (member.getName() != null && member.getName().toLowerCase().contains(search)) {
                            result.add(member);
                        }
                    }
                    filteredList = result;
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (ArrayList<MemberModel>) results.values;
                notifyDataSetChanged();
            }
        };
    }
    public interface OnMemberSelectionChanged {
        void onSelectionChanged(ArrayList<MemberModel> selectedMembers, boolean userInteracted);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbMember;
        TextView tvMemberName;
        LinearLayout selectLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbMember = itemView.findViewById(R.id.chk_selected);
            tvMemberName = itemView.findViewById(R.id.tv_tm_name);
            selectLayout = itemView.findViewById(R.id.select_tm_layout);
        }
    }
}

