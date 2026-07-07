
package com.digicoffer.lauditor.Matter.ViewModels;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.checkSwitchState;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.renderSelectedTags;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.digicoffer.lauditor.Documents.DocumentsListAdpater.DocumentsListAdapter;
import com.digicoffer.lauditor.Matter.Adapters.DocumentsAdapter;
import com.digicoffer.lauditor.Matter.Adapters.ViewMatterAdapter;
import com.digicoffer.lauditor.Matter.Models.AdvocateModel;
import com.digicoffer.lauditor.Matter.Models.ClientsModel;
import com.digicoffer.lauditor.Matter.Models.DocumentsModel;
import com.digicoffer.lauditor.Matter.Models.GroupsModel;
import com.digicoffer.lauditor.Matter.Models.MatterModel;
import com.digicoffer.lauditor.Matter.Models.TeamModel;
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.FileSelection.BottomSheetUploadFile;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.DescriptionValidation;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;
import com.digicoffer.lauditor.CommonFiles.PdfUtils.File_Content_Type;
import com.digicoffer.lauditor.CommonFiles.PdfUtils.RetrievePDFfromUrl;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

public class MatterDocuments_En extends Fragment implements AsyncTaskCompleteListener, DocumentsListAdapter.EventListener, View.OnClickListener, BottomSheetUploadFile.OnPhotoSelectedListner {
    private TextView tv_added_tags, tv_selected_document, tv_tag_document_name, matter_date, tv_document_library, tv_device_drive, at_add_documents, add_groups, select_all, tv_enable_download, tv_disable_download, tv_multiple_doc;
    private LinearLayout tv_selected_file,llSelectedTags, ll_added_tags, ll_add_documents, ll_selected_documents, ll_select_doc, ll_matterDate, ll_select_file;
    private RecyclerView ll_uploaded_documents;
    private ImageView iv_remove_matter;
    private Button btn_browse, btn_add_documents;
    RecyclerView rv_matter_list;
    boolean isChangesOccured = false;
    AlertDialog dialog = null;
    RelativeLayout rl_buttons;
    String subtag = "";
    boolean is_clicked_add = true;
    boolean is_clicked_edit = true;
    DocumentsListAdapter adapter;
    LinearLayout chk_box_layout, ll_buttons;
    CheckBox chk_select_all;
    private int totalUploads = 0;
    private int completedUploads = 0;
    boolean isselect_all_checked = true;
    private boolean isAlertShown = false;
    boolean isDocUploaded = false;
    MatterModel matterModel = new MatterModel();
    InputFilter[] filters, filters1;
    boolean DOWNLOAD_TAG = false;
    boolean isedit = false;
    boolean ENCRYPTION_TAG = true, DECRYPTION_TAG = true;
    Dialog progressDialog;
    String matter_id = "";
    int edit_position = 0;
    DocumentsModel sharedDocumentsDo = new DocumentsModel();
    private NewModel mViewModel;
    String matter_type = "";
    DocumentsAdapter documentsAdapter;
    CardView cv_client_details, cv_add_opponent_advocate;
    Matter matter;
    ArrayList<ViewMatterModel> matterList = new ArrayList<>();
    String matter_title, case_number, case_type, description, dof, start_date, end_date, court, judge, case_priority, case_status;
    private JSONArray existing_opponents;
    TextInputEditText tv_tag_type, tv_tag_name;
    boolean isInitialLoad = true;
    TextInputLayout search_matter;
    TextView tag_type_name, tag_name, header_name;
    ArrayList<AdvocateModel> advocates_list = new ArrayList<>();
    private ImageView imageView;
    ArrayList<TeamModel> selected_tm_list = new ArrayList<>();
    LinearLayoutCompat ll_download;
    RecyclerView rv_display_upload_doc;
    ArrayList<ClientsModel> selected_clients_list = new ArrayList<>();
    ArrayList<ClientsModel> selected_temp_clients_list = new ArrayList<>();
    ArrayList<ClientsModel> selected_corp_clients_list = new ArrayList<>();
    ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel> tags_list = new ArrayList<>();
    BottomSheetUploadFile bottommSheetUploadDocument;
    AppCompatButton btn_cancel_save, btn_create, btn_add_tags;
    boolean[] selectedDocument;
    String corp_client_id = "";
    private Bitmap mSelectedBitmap;
    ConstraintLayout cl_matter_document;
    ArrayList<MatterModel> matterArraylist;
    private File mSelectedUri;
    String ADAPTER_TAG = "Documents";
    ArrayList<DocumentsModel> documentsList = new ArrayList<>();
    public ArrayList<DocumentsModel> selected_documents_list = new ArrayList<>();
    public ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel> selected_upload_documents_list = new ArrayList<>();
    public ArrayList<DocumentsModel> tempSelectedDocuments = new ArrayList<>();
    ArrayList<DocumentsModel> new_selected_doc = new ArrayList<>();
    ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel> upload_documents_list = new ArrayList<>();
    JSONArray exisiting_group_acls;
    JSONArray existing_documents;
    JSONArray existing_corp_clients;
    JSONArray existing_temp_clients;
    JSONArray existing_documents_list;
    TextInputEditText et_search_matter;
    boolean ischecked_doc = true;
    JSONArray existing_clients;
    JSONArray existing_members;
    JSONArray existing_groups_list;
    JSONArray existing_clients_list;
    JSONArray existing_tm_list;
    ArrayList<GroupsModel> selected_groups_list = new ArrayList<>();
    private Dialog progress_dialog;
    String filename;
    TextView matter_title_tv, tv_enable_encryption, tv_disable_encryption, tv_add_tag, tv_edit_meta;
    LinearLayoutCompat ll_upload_type;
    File file;
    boolean isUpdateTag = false;
    ViewMatterModel viewMatterModel1;
    ViewMatter viewmatter;
    private ArrayList<GroupsModel> groupsList = new ArrayList<>();
    private ArrayList<ClientsModel> clientsList = new ArrayList<>();
    private ArrayList<TeamModel> tmList = new ArrayList<>();
    private String uploaded_document_name, upload_description, upload_exp_date;
    private AbstractCollection<DocumentsModel> MergedList;
    private int changedCollection;
    private ArrayList<String> tag_list = new ArrayList<>();
    private JSONArray existing_tags_list;

    public MatterDocuments_En() {//ViewMatterModel viewMatterModel
//        viewMatterModel1 = viewMatterModel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.documents_matter, container, false);
        Calendar myCalendar = Calendar.getInstance();
        ll_matterDate = view.findViewById(R.id.ll_matterDate);
        ll_matterDate.setVisibility(GONE);
        ll_upload_type = view.findViewById(R.id.ll_upload_type);
        ll_download = view.findViewById(R.id.ll_download);
        ll_buttons = view.findViewById(R.id.ll_buttons);
        tv_multiple_doc = view.findViewById(R.id.tv_multiple_doc);
        tv_multiple_doc.setText(R.string.upload_multiple_document_s);
        tv_selected_document = view.findViewById(R.id.tv_selected_document);
        iv_remove_matter = view.findViewById(R.id.iv_remove_matter);
//        if (!Constants.create_matter) {
        iv_remove_matter.setVisibility(VISIBLE);
//        } else {
//            iv_remove_matter.setVisibility(GONE);
//        }
        tv_selected_document.setText(R.string.selected_documents);
//        tv_disable_download.setTextSize(13);
//        tv_enable_download.setPadding(20, 15, 10, 15);
//        tv_disable_download.setPadding(20, 15, 10, 15);
        rl_buttons = view.findViewById(R.id.rl_buttons);
        rl_buttons.setVisibility(GONE);
        tv_edit_meta = view.findViewById(R.id.tv_edit_meta);
        tv_add_tag = view.findViewById(R.id.tv_add_tag);
        tv_add_tag.setText(R.string.add_tag);
        tv_edit_meta = view.findViewById(R.id.tv_edit_meta);
        tv_edit_meta.setText(R.string.edit_meta);
        btn_add_tags = view.findViewById(R.id.btn_add_tag);
        btn_add_tags.setText(R.string.add_tag);
        btn_add_tags.setVisibility(VISIBLE);
        tv_enable_download = view.findViewById(R.id.tv_enable_download);
        tv_enable_download.setText(R.string.enable_download);
        tv_enable_download.setTextSize(13);
        tv_disable_download = view.findViewById(R.id.tv_disable_download);
        tv_disable_download.setText(R.string.disable_download);
        tv_disable_download.setTextSize(13);
        tv_enable_encryption = view.findViewById(R.id.tv_enable_encryption);
        tv_enable_encryption.setText(R.string.enable_encryption);
        tv_enable_encryption.setTextSize(13);
        tv_disable_encryption = view.findViewById(R.id.tv_disable_encryption);
        tv_disable_encryption.setText(R.string.disable_encryption);
        tv_disable_encryption.setTextSize(13);
        tv_enable_download.setPadding(20, 20, 20, 20);
        tv_disable_download.setPadding(20, 20, 20, 20);
        tv_enable_encryption.setPadding(20, 20, 20, 20);
        tv_disable_encryption.setPadding(20, 20, 20, 20);
        matter_date = view.findViewById(R.id.matter_date);
        chk_box_layout = view.findViewById(R.id.chk_box_layout);
        chk_box_layout.setAlpha(0.5F);
        chk_select_all = view.findViewById(R.id.chk_select_all);
//        chk_select_all.getBackground().setAlpha(50);
//        chk_select_all.setEnabled(false);
        AndroidUtils.ToggleButton(0, chk_box_layout);
        AndroidUtils.ToggleButton(0, chk_select_all);
        AndroidUtils.ToggleButton(0, btn_add_tags);
        cl_matter_document = view.findViewById(R.id.cl_matter_document);
        tv_document_library = view.findViewById(R.id.tv_document_library);
        tv_document_library.setText(R.string.document_library);
        tv_document_library.setOnClickListener(this);
        et_search_matter = view.findViewById(R.id.et_Search);
        cv_client_details = view.findViewById(R.id.cv_client_details);
        cv_add_opponent_advocate = view.findViewById(R.id.cv_add_opponent_advocate);
        tv_device_drive = view.findViewById(R.id.tv_device_drive);
        tv_device_drive.setText(R.string.device_drive);
        add_groups = view.findViewById(R.id.add_groups);
        add_groups.setText(R.string.add_documents);
        select_all = view.findViewById(R.id.select_all);
        select_all.setText(R.string.select_document);
        rv_matter_list = view.findViewById(R.id.rv_matter_list);
//        search_matter = view.findViewById(R.id.search_matter);
        tv_device_drive.setOnClickListener(this);
        at_add_documents = view.findViewById(R.id.at_add_documents);
        at_add_documents.setHint(R.string.select_document);
        at_add_documents.setOnClickListener(this);
//        tv_selected_file.setHint(R.string.select_document);
//        tv_selected_file.setHintTextColor(requireContext().getColor(R.color.grey_color_dark));
//        tv_selected_file.setGravity(Gravity.CENTER_VERTICAL);
//        tv_selected_file.setText("");
//        tv_selected_file.setBackground(getContext().getResources().getDrawable(R.drawable.rectangle_light_grey_bg));
//        tv_selected_file.setPadding(20, 0, 0, 0);
        ll_add_documents = view.findViewById(R.id.ll_add_documents);
        ll_selected_documents = view.findViewById(R.id.ll_selected_documents);
        ll_uploaded_documents = view.findViewById(R.id.ll_uploaded_documents);
        ll_select_doc = view.findViewById(R.id.ll_select_doc);
        ll_select_doc.setVisibility(GONE);
        ll_select_file = view.findViewById(R.id.ll_select_file);
        tv_selected_file = ll_select_file.findViewById(R.id.tv_selected_file);
        btn_browse = ll_select_file.findViewById(R.id.btn_browse);
//        btn_browse.setBackground(getActivity().getDrawable(R.drawable.rectangular_complete_blue_background));
//        btn_browse.setText(R.string.browse_small);
//        btn_browse.setOnClickListener(this);
        btn_add_documents = view.findViewById(R.id.btn_add_documents);
//        btn_add_documents.setOnClickListener(this);
        btn_cancel_save = view.findViewById(R.id.btn_cancel_save);
        btn_cancel_save.setOnClickListener(this);
        btn_create = view.findViewById(R.id.btn_submit);
        btn_create.setText(R.string.save);
        matter_title_tv = view.findViewById(R.id.matter_title);
        matter_title_tv.setTextSize(DynamicUtils.twenty);
        rv_display_upload_doc = view.findViewById(R.id.rv_display_upload_doc);
        rv_display_upload_doc.setBackground(getContext().getDrawable(R.drawable.rectangle_light_grey));
        btn_create.setOnClickListener(this);
        rv_display_upload_doc.setVisibility(GONE);
        ll_select_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionREAD_EXTERNAL_STORAGE(getContext());
            }
        });
        tv_selected_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionREAD_EXTERNAL_STORAGE(getContext());
            }
        });
        btn_browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionREAD_EXTERNAL_STORAGE(getContext());
            }
        });
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constants.create_matter) {
//                    submitMatter();
                    if (!upload_documents_list.isEmpty()) {
                        submitMatterInformation();
                    } else {
                        try {
                            update_document();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    //                        if (isDocUploaded) {
//                            update_document();
//                        } else {
                    if (!upload_documents_list.isEmpty()) {
                        submitMatterInformation();
                    } else {
                        try {
                            update_document();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
//                }
                    //  matter.loadViewUI();
                    // callMatterListWebservice();
                }
            }
        });
        at_add_documents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //   documentsList.clear();
//                if (Constants.create_matter)
//                    selected_documents_list.clear();
                try {
                    if (ischecked_doc) {
                        if (documentsList.isEmpty()) {
                            if (!Constants.create_matter)
                                document_list();
                            else
                                DocumentsPopUp();
//                                callDocumentsWebService();
                        } else {
                            DocumentsPopUp();
                        }
                    } else {
//                        DocumentsText();
//                        loadSelectedDocuments(new String[selected_documents_list.size()]);
                        rv_display_upload_doc.setVisibility(GONE);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                ischecked_doc = !ischecked_doc;
            }
        });

        loadDeviceDriveUI();

        matter = (Matter)

                getParentFragment();
        assert matter != null;
        matterArraylist = matter.getMatter_arraylist();
        if (Constants.create_matter) {
            if (!Constants.upload_documents_list.isEmpty()) {
                upload_documents_list.clear();
                upload_documents_list = Constants.upload_documents_list;
                loadUploadedDocuments();
                checkEnableDownloadStatus();
                checkEnableEncryption();
            }
            if (!matterArraylist.isEmpty()) {
                for (int i = 0; i < matterArraylist.size(); i++) {
                    matterModel = matterArraylist.get(i);
                    matter_title_tv.setText("");
                    matter_title_tv.setText(matterModel.getMatter_title());
                    if (matterModel.getClients_list() != null && matterModel.getClients() != null && matterModel.getGroups_list() != null && matterModel.getGroup_acls() != null) {
                        exisiting_group_acls = matterModel.getGroup_acls();
                        existing_clients = matterModel.getClients();
//                existing_members = matterModel.getMembers();
                        existing_corp_clients = matterModel.getCorp_clients_list();
                        existing_groups_list = matterModel.getGroups_list();
                        existing_clients_list = matterModel.getClients_list();
                        existing_temp_clients = matterModel.getTemp_clients_list();
//                existing_tm_list = matterModel.getMembers_list();
                        if (matterModel.getDocuments() != null) {
                            existing_documents = matterModel.getDocuments();
                        }
                        if (matterModel.getDocuments_list() != null) {
                            existing_documents_list = matterModel.getDocuments_list();
                        }
                        try {
                            for (int g = 0; g < exisiting_group_acls.length(); g++) {
                                GroupsModel groupsModel = new GroupsModel();
                                JSONObject jsonObject = exisiting_group_acls.getJSONObject(g);
                                groupsModel.setGroup_id(jsonObject.getString("id"));
                                groupsModel.setGroup_name(jsonObject.getString("name"));
                                groupsModel.setChecked(jsonObject.getBoolean("isChecked"));
                                selected_groups_list.add(groupsModel);
                            }
                            if (existing_documents != null) {
                                for (int d = 0; d < existing_documents.length(); d++) {
                                    DocumentsModel documentsModel = new DocumentsModel();
                                    JSONObject jsonObject = existing_documents.getJSONObject(d);
                                    documentsModel.setDocid(jsonObject.optString("docid"));
                                    documentsModel.setName(jsonObject.optString("name"));
                                    documentsModel.setUser_id(jsonObject.optString("user_id"));
                                    documentsModel.setDoctype(jsonObject.optString("doctype"));
                                    documentsModel.setTags_list(jsonObject.optJSONObject("tags"));
                                    documentsModel.setContentType(jsonObject.optString("contentType"));
                                    documentsModel.setViewUrl(jsonObject.optString("viewUrl"));
                                    documentsModel.setIs_encrypted(jsonObject.optBoolean("is_encrypted"));
                                    documentsModel.setIs_password(jsonObject.optBoolean("is_password"));
                                    documentsModel.setAdded_encryption(jsonObject.optBoolean("added_encryption"));
                                    selected_documents_list.add(documentsModel);
                                    tempSelectedDocuments.add(documentsModel);
                                }
                            }
//                            if (Constants.upload_documents_list != null) {
//                                upload_documents_list.clear();
//                                upload_documents_list = Constants.upload_documents_list;
//                                loadUploadedDocuments();
//                            }
                            if (existing_temp_clients.length() > 0) {
                                for (int m = 0; m < existing_temp_clients.length(); m++) {
                                    ClientsModel clientsModel = new ClientsModel();
                                    JSONObject jsonObject = existing_temp_clients.getJSONObject(m);
                                    clientsModel.setClient_id(jsonObject.getString("id"));
                                    clientsModel.setClient_name(jsonObject.getString("name"));
                                    clientsModel.setRel_id(jsonObject.getString("rel_id"));
                                    clientsModel.setClient_type(jsonObject.getString("type"));
                                    selected_temp_clients_list.add(clientsModel);
                                }
                            }
                            for (int m = 0; m < existing_corp_clients.length(); m++) {
                                ClientsModel clientsModel = new ClientsModel();
                                JSONObject jsonObject = existing_corp_clients.getJSONObject(m);
                                clientsModel.setClient_id(jsonObject.getString("id"));
                                clientsModel.setClient_name(jsonObject.getString("name"));
                                clientsModel.setClient_type(jsonObject.getString("type"));
                                selected_corp_clients_list.add(clientsModel);
                            }
                            if (existing_documents_list != null) {
                                documentsList.clear();
                                for (int ed = 0; ed < existing_documents_list.length(); ed++) {
                                    DocumentsModel documentsModel = new DocumentsModel();
                                    JSONObject jsonObject = existing_documents_list.getJSONObject(ed);
                                    documentsModel.setDocid(jsonObject.optString("docid"));
                                    documentsModel.setName(jsonObject.optString("name"));
                                    documentsModel.setUser_id(jsonObject.optString("user_id"));
                                    documentsModel.setDoctype(jsonObject.optString("doctype"));
                                    documentsModel.setTags_list(jsonObject.optJSONObject("tags"));
                                    documentsModel.setContentType(jsonObject.optString("contentType"));
                                    documentsModel.setViewUrl(jsonObject.optString("viewUrl"));
                                    documentsModel.setIs_encrypted(jsonObject.optBoolean("is_encrypted"));
                                    documentsModel.setIs_password(jsonObject.optBoolean("is_password"));
                                    documentsModel.setAdded_encryption(jsonObject.optBoolean("added_encryption"));
                                    documentsList.add(documentsModel);
                                }

                                for (int k = 0; k < existing_groups_list.length(); k++) {
                                    GroupsModel groupsModel = new GroupsModel();
                                    JSONObject jsonObject = existing_groups_list.getJSONObject(k);
                                    groupsModel.setGroup_id(jsonObject.getString("id"));
                                    groupsModel.setGroup_name(jsonObject.getString("name"));
                                    groupsList.add(groupsModel);
                                }

                                for (int m = 0; m < existing_clients.length(); m++) {
                                    ClientsModel clientsModel = new ClientsModel();
                                    JSONObject jsonObject = existing_clients.getJSONObject(m);
                                    clientsModel.setClient_id(jsonObject.getString("id"));
                                    clientsModel.setClient_name(jsonObject.getString("name"));
                                    clientsModel.setClient_type(jsonObject.getString("type"));
                                    selected_clients_list.add(clientsModel);
                                }
                                for (int c = 0; c < existing_clients_list.length(); c++) {
                                    ClientsModel clientsModel = new ClientsModel();
                                    JSONObject jsonObject = existing_clients_list.getJSONObject(c);
                                    clientsModel.setClient_id(jsonObject.getString("id"));
                                    clientsModel.setClient_name(jsonObject.getString("name"));
                                    clientsModel.setClient_type(jsonObject.getString("type"));
                                    clientsList.add(clientsModel);
                                }
//
                            }
                            if (matterModel.getMembers() != null) {
                                existing_members = matterModel.getMembers();
                                try {
                                    for (int t = 0; t < existing_members.length(); t++) {
                                        TeamModel teamModel = new TeamModel();
                                        JSONObject jsonObject = existing_members.getJSONObject(t);
                                        teamModel.setTm_id(jsonObject.getString("id"));
                                        teamModel.setTm_name(jsonObject.getString("name"));
                                        teamModel.setUser_id(jsonObject.getString("user_id"));
                                        selected_tm_list.add(teamModel);
                                    }
                                } catch (JSONException e) {
                                    e.fillInStackTrace();
                                }
                            }
                            if (matterModel.getMembers_list() != null) {
                                existing_tm_list = matterModel.getMembers_list();
                                try {

                                    for (int d = 0; d < existing_tm_list.length(); d++) {
                                        TeamModel teamModel = new TeamModel();
                                        JSONObject jsonObject = existing_tm_list.getJSONObject(d);
                                        teamModel.setTm_id(jsonObject.getString("id"));
                                        teamModel.setTm_name(jsonObject.getString("name"));
                                        teamModel.setUser_id(jsonObject.getString("user_id"));
                                        tmList.add(teamModel);
                                    }
                                } catch (JSONException e) {
                                    e.fillInStackTrace();
                                }
                            }
                            if (matterArraylist.get(i).getOpponent_advocate() != null) {
                                existing_opponents = matterArraylist.get(i).getOpponent_advocate();
                                try {
                                    for (int j = 0; j < existing_opponents.length(); j++) {
                                        try {
                                            JSONObject jsonObject = existing_opponents.getJSONObject(j);
                                            AdvocateModel advocateModel = new AdvocateModel();
                                            advocateModel.setAdvocate_name(jsonObject.getString("name"));
                                            advocateModel.setNumber(jsonObject.getString("phone"));
                                            advocateModel.setEmail(jsonObject.getString("email"));
                                            advocates_list.add(advocateModel);
                                        } catch (JSONException e) {
                                            e.fillInStackTrace();
                                        }
                                    }
                                } catch (Exception e) {
                                    e.fillInStackTrace();
                                }
                            }
                            if (matterModel.getTags_list() != null) {
                                existing_tags_list = matterModel.getTags_list();
                            }
                            tag_list.clear();
                            if (existing_tags_list != null && existing_tags_list.length() > 0) {
                                for (int j = 0; j < existing_tags_list.length(); j++) {
                                    try {
                                        tag_list.add(existing_tags_list.getString(j)); // ✅ plain string
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                            if (matterModel.getMatter_title() != null) {
                                matter_title = (String) matterModel.getMatter_title();
                            }
                            if (matterModel.getCase_number() != null) {
                                case_number = matterModel.getCase_number();
                            }
                            if (matterModel.getCase_type() != null) {
                                case_type = matterModel.getCase_type();
                            }
                            if (matterModel.getDescription() != null) {
                                description = matterModel.getDescription();
                            }
                            if (matterModel.getDate_of_filing() != null) {
                                dof = matterModel.getDate_of_filing();
                            }
                            if (matterModel.getStart_date() != null) {
                                start_date = matterModel.getStart_date();
                            }
                            if (matterModel.getEnd_date() != null) {
                                end_date = matterModel.getEnd_date();
                            }
                            if (matterModel.getCourt() != null) {
                                court = matterModel.getCourt();
                            }
                            if (matterModel.getJudge() != null) {
                                judge = matterModel.getJudge();
                            }
                            if (matterModel.getCase_priority() != null) {
                                case_priority = matterModel.getCase_priority();
                            }
                            if (matterModel.getStatus() != null) {
                                case_status = matterModel.getStatus();
                            }
                            if (!selected_documents_list.isEmpty()) {
                                DocumentsText();
                                loadSelectedDocuments(new String[selected_documents_list.size()]);
                            }
                        } catch (JSONException e) {
                            e.fillInStackTrace();
                        }
                    }
                }

            }
            ll_matterDate.setVisibility(GONE);
            ll_upload_type.setVisibility(GONE);
            btn_create.setText(R.string.save);
        } else {
            matter_title_tv.setText("");
            matter_title_tv.setText(Constants.GeneratedMatterTitle);
            ll_upload_type.setVisibility(GONE);
            isChangesOccured = false;
//            AndroidUtils.ToggleButton(0, btn_create);
            btn_create.setText(R.string.save);
            matter_date.setText(Constants.matterDate);
//            ll_matterDate.setVisibility(View.VISIBLE);

            existing_document();
            loadDeviceDriveUI();
        }

        //        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
//            @Override
//            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//                myCalendar.set(Calendar.YEAR, year);
//                myCalendar.set(Calendar.MONTH, month);
//                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//                updateLabel();
//            }
//
//            private void updateLabel() {
//                String myFormat = "dd-MM-yyyy";
//                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
//                matter_date.setText(sdf.format(myCalendar.getTime()));
//            }
//        };
//        matter_date.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
//                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
//                datePickerDialog.show();
//            }
//        });
        tv_enable_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnableDownloadBackground();
                adapter.EnableAllorDisableAll(true);
            }
        });
        tv_disable_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisableDownloadBackground();
                adapter.EnableAllorDisableAll(false);
            }
        });
        tv_enable_encryption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ENCRYPTION_TAG = true;
                if (ENCRYPTION_TAG) {
                    EnableEncryptionBackground();
                }
                ENCRYPTION_TAG = !ENCRYPTION_TAG;
            }
        });
        tv_disable_encryption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DECRYPTION_TAG = true;
                if (DECRYPTION_TAG) {
                    DisableEncryptionBackground();
                    adapter.EncryptAllorDecryptAll(false);
                    String tag = "dis_encrption";
                    loadRecyclerview(tag, subtag);
                }
                DECRYPTION_TAG = !DECRYPTION_TAG;
            }
        });
        if (Constants.create_matter) {
            callDocumentsWebService();
        }
        rv_display_upload_doc.setVisibility(GONE);
        chk_select_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chk_select_all.setChecked(isselect_all_checked);
                isselect_all_checked = !isselect_all_checked;
                isAnyOneSelected(chk_select_all.isChecked());
                adapter.selectOrDeselectAll(chk_select_all.isChecked());
            }
        });
        tv_add_tag.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (is_clicked_add) {
                    AddTag();
                } else {
                    Hide_Add_EditMeta();
                }
                is_clicked_edit = true;
                is_clicked_add = !is_clicked_add;
            }
        });
//        iv_remove_matter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!selected_documents_list.isEmpty() || !upload_documents_list.isEmpty()) {
//                    AndroidUtils.showConfirmation(
//                            getActivity(),
//                            requireContext().getString(R.string.leavepage),
//                            requireContext().getString(R.string.changes_you_made_may_not_be_saved),
//                            requireContext().getString(R.string.leave),
//                            new AndroidUtils.OnConfirmListener() {
//                                @Override
//                                public void onSave() {
//                                    matter.loadViewUI();
//                                }
//
//                                @Override
//                                public void onCancel() {
//                                }
//                            }
//                    );
//                } else {
//                    matter.loadViewUI();
//                }
//            }
//        });
//        if (Constants.create_matter) {
//            btn_cancel_save.setText(R.string.save_later);
//        } else {
        btn_cancel_save.setText(R.string.cancel);
//        }
        iv_remove_matter.setOnClickListener(v -> handleLeaveClick());
        btn_cancel_save.setOnClickListener(v -> handleLeaveClick());
        tv_edit_meta.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (is_clicked_edit) {
                    EditMeta();
                } else {
                    Hide_Add_EditMeta();
                }
                is_clicked_add = true;
                is_clicked_edit = !is_clicked_edit;
            }
        });
        btn_add_tags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected_upload_documents_list.clear();
                for (int i = 0; i < adapter.getList_item().size(); i++) {
                    com.digicoffer.lauditor.Documents.Models.DocumentsModel documentsModel = adapter.getList_item().get(i);
                    if (documentsModel.isChecked()) {
//                            if (documentsModel.getTags() == null) {
                        selected_upload_documents_list.add(documentsModel);
//                            }
                    }
                }
                open_add_tags_popup();
            }
        });
        return view;
    }

    private void handleLeaveClick() {
        boolean shouldWarn;

        if (Constants.create_matter) {
            // For creation mode, warn only if clients or team members selected
            shouldWarn = !selected_documents_list.isEmpty() || !upload_documents_list.isEmpty();
        } else {
            // For edit mode, warn if any changes occurred
            shouldWarn = isChangesOccured;
        }
        if (shouldWarn) {
            AndroidUtils.showConfirmation(
                    getActivity(),
                    requireContext().getString(R.string.leavepage),
                    requireContext().getString(R.string.changes_you_made_may_not_be_saved),
                    requireContext().getString(R.string.leave),
                    new AndroidUtils.OnConfirmListener() {
                        @Override
                        public void onSave() {
                            matter.loadViewUI();
                        }

                        @Override
                        public void onCancel() {
                        }
                    }
            );
        } else {
            matter.loadViewUI();
        }
    }

    private void AddTag() {
//        chk_box_layout.setAlpha(1F);
//        chk_select_all.setEnabled(true);
        btn_create.setVisibility(View.GONE);
        btn_add_tags.setVisibility(View.VISIBLE);
        tv_edit_meta.setTextColor(getContext().getResources().getColor(R.color.black));
        tv_add_tag.setTextColor(getContext().getResources().getColor(R.color.white));
        tv_edit_meta.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
        tv_add_tag.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        String tag = "add_tag";
        Constants.DocTagType = tag;
        for (int i = 0; i < upload_documents_list.size(); i++) {
            upload_documents_list.get(i).setChecked(false);
        }
        loadRecyclerview(tag, subtag);
    }

    //Hide when
    private void Hide_Add_EditMeta() {
//        chk_box_layout.setAlpha(0.5F);
//        chk_select_all.setEnabled(false);
//        chk_select_all.setChecked(false);
        isselect_all_checked = true;
//        chk_select_all.setChecked(false);
        btn_create.setVisibility(View.VISIBLE);
//        btn_add_tags.setVisibility(View.GONE);
        tv_edit_meta.setTextColor(getContext().getResources().getColor(R.color.black));
        tv_add_tag.setTextColor(getContext().getResources().getColor(R.color.black));
        tv_add_tag.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_edit_meta.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
        String tag = "Hide_Add_Edit_tag";
        Constants.DocTagType = tag;
        for (int i = 0; i < upload_documents_list.size(); i++) {
            upload_documents_list.get(i).setChecked(false);
        }
        loadRecyclerview(tag, subtag);
    }

    private void EditMeta() {
//        chk_box_layout.setAlpha(0.5F);
//        chk_select_all.setEnabled(false);
        isselect_all_checked = true;
//        chk_select_all.setChecked(false);
        btn_create.setVisibility(View.VISIBLE);
//        btn_add_tags.setVisibility(View.GONE);
        tv_edit_meta.setTextColor(getContext().getResources().getColor(R.color.white));
        tv_add_tag.setTextColor(getContext().getResources().getColor(R.color.black));
        tv_add_tag.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_edit_meta.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_background));
        String tag = "edit_meta";
        Constants.DocTagType = tag;
        // Update documents list if required
        for (int i = 0; i < upload_documents_list.size(); i++) {
            upload_documents_list.get(i).setChecked(false);
        }
        loadRecyclerview(tag, subtag);
    }

    private void DisableEncryptionBackground() {
        tv_enable_encryption.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_enable_encryption.setTextColor(getContext().getColor(R.color.black));
        tv_disable_encryption.setTextColor(getContext().getColor(R.color.white));
        tv_disable_encryption.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_background));
    }

    private void EnableEncryptionBackground() {
        ENCRYPTION_TAG = true;
        tv_enable_encryption.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        tv_enable_encryption.setTextColor(getContext().getColor(R.color.white));
        tv_disable_encryption.setTextColor(getContext().getColor(R.color.black));
        tv_disable_encryption.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
        adapter.EncryptAllorDecryptAll(true);
        String tag = "en_encrption";
        loadRecyclerview(tag, subtag);
    }

    private void DisableDownloadBackground() {
        DOWNLOAD_TAG = false;
        tv_enable_download.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_enable_download.setTextColor(getContext().getColor(R.color.black));
        tv_disable_download.setTextColor(getContext().getColor(R.color.white));
        tv_disable_download.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_background));
        String tag = "disable_download";
        loadRecyclerview(tag, subtag);
    }

    private void EnableDownloadBackground() {
        DOWNLOAD_TAG = true;
        tv_disable_download.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
        tv_enable_download.setTextColor(getContext().getColor(R.color.white));
        tv_disable_download.setTextColor(getContext().getColor(R.color.black));
        tv_enable_download.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        String tag = "enable_download";
        loadRecyclerview(tag, subtag);
    }

    private void Disable_allEncryptionBackground() {
        tv_enable_encryption.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_enable_encryption.setTextColor(getContext().getColor(R.color.black));
        tv_disable_encryption.setTextColor(getContext().getColor(R.color.white));
        tv_disable_encryption.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_background));
        String tag = "dis_encrption";
        loadRecyclerview(tag, subtag);
    }

    private void Enable_allEncryptionBackground() {
        tv_enable_encryption.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        tv_enable_encryption.setTextColor(getContext().getColor(R.color.white));
        tv_disable_encryption.setTextColor(getContext().getColor(R.color.black));
        tv_disable_encryption.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
//        adapter.EncryptAllorDecryptAll(true);
        String tag = "en_encrption";
        loadRecyclerview(tag, subtag);
    }

    private void Disable_AllDownloadBackground() {
        tv_enable_download.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_enable_download.setTextColor(getContext().getColor(R.color.black));
        tv_disable_download.setTextColor(getContext().getColor(R.color.white));
        tv_disable_download.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_background));
        String tag = "disable_download";
        loadRecyclerview(tag, subtag);
    }

    private void Enable_ALlDownloadBackground() {
        tv_disable_download.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
        tv_enable_download.setTextColor(getContext().getColor(R.color.white));
        tv_disable_download.setTextColor(getContext().getColor(R.color.black));
        tv_enable_download.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        String tag = "enable_download";
        loadRecyclerview(tag, subtag);
    }

    public void checkEnableDownloadStatus() {
        boolean allEnabled = true;
        boolean allDisabled = true;

        for (com.digicoffer.lauditor.Documents.Models.DocumentsModel item : upload_documents_list) {
            if (item.isIsenabled()) {
                allDisabled = false; // At least one is encrypted
            } else {
                allEnabled = false; // At least one is decrypted
            }

            if (!allDisabled && !allEnabled) {
                break; // No need to check further
            }
        }

        if (allEnabled) {
            check_enabled_all(true); // Enable encryption background
        } else if (allDisabled) {
            check_enabled_all(false); // Disable encryption background
        }
//        loadUploadedDocuments();
    }

    public void checkEnableEncryption() {
        boolean allEnabled = true;
        boolean allDisabled = true;

        for (com.digicoffer.lauditor.Documents.Models.DocumentsModel item : upload_documents_list) {
            if (item.getIsencrypted()) {
                allDisabled = false; // At least one is encrypted
            } else {
                allEnabled = false; // At least one is decrypted
            }

            if (!allDisabled && !allEnabled) {
                break; // No need to check further
            }
        }

        if (allEnabled) {
            check_encrypted_all(true); // Enable encryption background
        } else if (allDisabled) {
            check_encrypted_all(false); // Disable encryption background
        }
//        loadUploadedDocuments();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_document_library:
                loadDocumentLibraryUI();
                break;
            case R.id.tv_device_drive:
                loadDeviceDriveUI();
                break;
//            case R.id.btn_add_documents:
//                ischecked_doc=true;
//                break;
            case R.id.btn_browse:
                checkPermissionREAD_EXTERNAL_STORAGE(getContext());
                break;


            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }

    }

    private void submitMatterInformation() {
        if (selected_documents_list.isEmpty() && upload_documents_list.isEmpty()) {
            // AndroidUtils.showAlert("Please add atleast one document from existing documents or upload a new one",getContext());
        } else {
            try {
                if (!upload_documents_list.isEmpty()) {
                    try {
                        totalUploads = upload_documents_list.size();
                        completedUploads = 0;
                        isAlertShown = false;
                        progress_dialog = AndroidUtils.get_progress(getActivity());
//                            changedCollection = upload_documents_list.size();
                        for (int i = 0; i < upload_documents_list.size(); i++) {
//                            changedCollection--;
                            String name = upload_documents_list.get(i).getName();
                            JSONArray new_clients = new JSONArray();
                            JSONArray new_groups = new JSONArray();
                            JSONObject clients_jobject = new JSONObject();
                            JSONArray matter = new JSONArray();
//                JSONArray tags = new JSONArray();
                            String docname = "";
                            com.digicoffer.lauditor.Documents.Models.DocumentsModel documentsModel = upload_documents_list.get(i);
//                           MergedList.addAll(Collections.singleton(documentsModel));
                            filename = documentsModel.getName();
                            File new_file = documentsModel.getFile();
                            boolean isenabled = documentsModel.isIsenabled();
                            String doc_type = "pdf";
                            String content_string = new_file.getName().replace(".", "/");
                            String[] content_type = content_string.split("/");
                            if (content_type.length >= 2) {
                                doc_type = content_type[1];
                                docname = content_type[0];
                            }

//                            for (int j = 0; j < selected_clients_list.size(); j++) {
////                                if (sele.get(j).getId().matches(client_id)) {
//                                ClientsModel clientsModel = selected_clients_list.get(j);
//                                clients_jobject.put("id", clientsModel.getClient_id());
//                                clients_jobject.put("type", clientsModel.getClient_type());
//                                new_clients.put(clients_jobject);
//                            }
//                            for (int g = 0; g < selected_groups_list.size(); g++) {
//                                GroupsModel groupsModel = selected_groups_list.get(g);
//                                new_groups.put(groupsModel.getGroup_id());
//                            }

                            JSONArray matters = new JSONArray();
                            matters.put(Constants.Matter_id);
                            JSONObject jsonObject = new JSONObject();
//                            matter.put(matter_id);
                            uploaded_document_name = name;
                            upload_description = upload_documents_list.get(i).getDescription();
                            upload_exp_date = upload_documents_list.get(i).getExpiration_date();
                            jsonObject.put("name", name);
                            jsonObject.put("description", upload_description);
                            jsonObject.put("expiration_date", (AndroidUtils.convertAnyDateToDDMMYYYY(upload_exp_date)));
                            jsonObject.put("filename", docname);
                            jsonObject.put("matters", matters);
                            jsonObject.put("category", "client");
                            if (Constants.clientList.length() > 0) {
                                jsonObject.put("clients", Constants.clientList);
                            } else {
                                jsonObject.put("clients", Constants.corpclientList);
                            }
                            jsonObject.put("groups", Constants.ex_group_attachment);
                            jsonObject.put("downloadDisabled", upload_documents_list.get(i).isIsenabled());
                            jsonObject.put("custom_encrypt", upload_documents_list.get(i).getIsencrypted());
                            if (upload_documents_list.get(i).getTags() == null) {
                                jsonObject.put("tags", "");
                            } else {
                                jsonObject.put("tags", upload_documents_list.get(i).getTags());
                            }

                            if (doc_type.equalsIgnoreCase("apng") || doc_type.equalsIgnoreCase("avif") || doc_type.equalsIgnoreCase("gif") || doc_type.equalsIgnoreCase("jpeg") || doc_type.equalsIgnoreCase("png") || doc_type.equalsIgnoreCase("svg") || doc_type.equalsIgnoreCase("webp") || doc_type.equalsIgnoreCase("jpg")) {
                                jsonObject.put("content_type", "image/" + doc_type);
                            } else {
                                jsonObject.put("content_type", "application/" + doc_type);
                            }
//                            upload_documents_list.remove(documentsModel);
                            WebServiceHelper.callHttpUploadWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v3/document/upload", "Upload Document", new_file, jsonObject.toString());
                        }
//            AndroidUtils.showAlert(jsonObject.toString(),getContext());
                    } catch (JSONException e) {
                        if (progress_dialog != null && progress_dialog.isShowing())
                            AndroidUtils.dismiss_dialog(progress_dialog);
                        e.fillInStackTrace();
                    }
                } else {
                    submitMatter();
                }
//                matter.loadDocuments();

            } catch (Exception e) {
                if (progress_dialog != null && progress_dialog.isShowing())
                    AndroidUtils.dismiss_dialog(progress_dialog);
                e.fillInStackTrace();
            }
        }
    }

    // Define ActivityResultLauncher

    // Keep this only for Android 6-12
    ActivityResultLauncher<String[]> requestPermissions =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    results -> {
                        boolean permissionGranted =
                                Boolean.TRUE.equals(results.get(READ_EXTERNAL_STORAGE));
                        if (permissionGranted) {
                            BottomSheetUploadfile();
                        }
                    }
            );

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ : No permission needed, directly open BottomSheet
            BottomSheetUploadfile();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6 to 12 : check READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.launch(new String[]{READ_EXTERNAL_STORAGE});
            } else {
                BottomSheetUploadfile();
            }
        } else {
            // Android 5 and below
            BottomSheetUploadfile();
        }
        return true;
    }
    public void DocumentsText() {
        AndroidUtils.ToggleButton(tempSelectedDocuments.size(), btn_add_documents);
        if (tempSelectedDocuments.isEmpty()) {
            at_add_documents.setText("");
        } else {
            String[] value = new String[tempSelectedDocuments.size()];
            for (int i = 0; i < tempSelectedDocuments.size(); i++) {
                value[i] = tempSelectedDocuments.get(i).getName();
            }
            at_add_documents.setText(String.join(", ", value));
        }
    }


    private void callDocumentsWebService() {
        try {
            JSONArray group_acls = new JSONArray();
            JSONArray clients = new JSONArray();
            JSONObject postdata = new JSONObject();
            for (int i = 0; i < selected_groups_list.size(); i++) {
                GroupsModel groupsModel = selected_groups_list.get(i);
                group_acls.put(groupsModel.getGroup_id());
            }
            for (int i = 0; i < selected_clients_list.size(); i++) {
                ClientsModel clientsModel = selected_clients_list.get(i);
                clients.put(clientsModel.getClient_id());
            }
            if (!selected_temp_clients_list.isEmpty()) {
                for (int i = 0; i < selected_temp_clients_list.size(); i++) {
                    ClientsModel clientsModel = selected_temp_clients_list.get(i);
                    clients.put(clientsModel.getClient_id());
                }
            }
            if (!selected_corp_clients_list.isEmpty()) {
                for (int i = 0; i < selected_corp_clients_list.size(); i++) {
                    ClientsModel clientsModel = selected_corp_clients_list.get(i);
                    clients.put(clientsModel.getClient_id());
                }
            }
            postdata.put("clients", clients);
            postdata.put("group_acls", group_acls);
            postdata.put("attachment_type", "documents");
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "matter/attachments", "Documents", postdata.toString());
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    private void loadDeviceDriveUI() {
//        selected_documents_list.clear();
//        documentsList.clear();
//        ll_selected_documents.removeAllViews();
        isDocUploaded = !upload_documents_list.isEmpty();
//        tv_selected_document.setVisibility(GONE);
//        at_add_documents.setText("");
//        tv_selected_file.setText("");
        tv_document_library.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_document_library.setTextColor(Color.BLACK);
        tv_device_drive.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_background));
        tv_device_drive.setTextColor(Color.WHITE);
        ll_add_documents.setVisibility(GONE);
//        ll_select_doc.setVisibility(VISIBLE);
        AndroidUtils.ToggleButton(upload_documents_list.size(), chk_select_all);
        AndroidUtils.ToggleButton(upload_documents_list.size(), chk_box_layout);
        loadSelectedDocuments(new String[selected_documents_list.size()]);
        UploadedDocView();
//        ll_selected_documents.removeAllViews();
//        documentsList.clear();
//        at_add_documents.setText(R.string.select_document);

    }

    private void loadDocumentLibraryUI() {
//        selected_documents_list.clear();
//        documentsList.clear();
        isDocUploaded = true;
        ll_uploaded_documents.removeAllViews();
        ll_uploaded_documents.setVisibility(GONE);
        DocumentsText();
        loadSelectedDocuments(new String[selected_documents_list.size()]);
//        loadSelectedDocuments();
//        at_add_documents.setText("");
//        tv_selected_file.setText("");
        tv_document_library.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        tv_document_library.setTextColor(Color.WHITE);
        tv_device_drive.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
        tv_device_drive.setTextColor(Color.BLACK);
        ll_add_documents.setVisibility(VISIBLE);
        ll_select_doc.setVisibility(GONE);
    }

    private void BottomSheetUploadfile() {
        cl_matter_document.setAlpha(0.5f);
        Constants.isDocEditor = false;
        bottommSheetUploadDocument = new BottomSheetUploadFile(cl_matter_document);
        bottommSheetUploadDocument.show(getParentFragmentManager(), "");
        bottommSheetUploadDocument.setTargetFragment(MatterDocuments_En.this, 1);
    }

    @SuppressLint("Range")
    @Override
    public void getImagepath(File imagepath, Uri ImageURI) throws IOException {
        if ((imagepath == null)) {
            mSelectedBitmap = null;
            mSelectedUri = imagepath;
            String uri = imagepath.toString();
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(ImageLoaderConfiguration.createDefault(requireActivity()));
            imageLoader.displayImage(String.valueOf(Uri.fromFile(new File(uri))), imageView);
            file = imagepath;
            Cursor c = requireContext().getContentResolver().query(ImageURI, null, null, null, null);
            assert c != null;
            c.moveToFirst();
            String[] content_type = file.getName().split(".");
            String file_name = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
//            tv_selected_file.setText(file_name);

            load_documents(file_name, file);
        } else {
            file = getFile(requireContext(), ImageURI);
            Log.i("FILE", "Info:" + file.toString());
            String file_name = file.getName();
//            tv_selected_file.setText(file_name);
//            DocumentsModel documentsModel = new DocumentsModel();
//            documentsModel.setName(file.getName());
//            docsList.add(documentsModel);
            load_documents(file_name, file);
//            docsList.add()
        }
        cl_matter_document.setAlpha(1.0f);
        bottommSheetUploadDocument.dismiss();

    }

    private void load_documents(String file_name, File file) {
//
//        try {
//             filedata= convertfiletostring(file).getBytes();
//            Log.d("File_data1", Arrays.toString(filedata));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        try {
//            String filedata= convertfiletostring(file);
//            Log.d("File_data2", filedata);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

//        for (int i = 0; i < docsList.size(); i++) {
//            View view = LayoutInflater.from(getContext()).inflate(R.layout.displays_documents_list, null);
//            TextView tv_docname = view.findViewById(R.id.tv_document_name);
//            tv_docname.setText(docsList.get(i).getName());
        String doc_type = "";
        String docname = "";
        String content_string = file_name.replace(".", "/");
        String[] content_type = content_string.split("/");
        if (content_type.length >= 2) {
            doc_type = content_type[1];
            docname = content_type[0];
        }
        com.digicoffer.lauditor.Documents.Models.DocumentsModel documentsModel = new com.digicoffer.lauditor.Documents.Models.DocumentsModel();
        documentsModel.setName(docname);
        documentsModel.setFilename(file_name);
        documentsModel.setContent_type(doc_type);
        documentsModel.setDescription(docname);
        documentsModel.setFile(file);
        documentsModel.setIsenabled(false);
        documentsModel.setChecked(false);
        upload_documents_list.add(documentsModel);
        Constants.upload_documents_list = upload_documents_list;
        if (!upload_documents_list.isEmpty()) {
//            ll_hide_document_details.setVisibility(View.VISIBLE);
            DisableDownloadBackground();
            tv_enable_encryption.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
            tv_enable_encryption.setTextColor(getContext().getColor(R.color.black));
            tv_disable_encryption.setTextColor(getContext().getColor(R.color.white));
            tv_disable_encryption.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_background));
        } else if (upload_documents_list.isEmpty()) {
//            ll_hide_document_details.setVisibility(View.GONE);
//            hideDisableDownloadBackground();
        }
        EditMeta();
        is_clicked_edit = true;
        is_clicked_add = true;
        loadUploadedDocuments();
//            ll_documents.addView(view);
//        }
    }

    @NonNull
    private static com.digicoffer.lauditor.Documents.Models.DocumentsModel getDocumentsModel(String file_name, File file) {
        String doc_type = "";
        String docname = "";
        String content_string = file_name.replace(".", "/");
        String[] content_type = content_string.split("/");
        if (content_type.length >= 2) {
            doc_type = content_type[1];
            docname = content_type[0];
        }
        com.digicoffer.lauditor.Documents.Models.DocumentsModel documentsModel = new com.digicoffer.lauditor.Documents.Models.DocumentsModel();
        documentsModel.setName(docname);
        documentsModel.setDescription(docname);
        documentsModel.setExpiration_date("");
        documentsModel.setFile(file);
        documentsModel.setIsenabled(false);
        return documentsModel;
    }

    public static File getFile(Context context, Uri uri) throws IOException {
        File destinationFilename = new File(context.getFilesDir().getPath() + File.separatorChar + queryName(context, uri));
        try (InputStream ins = context.getContentResolver().openInputStream(uri)) {
            createFileFromStream(ins, destinationFilename);
        } catch (Exception ex) {
            Log.e("Save File", Objects.requireNonNull(ex.getMessage()));
            ex.fillInStackTrace();
        }
        return destinationFilename;
    }

    public static void createFileFromStream(InputStream ins, File destination) {
        try (OutputStream os = Files.newOutputStream(destination.toPath())) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = ins.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
        } catch (Exception ex) {
            Log.e("Save File", Objects.requireNonNull(ex.getMessage()));
            ex.fillInStackTrace();
        }
    }

    private static String queryName(Context context, Uri uri) {
        Cursor returnCursor =
                context.getContentResolver().query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        mSelectedBitmap = bitmap;
        mSelectedUri = null;
//        tv_upload_file.setEnabled(false);
        File filesDir = requireContext().getFilesDir();
        File imageFile = new File(filesDir, "bitmap" + ".jpg");
        OutputStream os;
        try {
            os = Files.newOutputStream(imageFile.toPath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
            file = imageFile;
//            tv_selected_file.setText(file.getName());
            DocumentsModel documentsModel = new DocumentsModel();
            documentsModel.setName(file.getName());
            selected_documents_list.add(documentsModel);
            tempSelectedDocuments.add(documentsModel);
//            loadSelectedDocuments();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = requireContext().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            file = new File(picturePath);
            filename = file.getName();
//            tv_selected_file.setText(file.getName());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 123:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
//                    upload_file();
                } else {
                    AndroidUtils.showAlert("GET_ACCOUNTS Denied", getActivity());
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    public void callDecryptApi(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("docid", id);
            jsonObject.put("download", false);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, Constants.decryptUrl, "Decrypt Doc", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    // create an async task class for loading pdf file from URL.
    private void handleDocumentDisplay(String url, DocumentsModel docModel, PDFView idPDFView, ImageView iv_image) {
        boolean isImage = File_Content_Type.isImage(docModel.getContentType());
        boolean isPDF = File_Content_Type.isPDF(docModel.getContentType());
        boolean isEncrypted = docModel.isAdded_encryption() || docModel.isIs_encrypted();

        if (isEncrypted) {
            // Encrypted doc → call decrypt API
            dialog.dismiss();
            callDecryptApi(docModel.getDocid());
            return;
        }

        if (isImage) {
            // Show image
            idPDFView.setVisibility(View.GONE);
            iv_image.setVisibility(View.VISIBLE);

            Glide.with(getContext())
                    .load(url)
                    .placeholder(R.drawable.progress_animation)
                    .centerCrop()
                    .into(iv_image);
        } else if (isPDF) {
            // Show PDF
            iv_image.setVisibility(View.GONE);
            idPDFView.setVisibility(View.VISIBLE);
//            new RetrievePDFfromUrl(idPDFView).execute(url);
        } else {
            // Other documents → call WebView API
            dialog.dismiss();
            callOtherDocViewApi(docModel.getDocid());
        }
    }

    public void callOtherDocViewApi(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("docid", id);
            jsonObject.put("download", false);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, Constants.base_URL + "v3/document/" + id + "/view", "Other Doc View", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                if (httpResult.getRequestType().equals("Upload Document")) {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    completedUploads++;

                    boolean isError = result.optBoolean("error");
                    String msg = result.optString("msg");

                    if (!isError) {
                        String docid = result.getString("docid");
                        addDocsToMatter(docid);  // Just add to list here
                    }
                    if (completedUploads == totalUploads && !isAlertShown) {
                        isAlertShown = true;
                        if (progress_dialog != null && progress_dialog.isShowing())
                            AndroidUtils.dismiss_dialog(progress_dialog);
                        if (isError) {
                            AndroidUtils.showAlert(msg, getActivity());
                        } else {
                            // Once all documents are added, trigger the update once
                            update_document();
                            matterArraylist.clear();
                            matter.loadViewUI();
//                            AndroidUtils.showAlert(msg, getActivity());
                        }
                    }
                } else {
                    if (progress_dialog != null && progress_dialog.isShowing())
                        AndroidUtils.dismiss_dialog(progress_dialog);
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    if (httpResult.getRequestType().equals("Documents")) {
                        JSONArray data = result.getJSONArray("documents");
//                    AndroidUtils.showAlert(data.toString(),getContext());
                        loadDocumentsData(data);
                    } else if (httpResult.getRequestType().equals("Update Members")) {
                        boolean isError = result.optBoolean("error");
                        String msg = result.optString("msg");
                        if (isError) {
                            AndroidUtils.showAlert(msg, getActivity());
                        }
                    } else if (httpResult.getRequestType().equals("Documents_List")) {
                        JSONArray data = result.getJSONArray("documents");
//                    AndroidUtils.showAlert(data.toString(),getContext());
                        loadDocumentsData(data);
                    } else if (httpResult.getRequestType().equals("matter_update")) {
                        String msg = result.getString("msg");
                        AndroidUtils.showAlert("" + msg, getActivity(), "Success");
//                    if (!upload_documents_list.isEmpty()) {
//                        submitMatterInformation();
//                    }
                        matter.loadViewUI();
                    } else if (httpResult.getRequestType().equals("Update Documents List")) {
                        String msg = result.getString("msg");
//                    AndroidUtils.showAlert("" + msg, getActivity(),"");
                        matterArraylist.clear();
                        matter.loadViewUI();
                        if (!Constants.ROLE.equals("TM"))
                            if (!selected_temp_clients_list.isEmpty()) {
                                triggerUpdateMembersForTempClients();
                            }
                    } else if (httpResult.getRequestType().equals("Chosen_Documents")) {
                        JSONArray data = result.getJSONArray("documents");
                        display_doc(data);

                        if (!selected_documents_list.isEmpty()) {
                            DocumentsText();
                            loadSelectedDocuments(new String[selected_documents_list.size()]);
                        }
                        isInitialLoad = false;    // ← ADD THIS
                        isChangesOccured = false; // ← ADD THIS
                    } else if (httpResult.getRequestType().equals("Decrypt Doc") || (httpResult.getRequestType().equals("Other Doc View"))) {
                        boolean error = result.getBoolean("error");
                        if (!error) {
                            String url = result.getJSONObject("data").getString("url");
//                        String contentType=result.getJSONObject("data").getString("content_type");
//                        sharedDocumentsDo.setContentType(contentType);
                            display_doc(url, sharedDocumentsDo);
                            Log.d("TAG_Image", url);
                        } else {
                            String msg = result.optString("msg", "");
                            AndroidUtils.showAlert(msg, getActivity());
                        }
                    } else if (httpResult.getRequestType().equals("View Doc")) {
                        boolean error = result.getBoolean("error");
                        if (!error) {
                            String url = result.getJSONObject("data").getString("url");
                            checkViewType(url, sharedDocumentsDo);
                            Log.d("TAG_Image", url);
                        } else {
                            String msg = result.optString("msg", "");
                            AndroidUtils.showAlert(msg, getActivity());
                        }
                    }
                    if (httpResult.getRequestType().equals("Matter List")) {
                        boolean error = result.getBoolean("error");
                        if (error) {
                            String msg = result.getString("msg");
                            AndroidUtils.showAlert(msg, getActivity());
                        } else {
                            JSONArray matters = result.getJSONArray("matters");
                            try {
                                loadMattersList(matters);
                            } catch (Exception e) {
                                AndroidUtils.showAlert(e.getMessage(), getActivity());
                                e.fillInStackTrace();
                            }
                        }
                    } else if (Objects.equals(httpResult.getRequestType(), "Create Matter")) {
                        boolean error = result.getBoolean("error");
                        String msg = result.getString("msg");
                        if (error) {
//                        If the message gets an error msg...
                            AndroidUtils.showAlert(msg, getActivity());
                        } else {
                            AndroidUtils.showAlert(msg, getActivity(), "");
                            matter_id = result.getString("matter_id");
                            matterArraylist.clear();
                            matter.loadViewUI();
                            if (!selected_documents_list.isEmpty())
                                UpdateDocWebservice();
                        }
                    }
                }
            } catch (JSONException e) {
                e.fillInStackTrace();
                if (progress_dialog != null && progress_dialog.isShowing())
                    AndroidUtils.dismiss_dialog(progress_dialog);
            }
        } else if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Failed) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                if (result.optBoolean("error")) {
                    AndroidUtils.showErrorAlert(result.optString("msg"), getActivity());
                }
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        } else {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            AndroidUtils.showErrorAlert(httpResult.getResponseContent().toString(), getActivity());
        }
    }

    public void UpdateDocWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            JSONArray documents = new JSONArray();
            for (int i = 0; i < selected_documents_list.size(); i++) {
                DocumentsModel documentsModel = selected_documents_list.get(i);
                documents.put(documentsModel.getDocid());
            }
            postdata.put("documents", documents);
            postdata.put("matter_id", matter_id);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PATCH, "v3/update", "Update Documents List", postdata.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void triggerUpdateMembersForTempClients() {
        for (int i = 0; i < selected_temp_clients_list.size(); i++) {
            // Assuming your temp client model has getId() method
            String tempClientId = selected_temp_clients_list.get(i).getRel_id();
            callUpdateMembers(tempClientId);
        }
    }

    private void callUpdateMembers(String id) {
        try {
            JSONObject postdata = new JSONObject();
            JSONArray members = new JSONArray();
            if (!selected_temp_clients_list.isEmpty()) {

                for (int i = 0; i < selected_tm_list.size(); i++) {
                    TeamModel teamModel = selected_tm_list.get(i);
                    members.put(teamModel.getTm_id());
                }
            }
            postdata.put("members", members);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "v2/relationship/" + id + "/members", "Update Members", postdata.toString());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void submitMatter() {
        try {
            if (!selected_corp_clients_list.isEmpty())
                corp_client_id = selected_corp_clients_list.get(0).getClient_id();
//            if (upload_documents_list.isEmpty()) {
            String Matter_type = "legal";
            JSONObject postdata = new JSONObject();
            JSONArray clients = new JSONArray();
            JSONArray documents = new JSONArray();
            JSONArray group_acls = new JSONArray();
            JSONArray members = new JSONArray();
            JSONArray opponent_advocates = new JSONArray();

            for (int i = 0; i < selected_clients_list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                ClientsModel clientsModel = selected_clients_list.get(i);
                jsonObject.put("id", clientsModel.getClient_id());
                jsonObject.put("type", clientsModel.getClient_type());
                clients.put(jsonObject);
            }
            if (!selected_temp_clients_list.isEmpty()) {
                for (int i = 0; i < selected_temp_clients_list.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    ClientsModel clientsModel = selected_temp_clients_list.get(i);
                    jsonObject.put("id", clientsModel.getClient_id());
                    jsonObject.put("type", clientsModel.getClient_type());
                    clients.put(jsonObject);
                }
            }
            if (!corp_client_id.isEmpty()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", corp_client_id);
                jsonObject.put("type", "corporate");
                clients.put(jsonObject);
            }
            for (int i = 0; i < selected_documents_list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                DocumentsModel documentsModel = selected_documents_list.get(i);
                jsonObject.put("docid", documentsModel.getDocid());
                jsonObject.put("doctype", documentsModel.getDoctype());
                jsonObject.put("user_id", documentsModel.getUser_id());
                documents.put(jsonObject);
            }
            matterModel.setDocuments(documents);
            existing_tags_list = new JSONArray();
            for (int i = 0; i < tag_list.size(); i++) {
                existing_tags_list.put(tag_list.get(i));  // no JSONObject
            }
            matterModel.setTags_list(existing_tags_list);
            for (int i = 0; i < selected_groups_list.size(); i++) {
                GroupsModel groupsModel = selected_groups_list.get(i);
                group_acls.put(groupsModel.getGroup_id());
            }

            for (int i = 0; i < selected_tm_list.size(); i++) {
                TeamModel teamModel = selected_tm_list.get(i);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", teamModel.getTm_id());
                members.put(jsonObject);
            }

            for (int i = 0; i < advocates_list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                AdvocateModel advocateModel = advocates_list.get(i);
                jsonObject.put("name", advocateModel.getAdvocate_name());
                jsonObject.put("email", advocateModel.getEmail());
                jsonObject.put("phone", advocateModel.getNumber());
                opponent_advocates.put(jsonObject);
            }
            JSONObject tagsObject = new JSONObject();

            for (int i = 0; i < tag_list.size(); i++) {
                try {
                    tagsObject.put(String.valueOf(i), tag_list.get(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            postdata.put("title", matter_title);
            postdata.put("affidavit_filing_date", "");
            postdata.put("affidavit_isfiled", "");
            postdata.put("description", description);
            postdata.put("priority", case_priority);
            postdata.put("status", case_status);
            postdata.put("clients", clients);
            postdata.put("corporate", corp_client_id);
            postdata.put("documents", documents);
            postdata.put("group_acls", group_acls);
            postdata.put("members", members);
            postdata.put("opponent_advocates", opponent_advocates);
            postdata.put("tags", tagsObject);
            if (Objects.equals(Constants.MATTER_TYPE, "Legal")) {
                postdata.put("judges", judge);
                postdata.put("date_of_filling", dof);
                postdata.put("court_name", court);
                postdata.put("case_number", case_number);
                postdata.put("case_type", case_type);
                Matter_type = "legal";
            } else {
                postdata.put("startdate", start_date);
                postdata.put("closedate", end_date);
                postdata.put("matter_number", case_number);
                postdata.put("matter_type", case_type);
                Matter_type = "general";
            }
            Log.d("Matter _Creation", "" + postdata);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "matter/" + Matter_type + "/create", "Create Matter", postdata.toString());
//            }
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    private void addDocsToMatter(String docid) {
        try {
            DocumentsModel documentsModel = new DocumentsModel();
//            JSONObject jsonObject = new JSONObject();
            documentsModel.setDocid(docid);
            documentsModel.setName(uploaded_document_name);
            documentsModel.setUser_id(Constants.USER_ID);
            documentsModel.setDoctype("general");
            documentsModel.setDescription(upload_description);
            documentsModel.setExpiration_date(upload_exp_date);
            selected_documents_list.add(documentsModel);
            tempSelectedDocuments.add(documentsModel);
            isDocUploaded = true;
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void display_doc(JSONArray existing_documents) {
        try {
            new_selected_doc.clear();
            selected_documents_list.clear();   // ← ADD THIS
            tempSelectedDocuments.clear();
            for (int d = 0; d < existing_documents.length(); d++) {
                DocumentsModel documentsModel = new DocumentsModel();
                JSONObject jsonObject = existing_documents.getJSONObject(d);
                documentsModel.setDocid(jsonObject.optString("docid"));
                documentsModel.setName(jsonObject.optString("name"));
                documentsModel.setUser_id(jsonObject.optString("user_id"));
                documentsModel.setDoctype(jsonObject.optString("doctype"));
                documentsModel.setTags_list(jsonObject.optJSONObject("tags"));
                documentsModel.setContentType(jsonObject.optString("contentType"));
                documentsModel.setViewUrl(jsonObject.optString("viewUrl"));
                documentsModel.setIs_encrypted(jsonObject.optBoolean("is_encrypted"));
                documentsModel.setIs_password(jsonObject.optBoolean("is_password"));
                documentsModel.setAdded_encryption(jsonObject.optBoolean("added_encryption"));
                selected_documents_list.add(documentsModel);
                tempSelectedDocuments.add(documentsModel);
                new_selected_doc.add(documentsModel);
            }
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

//    private void loadDocumentsData(JSONArray data) {
//        documentsList.clear();
//        try {
//            for (int i = 0; i < data.length(); i++) {
//                JSONObject jsonObject = data.getJSONObject(i);
//                DocumentsModel documentsModel = new DocumentsModel();
//                documentsModel.setDocid(jsonObject.optString("docid"));
//                documentsModel.setName(jsonObject.optString("name"));
//                documentsModel.setUser_id(jsonObject.optString("user_id"));
//                documentsModel.setDoctype(jsonObject.optString("doctype"));
//                documentsModel.setTags_list(jsonObject.optJSONObject("tags"));
//                documentsModel.setContentType(jsonObject.optString("contentType"));
//                documentsModel.setViewUrl(jsonObject.optString("viewUrl"));
//                documentsModel.setIs_encrypted(jsonObject.optBoolean("is_encrypted"));
//                documentsModel.setIs_password(jsonObject.optBoolean("is_password"));
//                documentsModel.setAdded_encryption(jsonObject.optBoolean("added_encryption"));
//                documentsList.add(documentsModel);
//            }
////            if (!documentsList.isEmpty()) {
//            if (!Constants.create_matter) {
//                documentsList.addAll(new_selected_doc);
//            }
//            if (!documentsList.isEmpty())
//                DocumentsPopUp();
//            else rv_display_upload_doc.setVisibility(View.GONE);

    /// /                else rv_display_upload_doc.setVisibility(View.GONE);
    /// /            }
//        } catch (JSONException e) {
//            e.fillInStackTrace();
//        }
//    }
    private void loadDocumentsData(JSONArray data) {
        documentsList.clear();
        try {
            // Keep track of docids to prevent duplicates
            HashSet<String> existingDocIds = new HashSet<>();

            // Load documents from API
            for (int i = 0; i < data.length(); i++) {
                JSONObject jsonObject = data.getJSONObject(i);
                DocumentsModel documentsModel = new DocumentsModel();
                documentsModel.setDocid(jsonObject.optString("docid"));
                documentsModel.setName(jsonObject.optString("name"));
                documentsModel.setUser_id(jsonObject.optString("user_id"));
                documentsModel.setDoctype(jsonObject.optString("doctype"));
                documentsModel.setTags_list(jsonObject.optJSONObject("tags"));
                documentsModel.setContentType(jsonObject.optString("contentType"));
                documentsModel.setViewUrl(jsonObject.optString("viewUrl"));
                documentsModel.setIs_encrypted(jsonObject.optBoolean("is_encrypted"));
                documentsModel.setIs_password(jsonObject.optBoolean("is_password"));
                documentsModel.setAdded_encryption(jsonObject.optBoolean("added_encryption"));

                documentsList.add(documentsModel);
                existingDocIds.add(documentsModel.getDocid());
            }

            // Add any new selections only if not in create_matter mode
            if (!Constants.create_matter && new_selected_doc != null) {
                for (DocumentsModel newDoc : new_selected_doc) {
                    String newDocId = newDoc.getDocid();
                    if (newDocId != null && !existingDocIds.contains(newDocId)) {
                        documentsList.add(newDoc);
                        existingDocIds.add(newDocId); // update set so no duplicates
                    }
                }
            }

            // 🔽 Filter selected_documents_list based on available documentsList
            for (int i = selected_documents_list.size() - 1; i >= 0; i--) {
                DocumentsModel selectedDoc = selected_documents_list.get(i);
                boolean existsInList = false;

                for (DocumentsModel doc : documentsList) {
                    if (selectedDoc.getDocid().equals(doc.getDocid())) {
                        doc.setChecked(true); // Set checked on matched doc
                        existsInList = true;
                        break;
                    }
                }

                if (!existsInList) {
                    selected_documents_list.remove(i);
                    tempSelectedDocuments.remove(i); // Remove invalid doc
                }
            }

            // Now proceed to open the document selection UI
            if (!documentsList.isEmpty()) {
                DocumentsPopUp();
                if (!Constants.create_matter) {
                    rv_display_upload_doc.setVisibility(VISIBLE);
                } else {
                    rv_display_upload_doc.setVisibility(GONE);
                }
            } else {
                at_add_documents.setText("");
                tempSelectedDocuments.clear();
                selected_documents_list.clear();
                rv_display_upload_doc.setVisibility(GONE);
                ll_selected_documents.removeAllViews();
            }

            DocumentsText();
            loadSelectedDocuments(new String[selected_documents_list.size()]);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void DocumentsPopUp() {
        try {
            if (!documentsList.isEmpty()) {
                rv_display_upload_doc.setVisibility(VISIBLE);
            } else {
                rv_display_upload_doc.setVisibility(GONE);
            }
            for (int i = 0; i < documentsList.size(); i++) {
                for (int j = 0; j < selected_documents_list.size(); j++) {
                    if (documentsList.get(i).getDocid().matches(selected_documents_list.get(j).getDocid())) {
                        DocumentsModel documentsModel = documentsList.get(i);
                        documentsModel.setChecked(true);
//                        selected_groups_list.set(j,documentsModel);
                    }
                }
            }

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            rv_display_upload_doc.setLayoutManager(layoutManager);
            //  rv_display_upload_doc.setHasFixedSize(true);
//            ADAPTER_TAG = "Documents";
            documentsAdapter = new DocumentsAdapter(documentsList, this, btn_create);
            rv_display_upload_doc.setAdapter(documentsAdapter);
            Log.d("Documentslist.size", "" + documentsList.size());
            btn_add_documents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selected_documents_list.clear();
                    selected_documents_list.addAll(tempSelectedDocuments);

                    DocumentsText();
                    loadSelectedDocuments(new String[selected_documents_list.size()]);
                    rv_display_upload_doc.setVisibility(GONE);
                    ischecked_doc = true;

                    if (!Constants.create_matter && !isInitialLoad) {
                        isChangesOccured = true;
                    }
                    Add_Documents();
                }
            });
        } catch (Exception e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

//    public void Add_Documents() {
////        for (int i = 0; i < documentsAdapter.getDocumentsList().size(); i++) {
////            DocumentsModel documentsModel = documentsAdapter.getDocumentsList().get(i);
////
////            // Check if the document is checked
////            if (documentsModel.isChecked()) {
////                boolean alreadySelected = false;
////
////                // Compare the docid with the selected_documents_list
////                for (DocumentsModel selectedDocument : selected_documents_list) {
////                    if (selectedDocument.getDocid().equals(documentsModel.getDocid())) {
////                        alreadySelected = true;
////                        break;
////                    }
////                }
////
////                // Add to the list if it's not already selected
////                if (!alreadySelected) {
////                    selected_documents_list.add(documentsModel);
////                }
////            } else {
////                // Remove from the list if it was previously selected but now unchecked
////                selected_documents_list.removeIf(selectedDocument ->
////                        selectedDocument.getDocid().equals(documentsModel.getDocid())
////                );
////            }
////        }
//        AndroidUtils.ToggleButton(selected_documents_list.size(), btn_add_documents);
//        if (!selected_documents_list.isEmpty())
//            DocumentsText();
//        else
//            at_add_documents.setText("");
//    }

//    private void loadUploadedDocuments() {
//        String[] value = new String[upload_documents_list.size()];
//        for (int i = 0; i < upload_documents_list.size(); i++) {
//            value[i] = upload_documents_list.get(i).getName();
//        }
//        String str = String.join(",", value);
////    at_add_documents.setText(str);
//        tv_selected_file.setText(str);
////        selected_tm.setVisibility(View.VISIBLE);
//        ll_uploaded_documents.removeAllViews();
//        ll_selected_documents.removeAllViews();
//        if (!upload_documents_list.isEmpty()) {
//            tv_selected_document.setVisibility(View.VISIBLE);
//            if (!Constants.create_matter) {
//                btn_create.setAlpha(1.0f);
//                btn_create.setEnabled(true);
//            }
//        } else {
//            tv_selected_document.setVisibility(View.GONE);
//        }
//        for (int i = 0; i < upload_documents_list.size(); i++) {
//            View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.document_tag, null);
//            TextView tv_opponent_name = view_opponents.findViewById(R.id.tv_opponent_name);
//            tv_opponent_name.setText(upload_documents_list.get(i).getName());
//            ImageView iv_edit_tag = view_opponents.findViewById(R.id.iv_edit_tag);
//            ImageView iv_edit_document = view_opponents.findViewById(R.id.iv_edit_opponent);
//            ImageView iv_remove_opponent = view_opponents.findViewById(R.id.iv_remove_opponent);
//            iv_remove_opponent.setTag(i);
//            iv_remove_opponent.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    try {
//                        int position = 0;
//                        if (v.getTag() instanceof Integer) {
//                            position = (Integer) v.getTag();
//                            v = ll_uploaded_documents.getChildAt(position);
//                            ll_uploaded_documents.removeView(v);
////                            ll_selected_groups.addView(view_opponents,position);
//                            DocumentsModel documentsModel = upload_documents_list.get(position);
//                            documentsModel.setChecked(false);
//                            upload_documents_list.remove(position);
////                            selected_groups_list.set(position, groupsModel);
//                            String[] value = new String[upload_documents_list.size()];
//                            for (int i = 0; i < upload_documents_list.size(); i++) {
//                                value[i] = upload_documents_list.get(i).getName();
//                            }
//                            String str = String.join(",", value);
////                        at_add_documents.setText(str);
//                            tv_selected_file.setText(str);
//                        }
//                    } catch (Exception e) {
//                        e.fillInStackTrace();
//                        AndroidUtils.showAlert(e.getMessage(), getActivity());
//                    }
//                }
//            });
//            iv_edit_tag.setTag(i);
//            iv_edit_tag.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int position = 0;
//                    if (v.getTag() instanceof Integer) {
//                        position = (Integer) v.getTag();
//                        v = ll_uploaded_documents.getChildAt(position);
//                        DocumentsModel documentsModel = upload_documents_list.get(position);
//                        try {
//                            open_add_tags_popup(documentsModel);
//                        } catch (JSONException e) {
//                            e.fillInStackTrace();
//                        }
//                    }
//                }
//            });
//            iv_edit_document.setTag(i);
//            iv_edit_document.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int position = 0;
//                    if (v.getTag() instanceof Integer) ;
//                    position = (Integer) v.getTag();
//                    v = ll_uploaded_documents.getChildAt(position);
//                    DocumentsModel documentsModel1 = upload_documents_list.get(position);
//                    EditDocuments(documentsModel1.getName(), documentsModel1.getDescription(), documentsModel1.getFile(), position, v);
//                }
//            });

/// /        iv_edit_opponent.setVisibility(View.GONE);
//            ll_uploaded_documents.addView(view_opponents);
//        }
//    }
//    private void loadUploadedDocuments() {
////        SelectedDocView();
//        ll_uploaded_documents.removeAllViews();
//        if (selected_documents_list.isEmpty())
//            ll_selected_documents.removeAllViews();
//        tv_selected_document.setVisibility(View.GONE);
//        if (!upload_documents_list.isEmpty()) {
//            if (!Constants.create_matter) {
//                btn_create.setAlpha(1.0f);
//                btn_create.setEnabled(true);
//            }
//        }
//        UploadedDocView();
//
//        for (int i = 0; i < upload_documents_list.size(); i++) {
//            View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.document_tag, null);
//            if (view_opponents != null) {
//                TextView tv_opponent_name = view_opponents.findViewById(R.id.tv_opponent_name);
//                ImageView iv_remove_opponent = view_opponents.findViewById(R.id.iv_remove_opponent);
//                ImageView iv_edit_opponent = view_opponents.findViewById(R.id.iv_edit_opponent);
//                ImageView enable_download_icon = view_opponents.findViewById(R.id.enable_download_icon);
//                ImageView disable_download_icon = view_opponents.findViewById(R.id.disable_download_icon);
//                Button btn_view_tags = view_opponents.findViewById(R.id.btn_view_tags);
//                btn_view_tags.setText(R.string.view_tags);
////            ImageView iv_view_icon = view_opponents.findViewById(R.id.iv_edit_view);
//                ImageView iv_edit_tag = view_opponents.findViewById(R.id.iv_edit_tag);
//
////            iv_view_icon.setVisibility(View.VISIBLE);
//                iv_edit_opponent.setVisibility(View.VISIBLE);
//                iv_edit_tag.setVisibility(View.VISIBLE);
//
////            if (iv_view_icon != null) {
////                iv_view_icon.setTag(i);
////                iv_view_icon.setOnClickListener(v -> {
////                    int position = (int) v.getTag();
////                    sharedDocumentsDo = upload_documents_list.get(position);
////                    View_doc(upload_documents_list.get(position).getDocid());
////                });
////            }
//
//                if (upload_documents_list.get(i).isIsenabled()) {
//                    disable_download_icon.setVisibility(View.GONE);
//                    enable_download_icon.setVisibility(View.VISIBLE);
//                } else {
//                    disable_download_icon.setVisibility(View.VISIBLE);
//                    enable_download_icon.setVisibility(View.GONE);
//                }
//                if (upload_documents_list.get(i).getTags_list().length() > 0) {
//                    btn_view_tags.setVisibility(View.VISIBLE);
//                } else {
//                    btn_view_tags.setVisibility(View.GONE);
//                }
//                if (tv_opponent_name != null && iv_remove_opponent != null) {
//                    tv_opponent_name.setText(upload_documents_list.get(i).getName());
//                    iv_remove_opponent.setTag(i);
//                    iv_remove_opponent.setOnClickListener(v -> {
//                        try {
//                            int position = (int) v.getTag();
//                            ll_uploaded_documents.removeViewAt(position);
//                            DocumentsModel documentsModel = upload_documents_list.remove(position);
//                            documentsModel.setChecked(false);
//
//                            for (int j = 0; j < ll_uploaded_documents.getChildCount(); j++) {
//                                ImageView iv_remove = ll_uploaded_documents.getChildAt(j).findViewById(R.id.iv_remove_opponent);
//                                if (iv_remove != null) {
//                                    iv_remove.setTag(j);
//                                    iv_edit_opponent.setTag(j);
//                                    iv_edit_tag.setTag(j);
//                                    enable_download_icon.setTag(j);
//                                    disable_download_icon.setTag(j);
//                                    btn_view_tags.setTag(j);
//                                }
//                            }
//
////                            StringBuilder stringBuilder = new StringBuilder();
////                            for (DocumentsModel model : upload_documents_list) {
////                                stringBuilder.append(model.getName()).append(",");
////                            }
////                            if (stringBuilder.length() > 0) {
////                                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
////                            }
////
////                            String strn = stringBuilder.toString();
////                            tv_selected_file.setText(strn);
//
////                            SelectedDocView();
//                            UploadedDocView();

    /// /                            if (!selected_documents_list.isEmpty() || !upload_documents_list.isEmpty())
//                            if (!Constants.create_matter) {
//                                btn_create.setEnabled(true);
//                                btn_create.setAlpha(1.0f);
//                            }
//                        } catch (Exception e) {
//                            e.fillInStackTrace();
//                            AndroidUtils.showAlert(e.getMessage(), getActivity());
//                        }
//                    });
//                    iv_remove_opponent.setVisibility(View.VISIBLE);
//                }
//
//                iv_edit_opponent.setTag(i);
//                iv_edit_opponent.setOnClickListener(v -> {
//                    int position = (int) v.getTag();
//                    DocumentsModel documentsModel1 = upload_documents_list.get(position);
//                    EditDocuments(documentsModel1.getName(), documentsModel1.getDescription(), documentsModel1.getFile(), position, v);
//                });
//
//                iv_edit_tag.setTag(i);
//                iv_edit_tag.setOnClickListener(v -> {
//                    int position = (int) v.getTag();
//                    DocumentsModel documentsModel = upload_documents_list.get(position);
//                    try {
//                        open_add_tags_popup(documentsModel, position);
//                    } catch (JSONException e) {
//                        e.fillInStackTrace();
//                    }
//                });
//                enable_download_icon.setTag(i);
//                disable_download_icon.setTag(i);
//                btn_view_tags.setTag(i);
//                enable_download_icon.setOnClickListener(v -> {
//                    int position = (int) v.getTag();
//                    DocumentsModel documentsModel = upload_documents_list.get(position);
//                    documentsModel.setIsenabled(false);
//                    updateSingleDocumentView(position);
//                    checkEnableDownloadStatus();
//                });
//                disable_download_icon.setOnClickListener(v -> {
//                    int position = (int) v.getTag();
//                    DocumentsModel documentsModel = upload_documents_list.get(position);
//                    documentsModel.setIsenabled(true);
//                    updateSingleDocumentView(position);
//                    checkEnableDownloadStatus();
//                });
//                btn_view_tags.setOnClickListener(v -> {
//                    int position = (int) v.getTag();
//                    DocumentsModel documentsModel = upload_documents_list.get(position);
//                    if (documentsModel.getTags_list().length() > 0)
//                        ViewTags(documentsModel, position);
//                });
//
//                ll_uploaded_documents.addView(view_opponents);
//            }
//        }
//    }
    private void loadRecyclerview(String tag, String subtag) {
        ll_uploaded_documents.setLayoutManager(new LinearLayoutManager(getContext()));
        AndroidUtils.ToggleButton(upload_documents_list.size(), chk_select_all);
        AndroidUtils.ToggleButton(upload_documents_list.size(), chk_box_layout);
        Constants.upload_documents_list = upload_documents_list;
        // Initialize the adapter with the documents list
        adapter = new DocumentsListAdapter(upload_documents_list, tag, subtag, this, this);
        ll_uploaded_documents.setAdapter(adapter);
        //ll_uploaded_documents.setHasFixedSize(true);
        AndroidUtils.LoadList(ll_uploaded_documents, getContext(), upload_documents_list.size(), false);
        // Update file count and hint for the selected files
        if (upload_documents_list.isEmpty()) {
            ll_uploaded_documents.setVisibility(GONE);
        } else {
            ll_uploaded_documents.setVisibility(VISIBLE);
        }
        // Check the checkbox whether all list items are selected or not
        chk_select_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chk_select_all.setChecked(isselect_all_checked);
                isselect_all_checked = !isselect_all_checked;
                adapter.selectOrDeselectAll(chk_select_all.isChecked());
            }
        });
    }

    private void loadUploadedDocuments() {
        if (selected_documents_list.isEmpty()) {
            ll_selected_documents.removeAllViews();
        }

        if (!upload_documents_list.isEmpty()) {
            if (!isInitialLoad) {  // ← ADD this check
                isChangesOccured = true;
            }
            ll_uploaded_documents.removeAllViews();
        }

        UploadedDocView();
    }

    public boolean EnableAllorDisableAll(boolean isChecked) {
        for (com.digicoffer.lauditor.Documents.Models.DocumentsModel item : upload_documents_list) {
            item.setIsenabled(isChecked);
        }
        return isChecked;
    }

    private void updateSingleDocumentView(int position) {
        View child = ll_uploaded_documents.getChildAt(position);
        if (child != null) {
            ImageView enableIcon = child.findViewById(R.id.enable_download_icon);
            ImageView disableIcon = child.findViewById(R.id.disable_download_icon);
            Button btn_view_tags = child.findViewById(R.id.btn_view_tags);
            btn_view_tags.setText(R.string.view_tags);
            if (upload_documents_list.get(position).isIsenabled()) {
                enableIcon.setVisibility(VISIBLE);
                disableIcon.setVisibility(GONE);
            } else {
                enableIcon.setVisibility(GONE);
                disableIcon.setVisibility(VISIBLE);
            }
            if (upload_documents_list.get(position).getTags().length() > 0) {
                btn_view_tags.setVisibility(VISIBLE);
            } else {
                btn_view_tags.setVisibility(GONE);
            }
        }
    }


    public void ViewTags(com.digicoffer.lauditor.Documents.Models.DocumentsModel documentsModel, int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view_edit_tags = inflater.inflate(R.layout.edit_existing_tags, null);
        LinearLayout ll_existing_tags = view_edit_tags.findViewById(R.id.ll_view_tags);
        ImageView iv_close_existing_tags = view_edit_tags.findViewById(R.id.close_edit_docs);
        TextView header_name = view_edit_tags.findViewById(R.id.header_name);
        TextView tv_document_name = view_edit_tags.findViewById(R.id.tv_document_name);
        tv_document_name.setTextSize(DynamicUtils.twenty);

// Set document name and header
        tv_document_name.setVisibility(VISIBLE);
        tv_document_name.setText(documentsModel.getName());
        header_name.setText(R.string.view_tags);
        AlertDialog dialog = dialogBuilder.create();
// Iterate over tags and add them to the view
        Iterator<String> iter = documentsModel.getTags().keys();
        while (iter.hasNext()) {
            String key = iter.next();
            String value = documentsModel.getTags().optString(key); // Corrected to optString
            View view_added_tags = inflater.inflate(R.layout.displays_documents_list, null);
//            ImageView download_icon = view_added_tags.findViewById(R.id.en);
//            download_icon.setVisibility(View.GONE);
            TextView tv_tag_name = view_added_tags.findViewById(R.id.tv_document_name);
            ImageView iv_remove_tag = view_added_tags.findViewById(R.id.iv_cancel);

            // Set tag name
            String tag_msg = key + " - " + value;
            tv_tag_name.setText(tag_msg);

            // Set remove tag button action
            iv_remove_tag.setTag(iter);
            iv_remove_tag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ll_existing_tags.removeView(view_added_tags);
                    documentsModel.getTags().remove(key);
                    if (documentsModel.getTags().length() == 0) {
                        dialog.dismiss();
                        updateSingleDocumentView(position);
                    }
                }
            });
            // Add the tag view to the layout
            ll_existing_tags.addView(view_added_tags);
        }

        iv_close_existing_tags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
//        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                cl_document.setAlpha(1.0f);
//            }
//        });
        dialog.setCancelable(false);
        // Prevents the dialog from being dismissed when pressing the back button.
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(view_edit_tags);
        dialog.show();

    }

    //    private void loadrecyclerview() {
//        ll_uploaded_documents.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        // Initialize the adapter with the documents list
//        DocumentsListAdapter adapter = new DocumentsListAdapter(upload_documents_list, "Upload", subtag, (DocumentsListAdapter.EventListener) this, this);
//        ll_uploaded_documents.setAdapter(adapter);
//        ll_uploaded_documents.setHasFixedSize(true);
//    }
    public void View_doc(String doc_id) {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONObject jsonObject = new JSONObject();
        WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/document/" + doc_id + "/view", "View Doc", jsonObject.toString());
    }

    public void Add_Documents() {
        if (!upload_documents_list.isEmpty()) {
            if (!isInitialLoad) {  // ← ADD this check
                isChangesOccured = true;
            }
        }
        DocumentsText();
        try {
            JSONArray documents = getJsonArray();
            matterModel.setDocuments(documents);
            matterArraylist.set(0, matterModel);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    @NonNull
    private JSONArray getJsonArray() throws JSONException {
        JSONArray documents = new JSONArray();
        for (int i = 0; i < selected_documents_list.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            DocumentsModel documentsModel = selected_documents_list.get(i);
            jsonObject.put("docid", documentsModel.getDocid());
            jsonObject.put("doctype", documentsModel.getDoctype());
            jsonObject.put("user_id", documentsModel.getUser_id());
            jsonObject.put("name", documentsModel.getName());
            jsonObject.put("tags", documentsModel.getTags_list());
            jsonObject.put("contentType", documentsModel.getContentType());
            jsonObject.put("viewUrl", documentsModel.getViewUrl());
            jsonObject.put("is_encrypted", documentsModel.isIs_encrypted());
            jsonObject.put("is_password", documentsModel.isIs_password());
            jsonObject.put("added_encryption", documentsModel.isAdded_encryption());
            documents.put(jsonObject);
        }
        return documents;
    }

    public void loadSelectedDocuments(String[] value) {
        SelectedDocView();
        ll_uploaded_documents.removeAllViews();
        ll_selected_documents.removeAllViews();
        for (int i = 0; i < selected_documents_list.size(); i++) {
            View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.edit_opponent_advocate, null);
            if (view_opponents != null) {
                TextView tv_opponent_name = view_opponents.findViewById(R.id.tv_opponent_name);
                ImageView iv_remove_opponent = view_opponents.findViewById(R.id.iv_remove_opponent);
                ImageView iv_edit_opponent = view_opponents.findViewById(R.id.iv_edit_opponent);
                ImageView iv_view_icon = view_opponents.findViewById(R.id.iv_edit_view);
                iv_view_icon.setVisibility(VISIBLE);
                iv_edit_opponent.setVisibility(GONE);
                iv_view_icon.setTag(i);
                iv_view_icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = (int) v.getTag();
                        sharedDocumentsDo = selected_documents_list.get(position);
                        View_doc(selected_documents_list.get(position).getDocid());
                    }
                });
                if (tv_opponent_name != null && iv_remove_opponent != null) {
                    tv_opponent_name.setText(selected_documents_list.get(i).getName());
                    iv_remove_opponent.setTag(i);
                    iv_remove_opponent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                int position = (int) v.getTag();
                                ll_selected_documents.removeViewAt(position);
                                DocumentsModel documentsModel1 = tempSelectedDocuments.remove(position);
                                documentsModel1.setChecked(false);
                                DocumentsModel documentsModel = selected_documents_list.remove(position);
                                documentsModel.setChecked(false);
                                for (int j = 0; j < ll_selected_documents.getChildCount(); j++) {
                                    ImageView iv_remove = ll_selected_documents.getChildAt(j).findViewById(R.id.iv_remove_opponent);
                                    ImageView iv_view = ll_selected_documents.getChildAt(j).findViewById(R.id.iv_edit_view);
                                    if (iv_remove != null) {
                                        iv_remove.setTag(j);
                                        iv_view.setTag(j);
                                    }
                                }
                                String str = String.join(",", value);
                                at_add_documents.setText(str);
                                StringBuilder stringBuilder = new StringBuilder();
                                for (DocumentsModel model : selected_documents_list) {
                                    stringBuilder.append(model.getName()).append(",");
                                }
                                if (stringBuilder.length() > 0) {
                                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                                }
                                String strn = stringBuilder.toString();
                                at_add_documents.setText(strn);
                                if (!Constants.create_matter && !isInitialLoad) { // ← CHANGED
                                    isChangesOccured = true;
                                }
                                try {
                                    JSONArray documents = getJsonArray();
                                    matterModel.setDocuments(documents);
                                    matterArraylist.set(0, matterModel);
                                } catch (Exception e) {
                                    e.fillInStackTrace();
                                }
                                SelectedDocView();
                                AndroidUtils.ToggleButton(selected_documents_list.size(), btn_add_documents);
                            } catch (Exception e) {
                                e.fillInStackTrace();
                                AndroidUtils.showAlert(e.getMessage(), getActivity());
                            }
                        }
                    });
                    iv_remove_opponent.setVisibility(VISIBLE);
                }
                ll_selected_documents.addView(view_opponents);
            }
        }
    }

    private void checkViewType(String url, DocumentsModel sharedDocumentsDo) {
        boolean isImage = File_Content_Type.isImage(sharedDocumentsDo.getContentType());
        boolean isPDF = File_Content_Type.isPDF(sharedDocumentsDo.getContentType());
        boolean isEncrypted = sharedDocumentsDo.isAdded_encryption() || sharedDocumentsDo.isIs_encrypted();

        // 1️⃣ If encrypted, directly call decrypt API and return
        if (isEncrypted) {
            callDecryptApi(sharedDocumentsDo.getDocid());
        } else if (!isPDF && (!isImage)) {
            callOtherDocViewApi(sharedDocumentsDo.getDocid());
        } else {
            display_doc(url, sharedDocumentsDo);
        }
    }

    private void display_doc(String url, DocumentsModel sharedDocumentsDo) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.view_documents, null);
        ProgressBar progressBar = view.findViewById(R.id.progress_pdf);
        ImageView iv_image = view.findViewById(R.id.doc_image);
        PDFView idPDFView = view.findViewById(R.id.idPDFView);
        WebView webView = view.findViewById(R.id.doc_webview);
        TextView header = view.findViewById(R.id.header_name);
        ImageView iv_close_edit_docs = view.findViewById(R.id.close_edit_docs);
        boolean isImage = File_Content_Type.isImage(sharedDocumentsDo.getContentType());
        header.setText(sharedDocumentsDo.getName());
        // 4️⃣ Create dialog
        final AlertDialog dialog = dialogBuilder.create();

        // 5️⃣ Close button
        final RetrievePDFfromUrl[] pdfTask = new RetrievePDFfromUrl[1];

        // Close button safely
        iv_close_edit_docs.setOnClickListener(v -> {
            try {
                if (idPDFView != null) idPDFView.recycle();
                if (pdfTask[0] != null) {
                    pdfTask[0].cancelLoading();
                    pdfTask[0].cancel(true);
                }
            } catch (Exception ignored) {
            }
            dialog.dismiss();
        });
        String lowerUrl = url.toLowerCase();
//        boolean urlIsImage =
//                lowerUrl.contains("image") ||
//                        lowerUrl.contains(".jpg") ||
//                        lowerUrl.contains(".jpeg") ||
//                        lowerUrl.contains(".png") ||
//                        lowerUrl.contains(".webp");
//        if (isImage||urlIsImage) {
//            iv_image.setVisibility(View.VISIBLE);
//            Glide.with(getContext())
//                    .load(url)
//                    .placeholder(R.drawable.progress_animation)
//                    .centerCrop()
//                    .into(iv_image);
//        } else {
//            // Load PDF
//            idPDFView.setVisibility(View.VISIBLE);
//            progressBar.setVisibility(View.VISIBLE);
//
//            pdfTask[0] = new RetrievePDFfromUrl(idPDFView, progressBar);
//            pdfTask[0].execute(url);
//        }
        boolean urlIsPDF =
                lowerUrl.contains("application/pdf") ||
                        lowerUrl.contains(".pdf");
        if (urlIsPDF) {
            idPDFView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);

            pdfTask[0] = new RetrievePDFfromUrl(idPDFView, progressBar);
            pdfTask[0].execute(url);
        } else {
            if (isImage) {
                iv_image.setVisibility(View.VISIBLE);
                Glide.with(getContext())
                        .load(url)
                        .placeholder(R.drawable.progress_animation)
                        .centerCrop()
                        .into(iv_image);
            } else {
                // Load PDF
                idPDFView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                pdfTask[0] = new RetrievePDFfromUrl(idPDFView, progressBar);
                pdfTask[0].execute(url);
            }
        }
//        dialog.setOnDismissListener(d -> cl_document.setAlpha(1.0f));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    private void SelectedDocView() {
        if (selected_documents_list.isEmpty()) {
            ll_selected_documents.removeAllViews();
            tv_selected_document.setVisibility(GONE);
        } else {
            tv_selected_document.setVisibility(VISIBLE);
        }
    }

    private void UploadedDocView() {
        if (upload_documents_list.isEmpty()) {
            ll_uploaded_documents.removeAllViews();
            ll_download.setVisibility(GONE);
            rl_buttons.setVisibility(GONE);
            ll_buttons.setVisibility(GONE);
            ll_uploaded_documents.setVisibility(GONE);
        } else {
            ll_download.setVisibility(VISIBLE);
            ll_buttons.setVisibility(VISIBLE);
            ll_uploaded_documents.setVisibility(VISIBLE);
//            rl_buttons.setVisibility(VISIBLE);
        }
    }

    private void loadMattersList(JSONArray matters) {
        try {
            matterList.clear();
            for (int i = 0; i < matters.length(); i++) {
                JSONObject jsonObject = matters.getJSONObject(i);
                ViewMatterModel viewMatterModel = new ViewMatterModel();
                viewMatterModel.setId(jsonObject.getString("id"));
                if (jsonObject.has("caseNumber")) {
                    viewMatterModel.setCaseNumber(jsonObject.getString("caseNumber"));
                }
                if (jsonObject.has("caseType")) {
                    viewMatterModel.setCasetype(jsonObject.getString("caseType"));
                }
                viewMatterModel.setClients(jsonObject.getJSONArray("clients"));
                if (jsonObject.has("courtName")) {
                    viewMatterModel.setCourtName(jsonObject.getString("courtName"));
                }
                if (jsonObject.has("date_of_filling")) {
                    viewMatterModel.setDate_of_filling(jsonObject.getString("date_of_filling"));
                }
                if (jsonObject.has("closedate")) {
                    viewMatterModel.setClosedate(jsonObject.getString("closedate"));
                }
                if (jsonObject.has("matterNumber")) {
                    viewMatterModel.setMatterNumber(jsonObject.getString("matterNumber"));
                }
                if (jsonObject.has("matterType")) {
                    viewMatterModel.setMatterType(jsonObject.getString("matterType"));
                }
                if (jsonObject.has("startdate")) {
                    viewMatterModel.setStartdate(jsonObject.getString("startdate"));
                }
                if (jsonObject.has("timesheets")) {
                    viewMatterModel.setTimesheets(jsonObject.getJSONArray("timesheets"));
                }
                viewMatterModel.setDescription(jsonObject.getString("description"));
                viewMatterModel.setDocuments(jsonObject.getJSONArray("documents"));

                viewMatterModel.setGroupAcls(jsonObject.getJSONArray("groupAcls"));
                viewMatterModel.setGroups(jsonObject.getJSONArray("groups"));
                if (jsonObject.has("hearingDateDetails")) {
                    viewMatterModel.setHearingDateDetails(jsonObject.getJSONObject("hearingDateDetails"));
                }
                viewMatterModel.setIs_editable(jsonObject.getBoolean("is_editable"));
                if (jsonObject.has("judges")) {
                    viewMatterModel.setJudges(jsonObject.getString("judges"));
                }
                if (jsonObject.has("matterClosedDate")) {
                    viewMatterModel.setMatterClosedDate(jsonObject.getString("matterClosedDate"));
                }
                viewMatterModel.setMembers(jsonObject.getJSONArray("members"));
                if (jsonObject.has("nextHearingDate")) {
                    viewMatterModel.setNextHearingDate(jsonObject.getString("nextHearingDate"));
                }
                if (jsonObject.has("opponentAdvocates")) {
                    viewMatterModel.setOpponentAdvocates(jsonObject.getJSONArray("opponentAdvocates"));
                }
                viewMatterModel.setOwner(jsonObject.getJSONObject("owner"));
                viewMatterModel.setPriority(jsonObject.getString("priority"));
                viewMatterModel.setStatus(jsonObject.getString("status"));
                JSONObject tagsObject = jsonObject.getJSONObject("tags");
                JSONArray tagsArray = new JSONArray();

                Iterator<String> keys = tagsObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    try {
                        // Add each tag value to the array
                        tagsArray.put(tagsObject.getString(key));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

// Now set it in your model
                viewMatterModel.setTags_list(tagsArray);
                if (jsonObject.has("tempClients")) {
                    viewMatterModel.setTempClients(jsonObject.getJSONArray("tempClients"));
                }
                if (jsonObject.has("timesheets")) {
                    viewMatterModel.setTimesheets(jsonObject.getJSONArray("timesheets"));
                }
                viewMatterModel.setTemporaryClients(jsonObject.getJSONArray("temporaryClients"));
                viewMatterModel.setTitle(jsonObject.getString("title"));
                matterList.add(viewMatterModel);
            }
            loadMatterRecyclerview();
        } catch (JSONException e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
            e.fillInStackTrace();
        }
    }

    public void loadMatterRecyclerview() {
        try {
            if (getContext() != null && rv_matter_list != null) {
                rv_matter_list.removeAllViews();
                rv_matter_list.setLayoutManager(new GridLayoutManager(getContext(), 1));
                ViewMatterAdapter viewMatterAdapter = new ViewMatterAdapter(matterList, getContext(), (ViewMatterAdapter.InterfaceListener) MatterDocuments_En.this);
                rv_matter_list.setAdapter(viewMatterAdapter);
                //rv_matter_list.setHasFixedSize(true);
//            viewMatterAdapter.notifyDataSetChanged();
                et_search_matter.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        viewMatterAdapter.getFilter().filter(s);
                    }
                });
            }
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void open_add_tags_popup() {
        if (!selected_upload_documents_list.isEmpty()) {
            tags_list.clear();  // Clear first

            // ✅ Load existing tags from the selected document
            com.digicoffer.lauditor.Documents.Models.DocumentsModel selectedDoc = selected_upload_documents_list.get(0);
            JSONObject existingTags = selectedDoc.getTags();

            Log.d("TagsDebug", "Loading existing tags for document: " + selectedDoc.getName());
            Log.d("TagsDebug", "Existing tags JSON: " + (existingTags != null ? existingTags.toString() : "null"));

            if (existingTags != null && existingTags.length() > 0) {
                // Load existing tags into tags_list
                Iterator<String> keys = existingTags.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    try {
                        com.digicoffer.lauditor.Documents.Models.DocumentsModel tagModel = new com.digicoffer.lauditor.Documents.Models.DocumentsModel();
                        tagModel.setTag_type(key);
                        tagModel.setTag_name(existingTags.getString(key));
                        tags_list.add(tagModel);
                        Log.d("TagsDebug", "Loaded tag: " + key + " = " + existingTags.getString(key));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.d("TagsDebug", "No existing tags found for document");
            }

            Log.d("TagsDebug", "Total tags loaded: " + tags_list.size());
        }

        boolean[] isedit_ref = {isedit};
        int[] edit_position_ref = {edit_position};
        LinearLayout[] ll_added_tags_ref = {ll_added_tags};
        TextInputEditText[] tv_tag_type_ref = {tv_tag_type};
        TextInputEditText[] tv_tag_name_ref = {tv_tag_name};

        // ✅ IMPORTANT: We need to pass isUpdateTag = true when we want to show existing tags
        // But we also need to preserve the actual edit mode functionality
        // So we'll pass isUpdateTag = true to show tags, but handle the logic differently in the callback

        AndroidUtils.openAddTagsPopup(
                getContext(),
                getActivity(),
                requireActivity().getLayoutInflater(),
                null, // no background dim in MatterDocuments
                false, // ✅ Set to true to show existing tags in the popup
                tags_list,
                selected_upload_documents_list,
                ll_added_tags_ref,
                tv_tag_type_ref,
                tv_tag_name_ref,
                isedit_ref,
                edit_position_ref,
                (tagsList, dialog) -> {
                    // When isUpdateTag is true, the popup will show existing tags
                    // But we need to handle both adding new tags and updating existing ones

                    // Get the selected document
                    com.digicoffer.lauditor.Documents.Models.DocumentsModel selectedDoc = selected_upload_documents_list.get(0);

                    // Create a new JSONObject for tags
                    JSONObject combinedTags = new JSONObject();

                    // ✅ Add all tags from the popup (which already includes existing tags)
                    for (com.digicoffer.lauditor.Documents.Models.DocumentsModel tagModel : tagsList) {
                        try {
                            combinedTags.put(tagModel.getTag_type(), tagModel.getTag_name());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    // Update the selected document with combined tags
                    selectedDoc.setTags(combinedTags);

                    // Update the same document in upload_documents_list
                    for (int i = 0; i < upload_documents_list.size(); i++) {
                        if (upload_documents_list.get(i) == selectedDoc) {
                            upload_documents_list.get(i).setTags(combinedTags);
                            upload_documents_list.get(i).setChecked(false);
                            break;
                        }
                    }

                    // ✅ Re-render tags in the edit dialog
                    if (llSelectedTags != null) {
                        // Convert JSONObject to ArrayList<DocumentsModel>
                        ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel> mergedTagList = new ArrayList<>();
                        JSONObject json = selectedDoc.getTags();
                        if (json != null) {
                            Iterator<String> keys = json.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                try {
                                    com.digicoffer.lauditor.Documents.Models.DocumentsModel tag = new com.digicoffer.lauditor.Documents.Models.DocumentsModel();
                                    tag.setTag_type(key);
                                    tag.setTag_name(json.getString(key));
                                    mergedTagList.add(tag);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        renderSelectedTags(getContext(), llSelectedTags, mergedTagList, selectedDoc, null, getActivity());
                    }

                    is_clicked_add = true;
                    Hide_Add_EditMeta();
                    dialog.dismiss();

                    ll_added_tags = ll_added_tags_ref[0];
                    tv_tag_type = tv_tag_type_ref[0];
                    tv_tag_name = tv_tag_name_ref[0];
                    isedit = isedit_ref[0];
                    edit_position = edit_position_ref[0];
                }
        );

        ll_added_tags = ll_added_tags_ref[0];
        tv_tag_type = tv_tag_type_ref[0];
        tv_tag_name = tv_tag_name_ref[0];
    }

    private void add_tags_listing(Button btn_save_tag) {
        ll_added_tags.removeAllViews();
        if (!Objects.requireNonNull(tv_tag_type.getText()).toString().isEmpty() && !Objects.requireNonNull(tv_tag_name.getText()).toString().isEmpty()) {
            com.digicoffer.lauditor.Documents.Models.DocumentsModel documentsModel = new com.digicoffer.lauditor.Documents.Models.DocumentsModel();
            documentsModel.setTag_type(Objects.requireNonNull(tv_tag_type.getText()).toString());
            documentsModel.setTag_name(Objects.requireNonNull(tv_tag_name.getText()).toString());
            tags_list.add(documentsModel);
        }
        for (int i = 0; i < tags_list.size(); i++) {
            View view_added_tags = LayoutInflater.from(getContext()).inflate(R.layout.displays_documents_list, null);
            tv_tag_document_name = view_added_tags.findViewById(R.id.tv_document_name);
            LinearLayout ll_tags = view_added_tags.findViewById(R.id.ll_tags);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 10, 10, 10);
            ll_tags.setLayoutParams(params);
            LinearLayoutCompat chk_box_layout = view_added_tags.findViewById(R.id.chk_box_layout);
            chk_box_layout.setVisibility(GONE);
            CheckBox chk_selected_documents = view_added_tags.findViewById(R.id.chk_selected_documents);
            chk_selected_documents.setVisibility(GONE);
            ImageView iv_edit_tag = view_added_tags.findViewById(R.id.iv_edit_meta);
            ImageView iv_remove_tag = view_added_tags.findViewById(R.id.iv_cancel);
            iv_remove_tag.setTag(i);
            iv_remove_tag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //..

                    int position = (int) view.getTag();
                    // Remove the view at the specified position
                    ll_added_tags.removeViewAt(position);
                    // Remove the corresponding item from the list
                    btn_save_tag.setEnabled(true);
                    btn_save_tag.setAlpha(1.0f);
                    com.digicoffer.lauditor.Documents.Models.DocumentsModel documentsModel1 = tags_list.get(position);
                    documentsModel1.setTag_name("");
                    documentsModel1.setTag_type("");
                    documentsModel1.setChecked(false);
                    tags_list.set(position, documentsModel1);
                    tags_list.remove(position);
                    // Update the tags of the remaining views
                    for (int j = 0; j < ll_added_tags.getChildCount(); j++) {
                        ImageView iv_remove = ll_added_tags.getChildAt(j).findViewById(R.id.iv_cancel);
                        if (iv_remove != null) {
                            iv_remove.setTag(j);
                        }
                    }
                    //..

//
//                    int position = 0;
//                    if (view.getTag() instanceof Integer) {
//                        position = (Integer) view.getTag();
//                        if (position >= 0) {
//                            view = ll_added_tags.getChildAt(position);
//
//                            ll_added_tags.removeView(view);
//                            DocumentsModel documentsModel1 = tags_list.get(position);
//                            documentsModel1.setTag_name("");
//                            documentsModel1.setTag_type("");
//                            tags_list.set(position, documentsModel1);
//                            tags_list.remove(position);
//                        }
//                        add_tags_listing();
//                                    ll_added_tags.removeAllViews();
                    if (ll_added_tags.getChildCount() > 0) {
                        tv_added_tags.setVisibility(VISIBLE);
                    } else {
                        tv_added_tags.setVisibility(GONE);
                    }
                }
            });
            iv_edit_tag.setTag(i);
            iv_edit_tag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = 0;
                    if (view.getTag() instanceof Integer) {
                        position = (Integer) view.getTag();
                        com.digicoffer.lauditor.Documents.Models.DocumentsModel documentsModel1 = tags_list.get(position);
                        //Edit the tags in Add tag page....
                        if (documentsModel1 != null) {
                            edit_position = position;
                            isedit = true;
                            tv_tag_type.setText(documentsModel1.getTag_type());
                            tv_tag_name.setText(documentsModel1.getTag_name());
                        }
                    }
                }
            });
            iv_edit_tag.setVisibility(VISIBLE);
            String tag_name = tags_list.get(i).getTag_type() + " - " + tags_list.get(i).getTag_name();
            tv_tag_document_name.setText(tag_name);
            ll_added_tags.addView(view_added_tags);
            if (ll_added_tags.getChildCount() > 0) {
                tv_added_tags.setVisibility(VISIBLE);
            } else {
                tv_added_tags.setVisibility(GONE);
            }
        }
    }

//    private void edit_tags(String tag_type, String tag_name, int position, View view_tag, TextView tv_tag_document_name) {
//        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
//        LayoutInflater inflater = requireActivity().getLayoutInflater();
//        View view_edit_tags = inflater.inflate(R.layout.edit_tag, null);
//        TextInputEditText tv_edit_tag_type = view_edit_tags.findViewById(R.id.tv_edit_tag_type);
//        TextInputEditText tv_edit_tag_nam = view_edit_tags.findViewById(R.id.tv_edit_tag_name);
//        AppCompatButton btn_cancel = view_edit_tags.findViewById(R.id.btn_edit_cancel_tag);
//        AppCompatButton btn_save_edited_tag = view_edit_tags.findViewById(R.id.btn_edit_save_tag);
//        ImageView iv_close_edit_tags = view_edit_tags.findViewById(R.id.close_edit_docs);
//        tv_edit_tag_type.setText(tag_type);
//        tv_edit_tag_nam.setText(tag_name);
//        final AlertDialog dialog = dialogBuilder.create();
//        btn_cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//            }
//        });
//        btn_save_edited_tag.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                save_edited_tags(Objects.requireNonNull(tv_edit_tag_type.getText()).toString(), Objects.requireNonNull(tv_edit_tag_nam.getText()).toString(), position, view_tag, dialog, tv_tag_document_name);
//            }
//        });
//        iv_close_edit_tags.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//            }
//        });
//        dialog.setCancelable(false);
//        dialog.setView(view_edit_tags);
//        dialog.show();
//    }

    //    private void save_edited_tags(String tag_type, String tag_name, int position, View view, AlertDialog dialog, TextView tv_edit_tag_document_name) {
//        try {
//            DocumentsModel documentsModel = new DocumentsModel();
//            documentsModel.setTag_type(tag_type);
//            documentsModel.setTag_name(tag_name);
//            tags_list.set(position, documentsModel);
//            tv_edit_tag_document_name = view.findViewById(R.id.tv_document_name);
//            tv_edit_tag_document_name.setText(documentsModel.getTag_type() + " - " + documentsModel.getTag_name());
//            dialog.dismiss();
//        } catch (Exception e) {
//            e.fillInStackTrace();
//            AndroidUtils.showAlert(e.getMessage(), getActivity());
//        }
//    }
    private void save_edited_tags(String tag_type, String tag_name) {
        try {
            // Validate edit_position
            if (edit_position < 0 || edit_position >= tags_list.size()) {
                AndroidUtils.showAlert("Invalid position for editing tag.", getActivity());
                return;
            }

            // Update the tag in the list
            com.digicoffer.lauditor.Documents.Models.DocumentsModel documentsModel = tags_list.get(edit_position);
            documentsModel.setTag_type(tag_type);
            documentsModel.setTag_name(tag_name);
            tags_list.set(edit_position, documentsModel);

            // Update the UI for the specific position in ll_added_tags
            View view_to_update = ll_added_tags.getChildAt(edit_position);
            if (view_to_update != null) {
//                ImageView download_icon = view_to_update.findViewById(R.id.download_icon);
//                download_icon.setVisibility(View.GONE);
                TextView tv_edit_tag_document_name = view_to_update.findViewById(R.id.tv_document_name);
                tv_edit_tag_document_name.setText(tag_type + " - " + tag_name);
            }

            // Optionally clear input fields if required
            if (tv_tag_name != null && tv_tag_type != null) {
                tv_tag_name.setText("");
                tv_tag_type.setText("");
            }
        } catch (Exception e) {
            Log.e("save_edited_tags", "Error saving edited tag", e);
            AndroidUtils.showAlert("An error occurred while saving the tag: " + e.getMessage(), getActivity());
        }
    }

    private void EditDocuments(String name, String description, File file, int position, View v) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view_edit_documents = inflater.inflate(R.layout.edit_meta_data, null);
        TextView header_name = view_edit_documents.findViewById(R.id.header_name);
        header_name.setText(R.string.edit_document);
        ImageView iv_cancel_edit_doc = view_edit_documents.findViewById(R.id.close_edit_docs);
        AppCompatButton btn_close_edit_docs = view_edit_documents.findViewById(R.id.btn_cancel_edit_docs);
        TextInputEditText tv_doc_name = view_edit_documents.findViewById(R.id.edit_doc_name);
        TextInputEditText tv_description = view_edit_documents.findViewById(R.id.edit_description);
        TextView tv_document_name = view_edit_documents.findViewById(R.id.tv_document_name);
        tv_document_name.setText(R.string.document_name);
        TextView description1 = view_edit_documents.findViewById(R.id.description);
        description1.setText(R.string.description);
        tv_doc_name.setMaxLines(5);
        filters = new InputFilter[]{
                new InputFilter.LengthFilter(50)
        };
        filters1 = new InputFilter[]{
                new InputFilter.LengthFilter(300)
        };
        tv_doc_name.setFilters(filters);
        tv_description.setMaxLines(10);
        tv_description.setFilters(filters1);

        tv_doc_name.addTextChangedListener(new Validation(tv_doc_name));
        tv_description.addTextChangedListener(new Validation(tv_description));
        AppCompatButton tv_exp_date = view_edit_documents.findViewById(R.id.tv_expiration_date);
        tv_exp_date.setHint(R.string.expiration_date);
        TextView expiration_date_id = view_edit_documents.findViewById(R.id.expiration_date_id);
        expiration_date_id.setText(R.string.expiration_date);
        tv_doc_name.setText(name);
        tv_description.setText(description);

        AppCompatButton btn_save_tag = view_edit_documents.findViewById(R.id.btn_save_tag);
        final AlertDialog dialog = dialogBuilder.create();
        iv_cancel_edit_doc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleEditDocClose(name, description, tv_doc_name, tv_description, dialog);
            }
        });
        btn_close_edit_docs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleEditDocClose(name, description, tv_doc_name, tv_description, dialog);
            }
        });
        tv_exp_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a Calendar instance to get the current date
                AndroidUtils.showDatePicker(tv_exp_date, false, false, true, null);
            }
        });
        btn_save_tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    com.digicoffer.lauditor.Documents.Models.DocumentsModel documentsModel = new com.digicoffer.lauditor.Documents.Models.DocumentsModel();
                    documentsModel.setName(Objects.requireNonNull(tv_doc_name.getText()).toString());
                    documentsModel.setDescription(Objects.requireNonNull(tv_description.getText()).toString());
                    documentsModel.setExpiration_date(tv_exp_date.getText().toString());
                    documentsModel.setFile(file);
                    upload_documents_list.set(position, documentsModel);
                    dialog.dismiss();
                    loadUploadedDocuments();
                } catch (Exception e) {
                    e.fillInStackTrace();
                    AndroidUtils.showAlert(e.getMessage(), getActivity());
                }
            }
        });
        dialog.setCancelable(false);
        dialog.setView(view_edit_documents);
        dialog.show();
    }

    private void handleEditDocClose(String name, String description, TextView tv_doc_name, TextView tv_description, Dialog dialog) {
        String docName = Objects.requireNonNull(tv_doc_name.getText()).toString();
        String desc = Objects.requireNonNull(tv_description.getText()).toString();

        if ((!docName.isEmpty() && (!docName.equals(name))) || (!desc.isEmpty() && (!desc.equals(description)))) {
            AndroidUtils.Delete_Popup(getActivity(), dialog);
        } else {
            dialog.dismiss();
        }
    }

    private void existing_document() {
        isInitialLoad = true;   // ← ADD THIS
        isChangesOccured = false; // ← ADD THIS
        if (Constants.MATTER_TYPE.equals("Legal")) {
            matter_type = "legal";
        } else if (Constants.MATTER_TYPE.equals("General")) {
            matter_type = "general";
        }
        JSONObject postdata = new JSONObject();
        WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "matter/" + matter_type + "/" + Constants.Matter_id + "/documents", "Chosen_Documents", postdata.toString());
    }

    private void document_list() throws JSONException {
//        https://api.staging.digicoffer.com/professional/matter/attachments
        JSONObject postdata = new JSONObject();
        postdata.put("attachment_type", "documents");
//        postdata.put("group_acls", Constants.ex_group_attachment);
//        postdata.put("clients", Constants.ex_client);
        WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "matter/attachments", "Documents_List", postdata.toString());
    }

    public void update_document() throws JSONException {
//        https://api.staging.digicoffer.com/professional/matter/legal/652f70e5fffd8f1bcd11bf36/documents/update
//        https://api.staging.digicoffer.com/professional/matter/legal/652f70e5fffd8f1bcd11bf36/members/update
        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONArray documents = new JSONArray();
        for (int i = 0; i < selected_documents_list.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            DocumentsModel documentsModel = selected_documents_list.get(i);
            jsonObject.put("docid", documentsModel.getDocid());
            jsonObject.put("doctype", documentsModel.getDoctype());
            jsonObject.put("user_id", documentsModel.getUser_id());
            documents.put(jsonObject);
        }
//        for(int i=0;i<upload_documents_list.size();i++)
//        {
//
//        }
        JSONObject postdata = new JSONObject();
        postdata.put("documents", documents);
//   array=["661fa5eafffd8f20732f439d", "65ee94cffffd8f453d0b1c95", "65ee9307fffd8f453d0b1c8f"]
//        https://api.staging.digicoffer.com/professional/matter/legal/65264766fffd8f05faaf6152/members
        WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PATCH, "v2/matter/" + Constants.MATTER_TYPE.toLowerCase(Locale.ROOT) + "/" + Constants.Matter_id, "matter_update", postdata.toString());
    }

    //checking the check box whether all the list items are checked....
    public void check_select_all(boolean check_status) {
        chk_select_all.setChecked(check_status);
    }

    public void isAnyOneSelected(boolean check_status) {
        int chk_item = 0;
        if (check_status) {
            chk_item = 1;
        } else {
            chk_item = 0;
        }
        AndroidUtils.ToggleButton(chk_item, btn_add_tags);
        chk_select_all.setChecked(check_status);
    }

    public void check_encrypted(boolean check_encrypt) {
        if (check_encrypt) {
            EnableEncryptionBackground();
        } else {
            DisableEncryptionBackground();
        }
    }

    public void check_enabled(boolean check_encrypt) {
        if (check_encrypt) {
            EnableDownloadBackground();
        } else {
            DisableDownloadBackground();
        }
    }

    public void check_encrypted_all(boolean check_encrypt) {
        if (check_encrypt) {
            Enable_allEncryptionBackground();
        } else {
            Disable_allEncryptionBackground();
        }
    }

    public void check_enabled_all(boolean check_encrypt) {
        if (check_encrypt) {
            Enable_ALlDownloadBackground();
        } else {
            Disable_AllDownloadBackground();
        }
    }

    @Override
    public void ViewTags(com.digicoffer.lauditor.Documents.Models.DocumentsModel documentsModel, ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel> itemsArrayList) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
//        cl_document.setAlpha(0.5f);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view_edit_tags = inflater.inflate(R.layout.edit_existing_tags, null);
        LinearLayout ll_existing_tags = view_edit_tags.findViewById(R.id.ll_view_tags);
        ImageView iv_close_existing_tags = view_edit_tags.findViewById(R.id.close_edit_docs);
        TextView header_name = view_edit_tags.findViewById(R.id.header_name);
        TextView tv_document_name = view_edit_tags.findViewById(R.id.tv_document_name);
        tv_document_name.setTextSize(DynamicUtils.twenty);

// Set document name and header
        tv_document_name.setVisibility(VISIBLE);
        tv_document_name.setText(documentsModel.getName());
        header_name.setText(R.string.view_tags);

// Iterate over tags and add them to the view
        Iterator<String> iter = documentsModel.getTags().keys();
        while (iter.hasNext()) {
            String key = iter.next();
            String value = documentsModel.getTags().optString(key); // Corrected to optString
            View view_added_tags = inflater.inflate(R.layout.displays_documents_list, null);
//            ImageView download_icon = view_added_tags.findViewById(R.id.en);
//            download_icon.setVisibility(View.GONE);
            TextView tv_tag_name = view_added_tags.findViewById(R.id.tv_document_name);
            ImageView iv_remove_tag = view_added_tags.findViewById(R.id.iv_cancel);

            // Set tag name
            String tag_msg = key + " - " + value;
            tv_tag_name.setText(tag_msg);

            // Set remove tag button action
            iv_remove_tag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Remove the tag from the layout and model
                    ll_existing_tags.removeView(view_added_tags);
                    documentsModel.getTags().remove(key);
                }
            });

            // Add the tag view to the layout
            ll_existing_tags.addView(view_added_tags);

        }

// Create and show the dialog
        AlertDialog dialog = dialogBuilder.create();
        iv_close_existing_tags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
//                cl_document.setAlpha(1.0f);
            }
        });
        dialog.setCancelable(false);
        // Prevents the dialog from being dismissed when pressing the back button.
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(view_edit_tags);
        dialog.show();

    }

    @Override
//    public void EditDocuments(com.digicoffer.lauditor.Documents.models.DocumentsModel documentsModel, ArrayList<com.digicoffer.lauditor.Documents.models.DocumentsModel> itemsArrayList, int position) {
//        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
////        cl_document.setAlpha(0.5f);
//        LayoutInflater inflater = getActivity().getLayoutInflater();
//        View view_edit_documents = inflater.inflate(R.layout.edit_document, null);
//        TextView response_docname, response_docdes;
//        response_docname = view_edit_documents.findViewById(R.id.response_docname);
//        response_docdes = view_edit_documents.findViewById(R.id.response_docdes);
//        ImageView iv_cancel_edit_doc = view_edit_documents.findViewById(R.id.close_edit_docs);
//        AppCompatButton btn_close_edit_docs = view_edit_documents.findViewById(R.id.btn_cancel_edit_docs);
//        TextInputEditText tv_doc_name = view_edit_documents.findViewById(R.id.edit_doc_name);
//
//        TextView tv_document_name = view_edit_documents.findViewById(R.id.tv_document_name);
//        tv_document_name.setText(R.string.document_name);
//
//        TextInputEditText tv_description = view_edit_documents.findViewById(R.id.edit_description);
//        TextView description = view_edit_documents.findViewById(R.id.description);
//        description.setText(R.string.description);
//        filters = new InputFilter[]{
//                new InputFilter.LengthFilter(50)
//        };
//        filters1 = new InputFilter[]{
//                new InputFilter.LengthFilter(300)
//        };
//        tv_doc_name.setFilters(filters);
//        tv_description.setFilters(filters1);
//        tv_description.setMaxLines(10);
//        AppCompatButton tv_exp_date = view_edit_documents.findViewById(R.id.tv_expiration_date);
//        tv_exp_date.setHint(R.string.expiration_date);
//        TextView expiration_date_id = view_edit_documents.findViewById(R.id.expiration_date_id);
//        expiration_date_id.setText(R.string.expiration_date);
//
//        tv_doc_name.setText(documentsModel.getName());
//        tv_description.setText(documentsModel.getDescription());
//        tv_exp_date.setText(documentsModel.getExpiration_date());
//        tv_doc_name.addTextChangedListener(new Validation(tv_doc_name));
//        tv_description.addTextChangedListener(new Validation(tv_description));
//        //Expiration date field.....
//        Calendar myCalendar = Calendar.getInstance();
//        // Assuming tv_exp_date is already defined and initialized
//        tv_exp_date.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Create a Calendar instance to get the current date
//                Calendar myCalendar = Calendar.getInstance();
//
//                // Define the DatePickerDialog's OnDateSetListener
//                final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
//                    @Override
//                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//                        // Update the calendar with the selected date
//                        myCalendar.set(Calendar.YEAR, year);
//                        myCalendar.set(Calendar.MONTH, month);
//                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//
//                        // Update the label with the selected date
//                        updateLabel(myCalendar);
//                    }
//
//                    private void updateLabel(Calendar calendar) {
//                        // Format the date and set it to tv_exp_date
//                        String myFormat = "dd-MM-yyyy";
//                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
//                        tv_exp_date.setText(sdf.format(calendar.getTime()));
//                    }
//                };
//
//                // Create the DatePickerDialog
//                DatePickerDialog datePickerDialog = new DatePickerDialog(
//                        requireActivity(),
//                        date,
//                        myCalendar.get(Calendar.YEAR),
//                        myCalendar.get(Calendar.MONTH),
//                        myCalendar.get(Calendar.DAY_OF_MONTH)
//                );
//
//                // Set minimum selectable date to today
//                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
//
//                // Add OnCancelListener to clear tv_exp_date when dialog is canceled
//                datePickerDialog.setOnCancelListener(dialog -> {
//                    Log.d("DatePickerDialog", "Dialog canceled");
//                    tv_exp_date.setText(""); // Clear the text field when canceled
//                });
//
//                // Show the DatePickerDialog
//                datePickerDialog.show();
//            }
//        });
//
////        Date c = Calendar.getInstance().getTime();
////        System.out.println("Current time => " + c);
////        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
////        String formattedDate = df.format(c);
////        tv_exp_date.setText("");
//
//        AppCompatButton btn_save_tag = view_edit_documents.findViewById(R.id.btn_save_tag);
//        Enable_Button(btn_save_tag, (!Objects.requireNonNull(tv_doc_name.getText()).toString().isEmpty()) && (!Objects.requireNonNull(tv_description.getText()).toString().isEmpty()));
//        final AlertDialog dialog = dialogBuilder.create();
//        iv_cancel_edit_doc.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if ((!Objects.requireNonNull(tv_doc_name.getText()).toString().isEmpty()) || (!Objects.requireNonNull(tv_description.getText()).toString().isEmpty())) {
//                    AndroidUtils.Delete_Popup(getActivity(), dialog);
//                } else dialog.dismiss();
//            }
//        });
//        btn_close_edit_docs.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if ((!Objects.requireNonNull(tv_doc_name.getText()).toString().isEmpty()) || (!Objects.requireNonNull(tv_description.getText()).toString().isEmpty())) {
//                    AndroidUtils.Delete_Popup(getActivity(), dialog);
//                } else dialog.dismiss();
//            }
//        });
//
//        TextWatcher textWatcher = new TextWatcher() {
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Enable_Button(btn_save_tag, (!Objects.requireNonNull(tv_doc_name.getText()).toString().isEmpty()) && (!Objects.requireNonNull(tv_description.getText()).toString().isEmpty()));
//                if (tv_doc_name.getText().toString().isEmpty()) {
//                    response_docname.setVisibility(VISIBLE);
//                    response_docname.setText("Document Name is required");
//                } else {
//                    response_docname.setVisibility(View.GONE);
//                }
//                if (Objects.requireNonNull(tv_description.getText()).toString().isEmpty()) {
//                    response_docdes.setVisibility(VISIBLE);
//                    response_docdes.setText("Description is required");
//                } else {
//                    response_docdes.setVisibility(View.GONE);
//                }
//            }
//        };
//        tv_doc_name.addTextChangedListener(textWatcher);
//        tv_description.addTextChangedListener(textWatcher);
//
//        btn_save_tag.setOnClickListener(view -> {
//            for (int i = 0; i < itemsArrayList.size(); i++) {
//                if (i == position) {
//                    com.digicoffer.lauditor.Documents.models.DocumentsModel documentsModel1 = itemsArrayList.get(i);
//                    documentsModel1.setName(Objects.requireNonNull(tv_doc_name.getText()).toString());
//                    documentsModel1.setDescription(Objects.requireNonNull(tv_description.getText()).toString());
//                    documentsModel1.setExpiration_date(tv_exp_date.getText().toString());
//                    itemsArrayList.set(i, documentsModel1);
//                    dialog.dismiss();
//                    String tag = "edit_meta";
//                    loadRecyclerview(tag, "");
//                }
//            }
//        });
////        dialog.setOnDismissListener(dialog1 -> cl_document.setAlpha(1.0f));
//
//        dialog.setCancelable(false);
//        // Prevents the dialog from being dismissed when pressing the back button.
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.setView(view_edit_documents);
//        dialog.show();
//    }
    public void EditDocuments(
            com.digicoffer.lauditor.Documents.Models.DocumentsModel documentsModel,
            ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel> itemsArrayList,
            int position
    ) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());

        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.edit_document, null);

        // ---------- INPUT FIELDS ----------
        TextInputEditText etDocName = view.findViewById(R.id.edit_doc_name);
        TextInputEditText etDescription = view.findViewById(R.id.edit_description);
        AppCompatButton btnExpDate = view.findViewById(R.id.tv_expiration_date);

//        TextView tvDocNameError = view.findViewById(R.id.response_docname);
//        TextView tvDescError = view.findViewById(R.id.response_docdes);

        //.
        TextView tv_document_name = view.findViewById(R.id.tv_document_name);
        tv_document_name.setText(R.string.document_name);

        TextView description = view.findViewById(R.id.description);
        description.setText(R.string.description);

        btnExpDate.setHint(R.string.expiration_date);
        TextView expiration_date_id = view.findViewById(R.id.expiration_date_id);
        expiration_date_id.setText(R.string.expiration_date);
        InputFilter[] filters, filters1;
        filters = new InputFilter[]{
                new InputFilter.LengthFilter(50)
        };
        filters1 = new InputFilter[]{
                new InputFilter.LengthFilter(300)
        };
        etDescription.setHint(R.string.document_name);
        etDescription.setHint(R.string.description);
        etDocName.setMaxLines(5);
        etDocName.setFilters(filters);
        etDescription.setMaxLines(10);
        etDescription.setFilters(filters1);

        etDocName.addTextChangedListener(new Validation(etDocName));
        etDescription.addTextChangedListener(new DescriptionValidation(etDescription));

        // ---------- SWITCH COMPONENTS ----------
        SwitchMaterial switchDownload =
                view.findViewById(R.id.switch_download)
                        .findViewById(R.id.switch_action);

        SwitchMaterial switchEncryption =
                view.findViewById(R.id.switch_encryption)
                        .findViewById(R.id.switch_action);
        TextView tv_label = view.findViewById(R.id.switch_encryption)
                .findViewById(R.id.tv_label);
        tv_label.setText(R.string.enable_encryption);
        // ---------- TAG COMPONENTS ----------
        AppCompatButton btnAddTag = view.findViewById(R.id.btn_add_tag);
        llSelectedTags = view.findViewById(R.id.ll_selected_tags);

        // ---------- BUTTONS ----------
        AppCompatButton btnSave = view.findViewById(R.id.btn_save_tag);
        AndroidUtils.ToggleButton(1, btnSave);
        AppCompatButton btnCancel = view.findViewById(R.id.btn_cancel_edit_docs);
        ImageView ivClose = view.findViewById(R.id.close_edit_docs);

// ---------- PREFILL DATA ----------
        etDocName.setText(documentsModel.getName());
        etDescription.setText(documentsModel.getDescription());
        btnExpDate.setText(documentsModel.getExpiration_date());
        switchDownload.setChecked(documentsModel.isIsenabled());
        checkSwitchState(switchDownload);
        switchEncryption.setChecked(documentsModel.getIsencrypted());
        checkSwitchState(switchEncryption);
        switchDownload.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean b) {
                documentsModel.setIsenabled(b);
                checkSwitchState(switchDownload);
            }
        });
        switchEncryption.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean b) {
                documentsModel.setIsencrypted(b);
                checkSwitchState(switchEncryption);
            }
        });
// ---------- PREFILL TAGS ----------
        // ---------- PREFILL TAGS ----------
        ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel> tagList = new ArrayList<>();

        JSONObject tags = documentsModel.getTags();
        if (tags != null) {
            Iterator<String> keys = tags.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    com.digicoffer.lauditor.Documents.Models.DocumentsModel tag = new com.digicoffer.lauditor.Documents.Models.DocumentsModel();
                    tag.setTag_type(key);
                    tag.setTag_name(tags.getString(key));
                    tagList.add(tag);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        renderSelectedTags(getContext(), llSelectedTags, tagList, documentsModel, null, getActivity());

        // ---------- VALIDATION ----------
//        TextWatcher watcher = new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                boolean validName = !etDocName.getText().toString().trim().isEmpty();
//                boolean validDesc = !etDescription.getText().toString().trim().isEmpty();
//
//                tvDocNameError.setVisibility(validName ? View.GONE : View.VISIBLE);
//                tvDescError.setVisibility(validDesc ? View.GONE : View.VISIBLE);
//
//                int isValid = 0;
//                if (validName && validDesc) {
//                    isValid = 1;
//                } else {
//                    isValid = 0;
//                }
//                AndroidUtils.ToggleButton(isValid, btnSave);
//            }
//        };
//
//        etDocName.addTextChangedListener(watcher);
//        etDescription.addTextChangedListener(watcher);

        // ---------- DATE PICKER ----------
        btnExpDate.setOnClickListener(v -> {
            AndroidUtils.showDatePicker(btnExpDate, false, false, true, null);
        });

        // ---------- ADD TAG ----------
        btnAddTag.setOnClickListener(v -> {

            com.digicoffer.lauditor.Documents.Models.DocumentsModel model = itemsArrayList.get(position);

            // Save current edit values before opening tag popup
            model.setName(etDocName.getText().toString().trim());
            model.setDescription(etDescription.getText().toString().trim());
            model.setExpiration_date(btnExpDate.getText().toString());
            model.setIsenabled(switchDownload.isChecked());
            model.setIsencrypted(switchEncryption.isChecked());

            // IMPORTANT: select ONLY this document
            selected_upload_documents_list.clear();
            selected_upload_documents_list.add(model);

            // Open existing popup
            open_add_tags_popup();
        });
        AlertDialog dialog = dialogBuilder
                .setView(view)
                .setCancelable(false)
                .create();
        // ---------- SAVE ----------
        btnSave.setOnClickListener(v -> {
            if ((Objects.requireNonNull(etDocName.getText()).toString().isEmpty()) && (Objects.requireNonNull(etDescription.getText()).toString().isEmpty())) {
                AndroidUtils.showAlert("Please enter the Document name, Description", getActivity());
            } else if (Objects.requireNonNull(etDescription.getText()).toString().isEmpty()) {
                AndroidUtils.showAlert("Please enter the Description", getActivity());
            } else if (Objects.requireNonNull(etDocName.getText()).toString().isEmpty()) {
                AndroidUtils.showAlert("Please enter the Document name", getActivity());
            } else {
                com.digicoffer.lauditor.Documents.Models.DocumentsModel model = itemsArrayList.get(position);

                model.setName(etDocName.getText().toString().trim());
                model.setDescription(etDescription.getText().toString().trim());
                model.setExpiration_date(btnExpDate.getText().toString());

                model.setIsenabled(switchDownload.isChecked());
                model.setIsencrypted(switchEncryption.isChecked());

                itemsArrayList.set(position, model);

                dialog.dismiss();
                loadRecyclerview("edit_meta", "");
            }
        });
        // ---------- CLOSE ----------
        View.OnClickListener closeListener = v -> {
            com.digicoffer.lauditor.Documents.Models.DocumentsModel editedModel = new com.digicoffer.lauditor.Documents.Models.DocumentsModel();
            editedModel.setName(etDocName.getText().toString().trim());
            editedModel.setDescription(etDescription.getText().toString().trim());
            editedModel.setExpiration_date(btnExpDate.getText().toString());
            editedModel.setIsenabled(switchDownload.isChecked());
            editedModel.setIsencrypted(switchEncryption.isChecked());
            editedModel.setTags(itemsArrayList.get(position).getTags()); // keep original tags


            boolean changed = AndroidUtils.hasDocumentChanged(
                    documentsModel,     // original
                    editedModel         // current edited values + tags
            );

            if (changed) {
                AndroidUtils.Delete_Popup(requireActivity(), dialog);
            } else {
                dialog.dismiss();
            }
        };

        btnCancel.setOnClickListener(closeListener);
        ivClose.setOnClickListener(closeListener);
//        dialog.setOnDismissListener(d -> cl_document.setAlpha(1f));
        dialog.show();
    }

    private void Enable_Button(Button btn, boolean istrue) {
        if (!istrue) {
            btn.setEnabled(false);
            btn.setAlpha(0.5f);
        } else {
            btn.setEnabled(true);
            btn.setAlpha(1.0f);
        }
    }

    @Override
    public void RemoveDocument(int position, String tag) {
        if (position < 0 || position >= upload_documents_list.size()) return;

        upload_documents_list.remove(position);

        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, adapter.getItemCount());

        Constants.upload_documents_list = upload_documents_list;
        UploadedDocView();
        AndroidUtils.LoadList(ll_uploaded_documents, getContext(), upload_documents_list.size(), false);
    }


}

