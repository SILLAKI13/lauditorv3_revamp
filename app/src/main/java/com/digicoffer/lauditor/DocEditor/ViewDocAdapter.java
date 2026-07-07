package com.digicoffer.lauditor.DocEditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Groups.Models.ActionModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;

import java.util.ArrayList;

public class ViewDocAdapter extends RecyclerView.Adapter<ViewDocAdapter.MyViewHolder> {
    ArrayList<ActionModel> actions_List = new ArrayList();
    ArrayList<DocListingModel> itemsArrayList;
    Context mcontext;
    CommonSpinnerAdapter spinner_adapter;
    boolean ischecked = true;
    ViewDocAdapter.InterfaceListener eventListener;
    ArrayList<DocListingModel> list_item;
    boolean isView = true;
    private int expandedPosition = -1;  // no item expanded

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (isView) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_list_doceditor, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.open_doc_file_view, parent, false);
        }
        return new MyViewHolder(view, isView);
    }


    public ViewDocAdapter(Context mcontext, ArrayList<DocListingModel> listitem, ViewDocAdapter.InterfaceListener eventListener, DocEditor docEditor, boolean isView) {
        this.mcontext = mcontext;
        itemsArrayList = listitem;
        list_item = listitem;
        this.isView = isView;
        this.eventListener = eventListener;
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    itemsArrayList = list_item;
                } else {
                    ArrayList<DocListingModel> filteredList = new ArrayList<>();
                    for (DocListingModel row : list_item) {
                        if (AndroidUtils.isNull(row.getDocumentname()).toLowerCase().contains(charString.toLowerCase()) || (AndroidUtils.isNull(row.get$date()).toLowerCase().contains(charString.toLowerCase()))) {
                            filteredList.add(row);
                        }
                    }
                    itemsArrayList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.count = itemsArrayList.size();
                filterResults.values = itemsArrayList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                itemsArrayList = (ArrayList<DocListingModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    //    @Override
//    public void onBindViewHolder(@NonNull ViewDocAdapter.MyViewHolder holder, int position) {
//        DocListingModel docListingModel = itemsArrayList.get(position);
//        actions_List.clear();
//        holder.sp_action.setVisibility(View.GONE);
//        actions_List.add(new ActionModel("View"));
//        actions_List.add(new ActionModel("Delete"));
//
//        holder.tv_description.setText(docListingModel.getDocumentname());
//        holder.tv_created_date.setText(AndroidUtils.formatDateToReadable(docListingModel.get$date()));
//        holder.tv_created_by.setText("Created By : ");
//        holder.created_date.setText("Created Date : ");
//        holder.tv_created_by_name.setText(Constants.NAME);
////        if(Constants.isSubscriptionEnded){
////            holder.custom_spinner_cardview.setAlpha(0.5f);
////            holder.custom_spinner_cardview.setEnabled(false);
////        }else{
////           holder. custom_spinner_cardview.setAlpha(1.0f);
////            holder.custom_spinner_cardview.setEnabled(true);
////        }
//
//        final CommonSpinnerAdapter spinner_adapter = new CommonSpinnerAdapter((Activity) mcontext, actions_List);
//        holder.sp_action.setAdapter(spinner_adapter);
//
//        holder.custom_spinner_cardview.setOnClickListener(new View.OnClickListener() {
//            boolean ischecked = true;
//
//            @Override
//            public void onClick(View v) {
//                if (ischecked)
//                    holder.sp_action.setVisibility(View.VISIBLE);
//                else
//                    holder.sp_action.setVisibility(View.GONE);
//                ischecked = !ischecked;
//            }
//        });
//        AndroidUtils.setDynamicHeight(holder.sp_action);
//        holder.sp_action.findFocus();
//        holder.sp_action.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                int name = position;
//                if (name == 0) {
////                    members.model_name("Edit Member");
//                    eventListener.ViewDoc(docListingModel);
//                } else if (name == 1) {

    /// /                    members.model_name("Update Group Access");
//                    eventListener.DeleteGroup(docListingModel);
//                }
//                holder.sp_action.setVisibility(View.GONE);
//            }
//        });
//    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        DocListingModel docListingModel = itemsArrayList.get(position);

        if (isView) {

            holder.tv_description.setText(docListingModel.getDocumentname());
            holder.tv_created_date.setText(AndroidUtils.formatDateToReadable(docListingModel.get$date()));
            holder.tv_created_by.setText("Created By : ");
            holder.created_date.setText("Created Date : ");
            holder.tv_created_by_name.setText(Constants.NAME);

            actions_List.clear();
            actions_List.add(new ActionModel("View"));
            actions_List.add(new ActionModel("Delete"));

            spinner_adapter = new CommonSpinnerAdapter<>((Activity) mcontext, actions_List);
            holder.sp_action.setAdapter(spinner_adapter);

            // ***** IMPORTANT: EXPAND ONLY IF THIS IS THE CURRENT EXPANDED ITEM *****
            boolean isExpanded = (position == expandedPosition);
            holder.sp_action.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            holder.custom_spinner_cardview.setOnClickListener(v -> {

                // If clicked item is already expanded → collapse
                if (expandedPosition == position) {
                    expandedPosition = -1;
                }
                else {
                    // Expand this & collapse previous one
                    int oldPos = expandedPosition;
                    expandedPosition = position;

                    // collapse previously expanded item
                    if (oldPos != -1) notifyItemChanged(oldPos);
                }

                // refresh the clicked item
                notifyItemChanged(position);
            });

            AndroidUtils.setDynamicHeight(holder.sp_action);

            holder.sp_action.setOnItemClickListener((parent, view, i, id) -> {

                spinner_adapter.setSelectedPosition(i);

                ActionModel actionModel = actions_List.get(i);
                String actionName = actionModel.getName();

                switch (actionName) {
                    case "View":
                        eventListener.ViewDoc(docListingModel.getDocumentname(), docListingModel.getDocid());
                        break;
                    case "Delete":
                        eventListener.DeleteDoc(docListingModel.getDocumentname(), docListingModel.getDocid());
                        break;
                }

                // After selection, close the spinner
                expandedPosition = -1;
                notifyItemChanged(position);
            });

        } else {
            holder.tv_description.setText(docListingModel.getDocumentname());
            holder.tv_created_by.setText("Created By: " + Constants.NAME);
            holder.tv_created_date.setText("Created Date: " + AndroidUtils.formatDateToReadable(docListingModel.get$date()));
            holder.ll_open_doc_view.setOnClickListener(v ->
                    eventListener.OpenDoc(docListingModel));
        }
    }



    public interface InterfaceListener {
        void OpenDoc(DocListingModel docListingModel);

        void DeleteDoc(String documentname, String docid);

        void ViewDoc(String documentname, String docid);
    }

    @Override
    public int getItemCount() {
        return itemsArrayList != null ? itemsArrayList.size() : 0;
    }

    //    public static class MyViewHolder extends RecyclerView.ViewHolder {
//        TextView tv_description, tv_created_by, tv_created_by_name, created_date, tv_created_date;
//        ListView sp_action;
//        CardView custom_spinner_cardview;
//
//        @SuppressLint("WrongViewCast")
//        public MyViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            tv_description = itemView.findViewById(R.id.tv_description);
//            tv_description.setTextSize(DynamicUtils.twenty);
//            tv_description.setAutoSizeTextTypeUniformWithConfiguration(1, 20, 1, TypedValue.COMPLEX_UNIT_SP);
//            tv_created_by = itemView.findViewById(R.id.tv_created_by);
//            tv_created_by_name = itemView.findViewById(R.id.tv_created_by_name);
//            created_date = itemView.findViewById(R.id.created_date);
//            tv_created_date = itemView.findViewById(R.id.tv_created_date);
//            sp_action = itemView.findViewById(R.id.sp_action);
//            custom_spinner_cardview = itemView.findViewById(R.id.custom_spinner_cardview);
//        }
//    }
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_description, tv_created_by, tv_created_by_name, created_date, tv_created_date;
        ListView sp_action;
        CardView custom_spinner_cardview;
        LinearLayout ll_open_doc_view;

        @SuppressLint("WrongViewCast")
        public MyViewHolder(@NonNull View itemView, boolean isView) {
            super(itemView);

            if (isView) {
                tv_description = itemView.findViewById(R.id.tv_description);
                tv_created_by = itemView.findViewById(R.id.tv_created_by);
                tv_created_by_name = itemView.findViewById(R.id.tv_created_by_name);
                created_date = itemView.findViewById(R.id.created_date);
                tv_created_date = itemView.findViewById(R.id.tv_created_date);
                sp_action = itemView.findViewById(R.id.sp_action);
                custom_spinner_cardview = itemView.findViewById(R.id.custom_spinner_cardview);
            } else {
                // Simpler layout view binding
                ll_open_doc_view = itemView.findViewById(R.id.ll_open_doc_view);
                tv_description = itemView.findViewById(R.id.tv_file_name);
                tv_created_by = itemView.findViewById(R.id.tv_created_by);
                tv_created_date = itemView.findViewById(R.id.tv_created_date);
            }
        }
    }

}
