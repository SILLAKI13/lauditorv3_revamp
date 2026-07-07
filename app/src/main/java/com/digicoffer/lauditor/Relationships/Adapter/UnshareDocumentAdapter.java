package com.digicoffer.lauditor.Relationships.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Relationships.Model.SharedDocumentsDo;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;

import java.util.ArrayList;

public class UnshareDocumentAdapter extends RecyclerView.Adapter<UnshareDocumentAdapter.ViewHolder> {
    ArrayList<SharedDocumentsDo> sharedList = new ArrayList<>();
    ArrayList<SharedDocumentsDo> list_item = new ArrayList<>();
    String Shared_tag = "";
    Context mContext;
    FragmentActivity mActivity;
    RelationshipsAdapter relationshipsAdapter = null;
    String client_id = "";
    String rel_id;

    public UnshareDocumentAdapter(ArrayList<SharedDocumentsDo> sharedList, RelationshipsAdapter relationshipsAdapter) {
        this.relationshipsAdapter = relationshipsAdapter;
        this.sharedList = sharedList;
    }

    public ArrayList<SharedDocumentsDo> getList_item() {
        return sharedList;
    }

    @NonNull
    @Override
    public UnshareDocumentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.share_document_list, parent, false);
        return new ViewHolder(itemView);
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
    public void onBindViewHolder(@NonNull UnshareDocumentAdapter.ViewHolder holder, int position) {
        SharedDocumentsDo sharedDocumentsDo = sharedList.get(position);
        holder.tv_share_document.setText(sharedDocumentsDo.getName());
    }


    @Override
    public int getItemCount() {
        return sharedList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_share_document;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            {
                tv_share_document = itemView.findViewById(R.id.tv_share_document);
                tv_share_document.setTextSize(DynamicUtils.fifteen);
            }
        }
    }
}
