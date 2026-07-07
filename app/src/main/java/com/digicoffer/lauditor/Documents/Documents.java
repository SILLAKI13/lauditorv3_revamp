package com.digicoffer.lauditor.Documents;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.digicoffer.lauditor.AuditTrails.Adapters.PaginationHelper;
import com.digicoffer.lauditor.Documents.DocumentsListAdpater.DocumentsListAdapter;
import com.digicoffer.lauditor.Documents.DocumentsListAdpater.GroupsListAdapter;
import com.digicoffer.lauditor.Documents.DocumentsListAdpater.View_documents_adapter;
import com.digicoffer.lauditor.Documents.Models.ClientsModel;
import com.digicoffer.lauditor.Documents.Models.DocumentsModel;
import com.digicoffer.lauditor.Documents.Models.MattersModel;
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel;
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
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.FileDownloader;
import com.digicoffer.lauditor.CommonFiles.PdfUtils.File_Content_Type;
import com.digicoffer.lauditor.CommonFiles.PdfUtils.PDFviewer;
import com.digicoffer.lauditor.CommonFiles.PdfUtils.RetrievePDFfromUrl;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;


public class Documents extends Fragment implements BottomSheetUploadFile.OnPhotoSelectedListner, AsyncTaskCompleteListener, DocumentsListAdapter.EventListener, View_documents_adapter.Eventlistner, GroupsListAdapter.OnCheckedChangeListener {
    Button btn_browse, btn_group_cancel, btn_group_submit, btn_group_view_cancel, btn_group_view_submit;
    //Initialize a file count to Zero
    boolean isListFiltered = false;
    boolean isDownload = false;
    private int totalUploads = 0;
    private int completedUploads = 0;
    private boolean isAlertShown = false;
    int end_temp = 0;
    ArrayList<ViewDocumentsModel> filteredList = new ArrayList<>();
    private List<TextView> pagebuttons = new ArrayList<>();
    boolean is_MergePdfClicked = true;
    byte[] filedata;
    String doc_name;
    int currentPage = 1;
    String ReceivedFileName = "";
    String client_name;
    AlertDialog Dialog;
    boolean isedit = false;
    boolean isUpdateTag = false;
    int edit_position = 0;
    //....
    ViewDocumentsModel viewDocumentsModel_download;
    ViewDocumentsModel viewDocumentsModel;
    ArrayList<ViewDocumentsModel> pageItems = new ArrayList<>();
    boolean isUploadDoc = false;
    TextView custom_spinner, custom_spinner2, custom_spinner3, custom_spinner4, custom_spinner_group, tv_select_groups_view, tv_client_doc, tv_firm_doc;
    CardView cv_view_doc;
    LinearLayout chk_box_layout;
    boolean isselect_all_checked = true;
    JSONArray array_group = new JSONArray();
    boolean is_clicked_add = true;
    boolean ischecked_group = true;
    boolean ischeckedClient_group = true;
    boolean ischecked_group_view = true;
    boolean is_clicked_edit = true;
    boolean ischecked = true;
    ListView list_client, list_matter, list_client_view, list_matter_view, list_group;
    ScrollView list_scroll, list_scroll2, list_scroll3, list_scroll4, list_scroll_group;
    ImageView iv_forward_button, iv_backward_button;
    //...
    int count_file = 0;
    private NewModel mViewModel;
    RelativeLayout spinnerLayout;
    private boolean ismatter_chosen = false;

    BottomSheetUploadFile bottommSheetUploadDocument;
    private Bitmap mSelectedBitmap;
    ConstraintLayout cl_document;
    LinearLayout ll_added_tags, ll_matter, ll_category, ll_groups, ll_client_name, ll_view_docs, ll_upload_docs, upload_group_layout, view_group_layout;
    LinearLayout ll_matter_view, ll_client_name_view, ll_categories_layout, ll_document_type_view, ll_upload_groups, ll_upload_client_group;
    TextInputEditText tv_tag_type, tv_tag_name, tv_search_client_view;
    private ImageView imageView;
    boolean[] selectedLanguage;
    String DOCUMENT_TYPE_TAG = "client";
    String CONTENT_TYPE = "";
    String UPLOAD_TAG = "Client";
    String VIEW_TAG = "Client";
    DocumentsListAdapter adapter;
    View_documents_adapter adapter1;
    CardView cv_view_documents;
    ArrayList<ViewDocumentsModel> view_docs_list = new ArrayList<>();
    ShapeableImageView siv_upload_document, siv_view_document;
    ArrayList<Integer> langList = new ArrayList<>();
    ArrayList<DocumentsModel> groupsList = new ArrayList<>();
    ArrayList<DocumentsModel> tags_list = new ArrayList<>();
    private File mSelectedUri;
    String subtag = "";
    ArrayList<DocumentsModel> selected_documents_list = new ArrayList<>();
    LinearLayout ll_page_navigaiton;
    boolean DOWNLOAD_TAG = false;
    boolean ENCRYPTION_TAG = true;
    boolean DECRYPTION_TAG = true;
    String CATEGORY_TAG = "";
    CheckBox chk_select_all;
    String filename;
    int currentpoistion = 0;
    ArrayList<ClientsModel> clientsList = new ArrayList<>();
    ArrayList<ClientsModel> CorpClientsList = new ArrayList<>();
    ArrayList<MattersModel> matterlist = new ArrayList<>();
    ArrayList<ClientsModel> updatedClients = new ArrayList<>();
    ArrayList<DocumentsModel> selected_groups_list = new ArrayList<>();
    ArrayList<DocumentsModel> client_groups_list = new ArrayList<>();
    ArrayList<DocumentsModel> selected_client_groups_list = new ArrayList<>();
    ArrayList<DocumentsModel> docsList = new ArrayList<>();
    RecyclerView rv_documents, rv_display_view_docs, rv_display_upload_groups_docs, rv_display_view_groups_docs, rv_upload_groups;
    Dialog progress_dialog;

    TextView tv_matter, select_documents, tv_select_group_name, upload_name, view_name, tag_type_name, tag_name, header_name, header_name_group;
    TextView category_name, tv_category, tv_selected_file, tv_select_upload_group_name, tv_select_upload_groups;

    //edit_tag..
    TextView header_name_edit, tag_type_edit, tag_edit;
    TextInputEditText tv_edit_tag_type, tv_edit_tag_name;
    Button btn_upload, btn_add_tags, btn_cancel;
    TextView tv_add_tag, tv_client, tv_firm, tv_enable_download, tv_disable_download, tv_enable_encryption, tv_disable_encryption, tv_edit_meta, tv_name, tv_client_view, tv_firm_view, tv_deleted_view, tv_name_view, matter_name, category_name_id, select_doc_type, tv_document_name, description;
    //    AutoCompleteTextView ;
    File file;
    String value = "";
    String entity_id = "";
    String matter_id = "";
    String client_id = "";
    ImageView img_clear_icon1, img_dropdown_icon1, img_clear_icon2, img_dropdown_icon2, img_clear_icon3, img_dropdown_icon3, img_clear_icon4, img_dropdown_icon4;
    TextView tv_tag_document_name, tv_select_groups, merge_pdf;
    View view1, view2, view3, view4;
    //    TextInputLayout tl_selected_file;
    LinearLayout ll_hide_document_details, pageNumberLayout, ll_merge_pdf;
    Spinner sp_matter, sp_client, tv_search_client, sp_matter_view;
    TextInputEditText tv_search_client_views;
    TextInputLayout tl_selected_file;
    private boolean ischecked_matter = true;
    private boolean ischecked_matter2 = true;
    private boolean ischecked2 = true;

    private static ViewDocumentsModel tempDocModel;
    InputFilter[] filters, filters1;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.upload_document, container, false);
        try {
            mViewModel = new ViewModelProvider(requireActivity()).get(NewModel.class);
//            mViewModel.setData(getContext().optString(R.string.document_upload));
//            pdfView = v.findViewById(R.id.webview);
            filters = new InputFilter[]{
                    new InputFilter.LengthFilter(50)
            };
            filters1 = new InputFilter[]{
                    new InputFilter.LengthFilter(300)
            };
            tv_client_view = v.findViewById(R.id.tv_client_view);
            tv_client_view.setText(R.string.client);
            ll_merge_pdf = v.findViewById(R.id.ll_merge_pdf);
//            tv_client_view.setPadding(20,10,20,10);
            tv_deleted_view = v.findViewById(R.id.tv_deleted_view);
            tv_deleted_view.setText(R.string.deleted);
            tv_firm_view = v.findViewById(R.id.tv_firm_view);
            tv_firm_view.setText(R.string.firm);
            tv_search_client_view = v.findViewById(R.id.tv_search_client_view);
            tv_search_client_view.setHint(R.string.search);
            //......
            ll_page_navigaiton = v.findViewById(R.id.ll_page_navigaiton);
            ll_upload_groups = v.findViewById(R.id.ll_upload_groups);
            pageNumberLayout = ll_page_navigaiton.findViewById(R.id.pageNumberLayout);
            iv_forward_button = ll_page_navigaiton.findViewById(R.id.iv_forward_button);
            iv_backward_button = ll_page_navigaiton.findViewById(R.id.iv_backward_button);
            tv_select_upload_group_name = v.findViewById(R.id.tv_select_upload_group_name);
            tv_select_upload_group_name.setText(R.string.select_groups);
            tv_select_upload_groups = v.findViewById(R.id.tv_select_upload_groups);
            rv_upload_groups = v.findViewById(R.id.rv_upload_groups);
            ll_upload_client_group = v.findViewById(R.id.ll_upload_client_group);
            rv_upload_groups.setBackground(getContext().getDrawable(R.drawable.rectangle_light_grey_bg));

            //Spinner layout1
            view1 = v.findViewById(R.id.customLayout1);
            custom_spinner = view1.findViewById(R.id.tv_spinner_view);
            custom_spinner.setHint(R.string.select_client_name);
            img_clear_icon1 = view1.findViewById(R.id.img_clear_icon);
            img_dropdown_icon1 = view1.findViewById(R.id.img_dropdown_icon);
            //Spinner layout2
            view2 = v.findViewById(R.id.customLayout2);
            custom_spinner2 = view2.findViewById(R.id.tv_spinner_view);
            custom_spinner2.setHint(R.string.select_matters);
            img_clear_icon2 = view2.findViewById(R.id.img_clear_icon);
            img_dropdown_icon2 = view2.findViewById(R.id.img_dropdown_icon);
            //Spinner layout3
            view3 = v.findViewById(R.id.customLayout3);
            custom_spinner3 = view3.findViewById(R.id.tv_spinner_view);
            custom_spinner3.setHint(R.string.select_client_name);
            img_clear_icon3 = view3.findViewById(R.id.img_clear_icon);
            img_dropdown_icon3 = view3.findViewById(R.id.img_dropdown_icon);
            //Spinner layout4
            view4 = v.findViewById(R.id.customLayout4);
            custom_spinner4 = view4.findViewById(R.id.tv_spinner_view);
            custom_spinner4.setHint(R.string.select_matters);
            img_clear_icon4 = view4.findViewById(R.id.img_clear_icon);
            img_dropdown_icon4 = view4.findViewById(R.id.img_dropdown_icon);
            //......
            list_client = v.findViewById(R.id.list_client);
//            list_scroll = v.findViewById(R.id.list_scroll);
//            custom_spinner2 = v.findViewById(R.id.custom_spinner2);
            list_matter = v.findViewById(R.id.list_matter);
//            list_scroll2 = v.findViewById(R.id.list_scroll2);

//            custom_spinner3 = v.findViewById(R.id.custom_spinner3);
//            custom_spinner4 = v.findViewById(R.id.custom_spinner4);
            list_client_view = v.findViewById(R.id.list_client_view);
            list_matter_view = v.findViewById(R.id.list_matter_view);
//            list_scroll3 = v.findViewById(R.id.list_scroll3);
//            list_scroll4 = v.findViewById(R.id.list_scroll4);


            // tv_search_client_view.setHint("Search");
            // tv_search_client_view.setBackground(getContext().getResources().getDrawable(R.drawable.rectangle_light_grey_bg));

//            sp_matter = v.findViewById(R.id.sp_matter);
//            sp_matter_view = v.findViewById(R.id.sp_matter_view);
            tv_add_tag = v.findViewById(R.id.tv_add_tag);
            tv_edit_meta = v.findViewById(R.id.tv_edit_meta);
            btn_upload = v.findViewById(R.id.btn_upload);
            tv_firm = v.findViewById(R.id.tv_firm);
            tv_search_client_views = v.findViewById(R.id.tv_search_client_views);
            tv_search_client_views.setHint(R.string.search);
            tv_search_client_view.addTextChangedListener(new Validation(tv_search_client_view));
            tv_search_client_views.addTextChangedListener(new Validation(tv_search_client_views));
            //   tv_search_client_views.setBackground(getContext().getResources().getDrawable(R.drawable.rectangle_light_grey_bg));

            tv_name = v.findViewById(R.id.tv_name);
            ll_category = v.findViewById(R.id.ll_category);
            tv_name_view = v.findViewById(R.id.tv_name_view);
            tv_name_view.setText(R.string.client_name);
            matter_name = v.findViewById(R.id.matter_name);
            matter_name.setText(R.string.matters);
            merge_pdf = v.findViewById(R.id.merge_pdf);
            merge_pdf.setText(R.string.merge_pdf);
            merge_pdf.setTypeface(Typeface.DEFAULT_BOLD);
            merge_pdf.setBackground(getActivity().getDrawable(R.drawable.rectangle_light_grey_bg));
            rv_display_view_docs = v.findViewById(R.id.rv_display_view_docs);
            select_doc_type = v.findViewById(R.id.select_doc_type);
            select_doc_type.setText(R.string.select_groups);
            category_name_id = v.findViewById(R.id.category_name_id);
            category_name_id.setText(R.string.sub_categories);
            ll_groups = v.findViewById(R.id.ll_groups);


            siv_upload_document = v.findViewById(R.id.upload_icon);
            siv_view_document = v.findViewById(R.id.view_icon);
//            spinnerLayout = v.findViewById(R.id.spinnerLayout);
//            btn_upload = v.findViewById(R.id.btn_upload);
            btn_upload.setText(R.string.upload);
            btn_cancel = v.findViewById(R.id.btn_cancel);
            btn_add_tags = v.findViewById(R.id.btn_add_tag);
            btn_add_tags.setText(R.string.add_tag);
            btn_add_tags.setVisibility(GONE);
            btn_browse = v.findViewById(R.id.btn_browse);
//            btn_browse.setBackground(getActivity().getDrawable(R.drawable.rectangular_complete_blue_background));
            btn_browse.setText(R.string.browse_small);

            upload_group_layout = v.findViewById(R.id.upload_group_layout);
            rv_display_upload_groups_docs = v.findViewById(R.id.rv_display_upload_groups_docs);
            rv_display_upload_groups_docs.setBackground(getContext().getDrawable(R.drawable.rectangle_light_grey_bg));
            btn_group_cancel = v.findViewById(R.id.btn_group_cancel);
            btn_group_cancel.setVisibility(GONE);
            btn_group_submit = v.findViewById(R.id.btn_group_submit);
            btn_group_submit.setVisibility(GONE);

            view_group_layout = v.findViewById(R.id.view_group_layout);
            rv_display_view_groups_docs = v.findViewById(R.id.rv_display_view_groups_docs);
            rv_display_view_groups_docs.setBackground(getContext().getDrawable(R.drawable.rectangle_light_grey_bg));
            btn_group_view_cancel = v.findViewById(R.id.btn_group_view_cancel);
            btn_group_view_submit = v.findViewById(R.id.btn_group_view_submit);

//            tl_selected_file = v.findViewById(R.id.tl_selected_file);

            //modifying the name of add tag and edit meta textview..
            tv_add_tag = v.findViewById(R.id.tv_add_tag);
            tv_add_tag.setText(R.string.add_tag);
            tv_edit_meta = v.findViewById(R.id.tv_edit_meta);
            tv_edit_meta.setText(R.string.edit_meta);
            tv_client = v.findViewById(R.id.tv_client);
            tv_client.setText(R.string.client);
//            tv_client.setPadding(20,10,20,10);
            tv_firm = v.findViewById(R.id.tv_firm);
            tv_firm.setText(R.string.firm);
            tv_name = v.findViewById(R.id.tv_name);
            tv_name.setText(R.string.client_name);
            tv_matter = v.findViewById(R.id.tv_matter);
            tv_matter.setText(R.string.matters);
            tv_category = v.findViewById(R.id.tv_category);
            tv_category.setHint(R.string.sub_categories);
            tv_enable_download = v.findViewById(R.id.tv_enable_download);
            tv_enable_download.setText(R.string.enable_download);
            tv_enable_download.setTextSize(13);
            tv_disable_download = v.findViewById(R.id.tv_disable_download);
            tv_disable_download.setText(R.string.disable_download);
            tv_disable_download.setTextSize(13);
            tv_enable_encryption = v.findViewById(R.id.tv_enable_encryption);
            tv_enable_encryption.setText(R.string.enable_encryption);
            tv_enable_encryption.setTextSize(13);
            tv_disable_encryption = v.findViewById(R.id.tv_disable_encryption);
            tv_disable_encryption.setText(R.string.disable_encryption);
            tv_disable_encryption.setTextSize(13);
//            tv_enable_download.setPadding(20, 15, 10, 15);
//            tv_disable_download.setPadding(20, 15, 10, 15);
//            tv_enable_encryption.setPadding(20, 15, 10, 15);
//            tv_disable_encryption.setPadding(20, 15, 10, 15);
            tv_select_groups = v.findViewById(R.id.tv_select_groups);
            tv_select_group_name = v.findViewById(R.id.tv_select_group_name);
            tv_select_group_name.setText(R.string.select_groups);
            tv_select_groups.setText("");
            tv_select_groups.setHint(R.string.select_groups);
//            tv_select_groups.setHint(R.string.select_groups);

//            tv_select_groups.setText(null);
            tv_client_doc = v.findViewById(R.id.tv_client_doc);
            tv_client_doc.setTextSize(DynamicUtils.twenty);
            tv_client_doc.setText(R.string.list_of_client_documents);

            tv_selected_file = v.findViewById(R.id.tv_selected_file);

            ll_client_name = v.findViewById(R.id.ll_client_name);
            ll_matter = v.findViewById(R.id.ll_matter);
            ll_category = v.findViewById(R.id.ll_category);
            ll_groups = v.findViewById(R.id.ll_groups);
            ll_upload_docs = v.findViewById(R.id.ll_upload_docs);

            ll_view_docs = v.findViewById(R.id.ll_view_docs);
            cl_document = v.findViewById(R.id.cl_document);
            ll_hide_document_details = v.findViewById(R.id.ll_hide_doc_details);
            ll_hide_document_details.setVisibility(GONE);

            category_name = v.findViewById(R.id.category_name);
            category_name.setText(R.string.sub_categories);
            upload_name = v.findViewById(R.id.upload_name);
            upload_name.setTextColor(getContext().getResources().getColor(R.color.white));
            upload_name.setText(R.string.upload);
            view_name = v.findViewById(R.id.view_name);
            view_name.setTextColor(getContext().getResources().getColor(R.color.white));
            view_name.setText(R.string.view);
            select_documents = v.findViewById(R.id.select_documents);
            select_documents.setText(R.string.select_document);

            chk_box_layout = v.findViewById(R.id.chk_box_layout);
            chk_box_layout.setAlpha(0.5F);
            chk_select_all = v.findViewById(R.id.chk_select_all);
            chk_select_all.getBackground().setAlpha(50);
            chk_select_all.setEnabled(false);
            rv_documents = v.findViewById(R.id.rv_documents);

            //Enable the upload documents as the default view...
            siv_upload_document.setBackground(getContext().getResources().getDrawable(R.color.green_count_color));
            siv_upload_document.setImageDrawable(getContext().getResources().getDrawable(R.mipmap.green_background_icon));

            ll_matter_view = v.findViewById(R.id.ll_matter_view);
            ll_document_type_view = v.findViewById(R.id.ll_document_type_view);
            ll_client_name_view = v.findViewById(R.id.ll_client_name_view);
            ll_categories_layout = v.findViewById(R.id.ll_categories_layout);
            ll_client_name = v.findViewById(R.id.ll_client_name);
            category_name = v.findViewById(R.id.tv_category_name);
            category_name.setHint(R.string.sub_categories);

            ll_hide_document_details = v.findViewById(R.id.ll_hide_doc_details);
            ll_hide_document_details.setVisibility(GONE);

            //Make View Documents as Default View.....
            ll_upload_docs.setVisibility(GONE);
            ll_view_docs.setVisibility(VISIBLE);
            tv_client.setPadding(40, 40, 40, 40);
            tv_firm.setPadding(40, 40, 40, 40);
            tv_client_view.setPadding(40, 40, 40, 40);
            tv_firm_view.setPadding(40, 40, 40, 40);
            tv_deleted_view.setPadding(40, 40, 40, 40);
            if (Constants.isCreate)
                upload_documents();
            else
                view_document();
            //..
//            hideUploadDoc();
//            hideViewDoc();
//            EnableUpload();

            view1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clientsList.isEmpty()) {
                        callClientWebservice();
                    }
                    if (ischecked)
                        list_client.setVisibility(VISIBLE);
                    else
                        list_client.setVisibility(GONE);
                    ischecked = !ischecked;
                }
            });
            img_clear_icon1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!custom_spinner.getText().toString().isEmpty()) {
                        client_id = "";
                        matter_id = "";
                        AndroidUtils.DisplaySpinnerView(list_client, custom_spinner, client_name, img_dropdown_icon1, img_clear_icon1, false);
                        AndroidUtils.DisplaySpinnerView(list_matter, custom_spinner2, matter_id, img_dropdown_icon2, img_clear_icon2, false);
                        EnableUpload();
                        ischecked = true;
                        ll_upload_groups.setVisibility(GONE);
                        hideUploadDoc();
                    }
                }
            });
            img_clear_icon2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!custom_spinner2.getText().toString().isEmpty()) {
                        matter_id = "";
                        ischecked_matter = true;
//                        matterlist.clear();
                        callClientGroupsWebservice();
                        hideMatterUpload();
                    }
                }
            });
            img_clear_icon3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!custom_spinner3.getText().toString().isEmpty()) {
                        selected_groups_list.clear();
                        client_id = "";
                        CATEGORY_TAG = "client";
                        matterlist.clear();
                        matter_id = "";
                        ismatter_chosen = true;
                        view_docs_list.clear();
                        rv_display_view_docs.removeAllViews();
                        rv_display_view_docs.setVisibility(GONE);
                        ll_page_navigaiton.setVisibility(GONE);
                        ischecked2 = true;
                        hideViewDoc();
                    }
                }
            });
            img_clear_icon4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!custom_spinner3.getText().toString().isEmpty()) {
                        matter_id = "";
                        ischecked_matter2 = true;
                        //The matter list should not call.
                        matterlist.clear();
                        ismatter_chosen = true;
                        view_docs_list.clear();
                        rv_display_view_docs.removeAllViews();
                        rv_display_view_docs.setVisibility(GONE);
                        ll_page_navigaiton.setVisibility(GONE);
                        callfilter_client_webservices();
                        hideMatterView();
                    }
                }
            });
            view2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ischecked_matter)
                        list_matter.setVisibility(VISIBLE);
                    else
                        list_matter.setVisibility(GONE);
                    ischecked_matter = !ischecked_matter;
                }
            });
            view3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clientsList.isEmpty()) {
                        callClientWebservice();
                    }
                    if (ischecked2)
                        list_client_view.setVisibility(VISIBLE);
                    else
                        list_client_view.setVisibility(GONE);
                    ischecked2 = !ischecked2;
                }
            });
            view4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ischecked_matter2)
                        list_matter_view.setVisibility(VISIBLE);
                    else
                        list_matter_view.setVisibility(GONE);
                    ischecked_matter2 = !ischecked_matter2;
                }
            });
            //...

            siv_upload_document.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    upload_documents();
                    client_name = "";
                    client_id = "";
                }
            });

            //view document option selection.....
            siv_view_document.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    view_document();
                }
            });

//            tv_select_groups = v.findViewById(R.id.tv_select_groups);
            tv_select_groups_view = v.findViewById(R.id.tv_select_groups_view);
            tv_select_groups_view.setText("");
            tv_select_groups_view.setHint(R.string.select_groups);

            // .....
            upload_group_layout.setVisibility(GONE);

            view_group_layout.setVisibility(GONE);

            //...upload Documents Group Selection
            tv_select_groups.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ischecked_group) {
                        if (groupsList.isEmpty()) {
                            callGroupsWebservice();
                        } else {
                            GroupsPopup(upload_group_layout, groupsList, selected_groups_list, rv_display_upload_groups_docs, tv_select_groups);
                        }
                    } else {
                        upload_group_layout.setVisibility(GONE);
                    }
                    ischecked_group = !ischecked_group;
                }
            });
//...upload Documents Group Selection
            tv_select_upload_groups.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ischeckedClient_group) {
                        if (client_groups_list.isEmpty()) {
                            callClientGroupsWebservice();
                        } else {
                            GroupsPopup(ll_upload_client_group, client_groups_list, selected_client_groups_list, rv_upload_groups, tv_select_upload_groups);
                        }
                    } else {
                        ll_upload_client_group.setVisibility(GONE);
                    }
                    ischeckedClient_group = !ischeckedClient_group;
                }
            });
            //...View Documents Group Selection
            tv_select_groups_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ischecked_group_view) {
                        if (groupsList.isEmpty()) {
                            callGroupsWebservice();
                        } else {
                            GroupsPopup(view_group_layout, groupsList, selected_groups_list, rv_display_view_groups_docs, tv_select_groups_view);
                        }
                        view_group_layout.setVisibility(VISIBLE);
                    } else {
                        view_group_layout.setVisibility(GONE);
                        if (!selected_groups_list.isEmpty())
                            callfilter_client_webservices();
                    }
                    ischecked_group_view = !ischecked_group_view;
                }
            });

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

            tv_client.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tv_firm.setTextColor(getContext().getResources().getColor(R.color.black));
                    tv_client.setTextColor(getContext().getResources().getColor(R.color.white));
                    rv_documents.removeAllViews();
                    ischecked = true;
                    hideFirmBackground();
                    client_name = "";
                    client_id = "";
                }
            });
            tv_firm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideClientBackground();
                    client_name = "";
                    client_id = "";
//                    rv_display_upload_groups_docs.removeAllViews();
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

            btn_browse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkPermissionREAD_EXTERNAL_STORAGE(getContext());
                }
            });
            btn_add_tags.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selected_documents_list.clear();
                    for (int i = 0; i < adapter.getList_item().size(); i++) {
                        DocumentsModel documentsModel = adapter.getList_item().get(i);
                        if (documentsModel.isChecked()) {
//                            if (documentsModel.getTags() == null) {
                            selected_documents_list.add(documentsModel);
//                            }
                        }
                    }
                    open_add_tags_popup();
                }
            });
            tv_client_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideviewFirmBackground();
                    DOCUMENT_TYPE_TAG = "client";
                    CATEGORY_TAG = "client";
//                    callViewDocumentWebservice();
                }
            });
            tv_firm_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ismatter_chosen = false;
                    hideviewClientBackground();
                    view_docs_list.clear();
                    view_group_layout.setVisibility(GONE);
                    ischecked_group_view = true;
                    ll_matter_view.setVisibility(GONE);
//                    rv_display_view_groups_docs.removeAllViews();
                    DOCUMENT_TYPE_TAG = "firm";

//                    callViewDocumentWebservice();
//                    callclientfirmWebServices();
                }
            });
            tv_deleted_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ismatter_chosen = false;
                    hideviewFirmBackground();
                    tv_client_doc.setText(R.string.list_of_documents_pending_approval);
                    ll_merge_pdf.setVisibility(GONE);
                    ll_client_name_view.setVisibility(GONE);
                    tv_client_view.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
                    tv_client_view.setTextColor(Color.BLACK);
                    if (Constants.ROLE.equals("SU")) {
                        tv_firm_view.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.radiobutton_centre_background));
                    } else {
                        tv_firm_view.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
                    }
                    tv_firm_view.setTextColor(Color.BLACK);
                    tv_deleted_view.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_background));
                    tv_deleted_view.setTextColor(Color.WHITE);
                    DOCUMENT_TYPE_TAG = "Deleted";
                    CATEGORY_TAG = "Deleted";
                    callDeletedDocumentWebservice();
                }
            });
            merge_pdf.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    client_id = "";
                    tv_select_groups_view.setText("");
                    tv_select_groups_view.setHint(R.string.select_groups);
                    tv_search_client_view.setText("");
                    tv_search_client_views.setText("");
                    category_name.setText("");
                    groupsList.clear();
                    selected_groups_list.clear();
                    matterlist.clear();
                    matter_id = "";
                    ismatter_chosen = true;
                    rv_display_view_docs.setVisibility(GONE);
                    ischecked2 = true;
                    hideViewDoc();
                    if (is_MergePdfClicked) {
                        merge_pdf.setTextColor(getContext().getResources().getColor(R.color.white));
                        merge_pdf.setBackground(getContext().getResources().getDrawable(R.drawable.rectangular_button_green_count));
                    } else {
                        merge_pdf.setTextColor(getContext().getResources().getColor(R.color.black));
                        merge_pdf.setBackground(getContext().getResources().getDrawable(R.drawable.rectangular_light_grey_background));
                    }
                    is_MergePdfClicked = !is_MergePdfClicked;
                }
            });

            //Adding cancel functionalities in upload documents...
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    count_file = 0;
                    clear_upload();
//                    client_name.isEmpty();
                    custom_spinner.setText("");
                    ll_matter.setVisibility(GONE);
                    tv_selected_file.setText("");
                    img_clear_icon1.setVisibility(GONE);
                    img_dropdown_icon1.setVisibility(VISIBLE);
                    tv_selected_file.setText("");
                    view_document();
                }
            });
            btn_upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callUploadDocumentWebservice();
                }
            });
            if (Constants.ROLE.equals("SU")) {
                tv_deleted_view.setVisibility(VISIBLE);
            } else {
                tv_deleted_view.setVisibility(GONE);
            }

        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return v;
    }

    private void Searchfilter(TextInputEditText tv_search_client_view) {
        String query = tv_search_client_view.getText().toString().trim().toLowerCase();
        ArrayList<ViewDocumentsModel> filteredList = new ArrayList<>();
        if (!view_docs_list.isEmpty()) {
            for (ViewDocumentsModel item : view_docs_list) {
                if (item.getName().toLowerCase().contains(query)) {
                    filteredList.add(item);
                }
            }
        }

        // Recalculate pagination based on the filtered list
        int startIndex = PaginationHelper.startIndexForCurrentPage(currentPage, 10);
        int endIndex = PaginationHelper.endIndexForCurrentPage(startIndex, filteredList.size(), 10);
        pageItems = new ArrayList<>(filteredList.subList(startIndex, endIndex));
        // Update RecyclerView with filtered data
        adapter1.setData(pageItems);
        currentPage = 1;

        setupPagination(filteredList);

        UpdatePageButton(currentPage);
    }

    private void setupPagination(ArrayList<ViewDocumentsModel> view_docs_list) {
        // Maximum number of page buttons to display
        pageNumberLayout.removeAllViews();
        pagebuttons.clear();

//        int totalPages = (int) Math.ceil((double) view_docs_list.size() / itemsPerPage);
        int totalPages = PaginationHelper.calculateTotalNoOfPages(view_docs_list.size(), 10);
        Log.d("total_pages", "" + totalPages + ".." + view_docs_list.size());
        if (view_docs_list.isEmpty()) {
            ll_page_navigaiton.setVisibility(GONE);
        } else {
            ll_page_navigaiton.setVisibility(VISIBLE);
        }
//        maxPageButtons = totalPages;

//        int screenWidth = getResources().getDisplayMetrics().widthPixels;
//        int buttonWidth = screenWidth / maxPageButtons;
        // Dynamically add page number buttons
        for (int i = 1; i <= totalPages; i++) {
//            if (i >= maxPageButtons) {
//                // Display only maxPageButtons page numbers
//                break;
//            }
            View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.page_number_layout, null);
            Button pageButton = view_opponents.findViewById(R.id.page_number_button);
            pageButton.setText(String.valueOf(i));
            final int pageNumber = i;
//            if (defaultButtonTint == null) {
//                defaultButtonTint = pageButton.getBackgroundTintList();
//            }
            pageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    pageButton.setBackgroundColor(R.color.green_count_color);
//                    currentPage = pageNumber;
//                    loadPage(currentPage);
//                    if (previousPageButton != null) {
//                        previousPageButton.setBackgroundTintList(whiteButtonTint);
//                    }
//
//                    // Set the background tint color of the clicked button to green
//                    pageButton.setBackgroundTintList(greenButtonTint);

                    currentPage = pageNumber;
                    loadViewDocumentsRecyclerview(view_docs_list);
                    UpdatePageButton(currentPage);
                    // Update the previousPageButton reference
                }
            });
            // Set the width of the page number button
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                    buttonWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
//            pageButton.setLayoutParams(params);

            pageNumberLayout.addView(view_opponents);
            pagebuttons.add(pageButton);
//            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
        iv_forward_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPage = currentPage + 1;
                int startIndex = PaginationHelper.startIndexForCurrentPage(currentPage, 10);
                int endIndex;
                endIndex = PaginationHelper.endIndexForCurrentPage(startIndex, view_docs_list.size(), 10);
                if (endIndex > end_temp) {
//                    isAdvancedSearchEnabled = false;
                    loadViewDocumentsRecyclerview(view_docs_list);
                    UpdatePageButton(currentPage);
                } else {
                    currentPage = currentPage - 1;
                }
            }
        });
        iv_backward_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPage = currentPage - 1;
                if (currentPage > 0) {
//                        isAdvancedSearchEnabled = false;
//                        setupPagination(auditsList);
                    loadViewDocumentsRecyclerview(view_docs_list);
                    UpdatePageButton(currentPage);
//                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                } else {
                    currentPage = 1;
                }
            }
        });
//        loadPage(currentPage);
    }

    private void UpdatePageButton(int currentPage) {
        for (int i = 0; i < pagebuttons.size(); i++) {
            TextView pageButton = pagebuttons.get(i);
            if (currentPage >= 0) {
                if (i + 1 == currentPage) {
                    pageButton.setTextColor(getActivity().getColor(R.color.white));
                    pageButton.setBackgroundColor(requireActivity().getColor(R.color.green_count_color));
                } else {
                    pageButton.setTextColor(getActivity().getColor(R.color.white));
                    pageButton.setBackgroundColor(requireActivity().getColor(R.color.Blue_text_color));
                }
            }
        }
    }

    private void view_document() {
        siv_view_document.setImageDrawable(requireContext().getResources().getDrawable(R.mipmap.green_count_backgroung_icon));
        siv_view_document.setBackground(getContext().getResources().getDrawable(R.color.green_count_color));
        siv_upload_document.setImageDrawable(getContext().getResources().getDrawable(R.mipmap.uploadwhiteupload1));
        siv_upload_document.setBackground(getContext().getResources().getDrawable(R.color.white));
        ll_client_name_view.setVisibility(VISIBLE);
        DOCUMENT_TYPE_TAG = "client";
        CATEGORY_TAG = "client";
        ll_view_docs.setVisibility(VISIBLE);
        rv_display_view_docs.setVisibility(GONE);
        rv_display_view_docs.removeAllViews();
        view_group_layout.setVisibility(GONE);
        ll_upload_docs.setVisibility(GONE);
        ll_matter_view.setVisibility(GONE);
        rv_display_upload_groups_docs.removeAllViews();
        clearClients();
        ismatter_chosen = false;
        isUploadDoc = false;
        if (UPLOAD_TAG.equals("Firm")) {
            hideviewClientBackground();
        } else {
            hideviewFirmBackground();
        }
        mViewModel.setData(getContext().getString(R.string.document_view));
    }

    private void callViewDocumentWebservice() {
        try {
//            https://api.staging.digicoffer.com/professional/docs/deleted/list
            JSONObject jsonObject = new JSONObject();
            progress_dialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/documents/" + DOCUMENT_TYPE_TAG, "VIEW_DOCUMENT", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    private void callDeletedDocumentWebservice() {
        try {
//            https://api.staging.digicoffer.com/professional/docs/deleted/list
            JSONObject jsonObject = new JSONObject();
            progress_dialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "docs/deleted/list", "DELETED_DOCUMENT", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    private void hideviewFirmBackground() {
        tv_client_doc.setText(R.string.list_of_client_documents);
        VIEW_TAG = "Client";
        ischecked2 = true;
        tv_search_client_view.getText().clear();
        ll_matter_view.setVisibility(GONE);
        ll_client_name_view.setVisibility(VISIBLE);
        ll_categories_layout.setVisibility(GONE);
        ll_document_type_view.setVisibility(GONE);
        tv_search_client_view.setVisibility(VISIBLE);
        tv_search_client_views.setVisibility(GONE);
        ll_merge_pdf.setVisibility(VISIBLE);
//        sp_documnet_type_view.setText("Select Groups");
        view_docs_list.clear();
        rv_display_view_docs.removeAllViews();
        rv_display_view_docs.setVisibility(GONE);
        hideViewDoc();

        tv_deleted_view.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
        tv_deleted_view.setTextColor(Color.BLACK);
        if (Constants.ROLE.equals("SU")) {
            tv_firm_view.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.radiobutton_centre_background));
        } else {
            tv_firm_view.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
        }
        tv_firm_view.setTextColor(Color.BLACK);
        tv_client_view.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        tv_client_view.setTextColor(Color.WHITE);
    }

    private void hideViewDoc() {
        hideClientView();
        hideMatterView();
        ll_page_navigaiton.setVisibility(GONE);
    }

    private void hideClientView() {
        custom_spinner3.setText("");
        img_dropdown_icon3.setVisibility(VISIBLE);
        img_clear_icon3.setVisibility(GONE);
        list_client_view.setVisibility(GONE);
        ll_matter_view.setVisibility(GONE);
    }

    private void hideMatterView() {
        custom_spinner4.setText("");
        img_dropdown_icon4.setVisibility(VISIBLE);
        img_clear_icon4.setVisibility(GONE);
        list_matter_view.setVisibility(GONE);
    }

    private void hideUploadDoc() {
        hideClientUpload();
        hideMatterUpload();
    }

    private void hideClientUpload() {
        custom_spinner.setText("");
        img_dropdown_icon1.setVisibility(VISIBLE);
        img_clear_icon1.setVisibility(GONE);
        list_client.setVisibility(GONE);
        ll_matter.setVisibility(GONE);
    }

    private void hideMatterUpload() {
        custom_spinner2.setText("");
        img_dropdown_icon2.setVisibility(VISIBLE);
        img_clear_icon2.setVisibility(GONE);
        list_matter.setVisibility(GONE);
    }

    private void hideviewClientBackground() {
        tv_client_doc.setText(R.string.list_of_firm_documents);
        VIEW_TAG = "Firm";
        CATEGORY_TAG = "firm";
        view_docs_list.clear();
        rv_display_view_docs.removeAllViews();
        rv_display_view_docs.setVisibility(GONE);
        tv_search_client_views.getText().clear();
        ll_merge_pdf.setVisibility(VISIBLE);
        ll_client_name_view.setVisibility(GONE);
        ll_document_type_view.setVisibility(VISIBLE);
        tv_search_client_view.setVisibility(GONE);
        tv_search_client_views.setVisibility(VISIBLE);
        ll_categories_layout.setVisibility(VISIBLE);
        tv_select_groups_view.setText("");
        tv_select_groups_view.setHint(R.string.select_groups);
//        tv_select_groups_view.setHint(R.string.select_groups);
        groupsList.clear();
        clearListData();
        hideViewDoc();
        if (Constants.ROLE.equals("SU")) {
            tv_firm_view.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.radiobutton_centre_green_background));
        } else {
            tv_firm_view.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_background));
        }
        tv_firm_view.setTextColor(Color.WHITE);
        tv_client_view.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_client_view.setTextColor(Color.BLACK);
        tv_deleted_view.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
        tv_deleted_view.setTextColor(Color.BLACK);
    }

    private void upload_documents() {
        isUploadDoc = true;
        isUpdateTag = false;
        clearClients();
        tv_client.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        siv_view_document.setImageDrawable(getContext().getResources().getDrawable(R.mipmap.eye_whitebackground_icon));
        siv_view_document.setBackground(getContext().getResources().getDrawable(R.color.white));
        siv_upload_document.setBackground(getContext().getResources().getDrawable(R.color.green_count_color));
        siv_upload_document.setImageDrawable(getContext().getResources().getDrawable(R.mipmap.green_background_icon));
        ll_upload_docs.setVisibility(VISIBLE);
        ll_view_docs.setVisibility(GONE);
        rv_documents.removeAllViews();
        rv_display_upload_groups_docs.removeAllViews();
        hideFirmBackground();
        //..
        hideUploadDoc();
        //...
        mViewModel.setData(getContext().getString(R.string.document_upload));
        count_file = 0;
        tv_select_groups.setText("");
        tv_select_groups.setHint(R.string.select_groups);
        tv_selected_file.setText("");
        tv_selected_file.setHint(R.string.select_documents);
        UPLOAD_TAG = "Client";
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

    private void open_add_tags_popup() {
//        tags_list.clear();
        if (!selected_documents_list.isEmpty()) {
            tags_list.clear();
        }
        if (isUpdateTag || !selected_documents_list.isEmpty()) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
            cl_document.setAlpha(0.5f);
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.add_tag, null);
            tv_tag_type = (TextInputEditText) view.findViewById(R.id.tv_tag_type);
            tv_tag_name = view.findViewById(R.id.tv_tag_name);
            tag_type_name = view.findViewById(R.id.tag_type_name);
            tag_type_name.setText(R.string.tag_type);
            tag_name = view.findViewById(R.id.tag_name);
            tag_name.setText(R.string.tag);
            tv_tag_type.setHint(R.string.tag_type);
            tv_tag_name.setHint(R.string.tag);
            tv_tag_name.addTextChangedListener(new Validation(tv_tag_name));
            tv_tag_type.addTextChangedListener(new Validation(tv_tag_type));
            header_name = view.findViewById(R.id.header_name);
            if (isUpdateTag) {
                header_name.setText(R.string.update_tag);
            } else {
                header_name.setText(R.string.add_tag);
            }
            final Button btn_add = view.findViewById(R.id.btn_add_tags);
            btn_add.setText(R.string.add);
            filters = new InputFilter[]{
                    new InputFilter.LengthFilter(30)
            };
            filters1 = new InputFilter[]{
                    new InputFilter.LengthFilter(100)
            };

            tv_tag_type.setFilters(filters);
            tv_tag_name.setFilters(filters1);
            final AppCompatButton btn_cancel = view.findViewById(R.id.btn_cancel_tag);
            final AppCompatButton btn_save_tag = view.findViewById(R.id.btn_save_tag);
            final ImageView iv_cancel = view.findViewById(R.id.close_edit_docs);
            ll_added_tags = view.findViewById(R.id.ll_added_tags);
            if (isUpdateTag && !tags_list.isEmpty()) {
                add_tags_listing(btn_save_tag);
            }
            final AlertDialog dialog = dialogBuilder.create();
            iv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!tags_list.isEmpty())
                        AndroidUtils.Delete_Popup(getActivity(), dialog);
                    else dialog.dismiss();
                }
            });
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!tags_list.isEmpty())
                        AndroidUtils.Delete_Popup(getActivity(), dialog);
                    else dialog.dismiss();
                }
            });
            btn_save_tag.setEnabled(false);
            btn_save_tag.setAlpha(0.5f);
//            DocumentsModel documentsModel1 = tags_list.get(position);
//            tags_list.clear();
            btn_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view1) {
                    if ((Objects.requireNonNull(tv_tag_type.getText())).toString().isEmpty() && (Objects.requireNonNull(tv_tag_name.getText()).toString().isEmpty())) {
                        AndroidUtils.showAlert("Please check the Tag Type, Tag", getActivity());
                    } else if (tv_tag_type.getText().toString().isEmpty()) {
                        AndroidUtils.showAlert("Please check the Tag Type", getActivity());
                    } else if (Objects.requireNonNull(tv_tag_name.getText()).toString().isEmpty()) {
                        AndroidUtils.showAlert("Please check the Tag", getActivity());
                    } else {
                        if (isedit) {
                            save_edited_tags(tv_tag_type.getText().toString(), tv_tag_name.getText().toString());
                        } else {
                            add_tags_listing(btn_save_tag);
                        }
                        isedit = false;
                        tv_tag_name.setText("");
                        tv_tag_type.setText("");
                        btn_save_tag.setEnabled(true);
                        btn_save_tag.setAlpha(1.0f);
                    }
                }
            });
            btn_save_tag.setOnClickListener(view1 -> {
                if (!tags_list.isEmpty()) {
                    // Iterate over each selected document
                    if (!isUpdateTag) {
                        for (DocumentsModel documentModel : selected_documents_list) {
                            JSONObject combinedTags = new JSONObject(); // Create a new JSONObject for each document

                            JSONObject existingTags = documentModel.getTags(); // Get existing tags

                            // Check if existingTags is not null and add them to combinedTags
                            if (existingTags != null) {
                                Iterator<String> keys = existingTags.keys();
                                while (keys.hasNext()) {
                                    String key = keys.next();
                                    try {
                                        combinedTags.put(key, existingTags.get(key)); // Add existing tags
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            // Iterate over new tags and add them to combinedTags
                            for (DocumentsModel tagModel : tags_list) {
                                try {
                                    combinedTags.put(tagModel.getTag_type(), tagModel.getTag_name()); // Add new tags
                                } catch (JSONException e) {
                                    e.printStackTrace(); // Handle JSON exception
                                }
                            }

                            // Update the document's tags with the new combinedTags for this specific document
                            documentModel.setTags(combinedTags);
                        }

                        // Update documents list if required
                        for (int i = 0; i < docsList.size(); i++) {
                            for (DocumentsModel selectedDocument : selected_documents_list) {
                                if (docsList.get(i).getName().equals(selectedDocument.getName())) {
                                    docsList.get(i).setTags(selectedDocument.getTags()); // Update tags for this specific document
                                    docsList.get(i).setChecked(false);
                                }
                            }
                        }

                        is_clicked_add = true;
                        Hide_Add_EditMeta(); // Custom method to handle UI state
                        dialog.dismiss();
                    } else {
                        callUpdateTag();
                        is_clicked_add = true;
                        dialog.dismiss();
                    }
                }
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    cl_document.setAlpha(1.0f);
                }
            });
            dialog.setCancelable(false);
            // Prevents the dialog from being dismissed when pressing the back button.
            dialog.setCanceledOnTouchOutside(false);
            dialog.setView(view);
            dialog.show();
        } else {
            AndroidUtils.showAlert("Please select atleast one document to add tags", getActivity());
        }
    }

    //    private void callUpdateTag() {
//        try {
//            progress_dialog = AndroidUtils.get_progress(getActivity());
//            JSONObject jsonObject = new JSONObject();
//            JSONObject combinedTags = new JSONObject(); // Create a new JSONObject for each document
//
//            JSONObject existingTags = viewDocumentsModel.getTag(); // Get existing tags
//
//            // Check if existingTags is not null and add them to combinedTags
//            if (existingTags != null) {
//                Iterator<String> keys = existingTags.keys();
//                while (keys.hasNext()) {
//                    String key = keys.next();
//                    try {
//                        combinedTags.put(key, existingTags.get(key)); // Add existing tags
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            // Iterate over new tags and add them to combinedTags
//            for (DocumentsModel tagModel : tags_list) {
//                try {
//                    combinedTags.put(tagModel.getTag_type(), tagModel.getTag_name()); // Add new tags
//                } catch (JSONException e) {
//                    e.printStackTrace(); // Handle JSON exception
//                }
//            }
//
//            // Update the document's tags with the new combinedTags for this specific document
//            viewDocumentsModel.setTag(combinedTags);
//            jsonObject.put("name", viewDocumentsModel.getName());
//            jsonObject.put("tags", combinedTags);
//            if (!is_MergePdfClicked) {
//                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "v3/mergepdf/" + viewDocumentsModel.getId() + "/tags", "Update Tags", jsonObject.toString());
//            } else {
//                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "v3/document/tags/" + viewDocumentsModel.getId(), "Update Tags", jsonObject.toString());
//            }
//        } catch (Exception e) {
//            if (progress_dialog != null && progress_dialog.isShowing()) {
//                AndroidUtils.dismiss_dialog(progress_dialog);
//            }
//        }
//    }
    private void callUpdateTag() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            JSONObject combinedTags = new JSONObject(); // New object built only from current tags_list

            // ✅ Only include the tags currently visible in the UI (tags_list)
            for (DocumentsModel tagModel : tags_list) {
                try {
                    combinedTags.put(tagModel.getTag_type(), tagModel.getTag_name());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // ✅ Update the document with this clean tag set
            viewDocumentsModel.setTag(combinedTags);

            jsonObject.put("name", viewDocumentsModel.getName());
            jsonObject.put("tags", combinedTags);

            if (!is_MergePdfClicked) {
                WebServiceHelper.callHttpWebService(
                        this,
                        getContext(),
                        WebServiceHelper.RestMethodType.PUT,
                        "v3/mergepdf/" + viewDocumentsModel.getId() + "/tags",
                        "Update Tags",
                        jsonObject.toString()
                );
            } else {
                WebServiceHelper.callHttpWebService(
                        this,
                        getContext(),
                        WebServiceHelper.RestMethodType.PUT,
                        "v3/document/tags/" + viewDocumentsModel.getId(),
                        "Update Tags",
                        jsonObject.toString()
                );
            }

        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
            e.printStackTrace();
        }
    }


    private void add_tags_listing(Button btn_save_tag) {
        ll_added_tags.removeAllViews();
        if (!Objects.requireNonNull(tv_tag_type.getText()).toString().isEmpty() && !Objects.requireNonNull(tv_tag_name.getText()).toString().isEmpty()) {
            DocumentsModel documentsModel = new DocumentsModel();
            documentsModel.setTag_type(Objects.requireNonNull(tv_tag_type.getText()).toString());
            documentsModel.setTag_name(Objects.requireNonNull(tv_tag_name.getText()).toString());
            tags_list.add(documentsModel);
        }
        for (int i = 0; i < tags_list.size(); i++) {
            View view_added_tags = LayoutInflater.from(getContext()).inflate(R.layout.displays_documents_list, null);
            tv_tag_document_name = view_added_tags.findViewById(R.id.tv_document_name);
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
                    DocumentsModel documentsModel1 = tags_list.get(position);
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
                }
            });
            iv_edit_tag.setTag(i);
            iv_edit_tag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = 0;
                    if (view.getTag() instanceof Integer) {
                        position = (Integer) view.getTag();
                        view1 = ll_added_tags.getChildAt(position);
                        DocumentsModel documentsModel1 = tags_list.get(position);
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
        }
    }

    @SuppressLint("MissingInflatedId")
    private void edit_tags(String tag_type, String tag_name, int position, View view_tag, TextView tv_tag_document_name) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        cl_document.setAlpha(0.5f);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view_edit_tags = inflater.inflate(R.layout.edit_tag, null);
        tv_edit_tag_type = view_edit_tags.findViewById(R.id.tv_edit_tag_type);
        tv_edit_tag_name = view_edit_tags.findViewById(R.id.tv_edit_tag_name);
        tag_type_edit = view_edit_tags.findViewById(R.id.tag_type_edit);
        tag_type_edit.setText(R.string.tag_type);
        tag_edit = view_edit_tags.findViewById(R.id.tag_edit);
        tag_edit.setText(R.string.tag);
        tv_edit_tag_type.setHint(R.string.tag_type);
        tv_edit_tag_name.setHint(R.string.tag_type);
        AppCompatButton btn_cancel = view_edit_tags.findViewById(R.id.btn_edit_cancel_tag);
        header_name_edit = view_edit_tags.findViewById(R.id.header_name);
        header_name_edit.setText(R.string.edit_tag);
        AppCompatButton btn_save_edited_tag = view_edit_tags.findViewById(R.id.btn_edit_save_tag);
        ImageView iv_close_edit_tags = view_edit_tags.findViewById(R.id.close_edit_docs);
        tv_edit_tag_type.setText(tag_type);
        tv_edit_tag_name.setText(tag_name);
        final AlertDialog dialog = dialogBuilder.create();
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btn_save_edited_tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save_edited_tags(tv_edit_tag_type.getText().toString(), tv_edit_tag_name.getText().toString());
            }
        });
        iv_close_edit_tags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                cl_document.setAlpha(1.0f);
            }
        });
        dialog.setCancelable(false);
        // Prevents the dialog from being dismissed when pressing the back button.
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(view_edit_tags);
        dialog.show();
    }

    private void save_edited_tags(String tag_type, String tag_name) {
        try {
            // Validate edit_position
            if (edit_position < 0 || edit_position >= tags_list.size()) {
                AndroidUtils.showAlert("Invalid position for editing tag.", getActivity());
                return;
            }

            // Update the tag in the list
            DocumentsModel documentsModel = tags_list.get(edit_position);
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


    private void hideFirmBackground() {
//        tv_select_groups_view.setText("");
        UPLOAD_TAG = "Client";
        ll_category.setVisibility(GONE);
        ll_groups.setVisibility(GONE);
        ll_client_name.setVisibility(VISIBLE);
        ll_upload_groups.setVisibility(GONE);
        selected_client_groups_list.clear();
        client_groups_list.clear();
//        clearClients();
        rv_display_upload_groups_docs.removeAllViews();
        //..
        hideUploadDoc();
        //...
        tv_client.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        tv_client.setTextColor(getContext().getResources().getColor(R.color.white));
        tv_firm.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
        tv_firm.setTextColor(getContext().getResources().getColor(R.color.black));
    }

    private void clearClients() {
        count_file = 0;
        tv_selected_file.setText("");
        tv_selected_file.setHint(R.string.select_documents);
        selected_documents_list.clear();
        clearListData();
    }

    private void hideClientBackground() {
        UPLOAD_TAG = "Firm";
        ll_upload_groups.setVisibility(GONE);
        selected_client_groups_list.clear();
        upload_group_layout.setVisibility(GONE);
        ischecked_group = true;
        ll_matter.setVisibility(GONE);
        ll_category.setVisibility(VISIBLE);
        ll_groups.setVisibility(VISIBLE);
        selected_groups_list.clear();
//        tv_select_groups.setHint(R.string.select_groups);
        tv_select_groups.setText("");
        tv_select_groups.setHint(R.string.select_groups);
        groupsList.clear();
//        tv_select_groups.setText(R.string.select_groups);
        ll_client_name.setVisibility(GONE);
        rv_documents.removeAllViews();
        tv_client.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_firm.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_background));
        tv_firm.setTextColor(getContext().getResources().getColor(R.color.white));
        tv_client.setTextColor(getContext().getResources().getColor(R.color.black));
//        clearClients();
    }

    private void clearListData() {
        ll_hide_document_details.setVisibility(GONE);
//        tv_selected_file.setText("");
        selected_groups_list.clear();
        selected_documents_list.clear();
//        tv_select_groups_view.setText("Select Groups");
        langList.clear();
        tags_list.clear();
        docsList.clear();
        groupsList.clear();
    }

    private void clear_upload() {
        ll_hide_document_details.setVisibility(GONE);
        tv_selected_file.setHint(R.string.select_documents);
//        tv_selected_file.setText("");
        selected_groups_list.clear();

        selected_documents_list.clear();
        langList.clear();
        tags_list.clear();
        docsList.clear();
        groupsList.clear();

//        tv_select_groups.setHint(R.string.select_groups);
        tv_select_groups_view.setText("");
        tv_select_groups_view.setHint(R.string.select_groups);
//        tv_select_groups_view.setHint(R.string.select_groups);
    }

    private void callUploadDocumentWebservice() {
        try {
            if (Objects.equals(UPLOAD_TAG, "Client") && client_id.isEmpty()) {
                AndroidUtils.showAlert("Please check the Client Name", getActivity());
            } else if (Objects.equals(UPLOAD_TAG, "Firm") && selected_groups_list.isEmpty()) {
                AndroidUtils.showAlert("Please check the Groups", getActivity());
            } else if (Objects.equals(UPLOAD_TAG, "Client") && (!client_groups_list.isEmpty() && selected_client_groups_list.isEmpty())) {
                AndroidUtils.showAlert("Please check the Client Groups", getActivity());
            } else {
                if (docsList.isEmpty()) {
                    AndroidUtils.showAlert("Please Select Atleast one document", getActivity());
                } else {
                    progress_dialog = AndroidUtils.get_progress(getActivity());
                    totalUploads = docsList.size();
                    completedUploads = 0;
                    isAlertShown = false;
                    if (Objects.equals(UPLOAD_TAG, "Client")) {
                        for (int i = 0; i < docsList.size(); i++) {
                            JSONObject jsonObject = new JSONObject();
                            JSONArray clients = new JSONArray();
                            JSONObject clients_jobject = new JSONObject();
//                JSONArray tags = new JSONArray();
                            JSONArray groups = new JSONArray();
                            for (int k = 0; k < selected_client_groups_list.size(); k++) {
                                DocumentsModel documentsModel1 = selected_client_groups_list.get(k);
                                groups.put(documentsModel1.getGroup_id());
                            }
                            String docname = "";
                            DocumentsModel documentsModel = docsList.get(i);
                            filename = documentsModel.getName();
                            File new_file = documentsModel.getFile();
                            String doc_type = "pdf";
                            String content_string = new_file.getName().replace(".", "/");
                            String[] content_type = content_string.split("/");
                            if (content_type.length >= 2) {
                                doc_type = content_type[1];
                                docname = content_type[0];
                            }

                            for (int j = 0; j < clientsList.size(); j++) {
                                if (clientsList.get(j).getId().matches(client_id)) {
                                    ClientsModel clientsModel = clientsList.get(j);
                                    clients_jobject.put("id", clientsModel.getId());
                                    clients_jobject.put("type", clientsModel.getType());
                                    clients.put(clients_jobject);
                                }
                            }
                            //When the matter is chosen by the user.....
                            if (!matter_id.isEmpty()) {
                                JSONArray matter = new JSONArray();
                                matter.put(matter_id);
                                jsonObject.put("matters", matter);
                            }
                            jsonObject.put("name", docsList.get(i).getName());
                            jsonObject.put("description", docsList.get(i).getDescription());
                            jsonObject.put("expiration_date", docsList.get(i).getExpiration_date());
                            jsonObject.put("filename", docname);
                            jsonObject.put("category", "client");
                            jsonObject.put("clients", clients);
                            jsonObject.put("groups", groups);
//                            jsonObject.put("file", "(binary)");
                            jsonObject.put("custom_encrypt", docsList.get(i).getIsencrypted());
                            jsonObject.put("downloadDisabled", DOWNLOAD_TAG);
                            if (docsList.get(i).getTags() == null) {
                                jsonObject.put("tags", "");
                            } else {
                                jsonObject.put("tags", docsList.get(i).getTags());
                            }

                            if (doc_type.equalsIgnoreCase("apng") || doc_type.equalsIgnoreCase("avif") || doc_type.equalsIgnoreCase("gif") || doc_type.equalsIgnoreCase("jpeg") || doc_type.equalsIgnoreCase("png") || doc_type.equalsIgnoreCase("svg") || doc_type.equalsIgnoreCase("webp") || doc_type.equalsIgnoreCase("jpg")) {
                                jsonObject.put("content_type", "image/" + doc_type);
                            } else {
                                jsonObject.put("content_type", "application/" + doc_type);
                            }
                            Log.d("Content_type", doc_type);
                            WebServiceHelper.callHttpUploadWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v3/document/upload", "Upload Document", new_file, jsonObject.toString());

//            AndroidUtils.showAlert(jsonObject.toString(),getContext());
                        }
                    } else {
                        for (int i = 0; i < docsList.size(); i++) {
                            currentpoistion++;
                            JSONObject jsonObject = new JSONObject();
                            JSONArray clients = new JSONArray();
                            JSONObject clients_jobject = new JSONObject();

//                JSONArray tags = new JSONArray();
                            String docname = "";
                            DocumentsModel documentsModel = docsList.get(i);
                            filename = documentsModel.getName();
                            JSONArray groups = new JSONArray();
                            for (int k = 0; k < selected_groups_list.size(); k++) {
                                DocumentsModel documentsModel1 = selected_groups_list.get(k);
                                groups.put(documentsModel1.getGroup_id());
                            }
                            File new_file = documentsModel.getFile();
                            String doc_type = "pdf";
                            String content_string = new_file.getName().replace(".", "/");
                            String[] content_type = content_string.split("/");
                            if (content_type.length >= 2) {
                                doc_type = content_type[1];
                                docname = content_type[0];
                            }

                            for (int j = 0; j < clientsList.size(); j++) {
                                if (clientsList.get(j).getId().matches(client_id)) {
                                    ClientsModel clientsModel = clientsList.get(j);
                                    clients_jobject.put("id", clientsModel.getId());
                                    clients_jobject.put("type", clientsModel.getType());
                                    clients.put(clients_jobject);
                                }
                            }

                            jsonObject.put("name", docsList.get(i).getName());
                            jsonObject.put("description", docsList.get(i).getDescription());
                            jsonObject.put("filename", docname);
                            jsonObject.put("expiration_date", docsList.get(i).getExpiration_date());
                            jsonObject.put("category", "firm");
                            jsonObject.put("clients", "");
                            jsonObject.put("custom_encrypt", docsList.get(i).getIsencrypted());
                            jsonObject.put("groups", groups);
                            jsonObject.put("downloadDisabled", DOWNLOAD_TAG);
                            if (docsList.get(i).getTags() == null) {
                                jsonObject.put("tags", "");
                            } else {
                                jsonObject.put("tags", docsList.get(i).getTags());
                            }

                            if (doc_type.equalsIgnoreCase("apng") || doc_type.equalsIgnoreCase("avif") || doc_type.equalsIgnoreCase("gif") || doc_type.equalsIgnoreCase("jpeg") || doc_type.equalsIgnoreCase("png") || doc_type.equalsIgnoreCase("svg") || doc_type.equalsIgnoreCase("webp") || doc_type.equalsIgnoreCase("jpg")) {
                                jsonObject.put("content_type", "image/" + doc_type);
                            } else {
                                jsonObject.put("content_type", "application/" + doc_type);
                            }
                            Log.d("Content_type", "image/" + doc_type);
//            AndroidUtils.showAlert(jsonObject.toString(),getContext());
                            WebServiceHelper.callHttpUploadWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v3/document/upload", "Upload Document", new_file, jsonObject.toString());
                        }
                    }
                }
            }

        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
            e.fillInStackTrace();
        }
    }

    private void callClientWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
//            https://api.staging.digicoffer.com/professional/v3/corporate/list
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

    // Define ActivityResultLauncher
    ActivityResultLauncher<String[]> requestPermissions =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    results -> {

                        boolean permissionGranted = false;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            permissionGranted =
                                    Boolean.TRUE.equals(results.get(READ_MEDIA_IMAGES)) ||
                                            Boolean.TRUE.equals(results.get(READ_MEDIA_VIDEO)) ||
                                            Boolean.TRUE.equals(results.get(READ_MEDIA_VISUAL_USER_SELECTED)); // Allow limited
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionGranted =
                                    Boolean.TRUE.equals(results.get(READ_MEDIA_IMAGES)) ||
                                            Boolean.TRUE.equals(results.get(READ_MEDIA_VIDEO));
                        } else {
                            permissionGranted =
                                    Boolean.TRUE.equals(results.get(READ_EXTERNAL_STORAGE));
                        }

                        if (permissionGranted) {
                            BottomSheetUploadfile(); // ✅ CALL HERE
                        } else {
                            // Optional: show toast or settings dialog
//                            Toast.makeText(getContext(),
//                                    "Permission required to upload files",
//                                    Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (ContextCompat.checkSelfPermission(context, READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED && ((ContextCompat.checkSelfPermission(context, READ_MEDIA_VIDEO)) != PackageManager.PERMISSION_GRANTED) && ((ContextCompat.checkSelfPermission(context, READ_MEDIA_VISUAL_USER_SELECTED)) != PackageManager.PERMISSION_GRANTED)) {
                    requestPermissions.launch(new String[]{READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED});
                } else {
                    BottomSheetUploadfile();
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED && ((ContextCompat.checkSelfPermission(context, READ_MEDIA_VIDEO)) != PackageManager.PERMISSION_GRANTED)) {
                    requestPermissions.launch(new String[]{READ_MEDIA_IMAGES, READ_MEDIA_VIDEO});
                } else {
                    BottomSheetUploadfile();
                }
            } else {
                if (ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions.launch(new String[]{READ_EXTERNAL_STORAGE});
                } else {
                    BottomSheetUploadfile();
                }
            }
//                BottomSheetUploadfile();
            return false;
        } else {
            return true;
        }
    }


    private void BottomSheetUploadfile() {
        cl_document.setAlpha(0.5f);
        Constants.isDocEditor = false;
        bottommSheetUploadDocument = new BottomSheetUploadFile(cl_document);
        bottommSheetUploadDocument.show(getParentFragmentManager(), "");
        bottommSheetUploadDocument.setTargetFragment(Documents.this, 1);
    }

    public static String convertfiletostring(File file) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        FileInputStream fis = new FileInputStream(file);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        bufferedReader.close();
        return stringBuilder.toString();
    }


    @SuppressLint("Range")
    @Override
    public void getImagepath(File imagepath, Uri ImageURI) throws IOException {
        if ((imagepath == null)) {
            mSelectedBitmap = null;
            mSelectedUri = imagepath;
            String uri = imagepath.toString();
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
            imageLoader.displayImage(String.valueOf(Uri.fromFile(new File(uri))), imageView);
            file = imagepath;

            Cursor c = getContext().getContentResolver().query(ImageURI, null, null, null, null);
            c.moveToFirst();
            String[] content_type = file.getName().split(".");
            String file_name = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            //Displaying the files count.....
            Hide_Add_EditMeta();
            is_clicked_edit = true;
            is_clicked_add = true;
            count_file++;
            if (count_file > 0) {
                tv_selected_file.setText(count_file + " files");
            }
            if (count_file == 0) {
                tv_selected_file.setText("");
                tv_selected_file.setHint(R.string.select_documents);
            }

            load_documents(docsList, file_name, file);
        } else {
            file = getFile(requireContext(), ImageURI);
            Log.i("FILE", "Info:" + file.toString());
            String file_name = file.getName();
            count_file++;
            Hide_Add_EditMeta();
            is_clicked_edit = true;
            is_clicked_add = true;
            //Displaying the files count.....
            if (count_file > 0) {
                tv_selected_file.setText(count_file + " files");
            }
            if (count_file == 0) {
                tv_selected_file.setText("");
                tv_selected_file.setHint(R.string.select_documents);
            }
//            DocumentsModel documentsModel = new DocumentsModel();
//            documentsModel.setName(file.getName());
//            docsList.add(documentsModel);
            load_documents(docsList, file_name, file);
//            docsList.add()
        }
        cl_document.setAlpha(1.0f);
        bottommSheetUploadDocument.dismiss();
    }

    //Removing the file and decrement the files count...
    public void remove_file(boolean ischecked) {
        if (ischecked) {
            count_file--;
            if (count_file < 0) {
                count_file = 0;
            }
            tv_selected_file.setText(count_file + " files");
            if (count_file == 0) {
                ll_hide_document_details.setVisibility(GONE);
            }
        }
    }

    private void load_documents(ArrayList<DocumentsModel> docsList, String file_name, File file) {
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
        DocumentsModel documentsModel = new DocumentsModel();
        documentsModel.setName(docname);
        documentsModel.setFilename(file_name);
        documentsModel.setContent_type(doc_type);
        documentsModel.setDescription(docname);
        documentsModel.setFile(file);
        documentsModel.setIsenabled(false);
        documentsModel.setChecked(false);
        docsList.add(documentsModel);
        if (!docsList.isEmpty()) {
            ll_hide_document_details.setVisibility(VISIBLE);
            DisableDownloadBackground();
            tv_enable_encryption.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
            tv_enable_encryption.setTextColor(getContext().getColor(R.color.black));
            tv_disable_encryption.setTextColor(getContext().getColor(R.color.white));
            tv_disable_encryption.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_background));
        } else if (docsList.isEmpty()) {
            ll_hide_document_details.setVisibility(GONE);
//            hideDisableDownloadBackground();
        }
        EditMeta();
//            ll_documents.addView(view);
//        }
    }

    private void loadRecyclerview(String tag, String subtag) {
        rv_documents.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the adapter with the documents list
        adapter = new DocumentsListAdapter(docsList, tag, subtag, this, Documents.this);
        rv_documents.setAdapter(adapter);
        // rv_documents.setHasFixedSize(true);
        count_file = docsList.size();
        AndroidUtils.LoadList(rv_documents, getContext(), docsList.size(), false);
        // Update file count and hint for the selected files
        if (count_file == 0) {
            rv_documents.setVisibility(GONE);
            tv_selected_file.setText("");
            tv_selected_file.setHint(R.string.select_documents);
            ll_hide_document_details.setVisibility(GONE);
        } else {
            rv_documents.setVisibility(VISIBLE);
            tv_selected_file.setText(count_file + " files");
            ll_hide_document_details.setVisibility(VISIBLE);
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

    private void AddTag() {
        chk_box_layout.setAlpha(1F);
        chk_select_all.setEnabled(true);
        btn_upload.setVisibility(GONE);
        btn_add_tags.setVisibility(VISIBLE);
        tv_edit_meta.setTextColor(getContext().getResources().getColor(R.color.black));
        tv_add_tag.setTextColor(getContext().getResources().getColor(R.color.white));
        tv_edit_meta.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
        tv_add_tag.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        String tag = "add_tag";
        Constants.DocTagType = tag;
        for (int i = 0; i < docsList.size(); i++) {
            docsList.get(i).setChecked(false);
        }
        loadRecyclerview(tag, subtag);
    }

    //Hide when
    private void Hide_Add_EditMeta() {
        chk_box_layout.setAlpha(0.5F);
        chk_select_all.setEnabled(false);
        chk_select_all.setChecked(false);
        isselect_all_checked = true;
//        chk_select_all.setChecked(false);
        btn_add_tags.setVisibility(GONE);
        btn_upload.setVisibility(VISIBLE);
        tv_edit_meta.setTextColor(getContext().getResources().getColor(R.color.black));
        tv_add_tag.setTextColor(getContext().getResources().getColor(R.color.black));
        tv_add_tag.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_edit_meta.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
        String tag = "Hide_Add_Edit_tag";
        Constants.DocTagType = tag;
        for (int i = 0; i < docsList.size(); i++) {
            docsList.get(i).setChecked(false);
        }
        loadRecyclerview(tag, subtag);
    }

    private void EditMeta() {
        chk_box_layout.setAlpha(0.5F);
        chk_select_all.setEnabled(false);
        chk_select_all.setChecked(false);
        isselect_all_checked = true;
//        chk_select_all.setChecked(false);
        btn_upload.setVisibility(VISIBLE);
        btn_add_tags.setVisibility(GONE);
        tv_edit_meta.setTextColor(getContext().getResources().getColor(R.color.white));
        tv_add_tag.setTextColor(getContext().getResources().getColor(R.color.black));
        tv_add_tag.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_edit_meta.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_background));
        String tag = "edit_meta";
        Constants.DocTagType = tag;
        // Update documents list if required
        for (int i = 0; i < docsList.size(); i++) {
            docsList.get(i).setChecked(false);
        }
        loadRecyclerview(tag, subtag);
    }


    public static File getFile(Context context, Uri uri) throws IOException {
        File destinationFilename = new File(context.getFilesDir().getPath() + File.separatorChar + queryName(context, uri));
        try (InputStream ins = context.getContentResolver().openInputStream(uri)) {
            createFileFromStream(ins, destinationFilename);
        } catch (Exception ex) {
            Log.e("Save File", ex.getMessage());
            ex.fillInStackTrace();
        }
        return destinationFilename;
    }

    public static void createFileFromStream(InputStream ins, File destination) {
        try (OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = ins.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
        } catch (Exception ex) {
            Log.e("Save File", ex.getMessage());
            ex.fillInStackTrace();
        }
    }

    private static String queryName(Context context, Uri uri) {
        Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
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
        File filesDir = getContext().getFilesDir();
        File imageFile = new File(filesDir, "bitmap" + ".jpg");
        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
            file = imageFile;
            tv_selected_file.setText(file.getName());
            DocumentsModel documentsModel = new DocumentsModel();
            documentsModel.setName(file.getName());
            docsList.add(documentsModel);
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
            Cursor cursor = getContext().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            file = new File(picturePath);
            filename = file.getName();
            tv_selected_file.setText(file.getName());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progress_dialog != null && progress_dialog.isShowing())
            AndroidUtils.dismiss_dialog(progress_dialog);
        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                if (httpResult.getRequestType().equals("View Encrypted Doc")) {
                    String image_data = httpResult.getResponseContent();
                    encrypted_doc(image_data);
//                    Log.d("image_data", image_data);
                } else {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    if (httpResult.getRequestType().equals("Clients List")) {
                        JSONObject data = result.getJSONObject("data");
                        try {
                            loadClients(data);
                            callCorpClientWebservice();
                        } catch (Exception e) {
                            e.fillInStackTrace();
                        }
                    } else if (httpResult.getRequestType().equals("Corp Clients List")) {
                        try {
                            loadCorpClients(result);
                        } catch (Exception e) {
                            e.fillInStackTrace();
                        }
                    } else if (httpResult.getRequestType().equals("Deleted Documents")) {
                        isDownload = false;
                        JSONObject jsonObject = result.optJSONObject("data");
                        if (result.has("data")) {
                            assert jsonObject != null;
                            String url = jsonObject.optString("url");
                            checkViewType(url);
                            Log.d("TAG_Image", url);
                        } else {
                            String msg = "";
                            msg = result.getString("msg");
                            AndroidUtils.showAlert(msg, getActivity());
                            callDeletedDocumentWebservice();
                        }
                    } else if (httpResult.getRequestType().equals("Legal Matter")) {
                        JSONArray matters = result.getJSONArray("matterList");
                        loadMatters(matters);
                        if (isUploadDoc)
                            callClientGroupsWebservice();
                    } else if (httpResult.getRequestType().equals("Upload Document")) {
                        completedUploads++;
                        boolean isError = result.optBoolean("error");
                        String msg = result.optString("msg");
                        if (completedUploads == totalUploads && !isAlertShown) {
                            isAlertShown = true;
                            if (!isError) {
                                AndroidUtils.showAlert(msg, getActivity(), "");
                                rv_documents.removeAllViews();
                                view_document();
                                clearListData();
                            } else {
                                AndroidUtils.showAlert(msg, getActivity());
                            }
                        }
//                    else {
//                        view_document();
//                        clearListData();
//                    }
                    } else if (httpResult.getRequestType().equals("Groups")) {
                        JSONArray data = result.optJSONArray("data");
                        loadGroupsData(data, groupsList, false);
                    } else if (httpResult.getRequestType().equals("Client Groups")) {
                        JSONArray data = result.optJSONArray("data");
                        loadGroupsData(data, client_groups_list, true);
                    } else if (httpResult.getRequestType().equals("Display FilterDocuments")) {
                        JSONArray data = result.optJSONArray("data");
                        //The Matter list must be call only we choose the client in view documents page.
                        if (ismatter_chosen) {
                            callLegalMatter();
                        }
                        load_view_doc(data);
                        Log.d("TAG_VIEW_CLIENT", data.toString());
                    } else if (httpResult.getRequestType().equals("Display MergeFilterDocuments")) {
                        JSONObject data = result.optJSONObject("data");
                        JSONArray array = data.optJSONArray("items");
                        load_view_doc(array);
                        Log.d("TAG_VIEW_CLIENT", data.toString());
                    } else if (httpResult.getRequestType().equals("VIEW_DOCUMENT")) {
                        JSONArray docs = result.optJSONArray("docs");
                        loadViewDocuments(docs);
                        Log.d("TAG_view", docs.toString());
                    } else if (httpResult.getRequestType().equals("DELETED_DOCUMENT")) {
                        boolean error = result.optBoolean("error");
                        if (!error) {
//                            progress_dialog = AndroidUtils.get_progress(getActivity());
                            JSONArray docs = result.getJSONArray("documents");
                            load_view_doc(docs);
                            Log.d("TAG_view", docs.toString());
                        } else {
                            String msg = result.optString("msg");
                            AndroidUtils.showAlert(msg, getActivity());
                        }
                    } else if (httpResult.getRequestType().equals("Update Documents")) {
                        if (!result.optBoolean("error")) {
                            Dialog.dismiss();
                            rv_display_view_docs.removeAllViews();
//                            Objects.requireNonNull(tv_search_client_view.getText()).clear();
//                            Objects.requireNonNull(tv_search_client_views.getText()).clear();
                            callfilter_client_webservices();
//                            String msg = result.optString("msg");
                            String msg = "You have successfully \n updated document information.";
                            AndroidUtils.showAlert(msg, getActivity(), "Success");
                        } else {
                            String msg = result.optString("msg");
                            AndroidUtils.showAlert(msg, getActivity());
                        }
                    } else if (httpResult.getRequestType().equals("Download Document")) {
                        boolean iserror = result.getBoolean("error");
                        String msg = "";
                        if (iserror) {
                            msg = result.getString("msg");
                            AndroidUtils.showAlert(msg, getActivity());
                        } else {
                            rv_display_view_docs.removeAllViews();
//                        Objects.requireNonNull(tv_search_client_view.getText()).clear();
//                        Objects.requireNonNull(tv_search_client_views.getText()).clear();
                            String url = "";
                            if (!is_MergePdfClicked) {
                                url = result.optString("url");
                            } else {
                                JSONObject data = result.optJSONObject("data");
                                assert data != null;
                                url = data.optString("url");
                            }
                            AndroidUtils.showAlert("You have successfully downloaded the document.", getActivity(), "Success");
//                        String msg = result.optString("msg");
//                        AndroidUtils.showAlert(msg, getActivity());
//                        callDownloadWebservice(url, viewDocumentsModel_download.getName(), viewDocumentsModel_download.getContent_type());
                            FileDownloader.downloadFile(getContext(), url, "Download1");
                            callfilter_client_webservices();
                        }
                    } else if (httpResult.getRequestType().equals("Delete Documents")) {
                        boolean iserror = result.getBoolean("error");
                        String msg = "";
                        if (iserror) {
                            msg = result.getString("msg");
                            AndroidUtils.showAlert(msg, getActivity());
                        } else {
                            rv_display_view_docs.removeAllViews();
//                        Objects.requireNonNull(tv_search_client_view.getText()).clear();
//                        Objects.requireNonNull(tv_search_client_views.getText()).clear();
                            callfilter_client_webservices();
                            msg = result.optString("msg");
                            AndroidUtils.showAlert(msg, getActivity(), "Success");
                        }
                    } else if (httpResult.getRequestType().equals("Delete Merge Documents")) {
                        boolean iserror = result.getBoolean("error");
                        String msg = result.getString("msg");
                        if (iserror) {
                            AndroidUtils.showAlert(msg, getActivity());
                        } else {
                            AndroidUtils.showAlert(msg, getActivity(), "Success");
                            rv_display_view_docs.removeAllViews();
//                        Objects.requireNonNull(tv_search_client_view.getText()).clear();
//                        Objects.requireNonNull(tv_search_client_views.getText()).clear();
                            callfilter_client_webservices();
//                        String msg = result.optString("msg");
//                        AndroidUtils.showAlert(msg, getActivity());\
                        }
                    } else if (httpResult.getRequestType().equals("Decrypt Documents")) {
                        boolean iserror = result.getBoolean("error");
                        String msg = "";
                        if (iserror) {
                            msg = result.getString("msg");
                            AndroidUtils.showAlert(msg, getActivity());
                        } else {
                            //                        String msg = result.optString("msg");
                            rv_display_view_docs.removeAllViews();
//                        Objects.requireNonNull(tv_search_client_view.getText()).clear();
//                        Objects.requireNonNull(tv_search_client_views.getText()).clear();
                            callfilter_client_webservices();
                            msg = "You have successfully \n decrypted document information.";
                            ;
                            AndroidUtils.showAlert(msg, getActivity(), "Success");
                        }
                    } else if (httpResult.getRequestType().equals("Decrypt Doc") || (httpResult.getRequestType().equals("Other Doc View"))) {
                        if (!result.getBoolean("error")) {
                            String url = "";
//                            if (!is_MergePdfClicked) {
//                                url = result.optString("url");
//                            } else {
                            JSONObject jsonObject = result.optJSONObject("data");
                            assert jsonObject != null;
                            url = jsonObject.optString("url");
//                            }
                            if (isDownload) {
                                AndroidUtils.showAlert("You have successfully downloaded the document.", getActivity(), "Success");
//                        String msg = result.optString("msg");
//                        AndroidUtils.showAlert(msg, getActivity());
//                        callDownloadWebservice(url, viewDocumentsModel_download.getName(), viewDocumentsModel_download.getContent_type());
                                FileDownloader.downloadFile(getContext(), url, "Download1");
                            } else {
//                                debugUrl(url);
//                                String contentType=result.getJSONObject("data").optString("content_type");
//                                tempDocModel.setContent_type(contentType);
                                loadDisplayDocuments(url);
                            }
                            Log.d("TAG_Image", url);
                        } else {
                            AndroidUtils.showAlert(result.optString("msg"), getActivity());
                        }
                    } else if (httpResult.getRequestType().equals("Update Tags")) {
                        boolean iserror = result.getBoolean("error");
                        String msg = "";
                        if (iserror) {
                            msg = result.getString("msg");
                            AndroidUtils.showAlert(msg, getActivity());
                        } else {
                            msg = "You have successfully \n updated metadata for the document.";
                            AndroidUtils.showAlert(msg, getActivity(), "Success");
                        }
                        callfilter_client_webservices();
                    } else if (httpResult.getRequestType().equals("Encrypt Documents")) {
                        String msg = result.optString("msg");
                        boolean error = result.getBoolean("error");
                        if (!error) {
                            msg = "You have successfully \n encrypted document information.";
                            rv_display_view_docs.removeAllViews();
//                        Objects.requireNonNull(tv_search_client_view.getText()).clear();
//                        Objects.requireNonNull(tv_search_client_views.getText()).clear();
                            callfilter_client_webservices();
                            AndroidUtils.showAlert(msg, getActivity(), "");
                        } else {
                            AndroidUtils.showAlert(msg, getActivity());
                        }
                    } else if (httpResult.getRequestType().equals("Enabled Documents")) {
                        String msg = result.optString("msg");
                        boolean error = result.getBoolean("error");
                        if (!error) {
                            rv_display_view_docs.removeAllViews();
//                        Objects.requireNonNull(tv_search_client_view.getText()).clear();
//                        Objects.requireNonNull(tv_search_client_views.getText()).clear();
                            callfilter_client_webservices();
//                        String msg = "You have successfully \n encrypted document information.";
                        }
                        AndroidUtils.showAlert(msg, getActivity());
                    } else if (httpResult.getRequestType().equals("Disabled Documents")) {
                        String msg = result.optString("msg");
                        boolean error = result.optBoolean("error");
                        if (!error) {
                            rv_display_view_docs.removeAllViews();
//                        Objects.requireNonNull(tv_search_client_view.getText()).clear();
//                        Objects.requireNonNull(tv_search_client_views.getText()).clear();
                            callfilter_client_webservices();
//                        String msg = "You have successfully \n encrypted document information.";
                        }
                        AndroidUtils.showAlert(msg, getActivity());
                    } else if (httpResult.getRequestType().equals("Display Documents")) {
                        if (!result.getBoolean("error")) {
                            String url;
                            if (!is_MergePdfClicked) {
                                url = result.optString("url");
                            } else {
                                JSONObject jsonObject = result.optJSONObject("data");
                                assert jsonObject != null;
                                url = jsonObject.optString("url");
                            }
                            checkViewType(url);
                            Log.d("TAG_Image", url);
                        } else {
                            AndroidUtils.showAlert(result.optString("msg"), getActivity());
                        }
                    }
                }
            } catch (JSONException e) {
                e.fillInStackTrace();
                AndroidUtils.showAlert(e.getMessage(), getActivity());
            }
        } else if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Failed) {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
//                if (result.optBoolean("error")) {
                AndroidUtils.showErrorAlert(result.optString("msg"), getActivity());
//                }
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        } else {
            AndroidUtils.showErrorAlert(httpResult.getResponseContent().toString(), getActivity());
        }
    }

    private void encrypted_doc(String url) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        cl_document.setAlpha(0.5f);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.view_documents, null);
        ImageView iv_image = view.findViewById(R.id.doc_image);
        TextView header = view.findViewById(R.id.header_name);
        PDFView idPDFView = view.findViewById(R.id.idPDFView);
        header.setText(doc_name);
//        WebView pdfView = view.findViewById(R.id.webview); // Assuming the WebView has the id "pdfview"
        ImageView iv_close_edit_docs = view.findViewById(R.id.close_edit_docs);

        List<String> list = new ArrayList<>(Arrays.asList(CONTENT_TYPE.split("/")));
        if (list.get(1).equalsIgnoreCase("apng") || list.get(1).equalsIgnoreCase("avif") || list.get(1).equalsIgnoreCase("gif") || list.get(1).equalsIgnoreCase("jpeg") || list.get(1).equalsIgnoreCase("png") || list.get(1).equalsIgnoreCase("svg") || list.get(1).equalsIgnoreCase("webp") || list.get(1).equalsIgnoreCase("jpg")) {
//            pdfView.setVisibility(View.GONE);
            idPDFView.setVisibility(GONE);
            try {
//                byte[] imageData = null;
//
////                byte[] imageData1 = url.getBytes("ISO-8859-1");
//                imageData = Base64.decode(url, Base64.DEFAULT);
//                Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
//                Bitmap bitmap = BitmapFactory.decodeByteArray(url.getBytes(), 0, url.getBytes().length);

                //..
                byte[] imageData = url.getBytes(StandardCharsets.ISO_8859_1);

                // Log the length of the byte array
                Log.d("Image Decode", "Decoded byte array length: " + imageData.length);


                //...
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    if (bitmap != null) {
                        Log.d("Image Decode", "Bitmap decoded successfully");
                    } else {
                        Log.e("Image Decode1", "Bitmap decoding returned null");
                    }
                } catch (Exception e) {
                    Log.e("Image Decode2", "Error decoding byte array to Bitmap", e);
                }
                iv_image.setImageBitmap(bitmap);
                iv_image.setVisibility(VISIBLE);

//                if (imageData != null && imageData.length > 0) {
//                    // Decode byte array to Bitmap
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
////                Bitmap bitmap = BitmapFactory.decodeByteArray(url.getBytes(), 0, url.getBytes().length);
//                    iv_image.setImageBitmap(bitmap);
//                    iv_image.setVisibility(View.VISIBLE);
//                }
            } catch (Exception e) {
                e.fillInStackTrace();
            }

        } else {
            iv_image.setVisibility(GONE);
            new PDFviewer(idPDFView, url);
//            idPDFView.fromBytes(b)
//                    .load();
//            idPDFView.setVisibility(View.VISIBLE);
        }
        final AlertDialog dialog = dialogBuilder.create();
        iv_close_edit_docs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                cl_document.setAlpha(1.0f);
            }
        });
        dialog.setCancelable(false);
        // Prevents the dialog from being dismissed when pressing the back button.
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(view);
        dialog.show();
    }

    private void checkViewType(String url) {
        boolean isImage = File_Content_Type.isImage(tempDocModel.getContent_type());
        boolean isPDF = File_Content_Type.isPDF(tempDocModel.getContent_type());
        boolean isEncrypted = tempDocModel.isAdded_encryption() || tempDocModel.isIs_encrypted();

        // 1️⃣ If encrypted, directly call decrypt API and return
        if (isEncrypted) {
            callDecryptApi(tempDocModel.getId(), false);
        } else if (!is_MergePdfClicked) {
            loadDisplayDocuments(url);
        } else if (!isPDF && (!isImage)) {
            callOtherDocViewApi(tempDocModel.getId());
        } else {
            loadDisplayDocuments(url);
        }
    }

    private void loadDisplayDocuments(String url) {
        // 2️⃣ Inflate dialog view once
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        cl_document.setAlpha(0.5f);
        boolean isImage = File_Content_Type.isImage(tempDocModel.getContent_type());
        boolean isPDF = File_Content_Type.isPDF(tempDocModel.getContent_type());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.view_documents, null);
        ProgressBar progressBar = view.findViewById(R.id.progress_pdf);
        ImageView iv_image = view.findViewById(R.id.doc_image);
        PDFView idPDFView = view.findViewById(R.id.idPDFView);
        WebView webView = view.findViewById(R.id.doc_webview);
        TextView header = view.findViewById(R.id.header_name);
        ImageView iv_close_edit_docs = view.findViewById(R.id.close_edit_docs);
        header.setText(doc_name);

        // 3️⃣ Set visibility based on type
        iv_image.setVisibility(GONE);
        idPDFView.setVisibility(GONE);
        webView.setVisibility(GONE);

        // 4️⃣ Create dialog
        final AlertDialog dialog = dialogBuilder.create();
        Dialog = dialog; // store reference if needed

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
//            Glide.with(requireContext())
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
            idPDFView.setVisibility(VISIBLE);
            progressBar.setVisibility(VISIBLE);

            pdfTask[0] = new RetrievePDFfromUrl(idPDFView, progressBar);
            pdfTask[0].execute(url);
        } else {
            if (isImage) {
                iv_image.setVisibility(VISIBLE);
                Glide.with(getContext())
                        .load(url)
                        .placeholder(R.drawable.progress_animation)
                        .centerCrop()
                        .into(iv_image);
            } else {
                // Load PDF
                idPDFView.setVisibility(VISIBLE);
                progressBar.setVisibility(VISIBLE);

                pdfTask[0] = new RetrievePDFfromUrl(idPDFView, progressBar);
                pdfTask[0].execute(url);
            }
        }
        dialog.setOnDismissListener(d -> cl_document.setAlpha(1.0f));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(view);
        dialog.show();
    }


    // create an async task class for loading pdf file from URL.
    private void handleDocumentDisplay(String url, ViewDocumentsModel docModel, PDFView idPDFView, ImageView iv_image) {
        boolean isImage = File_Content_Type.isImage(docModel.getContent_type());
        boolean isPDF = File_Content_Type.isPDF(docModel.getContent_type());
        boolean isEncrypted = docModel.isAdded_encryption() || docModel.isIs_encrypted();

        if (isEncrypted) {
            // Encrypted doc → call decrypt API
            Dialog.dismiss();
            callDecryptApi(docModel.getId(), false);
            return;
        }

        if (isImage) {
            // Show image
            idPDFView.setVisibility(GONE);
            iv_image.setVisibility(VISIBLE);

            Glide.with(requireContext())
                    .load(url)
                    .placeholder(R.drawable.progress_animation)
                    .centerCrop()
                    .into(iv_image);
        } else if (isPDF) {
            // Show PDF
            iv_image.setVisibility(GONE);
            idPDFView.setVisibility(VISIBLE);
//            new RetrievePDFfromUrl(idPDFView).execute(url);
        } else {
            // Other documents → call WebView API
            Dialog.dismiss();
            callOtherDocViewApi(docModel.getId());
        }
    }

    private void load_view_doc(JSONArray docs) throws JSONException {
        try {
            view_docs_list.clear();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            for (int i = 0; i < docs.length(); i++) {
                ViewDocumentsModel viewDocumentsModel = new ViewDocumentsModel();
                JSONObject jsonObject = docs.getJSONObject(i);
                viewDocumentsModel.setCreated(jsonObject.optString("created"));
                viewDocumentsModel.setContent_type(jsonObject.optString("content_type"));
                viewDocumentsModel.setDescription(jsonObject.optString("description"));
                viewDocumentsModel.setExpiration_date(jsonObject.optString("expiration_date"));
                viewDocumentsModel.setFilename(jsonObject.optString("filename"));
                viewDocumentsModel.setId(jsonObject.optString("id"));
                viewDocumentsModel.setIsdisabled(jsonObject.optBoolean("isdisabled"));
                viewDocumentsModel.setIs_disabled(jsonObject.optBoolean("is_disabled"));
                viewDocumentsModel.setIs_encrypted(jsonObject.optBoolean("is_encrypted"));
                viewDocumentsModel.setAdded_encryption(jsonObject.optBoolean("added_encryption"));
                viewDocumentsModel.setIs_password(jsonObject.optBoolean("is_password"));
                viewDocumentsModel.setName(jsonObject.optString("name"));
                viewDocumentsModel.setOrigin(jsonObject.optString("origin"));
                if (!is_MergePdfClicked) {
                    viewDocumentsModel.setUploaded_by(jsonObject.optString("createdby"));
                } else {
                    viewDocumentsModel.setUploaded_by(jsonObject.optString("uploaded_by"));
                }
                viewDocumentsModel.setDoc_type(jsonObject.optString("doctype"));
                viewDocumentsModel.setCategory(jsonObject.optString("category"));
                viewDocumentsModel.setDeletedBy(jsonObject.optString("deletedBy"));
                String rawDeletedOn = jsonObject.optString("deletedOn");
                String fixedDeletedOn = AndroidUtils.normalizeDeletedOn(rawDeletedOn);
                viewDocumentsModel.setDeletedOn(fixedDeletedOn);
                viewDocumentsModel.setTag(jsonObject.optJSONObject("tags"));
                viewDocumentsModel.setTagslist(jsonObject.optJSONArray("tag"));
                view_docs_list.add(viewDocumentsModel);
            }
            // **Sort by created date (Descending)**
            Collections.sort(view_docs_list, new Comparator<ViewDocumentsModel>() {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());

                @Override
//                public int compare(ViewDocumentsModel doc1, ViewDocumentsModel doc2) {
//                    if (!DOCUMENT_TYPE_TAG.equals("Deleted")) {
//                        try {
//                            Date date1 = sdf.parse(doc1.getCreated());
//                            Date date2 = sdf.parse(doc2.getCreated());
//                            return date2.compareTo(date1); // Descending order (most recent first)
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                            return 0; // Keep original order if parsing fails
//                        }
//                    } else {
//                        try {
//                            if (!(doc1.getDeletedOn() == null || doc2.getDeletedOn() == null)) {
//                                Date date1 = sdf.parse(doc1.getDeletedOn());
//                                Date date2 = sdf.parse(doc2.getDeletedOn());
//                                return date2.compareTo(date1);
//                            } else {
//                                Date date1 = sdf.parse(doc1.getCreated());
//                                Date date2 = sdf.parse(doc2.getCreated());
//                                return date2.compareTo(date1);
//                            }
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                            return 0; // Keep original order if parsing fails
//                        }
//                    }
                public int compare(ViewDocumentsModel doc1, ViewDocumentsModel doc2) {
                    try {
                        if (!DOCUMENT_TYPE_TAG.equals("Deleted")) {
                            Date date1 = sdf.parse(doc1.getCreated());
                            Date date2 = sdf.parse(doc2.getCreated());
                            return date2.compareTo(date1); // Descending
                        } else {
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
                            try {
                                Date date1 = sdf.parse(doc1.getCreated());
                                Date date2 = sdf.parse(doc2.getCreated());
                                return date2.compareTo(date1);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return 0;
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return 0;
                    }
                }
            });

            currentPage = 1;
            setupPagination(view_docs_list);
            loadViewDocumentsRecyclerview(view_docs_list);
            UpdatePageButton(currentPage);

        } catch (JSONException e) {
            e.printStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }

    }

    private void loadViewDocuments(JSONArray docs) throws JSONException {
        try {
            view_docs_list.clear();
            for (int i = 0; i < docs.length(); i++) {
                ViewDocumentsModel viewDocumentsModel = new ViewDocumentsModel();
                JSONObject jsonObject = docs.getJSONObject(i);
                viewDocumentsModel.setCreated(jsonObject.optString("created"));
                viewDocumentsModel.setContent_type(jsonObject.optString("content_type"));
                viewDocumentsModel.setDescription(jsonObject.optString("description"));
                viewDocumentsModel.setExpiration_date(jsonObject.optString("expiration_date"));
                viewDocumentsModel.setFilename(jsonObject.optString("filename"));
                viewDocumentsModel.setId(jsonObject.optString("id"));
                viewDocumentsModel.setIsdisabled(jsonObject.optBoolean("isdisabled"));
                viewDocumentsModel.setIs_disabled(jsonObject.optBoolean("is_disabled"));
                viewDocumentsModel.setIs_encrypted(jsonObject.optBoolean("is_encrypted"));
                viewDocumentsModel.setAdded_encryption(jsonObject.optBoolean("added_encryption"));
                viewDocumentsModel.setIs_password(jsonObject.optBoolean("is_password"));
                viewDocumentsModel.setName(jsonObject.optString("name"));
                viewDocumentsModel.setOrigin(jsonObject.optString("origin"));
                viewDocumentsModel.setUploaded_by(jsonObject.optString("uploaded_by"));
                viewDocumentsModel.setDoc_type(jsonObject.optString("doctype"));
                viewDocumentsModel.setDeletedBy(jsonObject.optString("deletedBy"));
                String rawDeletedOn = jsonObject.optString("deletedOn");
                String fixedDeletedOn = AndroidUtils.normalizeDeletedOn(rawDeletedOn);
                viewDocumentsModel.setDeletedOn(fixedDeletedOn);
                viewDocumentsModel.setTag(jsonObject.optJSONObject("tags"));
                viewDocumentsModel.setTagslist(jsonObject.optJSONArray("tag"));
                view_docs_list.add(viewDocumentsModel);
                Log.d("VIEW_POSITION", view_docs_list.get(i).toString());
            }
            currentPage = 1;
            loadViewDocumentsRecyclerview(view_docs_list);
        } catch (JSONException e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void loadViewDocumentsRecyclerview(ArrayList<ViewDocumentsModel> view_docs_list) {
        try {
            if (view_docs_list.isEmpty()) {
                rv_display_view_docs.removeAllViews();
                rv_display_view_docs.setVisibility(GONE);
                ll_page_navigaiton.setVisibility(GONE);
//                AndroidUtils.showAlert("No documents to display", getActivity());
            } else {
                rv_display_view_docs.setLayoutManager(new GridLayoutManager(getContext(), 1));
                adapter1 = new View_documents_adapter(view_docs_list, this, getContext(), is_MergePdfClicked, DOCUMENT_TYPE_TAG,new ArrayList<>());
                rv_display_view_docs.setAdapter(adapter1);
                //rv_display_view_docs.setHasFixedSize(true);
                if (!view_docs_list.isEmpty()) {
//                int startIndex = (page - 1) * itemsPerPage;
//                int endIndex = Math.min(startIndex + itemsPerPage, view_docs_list.size());
                    int startIndex = PaginationHelper.startIndexForCurrentPage(currentPage, 10);
                    int endIndex = PaginationHelper.endIndexForCurrentPage(startIndex, view_docs_list.size(), 10);
                    end_temp = endIndex;
                    pageItems = new ArrayList<>(view_docs_list.subList(startIndex, endIndex));
                    adapter1.setData(pageItems);
                    UpdatePageButton(1);
                    rv_display_view_docs.setVisibility(VISIBLE);
                    ll_page_navigaiton.setVisibility(VISIBLE);
                }
                if (!tv_search_client_view.getText().toString().isEmpty()) {
                    Searchfilter(tv_search_client_view);
                }
                if (!tv_search_client_views.getText().toString().isEmpty()) {
                    Searchfilter(tv_search_client_views);
                }
                tv_search_client_view.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        Searchfilter(tv_search_client_view);
//                        adapter1.getFilter().filter(tv_search_client_view.getText().toString());
                    }
                });
                tv_search_client_views.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        Searchfilter(tv_search_client_views);
                    }
                });
//                if (progress_dialog != null && progress_dialog.isShowing())
//                    AndroidUtils.dismiss_dialog(progress_dialog);
            }
        } catch (Exception e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private ArrayList<ViewDocumentsModel> filterList(ArrayList<ViewDocumentsModel> list, String charString) {
        isListFiltered = false;

        // If the search string is empty, return the full list
        if (charString.isEmpty()) {
            return list;
        } else {
            ArrayList<ViewDocumentsModel> filteredList = new ArrayList<>();

            // Use a Pattern with case-insensitive flag for matching
            Pattern pattern = Pattern.compile(Pattern.quote(charString), Pattern.CASE_INSENSITIVE);

            for (ViewDocumentsModel row : list) {
                // Check if any of the fields contain the search string
                if (pattern.matcher(AndroidUtils.isNull(row.getName()).toLowerCase()).find()
                        || pattern.matcher(AndroidUtils.isNull(row.getFilename()).toLowerCase()).find()) {
                    filteredList.add(row);
                }
            }

            // If no matches are found, return an empty list instead of the original list
            if (filteredList.isEmpty()) {
                return new ArrayList<>(); // Return empty list
            } else {
                isListFiltered = true;
                return filteredList;
            }
        }
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
            if (isclient_groups) {
//                ll_upload_groups.setVisibility(View.VISIBLE);
                GroupsPopup(ll_upload_groups, client_groups_list, selected_client_groups_list, rv_upload_groups, tv_select_upload_groups);
            } else {
                ll_upload_client_group.setVisibility(GONE);
                if (isUploadDoc)
                    GroupsPopup(upload_group_layout, groupsList, selected_groups_list, rv_display_upload_groups_docs, tv_select_groups);
                else
                    GroupsPopup(view_group_layout, groupsList, selected_groups_list, rv_display_view_groups_docs, tv_select_groups_view);
            }
        } catch (
                JSONException e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    @SuppressLint("MissingInflatedId")
    private void GroupsPopup(LinearLayout upload_group_layout, ArrayList<DocumentsModel> groupsList, ArrayList<DocumentsModel> selected_groups_list, RecyclerView rv_display_upload_groups_docs, TextView tv_select_groups) {
        tv_select_groups.setText("");
        tv_select_groups.setHint(R.string.select_groups);
        upload_group_layout.setVisibility(VISIBLE);
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
                    tv_select_groups.setHint(R.string.select_groups);
                    // Update TextView with selected groups
                    String[] value = new String[selected_groups_list.size()];
                    for (int i = 0; i < selected_groups_list.size(); i++) {
                        value[i] = selected_groups_list.get(i).getGroup_name();
                    }
                    String str = TextUtils.join(",", value);
                    tv_select_groups.setText(str);
                    EnableUpload();
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

    private void loadClientGroups() {
        try {
            // Upload Documents Group Selection
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            rv_upload_groups.setLayoutManager(layoutManager);
            // rv_upload_groups.setHasFixedSize(true);

            GroupsListAdapter documentsAdapter = new GroupsListAdapter(client_groups_list, Documents.class.newInstance(), new GroupsListAdapter.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(DocumentsModel documentsModel) {
                    if (documentsModel.isGroupChecked()) {
                        selected_client_groups_list.add(documentsModel);
                    } else {
                        // Remove the unchecked item from selected_groups_list
                        for (int i = 0; i < selected_client_groups_list.size(); i++) {
                            if (selected_client_groups_list.get(i).getGroup_id().equals(documentsModel.getGroup_id())) {
                                selected_client_groups_list.remove(i);

                                break; // Exit loop after removing the item
                            }
                        }
                    }

                    // Update TextView with selected groups
                    String[] value = new String[selected_client_groups_list.size()];
                    for (int i = 0; i < selected_client_groups_list.size(); i++) {
                        value[i] = selected_client_groups_list.get(i).getGroup_name();
                    }
                    String str = TextUtils.join(",", value);
                    tv_select_upload_groups.setText(str);
                }
            });

            rv_display_upload_groups_docs.setAdapter(documentsAdapter);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void loadMatters(JSONArray matters) throws JSONException {
        //Adding a list first value as empty...
//        matterlist.add(0, new MattersModel());
        if (!isUploadDoc) {
            MattersModel mattersModel1 = new MattersModel();
            mattersModel1.setId("all");
            mattersModel1.setTitle("All Documents");
            mattersModel1.setType("consumer");
            matterlist.add(0, mattersModel1);
        }
        for (int i = 0; i < matters.length(); i++) {
            JSONObject jsonObject = matters.getJSONObject(i);
            MattersModel mattersModel = new MattersModel();
            mattersModel.setId(jsonObject.optString("id"));
            mattersModel.setTitle(jsonObject.optString("title"));
            mattersModel.setType(jsonObject.optString("type"));
            matterlist.add(mattersModel);
        }
        if (matterlist.isEmpty()) {
            ll_matter_view.setVisibility(GONE);
            ll_matter.setVisibility(GONE);
        } else {
            ll_matter_view.setVisibility(VISIBLE);
            ll_matter.setVisibility(VISIBLE);
        }
        initMatter();
    }

    private void initMatter() {
        final CommonSpinnerAdapter adapter = new CommonSpinnerAdapter(getActivity(), this.matterlist);
        Log.i("ArrayList", "Info:" + matterlist);
//        sp_matter.setAdapter(adapter);
        list_matter.setAdapter(adapter);
        custom_spinner2.setText("");
        custom_spinner4.setText("");
        list_matter_view.setAdapter(adapter);
        AndroidUtils.LoadList(list_matter, getContext(), matterlist.size(), true);
        AndroidUtils.LoadList(list_matter_view, getContext(), matterlist.size(), true);

        list_matter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                matter_name = Documents.this.clientsList.get(position).getName();
                matter_id = matterlist.get(position).getId();
                String matter_name = matterlist.get(position).getTitle();
                Log.d("Matter_value_name", matter_name);
                custom_spinner2.setText(matter_name);
                list_matter.setVisibility(GONE);
                ischecked_matter = true;
                img_dropdown_icon2.setVisibility(GONE);
                img_clear_icon2.setVisibility(VISIBLE);
                callClientGroupsWebservice();
            }
        });

        list_matter_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                matter_name = Documents.this.clientsList.get(position).getName();
                matter_id = matterlist.get(position).getId();
                String matter_name = matterlist.get(position).getTitle();
                Log.d("Matter_value_name", matter_name);
                custom_spinner4.setText(matter_name);
                list_matter_view.setVisibility(GONE);
                ischecked_matter2 = true;
                //The matter list should not call.
                ismatter_chosen = false;
                callfilter_client_webservices();
                rv_display_view_docs.setVisibility(VISIBLE);
                img_dropdown_icon4.setVisibility(GONE);
                img_clear_icon4.setVisibility(VISIBLE);
            }
        });
    }

    private void initUI(ArrayList<ClientsModel> clientsList) {
        CommonSpinnerAdapter adapter = new CommonSpinnerAdapter(getActivity(), this.clientsList);
        list_client.setAdapter(adapter);
        list_client_view.setAdapter(adapter);
        AndroidUtils.LoadList(list_client, getContext(), clientsList.size(), true);
        AndroidUtils.LoadList(list_client_view, getContext(), clientsList.size(), true);
//        tv_search_client.setAdapter(adapter);

        list_client.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                matter_name = Documents.this.clientsList.get(position).getName();
                client_id = clientsList.get(position).getId();
                client_name = clientsList.get(position).getName();
                Log.d("Client_value_name", client_name);
                ll_matter.setVisibility(VISIBLE);
                matter_id = "";
                matterlist.clear();
                callLegalMatter();
                Log.d("Matter_list_number", "" + matterlist.size());
                AndroidUtils.DisplaySpinnerView(list_client, custom_spinner, client_name, img_dropdown_icon1, img_clear_icon1, true);
                EnableUpload();
                img_dropdown_icon2.setVisibility(VISIBLE);
                img_clear_icon2.setVisibility(GONE);
                img_dropdown_icon1.setVisibility(GONE);
                img_clear_icon1.setVisibility(VISIBLE);
                ischecked = true;
            }
        });
        list_client_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                matter_name = Documents.this.clientsList.get(position).getName();
                selected_groups_list.clear();
                client_id = clientsList.get(position).getId();
                String client_name = clientsList.get(position).getName();
                Log.d("Client_value_name", client_name);
                custom_spinner3.setText(client_name);
                currentPage = 1;
                ll_matter_view.setVisibility(VISIBLE);
                CATEGORY_TAG = "client";
                custom_spinner4.setText("");
                matterlist.clear();
                matter_id = "";
                ismatter_chosen = true;
                callfilter_client_webservices();
                rv_display_view_docs.setVisibility(VISIBLE);
                list_client_view.setVisibility(GONE);
                ischecked2 = true;
                img_dropdown_icon3.setVisibility(GONE);
                img_clear_icon3.setVisibility(VISIBLE);
                img_dropdown_icon4.setVisibility(VISIBLE);
                img_clear_icon4.setVisibility(GONE);
            }
        });
    }

    private void loadCorpClients(JSONObject data) throws JSONException {
        JSONArray relationships = data.getJSONArray("relationships");
        //Adding a list first value as empty...
//        clientsList.add(0, new ClientsModel());
        CorpClientsList.clear();
        for (int i = 0; i < relationships.length(); i++) {
            JSONObject jsonObject = relationships.getJSONObject(i);
            ClientsModel clientsModel = new ClientsModel();
            clientsModel.setId(jsonObject.optString("id"));
            clientsModel.setName(jsonObject.optString("name"));
            if (!jsonObject.optString("type").equals("consumer")) {
                clientsModel.setType("corporate");
            }
            CorpClientsList.add(clientsModel);
//                    updatedClients.add(clientsModel);
        }
        clientsList.addAll(CorpClientsList);
        initUI(clientsList);
        // intUI(clientsList);
    }

    private void loadClients(JSONObject data) throws JSONException {
        JSONArray relationships = data.getJSONArray("relationships");
        //Adding a list first value as empty...
//        clientsList.add(0, new ClientsModel());
        clientsList.clear();
        for (int i = 0; i < relationships.length(); i++) {
            JSONObject jsonObject = relationships.getJSONObject(i);
            ClientsModel clientsModel = new ClientsModel();
            clientsModel.setId(jsonObject.optString("id"));
            clientsModel.setName(jsonObject.optString("name"));
            clientsModel.setType(jsonObject.optString("type"));
            clientsList.add(clientsModel);
//                    updatedClients.add(clientsModel);
        }
//        initUI(clientsList);
        // intUI(clientsList);
    }

    private void EnableUpload() {
        if ((!custom_spinner.getText().toString().isEmpty()) || (!selected_groups_list.isEmpty())) {
            Enable_Button(btn_upload, true);
        } else {
            Enable_Button(btn_upload, false);
        }
    }

    private void callLegalMatter() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            Log.d("Client_id", client_id);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/matter/all/" + client_id, "Legal Matter", jsonObject.toString());

        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
            e.fillInStackTrace();
        }
    }


    @Override
    public void ViewTags(DocumentsModel documentsModel, ArrayList<DocumentsModel> itemsArrayList) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        cl_document.setAlpha(0.5f);
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
                cl_document.setAlpha(1.0f);
            }
        });
        dialog.setCancelable(false);
        // Prevents the dialog from being dismissed when pressing the back button.
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(view_edit_tags);
        dialog.show();

    }

    @Override
    public void EditDocuments(DocumentsModel documentsModel, ArrayList<DocumentsModel> itemsArrayList, int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        cl_document.setAlpha(0.5f);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view_edit_documents = inflater.inflate(R.layout.edit_meta_data, null);
        TextView response_docname, response_docdes;
        response_docname = view_edit_documents.findViewById(R.id.response_docname);
        response_docdes = view_edit_documents.findViewById(R.id.response_docdes);
        ImageView iv_cancel_edit_doc = view_edit_documents.findViewById(R.id.close_edit_docs);
        AppCompatButton btn_close_edit_docs = view_edit_documents.findViewById(R.id.btn_cancel_edit_docs);
        TextInputEditText tv_doc_name = view_edit_documents.findViewById(R.id.edit_doc_name);
        tv_doc_name.setMaxLines(5);

        tv_doc_name.setFilters(filters);
        TextView tv_document_name = view_edit_documents.findViewById(R.id.tv_document_name);
        tv_document_name.setText(R.string.document_name);

        TextInputEditText tv_description = view_edit_documents.findViewById(R.id.edit_description);
        TextView description = view_edit_documents.findViewById(R.id.description);
        description.setText(R.string.description);

        AppCompatButton tv_exp_date = view_edit_documents.findViewById(R.id.tv_expiration_date);
        tv_exp_date.setHint(R.string.expiration_date);
        TextView expiration_date_id = view_edit_documents.findViewById(R.id.expiration_date_id);
        expiration_date_id.setText(R.string.expiration_date);

        tv_doc_name.setText(documentsModel.getName());
        tv_description.setText(documentsModel.getDescription());
        tv_exp_date.setText(documentsModel.getExpiration_date());
        tv_description.setMaxLines(10);
        tv_description.setFilters(filters1);
        tv_doc_name.addTextChangedListener(new Validation(tv_doc_name));
        tv_description.addTextChangedListener(new DescriptionValidation(tv_description));
        //Expiration date field.....
        Calendar myCalendar = Calendar.getInstance();
        // Assuming tv_exp_date is already defined and initialized
        tv_exp_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a Calendar instance to get the current date
                Calendar myCalendar = Calendar.getInstance();

                // Define the DatePickerDialog's OnDateSetListener
                final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Update the calendar with the selected date
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, month);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // Update the label with the selected date
                        updateLabel(myCalendar);
                    }

                    private void updateLabel(Calendar calendar) {
                        // Format the date and set it to tv_exp_date
                        String myFormat = "dd-MM-yyyy";
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                        tv_exp_date.setText(sdf.format(calendar.getTime()));
                    }
                };

                // Create the DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        requireActivity(),
                        date,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)
                );

                // Set minimum selectable date to today
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

                // Add OnCancelListener to clear tv_exp_date when dialog is canceled
                datePickerDialog.setOnCancelListener(dialog -> {
                    Log.d("DatePickerDialog", "Dialog canceled");
                    tv_exp_date.setText(""); // Clear the text field when canceled
                });

                // Show the DatePickerDialog
                datePickerDialog.show();
            }
        });

//        Date c = Calendar.getInstance().getTime();
//        System.out.println("Current time => " + c);
//        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
//        String formattedDate = df.format(c);
//        tv_exp_date.setText("");

        AppCompatButton btn_save_tag = view_edit_documents.findViewById(R.id.btn_save_tag);
        Enable_Button(btn_save_tag, (!Objects.requireNonNull(tv_doc_name.getText()).toString().isEmpty()) && (!Objects.requireNonNull(tv_description.getText()).toString().isEmpty()));
        final AlertDialog dialog = dialogBuilder.create();
        iv_cancel_edit_doc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((!Objects.requireNonNull(tv_doc_name.getText()).toString().isEmpty()) || (!Objects.requireNonNull(tv_description.getText()).toString().isEmpty())) {
                    AndroidUtils.Delete_Popup(getActivity(), dialog);
                } else dialog.dismiss();
            }
        });
        btn_close_edit_docs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((!Objects.requireNonNull(tv_doc_name.getText()).toString().isEmpty()) || (!Objects.requireNonNull(tv_description.getText()).toString().isEmpty())) {
                    AndroidUtils.Delete_Popup(getActivity(), dialog);
                } else dialog.dismiss();
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Enable_Button(btn_save_tag, (!Objects.requireNonNull(tv_doc_name.getText()).toString().isEmpty()) && (!Objects.requireNonNull(tv_description.getText()).toString().isEmpty()));
                if (tv_doc_name.getText().toString().isEmpty()) {
                    response_docname.setVisibility(VISIBLE);
                    response_docname.setText("Document Name is required");
                } else {
                    response_docname.setVisibility(GONE);
                }
                if (Objects.requireNonNull(tv_description.getText()).toString().isEmpty()) {
                    response_docdes.setVisibility(VISIBLE);
                    response_docdes.setText("Description is required");
                } else {
                    response_docdes.setVisibility(GONE);
                }
            }
        };
        tv_doc_name.addTextChangedListener(textWatcher);
        tv_description.addTextChangedListener(textWatcher);
        btn_save_tag.setOnClickListener(view -> {
            for (int i = 0; i < itemsArrayList.size(); i++) {
                if (i == position) {
                    DocumentsModel documentsModel1 = itemsArrayList.get(i);
                    documentsModel1.setName(Objects.requireNonNull(tv_doc_name.getText()).toString());
                    documentsModel1.setDescription(Objects.requireNonNull(tv_description.getText()).toString());
                    documentsModel1.setExpiration_date(tv_exp_date.getText().toString());
                    itemsArrayList.set(i, documentsModel1);
                    dialog.dismiss();
                    String tag = "edit_meta";
                    loadRecyclerview(tag, "");
                }
            }
        });
        dialog.setOnDismissListener(dialog1 -> cl_document.setAlpha(1.0f));

        dialog.setCancelable(false);
        // Prevents the dialog from being dismissed when pressing the back button.
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(view_edit_documents);
        dialog.show();
    }

    private void handleEditDocClose(String name, String description, String exp_date, TextView tv_doc_name, TextView tv_description, TextView tv_exp_date, Dialog dialog) {
        String docName = Objects.requireNonNull(tv_doc_name.getText()).toString();
        String desc = Objects.requireNonNull(tv_description.getText()).toString();
        String exp = Objects.requireNonNull(tv_exp_date.getText()).toString();
        if ((!docName.isEmpty() && (!docName.equals(name))) || (!desc.isEmpty() && (!desc.equals(description))) || (!exp.isEmpty() && (!exp.equals(exp_date)))) {
            AndroidUtils.Delete_Popup(getActivity(), dialog);
        } else {
            dialog.dismiss();
        }
    }

    @Override
    public void RemoveDocument(int position, String tag) {
        if (position < 0 || position >= docsList.size()) return;

        docsList.remove(position);

        adapter.notifyItemRemoved(position);
        loadRecyclerview(tag, "");
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
    public void edit_document(ViewDocumentsModel viewDocumentsModel) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        cl_document.setAlpha(0.5f);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view_edit_documents = inflater.inflate(R.layout.edit_meta_data, null);
        TextView response_docname, response_docdes;
        response_docname = view_edit_documents.findViewById(R.id.response_docname);
        response_docdes = view_edit_documents.findViewById(R.id.response_docdes);
        ImageView iv_cancel_edit_doc = view_edit_documents.findViewById(R.id.close_edit_docs);
        AppCompatButton btn_close_edit_docs = view_edit_documents.findViewById(R.id.btn_cancel_edit_docs);
        TextInputEditText tv_doc_name = view_edit_documents.findViewById(R.id.edit_doc_name);
        tv_doc_name.setMaxLines(5);
        tv_doc_name.setFilters(filters);
        TextView tv_document_name = view_edit_documents.findViewById(R.id.tv_document_name);
        tv_document_name.setText(R.string.document_name);
        TextInputEditText tv_description = view_edit_documents.findViewById(R.id.edit_description);
        tv_description.setMaxLines(10);
        tv_description.setFilters(filters1);

        tv_doc_name.addTextChangedListener(new Validation(tv_doc_name));
        tv_description.addTextChangedListener(new DescriptionValidation(tv_description));
        TextView description = view_edit_documents.findViewById(R.id.description);
        description.setText(R.string.description);
        AppCompatButton tv_exp_date = view_edit_documents.findViewById(R.id.tv_expiration_date);
        tv_exp_date.setHint(R.string.expiration_date);
        TextView expiration_date_id = view_edit_documents.findViewById(R.id.expiration_date_id);
        expiration_date_id.setText(R.string.expiration_date);

//        TextInputEditText tv_expiration_date = view_edit_documents.findViewById(R.id.tv_expiration_date);
//        Calendar myCalendar = Calendar.getInstance();
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
//                tv_exp_date.setText(sdf.format(myCalendar.getTime()));
//            }
//        };
        // Assuming tv_exp_date is already defined and initialized
        tv_exp_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a Calendar instance to get the current date
                Calendar myCalendar = Calendar.getInstance();

                // Define the DatePickerDialog's OnDateSetListener
                final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Update the calendar with the selected date
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, month);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // Update the label with the selected date
                        updateLabel(myCalendar);
                    }

                    private void updateLabel(Calendar calendar) {
                        // Format the date and set it to tv_exp_date
                        String myFormat = "dd-MM-yyyy";
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                        tv_exp_date.setText(sdf.format(calendar.getTime()));
                    }
                };

                // Create the DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        requireActivity(),
                        date,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)
                );

                // Set minimum selectable date to today
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

                // Add OnCancelListener to clear tv_exp_date when dialog is canceled
                datePickerDialog.setOnCancelListener(dialog -> {
                    Log.d("DatePickerDialog", "Dialog canceled");
                    tv_exp_date.setText(""); // Clear the text field when canceled
                });
                // Change CANCEL button text to CLEAR
                Button cancelButton = datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                if (cancelButton != null) {
                    cancelButton.setText("Clear");
                }
                // Show the DatePickerDialog
                datePickerDialog.show();
            }
        });

        if (viewDocumentsModel.getExpiration_date().equals("NA") || viewDocumentsModel.getExpiration_date().equals("")) {
//            Date c = Calendar.getInstance().getTime();
//            System.out.println("Current time => " + c);
//
//            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
//            String formattedDate = df.format(c);
//            tv_exp_date.setText(formattedDate);
            tv_exp_date.setText("");
        } else {
            String exp_date = viewDocumentsModel.getExpiration_date();
            Date date_new = AndroidUtils.stringToDateTimeDefault(exp_date, "MMM dd, yyyy");
            String created = AndroidUtils.getDateToString(date_new, "dd-MM-yyyy");
            tv_exp_date.setText(created);
        }
        tv_doc_name.setText(viewDocumentsModel.getName());
        tv_description.setText(viewDocumentsModel.getDescription());
//        tv_exp_date.setText(viewDocumentsModel.getExpiration_date());
        AppCompatButton btn_save_tag = view_edit_documents.findViewById(R.id.btn_save_tag);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Enable_Button(btn_save_tag, (!Objects.requireNonNull(tv_doc_name.getText()).toString().isEmpty()) && (!Objects.requireNonNull(tv_description.getText()).toString().isEmpty()));
                if (tv_doc_name.getText().toString().isEmpty()) {
                    response_docname.setVisibility(VISIBLE);
                    response_docname.setText("Document Name is required");
                } else {
                    response_docname.setVisibility(GONE);
                }
                if (Objects.requireNonNull(tv_description.getText()).toString().isEmpty()) {
                    response_docdes.setVisibility(VISIBLE);
                    response_docdes.setText("Description is required");
                } else {
                    response_docdes.setVisibility(GONE);
                }
            }
        };
        tv_doc_name.addTextChangedListener(textWatcher);
        tv_description.addTextChangedListener(textWatcher);
        final AlertDialog dialog = dialogBuilder.create();
        iv_cancel_edit_doc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleEditDocClose(viewDocumentsModel.getName(), viewDocumentsModel.getDescription(), viewDocumentsModel.getExpiration_date(), tv_doc_name, tv_description, tv_exp_date, dialog);
            }
        });
        btn_close_edit_docs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleEditDocClose(viewDocumentsModel.getName(), viewDocumentsModel.getDescription(), viewDocumentsModel.getExpiration_date(), tv_doc_name, tv_description, tv_exp_date, dialog);
            }
        });

        btn_save_tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog = dialog;
//                if (Objects.requireNonNull(tv_doc_name.getText()).toString().isEmpty() || Objects.requireNonNull(tv_description.getText()).toString().isEmpty()) {
//                    if (tv_doc_name.getText().toString().isEmpty()) {
//                        response_docname.setVisibility(View.VISIBLE);
//                        response_docname.setText("Document Name is required");
//                    }
//                    if (Objects.requireNonNull(tv_description.getText()).toString().isEmpty()) {
//                        response_docdes.setVisibility(View.VISIBLE);
//                        response_docdes.setText("Description is required");
//                    }
//                } else {
                callUpdateDocumentWebservice(Objects.requireNonNull(tv_doc_name.getText()).toString(), tv_description.getText().toString(), tv_exp_date.getText().toString(), viewDocumentsModel.getId());
//                }
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                cl_document.setAlpha(1.0f);
            }
        });
        dialog.setCancelable(false);
        dialog.setView(view_edit_documents);
        // Prevents the dialog from being dismissed when pressing the back button.
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void callUpdateDocumentWebservice(String name, String description, String
            expiration_date, String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", name);
            jsonObject.put("description", description);
            jsonObject.put("expiration_date", expiration_date);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "v3/document/" + id, "Update Documents", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

//    @Override
//    public void delete_document(ViewDocumentsModel viewDocumentsModel) {
//        try {
//            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
//            LayoutInflater inflater = requireActivity().getLayoutInflater();
//            View view = inflater.inflate(R.layout.delete_relationship, null);
//            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
//            TextView header_name = view.findViewById(R.id.header_name);
//            ImageView close_documents = view.findViewById(R.id.close_documents);
//            header_name.setTextColor(Color.BLACK);
//            String delete_msg = "Are you sure you want to delete " + viewDocumentsModel.getName() + " document ?";
//            tv_confirmation.setText(delete_msg);
//            AppCompatButton bt_yes = view.findViewById(R.id.btn_yes);
//            AppCompatButton btn_no = view.findViewById(R.id.btn_No);
//            final AlertDialog dialog = dialogBuilder.create();

    /// /            ad_dialog_delete = dialog;
//            btn_no.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    dialog.dismiss();
//                }
//            });
//            bt_yes.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    dialog.dismiss();
//                    callDeleteDocumentWebservice(viewDocumentsModel.getId());
//                }
//            });
//            close_documents.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    dialog.dismiss();
//                }
//            });
//
//            dialog.setView(view);
//            dialog.show();
//        } catch (Exception e) {
//            AndroidUtils.showToast(e.getMessage(), getContext());
//        }
//    }
    public static String replaceLastDotWithSlash(String input) {
        int lastIndex = input.lastIndexOf('.');
        if (lastIndex == -1) {
            // No slash found in the string
            return input;
        }
        // Replace the last slash with a dot
        return input.substring(0, lastIndex) + '/' + input.substring(lastIndex + 1);
    }

    @Override
    public void Display_Document(ViewDocumentsModel viewDocumentsModel) {
//        String content_string = viewDocumentsModel.getFilename().replace(".", "/");
//        String content_type = replaceLastDotWithSlash(viewDocumentsModel.getFilename());
//        String[] content_type1 = content_type.split("/");
//        if (content_type1.length >= 2) {
//            ReceivedFileName = content_type1[1];
//        }
//        if (ReceivedFileName.equalsIgnoreCase("apng") || ReceivedFileName.equalsIgnoreCase("avif") || ReceivedFileName.equalsIgnoreCase("gif") || ReceivedFileName.equalsIgnoreCase("jpeg") || ReceivedFileName.equalsIgnoreCase("png") || ReceivedFileName.equalsIgnoreCase("svg") || ReceivedFileName.equalsIgnoreCase("webp") || ReceivedFileName.equalsIgnoreCase("jpg")) {
//            viewDocumentsModel.setContent_type("image/" + ReceivedFileName);
//        } else {
//            viewDocumentsModel.setContent_type("application/" + ReceivedFileName);
//        }
//        CONTENT_TYPE = "";
        CONTENT_TYPE = viewDocumentsModel.getContent_type();
//        if (CONTENT_TYPE.isEmpty()) {
//            CONTENT_TYPE = "image/png";
//        }
        Log.d("IMage_name_content", CONTENT_TYPE);
        Log.d("IMage_name_content", CONTENT_TYPE);
        tempDocModel = viewDocumentsModel;
//        if (viewDocumentsModel.isAdded_encryption() || viewDocumentsModel.isIs_encrypted()) {
//            ViewEncryptedDoc(viewDocumentsModel.getId());
//        } else {
        if (DOCUMENT_TYPE_TAG.equals("Deleted")) {
            Deleted_Document(viewDocumentsModel, "view");
        } else {
            callDisplayDocumentWebservice(viewDocumentsModel.getId());
        }
        doc_name = "";
        doc_name = viewDocumentsModel.getName();
//        }
    }

    public void Deleted_Document(ViewDocumentsModel viewDocumentsModel, String delete) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("docid", viewDocumentsModel.getId());
            jsonObject.put("doctype", viewDocumentsModel.getDoc_type());
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "docs/deleted/" + delete, "Deleted Documents", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    public void ViewEncryptedDoc(String doc_id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
//            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                    .addFormDataPart("docid",doc_id)
//                    .build();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("docid", doc_id);
            WebServiceHelper.callEmailHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v3/decrypt", "View Encrypted Doc", jsonObject.toString());
            Log.d("Token111", Constants.TOKEN);
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }

    }

    public interface OnConfirmClickListener {
        void onYesClick();

        void onNoClick();
    }

    public void showConfirmDialog(Context context, String message, String title, OnConfirmClickListener listener) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            View view = LayoutInflater.from(context).inflate(R.layout.delete_relationship, null);

            TextView header_name = view.findViewById(R.id.header_name);
            ImageView close_documents = view.findViewById(R.id.close_documents);
            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
            AppCompatButton btn_yes = view.findViewById(R.id.btn_yes);
            AppCompatButton btn_no = view.findViewById(R.id.btn_No);

            header_name.setTextColor(Color.BLACK);
            header_name.setText(title);
            tv_confirmation.setText(message);

            AlertDialog dialog = dialogBuilder.create();
            dialog.setView(view);

            // CLOSE
            close_documents.setOnClickListener(v -> {
                dialog.dismiss();
                listener.onNoClick();
            });

            btn_no.setOnClickListener(v -> {
                dialog.dismiss();
                listener.onNoClick();
            });

            btn_yes.setOnClickListener(v -> {
                dialog.dismiss();
                listener.onYesClick();
            });

            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void ViewDialog(ViewDocumentsModel viewDocumentsModel, String ViewType) {
//        try {
//            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
//            cl_document.setAlpha(0.5f);
//            LayoutInflater inflater = requireActivity().getLayoutInflater();
//            View view = inflater.inflate(R.layout.delete_relationship, null);
//            TextView header_name = view.findViewById(R.id.header_name);
//            ImageView close_documents = view.findViewById(R.id.close_documents);
//            header_name.setTextColor(Color.BLACK);
//            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
//            String delete_msg = "";
//            if (ViewType.equals("encrypt")) {
//                delete_msg = "Are you sure you want to encrypt Document?";
//            } else if (ViewType.equals("decrypt")) {
//                delete_msg = "Are you sure you want to decrypt Document?";
//            } else if (ViewType.equals("deleted")) {
//                delete_msg = "Are you sure to permanently delete document?";
//            } else if (ViewType.equals("restore")) {
//                delete_msg = "Are you sure to restore document?";
//            } else {
//                delete_msg = "Are you sure you want to delete Document?";
//            }
//            tv_confirmation.setText(delete_msg);
//            AppCompatButton bt_yes = view.findViewById(R.id.btn_yes);
//            AppCompatButton btn_no = view.findViewById(R.id.btn_No);
//
////            ll_view_docs.setEnabled(false);
//            final AlertDialog dialog = dialogBuilder.create();

    /// /            ad_dialog_delete = dialog;
//            // Ensure cl_document's alpha is reset when the dialog is dismissed.
//            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialog) {
//                    cl_document.setAlpha(1.0f);
//                }
//            });
//            btn_no.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    dialog.dismiss();
//                }
//            });
//            close_documents.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    dialog.dismiss();
//                }
//            });
//            bt_yes.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    dialog.dismiss();
//                    if (ViewType.equals("encrypt")) {
//                        encryption_doc(viewDocumentsModel.getId());
//                    } else if (ViewType.equals("decrypt")) {
//                        decryption_doc(viewDocumentsModel.getId());
//                    } else if (ViewType.equals("deleted")) {
//                        Deleted_Document(viewDocumentsModel, "delete");
//                    } else if (ViewType.equals("restore")) {
//                        Deleted_Document(viewDocumentsModel, ViewType);
//                    } else callDeleteDocumentWebservice(viewDocumentsModel.getId());
//                }
//            });
//            dialog.setView(view);
//            dialog.show();
//        } catch (Exception e) {
//            AndroidUtils.showAlert(e.getMessage(), getActivity());
//        }
//    }
    @Override
    public void ViewDialog(ViewDocumentsModel viewDocumentsModel, String ViewType) {
        String deleteMsg;
        String title = "Alert ! ";
        switch (ViewType) {
            case "disabled":
                deleteMsg = "Are you sure you want to Enable Download document?";
                break;
            case "enabled":
                deleteMsg = "Are you sure you want to Disabled Download document?";
                break;
            case "encrypt":
                deleteMsg = "Are you sure you want to encrypt Document?";
                break;
            case "decrypt":
                deleteMsg = "Are you sure you want to decrypt Document?";
                break;
            case "deleted":
                deleteMsg = "Are you sure to permanently delete document?";
                break;
            case "download":
                title = "Confirmation";
                deleteMsg = "Downloading this document will remove it from the secure system.\n" +
                        "Do you wish to proceed with the download?";
                break;
            case "restore":
                deleteMsg = "Are you sure to restore document?";
                break;
            default:
                deleteMsg = "Are you sure you want to delete Document?";
                break;
        }

        showConfirmDialog(getActivity(), deleteMsg, title, new OnConfirmClickListener() {
            @Override
            public void onYesClick() {
                switch (ViewType) {
                    case "disabled":
                        enabled_doc(viewDocumentsModel.getId());
                        break;
                    case "enabled":
                        disabled_doc(viewDocumentsModel.getId());
                        break;
                    case "encrypt":
                        encryption_doc(viewDocumentsModel.getId());
                        break;
                    case "decrypt":
                        decryption_doc(viewDocumentsModel.getId());
                        break;
                    case "download":
                        Download_Document(viewDocumentsModel.getId());
                        break;
                    case "deleted":
                        Deleted_Document(viewDocumentsModel, "delete");
                        break;
                    case "restore":
                        Deleted_Document(viewDocumentsModel, ViewType);
                        break;
                    default:
                        callDeleteDocumentWebservice(viewDocumentsModel.getId());
                }
            }

            @Override
            public void onNoClick() {
                // nothing extra
            }
        });
    }

    @Override
    public void Download_Document(ViewDocumentsModel viewDocumentsModel) {
        tempDocModel = viewDocumentsModel;
    }

    public void Download_Document(String docid) {
        try {
//            viewDocumentsModel_download = viewDocumentsModel;
            JSONObject jsonObject = new JSONObject();
//            jsonObjectect.put("docid", viewDocumentsModel.getId());
//            jsonObject.put("doctype", viewDocumentsModel.getDoc_type());
            if (!is_MergePdfClicked) {
                progress_dialog = AndroidUtils.get_progress(getActivity());
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/mergepdf/" + docid + "/download", "Download Document", jsonObject.toString());
            } else if (tempDocModel.isAdded_encryption() || tempDocModel.isIs_encrypted()) {
                callDecryptApi(docid, true);
            } else {
                progress_dialog = AndroidUtils.get_progress(getActivity());
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/document/" + tempDocModel.getId() + "/download", "Download Document", jsonObject.toString());
            }
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    @Override
    public void Update_Tag(ViewDocumentsModel viewDocumentsModel) {
        this.viewDocumentsModel = viewDocumentsModel;
        isUpdateTag = true;
        tags_list.clear();

        JSONArray tagArray = viewDocumentsModel.getTagslist();

        if (tagArray != null) {
            for (int i = 0; i < tagArray.length(); i++) {
                try {
                    JSONObject tagObject = tagArray.getJSONObject(i);

                    String tagType = tagObject.optString("key");
                    String tagName = tagObject.optString("value");

                    DocumentsModel tagModel = new DocumentsModel();
                    tagModel.setTag_type(tagType);
                    tagModel.setTag_name(tagName);

                    tags_list.add(tagModel);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // Refresh UI
            open_add_tags_popup();
        }
    }

    public void disabled_doc(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("downloadDisabled", false);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PATCH, "v3/document/" + id, "Disabled Documents", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    public void enabled_doc(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("downloadDisabled", true);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PATCH, "v3/document/" + id, "Enabled Documents", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    public void encryption_doc(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v3/document/encrypt/" + id, "Encrypt Documents", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    public void decryption_doc(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("get_file", false);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v3/document/decrypt/" + id, "Decrypt Documents", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    public void callDecryptApi(String id, boolean isDownload) {
        this.isDownload = isDownload;
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("docid", id);
            jsonObject.put("download", isDownload);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, Constants.decryptUrl, "Decrypt Doc", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    public void callOtherDocViewApi(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, Constants.base_URL + "v3/document/" + id + "/view", "Other Doc View", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    private void callDisplayDocumentWebservice(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            if (!is_MergePdfClicked) {
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/mergepdf/" + id + "/view", "Display Documents", jsonObject.toString());
            } else {
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/document/" + id + "/view", "Display Documents", jsonObject.toString());
            }
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    public void callfilter_client_webservices() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
//            JSONArray groups1 = new JSONArray();
//            if (!is_MergePdfClicked) {
//                jsonObject.put("showPdfDocs", true);
//            } else {
//                jsonObject.put("showPdfDocs", false);
//            }
            jsonObject.put("showPdfDocs", false);
            if (Objects.equals(CATEGORY_TAG, "client")) {
                jsonObject.put("category", "client");
                jsonObject.put("clients", client_id);
                jsonObject.put("groups", null);
                //When the matter is chosen by the user.....
                if (!matter_id.isEmpty()) {
                    jsonObject.put("matters", matter_id);
                }
            } else if (Objects.equals(CATEGORY_TAG, "firm")) {
                JSONArray groups = new JSONArray();
                for (int k = 0; k < selected_groups_list.size(); k++) {
                    DocumentsModel documentsModel1 = selected_groups_list.get(k);
                    groups.put(documentsModel1.getGroup_id());
                }
                jsonObject.put("category", "firm");
                jsonObject.put("clients", "");
                jsonObject.put("matters", "");
                jsonObject.put("groups", groups);
                Log.d("Group_value_num", groups.toString());
                Log.d("Group_doc_view1", jsonObject.toString());
            }
            if (is_MergePdfClicked)
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "v3/document/filter", "Display FilterDocuments", jsonObject.toString());
            else
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "v3/mergepdf/filter", "Display MergeFilterDocuments", jsonObject.toString());
            Log.d("Group_doc_view1", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    private void callDeleteDocumentWebservice(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            if (is_MergePdfClicked) {
                jsonArray.put(id);
                jsonObject.put("docids", jsonArray);
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v3/document/delete", "Delete Documents", jsonObject.toString());
            } else {
                jsonObject = new JSONObject();
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.DELETE, "v3/mergepdf/" + id, "Delete Merge Documents", jsonObject.toString());
            }
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    private void callclientfirmWebServices() {
        try {
            if (selected_groups_list.isEmpty()) {
                AndroidUtils.showAlert("Please select atleast one group", getActivity());
            } else {
                if (Objects.equals(VIEW_TAG, "Firm"))

                    for (int i = 0; i < docsList.size(); i++) {
                        currentpoistion++;
                        JSONObject jsonObject = new JSONObject();
                        JSONArray clients = new JSONArray();
                        JSONObject clients_jobject = new JSONObject();

//                JSONArray tags = new JSONArray();
                        String docname = "";
                        DocumentsModel documentsModel = docsList.get(i);
                        filename = documentsModel.getName();
                        JSONArray groups = new JSONArray();
                        for (int k = 0; k < selected_groups_list.size(); k++) {
                            DocumentsModel documentsModel1 = selected_groups_list.get(k);
                            groups.put(documentsModel1.getGroup_id());

                        }
                        File new_file = documentsModel.getFile();
                        String doc_type = "pdf";
                        String content_string = new_file.getName().replace(".", "/");
                        String[] content_type = content_string.split("/");
                        if (content_type.length >= 2) {
                            doc_type = content_type[1];
                            docname = content_type[0];
                        }

                        jsonObject.put("category", "firm");
                        jsonObject.put("matters", "");
                        jsonObject.put("groups", groups);
                        jsonObject.put("showPdfDocs", false);

                        if (doc_type.equalsIgnoreCase("apng") || doc_type.equalsIgnoreCase("avif") || doc_type.equalsIgnoreCase("gif") || doc_type.equalsIgnoreCase("jpeg") || doc_type.equalsIgnoreCase("png") || doc_type.equalsIgnoreCase("svg") || doc_type.equalsIgnoreCase("webp") || doc_type.equalsIgnoreCase("jpg")) {
                            jsonObject.put("content_type", "image/" + doc_type);
                        } else {
                            jsonObject.put("content_type", "application/" + doc_type);
                        }
//            AndroidUtils.showAlert(jsonObject.toString(),getContext());
//                        WebServiceHelper.callHttpViewWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "v3/document/filter", "View Document", new_file, jsonObject.toString());
                        rv_display_view_docs.setVisibility(VISIBLE);
                    }
            }
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
            e.fillInStackTrace();
        }
    }

    //checking the check box whether all the list items are checked....
    public void check_select_all(boolean check_status) {
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

    @Override
    public void onCheckedChanged(DocumentsModel documentsModel) {

    }

    private boolean getContType(String docType) {
        switch (docType) {
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document": //docx extension
                return true;
            case "application/msword":
                return true;
            case "application/pdf":
                return true;
            case "image/png":
                return false;
            case "image/gif":
                return false;
            case "image/jpg":
                return false;
            case "image/jpeg":
                return false;
            case "application/vnd.ms-excel":
                return true;
            case "application/vnd.ms-powerpoint":
                return true;
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                return true;
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return true;
            case "text/csv":
                return true;
            case "application/rtf":
                return true;
            case "text/rtf":
                return true;
            default:
                return true;
        }
    }

}