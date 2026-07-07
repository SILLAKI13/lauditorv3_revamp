package com.digicoffer.lauditor.Email;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.check_url;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.isGmail;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.isGoogle;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.isValidEmail;
import static com.digicoffer.lauditor.Email.EmailAdapter.context_type;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Documents.Documents;
import com.digicoffer.lauditor.Documents.DocumentsListAdpater.GroupsListAdapter;
import com.digicoffer.lauditor.Documents.Models.ClientsModel;
import com.digicoffer.lauditor.Documents.Models.DocumentsModel;
import com.digicoffer.lauditor.Documents.Models.MattersModel;
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class Email extends Fragment implements AsyncTaskCompleteListener {

    private static final int AUTH_REQUEST_CODE = 1001;
    ArrayList<ClientsModel> CorpClientsList = new ArrayList<>();
    private String URL = "";
    String msgId = "";
    EmailAdapter adapter;
    private static final int VIEW_TYPE_DOCUMENT = 0;
    private static final int VIEW_TYPE_ATTACHMENT = 1;
    String matter_id = "";
    boolean ischeckedClient_group = true;
    boolean ISCHECK_EMAIL = false;
    String toEmail = "";
    String subject = "";
    Email email;
    EmailModel emailModel = new EmailModel();
    ArrayList<MattersModel> matterlist = new ArrayList<>();
    String body = "";
    List<String> documentFilenames = new ArrayList<>();

    private String selectedDocumentName = "";
    int doc_count = 0;
    boolean ischecked_group = true;
    boolean[] selectedLanguage;
    Button btn_group_cancel, btn_group_submit, btn_continue;
    AppCompatButton btn_create, btn_cancel_save;
    //    LinearLayout upload_group_layout;
    TextInputEditText et_Search_email_document;
    TextView tv_select_groups, tv_select_upload_group_name, tv_select_upload_groups;
    ArrayList<DocumentsModel> selected_groups_list = new ArrayList<DocumentsModel>();
    ArrayList<ViewDocumentsModel> view_docs_list = new ArrayList<>();
    boolean ISCHECK_AUTH = false;
    boolean ischeck_label, ischeck_auth;
    String nextPageToken = "";
    LinearLayout ll_attach_grp;

    ArrayList<DocumentsModel> groupsList = new ArrayList<>();
    ArrayList<DocumentsModel> client_groups_list = new ArrayList<>();
    ArrayList<DocumentsModel> selected_client_groups_list = new ArrayList<>();
    ImageView arrow_left;
    ImageView clear_search;
    boolean ischecked = true, ischecked_matter = true;
    private TextView custom_client, custom_matter;
    private LinearLayout ll_custom_client;
    ImageView dropdown_icon, clear_icon, dropdown_icon2, clear_icon2;
    private int currentPosition = 1;
    JSONArray attachmentsArray = initializeAttachmentsArray();


    AlertDialog composeDialog;

    private JSONArray initializeAttachmentsArray() {

        JSONArray attachmentsArray = new JSONArray();

        // Assuming you have some attachment data to add
        try {
            // Add some attachment data to the array
            JSONObject attachment1 = new JSONObject();
            attachment1.put("document_name", "Document 1");
            attachment1.put("name", "attachment1.txt");
            attachment1.put("size", "10 KB");
            attachmentsArray.put(attachment1);

            JSONObject attachment2 = new JSONObject();
            attachment2.put("document_name", "Document 2");
            attachment2.put("name", "attachment2.pdf");
            attachment2.put("size", "100 KB");
            attachmentsArray.put(attachment2);

            // Add more attachments as needed
        } catch (JSONException e) {
            e.fillInStackTrace();
        }

        return attachmentsArray;
    }


    TextInputEditText et_Search;
    RecyclerView rv_documents_email;
    boolean ischecked_group_view;
    ArrayList<ClientsModel> clientsList = new ArrayList<>();

    public static Dialog progress_dialog;
    Stack<List<MessageModel>> pageStack = new Stack<>();

    boolean emailcheck;
    private List<MessageModel> messages = new ArrayList<>();
    private List<List<MessageModel>> totalMessageArray = new ArrayList<>();
    List<List<MessageModel>> nextMessages = new ArrayList<>();
    Integer pre_next_position = 0;
    private String requestType = null;
    HttpURLConnection httpURLConnection = null;
    TextView inbox_textViews;
    AppCompatButton first_button, search_email, sends_button;
    EditText to_input, subject_input, message_inputs;
    ListView client_list_view, list_matter;
    TextView grp_name, matter_name;
    RadioButton rb_google, rb_outlook;
    String client_id = "";
    String clientname = "", matterName = "";
    RecyclerView rv_display_upload_groups_docs, rv_upload_groups;
    String CATEGORY_TAG = "";
    LinearLayout linearLayout2, ll_mail_provider, ll_matter, ll_matter_view;
    JSONArray array_group = new JSONArray();
    GridView yourGridView;
    ImageView composeDocuments;

    LinearLayout overlay, ll_upload_groups, ll_upload_client_group;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.email_layout, container, false);
        composeDocuments = view.findViewById(R.id.compose);
        composeDocuments.setAlpha(0.3f);
        btn_continue = view.findViewById(R.id.btn_continue);
        btn_continue.setText(R.string.continue_);
        rb_google = view.findViewById(R.id.rb_google);
        rb_google.setText(R.string.google);
        rb_outlook = view.findViewById(R.id.rb_outlook);
        rb_outlook.setText(R.string.outlook);
        ll_mail_provider = view.findViewById(R.id.ll_mail_provider);
        ll_mail_provider.setVisibility(VISIBLE);
        isGmail = true;
        ImageView arrow_right = view.findViewById(R.id.arrow_right);
        ImageView arrow_left = view.findViewById(R.id.arrow_left);
        arrow_left.setAlpha(0.3f);
        clear_search = view.findViewById(R.id.clear_search);
        clear_search.setVisibility(GONE);
        inbox_textViews = view.findViewById(R.id.inbox_textViews);
        first_button = view.findViewById(R.id.first_button);
        first_button.setAlpha(0.3f);
        et_Search = view.findViewById(R.id.et_Search);
        et_Search.addTextChangedListener(new Validation(et_Search));
        email = this;
        search_email = view.findViewById(R.id.search_email);
        search_email.setAlpha(0.3f);
        sends_button = view.findViewById(R.id.sends_button);
        overlay = view.findViewById(R.id.overlay);
        //...

        composeDocuments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Constants.is_active) {
                    AndroidUtils.showRenewalPopup(getActivity());
                } else {
                    Constants.composAttachDocAry = new ArrayList<>();
                    openComposePopup();
                }
            }
        });
        first_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!totalMessageArray.isEmpty()) {
                    pre_next_position = 0;
                    updateRecyclerView(totalMessageArray.get(pre_next_position));
                    arrow_left.setAlpha(0.3f);
                    first_button.setAlpha(0.3f);
                    arrow_right.setAlpha(1.0f);
                }
            }
        });

        rb_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb_outlook.setChecked(false);
                isGmail = true;
            }
        });
        rb_outlook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb_google.setChecked(false);
                isGmail = false;
            }
        });
        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check_url();
                callLabel();
                ll_mail_provider.setVisibility(GONE);
            }
        });
        arrow_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isMorePagesAvailable()) {

                    pre_next_position++;
                    callMessageListnext();
                    arrow_left.setAlpha(1.0f);
                    first_button.setAlpha(1.0f);

                } else {

                    Toast.makeText(getContext(), "No more pages available", Toast.LENGTH_SHORT).show();
                }
            }

        });


        arrow_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pre_next_position > 0) {
                    pre_next_position--;
                    List<MessageModel> previousPageMessages = totalMessageArray.get(pre_next_position);
                    updateRecyclerView(previousPageMessages);

                    // If going back to the first page, disable the first_button and left arrow
                    if (pre_next_position == 0) {
                        first_button.setAlpha(0.3f);
                        arrow_left.setAlpha(0.3f);
                        Toast.makeText(getContext(), "You are already on the first page", Toast.LENGTH_SHORT).show();
                    } else {
                        // Otherwise, enable them
                        first_button.setAlpha(1.0f);
                        arrow_left.setAlpha(1.0f);
                    }
                } else {
//                    Toast.makeText(getContext(), "You are already on the first page", Toast.LENGTH_SHORT).show();
                    // No need to set alpha here since it's already handled when pre_next_position is 0
                }
            }
        });


//        emaiAPI();
//        callLabel();


        return view;
    }

    private boolean isMorePagesAvailable() {

        return nextPageToken != null && !nextPageToken.isEmpty();
    }


    @SuppressLint("MissingInflatedId")
    private void openComposePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.compose, null);
        ViewDocumentsModel viewDocumentsModel = new ViewDocumentsModel();
        ImageView attachmentImageView = view.findViewById(R.id.attachments);
        ImageView cross_icon = view.findViewById(R.id.close_comp_id);
        to_input = view.findViewById(R.id.to_input);
        subject_input = view.findViewById(R.id.subject_input);
        message_inputs = view.findViewById(R.id.message_inputss);
        sends_button = view.findViewById(R.id.sends_button);
        overlay.setVisibility(VISIBLE);
        yourGridView = view.findViewById(R.id.compose_gridview);
        yourGridView.setVisibility(GONE);

        sends_button.setAlpha(0.4f);
//        builder.getContext().getTheme().applyStyle(R.style.MyAlertDialog, true);
        builder.setView(view);
        composeDialog = builder.create();
        composeDialog.setCanceledOnTouchOutside(false);
//        composeDialog.getWindow().setLayout(500, 1000);
//        composeDialog.getWindow().setBackgroundDrawableResource(android.R.color.darker_gray);
        composeDialog.show();
        if (Constants.ROLE.equals("AAM")) {
            attachmentImageView.setVisibility(GONE);
        } else {
            attachmentImageView.setVisibility(VISIBLE);
        }
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                validateEmail();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        };
        to_input.addTextChangedListener(textWatcher);
        subject_input.addTextChangedListener(textWatcher);
        message_inputs.addTextChangedListener(textWatcher);

        sends_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if ((!to_input.getText().toString().isEmpty()) && (!subject_input.getText().toString().isEmpty()) && (!message_inputs.getText().toString().isEmpty()) && (!Constants.composAttachDocAry.isEmpty())) {
//                if (!to_input.getText().toString().isEmpty()) {
//                    send_email();
//                }
//                else {
//                    AndroidUtils.showEmailAlert(getActivity(), email);
//                }
                if ((subject_input.getText().toString().isEmpty()) && (message_inputs.getText().toString().isEmpty())) {
                    AndroidUtils.showEmailAlert(getActivity(), email);
                } else {
                    send_email();
                }
            }
        });

        attachmentImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder attachmentDialogBuilder = new AlertDialog.Builder(getContext());
                LayoutInflater attachmentInflater = getActivity().getLayoutInflater();
                View attachmentView = attachmentInflater.inflate(R.layout.attach_document, null);
                TextView client_name = attachmentView.findViewById(R.id.client_name);
                client_name.setText(R.string.client_name);
                CATEGORY_TAG = "client";
                matter_name = attachmentView.findViewById(R.id.matter_name);
                matter_name.setText(R.string.matter);
                ll_matter = attachmentView.findViewById(R.id.ll_matter);
                ll_matter.setVisibility(GONE);
                RadioGroup selectionGroups;
                RadioButton rbMatter, rbClient;
                rbMatter = attachmentView.findViewById(R.id.rbMatter);
                rbMatter.setChecked(true);
                rbClient = attachmentView.findViewById(R.id.rbClient);
                selectionGroups = attachmentView.findViewById(R.id.selectionGroups);
                selectionGroups.setVisibility(VISIBLE);
                ll_matter_view = attachmentView.findViewById(R.id.ll_matter_view);
                ll_matter_view.setVisibility(VISIBLE);

                custom_matter = ll_matter.findViewById(R.id.tv_spinner_view);
                dropdown_icon2 = ll_matter.findViewById(R.id.img_dropdown_icon);
                clear_icon2 = ll_matter.findViewById(R.id.img_clear_icon);
                list_matter = attachmentView.findViewById(R.id.list_matter);

                ll_custom_client = attachmentView.findViewById(R.id.custom_client);
                custom_client = ll_custom_client.findViewById(R.id.tv_spinner_view);
                dropdown_icon = ll_custom_client.findViewById(R.id.img_dropdown_icon);
                clear_icon = ll_custom_client.findViewById(R.id.img_clear_icon);
                //...
//                tv_select_upload_group_name = attachmentView.findViewById(R.id.tv_select_upload_group_name);
//                tv_select_upload_group_name.setText(R.string.select_groups);
//                tv_select_upload_groups = attachmentView.findViewById(R.id.tv_select_upload_groups);
//                rv_upload_groups = attachmentView.findViewById(R.id.rv_upload_groups);
//                ll_upload_groups = attachmentView.findViewById(R.id.ll_upload_groups);
//                ll_upload_client_group = attachmentView.findViewById(R.id.ll_upload_client_group);
//                rv_upload_groups.setBackground(getContext().getDrawable(R.drawable.rectangle_light_grey_bg));

                TextView compose_client_name = attachmentView.findViewById(R.id.compose_client_name);
                compose_client_name.setText(R.string.matter);

                compose_client_name.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));// Assuming "green" is the desired color resource

                compose_client_name.setTextColor(ContextCompat.getColor(context_type, R.color.white));
                compose_client_name.setTextColor(ContextCompat.getColor(context_type, R.color.white));

                TextView compose_firm_name = attachmentView.findViewById(R.id.compose_firm_name);
                compose_firm_name.setText(R.string.firm);
                rv_documents_email = attachmentView.findViewById(R.id.rv_documents_email);
                rv_documents_email.setVisibility(GONE);
//                ScrollView list_scroll_view = attachmentView.findViewById(R.id.list_scroll_view);
                client_list_view = attachmentView.findViewById(R.id.client_list_view);
                rv_display_upload_groups_docs = attachmentView.findViewById(R.id.rv_display_upload_groups_docs);
                rv_display_upload_groups_docs.setBackground(getActivity().getDrawable(R.drawable.rectangle_light_grey));
                client_list_view.setVisibility(GONE);
                btn_group_cancel = attachmentView.findViewById(R.id.btn_group_cancel);
                btn_group_cancel.setVisibility(GONE);
//                upload_group_layout = attachmentView.findViewById(R.id.upload_group_layout);
                btn_group_submit = attachmentView.findViewById(R.id.btn_group_submit);
                btn_group_submit.setVisibility(GONE);
                tv_select_groups = attachmentView.findViewById(R.id.tv_select_groups);
                tv_select_groups.setVisibility(GONE);
                ll_attach_grp = attachmentView.findViewById(R.id.ll_attach_grp);
                grp_name = attachmentView.findViewById(R.id.grp_name);
                grp_name.setText(R.string.select_group);
                et_Search_email_document = attachmentView.findViewById(R.id.et_Search_email_document);
                LinearLayout ll_select_groups = attachmentView.findViewById(R.id.ll_select_groups);
                ll_select_groups.setVisibility(GONE);
                LinearLayout ll_client_name = attachmentView.findViewById(R.id.ll_client_name);
                linearLayout2 = attachmentView.findViewById(R.id.linearLayout2);
                btn_create = attachmentView.findViewById(R.id.btn_create);
                btn_cancel_save = attachmentView.findViewById(R.id.btn_cancel_save);
                btn_create.setText(R.string.attach);
                btn_create.setAlpha(0.5f);
                btn_create.setEnabled(false);
                ll_client_name.setVisibility(GONE);
                attachmentDialogBuilder.setView(attachmentView);
                AlertDialog attachmentDialog = attachmentDialogBuilder.create();
                attachmentDialog.show();
                callLegalMatter();
                custom_client.setHint(R.string.select_client_name);
                custom_matter.setHint(R.string.select_matters);
                tv_select_groups.setHint(R.string.select_groups);
                if (!"solo".equals(Constants.CATEGORY)) {
                    compose_firm_name.setVisibility(VISIBLE);
                } else {
                    compose_client_name.setTextColor(ContextCompat.getColor(context_type, R.color.white));
                    compose_client_name.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.rectangular_button_green_count));
                    compose_firm_name.setVisibility(GONE);
                }
                compose_client_name.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SuspiciousIndentation")
                    @Override
                    public void onClick(View v) {
                        CATEGORY_TAG = "client";
                        selectionGroups.setVisibility(VISIBLE);
                        ll_select_groups.setVisibility(GONE);
                        ll_matter_view.setVisibility(VISIBLE);
                        ll_client_name.setVisibility(GONE);
                        rbMatter.setChecked(true);
                        client_name.setText(R.string.matter);
                        custom_client.setText("");
                        custom_matter.setText("");
                        client_id = "";
                        clear_icon.setVisibility(GONE);
                        dropdown_icon.setVisibility(VISIBLE);

                        matter_id = "";
                        clear_icon2.setVisibility(GONE);
                        dropdown_icon2.setVisibility(VISIBLE);
                        view_docs_list.clear();
                        rv_documents_email.clearFocus();
                        ll_attach_grp.setVisibility(GONE);
                        rv_documents_email.setVisibility(GONE);
                        groupsList.clear();
                        client_list_view.setVisibility(GONE);
                        view_docs_list.clear();
                        rv_documents_email.clearFocus();
                        ll_attach_grp.setVisibility(GONE);
                        rv_documents_email.setVisibility(GONE);
//                        ll_upload_groups.setVisibility(GONE);
                        compose_client_name.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));// Assuming "green" is the desired color resource
                        compose_client_name.setTextColor(ContextCompat.getColor(context_type, R.color.white));
                        compose_firm_name.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));// Assuming "green" is the desired color resource
                        compose_firm_name.setTextColor(ContextCompat.getColor(context_type, R.color.black));
                        clear_icon.setVisibility(GONE);
                        if (!"solo".equals(Constants.CATEGORY)) {
                            compose_firm_name.setVisibility(VISIBLE);
                        } else {
                            compose_client_name.setTextColor(ContextCompat.getColor(context_type, R.color.white));
                            compose_client_name.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.rectangular_button_green_count));
                            compose_firm_name.setVisibility(GONE);
                        }
                        btn_create.setAlpha(0.5f);
                        btn_create.setEnabled(false);
                        callLegalMatter();
                    }
                });
                // Initialize and set the adapter
                compose_firm_name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectionGroups.setVisibility(GONE);
                        CATEGORY_TAG = "firm";
                        ll_client_name.setVisibility(GONE);
                        ll_matter_view.setVisibility(GONE);
                        ll_select_groups.setVisibility(VISIBLE);
                        view_docs_list.clear();
                        groupsList.clear();
                        clientsList.clear();
                        rv_documents_email.setVisibility(GONE);
                        tv_select_groups.setVisibility(VISIBLE);
                        client_list_view.setVisibility(GONE);
                        rv_display_upload_groups_docs.setVisibility(GONE);
//                        ll_upload_groups.setVisibility(GONE);
                        linearLayout2.setVisibility(GONE);
                        ischecked_group = true;
                        selected_groups_list.clear();
                        tv_select_groups.setText("");
                        rv_documents_email.clearFocus();
                        ll_attach_grp.setVisibility(VISIBLE);
                        linearLayout2.setVisibility(GONE);
                        btn_create.setAlpha(0.5f);
                        btn_create.setEnabled(false);
                        compose_firm_name.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_background));
                        compose_firm_name.setTextColor(Color.WHITE);
                        compose_client_name.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));// Assuming "green" is the desired color resource
// Assuming "green" is the desired color resource
                        compose_client_name.setTextColor(ContextCompat.getColor(context_type, R.color.black));
                    }
                });

                rbMatter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        client_id = "";
                        matter_id = "";
                        matterName = "";
                        callLegalMatter();
                        ll_client_name.setVisibility(GONE);
                        ll_matter_view.setVisibility(VISIBLE);
                        client_name.setText(R.string.matter);
                        custom_client.setText("");
                        client_id = "";
                        clear_icon.setVisibility(GONE);
                        dropdown_icon.setVisibility(VISIBLE);
                        view_docs_list.clear();
                        rv_documents_email.clearFocus();
                        ll_attach_grp.setVisibility(GONE);
                        rv_documents_email.setVisibility(GONE);
//                    tv_client_doc.setText(R.string.list_of_matter_documents);
//                    ismatter_chosen = true;
//                    view_docs_list.clear();
//                    rv_display_view_docs.removeAllViews();
//                    rv_display_view_docs.setVisibility(GONE);
//                    ll_page_navigaiton.setVisibility(GONE);
//                    hideMatterView();
                    }
                });
                rbClient.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        client_id = "";
                        matter_id = "";
                        clientname = "";
                        custom_matter.setText("");
                        clear_icon2.setVisibility(GONE);
                        dropdown_icon2.setVisibility(VISIBLE);
                        view_docs_list.clear();
                        rv_documents_email.clearFocus();
                        ll_attach_grp.setVisibility(GONE);
                        rv_documents_email.setVisibility(GONE);
                        ll_matter_view.setVisibility(GONE);
                        client_name.setText(R.string.client);
                        ll_client_name.setVisibility(VISIBLE);
                        client_name.setText(R.string.client);
                        callClientWebservice();
                    }
                });
//                tv_select_groups.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (ischecked_group) {
//                            if (groupsList.isEmpty())
//                                callGroupsWebservice();
//                            rv_display_upload_groups_docs.setVisibility(View.VISIBLE);
//                            linearLayout2.setVisibility(View.VISIBLE);
//                        } else {
//                            rv_display_upload_groups_docs.setVisibility(GONE);
//                            linearLayout2.setVisibility(GONE);
//                        }
//                        ischecked_group = !ischecked_group;
//                    }
//                });
//                initUI(client_list_view, clientsList);
                ll_custom_client.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SuspiciousIndentation")
                    @Override
                    public void onClick(View v) {
                        AndroidUtils.display_listview(ischecked, client_list_view);
                        ischecked = !ischecked;
                    }
                });
                clear_icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        client_id = "";
                        view_docs_list.clear();
                        matterlist.clear();
                        matter_id = "";
                        matterName = "";
                        custom_matter.setText("");
                        matterlist.clear();
                        ll_matter_view.setVisibility(GONE);
                        rv_documents_email.setVisibility(GONE);
                        dropdown_icon2.setVisibility(VISIBLE);
                        clear_icon2.setVisibility(GONE);
//                        ll_upload_groups.setVisibility(GONE);
                        selected_client_groups_list.clear();
                        ischecked_matter = true;
//                        btn_create.setAlpha(0.5f);
//                        btn_create.setEnabled(false);
//                        rv_documents_email.removeAllViews();
                        AndroidUtils.DisplaySpinnerView(client_list_view, custom_client, clientname, dropdown_icon, clear_icon, false);
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
                        AndroidUtils.DisplaySpinnerView(list_matter, custom_matter, matterName, dropdown_icon2, clear_icon2, false);
                        matter_id = "";
//                        callClientGroupsWebservice();
                    }
                });
                //...upload Documents Group Selection
                tv_select_groups.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ischecked_group) {
                            if (groupsList.isEmpty()) {
                                callGroupsWebservice();
                            } else {
                                GroupsPopup(groupsList, selected_groups_list, rv_display_upload_groups_docs, tv_select_groups);
                            }
                        } else {
                            callfilter_client_webservices();
                            rv_display_upload_groups_docs.setVisibility(View.GONE);
                        }
                        ischecked_group = !ischecked_group;
                    }
                });
//                tv_select_upload_groups.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (ischeckedClient_group) {
//                            if (client_groups_list.isEmpty()) {
//                                callClientGroupsWebservice();
//                            } else {
//                                GroupsPopup(ll_upload_client_group, client_groups_list, selected_client_groups_list, rv_upload_groups, tv_select_upload_groups);
//                            }
//                        } else {
//                            ll_upload_client_group.setVisibility(View.GONE);
//                        }
//                        ischeckedClient_group = !ischeckedClient_group;
//                    }
//                });
                btn_cancel_save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        attachmentDialog.dismiss();
                    }
                });

                btn_create.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        try {
                        if (!Constants.composAttachDocAry.isEmpty()) {
                            Log.e("SelectedDocument:", "" + Constants.composAttachDocAry.get((Constants.composAttachDocAry.size() - 1)).getName());
                            attachmentDialog.dismiss();
                            if (Constants.composAttachDocAry.size() > 4) {
                                int high = Constants.composAttachDocAry.size() / 2;
                                ViewGroup.LayoutParams layoutParams = yourGridView.getLayoutParams();
                                layoutParams.height = layoutParams.height + (high * 40);
                                yourGridView.setLayoutParams(layoutParams);
                            }
                            yourGridView.setVisibility(VISIBLE);
                            Griddocument griddocument = new Griddocument(getContext(), Constants.composAttachDocAry, selectedDocumentName);
                            yourGridView.setAdapter(griddocument);
                            validateEmail();
                        }
                            /* String doc1_id = Constants.doc_id.getString(0);
                            view_document(doc1_id);
                            selectedDocumentName = doc1_id; */
//                        } catch (JSONException e) {
//                            throw new RuntimeException(e);
//                        }
                    }
                });

                client_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        client_id = clientsList.get(position).getId();
                        clientname = clientsList.get(position).getName();
                        Log.d("Client_value_name", clientname);
                        callfilter_client_webservices();
//                        callLegalMatter();
//                        btn_create.setAlpha(1.0f);
//                        btn_create.setEnabled(true);
                        ischecked = true;
                        AndroidUtils.DisplaySpinnerView(client_list_view, custom_client, clientname, dropdown_icon, clear_icon, true);
                    }
                });
            }
        });

        cross_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the AlertDialog
                composeDialog.dismiss();
                overlay.setVisibility(GONE);
            }
        });
    }

    private void validateEmail() {
        if (isValidEmail(to_input.getText().toString())) {
//        if ((!to_input.getText().toString().isEmpty()) && (!subject_input.getText().toString().isEmpty()) && (!message_inputs.getText().toString().isEmpty()) && (!Constants.composAttachDocAry.isEmpty())) {
            sends_button.setAlpha(1.0f);
            sends_button.setEnabled(true);
        } else {
            sends_button.setAlpha(0.5f);
            sends_button.setEnabled(false);
        }
    }
//    boolean attachmentEmpty = Constants.composAttachDocAry.isEmpty();
//        if (!isGmail) {
//        if (!attachmentEmpty&&(isValidEmail(to_input.getText().toString()))) {
//            // Disable send button
//            sends_button.setAlpha(1.0f);
//            sends_button.setEnabled(true);
//
//            // Show alert

    /// /                AndroidUtils.showAlert("Please attach a document before sending.", getActivity());
    /// /                return;
//        }
//    } else if (isValidEmail(to_input.getText().toString())) {
    public void view_document(String doc_id) {
        try {
            doc_count++;
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            //https://api.staging.digicoffer.com/professional/v3/document/64ca0bd9fffd8f083fafaa22/view
            String baseUrl = "https://api.staging.digicoffer.com/professional/v3/document/";
            String url = baseUrl + doc_id + "/view";
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/document/" + doc_id + "/view", "view_document", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    public void send_email() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());

            JSONObject emailJson = new JSONObject();
            String[] emailArray = to_input.getText().toString().split(",");
            JSONArray emailJsonArray = new JSONArray();
            for (String email : emailArray) {
                String trimmedEmail = email.trim();
                if (!trimmedEmail.isEmpty()) {
                    emailJsonArray.put(trimmedEmail);
                }
            }
            emailJson.put("toEmail", emailJsonArray);
            if (!subject_input.getText().toString().isEmpty()) {
                emailJson.put("subject", subject_input.getText().toString());
            } else {
                emailJson.put("subject", "(no Subject)");
            }
            emailJson.put("body", message_inputs.getText().toString());

            JSONArray documentsArray = new JSONArray();
            try {

                for (int i = 0; i < Constants.composAttachDocAry.size(); i++) {
                    JSONObject documentObject = new JSONObject();
                    documentObject.put("filename", Constants.composAttachDocAry.get(i).getName());
                    documentObject.put("id", Constants.composAttachDocAry.get(i).getId());
                    documentObject.put("name", Constants.composAttachDocAry.get(i).getName());
                    documentObject.put("path", Constants.composAttachDocAry.get(i).getUrl());
                    documentsArray.put(documentObject);
                }
            } catch (JSONException e) {
                e.fillInStackTrace();
            }
            emailJson.put("documents", documentsArray);

//https://mailapi.digicoffer.com/api/v1/gmail/sendmail/attach/documents/eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiJBQ0IxMEE2QjUzRjlEMjdEIiwiYWRtaW4iOmZhbHNlLCJwbGFuIjoibGF1ZGl0b3IiLCJyb2xlIjoiU1UiLCJuYW1lIjoiU291bmRhcnlhIFNMRiBTVSIsInVzZXJfaWQiOiI2M2JmZDliN2ExZGI3MjBmMmQzOGQ0ZDIiLCJleHAiOjE3MTQ0ODE3NDF9.NKYw2dEBdSt_CXey-tqcUyCrpEZTlTXsqVpnV9qzJaY
            //https://dev.utils.mail.digicoffer.com/api/v1/gmail/sendmail/attach/documents/eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiJBQ0IxMEE2QjUzRjlEMjdEIiwiYWRtaW4iOmZhbHNlLCJwbGFuIjoibGF1ZGl0b3IiLCJyb2xlIjoiU1UiLCJuYW1lIjoiU291bmRhcnlhIERMRiBTVSIsInVzZXJfaWQiOiI2M2JmZDliN2ExZGI3MjBmMmQzOGQ0ZDIiLCJleHAiOjE3MTQ0ODExMDB9.8_2U9K_AqgJDh6SPK4mSFKU1RzEnOQJlPpKVcoP4wbM
            String url = "";
            if (isGmail) {
                url = Constants.EMAIL_BASE_URL + Constants.sending_mail;
            } else {
                url = Constants.EMAIL_GET_URL + Constants.sending_mail;
            }
            String emailUrl = url + Constants.TOKEN;
            WebServiceHelper.callEmailHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, emailUrl, "sending_email", emailJson.toString());
//            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, emailUrl, "sending_email", emailJson.toString());
        } catch (Exception e) {
            // Handle exceptions

            e.fillInStackTrace();
        }
    }


    public void callfilter_client_webservices() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
//            JSONArray groups1 = new JSONArray();
            if (Objects.equals(CATEGORY_TAG, "client")) {
                jsonObject.put("category", "client");
                jsonObject.put("clients", client_id);
                if (!matter_id.isEmpty())
                    jsonObject.put("matters", matter_id);
                jsonObject.put("showPdfDocs", false);
                jsonObject.put("groups", null);
                //When the matter is chosen by the user.....
//                if (!matter_id.equals("")) {
//                    jsonObject.put("matters", matter_id);
//                }
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "v3/document/filter", "Display clientDocuments", jsonObject.toString());
                Log.d("Group_doc_view1", jsonObject.toString());
            } else if (Objects.equals(CATEGORY_TAG, "firm")) {
                String[] value = new String[selected_groups_list.size()];
                String[] value_id = new String[selected_groups_list.size()];
                for (int i = 0; i < selected_groups_list.size(); i++) {
                    value[i] = selected_groups_list.get(i).getGroup_name();
                    value_id[i] = selected_groups_list.get(i).getGroup_id();
                }

//                try {
                array_group = new JSONArray(value_id);
//                    callfilter_client_webservices();
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
                jsonObject.put("category", "firm");
                jsonObject.put("clients", "");
                jsonObject.put("matters", "");
                jsonObject.put("showPdfDocs", false);
                jsonObject.put("groups", array_group);
                Log.d("Group_value_num", array_group.toString());

                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "v3/document/filter", "Display firmDocuments", jsonObject.toString());
                Log.d("Group_doc_view1", jsonObject.toString());
            }
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }


    private void load_view_doc(JSONArray docs) throws JSONException {
        try {
            view_docs_list.clear();
            for (int i = 0; i < docs.length(); i++) {
                ViewDocumentsModel viewDocumentsModel = new ViewDocumentsModel();
                JSONObject jsonObject = docs.getJSONObject(i);

                // Set all attributes of the viewDocumentsModel
                viewDocumentsModel.setCreated(jsonObject.getString("created"));
                viewDocumentsModel.setContent_type(jsonObject.getString("content_type"));
                viewDocumentsModel.setDescription(jsonObject.getString("description"));
                viewDocumentsModel.setExpiration_date(jsonObject.getString("expiration_date"));
                viewDocumentsModel.setFilename(jsonObject.getString("filename"));
                viewDocumentsModel.setId(jsonObject.getString("id"));
                viewDocumentsModel.setIs_disabled(jsonObject.getBoolean("is_disabled"));
                viewDocumentsModel.setIs_encrypted(jsonObject.getBoolean("is_encrypted"));
                viewDocumentsModel.setIs_password(jsonObject.getBoolean("is_password"));
                viewDocumentsModel.setName(jsonObject.getString("name"));
                viewDocumentsModel.setUploaded_by(jsonObject.getString("uploaded_by"));

                // Check if the document is already attached in Constants.composAttachDocAry
                boolean isAlreadyAttached = false;
                for (Constants.IdNameModel item : Constants.composAttachDocAry) {
                    if (item.getId().equals(viewDocumentsModel.getId())) {
                        isAlreadyAttached = true;
                        break; // Exit the loop if a match is found
                    }
                }

                // If the document is already attached, set its checked state to true
                viewDocumentsModel.setIsChecked(isAlreadyAttached);

                // Add the document to the view_docs_list
                view_docs_list.add(viewDocumentsModel);

                Log.d("VIEW_POSITION", view_docs_list.get(i).toString());
            }
            loadViewDocumentsRecyclerview();
        } catch (JSONException e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void loadViewDocumentsRecyclerview() {
        try {
            if (view_docs_list.isEmpty()) {
                rv_documents_email.removeAllViews();
//                AndroidUtils.showToast("No documents to display", getContext());
                rv_documents_email.setVisibility(GONE);

            }
//            if (tv_select_groups.getText().toString().isEmpty()) {
//                rv_documents_email.removeAllViews();
//                AndroidUtils.showToast("No documents to display", getContext());
//                rv_documents_email.setVisibility(GONE);
//            }
            else {
                rv_documents_email.setHasFixedSize(false); // ✅ let items resize
                rv_documents_email.setVisibility(VISIBLE);

                view_document_emailadapter adapter =
                        new view_document_emailadapter(view_docs_list, getContext(), getActivity(), btn_create);
                AndroidUtils.LoadingRecyclerview(rv_documents_email, getContext());
//                rv_documents_email.setLayoutAnimation(
//                        AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fall_down)
//                );
//                rv_documents_email.scheduleLayoutAnimation();
                rv_documents_email.setAdapter(adapter);
                rv_documents_email.clearFocus();

                et_Search_email_document.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        adapter.getFilter().filter(s.toString());
                    }
                });


            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void loadRowDatas(JSONObject jsonObject) throws JSONException {

        JSONArray messagesArray = jsonObject.getJSONArray("messages");
        List<MessageModel> messages = new ArrayList<>();
        for (int i = 0; i < messagesArray.length(); i++) {
            JSONObject messageObject = messagesArray.getJSONObject(i);
            MessageModel message = new MessageModel();
            message.setMsgId(messageObject.getString("msgId"));
            message.setSubject(messageObject.getString("subject"));
            message.setFrom(messageObject.getString("from"));
            message.setTo(messageObject.getString("to"));

            JSONArray attachmentsArray = messageObject.getJSONArray("attachments");
            List<AttachmentModel> attachments = new ArrayList<>();
            for (int j = 0; j < attachmentsArray.length(); j++) {
                JSONObject attachmentObject = attachmentsArray.getJSONObject(j);
                AttachmentModel attachment = new AttachmentModel();
                if (!attachmentObject.getString("filename").isEmpty() && !attachmentObject.getString("partId").isEmpty()) {
                    Log.e("Email_Res", "File-" + attachmentObject.getString("filename") + ": Partid-" + attachmentObject.getString("partId"));
                    attachment.setPartId(attachmentObject.getString("partId"));
                    attachment.setMimeType(attachmentObject.getString("mimeType"));
                    attachment.setFilename(attachmentObject.getString("filename"));
                    JSONArray headersArray = attachmentObject.getJSONArray("headers");
                    List<Header> headers = new ArrayList<>();
                    for (int k = 0; k < headersArray.length(); k++) {
                        JSONObject headerObject = headersArray.getJSONObject(k);
                        Header header = new Header();
                        header.setName(headerObject.getString("name"));
                        header.setValue(headerObject.getString("value"));
                        headers.add(header);
                    }
                    attachment.setHeaders(headers);


                    JSONObject bodyObject = attachmentObject.getJSONObject("body");
                    Body body = new Body();
                    body.setSize(bodyObject.getInt("size"));
                    attachment.setBody(body);

                    attachments.add(attachment);
                    message.setAttachments(attachments);
                }

            }
            messages.add(message);
        }
        totalMessageArray.add(pre_next_position, messages);
        // Access other fields like nextPageToken and resultSizeEstimation
        nextPageToken = jsonObject.optString("nextPageToken");
        Log.d("Nextpagetoken", nextPageToken);
        int resultSizeEstimate = jsonObject.optInt("resultSizeEstimate");
        if (messages.size() > 0) {
            updateRecyclerView(messages);


        }

    }

//    private void loadRowDatasFromGraph(JSONObject jsonObject) throws JSONException {
//        JSONArray messagesArray = jsonObject.getJSONArray("value");
//        List<MessageModel> messages = new ArrayList<>();
//
//        for (int i = 0; i < messagesArray.length(); i++) {
//            JSONObject messageObject = messagesArray.getJSONObject(i);
//            MessageModel message = new MessageModel();
//
//            // Basic fields
//            message.setMsgId(messageObject.optString("id"));
//            message.setSubject(messageObject.optString("subject"));
//
//            // Set From
//            JSONObject fromObj = messageObject.optJSONObject("from");
//            if (fromObj != null) {
//                JSONObject emailAddress = fromObj.optJSONObject("emailAddress");
//                if (emailAddress != null) {
//                    message.setFrom(emailAddress.optString("address"));
//                }
//            }
//
//            // Set To
//            JSONArray toRecipients = messageObject.optJSONArray("toRecipients");
//            if (toRecipients != null && toRecipients.length() > 0) {
//                JSONObject toEmailObj = toRecipients.getJSONObject(0).optJSONObject("emailAddress");
//                if (toEmailObj != null) {
//                    message.setTo(toEmailObj.optString("address"));
//                }
//            }
//
//            // Placeholder for attachments if needed
//            if (messageObject.optBoolean("hasAttachments", false)) {
//                Log.e("Email_Res", "Message with ID " + message.getMsgId() + " has attachments.");
//                callMessageAttachmentList(message.getMsgId());
//                // Attachments API call is usually separate; skip here or leave placeholder
//            }
//
//            messages.add(message);
//        }
//
//        totalMessageArray.add(pre_next_position, messages);
//
//        // Optional: extract and log next page token
//        nextPageToken = jsonObject.optString("@odata.nextLink");
//        Log.d("Nextpagetoken", nextPageToken);
//
//        if (messages.size() > 0) {
//            updateRecyclerView(messages);
//        }
//    }

    //    private void loadRowDatasFromGraph(JSONObject jsonObject) throws JSONException {
//        JSONArray messagesArray = jsonObject.getJSONArray("value");
//
//        for (int i = 0; i < messagesArray.length(); i++) {
//            JSONObject messageObject = messagesArray.getJSONObject(i);
//            MessageModel message = new MessageModel();
//
//            message.setMsgId(messageObject.optString("id"));
//            message.setSubject(messageObject.optString("subject"));
//
//            JSONObject fromObj = messageObject.optJSONObject("from");
//            if (fromObj != null) {
//                JSONObject emailAddress = fromObj.optJSONObject("emailAddress");
//                if (emailAddress != null) {
//                    message.setFrom(emailAddress.optString("name"));
//                }
//            }
//
//            JSONArray toRecipients = messageObject.optJSONArray("toRecipients");
//            if (toRecipients != null && toRecipients.length() > 0) {
//                JSONObject toEmailObj = toRecipients.getJSONObject(0).optJSONObject("emailAddress");
//                if (toEmailObj != null) {
//                    message.setTo(toEmailObj.optString("name"));
//                }
//            }
//
//            // Add message to list early
//            messages.add(message);
//
//            // If attachments exist, call attachment API
//            if (messageObject.optBoolean("hasAttachments", false)) {
//                callMessageAttachmentList(messages.get(i).getMsgId());
//            }
//        }
//
//        totalMessageArray.add(pre_next_position, messages);
//
//        nextPageToken = jsonObject.optString("@odata.nextLink");
//        Log.d("Nextpagetoken", nextPageToken);
//
//        if (!messages.isEmpty()) {
//            updateRecyclerView(messages);
//        }
//    }
    private void loadRowDatasFromGraph(JSONObject jsonObject) throws JSONException {
        JSONArray messagesArray = jsonObject.getJSONArray("value");

        messages.clear(); // Clear previous messages

        progress_dialog = AndroidUtils.get_progress(getActivity()); // Show once

        for (int i = 0; i < messagesArray.length(); i++) {
            JSONObject messageObject = messagesArray.getJSONObject(i);
            MessageModel message = new MessageModel();

            message.setMsgId(messageObject.optString("id"));
            message.setSubject(messageObject.optString("subject"));

            JSONObject fromObj = messageObject.optJSONObject("from");
            if (fromObj != null) {
                JSONObject emailAddress = fromObj.optJSONObject("emailAddress");
                if (emailAddress != null) {
                    message.setFrom(emailAddress.optString("name"));
                }
            }

            JSONArray toRecipients = messageObject.optJSONArray("toRecipients");
            if (toRecipients != null && toRecipients.length() > 0) {
                JSONObject toEmailObj = toRecipients.getJSONObject(0).optJSONObject("emailAddress");
                if (toEmailObj != null) {
                    message.setTo(toEmailObj.optString("name"));
                }
            }

            messages.add(message); // Add to list

            // Load attachments if they exist
            if (messageObject.optBoolean("hasAttachments", false)) {
                callMessageAttachmentList(message.getMsgId());
            }
        }

        totalMessageArray.add(pre_next_position, messages);
        nextPageToken = jsonObject.optString("@odata.nextLink");

        updateRecyclerView(messages);
    }


    @Override
    public void onClick(View view) {

    }

    public void onAsyncTaskComplete(HttpResultDo httpResult) {

        if (progress_dialog != null && progress_dialog.isShowing()) {
            AndroidUtils.dismiss_dialog(progress_dialog);
        }

        String success = String.valueOf(httpResult.getResult());
        Log.d("Succ", success);

        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {

            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());

                if (httpResult.getRequestType().equals("Label")) {

                    if (isGmail) {
                        JSONObject data = result.getJSONObject("data");
                        if (data.length() > 0) {
                            loadLabelData(data);
                            ISCHECK_EMAIL = true;
                            callMessageList();
                        }
                    } else {
                        loadOutlookLabelData(result);
                        ISCHECK_EMAIL = true;
                        callMessageList();
                    }

                } else if (httpResult.getRequestType().equals("messages_rows")) {

                    if (isGmail)
                        loadRowDatas(result);
                    else
                        loadRowDatasFromGraph(result);

                } else if (httpResult.getRequestType().startsWith("messages_attachment_")) {

                    String msgId = httpResult.getRequestType().replace("messages_attachment_", "");

                    try {
                        JSONObject responseObj = result.optJSONObject("attachments");
                        JSONArray attachmentsArray = responseObj != null ? responseObj.optJSONArray("value") : null;

                        List<AttachmentModel> attachments = new ArrayList<>();

                        if (attachmentsArray != null) {
                            for (int j = 0; j < attachmentsArray.length(); j++) {
                                JSONObject attObj = attachmentsArray.getJSONObject(j);
                                AttachmentModel attachment = new AttachmentModel();
                                attachment.setPartId(attObj.optString("id"));
                                attachment.setFilename(attObj.optString("name"));
                                attachment.setMimeType(attObj.optString("contentType"));
                                if (attObj.has("size")) {
                                    attachment.setSize(attObj.optInt("size"));
                                }
                                attachments.add(attachment);
                            }
                        }

                        for (int i = 0; i < messages.size(); i++) {
                            MessageModel m = messages.get(i);
                            if (m.getMsgId().equals(msgId)) {
                                m.setAttachments(attachments);
                                adapter.notifyItemChanged(i);
                                break;
                            }
                        }

                    } catch (JSONException e) {
                        Log.e("Attachment Parsing", "Error: " + e.getMessage());
                    }

                } else if (httpResult.getRequestType().equals("Display clientDocuments")
                        || httpResult.getRequestType().equals("Display firmDocuments")) {

                    JSONArray data = result.getJSONArray("data");
                    load_view_doc(data);

                } else if (httpResult.getRequestType().equals("Groups")) {

                    JSONArray data = result.getJSONArray("data");
                    loadGroupsData(data, groupsList, false);

                } else if (httpResult.getRequestType().equals("Clients List")) {

                    JSONObject data = result.getJSONObject("data");
                    loadClients(data);
                    callCorpClientWebservice();

                } else if (httpResult.getRequestType().equals("Legal Matter")) {

                    JSONArray matters = result.getJSONArray("matters");
                    loadMatters(matters);

                } else if (httpResult.getRequestType().equals("Corp Clients List")) {

                    loadCorpClients(result);

                } else if (httpResult.getRequestType().equals("view_document")) {

                    JSONObject data = result.getJSONObject("data");
                    String url = data.getString("url");
                    Constants.composAttachDocAry.get(Constants.tempPos).setUrl(url);

                } else if (httpResult.getRequestType().equals("sending_email")) {

                    composeDialog.dismiss();
                    overlay.setVisibility(GONE);
                    AndroidUtils.showAlert_docs("Success!", result.getString("message"), getActivity());

                } else if (httpResult.getRequestType().equals("auth")) {

                    String url = result.getString("url");
                    ISCHECK_AUTH = true;
                    emaiAPI();
                    launchAuthUrl(url);
                }

            } catch (JSONException e) {
                e.fillInStackTrace();
            }

        } else if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Failed) {

            if (httpResult.getRequestType().equals("Label")
                    || httpResult.getRequestType().equals("auth")) {
                emaiAPI();
            }

            if (httpResult.getRequestType().equals("sending_email")) {
                AndroidUtils.showAlert("Mail not sent successfully", getActivity());
                composeDialog.dismiss();
                overlay.setVisibility(GONE);
            }

            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
//                AndroidUtils.showErrorAlert(result.optString("msg"), getActivity());
            } catch (Exception e) {
                e.fillInStackTrace();
            }

        } else {

            AndroidUtils.showErrorAlert(
                    httpResult.getResponseContent().toString(),
                    getActivity()
            );
        }
    }


    private void callClientGroupsWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
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
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "v3/documents/groupslist", "Client Groups", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    private void callGroupsWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/groups", "Groups", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    private void loadLabelData(JSONObject data) {
        //            for (int i = 0; i < data.length(); i++) {
//                JSONObject jsonObject = data.getJSONObject(String.valueOf(i));
        EmailModel emailModel = new EmailModel();
        emailModel.setId(data.optString("id"));
        emailModel.setName(data.optString("name"));
        emailModel.setType(data.optString("type"));
        emailModel.setMessagesTotal(data.optInt("messagesTotal"));
        emailModel.setMessagesUnread(data.optInt("messagesUnread"));
        emailModel.setThreadsTotal(data.optInt("threadsTotal"));
        emailModel.setThreadsUnread(data.optInt("threadsUnread"));
        Log.e("Email Inbox", String.valueOf(emailModel.getMessagesTotal()));
        inbox_textViews.setText("Inbox " + emailModel.getMessagesTotal());
        composeDocuments.setAlpha(1.0f);
//            }
    }

    private void loadOutlookLabelData(JSONObject data) {
        //            for (int i = 0; i < data.length(); i++) {
//                JSONObject jsonObject = data.getJSONObject(String.valueOf(i));
        emailModel = new EmailModel();
        emailModel.setId(data.optString("id"));
        emailModel.setName(data.optString("displayName"));
        emailModel.setType(data.optString("type"));
        emailModel.setMessagesTotal(data.optInt("totalItemCount"));
        emailModel.setMessagesUnread(data.optInt("unreadItemCount"));
        emailModel.setThreadsTotal(data.optInt("sizeInBytes"));
        emailModel.setThreadsUnread(data.optInt("threadsUnread"));
        Log.e("Email Inbox", String.valueOf(emailModel.getMessagesTotal()));
        inbox_textViews.setText("Inbox " + emailModel.getMessagesTotal());
        composeDocuments.setAlpha(1.0f);
//            }
    }

    private void launchAuthUrl(String authUrl) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(authUrl));
        startActivityForResult(intent, AUTH_REQUEST_CODE);
        ischeck_auth = true;
    }


    public void emaiauth() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("authtoken",Constants.TOKEN);
            if (isGmail) {
                WebServiceHelper.callHttpWebService(this,
                        getContext(),
                        WebServiceHelper.RestMethodType.GET,
                        Constants.EMAIL_BASE_URL + "gmail" + "/authurl?authtoken=" + Constants.TOKEN,
                        "auth",
                        jsonObject.toString());
            } else {
                WebServiceHelper.callHttpWebService(this,
                        getContext(),
                        WebServiceHelper.RestMethodType.GET,
                        Constants.EMAIL_BASE_URL + "authurl?authtoken=" + Constants.TOKEN,
                        "auth",
                        jsonObject.toString());
            }
            Log.d("C12Token", Constants.EMAIL_BASE_URL + isGoogle + "/authurl?authtoken=" + Constants.TOKEN);
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
            e.fillInStackTrace();
        }
    }

    public void callMessageList() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();

            if (isGmail)
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, Constants.EMAIL_BASE_URL + Constants.gmail_messages + Constants.TOKEN + "?rows=10", "messages_rows", jsonObject.toString());
            else
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, Constants.EMAIL_GET_URL + "messages/" + Constants.TOKEN + "?labelid=" + emailModel.getId() + "&rows=10", "messages_rows", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
            e.fillInStackTrace();

            Log.e("API Error", "Failed to call API: " + e.getMessage());
        }
    }

    //    public void callMessageAttachmentList(String msgId) {
//        try {
//            this.msgId = msgId;
//            progress_dialog = AndroidUtils.get_progress(getActivity());
//            JSONObject jsonObject = new JSONObject();
//            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, Constants.EMAIL_GET_URL + "message/detail/" + Constants.TOKEN + "/" + msgId, "messages_attachment", jsonObject.toString());
//        } catch (Exception e) {
//            if (progress_dialog != null && progress_dialog.isShowing()) {
//                AndroidUtils.dismiss_dialog(progress_dialog);
//            }
//            e.fillInStackTrace();
//
//            Log.e("API Error", "Failed to call API: " + e.getMessage());
//        }
//    }
    public void callMessageAttachmentList(String msgId) {
        try {
            // Don't show progress dialog for each message, show it only once from caller
            JSONObject jsonObject = new JSONObject();

            // Use unique requestType with msgId to match later
            WebServiceHelper.callHttpWebService(
                    this,
                    getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    Constants.EMAIL_GET_URL + "message/detail/" + Constants.TOKEN + "/" + msgId,
                    "messages_attachment_" + msgId,
                    jsonObject.toString()
            );
        } catch (Exception e) {
            Log.e("API Error", "Failed to call API: " + e.getMessage());
        }
    }

//    public void callMessageAttachmentList(String msgId) {
//        try {
//            this.msgId=msgId;
//            JSONObject jsonObject = new JSONObject(); // not needed if no POST body
//            WebServiceHelper.callHttpWebService(this,
//                    getContext(),
//                    WebServiceHelper.RestMethodType.GET,
//                    Constants.EMAIL_GET_URL + "messages/detail/" + Constants.TOKEN + "/" + emailModel.getId() + "/" + msgId,
//                    "messages_attachment",jsonObject.toString()
//            );
//
//        } catch (Exception e) {
//            Log.e("API Error", "Exception for msgId " + msgId + ": " + e.getMessage());
//        }
//    }


    private void parseAndAssignAttachments(String msgId, String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject attachmentsObj = jsonObject.getJSONObject("attachments");
            JSONArray valuesArray = attachmentsObj.getJSONArray("value");

            List<AttachmentModel> attachmentList = new ArrayList<>();

            for (int i = 0; i < valuesArray.length(); i++) {
                JSONObject attach = valuesArray.getJSONObject(i);
                AttachmentModel attachment = new AttachmentModel();
                attachment.setPartId(attach.getString("id"));
                attachment.setFilename(attach.getString("name"));
                attachment.setContentType(attach.getString("contentType"));
                attachment.setSize(attach.getInt("size"));

                attachmentList.add(attachment);
            }

            // Find the message by msgId and assign attachments
            for (MessageModel message : messages) {
                if (message.getMsgId().equals(msgId)) {
                    message.setAttachments(attachmentList);
                    break;
                }
            }

            // Optional: Notify adapter
            adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            Log.e("Parse Error", "Failed to parse attachments: " + e.getMessage());
        }
    }

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
        if (!clientsList.isEmpty()) {
            initUI(clientsList);
        } else {
            client_list_view.setVisibility(GONE);
            ischecked = true;
        }
        // intUI(clientsList);
    }

    private void initUI(ArrayList<ClientsModel> clientsList) {

        CommonSpinnerAdapter adapter = new CommonSpinnerAdapter(getActivity(), clientsList);
        client_list_view.setAdapter(adapter);
        AndroidUtils.LoadList(client_list_view, context_type, clientsList.size(), true);
//        if (!clientsList.isEmpty()) {
//            client_list_view.setVisibility(VISIBLE);
//        } else {
//            client_list_view.setVisibility(GONE);
//            ischecked = true;
//        }
    }

    private void callLegalMatter() {
        try {
            progress_dialog = AndroidUtils.get_progress((Activity) context_type);
            JSONObject jsonObject = new JSONObject();
//            Log.d("Client_id", client_id);
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
//        MattersModel mattersModel1 = new MattersModel();
//        mattersModel1.setId("all");
//        mattersModel1.setTitle("All Documents");
//        mattersModel1.setType("consumer");
//        matterlist.add(0, mattersModel1);
        for (int i = 0; i < matters.length(); i++) {
            JSONObject jsonObject = matters.getJSONObject(i);
            MattersModel mattersModel = new MattersModel();
            mattersModel.setId(jsonObject.optString("id"));
            mattersModel.setTitle(jsonObject.optString("title"));
            mattersModel.setType(jsonObject.optString("type"));
            matterlist.add(mattersModel);
        }
        if (matters.length() == 0) {
            ll_matter_view.setVisibility(View.GONE);
            ll_matter.setVisibility(View.GONE);
        } else {
            ll_matter_view.setVisibility(VISIBLE);
            ll_matter.setVisibility(VISIBLE);
        }
        initMatter();
    }

    private void initMatter() {
        CommonSpinnerAdapter adapter = new CommonSpinnerAdapter((Activity) context_type, matterlist);
        list_matter.setAdapter(adapter);
        AndroidUtils.LoadList(list_matter, context_type, matterlist.size(), true);
        list_matter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                matter_id = "";
                matter_id = matterlist.get(position).getId();
                matterName = matterlist.get(position).getTitle();
                Log.d("Matter_value_name", matterName);
                callfilter_client_webservices();
                //Displaying the chosen client name
                AndroidUtils.DisplaySpinnerView(list_matter, custom_matter, matterName, dropdown_icon2, clear_icon2, true);
                ischecked_matter = true;
            }
        });
    }

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
            selectedLanguage = new boolean[groupsList.size()];
//            if (isclient_groups) {
//                GroupsPopup(ll_upload_groups, client_groups_list, selected_client_groups_list, rv_upload_groups, tv_select_upload_groups);
//                ll_upload_groups.setVisibility(View.VISIBLE);
//            } else {
//                ll_upload_groups.setVisibility(View.GONE);
            GroupsPopup(groupsList, selected_groups_list, rv_display_upload_groups_docs, tv_select_groups);
//            }
        } catch (
                JSONException e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }
//    private void loadGroupsData(JSONArray data)
//    {
//        try {
//            groupsList.clear();
//            for (int i = 0; i < data.length(); i++) {
//                JSONObject jsonObject = data.getJSONObject(i);
//                DocumentsModel documentsModel = new DocumentsModel();
//                if ((!jsonObject.getString("name").equals("AAM")) && ((!jsonObject.getString("name").equals("SuperUser")))) {
//                    documentsModel.setGroup_id(jsonObject.getString("id"));
//                    documentsModel.setGroup_name(jsonObject.getString("name"));
//                    groupsList.add(documentsModel);
//                }
//            }
//            selectedLanguage = new boolean[groupsList.size()];

    /// /            GroupsAlert();
//            GroupsPopup();
//        } catch (JSONException e) {
//            e.fillInStackTrace();
//            AndroidUtils.showAlert(e.getMessage(), getActivity());
//        }
//    }
    @SuppressLint("MissingInflatedId")
    private void GroupsPopup(ArrayList<DocumentsModel> groupsList, ArrayList<DocumentsModel> selected_groups_list, RecyclerView rv_display_upload_groups_docs, TextView tv_select_groups) {
        tv_select_groups.setText("");
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
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            rv_display_upload_groups_docs.setLayoutManager(layoutManager);
            // rv_display_upload_groups_docs.setHasFixedSize(true);
            rv_display_upload_groups_docs.setVisibility(VISIBLE);
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
//                    EnableUpload();
//                    if (selected_groups_list.isEmpty()) {
//                        btn_create.setAlpha(0.5f);
//                        btn_create.setEnabled(false);
//                    } else {
//                        btn_create.setAlpha(1.0f);
//                        btn_create.setEnabled(true);
//                    }
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
            AndroidUtils.LoadList(rv_display_upload_groups_docs, getContext(), groupsList.size(), true);

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
//    private void GroupsPopup() {
//        try {
//            RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
//            rv_display_upload_groups_docs.setLayoutManager(layoutManager1);
//            rv_display_upload_groups_docs.setHasFixedSize(true);
//
//            GroupsListAdapter documentsAdapter1 = new GroupsListAdapter(groupsList, Documents.class.newInstance(), new GroupsListAdapter.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(DocumentsModel documentsModel) {
//                    // Update selected groups list
//                    if (documentsModel.isGroupChecked()) {
//                        selected_groups_list.add(documentsModel);

    /// /                        client_list_view.setVisibility(View.VISIBLE);
//                        rv_documents_email.setVisibility(View.VISIBLE);
//                    } else {
//                        // Remove the unchecked item from selected_groups_list
//                        for (int i = 0; i < selected_groups_list.size(); i++) {
//                            if (selected_groups_list.get(i).getGroup_id().equals(documentsModel.getGroup_id())) {
//                                selected_groups_list.remove(i);
//
//                                break;
//                            }
//                        }
//                    }
//
//
//                    String[] value = new String[selected_groups_list.size()];
//                    String[] value_id = new String[selected_groups_list.size()];
//                    for (int i = 0; i < selected_groups_list.size(); i++) {
//                        value[i] = selected_groups_list.get(i).getGroup_name();
//                        value_id[i] = selected_groups_list.get(i).getGroup_id();
//                    }
//
//                    try {
//                        array_group = new JSONArray(value_id);
//                        callfilter_client_webservices();
//                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
//                    }
//
//                    String str = TextUtils.join(",", value);
//                    tv_select_groups.setText(str);
//                    if (tv_select_groups.getText().toString().isEmpty()) {
//
//                        ViewGroup.LayoutParams params = rv_documents_email.getLayoutParams();
//
//                        rv_documents_email.setLayoutParams(params);
//                        // Clear focus from the RecyclerView
//                        rv_documents_email.clearFocus();
//                    } else {
//
//
//                    }
//
//                    rv_documents_email.setVisibility(GONE);
//
//
//                    ischecked_group_view = true;
//                }
//            });
//            rv_display_upload_groups_docs.setAdapter(documentsAdapter1);
//        } catch (Exception e) {
//            e.fillInStackTrace();
//            AndroidUtils.showAlert(e.getMessage(), getActivity());
//        }
//    }
    public void callMessageListnext() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();

            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, Constants.EMAIL_BASE_URL + Constants.gmail_messages + Constants.TOKEN + "?rows=10&nextpagetoken=" + nextPageToken, "messages_rows", jsonObject.toString());

        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
            e.fillInStackTrace();

            Log.e("API Error", "Failed to call API: " + e.getMessage());
        }
    }


    public void callLabel() {
        try {

            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
//            isGoogle
            if (isGmail) {
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, Constants.EMAIL_BASE_URL + "gmail" + "/label/" + Constants.TOKEN + "?labelid=INBOX", "Label", jsonObject.toString());
            } else {
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, Constants.EMAIL_GET_URL + "label/" + Constants.TOKEN + "?labelid=INBOX", "Label", jsonObject.toString());
            }
            Log.d("Label_value", Constants.EMAIL_BASE_URL + "gmail/label/" + Constants.TOKEN + "?labelid=INBOX");

        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
            e.fillInStackTrace();

        }
    }


    public void emaiAPI() {

        if (!ISCHECK_AUTH) {
            emaiauth();
        } else {

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {

                    //Do something here
                    if (!ISCHECK_EMAIL) {
                        callLabel();
                    }

                }
            }, 10000);
        }

    }

    private void callClientWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/client/all/list", "Clients List", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    private void callCorpClientWebservice() {
        try {
//            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/corporate/list", "Corp Clients List", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    private void updateRecyclerView(List<MessageModel> messages) {
        RecyclerView recyclerView = getView().findViewById(R.id.recyclerView);
        adapter = new EmailAdapter(messages, this, getActivity());
        recyclerView.setAdapter(adapter);
        AndroidUtils.LoadingRecyclerview(recyclerView, getContext());
//        recyclerView.setLayoutAnimation(
//                AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fall_down)
//        );
//        recyclerView.scheduleLayoutAnimation();
        search_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.getFilter().filter(et_Search.getText().toString());
            }
        });
        et_Search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                search_email.setAlpha(0.3f);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    clear_search.setVisibility(GONE);
                    search_email.setAlpha(0.3f);
                } else {
                    clear_search.setVisibility(VISIBLE);
                    search_email.setAlpha(1.0f);
                }
            }
        });
        clear_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_Search.setText("");
                search_email.setAlpha(0.3f);
                adapter.getFilter().filter(et_Search.getText().toString());
            }
        });
    }
}