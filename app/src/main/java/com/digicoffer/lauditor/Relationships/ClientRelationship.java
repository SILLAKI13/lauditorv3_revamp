package com.digicoffer.lauditor.Relationships;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.isMaskedEmail;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.isValidEmail;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.maskEmail;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.maskPhoneNumber;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Relationships.Adapter.IndividualDropdownAdapter;
import com.digicoffer.lauditor.Relationships.Adapter.RelationshipsAdapter;
import com.digicoffer.lauditor.Relationships.Model.CountriesDO;
import com.digicoffer.lauditor.Relationships.Model.EntityModel;
import com.digicoffer.lauditor.Relationships.Model.EntitySearchModel;
import com.digicoffer.lauditor.Relationships.Model.IndividualModel;
import com.digicoffer.lauditor.Relationships.Model.RelationshipsModel;
import com.digicoffer.lauditor.Relationships.Model.SearchModel;
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ClientRelationship extends Fragment implements AsyncTaskCompleteListener, View.OnClickListener, RelationshipsAdapter.EventListener {
    AppCompatButton btn_prev, btn_next, btn_search;
    String Prev_Cursor = "";
    private List<IndividualModel> filteredIndividualList = new ArrayList<>();
    String route = "";
    private AlertDialog relationshipPopup;
    String relationshipId = "", selectedRelId = "";
    private ArrayList<RelationshipsModel> selectedRelationshipsList = new ArrayList<>();
    String Next_Cursor = "";
    String individualId = "";
    LinearLayout tv_switchCreate, tv_switchView;
    private LinearLayout Content_layout, rg_individual_entity, rg_relationship;
    ArrayList<SearchModel> searchModelsList = new ArrayList<>();
    ArrayList<String> entityList = new ArrayList<>();
    ArrayList<ViewGroupModel> updatedMembersList = new ArrayList<>();
    ArrayList<RelationshipsModel> relationshipsList = new ArrayList<>();
    String TAG = "";
    LinearLayout ll_nav_buttons;
    CardView tl_search_relationships;
    CompoundButton.OnCheckedChangeListener selectAllListener;
    RelationshipsAdapter adapter;
    TextView Email_error, ConfirmEmail_error, PhoneNumber_error;
    private boolean iscountry_checked = true;
    private NewModel mViewModel;
    public static String FLAG = "";
    private Dialog progress_dialog;
    LinearLayout ll_email, ll_confirm_email;
    private ListView countryListView;
    TextView email, confirm_email, first_name, last_name, country, Contact_phno, contact_person;
    String value = "";
    String Relationship_Type = "individuals";
    EntitySearchModel entitySearchModel;
    EntityModel entityModel;
    ArrayList<EntityModel> updatedEntityList = new ArrayList<>();
    ArrayList<IndividualModel> updatedIndividualList = new ArrayList<>();
    TextView rb_add_relationship, rb_view_relationships, tv_add_individual, tv_add_entity, tv_add_corporate, tv_view_individual_relationship, tv_view_entity_relationship, tv_view_corporate_relationship, tv_view_temp_relationship;
    LinearLayout ll_entity_name, ll_contact_person, ll_first_name, ll_last_name, ll_contatc_phone, ll_search_individual, ll_search_entity, ll_relationships, ll_select_all, ll_groups;
    TextInputEditText et_search_relationships, et_search_individual, et_search_view_relationships, tv_individual_email, tv_individual_confirm_email, tv_individual_firstname, tv_individual_last_name, tv_entity_name, tv_entity_contact_person, tv_entity_phone_number;
    Button btn_search_individual, btn_relationships_cancel, btn_send_request, btn_search_entity;
    TextView tv_response, entity_name, tv_sp_country;
    AutoCompleteTextView ac_search_entity;
    LinearLayout tl_individual_country;
    ImageView img_dropdown_icon, img_clear_icon, iv_mobile_mandatory;
    String entity_id = "";
    ListView sp_country;
    String negative_msg = " - not found. Please fill in the details below to send relationship invite.";
    String positive_msg = " - found!";
    Groupsadapter_relationship groupsAdapter;
    RecyclerView rv_relationship_groups;
    public RecyclerView rv_relationships;
    SearchModel searchModel;
    private CheckBox chk_select_all;
    ArrayList<ViewGroupModel> groupsList = new ArrayList<>();
    ArrayList<CountriesDO> countriesList = new ArrayList<>();
    private static String RELATIONSHIP_TAG = "INDIVIDUAL";
    CardView cv_details;
    private String country_name;
    LinearLayout ll_country;
    public ScrollView sv_relationships;
    public LinearLayout ll_view_rel;

    // ─── Empty state view ─────────────────────────────────────────────────────
    private LinearLayout layout_relationships_empty_state;

    // Validation state variables
    private boolean isEmailValid = false;
    private boolean isConfirmEmailValid = false;
    private boolean isPhoneValid = false;
    private boolean isFormValid = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.client_relationships, container, false);
        callCountriesWebService();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(NewModel.class);
        Content_layout = view.findViewById(R.id.Content_layout);
        iv_mobile_mandatory = view.findViewById(R.id.iv_mobile_mandatory);
        tv_switchView = view.findViewById(R.id.tv_switchView);
        tv_switchCreate = view.findViewById(R.id.tv_switchCreate);
        Email_error = view.findViewById(R.id.Email_error);
        ll_view_rel = view.findViewById(R.id.ll_view_rel);
        PhoneNumber_error = view.findViewById(R.id.PhoneNumber_error);
        ConfirmEmail_error = view.findViewById(R.id.ConfirmEmail_error);
        rg_relationship = view.findViewById(R.id.view_relationship);
        sv_relationships = (ScrollView) view.findViewById(R.id.sv_relationships);
        rb_add_relationship = view.findViewById(R.id.tv_add_relationship);
        rb_add_relationship.setText(R.string.add_relationships);
        rb_view_relationships = view.findViewById(R.id.tv_view_relationship);
        rb_view_relationships.setText(R.string.view_relationships);
        rg_individual_entity = view.findViewById(R.id.entity);
        tv_add_individual = view.findViewById(R.id.tv_add_individual);
        tv_add_individual.setText(R.string.individual);
        tv_add_entity = view.findViewById(R.id.tv_add_entity);
        tv_add_entity.setText(R.string.entity);
        tv_add_corporate = view.findViewById(R.id.tv_add_corporate);
        tv_add_corporate.setText(R.string.corporate);
        tv_view_individual_relationship = view.findViewById(R.id.tv_view_individual_relationship);
        tv_view_individual_relationship.setText(R.string.individual);
        tv_view_entity_relationship = view.findViewById(R.id.tv_view_entity_relationship);
        tv_view_entity_relationship.setText(R.string.business);
        tv_view_corporate_relationship = view.findViewById(R.id.tv_view_corporate_relationship);
        tv_view_corporate_relationship.setText(R.string.corporate);
        tv_view_temp_relationship = view.findViewById(R.id.tv_view_temp_relationship);
        tv_view_temp_relationship.setText(R.string.deleted);
        if (Constants.ROLE.equals("SU")) {
            tv_view_temp_relationship.setVisibility(VISIBLE);
            tv_view_corporate_relationship.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.radiobutton_centre_background));
        } else {
            tv_view_temp_relationship.setVisibility(GONE);
            tv_view_corporate_relationship.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
        }
        tv_view_individual_relationship.setTextSize(10);
        tv_view_individual_relationship.setMaxLines(1);
        tv_view_entity_relationship.setTextSize(10);
        tv_view_entity_relationship.setMaxLines(1);
        tv_view_corporate_relationship.setTextSize(10);
        tv_view_corporate_relationship.setMaxLines(1);
        tv_view_temp_relationship.setTextSize(10);
        tv_view_temp_relationship.setMaxLines(1);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100);

        rv_relationship_groups = view.findViewById(R.id.rv_relationship_groups);
        rv_relationships = view.findViewById(R.id.rv_relationships);

        // ─── Bind empty state view ────────────────────────────────────────────
        layout_relationships_empty_state = view.findViewById(R.id.layout_relationships_empty_state);

        email = view.findViewById(R.id.email);
        email.setText(R.string.email);
        ll_select_all = view.findViewById(R.id.ll_select_all);
        ll_select_all.setVisibility(GONE);
        ll_email = view.findViewById(R.id.ll_email);
        ll_confirm_email = view.findViewById(R.id.ll_confirm_email);
        confirm_email = view.findViewById(R.id.confirm_email);
        confirm_email.setText(R.string.confirm_email);
        first_name = view.findViewById(R.id.first_name);
        first_name.setText(R.string.first_name);
        last_name = view.findViewById(R.id.last_name);
        last_name.setText(R.string.last_name);
        country = view.findViewById(R.id.country);
        country.setText(R.string.country);
        Contact_phno = view.findViewById(R.id.Contact_phno);
        Contact_phno.setText(R.string.mobile_);
        entity_name = view.findViewById(R.id.entity_name);
        entity_name.setText(R.string.entity_name);
        contact_person = view.findViewById(R.id.contact_person);
        contact_person.setText(R.string.contact_person);
        ll_search_entity = view.findViewById(R.id.ll_search_entity);
        ll_search_individual = view.findViewById(R.id.ll_search_individual);
        ll_search_individual.setVisibility(GONE);
        ll_entity_name = view.findViewById(R.id.ll_entity_name);
        ll_first_name = view.findViewById(R.id.ll_first_name);
        ll_last_name = view.findViewById(R.id.ll_last_name);
        ll_country = view.findViewById(R.id.ll_country);
        ll_groups = view.findViewById(R.id.ll_groups);
        ll_groups.setVisibility(GONE);
        ll_contact_person = view.findViewById(R.id.ll_contact_person);
        ll_contatc_phone = view.findViewById(R.id.ll_entity_number);
        ac_search_entity = view.findViewById(R.id.ac_search_entity);
        ac_search_entity.setHint(R.string.search);
        et_search_individual = view.findViewById(R.id.et_search_individual);
        et_search_individual.setHint(R.string.search);
        et_search_individual.addTextChangedListener(new Validation(et_search_individual));
        btn_search_individual = view.findViewById(R.id.btn_search_individual);
        btn_search_individual.setText(R.string.search);
        btn_search_entity = view.findViewById(R.id.btn_search_entity);
        btn_search_entity.setText(R.string.search);
        et_search_relationships = view.findViewById(R.id.et_search_relationships);
        et_search_relationships.setHint(R.string.search_groups);
        et_search_relationships.addTextChangedListener(new Validation(et_search_relationships));
        tv_response = view.findViewById(R.id.tv_response);
        cv_details = view.findViewById(R.id.cv_details);
        chk_select_all = view.findViewById(R.id.chk_select_all);
        tv_individual_email = view.findViewById(R.id.tv_individual_email);
        tv_individual_email.setHint(R.string.email);
        tv_individual_email.addTextChangedListener(new Validation(tv_individual_email));
        tv_entity_contact_person = view.findViewById(R.id.tv_entity_contact_person);
        tv_entity_contact_person.setHint(R.string.contact_person);
        tv_entity_contact_person.addTextChangedListener(new Validation(tv_entity_contact_person));
        tv_entity_name = view.findViewById(R.id.tv_entity_name);
        tv_entity_name.setHint(R.string.entity_name);
        tv_entity_name.addTextChangedListener(new Validation(tv_entity_name));
        tv_entity_phone_number = view.findViewById(R.id.tv_entity_phone_number);
        tv_entity_phone_number.setHint(R.string.mobile_);
        iv_mobile_mandatory.setVisibility(GONE);
        tv_entity_contact_person.addTextChangedListener(new Validation(tv_entity_contact_person));
        ll_relationships = view.findViewById(R.id.ll_relationships);
        tv_individual_confirm_email = view.findViewById(R.id.tv_individual_confirm_email);
        tv_individual_confirm_email.setHint(R.string.confirm_email);
        tv_individual_confirm_email.addTextChangedListener(new Validation(tv_individual_confirm_email));

        tl_individual_country = view.findViewById(R.id.tl_individual_country);
        tv_sp_country = tl_individual_country.findViewById(R.id.tv_spinner_view);
        img_clear_icon = tl_individual_country.findViewById(R.id.img_clear_icon);
        img_clear_icon.setEnabled(false);
        img_dropdown_icon = tl_individual_country.findViewById(R.id.img_dropdown_icon);

        sp_country = view.findViewById(R.id.sp_country);
        tv_individual_firstname = view.findViewById(R.id.tv_individual_firstname);
        tv_individual_firstname.setHint(R.string.first_name);
        tv_individual_firstname.addTextChangedListener(new Validation(tv_individual_firstname));

        tv_individual_last_name = view.findViewById(R.id.tv_individual_last_name);
        tv_individual_last_name.setHint(R.string.last_name);
        tv_individual_last_name.addTextChangedListener(new Validation(tv_individual_last_name));
        btn_send_request = view.findViewById(R.id.btn_send_request);
        btn_send_request.setText(R.string.send_request);
        btn_send_request.setAlpha(0.5f);
        btn_send_request.setEnabled(false);
        btn_relationships_cancel = view.findViewById(R.id.btn_relationships_cancel);
        rg_individual_entity.setVisibility(VISIBLE);

        tl_individual_country.setOnClickListener(this);
        rb_add_relationship.setOnClickListener(this);
        rb_view_relationships.setOnClickListener(this);
        tv_add_individual.setOnClickListener(this);
        tv_add_entity.setOnClickListener(this);
        tv_add_corporate.setOnClickListener(this);
        tv_view_individual_relationship.setOnClickListener(this);
        tv_view_entity_relationship.setOnClickListener(this);
        tv_view_corporate_relationship.setOnClickListener(this);
        tv_view_temp_relationship.setOnClickListener(this);
        btn_search_individual.setOnClickListener(this);
        btn_search_entity.setOnClickListener(this);
        ll_nav_buttons = view.findViewById(R.id.ll_nav_buttons);
        ll_nav_buttons.setVisibility(GONE);
        btn_prev = view.findViewById(R.id.btn_prev);
        btn_prev.setText(R.string.prev_);
        btn_next = view.findViewById(R.id.btn_next);
        btn_next.setText(R.string.next_);
        tl_search_relationships = view.findViewById(R.id.tl_search_relationships);
        btn_search = tl_search_relationships.findViewById(R.id.btn_search);
        et_search_view_relationships = tl_search_relationships.findViewById(R.id.et_search_tm);
        et_search_view_relationships.setHint(R.string.search_relationship);
        et_search_view_relationships.setTextSize(DynamicUtils.fifteen);
        et_search_view_relationships.addTextChangedListener(new Validation(et_search_view_relationships));

        enableAlpha();
        disableIndividualData();
        AndroidUtils.setupModuleView(
                tv_switchCreate,
                getString(R.string.view_relationships),
                false, true, getContext(), getString(R.string.add_relationships) + " - " + getString(R.string.individual),
                clickedView -> {
                    ShowCreateSelection();
                }
        );
        AndroidUtils.setupModuleView(
                tv_switchView,
                getString(R.string.add_relationships),
                true, true, getContext(), getString(R.string.view_relationships) + " - " + getString(R.string.individual),
                clickedView -> {
                    if (!Constants.is_active) {
                        AndroidUtils.showRenewalPopup(getActivity());
                    } else {
                        showAddRelationshipUI();
                    }
                }
        );
        if (Constants.isCreate) {
            showAddRelationshipUI();
        } else {
            tv_switchCreate.setVisibility(GONE);
            tv_switchView.setVisibility(VISIBLE);
            if (Constants.isFromNotification) {
                handleNotificationNavigation();
            }
            if (Constants.Rel_Type.equals("Entity")) {
                View_Entity();
            } else if (Constants.Rel_Type.equals("Deleted")) {
                View_deleted();
            } else if (Constants.Rel_Type.equals("Corporate")) {
                View_corporate();
            } else {
                View_Individual();
            }
            mViewModel.setData(getString(R.string.view_relationships));
        }
        img_clear_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.DisplaySpinnerView(sp_country, tv_sp_country, country_name, img_dropdown_icon, img_clear_icon, false);
                iscountry_checked = true;
                updateSendButtonState();
            }
        });

        sp_country.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                country_name = countriesList.get(position).getName();
                iscountry_checked = true;
                AndroidUtils.DisplaySpinnerView(sp_country, tv_sp_country, country_name, img_dropdown_icon, img_clear_icon, true);
                updateSendButtonState();
            }
        });

        setupEmailValidation();
        setupConfirmEmailValidation();
        setupPhoneValidation();
        setupFormFieldValidation();

        selectAllListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                groupsAdapter.selectOrDeselectAll(isChecked);
            }
        };
        btn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callViewRelationshipWebservice("before", Prev_Cursor);
            }
        });
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callViewRelationshipWebservice("after", Next_Cursor);
            }
        });

        btn_relationships_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_search_relationships.setText("");
                groupsList.clear();
                clearIndividualData();
                tv_response.setText("");
                enableAlpha();
                ll_groups.setVisibility(GONE);
            }
        });
        btn_send_request.setOnClickListener(view1 -> {
            if (RELATIONSHIP_TAG.equals("INDIVIDUAL")) {
                individualValidationClick();
            } else if (RELATIONSHIP_TAG.equals("ENTITY")) {
                RELATIONSHIP_TAG = "ENTITY";
                entityValidationClick();
            } else {
                entityValidationClick();
            }
        });
        chk_select_all.setOnCheckedChangeListener(selectAllListener);
    }

    // ─── Empty State toggle ───────────────────────────────────────────────────

    /**
     * Shows the empty-state illustration when the relationships list is empty,
     * and hides it (showing the RecyclerView) when items are present.
     */
    private void toggleRelationshipsEmptyState(boolean isEmpty) {
        if (layout_relationships_empty_state == null) return;
        if (isEmpty) {
            layout_relationships_empty_state.setVisibility(VISIBLE);
            ll_nav_buttons.setVisibility(GONE);
            rv_relationships.setVisibility(GONE);
        } else {
            layout_relationships_empty_state.setVisibility(GONE);
            rv_relationships.setVisibility(VISIBLE);
            ll_nav_buttons.setVisibility(VISIBLE);
        }
    }

    private void setupEmailValidation() {
        tv_individual_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Email_error.setVisibility(GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString().trim();

                if (isMaskedEmail(email)) {
                    Email_error.setVisibility(GONE);
                    isEmailValid = true;
                    updateSendButtonState();
                    return;
                }

                if (!email.isEmpty()) {
                    if (isValidEmail(email)) {
                        Email_error.setVisibility(GONE);
                        isEmailValid = true;
                    } else {
                        Email_error.setText(R.string.enter_a_valid_email_address);
                        Email_error.setVisibility(VISIBLE);
                        isEmailValid = false;
                    }
                } else {
                    isEmailValid = false;
                }

                String confirmEmail = tv_individual_confirm_email.getText().toString().trim();
                if (!confirmEmail.isEmpty() && !email.equals(confirmEmail)) {
                    ConfirmEmail_error.setText(Constants.cemail_alert);
                    ConfirmEmail_error.setVisibility(VISIBLE);
                    isConfirmEmailValid = false;
                } else if (!confirmEmail.isEmpty() && email.equals(confirmEmail)) {
                    ConfirmEmail_error.setVisibility(GONE);
                    isConfirmEmailValid = true;
                } else if (confirmEmail.isEmpty()) {
                    isConfirmEmailValid = false;
                }

                updateSendButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupConfirmEmailValidation() {
        tv_individual_confirm_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                ConfirmEmail_error.setVisibility(GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = tv_individual_email.getText().toString().trim();
                String confirmEmail = s.toString().trim();

                if (!confirmEmail.isEmpty()) {
                    if (email.equals(confirmEmail)) {
                        ConfirmEmail_error.setVisibility(GONE);
                        isConfirmEmailValid = true;
                    } else {
                        ConfirmEmail_error.setText(Constants.cemail_alert);
                        ConfirmEmail_error.setVisibility(VISIBLE);
                        isConfirmEmailValid = false;
                    }
                } else {
                    isConfirmEmailValid = false;
                }

                updateSendButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupPhoneValidation() {
        tv_entity_phone_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                PhoneNumber_error.setVisibility(GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    if (s.toString().length() < 10) {
                        PhoneNumber_error.setText("Please enter a 10 digit valid mobile number");
                        PhoneNumber_error.setVisibility(VISIBLE);
                        isPhoneValid = false;
                    } else {
                        PhoneNumber_error.setVisibility(GONE);
                        isPhoneValid = true;
                    }
                } else {
                    isPhoneValid = false;
                }

                updateSendButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupFormFieldValidation() {
        tv_individual_firstname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendButtonState();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        tv_individual_last_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendButtonState();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        tv_entity_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendButtonState();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        tv_entity_contact_person.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendButtonState();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateSendButtonState() {
        if (RELATIONSHIP_TAG.equals("INDIVIDUAL")) {
            if (individualId.isEmpty()) {
                boolean firstNameValid = !tv_individual_firstname.getText().toString().trim().isEmpty();
                boolean lastNameValid = !tv_individual_last_name.getText().toString().trim().isEmpty();
                boolean emailValid = isEmailValid && !tv_individual_email.getText().toString().trim().isEmpty();
                boolean confirmEmailValid = isConfirmEmailValid;
                boolean countryValid = !tv_sp_country.getText().toString().trim().isEmpty();

                isFormValid = firstNameValid && lastNameValid && emailValid && confirmEmailValid && countryValid;
            } else {
                isFormValid = true;
            }
        } else if (RELATIONSHIP_TAG.equals("ENTITY") || RELATIONSHIP_TAG.equals("CORPORATE")) {
            if (entity_id.isEmpty()) {
                boolean entityNameValid = !tv_entity_name.getText().toString().trim().isEmpty();
                boolean contactPersonValid = !tv_entity_contact_person.getText().toString().trim().isEmpty();
                boolean emailValid = isEmailValid && !tv_individual_email.getText().toString().trim().isEmpty();
                boolean confirmEmailValid = isConfirmEmailValid;
                boolean countryValid = !tv_sp_country.getText().toString().trim().isEmpty();
                boolean phoneValid = isPhoneValid || tv_entity_phone_number.getText().toString().trim().isEmpty();

                isFormValid = entityNameValid && contactPersonValid && emailValid && confirmEmailValid && countryValid && phoneValid;
            } else {
                isFormValid = true;
            }
        }

        btn_send_request.setEnabled(isFormValid);
        btn_send_request.setAlpha(isFormValid ? 1.0f : 0.5f);
    }

    private void resetValidationStates() {
        isEmailValid = false;
        isConfirmEmailValid = false;
        isPhoneValid = false;
        isFormValid = false;
        updateSendButtonState();
    }

    private void ShowViewSelection() {
        switch (Relationship_Type) {
            case "corporate":
                Add_Corporate();
                break;
            case "business":
                Add_Entity();
                break;
            default:
                Add_Individual();
                break;
        }
    }

    private void ShowCreateSelection() {
        tv_switchCreate.setVisibility(GONE);
        tv_switchView.setVisibility(VISIBLE);
        switch (RELATIONSHIP_TAG) {
            case "CORPORATE":
                View_corporate();
                break;
            case "ENTITY":
                View_Entity();
                break;
            default:
                View_Individual();
                break;
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.tl_individual_country) {
            AndroidUtils.display_listview(iscountry_checked, sp_country);
            iscountry_checked = !iscountry_checked;
        } else if (viewId == R.id.tv_add_relationship) {
            showAddRelationshipUI();
        } else if (viewId == R.id.tv_view_relationship) {
            ShowCreateSelection();
            mViewModel.setData(getString(R.string.view_relationships));
        } else if (viewId == R.id.tv_add_individual) {
            Add_Individual();
        } else if (viewId == R.id.tv_add_corporate) {
            Add_Corporate();
        } else if (viewId == R.id.tv_add_entity) {
            Add_Entity();
        } else if (viewId == R.id.btn_search_individual) {
            searchIndividual();
        } else if (viewId == R.id.btn_search_entity) {
            searchEntity();
        } else if (viewId == R.id.tv_view_corporate_relationship) {
            relationshipId = "";
            selectedRelId = "";
            View_corporate();
        } else if (viewId == R.id.tv_view_temp_relationship) {
            relationshipId = "";
            selectedRelId = "";
            View_deleted();
        } else if (viewId == R.id.tv_view_individual_relationship) {
            relationshipId = "";
            selectedRelId = "";
            View_Individual();
        } else if (viewId == R.id.tv_view_entity_relationship) {
            relationshipId = "";
            selectedRelId = "";
            View_Entity();
        }
    }

    private void showAddRelationshipUI() {
        rg_individual_entity.setVisibility(VISIBLE);
        rg_relationship.setVisibility(GONE);
        updateButtonStyles(rb_add_relationship, R.drawable.button_left_green_background, R.color.white);
        updateButtonStyles(rb_view_relationships, R.drawable.button_right_background, R.color.black);
        ll_relationships.setVisibility(GONE);
        ll_contatc_phone.setVisibility(VISIBLE);
        cv_details.setVisibility(VISIBLE);
        ll_groups.setVisibility(GONE);
        relationshipsList.clear();
        enableAlpha();
        clearIndividualFields();
        ShowViewSelection();
        mViewModel.setData(getString(R.string.add_relationships));
        tv_switchView.setVisibility(GONE);
        tv_switchCreate.setVisibility(VISIBLE);
        resetValidationStates();
    }

    private void handleNotificationNavigation() {
        Bundle bundle = Constants.notificationBundle;
        if (bundle == null) return;
        route = bundle.getString(Constants.NavKeys.ROUTE_NAME);
        if (route != null) {
            ArrayList<String> highlightList = bundle.getStringArrayList(Constants.NavKeys.HIGHLIGHT_IDS);
            if (highlightList != null && !highlightList.isEmpty()) {
                relationshipId = highlightList.get(0);
            }
            selectedRelId = bundle.getString(Constants.NavKeys.RELATIONSHIP_ID);
        }
    }

    private void clearIndividualFields() {
        et_search_individual.setText("");
        tl_individual_country.setEnabled(false);
        img_clear_icon.setEnabled(false);
        tv_response.setText("");
        tv_individual_email.setText("");
        tv_individual_email.setEnabled(false);
        tv_individual_confirm_email.setEnabled(false);
        tv_entity_phone_number.setEnabled(false);
        tv_entity_phone_number.setText("");
        AndroidUtils.NumberFilter(tv_entity_phone_number, true);
        tv_individual_confirm_email.setText("");
        tv_individual_firstname.setText("");
        tv_individual_last_name.setText("");
        sp_country.clearFocus();
        sp_country.setVisibility(GONE);
        rv_relationships.removeAllViews();
        resetValidationStates();
    }

    private void updateButtonStyles(TextView button, int backgroundResId, int textColorResId) {
        button.setBackgroundDrawable(getContext().getResources().getDrawable(backgroundResId));
        button.setTextColor(getContext().getResources().getColor(textColorResId));
    }

    private void Add_Entity() {
        RELATIONSHIP_TAG = "ENTITY";
        mViewModel.setData(getString(R.string.entity));
        AndroidUtils.updateModuleTitle(
                tv_switchCreate,
                getString(R.string.add_relationships) + " - " + getString(R.string.entity)
        );
        tv_entity_name.setText("");
        ll_search_individual.setVisibility(GONE);
        ll_search_entity.setVisibility(VISIBLE);
        ll_first_name.setVisibility(GONE);
        ll_last_name.setVisibility(GONE);
        ll_groups.setVisibility(GONE);
        ac_search_entity.setText("");
        tv_response.setText("");
        btn_send_request.setAlpha(0.5f);
        btn_send_request.setEnabled(false);
        Contact_phno.setText(R.string.contact_phone_number_);
        tv_entity_phone_number.setHint(R.string.contact_phone_number_);
        iv_mobile_mandatory.setVisibility(VISIBLE);
        callSearchWebService("entity");
        enableAlpha();
        unHideEntityData();
        clearIndividualData();
        configureEntityView();
        clearSearch();
        resetValidationStates();
    }

    private void clearSearch() {
        if (RELATIONSHIP_TAG.equals("INDIVIDUAL")) {
            filteredIndividualList.clear();
            IndividualDropdownAdapter adapter = new IndividualDropdownAdapter(requireContext(), filteredIndividualList);
            adapter.notifyDataSetChanged();
            ac_search_entity.setAdapter(adapter);
            ac_search_entity.dismissDropDown();
        }
    }

    private void searchIndividual() {
        try {
            value = ac_search_entity.getText().toString().trim();
            String email = Objects.requireNonNull(et_search_individual.getText()).toString().trim();
            if (email.isEmpty()) {
                AndroidUtils.showAlert("Please check the search field..,", getActivity());
            } else {
                disableAlpha();
                tv_individual_email.setText(maskEmail(email));
                tv_individual_confirm_email.setText(maskEmail(email));
                ll_groups.setVisibility(GONE);
                tv_individual_firstname.setText("");
                tv_individual_last_name.setText("");
                callSearchIndividualWebservice();
            }
        } catch (JSONException e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void resetIndividualSearchFields() {
        tv_individual_email.setText("");
        tv_individual_confirm_email.setText("");
        tv_response.setText("");
        et_search_relationships.setText("");
        enableAlpha();
        ll_groups.setVisibility(GONE);
    }

    private void searchEntity() {
        String entityName = ac_search_entity.getText().toString().trim();
        entity_id = "";
        individualId = "";
        try {
            if (!entityName.isEmpty()) {
                value = entityName;
                if (RELATIONSHIP_TAG.equals("INDIVIDUAL")) {
                    callSearchIndividualWebservice();
                } else {
                    chk_select_all.setVisibility(GONE);
                    Search_Entity();
                }
            } else {
                value = "";
                AndroidUtils.showAlert("Please check the search field..,", getActivity());
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void View_Entity() {
        et_search_relationships.setText("");
        et_search_view_relationships.setText("");
        Relationship_Type = "business";
        mViewModel.setData(getString(R.string.business));
        updateRelationshipButtons(tv_view_individual_relationship, R.drawable.button_left_background, Color.BLACK);
        updateRelationshipButtons(tv_view_entity_relationship, R.drawable.radiobutton_centre_green_background, Color.WHITE);
        if (Constants.ROLE.equals("SU")) {
            tv_view_temp_relationship.setVisibility(VISIBLE);
            updateRelationshipButtons(tv_view_corporate_relationship, R.drawable.radiobutton_centre_background, Color.BLACK);
        } else {
            tv_view_temp_relationship.setVisibility(GONE);
            updateRelationshipButtons(tv_view_corporate_relationship, R.drawable.button_right_background, Color.BLACK);
        }
        updateRelationshipButtons(tv_view_temp_relationship, R.drawable.button_right_background, Color.BLACK);
        AndroidUtils.updateModuleTitle(
                tv_switchView,
                getString(R.string.view_relationships) + " - " + getString(R.string.business)
        );
        viewRelationshipsData();
        clearIndividualData();
    }

    private void updateRelationshipButtons(TextView button, int backgroundResId, int textColor) {
        button.setBackground(getContext().getResources().getDrawable(backgroundResId));
        button.setTextColor(textColor);
    }

    private void View_temp() {
        et_search_relationships.setText("");
        et_search_view_relationships.setText("");
        updateRelationshipButtons(tv_view_individual_relationship, R.drawable.button_left_background, Color.BLACK);
        updateRelationshipButtons(tv_view_entity_relationship, R.drawable.radiobutton_centre_background, Color.BLACK);
        updateRelationshipButtons(tv_view_corporate_relationship, R.drawable.button_right_green_background, Color.BLACK);
        updateRelationshipButtons(tv_view_temp_relationship, R.drawable.button_right_green_background, Color.WHITE);
        Relationship_Type = "temp";
        mViewModel.setData(getString(R.string.temporary_clients));
        viewRelationshipsData();
        clearIndividualData();
    }

    private void View_deleted() {
        et_search_relationships.setText("");
        et_search_view_relationships.setText("");
        updateRelationshipButtons(tv_view_individual_relationship, R.drawable.button_left_background, Color.BLACK);
        updateRelationshipButtons(tv_view_entity_relationship, R.drawable.radiobutton_centre_background, Color.BLACK);
        updateRelationshipButtons(tv_view_corporate_relationship, R.drawable.radiobutton_centre_background, Color.BLACK);
        updateRelationshipButtons(tv_view_temp_relationship, R.drawable.button_right_green_background, Color.WHITE);
        Relationship_Type = "deleted";
        mViewModel.setData(getString(R.string.deleted_relationship));
        AndroidUtils.updateModuleTitle(
                tv_switchView,
                getString(R.string.view_relationships) + " - " + getString(R.string.deleted)
        );
        viewRelationshipsData();
        clearIndividualData();
    }

    private void View_corporate() {
        et_search_relationships.setText("");
        et_search_view_relationships.setText("");
        updateRelationshipButtons(tv_view_individual_relationship, R.drawable.button_left_background, Color.BLACK);
        updateRelationshipButtons(tv_view_entity_relationship, R.drawable.radiobutton_centre_background, Color.BLACK);
        if (Constants.ROLE.equals("SU")) {
            tv_view_temp_relationship.setVisibility(VISIBLE);
            updateRelationshipButtons(tv_view_corporate_relationship, R.drawable.radiobutton_centre_green_background, Color.WHITE);
        } else {
            tv_view_temp_relationship.setVisibility(GONE);
            updateRelationshipButtons(tv_view_corporate_relationship, R.drawable.button_right_green_background, Color.WHITE);
        }
        updateRelationshipButtons(tv_view_temp_relationship, R.drawable.button_right_background, Color.BLACK);
        Relationship_Type = "corporate";
        mViewModel.setData(getString(R.string.corporate));
        AndroidUtils.updateModuleTitle(
                tv_switchView,
                getString(R.string.view_relationships) + " - " + getString(R.string.corporate)
        );
        viewRelationshipsData();
        clearIndividualData();
    }

    private void configureEntityView() {
        updateAddButtons(tv_add_corporate, R.drawable.button_right_background, R.color.black);
        updateAddButtons(tv_add_individual, R.drawable.button_left_background, R.color.black);
        updateAddButtons(tv_add_entity, R.drawable.radiobutton_centre_green_background, R.color.white);
        ((LinearLayout) ll_country.getParent()).removeView(ll_country);
        ((LinearLayout) ll_contact_person.getParent()).addView(ll_country, 2);
    }

    private void Add_Corporate() {
        tv_entity_name.setText("");
        btn_send_request.setAlpha(0.5f);
        btn_send_request.setEnabled(false);
        RELATIONSHIP_TAG = "CORPORATE";
        mViewModel.setData(getString(R.string.corporate));
        callSearchWebService("corporate");
        updateAddButtons(tv_add_corporate, R.drawable.button_right_green_background, R.color.white);
        updateAddButtons(tv_add_individual, R.drawable.button_left_background, R.color.black);
        updateAddButtons(tv_add_entity, R.drawable.radiobutton_centre_background, R.color.black);
        AndroidUtils.updateModuleTitle(
                tv_switchCreate,
                getString(R.string.add_relationships) + " - " + getString(R.string.corporate)
        );
        unHideEntityData();
        clearIndividualData();
        enableAlpha();
        setupCorporateView();
        resetValidationStates();
    }

    private void setupCorporateView() {
        ll_search_individual.setVisibility(GONE);
        ll_search_entity.setVisibility(VISIBLE);
        ll_first_name.setVisibility(GONE);
        ll_last_name.setVisibility(GONE);
        ll_confirm_email.setVisibility(VISIBLE);
        ll_groups.setVisibility(GONE);
        ac_search_entity.setText("");
        clearSearch();
        tv_response.setText("");
        ((LinearLayout) ll_country.getParent()).removeView(ll_country);
        ((LinearLayout) ll_contact_person.getParent()).addView(ll_country, 2);
    }

    private void Add_Individual() {
        btn_send_request.setAlpha(0.5f);
        btn_send_request.setEnabled(false);
        RELATIONSHIP_TAG = "INDIVIDUAL";
        mViewModel.setData(getString(R.string.individual));
        AndroidUtils.updateModuleTitle(
                tv_switchCreate,
                getString(R.string.add_relationships) + " - " + getString(R.string.individual)
        );
        clearIndividualFields();
        HideEntityData();
        updatedEntityList.clear();
        entityList.clear();
        ll_search_individual.setVisibility(GONE);
        ll_search_entity.setVisibility(VISIBLE);
        ll_first_name.setVisibility(VISIBLE);
        ll_last_name.setVisibility(VISIBLE);
        ll_confirm_email.setVisibility(VISIBLE);
        tv_entity_name.setText("");
        updateAddButtons(tv_add_individual, R.drawable.button_left_green_background, R.color.white);
        updateAddButtons(tv_add_entity, R.drawable.radiobutton_centre_background, R.color.black);
        updateAddButtons(tv_add_corporate, R.drawable.button_right_background, R.color.black);
        enableAlpha();
        ll_contatc_phone.setVisibility(VISIBLE);
        ll_groups.setVisibility(GONE);
        clearIndividualData();
        Contact_phno.setText(R.string.mobile_);
        tv_entity_phone_number.setHint(R.string.mobile_);
        iv_mobile_mandatory.setVisibility(GONE);
        ((LinearLayout) ll_country.getParent()).removeView(ll_country);
        ((LinearLayout) ll_last_name.getParent()).addView(ll_country);
        resetValidationStates();
    }

    private void updateAddButtons(TextView button, int backgroundResId, int textColorResId) {
        button.setBackgroundDrawable(getContext().getResources().getDrawable(backgroundResId));
        button.setTextColor(getContext().getResources().getColor(textColorResId));
    }

    private void View_Individual() {
        if (progress_dialog == null || !progress_dialog.isShowing()) {
            if (progress_dialog == null || !progress_dialog.isShowing()) {
                progress_dialog = AndroidUtils.get_progress(getActivity());
            }
        }
        et_search_relationships.setText("");
        et_search_view_relationships.setText("");
        updateRelationshipButtons(tv_view_individual_relationship, R.drawable.button_left_green_background, Color.WHITE);
        updateRelationshipButtons(tv_view_entity_relationship, R.drawable.radiobutton_centre_background, Color.BLACK);
        if (Constants.ROLE.equals("SU")) {
            tv_view_temp_relationship.setVisibility(VISIBLE);
            updateRelationshipButtons(tv_view_corporate_relationship, R.drawable.radiobutton_centre_background, Color.BLACK);
        } else {
            tv_view_temp_relationship.setVisibility(GONE);
            updateRelationshipButtons(tv_view_corporate_relationship, R.drawable.button_right_background, Color.BLACK);
        }
        updateRelationshipButtons(tv_view_temp_relationship, R.drawable.button_right_background, Color.BLACK);
        Relationship_Type = "individuals";
        mViewModel.setData(getString(R.string.individual));
        AndroidUtils.updateModuleTitle(
                tv_switchView,
                getString(R.string.view_relationships) + " - " + getString(R.string.individual)
        );
        viewRelationshipsData();
    }

    private void Search_Entity() {
        try {
            ll_groups.setVisibility(GONE);
            disableAlpha();
            clearIndividualData();
            callSearchEntityWebservice();
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    private void callSearchEntityWebservice() throws JSONException {
        JSONObject postdata = new JSONObject();
        for (int j = 0; j < updatedEntityList.size(); j++) {
            if (value.equals(updatedEntityList.get(j).getName())) {
                entity_id = updatedEntityList.get(j).getEntityID();
            }
        }
        if (entity_id.isEmpty()) {
            if (RELATIONSHIP_TAG.equals("CORPORATE")) {
                enableAlpha();
                ll_groups.setVisibility(GONE);
                if (tv_response != null && getContext() != null) {
                    tv_response.setText(value + " - not found.");
                    tv_response.setTextColor(getContext().getResources().getColor(R.color.Red));
                }
                btn_send_request.setAlpha(0.5f);
                btn_send_request.setEnabled(false);
                ll_confirm_email.setVisibility(VISIBLE);
            } else {
                country_name = "";
                tv_sp_country.setText("");
                iscountry_checked = true;
                ll_confirm_email.setVisibility(VISIBLE);
                if (tv_response != null && getContext() != null) {
                    tv_response.setText(value + negative_msg);
                    tv_response.setTextColor(getContext().getResources().getColor(R.color.Red));
                }
                clearIndividualData();
                tv_entity_name.setText(value);
                disableAlpha();
                if (tv_entity_name != null) {
                    tv_entity_name.setEnabled(true);
                    tv_entity_name.setFocusable(true);
                    tv_entity_name.setFocusableInTouchMode(true);
                }
                if (tv_entity_contact_person != null) {
                    tv_entity_contact_person.setEnabled(true);
                    tv_entity_contact_person.setFocusable(true);
                    tv_entity_contact_person.setFocusableInTouchMode(true);
                }
                if (tv_individual_email != null) {
                    tv_individual_email.setEnabled(true);
                    tv_individual_email.setFocusable(true);
                    tv_individual_email.setFocusableInTouchMode(true);
                }
                if (tv_individual_confirm_email != null) {
                    tv_individual_confirm_email.setEnabled(true);
                    tv_individual_confirm_email.setFocusable(true);
                    tv_individual_confirm_email.setFocusableInTouchMode(true);
                }
                if (sp_country != null) {
                    sp_country.setEnabled(true);
                    img_clear_icon.setEnabled(true);
                    tl_individual_country.setEnabled(true);
                    sp_country.setSelection(0);
                    sp_country.setVisibility(GONE);
                }
                if (tv_entity_phone_number != null) {
                    tv_entity_phone_number.setFocusable(true);
                    tv_entity_phone_number.setEnabled(true);
                    tv_entity_phone_number.setFocusableInTouchMode(true);
                }
                ll_confirm_email.setVisibility(VISIBLE);
            }
        } else {
            if (progress_dialog == null || !progress_dialog.isShowing()) {
                progress_dialog = AndroidUtils.get_progress(getActivity());
            }
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v2/relationship/entity/" + entity_id, "Search Entity", postdata.toString());
        }
    }

    private void callGroupsWebservice() {
        JSONObject postdata = new JSONObject();
        WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/groups", "Get Groups", postdata.toString());
    }

    private void callSearchIndividualWebservice() throws JSONException {
        if (progress_dialog == null || !progress_dialog.isShowing()) {
            progress_dialog = AndroidUtils.get_progress(getActivity());
        }
        JSONObject postdata = new JSONObject();
        postdata.put("search", ac_search_entity.getText().toString().trim());
        WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v3/relationship/search/consumer", "Search Consumer", postdata.toString());
    }

    public void viewRelationshipsData() {
        rv_relationships.setVisibility(GONE);
        rg_individual_entity.setVisibility(GONE);
        rg_relationship.setVisibility(GONE);
        rb_add_relationship.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
        rb_view_relationships.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_count));
        rb_add_relationship.setTextColor(getContext().getResources().getColor(R.color.black));
        rb_view_relationships.setTextColor(getContext().getResources().getColor(R.color.white));
        ll_relationships.setVisibility(VISIBLE);
        cv_details.setVisibility(GONE);
        ll_groups.setVisibility(GONE);
        // Reset empty state before loading new data
        toggleRelationshipsEmptyState(false);
        if (Constants.isFromNotification)
            callViewRelationshipWebservice("", "", relationshipId);
        else callViewRelationshipWebservice("", "");
        Constants.isFromNotification = false;
        Constants.notificationBundle.clear();
    }

    public void callViewRelationshipWebservice(String NavPosition, String id, String anchorId) {
        if (progress_dialog == null || !progress_dialog.isShowing()) {
            if (progress_dialog == null || !progress_dialog.isShowing()) {
                progress_dialog = AndroidUtils.get_progress(getActivity());
            }
        }
        JSONObject postdata = new JSONObject();
        String url;
        switch (Relationship_Type) {
            case "corporate":
                if (!anchorId.isEmpty()) {
                    url = "v3/corporate?" + "paginate=true&anchor_id=" + anchorId;
                } else if (!NavPosition.isEmpty()) {
                    url = "v3/corporate?" + NavPosition + "=" + id + "&paginate=true&anchor_id=" + anchorId;
                } else if (!Objects.requireNonNull(et_search_view_relationships.getText()).toString().isEmpty()) {
                    url = "v3/corporate?" + NavPosition + "=" + id + "&paginate=true" + "&search=" + Objects.requireNonNull(et_search_view_relationships.getText()).toString();
                } else {
                    url = "v3/corporate?paginate=true";
                }
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, url, "View Corporate Relationships", postdata.toString());
                break;
            case "temp":
                if (!anchorId.isEmpty()) {
                    url = "v3/tempclient?" + "paginate=true&anchor_id=" + anchorId;
                } else if (!NavPosition.isEmpty()) {
                    url = "v3/tempclient?" + NavPosition + "=" + id + "&paginate=true";
                } else if (!Objects.requireNonNull(et_search_view_relationships.getText()).toString().isEmpty()) {
                    url = "v3/tempclient?" + NavPosition + "=" + id + "&paginate=true" + "&search=" + Objects.requireNonNull(et_search_view_relationships.getText()).toString();
                } else {
                    url = "v3/tempclient?paginate=true";
                }
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, url, "View Temp Relationships", postdata.toString());
                break;
            case "deleted":
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v2/relationship/delete/list", "View Deleted Relationships", postdata.toString());
                break;
            default:
                if (!anchorId.isEmpty()) {
                    url = "v2/relationship/" + Relationship_Type + "?" + "paginate=true&anchor_id=" + anchorId;
                } else if (!NavPosition.isEmpty()) {
                    url = "v2/relationship/" + Relationship_Type + "?" + NavPosition + "=" + id + "&paginate=true";
                } else if (!Objects.requireNonNull(et_search_view_relationships.getText()).toString().isEmpty()) {
                    url = "v2/relationship/" + Relationship_Type + "?" + NavPosition + "=" + id + "&paginate=true" + "&search=" + Objects.requireNonNull(et_search_view_relationships.getText()).toString();
                } else {
                    url = "v2/relationship/" + Relationship_Type + "?paginate=true";
                }
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, url, "View Relationships", postdata.toString());
                break;
        }
    }

    public void callViewRelationshipWebservice(String NavPosition, String id) {
        if (progress_dialog == null || !progress_dialog.isShowing()) {
            if (progress_dialog == null || !progress_dialog.isShowing()) {
                progress_dialog = AndroidUtils.get_progress(getActivity());
            }
        }
        JSONObject postdata = new JSONObject();
        String url;
        switch (Relationship_Type) {
            case "corporate":
                if (!NavPosition.isEmpty()) {
                    url = "v3/corporate?" + NavPosition + "=" + id + "&paginate=true";
                } else if (!Objects.requireNonNull(et_search_view_relationships.getText()).toString().isEmpty()) {
                    url = "v3/corporate?" + NavPosition + "=" + id + "&paginate=true" + "&search=" + Objects.requireNonNull(et_search_view_relationships.getText()).toString();
                } else {
                    url = "v3/corporate?paginate=true";
                }
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, url, "View Corporate Relationships", postdata.toString());
                break;
            case "temp":
                if (!NavPosition.isEmpty()) {
                    url = "v3/tempclient?" + NavPosition + "=" + id + "&paginate=true";
                } else if (!Objects.requireNonNull(et_search_view_relationships.getText()).toString().isEmpty()) {
                    url = "v3/tempclient?" + NavPosition + "=" + id + "&paginate=true" + "&search=" + Objects.requireNonNull(et_search_view_relationships.getText()).toString();
                } else {
                    url = "v3/tempclient?paginate=true";
                }
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, url, "View Temp Relationships", postdata.toString());
                break;
            case "deleted":
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v2/relationship/delete/list", "View Deleted Relationships", postdata.toString());
                break;
            default:
                if (!NavPosition.isEmpty()) {
                    url = "v2/relationship/" + Relationship_Type + "?" + NavPosition + "=" + id + "&paginate=true";
                } else if (!Objects.requireNonNull(et_search_view_relationships.getText()).toString().isEmpty()) {
                    url = "v2/relationship/" + Relationship_Type + "?" + NavPosition + "=" + id + "&paginate=true" + "&search=" + Objects.requireNonNull(et_search_view_relationships.getText()).toString();
                } else {
                    url = "v2/relationship/" + Relationship_Type + "?paginate=true";
                }
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, url, "View Relationships", postdata.toString());
                break;
        }
    }

    private void calltempclients() {
        JSONObject jsonObject = new JSONObject();
    }

    private void callSearchWebService(String client_name) {
        if (progress_dialog == null || !progress_dialog.isShowing()) {
            progress_dialog = AndroidUtils.get_progress(getActivity());
        }
        JSONObject postdata = new JSONObject();
        if (!client_name.equals("corporate"))
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v2/relationship/search/entity", "Entities List", postdata.toString());
        else
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v2/relationship/search/corporate", "Corporate List", postdata.toString());
    }

    public void callCountriesWebService() {
        if (progress_dialog == null || !progress_dialog.isShowing()) {
            progress_dialog = AndroidUtils.get_progress(getActivity());
        }
        JSONObject jsonData = new JSONObject();
        try {
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "countries", "COUNTRIES", jsonData.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void enableAlpha() {
        Content_layout.setAlpha(0.5F);
        Content_layout.setEnabled(false);
        Content_layout.setClickable(false);
    }

    private void disableAlpha() {
        Content_layout.setAlpha(1.0F);
        Content_layout.setEnabled(true);
        Content_layout.setClickable(true);
    }

    private void HideEntityData() {
        ll_entity_name.setVisibility(GONE);
        ll_contact_person.setVisibility(GONE);
        ll_contatc_phone.setVisibility(VISIBLE);
    }

    private void unHideEntityData() {
        ll_entity_name.setVisibility(VISIBLE);
        ll_contact_person.setVisibility(VISIBLE);
        ll_contatc_phone.setVisibility(VISIBLE);
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                Log.i("Tag", "Info:" + httpResult.getStatus_code());
                Log.i("Tag", "Info:" + result);
                if (progress_dialog != null && progress_dialog.isShowing())
                    AndroidUtils.dismiss_dialog(progress_dialog);
                if (Objects.equals(httpResult.getRequestType(), "View Relationships")) {
                    JSONObject data = result.getJSONObject("data");
                    JSONArray relationships = data.getJSONArray("relationships");
                    ll_nav_buttons.setVisibility(VISIBLE);
                    Prev_Cursor = result.optString("prev_cursor");
                    Next_Cursor = result.optString("next_cursor");
                    if (Prev_Cursor.isEmpty() || Prev_Cursor.equals("null") || Prev_Cursor == null) {
                        AndroidUtils.ToggleButton(0, btn_prev);
                    } else {
                        AndroidUtils.ToggleButton(1, btn_prev);
                    }
                    if (Next_Cursor.isEmpty() || Next_Cursor.equals("null") || Next_Cursor == null) {
                        AndroidUtils.ToggleButton(0, btn_next);
                    } else {
                        AndroidUtils.ToggleButton(1, btn_next);
                    }
                    if (relationships.length() >= 1) {
                        loadRelationshipsData(relationships);
                    } else {
                        // ── Show empty state when no relationships found ──
                        toggleRelationshipsEmptyState(true);
                    }
                    callCountriesWebService();
                    adapter.getFilter().filter(Objects.requireNonNull(et_search_view_relationships.getText()).toString());
                } else if (Objects.equals(httpResult.getRequestType(), "COUNTRIES")) {
                    JSONArray jsonArray = (new JSONObject(result.getString("data"))).getJSONArray("countries");
                    CountriesDO countriesDO;
                    countriesList.clear();
                    for (int i = 1; i < jsonArray.length(); i++) {
                        countriesDO = new CountriesDO();
                        countriesDO.setName(String.valueOf(jsonArray.getJSONArray(i).get(1)));
                        countriesDO.setValue(String.valueOf(jsonArray.getJSONArray(i).get(0)));
                        countriesList.add(countriesDO);
                    }
                    loadCountryData();
                } else {
                    if (httpResult.getRequestType().equals("Search Consumer")) {
                        disableAlpha();
                        JSONArray relationships = Objects.requireNonNull(result.optJSONArray("data"));
                        if (relationships.length() > 0) {
                            load_IndividualData(relationships);
                        } else {
                            enableView();
                        }
                    } else if (Objects.equals(httpResult.getRequestType(), "View Relationships")) {
                        JSONObject data = result.getJSONObject("data");
                        JSONArray relationships = data.getJSONArray("relationships");
                        ll_nav_buttons.setVisibility(VISIBLE);
                        Prev_Cursor = result.optString("prev_cursor");
                        Next_Cursor = result.optString("next_cursor");
                        if (Prev_Cursor.isEmpty() || Prev_Cursor.equals("null") || Prev_Cursor == null) {
                            AndroidUtils.ToggleButton(0, btn_prev);
                        } else {
                            AndroidUtils.ToggleButton(1, btn_prev);
                        }
                        if (Next_Cursor.isEmpty() || Next_Cursor.equals("null") || Next_Cursor == null) {
                            AndroidUtils.ToggleButton(0, btn_next);
                        } else {
                            AndroidUtils.ToggleButton(1, btn_next);
                        }
                        if (relationships.length() >= 1) {
                            loadRelationshipsData(relationships);
                        } else {
                            // ── Show empty state when no relationships found ──
                            toggleRelationshipsEmptyState(true);
                        }
                        callCountriesWebService();
                        adapter.getFilter().filter(Objects.requireNonNull(et_search_view_relationships.getText()).toString());
                    } else if (httpResult.getRequestType().equals("Get Groups")) {
                        JSONArray data = result.getJSONArray("data");
                        loadViewGroups(data);
                    } else if (Objects.equals(httpResult.getRequestType(), "Send Request")) {
                        boolean error = result.getBoolean("error");
                        if (!error) {
                            et_search_individual.setText("");
                            enableAlpha();
                            clearIndividualData();
                            disableIndividualData();
                            groupsList.clear();
                            searchModelsList.clear();
                            ShowCreateSelection();
                            AndroidUtils.showAlert(result.getString("msg"), getActivity());
                        } else {
                            String errorMessage = parseServerErrorMessage(result);
                            AndroidUtils.showAlert(errorMessage, getActivity());
                        }
                    } else if (Objects.equals(httpResult.getRequestType(), "Send Entity Request")) {
                        boolean error = result.getBoolean("error");
                        if (!error) {
                            entity_id = "";
                            value = "";
                            enableAlpha();
                            disableIndividualData();
                            groupsList.clear();
                            tv_response.setText("");
                            ShowCreateSelection();
                            clearIndividualData();
                            AndroidUtils.showAlert(result.getString("msg"), getActivity());
                        } else {
                            String errorMessage = parseServerErrorMessage(result);
                            AndroidUtils.showAlert(errorMessage, getActivity());
                        }
                    } else if (Objects.equals(httpResult.getRequestType(), "Entities List")) {
                        JSONArray relationships = Objects.requireNonNull(result.optJSONArray("data"));
                        Log.i("Tag", "Info:" + relationships);
                        assert relationships != null;
                        loadEntityData(relationships);
                    } else if (Objects.equals(httpResult.getRequestType(), "Corporate List")) {
                        JSONArray relationships = Objects.requireNonNull(result.optJSONArray("data"));
                        Log.i("Tag", "Info:" + relationships);
                        loadEntityData(relationships);
                    } else if (Objects.equals(httpResult.getRequestType(), "Search Entity")) {
                        Log.i("TAG", "EntityDATA:" + result.toString());
                        JSONObject data = result.getJSONObject("data");
                        loadSearchedEntityData(data);
                        value = "";
                    } else if (Objects.equals(httpResult.getRequestType(), "View Corporate Relationships")) {
                        JSONArray relationships = result.getJSONArray("relationships");
                        ll_nav_buttons.setVisibility(VISIBLE);
                        Prev_Cursor = result.optString("prev_cursor");
                        Next_Cursor = result.optString("next_cursor");
                        if (Prev_Cursor.isEmpty() || Prev_Cursor.equals("null") || Prev_Cursor == null) {
                            AndroidUtils.ToggleButton(0, btn_prev);
                        } else {
                            AndroidUtils.ToggleButton(1, btn_prev);
                        }
                        if (Next_Cursor.isEmpty() || Next_Cursor.equals("null") || Next_Cursor == null) {
                            AndroidUtils.ToggleButton(0, btn_next);
                        } else {
                            AndroidUtils.ToggleButton(1, btn_next);
                        }
                        if (relationships.length() >= 1) {
                            loadRelationshipsData(relationships);
                        } else {
                            // ── Show empty state when no corporate relationships found ──
                            toggleRelationshipsEmptyState(true);
                        }
                        callCountriesWebService();
                        adapter.getFilter().filter(Objects.requireNonNull(et_search_view_relationships.getText()).toString());
                    } else if (Objects.equals(httpResult.getRequestType(), "View Temp Relationships")) {
                        JSONArray relationships = Objects.requireNonNull(result.optJSONObject("data")).optJSONArray("relationships");
                        ll_nav_buttons.setVisibility(VISIBLE);
                        Prev_Cursor = result.optString("prev_cursor");
                        Next_Cursor = result.optString("next_cursor");
                        if (Prev_Cursor.isEmpty() || Prev_Cursor.equals("null") || Prev_Cursor == null) {
                            AndroidUtils.ToggleButton(0, btn_prev);
                        } else {
                            AndroidUtils.ToggleButton(1, btn_prev);
                        }
                        if (Next_Cursor.isEmpty() || Next_Cursor.equals("null") || Next_Cursor == null) {
                            AndroidUtils.ToggleButton(0, btn_next);
                        } else {
                            AndroidUtils.ToggleButton(1, btn_next);
                        }
                        if (relationships != null && relationships.length() >= 1) {
                            loadRelationshipsData(relationships);
                        } else {
                            // ── Show empty state when no temp relationships found ──
                            toggleRelationshipsEmptyState(true);
                        }
                        Log.d("Search_Name", Objects.requireNonNull(et_search_view_relationships.getText()).toString());
                        adapter.getFilter().filter(et_search_view_relationships.getText().toString());
                    } else if (httpResult.getRequestType().equals("View Deleted Relationships")) {
                        JSONArray relationships = Objects.requireNonNull(result.optJSONObject("data")).optJSONArray("relationships");
                        ll_nav_buttons.setVisibility(GONE);
                        if (relationships != null && relationships.length() >= 1) {
                            loadRelationshipsData(relationships);
                        } else {
                            // ── Show empty state when no deleted relationships found ──
                            toggleRelationshipsEmptyState(true);
                        }
                        Log.d("Search_Name", Objects.requireNonNull(et_search_view_relationships.getText()).toString());
                        adapter.getFilter().filter(et_search_view_relationships.getText().toString());
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
                String errorMessage = parseServerErrorMessage(result);
                AndroidUtils.showErrorAlert(errorMessage, getActivity());
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        } else {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            AndroidUtils.showErrorAlert(httpResult.getResponseContent().toString(), getActivity());
        }
    }

    private String parseServerErrorMessage(JSONObject result) {
        try {
            Object msgObj = result.opt("msg");
            if (msgObj != null) {
                if (msgObj instanceof String) {
                    String msgStr = (String) msgObj;
                    if (!msgStr.isEmpty() && !msgStr.equals("null")) {
                        return msgStr;
                    }
                } else if (msgObj instanceof JSONObject) {
                    return flattenJsonErrors((JSONObject) msgObj);
                }
            }

            Object errorsObj = result.opt("errors");
            if (errorsObj != null) {
                if (errorsObj instanceof String) {
                    String errStr = (String) errorsObj;
                    if (!errStr.isEmpty() && !errStr.equals("null")) {
                        return errStr;
                    }
                } else if (errorsObj instanceof JSONObject) {
                    return flattenJsonErrors((JSONObject) errorsObj);
                }
            }

            String message = result.optString("message", "");
            if (!message.isEmpty() && !message.equals("null")) {
                return message;
            }

            return "Something went wrong. Please try again.";

        } catch (Exception e) {
            return "Something went wrong. Please try again.";
        }
    }

    private String flattenJsonErrors(JSONObject errorsJson) {
        StringBuilder sb = new StringBuilder();
        try {
            JSONArray keys = errorsJson.names();
            if (keys != null) {
                for (int i = 0; i < keys.length(); i++) {
                    String key = keys.getString(i);
                    String value = errorsJson.optString(key, "");
                    if (!value.isEmpty()) {
                        if (sb.length() > 0) sb.append("\n");
                        sb.append(value);
                    }
                }
            }
        } catch (Exception e) {
            return "Something went wrong. Please try again.";
        }
        return sb.length() > 0 ? sb.toString() : "Something went wrong. Please try again.";
    }

    private void loadRelationshipsData(JSONArray relationships) throws JSONException {
        try {
            RelationshipsModel relationshipsModel = new RelationshipsModel();
            relationshipsList.clear();
            rv_relationships.removeAllViews();
            for (int i = 0; i < relationships.length(); i++) {
                relationshipsModel = new RelationshipsModel();
                JSONObject jsonObject = relationships.getJSONObject(i);
                relationshipsModel.setId(jsonObject.optString("id"));
                relationshipsModel.setClientType(jsonObject.optString("clientType"));
                relationshipsModel.setClient_id(jsonObject.optString("client_id"));
                relationshipsModel.setName(jsonObject.optString("name"));
                relationshipsModel.setGroups(jsonObject.optJSONArray("groups"));
                if (jsonObject.has("canAccept")) {
                    relationshipsModel.setCanAccept(jsonObject.getBoolean("canAccept"));
                }
                if (jsonObject.has("status")) {
                    relationshipsModel.setStatus(jsonObject.optString("status"));
                }
                if (jsonObject.has("deletedBy")) {
                    relationshipsModel.setDeletedBy(jsonObject.optString("deletedBy"));
                }
                if (jsonObject.has("adminName")) {
                    relationshipsModel.setAdminName(jsonObject.getString("adminName"));
                }
                if (jsonObject.has("isDisabled")) {
                    relationshipsModel.setIstemp(true);
                    relationshipsModel.setDisabled(jsonObject.getBoolean("isDisabled"));
                }
                if (jsonObject.has("isdisabled")) {
                    relationshipsModel.setIstemp(true);
                    relationshipsModel.setDisabled(jsonObject.getBoolean("isdisabled"));
                }
                if (jsonObject.has("canAccess")) {
                    relationshipsModel.setCanAccess(jsonObject.optBoolean("canAccess"));
                }
                if (jsonObject.has("created_on")) {
                    relationshipsModel.setCreated(jsonObject.getString("created_on"));
                } else {
                    relationshipsModel.setCreated(jsonObject.getString("created"));
                }
                if (jsonObject.has("consent")) {
                    relationshipsModel.setConsent(jsonObject.getString("consent"));
                }
                if (jsonObject.has("guid"))
                    relationshipsModel.setGuid(jsonObject.getString("guid"));
                if (jsonObject.has("isAccepted"))
                    relationshipsModel.setAccepted(jsonObject.getBoolean("isAccepted"));
                if (jsonObject.has("isClient"))
                    relationshipsModel.setClient(jsonObject.getBoolean("isClient"));
                if (jsonObject.has("isEditable"))
                    relationshipsModel.setEditable(jsonObject.getBoolean("isEditable"));
                if (jsonObject.has("members")) {
                    JSONArray members = jsonObject.getJSONArray("members");
                    relationshipsModel.setMembersList(members);
                }
                if (jsonObject.has("matterList"))
                    relationshipsModel.setMatterList(jsonObject.getJSONArray("matterList"));
                relationshipsList.add(relationshipsModel);
            }
            if (selectedRelId != null && !selectedRelId.isEmpty()) {
                selectedRelationshipsList.clear();
                RelationshipsModel rel = new RelationshipsModel();
                rel.setId(selectedRelId);
                selectedRelationshipsList.add(rel);
                showIndividualEventPopup();
            }
            loadRelationshipsRecylerview(relationshipsModel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void showIndividualEventPopup() {
        try {
            if (relationshipPopup != null && relationshipPopup.isShowing()) {
                relationshipPopup.dismiss();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getLayoutInflater();
            final View dialog = inflater.inflate(R.layout.individual_selected_layout, null);

            ImageView iv_close_popup = dialog.findViewById(R.id.iv_close_popup);
            RecyclerView rv_displayEvents = dialog.findViewById(R.id.rv_view);

            relationshipPopup = builder.create();
            relationshipPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            relationshipPopup.setView(dialog);

            RelationshipsAdapter adapter = new RelationshipsAdapter(
                    selectedRelationshipsList,
                    getContext(),
                    getActivity(),
                    this,
                    Relationship_Type,
                    this,
                    relationshipId,
                    route,
                    true
            );

            rv_displayEvents.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_displayEvents.setAdapter(adapter);

            if (!selectedRelationshipsList.isEmpty()) {
                rv_displayEvents.setVisibility(VISIBLE);
            } else {
                rv_displayEvents.setVisibility(GONE);
            }

            iv_close_popup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    relationshipPopup.dismiss();
                    Constants.isFromNotification = false;
                    if (Constants.notificationBundle != null) {
                        Constants.notificationBundle.clear();
                    }
                    selectedRelId = "";
                    relationshipId = "";
                    selectedRelationshipsList.clear();
                }
            });

            relationshipPopup.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Constants.isFromNotification = false;
                    if (Constants.notificationBundle != null) {
                        Constants.notificationBundle.clear();
                    }
                    selectedRelId = "";
                    relationshipId = "";
                    selectedRelationshipsList.clear();
                }
            });

            relationshipPopup.show();

        } catch (Exception e) {
            Log.e("ClientRelationship", "Error showing popup", e);
            AndroidUtils.showAlert("Error displaying relationship: " + e.getMessage(), getActivity());
        }
    }

    private void loadRelationshipsRecylerview(RelationshipsModel relationshipsModel) throws JSONException {
        if (relationshipsModel.getGroups() != null) {
            for (int j = 0; j < relationshipsModel.getGroups().length(); j++) {
                ViewGroupModel viewGroupModel = new ViewGroupModel();
                JSONObject jsonObject = relationshipsModel.getGroups().getJSONObject(j);
                viewGroupModel.setGroup_id(jsonObject.getString("id"));
                viewGroupModel.setGroup_name(jsonObject.getString("name"));
                updatedMembersList.add(viewGroupModel);
            }
        }
        Log.i("Tag", "Info:" + updatedMembersList.toString());
        adapter = new RelationshipsAdapter(relationshipsList, getContext(), getActivity(), this, Relationship_Type, this, relationshipId);
        rv_relationships.setAdapter(adapter);
        AndroidUtils.LoadingRecyclerview(rv_relationships, getContext());
        AndroidUtils.setupBottomSpacerFooter(rv_relationships, getResources().getDimensionPixelSize(R.dimen.twentyeight_dp));


        // ── Toggle empty state based on loaded list size ──
        toggleRelationshipsEmptyState(relationshipsList.isEmpty());

        et_search_view_relationships.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_search_view_relationships.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                            if (s.isEmpty())
                                callViewRelationshipWebservice("", "");
                        }
                    }
                });
            }
        });
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callViewRelationshipWebservice("", "");
            }
        });
        adapter.notifyDataSetChanged();
    }

    private void loadSearchedEntityData(JSONObject data) throws JSONException {
        ac_search_entity.setText("");
        entitySearchModel = new EntitySearchModel();
        entitySearchModel.setEntityName(data.getString("entityName"));
        entitySearchModel.setEmail(data.getString("email"));
        entitySearchModel.setContactPerson(data.getString("contactPerson"));
        entitySearchModel.setContactPhone(data.getString("contactPhone"));
        entitySearchModel.setCountry(data.getString("country"));
        for (int i = 0; i < countriesList.size(); i++) {
            if (countriesList.get(i).getName().equals(entitySearchModel.getCountry())) {
                sp_country.setSelection(i);
                country_name = countriesList.get(i).getName();
            }
        }
        tv_sp_country.setText(country_name);
        img_clear_icon.setEnabled(false);
        img_clear_icon.setVisibility(VISIBLE);
        img_dropdown_icon.setVisibility(GONE);
        iscountry_checked = true;
        loadEntityUI(entitySearchModel);
        clearSearch();
        updateSendButtonState();
    }

    private void loadEntityUI(EntitySearchModel entitySearchModel) {
        disableAlpha();
        disableIndividualData();
        btn_relationships_cancel.setBackground(getContext().getResources().getDrawable(R.drawable.cancel_button_background));
        btn_send_request.setBackground(getContext().getResources().getDrawable(R.drawable.save_button_background));
        btn_send_request.setAlpha(1.0f);
        btn_send_request.setEnabled(true);
        btn_relationships_cancel.setEnabled(true);
        tv_individual_email.setText((entitySearchModel.getEmail()));
        tv_individual_email.setTextColor(getResources().getColor(R.color.black));
        tv_individual_confirm_email.setText((entitySearchModel.getEmail()));
        tv_individual_confirm_email.setTextColor(getResources().getColor(R.color.black));
        tv_entity_contact_person.setText(entitySearchModel.getContactPerson());
        tv_entity_contact_person.setTextColor(getContext().getResources().getColor(R.color.black));
        AndroidUtils.NumberFilter(tv_entity_phone_number, true);
        tv_entity_phone_number.setText(entitySearchModel.getContactPhone());
        tv_entity_phone_number.setTextColor(getContext().getResources().getColor(R.color.black));
        tv_entity_name.setText(entitySearchModel.getEntityName());
        tv_entity_name.setTextColor(getContext().getResources().getColor(R.color.black));
        String entity = "Entity ";
        String response_txt = entity + entitySearchModel.getEntityName() + positive_msg;
        tv_response.setText(response_txt);
        ll_confirm_email.setVisibility(GONE);
        tv_response.setTextColor(getContext().getResources().getColor(R.color.green_color));

        isEmailValid = true;
        isConfirmEmailValid = true;
        updateSendButtonState();
    }

    private void loadUI(SearchModel searchModel) {
        disableAlpha();
        disableIndividualData();
        btn_relationships_cancel.setBackground(getContext().getResources().getDrawable(R.drawable.cancel_button_background));
        btn_send_request.setBackground(getContext().getResources().getDrawable(R.drawable.save_button_background));
        btn_send_request.setAlpha(1.0f);
        btn_send_request.setEnabled(true);
        btn_relationships_cancel.setEnabled(true);
        tv_individual_firstname.setText(searchModel.getFirstName());
        tv_individual_last_name.setText(searchModel.getLastName());
        tv_individual_email.setText(maskEmail(Objects.requireNonNull(et_search_individual.getText()).toString().trim()));
        tv_individual_confirm_email.setText(maskEmail(et_search_individual.getText().toString().trim()));
        et_search_individual.setText("");

        isEmailValid = true;
        isConfirmEmailValid = true;
        updateSendButtonState();
    }

    private void load_IndividualData(JSONArray entity) throws JSONException {
        filteredIndividualList.clear();
        ArrayList<String> displayNameList = new ArrayList<>();

        for (int i = 0; i < entity.length(); i++) {
            JSONObject jsonObject = entity.getJSONObject(i);
            IndividualModel model = new IndividualModel();
            model.setId(jsonObject.getString("id"));
            String rawFirstName = jsonObject.optString("first_name", "");
            String rawLastName = jsonObject.optString("last_name", "");
            model.setFirst_name(rawFirstName);
            model.setLast_name(rawLastName);
            String cleanFirstName = rawFirstName.trim();
            String cleanLastName = rawLastName.trim();
            String displayName;
            if (!cleanFirstName.isEmpty() && !cleanLastName.isEmpty()) {
                displayName = cleanFirstName + " " + cleanLastName;
            } else if (!cleanFirstName.isEmpty()) {
                displayName = cleanFirstName;
            } else if (!cleanLastName.isEmpty()) {
                displayName = cleanLastName;
            } else {
                displayName = jsonObject.optString("email", "Unknown");
            }
            model.setName(displayName.trim());
            model.setCountry(jsonObject.optString("country"));
            model.setEmail(jsonObject.optString("email"));
            model.setConfirmEmail(jsonObject.optString("email"));
            model.setMobile(jsonObject.optString("mobile"));
            filteredIndividualList.add(model);
            displayNameList.add(displayName);
        }

        Log.d("IndividualData", "Loaded " + filteredIndividualList.size() + " individuals");

        if (filteredIndividualList.size() > 0) {
            IndividualDropdownAdapter adapter = new IndividualDropdownAdapter(requireContext(), filteredIndividualList);
            adapter.notifyDataSetChanged();
            ac_search_entity.setAdapter(adapter);
            ac_search_entity.setThreshold(1);
            ac_search_entity.postDelayed(() -> {
                ac_search_entity.showDropDown();
                Log.d("IndividualData", "Dropdown shown with " + displayNameList.size() + " items");
            }, 100);

            ac_search_entity.setOnItemClickListener((parent, view, position, id) -> {
                String selectedName = displayNameList.get(position);
                IndividualModel selected = filteredIndividualList.get(position);
                ac_search_entity.setText(selectedName, false);
                individualId = selected.getId();
                loadIndividual(selected);
                Log.i("TAG", "Selected: " + selected.getName() + " ID: " + individualId);
            });
        } else {
            Log.d("IndividualData", "No individuals found in response");
            enableView();
        }

        ac_search_entity.setOnFocusChangeListener(null);
        ac_search_entity.setOnClickListener(null);

        ac_search_entity.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && filteredIndividualList.size() > 0) {
                ac_search_entity.postDelayed(() -> ac_search_entity.showDropDown(), 50);
            }
        });

        ac_search_entity.setOnClickListener(v -> {
            if (filteredIndividualList.size() > 0) {
                ac_search_entity.showDropDown();
            }
        });

        ac_search_entity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    clearSearch();
                }
            }
        });
    }

    private void showDropdownIfNotEmpty() {
        ListAdapter adapter = ac_search_entity.getAdapter();
        if (adapter != null && adapter.getCount() > 0) {
            ac_search_entity.showDropDown();
        } else {
            ac_search_entity.dismissDropDown();
        }
    }

    private void loadEntityData(JSONArray entity) throws JSONException {
        entityList.clear();
        for (int i = 0; i < entity.length(); i++) {
            JSONObject jsonObject = entity.getJSONObject(i);
            entityModel = new EntityModel();
            entityModel.setEntityID(jsonObject.getString("entityId"));
            entityModel.setName(jsonObject.getString("name"));
            entityModel.setContactName(jsonObject.getString("contactName"));
            entityList.add(entityModel.getName());
            updatedEntityList.add(entityModel);
        }
        Log.i("TAG_EntityList", "EntityList:" + entityList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                requireContext(),
                R.layout.spinnerdropdownview,
                R.id.spinnerDropDownTextview,
                entityList
        );
        ac_search_entity.setAdapter(adapter);
        ac_search_entity.setThreshold(0);
        ac_search_entity.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) return;
            ac_search_entity.post(this::showDropdownIfNotEmpty);
        });
        ac_search_entity.setOnClickListener(v -> showDropdownIfNotEmpty());

        ac_search_entity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedValue = adapter.getItem(position);
                if (selectedValue != null && updatedEntityList != null) {
                    for (EntityModel entity : updatedEntityList) {
                        if (selectedValue.equals(entity.getName())) {
                            entity_id = entity.getEntityID();
                            break;
                        }
                    }
                } else {
                    Log.e(TAG, "Selected value or updatedEntityList is null");
                }
            }
        });
    }

    private void loadCountryData() {
        CommonSpinnerAdapter adapter = new CommonSpinnerAdapter(getActivity(), countriesList);
        Log.i("ArrayList", "Info:" + countriesList);
        sp_country.setAdapter(adapter);
    }

    private void loadViewGroups(JSONArray data) throws JSONException {
        ViewGroupModel viewGroupModel;
        groupsList.clear();
        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            viewGroupModel = new ViewGroupModel();
            viewGroupModel.setId(jsonObject.getString("id"));
            String date = jsonObject.getString("created");
            Date date_new = AndroidUtils.stringToDateTimeDefault(date, "yyyy-MM-dd'T'HH:mm:ss.SSS");
            String created = AndroidUtils.getDateToString(date_new, "MMM dd YYYY");
            viewGroupModel.setCreated(created);
            JSONArray members = jsonObject.getJSONArray("members");
            viewGroupModel.setMembers(members);
            viewGroupModel.setDescription(jsonObject.getString("description"));
            viewGroupModel.setName(jsonObject.getString("name"));
            JSONObject group_head = jsonObject.getJSONObject("groupHead");
            viewGroupModel.setGroup_head_id(group_head.getString("id"));
            viewGroupModel.setGroup_head_name(group_head.getString("name"));
            viewGroupModel.setOwner_name(group_head.getString("name"));
            viewGroupModel.setChecked(false);
            if ((!jsonObject.getString("name").equals("AAM")) && ((!jsonObject.getString("name").equals("SuperUser"))))
                groupsList.add(viewGroupModel);
        }
        Log.i("ArrayList", "info" + groupsList.toString());
        String mtag = "VG";
        loadGroupsRecylerview();
    }

    public void load_selected_groups(ArrayList<ViewGroupModel> list_item) {
        JSONArray acls = new JSONArray();
        if (!list_item.isEmpty()) {
            for (int i = 0; i < list_item.size(); i++) {
                ViewGroupModel viewGroupModel = list_item.get(i);
                if (viewGroupModel.isChecked()) {
                    acls.put(viewGroupModel.getId());
                }
            }
        }
        if (acls.length() == 0) {
            btn_send_request.setAlpha(1.0f);
            btn_send_request.setEnabled(true);
        } else {
            btn_send_request.setAlpha(1.0f);
            btn_send_request.setEnabled(true);
        }
    }

    private void loadGroupsRecylerview() {
        FLAG = "second_click";
        rv_relationship_groups.setLayoutManager(new GridLayoutManager(getContext(), 1));
        groupsAdapter = new Groupsadapter_relationship(groupsList, this);
        rv_relationship_groups.setAdapter(groupsAdapter);
        et_search_relationships.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                groupsAdapter.getFilter().filter(et_search_relationships.getText().toString().trim());
            }
        });
        btn_relationships_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_search_relationships.setText("");
                groupsList.clear();
                clearIndividualData();
                tv_response.setText("");
                enableAlpha();
                ll_groups.setVisibility(GONE);
            }
        });
        btn_send_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (RELATIONSHIP_TAG.equals("INDIVIDUAL")) {
                    individualValidationClick();
                } else if (RELATIONSHIP_TAG.equals("ENTITY")) {
                    RELATIONSHIP_TAG = "ENTITY";
                    entityValidationClick();
                } else {
                    entityValidationClick();
                }
            }
        });
        chk_select_all.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                groupsAdapter.selectOrDeselectAll(isChecked);
            }
        });
    }

    private void callEntityRequestWebservice() throws JSONException {
        Log.i("Tag", "Country_Name:" + country_name);
        if (progress_dialog == null || !progress_dialog.isShowing()) {
            progress_dialog = AndroidUtils.get_progress(getActivity());
        }
        JSONObject postdata = new JSONObject();
        JSONArray groups = new JSONArray();
        if (!groupsList.isEmpty()) {
            for (int i = 0; i < groupsList.size(); i++) {
                ViewGroupModel viewGroupModel = groupsList.get(i);
                if (viewGroupModel.isChecked()) {
                    groups.put(viewGroupModel.getId());
                }
            }
        }
        if (Objects.equals(entity_id, "")) {
            postdata.put("country", country_name);
            postdata.put("email", (tv_individual_email.getText().toString().trim().trim()));
            postdata.put("fullname", tv_entity_name.getText().toString().trim());
            postdata.put("contact_person", tv_entity_contact_person.getText().toString().trim());
            postdata.put("contact_phone", tv_entity_phone_number.getText().toString().trim());
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v2/relationship/invite/entity", "Send Entity Request", postdata.toString());
        } else {
            postdata.put("entityId", entity_id);
            postdata.put("description", "Description");
            if (RELATIONSHIP_TAG.equals("CORPORATE"))
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v3/corporate", "Send Entity Request", postdata.toString());
            else
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v2/relationship/request/entity", "Send Entity Request", postdata.toString());
        }
    }

    private void individualValidationClick() {
        if (!btn_send_request.isEnabled()) {
            AndroidUtils.showAlert("Please fill all required fields correctly", getActivity());
            return;
        }

        if (individualValidation()) {
            AndroidUtils.showConfirmationDialog(getContext(), requireContext().getString(R.string.confirm), requireContext().getString(R.string.are_you_sure_you_want_send_relationship_request_to) + tv_individual_firstname.getText().toString() + " " + tv_individual_last_name.getText().toString() + "?", tv_individual_firstname.getText().toString() + " " + tv_individual_last_name.getText().toString(), new AndroidUtils.OnConfirmListener() {
                        @Override
                        public void onSave() {
                            try {
                                callIndividualRequestWebservice();
                            } catch (Exception e) {
                                e.fillInStackTrace();
                            }
                        }

                        @Override
                        public void onCancel() {
                        }
                    }
            );
        }
    }

    private void entityValidationClick() {
        if (!btn_send_request.isEnabled()) {
            AndroidUtils.showAlert("Please fill all required fields correctly", getActivity());
            return;
        }

        if (entityValidation()) {
            AndroidUtils.showConfirmationDialog(getContext(), requireContext().getString(R.string.confirm), requireContext().getString(R.string.are_you_sure_you_want_send_relationship_request_to) + tv_entity_name.getText().toString() + "?", tv_entity_name.getText().toString(), new AndroidUtils.OnConfirmListener() {
                        @Override
                        public void onSave() {
                            try {
                                callEntityRequestWebservice();
                            } catch (Exception e) {
                                e.fillInStackTrace();
                            }
                        }

                        @Override
                        public void onCancel() {
                        }
                    }
            );
        }
    }

    public void updateSelectAllState(boolean allSelected) {
        chk_select_all.setOnCheckedChangeListener(null);
        chk_select_all.setChecked(allSelected);
        chk_select_all.setOnCheckedChangeListener(selectAllListener);
    }

    private void callIndividualRequestWebservice() throws JSONException {
        if (progress_dialog == null || !progress_dialog.isShowing()) {
            progress_dialog = AndroidUtils.get_progress(getActivity());
        }
        Log.i("Tag", "Country_Name:" + country_name);
        JSONObject postdata = new JSONObject();
        JSONArray groups = new JSONArray();
        if (!groupsList.isEmpty()) {
            for (int i = 0; i < groupsList.size(); i++) {
                ViewGroupModel viewGroupModel = groupsList.get(i);
                if (viewGroupModel.isChecked()) {
                    groups.put(viewGroupModel.getId());
                }
            }
        }
        if (individualId.isEmpty()) {
            postdata.put("country", country_name);
            postdata.put("email", tv_individual_email.getText().toString().trim());
            postdata.put("first_name", tv_individual_firstname.getText().toString().trim());
            postdata.put("last_name", tv_individual_last_name.getText().toString().trim());
            postdata.put("mobile", tv_entity_phone_number.getText().toString().trim());
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v2/relationship/invite/consumer", "Send Request", postdata.toString());
        } else {
            postdata.put("consumerId", individualId);
            postdata.put("description", "Description");
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v2/relationship/request/consumer", "Send Request", postdata.toString());
        }
    }

    private void loadIndividual(IndividualModel individualModel) {
        enableAlpha();
        clearIndividualFields();
        tv_individual_email.setText(maskEmail(individualModel.getEmail()));
        tv_individual_confirm_email.setText(maskEmail(individualModel.getEmail()));
        tv_individual_firstname.setText(individualModel.getFirst_name());
        tv_individual_last_name.setText(individualModel.getLast_name());
        AndroidUtils.NumberFilterwithStar(tv_entity_phone_number, true);
        tv_entity_phone_number.setText(maskPhoneNumber(individualModel.getMobile()));

        for (int i = 0; i < countriesList.size(); i++) {
            if (countriesList.get(i).getName().equals(individualModel.getCountry())) {
                sp_country.setSelection(i);
                country_name = countriesList.get(i).getName();
            }
        }
        sp_country.setVisibility(GONE);
        iscountry_checked = true;
        tv_individual_last_name.setEnabled(false);
        tv_individual_firstname.setEnabled(false);
        tl_individual_country.setEnabled(false);
        img_clear_icon.setEnabled(false);
        if (country_name != null) {
            if (!country_name.isEmpty()) {
                tv_sp_country.setText(country_name);
                img_clear_icon.setVisibility(VISIBLE);
                img_dropdown_icon.setVisibility(GONE);
            }
        } else {
            tv_sp_country.setText(country_name);
            img_clear_icon.setVisibility(GONE);
            img_dropdown_icon.setVisibility(VISIBLE);
        }
        if (tv_response != null && getContext() != null) {
            String response_txt = "Individual " + individualModel.getName() + positive_msg;
            tv_response.setText(response_txt);
            ll_confirm_email.setVisibility(GONE);
            tv_response.setTextColor(getContext().getResources().getColor(R.color.green_color));
            Content_layout.setAlpha(1.0F);
        }

        isEmailValid = true;
        isConfirmEmailValid = true;
        updateSendButtonState();
    }

    private void enableView() {
        disableAlpha();
        tv_individual_confirm_email.setText("");
        tv_individual_firstname.setText("");
        tv_individual_last_name.setText("");
        tv_individual_email.setText("");
        tv_entity_phone_number.setText("");
        tv_entity_name.setText("");
        tv_entity_contact_person.setText("");
        AndroidUtils.NumberFilter(tv_entity_phone_number, true);
        country_name = "";
        tv_individual_confirm_email.setEnabled(true);
        tv_individual_firstname.setEnabled(true);
        tv_individual_last_name.setEnabled(true);
        tv_entity_phone_number.setEnabled(true);
        tv_individual_email.setEnabled(true);
        sp_country.setVisibility(GONE);
        iscountry_checked = true;
        tl_individual_country.setEnabled(true);
        img_clear_icon.setEnabled(true);
        tv_sp_country.setText(country_name);
        img_clear_icon.setVisibility(GONE);
        img_dropdown_icon.setVisibility(VISIBLE);
        if (tv_response != null && getContext() != null) {
            String response_txt = value + negative_msg;
            tv_response.setText(response_txt);
            tv_response.setTextColor(getContext().getResources().getColor(R.color.Red));
        }
        ll_confirm_email.setVisibility(VISIBLE);
        ac_search_entity.setText("");
        clearSearch();
        resetValidationStates();
    }

    private void loadIndividualData(JSONObject result) throws JSONException {
        searchModel = new SearchModel();
        disableAlpha();
        searchModel.setError(result.optBoolean("error"));
        if (searchModel.getError()) {
            Log.i("Tag", "Info:" + searchModel.getError());
            String response_txt = Objects.requireNonNull(et_search_individual.getText()).toString().trim() + negative_msg;
            tv_response.setText(response_txt);
            tv_individual_email.setText(maskEmail(et_search_individual.getText().toString().trim()));
            tv_individual_email.setTextColor(getResources().getColor(R.color.black));
            tv_individual_confirm_email.setText(maskEmail(et_search_individual.getText().toString().trim()));
            tv_individual_confirm_email.setTextColor(getResources().getColor(R.color.black));
            tv_individual_firstname.setEnabled(true);
            tv_individual_firstname.setFocusable(true);
            tv_individual_firstname.setFocusableInTouchMode(true);
            tv_individual_last_name.setFocusable(true);
            tv_individual_last_name.setEnabled(true);
            tv_individual_last_name.setFocusableInTouchMode(true);
            sp_country.setEnabled(true);
            country_name = "";
            tv_sp_country.setText("");
            img_clear_icon.setVisibility(GONE);
            img_dropdown_icon.setVisibility(VISIBLE);
            tl_individual_country.setEnabled(true);
            img_clear_icon.setEnabled(true);
            sp_country.setSelection(0);
            sp_country.setVisibility(GONE);
            ll_confirm_email.setVisibility(VISIBLE);
            iscountry_checked = true;
        } else {
            Log.i("Tag", "Info:" + searchModel.getError());
            tv_individual_firstname.setText(searchModel.getFirstName());
            tv_individual_firstname.setTextColor(getResources().getColor(R.color.black));
            tv_individual_email.setText(maskEmail(et_search_individual.getText().toString().trim()));
            tv_individual_email.setTextColor(getResources().getColor(R.color.black));
            tv_individual_confirm_email.setText(maskEmail(et_search_individual.getText().toString().trim()));
            tv_individual_confirm_email.setTextColor(getResources().getColor(R.color.black));
            searchModel.setFirstName(result.getString("firstName"));
            tv_individual_last_name.setText(searchModel.getLastName());
            tv_individual_last_name.setTextColor(getResources().getColor(R.color.black));
            searchModel.setLastName(result.getString("lastName"));
            searchModel.setCountry(result.getString("country"));
            searchModel.setConsumerID(result.getString("consumerId"));
            searchModel.setName(result.getString("name"));
            tv_response.setText(searchModel.getMsg());
            ll_confirm_email.setVisibility(GONE);
            tv_response.setTextColor(requireContext().getResources().getColor(R.color.green_color));
            for (int i = 0; i < countriesList.size(); i++) {
                if (countriesList.get(i).getName().equals(searchModel.getCountry())) {
                    sp_country.setSelection(i);
                    country_name = countriesList.get(i).getName();
                }
            }
            sp_country.setVisibility(GONE);
            iscountry_checked = true;
            tl_individual_country.setEnabled(false);
            img_clear_icon.setEnabled(false);
            tv_sp_country.setText(country_name);
            img_clear_icon.setVisibility(VISIBLE);
            img_dropdown_icon.setVisibility(GONE);
            loadUI(searchModel);
        }
        searchModelsList.add(searchModel);
    }

    private void clearIndividualData() {
        tv_individual_email.setEnabled(false);
        tv_individual_confirm_email.setEnabled(false);
        tl_individual_country.setEnabled(false);
        img_clear_icon.setEnabled(false);
        tv_individual_firstname.setText("");
        et_search_relationships.setText("");
        tv_individual_last_name.setText("");
        tv_individual_email.setText("");
        tv_individual_confirm_email.setText("");
        ll_confirm_email.setVisibility(VISIBLE);
        tv_entity_contact_person.setText("");
        tv_entity_phone_number.setText("");
        AndroidUtils.NumberFilter(tv_entity_phone_number, true);
        et_search_individual.setText("");
        ac_search_entity.setText("");
        clearSearch();
        tv_sp_country.setText("");
        img_clear_icon.setVisibility(GONE);
        img_dropdown_icon.setVisibility(VISIBLE);
        sp_country.setVisibility(GONE);
        for (int i = 0; i < countriesList.size(); i++) {
            if (countriesList.get(i).getName().equals("Choose country")) {
                sp_country.setSelection(i);
            }
        }
        resetValidationStates();
    }

    private void enableIndividualData() {
        tv_individual_firstname.setEnabled(true);
        tv_individual_last_name.setEnabled(true);
        tv_individual_email.setEnabled(true);
        tv_individual_confirm_email.setEnabled(true);
        tv_entity_phone_number.setEnabled(true);
        tv_entity_name.setEnabled(true);
        tv_entity_contact_person.setEnabled(true);
        btn_send_request.setEnabled(true);
        btn_relationships_cancel.setEnabled(true);
        tl_individual_country.setEnabled(true);
        sp_country.setEnabled(true);
    }

    private void disableIndividualData() {
        tv_individual_firstname.setEnabled(false);
        tv_individual_last_name.setEnabled(false);
        tv_individual_email.setEnabled(false);
        tv_individual_confirm_email.setEnabled(false);
        tv_entity_phone_number.setEnabled(false);
        tv_entity_name.setEnabled(false);
        tv_entity_contact_person.setEnabled(false);
        tl_individual_country.setEnabled(false);
        img_clear_icon.setEnabled(false);
        tl_individual_country.setEnabled(false);
    }

    private void disabledIndividualData() {
        tv_individual_firstname.setEnabled(false);
        tv_individual_last_name.setEnabled(false);
        tv_individual_email.setEnabled(false);
        tv_individual_confirm_email.setEnabled(false);
        tv_entity_phone_number.setEnabled(false);
        tv_entity_name.setEnabled(false);
        tl_individual_country.setEnabled(false);
        sp_country.setEnabled(false);
    }

    private boolean individualValidation() {
        if (individualId.isEmpty()) {
            if (isFormValid) {
                return true;
            } else {
                AndroidUtils.showAlert("Please fill all required fields correctly", getActivity());
                return false;
            }
        }
        return true;
    }

    private boolean entityValidation() {
        if (entity_id.isEmpty()) {
            if (isFormValid) {
                String email = tv_individual_email.getText().toString().trim();
                String confirmEmail = tv_individual_confirm_email.getText().toString().trim();

                if (!email.equals(confirmEmail)) {
                    AndroidUtils.showAlert("Email and confirm email do not match", getActivity());
                    return false;
                }

                if (!isValidEmail(email) && !isMaskedEmail(email)) {
                    AndroidUtils.showAlert("Please enter a valid email address", getActivity());
                    return false;
                }

                return true;
            } else {
                AndroidUtils.showAlert("Please fill all required fields correctly", getActivity());
                return false;
            }
        }
        return true;
    }

    @Override
    public void RefreshViewRelationshipsData() {
        viewRelationshipsData();
    }
}