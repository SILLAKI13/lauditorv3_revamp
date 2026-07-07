package com.digicoffer.lauditor.AuditTrails;

import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.showDatePicker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.net.ParseException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.AuditTrails.Adapters.AuditsAdapter;
import com.digicoffer.lauditor.AuditTrails.Adapters.PaginationHelper;
import com.digicoffer.lauditor.AuditTrails.Model.AuditsModel;
import com.digicoffer.lauditor.AuditTrails.Model.SpinnerItemModal;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.DateUtils.DateUtils;
import com.digicoffer.lauditor.CommonFiles.DateUtils.DateUtilsEndDate;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class AuditTrails extends Fragment implements AsyncTaskCompleteListener, DateUtils.OnDateSelectedListener, DateUtilsEndDate.OnDateSelectedListenerEndDate {
    ArrayList<SpinnerItemModal> categoryList = new ArrayList<>();
    CommonSpinnerAdapter categoryAdapter;
    private List<TextView> pagebuttons = new ArrayList<>();
    TextView tv_name, tv_advanced_search, tv_history_act, tv_category, tv_sp_category;
    LinearLayout ll_category;
    ArrayList<AuditsModel> filterlist = new ArrayList<>();
    boolean isListFiltered = false;
    boolean iscategory_checked = true;
    ImageView img_dropdown_icon, img_clear_icon;
    HorizontalScrollView scrollView;
    private Button previousPageButton;
    AuditsAdapter audit_adapter;
    Date startDate, endDate;
    int end_temp = 0;
    ArrayList<AuditsModel> sorted_list = new ArrayList<>();
    ArrayList<AuditsModel> autentication_list = new ArrayList<>();
    ArrayList<AuditsModel> groups_list = new ArrayList<>();
    ArrayList<AuditsModel> relationship_invite_list = new ArrayList<>();
    ArrayList<AuditsModel> tm_list = new ArrayList<>();
    ArrayList<AuditsModel> relationships_list = new ArrayList<>();
    ArrayList<AuditsModel> share_list = new ArrayList<>();
    ArrayList<AuditsModel> documents_list = new ArrayList<>();
    ArrayList<AuditsModel> merge_pdf_list = new ArrayList<>();
    ArrayList<AuditsModel> legal_matter_list = new ArrayList<>();
    ArrayList<AuditsModel> general_matter_list = new ArrayList<>();
    private ColorStateList greenButtonTint, whiteButtonTint;
    ArrayList<AuditsModel> pageItems = new ArrayList<>();
    ListView sp_category;
    LinearLayout ll_page_navigation;
    LinearLayoutCompat ll_list;
    //    int maxPageButtons;
    RecyclerView rv_audits;
    TextInputLayout tl_event_start_time;
    //    TextInputEditText tv_event_end_time, tv_event_start_time;
    AppCompatButton tv_event_start_time, tv_event_end_time;
    private int currentPage = 1;
    ArrayList<AuditsModel> auditsList = new ArrayList<>();
    Dialog progress_dialog;
    String CategoryType = "";
    TextInputEditText et_search_audit_list;
    LinearLayout datePickersLayout;
    private int itemsPerPage = 10;
    LinearLayout pageNumberLayout;
    TextView tv_list;
    boolean isAdvancedSearchEnabled = false;
    private ColorStateList defaultButtonTint;
    ImageView iv_forward_button, iv_backward_button, ib_start_mandatory, ib_end_mandatory;
    ImageView ib_cancel_button, ib_cancel_button_end_date;
    private boolean isDatePickerVisible = false;
    View view;
    private Date selectedStartDate;
    private Date selectedEndDate;
    private NewModel mViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            view = inflater.inflate(R.layout.audit_trials, container, false);
            mViewModel = new ViewModelProvider(requireActivity()).get(NewModel.class);
            mViewModel.setData(requireContext().getString(R.string.audit_trails));
            greenButtonTint = ColorStateList.valueOf(getResources().getColor(R.color.blue));
            whiteButtonTint = ColorStateList.valueOf(getResources().getColor(R.color.blue_pale));
            datePickersLayout = view.findViewById(R.id.datePickersLayout);
            tv_advanced_search = view.findViewById(R.id.tv_advancedSearch);
            rv_audits = view.findViewById(R.id.rv_audits);
            tv_list = view.findViewById(R.id.tv_list);
            rv_audits.setLayoutManager(new GridLayoutManager(getContext(), 1));
            TextView from_date = view.findViewById(R.id.from_date);
            TextView to_date = view.findViewById(R.id.to_date);
            from_date.setText(R.string.from);
            tv_list.setTextColor(getContext().getColor(R.color.grey_medium));
            tv_list.setText(R.string.data_not_available);
            to_date.setText(R.string.to);
            to_date.setVisibility(View.VISIBLE);
            ib_cancel_button = view.findViewById(R.id.cancel_button);
            ib_cancel_button_end_date = view.findViewById(R.id.cancel_button_end_date);
            ib_start_mandatory = view.findViewById(R.id.mandatory);
            ib_end_mandatory = view.findViewById(R.id.mandatory_end_date);
            ib_start_mandatory.setVisibility(View.GONE);
            ib_end_mandatory.setVisibility(View.GONE);
            ib_cancel_button.setVisibility(View.GONE);
            ib_cancel_button_end_date.setVisibility(View.GONE);
            View tl_search_audit_list = view.findViewById(R.id.tl_search_audit_list);
            et_search_audit_list = tl_search_audit_list.findViewById(R.id.et_Search);
            et_search_audit_list.setHint(R.string.search);
            ll_page_navigation = view.findViewById(R.id.ll_page_navigaiton);
            ll_list = view.findViewById(R.id.ll_list);
            DateUtils dateUtils = new DateUtils(this);
            DateUtilsEndDate dateUtilsEndDate = new DateUtilsEndDate(this);
            dateUtilsEndDate.setOnDateSelectedListener(this);
            dateUtils.setOnDateSelectedListener(this);
            tv_event_start_time = view.findViewById(R.id.tv_event_start_time);
            tv_event_start_time.setHint(R.string.from);
            tv_event_end_time = view.findViewById(R.id.tv_event_end_time);
            tv_event_end_time.setHint(R.string.to);
            scrollView = view.findViewById(R.id.scrollView);
            iv_forward_button = view.findViewById(R.id.iv_forward_button);
            iv_backward_button = view.findViewById(R.id.iv_backward_button);

            tv_history_act = view.findViewById(R.id.tv_history_act);
            tv_category = view.findViewById(R.id.tv_category);
            tv_history_act.setTextSize(DynamicUtils.twenty);

            tv_category.setText(R.string.category);
            tv_history_act.setText(R.string.history_of_actions);
            tv_advanced_search.setText(R.string.advanced_search);
            ll_category = view.findViewById(R.id.ll_category);
            tv_sp_category = ll_category.findViewById(R.id.tv_spinner_view);
            tv_sp_category.setText(R.string.select_category);
            sp_category = view.findViewById(R.id.sp__category);
            sp_category.setVisibility(View.GONE);
            img_dropdown_icon = ll_category.findViewById(R.id.img_dropdown_icon);
            img_clear_icon = ll_category.findViewById(R.id.img_clear_icon);
            ll_page_navigation.setVisibility(View.GONE);

            ll_category.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AndroidUtils.display_listview(iscategory_checked, sp_category);
                    iscategory_checked = !iscategory_checked;
                }
            });
            img_clear_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AndroidUtils.DisplaySpinnerView(sp_category, tv_sp_category, CategoryType, img_dropdown_icon, img_clear_icon, false,categoryAdapter,"Search Category");
//                    loadGeneralList();
                    CategoryType = "";
                    iscategory_checked = true;
                    isDatePickerVisible = false;
                    datePickersLayout.setVisibility(View.GONE);
                    et_search_audit_list.setText("");
                    tv_event_start_time.setText(""); // Clear start time
                    tv_event_end_time.setText("");
                    loadGeneralList();// Clear end time
                }
            });
            tv_event_start_time.setOnClickListener(v ->
                    showDatePicker(tv_event_start_time, true, () -> {
                        String FLAG = "St Time";
                        loadnewPage(tv_event_start_time.getText().toString(), FLAG);
                        fetchpagedata();
                    })
            );

            tv_event_end_time.setOnClickListener(v ->
                    showDatePicker(tv_event_end_time, false, () -> {
                        String FLAG = "End Time";
                        loadnewPage(tv_event_end_time.getText().toString(), FLAG);
                        fetchpagedata();
                    })
            );
            ib_cancel_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tv_event_start_time.setText("");
                    clearDates();

                }
            });
            ib_cancel_button_end_date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tv_event_end_time.setText("");
//                    tv_event_start_time.setText("");
                    clearDates();
                }
            });
            iv_forward_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    AndroidUtils.showToast("Forward_clicked", getContext());
//                    maxPageButtons += 5;
//                    setupPagination(auditsList);
                    currentPage = currentPage + 1;
//                    load_ChosenType_list();
//                    UpdatePageButton(currentPage);
                    int startIndex = PaginationHelper.startIndexForCurrentPage(currentPage, 10);
                    int endIndex;
                    if (isListFiltered) {
                        endIndex = PaginationHelper.endIndexForCurrentPage(startIndex, filterlist.size(), 10);
                    } else if (isAdvancedSearchEnabled) {
                        endIndex = PaginationHelper.endIndexForCurrentPage(startIndex, sorted_list.size(), 10);
                    } else if (CategoryType.isEmpty()) {
                        endIndex = PaginationHelper.endIndexForCurrentPage(startIndex, auditsList.size(), 10);
                    } else {
                        endIndex = PaginationHelper.endIndexForCurrentPage(startIndex, sorted_list.size(), 10);
                    }
                    if (endIndex > end_temp) {
//                    isAdvancedSearchEnabled = false;
                        load_ChosenType_list();
                        UpdatePageButton(currentPage);
                    } else {
                        currentPage = currentPage - 1;
                    }
                    Log.d("current_page", "" + currentPage);
//                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });

            iv_backward_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    AndroidUtils.showToast("Back_clicked", getContext());
//                    maxPageButtons -= 5;

                    currentPage = currentPage - 1;
                    if (currentPage > 0) {
//                        isAdvancedSearchEnabled = false;
//                        setupPagination(auditsList);
                        load_ChosenType_list();
                        UpdatePageButton(currentPage);
//                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    } else {
                        currentPage = 1;
                    }
                }
            });

            pageNumberLayout = view.findViewById(R.id.pageNumberLayout);

            //            et_search_audit_list.setHint("Search");
//            loadSearchTextData();
            loadSpinnerData();
            tv_advanced_search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isDatePickerVisible = !isDatePickerVisible;
                    // Find the datePickersLayout by its ID
                    // Set the visibility based on the flag
                    datePickersLayout.setVisibility(isDatePickerVisible ? View.VISIBLE : View.GONE);
                    if (!isDatePickerVisible) {
                        tv_event_start_time.setText(""); // Clear start time
                        tv_event_end_time.setText(""); // Clear end time
                        sorted_list.clear();
                        clearDates();
//                    auditsList.clear();
//                    loadSpinnerData();
                    }
//                    loadPage(currentPage, isAdvancedSearchEnabled);
//                    setupPagination();
//                    isDatePickerVisible = !isDatePickerVisible;
//
//                    // Find the datePickersLayout by its ID
//                    datePickersLayout.setVisibility(isDatePickerVisible ? View.VISIBLE : View.GONE);
//
//                    // Check if the date picker layout is hidden, and clear the date fields
//                    if (!isDatePickerVisible) {
//                        tv_event_start_time.setText(""); // Clear start time
//                        tv_event_end_time.setText(""); // Clear end time
//                        sorted_list.clear();
//                    }
                }
            });
//          callAuditWebservice();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return view;
    }

    private void clearDates() {
        sorted_list.clear();
        et_search_audit_list.setText("");
        String FLAG = "";
        loadnewPage(null, FLAG);
        fetchpagedata();
    }

    private @NonNull DatePickerDialog getDatePickerDialog(boolean is_start) {
        final Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                if (is_start) {
                    updatestartLabel();
                    String FLAG = "St Time";
                    loadnewPage(tv_event_start_time.getText().toString(), FLAG);
                    fetchpagedata();
                } else {
                    updateendLabel();
                    String FLAG = "End Time";
                    loadnewPage(tv_event_end_time.getText().toString(), FLAG);
                    fetchpagedata();
                }

            }

            private void updatestartLabel() {
                String myFormat = "dd-MM-yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                tv_event_start_time.setText(sdf.format(myCalendar.getTime()));
            }

            private void updateendLabel() {
                String myFormat = "dd-MM-yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                tv_event_end_time.setText(sdf.format(myCalendar.getTime()));
            }
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
        return datePickerDialog;
    }

    private void callAuditWebservice() {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONObject postData = new JSONObject();
        try {
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/auditlogs", "Audit Logs", postData.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    private void loadSpinnerData() {
//        19731710
        categoryList.add(new SpinnerItemModal("Authorization"));
        if (!"solo".equals(Constants.CATEGORY)) {
            categoryList.add(new SpinnerItemModal("Groups"));
            categoryList.add(new SpinnerItemModal("Team Members"));
        }
        categoryList.add(new SpinnerItemModal("Relationships"));
        categoryList.add(new SpinnerItemModal("Relationship Invite"));
        categoryList.add(new SpinnerItemModal("Share"));
        categoryList.add(new SpinnerItemModal("Documents"));
        categoryList.add(new SpinnerItemModal("Merge PDF"));
        categoryList.add(new SpinnerItemModal("Legal Matters"));
        categoryList.add(new SpinnerItemModal("General Matters"));
        categoryAdapter = new CommonSpinnerAdapter(getActivity(), categoryList);
        sp_category.setAdapter(categoryAdapter);
        loadItemSelectedListner();
        if (autentication_list.isEmpty() && groups_list.isEmpty() && relationship_invite_list.isEmpty() && tm_list.isEmpty() && relationships_list.isEmpty() && share_list.isEmpty() && documents_list.isEmpty() && merge_pdf_list.isEmpty() && legal_matter_list.isEmpty() && general_matter_list.isEmpty()) {
            callAuditWebservice();
        }
    }

    private void loadItemSelectedListner() {
        sp_category.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                try {
                    rv_audits.removeAllViews();
//                    rv_audits.setAdapter(null);
                    et_search_audit_list.setText("");
                    startDate = null;
                    endDate = null;
                    tv_event_start_time.setText("");
                    tv_event_end_time.setText("");
//                 sorted_list.clear();
//              auditsList.clear();

                    if (categoryList.get(i).getName().equalsIgnoreCase("Authorization")) {
                        CategoryType = "AUTH";
                    } else if (categoryList.get(i).getName().equalsIgnoreCase("Groups")) {
                        CategoryType = "GROUPS";
                    } else if (categoryList.get(i).getName().equalsIgnoreCase("Team Members")) {
                        CategoryType = "TEAM MEMBER";
                    } else if (categoryList.get(i).getName().equalsIgnoreCase("Relationships")) {
                        CategoryType = "RELATIONSHIP";
                    } else if (categoryList.get(i).getName().equalsIgnoreCase("Share")) {
                        CategoryType = "SHARE";
                    } else if (categoryList.get(i).getName().equalsIgnoreCase("Relationship Invite")) {
                        CategoryType = "RELATIONSHIP INVITE";
                    } else if (categoryList.get(i).getName().equalsIgnoreCase("Documents")) {
                        CategoryType = "DOCUMENT";
                    } else if (categoryList.get(i).getName().equalsIgnoreCase("Merge PDF")) {
                        CategoryType = "MERGE PDF";
                    } else if (categoryList.get(i).getName().equalsIgnoreCase("Legal Matters")) {
                        CategoryType = "LEGAL MATTER";
                    } else if (categoryList.get(i).getName().equalsIgnoreCase("General Matters")) {
                        CategoryType = "GENERAL MATTER";
                    } else {
                        CategoryType = "";
                    }
                    Log.d("Category_type", CategoryType);
                    Log.d("AuditSize", String.valueOf(auditsList.size()));

//                    if (audit_adapter != null) {
//                        audit_adapter.clearData();
//                    }
//
                    if (startDate != null || endDate != null) {
                        sorted_list.clear();
                        et_search_audit_list.setText("");
                        String FLAG = "End Time";
                        loadnewPage(null, FLAG);
//                        fetchpagedata();
                    } else {
//                        pageNumberLayout.removeAllViews();
                        isAdvancedSearchEnabled = false;
                        tv_event_start_time.setText("");
                        tv_event_end_time.setText("");
                        et_search_audit_list.setText("");
//                        cv_list.setVisibility(View.VISIBLE);
//                     setupPagination();
                        currentPage = 1;
//                             String FLAG = "";
//                         loadnewPage(null, FLAG);
                        loadPage(currentPage, isAdvancedSearchEnabled);
                        if (CategoryType.isEmpty())
                            setupPagination(auditsList);
                        else
                            setupPagination(sorted_list);
//                        }
//                        else{
//
                    }
                    UpdatePageButton(currentPage);
//                    }
//                     }
//                    }

                } catch (Exception e) {
                    Log.d("Exception", e.getMessage());
                    throw new RuntimeException(e);
                }
//                loadPage(currentPage);
//                entity_id = entities_list.get(sp_entities.getSelectedItemPosition()).getId();
//                callEntityClientWebservice(entity_id);
                AndroidUtils.DisplaySpinnerView(sp_category, tv_sp_category, categoryList.get(i).getName(), img_dropdown_icon, img_clear_icon, false, categoryAdapter,"Search Category");
//                tv_sp_category.setText(categoryList.get(i).getName());
                iscategory_checked = true;
            }
        });
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
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                Log.d("Request_Type", httpResult.getRequestType());

                if ("Audit Logs".equals(httpResult.getRequestType())) {

                    JSONArray jsonArray = result.getJSONArray("data");
                    et_search_audit_list.setText("");
                    loadNewAuditsData(jsonArray);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Failed) {

            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);

            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                AndroidUtils.showErrorAlert(result.optString("msg"), getActivity());
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);

            AndroidUtils.showErrorAlert(
                    httpResult.getResponseContent().toString(),
                    getActivity()
            );
        }
    }


    private void setupPagination(ArrayList<AuditsModel> sorted_list) {
        // Maximum number of page buttons to display
        pageNumberLayout.removeAllViews();
        pagebuttons.clear();

//        int totalPages = (int) Math.ceil((double) sorted_list.size() / itemsPerPage);
        int totalPages = PaginationHelper.calculateTotalNoOfPages(sorted_list.size(), 10);
        Log.d("total_pages", "" + totalPages + ".." + sorted_list.size());
        if (totalPages == 0) {
            ll_list.setVisibility(View.GONE);
        } else {
            ll_list.setVisibility(View.VISIBLE);
        }
        if (totalPages == 0) {
            tv_list.setVisibility(View.VISIBLE);
        } else {
            tv_list.setVisibility(View.GONE);
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
                    load_ChosenType_list();
                    UpdatePageButton(currentPage);
                    // Update the previousPageButton reference
                    previousPageButton = pageButton;
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

        scrollView.post(new Runnable() {
            @Override
            public void run() {
//                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
//        loadPage(currentPage);
    }

    private void loadPage(final int page, final boolean isAdvancedSearchEnabled) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        try {
            // Add data based on category type
            if (!isAdvancedSearchEnabled) {
                sorted_list.clear();
                switch (CategoryType) {
                    case "AUTH":
                        sorted_list.addAll(autentication_list);
                        break;
                    case "GROUPS":
                        sorted_list.addAll(groups_list);
                        break;
                    case "RELATIONSHIP INVITE":
                        sorted_list.addAll(relationship_invite_list);
                        break;
                    case "TEAM MEMBER":
                        sorted_list.addAll(tm_list);
                        break;
                    case "RELATIONSHIP":
                        sorted_list.addAll(relationships_list);
                        break;
                    case "SHARE":
                        sorted_list.addAll(share_list);
                        break;
                    case "DOCUMENT":
                        sorted_list.addAll(documents_list);
                        break;
                    case "MERGE PDF":
                        sorted_list.addAll(merge_pdf_list);
                        break;
                    case "LEGAL MATTER":
                        sorted_list.addAll(legal_matter_list);
                        break;
                    case "GENERAL MATTER":
                        sorted_list.addAll(general_matter_list);
                        break;
                    default:
                        sorted_list.addAll(auditsList);
                        break;
                }
            }
//            Collections.reverse(sorted_list);
            loadRecyclerView(page, sorted_list);
        } catch (final Exception e) {
        }
//        }).start();
    }

    private void loadNewAuditsData(JSONArray jsonArray) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault());
            outputFormat.setTimeZone(TimeZone.getDefault());
            clearLists();
//            autentication_list.clear();
            int EndIndex = Math.min(2500, jsonArray.length());
            for (int i = 0; i < EndIndex; i++) {
                AuditsModel auditsModel = new AuditsModel();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                auditsModel.setMessage(jsonObject.getString("message"));
                auditsModel.setName(jsonObject.getString("name"));
                String rawTimestamp = jsonObject.getString("timestamp");
//                auditsModel.setTimestamp(rawTimestamp); // Store original timestamp if needed
                // Convert and format the timestamp
                Date parsedDate = isoFormat.parse(rawTimestamp);
                String formattedTimestamp = parsedDate != null ? outputFormat.format(parsedDate) : "";
                auditsModel.setTimestamp(formattedTimestamp);
                if (!(auditsModel.getMessage().equals("PROFILE UPDATE")) || !(auditsModel.getMessage().equals("DOCS COLLABORATION"))) {
//                    if ()
                    auditsList.add(auditsModel);
//                    Collections.reverse(auditsList);
                    if (jsonObject.getString("name").equals("AUTH")) {
                        autentication_list.add(auditsModel);
//                        Collections.reverse(autentication_list);
                    } else if (jsonObject.getString("name").equals("GROUPS")) {
                        groups_list.add(auditsModel);
//                        Collections.reverse(groups_list);
                    } else if (jsonObject.getString("name").equals("RELATIONSHIP INVITE")) {
                        relationship_invite_list.add(auditsModel);
//                        Collections.reverse(groups_list);
                    } else if (jsonObject.getString("name").equals("TEAM MEMBER")) {
                        tm_list.add(auditsModel);
//                        Collections.reverse(tm_list);
                    } else if (jsonObject.getString("name").equals("RELATIONSHIP")) {
                        relationships_list.add(auditsModel);
//                        Collections.reverse(relationships_list);
                    } else if (jsonObject.getString("name").equals("SHARE")) {
                        share_list.add(auditsModel);
//                        Collections.reverse(share_list);
                    } else if (jsonObject.getString("name").equals("DOCUMENT")) {
                        documents_list.add(auditsModel);
//                        Collections.reverse(documents_list);
                    } else if (jsonObject.getString("name").equals("MERGE PDF")) {
                        merge_pdf_list.add(auditsModel);
//                        Collections.reverse(merge_pdf_list);
                    } else if (jsonObject.getString("name").equals("LEGAL MATTER")) {
                        legal_matter_list.add(auditsModel);
//                        Collections.reverse(legal_matter_list);
                    } else if (jsonObject.getString("name").equals("GENERAL MATTER")) {
                        general_matter_list.add(auditsModel);
//                        Collections.reverse(general_matter_list);
                    }
                }
            }
//            for (int i = 0; i < autentication_list.size(); i++) {
//                Log.d("Audit_List", autentication_list.get(i).getName());
//            }
            Log.d("auth_size", "" + auditsList.size());
//            System.out.println("auth isze" + autentication_list.size());
            ll_page_navigation.setVisibility(View.VISIBLE);
            loadGeneralList();
        } catch (JSONException | java.text.ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadGeneralList() {
        currentPage = 1;
        loadRecyclerView(currentPage, auditsList);
        setupPagination(auditsList);
        UpdatePageButton(currentPage);
//        if (auditsList.isEmpty()) {
//            tv_list.setVisibility(View.VISIBLE);
//        } else {
//            tv_list.setVisibility(View.GONE);
//        }
    }

    private void clearLists() {
        autentication_list.clear();
        groups_list.clear();
        relationship_invite_list.clear();
        tm_list.clear();
        relationships_list.clear();
        share_list.clear();
        documents_list.clear();
        merge_pdf_list.clear();
        legal_matter_list.clear();
        general_matter_list.clear();
        auditsList.clear();
    }


    private void loadRecyclerView(int page, ArrayList<AuditsModel> sorted_list) {
        try {
            Log.d("Sorted_list_new", String.valueOf(sorted_list.size()));
            if (!sorted_list.isEmpty()) {
                int startIndex = PaginationHelper.startIndexForCurrentPage(currentPage, 10);
                int endIndex = PaginationHelper.endIndexForCurrentPage(startIndex, sorted_list.size(), 10);
                end_temp = endIndex;
                pageItems = new ArrayList<>(sorted_list.subList(startIndex, endIndex));

                if (audit_adapter == null) {
                    // If the adapter is null, create a new one and set it to the RecyclerView
                    audit_adapter = new AuditsAdapter(sorted_list);
                    rv_audits.setAdapter(audit_adapter);
                    Log.d("sorted_list", "" + sorted_list.size());
                    audit_adapter.setData(pageItems);
                    AndroidUtils.LoadingRecyclerview(rv_audits, getContext());
                    AndroidUtils.setupBottomSpacerFooter(rv_audits, getResources().getDimensionPixelSize(R.dimen.twentyeight_dp));
                    et_search_audit_list.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            Searchfilter();
                        }
                    });
                } else {
                    // If the adapter already exists, notify it about the new data
                    audit_adapter.setData(pageItems);
                }
                UpdatePageButton(1);
            }
        } catch (Exception e) {
            Log.d("Recyclervie_Exception", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDateSelected(String selectedDate, String FLAG) {
        loadnewPage(selectedDate, FLAG);
        fetchpagedata();
    }

    private void fetchpagedata() {
        setupPagination(sorted_list);
        currentPage = 1;
        isAdvancedSearchEnabled = true;
        loadPage(currentPage, isAdvancedSearchEnabled);
    }

    private void load_ChosenType_list() {
        if (CategoryType.isEmpty()) {
            if (isListFiltered)
                loadRecyclerView(currentPage, filterlist);
            else if (isAdvancedSearchEnabled) {
                loadRecyclerView(currentPage, sorted_list);
            } else
                loadRecyclerView(currentPage, auditsList);
        } else {
            if (isListFiltered)
                loadRecyclerView(currentPage, filterlist);
            else {
                loadRecyclerView(currentPage, sorted_list);
            }
        }
    }

    private void Searchfilter() {
        rv_audits.removeAllViews();
        filterlist.clear();
        switch (CategoryType) {
            case "AUTH":
                if (!isAdvancedSearchEnabled) {
//                    sorted_list.clear();
                    filterlist.addAll(filterList(autentication_list, et_search_audit_list.getText().toString()));
                } else {
                    filterlist.addAll(filterList(sorted_list, et_search_audit_list.getText().toString()));
                }
                break;
            case "GROUPS":
                if (!isAdvancedSearchEnabled) {
//                    sorted_list.clear();
                    filterlist.addAll(filterList(groups_list, et_search_audit_list.getText().toString()));
                } else {
                    filterlist.addAll(filterList(sorted_list, et_search_audit_list.getText().toString()));
                }
                break;

            case "TEAM MEMBER":
                if (!isAdvancedSearchEnabled) {
//                    sorted_list.clear();
                    filterlist.addAll(filterList(tm_list, et_search_audit_list.getText().toString()));
                } else {
                    filterlist.addAll(filterList(sorted_list, et_search_audit_list.getText().toString()));
                }
                break;
            case "RELATIONSHIP":
                if (!isAdvancedSearchEnabled) {
//                    sorted_list.clear();
                    filterlist.addAll(filterList(relationships_list, et_search_audit_list.getText().toString()));
                } else {
                    filterlist.addAll(filterList(sorted_list, et_search_audit_list.getText().toString()));
                }
                break;
            case "RELATIONSHIP INVITE":
                if (!isAdvancedSearchEnabled) {
//                    sorted_list.clear();
                    filterlist.addAll(filterList(relationship_invite_list, et_search_audit_list.getText().toString()));
                } else {
                    filterlist.addAll(filterList(sorted_list, et_search_audit_list.getText().toString()));
                }
                break;
            case "SHARE":
                if (!isAdvancedSearchEnabled) {
//                    sorted_list.clear();
                    filterlist.addAll(filterList(share_list, et_search_audit_list.getText().toString()));
                } else {
                    filterlist.addAll(filterList(sorted_list, et_search_audit_list.getText().toString()));
                }
                break;
            case "DOCUMENT":
                if (!isAdvancedSearchEnabled) {
//                    sorted_list.clear();
                    filterlist.addAll(filterList(documents_list, et_search_audit_list.getText().toString()));
                } else {
                    filterlist.addAll(filterList(sorted_list, et_search_audit_list.getText().toString()));
                }
                break;
            case "MERGE PDF":
                if (!isAdvancedSearchEnabled) {
//                    sorted_list.clear();
                    filterlist.addAll(filterList(merge_pdf_list, et_search_audit_list.getText().toString()));
                } else {
                    filterlist.addAll(filterList(sorted_list, et_search_audit_list.getText().toString()));
                }
                break;
            case "LEGAL MATTER":
                if (!isAdvancedSearchEnabled) {
//                    sorted_list.clear();
                    filterlist.addAll(filterList(legal_matter_list, et_search_audit_list.getText().toString()));
                } else {
                    filterlist.addAll(filterList(sorted_list, et_search_audit_list.getText().toString()));
                }
                break;
            case "GENERAL MATTER":
                if (!isAdvancedSearchEnabled) {
//                    sorted_list.clear();
                    filterlist.addAll(filterList(general_matter_list, et_search_audit_list.getText().toString()));
                } else {
                    filterlist.addAll(filterList(sorted_list, et_search_audit_list.getText().toString()));
                }
                break;
            default:
                if (!isAdvancedSearchEnabled) {
//                    sorted_list.clear();
                    filterlist.addAll(filterList(auditsList, et_search_audit_list.getText().toString()));
                } else {
                    filterlist.addAll(filterList(sorted_list, et_search_audit_list.getText().toString()));
                }
                break;

        }
        currentPage = 1;
        if (CategoryType.isEmpty()) {
            if (!et_search_audit_list.getText().toString().isEmpty()) {
                loadRecyclerView(currentPage, filterlist);
                setupPagination(filterlist);
            } else if (!isAdvancedSearchEnabled) {
                loadRecyclerView(currentPage, auditsList);
                setupPagination(auditsList);
            } else {
                loadRecyclerView(currentPage, sorted_list);
                setupPagination(sorted_list);
            }
            UpdatePageButton(currentPage);
//            loadRecyclerView(currentPage, filterlist);
//            setupPagination(filterlist);
//            UpdatePageButton(currentPage);
        } else {
            if (!et_search_audit_list.getText().toString().isEmpty()) {
                loadRecyclerView(currentPage, filterlist);
                setupPagination(filterlist);
            } else {
                loadRecyclerView(currentPage, sorted_list);
                setupPagination(sorted_list);
            }
            UpdatePageButton(currentPage);
        }
    }

    private ArrayList<AuditsModel> filterList(ArrayList<AuditsModel> list, String charString) {
        isListFiltered = false;

        // If the search string is empty, return the full list
        if (charString.isEmpty()) {
            return list;
        } else {
            ArrayList<AuditsModel> filteredList = new ArrayList<>();

            // Use a Pattern with case-insensitive flag for matching
            Pattern pattern = Pattern.compile(Pattern.quote(charString), Pattern.CASE_INSENSITIVE);

            for (AuditsModel row : list) {
                // Check if any of the fields contain the search string
                if (pattern.matcher(AndroidUtils.isNull(row.getMessage()).toLowerCase()).find()
                        || pattern.matcher(AndroidUtils.isNull(row.getTimestamp()).toLowerCase()).find()
                        || pattern.matcher(AndroidUtils.isNull(row.getName()).toLowerCase()).find()) {
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

    private void loadnewPage(String selectedDate, String FLAG) {
        try {
            // Clear existing data
            sorted_list.clear();
            rv_audits.removeAllViews();
            et_search_audit_list.setText("");

            // Convert the start and end dates from UI elements
            startDate = DateUtils.stringToDate(tv_event_start_time.getText().toString());
            endDate = DateUtils.stringToDate(tv_event_end_time.getText().toString());

            // Load data based on category
            switch (CategoryType) {
                case "AUTH":
                    loadAdvancedData(autentication_list);
                    break;
                case "GROUPS":
                    loadAdvancedData(groups_list);
                    break;
                case "RELATIONSHIP INVITE":
                    loadAdvancedData(relationship_invite_list);
                    break;
                case "TEAM MEMBER":
                    loadAdvancedData(tm_list);
                    break;
                case "RELATIONSHIP":
                    loadAdvancedData(relationships_list);
                    break;
                case "SHARE":
                    loadAdvancedData(share_list);
                    break;
                case "DOCUMENT":
                    loadAdvancedData(documents_list);
                    break;
                case "MERGE PDF":
                    loadAdvancedData(merge_pdf_list);
                    break;
                case "LEGAL MATTER":
                    loadAdvancedData(legal_matter_list);
                    break;
                case "GENERAL MATTER":
                    loadAdvancedData(general_matter_list);
                    break;
                default:
                    loadAdvancedData(auditsList);
                    break;
            }

            // Filter the list based on selected date range
            filterByDateRange();

            // Set up the RecyclerView and Adapter
            audit_adapter = new AuditsAdapter(sorted_list);
            rv_audits.setAdapter(audit_adapter);
            AndroidUtils.LoadingRecyclerview(rv_audits, getContext());
            AndroidUtils.setupBottomSpacerFooter(rv_audits, getResources().getDimensionPixelSize(R.dimen.twentyeight_dp));
            // Refresh RecyclerView
            currentPage = 1;
            UpdatePageButton(currentPage);
            audit_adapter.updateData(sorted_list);
            et_search_audit_list.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Searchfilter();
                }
            });

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    private void filterByDateRange() {
//        List<AuditsModel> filteredList = new ArrayList<>();
//
//        for (AuditsModel auditsModel : sorted_list) {
//            String timestamp = auditsModel.getTimestamp();
//            Date formattedDate = AndroidUtils.stringToDateTimeDefault(timestamp, "MMM dd,yyyy, hh:mm a");
//
//            if (isDateInRange(formattedDate, startDate, endDate)) {
//                filteredList.add(auditsModel);
//            }
//        }
//
//        // Update the sorted_list with filtered results
//        sorted_list.clear();
//        sorted_list.addAll(filteredList);
        try {
            List<AuditsModel> filteredList = new ArrayList<>();

            SimpleDateFormat formattedParser = new SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault());
            formattedParser.setTimeZone(TimeZone.getDefault());

            for (AuditsModel auditsModel : sorted_list) {
                String formattedTimestamp = auditsModel.getTimestamp();  // formatted string like "May 27, 2025 | 10:13 PM"
                Date parsedDate = formattedParser.parse(formattedTimestamp);

                if (parsedDate != null && isDateInRange(parsedDate, startDate, endDate)) {
                    filteredList.add(auditsModel);
                }
            }
// Update the sorted_list with filtered results
            sorted_list.clear();
            sorted_list.addAll(filteredList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void UpdatePageButton(int currentPage) {
        for (int i = 0; i < pagebuttons.size(); i++) {
            TextView pageButton = pagebuttons.get(i);
            if (currentPage >= 0) {
                if (i + 1 == currentPage) {
                    pageButton.setTextColor(getActivity().getColor(R.color.white));
                    pageButton.setBackground(getActivity().getDrawable(R.drawable.blue_gradient_card));
                } else {
                    pageButton.setTextColor(getActivity().getColor(R.color.blue));
                    pageButton.setBackground(requireActivity().getDrawable(R.drawable.background_transparent));
                }
            }
        }
    }

    //}
//    private void loadAdvancedData(ArrayList<AuditsModel> advanced_list) {
//        sorted_list.clear();
//        for (int i = 0; i < advanced_list.size(); i++) {
//            try {
//                AuditsModel auditsModel = advanced_list.get(i);
//                Log.d("Advanced_DATA", auditsModel.getName());
//                String timestamp = auditsModel.getTimestamp();
//                String isoDateString = auditsModel.getTimestamp(); // example: "2025-01-24T09:33:01.499Z"
//
//// Step 1: Parse ISO 8601 string
//                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
//                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//                Date parsedDate = isoFormat.parse(isoDateString);
//

    /// / Step 2: Format to "May 27, 2025 | 10:13 PM"
//                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault());
//                outputFormat.setTimeZone(TimeZone.getDefault()); // convert to local time zone
//                String formattedOutput = parsedDate != null ? outputFormat.format(parsedDate) : "";
//
//                Log.d("Formatted_Output", formattedOutput);
//
//                if (startDate != null && endDate != null) {
//                    if (formatted_date != null && formatted_date.after(startDate) && formatted_date.before(endDate)) {
//                        if (auditsModel.getName().startsWith(CategoryType.toUpperCase(Locale.ROOT))) {
//                            sorted_list.add(auditsModel);
//                        }
//                    }
//                } else if (startDate != null) {
//                    if (formatted_date != null && (formatted_date.after(startDate) || formatted_date.equals(startDate))) {
//                        if (auditsModel.getName().startsWith(CategoryType.toUpperCase(Locale.ROOT))) {
//                            try {
//                                sorted_list.add(auditsModel);
//                            } catch (Exception e) {
//                                throw new RuntimeException(e);
//                            }
//                        }
//                    }
//                } else if (endDate != null) {
//                    if (formatted_date != null && (formatted_date.before(endDate) || formatted_date.equals(endDate))) {
//                        if (auditsModel.getName().startsWith(CategoryType.toUpperCase(Locale.ROOT))) {
//                            sorted_list.add(auditsModel);
//                        }
//                    }
//                } else {
//                    if (auditsModel.getName().startsWith(CategoryType.toUpperCase(Locale.ROOT))) {
//                        sorted_list.add(auditsModel);
//                    }
//                }
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
    private void loadAdvancedData(ArrayList<AuditsModel> advanced_list) {
        sorted_list.clear();

        // Parser for formatted timestamp: "May 27, 2025 | 10:13 PM"
        SimpleDateFormat formattedParser = new SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault());
        formattedParser.setTimeZone(TimeZone.getDefault());

        for (int i = 0; i < advanced_list.size(); i++) {
            try {
                AuditsModel auditsModel = advanced_list.get(i);
                Log.d("Advanced_DATA", auditsModel.getName());

                String formattedTimestamp = auditsModel.getTimestamp();  // Already in "May 27, 2025 | 10:13 PM"

                // Parse the formatted timestamp string to a Date
                Date parsedDate = formattedParser.parse(formattedTimestamp);

                Log.d("Formatted_Output", formattedTimestamp);

                // Filter by date range
                boolean isInRange = false;

                if (startDate != null && endDate != null) {
                    isInRange = parsedDate != null && parsedDate.after(startDate) && parsedDate.before(endDate);
                } else if (startDate != null) {
                    isInRange = parsedDate != null && (parsedDate.after(startDate) || parsedDate.equals(startDate));
                } else if (endDate != null) {
                    isInRange = parsedDate != null && (parsedDate.before(endDate) || parsedDate.equals(endDate));
                } else {
                    isInRange = true; // no date filters
                }

                // Filter by category type
                if (isInRange && auditsModel.getName().startsWith(CategoryType.toUpperCase(Locale.ROOT))) {
                    sorted_list.add(auditsModel);
                }

            } catch (ParseException e) {
                e.printStackTrace(); // Invalid date format
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isDateInRange(Date date, Date startDate, Date endDate) {
        return (startDate == null || !date.before(startDate)) &&
                (endDate == null || !date.after(endDate));
    }

    @Override
    public void onDateSelectedEndDate(String selectedDate, String FLAG) {
        loadnewPage(selectedDate, FLAG);
        fetchpagedata();
    }
}

