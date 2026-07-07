package com.digicoffer.lauditor.Documents.DocumentsListAdpater;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Documents.Documents;
import com.digicoffer.lauditor.Documents.ViewModel.DocumentsEn;
import com.digicoffer.lauditor.Documents.Models.DocumentsModel;
import com.digicoffer.lauditor.Matter.OldViewModels.MatterDocuments;
import com.digicoffer.lauditor.Matter.ViewModels.MatterDocuments_En;
import com.digicoffer.lauditor.R;

import java.util.ArrayList;
import java.util.Objects;

public class DocumentsListAdapter extends RecyclerView.Adapter<DocumentsListAdapter.ViewHolder> {
    ArrayList<DocumentsModel> itemsArrayList;
    ArrayList<DocumentsModel> list_item;
    boolean select_checked;
    boolean isfiledeleted = false;
    String tag = "";
    String subtag = "";
    Documents documents;
    DocumentsEn documents_en;
    MatterDocuments matterDocuments;
    MatterDocuments_En matterDocuments_en;
    DocumentsListAdapter.EventListener eventListener;

    public DocumentsListAdapter(ArrayList<DocumentsModel> itemsArrayList, String tag, String subtag, EventListener eventListener, Documents documents) {
        this.documents = documents;
        this.itemsArrayList = itemsArrayList;
        this.list_item = itemsArrayList;
        this.tag = tag;
        this.subtag = subtag;
        this.eventListener = eventListener;
    }

    public DocumentsListAdapter(ArrayList<DocumentsModel> itemsArrayList, String tag, String subtag, EventListener eventListener, DocumentsEn documents) {
        this.documents_en = documents;
        this.itemsArrayList = itemsArrayList;
        this.list_item = itemsArrayList;
        this.tag = tag;
        this.subtag = subtag;
        this.eventListener = eventListener;
    }

    public DocumentsListAdapter(ArrayList<DocumentsModel> itemsArrayList, String tag, String subtag, EventListener eventListener, MatterDocuments documents) {
        this.matterDocuments = documents;
        this.itemsArrayList = itemsArrayList;
        this.list_item = itemsArrayList;
        this.tag = tag;
        this.subtag = subtag;
        this.eventListener = eventListener;
    }

    public DocumentsListAdapter(ArrayList<DocumentsModel> itemsArrayList, String tag, String subtag, EventListener eventListener, MatterDocuments_En documents) {
        this.matterDocuments_en = documents;
        this.itemsArrayList = itemsArrayList;
        this.list_item = itemsArrayList;
        this.tag = tag;
        this.subtag = subtag;
        this.eventListener = eventListener;
    }

    public interface EventListener {

        void ViewTags(DocumentsModel documentsModel, ArrayList<DocumentsModel> itemsArrayList);

        void EditDocuments(DocumentsModel documentsModel, ArrayList<DocumentsModel> itemsArrayList, int position);

        void RemoveDocument(int position, String tag);
//        void RemoveDocument(DocumentsModel documentsModel, ArrayList<DocumentsModel> itemsArrayList, String tag);

    }

    @NonNull
    @Override
    public DocumentsListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.displays_documents_list, parent, false);
        return new DocumentsListAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentsListAdapter.ViewHolder holder, int position) {
        DocumentsModel documentsModel = itemsArrayList.get(position);
        itemsArrayList = list_item;
//        if (subtag=="view_tags"){
//            holder.btn_view_tags.setVisibility(View.VISIBLE);
//        }
//        if (documentsModel.getTags() != null && (Objects.equals(tag, "add_tag") || (Objects.equals(tag, "Hide_Add_Edit_tag")))) {
        holder.tv_document_name.setText(documentsModel.getName() + "." + documentsModel.getContent_type());
        holder.tv_document_name.setMaxLines(1);
        holder.tv_document_name.setEllipsize(TextUtils.TruncateAt.END);
        holder.cb_documents_list.setChecked(itemsArrayList.get(position).isChecked());
        holder.cb_documents_list.setTag(position);
        holder.iv_edit_meta.setVisibility(View.VISIBLE);
        holder.lock_open.setVisibility(View.GONE);
//            if(groupModel.isIsenabled()==null)
//        if (matterDocuments_en != null) {
//            holder.chk_box_layout.setVisibility(View.VISIBLE);
//            holder.cb_documents_list.setVisibility(View.VISIBLE);
//            holder.iv_edit_meta.setVisibility(View.VISIBLE);
//            check_allselected();
//            isAnyItemChecked();
//            if (documentsModel.getTags() != null) {
//                holder.btn_view_tags.setVisibility(View.VISIBLE);
//            } else {
//                holder.btn_view_tags.setVisibility(View.GONE);
//            }
//            if (itemsArrayList.get(position).isIsenabled()) {
////            holder.cb_documents_list.setEnabled(true);
//                holder.disable_download_icon.setVisibility(View.GONE);
//                holder.enable_download_icon.setVisibility(View.VISIBLE);
//            } else {
////            holder.cb_documents_list.setEnabled(false);
//                holder.disable_download_icon.setVisibility(View.VISIBLE);
//                holder.enable_download_icon.setVisibility(View.GONE);
//            }
////        holder.iv_edit_meta.setVisibility(View.VISIBLE);
//            if (Constants.DocTagType.equals("add_tag")) {
//                holder.chk_box_layout.setVisibility(View.VISIBLE);
//                holder.iv_edit_meta.setVisibility(View.GONE);
//            } else if (Constants.DocTagType.equals("edit_meta")) {
//                holder.chk_box_layout.setVisibility(View.GONE);
//                holder.iv_edit_meta.setVisibility(View.VISIBLE);
//            } else {
//                holder.chk_box_layout.setVisibility(View.GONE);
//                holder.iv_edit_meta.setVisibility(View.GONE);
//            }
//            if (itemsArrayList.get(position).getIsencrypted()) {
//                holder.lock_open.setVisibility(View.GONE);
//                holder.lock_close.setVisibility(View.VISIBLE);
//            } else {
//                holder.lock_close.setVisibility(View.GONE);
//                holder.lock_open.setVisibility(View.VISIBLE);
//            }
//        }
        holder.cb_documents_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer pos = (Integer) holder.cb_documents_list.getTag();
                //                        itemsArrayList.add(itemsArrayList.get(pos));
                //                        itemsArrayList.remove(itemsArrayList.get(pos));
                itemsArrayList.get(pos).setChecked(!itemsArrayList.get(pos).isChecked());
                check_allselected();
                isAnyItemChecked();
                notifyDataSetChanged();

            }
        });
        holder.btn_view_tags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventListener.ViewTags(documentsModel, itemsArrayList);
            }
        });
        holder.iv_edit_meta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventListener.EditDocuments(documentsModel, itemsArrayList, position);
            }
        });
        holder.lock_open.setOnClickListener(view -> {
//            holder.lock_close.setVisibility(View.VISIBLE);
//            holder.lock_open.setVisibility(View.GONE);
//            documentsModel.setIsencrypted(true);
//            Check_AllEncrypted();
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                DocumentsModel doc = itemsArrayList.get(pos);
                doc.setIsencrypted(true);

                notifyItemChanged(pos); // Only update this item
                checkEncryptionStatus(); // Check if all are encrypted
            }
        });

        holder.lock_close.setOnClickListener(view -> {
//            holder.lock_open.setVisibility(View.VISIBLE);
//            holder.lock_close.setVisibility(View.GONE);
//            documentsModel.setIsencrypted(false);
//            Check_AllEncrypted();
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                DocumentsModel doc = itemsArrayList.get(pos);
                doc.setIsencrypted(false);

                notifyItemChanged(pos); // Only update this item
                checkEncryptionStatus(); // Check if all are decrypted
            }
        });
        holder.enable_download_icon.setOnClickListener(view -> {
//            holder.lock_open.setVisibility(View.VISIBLE);
//            holder.lock_close.setVisibility(View.GONE);
//            documentsModel.setIsencrypted(false);
//            Check_AllEncrypted();
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                DocumentsModel doc = itemsArrayList.get(pos);
                doc.setIsenabled(false);

                notifyItemChanged(pos); // Only update this item
                checkEnableDownloadStatus(); // Check if all are decrypted
            }
        });
        holder.disable_download_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    DocumentsModel doc = itemsArrayList.get(pos);
                    doc.setIsenabled(true);

                    notifyItemChanged(pos); // Only update this item
                    checkEnableDownloadStatus();
                }
            }
        });

        holder.iv_edit_meta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventListener.EditDocuments(documentsModel, itemsArrayList, position);
            }
        });
        holder.iv_cancel.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                eventListener.RemoveDocument(pos, tag);
            }
        });

//            holder.cb_team_members.setChecked(true);
//        holder.tv_tm_name.setText(groupModel.getName());
    }

    public ArrayList<DocumentsModel> getList_item() {
        return itemsArrayList;
    }

    public void isAnyItemChecked() {
        boolean isSelected = false;
        for (DocumentsModel item : itemsArrayList) {
            if (item.isChecked()) {
                isSelected = true;
                break;// At least one checked
            }
        }
        if (matterDocuments_en != null) {
            matterDocuments_en.isAnyOneSelected(isSelected);
        }
    }

    public void check_allselected() {
        boolean allSelected = true; // Assume all are selected

        for (DocumentsModel item : itemsArrayList) {
            if (!item.isChecked()) {
                allSelected = false;
                break;
            }
        }
        if (matterDocuments != null) {
            matterDocuments.check_select_all(allSelected);
        } else if (matterDocuments_en != null) {
            matterDocuments_en.check_select_all(allSelected);
        } else {
            documents.check_select_all(allSelected);
        }
//        notifyDataSetChanged();
    }

    public void checkEncryptionStatus() {
        boolean allEncrypted = true;
        boolean allDecrypted = true;

        for (DocumentsModel item : itemsArrayList) {
            if (item.getIsencrypted()) {
                allDecrypted = false; // At least one is encrypted
            } else {
                allEncrypted = false; // At least one is decrypted
            }

            if (!allDecrypted && !allEncrypted) {
                break; // No need to check further
            }
        }

        if (matterDocuments != null) {
            if (allEncrypted) {
                matterDocuments.check_encrypted(true); // Enable encryption background
            } else if (allDecrypted) {
                matterDocuments.check_encrypted(false); // Disable encryption background
            }
        } else if (matterDocuments_en != null) {
            if (allEncrypted) {
                matterDocuments_en.check_encrypted(true); // Enable encryption background
            } else if (allDecrypted) {
                matterDocuments_en.check_encrypted(false); // Disable encryption background
            }
        } else {
            if (allEncrypted) {
                documents.check_encrypted(true); // Enable encryption background
            } else if (allDecrypted) {
                documents.check_encrypted(false); // Disable encryption background
            }
        }

//        notifyDataSetChanged();
    }

    public void checkEnableDownloadStatus() {
        boolean allEnabled = true;
        boolean allDisabled = true;

        for (DocumentsModel item : itemsArrayList) {
            if (item.isIsenabled()) {
                allDisabled = false; // At least one is encrypted
            } else {
                allEnabled = false; // At least one is decrypted
            }

            if (!allDisabled && !allEnabled) {
                break; // No need to check further
            }
        }
        if (matterDocuments != null) {
            if (allEnabled) {
                matterDocuments.check_enabled(true); // Enable encryption background
            } else if (allDisabled) {
                matterDocuments.check_enabled(false);
            }// Disable encryption background
        } else if (matterDocuments_en != null) {
            if (allEnabled) {
                matterDocuments_en.check_enabled(true); // Enable encryption background
            } else if (allDisabled) {
                matterDocuments_en.check_enabled(false);
            }
        } else {
            if (allEnabled) {
                documents.check_enabled(true); // Enable encryption background
            } else if (allDisabled) {
                documents.check_enabled(false); // Disable encryption background
            }
        }

//        notifyDataSetChanged();
    }

    public boolean EncryptAllorDecryptAll(boolean isChecked) {
        for (DocumentsModel item : itemsArrayList) {
            item.setIsencrypted(isChecked);
        }

        notifyDataSetChanged(); // Only call once after updating all items
        return isChecked;
    }

    public boolean EnableAllorDisableAll(boolean isChecked) {
        for (DocumentsModel item : itemsArrayList) {
            item.setIsenabled(isChecked);
        }

        notifyDataSetChanged(); // Only call once after updating all items
        return isChecked;
    }

    public boolean selectOrDeselectAll(boolean isChecked) {
        for (int i = 0; i < list_item.size(); i++) {
            list_item.get(i).setChecked(isChecked);
            notifyDataSetChanged();
        }
        return isChecked;
    }

    //...
//    public boolean selectOrDeselectAll(boolean isChecked) {
//        for (int i = 0; i < list_item.size(); i++) {
//            list_item.get(i).setChecked(isChecked);
//            notifyDataSetChanged();
//        }
//        return isChecked;
//    }
//
//    public boolean EncryptAllorDecryptAll(boolean isChecked) {
//        for (int i = 0; i < itemsArrayList.size(); i++) {
//            itemsArrayList.get(i).setIsencrypted(isChecked);
//            notifyDataSetChanged();
//        }
//        return isChecked;
//    }
//
//    public void check_allselected() {
//        for (int i = 0; i < itemsArrayList.size(); i++) {
//            if (!itemsArrayList.get(i).isChecked()) {
//                select_checked = false;
//                break;
//            } else {
//                select_checked = true;
//            }
//        }
//        documents.check_select_all(select_checked);
//
//        notifyDataSetChanged();
//    }
//
//    public void Check_AllEncrypted() {
//        for (int i = 0; i < itemsArrayList.size(); i++) {
//            if (!itemsArrayList.get(i).getIsencrypted()) {
//                select_checked = false;
//                break;
//            } else {
//                select_checked = true;
//            }
//        }
//        documents.check_encrypted(select_checked);
//
//        notifyDataSetChanged();
//    }
    //..

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return itemsArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cb_documents_list;
        private TextView tv_document_name;
        private LinearLayoutCompat chk_box_layout;
        private ImageView iv_cancel, iv_edit_meta, lock_close, lock_open, enable_download_icon, disable_download_icon;
        private Button btn_view_tags;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chk_box_layout = itemView.findViewById(R.id.chk_box_layout);
            cb_documents_list = itemView.findViewById(R.id.chk_selected_documents);
            tv_document_name = itemView.findViewById(R.id.tv_document_name);
            iv_edit_meta = itemView.findViewById(R.id.iv_edit_meta);
            lock_close = itemView.findViewById(R.id.lock_close);
            enable_download_icon = itemView.findViewById(R.id.enable_download_icon);
            disable_download_icon = itemView.findViewById(R.id.disable_download_icon);
            lock_open = itemView.findViewById(R.id.lock_open);
            if (matterDocuments_en != null) {
                lock_open.setVisibility(View.VISIBLE);
            }
//            chk_box_layout.setVisibility(View.VISIBLE);
//            cb_documents_list.setVisibility(View.VISIBLE);
            iv_edit_meta.setImageResource(R.drawable.edit__icon);
            btn_view_tags = itemView.findViewById(R.id.btn_view_tags);
            btn_view_tags.setText(R.string.view_tags);
            iv_cancel = itemView.findViewById(R.id.iv_cancel);
            if (Objects.equals(tag, "add_tag")) {
                chk_box_layout.setVisibility(View.VISIBLE);
                cb_documents_list.setVisibility(View.VISIBLE);
                iv_edit_meta.setVisibility(View.GONE);
//                btn_view_tags.setVisibility(View.GONE);
            } else if (Objects.equals(tag, "edit_meta")) {
                iv_edit_meta.setVisibility(View.VISIBLE);
                cb_documents_list.setVisibility(View.GONE);
                chk_box_layout.setVisibility(View.GONE);
//                btn_view_tags.setVisibility(View.GONE);
            } else if (Objects.equals(tag, "en_encrption")) {
//                btn_view_tags.setVisibility(View.GONE);
                lock_open.setVisibility(View.GONE);
                lock_close.setVisibility(View.VISIBLE);
            } else if (Objects.equals(tag, "dis_encrption")) {
//                btn_view_tags.setVisibility(View.GONE);
                lock_open.setVisibility(View.VISIBLE);
                lock_close.setVisibility(View.GONE);
            } else if (Objects.equals(tag, "enable_download")) {
                disable_download_icon.setVisibility(View.GONE);
                enable_download_icon.setVisibility(View.VISIBLE);
            } else if (Objects.equals(tag, "disable_download")) {
                disable_download_icon.setVisibility(View.VISIBLE);
                enable_download_icon.setVisibility(View.GONE);
            } else {
                chk_box_layout.setVisibility(View.GONE);
                iv_edit_meta.setVisibility(View.GONE);
                cb_documents_list.setVisibility(View.GONE);
                lock_open.setVisibility(View.VISIBLE);
                lock_close.setVisibility(View.GONE);
            }
        }
    }
}
