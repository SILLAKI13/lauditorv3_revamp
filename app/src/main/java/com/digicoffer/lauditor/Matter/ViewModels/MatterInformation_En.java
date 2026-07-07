package com.digicoffer.lauditor.Matter.ViewModels;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.showDatePicker;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;

import java.util.Iterator;
import java.util.regex.Pattern;

import android.util.Patterns;
import android.net.ParseException;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.digicoffer.lauditor.Matter.Models.ViewMatterModel;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.DescriptionValidation;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
//
//import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.digicoffer.lauditor.Matter.Models.AdvocateModel;
import com.digicoffer.lauditor.Matter.Models.MatterModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class MatterInformation_En extends Fragment implements View.OnClickListener, AsyncTaskCompleteListener {
    TextInputEditText et_matter_title, et_matter_num, et_matter_description, et_court, et_judge, et_matter_tags, et_matter_type;
    AppCompatButton tv_start_date, tv_end_date, tv_dof, tv_create_date;
    private List<TextView> pagebuttons = new ArrayList<>();
    ListView sp_case_type;
    Boolean isCancelClicked = false;
    TextView matter_type, et_case_type, defaultTxt, tv_high_priority, tv_medium_priority, tv_low_priority, tv_status_active, tv_status_pending, Title, datefill, create_date, start_date, closedate, court, judge, priority, status, addopponentadvocate, tv_matter_tags, matter_title, matter_date;
    Button btn_add_advocate, btn_cancel_edit;
    private int currentPage = 1;
    private boolean isInitialLoad = true;
    MatterModel matterModel = new MatterModel();
    ArrayList<String> tag_list = new ArrayList<>();
    ArrayList<String> caseTypeList = new ArrayList<>();
    private int editPosition = -1;
    ShapeableImageView edit_matter_page_icon;
    TextView edit_matter_page_txt;
    private int end_index;
    boolean isChangesOccured = false;
    private int pageNumber;
    boolean isCaseTypeChecked = true;
    private Dialog progress_dialog;
    String ad_name = "";
    String ad_email = "";
    String ad_phone = "";
    JSONArray clients = new JSONArray();
    JSONArray documents = new JSONArray();
    JSONArray members = new JSONArray();
    JSONArray groups = new JSONArray();
    JSONArray group_acls = new JSONArray();
    JSONArray advocates = new JSONArray();
    JSONArray tags = new JSONArray();
    ImageView iv_remove_matter;
    JSONArray existing_clients;
    JSONArray exisiting_group_acls;
    JSONArray existing_corp_clients;
    JSONArray existing_temp_list;
    JSONArray existing_members;
    JSONArray existing_groups_list;
    JSONArray existing_clients_list;
    JSONArray existing_tm_list;
    JSONArray existing_documents;
    JSONArray existing_documents_list;
    JSONArray existing_tags_list = new JSONArray();

    LinearLayout ll_case_type, ll_page_navigaiton, ll_matter_id, pageNumberLayout, ll_selected_advocates, tl_case_type, ll_case_num, ll_matter_type;
    HorizontalScrollView scrollView;
    ArrayList<AdvocateModel> advocates_list = new ArrayList<>();

    ArrayList<MatterModel> matterArraylist;
    Matter matter;
    TextView m_c_number, m_c_type, description_name, matter_id;
    AppCompatButton btn_cancel_save, btn_add_tag, btn_create;
    LinearLayout ll_add_advocate, ll_added_tags, ll_date, ll_start_date, ll_end_date, ll_court, ll_judge, ll_dof, ll_create_date, ll_matter_tags, other_information;
    TextInputEditText et_matter_tag;
    RelativeLayout ll_matter_title;
    LinearLayoutCompat ll_header;
    JSONArray existing_opponents;
    CardView cv_client_details;
    TextView advocate_title, advocate_email, advocate_phone;
    String CASE_PRIORITY = "High";
    String STATUS = "Active";
    String oldStatus = "";
    String oldPriority = "";
    Boolean isMoreDetailsChecked = true;

    LinearLayout ll_opponent_advocate, ll_matterDate;
    TextInputEditText et_advocate_name, et_advocate_email, et_advocate_phone, et_matter_id;
    AppCompatButton btn_cancel_tag, btn_save_tag;
    //    AppCompatButton btn_add_more_info;
    TextView btn_add_more_info;
//    EditMatterTimeline editMatterTimeline;
    ImageView iv_backward_button, iv_forward_button, iv_remove_tag, img_clear_icon;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    TextView tv_response, tv_reponse_email, tv_response_phone;
    private static final String TAG = "MatterInformation";
    private Calendar finalMyCalendar = Calendar.getInstance();
    ViewMatterModel viewMatterModel1;
//    ViewMatter viewMatter;

//    public MatterInformation_En(ViewMatterModel viewMatterModel, ViewMatter viewMatter) {
//        viewMatterModel1 = viewMatterModel;
//        this.viewMatter = viewMatter;
//    }

    public MatterInformation_En() {
    }


    @SuppressLint({"MissingInflatedId", "SetTextI18n", "ClickableViewAccessibility"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.matter_information_en, container, false);
        matter_date = view.findViewById(R.id.matter_date);
        ll_matter_type = view.findViewById(R.id.ll_matter_type);
        iv_remove_matter = view.findViewById(R.id.iv_remove_matter);
        matter_type = view.findViewById(R.id.matter_type);
        ll_case_type = view.findViewById(R.id.ll_case_type);
        matter_type.setText(R.string.matter_type);
        defaultTxt = view.findViewById(R.id.defaultTxt);
        defaultTxt.setText(R.string.auto_generated_txt);
        defaultTxt.setTextColor(requireContext().getColor(R.color.light_blue));
        ll_matterDate = view.findViewById(R.id.ll_matterDate);
        ll_matterDate.setVisibility(GONE);
        ll_matter_id = view.findViewById(R.id.ll_matter_id);
        ll_matter_id.setAlpha(0.6f);
        et_matter_title = view.findViewById(R.id.et_matter_title);
        matter_title = view.findViewById(R.id.matter_title);
        edit_matter_page_icon = view.findViewById(R.id.edit_matter_page_icon);
        edit_matter_page_txt = view.findViewById(R.id.edit_matter_page_txt);
        et_matter_title.addTextChangedListener(new Validation(et_matter_title));
        et_matter_title.setHint(R.string.case_title);
        ll_header = view.findViewById(R.id.ll_header);
        ll_opponent_advocate = view.findViewById(R.id.ll_opponent_advocate);
        ll_case_num = view.findViewById(R.id.ll_case_num);
        ll_opponent_advocate.setVisibility(GONE);
        btn_save_tag = view.findViewById(R.id.btn_save_tag);
        ll_page_navigaiton = view.findViewById(R.id.ll_page_navigaiton);
        ll_page_navigaiton.setVisibility(GONE);
        ll_selected_advocates = view.findViewById(R.id.ll_selected_advocates);
        scrollView = view.findViewById(R.id.PageScrollView);
        pageNumberLayout = view.findViewById(R.id.pageNumberLayout);
        iv_backward_button = view.findViewById(R.id.iv_backward_button);
        iv_backward_button.setVisibility(VISIBLE);
        iv_forward_button = view.findViewById(R.id.iv_forward_button);
        iv_forward_button.setImageDrawable(getContext().getDrawable(R.drawable.baseline_arrow_forward_ios_24));
        btn_cancel_tag = view.findViewById(R.id.btn_cancel_tag);
        iv_remove_tag = view.findViewById(R.id.iv_remove_tag);
        btn_cancel_tag.setText(R.string.remove);
        et_advocate_name = view.findViewById(R.id.et_advocate_name);
        et_advocate_name.addTextChangedListener(new Validation(et_advocate_name));
        et_advocate_email = view.findViewById(R.id.et_advocate_email);
        et_advocate_email.addTextChangedListener(new Validation(et_advocate_email));
        et_advocate_phone = view.findViewById(R.id.et_advocate_phone);
        matter_id = view.findViewById(R.id.matter_id);
        matter_id.setText(R.string.matter_number);
        et_matter_id = view.findViewById(R.id.et_matter_id);
        et_matter_id.setEnabled(false);

        et_matter_id.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                TooltipCompat.setTooltipText(et_matter_id, "This field is read-only");
                et_matter_id.performLongClick(); // triggers the tooltip
                return true; // consume touch
            }
            return false;
        });

        et_advocate_phone.addTextChangedListener(new Validation(et_advocate_phone));
        InputFilter[] filters = new InputFilter[]{
                new InputFilter.LengthFilter(10)
        };
        et_advocate_phone.setFilters(filters);
        advocate_title = view.findViewById(R.id.advocate_title);
        advocate_title.setText(R.string.name);
        advocate_email = view.findViewById(R.id.advocate_email);
        advocate_email.setText(R.string.email);
        advocate_phone = view.findViewById(R.id.advocate_phone);
        advocate_phone.setText(R.string.phone_number);

        et_matter_title.setTextSize(DynamicUtils.fifteen);
//        cv_add_opponent_advocate = view.findViewById(R.id.cv_add_opponent_advocate);
        et_matter_num = view.findViewById(R.id.et_matter_num);
        if (Constants.MATTER_TYPE.equals("Legal")) {
            ll_case_num.setVisibility(VISIBLE);
            et_matter_num.setHint(R.string.case_number);
        } else {
            ll_case_num.setVisibility(GONE);
            et_matter_num.setHint(R.string.matter_number);
        }
        et_matter_num.addTextChangedListener(new Validation(et_matter_num));

        et_matter_num.setTextSize(DynamicUtils.fifteen);
        btn_cancel_edit = view.findViewById(R.id.btn_cancel_edit);
        tl_case_type = view.findViewById(R.id.tl_case_type);
        et_matter_type = view.findViewById(R.id.et_matter_type);
        tl_case_type.setVisibility(VISIBLE);
        et_matter_type.setVisibility(GONE);
        et_case_type = tl_case_type.findViewById(R.id.tv_spinner_view);
        ImageView img_dropdown_icon = tl_case_type.findViewById(R.id.img_dropdown_icon);
        sp_case_type = view.findViewById(R.id.sp_case_type);
        if (Constants.MATTER_TYPE.equals("Legal")) {
            et_case_type.setHint(R.string.select_case_type);
            ll_matter_type.setVisibility(GONE);
            ll_case_type.setVisibility(VISIBLE);
        } else {
            ll_matter_type.setVisibility(VISIBLE);
            ll_case_type.setVisibility(GONE);
            et_matter_type.setHint(R.string.matter_type);
            et_case_type.setHint(R.string.select_case_type);
        }
        et_case_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCaseTypeChecked)
                    sp_case_type.setVisibility(VISIBLE);
                else sp_case_type.setVisibility(GONE);
                isCaseTypeChecked = !isCaseTypeChecked;
            }
        });
        img_dropdown_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCaseTypeChecked)
                    sp_case_type.setVisibility(VISIBLE);
                else sp_case_type.setVisibility(GONE);
                isCaseTypeChecked = !isCaseTypeChecked;
            }
        });
//        et_case_type.setTextSize(DynamicUtils.fifteen);
//        et_case_type.addTextChangedListener(new Validation(et_case_type));
        description_name = view.findViewById(R.id.description_name);
        description_name.setText(R.string.description);

        Title = view.findViewById(R.id.Title_name);
        Title.setText(R.string.case_title);
        datefill = view.findViewById(R.id.datefill);
        datefill.setText(R.string.date_of_filing);

        create_date = view.findViewById(R.id.create_date);
        create_date.setText(R.string.created_date);

        start_date = view.findViewById(R.id.start_date);
        start_date.setText(R.string.start_date);
        closedate = view.findViewById(R.id.closedate);
        closedate.setText(R.string.close_date);
        court = view.findViewById(R.id.court);
        court.setText(R.string.court);
        judge = view.findViewById(R.id.judge);
        judge.setText(R.string.judge_s);
        priority = view.findViewById(R.id.priority);
        priority.setText(R.string.priority);
        status = view.findViewById(R.id.status);
        status.setText(R.string.status);
        addopponentadvocate = view.findViewById(R.id.addopponentadvocate);
        addopponentadvocate.setText(R.string.opponent_advocates);
        addopponentadvocate.setTextSize(DynamicUtils.eighteen);
        ll_court = view.findViewById(R.id.ll_court);
        ll_judge = view.findViewById(R.id.ll_judge);
        ll_dof = view.findViewById(R.id.ll_dof);
        ll_create_date = view.findViewById(R.id.ll_create_date);
        ll_matter_title = view.findViewById(R.id.ll_matter_title);
        btn_add_more_info = view.findViewById(R.id.btn_add_more_info);
        other_information = view.findViewById(R.id.other_information);
        ll_matter_tags = view.findViewById(R.id.ll_matter_tags);
        tv_matter_tags = view.findViewById(R.id.tv_matter_tags);
        tv_matter_tags.setText(R.string.matter_tags);
        btn_add_tag = view.findViewById(R.id.btn_add_tag);
        btn_add_tag.setBackgroundDrawable(getContext().getDrawable(R.drawable.rectangular_button_green_count));
        et_matter_tags = view.findViewById(R.id.et_matter_tag);
//        et_matter_tags = et_matter_tag.findViewById(R.id.et_content);
//        img_clear_icon = et_matter_tag.findViewById(R.id.img_clear_icon);
//        img_clear_icon.setVisibility(GONE);
//        et_matter_tags.setHint(R.string.matter_tags);
//        et_matter_tags.addTextChangedListener(new Validation(et_matter_tags));
        InputFilter[] tag_filters = new InputFilter[]{
                new InputFilter.LengthFilter(30)
        };
        et_matter_tags.setFilters(tag_filters);
        et_matter_tags.setMaxLines(1);
        et_matter_tags.setHint(R.string.matter_tags);
        m_c_number = view.findViewById(R.id.m_c_number);
        m_c_number.setText(R.string.case_number);
        m_c_type = view.findViewById(R.id.m_c_type);
        m_c_type.setText(R.string.case_type);
        ll_end_date = view.findViewById(R.id.ll_end_date);
        ll_start_date = view.findViewById(R.id.ll_start_date);
        ll_date = view.findViewById(R.id.ll_date);
        tv_start_date = view.findViewById(R.id.tv_start_date);
        tv_start_date.setHint(R.string.start_date);
        tv_start_date.setTextSize(DynamicUtils.fifteen);
        tv_end_date = view.findViewById(R.id.tv_end_date);
        tv_end_date.setHint(R.string.close_date);
        tv_end_date.setTextSize(DynamicUtils.fifteen);
        cv_client_details = view.findViewById(R.id.cv_client_details);
        et_matter_description = view.findViewById(R.id.et_matter_description);
        et_matter_description.setHint(R.string.description);
        et_matter_description.addTextChangedListener(new DescriptionValidation(et_matter_description));
//        et_matter_description.addTextChangedListener(new DescriptionValidation(et_matter_description));
//        et_matter_description.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        filters = new InputFilter[]{new InputFilter.LengthFilter(300)}; // Limit input to 300 characters
        et_matter_description.setFilters(filters);
        et_matter_description.setMaxLines(10);
        tv_dof = view.findViewById(R.id.tv_dof);

        tv_dof.setHint(R.string.date_of_filing);
        tv_dof.setTextSize(DynamicUtils.fifteen);


        tv_create_date = view.findViewById(R.id.tv_create_date);
        tv_create_date.setHint(R.string.created_date);
        tv_create_date.setTextSize(DynamicUtils.fifteen);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        String todayDate = sdf.format(new Date());

// Set text
        tv_create_date.setText(todayDate);
        et_court = view.findViewById(R.id.et_court);
        et_court.setHint(R.string.court);
        et_court.setTextSize(DynamicUtils.fifteen);
        et_court.addTextChangedListener(new Validation(et_court));
        et_judge = view.findViewById(R.id.et_judge);
        et_judge.setHint(R.string.judge_s);
        et_judge.setTextSize(DynamicUtils.fifteen);
        et_judge.addTextChangedListener(new Validation(et_judge));
        tv_high_priority = view.findViewById(R.id.tv_high_priority);
        tv_high_priority.setOnClickListener(this);
        tv_medium_priority = view.findViewById(R.id.tv_medium_priority);
        tv_medium_priority.setOnClickListener(this);
        tv_low_priority = view.findViewById(R.id.tv_low_priority);
        tv_low_priority.setOnClickListener(this);
        tv_status_pending = view.findViewById(R.id.tv_status_pending);
        tv_status_pending.setOnClickListener(this);
        tv_status_active = view.findViewById(R.id.tv_status_active);
        tv_status_active.setOnClickListener(this);
        btn_add_advocate = view.findViewById(R.id.btn_add_advocate);
        btn_add_advocate.setOnClickListener(this);
        btn_cancel_save = view.findViewById(R.id.btn_cancel_save);
        btn_cancel_save.setOnClickListener(this);
//        btn_cancel = view.findViewById(R.id.btn_cancel);
//        btn_cancel.setOnClickListener(this);
        ll_added_tags = view.findViewById(R.id.ll_added_tags);
        btn_create = view.findViewById(R.id.btn_create);
        btn_create.setText(R.string.save_next);
        btn_create.setOnClickListener(this);
        ll_add_advocate = view.findViewById(R.id.ll_add_advocate);
        ll_add_advocate.setVisibility(GONE);
        matter = (Matter) getParentFragment();
        tv_start_date.setInputType(InputType.TYPE_NULL);
        tv_end_date.setInputType(InputType.TYPE_NULL);
        tv_dof.setInputType(InputType.TYPE_NULL);
        tv_response = view.findViewById(R.id.response_name);
        tv_reponse_email = view.findViewById(R.id.response_email);
        tv_response_phone = view.findViewById(R.id.response_phone);
//        Constants.matterInformation = this;
        Constants.matterInformation_en = this;
        SimpleDateFormat sdf1 = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        String todayDate1 = sdf1.format(new Date());

        tv_create_date.setText(todayDate1);
        tv_create_date.setOnClickListener(v -> showDatePicker(tv_create_date, true, true, false, null));
        if (Objects.equals(Constants.MATTER_TYPE, "Legal")) {
            ll_case_num.setVisibility(VISIBLE);
            tl_case_type.setVisibility(VISIBLE);
            et_matter_type.setVisibility(GONE);
            m_c_number.setText(R.string.case_number);
            m_c_type.setText(R.string.case_type);
            ll_court.setVisibility(VISIBLE);
            ll_judge.setVisibility(VISIBLE);
            ll_dof.setVisibility(VISIBLE);
            ll_date.setVisibility(GONE);
            ll_start_date.setVisibility(GONE);
            ll_end_date.setVisibility(GONE);
            ll_header.setVisibility(VISIBLE);
            tv_dof.setOnClickListener(v -> showDatePicker(tv_dof, true, true, true, null));
        } else {
            ll_case_num.setVisibility(GONE);
            tl_case_type.setVisibility(GONE);
            et_matter_type.setVisibility(VISIBLE);
            m_c_number.setText(R.string.matter_number);
            m_c_type.setText(R.string.matter_type);
            ll_court.setVisibility(GONE);
            ll_judge.setVisibility(GONE);
            ll_dof.setVisibility(GONE);
            ll_date.setVisibility(VISIBLE);
            ll_start_date.setVisibility(VISIBLE);
            ll_end_date.setVisibility(VISIBLE);
            ll_header.setVisibility(GONE);
            initDatePickers();
        }
        loadActiveUI();
        loadHighPriorityUI();
        edit_matter_page_txt.setVisibility(GONE);
        ll_matter_title.setVisibility(VISIBLE);
        if (Constants.create_matter) {
            matter_title.setVisibility(GONE);
            iv_remove_matter.setVisibility(GONE);
//            ll_matter_title.setVisibility(VISIBLE);
        } else {
            matter_title.setVisibility(VISIBLE);
            iv_remove_matter.setVisibility(VISIBLE);
//            ll_matter_title.setVisibility(VISIBLE);
        }
        edit_matter_page_icon.setVisibility(GONE);
//        if (viewMatter == null) {
        loadAllDetails();
//        }
        btn_add_more_info.setVisibility(VISIBLE);
        other_information.setVisibility(GONE);
        btn_cancel_save.setText(R.string.save_later);
        btn_create.setText(R.string.save_next);
        //Edit
        iv_forward_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int totalPages = pagebuttons.size(); // real visible pages (including new entry)

                if (currentPage < totalPages - 1) {
                    currentPage++;

                    if (currentPage < advocates_list.size()) {
                        AdvocateModel model = advocates_list.get(currentPage);
                        EditAdvocateUI(model.getAdvocate_name(), model.getEmail(), model.getNumber(), currentPage);
                        loadOpponentsList(false);
                    } else {
                        RefreshAdvocateView(); // new entry slot
                        loadOpponentsList(true);
                    }

                    UpdatePageButton(currentPage);
                } else {
                    Log.d("page_index_f", "Reached the last visible page");
                }
            }
        });


        iv_backward_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage > 0) {
                    currentPage--;
                    AdvocateModel advocateModel = advocates_list.get(currentPage);
                    EditAdvocateUI(advocateModel.getAdvocate_name(), advocateModel.getEmail(), advocateModel.getNumber(), currentPage);
                    UpdatePageButton(currentPage);
                } else {
                    Log.d("page_index_p", "Index ended");
                }
            }
        });

        //..
        iv_remove_tag.setOnClickListener(v -> {
            // Remove or reset the current advocate entry
//            if (currentPage < advocates_list.size()) {
//                // Remove the selected advocate
//                advocates_list.remove(currentPage);
//
//                // Adjust currentPage
//                if (advocates_list.isEmpty()) {
//                    currentPage = 0;
//                    et_advocate_name.setText("");
//                    et_advocate_email.setText("");
//                    et_advocate_phone.setText("");
//                } else {
//                    currentPage = Math.max(0, currentPage - 1);
//                    AdvocateModel model = advocates_list.get(currentPage);
//                    et_advocate_name.setText(model.getAdvocate_name());
//                    et_advocate_email.setText(model.getEmail());
//                    et_advocate_phone.setText(model.getNumber());
//                }
//                loadOpponentsList(true);
//            } else {
            // It was a new entry, just clear fields
//                if (!advocates_list.isEmpty()) {
//                    currentPage = advocates_list.size() - 1;
//                    AdvocateModel model = advocates_list.get(currentPage);
//                    et_advocate_name.setText(model.getAdvocate_name());
//                    et_advocate_email.setText(model.getEmail());
//                    et_advocate_phone.setText(model.getNumber());
//                } else {
//                    currentPage = 0;
//                    et_advocate_name.setText("");
//                    et_advocate_email.setText("");
//                    et_advocate_phone.setText("");
//                    ll_opponent_advocate.setVisibility(View.GONE);
//                }
//                loadOpponentsList(false);
//            }

            // Reset error messages
//            tv_response.setVisibility(View.GONE);
//            tv_reponse_email.setVisibility(View.GONE);
//            tv_response_phone.setVisibility(View.GONE);
//
//            btn_save_tag.setAlpha(1.0f);
//            btn_save_tag.setEnabled(true);
//            AddMatterDetails();
            RefreshAdvocateView();
            loadSelectedView();
        });
        btn_cancel_tag.setOnClickListener(v -> {
            // Remove or reset the current advocate entry
            if (currentPage < advocates_list.size()) {
                // Remove the selected advocate
                advocates_list.remove(currentPage);

                // Adjust currentPage
                if (advocates_list.isEmpty()) {
                    currentPage = 0;
                    et_advocate_name.setText("");
                    et_advocate_email.setText("");
                    et_advocate_phone.setText("");
                } else {
                    currentPage = Math.max(0, currentPage - 1);
                    AdvocateModel model = advocates_list.get(currentPage);
                    et_advocate_name.setText(model.getAdvocate_name());
                    et_advocate_email.setText(model.getEmail());
                    et_advocate_phone.setText(model.getNumber());
                }
                loadOpponentsList(true);
            } else {
                // It was a new entry, just clear fields
                if (!advocates_list.isEmpty()) {
                    currentPage = advocates_list.size() - 1;
                    AdvocateModel model = advocates_list.get(currentPage);
                    et_advocate_name.setText(model.getAdvocate_name());
                    et_advocate_email.setText(model.getEmail());
                    et_advocate_phone.setText(model.getNumber());
                } else {
                    currentPage = 0;
                    et_advocate_name.setText("");
                    et_advocate_email.setText("");
                    et_advocate_phone.setText("");
                    ll_opponent_advocate.setVisibility(GONE);
                }
                loadOpponentsList(false);
            }

            // Reset error messages
            tv_response.setVisibility(GONE);
            tv_reponse_email.setVisibility(GONE);
            tv_response_phone.setVisibility(GONE);

            btn_save_tag.setAlpha(1.0f);
            btn_save_tag.setEnabled(true);
            AddMatterDetails();
        });

        iv_remove_matter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isChangesOccured) {
                    AndroidUtils.showConfirmation(
                            getActivity(),
                            requireContext().getString(R.string.leavepage),
                            requireContext().getString(R.string.changes_you_made_may_not_be_saved),
                            requireContext().getString(R.string.leave),
                            new AndroidUtils.OnConfirmListener() {
                                @Override
                                public void onSave() {
                                    if (!Constants.create_matter) {
                                        nav_view_matter();
                                    } else if (matter != null)
                                        matter.loadViewUI();
                                }

                                @Override
                                public void onCancel() {
                                }
                            }
                    );
                } else {
                    // No details filled — directly go back
                    if (!Constants.create_matter) {
                        nav_view_matter();
                    } else if (matter != null)
                        matter.loadViewUI();
                }
            }
        });
        btn_save_tag.setOnClickListener(v -> {
            if (!validateFields()) return;

            // Add or update the advocate
            if (currentPage < advocates_list.size()) {
                AdvocateModel advocateModel = new AdvocateModel();
                advocateModel.setAdvocate_name(et_advocate_name.getText().toString().trim());
                advocateModel.setEmail(et_advocate_email.getText().toString().trim());
                advocateModel.setNumber(et_advocate_phone.getText().toString().trim());
                advocates_list.set(currentPage, advocateModel);
                loadSelectedView();
            } else {

//                btn_save_tag.setAlpha(0.5f);
//                btn_save_tag.setEnabled(false);

                AdvocateModel advocateModel = new AdvocateModel();
                advocateModel.setAdvocate_name(et_advocate_name.getText().toString().trim());
                advocateModel.setEmail(et_advocate_email.getText().toString().trim());
                advocateModel.setNumber(et_advocate_phone.getText().toString().trim());
                advocates_list.add(advocateModel);
                currentPage = advocates_list.size();
                AddMatterDetails();
                loadSelectedView();
            }

            JSONArray jsonArray = new JSONArray();
            for (AdvocateModel model : advocates_list) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", model.getAdvocate_name());
                    jsonObject.put("email", model.getEmail());
                    jsonObject.put("phone", model.getNumber());
                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            matterModel.setOpponent_advocate(jsonArray);
            loadMatterDetails();
            AddMatterDetails();
        });
        btn_add_more_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMoreDetailsChecked) {
                    callCaseType();
                    other_information.setVisibility(VISIBLE);
                } else
                    other_information.setVisibility(GONE);
//                btn_add_more_info.setVisibility(GONE);
                isMoreDetailsChecked = !isMoreDetailsChecked;
            }
        });
        et_matter_tags.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                if (Constants.create_matter) {
                AddMatterDetails();
//                }
//                if (s.toString().isEmpty()) {
//                    img_clear_icon.setVisibility(GONE);
//                } else {
//                    img_clear_icon.setVisibility(VISIBLE);
//                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                if (s.toString().isEmpty()) {
//                    img_clear_icon.setVisibility(GONE);
//                } else {
//                    img_clear_icon.setVisibility(VISIBLE);
//                }
                AddMatterDetails();
            }
        });
//        img_clear_icon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                et_matter_tags.setText("");
//            }
//        });
        btn_add_tag.setOnClickListener(v -> {
            String newTag = et_matter_tags.getText().toString().trim();
            if (newTag.isEmpty()) {
                Toast.makeText(getContext(), "Enter a tag", Toast.LENGTH_SHORT).show();
                return;
            }

            if (editPosition >= 0 && editPosition < tag_list.size()) {
                tag_list.set(editPosition, newTag);
                editPosition = -1;
            } else {
                // if editPosition was invalid (list cleared), treat as add
                if (!tag_list.contains(newTag)) {
                    tag_list.add(newTag);
                } else {
                    Toast.makeText(getContext(), "Tag already added", Toast.LENGTH_SHORT).show();
                    return;
                }
                btn_add_tag.setText(R.string.add);
            }

            et_matter_tags.setText("");
            btn_add_tag.setText(R.string.add);
//            img_clear_icon.setVisibility(GONE);
            loadMatterTags();
        });
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCancelClicked = false;
                saveMatterInformation();
            }
        });
        if (!Constants.Matter_id.isEmpty()) {
            callEditMatterInfo();
        } else {
            if (Objects.requireNonNull(et_matter_id.getText()).toString().isEmpty())
                getmatterId();
        }
        return view;
    }

    private void loadMatterTags() {
        ll_added_tags.removeAllViews();
        for (int i = 0; i < tag_list.size(); i++) {
            View view_added_tags = LayoutInflater.from(getContext()).inflate(R.layout.displays_documents_list, null);

            LinearLayout ll_tags = view_added_tags.findViewById(R.id.ll_tags);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 10, 10, 10);
            ll_tags.setLayoutParams(params);
            TextView tv_tag_document_name = view_added_tags.findViewById(R.id.tv_document_name);
            ImageView iv_edit_tag = view_added_tags.findViewById(R.id.iv_edit_meta);
            ImageView iv_remove_tag = view_added_tags.findViewById(R.id.iv_cancel);
            iv_edit_tag.setVisibility(VISIBLE);
            String tag_name = tag_list.get(i);
            tv_tag_document_name.setText(tag_name);

            int finalI = i;
            iv_remove_tag.setOnClickListener(v -> {
                tag_list.remove(finalI);

                // If list is empty → reset edit mode
                if (tag_list.isEmpty()) {
                    editPosition = -1;
                    btn_add_tag.setText(R.string.add);
//                    et_matter_tags.setText("");
                }

                loadMatterTags();
            });


            iv_edit_tag.setOnClickListener(v -> {
                btn_add_tag.setText(R.string.update);
                et_matter_tags.setText(tag_list.get(finalI));
                et_matter_tags.setSelection(et_matter_tags.getText().length());
                editPosition = finalI;
            });

            ll_added_tags.addView(view_added_tags);
        }
    }

    private String getAdvocateDisplayText(AdvocateModel model) {
        if (model.getAdvocate_name() != null && !model.getAdvocate_name().trim().isEmpty()) {
            return model.getAdvocate_name();
        }

        if (model.getEmail() != null && !model.getEmail().trim().isEmpty()) {
            return model.getEmail();
        }

        if (model.getNumber() != null && !model.getNumber().trim().isEmpty()) {
            return model.getNumber();
        }

        return "";
    }

    private void loadSelectedView() {
        ll_opponent_advocate.setVisibility(GONE);
        ll_selected_advocates.setVisibility(VISIBLE);
        ll_selected_advocates.removeAllViews();
        for (int i = 0; i < advocates_list.size(); i++) {
            View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.edit_opponent_advocate, null);
            if (view_opponents != null) {
                TextView tv_opponent_name = view_opponents.findViewById(R.id.tv_opponent_name);
                ImageView iv_remove_opponent = view_opponents.findViewById(R.id.iv_remove_opponent);
                ImageView iv_edit_opponent = view_opponents.findViewById(R.id.iv_edit_opponent);
                iv_edit_opponent.setImageDrawable(getContext().getDrawable(R.drawable.edit_new_icon_));

                if (tv_opponent_name != null && iv_remove_opponent != null) {
                    AdvocateModel model = advocates_list.get(i);
                    tv_opponent_name.setText(getAdvocateDisplayText(model));

//                    if (!Constants.create_matter) {
//                        if (advocates_list.get(i).get().equals(Constants.owner_id)) {
//                            iv_remove_opponent.setVisibility(View.INVISIBLE);
//                        }
//                    } else {
                    iv_edit_opponent.setTag(i);
                    iv_edit_opponent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int position = (int) v.getTag();
                            if (position < advocates_list.size()) {
                                AdvocateModel advocateModel = advocates_list.get(position);
                                currentPage = position; // update currentPage
                                EditAdvocateUI(advocateModel.getAdvocate_name(), advocateModel.getEmail(), advocateModel.getNumber(), position);
                            }
                        }
                    });

                    iv_remove_opponent.setTag(i); // Tag each view with its position
                    iv_remove_opponent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                int position = (int) v.getTag();
                                // Remove the view at the specified position
                                ll_selected_advocates.removeViewAt(position);
                                // Remove the corresponding item from the list
                                AdvocateModel teamModel = advocates_list.remove(position);
//                                    teamModel.setChecked(false);
                                // Update the tags of the remaining views
                                for (int j = 0; j < ll_selected_advocates.getChildCount(); j++) {
                                    ImageView iv_remove = ll_selected_advocates.getChildAt(j).findViewById(R.id.iv_remove_opponent);
                                    if (iv_remove != null) {
                                        iv_remove.setTag(j);
                                    }
                                }

//                                    String str = String.join(",", value);
//                                    at_assigned_team_members.setText(str);
//                                    // Update at_add_groups text
//                                    StringBuilder stringBuilder = new StringBuilder();
//                                    for (TeamModel model : selected_tm_list) {
//                                        stringBuilder.append(model.getTm_name()).append(",");
//                                    }

//                                    if (stringBuilder.length() > 0) {
//                                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
//                                    }
//                                    String strn = stringBuilder.toString();
//                                    at_assigned_team_members.setText(strn);
//                                    if (!selected_tm_list.isEmpty()) {
//                                        selected_tm.setVisibility(View.VISIBLE);
//                                    } else {
//                                        selected_tm.setVisibility(View.GONE);
//                                    }
//                                    AddGctDetails();
//                                    AndroidUtils.ToggleButton(temporary_tm_list.size(), btn_add_teammembers);
                            } catch (Exception e) {
                                e.fillInStackTrace();
                                AndroidUtils.showAlert(e.getMessage(), getActivity());
                            }
//                                btn_create.setAlpha(1.0f);
//                                btn_create.setEnabled(true);
                        }
                    });
                    iv_remove_opponent.setVisibility(VISIBLE); // Set visibility here
                }
            }
            ll_selected_advocates.addView(view_opponents);
        }
//        }
    }

    private boolean checkFilledDetails() {
        TextInputEditText[] fields = new TextInputEditText[]{
                et_matter_title, et_matter_num, et_matter_description, et_court, et_judge, et_matter_tags
        };

        for (TextInputEditText field : fields) {
            if (field != null && field.getText() != null && !field.getText().toString().trim().isEmpty()) {
                return true; // At least one field is filled
            }
        }
        return false; // None of the fields are filled
    }


    private void loadAllDetails() {
        if (matter != null) {
            matterArraylist = matter.getMatter_arraylist();
            if (!matterArraylist.isEmpty()) {
                for (int i = 0; i < matterArraylist.size(); i++) {
                    matterModel = matterArraylist.get(i);

                    if (matterModel.getGroup_acls() != null) {
                        exisiting_group_acls = matterModel.getGroup_acls();
                    }
                    if (matterModel.getClients() != null) {
                        existing_clients = matterModel.getClients();
                    }
                    if (matterModel.getMembers() != null) {
                        existing_members = matterModel.getMembers();
                    }
                    if (matterModel.getCorp_clients_list() != null) {
                        existing_corp_clients = matterModel.getCorp_clients_list();
                    }
                    if (matterModel.getGroups_list() != null) {
                        existing_groups_list = matterModel.getGroups_list();
                    }
                    if (matterModel.getTemp_clients_list() != null) {
                        existing_temp_list = matterModel.getTemp_clients_list();
                    }
                    if (matterModel.getClients_list() != null) {
                        existing_clients_list = matterModel.getClients_list();
                    }
                    if (matterModel.getMembers_list() != null) {
                        existing_tm_list = matterModel.getMembers_list();
                    }
                    if (matterModel.getDocuments() != null) {
                        existing_documents = matterModel.getDocuments();
                    }
                    if (matterModel.getDocuments_list() != null) {
                        existing_documents_list = matterModel.getDocuments_list();
                    }
                    if (matterModel.getTags_list() != null) {
                        existing_tags_list = matterModel.getTags_list();
                    }
                    tag_list.clear();
                    if (existing_tags_list != null && existing_tags_list.length() > 0) {
                        for (int j = 0; j < existing_tags_list.length(); j++) {
                            try {
                                tag_list.add(existing_tags_list.getString(j));
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    if (!Constants.GeneratedMatterId.isEmpty()) {
                        et_matter_id.setText(Constants.GeneratedMatterId);
                    }

                    loadMatterTags();

                    if (matterModel.getMatter_title() != null) {
                        et_matter_title.setText(matterModel.getMatter_title());
                    } else {
                        et_matter_title.setText("");
                    }
                    if (matterModel.getCase_number() != null) {
                        et_matter_num.setText(matterModel.getCase_number());
                    } else {
                        et_matter_num.setText("");
                    }
                    if (matterModel.getCase_type() != null) {
                        et_case_type.setText(matterModel.getCase_type());
                        et_matter_type.setText(matterModel.getCase_type());
                    } else {
                        et_matter_type.setText("");
                        et_case_type.setText("");
                    }
                    if (matterModel.getDescription() != null) {
                        et_matter_description.setText(matterModel.getDescription());
                    } else {
                        et_matter_description.setText("");
                    }
                    if (matterModel.getDate_of_filing() != null) {
                        tv_dof.setText(matterModel.getDate_of_filing());
                    } else {
                        tv_dof.setOnClickListener(v -> showDatePicker(tv_dof, true, true, true, null));
                    }
                    if (matterModel.getCreated_date() != null) {
                        tv_create_date.setText(matterModel.getCreated_date());
                    } else {
                        tv_create_date.setOnClickListener(v -> showDatePicker(tv_create_date, true, true, false, null));
                    }
                    if (matterModel.getStart_date() != null) {
                        tv_start_date.setText(matterModel.getStart_date());
                    } else {
                        tv_start_date.setOnClickListener(v -> showDatePicker(tv_start_date, true));
                        AndroidUtils.validateStartAndEndDates(getActivity(), tv_start_date, tv_end_date, "MMM dd, yyyy");
                    }
                    if (matterModel.getEnd_date() != null) {
                        tv_end_date.setText(matterModel.getEnd_date());
                    } else {
                        tv_end_date.setOnClickListener(v -> showDatePicker(tv_end_date, false));
                        AndroidUtils.validateStartAndEndDates(getActivity(), tv_start_date, tv_end_date, "MMM dd, yyyy");
                    }
                    if (matterModel.getCourt() != null) {
                        et_court.setText(matterModel.getCourt());
                    } else {
                        et_court.setText("");
                    }
                    if (matterModel.getJudge() != null) {
                        et_judge.setText(matterModel.getJudge());
                    } else {
                        et_judge.setText("");
                    }
                    if (matterModel.getCase_priority() != null) {
                        CASE_PRIORITY = matterModel.getCase_priority();
                    }
                    if (matterModel.getStatus() != null) {
                        STATUS = matterModel.getStatus();
                    }

                    // ← FIX: Only load opponents from matterArraylist cache during CREATE flow.
                    //         During EDIT flow, load_existing_advocates() handles this from
                    //         viewMatterModel1 to avoid duplication.
                    if (Constants.create_matter) {
                        if (matterArraylist.get(i).getOpponent_advocate() != null) {
                            advocates_list.clear();
                            existing_opponents = matterArraylist.get(i).getOpponent_advocate();
                            try {
                                if (existing_opponents.length() > 0) {
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
                                    loadSelectedView();
                                }
                            } catch (Exception e) {
                                e.fillInStackTrace();
                                AndroidUtils.showAlert(e.getMessage(), getActivity());
                            }
                        }
                    }
                }
                if (Objects.equals(CASE_PRIORITY, "High")) {
                    loadHighPriorityUI();
                } else if (Objects.equals(CASE_PRIORITY, "Medium")) {
                    loadMediumPriorityUI();
                } else {
                    loadLowPriorityUI();
                }
                if (Objects.equals(STATUS, "Active")) {
                    loadActiveUI();
                } else {
                    loadPendingUI();
                }
            }
            isInitialLoad = false;
        }
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (isInitialLoad && !Constants.create_matter) return;
                AddMatterDetails();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };

        et_matter_title.addTextChangedListener(watcher);
        et_matter_num.addTextChangedListener(watcher);
        et_matter_type.addTextChangedListener(watcher);
        et_matter_tags.addTextChangedListener(watcher);
        et_matter_type.addTextChangedListener(watcher);
        et_matter_description.addTextChangedListener(watcher);
        tv_dof.addTextChangedListener(watcher);
        tv_create_date.addTextChangedListener(watcher);
        tv_start_date.addTextChangedListener(watcher);
        tv_end_date.addTextChangedListener(watcher);
        et_court.addTextChangedListener(watcher);
        et_judge.addTextChangedListener(watcher);
    }

    private void load_existing_advocates() {
        advocates_list.clear(); // ← FIX: prevent duplication on re-entry

        JSONArray advocates = viewMatterModel1.getOpponentAdvocates();
        if (advocates != null) {
            for (int i = 0; i < advocates.length(); i++) {
                JSONObject client_value = null;
                try {
                    client_value = advocates.getJSONObject(i);
                    ad_name = client_value.getString("name");
                    ad_email = client_value.getString("email");
                    ad_phone = client_value.getString("phone");
                    Log.d("Advocate_value_name", ad_name + ad_email + ad_phone);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                AdvocateModel advocateModel = new AdvocateModel();
                advocateModel.setAdvocate_name(ad_name);
                advocateModel.setEmail(ad_email);
                advocateModel.setNumber(ad_phone);
                advocates_list.add(advocateModel);
                loadMatterDetails();
            }
            if (!advocates_list.isEmpty()) {
                loadSelectedView();
            } else {
                ll_opponent_advocate.setVisibility(GONE);
            }
        }
    }

    private void UpdateMatterInfo() {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject postdata = new JSONObject();
            for (int i = 0; i < advocates_list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                AdvocateModel advocateModel = advocates_list.get(i);
                jsonObject.put("name", advocateModel.getAdvocate_name());
                jsonObject.put("email", advocateModel.getEmail());
                jsonObject.put("phone", advocateModel.getNumber());
                advocates.put(jsonObject);
            }
//            for (int i = 0; i < viewMatterModel1.getGroup_acls().length(); i++) {
//                ViewMatterModel viewMatterModel1 = (ViewMatterModel) viewMatter1.viewMatterModel1.getGroup_acls().get(i);
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("id", viewMatterModel1.getGroup_id());
//                jsonObject.put("name", viewMatterModel1.getGroup_name());
//                jsonObject.put("canDelete", viewMatterModel1.isCanDelete());
//                groups.put(jsonObject);
//                group_acls.put(viewMatterModel1.getGroup_id());
//            }
            postdata.put("affidavit_filing_date", "");
            postdata.put("affidavit_isfiled", "");
            if (Constants.MATTER_TYPE.equals("Legal")) {
                //Rectify the un Parseable date.......
                if (tv_dof.getText().toString().trim().isEmpty()) {
                    postdata.put("case_number", Objects.requireNonNull(et_matter_num.getText()).toString());
                    postdata.put("judges", Objects.requireNonNull(et_judge.getText()).toString());
                    postdata.put("case_type", Objects.requireNonNull(et_case_type.getText()).toString());
                    postdata.put("opponent_advocates", advocates);
                    postdata.put("court_name", Objects.requireNonNull(et_court.getText()).toString());
                    postdata.put("date_of_filling", "");
                } else {
                    postdata.put("case_number", Objects.requireNonNull(et_matter_num.getText()).toString());//Updating the matter number
                    postdata.put("judges", Objects.requireNonNull(et_judge.getText()).toString());
                    postdata.put("case_type", Objects.requireNonNull(et_case_type.getText()).toString());
                    postdata.put("opponent_advocates", advocates);
                    postdata.put("court_name", Objects.requireNonNull(et_court.getText()).toString());
                    String date_of_filling = AndroidUtils.convertAnyDateToDDMMYYYY(tv_dof.getText().toString().trim());
                    postdata.put("date_of_filling", date_of_filling);
                }
                //.......
            } else {
                String closeDate = tv_end_date.getText().toString();
                String startDate = tv_start_date.getText().toString();

                if (closeDate.trim().isEmpty()) {
                    postdata.put("closedate", "");
                } else {
//                    SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
//                    Date date = inputFormat.parse(closeDate);
//                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
                    String date_of_filling = AndroidUtils.convertAnyDateToDDMMYYYY(closeDate);
                    postdata.put("closedate", date_of_filling);
                }
                if (startDate.trim().isEmpty()) {
                    postdata.put("startdate", "");
                } else {
//                    SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
//                    Date date = inputFormat.parse(startDate);
//                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
                    String date_of_filling = AndroidUtils.convertAnyDateToDDMMYYYY(startDate);
                    postdata.put("startdate", date_of_filling);
                }
//                    SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd");
//                    Date date2 = inputFormat2.parse(startdate);
//                    SimpleDateFormat outputFormat2 = new SimpleDateFormat("dd-MM-yyyy");
//                    String new_start_date = outputFormat2.format(date2);
                postdata.put("matter_type", Objects.requireNonNull(et_matter_type.getText()).toString());
                postdata.put("matter_number", Objects.requireNonNull(et_matter_num.getText()).toString());
            }
            postdata.put("matter_id", et_matter_id.getText().toString());
            String date_of_filling = AndroidUtils.convertAnyDateToDDMMYYYY(tv_create_date.getText().toString().trim());
            postdata.put("created_date", date_of_filling);
            JSONObject tagsObject = new JSONObject();

            for (int i = 0; i < tag_list.size(); i++) {
                try {
                    tagsObject.put(String.valueOf(i), tag_list.get(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
//            postdata.put("clients", clients);
            postdata.put("description", Objects.requireNonNull(et_matter_description.getText()).toString());//updating the matter description
//            postdata.put("documents", documents);
//            postdata.put("group_acls", group_acls);
//            postdata.put("members", members);
            postdata.put("priority", CASE_PRIORITY);
            postdata.put("status", STATUS);
            postdata.put("title", Objects.requireNonNull(et_matter_title.getText()).toString());
            postdata.put("tags", tagsObject);
//            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "/matter/legal/update/64ad2f54a1db7203e4fd6014", "Edit Document",postdata.toString());
//            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "matter/" + Constants.MATTER_TYPE.toLowerCase(Locale.ROOT) + "/update/" + viewMatterModel1.getId(), "Update Matter", postdata.toString());
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PATCH, "v2/matter/" + Constants.MATTER_TYPE.toLowerCase(Locale.ROOT) + "/" + Constants.Matter_id, "Update Matter", postdata.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                progress_dialog.dismiss();
                AndroidUtils.showAlert(e.getMessage(), getActivity());
            }
            e.fillInStackTrace();
        }
    }

    private void CreateMatterInfo() {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject postdata = new JSONObject();
            for (int i = 0; i < advocates_list.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                AdvocateModel advocateModel = advocates_list.get(i);
                jsonObject.put("name", advocateModel.getAdvocate_name());
                jsonObject.put("email", advocateModel.getEmail());
                jsonObject.put("phone", advocateModel.getNumber());
                advocates.put(jsonObject);
            }
//            for (int i = 0; i < viewMatterModel1.getGroup_acls().length(); i++) {
//                ViewMatterModel viewMatterModel1 = (ViewMatterModel) viewMatter1.viewMatterModel1.getGroup_acls().get(i);
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("id", viewMatterModel1.getGroup_id());
//                jsonObject.put("name", viewMatterModel1.getGroup_name());
//                jsonObject.put("canDelete", viewMatterModel1.isCanDelete());
//                groups.put(jsonObject);
//                group_acls.put(viewMatterModel1.getGroup_id());
//            }
            postdata.put("affidavit_filing_date", "");
            postdata.put("affidavit_isfiled", "");
            if (Constants.MATTER_TYPE.equals("Legal")) {
                //Rectify the un Parseable date.......
                if (tv_dof.getText().toString().trim().isEmpty()) {
                    postdata.put("case_number", Objects.requireNonNull(et_matter_num.getText()).toString());
                    postdata.put("judges", Objects.requireNonNull(et_judge.getText()).toString());
                    postdata.put("case_type", Objects.requireNonNull(et_case_type.getText()).toString());
                    postdata.put("opponent_advocates", advocates);
                    postdata.put("court_name", Objects.requireNonNull(et_court.getText()).toString());
                    postdata.put("date_of_filling", "");
                } else {
                    postdata.put("case_number", Objects.requireNonNull(et_matter_num.getText()).toString());//Updating the matter number
                    postdata.put("judges", Objects.requireNonNull(et_judge.getText()).toString());
                    postdata.put("case_type", Objects.requireNonNull(et_case_type.getText()).toString());
                    postdata.put("opponent_advocates", advocates);
                    postdata.put("court_name", Objects.requireNonNull(et_court.getText()).toString());
                    String date_of_filling = AndroidUtils.convertAnyDateToDDMMYYYY(tv_dof.getText().toString().trim());
                    postdata.put("date_of_filling", date_of_filling);
                }
                //.......
            } else {
                String closeDate = tv_end_date.getText().toString();
                String startDate = tv_start_date.getText().toString();

                if (closeDate.trim().isEmpty()) {
                    postdata.put("closedate", "");
                } else {
//                    SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
//                    Date date = inputFormat.parse(closeDate);
//                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
//                    String date_of_filling = outputFormat.format(date);
                    String date_of_filling = AndroidUtils.convertAnyDateToDDMMYYYY(closeDate);
                    postdata.put("closedate", date_of_filling);
                }
                if (startDate.trim().isEmpty()) {
                    postdata.put("startdate", "");
                } else {
//                    SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
//                    Date date = inputFormat.parse(startDate);
//                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
//                    String date_of_filling = outputFormat.format(date);
                    String date_of_filling = AndroidUtils.convertAnyDateToDDMMYYYY(startDate);
                    postdata.put("startdate", date_of_filling);
                }
//                    SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd");
//                    Date date2 = inputFormat2.parse(startdate);
//                    SimpleDateFormat outputFormat2 = new SimpleDateFormat("dd-MM-yyyy");
//                    String new_start_date = outputFormat2.format(date2);
                postdata.put("matter_type", Objects.requireNonNull(et_matter_type.getText()).toString());
                postdata.put("matter_number", Objects.requireNonNull(et_matter_num.getText()).toString());
            }
            postdata.put("matter_id", et_matter_id.getText().toString());
            String date_of_filling = AndroidUtils.convertAnyDateToDDMMYYYY(tv_create_date.getText().toString().trim());
            postdata.put("created_date", date_of_filling);
            JSONObject tagsObject = new JSONObject();

            for (int i = 0; i < tag_list.size(); i++) {
                try {
                    tagsObject.put(String.valueOf(i), tag_list.get(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
//            postdata.put("clients", clients);
            postdata.put("description", Objects.requireNonNull(et_matter_description.getText()).toString());//updating the matter description
//            postdata.put("documents", documents);
//            postdata.put("group_acls", group_acls);
//            postdata.put("members", members);
            postdata.put("priority", CASE_PRIORITY);
            postdata.put("status", STATUS);
            postdata.put("title", Objects.requireNonNull(et_matter_title.getText()).toString());
            postdata.put("tags", tagsObject);
//            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "/matter/legal/update/64ad2f54a1db7203e4fd6014", "Edit Document",postdata.toString());
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v2/matter/" + Constants.MATTER_TYPE.toLowerCase(Locale.ROOT), "Create Matter", postdata.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                progress_dialog.dismiss();
                AndroidUtils.showAlert(e.getMessage(), getActivity());
            }
            e.fillInStackTrace();
        }
    }

    private void callCaseType() {
//        /v2/matter/casetypes/
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

    private void getmatterId() {
//        /v2/matter/casetypes/
        progress_dialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v2/generate/matterid".toLowerCase(Locale.ROOT), "Matter Id", postdata.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                progress_dialog.dismiss();
                AndroidUtils.showAlert(e.getMessage(), getActivity());
            }
            e.fillInStackTrace();
        }
    }

    //        loadPage(currentPage);
//    private void datePickerStartDate() {
//        final Calendar myCalendar = Calendar.getInstance();
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
//                if (viewMatter != null) {
//                    btn_create.setAlpha(1.0f);
//                    btn_create.setEnabled(true);
//                }
//                tv_start_date.setText(sdf.format(myCalendar.getTime()));
//            }
//        };
//
//        tv_start_date.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int selectedYear = myCalendar.get(Calendar.YEAR);
//                int selectedMonth = myCalendar.get(Calendar.MONTH);
//                int selectedDay = myCalendar.get(Calendar.DAY_OF_MONTH);
//                DatePickerDialog dialog = new DatePickerDialog(
//                        v.getContext(),
//                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
//                        date,
//                        selectedYear, selectedMonth, selectedDay);
//                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                dialog.show();
//            }
//        });
//    }

    public void CheckUnique() {
//        loadAllDetails();
        progress_dialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject postdata = new JSONObject();
//            if (viewMatter != null)
//                if (viewMatterModel1.getId() != null)
            if (!Constants.Matter_id.isEmpty())
                postdata.put("matter_id", Constants.Matter_id);

//            if (Constants.MATTER_TYPE.toLowerCase(Locale.ROOT).equals("general")) {
//                postdata.put("matter_number", Objects.requireNonNull(et_matter_num.getText()).toString());
//            } else {
//                postdata.put("case_number", Objects.requireNonNull(et_matter_num.getText()).toString());
//            }
            postdata.put("title", Objects.requireNonNull(et_matter_title.getText()).toString());
            postdata.put("type", Constants.MATTER_TYPE.toLowerCase(Locale.ROOT));
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "matter/check/unique", "Unique", postdata.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    //    private void datePickerEndDate() {
//        final Calendar[] myCalendar = {Calendar.getInstance()};
//        Calendar finalMyCalendar = myCalendar[0];
//        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
//            @Override
//            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//                finalMyCalendar.set(Calendar.YEAR, year);
//                finalMyCalendar.set(Calendar.MONTH, month);
//                finalMyCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//                updateLabel();
//            }
//
//            private void updateLabel() {
//                String myFormat = "dd-MM-yyyy";
//                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
//                if (viewMatter != null) {
//                    btn_create.setAlpha(1.0f);
//                    btn_create.setEnabled(true);
//                }
//                tv_dof.setText(sdf.format(finalMyCalendar.getTime()));
//            }
//        };
//        // Declare a global variable to store the selected date
//        final int[] selectedYear = new int[1];
//        final int[] selectedMonth = new int[1];
//        final int[] selectedDay = new int[1];
//        tv_end_date.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                myCalendar[0] = Calendar.getInstance();
//                selectedYear[0] = myCalendar[0].get(Calendar.YEAR);
//                selectedMonth[0] = myCalendar[0].get(Calendar.MONTH);
//                selectedDay[0] = myCalendar[0].get(Calendar.DAY_OF_MONTH);
//                DatePickerDialog dialog;
//                dialog = new DatePickerDialog(
//                        v.getContext(),
//                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
//                        mDateSetListener,
//                        selectedYear[0], selectedMonth[0], selectedDay[0]);
//                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                dialog.show();
//            }
//        });
//
//        mDateSetListener = (view, year, month, dayOfMonth) -> {
//            month = month + 1;
//            Log.d(TAG, "onDataSet : mm/dd/yy : " + month + "/" + dayOfMonth + "/" + year);
//            updateDateInViews(year, month, dayOfMonth);
//        };
//    }

    private void initDatePickers() {
        tv_start_date.setOnClickListener(v -> showDatePicker(tv_start_date, true));
        tv_end_date.setOnClickListener(v -> showDatePicker(tv_end_date, false));
        AndroidUtils.validateStartAndEndDates(getActivity(), tv_start_date, tv_end_date, "MMM dd, yyyy");
//        if (!tv_start_date.getText().toString().isEmpty())
//            if (viewMatter != null) {
//                btn_create.setAlpha(1.0f);
//                btn_create.setEnabled(true);
//            }
    }


    private void updateDateInViews(int year, int month, int dayOfMonth) {
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Calendar selectedCalendar = Calendar.getInstance();
        selectedCalendar.set(year, month - 1, dayOfMonth);
        String formattedDate = df.format(selectedCalendar.getTime());
//        if (viewMatter != null) {
//            btn_create.setAlpha(1.0f);
//            btn_create.setEnabled(true);
//        }
        tv_end_date.setText(formattedDate);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_high_priority:
                PriorityStatus();
                loadHighPriorityUI();
                break;
            case R.id.tv_medium_priority:
                PriorityStatus();
                loadMediumPriorityUI();
                break;
            case R.id.tv_low_priority:
                PriorityStatus();
                loadLowPriorityUI();
                break;
            case R.id.tv_status_pending:
                Status();
                loadPendingUI();
                break;
            case R.id.tv_status_active:
                Status();
                loadActiveUI();
                break;
            case R.id.btn_add_advocate:
//                btn_save_tag.setAlpha(1.0f);
//                btn_save_tag.setEnabled(true);
                // Clear input and hide previous validation
                RefreshAdvocateView();
                currentPage = advocates_list.size(); // Move to next index for new entry
                loadAdvocateUI();
                break;

            case R.id.btn_create:
                isCancelClicked = false;
                saveMatterInformation();
                break;
            case R.id.btn_cancel_save:
                isCancelClicked = true;
                saveMatterInformation();
//                loadCancelMatter();
//                } else if (viewMatter != null) {
//                    nav_view_matter();
//                }
                break;

//            case R.id.btn_cancel:
//                ll_opponent_advocate.setVisibility(View.GONE);
//                btn_create.setAlpha(1.0f);
//                btn_create.setEnabled(true);
//                break;
        }
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progress_dialog != null && progress_dialog.isShowing())
            AndroidUtils.dismiss_dialog(progress_dialog);
        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                if (httpResult.getRequestType().equals("Update Matter")) {
                    String message = result.getString("msg");
//                    AndroidUtils.showAlert(message, getActivity());
                    if (!result.getBoolean("error")) {
//                        if (viewMatter != null) {
//                            AndroidUtils.showAlert(message, getActivity(), "Success");
//                            nav_view_matter();
//                        } else {
                        loadMatterDetails();
                        // matterModel_info = matterModel;
//                            AndroidUtils.showConfirmDialog(
//                                    getActivity(),
//                                    "Success",
//                                    "Congratulations! You have successfully created the matter.",
//                                    requireContext().getString(R.string.add_more_details),
//                                    requireContext().getString(R.string.view_matter_list),
//                                    new AndroidUtils.OnConfirmListener() {
//                                        @Override
//                                        public void onSave() {
//                                            matter.loadViewUI();
//                                        }
//
//                                        @Override
//                                        public void onCancel() {
//                                            cv_client_details.setVisibility(GONE);
//                                            matter.loadGCT();
//                                        }
//                                    }
//                            );
                        AndroidUtils.showToast(message, getActivity());
                        if (isCancelClicked) {
                            matter.loadViewUI();
                        } else {
                            matter.loadGCT();
                        }
                        AddMatterDetails();
                        Constants.GeneratedMatterId = et_matter_id.getText().toString();
                        Constants.GeneratedMatterTitle = et_matter_title.getText().toString();
//                        }
//                        }
                    } else {
//                        AndroidUtils.showToast("Matter Not Updated. Please Check the filled Details", getContext());
                        AndroidUtils.showAlert(message, getActivity(), "");
                    }
                } else if (httpResult.getRequestType().equals("Create Matter")) {
                    if (!result.getBoolean("error")) {
                        Constants.Matter_id = result.optString("matter_id");
                        String message = result.getString("msg");
//                        AndroidUtils.showAlert(message, getActivity());
//                        nav_view_matter();
                        loadMatterDetails();
                        // matterModel_info = matterModel;
//                        AndroidUtils.showConfirmDialog(
//                                getActivity(),
//                                "Success",
//                                "Congratulations! You have successfully created the matter.",
//                                requireContext().getString(R.string.add_more_details),
//                                requireContext().getString(R.string.view_matter_list),
//                                new AndroidUtils.OnConfirmListener() {
//                                    @Override
//                                    public void onSave() {
//                                        matter.loadViewUI();
//                                    }
//
//                                    @Override
//                                    public void onCancel() {
//                                        cv_client_details.setVisibility(GONE);
//                                        matter.loadGCT();
//                                    }
//                                }
//                        );
                        AndroidUtils.showToast(message, getActivity());
                        if (isCancelClicked) {
                            matter.loadViewUI();
                        } else {
                            matter.loadGCT();
                        }
                        AddMatterDetails();
                        Constants.GeneratedMatterId = et_matter_id.getText().toString();
                        Constants.GeneratedMatterTitle = et_matter_title.getText().toString();
                    } else {
                        String message = result.getString("msg");
//                        AndroidUtils.showToast("Matter Not Updated. Please Check the filled Details", getContext());
                        AndroidUtils.showAlert(message, getActivity(), "");
                    }
                } else if (httpResult.getRequestType().equals("Edit Matter")) {
                    if (result.getBoolean("error")) {
                        String msg = result.getString("msg");
                        AndroidUtils.showAlert(msg, getActivity());
                    } else {
                        JSONObject matters = result.optJSONObject("matter");
                        try {
                            assert matters != null;
                            loadeditmatter(matters);
                        } catch (Exception e) {
                            AndroidUtils.showAlert(e.getMessage(), getActivity());
                            e.fillInStackTrace();
                        }
                    }
                } else if (httpResult.getRequestType().equals("Unique")) {
                    if (!result.getBoolean("error")) {
                        String message = result.getString("msg");
                        if (matter != null) {
                            if (Constants.Matter_id.isEmpty())
                                CreateMatterInfo();
                            else
                                UpdateMatterInfo();
//                            submitMatter();
                        } else if (!Constants.create_matter) {
                            UpdateMatterInfo();
                        }
                    } else {
                        String message = result.getString("msg");
                        AndroidUtils.showAlert(message, getActivity(), "");
                    }
                } else if (httpResult.getRequestType().equals("Case Type")) {
                    if (!result.getBoolean("error")) {
                        JSONArray jsonArray = result.getJSONArray("data");
                        caseTypeList.clear();  // optional, if you want to reset list first
                        for (int i = 0; i < jsonArray.length(); i++) {
                            caseTypeList.add(jsonArray.getString(i));
                        }
                        if (!caseTypeList.isEmpty())
                            loadCaseTypes();
                    } else {
                        String message = result.getString("msg");
                        AndroidUtils.showAlert(message, getActivity(), "");
                    }
                } else if (httpResult.getRequestType().equals("Matter Id")) {
                    if (!result.getBoolean("error")) {
                        String matter_id = result.optString("matter_id");
                        et_matter_id.setText(matter_id);
                    } else {
                        String message = result.optString("msg");
//                        AndroidUtils.showToast("Matter Not Updated. Please Check the filled Details", getContext());
                        AndroidUtils.showAlert(message, getActivity(), "");
                    }
                }
            } catch (Exception e) {
                if (progress_dialog != null && progress_dialog.isShowing())
                    AndroidUtils.dismiss_dialog(progress_dialog);
                e.fillInStackTrace();
            }
        } else {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                String message = "Request Failed, Try Again";
                if (result.has("error")) {
                    if (result.has("msg")) {
                        String msg = result.optString("msg");
//                        AndroidUtils.showToast("Matter Not Updated. Please Check the filled Details", getContext());
                        AndroidUtils.showAlert(message, getActivity());
                    } else {
                        AndroidUtils.showAlert(message, getActivity());
                    }
                } else {
//                        AndroidUtils.showToast("Matter Not Updated. Please Check the filled Details", getContext());
                    AndroidUtils.showAlert(message, getActivity());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void loadEditMatterDetails() {
        isInitialLoad = true; // ← SET TRUE at the start to block change detection during load
        matter_date.setText(viewMatterModel1.getCreated());
        if (Constants.create_matter) {
            matter_title.setVisibility(GONE);
            iv_remove_matter.setVisibility(GONE);
        } else {
            matter_title.setVisibility(VISIBLE);
            iv_remove_matter.setVisibility(VISIBLE);
        }
        et_matter_title.setText(viewMatterModel1.getTitle());
        matter_title.setText(Objects.requireNonNull(et_matter_title.getText()).toString());
        if (Objects.equals(Constants.MATTER_TYPE, "Legal")) {
            et_matter_num.setText(viewMatterModel1.getCaseNumber());
        } else {
            et_matter_num.setText(viewMatterModel1.getMatterNumber());
        }
        if (Objects.equals(Constants.MATTER_TYPE, "Legal")) {
            et_case_type.setText(viewMatterModel1.getCasetype());
        } else {
            et_matter_type.setText(viewMatterModel1.getMatterType());
        }
        et_matter_description.setText(viewMatterModel1.getDescription());
        et_court.setText(viewMatterModel1.getCourtName());
        et_judge.setText(viewMatterModel1.getJudges());
        if (viewMatterModel1.getCreated_date().trim().isEmpty())
            tv_create_date.setText("");
        else {
            try {
                tv_create_date.setText(viewMatterModel1.getCreated_date());
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        }
        Constants.GeneratedMatterId = viewMatterModel1.getMatter_id();
        Constants.GeneratedMatterTitle = viewMatterModel1.getTitle();
        if (Constants.GeneratedMatterId != null) {
            et_matter_id.setText(Constants.GeneratedMatterId);
        }
        if (Constants.MATTER_TYPE.equals("Legal")) {
            if (viewMatterModel1.getDate_of_filling().trim().isEmpty())
                tv_dof.setText("");
            else {
                try {
                    String inputDate = viewMatterModel1.getDate_of_filling();
                    tv_dof.setText(inputDate);
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            }
        } else {
            String inputDate = viewMatterModel1.getStartdate();
            String inputDate2 = viewMatterModel1.getClosedate();
            if (inputDate.trim().isEmpty()) {
                tv_start_date.setText("");
            } else {
                tv_start_date.setText(inputDate);
            }
            if (inputDate2.trim().isEmpty())
                tv_end_date.setText("");
            else {
                tv_end_date.setText(inputDate2);
            }
        }

        //Loading existing chosen elements list...
        try {
            groups = viewMatterModel1.getGroups();
            group_acls = viewMatterModel1.getGroupAcls();
            for (int i = 0; i < viewMatterModel1.getClients().length(); i++) {
                JSONObject client_list = new JSONObject();
                JSONObject jsonObject = viewMatterModel1.getClients().getJSONObject(i);
                client_list.put("id", jsonObject.getString("id"));
                client_list.put("type", jsonObject.getString("type"));
                clients.put(client_list);
            }
            if (!viewMatterModel1.getCorpId().isEmpty()) {
                JSONObject client_list = new JSONObject();
                client_list.put("id", viewMatterModel1.getCorpId());
                client_list.put("type", "corporate");
                clients.put(client_list);
            }
            for (int i = 0; i < viewMatterModel1.getDocuments().length(); i++) {
                JSONObject document_list = new JSONObject();
                JSONObject jsonObject = viewMatterModel1.getDocuments().getJSONObject(i);
                document_list.put("docid", jsonObject.getString("docid"));
                document_list.put("doctype", jsonObject.getString("doctype"));
                document_list.put("user_id", jsonObject.getString("user_id"));
                documents.put(document_list);
            }
            for (int i = 0; i < viewMatterModel1.getMembers().length(); i++) {
                JSONObject member_list = new JSONObject();
                JSONObject jsonObject = viewMatterModel1.getMembers().getJSONObject(i);
                member_list.put("id", jsonObject.getString("id"));
                members.put(member_list);
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        if (viewMatterModel1.getPriority() != null) {
            CASE_PRIORITY = viewMatterModel1.getPriority();
            oldPriority = CASE_PRIORITY;
        }
        if (viewMatterModel1.getStatus() != null) {
            STATUS = viewMatterModel1.getStatus();
            oldStatus = STATUS;
        }
        if (Objects.equals(CASE_PRIORITY, "High")) {
            loadHighPriorityUI();
        } else if (Objects.equals(CASE_PRIORITY, "Medium")) {
            loadMediumPriorityUI();
        } else {
            loadLowPriorityUI();
        }
        if (Objects.equals(STATUS, "Active")) {
            loadActiveUI();
        } else {
            loadPendingUI();
        }
        if (Constants.MATTER_TYPE.equals("Legal")) {
            ll_date.setVisibility(GONE);
            ll_start_date.setVisibility(GONE);
            ll_end_date.setVisibility(GONE);
            ll_dof.setVisibility(VISIBLE);
            ll_header.setVisibility(VISIBLE);
            btn_create.setText(R.string.save_next);
            load_existing_advocates();
            tv_dof.setOnClickListener(v -> showDatePicker(tv_dof, true, true, true, null));
        } else {
            ll_date.setVisibility(VISIBLE);
            ll_start_date.setVisibility(VISIBLE);
            ll_end_date.setVisibility(VISIBLE);
            ll_dof.setVisibility(GONE);
            ll_header.setVisibility(GONE);
            btn_create.setText(R.string.save_next);
            initDatePickers();
        }
        TextInputEditText[] fields = new TextInputEditText[]{
                et_matter_title, et_matter_num, et_matter_description, et_court, et_judge, et_matter_tags
        };

        final HashMap<TextInputEditText, String> initialValues = new HashMap<>();
        for (TextInputEditText field : fields) {
            initialValues.put(field, field.getText() != null ? field.getText().toString() : "");
        }

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                for (TextInputEditText field : fields) {
                    String initial = initialValues.get(field);
                    String current = field.getText() != null ? field.getText().toString() : "";
                }
            }
        };

        for (TextInputEditText field : fields) {
            field.addTextChangedListener(watcher);
        }

        et_matter_title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                matter_title.setText(s.toString());
            }
        });
        if (viewMatterModel1.getTags_list() != null) {
            existing_tags_list = viewMatterModel1.getTags_list();
        }
        tag_list.clear();
        if (existing_tags_list != null && existing_tags_list.length() > 0) {
            for (int j = 0; j < existing_tags_list.length(); j++) {
                try {
                    tag_list.add(existing_tags_list.getString(j));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        loadMatterTags();
        callCaseType();
        btn_add_more_info.setVisibility(VISIBLE);
        other_information.setVisibility(GONE);
        isInitialLoad = false;  // ← MARK load complete
        isChangesOccured = false; // ← RESET so no accidental triggers during load count as changes
    }

    private void loadeditmatter(JSONObject jsonObject) {
        viewMatterModel1 = new ViewMatterModel();
        try {
            ViewMatterModel viewMatterModel = new ViewMatterModel();
            viewMatterModel.setId(jsonObject.optString("id"));
            Constants.Matter_id = "";
            Constants.Matter_id = viewMatterModel.getId();
            if (jsonObject.has("caseNumber")) {
                viewMatterModel.setCaseNumber(jsonObject.optString("caseNumber"));
            }
            if (jsonObject.has("caseType")) {
                viewMatterModel.setCasetype(jsonObject.optString("caseType"));
            }
            JSONArray clientIds = new JSONArray();
//                viewMatterModel.setClients(jsonObject.optJSONArray("clients"));
            JSONArray clientsArray = jsonObject.optJSONArray("clients");

            if (clientsArray != null) {
                for (int i = 0; i < clientsArray.length(); i++) {
                    JSONObject clientObject = clientsArray.optJSONObject(i);
                    if (clientObject != null) {
                        String clientId = clientObject.optString("id", "");
                        clientIds.put(clientId); // Add the ID to the JSONArray
                    }
                }
            }

            viewMatterModel.setClients(clientsArray);

            if (jsonObject.has("corporate")) {
                String CorpclientId = "";
                JSONArray CorpclientsArray = jsonObject.optJSONArray("corporate");

                if (CorpclientsArray != null) {
                    for (int i = 0; i < CorpclientsArray.length(); i++) {
                        JSONObject clientObject = CorpclientsArray.optJSONObject(i);
                        if (clientObject != null) {
                            CorpclientId = clientObject.optString("id", "");
                        }
                    }
                }
                viewMatterModel.setCorporate(CorpclientsArray);
                viewMatterModel.setCorpId(CorpclientId);
                clientIds.put(CorpclientId);
            }

            viewMatterModel.setCreated(jsonObject.optString("created_on"));

            viewMatterModel.setClients_list(clientIds);
            if (jsonObject.has("courtName")) {
                viewMatterModel.setCourtName(jsonObject.optString("courtName"));
            }
            if (jsonObject.has("isdisabled")) {
                viewMatterModel.setIsdisabled(jsonObject.optBoolean("isdisabled"));
            }
            if (jsonObject.has("date_of_filling")) {
                viewMatterModel.setDate_of_filling(AndroidUtils.formatToMMMddYYYY(jsonObject.optString("date_of_filling")));
            }
            if (jsonObject.has("closedate")) {
                viewMatterModel.setClosedate(AndroidUtils.formatToMMMddYYYY(jsonObject.optString("closedate")));
            }
            if (jsonObject.has("matterNumber")) {
                viewMatterModel.setMatterNumber(jsonObject.optString("matterNumber"));
            }
            if (jsonObject.has("matterType")) {
                viewMatterModel.setMatterType(jsonObject.optString("matterType"));
            }
            if (jsonObject.has("owner")) {
                JSONObject jsonObject1 = jsonObject.optJSONObject("owner");
                assert jsonObject1 != null;
                Constants.owner_id = jsonObject1.optString("id");
                Constants.owner_name = jsonObject1.optString("name");
            }
            if (jsonObject.has("startdate")) {
                viewMatterModel.setStartdate(AndroidUtils.formatToMMMddYYYY(jsonObject.optString("startdate")));
            }
            if (jsonObject.has("timesheets")) {
                viewMatterModel.setTimesheets(jsonObject.optJSONArray("timesheets"));
            }
            if (jsonObject.has("created_date")) {
                viewMatterModel.setCreated_date(AndroidUtils.formatToMMMddYYYY(jsonObject.optString("created_date")));
            }
            if (jsonObject.has("created_date")) {
                viewMatterModel.setCreated(AndroidUtils.formatToMMMddYYYY(jsonObject.optString("created_date")));
            }
            if (jsonObject.has("matter_id")) {
                viewMatterModel.setMatter_id(jsonObject.optString("matter_id"));
            }
            viewMatterModel.setDescription(jsonObject.optString("description"));
            viewMatterModel.setDocuments(jsonObject.optJSONArray("documents"));

//                viewMatterModel.setGroupAcls(jsonObject.optJSONArray("groupAcls"));
//                viewMatterModel.setGroups(jsonObject.optJSONArray("groups"));
            viewMatterModel.setGroupAcls(jsonObject.optJSONArray("groupAcls"));

            viewMatterModel.setGroups(jsonObject.optJSONArray("groups"));

            if (jsonObject.has("hearingDateDetails")) {
                viewMatterModel.setHearingDateDetails(jsonObject.optJSONObject("hearingDateDetails"));
            }
            viewMatterModel.setIs_editable(jsonObject.getBoolean("is_editable"));
            if (jsonObject.has("judges")) {
                viewMatterModel.setJudges(jsonObject.optString("judges"));
            }
            if (jsonObject.has("matterClosedDate")) {
                viewMatterModel.setMatterClosedDate(jsonObject.optString("matterClosedDate"));
            }
            viewMatterModel.setMembers(jsonObject.optJSONArray("members"));
            if (jsonObject.has("nextHearingDate")) {
                viewMatterModel.setNextHearingDate(jsonObject.optString("nextHearingDate"));
            }
            if (jsonObject.has("opponentAdvocates")) {
                viewMatterModel.setOpponentAdvocates(jsonObject.optJSONArray("opponentAdvocates"));
            }
            viewMatterModel.setOwner(jsonObject.optJSONObject("owner"));
            viewMatterModel.setPriority(jsonObject.optString("priority"));
            viewMatterModel.setStatus(jsonObject.optString("status"));
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
                viewMatterModel.setTempClients(jsonObject.optJSONArray("tempClients"));
            }
            if (jsonObject.has("timesheets")) {
                viewMatterModel.setTimesheets(jsonObject.optJSONArray("timesheets"));
            }
            viewMatterModel.setTemporaryClients(jsonObject.optJSONArray("temporaryClients"));
            viewMatterModel.setTitle(jsonObject.optString("title"));
            viewMatterModel1 = viewMatterModel;
            loadEditMatterDetails();
        } catch (JSONException e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
            e.fillInStackTrace();
        }
    }

    private void loadCaseTypes() {
        CommonSpinnerAdapter adapter = new CommonSpinnerAdapter(getActivity(), caseTypeList);
        sp_case_type.setAdapter(adapter);
        AndroidUtils.LoadList(sp_case_type, getContext(), caseTypeList.size(), true);
        sp_case_type.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                et_case_type.setText(caseTypeList.get(position));
                isCaseTypeChecked = true;
                sp_case_type.setVisibility(GONE);
                AddMatterDetails();
//                loadselected_corp_list();
            }
        });
    }

    private void nav_view_matter() {
        Constants.is_CreateMatter = false;
        Constants.isCreate = false;
        Fragment fragment = new Matter();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.id_framelayout, fragment);
        ft.commit();
    }

    private void loadCancelMatter() {
//        if (!Constants.create_matter) {
//            if (isChangesOccured) {
//                AndroidUtils.showConfirmation(
//                        getActivity(),
//                        requireContext().getString(R.string.leavepage),
//                        requireContext().getString(R.string.changes_you_made_may_not_be_saved),
//                        requireContext().getString(R.string.leave),
//                        new AndroidUtils.OnConfirmListener() {
//                            @Override
//                            public void onSave() {
//                                if (viewMatter != null) {
//                                    nav_view_matter();
//                                } else if (matter != null)
//                                    matter.loadViewUI();
//                            }
//
//                            @Override
//                            public void onCancel() {
//                            }
//                        }
//                );
//            } else {
//                // No details filled — directly go back
//                if (viewMatter != null) {
//                    nav_view_matter();
//                } else if (matter != null)
//                    matter.loadViewUI();
//            }
//        } else {
//            saveMatterInformation();
//        }
    }

    public void saveMatterInformation() {
        String msg;
//        String number;
//        if (Constants.MATTER_TYPE.equals("Legal")) {
//            number = "Case Number";
//        } else {
//            number = "Matter Number";
//        }
        if (!Constants.create_matter) {
            msg = "Please enter the title";
            if (Objects.requireNonNull(et_matter_title.getText()).toString().trim().isEmpty()) {
                AndroidUtils.showAlert(msg, getActivity());
                et_matter_title.requestFocus();
            } else if (Objects.requireNonNull(et_matter_description.getText()).toString().length() > 300) {
                AndroidUtils.showAlert("Please check the description field size..", getActivity());
            } else {
//            AdvocateModel advocateModel = new AdvocateModel();
//            advocateModel.setAdvocate_name(et_advocate_name.getText().toString());
//            advocateModel.setEmail(et_advocate_email.getText().toString());
//            advocateModel.setNumber(et_advocate_phone.getText().toString());
//            advocates_list.add(advocateModel);
                CheckUnique();
            }
        } else {
            msg = "Please Check the Title";
            if (Objects.requireNonNull(et_matter_title.getText()).toString().trim().isEmpty()) {
                AndroidUtils.showAlert(msg, getActivity());
                et_matter_title.requestFocus();
            } else if (Objects.requireNonNull(et_matter_description.getText()).toString().length() > 300) {
                AndroidUtils.showAlert("Please check the description field size..", getActivity());
            } else {
//            AdvocateModel advocateModel = new AdvocateModel();
//            advocateModel.setAdvocate_name(et_advocate_name.getText().toString());
//            advocateModel.setEmail(et_advocate_email.getText().toString());
//            advocateModel.setNumber(et_advocate_phone.getText().toString());
//            advocates_list.add(advocateModel);
                CheckUnique();
            }
        }
    }

    void submitMatter() {
        if (Objects.equals(Constants.MATTER_TYPE, "General")) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String startDateStr = tv_start_date.getText().toString();
            String endDateStr = tv_end_date.getText().toString();

            try {
                Date startDate = null;
                Date endDate = null;
                if (!startDateStr.isEmpty()) {
                    startDate = sdf.parse(startDateStr);
                }
                if (!endDateStr.isEmpty()) {
                    endDate = sdf.parse(endDateStr);
                }

                if (startDate != null && endDate != null && endDate.before(startDate)) {
                    AndroidUtils.showAlert("End date should not be earlier than start date", getActivity());
                    return;
                }
//            JSONArray client = new JSONArray();
//            JSONArray members = new JSONArray();
                matterModel = new MatterModel();
                matterModel.setMatter_title(Objects.requireNonNull(et_matter_title.getText()).toString());
                matterModel.setMatter_id(Objects.requireNonNull(et_matter_id.getText()).toString());
//                et_matter_title.clearFocus();
                matterModel.setCase_number(Objects.requireNonNull(et_matter_num.getText()).toString());
//                et_matter_num.clearFocus();
                matterModel.setCase_type(Objects.requireNonNull(et_case_type.getText()).toString());
                matterModel.setDescription(Objects.requireNonNull(et_matter_description.getText()).toString());
                matterModel.setDate_of_filing(tv_dof.getText().toString());
                matterModel.setStart_date(tv_start_date.getText().toString());
                matterModel.setEnd_date(tv_end_date.getText().toString());
                matterModel.setCourt(Objects.requireNonNull(et_court.getText()).toString());
                matterModel.setJudge(Objects.requireNonNull(et_judge.getText()).toString());
                matterModel.setCase_priority(CASE_PRIORITY);
                matterModel.setStatus(STATUS);
                matterModel.setCorp_clients_list(existing_corp_clients);
                matterModel.setTemp_clients_list(existing_temp_list);
                matterModel.setClients(existing_clients);
                matterModel.setGroup_acls(exisiting_group_acls);
                matterModel.setMembers(existing_members);
                matterModel.setGroups_list(existing_groups_list);
                matterModel.setClients_list(existing_clients_list);
                matterModel.setMembers_list(existing_tm_list);
                matterModel.setDocuments(existing_documents);
                matterModel.setDocuments_list(existing_documents_list);
                existing_tags_list = new JSONArray();
                for (int i = 0; i < tag_list.size(); i++) {
                    existing_tags_list.put(tag_list.get(i));  // no JSONObject
                }
                matterModel.setTags_list(existing_tags_list);

                loadMatterDetails();
                // matterModel_info = matterModel;
                cv_client_details.setVisibility(GONE);
                matter.loadGCT();
            } catch (ParseException | java.text.ParseException e) {
                throw new RuntimeException(e);
            }
        } else {
//            JSONArray client = new JSONArray();
//            JSONArray members = new JSONArray();
            matterModel = new MatterModel();
            matterModel.setMatter_title(Objects.requireNonNull(et_matter_title.getText()).toString());
            matterModel.setMatter_id(Objects.requireNonNull(et_matter_id.getText()).toString());
//            et_matter_title.clearFocus();
            matterModel.setCase_number(Objects.requireNonNull(et_matter_num.getText()).toString());
//            et_matter_num.clearFocus();
            matterModel.setCase_type(Objects.requireNonNull(et_case_type.getText()).toString());
            matterModel.setDescription(Objects.requireNonNull(et_matter_description.getText()).toString());
            matterModel.setDate_of_filing(tv_dof.getText().toString());
            matterModel.setStart_date(tv_start_date.getText().toString());
            matterModel.setEnd_date(tv_end_date.getText().toString());
            matterModel.setCourt(Objects.requireNonNull(et_court.getText()).toString());
            matterModel.setJudge(Objects.requireNonNull(et_judge.getText()).toString());
            matterModel.setCase_priority(CASE_PRIORITY);
            matterModel.setStatus(STATUS);
            matterModel.setCorp_clients_list(existing_corp_clients);
            matterModel.setClients(existing_clients);
            matterModel.setGroup_acls(exisiting_group_acls);
            matterModel.setMembers(existing_members);
            matterModel.setGroups_list(existing_groups_list);
            matterModel.setClients_list(existing_clients_list);
            matterModel.setMembers_list(existing_tm_list);
            matterModel.setDocuments(existing_documents);
            matterModel.setDocuments_list(existing_documents_list);
            existing_tags_list = new JSONArray();
            for (int i = 0; i < tag_list.size(); i++) {
                existing_tags_list.put(tag_list.get(i));  // no JSONObject
            }
            matterModel.setTags_list(existing_tags_list);

            JSONArray jsonArray = new JSONArray();
            try {
                for (int i = 0; i < advocates_list.size(); i++) {
                    AdvocateModel advocateModel = advocates_list.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", advocateModel.getAdvocate_name());
                    jsonObject.put("email", advocateModel.getEmail());
                    jsonObject.put("phone", advocateModel.getNumber());
                    jsonArray.put(jsonObject);
                }

            } catch (JSONException e) {
                e.fillInStackTrace();
            }
            Constants.currentPage = currentPage;
            matterModel.setOpponent_advocate(jsonArray);
            loadMatterDetails();
            // matterModel_info = matterModel;
            cv_client_details.setVisibility(GONE);
            matter.loadGCT();
        }
    }

    private void loadMatterDetails() {
        if (matterArraylist != null)
            if (matterArraylist.isEmpty()) {
                matterArraylist.add(matterModel);
            } else {
                matterArraylist.set(0, matterModel);
            }
    }

    //    private void loadAdvocateUI() {
//        try {
//            loadOpponentsList(true);
//            ll_opponent_advocate.setVisibility(View.VISIBLE);
//            RefreshAdvocateView();
//
//            et_advocate_name.setHint(R.string.name);
//            et_advocate_name.setTextSize(DynamicUtils.fifteen);
//
//            et_advocate_email.setHint(R.string.email);
//            et_advocate_email.setTextSize(DynamicUtils.fifteen);
//            et_advocate_phone.setInputType(InputType.TYPE_CLASS_NUMBER);
//            et_advocate_phone.setHint(R.string.phone_number);
//            et_advocate_phone.setTextSize(DynamicUtils.fifteen);
//
//            et_advocate_name.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                }
//
//                @Override
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
//                    if (s.toString().trim().isEmpty()) {
//                        tv_response.setVisibility(View.VISIBLE);
//                        tv_response.setText("Please enter the name.");
//                    } else {
//                        tv_response.setVisibility(View.GONE);
//                    }
//                }
//
//                @Override
//                public void afterTextChanged(Editable s) {
//                }
//            });
//
//            et_advocate_email.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                }
//
//                @Override
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
//                }
//
//                @Override
//                public void afterTextChanged(Editable s) {
//                    if (Objects.requireNonNull(et_advocate_email.getText()).toString().trim().isEmpty()) {
//                        tv_reponse_email.setVisibility(View.VISIBLE);
//                        tv_reponse_email.setText("Please enter the email address");
//                    } else if (AndroidUtils.isValidEmail(et_advocate_email.getText().toString().trim()).matches()) {
//                        tv_reponse_email.setVisibility(View.VISIBLE);
//                        tv_reponse_email.setText("Please enter a valid email address");
//                    } else {
//                        tv_reponse_email.setVisibility(View.GONE);
//                    }
//                }
//            });
//
//            et_advocate_phone.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                    tv_response_phone.setVisibility(View.GONE);
//                }
//
//                @Override
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
//                    String phone = s.toString().trim();
//                    if (phone.length() < 10) {
//                        tv_response_phone.setVisibility(View.VISIBLE);
//                        tv_response_phone.setText("Please enter a 10 digit valid mobile number.");
//                    } else {
//                        tv_response_phone.setVisibility(View.GONE);
//                    }
//                }
//
//                @Override
//                public void afterTextChanged(Editable s) {
//                }
//            });
//
//            btn_cancel_tag.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (currentPage == advocates_list.size()) {
//                        if (!advocates_list.isEmpty()) {
//                            currentPage = advocates_list.size() - 1;
//                            AdvocateModel previous = advocates_list.get(currentPage);
//                            et_advocate_name.setText(previous.getAdvocate_name());
//                            et_advocate_email.setText(previous.getEmail());
//                            et_advocate_phone.setText(previous.getNumber());
//                        } else {
//                            et_advocate_name.setText("");
//                            et_advocate_email.setText("");
//                            et_advocate_phone.setText("");
//                        }
//                    } else {
//                        advocates_list.remove(currentPage);
//                        if (!advocates_list.isEmpty()) {
//                            currentPage = Math.max(currentPage - 1, 0);
//                            AdvocateModel model = advocates_list.get(currentPage);
//                            et_advocate_name.setText(model.getAdvocate_name());
//                            et_advocate_email.setText(model.getEmail());
//                            et_advocate_phone.setText(model.getNumber());
//                        } else {
//                            currentPage = 0;
//                            et_advocate_name.setText("");
//                            et_advocate_email.setText("");
//                            et_advocate_phone.setText("");
//                        }
//                    }
//
//                    tv_response.setVisibility(View.GONE);
//                    tv_reponse_email.setVisibility(View.GONE);
//                    tv_response_phone.setVisibility(View.GONE);
//
//                    loadOpponentsList(false);
//                    UpdatePageButton(currentPage);
//
//                    btn_save_tag.setAlpha(1.0f);
//                    btn_save_tag.setEnabled(true);
//                }
//            });
//
//            btn_save_tag.setOnClickListener(v -> {
//                String name = Objects.requireNonNull(et_advocate_name.getText()).toString().trim();
//                String email = Objects.requireNonNull(et_advocate_email.getText()).toString().trim();
//                String phone = Objects.requireNonNull(et_advocate_phone.getText()).toString().trim();
//
//                isValid = true;
//
//                if (name.isEmpty()) {
//                    tv_response.setVisibility(View.VISIBLE);
//                    tv_response.setText("Please enter the name.");
//                    isValid = false;
//                } else {
//                    tv_response.setVisibility(View.GONE);
//                }
//
//                if (email.isEmpty() || AndroidUtils.isValidEmail(email).matches()) {
//                    tv_reponse_email.setVisibility(View.VISIBLE);
//                    tv_reponse_email.setText("Please enter a valid email address.");
//                    isValid = false;
//                } else {
//                    tv_reponse_email.setVisibility(View.GONE);
//                }
//
//                if (phone.isEmpty() || phone.length() < 10) {
//                    tv_response_phone.setVisibility(View.VISIBLE);
//                    tv_response_phone.setText("Please enter a 10 digit valid mobile number.");
//                    isValid = false;
//                } else {
//                    tv_response_phone.setVisibility(View.GONE);
//                }
//                btn_save_tag.setAlpha(0.5f);
//                btn_save_tag.setEnabled(false);
////                btn_create.setEnabled(true);
////                btn_create.setAlpha(1.0f);
//                if (isValid) {
//                    AdvocateModel advocateModel = new AdvocateModel();
//                    advocateModel.setAdvocate_name(Objects.requireNonNull(et_advocate_name.getText()).toString());
//                    advocateModel.setEmail(Objects.requireNonNull(et_advocate_email.getText()).toString());
//                    advocateModel.setNumber(Objects.requireNonNull(et_advocate_phone.getText()).toString());
//
//                    // Replace or add new
//                    if (currentPage < advocates_list.size()) {
//                        advocates_list.set(currentPage, advocateModel);
//                    } else {
//                        advocates_list.add(advocateModel);
//                        currentPage = advocates_list.size(); // For next new entry
//                    }
//
//                    JSONArray jsonArray = new JSONArray();
//                    try {
//                        for (AdvocateModel model : advocates_list) {
//                            JSONObject jsonObject = new JSONObject();
//                            jsonObject.put("name", model.getAdvocate_name());
//                            jsonObject.put("email", model.getEmail());
//                            jsonObject.put("phone", model.getNumber());
//                            jsonArray.put(jsonObject);
//                        }
//                    } catch (JSONException e) {
//                        e.fillInStackTrace();
//                    }
//
//                    matterModel.setOpponent_advocate(jsonArray);
//                    loadMatterDetails();

    /// /                        UpdatePageButton(currentPage);
//                }
//            });
//        } catch (Exception e) {
//            e.fillInStackTrace();
//            AndroidUtils.showAlert(e.getMessage(), getActivity());
//        }
//    }
    private void loadAdvocateUI() {
        try {
            loadOpponentsList(true);
            ll_opponent_advocate.setVisibility(VISIBLE);
            RefreshAdvocateView();

            // Setup hints and input types
            et_advocate_name.setHint(R.string.name);
            et_advocate_name.setTextSize(DynamicUtils.fifteen);

            et_advocate_email.setHint(R.string.email);
            et_advocate_email.setTextSize(DynamicUtils.fifteen);

            et_advocate_phone.setInputType(InputType.TYPE_CLASS_PHONE);
            et_advocate_phone.setHint(R.string.phone_number);
            et_advocate_phone.setTextSize(DynamicUtils.fifteen);
            et_advocate_phone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

            // Attach validation watchers
            addValidationWatcher(et_advocate_name, tv_response, "Please enter the name.", null, null);
            addValidationWatcher(et_advocate_email, tv_reponse_email, "Please enter the email address", Patterns.EMAIL_ADDRESS, "Please enter a valid email address");
            addValidationWatcher(et_advocate_phone, tv_response_phone, "Please enter the phone number", Pattern.compile("^\\d{10}$"), "Please enter a 10 digit valid mobile number");

//            btn_cancel_tag.setOnClickListener(v -> {
//                if (currentPage == advocates_list.size()) {
//                    if (!advocates_list.isEmpty()) {
//                        currentPage = advocates_list.size() - 1;
//                        AdvocateModel previous = advocates_list.get(currentPage);
//                        et_advocate_name.setText(previous.getAdvocate_name());
//                        et_advocate_email.setText(previous.getEmail());
//                        et_advocate_phone.setText(previous.getNumber());
//                    } else {
//                        et_advocate_name.setText("");
//                        et_advocate_email.setText("");
//                        et_advocate_phone.setText("");
//                    }
//                } else {
//                    if (currentPage < advocates_list.size()) {
//                        advocates_list.remove(currentPage);
//                        currentPage = Math.min(currentPage, advocates_list.size() - 1);
//                    }
//                    if (!advocates_list.isEmpty()) {
//                        currentPage = Math.max(currentPage - 1, 0);
//                        AdvocateModel model = advocates_list.get(currentPage);
//                        et_advocate_name.setText(model.getAdvocate_name());
//                        et_advocate_email.setText(model.getEmail());
//                        et_advocate_phone.setText(model.getNumber());
//                    } else {
//                        currentPage = 0;
//                        et_advocate_name.setText("");
//                        et_advocate_email.setText("");
//                        et_advocate_phone.setText("");
//                    }
//                }
//
//                tv_response.setVisibility(View.GONE);
//                tv_reponse_email.setVisibility(View.GONE);
//                tv_response_phone.setVisibility(View.GONE);
//
//                loadOpponentsList(false);
//                UpdatePageButton(currentPage);
//
//                btn_save_tag.setAlpha(1.0f);
//                btn_save_tag.setEnabled(true);
//            });

        } catch (Exception e) {
            e.printStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void addValidationWatcher(EditText editText, TextView errorView, String emptyError, @Nullable Pattern pattern, @Nullable String patternError) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString().trim();
//                if (!value.isEmpty()) {
//                    errorView.setText(emptyError);
//                    errorView.setVisibility(VISIBLE);
//                }
                if (!value.isEmpty() && pattern != null && !pattern.matcher(value).matches()) {
                    errorView.setText(patternError);
                    errorView.setVisibility(VISIBLE);
                } else {
                    errorView.setVisibility(GONE);
                }
            }
        });
    }

    private boolean validateFields() {

        String name = et_advocate_name.getText().toString().trim();
        String email = et_advocate_email.getText().toString().trim();
        String phone = et_advocate_phone.getText().toString().trim();

        // 🔴 Clear previous errors
        tv_response.setVisibility(GONE);
        tv_reponse_email.setVisibility(GONE);
        tv_response_phone.setVisibility(GONE);

        // ❌ All fields empty → NOT allowed
        if (name.isEmpty() && email.isEmpty() && phone.isEmpty()) {
            AndroidUtils.showAlert("Please enter at least one detail (Name, Email or Phone).", getActivity());
            return false;
        }

        boolean valid = true;

        // ✅ Validate only if filled
        if (!name.isEmpty()) {
            // (No special validation needed for name)
        }

        if (!email.isEmpty() && !AndroidUtils.isValidEmail(email)) {
            tv_reponse_email.setText("Please enter a valid email address.");
            tv_reponse_email.setVisibility(VISIBLE);
            valid = false;
        }

        if (!phone.isEmpty() && !phone.matches("^\\d{10}$")) {
            tv_response_phone.setText("Please enter a 10 digit valid mobile number.");
            tv_response_phone.setVisibility(VISIBLE);
            valid = false;
        }

        return valid;
    }


    private void loadEditedData(String adv_name, String adv_email, String adv_phone, int position) {
        AdvocateModel advocateModel = new AdvocateModel();
        advocateModel.setAdvocate_name(adv_name);
        advocateModel.setEmail(adv_email);
        advocateModel.setNumber(adv_phone);
        advocates_list.set(position, advocateModel);
        matterModel.setOpponent_advocate(new JSONArray());
        JSONArray jsonArray = new JSONArray();
        try {
            for (int i = 0; i < advocates_list.size(); i++) {
                AdvocateModel advocateModel1 = advocates_list.get(i);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", advocateModel.getAdvocate_name());
                jsonObject.put("email", advocateModel.getEmail());
                jsonObject.put("phone", advocateModel.getNumber());
                jsonArray.put(jsonObject);
            }

        } catch (JSONException e) {
            e.fillInStackTrace();
        }
        matterModel.setOpponent_advocate(jsonArray);
        loadMatterDetails();
//        tv_opponent_name = view_advocate.findViewById(R.id.tv_opponent_name);
//        tv_opponent_name.setText(advocateModel.getAdvocate_name());
    }

    //    private void loadOpponentsList() {
//        pageNumberLayout.removeAllViews();
//        pagebuttons.clear();
//        for (int i = 0; i < advocates_list.size(); i++) {
//            //...
//            View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.weekly_view_dates, null);
//            TextView pageButton = view_opponents.findViewById(R.id.textDay);
//            pageButton.setText(String.valueOf(i + 1));
//            pageButton.setPadding(20, 15, 20, 15);
//            pageNumber = i + 1;
//            if (pageNumber == advocates_list.size()) {
//                pageButton.setTextColor(getActivity().getColor(R.color.white));
//                pageButton.setBackground(getActivity().getDrawable(R.drawable.rectangular_button_green_count));
//            } else {
//                pageButton.setTextColor(getActivity().getColor(R.color.black));
//                pageButton.setBackground(getActivity().getDrawable(R.drawable.background_transparent));
//            }
//            currentPage = pageNumber;
//
//            pageButton.setTag(i);
//            pageButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    // Set the background tint color of the clicked button to green
//                    int position = 0;
//                    if (view.getTag() instanceof Integer) {
//                        position = (Integer) view.getTag();
//                        view = ll_page_navigaiton.getChildAt(position);

    /// /                        ll_add_advocate.addView(view);
//                        currentPage = position;
//                        AdvocateModel advocateModel = advocates_list.get(position);
//                        EditAdvocateUI(advocateModel.getAdvocate_name(), advocateModel.getEmail(), advocateModel.getNumber(), position, view);
//                        UpdatePageButton(currentPage);
//                    }
//                }
//            });
//            pagebuttons.add(pageButton);
//            pageNumberLayout.addView(view_opponents);
//        }
//        if (!advocates_list.isEmpty()) {
//            ll_page_navigaiton.setVisibility(View.VISIBLE);
//        }
//    }
//
//    private void UpdatePageButton(int currentPage) {
//        for (int i = 0; i < pagebuttons.size(); i++) {
//            TextView pageButton = pagebuttons.get(i);
//            if (i == currentPage) {
//                pageButton.setTextColor(getActivity().getColor(R.color.white));
//                pageButton.setBackground(getActivity().getDrawable(R.drawable.rectangular_button_green_count));
//            } else {
//                pageButton.setTextColor(getActivity().getColor(R.color.black));
//                pageButton.setBackground(getActivity().getDrawable(R.drawable.background_transparent));
//            }
//        }
//    }
//    private void loadOpponentsList() {
//        pageNumberLayout.removeAllViews();
//        pagebuttons.clear();
//
//        int totalButtons = advocates_list.size() + 1; // Extra button for next entry
//
//        for (int i = 0; i < totalButtons; i++) {
//            View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.weekly_view_dates, null);
//            TextView pageButton = view_opponents.findViewById(R.id.textDay);
//            pageButton.setText(String.valueOf(i + 1));
//            pageButton.setPadding(20, 15, 20, 15);
//            pageButton.setTag(i);
//
//            if (i < advocates_list.size()) {
//                // Existing advocate data
//                pageButton.setOnClickListener(view -> {
//                    int position = (Integer) view.getTag();
//                    currentPage = position;
//
//                    AdvocateModel advocateModel = advocates_list.get(position);
//                    EditAdvocateUI(advocateModel.getAdvocate_name(), advocateModel.getEmail(), advocateModel.getNumber(), position, view);
//
//                    UpdatePageButton(currentPage);
//                });
//            } else {
//                // Extra button (new entry)
//                pageButton.setTextColor(getActivity().getColor(R.color.green_count_color)); // Different color for new entry
//                pageButton.setOnClickListener(view -> {
//                    // Logic to add a new advocate entry
//                    currentPage = (int) (Integer) view.getTag();
//                    RefreshAdvocateView();
//                    UpdatePageButton(currentPage);
//                });
//            }
//
//            pagebuttons.add(pageButton);
//            pageNumberLayout.addView(view_opponents);
//        }
//
//        // Highlight the last filled entry
//        if (!advocates_list.isEmpty()) {
//            currentPage = advocates_list.size();
//            UpdatePageButton(currentPage);
//            ll_page_navigaiton.setVisibility(View.VISIBLE);
//        }
//    }
    private void loadOpponentsList(boolean isNewEntry) {
        pageNumberLayout.removeAllViews();
        pagebuttons.clear();

        int totalButtons = advocates_list.size();

        // Only show extra button if we're currently creating a new entry
//        boolean isNewEntry = (currentPage == advocates_list.size());
        if (isNewEntry) {
            totalButtons += 1;
        }

        for (int i = 0; i < totalButtons; i++) {
            View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.weekly_view_dates, null);
            TextView pageButton = view_opponents.findViewById(R.id.textDay);
            pageButton.setText(String.valueOf(i + 1));
            pageButton.setPadding(20, 15, 20, 15);

            if (i == currentPage) {
                pageButton.setTextColor(getActivity().getColor(R.color.white));
                pageButton.setBackground(getActivity().getDrawable(R.drawable.rectangular_button_green_count));
            } else {
                pageButton.setTextColor(getActivity().getColor(R.color.black));
                pageButton.setBackground(getActivity().getDrawable(R.drawable.background_transparent));
            }

            int finalI = i;
            pageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentPage = finalI;

                    if (currentPage < advocates_list.size()) {
                        AdvocateModel advocateModel = advocates_list.get(currentPage);
                        EditAdvocateUI(advocateModel.getAdvocate_name(), advocateModel.getEmail(), advocateModel.getNumber(), currentPage);
                    } else {
                        // new entry case
                        loadAdvocateUI();
                    }

                    UpdatePageButton(currentPage);
                }
            });

            pagebuttons.add(pageButton);
            pageNumberLayout.addView(view_opponents);
        }

//        ll_page_navigaiton.setVisibility(advocates_list.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void UpdatePageButton(int currentPage) {
        for (int i = 0; i < pagebuttons.size(); i++) {
            TextView pageButton = pagebuttons.get(i);
            if (i == currentPage) {
                pageButton.setTextColor(getActivity().getColor(R.color.white));
                pageButton.setBackground(getActivity().getDrawable(R.drawable.rectangular_button_green_count));
            } else if (i < advocates_list.size()) {
                pageButton.setTextColor(getActivity().getColor(R.color.black));
                pageButton.setBackground(getActivity().getDrawable(R.drawable.background_transparent));
            } else {
                // Extra button (next available)
                pageButton.setTextColor(getActivity().getColor(R.color.black));
                pageButton.setBackground(getActivity().getDrawable(R.drawable.background_transparent));
            }
        }
    }


    private void EditAdvocateUI(String advocate_name, String email, String number, int position) {
        try {
            ll_opponent_advocate.setVisibility(VISIBLE);
            RefreshAdvocateView();
//            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//            LayoutInflater inflater = requireActivity().getLayoutInflater();
//            View view = inflater.inflate(R.layout.add_opponent_advocate, null);
//            TextInputEditText et_advocate_name = view.findViewById(R.id.et_advocate_name);
//            TextInputEditText et_advocate_email = view.findViewById(R.id.et_advocate_email);
//            TextInputEditText et_advocate_phone = view.findViewById(R.id.et_advocate_phone);
            et_advocate_phone.setInputType(InputType.TYPE_CLASS_PHONE);
//            AppCompatButton btn_cancel_tag = view.findViewById(R.id.btn_cancel_tag);
//            AppCompatButton btn_save_tag = view.findViewById(R.id.btn_save_tag);
//            final AlertDialog dialog = builder.create();

//            et_advocate_name.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                }
//
//                @Override
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                }
//
//                @Override
//                public void afterTextChanged(Editable s) {
//                    if (s.toString().trim().isEmpty()) {
////                        et_advocate_name.setError("Please Enter the name.");
////                        et_advocate_name.requestFocus();
//                        tv_response.setVisibility(VISIBLE);
//                        tv_response.setText("Please enter the name.");
//                    }
//                }
//            });
            et_advocate_email.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!s.toString().trim().isEmpty())
                        if (!(Objects.requireNonNull(et_advocate_email.getText()).toString().trim().matches(Patterns.EMAIL_ADDRESS.toString()))) {
                            tv_reponse_email.setText("Please enter a valid email address");
                            et_advocate_email.setVisibility(VISIBLE);
                        }
                }
            });
            et_advocate_phone.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!s.toString().trim().isEmpty() && (s.length() < 10)) {
                        tv_response_phone.setText("Please enter a 10 digit valid mobile number.");
                        tv_response_phone.setVisibility(VISIBLE);
                    }
                }
            });

//            btn_cancel_tag.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    dialog.dismiss();
//                }
//            });

            et_advocate_name.setText(advocate_name);
            et_advocate_email.setText(email);
            et_advocate_phone.setText(number);
//            btn_cancel_tag.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // If it's a new entry (not added to the list yet)
//                    if (currentPage == advocates_list.size()) {
//                        if (!advocates_list.isEmpty()) {
//                            // Move to last saved item
//                            currentPage = advocates_list.size() - 1;
//
//                            AdvocateModel previous = advocates_list.get(currentPage);
//                            et_advocate_name.setText(previous.getAdvocate_name());
//                            et_advocate_email.setText(previous.getEmail());
//                            et_advocate_phone.setText(previous.getNumber());
//                        } else {
//                            // No previous items — clear everything
//                            et_advocate_name.setText("");
//                            et_advocate_email.setText("");
//                            et_advocate_phone.setText("");
//                        }
//                    } else {
//                        // Current page is in list — remove and go back
//                        advocates_list.remove(currentPage);
//                        if (!advocates_list.isEmpty()) {
//                            currentPage = Math.max(currentPage - 1, 0);
//                            AdvocateModel model = advocates_list.get(currentPage);
//                            et_advocate_name.setText(model.getAdvocate_name());
//                            et_advocate_email.setText(model.getEmail());
//                            et_advocate_phone.setText(model.getNumber());
//                        } else {
//                            currentPage = 0;
//                            et_advocate_name.setText("");
//                            et_advocate_email.setText("");
//                            et_advocate_phone.setText("");
//                        }
//                    }
//
//                    // Hide validation responses
//                    tv_response.setVisibility(View.GONE);
//                    tv_reponse_email.setVisibility(View.GONE);
//                    tv_response_phone.setVisibility(View.GONE);
//
//                    loadOpponentsList(false);
//                    UpdatePageButton(currentPage);
//
//                    // Enable save
//                    btn_save_tag.setAlpha(1.0f);
//                    btn_save_tag.setEnabled(true);
//                }
//            });
//            btn_cancel_tag.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (currentPage == advocates_list.size()) {
//                        // Case: New Entry (not yet added)
//                        if (!advocates_list.isEmpty()) {
//                            currentPage = advocates_list.size() - 1;
//                            AdvocateModel lastModel = advocates_list.get(currentPage);
//                            EditAdvocateUI(lastModel.getAdvocate_name(), lastModel.getEmail(), lastModel.getNumber(), currentPage);
//                        } else {
//                            currentPage = 0;
//                            loadAdvocateUI();
//                        }
//                        loadOpponentsList(false);
//                    } else {
//                        // Case: Existing Entry — Remove it
//                        if (advocates_list.size() > currentPage) {
//                            advocates_list.remove(currentPage);
//                        }
//
//                        if (!advocates_list.isEmpty()) {
//                            currentPage = Math.max(currentPage - 1, 0);
//                            AdvocateModel model = advocates_list.get(currentPage);
//                            EditAdvocateUI(model.getAdvocate_name(), model.getEmail(), model.getNumber(), currentPage);
//                        } else {
//                            currentPage = 0;
//                            loadAdvocateUI();
//                        }
//                        loadOpponentsList(true);
//                    }
//
//                    // Clear validation messages
//                    tv_response.setVisibility(View.GONE);
//                    tv_reponse_email.setVisibility(View.GONE);
//                    tv_response_phone.setVisibility(View.GONE);
//
//                    // Refresh page buttons
//                    UpdatePageButton(currentPage);
//
//                    // Enable save again
//                    btn_save_tag.setAlpha(1.0f);
//                    btn_save_tag.setEnabled(true);
//                    AddMatterDetails();
//                }
//            });
//
//            btn_save_tag.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (Objects.requireNonNull(et_advocate_name.getText()).toString().trim().isEmpty() && (Objects.requireNonNull(et_advocate_email.getText()).toString().trim().isEmpty()) && (Objects.requireNonNull(et_advocate_phone.getText()).toString().trim().isEmpty())) {
////                        et_advocate_name.setError("Please Enter the name.");
//                        tv_response.setVisibility(View.VISIBLE);
//                        tv_response.setText("Please enter the name.");
//                        tv_reponse_email.setVisibility(View.VISIBLE);
//                        tv_reponse_email.setText("Please enter the Email.");
//                        tv_response_phone.setVisibility(View.VISIBLE);
//                        tv_response_phone.setText("Please enter a 10 digit valid mobile number.");
////                        et_advocate_email.setError("Please enter the Email.");
////                        et_advocate_phone.setError("Please enter a 10 digit valid mobile number.");
//                    } else if (Objects.requireNonNull(et_advocate_name.getText()).toString().trim().isEmpty()) {
////                        et_advocate_name.setError("Please Enter the name.");
////                        et_advocate_name.requestFocus();
//                        tv_response.setVisibility(View.VISIBLE);
//                        tv_response.setText("Please enter the name.");
//                    } else if ((!(Objects.requireNonNull(et_advocate_email.getText()).toString().matches(Patterns.EMAIL_ADDRESS.toString()))) || (et_advocate_email.getText().toString().trim().isEmpty())) {
////                        et_advocate_email.setError("Please enter a valid email address");
////                        et_advocate_email.requestFocus();
//                        tv_reponse_email.setVisibility(View.VISIBLE);
//                        tv_reponse_email.setText("Please enter the Email");
//                    } else if (Objects.requireNonNull(et_advocate_phone.getText()).toString().trim().isEmpty() || (et_advocate_phone.getText().length() < 10)) {
//                        tv_response_phone.setText("Please enter a 10 digit valid mobile number.");
//                        tv_response_phone.requestFocus();
////                    } else if (!(et_advocate_phone.getText().toString().matches(Patterns.PHONE.toString()))) {
////                        et_advocate_phone.setError("Please enter a valid phone number");
////                        et_advocate_phone.requestFocus();
//                    } else {
////                        dialog.dismiss();
////                        ll_opponent_advocate.setVisibility(View.GONE);
//                        if (position != advocates_list.size()) {
//                            loadEditedData(et_advocate_name.getText().toString(), et_advocate_email.getText().toString(), et_advocate_phone.getText().toString(), position);
////                        loadOpponentsList(advocates_list);
//                        RefreshAdvocateView();
//                        } else {
//                            loadEditedData(et_advocate_name.getText().toString(), et_advocate_email.getText().toString(), et_advocate_phone.getText().toString(), position);
//                            loadAdvocateUI();
//                        }
//                    }
//                }
//            });
//            dialog.setCancelable(false);
//            dialog.setView(view);
//            dialog.show();
        } catch (Exception e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void clearAdvocateView() {
        tv_response.setVisibility(GONE);
        tv_reponse_email.setVisibility(GONE);
        tv_response_phone.setVisibility(GONE);
    }

    private void RefreshAdvocateView() {
        ll_selected_advocates.setVisibility(GONE);
        et_advocate_name.setText("");
        et_advocate_email.setText("");
        et_advocate_phone.setText("");

        tv_response.setVisibility(GONE);
        tv_reponse_email.setVisibility(GONE);
        tv_response_phone.setVisibility(GONE);
//        et_advocate_name.setError(null);
//        et_advocate_email.setError(null);
//        et_advocate_phone.setError(null);
    }

    private void loadActiveUI() {
        STATUS = "Active";
        tv_status_active.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_round_background));
        tv_status_active.setTextColor(Color.WHITE);
        tv_status_pending.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_round_background));
        tv_status_pending.setTextColor(Color.BLACK);
    }

    private void loadPendingUI() {
        STATUS = "Pending";
        tv_status_active.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_round_background));
        tv_status_active.setTextColor(Color.BLACK);
        tv_status_pending.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_round_background));
        tv_status_pending.setTextColor(Color.WHITE);
    }

    private void loadLowPriorityUI() {
        CASE_PRIORITY = "Low";
        tv_high_priority.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_round_background));
        tv_high_priority.setTextColor(Color.BLACK);
        tv_medium_priority.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_centre_background));
        tv_medium_priority.setTextColor(Color.BLACK);
        tv_low_priority.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_round_background));
        tv_low_priority.setTextColor(Color.WHITE);
    }

    private void loadMediumPriorityUI() {
        CASE_PRIORITY = "Medium";
        tv_high_priority.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_round_background));
        tv_high_priority.setTextColor(Color.BLACK);
        tv_medium_priority.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.radiobutton_centre_green_background));
        tv_medium_priority.setTextColor(Color.WHITE);
        tv_low_priority.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_round_background));
        tv_low_priority.setTextColor(Color.BLACK);
    }

    private void loadHighPriorityUI() {
        CASE_PRIORITY = "High";
        tv_high_priority.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_round_background));
        tv_high_priority.setTextColor(Color.WHITE);
        tv_medium_priority.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_centre_background));
        tv_medium_priority.setTextColor(Color.BLACK);
        tv_low_priority.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_round_background));
        tv_low_priority.setTextColor(Color.BLACK);
    }

    private void PriorityStatus() {
//        if (Objects.equals(CASE_PRIORITY, oldPriority)) {
//            btn_create.setAlpha(0.5f);
//            btn_create.setEnabled(false);
//        } else {
//            btn_create.setAlpha(1.0f);
//            btn_create.setEnabled(true);
//        }
        if (isInitialLoad && !Constants.create_matter) return; // ← CHANGED
        AddMatterDetails();
    }

    public void callEditMatterInfo() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v2/matter/" + Constants.MATTER_TYPE.toLowerCase(Locale.ROOT) + "/" + Constants.Matter_id, "Edit Matter", postdata.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
            e.fillInStackTrace();
        }
    }

    private void AddMatterDetails() {
        if (!Constants.create_matter) {
//            AndroidUtils.ToggleButton(1, btn_create);
        }
        if (isInitialLoad && !Constants.create_matter) return;

        isChangesOccured = true;
        if (Objects.equals(Constants.MATTER_TYPE, "General")) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String startDateStr = tv_start_date.getText().toString();
            String endDateStr = tv_end_date.getText().toString();

            try {
                Date startDate = null;
                Date endDate = null;
                if (!startDateStr.isEmpty()) {
                    startDate = sdf.parse(startDateStr);
                }
                if (!endDateStr.isEmpty()) {
                    endDate = sdf.parse(endDateStr);
                }

                if (startDate != null && endDate != null && endDate.before(startDate)) {
                    AndroidUtils.showAlert("End date should not be earlier than start date", getActivity());
                    return;
                }
//            JSONArray client = new JSONArray();
//            JSONArray members = new JSONArray();
//                matterModel = new MatterModel();
                matterModel.setMatter_title(Objects.requireNonNull(et_matter_title.getText()).toString());
                matterModel.setMatter_id(Objects.requireNonNull(et_matter_id.getText()).toString());
//                et_matter_title.clearFocus();
                matterModel.setCase_number(Objects.requireNonNull(et_matter_num.getText()).toString());
//                et_matter_num.clearFocus();
                matterModel.setCase_type(Objects.requireNonNull(et_matter_type.getText()).toString());
                matterModel.setDescription(Objects.requireNonNull(et_matter_description.getText()).toString());
                matterModel.setDate_of_filing(tv_dof.getText().toString());
                matterModel.setStart_date(tv_start_date.getText().toString());
                matterModel.setEnd_date(tv_end_date.getText().toString());
                matterModel.setCourt(Objects.requireNonNull(et_court.getText()).toString());
                matterModel.setJudge(Objects.requireNonNull(et_judge.getText()).toString());
                matterModel.setCase_priority(CASE_PRIORITY);
                matterModel.setStatus(STATUS);
                matterModel.setCorp_clients_list(existing_corp_clients);
                matterModel.setTemp_clients_list(existing_temp_list);
                matterModel.setClients(existing_clients);
                matterModel.setGroup_acls(exisiting_group_acls);
                matterModel.setMembers(existing_members);
                matterModel.setGroups_list(existing_groups_list);
                matterModel.setClients_list(existing_clients_list);
                matterModel.setMembers_list(existing_tm_list);
                matterModel.setCreated_date(tv_create_date.getText().toString());
                matterModel.setDocuments(existing_documents);
                matterModel.setDocuments_list(existing_documents_list);
                matterModel.setMatter_id(et_matter_id.getText().toString());
                existing_tags_list = new JSONArray();
                for (int i = 0; i < tag_list.size(); i++) {
                    existing_tags_list.put(tag_list.get(i));  // no JSONObject
                }
                matterModel.setTags_list(existing_tags_list);

                loadMatterDetails();
                // matterModel_info = matterModel;
//                cv_client_details.setVisibility(View.GONE);
//                matter.loadGCT();
            } catch (ParseException | java.text.ParseException e) {
                throw new RuntimeException(e);
            }
        } else {
//            JSONArray client = new JSONArray();
//            JSONArray members = new JSONArray();
//            matterModel = new MatterModel();
            matterModel.setMatter_title(Objects.requireNonNull(et_matter_title.getText()).toString());
            matterModel.setMatter_id(Objects.requireNonNull(et_matter_id.getText()).toString());
//            et_matter_title.clearFocus();
            matterModel.setCase_number(Objects.requireNonNull(et_matter_num.getText()).toString());
//            et_matter_num.clearFocus();
            matterModel.setCase_type(Objects.requireNonNull(et_case_type.getText()).toString());
            matterModel.setDescription(Objects.requireNonNull(et_matter_description.getText()).toString());
            matterModel.setDate_of_filing(tv_dof.getText().toString());
            matterModel.setStart_date(tv_start_date.getText().toString());
            matterModel.setEnd_date(tv_end_date.getText().toString());
            matterModel.setCourt(Objects.requireNonNull(et_court.getText()).toString());
            matterModel.setJudge(Objects.requireNonNull(et_judge.getText()).toString());
            matterModel.setCase_priority(CASE_PRIORITY);
            matterModel.setStatus(STATUS);
            matterModel.setCorp_clients_list(existing_corp_clients);
            matterModel.setTemp_clients_list(existing_temp_list);
            matterModel.setClients(existing_clients);
            matterModel.setCreated_date(tv_create_date.getText().toString());
            matterModel.setGroup_acls(exisiting_group_acls);
            matterModel.setMembers(existing_members);
            matterModel.setGroups_list(existing_groups_list);
            matterModel.setClients_list(existing_clients_list);
            matterModel.setMembers_list(existing_tm_list);
            matterModel.setDocuments(existing_documents);
            matterModel.setDocuments_list(existing_documents_list);
            matterModel.setMatter_id(et_matter_id.getText().toString());

            JSONArray jsonArray = new JSONArray();
            try {
                for (int i = 0; i < advocates_list.size(); i++) {
                    AdvocateModel advocateModel = advocates_list.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", advocateModel.getAdvocate_name());
                    jsonObject.put("email", advocateModel.getEmail());
                    jsonObject.put("phone", advocateModel.getNumber());
                    jsonArray.put(jsonObject);
                }

            } catch (JSONException e) {
                e.fillInStackTrace();
            }
            existing_tags_list = new JSONArray();
            for (int i = 0; i < tag_list.size(); i++) {
                existing_tags_list.put(tag_list.get(i));  // no JSONObject
            }
            matterModel.setTags_list(existing_tags_list);
            Constants.currentPage = currentPage;
            matterModel.setOpponent_advocate(jsonArray);

            loadMatterDetails();
            // matterModel_info = matterModel;
//            cv_client_details.setVisibility(View.GONE);
//            matter.loadGCT();
        }
    }

    private void Status() {
//        if (Objects.equals(STATUS, oldStatus)) {
//            btn_create.setAlpha(0.5f);
//            btn_create.setEnabled(false);
//        } else {
//            btn_create.setAlpha(1.0f);
//            btn_create.setEnabled(true);
//        }
        if (isInitialLoad && !Constants.create_matter) return; // ← CHANGED
        AddMatterDetails();
    }
}
