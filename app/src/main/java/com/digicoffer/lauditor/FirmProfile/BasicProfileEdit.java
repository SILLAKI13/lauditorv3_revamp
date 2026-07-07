package com.digicoffer.lauditor.FirmProfile;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.showDatePicker;

import android.annotation.SuppressLint;
import android.app.Activity;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.res.ResourcesCompat;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.digicoffer.lauditor.Relationships.Model.CountriesDO;
import com.digicoffer.lauditor.MainActivity;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.CacheUtils.AppImageCache;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonMultiSelectionAdapter;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.DescriptionValidation;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BasicProfileEdit extends Fragment implements AsyncTaskCompleteListener, View.OnClickListener {
    TextView tv_BasicProfile, tv_Firmname, tv_Country, tv_Email, tv_contactName, tv_ContactPhone, tv_Website, tv_DefaultCurrency, tv_nationality, tv_dob, tv_bio;
    TextView optional_txt, tv_generate, tv_RegisterAddress, tv_gender, tv_Building, tv_Street, tv_City, tv_State, tv_ad_Country, tv_Zip, tv_mailingAddressnNote, tv_yes, tv_no, etPractice;
    TextInputEditText et_Firmname, et_Email, et_contactName, et_ContactPhone, et_Website, et_nationality, et_bio, etExperience;
    AppCompatButton et_dob;
    private ArrayList<String> allCourtTypesList = new ArrayList<>();
    TextView et_confees, tv_minus, tv_plus;
    LinearLayout ll_et_confees, tl_generate;
    LinearLayoutCompat ll_confee_row;
    ListView sp_et_DefaultCurrency;
    RecyclerView sp_practice, rvServicesList;
    CheckBox cbYes;
    boolean isCaseTypeChecked = true;
    ImageView iv_profile, iv_delete_circle;
    private CommonMultiSelectionAdapter practiceAreaAdapter;
    private Map<String, String[]> dayAvailability = new HashMap<>();
    private final List<String> practiceAreas = new ArrayList<>();
    private final List<String> languagesSpoken = new ArrayList<>();
    private final List<String> servicesOffered = new ArrayList<>();
    LinearLayout llEducation, llCertification, llAwards;
    TextInputEditText etLanguages, etServices, etFirmName, et_counsel_id;
    LinearLayout tl_practice_area;
    LinearLayout ll_et_DefaultCurrency, ll_gender;
    // ── Services Offered API ──────────────────────────────────────────────
    private ArrayList<String> suggestedServicesList = new ArrayList<>();
    private ArrayList<String> allServicesList = new ArrayList<>();
    private ArrayList<String> searchedServicesList = new ArrayList<>();
    private CommonMultiSelectionAdapter servicesDropdownAdapter;
    private boolean isServicesDropdownOpen = false;
    // ── Court States / Suggest API ────────────────────────────────────────────
    private ArrayList<String> courtStateList = new ArrayList<>();
    private Map<String, ArrayList<String>> courtStatesMap = new HashMap<>();
    private ArrayList<String> suggestedCourtTypesList = new ArrayList<>();
    private Runnable pendingCourtStatesCallback = null;
    LinearLayout ll_suggestedServices, ll_suggestedCourtType;
    private Runnable pendingCourtSuggestCallback = null;
    private static final String COURT_CITY_OTHER_OPTION = "+ Other (enter city)";
    private static final String SERVICES_OTHER_OPTION = "+ Other (enter custom)";
    private boolean isSameAddressChecked = false;
    // Pre-built from HIGH_COURTS API — populated once, read many times
    private ArrayList<String> highCourtStateList = new ArrayList<>();
    private Map<String, ArrayList<String>> highCourtCitiesMap = new HashMap<>();
    private Map<String, String> highCourtNameMap = new HashMap<>(); // state → court name
    private ActivityResultLauncher<String> requestCameraPermission;
    private Uri cameraImageUri;
    ArrayList<String> gender_list = new ArrayList<>();
    ArrayList<String> currency_list = new ArrayList<>();
    String default_currency = "", gender = "";
    TextInputEditText et_Building, et_Street, et_City, et_State, et_Zip;
    ImageView img_dropdown_icon_Country, img_clear_icon_Country, img_dropdown_icon_adCountry, img_clear_icon_adCountry, img_dropdown_icon_gender, img_clear_icon_gender, img_dropdown_icon_madCountry, img_clear_icon_madCountry, img_dropdown_icon_Currency, img_clear_icon_Currency, iv_cancel, iv_edit_circle, iv_cancel_add;
    TextView tv_MailingAddress, mtv_Building, mtv_Street, mtv_City, mtv_State, mtv_ad_Country, mtv_Zip, tv_confees;
    TextInputEditText met_Building, met_Street, met_City, met_State, met_Zip;
    TextView met_ad_gender, met_ad_Country, et_ad_Country, et_Country, et_DefaultCurrency;
    LinearLayout ll_et_gender, ll_met_ad_country, ll_et_ad_country, ll_et_country;
    ListView sp_met_ad_country, sp_et_ad_country, sp_et_country, sp_et_gender;
    Button btn_cancel_pp, btn_save_pp, btn_cancel_ai, btn_save_ai;
    Dialog progress_dialog;
    String country_name1 = "", country_name2 = "", country_name3 = "", practice = "";
    boolean iscountry_checked1 = true, iscountry_checked2 = true, iscountry_checked3 = true, iscurrency_checked = true, isgender_checked = true;
    FirmProfileModel firmProfileModel;
    boolean emailhaserror = false, phonehaserror = false, ziphaserror = false, mziphaserror = false;
    FirmProfile firmProfile;
    Boolean isProfileEdit = true;
    Boolean isadditionalinfo = true;
    ArrayList<CountriesDO> countriesList = new ArrayList<>();
    TextView tvErrorEmail, tvErrorZip, tvErrorMzip, tvErrorPhone, person_icon, tv_add_info;
    private NewModel mViewModel;
    String profileUrl = "";
    private static final int REQ_CAMERA = 101;
    private static final int REQ_GALLERY = 102;
    LinearLayout llViewScreen;
    ChipGroup chipPractice, chipLanguages, chipServices;
    TabLayout tabLayout;
    LinearLayout ll_add_edit, ll_edit_Bp;
    LinearLayoutCompat genderlayout, doblayout, firmlayout, nationalitylayout, countrylayout, billing_layout;
    ArrayList<String> caseTypeList = new ArrayList<>();
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    ArrayList<FirmProfileModel> firmProfileModelArrayList;
    private static final int TYPE_EDUCATION = 1;
    private Map<String, List<String>> excludedSlotsMap = new HashMap<>();
    private static final int TYPE_CERTIFICATION = 2;
    private static final int TYPE_AWARD = 3;
    private static final int SLOT_DURATION_MIN = 30;
    private static final int MAX_EXCLUDED_SLOTS = 4;
    ImageView img_dropdown_icon, img_clear_icon, imgServicesDrop;
    // Add at the top with other class variables
    // Add these with the other class variables at the top
    private LinearLayout llCourtEnrollments;
    private JSONArray originalCasesHandled;
    private ChipGroup chipCasesHandled;
    private Map<String, Object> originalValues = new HashMap<>();
    private JSONArray originalPracticeAreas;
    private JSONArray originalLanguages;
    private JSONArray originalServices;
    private JSONArray originalEducation;
    private JSONArray originalCertifications;
    private JSONArray originalAwards;
    private List<FirmProfileModel.WeeklySchedule> originalWeeklySchedule;
    private ArrayList<CourtData> highCourtsList = new ArrayList<>();
    private CourtData supremeCourtData;
    private String selectedState = "";
    private String selectedCity = "";
    private String selectedCourtName = "";
    // ── Spinner adapters (fields so clear-icon & item-click listeners can reference them) ──
    private CommonSpinnerAdapter genderAdapter;
    private CommonSpinnerAdapter currencyAdapter;
    private CommonSpinnerAdapter adCountryAdapter;   // registered address country
    private CommonSpinnerAdapter madCountryAdapter;  // mailing address country
    // ── Live chip state preserved across tab switches ─────────────────────
    private JSONArray liveChipPracticeAreas = null;
    private JSONArray liveChipLanguages = null;
    private JSONArray liveChipServices = null;

    public BasicProfileEdit(FirmProfile firmProfile, Boolean isProfileEdit, Boolean isadditionalinfo) {
        this.firmProfile = firmProfile;
        this.isProfileEdit = isProfileEdit;
        this.isadditionalinfo = isadditionalinfo;
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.basic_profile_edit, container, false);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setupOnBackPressed();
        super.onCreate(savedInstanceState);
        cameraLauncher =
                registerForActivityResult(new ActivityResultContracts.TakePicture(),
                        success -> {
                            if (success && cameraImageUri != null) {
                                handleFileUri(cameraImageUri);
                            }
                        });
        requestCameraPermission =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                openCamera_new();
                            } else {
                                Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
                            }
                        });

        galleryLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK
                                    && result.getData() != null
                                    && result.getData().getData() != null) {

                                Uri galleryUri = result.getData().getData();
                                setProfileImage(galleryUri);
                                profile_upload(galleryUri);
                            }
                        });
    }

    private void handleFileUri(Uri uri) {
        setProfileImage(uri);
        profile_upload(uri);
    }

    private void snapshotLiveChipState() {
        if (chipPractice != null) {
            liveChipPracticeAreas = getChipValuesFromGroup(chipPractice);
        }
        if (chipLanguages != null) {
            liveChipLanguages = getChipValuesFromGroup(chipLanguages);
        }
        if (chipServices != null) {
            liveChipServices = getChipValuesFromGroup(chipServices);
        }
    }

    private void setupOnBackPressed() {
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isEnabled()) {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    private void openCamera_new() {
        if (cameraLauncher == null) {
            Log.e("DEBUG", "cameraLauncher is NULL");
            return;
        }
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "FirmProfile_" + System.currentTimeMillis());
            values.put(MediaStore.Images.Media.DESCRIPTION, "Profile Image");
            cameraImageUri = requireActivity()
                    .getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (cameraImageUri == null) {
                Toast.makeText(getContext(), "Cannot access camera storage", Toast.LENGTH_SHORT).show();
                return;
            }
            cameraLauncher.launch(cameraImageUri);
            profile();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Camera error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setProfileImage(Uri uri) {
        Glide.with(this)
                .load(uri)
                .circleCrop()
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(iv_profile);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        try {
            et_Email = v.findViewById(R.id.et_Email);
            et_ContactPhone = v.findViewById(R.id.et_ContactPhone);
            SharedPreferences prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String loginMethod = prefs.getString("login_method", "");

            // If not found in SharedPreferences, check Constants
            if (loginMethod.isEmpty()) {
                loginMethod = Constants.LOGIN_METHOD;
            }

            // Disable fields based on login method
            if (loginMethod.equals("email")) {
                AndroidUtils.ToggleButton(1, et_ContactPhone);
                AndroidUtils.ToggleButton(0, et_Email);
            } else if (loginMethod.equals("mobile")) {
                AndroidUtils.ToggleButton(0, et_ContactPhone);
                AndroidUtils.ToggleButton(1, et_Email);
            } else {
                // Default - both enabled
                et_Email.setEnabled(true);
                et_ContactPhone.setEnabled(true);
            }

            mViewModel = new ViewModelProvider(requireActivity()).get(NewModel.class);
            mViewModel.setData(getResources().getString(R.string.edit_profile));
            tv_gender = v.findViewById(R.id.tv_gender);
            tv_gender.setText(R.string.gender);
            et_dob = v.findViewById(R.id.et_dob);
            tl_generate = v.findViewById(R.id.tl_generate);
            optional_txt = v.findViewById(R.id.optional_txt);
            optional_txt.setText(R.string.optional_txt);
            tv_generate = tl_generate.findViewById(R.id.tv_image_name);
            tv_generate.setText(R.string.summarize_bio);
            tv_BasicProfile = v.findViewById(R.id.tv_BasicProfile);
            iv_cancel = v.findViewById(R.id.iv_cancel);
            iv_cancel_add = v.findViewById(R.id.iv_cancel_add);
            iv_profile = v.findViewById(R.id.iv_profile);
            person_icon = v.findViewById(R.id.person_icon);
            person_icon.setTextSize(37);
            iv_edit_circle = v.findViewById(R.id.iv_edit_circle);
            iv_delete_circle = v.findViewById(R.id.iv_delete_circle);
            genderlayout = v.findViewById(R.id.genderlayout);
            doblayout = v.findViewById(R.id.doblayout);
            nationalitylayout = v.findViewById(R.id.nationalitylayout);
            nationalitylayout.setVisibility(GONE);
            countrylayout = v.findViewById(R.id.countrylayout);
            countrylayout.setVisibility(GONE);
            firmlayout = v.findViewById(R.id.firmlayout);
            iv_edit_circle.setVisibility(VISIBLE);
            iv_delete_circle.setVisibility(View.GONE);
            ll_add_edit = v.findViewById(R.id.ll_add_edit);
            ll_edit_Bp = v.findViewById(R.id.ll_edit_Bp);

            iv_delete_circle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AndroidUtils.showConfirmationDialog(getContext(), "Confirmation", "Are you sure you want to remove the Profile Picture ?", new AndroidUtils.OnConfirmListener() {
                        @Override
                        public void onSave() {
                            delete();
                        }

                        @Override
                        public void onCancel() {
                        }
                    });
                }
            });

            if (isProfileEdit && !isadditionalinfo) {
                ll_edit_Bp.setVisibility(VISIBLE);
                ll_add_edit.setVisibility(GONE);
            } else if (!isProfileEdit && isadditionalinfo) {
                ll_add_edit.setVisibility(VISIBLE);
                ll_edit_Bp.setVisibility(GONE);
            }

            tv_Firmname = v.findViewById(R.id.tv_Firmname);
            tv_nationality = v.findViewById(R.id.tv_nationality);
            tv_dob = v.findViewById(R.id.tv_dob);
            tv_bio = v.findViewById(R.id.tv_bio);
            tv_Country = v.findViewById(R.id.tv_Country);
            tv_Email = v.findViewById(R.id.tv_Email);
            tv_contactName = v.findViewById(R.id.tv_contactName);
            tv_ContactPhone = v.findViewById(R.id.tv_ContactPhone);
            tv_Website = v.findViewById(R.id.tv_Website);
            et_Firmname = v.findViewById(R.id.et_Firmname);

            et_contactName = v.findViewById(R.id.et_contactName);
            TextInputLayout til_contactName = v.findViewById(R.id.til_contactName);

            if ("solo".equalsIgnoreCase(Constants.CATEGORY)) {
//                et_contactName.setFilters(new InputFilter[]{
//                        new InputFilter.LengthFilter(30)
//                });
//                if (til_contactName != null) {
//                    til_contactName.setCounterEnabled(true);
//                    til_contactName.setCounterMaxLength(30);
//                }
            } else {
                et_contactName.setFilters(new InputFilter[]{});
                if (til_contactName != null) {
                    til_contactName.setCounterEnabled(false);
                }
            }

            et_Website = v.findViewById(R.id.et_Website);
            et_bio = v.findViewById(R.id.et_bio);
            et_bio.setText(R.string.summarize_bio_txt);
            et_bio.addTextChangedListener(new DescriptionValidation(et_bio));

            if ("solo".equalsIgnoreCase(Constants.CATEGORY)) {
                firmlayout.setVisibility(View.GONE);
            } else {
                firmlayout.setVisibility(VISIBLE);
            }

            tv_RegisterAddress = v.findViewById(R.id.tv_RegisterAddress);
            tv_Building = v.findViewById(R.id.tv_Building);
            tv_City = v.findViewById(R.id.tv_City);
            tv_Street = v.findViewById(R.id.tv_Street);
            tv_State = v.findViewById(R.id.tv_State);
            tv_ad_Country = v.findViewById(R.id.tv_ad_Country);
            tv_Zip = v.findViewById(R.id.tv_Zip);
            tv_mailingAddressnNote = v.findViewById(R.id.tv_mailingAddressnNote);
            cbYes = v.findViewById(R.id.cb_yes);

            if (cbYes != null) {
                cbYes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            met_Building.setText(et_Building.getText().toString());
                            met_Street.setText(et_Street.getText().toString());
                            met_City.setText(et_City.getText().toString());
                            met_State.setText(et_State.getText().toString());
                            met_ad_Country.setText(et_ad_Country.getText().toString());
                            met_Zip.setText(et_Zip.getText().toString());
                            cbYes.setTextColor(Color.WHITE);
                        } else {
                            cbYes.setTextColor(Color.BLACK);
                        }
                    }
                });
            }

            tv_MailingAddress = v.findViewById(R.id.tv_MailingAddress);
            mtv_Building = v.findViewById(R.id.mtv_Building);
            mtv_City = v.findViewById(R.id.mtv_City);
            mtv_Street = v.findViewById(R.id.mtv_Street);
            mtv_State = v.findViewById(R.id.mtv_State);
            mtv_ad_Country = v.findViewById(R.id.mtv_ad_Country);
            mtv_Zip = v.findViewById(R.id.mtv_Zip);

            et_Building = v.findViewById(R.id.et_Building);
            et_Street = v.findViewById(R.id.et_Street);
            et_City = v.findViewById(R.id.et_City);
            et_State = v.findViewById(R.id.et_State);
            et_Zip = v.findViewById(R.id.et_Zip);

            met_Building = v.findViewById(R.id.met_Building);
            met_Street = v.findViewById(R.id.met_Street);
            met_City = v.findViewById(R.id.met_City);
            met_State = v.findViewById(R.id.met_State);
            met_Zip = v.findViewById(R.id.met_Zip);

            sp_met_ad_country = v.findViewById(R.id.sp_met_ad_country);
            ll_met_ad_country = v.findViewById(R.id.ll_met_ad_country);
            met_ad_Country = ll_met_ad_country.findViewById(R.id.tv_spinner_view);
            img_dropdown_icon_madCountry = ll_met_ad_country.findViewById(R.id.img_dropdown_icon);
            img_clear_icon_madCountry = ll_met_ad_country.findViewById(R.id.img_clear_icon);
            et_nationality = v.findViewById(R.id.et_nationality);
            et_nationality.setText("Indian");
            et_nationality.setEnabled(false);

            sp_et_gender = v.findViewById(R.id.sp_et_gender);
            ll_et_gender = v.findViewById(R.id.ll_et_gender);
            met_ad_gender = ll_et_gender.findViewById(R.id.tv_spinner_view);
            img_dropdown_icon_gender = ll_et_gender.findViewById(R.id.img_dropdown_icon);
            img_clear_icon_gender = ll_et_gender.findViewById(R.id.img_clear_icon);

            sp_et_ad_country = v.findViewById(R.id.sp_et_ad_country);
            ll_et_ad_country = v.findViewById(R.id.ll_et_ad_country);
            et_ad_Country = ll_et_ad_country.findViewById(R.id.tv_spinner_view);
            img_dropdown_icon_adCountry = ll_et_ad_country.findViewById(R.id.img_dropdown_icon);
            img_clear_icon_adCountry = ll_et_ad_country.findViewById(R.id.img_clear_icon);

            ll_et_country = v.findViewById(R.id.ll_et_country);
            sp_et_country = v.findViewById(R.id.sp_et_country);
            et_Country = ll_et_country.findViewById(R.id.tv_spinner_view);
            img_dropdown_icon_Country = ll_et_country.findViewById(R.id.img_dropdown_icon);
            img_dropdown_icon_Country.setVisibility(GONE);
            img_clear_icon_Country = ll_et_country.findViewById(R.id.img_clear_icon);
            img_clear_icon_Country.setVisibility(GONE);
            ll_et_country.setEnabled(false);
            ll_et_country.setClickable(false);
            ll_et_country.setFocusable(false);
            img_dropdown_icon_Country.setEnabled(false);
            img_dropdown_icon_Country.setClickable(false);
            img_dropdown_icon_Country.setFocusable(false);
            img_clear_icon_Country.setEnabled(false);
            img_clear_icon_Country.setClickable(false);
            img_clear_icon_Country.setFocusable(false);
            sp_et_country.setEnabled(false);
            sp_et_country.setClickable(false);
            et_Country.setEnabled(false);
            et_Country.setClickable(false);
            et_Country.setFocusable(false);

            if ("entity".equalsIgnoreCase(Constants.CATEGORY)) {
                doblayout.setVisibility(View.GONE);
                genderlayout.setVisibility(View.GONE);
            } else {
                doblayout.setVisibility(VISIBLE);
                genderlayout.setVisibility(VISIBLE);
            }

            img_dropdown_icon_Country.setVisibility(View.GONE);
            img_clear_icon_Country.setVisibility(View.GONE);

            btn_cancel_pp = v.findViewById(R.id.btn_cancel_pp);
            btn_save_pp = v.findViewById(R.id.btn_save_pp);
            btn_cancel_ai = v.findViewById(R.id.btn_cancel_ai);
            btn_save_ai = v.findViewById(R.id.btn_save_ai);

            tvErrorMzip = v.findViewById(R.id.tvErrorMzip);
            tvErrorEmail = v.findViewById(R.id.tvErrorEmail);
            tvErrorZip = v.findViewById(R.id.tvErrorZip);
            tvErrorPhone = v.findViewById(R.id.tvErrorPhone);

            et_ContactPhone.addTextChangedListener(new Validation(et_ContactPhone));
            et_ContactPhone.setInputType(InputType.TYPE_CLASS_NUMBER);

            // =====================================================================
            // ZIP INPUT TYPE + MAX LENGTH FILTER (6 digits only)
            // =====================================================================
            et_Zip.setInputType(InputType.TYPE_CLASS_NUMBER);
            met_Zip.setInputType(InputType.TYPE_CLASS_NUMBER);

            // Apply max 6 digit filter to both zip fields
            InputFilter[] zipFilters = new InputFilter[]{
                    new InputFilter.LengthFilter(6),
                    (source, start, end, dest, dstart, dend) -> {
                        for (int i = start; i < end; i++) {
                            if (!Character.isDigit(source.charAt(i))) {
                                return "";
                            }
                        }
                        return null;
                    }
            };
            et_Zip.setFilters(zipFilters);
            met_Zip.setFilters(zipFilters);
            // =====================================================================

            et_Zip.addTextChangedListener(new Validation(et_Zip));
            met_Zip.addTextChangedListener(new Validation(met_Zip));
            AndroidUtils.NumberFilter(et_ContactPhone, true);

            et_Firmname.addTextChangedListener(new Validation(et_Firmname));
            et_Email.addTextChangedListener(new Validation(et_Email));
            et_contactName.addTextChangedListener(new Validation(et_contactName));
            et_ContactPhone.addTextChangedListener(new Validation(et_ContactPhone));
            et_Website.addTextChangedListener(new Validation(et_Website));
            et_Building.addTextChangedListener(new Validation(et_Building));
            et_City.addTextChangedListener(new Validation(et_City));
            et_Street.addTextChangedListener(new Validation(et_Street));
            et_State.addTextChangedListener(new Validation(et_State));
            et_Zip.addTextChangedListener(new Validation(et_Zip));
            met_Building.addTextChangedListener(new Validation(met_Building));
            met_City.addTextChangedListener(new Validation(met_City));
            met_Street.addTextChangedListener(new Validation(met_Street));
            met_State.addTextChangedListener(new Validation(met_State));
            met_Zip.addTextChangedListener(new Validation(met_Zip));
            et_Email.addTextChangedListener(new Validation(et_Email));

            tabLayout = v.findViewById(R.id.tab_layout);
            llViewScreen = v.findViewById(R.id.ll_viewscreen);
            tv_add_info = v.findViewById(R.id.tv_add_info);
            tv_add_info.setTextSize(DynamicUtils.twenty);
            tv_add_info.setText(R.string.additional_info);

            // Determine if this is Firm Profile or My Profile
            boolean isFirmProfile = isFirmProfile();
            tabLayout.clearOnTabSelectedListeners();
            tabLayout.removeAllTabs();
            tabLayout.addTab(tabLayout.newTab().setText("Practice Details"));

            if (isFirmProfile) {
                tabLayout.addTab(tabLayout.newTab().setText("Awards & Recognition"));
            } else {
                boolean isAMMorSuperuser = Constants.ROLE.equalsIgnoreCase("AAM");
                if (isAMMorSuperuser) {
                    tabLayout.addTab(tabLayout.newTab().setText("Awards & Recognition"));
                } else {
                    tabLayout.addTab(tabLayout.newTab().setText("Courts & Cases"));
                    tabLayout.addTab(tabLayout.newTab().setText("Education & Awards"));
                    tabLayout.addTab(tabLayout.newTab().setText("Set Availability"));
                }
            }

            TabLayout.Tab defaultTab = tabLayout.getTabAt(0);
            if (defaultTab != null) {
                defaultTab.select();
            }
            tabLayout.clearOnTabSelectedListeners();
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    // ── Snapshot chips before leaving Practice Details tab ────────────
                    snapshotLiveChipState();

                    llViewScreen.removeAllViews();
                    boolean isFirmProfileInner = isFirmProfile();
                    boolean isAMMorSuperuserInner = Constants.ROLE.equalsIgnoreCase("AAM");

                    switch (tab.getPosition()) {
                        case 0:
                            Constants.PROFILE_EDIT_TAB = "practice_details";
                            createPracticeDetails();
                            break;
                        case 1:
                            Constants.PROFILE_EDIT_TAB = "education_awards";
                            if (isFirmProfileInner || isAMMorSuperuserInner) {
                                createAwardsOnlyEdit();
                            } else {
                                createEducationsAwardsEdit();
                            }
                            break;
                        case 2:
                            Constants.PROFILE_EDIT_TAB = "availability";
                            createSetAvailability();
                            break;
                        case 3:
                            Constants.PROFILE_EDIT_TAB = "courts_cases";
                            if (!isFirmProfileInner && !isAMMorSuperuserInner) {
                                createCourtsAndCasesEdit();
                            }
                            break;
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });

            createPracticeDetails();
            switch (Constants.PROFILE_EDIT_TAB) {
                case "practice_details":
                    createPracticeDetails();
                    break;
                case "education_awards":
                    if (isFirmProfile() || Constants.ROLE.equalsIgnoreCase("AAM")) {
                        createAwardsOnlyEdit();
                    } else {
                        createEducationsAwardsEdit();
                    }
                    break;
                case "availability":
                    if (!isFirmProfile() && !Constants.ROLE.equalsIgnoreCase("AAM")) {
                        createSetAvailability();
                    }
                    break;
                case "courts_cases":
                    if (!isFirmProfile() && !Constants.ROLE.equalsIgnoreCase("AAM")) {
                        createCourtsAndCasesEdit();
                    }
                    break;
            }

            et_Email.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    tvErrorEmail.setVisibility(View.GONE);
                    emailhaserror = false;
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!Objects.requireNonNull(et_Email.getText()).toString().trim().isEmpty()) {
                        if (!AndroidUtils.isValidEmail(s.toString())) {
                            tvErrorEmail.setVisibility(VISIBLE);
                            tvErrorEmail.setText(R.string.enter_a_valid_email_address);
                            emailhaserror = true;
                        } else {
                            tvErrorEmail.setVisibility(View.GONE);
                            emailhaserror = false;
                        }
                    }
                }
            });

            et_ContactPhone.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    tvErrorPhone.setVisibility(View.GONE);
                    phonehaserror = false;
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!s.toString().isEmpty()) {
                        if (s.length() < 10) {
                            tvErrorPhone.setVisibility(VISIBLE);
                            tvErrorPhone.setText(R.string.please_enter_the_10_digit_mobile_number);
                            phonehaserror = true;
                        }
                    } else {
                        tvErrorPhone.setVisibility(View.GONE);
                        phonehaserror = false;
                    }
                }
            });

            // =====================================================================
            // ZIP TEXTWATCHER — Registered Address (exactly 6 digits)
            // =====================================================================
            et_Zip.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    tvErrorZip.setVisibility(View.GONE);
                    ziphaserror = false;
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!s.toString().isEmpty()) {
                        if (s.toString().length() != 6) {
                            tvErrorZip.setVisibility(VISIBLE);
                            tvErrorZip.setText("Invalid ZIP code. Must be exactly 6 digits.");
                            ziphaserror = true;
                        } else {
                            tvErrorZip.setVisibility(View.GONE);
                            ziphaserror = false;
                        }
                    } else {
                        tvErrorZip.setVisibility(View.GONE);
                        ziphaserror = false;
                    }
                }
            });
            // =====================================================================

            // =====================================================================
            // MAILING ZIP TEXTWATCHER — Mailing Address (exactly 6 digits)
            // =====================================================================
            met_Zip.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    tvErrorMzip.setVisibility(View.GONE);
                    mziphaserror = false;
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!s.toString().isEmpty()) {
                        if (s.toString().length() != 6) {
                            tvErrorMzip.setVisibility(VISIBLE);
                            tvErrorMzip.setText("Invalid ZIP code. Must be exactly 6 digits.");
                            mziphaserror = true;
                        } else {
                            tvErrorMzip.setVisibility(View.GONE);
                            mziphaserror = false;
                        }
                    } else {
                        tvErrorMzip.setVisibility(View.GONE);
                        mziphaserror = false;
                    }
                }
            });
            // =====================================================================

            btn_save_pp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (firmProfileModel != null) {
                        SaveDetailsBp();
                    }
                }
            });

            btn_save_ai.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (firmProfileModel != null) {
                        SaveDetailsAdditionalinfo();
                    }
                }
            });

            btn_cancel_pp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearDetails();
                }
            });

            btn_cancel_ai.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearDetails();
                }
            });

            iv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearDetails();
                    boolean fp = isFirmProfile();
                    if (fp) {
                        mViewModel.setData("Firm Profile");
                    } else {
                        mViewModel.setData(getResources().getString(R.string.my_profile));
                    }
                }
            });
            iv_cancel_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearDetails();
                }
            });

            sp_et_country.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    country_name1 = countriesList.get(position).getName();
                    AndroidUtils.DisplaySpinnerView(sp_et_country, et_Country, country_name1, img_dropdown_icon_Country, img_clear_icon_Country, false, adCountryAdapter, "Search Country");
                    iscountry_checked1 = true;
                }
            });

            sp_et_ad_country.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    country_name2 = countriesList.get(position).getName();
                    AndroidUtils.DisplaySpinnerView(sp_et_ad_country, et_ad_Country, country_name2, img_dropdown_icon_adCountry, img_clear_icon_adCountry, false, adCountryAdapter, "Search Country");
                    iscountry_checked2 = true;
                }
            });

            sp_met_ad_country.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    country_name3 = countriesList.get(position).getName();
                    AndroidUtils.DisplaySpinnerView(sp_met_ad_country, met_ad_Country, country_name3, img_dropdown_icon_madCountry, img_clear_icon_madCountry, false, adCountryAdapter, "Search Country");
                    iscountry_checked3 = true;
                }
            });

            currency_list = AndroidUtils.getCurrency_list();
            gender_list.clear();
            gender_list.add("Male");
            gender_list.add("Female");
            gender_list.add("Other");
            genderAdapter = new CommonSpinnerAdapter(getActivity(), gender_list);
            sp_et_gender.setAdapter(genderAdapter);
            AndroidUtils.LoadList(sp_et_gender, getContext(), gender_list.size(), true);
            met_ad_gender.setHint("Select Gender");
            ll_et_gender.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isVisible = sp_et_gender != null
                            && sp_et_gender.getVisibility() == View.VISIBLE;

                    AndroidUtils.DisplaySpinnerView(sp_et_gender, met_ad_gender, gender,
                            img_dropdown_icon_gender, img_clear_icon_gender,
                            !isVisible, genderAdapter, "Search Gender");
                }
            });

            sp_et_gender.setOnItemClickListener((parent, view, position, id) -> {
                gender = (String) genderAdapter.getItem(position);
                AndroidUtils.DisplaySpinnerView(sp_et_gender, met_ad_gender, gender,
                        img_dropdown_icon_gender, img_clear_icon_gender,
                        false, genderAdapter, "Search Gender");
            });

            img_clear_icon_gender.setOnClickListener(v1 -> {
                gender = "";
                AndroidUtils.DisplaySpinnerView(sp_et_gender, met_ad_gender, "",
                        img_dropdown_icon_gender, img_clear_icon_gender,
                        false, genderAdapter, "Search Gender");
            });

            img_clear_icon_Country.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AndroidUtils.DisplaySpinnerView(sp_et_country, et_Country, country_name1, img_dropdown_icon_Country, img_clear_icon_Country, false, adCountryAdapter, "Search Country");
                    iscountry_checked1 = true;
                }
            });

            img_clear_icon_adCountry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AndroidUtils.DisplaySpinnerView(sp_et_ad_country, et_ad_Country, country_name2, img_dropdown_icon_adCountry, img_clear_icon_adCountry, false, adCountryAdapter, "Search Country");
                    iscountry_checked2 = true;
                }
            });

            img_clear_icon_madCountry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AndroidUtils.DisplaySpinnerView(sp_met_ad_country, met_ad_Country, country_name3, img_dropdown_icon_madCountry, img_clear_icon_madCountry, false, madCountryAdapter, "Search Country");
                    iscountry_checked3 = true;
                }
            });

            ll_et_country.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AndroidUtils.display_listview(iscountry_checked1, sp_et_country);
                    iscountry_checked1 = !iscountry_checked1;
                }
            });

            ll_et_ad_country.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AndroidUtils.display_listview(iscountry_checked2, sp_et_ad_country);
                    iscountry_checked2 = !iscountry_checked2;
                }
            });

            ll_met_ad_country.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AndroidUtils.display_listview(iscountry_checked3, sp_met_ad_country);
                    iscountry_checked3 = !iscountry_checked3;
                }
            });

            setData();

            if (Constants.firmProfileModel != null) {
                firmProfileModel = Constants.firmProfileModel;
                LoadData();
                checkIfAddressesMatch();
                callCountriesWebService();
                img_clear_icon_Country.setVisibility(VISIBLE);
                img_dropdown_icon_Country.setVisibility(View.GONE);
                img_clear_icon_adCountry.setVisibility(VISIBLE);
                img_dropdown_icon_adCountry.setVisibility(View.GONE);
                img_clear_icon_madCountry.setVisibility(VISIBLE);
                img_dropdown_icon_madCountry.setVisibility(View.GONE);

                // ── GH Team Member: Disable firm name + address fields ────────
                boolean isGHTeamMember = Constants.ROLE.equalsIgnoreCase("GH") ||
                        Constants.ROLE.equalsIgnoreCase("TM");

                if (isGHTeamMember) {
                    // Firm name not editable
                    et_Firmname.setEnabled(false);
                    et_Firmname.setFocusable(false);
                    et_Firmname.setFocusableInTouchMode(false);
                    et_Firmname.setClickable(false);
                    et_Firmname.setAlpha(0.6f);

                    // Website not editable
                    et_Website.setEnabled(false);
                    et_Website.setFocusable(false);
                    et_Website.setFocusableInTouchMode(false);
                    et_Website.setClickable(false);
                    et_Website.setAlpha(0.6f);

                    // Address fields not editable
                    disableAddressFieldsForGH();
                }

                int tabIndex = 0;
                switch (Constants.PROFILE_EDIT_TAB) {
                    case "practice_details":
                        tabIndex = 0;
                        break;
                    case "courts_cases":
                        boolean isFirmProfileTabCourts = isFirmProfile();
                        boolean isAMMCourts = Constants.ROLE.equalsIgnoreCase("AAM");
                        tabIndex = (isFirmProfileTabCourts || isAMMCourts) ? 0 : 1;
                        break;
                    case "education_awards":
                        boolean isFirmProfileTab = isFirmProfile();
                        boolean isAMM = Constants.ROLE.equalsIgnoreCase("AAM");
                        tabIndex = (isFirmProfileTab || isAMM) ? 1 : 2;
                        break;
                    case "availability":
                        boolean isFirmProfileAvail = isFirmProfile();
                        boolean isAMMAvail = Constants.ROLE.equalsIgnoreCase("AAM");
                        tabIndex = (isFirmProfileAvail || isAMMAvail) ? 0 : 3;
                        break;
                }

                tabLayout.clearOnTabSelectedListeners();
                TabLayout.Tab tab = tabLayout.getTabAt(tabIndex);
                if (tab != null) tab.select();

                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        // ── Snapshot chips before leaving Practice Details tab ────────────
                        snapshotLiveChipState();

                        llViewScreen.removeAllViews();
                        boolean isFirmProfileInner = isFirmProfile();
                        boolean isAMMorSuperuserInner = Constants.ROLE.equalsIgnoreCase("AAM");

                        switch (tab.getPosition()) {
                            case 0:
                                Constants.PROFILE_EDIT_TAB = "practice_details";
                                createPracticeDetails();
                                break;
                            case 1:
                                if (!isFirmProfileInner && !isAMMorSuperuserInner) {
                                    Constants.PROFILE_EDIT_TAB = "courts_cases";
                                    createCourtsAndCasesEdit();
                                } else {
                                    Constants.PROFILE_EDIT_TAB = "education_awards";
                                    if (isFirmProfileInner || isAMMorSuperuserInner) {
                                        createAwardsOnlyEdit();
                                    }
                                }
                                break;
                            case 2:
                                Constants.PROFILE_EDIT_TAB = "education_awards";
                                if (!isFirmProfileInner && !isAMMorSuperuserInner) {
                                    createEducationsAwardsEdit();
                                }
                                break;
                            case 3:
                                Constants.PROFILE_EDIT_TAB = "availability";
                                if (!isFirmProfileInner && !isAMMorSuperuserInner) {
                                    createSetAvailability();
                                }
                                break;
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                    }
                });
                createPracticeDetails();
                switch (Constants.PROFILE_EDIT_TAB) {
                    case "practice_details":
                        createPracticeDetails();
                        break;
                    case "courts_cases":
                        if (!isFirmProfile() && !Constants.ROLE.equalsIgnoreCase("AAM")) {
                            createCourtsAndCasesEdit();
                        }
                        break;
                    case "education_awards":
                        if (isFirmProfile() || Constants.ROLE.equalsIgnoreCase("AAM")) {
                            createAwardsOnlyEdit();
                        } else {
                            createEducationsAwardsEdit();
                        }
                        break;
                    case "availability":
                        if (!isFirmProfile() && !Constants.ROLE.equalsIgnoreCase("AAM")) {
                            createSetAvailability();
                        }
                        break;
                }

            }

            // ── Bottom padding so content clears the nav bar ──────────────────
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    getResources().getDimensionPixelSize(R.dimen.twentyeight_dp)
            );

        } catch (Resources.NotFoundException e) {
            e.fillInStackTrace();
        }
    }

    private View createCourtsAndCasesEdit() {
        llViewScreen.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View practiceView = inflater.inflate(R.layout.practicedetails_edit, llViewScreen, false);

        boolean isAMMorSuperuser = Constants.ROLE.equalsIgnoreCase("AAM");
        boolean isFirmProfileMode = isFirmProfile();

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        if (isFirmProfileMode || isAMMorSuperuser) {
            llViewScreen.addView(container);
            llViewScreen.setVisibility(VISIBLE);
            return container;
        }

        View llCourtSection = practiceView.findViewById(R.id.ll_court_section);
        View llCasesSection = practiceView.findViewById(R.id.ll_cases_handled_section);

        // ════════════════════════════════════════════════════════════════════════════════════
        // COURT SECTION (FIRST - at top)
        // ════════════════════════════════════════════════════════════════════════════════════
        if (llCourtSection != null) {
            if (llCourtSection.getParent() instanceof ViewGroup) {
                ((ViewGroup) llCourtSection.getParent()).removeView(llCourtSection);
            }
            llCourtSection.setVisibility(View.VISIBLE);

            TextView tvCourtLabel = llCourtSection.findViewById(R.id.tv_court_enrollments_label);
            if (tvCourtLabel != null) tvCourtLabel.setText("Court Practice Details");
            ll_suggestedCourtType = llCourtSection.findViewById(R.id.ll_suggestedCourtType);
            TextView tv_recommendedCourtType = llCourtSection.findViewById(R.id.tv_recommendedCourtType);
            tv_recommendedCourtType.setText(R.string.recommended_court_types_for_your_selected_practice_areas);
            llCourtEnrollments = llCourtSection.findViewById(R.id.ll_court_enrollments);
            TextView tvNoCourt = llCourtSection.findViewById(R.id.tvNoCourtEnrollments);

            View tvAddCourtView = llCourtSection.findViewById(R.id.tv_add_court);
            final TextView tvAddCourt = (tvAddCourtView instanceof TextView)
                    ? (TextView) tvAddCourtView : null;

            final TextView tvWarning = llCourtSection.findViewById(R.id.tv_no_practice_warning);
            final ChipGroup chipPracticeLocal = practiceView.findViewById(R.id.chipGroupPractice);

            if (tvNoCourt != null) tvNoCourt.setVisibility(View.GONE);
            if (!suggestedCourtTypesList.isEmpty()) {
                if (ll_suggestedCourtType != null) ll_suggestedCourtType.setVisibility(VISIBLE);
            } else {
                if (ll_suggestedCourtType != null) ll_suggestedCourtType.setVisibility(GONE);
            }

            List<FirmProfileModel.CourtEnrollment> courts = (firmProfileModel != null
                    && firmProfileModel.getData() != null
                    && firmProfileModel.getData().getProfile() != null)
                    ? firmProfileModel.getData().getProfile().getCourt_enrollments() : null;

            LayoutInflater courtInflater = LayoutInflater.from(requireContext());

            updateCourtSectionState(tvAddCourt, tvWarning, chipPracticeLocal);

            if (llCourtEnrollments != null) {
                llCourtEnrollments.removeAllViews();
                llCourtEnrollments.setVisibility(View.VISIBLE);

                if (courts != null && !courts.isEmpty()) {
                    List<String> currentPA = getCurrentPracticeAreaValues();

                    // Step 1: Load states first, THEN suggest, THEN populate rows
                    callCourtStatesApi(() -> {
                        // States are ready — now get suggested court types
                        callCourtSuggestApi(currentPA, () -> {
                            if (!isAdded() || llCourtEnrollments == null) return;
                            llCourtEnrollments.removeAllViews();
                            for (FirmProfileModel.CourtEnrollment ce : courts) {
                                addCourtEnrollmentRow(
                                        courtInflater,
                                        llCourtEnrollments,
                                        ce.getCourt_type() != null ? ce.getCourt_type() : "",
                                        ce.getState() != null ? ce.getState() : "",
                                        ce.getCity() != null ? ce.getCity() : "",
                                        ce.getCourt_name() != null ? ce.getCourt_name() : ""
                                );
                            }
                        });
                    });
                }
            }

            ChipGroup source = (chipPractice != null) ? chipPractice : chipPracticeLocal;
            boolean hasPracticeAreas = false;
            if (source != null && source.getChildCount() > 0) {
                JSONArray vals = getChipValuesFromGroup(source);
                hasPracticeAreas = vals.length() > 0;
            }
            if (tvWarning != null) {
                tvWarning.setText("Select at least one Practice Area to enable court details entry. ");
                if (hasPracticeAreas) {
                    tvWarning.setVisibility(GONE);
                } else {
                    tvWarning.setVisibility(VISIBLE);
                }
            }
            if (tvAddCourt != null) {
                tvAddCourt.setText(R.string._add_court);
                tvAddCourt.setOnClickListener(v -> {
                    if (!isAdded() || llCourtEnrollments == null) return;

                    List<String> currentPA = getCurrentPracticeAreaValues();
                    callCourtStatesApi(null);
                    callCourtSuggestApi(currentPA, () -> {
                        if (!isAdded() || llCourtEnrollments == null) return;
                        addCourtEnrollmentRow(courtInflater, llCourtEnrollments, "", "", "", "");
                    });
                });
            }

            if (chipPracticeLocal != null) {
                chipPracticeLocal.setOnHierarchyChangeListener(
                        new ViewGroup.OnHierarchyChangeListener() {
                            @Override
                            public void onChildViewAdded(View parent, View child) {
                                updateCourtSectionState(tvAddCourt, tvWarning, chipPracticeLocal);
                            }

                            @Override
                            public void onChildViewRemoved(View parent, View child) {
                                updateCourtSectionState(tvAddCourt, tvWarning, chipPracticeLocal);
                                ChipGroup source = (chipPractice != null)
                                        ? chipPractice : chipPracticeLocal;
                                boolean hasPracticeAreas = false;
                                if (source != null) {
                                    JSONArray vals = getChipValuesFromGroup(source);
                                    hasPracticeAreas = vals.length() > 0;
                                }
                                if (!hasPracticeAreas
                                        && llCourtEnrollments != null
                                        && llCourtEnrollments.getChildCount() > 0) {
                                    if (tvWarning != null)
                                        tvWarning.setVisibility(View.VISIBLE);
                                }
                            }
                        });
            }

            container.addView(llCourtSection);
        }

        // ─── Divider between Court and Cases sections ─────────────────────────────────────
        if (llCourtSection != null && llCasesSection != null) {
            View divider = new View(requireContext());
            int dividerHeightPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 1.5f,
                    requireContext().getResources().getDisplayMetrics());
            int marginPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 10f,
                    requireContext().getResources().getDisplayMetrics());
            LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dividerHeightPx);
            divParams.setMargins(marginPx, marginPx, marginPx, marginPx);
            divider.setLayoutParams(divParams);
            divider.setBackgroundColor(Color.parseColor("#CCCCCC"));
            container.addView(divider);
        }

        // ════════════════════════════════════════════════════════════════════════════════════
        // CASES HANDLED SECTION (SECOND - below court)
        // ════════════════════════════════════════════════════════════════════════════════════
        if (llCasesSection != null) {
            if (llCasesSection.getParent() instanceof ViewGroup) {
                ((ViewGroup) llCasesSection.getParent()).removeView(llCasesSection);
            }
            llCasesSection.setVisibility(View.VISIBLE);

            TextView tvCasesLabel = llCasesSection.findViewById(R.id.tv_cases_handled_label);
            if (tvCasesLabel != null) tvCasesLabel.setText("Types of Cases Handled");

            ChipGroup chipCases = llCasesSection.findViewById(R.id.chipGroupCasesHandled);
            TextInputEditText etCases = llCasesSection.findViewById(R.id.et_cases_handled);
            chipCasesHandled = chipCases;
            if (chipCasesHandled != null) {
                chipCasesHandled.removeAllViews();
                if (firmProfileModel != null
                        && firmProfileModel.getData() != null
                        && firmProfileModel.getData().getProfile() != null) {
                    List<String> cases = firmProfileModel.getData().getProfile().getCases_handled();
                    if (cases != null) {
                        for (String c : cases) addChip(chipCasesHandled, c);
                    }
                }
            }

            if (etCases != null) {
                etCases.setFilters(new InputFilter[]{
                        (source, start, end, dest, dstart, dend) -> {
                            if (source != null && source.toString().equals("\n")) {
                                return null;
                            }
                            int destLen = dest.length() - (dend - dstart);
                            int keep = 500 - destLen;
                            if (keep <= 0) {
                                return "";
                            }
                            if (end - start <= keep) {
                                return null;
                            }
                            return source.subSequence(start, start + keep);
                        }
                });

                etCases.setOnEditorActionListener((v, actionId, event) -> {
                    boolean isDone =
                            actionId == EditorInfo.IME_ACTION_DONE ||
                                    (event != null
                                            && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                                            && event.getAction() == KeyEvent.ACTION_DOWN);
                    if (!isDone) return false;
                    String text = etCases.getText() != null
                            ? etCases.getText().toString().replace("\n", "").trim() : "";
                    if (text.isEmpty()) return true;
                    if (text.length() > 500) text = text.substring(0, 500);
                    addChip(chipCasesHandled, text);
                    etCases.setText("");
                    return true;
                });

                etCases.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int i, int c, int a) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s == null) return;
                        String raw = s.toString();
                        if (raw.contains("\n")) {
                            String text = raw.replace("\n", "").trim();
                            if (!text.isEmpty()) {
                                if (text.length() > 500) text = text.substring(0, 500);
                                addChip(chipCasesHandled, text);
                            }
                            etCases.setText("");
                        }
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }
                });
            }

            container.addView(llCasesSection);
        }

        llViewScreen.addView(container);
        llViewScreen.setVisibility(VISIBLE);
        scrollToTargetField();
        return container;
    }
    // ─────────────────────────────────────────────────────────────────────────────
// SERVICES OFFERED — API-powered spinner with search + "Other" custom entry
// ─────────────────────────────────────────────────────────────────────────────

    /**
     * Call /professional/v3/services/suggest  passing current practice areas.
     * Called every time practice-area chips change AND when the services dropdown opens.
     */
    private void fetchSuggestedServices(List<String> practiceAreas) {
        if (practiceAreas == null || practiceAreas.isEmpty()) {
            allServicesList.clear();
            suggestedServicesList.clear();
        }
        try {
            JSONObject body = new JSONObject();
            JSONArray pa = new JSONArray();
            for (String p : practiceAreas) pa.put(p);
            body.put("practice_areas", pa);

            WebServiceHelper.callHttpWebService(
                    this,
                    getContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "v3/services/suggest",
                    "SERVICES_SUGGEST",
                    body.toString()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable pendingSuggestCallback = null;

    /**
     * Call /professional/v3/services/search?query=<text>
     */
    private void fetchSearchedServices(String query, Runnable onComplete) {
        if (query == null || query.trim().isEmpty()) {
            searchedServicesList.clear();
            searchedServicesList.addAll(suggestedServicesList);
            if (onComplete != null) onComplete.run();
            return;
        }
        try {
            WebServiceHelper.callHttpWebService(
                    this,
                    getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/services/search?query=" + Uri.encode(query.trim()),
                    "SERVICES_SEARCH",
                    new JSONObject().toString()
            );
            pendingSearchCallback = onComplete;
        } catch (Exception e) {
            e.printStackTrace();
            searchedServicesList.clear();
            if (onComplete != null) onComplete.run();
        }
    }

    private Runnable pendingSearchCallback = null;

    /**
     * Build the display list for the services dropdown:
     * [ suggested/search results ... ] + "Other (enter custom)"
     */
    private ArrayList<String> buildServicesDisplayList(ArrayList<String> source) {
        ArrayList<String> list = new ArrayList<>(source);
        // Remove any already-chipped items so we don't show duplicates
        if (chipServices != null) {
            JSONArray already = getChipValuesFromGroup(chipServices);
            for (int i = 0; i < already.length(); i++) {
                list.remove(already.optString(i, ""));
            }
        }
        if (!list.contains(SERVICES_OTHER_OPTION)) {
            list.add(SERVICES_OTHER_OPTION);
        }
        return list;
    }

    /**
     * Wire up the full services-offered UI inside the given practiceView.
     * <p>
     * Layout expectations inside practiceView:
     * - ll_services_spinner_row  : LinearLayout (the clickable dropdown row)
     * - tv_services_spinner_view : TextView     (shows hint / selected item)
     * - img_services_dropdown    : ImageView    (chevron)
     * - img_services_clear       : ImageView    (×)
     * - sp_services_list         : ListView     (dropdown list)
     * - et_services_custom       : TextInputEditText  (hidden by default, shown for "Other")
     * - chipGroupServices        : ChipGroup    (existing)
     * <p>
     * NOTE: et_services (the old TextInputEditText used for normal chip input)
     * is now REPLACED by this dropdown+search UI. We keep chipGroupServices.
     */
    private void setupServicesOfferedUI(View practiceView) {
        // Get references to views
        TextInputEditText etServicesSearch = practiceView.findViewById(R.id.et_services_search);
        AppCompatImageButton btnServiceConfirm = practiceView.findViewById(R.id.btn_service_confirm);
        AppCompatImageButton btnServiceCancel = practiceView.findViewById(R.id.btn_service_cancel);
        ImageView imgServicesDrop = practiceView.findViewById(R.id.img_services_dropdown);
        rvServicesList = practiceView.findViewById(R.id.sp_services_list);
        ll_suggestedServices = practiceView.findViewById(R.id.ll_suggestedServices);
        TextView tv_recommendedTxt = practiceView.findViewById(R.id.tv_recommendedTxt);
        tv_recommendedTxt.setText(R.string.recommended_services_for_your_selected_practice_areas);
        chipServices = practiceView.findViewById(R.id.chipGroupServices);

        // ── Use local reference so we always read live chip state ─────────────
        ChipGroup chipPracticeLocal = practiceView.findViewById(R.id.chipGroupPractice);

        if (etServicesSearch == null || rvServicesList == null) return;

        // ── Helper: check live whether any practice area is selected ──────────
        // Uses chipPracticeLocal (direct view lookup) so it is never stale
        // regardless of when chipPractice (fragment field) gets assigned.
        final Runnable[] applyEnabledState = {null};
        applyEnabledState[0] = () -> {
            ChipGroup source = (chipPractice != null) ? chipPractice : chipPracticeLocal;
            boolean hasPracticeAreas = source != null
                    && source.getChildCount() > 0
                    && getChipValuesFromGroup(source).length() > 0;

            float alpha = hasPracticeAreas ? 1f : 0.5f;

            LinearLayout tlServicesArea = practiceView.findViewById(R.id.tl_services_area);
            if (tlServicesArea != null) {
                tlServicesArea.setAlpha(alpha);
            }
            etServicesSearch.setEnabled(hasPracticeAreas);
            etServicesSearch.setFocusable(hasPracticeAreas);
            etServicesSearch.setFocusableInTouchMode(hasPracticeAreas);
            etServicesSearch.setClickable(hasPracticeAreas);
            if (imgServicesDrop != null) {
                imgServicesDrop.setEnabled(hasPracticeAreas);
                imgServicesDrop.setClickable(hasPracticeAreas);
            }
            if (btnServiceConfirm != null) btnServiceConfirm.setEnabled(hasPracticeAreas);
            if (btnServiceCancel != null) btnServiceCancel.setEnabled(hasPracticeAreas);

            // Close dropdown immediately if open and being disabled
            if (!hasPracticeAreas && rvServicesList != null) {
                rvServicesList.setVisibility(View.GONE);
                if (imgServicesDrop != null)
                    imgServicesDrop.animate().rotation(0).setDuration(200).start();
                etServicesSearch.setText("");
            }
        };

        // ── Apply initial enabled/disabled state ──────────────────────────────
        applyEnabledState[0].run();

        // ── Watch chipGroupPractice so services row re-evaluates live ─────────
        if (chipPracticeLocal != null) {
            chipPracticeLocal.setOnHierarchyChangeListener(
                    new ViewGroup.OnHierarchyChangeListener() {
                        @Override
                        public void onChildViewAdded(View parent, View child) {
                            applyEnabledState[0].run();
                        }

                        @Override
                        public void onChildViewRemoved(View parent, View child) {
                            applyEnabledState[0].run();
                        }
                    });
        }

        // ── Initially hide confirm and cancel buttons ─────────────────────
        if (btnServiceConfirm != null) btnServiceConfirm.setVisibility(View.GONE);
        if (btnServiceCancel != null) btnServiceCancel.setVisibility(View.GONE);

        // Set up the adapter
        CommonMultiSelectionAdapter servicesAdapter = new CommonMultiSelectionAdapter(
                requireContext(),
                buildSortedServicesList(),   // ← sorted list instead of allServicesList
                new ArrayList<>(),
                selectedItems -> {
                    if (chipServices == null) return;
                    chipServices.removeAllViews();
                    for (String item : selectedItems) {
                        if (!item.isEmpty()) addChip(chipServices, item);
                    }
                }
        );

        // ── Mark suggested items so iv_suggest shows only for them ───────────
        servicesAdapter.setSuggestedItems(suggestedServicesList, true);
        if (!suggestedServicesList.isEmpty()) {
            if (ll_suggestedServices != null) ll_suggestedServices.setVisibility(VISIBLE);
        } else {
            if (ll_suggestedServices != null) ll_suggestedServices.setVisibility(GONE);
        }
        rvServicesList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvServicesList.setAdapter(servicesAdapter);
        rvServicesList.setVisibility(View.GONE);

        // Adaptive height: max 5 items visible (~48dp each), grows/shrinks with filter results
        final int ITEM_HEIGHT_DP = 48;
        final int MAX_VISIBLE_ITEMS = 5;
        float density = requireContext().getResources().getDisplayMetrics().density;
        final int itemPx = (int) (ITEM_HEIGHT_DP * density);
        final int maxPx = (int) (ITEM_HEIGHT_DP * MAX_VISIBLE_ITEMS * density);

        servicesAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                updateHeight();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                updateHeight();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                updateHeight();
            }

            private void updateHeight() {
                int count = servicesAdapter.getItemCount();
                if (count == 0) {
                    rvServicesList.setVisibility(View.GONE);
                    return;
                }
                int height = Math.min(count * itemPx, maxPx);
                ViewGroup.LayoutParams lp = rvServicesList.getLayoutParams();
                if (lp == null) {
                    lp = new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, height);
                } else {
                    lp.height = height;
                }
                rvServicesList.setLayoutParams(lp);
            }
        });

        // Store current selected items in adapter
        updateAdapterSelection(servicesAdapter);

        // ── Live sync: chip removal → immediately deselect in RecyclerView ──
        final boolean[] updatingAdapter = {false};

        if (chipServices != null) {
            chipServices.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                @Override
                public void onChildViewAdded(View parent, View child) {
                    if (updatingAdapter[0]) return;
                    updatingAdapter[0] = true;
                    try {
                        updateAdapterSelection(servicesAdapter);
                    } finally {
                        updatingAdapter[0] = false;
                    }
                }

                @Override
                public void onChildViewRemoved(View parent, View child) {
                    if (updatingAdapter[0]) return;
                    updatingAdapter[0] = true;
                    try {
                        updateAdapterSelection(servicesAdapter);
                    } finally {
                        updatingAdapter[0] = false;
                    }
                }
            });
        }

        // ── TextWatcher for filtering ─────────────────────────────────────
        etServicesSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s != null ? s.toString() : "";

                if (btnServiceConfirm != null) {
                    btnServiceConfirm.setVisibility(query.length() > 0 ? View.VISIBLE : View.GONE);
                }
                if (btnServiceCancel != null) {
                    btnServiceCancel.setVisibility(query.length() > 0 ? View.VISIBLE : View.GONE);
                }

                if (query.length() > 0 && rvServicesList.getVisibility() != View.VISIBLE) {
                    rvServicesList.setVisibility(View.VISIBLE);
                    if (imgServicesDrop != null) {
                        imgServicesDrop.animate().rotation(180).setDuration(200).start();
                    }
                }

                servicesAdapter.filter(query);

                if (servicesAdapter.getItemCount() == 0 && query.length() > 0) {
                    rvServicesList.setVisibility(View.GONE);
                    if (imgServicesDrop != null) {
                        imgServicesDrop.animate().rotation(0).setDuration(200).start();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // ── Confirm button ────────────────────────────────────────────────
        btnServiceConfirm.setOnClickListener(v -> {
            String text = etServicesSearch.getText() != null
                    ? etServicesSearch.getText().toString().trim() : "";
            if (!text.isEmpty() && chipServices != null) {
                boolean exists = false;
                for (int i = 0; i < chipServices.getChildCount(); i++) {
                    View child = chipServices.getChildAt(i);
                    TextView tv = child.findViewById(R.id.tv_chip_text);
                    if (tv != null && tv.getText().toString().equals(text)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    addChip(chipServices, text);
                    updateAdapterSelection(servicesAdapter);
                }
            }
            etServicesSearch.setText("");
            rvServicesList.setVisibility(View.GONE);
            if (imgServicesDrop != null) {
                imgServicesDrop.animate().rotation(0).setDuration(200).start();
            }
        });

        // ── Cancel button ─────────────────────────────────────────────────
        btnServiceCancel.setOnClickListener(v -> {
            etServicesSearch.setText("");
            rvServicesList.setVisibility(View.GONE);
            if (imgServicesDrop != null) {
                imgServicesDrop.animate().rotation(0).setDuration(200).start();
            }
        });

        // ── IME action done ───────────────────────────────────────────────
        etServicesSearch.setOnEditorActionListener((tv, actionId, event) -> {
            boolean isDone = actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_DOWN);
            if (!isDone) return false;

            String text = etServicesSearch.getText() != null
                    ? etServicesSearch.getText().toString().trim() : "";
            if (!text.isEmpty() && chipServices != null) {
                boolean exists = false;
                for (int i = 0; i < chipServices.getChildCount(); i++) {
                    View child = chipServices.getChildAt(i);
                    TextView tv1 = child.findViewById(R.id.tv_chip_text);
                    if (tv1 != null && tv1.getText().toString().equals(text)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    addChip(chipServices, text);
                    updateAdapterSelection(servicesAdapter);
                }
            }
            etServicesSearch.setText("");
            rvServicesList.setVisibility(View.GONE);
            if (imgServicesDrop != null) {
                imgServicesDrop.animate().rotation(0).setDuration(200).start();
            }
            return true;
        });

        // ── Dropdown icon click ───────────────────────────────────────────
        if (imgServicesDrop != null) {
            imgServicesDrop.setOnClickListener(v -> {
                // Re-check live state before allowing open
                ChipGroup src = (chipPractice != null) ? chipPractice : chipPracticeLocal;
                boolean hasPANow = src != null
                        && src.getChildCount() > 0
                        && getChipValuesFromGroup(src).length() > 0;
                if (!hasPANow) return;

                if (rvServicesList.getVisibility() == View.VISIBLE) {
                    rvServicesList.setVisibility(View.GONE);
                    imgServicesDrop.animate().rotation(0).setDuration(200).start();
                    etServicesSearch.setText("");
                } else {
                    refreshServicesAdapter(etServicesSearch, servicesAdapter, imgServicesDrop);
                    servicesAdapter.filter("");
                    rvServicesList.setVisibility(View.VISIBLE);
                    imgServicesDrop.animate().rotation(180).setDuration(200).start();
                    etServicesSearch.requestFocus();
                }
            });
        }

        // ── Focus change opens the list ───────────────────────────────────
        etServicesSearch.setOnFocusChangeListener((v, hasFocus) -> {
            // Re-check live state before allowing open
            ChipGroup src = (chipPractice != null) ? chipPractice : chipPracticeLocal;
            boolean hasPANow = src != null
                    && src.getChildCount() > 0
                    && getChipValuesFromGroup(src).length() > 0;
            if (!hasPANow) {
                etServicesSearch.clearFocus();
                return;
            }
            if (hasFocus && rvServicesList.getVisibility() != View.VISIBLE) {
                refreshServicesAdapter(etServicesSearch, servicesAdapter, imgServicesDrop);
                servicesAdapter.filter("");
                rvServicesList.setVisibility(View.VISIBLE);
                if (imgServicesDrop != null) {
                    imgServicesDrop.animate().rotation(180).setDuration(200).start();
                }
            }
        });

        // ── Click on field also opens list ────────────────────────────────
        etServicesSearch.setOnClickListener(v -> {
            // Re-check live state before allowing open
            ChipGroup src = (chipPractice != null) ? chipPractice : chipPracticeLocal;
            boolean hasPANow = src != null
                    && src.getChildCount() > 0
                    && getChipValuesFromGroup(src).length() > 0;
            if (!hasPANow) return;

            if (rvServicesList.getVisibility() != View.VISIBLE) {
                refreshServicesAdapter(etServicesSearch, servicesAdapter, imgServicesDrop);
                servicesAdapter.filter("");
                rvServicesList.setVisibility(View.VISIBLE);
                if (imgServicesDrop != null) {
                    imgServicesDrop.animate().rotation(180).setDuration(200).start();
                }
            }
        });

        // Store reference
        this.imgServicesDrop = imgServicesDrop;
    }

    private void refreshServicesAdapter(TextInputEditText searchEditText,
                                        CommonMultiSelectionAdapter adapter,
                                        ImageView dropIcon) {
        if (adapter == null) return;

        // ── Suggested first, then rest sorted alphabetically ─────────────────
        ArrayList<String> dataToShow = buildSortedServicesList();

        adapter.updateData(dataToShow);

        // ── Re-apply suggested markers (updateData clears nothing, but
        //    setSuggestedItems triggers notifyDataSetChanged) ──────────────────
        adapter.setSuggestedItems(suggestedServicesList, true);
        if (!suggestedServicesList.isEmpty()) {
            if (ll_suggestedServices != null) ll_suggestedServices.setVisibility(VISIBLE);
        } else {
            if (ll_suggestedServices != null) ll_suggestedServices.setVisibility(GONE);
        }
        updateAdapterSelection(adapter);

        if (searchEditText != null) searchEditText.setText("");
    }

    private void updateAdapterSelection(CommonMultiSelectionAdapter adapter) {
        if (adapter == null || chipServices == null) return;

        List<String> selected = new ArrayList<>();
        for (int i = 0; i < chipServices.getChildCount(); i++) {
            View child = chipServices.getChildAt(i);
            TextView tv = child.findViewById(R.id.tv_chip_text);
            if (tv != null) {
                selected.add(tv.getText().toString());
            }
        }

        // Normalize: lowercase + collapse all whitespace, for matching only
        List<String> normalizedSelected = new ArrayList<>();
        for (String s : selected) {
            normalizedSelected.add(s.toLowerCase(Locale.ROOT).replaceAll("\\s+", ""));
        }

        // Build final list using API display names where they match, chip text otherwise
        List<String> resolvedSelected = new ArrayList<>();
        List<String> adapterData = adapter.getAllItems(); // need this method — see below
        for (String chip : selected) {
            String chipNorm = chip.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
            boolean matched = false;
            for (String apiItem : adapterData) {
                String apiNorm = apiItem.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
                if (apiNorm.equals(chipNorm)) {
                    resolvedSelected.add(apiItem); // use API display name
                    matched = true;
                    break;
                }
            }
            if (!matched) resolvedSelected.add(chip); // keep chip text as-is
        }

        adapter.setSelectedItems(resolvedSelected);
    }

    /**
     * Get the current practice area chip values as a List<String>.
     */
    private List<String> getCurrentPracticeAreaValues() {
        List<String> result = new ArrayList<>();
        if (chipPractice == null) return result;
        JSONArray arr = getChipValuesFromGroup(chipPractice);
        for (int i = 0; i < arr.length(); i++) {
            result.add(arr.optString(i, ""));
        }
        return result;
    }

    /**
     * Helper method to determine if we are viewing Firm Profile or My Profile
     */
    private boolean isFirmProfile() {
        return !Constants.isMyProfileClicked;
    }

    private void createSubscriptionDetails() {
        // This method would create the subscription details view
        // For GH Team Member, this tab is hidden, so this method won't be called
        llViewScreen.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View subscriptionView = inflater.inflate(R.layout.view_pp_card, llViewScreen, false);
        // Populate subscription details here
        llViewScreen.addView(subscriptionView);
        llViewScreen.setVisibility(VISIBLE);
    }

    private Uri createImageUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "IMG_" + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        return requireActivity()
                .getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera_new();
        }
    }

    private void openGallery_new() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == REQ_CAMERA && cameraImageUri != null) {
            iv_profile.setImageURI(cameraImageUri);
            profile_upload(cameraImageUri);
        }
        if (requestCode == REQ_GALLERY && data != null && data.getData() != null) {
            Uri galleryUri = data.getData();
            iv_profile.setImageURI(galleryUri);
            profile_upload(galleryUri);
        }
    }

    private void delete() {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(
                    this,
                    getContext(),
                    WebServiceHelper.RestMethodType.DELETE,
                    "v3/profile/pic",
                    "Profile_delete",
                    jsonObject.toString()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, FirmProfileModel.WeeklySchedule> buildWeeklyMap() {
        Map<String, FirmProfileModel.WeeklySchedule> map = new HashMap<>();
        FirmProfileModel.Profile profile =
                firmProfileModel != null &&
                        firmProfileModel.getData() != null ?
                        firmProfileModel.getData().getProfile() : null;
        if (profile != null &&
                profile.getAvailability() != null &&
                profile.getAvailability().getWeekly_schedule() != null) {
            for (FirmProfileModel.WeeklySchedule schedule :
                    profile.getAvailability().getWeekly_schedule()) {
                map.put(schedule.getDay_name(), schedule);
            }
        }
        return map;
    }

    private void profile_upload(Uri imageUri) {
        Log.e("UPLOAD_DEBUG", "profile_upload called with URI: " + imageUri);
        Constants.firm_image = imageUri.toString();
        AndroidUtils.loadProfileImage(
                requireContext(),
                Constants.firm_image,
                iv_profile,
                person_icon
        );
        progress_dialog = AndroidUtils.get_progress(getActivity());
        try {
            File imageFile = getFileFromUri(imageUri);
            Log.e("UPLOAD_DEBUG", "Uploading file: " + imageFile.getName());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "profile_pic");
            WebServiceHelper.callHttpUploadWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "v3/profile/pic/upload",
                    "Profile_upload",
                    imageFile,
                    jsonObject.toString()
            );
        } catch (Exception e) {
            Log.e("UPLOAD_DEBUG", "Upload failed BEFORE API call", e);
            AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    private void loadCaseTypes() {
        List<String> currentlySelected = new ArrayList<>();
        if (chipPractice != null) {
            JSONArray existing = getChipValuesFromGroup(chipPractice);
            for (int i = 0; i < existing.length(); i++) {
                currentlySelected.add(existing.optString(i, ""));
            }
        }
        practiceAreaAdapter = new CommonMultiSelectionAdapter(
                requireContext(),
                caseTypeList,
                currentlySelected,
                selectedItems -> {
                    if (chipPractice != null) {
                        chipPractice.removeAllViews();
                        for (String item : selectedItems) {
                            addChip(chipPractice, item);
                        }
                    }
                    syncCourtSectionFromPracticeChange(); // ← add this
                }
        );
        if (sp_practice != null) {
            sp_practice.setLayoutManager(new LinearLayoutManager(getContext()));
            sp_practice.setAdapter(practiceAreaAdapter);
        }
        if (tl_practice_area != null) {
            tl_practice_area.setOnClickListener(v -> togglePracticeRecyclerView());
        }
        if (img_dropdown_icon != null) {
            img_dropdown_icon.setOnClickListener(v -> togglePracticeRecyclerView());
        }
        if (img_clear_icon != null) {
            img_clear_icon.setVisibility(View.GONE);
        }
    }

    private void togglePracticeRecyclerView() {
        if (sp_practice == null) return;
        if (sp_practice.getVisibility() == View.VISIBLE) {
            sp_practice.setVisibility(View.GONE);
            isCaseTypeChecked = true;
            if (img_dropdown_icon != null) {
                img_dropdown_icon.animate().rotation(0).setDuration(200).start();
            }
        } else {
            sp_practice.setVisibility(View.VISIBLE);
            isCaseTypeChecked = false;
            if (img_dropdown_icon != null) {
                img_dropdown_icon.animate().rotation(180).setDuration(200).start();
            }
        }
    }

    private File getFileFromUri(Uri uri) throws Exception {
        if (uri == null) {
            throw new Exception("URI is null");
        }
        if (!isAdded()) {
            throw new Exception("Fragment not attached");
        }
        InputStream inputStream =
                requireActivity().getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new Exception("Unable to open InputStream for URI: " + uri);
        }
        File file = new File(
                requireActivity().getCacheDir(),
                "profile_" + System.currentTimeMillis() + ".jpg"
        );
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
        if (totalBytes == 0) {
            throw new Exception("File created but empty (0 bytes)");
        }
        Log.e("UPLOAD_DEBUG", "File created: " + file.getAbsolutePath());
        Log.e("UPLOAD_DEBUG", "File size: " + file.length());
        return file;
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkFields();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private void checkFields() {
        boolean shouldDisable =
                et_Firmname.getText() == null || et_Firmname.getText().toString().trim().isEmpty() ||
                        et_Country.getText() == null || et_Country.getText().toString().trim().isEmpty() ||
                        et_contactName.getText() == null || et_contactName.getText().toString().trim().isEmpty() ||
                        et_ContactPhone.getText() == null || et_ContactPhone.getText().toString().trim().isEmpty() ||
                        et_Website.getText() == null || et_Website.getText().toString().trim().isEmpty() || et_bio.getText().toString().trim().isEmpty() ||
                        et_DefaultCurrency.getText() == null || et_DefaultCurrency.getText().toString().trim().isEmpty() ||
                        emailhaserror || phonehaserror ||
                        et_ad_Country.getText() == null || et_ad_Country.getText().toString().trim().isEmpty() ||
                        et_Building.getText() == null || et_Building.getText().toString().trim().isEmpty() ||
                        et_Street.getText() == null || et_Street.getText().toString().trim().isEmpty() ||
                        et_City.getText() == null || et_City.getText().toString().trim().isEmpty() ||
                        et_State.getText() == null || et_State.getText().toString().trim().isEmpty() ||
                        et_Zip.getText() == null || et_Zip.getText().toString().trim().isEmpty() ||
                        ziphaserror ||
                        met_ad_Country.getText() == null || met_ad_Country.getText().toString().trim().isEmpty() ||
                        met_Building.getText() == null || met_Building.getText().toString().trim().isEmpty() ||
                        met_Street.getText() == null || met_Street.getText().toString().trim().isEmpty() ||
                        met_City.getText() == null || met_City.getText().toString().trim().isEmpty() ||
                        met_State.getText() == null || met_State.getText().toString().trim().isEmpty() ||
                        met_Zip.getText() == null || met_Zip.getText().toString().trim().isEmpty() ||
                        mziphaserror;
        btn_save_pp.setEnabled(!shouldDisable);
    }

    private JSONObject buildAddress(
            TextInputEditText building,
            TextInputEditText street,
            TextInputEditText city,
            TextInputEditText state,
            TextView country,
            TextInputEditText zip
    ) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("house_flat_no", building.getText().toString());
        obj.put("street", street.getText().toString());
        obj.put("city_town", city.getText().toString());
        obj.put("state", state.getText().toString());
        obj.put("country", "India");
        obj.put("zipcode", zip.getText().toString());
        return obj;
    }

    private JSONArray getChipValues(ChipGroup chipGroup) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            array.put(chip.getText().toString());
        }
        return array;
    }

    private JSONArray getEducationJson(LinearLayout llEducation) {
        JSONArray array = new JSONArray();
        if (llEducation != null) {
            for (int i = 0; i < llEducation.getChildCount(); i++) {
                View view = llEducation.getChildAt(i);
                EditText etDegree = view.findViewById(R.id.et_title);
                EditText etUniversity = view.findViewById(R.id.et_content);
                EditText etYear = view.findViewById(R.id.et_year);
                try {
                    String degree = etDegree.getText().toString().trim();
                    String university = etUniversity.getText().toString().trim();
                    String yearText = etYear.getText().toString().trim();
                    if (degree.isEmpty() && university.isEmpty() && yearText.isEmpty()) {
                        continue;
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("degree", degree);
                    obj.put("university", university);
                    if (yearText.isEmpty()) {
                        obj.put("passing_year", JSONObject.NULL);
                    } else {
                        obj.put("passing_year", Integer.parseInt(yearText));
                    }
                    array.put(obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return array;
    }

    private JSONArray getCertificationJson(LinearLayout llCertification) {
        JSONArray array = new JSONArray();
        if (llCertification != null) {
            for (int i = 0; i < llCertification.getChildCount(); i++) {
                View view = llCertification.getChildAt(i);
                EditText etName = view.findViewById(R.id.et_title);
                EditText etAuthority = view.findViewById(R.id.et_content);
                EditText etYear = view.findViewById(R.id.et_year);
                String name = etName.getText().toString().trim();
                String authority = etAuthority.getText().toString().trim();
                String yearText = etYear.getText().toString().trim();
                if (name.isEmpty() && authority.isEmpty() && yearText.isEmpty()) {
                    continue;
                }
                JSONObject obj = new JSONObject();
                try {
                    obj.put("certification_name", name);
                    obj.put("issuing_authority", authority);
                    if (yearText.isEmpty()) {
                        obj.put("year_of_issue", JSONObject.NULL);
                    } else {
                        obj.put("year_of_issue", Integer.parseInt(yearText));
                    }
                    array.put(obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return array;
    }

    private JSONArray getAwardsJson(LinearLayout llAwards) {
        JSONArray array = new JSONArray();
        if (llAwards == null) return array;
        for (int i = 0; i < llAwards.getChildCount(); i++) {
            View view = llAwards.getChildAt(i);
            EditText etName = view.findViewById(R.id.et_title);
            EditText etYear = view.findViewById(R.id.et_year);
            EditText etPurpose = view.findViewById(R.id.et_content);
            String name = etName.getText().toString().trim();
            String yearStr = etYear.getText().toString().trim();
            String purpose = etPurpose.getText().toString().trim();
            if (name.isEmpty() && yearStr.isEmpty() && purpose.isEmpty()) {
                continue;
            }
            JSONObject obj = new JSONObject();
            try {
                obj.put("award_name", name);
                if (!yearStr.isEmpty()) {
                    obj.put("year_of_award", Integer.parseInt(yearStr));
                } else {
                    obj.put("year_of_award", JSONObject.NULL);
                }
                obj.put("purpose", purpose);
                array.put(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return array;
    }

    private JSONArray getExpertSlotsFromLayout(FlexboxLayout layout) throws Exception {
        JSONArray expertSlots = new JSONArray();
        for (int i = 0; i < layout.getChildCount(); i++) {
            TextView tv = (TextView) layout.getChildAt(i);
            expertSlots.put(slotToJson(tv.getText().toString()));
        }
        return expertSlots;
    }

    private String to24Hour(String time12h) throws Exception {
        SimpleDateFormat in = new SimpleDateFormat("hh:mm a", Locale.US);
        SimpleDateFormat out = new SimpleDateFormat("HH:mm", Locale.US);
        return out.format(in.parse(time12h));
    }

    private JSONObject slotToJson(String slotText) throws Exception {
        String[] parts = slotText.split(" - ");
        JSONObject obj = new JSONObject();
        obj.put("start_time", to24Hour(parts[0]));
        obj.put("end_time", to24Hour(parts[1]));
        return obj;
    }

    @SuppressLint("SetTextI18n")
    private void bindFirmPracticeDetails(View view) {
        // ── Same guard as createPracticeDetails(): never touch firmProfileModel
        //    fields if the model itself isn't ready. ────────────────────────
        if (firmProfileModel == null
                || firmProfileModel.getData() == null
                || firmProfileModel.getData().getProfile() == null) {
            return;
        }

        FirmProfileModel.Profile profile = firmProfileModel.getData().getProfile();
        FirmProfileModel.Firm firm = profile.getFirm();
        boolean fp = isFirmProfile();

        etFirmName = view.findViewById(R.id.et_Firmname);
        etExperience = view.findViewById(R.id.et_year_exp);

        // ── Year: Firm Profile → years_of_incorporation, My Profile → years_of_experience ──
        int expValue = fp ? firm.getYears_of_incorporation() : profile.getYears_of_experience();
        etExperience.setText(expValue > 0 ? String.valueOf(expValue) : "");
        etExperience.setInputType(InputType.TYPE_CLASS_NUMBER);
        etExperience.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        etLanguages = view.findViewById(R.id.et_languages);
        etLanguages.setHint(R.string.select_languages_spoken);
        etServices = view.findViewById(R.id.et_services_search);
        chipPractice = view.findViewById(R.id.chipGroupPractice);
        // ── Practice areas: Firm Profile → firm.practice_areas, My Profile → profile.practice_areas ──
        JSONArray practiceSource = (liveChipPracticeAreas != null)
                ? liveChipPracticeAreas
                : (fp ? firm.getPractice_areas() : profile.getPractice_areas());
        setChips(chipPractice, practiceSource);
        tv_DefaultCurrency = view.findViewById(R.id.tv_DefaultCurrency);
        tv_DefaultCurrency.setText(R.string.billing_currency);
        chipLanguages = view.findViewById(R.id.chipGroupLanguages);
        JSONArray langSource = (liveChipLanguages != null)
                ? liveChipLanguages
                : profile.getLanguages_spoken();
        setChips(chipLanguages, langSource);

        chipServices = view.findViewById(R.id.chipGroupServices);
        JSONArray servicesSource = (liveChipServices != null)
                ? liveChipServices
                : (fp ? firm.getServices_offered() : profile.getServices_offered());
        setChips(chipServices, servicesSource);

        // ── Wire services-offered API dropdown ────────────────────────────────
        // Remove old: setupChipInput(etServices, chipServices);
        // Replace with API-powered dropdown:
        setupServicesOfferedUI(view);

        setupChipInput(etLanguages, chipLanguages);
        // NOTE: setupChipInput(etServices, chipServices) is intentionally removed above

        et_Building = view.findViewById(R.id.et_Building);
        et_Street = view.findViewById(R.id.et_Street);
        et_City = view.findViewById(R.id.et_City);
        et_State = view.findViewById(R.id.et_State);
        et_Zip = view.findViewById(R.id.et_Zip);

        sp_et_ad_country = view.findViewById(R.id.sp_et_ad_country);
        ll_et_ad_country = view.findViewById(R.id.ll_et_ad_country);
        et_ad_Country = ll_et_ad_country.findViewById(R.id.tv_spinner_view);
        img_dropdown_icon_adCountry = ll_et_ad_country.findViewById(R.id.img_dropdown_icon);
        img_clear_icon_adCountry = ll_et_ad_country.findViewById(R.id.img_clear_icon);

        // Default: country dropdown disabled (India is pre-set)
        sp_et_ad_country.setEnabled(false);
        sp_et_ad_country.setClickable(false);
        ll_et_ad_country.setEnabled(false);
        ll_et_ad_country.setClickable(false);
        ll_et_ad_country.setFocusable(false);
        img_dropdown_icon_adCountry.setVisibility(View.GONE);
        img_clear_icon_adCountry.setVisibility(View.GONE);

        if (firm.getAddress() != null) {
            FirmProfileModel.Address address = firm.getAddress();
            if (et_Building != null) et_Building.setText(address.getHouse_flat_no());
            if (et_Street != null) et_Street.setText(address.getStreet());
            if (et_City != null) et_City.setText(address.getCity_town());
            if (et_State != null) et_State.setText(address.getState());
            if (et_ad_Country != null) et_ad_Country.setText(address.getCountry());
            if (et_Zip != null) et_Zip.setText(address.getZipcode());
        }

        met_Building = view.findViewById(R.id.met_Building);
        met_Street = view.findViewById(R.id.met_Street);
        met_City = view.findViewById(R.id.met_City);
        met_State = view.findViewById(R.id.met_State);
        met_Zip = view.findViewById(R.id.met_Zip);

        // ── Reapply 6-digit ZIP filter after re-binding views ────────────────
        et_Zip.setInputType(InputType.TYPE_CLASS_NUMBER);
        met_Zip.setInputType(InputType.TYPE_CLASS_NUMBER);

        InputFilter[] zipFilters = new InputFilter[]{
                new InputFilter.LengthFilter(6),
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
        };
        et_Zip.setFilters(zipFilters);
        met_Zip.setFilters(zipFilters);

        et_Zip.addTextChangedListener(new Validation(et_Zip));
        met_Zip.addTextChangedListener(new Validation(met_Zip));
        AndroidUtils.NumberFilter(et_ContactPhone, true);

        sp_met_ad_country = view.findViewById(R.id.sp_met_ad_country);
        ll_met_ad_country = view.findViewById(R.id.ll_met_ad_country);
        met_ad_Country = ll_met_ad_country.findViewById(R.id.tv_spinner_view);
        img_dropdown_icon_madCountry = ll_met_ad_country.findViewById(R.id.img_dropdown_icon);
        img_clear_icon_madCountry = ll_met_ad_country.findViewById(R.id.img_clear_icon);

        if (firm.getCorrespondence_address() != null) {
            FirmProfileModel.Address mAddress = firm.getCorrespondence_address();
            if (met_Building != null) met_Building.setText(mAddress.getHouse_flat_no());
            if (met_Street != null) met_Street.setText(mAddress.getStreet());
            if (met_City != null) met_City.setText(mAddress.getCity_town());
            if (met_State != null) met_State.setText(mAddress.getState());
            if (met_ad_Country != null) met_ad_Country.setText(mAddress.getCountry());
            if (met_Zip != null) met_Zip.setText(mAddress.getZipcode());
        }

        loadCountryData();

        // Country / address listeners — only wire up if NOT GH/TM
        boolean isGHTeamMember = Constants.ROLE.equalsIgnoreCase("GH") ||
                Constants.ROLE.equalsIgnoreCase("TM");

        if (!isGHTeamMember) {

            // Item selected — registered country
            sp_et_ad_country.setOnItemClickListener((parent, v, position, id) -> {
                country_name2 = countriesList.get(position).getName();
                AndroidUtils.DisplaySpinnerView(sp_et_ad_country, et_ad_Country, country_name2,
                        img_dropdown_icon_adCountry, img_clear_icon_adCountry,
                        false, adCountryAdapter, "Search Country");
                iscountry_checked2 = true;
            });

            // Clear — registered country
            img_clear_icon_adCountry.setOnClickListener(v -> {
                country_name2 = "";
                AndroidUtils.DisplaySpinnerView(sp_et_ad_country, et_ad_Country, "",
                        img_dropdown_icon_adCountry, img_clear_icon_adCountry,
                        false, adCountryAdapter, "Search Country");
                iscountry_checked2 = true;
            });

            // Item selected — mailing country
            sp_met_ad_country.setOnItemClickListener((parent, v, position, id) -> {
                country_name3 = countriesList.get(position).getName();
                AndroidUtils.DisplaySpinnerView(sp_met_ad_country, met_ad_Country, country_name3,
                        img_dropdown_icon_madCountry, img_clear_icon_madCountry,
                        false, madCountryAdapter, "Search Country");
                iscountry_checked3 = true;
            });

            // Clear — mailing country
            img_clear_icon_madCountry.setOnClickListener(v -> {
                country_name3 = "";
                AndroidUtils.DisplaySpinnerView(sp_met_ad_country, met_ad_Country, "",
                        img_dropdown_icon_madCountry, img_clear_icon_madCountry,
                        false, madCountryAdapter, "Search Country");
                iscountry_checked3 = true;
            });
        }

        // ── Bar Council ID vs Registration ID ────────────────────────────────
        et_counsel_id = view.findViewById(R.id.et_counsel_id);
        et_counsel_id.addTextChangedListener(new Validation(et_counsel_id));
        if (et_counsel_id != null) {
            // Firm Profile → reg_id from firm, My Profile → bar_council_id from profile
            String barId = fp ? firm.getReg_id() : profile.getBar_council_id();
            et_counsel_id.setText(barId != null ? barId : "");
        }
// ── Consultation Fee & Default Currency — now in practicedetails_edit ────
        ll_confee_row = view.findViewById(R.id.ll_confee_row);
        tv_confees = view.findViewById(R.id.tv_confees);
        tv_confees.setText(R.string.con_fees);
        ll_et_confees = view.findViewById(R.id.ll_et_confees);
        if (ll_et_confees != null) {
            et_confees = ll_et_confees.findViewById(R.id.et_title);
            tv_minus = ll_et_confees.findViewById(R.id.tv_minus);
            tv_plus = ll_et_confees.findViewById(R.id.tv_plus);
            AndroidUtils.setupStepper(et_confees, tv_minus, tv_plus, 500, 500, 2000, 100);
            et_confees.setHint("Starts from ₹500");
        }
        billing_layout = view.findViewById(R.id.billing_layout);
        billing_layout.setVisibility(VISIBLE);
        sp_et_DefaultCurrency = view.findViewById(R.id.sp_et_DefaultCurrency);
        ll_et_DefaultCurrency = view.findViewById(R.id.ll_et_DefaultCurrency);
        if (ll_et_DefaultCurrency != null) {
            et_DefaultCurrency = ll_et_DefaultCurrency.findViewById(R.id.tv_spinner_view);
            img_dropdown_icon_Currency = ll_et_DefaultCurrency.findViewById(R.id.img_dropdown_icon);
            img_clear_icon_Currency = ll_et_DefaultCurrency.findViewById(R.id.img_clear_icon);
            et_DefaultCurrency.setEnabled(false);
            img_clear_icon_Currency.setEnabled(false);
            img_dropdown_icon_Currency.setEnabled(false);
            AndroidUtils.ToggleButton(0, billing_layout);
            AndroidUtils.ToggleButton(0, ll_et_DefaultCurrency);
            currencyAdapter = new CommonSpinnerAdapter(getActivity(), currency_list);
            sp_et_DefaultCurrency.setAdapter(currencyAdapter);

            String loadedCurrency = firm.getBilling_currency() != null ? firm.getBilling_currency() : "";
            default_currency = loadedCurrency;
            AndroidUtils.DisplaySpinnerView(sp_et_DefaultCurrency, et_DefaultCurrency,
                    loadedCurrency, img_dropdown_icon_Currency, img_clear_icon_Currency,
                    false, currencyAdapter, "Search Currency");

            ll_et_DefaultCurrency.setOnClickListener(v -> {
                boolean isVisible = sp_et_DefaultCurrency != null
                        && sp_et_DefaultCurrency.getVisibility() == View.VISIBLE;
                AndroidUtils.DisplaySpinnerView(sp_et_DefaultCurrency, et_DefaultCurrency,
                        default_currency, img_dropdown_icon_Currency, img_clear_icon_Currency,
                        !isVisible, currencyAdapter, "Search Currency");
            });

            sp_et_DefaultCurrency.setOnItemClickListener((parent, v, position, id) -> {
                default_currency = currency_list.get(position);
                AndroidUtils.DisplaySpinnerView(sp_et_DefaultCurrency, et_DefaultCurrency,
                        default_currency, img_dropdown_icon_Currency, img_clear_icon_Currency,
                        false, currencyAdapter, "Search Currency");
            });

            img_clear_icon_Currency.setOnClickListener(v -> {
                default_currency = "";
                AndroidUtils.DisplaySpinnerView(sp_et_DefaultCurrency, et_DefaultCurrency, "",
                        img_dropdown_icon_Currency, img_clear_icon_Currency,
                        false, currencyAdapter, "Search Currency");
            });
        }

// ── Load consultation fee value ──────────────────────────────────────────
        if (et_confees != null && !fp) {
            String fee = firmProfileModel.getData().getProfile().getConsultation_fee() != null
                    ? firmProfileModel.getData().getProfile().getConsultation_fee().getAmount() : "";
            et_confees.setText(!TextUtils.isEmpty(fee) ? fee : "");
        }

// ── Hide consultation fee row for Firm Profile ────────────────────────────
        if (ll_confee_row != null) {
            ll_confee_row.setVisibility(fp ? View.GONE : View.VISIBLE);
        }
        // ── GH / TM: disable all address fields in Practice Details tab ──
        if (isGHTeamMember) {
            // Registered address
            setEditTextState(et_Building, false);
            setEditTextState(et_Street, false);
            setEditTextState(et_City, false);
            setEditTextState(et_State, false);
            setEditTextState(et_Zip, false);
            setLayoutState(ll_et_ad_country, false);
            img_dropdown_icon_adCountry.setVisibility(View.GONE);
            img_clear_icon_adCountry.setVisibility(View.GONE);
            et_ad_Country.setEnabled(false);
            et_ad_Country.setClickable(false);
            et_ad_Country.setAlpha(0.6f);

            // Mailing address
            setEditTextState(met_Building, false);
            setEditTextState(met_Street, false);
            setEditTextState(met_City, false);
            setEditTextState(met_State, false);
            setEditTextState(met_Zip, false);
            setLayoutState(ll_met_ad_country, false);
            img_dropdown_icon_madCountry.setVisibility(View.GONE);
            img_clear_icon_madCountry.setVisibility(View.GONE);
            met_ad_Country.setEnabled(false);
            met_ad_Country.setClickable(false);
            met_ad_Country.setAlpha(0.6f);

            // Hide same-address checkbox
            CheckBox cbYesPractice = view.findViewById(R.id.cb_yes);
            if (cbYesPractice != null) cbYesPractice.setVisibility(View.GONE);
        }
    }

    private JSONObject buildProfilePatchPayload() throws JSONException {
        JSONObject root = new JSONObject();
        JSONObject firm = new JSONObject();
        boolean fp = isFirmProfile();
        boolean isGHorTM = Constants.ROLE.equalsIgnoreCase("GH") ||
                Constants.ROLE.equalsIgnoreCase("TM");

        // ── Firm name ────────────────────────────────────────────────
        if (!isGHorTM) {
            String cur = "";
            if ("solo".equalsIgnoreCase(Constants.CATEGORY)) {
                cur = Objects.requireNonNull(et_contactName.getText()).toString();
            } else {
                cur = Objects.requireNonNull(et_Firmname.getText()).toString();
            }
            if (!cur.equals(originalValues.getOrDefault("fullname", ""))) {
                firm.put("fullname", cur);
            }
        }

        // ── Email: key depends on mode ────────────────────────────────
        String curEmail = et_Email.getText().toString();
        if (!curEmail.equals(originalValues.getOrDefault("email", ""))) {
            if (fp) firm.put("email", curEmail);    // firm array email
            else if ("solo".equals(Constants.CATEGORY)) {
                root.put("email", curEmail);
                firm.put("email", curEmail); // profile-level email
            } else {
                root.put("email", curEmail);     // profile-level email
            }
        }

        // Fix:
        String curPhone = et_ContactPhone.getText().toString();
        if (!curPhone.equals(originalValues.getOrDefault("mobile", ""))) {
            if (fp) {
                firm.put("contact_phone", curPhone);  // firm profile → firm object
            } else {
                root.put("mobile", curPhone);         // my profile → root
            }
        }

        // ── Contact person / Name ─────────────────────────────────────
        String curContact = et_contactName.getText().toString();
        if (fp) {
            if (!curContact.equals(originalValues.getOrDefault("contact_person", "")))
                firm.put("contact_person", curContact);
        } else if (isGHorTM) {
            if (!curContact.equals(originalValues.getOrDefault("contact_person", "")))
                root.put("name", curContact);        // GH/TM: own name
        } else {
            if (!curContact.equals(originalValues.getOrDefault("contact_person", "")))
                root.put("name", curContact);        // GH/TM: own name
        }

        // ── Website: firm array, GH/TM cannot edit ───────────────────
        if (!isGHorTM) {
            String curWeb = et_Website.getText().toString();
            if (!curWeb.equals(originalValues.getOrDefault("website", "")))
                firm.put("website", curWeb);
        }

        String curBio = et_bio.getText().toString();
        String origKey = fp ? "firm_description" : "bio_description";
        if (!curBio.equals(originalValues.getOrDefault(origKey, ""))) {
            if (fp) firm.put(origKey, curBio);
            else root.put("bio_description", curBio);
        }

        // ── Consultation fee: My Profile only (hidden for Firm Profile) ──
        if (!fp) {
            String curFee = et_confees.getText().toString().trim().replace("₹", "").trim();
            if (!curFee.equals(originalValues.getOrDefault("consultation_fee", ""))) {
                JSONObject consultFee = new JSONObject();
                consultFee.put("amount", curFee);
                root.put("consultation_fee", consultFee);
            }
        }

        // Gender and DOB for solo category
        if ("solo".equalsIgnoreCase(Constants.CATEGORY)) {
            String currentGender = met_ad_gender.getText().toString().toLowerCase(Locale.ROOT);
            if (!currentGender.equals(originalValues.getOrDefault("gender", ""))) {
                root.put("gender", currentGender);
            }
            String currentDob = AndroidUtils.convertAnyDateToDDMMYYYY(et_dob.getText().toString().trim());
            String originalDob = (String) originalValues.getOrDefault("date_of_birth", "");
            String formattedDob = "";
            if (!TextUtils.isEmpty(currentDob)) {
                formattedDob = AndroidUtils.convertAnyDateToYYYYMMDD(currentDob);
            }
            if (!formattedDob.equals(originalDob)) {
                root.put("date_of_birth", formattedDob);
            }
        }

        // Address fields — GH/TM cannot edit, so skip
        if (!isGHorTM) {
            // Registered address
            String currentBuilding = et_Building.getText() != null ? et_Building.getText().toString() : "";
            String currentStreet = et_Street.getText() != null ? et_Street.getText().toString() : "";
            String currentCity = et_City.getText() != null ? et_City.getText().toString() : "";
            String currentState = et_State.getText() != null ? et_State.getText().toString() : "";
            String currentAdCountry = et_ad_Country.getText() != null ? et_ad_Country.getText().toString() : "";
            String currentZip = et_Zip.getText() != null ? et_Zip.getText().toString() : "";

            boolean addressChanged =
                    !currentBuilding.equals(originalValues.getOrDefault("address_house_flat_no", "")) ||
                            !currentStreet.equals(originalValues.getOrDefault("address_street", "")) ||
                            !currentCity.equals(originalValues.getOrDefault("address_city_town", "")) ||
                            !currentState.equals(originalValues.getOrDefault("address_state", "")) ||
                            !currentAdCountry.equals(originalValues.getOrDefault("address_country", "India")) ||
                            !currentZip.equals(originalValues.getOrDefault("address_zipcode", ""));

            if (addressChanged) {
                JSONObject address = new JSONObject();
                address.put("house_flat_no", currentBuilding);
                address.put("street", currentStreet);
                address.put("city_town", currentCity);
                address.put("state", currentState);
                address.put("country", "India");
                address.put("zipcode", currentZip);
                firm.put("address", address);
            }

            // Mailing / correspondence address
            String mBuilding = met_Building.getText() != null ? met_Building.getText().toString() : "";
            String mStreet = met_Street.getText() != null ? met_Street.getText().toString() : "";
            String mCity = met_City.getText() != null ? met_City.getText().toString() : "";
            String mState = met_State.getText() != null ? met_State.getText().toString() : "";
            String mCountry = met_ad_Country.getText() != null ? met_ad_Country.getText().toString() : "";
            String mZip = met_Zip.getText() != null ? met_Zip.getText().toString() : "";

            boolean mAddressChanged =
                    !mBuilding.equals(originalValues.getOrDefault("correspondence_house_flat_no", "")) ||
                            !mStreet.equals(originalValues.getOrDefault("correspondence_street", "")) ||
                            !mCity.equals(originalValues.getOrDefault("correspondence_city_town", "")) ||
                            !mState.equals(originalValues.getOrDefault("correspondence_state", "")) ||
                            !mCountry.equals(originalValues.getOrDefault("correspondence_country", "India")) ||
                            !mZip.equals(originalValues.getOrDefault("correspondence_zipcode", ""));

            if (mAddressChanged) {
                JSONObject mAddress = new JSONObject();
                mAddress.put("house_flat_no", mBuilding);
                mAddress.put("street", mStreet);
                mAddress.put("city_town", mCity);
                mAddress.put("state", mState);
                mAddress.put("country", "India");
                mAddress.put("zipcode", mZip);
                firm.put("correspondence_address", mAddress);
            }
        }

        // Only add firm object if it has any fields
        if (firm.length() > 0) {
            root.put("firm", firm);
        }

        return root;
    }

    private JSONObject buildadditional() throws Exception {
        JSONObject root = new JSONObject();
        JSONObject firm = new JSONObject();
        boolean fp = isFirmProfile();

        // ── Bio in additional info (if edited here too) ───────────────
        String curBio = (et_bio != null && et_bio.getText() != null)
                ? et_bio.getText().toString().trim() : "";
        if (!curBio.equals(originalValues.getOrDefault("bio_description", ""))) {
            if (fp) firm.put("about", curBio);
            else root.put("bio_description", curBio);
        }
        if (!fp) {
            String curFee = et_confees.getText().toString().trim().replace("₹", "").trim();
            if (!curFee.equals(originalValues.getOrDefault("consultation_fee", ""))) {
                JSONObject consultFee = new JSONObject();
                consultFee.put("amount", curFee);
                root.put("consultation_fee", consultFee);
            }
        }
        // ── Bar Council ID vs Registration ID ────────────────────────
        String curBarId = (et_counsel_id != null && et_counsel_id.getText() != null)
                ? et_counsel_id.getText().toString().trim() : "";
        String origBarId = (String) originalValues.getOrDefault("bar_council_id", "");
        if (!curBarId.equals(origBarId)) {
            if (fp) firm.put("reg_id", curBarId);
            else root.put("bar_council_id", curBarId);
        }

        // ── Year of Experience vs Year of Incorporation ───────────────
        String curExp = (etExperience != null && etExperience.getText() != null)
                ? etExperience.getText().toString().trim() : "";
        String origExp = (String) originalValues.getOrDefault("years_of_experience", "0");
        if (!curExp.equals(origExp)) {
            if (fp) {
                firm.put("years_of_incorporation",
                        curExp.isEmpty() ? JSONObject.NULL : Integer.parseInt(curExp));
            } else {
                root.put("years_of_experience",
                        curExp.isEmpty() ? JSONObject.NULL : Integer.parseInt(curExp));
            }
        }

        // ── Practice areas ────────────────────────────────────────────
        JSONArray currentPracticeAreas = chipPractice != null
                ? getChipValuesFromGroup(chipPractice) : new JSONArray();
        if (!currentPracticeAreas.toString().equals(
                originalPracticeAreas != null ? originalPracticeAreas.toString() : "[]")) {
            if (fp) {
                firm.put("practice_areas", currentPracticeAreas);
            } else {
                root.put("practice_areas", currentPracticeAreas);
            }
        }

        // ── Cases Handled: My Profile only ───────────────────────────────────
        if (!fp && chipCasesHandled != null) {
            JSONArray curCases = getChipValuesFromGroup(chipCasesHandled);
            if (!curCases.toString().equals(
                    originalCasesHandled != null ? originalCasesHandled.toString() : "[]")) {
                root.put("cases_handled", curCases);
            }
        }

        // ── Court Enrollments: My Profile only ───────────────────────────────
        if (!fp && llCourtEnrollments != null) {
            JSONArray curCourts = getCourtEnrollmentsJson(llCourtEnrollments);
            // ✅ Always send court_enrollments regardless of length
            // Empty array tells API to clear all enrollments
            root.put("court_enrollments", curCourts);
        }

        // ── Services Offered ──────────────────────────────────────────
        JSONArray currentServices = chipServices != null
                ? getChipValuesFromGroup(chipServices) : new JSONArray();
        if (!currentServices.toString().equals(
                originalServices != null ? originalServices.toString() : "[]")) {
            if (fp) {
                firm.put("services_offered", currentServices);
            } else {
                root.put("services_offered", currentServices);
            }
        }

        // ── Languages: My Profile only (hidden for Firm Profile) ──────
        if (!fp && chipLanguages != null) {
            JSONArray curLangs = getChipValuesFromGroup(chipLanguages);
            if (!curLangs.toString().equals(
                    originalLanguages != null ? originalLanguages.toString() : "[]")) {
                root.put("languages_spoken", curLangs);
            }
        }

        // ── Education + Certifications: My Profile only ───────────────
        if (!fp) {
            if (llEducation != null) {
                JSONArray cur = getEducationJson(llEducation);
                if (!cur.toString().equals(
                        originalEducation != null ? originalEducation.toString() : "[]"))
                    root.put("education", cur);
            }
            if (llCertification != null) {
                JSONArray cur = getCertificationJson(llCertification);
                if (!cur.toString().equals(
                        originalCertifications != null ? originalCertifications.toString() : "[]"))
                    root.put("certifications", cur);
            }
        }

        // ── Awards: both modes ────────────────────────────────────────
        if (llAwards != null) {
            JSONArray cur = getAwardsJson(llAwards);
            if (!cur.toString().equals(
                    originalAwards != null ? originalAwards.toString() : "[]"))
                if (fp) {
                    firm.put("awards", cur);
                } else {
                    root.put("awards", cur);
                }
        }

        // ── Availability: My Profile only ─────────────────────────────
        if (!fp) {
            JSONArray weekly = buildWeeklySchedule();
            if (isWeeklyScheduleChanged(weekly) && weekly.length() > 0) {
                JSONObject avail = new JSONObject();
                avail.put("weekly_schedule", weekly);
                root.put("availability", avail);
            }
        }

        // ── Address diff ──────────────────────────────────────────────
        JSONObject address = buildAddressIfChanged();
        if (address != null && address.length() > 0) {
            firm.put("address", address);
        }

        JSONObject correspondenceAddress = buildCorrespondenceAddressIfChanged();
        if (correspondenceAddress != null && correspondenceAddress.length() > 0) {
            firm.put("correspondence_address", correspondenceAddress);
        }

        if (firm.length() > 0) root.put("firm", firm);
        return root;
    }

    private JSONArray getCourtEnrollmentsJson(LinearLayout llCourts) {
        JSONArray array = new JSONArray();
        if (llCourts == null) return array;
        if (llCourts.getChildCount() == 0) return array;

        for (int i = 0; i < llCourts.getChildCount(); i++) {
            View row = llCourts.getChildAt(i);

            // ── Court Type ────────────────────────────────────────────────────────
            LinearLayout llCt = row.findViewById(R.id.ll_court_type);
            TextView tvCt = llCt != null ? llCt.findViewById(R.id.tv_spinner_view) : null;
            String courtType = tvCt != null ? tvCt.getText().toString().trim() : "";
            String apiCourtType = convertCourtTypeToApiFormat(courtType);

            // ── State ─────────────────────────────────────────────────────────────
            LinearLayout llStateSpin = row.findViewById(R.id.ll_state_spinner);
            TextView tvState = llStateSpin != null
                    ? llStateSpin.findViewById(R.id.tv_spinner_view) : null;
            String stateVal = tvState != null ? tvState.getText().toString().trim() : "";

            // ── City - Get from TextInputEditText (NOT from spinner) ─────────────
            TextInputEditText etCitySearch = row.findViewById(R.id.et_city_search);
            String cityVal = etCitySearch != null ? etCitySearch.getText().toString().trim() : "";

            // ── Skip completely blank rows ────────────────────────────────────────
            if (courtType.isEmpty() && stateVal.isEmpty() && cityVal.isEmpty()) {
                continue;
            }

            try {
                JSONObject obj = new JSONObject();
                obj.put("state", stateVal);
                obj.put("city", cityVal);
                obj.put("court_name", apiCourtType);
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array;
    }

    private JSONObject buildAddressIfChanged() throws JSONException {
        // Check if ANY address field has changed
        String currentBuilding = et_Building.getText().toString();
        String currentStreet = et_Street.getText().toString();
        String currentCity = et_City.getText().toString();
        String currentState = et_State.getText().toString();
        String currentCountry = et_ad_Country.getText().toString();
        String currentZip = et_Zip.getText().toString();

        String originalBuilding = (String) originalValues.getOrDefault("address_house_flat_no", "");
        String originalStreet = (String) originalValues.getOrDefault("address_street", "");
        String originalCity = (String) originalValues.getOrDefault("address_city_town", "");
        String originalState = (String) originalValues.getOrDefault("address_state", "");
        String originalCountry = (String) originalValues.getOrDefault("address_country", "India");
        String originalZip = (String) originalValues.getOrDefault("address_zipcode", "");

        // Check if any field has changed
        boolean hasChanged = !currentBuilding.equals(originalBuilding) ||
                !currentStreet.equals(originalStreet) ||
                !currentCity.equals(originalCity) ||
                !currentState.equals(originalState) ||
                !currentCountry.equals(originalCountry) ||
                !currentZip.equals(originalZip);

        if (hasChanged) {
            // Send the COMPLETE address object
            JSONObject address = new JSONObject();
            address.put("house_flat_no", currentBuilding);
            address.put("street", currentStreet);
            address.put("city_town", currentCity);
            address.put("state", currentState);
            address.put("country", "India");
            address.put("zipcode", currentZip);
            return address;
        }

        return null;
    }

    private JSONObject buildCorrespondenceAddressIfChanged() throws JSONException {
        // Check if ANY correspondence address field has changed
        String currentBuilding = met_Building.getText().toString();
        String currentStreet = met_Street.getText().toString();
        String currentCity = met_City.getText().toString();
        String currentState = met_State.getText().toString();
        String currentCountry = met_ad_Country.getText().toString();
        String currentZip = met_Zip.getText().toString();

        String originalBuilding = (String) originalValues.getOrDefault("correspondence_house_flat_no", "");
        String originalStreet = (String) originalValues.getOrDefault("correspondence_street", "");
        String originalCity = (String) originalValues.getOrDefault("correspondence_city_town", "");
        String originalState = (String) originalValues.getOrDefault("correspondence_state", "");
        String originalCountry = (String) originalValues.getOrDefault("correspondence_country", "India");
        String originalZip = (String) originalValues.getOrDefault("correspondence_zipcode", "");

        // Check if any field has changed
        boolean hasChanged = !currentBuilding.equals(originalBuilding) ||
                !currentStreet.equals(originalStreet) ||
                !currentCity.equals(originalCity) ||
                !currentState.equals(originalState) ||
                !currentCountry.equals(originalCountry) ||
                !currentZip.equals(originalZip);

        if (hasChanged) {
            // Send the COMPLETE correspondence address object
            JSONObject address = new JSONObject();
            address.put("house_flat_no", currentBuilding);
            address.put("street", currentStreet);
            address.put("city_town", currentCity);
            address.put("state", currentState);
            address.put("country", "India");
            address.put("zipcode", currentZip);
            return address;
        }

        return null;
    }

    private boolean isWeeklyScheduleChanged(JSONArray currentSchedule) throws JSONException {
        if (originalWeeklySchedule == null && currentSchedule.length() == 0) return false;
        if (originalWeeklySchedule == null && currentSchedule.length() > 0) return true;

        // Build original schedule JSON for comparison
        JSONArray originalSchedule = new JSONArray();
        for (FirmProfileModel.WeeklySchedule schedule : originalWeeklySchedule) {
            JSONObject day = new JSONObject();
            day.put("day_name", schedule.getDay_name());
            day.put("date", schedule.getDate());
            day.put("is_working_day", schedule.isIs_working_day());

            JSONArray workSlots = new JSONArray();
            if (schedule.getWork_slots() != null) {
                for (FirmProfileModel.WorkSlot slot : schedule.getWork_slots()) {
                    JSONObject work = new JSONObject();
                    work.put("start_time", slot.getStart_time());
                    work.put("end_time", slot.getEnd_time());
                    workSlots.put(work);
                }
            }
            day.put("work_slots", workSlots);

            JSONArray expertSlots = new JSONArray();
            if (schedule.getExpert_slots() != null) {
                for (FirmProfileModel.WorkSlot slot : schedule.getExpert_slots()) {
                    JSONObject expert = new JSONObject();
                    expert.put("start_time", slot.getStart_time());
                    expert.put("end_time", slot.getEnd_time());
                    expertSlots.put(expert);
                }
            }
            day.put("expert_slots", expertSlots);
            originalSchedule.put(day);
        }

        // FIX: Return true if changed (not false)
        return !currentSchedule.toString().equals(originalSchedule.toString());
    }

    private void createPendingChips() {
        if (etPractice != null && etPractice.getText() != null) {
            String text = etPractice.getText().toString().trim();
            if (!text.isEmpty() && chipPractice != null) {
                addChip(chipPractice, text);
                etPractice.setText("");
            }
        }
        if (etLanguages != null && etLanguages.getText() != null) {
            String text = etLanguages.getText().toString().trim();
            if (!text.isEmpty() && chipLanguages != null) {
                addChip(chipLanguages, text);
                etLanguages.setText("");
            }
        }
        if (etServices != null && etServices.getText() != null) {
            String text = etServices.getText().toString().trim();
            if (!text.isEmpty() && chipServices != null) {
                addChip(chipServices, text);
                etServices.setText("");
            }
        }
        // Update this block to use et_services_search
        if (llViewScreen != null && llViewScreen.getChildCount() > 0) {
            View practiceView = llViewScreen.getChildAt(0);
            if (practiceView != null) {
                TextInputEditText etServicesSearch = practiceView.findViewById(R.id.et_services_search);
                if (etServicesSearch != null
                        && etServicesSearch.getVisibility() == View.VISIBLE
                        && etServicesSearch.getText() != null
                        && chipServices != null) {
                    String text = etServicesSearch.getText().toString().trim();
                    if (!text.isEmpty()) {
                        addChip(chipServices, text);
                        etServicesSearch.setText("");
                    }
                }
            }
        }
    }

    private void updateCachedUserData(String name, String firmName) {
        try {
            SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String existingJson = prefs.getString("Json_key", "");

            if (!existingJson.isEmpty()) {
                JSONObject userJson = new JSONObject(existingJson);
                userJson.put("name", name);
                userJson.put("firm_name", firmName);
                userJson.put("contact_person", name);

                prefs.edit().putString("Json_key", userJson.toString()).apply();

                Log.d("ProfileUpdate", "Cached data updated - Name: " + name + ", Firm: " + firmName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateProfile() {
        try {
            JSONObject payload = buildProfilePatchPayload();

            boolean fp = isFirmProfile();

            // ── Immediately write the new name into Constants BEFORE the API
            //    call so the header and person_icon update without waiting for
            //    the round-trip response ─────────────────────────────────────
            if (et_contactName != null && et_contactName.getText() != null) {
                String newName = et_contactName.getText().toString().trim();
                if (!TextUtils.isEmpty(newName)) {
                    Constants.NAME = newName;
                    Constants.ContactName = newName;
                }
            }

            if (!fp) {
                // My Profile: firm name comes from et_Firmname
                if (et_Firmname != null && et_Firmname.getText() != null) {
                    String newFirmName = et_Firmname.getText().toString().trim();
                    if (!TextUtils.isEmpty(newFirmName)) {
                        Constants.FIRM_NAME = newFirmName;
                    }
                }
            } else {
                // Firm Profile: firm name also from et_Firmname
                if (et_Firmname != null && et_Firmname.getText() != null) {
                    String newFirmName = et_Firmname.getText().toString().trim();
                    if (!TextUtils.isEmpty(newFirmName)) {
                        Constants.FIRM_NAME = newFirmName;
                    }
                }
            }

            // ── Immediately update person_icon in this fragment's own view ──
            if (person_icon != null) {
                String initial;
                if (!TextUtils.isEmpty(Constants.NAME)) {
                    initial = Constants.NAME.substring(0, 1).toUpperCase();
                } else if (!TextUtils.isEmpty(Constants.FIRM_NAME)) {
                    initial = Constants.FIRM_NAME.substring(0, 1).toUpperCase();
                } else {
                    initial = "?";
                }
                person_icon.setText(initial);
            }

            // ── Persist updated name to SharedPreferences so it survives
            //    navigation and process death ─────────────────────────────────
            updateCachedUserData(Constants.NAME, Constants.FIRM_NAME);

            // ── Push updated initial to the MainActivity header immediately ──
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).updateProfileInitial();
            }

            WebServiceHelper.callHttpWebService(
                    this,
                    getContext(),
                    WebServiceHelper.RestMethodType.PATCH,
                    "v3/profile",
                    "Update_Bp",
                    payload.toString()
            );
            Log.d("Update_Bp", payload.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void update_add_Profile() {
        try {
            JSONObject payload = buildadditional();
            WebServiceHelper.callHttpWebService(
                    this,
                    getContext(),
                    WebServiceHelper.RestMethodType.PATCH,
                    "v3/profile",
                    "Update_Bp",
                    payload.toString()
            );
            Log.d("Update_Bp", payload.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void SaveDetailsBp() {
        try {
            boolean isFirmNameEmpty =
                    Objects.requireNonNull(et_Firmname.getText()).toString().trim().isEmpty();
            boolean isCountryEmpty =
                    Objects.requireNonNull(et_Country.getText()).toString().trim().isEmpty();
            boolean isContactPhoneEmpty =
                    Objects.requireNonNull(et_ContactPhone.getText()).toString().trim().isEmpty();
            boolean isDefaultCurrencyEmpty =
                    Objects.requireNonNull(et_DefaultCurrency.getText()).toString().trim().isEmpty();
            boolean isEmailEmpty =
                    Objects.requireNonNull(et_Email.getText()).toString().trim().isEmpty();
            boolean isConsultationFeeEmpty =
                    et_confees.getText().toString().trim()
                            .replace("₹", "")
                            .trim()
                            .isEmpty();
            boolean isAdCountryEmpty =
                    Objects.requireNonNull(et_ad_Country.getText()).toString().trim().isEmpty();

            // =====================================================================
            // ZIP VALIDATION ON SAVE — exactly 6 digits
            // =====================================================================
            String zipValue = et_Zip.getText() != null ? et_Zip.getText().toString().trim() : "";
            boolean isZipEmpty = zipValue.isEmpty();
            boolean isZipInvalid = !isZipEmpty && zipValue.length() < 5;

            String mZipValue = met_Zip.getText() != null ? met_Zip.getText().toString().trim() : "";
            boolean isMZipEmpty = mZipValue.isEmpty();
            boolean isMZipInvalid = !isMZipEmpty && mZipValue.length() < 5;
            // =====================================================================

            boolean isSolo = "solo".equals(Constants.CATEGORY);
            boolean isFirmProfileMode = isFirmProfile();

            if ((isSolo && isFirmNameEmpty)
                    || isContactPhoneEmpty
                    || isDefaultCurrencyEmpty
                    || isEmailEmpty
                    || emailhaserror
                    || phonehaserror) {

                String msg = "Please enter the";

                if (isSolo && isFirmNameEmpty) {
                    msg = append(msg, "First Name");
                }
                if (isContactPhoneEmpty) {
                    msg = append(msg, "Phone Number");
                } else if (phonehaserror) {
                    msg = append(msg, "valid Phone Number");
                }
                if (isDefaultCurrencyEmpty) {
                    msg = append(msg, "Default Currency");
                }
                if (isEmailEmpty) {
                    msg = append(msg, "Email");
                } else if (emailhaserror) {
                    msg = append(msg, "valid Email");
                }

                // Consultation fee validation only for My Profile
                if (!isFirmProfileMode && isConsultationFeeEmpty) {
                    msg = append(msg, "Consultation Fee");
                }

                AndroidUtils.showAlert(msg, getActivity());
                return;
            }

            updateProfile();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String append(String msg, String value) {
        if (msg.equals("Please enter the")) {
            return msg + " " + value;
        } else {
            return msg + ", " + value;
        }
    }

    private boolean isEmpty(EditText et) {
        return et.getText() == null || et.getText().toString().trim().isEmpty();
    }

    private void appendMsg(StringBuilder msg, String field) {
        if (msg.length() > "Please enter the ".length()) {
            msg.append(", ");
        }
        msg.append(field);
    }

    private boolean validateRegisteredAddress(StringBuilder msg) {
        boolean hasError = false;

        if (isEmpty(et_Building)) {
            appendMsg(msg, "Building");
            hasError = true;
        }
        if (isEmpty(et_City)) {
            appendMsg(msg, "City");
            hasError = true;
        }
        if (isEmpty(et_State)) {
            appendMsg(msg, "State");
            hasError = true;
        }

        // =====================================================================
        // ZIP VALIDATION — exactly 6 digits
        // =====================================================================
        if (isEmpty(et_Zip)) {
            appendMsg(msg, "ZIP Code");
            hasError = true;
        } else if (et_Zip.getText().toString().trim().length() < 6) {
            appendMsg(msg, "valid 6-digit ZIP Code");
            ziphaserror = true;
            hasError = true;
        }
        // =====================================================================

        return hasError;
    }

    private boolean validateMailingAddress(StringBuilder msg) {
        boolean hasError = false;

        if (isEmpty(met_Building)) {
            appendMsg(msg, "Mailing Building");
            hasError = true;
        }
        if (isEmpty(met_City)) {
            appendMsg(msg, "Mailing City");
            hasError = true;
        }
        if (isEmpty(met_State)) {
            appendMsg(msg, "Mailing State");
            hasError = true;
        }

        if (isEmpty(met_Zip)) {
            appendMsg(msg, "Mailing ZIP Code");
            hasError = true;
        } else if (met_Zip.getText().toString().trim().length() < 6) {
            appendMsg(msg, "valid 6-digit Mailing ZIP Code");
            mziphaserror = true;
            hasError = true;
        }

        return hasError;
    }

    private void SaveDetailsAdditionalinfo() {
        try {
            createPendingChips();
            int pos = tabLayout != null ? tabLayout.getSelectedTabPosition() : 0;
            String[] tabs = {"practice_details", "courts_cases", "education_awards", "availability"};
            Constants.PROFILE_EDIT_TAB = (pos >= 0 && pos < tabs.length) ? tabs[pos] : "practice_details";

            // ── Practice Area mandatory check: My Profile only ────────────────
            boolean isFirmProfileMode = isFirmProfile();
            if (!isFirmProfileMode) {
                JSONArray practiceValues = chipPractice != null
                        ? getChipValuesFromGroup(chipPractice) : new JSONArray();
                if (practiceValues.length() == 0) {
                    AndroidUtils.showAlert("Please check the Practice Area.", getActivity());
                    return;
                }
            }

            // ── Court Enrollment Validation ───────────────────────────────────
            if (llCourtEnrollments != null && llCourtEnrollments.getChildCount() > 0) {
                if (validateCourtEnrollments()) {
                    AndroidUtils.showAlert("Please check the court details.", getActivity());
                    return;
                }
            }

            // ── Address Validation ────────────────────────────────────────────
            StringBuilder msg = new StringBuilder("Please enter the ");
            boolean hasError = false;
            hasError |= validateRegisteredAddress(msg);
            hasError |= validateMailingAddress(msg);
            if (hasError) {
                AndroidUtils.showAlert(msg.toString(), getActivity());
                return;
            }

            update_add_Profile();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateCourtEnrollments() {
        boolean hasError = false;
        if (llCourtEnrollments == null) return false;

        for (int i = 0; i < llCourtEnrollments.getChildCount(); i++) {
            View row = llCourtEnrollments.getChildAt(i);

            setCourtError(row, R.id.tvCourtTypeError, null);
            setCourtError(row, R.id.tvStateNameError, null);
            setCourtError(row, R.id.tvCourtCityError, null);
            setCourtError(row, R.id.tvCourtNameError, null);

            // Court Type validation
            LinearLayout llCt = row.findViewById(R.id.ll_court_type);
            TextView tvCt = llCt != null ? llCt.findViewById(R.id.tv_spinner_view) : null;
            String courtType = tvCt != null ? tvCt.getText().toString().trim() : "";
            if (TextUtils.isEmpty(courtType)) {
                setCourtError(row, R.id.tvCourtTypeError, "Required");
                hasError = true;
                continue;
            }

            boolean isSupreme = courtType.equalsIgnoreCase("Supreme Court");

            // State validation (not required for Supreme Court)
            LinearLayout llStateSpin = row.findViewById(R.id.ll_state_spinner);
            TextView tvState = llStateSpin != null ? llStateSpin.findViewById(R.id.tv_spinner_view) : null;
            String stateVal = tvState != null ? tvState.getText().toString().trim() : "";
            if (!isSupreme && TextUtils.isEmpty(stateVal)) {
                setCourtError(row, R.id.tvStateNameError, "Required");
                hasError = true;
            }

            // City validation - Get from TextInputEditText
            TextInputEditText etCitySearch = row.findViewById(R.id.et_city_search);
            String cityVal = etCitySearch != null ? etCitySearch.getText().toString().trim() : "";

            if (!isSupreme && TextUtils.isEmpty(cityVal)) {
                setCourtError(row, R.id.tvCourtCityError, "Required");
                hasError = true;
            }
        }
        return hasError;
    }

    private void setCourtError(View row, int errorViewId, String message) {
        TextView errorTv = row.findViewById(errorViewId);
        if (errorTv == null) return;
        if (TextUtils.isEmpty(message)) {
            errorTv.setVisibility(View.GONE);
            errorTv.setText("");
        } else {
            errorTv.setText(message);
            errorTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.Red));
            errorTv.setVisibility(View.VISIBLE);
        }
    }

    private void requestFocusOnField(View view) {
        if (view == null || !isAdded()) return;

        if (view instanceof TextInputEditText || view instanceof EditText) {
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager)
                            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        } else if (view instanceof AppCompatButton) {
            // e.g. et_dob — tap it programmatically instead of showing keyboard
            view.performClick();
        } else {
            // Non-editable target (spinner row, chip group label, etc.)
            // Just make it focusable enough to visually indicate selection
            view.setFocusableInTouchMode(true);
            view.requestFocus();
        }
    }

    private void highlightDropdownError(LinearLayout layout) {
        if (layout == null) return;
        layout.setBackgroundResource(R.drawable.border_grey_bg); // use your error drawable

        // Auto-clear error when user clicks dropdown
        layout.setOnClickListener(v -> {
            layout.setBackgroundResource(R.drawable.rectangular_white_background);
            // Re-trigger original click (toggle dropdown)
            // The original click listener will be re-set when the row rebuilds
        });
    }

    private boolean validateAddress(
            StringBuilder msg,
            String prefix,
            EditText country,
            EditText building,
            EditText street,
            EditText city,
            EditText state,
            EditText zip,
            boolean zipHasError
    ) {
        boolean hasError = false;

        if (isEmpty(building)) {
            appendMsg(msg, prefix + "Building");
            hasError = true;
        }
        if (isEmpty(street)) {
            appendMsg(msg, prefix + "Street");
            hasError = true;
        }
        if (isEmpty(city)) {
            appendMsg(msg, prefix + "City");
            hasError = true;
        }
        if (isEmpty(state)) {
            appendMsg(msg, prefix + "State");
            hasError = true;
        }
        if (isEmpty(zip)) {
            appendMsg(msg, prefix + "ZIP Code");
            hasError = true;
        } else if (zip.getText().toString().trim().length() != 6) {
            appendMsg(msg, "valid 6-digit " + prefix + "ZIP Code");
            hasError = true;
        } else if (zipHasError) {
            appendMsg(msg, "valid 6-digit " + prefix + "ZIP Code");
            hasError = true;
        }
        return hasError;
    }

    private void profile() {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONObject jsonObject = new JSONObject();
        WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "/v3/profile/pic", "Profile", jsonObject.toString());
    }

    private void setData() {
        boolean fp = isFirmProfile();
//        boolean isGHorTM = Constants.ROLE.equalsIgnoreCase("GH") ||
//                Constants.ROLE.equalsIgnoreCase("TM");
//
//        // ── Header title ─────────────────────────────────────────────
//        tv_BasicProfile.setTextSize(DynamicUtils.twenty);
//        tv_BasicProfile.setText(fp ? "Firm Information" : getString(R.string.profile_inform));
//
//        // ── Profile image: logo API for Firm Profile, pic API for My Profile ──
//        // (called after view is ready — see loadProfileImage() call in LoadData)
//
//        // ── Firm name row ─────────────────────────────────────────────
//        if ("solo".equalsIgnoreCase(Constants.CATEGORY) || isGHorTM) {
//            firmlayout.setVisibility(View.GONE);
//            tv_contactName.setText(R.string.name);
//        } else {
//            firmlayout.setVisibility(fp ? VISIBLE : VISIBLE);
//            tv_contactName.setText(fp ? getString(R.string._contact_name)
//                    : getString(R.string._contact_name));
//        }
//..
        tv_BasicProfile.setText(R.string.profile_inform);
        tv_BasicProfile.setTextSize(DynamicUtils.twenty);

        boolean isAMMorSuperuser = Constants.ROLE.equalsIgnoreCase("AAM");
        boolean isGHTeamMember = Constants.ROLE.equalsIgnoreCase("GH") ||
                Constants.ROLE.equalsIgnoreCase("TM");

//        // Title: "Firm Profile" for AMM/Superuser, else "Profile Information"
//        .  if (isAMMorSuperuser) {
//            tv_BasicProfile.setText("Firm Profile");
//        } else {
//            tv_BasicProfile.setText(R.string.profile_inform);
//        }
        if (fp) {
            tv_BasicProfile.setText("Firm Information");
        } else if (isAMMorSuperuser) {
            tv_BasicProfile.setText("Firm Profile");
        } else {
            tv_BasicProfile.setText(R.string.profile_inform);
        }
        tv_BasicProfile.setTextSize(20);

        tv_Firmname.setText(R.string.firm_name);
        tv_Country.setText(R.string.country);
        tv_Email.setText(R.string.email);

        if ("solo".equalsIgnoreCase(Constants.CATEGORY) || isGHTeamMember) {
            tv_contactName.setText(R.string.name);
            firmlayout.setVisibility(View.GONE);
        } else {
            firmlayout.setVisibility(VISIBLE);
            tv_contactName.setText(R.string._contact_name);
        }

        tv_ContactPhone.setText(R.string.phone_no);
        tv_Website.setText(R.string._website);
        tv_nationality.setText(R.string.nationality);
        tv_dob.setText(R.string.dob);
        et_dob.setHint(R.string.dob);
        et_dob.setOnClickListener(v -> showDatePicker(et_dob, true, true, false, null));
        //..
        tl_generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GenerateBio();
            }
        });
        // ── Consultation fee: hidden for Firm Profile ─────────────────
//        View conFeeRow = requireView().findViewById(R.id.ll_confee_row);
//        if (conFeeRow != null) {
//            conFeeRow.setVisibility(fp ? View.GONE : VISIBLE);
//        }

        // ── Bio label ─────────────────────────────────────────────────
        tv_bio.setText(fp ? "About the Firm" : getString(R.string.bio));

        // ── Address labels (unchanged) ────────────────────────────────
        tv_RegisterAddress.setText(R.string.register_address);
        tv_RegisterAddress.setTextSize(DynamicUtils.twenty);
        tv_Building.setText(R.string.building_no);
        tv_City.setText(R.string.city);
        tv_Street.setText(R.string.street);
        tv_State.setText(R.string.state);
        tv_ad_Country.setText(R.string.country);
        tv_Zip.setText(R.string.zip);
        tv_mailingAddressnNote.setText(R.string.mailing_address_is_same_as_registered_address);
        setMailingEditable(true);
        tv_MailingAddress.setText(R.string.mailing_address);
        tv_MailingAddress.setTextSize(DynamicUtils.twenty);
    }

    private void disableAddressFieldsForGH() {
        // ── Basic Profile tab: registered address ────────────────────────
        if (et_Building != null) setEditTextState(et_Building, false);
        if (et_Street != null) setEditTextState(et_Street, false);
        if (et_City != null) setEditTextState(et_City, false);
        if (et_State != null) setEditTextState(et_State, false);
        if (et_Zip != null) setEditTextState(et_Zip, false);
        if (ll_et_ad_country != null) {
            setLayoutState(ll_et_ad_country, false);
            if (img_dropdown_icon_adCountry != null)
                img_dropdown_icon_adCountry.setVisibility(View.GONE);
            if (img_clear_icon_adCountry != null) img_clear_icon_adCountry.setVisibility(View.GONE);
        }
        if (et_ad_Country != null) setTextViewState(et_ad_Country, false);

        // ── Basic Profile tab: mailing address ──────────────────────────
        if (met_Building != null) setEditTextState(met_Building, false);
        if (met_Street != null) setEditTextState(met_Street, false);
        if (met_City != null) setEditTextState(met_City, false);
        if (met_State != null) setEditTextState(met_State, false);
        if (met_Zip != null) setEditTextState(met_Zip, false);
        if (ll_met_ad_country != null) {
            setLayoutState(ll_met_ad_country, false);
            if (img_dropdown_icon_madCountry != null)
                img_dropdown_icon_madCountry.setVisibility(View.GONE);
            if (img_clear_icon_madCountry != null)
                img_clear_icon_madCountry.setVisibility(View.GONE);
        }
        if (met_ad_Country != null) setTextViewState(met_ad_Country, false);

        // ── Hide same-address checkbox ───────────────────────────────────
        if (cbYes != null) cbYes.setVisibility(View.GONE);

        // ── Firm name not editable ───────────────────────────────────────
        if (et_Firmname != null) {
            et_Firmname.setEnabled(false);
            et_Firmname.setFocusable(false);
            et_Firmname.setFocusableInTouchMode(false);
            et_Firmname.setClickable(false);
            et_Firmname.setAlpha(0.6f);
        }
    }

    private void setMailingEditable(boolean editable) {
        setEditTextState(met_Building, editable);
        setEditTextState(met_Street, editable);
        setEditTextState(met_City, editable);
        setEditTextState(met_State, editable);
        setLayoutState(ll_met_ad_country, editable);
        setTextViewState(met_ad_Country, editable);
        setEditTextState(met_Zip, editable);
    }

    private void setLayoutState(View layout, boolean editable) {
        layout.setEnabled(editable);
        layout.setClickable(editable);
        layout.setFocusable(editable);
        layout.setAlpha(editable ? 1f : 0.6f);
    }

    private void setEditTextState(EditText editText, boolean editable) {
        editText.setEnabled(editable);
        editText.setFocusable(editable);
        editText.setFocusableInTouchMode(editable);
        editText.setClickable(editable);
        editText.setLongClickable(editable);
        if (!editable) {
            editText.clearFocus();
        }
        editText.setAlpha(editable ? 1f : 0.6f);
    }

    private void setTextViewState(TextView textView, boolean editable) {
        textView.setEnabled(editable);
        textView.setClickable(editable);
        textView.setFocusable(editable);
        textView.setFocusableInTouchMode(editable);
        textView.setAlpha(editable ? 1f : 0.6f);
    }

    private void LoadData() {
        boolean fp = isFirmProfile();
        FirmProfileModel.Profile profile = firmProfileModel.getData().getProfile();
        FirmProfileModel.Firm firm = profile.getFirm();

        // ── Profile image: logo vs pic ───────────────────────────────
        String imageUrl = "";
        if (fp) {
            imageUrl = profile.getProfile_pic_url();
        } else {
            imageUrl = Constants.firm_image;
        }
        AndroidUtils.loadProfileImage(requireContext(), imageUrl, iv_profile, person_icon);

        // ── Firm name (Bp: firm.getFullname, My: same) ───────────────
        if (et_Firmname != null) {
            String val = firm.getFullname();
            et_Firmname.setText(val);
            originalValues.put("fullname", val);
        }

        // ── Contact name / Name ──────────────────────────────────────
        if (et_contactName != null) {
            String val = fp ? firm.getContact_person() : profile.getName();
            et_contactName.setText(val);
            originalValues.put("contact_person", val);
        }

        // ── Email ────────────────────────────────────────────────────
        if (et_Email != null) {
            String val = fp ? firm.getEmail() : profile.getEmail();
            et_Email.setText(val);
            originalValues.put("email", val);
        }

        // Fix:
        if (et_ContactPhone != null) {
            String val = fp ? firm.getContact_phone() : profile.getMobile();
            if (val == null) val = "";
            et_ContactPhone.setText(val);
            originalValues.put(fp ? "contact_phone" : "mobile", val);
        }

        // ── Website ──────────────────────────────────────────────────
        if (et_Website != null) {
            String val = firm.getWebsite();
            et_Website.setText(val);
            originalValues.put("website", val);
            boolean isGHorTM = Constants.ROLE.equalsIgnoreCase("GH") ||
                    Constants.ROLE.equalsIgnoreCase("TM");
            if (isGHorTM) setEditTextState(et_Website, false);
        }

        // Fix:
        if (et_bio != null) {
            String val = fp
                    ? (firm.getFirm_description() != null ? firm.getFirm_description() : "")
                    : profile.getBio_description();
            et_bio.setText(val);
            originalValues.put(fp ? "firm_description" : "bio_description", val);
        }

        // ── Consultation fee: only for My Profile ────────────────────
        if (et_confees != null) {
            if (fp) {
                et_confees.setText("");
            } else {
                String fee = profile.getConsultation_fee() != null
                        ? profile.getConsultation_fee().getAmount() : "";
                et_confees.setText(!TextUtils.isEmpty(fee) ? fee : "");
                originalValues.put("consultation_fee", fee != null ? fee : "");
            }
        }

        // ── DOB ──────────────────────────────────────────────────────
        if (et_dob != null) {
            String value = AndroidUtils.formatToMMMddYYYY(profile.getDate_of_birth());
            et_dob.setText(value);
            originalValues.put("date_of_birth", profile.getDate_of_birth());
        }

        // ── Gender ───────────────────────────────────────────────────
        if (met_ad_gender != null) {
            String value = AndroidUtils.CapitalizeFirstLetter(profile.getGender());
            met_ad_gender.setText(value);
            originalValues.put("gender", profile.getGender());
        }

        // ── Default Currency ─────────────────────────────────────────
        if (et_DefaultCurrency != null) {
            String value = firm.getBilling_currency();
            et_DefaultCurrency.setText(value);
            originalValues.put("billing_currency", value);
        }

        // ── Registered Address fields ─────────────────────────────────
        if (firm.getAddress() != null) {
            FirmProfileModel.Address address = firm.getAddress();
            if (et_Building != null) {
                String value = address.getHouse_flat_no();
                et_Building.setText(value);
                originalValues.put("address_house_flat_no", value);
            }
            if (et_Street != null) {
                String value = address.getStreet();
                et_Street.setText(value);
                originalValues.put("address_street", value);
            }
            if (et_City != null) {
                String value = address.getCity_town();
                et_City.setText(value);
                originalValues.put("address_city_town", value);
            }
            if (et_State != null) {
                String value = address.getState();
                et_State.setText(value);
                originalValues.put("address_state", value);
            }
            if (et_ad_Country != null) {
                String value = address.getCountry();
                et_ad_Country.setText(value);
                originalValues.put("address_country", value);
            }
            if (et_Zip != null) {
                String value = address.getZipcode();
                et_Zip.setText(value);
                originalValues.put("address_zipcode", value);
            }
        }

        // ── Correspondence Address fields ─────────────────────────────
        if (firm.getCorrespondence_address() != null) {
            FirmProfileModel.Address mAddress = firm.getCorrespondence_address();
            if (met_Building != null) {
                String value = mAddress.getHouse_flat_no();
                met_Building.setText(value);
                originalValues.put("correspondence_house_flat_no", value);
            }
            if (met_Street != null) {
                String value = mAddress.getStreet();
                met_Street.setText(value);
                originalValues.put("correspondence_street", value);
            }
            if (met_City != null) {
                String value = mAddress.getCity_town();
                met_City.setText(value);
                originalValues.put("correspondence_city_town", value);
            }
            if (met_State != null) {
                String value = mAddress.getState();
                met_State.setText(value);
                originalValues.put("correspondence_state", value);
            }
            if (met_ad_Country != null) {
                String value = mAddress.getCountry();
                met_ad_Country.setText(value);
                originalValues.put("correspondence_country", value);
            }
            if (met_Zip != null) {
                String value = mAddress.getZipcode();
                met_Zip.setText(value);
                originalValues.put("correspondence_zipcode", value);
            }
        }
// ── Store original cases_handled ─────────────────────────────────────
        if (profile.getCases_handled() != null) {
            originalCasesHandled = new JSONArray();
            for (String c : profile.getCases_handled()) {
                originalCasesHandled.put(c);
            }
        } else {
            originalCasesHandled = new JSONArray();
        }
        // ── Store Additional Info arrays ──────────────────────────────
        // Practice areas: Firm Profile → firm.practice_areas, My Profile → profile.practice_areas
        originalPracticeAreas = fp ? firm.getPractice_areas() : profile.getPractice_areas();

        // Bar Council ID: Firm Profile → firm.reg_id, My Profile → profile.bar_council_id
        originalValues.put("bar_council_id",
                fp
                        ? (firm.getReg_id() != null ? firm.getReg_id() : "")
                        : (profile.getBar_council_id() != null ? profile.getBar_council_id() : "")
        );

        // Years: Firm Profile → firm.years_of_incorporation, My Profile → profile.years_of_experience
        originalValues.put("years_of_experience",
                fp
                        ? String.valueOf(firm.getYears_of_incorporation())
                        : String.valueOf(profile.getYears_of_experience())
        );

        originalLanguages = profile.getLanguages_spoken();
        originalServices = fp ? firm.getServices_offered() : profile.getServices_offered();
        originalEducation = getEducationJsonFromModel(profile.getEducation());
        originalCertifications = getCertificationJsonFromModel(profile.getCertifications());
        originalAwards = fp
                ? getAwardsJsonFromModel(firm.getAwards())
                : getAwardsJsonFromModel(profile.getAwards());

        if (profile.getAvailability() != null
                && profile.getAvailability().getWeekly_schedule() != null) {
            originalWeeklySchedule =
                    new ArrayList<>(profile.getAvailability().getWeekly_schedule());
        }
    }

    private JSONArray getEducationJsonFromModel(List<FirmProfileModel.Education> list) {
        JSONArray array = new JSONArray();
        if (list == null) return array;
        for (FirmProfileModel.Education edu : list) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("degree", edu.getDegree());
                obj.put("university", edu.getUniversity());
                obj.put("passing_year", edu.getPassing_year());
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array;
    }

    private JSONArray getCertificationJsonFromModel(List<FirmProfileModel.Certification> list) {
        JSONArray array = new JSONArray();
        if (list == null) return array;
        for (FirmProfileModel.Certification cert : list) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("certification_name", cert.getCertification_name());
                obj.put("issuing_authority", cert.getIssuing_authority());
                obj.put("year_of_issue", cert.getYear_of_issue());
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array;
    }

    private JSONArray getAwardsJsonFromModel(List<FirmProfileModel.Award> list) {
        JSONArray array = new JSONArray();
        if (list == null) return array;
        for (FirmProfileModel.Award award : list) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("award_name", award.getAward_name());
                obj.put("year_of_award", award.getYear_of_award());
                obj.put("purpose", award.getPurpose());
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array;
    }

    private View createEducationsAwardsEdit() {
        llViewScreen.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View detailView = inflater.inflate(R.layout.educational_edit_view, llViewScreen, false);

        llEducation = detailView.findViewById(R.id.ll_education);
        llCertification = detailView.findViewById(R.id.ll_certification);
        llAwards = detailView.findViewById(R.id.ll_awards);

        TextView tvEduTitle = detailView.findViewById(R.id.tv_title_education);
        TextView tvCertTitle = detailView.findViewById(R.id.tv_title_certification);
        TextView tvAwardsTitle = detailView.findViewById(R.id.tv_title_awards);

        TextView tv_add_education = detailView.findViewById(R.id.tv_add_education);
        TextView tv_add_certification = detailView.findViewById(R.id.tv_add_certification);
        TextView tv_add_awards = detailView.findViewById(R.id.tv_add_awards);

        tv_add_education.setText(R.string._add_more);
        tv_add_certification.setText(R.string._add_more);
        tv_add_awards.setText(R.string._add_more);

        tv_add_education.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem(inflater, llEducation, tvEduTitle, "", "", "", TYPE_EDUCATION);
            }
        });
        tv_add_certification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem(inflater, llCertification, tvCertTitle, "", "", "", TYPE_CERTIFICATION);
            }
        });
        tv_add_awards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem(inflater, llAwards, tvAwardsTitle, "", "", "", TYPE_AWARD);
            }
        });

        tvEduTitle.setText(R.string.educational_qualifications);
        tvCertTitle.setText(R.string.certifications);
        tvAwardsTitle.setText(R.string.awards_recoginations);

        FirmProfileModel.Profile profile =
                firmProfileModel != null && firmProfileModel.getData() != null ?
                        firmProfileModel.getData().getProfile() : null;

        if (profile == null) {
            llViewScreen.setVisibility(View.GONE);
            return detailView;
        }

        loadItems(inflater, llEducation, tvEduTitle, profile.getEducation(), 1);
        loadItems(inflater, llCertification, tvCertTitle, profile.getCertifications(), 2);
        loadItems(inflater, llAwards, tvAwardsTitle, profile.getAwards(), 3);

        llViewScreen.addView(detailView);
        llViewScreen.setVisibility(VISIBLE);
        scrollToTargetField();
        return detailView;
    }


    private JSONArray buildWeeklySchedule() throws Exception {
        JSONArray weekly = new JSONArray();
        View settingsView = llViewScreen.getChildAt(0);
        if (settingsView == null) return weekly;
        LinearLayout llDaysContainer = settingsView.findViewById(R.id.ll_days_container);
        if (llDaysContainer == null) return weekly;

        for (int i = 0; i < llDaysContainer.getChildCount(); i++) {
            View dayView = llDaysContainer.getChildAt(i);
            CheckBox cb = dayView.findViewById(R.id.cbDay);
            TextView from = dayView.findViewById(R.id.tvFromTime);
            TextView to = dayView.findViewById(R.id.tvToTime);
            FlexboxLayout selectedSlots = dayView.findViewById(R.id.layoutSelectedSlots);
            String dayName = (String) dayView.getTag();
            Object dateTag = dayView.getTag(R.id.tvDay);
            String apiDate = (dateTag != null) ? dateTag.toString() : "";

            JSONObject day = new JSONObject();
            day.put("day_name", dayName != null ? dayName : "");
            day.put("date", apiDate);
            day.put("is_working_day", cb.isChecked());
            Log.d("Avaiability_Day", day.toString());

            if (cb.isChecked()) {
                JSONArray workSlots = new JSONArray();
                try {
                    JSONObject work = new JSONObject();
                    work.put("start_time", to24Hour(from.getText().toString()));
                    work.put("end_time", to24Hour(to.getText().toString()));
                    workSlots.put(work);
                } catch (Exception e) {
                    continue;
                }
                day.put("work_slots", workSlots);
                day.put("expert_slots", getExpertSlotsFromLayout(selectedSlots));
            } else {
                day.put("work_slots", new JSONArray());
                day.put("expert_slots", new JSONArray());
            }
            weekly.put(day);
        }
        return weekly;
    }

    private View createSetAvailability() {
        llViewScreen.removeAllViews();
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.activity_book_setting_edit, llViewScreen, false);
        llViewScreen.addView(view);
        llViewScreen.setVisibility(VISIBLE);

        view.findViewById(R.id.set_clear).setVisibility(GONE);
        view.findViewById(R.id.slot_min).setVisibility(GONE);

        view.findViewById(R.id.iv_info1).setOnClickListener(v1 -> {
            String msg = "How it works: Set your available hours. We will automatically create " +
                    "30-minute booking slots. Need a break? Click Exclude icon to block specific " +
                    "times (e.g., 12:00-01.00 PM).";

            TextView popupText = new TextView(requireContext());
            popupText.setText(msg);
            popupText.setPadding(30, 20, 30, 20);
            popupText.setTextColor(ResourcesCompat.getColor(getContext().getResources(), R.color.blue, null));
            popupText.setBackgroundResource(R.drawable.info_box_bg);
            popupText.setTypeface(ResourcesCompat.getFont(getContext(), R.font.gill_sans));
            popupText.setTextSize(13f);

            PopupWindow popup = new PopupWindow(popupText,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    true);

            popup.setElevation(10f);
            popup.showAsDropDown(v1, 0, 10);
        });

        LinearLayout llDaysContainer = view.findViewById(R.id.ll_days_container);
        llDaysContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        List<FirmProfileModel.WeeklySchedule> apiSchedule = getApiWeeklySchedule();
        List<View> dayViews = new ArrayList<>();

        for (FirmProfileModel.WeeklySchedule schedule : apiSchedule) {
            String dayName = schedule.getDay_name() != null ? schedule.getDay_name() : "";
            String dateLabel = schedule.getDate_label() != null ? schedule.getDate_label() : "";

            View dayView = inflater.inflate(R.layout.item_week_day, llDaysContainer, false);

            TextView tvDay = dayView.findViewById(R.id.tvDay);
            tvDay.setText(!TextUtils.isEmpty(dateLabel) ? dateLabel : dayName);

            dayView.setTag(dayName);
            dayView.setTag(R.id.tvDay, schedule.getDate());

            setupDayRow(dayView, dayName, schedule);

            llDaysContainer.addView(dayView);
            dayViews.add(dayView);
        }

        Set<String> weekdays = new HashSet<>(
                Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        );

        view.findViewById(R.id.tv_set_weekdays).setOnClickListener(v -> {
            for (View dayView : dayViews) {
                Object tagObj = dayView.getTag();
                if (!(tagObj instanceof String)) continue;
                String tag = (String) tagObj;

                CheckBox cbDay = dayView.findViewById(R.id.cbDay);
                if (cbDay != null) {
                    cbDay.setChecked(weekdays.contains(tag));
                }
            }
        });

        view.findViewById(R.id.tv_clear_all).setOnClickListener(v -> {
            for (View dayView : dayViews) {
                CheckBox cbDay = dayView.findViewById(R.id.cbDay);
                if (cbDay != null) {
                    cbDay.setChecked(false);
                }
            }
        });

        return view;
    }

    private List<FirmProfileModel.WeeklySchedule> getApiWeeklySchedule() {
        if (firmProfileModel == null
                || firmProfileModel.getData() == null
                || firmProfileModel.getData().getProfile() == null
                || firmProfileModel.getData().getProfile().getAvailability() == null
                || firmProfileModel.getData().getProfile().getAvailability().getWeekly_schedule() == null) {
            return new ArrayList<>();
        }
        return firmProfileModel.getData().getProfile().getAvailability().getWeekly_schedule();
    }

    private void setupDayRow(View dayView, String dayName, FirmProfileModel.WeeklySchedule savedSchedule) {
        CheckBox cbDay = dayView.findViewById(R.id.cbDay);
        TextView tvFrom = dayView.findViewById(R.id.tvFromTime);
        TextView tvTo = dayView.findViewById(R.id.tvToTime);
        TextView tvavail = dayView.findViewById(R.id.noavail);
        tvavail.setText(R.string.not_available);
        TextView toText = dayView.findViewById(R.id.to_text);
        ImageView ivSlot = dayView.findViewById(R.id.slot);
        LinearLayout exceptionContainer = dayView.findViewById(R.id.exceptionContainer);
        FlexboxLayout llSelectedSlots = dayView.findViewById(R.id.layoutSelectedSlots);
        TextView tvOutTime = dayView.findViewById(R.id.tvOutTime);
        LinearLayout timinglayout = dayView.findViewById(R.id.timinglayout);
        timinglayout.setVisibility(View.GONE);
        TextView tvAvailTime = dayView.findViewById(R.id.tvAvailTime);
        List<String> selectedSlots = new ArrayList<>();

        cbDay.setChecked(false);
        tvFrom.setVisibility(View.GONE);
        tvTo.setVisibility(View.GONE);
        toText.setVisibility(View.GONE);
        ivSlot.setVisibility(View.GONE);
        exceptionContainer.setVisibility(View.GONE);
        llSelectedSlots.removeAllViews();
        tvOutTime.setText("Availability:");
        tvAvailTime.setText("Excluded:");

        if (savedSchedule != null) {
            cbDay.setChecked(savedSchedule.isIs_working_day());
            if (savedSchedule.getWork_slots() != null && !savedSchedule.getWork_slots().isEmpty()) {
                FirmProfileModel.WorkSlot w = savedSchedule.getWork_slots().get(0);
                tvFrom.setText(convertTo12HourFormat(w.getStart_time()));
                tvTo.setText(convertTo12HourFormat(w.getEnd_time()));
                ivSlot.setVisibility(VISIBLE);
                tvFrom.setVisibility(VISIBLE);
                tvavail.setVisibility(View.GONE);
                tvTo.setVisibility(VISIBLE);
                toText.setVisibility(VISIBLE);
            }
            if (savedSchedule.getExpert_slots() != null && !savedSchedule.getExpert_slots().isEmpty()) {
                exceptionContainer.setVisibility(VISIBLE);
                timinglayout.setVisibility(VISIBLE);
                llSelectedSlots.removeAllViews();
                selectedSlots.clear();
                for (FirmProfileModel.WorkSlot slot : savedSchedule.getExpert_slots()) {
                    String time =
                            convertTo12HourFormat(slot.getStart_time())
                                    + " - " +
                                    convertTo12HourFormat(slot.getEnd_time());
                    selectedSlots.add(time);
                    llSelectedSlots.addView(createSelectedSlotTextView(dayView.getContext(), time));
                }
                ivSlot.setVisibility(VISIBLE);
            }
            if (!tvFrom.getText().toString().isEmpty() && !tvTo.getText().toString().isEmpty()) {
                updateSummary(tvFrom.getText().toString(), tvTo.getText().toString(), selectedSlots, tvOutTime, tvAvailTime);
            }
        }

        cbDay.setOnCheckedChangeListener((btn, isChecked) -> {
            tvavail.setVisibility(isChecked ? View.GONE : VISIBLE);
            tvFrom.setVisibility(isChecked ? VISIBLE : View.GONE);
            tvTo.setVisibility(isChecked ? VISIBLE : View.GONE);
            toText.setVisibility(isChecked ? VISIBLE : View.GONE);
            if (!isChecked) {
                resetDayToDefault(tvFrom, tvTo, ivSlot, llSelectedSlots, selectedSlots, tvOutTime, tvAvailTime);
                ivSlot.setVisibility(View.GONE);
                ivSlot.setEnabled(false);
                ivSlot.setAlpha(0.3f);
                exceptionContainer.setVisibility(View.GONE);
                llSelectedSlots.removeAllViews();
                selectedSlots.clear();
                dayAvailability.remove(dayName);
                tvOutTime.setText("0h out");
                tvAvailTime.setText("0h avail");
            } else {
                updateSlotIconState(tvFrom, tvTo, ivSlot);
            }
        });

        tvFrom.setOnClickListener(v ->
                openTimePicker(tvFrom.getText().toString(), time -> {
                    tvFrom.setText(time);
                    if (!tvTo.getText().toString().isEmpty()) {
                        String fixedToTime = validateMinimumDuration(tvFrom.getText().toString(), tvTo.getText().toString());
                        tvTo.setText(fixedToTime);
                    }
                    // ── Remove excluded slots that fall outside the new time range ──
                    filterInvalidSlots(selectedSlots, tvFrom.getText().toString(), tvTo.getText().toString());
                    rebuildSelectedSlotsView(llSelectedSlots, selectedSlots, tvFrom.getContext(), exceptionContainer, timinglayout);
                    refreshDay(dayName, tvFrom, tvTo, ivSlot, llSelectedSlots, selectedSlots, tvOutTime, tvAvailTime);
                    updateSlotIconState(tvFrom, tvTo, ivSlot);
                })
        );

        tvTo.setOnClickListener(v ->
                openTimePicker(tvTo.getText().toString(), time -> {
                    String fixedToTime = validateMinimumDuration(tvFrom.getText().toString(), time);
                    tvTo.setText(fixedToTime);
                    // ── Remove excluded slots that fall outside the new time range ──
                    filterInvalidSlots(selectedSlots, tvFrom.getText().toString(), tvTo.getText().toString());
                    rebuildSelectedSlotsView(llSelectedSlots, selectedSlots, tvFrom.getContext(), exceptionContainer, timinglayout);
                    refreshDay(dayName, tvFrom, tvTo, ivSlot, llSelectedSlots, selectedSlots, tvOutTime, tvAvailTime);
                    updateSlotIconState(tvFrom, tvTo, ivSlot);
                })
        );

        ivSlot.setOnClickListener(v ->
                openSlotPopup(v.getContext(), tvFrom, tvTo, selectedSlots, llSelectedSlots, exceptionContainer, ivSlot, tvOutTime, tvAvailTime, timinglayout)
        );
    }

    private void filterInvalidSlots(List<String> selectedSlots, String fromTime, String toTime) {
        if (selectedSlots == null || selectedSlots.isEmpty()) return;
        if (TextUtils.isEmpty(fromTime) || TextUtils.isEmpty(toTime)) return;

        List<String> validSlots = generateSlots(fromTime, toTime);
        selectedSlots.retainAll(validSlots);
    }

    /**
     * Rebuilds the FlexboxLayout that shows the excluded slot chips,
     * and hides the timingLayout if no slots remain.
     */
    private void rebuildSelectedSlotsView(
            FlexboxLayout llSelectedSlots,
            List<String> selectedSlots,
            Context context,
            LinearLayout exceptionContainer,
            LinearLayout timingLayout
    ) {
        if (llSelectedSlots == null) return;
        llSelectedSlots.removeAllViews();

        if (selectedSlots == null || selectedSlots.isEmpty()) {
            if (timingLayout != null) timingLayout.setVisibility(View.GONE);
            return;
        }

        for (String slot : selectedSlots) {
            llSelectedSlots.addView(createSelectedSlotTextView(context, slot));
        }
        if (timingLayout != null) timingLayout.setVisibility(View.VISIBLE);
    }

    private int getTimeDifferenceInHours(String fromTime, String toTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            Date from = sdf.parse(fromTime);
            Date to = sdf.parse(toTime);
            if (from != null && to != null) {
                long diffMillis = to.getTime() - from.getTime();
                return (int) (diffMillis / (1000 * 60 * 60));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void updateSlotIconState(TextView tvFrom, TextView tvTo, ImageView ivSlot) {
        String from = tvFrom.getText().toString();
        String to = tvTo.getText().toString();
        if (!from.isEmpty() && !to.isEmpty()) {
            int diff = getTimeDifferenceInHours(from, to);
            ivSlot.setVisibility(VISIBLE);
            if (diff >= 4) {
                ivSlot.setEnabled(true);
                ivSlot.setClickable(true);
                ivSlot.setAlpha(1f);
            } else {
                ivSlot.setEnabled(false);
                ivSlot.setClickable(false);
                ivSlot.setAlpha(0.3f);
            }
        } else {
            ivSlot.setVisibility(View.GONE);
        }
    }

    private String addMinutes(String time, int minutes) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(time));
            cal.add(Calendar.MINUTE, minutes);
            return sdf.format(cal.getTime());
        } catch (Exception e) {
            return time;
        }
    }

    private void resetDayToDefault(
            TextView tvFrom,
            TextView tvTo,
            ImageView ivSlot,
            FlexboxLayout llSelectedSlots,
            List<String> selectedSlots,
            TextView tvOutTime,
            TextView tvAvailTime
    ) {
        tvFrom.setText("09:00 AM");
        tvTo.setText("05:00 PM");
        ivSlot.setVisibility(View.GONE);
        llSelectedSlots.removeAllViews();
        selectedSlots.clear();
        tvOutTime.setText("0h out");
        tvAvailTime.setText("8h 0m avail");
    }

    private String validateMinimumDuration(String fromTime, String toTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            Date from = sdf.parse(fromTime);
            Date to = sdf.parse(toTime);
            if (!to.after(from)) {
                return addMinutes(fromTime, 30);
            }
            long diffMinutes = (to.getTime() - from.getTime()) / (1000 * 60);
            if (diffMinutes < 30) {
                return addMinutes(fromTime, 30);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toTime;
    }

    private void openSlotPopup(
            Context context,
            TextView tvFrom,
            TextView tvTo,
            List<String> selectedSlots,
            FlexboxLayout llSelectedSlots,
            LinearLayout exceptionContainer,
            ImageView ivSlot,
            TextView tvOutTime,
            TextView tvAvailTime,
            LinearLayout timingLayout
    ) {
        String fromTime = tvFrom.getText().toString();
        String toTime = tvTo.getText().toString();
        if (fromTime.isEmpty() || toTime.isEmpty()) return;
        ivSlot.setVisibility(View.GONE);
        View slotView = LayoutInflater.from(context).inflate(R.layout.layout_excluded_slots, exceptionContainer, false);
        GridLayout gridSlots = slotView.findViewById(R.id.gridSlots);
        List<String> tempSelected = new ArrayList<>(selectedSlots);
        List<String> allSlots = generateSlots(fromTime, toTime);

        List<TextView> allSlotViews = new ArrayList<>();

        for (String slot : allSlots) {
            TextView slotTile = createSlotView(context, slot, tempSelected, allSlotViews);
            allSlotViews.add(slotTile);
            gridSlots.addView(slotTile);
        }

// Apply initial state in case slots were pre-selected
        refreshSlotStates(allSlotViews, tempSelected);

        slotView.findViewById(R.id.btnCancel).setOnClickListener(c -> {
            exceptionContainer.removeView(slotView);
            ivSlot.setVisibility(selectedSlots.isEmpty() ? View.GONE : VISIBLE);
        });

        slotView.findViewById(R.id.btnSave).setOnClickListener(c -> {
            if (tempSelected.size() > 4) {
                AndroidUtils.showAlert("You can select a maximum of 4 blocked slots.", getActivity());
                return;
            }
            selectedSlots.clear();
            selectedSlots.addAll(tempSelected);
            llSelectedSlots.removeAllViews();
            for (String s : selectedSlots) {
                llSelectedSlots.addView(createSelectedSlotTextView(context, s));
            }
            if (selectedSlots.isEmpty()) {
                timingLayout.setVisibility(View.GONE);
            } else {
                timingLayout.setVisibility(VISIBLE);
                updateSummary(fromTime, toTime, selectedSlots, tvOutTime, tvAvailTime);
            }
            exceptionContainer.removeView(slotView);
            ivSlot.setVisibility(VISIBLE);
        });

        slotView.findViewById(R.id.btnClose).setOnClickListener(c -> {
            exceptionContainer.removeView(slotView);
            ivSlot.setVisibility(VISIBLE);
        });

        exceptionContainer.addView(slotView);
        exceptionContainer.setVisibility(VISIBLE);
        slotView.bringToFront();
    }

    private void refreshDay(
            String dayName,
            TextView tvFrom,
            TextView tvTo,
            ImageView ivSlot,
            FlexboxLayout llSelectedSlots,
            List<String> selectedSlots,
            TextView tvOutTime,
            TextView tvAvailTime
    ) {
        String fromTime = tvFrom.getText().toString().trim();
        String toTime = tvTo.getText().toString().trim();
        if (fromTime.isEmpty() || toTime.isEmpty()) {
            ivSlot.setVisibility(View.GONE);
            llSelectedSlots.removeAllViews();
            selectedSlots.clear();
            tvOutTime.setText("0h out");
            tvAvailTime.setText("0h avail");
            return;
        }
        dayAvailability.put(dayName, new String[]{fromTime, toTime});
        updateSlotIconVisibility(tvFrom, tvTo, ivSlot);
        llSelectedSlots.removeAllViews();
        for (String slot : selectedSlots) {
            llSelectedSlots.addView(createSelectedSlotTextView(tvFrom.getContext(), slot));
        }
        updateSummary(fromTime, toTime, selectedSlots, tvOutTime, tvAvailTime);
    }

    private String convertTo12HourFormat(String time24) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("H:mm", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            Date date = inputFormat.parse(time24);
            return outputFormat.format(date);
        } catch (Exception e) {
            return time24;
        }
    }

    private TextView createSelectedSlotTextView(Context context, String text) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(12);
        tv.setTextColor(Color.BLACK);
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundResource(R.drawable.bg_exclude_container);
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());
        tv.setPadding(padding, padding, padding, padding);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, context.getResources().getDisplayMetrics());
        params.setMargins(margin, margin, margin, margin);
        tv.setLayoutParams(params);
        return tv;
    }

    private void updateSlotIconVisibility(TextView from, TextView to, ImageView slotIcon) {
        String fromTime = from.getText().toString();
        String toTime = to.getText().toString();
        if (fromTime.isEmpty() || toTime.isEmpty()) {
            slotIcon.setVisibility(View.GONE);
            return;
        }
        boolean showIcon = isMoreThanFourHours(fromTime, toTime);
        slotIcon.setVisibility(showIcon ? VISIBLE : View.GONE);
    }

    private boolean isMoreThanFourHours(String fromTime, String toTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            Date from = sdf.parse(fromTime);
            Date to = sdf.parse(toTime);
            if (to.before(from)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(to);
                cal.add(Calendar.DATE, 1);
                to = cal.getTime();
            }
            long diffMillis = to.getTime() - from.getTime();
            return diffMillis >= (4 * 60 * 60 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<String> generateSlots(String fromTime, String toTime) {
        List<String> slots = new ArrayList<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            Calendar start = Calendar.getInstance();
            start.setTime(sdf.parse(fromTime));
            Calendar end = Calendar.getInstance();
            end.setTime(sdf.parse(toTime));
            if (end.before(start)) end.add(Calendar.DATE, 1);
            while (start.before(end)) {
                Calendar slotEndCal = (Calendar) start.clone();
                slotEndCal.add(Calendar.MINUTE, 30);
                if (slotEndCal.after(end)) break;
                slots.add(sdf.format(start.getTime()) + " - " + sdf.format(slotEndCal.getTime()));
                start = slotEndCal;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return slots;
    }

    private TextView createSlotView(Context context, String slotText,
                                    List<String> tempSelectedSlots, List<TextView> allSlotViews) {
        TextView tv = new TextView(context);
        tv.setText(slotText);
        tv.setGravity(Gravity.CENTER);
        Typeface typeface = ResourcesCompat.getFont(tv.getContext(), R.font.gill_sans);
        tv.setTypeface(typeface);
        tv.setTextSize(12);
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12,
                context.getResources().getDisplayMetrics());
        tv.setPadding(padding, padding, padding, padding);
        tv.setBackgroundResource(R.drawable.bg_slot_unselected);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                context.getResources().getDisplayMetrics());
        params.setMargins(margin, margin, margin, margin);
        tv.setLayoutParams(params);

        if (tempSelectedSlots.contains(slotText)) {
            tv.setSelected(true);
            tv.setTextColor(ContextCompat.getColor(context, R.color.white));
            tv.setBackgroundResource(R.drawable.bg_slot_selected);
        } else {
            tv.setTextColor(ContextCompat.getColor(context, R.color.black));
        }

        tv.setOnClickListener(v -> {
            boolean isNowSelected = !v.isSelected();

            if (isNowSelected && tempSelectedSlots.size() >= 4) {
                // Max reached, ignore tap
                return;
            }

            v.setSelected(isNowSelected);

            if (isNowSelected) {
                tv.setBackgroundResource(R.drawable.bg_slot_selected);
                tv.setTextColor(ContextCompat.getColor(context, R.color.white));
                if (!tempSelectedSlots.contains(slotText)) tempSelectedSlots.add(slotText);
            } else {
                tv.setBackgroundResource(R.drawable.bg_slot_unselected);
                tv.setTextColor(ContextCompat.getColor(context, R.color.black));
                tempSelectedSlots.remove(slotText);
            }

            // After every tap, refresh enabled/disabled state for all slots
            refreshSlotStates(allSlotViews, tempSelectedSlots);
        });

        return tv;
    }

    private void refreshSlotStates(List<TextView> allSlotViews, List<String> tempSelectedSlots) {
        boolean maxReached = tempSelectedSlots.size() >= 4;
        for (TextView slot : allSlotViews) {
            if (slot.isSelected()) {
                // Always keep selected slots clickable so user can deselect
                slot.setAlpha(1.0f);
                slot.setClickable(true);
            } else if (maxReached) {
                // Dim and block unselected slots when limit hit
                slot.setAlpha(0.5f);
                slot.setClickable(false);
            } else {
                // Restore unselected slots when under limit
                slot.setAlpha(1.0f);
                slot.setClickable(true);
            }
        }
    }

    private void updateSummary(
            String fromTime,
            String toTime,
            List<String> selectedSlots,
            TextView tvAvailability,
            TextView tvExcluded
    ) {
        if (fromTime == null || fromTime.trim().isEmpty()
                || toTime == null || toTime.trim().isEmpty()) {
            tvAvailability.setText("Availability:");
            tvExcluded.setText("Excluded:");
            return;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            Date from = sdf.parse(fromTime);
            Date to = sdf.parse(toTime);
            if (from == null || to == null) return;

            Calendar startCal = Calendar.getInstance();
            startCal.setTime(from);
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(to);
            if (endCal.before(startCal)) {
                endCal.add(Calendar.DATE, 1);
            }

            long totalMinutes = (endCal.getTimeInMillis() - startCal.getTimeInMillis()) / (1000 * 60);
            long excludedSlots = selectedSlots == null ? 0 : selectedSlots.size();
            long totalSlots = totalMinutes / 30;
            long availableSlots = Math.max(0, totalSlots - excludedSlots);
            long availableMinutes = availableSlots * 30;

            tvAvailability.setText(
                    "Availability: " +
                            formatHoursMinutes(availableMinutes) +
                            " (" + availableSlots + " slot" + (availableSlots == 1 ? "" : "s") + ")"
            );

            if (selectedSlots == null || selectedSlots.isEmpty()) {
                tvExcluded.setText("Excluded: 0 slots");
            } else {
                tvExcluded.setText(
                        "Excluded: " + excludedSlots +
                                " slot" + (excludedSlots == 1 ? "" : "s") +
                                " ; " + TextUtils.join(", ", selectedSlots)
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            tvAvailability.setText("Availability:");
            tvExcluded.setText("Excluded:");
        }
    }

    private String formatHoursMinutes(long minutes) {
        long hours = minutes / 60;
        long mins = minutes % 60;
        if (mins == 0) {
            return hours + " hours";
        }
        return hours + "h " + mins + "m";
    }

    private void openTimePicker(String existingTime, TimeCallback callback) {
        Calendar cal = Calendar.getInstance();
        if (existingTime != null && !existingTime.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                Date date = sdf.parse(existingTime);
                cal.setTime(date);
            } catch (Exception ignored) {
            }
        }
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        TimePickerDialog dialog = new TimePickerDialog(
                getContext(),
                (view, h, m) -> {
                    int selectedMinute = (m < 15) ? 0 : (m < 45) ? 30 : 0;
                    if (m >= 45) {
                        h = (h + 1) % 24;
                    }
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.HOUR_OF_DAY, h);
                    c.set(Calendar.MINUTE, selectedMinute);
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    callback.onTimeSelected(sdf.format(c.getTime()));
                },
                hour,
                minute,
                false
        );
        dialog.show();
    }

    interface TimeCallback {
        void onTimeSelected(String time);
    }

    public Map<String, String[]> getDayAvailability() {
        return dayAvailability;
    }

    private void addItem(
            LayoutInflater inflater,
            LinearLayout parent,
            TextView title,
            String titleText,
            String content,
            String year,
            int type
    ) {
        parent.setVisibility(VISIBLE);
        title.setVisibility(VISIBLE);
        View row = inflater.inflate(R.layout.educational_item_view, parent, false);
        TextInputEditText etTitle = row.findViewById(R.id.et_title);
        TextInputEditText etContent = row.findViewById(R.id.et_content);
        TextInputEditText etYear = row.findViewById(R.id.et_year);
        etTitle.addTextChangedListener(new Validation(etTitle));
        etContent.addTextChangedListener(new Validation(etContent));
        etYear.setInputType(InputType.TYPE_CLASS_NUMBER);
        etYear.setHint("Year");
        AndroidUtils.NumberFilter(etYear, true);
        switch (type) {
            case TYPE_EDUCATION:
                etTitle.setHint("Degree Name");
                etContent.setHint("Institution");
                break;
            case TYPE_CERTIFICATION:
                etTitle.setHint("Certification Name");
                etContent.setHint("Institution");
                break;
            case TYPE_AWARD:
                etTitle.setHint("Award Name");
                etContent.setHint("Institution");
                break;
        }
        etTitle.setText(titleText);
        etContent.setText(content);
        etYear.setText(year);
        setupDeleteButton(row, parent);
        parent.addView(row);
    }

    private void loadItems(
            LayoutInflater inflater,
            LinearLayout parent,
            TextView title,
            List<?> list,
            int type
    ) {
        parent.removeAllViews();
        parent.setVisibility(VISIBLE);
        title.setVisibility(VISIBLE);
        if (list == null || list.isEmpty()) {
            addItem(inflater, parent, title, "", "", "", type);
            return;
        }
        for (Object obj : list) {
            if (type == TYPE_EDUCATION) {
                FirmProfileModel.Education e = (FirmProfileModel.Education) obj;
                addItem(inflater, parent, title, e.getDegree(), e.getUniversity(), String.valueOf(e.getPassing_year()), type);
            } else if (type == TYPE_CERTIFICATION) {
                FirmProfileModel.Certification c = (FirmProfileModel.Certification) obj;
                addItem(inflater, parent, title, c.getCertification_name(), c.getIssuing_authority(), String.valueOf(c.getYear_of_issue()), type);
            } else if (type == TYPE_AWARD) {
                FirmProfileModel.Award a = (FirmProfileModel.Award) obj;
                addItem(inflater, parent, title, a.getAward_name(), a.getPurpose(), String.valueOf(a.getYear_of_award()), type);
            }
        }
    }

    private void setupDeleteButton(View row, LinearLayout parent) {
        ImageView ivDelete = row.findViewById(R.id.iv_delete);
        ivDelete.setOnClickListener(v -> {
            parent.removeView(row);
            if (parent.getChildCount() == 0) {
                parent.setVisibility(View.GONE);
            }
        });
    }

    private void addCertificationItems(
            LayoutInflater inflater,
            LinearLayout parentLayout,
            TextView titleView,
            List<FirmProfileModel.Certification> list
    ) {
        parentLayout.removeAllViews();
        if (list == null || list.isEmpty()) {
            titleView.setVisibility(View.GONE);
            parentLayout.setVisibility(View.GONE);
            return;
        }
        titleView.setVisibility(VISIBLE);
        parentLayout.setVisibility(VISIBLE);
        for (FirmProfileModel.Certification item : list) {
            View row = inflater.inflate(R.layout.educational_item_view, parentLayout, false);
            TextInputEditText etTitle = row.findViewById(R.id.et_title);
            TextInputEditText etContent = row.findViewById(R.id.et_content);
            TextInputEditText etYear = row.findViewById(R.id.et_year);
            ImageView ivDelete = row.findViewById(R.id.iv_delete);
            etTitle.setText(item.getCertification_name());
            etContent.setText(item.getIssuing_authority());
            etYear.setText(String.valueOf(item.getYear_of_issue()));
            parentLayout.addView(row);
        }
    }

    private void addEducationItems(
            LayoutInflater inflater,
            LinearLayout parentLayout,
            TextView titleView,
            List<FirmProfileModel.Education> list
    ) {
        parentLayout.removeAllViews();
        if (list == null || list.isEmpty()) {
            titleView.setVisibility(View.GONE);
            parentLayout.setVisibility(View.GONE);
            return;
        }
        titleView.setVisibility(VISIBLE);
        parentLayout.setVisibility(VISIBLE);
        for (FirmProfileModel.Education item : list) {
            View row = inflater.inflate(R.layout.educational_item_view, parentLayout, false);
            TextInputEditText etTitle = row.findViewById(R.id.et_title);
            TextInputEditText etContent = row.findViewById(R.id.et_content);
            TextInputEditText etYear = row.findViewById(R.id.et_year);
            ImageView ivDelete = row.findViewById(R.id.iv_delete);
            etTitle.setText(item.getDegree());
            etContent.setText(item.getUniversity());
            etYear.setText(String.valueOf(item.getPassing_year()));
            parentLayout.addView(row);
        }
    }

    // ── Static data ───────────────────────────────────────────────────────────

    private static final List<String> COURT_TYPES = Arrays.asList(
            "High Court", "Supreme Court", "District & Other Lower Courts"
    );

    // Add this helper method in your class
    private ArrayList<String> buildCourtNameList(String state) {
        ArrayList<String> names = new ArrayList<>();
        String name = highCourtNameMap.get(state);
        if (!TextUtils.isEmpty(name)) names.add(name);
        return names;
    }

    // ── Method ────────────────────────────────────────────────────────────────

    // Helper method to convert display text to API format
    private String convertCourtTypeToApiFormat(String displayType) {
        if (displayType.equalsIgnoreCase("Supreme Court")) {
            return "supreme_court";
        } else if (displayType.equalsIgnoreCase("High Court")) {
            return "high_court";
        } else if (displayType.equalsIgnoreCase("District & Other Lower Courts")) {
            return "district_court";
        }
        return displayType.toLowerCase().replace(" ", "_");
    }

    // Helper method to convert API format to display text
    private String convertApiToDisplayFormat(String apiType) {
        if (apiType.equalsIgnoreCase("supreme_court")) {
            return "Supreme Court";
        } else if (apiType.equalsIgnoreCase("high_court")) {
            return "High Court";
        } else if (apiType.equalsIgnoreCase("district_court")) {
            return "District & Other Lower Courts";
        }
        return apiType;
    }

// ── Helpers for show/hide ─────────────────────────────────────────────────

    private void showTextField(View textWrap, LinearLayout dropdownLayout, ListView dropdownList) {
        if (textWrap != null) textWrap.setVisibility(View.VISIBLE);
        if (dropdownLayout != null) dropdownLayout.setVisibility(View.GONE);
        if (dropdownList != null) dropdownList.setVisibility(View.GONE);
    }

    private void showDropdown(View textWrap, LinearLayout dropdownLayout) {
        if (textWrap != null) textWrap.setVisibility(View.GONE);
        if (dropdownLayout != null) dropdownLayout.setVisibility(View.VISIBLE);
    }

    // ─────────────────────────────────────────────────────────────────────────────
// UPDATED addCourtEnrollmentRow — all dropdowns now use DisplaySpinnerView
// with built-in search (attachSearch) so users can filter long lists.
//
// Prerequisites (must exist in AndroidUtils):
//   • DisplaySpinnerView(ListView, TextView, String, ImageView, ImageView,
//                         boolean, CommonSpinnerAdapter)
//   • attachSearch(ListView, CommonSpinnerAdapter)            ← attaches search bar
//   • LoadList(ListView, Context, int, boolean)
// ─────────────────────────────────────────────────────────────────────────────

    // ── Private helper: close a dropdown list, hide its search bar, reset filter ──
//    private void closeSpinnerDropdown(
//            ListView list,
//            TextView label,
//            ImageView drop,
//            ImageView clear,
//            CommonSpinnerAdapter adapter
//    ) {
//        if (list == null) return;
//        list.setVisibility(View.GONE);
//
//        // Hide the search CardView injected by attachSearch
//        View searchCard = (View) list.getTag(R.id.tag_search_bar);
//        if (searchCard != null) {
//            searchCard.setVisibility(View.GONE);
//            if (searchCard.getParent() instanceof ViewGroup) {
//                ((ViewGroup) searchCard.getParent()).setVisibility(View.GONE);
//            }
//            EditText etSearch = (EditText) list.getTag(R.id.tag_search_edittext);
//            if (etSearch != null) etSearch.setText("");
//        }
//
//        // Reset adapter filter so full list shows next open
//        if (adapter != null) {
//            adapter.getFilter().filter("");
//        }
//
//        // Restore icons based on whether a value is selected
//        String currentText = (label != null && label.getText() != null)
//                ? label.getText().toString().trim() : "";
//        if (!currentText.isEmpty()) {
//            if (drop  != null) drop.setVisibility(View.GONE);
//            if (clear != null) clear.setVisibility(View.VISIBLE);
//        } else {
//            if (drop  != null) drop.setVisibility(View.VISIBLE);
//            if (clear != null) clear.setVisibility(View.GONE);
//        }
//    }

    // ── Private helper: open a dropdown list, show its search bar ─────────────
//    private void openSpinnerDropdown(ListView list, CommonSpinnerAdapter adapter) {
//        if (list == null) return;
//
//        // Show the search CardView injected by attachSearch
//        View searchCard = (View) list.getTag(R.id.tag_search_bar);
//        if (searchCard != null) {
//            searchCard.setVisibility(View.VISIBLE);
//            if (searchCard.getParent() instanceof ViewGroup) {
//                ((ViewGroup) searchCard.getParent()).setVisibility(View.VISIBLE);
//            }
//        }
//
//        // Reset filter to full list then make list visible
//        if (adapter != null) {
//            adapter.getFilter().filter("", count -> {
//                list.setVisibility(View.VISIBLE);
//                AndroidUtils.LoadList(list, list.getContext(), adapter.getCount(), true);
//            });
//        } else {
//            list.setVisibility(View.VISIBLE);
//        }
//    }

    // ── Private helper: hide search bar without closing list (used after attach) ──
    private void hideSearchBarForList(ListView list) {
        if (list == null) return;
        View searchCard = (View) list.getTag(R.id.tag_search_bar);
        if (searchCard != null) {
            searchCard.setVisibility(View.GONE);
            if (searchCard.getParent() instanceof ViewGroup) {
                ((ViewGroup) searchCard.getParent()).setVisibility(View.GONE);
            }
        }
        list.setVisibility(View.GONE);
    }

    private void addCourtEnrollmentRow(
            LayoutInflater inflater,
            LinearLayout parent,
            String courtType,
            String state,
            String city,
            String courtName
    ) {
        if (parent == null) return;
        parent.setVisibility(VISIBLE);

        View row = inflater.inflate(R.layout.court_enrollment_item, parent, false);
        String displayCourtType = convertApiToDisplayFormat(courtType);

        // ── Court Type ────────────────────────────────────────────────────────
        LinearLayout llCourtType = row.findViewById(R.id.ll_court_type);
        TextView tvCourtType = llCourtType != null ? llCourtType.findViewById(R.id.tv_spinner_view) : null;
        ImageView imgCtDrop = llCourtType != null ? llCourtType.findViewById(R.id.img_dropdown_icon) : null;
        ImageView imgCtClear = llCourtType != null ? llCourtType.findViewById(R.id.img_clear_icon) : null;
        ListView spCourtType = row.findViewById(R.id.sp_court_type);
        if (tvCourtType != null) tvCourtType.setHint("Select Court Type");

        // ── State ─────────────────────────────────────────────────────────────
        LinearLayout llStateSpinner = row.findViewById(R.id.ll_state_spinner);
        TextView tvState = llStateSpinner != null ? llStateSpinner.findViewById(R.id.tv_spinner_view) : null;
        ImageView imgStateDrop = llStateSpinner != null ? llStateSpinner.findViewById(R.id.img_dropdown_icon) : null;
        ImageView imgStateClear = llStateSpinner != null ? llStateSpinner.findViewById(R.id.img_clear_icon) : null;
        ListView spStateList = row.findViewById(R.id.sp_state_list);
        if (tvState != null) tvState.setHint("Select State");

        // ── City with TextInputEditText + Tick/Cancel ─────────────────────────
        LinearLayout tlCityArea = row.findViewById(R.id.tl_city_area);
        TextInputEditText etCitySearch = row.findViewById(R.id.et_city_search);
        AppCompatImageButton btnCityConfirm = row.findViewById(R.id.btn_city_confirm);
        AppCompatImageButton btnCityCancel = row.findViewById(R.id.btn_city_cancel);
        ImageView imgCityDropdown = row.findViewById(R.id.img_city_dropdown);
        ListView spCityList = row.findViewById(R.id.sp_city_list);

        if (etCitySearch != null) {
//            etCitySearch.setHint("Search or enter city");
            if (!TextUtils.isEmpty(city)) {
                etCitySearch.setText(city);
            }
        }

        // Initially hide confirm button, show cancel
        // Confirm always hidden initially
// Cancel only visible if city is pre-filled (i.e. there's already a value to clear)
        if (btnCityConfirm != null) btnCityConfirm.setVisibility(View.GONE);
        if (btnCityCancel != null) btnCityCancel.setVisibility(View.GONE);

        // ── ALL rows VISIBLE immediately ──────────────────────────────────────
        if (llCourtType != null) llCourtType.setVisibility(View.VISIBLE);
        if (llStateSpinner != null) {
            llStateSpinner.setVisibility(View.VISIBLE);
            llStateSpinner.setEnabled(false);
            llStateSpinner.setClickable(false);
            llStateSpinner.setAlpha(0.5f);
        }

        // ── City disabled until state is selected ─────────────────────────────
        if (tlCityArea != null) {
            tlCityArea.setEnabled(false);
            tlCityArea.setAlpha(0.5f);
        }
        if (etCitySearch != null) etCitySearch.setEnabled(false);

        // ── Icons: dropdown visible, clear hidden ─────────────────────────────
        if (imgCtDrop != null) imgCtDrop.setVisibility(View.VISIBLE);
        if (imgCtClear != null) imgCtClear.setVisibility(View.GONE);
        if (imgStateDrop != null) imgStateDrop.setVisibility(View.VISIBLE);
        if (imgStateClear != null) imgStateClear.setVisibility(View.GONE);

        // ── Check if practice areas are present ──────────────────────────────
        boolean hasPracticeAreas = false;
        if (chipPractice != null && chipPractice.getChildCount() > 0) {
            JSONArray paVals = getChipValuesFromGroup(chipPractice);
            hasPracticeAreas = paVals.length() > 0;
        }

        if (!hasPracticeAreas) {
            if (llCourtType != null) {
                llCourtType.setEnabled(false);
                llCourtType.setClickable(false);
                llCourtType.setAlpha(0.4f);
            }
            if (llStateSpinner != null) {
                llStateSpinner.setEnabled(false);
                llStateSpinner.setClickable(false);
                llStateSpinner.setAlpha(0.4f);
            }
            if (tlCityArea != null) {
                tlCityArea.setEnabled(false);
                tlCityArea.setClickable(false);
                tlCityArea.setAlpha(0.4f);
            }
            if (etCitySearch != null) etCitySearch.setEnabled(false);
            if (imgCtDrop != null) imgCtDrop.setVisibility(View.GONE);
            if (imgCtClear != null) imgCtClear.setVisibility(View.GONE);
            if (imgStateDrop != null) imgStateDrop.setVisibility(View.GONE);
            if (imgStateClear != null) imgStateClear.setVisibility(View.GONE);
            if (btnCityConfirm != null) btnCityConfirm.setVisibility(View.GONE);
            if (btnCityCancel != null) btnCityCancel.setVisibility(View.GONE);
            if (imgCityDropdown != null) imgCityDropdown.setVisibility(View.GONE);
        }

        // ── Delete button ─────────────────────────────────────────────────────
        View ivDelete = row.findViewById(R.id.iv_delete);
        if (ivDelete != null) {
            ivDelete.setOnClickListener(v -> {
                ivDelete.setEnabled(false);
                if (row.getParent() == parent) parent.removeView(row);
                if (parent.getChildCount() == 0) parent.setVisibility(View.GONE);
            });
        }

        // ── Clear errors ──────────────────────────────────────────────────────
        setCourtError(row, R.id.tvCourtTypeError, null);
        setCourtError(row, R.id.tvStateNameError, null);
        setCourtError(row, R.id.tvCourtCityError, null);

        // ── Adapter holders ───────────────────────────────────────────────────
        final CommonSpinnerAdapter[] ctAdapterHolder = {null};
        final CommonSpinnerAdapter[] stateAdapterHolder = {null};
        final CommonSpinnerAdapter[] cityAdapterHolder = {null};

        // ─────────────────────────────────────────────────────────────────────
        // CITY SELECTION with TextInputEditText + Tick/Cancel
        // ─────────────────────────────────────────────────────────────────────
        final int cityMaxHeightDp = 150;
        final int cityItemHeightDp = 48;

        Runnable setupCityDropdown = () -> {
            String selectedStateName = tvState != null ? tvState.getText().toString().trim() : "";
            if (selectedStateName.isEmpty()) return;

            ArrayList<String> cities = new ArrayList<>();
            ArrayList<String> mapped = courtStatesMap.get(selectedStateName);
            if (mapped != null) cities.addAll(mapped);

            CommonSpinnerAdapter cityAdapter = new CommonSpinnerAdapter((Activity) requireContext(), cities);
            cityAdapterHolder[0] = cityAdapter;

            if (spCityList != null) {
                spCityList.setAdapter(cityAdapter);
                spCityList.setVisibility(View.GONE);
            }

            // ── Helper: resize ListView to match filtered item count (max 150dp) ──
            Runnable resizeCityList = () -> {
                if (spCityList == null || cityAdapterHolder[0] == null) return;
                int count = cityAdapterHolder[0].getCount();
                if (count == 0) {
                    spCityList.setVisibility(View.GONE);
                    if (imgCityDropdown != null)
                        imgCityDropdown.animate().rotation(0).setDuration(200).start();
                    return;
                }
                float density = requireContext().getResources().getDisplayMetrics().density;
                int itemPx = (int) (cityItemHeightDp * density);
                int maxPx = (int) (cityMaxHeightDp * density);
                int height = Math.min(count * itemPx, maxPx);
                ViewGroup.LayoutParams lp = spCityList.getLayoutParams();
                if (lp == null) lp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, height);
                lp.height = height;
                spCityList.setLayoutParams(lp);
                AndroidUtils.LoadList(spCityList, requireContext(), count, true);
                spCityList.setVisibility(View.VISIBLE);
                if (imgCityDropdown != null)
                    imgCityDropdown.animate().rotation(180).setDuration(200).start();
            };

            // ── Remove any previously registered TextWatcher ──────────────────
            if (etCitySearch != null) {
                Object oldWatcher = etCitySearch.getTag(R.id.tag_search_edittext);
                if (oldWatcher instanceof TextWatcher)
                    etCitySearch.removeTextChangedListener((TextWatcher) oldWatcher);

                TextWatcher cityWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int st, int c, int a) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int st, int b, int c) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String query = s != null ? s.toString() : "";

                        if (btnCityConfirm != null)
                            btnCityConfirm.setVisibility(query.length() > 0 ? View.VISIBLE : View.GONE);
                        if (btnCityCancel != null)
                            btnCityCancel.setVisibility(query.length() > 0 ? View.VISIBLE : View.GONE);

                        // Skip filter if field doesn't have focus
                        if (etCitySearch == null || !etCitySearch.hasFocus()) return;
                        if (cityAdapterHolder[0] == null) return;

                        if (query.isEmpty()) {
                            // Empty query → reset filter → show ALL cities
                            cityAdapterHolder[0].getFilter().filter("", count -> {
                                if (!isAdded()) return;
                                if (count > 0) {
                                    resizeCityList.run(); // shows list with correct height
                                } else {
                                    spCityList.setVisibility(View.GONE);
                                }
                            });
                            return;
                        }

                        cityAdapterHolder[0].getFilter().filter(query, count -> {
                            if (!isAdded()) return;
                            if (count == 0) {
                                spCityList.setVisibility(View.GONE);
                                if (imgCityDropdown != null)
                                    imgCityDropdown.animate().rotation(0).setDuration(200).start();
                            } else {
                                resizeCityList.run();
                            }
                        });
                    }
                };

                etCitySearch.setTag(R.id.tag_search_edittext, cityWatcher);
                etCitySearch.addTextChangedListener(cityWatcher);
            }

            // ── Item click ────────────────────────────────────────────────────
            if (spCityList != null) {
                spCityList.setOnItemClickListener((parent1, view2, position, id) -> {
                    Object item = cityAdapterHolder[0] != null
                            ? cityAdapterHolder[0].getItem(position) : null;
                    String selectedCity = item != null ? item.toString() : "";

                    if (!TextUtils.isEmpty(selectedCity) && etCitySearch != null) {
                        Object w = etCitySearch.getTag(R.id.tag_search_edittext);
                        if (w instanceof TextWatcher)
                            etCitySearch.removeTextChangedListener((TextWatcher) w);

                        etCitySearch.setText(selectedCity);

                        if (w instanceof TextWatcher)
                            etCitySearch.addTextChangedListener((TextWatcher) w);

                        setCourtError(row, R.id.tvCourtCityError, null);
                    }
                    spCityList.setVisibility(View.GONE);
                    if (imgCityDropdown != null)
                        imgCityDropdown.animate().rotation(0).setDuration(200).start();
                    if (btnCityConfirm != null) btnCityConfirm.setVisibility(View.GONE);
                    if (btnCityCancel != null) btnCityCancel.setVisibility(GONE);

                    if (cityAdapterHolder[0] != null)
                        cityAdapterHolder[0].getFilter().filter("", null);
                });
            }

            // ── Dropdown icon toggle ──────────────────────────────────────────
            if (imgCityDropdown != null) {
                imgCityDropdown.setOnClickListener(v -> {
                    if (spCityList == null || cityAdapterHolder[0] == null) return;

                    if (spCityList.getVisibility() == View.VISIBLE) {
                        spCityList.setVisibility(View.GONE);
                        imgCityDropdown.animate().rotation(0).setDuration(200).start();
                    } else {
                        // Reset filter to show ALL cities
                        cityAdapterHolder[0].getFilter().filter("", count -> {
                            if (!isAdded()) return;
                            resizeCityList.run();
                        });
                    }
                });
            }

// ── Tap on city field also opens the list ─────────────────────────
            if (etCitySearch != null) {
                etCitySearch.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus && cityAdapterHolder[0] != null
                            && spCityList != null
                            && spCityList.getVisibility() != View.VISIBLE) {
                        cityAdapterHolder[0].getFilter().filter("", count -> {
                            if (!isAdded()) return;
                            resizeCityList.run();
                        });
                    }
                });

                // ── Also handle re-click when field already has focus ─────────
                etCitySearch.setOnClickListener(v -> {
                    if (cityAdapterHolder[0] != null
                            && spCityList != null
                            && spCityList.getVisibility() != View.VISIBLE) {
                        cityAdapterHolder[0].getFilter().filter("", count -> {
                            if (!isAdded()) return;
                            resizeCityList.run();
                        });
                    }
                });
            }

            // ── Confirm button ────────────────────────────────────────────────
            if (btnCityConfirm != null) {
                btnCityConfirm.setOnClickListener(v -> {
                    String text = etCitySearch != null && etCitySearch.getText() != null
                            ? etCitySearch.getText().toString().trim() : "";
                    if (!text.isEmpty()) setCourtError(row, R.id.tvCourtCityError, null);
                    spCityList.setVisibility(View.GONE);
                    if (imgCityDropdown != null)
                        imgCityDropdown.animate().rotation(0).setDuration(200).start();
                    btnCityConfirm.setVisibility(View.GONE);
                    if (btnCityCancel != null) btnCityCancel.setVisibility(GONE);
                });
            }

            // ── Cancel button ─────────────────────────────────────────────────
            if (btnCityCancel != null) {
                btnCityCancel.setOnClickListener(v -> {
                    if (etCitySearch != null) etCitySearch.setText("");
                    spCityList.setVisibility(View.GONE);
                    if (imgCityDropdown != null)
                        imgCityDropdown.animate().rotation(0).setDuration(200).start();
                    if (btnCityConfirm != null) btnCityConfirm.setVisibility(View.GONE);
                    btnCityCancel.setVisibility(GONE);
                    setCourtError(row, R.id.tvCourtCityError, null);
                });
            }

            // ── IME Done ──────────────────────────────────────────────────────
            if (etCitySearch != null) {
                etCitySearch.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        spCityList.setVisibility(View.GONE);
                        if (imgCityDropdown != null)
                            imgCityDropdown.animate().rotation(0).setDuration(200).start();
                        if (btnCityConfirm != null) btnCityConfirm.setVisibility(View.GONE);
                        if (btnCityCancel != null) btnCityCancel.setVisibility(GONE);
                        return true;
                    }
                    return false;
                });
            }
        };

        // ─────────────────────────────────────────────────────────────────────
        // wireStateDropdown
        // ─────────────────────────────────────────────────────────────────────
        Runnable wireStateDropdown = () -> {
            if (llStateSpinner == null) return;

            CommonSpinnerAdapter stateAdapter =
                    new CommonSpinnerAdapter((Activity) requireContext(), courtStateList);
            stateAdapterHolder[0] = stateAdapter;

            if (spStateList != null) {
                spStateList.setAdapter(stateAdapter);
                AndroidUtils.LoadList(spStateList, requireContext(), courtStateList.size(), true);
                AndroidUtils.attachSearch(spStateList, stateAdapter, "Search State");
                hideSearchBarForList(spStateList);
            }

            llStateSpinner.setEnabled(true);
            llStateSpinner.setClickable(true);
            llStateSpinner.setAlpha(1f);

            llStateSpinner.setOnClickListener(sv -> {
                boolean isVisible = spStateList != null && spStateList.getVisibility() == View.VISIBLE;
                String curVal = tvState != null ? tvState.getText().toString() : "";
                AndroidUtils.DisplaySpinnerView(
                        spStateList, tvState, curVal,
                        imgStateDrop, imgStateClear,
                        !isVisible, stateAdapterHolder[0], "Search State");
            });

            if (spStateList != null) {
                spStateList.setOnItemClickListener((p, v2, pos, id) -> {
                    String sel = (String) stateAdapterHolder[0].getItem(pos);
                    setCourtError(row, R.id.tvStateNameError, null);
                    AndroidUtils.DisplaySpinnerView(
                            spStateList, tvState, sel,
                            imgStateDrop, imgStateClear,
                            false, stateAdapterHolder[0], "Search State");

                    // ── Enable city now that state is selected ────────────────
                    if (tlCityArea != null) {
                        tlCityArea.setEnabled(true);
                        tlCityArea.setAlpha(1f);
                    }
                    if (etCitySearch != null) etCitySearch.setEnabled(true);
                    // ─────────────────────────────────────────────────────────

                    // Reset city when state changes
                    if (etCitySearch != null) {
                        etCitySearch.setText("");
                        if (btnCityConfirm != null) btnCityConfirm.setVisibility(View.GONE);
                        if (btnCityCancel != null) btnCityCancel.setVisibility(GONE);
                    }
                    if (spCityList != null) {
                        spCityList.setVisibility(View.GONE);
                        if (imgCityDropdown != null)
                            imgCityDropdown.animate().rotation(0).setDuration(200).start();
                    }

                    setupCityDropdown.run();
                });
            }
        };

        // ─────────────────────────────────────────────────────────────────────
        // applyCourtTypeLayout
        // ─────────────────────────────────────────────────────────────────────
        final Runnable[] applyCourtTypeLayout = {null};
        applyCourtTypeLayout[0] = () -> {
            String ct = tvCourtType != null ? tvCourtType.getText().toString().trim() : "";

            if ("Supreme Court".equals(ct)) {
                if (llStateSpinner != null) {
                    llStateSpinner.setEnabled(false);
                    llStateSpinner.setClickable(false);
                    llStateSpinner.setAlpha(0.5f);
                }
                if (tlCityArea != null) {
                    tlCityArea.setEnabled(false);
                    tlCityArea.setAlpha(0.5f);
                }
                if (etCitySearch != null) etCitySearch.setEnabled(false);

                String scState = (supremeCourtData != null && !TextUtils.isEmpty(supremeCourtData.getState()))
                        ? supremeCourtData.getState() : "Delhi";
                String scCity = (supremeCourtData != null && !TextUtils.isEmpty(supremeCourtData.getCity()))
                        ? supremeCourtData.getCity() : "New Delhi";
                if (tvState != null) {
                    tvState.setText(scState);
                    if (imgStateDrop != null) imgStateDrop.setVisibility(View.GONE);
                    if (imgStateClear != null) imgStateClear.setVisibility(View.GONE);
                }
                if (etCitySearch != null) {
                    etCitySearch.setText(scCity);
                }
            } else {
                if (llStateSpinner != null) {
                    llStateSpinner.setVisibility(View.VISIBLE);
                    llStateSpinner.setAlpha(1f);
                }
                // City enabled only if a state is already selected
                String currentState = tvState != null ? tvState.getText().toString().trim() : "";
                boolean stateSelected = !currentState.isEmpty();
                if (tlCityArea != null) {
                    tlCityArea.setEnabled(stateSelected);
                    tlCityArea.setAlpha(stateSelected ? 1f : 0.5f);
                }
                if (etCitySearch != null) etCitySearch.setEnabled(stateSelected);

                if (imgStateDrop != null) imgStateDrop.setVisibility(View.VISIBLE);
                if (imgStateClear != null) imgStateClear.setVisibility(View.GONE);
            }
        };

        Runnable buildCourtTypeAdapter = () -> {
            if (!allCourtTypesList.isEmpty() && !allCourtTypesList.contains("Supreme Court")) {
                allCourtTypesList.add(0, "Supreme Court");
            }

            // ── Build sorted display list: suggested first, then remaining sorted ──
            ArrayList<String> sortedCourtTypeList = new ArrayList<>();

            // 1. Suggested items first (in their original API order)
            for (String s : suggestedCourtTypesList) {
                if (!sortedCourtTypeList.contains(s)) sortedCourtTypeList.add(s);
            }

            // 2. Supreme Court pinned next if not already in suggested
            if (!sortedCourtTypeList.contains("Supreme Court")) {
                sortedCourtTypeList.add(0, "Supreme Court");
            }

            // 3. Remaining items from allCourtTypesList sorted alphabetically
            ArrayList<String> rest = new ArrayList<>();
            for (String s : allCourtTypesList) {
                if (!suggestedCourtTypesList.contains(s) && !sortedCourtTypeList.contains(s)) {
                    rest.add(s);
                }
            }
            Collections.sort(rest);
            sortedCourtTypeList.addAll(rest);

            CommonSpinnerAdapter ctAdapter =
                    new CommonSpinnerAdapter((Activity) requireContext(), sortedCourtTypeList);
            ctAdapter.setSuggestedItems(suggestedCourtTypesList);
            if (!suggestedCourtTypesList.isEmpty()) {
                if (ll_suggestedCourtType != null) ll_suggestedCourtType.setVisibility(VISIBLE);
            } else {
                if (ll_suggestedCourtType != null) ll_suggestedCourtType.setVisibility(GONE);
            }
            ctAdapterHolder[0] = ctAdapter;
            if (spCourtType != null) {
                spCourtType.setAdapter(ctAdapter);
                AndroidUtils.LoadList(spCourtType, requireContext(), sortedCourtTypeList.size(), true);
                AndroidUtils.attachSearch(spCourtType, ctAdapter, "Search Court Type");
                hideSearchBarForList(spCourtType);
            }

            if (llCourtType != null) {
                boolean hasPAForAdapter = false;
                if (chipPractice != null && chipPractice.getChildCount() > 0) {
                    JSONArray paValsForAdapter = getChipValuesFromGroup(chipPractice);
                    hasPAForAdapter = paValsForAdapter.length() > 0;
                }
                if (hasPAForAdapter) {
                    llCourtType.setOnClickListener(v -> {
                        boolean isVisible = spCourtType != null && spCourtType.getVisibility() == View.VISIBLE;
                        String curVal = tvCourtType != null ? tvCourtType.getText().toString() : "";
                        AndroidUtils.DisplaySpinnerView(
                                spCourtType, tvCourtType, curVal,
                                imgCtDrop, imgCtClear,
                                !isVisible, ctAdapterHolder[0], "Search Court Type");
                    });
                } else {
                    llCourtType.setOnClickListener(null);
                }
            }
            if (spCourtType != null) {
                spCourtType.setOnItemClickListener((p, v2, pos, id) -> {
                    String selected = (String) ctAdapterHolder[0].getItem(pos);

                    // ── Issue 4: block duplicate Supreme Court ────────────────────
                    if ("Supreme Court".equalsIgnoreCase(selected)
                            && isSupremeCourtAlreadyAdded(row)) {
                        AndroidUtils.showAlert(
                                "Supreme Court is already added.",
                                getActivity()
                        );
                        // Close dropdown, keep previous selection unchanged
                        AndroidUtils.DisplaySpinnerView(
                                spCourtType, tvCourtType,
                                tvCourtType != null ? tvCourtType.getText().toString() : "",
                                imgCtDrop, imgCtClear,
                                false, ctAdapterHolder[0], "Search Court Type");
                        return;
                    }
                    // ── existing code below — no changes ─────────────────────────
                    setCourtError(row, R.id.tvCourtTypeError, null);
                    setCourtError(row, R.id.tvStateNameError, null);
                    setCourtError(row, R.id.tvCourtCityError, null);
                    AndroidUtils.DisplaySpinnerView(
                            spCourtType, tvCourtType, selected,
                            imgCtDrop, imgCtClear,
                            false, ctAdapterHolder[0], "Search Court Type");

                    if (tvState != null) tvState.setText("");
                    if (imgStateDrop != null) imgStateDrop.setVisibility(View.VISIBLE);
                    if (imgStateClear != null) imgStateClear.setVisibility(View.GONE);
                    if (spStateList != null) spStateList.setVisibility(View.GONE);
                    hideSearchBarForList(spStateList);

                    if (etCitySearch != null) {
                        etCitySearch.setText("");
                        if (btnCityConfirm != null) btnCityConfirm.setVisibility(View.GONE);
                        if (btnCityCancel != null) btnCityCancel.setVisibility(GONE);
                    }
                    if (spCityList != null) {
                        spCityList.setVisibility(View.GONE);
                        if (imgCityDropdown != null)
                            imgCityDropdown.animate().rotation(0).setDuration(200).start();
                    }

                    if (tlCityArea != null) {
                        tlCityArea.setEnabled(false);
                        tlCityArea.setAlpha(0.5f);
                    }
                    if (etCitySearch != null) etCitySearch.setEnabled(false);

                    applyCourtTypeLayout[0].run();
                    if (!"Supreme Court".equals(selected)) {
                        wireStateDropdown.run();
                    }
                });
            }
        };

        // ── Clear icon: Court Type ────────────────────────────────────────────
        if (imgCtClear != null) {
            imgCtClear.setOnClickListener(v -> {
                if (tvCourtType != null) tvCourtType.setText("");
                if (imgCtDrop != null) imgCtDrop.setVisibility(View.VISIBLE);
                imgCtClear.setVisibility(View.GONE);
                if (spCourtType != null) spCourtType.setVisibility(View.GONE);
                hideSearchBarForList(spCourtType);

                if (tvState != null) tvState.setText("");
                if (imgStateDrop != null) imgStateDrop.setVisibility(View.VISIBLE);
                if (imgStateClear != null) imgStateClear.setVisibility(View.GONE);
                if (spStateList != null) spStateList.setVisibility(View.GONE);
                hideSearchBarForList(spStateList);

                if (etCitySearch != null) {
                    etCitySearch.setText("");
                    etCitySearch.setEnabled(false);
                    if (btnCityConfirm != null) btnCityConfirm.setVisibility(View.GONE);
                    if (btnCityCancel != null) btnCityCancel.setVisibility(GONE);
                }
                if (spCityList != null) {
                    spCityList.setVisibility(View.GONE);
                    if (imgCityDropdown != null)
                        imgCityDropdown.animate().rotation(0).setDuration(200).start();
                }

                // Disable state and city since court type was cleared
                if (llStateSpinner != null) {
                    llStateSpinner.setEnabled(false);
                    llStateSpinner.setClickable(false);
                    llStateSpinner.setAlpha(0.5f);
                }
                if (tlCityArea != null) {
                    tlCityArea.setEnabled(false);
                    tlCityArea.setAlpha(0.5f);
                }

                setCourtError(row, R.id.tvCourtTypeError, null);
                setCourtError(row, R.id.tvStateNameError, null);
                setCourtError(row, R.id.tvCourtCityError, null);
            });
        }

        // ── Clear icon: State ─────────────────────────────────────────────────
        if (imgStateClear != null) {
            imgStateClear.setOnClickListener(v -> {
                if (tvState != null) tvState.setText("");
                if (imgStateDrop != null) imgStateDrop.setVisibility(View.VISIBLE);
                imgStateClear.setVisibility(View.GONE);
                if (spStateList != null) spStateList.setVisibility(View.GONE);
                hideSearchBarForList(spStateList);

                // Disable city since state was cleared
                if (tlCityArea != null) {
                    tlCityArea.setEnabled(false);
                    tlCityArea.setAlpha(0.5f);
                }
                if (etCitySearch != null) {
                    etCitySearch.setEnabled(false);
                    etCitySearch.setText("");
                    if (btnCityConfirm != null) btnCityConfirm.setVisibility(View.GONE);
                    if (btnCityCancel != null) btnCityCancel.setVisibility(GONE);
                }
                if (spCityList != null) {
                    spCityList.setVisibility(View.GONE);
                    if (imgCityDropdown != null)
                        imgCityDropdown.animate().rotation(0).setDuration(200).start();
                }

                setCourtError(row, R.id.tvStateNameError, null);
                setCourtError(row, R.id.tvCourtCityError, null);
            });
        }

        // ─────────────────────────────────────────────────────────────────────
        // BUILD ADAPTER
        // ─────────────────────────────────────────────────────────────────────
        buildCourtTypeAdapter.run();

        // ─────────────────────────────────────────────────────────────────────
        // PRE-FILL existing data if present
        // ─────────────────────────────────────────────────────────────────────
        if (!TextUtils.isEmpty(displayCourtType)) {
            String courtType1 = displayCourtType.replace("_", " ").toLowerCase(Locale.getDefault());
            if (!courtType1.isEmpty()) {
                String[] words = courtType1.split(" ");
                StringBuilder result = new StringBuilder();
                for (String word : words) {
                    if (!word.isEmpty()) {
                        result.append(word.substring(0, 1).toUpperCase())
                                .append(word.substring(1))
                                .append(" ");
                    }
                }
                courtType1 = result.toString().trim();
            }

            if (tvCourtType != null) tvCourtType.setText(courtType1);
            if (imgCtDrop != null) imgCtDrop.setVisibility(View.GONE);
            if (imgCtClear != null) imgCtClear.setVisibility(View.VISIBLE);

            applyCourtTypeLayout[0].run();

            if (!"Supreme Court".equals(displayCourtType)) {
                wireStateDropdown.run();

                if (!TextUtils.isEmpty(state) && tvState != null) {
                    tvState.setText(state);
                    if (imgStateDrop != null) imgStateDrop.setVisibility(View.GONE);
                    if (imgStateClear != null) imgStateClear.setVisibility(View.VISIBLE);

                    // Enable city since state is pre-filled
                    if (tlCityArea != null) {
                        tlCityArea.setEnabled(true);
                        tlCityArea.setAlpha(1f);
                    }
                    if (etCitySearch != null) etCitySearch.setEnabled(true);

                    setupCityDropdown.run();

                    if (!TextUtils.isEmpty(city) && etCitySearch != null) {
                        Object w = etCitySearch.getTag(R.id.tag_search_edittext);
                        if (w instanceof TextWatcher)
                            etCitySearch.removeTextChangedListener((TextWatcher) w);

                        etCitySearch.setText(city);

                        if (w instanceof TextWatcher)
                            etCitySearch.addTextChangedListener((TextWatcher) w);

                        if (btnCityConfirm != null) btnCityConfirm.setVisibility(View.GONE);
                        if (btnCityCancel != null) btnCityCancel.setVisibility(GONE);
                    }
                }
                // If state is empty, city stays disabled (already set above)
            }
        } else {
            if (llStateSpinner != null) {
                llStateSpinner.setEnabled(false);
                llStateSpinner.setClickable(false);
                llStateSpinner.setAlpha(0.5f);
            }
            // City also stays disabled (already set above)
        }

        parent.addView(row);
    }

    // ── Add once to BasicProfileEdit ─────────────────────────────────────────
    private boolean isSupremeCourtAlreadyAdded(View excludeRow) {
        if (llCourtEnrollments == null) return false;
        for (int i = 0; i < llCourtEnrollments.getChildCount(); i++) {
            View row = llCourtEnrollments.getChildAt(i);
            if (row == excludeRow) continue; // skip the row being configured
            LinearLayout llCt = row.findViewById(R.id.ll_court_type);
            TextView tvCt = llCt != null ? llCt.findViewById(R.id.tv_spinner_view) : null;
            if (tvCt != null
                    && "Supreme Court".equalsIgnoreCase(tvCt.getText().toString().trim())) {
                return true;
            }
        }
        return false;
    }

    private void createPracticeDetails() {
        callCountriesWebService();
        llViewScreen.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View practiceView = inflater.inflate(R.layout.practicedetails_edit, llViewScreen, false);

        boolean isAMMorSuperuser = Constants.ROLE.equalsIgnoreCase("AAM");
        boolean isFirmProfileMode = isFirmProfile();

        // ── Cases Handled & Court Enrollments now live in their own "Courts & Cases" tab ──
        View llCasesSection = practiceView.findViewById(R.id.ll_cases_handled_section);
        View llCourtSection = practiceView.findViewById(R.id.ll_court_section);
        if (llCasesSection != null) llCasesSection.setVisibility(View.GONE);
        if (llCourtSection != null) llCourtSection.setVisibility(View.GONE);

        TextView tvProfHeader = practiceView.findViewById(R.id.tv_professional_info_header);
        if (tvProfHeader != null) tvProfHeader.setText("Professional Information");

        TextView tvAddrHeader = practiceView.findViewById(R.id.tv_address_info_header);
        if (tvAddrHeader != null) tvAddrHeader.setText("Addresses");

        TextView tv_Firmname = practiceView.findViewById(R.id.tv_Firmname);
        if (isFirmProfileMode || isAMMorSuperuser) {
            tv_Firmname.setText("Registration Id");
        } else {
            tv_Firmname.setText(getString(R.string.bar_id));
        }

        TextView tv_nationality = practiceView.findViewById(R.id.tv_nationality);
        tv_nationality.setText(R.string.year_exp);
        TextView tv_pratice = practiceView.findViewById(R.id.tv_pratice);
        tv_pratice.setText(R.string.practice_area);

        TextView tv_lan = practiceView.findViewById(R.id.tv_lan);
        if (isFirmProfileMode) {
            if (tv_lan != null) tv_lan.setVisibility(View.GONE);
        } else if (isAMMorSuperuser) {
            if (tv_lan != null) tv_lan.setVisibility(View.GONE);
        } else {
            if (tv_lan != null) {
                tv_lan.setVisibility(View.VISIBLE);
                tv_lan.setText(R.string.language);
            }
        }

        TextView tv_service = practiceView.findViewById(R.id.tv_service);
        tv_service.setText(R.string.service_offered);
        TextView tv_RegisterAddress = practiceView.findViewById(R.id.tv_RegisterAddress);
        tv_RegisterAddress.setText(R.string.mailing_address);

        // ── CHANGE: Building No → Details ──
        TextView tv_Building = practiceView.findViewById(R.id.tv_Building);
        tv_Building.setText("Details");

        // ── CHANGE: Hide Street in registered address ──
        TextView tv_Street = practiceView.findViewById(R.id.tv_Street);
        if (tv_Street != null) tv_Street.setVisibility(View.GONE);
        View tv_StreetParent = tv_Street != null ? (View) tv_Street.getParent() : null;
        if (tv_StreetParent != null) tv_StreetParent.setVisibility(View.GONE);

        TextView tv_City = practiceView.findViewById(R.id.tv_City);
        tv_City.setText(R.string.city);
        TextView tv_State = practiceView.findViewById(R.id.tv_State);
        tv_State.setText(R.string.state);
        TextView tv_ad_Country = practiceView.findViewById(R.id.tv_ad_Country);
        tv_ad_Country.setText(R.string.country);
        TextView tv_Zip = practiceView.findViewById(R.id.tv_Zip);
        tv_Zip.setText(R.string.zip);
        TextView tv_mailingAddressnNote = practiceView.findViewById(R.id.tv_mailingAddressnNote);
        tv_mailingAddressnNote.setText(R.string.mailing_address_is_same_as_registered_address);
        TextView tv_MailingAddress = practiceView.findViewById(R.id.tv_MailingAddress);
        tv_MailingAddress.setText(R.string.mailing_address);

        // ── CHANGE: Mailing address Building No → Details ──
        TextView mtv_Building = practiceView.findViewById(R.id.mtv_Building);
        mtv_Building.setText("Details");

        // ── CHANGE: Hide Street in mailing address ──
        TextView mtv_Street = practiceView.findViewById(R.id.mtv_Street);
        if (mtv_Street != null) mtv_Street.setVisibility(View.GONE);
        View mtv_StreetParent = mtv_Street != null ? (View) mtv_Street.getParent() : null;
        if (mtv_StreetParent != null) mtv_StreetParent.setVisibility(View.GONE);

        TextView mtv_City = practiceView.findViewById(R.id.mtv_City);
        mtv_City.setText(R.string.city);
        TextView mtv_State = practiceView.findViewById(R.id.mtv_State);
        mtv_State.setText(R.string.state);
        TextView mtv_ad_Country = practiceView.findViewById(R.id.mtv_ad_Country);
        mtv_ad_Country.setText(R.string.country);
        sp_et_ad_country = practiceView.findViewById(R.id.sp_et_ad_country);
        sp_met_ad_country = practiceView.findViewById(R.id.sp_met_ad_country);
        ll_met_ad_country = practiceView.findViewById(R.id.ll_met_ad_country);
        ll_et_ad_country = practiceView.findViewById(R.id.ll_et_ad_country);
        TextView mtv_Zip = practiceView.findViewById(R.id.mtv_Zip);
        mtv_Zip.setText(R.string.zip);

        LinearLayoutCompat ll_register_country = practiceView.findViewById(R.id.ll_register_country);
        ll_register_country.setVisibility(GONE);
        LinearLayoutCompat ll_mailing_country = practiceView.findViewById(R.id.ll_mailing_country);
        ll_mailing_country.setVisibility(GONE);

        sp_practice = practiceView.findViewById(R.id.sp_practice);
        tl_practice_area = practiceView.findViewById(R.id.tl_practice_area);
        etPractice = tl_practice_area.findViewById(R.id.tv_spinner_view);
        etPractice.setHint(R.string.select_practice_areas);
        img_clear_icon = tl_practice_area.findViewById(R.id.img_clear_icon);
        img_dropdown_icon = tl_practice_area.findViewById(R.id.img_dropdown_icon);

        bindFirmPracticeDetails(practiceView);

        if (isFirmProfileMode || isAMMorSuperuser) {
            if (etLanguages != null) {
                etLanguages.setVisibility(View.GONE);
                if (etLanguages.getParent() instanceof View) {
                    ((View) etLanguages.getParent()).setVisibility(View.GONE);
                }
            }
            if (chipLanguages != null) {
                chipLanguages.setVisibility(View.GONE);
                if (chipLanguages.getParent() instanceof View) {
                    ((View) chipLanguages.getParent()).setVisibility(View.GONE);
                }
            }
        }

        et_Building.addTextChangedListener(registeredToMailingWatcher);
        et_Street.addTextChangedListener(registeredToMailingWatcher);
        et_City.addTextChangedListener(registeredToMailingWatcher);
        et_State.addTextChangedListener(registeredToMailingWatcher);
        et_Zip.addTextChangedListener(registeredToMailingWatcher);

        tv_RegisterAddress.setText(R.string.register_address);
        tv_RegisterAddress.setTextSize(DynamicUtils.twenty);
        tv_Building.setText("Details");
        tv_City.setText(R.string.city);
        tv_State.setText(R.string.state);
        tv_ad_Country.setText(R.string.country);
        tv_Zip.setText(R.string.zip);
        tv_mailingAddressnNote.setText(R.string.mailing_address_is_same_as_registered_address);
        tv_MailingAddress.setText(R.string.mailing_address);
        tv_MailingAddress.setTextSize(DynamicUtils.twenty);
        mtv_Building.setText("Details");
        mtv_City.setText(R.string.city);
        mtv_State.setText(R.string.state);
        mtv_ad_Country.setText(R.string.country);
        mtv_Zip.setText(R.string.zip);

        tl_practice_area.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCaseTypeChecked)
                    sp_practice.setVisibility(VISIBLE);
                else sp_practice.setVisibility(GONE);
                isCaseTypeChecked = !isCaseTypeChecked;
            }
        });

        cbYes = practiceView.findViewById(R.id.cb_yes);
        if (cbYes != null) {
            cbYes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    isSameAddressChecked = isChecked;
                    if (isChecked) {
                        copyRegisteredToMailing();
                        setMailingEditable(false);
                        cbYes.setTextColor(Color.WHITE);
                    } else {
                        setMailingEditable(true);
                        cbYes.setTextColor(Color.BLACK);
                    }
                }
            });
            checkIfAddressesMatch();
        }

        if (isFirmProfileMode || isAMMorSuperuser) {
            if (tv_Firmname.getParent() instanceof LinearLayout) {
                LinearLayout parentRow = (LinearLayout) tv_Firmname.getParent();
                if (parentRow.getChildCount() > 1) {
                    parentRow.getChildAt(1).setVisibility(View.GONE);
                }
            }
        }

        llViewScreen.addView(practiceView);
        llViewScreen.setVisibility(VISIBLE);
        scrollToTargetField();
    }

    private void checkIfAddressesMatch() {
        if (cbYes == null) return;

        String regBuilding = et_Building != null && et_Building.getText() != null ? et_Building.getText().toString().trim() : "";
        String regStreet = et_Street != null && et_Street.getText() != null ? et_Street.getText().toString().trim() : "";
        String regCity = et_City != null && et_City.getText() != null ? et_City.getText().toString().trim() : "";
        String regState = et_State != null && et_State.getText() != null ? et_State.getText().toString().trim() : "";
        String regZip = et_Zip != null && et_Zip.getText() != null ? et_Zip.getText().toString().trim() : "";

        String mailBuilding = met_Building != null && met_Building.getText() != null ? met_Building.getText().toString().trim() : "";
        String mailStreet = met_Street != null && met_Street.getText() != null ? met_Street.getText().toString().trim() : "";
        String mailCity = met_City != null && met_City.getText() != null ? met_City.getText().toString().trim() : "";
        String mailState = met_State != null && met_State.getText() != null ? met_State.getText().toString().trim() : "";
        String mailZip = met_Zip != null && met_Zip.getText() != null ? met_Zip.getText().toString().trim() : "";

        boolean allFieldsPresent = !regBuilding.isEmpty() && !regCity.isEmpty();
        boolean addressesMatch =
                regBuilding.equalsIgnoreCase(mailBuilding) &&
                        regStreet.equalsIgnoreCase(mailStreet) &&
                        regCity.equalsIgnoreCase(mailCity) &&
                        regState.equalsIgnoreCase(mailState) &&
                        regZip.equalsIgnoreCase(mailZip);

        if (allFieldsPresent && addressesMatch) {
            // ── Remove listener before programmatic check to avoid side-effects ──
            cbYes.setOnCheckedChangeListener(null);
            isSameAddressChecked = true;
            cbYes.setChecked(true);
            cbYes.setTextColor(Color.WHITE);
            setMailingEditable(false);

            // ── Re-attach listener after ──────────────────────────────────────────
            cbYes.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isSameAddressChecked = isChecked;
                if (isChecked) {
                    copyRegisteredToMailing();
                    setMailingEditable(false);
                    cbYes.setTextColor(Color.WHITE);
                } else {
                    setMailingEditable(true);
                    cbYes.setTextColor(Color.BLACK);
                }
            });
        }
    }

    private void updateCourtSectionState(@Nullable TextView tvAddCourt,
                                         @Nullable TextView tvWarning,
                                         @Nullable ChipGroup chipGroupPractice) {
        if (tvAddCourt == null) return;

        // Use the fragment-level chipPractice if already assigned by
        // bindFirmPracticeDetails(), otherwise fall back to the locally
        // looked-up chipGroupPractice from practiceView.
        ChipGroup source = (chipPractice != null) ? chipPractice : chipGroupPractice;

        boolean hasPracticeAreas = false;
        if (source != null && source.getChildCount() > 0) {
            JSONArray vals = getChipValuesFromGroup(source);
            hasPracticeAreas = vals.length() > 0;
        }

        if (hasPracticeAreas) {
            tvAddCourt.setEnabled(true);
            tvAddCourt.setClickable(true);
            tvAddCourt.setAlpha(1f);
            if (tvWarning != null) tvWarning.setVisibility(View.GONE);
        } else {
            tvAddCourt.setEnabled(false);
            tvAddCourt.setClickable(false);
            tvAddCourt.setAlpha(0.4f);
            // Warning is shown reactively only — not on passive state updates
        }
    }

    /**
     * if (llCourtSection != null)
     * Enables or disables the "Add Court" button based on whether at least one
     * practice area chip is present. Also shows/hides the warning label.
     *
     * @param tvAddCourt the "Add Court" TextView button
     * @param tvWarning  the warning TextView (may be null)
     */
    private void updateCourtSectionState(@Nullable TextView tvAddCourt,
                                         @Nullable TextView tvWarning) {
        if (tvAddCourt == null) return;
        boolean hasPracticeAreas = !getCurrentPracticeAreaValues().isEmpty();

        if (hasPracticeAreas) {
            tvAddCourt.setEnabled(true);
            tvAddCourt.setAlpha(1f);
            tvAddCourt.setClickable(true);
            if (tvWarning != null) tvWarning.setVisibility(View.GONE);
        } else {
            tvAddCourt.setEnabled(false);
            tvAddCourt.setAlpha(0.4f);
            tvAddCourt.setClickable(false);
            // Don't show the warning on initial load — only show it when the
            // user actually taps the disabled button (handled in onClick above).
            // We do hide it here in case it was previously visible and the user
            // then removed all chips.
            if (tvWarning != null) tvWarning.setVisibility(View.GONE);
        }
    }

    private View createAwardsOnlyEdit() {
        llViewScreen.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View detailView = inflater.inflate(R.layout.educational_edit_view, llViewScreen, false);

        View eduSection = detailView.findViewById(R.id.ll_education);
        View certSection = detailView.findViewById(R.id.ll_certification);
        TextView tvEduTitle = detailView.findViewById(R.id.tv_title_education);
        TextView tvCertTitle = detailView.findViewById(R.id.tv_title_certification);
        TextView tv_add_education = detailView.findViewById(R.id.tv_add_education);
        TextView tv_add_certification = detailView.findViewById(R.id.tv_add_certification);

        if (eduSection != null) eduSection.setVisibility(View.GONE);
        if (certSection != null) certSection.setVisibility(View.GONE);
        if (tvEduTitle != null) tvEduTitle.setVisibility(View.GONE);
        if (tvCertTitle != null) tvCertTitle.setVisibility(View.GONE);
        if (tv_add_education != null) tv_add_education.setVisibility(View.GONE);
        if (tv_add_certification != null) tv_add_certification.setVisibility(View.GONE);

        llAwards = detailView.findViewById(R.id.ll_awards);
        TextView tvAwardsTitle = detailView.findViewById(R.id.tv_title_awards);
        TextView tv_add_awards = detailView.findViewById(R.id.tv_add_awards);

        tvAwardsTitle.setText("Awards & Recognition");
        tv_add_awards.setText(R.string._add_more);

        tv_add_awards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem(inflater, llAwards, tvAwardsTitle, "", "", "", TYPE_AWARD);
            }
        });

        FirmProfileModel.Profile profile =
                firmProfileModel != null && firmProfileModel.getData() != null ?
                        firmProfileModel.getData().getProfile() : null;

        if (profile == null) {
            llViewScreen.setVisibility(View.GONE);
            return detailView;
        }

        FirmProfileModel.Firm firm = profile.getFirm();
        loadItems(inflater, llAwards, tvAwardsTitle, firm.getAwards(), TYPE_AWARD);

        llViewScreen.addView(detailView);
        llViewScreen.setVisibility(VISIBLE);
        scrollToTargetField();
        return detailView;
    }

    private void scrollToTargetField() {
        final String targetId = Constants.TARGET_FIELD;
        if (TextUtils.isEmpty(targetId)) return;
        Constants.TARGET_FIELD = "";
        View root = getView();
        if (root == null) return;
        root.post(new Runnable() {
            @Override
            public void run() {
                View target = findViewByStringId(root, targetId);
                if (target == null && llViewScreen != null && llViewScreen.getChildCount() > 0) {
                    target = findViewByStringId(llViewScreen.getChildAt(0), targetId);
                }
                if (target == null) {
                    Log.w("SCROLL_TARGET", "View not found for id: " + targetId);
                    return;
                }
                final View found = target;
                ScrollView scrollView = findParentScrollView(found);
                if (scrollView == null) {
                    Log.w("SCROLL_TARGET", "No ScrollView parent for: " + targetId);
                    return;
                }
                int[] targetPos = new int[2];
                int[] scrollPos = new int[2];
                found.getLocationOnScreen(targetPos);
                scrollView.getLocationOnScreen(scrollPos);
                int scrollTo = scrollView.getScrollY() + (targetPos[1] - scrollPos[1]) - 80;
                scrollView.smoothScrollTo(0, Math.max(0, scrollTo));
                flashHighlight(found);
                requestFocusOnField(found);
            }
        });
    }

    private View findViewByStringId(View parent, String fieldId) {
        if (parent == null || TextUtils.isEmpty(fieldId)) return null;
        try {
            int resId = getResources().getIdentifier(fieldId, "id", requireActivity().getPackageName());
            if (resId != 0) {
                View v = parent.findViewById(resId);
                if (v != null) return v;
            }
        } catch (Exception e) {
            Log.w("SCROLL_TARGET", "getIdentifier failed for: " + fieldId);
        }
        if (parent instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) parent;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View result = findViewByStringId(vg.getChildAt(i), fieldId);
                if (result != null) return result;
            }
        }
        return null;
    }

    private ScrollView findParentScrollView(View view) {
        android.view.ViewParent parent = view.getParent();
        while (parent != null) {
            if (parent instanceof ScrollView) return (ScrollView) parent;
            parent = parent.getParent();
        }
        return null;
    }

    private void flashHighlight(final View view) {
        if (view == null || !isAdded()) return;
        final android.graphics.drawable.Drawable originalBg = view.getBackground();
        view.setBackgroundColor(requireContext().getResources().getColor(R.color.blue_pale));
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) return;
                view.animate()
                        .alpha(0.5f)
                        .setDuration(150)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                if (!isAdded()) return;
                                view.setBackground(originalBg);
                                view.setAlpha(1f);
                            }
                        })
                        .start();
            }
        }, 300);
    }


    private void copyRegisteredToMailing() {
        if (et_Building != null && met_Building != null)
            met_Building.setText(et_Building.getText());
        if (et_Street != null && met_Street != null)
            met_Street.setText(et_Street.getText());
        if (et_City != null && met_City != null)
            met_City.setText(et_City.getText());
        if (et_State != null && met_State != null)
            met_State.setText(et_State.getText());
        if (et_Zip != null && met_Zip != null)
            met_Zip.setText(et_Zip.getText());
        if (sp_et_ad_country != null && sp_met_ad_country != null)
            met_ad_Country.setText(et_ad_Country.getText().toString());
    }

    private final TextWatcher registeredToMailingWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (isSameAddressChecked) {
                copyRegisteredToMailing();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private JSONArray toJsonArray(List<String> list) {
        JSONArray array = new JSONArray();
        for (String item : list) {
            array.put(item);
        }
        return array;
    }

    private JSONArray getChipValuesFromGroup(ViewGroup chipGroup) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            TextView tv = child.findViewById(R.id.tv_chip_text);
            if (tv != null) {
                String val = tv.getText().toString().trim();
                if (!val.isEmpty()) array.put(val);
            }
        }
        return array;
    }

    private void setupChipInput(TextInputEditText editText, ChipGroup chipGroup) {
        if (editText == null || chipGroup == null) return;
        editText.setOnEditorActionListener((v, actionId, event) -> {
            boolean isEnter =
                    actionId == EditorInfo.IME_ACTION_DONE ||
                            actionId == EditorInfo.IME_ACTION_NEXT ||
                            (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                                    && event.getAction() == KeyEvent.ACTION_DOWN);
            if (!isEnter) return false;
            String text = editText.getText() != null ? editText.getText().toString().trim() : "";
            if (text.isEmpty()) return true;
            addChip(chipGroup, text);
            editText.setText("");
            return true;
        });
    }

    /// ..
// ── Practice area chip ─────────────────────────────────────────────────
    private void addChip(ChipGroup chipGroup, String text) {
        boolean showEditIcon = chipGroup.getId() == R.id.chipGroupCasesHandled;

        EditableChipHelper.addChip(
                chipGroup.getContext(),
                chipGroup,
                text,
                showEditIcon,
                newText -> {
                    if (chipGroup.getId() == R.id.chipGroupCasesHandled) {
                        View practiceView = llViewScreen.getChildAt(0);
                        if (practiceView != null) {
                            TextInputEditText etCases = practiceView.findViewById(R.id.et_cases_handled);
                            if (etCases != null) {
                                etCases.setText(newText);
                                etCases.setSelection(etCases.getText() != null ? etCases.getText().length() : 0);
                                etCases.requestFocus();
                            }
                        }
                    } else if (chipGroup.getId() == R.id.chipGroupPractice && practiceAreaAdapter != null) {
                        practiceAreaAdapter.renameItem(text, newText);
                    }
                },
                () -> {
                    if (chipGroup.getId() == R.id.chipGroupPractice && practiceAreaAdapter != null) {
                        practiceAreaAdapter.unselectItem(text);
                    }
                    if (chipGroup.getId() == R.id.chipGroupPractice) {
                        List<String> currentPA = getCurrentPracticeAreaValues();
                        fetchSuggestedServices(currentPA);
                        syncCourtSectionFromPracticeChange(); // ← add this
                    }
                }
        );

        // ── Trigger services refresh on practice area chip addition ──
        // ── Trigger services + court refresh on practice area chip addition ──
        if (chipGroup.getId() == R.id.chipGroupPractice) {
            List<String> currentPA = getCurrentPracticeAreaValues();
            fetchSuggestedServices(currentPA);
            syncCourtSectionFromPracticeChange(); // ← add this
        }
    }

    // ── setChips: pre-fill an entire ChipGroup from a JSONArray ───────────
    private void setChips(ChipGroup chipGroup, JSONArray values) {
        if (chipGroup == null || values == null || values.length() == 0) return;
        chipGroup.removeAllViews();
        for (int i = 0; i < values.length(); i++) {
            String value = values.optString(i, "").trim();
            if (value.isEmpty()) continue;
            addChip(chipGroup, value);          // uses the overload above ↑
        }
    }

    ///

    private String getFullAddress(FirmProfileModel.Address address) {
        if (address == null) return "-";
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(address.getHouse_flat_no()))
            sb.append(address.getHouse_flat_no()).append(", ");
        if (!TextUtils.isEmpty(address.getStreet()))
            sb.append(address.getStreet()).append(", ");
        if (!TextUtils.isEmpty(address.getCity_town()))
            sb.append(address.getCity_town()).append(", ");
        if (!TextUtils.isEmpty(address.getState()))
            sb.append(address.getState()).append(", ");
        if (!TextUtils.isEmpty(address.getCountry()))
            sb.append(address.getCountry()).append(" ");
        if (!TextUtils.isEmpty(address.getZipcode()))
            sb.append(address.getZipcode());
        return sb.toString().trim();
    }

    public void callCountriesWebService() {
        JSONObject jsonData = new JSONObject();
        try {
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "countries", "COUNTRIES", jsonData.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void clearDetails() {
        liveChipPracticeAreas = null;
        liveChipLanguages = null;
        liveChipServices = null;
        et_Firmname.setText("");
        et_Country.setText("");
        et_Email.setText("");
        et_contactName.setText("");
        et_ContactPhone.setText("");
        et_Website.setText("");
        et_DefaultCurrency.setText("");
        et_Building.setText("");
        et_City.setText("");
        et_Street.setText("");
        et_State.setText("");
        et_ad_Country.setText("");
        et_Zip.setText("");
        met_Building.setText("");
        met_City.setText("");
        met_Street.setText("");
        met_State.setText("");
        met_ad_Country.setText("");
        met_Zip.setText("");
        Constants.Edit_OR_View = "View";
        Constants.issubscription = false;
//        Constants.PROFILE_EDIT_TAB = "practice_details";
        firmProfile.ChangeBackGround();
        firmProfile.NavFragment(PracticePartnerView.newInstance(firmProfile));
        boolean fp = isFirmProfile();
        if (fp) {
            mViewModel.setData("Firm Profile");
        } else {
            mViewModel.setData(getResources().getString(R.string.my_profile));
        }
    }

    private void refreshCourtEnrollmentRows() {
        if (llCourtEnrollments == null) return;

        // Capture existing data before clearing
        List<String[]> existingRows = new ArrayList<>();
        for (int i = 0; i < llCourtEnrollments.getChildCount(); i++) {
            View row = llCourtEnrollments.getChildAt(i);

            // ── Court Type ────────────────────────────────────────────────────────
            LinearLayout llCt = row.findViewById(R.id.ll_court_type);
            TextView tvCt = llCt != null ? llCt.findViewById(R.id.tv_spinner_view) : null;
            String courtType = tvCt != null ? tvCt.getText().toString().trim() : "";

            // ── State ─────────────────────────────────────────────────────────────
            LinearLayout llStateSpin = row.findViewById(R.id.ll_state_spinner);
            TextView tvState = llStateSpin != null ? llStateSpin.findViewById(R.id.tv_spinner_view) : null;
            String state = (tvState != null && !TextUtils.isEmpty(tvState.getText()))
                    ? tvState.getText().toString().trim() : "";

            // ── City - Get from TextInputEditText ─────────────────────────────────
            TextInputEditText etCitySearch = row.findViewById(R.id.et_city_search);
            String city = (etCitySearch != null && !TextUtils.isEmpty(etCitySearch.getText()))
                    ? etCitySearch.getText().toString().trim() : "";

            // ── Convert display format back to API format for re-rendering ────────
            existingRows.add(new String[]{
                    convertCourtTypeToApiFormat(courtType), state, city, "" // courtName not used
            });
        }

        // Re-render all rows with fresh API data
        llCourtEnrollments.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (String[] row : existingRows) {
            addCourtEnrollmentRow(inflater, llCourtEnrollments, row[0], row[1], row[2], row[3]);
        }
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progress_dialog != null && progress_dialog.isShowing())
            AndroidUtils.dismiss_dialog(progress_dialog);

        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                Log.d("Request_Type", httpResult.getRequestType());

                if (Objects.equals(httpResult.getRequestType(), "Update_Bp")) {
                    boolean iserror = result.optBoolean("error");
                    if (!iserror) {
                        // ── Parse the success message ─────────────────────────
                        JSONObject data = result.optJSONObject("data");
                        if (data != null) {
                            String msg = (data != null)
                                    ? data.optString("msg", "")
                                    : result.optString("msg", "");
                            AndroidUtils.showAlert_docs("Success !", msg, getActivity());
                        }

                        // ── Re-confirm Constants.NAME from the EditText so that
                        //    even if the user edited again before the response
                        //    came back, we capture the correct current value ────
                        boolean fp = isFirmProfile();

                        if (et_contactName != null && et_contactName.getText() != null) {
                            String confirmedName = et_contactName.getText().toString().trim();
                            if (!TextUtils.isEmpty(confirmedName)) {
                                Constants.NAME = confirmedName;
                                Constants.ContactName = confirmedName;
                            }
                        }

                        if (et_Firmname != null && et_Firmname.getText() != null) {
                            String confirmedFirmName = et_Firmname.getText().toString().trim();
                            if (!TextUtils.isEmpty(confirmedFirmName)) {
                                Constants.FIRM_NAME = confirmedFirmName;
                            }
                        }

                        // ── Update person_icon in this fragment ───────────────
                        if (person_icon != null) {
                            String initial;
                            if (!TextUtils.isEmpty(Constants.NAME)) {
                                initial = Constants.NAME.substring(0, 1).toUpperCase();
                            } else if (!TextUtils.isEmpty(Constants.FIRM_NAME)) {
                                initial = Constants.FIRM_NAME.substring(0, 1).toUpperCase();
                            } else {
                                initial = "?";
                            }
                            person_icon.setText(initial);
                        }

                        // ── Persist confirmed values to SharedPreferences ─────
                        updateCachedUserData(Constants.NAME, Constants.FIRM_NAME);

                        // ── Push confirmed initial to MainActivity header ──────
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).updateProfileInitial();
                        }

//                        Constants.PROFILE_EDIT_TAB = "practice_details";
                        clearDetails();

                    } else {
                        // ── API returned an error ─────────────────────────────
                        if (result.has("errors")) {
                            JSONArray errors = result.optJSONArray("errors");
                            if (errors != null && errors.length() > 0) {
                                JSONObject err = errors.optJSONObject(0);
                                String field = err != null ? err.optString("field", "") : "";
                                String errMsg = err != null ? err.optString("msg", "") : "";
                                AndroidUtils.showAlert(field + " : " + errMsg, getActivity());
                            }
                        } else {
                            AndroidUtils.showAlert(result.optString("msg", ""), getActivity());
                        }
                    }

                    // ── Always sync ContactName regardless of error/success ───
                    if (et_contactName != null && et_contactName.getText() != null) {
                        Constants.ContactName = et_contactName.getText().toString().trim();
                    }

                } else if (Objects.equals(httpResult.getRequestType(), "Generate_Bio")) {
                    boolean iserror = result.optBoolean("error");
                    if (!iserror) {
                        JSONObject data = result.optJSONObject("data");
                        if (data != null) {
                            String bio = data.optString("bio", "");
                            if (et_bio != null) {
                                et_bio.setText("");
                                et_bio.setText(bio);
                                et_bio.requestFocus();
                                et_bio.setSelection(et_bio.getText() != null ? et_bio.getText().length() : 0);
                            }
                        }
                    } else {
                        if (result.has("errors")) {
                            JSONArray errors = result.optJSONArray("errors");
                            if (errors != null && errors.length() > 0) {
                                StringBuilder fieldNames = new StringBuilder();
                                for (int i = 0; i < errors.length(); i++) {
                                    JSONObject err = errors.optJSONObject(i);
                                    if (err != null) {
                                        String field = err.optString("field", "");
                                        if (!field.isEmpty()) {
                                            if (fieldNames.length() > 0) fieldNames.append(", ");
                                            fieldNames.append(formatFieldName(field));
                                        }
                                    }
                                }
                                String message = "Please fill in the necessary fields: " + fieldNames;
                                AndroidUtils.showAlert(message, getActivity());
                            }
                        } else {
                            AndroidUtils.showAlert(result.optString("msg", "Failed to generate bio"), getActivity());
                        }
                    }
                } else if (Objects.equals(httpResult.getRequestType(), "COURT_STATES")) {
                    boolean iserror = result.optBoolean("error");
                    if (!iserror) {
                        JSONObject dataObj = result.optJSONObject("data");
                        if (dataObj != null) {
                            // Build statesCitiesMap: state -> list of cities
                            courtStatesMap.clear();
                            courtStateList.clear();
                            JSONArray statesArray = dataObj.optJSONArray("states");
                            if (statesArray != null) {
                                for (int i = 0; i < statesArray.length(); i++) {
                                    JSONObject stObj = statesArray.optJSONObject(i);
                                    if (stObj == null) continue;
                                    String stName = stObj.optString("state", "").trim();
                                    JSONArray citiesArr = stObj.optJSONArray("cities");
                                    ArrayList<String> cities = new ArrayList<>();
                                    if (citiesArr != null) {
                                        for (int j = 0; j < citiesArr.length(); j++) {
                                            String c = citiesArr.optString(j, "").trim();
                                            if (!c.isEmpty()) cities.add(c);
                                        }
                                    }
                                    if (!stName.isEmpty()) {
                                        courtStatesMap.put(stName, cities);
                                        courtStateList.add(stName);
                                    }
                                }
                            }
                            Collections.sort(courtStateList);
                        }
                    }
                    Runnable cb = pendingCourtStatesCallback;
                    pendingCourtStatesCallback = null;
                    if (cb != null && isAdded()) requireActivity().runOnUiThread(cb);

                } else if (Objects.equals(httpResult.getRequestType(), "COURT_SUGGEST")) {
                    boolean iserror = result.optBoolean("error");
                    suggestedCourtTypesList.clear();
                    allCourtTypesList.clear();
                    if (!iserror) {
                        JSONObject dataObj = result.optJSONObject("data");
                        if (dataObj != null) {
                            JSONArray suggestions = dataObj.optJSONArray("court_suggestions");
                            if (suggestions != null) {
                                for (int i = 0; i < suggestions.length(); i++) {
                                    String s = suggestions.optString(i, "").trim();
                                    if (!s.isEmpty()) suggestedCourtTypesList.add(s);
                                }
                            }
                            JSONArray allCourts = dataObj.optJSONArray("all_courts");
                            if (allCourts != null) {
                                for (int i = 0; i < allCourts.length(); i++) {
                                    String s = allCourts.optString(i, "").trim();
                                    if (!s.isEmpty()) allCourtTypesList.add(s);
                                }
                            }
                        }
                    }
                    Runnable cb = pendingCourtSuggestCallback;
                    pendingCourtSuggestCallback = null;
                    if (cb != null && isAdded()) requireActivity().runOnUiThread(cb);
                } else if (Objects.equals(httpResult.getRequestType(), "SERVICES_SUGGEST")) {
                    boolean iserror = result.optBoolean("error");
                    if (!iserror) {
                        JSONObject data = result.optJSONObject("data");
                        suggestedServicesList.clear();
                        allServicesList.clear();
                        if (data != null) {
                            JSONArray suggested = data.optJSONArray("service_suggestions");
                            if (suggested != null) {
                                for (int i = 0; i < suggested.length(); i++) {
                                    String s = suggested.optString(i, "").trim();
                                    if (!s.isEmpty()) suggestedServicesList.add(s);
                                }
                            }
                            JSONArray allArr = data.optJSONArray("all_services");
                            if (allArr != null) {
                                for (int i = 0; i < allArr.length(); i++) {
                                    String s = allArr.optString(i, "").trim();
                                    if (!s.isEmpty()) allServicesList.add(s);
                                }
                            }
                        }
                    }

                    // Refresh the services adapter if it exists
                    if (rvServicesList != null && rvServicesList.getAdapter() != null) {
                        CommonMultiSelectionAdapter adapter =
                                (CommonMultiSelectionAdapter) rvServicesList.getAdapter();

                        View practiceView = llViewScreen != null && llViewScreen.getChildCount() > 0
                                ? llViewScreen.getChildAt(0) : null;
                        TextInputEditText etSearch = practiceView != null
                                ? practiceView.findViewById(R.id.et_services_search) : null;

                        refreshServicesAdapter(etSearch, adapter, imgServicesDrop);
                    }
                    if (pendingSuggestCallback != null) {
                        Runnable cb = pendingSuggestCallback;
                        pendingSuggestCallback = null;
                        if (isAdded()) requireActivity().runOnUiThread(cb);
                    }
                    rvServicesList.setVisibility(View.GONE);
                    isServicesDropdownOpen = false;
                    if (imgServicesDrop != null)
                        imgServicesDrop.animate().rotation(0).setDuration(200).start();
                    List<String> currentPA = getCurrentPracticeAreaValues();
                    callCourtSuggestApi(currentPA, () -> {
                        // Court types refreshed — rebuild any open court type dropdowns
                        if (isAdded() && llCourtEnrollments != null
                                && llCourtEnrollments.getChildCount() > 0) {
                            refreshCourtEnrollmentRows();
                        }
                    });
                } else if (Objects.equals(httpResult.getRequestType(), "SERVICES_SEARCH")) {
                    boolean iserror = result.optBoolean("error");
                    searchedServicesList.clear();
                    if (!iserror) {
                        JSONObject data = result.optJSONObject("data");
                        if (data != null) {
                            JSONArray arr = data.optJSONArray("services");
                            if (arr != null) {
                                for (int i = 0; i < arr.length(); i++) {
                                    String s = arr.optString(i, "").trim();
                                    if (!s.isEmpty()) searchedServicesList.add(s);
                                }
                            }
                        }
                    }
                    if (pendingSearchCallback != null) {
                        Runnable cb = pendingSearchCallback;
                        pendingSearchCallback = null;
                        if (isAdded()) requireActivity().runOnUiThread(cb);
                    }
                } else if (Objects.equals(httpResult.getRequestType(), "HIGH_COURTS")) {
                    boolean iserror = result.optBoolean("error");
                    if (!iserror) {
                        JSONArray dataArray = result.getJSONArray("data");
                        highCourtsList.clear();
                        highCourtStateList.clear();
                        highCourtCitiesMap.clear();
                        highCourtNameMap.clear();

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject courtObj = dataArray.getJSONObject(i);
                            CourtData court = new CourtData();
                            court.setId(courtObj.optString("id"));
                            court.setName(courtObj.optString("name"));
                            court.setCourt_type(courtObj.optString("court_type"));
                            court.setCity(courtObj.optString("city"));
                            court.setState(courtObj.optString("state"));

                            JSONArray jurisdictionArray = courtObj.optJSONArray("jurisdiction");
                            if (jurisdictionArray != null) {
                                ArrayList<String> jurisdiction = new ArrayList<>();
                                for (int j = 0; j < jurisdictionArray.length(); j++) {
                                    jurisdiction.add(jurisdictionArray.getString(j));
                                }
                                court.setJurisdiction(jurisdiction);
                            }

                            JSONArray benchesArray = courtObj.optJSONArray("benches");
                            if (benchesArray != null) {
                                ArrayList<String> benches = new ArrayList<>();
                                for (int j = 0; j < benchesArray.length(); j++) {
                                    benches.add(benchesArray.getString(j));
                                }
                                court.setBenches(benches);
                            }

                            highCourtsList.add(court);

                            // ── Pre-build maps ────────────────────────────────────────
                            String st = court.getState();
                            if (!TextUtils.isEmpty(st)) {
                                // State list
                                if (!highCourtStateList.contains(st)) {
                                    highCourtStateList.add(st);
                                }

                                // Cities map: principal seat + benches
                                ArrayList<String> cities = new ArrayList<>();
                                if (!TextUtils.isEmpty(court.getCity())) {
                                    cities.add(court.getCity());
                                }
                                if (court.getBenches() != null) {
                                    for (String bench : court.getBenches()) {
                                        if (!TextUtils.isEmpty(bench) && !cities.contains(bench)) {
                                            cities.add(bench);
                                        }
                                    }
                                }
                                highCourtCitiesMap.put(st, cities);

                                // Court name map
                                if (!TextUtils.isEmpty(court.getName())) {
                                    highCourtNameMap.put(st, court.getName());
                                }
                            }
                        }
                        Collections.sort(highCourtStateList);
                        Log.d("Courts", "Loaded " + highCourtsList.size() + " high courts, "
                                + highCourtStateList.size() + " states");
                    }
                    callSupremeCourt();
                } else if (Objects.equals(httpResult.getRequestType(), "SUPREME_COURT")) {
                    boolean iserror = result.optBoolean("error");
                    if (!iserror) {
                        JSONObject dataObj = result.getJSONObject("data");
                        supremeCourtData = new CourtData();
                        supremeCourtData.setId(dataObj.optString("id"));
                        supremeCourtData.setName(dataObj.optString("name"));
                        supremeCourtData.setCourt_type(dataObj.optString("court_type"));
                        supremeCourtData.setCity(dataObj.optString("city"));
                        supremeCourtData.setState(dataObj.optString("state"));

                        Log.d("Courts", "Loaded Supreme Court: " + supremeCourtData.getName());
                        refreshCourtEnrollmentRows();
                    }

                } else if (httpResult.getRequestType().equals("Case Type")) {
                    if (!result.getBoolean("error")) {
                        JSONArray jsonArray = result.getJSONArray("data");
                        caseTypeList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            caseTypeList.add(jsonArray.getString(i));
                        }
                        if (!caseTypeList.isEmpty()) loadCaseTypes();
                    } else {
                        AndroidUtils.showAlert(result.getString("msg"), getActivity(), "");
                    }
                    List<String> currentPA = getCurrentPracticeAreaValues();
                    fetchSuggestedServices(currentPA);
                } else if (Objects.equals(httpResult.getRequestType(), "COUNTRIES")) {
                    JSONArray jsonArray = (new JSONObject(result.getString("data")))
                            .getJSONArray("countries");
                    CountriesDO countriesDO;
                    countriesList.clear();
                    for (int i = 1; i < jsonArray.length(); i++) {
                        countriesDO = new CountriesDO();
                        countriesDO.setName(String.valueOf(jsonArray.getJSONArray(i).get(1)));
                        countriesDO.setValue(String.valueOf(jsonArray.getJSONArray(i).get(0)));
                        countriesList.add(countriesDO);
                    }
                    loadCountryData();
                    callCaseType();

                } else if (Objects.equals(httpResult.getRequestType(), "Profile_upload")) {
                    boolean iserror = result.optBoolean("error");
                    AndroidUtils.showAlert(result.optString("msg"), getActivity());
                    if (!iserror) {
                        // Reload profile image after successful upload
                        profile();
                    }

                } else if (Objects.equals(httpResult.getRequestType(), "Profile_delete")) {
                    boolean iserror = result.optBoolean("error");
                    AndroidUtils.showAlert(result.optString("msg"), getActivity());
                    if (!iserror) {
                        // Clear the profile image and reset person_icon
                        Constants.firm_image = "";
                        if (iv_profile != null) {
                            iv_profile.setImageResource(R.drawable.ic_profile_placeholder);
                            iv_profile.setVisibility(View.GONE);
                        }
                        if (iv_delete_circle != null) {
                            iv_delete_circle.setVisibility(View.GONE);
                        }
                        if (person_icon != null) {
                            person_icon.setVisibility(View.VISIBLE);
                            String initial;
                            if (!TextUtils.isEmpty(Constants.NAME)) {
                                initial = Constants.NAME.substring(0, 1).toUpperCase();
                            } else if (!TextUtils.isEmpty(Constants.FIRM_NAME)) {
                                initial = Constants.FIRM_NAME.substring(0, 1).toUpperCase();
                            } else {
                                initial = "?";
                            }
                            person_icon.setText(initial);
                        }
                    }

                } else if (Objects.equals(httpResult.getRequestType(), "Profile")) {
                    boolean iserror = result.optBoolean("error");
                    if (!iserror) {
                        JSONObject d = result.optJSONObject("data");
                        if (d != null) {
                            String url = d.optString("imageUrl", "");
                            if (!TextUtils.isEmpty(url)) {
                                Constants.firm_image = url;
                                AppImageCache.preload(requireContext(), url);
                                AndroidUtils.loadProfileImage(
                                        requireContext(), url, iv_profile, person_icon);
                                if (iv_delete_circle != null)
                                    iv_delete_circle.setVisibility(View.VISIBLE);
                            } else {
                                if (iv_delete_circle != null)
                                    iv_delete_circle.setVisibility(View.GONE);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String formatFieldName(String field) {
        if (field == null || field.isEmpty()) return field;
        String[] words = field.split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }
        return sb.toString();
    }

    private void callCourtStatesApi(Runnable onComplete) {
        // If already loaded, run callback immediately
        if (!courtStateList.isEmpty()) {
            if (onComplete != null && isAdded()) requireActivity().runOnUiThread(onComplete);
            return;
        }
        pendingCourtStatesCallback = onComplete;
        try {
            WebServiceHelper.callHttpWebService(
                    this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/court/states",
                    "COURT_STATES",
                    new JSONObject().toString()
            );
        } catch (Exception e) {
            e.printStackTrace();
            if (onComplete != null && isAdded()) requireActivity().runOnUiThread(onComplete);
        }
    }

    private void GenerateBio() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            boolean fp = isFirmProfile();
            FirmProfileModel.Profile profile = firmProfileModel.getData().getProfile();
            FirmProfileModel.Firm firm = profile.getFirm();

            JSONObject payload = new JSONObject();

            // ── name ─────────────────────────────────────────────────────────────
            String name = fp ? firm.getFullname() : profile.getName();
            if (!TextUtils.isEmpty(name)) payload.put("name", name);

            // ── gender ───────────────────────────────────────────────────────────
            if (!fp) {
                String genderVal = met_ad_gender != null
                        ? met_ad_gender.getText().toString().trim().toLowerCase(Locale.ROOT)
                        : (profile.getGender() != null ? profile.getGender() : "");
                if (!TextUtils.isEmpty(genderVal)) payload.put("gender", genderVal);
            }

            // ── city (flat field) ─────────────────────────────────────────────────
            String cityVal = et_City != null && et_City.getText() != null
                    ? et_City.getText().toString().trim() : "";
            if (TextUtils.isEmpty(cityVal) && firm.getAddress() != null
                    && firm.getAddress().getCity_town() != null) {
                cityVal = firm.getAddress().getCity_town();
            }
            if (!TextUtils.isEmpty(cityVal)) payload.put("city", cityVal);

            // ── practice_areas ────────────────────────────────────────────────────
            JSONArray practiceArr = chipPractice != null && chipPractice.getChildCount() > 0
                    ? getChipValuesFromGroup(chipPractice)
                    : (fp ? firm.getPractice_areas() : profile.getPractice_areas());
            if (practiceArr != null && practiceArr.length() > 0)
                payload.put("practice_areas", practiceArr);

            // ── services_offered ──────────────────────────────────────────────────
            JSONArray servicesArr = chipServices != null && chipServices.getChildCount() > 0
                    ? getChipValuesFromGroup(chipServices)
                    : (fp ? firm.getServices_offered() : profile.getServices_offered());
            if (servicesArr != null && servicesArr.length() > 0)
                payload.put("services_offered", servicesArr);

            // ── years_of_experience ───────────────────────────────────────────────
            String expStr = etExperience != null && etExperience.getText() != null
                    ? etExperience.getText().toString().trim() : "";
            if (!TextUtils.isEmpty(expStr)) {
                payload.put("years_of_experience", Integer.parseInt(expStr));
            } else {
                int exp = fp ? firm.getYears_of_incorporation() : profile.getYears_of_experience();
                if (exp > 0) payload.put("years_of_experience", exp);
            }

            // ── languages_spoken (My Profile only) ───────────────────────────────
            if (!fp) {
                JSONArray langsArr = chipLanguages != null && chipLanguages.getChildCount() > 0
                        ? getChipValuesFromGroup(chipLanguages)
                        : profile.getLanguages_spoken();
                if (langsArr != null && langsArr.length() > 0)
                    payload.put("languages_spoken", langsArr);
            }

            // ── cases_handled (My Profile only) ──────────────────────────────────
            if (!fp) {
                JSONArray casesArr = chipCasesHandled != null && chipCasesHandled.getChildCount() > 0
                        ? getChipValuesFromGroup(chipCasesHandled)
                        : null;
                if (casesArr == null || casesArr.length() == 0) {
                    List<String> cases = profile.getCases_handled();
                    if (cases != null && !cases.isEmpty()) {
                        casesArr = new JSONArray();
                        for (String c : cases) casesArr.put(c);
                    }
                }
                if (casesArr != null && casesArr.length() > 0)
                    payload.put("cases_handled", casesArr);
            }

            // ── education (My Profile only) ───────────────────────────────────────
            if (!fp) {
                JSONArray eduArr = llEducation != null
                        ? getEducationJson(llEducation)
                        : getEducationJsonFromModel(profile.getEducation());
                if (eduArr != null && eduArr.length() > 0)
                    payload.put("education", eduArr);
            }

            // ── certifications (My Profile only) ─────────────────────────────────
            if (!fp) {
                JSONArray certArr = llCertification != null
                        ? getCertificationJson(llCertification)
                        : getCertificationJsonFromModel(profile.getCertifications());
                if (certArr != null && certArr.length() > 0)
                    payload.put("certifications", certArr);
            }

            // ── awards ────────────────────────────────────────────────────────────
            JSONArray awardsArr = llAwards != null
                    ? getAwardsJson(llAwards)
                    : (fp
                    ? getAwardsJsonFromModel(firm.getAwards())
                    : getAwardsJsonFromModel(profile.getAwards()));
            if (awardsArr != null && awardsArr.length() > 0)
                payload.put("awards", awardsArr);

            // ── court_enrollments (My Profile only) ──────────────────────────────
            if (!fp) {
                JSONArray courtsArr = llCourtEnrollments != null
                        ? getCourtEnrollmentsJson(llCourtEnrollments)
                        : null;
                if (courtsArr == null || courtsArr.length() == 0) {
                    List<FirmProfileModel.CourtEnrollment> courts = profile.getCourt_enrollments();
                    if (courts != null && !courts.isEmpty()) {
                        courtsArr = new JSONArray();
                        for (FirmProfileModel.CourtEnrollment ce : courts) {
                            JSONObject obj = new JSONObject();
                            obj.put("court_name", ce.getCourt_name() != null ? ce.getCourt_name() : "");
                            obj.put("court_type", ce.getCourt_type() != null ? ce.getCourt_type() : "");
                            obj.put("state", ce.getState() != null ? ce.getState() : "");
                            obj.put("city", ce.getCity() != null ? ce.getCity() : "");
                            courtsArr.put(obj);
                        }
                    }
                }
                if (courtsArr != null && courtsArr.length() > 0)
                    payload.put("court_enrollments", courtsArr);
            }

            // ── address ───────────────────────────────────────────────────────────
            String building = et_Building != null && et_Building.getText() != null
                    ? et_Building.getText().toString().trim() : "";
            String street = et_Street != null && et_Street.getText() != null
                    ? et_Street.getText().toString().trim() : "";
            String city2 = et_City != null && et_City.getText() != null
                    ? et_City.getText().toString().trim() : "";
            String state = et_State != null && et_State.getText() != null
                    ? et_State.getText().toString().trim() : "";
            String zip = et_Zip != null && et_Zip.getText() != null
                    ? et_Zip.getText().toString().trim() : "";
            String country = et_ad_Country != null && et_ad_Country.getText() != null
                    ? et_ad_Country.getText().toString().trim() : "India";

            // fallback to model if view fields are empty (e.g. tab not yet loaded)
            if (TextUtils.isEmpty(building) && firm.getAddress() != null) {
                FirmProfileModel.Address addr = firm.getAddress();
                building = addr.getHouse_flat_no() != null ? addr.getHouse_flat_no() : "";
                street = addr.getStreet() != null ? addr.getStreet() : "";
                city2 = addr.getCity_town() != null ? addr.getCity_town() : "";
                state = addr.getState() != null ? addr.getState() : "";
                zip = addr.getZipcode() != null ? addr.getZipcode() : "";
                country = addr.getCountry() != null ? addr.getCountry() : "India";
            }

            JSONObject addressObj = new JSONObject();
            addressObj.put("house_flat_no", building);
            addressObj.put("street", street);
            addressObj.put("city_town", city2);
            addressObj.put("state", state);
            addressObj.put("country", country);
            addressObj.put("zipcode", zip);
            payload.put("address", addressObj);

            Log.d("Generate_Bio", payload.toString());

            WebServiceHelper.callHttpWebService(
                    this, getContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "v3/generate-bio",
                    "Generate_Bio",
                    payload.toString()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callCourtSuggestApi(List<String> practiceAreas, Runnable onComplete) {
        suggestedCourtTypesList.clear();
        allCourtTypesList.clear();
        pendingCourtSuggestCallback = onComplete;
        try {
            JSONObject body = new JSONObject();
            JSONArray pa = new JSONArray();
            if (practiceAreas != null) {
                for (String p : practiceAreas) pa.put(p);
            }
            body.put("practice_areas", pa);
            WebServiceHelper.callHttpWebService(
                    this, getContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "v3/court/suggest",
                    "COURT_SUGGEST",
                    body.toString()
            );
        } catch (Exception e) {
            e.printStackTrace();
            if (onComplete != null && isAdded()) requireActivity().runOnUiThread(onComplete);
        }
    }

    private void syncCourtSectionFromPracticeChange() {
        if (!isAdded()) return;

        // ── Find the Add Court button and warning from current practiceView ──
        View practiceView = (llViewScreen != null && llViewScreen.getChildCount() > 0)
                ? llViewScreen.getChildAt(0) : null;

        TextView tvAddCourt = null;
        TextView tvWarning = null;
        ChipGroup chipPracticeLocal = null;

        if (practiceView != null) {
            View tvAddCourtView = practiceView.findViewById(R.id.tv_add_court);
            tvAddCourt = (tvAddCourtView instanceof TextView) ? (TextView) tvAddCourtView : null;
            tvWarning = practiceView.findViewById(R.id.tv_no_practice_warning);
            chipPracticeLocal = practiceView.findViewById(R.id.chipGroupPractice);
        }

        // ── Update Add Court button state ─────────────────────────────────────
        updateCourtSectionState(tvAddCourt, tvWarning, chipPracticeLocal);

        // ── Show/hide warning based on chips ──────────────────────────────────
        boolean hasPracticeAreas = !getCurrentPracticeAreaValues().isEmpty();
        if (tvWarning != null) {
            tvWarning.setVisibility(hasPracticeAreas ? View.GONE : View.VISIBLE);
        }

        // ── Lock/unlock existing court enrollment rows ────────────────────────
        if (llCourtEnrollments != null) {
            for (int i = 0; i < llCourtEnrollments.getChildCount(); i++) {
                View row = llCourtEnrollments.getChildAt(i);
                lockOrUnlockCourtRow(row, hasPracticeAreas);
            }
        }
    }

    private ArrayList<String> buildSortedServicesList() {
        ArrayList<String> result = new ArrayList<>();

        // 1. Suggested items first (in their original API order)
        for (String s : suggestedServicesList) {
            if (!result.contains(s)) result.add(s);
        }

        // 2. Remaining items from allServicesList sorted alphabetically
        ArrayList<String> rest = new ArrayList<>();
        for (String s : allServicesList) {
            if (!suggestedServicesList.contains(s) && !result.contains(s)) {
                rest.add(s);
            }
        }
        Collections.sort(rest);
        result.addAll(rest);

        return result;
    }

    private void lockOrUnlockCourtRow(View row, boolean enabled) {
        if (row == null) return;

        float alpha = enabled ? 1f : 0.4f;

        LinearLayout llCourtType = row.findViewById(R.id.ll_court_type);
        LinearLayout llStateSpinner = row.findViewById(R.id.ll_state_spinner);
        LinearLayout tlCityArea = row.findViewById(R.id.tl_city_area);  // Changed from llCitySpinner
        TextInputEditText etCitySearch = row.findViewById(R.id.et_city_search);  // Add this

        ImageView imgCtDrop = llCourtType != null ? llCourtType.findViewById(R.id.img_dropdown_icon) : null;
        ImageView imgCtClear = llCourtType != null ? llCourtType.findViewById(R.id.img_clear_icon) : null;
        ImageView imgStateDrop = llStateSpinner != null ? llStateSpinner.findViewById(R.id.img_dropdown_icon) : null;
        ImageView imgStateClear = llStateSpinner != null ? llStateSpinner.findViewById(R.id.img_clear_icon) : null;
        AppCompatImageButton btnCityConfirm = row.findViewById(R.id.btn_city_confirm);
        AppCompatImageButton btnCityCancel = row.findViewById(R.id.btn_city_cancel);
        ImageView imgCityDropdown = row.findViewById(R.id.img_city_dropdown);

        if (llCourtType != null) {
            llCourtType.setEnabled(enabled);
            llCourtType.setClickable(enabled);
            llCourtType.setAlpha(alpha);
            if (!enabled) llCourtType.setOnClickListener(null);
        }

        if (llStateSpinner != null) {
            llStateSpinner.setEnabled(enabled);
            llStateSpinner.setClickable(enabled);
            llStateSpinner.setAlpha(alpha);
            if (!enabled) llStateSpinner.setOnClickListener(null);
        }

        // Handle city TextInputEditText and its buttons
        if (tlCityArea != null) {
            tlCityArea.setEnabled(enabled);
            tlCityArea.setClickable(enabled);
            tlCityArea.setAlpha(alpha);
        }

        if (etCitySearch != null) {
            etCitySearch.setEnabled(enabled);
            etCitySearch.setFocusable(enabled);
            etCitySearch.setFocusableInTouchMode(enabled);
            etCitySearch.setClickable(enabled);
            etCitySearch.setAlpha(alpha);
        }

        // Icons: hide all when disabled, restore based on value when enabled
        if (!enabled) {
            if (imgCtDrop != null) imgCtDrop.setVisibility(View.GONE);
            if (imgCtClear != null) imgCtClear.setVisibility(View.GONE);
            if (imgStateDrop != null) imgStateDrop.setVisibility(View.GONE);
            if (imgStateClear != null) imgStateClear.setVisibility(View.GONE);
            if (btnCityConfirm != null) btnCityConfirm.setVisibility(View.GONE);
            if (btnCityCancel != null) btnCityCancel.setVisibility(View.GONE);
            if (imgCityDropdown != null) imgCityDropdown.setVisibility(View.GONE);
        } else {
            // Restore icons based on whether values are selected
            TextView tvCt = llCourtType != null ? llCourtType.findViewById(R.id.tv_spinner_view) : null;
            boolean hasCourtType = tvCt != null && !TextUtils.isEmpty(tvCt.getText());
            if (imgCtDrop != null) imgCtDrop.setVisibility(hasCourtType ? View.GONE : View.VISIBLE);
            if (imgCtClear != null)
                imgCtClear.setVisibility(hasCourtType ? View.VISIBLE : View.GONE);

            TextView tvState = llStateSpinner != null ? llStateSpinner.findViewById(R.id.tv_spinner_view) : null;
            boolean hasState = tvState != null && !TextUtils.isEmpty(tvState.getText());
            if (imgStateDrop != null)
                imgStateDrop.setVisibility(hasState ? View.GONE : View.VISIBLE);
            if (imgStateClear != null)
                imgStateClear.setVisibility(hasState ? View.VISIBLE : View.GONE);

            // For city, check if there's text in the EditText
            boolean hasCity = etCitySearch != null && !TextUtils.isEmpty(etCitySearch.getText());
            if (imgCityDropdown != null) {
                imgCityDropdown.setVisibility(hasCity ? View.GONE : View.VISIBLE);
            }

            // Show confirm/cancel buttons based on state
            if (btnCityConfirm != null) {
                btnCityConfirm.setVisibility(hasCity ? View.GONE : View.VISIBLE);
            }
            if (btnCityCancel != null) {
                btnCityCancel.setVisibility(hasCity ? View.GONE : View.VISIBLE);
            }
        }
    }

    private void callCaseType() {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v2/matter/casetypes".toLowerCase(Locale.ROOT), "Case Type", postdata.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                progress_dialog.dismiss();
                AndroidUtils.showAlert(e.getMessage(), getActivity());
            }
            e.fillInStackTrace();
        }
    }

    private void callHighCourts() {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/court/high",
                    "HIGH_COURTS",
                    postdata.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                progress_dialog.dismiss();
                AndroidUtils.showAlert(e.getMessage(), getActivity());
            }
            e.fillInStackTrace();
        }
    }

    private void callSupremeCourt() {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/court/supreme",
                    "SUPREME_COURT",
                    postdata.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                progress_dialog.dismiss();
                AndroidUtils.showAlert(e.getMessage(), getActivity());
            }
            e.fillInStackTrace();
        }
    }

    // Create a list of state names from High Courts for the dropdown
    private ArrayList<String> getStateListFromHighCourts() {
        return highCourtStateList; // already sorted, already built
    }

    private ArrayList<String> getCitiesForState(String state) {
        ArrayList<String> cities = highCourtCitiesMap.get(state);
        return cities != null ? cities : new ArrayList<>();
    }

    private void loadCountryData() {
        // Each ListView needs its OWN adapter so search bars filter independently
        adCountryAdapter = new CommonSpinnerAdapter(getActivity(), countriesList);
        madCountryAdapter = new CommonSpinnerAdapter(getActivity(), countriesList);
        CommonSpinnerAdapter countryAdapter = new CommonSpinnerAdapter(getActivity(), countriesList);

        // sp_et_country is India-only / disabled — just attach adapter, no toggle
        sp_et_country.setAdapter(countryAdapter);

        // Registered address country — full setup including toggle
        AndroidUtils.DisplaySpinnerView(
                sp_et_ad_country, et_ad_Country, country_name1,
                img_dropdown_icon_adCountry, img_clear_icon_adCountry, false, adCountryAdapter, "Search Country");


        // Mailing address country — full setup including toggle
        AndroidUtils.DisplaySpinnerView(
                sp_met_ad_country, met_ad_Country, country_name2,
                img_dropdown_icon_madCountry, img_clear_icon_madCountry, false, madCountryAdapter, "Search Country");
    }
}