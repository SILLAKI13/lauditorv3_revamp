package com.digicoffer.lauditor.Matter.OldViewModels;

import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.isValidEmail;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.cemail_alert;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.email_alert;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Relationships.Model.CountriesDO;
import com.digicoffer.lauditor.Matter.Adapters.GroupsAdapter;
import com.digicoffer.lauditor.Matter.Models.AdvocateModel;
import com.digicoffer.lauditor.Matter.Models.ClientGroupModel;
import com.digicoffer.lauditor.Matter.Models.ClientsModel;
import com.digicoffer.lauditor.Matter.Models.DocumentsModel;
import com.digicoffer.lauditor.Matter.Models.GroupsModel;
import com.digicoffer.lauditor.Matter.Models.MatterModel;
import com.digicoffer.lauditor.Matter.Models.TeamModel;
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel;
import com.digicoffer.lauditor.Matter.ViewModels.Matter;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class
GCT extends Fragment implements View.OnClickListener, AsyncTaskCompleteListener {
    String corp_client_id = "";
    ListView sp_corp_client;
    AlertDialog ad_dialog_copy;
    ArrayList<ClientGroupModel> allClientGroups = new ArrayList<>();
    ArrayList<ClientsModel> filteredClientsList = new ArrayList<>();
    boolean is_fname_empty = true, is_lname_empty = true, is_email_empty = true, is_confirm_email_empty = true;
    MatterModel matterModel = new MatterModel();
    TextView response_email, response_cemail;
    boolean isclient = true, iscorpclient = false, istempclient = false;
    TextView matter_date, at_add_groups, at_add_clients, at_assigned_team_members, add_groups, add_clients, tv_assigned_team_members;
    TextView at_add_corp_clients;
    LinearLayout upload_corp_client_layout;
    RecyclerView rv_display_upload_corp_client_docs;
    boolean[] selectedLanguage;
    boolean[] selectedClients;
    int position = 0;
    boolean[] selcted_corp_Clients;
    boolean[] selectedTM;
    boolean is_error;
    String attachment_type;
    String country_name = "";
    String client_type = "consumer";
    String success_msg = "", temp_rel_id = "", temp_client_id = "", temp_name = "";
    JSONArray client_list = new JSONArray();
    ArrayList<ClientsModel> clientsList = new ArrayList<>();
    ArrayList<ClientsModel> corp_clients_list = new ArrayList<>();
    ArrayList<String> corp_clients_name = new ArrayList<>();
    ArrayList<MatterModel> matterArraylist;
    ArrayList<CountriesDO> countriesList = new ArrayList<>();
    TextView matter_title_tv, tv_name, tv_selected_clients, tv_selected_tm, tv_selected_temp_clients;
    ConstraintLayout cv_details;
    String matter_title, case_number, case_type, description, dof, start_date, end_date, court, judge, case_priority, case_status;
    JSONArray existing_clients;
    JSONArray exisiting_group_acls;
    JSONArray existing_corp_clients;
    JSONArray existing_temp_clients;
    JSONArray existing_members;

    CardView cv_client_details;
    LinearLayout ll_add_groups;
    MatterInformation matterInformation;

    public ArrayList<DocumentsModel> selected_documents_list = new ArrayList<>();
    ArrayList<AdvocateModel> advocates_list = new ArrayList<>();
    JSONArray existing_groups_list;
    JSONArray existing_clients_list;
    JSONArray existing_tm_list;
    JSONArray existing_documents;
    JSONArray existing_documents_list;
    String ADAPTER_TAG = "Groups";
    String chosen_matter = "";
    Button btn_add_groups, btn_add_clients, btn_add_teammembers, btn_create, btn_cancel_save;
    Button btn_add_corp_clients;
    LinearLayout ll_selected_groups, ll_selected_clients, ll_assigned_team_members, selected_groups, selected_clients, selected_temp_clients, ll_selected_temp_clients, selected_tm, ll_add_clients, ll_assign_team_members;
    LinearLayout selected_corp_clients, ll_selected_corp_clients, ll_matterDate;
    TextView tv_selected_corp_clients;
    Dialog progress_dialog;
    boolean iscountry = true;
    public ArrayList<GroupsModel> selected_groups_list = new ArrayList<>();
    ArrayList<GroupsModel> updated_groups_list = new ArrayList<>();
    public ArrayList<ClientsModel> selected_clients_list = new ArrayList<>();
    public ArrayList<ClientsModel> temporary_clients_list = new ArrayList<>();
    public ArrayList<ClientsModel> temporary_corpclients_list = new ArrayList<>();
    public ArrayList<GroupsModel> temporary_groups_list = new ArrayList<>();
    public ArrayList<TeamModel> temporary_tm_list = new ArrayList<>();
    ArrayList<ClientsModel> old_clients_list = new ArrayList<>();
    public ArrayList<ClientsModel> selected_corp_clients_list = new ArrayList<>();
    ArrayList<ClientsModel> selected_temp_clients_list = new ArrayList<>();
    public ArrayList<TeamModel> selected_tm_list = new ArrayList<>();
    ArrayList<TeamModel> owner_tm = new ArrayList<>();
    ArrayList<GroupsModel> groupsList = new ArrayList<>();
    boolean ischecked_group = true;
    LinearLayoutCompat ll_save_buttons;
    boolean ischecked_client = true;
    boolean ischecked_corp_client = true;
    boolean ischecked_tm = true;
    public ArrayList<DocumentsModel> documentsList = new ArrayList<>();
    ArrayList<ViewMatterModel> new_groupsList = new ArrayList<>();
    ArrayList<TeamModel> tmList = new ArrayList<>();
    Matter matter;
    ListView sp_country;
    LinearLayoutCompat clients_list_layout;
    LinearLayout temp_client_layout, ll_sp_country;
    ImageView img_country_clear, img_country_dropdown;
    Button btn_add_temp_client;
    TextView tv_add_client, tv_temp_client, tv_corp_client, tv_temp_fname, tv_temp_lname, tv_temp_email, tv_temp_confirm_email, tv_temp_country, et_temp_country, tv_temp_phone, tv_temp_individual, tv_temp_entity;
    TextInputEditText et_temp_fname, et_temp_lname, et_temp_email, et_temp_confirm_email, et_temp_phone;
    RecyclerView rv_display_upload_groups_docs, rv_display_upload_client_docs, rv_display_upload_tm_docs;

    GroupsAdapter groupsAdapter, clientsAdapter, teamsAdapter;
    private JSONArray existing_opponents;
    private ArrayList<String> tag_list = new ArrayList<>();
    private JSONArray existing_tags_list;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gct_layout, container, false);
        matter_date = view.findViewById(R.id.matter_date);
        ll_matterDate = view.findViewById(R.id.ll_matterDate);
        ll_matterDate.setVisibility(View.GONE);
        at_add_groups = view.findViewById(R.id.at_add_groups);
        clients_list_layout = view.findViewById(R.id.clients_list_layout);
        clients_list_layout.setVisibility(View.GONE);
        temp_client_layout = view.findViewById(R.id.temp_client_layout);
        temp_client_layout.setVisibility(View.GONE);
        sp_corp_client = view.findViewById(R.id.sp_corp_client);

        tv_temp_fname = view.findViewById(R.id.tv_temp_fname);
        tv_temp_fname.setText(R.string.first_name);
        //..
        tv_temp_lname = view.findViewById(R.id.tv_temp_lname);
        tv_temp_lname.setText(R.string.last_name);
        //..
        tv_temp_email = view.findViewById(R.id.tv_temp_email);
        tv_temp_email.setText(R.string.email);
        //..
        tv_temp_confirm_email = view.findViewById(R.id.tv_temp_confirm_email);
        tv_temp_confirm_email.setText(R.string.confirm_email);
        //..
        tv_temp_country = view.findViewById(R.id.tv_temp_country);
        tv_temp_country.setText(R.string.country);
        sp_country = view.findViewById(R.id.sp_country);
        sp_country.setVisibility(View.GONE);
        //..
        ll_sp_country = view.findViewById(R.id.ll_sp_country);
        img_country_clear = ll_sp_country.findViewById(R.id.img_clear_icon);
        img_country_dropdown = ll_sp_country.findViewById(R.id.img_dropdown_icon);
        et_temp_country = ll_sp_country.findViewById(R.id.tv_spinner_view);
        et_temp_country.setHint(R.string.select_country);
        response_email = view.findViewById(R.id.response_email);
        response_email.setVisibility(View.GONE);
        response_cemail = view.findViewById(R.id.response_cemail);

        img_country_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.DisplaySpinnerView(sp_country, et_temp_country, country_name, img_country_dropdown, img_country_clear, false);
                AddTempClientStatus();
                iscountry = true;
            }
        });
        ll_sp_country.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.display_listview(iscountry, sp_country);
                call_country_list();
                iscountry = !iscountry;
//                sp_country.setVisibility(View.GONE);


            }
        });
        //..
        tv_temp_phone = view.findViewById(R.id.tv_temp_phone);
        tv_temp_phone.setText(R.string.phone);
        //..
        et_temp_fname = view.findViewById(R.id.et_temp_fname);
        et_temp_fname.setHint(R.string.first_name);
        et_temp_fname.addTextChangedListener(new Validation(et_temp_fname));
        //..
        et_temp_lname = view.findViewById(R.id.et_temp_lname);
        et_temp_lname.setHint(R.string.last_name);
        et_temp_lname.addTextChangedListener(new Validation(et_temp_lname));
        //..
        et_temp_email = view.findViewById(R.id.et_temp_email);
        et_temp_email.setHint(R.string.email);
        et_temp_email.addTextChangedListener(new Validation(et_temp_email));
        //..
        et_temp_confirm_email = view.findViewById(R.id.et_temp_confirm_email);
        et_temp_confirm_email.setHint(R.string.confirm_email);
        et_temp_confirm_email.addTextChangedListener(new Validation(et_temp_confirm_email));
        //..
        et_temp_phone = view.findViewById(R.id.et_temp_phone);
        et_temp_phone.setHint(R.string.phone);
        et_temp_phone.addTextChangedListener(new Validation(et_temp_phone));
        et_temp_phone.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter[] filters = new InputFilter[]{
                new InputFilter.LengthFilter(10)
        };
        et_temp_phone.setFilters(filters);
        //..
        tv_temp_individual = view.findViewById(R.id.tv_temp_individual);
        tv_temp_individual.setText(R.string.individual);
        tv_temp_individual.setTextColor(getResources().getColor(R.color.white));
        //..
        tv_temp_entity = view.findViewById(R.id.tv_temp_entity);
        tv_temp_entity.setBackground(getContext().getDrawable(R.drawable.button_right_round_background));
        tv_temp_entity.setText(R.string.entity);
        //..sd
        btn_add_temp_client = view.findViewById(R.id.btn_add_temp_client);
        btn_add_temp_client.setAlpha(0.5f);
        btn_add_temp_client.setEnabled(false);
        View.OnFocusChangeListener commonFocusListener = (v, hasFocus) -> {
            if (!hasFocus) {
                AddTempClientStatus();
            }
        };
        et_temp_fname.setOnFocusChangeListener(commonFocusListener);
        et_temp_lname.setOnFocusChangeListener(commonFocusListener);
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
                AddTempClientStatus();
            }
        };

        et_temp_email.addTextChangedListener(textWatcher);
        et_temp_confirm_email.addTextChangedListener(textWatcher);

        btn_add_temp_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check_temp_values();
            }
        });
        sp_country.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                country_name = countriesList.get(position).getName();
                AndroidUtils.DisplaySpinnerView(sp_country, et_temp_country, country_name, img_country_dropdown, img_country_clear, true);
                AddTempClientStatus();
                iscountry = true;
            }
        });
        // Listener for "Individual" button
        // Listener for "Individual" button
        tv_temp_individual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((!et_temp_fname.getText().toString().isEmpty()) || (!et_temp_lname.getText().toString().isEmpty()) || (!et_temp_email.getText().toString().isEmpty()) || (!et_temp_confirm_email.getText().toString().isEmpty()) || (!et_temp_country.getText().toString().isEmpty()) || (!et_temp_phone.getText().toString().isEmpty()))) {
                    Alert(false);
                } else {
                    tv_temp_individual.setTextColor(getResources().getColor(R.color.white));
                    tv_temp_entity.setTextColor(getResources().getColor(R.color.black));
                    client_type = "consumer";
                    tv_temp_individual.setBackgroundDrawable(getContext().getDrawable(R.drawable.button_left_green_round_background));
                    tv_temp_entity.setBackgroundDrawable(getContext().getDrawable(R.drawable.button_right_round_background));
                    tv_temp_lname.setText(R.string.last_name);
                    et_temp_lname.setHint(R.string.last_name);
                    tv_temp_fname.setText(R.string.first_name);
                    et_temp_fname.setHint(R.string.first_name);
                }
//                    et_temp_fname.setText("");
//                    et_temp_lname.setText("");
//                    et_temp_email.setText("");
//                    et_temp_country.setText("");
//                    et_temp_confirm_email.setText("");
//                if (((et_temp_fname.getText().toString().isEmpty()) && (et_temp_lname.getText().toString().isEmpty()) && (et_temp_email.getText().toString().isEmpty()) && (et_temp_confirm_email.getText().toString().isEmpty()) && (et_temp_country.getText().toString().isEmpty()) && (et_temp_phone.getText().toString().isEmpty()))) {
//
//                }else {
//                    Alert();
//                }
            }
        });

// Listener for "Entity" button
        tv_temp_entity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If fields are empty, switch to "Entity" mode
                // Change colors and backgrounds
                if (((!et_temp_fname.getText().toString().isEmpty()) || (!et_temp_lname.getText().toString().isEmpty()) || (!et_temp_email.getText().toString().isEmpty()) || (!et_temp_confirm_email.getText().toString().isEmpty()) || (!et_temp_country.getText().toString().isEmpty()) || (!et_temp_phone.getText().toString().isEmpty()))) {
                    Alert(true);
                } else {
                    tv_temp_individual.setTextColor(getResources().getColor(R.color.black));
                    tv_temp_entity.setTextColor(getResources().getColor(R.color.white));
                    client_type = "entity";
                    tv_temp_individual.setBackgroundDrawable(getContext().getDrawable(R.drawable.button_left_round_background));
                    tv_temp_entity.setBackgroundDrawable(getContext().getDrawable(R.drawable.button_right_green_round_background));
                    tv_temp_lname.setText(R.string.contact_person);
                    et_temp_lname.setHint(R.string.contact_person);
                    tv_temp_fname.setText(R.string.firm_name);
                    et_temp_fname.setHint(R.string.firm_name);
                }
//                    et_temp_fname.setText("");
//                    et_temp_lname.setText("");
//                    et_temp_email.setText("");
//                    et_temp_country.setText("");
//                    et_temp_confirm_email.setText("");
            }
        });


        tv_add_client = view.findViewById(R.id.tv_add_client);
        tv_add_client.setText(R.string.add_clients);
        tv_add_client.setTextColor(getResources().getColor(R.color.white));

        tv_temp_client = view.findViewById(R.id.tv_temp_client);
        tv_temp_client.setText(R.string.temp_clients);
        tv_temp_client.setBackground(getContext().getDrawable(R.drawable.radiobutton_centre_background));

        tv_corp_client = view.findViewById(R.id.tv_corp_client);
        tv_corp_client.setText(R.string.corporate_clients);
        tv_corp_client.setBackground(getContext().getDrawable(R.drawable.button_right_round_background));
        //..
//        tv_add_client.setBackgroundDrawable(getContext().getDrawable(R.drawable.button_left_green_round_background));
//        tv_temp_client.setBackgroundDrawable(getContext().getDrawable(R.drawable.radiobutton_centre_green_background));
//        tv_corp_client.setBackgroundDrawable(getContext().getDrawable(R.drawable.button_right_green_round_background));
        //...
        at_add_groups.setHint(R.string.select_assign_groups);
        tv_selected_clients = view.findViewById(R.id.tv_selected_clients);
        tv_selected_clients.setText(R.string.selected_clients);
        selected_corp_clients = view.findViewById(R.id.selected_corp_clients);
        selected_corp_clients.setVisibility(View.GONE);
        tv_selected_corp_clients = view.findViewById(R.id.tv_selected_corp_clients);
        tv_selected_corp_clients.setText(R.string.selected_corporate_clients);
        tv_selected_tm = view.findViewById(R.id.tv_selected_tm);
        tv_selected_tm.setText(R.string.assigned_team_members);
        tv_name = view.findViewById(R.id.tv_name);
        tv_name.setText(R.string.selected_groups);
        at_add_groups.setOnClickListener(this);
        add_groups = view.findViewById(R.id.add_groups);
        btn_cancel_save = view.findViewById(R.id.btn_cancel_save);
        cv_details = view.findViewById(R.id.cv_details);
        ll_save_buttons = view.findViewById(R.id.ll_save_buttons);
        ll_save_buttons.setVisibility(View.GONE);
        ll_add_clients = view.findViewById(R.id.ll_add_clients);
        ll_add_clients.setVisibility(View.GONE);
        rv_display_upload_groups_docs = view.findViewById(R.id.rv_display_upload_groups_docs);
        rv_display_upload_groups_docs.setBackground(getContext().getDrawable(R.drawable.rectangle_light_grey_bg));
        rv_display_upload_groups_docs.setVisibility(View.GONE);
        rv_display_upload_client_docs = view.findViewById(R.id.rv_display_upload_client_docs);
        rv_display_upload_client_docs.setBackground(getContext().getDrawable(R.drawable.rectangle_light_grey_bg));
        rv_display_upload_client_docs.setVisibility(View.GONE);

        rv_display_upload_corp_client_docs = view.findViewById(R.id.rv_display_upload_corp_client_docs);
        rv_display_upload_corp_client_docs.setBackground(getContext().getDrawable(R.drawable.rectangle_light_grey_bg));
        rv_display_upload_corp_client_docs.setVisibility(View.GONE);

        rv_display_upload_tm_docs = view.findViewById(R.id.rv_display_upload_tm_docs);
        rv_display_upload_tm_docs.setBackground(getContext().getDrawable(R.drawable.rectangle_light_grey_bg));
        rv_display_upload_tm_docs.setVisibility(View.GONE);
        ll_add_groups = view.findViewById(R.id.ll_add_groups);
        add_groups.setText(R.string.assign_group);
        add_clients = view.findViewById(R.id.add_clients);
        add_clients.setText(R.string.add_clients);
        cv_client_details = view.findViewById(R.id.cv_client_details);
        tv_assigned_team_members = view.findViewById(R.id.tv_assigned_team_members);
        tv_assigned_team_members.setText(R.string.assign_team_members);
        at_add_clients = view.findViewById(R.id.at_add_clients);
        at_add_clients.setHint(R.string.select_clients);
        at_add_corp_clients = view.findViewById(R.id.at_add_corp_clients);
        at_add_corp_clients.setVisibility(View.GONE);
        at_add_corp_clients.setHint(R.string.select_corporate_client);
        matter_title_tv = view.findViewById(R.id.matter_title);
        matter_title_tv.setVisibility(View.VISIBLE);
        at_add_clients.setOnClickListener(this);
        at_assigned_team_members = view.findViewById(R.id.at_assigned_team_members);
//        at_assigned_team_members.setOnClickListener(this);
        at_assigned_team_members.setHint(R.string.select_assign_team_members);
        btn_add_groups = view.findViewById(R.id.btn_add_groups);
        btn_add_groups.setText(R.string.add);
        selected_groups = view.findViewById(R.id.selected_groups);
        selected_clients = view.findViewById(R.id.selected_clients);
        selected_clients.setVisibility(View.GONE);
        selected_tm = view.findViewById(R.id.selected_tm);
        selected_tm.setVisibility(View.GONE);
//        btn_add_groups.setOnClickListener(this);
        btn_add_corp_clients = view.findViewById(R.id.btn_add_corp_clients);
        btn_add_corp_clients.setText(R.string.add);

        btn_add_clients = view.findViewById(R.id.btn_add_clients);
        btn_add_clients.setText(R.string.add);
//        btn_add_clients.setOnClickListener(this);

        ll_assign_team_members = view.findViewById(R.id.ll_assign_team_members);
        ll_assign_team_members.setVisibility(View.GONE);
        //  String matter_title = tv_matter_title.getText().toString();


        btn_create = view.findViewById(R.id.btn_create);
        btn_create.setOnClickListener(this);
        btn_add_teammembers = view.findViewById(R.id.btn_assigned_team_members);
        btn_add_teammembers.setText(R.string.add);
        AndroidUtils.ToggleButton(selected_groups_list.size(), btn_add_groups);
        AndroidUtils.ToggleButton(selected_clients_list.size(), btn_add_clients);
        AndroidUtils.ToggleButton(selected_tm_list.size(), btn_add_teammembers);
//        btn_add_teammembers.setOnClickListener(this);
        ll_selected_groups = view.findViewById(R.id.ll_selected_groups);
        ll_assigned_team_members = view.findViewById(R.id.ll_assigned_team_members);
        ll_selected_clients = view.findViewById(R.id.ll_selected_clients);
        ll_selected_temp_clients = view.findViewById(R.id.ll_selected_temp_clients);
        selected_temp_clients = view.findViewById(R.id.selected_temp_clients);
        tv_selected_temp_clients = view.findViewById(R.id.tv_selected_temp_clients);
        tv_selected_temp_clients.setText(R.string.selected_temp_clients);
        ll_selected_corp_clients = view.findViewById(R.id.ll_selected_corp_clients);
        Calendar myCalendar = Calendar.getInstance();
//        TextInputEditText tv_matter_title = view. findViewById(R.id.tv_matter_title);
//        AppCompatButton matter_title = view.findViewById(R.id.matter_title);
//        AppCompatButton finalmatter_title = matter_title;
//        matter_title.setOnClickListener(new View.OnClickListener(){
//            private BreakIterator finalMatter_title;
//
//            public void onClick(View view){
//                String item = tv_matter_title.getText().toString();
//                finalMatter_title.setText(item);
//            }
//        });
//        et_temp_email.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
        tv_add_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAddClient();
            }
        });
        tv_temp_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddTemp();
            }
        });
        tv_corp_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddCorporate();
            }
        });
        at_add_groups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ischecked_group) {
                    GroupsPopup();
                    rv_display_upload_groups_docs.setVisibility(View.VISIBLE);
//                    btn_add_groups.setEnabled(true);
                } else {
                    rv_display_upload_groups_docs.setVisibility(View.GONE);
//                    btn_add_groups.setEnabled(false);
                    loadGroupsText();
                }
//                updateDisplay();
                ischecked_group = !ischecked_group;
            }
        });
        at_add_corp_clients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ischecked_corp_client) {
                    sp_corp_client.setVisibility(View.VISIBLE);
                    call_corporate_clients();
                } else {
                    sp_corp_client.setVisibility(View.GONE);
                }
                ischecked_corp_client = !ischecked_corp_client;
            }
        });
        at_add_clients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ischecked_client) {
                    if (clientsList.isEmpty()) {
                        callClientsWebservice();
                    } else {
                        rv_display_upload_client_docs.setVisibility(View.VISIBLE);
                        ClientssPopUp();
                    }
                } else {
                    rv_display_upload_client_docs.setVisibility(View.GONE);
                    loadClientsText();
//                    loadClients();
                }
                ischecked_client = !ischecked_client;
            }
        });


        at_assigned_team_members.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                tmList.clear();
                if (ischecked_tm) {
//                    if (tmList.isEmpty()) {
                    if (!Constants.create_matter) {
                        rv_display_upload_tm_docs.setVisibility(View.VISIBLE);
                        TeamPopUp();
                    } else {
                        if (tmList.isEmpty()) {
//                            callTMWebservice();
                        } else {
                            rv_display_upload_tm_docs.setVisibility(View.VISIBLE);
                            TeamPopUp();
                        }
                    }
//                    } else {
//                        rv_display_upload_tm_docs.setVisibility(View.VISIBLE);
//                        TeamPopUp();
//                    }
                } else {
                    rv_display_upload_tm_docs.setVisibility(View.GONE);
                    loadTeamText();
                }
//                loadTeam();
                //  callClientsWebservice();
                ischecked_tm = !ischecked_tm;
            }
        });

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
//                DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(), date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
//                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
//                datePickerDialog.show();
//            }
//        });
        AndroidUtils.ToggleButton(temporary_corpclients_list.size(), btn_add_corp_clients);
        sp_corp_client.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                temporary_corpclients_list.clear();
                ClientsModel clientsModel = corp_clients_list.get(position);
                clientsModel.setClient_id(clientsModel.getClient_id());
                clientsModel.setClient_name(clientsModel.getClient_name());
                clientsModel.setClient_type(clientsModel.getClient_type());
                temporary_corpclients_list.add(clientsModel);
                at_add_corp_clients.setText(clientsModel.getClient_name());
                AndroidUtils.ToggleButton(temporary_corpclients_list.size(), btn_add_corp_clients);
//                loadselected_corp_list();
            }
        });
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
//        matter_date.setText(formattedDate);
        matter = (Matter) getParentFragment();
        assert matter != null;

        btn_cancel_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Constants.create_matter) {
                    matter.loadViewUI();
                } else {
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
                }
            }
        });
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cv_client_details.setVisibility(View.VISIBLE);
                cv_details.setVisibility(View.VISIBLE);
                if (!Constants.create_matter) {
                    try {
                        update_matter();
                    } catch (Exception e) {
                        e.fillInStackTrace();
                    }
                } else {

                    saveGCTinformation();
                }
//
//                ll_add_clients.setVisibility(View.VISIBLE);
//                ll_assign_team_members.setVisibility(View.VISIBLE);
//                ll_save_buttons.setVisibility(View.VISIBLE);
            }
        });
//        callGroupsWebservice();
        btn_add_clients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                    Add_Clients();
                ischecked_client = true;
                rv_display_upload_client_docs.setVisibility(View.GONE);
                selected_clients_list.clear();
                selected_clients_list.addAll(temporary_clients_list);
//                    selected_documents_list.clear();
                documentsList.clear();
                loadClients();
                if (!Constants.create_matter) {
                    AndroidUtils.ToggleButton(temporary_clients_list.size(), btn_create);
                }
//                    if (!Constants.create_matter) {
//                        try {
//                            load_existing_member_list();
//                        } catch (JSONException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
            }
        });
        ToggleClient();
//        else
//            loadAddClient();
        return view;
    }

    private boolean isAtLeastOneListNonEmpty() {
        return !selected_clients_list.isEmpty()
                || !selected_corp_clients_list.isEmpty()
                || !selected_temp_clients_list.isEmpty()
                || !selected_groups_list.isEmpty()
                || !selected_tm_list.isEmpty();
    }

    @Override
    public void onResume() {
        super.onResume();
        matterArraylist = matter.getMatter_arraylist();
//        detectListChanges();
        if (Constants.create_matter) {
            if (!matterArraylist.isEmpty()) {
                for (int i = 0; i < matterArraylist.size(); i++) {
                    MatterModel matterModel = matterArraylist.get(i);
                    matter_title_tv.setText("");
                    matter_title_tv.setText(matterModel.getMatter_title());
                    if (matterModel.getGroup_acls() != null) {
                        exisiting_group_acls = matterModel.getGroup_acls();
                        selected_groups_list.clear();
                        temporary_groups_list.clear();
                        try {
                            for (int g = 0; g < exisiting_group_acls.length(); g++) {
                                GroupsModel groupsModel = new GroupsModel();
                                JSONObject jsonObject = exisiting_group_acls.getJSONObject(g);
                                groupsModel.setGroup_id(jsonObject.getString("id"));
                                groupsModel.setGroup_name(jsonObject.getString("name"));
                                groupsModel.setChecked(jsonObject.getBoolean("isChecked"));
                                selected_groups_list.add(groupsModel);
                                temporary_groups_list.add(groupsModel);
                            }
                        } catch (JSONException e) {
                            e.fillInStackTrace();
                        }
                    }
                    if (matterModel.getClients() != null) {
                        existing_clients = matterModel.getClients();
                        temporary_clients_list.clear();
                        selected_clients_list.clear();
                        try {
                            for (int p = 0; p < existing_clients.length(); p++) {
                                ClientsModel clientsModel = new ClientsModel();
                                JSONObject jsonObject = existing_clients.getJSONObject(p);
                                clientsModel.setClient_id(jsonObject.getString("id"));
                                clientsModel.setClient_name(jsonObject.getString("name"));
                                clientsModel.setClient_type(jsonObject.getString("type"));
                                temporary_clients_list.add(clientsModel);
                                selected_clients_list.add(clientsModel);
                            }
                        } catch (JSONException e) {
                            e.fillInStackTrace();
                        }
                    }
                    if (matterModel.getMembers() != null) {
                        existing_members = matterModel.getMembers();
                        temporary_tm_list.clear();
                        selected_tm_list.clear();
                        try {
                            for (int t = 0; t < existing_members.length(); t++) {
                                TeamModel teamModel = new TeamModel();
                                JSONObject jsonObject = existing_members.getJSONObject(t);
                                teamModel.setTm_id(jsonObject.getString("id"));
                                teamModel.setTm_name(jsonObject.getString("name"));
                                teamModel.setUser_id(jsonObject.getString("user_id"));
                                temporary_tm_list.add(teamModel);
                                selected_tm_list.add(teamModel);
                            }
                        } catch (JSONException e) {
                            e.fillInStackTrace();
                        }
                    }
                    //Selected Corporate Clients list
                    if (matterModel.getCorp_clients_list() != null) {
                        existing_corp_clients = matterModel.getCorp_clients_list();
                        temporary_corpclients_list.clear();
                        selected_corp_clients_list.clear();
                        try {
                            for (int p = 0; p < existing_corp_clients.length(); p++) {
                                ClientsModel clientsModel = new ClientsModel();
                                JSONObject jsonObject = existing_corp_clients.getJSONObject(p);
                                clientsModel.setClient_id(jsonObject.getString("id"));
                                clientsModel.setClient_name(jsonObject.getString("name"));
                                clientsModel.setClient_type(jsonObject.getString("type"));
                                temporary_corpclients_list.add(clientsModel);
                                selected_corp_clients_list.add(clientsModel);
                            }
                        } catch (JSONException e) {
                            e.fillInStackTrace();
                        }
                    }
                    //Selected Temp Clients list
                    if (matterModel.getTemp_clients_list() != null) {
                        existing_temp_clients = matterModel.getTemp_clients_list();
                        selected_temp_clients_list.clear();
                        Constants.selected_temp_clients_list.clear();
                        try {
                            for (int p = 0; p < existing_temp_clients.length(); p++) {
                                ClientsModel clientsModel = new ClientsModel();
                                JSONObject jsonObject = existing_temp_clients.getJSONObject(p);
                                clientsModel.setClient_id(jsonObject.getString("id"));
                                clientsModel.setClient_name(jsonObject.getString("name"));
                                clientsModel.setRel_id(jsonObject.getString("rel_id"));
                                clientsModel.setClient_type(jsonObject.getString("type"));
                                selected_temp_clients_list.add(clientsModel);
                                Constants.selected_temp_clients_list.add(clientsModel);
                            }
                            ll_selected_temp_clients.setVisibility(View.VISIBLE);
                            loadselectedTempClients();
                        } catch (JSONException e) {
                            e.fillInStackTrace();
                        }
                    }
                    if (matterModel.getGroups_list() != null) {
                        existing_groups_list = matterModel.getGroups_list();
                        groupsList.clear();
//                        temporary_groups_list.clear();
                        try {
                            for (int m = 0; m < existing_groups_list.length(); m++) {
                                GroupsModel groupsModel = new GroupsModel();
                                JSONObject jsonObject = existing_groups_list.getJSONObject(m);
                                groupsModel.setGroup_id(jsonObject.getString("id"));
                                groupsModel.setGroup_name(jsonObject.getString("name"));
                                if ((!jsonObject.getString("name").equals("AAM")) && ((!jsonObject.getString("name").equals("SuperUser")))) {
                                    groupsList.add(groupsModel);
//                                    temporary_groups_list.add(groupsModel);
                                }
                            }
                        } catch (JSONException e) {
                            e.fillInStackTrace();
                        }
                    }
                    if (Constants.allClientGroups != null) {
//                        selected_groups_list.clear();
                        temporary_groups_list.clear();
                        allClientGroups.clear();
                        allClientGroups.addAll(Constants.allClientGroups);
                        if (!Constants.allClientGroups.isEmpty()) {
                            for (GroupsModel group : groupsList) {
                                List<ClientGroupModel> clientsInGroup = new ArrayList<>();
                                for (ClientGroupModel client : allClientGroups) {
                                    if (client.getGroups().contains(group.getGroup_id())) {
                                        clientsInGroup.add(client);
                                    }
                                }
                                group.setClientGroupModelList(clientsInGroup);
                            }
                            for (GroupsModel group : selected_groups_list) {
                                List<ClientGroupModel> clientsInGroup = new ArrayList<>();
                                for (ClientGroupModel client : allClientGroups) {
                                    if (client.getGroups().contains(group.getGroup_id())) {
                                        clientsInGroup.add(client);
                                    }
                                }
                                group.setClientGroupModelList(clientsInGroup);
                            }
                        }
                    }
                    temporary_groups_list.addAll(selected_groups_list);
                    if (matterModel.getClients_list() != null) {
                        existing_clients_list = matterModel.getClients_list();
                        clientsList.clear();
                        try {
                            for (int n = 0; n < existing_clients_list.length(); n++) {
                                ClientsModel clientsModel = new ClientsModel();
                                JSONObject jsonObject = existing_clients_list.getJSONObject(n);
                                clientsModel.setClient_id(jsonObject.getString("id"));
                                clientsModel.setClient_name(jsonObject.getString("name"));
                                clientsModel.setClient_type(jsonObject.getString("type"));
                                clientsList.add(clientsModel);
                            }
                        } catch (JSONException e) {
                            e.fillInStackTrace();
                        }
                    }
                    if (matterModel.getMembers_list() != null) {
                        existing_tm_list = matterModel.getMembers_list();
                        tmList.clear();
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
                    tag_list.clear();
                    if (matterModel.getTags_list() != null) {
                        existing_tags_list = matterModel.getTags_list();
                    }
                    if (existing_tags_list != null && existing_tags_list.length() > 0) {
                        for (int j = 0; j < existing_tags_list.length(); j++) {
                            try {
                                tag_list.add(existing_tags_list.getString(j)); // ✅ plain string
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
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
                    if (matterModel.getDocuments() != null) {
                        try {
                            existing_documents = matterModel.getDocuments();
                            for (int d = 0; d < existing_documents.length(); d++) {
                                DocumentsModel documentsModel = new DocumentsModel();
                                JSONObject jsonObject = existing_documents.getJSONObject(d);
                                documentsModel.setDocid(jsonObject.getString("docid"));
                                documentsModel.setName(jsonObject.getString("name"));
                                documentsModel.setUser_id(jsonObject.getString("user_id"));
                                documentsModel.setDoctype(jsonObject.getString("doctype"));
                                selected_documents_list.add(documentsModel);
                            }
                        } catch (JSONException e) {
                            e.fillInStackTrace();
                        }
                    }
                    if (matterModel.getDocuments_list() != null) {
                        try {
                            existing_documents_list = matterModel.getDocuments_list();
                            for (int ed = 0; ed < existing_documents_list.length(); ed++) {
                                DocumentsModel documentsModel = new DocumentsModel();
                                JSONObject jsonObject = existing_documents_list.getJSONObject(ed);
                                documentsModel.setDocid(jsonObject.getString("docid"));
                                documentsModel.setName(jsonObject.getString("name"));
                                documentsModel.setUser_id(jsonObject.getString("user_id"));
                                documentsModel.setDoctype(jsonObject.getString("doctype"));
                                documentsList.add(documentsModel);
                            }
                        } catch (JSONException e) {
                            e.fillInStackTrace();
                        }
                    }
                    Log.d("tm_list..", String.valueOf(selected_groups_list.size() + " ...C." + selected_clients_list.size()) + " ..g." + selected_tm_list.size());
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
                    if (matterModel.getCorp_client_id() != null) {
                        corp_client_id = matterModel.getCorp_client_id();
                    }
                    try {
//                        if (!selected_temp_clients_list.isEmpty()) {
//                    callGroupsWebservice();
//                            loadselectedTempClients();
//                        }
                        if (!selected_clients_list.isEmpty()) {
//                    callClientsWebservice();
                            loadClientsText();
                            loadSelectedClients(new String[selected_clients_list.size()]);
                        }
                        if (!selected_tm_list.isEmpty()) {
//                    callTMWebservice();
                            loadTeamText();
                            loadSelectedTM(new String[selected_tm_list.size()]);
                        }
                        if (!selected_corp_clients_list.isEmpty()) {
                            loadSelectedCorp_Clients();
                        }
                        if (!selected_groups_list.isEmpty()) {
                            loadSelectedGroups(new String[selected_groups_list.size()]);
                            loadGroupsText();
                            GroupsPopup();
                        }
                        updateDisplay();
                    } catch (Exception e) {
                        e.fillInStackTrace();
                    }
                }
            }
            ll_add_clients.setVisibility(View.VISIBLE);
            clients_list_layout.setVisibility(View.VISIBLE);

            if (!selected_groups_list.isEmpty()) {
                ll_add_groups.setVisibility(View.VISIBLE);
            } else if ((!selected_clients_list.isEmpty()) || (!selected_corp_clients_list.isEmpty()) || (!selected_temp_clients_list.isEmpty())) {
                ll_add_groups.setVisibility(View.VISIBLE);
            } else {
                ll_add_groups.setVisibility(View.GONE);
            }
            if (!tmList.isEmpty()) {
                ll_assign_team_members.setVisibility(View.VISIBLE);
            } else {
                ll_assign_team_members.setVisibility(View.GONE);
//                rv_display_upload_tm_docs.setVisibility(View.VISIBLE);
            }
//            clients_list_layout.setVisibility(View.GONE);
            ll_matterDate.setVisibility(View.GONE);
        } else {
            matter_title_tv.setText(Constants.Matter_title);
            ll_add_groups.setVisibility(View.GONE);
            if (Constants.MATTER_TYPE.equals("General")) {
                chosen_matter = "general";
            } else {
                chosen_matter = "legal";
            }
            ll_add_clients.setVisibility(View.VISIBLE);
            clients_list_layout.setVisibility(View.VISIBLE);
//            temp_client_layout.setVisibility(View.VISIBLE);
//            ll_matterDate.setVisibility(View.VISIBLE);
            matter_date.setText(Constants.matterDate);
            ll_assign_team_members.setVisibility(View.VISIBLE);
            ll_save_buttons.setVisibility(View.VISIBLE);
            load_existing_matter();
        }
        ToggleClient();
        if ((Constants.selectedMatterTab.equals("Temp")) || (selected_clients_list.isEmpty()) && (!Constants.selected_temp_clients_list.isEmpty()))
            AddTemp();
    }

    private void CheckEmailAlert() {
        response_email.setVisibility(View.GONE);
        response_cemail.setVisibility(View.GONE);

        String email = Objects.requireNonNull(et_temp_email.getText()).toString().trim();
        String confirmEmail = Objects.requireNonNull(et_temp_confirm_email.getText()).toString().trim();

        // Check email format only if not empty
        if (!email.isEmpty() && !AndroidUtils.isValidEmail(email)) {
            response_email.setVisibility(View.VISIBLE);
            response_email.setText(email_alert);
        }

        // Check confirm email format only if not empty
        if (!confirmEmail.isEmpty() && !AndroidUtils.isValidEmail(confirmEmail)) {
            response_cemail.setVisibility(View.VISIBLE);
            response_cemail.setText(email_alert);
        }

        // Check equality only if both are non-empty and valid emails
        if (!email.isEmpty() && !confirmEmail.isEmpty()
                && AndroidUtils.isValidEmail(email)
                && AndroidUtils.isValidEmail(confirmEmail)
                && !email.equals(confirmEmail)) {
            response_cemail.setVisibility(View.VISIBLE);
            response_cemail.setText(cemail_alert);
        }
    }

    private void AddTemp() {
        img_country_clear.setVisibility(View.GONE);
        img_country_dropdown.setVisibility(View.VISIBLE);
        tv_add_client.setTextColor(getResources().getColor(R.color.black));
        tv_temp_client.setTextColor(getResources().getColor(R.color.white));
        tv_corp_client.setTextColor(getResources().getColor(R.color.black));

        tv_add_client.setBackgroundDrawable(getContext().getDrawable(R.drawable.button_left_round_background));
        tv_temp_client.setBackgroundDrawable(getContext().getDrawable(R.drawable.radiobutton_centre_green_background));
        tv_corp_client.setBackgroundDrawable(getContext().getDrawable(R.drawable.button_right_round_background));
        temp_client_layout.setVisibility(View.VISIBLE);
        et_temp_fname.setText(null);
        et_temp_lname.setText(null);
        et_temp_country.setText(null);
        et_temp_phone.setText(null);
        et_temp_email.setText(null);
        et_temp_confirm_email.setText(null);

//                et_temp_fname.setError(null);
//                et_temp_lname.setError(null);
//                et_temp_country.setError(null);
//                et_temp_email.setError(null);
//                et_temp_confirm_email.setError(null);

        ll_add_clients.setVisibility(View.GONE);
        selected_clients.setVisibility(View.GONE);
        selected_corp_clients.setVisibility(View.GONE);
        client_type = "consumer";
        isclient = false;
        istempclient = true;
        iscorpclient = false;
        Constants.selectedMatterTab = "Temp";
        if (!Constants.selected_temp_clients_list.isEmpty()) {
            selected_temp_clients_list.clear();
            selected_temp_clients_list = Constants.selected_temp_clients_list;
            ll_selected_temp_clients.setVisibility(View.VISIBLE);
            loadselectedTempClients();
//            loadClients();
        } else {
            selected_temp_clients.setVisibility(View.GONE);
            ll_selected_temp_clients.setVisibility(View.GONE);
        }
    }

    private void AddCorporate() {
        tv_add_client.setTextColor(getResources().getColor(R.color.black));
        tv_temp_client.setTextColor(getResources().getColor(R.color.black));
        tv_corp_client.setTextColor(getResources().getColor(R.color.white));

        tv_add_client.setBackgroundDrawable(getContext().getDrawable(R.drawable.button_left_round_background));
        tv_temp_client.setBackgroundDrawable(getContext().getDrawable(R.drawable.radiobutton_centre_background));
        tv_corp_client.setBackgroundDrawable(getContext().getDrawable(R.drawable.button_right_green_round_background));
        temp_client_layout.setVisibility(View.GONE);
        ll_add_clients.setVisibility(View.VISIBLE);
        add_clients.setText(R.string.add_corporate_clients);
        btn_add_clients.setVisibility(View.GONE);
        at_add_corp_clients.setVisibility(View.VISIBLE);
        at_add_clients.setVisibility(View.GONE);
        btn_add_corp_clients.setVisibility(View.VISIBLE);
        selected_clients.setVisibility(View.GONE);
//                selected_corp_clients.setVisibility(View.VISIBLE);
        ll_selected_clients.setVisibility(View.GONE);
        rv_display_upload_client_docs.setVisibility(View.GONE);
        selected_temp_clients.setVisibility(View.GONE);
        isclient = false;
        istempclient = false;
        iscorpclient = true;
        Constants.selectedMatterTab = "Corp";
        if (!selected_corp_clients_list.isEmpty()) {
            ll_selected_corp_clients.setVisibility(View.VISIBLE);
            loadSelectedCorp_Clients();
        } else {
            ll_selected_corp_clients.setVisibility(View.GONE);
        }
        client_type = "corporate";
    }

    private void ToggleClient() {
        if (Constants.create_matter) {
            if (!selected_clients_list.isEmpty() || !Constants.selected_temp_clients_list.isEmpty()) {
                AndroidUtils.ToggleButton(0, tv_corp_client);
                Add_Clients();
            } else {
                AndroidUtils.ToggleButton(1, tv_corp_client);
            }
            if (!selected_corp_clients_list.isEmpty()) {
                AndroidUtils.ToggleButton(0, tv_add_client);
                AndroidUtils.ToggleButton(0, tv_temp_client);
                AddCorporate();
            } else {
                AndroidUtils.ToggleButton(1, tv_add_client);
                AndroidUtils.ToggleButton(1, tv_temp_client);
            }
        }
    }

    private void loadAddClient() {
        tv_add_client.setTextColor(getResources().getColor(R.color.white));
        tv_temp_client.setTextColor(getResources().getColor(R.color.black));
        tv_corp_client.setTextColor(getResources().getColor(R.color.black));

        if (!Constants.create_matter) {
            tv_temp_client.setVisibility(View.GONE);
            tv_corp_client.setVisibility(View.GONE);
            btn_create.setAlpha(0.5f);
            btn_create.setEnabled(false);
            tv_add_client.setVisibility(View.VISIBLE);
            tv_add_client.setBackgroundDrawable(getContext().getDrawable(R.drawable.full_green_background));
        } else {
            tv_add_client.setBackgroundDrawable(getContext().getDrawable(R.drawable.button_left_green_round_background));
            tv_temp_client.setVisibility(View.VISIBLE);
            tv_corp_client.setVisibility(View.VISIBLE);
        }
        tv_temp_client.setBackgroundDrawable(getContext().getDrawable(R.drawable.radiobutton_centre_background));
        tv_corp_client.setBackgroundDrawable(getContext().getDrawable(R.drawable.button_right_round_background));
        temp_client_layout.setVisibility(View.GONE);
        add_clients.setText(R.string.add_clients);
        btn_add_clients.setVisibility(View.VISIBLE);
        btn_add_corp_clients.setVisibility(View.GONE);
        at_add_clients.setVisibility(View.VISIBLE);
        at_add_corp_clients.setVisibility(View.GONE);
//                selected_clients.setVisibility(View.VISIBLE);
        selected_corp_clients.setVisibility(View.GONE);
        rv_display_upload_corp_client_docs.setVisibility(View.GONE);
        ll_selected_corp_clients.setVisibility(View.GONE);
        selected_temp_clients.setVisibility(View.GONE);
        sp_corp_client.setVisibility(View.GONE);
        isclient = true;
        istempclient = false;
        iscorpclient = false;
        Constants.selectedMatterTab = "Client";
        if (!selected_clients_list.isEmpty()) {
            ll_selected_clients.setVisibility(View.VISIBLE);
            loadClientsText();
            loadSelectedClients(new String[selected_clients_list.size()]);
        } else {
            ll_selected_clients.setVisibility(View.GONE);
        }
        if (!Constants.create_matter) {
            if (filteredClientsList.isEmpty() || !selected_corp_clients_list.isEmpty()) {
                ll_add_clients.setVisibility(View.GONE);
                tv_add_client.setVisibility(View.GONE);
            } else {
                ll_add_clients.setVisibility(View.VISIBLE);
            }
        } else {
            ll_add_clients.setVisibility(View.VISIBLE);
        }
        client_type = "consumer";
    }

    private void loadselectedTempClients() {
//        ll_assign_team_members.setVisibility(View.VISIBLE);
//        ll_save_buttons.setVisibility(View.VISIBLE);
        if (selected_temp_clients_list.isEmpty()) {
            selected_temp_clients.setVisibility(View.GONE);
            ll_selected_temp_clients.setVisibility(View.GONE);
        } else {
            ll_selected_temp_clients.setVisibility(View.VISIBLE);
            loadSelectedTemp_Clients();
        }
    }

    private void callClientsWebservice() {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONObject postdata = new JSONObject();
        WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/client/all/list", "Clients", postdata.toString());
    }

//    @Override
//    public void onClick(View v) {
//        if (v.getId() == R.id.btn_create) {
//            cv_client_details.setVisibility(View.VISIBLE);
//            cv_details.setVisibility(View.VISIBLE);
//            if (!Constants.create_matter) {
//                try {
//                    update_matter();
//                } catch (Exception e) {
//                    e.fillInStackTrace();
//                }
//            } else {
//
//                saveGCTinformation();
//            }

    /// /
    /// /                ll_add_clients.setVisibility(View.VISIBLE);
    /// /                ll_assign_team_members.setVisibility(View.VISIBLE);
    /// /                ll_save_buttons.setVisibility(View.VISIBLE);
//        }
//    }
    private void Alert(boolean is_entity) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.delete_relationship, null);
            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
            ImageView close_documents = view.findViewById(R.id.close_documents);

            tv_confirmation.setText("Data will not be saved. Do you want to proceed");
            AppCompatButton bt_yes = view.findViewById(R.id.btn_yes);
            AppCompatButton btn_no = view.findViewById(R.id.btn_No);
//            bt_yes.setText(R.string.copy);
//            btn_no.setText(R.string.cancel);
            close_documents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ad_dialog_copy.dismiss();
                }
            });
            btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ad_dialog_copy.dismiss();
                }
            });
            bt_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    et_temp_fname.setText(null);
                    et_temp_lname.setText(null);
                    et_temp_country.setText(null);
                    et_temp_phone.setText(null);
                    et_temp_email.setText(null);
                    et_temp_confirm_email.setText(null);

//                    et_temp_fname.setError(null);
//                    et_temp_lname.setError(null);
//                    et_temp_country.setError(null);
//                    et_temp_email.setError(null);
//                    et_temp_confirm_email.setError(null);
                    if (is_entity) {
                        tv_temp_individual.setTextColor(getResources().getColor(R.color.black));
                        tv_temp_entity.setTextColor(getResources().getColor(R.color.white));
                        client_type = "entity";
                        tv_temp_individual.setBackgroundDrawable(getContext().getDrawable(R.drawable.button_left_round_background));
                        tv_temp_entity.setBackgroundDrawable(getContext().getDrawable(R.drawable.button_right_green_round_background));
                        tv_temp_fname.setText(R.string.firm_name);
                        et_temp_fname.setHint(R.string.firm_name);
                        tv_temp_lname.setText(R.string.contact_person);
                        et_temp_lname.setHint(R.string.contact_person);
                    } else {
                        tv_temp_individual.setTextColor(getResources().getColor(R.color.white));
                        tv_temp_entity.setTextColor(getResources().getColor(R.color.black));
                        client_type = "consumer";
                        tv_temp_individual.setBackgroundDrawable(getContext().getDrawable(R.drawable.button_left_green_round_background));
                        tv_temp_entity.setBackgroundDrawable(getContext().getDrawable(R.drawable.button_right_round_background));
                        tv_temp_lname.setText(R.string.last_name);
                        et_temp_lname.setHint(R.string.last_name);
                        tv_temp_fname.setText(R.string.first_name);
                        et_temp_fname.setHint(R.string.first_name);
                    }

                    ad_dialog_copy.dismiss();
                }
            });
            final AlertDialog dialog = dialogBuilder.create();
            ad_dialog_copy = dialog;
            dialog.setView(view);
            dialog.show();
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }


    private void saveGCTinformation() {
        if (selected_groups_list.isEmpty()) {
            AndroidUtils.showAlert("Please select atleast one group", getActivity());
        } else if (selected_clients_list.isEmpty() && selected_corp_clients_list.isEmpty() && Constants.selected_temp_clients_list.isEmpty()) {
            AndroidUtils.showAlert("Please check the add client", getActivity());
        } else {
            try {
                JSONArray clients = new JSONArray();
                JSONArray corp_clients = new JSONArray();
                JSONArray temp_clients = new JSONArray();
                JSONArray group_acls = new JSONArray();
                JSONArray members = new JSONArray();
                JSONArray new_groups_list = new JSONArray();
                JSONArray new_clients_list = new JSONArray();
                JSONArray new_tm_list = new JSONArray();
                JSONArray documents = new JSONArray();
                JSONArray new_documents_list = new JSONArray();
//                JSONArray advocates_list = new JSONArray();

                for (int i = 0; i < selected_groups_list.size(); i++) {
                    try {
                        GroupsModel groupsModel = selected_groups_list.get(i);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", groupsModel.getGroup_id());
                        jsonObject.put("name", groupsModel.getGroup_name());
                        jsonObject.put("isChecked", groupsModel.isChecked());
                        group_acls.put(jsonObject);
                    } catch (Exception e) {
                        e.fillInStackTrace();
                    }
                }
                for (int i = 0; i < groupsList.size(); i++) {
                    try {
                        GroupsModel groupsModel = groupsList.get(i);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", groupsModel.getGroup_id());
                        jsonObject.put("name", groupsModel.getGroup_name());
                        new_groups_list.put(jsonObject);
                    } catch (JSONException e) {
                        e.fillInStackTrace();
                    }
                }
                for (int i = 0; i < selected_clients_list.size(); i++) {
                    try {
                        ClientsModel clientsModel = selected_clients_list.get(i);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", clientsModel.getClient_id());
                        jsonObject.put("type", clientsModel.getClient_type());
                        jsonObject.put("name", clientsModel.getClient_name());
                        clients.put(jsonObject);
                    } catch (Exception e) {
                        e.fillInStackTrace();
                    }
                }
                for (int i = 0; i < clientsList.size(); i++) {
                    ClientsModel clientsModel = clientsList.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", clientsModel.getClient_id());
                    jsonObject.put("type", clientsModel.getClient_type());
                    jsonObject.put("name", clientsModel.getClient_name());
                    new_clients_list.put(jsonObject);
                }
                for (int i = 0; i < selected_tm_list.size(); i++) {
                    try {
                        TeamModel teamModel = selected_tm_list.get(i);
                        JSONObject team_object = new JSONObject();
                        team_object.put("id", teamModel.getTm_id());
                        team_object.put("name", teamModel.getTm_name());
                        team_object.put("user_id", teamModel.getUser_id());
//                        team_object.put("")
                        members.put(team_object);
                    } catch (Exception e) {
                        e.fillInStackTrace();
                        e.fillInStackTrace();
                    }
                }
                for (int i = 0; i < tmList.size(); i++) {
                    try {
                        TeamModel teamModel = tmList.get(i);
                        JSONObject team_object = new JSONObject();
                        team_object.put("id", teamModel.getTm_id());
                        team_object.put("name", teamModel.getTm_name());
                        team_object.put("user_id", teamModel.getUser_id());
//                        team_object.put("")
                        new_tm_list.put(team_object);
                    } catch (Exception e) {
                        e.fillInStackTrace();
                    }
                }
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
                for (int d = 0; d < selected_documents_list.size(); d++) {
                    DocumentsModel documentsModel = selected_documents_list.get(d);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("docid", documentsModel.getDocid());
                    jsonObject.put("doctype", documentsModel.getDoctype());
                    jsonObject.put("user_id", documentsModel.getUser_id());
                    jsonObject.put("name", documentsModel.getName());
                    documents.put(jsonObject);
                }
                for (int e = 0; e < documentsList.size(); e++) {
                    DocumentsModel documentsModel = documentsList.get(e);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("docid", documentsModel.getDocid());
                    jsonObject.put("doctype", documentsModel.getDoctype());
                    jsonObject.put("user_id", documentsModel.getUser_id());
                    jsonObject.put("name", documentsModel.getName());
                    new_documents_list.put(jsonObject);
                }
                corp_client_id = "";
                for (int i = 0; i < selected_corp_clients_list.size(); i++) {
                    try {
                        ClientsModel clientsModel = selected_corp_clients_list.get(i);
                        corp_client_id = clientsModel.getClient_id();
                    } catch (Exception e) {
                        e.fillInStackTrace();
                    }
                }
                for (int i = 0; i < selected_corp_clients_list.size(); i++) {
                    try {
                        ClientsModel clientsModel = selected_corp_clients_list.get(i);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", clientsModel.getClient_id());
                        jsonObject.put("type", clientsModel.getClient_type());
                        jsonObject.put("name", clientsModel.getClient_name());
                        corp_clients.put(jsonObject);
                    } catch (Exception e) {
                        e.fillInStackTrace();
                    }
                }
                for (int i = 0; i < selected_temp_clients_list.size(); i++) {
                    try {
                        ClientsModel clientsModel = selected_temp_clients_list.get(i);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", clientsModel.getClient_id());
                        jsonObject.put("type", clientsModel.getClient_type());
                        jsonObject.put("rel_id", clientsModel.getRel_id());
                        jsonObject.put("name", clientsModel.getClient_name());
                        temp_clients.put(jsonObject);
                    } catch (Exception e) {
                        e.fillInStackTrace();
                    }
                }
                matterModel.setCorp_client_id(corp_client_id);
                matterModel.setMatter_title(matter_title);
                matterModel.setCase_number(case_number);
                matterModel.setCase_type(case_type);
                matterModel.setDescription(description);
                matterModel.setDate_of_filing(dof);
                matterModel.setStart_date(start_date);
                matterModel.setEnd_date(end_date);
                matterModel.setCourt(court);
                matterModel.setJudge(judge);
                matterModel.setCase_priority(case_priority);
                matterModel.setStatus(case_status);
                matterModel.setCorp_clients_list(corp_clients);
                matterModel.setTemp_clients_list(temp_clients);
                matterModel.setClients(clients);
                matterModel.setGroup_acls(group_acls);
                matterModel.setMembers(members);
                matterModel.setGroups_list(new_groups_list);
                matterModel.setClients_list(new_clients_list);
                matterModel.setMembers_list(new_tm_list);
                matterModel.setOpponent_advocate(jsonArray);
                matterModel.setDocuments(documents);
                matterModel.setDocuments_list(new_documents_list);
                matterArraylist.set(0, matterModel);
                matter.loadDocuments();
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        }
    }

    private void AddGctDetails() {
//        if (selected_groups_list.isEmpty()) {
//            AndroidUtils.showToast("Please select atleast one group", getContext());
//        } else if (selected_clients_list.isEmpty() && selected_corp_clients_list.isEmpty() && selected_temp_clients_list.isEmpty()) {
//            AndroidUtils.showAlert("Please check the add client", getActivity());
//        } else {
        try {
            JSONArray clients = new JSONArray();
            JSONArray corp_clients = new JSONArray();
            JSONArray temp_clients = new JSONArray();
            JSONArray group_acls = new JSONArray();
            JSONArray members = new JSONArray();
            JSONArray new_groups_list = new JSONArray();
            JSONArray new_clients_list = new JSONArray();
            JSONArray new_tm_list = new JSONArray();
            JSONArray documents = new JSONArray();
            JSONArray new_documents_list = new JSONArray();
//                JSONArray advocates_list = new JSONArray();

            for (int i = 0; i < selected_groups_list.size(); i++) {
                try {
                    GroupsModel groupsModel = selected_groups_list.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", groupsModel.getGroup_id());
                    jsonObject.put("name", groupsModel.getGroup_name());
                    jsonObject.put("isChecked", groupsModel.isChecked());
                    group_acls.put(jsonObject);
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            }
            for (int i = 0; i < groupsList.size(); i++) {
                try {
                    GroupsModel groupsModel = groupsList.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", groupsModel.getGroup_id());
                    jsonObject.put("name", groupsModel.getGroup_name());
                    new_groups_list.put(jsonObject);
                } catch (JSONException e) {
                    e.fillInStackTrace();
                }
            }
            for (int i = 0; i < selected_clients_list.size(); i++) {
                try {
                    ClientsModel clientsModel = selected_clients_list.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", clientsModel.getClient_id());
                    jsonObject.put("type", clientsModel.getClient_type());
                    jsonObject.put("name", clientsModel.getClient_name());
                    clients.put(jsonObject);
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            }
            for (int i = 0; i < clientsList.size(); i++) {
                ClientsModel clientsModel = clientsList.get(i);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", clientsModel.getClient_id());
                jsonObject.put("type", clientsModel.getClient_type());
                jsonObject.put("name", clientsModel.getClient_name());
                new_clients_list.put(jsonObject);
            }
            for (int i = 0; i < selected_tm_list.size(); i++) {
                try {
                    TeamModel teamModel = selected_tm_list.get(i);
                    JSONObject team_object = new JSONObject();
                    team_object.put("id", teamModel.getTm_id());
                    team_object.put("name", teamModel.getTm_name());
                    team_object.put("user_id", teamModel.getUser_id());
//                        team_object.put("")
                    members.put(team_object);
                } catch (Exception e) {
                    e.fillInStackTrace();
                    e.fillInStackTrace();
                }
            }
            for (int i = 0; i < tmList.size(); i++) {
                try {
                    TeamModel teamModel = tmList.get(i);
                    JSONObject team_object = new JSONObject();
                    team_object.put("id", teamModel.getTm_id());
                    team_object.put("name", teamModel.getTm_name());
                    team_object.put("user_id", teamModel.getUser_id());
//                        team_object.put("")
                    new_tm_list.put(team_object);
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            }
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
//            try {
//                for (int i = 0; i < tag_list.size(); i++) {
//                    JSONObject tagsObject = new JSONObject();
//                    tagsObject.put(String.valueOf(i), tag_list.get(i));
//                    existing_tags_list.put(tagsObject);
//                }
//            } catch (JSONException e) {
//                e.fillInStackTrace();
//            }
            for (int d = 0; d < selected_documents_list.size(); d++) {
                DocumentsModel documentsModel = selected_documents_list.get(d);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("docid", documentsModel.getDocid());
                jsonObject.put("doctype", documentsModel.getDoctype());
                jsonObject.put("user_id", documentsModel.getUser_id());
                jsonObject.put("name", documentsModel.getName());
                documents.put(jsonObject);
            }
            for (int e = 0; e < documentsList.size(); e++) {
                DocumentsModel documentsModel = documentsList.get(e);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("docid", documentsModel.getDocid());
                jsonObject.put("doctype", documentsModel.getDoctype());
                jsonObject.put("user_id", documentsModel.getUser_id());
                jsonObject.put("name", documentsModel.getName());
                new_documents_list.put(jsonObject);
            }
            corp_client_id = "";
            for (int i = 0; i < selected_corp_clients_list.size(); i++) {
                try {
                    ClientsModel clientsModel = selected_corp_clients_list.get(i);
                    corp_client_id = clientsModel.getClient_id();
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            }
            for (int i = 0; i < selected_corp_clients_list.size(); i++) {
                try {
                    ClientsModel clientsModel = selected_corp_clients_list.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", clientsModel.getClient_id());
                    jsonObject.put("type", clientsModel.getClient_type());
                    jsonObject.put("name", clientsModel.getClient_name());
                    corp_clients.put(jsonObject);
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            }
            for (int i = 0; i < selected_temp_clients_list.size(); i++) {
                try {
                    ClientsModel clientsModel = selected_temp_clients_list.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", clientsModel.getClient_id());
                    jsonObject.put("type", clientsModel.getClient_type());
                    jsonObject.put("rel_id", clientsModel.getRel_id());
                    jsonObject.put("name", clientsModel.getClient_name());
                    temp_clients.put(jsonObject);
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            }
            matterModel.setCorp_client_id(corp_client_id);
            matterModel.setMatter_title(matter_title);
            matterModel.setCase_number(case_number);
            matterModel.setCase_type(case_type);
            matterModel.setDescription(description);
            matterModel.setDate_of_filing(dof);
            matterModel.setStart_date(start_date);
            matterModel.setEnd_date(end_date);
            matterModel.setCourt(court);
            matterModel.setJudge(judge);
            matterModel.setCase_priority(case_priority);
            matterModel.setStatus(case_status);
            matterModel.setCorp_clients_list(corp_clients);
            matterModel.setTemp_clients_list(temp_clients);
            matterModel.setClients(clients);
            matterModel.setGroup_acls(group_acls);
            matterModel.setMembers(members);
            matterModel.setGroups_list(new_groups_list);
            matterModel.setClients_list(new_clients_list);
            matterModel.setMembers_list(new_tm_list);
            matterModel.setOpponent_advocate(jsonArray);
            existing_tags_list = new JSONArray();
            for (int i = 0; i < tag_list.size(); i++) {
                existing_tags_list.put(tag_list.get(i));  // no JSONObject
            }
            matterModel.setTags_list(existing_tags_list);
            matterModel.setDocuments(documents);
            matterModel.setDocuments_list(new_documents_list);
            matterArraylist.set(0, matterModel);
//                matter.loadDocuments();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
//        }
    }

    private void TeamPopUp() {
        try {
            ll_assigned_team_members.setVisibility(View.VISIBLE);
            // Ensure all members are unchecked initially
            for (int i = 0; i < tmList.size(); i++) {
                TeamModel teamModel = tmList.get(i);
                teamModel.setChecked(false);  // Uncheck all team members by default
            }

            // Re-check only the selected team members
            for (int i = 0; i < tmList.size(); i++) {
                for (int j = 0; j < temporary_tm_list.size(); j++) {
                    if (tmList.get(i).getTm_id().equals(temporary_tm_list.get(j).getTm_id())) {
                        TeamModel teamModel = tmList.get(i);
                        teamModel.setChecked(true);
                    }
                }
            }
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            rv_display_upload_tm_docs.setLayoutManager(layoutManager);
            // rv_display_upload_tm_docs.setHasFixedSize(true);
            ADAPTER_TAG = "TM";
            GroupsAdapter documentsAdapter = new GroupsAdapter(groupsList, clientsList, tmList, new_groupsList, ADAPTER_TAG, this);
            rv_display_upload_tm_docs.setAdapter(documentsAdapter);
            teamsAdapter = documentsAdapter;
            AndroidUtils.LoadList(rv_display_upload_tm_docs, getContext(), tmList.size(), false);
//            ButtonStatus(selected_tm_list.size(), btn_add_teammembers);
            btn_add_teammembers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selected_tm_list.clear();
                    selected_tm_list.addAll(temporary_tm_list);
                    ischecked_tm = true;
                    rv_display_upload_tm_docs.setVisibility(View.GONE);
                    Add_Teams();
                    loadTeam();
                }
            });
        } catch (Exception e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    public void Add_Teams() {
        //..
//        Set<TeamModel> selected_groups_set = new HashSet<TeamModel>();
//        for (int i = 0; i < teamsAdapter.getTmList().size(); i++) {
//            TeamModel teamModel = teamsAdapter.getTmList().get(i);
//            if (teamModel.isChecked()) {
//                //                           jsonArray.put(selected_documents_list.get(i).getGroup_name());
//                selected_groups_set.add(teamModel);
//            }
//        }
//        selected_tm_list.clear();
        if (!Constants.create_matter) {
            btn_create.setAlpha(1.0f);
            btn_create.setEnabled(true);
//            selected_tm_list.addAll(owner_tm);
        }
//        selected_tm_list.addAll(selected_groups_set);
        loadTeamText();
        //..
    }

    public void loadTeam() {
        if (!Constants.create_matter) {
            boolean ownerExists = false;
            for (TeamModel model : selected_tm_list) {
                if (model.getTm_id().equals(Constants.owner_id)) {
                    ownerExists = true;
                    break;
                }
            }

            // Add owner to 0th position if not present
            if (!ownerExists && owner_tm != null && !owner_tm.isEmpty()) {
                selected_tm_list.add(0, owner_tm.get(0)); // assuming owner_tm holds exactly one owner model
            }
        }

        if (selected_tm_list.isEmpty()) {
            at_assigned_team_members.setText("");
            selected_tm.setVisibility(View.GONE);
        } else {
            loadSelectedTM(new String[selected_tm_list.size()]);
        }

        AddGctDetails();
    }


    public void callTMWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            JSONArray clients = new JSONArray();
            JSONArray group_acls = new JSONArray();
            if (!selected_temp_clients_list.isEmpty() && selected_clients_list.isEmpty() && selected_corp_clients_list.isEmpty()) {
                for (int i = 0; i < selected_groups_list.size(); i++) {
                    GroupsModel groupsModel = selected_groups_list.get(i);
                    group_acls.put(groupsModel.getGroup_id());
                }
            } else {
                if (!selected_clients_list.isEmpty()) {
                    for (int i = 0; i < selected_clients_list.size(); i++) {
                        ClientsModel clientsModel = selected_clients_list.get(i);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", clientsModel.getClient_id());
                        jsonObject.put("type", clientsModel.getClient_type());
                        clients.put(jsonObject);
                    }
                }
                if (!selected_corp_clients_list.isEmpty()) {
                    for (int i = 0; i < selected_corp_clients_list.size(); i++) {
                        ClientsModel clientsModel = selected_corp_clients_list.get(i);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", clientsModel.getClient_id());
                        jsonObject.put("type", "corporate");
                        clients.put(jsonObject);
                    }
                }
                if (!selected_temp_clients_list.isEmpty()) {
                    for (int i = 0; i < selected_temp_clients_list.size(); i++) {
                        ClientsModel clientsModel = selected_temp_clients_list.get(i);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", clientsModel.getClient_id());
                        jsonObject.put("type", "consumer");
                        clients.put(jsonObject);
                    }
                }
                for (int i = 0; i < selected_groups_list.size(); i++) {
                    GroupsModel groupsModel = selected_groups_list.get(i);
                    group_acls.put(groupsModel.getGroup_id());
                }
            }
            postdata.put("clients", clients);
            postdata.put("group_acls", group_acls);
            postdata.put("attachment_type", "members");
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "matter/attachments", "Members", postdata.toString());
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    private void callGroupsWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            JSONArray clients = new JSONArray();
            if (!selected_clients_list.isEmpty()) {
                for (int i = 0; i < selected_clients_list.size(); i++) {
                    ClientsModel clientsModel = selected_clients_list.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", clientsModel.getClient_id());
                    jsonObject.put("type", clientsModel.getClient_type());
                    clients.put(jsonObject);
                }
            }
            if (!selected_corp_clients_list.isEmpty()) {
                for (int i = 0; i < selected_corp_clients_list.size(); i++) {
                    ClientsModel clientsModel = selected_corp_clients_list.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", clientsModel.getClient_id());
                    jsonObject.put("type", "corporate");
                    clients.put(jsonObject);
                }
            }
            if (!selected_temp_clients_list.isEmpty()) {
                for (int i = 0; i < selected_temp_clients_list.size(); i++) {
                    ClientsModel clientsModel = selected_temp_clients_list.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", clientsModel.getClient_id());
                    jsonObject.put("type", "consumer");
                    clients.put(jsonObject);
                }
            }
            postdata.put("clients", clients);
            postdata.put("attachment_type", "groups");
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "matter/attachments", "Groups", postdata.toString());
        } catch (JSONException e) {
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
                if (httpResult.getRequestType().equals("Groups")) {
                    is_error = result.getBoolean("error");
                    if (!is_error) {
                        JSONArray groups = result.getJSONArray("groups");
                        JSONArray clientGroups = result.optJSONArray("client_groups"); // may not always exist
                        loadGroupsData(groups, clientGroups);
                        if (groupsList.isEmpty())
                            ll_add_groups.setVisibility(View.GONE);
                    } else {
                        AndroidUtils.showAlert(result.optString("msg"), getActivity());
                        if (groupsList.isEmpty())
                            ll_add_groups.setVisibility(View.GONE);
                    }
                } else if (httpResult.getRequestType().equals("chosen_member")) {
                    String id = result.getString("id");
//                    AndroidUtils.showAlert("Error_value.." + id, getContext());
                    JSONArray members = result.getJSONArray("members");
                    JSONArray clients = result.getJSONArray("clients");
                    JSONArray corp_clients = result.getJSONArray("corporate");
                    display_existing_members(members, clients, corp_clients);
                    try {
                        load_existing_member_list();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else if (httpResult.getRequestType().equals("attachment_members")) {
                    is_error = result.getBoolean("error");
                    attachment_type = result.getString("attachment_type");
                    JSONArray members = result.getJSONArray("members");
                    loadMembers(members);
//                    AndroidUtils.showAlert("Att_value.." + attachment_type, getContext());
                    load_existing_clients_list();
                } else if (httpResult.getRequestType().equals("attachment_corp_clients")) {
                    is_error = result.getBoolean("error");
//                    attachment_type = result.getString("attachment_type");
//                    JSONArray corporate = result.getJSONArray("corporate");
//                    JSONObject data = result.getJSONObject("data");
                    JSONArray corporate = result.getJSONArray("relationships");
                    load_Corp_clients(corporate);
//                    AndroidUtils.showAlert("Att_value.." + attachment_type, getContext());
                } else if (httpResult.getRequestType().equals("countries")) {
                    is_error = result.getBoolean("error");
                    JSONArray jsonArray = (new JSONObject(result.getString("data"))).getJSONArray("countries");
                    CountriesDO countriesDO;
                    countriesList.clear();
                    for (int i = 1; i < jsonArray.length(); i++) {
                        countriesDO = new CountriesDO();
                        countriesDO.setName(String.valueOf(jsonArray.getJSONArray(i).get(1)));
                        countriesDO.setValue(String.valueOf(jsonArray.getJSONArray(i).get(0)));
                        countriesList.add(countriesDO);
                    }
                    load_countries();
                } else if (httpResult.getRequestType().equals("temp_client")) {
//                    "msg": "Temporary account created successfully.",
//                            "createdId": "66449261fffd8f7678dad416",
//                            "name": "others test 2 hhhh"
                    if (result.has("errors")) {
                        JSONArray errors = result.getJSONArray("errors");
                        String error_msg = "";
                        for (int i = 0; i < errors.length(); i++) {
                            JSONObject error = errors.getJSONObject(i);
                            error_msg = error.getString("msg");
                        }
                        AndroidUtils.showAlert(error_msg, getActivity());
                    } else {
                        success_msg = result.getString("msg");
                        temp_client_id = result.getString("createdId");
                        temp_rel_id = result.optString("rel_id");
                        temp_name = result.getString("name");
                        AndroidUtils.showAlert(success_msg, getActivity());

//                        selected_temp_clients_list.clear();
                        ClientsModel clientsModel = new ClientsModel();
                        clientsModel.setClient_id(temp_client_id);
                        clientsModel.setRel_id(temp_rel_id);
                        clientsModel.setClient_name(temp_name);
                        clientsModel.setClient_type(client_type);
                        selected_temp_clients_list.add(clientsModel);
                        Constants.selected_temp_clients_list.add(clientsModel);
                        ToggleClient();
//                        selected_documents_list.clear();
                        documentsList.clear();
                        JSONArray temp_clients = new JSONArray();
                        for (int i = 0; i < selected_temp_clients_list.size(); i++) {
                            try {
                                ClientsModel clientsModel1 = selected_temp_clients_list.get(i);
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("id", clientsModel1.getClient_id());
                                jsonObject.put("type", clientsModel1.getClient_type());
                                jsonObject.put("rel_id", clientsModel1.getRel_id());
                                jsonObject.put("name", clientsModel1.getClient_name());
                                temp_clients.put(jsonObject);
                            } catch (Exception e) {
                                e.fillInStackTrace();
                            }
                        }
                        matterModel.setTemp_clients_list(temp_clients);
                        matterArraylist.set(0, matterModel);
                        et_temp_fname.setText(null);
                        et_temp_lname.setText(null);
                        et_temp_country.setText(null);
                        et_temp_email.setText(null);
                        et_temp_phone.setText(null);
                        et_temp_confirm_email.setText(null);

                        et_temp_fname.setError(null);
                        et_temp_lname.setError(null);
                        et_temp_country.setError(null);
//                        et_temp_email.setError(null);
//                        et_temp_confirm_email.setError(null);
//                        if (!selected_clients_list.contains(selected_temp_clients_list)) {
//                            selected_clients_list.addAll(selected_temp_clients_list);
//                        }
//                        loadSelectedClients();
                        loadselectedTempClients();
                        loadClients();
                    }
//                    AndroidUtils.showAlert("Att_value.." + attachment_type, getContext());
                } else if (httpResult.getRequestType().equals("attachment_clients")) {
                    is_error = result.getBoolean("error");
                    if (is_error) {
                        if (clientsList.isEmpty()) {
                            callClientsWebservice();
                        }
                    } else {
                        attachment_type = result.getString("attachment_type");
                        JSONArray members = result.getJSONArray("clients");
                        loadClientsList(members);
                    }
//                    call_corporate_clients();
//                    AndroidUtils.showAlert("Att_value.." + attachment_type, getContext());
                } else if (httpResult.getRequestType().equals("matter_update")) {
                    is_error = result.getBoolean("error");
                    String msg = result.getString("msg");
                    AndroidUtils.showAlert(msg, getActivity());
                    matter.loadViewUI();
                } else if (httpResult.getRequestType().equals("Clients")) {
                    JSONObject data = result.getJSONObject("data");
                    JSONArray jsonArray = data.getJSONArray("relationships");
                    loadClientsList(jsonArray);
                } else if (httpResult.getRequestType().equals("Members")) {
                    JSONArray members = result.getJSONArray("members");
                    loadMembers(members);
                }
            } catch (JSONException e) {
                e.fillInStackTrace();
            }
        }
    }

    private void load_Corp_clients(JSONArray corp_clients) {
        try {
            corp_clients_list.clear();
            for (int i = 0; i < corp_clients.length(); i++) {
                JSONObject jsonObject = corp_clients.getJSONObject(i);
                ClientsModel clientsModel = new ClientsModel();
                clientsModel.setClient_id(jsonObject.getString("id"));
                clientsModel.setClient_name(jsonObject.getString("name"));
                clientsModel.setClient_type(jsonObject.getString("type"));
                corp_clients_list.add(clientsModel);
            }
            if (corp_clients_list.isEmpty()) {
                //ll_add_clients.setVisibility(View.GONE);
                sp_corp_client.setVisibility(View.GONE);
            } else {
                // ll_add_clients.setVisibility(View.VISIBLE);
                sp_corp_client.setVisibility(View.VISIBLE);
            }

            selcted_corp_Clients = new boolean[corp_clients_list.size()];

//            if (!Constants.create_matter) {
//                sp_corp_client.setVisibility(View.GONE);
//            }
            corp_clients_popup();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void loadMembers(JSONArray members) {
        try {
            tmList.clear();

            for (int i = 0; i < members.length(); i++) {
                JSONObject jsonObject = members.getJSONObject(i);
                TeamModel teamModel = new TeamModel();
                teamModel.setTm_id(jsonObject.getString("id"));
                teamModel.setTm_name(jsonObject.getString("name"));
                teamModel.setUser_id(jsonObject.getString("user_id"));

                if (!Constants.create_matter) {
                    // Skip adding owner from API list in edit mode
                    if ((!jsonObject.getString("id").equals(Constants.owner_id)) && (!jsonObject.getString("id").equals(Constants.USER_ID))) {
                        tmList.add(teamModel);
                    }
                } else {
                    // In create mode, skip adding current logged-in user
                    if (!jsonObject.getString("id").equals(Constants.USER_ID)) {
                        tmList.add(teamModel);
                    }
                }
            }

            if (tmList.isEmpty()) {
                ll_assign_team_members.setVisibility(View.GONE);
                rv_display_upload_tm_docs.setVisibility(View.GONE);
            } else {
                ll_assign_team_members.setVisibility(View.VISIBLE);
            }

            if (!Constants.create_matter) {
                rv_display_upload_tm_docs.setVisibility(View.GONE);
            }

            if (!tmList.isEmpty()) {
                selectedTM = new boolean[tmList.size()];

                for (int i = selected_tm_list.size() - 1; i >= 0; i--) {
                    TeamModel selectedGroup = selected_tm_list.get(i);
                    boolean existsInTms = false;

                    for (int j = 0; j < tmList.size(); j++) {
                        if (selectedGroup.getTm_id().equals(tmList.get(j).getTm_id())) {
                            tmList.get(j).setChecked(true);
                            existsInTms = true;
                            break;
                        }
                    }

                    if (!existsInTms) {
                        selected_tm_list.remove(i);
                        temporary_tm_list.remove(i);
                    }
                }
            }

            loadSelectedTM(new String[selected_tm_list.size()]);
            loadTeamText();
            TeamPopUp();

            if (!Constants.create_matter) {
                // Prepare owner model
                owner_tm.clear();
                TeamModel teamModel1 = new TeamModel();
                teamModel1.setTm_id(Constants.owner_id);
                teamModel1.setTm_name(Constants.owner_name);
                owner_tm.add(teamModel1);

                // Only add owner if not already in list
                boolean ownerExists = false;
                for (TeamModel tm : selected_tm_list) {
                    if (tm.getTm_id().equals(Constants.owner_id)) {
                        ownerExists = true;
                        break;
                    }
                }
                if (!ownerExists) {
                    temporary_tm_list.add(0, owner_tm.get(0));
                    selected_tm_list.add(0, owner_tm.get(0));
                }

                Add_Teams();
                loadTeam();
                btn_create.setEnabled(false);
                btn_create.setAlpha(0.5f);
            }

        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }


    private void loadClientsList(JSONArray clients) {
        try {
            clientsList.clear();
            for (int i = 0; i < clients.length(); i++) {
                JSONObject jsonObject = clients.getJSONObject(i);
                ClientsModel clientsModel = new ClientsModel();
                clientsModel.setClient_id(jsonObject.getString("id"));
                clientsModel.setClient_name(jsonObject.getString("name"));
                clientsModel.setClient_type(jsonObject.getString("type"));
                clientsList.add(clientsModel);
            }
            if (clientsList.isEmpty()) {
                //ll_add_clients.setVisibility(View.GONE);
                rv_display_upload_client_docs.setVisibility(View.GONE);
            } else {
                // ll_add_clients.setVisibility(View.VISIBLE);
                rv_display_upload_client_docs.setVisibility(View.VISIBLE);
            }

            selectedClients = new boolean[clientsList.size()];

            if (!Constants.create_matter) {
                rv_display_upload_client_docs.setVisibility(View.GONE);
            }
            ClientssPopUp();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void corp_clients_popup() {
        ll_add_clients.setVisibility(View.VISIBLE);
        clients_list_layout.setVisibility(View.VISIBLE);
//        temp_client_layout.setVisibility(View.VISIBLE);
        try {
//                for (int i = 0; i < corp_clients_list.size(); i++) {
//                    for (int j = 0; j < selected_corp_clients_list.size(); j++) {
//                        if (corp_clients_list.get(i).getClient_id().matches(selected_corp_clients_list.get(j).getClient_id())) {
//                            ClientsModel clientsModel = corp_clients_list.get(i);
//                            clientsModel.setChecked(true);
////                        selected_groups_list.set(j,documentsModel);
//                        }
//                    }
//                }
//            for (int i = 0; i < corp_clients_list.size(); i++) {
//                for (int j = 0; j < selected_corp_clients_list.size(); j++) {
//                    if (corp_clients_list.get(i).getClient_id().matches(selected_corp_clients_list.get(j).getClient_id())) {
//                        ClientsModel clientsModel = corp_clients_list.get(i);
//                        clientsModel.setChecked(true);
////                        selected_groups_list.set(j,documentsModel);
//                    }
//                }
//            }

            corp_clients_name.clear();
            for (int i = 0; i < corp_clients_list.size(); i++) {
                ClientsModel clientsModel = corp_clients_list.get(i);
                corp_clients_name.add(clientsModel.getClient_name());
            }
            CommonSpinnerAdapter adapter = new CommonSpinnerAdapter(getActivity(), corp_clients_name);
            sp_corp_client.setAdapter(adapter);
            AndroidUtils.LoadList(sp_corp_client, getContext(), corp_clients_name.size(), true);
            btn_add_corp_clients.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selected_corp_clients_list.clear();
                    selected_corp_clients_list.addAll(temporary_corpclients_list);
//                    selected_documents_list.clear();
                    groupsList.clear();
                    tmList.clear();
//                selected_groups_list.clear();
//                temporary_groups_list.clear();
//                temporary_tm_list.clear();
//                selected_tm_list.clear();
                    at_add_groups.setText("");
                    at_assigned_team_members.setText("");
                    selected_tm.setVisibility(View.GONE);
                    selected_groups.setVisibility(View.GONE);
                    loadselected_corp_list();
                    AddGctDetails();
                    documentsList.clear();
                    loadClients();
//                    loadSelectedGroups();
                }
            });
        } catch (Exception e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void loadselected_corp_list() {
        rv_display_upload_corp_client_docs.setVisibility(View.GONE);
        if (selected_corp_clients_list.isEmpty()) {
            selected_corp_clients.setVisibility(View.GONE);
            ll_selected_corp_clients.setVisibility(View.GONE);
        } else {
            ll_selected_corp_clients.setVisibility(View.VISIBLE);
            loadSelectedCorp_Clients();
        }
        sp_corp_client.setVisibility(View.GONE);
        ischecked_corp_client = true;
    }

    private void ClientssPopUp() {
        clients_list_layout.setVisibility(View.VISIBLE);
//        temp_client_layout.setVisibility(View.VISIBLE);
        try {
            for (int i = 0; i < clientsList.size(); i++) {
                ClientsModel teamModel = clientsList.get(i);
                teamModel.setChecked(false);  // Uncheck all team members by default
            }

            // Re-check only the selected team members
            for (int i = 0; i < clientsList.size(); i++) {
                for (int j = 0; j < temporary_clients_list.size(); j++) {
                    if (clientsList.get(i).getClient_id().equals(temporary_clients_list.get(j).getClient_id())) {
                        ClientsModel teamModel = clientsList.get(i);
                        teamModel.setChecked(true);
                    }
                }
            }
//            for (int i = 0; i < corp_clients_list.size(); i++) {
//                for (int j = 0; j < selected_corp_clients_list.size(); j++) {
//                    if (corp_clients_list.get(i).getClient_id().matches(selected_corp_clients_list.get(j).getClient_id())) {
//                        ClientsModel clientsModel = corp_clients_list.get(i);
//                        clientsModel.setChecked(true);
////                        selected_groups_list.set(j,documentsModel);
//                    }
//                }
//            }

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            rv_display_upload_client_docs.setLayoutManager(layoutManager);
            //rv_display_upload_client_docs.setHasFixedSize(true);
            ADAPTER_TAG = "Clients";
            GroupsAdapter documentsAdapter;
//            if (client_type.equals("corporate")) {
//                documentsAdapter = new GroupsAdapter(groupsList, corp_clients_list, tmList, new_groupsList, ADAPTER_TAG);
//                rv_display_upload_client_docs.setAdapter(documentsAdapter);
//            } else {
//..
            if (!Constants.create_matter) {
                filteredClientsList.clear();
                // Iterate over clientsList and add only clients that are not in old_clients_list
                for (ClientsModel client : clientsList) {
                    boolean isInOldClientsList = false;

                    // Check if the current client is in old_clients_list
                    for (ClientsModel oldClient : old_clients_list) {
                        if (client.getClient_id().equals(oldClient.getClient_id())) {
                            isInOldClientsList = true;
                            break;
                        }
                    }

                    // If the client is not in old_clients_list, add it to the filteredClientsList
                    if (!isInOldClientsList) {
                        filteredClientsList.add(client);
                    }
                }
                if (filteredClientsList.isEmpty() || !selected_corp_clients_list.isEmpty()) {
                    ll_add_clients.setVisibility(View.GONE);
                } else {
                    ll_add_clients.setVisibility(View.VISIBLE);
                }
                documentsAdapter = new GroupsAdapter(groupsList, filteredClientsList, tmList, new_groupsList, ADAPTER_TAG, this);
            } else {
                ll_add_clients.setVisibility(View.VISIBLE);
                documentsAdapter = new GroupsAdapter(groupsList, clientsList, tmList, new_groupsList, ADAPTER_TAG, this);
            }
            //..
            rv_display_upload_client_docs.setAdapter(documentsAdapter);
            clientsAdapter = documentsAdapter;
            AndroidUtils.LoadList(rv_display_upload_client_docs, getContext(), clientsAdapter.getItemCount(), false);
//            }
//            ButtonStatus(selected_clients_list.size(), btn_add_clients);
        } catch (Exception e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    public void loadClientsText() {
        String[] value = new String[temporary_clients_list.size()];
        for (int i = 0; i < temporary_clients_list.size(); i++) {
//                                value += "," + family_members.get(i);
//                               value.add(family_members.get(i));
            value[i] = temporary_clients_list.get(i).getClient_name();
        }

        String str = String.join(",", value);
        at_add_clients.setText(str);
        AndroidUtils.ToggleButton(temporary_clients_list.size(), btn_add_clients);
    }

    public void loadClients() {
        if (Constants.create_matter) {
            if ((!selected_temp_clients_list.isEmpty()) || (!selected_clients_list.isEmpty()) || (!selected_corp_clients_list.isEmpty())) {
                ll_add_groups.setVisibility(View.VISIBLE);
                AndroidUtils.ToggleButton(temporary_groups_list.size(), btn_add_groups);
                ischecked_group = true;
                rv_display_upload_groups_docs.setVisibility(View.GONE);
//                ll_save_buttons.setVisibility(View.VISIBLE);
                if (!selected_clients_list.isEmpty()) {
                    loadSelectedClients(new String[selected_clients_list.size()]);
                    ll_selected_clients.setVisibility(View.VISIBLE);
                }
                callGroupsWebservice();
                AddGctDetails();
            } else {
                at_add_clients.setText("");
                at_add_groups.setText("");
                at_add_corp_clients.setText("");
                selected_clients.setVisibility(View.GONE);
                ll_selected_clients.setVisibility(View.GONE);
                ll_selected_groups.removeAllViews();
                rv_display_upload_groups_docs.setVisibility(View.GONE);
                ll_assign_team_members.setVisibility(View.GONE);
                ll_add_groups.setVisibility(View.GONE);
                selected_groups_list.clear();
                temporary_tm_list.clear();
                temporary_groups_list.clear();
                selected_tm_list.clear();
                loadSelectedGroups(new String[selected_groups_list.size()]);
//                ll_save_buttons.setVisibility(View.GONE);
            }
            if (selected_clients_list.isEmpty() && selected_corp_clients_list.isEmpty() && selected_temp_clients_list.isEmpty()) {
                selected_tm.setVisibility(View.GONE);
                ll_save_buttons.setVisibility(View.GONE);
            }
            AddGctDetails();
        } else {
            ll_add_clients.setVisibility(View.VISIBLE);
            if (tmList.isEmpty()) {
                ll_assign_team_members.setVisibility(View.GONE);
            } else {
                ll_assign_team_members.setVisibility(View.VISIBLE);
            }
            loadSelectedClients(new String[selected_clients_list.size()]);
            ll_save_buttons.setVisibility(View.VISIBLE);
        }
        ToggleClient();
    }

//    public void Add_Clients() {
//        Set<ClientsModel> selected_groups_set = new HashSet<ClientsModel>();
//        for (int i = 0; i < clientsList.size(); i++) {
//            ClientsModel clientsModel = clientsList.get(i);
//            if (clientsModel.isChecked()) {
//
//                //                           jsonArray.put(selected_documents_list.get(i).getGroup_name());
//                selected_groups_set.add(clientsModel);
//            }
//            selected_clients_list.clear();
//            if (!Constants.create_matter) {
//                btn_create.setAlpha(1.0f);
//                btn_create.setEnabled(true);
//            }
//            selected_clients_list.addAll(selected_groups_set);
//        }

    /// /                    if (!selected_clients_list.contains(selected_temp_clients_list)) {
    /// /                        selected_clients_list.addAll(selected_temp_clients_list);
    /// /                    }
    /// /        ll_assign_team_members.setVisibility(View.VISIBLE);
    /// /        ll_save_buttons.setVisibility(View.VISIBLE);
    /// /                    loadSelectedGroups();
//        loadClientsText();
//        if (Constants.create_matter) {
//            ClientListChanges();
//        }
//    }
    public void Add_Clients() {
        if (!Constants.create_matter) {
            btn_create.setAlpha(1.0f);
            btn_create.setEnabled(true);
        }

        loadClientsText();

//        if (Constants.create_matter) {
//            ClientListChanges(selected_clients_list);
//        }
    }


    public void loadTeamText() {
        String[] value = new String[temporary_tm_list.size()];
        for (int i = 0; i < temporary_tm_list.size(); i++) {
//                                value += "," + family_members.get(i);
//                               value.add(family_members.get(i));
            value[i] = temporary_tm_list.get(i).getTm_name();

        }

        String str = String.join(",", value);
        at_assigned_team_members.setText(str);
        AddGctDetails();
        AndroidUtils.ToggleButton(temporary_tm_list.size(), btn_add_teammembers);
    }

//    private void loadSelectedTM(String[] value) {
//        if (Constants.create_matter)
//            if (!selected_groups_list.isEmpty())
//                ll_save_buttons.setVisibility(View.VISIBLE);
//        if (tmList.isEmpty()) {
//            ll_assign_team_members.setVisibility(View.GONE);
//        } else {
//            ll_assign_team_members.setVisibility(View.VISIBLE);
//        }
//        if (selected_tm_list.isEmpty()) {
//            selected_tm.setVisibility(View.GONE);
//        } else {
//            selected_tm.setVisibility(View.VISIBLE);
//        }
//        ll_assigned_team_members.removeAllViews();
//        for (int i = 0; i < selected_tm_list.size(); i++) {
//            View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.edit_opponent_advocate, null);
//            if (view_opponents != null) {
//                TextView tv_opponent_name = view_opponents.findViewById(R.id.tv_opponent_name);
//                ImageView iv_remove_opponent = view_opponents.findViewById(R.id.iv_remove_opponent);
//                ImageView iv_edit_opponent = view_opponents.findViewById(R.id.iv_edit_opponent);
//                iv_edit_opponent.setVisibility(View.GONE);
//
//                if (tv_opponent_name != null && iv_remove_opponent != null) {
//                    tv_opponent_name.setText(selected_tm_list.get(i).getTm_name());
//                    if (!Constants.create_matter) {
//                        if (selected_tm_list.get(i).getTm_id().equals(Constants.owner_id)) {
//                            iv_remove_opponent.setVisibility(View.INVISIBLE);
//                        }

    /// /                    } else {
//                        iv_remove_opponent.setTag(i); // Tag each view with its position
//                        iv_remove_opponent.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                try {
//                                    int position = (int) v.getTag();
//                                    // Remove the view at the specified position
//                                    ll_assigned_team_members.removeViewAt(position);
//                                    // Remove the corresponding item from the list
//                                    TeamModel teamModel = selected_tm_list.remove(position);
//                                    teamModel.setChecked(false);
//                                    // Update the tags of the remaining views
//                                    for (int j = 0; j < ll_assigned_team_members.getChildCount(); j++) {
//                                        ImageView iv_remove = ll_assigned_team_members.getChildAt(j).findViewById(R.id.iv_remove_opponent);
//                                        if (iv_remove != null) {
//                                            iv_remove.setTag(j);
//                                        }
//                                    }
//
//                                    String str = String.join(",", value);
//                                    at_assigned_team_members.setText(str);
//                                    // Update at_add_groups text
//                                    StringBuilder stringBuilder = new StringBuilder();
//                                    for (TeamModel model : selected_tm_list) {
//                                        stringBuilder.append(model.getTm_name()).append(",");
//                                    }
//
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
//                                } catch (Exception e) {
//                                    e.fillInStackTrace();
//                                    AndroidUtils.showAlert(e.getMessage(), getActivity());
//                                }
//                                btn_create.setAlpha(1.0f);
//                                btn_create.setEnabled(true);
//                            }
//                        });
//                        iv_remove_opponent.setVisibility(View.VISIBLE); // Set visibility here
//                    }
//                }
//                ll_assigned_team_members.addView(view_opponents);
//            }
//        }
//    }
    private void loadSelectedTM(String[] value) {
        if (Constants.create_matter && !selected_groups_list.isEmpty()) {
            ll_save_buttons.setVisibility(View.VISIBLE);
        }

        ll_assign_team_members.setVisibility(tmList.isEmpty() ? View.GONE : View.VISIBLE);
        selected_tm.setVisibility(selected_tm_list.isEmpty() ? View.GONE : View.VISIBLE);

        ll_assigned_team_members.removeAllViews();

        for (int i = 0; i < selected_tm_list.size(); i++) {
            View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.edit_opponent_advocate, null);
            if (view_opponents == null) continue;

            TextView tv_opponent_name = view_opponents.findViewById(R.id.tv_opponent_name);
            ImageView iv_remove_opponent = view_opponents.findViewById(R.id.iv_remove_opponent);
            ImageView iv_edit_opponent = view_opponents.findViewById(R.id.iv_edit_opponent);

            iv_edit_opponent.setVisibility(View.GONE);

            if (tv_opponent_name != null && iv_remove_opponent != null) {
                TeamModel currentModel = selected_tm_list.get(i);
                tv_opponent_name.setText(currentModel.getTm_name());

                boolean isOwner = currentModel.getTm_id().equals(Constants.owner_id);
                boolean isCurrentUser = currentModel.getTm_id().equals(Constants.USER_ID);

                if (!Constants.create_matter && (isOwner || isCurrentUser)) {
                    // Hide remove button for owner OR current user when not creating matter
                    iv_remove_opponent.setVisibility(View.INVISIBLE);
                } else {
                    // Show remove button for others
                    iv_remove_opponent.setVisibility(View.VISIBLE);
                    iv_remove_opponent.setTag(i);

                    iv_remove_opponent.setOnClickListener(v -> {
                        try {
                            int position = (int) v.getTag();

                            // Remove from selected list
                            TeamModel teamModel = selected_tm_list.remove(position);
                            teamModel.setChecked(false);

                            // 🔥 Sync with temporary list
                            for (int k = 0; k < temporary_tm_list.size(); k++) {
                                if (temporary_tm_list.get(k).getTm_id().equals(teamModel.getTm_id())) {
                                    temporary_tm_list.remove(k);
                                    break;
                                }
                            }

                            // Remove view
                            ll_assigned_team_members.removeViewAt(position);

                            // Update tags
                            for (int j = 0; j < ll_assigned_team_members.getChildCount(); j++) {
                                ImageView iv_remove = ll_assigned_team_members
                                        .getChildAt(j)
                                        .findViewById(R.id.iv_remove_opponent);
                                if (iv_remove != null) {
                                    iv_remove.setTag(j);
                                }
                            }

                            // Update display text
                            StringBuilder stringBuilder = new StringBuilder();
                            for (TeamModel model : selected_tm_list) {
                                stringBuilder.append(model.getTm_name()).append(",");
                            }
                            if (stringBuilder.length() > 0) {
                                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                            }
                            at_assigned_team_members.setText(stringBuilder.toString());

                            selected_tm.setVisibility(selected_tm_list.isEmpty() ? View.GONE : View.VISIBLE);

                            // if groups are empty → hide assigned team section
                            if (selected_groups_list.isEmpty()) {
                                ll_assigned_team_members.removeAllViews();
                                selected_tm.setVisibility(View.GONE);
                                temporary_tm_list.clear();
                            }

                            AddGctDetails();
                            AndroidUtils.ToggleButton(temporary_tm_list.size(), btn_add_teammembers);

                        } catch (Exception e) {
                            e.fillInStackTrace();
                            AndroidUtils.showAlert(e.getMessage(), getActivity());
                        }

                        btn_create.setAlpha(1.0f);
                        btn_create.setEnabled(true);
                    });
                }
            }

            ll_assigned_team_members.addView(view_opponents);
        }
    }

    private void loadSelectedTemp_Clients() {
        if (istempclient)
            selected_temp_clients.setVisibility(View.VISIBLE);
        //views get cleared after clicking the add client.
        ll_selected_temp_clients.removeAllViews();
//        String[] value = new String[selected_corp_clients_list.size()];
//        for (int i = 0; i < selected_corp_clients_list.size(); i++) {
////                                value += "," + family_members.get(i);
////                               value.add(family_members.get(i));
//            value[i] = selected_corp_clients_list.get(i).getClient_name();
//        }

//        String str = String.join(",", value);
//        at_add_corp_clients.setText(str);
//        temp_client_layout.setVisibility(View.VISIBLE);

//        ll_assign_team_members.setVisibility(View.VISIBLE);
//        ll_save_buttons.setVisibility(View.VISIBLE);


        for (int i = 0; i < selected_temp_clients_list.size(); i++) {
            View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.edit_opponent_advocate, null);
            if (view_opponents != null) {
                TextView tv_opponent_name = view_opponents.findViewById(R.id.tv_opponent_name);
                ImageView iv_remove_opponent = view_opponents.findViewById(R.id.iv_remove_opponent);
                ImageView iv_edit_opponent = view_opponents.findViewById(R.id.iv_edit_opponent);
                iv_edit_opponent.setVisibility(View.GONE);
                if (tv_opponent_name != null && iv_remove_opponent != null) {
                    tv_opponent_name.setText(selected_temp_clients_list.get(i).getClient_name());
                    iv_remove_opponent.setTag(i); // Tag each view with its position
                    iv_remove_opponent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                int position = (int) v.getTag();
                                // Remove the view at the specified position
                                ll_selected_temp_clients.removeViewAt(position);
                                // Remove the corresponding item from the list
                                ClientsModel clientsModel = selected_temp_clients_list.remove(position);
                                clientsModel.setChecked(false);
                                // Update the tags of the remaining views
                                for (int j = 0; j < ll_selected_temp_clients.getChildCount(); j++) {
                                    ImageView iv_remove = ll_selected_temp_clients.getChildAt(j).findViewById(R.id.iv_remove_opponent);
                                    if (iv_remove != null) {
                                        iv_remove.setTag(j);
                                    }
                                }
                                if (selected_temp_clients_list.isEmpty()) {
                                    selected_temp_clients.setVisibility(View.GONE);
                                } else {
                                    selected_temp_clients.setVisibility(View.VISIBLE);
                                }
                                loadClients();
//                                selected_documents_list.clear();
                                documentsList.clear();
                                AndroidUtils.ToggleButton(selected_temp_clients_list.size(), btn_add_temp_client);
                            } catch (Exception e) {
                                e.fillInStackTrace();
                                AndroidUtils.showAlert(e.getMessage(), getActivity());
                            }

                        }
                    });
                    iv_remove_opponent.setVisibility(View.VISIBLE); // Set visibility here
                }
                ll_selected_temp_clients.addView(view_opponents);
            }
        }
    }

    private void loadSelectedCorp_Clients() {
        if (iscorpclient) {
            selected_corp_clients.setVisibility(View.VISIBLE);
        }
        //views get cleared after clicking the add client.
        ll_selected_corp_clients.removeAllViews();
        String[] value = new String[selected_corp_clients_list.size()];
        for (
                int i = 0; i < selected_corp_clients_list.size(); i++) {
//                                value += "," + family_members.get(i);
//                               value.add(family_members.get(i));
            value[i] = selected_corp_clients_list.get(i).getClient_name();
        }

        String str = String.join(",", value);
        at_add_corp_clients.setText(str);
        ll_add_clients.setVisibility(View.VISIBLE);
        clients_list_layout.setVisibility(View.VISIBLE);
//        temp_client_layout.setVisibility(View.VISIBLE);

//        ll_assign_team_members.setVisibility(View.VISIBLE);
//        ll_save_buttons.setVisibility(View.VISIBLE);


        for (int i = 0; i < selected_corp_clients_list.size(); i++) {
            View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.edit_opponent_advocate, null);
            if (view_opponents != null) {
                TextView tv_opponent_name = view_opponents.findViewById(R.id.tv_opponent_name);
                ImageView iv_remove_opponent = view_opponents.findViewById(R.id.iv_remove_opponent);
                ImageView iv_edit_opponent = view_opponents.findViewById(R.id.iv_edit_opponent);
                iv_edit_opponent.setVisibility(View.GONE);
                if (tv_opponent_name != null && iv_remove_opponent != null) {
                    tv_opponent_name.setText(selected_corp_clients_list.get(i).getClient_name());
                    iv_remove_opponent.setTag(i); // Tag each view with its position
                    iv_remove_opponent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                int position = (int) v.getTag();
                                // Remove the view at the specified position
                                ll_selected_corp_clients.removeViewAt(position);
                                // Remove the corresponding item from the list
                                ClientsModel clientsModel = selected_corp_clients_list.remove(position);
                                clientsModel.setChecked(false);
                                // Update the tags of the remaining views
                                for (int j = 0; j < ll_selected_corp_clients.getChildCount(); j++) {
                                    ImageView iv_remove = ll_selected_corp_clients.getChildAt(j).findViewById(R.id.iv_remove_opponent);
                                    if (iv_remove != null) {
                                        iv_remove.setTag(j);
                                    }
                                }

                                String str = String.join(",", value);
                                at_add_corp_clients.setText(str);
                                // Update at_add_groups text
                                StringBuilder stringBuilder = new StringBuilder();
                                for (ClientsModel model : selected_corp_clients_list) {
                                    stringBuilder.append(model.getClient_name()).append(",");
                                }

                                if (stringBuilder.length() > 0) {
                                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                                }
                                String strn = stringBuilder.toString();
                                at_add_corp_clients.setText(strn);
                                if (selected_corp_clients_list.isEmpty()) {
                                    selected_corp_clients.setVisibility(View.GONE);
                                } else {
                                    selected_corp_clients.setVisibility(View.VISIBLE);
                                }
                                loadClients();
//                                selected_documents_list.clear();
                                documentsList.clear();
                                AddGctDetails();
//                                ClientListChanges(selected_corp_clients_list);
                                AndroidUtils.ToggleButton(temporary_corpclients_list.size(), btn_add_corp_clients);
//                                updatedDisplay();
                            } catch (Exception e) {
                                e.fillInStackTrace();
                                AndroidUtils.showAlert(e.getMessage(), getActivity());
                            }

                        }
                    });
                    iv_remove_opponent.setVisibility(View.VISIBLE); // Set visibility here
                }
                ll_selected_corp_clients.addView(view_opponents);
            }
        }
    }

    private void loadSelectedClients(String[] value) {
//        if(client_type.equals("corporate"))
//        {
//            selected_clients_list.clear();
//            selected_clients_list=selected_corp_clients_list;
//        }
        if (isclient) {
            selected_clients.setVisibility(View.VISIBLE);
        }
        if (!selected_clients_list.isEmpty()) {
            ll_selected_clients.setVisibility(View.VISIBLE);
        }
        //views get cleared after clicking the add client.
        ll_selected_clients.removeAllViews();
        ll_add_clients.setVisibility(View.VISIBLE);
        clients_list_layout.setVisibility(View.VISIBLE);
        if (!Constants.create_matter)
            if (!selected_corp_clients_list.isEmpty()) {
                // Check if the first item in selected_corp_clients_list is already present in selected_clients_list
                boolean isAlreadyPresent = false;
                for (ClientsModel client : selected_clients_list) {
                    if (client.getClient_id().equals(selected_corp_clients_list.get(0).getClient_id())) {
                        isAlreadyPresent = true;
                        break;
                    }
                }

                // Add only if not present
                if (!isAlreadyPresent) {
                    temporary_clients_list.add(selected_corp_clients_list.get(0));
                    selected_clients_list.add(selected_corp_clients_list.get(0));
                }
            }


//        temp_client_layout.setVisibility(View.VISIBLE);


        for (int i = 0; i < selected_clients_list.size(); i++) {
            View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.edit_opponent_advocate, null);
            if (view_opponents != null) {
                TextView tv_opponent_name = view_opponents.findViewById(R.id.tv_opponent_name);
                ImageView iv_remove_opponent = view_opponents.findViewById(R.id.iv_remove_opponent);
                ImageView iv_edit_opponent = view_opponents.findViewById(R.id.iv_edit_opponent);
                iv_edit_opponent.setVisibility(View.GONE);
//                if (!Constants.create_matter) {
//                    iv_edit_opponent.setVisibility(View.GONE);
//                } else {
////                    iv_edit_opponent.setVisibility(View.VISIBLE);
//                }
//                iv_remove_opponent.setVisibility(View.GONE);
                if (!Constants.create_matter) {
                    iv_remove_opponent.setVisibility(View.INVISIBLE);
                } else {
                    iv_remove_opponent.setVisibility(View.VISIBLE);
                }
                if (tv_opponent_name != null && iv_remove_opponent != null) {
                    tv_opponent_name.setText(selected_clients_list.get(i).getClient_name());
                    if (Constants.create_matter) {
                        iv_remove_opponent.setTag(i); // Tag each view with its position
                        iv_remove_opponent.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    int position = (int) v.getTag();
                                    // Remove the view at the specified position
                                    ll_selected_clients.removeViewAt(position);
                                    // Remove the corresponding item from the list
                                    ClientsModel clientsModel = selected_clients_list.remove(position);
                                    temporary_clients_list.remove(position);
                                    clientsModel.setChecked(false);
                                    // Update the tags of the remaining views
                                    for (int j = 0; j < ll_selected_clients.getChildCount(); j++) {
                                        ImageView iv_remove = ll_selected_clients.getChildAt(j).findViewById(R.id.iv_remove_opponent);
                                        if (iv_remove != null) {
                                            iv_remove.setTag(j);
                                        }
                                    }

                                    String str = String.join(",", value);
                                    at_add_clients.setText(str);
                                    // Update at_add_groups text
                                    StringBuilder stringBuilder = new StringBuilder();
                                    for (ClientsModel model : selected_clients_list) {
                                        stringBuilder.append(model.getClient_name()).append(",");
                                    }

                                    if (stringBuilder.length() > 0) {
                                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                                    }
                                    String strn = stringBuilder.toString();
                                    at_add_clients.setText(strn);
//                                updatedDisplay();
                                    if (!selected_clients_list.isEmpty()) {
                                        selected_clients.setVisibility(View.VISIBLE);
                                    } else {
                                        selected_clients.setVisibility(View.GONE);
                                    }
//                                    if (Constants.create_matter) {
//                                        ClientListChanges(selected_clients_list);
//                                    }
                                    loadClients();
//                                    selected_documents_list.clear();
                                    documentsList.clear();
                                    AndroidUtils.ToggleButton(temporary_clients_list.size(), btn_add_clients);
                                    btn_create.setAlpha(1.0f);
                                    btn_create.setEnabled(true);
                                } catch (Exception e) {
                                    e.fillInStackTrace();
                                    AndroidUtils.showAlert(e.getMessage(), getActivity());
                                }
                            }
                        });
                        iv_remove_opponent.setVisibility(View.VISIBLE); // Set visibility here
                    }
                }
                ll_selected_clients.addView(view_opponents);
            }
        }
    }

    private void updatedDisplay() {
        if (!selected_clients_list.isEmpty()) {
            selected_clients.setVisibility(View.VISIBLE);
        } else {
            selected_clients.setVisibility(View.GONE);
        }
    }

    private void load_countries() {
//        sp_country.setVisibility(View.VISIBLE);
        CommonSpinnerAdapter adapter = new CommonSpinnerAdapter(getActivity(), countriesList);
        Log.i("ArrayList", "Info:" + countriesList);
//        ArrayAdapter adaptador = new ArrayAdapter(User_Profile.this, android.R.layout.simple_spinner_item, sorted_countriesList);
//        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

//        spinner.setAdapter(adaptador);
        sp_country.setAdapter(adapter);
    }

    //    private void loadGroupsData(JSONArray data) {
//        try {
//            groupsList.clear();
//            for (int i = 0; i < data.length(); i++) {
//                JSONObject jsonObject = data.getJSONObject(i);
//                GroupsModel groupsModel = new GroupsModel();
//                groupsModel.setGroup_id(jsonObject.getString("id"));
//                groupsModel.setGroup_name(jsonObject.getString("name"));
//                if ((!jsonObject.getString("name").equals("AAM")) && ((!jsonObject.getString("name").equals("SuperUser"))))
//                    groupsList.add(groupsModel);
//            }
//            selectedLanguage = new boolean[groupsList.size()];

    /// /            GroupsAlert();
//            for (int i = selected_groups_list.size() - 1; i >= 0; i--) {
//                GroupsModel selectedGroup = selected_groups_list.get(i);
//                boolean existsInGroups = false;
//                for (int j = 0; j < groupsList.size(); j++) {
//                    if (selectedGroup.getGroup_id().equals(groupsList.get(j).getGroup_id())) {
//                        groupsList.get(j).setChecked(true); // Mark as checked in groupsList
//                        existsInGroups = true;
//                        break;
//                    }
//                }
//                if (!existsInGroups) {
//                    selected_groups_list.remove(i); // Safe to remove in reverse
//                }
//            }
//            GroupsPopup();
//        } catch (JSONException e) {
//            e.fillInStackTrace();
//            AndroidUtils.showAlert(e.getMessage(), getActivity());
//        }
//    }
//    private void loadGroupsData(JSONArray data) {
//        try {
//            groupsList.clear();
//
//            // Populate groupsList from JSON array, excluding "AAM" and "SuperUser"
//            for (int i = 0; i < data.length(); i++) {
//                JSONObject jsonObject = data.getJSONObject(i);
//                String groupId = jsonObject.getString("id");
//                String groupName = jsonObject.getString("name");
//
//                if (!groupName.equals("AAM") && !groupName.equals("SuperUser")) {
//                    GroupsModel groupsModel = new GroupsModel();
//                    groupsModel.setGroup_id(groupId);
//                    groupsModel.setGroup_name(groupName);
//                    groupsList.add(groupsModel);
//                }
//            }
//
//            selectedLanguage = new boolean[groupsList.size()];
//
//            // Clean up selected_groups_list to only keep valid groups that still exist in groupsList
//            for (int i = selected_groups_list.size() - 1; i >= 0; i--) {
//                GroupsModel selectedGroup = selected_groups_list.get(i);
//                boolean existsInGroups = false;
//
//                for (int j = 0; j < groupsList.size(); j++) {
//                    if (selectedGroup.getGroup_id().equals(groupsList.get(j).getGroup_id())) {
//                        groupsList.get(j).setChecked(true); // Mark group as selected in groupsList
//                        existsInGroups = true;
//                        break;
//                    }
//                }
//
//                if (!existsInGroups) {
//                    selected_groups_list.remove(i);
//                    temporary_groups_list.remove(i);// Remove from selected_groups_list if not in groupsList
//                }
//                loadSelectedGroups(new String[selected_groups_list.size()]);
//                loadGroupsText();
//            }
//
//            // Now show the popup with filtered groups
//            GroupsPopup();
//            if (!selected_groups_list.isEmpty()) {
//                callTMWebservice();
//            } else {
//                selected_tm_list.clear();
//                tmList.clear();
//                at_assigned_team_members.setText("");
//            }
//        } catch (JSONException e) {
//            e.fillInStackTrace();
//            AndroidUtils.showAlert(e.getMessage(), getActivity());
//        }
//    }
//    private void loadGroupsData(JSONArray data, JSONArray clientGroups) {
//        try {
//            groupsList.clear();
//
//            // 1️⃣ Parse groups
//            for (int i = 0; i < data.length(); i++) {
//                JSONObject jsonObject = data.getJSONObject(i);
//                String groupId = jsonObject.getString("id");
//                String groupName = jsonObject.getString("name");
//
//                if (!groupName.equals("AAM") && !groupName.equals("SuperUser")) {
//                    GroupsModel groupsModel = new GroupsModel();
//                    groupsModel.setGroup_id(groupId);
//                    groupsModel.setGroup_name(groupName);
//                    groupsList.add(groupsModel);
//                }
//            }
//
//            // 2️⃣ Parse client_groups (if available)
//            ArrayList<ClientGroupModel> allClientGroups = new ArrayList<>();
//            if (clientGroups != null) {
//                for (int i = 0; i < clientGroups.length(); i++) {
//                    JSONObject clientObj = clientGroups.getJSONObject(i);
//                    ClientGroupModel clientGroupModel = new ClientGroupModel();
//                    clientGroupModel.setId(clientObj.getString("id"));
//                    clientGroupModel.setName(clientObj.getString("name"));
//
//                    JSONArray groupsArray = clientObj.optJSONArray("groups");
//                    List<String> groupIds = new ArrayList<>();
//                    if (groupsArray != null) {
//                        for (int j = 0; j < groupsArray.length(); j++) {
//                            groupIds.add(groupsArray.getString(j));
//                        }
//                    }
//                    clientGroupModel.setGroups(groupIds);
//                    allClientGroups.add(clientGroupModel);
//                }
//
//                // 3️⃣ Attach client groups to each group
//                for (GroupsModel group : groupsList) {
//                    List<ClientGroupModel> clientsInGroup = new ArrayList<>();
//                    for (ClientGroupModel client : allClientGroups) {
//                        if (client.getGroups().contains(group.getGroup_id())) {
//                            clientsInGroup.add(client);
//                        }
//                    }
//                    group.setClientGroupModelList(clientsInGroup);
//                }
//            }
//
//            // 4️⃣ Maintain previous selections
//            selectedLanguage = new boolean[groupsList.size()];
//
//            for (int i = selected_groups_list.size() - 1; i >= 0; i--) {
//                GroupsModel selectedGroup = selected_groups_list.get(i);
//                boolean existsInGroups = false;
//
//                for (int j = 0; j < groupsList.size(); j++) {
//                    if (selectedGroup.getGroup_id().equals(groupsList.get(j).getGroup_id())) {
//                        groupsList.get(j).setChecked(true);
//                        existsInGroups = true;
//                        break;
//                    }
//                }
//
//                if (!existsInGroups) {
//                    selected_groups_list.remove(i);
//                    temporary_groups_list.remove(i);
//                }
//            }
//
//            loadSelectedGroups(new String[selected_groups_list.size()]);
//            loadGroupsText();
//
//            GroupsPopup();
//
//            if (!selected_groups_list.isEmpty()) {
//                callTMWebservice();
//            } else {
//                selected_tm_list.clear();
//                tmList.clear();
//                at_assigned_team_members.setText("");
//            }
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//            AndroidUtils.showAlert(e.getMessage(), getActivity());
//        }
//    }
    private void loadGroupsData(JSONArray data, JSONArray clientGroups) {
        try {
            groupsList.clear();
            selected_groups_list.clear();
            temporary_groups_list.clear();

            // 1️⃣ Parse groups
            for (int i = 0; i < data.length(); i++) {
                JSONObject jsonObject = data.getJSONObject(i);
                String groupId = jsonObject.getString("id");
                String groupName = jsonObject.getString("name");

                if (!groupName.equals("AAM") && !groupName.equals("SuperUser")) {
                    GroupsModel groupsModel = new GroupsModel();
                    groupsModel.setGroup_id(groupId);
                    groupsModel.setGroup_name(groupName);
                    groupsModel.setChecked(true); // ✅ Default all selected
                    groupsList.add(groupsModel);
                }
            }

            // 2️⃣ Parse client_groups (if available)
            allClientGroups.clear();
            Constants.allClientGroups.clear();
            if (clientGroups != null) {
                for (int i = 0; i < clientGroups.length(); i++) {
                    JSONObject clientObj = clientGroups.getJSONObject(i);
                    ClientGroupModel clientGroupModel = new ClientGroupModel();
                    clientGroupModel.setId(clientObj.getString("id"));
                    clientGroupModel.setName(clientObj.getString("name"));

                    JSONArray groupsArray = clientObj.optJSONArray("groups");
                    List<String> groupIds = new ArrayList<>();
                    if (groupsArray != null) {
                        for (int j = 0; j < groupsArray.length(); j++) {
                            groupIds.add(groupsArray.getString(j));
                        }
                    }
                    clientGroupModel.setGroups(groupIds);
                    allClientGroups.add(clientGroupModel);
                }
                Constants.allClientGroups.addAll(allClientGroups);

                // 3️⃣ Attach client groups to each group
                for (GroupsModel group : groupsList) {
                    List<ClientGroupModel> clientsInGroup = new ArrayList<>();
                    for (ClientGroupModel client : allClientGroups) {
                        if (client.getGroups().contains(group.getGroup_id())) {
                            clientsInGroup.add(client);
                        }
                    }
                    group.setClientGroupModelList(clientsInGroup);
                }
            }

//            if (!selected_temp_clients_list.isEmpty() && selected_clients_list.isEmpty() && selected_corp_clients_list.isEmpty()) {
            selected_groups_list.addAll(groupsList);
            temporary_groups_list.addAll(groupsList);
//            }
            selectedLanguage = new boolean[groupsList.size()];
            Arrays.fill(selectedLanguage, true);

            // 5️⃣ Load UI
            loadSelectedGroups(new String[selected_groups_list.size()]);
            loadGroupsText();
            GroupsPopup();
            AddGctDetails();
            // 6️⃣ Load Team Members
            if (!selected_groups_list.isEmpty()) {
                callTMWebservice();
            } else {
                selected_tm_list.clear();
                tmList.clear();
                at_assigned_team_members.setText("");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }


    public void GroupsPopup() {
        try {
            for (int i = 0; i < groupsList.size(); i++) {
                for (int j = 0; j < selected_groups_list.size(); j++) {
                    if (groupsList.get(i).getGroup_id().equals(selected_groups_list.get(j).getGroup_id())) {
                        GroupsModel groupsModel = groupsList.get(i);
                        groupsModel.setChecked(true);
                    }
                }
            }

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            AlertDialog dialog = dialogBuilder.create();
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            rv_display_upload_groups_docs.setLayoutManager(layoutManager);
            // rv_display_upload_groups_docs.setHasFixedSize(true);
            ADAPTER_TAG = "Groups";
            GroupsAdapter documentsAdapter = new GroupsAdapter(groupsList, clientsList, tmList, new_groupsList, ADAPTER_TAG, this);
            rv_display_upload_groups_docs.setAdapter(documentsAdapter);
            groupsAdapter = documentsAdapter;
            AndroidUtils.LoadList(rv_display_upload_groups_docs, getContext(), groupsList.size(), false);
//            ButtonStatus(selected_groups_list.size(), btn_add_groups);
            btn_add_groups.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selected_groups_list.clear();
                    selected_groups_list.addAll(temporary_groups_list);
                    Add_Groups();
                    ischecked_group = true;
                    rv_display_upload_groups_docs.setVisibility(View.GONE);
                    updateDisplay();
//                    selected_documents_list.clear();
                    documentsList.clear();
                    loadSelectedGroups(new String[selected_groups_list.size()]);
                    callTMWebservice();
                }
            });

        } catch (Exception e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    public void Add_Groups() {
//        Set<GroupsModel> selected_groups_set = new HashSet<>();
//        for (int i = 0; i < groupsAdapter.getList_item().size(); i++) {
//            GroupsModel groupsModel = groupsAdapter.getList_item().get(i);
//            if (groupsModel.isChecked()) {
//                selected_groups_set.add(groupsModel);
//            }
//        }
//
//        selected_groups_list.clear();
//        selected_groups_list.addAll(selected_groups_set);

        detectListChanges();
//        updateDisplay();
        loadGroupsText();
//        btn_add_groups.setEnabled(false);
    }

    private void ClientListChanges(ArrayList<ClientsModel> selected_clients_list) {
        ArrayList<ClientsModel> clientsModelArrayList = new ArrayList<>(selected_clients_list);
        int originalsize = clientsModelArrayList.size();
        clientsModelArrayList.removeAll(selected_clients_list);
        int newsize = clientsModelArrayList.size();
        if (newsize > originalsize || newsize < originalsize) {
//            groupsList.clear();
            tmList.clear();
//            temporary_groups_list.clear();
//            selected_groups_list.clear();
//            temporary_tm_list.clear();
//            selected_tm_list.clear();
            at_add_groups.setText("");
            at_assigned_team_members.setText("");
            selected_tm.setVisibility(View.GONE);
            selected_groups.setVisibility(View.GONE);
        }
    }

    private void detectListChanges() {
        updated_groups_list.addAll(selected_groups_list);
        int originalSize = updated_groups_list.size();
        updated_groups_list.removeAll(selected_groups_list);
        int newSize = updated_groups_list.size();
        if (newSize > originalSize || newSize < originalSize) {
//            clientsList.clear();
//            corp_clients_list.clear();
//            tmList.clear();
//            selected_corp_clients_list.clear();
//            selected_clients_list.clear();
//            selected_tm_list.clear();
//            temporary_tm_list.clear();
//            ll_selected_corp_clients.removeAllViews();
//            ll_selected_clients.removeAllViews();
//            selected_clients.setVisibility(View.GONE);
//            selected_corp_clients.setVisibility(View.GONE);
            ll_assigned_team_members.removeAllViews();
            selected_tm.setVisibility(View.GONE);
        } else if (newSize == originalSize) {
            // items have been removed from the list
        } else if (newSize == 0 || originalSize == 0) {
            selected_groups.setVisibility(View.GONE);
            ll_selected_groups.removeAllViews();
        }
//        at_add_clients.setText("");
//        at_add_corp_clients.setText("");
        at_assigned_team_members.setText("");
        AddGctDetails();
    }

    public void loadGroupsText() {
        if (temporary_groups_list.isEmpty()) {
            at_add_groups.setText("");
        } else {
            String[] value = new String[temporary_groups_list.size()];
            for (int i = 0; i < temporary_groups_list.size(); i++) {
                value[i] = temporary_groups_list.get(i).getGroup_name();
            }
//            detectListChanges();
            String str = String.join(",", value);
            at_add_groups.setText(str);
        }
        AndroidUtils.ToggleButton(temporary_tm_list.size(), btn_add_teammembers);
        AndroidUtils.ToggleButton(temporary_groups_list.size(), btn_add_groups);
    }

    public void loadSelectedGroups(String[] value) {
        if (selected_groups_list.isEmpty()) {
            at_add_groups.setText("");
            selected_groups.setVisibility(View.GONE);
            ll_selected_groups.removeAllViews();

            // 🔥 Also clear assigned TMs when no groups remain
            selected_tm_list.clear();
            temporary_tm_list.clear();
            ll_assigned_team_members.removeAllViews();
            selected_tm.setVisibility(View.GONE);

        } else {
            ll_selected_groups.removeAllViews();
            selected_groups.setVisibility(View.VISIBLE);
            AddGctDetails();

            for (int i = 0; i < selected_groups_list.size(); i++) {
                View view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.edit_opponent_advocate, null);
                if (view_opponents != null) {
                    TextView tv_opponent_name = view_opponents.findViewById(R.id.tv_opponent_name);
                    ImageView iv_remove_opponent = view_opponents.findViewById(R.id.iv_remove_opponent);
                    ImageView iv_edit_opponent = view_opponents.findViewById(R.id.iv_edit_opponent);

                    iv_edit_opponent.setVisibility(View.GONE);

                    if (tv_opponent_name != null && iv_remove_opponent != null) {
                        GroupsModel currentGroup = selected_groups_list.get(i);
                        tv_opponent_name.setText(currentGroup.getGroup_name());

                        iv_remove_opponent.setTag(i);
                        iv_remove_opponent.setOnClickListener(v -> {
                            int position = (int) v.getTag();
                            checkingRemovalLogic(position);
                        });

                        iv_remove_opponent.setVisibility(View.VISIBLE);
                    }

                    ll_selected_groups.addView(view_opponents);
                }
            }
        }
    }

    public void checkingRemovalLogic(int position) {
        try {
            // Remove from selected list
            GroupsModel groupsModel = selected_groups_list.get(position);
            // 🚫 Validate client mapping before removal
            boolean canRemove = true;
            for (ClientGroupModel client : groupsModel.getClientGroupModelList()) {
                // Check if this is the only selected group for the client
                boolean hasOtherSelected = false;

                for (GroupsModel selectedGroup : selected_groups_list) {
                    if (!selectedGroup.getGroup_id().equals(groupsModel.getGroup_id()) &&
                            client.getGroups().contains(selectedGroup.getGroup_id())) {
                        hasOtherSelected = true;
                        break;
                    }
                }

                if (!hasOtherSelected) {
                    canRemove = false;
                    groupsModel.setChecked(true);
                    groupsAdapter.notifyDataSetChanged();
                    AndroidUtils.showAlert("This group cannot be removed — all selected clients must have at least one associated group.", getActivity());
//                    AndroidUtils.showAlert("Client \"" + client.getName() + "\" must have at least one group selected.", getActivity());
                    break;
                }
            }

            if (!canRemove) {
                // ❌ Don't allow deselection
                return;
            }

// ✅ Safe to remove
            selected_groups_list.remove(position);
            groupsModel.setChecked(false);


            // 🔥 Also update adapter's sharedList (source of RecyclerView)
            for (GroupsModel model : groupsList) {
                if (model.getGroup_id().equals(groupsModel.getGroup_id())) {
                    model.setChecked(false);
                    break;
                }
            }

            // Sync with temporary list
            for (int k = 0; k < temporary_groups_list.size(); k++) {
                if (temporary_groups_list.get(k).getGroup_id().equals(groupsModel.getGroup_id())) {
                    temporary_groups_list.remove(k);
                    break;
                }
            }

            // Remove view
            ll_selected_groups.removeViewAt(position);

            // Update tags for remaining
            for (int j = 0; j < ll_selected_groups.getChildCount(); j++) {
                ImageView iv_remove = ll_selected_groups.getChildAt(j)
                        .findViewById(R.id.iv_remove_opponent);
                if (iv_remove != null) {
                    iv_remove.setTag(j);
                }
            }

            // Update text
            StringBuilder stringBuilder = new StringBuilder();
            for (GroupsModel model : selected_groups_list) {
                stringBuilder.append(model.getGroup_name()).append(",");
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
            at_add_groups.setText(stringBuilder.toString());

            // Update display
            documentsList.clear();
            updateDisplay();
            AndroidUtils.ToggleButton(temporary_groups_list.size(), btn_add_groups);

            if (selected_groups_list.isEmpty()) {
                selected_tm_list.clear();
                temporary_tm_list.clear();
                ll_assigned_team_members.removeAllViews();
                selected_tm.setVisibility(View.GONE);
            } else {
                callTMWebservice();
            }

            // 🔥 Notify adapter so RecyclerView updates checkboxes
            groupsAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    public void updateDisplay() {
        // Update UI visibility based on the size of the selected groups list
        if (!temporary_groups_list.isEmpty()) {
            ll_add_clients.setVisibility(View.VISIBLE);
            clients_list_layout.setVisibility(View.VISIBLE);
//            temp_client_layout.setVisibility(View.VISIBLE);
            if (tmList.isEmpty()) {
                ll_assign_team_members.setVisibility(View.GONE);
            } else {
                ll_assign_team_members.setVisibility(View.VISIBLE);
            }
            ll_save_buttons.setVisibility(View.VISIBLE);
        } else {
//            clientsList.clear();
//            corp_clients_list.clear();
            tmList.clear();
            ll_selected_groups.removeAllViews();
//            ll_selected_corp_clients.removeAllViews();
//            ll_selected_clients.removeAllViews();
            ll_assigned_team_members.removeAllViews();

//            ll_add_clients.setVisibility(View.GONE);
//            temp_client_layout.setVisibility(View.GONE);
//            clients_list_layout.setVisibility(View.GONE);

            ll_assign_team_members.setVisibility(View.GONE);
            selected_groups.setVisibility(View.GONE);
//            selected_clients.setVisibility(View.GONE);
//            selected_corp_clients.setVisibility(View.GONE);
            selected_tm.setVisibility(View.GONE);
            if (Constants.create_matter)
                ll_save_buttons.setVisibility(View.GONE);
        }
    }

    private void load_existing_matter() {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONObject postdata = new JSONObject();
        WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "matter/" + chosen_matter + "/" + Constants.Matter_id + "/members", "chosen_member", postdata.toString());
    }

    private void load_existing_member_list() throws JSONException {
        JSONObject postdata = new JSONObject();
        postdata.put("attachment_type", "members");
        JSONArray clients = new JSONArray();
        if (!selected_clients_list.isEmpty()) {
            for (int i = 0; i < selected_clients_list.size(); i++) {
                ClientsModel clientsModel = selected_clients_list.get(i);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", clientsModel.getClient_id());
                jsonObject.put("type", clientsModel.getClient_type());
                clients.put(jsonObject);
            }
        }
        if (!selected_corp_clients_list.isEmpty()) {
            for (int i = 0; i < selected_corp_clients_list.size(); i++) {
                ClientsModel clientsModel = selected_corp_clients_list.get(i);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", clientsModel.getClient_id());
                jsonObject.put("type", "corporate");
                clients.put(jsonObject);
            }
        }
        if (!selected_temp_clients_list.isEmpty()) {
            for (int i = 0; i < selected_temp_clients_list.size(); i++) {
                ClientsModel clientsModel = selected_temp_clients_list.get(i);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", clientsModel.getClient_id());
                jsonObject.put("type", "consumer");
                clients.put(jsonObject);
            }
        }
        postdata.put("clients", clients);
        postdata.put("group_acls", Constants.ex_group_attachment);
        if (!Constants.create_matter) {
            selected_groups_list.clear();
            JSONArray groupsArray = Constants.ex_group_attachment;
            for (int i = 0; i < groupsArray.length(); i++) {
                String groupId = groupsArray.getString(i);
                GroupsModel groupsModel = new GroupsModel();
                groupsModel.setGroup_id(groupId);
                selected_groups_list.add(groupsModel);
            }
        }

        WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "matter/attachments", "attachment_members", postdata.toString());
    }

    private void load_existing_clients_list() throws JSONException {
        JSONObject postdata = new JSONObject();
        postdata.put("attachment_type", "clients");
//        postdata.put("group_acls", Constants.ex_group_attachment);
        WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "matter/attachments", "attachment_clients", postdata.toString());
    }

    private void update_matter() throws JSONException {
        corp_client_id = "";
        JSONArray clients = new JSONArray();
        JSONArray corp_clients = new JSONArray();
        JSONArray members = new JSONArray();
        for (int i = 0; i < selected_tm_list.size(); i++) {
            try {
                TeamModel teamModel = selected_tm_list.get(i);
                JSONObject team_object = new JSONObject();
                team_object.put("id", teamModel.getTm_id());
                team_object.put("name", teamModel.getTm_name());
                members.put(team_object);
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        }
        for (int i = 0; i < selected_corp_clients_list.size(); i++) {
            try {
                ClientsModel clientsModel = selected_corp_clients_list.get(i);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", clientsModel.getClient_id());
                jsonObject.put("type", clientsModel.getClient_type());
                jsonObject.put("name", clientsModel.getClient_name());
                corp_clients.put(jsonObject);
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        }
        for (int i = 0; i < selected_clients_list.size(); i++) {
            try {
                ClientsModel clientsModel = selected_clients_list.get(i);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", clientsModel.getClient_id());
                jsonObject.put("type", clientsModel.getClient_type());
                jsonObject.put("name", clientsModel.getClient_name());
                clients.put(jsonObject);
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        }
        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONObject postdata = new JSONObject();
        postdata.put("clients", clients);
        postdata.put("members", members);
        postdata.put("corporate", corp_clients);
//        Log.d("Corp_list_clients", selected_corp_clients_list.get(0).getClient_name());
        WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "matter/" + chosen_matter + "/" + Constants.Matter_id + "/members/update", "matter_update", postdata.toString());
    }

    private void display_existing_members(JSONArray members, JSONArray clients, JSONArray corp_clients) {
        selected_clients_list.clear();
        temporary_clients_list.clear();
        selected_tm_list.clear();
        temporary_tm_list.clear();
        temporary_corpclients_list.clear();
        selected_corp_clients_list.clear();
        old_clients_list.clear();
        try {
            for (int p = 0; p < clients.length(); p++) {
                ClientsModel clientsModel = new ClientsModel();
                JSONObject jsonObject = clients.getJSONObject(p);
                clientsModel.setClient_id(jsonObject.getString("id"));
                clientsModel.setClient_name(jsonObject.getString("name"));
                clientsModel.setClient_type(jsonObject.getString("type"));
                temporary_clients_list.add(clientsModel);
                selected_clients_list.add(clientsModel);
                old_clients_list.add(clientsModel);
            }
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
        try {
            for (int p = 0; p < corp_clients.length(); p++) {
                ClientsModel clientsModel = new ClientsModel();
                JSONObject jsonObject = corp_clients.getJSONObject(p);
                clientsModel.setClient_id(jsonObject.getString("id"));
                clientsModel.setClient_name(jsonObject.getString("name"));
                clientsModel.setClient_type(jsonObject.getString("type"));
                temporary_corpclients_list.add(clientsModel);
                selected_corp_clients_list.add(clientsModel);
            }
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
        try {
            owner_tm.clear();
            TeamModel teamModel1 = new TeamModel();
            teamModel1.setTm_id(Constants.owner_id);
            teamModel1.setTm_name(Constants.owner_name);
            owner_tm.add(teamModel1);

            if (Constants.create_matter) {
                // Always add owner at 0th position
                temporary_tm_list.addAll(0, owner_tm);
                selected_tm_list.addAll(0, owner_tm);
            }

            for (int t = 0; t < members.length(); t++) {
                TeamModel teamModel = new TeamModel();
                JSONObject jsonObject = members.getJSONObject(t);
                teamModel.setTm_id(jsonObject.getString("id"));
                teamModel.setTm_name(jsonObject.getString("name"));

                // Skip duplicate owner entries
                if (teamModel.getTm_id().equals(Constants.owner_id)) {
                    if (!Constants.create_matter) {
                        // Add owner here only if not already added
                        boolean ownerExists = false;
                        for (TeamModel tm : selected_tm_list) {
                            if (tm.getTm_id().equals(Constants.owner_id)) {
                                ownerExists = true;
                                break;
                            }
                        }
                        if (!ownerExists) {
                            temporary_tm_list.add(0, teamModel1);
                            selected_tm_list.add(0, teamModel1);
                        }
                    }
                    continue; // skip adding owner twice
                }

                temporary_tm_list.add(teamModel);
                selected_tm_list.add(teamModel);
            }
        } catch (JSONException e) {
            e.fillInStackTrace();
        }

        try {
//            if (!selected_corp_clients_list.isEmpty()) {
//                selected_clients_list.addAll(selected_corp_clients_list);
//            }
            if (!selected_corp_clients_list.isEmpty())
                temporary_clients_list.addAll(selected_corp_clients_list);
            selected_clients_list.addAll(selected_corp_clients_list);
//            updateDisplay();
            if (!selected_clients_list.isEmpty()) {
//                    callClientsWebservice();
                loadClientsText();
                loadSelectedClients(new String[selected_clients_list.size()]);
            }
            if (!selected_tm_list.isEmpty()) {
//                    callTMWebservice();
                loadTeamText();
                loadSelectedTM(new String[selected_tm_list.size()]);
            }
            loadAddClient();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void call_country_list() {
        JSONObject postdata = new JSONObject();
        WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "countries", "countries", postdata.toString());
//        https://api.staging.digicoffer.com/professional/countries
    }

    private void add_temp_client() {
        try {
            JSONObject jsonObject = new JSONObject();
            if (!Constants.create_matter) {
                jsonObject.put("group_acls", Constants.ex_group_attachment);
            } else {
                JSONArray group_acls = new JSONArray();
                for (int i = 0; i < selected_groups_list.size(); i++) {
                    GroupsModel groupsModel = selected_groups_list.get(i);
                    group_acls.put(groupsModel.getGroup_id());
                }
                jsonObject.put("group_acls", group_acls);
            }
            if (client_type.equals("entity")) {
                jsonObject.put("fullname", Objects.requireNonNull(et_temp_fname.getText()).toString());
                jsonObject.put("contact_person", Objects.requireNonNull(et_temp_lname.getText()).toString());
                jsonObject.put("email", Objects.requireNonNull(et_temp_email.getText()).toString());
                jsonObject.put("country", et_temp_country.getText().toString());
                jsonObject.put("contact_phone", Objects.requireNonNull(et_temp_phone.getText()).toString());
            } else {
                jsonObject.put("first_name", Objects.requireNonNull(et_temp_fname.getText()).toString());
                jsonObject.put("last_name", Objects.requireNonNull(et_temp_lname.getText()).toString());
                jsonObject.put("email", Objects.requireNonNull(et_temp_email.getText()).toString());
                jsonObject.put("country", et_temp_country.getText().toString());
//                jsonObject.put("contact_phone",Objects.requireNonNull(et_temp_phone.getText()).toString());
            }
            //        https://api.staging.digicoffer.com/professional/v3/relationship/temp-invite/consumer
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v3/relationship/temp-invite/" + client_type, "temp_client", jsonObject.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void call_corporate_clients() {
        try {
            JSONObject postdata = new JSONObject();
//            if (!Constants.create_matter) {
//                postdata.put("group_acls", Constants.ex_group_attachment);
//            } else {
//                JSONArray group_acls = new JSONArray();
//                for (int i = 0; i < selected_groups_list.size(); i++) {
//                    GroupsModel groupsModel = selected_groups_list.get(i);
//                    group_acls.put(groupsModel.getGroup_id());
//                }
//                postdata.put("group_acls", group_acls);
//            }
//            postdata.put("attachment_type", "corporate");
//            postdata.put("product", "lauditor");
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/corporate/list", "attachment_corp_clients", postdata.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void check_temp_values() {
        if (!isValidEmail(Objects.requireNonNull(et_temp_email.getText()).toString().trim())) {
            AndroidUtils.showAlert("Please Enter A Valid Email Address", getActivity());
        } else if (!Objects.requireNonNull(et_temp_confirm_email.getText()).toString().trim().equals(Objects.requireNonNull(et_temp_email.getText()).toString().trim())) {
            AndroidUtils.showAlert("Please Enter A Valid Confirm Email Address", getActivity());
        } else {
            add_temp_client();
        }
    }

    private void AddTempClientStatus() {
        is_fname_empty = Objects.requireNonNull(et_temp_fname.getText()).toString().isEmpty();
        is_lname_empty = Objects.requireNonNull(et_temp_fname.getText()).toString().isEmpty();
        is_email_empty = Objects.requireNonNull(et_temp_email.getText()).toString().isEmpty();
        is_confirm_email_empty = Objects.requireNonNull(et_temp_confirm_email.getText()).toString().isEmpty();
        if (is_fname_empty || is_lname_empty || is_email_empty || is_confirm_email_empty || Objects.requireNonNull(et_temp_country.getText()).toString().isEmpty()) {
            btn_add_temp_client.setAlpha(0.5f);
            btn_add_temp_client.setEnabled(false);
        } else {
            btn_add_temp_client.setAlpha(1.0f);
            btn_add_temp_client.setEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {

    }
}
