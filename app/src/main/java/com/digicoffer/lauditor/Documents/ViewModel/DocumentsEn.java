package com.digicoffer.lauditor.Documents.ViewModel;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.checkSwitchState;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.hasDocumentChanged;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.renderSelectedTags;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.digicoffer.lauditor.AuditTrails.Adapters.PaginationHelper;
import com.digicoffer.lauditor.Relationships.ClientRelationship;
import com.digicoffer.lauditor.Documents.DocumentsListAdpater.DocumentsListAdapter;
import com.digicoffer.lauditor.Documents.DocumentsListAdpater.GroupsListAdapter;
import com.digicoffer.lauditor.Documents.DocumentsListAdpater.View_documents_adapter;
import com.digicoffer.lauditor.Documents.Models.ClientsModel;
import com.digicoffer.lauditor.Documents.Models.DocumentsModel;
import com.digicoffer.lauditor.Documents.Models.MattersModel;
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel;
import com.digicoffer.lauditor.Matter.ViewModels.Matter;
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
import com.google.android.material.switchmaterial.SwitchMaterial;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class DocumentsEn extends Fragment implements BottomSheetUploadFile.OnPhotoSelectedListner,
        AsyncTaskCompleteListener, DocumentsListAdapter.EventListener,
        View_documents_adapter.Eventlistner, GroupsListAdapter.OnCheckedChangeListener,
        AndroidUtils.OnModuleClickListener {

    Button btn_browse, btn_group_cancel, btn_group_submit, btn_group_view_cancel, btn_group_view_submit;
    boolean isListFiltered = false;
    private ArrayList<String> pendingHighlightIds = null;
    private ArrayList<String> selectedGroupsIds = null;
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
    CommonSpinnerAdapter ClientAdapter, docTypeAdapter, Matteradapter;
    LinearLayout llSelectedTags, ll_select_file;
    TextView upload, view;
    LinearLayout tv_switchUpload, tv_switchView;
    RadioGroup selectionGroups, selectionGroupsView;
    RadioButton rbMatter, rbMatterView, rbClient, rbClientView;
    ViewDocumentsModel viewDocumentsModel_download;
    ViewDocumentsModel viewDocumentsModel;
    ArrayList<ViewDocumentsModel> pageItems = new ArrayList<>();
    boolean isUploadDoc = false;
    TextView tv_no_document, custom_spinner, custom_spinner2, custom_spinner3, custom_spinner4, custom_spinner5,
            custom_spinner_group, tv_select_groups_view, tv_client_doc, tv_firm_doc;
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
    ListView list_client, list_matter, list_client_view, list_matter_view, list_docType_view, list_group;
    ScrollView list_scroll, list_scroll2, list_scroll3, list_scroll4, list_scroll_group;
    ImageView iv_forward_button, iv_backward_button;
    int count_file = 0;
    private NewModel mViewModel;
    RelativeLayout spinnerLayout;
    // Empty state views
    private LinearLayout ll_empty_state;
    private ImageView iv_empty_state;
    private TextView tv_empty_state_title;
    private TextView tv_empty_state_subtitle;
    private boolean ismatter_chosen = false;
    BottomSheetUploadFile bottommSheetUploadDocument;
    private Bitmap mSelectedBitmap;
    ConstraintLayout cl_document;
    TextView tv_added_tags;
    LinearLayout ll_added_tags, ll_matter, ll_category, ll_groups, ll_client_name, ll_view_docs,
            ll_upload_docs, upload_group_layout, view_group_layout;
    LinearLayout ll_matter_view, ll_client_name_view, ll_categories_layout, ll_document_type_view,
            ll_upload_groups, ll_upload_client_group, ll_docType_view;
    LinearLayoutCompat ll_buttons;
    View tl_search_client_view, tl_search_client_views;
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
    RecyclerView rv_documents, rv_display_view_docs, rv_display_upload_groups_docs,
            rv_display_view_groups_docs, rv_upload_groups;
    android.app.Dialog progress_dialog;
    TextView tv_matter, select_documents, tv_select_group_name, tv_docType_view, upload_name, view_name,
            tag_type_name, tag_name, header_name, header_name_group;
    TextView category_name, tv_category, tv_select_upload_group_name,
            tv_select_upload_groups;
    TextView header_name_edit, tag_type_edit, tag_edit;
    TextInputEditText tv_edit_tag_type, tv_edit_tag_name;
    Button btn_upload, btn_add_tags, btn_cancel;
    TextView tv_add_tag, tv_client, tv_firm, tv_enable_download, tv_disable_download,
            tv_enable_encryption, tv_disable_encryption, tv_edit_meta, tv_name, tv_client_view,
            tv_firm_view, tv_deleted_view, tv_name_view, matter_name, category_name_id,
            select_doc_type, tv_document_name, description;
    File file;
    String value = "";
    String entity_id = "";
    String matter_id = "";
    String client_id = "";
    String selectedId = "";
    ImageView img_clear_icon1, img_dropdown_icon1, img_clear_icon2, img_dropdown_icon2,
            img_clear_icon3, img_dropdown_icon3, img_clear_icon4, img_dropdown_icon4, img_clear_icon5, img_dropdown_icon5;
    TextView tv_tag_document_name, tv_select_groups;
    View view1, view2, view3, view4, view5;
    LinearLayout ll_hide_document_details, pageNumberLayout, ll_merge_pdf;
    Spinner sp_matter, sp_client, tv_search_client, sp_matter_view;
    TextInputEditText tv_search_client_views;
    TextInputLayout tl_selected_file;
    private boolean ischecked_matter = true;
    private boolean ischecked_docType = true;
    private boolean ischecked_matter2 = true;
    private boolean ischecked2 = true;
    private static ViewDocumentsModel tempDocModel;

    // Current module variable
    private String currentModule = "matter";

    // ── List/Grid toggle layout ───────────────────────────────────────────
    LinearLayout ll_view_toggle;

    // ── List/Grid toggle image buttons ────────────────────────────────────
    private ImageView iv_list_view;
    private ImageView iv_grid_view;
    private boolean isGridView = false;
    // ─────────────────────────────────────────────────────────────────────

    // ── NEW FILE PICKER ───────────────────────────────────────────────────
    private LinearLayout tv_selected_file_layout;
    private TextView tv_file_count_label;
    // ─────────────────────────────────────────────────────────────────────

    // ── DELETED BANNER ────────────────────────────────────────────────────
    private LinearLayout ll_deleted_banner;
    private TextView tv_banner_deleted_message;
    private TextView tv_banner_days_count;
    private ImageView iv_banner_close;
    private ImageView iv_banner_warning_icon;
    // ─────────────────────────────────────────────────────────────────────

    // ── isTablet helper ──────────────────────────────────────────────────
    private boolean isTabletDevice() {
        if (getContext() == null) return false;
        return getContext().getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }
    // ─────────────────────────────────────────────────────────────────────

    // Set current module from side menu
    public void setCurrentModule(String module) {
        this.currentModule = module;
        loadContentBasedOnModule();
    }

    private void loadContentBasedOnModule() {
        // Clear previous selections
        clearSelections();
        switch (currentModule) {
            case "firm":
                loadFirmDocuments();
                break;
            case "matter":
                loadMatterDocuments();
                break;
            case "client":
                loadClientDocuments();
                break;
            case "delete":
                loadDeletedDocuments();
                break;
            default:
                loadMatterDocuments();
                break;
        }
    }

    private void clearSelections() {
        // Clear client and matter selections
        client_id = "";
        matter_id = "";
        selected_groups_list.clear();
        selected_client_groups_list.clear();

        // Clear spinners
        if (custom_spinner != null) custom_spinner.setText("");
        if (custom_spinner2 != null) custom_spinner2.setText("");
        if (custom_spinner3 != null) custom_spinner3.setText("");
        if (custom_spinner4 != null) custom_spinner4.setText("");
        if (tv_select_groups != null) tv_select_groups.setText("");
        if (tv_select_groups_view != null) tv_select_groups_view.setText("");
        if (tv_select_upload_groups != null) tv_select_upload_groups.setText("");

        // Reset checked states
        ischecked = true;
        ischecked_matter = true;
        ischecked2 = true;
        ischecked_matter2 = true;
        ischecked_docType = true;
        ischecked_group = true;
        ischecked_group_view = true;
        ischeckedClient_group = true;
    }

    private void loadFirmDocuments() {
        DOCUMENT_TYPE_TAG = "firm";
        CATEGORY_TAG = "firm";
        VIEW_TAG = "Firm";
        UPLOAD_TAG = "Firm";
        ismatter_chosen = false;

        // Hide all selection UI elements
        hideAllSelectionUI();

        // Hide deleted banner — not the deleted tab
        hideDeletedBanner();

        // Show upload button
        if (tv_switchUpload != null) {
            tv_switchUpload.setVisibility(VISIBLE);
        }

        // Show toggle for Firm section
        if (ll_view_toggle != null) {
            ll_view_toggle.setVisibility(VISIBLE);
        }

        // Show firm documents
        tv_client_doc.setText(R.string.list_of_firm_documents);
        ll_document_type_view.setVisibility(VISIBLE);
        tl_search_client_views.setVisibility(VISIBLE);
        tl_search_client_view.setVisibility(GONE);
        ll_client_name_view.setVisibility(GONE);
        ll_matter_view.setVisibility(GONE);
        ll_categories_layout.setVisibility(GONE);
        ll_docType_view.setVisibility(GONE);
        mViewModel.setData(requireContext().getString(R.string.document_view));
        callGroupsWebservice();
        callfilter_client_webservices();
    }

    private void loadMatterDocuments() {
        DOCUMENT_TYPE_TAG = "client";
        CATEGORY_TAG = "client";
        VIEW_TAG = "Client";
        UPLOAD_TAG = "Client";
        ismatter_chosen = true;

        // Hide all selection UI elements
        hideAllSelectionUI();

        // Hide deleted banner — not the deleted tab
        hideDeletedBanner();

        // Show upload button
        if (tv_switchUpload != null) {
            tv_switchUpload.setVisibility(VISIBLE);
        }

        // Show toggle for Matter section
        if (ll_view_toggle != null) {
            ll_view_toggle.setVisibility(VISIBLE);
        }

        // Show matter documents
        tv_client_doc.setText(R.string.list_of_matter_documents);
        ll_matter_view.setVisibility(VISIBLE);
        tl_search_client_view.setVisibility(VISIBLE);
        tl_search_client_views.setVisibility(GONE);
        ll_client_name_view.setVisibility(GONE);
        ll_document_type_view.setVisibility(GONE);
        ll_categories_layout.setVisibility(GONE);
        ll_docType_view.setVisibility(GONE);
        mViewModel.setData(requireContext().getString(R.string.document_view));
        callLegalMatter();
    }

    private void loadClientDocuments() {
        DOCUMENT_TYPE_TAG = "client";
        CATEGORY_TAG = "client";
        VIEW_TAG = "Client";
        UPLOAD_TAG = "Client";
        ismatter_chosen = false;

        // Hide all selection UI elements
        hideAllSelectionUI();

        // Hide deleted banner — not the deleted tab
        hideDeletedBanner();

        // Show upload button
        if (tv_switchUpload != null) {
            tv_switchUpload.setVisibility(VISIBLE);
        }

        // Show toggle for Client section
        if (ll_view_toggle != null) {
            ll_view_toggle.setVisibility(VISIBLE);
        }

        // Show client documents
        tv_client_doc.setText(R.string.list_of_client_documents);
        ll_client_name_view.setVisibility(VISIBLE);
        tl_search_client_view.setVisibility(VISIBLE);
        tl_search_client_views.setVisibility(GONE);
        ll_matter_view.setVisibility(GONE);
        ll_document_type_view.setVisibility(GONE);
        ll_categories_layout.setVisibility(GONE);
        ll_docType_view.setVisibility(GONE);
        mViewModel.setData(requireContext().getString(R.string.document_view));
        callClientWebservice();
    }

    private void loadDeletedDocuments() {
        DOCUMENT_TYPE_TAG = "Deleted";
        CATEGORY_TAG = "Deleted";

        // Hide all selection UI elements
        hideAllSelectionUI();

        // HIDE upload button for deleted documents
        if (tv_switchView != null) {
            tv_switchView.setVisibility(GONE);
        }

        // Show toggle for Deleted section (ListView/GridView)
        if (ll_view_toggle != null) {
            ll_view_toggle.setVisibility(VISIBLE);
        }

        // Show deleted banner ONLY in the deleted tab
        showDeletedBanner();

        // Show deleted documents
        if ("solo".equals(Constants.CATEGORY)) {
            tv_client_doc.setText(R.string.deleted_documents);
        } else {
            tv_client_doc.setText(R.string.list_of_documents_pending_approval);
        }

        ll_client_name_view.setVisibility(GONE);
        ll_matter_view.setVisibility(GONE);
        ll_document_type_view.setVisibility(GONE);
        ll_categories_layout.setVisibility(GONE);
        tl_search_client_view.setVisibility(GONE);
        tl_search_client_views.setVisibility(VISIBLE);
        ll_docType_view.setVisibility(VISIBLE);
        mViewModel.setData(requireContext().getString(R.string.deleted_documents));
        callDeletedDocumentWebservice();
    }

    // ── Banner show/hide helpers ──────────────────────────────────────────

    /**
     * Shows the deleted documents warning banner.
     * Called ONLY from loadDeletedDocuments().
     */
    private void showDeletedBanner() {
        if (ll_deleted_banner == null) return;
        if (Constants.CATEGORY.equals("solo") && Constants.ROLE.equals("SU"))
            ll_deleted_banner.setVisibility(VISIBLE);
    }

    /**
     * Hides the deleted documents warning banner.
     * Called from every load method except loadDeletedDocuments().
     */
    private void hideDeletedBanner() {
        if (ll_deleted_banner == null) return;
        ll_deleted_banner.setVisibility(GONE);
    }
    // ─────────────────────────────────────────────────────────────────────

    private void hideAllSelectionUI() {
        // Hide all tabs
        if (tv_client_view != null) tv_client_view.setVisibility(GONE);
        if (tv_firm_view != null) tv_firm_view.setVisibility(GONE);
        if (tv_deleted_view != null) tv_deleted_view.setVisibility(GONE);

        // Hide all radio groups
        if (selectionGroups != null) selectionGroups.setVisibility(GONE);
        if (selectionGroupsView != null) selectionGroupsView.setVisibility(GONE);

        // Hide client/firm tabs in upload section
        if (tv_client != null) tv_client.setVisibility(GONE);
        if (tv_firm != null) tv_firm.setVisibility(GONE);
    }
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.documents_en, container, false);
        try {
            mViewModel = new ViewModelProvider(requireActivity()).get(NewModel.class);
            ll_buttons = v.findViewById(R.id.ll_buttons);

            selectionGroups = v.findViewById(R.id.selectionGroups);
            selectionGroups.setVisibility(GONE);
            rbMatter = v.findViewById(R.id.rbMatter);
            rbClient = v.findViewById(R.id.rbClient);

            tv_no_document = v.findViewById(R.id.tv_no_document);
            tv_no_document.setText(R.string.list_of_documents_not_available);
            selectionGroupsView = v.findViewById(R.id.selectionGroupsView);
            selectionGroupsView.setVisibility(GONE);
            rbMatterView = v.findViewById(R.id.rbMatterView);
            rbClientView = v.findViewById(R.id.rbClientView);

            tv_switchUpload = v.findViewById(R.id.tv_switchUpload);
            tv_switchView = v.findViewById(R.id.tv_switchView);

            tv_client_view = v.findViewById(R.id.tv_client_view);
            tv_client_view.setVisibility(GONE);
            tv_deleted_view = v.findViewById(R.id.tv_deleted_view);
            tv_deleted_view.setVisibility(GONE);
            tv_firm_view = v.findViewById(R.id.tv_firm_view);
            tv_firm_view.setVisibility(GONE);

            tl_search_client_view = v.findViewById(R.id.tl_search_client_view);
            tv_search_client_view = tl_search_client_view.findViewById(R.id.et_Search);
            tv_search_client_view.setHint(R.string.search);

            ll_page_navigaiton = v.findViewById(R.id.ll_page_navigaiton);
            ll_upload_groups = v.findViewById(R.id.ll_upload_groups);
            ll_upload_groups.setVisibility(GONE);
            pageNumberLayout = ll_page_navigaiton.findViewById(R.id.pageNumberLayout);
            iv_forward_button = ll_page_navigaiton.findViewById(R.id.iv_forward_button);
            iv_backward_button = ll_page_navigaiton.findViewById(R.id.iv_backward_button);
            tv_select_upload_group_name = v.findViewById(R.id.tv_select_upload_group_name);
            tv_select_upload_group_name.setText(R.string.select_groups);
            tv_select_upload_groups = v.findViewById(R.id.tv_select_upload_groups);
            rv_upload_groups = v.findViewById(R.id.rv_upload_groups);
            ll_upload_client_group = v.findViewById(R.id.ll_upload_client_group);
            rv_upload_groups.setBackground(getContext().getDrawable(R.drawable.rectangle_light_grey_bg));
            ll_docType_view = v.findViewById(R.id.ll_docType_view);
            // ── List/Grid toggle — bind views ─────────────────────────────
            ll_view_toggle = v.findViewById(R.id.ll_view_toggle);
            iv_list_view = v.findViewById(R.id.iv_list_view);
            iv_grid_view = v.findViewById(R.id.iv_grid_view);

            // Initially set to LIST VIEW mode (default)
            isGridView = false;
            iv_list_view.setBackground(getContext().getDrawable(R.drawable.button_left_green_background));
            iv_grid_view.setBackground(getContext().getDrawable(R.drawable.button_right_grey));
            iv_grid_view.setColorFilter(getContext().getColor(R.color.grey_medium));
            iv_list_view.setColorFilter(getContext().getColor(R.color.white));
            ll_view_toggle.setVisibility(View.VISIBLE);

            // List View button click
            iv_list_view.setOnClickListener(toggleView -> {
                isGridView = false;
                // Update backgrounds
                iv_list_view.setBackground(
                        getContext().getDrawable(R.drawable.button_left_green_background));
                iv_grid_view.setBackground(
                        getContext().getDrawable(R.drawable.button_right_grey));
                // Update icons
                iv_grid_view.setColorFilter(getContext().getColor(R.color.grey_medium));
                iv_list_view.setColorFilter(getContext().getColor(R.color.white));
                switchViewMode(View_documents_adapter.VIEW_TYPE_LIST);
            });

            // Grid View button click
            iv_grid_view.setOnClickListener(toggleView -> {
                isGridView = true;
                // Update backgrounds
                iv_list_view.setBackground(
                        getContext().getDrawable(R.drawable.button_left_grey));
                iv_grid_view.setBackground(
                        getContext().getDrawable(R.drawable.button_right_green_background));
                // Update icons
                iv_grid_view.setColorFilter(getContext().getColor(R.color.white));
                iv_list_view.setColorFilter(getContext().getColor(R.color.grey_medium));
                switchViewMode(View_documents_adapter.VIEW_TYPE_GRID);
            });
            // ─────────────────────────────────────────────────────────────

            // ── DELETED BANNER — bind views ───────────────────────────────
            ll_deleted_banner = v.findViewById(R.id.ll_deleted_banner);
            tv_banner_deleted_message = v.findViewById(R.id.tv_banner_deleted_message);
//            tv_banner_days_count = v.findViewById(R.id.tv_banner_days_count);
            iv_banner_close = v.findViewById(R.id.iv_banner_close);
            iv_banner_warning_icon = v.findViewById(R.id.iv_banner_warning_icon);
            SpannableStringBuilder displayText = new SpannableStringBuilder();

// Message with "30 days" bold
            String message = "Documents will be permanently deleted in 30 days";
            SpannableString spannableMessage = new SpannableString(message);
            int start = message.indexOf("30 days");
            if (start != -1) {
                spannableMessage.setSpan(new StyleSpan(Typeface.BOLD), start, start + "30 days".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            displayText.append(spannableMessage);

            tv_banner_deleted_message.setText(displayText);
            // Ensure banner is hidden on initial load
            if (ll_deleted_banner != null) {
                ll_deleted_banner.setVisibility(GONE);
            }

            // Close / dismiss button click — hides the banner for this session
            if (iv_banner_close != null) {
                iv_banner_close.setOnClickListener(bannerCloseView -> {
                    hideDeletedBanner();
                });
            }
            // ─────────────────────────────────────────────────────────────

            // ── NEW FILE PICKER: bind the new drop-zone views ─────────────
            ll_select_file = v.findViewById(R.id.ll_select_file);
            tv_selected_file_layout = v.findViewById(R.id.tv_selected_file);
            btn_browse = v.findViewById(R.id.btn_browse);

            if (tv_selected_file_layout != null && tv_selected_file_layout.getChildCount() > 1) {
                View child = tv_selected_file_layout.getChildAt(1);
                if (child instanceof TextView) {
                    tv_file_count_label = (TextView) child;
                }
            }
            // ─────────────────────────────────────────────────────────────
            // Spinner layout1
            view1 = v.findViewById(R.id.customLayout1);
            custom_spinner = view1.findViewById(R.id.tv_spinner_view);
            custom_spinner.setHint(R.string.select_client_name);
            img_clear_icon1 = view1.findViewById(R.id.img_clear_icon);
            img_dropdown_icon1 = view1.findViewById(R.id.img_dropdown_icon);
            // Spinner layout2
            view2 = v.findViewById(R.id.customLayout2);
            custom_spinner2 = view2.findViewById(R.id.tv_spinner_view);
            custom_spinner2.setHint(R.string.select_matters);
            img_clear_icon2 = view2.findViewById(R.id.img_clear_icon);
            img_dropdown_icon2 = view2.findViewById(R.id.img_dropdown_icon);
            // Spinner layout3
            view3 = v.findViewById(R.id.customLayout3);
            custom_spinner3 = view3.findViewById(R.id.tv_spinner_view);
            custom_spinner3.setHint(R.string.select_client_name);
            img_clear_icon3 = view3.findViewById(R.id.img_clear_icon);
            img_dropdown_icon3 = view3.findViewById(R.id.img_dropdown_icon);
            // Spinner layout4
            view4 = v.findViewById(R.id.customLayout4);
            custom_spinner4 = view4.findViewById(R.id.tv_spinner_view);
            custom_spinner4.setHint(R.string.select_matters);
            img_clear_icon4 = view4.findViewById(R.id.img_clear_icon);
            img_dropdown_icon4 = view4.findViewById(R.id.img_dropdown_icon);

            view5 = v.findViewById(R.id.ll_docTypeSpinner);
            custom_spinner5 = view5.findViewById(R.id.tv_spinner_view);
            custom_spinner5.setHint(R.string.select_document_type);
            img_clear_icon5 = view5.findViewById(R.id.img_clear_icon);
            img_dropdown_icon5 = view5.findViewById(R.id.img_dropdown_icon);

            list_client = v.findViewById(R.id.list_client);
            list_matter = v.findViewById(R.id.list_matter);
            list_docType_view = v.findViewById(R.id.list_docType_view);
            list_client_view = v.findViewById(R.id.list_client_view);
            list_matter_view = v.findViewById(R.id.list_matter_view);

            tv_add_tag = v.findViewById(R.id.tv_add_tag);
            tv_edit_meta = v.findViewById(R.id.tv_edit_meta);
            btn_upload = v.findViewById(R.id.btn_upload);
            tv_firm = v.findViewById(R.id.tv_firm);
            tv_firm.setVisibility(GONE);
            tv_client = v.findViewById(R.id.tv_client);
            tv_client.setVisibility(GONE);

            tl_search_client_views = v.findViewById(R.id.tl_search_client_views);
            tv_search_client_views = tl_search_client_views.findViewById(R.id.et_Search);
            tv_search_client_views.setHint(R.string.search);
            tv_search_client_view.addTextChangedListener(new Validation(tv_search_client_view));
            tv_search_client_views.addTextChangedListener(new Validation(tv_search_client_views));

            tv_name = v.findViewById(R.id.tv_name);
            ll_category = v.findViewById(R.id.ll_category);
            tv_name_view = v.findViewById(R.id.tv_name_view);
            tv_name_view.setText(R.string.client_name);
            matter_name = v.findViewById(R.id.matter_name);
            matter_name.setText(R.string.matters);
            rv_display_view_docs = v.findViewById(R.id.rv_display_view_docs);

            select_doc_type = v.findViewById(R.id.select_doc_type);
            select_doc_type.setText(R.string.select_groups);
            category_name_id = v.findViewById(R.id.category_name_id);
            category_name_id.setText(R.string.sub_categories);
            ll_groups = v.findViewById(R.id.ll_groups);

            btn_upload.setText(R.string.upload);
            btn_cancel = v.findViewById(R.id.btn_cancel);
            btn_add_tags = v.findViewById(R.id.btn_add_tag);
            btn_add_tags.setText(R.string.add_tag);
            btn_add_tags.setVisibility(GONE);

            btn_browse.setBackground(
                    getActivity().getDrawable(R.drawable.rectangle_blue_bg));
            btn_browse.setText(R.string.browse_small);

            upload_group_layout = v.findViewById(R.id.upload_group_layout);
            rv_display_upload_groups_docs = v.findViewById(R.id.rv_display_upload_groups_docs);
            rv_display_upload_groups_docs.setBackground(
                    getContext().getDrawable(R.drawable.rectangle_light_grey_bg));
            btn_group_cancel = v.findViewById(R.id.btn_group_cancel);
            btn_group_cancel.setVisibility(GONE);
            btn_group_submit = v.findViewById(R.id.btn_group_submit);
            btn_group_submit.setVisibility(GONE);

            view_group_layout = v.findViewById(R.id.view_group_layout);
            rv_display_view_groups_docs = v.findViewById(R.id.rv_display_view_groups_docs);
            rv_display_view_groups_docs.setBackground(
                    getContext().getDrawable(R.drawable.rectangle_light_grey_bg));
            btn_group_view_cancel = v.findViewById(R.id.btn_group_view_cancel);
            btn_group_view_submit = v.findViewById(R.id.btn_group_view_submit);

            tv_add_tag = v.findViewById(R.id.tv_add_tag);
            tv_add_tag.setText(R.string.add_tag);
            tv_edit_meta = v.findViewById(R.id.tv_edit_meta);
            tv_edit_meta.setText(R.string.edit_meta);

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
            tv_select_groups = v.findViewById(R.id.tv_select_groups);
            tv_select_group_name = v.findViewById(R.id.tv_select_group_name);
            tv_select_group_name.setText(R.string.select_groups);
            tv_select_groups.setText("");
            tv_select_groups.setHint(R.string.select_groups);
            tv_client_doc = v.findViewById(R.id.tv_client_doc);
            // Initialize empty state views
            ll_empty_state = v.findViewById(R.id.ll_empty_state);
            iv_empty_state = v.findViewById(R.id.iv_empty_state);
            tv_empty_state_title = v.findViewById(R.id.tv_empty_state_title);
            tv_empty_state_subtitle = v.findViewById(R.id.tv_empty_state_subtitle);
            tv_client_doc.setTextSize(DynamicUtils.twenty);
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
            select_documents = v.findViewById(R.id.select_documents);
            select_documents.setText(R.string.select_document);
            chk_box_layout = v.findViewById(R.id.chk_box_layout);
            chk_box_layout.setAlpha(0.5F);
            chk_select_all = v.findViewById(R.id.chk_select_all);
            chk_select_all.getBackground().setAlpha(50);
            chk_select_all.setEnabled(false);
            rv_documents = v.findViewById(R.id.rv_documents);
            ll_matter_view = v.findViewById(R.id.ll_matter_view);
            ll_document_type_view = v.findViewById(R.id.ll_document_type_view);
            tv_docType_view = v.findViewById(R.id.tv_docType_view);
            tv_docType_view.setText(R.string.select_document_type);
            ll_client_name_view = v.findViewById(R.id.ll_client_name_view);
            ll_categories_layout = v.findViewById(R.id.ll_categories_layout);
            ll_client_name = v.findViewById(R.id.ll_client_name);
            category_name = v.findViewById(R.id.tv_category_name);
            category_name.setHint(R.string.sub_categories);
            ll_hide_document_details = v.findViewById(R.id.ll_hide_doc_details);
            ll_hide_document_details.setVisibility(GONE);

            ll_upload_docs.setVisibility(GONE);
            ll_view_docs.setVisibility(VISIBLE);
            initDocType();
            AndroidUtils.setupModuleView(
                    tv_switchUpload,
                    getString(R.string.document_view),
                    false, false, getContext(), getString(R.string.upload_new),
                    clickedView -> {
                        count_file = 0;
                        clear_upload();
                        custom_spinner.setText("");
                        updateFilePickerLabel("");
                        img_clear_icon1.setVisibility(GONE);
                        img_dropdown_icon1.setVisibility(VISIBLE);
                        view_document();
                    }
            );
            AndroidUtils.setupModuleView(
                    tv_switchView,
                    getString(R.string.upload_new),
                    true, false, getContext(), getString(R.string.document_view),
                    clickedView -> {
                        if (!Constants.is_active) {
                            AndroidUtils.showRenewalPopup(getActivity());
                        } else {
                            upload_documents();
                            client_name = "";
                            client_id = "";
                        }
                    }
            );
            view1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ClientAdapter == null) {
                        AndroidUtils.display_listview(ischecked, list_client);
                        ischecked = !ischecked;
                        return;
                    }
                    boolean isVisible = list_client.getVisibility() == VISIBLE;
                    AndroidUtils.DisplaySpinnerView(list_client, custom_spinner,
                            custom_spinner.getText().toString(),
                            img_dropdown_icon1, img_clear_icon1,
                            !isVisible, ClientAdapter, "Search Client");
                }
            });

            view2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Matteradapter == null) {
                        AndroidUtils.display_listview(ischecked_matter, list_matter);
                        ischecked_matter = !ischecked_matter;
                        return;
                    }
                    boolean isVisible = list_matter.getVisibility() == VISIBLE;
                    AndroidUtils.DisplaySpinnerView(list_matter, custom_spinner2,
                            custom_spinner2.getText().toString(),
                            img_dropdown_icon2, img_clear_icon2,
                            !isVisible, Matteradapter, "Search Matter");
                }
            });

            view3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ClientAdapter == null) {
                        AndroidUtils.display_listview(ischecked2, list_client_view);
                        ischecked2 = !ischecked2;
                        return;
                    }
                    boolean isVisible = list_client_view.getVisibility() == VISIBLE;
                    AndroidUtils.DisplaySpinnerView(list_client_view, custom_spinner3,
                            custom_spinner3.getText().toString(),
                            img_dropdown_icon3, img_clear_icon3,
                            !isVisible, ClientAdapter, "Search Client");
                }
            });

            view4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Matteradapter == null) {
                        AndroidUtils.display_listview(ischecked_matter2, list_matter_view);
                        ischecked_matter2 = !ischecked_matter2;
                        return;
                    }
                    boolean isVisible = list_matter_view.getVisibility() == VISIBLE;
                    AndroidUtils.DisplaySpinnerView(list_matter_view, custom_spinner4,
                            custom_spinner4.getText().toString(),
                            img_dropdown_icon4, img_clear_icon4,
                            !isVisible, Matteradapter, "Search Matter");
                }
            });
            view5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (docTypeAdapter == null) {
                        AndroidUtils.display_listview(ischecked_docType, list_docType_view);
                        ischecked_docType = !ischecked_docType;
                        return;
                    }
                    boolean isVisible = list_docType_view.getVisibility() == VISIBLE;
                    AndroidUtils.DisplaySpinnerView(list_docType_view, custom_spinner5,
                            custom_spinner5.getText().toString(),
                            img_dropdown_icon5, img_clear_icon5,
                            !isVisible, docTypeAdapter, "Search DocType");
                }
            });
            img_clear_icon5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isVisible = list_docType_view.getVisibility() == VISIBLE;
                    if (!custom_spinner5.getText().toString().isEmpty()) {
                        AndroidUtils.DisplaySpinnerView(list_docType_view, custom_spinner5, "",
                                img_dropdown_icon5, img_clear_icon5, isVisible, docTypeAdapter, "Search DocType");
                        ischecked_docType = true;
                        // Reset to show all deleted docs
                        currentPage = 1;
                        setupPagination(view_docs_list);
                        loadViewDocumentsRecyclerview(view_docs_list);
                        UpdatePageButton(currentPage);
                    }
                }
            });
            img_clear_icon1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!custom_spinner.getText().toString().isEmpty()) {
                        client_id = "";
                        matter_id = "";
                        AndroidUtils.DisplaySpinnerView(list_client, custom_spinner, "",
                                img_dropdown_icon1, img_clear_icon1, false, ClientAdapter, "Search Client");
                        ischecked = true;
                        ll_upload_groups.setVisibility(GONE);
                        hideUploadDoc();
                        if (currentModule.equals("client")) {
                            callClientWebservice();
                        }
                    }
                }
            });

            img_clear_icon2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!custom_spinner2.getText().toString().isEmpty()) {
                        matter_id = "";
                        AndroidUtils.DisplaySpinnerView(list_matter, custom_spinner2, "",
                                img_dropdown_icon2, img_clear_icon2, false, Matteradapter, "Search Matter");
                        ischecked_matter = true;
                        callClientGroupsWebservice();
                        hideMatterUpload();
                        if (currentModule.equals("matter")) {
                            callLegalMatter();
                        }
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
                        matter_id = "";
                        AndroidUtils.DisplaySpinnerView(list_client_view, custom_spinner3, "",
                                img_dropdown_icon3, img_clear_icon3, false, ClientAdapter, "Search Client");
                        ismatter_chosen = true;
                        view_docs_list.clear();
                        rv_display_view_docs.removeAllViews();
                        rv_display_view_docs.setVisibility(GONE);
                        ll_page_navigaiton.setVisibility(GONE);
                        tv_no_document.setVisibility(GONE);
                        ischecked2 = true;
                        hideViewDoc();
                        if (currentModule.equals("client")) {
                            callClientWebservice();
                        }
                    }
                }
            });

            img_clear_icon4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!custom_spinner4.getText().toString().isEmpty()) {
                        matter_id = "";
                        AndroidUtils.DisplaySpinnerView(list_matter_view, custom_spinner4, "",
                                img_dropdown_icon4, img_clear_icon4, false, Matteradapter, "Search Matter");
                        ischecked_matter2 = true;
                        ismatter_chosen = true;
                        view_docs_list.clear();
                        rv_display_view_docs.removeAllViews();
                        rv_display_view_docs.setVisibility(GONE);
                        ll_page_navigaiton.setVisibility(GONE);
                        tv_no_document.setVisibility(GONE);
                        hideMatterView();
                        if (currentModule.equals("matter")) {
                            callLegalMatter();
                        }
                    }
                }
            });

            tv_select_groups_view = v.findViewById(R.id.tv_select_groups_view);
            tv_select_groups_view.setText("");
            tv_select_groups_view.setHint(R.string.select_groups);

            upload_group_layout.setVisibility(GONE);
            view_group_layout.setVisibility(GONE);

            tv_select_groups.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ischecked_group) {
                        if (!groupsList.isEmpty()) {
                            upload_group_layout.setVisibility(VISIBLE);
                            GroupsPopup(upload_group_layout, groupsList, selected_groups_list,
                                    rv_display_upload_groups_docs, tv_select_groups);
                        } else {
                            rv_display_upload_groups_docs.setVisibility(GONE);
                        }
                    } else {
                        upload_group_layout.setVisibility(GONE);
                    }
                    ischecked_group = !ischecked_group;
                }
            });

            tv_select_upload_groups.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ischeckedClient_group) {
                        if (client_groups_list.isEmpty()) {
                            callClientGroupsWebservice();
                        } else {
                            GroupsPopup(ll_upload_client_group, client_groups_list,
                                    selected_client_groups_list, rv_upload_groups,
                                    tv_select_upload_groups);
                        }
                    } else {
                        ll_upload_client_group.setVisibility(GONE);
                    }
                    ischeckedClient_group = !ischeckedClient_group;
                }
            });

            tv_select_groups_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ischecked_group_view) {
                        if (!groupsList.isEmpty()) {
                            view_group_layout.setVisibility(VISIBLE);
                            GroupsPopup(view_group_layout, groupsList, selected_groups_list,
                                    rv_display_view_groups_docs, tv_select_groups_view);
                        } else {
                            view_group_layout.setVisibility(GONE);
                        }
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

            ll_select_file.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkPermissionREAD_EXTERNAL_STORAGE(getContext());
                }
            });

            if (tv_selected_file_layout != null) {
                tv_selected_file_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checkPermissionREAD_EXTERNAL_STORAGE(getContext());
                    }
                });
            }

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
                            selected_documents_list.add(documentsModel);
                        }
                    }
                    open_add_tags_popup();
                }
            });

            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    count_file = 0;
                    clear_upload();
                    custom_spinner.setText("");
                    updateFilePickerLabel("");
                    img_clear_icon1.setVisibility(GONE);
                    img_dropdown_icon1.setVisibility(VISIBLE);
                    view_document();
                }
            });

            btn_upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callUploadDocumentWebservice();
                }
            });

        } catch (Exception e) {
            e.fillInStackTrace();
        }

        client_id = "";
        matter_id = "";
        matterlist.clear();
        ll_client_name.setVisibility(GONE);
        list_client.setVisibility(GONE);
        list_client_view.setVisibility(GONE);
        list_matter.setVisibility(GONE);
        list_matter_view.setVisibility(GONE);
//        view_document();
        // Load content based on current module from arguments
        if (getArguments() != null) {
            String doc_type = getArguments().getString("document_type", "matter");
            currentModule = doc_type;
            loadContentBasedOnModule();
        } else if (Constants.isCreate) {
            upload_documents();
        } else {
            loadMatterDocuments();
        }
        return v;
    }


    // ── updateFilePickerLabel ─────────────────────────────────────────────
    private void updateFilePickerLabel(String text) {
        if (tv_file_count_label == null) return;
        if (text == null || text.isEmpty()) {
            tv_file_count_label.setText(
                    "Choose the files from your device or drag\n& drop them here");
        } else {
            tv_file_count_label.setText(text);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    public static void LoadingRecyclerview(RecyclerView recyclerView, Context context, int mode) {
        if (mode == View_documents_adapter.VIEW_TYPE_GRID) {
            recyclerView.setLayoutManager(
                    new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            );
        } else {
            recyclerView.setLayoutManager(
                    new LinearLayoutManager(context)
            );
        }
        recyclerView.setLayoutAnimation(
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_fall_down)
        );
        recyclerView.scheduleLayoutAnimation();
    }

    // ── switchViewMode ────────────────────────────────────────────────────
    private void switchViewMode(int mode) {
        if (adapter1 == null) return;
//        if (mode == View_documents_adapter.VIEW_TYPE_GRID) {
//        }
        LoadingRecyclerview(rv_display_view_docs, getContext(), mode);
        adapter1.setViewType(mode);
        adapter1.notifyDataSetChanged();
    }
    // ─────────────────────────────────────────────────────────────────────

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

        // Check if filtered list is empty
        if (filteredList.isEmpty()) {
            // Show empty state for search results
            if (ll_empty_state != null) {
                ll_empty_state.setVisibility(VISIBLE);
                tv_empty_state_title.setText("No Matching Documents");
                tv_empty_state_subtitle.setText("Try adjusting your search criteria");
            }
            pageItems.clear();
            if (adapter1 != null) {
                adapter1.setData(pageItems);
            }
            if (rv_display_view_docs != null) {
                rv_display_view_docs.setVisibility(GONE);
            }
            if (ll_page_navigaiton != null) {
                ll_page_navigaiton.setVisibility(GONE);
            }
            if (tv_no_document != null) {
                tv_no_document.setVisibility(GONE);
            }
            return;
        }

        // Hide empty state when search has results
        if (ll_empty_state != null) {
            ll_empty_state.setVisibility(GONE);
        }
        if (rv_display_view_docs != null) {
            rv_display_view_docs.setVisibility(VISIBLE);
        }

        int startIndex = PaginationHelper.startIndexForCurrentPage(currentPage, 10);
        int endIndex = PaginationHelper.endIndexForCurrentPage(startIndex, filteredList.size(), 10);
        if (filteredList.isEmpty()) {
            pageItems.clear();
            if (adapter1 != null) {
                adapter1.setData(pageItems);
            }
        } else {
            pageItems = new ArrayList<>(filteredList.subList(startIndex, endIndex));
            if (adapter1 != null) {
                adapter1.setData(pageItems);
            }
        }
        currentPage = 1;
        setupPagination(filteredList);
        UpdatePageButton(currentPage);
    }

    private void setupPagination(ArrayList<ViewDocumentsModel> view_docs_list) {
        pageNumberLayout.removeAllViews();
        pagebuttons.clear();
        int totalPages = PaginationHelper.calculateTotalNoOfPages(view_docs_list.size(), 10);
        Log.d("total_pages", "" + totalPages + ".." + view_docs_list.size());

        if (view_docs_list.isEmpty()) {
            tv_no_document.setVisibility(VISIBLE);
            ll_page_navigaiton.setVisibility(GONE);

            // Show empty state
            if (ll_empty_state != null) {
                ll_empty_state.setVisibility(VISIBLE);
                switch (currentModule) {
                    case "firm":
                        tv_empty_state_title.setText("No Firm Documents");
                        tv_empty_state_subtitle.setText("Upload firm documents to get started");
                        break;
                    case "client":
                        tv_empty_state_title.setText("No Client Documents");
                        tv_empty_state_subtitle.setText("Upload client documents to get started");
                        break;
                    case "matter":
                        tv_empty_state_title.setText("No Matter Documents");
                        tv_empty_state_subtitle.setText("Upload matter documents to get started");
                        break;
                    case "delete":
                        if ("solo".equals(Constants.CATEGORY)) {
                            tv_empty_state_title.setText("No Deleted Documents");
                            tv_empty_state_subtitle.setText("Deleted documents will appear here");
                        } else {
                            tv_empty_state_title.setText("No Documents Pending Approval");
                            tv_empty_state_subtitle.setText("Documents pending approval will appear here");
                        }
                        break;
                    default:
                        tv_empty_state_title.setText("No Documents Found");
                        tv_empty_state_subtitle.setText("Upload documents to get started");
                        break;
                }
            }

            // IMPORTANT: Hide RecyclerView when no documents
            if (rv_display_view_docs != null) {
                rv_display_view_docs.setVisibility(GONE);
            }

            // Clear adapter data
            if (adapter1 != null) {
                adapter1.setData(new ArrayList<>());
            }

        } else {
            tv_no_document.setVisibility(GONE);
            ll_page_navigaiton.setVisibility(VISIBLE);

            // Hide empty state when there are documents
            if (ll_empty_state != null) {
                ll_empty_state.setVisibility(GONE);
            }

            if (rv_display_view_docs != null) {
                rv_display_view_docs.setVisibility(VISIBLE);
            }
            for (int i = 1; i <= totalPages; i++) {
                View view_opponents = LayoutInflater.from(getContext()).inflate(
                        R.layout.page_number_layout, null);
                Button pageButton = view_opponents.findViewById(R.id.page_number_button);
                pageButton.setText(String.valueOf(i));
                final int pageNumber = i;
                pageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentPage = pageNumber;
                        loadViewDocumentsRecyclerview(view_docs_list);
                        UpdatePageButton(currentPage);
                    }
                });
                pageNumberLayout.addView(view_opponents);
                pagebuttons.add(pageButton);
            }
            iv_forward_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentPage = currentPage + 1;
                    int startIndex = PaginationHelper.startIndexForCurrentPage(currentPage, 10);
                    int endIndex = PaginationHelper.endIndexForCurrentPage(
                            startIndex, view_docs_list.size(), 10);
                    if (endIndex > end_temp) {
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
                        loadViewDocumentsRecyclerview(view_docs_list);
                        UpdatePageButton(currentPage);
                    } else {
                        currentPage = 1;
                    }
                }
            });
        }
    }


    private void UpdatePageButton(int currentPage) {
        for (int i = 0; i < pagebuttons.size(); i++) {
            TextView pageButton = pagebuttons.get(i);
            if (currentPage >= 0) {
                if (i + 1 == currentPage) {
                    pageButton.setTextColor(getActivity().getColor(R.color.white));
                    pageButton.setBackground(
                            getActivity().getDrawable(R.drawable.blue_gradient_card));
                } else {
                    pageButton.setTextColor(getActivity().getColor(R.color.blue));
                    pageButton.setBackground(
                            requireActivity().getDrawable(R.drawable.background_transparent));
                }
            }
        }
    }

    private void scrollToHighlightedDocument(ArrayList<ViewDocumentsModel> allDocs) {
        if (pendingHighlightIds == null || pendingHighlightIds.isEmpty()) return;
        int highlightPosition = -1;
        for (int i = 0; i < allDocs.size(); i++) {
            if (pendingHighlightIds.contains(allDocs.get(i).getId())) {
                highlightPosition = i;
                break;
            }
        }
        if (highlightPosition != -1) {
            int itemsPerPage = 10;
            int targetPage = (highlightPosition / itemsPerPage) + 1;
            if (targetPage != currentPage) {
                currentPage = targetPage;
                int startIndex = PaginationHelper.startIndexForCurrentPage(currentPage, itemsPerPage);
                int endIndex = PaginationHelper.endIndexForCurrentPage(
                        startIndex, allDocs.size(), itemsPerPage);
                pageItems = new ArrayList<>(allDocs.subList(startIndex, endIndex));
                adapter1.setData(pageItems);
                UpdatePageButton(currentPage);
            }
            final int positionInPage = highlightPosition % itemsPerPage;
//            rv_display_view_docs.postDelayed(() -> {
//                rv_display_view_docs.smoothScrollToPosition(positionInPage);
//                rv_display_view_docs.postDelayed(() -> {
//                    RecyclerView.ViewHolder holder =
//                            rv_display_view_docs.findViewHolderForAdapterPosition(positionInPage);
//                    if (holder != null && holder.itemView != null) {
//                        holder.itemView.animate()
//                                .scaleX(1.05f).scaleY(1.05f).setDuration(200)
//                                .withEndAction(() -> holder.itemView.animate()
//                                        .scaleX(1.0f).scaleY(1.0f).setDuration(200).start())
//                                .start();
//                    }
//                }, 600);
//            }, 300);
        }
    }

    private void view_document() {
        rv_documents.setVisibility(GONE);
        ll_buttons.setVisibility(GONE);
        tv_no_document.setVisibility(GONE);

        ll_view_docs.setVisibility(VISIBLE);
        rv_display_view_docs.setVisibility(GONE);
        rv_display_view_docs.removeAllViews();
        view_group_layout.setVisibility(GONE);
        ll_upload_docs.setVisibility(GONE);
        rv_display_upload_groups_docs.removeAllViews();
        clearClients();
        ismatter_chosen = false;
        isUploadDoc = false;

        // Show toggle in View Documents section for all modules including Deleted
        if (ll_view_toggle != null) {
            ll_view_toggle.setVisibility(VISIBLE);
        }

        if (Constants.isFromNotification) {
            handleNotificationNavigation();
        } else if (getArguments() != null) {
            String doc_type = getArguments().getString("document_type", "matter");
            currentModule = doc_type;
            loadContentBasedOnModule();
        } else {
            loadMatterDocuments();
        }
        mViewModel.setData(requireContext().getString(R.string.document_view));
    }

    private void callViewDocumentWebservice() {
        try {
            JSONObject jsonObject = new JSONObject();
            progress_dialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/documents/" + DOCUMENT_TYPE_TAG, "VIEW_DOCUMENT", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    private void callDeletedDocumentWebservice() {
        try {
            JSONObject jsonObject = new JSONObject();
            progress_dialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "docs/deleted/list", "DELETED_DOCUMENT", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    private void hideViewDoc() {
        hideClientView();
        hideMatterView();
        ll_page_navigaiton.setVisibility(GONE);
        tv_no_document.setVisibility(GONE);
    }

    private void hideClientView() {
        custom_spinner3.setText("");
        img_dropdown_icon3.setVisibility(VISIBLE);
        img_clear_icon3.setVisibility(GONE);
        list_client_view.setVisibility(GONE);
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
    }

    private void hideMatterUpload() {
        custom_spinner2.setText("");
        img_dropdown_icon2.setVisibility(VISIBLE);
        img_clear_icon2.setVisibility(GONE);
        list_matter.setVisibility(GONE);
    }

    private void upload_documents() {
        isUploadDoc = true;
        isUpdateTag = false;
        clearClients();

        ll_upload_docs.setVisibility(VISIBLE);
        ll_view_docs.setVisibility(GONE);
        rv_documents.removeAllViews();
        rv_display_upload_groups_docs.removeAllViews();
        hideUploadDoc();

        // Hide the list/grid toggle in Upload Documents section
        if (ll_view_toggle != null) {
            ll_view_toggle.setVisibility(GONE);
        }

        // Hide deleted banner when switching to upload mode
        hideDeletedBanner();

        mViewModel.setData(getContext().getString(R.string.upload_new));
        count_file = 0;
        tv_select_groups.setText("");
        tv_select_groups.setHint(R.string.select_groups);

        // Reset drop-zone to default state
        updateFilePickerLabel("");

        if (currentModule.equals("firm")) {
            UPLOAD_TAG = "Firm";
            ll_groups.setVisibility(VISIBLE);
            ll_category.setVisibility(GONE);
            ll_client_name.setVisibility(GONE);
            ll_matter.setVisibility(GONE);
            selected_groups_list.clear();
            tv_select_groups.setText("");
            tv_select_groups.setHint(R.string.select_groups);
            groupsList.clear();
            callGroupsWebservice();
        } else if (currentModule.equals("client")) {
            UPLOAD_TAG = "Client";
            ll_category.setVisibility(GONE);
            ll_groups.setVisibility(GONE);
            ll_upload_groups.setVisibility(GONE);
            selected_client_groups_list.clear();
            client_groups_list.clear();
            rv_display_upload_groups_docs.removeAllViews();
            client_id = "";
            matter_id = "";
            matterlist.clear();
            ll_client_name.setVisibility(VISIBLE);
            ll_matter.setVisibility(GONE);
            callClientWebservice();
        } else {
            // matter documents
            UPLOAD_TAG = "Client";
            ll_category.setVisibility(GONE);
            ll_groups.setVisibility(GONE);
            ll_upload_groups.setVisibility(GONE);
            selected_client_groups_list.clear();
            client_groups_list.clear();
            rv_display_upload_groups_docs.removeAllViews();
            client_id = "";
            matter_id = "";
            matterlist.clear();
            ll_client_name.setVisibility(GONE);
            ll_matter.setVisibility(VISIBLE);
            callLegalMatter();
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
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.PUT,
                    "v3/documents/groupslist", "Client Groups", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    private void callGroupsWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/groups", "Groups", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    private void open_add_tags_popup() {
        if (!selected_documents_list.isEmpty()) {
            tags_list.clear();  // Clear first

            // Load existing tags from the selected document
            com.digicoffer.lauditor.Documents.Models.DocumentsModel selectedDoc = selected_documents_list.get(0);
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
        AndroidUtils.openAddTagsPopup(
                getContext(), getActivity(), requireActivity().getLayoutInflater(),
                cl_document, isUpdateTag, tags_list, selected_documents_list,
                ll_added_tags_ref, tv_tag_type_ref, tv_tag_name_ref,
                isedit_ref, edit_position_ref,
                (tagsList, dialog) -> {
                    if (!isUpdateTag) {
                        for (DocumentsModel documentModel : selected_documents_list) {
                            JSONObject combinedTags = new JSONObject();
                            JSONObject existingTags = documentModel.getTags();
                            if (existingTags != null) {
                                Iterator<String> keys = existingTags.keys();
                                while (keys.hasNext()) {
                                    String key = keys.next();
                                    try {
                                        combinedTags.put(key, existingTags.get(key));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            for (DocumentsModel tagModel : tagsList) {
                                try {
                                    combinedTags.put(tagModel.getTag_type(), tagModel.getTag_name());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            documentModel.setTags(combinedTags);
                        }
                        for (int i = 0; i < docsList.size(); i++) {
                            for (DocumentsModel selectedDoc : selected_documents_list) {
                                if (docsList.get(i) == selectedDoc) {
                                    docsList.get(i).setTags(selectedDoc.getTags());
                                    docsList.get(i).setChecked(false);
                                }
                            }
                        }
                        is_clicked_add = true;
                        Hide_Add_EditMeta();
                        dialog.dismiss();
                        if (llSelectedTags != null && !selected_documents_list.isEmpty()) {
                            DocumentsModel editingDoc = selected_documents_list.get(0);
                            ArrayList<DocumentsModel> mergedTagList = new ArrayList<>();
                            JSONObject json = editingDoc.getTags();
                            if (json != null) {
                                Iterator<String> keys = json.keys();
                                while (keys.hasNext()) {
                                    String key = keys.next();
                                    try {
                                        DocumentsModel tag = new DocumentsModel();
                                        tag.setTag_type(key);
                                        tag.setTag_name(json.getString(key));
                                        mergedTagList.add(tag);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            renderSelectedTags(
                                    getContext(),
                                    llSelectedTags,
                                    mergedTagList,
                                    editingDoc,
                                    cl_document,
                                    getActivity()
                            );
                        }
                    } else {
                        callUpdateTag();
                        is_clicked_add = true;
                        dialog.dismiss();
                    }
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

    private void callUpdateTag() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            JSONObject combinedTags = new JSONObject();
            for (DocumentsModel tagModel : tags_list) {
                try {
                    combinedTags.put(tagModel.getTag_type(), tagModel.getTag_name());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            viewDocumentsModel.setTag(combinedTags);
            jsonObject.put("name", viewDocumentsModel.getName());
            jsonObject.put("tags", combinedTags);
            if (!is_MergePdfClicked) {
                WebServiceHelper.callHttpWebService(this, getContext(),
                        WebServiceHelper.RestMethodType.PUT,
                        "v3/mergepdf/" + viewDocumentsModel.getId() + "/tags",
                        "Update Tags", jsonObject.toString());
            } else {
                WebServiceHelper.callHttpWebService(this, getContext(),
                        WebServiceHelper.RestMethodType.PUT,
                        "v3/document/tags/" + viewDocumentsModel.getId(),
                        "Update Tags", jsonObject.toString());
            }
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            e.printStackTrace();
        }
    }

    private void add_tags_listing(Button btn_save_tag) {
        ll_added_tags.removeAllViews();
        if (!Objects.requireNonNull(tv_tag_type.getText()).toString().isEmpty()
                && !Objects.requireNonNull(tv_tag_name.getText()).toString().isEmpty()) {
            DocumentsModel documentsModel = new DocumentsModel();
            documentsModel.setTag_type(Objects.requireNonNull(tv_tag_type.getText()).toString());
            documentsModel.setTag_name(Objects.requireNonNull(tv_tag_name.getText()).toString());
            tags_list.add(documentsModel);
        }
        for (int i = 0; i < tags_list.size(); i++) {
            View view_added_tags = LayoutInflater.from(getContext()).inflate(
                    R.layout.displays_documents_list, null);
            tv_tag_document_name = view_added_tags.findViewById(R.id.tv_document_name);
            LinearLayout ll_tags = view_added_tags.findViewById(R.id.ll_tags);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
                    int position = (int) view.getTag();
                    ll_added_tags.removeViewAt(position);
                    btn_save_tag.setEnabled(true);
                    btn_save_tag.setAlpha(1.0f);
                    DocumentsModel documentsModel1 = tags_list.get(position);
                    documentsModel1.setTag_name("");
                    documentsModel1.setTag_type("");
                    documentsModel1.setChecked(false);
                    tags_list.set(position, documentsModel1);
                    tags_list.remove(position);
                    for (int j = 0; j < ll_added_tags.getChildCount(); j++) {
                        ImageView iv_remove = ll_added_tags.getChildAt(j).findViewById(R.id.iv_cancel);
                        if (iv_remove != null) {
                            iv_remove.setTag(j);
                        }
                    }
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
                        view1 = ll_added_tags.getChildAt(position);
                        DocumentsModel documentsModel1 = tags_list.get(position);
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

    @SuppressLint("MissingInflatedId")
    private void edit_tags(String tag_type, String tag_name, int position,
                           View view_tag, TextView tv_tag_document_name) {
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
        btn_cancel.setOnClickListener(view -> dialog.dismiss());
        btn_save_edited_tag.setOnClickListener(view ->
                save_edited_tags(tv_edit_tag_type.getText().toString(),
                        tv_edit_tag_name.getText().toString()));
        iv_close_edit_tags.setOnClickListener(view -> dialog.dismiss());
        dialog.setOnDismissListener(d -> cl_document.setAlpha(1.0f));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(view_edit_tags);
        dialog.show();
    }

    private void save_edited_tags(String tag_type, String tag_name) {
        try {
            if (edit_position < 0 || edit_position >= tags_list.size()) {
                AndroidUtils.showAlert("Invalid position for editing tag.", getActivity());
                return;
            }
            for (int i = 0; i < tags_list.size(); i++) {
                if (i == edit_position) continue;
                if (tags_list.get(i).getTag_type().equalsIgnoreCase(tag_type.trim())) {
                    AndroidUtils.showAlert(
                            "This tag type already exists. Please use a different tag type.",
                            getActivity());
                    return;
                }
            }
            DocumentsModel documentsModel = tags_list.get(edit_position);
            documentsModel.setTag_type(tag_type);
            documentsModel.setTag_name(tag_name);
            tags_list.set(edit_position, documentsModel);
            View view_to_update = ll_added_tags.getChildAt(edit_position);
            if (view_to_update != null) {
                TextView tv_edit_tag_document_name =
                        view_to_update.findViewById(R.id.tv_document_name);
                tv_edit_tag_document_name.setText(tag_type + " - " + tag_name);
            }
            if (tv_tag_name != null && tv_tag_type != null) {
                tv_tag_name.setText("");
                tv_tag_type.setText("");
            }
        } catch (Exception e) {
            Log.e("save_edited_tags", "Error saving edited tag", e);
            AndroidUtils.showAlert("An error occurred while saving the tag: " + e.getMessage(),
                    getActivity());
        }
    }

    private void loadClientSelection(Boolean isMatter) {
        matterlist.clear();
        if (isMatter) {
            callLegalMatter();
            ll_client_name.setVisibility(GONE);
        } else {
            client_name = "";
            callClientWebservice();
//            AndroidUtils.DisplaySpinnerView(list_client, custom_spinner, client_name,
//                    img_dropdown_icon1, img_clear_icon1, false);
            hideMatterUpload();
            ll_matter_view.setVisibility(GONE);
            ll_matter.setVisibility(GONE);
        }
    }

    private void clearClients() {
        count_file = 0;
        updateFilePickerLabel("");
        selected_documents_list.clear();
        clearListData();
    }

    private void clearListData() {
        ll_hide_document_details.setVisibility(GONE);
        selected_groups_list.clear();
        selected_documents_list.clear();
        langList.clear();
        tags_list.clear();
        docsList.clear();
        groupsList.clear();
    }

    private void clear_upload() {
        ll_hide_document_details.setVisibility(GONE);
        updateFilePickerLabel("");
        selected_groups_list.clear();
        selected_documents_list.clear();
        langList.clear();
        tags_list.clear();
        docsList.clear();
        groupsList.clear();
        rv_documents.setVisibility(GONE);
        ll_buttons.setVisibility(GONE);
        tv_select_groups_view.setText("");
        tv_select_groups_view.setHint(R.string.select_groups);
    }

    private void callUploadDocumentWebservice() {
        try {
            if (Objects.equals(UPLOAD_TAG, "Client") && matter_id.isEmpty()
                    && currentModule.equals("matter")) {
                AndroidUtils.showAlert("Please select a Matter", getActivity());
            } else if (Objects.equals(UPLOAD_TAG, "Client") && client_id.isEmpty()
                    && currentModule.equals("client")) {
                AndroidUtils.showAlert("Please select a Client", getActivity());
            } else if (Objects.equals(UPLOAD_TAG, "Firm") && selected_groups_list.isEmpty()) {
                AndroidUtils.showAlert("Please select a Group", getActivity());
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
                            jsonObject.put("custom_encrypt", docsList.get(i).getIsencrypted());
                            jsonObject.put("downloadDisabled", docsList.get(i).isIsenabled());
                            if (docsList.get(i).getTags() == null) {
                                jsonObject.put("tags", "");
                            } else {
                                jsonObject.put("tags", docsList.get(i).getTags());
                            }
                            if (doc_type.equalsIgnoreCase("apng") || doc_type.equalsIgnoreCase("avif")
                                    || doc_type.equalsIgnoreCase("gif")
                                    || doc_type.equalsIgnoreCase("jpeg")
                                    || doc_type.equalsIgnoreCase("png")
                                    || doc_type.equalsIgnoreCase("svg")
                                    || doc_type.equalsIgnoreCase("webp")
                                    || doc_type.equalsIgnoreCase("jpg")) {
                                jsonObject.put("content_type", "image/" + doc_type);
                            } else {
                                jsonObject.put("content_type", "application/" + doc_type);
                            }
                            WebServiceHelper.callHttpUploadWebService(this, getContext(),
                                    WebServiceHelper.RestMethodType.POST,
                                    "v3/document/upload", "Upload Document",
                                    new_file, jsonObject.toString());
                        }
                    } else {
                        for (int i = 0; i < docsList.size(); i++) {
                            currentpoistion++;
                            JSONObject jsonObject = new JSONObject();
                            JSONArray clients = new JSONArray();
                            JSONObject clients_jobject = new JSONObject();
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
                            jsonObject.put("downloadDisabled", docsList.get(i).isIsenabled());
                            if (docsList.get(i).getTags() == null) {
                                jsonObject.put("tags", "");
                            } else {
                                jsonObject.put("tags", docsList.get(i).getTags());
                            }
                            if (doc_type.equalsIgnoreCase("apng") || doc_type.equalsIgnoreCase("avif")
                                    || doc_type.equalsIgnoreCase("gif")
                                    || doc_type.equalsIgnoreCase("jpeg")
                                    || doc_type.equalsIgnoreCase("png")
                                    || doc_type.equalsIgnoreCase("svg")
                                    || doc_type.equalsIgnoreCase("webp")
                                    || doc_type.equalsIgnoreCase("jpg")) {
                                jsonObject.put("content_type", "image/" + doc_type);
                            } else {
                                jsonObject.put("content_type", "application/" + doc_type);
                            }
                            WebServiceHelper.callHttpUploadWebService(this, getContext(),
                                    WebServiceHelper.RestMethodType.POST,
                                    "v3/document/upload", "Upload Document",
                                    new_file, jsonObject.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            e.fillInStackTrace();
        }
    }

    private void callClientWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/client/all/list", "Clients List", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    private void callCorpClientWebservice() {
        try {
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/corporate/list", "Corp Clients List", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }


    // No launcher needed for permissions at all
// Just call BottomSheetUploadfile directly

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ : No permission needed, just open bottom sheet
            // System file picker inside BottomSheet handles everything
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

    private void BottomSheetUploadfile() {
        cl_document.setAlpha(0.5f);
        Constants.isDocEditor = false;
        bottommSheetUploadDocument = new BottomSheetUploadFile(cl_document);
        bottommSheetUploadDocument.show(getParentFragmentManager(), "");
        bottommSheetUploadDocument.setTargetFragment(this, 1);
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
            Hide_Add_EditMeta();
            is_clicked_edit = true;
            is_clicked_add = true;
            count_file++;
            if (count_file > 0) {
                updateFilePickerLabel(count_file + " file" + (count_file > 1 ? "s" : "") + " selected");
            } else {
                updateFilePickerLabel("");
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
            if (count_file > 0) {
                updateFilePickerLabel(count_file + " file" + (count_file > 1 ? "s" : "") + " selected");
            } else {
                updateFilePickerLabel("");
            }
            load_documents(docsList, file_name, file);
        }
        cl_document.setAlpha(1.0f);
        bottommSheetUploadDocument.dismiss();
    }

    public void remove_file(boolean ischecked) {
        if (ischecked) {
            count_file--;
            if (count_file < 0) {
                count_file = 0;
            }
            if (count_file > 0) {
                updateFilePickerLabel(count_file + " file" + (count_file > 1 ? "s" : "") + " selected");
            } else {
                updateFilePickerLabel("");
                ll_hide_document_details.setVisibility(GONE);
            }
        }
    }

    private void load_documents(ArrayList<DocumentsModel> docsList, String file_name, File
            file) {
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
            DisableDownloadBackground();
            tv_enable_encryption.setBackgroundDrawable(
                    getContext().getResources().getDrawable(R.drawable.button_left_background));
            tv_enable_encryption.setTextColor(getContext().getColor(R.color.black));
            tv_disable_encryption.setTextColor(getContext().getColor(R.color.white));
            tv_disable_encryption.setBackgroundDrawable(
                    getContext().getResources().getDrawable(
                            R.drawable.button_right_green_background));
        } else if (docsList.isEmpty()) {
            ll_hide_document_details.setVisibility(GONE);
        }
        EditMeta();
    }

    private void loadRecyclerview(String tag, String subtag) {
        rv_documents.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DocumentsListAdapter(docsList, tag, subtag, this, this);
        rv_documents.setAdapter(adapter);
        count_file = docsList.size();
        AndroidUtils.LoadList(rv_documents, getContext(), docsList.size(), false);
        if (count_file == 0) {
            rv_documents.setVisibility(GONE);
            ll_buttons.setVisibility(GONE);
            updateFilePickerLabel("");
            ll_hide_document_details.setVisibility(GONE);
        } else {
            rv_documents.setVisibility(VISIBLE);
            ll_buttons.setVisibility(VISIBLE);
            updateFilePickerLabel(count_file + " file" + (count_file > 1 ? "s" : "") + " selected");
        }
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
        tv_enable_encryption.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_enable_encryption.setTextColor(getContext().getColor(R.color.black));
        tv_disable_encryption.setTextColor(getContext().getColor(R.color.white));
        tv_disable_encryption.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_right_green_background));
    }

    private void EnableEncryptionBackground() {
        ENCRYPTION_TAG = true;
        tv_enable_encryption.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        tv_enable_encryption.setTextColor(getContext().getColor(R.color.white));
        tv_disable_encryption.setTextColor(getContext().getColor(R.color.black));
        tv_disable_encryption.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_right_background));
        adapter.EncryptAllorDecryptAll(true);
        String tag = "en_encrption";
        loadRecyclerview(tag, subtag);
    }

    private void DisableDownloadBackground() {
        DOWNLOAD_TAG = false;
        tv_enable_download.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_enable_download.setTextColor(getContext().getColor(R.color.black));
        tv_disable_download.setTextColor(getContext().getColor(R.color.white));
        tv_disable_download.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_right_green_background));
        String tag = "disable_download";
        loadRecyclerview(tag, subtag);
    }

    private void EnableDownloadBackground() {
        DOWNLOAD_TAG = true;
        tv_disable_download.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_right_background));
        tv_enable_download.setTextColor(getContext().getColor(R.color.white));
        tv_disable_download.setTextColor(getContext().getColor(R.color.black));
        tv_enable_download.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_left_green_background));
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
        tv_edit_meta.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_right_background));
        tv_add_tag.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        String tag = "add_tag";
        Constants.DocTagType = tag;
        for (int i = 0; i < docsList.size(); i++) {
            docsList.get(i).setChecked(false);
        }
        loadRecyclerview(tag, subtag);
    }

    private void Hide_Add_EditMeta() {
        chk_box_layout.setAlpha(0.5F);
        chk_select_all.setEnabled(false);
        chk_select_all.setChecked(false);
        isselect_all_checked = true;
        btn_add_tags.setVisibility(GONE);
        btn_upload.setVisibility(VISIBLE);
        tv_edit_meta.setTextColor(getContext().getResources().getColor(R.color.black));
        tv_add_tag.setTextColor(getContext().getResources().getColor(R.color.black));
        tv_add_tag.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_edit_meta.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_right_background));
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
        btn_upload.setVisibility(VISIBLE);
        btn_add_tags.setVisibility(GONE);
        tv_edit_meta.setTextColor(getContext().getResources().getColor(R.color.white));
        tv_add_tag.setTextColor(getContext().getResources().getColor(R.color.black));
        tv_add_tag.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_edit_meta.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_right_green_background));
        String tag = "edit_meta";
        Constants.DocTagType = tag;
        for (int i = 0; i < docsList.size(); i++) {
            docsList.get(i).setChecked(false);
        }
        loadRecyclerview(tag, subtag);
    }

    public static File getFile(Context context, Uri uri) throws IOException {
        File destinationFilename = new File(context.getFilesDir().getPath()
                + File.separatorChar + queryName(context, uri));
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
        Cursor returnCursor = context.getContentResolver().query(
                uri, null, null, null, null);
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
        File filesDir = getContext().getFilesDir();
        File imageFile = new File(filesDir, "bitmap" + ".jpg");
        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
            file = imageFile;
            count_file++;
            updateFilePickerLabel(count_file + " file" + (count_file > 1 ? "s" : "") + " selected");
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
            Cursor cursor = getContext().getContentResolver().query(
                    selectedImage, filePathColumn, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            file = new File(picturePath);
            filename = file.getName();
            count_file++;
            updateFilePickerLabel(count_file + " file" + (count_file > 1 ? "s" : "") + " selected");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case 123:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    AndroidUtils.showAlert("GET_ACCOUNTS Denied", getActivity());
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                if (httpResult.getRequestType().equals("View Encrypted Doc")) {
                    if (progress_dialog != null && progress_dialog.isShowing())
                        AndroidUtils.dismiss_dialog(progress_dialog);
                    String image_data = httpResult.getResponseContent();
                    encrypted_doc(image_data);
                } else if (httpResult.getRequestType().equals("Upload Document")) {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    completedUploads++;
                    boolean isError = result.optBoolean("error");
                    String msg = result.optString("msg");
                    if (completedUploads == totalUploads && !isAlertShown) {
                        isAlertShown = true;
                        if (progress_dialog != null && progress_dialog.isShowing())
                            AndroidUtils.dismiss_dialog(progress_dialog);
                        if (!isError) {
                            AndroidUtils.showAlert(msg, getActivity(), "");
                            rv_documents.removeAllViews();
                            view_document();
                            clearListData();
                        } else {
                            AndroidUtils.showAlert(msg, getActivity());
                        }
                    }
                } else {
                    if (progress_dialog != null && progress_dialog.isShowing())
                        AndroidUtils.dismiss_dialog(progress_dialog);
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    if (httpResult.getRequestType().equals("Clients List")) {
                        JSONObject data = result.getJSONObject("data");
                        try {
                            loadClients(data);
                            callCorpClientWebservice();
                        } catch (Exception e) {
                            e.fillInStackTrace();
                        }
                    } else if (httpResult.getRequestType().equals("Update Documents")) {
                        if (!result.optBoolean("error")) {
                            Dialog.dismiss();
                            rv_display_view_docs.removeAllViews();
                            callfilter_client_webservices();
                            String msg = result.optString("msg");
                            AndroidUtils.showAlert(msg, getActivity(), "Success");
                        } else {
                            String msg = result.optString("msg");
                            AndroidUtils.showAlert(msg, getActivity());
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
                        } else {
                            String msg = result.getString("msg");
                            AndroidUtils.showAlert_docs("Success!", msg, getActivity());
                            callDeletedDocumentWebservice();
                        }
                    } else if (httpResult.getRequestType().equals("Legal Matter")) {
                        JSONArray matters = result.getJSONArray("matters");
                        loadMatters(matters);
                    } else if (httpResult.getRequestType().equals("Groups")) {
                        JSONArray data = result.optJSONArray("data");
                        loadGroupsData(data, groupsList, false);
                    } else if (httpResult.getRequestType().equals("Client Groups")) {
                        JSONArray data = result.optJSONArray("data");
                        loadGroupsData(data, client_groups_list, true);
                    } else if (httpResult.getRequestType().equals("Display FilterDocuments")) {
                        JSONArray data = result.optJSONArray("data");
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
                            JSONArray docs = result.getJSONArray("documents");
                            load_view_doc(docs);
                            Log.d("TAG_view", docs.toString());
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
                            String url = "";
                            if (!is_MergePdfClicked) {
                                url = result.optString("url");
                            } else {
                                JSONObject data = result.optJSONObject("data");
                                assert data != null;
                                url = data.optString("url");
                            }
                            AndroidUtils.showAlert(
                                    "You have successfully downloaded the document.",
                                    getActivity(), "Success");
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
                            callfilter_client_webservices();
                            msg = result.optString("msg");
                            AndroidUtils.showAlert(msg, getActivity());
                        }
                    } else if (httpResult.getRequestType().equals("Delete Merge Documents")) {
                        boolean iserror = result.getBoolean("error");
                        String msg = result.getString("msg");
                        if (iserror) {
                            AndroidUtils.showAlert(msg, getActivity());
                        } else {
                            AndroidUtils.showAlert(msg, getActivity());
                            rv_display_view_docs.removeAllViews();
                            callfilter_client_webservices();
                        }
                    } else if (httpResult.getRequestType().equals("Decrypt Documents")) {
                        boolean iserror = result.getBoolean("error");
                        String msg = "";
                        if (iserror) {
                            msg = result.getString("msg");
                            AndroidUtils.showAlert(msg, getActivity());
                        } else {
                            rv_display_view_docs.removeAllViews();
                            callfilter_client_webservices();
                            msg = "You have successfully \n decrypted document information.";
                            AndroidUtils.showAlert(msg, getActivity(), "Success");
                        }
                    } else if (httpResult.getRequestType().equals("Decrypt Doc")
                            || httpResult.getRequestType().equals("Other Doc View")) {
                        if (!result.getBoolean("error")) {
                            String url = "";
                            JSONObject jsonObject = result.optJSONObject("data");
                            assert jsonObject != null;
                            url = jsonObject.optString("url");
                            if (isDownload) {
                                AndroidUtils.showAlert(
                                        "You have successfully downloaded the document.",
                                        getActivity(), "Success");
                                FileDownloader.downloadFile(getContext(), url, "Download1");
                            } else {
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
                            msg = result.getString("msg");
                            AndroidUtils.showAlert(msg, getActivity(), "Success");
                        }
                        callfilter_client_webservices();
                    } else if (httpResult.getRequestType().equals("Encrypt Documents")) {
                        String msg = result.optString("msg");
                        boolean error = result.getBoolean("error");
                        if (!error) {
                            msg = "You have successfully \n encrypted document information.";
                            rv_display_view_docs.removeAllViews();
                            callfilter_client_webservices();
                            AndroidUtils.showAlert(msg, getActivity(), "Success");
                        } else {
                            AndroidUtils.showAlert(msg, getActivity());
                        }
                    } else if (httpResult.getRequestType().equals("Enabled Documents")) {
                        String msg = result.optString("msg");
                        boolean error = result.getBoolean("error");
                        if (!error) {
                            rv_display_view_docs.removeAllViews();
                            callfilter_client_webservices();
                            AndroidUtils.showAlert(msg, getActivity(), "Success");
                        } else
                            AndroidUtils.showAlert(msg, getActivity());
                    } else if (httpResult.getRequestType().equals("Disabled Documents")) {
                        String msg = result.optString("msg");
                        boolean error = result.optBoolean("error");
                        if (!error) {
                            AndroidUtils.showAlert(msg, getActivity(), "Success");
                            rv_display_view_docs.removeAllViews();
                            callfilter_client_webservices();
                        } else {
                            AndroidUtils.showAlert(msg, getActivity());
                        }

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
                if (progress_dialog != null && progress_dialog.isShowing())
                    AndroidUtils.dismiss_dialog(progress_dialog);
            }
        } else if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Failed) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                AndroidUtils.showErrorAlert(result.optString("msg"), getActivity());
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        } else {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            AndroidUtils.showErrorAlert(
                    httpResult.getResponseContent().toString(), getActivity());
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
        ImageView iv_close_edit_docs = view.findViewById(R.id.close_edit_docs);
        List<String> list = new ArrayList<>(Arrays.asList(CONTENT_TYPE.split("/")));
        if (list.get(1).equalsIgnoreCase("apng") || list.get(1).equalsIgnoreCase("avif")
                || list.get(1).equalsIgnoreCase("gif") || list.get(1).equalsIgnoreCase("jpeg")
                || list.get(1).equalsIgnoreCase("png") || list.get(1).equalsIgnoreCase("svg")
                || list.get(1).equalsIgnoreCase("webp") || list.get(1).equalsIgnoreCase("jpg")) {
            idPDFView.setVisibility(GONE);
            try {
                byte[] imageData = url.getBytes(StandardCharsets.ISO_8859_1);
                Log.d("Image Decode", "Decoded byte array length: " + imageData.length);
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
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        } else {
            iv_image.setVisibility(GONE);
            new PDFviewer(idPDFView, url);
        }
        final AlertDialog dialog = dialogBuilder.create();
        iv_close_edit_docs.setOnClickListener(v -> dialog.dismiss());
        dialog.setOnDismissListener(d -> cl_document.setAlpha(1.0f));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    private void checkViewType(String url) {
        boolean isImage = File_Content_Type.isImage(tempDocModel.getContent_type());
        boolean isPDF = File_Content_Type.isPDF(tempDocModel.getContent_type());
        boolean isEncrypted = tempDocModel.isAdded_encryption() || tempDocModel.isIs_encrypted();
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
        iv_image.setVisibility(GONE);
        idPDFView.setVisibility(GONE);
        webView.setVisibility(GONE);
        final AlertDialog dialog = dialogBuilder.create();
        Dialog = dialog;
        final RetrievePDFfromUrl[] pdfTask = new RetrievePDFfromUrl[1];
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
        boolean urlIsPDF = lowerUrl.contains("application/pdf") || lowerUrl.contains(".pdf");
        if (urlIsPDF) {
            idPDFView.setVisibility(VISIBLE);
            progressBar.setVisibility(VISIBLE);
            pdfTask[0] = new RetrievePDFfromUrl(idPDFView, progressBar);
            pdfTask[0].execute(url);
        } else {
            if (isImage) {
                iv_image.setVisibility(VISIBLE);
                Glide.with(getContext()).load(url)
                        .placeholder(R.drawable.progress_animation)
                        .centerCrop().into(iv_image);
            } else {
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
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    private void handleDocumentDisplay(String url, ViewDocumentsModel docModel,
                                       PDFView idPDFView, ImageView iv_image) {
        boolean isImage = File_Content_Type.isImage(docModel.getContent_type());
        boolean isPDF = File_Content_Type.isPDF(docModel.getContent_type());
        boolean isEncrypted = docModel.isAdded_encryption() || docModel.isIs_encrypted();
        if (isEncrypted) {
            Dialog.dismiss();
            callDecryptApi(docModel.getId(), false);
            return;
        }
        if (isImage) {
            idPDFView.setVisibility(GONE);
            iv_image.setVisibility(VISIBLE);
            Glide.with(requireContext()).load(url)
                    .placeholder(R.drawable.progress_animation)
                    .centerCrop().into(iv_image);
        } else if (isPDF) {
            iv_image.setVisibility(GONE);
            idPDFView.setVisibility(VISIBLE);
        } else {
            Dialog.dismiss();
            callOtherDocViewApi(docModel.getId());
        }
    }

    private void load_view_doc(JSONArray docs) throws JSONException {
        try {
            view_docs_list.clear();
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
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

//            if (view_docs_list.isEmpty()) {
//                // Show empty state
//                if (ll_empty_state != null) {
//                    ll_empty_state.setVisibility(VISIBLE);
//                    switch (currentModule) {
//                        case "firm":
//                            tv_empty_state_title.setText("No Firm Documents Yet!");
//                            tv_empty_state_subtitle.setText("Secure and organize your documents by start uploading it.");
//                            break;
//                        case "client":
//                            tv_empty_state_title.setText("No Client Documents Yet!");
//                            tv_empty_state_subtitle.setText("Secure and organize your documents by start uploading it.");
//                            break;
//                        case "matter":
//                            tv_empty_state_title.setText("No Matter Documents Yet!");
//                            tv_empty_state_subtitle.setText("Secure and organize your documents by start uploading it.");
//                            break;
//                        case "delete":
//                            if ("solo".equals(Constants.CATEGORY)) {
//                                tv_empty_state_title.setText("No Deleted Documents Yet!");
//                                tv_empty_state_subtitle.setText("Deleted documents will appear here");
//                            } else {
//                                tv_empty_state_title.setText("No Documents Pending Approval Yet!");
//                                tv_empty_state_subtitle.setText("Documents pending approval will appear here");
//                            }
//                            break;
//                        default:
//                            tv_empty_state_title.setText("No Documents Found");
//                            tv_empty_state_subtitle.setText("Upload documents to get started");
//                            break;
//                    }
//                }
//                rv_display_view_docs.setVisibility(GONE);
//                ll_page_navigaiton.setVisibility(GONE);
//                tv_no_document.setVisibility(GONE);
//                return;
//            }

            Collections.sort(view_docs_list, new Comparator<ViewDocumentsModel>() {
                SimpleDateFormat sdf = new SimpleDateFormat(
                        "MMM dd, yyyy, hh:mm a", Locale.getDefault());

                @Override
                public int compare(ViewDocumentsModel doc1, ViewDocumentsModel doc2) {
                    try {
                        Date date1 = sdf.parse(doc1.getCreated());
                        Date date2 = sdf.parse(doc2.getCreated());
                        return date2.compareTo(date1);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return 0;
                    }
                }
            });

            if (pendingHighlightIds != null && !pendingHighlightIds.isEmpty()) {
                ArrayList<ViewDocumentsModel> highlighted = new ArrayList<>();
                ArrayList<ViewDocumentsModel> nonHighlighted = new ArrayList<>();
                for (ViewDocumentsModel doc : view_docs_list) {
                    if (pendingHighlightIds.contains(doc.getId())) {
                        highlighted.add(doc);
                    } else {
                        nonHighlighted.add(doc);
                    }
                }
                view_docs_list.clear();
                view_docs_list.addAll(highlighted);
                view_docs_list.addAll(nonHighlighted);
            }

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
                // Show empty state, hide recycler view
                if (ll_empty_state != null) {
                    ll_empty_state.setVisibility(VISIBLE);
                    switch (currentModule) {
                        case "firm":
                            tv_empty_state_title.setText("No Firm Documents Yet!");
                            tv_empty_state_subtitle.setText("Secure and organize your documents by start uploading it.");
                            break;
                        case "client":
                            tv_empty_state_title.setText("No Client Documents Yet!");
                            tv_empty_state_subtitle.setText("Secure and organize your documents by start uploading it.");
                            break;
                        case "matter":
                            tv_empty_state_title.setText("No Matter Documents Yet!");
                            tv_empty_state_subtitle.setText("Secure and organize your documents by start uploading it.");
                            break;
                        case "delete":
                            if ("solo".equals(Constants.CATEGORY)) {
                                tv_empty_state_title.setText("No Deleted Documents Yet!");
                                tv_empty_state_subtitle.setText("Deleted documents will appear here");
                            } else {
                                tv_empty_state_title.setText("No Documents Pending Approval Yet!");
                                tv_empty_state_subtitle.setText("Documents pending approval will appear here");
                            }
                            break;
                        default:
                            tv_empty_state_title.setText("No Documents Found");
                            tv_empty_state_subtitle.setText("Upload documents to get started");
                            break;
                    }
                }

                // IMPORTANT: Hide RecyclerView when empty
                if (rv_display_view_docs != null) {
                    rv_display_view_docs.setVisibility(GONE);
                }

                if (ll_page_navigaiton != null) {
                    ll_page_navigaiton.setVisibility(GONE);
                }

                if (tv_no_document != null) {
                    tv_no_document.setVisibility(GONE);
                }

                // Clear adapter data to prevent showing old list
                if (adapter1 != null) {
                    adapter1.setData(new ArrayList<>());
                }

                return; // Exit early - no need to continue
            }

            // Has documents - hide empty state and show recycler view
            if (ll_empty_state != null) {
                ll_empty_state.setVisibility(GONE);
            }

            if (tv_no_document != null) {
                tv_no_document.setVisibility(GONE);
            }

            if (isGridView) {
                int spanCount = isTabletDevice() ? 3 : 2;
                rv_display_view_docs.setLayoutManager(
                        new GridLayoutManager(getContext(), spanCount));
            } else {
                rv_display_view_docs.setLayoutManager(
                        new LinearLayoutManager(getContext()));
            }

            adapter1 = new View_documents_adapter(view_docs_list, this, getContext(),
                    is_MergePdfClicked, DOCUMENT_TYPE_TAG, pendingHighlightIds);
            adapter1.setViewType(isGridView
                    ? View_documents_adapter.VIEW_TYPE_GRID
                    : View_documents_adapter.VIEW_TYPE_LIST);
            rv_display_view_docs.setAdapter(adapter1);
            AndroidUtils.LoadAnimation(rv_display_view_docs, getContext());

            if (!view_docs_list.isEmpty()) {
                int startIndex = PaginationHelper.startIndexForCurrentPage(currentPage, 10);
                int endIndex = PaginationHelper.endIndexForCurrentPage(
                        startIndex, view_docs_list.size(), 10);
                end_temp = endIndex;
                pageItems = new ArrayList<>(view_docs_list.subList(startIndex, endIndex));
                adapter1.setData(pageItems);
                if (pendingHighlightIds != null && !pendingHighlightIds.isEmpty()) {
                    scrollToHighlightedDocument(view_docs_list);
                }
                UpdatePageButton(1);

                // IMPORTANT: Show RecyclerView only when there's data
                rv_display_view_docs.setVisibility(VISIBLE);
                ll_page_navigaiton.setVisibility(VISIBLE);
            }

            // ── POST: guaranteed final hide after layout pass ─────────────
            rv_display_view_docs.post(() -> {
                if (getContext() == null) return;
                if (adapter1 != null && adapter1.getItemCount() > 0) {
                    if (ll_empty_state != null) ll_empty_state.setVisibility(GONE);
                    rv_display_view_docs.setVisibility(VISIBLE);
                    ll_page_navigaiton.setVisibility(VISIBLE);
                }
            });

            // ── Restore search watchers ───────────────────────────────────
            if (!tv_search_client_view.getText().toString().isEmpty()) {
                Searchfilter(tv_search_client_view);
            }
            if (!tv_search_client_views.getText().toString().isEmpty()) {
                Searchfilter(tv_search_client_views);
            }
            tv_search_client_view.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence cs, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    Searchfilter(tv_search_client_view);
                }
            });
            tv_search_client_views.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence cs, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    Searchfilter(tv_search_client_views);
                }
            });

            pendingHighlightIds = null;

        } catch (Exception e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private ArrayList<ViewDocumentsModel> filterList(ArrayList<ViewDocumentsModel> list,
                                                     String charString) {
        isListFiltered = false;
        if (charString.isEmpty()) {
            return list;
        } else {
            ArrayList<ViewDocumentsModel> filteredList = new ArrayList<>();
            Pattern pattern = Pattern.compile(
                    Pattern.quote(charString), Pattern.CASE_INSENSITIVE);
            for (ViewDocumentsModel row : list) {
                if (pattern.matcher(AndroidUtils.isNull(row.getName()).toLowerCase()).find()
                        || pattern.matcher(
                        AndroidUtils.isNull(row.getFilename()).toLowerCase()).find()) {
                    filteredList.add(row);
                }
            }
            if (filteredList.isEmpty()) {
                return new ArrayList<>();
            } else {
                isListFiltered = true;
                return filteredList;
            }
        }
    }

    private void loadGroupsData(JSONArray data, ArrayList<DocumentsModel> groupsList,
                                boolean isclient_groups) {
        groupsList.clear();
        selected_client_groups_list.clear();
        try {
            StringBuilder allIds = new StringBuilder();

            for (int i = 0; i < data.length(); i++) {
                JSONObject jsonObject = data.getJSONObject(i);
                DocumentsModel documentsModel = new DocumentsModel();
                documentsModel.setGroup_id(jsonObject.optString("id"));
                documentsModel.setGroup_name(jsonObject.optString("name"));
                if ((!jsonObject.optString("name").equals("AAM"))
                        && (!jsonObject.optString("name").equals("SuperUser"))) {
                    documentsModel.setGroup_id(jsonObject.optString("id"));
                    documentsModel.setGroup_name(jsonObject.optString("name"));
                    if (isclient_groups) {
                        documentsModel.setGroupChecked(true);
                        selected_client_groups_list.add(documentsModel);
                    }
                    groupsList.add(documentsModel);

                    // Build all IDs while iterating
                    if (allIds.length() > 0) allIds.append(",");
                    allIds.append(jsonObject.optString("id"));
                }
            }
            // Add "All Groups" entry at top for view mode only (not client_groups, not upload)
            if (!isUploadDoc && !isclient_groups && !groupsList.isEmpty()) {
                DocumentsModel allGroupsModel = new DocumentsModel();
                allGroupsModel.setGroup_id(allIds.toString());
                allGroupsModel.setGroup_name("All Groups");
                allGroupsModel.setGroupChecked(true); // pre-checked
                groupsList.add(0, allGroupsModel);

                // Auto-select "All Groups" and trigger filter
                selected_groups_list.clear();
                selected_groups_list.add(allGroupsModel);
                if (tv_select_groups_view != null) {
                    tv_select_groups_view.setText(allGroupsModel.getGroup_name());
                }
                callfilter_client_webservices();
            }

            if (selectedGroupsIds != null && !selectedGroupsIds.isEmpty()) {
                selected_groups_list.clear();
                for (int i = 0; i < groupsList.size(); i++) {
                    DocumentsModel group = groupsList.get(i);
                    if (selectedGroupsIds.contains(group.getGroup_id())) {
                        group.setGroupChecked(true);
                        selected_groups_list.add(group);
                    }
                }
            }
            selectedLanguage = new boolean[groupsList.size()];
            if (isclient_groups) {
                GroupsPopup(ll_upload_groups, client_groups_list, selected_client_groups_list,
                        rv_upload_groups, tv_select_upload_groups);
            } else {
                ll_upload_client_group.setVisibility(GONE);
                if (isUploadDoc)
                    GroupsPopup(upload_group_layout, groupsList, selected_groups_list,
                            rv_display_upload_groups_docs, tv_select_groups);
                else
                    GroupsPopup(view_group_layout, groupsList, selected_groups_list,
                            rv_display_view_groups_docs, tv_select_groups_view);
            }
        } catch (JSONException e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    @SuppressLint("MissingInflatedId")
    private void GroupsPopup(LinearLayout upload_group_layout,
                             ArrayList<DocumentsModel> groupsList,
                             ArrayList<DocumentsModel> selected_groups_list,
                             RecyclerView rv_display_upload_groups_docs,
                             TextView tv_select_groups) {
        tv_select_groups.setText("");
        tv_select_groups.setHint(R.string.select_groups);
        ll_upload_groups.setVisibility(GONE);
        try {
            for (int i = 0; i < groupsList.size(); i++) {
                for (int j = 0; j < selected_groups_list.size(); j++) {
                    if (groupsList.get(i).getGroup_id().matches(
                            selected_groups_list.get(j).getGroup_id())) {
                        DocumentsModel documentsModel = groupsList.get(i);
                        documentsModel.setChecked(true);
                    }
                }
            }
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                    getContext(), LinearLayoutManager.VERTICAL, false);
            rv_display_upload_groups_docs.setLayoutManager(layoutManager);
            GroupsListAdapter documentsAdapter = new GroupsListAdapter(
                    groupsList,
                    com.digicoffer.lauditor.Documents.Documents.class.newInstance(),
                    new GroupsListAdapter.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(DocumentsModel documentsModel) {
                            if (documentsModel.isGroupChecked()) {
                                selected_groups_list.add(documentsModel);
                            } else {
                                for (int i = 0; i < selected_groups_list.size(); i++) {
                                    if (selected_groups_list.get(i).getGroup_id().equals(
                                            documentsModel.getGroup_id())) {
                                        selected_groups_list.remove(i);
                                        break;
                                    }
                                }
                            }
                            tv_select_groups.setText("");
                            tv_select_groups.setHint(R.string.select_groups);
                            String[] value = new String[selected_groups_list.size()];
                            for (int i = 0; i < selected_groups_list.size(); i++) {
                                value[i] = selected_groups_list.get(i).getGroup_name();
                            }
                            String str = TextUtils.join(",", value);
                            tv_select_groups.setText(str);
                        }
                    });
            String[] value = new String[selected_groups_list.size()];
            for (int i = 0; i < selected_groups_list.size(); i++) {
                value[i] = selected_groups_list.get(i).getGroup_name();
            }
            String str = TextUtils.join(",", value);
            tv_select_groups.setText(str);
            rv_display_upload_groups_docs.setAdapter(documentsAdapter);
            AndroidUtils.LoadList(rv_display_upload_groups_docs, getContext(),
                    groupsList.size(), true);
        } catch (IllegalAccessException | java.lang.InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadClientGroups() {
        try {
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                    getContext(), LinearLayoutManager.VERTICAL, false);
            rv_upload_groups.setLayoutManager(layoutManager);
            GroupsListAdapter documentsAdapter = new GroupsListAdapter(
                    client_groups_list,
                    com.digicoffer.lauditor.Documents.Documents.class.newInstance(),
                    new GroupsListAdapter.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(DocumentsModel documentsModel) {
                            if (documentsModel.isGroupChecked()) {
                                selected_client_groups_list.add(documentsModel);
                            } else {
                                for (int i = 0; i < selected_client_groups_list.size(); i++) {
                                    if (selected_client_groups_list.get(i).getGroup_id().equals(
                                            documentsModel.getGroup_id())) {
                                        selected_client_groups_list.remove(i);
                                        break;
                                    }
                                }
                            }
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
        matterlist.clear();
        for (int i = 0; i < matters.length(); i++) {
            JSONObject jsonObject = matters.getJSONObject(i);
            MattersModel mattersModel = new MattersModel();
            mattersModel.setId(jsonObject.optString("id"));
            mattersModel.setTitle(jsonObject.optString("title"));
            mattersModel.setType(jsonObject.optString("type"));
            matterlist.add(mattersModel);
        }

        if (!isUploadDoc && !matterlist.isEmpty()) {
            // Build a comma-separated string of all matter IDs
            StringBuilder allIds = new StringBuilder();
            for (int i = 0; i < matterlist.size(); i++) {
                allIds.append(matterlist.get(i).getId());
                if (i < matterlist.size() - 1) {
                    allIds.append(",");
                }
            }

            MattersModel allMattersModel = new MattersModel();
            allMattersModel.setId(allIds.toString());
            allMattersModel.setTitle("All Matters");
            allMattersModel.setType(matterlist.get(0).getType()); // same type as others
            matterlist.add(0, allMattersModel); // add at top of list
        }

        if (matterlist.isEmpty()) {
            ll_matter_view.setVisibility(GONE);
            ll_matter.setVisibility(GONE);
            Constants.create_matter = true;
            Constants.MATTER_TYPE = "Legal";
            AndroidUtils.showReDirectionPopup(getActivity(), new Matter(),
                    "Please create a matter to upload documents. Click here to create matter.");
        } else {
            ll_matter_view.setVisibility(VISIBLE);
            ll_matter.setVisibility(VISIBLE);
        }
        initMatter();
    }

    //    private void initMatter() {
//        Matteradapter = new CommonSpinnerAdapter(getActivity(), this.matterlist);
//        Log.i("ArrayList", "Info:" + matterlist);
//        list_matter.setAdapter(Matteradapter);
//        custom_spinner2.setText("");
//        custom_spinner4.setText("");
//        list_matter_view.setAdapter(Matteradapter);
//        AndroidUtils.LoadList(list_matter, getContext(), matterlist.size(), true);
//        AndroidUtils.LoadList(list_matter_view, getContext(), matterlist.size(), true);
//        if (selectedId != null && !selectedId.isEmpty()) {
//            for (int i = 0; i < matterlist.size(); i++) {
//                MattersModel model = matterlist.get(i);
//                if (selectedId.equals(model.getId())) {
//                    String matter_name = model.getTitle();
//                    custom_spinner2.setText(matter_name);
//                    list_matter.setVisibility(GONE);
//                    ischecked_matter = true;
//                    img_dropdown_icon2.setVisibility(GONE);
//                    img_clear_icon2.setVisibility(VISIBLE);
//                    custom_spinner4.setText(matter_name);
//                    list_matter_view.setVisibility(GONE);
//                    ischecked_matter2 = true;
//                    ismatter_chosen = false;
//                    img_dropdown_icon4.setVisibility(GONE);
//                    img_clear_icon4.setVisibility(VISIBLE);
//                }
//            }
//        }
//        list_matter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                MattersModel selectedMatter = (MattersModel) parent.getItemAtPosition(position);
//                matter_id = selectedMatter.getId();
//                String matter_name = selectedMatter.getTitle();
//                custom_spinner2.setText(matter_name);
//                AndroidUtils.DisplaySpinnerView(list_matter, custom_spinner2, matter_name,
//                        img_dropdown_icon2, img_clear_icon2, false);
//                custom_spinner2.setText(matter_name);
//                img_dropdown_icon2.setVisibility(GONE);
//                img_clear_icon2.setVisibility(VISIBLE);
//                ischecked_matter = true;
//                callClientGroupsWebservice();
//                callfilter_client_webservices();
//            }
//        });
//        list_matter_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                MattersModel selectedMatter = (MattersModel) parent.getItemAtPosition(position);
//                matter_id = selectedMatter.getId();
//                String matter_name = selectedMatter.getTitle();
//                custom_spinner4.setText(matter_name);
//                AndroidUtils.DisplaySpinnerView(list_matter_view, custom_spinner4, matter_name,
//                        img_dropdown_icon4, img_clear_icon4, false);
//                custom_spinner4.setText(matter_name);
//                img_dropdown_icon4.setVisibility(GONE);
//                img_clear_icon4.setVisibility(VISIBLE);
//                ischecked_matter2 = true;
//                ismatter_chosen = false;
//                callfilter_client_webservices();
//                rv_display_view_docs.setVisibility(VISIBLE);
//            }
//        });
//    }
    private void filterDeletedDocsByCategory(String selectedType) {
        String categoryFilter = selectedType.toLowerCase(); // "matter", "client", "firm"

        ArrayList<ViewDocumentsModel> filtered = new ArrayList<>();
        for (ViewDocumentsModel doc : view_docs_list) {
            if (doc.getCategory() != null &&
                    doc.getCategory().equalsIgnoreCase(categoryFilter)) {
                filtered.add(doc);
            }
        }

        currentPage = 1;
        setupPagination(filtered);
        loadViewDocumentsRecyclerview(filtered);
        UpdatePageButton(currentPage);
    }

    private void initDocType() {
        ArrayList<String> docTypeList = new ArrayList<>();
        docTypeList.clear();
        docTypeList.add("Matter");
        docTypeList.add("Client");
        if (!"solo".equals(Constants.CATEGORY)) {
            docTypeList.add("Firm");
        }
        docTypeAdapter = new CommonSpinnerAdapter(getActivity(), docTypeList);
        list_docType_view.setAdapter(docTypeAdapter);
        custom_spinner5.setText("");
        AndroidUtils.LoadList(list_docType_view, getContext(), docTypeList.size(), true);

        list_docType_view.setOnItemClickListener((parent, view, position, id) -> {
            String selectedType = (String) parent.getItemAtPosition(position);
            AndroidUtils.DisplaySpinnerView(list_docType_view, custom_spinner5, selectedType,
                    img_dropdown_icon5, img_clear_icon5, false, docTypeAdapter, "Search DocType");
            ischecked_docType = true;
            filterDeletedDocsByCategory(selectedType);
        });
    }

    private void initMatter() {
        Matteradapter = new CommonSpinnerAdapter(getActivity(), this.matterlist);
        Log.i("ArrayList", "Info:" + matterlist);
        list_matter.setAdapter(Matteradapter);
        custom_spinner2.setText("");
        custom_spinner4.setText("");
        list_matter_view.setAdapter(Matteradapter);
        AndroidUtils.LoadList(list_matter, getContext(), matterlist.size(), true);
        AndroidUtils.LoadList(list_matter_view, getContext(), matterlist.size(), true);

        // Auto-select "All Matters" (index 0) in view mode
        if (!isUploadDoc && !matterlist.isEmpty()) {
            MattersModel allMatters = matterlist.get(0); // "All Matters" is always at index 0
            matter_id = allMatters.getId();
            custom_spinner4.setText(allMatters.getTitle());
            img_dropdown_icon4.setVisibility(GONE);
            img_clear_icon4.setVisibility(VISIBLE);
            ischecked_matter2 = true;
            ismatter_chosen = false;
            callfilter_client_webservices();
            rv_display_view_docs.setVisibility(VISIBLE);
        }

        if (selectedId != null && !selectedId.isEmpty()) {
            for (int i = 0; i < matterlist.size(); i++) {
                MattersModel model = matterlist.get(i);
                if (selectedId.equals(model.getId())) {
                    String matter_name = model.getTitle();
                    custom_spinner2.setText(matter_name);
                    list_matter.setVisibility(GONE);
                    ischecked_matter = true;
                    img_dropdown_icon2.setVisibility(GONE);
                    img_clear_icon2.setVisibility(VISIBLE);
                    custom_spinner4.setText(matter_name);
                    list_matter_view.setVisibility(GONE);
                    ischecked_matter2 = true;
                    ismatter_chosen = false;
                    img_dropdown_icon4.setVisibility(GONE);
                    img_clear_icon4.setVisibility(VISIBLE);
                }
            }
        }
        list_matter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MattersModel selectedMatter = (MattersModel) parent.getItemAtPosition(position);
                matter_id = selectedMatter.getId();
                String matter_name = selectedMatter.getTitle();
                AndroidUtils.DisplaySpinnerView(list_matter, custom_spinner2, matter_name,
                        img_dropdown_icon2, img_clear_icon2, false, Matteradapter, "Search Matter");
                ischecked_matter = true;
                callClientGroupsWebservice();
                callfilter_client_webservices();
            }
        });

        list_matter_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MattersModel selectedMatter = (MattersModel) parent.getItemAtPosition(position);
                matter_id = selectedMatter.getId();
                String matter_name = selectedMatter.getTitle();
                AndroidUtils.DisplaySpinnerView(list_matter_view, custom_spinner4, matter_name,
                        img_dropdown_icon4, img_clear_icon4, false, Matteradapter, "Search Matter");
                ischecked_matter2 = true;
                ismatter_chosen = false;
                callfilter_client_webservices();
                rv_display_view_docs.setVisibility(VISIBLE);
            }
        });
    }

    //    private void initUI(ArrayList<ClientsModel> clientsList) {
//        ClientAdapter = new CommonSpinnerAdapter(getActivity(), clientsList);
//        list_client.setAdapter(ClientAdapter);
//        list_client_view.setAdapter(ClientAdapter);
//        AndroidUtils.LoadList(list_client, getContext(), clientsList.size(), true);
//        AndroidUtils.LoadList(list_client_view, getContext(), clientsList.size(), true);
//        if (selectedId != null && !selectedId.isEmpty()) {
//            for (int i = 0; i < clientsList.size(); i++) {
//                ClientsModel model = clientsList.get(i);
//                if (selectedId.equals(model.getId())) {
//                    client_name = model.getName();
//                    AndroidUtils.DisplaySpinnerView(list_client, custom_spinner, client_name,
//                            img_dropdown_icon1, img_clear_icon1, true);
//                    custom_spinner3.setText(client_name);
//                    img_dropdown_icon1.setVisibility(GONE);
//                    img_clear_icon1.setVisibility(VISIBLE);
//                    ischecked = true;
//                    ischecked2 = true;
//                    break;
//                }
//            }
//        }
//        list_client.setOnItemClickListener((parent, view, position, id) -> {
//            ClientsModel selectedClient = (ClientsModel) parent.getItemAtPosition(position);
//            client_id = selectedClient.getId();
//            client_name = selectedClient.getName();
//            AndroidUtils.DisplaySpinnerView(list_client, custom_spinner, client_name,
//                    img_dropdown_icon1, img_clear_icon1, false);
//            custom_spinner.setText(client_name);
//            img_dropdown_icon1.setVisibility(GONE);
//            img_clear_icon1.setVisibility(VISIBLE);
//            img_dropdown_icon2.setVisibility(VISIBLE);
//            img_clear_icon2.setVisibility(GONE);
//            ischecked = true;
//            callfilter_client_webservices();
//        });
//        list_client_view.setOnItemClickListener((parent, view, position, id) -> {
//            ClientsModel selectedClient = (ClientsModel) parent.getItemAtPosition(position);
//            selected_groups_list.clear();
//            client_id = selectedClient.getId();
//            String selectedClientName = selectedClient.getName();
//            custom_spinner3.setText(selectedClientName);
//            AndroidUtils.DisplaySpinnerView(list_client_view, custom_spinner3, selectedClientName,
//                    img_dropdown_icon3, img_clear_icon3, false);
//            custom_spinner3.setText(selectedClientName);
//            img_dropdown_icon3.setVisibility(GONE);
//            img_clear_icon3.setVisibility(VISIBLE);
//            currentPage = 1;
//            CATEGORY_TAG = "client";
//            custom_spinner4.setText("");
//            ismatter_chosen = true;
//            callfilter_client_webservices();
//            rv_display_view_docs.setVisibility(VISIBLE);
//            ischecked2 = true;
//            img_dropdown_icon4.setVisibility(VISIBLE);
//            img_clear_icon4.setVisibility(GONE);
//        });
//    }
    private void initUI(ArrayList<ClientsModel> clientsList) {
        ClientAdapter = new CommonSpinnerAdapter(getActivity(), clientsList);
        list_client.setAdapter(ClientAdapter);
        list_client_view.setAdapter(ClientAdapter);
        AndroidUtils.LoadList(list_client, getContext(), clientsList.size(), true);
        AndroidUtils.LoadList(list_client_view, getContext(), clientsList.size(), true);

        // Auto-select "All Clients" (index 0) in view mode
        if (!isUploadDoc && !clientsList.isEmpty()) {
            ClientsModel allClients = clientsList.get(0); // "All Clients" is always at index 0
            client_id = allClients.getId();
            String allClientsName = allClients.getName();
            custom_spinner3.setText(allClientsName);
            img_dropdown_icon3.setVisibility(GONE);
            img_clear_icon3.setVisibility(VISIBLE);
            ischecked2 = true;
            CATEGORY_TAG = "client";
            ismatter_chosen = true;
            callfilter_client_webservices();
            rv_display_view_docs.setVisibility(VISIBLE);
        }

        if (selectedId != null && !selectedId.isEmpty()) {
            for (int i = 0; i < clientsList.size(); i++) {
                ClientsModel model = clientsList.get(i);
                if (selectedId.equals(model.getId())) {
                    client_name = model.getName();
                    AndroidUtils.DisplaySpinnerView(list_client, custom_spinner, client_name,
                            img_dropdown_icon1, img_clear_icon1, false,ClientAdapter,"Search Client");
                    custom_spinner3.setText(client_name);
                    img_dropdown_icon1.setVisibility(GONE);
                    img_clear_icon1.setVisibility(VISIBLE);
                    ischecked = true;
                    ischecked2 = true;
                    break;
                }
            }
        }
        list_client.setOnItemClickListener((parent, view, position, id) -> {
            ClientsModel selectedClient = (ClientsModel) parent.getItemAtPosition(position);
            client_id = selectedClient.getId();
            client_name = selectedClient.getName();
            AndroidUtils.DisplaySpinnerView(list_client, custom_spinner, client_name,
                    img_dropdown_icon1, img_clear_icon1, false, ClientAdapter, "Search Client");
            img_dropdown_icon2.setVisibility(VISIBLE);
            img_clear_icon2.setVisibility(GONE);
            ischecked = true;
            callfilter_client_webservices();
        });

        list_client_view.setOnItemClickListener((parent, view, position, id) -> {
            ClientsModel selectedClient = (ClientsModel) parent.getItemAtPosition(position);
            selected_groups_list.clear();
            client_id = selectedClient.getId();
            String selectedClientName = selectedClient.getName();
            AndroidUtils.DisplaySpinnerView(list_client_view, custom_spinner3, selectedClientName,
                    img_dropdown_icon3, img_clear_icon3, false, ClientAdapter, "Search Client");
            currentPage = 1;
            CATEGORY_TAG = "client";
            custom_spinner4.setText("");
            ismatter_chosen = true;
            callfilter_client_webservices();
            rv_display_view_docs.setVisibility(VISIBLE);
            ischecked2 = true;
            img_dropdown_icon4.setVisibility(VISIBLE);
            img_clear_icon4.setVisibility(GONE);
        });
    }

    private void loadCorpClients(JSONObject data) throws JSONException {
        JSONArray relationships = data.getJSONArray("relationships");
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
        }
        clientsList.addAll(CorpClientsList);
        initUI(clientsList);
    }

    private void loadClients(JSONObject data) throws JSONException {
        JSONArray relationships = data.getJSONArray("relationships");
        clientsList.clear();
        for (int i = 0; i < relationships.length(); i++) {
            JSONObject jsonObject = relationships.getJSONObject(i);
            ClientsModel clientsModel = new ClientsModel();
            clientsModel.setId(jsonObject.optString("id"));
            clientsModel.setName(jsonObject.optString("name"));
            clientsModel.setType(jsonObject.optString("type"));
            clientsList.add(clientsModel);
        }

        if (!isUploadDoc && !clientsList.isEmpty()) {
            // Build a comma-separated string of all client IDs
            StringBuilder allIds = new StringBuilder();
            for (int i = 0; i < clientsList.size(); i++) {
                allIds.append(clientsList.get(i).getId());
                if (i < clientsList.size() - 1) {
                    allIds.append(",");
                }
            }

            ClientsModel allClientsModel = new ClientsModel();
            allClientsModel.setId(allIds.toString());
            allClientsModel.setName("All Clients");
            allClientsModel.setType(clientsList.get(0).getType()); // same type as others
            clientsList.add(0, allClientsModel); // add at top of list
        }

        if (!clientsList.isEmpty()) {
            ll_client_name.setVisibility(VISIBLE);
            ll_client_name_view.setVisibility(VISIBLE);
        } else {
            ll_client_name.setVisibility(GONE);
            ll_client_name_view.setVisibility(GONE);
            AndroidUtils.showReDirectionPopup(getActivity(), new ClientRelationship(),
                    "Please add a relationship to upload documents. Click here to add relationship.");
        }
    }

    private void EnableUpload() {
        if ((!custom_spinner.getText().toString().isEmpty())
                || (!custom_spinner2.getText().toString().isEmpty())
                || (!selected_groups_list.isEmpty())) {
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
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v2/matter/list", "Legal Matter", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            e.fillInStackTrace();
        }
    }

    @Override
    public void ViewTags(DocumentsModel
                                 documentsModel, ArrayList<DocumentsModel> itemsArrayList) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        cl_document.setAlpha(0.5f);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view_edit_tags = inflater.inflate(R.layout.edit_existing_tags, null);
        LinearLayout ll_existing_tags = view_edit_tags.findViewById(R.id.ll_view_tags);
        ImageView iv_close_existing_tags = view_edit_tags.findViewById(R.id.close_edit_docs);
        TextView header_name = view_edit_tags.findViewById(R.id.header_name);
        TextView tv_document_name = view_edit_tags.findViewById(R.id.tv_document_name);
        tv_document_name.setTextSize(DynamicUtils.twenty);
        tv_document_name.setVisibility(VISIBLE);
        tv_document_name.setText(documentsModel.getName());
        header_name.setText(R.string.view_tags);
        Iterator<String> iter = documentsModel.getTags().keys();
        while (iter.hasNext()) {
            String key = iter.next();
            String value = documentsModel.getTags().optString(key);
            View view_added_tags = inflater.inflate(R.layout.displays_documents_list, null);
            TextView tv_tag_name = view_added_tags.findViewById(R.id.tv_document_name);
            ImageView iv_remove_tag = view_added_tags.findViewById(R.id.iv_cancel);
            String tag_msg = key + " - " + value;
            tv_tag_name.setText(tag_msg);
            iv_remove_tag.setOnClickListener(view -> {
                ll_existing_tags.removeView(view_added_tags);
                documentsModel.getTags().remove(key);
            });
            ll_existing_tags.addView(view_added_tags);
        }
        AlertDialog dialog = dialogBuilder.create();
        iv_close_existing_tags.setOnClickListener(view -> dialog.dismiss());
        dialog.setOnDismissListener(d -> cl_document.setAlpha(1.0f));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(view_edit_tags);
        dialog.show();
    }

    private ArrayList<DocumentsModel> getTagsFromDocument(DocumentsModel model) {
        ArrayList<DocumentsModel> list = new ArrayList<>();
        JSONObject tags = model.getTags();
        if (tags == null) return list;
        Iterator<String> keys = tags.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                DocumentsModel tag = new DocumentsModel();
                tag.setTag_type(key);
                tag.setTag_name(tags.getString(key));
                list.add(tag);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public void EditDocuments(DocumentsModel documentsModel,
                              ArrayList<DocumentsModel> itemsArrayList, int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
        cl_document.setAlpha(0.5f);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.edit_document, null);
        TextInputEditText etDocName = view.findViewById(R.id.edit_doc_name);
        TextInputEditText etDescription = view.findViewById(R.id.edit_description);
        AppCompatButton btnExpDate = view.findViewById(R.id.tv_expiration_date);
        InputFilter[] filters = new InputFilter[]{new InputFilter.LengthFilter(50)};
        InputFilter[] filters1 = new InputFilter[]{new InputFilter.LengthFilter(300)};
        TextView tv_document_name = view.findViewById(R.id.tv_document_name);
        tv_document_name.setText(R.string.document_name);
        TextView description = view.findViewById(R.id.description);
        description.setText(R.string.description);
        btnExpDate.setHint(R.string.expiration_date);
        TextView expiration_date_id = view.findViewById(R.id.expiration_date_id);
        expiration_date_id.setText(R.string.expiration_date);
        etDocName.setHint(R.string.document_name);
        etDescription.setHint(R.string.description);
        etDocName.setMaxLines(5);
        etDocName.setFilters(filters);
        etDescription.setMaxLines(10);
        etDescription.setFilters(filters1);
        etDocName.addTextChangedListener(new Validation(etDocName));
        etDescription.addTextChangedListener(new DescriptionValidation(etDescription));
        SwitchMaterial switchDownload =
                view.findViewById(R.id.switch_download).findViewById(R.id.switch_action);
        SwitchMaterial switchEncryption =
                view.findViewById(R.id.switch_encryption).findViewById(R.id.switch_action);
        TextView tv_label = view.findViewById(R.id.switch_encryption)
                .findViewById(R.id.tv_label);
        tv_label.setText(R.string.enable_encryption);
        AppCompatButton btnAddTag = view.findViewById(R.id.btn_add_tag);
        llSelectedTags = view.findViewById(R.id.ll_selected_tags);
        AppCompatButton btnSave = view.findViewById(R.id.btn_save_tag);
        AppCompatButton btnCancel = view.findViewById(R.id.btn_cancel_edit_docs);
        ImageView ivClose = view.findViewById(R.id.close_edit_docs);
        etDocName.setText(documentsModel.getName());
        etDescription.setText(documentsModel.getDescription());
        btnExpDate.setText(AndroidUtils.formatToMMMddYYYY(documentsModel.getExpiration_date()));
        switchDownload.setChecked(documentsModel.isIsenabled());
        checkSwitchState(switchDownload);
        switchEncryption.setChecked(documentsModel.getIsencrypted());
        checkSwitchState(switchEncryption);
        switchDownload.setOnCheckedChangeListener((compoundButton, b) -> {
            documentsModel.setIsenabled(b);
            checkSwitchState(switchDownload);
        });
        switchEncryption.setOnCheckedChangeListener((compoundButton, b) -> {
            documentsModel.setIsencrypted(b);
            checkSwitchState(switchEncryption);
        });
        ArrayList<DocumentsModel> tagList = new ArrayList<>();
        JSONObject tags = documentsModel.getTags();
        if (tags != null) {
            Iterator<String> keys = tags.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    DocumentsModel tag = new DocumentsModel();
                    tag.setTag_type(key);
                    tag.setTag_name(tags.getString(key));
                    tagList.add(tag);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        renderSelectedTags(
                getContext(),
                llSelectedTags,
                tagList,
                documentsModel,
                cl_document,
                getActivity()
        );
        btnExpDate.setOnClickListener(v ->
                AndroidUtils.showDatePicker(btnExpDate, false, false, true, null));
        btnAddTag.setOnClickListener(v -> {
            DocumentsModel model = itemsArrayList.get(position);
            model.setName(etDocName.getText().toString().trim());
            model.setDescription(etDescription.getText().toString().trim());
            model.setExpiration_date(
                    AndroidUtils.convertAnyDateToDDMMYYYY(btnExpDate.getText().toString()));
            model.setIsenabled(switchDownload.isChecked());
            model.setIsencrypted(switchEncryption.isChecked());
            selected_documents_list.clear();
            selected_documents_list.add(model);
            open_add_tags_popup();
        });
        AlertDialog dialog = dialogBuilder.setView(view).setCancelable(false).create();
        btnSave.setOnClickListener(v -> {
            if (Objects.requireNonNull(etDocName.getText()).toString().isEmpty()
                    && Objects.requireNonNull(etDescription.getText()).toString().isEmpty()) {
                AndroidUtils.showAlert(
                        "Please enter the Document name, Description", getActivity());
            } else if (Objects.requireNonNull(etDescription.getText()).toString().isEmpty()) {
                AndroidUtils.showAlert("Please enter the Description", getActivity());
            } else if (Objects.requireNonNull(etDocName.getText()).toString().isEmpty()) {
                AndroidUtils.showAlert("Please enter the Document name", getActivity());
            } else {
                DocumentsModel model = itemsArrayList.get(position);
                model.setName(etDocName.getText().toString().trim());
                model.setDescription(etDescription.getText().toString().trim());
                model.setExpiration_date(
                        AndroidUtils.convertAnyDateToDDMMYYYY(btnExpDate.getText().toString()));
                model.setIsenabled(switchDownload.isChecked());
                model.setIsencrypted(switchEncryption.isChecked());
                itemsArrayList.set(position, model);
                dialog.dismiss();
                loadRecyclerview("edit_meta", "");
            }
        });
        View.OnClickListener closeListener = v -> {
            DocumentsModel editedModel = new DocumentsModel();
            editedModel.setName(etDocName.getText().toString().trim());
            editedModel.setDescription(etDescription.getText().toString().trim());
            editedModel.setExpiration_date(
                    AndroidUtils.convertAnyDateToDDMMYYYY(btnExpDate.getText().toString()));
            editedModel.setIsenabled(switchDownload.isChecked());
            editedModel.setIsencrypted(switchEncryption.isChecked());
            editedModel.setTags(itemsArrayList.get(position).getTags());
            boolean changed = hasDocumentChanged(documentsModel, editedModel);
            if (changed) {
                AndroidUtils.showConfirmationDialog(getContext(), "Alert!",
                        "Changes you made will not be saved. Do you want to continue?",
                        new AndroidUtils.OnConfirmListener() {
                            @Override
                            public void onSave() {
                            }

                            @Override
                            public void onCancel() {
                                dialog.dismiss();
                            }
                        });
            } else {
                dialog.dismiss();
            }
        };
        btnCancel.setOnClickListener(closeListener);
        ivClose.setOnClickListener(closeListener);
        dialog.setOnDismissListener(d -> cl_document.setAlpha(1f));
        dialog.show();
    }

    private void handleEditDocClose(String name, String description, String exp_date,
                                    TextView tv_doc_name, TextView tv_description,
                                    TextView tv_exp_date, Dialog dialog) {
        String docName = Objects.requireNonNull(tv_doc_name.getText()).toString();
        String desc = Objects.requireNonNull(tv_description.getText()).toString();
        String exp = Objects.requireNonNull(
                AndroidUtils.convertAnyDateToDDMMYYYY(tv_exp_date.getText().toString()));
        if ((!docName.isEmpty() && !docName.equals(name))
                || (!desc.isEmpty() && !desc.equals(description))
                || (!exp.isEmpty() && !exp.equals(exp_date))) {
            AndroidUtils.showConfirmationDialog(getContext(), "Alert!",
                    "Changes you made will not be saved. Do you want to continue?",
                    new AndroidUtils.OnConfirmListener() {
                        @Override
                        public void onSave() {
                        }

                        @Override
                        public void onCancel() {
                            dialog.dismiss();
                        }
                    });
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
        ImageView iv_cancel_edit_doc = view_edit_documents.findViewById(R.id.close_edit_docs);
        AppCompatButton btn_close_edit_docs =
                view_edit_documents.findViewById(R.id.btn_cancel_edit_docs);
        TextInputEditText tv_doc_name = view_edit_documents.findViewById(R.id.edit_doc_name);
        InputFilter[] filters = new InputFilter[]{new InputFilter.LengthFilter(50)};
        InputFilter[] filters1 = new InputFilter[]{new InputFilter.LengthFilter(300)};
        tv_doc_name.setMaxLines(5);
        tv_doc_name.setFilters(filters);
        TextView tv_document_name = view_edit_documents.findViewById(R.id.tv_document_name);
        tv_document_name.setText(R.string.document_name);
        TextInputEditText tv_description = view_edit_documents.findViewById(R.id.edit_description);
        tv_doc_name.setHint(R.string.document_name);
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
        tv_exp_date.setOnClickListener(v ->
                AndroidUtils.showDatePicker(tv_exp_date, false, false, true, null));
        if (viewDocumentsModel.getExpiration_date().equals("NA")
                || viewDocumentsModel.getExpiration_date().equals("")) {
            tv_exp_date.setText("");
        } else {
            tv_exp_date.setText(
                    AndroidUtils.formatToMMMddYYYY(viewDocumentsModel.getExpiration_date()));
        }
        tv_doc_name.setText(viewDocumentsModel.getName());
        tv_description.setText(viewDocumentsModel.getDescription());
        AppCompatButton btn_save_tag = view_edit_documents.findViewById(R.id.btn_save_tag);
        final AlertDialog dialog = dialogBuilder.create();
        iv_cancel_edit_doc.setOnClickListener(view ->
                handleEditDocClose(viewDocumentsModel.getName(),
                        viewDocumentsModel.getDescription(),
                        viewDocumentsModel.getExpiration_date(),
                        tv_doc_name, tv_description, tv_exp_date, dialog));
        btn_close_edit_docs.setOnClickListener(view ->
                handleEditDocClose(viewDocumentsModel.getName(),
                        viewDocumentsModel.getDescription(),
                        viewDocumentsModel.getExpiration_date(),
                        tv_doc_name, tv_description, tv_exp_date, dialog));
        btn_save_tag.setOnClickListener(v -> {
            if (Objects.requireNonNull(tv_doc_name.getText()).toString().isEmpty()
                    && Objects.requireNonNull(tv_description.getText()).toString().isEmpty()) {
                AndroidUtils.showAlert(
                        "Please enter the Document name, Description", getActivity());
            } else if (Objects.requireNonNull(tv_description.getText()).toString().isEmpty()) {
                AndroidUtils.showAlert("Please enter the Description", getActivity());
            } else if (Objects.requireNonNull(tv_document_name.getText()).toString().isEmpty()) {
                AndroidUtils.showAlert("Please enter the Document name", getActivity());
            } else {
                callUpdateDocumentWebservice(
                        Objects.requireNonNull(tv_doc_name.getText()).toString(),
                        tv_description.getText().toString(),
                        AndroidUtils.convertAnyDateToDDMMYYYY(tv_exp_date.getText().toString()),
                        viewDocumentsModel.getId());
            }
        });
        dialog.setOnDismissListener(d -> cl_document.setAlpha(1.0f));
        dialog.setCancelable(false);
        dialog.setView(view_edit_documents);
        Dialog = dialog;
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void callUpdateDocumentWebservice(String name, String description,
                                              String expiration_date, String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", name);
            jsonObject.put("description", description);
            jsonObject.put("expiration_date", expiration_date);
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.PUT,
                    "v3/document/" + id, "Update Documents", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    public static String replaceLastDotWithSlash(String input) {
        int lastIndex = input.lastIndexOf('.');
        if (lastIndex == -1) return input;
        return input.substring(0, lastIndex) + '/' + input.substring(lastIndex + 1);
    }

    @Override
    public void Display_Document(ViewDocumentsModel viewDocumentsModel) {
        CONTENT_TYPE = viewDocumentsModel.getContent_type();
        Log.d("IMage_name_content", CONTENT_TYPE);
        tempDocModel = viewDocumentsModel;
        if (DOCUMENT_TYPE_TAG.equals("Deleted")) {
            Deleted_Document(viewDocumentsModel, "view");
        } else {
            callDisplayDocumentWebservice(viewDocumentsModel.getId());
        }
        doc_name = "";
        doc_name = viewDocumentsModel.getName();
    }

    public void Deleted_Document(ViewDocumentsModel viewDocumentsModel, String delete) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("docid", viewDocumentsModel.getId());
            jsonObject.put("doctype", viewDocumentsModel.getDoc_type());
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "docs/deleted/" + delete, "Deleted Documents", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    public void ViewEncryptedDoc(String doc_id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("docid", doc_id);
            WebServiceHelper.callEmailHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "v3/decrypt", "View Encrypted Doc", jsonObject.toString());
            Log.d("Token111", Constants.TOKEN);
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    public interface OnConfirmClickListener {
        void onYesClick();

        void onNoClick();
    }

    public void showConfirmDialog(Context context, String message, String title,
                                  com.digicoffer.lauditor.Documents.Documents.OnConfirmClickListener listener) {
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
            if (message.equals("Are you sure to permanently delete document?")
                    || message.equals("Are you sure you want to delete Document?")) {
                btn_no.setTextColor(context.getColor(R.color.white));
                btn_yes.setTextColor(context.getColor(R.color.black));
                btn_yes.setBackground(context.getDrawable(R.drawable.yes_button_red_button));
                btn_no.setBackground(context.getDrawable(R.drawable.no_button_green_button));
            } else {
                btn_yes.setTextColor(context.getColor(R.color.white));
                btn_no.setTextColor(context.getColor(R.color.black));
                btn_no.setBackground(context.getDrawable(R.drawable.yes_button_red_button));
                btn_yes.setBackground(context.getDrawable(R.drawable.no_button_green_button));
            }
            AlertDialog dialog = dialogBuilder.create();
            dialog.setView(view);
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

    @Override
    public void ViewDialog(ViewDocumentsModel viewDocumentsModel, String ViewType) {
        String deleteMsg;
        String title = "Alert!";

        switch (ViewType) {
            case "disabled":
                // isdisabled = true → download is currently DISABLED → action = ENABLE it
                title = "Confirmation";
                deleteMsg = "Are you sure you want to enable download for this document?";
                break;
            case "enabled":
                // isdisabled = false → download is currently ENABLED → action = DISABLE it
                title = "Confirmation";
                deleteMsg = "Are you sure you want to disable download for this document?";
                break;
            case "encrypt":
                title = "Alert!";
                deleteMsg = "Are you sure you want to encrypt Document?";
                break;
            case "decrypt":
                title = "Alert!";
                deleteMsg = "Are you sure you want to decrypt Document?";
                break;
            case "deleted":
                title = "Confirmation";
                deleteMsg = "Are you sure to permanently delete document?";
                break;
            case "download":
                // This ONLY fires when the user explicitly taps the Download icon
                title = "Confirmation";
                deleteMsg = "Downloading this document will remove it from the secure system.\n"
                        + "Do you wish to proceed with the download?";
                break;
            case "restore":
                title = "Alert!";
                deleteMsg = "Are you sure to restore document?";
                break;
            default:
                title = "Confirmation";
                deleteMsg = "Are you sure you want to delete Document?";
                break;
        }

        showConfirmDialog(getActivity(), deleteMsg, title,
                new com.digicoffer.lauditor.Documents.Documents.OnConfirmClickListener() {
                    @Override
                    public void onYesClick() {
                        switch (ViewType) {
                            case "disabled":
                                // Download was DISABLED → user confirmed ENABLE
                                // isdisabled must become FALSE (download is now allowed)
                                viewDocumentsModel.setIsdisabled(false);
                                viewDocumentsModel.setIs_disabled(false);
                                updateDocumentInPageItems(viewDocumentsModel);
                                disabled_doc(viewDocumentsModel.getId());
                                break;

                            case "enabled":
                                // Download was ENABLED → user confirmed DISABLE
                                // isdisabled must become TRUE (download is now blocked)
                                viewDocumentsModel.setIsdisabled(true);
                                viewDocumentsModel.setIs_disabled(true);
                                updateDocumentInPageItems(viewDocumentsModel);
                                enabled_doc(viewDocumentsModel.getId());
                                break;

                            case "encrypt":
                                viewDocumentsModel.setAdded_encryption(true);
                                viewDocumentsModel.setIs_encrypted(true);
                                updateDocumentInPageItems(viewDocumentsModel);
                                encryption_doc(viewDocumentsModel.getId());
                                break;

                            case "decrypt":
                                viewDocumentsModel.setAdded_encryption(false);
                                viewDocumentsModel.setIs_encrypted(false);
                                updateDocumentInPageItems(viewDocumentsModel);
                                decryption_doc(viewDocumentsModel.getId());
                                break;

                            case "download":
                                // Only the Download icon tap reaches here
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
                                break;
                        }
                    }

                    @Override
                    public void onNoClick() {
                        // Nothing to do — user cancelled
                    }
                });
    }

    /**
     * Updates the matching document in both pageItems and view_docs_list,
     * then notifies the adapter so Grid View (and List View) reflect the
     * new state immediately — without waiting for a server round-trip.
     */
    private void updateDocumentInPageItems(ViewDocumentsModel updatedModel) {
        // Update the master list so pagination stays correct
        for (int i = 0; i < view_docs_list.size(); i++) {
            if (view_docs_list.get(i).getId().equals(updatedModel.getId())) {
                view_docs_list.set(i, updatedModel);
                break;
            }
        }
        // Update the current page slice that the adapter is bound to
        for (int i = 0; i < pageItems.size(); i++) {
            if (pageItems.get(i).getId().equals(updatedModel.getId())) {
                pageItems.set(i, updatedModel);
                break;
            }
        }
        // Push the corrected slice to the adapter and redraw every visible cell
        // (notifyDataSetChanged covers both List and Grid view modes)
        if (adapter1 != null) {
            adapter1.setData(new ArrayList<>(pageItems));
            adapter1.notifyDataSetChanged();
        }
    }

    @Override
    public void Download_Document(ViewDocumentsModel viewDocumentsModel) {
        tempDocModel = viewDocumentsModel;
    }

    public void Download_Document(String docid) {
        try {
            JSONObject jsonObject = new JSONObject();
            if (!is_MergePdfClicked) {
                progress_dialog = AndroidUtils.get_progress(getActivity());
                WebServiceHelper.callHttpWebService(this, getContext(),
                        WebServiceHelper.RestMethodType.GET,
                        "v3/mergepdf/" + docid + "/download",
                        "Download Document", jsonObject.toString());
            } else if (tempDocModel.isAdded_encryption() || tempDocModel.isIs_encrypted()) {
                callDecryptApi(docid, true);
            } else {
                progress_dialog = AndroidUtils.get_progress(getActivity());
                WebServiceHelper.callHttpWebService(this, getContext(),
                        WebServiceHelper.RestMethodType.GET,
                        "v3/document/" + tempDocModel.getId() + "/download",
                        "Download Document", jsonObject.toString());
            }
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    @Override
    public void Update_Tag(ViewDocumentsModel viewDocumentsModel) {
        this.viewDocumentsModel = viewDocumentsModel;
        isUpdateTag = true;
        tags_list.clear();
        JSONArray tagArray = viewDocumentsModel.getTagslist();
        if (tagArray != null) {
            ArrayList<String> duplicateTypes = new ArrayList<>();
            for (int i = 0; i < tagArray.length(); i++) {
                try {
                    JSONObject tagObject = tagArray.getJSONObject(i);
                    String tagType = tagObject.optString("key");
                    String tagName = tagObject.optString("value");
                    boolean isDuplicateType = false;
                    for (DocumentsModel existing : tags_list) {
                        if (existing.getTag_type().equalsIgnoreCase(tagType.trim())) {
                            isDuplicateType = true;
                            break;
                        }
                    }
                    if (isDuplicateType) {
                        duplicateTypes.add(tagType);
                        continue;
                    }
                    DocumentsModel tagModel = new DocumentsModel();
                    tagModel.setTag_type(tagType);
                    tagModel.setTag_name(tagName);
                    tags_list.add(tagModel);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (!duplicateTypes.isEmpty()) {
                String duplicateList = TextUtils.join(", ", duplicateTypes);
                AndroidUtils.showAlert(
                        "This tag type already exists. Please use a different tag type: "
                                + duplicateList, getActivity());
            }
            open_add_tags_popup();
        }
    }

    public void disabled_doc(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("downloadDisabled", true);    // ← was false (wrong)
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.PATCH,
                    "v3/document/" + id, "Disabled Documents", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    public void enabled_doc(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("downloadDisabled", false);   // ← was true (wrong)
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.PATCH,
                    "v3/document/" + id, "Enabled Documents", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    public void encryption_doc(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "v3/document/encrypt/" + id, "Encrypt Documents", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    public void decryption_doc(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("get_file", false);
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "v3/document/decrypt/" + id, "Decrypt Documents", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    public void callDecryptApi(String id, boolean isDownload) {
        this.isDownload = isDownload;
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("docid", id);
            jsonObject.put("download", isDownload);
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.POST,
                    Constants.decryptUrl, "Decrypt Doc", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    public void callOtherDocViewApi(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    Constants.base_URL + "v3/document/" + id + "/view",
                    "Other Doc View", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    private void callDisplayDocumentWebservice(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            if (!is_MergePdfClicked) {
                WebServiceHelper.callHttpWebService(this, getContext(),
                        WebServiceHelper.RestMethodType.GET,
                        "v3/mergepdf/" + id + "/view",
                        "Display Documents", jsonObject.toString());
            } else {
                WebServiceHelper.callHttpWebService(this, getContext(),
                        WebServiceHelper.RestMethodType.GET,
                        "v3/document/" + id + "/view",
                        "Display Documents", jsonObject.toString());
            }
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    //    public void callfilter_client_webservices() {
//        try {
//            progress_dialog = AndroidUtils.get_progress(getActivity());
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("showPdfDocs", false);
//            if (Objects.equals(CATEGORY_TAG, "client")) {
//                jsonObject.put("category", "client");
//                jsonObject.put("clients", client_id);
//                jsonObject.put("groups", null);
//                if (!matter_id.isEmpty()) {
//                    jsonObject.put("matters", matter_id);
//                }
//            } else if (Objects.equals(CATEGORY_TAG, "firm")) {
//                JSONArray groups = new JSONArray();
//                for (int k = 0; k < selected_groups_list.size(); k++) {
//                    DocumentsModel documentsModel1 = selected_groups_list.get(k);
//                    groups.put(documentsModel1.getGroup_id());
//                }
//                jsonObject.put("category", "firm");
//                jsonObject.put("clients", "");
//                jsonObject.put("matters", "");
//                jsonObject.put("groups", groups);
//                Log.d("Group_value_num", groups.toString());
//                Log.d("Group_doc_view1", jsonObject.toString());
//            }
//            if (is_MergePdfClicked)
//                WebServiceHelper.callHttpWebService(this, getContext(),
//                        WebServiceHelper.RestMethodType.PUT,
//                        "v3/document/filter", "Display FilterDocuments", jsonObject.toString());
//            else
//                WebServiceHelper.callHttpWebService(this, getContext(),
//                        WebServiceHelper.RestMethodType.PUT,
//                        "v3/mergepdf/filter", "Display MergeFilterDocuments",
//                        jsonObject.toString());
//            Log.d("Group_doc_view1", jsonObject.toString());
//        } catch (Exception e) {
//            if (progress_dialog != null && progress_dialog.isShowing())
//                AndroidUtils.dismiss_dialog(progress_dialog);
//        }
//    }
    public void callfilter_client_webservices() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("showPdfDocs", false);

            if (Objects.equals(CATEGORY_TAG, "client")) {
                jsonObject.put("category", "client");

                // Handle "All Clients" — client_id may be comma-separated
                if (client_id.contains(",")) {
                    JSONArray clientArray = new JSONArray();
                    String[] ids = client_id.split(",");
                    for (String id : ids) {
                        if (!id.trim().isEmpty()) {
                            clientArray.put(id.trim());
                        }
                    }
                    jsonObject.put("clients", clientArray);
                } else {
                    jsonObject.put("clients", client_id);
                }

                jsonObject.put("groups", null);

                // Handle "All Matters" — matter_id may be comma-separated
                if (!matter_id.isEmpty()) {
                    if (matter_id.contains(",")) {
                        JSONArray matterArray = new JSONArray();
                        String[] ids = matter_id.split(",");
                        for (String id : ids) {
                            if (!id.trim().isEmpty()) {
                                matterArray.put(id.trim());
                            }
                        }
                        jsonObject.put("matters", matterArray);
                    } else {
                        jsonObject.put("matters", matter_id);
                    }
                }

            } else if (Objects.equals(CATEGORY_TAG, "firm")) {
                JSONArray groups = new JSONArray();

                // Handle "All Groups" — group_id may be comma-separated
                for (int k = 0; k < selected_groups_list.size(); k++) {
                    DocumentsModel documentsModel1 = selected_groups_list.get(k);
                    String groupId = documentsModel1.getGroup_id();
                    if (groupId.contains(",")) {
                        // "All Groups" selected — split and add each ID
                        String[] ids = groupId.split(",");
                        for (String id : ids) {
                            if (!id.trim().isEmpty()) {
                                groups.put(id.trim());
                            }
                        }
                    } else {
                        groups.put(groupId);
                    }
                }

                jsonObject.put("category", "firm");
                jsonObject.put("clients", "");
                jsonObject.put("matters", "");
                jsonObject.put("groups", groups);
                Log.d("Group_value_num", groups.toString());
                Log.d("Group_doc_view1", jsonObject.toString());
            }

            if (is_MergePdfClicked)
                WebServiceHelper.callHttpWebService(this, getContext(),
                        WebServiceHelper.RestMethodType.PUT,
                        "v3/document/filter", "Display FilterDocuments", jsonObject.toString());
            else
                WebServiceHelper.callHttpWebService(this, getContext(),
                        WebServiceHelper.RestMethodType.PUT,
                        "v3/mergepdf/filter", "Display MergeFilterDocuments",
                        jsonObject.toString());

            Log.d("Group_doc_view1", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
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
                WebServiceHelper.callHttpWebService(this, getContext(),
                        WebServiceHelper.RestMethodType.POST,
                        "v3/document/delete", "Delete Documents", jsonObject.toString());
            } else {
                jsonObject = new JSONObject();
                WebServiceHelper.callHttpWebService(this, getContext(),
                        WebServiceHelper.RestMethodType.DELETE,
                        "v3/mergepdf/" + id, "Delete Merge Documents", jsonObject.toString());
            }
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

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

    private void handleNotificationNavigation() {
        Bundle bundle = Constants.notificationBundle;
        String route = bundle.getString(Constants.NavKeys.ROUTE_NAME);
        client_id = "";
        selectedId = "";
        matter_id = "";

        if (route == null) {
            loadMatterDocuments();
            return;
        }

        pendingHighlightIds = bundle.getStringArrayList(Constants.NavKeys.HIGHLIGHT_IDS);
        selectedGroupsIds = bundle.getStringArrayList(Constants.NavKeys.GROUPS_ID);
        client_id = bundle.getString(Constants.NavKeys.CLIENT_ID);
        selectedId = bundle.getString(Constants.NavKeys.CLIENT_ID);
        matter_id = bundle.getString(Constants.NavKeys.MATTER_ID);
        if (matter_id != null && !matter_id.isEmpty()) {
            selectedId = bundle.getString(Constants.NavKeys.MATTER_ID);
        }

        switch (route) {
            case "document_deleted_list":
                currentModule = "delete";
                loadDeletedDocuments();
                break;
            case "document_firm_list":
                currentModule = "firm";
                loadFirmDocuments();
                callfilter_client_webservices();
                break;
            case "document_matter_list":
                currentModule = "matter";
                loadMatterDocuments();
                callfilter_client_webservices();
                break;
            case "document_client_list":
                currentModule = "client";
                loadClientDocuments();
                callfilter_client_webservices();
                break;
            default:
                loadMatterDocuments();
                break;
        }
        Constants.isFromNotification = false;
        Constants.notificationBundle.clear();
    }
}