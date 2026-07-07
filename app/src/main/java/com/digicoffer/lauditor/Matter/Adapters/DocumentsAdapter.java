package com.digicoffer.lauditor.Matter.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Matter.OldViewModels.MatterDocuments;
import com.digicoffer.lauditor.Matter.ViewModels.MatterDocuments_En;
import com.digicoffer.lauditor.Matter.Models.DocumentsModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import java.util.ArrayList;

public class DocumentsAdapter extends RecyclerView.Adapter<DocumentsAdapter.Viewholder> {

    ArrayList<DocumentsModel> documentsList = new ArrayList<>();
    MatterDocuments matterDocuments;
    MatterDocuments_En matterDocuments_en;
    AppCompatButton btn_create;


    public DocumentsAdapter(ArrayList<DocumentsModel> documentsList, MatterDocuments matterDocuments, AppCompatButton btn_create) {
        this.matterDocuments = matterDocuments;
        this.documentsList = documentsList;
        this.btn_create = btn_create;
    }
    public DocumentsAdapter(ArrayList<DocumentsModel> documentsList, MatterDocuments_En matterDocuments, AppCompatButton btn_create) {
        this.matterDocuments_en = matterDocuments;
        this.documentsList = documentsList;
        this.btn_create = btn_create;
    }

    @NonNull
    @Override
    public DocumentsAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_team_members, parent, false);
        return new DocumentsAdapter.Viewholder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentsAdapter.Viewholder holder, int position) {

        DocumentsModel documentsModel = documentsList.get(position);
        holder.cb_documents.setChecked(documentsList.get(position).isChecked());
        holder.cb_documents.setTag(position);
        holder.tv_tm_name.setText(documentsModel.getName());
//        holder.cb_documents.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Integer pos = (Integer) holder.cb_documents.getTag();
//                if (documentsList.get(pos).isChecked()) {
//                    documentsList.get(pos).setChecked(false);
//                } else {
//                    documentsList.get(pos).setChecked(true);
//                }
//                matterDocuments.Add_Documents();
//                btn_create.setEnabled(true);
//                btn_create.setAlpha(1.0f);
//            }
//        });

//        holder.cb_documents.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Integer pos = (Integer) holder.cb_documents.getTag();
//                DocumentsModel clickedClient = documentsList.get(pos);
//
//                // Toggle checked state
//                clickedClient.setChecked(!clickedClient.isChecked());
//
//                if (clickedClient.isChecked()) {
//                    // Add only if not already in the list (to maintain selection order)
//                    if (!matterDocuments.selected_documents_list.contains(clickedClient)) {
//                        matterDocuments.selected_documents_list.add(clickedClient);
//                    }
//                } else {
//                    // Remove if unchecked
//                    matterDocuments.selected_documents_list.remove(clickedClient);
//                    matterDocuments.loadSelectedDocuments(new String[matterDocuments.selected_documents_list.size()]);
//                }
//
//                matterDocuments.Add_Documents(); // Update UI or other dependent logic
//            }
//        });
        holder.cb_documents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                DocumentsModel clickedDoc = documentsList.get(holder.getAdapterPosition());
//                clickedDoc.setChecked(!clickedDoc.isChecked());
//                holder.cb_documents.setChecked(clickedDoc.isChecked());
//
//                if (clickedDoc.isChecked()) {
//                    if (!matterDocuments.tempSelectedDocuments.contains(clickedDoc)) {
//                        matterDocuments.tempSelectedDocuments.add(clickedDoc);
//                    }
//                } else {
//                    matterDocuments.tempSelectedDocuments.remove(clickedDoc);
//                    matterDocuments.selected_documents_list.remove(clickedDoc);
//                    matterDocuments.loadSelectedDocuments(new String[matterDocuments.selected_documents_list.size()]);
//                }
//                matterDocuments.DocumentsText();

                Integer pos = (Integer) holder.cb_documents.getTag();
                DocumentsModel clickedtm = documentsList.get(pos);

                // Toggle checked state
                clickedtm.setChecked(!clickedtm.isChecked());

                if (clickedtm.isChecked()) {
                    // Add only if not already in the list (to maintain selection order)
                    if (!matterDocuments.tempSelectedDocuments.contains(clickedtm)) {
                        matterDocuments.tempSelectedDocuments.add(clickedtm);
                    }
                } else {
                    // Remove if unchecked
                    matterDocuments.selected_documents_list.remove(clickedtm);
                    matterDocuments.tempSelectedDocuments.remove(clickedtm);
                    if (!Constants.create_matter) {
                        btn_create.setEnabled(true);
                        btn_create.setAlpha(1.0f);
                    }
                    matterDocuments.loadSelectedDocuments(new String[matterDocuments.selected_documents_list.size()]);
                }
                matterDocuments.DocumentsText();
            }
        });

    }


    public ArrayList<DocumentsModel> getDocumentsList() {
        return documentsList;
    }

    @Override
    public int getItemCount() {

        return documentsList.size();


    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        private TextView tv_tm_name;
        private CheckBox cb_documents;
        private View list_line;
        private LinearLayout select_tm_layout;

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
