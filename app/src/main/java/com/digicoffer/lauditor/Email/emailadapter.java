package com.digicoffer.lauditor.Email;

import static android.app.PendingIntent.getActivity;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.digicoffer.lauditor.Email.Email.progress_dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Documents.Documents;
import com.digicoffer.lauditor.Documents.DocumentsListAdpater.GroupsListAdapter;
import com.digicoffer.lauditor.Documents.Models.ClientsModel;
import com.digicoffer.lauditor.Documents.Models.DocumentsModel;
import com.digicoffer.lauditor.Documents.Models.MattersModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class EmailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements AsyncTaskCompleteListener {
    private static final int VIEW_TYPE_EMAIL = 0;
    private static final int VIEW_TYPE_ATTACHMENT = 1;
    static Context context_type;
    AlertDialog dialog;
    ArrayList<MattersModel> matterlist = new ArrayList<>();
    ArrayList<DocumentsModel> client_groups_list = new ArrayList<>();
    ArrayList<DocumentsModel> selected_client_groups_list = new ArrayList<>();
    private List<MessageModel> messages = new ArrayList<>();
    TextView tv_client_name;

    List<MessageModel> itemsList = new ArrayList<>();

    boolean ischecked = true;
    Email email;

    String client_id = "", matter_id = "";
    String client_name = "", matterName = "";
    String msg_id = " ";
    String baseUrl = Constants.EMAIL_UPLOAD_URL;
    String token = Constants.TOKEN;
    String msgId = Constants.msg_id;
    String partId = Constants.part_id;
    Activity activity;

    public EmailAdapter(List<MessageModel> messages, Email email, Activity activity) {
        this.context_type = email.getContext();
        this.email = email;
        this.messages = messages;
        this.itemsList = messages;
        this.activity = activity;

    }


    @Override
    public int getItemViewType(int position) {
        if ((messages != null) && (messages.get(position).getAttachment() != null)) {
            if ((!messages.get(position).getAttachment().isEmpty()) && (messages.get(position).isAttachment())) {
                return VIEW_TYPE_ATTACHMENT;
            } else {
                return VIEW_TYPE_EMAIL;
            }
        } else {
            return VIEW_TYPE_EMAIL;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        switch (viewType) {
            case VIEW_TYPE_EMAIL:
                view = inflater.inflate(R.layout.email_card_view, parent, false);
                return new EmailViewHolder(view);
            case VIEW_TYPE_ATTACHMENT:
                view = inflater.inflate(R.layout.attachment_item, parent, false);
                return new EmailViewHolder(view);
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    messages = itemsList;
                } else {
                    ArrayList<MessageModel> filteredList = new ArrayList<>();
                    for (MessageModel row : itemsList) {
                        if (AndroidUtils.isNull(row.getFrom()).toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    messages = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.count = messages.size();
                filterResults.values = messages;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                messages = (ArrayList<MessageModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel message = messages.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_EMAIL:
                ((EmailViewHolder) holder).bindEmail(message);
                break;
            case VIEW_TYPE_ATTACHMENT:
                ((EmailViewHolder) holder).bindEmail(message);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {

    }


    class EmailViewHolder extends RecyclerView.ViewHolder implements AsyncTaskCompleteListener {
        TextView senderName;
        TextView subject, matter_name;
        RecyclerView gridRecyclerView;
        TextView tvEmail;
        RecyclerView rv_display_upload_groups_docs, rv_upload_groups;
        ListView list_client, list_matter;
        LinearLayout ll_select_groups, ll_select_grp, ll_matter, ll_matter_view, ll_upload_client_group, ll_upload_groups;
        Button btn_group_cancel;
        CheckBox cb_documents;
        LinearLayout ll_client_name;
        boolean ischecked = true, ischecked_matter = true, ischeckedClient_group = true;
        private TextView custom_client, custom_matter;
        private LinearLayout ll_custom_client;
        ImageView dropdown_icon, clear_icon, dropdown_icon2, clear_icon2;
        String category = "";
        AppCompatButton btn_upload_new, btn_cancel_save;
        TextView tv_select_groups, tv_select_upload_group_name, tv_select_upload_groups;
        LinearLayout linearLayout2;
        ArrayList<DocumentsModel> groupsList = new ArrayList<>();
        boolean ischecked_group = true;
        Button btn_group_submit;
        ArrayList<ClientsModel> clientsList = new ArrayList<>();
        ArrayList<ClientsModel> CorpClientsList = new ArrayList<>();
        ArrayList<ClientsModel> selectedClients = new ArrayList<>();
        LinearLayout constraint_root;

        boolean[] selectedLanguage;
        ArrayList<DocumentsModel> selected_groups_list = new ArrayList<>();

        public EmailViewHolder(@NonNull View itemView) {
            super(itemView);
            senderName = itemView.findViewById(R.id.sender_name);
            subject = itemView.findViewById(R.id.subject);
            gridRecyclerView = itemView.findViewById(R.id.gridRecyclerView);
//            gridRecyclerView.setNumColumns(3);
        }

        //        public void bindEmail(MessageModel email) {
//            senderName.setText(email.getFrom());
//            subject.setText(email.getSubject());
//            senderName.setTag(email.getMsgId());
//            if(email.getAttachment()!=null) {
//                if (!email.getAttachment().isEmpty()) {
//                    GridAdapter adapter = new GridAdapter(context_type, email.attachments);
//                    gridView.setAdapter(adapter);
//
//                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                        @Override
//                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                            Log.e("msgId", ":" + senderName.getTag().toString());
//                            Log.e("msgId", ":" + email.getAttachment().get(position).partId);
//                            Constants.msg_id = senderName.getTag().toString();
//                            Constants.part_id = email.getAttachment().get(position).partId;
//                            GridviewPopup(itemView);
//                        }
//                    });
//                }
//            }
//
//        }
        public void bindEmail(MessageModel email) {
            senderName.setText(email.getFrom());
            subject.setText(email.getSubject());
            senderName.setTag(email.getMsgId());

            if (Constants.ROLE.equals("AAM")) {
                gridRecyclerView.setVisibility(GONE);
            } else {
                gridRecyclerView.setVisibility(VISIBLE);
            }
            if (email.getAttachment() != null && !email.getAttachment().isEmpty()) {
                gridRecyclerView.setLayoutManager(new GridLayoutManager(context_type, 2));
                gridRecyclerView.setNestedScrollingEnabled(false);
                gridRecyclerView.setHasFixedSize(false);

//        if(email.getAttachment()!=null) {
                if (!email.getAttachment().isEmpty()) {
                    Attachmentadapter adapter = new Attachmentadapter(context_type, email.attachments);
                    gridRecyclerView.setAdapter(adapter);

                    adapter.setOnAttachmentClickListener(new Attachmentadapter.OnAttachmentClickListener() {
                        @Override
                        public void onAttachmentClick(int position) {
                            if (!Constants.is_active) {
                                AndroidUtils.showRenewalPopup(activity);
                            } else {
                                Log.e("msgId", ":" + senderName.getTag().toString());
                                Log.e("partId", ":" + email.getAttachment().get(position).partId);
                                Constants.msg_id = senderName.getTag().toString();
                                Constants.part_id = email.getAttachment().get(position).partId;
                                GridviewPopup(itemView);
                            }
                        }
                    });
                    gridRecyclerView.setAdapter(adapter);
                }
//            }
            }
        }


        private void GridviewPopup(View view) {
            RadioGroup selectionGroupsView;
            RadioButton rbMatter, rbMatterView, rbClient, rbClientView;
            selected_groups_list.clear();
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            LayoutInflater inflater = LayoutInflater.from(view.getContext());
            View popupView = inflater.inflate(R.layout.document_upload, null);
            TextView tv_client_name = popupView.findViewById(R.id.client_name);

            TextView group_name = popupView.findViewById(R.id.group_name);
            group_name.setText(R.string.select_group);
            TextView client_namee = popupView.findViewById(R.id.client_nameee);
            client_namee.setText(R.string.matter);
            client_namee.setBackgroundDrawable(context_type.getResources().getDrawable(R.drawable.button_left_green_background));// Assuming "green" is the desired color resource

            client_namee.setTextColor(ContextCompat.getColor(context_type, R.color.white));
            TextView firm_namee = popupView.findViewById(R.id.firm_nameee);
            firm_namee.setText(R.string.firm);
            firm_namee.setBackgroundDrawable(context_type.getResources().getDrawable(R.drawable.button_right_background));// Assuming "green" is the desired color resource

            firm_namee.setTextColor(ContextCompat.getColor(context_type, R.color.black));
            if (!"solo".equals(Constants.CATEGORY)) {
                firm_namee.setVisibility(VISIBLE);
            } else {
                client_namee.setTextColor(ContextCompat.getColor(context_type, R.color.white));
                client_namee.setBackgroundDrawable(context_type.getResources().getDrawable(R.drawable.rectangular_button_green_count));
                firm_namee.setVisibility(GONE);
            }
            ll_custom_client = popupView.findViewById(R.id.custom_client);
            custom_client = ll_custom_client.findViewById(R.id.tv_spinner_view);
            dropdown_icon = ll_custom_client.findViewById(R.id.img_dropdown_icon);
            clear_icon = ll_custom_client.findViewById(R.id.img_clear_icon);
            matter_name = popupView.findViewById(R.id.matter_name);
            matter_name.setText(R.string.matter);
            ll_matter = popupView.findViewById(R.id.ll_matter);
            ll_matter.setVisibility(GONE);

            ll_matter_view = popupView.findViewById(R.id.ll_matter_view);
            ll_matter_view.setVisibility(GONE);

            custom_matter = ll_matter.findViewById(R.id.tv_spinner_view);
            dropdown_icon2 = ll_matter.findViewById(R.id.img_dropdown_icon);
            clear_icon2 = ll_matter.findViewById(R.id.img_clear_icon);
            list_matter = popupView.findViewById(R.id.list_matter);

            list_client = popupView.findViewById(R.id.list_client_email);
            btn_upload_new = popupView.findViewById(R.id.btn_upload_new);
            tv_select_groups = popupView.findViewById(R.id.tv_select_groups);
            rv_display_upload_groups_docs = popupView.findViewById(R.id.rv_display_upload_groups_docs);

            tv_select_upload_group_name = popupView.findViewById(R.id.tv_select_upload_group_name);
            tv_select_upload_group_name.setText(R.string.select_groups);

            tv_select_upload_groups = popupView.findViewById(R.id.tv_select_upload_groups);
            //hint
            custom_client.setHint(R.string.select_client_name);
            custom_matter.setHint(R.string.select_matters);
            tv_select_upload_groups.setHint(R.string.select_groups);

            rv_upload_groups = popupView.findViewById(R.id.rv_upload_groups);
            rv_upload_groups.setBackground(context_type.getDrawable(R.drawable.rectangle_light_grey));
            ll_upload_client_group = popupView.findViewById(R.id.ll_upload_client_group);
            ll_upload_groups = popupView.findViewById(R.id.ll_upload_groups);

            btn_group_cancel = popupView.findViewById(R.id.btn_group_cancel);
            btn_group_cancel.setVisibility(View.GONE);
            btn_cancel_save = popupView.findViewById(R.id.btn_cancel_save);
            btn_group_submit = popupView.findViewById(R.id.btn_group_submit);
            btn_group_submit.setVisibility(View.GONE);
            cb_documents = popupView.findViewById(R.id.chk_selected);
            ll_select_groups = popupView.findViewById(R.id.ll_select_groups);
            ll_client_name = popupView.findViewById(R.id.ll_client_name);
            selectionGroupsView = popupView.findViewById(R.id.selectionGroups);
            selectionGroupsView.setVisibility(VISIBLE);
            rbMatterView = popupView.findViewById(R.id.rbMatter);
            rbMatterView.setChecked(true);
            rbClientView = popupView.findViewById(R.id.rbClient);
            linearLayout2 = popupView.findViewById(R.id.linearLayout2);
            ll_select_grp = popupView.findViewById(R.id.ll_select_grp);
            constraint_root = popupView.findViewById(R.id.constraint_root);
            TextView document_upload = popupView.findViewById(R.id.document_upload);
            selected_client_groups_list.clear();
            matter_id = "";
            btn_upload_new.setText(R.string.upload);
            rv_display_upload_groups_docs.setBackground(context_type.getDrawable(R.drawable.rectangle_light_grey));
            callLegalMatter();
            //Refresh Selected Client List
            selectedClients = new ArrayList<>();
            ll_client_name.setVisibility(GONE);
            tv_client_name.setText(R.string.client_name);
            builder.setView(popupView);
            dialog = builder.create();
            dialog.show();
            btn_upload_new.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((!client_id.isEmpty()) || (!tv_select_groups.getText().toString().isEmpty()))
                        if (Constants.isGmail) {
                            callUploadDocument(baseUrl, token, Constants.msg_id, Constants.part_id);
                        } else {
                            callUploadDocument(Constants.EMAIL_GET_URL, token, Constants.msg_id, Constants.part_id);
                        }
                }
            });
            btn_cancel_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            tv_select_upload_groups.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ischeckedClient_group) {
//                        if (client_groups_list.isEmpty()) {
//                            callClientGroupsWebservice();
//                        } else {
//                            GroupsPopup(ll_upload_client_group, client_groups_list, selected_client_groups_list, rv_upload_groups, tv_select_upload_groups);
//                        }
                        ll_upload_client_group.setVisibility(View.VISIBLE);
                    } else {
                        ll_upload_client_group.setVisibility(GONE);
                    }
                    ischeckedClient_group = !ischeckedClient_group;
                }
            });
            rbMatterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    client_id = "";
                    matter_id = "";
                    callLegalMatter();
                    ll_client_name.setVisibility(GONE);
                    ll_matter_view.setVisibility(VISIBLE);
                    client_namee.setText(R.string.matter);
//                    tv_client_doc.setText(R.string.list_of_matter_documents);
//                    ismatter_chosen = true;
//                    view_docs_list.clear();
//                    rv_display_view_docs.removeAllViews();
//                    rv_display_view_docs.setVisibility(GONE);
//                    ll_page_navigaiton.setVisibility(GONE);
//                    hideMatterView();
                }
            });
            rbClientView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    client_id = "";
                    matter_id = "";
                    matterlist.clear();
                    client_name = "";
                    ll_matter_view.setVisibility(GONE);
                    client_namee.setText(R.string.client);
                    ll_client_name.setVisibility(VISIBLE);
                    callClientWebservice();
                }
            });
            client_namee.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectionGroupsView.setVisibility(VISIBLE);
                    ll_select_groups.setVisibility(View.GONE);
                    ll_client_name.setVisibility(GONE);
                    ll_select_grp.setVisibility(View.GONE);
                    custom_client.setText("");
                    tv_select_groups.setText("");
                    selected_groups_list.clear();
                    groupsList.clear();
                    // Set background color
                    client_namee.setBackgroundDrawable(context_type.getResources().getDrawable(R.drawable.button_left_green_background));// Assuming "green" is the desired color resource
                    client_namee.setTextColor(ContextCompat.getColor(context_type, R.color.white));
                    firm_namee.setBackgroundDrawable(context_type.getResources().getDrawable(R.drawable.button_right_background));// Assuming "green" is the desired color resource
                    firm_namee.setTextColor(ContextCompat.getColor(context_type, R.color.black));// Assuming "black" is the desired color resource
                    if (!"solo".equals(Constants.CATEGORY)) {
                        firm_namee.setVisibility(VISIBLE);
                    } else {
                        client_namee.setTextColor(ContextCompat.getColor(context_type, R.color.white));
                        client_namee.setBackgroundDrawable(context_type.getResources().getDrawable(R.drawable.rectangular_button_green_count));
                        firm_namee.setVisibility(GONE);
                    }
//                    ll_matter.setVisibility(VISIBLE);
                    ll_matter_view.setVisibility(VISIBLE);
                    ll_upload_groups.setVisibility(GONE);
                    dropdown_icon.setVisibility(View.VISIBLE);
                    client_namee.setText(R.string.matter);
                    clear_icon.setVisibility(GONE);
                    matterName = "";
                    matter_id = "";
                    custom_matter.setText("");
                    dropdown_icon2.setVisibility(View.VISIBLE);
                    clear_icon2.setVisibility(GONE);
                    rbMatterView.setChecked(true);
                    callLegalMatter();
                }
            });

            firm_namee.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectionGroupsView.setVisibility(GONE);
                    ll_client_name.setVisibility(View.GONE);
                    ll_matter_view.setVisibility(GONE);
                    ll_select_groups.setVisibility(View.VISIBLE);
                    ll_select_grp.setVisibility(View.VISIBLE);
                    custom_client.setText("");
                    tv_select_groups.setText("");
                    selected_groups_list.clear();
                    matter_id = "";
                    rbMatterView.setChecked(true);
                    client_namee.setText(R.string.matter);
                    groupsList.clear();
                    firm_namee.setBackgroundDrawable(context_type.getResources().getDrawable(R.drawable.button_right_green_background));
                    firm_namee.setTextColor(Color.WHITE);
                    client_namee.setBackgroundDrawable(context_type.getResources().getDrawable(R.drawable.button_left_background));// Assuming "green" is the desired color resource
// Assuming "green" is the desired color resource
                    ll_upload_groups.setVisibility(GONE);
                    ll_matter_view.setVisibility(View.GONE);
                    client_namee.setTextColor(ContextCompat.getColor(context_type, R.color.black));
                }
            });
            ll_custom_client.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clientsList.isEmpty()) {
                        list_client.setVisibility(GONE);
                    } else {
                        AndroidUtils.display_listview(ischecked, list_client);
                    }
                    ischecked = !ischecked;
                }
            });
            ll_matter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    callClientWebservice();
                    AndroidUtils.display_listview(ischecked_matter, list_matter);
                    ischecked_matter = !ischecked_matter;
                }
            });
            clear_icon2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    matter_id = "";
                    matterName = "";
                    selected_client_groups_list.clear();
//                    callClientGroupsWebservice();
                    AndroidUtils.DisplaySpinnerView(list_matter, custom_matter, matterName, dropdown_icon2, clear_icon2, false);
                }
            });
            clear_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    client_id = "";
                    selected_client_groups_list.clear();
                    matter_id = "";
                    ll_matter_view.setVisibility(GONE);
                    ll_upload_groups.setVisibility(GONE);
                    AndroidUtils.DisplaySpinnerView(list_client, custom_client, client_name, dropdown_icon, clear_icon, false);
                }
            });
            tv_select_groups.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ischecked_group) {
                        if (groupsList.isEmpty()) {
                            callGroupsWebservice();
                        }
                        rv_display_upload_groups_docs.setVisibility(View.VISIBLE);
                        linearLayout2.setVisibility(View.VISIBLE);
                    } else {
                        rv_display_upload_groups_docs.setVisibility(View.GONE);
                        linearLayout2.setVisibility(View.GONE);
                    }

                    ischecked_group = !ischecked_group;
                }
            });


        }

        private void callClientGroupsWebservice() {
            try {
                progress_dialog = AndroidUtils.get_progress((Activity) context_type);
                JSONObject jsonObject = new JSONObject();
                JSONArray ClientGroups = new JSONArray();
                JSONObject clients = new JSONObject();
                for (int j = 0; j < clientsList.size(); j++) {
                    if (clientsList.get(j).getId().matches(client_id)) {
                        ClientsModel clientsModel = clientsList.get(j);
                        clients.put("id", clientsModel.getId());
                        clients.put("type", clientsModel.getType());
                        ClientGroups.put(clients);
                    }
                }
                jsonObject.put("clients", ClientGroups);
                jsonObject.put("matterid", matter_id);
//            {"clients":[{"id":"5f732a91181e96216230123a","type":"consumer"}],"matterid":""}
                WebServiceHelper.callHttpWebService(this, context_type, WebServiceHelper.RestMethodType.PUT, "v3/documents/groupslist", "Client Groups", jsonObject.toString());
            } catch (Exception e) {
                if (progress_dialog != null && progress_dialog.isShowing()) {
                    AndroidUtils.dismiss_dialog(progress_dialog);
                }
            }
        }

        public void callUploadDocument(String uploadUrl, String token, String msgId, String partId) {
            try {
                progress_dialog = AndroidUtils.get_progress((Activity) context_type);
                try {
                    JSONArray clients = new JSONArray();
                    for (ClientsModel clientsModel : selectedClients) {
                        if (clientsModel.getId().equals(client_id)) {
                            JSONObject jsonObject_client = new JSONObject();
                            jsonObject_client.put("id", clientsModel.getId());
                            jsonObject_client.put("type", clientsModel.getType());
                            clients.put(jsonObject_client);
                        }
                    }

                    JSONArray groups = new JSONArray();
                    for (DocumentsModel documentsModel : selected_groups_list) {
                        String groupId = documentsModel.getGroup_id();
                        if (groupId != null && !groupId.isEmpty()) {
                            groups.put(groupId);
                        }
                    }

                    JSONObject jsonObject = new JSONObject();
                    String category = "";
                    if (clients.length() > 0) {
                        category = "client";
                    } else {
                        category = "firm";
                    }
                    JSONArray matter = new JSONArray();
                    if (matter_id != null && !matter_id.equals("")) {
                        matter.put(matter_id);
                        matter.put(matter_id);
                    }
                    jsonObject.put("category", category);
                    jsonObject.put("clientids", clients);
                    jsonObject.put("matters", matter);

                    jsonObject.put("groupids", groups);
                    jsonObject.put("enableDownload", true);

                    Log.e("Generated JSON", jsonObject.toString());

                    String url = uploadUrl + Constants.mail_document + token + "/" + msgId + "?partid=" + partId;


                    WebServiceHelper.callEmailHttpWebService(this, context_type, WebServiceHelper.RestMethodType.POST, url, "uploaded file", jsonObject.toString());
                    Log.d("json_value", url);

                } catch (Exception e) {
                    Log.e("callUploadDocument", "Error occurred while constructing request or sending request: " + e.getMessage());
                    e.fillInStackTrace();
                }
            } catch (Exception e) {
                Log.e("callUploadDocument", "Error occurred while executing callUploadDocument: " + e.getMessage());
                if (progress_dialog != null && progress_dialog.isShowing()) {
                    AndroidUtils.dismiss_dialog(progress_dialog);
                }
                e.fillInStackTrace();
            }
        }


        private void callClientWebservice() {
            try {
                progress_dialog = AndroidUtils.get_progress((Activity) context_type);
                JSONObject jsonObject = new JSONObject();
                WebServiceHelper.callHttpWebService(this, context_type, WebServiceHelper.RestMethodType.GET, "v3/client/all/list", "Clients List", jsonObject.toString());
            } catch (Exception e) {
                if (progress_dialog != null && progress_dialog.isShowing()) {
                    AndroidUtils.dismiss_dialog(progress_dialog);
                }
            }
        }

        private void callGroupsWebservice() {
            try {
                progress_dialog = AndroidUtils.get_progress((Activity) context_type);
                JSONObject jsonObject = new JSONObject();
                WebServiceHelper.callHttpWebService(this, context_type, WebServiceHelper.RestMethodType.GET, "v3/groups", "Groups", jsonObject.toString());
            } catch (Exception e) {
                if (progress_dialog != null && progress_dialog.isShowing()) {
                    AndroidUtils.dismiss_dialog(progress_dialog);
                }
            }
        }

        //        private void loadClients(JSONObject data) throws JSONException {
//            JSONArray relationships = data.getJSONArray("relationships");
//            clientsList.clear();
//            for (int i = 0; i < relationships.length(); i++) {
//                JSONObject jsonObject = relationships.getJSONObject(i);
//                ClientsModel clientsModel = new ClientsModel();
//                clientsModel.setId(jsonObject.getString("id"));
//                clientsModel.setName(jsonObject.getString("name"));
//                clientsModel.setType(jsonObject.getString("type"));
//                clientsList.add(clientsModel);
//            }
//            if (clientsList.isEmpty()) {
//                ll_client_name.setVisibility(GONE);
//            } else {
//                initUI(clientsList);
//            }
//        }
        private void loadClients(JSONObject data) throws JSONException {
            JSONArray relationships = data.getJSONArray("relationships");
            //Adding a list first value as empty...
//        clientsList.add(0, new ClientsModel());
            clientsList.clear();
            for (int i = 0; i < relationships.length(); i++) {
                JSONObject jsonObject = relationships.getJSONObject(i);
                ClientsModel clientsModel = new ClientsModel();
                clientsModel.setId(jsonObject.getString("id"));
                clientsModel.setName(jsonObject.getString("name"));
                clientsModel.setType(jsonObject.getString("type"));
                clientsList.add(clientsModel);
//                    updatedClients.add(clientsModel);
            }
            // intUI(clientsList);
        }

        private void loadCorpClients(JSONObject data) throws JSONException {
            JSONArray relationships = data.getJSONArray("relationships");
            //Adding a list first value as empty...
//        clientsList.add(0, new ClientsModel());
            CorpClientsList.clear();
            for (int i = 0; i < relationships.length(); i++) {
                JSONObject jsonObject = relationships.getJSONObject(i);
                ClientsModel clientsModel = new ClientsModel();
                clientsModel.setId(jsonObject.getString("id"));
                clientsModel.setName(jsonObject.getString("name"));
                if (!jsonObject.getString("type").equals("consumer")) {
                    clientsModel.setType("corporate");
                }
                CorpClientsList.add(clientsModel);
//                    updatedClients.add(clientsModel);
            }
            clientsList.addAll(CorpClientsList);
            if (clientsList.isEmpty()) {
                ll_client_name.setVisibility(GONE);
            } else {
                initUI(clientsList);
            }
            // intUI(clientsList);
        }

//        private void loadGroupsData(JSONArray data) {
//            try {
//                groupsList.clear();
//                for (int i = 0; i < data.length(); i++) {
//                    JSONObject jsonObject = data.getJSONObject(i);
//                    DocumentsModel documentsModel = new DocumentsModel();
//                    if ((!jsonObject.getString("name").equals("AAM")) && ((!jsonObject.getString("name").equals("SuperUser")))) {
//                        documentsModel.setGroup_id(jsonObject.getString("id"));
//                        documentsModel.setGroup_name(jsonObject.getString("name"));
//                        groupsList.add(documentsModel);
//                    }
//                }
//                selectedLanguage = new boolean[groupsList.size()];

        /// /            GroupsAlert();
//                GroupsPopup();
//            } catch (JSONException e) {
//                e.fillInStackTrace();
//                AndroidUtils.showAlert(e.getMessage(), email.getActivity());
//            }
//        }
        private void loadGroupsData(JSONArray data, ArrayList<DocumentsModel> groupsList, boolean isclient_groups) {
            groupsList.clear();
            selected_client_groups_list.clear();
            try {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject jsonObject = data.getJSONObject(i);
                    DocumentsModel documentsModel = new DocumentsModel();
                    documentsModel.setGroup_id(jsonObject.optString("id"));
                    documentsModel.setGroup_name(jsonObject.optString("name"));
                    if ((!jsonObject.optString("name").equals("AAM")) && ((!jsonObject.optString("name").equals("SuperUser")))) {
                        documentsModel.setGroup_id(jsonObject.optString("id"));
                        documentsModel.setGroup_name(jsonObject.optString("name"));
                        if (isclient_groups) {
                            documentsModel.setGroupChecked(true);
                            selected_client_groups_list.add(documentsModel);
                        }
                        groupsList.add(documentsModel);
                    }
                }
                if (!selected_client_groups_list.isEmpty()) {
                    ll_upload_groups.setVisibility(View.VISIBLE);
                } else {
                    ll_upload_groups.setVisibility(GONE);
                }
                selectedLanguage = new boolean[groupsList.size()];
                if (isclient_groups) {
//                ll_upload_groups.setVisibility(View.VISIBLE);
                    GroupsPopup(rv_upload_groups, client_groups_list, selected_client_groups_list, rv_upload_groups, tv_select_upload_groups);
                } else {
                    GroupsPopup(ll_select_groups, groupsList, selected_groups_list, rv_display_upload_groups_docs, tv_select_groups);
                }
            } catch (
                    JSONException e) {
                e.fillInStackTrace();
                AndroidUtils.showAlert(e.getMessage(), (Activity) context_type);
            }
        }

        @SuppressLint("MissingInflatedId")
        private void GroupsPopup(View upload_group_layout, ArrayList<DocumentsModel> groupsList, ArrayList<DocumentsModel> selected_groups_list, RecyclerView rv_display_upload_groups_docs, TextView tv_select_groups) {
            tv_select_groups.setText("");
            upload_group_layout.setVisibility(View.VISIBLE);
            try {
                for (int i = 0; i < groupsList.size(); i++) {
                    for (int j = 0; j < selected_groups_list.size(); j++) {
                        if (groupsList.get(i).getGroup_id().matches(selected_groups_list.get(j).getGroup_id())) {
                            DocumentsModel documentsModel = groupsList.get(i);
                            documentsModel.setChecked(true);
                        }
                    }
                }

                // Upload Documents Group Selection
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context_type, LinearLayoutManager.VERTICAL, false);
                rv_display_upload_groups_docs.setLayoutManager(layoutManager);
                //rv_display_upload_groups_docs.setHasFixedSize(true);
//            selected_groups_list.clear();
                GroupsListAdapter documentsAdapter = new GroupsListAdapter(groupsList, Documents.class.newInstance(), new GroupsListAdapter.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(DocumentsModel documentsModel) {
                        if (documentsModel.isGroupChecked()) {
                            selected_groups_list.add(documentsModel);
                        } else {
                            // Remove the unchecked item from selected_groups_list
                            for (int i = 0; i < selected_groups_list.size(); i++) {
                                if (selected_groups_list.get(i).getGroup_id().equals(documentsModel.getGroup_id())) {
                                    selected_groups_list.remove(i);

                                    break; // Exit loop after removing the item
                                }
                            }
                        }
                        tv_select_groups.setText("");
                        // Update TextView with selected groups
                        String[] value = new String[selected_groups_list.size()];
                        for (int i = 0; i < selected_groups_list.size(); i++) {
                            value[i] = selected_groups_list.get(i).getGroup_name();
                        }
                        String str = TextUtils.join(",", value);
                        tv_select_groups.setText(str);
                    }
                });
                // Update TextView with selected groups
                String[] value = new String[selected_groups_list.size()];
                for (int i = 0; i < selected_groups_list.size(); i++) {
                    value[i] = selected_groups_list.get(i).getGroup_name();
                }
                String str = TextUtils.join(",", value);
                tv_select_groups.setText(str);
                rv_display_upload_groups_docs.setAdapter(documentsAdapter);
                AndroidUtils.LoadList(rv_display_upload_groups_docs, context_type, groupsList.size(), true);

//            // View Documents Group Selection
//            try {
//                RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
//                rv_display_view_groups_docs.setLayoutManager(layoutManager1);
//                rv_display_view_groups_docs.setHasFixedSize(true);
//
//                GroupsListAdapter documentsAdapter1 = new GroupsListAdapter(groupsList, Documents.class.newInstance(), new GroupsListAdapter.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(DocumentsModel documentsModel) {
//                        // Update selected groups list
//                        if (documentsModel.isGroupChecked()) {
//                            selected_groups_list.add(documentsModel);
//                            rv_display_view_docs.setVisibility(View.VISIBLE);
//                        } else {
//                            // Remove the unchecked item from selected_groups_list
//                            for (int i = 0; i < selected_groups_list.size(); i++) {
//                                if (selected_groups_list.get(i).getGroup_id().equals(documentsModel.getGroup_id())) {
//                                    selected_groups_list.remove(i);
//                                    break; // Exit loop after removing the item
//                                }
//                            }
//                        }
//                        // Update TextView with selected groups
//                        String[] value = new String[selected_groups_list.size()];
//                        String[] value_id = new String[selected_groups_list.size()];
//                        for (int i = 0; i < selected_groups_list.size(); i++) {
//                            value[i] = selected_groups_list.get(i).getGroup_name();
//                            value_id[i] = selected_groups_list.get(i).getGroup_id();
//                        }
//
//                        try {
//                            array_group = new JSONArray(value_id);
//                            callfilter_client_webservices();
//                        } catch (JSONException e) {
//                            throw new RuntimeException(e);
//                        }
//
//                        String str = TextUtils.join(",", value);
//                        tv_select_groups_view.setText(str);
//                        if (tv_select_groups_view.getText().toString().isEmpty()) {
//                            rv_display_view_docs.setVisibility(View.GONE);
//                        }
////                        view_group_layout.setVisibility(View.GONE);
////                        ischecked_group_view = true;
//                        EnableUpload();
//                    }
//                });
//                rv_display_view_groups_docs.setAdapter(documentsAdapter1);
//            } catch (Exception e) {
//                e.fillInStackTrace();
//                AndroidUtils.showAlert(e.getMessage(), getActivity());
//            }
            } catch (IllegalAccessException | java.lang.InstantiationException e) {
                throw new RuntimeException(e);
            }
        }

        private void callLegalMatter() {
            try {
                progress_dialog = AndroidUtils.get_progress((Activity) context_type);
                JSONObject jsonObject = new JSONObject();
//                Log.d("Client_id", client_id);
                WebServiceHelper.callHttpWebService(this, context_type, WebServiceHelper.RestMethodType.GET, "v2/matter/list", "Legal Matter", jsonObject.toString());

            } catch (Exception e) {
                if (progress_dialog != null && progress_dialog.isShowing()) {
                    AndroidUtils.dismiss_dialog(progress_dialog);
                }
                e.fillInStackTrace();
            }
        }

        private void loadMatters(JSONArray matters) throws JSONException {
            //Adding a list first value as empty...
//        matterlist.add(0, new MattersModel());
            matterlist.clear();
//            MattersModel mattersModel1 = new MattersModel();
//            mattersModel1.setId("all");
//            mattersModel1.setTitle("All Documents");
//            mattersModel1.setType("consumer");
//            matterlist.add(mattersModel1);
            for (int i = 0; i < matters.length(); i++) {
                JSONObject jsonObject = matters.getJSONObject(i);
                MattersModel mattersModel = new MattersModel();
                mattersModel.setId(jsonObject.optString("id"));
                mattersModel.setTitle(jsonObject.optString("title"));
                mattersModel.setType(jsonObject.optString("type"));
                matterlist.add(0, mattersModel);
            }
            if (matters.length() == 0) {
                ll_matter_view.setVisibility(View.GONE);
                ll_matter.setVisibility(View.GONE);
            } else {
                ll_matter_view.setVisibility(View.VISIBLE);
                ll_matter.setVisibility(View.VISIBLE);
            }
            initMatter();
        }

        private void initMatter() {
            CommonSpinnerAdapter adapter = new CommonSpinnerAdapter((Activity) context_type, matterlist);
            list_matter.setAdapter(adapter);
            list_matter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    matter_id = "";
                    matter_id = matterlist.get(position).getId();
                    matterName = matterlist.get(position).getTitle();
                    Log.d("Matter_value_name", matterName);
                    //Displaying the chosen client name
                    selected_client_groups_list.clear();
//                    callClientGroupsWebservice();
                    AndroidUtils.DisplaySpinnerView(list_matter, custom_matter, matterName, dropdown_icon2, clear_icon2, true);
                    ischecked_matter = true;
                }
            });
        }

        private void initUI(ArrayList<ClientsModel> clientsList) {
            CommonSpinnerAdapter adapter = new CommonSpinnerAdapter((Activity) context_type, clientsList);
            list_client.setAdapter(adapter);
            list_client.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    client_id = "";
                    client_id = clientsList.get(position).getId();
                    client_name = clientsList.get(position).getName();
                    Log.d("Client_value_name", client_name);
                    if (!selectedClients.contains(clientsList.get(position))) {
                        selectedClients.add(clientsList.get(position));
                    }
                    matterlist.clear();
                    selected_client_groups_list.clear();
//                    callClientGroupsWebservice();
                    //Displaying the chosen client name
                    AndroidUtils.DisplaySpinnerView(list_matter, custom_matter, matterName, dropdown_icon2, clear_icon2, false);
                    AndroidUtils.DisplaySpinnerView(list_client, custom_client, client_name, dropdown_icon, clear_icon, true);

                    ischecked = true;
                }
            });
        }

        @Override
        public void onClick(View view) {

        }

        private void callCorpClientWebservice() {
            try {
//            progress_dialog = AndroidUtils.get_progress(getActivity());
                JSONObject jsonObject = new JSONObject();
                WebServiceHelper.callHttpWebService(this, context_type, WebServiceHelper.RestMethodType.GET, "v3/corporate/list", "Corp Clients List", jsonObject.toString());
            } catch (Exception e) {
                if (progress_dialog != null && progress_dialog.isShowing()) {
                    AndroidUtils.dismiss_dialog(progress_dialog);
                }
            }
        }

        public void onAsyncTaskComplete(HttpResultDo httpResult) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    String requestType = httpResult.getRequestType();

                    if ("Clients List".equals(requestType)) {
                        JSONObject data = result.getJSONObject("data");
                        try {
                            callCorpClientWebservice();
                            loadClients(data);
                        } catch (Exception e) {
                            e.fillInStackTrace();
                        }
                    } else if (httpResult.getRequestType().equals("Corp Clients List")) {
                        try {
                            loadCorpClients(result);
                        } catch (Exception e) {
                            e.fillInStackTrace();
                        }
                    } else if (httpResult.getRequestType().equals("Groups")) {
                        JSONArray data = result.getJSONArray("data");
                        loadGroupsData(data, groupsList, false);
                    } else if (httpResult.getRequestType().equals("Client Groups")) {
//                        callLegalMatter();
                        JSONArray data = result.getJSONArray("data");
                        loadGroupsData(data, client_groups_list, true);
                    } else if (httpResult.getRequestType().equals("Legal Matter")) {
                        JSONArray matters = result.getJSONArray("matters");
                        loadMatters(matters);

                    } else if (httpResult.getRequestType().equals("uploaded file")) {
                        if (result.has("message")) {
                            String message = result.optString("message");
                            AndroidUtils.showAlert_docs("Success!", message, email.getActivity());
                        } else {
                            String msg = result.getString("msg");
                            AndroidUtils.showToast(msg, context_type);
                        }
                        dialog.dismiss();
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    String requestType = httpResult.getRequestType();
                    if (httpResult.getRequestType().equals("uploaded file")) {
                        if (result.has("message")) {
                            String message = result.optString("message");
                            AndroidUtils.showAlert_docs("Success!", message, email.getActivity());
                        } else {
                            String msg = result.getString("msg");
                            AndroidUtils.showToast(msg, context_type);
                        }
                        dialog.dismiss();
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}




