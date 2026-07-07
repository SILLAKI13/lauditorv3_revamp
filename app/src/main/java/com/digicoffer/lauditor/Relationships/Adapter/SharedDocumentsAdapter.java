package com.digicoffer.lauditor.Relationships.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Relationships.Model.SharedDocumentsDo;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class SharedDocumentsAdapter extends RecyclerView.Adapter<SharedDocumentsAdapter.ViewHolder> implements Filterable {
    ArrayList<SharedDocumentsDo> sharedList = new ArrayList<>();
    ArrayList<SharedDocumentsDo> list_item = new ArrayList<>();
    ArrayList<SharedDocumentsDo> updated_shared_list = new ArrayList<>();
    String Shared_tag = "";
    Context mContext;
    FragmentActivity mActivity;
    RelationshipsAdapter relationshipsAdapter = null;
    String client_id = "";
    String rel_id;
    AlertDialog alertDialog;
    ArrayList<SharedDocumentsDo> selectedList = new ArrayList<>();
    String doc_name = "";
    int selectedPosition = -1;
    String highLightId = "";
    SharedDocumentsAdapter.EventListener eventListener;

    public SharedDocumentsAdapter(ArrayList<SharedDocumentsDo> sharedList, String shared_tag, Context mcontext, EventListener listner, String rel_id, String shared_client_id, FragmentActivity mActivity, RelationshipsAdapter relationshipsAdapter, String highLightId) {
        this.relationshipsAdapter = relationshipsAdapter;
        this.sharedList = sharedList;
        this.list_item = sharedList;
        this.Shared_tag = shared_tag;
        this.mContext = mcontext;
        this.eventListener = listner;
        client_id = shared_client_id;
        this.mActivity = mActivity;
        this.rel_id = rel_id;
        this.highLightId = highLightId;
    }

//    public SharedDocumentsAdapter(ArrayList<SharedDocumentsDo> sharedList, String shared_tag, Context mcontext, EventListener listner, String rel_id, String shared_client_id, FragmentActivity mActivity, RelationshipsAdapter relationshipsAdapter, String unshare) {
//        this.relationshipsAdapter = relationshipsAdapter;
//        this.sharedList = sharedList;
//        this.list_item = sharedList;
//        this.Shared_tag = shared_tag;
//        this.mContext = mcontext;
//        this.eventListener = listner;
//        client_id = shared_client_id;
//        this.mActivity = mActivity;
//        this.rel_id = rel_id;
//        this.doc_name = unshare;
//    }

    public ArrayList<SharedDocumentsDo> getList_item() {
        return sharedList;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    sharedList = list_item;
                } else {
                    ArrayList<SharedDocumentsDo> filteredList = new ArrayList<>();
                    for (SharedDocumentsDo row : list_item) {
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
                    sharedList = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.count = sharedList.size();
                filterResults.values = sharedList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                sharedList = (ArrayList<SharedDocumentsDo>) filterResults.values;
                check_allselected();
                notifyDataSetChanged();
            }
        };
    }

    public interface EventListener {
        void CopyDocument(SharedDocumentsDo sharedDocumentsDo);

        void viewDocument(SharedDocumentsDo sharedDocumentsDo);
    }

    @NonNull
    @Override
    public SharedDocumentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (Objects.equals(Shared_tag, "withme")) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.doc_sharewithus_layout, parent, false);
            return new SharedDocumentsAdapter.ViewHolder(itemView);
//        } else if (Shared_tag == "unshare") {
//            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.share_document_list, parent, false);
//            return new SharedDocumentsAdapter.ViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listing_rel_doc_layout, parent, false);
            return new SharedDocumentsAdapter.ViewHolder(itemView);
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

    @Override
    public void onBindViewHolder(@NonNull SharedDocumentsAdapter.ViewHolder holder, int position) {
        SharedDocumentsDo sharedDocumentsDo = sharedList.get(position);
//        if (!Shared_tag.equals("unshare")) {
//        if (getItemId(position + 1) % 2 == 0) {
//            holder.document_layout.setBackground(mContext.getDrawable(R.drawable.rectangle_light_grey_bg));
//        } else {
//            holder.document_layout.setBackground(mContext.getDrawable(R.drawable.rectangular_white_background));
//        }
//        }
//        list_item = sharedList;
        if (Objects.equals(Shared_tag, "withme")) {
              holder.tv_doc_name.setText(sharedDocumentsDo.getName());
            holder.tv_file_name.setText(sharedDocumentsDo.getDescription());
            holder.tv_doc_date.setText(sharedDocumentsDo.getCreated());
            if (sharedDocumentsDo.isHas_Confidential()) {
                if (!sharedDocumentsDo.getMatter_details_name().isEmpty()) {
                    holder.ll_confidential.setVisibility(View.VISIBLE);
                    holder.tv_matter_details_name.setText(sharedDocumentsDo.getMatter_details_name());
                }
            } else {
                holder.ll_confidential.setVisibility(View.GONE);
            }
//                holder.iv_copy.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
////                        eventListener.CopyDocument(sharedDocumentsDo);
//                    }
//                });
//                holder.iv_view.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        eventListener.viewDocument(sharedDocumentsDo);
//                    }
//                });
//                holder
//        } else if (Shared_tag.equals("unshare")) {
//            holder.tv_share_document.setText(doc_name);
        } else {
            holder.cb_documents.setChecked(sharedList.get(position).isChecked());
            holder.cb_documents.setTag(position);
            holder.tv_doc_name.setText(sharedDocumentsDo.getName());
            check_allselected();

            holder.iv_remove_doc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        updated_shared_list.clear();
                        SharedDocumentsDo sharedDocumentsDo1 = sharedList.get(holder.getAdapterPosition());
                        sharedDocumentsDo1.setId(sharedDocumentsDo.getId());
                        sharedDocumentsDo1.setName(sharedDocumentsDo.getName());
                        updated_shared_list.add(sharedDocumentsDo1);

                        JSONArray remove = new JSONArray();
                        JSONObject jsonObject1 = new JSONObject();
                        jsonObject1.put("docid", sharedDocumentsDo.getId());
                        jsonObject1.put("doctype", "general");
                        remove.put(jsonObject1);
                        Log.d("remove_doc_size", "" + remove.length() + "...");
                        remove_popup(updated_shared_list, remove);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            if (sharedDocumentsDo.isHas_Confidential()) {
                if (!sharedDocumentsDo.getMatter_details_name().isEmpty()) {
                    holder.ll_confidential.setVisibility(View.VISIBLE);
                    holder.tv_matter_details_name.setText(sharedDocumentsDo.getMatter_details_name());
                }
            } else {
                holder.ll_confidential.setVisibility(View.GONE);
            }
            holder.cb_documents.setOnClickListener(v -> {
                Integer pos = (Integer) holder.cb_documents.getTag();
                SharedDocumentsDo item = sharedList.get(pos);
                item.setChecked(!item.isChecked());

                if (item.isChecked()) {
                    if (!selectedList.contains(item)) selectedList.add(item);
                } else {
                    selectedList.remove(item);
                }

                check_allselected();

                // Update parent if needed
                if (Shared_tag.equals("byme"))
                    relationshipsAdapter.setSelected_unsharedocsList(selectedList);
                else
                    relationshipsAdapter.setSelected_sharedocsList(selectedList);
            });
        }
        if (highLightId != null && !highLightId.isEmpty() && Shared_tag.equals("withme") && sharedDocumentsDo.getId().equals(highLightId)) {

            holder.document_layout.setBackground(
                    mContext.getDrawable(R.drawable.rectangular_button_green_count)
            );

                // 🔵 Revert back to white after 2 seconds
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    holder.document_layout.setBackground(
                            mContext.getDrawable(R.drawable.rectangular_white_background)
                    );
                }, 5000);
            }
        holder.document_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                https://api.staging.digicoffer.com/professional/v2/relationship/645e0b02fffd8f042cf40b62/62ac591b863ef56f1aa5cef2/view
                relationshipsAdapter.View_doc(sharedDocumentsDo.getId(), sharedDocumentsDo);
            }
        });
//        if (position == position - 1){
//            holder.ll_dashed_line.setVisibility(View.GONE);
//        }
    }

    public boolean selectOrDeselectAll(boolean isChecked) {
        selectedList.clear(); // clear previous selections
        for (SharedDocumentsDo item : sharedList) {
            item.setChecked(isChecked);
            if (isChecked) selectedList.add(item);
        }
        notifyDataSetChanged();
        return isChecked;
    }

    public ArrayList<SharedDocumentsDo> getSelectedList() {
        return selectedList;
    }

    public void check_allselected() {
        boolean allSelected = true;
        for (SharedDocumentsDo groupModel : sharedList) {
            if (!groupModel.isChecked()) {
                allSelected = false;
                break;
            }
        }
        if (sharedList.isEmpty()) {
            allSelected = false;
        }
        relationshipsAdapter.check_select_all(allSelected);
    }

    private void remove_popup(ArrayList<SharedDocumentsDo> updated_shared_list, JSONArray remove) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
            LayoutInflater inflater = mActivity.getLayoutInflater();
            View view = inflater.inflate(R.layout.share_document_popup, null);
//        sv_relationships.getForeground().setAlpha(220);
            RecyclerView rv_remove_documents = view.findViewById(R.id.rv_remove_documents);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
            rv_remove_documents.setLayoutManager(layoutManager);
            // rv_remove_documents.setHasFixedSize(true);
            UnshareDocumentAdapter documentsAdapter = new UnshareDocumentAdapter(this.updated_shared_list, relationshipsAdapter);
            rv_remove_documents.setAdapter(documentsAdapter);
            // Assuming chk_select_all is the CheckBox you want to pass to GroupsAdapter
//            CheckBox chk_select_all = view.findViewById(R.id.chk_select_all);
            TextView tv_share_documents = view.findViewById(R.id.tv_share_documents);
            Button btn_cancel_share = view.findViewById(R.id.btn_cancel_share);
            Button btn_ok_share = view.findViewById(R.id.btn_ok_share);

            if (Shared_tag.equals("byme")) {
                tv_share_documents.setText("Documents Unshare");
                btn_ok_share.setText("Unshare");
            } else {
                tv_share_documents.setText("Documents Share");
                btn_ok_share.setText("Share");
            }
            btn_cancel_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
            btn_ok_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        relationshipsAdapter.callUnsharedDocumentWebservice(rel_id, remove, "", client_id, true);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    alertDialog.dismiss();
                }
            });
            final AlertDialog dialog = dialogBuilder.create();
            alertDialog = dialog;
            dialog.setView(view);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public int getItemCount() {
        return sharedList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cb_documents;
        private TextView tv_tm_name, tv_doc_name, tv_file_name, tv_doc_date, confidential_txt, tv_matter_details_name, tv_share_document;
        private LinearLayout ll_dashed_line, document_layout, ll_confidential;
        private ImageButton iv_view, iv_copy;
        private ImageView iv_remove_doc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cb_documents = itemView.findViewById(R.id.cb_documents);
            tv_tm_name = itemView.findViewById(R.id.tv_tm_name);
            tv_doc_name = itemView.findViewById(R.id.tv_doc_name);
            tv_file_name = itemView.findViewById(R.id.tv_file_name);
            if (Shared_tag.equals("withme")) {
                tv_doc_name.setTextColor(mContext.getResources().getColor(R.color.Blue_text_color));
            } else {
                tv_doc_name.setGravity(Gravity.START);
                tv_doc_name.setTextSize(DynamicUtils.eighteen);
                iv_remove_doc = itemView.findViewById(R.id.iv_remove_doc);
                if (Objects.equals(Shared_tag, "byme")) {
                    iv_remove_doc.setVisibility(View.VISIBLE);
                } else {
                    iv_remove_doc.setVisibility(View.GONE);
                }
            }
            ll_confidential = itemView.findViewById(R.id.ll_confidential);
            confidential_txt = itemView.findViewById(R.id.confidential_txt);
//            confidential_txt.setTextSize(12);
            confidential_txt.setTextColor(mContext.getColor(R.color.blue));
            confidential_txt.setText(R.string.confidential);
            tv_matter_details_name = itemView.findViewById(R.id.tv_matter_details_name);
            tv_matter_details_name.setTextSize(12);
//            if (Shared_tag.equals("unshare")) {
//                tv_share_document = itemView.findViewById(R.id.tv_share_document);
//            }
            document_layout = itemView.findViewById(R.id.document_layout);
            tv_doc_date = itemView.findViewById(R.id.tv_doc_date);
//            ll_dashed_line = itemView.findViewById(R.id.ll_dashed_line);
            iv_view = itemView.findViewById(R.id.iv_view);
            iv_copy = itemView.findViewById(R.id.iv_copy);
        }
    }
}
