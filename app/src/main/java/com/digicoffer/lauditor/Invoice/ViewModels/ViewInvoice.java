package com.digicoffer.lauditor.Invoice.ViewModels;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.showDatePicker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.digicoffer.lauditor.Documents.Models.MattersModel;
import com.digicoffer.lauditor.Invoice.Adapters.LineItemAdapter;
import com.digicoffer.lauditor.Invoice.Adapters.ViewInvoiceAdapter;
import com.digicoffer.lauditor.Invoice.Models.InvoiceModel;
import com.digicoffer.lauditor.Invoice.Models.LineItemModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.DescriptionValidation;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.FileDownloader;
import com.digicoffer.lauditor.CommonFiles.FileSelection.Filecosen;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ViewInvoice extends Fragment
        implements AsyncTaskCompleteListener, ViewInvoiceAdapter.InvoiceActionListener {

    // ── Mode constants ────────────────────────────────────────────────────────
    private static final String MODE_LIST = "LIST";
    private static final String MODE_CREATE = "CREATE";
    CommonSpinnerAdapter ClientAdapter, Matteradapter;
    private static final String MODE_EDIT = "EDIT";
    private static final String MODE_VIEW = "VIEW";

    // ── Currency list ─────────────────────────────────────────────────────────
    private static final String[] CURRENCY_LIST = {
            "AUD", "BHD", "CAD", "EUR", "INR", "JPY", "KWD", "GBP", "CHF", "USD"
    };

    private String currentMode = MODE_LIST;
    private InvoiceModel editingModel = null;

    // ── Suggested Invoice ID ──────────────────────────────────────────────────
    private int suggestedInvoiceSequence = 0;
    private String suggestedInvoiceCode = "";

    // ── Switch row ────────────────────────────────────────────────────────────
    private View tv_switchView, tv_switchCreate;
    private TextView tv_switchView_title;
    private ImageView tv_switchView_arrow;

    // ── LIST section ──────────────────────────────────────────────────────────
    private LinearLayout ll_view_invoice_section;
    private RecyclerView rv_invoice_list;
    private TextInputEditText et_search_invoice;

    // ── EMPTY STATE ───────────────────────────────────────────────────────────
    private LinearLayout ll_empty_state;

    // ── CREATE / EDIT / VIEW form ─────────────────────────────────────────────
    private View v_create_form;

    // Logo
    private LinearLayout ll_upload_logo;
    private CardView cv_logo_preview;
    private ImageView iv_logo, iv_remove_logo;
    private Uri logoUri = null;
    private boolean include_logo = true;
    private String logoUrl = "";
    private boolean pendingLogoUpload = false;

    // Left column
    private TextInputEditText et_invoice_no;
    private TextView invoice_name, invoice_date, invoice_due_date,
            tv_gst, tv_pan, tv_client_gst, tv_client_pan,
            tv_select_client, tv_client_address;

    AppCompatButton tv_invoice_date, tv_due_date;

    // Right column
    private TextInputEditText et_gst, et_pan, et_client_gst, et_client_pan;
    private TextInputEditText et_client_address;
    private TextView tv_address_count, tv_additional_details;

    // Line items
    private RecyclerView rv_line_items;
    private TextView cv_add_row;
    private LineItemAdapter lineItemAdapter;
    private ArrayList<LineItemModel> lineItems = new ArrayList<>();

    // Currency
    private View ll_sp_currency;
    private View sp_currency_list;
    private String selectedCurrency = "INR";

    // ── Totals ────────────────────────────────────────────────────────────────
    private TextView tv_sub_total;
    private TextView tv_discount_value;   // shows computed discount amount
    private TextView tv_tax_value;         // shows computed tax amount
    private TextView tv_total;
    private TextInputEditText et_discount;
    private TextInputEditText et_tax;
    private TextInputEditText et_additional_details;
    private TextView tv_additional_count;

    // Form Buttons
    private AppCompatButton btn_cancel, btn_save;

    // ── Data ──────────────────────────────────────────────────────────────────
    private ArrayList<InvoiceModel> invoiceList = new ArrayList<>();
    private ViewInvoiceAdapter invoiceAdapter;
    private Dialog progressDialog;

    // ── Camera / Gallery launchers ────────────────────────────────────────────
    private Uri cameraImageUri;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> requestCameraPermission;

    // ── Client spinner ────────────────────────────────────────────────────────
    private View ll_sp_client, ll_sp_matter, ll_matter;
    ListView sp_client_list, sp_matter_list;

    // Single source of truth for selected client
    String client_id = "";
    String client_type = "consumer";
    String client_name = "";
    String matter_id = "";
    String matter_name = "";
    String matter_type = "";
    Boolean isClientchecked = true;
    Boolean isMatterchecked = true;

    ArrayList<com.digicoffer.lauditor.Documents.Models.ClientsModel> clientsList = new ArrayList<>();
    ArrayList<com.digicoffer.lauditor.Documents.Models.MattersModel> mattersList = new ArrayList<>();
    ArrayList<com.digicoffer.lauditor.Documents.Models.ClientsModel> CorpClientsList = new ArrayList<>();

    TextView et_sp_client, et_sp_matter, tv_select_matter;
    ImageView iv_clear_client, iv_drop_down_client;
    ImageView iv_clear_matter, iv_drop_down_matter;

    // ── Edit: replace_original flag ───────────────────────────────────────────
    private boolean replaceOriginal = false;

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(), success -> {
                    if (success && cameraImageUri != null) handleLogoUri(cameraImageUri);
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null
                            && result.getData().getData() != null) {
                        handleLogoUri(result.getData().getData());
                    }
                });

        requestCameraPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), granted -> {
                    if (granted) openCamera();
                    else Toast.makeText(getContext(),
                            "Camera permission denied", Toast.LENGTH_SHORT).show();
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        android.widget.FrameLayout wrapper =
                new android.widget.FrameLayout(requireContext());
        wrapper.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        View root = inflater.inflate(R.layout.fragment_invoice_list, wrapper, false);
        bindListViews(root);
        wrapper.addView(root);

        v_create_form = inflater.inflate(R.layout.create_invoice, wrapper, false);
        bindFormViews(v_create_form);
        wrapper.addView(v_create_form);

        setupSwitchRow(root);
        setupFormListeners();
        setupLineItemsTable();

        switchToMode(MODE_LIST);
        return wrapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bind List views
    // ─────────────────────────────────────────────────────────────────────────

    private void bindListViews(View v) {
        if (tv_switchView != null) {
            tv_switchView_title = tv_switchView.findViewById(R.id.tv_title);
            int arrowId = tv_switchView.getResources()
                    .getIdentifier("iv_arrow", "id",
                            tv_switchView.getContext().getPackageName());
            if (arrowId != 0) tv_switchView_arrow = tv_switchView.findViewById(arrowId);
        }

        ll_view_invoice_section = v.findViewById(R.id.ll_view_invoice_section);
        rv_invoice_list = v.findViewById(R.id.rv_invoice_list);

        // ── EMPTY STATE ───────────────────────────────────────────────────────
        ll_empty_state = v.findViewById(R.id.ll_empty_state);

        View tl_et_search_invoice = v.findViewById(R.id.et_search_invoice);
        et_search_invoice = tl_et_search_invoice.findViewById(R.id.et_Search);
        et_search_invoice.setHint(R.string.search_invoices);
        tv_switchView = v.findViewById(R.id.tv_switchView);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bind Form views
    // ─────────────────────────────────────────────────────────────────────────

    private void bindFormViews(View v) {
        tv_switchCreate = v.findViewById(R.id.tv_switchCreate);
        cv_logo_preview = v.findViewById(R.id.cv_logo_preview);
        iv_logo = v.findViewById(R.id.iv_logo);
        iv_remove_logo = v.findViewById(R.id.iv_remove_logo);
        ll_upload_logo = v.findViewById(R.id.ll_upload_logo);

        et_invoice_no = v.findViewById(R.id.et_invoice_no);

        invoice_name = v.findViewById(R.id.tv_invoice_no);
        invoice_name.setText("Invoice No");
        invoice_date = v.findViewById(R.id.invoice_date);
        invoice_date.setText("Invoice Date");
        invoice_due_date = v.findViewById(R.id.invoice_due_date);
        invoice_due_date.setText("Due Date");

        tv_invoice_date = v.findViewById(R.id.tv_invoice_date);
        tv_invoice_date.setHint("Invoice Date");

        tv_due_date = v.findViewById(R.id.tv_due_date);
        tv_due_date.setHint("Due Date");

        invoice_date.setText("Invoice Date");
        tv_gst = v.findViewById(R.id.tv_gst);
        tv_gst.setText(R.string.gst);

        tv_pan = v.findViewById(R.id.tv_pan);
        tv_pan.setText(R.string.pan);

        tv_client_gst = v.findViewById(R.id.tv_client_gst);
        tv_client_gst.setText(R.string.client_gst);

        tv_client_pan = v.findViewById(R.id.tv_client_pan);
        tv_client_pan.setText(R.string.client_pan);

        tv_select_client = v.findViewById(R.id.tv_select_client);
        tv_select_client.setText(R.string.select_client);

        tv_client_address = v.findViewById(R.id.tv_client_address);
        tv_client_address.setText(R.string.client_address);

        et_gst = v.findViewById(R.id.et_gst);
        et_gst.setHint(R.string.gst);
        et_pan = v.findViewById(R.id.et_pan);
        et_pan.setHint(R.string.pan);

        et_client_gst = v.findViewById(R.id.et_client_gst);
        et_client_gst.setHint(R.string.client_gst);

        et_client_pan = v.findViewById(R.id.et_client_pan);
        et_client_pan.setHint(R.string.client_pan);

        ll_sp_client = v.findViewById(R.id.ll_sp_client);
        sp_client_list = v.findViewById(R.id.sp_client_list);

        ll_sp_matter = v.findViewById(R.id.ll_sp_matter);
        sp_matter_list = v.findViewById(R.id.sp_matter_list);

        tv_select_matter = v.findViewById(R.id.tv_select_matter);
        ll_matter = v.findViewById(R.id.ll_matter);
        ll_matter.setVisibility(GONE);

        et_sp_matter = ll_sp_matter.findViewById(R.id.tv_spinner_view);
        iv_clear_matter = ll_sp_matter.findViewById(R.id.img_clear_icon);
        iv_drop_down_matter = ll_sp_matter.findViewById(R.id.img_dropdown_icon);
        et_sp_matter.setHint(R.string.select_matters);
        tv_select_matter.setText(R.string.select_matters_optional);

        et_sp_client = ll_sp_client.findViewById(R.id.tv_spinner_view);
        iv_clear_client = ll_sp_client.findViewById(R.id.img_clear_icon);
        iv_drop_down_client = ll_sp_client.findViewById(R.id.img_dropdown_icon);
        et_sp_client.setHint(R.string.select_client);

        et_client_address = v.findViewById(R.id.et_client_address);
        et_client_address.setHint(R.string.client_address);

        tv_address_count = v.findViewById(R.id.tv_address_count);

        rv_line_items = v.findViewById(R.id.rv_line_items);
        cv_add_row = v.findViewById(R.id.btn_add);

        ll_sp_currency = v.findViewById(R.id.ll_sp_currency);
        sp_currency_list = v.findViewById(R.id.sp_currency_list);

        // ── Totals bindings ───────────────────────────────────────────────────
        tv_sub_total = v.findViewById(R.id.tv_sub_total);
        tv_discount_value = v.findViewById(R.id.tv_discount_value);
        tv_tax_value = v.findViewById(R.id.tv_tax_value);
        tv_total = v.findViewById(R.id.tv_total);
        et_discount = v.findViewById(R.id.et_discount);
        et_tax = v.findViewById(R.id.et_tax);
        et_additional_details = v.findViewById(R.id.et_additional_details);
        tv_additional_count = v.findViewById(R.id.tv_additional_count);
        tv_additional_details = v.findViewById(R.id.tv_additional_details);

        et_gst.addTextChangedListener(new Validation(et_gst));
        et_pan.addTextChangedListener(new Validation(et_pan));
        et_invoice_no.addTextChangedListener(new Validation(et_invoice_no));
        et_search_invoice.addTextChangedListener(new Validation(et_search_invoice));
        et_tax.addTextChangedListener(new Validation(et_tax));
        et_client_gst.addTextChangedListener(new Validation(et_client_gst));
        et_client_address.addTextChangedListener(new DescriptionValidation(et_client_address));
        et_client_pan.addTextChangedListener(new Validation(et_client_pan));
        et_additional_details.addTextChangedListener(new DescriptionValidation(et_additional_details));
        tv_additional_details.setText(R.string.additional_details);

        InputFilter[] filters1 = new InputFilter[]{new InputFilter.LengthFilter(250)};
        et_additional_details.setHint(R.string.additional_details);
        et_additional_details.setMaxLines(5);
        et_additional_details.setFilters(filters1);

        InputFilter[] filters = new InputFilter[]{new InputFilter.LengthFilter(150)};
        et_client_address.setMaxLines(5);
        et_client_address.setFilters(filters);

        AndroidUtils.setupModuleView(
                tv_switchCreate,
                getString(R.string.view_invoice),
                false, true, getContext(), getString(R.string.create_invoice),
                clickedView -> switchToMode(MODE_LIST));

        View cancelWrapper = v.findViewById(R.id.btn_cancel);
        if (cancelWrapper instanceof AppCompatButton) {
            btn_cancel = (AppCompatButton) cancelWrapper;
        } else if (cancelWrapper != null) {
            btn_cancel = cancelWrapper.findViewById(R.id.btn_cancel);
        }

        View saveWrapper = v.findViewById(R.id.btn_save);
        if (saveWrapper instanceof AppCompatButton) {
            btn_save = (AppCompatButton) saveWrapper;
        } else if (saveWrapper != null) {
            btn_save = saveWrapper.findViewById(R.id.btn_submit);
            if (btn_save == null) btn_save = saveWrapper.findViewById(R.id.btn_save);
        }

        Log.d("BTN_BIND", "btn_cancel=" + btn_cancel + "  btn_save=" + btn_save);

        callClientWebservice();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Currency Spinner
    // ─────────────────────────────────────────────────────────────────────────

    private void setupCurrencySpinner() {
        if (ll_sp_currency == null || sp_currency_list == null) return;

        TextView tvCurrencyDisplay = ll_sp_currency.findViewById(R.id.tv_spinner_view);
        ImageView ivDropdownCurrency = ll_sp_currency.findViewById(R.id.img_dropdown_icon);
        ImageView ivClearCurrency = ll_sp_currency.findViewById(R.id.img_clear_icon);

        if (ivClearCurrency != null) ivClearCurrency.setVisibility(View.GONE);
        if (tvCurrencyDisplay != null) tvCurrencyDisplay.setText(selectedCurrency);

        ListView lvCurrency = new ListView(requireContext());
        lvCurrency.setDivider(new android.graphics.drawable.ColorDrawable(
                Color.parseColor("#E0E0E0")));
        lvCurrency.setDividerHeight(dp(1));
        lvCurrency.setBackgroundColor(Color.WHITE);

        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<String>(
                requireContext(), android.R.layout.simple_list_item_1, CURRENCY_LIST) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setPadding(dp(16), dp(14), dp(16), dp(14));
                tv.setTextSize(14f);
                tv.setTypeface(Typeface.create("gill-sans-mt", Typeface.NORMAL));
                if (CURRENCY_LIST[position].equals(selectedCurrency)) {
                    tv.setBackgroundColor(
                            ContextCompat.getColor(requireContext(), R.color.Blue_text_color));
                    tv.setTextColor(Color.WHITE);
                    tv.setTypeface(Typeface.create("gill-sans-mt", Typeface.BOLD));
                } else {
                    tv.setBackgroundColor(Color.WHITE);
                    tv.setTextColor(Color.parseColor("#333333"));
                }
                return tv;
            }
        };

        lvCurrency.setAdapter(currencyAdapter);

        int popupWidth = ll_sp_currency.getWidth() > 0
                ? ll_sp_currency.getWidth()
                : (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.35f);
        int itemHeight = dp(48);
        int popupHeight = itemHeight * CURRENCY_LIST.length;

        android.widget.PopupWindow popupWindow = new android.widget.PopupWindow(
                lvCurrency, popupWidth, popupHeight, true);
        popupWindow.setBackgroundDrawable(
                new android.graphics.drawable.ColorDrawable(Color.WHITE));
        popupWindow.setElevation(dp(8));
        popupWindow.setOutsideTouchable(true);

        final boolean[] isCurrencyOpen = {false};

        ll_sp_currency.setOnClickListener(v -> {
            if (currentMode.equals(MODE_VIEW)) return;
            if (isCurrencyOpen[0]) {
                popupWindow.dismiss();
                isCurrencyOpen[0] = false;
                if (ivDropdownCurrency != null)
                    ivDropdownCurrency.animate().rotation(0f).setDuration(200).start();
            } else {
                int w = ll_sp_currency.getWidth() > 0
                        ? ll_sp_currency.getWidth()
                        : (int) (requireContext().getResources()
                        .getDisplayMetrics().widthPixels * 0.35f);
                popupWindow.setWidth(w);
                currencyAdapter.notifyDataSetChanged();
                popupWindow.showAsDropDown(ll_sp_currency, 0, 0);
                isCurrencyOpen[0] = true;
                if (ivDropdownCurrency != null)
                    ivDropdownCurrency.animate().rotation(180f).setDuration(200).start();
            }
        });

        popupWindow.setOnDismissListener(() -> {
            isCurrencyOpen[0] = false;
            if (ivDropdownCurrency != null)
                ivDropdownCurrency.animate().rotation(0f).setDuration(200).start();
        });

        lvCurrency.setOnItemClickListener((parent, view, position, id) -> {
            selectedCurrency = CURRENCY_LIST[position];
            if (tvCurrencyDisplay != null) tvCurrencyDisplay.setText(selectedCurrency);
            popupWindow.dismiss();
            isCurrencyOpen[0] = false;
            if (ivDropdownCurrency != null)
                ivDropdownCurrency.animate().rotation(0f).setDuration(200).start();
            currencyAdapter.notifyDataSetChanged();
            if (lineItemAdapter != null)
                lineItemAdapter.setSelectedCurrency(selectedCurrency);
            recalculateTotal();
            Log.d("CURRENCY", "Selected: " + selectedCurrency);
        });

        if (sp_currency_list != null) sp_currency_list.setVisibility(View.GONE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Logo picker
    // ─────────────────────────────────────────────────────────────────────────

    private void showLogoPickerDialog() {
        Filecosen filecosen = new Filecosen(requireActivity(),
                new Filecosen.FileChooserCallback() {
                    @Override
                    public void openCamera() {
                        if (requireContext().checkSelfPermission(
                                android.Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) {
                            ViewInvoice.this.openCamera();
                        } else {
                            requestCameraPermission.launch(android.Manifest.permission.CAMERA);
                        }
                    }

                    @Override
                    public void openGallery() {
                        ViewInvoice.this.openGallery();
                    }
                });
        filecosen.show();
    }

    private void openCamera() {
        try {
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(android.provider.MediaStore.Images.Media.TITLE,
                    "Invoice_Logo_" + System.currentTimeMillis());
            values.put(android.provider.MediaStore.Images.Media.DESCRIPTION, "Invoice Logo");
            cameraImageUri = requireActivity().getContentResolver()
                    .insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (cameraImageUri != null) cameraLauncher.launch(cameraImageUri);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Camera error: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Gallery error: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLogoUri(Uri uri) {
        logoUri = uri;
        logoUrl = "";
        include_logo = true;
        pendingLogoUpload = true;
        if (iv_logo != null) Glide.with(this).load(uri).centerCrop().into(iv_logo);
        if (ll_upload_logo != null) ll_upload_logo.setVisibility(GONE);
        if (cv_logo_preview != null) cv_logo_preview.setVisibility(VISIBLE);
        if (iv_remove_logo != null) iv_remove_logo.setVisibility(VISIBLE);
    }

    private void loadLogoFromUrl(String url) {
        if (url == null || url.isEmpty()) return;
        logoUrl = url;
        logoUri = null;
        include_logo = true;
        pendingLogoUpload = false;
        if (iv_logo != null) {
            Glide.with(this).load(url).centerCrop()
                    .placeholder(R.drawable.rectangle_grey_background)
                    .error(R.drawable.rectangle_grey_background)
                    .into(iv_logo);
        }
        if (ll_upload_logo != null) ll_upload_logo.setVisibility(GONE);
        if (cv_logo_preview != null) cv_logo_preview.setVisibility(VISIBLE);
        if (iv_remove_logo != null)
            iv_remove_logo.setVisibility(
                    currentMode.equals(MODE_VIEW) ? GONE : VISIBLE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Switch Row
    // ─────────────────────────────────────────────────────────────────────────

    private void setupSwitchRow(View root) {
        if (tv_switchView != null) {
            tv_switchView.setOnClickListener(v -> {
                if (currentMode.equals(MODE_LIST)) switchToMode(MODE_CREATE);
                else switchToMode(MODE_LIST);
            });
        }
        AndroidUtils.setupModuleView(
                tv_switchView,
                getString(R.string.create_invoice),
                true, true, getContext(), getString(R.string.view_invoice),
                clickedView -> switchToMode(MODE_CREATE));
    }

    private void switchToMode(String mode) {
        currentMode = mode;

        if (ll_view_invoice_section != null) ll_view_invoice_section.setVisibility(GONE);
        if (v_create_form != null) v_create_form.setVisibility(GONE);

        switch (mode) {
            case MODE_LIST:
                tv_switchView.setVisibility(VISIBLE);
                tv_switchCreate.setVisibility(GONE);
                if (ll_view_invoice_section != null)
                    ll_view_invoice_section.setVisibility(VISIBLE);
                setLabel("View Invoice", false);
                callInvoiceListWebservice();
                break;

            case MODE_CREATE:
                tv_switchView.setVisibility(GONE);
                tv_switchCreate.setVisibility(VISIBLE);
                editingModel = null;
                suggestedInvoiceCode = "";
                suggestedInvoiceSequence = 0;
                if (v_create_form != null) v_create_form.setVisibility(VISIBLE);
                setLabel("Create Invoice", true);
                clearForm();
                setFormEditable(true);
                updateSaveButtonLabel();
                callSuggestInvoiceIdWebservice();
                callGetLogoWebservice();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
                String todayDate = sdf.format(new Date());
                tv_invoice_date.setText(todayDate);
                break;

            case MODE_EDIT:
                if (v_create_form != null) v_create_form.setVisibility(VISIBLE);
                setLabel("Edit Invoice", true);
                setFormEditable(true);
                updateSaveButtonLabel();
                callGetLogoWebservice();
                break;

            case MODE_VIEW:
                if (v_create_form != null) v_create_form.setVisibility(VISIBLE);
                setLabel("Invoice Details", true);
                setFormEditable(false);
                updateSaveButtonLabel();
                callGetLogoWebservice();
                break;
        }
    }

    private void updateSaveButtonLabel() {
        if (btn_save == null) return;
        if (currentMode.equals(MODE_VIEW)) {
            btn_save.setText(R.string.download);
            btn_cancel.setText("Back to List");
        } else if (currentMode.equals(MODE_EDIT)) {
            btn_save.setText(R.string.update);
            btn_cancel.setText(R.string.cancel);
        } else {
            btn_save.setText(R.string.save);
            btn_cancel.setText(R.string.cancel);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Form editable / read-only toggle
    // ─────────────────────────────────────────────────────────────────────────

    private void setFormEditable(boolean editable) {
        setEditableField(et_invoice_no, editable);
        setEditableField(et_gst, editable);
        setEditableField(et_pan, editable);
        setEditableField(et_client_gst, editable);
        setEditableField(et_client_pan, editable);
        setEditableField(et_client_address, editable);
        setEditableField(et_additional_details, editable);
        setEditableField(et_discount, editable);
        setEditableField(et_tax, editable);

        if (tv_invoice_date != null) tv_invoice_date.setClickable(editable);
        if (tv_due_date != null) tv_due_date.setClickable(editable);
        if (ll_upload_logo != null) ll_upload_logo.setClickable(editable);
        if (iv_remove_logo != null) iv_remove_logo.setClickable(editable);
        if (cv_add_row != null) cv_add_row.setVisibility(editable ? VISIBLE : GONE);
        if (ll_sp_client != null) {
            ll_sp_client.setClickable(editable);
            iv_clear_client.setEnabled(editable);
            iv_drop_down_client.setEnabled(editable);
        }
        if (ll_sp_currency != null) {
            ll_sp_currency.setClickable(editable);
            ll_sp_currency.setEnabled(editable);
            ImageView ivDropdown = ll_sp_currency.findViewById(R.id.img_dropdown_icon);
            if (ivDropdown != null) ivDropdown.setVisibility(editable ? VISIBLE : GONE);
            if (!editable && sp_currency_list != null) {
                sp_currency_list.setVisibility(GONE);
            }
        }
        if (lineItemAdapter != null) lineItemAdapter.setEditable(editable);
    }

    private void setEditableField(TextInputEditText et, boolean editable) {
        if (et == null) return;
        et.setEnabled(editable);
        et.setFocusable(editable);
        et.setFocusableInTouchMode(editable);
        et.setClickable(editable);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Show / hide form buttons
    // ─────────────────────────────────────────────────────────────────────────

    private void showFormButtons(boolean editable) {
        if (v_create_form == null) return;

        View cancelInclude = v_create_form.findViewById(R.id.btn_cancel);
        View saveInclude = v_create_form.findViewById(R.id.btn_save);

        if (editable) {
            if (cancelInclude != null) cancelInclude.setVisibility(VISIBLE);
            if (saveInclude != null) saveInclude.setVisibility(VISIBLE);
        } else {
            if (cancelInclude != null) cancelInclude.setVisibility(GONE);
            if (saveInclude != null) saveInclude.setVisibility(GONE);
        }

        View existingBtnRow = v_create_form.findViewWithTag("view_btn_row");

        if (!editable) {
            if (existingBtnRow == null) {
                android.widget.ScrollView sv = (android.widget.ScrollView) v_create_form;
                LinearLayout parent = (LinearLayout) sv.getChildAt(0);
                if (parent != null) {
                    LinearLayout btnRow = new LinearLayout(requireContext());
                    btnRow.setTag("view_btn_row");
                    btnRow.setOrientation(LinearLayout.HORIZONTAL);
                    btnRow.setGravity(Gravity.CENTER);
                    LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    rowLp.topMargin = dp(20);
                    rowLp.bottomMargin = dp(24);
                    btnRow.setLayoutParams(rowLp);

                    AppCompatButton btnClose = new AppCompatButton(requireContext());
                    btnClose.setTag("btn_view_close");
                    btnClose.setText("Back to List");
                    btnClose.setTextColor(Color.BLACK);
                    btnClose.setBackgroundColor(Color.parseColor("#E0E0E0"));
                    LinearLayout.LayoutParams closeLp = new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                    closeLp.setMargins(dp(16), 0, dp(8), 0);
                    btnClose.setLayoutParams(closeLp);
                    btnClose.setOnClickListener(v -> switchToMode(MODE_LIST));
                    btnRow.addView(btnClose);

                    AppCompatButton btnDownload = new AppCompatButton(requireContext());
                    btnDownload.setTag("btn_view_download");
                    btnDownload.setText("Download");
                    btnDownload.setTextColor(Color.WHITE);
                    btnDownload.setBackgroundColor(
                            ContextCompat.getColor(requireContext(), R.color.blue));
                    LinearLayout.LayoutParams downloadLp = new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                    downloadLp.setMargins(dp(8), 0, dp(16), 0);
                    btnDownload.setLayoutParams(downloadLp);
                    btnDownload.setOnClickListener(v -> downloadInvoice());
                    btnRow.addView(btnDownload);

                    parent.addView(btnRow);
                }
            } else {
                existingBtnRow.setVisibility(VISIBLE);
                View btnClose = existingBtnRow.findViewWithTag("btn_view_close");
                View btnDownload = existingBtnRow.findViewWithTag("btn_view_download");
                if (btnClose != null) btnClose.setOnClickListener(v -> switchToMode(MODE_LIST));
                if (btnDownload != null) btnDownload.setOnClickListener(v -> downloadInvoice());
            }
        } else {
            if (existingBtnRow != null) existingBtnRow.setVisibility(GONE);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Download Invoice
    // ─────────────────────────────────────────────────────────────────────────

    private void downloadInvoice() {
        if (editingModel == null) {
            AndroidUtils.showAlert("No invoice selected.", getActivity());
            return;
        }
        String docId = editingModel.getDocid();
        if (docId == null || docId.isEmpty()) {
            AndroidUtils.showAlert("Invoice document ID not found.", getActivity());
            return;
        }
        callDownloadInvoiceWebservice(docId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Label helper
    // ─────────────────────────────────────────────────────────────────────────

    private void setLabel(String label, boolean expanded) {
        if (tv_switchView_title != null) tv_switchView_title.setText(label);
        if (tv_switchView_arrow != null) {
            tv_switchView_arrow.animate()
                    .rotation(expanded ? 180f : 0f)
                    .setDuration(200).start();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Client webservice
    // ─────────────────────────────────────────────────────────────────────────

    private void callClientWebservice() {
        try {
            progressDialog = AndroidUtils.get_progress((Activity) getContext());
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/client/all/list", "Clients List",
                    new JSONObject().toString());
        } catch (Exception e) {
            dismissProgress();
        }
    }

    private void callCorpClientWebservice() {
        try {
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/corporate/list", "Corp Clients List",
                    new JSONObject().toString());
        } catch (Exception e) {
            dismissProgress();
        }
    }

    private void loadClients(JSONObject data) throws JSONException {
        JSONArray relationships = data.getJSONArray("relationships");
        clientsList.clear();
        for (int i = 0; i < relationships.length(); i++) {
            JSONObject obj = relationships.getJSONObject(i);
            com.digicoffer.lauditor.Documents.Models.ClientsModel cm =
                    new com.digicoffer.lauditor.Documents.Models.ClientsModel();
            cm.setId(obj.getString("id"));
            cm.setName(obj.getString("name"));
            cm.setType(obj.getString("type"));
            clientsList.add(cm);
        }
    }

    private void loadCorpClients(JSONObject data) throws JSONException {
        JSONArray relationships = data.getJSONArray("relationships");
        CorpClientsList.clear();
        for (int i = 0; i < relationships.length(); i++) {
            JSONObject obj = relationships.getJSONObject(i);
            com.digicoffer.lauditor.Documents.Models.ClientsModel cm =
                    new com.digicoffer.lauditor.Documents.Models.ClientsModel();
            cm.setId(obj.getString("id"));
            cm.setName(obj.getString("name"));
            cm.setType(obj.getString("type").equals("consumer") ? "consumer" : "corporate");
            CorpClientsList.add(cm);
        }
        clientsList.addAll(CorpClientsList);
        if (clientsList.isEmpty()) {
            if (ll_sp_client != null) ll_sp_client.setVisibility(GONE);
        } else {
            initUI(clientsList);
            if (!client_name.isEmpty() && et_sp_client != null) {
//                boolean isVisible = sp_client_list.getVisibility() == VISIBLE;
//                AndroidUtils.DisplaySpinnerView(
//                        sp_client_list, et_sp_client, client_name,
//                        iv_drop_down_client, iv_clear_client, !isVisible,ClientAdapter,"Search Client");
            }
        }
    }

    private void initUI(
            ArrayList<com.digicoffer.lauditor.Documents.Models.ClientsModel> list) {
        ClientAdapter =
                new CommonSpinnerAdapter((Activity) getContext(), list);
        sp_client_list.setAdapter(ClientAdapter);
        sp_client_list.setOnItemClickListener((parent, view, position, id) -> {
            com.digicoffer.lauditor.Documents.Models.ClientsModel selectedClientItem =
                    (com.digicoffer.lauditor.Documents.Models.ClientsModel) parent.getItemAtPosition(position);
            client_id = selectedClientItem.getId();
            client_name = selectedClientItem.getName();
            client_type = selectedClientItem.getType() != null
                    ? selectedClientItem.getType() : "consumer";
            Log.d("CLIENT_SELECTED",
                    "id=" + client_id + "  name=" + client_name + "  type=" + client_type);
            AndroidUtils.DisplaySpinnerView(
                    sp_client_list, et_sp_client, client_name,
                    iv_drop_down_client, iv_clear_client, false, ClientAdapter, "Search Clients");
            callMatterList();
            isClientchecked = true;
        });
    }

    private void initMatterUI() {
        Matteradapter =
                new CommonSpinnerAdapter(getActivity(), this.mattersList);
        Log.i("ArrayList", "Info:" + mattersList);
        sp_matter_list.setAdapter(Matteradapter);
        AndroidUtils.LoadList(sp_matter_list, getContext(), mattersList.size(), true);
        isMatterchecked = true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Form Listeners
    // ─────────────────────────────────────────────────────────────────────────

    private void setupFormListeners() {

        setupCurrencySpinner();

        if (ll_sp_client != null) {
            ll_sp_client.setOnClickListener(v -> {
                if (currentMode.equals(MODE_VIEW)) return;
                if (clientsList.isEmpty()) {
                    if (sp_client_list != null) sp_client_list.setVisibility(GONE);
                } else {
                    boolean isVisible = sp_client_list.getVisibility() == VISIBLE;
                    AndroidUtils.DisplaySpinnerView(
                            sp_client_list, et_sp_client, client_name,
                            iv_drop_down_client, iv_clear_client, !isVisible, ClientAdapter, "Search Client");
                    AndroidUtils.display_listview(isClientchecked, sp_client_list);
                }
                isClientchecked = !isClientchecked;
            });
        }

        if (ll_sp_matter != null) {
            ll_sp_matter.setOnClickListener(v -> {
                if (currentMode.equals(MODE_VIEW)) return;
                if (mattersList.isEmpty()) {
                    if (sp_matter_list != null) sp_matter_list.setVisibility(GONE);
                } else {
                    boolean isVisible = sp_matter_list.getVisibility() == VISIBLE;
                    AndroidUtils.DisplaySpinnerView(sp_matter_list, et_sp_matter, matter_name,
                            iv_drop_down_matter, iv_clear_matter, !isVisible, Matteradapter, "Search Matter");
                    AndroidUtils.display_listview(isMatterchecked, sp_matter_list);
                }
                isMatterchecked = !isMatterchecked;
            });
        }

        sp_matter_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MattersModel selectedMatter = (MattersModel) parent.getItemAtPosition(position);
                matter_id = selectedMatter.getId();
                matter_name = selectedMatter.getTitle();
                matter_type = selectedMatter.getType();
                et_sp_matter.setText(matter_name);
                AndroidUtils.DisplaySpinnerView(sp_matter_list, et_sp_matter, matter_name,
                        iv_drop_down_matter, iv_clear_matter, false, Matteradapter, "Search Matter");
                isMatterchecked = true;
            }
        });

        if (iv_clear_client != null) {
            iv_clear_client.setOnClickListener(v -> {
                matter_id = "";
                matter_name = "";
                matter_type = "";
                client_id = "";
                client_name = "";
                client_type = "consumer";
                AndroidUtils.DisplaySpinnerView(
                        sp_client_list, et_sp_client, matter_name,
                        iv_drop_down_client, iv_clear_client, false, ClientAdapter, "Search Client");
                Log.d("CLIENT_SELECTED", "Client cleared");
            });
        }

        iv_clear_matter.setOnClickListener(v -> {
            matter_id = "";
            AndroidUtils.DisplaySpinnerView(
                    sp_matter_list, et_sp_matter, matter_name,
                    iv_drop_down_matter, iv_clear_matter, false, Matteradapter, "Search Matter");
            Log.d("CLIENT_SELECTED", "Matter cleared");
        });

        if (ll_upload_logo != null)
            ll_upload_logo.setOnClickListener(v -> {
                if (!currentMode.equals(MODE_VIEW)) showLogoPickerDialog();
            });

        if (iv_remove_logo != null) {
            iv_remove_logo.setOnClickListener(v -> {
                if (currentMode.equals(MODE_VIEW)) return;
                logoUri = null;
                logoUrl = "";
                include_logo = false;
                pendingLogoUpload = false;
                if (iv_logo != null) iv_logo.setImageDrawable(null);
                if (cv_logo_preview != null) cv_logo_preview.setVisibility(GONE);
                if (ll_upload_logo != null) ll_upload_logo.setVisibility(VISIBLE);
            });
        }

        if (tv_invoice_date != null)
            tv_invoice_date.setOnClickListener(v -> {
                if (!currentMode.equals(MODE_VIEW)) showDatePicker(tv_invoice_date, true);
            });

        if (tv_due_date != null)
            tv_due_date.setOnClickListener(v -> {
                if (!currentMode.equals(MODE_VIEW)) showDatePicker(tv_due_date, false);
            });

        if (et_client_address != null && tv_address_count != null)
            et_client_address.addTextChangedListener(makeCounter(tv_address_count, 150));
        if (et_additional_details != null && tv_additional_count != null)
            et_additional_details.addTextChangedListener(makeCounter(tv_additional_count, 250));

        // ── Total watcher: recalculate whenever discount or tax % changes ─────
        TextWatcher totalWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                recalculateTotal();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        if (et_discount != null) et_discount.addTextChangedListener(totalWatcher);
        if (et_tax != null) et_tax.addTextChangedListener(totalWatcher);

        // ── Search with empty-state support ───────────────────────────────────
        if (et_search_invoice != null) {
            et_search_invoice.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int st, int c, int a) {
                }

                @Override
                public void onTextChanged(CharSequence s, int st, int b, int c) {
                    if (invoiceAdapter != null) {
                        invoiceAdapter.getFilter().filter(s, count -> {
                            updateEmptyState(count == 0);
                        });
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        if (btn_cancel != null)
            btn_cancel.setOnClickListener(v -> {
                clearForm();
                switchToMode(MODE_LIST);
            });

        if (btn_save != null)
            btn_save.setOnClickListener(v -> {
                if (MODE_VIEW.equals(currentMode) && editingModel != null) {
                    downloadInvoice();
                } else {
                    if (!validateForm()) return;
                    if (MODE_EDIT.equals(currentMode) && editingModel != null) {
                        showSaveChangesDialog();
                    } else {
                        submitForm(false);
                    }
                }
            });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Empty State helper
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Shows or hides the empty state view and the RecyclerView accordingly.
     *
     * @param isEmpty true  -> show empty state, hide RecyclerView
     *                false -> hide empty state, show RecyclerView
     */
    private void updateEmptyState(boolean isEmpty) {
        if (ll_empty_state == null || rv_invoice_list == null) return;
        ll_empty_state.setVisibility(isEmpty ? VISIBLE : GONE);
        rv_invoice_list.setVisibility(isEmpty ? GONE : VISIBLE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Save Changes Dialog (Edit mode)
    // ─────────────────────────────────────────────────────────────────────────

    private void showSaveChangesDialog() {
        if (!isAdded() || getActivity() == null) return;

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        int p = dp(20);
        root.setPadding(p, p, p, p);

        // Header
        LinearLayout headerRow = new LinearLayout(requireContext());
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText("Save Changes");
        tvTitle.setTextSize(18f);
        tvTitle.setTextColor(Color.parseColor("#1A3A6B"));
        Typeface typeface1 = ResourcesCompat.getFont(getContext(), R.font.gill_sans_bold);
        tvTitle.setTypeface(typeface1);
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        headerRow.addView(tvTitle);

        ImageView ivClose = new ImageView(requireContext());
        ivClose.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        ivClose.setColorFilter(Color.GRAY);
        ivClose.setLayoutParams(new LinearLayout.LayoutParams(dp(24), dp(24)));
        headerRow.addView(ivClose);
        root.addView(headerRow);

        // Divider
        View divider = new View(requireContext());
        divider.setBackgroundColor(Color.parseColor("#E0E0E0"));
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        divLp.topMargin = dp(14);
        divLp.bottomMargin = dp(14);
        divider.setLayoutParams(divLp);
        root.addView(divider);

        // Question
        TextView tvQuestion = new TextView(requireContext());
        Typeface typeface2 = ResourcesCompat.getFont(getContext(), R.font.gill_sans);
        tvQuestion.setTypeface(typeface2);
        tvQuestion.setText("Do you want to replace the existing invoice or save both?");
        tvQuestion.setTextSize(14f);
        tvQuestion.setTextColor(Color.DKGRAY);
        LinearLayout.LayoutParams qLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        qLp.bottomMargin = dp(12);
        tvQuestion.setLayoutParams(qLp);
        root.addView(tvQuestion);

        // Options card
        LinearLayout optionsCard = new LinearLayout(requireContext());
        optionsCard.setOrientation(LinearLayout.VERTICAL);
        optionsCard.setBackgroundColor(Color.parseColor("#F5F5F5"));
        int op = dp(14);
        optionsCard.setPadding(op, op, op, op);

        // Replace row
        LinearLayout replaceRow = new LinearLayout(requireContext());
        replaceRow.setOrientation(LinearLayout.HORIZONTAL);
        replaceRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams replaceRowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        replaceRowLp.bottomMargin = dp(12);
        replaceRow.setLayoutParams(replaceRowLp);

        ImageView ivReplace = new ImageView(requireContext());
        ivReplace.setImageResource(R.drawable.recycle_icon);
        ivReplace.clearColorFilter();
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(28), dp(28));
        iconLp.setMargins(0, 0, dp(10), 0);
        ivReplace.setLayoutParams(iconLp);
        replaceRow.addView(ivReplace);

        TextView tvReplace = new TextView(requireContext());
        Typeface typeface3 = ResourcesCompat.getFont(getContext(), R.font.gill_sans);
        tvReplace.setTypeface(typeface3);
        tvReplace.setTextSize(13f);
        tvReplace.setTextColor(Color.DKGRAY);
        android.text.SpannableString replaceSpan =
                new android.text.SpannableString(
                        "Replace: Updates the current invoice with your changes");
        replaceSpan.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, 8,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvReplace.setText(replaceSpan);
        replaceRow.addView(tvReplace);
        optionsCard.addView(replaceRow);

        // Save Both row
        LinearLayout saveBothRow = new LinearLayout(requireContext());
        saveBothRow.setOrientation(LinearLayout.HORIZONTAL);
        saveBothRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView ivSaveBoth = new ImageView(requireContext());
        ivSaveBoth.setImageResource(R.drawable.blue_copy);
        ivSaveBoth.clearColorFilter();
        LinearLayout.LayoutParams iconLp2 = new LinearLayout.LayoutParams(dp(28), dp(28));
        iconLp2.setMargins(0, 0, dp(10), 0);
        ivSaveBoth.setLayoutParams(iconLp2);
        saveBothRow.addView(ivSaveBoth);

        TextView tvSaveBoth = new TextView(requireContext());
        Typeface typeface4 = ResourcesCompat.getFont(getContext(), R.font.gill_sans);
        tvSaveBoth.setTypeface(typeface4);
        tvSaveBoth.setTextSize(13f);
        tvSaveBoth.setTextColor(Color.DKGRAY);
        android.text.SpannableString saveBothSpan =
                new android.text.SpannableString(
                        "Save Both: Creates a new invoice while keeping the original");
        saveBothSpan.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, 10,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvSaveBoth.setText(saveBothSpan);
        saveBothRow.addView(tvSaveBoth);
        optionsCard.addView(saveBothRow);

        LinearLayout.LayoutParams optionsCardLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        optionsCardLp.bottomMargin = dp(8);
        optionsCard.setLayoutParams(optionsCardLp);
        root.addView(optionsCard);

        // Button Row
        LinearLayout btnRow = new LinearLayout(requireContext());
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END);
        LinearLayout.LayoutParams btnRowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnRowLp.topMargin = dp(20);
        btnRowLp.bottomMargin = dp(8);
        btnRow.setLayoutParams(btnRowLp);

        // Replace button
        AppCompatButton btnReplace = new AppCompatButton(requireContext());
        btnReplace.setText("Replace");
        btnReplace.setAllCaps(false);
        btnReplace.setTextColor(Color.WHITE);
        btnReplace.setTextSize(14f);
        Typeface typeface6 = ResourcesCompat.getFont(getContext(), R.font.gill_sans);
        btnReplace.setTypeface(typeface6);
        btnReplace.setBackgroundColor(Color.parseColor("#757575"));
        Drawable replaceIcon =
                ContextCompat.getDrawable(requireContext(), R.drawable.recycle_icon);
        if (replaceIcon != null) {
            replaceIcon = replaceIcon.mutate();
            replaceIcon.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
            replaceIcon.setBounds(0, 0, dp(18), dp(18));
        }
        btnReplace.setCompoundDrawables(replaceIcon, null, null, null);
        btnReplace.setCompoundDrawablePadding(dp(6));
        LinearLayout.LayoutParams replaceBtnLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(48));
        replaceBtnLp.setMargins(0, 0, dp(10), 0);
        btnReplace.setLayoutParams(replaceBtnLp);
        btnReplace.setPadding(dp(16), 0, dp(16), 0);

        // Save Both button
        AppCompatButton btnSaveBoth = new AppCompatButton(requireContext());
        btnSaveBoth.setText("Save Both");
        btnSaveBoth.setAllCaps(false);
        btnSaveBoth.setTextColor(Color.WHITE);
        btnSaveBoth.setTextSize(14f);
        Typeface typeface5 = ResourcesCompat.getFont(getContext(), R.font.gill_sans);
        btnSaveBoth.setTypeface(typeface5);
        btnSaveBoth.setBackgroundColor(Color.parseColor("#1A3A6B"));
        Drawable saveBothIcon =
                ContextCompat.getDrawable(requireContext(), R.drawable.blue_copy);
        if (saveBothIcon != null) {
            saveBothIcon = saveBothIcon.mutate();
            saveBothIcon.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
            saveBothIcon.setBounds(0, 0, dp(18), dp(18));
        }
        btnSaveBoth.setCompoundDrawables(saveBothIcon, null, null, null);
        btnSaveBoth.setCompoundDrawablePadding(dp(6));
        LinearLayout.LayoutParams saveBothBtnLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(48));
        btnSaveBoth.setLayoutParams(saveBothBtnLp);
        btnSaveBoth.setPadding(dp(16), 0, dp(16), 0);

        btnRow.addView(btnReplace);
        btnRow.addView(btnSaveBoth);
        root.addView(btnRow);

        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                .setView(root).create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ivClose.setOnClickListener(v -> dialog.dismiss());
        btnReplace.setOnClickListener(v -> {
            dialog.dismiss();
            submitForm(true);
        });
        btnSaveBoth.setOnClickListener(v -> {
            dialog.dismiss();
            submitForm(false);
        });

        dialog.show();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Submit flow
    // ─────────────────────────────────────────────────────────────────────────

    private void submitForm(boolean replace) {
        this.replaceOriginal = replace;
        if (pendingLogoUpload && logoUri != null) {
            callUploadLogoWebservice(logoUri);
        } else {
            saveInvoice();
        }
    }

    private void saveInvoice() {
        try {
            JSONObject payload = buildPayload();
            Log.d("INVOICE_PAYLOAD", payload.toString());
            if (MODE_EDIT.equals(currentMode) && editingModel != null) {
                callPatchInvoiceWebservice(editingModel.getId(), payload);
            } else {
                callCreateInvoiceWebservice(payload);
            }
        } catch (JSONException e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Build payload
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressLint("DefaultLocale")
    private JSONObject buildPayload() throws JSONException {
        JSONObject payload = new JSONObject();

        String invoiceCodeToSend = suggestedInvoiceCode.isEmpty()
                ? getText(et_invoice_no) : suggestedInvoiceCode;
        payload.put("invoice_code", invoiceCodeToSend);
        payload.put("firm_gst_no", getText(et_gst));
        payload.put("firm_pan_no", getText(et_pan));
        payload.put("client_gst_no", getText(et_client_gst));
        payload.put("client_pan_no", getText(et_client_pan));
        payload.put("name", client_name.isEmpty() ? "Invoice" : client_name);
        payload.put("billto", getText(et_client_address));
        payload.put("notes", getText(et_additional_details));
        payload.put("currency_code", selectedCurrency);
        payload.put("include_logo", include_logo);
        payload.put("matters", new JSONArray());

        if (!MODE_EDIT.equals(currentMode)) {
            payload.put("invoice_sequence", suggestedInvoiceSequence);
        }

        String invoiceDateApi = tv_invoice_date.getTag() != null
                ? tv_invoice_date.getTag().toString()
                : AndroidUtils.convertAnyDateToYYYYMMDD(tv_invoice_date.getText().toString());
        String dueDateApi = tv_due_date.getTag() != null
                ? tv_due_date.getTag().toString()
                : AndroidUtils.convertAnyDateToYYYYMMDD(tv_due_date.getText().toString());

        JSONObject info = new JSONObject();
        info.put("date", invoiceDateApi);
        info.put("duedate", dueDateApi);
        payload.put("info", info);

        JSONArray invoiceItems = new JSONArray();
        for (LineItemModel item : lineItems) {
            JSONObject obj = new JSONObject();
            obj.put("name", item.getName() != null ? item.getName() : "");
            obj.put("unitPrice", (int) item.getUnitPrice());
            obj.put("quantity", item.getQuantity() > 0 ? item.getQuantity() : 1);
            invoiceItems.put(obj);
        }
        payload.put("invoice_items", invoiceItems);

        double subTotal = computeSubTotal();
        double taxPct = parseDouble(getText(et_tax));
        double discPct = parseDouble(getText(et_discount));

        JSONObject taxObj = new JSONObject();
        taxObj.put("tax_type", "Tax");
        taxObj.put("amount", subTotal);
        taxObj.put("tax", taxPct);
        payload.put("tax_items", new JSONArray().put(taxObj));

        JSONObject discObj = new JSONObject();
        discObj.put("discount_type", "Discount");
        discObj.put("amount", subTotal);
        discObj.put("discount", discPct);
        payload.put("discounts", new JSONArray().put(discObj));

        JSONObject clientObj = new JSONObject();
        if (client_id != null && !client_id.isEmpty()) {
            clientObj.put("id", client_id);
            clientObj.put("type", client_type != null ? client_type : "consumer");
        }
        payload.put("client", new JSONArray().put(clientObj));

        JSONObject matterobj = new JSONObject();
        if (matter_id != null && !matter_id.isEmpty()) {
            matterobj.put("id", matter_id);
            matterobj.put("name", matter_name != null ? matter_name : "");
            matterobj.put("type", matter_type != null ? matter_type : "legal");
        }
        payload.put("matters", new JSONArray().put(matterobj));

        Log.d("CLIENT_PAYLOAD",
                "client_id=" + client_id + "  type=" + client_type + "  name=" + client_name);

        if (MODE_EDIT.equals(currentMode)) {
            payload.put("replace_original", replaceOriginal);
        }

        return payload;
    }

    private double computeSubTotal() {
        double sub = 0;
        for (LineItemModel item : lineItems) sub += item.getAmount();
        return sub;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API Calls
    // ─────────────────────────────────────────────────────────────────────────

    private void callInvoiceListWebservice() {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/invoice", "Invoice List",
                    new JSONObject().toString());
        } catch (Exception e) {
            dismissProgress();
        }
    }

    private void callDownloadInvoiceWebservice(String id) {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/document/" + id + "/download?module=invoice",
                    "Download Invoice",
                    new JSONObject().toString());
        } catch (Exception e) {
            dismissProgress();
        }
    }

    private void callSuggestInvoiceIdWebservice() {
        try {
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/invoice/suggest-id", "Suggest Invoice Id",
                    new JSONObject().toString());
        } catch (Exception e) { /* non-critical */ }
    }

    private void callGetLogoWebservice() {
        try {
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "invoice/logoget", "Get Invoice Logo",
                    new JSONObject().toString());
        } catch (Exception e) { /* non-critical */ }
    }

    private void callUploadLogoWebservice(Uri imageUri) {
        progressDialog = AndroidUtils.get_progress(getActivity());
        try {
            File imageFile = getFileFromUri(imageUri);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "logo");
            WebServiceHelper.callHttpUploadWebService(
                    this, requireContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "invoice/logoup", "Upload Logo",
                    imageFile, jsonObject.toString());
        } catch (Exception e) {
            Log.e("LOGO_UPLOAD", "Upload failed: " + e.getMessage(), e);
            dismissProgress();
            pendingLogoUpload = false;
            saveInvoice();
        }
    }

    private File getFileFromUri(Uri uri) throws Exception {
        if (uri == null) throw new Exception("URI is null");
        if (!isAdded()) throw new Exception("Fragment not attached");
        InputStream inputStream =
                requireActivity().getContentResolver().openInputStream(uri);
        if (inputStream == null)
            throw new Exception("Unable to open InputStream for URI: " + uri);
        File file = new File(requireActivity().getCacheDir(),
                "invoice_logo_" + System.currentTimeMillis() + ".jpg");
        OutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int read;
        long totalBytes = 0;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
            totalBytes += read;
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
        if (totalBytes == 0) throw new Exception("File is empty (0 bytes)");
        Log.d("LOGO_UPLOAD", "File: " + file.getAbsolutePath()
                + "  size: " + file.length() + " bytes");
        return file;
    }

    private void callInvoiceDetailWebservice(String id, String requestType) {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/invoice/" + id, requestType,
                    new JSONObject().toString());
        } catch (Exception e) {
            dismissProgress();
        }
    }

    private void callCreateInvoiceWebservice(JSONObject data) {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "v3/invoice", "Create Invoice",
                    data.toString());
        } catch (Exception e) {
            dismissProgress();
        }
    }

    private void callPatchInvoiceWebservice(String id, JSONObject data) {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.PATCH,
                    "v3/invoice/" + id, "Edit Invoice",
                    data.toString());
        } catch (Exception e) {
            dismissProgress();
        }
    }

    private void callChangeStatusWebservice(String id, String status) {
        try {
            JSONObject data = new JSONObject();
            data.put("status", status);
            progressDialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.PATCH,
                    "v3/invoice/" + id, "Change Invoice Status",
                    data.toString());
        } catch (Exception e) {
            dismissProgress();
        }
    }

    private void callDeleteInvoiceWebservice(String id) {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.DELETE,
                    "v3/invoice/" + id, "Delete Invoice",
                    new JSONObject().toString());
        } catch (Exception e) {
            dismissProgress();
        }
    }

    private void callMatterList() {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            Log.d("Client_id", client_id);
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/matter/all/" + client_id, "Matters List",
                    jsonObject.toString());
        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing())
                AndroidUtils.dismiss_dialog(progressDialog);
            e.fillInStackTrace();
        }
    }

    private void callShareInvoiceWebservice(InvoiceModel model, String message) {
        try {
            JSONObject docObj = new JSONObject();
            docObj.put("docid", model.getDocid());
            docObj.put("doctype", "general");
            docObj.put("matters", new JSONArray());

            JSONObject payload = new JSONObject();
            payload.put("add", new JSONArray().put(docObj));
            payload.put("message", message);
            payload.put("remove", new JSONArray());

            progressDialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.PUT,
                    "v2/relationship/" + model.getRel_id() + "/docs/share?module=invoice",
                    "Share Invoice",
                    payload.toString());
        } catch (JSONException e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API Response Handler
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        dismissProgress();
        try {
            if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                boolean error = result.optBoolean("error", false);

                switch (httpResult.getRequestType()) {

                    case "Invoice List":
                        if (!error) {
                            JSONArray data = result.optJSONArray("data");
                            if (data != null) loadInvoiceList(data);
                        } else {
                            AndroidUtils.showAlert(result.optString("msg"), getActivity());
                        }
                        break;

                    case "Download Invoice":
                        JSONObject dData = result.optJSONObject("data");
                        if (dData != null) {
                            String url1 = dData.optString("url");
                            AndroidUtils.showAlert(
                                    "You have successfully downloaded the document.",
                                    getActivity(), "Success");
                            FileDownloader.downloadFile(getContext(), url1, "Invoice_Download");
                        }
                        break;

                    case "Suggest Invoice Id":
                        if (!error) {
                            JSONObject data = result.optJSONObject("data");
                            if (data != null) {
                                suggestedInvoiceCode = data.optString("invoice_code", "");
                                suggestedInvoiceSequence = data.optInt("invoice_sequence", 0);
                                if (et_invoice_no != null) {
                                    et_invoice_no.setText(suggestedInvoiceCode);
                                    et_invoice_no.setEnabled(true);
                                    et_invoice_no.setFocusable(true);
                                    et_invoice_no.setFocusableInTouchMode(true);
                                }
                            }
                        }
                        break;

                    case "Clients List":
                        try {
                            JSONObject data1 = result.getJSONObject("data");
                            callCorpClientWebservice();
                            loadClients(data1);
                            if (!client_id.isEmpty()) callMatterList();
                        } catch (Exception e) {
                            e.fillInStackTrace();
                        }
                        break;

                    case "Matters List":
                        if (error) {
                            String msg = result.getString("msg");
                            AndroidUtils.showAlert(msg, getActivity());
                        } else {
                            JSONArray matters = result.getJSONArray("matterList");
                            try {
                                loadMatters(matters);
                            } catch (Exception e) {
                                AndroidUtils.showAlert(e.getMessage(), getActivity());
                                e.fillInStackTrace();
                            }
                        }
                        break;

                    case "Corp Clients List":
                        try {
                            loadCorpClients(result);
                        } catch (Exception e) {
                            e.fillInStackTrace();
                        }
                        break;

                    case "Get Invoice Logo":
                        if (!error) {
                            JSONObject data = result.optJSONObject("data");
                            if (data != null) {
                                boolean logoError = data.optBoolean("error", false);
                                if (!logoError) {
                                    String url = data.optString("url", "");
                                    if (!url.isEmpty()) loadLogoFromUrl(url);
                                }
                            }
                        }
                        break;

                    case "Upload Logo":
                        pendingLogoUpload = false;
                        if (!error) {
                            Log.d("LOGO_UPLOAD", "Logo uploaded OK, saving invoice...");
                        } else {
                            Log.w("LOGO_UPLOAD",
                                    "Logo upload error: " + result.optString("msg"));
                        }
                        saveInvoice();
                        break;

                    case "Invoice View":
                        if (!error) {
                            JSONObject obj = result.optJSONObject("invoice");
                            if (obj != null) {
                                InvoiceModel m = buildDetailModel(obj);
                                editingModel = m;
                                populateForm(m);
                                switchToMode(MODE_VIEW);
                            }
                        } else {
                            AndroidUtils.showAlert(result.optString("msg"), getActivity());
                        }
                        break;

                    case "Invoice Detail":
                        if (!error) {
                            JSONObject obj = result.optJSONObject("invoice");
                            if (obj != null) {
                                InvoiceModel m = buildDetailModel(obj);
                                editingModel = m;
                                populateForm(m);
                                switchToMode(MODE_EDIT);
                            }
                        } else {
                            AndroidUtils.showAlert(result.optString("msg"), getActivity());
                        }
                        break;

                    case "Create Invoice":
                        if (!error) {
                            AndroidUtils.showAlert(
                                    "Invoice created successfully.", getActivity());
                            clearForm();
                            switchToMode(MODE_LIST);
                        } else {
                            AndroidUtils.showAlert(
                                    result.optString("msg", "Failed to create invoice."),
                                    getActivity());
                        }
                        break;

                    case "Edit Invoice":
                        if (!error) {
                            AndroidUtils.showAlert(
                                    result.optString("msg", "Invoice updated successfully."),
                                    getActivity());
                            clearForm();
                            switchToMode(MODE_LIST);
                        } else {
                            AndroidUtils.showAlert(
                                    result.optString("msg", "Update failed."),
                                    getActivity());
                        }
                        break;

                    case "Change Invoice Status":
                        AndroidUtils.showAlert(
                                result.optString("msg",
                                        error ? "Status update failed." : "Status updated."),
                                getActivity());
                        if (!error) callInvoiceListWebservice();
                        break;

                    case "Delete Invoice":
                        AndroidUtils.showAlert(
                                result.optString("msg",
                                        error ? "Delete failed." : "Invoice deleted."),
                                getActivity());
                        if (!error) callInvoiceListWebservice();
                        break;

                    case "Share Invoice":
                        AndroidUtils.showAlert(
                                result.optString("msg",
                                        error ? "Failed to share invoice."
                                                : "Invoice shared successfully."),
                                getActivity());
                        break;
                }

            } else {
                try {
                    JSONObject r = new JSONObject(httpResult.getResponseContent());
                    AndroidUtils.showErrorAlert(r.optString("msg"), getActivity());
                } catch (Exception ignored) {
                    AndroidUtils.showErrorAlert(
                            httpResult.getResponseContent(), getActivity());
                }
            }
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    private void loadMatters(JSONArray matters) throws JSONException {
        mattersList.clear();
        for (int i = 0; i < matters.length(); i++) {
            JSONObject jsonObject = matters.getJSONObject(i);
            MattersModel mattersModel = new MattersModel();
            mattersModel.setId(jsonObject.optString("id"));
            mattersModel.setTitle(jsonObject.optString("title"));
            mattersModel.setType(jsonObject.optString("type"));
            mattersList.add(mattersModel);
        }
        if (mattersList.isEmpty()) {
            ll_matter.setVisibility(GONE);
        } else {
            ll_matter.setVisibility(VISIBLE);
        }
        initMatterUI();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Line Items Table
    // ─────────────────────────────────────────────────────────────────────────

    private void setupLineItemsTable() {
        if (rv_line_items == null) return;
        rv_line_items.setBackground(
                getContext().getDrawable(R.drawable.rectangular_white_background));
        rv_line_items.setLayoutManager(new LinearLayoutManager(getContext()));
        lineItemAdapter = new LineItemAdapter(lineItems, this::recalculateTotal);

        lineItemAdapter.setOnCurrencyChangedListener(currency -> {
            selectedCurrency = currency;
            if (ll_sp_currency != null) {
                TextView tvCurrencyDisplay =
                        ll_sp_currency.findViewById(R.id.tv_spinner_view);
                if (tvCurrencyDisplay != null) tvCurrencyDisplay.setText(selectedCurrency);
            }
            recalculateTotal();
        });

        rv_line_items.setAdapter(lineItemAdapter);
        rv_line_items.setNestedScrollingEnabled(false);
        addLineItemRow();
        if (cv_add_row != null) cv_add_row.setOnClickListener(v -> addLineItemRow());
    }

    private void addLineItemRow() {
        lineItems.add(new LineItemModel());
        lineItemAdapter.notifyItemInserted(lineItems.size() - 1);
        recalculateTotal();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Totals — full sequential calculation
    // ─────────────────────────────────────────────────────────────────────────

    public void recalculateTotal() {
        // 1. Sum all line item amounts for sub-total
        double subTotal = 0;
        for (LineItemModel item : lineItems) {
            subTotal += item.getAmount();
        }

        // 2. Build currency symbol: "₹ " for INR, otherwise "USD " / "EUR " etc.
        String sym = "INR".equals(selectedCurrency) ? "\u20B9 " : selectedCurrency + " ";

        // 3. Display Sub Total
        if (tv_sub_total != null) {
            tv_sub_total.setText(sym + String.format(Locale.getDefault(), "%.2f", subTotal));
        }

        // 4. Parse discount % and tax % entered by user (default 0 if empty / invalid)
        double discPct = parseDouble(getText(et_discount));
        double taxPct = parseDouble(getText(et_tax));

        // 5. Calculate discount amount and post-discount sub-total
        double discountAmount = subTotal * discPct / 100.0;
        double afterDiscount = subTotal - discountAmount;

        // 6. Calculate tax amount on the post-discount value
        double taxAmount = afterDiscount * taxPct / 100.0;

        // 7. Final total = after-discount + tax
        double total = afterDiscount + taxAmount;

        // 8. Display computed discount amount (e.g. "₹ 5.00")
        if (tv_discount_value != null) {
            tv_discount_value.setText(
                    sym + String.format(Locale.getDefault(), "%.2f", discountAmount));
        }

        // 9. Display computed tax amount (e.g. "₹ 2.50")
        if (tv_tax_value != null) {
            tv_tax_value.setText(
                    sym + String.format(Locale.getDefault(), "%.2f", taxAmount));
        }

        // 10. Display final total
        if (tv_total != null) {
            tv_total.setText(sym + String.format(Locale.getDefault(), "%.2f", total));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Form: clear / validate / populate
    // ─────────────────────────────────────────────────────────────────────────

    private void clearForm() {
        editingModel = null;
        logoUri = null;
        logoUrl = "";
        include_logo = true;
        pendingLogoUpload = false;
        suggestedInvoiceCode = "";
        suggestedInvoiceSequence = 0;
        replaceOriginal = false;

        client_id = "";
        client_name = "";
        client_type = "consumer";
        matter_id = "";
        matter_name = "";
        matter_type = "";

        selectedCurrency = "INR";
        if (ll_sp_currency != null) {
            TextView tvCurrencyDisplay =
                    ll_sp_currency.findViewById(R.id.tv_spinner_view);
            if (tvCurrencyDisplay != null) tvCurrencyDisplay.setText("INR");
            if (sp_currency_list != null) sp_currency_list.setVisibility(GONE);
        }
        if (lineItemAdapter != null) lineItemAdapter.setSelectedCurrency("INR");

        if (iv_logo != null) iv_logo.setImageDrawable(null);
        if (cv_logo_preview != null) cv_logo_preview.setVisibility(GONE);
        if (ll_upload_logo != null) ll_upload_logo.setVisibility(VISIBLE);

        if (et_sp_client != null && iv_drop_down_client != null
                && iv_clear_client != null && sp_client_list != null) {
            AndroidUtils.DisplaySpinnerView(
                    sp_client_list, et_sp_client, "",
                    iv_drop_down_client, iv_clear_client, false, ClientAdapter, "Search Client");
        }
        if (et_sp_matter != null && iv_drop_down_matter != null
                && iv_clear_matter != null && sp_matter_list != null) {
            AndroidUtils.DisplaySpinnerView(
                    sp_matter_list, et_sp_matter, "",
                    iv_drop_down_matter, iv_clear_matter, false, Matteradapter, "Search Matter");
        }
        ll_matter.setVisibility(GONE);

        if (et_invoice_no != null) {
            et_invoice_no.setText("");
            et_invoice_no.setEnabled(true);
            et_invoice_no.setFocusable(true);
            et_invoice_no.setFocusableInTouchMode(true);
        }

        setText(et_gst, "");
        setText(et_pan, "");
        setText(et_client_gst, "");
        setText(et_client_pan, "");
        setText(et_client_address, "");
        setText(et_additional_details, "");
        setText(et_discount, "");
        setText(et_tax, "");

        if (tv_invoice_date != null) tv_invoice_date.setText("");
        if (tv_due_date != null) tv_due_date.setText("");

        // Reset all totals displays
        if (tv_sub_total != null) tv_sub_total.setText("\u20B9 0.00");
        if (tv_discount_value != null) tv_discount_value.setText("\u20B9 0.00");
        if (tv_tax_value != null) tv_tax_value.setText("\u20B9 0.00");
        if (tv_total != null) tv_total.setText("\u20B9 0.00");

        if (tv_address_count != null) tv_address_count.setText("0/150");
        if (tv_additional_count != null) tv_additional_count.setText("0/250");

        lineItems.clear();
        if (lineItemAdapter != null) lineItemAdapter.notifyDataSetChanged();
        addLineItemRow();

        if (btn_save != null) btn_save.setText("Save");
    }

    private void populateForm(InvoiceModel m) {
        setText(et_invoice_no, m.getInvoice_no());
        setText(et_gst, m.getFirmGstNo());
        setText(et_pan, m.getFirmPanNo());
        setText(et_client_gst, m.getClientGstNo());
        setText(et_client_pan, m.getClientPanNo());
        setText(et_client_address, m.getBillto());
        setText(et_additional_details, m.getNotes());

        tv_invoice_date.setText(AndroidUtils.formatToMMMddYYYY(m.getDate()));
        tv_due_date.setText(AndroidUtils.formatToMMMddYYYY(m.getDueDate()));

        client_id = m.getClient_id() != null ? m.getClient_id() : "";
        matter_id = m.getMatterId() != null ? m.getMatterId() : "";
        matter_name = m.getMatterName() != null ? m.getMatterName() : "";
        matter_type = m.getMatter_type() != null ? m.getMatter_type() : "";
        client_type = m.getClient_type() != null ? m.getClient_type() : "consumer";
        client_name = m.getName() != null ? m.getName() : "";

        Log.d("POPULATE_CLIENT",
                "id=" + client_id + "  type=" + client_type + "  name=" + client_name);

        if (et_sp_client != null && iv_drop_down_client != null
                && iv_clear_client != null && sp_client_list != null
                && !client_name.isEmpty()) {
            boolean isVisible = sp_client_list.getVisibility() == VISIBLE;
            AndroidUtils.DisplaySpinnerView(
                    sp_client_list, et_sp_client, client_name,
                    iv_drop_down_client, iv_clear_client, false, ClientAdapter, "Search Client");
        }

        if (et_sp_matter != null && iv_drop_down_matter != null
                && iv_clear_matter != null && sp_matter_list != null
                && !matter_name.isEmpty()) {
            boolean isVisible = sp_matter_list.getVisibility() == VISIBLE;
            AndroidUtils.DisplaySpinnerView(
                    sp_matter_list, et_sp_matter, matter_name,
                    iv_drop_down_matter, iv_clear_matter, false, Matteradapter, "Search Matter");
            ll_matter.setVisibility(VISIBLE);
        }

        selectedCurrency = m.getCurrencyCode() != null && !m.getCurrencyCode().isEmpty()
                ? m.getCurrencyCode() : "INR";
        if (ll_sp_currency != null) {
            TextView tvCurrencyDisplay =
                    ll_sp_currency.findViewById(R.id.tv_spinner_view);
            if (tvCurrencyDisplay != null) tvCurrencyDisplay.setText(selectedCurrency);
        }
        if (lineItemAdapter != null) lineItemAdapter.setSelectedCurrency(selectedCurrency);

        if (m.getLogoUrl() != null && !m.getLogoUrl().isEmpty()) {
            loadLogoFromUrl(m.getLogoUrl());
        } else {
            logoUri = null;
            logoUrl = "";
            include_logo = m.isIncludeLogo();
            pendingLogoUpload = false;
            if (iv_logo != null) iv_logo.setImageDrawable(null);
            if (cv_logo_preview != null) cv_logo_preview.setVisibility(GONE);
            if (ll_upload_logo != null) ll_upload_logo.setVisibility(VISIBLE);
        }

        lineItems.clear();
        if (m.getLineItems() != null && !m.getLineItems().isEmpty()) {
            lineItems.addAll(m.getLineItems());
        } else {
            lineItems.add(new LineItemModel());
        }
        if (lineItemAdapter != null) lineItemAdapter.notifyDataSetChanged();

        setText(et_tax,
                m.getTaxAmount() == 0 ? "" : String.valueOf((int) m.getTaxAmount()));
        setText(et_discount,
                m.getDiscountAmount() == 0 ? "" : String.valueOf((int) m.getDiscountAmount()));

        // Recalculate totals after populating all fields
        recalculateTotal();

        callClientWebservice();
    }

    private boolean validateForm() {
        if (tv_invoice_date == null
                || tv_invoice_date.getText().toString().isEmpty()) {
            AndroidUtils.showAlert("Please select Invoice Date.", getActivity());
            return false;
        }
        if (tv_due_date == null
                || tv_due_date.getText().toString().isEmpty()) {
            AndroidUtils.showAlert("Please select Due Date.", getActivity());
            return false;
        }
        if (client_id == null || client_id.isEmpty()) {
            AndroidUtils.showAlert("Please select a Client.", getActivity());
            return false;
        }
        if (getText(et_client_address).isEmpty()) {
            AndroidUtils.showAlert("Please enter Client Address.", getActivity());
            return false;
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Data Loading
    // ─────────────────────────────────────────────────────────────────────────

    private void loadInvoiceList(JSONArray data) {
        try {
            invoiceList.clear();
            for (int i = 0; i < data.length(); i++) {
                JSONObject obj = data.optJSONObject(i);
                if (obj != null) invoiceList.add(buildInvoiceModel(obj));
            }
            loadInvoiceRecyclerView();
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private InvoiceModel buildInvoiceModel(JSONObject obj) {
        InvoiceModel m = new InvoiceModel();
        m.setId(obj.optString("id"));
        m.setName(obj.optString("name"));
        m.setInvoice_no(obj.optString("invoice_no"));
        m.setCreatedby(obj.optString("createdby"));
        m.setStatus(obj.optString("status"));
        m.setDate(obj.optString("date"));
        m.setDueDate(obj.optString("dueDate"));
        m.setCreated(obj.optString("created"));
        m.setIsdisabled(obj.optBoolean("isdisabled", false));
        m.setClient_id(obj.optString("client_id"));
        m.setClient_type(obj.optString("client_type"));
        m.setDocid(obj.optString("docid"));
        m.setDocument_status(obj.optString("document_status"));
        m.setRel_id(obj.optString("rel_id"));
        m.setCan_share(obj.optBoolean("can_share", false));
        return m;
    }

    private InvoiceModel buildDetailModel(JSONObject obj) {
        InvoiceModel m = new InvoiceModel();
        m.setId(obj.optString("id"));
        m.setName(obj.optString("name"));
        m.setInvoice_no(obj.optString("invoice_no"));
        m.setInvoiceSequence(obj.optInt("invoice_sequence", 0));
        m.setStatus(obj.optString("status"));
        m.setBillto(obj.optString("billto"));
        m.setNotes(obj.optString("notes"));
        m.setCurrencyCode(obj.optString("currency_code", "INR"));
        m.setIncludeLogo(obj.optBoolean("include_logo", true));
        m.setDocid(obj.optString("docid"));
        m.setDocument_status(obj.optString("document_status"));
        m.setFirmGstNo(obj.optString("firm_gst_no"));
        m.setFirmPanNo(obj.optString("firm_pan_no"));
        m.setClientGstNo(obj.optString("client_gst_no"));
        m.setClientPanNo(obj.optString("client_pan_no"));
        m.setRel_id(obj.optString("rel_id"));
        m.setCan_share(obj.optBoolean("can_share", false));

        if (obj.has("client")) {
            JSONArray clientArray = obj.optJSONArray("client");
            if (clientArray != null && clientArray.length() > 0) {
                JSONObject clientObj = clientArray.optJSONObject(0);
                if (clientObj != null) {
                    m.setClient_id(clientObj.optString("id"));
                    m.setClient_type(clientObj.optString("type"));
                }
            }
        }

        if (obj.has("matters")) {
            JSONArray mattersArray = obj.optJSONArray("matters");
            if (mattersArray != null && mattersArray.length() > 0) {
                JSONObject matterObj = mattersArray.optJSONObject(0);
                if (matterObj != null) {
                    m.setMatterName(matterObj.optString("name"));
                    m.setMatterId(matterObj.optString("id"));
                    m.setMatter_type(matterObj.optString("type"));
                }
            }
        }

        String logo = obj.optString("logo_url",
                obj.optString("logoUrl", obj.optString("logo", "")));
        m.setLogoUrl(logo);
        m.setDate(obj.optString("date"));
        m.setDueDate(obj.optString("duedate", obj.optString("dueDate")));

        JSONArray items = obj.optJSONArray("items");
        ArrayList<LineItemModel> list = new ArrayList<>();
        if (items != null) {
            for (int i = 0; i < items.length(); i++) {
                JSONObject it = items.optJSONObject(i);
                if (it == null) continue;
                LineItemModel li = new LineItemModel();
                li.setName(it.optString("name"));
                li.setUnitPrice(it.optDouble("unit_price", 0));
                li.setQuantity(it.optInt("no_of_units", 1));
                list.add(li);
            }
        }
        m.setLineItems(list);

        JSONArray taxItems = obj.optJSONArray("tax_items");
        if (taxItems != null && taxItems.length() > 0) {
            JSONObject t = taxItems.optJSONObject(0);
            if (t != null) m.setTaxAmount(t.optDouble("tax", 0));
        }

        JSONArray discounts = obj.optJSONArray("discounts");
        if (discounts != null && discounts.length() > 0) {
            JSONObject d = discounts.optJSONObject(0);
            if (d != null) m.setDiscountAmount(d.optDouble("discount", 0));
        }
        return m;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RecyclerView loader — with empty state logic
    // ─────────────────────────────────────────────────────────────────────────

    private void loadInvoiceRecyclerView() {
        if (rv_invoice_list == null) return;

        invoiceAdapter = new ViewInvoiceAdapter(invoiceList, getContext(), this);
        invoiceAdapter.setRecyclerView(rv_invoice_list);
        rv_invoice_list.setAdapter(invoiceAdapter);
        AndroidUtils.LoadingRecyclerview(rv_invoice_list, getContext());
        AndroidUtils.setupBottomSpacerFooter(rv_invoice_list, getResources().getDimensionPixelSize(R.dimen.twentyeight_dp));

        // Show empty state if list is empty, otherwise show RecyclerView
        updateEmptyState(invoiceList.isEmpty());

        // Re-apply active search filter
        if (et_search_invoice != null) {
            String search =
                    Objects.requireNonNull(et_search_invoice.getText()).toString();
            if (!search.isEmpty()) {
                invoiceAdapter.getFilter().filter(search, count -> {
                    updateEmptyState(count == 0);
                });
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // InvoiceActionListener callbacks
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onViewDetails(InvoiceModel model) {
        callInvoiceDetailWebservice(model.getId(), "Invoice View");
    }

    @Override
    public void onEditInvoice(InvoiceModel model) {
        client_id = model.getClient_id();
        matter_id = model.getMatterId();
        client_type = model.getClient_type();
        callInvoiceDetailWebservice(model.getId(), "Invoice Detail");
    }

    @Override
    public void onChangeStatus(InvoiceModel model) {
        try {
            String currentStatus = model.getStatus().toLowerCase(Locale.ROOT);
            String newStatus;

            switch (currentStatus) {
                case "raised":
                    newStatus = "collected";
                    break;
                case "collected":
                    newStatus = "raised";
                    break;
                case "cancelled":
                    newStatus = "raised";
                    break;
                default:
                    newStatus = "raised";
                    break;
            }

            String fromStatus = capitalize(model.getStatus());
            String toStatus = capitalize(newStatus);

            showSingleStatusConfirmDialog(model, newStatus,
                    "Change Invoice Status",
                    "Are you sure you want to change the invoice status from\n"
                            + fromStatus + " to " + toStatus + "?");

        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void showSingleStatusConfirmDialog(InvoiceModel model, String newStatus,
                                               String title, String confirmMsg) {
        try {
            AlertDialog.Builder dialogBuilder =
                    new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.delete_relationship, null);

            TextView header_name = view.findViewById(R.id.header_name);
            header_name.setText(title);
            header_name.setTextColor(Color.BLACK);

            ImageView close_documents = view.findViewById(R.id.close_documents);
            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
            tv_confirmation.setText(confirmMsg);

            AppCompatButton bt_yes = view.findViewById(R.id.btn_yes);
            AppCompatButton btn_no = view.findViewById(R.id.btn_No);

            final AlertDialog dialog = dialogBuilder.create();
            dialog.setView(view);

            close_documents.setOnClickListener(v -> dialog.dismiss());
            btn_no.setOnClickListener(v -> dialog.dismiss());
            bt_yes.setOnClickListener(v -> {
                dialog.dismiss();
                callChangeStatusWebservice(model.getId(), newStatus);
            });
            dialog.show();
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void showStatusConfirmDialog(InvoiceModel model, String newStatus,
                                         String title, String confirmMsg) {
        try {
            AlertDialog.Builder dialogBuilder =
                    new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.delete_relationship, null);

            TextView header_name = view.findViewById(R.id.header_name);
            header_name.setText(title);
            header_name.setTextColor(Color.BLACK);

            ImageView close_documents = view.findViewById(R.id.close_documents);
            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
            tv_confirmation.setText(confirmMsg);

            AppCompatButton bt_yes = view.findViewById(R.id.btn_yes);
            AppCompatButton btn_no = view.findViewById(R.id.btn_No);

            final AlertDialog dialog = dialogBuilder.create();
            dialog.setView(view);

            close_documents.setOnClickListener(v -> dialog.dismiss());
            btn_no.setOnClickListener(v -> dialog.dismiss());
            bt_yes.setOnClickListener(v -> {
                dialog.dismiss();
                callChangeStatusWebservice(model.getId(), newStatus);
            });
            dialog.show();
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    @Override
    public void onShareInvoice(InvoiceModel model) {
        if (!model.isCan_share()) {
            AndroidUtils.showAlert(
                    "You do not have permission to share this invoice.", getActivity());
            return;
        }
        if (model.getRel_id() == null || model.getRel_id().isEmpty()) {
            AndroidUtils.showAlert(
                    "No relationship ID found for this invoice.", getActivity());
            return;
        }
        if (model.getDocid() == null || model.getDocid().isEmpty()) {
            AndroidUtils.showAlert(
                    "No document ID found for this invoice.", getActivity());
            return;
        }

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(dp(20), dp(24), dp(20), dp(0));

        TextView tvShareWith = new TextView(requireContext());
        tvShareWith.setText("Share With: " + model.getName());
        tvShareWith.setTextSize(18f);
        tvShareWith.setTextColor(Color.parseColor("#1A3A6B"));
        Typeface typeface1 = ResourcesCompat.getFont(getContext(), R.font.gill_sans_bold);
        tvShareWith.setTypeface(typeface1);
        LinearLayout.LayoutParams shareWithLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        shareWithLp.bottomMargin = dp(4);
        tvShareWith.setLayoutParams(shareWithLp);
        root.addView(tvShareWith);

        TextView tvVia = new TextView(requireContext());
        tvVia.setText("Via: Lauditor(Secure)");
        tvVia.setTextSize(18f);
        tvVia.setTextColor(Color.parseColor("#1A3A6B"));
        Typeface typeface2 = ResourcesCompat.getFont(getContext(), R.font.gill_sans);
        tvVia.setTypeface(typeface2);
        LinearLayout.LayoutParams viaLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        viaLp.bottomMargin = dp(16);
        tvVia.setLayoutParams(viaLp);
        root.addView(tvVia);

        EditText etMessage = new EditText(requireContext());
        etMessage.setHint("Message");
        etMessage.setHintTextColor(Color.GRAY);
        etMessage.setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etMessage.setGravity(Gravity.TOP | Gravity.START);
        etMessage.setBackground(null);
        etMessage.setPadding(dp(12), dp(12), dp(12), dp(12));
        etMessage.setTextSize(14f);

        CardView etCard = new CardView(requireContext());
        etCard.setRadius(dp(6));
        etCard.setCardElevation(0);
        etCard.setCardBackgroundColor(Color.parseColor("#F0F0F0"));
        LinearLayout.LayoutParams etCardLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(200));
        etCardLp.bottomMargin = dp(20);
        etCard.setLayoutParams(etCardLp);
        etCard.addView(etMessage);
        root.addView(etCard);

        LinearLayout btnRow = new LinearLayout(requireContext());
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END);
        btnRow.setPadding(0, 0, 0, dp(16));

        AppCompatButton btnCancel = new AppCompatButton(requireContext());
        btnCancel.setText("Cancel");
        btnCancel.setTextColor(Color.DKGRAY);
        Typeface typeface4 = ResourcesCompat.getFont(getContext(), R.font.gill_sans);
        btnCancel.setTypeface(typeface4);
        btnCancel.setBackgroundColor(Color.parseColor("#E0E0E0"));
        LinearLayout.LayoutParams cancelLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cancelLp.setMargins(0, 0, dp(12), 0);
        btnCancel.setLayoutParams(cancelLp);

        AppCompatButton btnSend = new AppCompatButton(requireContext());
        btnSend.setText("Send");
        btnSend.setTextColor(Color.WHITE);
        btnSend.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.Blue_text_color));
        Typeface typeface3 = ResourcesCompat.getFont(getContext(), R.font.gill_sans);
        btnSend.setTypeface(typeface3);
        btnRow.addView(btnCancel);
        btnRow.addView(btnSend);
        root.addView(btnRow);

        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                .setView(root).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92f),
                    android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSend.setOnClickListener(v -> {
            dialog.dismiss();
            callShareInvoiceWebservice(
                    model, etMessage.getText().toString().trim());
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92f),
                    android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onDeleteInvoice(InvoiceModel model) {
        try {
            AlertDialog.Builder dialogBuilder =
                    new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.delete_relationship, null);

            TextView header_name = view.findViewById(R.id.header_name);
            header_name.setText("Delete Invoice");
            header_name.setTextColor(Color.BLACK);

            ImageView close_documents = view.findViewById(R.id.close_documents);
            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
            tv_confirmation.setText("Are you sure you want to delete invoice "
                    + model.getInvoice_no() + "?");

            AppCompatButton bt_yes = view.findViewById(R.id.btn_yes);
            AppCompatButton btn_no = view.findViewById(R.id.btn_No);

            final AlertDialog dialog = dialogBuilder.create();
            dialog.setView(view);

            close_documents.setOnClickListener(v -> dialog.dismiss());
            btn_no.setOnClickListener(v -> dialog.dismiss());
            bt_yes.setOnClickListener(v -> {
                dialog.dismiss();
                callDeleteInvoiceWebservice(model.getId());
            });
            dialog.show();
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing())
            AndroidUtils.dismiss_dialog(progressDialog);
    }

    private String getText(TextInputEditText et) {
        if (et == null || et.getText() == null) return "";
        return et.getText().toString().trim();
    }

    private void setText(TextInputEditText et, String value) {
        if (et != null) et.setText(value != null ? value : "");
    }

    private double parseDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private TextWatcher makeCounter(TextView counterView, int max) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                counterView.setText(s.length() + "/" + max);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    private int dp(int val) {
        return (int) (val * requireContext().getResources().getDisplayMetrics().density);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0))
                + s.substring(1).toLowerCase(Locale.ROOT);
    }

    @Override
    public void onClick(View view) {
    }
}