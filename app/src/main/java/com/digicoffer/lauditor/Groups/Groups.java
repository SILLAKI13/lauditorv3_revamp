package com.digicoffer.lauditor.Groups;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Groups.Adapters.GroupAdapters;
import com.digicoffer.lauditor.Groups.Adapters.SearchAdapter;
import com.digicoffer.lauditor.Groups.Adapters.ViewGroupsAdpater;
import com.digicoffer.lauditor.Groups.Models.ActionModel;
import com.digicoffer.lauditor.Groups.Models.GroupModel;
import com.digicoffer.lauditor.Groups.Models.SearchDo;
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.ItemClickListener;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.DescriptionValidation;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation;
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
import java.util.Objects;

public class Groups extends Fragment implements AsyncTaskCompleteListener, ViewGroupsAdpater.InterfaceListener {
    RecyclerView rv_select_team_members, rv_view_groups, rv_activity_log;
    TextInputEditText et_search, tv_description, tv_search_name, tv_Client_name_id, et_search_delete, et_search_message;
    //Initializing a search TextInputLayout..
    CardView cv_delete_team;
    CommonSpinnerAdapter GroupsAdapter;
    boolean isDelete = false;
    boolean is_group_name_new = true;
    //rename of title name
    TextView description_name, category_id, team_member_id, from_id, to_id, client_id_name, search_name, group_name_delete;
    AppCompatButton tv_from_date, tv_to_date;
    TextInputLayout tv_selected_members;
    private NewModel mViewModel;
    AppCompatButton btn_save_selected_tm;
    ItemClickListener itemClickListener;
    ViewGroupModel new_viewGroupModel = null;
    public static String FLAG = "";
    TextInputLayout tv_search_message, tv_select_team_members;
    ViewGroupsItemClickListener new_itemClickListener;
    String group_head = "";
    String group_name = "";
    ArrayList<SearchDo> searchList = new ArrayList<>();
    Spinner sp_category, sp_team_member;
    String selected_category = "";
    String selected_tm = "";
    ArrayList<ActionModel> actions_List = new ArrayList<ActionModel>();
    TextView tv_create_group, tv_view_group, tv_add_tm, tv_practice_head;
    TextInputEditText tv_group_name, tv_group_description;
    ArrayList<GroupModel> selectedTMArrayList = new ArrayList<GroupModel>();
    ArrayList<GroupModel> TMArrayList = new ArrayList<GroupModel>();
    public static String TM_TYPE = "";
    ArrayList<ViewGroupModel> viewGroupModelArrayList = new ArrayList<>();
    ArrayList<ViewGroupModel> viewGroupNameArrayList = new ArrayList<>();
    ArrayList<ViewGroupModel> viewGroupMembersList = new ArrayList<>();
    ArrayList<ViewGroupModel> updateGroupMembersList = new ArrayList<>();
    ArrayList<GroupModel> assignGroupsList = new ArrayList<>();
    private CheckBox chk_select_all;
    CardView cv_groups, cv_details, cv_activity_log;
    GroupAdapters adapter = null;
    ViewGroupsAdpater adapter_delete = null;
    ViewGroupsAdpater adapter_view_groups = null;
    Dialog progress_dialog;
    View v = null;
    LinearLayout ll_deleteGroups, tv_sp_groups, ll_Delete_view;
    ListView sp_groupslist;
    ImageView clear_icon, dropdown_icon;
    TextView group_head_name, select_ghead, tv_delete_groups, tv_documents, tv_matters, tv_relationships, tv_members;
    AppCompatButton btn_cancel, btn_save, btn_cancel_edit, btn_update, btn_cancel_gal, btn_search_gal;
    TextInputEditText et_search_tm;
    TextView tv_label;
    LinearLayout tv_switchCreate, tv_switchView;
    LinearLayoutCompat ll_Views, ll_tm, ll_select_all, ll_buttons, ll_group_list, ll_select_tm, ll_edit_groups;
    String selectedGroupName = "";
    View tl_et_search;

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
//                    AndroidUtils.showAlert("Dashboard", getContext());
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
        v = inflater.inflate(R.layout.groups, container, false);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(NewModel.class);
        rv_select_team_members = v.findViewById(R.id.rv_selected_tm);
        rv_view_groups = v.findViewById(R.id.rv_view_group);
        tl_et_search = v.findViewById(R.id.tl_et_search);
        et_search = tl_et_search.findViewById(R.id.et_Search);
        et_search.addTextChangedListener(new Validation(et_search));
        //..
        ll_deleteGroups = v.findViewById(R.id.ll_deleteGroups);
        ll_Delete_view = v.findViewById(R.id.ll_Delete_view);
        tv_documents = v.findViewById(R.id.tv_documents);
        tv_matters = v.findViewById(R.id.tv_matters);
        tv_relationships = v.findViewById(R.id.tv_relationships);
        tv_members = v.findViewById(R.id.tv_members);
        ll_Views = v.findViewById(R.id.ll_Views);
        tv_sp_groups = v.findViewById(R.id.tv_sp_groups);
        tv_delete_groups = tv_sp_groups.findViewById(R.id.tv_spinner_view);
        tv_delete_groups.setHint(R.string.select_group_name);
        clear_icon = tv_sp_groups.findViewById(R.id.img_clear_icon);
        dropdown_icon = tv_sp_groups.findViewById(R.id.img_dropdown_icon);
        //..
        sp_groupslist = v.findViewById(R.id.sp_groupslist);
//        et_search.setHint(R.string.search_team_members);
        tv_group_name = v.findViewById(R.id.tv_group_name);
        tv_group_name.setHint(R.string.group_name);
        select_ghead = v.findViewById(R.id.select_ghead);
        select_ghead.setTextSize(DynamicUtils.twenty);
        select_ghead.setText(R.string.select_head_of_the_group);
        tv_group_description = v.findViewById(R.id.tv_description);
        ll_edit_groups = v.findViewById(R.id.ll_edit_buttons);
        cv_groups = v.findViewById(R.id.cv_details);
        tv_label = v.findViewById(R.id.tv_label);
//        search_delete = v.findViewById(R.id.search_delete);
//        search_tm = v.findViewById(R.id.search_tm);
        View tl_search_appointments = v.findViewById(R.id.tl_et_search_delete);
        et_search_delete = tl_search_appointments.findViewById(R.id.et_Search);
        et_search_delete.setHint(R.string.search_groups);
        et_search_delete.addTextChangedListener(new Validation(et_search_delete));
        group_name_delete = v.findViewById(R.id.group_name_delete);
        group_name_delete.setGravity(Gravity.START);
        group_name_delete.setTypeface(Typeface.DEFAULT_BOLD);
        cv_delete_team = v.findViewById(R.id.cv_delete_team);
        tv_switchCreate = v.findViewById(R.id.tv_switchCreate);
        tv_switchView = v.findViewById(R.id.tv_switchView);
        //view_groups components...
        search_name = v.findViewById(R.id.search_name);
        search_name.setText(R.string.search);
        client_id_name = v.findViewById(R.id.client_id_name);
        client_id_name.setText(R.string.client);
//        search = v.findViewById(R.id.search);
        from_id = v.findViewById(R.id.from_id);
        from_id.setText(R.string.from);
        to_id = v.findViewById(R.id.to_id);
        to_id.setText(R.string.to);
        tv_search_name = v.findViewById(R.id.tv_search_name);
        tv_search_name.setHint(R.string.search);
        tv_Client_name_id = v.findViewById(R.id.tv_Client_name_id);
        tv_Client_name_id.setHint(R.string.client);
        //Changing sub_module name..
//        tv_description = v.findViewById(R.id.tv_description);
        tv_group_description.setHint(R.string.description);
        description_name = v.findViewById(R.id.description_name);
        description_name.setText(R.string.description);
        category_id = v.findViewById(R.id.category_id);
        category_id.setText(R.string.category);
        team_member_id = v.findViewById(R.id.team_member_id);
        team_member_id.setText(R.string.team_members);

        tv_select_team_members = v.findViewById(R.id.filledTextField3);
        tv_search_message = v.findViewById(R.id.search_message);
        tv_from_date = v.findViewById(R.id.btn_from_date);
        tv_from_date.setHint(R.string.from);
        tv_to_date = v.findViewById(R.id.btn_to_date);
        tv_to_date.setHint(R.string.to);
        View tl_et_search_tm = v.findViewById(R.id.tl_et_search_tm);
        et_search_tm = tl_et_search_tm.findViewById(R.id.et_Search);
        et_search_tm.setHint(R.string.search_team_members);
        et_search_tm.addTextChangedListener(new Validation(et_search_tm));
        group_head_name = (TextView) v.findViewById(R.id.group_head_name);
        cv_details = v.findViewById(R.id.cv_details_2);
        btn_cancel_gal = v.findViewById(R.id.btn_cancel_activity_log);
//        btn_search_gal = v.findViewById(R.id.btn_update_activity_log);
//        btn_search_gal.setText(R.string.search);

        cv_activity_log = v.findViewById(R.id.cv_details_activity_log);
        ll_tm = v.findViewById(R.id.linearLayoutCompat1);
        btn_cancel_edit = v.findViewById(R.id.btn_cancel_edit);
        //changing a submit button text to update..
        btn_update = v.findViewById(R.id.btn_update);
        btn_update.setText(R.string.update);
        ll_select_all = v.findViewById(R.id.ll_select_all);
        ll_group_list = v.findViewById(R.id.ll_Views);
        ll_select_tm = v.findViewById(R.id.linearLayoutCompat2);
        ll_buttons = v.findViewById(R.id.ll_buttons);
        tv_create_group = v.findViewById(R.id.tv_create_group);
        tv_create_group.setText(R.string.create_group);
        tv_view_group = v.findViewById(R.id.tv_view_group);
        tv_view_group.setText(R.string.view_group);
        tv_add_tm = v.findViewById(R.id.add_tm);
        tv_add_tm.setText(R.string.add_team_member);
        tv_practice_head = v.findViewById(R.id.add_phead);
        tv_practice_head.setText(R.string.add_practice_head);
        sp_category = v.findViewById(R.id.sp_category);
//        sp_team_member = v.findViewById(R.id.sp_team_member);
        btn_cancel = v.findViewById(R.id.btn_cancel);
        chk_select_all = v.findViewById(R.id.chk_select_all);
        btn_save = (AppCompatButton) v.findViewById(R.id.btn_save);
        //Making a view group as a default view...
//        setViewModelData(getString(R.string.view_group));
//        ViewGroupsData();
        tv_group_name.addTextChangedListener(new Validation(tv_group_name));
        tv_group_description.addTextChangedListener(new DescriptionValidation(tv_group_description));
//        tv_view_group.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_background));
        //tv_add_tm.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        AndroidUtils.setupEdgePaddingBehavior(rv_view_groups);
        tv_view_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewGroupsData();
            }
        });

        if (Constants.ROLE.equals("GH")) {
            ViewGroupsData();
            tv_create_group.setVisibility(GONE);
            AndroidUtils.setupModuleView(
                    tv_switchView,
                    getString(R.string.create_group),
                    true, true, false, getContext(), getString(R.string.list_groups),
                    clickedView -> {
                        if (!Constants.is_active) {
                            AndroidUtils.showRenewalPopup(getActivity());
                        } else {
                            if (!Constants.is_active) {
                                AndroidUtils.showRenewalPopup(getActivity());
                            } else
                                CreateGroupData();
                        }
                    }
            );
        } else {
            if (Constants.isCreate) {
                CreateGroupData();
            } else {
                ViewGroupsData();
            }
            tv_create_group.setVisibility(VISIBLE);
        }
        tv_practice_head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (assignGroupsList.isEmpty()) {
                    select_ghead.setVisibility(GONE);
                    AndroidUtils.showAlert("Please check the member selection", getActivity(), "Info");
                } else {
                    select_ghead.setVisibility(VISIBLE);
                    et_search_tm.setText("");
//                    if (!group_head.isEmpty()) {
//                        for (int i = 0; i < assignGroupsList.size(); i++) {
//                            GroupModel groupModel = assignGroupsList.get(i);
//                            if (groupModel.getId().equals(group_head)) {
//                                groupModel.setChecked(true);
//                                selectedGHArrayList.add(groupModel);
//                            }
//                        }
//                    }
                    assignGroupHead(TMArrayList);
                    ll_select_all.setVisibility(GONE);
                    tv_practice_head.setTextColor(requireContext().getResources().getColor(R.color.white));
                    tv_add_tm.setTextColor(requireContext().getResources().getColor(R.color.black));
                    tv_add_tm.setBackgroundDrawable(requireActivity().getResources().getDrawable(R.drawable.button_left_background));
                    tv_practice_head.setBackgroundDrawable(requireActivity().getResources().getDrawable(R.drawable.button_right_green_count));
                }
            }
        });
        AndroidUtils.setupModuleView(
                tv_switchCreate,
                getString(R.string.view_group),
                false, true, getContext(), getString(R.string.create_group),
                clickedView -> {
                    // handle click
                    ViewGroupsData();
                }
        );
        AndroidUtils.setupModuleView(
                tv_switchView,
                getString(R.string.create_group),
                true, true, getContext(), getString(R.string.list_groups),
                clickedView -> {
                    if (!Constants.is_active) {
                        AndroidUtils.showRenewalPopup(getActivity());
                    } else {
                        if (!Constants.is_active) {
                            AndroidUtils.showRenewalPopup(getActivity());
                        } else
                            CreateGroupData();
                    }
                }
        );
        tv_create_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Constants.is_active) {
                    AndroidUtils.showRenewalPopup(getActivity());
                } else
                    CreateGroupData();
            }
        });
        tv_add_tm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Changing a text color in Add team-member design when clicking...
                group_head = "";
                tv_add_tm.setTextColor(requireContext().getResources().getColor(R.color.white));
                tv_practice_head.setTextColor(requireContext().getResources().getColor(R.color.black));
                tv_practice_head.setBackgroundDrawable(requireActivity().getResources().getDrawable(R.drawable.button_right_background));
                tv_add_tm.setBackgroundDrawable(requireActivity().getResources().getDrawable(R.drawable.button_left_green_background));
                ll_select_all.setVisibility(VISIBLE);
                select_ghead.setVisibility(GONE);
                chk_select_all.setChecked(false);
                if (!TMArrayList.isEmpty()) {
//                    AndroidUtils.showAlert("No team Members selected", getContext());
                    TM_TYPE = "TM";
                    loadRecylcerview(TMArrayList, TM_TYPE);
//                    if (Objects.equals(tmType, "TM")) {
////                        ll_select_all.setVisibility(View.GONE);
//                        et_Search.setText("");
//                        assignGroupHead(adapter.getList_item());
//                    }
                } else {
                    adapter.selectOrDeselectAll(false);
                    adapter.getList_item().clear();
                    selectedTMArrayList.clear();
                    assignGroupsList.clear();
                    rv_select_team_members.removeAllViews();
                    callMembersWebservice();
                }
            }
        });
        tv_group_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains("\n")) {
                    tv_group_name.setText(s.toString().replace("\n", ""));
                    tv_group_name.setSelection(Objects.requireNonNull(tv_group_name.getText()).length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_search_tm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains("\n")) {
                    et_search_tm.setText(s.toString().replace("\n", ""));
                    et_search_tm.setSelection(Objects.requireNonNull(et_search_tm.getText()).length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_search_delete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains("\n")) {
                    et_search_delete.setText(s.toString().replace("\n", ""));
                    et_search_delete.setSelection(Objects.requireNonNull(et_search_delete.getText()).length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains("\n")) {
                    et_search.setText(s.toString().replace("\n", ""));
                    et_search.setSelection(Objects.requireNonNull(et_search.getText()).length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

//        if ()
        hideTM();

        FLAG = "first_click";
        tv_select_team_members.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Objects.equals(FLAG, "second_click")) {
                    if (Objects.requireNonNull(tv_group_name.getText()).toString().trim().isEmpty() && (tv_group_description.getText().toString().trim().isEmpty())) {
                        AndroidUtils.showAlert("Please check the Group Name , Description", getActivity());
                    } else if (tv_group_name.getText().toString().trim().isEmpty()) {
                        AndroidUtils.showAlert("Please check the Group Name", getActivity());
                    } else if (tv_group_description.getText().toString().trim().isEmpty()) {
                        AndroidUtils.showAlert("Please check the Description", getActivity());
                    } else {
                        unhideTM();
                        callMembersWebservice();
                    }
                } else {
                    FLAG = "first_click";
                    hideTM();
                }
//                }else{
//                    FLAG="first_click";
//                }
            }
        });
        try {
//            callMembersWebservice();
//            loadRecylcerview();
        } catch (
                Exception e) {
            e.fillInStackTrace();
        }

    }

    private void AddButtonsClickable() {
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Objects.requireNonNull(tv_group_name.getText()).toString().trim().isEmpty() && (Objects.requireNonNull(tv_group_description.getText()).toString().trim().isEmpty())) {
                    AndroidUtils.showAlert("Please check the Group Name , Description", getActivity());
                } else if (tv_group_name.getText().toString().trim().isEmpty()) {
                    AndroidUtils.showAlert("Please check the Group Name", getActivity());
                } else if (Objects.requireNonNull(tv_group_description.getText()).toString().trim().isEmpty()) {
                    AndroidUtils.showAlert("Please check the Description", getActivity());
                } else {
                    ArrayList<GroupModel> arraylist = new ArrayList<>();
                    callCreateGroupWebservice(tv_group_name.getText().toString().trim(), tv_group_description.getText().toString().trim(), arraylist, "");
                }
            }
        });
        btn_cancel_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_group_name.setText("");
                tv_group_description.setText("");
                assignGroupsList.clear();
                selectedTMArrayList.clear();
                mViewModel.setData(getString(R.string.view_group));
                group_head = "";
                group_name = "";
                reverse_data();
                ViewGroupsData();
            }
        });
    }

    private void CreateGroupData() {
        unhide_delete_data();
        ll_deleteGroups.setVisibility(GONE);
        tv_view_group.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
        tv_create_group.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        assignGroupsList.clear();
        chk_select_all.setChecked(false);
        cv_groups.setVisibility(VISIBLE);
        setViewModelData(getString(R.string.create_group));
        tv_switchView.setVisibility(GONE);
        tv_switchCreate.setVisibility(VISIBLE);
        ll_edit_groups.setVisibility(VISIBLE);
        btn_save.setText(R.string.save);
        btn_cancel.setText(R.string.cancel);
        btn_save.setAlpha(1.0f);
        btn_save.setEnabled(true);
        btn_update.setAlpha(1.0f);
        btn_update.setEnabled(true);
        //Changing a text color in create group module
        tv_create_group.setTextColor(getContext().getResources().getColor(R.color.white));
        tv_view_group.setTextColor(getContext().getResources().getColor(R.color.black));
        tv_add_tm.setTextColor(getContext().getResources().getColor(R.color.white));
        tv_practice_head.setTextColor(getContext().getResources().getColor(R.color.black));
//                cv_details.setVisibility(View.VISIBLE);
        ll_buttons.setVisibility(VISIBLE);
//                ll_tm.setVisibility(View.VISIBLE);
//                ll_select_all.setVisibility(View.VISIBLE);
        rv_view_groups.setVisibility(GONE);
        //Make search TextInputLayout invisible
//                search.setVisibility(View.GONE);
        tl_et_search.setVisibility(GONE);
        hideTM();
        viewGroupModelArrayList.removeAll(viewGroupModelArrayList);
        rv_view_groups.removeAllViews();
        AddButtonsClickable();
//                callMembersWebservice();
    }

    private void setViewModelData(String data) {
        mViewModel.setData(data);
    }

    private void unhideTM() {
        ll_tm.setVisibility(VISIBLE);
        cv_details.setVisibility(VISIBLE);
        ll_edit_groups.setVisibility(GONE);
        select_ghead.setVisibility(GONE);
        ll_select_all.setVisibility(VISIBLE);
        btn_update.setText(R.string.update);
    }

    private void hideTM() {
//        cv_groups.setVisibility(View.VISIBLE);
        ll_tm.setVisibility(GONE);
        et_search_tm.setText("");
        cv_details.setVisibility(GONE);
        ll_edit_groups.setVisibility(VISIBLE);
        select_ghead.setVisibility(VISIBLE);
        ll_select_all.setVisibility(GONE);
        group_head_name.setVisibility(GONE);
        group_head_name.setText("");
        btn_update.setText(R.string.save);
        updateGroupMembersList.clear();
        selectedTMArrayList.clear();
        actions_List.clear();
        assignGroupsList.clear();
        searchList.clear();
        viewGroupMembersList.clear();
        viewGroupModelArrayList.clear();
    }


    private void ViewGroupsData() {
        mViewModel.setData("View Groups");
        setViewModelData(getString(R.string.view_group));
        //Changing a text color in view group module
        if (Constants.ROLE.equals("GH")) {
            tv_view_group.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.full_green_background));
        } else {
            tv_view_group.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_background));
        }
        tv_view_group.setTextColor(getContext().getResources().getColor(R.color.white));
        tv_switchView.setVisibility(VISIBLE);
        tv_switchCreate.setVisibility(GONE);
        tv_create_group.setTextColor(getContext().getResources().getColor(R.color.black));
        tv_create_group.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_group_name.setText("");
        tv_group_description.setText("");
        cv_groups.setVisibility(GONE);
//        ll_buttons.setVisibility(View.GONE);
        ll_tm.setVisibility(GONE);
        tv_label.setVisibility(GONE);
        select_ghead.setVisibility(GONE);
        ll_select_all.setVisibility(GONE);
        selectedTMArrayList.removeAll(selectedTMArrayList);
        rv_select_team_members.removeAllViews();
        cv_details.setVisibility(GONE);
        rv_view_groups.setVisibility(VISIBLE);
        //Make search TextInputLayout Visible
//        search.setVisibility(View.VISIBLE);
        tl_et_search.setVisibility(VISIBLE);
        et_search.setText("");
        et_search.setHint(R.string.search_groups);
        callViewGroupsWebservice();
    }

    private void callMembersWebservice() {
        try {
            JSONObject postdata = new JSONObject();
            progress_dialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/member/groups", "Get Members", postdata.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    private void callViewGroupsWebservice() {

        try {
            JSONObject postdata = new JSONObject();
            progress_dialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/groups", "Get Groups", postdata.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    private void loadViewGroupsRecylerview(String TAG_TYPE, ArrayList<ViewGroupModel> viewGroupModelArrayList, Button button) {
        if (button != null) {
            button.setAlpha(0.5f);
            button.setEnabled(false);
        }
        if (Objects.equals(TAG_TYPE, "VG")) {

            adapter_view_groups = new ViewGroupsAdpater(viewGroupModelArrayList, getContext(), this, TAG_TYPE, new_itemClickListener, Groups.this);
            adapter_view_groups.setRecyclerView(rv_view_groups);
            rv_view_groups.setAdapter(adapter_view_groups);
            AndroidUtils.LoadingRecyclerview(rv_view_groups, getContext());
            AndroidUtils.setupBottomSpacerFooter(rv_view_groups, getResources().getDimensionPixelSize(R.dimen.twentyeight_dp));
//            rv_view_groups.setLayoutAnimation(
//                    AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fall_down)
//            );
//            rv_view_groups.scheduleLayoutAnimation();

            //rv_view_groups.setHasFixedSize(true);
        } else {
            try {
                rv_select_team_members.setLayoutManager(new GridLayoutManager(getContext(), 1));
                if (Objects.equals(TAG_TYPE, "DG")) {
//                    et_search_delete.setVisibility(View.VISIBLE);
                    tv_practice_head.setVisibility(GONE);
                    adapter_view_groups = new ViewGroupsAdpater(viewGroupModelArrayList, getContext(), this, TAG_TYPE, new_itemClickListener, Groups.this, button);
                    Log.i("TAG", "List" + TAG_TYPE);
                    rv_select_team_members.setAdapter(adapter_view_groups);
                    //rv_select_team_members.setHasFixedSize(true);
                } else {
                    tv_practice_head.setVisibility(VISIBLE);
                    adapter_view_groups = new ViewGroupsAdpater(viewGroupMembersList, getContext(), this, TAG_TYPE, new_itemClickListener, Groups.this, button);
                    rv_select_team_members.setAdapter(adapter_view_groups);
                    //rv_select_team_members.setHasFixedSize(true);
                    if (viewGroupMembersList.isEmpty()) {
                        rv_select_team_members.setVisibility(GONE);
                    } else {
                        rv_select_team_members.setVisibility(VISIBLE);
                    }
                }
//            ViewGroupsAdpater finalAdapter_view_groups = adapter_view_groups;
            } catch (Exception e) {
                Log.e("Tag", "Error" + e.getMessage());
                e.fillInStackTrace();
            }
        }
        new_itemClickListener = new ViewGroupsItemClickListener() {
            @Override
            public void onClick(String s) {
                rv_view_groups.post(new Runnable() {
                    @Override
                    public void run() {
//                        adapter_view_groups.notifyDataSetChanged();
                        group_head = s;
//                        AndroidUtils.showAlert("Selected:"+s, getContext());
                    }
                });
            }
        };
        et_search_tm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                adapter_view_groups.getFilter().filter(Objects.requireNonNull(et_search_tm.getText()).toString());
//                rv_select_team_members.notifyAll();
            }
        });
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter_view_groups.getFilter().filter(et_search.getText().toString());
            }
        });
    }

    private void loadRecylcerview(ArrayList<GroupModel> selectedTMArrayList, String tmType) {
        tv_create_group.setTextColor(requireContext().getResources().getColor(R.color.white));
        tv_view_group.setTextColor(requireContext().getResources().getColor(R.color.black));
        FLAG = "second_click";
        if (Objects.equals(tmType, "TM")) {
            select_ghead.setVisibility(GONE);
            ll_select_all.setVisibility(VISIBLE);
//            When clicking cancel button
            //Changing a text color for the Add-Group head and Add-Team member module..
            tv_add_tm.setTextColor(requireContext().getResources().getColor(R.color.white));
            tv_practice_head.setTextColor(requireContext().getResources().getColor(R.color.black));
            tv_practice_head.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
            tv_add_tm.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
            rv_select_team_members.removeAllViews();
            rv_select_team_members.setLayoutManager(new GridLayoutManager(getContext(), 1));
            adapter = new GroupAdapters(selectedTMArrayList, tmType, itemClickListener, Groups.this);
            rv_select_team_members.setAdapter(adapter);
            //rv_select_team_members.setHasFixedSize(true);
        } else {
            ll_select_all.setVisibility(GONE);
            select_ghead.setVisibility(VISIBLE);
            tv_add_tm.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
            tv_practice_head.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_background));
//            When Clicking Save button
            //Changing a text color for the Add-group head and Add-team member module..
            tv_add_tm.setTextColor(getContext().getResources().getColor(R.color.black));
            tv_practice_head.setTextColor(getContext().getResources().getColor(R.color.white));
            rv_select_team_members.removeAllViews();
            rv_select_team_members.setLayoutManager(new GridLayoutManager(getContext(), 1));
            adapter = new GroupAdapters(selectedTMArrayList, tmType, itemClickListener, Groups.this);
            rv_select_team_members.setAdapter(adapter);
            // rv_select_team_members.setHasFixedSize(true);
        }


//        rv_select_team_members.notify();
        et_search_tm.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(Objects.requireNonNull(et_search_tm.getText()).toString());
            }
        });
        itemClickListener = new ItemClickListener() {
            @Override
            public void onClick(String s) {
                rv_select_team_members.post(new Runnable() {
                    @Override
                    public void run() {
//                        adapter.notifyDataSetChanged();
                        group_head = s;
//                        AndroidUtils.showAlert("Selected:"+s, getContext());
                    }
                });
            }
        };
        chk_select_all.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.selectOrDeselectAll(chk_select_all.isChecked());
            }
        });
        btn_save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (Objects.equals(tmType, "TM")) {
//                        ll_select_all.setVisibility(View.GONE);
                        et_search_tm.setText("");
                        group_head = "";
                        group_name = "";
                        assignGroupHead(TMArrayList);
                    } else {
                        //..
//                        String grp_name = Objects.requireNonNull(tv_group_name.getText()).toString().trim();
//                        for (int i = 0; i < viewGroupNameArrayList.size(); i++) {
//                            ViewGroupModel viewGroupModel = viewGroupNameArrayList.get(i);
//                            if (viewGroupModel.getName().equals(grp_name)) {
//                                is_group_name_new = false;
//                            }
//                        }
//                        if (!is_group_name_new) {
//                            AndroidUtils.showAlert("Duplicate group name!", getActivity());
//                        } else
                        if (Objects.requireNonNull(tv_group_name.getText()).toString().trim().isEmpty() && (Objects.requireNonNull(tv_group_description.getText()).toString().trim().isEmpty())) {
                            AndroidUtils.showAlert("Please check the Group Name , Description", getActivity());
                        } else if (tv_group_name.getText().toString().trim().isEmpty()) {
                            AndroidUtils.showAlert("Please check the Group Name", getActivity());
                        } else if (Objects.requireNonNull(tv_group_description.getText()).toString().trim().isEmpty()) {
                            AndroidUtils.showAlert("Please check the Description", getActivity());
                        } else if (Objects.equals(group_head, "")) {
                            AndroidUtils.showAlert("Please select a group head", getActivity());
                        } else {
                            ll_tm.setVisibility(GONE);
                            ll_select_all.setVisibility(GONE);
                            select_ghead.setVisibility(GONE);
                            tl_et_search.setVisibility(GONE);
                            callCreateGroupWebservice(tv_group_name.getText().toString().trim(), tv_group_description.getText().toString().trim(), assignGroupsList, group_head);
                        }
                        //..
                    }
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            }
        });
        btn_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //Changing a text color for the Add-Group head and Add-Team member module..
                tv_add_tm.setTextColor(requireContext().getResources().getColor(R.color.white));
                tv_practice_head.setTextColor(requireContext().getResources().getColor(R.color.black));
                tv_practice_head.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
                tv_add_tm.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
                tv_group_name.setText("");
                tv_group_description.setText("");
//                mViewModel.setData(getString(R.string.view_group));
                group_head = "";
                group_name = "";
                FLAG = "first_click";
                hideTM();
//                reverse_data();
//                ViewGroupsData();
                chk_select_all.setChecked(false);
                select_ghead.setVisibility(GONE);
//                ll_select_all.setVisibility(View.VISIBLE);
//                if (selectedTMArrayList.isEmpty()) {
//                    AndroidUtils.showAlert("Please check the member selection", getContext());
//                } else {
////                    adapter.selectOrDeselectAll(false);
////                    adapter.getList_item().clear();
////                    selectedTMArrayList.clear();
////                    rv_select_team_members.removeAllViews();
////                    callMembersWebservice();
//                }
//                selectedTMArrayList.clear();
//                adapter.getList_item().clear();
            }
        });
    }

    private void callCreateGroupWebservice(String tv_group_name, String tv_group_description, ArrayList<GroupModel> list_item, String group_head) {
        try {
            JSONObject postData = new JSONObject();
            JSONArray members = new JSONArray();
            for (int i = 0; i < list_item.size(); i++) {
                GroupModel model = list_item.get(i);
                if (model.isChecked()) {
                    members.put(model.getId());
                }
            }
            if (members.length() != 0 && !Objects.equals(group_head, "")) {
                postData.put("name", tv_group_name);
                postData.put("description", tv_group_description);
                postData.put("groupHead", group_head);
                postData.put("members", members);
//                AndroidUtils.showAlert("Selected:"+members+" "+"GH"+group_head,getContext());
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v3/group", "Create Groups", postData.toString());
            } else if (members.length() != 0 && Objects.equals(group_head, "")) {
                AndroidUtils.showAlert("Please select a group head", getActivity());
            } else {
                postData.put("name", tv_group_name);
                postData.put("description", tv_group_description);
                postData.put("groupHead", group_head);
                postData.put("members", members);
//                AndroidUtils.showAlert("Selected:"+members+" "+"GH"+group_head,getContext());
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v3/group", "Create Groups", postData.toString());
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    public void selectedtmlist(ArrayList<GroupModel> list_item) {
        if (btn_save_selected_tm != null) {
            // Initially disable the button
            btn_save_selected_tm.setEnabled(false);
            btn_save_selected_tm.setAlpha(0.5f);

            // Check for differences between the lists
            boolean isDifferent = false;

            // Create a list of IDs for comparison
            List<String> updateGroupIds = new ArrayList<>();
            for (ViewGroupModel updateGroupModel : updateGroupMembersList) {
                updateGroupIds.add(updateGroupModel.getGroup_id());
            }

            // Loop through the `list_item` and check their checked status
            for (GroupModel groupModel : list_item) {
                if (groupModel.isChecked()) {
                    // If the selected item's ID is not in `updateGroupIds`, mark as different
                    if (!updateGroupIds.contains(groupModel.getId())) {
                        isDifferent = true;
                        break;
                    }
                }
            }

            // Also check for removed items (unchecked items in list_item that were previously in updateGroupMembersList)
            for (ViewGroupModel updateGroupModel : updateGroupMembersList) {
                boolean found = false;
                for (GroupModel groupModel : list_item) {
                    if (groupModel.isChecked() && groupModel.getId().equals(updateGroupModel.getGroup_id())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    isDifferent = true;
                    break;
                }
            }

            // Enable the button if there is any difference
            if (isDifferent) {
                btn_save_selected_tm.setEnabled(true);
                btn_save_selected_tm.setAlpha(1.0f);
            }
        }
    }

    private void assignGroupHead(ArrayList<GroupModel> list_item) {
//        selectedTMArrayList.clear();
        assignGroupsList.clear();
        for (int i = 0; i < list_item.size(); i++) {
            GroupModel groupModel = list_item.get(i);
            if (groupModel.isChecked()) {
                assignGroupsList.add(groupModel);
            }
//            if (groupModel.getId().equals(group_head)) {
//                assignGroupsList.get(i).setChecked(false);
//                break;
//            }
        }
        TM_TYPE = "GH";
        if (!assignGroupsList.isEmpty()) {
            if (Objects.requireNonNull(tv_group_name.getText()).toString().trim().isEmpty() && (tv_group_description.getText().toString().trim().isEmpty())) {
                AndroidUtils.showAlert("Please check the Group Name , Description", getActivity());
            } else if (tv_group_name.getText().toString().trim().isEmpty()) {
                AndroidUtils.showAlert("Please check the Group Name", getActivity());
            } else if (Objects.requireNonNull(tv_group_description.getText()).toString().trim().isEmpty()) {
                AndroidUtils.showAlert("Please check the Description", getActivity());
            } else {
                loadRecylcerview(assignGroupsList, TM_TYPE);
            }
        } else {
            AndroidUtils.showAlert("Please select atleast one team member", getActivity());
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
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                switch (httpResult.getRequestType()) {
                    case "Get Groups": {
                        JSONArray data = result.getJSONArray("data");
                        loadViewGroups(data);
                        break;
                    }
                    case "Get Members": {
                        JSONObject data = result.getJSONObject("data");
                        JSONArray users = data.getJSONArray("users");
                        loadMembers(users);
                        break;
                    }
                    case "Get Team Members": {
                        JSONObject data = result.getJSONObject("data");
                        JSONArray users = data.getJSONArray("users");
                        loadTeamMembers(users);
                        break;
                    }
                    case "Create Groups":
//                    JSONObject jsonObject = result.getJSONObject("msg");
                        if (result.has("errors")) {
                            JSONArray jsonArray = result.getJSONArray("errors");
                            JSONObject iserror = jsonArray.getJSONObject(0);
                            String FieldName = iserror.getString("field");
                            String msg = iserror.getString("msg");
                            AndroidUtils.showAlert(msg, getActivity());
                        } else {
                            chk_select_all.setChecked(false);
                            group_head = "";
                            group_name = "";
                            ViewGroupsData();
                            mViewModel.setData("View Groups");
                            AndroidUtils.showAlert(result.getString("msg"), getActivity(), "");
                        }
                        break;
                    case "Update Groups":
                        if (httpResult.getStatus_code() == 200) {
                            AndroidUtils.showAlert(result.getString("msg"), getActivity(), "");
                            unhideData();
                            ViewGroupsData();
                            clear_list();
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), getActivity(), "");
                        }
                        break;
                    case "Delete Groups":
                        ViewGroupsData();
                        AndroidUtils.showAlert(result.getString("msg"), getActivity(), "");
                        viewGroupModelArrayList.clear();
                        viewGroupMembersList.clear();
                        selectedTMArrayList.clear();
                        adapter.getList_item().clear();
                        updateGroupMembersList.clear();
                        break;
                    case "Group Counts":
                        if (httpResult.getStatus_code() == 200) {
                            JSONObject counts = result.optJSONObject("counts");
                            assert counts != null;
                            tv_documents.setText(counts.optInt("documents") + " Documents");
                            tv_matters.setText(counts.optInt("matters") + " Matters");
                            tv_relationships.setText(counts.optInt("relationships") + " Relationships");
                            tv_members.setText(counts.optInt("members") + " Members");
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), getActivity(), "");
                        }
                        break;
                    case "Update Group Head":
                        if (httpResult.getStatus_code() == 200) {
                            AndroidUtils.showAlert(result.getString("msg"), getActivity(), "");
                            mViewModel.setData("View Groups");
                            unhideData();
                            viewGroupMembersList.clear();
                            viewGroupModelArrayList.clear();
//                    et_search.setText("");
                            ViewGroupsData();
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), getActivity(), "");
                        }
                        break;
                    case "Search Results": {
                        JSONArray data = result.getJSONArray("data");
                        loadSearchResults(data);
                        break;
                    }
                }
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        } else if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Failed) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
//                if (result.optBoolean("error")) {
                AndroidUtils.showErrorAlert(result.optString("msg"), getActivity());
//                }
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        } else {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            AndroidUtils.showErrorAlert(httpResult.getResponseContent().toString(), getActivity());
        }
    }


    private void loadTeamMembers(JSONArray users) throws JSONException {
        GroupModel groupModel;
        String ghName = new_viewGroupModel.getGroup_head_name();
        selectedTMArrayList.clear();
        for (int i = 0; i < users.length(); i++) {
            JSONObject jsonObject = users.getJSONObject(i);
            groupModel = new GroupModel();
            groupModel.setId(jsonObject.getString("id"));
            groupModel.setName(jsonObject.getString("name"));
            for (int a = 0; a < users.length(); a++) {
                for (int m = 0; m < updateGroupMembersList.size(); m++) {
                    groupModel.setIsenabled(!groupModel.getId().matches(new_viewGroupModel.getGroup_head_id()));
                }
            }
            for (int j = 0; j < users.length(); j++) {
                for (int k = 0; k < updateGroupMembersList.size(); k++) {
                    if (groupModel.getId().matches(updateGroupMembersList.get(k).getGroup_id())) {
                        groupModel.setChecked(true);
                    }
                }
            }
//            selectedTMArrayList.add(groupModel);
            if (!groupModel.getName().equals(ghName)) {
                selectedTMArrayList.add(groupModel);
            }
        }
        if (!selectedTMArrayList.isEmpty()) {
            //check if the group head is selected or not..
            if (!new_viewGroupModel.getGroup_head_name().isEmpty()) {
                group_head_name.setVisibility(VISIBLE);
                group_head_name.setText(new_viewGroupModel.getGroup_head_name() + " - " + "Group Head");
            } else {
                group_head_name.setVisibility(GONE);
                group_head_name.setText("");
            }
        } else {
            group_head_name.setVisibility(GONE);
            group_head_name.setText("");
        }
        TM_TYPE = "TM";
        loadTeamRecyclerview(selectedTMArrayList, TM_TYPE);
    }

    private void loadTeamRecyclerview(ArrayList<GroupModel> selectedTMArrayList, String tmType) {
        btn_save_selected_tm = v.findViewById(R.id.btn_save);
        if (btn_save_selected_tm != null) {
            btn_save_selected_tm.setAlpha(0.5f);
            btn_save_selected_tm.setEnabled(false);
        }
        rv_select_team_members.removeAllViews();
        rv_select_team_members.setLayoutManager(new GridLayoutManager(getContext(), 1));
        adapter = new GroupAdapters(selectedTMArrayList, tmType, itemClickListener, Groups.this, btn_save_selected_tm);
        rv_select_team_members.setAdapter(adapter);
        //rv_select_team_members.setHasFixedSize(true);
//        rv_select_team_members.notify();
        et_search_tm.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(et_search_tm.getText().toString());
            }
        });
        chk_select_all.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chk_select_all.isChecked()) {
                    adapter.selectOrDeselectAll(true);
                    btn_save_selected_tm.setAlpha(1.0f);
                    btn_save_selected_tm.setEnabled(true);
                } else {
                    adapter.selectOrDeselectAll(false);
                    btn_save_selected_tm.setAlpha(0.5f);
                    btn_save_selected_tm.setEnabled(false);
                }
            }
        });
        AppCompatButton btn_cancel_selected_tm = (AppCompatButton) v.findViewById(R.id.btn_cancel);
        btn_cancel_selected_tm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                unhideData();
                select_ghead.setVisibility(GONE);
                ll_select_all.setVisibility(GONE);
                chk_select_all.setChecked(false);
                et_search_tm.setText("");
                ViewGroupsData();
                clear_list();
            }
        });

        btn_save_selected_tm.setAlpha(0.5f);
        btn_save_selected_tm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String update_type = "UGM";
                try {
//                    btn_save_selected_tm.setAlpha(1.0f);
                    callUpdateGroups("", "", new_viewGroupModel.getId(), update_type, selectedTMArrayList);
                } catch (JSONException e) {
                    e.fillInStackTrace();
                }
//                callUpdateUGMWebservice(adapter.getList_item(););
            }
        });
    }

    private void loadMembers(JSONArray users) throws JSONException {
        GroupModel groupModel;
        selectedTMArrayList.clear();
        TMArrayList.clear();
        for (int i = 0; i < users.length(); i++) {
            JSONObject jsonObject = users.getJSONObject(i);
            groupModel = new GroupModel();
            groupModel.setId(jsonObject.getString("id"));
            groupModel.setName(jsonObject.getString("name"));
            groupModel.setIsenabled(true);
            TMArrayList.add(groupModel);
//            selectedTMArrayList.add(groupModel);
        }
        TM_TYPE = "TM";
        loadRecylcerview(TMArrayList, TM_TYPE);
    }

    private void loadViewGroups(JSONArray data) throws JSONException {
        viewGroupModelArrayList.clear();
        viewGroupNameArrayList.clear();
        // First, parse everything normally
        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            ViewGroupModel viewGroupModel = new ViewGroupModel();
            viewGroupModel.setId(jsonObject.optString("id"));
            String date = jsonObject.optString("created");
            viewGroupModel.setIsdisabled(jsonObject.optBoolean("isdisabled"));
            if (!date.isEmpty()) {
                Date date_new = AndroidUtils.stringToDateTimeDefault(date, "MMM dd, yyyy, hh:mm a");
                String created = AndroidUtils.getDateToString(date_new, "MMM dd, yyyy | hh:mm a");
                viewGroupModel.setCreated(created);
            } else {
                viewGroupModel.setCreated("");
            }
            JSONArray members = jsonObject.optJSONArray("members");
            viewGroupModel.setMembers(members);
            viewGroupModel.setMemberCount(jsonObject.optString("memberCount"));
            viewGroupModel.setDescription(jsonObject.optString("description"));
            viewGroupModel.setName(jsonObject.optString("name"));

            JSONObject group_head = jsonObject.optJSONObject("groupHead");
            if (group_head != null) {
                viewGroupModel.setGroup_head_id(group_head.optString("id"));
                viewGroupModel.setGroup_head_name(group_head.optString("name"));
                viewGroupModel.setOwner_name(group_head.optString("name"));
            }
            viewGroupModelArrayList.add(viewGroupModel);
        }

        // ✅ Reorder list: AAM → SuperUser → others
        viewGroupModelArrayList.sort((a, b) -> {
            String nameA = a.getName() != null ? a.getName() : "";
            String nameB = b.getName() != null ? b.getName() : "";

            if (nameA.equalsIgnoreCase("AAM")) return -1;
            if (nameB.equalsIgnoreCase("AAM")) return 1;

            if (nameA.equalsIgnoreCase("SuperUser")) return -1;
            if (nameB.equalsIgnoreCase("SuperUser")) return 1;

            return 0; // keep original order otherwise
        });

        // Mirror into name array list
        viewGroupNameArrayList.addAll(viewGroupModelArrayList);

        Log.i("ArrayList", "info " + viewGroupModelArrayList.toString());
        loadViewGroupsRecylerview("VG", viewGroupModelArrayList, null);
    }


    public void EditGroup(ViewGroupModel viewGroupModel) {

        btn_update.setText(R.string.update);
        hideData();
        tv_group_name.setText(viewGroupModel.getName());
        tv_group_description.setText(viewGroupModel.getDescription());
        ll_deleteGroups.setVisibility(GONE);
        ll_edit_groups.setVisibility(VISIBLE);
        btn_cancel_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewModel.setData(getString(R.string.view_group));
                unhideData();
                ViewGroupsData();
            }
        });
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Objects.requireNonNull(tv_group_name.getText()).toString().trim().isEmpty() && (tv_group_description.getText().toString().trim().isEmpty())) {
                    AndroidUtils.showAlert("Please check the Group Name , Description", getActivity());
                } else if (tv_group_name.getText().toString().trim().isEmpty()) {
                    AndroidUtils.showAlert("Please check the Group Name", getActivity());
                } else if (Objects.requireNonNull(tv_group_description.getText()).toString().trim().isEmpty()) {
                    AndroidUtils.showAlert("Please check the Description", getActivity());
                } else {
                    try {//Only change the view when User is able to edit the Group.
                        String update_type = "EG";
                        ArrayList<GroupModel> new_list = new ArrayList<>();
                        callUpdateGroups(tv_group_name.getText().toString().trim(), tv_group_description.getText().toString().trim(), viewGroupModel.getId(), update_type, new_list);
                    } catch (JSONException ex) {
                        ex.fillInStackTrace();
                    }
                }
            }
        });

    }

    @Override
    public void DeleteGroup(ViewGroupModel viewGroupModel, ArrayList<ViewGroupModel> itemsArrayList) {
        //Hide the views when user navigate to Delete Group Page and Reassign the values.
        hideData();
        hidedelete_data();
//        ll_edit_groups.setVisibility(View.VISIBLE);
        group_head_name.setVisibility(GONE);
        group_head = "";
        group_name="";
        isDelete = true;
        AndroidUtils.DisplaySpinnerView(sp_groupslist, tv_delete_groups, group_head, dropdown_icon, clear_icon, false, GroupsAdapter, "Search Groups");
        et_search.setText("");
        et_search_tm.setText("");
        et_search_delete.setText("");
//        cv_details.setVisibility(View.VISIBLE);
        ll_deleteGroups.setVisibility(VISIBLE);//        delete_spinner_layout.setVisibility(View.VISIBLE);
        for (int i = 0; i < itemsArrayList.size(); i++) {
            if (viewGroupModel.getName().matches(itemsArrayList.get(i).getName())) {
                itemsArrayList.remove(i);
                break;
            }
        }
        ll_Delete_view.setVisibility(VISIBLE);
        callGroupsCounts(viewGroupModel.getId());
        group_name_delete.setText(viewGroupModel.getName());
//        btn_update.setVisibility(View.GONE);
//        btn_cancel_edit.setVisibility(View.GONE);
        tv_group_name.setText(viewGroupModel.getName());
        tv_group_name.setEnabled(false);
        tv_group_description.setText(viewGroupModel.getDescription());
        tv_group_description.setEnabled(false);
        AppCompatButton btn_delete = v.findViewById(R.id.btn_update);
        AppCompatButton btn_cancel = v.findViewById(R.id.btn_cancel_edit);
        btn_delete.setText(requireContext().getResources().getString(R.string.delete));
        btn_delete.setEnabled(false);
        btn_delete.setAlpha(0.4F);
        Log.i("TAG", "INFO" + itemsArrayList);
//        String mtag = "DG";
//        loadViewGroupsRecylerview(mtag, itemsArrayList, btn_delete);
        // Extract only names from itemsArrayList
        ArrayList<String> nameList = new ArrayList<>();
        for (ViewGroupModel item : itemsArrayList) {
            nameList.add(item.getName());
        }

// Set up the adapter with only names
        GroupsAdapter = new CommonSpinnerAdapter(getActivity(), nameList);
        sp_groupslist.setAdapter(GroupsAdapter);

        tv_sp_groups.setOnClickListener(v -> {
            boolean isVisible = sp_groupslist.getVisibility() == VISIBLE;
            AndroidUtils.DisplaySpinnerView(sp_groupslist, tv_delete_groups, group_name, dropdown_icon, clear_icon, !isVisible, GroupsAdapter, "Search Groups");
            AndroidUtils.display_listview(isDelete, sp_groupslist);
            isDelete = !isDelete;
        });

        sp_groupslist.setOnItemClickListener((parent, view, position, id) -> {
            group_head = itemsArrayList.get(position).getId();
            group_name = GroupsAdapter.getItem(position).toString();// Use nameList instead of itemsArrayList
            AndroidUtils.DisplaySpinnerView(sp_groupslist, tv_delete_groups, group_name, dropdown_icon, clear_icon, false, GroupsAdapter, "Search Groups");
            btn_delete.setAlpha(1.0F);
            btn_delete.setEnabled(true);
            isDelete = true;
        });

        clear_icon.setOnClickListener(v -> {
            AndroidUtils.DisplaySpinnerView(sp_groupslist, tv_delete_groups, group_head, dropdown_icon, clear_icon, false, GroupsAdapter, "Search Groups");
            btn_delete.setAlpha(0.4F);
            btn_delete.setEnabled(false);
            isDelete = true;
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewModel.setData(getString(R.string.view_group));
                group_head = "";
                group_name="";
                reverse_data();
                ViewGroupsData();
                loadViewGroupsRecylerview("VG", itemsArrayList, null);
            }
        });
//        if (group_head.isEmpty()) {//Check if the user is assigned a Current group to any other Group from a List of Group.
//            AndroidUtils.showAlert("Please Assign a Group", getContext());
//            btn_delete.setAlpha(0.4F);
//            btn_delete.setEnabled(false);
//        } else {
//            btn_delete.setAlpha(0.4F);
//            btn_delete.setEnabled(false);
//        }
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (group_head.isEmpty())
                    AndroidUtils.showAlert("Please select atleast one Group", getActivity());
                else
                    Delete_Popup(viewGroupModel, itemsArrayList);
//                cv_details.setVisibility(View.GONE);
            }
        });
    }

    private void Delete_Popup(ViewGroupModel viewGroupModel, ArrayList<ViewGroupModel> itemsArrayList) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.delete_relationship, null);
            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
            TextView header_name = view.findViewById(R.id.header_name);
            header_name.setText(R.string.confirmation);
            header_name.setTextSize(DynamicUtils.twenty);
            header_name.setTypeface(Typeface.DEFAULT_BOLD);
            header_name.setTextColor(Color.BLACK);
            ImageView close_documents = view.findViewById(R.id.close_documents);
//            close_documents.setVisibility(GONE);
            String ConfirmDelText = getString(R.string.delete_group) + viewGroupModel.getName() + "?";

            tv_confirmation.setText(ConfirmDelText);

            AppCompatButton bt_yes = view.findViewById(R.id.btn_yes);
            AppCompatButton btn_no = view.findViewById(R.id.btn_No);
            final AlertDialog dialog = dialogBuilder.create();

            btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    ViewGroupsData();
                }
            });
            close_documents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            bt_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    //Only change the view when User is able to Delete the Group.
                    unhideData();
                    reverse_data();
                    ViewGroupsData();
                    try {
                        callDeleteGroups(viewGroupModel.getId());
                        loadViewGroupsRecylerview("VG", itemsArrayList, null);
                    } catch (JSONException ex) {
                        ex.fillInStackTrace();
                    }
                }
            });
            dialog.setView(view);
            dialog.show();
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    @Override
    public void CGH(final ViewGroupModel viewGroupModel, ArrayList<ViewGroupModel> itemsArrayList) {
        AppCompatButton btn_save_CGH = v.findViewById(R.id.btn_save);
//        hideData();
        unhide_delete_data();
        btn_save.setText(R.string.update);
        hide_CGH_UGM_data();
        et_search_tm.setVisibility(VISIBLE);
        try {
            viewGroupMembersList.clear();
            group_head_name.setText(viewGroupModel.getGroup_head_name() + " - " + "Group Head");
            //Displaying if the group head selected before
            Log.i("Tag", "Info: " + viewGroupModel.getGroup_head_name());
            if (!viewGroupModel.getGroup_head_name().isEmpty())
                group_head_name.setVisibility(VISIBLE);
            else {
                group_head_name.setVisibility(GONE);
            }
            tv_group_name.setText(viewGroupModel.getName());
            String ghname = viewGroupModel.getGroup_head_name();
            tv_group_description.setText(viewGroupModel.getDescription());
            String mtag = "CGH";

            for (int i = 0; i < viewGroupModel.getMembers().length(); i++) {
                ViewGroupModel viewGroupModel_1 = new ViewGroupModel();
                JSONObject jsonObject = viewGroupModel.getMembers().getJSONObject(i);
                viewGroupModel_1.setGroup_name(jsonObject.getString("name"));
                viewGroupModel_1.setGroup_id(jsonObject.getString("id"));
                if (!viewGroupModel_1.getGroup_name().equals(ghname)) {
                    viewGroupMembersList.add(viewGroupModel_1);
                }
//                viewGroupMembersList.add(viewGroupModel_1);
            }
//            AndroidUtils.showAlert("" + viewGroupMembersList.size(), getContext());
            loadViewGroupsRecylerview(mtag, viewGroupModelArrayList, btn_save_CGH);
            AppCompatButton btn_cancel_CGH = v.findViewById(R.id.btn_cancel);
            btn_cancel_CGH.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mViewModel.setData("View Groups");
                    unhideData();
                    viewGroupMembersList.clear();
                    viewGroupModelArrayList.clear();
//                    et_search.setText("");
                    ViewGroupsData();
                }
            });

            btn_save_CGH.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mViewModel.setData("View Groups");
                    if (group_head.isEmpty()) {
                        AndroidUtils.showAlert("Please select a Group Head", getActivity());
                    } else {
                        try {
//                            unhideData();
                            callUpdateGroupHeadWebservice(viewGroupModel.getId(), group_head);
                        } catch (JSONException ex) {
                            ex.fillInStackTrace();
                        }
                    }
//                    ViewGroupsData();
                }
            });

        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    public void UGM(ViewGroupModel viewGroupModel) throws JSONException {
        new_viewGroupModel = viewGroupModel;
        et_search.setText("");
        et_search_tm.setText("");
//        Log.e("Message for UGM ",":");
        unhide_delete_data();
        btn_save.setText(R.string.update);
        hide_CGH_UGM_data();
        ll_select_all.setVisibility(VISIBLE);
        tv_label.setVisibility(VISIBLE);
        select_ghead.setVisibility(GONE);
        for (int i = 0; i < viewGroupModel.getMembers().length(); i++) {
            ViewGroupModel viewGroupModel_1 = new ViewGroupModel();
            JSONObject jsonObject = viewGroupModel.getMembers().getJSONObject(i);
            viewGroupModel_1.setGroup_name(jsonObject.getString("name"));
            viewGroupModel_1.setGroup_id(jsonObject.getString("id"));
            updateGroupMembersList.add(viewGroupModel_1);
        }
        callViewGroupMembersWebservice();
//        callMembersWebservice();
    }

    //    @Override
    public void GAL(ViewGroupModel viewGroupModel) throws JSONException {
        // Create dialog
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.group_activity_log); // replace with your actual XML name
        dialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.color.pearl));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        // Find your views
        CardView cvDetailsActivityLog = dialog.findViewById(R.id.cv_details_activity_log);
        Spinner spCategory = dialog.findViewById(R.id.sp_category);
        TextView category_id, client_id_name, team_member_id, search_name, from_id, to_id;
        category_id = dialog.findViewById(R.id.category_id);
        client_id_name = dialog.findViewById(R.id.client_id_name);
        team_member_id = dialog.findViewById(R.id.team_member_id);
        category_id.setText(R.string.category);
        client_id_name.setText(R.string.client);
        team_member_id.setText(R.string.team_member);
        search_name = dialog.findViewById(R.id.search_name);
        search_name.setText(R.string.search);
        TextInputEditText et_search_name = dialog.findViewById(R.id.et_search_name);
        TextInputEditText et_client_name = dialog.findViewById(R.id.et_client_name);
        TextInputEditText et_team_member = dialog.findViewById(R.id.et_team_member);
        Button tvFromDate = dialog.findViewById(R.id.btn_from_date);
        Button tvToDate = dialog.findViewById(R.id.btn_to_date);
        rv_activity_log = dialog.findViewById(R.id.rv_view_activity_log);
        ImageView cancelIcon = dialog.findViewById(R.id.cancelIcon);
        from_id = dialog.findViewById(R.id.from_id);
        to_id = dialog.findViewById(R.id.to_id);
        from_id.setText(R.string.from);
        to_id.setText(R.string.to);
        Button btnSearch = dialog.findViewById(R.id.btn_update_activity_log);
//        btnSearch.setBackground(getActivity().getDrawable(R.drawable.rectangular_complete_blue_background));
        btnSearch.setText(R.string.search);
        View tl_search_appointments = dialog.findViewById(R.id.tl_et_search_message);
        et_search_message = tl_search_appointments.findViewById(R.id.et_Search);

        et_search_message.setHint(R.string.search);
        et_search_name.setHint(R.string.search);

        et_client_name.setHint(R.string.client);
        et_team_member.setHint(R.string.team_member);
        tvFromDate.setHint(R.string.from);
        tvToDate.setHint(R.string.to);

        // Initialize your lists
        ArrayList<ActionModel> actionsList = new ArrayList<>();
        ArrayList<ViewGroupModel> viewGroupMembersList = new ArrayList<>();

        // Set up your adapters
        actionsList.add(new ActionModel("Authorization"));
        actionsList.add(new ActionModel("Groups"));
        actionsList.add(new ActionModel("Team Members"));
        actionsList.add(new ActionModel("Relationships"));
        actionsList.add(new ActionModel("Share"));
        actionsList.add(new ActionModel("Documents"));
        actionsList.add(new ActionModel("Merge PDF"));
        actionsList.add(new ActionModel("Matters"));
        actionsList.add(new ActionModel("Timesheets"));


        // Handle spinner selections
        final String[] selectedCategory = {""};
        selectedCategory[0] = actionsList.get(1).getName();
        CommonSpinnerAdapter spinnerAdapter = new CommonSpinnerAdapter((Activity) getContext(), actionsList);
        spCategory.setAdapter(spinnerAdapter);
        int defaultPosition = 0;
        for (int i = 0; i < actionsList.size(); i++) {
            if (actionsList.get(i).getName().equalsIgnoreCase("Groups")) {
                defaultPosition = i;
                break;
            }
        }
        spCategory.setSelection(defaultPosition);
        selectedCategory[0] = actionsList.get(defaultPosition).getName();

        // Populate team member spinner from JSON
        try {
            for (int i = 0; i < viewGroupModel.getMembers().length(); i++) {
                JSONObject jsonObject = viewGroupModel.getMembers().getJSONObject(i);
                ViewGroupModel member = new ViewGroupModel();
                member.setGroup_name(jsonObject.getString("name"));
                member.setGroup_id(jsonObject.getString("id"));
                viewGroupMembersList.add(member);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        CommonSpinnerAdapter teamAdapter = new CommonSpinnerAdapter((Activity) getContext(), viewGroupMembersList);
//        spTeamMember.setAdapter(teamAdapter);
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCategory[0] = actionsList.get(i).getName().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
//
//        spTeamMember.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                selectedTeamMember[0] = viewGroupMembersList.get(i).getGroup_id();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) { }
//        });

        // Date pickers
        tvFromDate.setOnClickListener(v -> datepicker((Button) tvFromDate));
        tvToDate.setOnClickListener(v -> datepicker(tvToDate));

        // Handle search button click
        btnSearch.setOnClickListener(v -> {
            try {
                callSearchResultsWebservice(
                        selectedCategory[0],
                        Objects.requireNonNull(et_team_member.getText()).toString(),
                        Objects.requireNonNull(et_client_name.getText()).toString(),
                        tvFromDate.getText().toString(),
                        Objects.requireNonNull(et_search_name.getText()).toString(),
                        tvToDate.getText().toString(),
                        viewGroupModel.getId()
                );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        // Handle cancel
        cancelIcon.setOnClickListener(v -> dialog.dismiss());
        callSearchResultsWebservice(
                selectedCategory[0],
                Objects.requireNonNull(et_team_member.getText()).toString(),
                Objects.requireNonNull(et_client_name.getText()).toString(),
                tvFromDate.getText().toString(),
                Objects.requireNonNull(et_search_name.getText()).toString(),
                tvToDate.getText().toString(),
                viewGroupModel.getId()
        );
        // Finally show the dialog
        dialog.show();
    }

    private void loadSearchResults(JSONArray data) throws JSONException {
        searchList.clear();
        for (int i = 0; i < data.length(); i++) {
            SearchDo searchDo = new SearchDo();
            JSONObject jsonObject = data.getJSONObject(i);
            searchDo.setCategory(jsonObject.getString("category"));
            searchDo.setTimestamp(jsonObject.getString("timestamp"));
            searchDo.setMsg(jsonObject.getString("msg"));
            searchList.add(searchDo);
        }
        loadSearchRecyclerview();
    }

    private void loadSearchRecyclerview() {
        rv_activity_log.setLayoutManager(new GridLayoutManager(getContext(), 1));
        SearchAdapter adapter = new SearchAdapter(searchList);
        rv_activity_log.setAdapter(adapter);
        et_search_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(et_search_message.getText().toString());
            }

        });
        // rv_activity_log.setHasFixedSize(true);
    }
    //        {
//            unhide_delete_data();
//            hide_CGH_UGM_data();
//            cv_activity_log.setVisibility(View.VISIBLE);
//            cv_details.setVisibility(View.GONE);
//            actions_List.clear();
//            et_search_tm.setVisibility(View.GONE);
//            et_search.setVisibility(View.GONE);
//            et_search_delete.setVisibility(View.GONE);
//            viewGroupMembersList.clear();
////        actions_List.add(new ActionModel("Add|Remove"));
//            actions_List.add(new ActionModel("Authorization"));
//            actions_List.add(new ActionModel("Groups"));
//            actions_List.add(new ActionModel("Team Members"));
//            actions_List.add(new ActionModel("Relationships"));
//            actions_List.add(new ActionModel("Share"));
//            actions_List.add(new ActionModel("Documents"));
//            actions_List.add(new ActionModel("Merge PDF"));
//            actions_List.add(new ActionModel("Matters"));
//            actions_List.add(new ActionModel("Timesheets"));
//            for (int i = 0; i < viewGroupModel.getMembers().length(); i++) {
//                ViewGroupModel viewGroupModel_1 = new ViewGroupModel();
//                JSONObject jsonObject = viewGroupModel.getMembers().getJSONObject(i);
//                viewGroupModel_1.setGroup_name(jsonObject.getString("name"));
//                viewGroupModel_1.setGroup_id(jsonObject.getString("id"));
//                viewGroupMembersList.add(viewGroupModel_1);
//            }
//            final CommonSpinnerAdapter spinner_adapter = new CommonSpinnerAdapter((Activity) getContext(), actions_List);
//            sp_category.setAdapter(spinner_adapter);
//            sp_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                    selected_category = actions_List.get(adapterView.getSelectedItemPosition()).getName();
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> adapterView) {
//
//                }
//            });
//            final CommonSpinnerAdapter team_adpater = new CommonSpinnerAdapter((Activity) getContext(), viewGroupMembersList);
//            sp_team_member.setAdapter(team_adpater);
//            sp_team_member.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                    selected_tm = viewGroupMembersList.get(adapterView.getSelectedItemPosition()).getGroup_id();
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> adapterView) {
//
//                }
//            });
//            tv_from_date.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                    datepicker(tv_from_date);
//                }
//            });
//            tv_to_date.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                    datepicker(tv_to_date);
//                }
//            });
////        btn_cancel_gal.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                mViewModel.setData("View Groups");
////                unhideData();
////                tv_from_date.setText("");
////                tv_to_date.setText("");
////                searchList.clear();
////                rv_activity_log.removeAllViews();
//////                tv_search_message.setVisibility(View.GONE);
////                ViewGroupsData();
////            }
////        });
//            btn_search_gal.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    //search layout displaying....
////                String update_type = "UGM";
//                    try {
//                        callSearchResultsWebservice(selected_category, selected_tm, tv_from_date.getText().toString(), tv_to_date.getText().toString(), viewGroupModel.getId());
//                        mViewModel.setData("View Groups");
//                    } catch (JSONException e) {
//                        e.fillInStackTrace();
//                    }

    /// /                callUpdateUGMWebservice(adapter.getList_item(););
//                }
//            });
//        }
    private void callSearchResultsWebservice(String selected_category, String
            selected_tm, String client, String from_date, String search, String to_date, String id) throws JSONException {
        JSONObject postdate = new JSONObject();
        postdate.put("category", selected_category);
        postdate.put("client", client);
        postdate.put("fromDate", from_date);
        postdate.put("search", search);
        postdate.put("tm", selected_tm);
        postdate.put("toDate", to_date);
        WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "v3/auditlogs/" + id, "Search Results", postdate.toString());
    }

    private void datepicker(Button bt_date) {
        final Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

            private void updateLabel() {
                String myFormat = "dd-MM-yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                bt_date.setText(sdf.format(myCalendar.getTime()));
            }
        };
        bt_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });
    }

    private void callViewGroupMembersWebservice() {
        try {
            JSONObject postdata = new JSONObject();
            progress_dialog = AndroidUtils.get_progress(getActivity());
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/member/groups", "Get Team Members", postdata.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    private void callUpdateGroupHeadWebservice(String id, String group_head) throws
            JSONException {
        try {
            JSONObject postData = new JSONObject();
            postData.put("groupHead", group_head);
            Log.i("Tag", "Info:" + id);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PATCH, "v3/group/" + id, "Update Group Head", postData.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void hide_CGH_UGM_data() {
        ll_group_list.setVisibility(GONE);
        ll_tm.setVisibility(GONE);
        select_ghead.setVisibility(GONE);
        ll_select_all.setVisibility(GONE);
        ll_select_tm.setVisibility(VISIBLE);
        rv_view_groups.setVisibility(GONE);
        rv_select_team_members.setVisibility(VISIBLE);
        //Make search TextInputLayout invisible
        tl_et_search.setVisibility(GONE);
        cv_groups.setVisibility(GONE);
        cv_details.setVisibility(VISIBLE);
        ll_edit_groups.setVisibility(GONE);
        tv_select_team_members.setVisibility(GONE);
    }

    private void callDeleteGroups(String id) throws JSONException {
        try {
            JSONObject postData = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.DELETE, "v3/group/" + id + "/" + group_head, "Delete Groups", postData.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }

    }

    private void callGroupsCounts(String id) {
        try {
            JSONObject postData = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/group/resources/counts/" + id, "Group Counts", postData.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void callUpdateGroups(String group_name, String description, String id, String update_type, ArrayList<GroupModel> list_item) throws JSONException {
        try {
            JSONObject postData = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            if (Objects.equals(update_type, "EG")) {
                postData.put("name", group_name);
                postData.put("description", description);
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PATCH, "v3/group/" + id, "Update Groups", postData.toString());
            } else if (Objects.equals(update_type, "UGM")) {
                for (int i = 0; i < list_item.size(); i++) {
                    GroupModel model = list_item.get(i);
                    if (model.isChecked()) {
                        jsonArray.put(model.getId());
                    }
                }
                String groupHeadId = new_viewGroupModel.getGroup_head_id();
                if (groupHeadId != null && !groupHeadId.isEmpty()) {
                    boolean alreadyIncluded = false;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        if (groupHeadId.equals(jsonArray.getString(i))) {
                            alreadyIncluded = true;
                            break;
                        }
                    }
                    if (!alreadyIncluded) {
                        jsonArray.put(groupHeadId);
                    }
                }
//                if (jsonArray.length() != 0) {
                postData.put("members", jsonArray);
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PATCH, "v3/group/" + id, "Update Groups", postData.toString());
//                } else {
//                    AndroidUtils.showAlert("Please select atleast one team member", getActivity());
//                }
            }
//            AndroidUtils.showAlert(postData.toString(),getContext());
            Log.i("TAG", "Object:" + postData.toString() + ":" + id);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void clear_list() {
        viewGroupModelArrayList.clear();
        viewGroupMembersList.clear();
        selectedTMArrayList.clear();
        if (adapter != null)
            adapter.getList_item().clear();
        updateGroupMembersList.clear();
    }

    private void hideData() {
        ll_group_list.setVisibility(GONE);
        ll_tm.setVisibility(GONE);
        select_ghead.setVisibility(GONE);
        ll_select_all.setVisibility(GONE);
        ll_select_tm.setVisibility(VISIBLE);
        rv_view_groups.setVisibility(GONE);
        rv_select_team_members.setVisibility(VISIBLE);
        //Make search TextInputLayout invisible
        tl_et_search.setVisibility(GONE);
        cv_groups.setVisibility(VISIBLE);
        ll_edit_groups.setVisibility(VISIBLE);
        tv_select_team_members.setVisibility(GONE);
    }

    private void reverse_data() {
        tv_group_description.setEnabled(true);
        tv_group_name.setEnabled(true);
        btn_update.setVisibility(VISIBLE);
        btn_cancel_edit.setVisibility(VISIBLE);
        cv_details.setVisibility(GONE);
        unhideData();
//        ViewGroupsData();
    }

    public void hidedelete_data() {
//        search_delete.setVisibility(View.VISIBLE);
        et_search_delete.setVisibility(VISIBLE);
        et_search_tm.setVisibility(GONE);
    }

    public void unhide_delete_data() {
//        et_search.setVisibility(View.VISIBLE);
//        search_delete.setVisibility(View.GONE);
        et_search_delete.setVisibility(GONE);
        et_search_tm.setVisibility(VISIBLE);
    }

    public void unhide_cgroup() {
        ll_edit_groups.setVisibility(VISIBLE);
    }

    private void unhideData() {
        ll_group_list.setVisibility(VISIBLE);
        ll_tm.setVisibility(VISIBLE);
        ll_select_all.setVisibility(VISIBLE);
        select_ghead.setVisibility(GONE);
        ll_select_tm.setVisibility(VISIBLE);
        rv_view_groups.setVisibility(VISIBLE);
        //Make search TextInputLayout Visible
        tl_et_search.setVisibility(VISIBLE);
        cv_groups.setVisibility(GONE);
        ll_edit_groups.setVisibility(GONE);
        tv_select_team_members.setVisibility(VISIBLE);
        cv_activity_log.setVisibility(GONE);
    }

    public void check_select_all(boolean check_status) {
        chk_select_all.setChecked(check_status);
//        chk_select_all.setChecked(true);
    }

    public void page_name(String action_list) {
        if (Objects.equals(action_list, "Edit Group Info")) {
            mViewModel.setData("Edit Group Info");
        } else if (Objects.equals(action_list, "Assign Group")) {
            mViewModel.setData("Assign Group");
        } else if (Objects.equals(action_list, "Update Group Members List")) {
            mViewModel.setData("Update Group Members List");
        } else if (Objects.equals(action_list, "Update Group Head")) {
            mViewModel.setData("Update Group Head");
        } else if (Objects.equals(action_list, "Group Activity Log")) {
            mViewModel.setData("Group Activity Log");
        } else {
            mViewModel.setData("View Groups");
        }
    }
}

