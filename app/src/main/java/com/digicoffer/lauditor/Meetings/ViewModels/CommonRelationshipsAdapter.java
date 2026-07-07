package com.digicoffer.lauditor.Meetings.ViewModels;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Meetings.Models.DocumentsDo;
import com.digicoffer.lauditor.Meetings.Models.RelationshipsDO;
import com.digicoffer.lauditor.Meetings.Models.TeamDo;
import com.digicoffer.lauditor.R;

import java.util.ArrayList;
import java.util.Objects;

public class CommonRelationshipsAdapter extends RecyclerView.Adapter<CommonRelationshipsAdapter.Viewholder> {
    ArrayList<TeamDo> sharedList = new ArrayList<>();
    ArrayList<TeamDo> list_item = new ArrayList<>();
    ArrayList<RelationshipsDO> individual_list = new ArrayList<>();
    ArrayList<TeamDo> tmList = new ArrayList<>();
    ArrayList<RelationshipsDO> entity_client_list;
    ArrayList<RelationshipsDO> entity_corp_client_list;
    ArrayList<DocumentsDo> documents_list = new ArrayList<>();
    String TAG = "TM";
    CreateEvent createEvent;

    public CommonRelationshipsAdapter(ArrayList<TeamDo> teamList, String Tag, ArrayList<RelationshipsDO> individual_list, ArrayList<RelationshipsDO> entity_client_list, ArrayList<RelationshipsDO> entity_corp_client_list, ArrayList<DocumentsDo> documents_list, CreateEvent createEvent) {
        this.entity_corp_client_list = entity_corp_client_list;
        this.createEvent = createEvent;
        this.entity_client_list = entity_client_list;
        this.individual_list = individual_list;
        this.tmList = teamList;
        this.documents_list = documents_list;
        this.TAG = Tag;
    }

    @NonNull
    @Override
    public CommonRelationshipsAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_team_members, parent, false);
        return new Viewholder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CommonRelationshipsAdapter.Viewholder holder, int position) {

        if (Objects.equals(TAG, "TM")) {
            TeamDo teamModel = tmList.get(position);
            holder.cb_documents.setChecked(tmList.get(position).isChecked());
            holder.cb_documents.setTag(position);
            holder.tv_tm_name.setText(teamModel.getName());
            holder.cb_documents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Integer pos = (Integer) holder.cb_documents.getTag();
//                    tmList.get(pos).setChecked(!tmList.get(pos).isChecked());
//                    createEvent.selected_Team(tmList);

                    Integer pos = (Integer) holder.cb_documents.getTag();
                    TeamDo list = tmList.get(pos);

                    // Toggle checked state
                    list.setChecked(!list.isChecked());

                    if (list.isChecked()) {
                        // Add only if not already in the list (to maintain selection order)
                        if (!createEvent.selected_tm_list.contains(list)) {
                            createEvent.selected_tm_list.add(list);
                        }
                    } else {
                        // Remove if unchecked
                        createEvent.selected_tm_list.remove(list);
                    }
                    createEvent.selected_Team(tmList);
                }
            });
        } else if (Objects.equals(TAG, "INDIVIDUAL")) {
            RelationshipsDO relationshipsDO = individual_list.get(position);
            holder.cb_documents.setChecked(individual_list.get(position).isChecked());
            holder.cb_documents.setTag(position);
            holder.tv_tm_name.setText(relationshipsDO.getName());
            holder.cb_documents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Integer pos = (Integer) holder.cb_documents.getTag();
//                    individual_list.get(pos).setChecked(!individual_list.get(pos).isChecked());
//                    createEvent.selected_individual(individual_list);
                    Integer pos = (Integer) holder.cb_documents.getTag();
                    RelationshipsDO list = individual_list.get(pos);

                    // Toggle checked state
                    list.setChecked(!list.isChecked());

                    if (list.isChecked()) {
                        // Add only if not already in the list (to maintain selection order)
                        if (!createEvent.selected_individual_list.contains(list)) {
                            createEvent.selected_individual_list.add(list);
                        }
                    } else {
                        // Remove if unchecked
                        createEvent.selected_individual_list.remove(list);
                    }
                    createEvent.selected_individual(individual_list);
                }
            });
        } else if (Objects.equals(TAG, "Documents")) {
            DocumentsDo documentsDo = documents_list.get(position);
            holder.cb_documents.setChecked(documents_list.get(position).isChecked());
            holder.cb_documents.setTag(position);
            holder.tv_tm_name.setText(documentsDo.getName());
            holder.cb_documents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Integer pos = (Integer) holder.cb_documents.getTag();
//                    documents_list.get(pos).setChecked(!documents_list.get(pos).isChecked());
//                    createEvent.selected_documents(documents_list);
                    Integer pos = (Integer) holder.cb_documents.getTag();
                    DocumentsDo documentsDo1 = documents_list.get(pos);

                    // Toggle checked state
                    documentsDo1.setChecked(!documentsDo1.isChecked());

                    if (documentsDo1.isChecked()) {
                        // Add only if not already in the list (to maintain selection order)
                        if (!createEvent.selected_documents_list.contains(documentsDo1)) {
                            createEvent.selected_documents_list.add(documentsDo1);
                        }
                    } else {
                        // Remove if unchecked
                        createEvent.selected_documents_list.remove(documentsDo1);
                    }
                    createEvent.selected_documents(documents_list);
                }
            });
        } else if (Objects.equals(TAG, "CORPORATE")) {
            RelationshipsDO relationshipsDO = entity_corp_client_list.get(position);
            holder.cb_documents.setChecked(entity_corp_client_list.get(position).isChecked());
            holder.cb_documents.setTag(position);
            holder.tv_tm_name.setText(relationshipsDO.getName());
            holder.cb_documents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Integer pos = (Integer) holder.cb_documents.getTag();
//                    entity_corp_client_list.get(pos).setChecked(!entity_corp_client_list.get(pos).isChecked());
//                    createEvent.selected_corporate(entity_corp_client_list);

                    Integer pos = (Integer) holder.cb_documents.getTag();
                    RelationshipsDO list = entity_corp_client_list.get(pos);

                    // Toggle checked state
                    list.setChecked(!list.isChecked());

                    if (list.isChecked()) {
                        // Add only if not already in the list (to maintain selection order)
                        if (!createEvent.selected_entity_corp_client_list.contains(list)) {
                            createEvent.selected_entity_corp_client_list.add(list);
                        }
                    } else {
                        // Remove if unchecked
                        createEvent.selected_entity_corp_client_list.remove(list);
                    }
                    createEvent.selected_corporate(entity_corp_client_list);
                }
            });
        } else {
            RelationshipsDO relationshipsDO = entity_client_list.get(position);
            holder.cb_documents.setChecked(entity_client_list.get(position).isChecked());
            holder.cb_documents.setTag(position);
            holder.tv_tm_name.setText(relationshipsDO.getName());
            holder.cb_documents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Integer pos = (Integer) holder.cb_documents.getTag();
//                    entity_client_list.get(pos).setChecked(!entity_client_list.get(pos).isChecked());
//                    createEvent.selected_entity_clients(entity_client_list);

                    Integer pos = (Integer) holder.cb_documents.getTag();
                    RelationshipsDO list = entity_client_list.get(pos);

                    // Toggle checked state
                    list.setChecked(!list.isChecked());

                    if (list.isChecked()) {
                        // Add only if not already in the list (to maintain selection order)
                        if (!createEvent.selected_entity_client_list.contains(list)) {
                            createEvent.selected_entity_client_list.add(list);
                        }
                    } else {
                        // Remove if unchecked
                        createEvent.selected_entity_client_list.remove(list);
                    }
                    createEvent.selected_entity_clients(entity_client_list);
                }
            });
        }

    }

    public ArrayList<RelationshipsDO> getIndividual_List() {
        return individual_list;
    }

    public ArrayList<TeamDo> getList_item() {
        return sharedList;
    }

    public ArrayList<TeamDo> getTmList() {
        return tmList;
    }

    public ArrayList<RelationshipsDO> getEntity_client_list() {
        return entity_client_list;
    }

    public ArrayList<DocumentsDo> getDocuments_list() {
        return documents_list;
    }

    @Override
    public int getItemCount() {
//        if (TAG == "Groups") {
//            return sharedList.size();
//        } else if (TAG == "Clients") {
//            return clientsList.size();
//        } else if(TAG == "UGM"){
//            return groupsList.size();
//        }
        if (Objects.equals(TAG, "TM")) {
            return tmList.size();
        } else if (Objects.equals(TAG, "INDIVIDUAL")) {
            return individual_list.size();
        } else if (Objects.equals(TAG, "Documents")) {
            return documents_list.size();
        } else if (Objects.equals(TAG, "CORPORATE")) {
            return entity_corp_client_list.size();
        } else {
            return entity_client_list.size();
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
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
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 0);
            select_tm_layout.setLayoutParams(params);
            select_tm_layout.setBackgroundResource(R.drawable.background_transparent);
        }
    }
}
