
package com.digicoffer.lauditor.Members;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.cemail_alert;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.email_info_alert;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Groups.Models.ViewGroupModel;
import com.digicoffer.lauditor.Members.Adapters.GroupsAdapter;
import com.digicoffer.lauditor.Members.Adapters.MembersAdapter;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Disabled_view;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class Members extends Fragment implements AsyncTaskCompleteListener, MembersAdapter.EventListener, View.OnClickListener {
    TextView tv_member_name, frozen_PageText, tv_designation, tv_email, tv_confirm_email, tv_create_members, tv_view_members, tv_sp_default_currency, assign_group;
    ListView sp_default_currency;
    CommonSpinnerAdapter currencyadapter;
    boolean iscurrency_checked = true;
    FrameLayout child_container;
    String total = "";
    String count = "";
    LinearLayout tv_switchView, tv_switchCreate;
    TextView response_email, response_cemail, tv_tot_license;
    TextInputEditText et_search_members, et_search_teammember, tv_default_rate;
    private NewModel mViewModel;
    String total_license = "";
    TextView tv_assign_groups, name, designation, default_rate, default_currency, email, confirm_email;
    AppCompatButton btn_cancel_members, btn_save_members, bt_cancel, bt_save, btn_cancel_save, btn_create;
    RecyclerView rv_selected_member;
    public RecyclerView rv_view_members;
    //    TextInputLayout search_teammember;
    ImageView img_dropdown_icon, img_clear_icon;
    public static String FLAG = "";
    String default_currency1 = "";
    CardView cv_details, cv_members_details;
    GroupsAdapter groupsAdapter = null;
    ArrayList<ViewGroupModel> updatedMembersList = new ArrayList<>();
    ArrayList<String> currency_list = new ArrayList<>();
    ArrayList<MembersModel> members_list = new ArrayList<>();
    String TAG = "";
    View tl_search_members;
    boolean isCreateMatter = true;
    ArrayList<ViewGroupModel> groupsList = new ArrayList<>();
    LinearLayoutCompat ll_buttons, ll_new_buttons, ll_save_buttons;
    LinearLayout ll_confirm_email, ll_sp_currency;
    Dialog progress_dialog;
    Members members;
    View tl_search_teammember;
    private boolean MemberModelArrayList;
    private Object MembersModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setupOnBackPressed();
        super.onCreate(savedInstanceState);
    }

    private void setupOnBackPressed() {
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isEnabled()) {
//                    AndroidUtils.showAlert("Dashboard",getContext());
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.create_members, container, false);
        return v;
    }

    @SuppressLint("WrongViewCast")
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(NewModel.class);
        tv_member_name = v.findViewById(R.id.tv_create_member_name);
        tv_member_name.addTextChangedListener(new Validation((EditText) tv_member_name));
        name = v.findViewById(R.id.name);
        tv_switchCreate = v.findViewById(R.id.tv_switchCreate);
        tv_switchView = v.findViewById(R.id.tv_switchView);
        name.setText(R.string.name);
        child_container = v.findViewById(R.id.child_container);
        tv_tot_license = v.findViewById(R.id.tv_tot_license);
        tv_tot_license.setText("");
//        tv_tot_license.setTextColor(getResources().getColor(R.color.white));
        assign_group = v.findViewById(R.id.assign_group);
        assign_group.setText(R.string.assign_groups_);
//        assign_group.setTextSize(DynamicUtils.twentyFive);
        designation = v.findViewById(R.id.designation);
        designation.setText(R.string.desg);
        default_rate = v.findViewById(R.id.default_rate);
        default_rate.setText(R.string.default_rate);
        default_currency = v.findViewById(R.id.default_currency);
        default_currency.setText(R.string.default_currency);
        email = v.findViewById(R.id.email);
        email.setText(R.string.email);
        confirm_email = v.findViewById(R.id.confirm_email);
        confirm_email.setText(R.string.confirm_email);
        name = v.findViewById(R.id.name);
        name.setText(R.string.name);
        tv_member_name.setHint(R.string.name);
        tv_designation = v.findViewById(R.id.tv_designation);
        tv_designation.setHint(R.string.desg);
        tv_designation.addTextChangedListener(new Validation((EditText) tv_designation));
        tv_email = v.findViewById(R.id.tv_email);
        tv_email.setHint(R.string.email);
        tv_email.addTextChangedListener(new Validation((EditText) tv_email));
        rv_view_members = v.findViewById(R.id.rv_view_members);
        tv_confirm_email = v.findViewById(R.id.tv_confirm_email);
        tv_confirm_email.setHint(R.string.confirm_email);
        tv_confirm_email.addTextChangedListener(new Validation((EditText) tv_confirm_email));
        tv_default_rate = v.findViewById(R.id.tv_default_rate);
        tv_default_rate.setHint(R.string.default_rate);
        tv_default_rate.setMaxLines(1);
        tv_default_rate.addTextChangedListener(new Validation(tv_default_rate));
        bt_save = v.findViewById(R.id.btn_update);
        bt_cancel = v.findViewById(R.id.btn_cancel_edit);
        tv_create_members = v.findViewById(R.id.tv_create_members);
        tv_create_members.setText(R.string.create_member);
        tv_view_members = v.findViewById(R.id.tv_view_members);
        tl_search_members = v.findViewById(R.id.tl_search_members);
        et_search_members = tl_search_members.findViewById(R.id.et_Search);
        et_search_members.setHint(R.string.search_groups);
        et_search_members.addTextChangedListener(new Validation(et_search_members));
        tl_search_teammember = v.findViewById(R.id.tl_search_teammember);
        et_search_teammember = tl_search_teammember.findViewById(R.id.et_Search);
        et_search_teammember.setHint(R.string.search_team_members);
        et_search_teammember.addTextChangedListener(new Validation(et_search_teammember));
//        search_teammember = v.findViewById(R.id.search_teammember);
        sp_default_currency = v.findViewById(R.id.sp_default_currency);
        ll_sp_currency = v.findViewById(R.id.ll_sp_currency);
        img_clear_icon = ll_sp_currency.findViewById(R.id.img_clear_icon);
        img_dropdown_icon = ll_sp_currency.findViewById(R.id.img_dropdown_icon);
        tv_sp_default_currency = ll_sp_currency.findViewById(R.id.tv_spinner_view);
        tv_sp_default_currency.setHint(R.string.select_default_currency);
//        img_dropdown_icon = view.findViewById(R.id.img_dropdown_icon);
//        img_clear_icon = view.findViewById(R.id.img_clear_icon);
        btn_cancel_members = v.findViewById(R.id.btn_cancel_members);
        btn_save_members = v.findViewById(R.id.btn_save_members);
        cv_details = v.findViewById(R.id.cv_details);
        btn_cancel_save = v.findViewById(R.id.btn_cancel_save);
        btn_create = v.findViewById(R.id.btn_create);
        btn_create.setOnClickListener(this);
        btn_cancel_save.setOnClickListener(this);
        ll_save_buttons = v.findViewById(R.id.ll_save_buttons);
        ll_confirm_email = v.findViewById(R.id.ll_confirm_email);
        ll_buttons = v.findViewById(R.id.ll_buttons);
        cv_members_details = v.findViewById(R.id.cv_details_2);
        ll_new_buttons = v.findViewById(R.id.ll_edit_buttons);
        tv_assign_groups = v.findViewById(R.id.tv_assign_group);
        response_email = v.findViewById(R.id.response_email);
        response_cemail = v.findViewById(R.id.response_cemail);
//        FLAG = "first_click";
        tv_assign_groups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HideSelectGroups();
            }
        });
//        AndroidUtils.setupEdgePaddingBehavior(rv_view_members);
        rv_selected_member = v.findViewById(R.id.rv_selected_member);
//        String data = "View Members";
//        setViewModelData(data);
        if (Constants.ROLE.equals("GH")) {
            tv_create_members.setVisibility(View.GONE);
            tv_view_members.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.full_green_background));
            ViewMembersData();
        } else {
            if (Constants.isCreate) {
                if (!Constants.is_active) {
                    AndroidUtils.showRenewalPopup(getActivity());
                } else
                    CreateMembers();
            } else {
                ViewMembersData();
            }
        }
        ToggleViewUi();

//        tv_view_members.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_background));
//        tv_view_members.setTextColor(Color.WHITE);
        tv_create_members.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Constants.is_active) {
                    AndroidUtils.showRenewalPopup(getActivity());
                } else
                    CreateMembers();
            }
        });
        AndroidUtils.setupModuleView(
                tv_switchCreate,
                getString(R.string.view_members),
                false, true, getContext(), getString(R.string.create_members),
                clickedView -> {
                    // handle click
                    ViewMembersData();
                }
        );
        AndroidUtils.setupModuleView(
                tv_switchView,
                getString(R.string.create_members),
                true, true, getContext(), getString(R.string.list_members),
                clickedView -> {
                    if (!Constants.is_active) {
                        AndroidUtils.showRenewalPopup(getActivity());
                    } else {
                        CreateMembers();
                    }
                }
        );
        tv_view_members.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //tv_view_members.setTextColor(Color.WHITE);
//                String data = "View Members";
                ViewMembersData();
            }
        });
        AndroidUtils.NumberFilter(tv_default_rate, false);
        tv_sp_default_currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isVisible = sp_default_currency.getVisibility() == VISIBLE;
                AndroidUtils.DisplaySpinnerView(sp_default_currency, tv_sp_default_currency, default_currency1, img_dropdown_icon, img_clear_icon, !isVisible, currencyadapter, "Search Currency");
//                AndroidUtils.display_listview(iscurrency_checked, sp_default_currency);
                iscurrency_checked = !iscurrency_checked;
            }
        });
        img_clear_icon.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                AndroidUtils.DisplaySpinnerView(sp_default_currency, tv_sp_default_currency, default_currency1, img_dropdown_icon, img_clear_icon, false, currencyadapter, "Search Currency");
                iscurrency_checked = true;
            }
        });
        currency_list = AndroidUtils.getCurrency_list();
        currencyadapter = new CommonSpinnerAdapter(getActivity(), currency_list);
        sp_default_currency.setAdapter(currencyadapter);
        default_currency1 = currency_list.get(0);
        tv_sp_default_currency.setText(currency_list.get(0));
        tv_sp_default_currency.setText(default_currency1);
        img_clear_icon.setVisibility(VISIBLE);
        img_dropdown_icon.setVisibility(GONE);
        iscurrency_checked = true;
        sp_default_currency.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                default_currency1 = currencyadapter.getItem(position).toString();
//                default_currency1 = currency_list.get(position);
                AndroidUtils.DisplaySpinnerView(sp_default_currency, tv_sp_default_currency, default_currency1, img_dropdown_icon, img_clear_icon, false, currencyadapter, "Search Currency");
                iscurrency_checked = true;
            }
        });
        btn_save_members.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Validation()) {
                    String tag = "Create";
                    String id = "";
                    callCreateMemberWebservice(tv_member_name.getText().toString().trim(), tv_designation.getText().toString().trim(), tv_default_rate.getText().toString().trim(), tv_email.getText().toString().trim(), tv_confirm_email.getText().toString().trim(), tag, id);
                }
            }
        });

//        tv_email.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                response_email.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
////                if (!tv_email.getText().toString().trim().isEmpty()) {
////                    if (AndroidUtils.isValidEmail(tv_email.getText().toString()).matches()) {
//////                    tv_email.setError("Enter a valid email address");
//////                    AndroidUtils.showAlert("Please check the Email", getContext());
////                        response_email.setVisibility(View.VISIBLE);
////                        response_email.setText("Enter a valid email address");
//////                hasErrors = true;
////                    } else {
////                        response_email.setVisibility(View.GONE);
////                    }
////                }
//            }
//
//
//            public void afterTextChanged(Editable s) {
//                if (!tv_email.getText().toString().trim().isEmpty()) {
//                    if (AndroidUtils.isValidEmail(tv_email.getText().toString()).matches()) {
////                    tv_email.setError("Enter a valid email address");
////                    AndroidUtils.showAlert("Please check the Email", getContext());
//                        response_email.setVisibility(View.VISIBLE);
//                        response_email.setText(email_alert);
////                hasErrors = true;
//                    }
//                } else {
//                    response_email.setVisibility(View.GONE);
//                }
//            }
//        });
//        tv_confirm_email.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                response_cemail.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
////                if (!tv_confirm_email.getText().toString().trim().isEmpty()) {
////                    if (AndroidUtils.isValidEmail(tv_confirm_email.getText().toString()).matches()) {
//////                    tv_email.setError("Enter a valid email address");
//////                    AndroidUtils.showAlert("Please check the Email", getContext());
////                        response_cemail.setVisibility(View.VISIBLE);
////                        response_cemail.setText("Enter a valid email address");
//////                hasErrors = true;
////                    } else {
////                        response_cemail.setVisibility(View.GONE);
////                    }
////                }
//            }
//
//
//            public void afterTextChanged(Editable s) {
//                if (!tv_confirm_email.getText().toString().trim().isEmpty()) {
//                    if (AndroidUtils.isValidEmail(tv_confirm_email.getText().toString()).matches()) {
////                    tv_email.setError("Enter a valid email address");
////                    AndroidUtils.showAlert("Please check the Email", getContext());
//                        response_cemail.setVisibility(View.VISIBLE);
//                        response_cemail.setText(email_alert);
////                hasErrors = true;
//                    } else if (!tv_email.getText().toString().equals(tv_confirm_email.getText().toString())) {
//                        response_cemail.setVisibility(View.VISIBLE);
//                        response_cemail.setText(cemail_alert);
//                    }
//                } else {
//                    response_cemail.setVisibility(View.GONE);
//                }
//            }
//        });
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                CheckEmailAlert();
            }
        };
        tv_confirm_email.addTextChangedListener(textWatcher);
        tv_email.addTextChangedListener(textWatcher);
    }

    private void CheckEmailAlert() {
        response_email.setVisibility(View.GONE);
        response_cemail.setVisibility(View.GONE);

        String email = Objects.requireNonNull(tv_email.getText()).toString().trim();
        String confirmEmail = Objects.requireNonNull(tv_confirm_email.getText()).toString().trim();

        // Check email format only if not empty
        if (!email.isEmpty() && !AndroidUtils.isValidEmail(email)) {
            response_email.setVisibility(VISIBLE);
            response_email.setText(email_info_alert);
        }

        // Check confirm email format only if not empty
        if (!confirmEmail.isEmpty() && !AndroidUtils.isValidEmail(confirmEmail)) {
            response_cemail.setVisibility(VISIBLE);
            response_cemail.setText(email_info_alert);
        }

        // Check equality only if both are non-empty and valid emails
        if (!email.isEmpty() && !confirmEmail.isEmpty()
                && Patterns.EMAIL_ADDRESS.matcher(email).matches()
                && Patterns.EMAIL_ADDRESS.matcher(confirmEmail).matches()
                && !email.equals(confirmEmail)) {
            response_cemail.setVisibility(VISIBLE);
            response_cemail.setText(cemail_alert);
        }
    }

    private void setViewModelData(String data) {
        mViewModel.setData(data);
    }

    private void ToggleViewUi() {
        if (Constants.ROLE.equals("GH")) {
            tv_create_members.setVisibility(View.GONE);
            AndroidUtils.setupModuleView(
                    tv_switchView,
                    getString(R.string.create_members),
                    true, true, false, getContext(), getString(R.string.list_members),
                    clickedView -> {
                        if (!Constants.is_active) {
                            AndroidUtils.showRenewalPopup(getActivity());
                        } else {
                            CreateMembers();
                        }
                    }
            );
//            tv_switchView.setVisibility(GONE);
            tv_view_members.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.full_green_background));
        } else {
            tv_create_members.setVisibility(VISIBLE);
            tv_switchView.setVisibility(VISIBLE);
            tv_view_members.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_background));

        }
    }

    private void clearData() {
        tv_member_name.setText("");
        tv_designation.setText("");
        btn_save_members.setText(R.string.save);
        ll_save_buttons.setVisibility(VISIBLE);
//        sp_default_currency.setSelection();
        tv_default_rate.setText("");
        tv_email.setText("");
        tv_confirm_email.setText("");
//        currency_list.clear();
        members_list.clear();
        groupsList.clear();
        updatedMembersList.clear();
        rv_view_members.setVisibility(GONE);
        for (int i = 0; i < currency_list.size(); i++) {
            if (currency_list.get(i).equals("USDollar(USD)")) {
                sp_default_currency.setSelection(i);
                default_currency1 = currency_list.get(i);
                tv_sp_default_currency.setText(default_currency1);
                img_clear_icon.setVisibility(VISIBLE);
                img_dropdown_icon.setVisibility(GONE);
                iscurrency_checked = true;
            }
        }
    }

    private void callGroupsWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/groups", "Get Groups", postdata.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    //..
//            if (tv_member_name.getText().toString().trim().isEmpty()) {
//                tv_member_name.setError("Name is required");
//                hasErrors = true;
//            }
//
//            // Check if designation is empty
//            if (tv_designation.getText().toString().trim().isEmpty()) {
//                tv_designation.setError("Designation is required");
//                hasErrors = true;
//            }
//
//            // Check if default rate is empty
//            if (tv_default_rate.getText().toString().trim().isEmpty()) {
//                tv_default_rate.setError("Hourly rate is required");
//                hasErrors = true;
//            }
//
//            // Check if email is empty
//            if (tv_email.getText().toString().trim().isEmpty()) {
//                tv_email.setError("Email is required");
//                hasErrors = true;
//            }
//
//            // Check if confirm email is empty
//            if (tv_confirm_email.getText().toString().trim().isEmpty()) {
//                tv_confirm_email.setError("Confirm email is required");
//                hasErrors = true;
//            }
//
//            // Check if email and confirm email match
//            if (!tv_email.getText().toString().equals(tv_confirm_email.getText().toString())) {
//                tv_confirm_email.setError("Email and Confirm Email doesn't match");
//                hasErrors = true;
//            }
//        }
//
//        // Validate email format
//        else {
//            if (AndroidUtils.isValidEmail(tv_email.getText().toString()).matches()) {
//                tv_email.setError("Enter a valid email address");
//                hasErrors = true;
//            }
//        }
    //...
    private boolean Validation() {
        String email = tv_email.getText().toString().trim();
        String confirmEmail = tv_confirm_email.getText().toString().trim();
        boolean hasErrors = false;
        if (tv_member_name.getText().toString().trim().isEmpty() || tv_designation.getText().toString().trim().isEmpty() || tv_sp_default_currency.getText().toString().isEmpty() || Objects.requireNonNull(tv_default_rate.getText()).toString().trim().isEmpty() || tv_email.getText().toString().trim().isEmpty() || tv_confirm_email.getText().toString().trim().isEmpty()) {
            // Check if member name is empty
            hasErrors = true;
            //..
            String msg = "Please check the";
            if ((Objects.requireNonNull(tv_member_name.getText()).toString().trim().isEmpty())) {
                msg = msg + " Name";
            }
            if (Objects.requireNonNull(tv_designation.getText()).toString().trim().isEmpty()) {
//                    AndroidUtils.showAlert(msg + ", Task", getContext());
                if (msg.equals("Please check the")) {
                    msg = msg + " Designation";
                } else {
                    msg = msg + ", Designation";
                }
            }
            if (Objects.requireNonNull(tv_sp_default_currency.getText()).toString().trim().isEmpty()) {
                if (msg.equals("Please check the")) {
                    msg = msg + " Default Currency";
                } else {
                    msg = msg + ", Default Currency";
                }
            }
            if (Objects.requireNonNull(tv_default_rate.getText()).toString().trim().isEmpty()) {
                if (msg.equals("Please check the")) {
                    msg = msg + " Default Rate";
                } else {
                    msg = msg + ", Default Rate";
                }
            }
            if (Objects.requireNonNull(tv_email.getText()).toString().trim().isEmpty()) {
                if (msg.equals("Please check the")) {
                    msg = msg + " Email";
                } else {
                    msg = msg + ", Email";
                }
            }
            if (Objects.requireNonNull(tv_confirm_email.getText()).toString().trim().isEmpty()) {
                if (msg.equals("Please check the")) {
                    msg = msg + " Confirm Email";
                } else {
                    msg = msg + ", Confirm Email";
                }
            }
            AndroidUtils.showAlert(msg, getActivity());
            // Validate email format
            //..
        } else {
            // Validate email format
            if (!AndroidUtils.isValidEmail(email)) {
                AndroidUtils.showAlert(email_info_alert, getActivity());
                hasErrors = true;
            } else if (!AndroidUtils.isValidEmail(confirmEmail)) {
                AndroidUtils.showAlert(email_info_alert, getActivity());
                hasErrors = true;
            } else if (!email.equals(confirmEmail)) {
                AndroidUtils.showAlert(cemail_alert, getActivity());
                hasErrors = true;
            }
        }
        return !hasErrors;
    }

    private void CreateMembersData() {
        if (Objects.equals(TAG, "UGA")) {
            cv_details.setVisibility(GONE);
            ll_new_buttons.setVisibility(GONE);
            ll_buttons.setVisibility(GONE);
            cv_members_details.setVisibility(VISIBLE);
            tl_search_teammember.setVisibility(GONE);
            tv_tot_license.setVisibility(GONE);
            members_list.clear();
        } else {
            cv_details.setVisibility(VISIBLE);
            cv_members_details.setVisibility(GONE);
            members_list.clear();
        }
        if (Objects.equals(TAG, "UGA")) {
            callGroupsWebservice();
        }
        rv_view_members.removeAllViews();
    }

    private void HideSelectGroups() {
        if (!Objects.equals(FLAG, "second_click")) {
            if (Validation()) {
                cv_members_details.setVisibility(VISIBLE);
                callGroupsWebservice();
                ll_save_buttons.setVisibility(GONE);
            }
        } else {
            FLAG = "first_click";
//                    cv_members_details.setVisibility(View.VISIBLE);
//                    callGroupsWebservice();
            cv_members_details.setVisibility(GONE);
            ll_save_buttons.setVisibility(VISIBLE);
            groupsList.clear();
        }
    }

    private void CreateMembers() {
        clearData();
//                cv_members_details.setVisibility(View.GONE);
        tv_create_members.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        tv_view_members.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
        tv_switchCreate.setVisibility(VISIBLE);
        tv_switchView.setVisibility(GONE);
        ll_confirm_email.setVisibility(VISIBLE);
        ll_new_buttons.setVisibility(GONE);
        TAG = "CM";
//                String data = "Create Members";
        isCreateMatter = true;
        setViewModelData("Create Members");
        CreateMembersData();
        callViewGroupsWebservice();
        FLAG = "first_click";
        tl_search_teammember.setVisibility(GONE);
        tv_tot_license.setVisibility(GONE);
//                search_teammember.setVisibility(GONE);
        tv_create_members.setTextColor(getContext().getResources().getColor(R.color.white));
        tv_view_members.setTextColor(getContext().getResources().getColor(R.color.black));
        rv_view_members.setVisibility(GONE);
    }

    private void ViewMembersData() {
        tv_create_members.setTextColor(getContext().getResources().getColor(R.color.black));
        tv_view_members.setTextColor(getContext().getResources().getColor(R.color.white));
        ll_buttons.setVisibility(VISIBLE);
        isCreateMatter = false;
        ToggleViewUi();
        tv_switchCreate.setVisibility(GONE);
        tv_switchView.setVisibility(VISIBLE);
        tv_create_members.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
        cv_details.setVisibility(GONE);
        cv_members_details.setVisibility(GONE);
        callViewGroupsWebservice();
        et_search_members.setText("");
//        search_teammember.setVisibility(View.VISIBLE);
        tl_search_teammember.setVisibility(VISIBLE);
        tv_tot_license.setVisibility(VISIBLE);
        et_search_teammember.setText("");
        members_list.clear();
        groupsList.clear();
        updatedMembersList.clear();
        setViewModelData("View Members");
    }

    private void callViewGroupsWebservice() {
        try {
            JSONObject postdata = new JSONObject();
            progress_dialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/members", "Get Members", postdata.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    private void callCreateMemberWebservice(String name, String designation, String
            default_rate, String email, String confirm_email, String tag, String id) {
        try {
            JSONObject postdata = new JSONObject();
            JSONArray groups = new JSONArray();
            if (groupsAdapter != null) {
                for (int i = 0; i < groupsAdapter.getList_item().size(); i++) {
                    ViewGroupModel viewGroupModel = groupsAdapter.getList_item().get(i);
                    if (viewGroupModel.isChecked()) {
                        groups.put(viewGroupModel.getId());
                    }
                }
            }

            progress_dialog = AndroidUtils.get_progress(getActivity());
            if (Objects.equals(tag, "Create") || Objects.equals(tag, "Update")) {
                postdata.put("currency", default_currency1);
                postdata.put("defaultRate", default_rate);
                postdata.put("designation", designation);
                postdata.put("email", email);
                postdata.put("emailConfirm", confirm_email);
                postdata.put("name", name);
            }

            if (Objects.equals(tag, "Create") || tag == "UGA")
                postdata.put("groups", groups);
            if (Objects.equals(tag, "RP")) {
                postdata.put("memberId", id);
            }
            if (Objects.equals(tag, "Upgrade as Practice Partner")) {
                postdata.put("id", id);
            }
            if (Objects.equals(tag, "Create")) {
                Log.i("Tag", "Info:" + postdata.toString());
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v3/member", "Create Members", postdata.toString());

            } else if (Objects.equals(tag, "RP")) {
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v3/member/resetpwd", "Reset Password", postdata.toString());

            } else if (Objects.equals(tag, "Delete")) {
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.DELETE, "v3/member/" + id, "Delete Member", postdata.toString());


            } else if (Objects.equals(tag, "Upgrade as Practice Partner")) {
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PATCH, "v3/convert/practice-partner", "Upgrade as Practice Partner", postdata.toString());


            } else {
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PATCH, "v3/member/" + id, "Update Members", postdata.toString());
            }
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.btn_create:
                    if (Validation()) {
                        String tag = "Create";
                        String id = "";
                        callCreateMemberWebservice(tv_member_name.getText().toString().trim(), tv_designation.getText().toString().trim(), tv_default_rate.getText().toString().trim(), tv_email.getText().toString().trim(), tv_confirm_email.getText().toString().trim(), tag, id);
                    }
                    break;
                case R.id.btn_cancel_save:
                    clearData();
                    break;
            }
        } catch (Exception e) {
            Log.e("TAG", "Error:" + e.getMessage());
            e.fillInStackTrace();
        }
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {

        if (progress_dialog != null && progress_dialog.isShowing())
            AndroidUtils.dismiss_dialog(progress_dialog);

        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {

            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());

                switch (httpResult.getRequestType()) {

                    case "Create Members":
                        if (httpResult.getStatus_code() == 200) {
                            AndroidUtils.showAlert(result.getString("msg"), getActivity());
                            ViewMembersData();
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), getActivity());
                        }
                        break;

                    case "Update Members":
                        if (httpResult.getStatus_code() == 200) {
                            AndroidUtils.showAlert(result.getString("msg"), getActivity());
                            ViewMembersData();
                            ll_new_buttons.setVisibility(GONE);
                            tv_assign_groups.setVisibility(VISIBLE);
                            ll_save_buttons.setVisibility(VISIBLE);
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), getActivity());
                        }
                        break;

                    case "Reset Password": {
                        JSONObject data = result.getJSONObject("data");
                        AndroidUtils.showAlert(data.getString("msg"), getActivity());
                        ViewMembersData();
                        break;
                    }

                    case "Upgrade as Practice Partner":
                        if (httpResult.getStatus_code() == 200) {
                            AndroidUtils.showAlert(result.getString("msg"), getActivity());
                            ViewMembersData();
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), getActivity());
                        }
                        break; // REQUIRED

                    case "Delete Member":
                        if (httpResult.getStatus_code() == 200) {
                            AndroidUtils.showAlert(result.getString("msg"), getActivity());
                            ViewMembersData();
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), getActivity());
                        }
                        break;

                    case "Get Members": {

                        JSONObject data = result.getJSONObject("data");
                        JSONArray users = data.getJSONArray("users");
                        total = data.getString("total");
                        count = data.getString("count");

                        String frozenText = getString(R.string.your_present_subscription_does_not_allow_creation_of_more_users);
                        FragmentManager fm = getChildFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        Fragment disabledView = fm.findFragmentByTag("DISABLED_VIEW");

                        Constants.isSubscriptionEnded = false;
                        if (total.equals(count)) {
                            Constants.isSubscriptionEnded = true;
                        }

                        if (isCreateMatter) {
                            if (total.equals(count)) {
                                Disabled_view view = new Disabled_view(frozenText, true);
                                ft.add(R.id.child_container, view, "DISABLED_VIEW");
                                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                ft.addToBackStack(null);
                                ft.commit();

                                child_container.setAlpha(0.5f);
                                tv_member_name.setEnabled(false);
                                tv_designation.setEnabled(false);
                                tv_sp_default_currency.setEnabled(false);
                                tv_default_rate.setEnabled(false);
                                tv_email.setEnabled(false);
                                tv_confirm_email.setEnabled(false);
                                tv_assign_groups.setEnabled(false);
                                btn_create.setEnabled(false);
                            }
                        } else {
                            if (disabledView != null) {
                                ft.remove(disabledView);
                                ft.commit();
                            }
                            child_container.setAlpha(1.0f);
                            tv_member_name.setEnabled(true);
                            tv_designation.setEnabled(true);
                            tv_sp_default_currency.setEnabled(true);
                            tv_default_rate.setEnabled(true);
                            tv_email.setEnabled(true);
                            tv_confirm_email.setEnabled(true);
                            tv_assign_groups.setEnabled(true);
                            btn_create.setEnabled(true);
                            loadMembers(users);
                        }
                        break;
                    }

                    case "Get Groups": {
                        JSONArray data = result.getJSONArray("data");
                        loadViewGroups(data);
                        break;
                    }
                }

            } catch (Exception e) {
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


    private void loadViewGroups(JSONArray data) throws JSONException {
        groupsList.clear();

        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            ViewGroupModel viewGroupModel = new ViewGroupModel();
            viewGroupModel.setId(jsonObject.optString("id"));

            String date = jsonObject.optString("created");
            if (date.contains("T")) { // ISO format
                Date date_new = AndroidUtils.stringToDateTimeDefault(date, "yyyy-MM-dd'T'HH:mm:ss.SSS");
                String created = AndroidUtils.getDateToString(date_new, "MMM dd YYYY");
                viewGroupModel.setCreated(created);
            } else { // already formatted
                if (!date.isEmpty()) {
                    Date date_new = AndroidUtils.stringToDateTimeDefault(date, "MMM dd, yyyy, hh:mm a");
                    String created = AndroidUtils.getDateToString(date_new, "MMM dd, yyyy | hh:mm a");
                    viewGroupModel.setCreated(created);
                } else {
                    viewGroupModel.setCreated("");
                }
            }

            viewGroupModel.setIsdisabled(jsonObject.optBoolean("isdisabled"));
            viewGroupModel.setMembers(jsonObject.optJSONArray("members"));
            viewGroupModel.setMemberCount(jsonObject.optString("memberCount"));
            viewGroupModel.setDescription(jsonObject.optString("description"));
            viewGroupModel.setName(jsonObject.optString("name"));

            JSONObject group_head = jsonObject.optJSONObject("groupHead");
            if (group_head != null) {
                viewGroupModel.setGroup_head_id(group_head.optString("id"));
                viewGroupModel.setGroup_head_name(group_head.optString("name"));
                viewGroupModel.setOwner_name(group_head.optString("name"));
            }

            // Check against updatedMembersList
            for (int k = 0; k < updatedMembersList.size(); k++) {
                if (viewGroupModel.getId().matches(updatedMembersList.get(k).getGroup_id())) {
                    viewGroupModel.setChecked(true);
                    break;
                }
            }

            groupsList.add(viewGroupModel);
        }

        // ✅ Reorder groups: AAM → SuperUser → Others
        groupsList.sort((a, b) -> {
            String nameA = a.getName() != null ? a.getName() : "";
            String nameB = b.getName() != null ? b.getName() : "";

            if (nameA.equalsIgnoreCase("AAM")) return -1;
            if (nameB.equalsIgnoreCase("AAM")) return 1;

            if (nameA.equalsIgnoreCase("SuperUser")) return -1;
            if (nameB.equalsIgnoreCase("SuperUser")) return 1;

            return 0; // preserve original relative order otherwise
        });

        Log.i("ArrayList", "info " + groupsList.toString());

        loadGroupsRecylerview();
        loadRecylcerview();
    }


    private void loadGroupsRecylerview() {
//        if (groupsList.size() != 0) {
        FLAG = "second_click";
        rv_selected_member.setLayoutManager(new GridLayoutManager(getContext(), 1));
        groupsAdapter = new GroupsAdapter(groupsList);
        rv_selected_member.setAdapter(groupsAdapter);
        //rv_selected_member.setHasFixedSize(true);
        et_search_members.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                groupsAdapter.getFilter().filter(et_search_members.getText().toString());
            }

        });
        if (Objects.equals(TAG, "UGA")) {
            btn_cancel_members.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String data = "View Members";
                    setViewModelData(data);
                    ViewMembersData();
                }
            });
        } else {
            btn_cancel_members.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FLAG = "second_click";
                    HideSelectGroups();
                    clearData();
                }
            });
        }
//        btn_cancel_save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AndroidUtils.showAlert("Cancel3 btn clicked",getContext());
//            }
//        });
        //  btn_cancel_members.setOnClickListener(new View.OnClickListener() {
        //   @Override
        //   public void onClick(View view) {
        //    et_search_members.setText("");
        //  groupsList.clear();
        // callGroupsWebservice();
        // }
        // });

//        }
//        else {
////            cv_members_details.setVisibility(View.GONE);
//        }
    }

    private void loadMembers(JSONArray users) throws JSONException {
        MembersModel membersModel;
        members_list.clear();
        for (int i = 0; i < users.length(); i++) {
            JSONObject jsonObject = users.getJSONObject(i);
            membersModel = new MembersModel();
            membersModel.setId(jsonObject.getString("id"));
            membersModel.setName(jsonObject.getString("name"));
            membersModel.setIsdisabled(jsonObject.optBoolean("isdisabled"));
            membersModel.setCurrency(jsonObject.getString("currency"));
            membersModel.setDefaultRate(jsonObject.getString("defaultRate"));
            membersModel.setDesignation(jsonObject.getString("designation"));
            membersModel.setEmail(jsonObject.getString("email"));
            membersModel.setLastLogin(jsonObject.getString("lastLogin"));
            membersModel.setGroups(jsonObject.getJSONArray("groups"));
            members_list.add(membersModel);
        }
        loadRecylcerview();
    }

    private void loadRecylcerview() {
        tv_tot_license.setText("Number of Licenses : " + count + " out of " + total);
        rv_view_members.setVisibility(VISIBLE);

        MembersAdapter adapter = new MembersAdapter(members_list, getContext(), this, Members.this);
        rv_view_members.setAdapter(adapter);
        AndroidUtils.LoadingRecyclerview(rv_view_members, getContext());
        AndroidUtils.setupBottomSpacerFooter(rv_view_members, getResources().getDimensionPixelSize(R.dimen.twentyeight_dp));
//        rv_view_members.setLayoutAnimation(
//                AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fall_down)
//        );
//        rv_view_members.scheduleLayoutAnimation();

        et_search_teammember.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                adapter.getFilter().filter(et_search_teammember.getText().toString());
            }
        });
    }

    @Override
    public void EditMember(MembersModel membersModel) {
        CreateMembersData();
        rv_view_members.setVisibility(GONE);
        cv_details.setVisibility(VISIBLE);
        tl_search_teammember.setVisibility(GONE);
        tv_tot_license.setVisibility(GONE);
        ll_buttons.setVisibility(GONE);
        ll_save_buttons.setVisibility(GONE);
        tv_assign_groups.setVisibility(GONE);
        ll_new_buttons.setVisibility(VISIBLE);
        cv_members_details.setVisibility(GONE);
        ll_confirm_email.setVisibility(VISIBLE);
        img_dropdown_icon.setVisibility(GONE);
        iscurrency_checked = true;
        img_clear_icon.setVisibility(VISIBLE);
        tv_member_name.setText(membersModel.getName());
        tv_member_name.addTextChangedListener(new Validation((EditText) tv_member_name));
        tv_email.setText(membersModel.getEmail());
        tv_email.addTextChangedListener(new Validation((EditText) tv_email));
        tv_default_rate.setText(membersModel.getDefaultRate());
        tv_default_rate.addTextChangedListener(new Validation(tv_default_rate));
        tv_confirm_email.setVisibility(VISIBLE);
        tv_confirm_email.setText(tv_email.getText().toString());
        tv_confirm_email.addTextChangedListener(new Validation((EditText) tv_confirm_email));
        for (int i = 0; i < currency_list.size(); i++) {
            if (currency_list.get(i).equals(membersModel.getCurrency())) {
                sp_default_currency.setSelection(i);
                tv_sp_default_currency.setText(currency_list.get(i));
            }
        }
        tv_designation.setText(membersModel.getDesignation());
        bt_save.setText(R.string.save);
        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Validation()) {
                    String tag = "Update";
                    String id = membersModel.getId();
                    callCreateMemberWebservice(tv_member_name.getText().toString().trim(), tv_designation.getText().toString().trim(), tv_default_rate.getText().toString().trim(), tv_email.getText().toString().trim(), tv_confirm_email.getText().toString().trim(), tag, id);
                }
            }
        });
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                ll_confirm_email.setVisibility(View.GONE);
//                membersModel.
                UnHide();
                ViewMembersData();
                ////Edit Member is Cancelled..
                tv_assign_groups.setVisibility(VISIBLE);
                ll_save_buttons.setVisibility(VISIBLE);
            }
        });
        rv_view_members.removeAllViews();
        rv_selected_member.removeAllViews();
    }

    public void UpdateGroupAccess(MembersModel membersModel) throws JSONException {

        TAG = "UGA";
        for (int i = 0; i < membersModel.getGroups().length(); i++) {
            ViewGroupModel viewGroupModel = new ViewGroupModel();
            JSONObject jsonObject = membersModel.getGroups().getJSONObject(i);
            viewGroupModel.setGroup_id(jsonObject.getString("id"));
            viewGroupModel.setGroup_name(jsonObject.getString("name"));
            updatedMembersList.add(viewGroupModel);
        }
        CreateMembersData();
        btn_cancel_members.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = "View Members";
                setViewModelData(data);
                ViewMembersData();
            }
        });
        btn_save_members.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tag = "UGA";
                callCreateMemberWebservice("", "", "", "", "", tag, membersModel.getId());
            }
        });
        btn_save_members.setText(R.string.save);
    }

    @Override
    public void ResetPassword(MembersModel membersModel) {
        try {
            AndroidUtils.showConfirmationDialog(requireContext(), "Confirmation", getString(R.string.reset_password_tm) + membersModel.getName() + "?", membersModel.getName(), new AndroidUtils.OnConfirmListener() {
                        @Override
                        public void onSave() {
//                                    dialog.dismiss();
                            String tag = "RP";
                            callCreateMemberWebservice("", "", "", "", "", tag, membersModel.getId());
                        }

                        @Override
                        public void onCancel() {
//                                dialog.dismiss();
                            ViewMembersData();
                        }
                    }
            );
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    @Override
    public void DeleteMember(MembersModel membersModel) {
        try {
            AndroidUtils.showConfirmationDialog(requireContext(), "Confirmation", getString(R.string.delete_team_member) + membersModel.getName() + " ?", membersModel.getName(), new AndroidUtils.OnConfirmListener() {
                        @Override
                        public void onSave() {
//                                    dialog.dismiss();
                            String tag = "Delete";
                            callCreateMemberWebservice("", "", "", "", "", tag, membersModel.getId());
                        }

                        @Override
                        public void onCancel() {
//                                dialog.dismiss();
                            ViewMembersData();
                        }
                    }
            );
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    @Override
    public void Upgrade(MembersModel membersModel) {
        try {
            AndroidUtils.showConfirmationDialog(requireContext(), "Confirmation", getString(R.string.upgrade_members) + " " + membersModel.getName() + " " + "Practice Partner and provide Super User access" + " ?", membersModel.getName(), new AndroidUtils.OnConfirmListener() {
                        @Override
                        public void onSave() {
//                                    dialog.dismiss();
                            String tag = "Upgrade as Practice Partner";
                            callCreateMemberWebservice("", "", "", "", "", tag, membersModel.getId());
                        }

                        @Override
                        public void onCancel() {
//                                dialog.dismiss();
                            ViewMembersData();
                        }
                    }
            );
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }


    private void UnHide() {
        ll_buttons.setVisibility(VISIBLE);
        tv_confirm_email.setVisibility(VISIBLE);
        tl_search_members.setVisibility(VISIBLE);
        tl_search_teammember.setVisibility(VISIBLE);
        tv_tot_license.setVisibility(VISIBLE);
        assign_group.setVisibility(VISIBLE);
        tv_confirm_email.setText("");
        tv_designation.setText("");
        tv_email.setText("");
        tv_member_name.setText("");
        tv_default_rate.setText("");
    }

    public void model_name(String action_list) {
        if (Objects.equals(action_list, "Edit Member")) {
//            mViewModel.setData("Edit Member");
        } else if (Objects.equals(action_list, "Update Group Access")) {
//            mViewModel.setData("Update Group Access");
        } else if (Objects.equals(action_list, "Delete Member")) {
//            mViewModel.setData("Delete Member");
        } else {
//            mViewModel.setData("View Members");
        }
    }

}

