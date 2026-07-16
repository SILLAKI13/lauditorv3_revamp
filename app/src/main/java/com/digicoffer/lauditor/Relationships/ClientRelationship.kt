package com.digicoffer.lauditor.Relationships

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.Companion.isMaskedEmail
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.Companion.isValidEmail
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.Companion.maskEmail
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.Companion.maskPhoneNumber
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Relationships.Adapter.IndividualDropdownAdapter
import com.digicoffer.lauditor.Relationships.Adapter.RelationshipsAdapter
import com.digicoffer.lauditor.Relationships.Model.CountriesDO
import com.digicoffer.lauditor.Relationships.Model.EntityModel
import com.digicoffer.lauditor.Relationships.Model.EntitySearchModel
import com.digicoffer.lauditor.Relationships.Model.IndividualModel
import com.digicoffer.lauditor.Relationships.Model.RelationshipsModel
import com.digicoffer.lauditor.Relationships.Model.SearchModel
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import java.util.Date
import java.util.Objects

class ClientRelationship : Fragment(), AsyncTaskCompleteListener, View.OnClickListener, RelationshipsAdapter.EventListener {

    var btn_prev: AppCompatButton? = null
    var btn_next: AppCompatButton? = null
    var btn_search: AppCompatButton? = null
    var Prev_Cursor: String = ""
    private val filteredIndividualList = ArrayList<IndividualModel>()
    var route: String = ""
    private var relationshipPopup: AlertDialog? = null
    var relationshipId: String = ""
    var selectedRelId: String = ""
    private val selectedRelationshipsList = ArrayList<RelationshipsModel>()
    var Next_Cursor: String = ""
    var individualId: String = ""
    var tv_switchCreate: LinearLayout? = null
    var tv_switchView: LinearLayout? = null
    private var Content_layout: LinearLayout? = null
    private var rg_individual_entity: LinearLayout? = null
    private var rg_relationship: LinearLayout? = null
    val searchModelsList = ArrayList<SearchModel>()
    val entityList = ArrayList<String>()
    val updatedMembersList = ArrayList<ViewGroupModel>()
    val relationshipsList = ArrayList<RelationshipsModel>()
    var TAG: String = ""
    var ll_nav_buttons: LinearLayout? = null
    var tl_search_relationships: CardView? = null
    var selectAllListener: CompoundButton.OnCheckedChangeListener? = null
    var adapter: RelationshipsAdapter? = null
    var Email_error: TextView? = null
    var ConfirmEmail_error: TextView? = null
    var PhoneNumber_error: TextView? = null
    private var iscountry_checked = true
    private var mViewModel: NewModel? = null
    private var progress_dialog: Dialog? = null
    var ll_email: LinearLayout? = null
    var ll_confirm_email: LinearLayout? = null
    private var countryListView: ListView? = null
    var email: TextView? = null
    var confirm_email: TextView? = null
    var first_name: TextView? = null
    var last_name: TextView? = null
    var country: TextView? = null
    var Contact_phno: TextView? = null
    var contact_person: TextView? = null
    var value: String = ""
    var Relationship_Type: String = "individuals"
    var entitySearchModel: EntitySearchModel? = null
    var entityModel: EntityModel? = null
    val updatedEntityList = ArrayList<EntityModel>()
    val updatedIndividualList = ArrayList<IndividualModel>()
    var rb_add_relationship: TextView? = null
    var rb_view_relationships: TextView? = null
    var tv_add_individual: TextView? = null
    var tv_add_entity: TextView? = null
    var tv_add_corporate: TextView? = null
    var tv_view_individual_relationship: TextView? = null
    var tv_view_entity_relationship: TextView? = null
    var tv_view_corporate_relationship: TextView? = null
    var tv_view_temp_relationship: TextView? = null
    var ll_entity_name: LinearLayout? = null
    var ll_contact_person: LinearLayout? = null
    var ll_first_name: LinearLayout? = null
    var ll_last_name: LinearLayout? = null
    var ll_contatc_phone: LinearLayout? = null
    var ll_search_individual: LinearLayout? = null
    var ll_search_entity: LinearLayout? = null
    var ll_relationships: LinearLayout? = null
    var ll_select_all: LinearLayout? = null
    var ll_groups: LinearLayout? = null
    var et_search_relationships: TextInputEditText? = null
    var et_search_individual: TextInputEditText? = null
    var et_search_view_relationships: TextInputEditText? = null
    var tv_individual_email: TextInputEditText? = null
    var tv_individual_confirm_email: TextInputEditText? = null
    var tv_individual_firstname: TextInputEditText? = null
    var tv_individual_last_name: TextInputEditText? = null
    var tv_entity_name: TextInputEditText? = null
    var tv_entity_contact_person: TextInputEditText? = null
    var tv_entity_phone_number: TextInputEditText? = null
    var btn_search_individual: Button? = null
    var btn_relationships_cancel: Button? = null
    var btn_send_request: Button? = null
    var btn_search_entity: Button? = null
    var tv_response: TextView? = null
    var entity_name: TextView? = null
    var tv_sp_country: TextView? = null
    var ac_search_entity: AutoCompleteTextView? = null
    var tl_individual_country: LinearLayout? = null
    var img_dropdown_icon: ImageView? = null
    var img_clear_icon: ImageView? = null
    var iv_mobile_mandatory: ImageView? = null
    var entity_id: String = ""
    var sp_country: ListView? = null
    var negative_msg: String = " - not found. Please fill in the details below to send relationship invite."
    var positive_msg: String = " - found!"
    var groupsAdapter: Groupsadapter_relationship? = null
    var rv_relationship_groups: RecyclerView? = null
    var rv_relationships: RecyclerView? = null
    var searchModel: SearchModel? = null
    private var chk_select_all: CheckBox? = null
    val groupsList = ArrayList<ViewGroupModel>()
    val countriesList = ArrayList<CountriesDO>()
    var cv_details: CardView? = null
    private var country_name: String? = null
    var ll_country: LinearLayout? = null
    var sv_relationships: ScrollView? = null
    var ll_view_rel: LinearLayout? = null

    // ─── Empty state view ─────────────────────────────────────────────────────
    private var layout_relationships_empty_state: LinearLayout? = null

    // Validation state variables
    private var isEmailValid = false
    private var isConfirmEmailValid = false
    private var isPhoneValid = false
    private var isFormValid = false

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @Nullable
    override fun onCreateView(
        @NonNull inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.client_relationships, container, false)
        callCountriesWebService()
        return view
    }

    override fun onViewCreated(@NonNull view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
        Content_layout = view.findViewById(R.id.Content_layout)
        iv_mobile_mandatory = view.findViewById(R.id.iv_mobile_mandatory)
        tv_switchView = view.findViewById(R.id.tv_switchView)
        tv_switchCreate = view.findViewById(R.id.tv_switchCreate)
        Email_error = view.findViewById(R.id.Email_error)
        ll_view_rel = view.findViewById(R.id.ll_view_rel)
        PhoneNumber_error = view.findViewById(R.id.PhoneNumber_error)
        ConfirmEmail_error = view.findViewById(R.id.ConfirmEmail_error)
        rg_relationship = view.findViewById(R.id.view_relationship)
        sv_relationships = view.findViewById(R.id.sv_relationships) as ScrollView
        rb_add_relationship = view.findViewById(R.id.tv_add_relationship)
        rb_add_relationship?.setText(R.string.add_relationships)
        rb_view_relationships = view.findViewById(R.id.tv_view_relationship)
        rb_view_relationships?.setText(R.string.view_relationships)
        rg_individual_entity = view.findViewById(R.id.entity)
        tv_add_individual = view.findViewById(R.id.tv_add_individual)
        tv_add_individual?.setText(R.string.individual)
        tv_add_entity = view.findViewById(R.id.tv_add_entity)
        tv_add_entity?.setText(R.string.entity)
        tv_add_corporate = view.findViewById(R.id.tv_add_corporate)
        tv_add_corporate?.setText(R.string.corporate)
        tv_view_individual_relationship = view.findViewById(R.id.tv_view_individual_relationship)
        tv_view_individual_relationship?.setText(R.string.individual)
        tv_view_entity_relationship = view.findViewById(R.id.tv_view_entity_relationship)
        tv_view_entity_relationship?.setText(R.string.business)
        tv_view_corporate_relationship = view.findViewById(R.id.tv_view_corporate_relationship)
        tv_view_corporate_relationship?.setText(R.string.corporate)
        tv_view_temp_relationship = view.findViewById(R.id.tv_view_temp_relationship)
        tv_view_temp_relationship?.setText(R.string.deleted)

        val context = requireContext()

        if (Constants.ROLE == "SU") {
            tv_view_temp_relationship?.visibility = VISIBLE
            tv_view_corporate_relationship?.background = ContextCompat.getDrawable(context, R.drawable.radiobutton_centre_background)
        } else {
            tv_view_temp_relationship?.visibility = GONE
            tv_view_corporate_relationship?.background = ContextCompat.getDrawable(context, R.drawable.button_right_background)
        }

        tv_view_individual_relationship?.textSize = 10f
        tv_view_individual_relationship?.maxLines = 1
        tv_view_entity_relationship?.textSize = 10f
        tv_view_entity_relationship?.maxLines = 1
        tv_view_corporate_relationship?.textSize = 10f
        tv_view_corporate_relationship?.maxLines = 1
        tv_view_temp_relationship?.textSize = 10f
        tv_view_temp_relationship?.maxLines = 1

        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100)

        rv_relationship_groups = view.findViewById(R.id.rv_relationship_groups)
        rv_relationships = view.findViewById(R.id.rv_relationships)

        layout_relationships_empty_state = view.findViewById(R.id.layout_relationships_empty_state)

        email = view.findViewById(R.id.email)
        email?.setText(R.string.email)
        ll_select_all = view.findViewById(R.id.ll_select_all)
        ll_select_all?.visibility = GONE
        ll_email = view.findViewById(R.id.ll_email)
        ll_confirm_email = view.findViewById(R.id.ll_confirm_email)
        confirm_email = view.findViewById(R.id.confirm_email)
        confirm_email?.setText(R.string.confirm_email)
        first_name = view.findViewById(R.id.first_name)
        first_name?.setText(R.string.first_name)
        last_name = view.findViewById(R.id.last_name)
        last_name?.setText(R.string.last_name)
        country = view.findViewById(R.id.country)
        country?.setText(R.string.country)
        Contact_phno = view.findViewById(R.id.Contact_phno)
        Contact_phno?.setText(R.string.mobile_)
        entity_name = view.findViewById(R.id.entity_name)
        entity_name?.setText(R.string.entity_name)
        contact_person = view.findViewById(R.id.contact_person)
        contact_person?.setText(R.string.contact_person)
        ll_search_entity = view.findViewById(R.id.ll_search_entity)
        ll_search_individual = view.findViewById(R.id.ll_search_individual)
        ll_search_individual?.visibility = GONE
        ll_entity_name = view.findViewById(R.id.ll_entity_name)
        ll_first_name = view.findViewById(R.id.ll_first_name)
        ll_last_name = view.findViewById(R.id.ll_last_name)
        ll_country = view.findViewById(R.id.ll_country)
        ll_groups = view.findViewById(R.id.ll_groups)
        ll_groups?.visibility = GONE
        ll_contact_person = view.findViewById(R.id.ll_contact_person)
        ll_contatc_phone = view.findViewById(R.id.ll_entity_number)
        ac_search_entity = view.findViewById(R.id.ac_search_entity)
        ac_search_entity?.setHint(R.string.search)
        et_search_individual = view.findViewById(R.id.et_search_individual)
        et_search_individual?.setHint(R.string.search)
        et_search_individual?.addTextChangedListener(Validation(et_search_individual))
        btn_search_individual = view.findViewById(R.id.btn_search_individual)
        btn_search_individual?.setText(R.string.search)
        btn_search_entity = view.findViewById(R.id.btn_search_entity)
        btn_search_entity?.setText(R.string.search)
        et_search_relationships = view.findViewById(R.id.et_search_relationships)
        et_search_relationships?.setHint(R.string.search_groups)
        et_search_relationships?.addTextChangedListener(Validation(et_search_relationships))
        tv_response = view.findViewById(R.id.tv_response)
        cv_details = view.findViewById(R.id.cv_details)
        chk_select_all = view.findViewById(R.id.chk_select_all)
        tv_individual_email = view.findViewById(R.id.tv_individual_email)
        tv_individual_email?.setHint(R.string.email)
        tv_individual_email?.addTextChangedListener(Validation(tv_individual_email))
        tv_entity_contact_person = view.findViewById(R.id.tv_entity_contact_person)
        tv_entity_contact_person?.setHint(R.string.contact_person)
        tv_entity_contact_person?.addTextChangedListener(Validation(tv_entity_contact_person))
        tv_entity_name = view.findViewById(R.id.tv_entity_name)
        tv_entity_name?.setHint(R.string.entity_name)
        tv_entity_name?.addTextChangedListener(Validation(tv_entity_name))
        tv_entity_phone_number = view.findViewById(R.id.tv_entity_phone_number)
        tv_entity_phone_number?.setHint(R.string.mobile_)
        iv_mobile_mandatory?.visibility = GONE
        tv_entity_contact_person?.addTextChangedListener(Validation(tv_entity_contact_person))
        ll_relationships = view.findViewById(R.id.ll_relationships)
        tv_individual_confirm_email = view.findViewById(R.id.tv_individual_confirm_email)
        tv_individual_confirm_email?.setHint(R.string.confirm_email)
        tv_individual_confirm_email?.addTextChangedListener(Validation(tv_individual_confirm_email))

        tl_individual_country = view.findViewById(R.id.tl_individual_country)
        tv_sp_country = tl_individual_country?.findViewById(R.id.tv_spinner_view)
        img_clear_icon = tl_individual_country?.findViewById(R.id.img_clear_icon)
        img_clear_icon?.isEnabled = false
        img_dropdown_icon = tl_individual_country?.findViewById(R.id.img_dropdown_icon)

        sp_country = view.findViewById(R.id.sp_country)
        tv_individual_firstname = view.findViewById(R.id.tv_individual_firstname)
        tv_individual_firstname?.setHint(R.string.first_name)
        tv_individual_firstname?.addTextChangedListener(Validation(tv_individual_firstname))

        tv_individual_last_name = view.findViewById(R.id.tv_individual_last_name)
        tv_individual_last_name?.setHint(R.string.last_name)
        tv_individual_last_name?.addTextChangedListener(Validation(tv_individual_last_name))
        btn_send_request = view.findViewById(R.id.btn_send_request)
        btn_send_request?.setText(R.string.send_request)
        btn_send_request?.setAlpha(0.5f)
        btn_send_request?.isEnabled = false
        btn_relationships_cancel = view.findViewById(R.id.btn_relationships_cancel)
        rg_individual_entity?.visibility = VISIBLE

        tl_individual_country?.setOnClickListener(this)
        rb_add_relationship?.setOnClickListener(this)
        rb_view_relationships?.setOnClickListener(this)
        tv_add_individual?.setOnClickListener(this)
        tv_add_entity?.setOnClickListener(this)
        tv_add_corporate?.setOnClickListener(this)
        tv_view_individual_relationship?.setOnClickListener(this)
        tv_view_entity_relationship?.setOnClickListener(this)
        tv_view_corporate_relationship?.setOnClickListener(this)
        tv_view_temp_relationship?.setOnClickListener(this)
        btn_search_individual?.setOnClickListener(this)
        btn_search_entity?.setOnClickListener(this)
        ll_nav_buttons = view.findViewById(R.id.ll_nav_buttons)
        ll_nav_buttons?.visibility = GONE
        btn_prev = view.findViewById(R.id.btn_prev)
        btn_prev?.setText(R.string.prev_)
        btn_next = view.findViewById(R.id.btn_next)
        btn_next?.setText(R.string.next_)
        tl_search_relationships = view.findViewById(R.id.tl_search_relationships)
        btn_search = tl_search_relationships?.findViewById(R.id.btn_search)
        et_search_view_relationships = tl_search_relationships?.findViewById(R.id.et_search_tm)
        et_search_view_relationships?.setHint(R.string.search_relationship)
        et_search_view_relationships?.textSize = DynamicUtils.fifteen.toFloat()
        et_search_view_relationships?.addTextChangedListener(Validation(et_search_view_relationships))

        enableAlpha()
        disableIndividualData()

        AndroidUtils.setupModuleView(
            tv_switchCreate,
            getString(R.string.view_relationships),
            false, true, getContext(), getString(R.string.add_relationships) + " - " + getString(R.string.individual)
        ) {
            ShowCreateSelection()
        }

        AndroidUtils.setupModuleView(
            tv_switchView,
            getString(R.string.add_relationships),
            true, true, getContext(), getString(R.string.view_relationships) + " - " + getString(R.string.individual)
        ) {
            if (!Constants.is_active) {
                AndroidUtils.showRenewalPopup(requireActivity())
            } else {
                showAddRelationshipUI()
            }
        }

        if (Constants.isCreate) {
            showAddRelationshipUI()
        } else {
            tv_switchCreate?.visibility = GONE
            tv_switchView?.visibility = VISIBLE
            if (Constants.isFromNotification) {
                handleNotificationNavigation()
            }
            if (Constants.Rel_Type == "Entity") {
                View_Entity()
            } else if (Constants.Rel_Type == "Deleted") {
                View_deleted()
            } else if (Constants.Rel_Type == "Corporate") {
                View_corporate()
            } else {
                View_Individual()
            }
            mViewModel?.setData(getString(R.string.view_relationships))
        }

        img_clear_icon?.setOnClickListener {
            AndroidUtils.DisplaySpinnerView(sp_country, tv_sp_country, country_name, img_dropdown_icon, img_clear_icon, false)
            iscountry_checked = true
            updateSendButtonState()
        }

        sp_country?.setOnItemClickListener { parent, view, position, id ->
            country_name = countriesList[position].name
            iscountry_checked = true
            AndroidUtils.DisplaySpinnerView(sp_country, tv_sp_country, country_name, img_dropdown_icon, img_clear_icon, true)
            updateSendButtonState()
        }

        setupEmailValidation()
        setupConfirmEmailValidation()
        setupPhoneValidation()
        setupFormFieldValidation()

        selectAllListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            groupsAdapter?.selectOrDeselectAll(isChecked)
        }

        btn_prev?.setOnClickListener {
            callViewRelationshipWebservice("before", Prev_Cursor)
        }

        btn_next?.setOnClickListener {
            callViewRelationshipWebservice("after", Next_Cursor)
        }

        btn_relationships_cancel?.setOnClickListener {
            et_search_relationships?.setText("")
            groupsList.clear()
            clearIndividualData()
            tv_response?.text = ""
            enableAlpha()
            ll_groups?.visibility = GONE
        }

        btn_send_request?.setOnClickListener {
            if (RELATIONSHIP_TAG == "INDIVIDUAL") {
                individualValidationClick()
            } else if (RELATIONSHIP_TAG == "ENTITY") {
                RELATIONSHIP_TAG = "ENTITY"
                entityValidationClick()
            } else {
                entityValidationClick()
            }
        }

        chk_select_all?.setOnCheckedChangeListener(selectAllListener)
    }

    private fun toggleRelationshipsEmptyState(isEmpty: Boolean) {
        val layout = layout_relationships_empty_state ?: return
        if (isEmpty) {
            layout.visibility = VISIBLE
            ll_nav_buttons?.visibility = GONE
            rv_relationships?.visibility = GONE
        } else {
            layout.visibility = GONE
            rv_relationships?.visibility = VISIBLE
            ll_nav_buttons?.visibility = VISIBLE
        }
    }

    private fun setupEmailValidation() {
        tv_individual_email?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Email_error?.visibility = GONE
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = s?.toString()?.trim() ?: ""

                if (isMaskedEmail(email)) {
                    Email_error?.visibility = GONE
                    isEmailValid = true
                    updateSendButtonState()
                    return
                }

                if (email.isNotEmpty()) {
                    if (isValidEmail(email)) {
                        Email_error?.visibility = GONE
                        isEmailValid = true
                    } else {
                        Email_error?.text = getString(R.string.enter_a_valid_email_address)
                        Email_error?.visibility = VISIBLE
                        isEmailValid = false
                    }
                } else {
                    isEmailValid = false
                }

                val confirmEmail = tv_individual_confirm_email?.text?.toString()?.trim() ?: ""
                if (confirmEmail.isNotEmpty() && email != confirmEmail) {
                    ConfirmEmail_error?.text = Constants.cemail_alert
                    ConfirmEmail_error?.visibility = VISIBLE
                    isConfirmEmailValid = false
                } else if (confirmEmail.isNotEmpty() && email == confirmEmail) {
                    ConfirmEmail_error?.visibility = GONE
                    isConfirmEmailValid = true
                } else if (confirmEmail.isEmpty()) {
                    isConfirmEmailValid = false
                }

                updateSendButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupConfirmEmailValidation() {
        tv_individual_confirm_email?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                ConfirmEmail_error?.visibility = GONE
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = tv_individual_email?.text?.toString()?.trim() ?: ""
                val confirmEmail = s?.toString()?.trim() ?: ""

                if (confirmEmail.isNotEmpty()) {
                    if (email == confirmEmail) {
                        ConfirmEmail_error?.visibility = GONE
                        isConfirmEmailValid = true
                    } else {
                        ConfirmEmail_error?.text = Constants.cemail_alert
                        ConfirmEmail_error?.visibility = VISIBLE
                        isConfirmEmailValid = false
                    }
                } else {
                    isConfirmEmailValid = false
                }

                updateSendButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupPhoneValidation() {
        tv_entity_phone_number?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                PhoneNumber_error?.visibility = GONE
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s?.toString()?.trim() ?: ""
                if (input.isNotEmpty()) {
                    if (input.length < 10) {
                        PhoneNumber_error?.text = "Please enter a 10 digit valid mobile number"
                        PhoneNumber_error?.visibility = VISIBLE
                        isPhoneValid = false
                    } else {
                        PhoneNumber_error?.visibility = GONE
                        isPhoneValid = true
                    }
                } else {
                    isPhoneValid = false
                }

                updateSendButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFormFieldValidation() {
        tv_individual_firstname?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSendButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        tv_individual_last_name?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSendButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        tv_entity_name?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSendButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        tv_entity_contact_person?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSendButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateSendButtonState() {
        if (RELATIONSHIP_TAG == "INDIVIDUAL") {
            if (individualId.isEmpty()) {
                val firstNameValid = tv_individual_firstname?.text?.toString()?.trim()?.isNotEmpty() ?: false
                val lastNameValid = tv_individual_last_name?.text?.toString()?.trim()?.isNotEmpty() ?: false
                val emailValid = isEmailValid && (tv_individual_email?.text?.toString()?.trim()?.isNotEmpty() ?: false)
                val confirmEmailValid = isConfirmEmailValid
                val countryValid = tv_sp_country?.text?.toString()?.trim()?.isNotEmpty() ?: false

                isFormValid = firstNameValid && lastNameValid && emailValid && confirmEmailValid && countryValid
            } else {
                isFormValid = true
            }
        } else if (RELATIONSHIP_TAG == "ENTITY" || RELATIONSHIP_TAG == "CORPORATE") {
            if (entity_id.isEmpty()) {
                val entityNameValid = tv_entity_name?.text?.toString()?.trim()?.isNotEmpty() ?: false
                val contactPersonValid = tv_entity_contact_person?.text?.toString()?.trim()?.isNotEmpty() ?: false
                val emailValid = isEmailValid && (tv_individual_email?.text?.toString()?.trim()?.isNotEmpty() ?: false)
                val confirmEmailValid = isConfirmEmailValid
                val countryValid = tv_sp_country?.text?.toString()?.trim()?.isNotEmpty() ?: false
                val phoneValid = isPhoneValid || (tv_entity_phone_number?.text?.toString()?.trim()?.isEmpty() ?: true)

                isFormValid = entityNameValid && contactPersonValid && emailValid && confirmEmailValid && countryValid && phoneValid
            } else {
                isFormValid = true
            }
        }

        btn_send_request?.isEnabled = isFormValid
        btn_send_request?.alpha = if (isFormValid) 1.0f else 0.5f
    }

    private fun resetValidationStates() {
        isEmailValid = false
        isConfirmEmailValid = false
        isPhoneValid = false
        isFormValid = false
        updateSendButtonState()
    }

    private fun ShowViewSelection() {
        when (Relationship_Type) {
            "corporate" -> Add_Corporate()
            "business" -> Add_Entity()
            else -> Add_Individual()
        }
    }

    private fun ShowCreateSelection() {
        tv_switchCreate?.visibility = GONE
        tv_switchView?.visibility = VISIBLE
        when (RELATIONSHIP_TAG) {
            "CORPORATE" -> View_corporate()
            "ENTITY" -> View_Entity()
            else -> View_Individual()
        }
    }

    override fun onClick(view: View) {
        val viewId = view.id
        if (viewId == R.id.tl_individual_country) {
            AndroidUtils.display_listview(iscountry_checked, sp_country)
            iscountry_checked = !iscountry_checked
        } else if (viewId == R.id.tv_add_relationship) {
            showAddRelationshipUI()
        } else if (viewId == R.id.tv_view_relationship) {
            ShowCreateSelection()
            mViewModel?.setData(getString(R.string.view_relationships))
        } else if (viewId == R.id.tv_add_individual) {
            Add_Individual()
        } else if (viewId == R.id.tv_add_corporate) {
            Add_Corporate()
        } else if (viewId == R.id.tv_add_entity) {
            Add_Entity()
        } else if (viewId == R.id.btn_search_individual) {
            searchIndividual()
        } else if (viewId == R.id.btn_search_entity) {
            searchEntity()
        } else if (viewId == R.id.tv_view_corporate_relationship) {
            relationshipId = ""
            selectedRelId = ""
            View_corporate()
        } else if (viewId == R.id.tv_view_temp_relationship) {
            relationshipId = ""
            selectedRelId = ""
            View_deleted()
        } else if (viewId == R.id.tv_view_individual_relationship) {
            relationshipId = ""
            selectedRelId = ""
            View_Individual()
        } else if (viewId == R.id.tv_view_entity_relationship) {
            relationshipId = ""
            selectedRelId = ""
            View_Entity()
        }
    }

    private fun showAddRelationshipUI() {
        rg_individual_entity?.visibility = VISIBLE
        rg_relationship?.visibility = GONE
        rb_add_relationship?.let { updateButtonStyles(it, R.drawable.button_left_green_background, R.color.white) }
        rb_view_relationships?.let { updateButtonStyles(it, R.drawable.button_right_background, R.color.black) }
        ll_relationships?.visibility = GONE
        ll_contatc_phone?.visibility = VISIBLE
        cv_details?.visibility = VISIBLE
        ll_groups?.visibility = GONE
        relationshipsList.clear()
        enableAlpha()
        clearIndividualFields()
        ShowViewSelection()
        mViewModel?.setData(getString(R.string.add_relationships))
        tv_switchView?.visibility = GONE
        tv_switchCreate?.visibility = VISIBLE
        resetValidationStates()
    }

    private fun handleNotificationNavigation() {
        val bundle = Constants.notificationBundle ?: return
        route = bundle.getString(Constants.NavKeys.ROUTE_NAME) ?: ""
        if (route.isNotEmpty()) {
            val highlightList = bundle.getStringArrayList(Constants.NavKeys.HIGHLIGHT_IDS)
            if (highlightList != null && highlightList.isNotEmpty()) {
                relationshipId = highlightList[0]
            }
            selectedRelId = bundle.getString(Constants.NavKeys.RELATIONSHIP_ID) ?: ""
        }
    }

    private fun clearIndividualFields() {
        et_search_individual?.setText("")
        tl_individual_country?.isEnabled = false
        img_clear_icon?.isEnabled = false
        tv_response?.text = ""
        tv_individual_email?.setText("")
        tv_individual_email?.isEnabled = false
        tv_individual_confirm_email?.isEnabled = false
        tv_entity_phone_number?.isEnabled = false
        tv_entity_phone_number?.setText("")
        tv_entity_phone_number?.let { AndroidUtils.NumberFilter(it, true) }
        tv_individual_confirm_email?.setText("")
        tv_individual_firstname?.setText("")
        tv_individual_last_name?.setText("")
        sp_country?.clearFocus()
        sp_country?.visibility = GONE
        rv_relationships?.removeAllViews()
        resetValidationStates()
    }

    private fun updateButtonStyles(button: TextView, backgroundResId: Int, textColorResId: Int) {
        button.background = ContextCompat.getDrawable(requireContext(), backgroundResId)
        button.setTextColor(ContextCompat.getColor(requireContext(), textColorResId))
    }

    private fun Add_Entity() {
        RELATIONSHIP_TAG = "ENTITY"
        mViewModel?.setData(getString(R.string.entity))
        tv_switchCreate?.let {
            AndroidUtils.updateModuleTitle(
                it,
                getString(R.string.add_relationships) + " - " + getString(R.string.entity)
            )
        }
        tv_entity_name?.setText("")
        ll_search_individual?.visibility = GONE
        ll_search_entity?.visibility = VISIBLE
        ll_first_name?.visibility = GONE
        ll_last_name?.visibility = GONE
        ll_groups?.visibility = GONE
        ac_search_entity?.setText("")
        tv_response?.text = ""
        btn_send_request?.setAlpha(0.5f)
        btn_send_request?.isEnabled = false
        Contact_phno?.setText(R.string.contact_phone_number_)
        tv_entity_phone_number?.setHint(R.string.contact_phone_number_)
        iv_mobile_mandatory?.visibility = VISIBLE
        callSearchWebService("entity")
        enableAlpha()
        unHideEntityData()
        clearIndividualData()
        configureEntityView()
        clearSearch()
        resetValidationStates()
    }

    private fun clearSearch() {
        if (RELATIONSHIP_TAG == "INDIVIDUAL") {
            filteredIndividualList.clear()
            val adapter = IndividualDropdownAdapter(requireContext(), filteredIndividualList)
            adapter.notifyDataSetChanged()
            ac_search_entity?.setAdapter(adapter)
            ac_search_entity?.dismissDropDown()
        }
    }

    private fun searchIndividual() {
        try {
            value = ac_search_entity?.text?.toString()?.trim() ?: ""
            val email = et_search_individual?.text?.toString()?.trim() ?: ""
            if (email.isEmpty()) {
                AndroidUtils.showAlert("Please check the search field..,", activity)
            } else {
                disableAlpha()
                tv_individual_email?.setText(maskEmail(email))
                tv_individual_confirm_email?.setText(maskEmail(email))
                ll_groups?.visibility = GONE
                tv_individual_firstname?.setText("")
                tv_individual_last_name?.setText("")
                callSearchIndividualWebservice()
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    private fun resetIndividualSearchFields() {
        tv_individual_email?.setText("")
        tv_individual_confirm_email?.setText("")
        tv_response?.text = ""
        et_search_relationships?.setText("")
        enableAlpha()
        ll_groups?.visibility = GONE
    }

    private fun searchEntity() {
        val entityName = ac_search_entity?.text?.toString()?.trim() ?: ""
        entity_id = ""
        individualId = ""
        try {
            if (entityName.isNotEmpty()) {
                value = entityName
                if (RELATIONSHIP_TAG == "INDIVIDUAL") {
                    callSearchIndividualWebservice()
                } else {
                    chk_select_all?.visibility = GONE
                    Search_Entity()
                }
            } else {
                value = ""
                AndroidUtils.showAlert("Please check the search field..,", activity)
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun View_Entity() {
        et_search_relationships?.setText("")
        et_search_view_relationships?.setText("")
        Relationship_Type = "business"
        mViewModel?.setData(getString(R.string.business))
        tv_view_individual_relationship?.let { updateRelationshipButtons(it, R.drawable.button_left_background, Color.BLACK) }
        tv_view_entity_relationship?.let { updateRelationshipButtons(it, R.drawable.radiobutton_centre_green_background, Color.WHITE) }
        if (Constants.ROLE == "SU") {
            tv_view_temp_relationship?.visibility = VISIBLE
            tv_view_corporate_relationship?.let { updateRelationshipButtons(it, R.drawable.radiobutton_centre_background, Color.BLACK) }
        } else {
            tv_view_temp_relationship?.visibility = GONE
            tv_view_corporate_relationship?.let { updateRelationshipButtons(it, R.drawable.button_right_background, Color.BLACK) }
        }
        tv_view_temp_relationship?.let { updateRelationshipButtons(it, R.drawable.button_right_background, Color.BLACK) }
        tv_switchView?.let {
            AndroidUtils.updateModuleTitle(
                it,
                getString(R.string.view_relationships) + " - " + getString(R.string.business)
            )
        }
        viewRelationshipsData()
        clearIndividualData()
    }

    private fun updateRelationshipButtons(button: TextView, backgroundResId: Int, textColor: Int) {
        button.background = ContextCompat.getDrawable(requireContext(), backgroundResId)
        button.setTextColor(textColor)
    }

    private fun View_temp() {
        et_search_relationships?.setText("")
        et_search_view_relationships?.setText("")
        tv_view_individual_relationship?.let { updateRelationshipButtons(it, R.drawable.button_left_background, Color.BLACK) }
        tv_view_entity_relationship?.let { updateRelationshipButtons(it, R.drawable.radiobutton_centre_background, Color.BLACK) }
        tv_view_corporate_relationship?.let { updateRelationshipButtons(it, R.drawable.button_right_green_background, Color.BLACK) }
        tv_view_temp_relationship?.let { updateRelationshipButtons(it, R.drawable.button_right_green_background, Color.WHITE) }
        Relationship_Type = "temp"
        mViewModel?.setData(getString(R.string.temporary_clients))
        viewRelationshipsData()
        clearIndividualData()
    }

    private fun View_deleted() {
        et_search_relationships?.setText("")
        et_search_view_relationships?.setText("")
        tv_view_individual_relationship?.let { updateRelationshipButtons(it, R.drawable.button_left_background, Color.BLACK) }
        tv_view_entity_relationship?.let { updateRelationshipButtons(it, R.drawable.radiobutton_centre_background, Color.BLACK) }
        tv_view_corporate_relationship?.let { updateRelationshipButtons(it, R.drawable.radiobutton_centre_background, Color.BLACK) }
        tv_view_temp_relationship?.let { updateRelationshipButtons(it, R.drawable.button_right_green_background, Color.WHITE) }
        Relationship_Type = "deleted"
        mViewModel?.setData(getString(R.string.deleted_relationship))
        tv_switchView?.let {
            AndroidUtils.updateModuleTitle(
                it,
                getString(R.string.view_relationships) + " - " + getString(R.string.deleted)
            )
        }
        viewRelationshipsData()
        clearIndividualData()
    }

    private fun View_corporate() {
        et_search_relationships?.setText("")
        et_search_view_relationships?.setText("")
        tv_view_individual_relationship?.let { updateRelationshipButtons(it, R.drawable.button_left_background, Color.BLACK) }
        tv_view_entity_relationship?.let { updateRelationshipButtons(it, R.drawable.radiobutton_centre_background, Color.BLACK) }
        if (Constants.ROLE == "SU") {
            tv_view_temp_relationship?.visibility = VISIBLE
            tv_view_corporate_relationship?.let { updateRelationshipButtons(it, R.drawable.radiobutton_centre_green_background, Color.WHITE) }
        } else {
            tv_view_temp_relationship?.visibility = GONE
            tv_view_corporate_relationship?.let { updateRelationshipButtons(it, R.drawable.button_right_green_background, Color.WHITE) }
        }
        tv_view_temp_relationship?.let { updateRelationshipButtons(it, R.drawable.button_right_background, Color.BLACK) }
        Relationship_Type = "corporate"
        mViewModel?.setData(getString(R.string.corporate))
        tv_switchView?.let {
            AndroidUtils.updateModuleTitle(
                it,
                getString(R.string.view_relationships) + " - " + getString(R.string.corporate)
            )
        }
        viewRelationshipsData()
        clearIndividualData()
    }

    private fun configureEntityView() {
        tv_add_corporate?.let { updateAddButtons(it, R.drawable.button_right_background, R.color.black) }
        tv_add_individual?.let { updateAddButtons(it, R.drawable.button_left_background, R.color.black) }
        tv_add_entity?.let { updateAddButtons(it, R.drawable.radiobutton_centre_green_background, R.color.white) }
        val parent = ll_country?.parent as? LinearLayout
        parent?.removeView(ll_country)
        val targetParent = ll_contact_person?.parent as? LinearLayout
        targetParent?.addView(ll_country, 2)
    }

    private fun Add_Corporate() {
        tv_entity_name?.setText("")
        btn_send_request?.setAlpha(0.5f)
        btn_send_request?.isEnabled = false
        RELATIONSHIP_TAG = "CORPORATE"
        mViewModel?.setData(getString(R.string.corporate))
        callSearchWebService("corporate")
        tv_add_corporate?.let { updateAddButtons(it, R.drawable.button_right_green_background, R.color.white) }
        tv_add_individual?.let { updateAddButtons(it, R.drawable.button_left_background, R.color.black) }
        tv_add_entity?.let { updateAddButtons(it, R.drawable.radiobutton_centre_background, R.color.black) }
        tv_switchCreate?.let {
            AndroidUtils.updateModuleTitle(
                it,
                getString(R.string.add_relationships) + " - " + getString(R.string.corporate)
            )
        }
        unHideEntityData()
        clearIndividualData()
        enableAlpha()
        setupCorporateView()
        resetValidationStates()
    }

    private fun setupCorporateView() {
        ll_search_individual?.visibility = GONE
        ll_search_entity?.visibility = VISIBLE
        ll_first_name?.visibility = GONE
        ll_last_name?.visibility = GONE
        ll_confirm_email?.visibility = VISIBLE
        ll_groups?.visibility = GONE
        ac_search_entity?.setText("")
        clearSearch()
        tv_response?.text = ""
        val parent = ll_country?.parent as? LinearLayout
        parent?.removeView(ll_country)
        val targetParent = ll_contact_person?.parent as? LinearLayout
        targetParent?.addView(ll_country, 2)
    }

    private fun Add_Individual() {
        btn_send_request?.setAlpha(0.5f)
        btn_send_request?.isEnabled = false
        RELATIONSHIP_TAG = "INDIVIDUAL"
        mViewModel?.setData(getString(R.string.individual))
        tv_switchCreate?.let {
            AndroidUtils.updateModuleTitle(
                it,
                getString(R.string.add_relationships) + " - " + getString(R.string.individual)
            )
        }
        clearIndividualFields()
        HideEntityData()
        updatedEntityList.clear()
        entityList.clear()
        ll_search_individual?.visibility = GONE
        ll_search_entity?.visibility = VISIBLE
        ll_first_name?.visibility = VISIBLE
        ll_last_name?.visibility = VISIBLE
        ll_confirm_email?.visibility = VISIBLE
        tv_entity_name?.setText("")
        tv_add_individual?.let { updateAddButtons(it, R.drawable.button_left_green_background, R.color.white) }
        tv_add_entity?.let { updateAddButtons(it, R.drawable.radiobutton_centre_background, R.color.black) }
        tv_add_corporate?.let { updateAddButtons(it, R.drawable.button_right_background, R.color.black) }
        enableAlpha()
        ll_contatc_phone?.visibility = VISIBLE
        ll_groups?.visibility = GONE
        clearIndividualData()
        Contact_phno?.setText(R.string.mobile_)
        tv_entity_phone_number?.setHint(R.string.mobile_)
        iv_mobile_mandatory?.visibility = GONE
        val parent = ll_country?.parent as? LinearLayout
        parent?.removeView(ll_country)
        val targetParent = ll_last_name?.parent as? LinearLayout
        targetParent?.addView(ll_country)
        resetValidationStates()
    }

    private fun updateAddButtons(button: TextView, backgroundResId: Int, textColorResId: Int) {
        button.background = ContextCompat.getDrawable(requireContext(), backgroundResId)
        button.setTextColor(ContextCompat.getColor(requireContext(), textColorResId))
    }

    private fun View_Individual() {
        if (progress_dialog == null || !progress_dialog!!.isShowing) {
            progress_dialog = AndroidUtils.get_progress(activity)
        }
        et_search_relationships?.setText("")
        et_search_view_relationships?.setText("")
        tv_view_individual_relationship?.let { updateRelationshipButtons(it, R.drawable.button_left_green_background, Color.WHITE) }
        tv_view_entity_relationship?.let { updateRelationshipButtons(it, R.drawable.radiobutton_centre_background, Color.BLACK) }
        if (Constants.ROLE == "SU") {
            tv_view_temp_relationship?.visibility = VISIBLE
            tv_view_corporate_relationship?.let { updateRelationshipButtons(it, R.drawable.radiobutton_centre_background, Color.BLACK) }
        } else {
            tv_view_temp_relationship?.visibility = GONE
            tv_view_corporate_relationship?.let { updateRelationshipButtons(it, R.drawable.button_right_background, Color.BLACK) }
        }
        tv_view_temp_relationship?.let { updateRelationshipButtons(it, R.drawable.button_right_background, Color.BLACK) }
        Relationship_Type = "individuals"
        mViewModel?.setData(getString(R.string.individual))
        tv_switchView?.let {
            AndroidUtils.updateModuleTitle(
                it,
                getString(R.string.view_relationships) + " - " + getString(R.string.individual)
            )
        }
        viewRelationshipsData()
    }

    private fun Search_Entity() {
        try {
            ll_groups?.visibility = GONE
            disableAlpha()
            clearIndividualData()
            callSearchEntityWebservice()
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
    }

    private fun callSearchEntityWebservice() {
        for (j in 0 until updatedEntityList.size) {
            if (value == updatedEntityList[j].name) {
                entity_id = updatedEntityList[j].entityID ?: ""
            }
        }
        if (entity_id.isEmpty()) {
            if (RELATIONSHIP_TAG == "CORPORATE") {
                enableAlpha()
                ll_groups?.visibility = GONE
                tv_response?.let {
                    it.text = "$value - not found."
                    it.setTextColor(ContextCompat.getColor(requireContext(), R.color.Red))
                }
                btn_send_request?.setAlpha(0.5f)
                btn_send_request?.isEnabled = false
                ll_confirm_email?.visibility = VISIBLE
            } else {
                country_name = ""
                tv_sp_country?.text = ""
                iscountry_checked = true
                ll_confirm_email?.visibility = VISIBLE
                tv_response?.let {
                    it.text = value + negative_msg
                    it.setTextColor(ContextCompat.getColor(requireContext(), R.color.Red))
                }
                clearIndividualData()
                tv_entity_name?.setText(value)
                disableAlpha()
                tv_entity_name?.let {
                    it.isEnabled = true
                    it.isFocusable = true
                    it.isFocusableInTouchMode = true
                }
                tv_entity_contact_person?.let {
                    it.isEnabled = true
                    it.isFocusable = true
                    it.isFocusableInTouchMode = true
                }
                tv_individual_email?.let {
                    it.isEnabled = true
                    it.isFocusable = true
                    it.isFocusableInTouchMode = true
                }
                tv_individual_confirm_email?.let {
                    it.isEnabled = true
                    it.isFocusable = true
                    it.isFocusableInTouchMode = true
                }
                sp_country?.let {
                    it.isEnabled = true
                    img_clear_icon?.isEnabled = true
                    tl_individual_country?.isEnabled = true
                    it.setSelection(0)
                    it.visibility = GONE
                }
                tv_entity_phone_number?.let {
                    it.isFocusable = true
                    it.isEnabled = true
                    it.isFocusableInTouchMode = true
                }
                ll_confirm_email?.visibility = VISIBLE
            }
        } else {
            if (progress_dialog == null || !progress_dialog!!.isShowing) {
                progress_dialog = AndroidUtils.get_progress(activity)
            }
            WebServiceHelper.callHttpWebService(
                this, requireContext(), WebServiceHelper.RestMethodType.GET,
                "v2/relationship/entity/$entity_id", "Search Entity", ""
            )
        }
    }

    private fun callGroupsWebservice() {
        val postdata = JSONObject()
        WebServiceHelper.callHttpWebService(
            this, requireContext(), WebServiceHelper.RestMethodType.GET,
            "v3/groups", "Get Groups", postdata.toString()
        )
    }

    private fun callSearchIndividualWebservice() {
        if (progress_dialog == null || !progress_dialog!!.isShowing) {
            progress_dialog = AndroidUtils.get_progress(activity)
        }
        val postdata = JSONObject()
        postdata.put("search", ac_search_entity?.text?.toString()?.trim() ?: "")
        WebServiceHelper.callHttpWebService(
            this, requireContext(), WebServiceHelper.RestMethodType.POST,
            "v3/relationship/search/consumer", "Search Consumer", postdata.toString()
        )
    }

    fun viewRelationshipsData() {
        rv_relationships?.visibility = GONE
        rg_individual_entity?.visibility = GONE
        rg_relationship?.visibility = GONE
        rb_add_relationship?.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_left_background)
        rb_view_relationships?.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_right_green_count)
        rb_add_relationship?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        rb_view_relationships?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        ll_relationships?.visibility = VISIBLE
        cv_details?.visibility = GONE
        ll_groups?.visibility = GONE
        toggleRelationshipsEmptyState(false)
        if (Constants.isFromNotification) {
            callViewRelationshipWebservice("", "", relationshipId)
        } else {
            callViewRelationshipWebservice("", "")
        }
        Constants.isFromNotification = false
        Constants.notificationBundle?.clear()
    }

    fun callViewRelationshipWebservice(NavPosition: String, id: String, anchorId: String) {
        if (progress_dialog == null || !progress_dialog!!.isShowing) {
            progress_dialog = AndroidUtils.get_progress(activity)
        }
        val postdata = JSONObject()
        val url: String
        when (Relationship_Type) {
            "corporate" -> {
                url = if (anchorId.isNotEmpty()) {
                    "v3/corporate?paginate=true&anchor_id=$anchorId"
                } else if (NavPosition.isNotEmpty()) {
                    "v3/corporate?$NavPosition=$id&paginate=true&anchor_id=$anchorId"
                } else if (et_search_view_relationships?.text?.toString()?.isNotEmpty() == true) {
                    "v3/corporate?$NavPosition=$id&paginate=true&search=" + et_search_view_relationships?.text?.toString()
                } else {
                    "v3/corporate?paginate=true"
                }
                WebServiceHelper.callHttpWebService(
                    this, requireContext(), WebServiceHelper.RestMethodType.GET,
                    url, "View Corporate Relationships", postdata.toString()
                )
            }
            "temp" -> {
                url = if (anchorId.isNotEmpty()) {
                    "v3/tempclient?paginate=true&anchor_id=$anchorId"
                } else if (NavPosition.isNotEmpty()) {
                    "v3/tempclient?$NavPosition=$id&paginate=true"
                } else if (et_search_view_relationships?.text?.toString()?.isNotEmpty() == true) {
                    "v3/tempclient?$NavPosition=$id&paginate=true&search=" + et_search_view_relationships?.text?.toString()
                } else {
                    "v3/tempclient?paginate=true"
                }
                WebServiceHelper.callHttpWebService(
                    this, requireContext(), WebServiceHelper.RestMethodType.GET,
                    url, "View Temp Relationships", postdata.toString()
                )
            }
            "deleted" -> {
                WebServiceHelper.callHttpWebService(
                    this, requireContext(), WebServiceHelper.RestMethodType.GET,
                    "v2/relationship/delete/list", "View Deleted Relationships", postdata.toString()
                )
            }
            else -> {
                url = if (anchorId.isNotEmpty()) {
                    "v2/relationship/$Relationship_Type?paginate=true&anchor_id=$anchorId"
                } else if (NavPosition.isNotEmpty()) {
                    "v2/relationship/$Relationship_Type?$NavPosition=$id&paginate=true"
                } else if (et_search_view_relationships?.text?.toString()?.isNotEmpty() == true) {
                    "v2/relationship/$Relationship_Type?$NavPosition=$id&paginate=true&search=" + et_search_view_relationships?.text?.toString()
                } else {
                    "v2/relationship/$Relationship_Type?paginate=true"
                }
                WebServiceHelper.callHttpWebService(
                    this, requireContext(), WebServiceHelper.RestMethodType.GET,
                    url, "View Relationships", postdata.toString()
                )
            }
        }
    }

    fun callViewRelationshipWebservice(NavPosition: String, id: String) {
        if (progress_dialog == null || !progress_dialog!!.isShowing) {
            progress_dialog = AndroidUtils.get_progress(activity)
        }
        val postdata = JSONObject()
        val url: String
        when (Relationship_Type) {
            "corporate" -> {
                url = if (NavPosition.isNotEmpty()) {
                    "v3/corporate?$NavPosition=$id&paginate=true"
                } else if (et_search_view_relationships?.text?.toString()?.isNotEmpty() == true) {
                    "v3/corporate?$NavPosition=$id&paginate=true&search=" + et_search_view_relationships?.text?.toString()
                } else {
                    "v3/corporate?paginate=true"
                }
                WebServiceHelper.callHttpWebService(
                    this, requireContext(), WebServiceHelper.RestMethodType.GET,
                    url, "View Corporate Relationships", postdata.toString()
                )
            }
            "temp" -> {
                url = if (NavPosition.isNotEmpty()) {
                    "v3/tempclient?$NavPosition=$id&paginate=true"
                } else if (et_search_view_relationships?.text?.toString()?.isNotEmpty() == true) {
                    "v3/tempclient?$NavPosition=$id&paginate=true&search=" + et_search_view_relationships?.text?.toString()
                } else {
                    "v3/tempclient?paginate=true"
                }
                WebServiceHelper.callHttpWebService(
                    this, requireContext(), WebServiceHelper.RestMethodType.GET,
                    url, "View Temp Relationships", postdata.toString()
                )
            }
            "deleted" -> {
                WebServiceHelper.callHttpWebService(
                    this, requireContext(), WebServiceHelper.RestMethodType.GET,
                    "v2/relationship/delete/list", "View Deleted Relationships", postdata.toString()
                )
            }
            else -> {
                url = if (NavPosition.isNotEmpty()) {
                    "v2/relationship/$Relationship_Type?$NavPosition=$id&paginate=true"
                } else if (et_search_view_relationships?.text?.toString()?.isNotEmpty() == true) {
                    "v2/relationship/$Relationship_Type?$NavPosition=$id&paginate=true&search=" + et_search_view_relationships?.text?.toString()
                } else {
                    "v2/relationship/$Relationship_Type?paginate=true"
                }
                WebServiceHelper.callHttpWebService(
                    this, requireContext(), WebServiceHelper.RestMethodType.GET,
                    url, "View Relationships", postdata.toString()
                )
            }
        }
    }

    private fun calltempclients() {}

    private fun callSearchWebService(client_name: String) {
        if (progress_dialog == null || !progress_dialog!!.isShowing) {
            progress_dialog = AndroidUtils.get_progress(activity)
        }
        val postdata = JSONObject()
        if (client_name != "corporate") {
            WebServiceHelper.callHttpWebService(
                this, requireContext(), WebServiceHelper.RestMethodType.GET,
                "v2/relationship/search/entity", "Entities List", postdata.toString()
            )
        } else {
            WebServiceHelper.callHttpWebService(
                this, requireContext(), WebServiceHelper.RestMethodType.GET,
                "v2/relationship/search/corporate", "Corporate List", postdata.toString()
            )
        }
    }

    fun callCountriesWebService() {
        if (progress_dialog == null || !progress_dialog!!.isShowing) {
            progress_dialog = AndroidUtils.get_progress(activity)
        }
        val jsonData = JSONObject()
        try {
            WebServiceHelper.callHttpWebService(
                this, requireContext(), WebServiceHelper.RestMethodType.GET,
                "countries", "COUNTRIES", jsonData.toString()
            )
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun enableAlpha() {
        Content_layout?.alpha = 0.5f
        Content_layout?.isEnabled = false
        Content_layout?.isClickable = false
    }

    private fun disableAlpha() {
        Content_layout?.alpha = 1.0f
        Content_layout?.isEnabled = true
        Content_layout?.isClickable = true
    }

    private fun HideEntityData() {
        ll_entity_name?.visibility = GONE
        ll_contact_person?.visibility = GONE
        ll_contatc_phone?.visibility = VISIBLE
    }

    private fun unHideEntityData() {
        ll_entity_name?.visibility = VISIBLE
        ll_contact_person?.visibility = VISIBLE
        ll_contatc_phone?.visibility = VISIBLE
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent)
                Log.i("Tag", "Info:" + httpResult.status_code)
                Log.i("Tag", "Info:$result")
                if (progress_dialog != null && progress_dialog!!.isShowing) {
                    AndroidUtils.dismiss_dialog(progress_dialog)
                }
                if (httpResult.requestType == "View Relationships") {
                    val data = result.getJSONObject("data")
                    val relationships = data.getJSONArray("relationships")
                    ll_nav_buttons?.visibility = VISIBLE
                    Prev_Cursor = result.optString("prev_cursor")
                    Next_Cursor = result.optString("next_cursor")
                    if (Prev_Cursor.isEmpty() || Prev_Cursor == "null") {
                        btn_prev?.let { AndroidUtils.ToggleButton(0, it) }
                    } else {
                        btn_prev?.let { AndroidUtils.ToggleButton(1, it) }
                    }
                    if (Next_Cursor.isEmpty() || Next_Cursor == "null") {
                        btn_next?.let { AndroidUtils.ToggleButton(0, it) }
                    } else {
                        btn_next?.let { AndroidUtils.ToggleButton(1, it) }
                    }
                    if (relationships.length() >= 1) {
                        loadRelationshipsData(relationships)
                    } else {
                        toggleRelationshipsEmptyState(true)
                    }
                    callCountriesWebService()
                    adapter?.filter?.filter(et_search_view_relationships?.text?.toString() ?: "")
                } else if (httpResult.requestType == "COUNTRIES") {
                    val jsonArray = JSONObject(result.getString("data")).getJSONArray("countries")
                    countriesList.clear()
                    for (i in 1 until jsonArray.length()) {
                        val countriesDO = CountriesDO()
                        val countryData = jsonArray.getJSONArray(i)
                        countriesDO.name = countryData.optString(1)
                        countriesDO.value = countryData.optString(0)
                        countriesList.add(countriesDO)
                    }
                    loadCountryData()
                } else {
                    if (httpResult.requestType == "Search Consumer") {
                        disableAlpha()
                        val relationships = result.optJSONArray("data")
                        if (relationships != null && relationships.length() > 0) {
                            load_IndividualData(relationships)
                        } else {
                            enableView()
                        }
                    } else if (httpResult.requestType == "Get Groups") {
                        val data = result.getJSONArray("data")
                        loadViewGroups(data)
                    } else if (httpResult.requestType == "Send Request") {
                        val error = result.getBoolean("error")
                        if (!error) {
                            et_search_individual?.setText("")
                            enableAlpha()
                            clearIndividualData()
                            disableIndividualData()
                            groupsList.clear()
                            searchModelsList.clear()
                            ShowCreateSelection()
                            AndroidUtils.showAlert(result.getString("msg"), activity)
                        } else {
                            val errorMessage = parseServerErrorMessage(result)
                            AndroidUtils.showAlert(errorMessage, activity)
                        }
                    } else if (httpResult.requestType == "Send Entity Request") {
                        val error = result.getBoolean("error")
                        if (!error) {
                            entity_id = ""
                            value = ""
                            enableAlpha()
                            disableIndividualData()
                            groupsList.clear()
                            tv_response?.text = ""
                            ShowCreateSelection()
                            clearIndividualData()
                            AndroidUtils.showAlert(result.getString("msg"), activity)
                        } else {
                            val errorMessage = parseServerErrorMessage(result)
                            AndroidUtils.showAlert(errorMessage, activity)
                        }
                    } else if (httpResult.requestType == "Entities List") {
                        val relationships = result.optJSONArray("data")
                        Log.i("Tag", "Info:$relationships")
                        if (relationships != null) {
                            loadEntityData(relationships)
                        }
                    } else if (httpResult.requestType == "Corporate List") {
                        val relationships = result.optJSONArray("data")
                        Log.i("Tag", "Info:$relationships")
                        if (relationships != null) {
                            loadEntityData(relationships)
                        }
                    } else if (httpResult.requestType == "Search Entity") {
                        Log.i("TAG", "EntityDATA:$result")
                        val data = result.getJSONObject("data")
                        loadSearchedEntityData(data)
                        value = ""
                    } else if (httpResult.requestType == "View Corporate Relationships") {
                        val relationships = result.getJSONArray("relationships")
                        ll_nav_buttons?.visibility = VISIBLE
                        Prev_Cursor = result.optString("prev_cursor")
                        Next_Cursor = result.optString("next_cursor")
                        if (Prev_Cursor.isEmpty() || Prev_Cursor == "null") {
                            btn_prev?.let { AndroidUtils.ToggleButton(0, it) }
                        } else {
                            btn_prev?.let { AndroidUtils.ToggleButton(1, it) }
                        }
                        if (Next_Cursor.isEmpty() || Next_Cursor == "null") {
                            btn_next?.let { AndroidUtils.ToggleButton(0, it) }
                        } else {
                            btn_next?.let { AndroidUtils.ToggleButton(1, it) }
                        }
                        if (relationships.length() >= 1) {
                            loadRelationshipsData(relationships)
                        } else {
                            toggleRelationshipsEmptyState(true)
                        }
                        callCountriesWebService()
                        adapter?.filter?.filter(et_search_view_relationships?.text?.toString() ?: "")
                    } else if (httpResult.requestType == "View Temp Relationships") {
                        val relationships = result.optJSONObject("data")?.optJSONArray("relationships")
                        ll_nav_buttons?.visibility = VISIBLE
                        Prev_Cursor = result.optString("prev_cursor")
                        Next_Cursor = result.optString("next_cursor")
                        if (Prev_Cursor.isEmpty() || Prev_Cursor == "null") {
                            btn_prev?.let { AndroidUtils.ToggleButton(0, it) }
                        } else {
                            btn_prev?.let { AndroidUtils.ToggleButton(1, it) }
                        }
                        if (Next_Cursor.isEmpty() || Next_Cursor == "null") {
                            btn_next?.let { AndroidUtils.ToggleButton(1, it) }
                        } else {
                            btn_next?.let { AndroidUtils.ToggleButton(1, it) }
                        }
                        if (relationships != null && relationships.length() >= 1) {
                            loadRelationshipsData(relationships)
                        } else {
                            toggleRelationshipsEmptyState(true)
                        }
                        adapter?.filter?.filter(et_search_view_relationships?.text?.toString() ?: "")
                    } else if (httpResult.requestType == "View Deleted Relationships") {
                        val relationships = result.optJSONObject("data")?.optJSONArray("relationships")
                        ll_nav_buttons?.visibility = GONE
                        if (relationships != null && relationships.length() >= 1) {
                            loadRelationshipsData(relationships)
                        } else {
                            toggleRelationshipsEmptyState(true)
                        }
                        adapter?.filter?.filter(et_search_view_relationships?.text?.toString() ?: "")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (httpResult.result == WebServiceHelper.ServiceCallStatus.Failed) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            try {
                val result = JSONObject(httpResult.responseContent)
                val errorMessage = parseServerErrorMessage(result)
                AndroidUtils.showErrorAlert(errorMessage, activity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            AndroidUtils.showErrorAlert(httpResult.responseContent.toString(), activity)
        }
    }

    private fun parseServerErrorMessage(result: JSONObject): String {
        try {
            val msgObj = result.opt("msg")
            if (msgObj != null) {
                if (msgObj is String) {
                    if (msgObj.isNotEmpty() && msgObj != "null") {
                        return msgObj
                    }
                } else if (msgObj is JSONObject) {
                    return flattenJsonErrors(msgObj)
                }
            }

            val errorsObj = result.opt("errors")
            if (errorsObj != null) {
                if (errorsObj is String) {
                    if (errorsObj.isNotEmpty() && errorsObj != "null") {
                        return errorsObj
                    }
                } else if (errorsObj is JSONObject) {
                    return flattenJsonErrors(errorsObj)
                }
            }

            val message = result.optString("message", "")
            if (message.isNotEmpty() && message != "null") {
                return message
            }

            return "Something went wrong. Please try again."
        } catch (e: Exception) {
            return "Something went wrong. Please try again."
        }
    }

    private fun flattenJsonErrors(errorsJson: JSONObject): String {
        val sb = StringBuilder()
        try {
            val keys = errorsJson.names()
            if (keys != null) {
                for (i in 0 until keys.length()) {
                    val key = keys.getString(i)
                    val value = errorsJson.optString(key, "")
                    if (value.isNotEmpty()) {
                        if (sb.isNotEmpty()) sb.append("\n")
                        sb.append(value)
                    }
                }
            }
        } catch (e: Exception) {
            return "Something went wrong. Please try again."
        }
        return if (sb.isNotEmpty()) sb.toString() else "Something went wrong. Please try again."
    }

    private fun loadRelationshipsData(relationships: JSONArray) {
        try {
            relationshipsList.clear()
            rv_relationships?.removeAllViews()
            var relationshipsModel = RelationshipsModel()
            for (i in 0 until relationships.length()) {
                relationshipsModel = RelationshipsModel()
                val jsonObject = relationships.getJSONObject(i)
                relationshipsModel.id = jsonObject.optString("id")
                relationshipsModel.clientType = jsonObject.optString("clientType")
                relationshipsModel.client_id = jsonObject.optString("client_id")
                relationshipsModel.name = jsonObject.optString("name")
                relationshipsModel.groups = jsonObject.optJSONArray("groups")
                if (jsonObject.has("canAccept")) {
                    relationshipsModel.canAccept = jsonObject.getBoolean("canAccept")
                }
                if (jsonObject.has("status")) {
                    relationshipsModel.status = jsonObject.optString("status")
                }
                if (jsonObject.has("deletedBy")) {
                    relationshipsModel.deletedBy = jsonObject.optString("deletedBy")
                }
                if (jsonObject.has("adminName")) {
                    relationshipsModel.adminName = jsonObject.getString("adminName")
                }
                if (jsonObject.has("isDisabled")) {
                    relationshipsModel.istemp = true
                    relationshipsModel.isDisabled = jsonObject.getBoolean("isDisabled")
                }
                if (jsonObject.has("isdisabled")) {
                    relationshipsModel.istemp = true
                    relationshipsModel.isDisabled = jsonObject.getBoolean("isdisabled")
                }
                if (jsonObject.has("canAccess")) {
                    relationshipsModel.canAccess = jsonObject.optBoolean("canAccess")
                }
                if (jsonObject.has("created_on")) {
                    relationshipsModel.created = jsonObject.getString("created_on")
                } else {
                    relationshipsModel.created = jsonObject.getString("created")
                }
                if (jsonObject.has("consent")) {
                    relationshipsModel.consent = jsonObject.getString("consent")
                }
                if (jsonObject.has("guid")) {
                    relationshipsModel.guid = jsonObject.getString("guid")
                }
                if (jsonObject.has("isAccepted")) {
                    relationshipsModel.isAccepted = jsonObject.getBoolean("isAccepted")
                }
                if (jsonObject.has("isClient")) {
                    relationshipsModel.isClient = jsonObject.getBoolean("isClient")
                }
                if (jsonObject.has("isEditable")) {
                    relationshipsModel.isEditable = jsonObject.getBoolean("isEditable")
                }
                if (jsonObject.has("members")) {
                    val members = jsonObject.getJSONArray("members")
                    relationshipsModel.membersList = members
                }
                if (jsonObject.has("matterList")) {
                    relationshipsModel.matterList = jsonObject.getJSONArray("matterList")
                }
                relationshipsList.add(relationshipsModel)
            }
            if (selectedRelId.isNotEmpty()) {
                selectedRelationshipsList.clear()
                val rel = RelationshipsModel()
                rel.id = selectedRelId
                selectedRelationshipsList.add(rel)
                showIndividualEventPopup()
            }
            loadRelationshipsRecylerview(relationshipsModel)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun showIndividualEventPopup() {
        try {
            if (relationshipPopup != null && relationshipPopup!!.isShowing) {
                relationshipPopup!!.dismiss()
            }
            val builder = AlertDialog.Builder(activity)
            val inflater = layoutInflater
            val dialog = inflater.inflate(R.layout.individual_selected_layout, null)

            val iv_close_popup = dialog.findViewById<ImageView>(R.id.iv_close_popup)
            val rv_displayEvents = dialog.findViewById<RecyclerView>(R.id.rv_view)

            relationshipPopup = builder.create()
            relationshipPopup!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            relationshipPopup!!.setView(dialog)

            val adapter = RelationshipsAdapter(
                selectedRelationshipsList,
                requireContext(),
                requireActivity(),
                this,
                Relationship_Type,
                this,
                relationshipId,
                route,
                true
            )

            rv_displayEvents.layoutManager = LinearLayoutManager(context)
            rv_displayEvents.adapter = adapter

            if (selectedRelationshipsList.isNotEmpty()) {
                rv_displayEvents.visibility = VISIBLE
            } else {
                rv_displayEvents.visibility = GONE
            }

            iv_close_popup.setOnClickListener {
                relationshipPopup!!.dismiss()
                Constants.isFromNotification = false
                Constants.notificationBundle?.clear()
                selectedRelId = ""
                relationshipId = ""
                selectedRelationshipsList.clear()
            }

            relationshipPopup!!.setOnDismissListener {
                Constants.isFromNotification = false
                Constants.notificationBundle?.clear()
                selectedRelId = ""
                relationshipId = ""
                selectedRelationshipsList.clear()
            }

            relationshipPopup!!.show()
        } catch (e: Exception) {
            Log.e("ClientRelationship", "Error showing popup", e)
            AndroidUtils.showAlert("Error displaying relationship: " + e.message, activity)
        }
    }

    private fun loadRelationshipsRecylerview(relationshipsModel: RelationshipsModel) {
        val groupsArray = relationshipsModel.groups
        if (groupsArray != null) {
            for (j in 0 until groupsArray.length()) {
                val viewGroupModel = ViewGroupModel()
                val jsonObject = groupsArray.getJSONObject(j)
                viewGroupModel.group_id = jsonObject.getString("id")
                viewGroupModel.group_name = jsonObject.getString("name")
                updatedMembersList.add(viewGroupModel)
            }
        }
        Log.i("Tag", "Info:$updatedMembersList")
        adapter = RelationshipsAdapter(relationshipsList, requireContext(), requireActivity(), this, Relationship_Type, this, relationshipId)
        rv_relationships?.adapter = adapter
        AndroidUtils.LoadingRecyclerview(rv_relationships, context)
        AndroidUtils.setupBottomSpacerFooter(rv_relationships, resources.getDimensionPixelSize(R.dimen.twentyeight_dp))

        toggleRelationshipsEmptyState(relationshipsList.isEmpty())

        et_search_view_relationships?.setOnClickListener {
            et_search_view_relationships?.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        if (s != null && s.isEmpty()) {
                            callViewRelationshipWebservice("", "")
                        }
                    }
                }
            })
        }
        btn_search?.setOnClickListener {
            callViewRelationshipWebservice("", "")
        }
        adapter?.notifyDataSetChanged()
    }

    private fun loadSearchedEntityData(data: JSONObject) {
        ac_search_entity?.setText("")
        entitySearchModel = EntitySearchModel()
        entitySearchModel!!.entityName = data.getString("entityName")
        entitySearchModel!!.email = data.getString("email")
        entitySearchModel!!.contactPerson = data.getString("contactPerson")
        entitySearchModel!!.contactPhone = data.getString("contactPhone")
        entitySearchModel!!.country = data.getString("country")
        for (i in 0 until countriesList.size) {
            if (countriesList[i].name == entitySearchModel!!.country) {
                sp_country?.setSelection(i)
                country_name = countriesList[i].name
            }
        }
        tv_sp_country?.text = country_name
        img_clear_icon?.isEnabled = false
        img_clear_icon?.visibility = VISIBLE
        img_dropdown_icon?.visibility = GONE
        iscountry_checked = true
        loadEntityUI(entitySearchModel!!)
        clearSearch()
        updateSendButtonState()
    }

    private fun loadEntityUI(entitySearchModel: EntitySearchModel) {
        disableAlpha()
        disableIndividualData()
        btn_relationships_cancel?.background = ContextCompat.getDrawable(requireContext(), R.drawable.cancel_button_background)
        btn_send_request?.background = ContextCompat.getDrawable(requireContext(), R.drawable.save_button_background)
        btn_send_request?.setAlpha(1.0f)
        btn_send_request?.isEnabled = true
        btn_relationships_cancel?.isEnabled = true
        tv_individual_email?.setText(entitySearchModel.email)
        tv_individual_email?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        tv_individual_confirm_email?.setText(entitySearchModel.email)
        tv_individual_confirm_email?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        tv_entity_contact_person?.setText(entitySearchModel.contactPerson)
        tv_entity_contact_person?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        tv_entity_phone_number?.let { AndroidUtils.NumberFilter(it, true) }
        tv_entity_phone_number?.setText(entitySearchModel.contactPhone)
        tv_entity_phone_number?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        tv_entity_name?.setText(entitySearchModel.entityName)
        tv_entity_name?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        val response_txt = "Entity " + entitySearchModel.entityName + positive_msg
        tv_response?.text = response_txt
        ll_confirm_email?.visibility = GONE
        tv_response?.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_color))

        isEmailValid = true
        isConfirmEmailValid = true
        updateSendButtonState()
    }

    private fun loadUI(searchModel: SearchModel) {
        disableAlpha()
        disableIndividualData()
        btn_relationships_cancel?.background = ContextCompat.getDrawable(requireContext(), R.drawable.cancel_button_background)
        btn_send_request?.background = ContextCompat.getDrawable(requireContext(), R.drawable.save_button_background)
        btn_send_request?.setAlpha(1.0f)
        btn_send_request?.isEnabled = true
        btn_relationships_cancel?.isEnabled = true
        tv_individual_firstname?.setText(searchModel.firstName)
        tv_individual_last_name?.setText(searchModel.lastName)
        tv_individual_email?.setText(maskEmail(et_search_individual?.text?.toString()?.trim() ?: ""))
        tv_individual_confirm_email?.setText(maskEmail(et_search_individual?.text?.toString()?.trim() ?: ""))
        et_search_individual?.setText("")

        isEmailValid = true
        isConfirmEmailValid = true
        updateSendButtonState()
    }

    private fun load_IndividualData(entity: JSONArray) {
        filteredIndividualList.clear()
        val displayNameList = ArrayList<String>()

        for (i in 0 until entity.length()) {
            val jsonObject = entity.getJSONObject(i)
            val model = IndividualModel()
            model.id = jsonObject.getString("id")
            val rawFirstName = jsonObject.optString("first_name", "")
            val rawLastName = jsonObject.optString("last_name", "")
            model.first_name = rawFirstName
            model.last_name = rawLastName
            val cleanFirstName = rawFirstName.trim()
            val cleanLastName = rawLastName.trim()
            val displayName = if (cleanFirstName.isNotEmpty() && cleanLastName.isNotEmpty()) {
                "$cleanFirstName $cleanLastName"
            } else if (cleanFirstName.isNotEmpty()) {
                cleanFirstName
            } else if (cleanLastName.isNotEmpty()) {
                cleanLastName
            } else {
                jsonObject.optString("email", "Unknown")
            }
            model.name = displayName.trim()
            model.country = jsonObject.optString("country")
            model.email = jsonObject.optString("email")
            model.confirmEmail = jsonObject.optString("email")
            model.mobile = jsonObject.optString("mobile")
            filteredIndividualList.add(model)
            displayNameList.add(model.name ?: "")
        }

        Log.d("IndividualData", "Loaded " + filteredIndividualList.size + " individuals")

        if (filteredIndividualList.size > 0) {
            val adapter = IndividualDropdownAdapter(requireContext(), filteredIndividualList)
            adapter.notifyDataSetChanged()
            ac_search_entity?.setAdapter(adapter)
            ac_search_entity?.threshold = 1
            ac_search_entity?.postDelayed({
                ac_search_entity?.showDropDown()
                Log.d("IndividualData", "Dropdown shown with " + displayNameList.size + " items")
            }, 100)

            ac_search_entity?.setOnItemClickListener { parent, view, position, id ->
                val selectedName = displayNameList[position]
                val selected = filteredIndividualList[position]
                ac_search_entity?.setText(selectedName, false)
                individualId = selected.id ?: ""
                loadIndividual(selected)
                Log.i("TAG", "Selected: " + selected.name + " ID: " + individualId)
            }
        } else {
            Log.d("IndividualData", "No individuals found in response")
            enableView()
        }

        ac_search_entity?.onFocusChangeListener = null
        ac_search_entity?.setOnClickListener(null)

        ac_search_entity?.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus && filteredIndividualList.size > 0) {
                ac_search_entity?.postDelayed({ ac_search_entity?.showDropDown() }, 50)
            }
        }

        ac_search_entity?.setOnClickListener {
            if (filteredIndividualList.size > 0) {
                ac_search_entity?.showDropDown()
            }
        }

        ac_search_entity?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s?.toString()?.trim()?.isEmpty() == true) {
                    clearSearch()
                }
            }
        })
    }

    private fun showDropdownIfNotEmpty() {
        val adapter = ac_search_entity?.adapter
        if (adapter != null && adapter.count > 0) {
            ac_search_entity?.showDropDown()
        } else {
            ac_search_entity?.dismissDropDown()
        }
    }

    private fun loadEntityData(entity: JSONArray) {
        entityList.clear()
        updatedEntityList.clear()
        for (i in 0 until entity.length()) {
            val jsonObject = entity.getJSONObject(i)
            val model = EntityModel()
            model.entityID = jsonObject.getString("entityId")
            model.name = jsonObject.getString("name")
            model.contactName = jsonObject.getString("contactName")
            entityList.add(model.name ?: "")
            updatedEntityList.add(model)
        }
        Log.i("TAG_EntityList", "EntityList:$entityList")
        val adapter = ArrayAdapter<String>(
            requireContext(),
            R.layout.spinnerdropdownview,
            R.id.spinnerDropDownTextview,
            entityList
        )
        ac_search_entity?.setAdapter(adapter)
        ac_search_entity?.threshold = 0
        ac_search_entity?.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) return@OnFocusChangeListener
            ac_search_entity?.post { showDropdownIfNotEmpty() }
        }
        ac_search_entity?.setOnClickListener { showDropdownIfNotEmpty() }

        ac_search_entity?.setOnItemClickListener { adapterView, view, position, id ->
            val selectedValue = adapter.getItem(position)
            if (selectedValue != null) {
                for (ent in updatedEntityList) {
                    if (selectedValue == ent.name) {
                        entity_id = ent.entityID ?: ""
                        break
                    }
                }
            } else {
                Log.e(TAG, "Selected value or updatedEntityList is null")
            }
        }
    }

    private fun loadCountryData() {
        val adapter = CommonSpinnerAdapter(activity, countriesList)
        Log.i("ArrayList", "Info:$countriesList")
        sp_country?.adapter = adapter
    }

    private fun loadViewGroups(data: JSONArray) {
        groupsList.clear()
        for (i in 0 until data.length()) {
            val jsonObject = data.getJSONObject(i)
            val viewGroupModel = ViewGroupModel()
            viewGroupModel.id = jsonObject.getString("id")
            val date = jsonObject.getString("created")
            val date_new = AndroidUtils.stringToDateTimeDefault(date, "yyyy-MM-dd'T'HH:mm:ss.SSS")
            val created = AndroidUtils.getDateToString(date_new, "MMM dd YYYY")
            viewGroupModel.created = created
            val members = jsonObject.getJSONArray("members")
            viewGroupModel.members = members
            viewGroupModel.description = jsonObject.getString("description")
            viewGroupModel.name = jsonObject.getString("name")
            val group_head = jsonObject.getJSONObject("groupHead")
            viewGroupModel.group_head_id = group_head.getString("id")
            viewGroupModel.group_head_name = group_head.getString("name")
            viewGroupModel.owner_name = group_head.getString("name")
            viewGroupModel.isChecked = false
            if (jsonObject.getString("name") != "AAM" && jsonObject.getString("name") != "SuperUser") {
                groupsList.add(viewGroupModel)
            }
        }
        Log.i("ArrayList", "info$groupsList")
        val mtag = "VG"
        loadGroupsRecylerview()
    }

    fun load_selected_groups(list_item: ArrayList<ViewGroupModel>) {
        val acls = JSONArray()
        if (list_item.isNotEmpty()) {
            for (i in 0 until list_item.size) {
                val viewGroupModel = list_item[i]
                if (viewGroupModel.isChecked) {
                    acls.put(viewGroupModel.id)
                }
            }
        }
        btn_send_request?.setAlpha(1.0f)
        btn_send_request?.isEnabled = true
    }

    private fun loadGroupsRecylerview() {
        FLAG = "second_click"
        rv_relationship_groups?.layoutManager = GridLayoutManager(context, 1)
        groupsAdapter = Groupsadapter_relationship(groupsList, this)
        rv_relationship_groups?.adapter = groupsAdapter
        et_search_relationships?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {
                groupsAdapter?.filter?.filter(et_search_relationships?.text?.toString()?.trim() ?: "")
            }
        })
        btn_relationships_cancel?.setOnClickListener {
            et_search_relationships?.setText("")
            groupsList.clear()
            clearIndividualData()
            tv_response?.text = ""
            enableAlpha()
            ll_groups?.visibility = GONE
        }
        btn_send_request?.setOnClickListener {
            if (RELATIONSHIP_TAG == "INDIVIDUAL") {
                individualValidationClick()
            } else if (RELATIONSHIP_TAG == "ENTITY") {
                RELATIONSHIP_TAG = "ENTITY"
                entityValidationClick()
            } else {
                entityValidationClick()
            }
        }
        chk_select_all?.setOnCheckedChangeListener { buttonView, isChecked ->
            groupsAdapter?.selectOrDeselectAll(isChecked)
        }
    }

    private fun callEntityRequestWebservice() {
        Log.i("Tag", "Country_Name:$country_name")
        if (progress_dialog == null || !progress_dialog!!.isShowing) {
            progress_dialog = AndroidUtils.get_progress(activity)
        }
        val postdata = JSONObject()
        val groups = JSONArray()
        if (groupsList.isNotEmpty()) {
            for (i in 0 until groupsList.size) {
                val viewGroupModel = groupsList[i]
                if (viewGroupModel.isChecked) {
                    groups.put(viewGroupModel.id)
                }
            }
        }
        if (entity_id == "") {
            postdata.put("country", country_name)
            postdata.put("email", tv_individual_email?.text?.toString()?.trim() ?: "")
            postdata.put("fullname", tv_entity_name?.text?.toString()?.trim() ?: "")
            postdata.put("contact_person", tv_entity_contact_person?.text?.toString()?.trim() ?: "")
            postdata.put("contact_phone", tv_entity_phone_number?.text?.toString()?.trim() ?: "")
            WebServiceHelper.callHttpWebService(
                this, requireContext(), WebServiceHelper.RestMethodType.POST,
                "v2/relationship/invite/entity", "Send Entity Request", postdata.toString()
            )
        } else {
            postdata.put("entityId", entity_id)
            postdata.put("description", "Description")
            if (RELATIONSHIP_TAG == "CORPORATE") {
                WebServiceHelper.callHttpWebService(
                    this, requireContext(), WebServiceHelper.RestMethodType.POST,
                    "v3/corporate", "Send Entity Request", postdata.toString()
                )
            } else {
                WebServiceHelper.callHttpWebService(
                    this, requireContext(), WebServiceHelper.RestMethodType.POST,
                    "v2/relationship/request/entity", "Send Entity Request", postdata.toString()
                )
            }
        }
    }

    private fun individualValidationClick() {
        if (btn_send_request?.isEnabled == false) {
            AndroidUtils.showAlert("Please fill all required fields correctly", activity)
            return
        }

        if (individualValidation()) {
            val fullName = (tv_individual_firstname?.text?.toString() ?: "") + " " + (tv_individual_last_name?.text?.toString() ?: "")
            AndroidUtils.showConfirmationDialog(
                requireContext(),
                requireContext().getString(R.string.confirm),
                requireContext().getString(R.string.are_you_sure_you_want_send_relationship_request_to) + fullName + "?",
                fullName,
                object : AndroidUtils.OnConfirmListener {
                    override fun onSave() {
                        try {
                            callIndividualRequestWebservice()
                        } catch (e: Exception) {
                            e.fillInStackTrace()
                        }
                    }

                    override fun onCancel() {}
                }
            )
        }
    }

    private fun entityValidationClick() {
        if (btn_send_request?.isEnabled == false) {
            AndroidUtils.showAlert("Please fill all required fields correctly", activity)
            return
        }

        if (entityValidation()) {
            val entityName = tv_entity_name?.text?.toString() ?: ""
            AndroidUtils.showConfirmationDialog(
                requireContext(),
                requireContext().getString(R.string.confirm),
                requireContext().getString(R.string.are_you_sure_you_want_send_relationship_request_to) + entityName + "?",
                entityName,
                object : AndroidUtils.OnConfirmListener {
                    override fun onSave() {
                        try {
                            callEntityRequestWebservice()
                        } catch (e: Exception) {
                            e.fillInStackTrace()
                        }
                    }

                    override fun onCancel() {}
                }
            )
        }
    }

    fun updateSelectAllState(allSelected: Boolean) {
        chk_select_all?.setOnCheckedChangeListener(null)
        chk_select_all?.isChecked = allSelected
        chk_select_all?.setOnCheckedChangeListener(selectAllListener)
    }

    private fun callIndividualRequestWebservice() {
        if (progress_dialog == null || !progress_dialog!!.isShowing) {
            progress_dialog = AndroidUtils.get_progress(activity)
        }
        Log.i("Tag", "Country_Name:$country_name")
        val postdata = JSONObject()
        val groups = JSONArray()
        if (groupsList.isNotEmpty()) {
            for (i in 0 until groupsList.size) {
                val viewGroupModel = groupsList[i]
                if (viewGroupModel.isChecked) {
                    groups.put(viewGroupModel.id)
                }
            }
        }
        if (individualId.isEmpty()) {
            postdata.put("country", country_name)
            postdata.put("email", tv_individual_email?.text?.toString()?.trim() ?: "")
            postdata.put("first_name", tv_individual_firstname?.text?.toString()?.trim() ?: "")
            postdata.put("last_name", tv_individual_last_name?.text?.toString()?.trim() ?: "")
            postdata.put("mobile", tv_entity_phone_number?.text?.toString()?.trim() ?: "")
            WebServiceHelper.callHttpWebService(
                this, requireContext(), WebServiceHelper.RestMethodType.POST,
                "v2/relationship/invite/consumer", "Send Request", postdata.toString()
            )
        } else {
            postdata.put("consumerId", individualId)
            postdata.put("description", "Description")
            WebServiceHelper.callHttpWebService(
                this, requireContext(), WebServiceHelper.RestMethodType.POST,
                "v2/relationship/request/consumer", "Send Request", postdata.toString()
            )
        }
    }

    private fun loadIndividual(individualModel: IndividualModel) {
        enableAlpha()
        clearIndividualFields()
        tv_individual_email?.setText(maskEmail(individualModel.email))
        tv_individual_confirm_email?.setText(maskEmail(individualModel.email))
        tv_individual_firstname?.setText(individualModel.first_name)
        tv_individual_last_name?.setText(individualModel.last_name)
        tv_entity_phone_number?.let { AndroidUtils.NumberFilterwithStar(it, true) }
        tv_entity_phone_number?.setText(maskPhoneNumber(individualModel.mobile))

        for (i in 0 until countriesList.size) {
            if (countriesList[i].name == individualModel.country) {
                sp_country?.setSelection(i)
                country_name = countriesList[i].name
            }
        }
        sp_country?.visibility = GONE
        iscountry_checked = true
        tv_individual_last_name?.isEnabled = false
        tv_individual_firstname?.isEnabled = false
        tl_individual_country?.isEnabled = false
        img_clear_icon?.isEnabled = false
        if (country_name != null) {
            if (country_name!!.isNotEmpty()) {
                tv_sp_country?.text = country_name
                img_clear_icon?.visibility = VISIBLE
                img_dropdown_icon?.visibility = GONE
            }
        } else {
            tv_sp_country?.text = country_name
            img_clear_icon?.visibility = GONE
            img_dropdown_icon?.visibility = VISIBLE
        }
        if (tv_response != null) {
            val response_txt = "Individual " + individualModel.name + positive_msg
            tv_response?.text = response_txt
            ll_confirm_email?.visibility = GONE
            tv_response?.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_color))
            Content_layout?.alpha = 1.0f
        }

        isEmailValid = true
        isConfirmEmailValid = true
        updateSendButtonState()
    }

    private fun enableView() {
        disableAlpha()
        tv_individual_confirm_email?.setText("")
        tv_individual_firstname?.setText("")
        tv_individual_last_name?.setText("")
        tv_individual_email?.setText("")
        tv_entity_phone_number?.setText("")
        tv_entity_name?.setText("")
        tv_entity_contact_person?.setText("")
        tv_entity_phone_number?.let { AndroidUtils.NumberFilter(it, true) }
        country_name = ""
        tv_individual_confirm_email?.isEnabled = true
        tv_individual_firstname?.isEnabled = true
        tv_individual_last_name?.isEnabled = true
        tv_entity_phone_number?.isEnabled = true
        tv_individual_email?.isEnabled = true
        sp_country?.visibility = GONE
        iscountry_checked = true
        tl_individual_country?.isEnabled = true
        img_clear_icon?.isEnabled = true
        tv_sp_country?.text = country_name
        img_clear_icon?.visibility = GONE
        img_dropdown_icon?.visibility = VISIBLE
        if (tv_response != null) {
            val response_txt = value + negative_msg
            tv_response?.text = response_txt
            tv_response?.setTextColor(ContextCompat.getColor(requireContext(), R.color.Red))
        }
        ll_confirm_email?.visibility = VISIBLE
        ac_search_entity?.setText("")
        clearSearch()
        resetValidationStates()
    }

    private fun loadIndividualData(result: JSONObject) {
        searchModel = SearchModel()
        disableAlpha()
        searchModel!!.error = result.optBoolean("error")
        if (searchModel!!.error) {
            Log.i("Tag", "Info:" + searchModel!!.error)
            val response_txt = (et_search_individual?.text?.toString()?.trim() ?: "") + negative_msg
            tv_response?.text = response_txt
            tv_individual_email?.setText(maskEmail(et_search_individual?.text?.toString()?.trim() ?: ""))
            tv_individual_email?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            tv_individual_confirm_email?.setText(maskEmail(et_search_individual?.text?.toString()?.trim() ?: ""))
            tv_individual_confirm_email?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            tv_individual_firstname?.let {
                it.isEnabled = true
                it.isFocusable = true
                it.isFocusableInTouchMode = true
            }
            tv_individual_last_name?.let {
                it.isFocusable = true
                it.isEnabled = true
                it.isFocusableInTouchMode = true
            }
            sp_country?.isEnabled = true
            country_name = ""
            tv_sp_country?.text = ""
            img_clear_icon?.visibility = GONE
            img_dropdown_icon?.visibility = VISIBLE
            tl_individual_country?.isEnabled = true
            img_clear_icon?.isEnabled = true
            sp_country?.setSelection(0)
            sp_country?.visibility = GONE
            ll_confirm_email?.visibility = VISIBLE
            iscountry_checked = true
        } else {
            Log.i("Tag", "Info:" + searchModel!!.error)
            tv_individual_firstname?.setText(searchModel!!.firstName)
            tv_individual_firstname?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            tv_individual_email?.setText(maskEmail(et_search_individual?.text?.toString()?.trim() ?: ""))
            tv_individual_email?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            tv_individual_confirm_email?.setText(maskEmail(et_search_individual?.text?.toString()?.trim() ?: ""))
            tv_individual_confirm_email?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            searchModel!!.firstName = result.getString("firstName")
            tv_individual_last_name?.setText(searchModel!!.lastName)
            tv_individual_last_name?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            searchModel!!.lastName = result.getString("lastName")
            searchModel!!.country = result.getString("country")
            searchModel!!.consumerID = result.getString("consumerId")
            searchModel!!.name = result.getString("name")
            tv_response?.text = searchModel!!.msg
            ll_confirm_email?.visibility = GONE
            tv_response?.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_color))
            for (i in 0 until countriesList.size) {
                if (countriesList[i].name == searchModel!!.country) {
                    sp_country?.setSelection(i)
                    country_name = countriesList[i].name
                }
            }
            sp_country?.visibility = GONE
            iscountry_checked = true
            tl_individual_country?.isEnabled = false
            img_clear_icon?.isEnabled = false
            tv_sp_country?.text = country_name
            img_clear_icon?.visibility = VISIBLE
            img_dropdown_icon?.visibility = GONE
            loadUI(searchModel!!)
        }
        searchModelsList.add(searchModel!!)
    }

    private fun clearIndividualData() {
        tv_individual_email?.isEnabled = false
        tv_individual_confirm_email?.isEnabled = false
        tl_individual_country?.isEnabled = false
        img_clear_icon?.isEnabled = false
        tv_individual_firstname?.setText("")
        et_search_relationships?.setText("")
        tv_individual_last_name?.setText("")
        tv_individual_email?.setText("")
        tv_individual_confirm_email?.setText("")
        ll_confirm_email?.visibility = VISIBLE
        tv_entity_contact_person?.setText("")
        tv_entity_phone_number?.setText("")
        tv_entity_phone_number?.let { AndroidUtils.NumberFilter(it, true) }
        et_search_individual?.setText("")
        ac_search_entity?.setText("")
        clearSearch()
        tv_sp_country?.text = ""
        img_clear_icon?.visibility = GONE
        img_dropdown_icon?.visibility = VISIBLE
        sp_country?.visibility = GONE
        for (i in 0 until countriesList.size) {
            if (countriesList[i].name == "Choose country") {
                sp_country?.setSelection(i)
            }
        }
        resetValidationStates()
    }

    private fun enableIndividualData() {
        tv_individual_firstname?.isEnabled = true
        tv_individual_last_name?.isEnabled = true
        tv_individual_email?.isEnabled = true
        tv_individual_confirm_email?.isEnabled = true
        tv_entity_phone_number?.isEnabled = true
        tv_entity_name?.isEnabled = true
        tv_entity_contact_person?.isEnabled = true
        btn_send_request?.isEnabled = true
        btn_relationships_cancel?.isEnabled = true
        tl_individual_country?.isEnabled = true
        sp_country?.isEnabled = true
    }

    private fun disableIndividualData() {
        tv_individual_firstname?.isEnabled = false
        tv_individual_last_name?.isEnabled = false
        tv_individual_email?.isEnabled = false
        tv_individual_confirm_email?.isEnabled = false
        tv_entity_phone_number?.isEnabled = false
        tv_entity_name?.isEnabled = false
        tv_entity_contact_person?.isEnabled = false
        tl_individual_country?.isEnabled = false
        img_clear_icon?.isEnabled = false
        tl_individual_country?.isEnabled = false
    }

    private fun disabledIndividualData() {
        tv_individual_firstname?.isEnabled = false
        tv_individual_last_name?.isEnabled = false
        tv_individual_email?.isEnabled = false
        tv_individual_confirm_email?.isEnabled = false
        tv_entity_phone_number?.isEnabled = false
        tv_entity_name?.isEnabled = false
        tl_individual_country?.isEnabled = false
        sp_country?.isEnabled = false
    }

    private fun individualValidation(): Boolean {
        if (individualId.isEmpty()) {
            return if (isFormValid) {
                true
            } else {
                AndroidUtils.showAlert("Please fill all required fields correctly", activity)
                false
            }
        }
        return true
    }

    private fun entityValidation(): Boolean {
        if (entity_id.isEmpty()) {
            if (isFormValid) {
                val email = tv_individual_email?.text?.toString()?.trim() ?: ""
                val confirmEmail = tv_individual_confirm_email?.text?.toString()?.trim() ?: ""

                if (email != confirmEmail) {
                    AndroidUtils.showAlert("Email and confirm email do not match", activity)
                    return false
                }

                if (!isValidEmail(email) && !isMaskedEmail(email)) {
                    AndroidUtils.showAlert("Please enter a valid email address", activity)
                    return false
                }

                return true
            } else {
                AndroidUtils.showAlert("Please fill all required fields correctly", activity)
                false
            }
        }
        return true
    }

    override fun RefreshViewRelationshipsData() {
        viewRelationshipsData()
    }

    companion object {
        @JvmField
        var FLAG: String = ""

        @JvmField
        var RELATIONSHIP_TAG: String = "INDIVIDUAL"
    }
}
