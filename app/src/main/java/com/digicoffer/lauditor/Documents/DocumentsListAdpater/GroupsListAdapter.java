package com.digicoffer.lauditor.Documents.DocumentsListAdpater;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Documents.Documents;
import com.digicoffer.lauditor.Documents.Models.DocumentsModel;
import com.digicoffer.lauditor.R;

import java.util.ArrayList;

public class GroupsListAdapter extends RecyclerView.Adapter<GroupsListAdapter.Viewholder> {
    private ArrayList<DocumentsModel> sharedList = new ArrayList<>();
    private ArrayList<DocumentsModel> list_item = new ArrayList<>();
    private OnCheckedChangeListener listener;

    public GroupsListAdapter(ArrayList<DocumentsModel> sharedList, Documents documents, OnCheckedChangeListener listener) {
        this.sharedList = sharedList;
        this.list_item = sharedList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_team_members, parent, false);
        return new Viewholder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        DocumentsModel groupsModel = sharedList.get(position);
        holder.cb_documents.setChecked(sharedList.get(position).isGroupChecked());
        holder.cb_documents.setTag(position);
        holder.tv_tm_name.setText(groupsModel.getGroup_name());

        // Setting click listener for the checkbox
        holder.cb_documents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer pos = (Integer) holder.cb_documents.getTag();
                if (sharedList.get(pos).isGroupChecked()) {
                    sharedList.get(pos).setGroupChecked(false);
                    holder.cb_documents.setChecked(false);
                } else {
                    sharedList.get(pos).setGroupChecked(true);
                    holder.cb_documents.setChecked(true);
                }


                if (listener != null) {
                    listener.onCheckedChanged(sharedList.get(pos));
                }
            }
        });
    }

    public ArrayList<DocumentsModel> getList_item() {
        return sharedList;
    }

    @Override
    public int getItemCount() {
        return sharedList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        private TextView tv_tm_name;
        private View list_line;
        private LinearLayout select_tm_layout;
        private CheckBox cb_documents;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            cb_documents = itemView.findViewById(R.id.chk_selected);
            tv_tm_name = itemView.findViewById(R.id.tv_tm_name);
            select_tm_layout = itemView.findViewById(R.id.select_tm_layout);
            list_line = itemView.findViewById(R.id.list_line);
            list_line.setVisibility(View.VISIBLE);
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            params.setMargins(0, 0, 0, 0);
//            select_tm_layout.setLayoutParams(params);
            select_tm_layout.setBackgroundResource(R.drawable.background_transparent);
        }
    }

    // Interface for checkbox change listener
    public interface OnCheckedChangeListener {
        void onCheckedChanged(DocumentsModel documentsModel);
    }
}
