package com.digicoffer.lauditor.Email;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class view_document_emailadapter extends RecyclerView.Adapter<view_document_emailadapter.MyViewHolder> implements AsyncTaskCompleteListener {
    //ArrayList<ViewDocumentsModel> docsList = new ArrayList<>();
    ArrayList<ViewDocumentsModel> list_item;
    ArrayList<ViewDocumentsModel> itemsArrayList;
    ArrayList<ViewDocumentsModel> docs_list = new ArrayList<>();
    private ArrayList<String> selectedDocumentNames = new ArrayList<>();
    private ArrayList<String> selectedDocumentIds = new ArrayList<>();
    private ArrayList<String> selectedDocumentPaths = new ArrayList<>();
    Email mail = new Email();
    Context cContext;
    Activity activity;
    Button btn_create;

    public static Dialog progress_dialog;
    int tempPos;

    public view_document_emailadapter(ArrayList<ViewDocumentsModel> itemsArrayList, Context context, Activity activity, AppCompatButton btn_create) {
        this.itemsArrayList = itemsArrayList;
        this.activity = activity;
        this.cContext = context;
        this.list_item = itemsArrayList;
        this.btn_create = btn_create;
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    itemsArrayList = list_item;
                } else {
                    ArrayList<ViewDocumentsModel> filteredList = new ArrayList<>();
                    for (ViewDocumentsModel row : list_item) {
                        if (AndroidUtils.isNull(row.getName()).toLowerCase().contains(charString.toLowerCase())) {
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

            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                itemsArrayList = (ArrayList<ViewDocumentsModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public view_document_emailadapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_client_group_document, parent, false);
        return new view_document_emailadapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull view_document_emailadapter.MyViewHolder holder, int position) {
        ViewDocumentsModel viewDocumentsModel = itemsArrayList.get(position);

        try {
            // Set text views
            holder.tv_client_name.setText(viewDocumentsModel.getUploaded_by());
            holder.tv_doc_description.setText(viewDocumentsModel.getDescription());
            holder.tv_image_name.setText(viewDocumentsModel.getName());
            holder.tv_created_date.setText(viewDocumentsModel.getCreated());
            holder.tv_Expiration_date.setText(viewDocumentsModel.getExpiration_date());

            // Set underline for text views
            holder.tv_client_name.setPaintFlags(holder.tv_client_name.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.tv_doc_description.setPaintFlags(holder.tv_doc_description.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            // Set checkbox state without using post()
            holder.checkbox_id.setTag(viewDocumentsModel);
            holder.checkbox_id.setChecked(viewDocumentsModel.getIsChecked()); // Set the saved checked state

            // Handle checkbox changes
            holder.checkbox_id.setOnCheckedChangeListener((buttonView, isChecked) -> {
                ViewDocumentsModel documentModel = (ViewDocumentsModel) buttonView.getTag();

                if (documentModel != null) {
                    documentModel.setIsChecked(isChecked); // Save the checkbox state in the model

                    if (isChecked) {
                        // Check if the document is already in the list
                        boolean documentExists = false;

                        for (int i = 0; i < Constants.composAttachDocAry.size(); i++) {
                            if (Constants.composAttachDocAry.get(i).getId().equals(documentModel.getId())) {
                                documentExists = true;
                                break; // Exit the loop once we find the document
                            }
                        }

                        if (!documentExists) {
                            // Add document to list since it is not already present
                            Constants.IdNameModel local = new Constants.IdNameModel();
                            local.setId(documentModel.getId());
                            local.setName(documentModel.getFilename());
                            Constants.composAttachDocAry.add(local);

                            // Find position of the newly added document
                            int pos = Constants.composAttachDocAry.size() - 1; // New document is always added at the last position
                            tempPos = pos;
                            // Load the document
                            view_document(documentModel.getId());
                        }
                    } else {
                        // Remove the document from the list
                        int pos = -1;
                        for (int i = 0; i < Constants.composAttachDocAry.size(); i++) {
                            if (documentModel.getId().equals(Constants.composAttachDocAry.get(i).getId())) {
                                pos = i;
                                break;
                            }
                        }
                        if (pos != -1) {
                            Constants.composAttachDocAry.remove(pos);
                        }
                    }
                }
                if (Constants.composAttachDocAry.isEmpty()) {
                    btn_create.setAlpha(0.5f);
                    btn_create.setEnabled(false);
                } else {
                    btn_create.setAlpha(1.0f);
                    btn_create.setEnabled(true);
                }
            });
        } catch (Exception e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), activity);
        }
    }


    @Override
    public int getItemCount() {
        return itemsArrayList.size();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progress_dialog != null && progress_dialog.isShowing()) {
            AndroidUtils.dismiss_dialog(progress_dialog);
        }
        String success = String.valueOf(httpResult.getResult());
        Log.d("Succ", success);
        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());

                if (httpResult.getRequestType().equals("view_document")) {
                    JSONObject data = result.getJSONObject("data");
                    String url = data.getString("url");
                    Constants.composAttachDocAry.get(tempPos).setUrl(url);
//                    URL=url;
                    Log.d("Doc_url", url);
                   /* if(!(doc_count==Constants.doc_id.length())) {
                        for (int i = 1; i < Constants.doc_id.length(); i++) {
                            try {
                                String doc1_id = Constants.doc_id.getString(i);
                                view_document(doc1_id);
                                Log.e("Const_doc", "count....."+doc_count+".....len.." + Constants.doc_id.length() + " .. " + doc1_id);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    else {
                        AndroidUtils.showAlert("Documents attached....");
                        openComposePopup();
                        yourGridView.setVisibility(View.VISIBLE);
                    } */
                } else {

                    System.out.println("Failed to obtain authentication URL");
                }
            } catch (JSONException e) {
                e.fillInStackTrace();
            }
        }
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkbox_id;
        RelativeLayout rv_doc_details;
        ViewDocumentsModel viewDocumentsModel;
        TextView tv_document_display_name, tv_Expiration, tv_client_name_one, tv_image_name, tv_Expiration_date, tv_client_name, tv_doc_description, tv_created_date;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_image_name = itemView.findViewById(R.id.tv_image_name);
            tv_client_name = itemView.findViewById(R.id.tv_client_name);
            tv_client_name.setTextSize(DynamicUtils.fifteen);
            checkbox_id = itemView.findViewById(R.id.checkbox_id);
            checkbox_id.setVisibility(View.VISIBLE);
            rv_doc_details = itemView.findViewById(R.id.rv_doc_details);
            tv_doc_description = itemView.findViewById(R.id.tv_doc_description);
            tv_doc_description.setTextSize(DynamicUtils.fifteen);
            tv_created_date = itemView.findViewById(R.id.tv_created_date);
            tv_created_date.setText(R.string.date);
            tv_Expiration = itemView.findViewById(R.id.tv_Expiration);
            tv_Expiration.setText(R.string.expiration_);
            tv_Expiration_date = itemView.findViewById(R.id.tv_Expiration_date);
            tv_Expiration_date.setText(R.string.expiration);

        }
    }

    public void view_document(String doc_id) {
        try {
            progress_dialog = AndroidUtils.get_progress(activity);
            JSONObject jsonObject = new JSONObject();
            //https://api.staging.digicoffer.com/professional/v3/document/64ca0bd9fffd8f083fafaa22/view
            String baseUrl = "https://api.staging.digicoffer.com/professional/v3/document/";
            String url = baseUrl + doc_id + "/view";
            WebServiceHelper.callHttpWebService(this, activity, WebServiceHelper.RestMethodType.GET, "v3/document/" + doc_id + "/view", "view_document", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }
}
