package com.digicoffer.lauditor.Matter.OldViewModels

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.digicoffer.lauditor.Relationships.Model.CountriesDO
import com.digicoffer.lauditor.Matter.Adapters.GroupsAdapter
import com.digicoffer.lauditor.Matter.Models.AdvocateModel
import com.digicoffer.lauditor.Matter.Models.ClientGroupModel
import com.digicoffer.lauditor.Matter.Models.ClientsModel
import com.digicoffer.lauditor.Matter.Models.DocumentsModel
import com.digicoffer.lauditor.Matter.Models.GroupsModel
import com.digicoffer.lauditor.Matter.Models.MatterModel
import com.digicoffer.lauditor.Matter.Models.TeamModel
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.Matter.ViewModels.Matter
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.google.android.material.textfield.TextInputEditText

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Objects

class GCT : Fragment(), View.OnClickListener, AsyncTaskCompleteListener {

    var corp_client_id = ""
    var corpAdapter: CommonSpinnerAdapter<ClientsModel>? = null
    lateinit var sp_corp_client: ListView
    var ad_dialog_copy: AlertDialog? = null
    var allClientGroups: ArrayList<ClientGroupModel> = ArrayList()
    var filteredClientsList: ArrayList<ClientsModel> = ArrayList()
    var is_fname_empty = true
    var is_lname_empty = true
    var is_email_empty = true
    var is_confirm_email_empty = true
    var matterModel = MatterModel()
    lateinit var response_email: TextView
    lateinit var response_cemail: TextView
    var isclient = true
    var iscorpclient = false
    var istempclient = false
    lateinit var matter_date: TextView
    lateinit var at_add_groups: TextView
    lateinit var at_add_clients: TextView
    lateinit var at_assigned_team_members: TextView
    lateinit var add_groups: TextView
    lateinit var add_clients: TextView
    lateinit var tv_assigned_team_members: TextView
    lateinit var at_add_corp_clients: TextView
    var upload_corp_client_layout: LinearLayout? = null
    lateinit var rv_display_upload_corp_client_docs: RecyclerView
    var selectedLanguage: BooleanArray? = null
    var selectedClients: BooleanArray? = null
    var position = 0
    var selcted_corp_Clients: BooleanArray? = null
    var selectedTM: BooleanArray? = null
    var is_error = false
    var attachment_type: String? = null
    var country_name = ""
    var client_type = "consumer"
    var success_msg = ""
    var temp_rel_id = ""
    var temp_client_id = ""
    var temp_name = ""
    var client_list = JSONArray()
    var clientsList: ArrayList<ClientsModel> = ArrayList()
    var corp_clients_list: ArrayList<ClientsModel> = ArrayList()
    var corp_clients_name: ArrayList<String> = ArrayList()
    var matterArraylist: ArrayList<MatterModel>? = null
    var countriesList: ArrayList<CountriesDO> = ArrayList()
    lateinit var matter_title_tv: TextView
    lateinit var tv_name: TextView
    lateinit var tv_selected_clients: TextView
    lateinit var tv_selected_tm: TextView
    lateinit var tv_selected_temp_clients: TextView
    lateinit var cv_details: ConstraintLayout
    var matter_title: String? = null
    var case_number: String? = null
    var case_type: String? = null
    var description: String? = null
    var dof: String? = null
    var start_date: String? = null
    var end_date: String? = null
    var court: String? = null
    var judge: String? = null
    var case_priority: String? = null
    var case_status: String? = null
    var existing_clients: JSONArray? = null
    var exisiting_group_acls: JSONArray? = null
    var existing_corp_clients: JSONArray? = null
    var existing_temp_clients: JSONArray? = null
    var existing_members: JSONArray? = null

    lateinit var cv_client_details: CardView
    lateinit var ll_add_groups: LinearLayout
    // var matterInformation: MatterInformation? = null

    var selected_documents_list: ArrayList<DocumentsModel> = ArrayList()
    var advocates_list: ArrayList<AdvocateModel> = ArrayList()
    var existing_groups_list: JSONArray? = null
    var existing_clients_list: JSONArray? = null
    var existing_tm_list: JSONArray? = null
    var existing_documents: JSONArray? = null
    var existing_documents_list: JSONArray? = null
    var ADAPTER_TAG = "Groups"
    var chosen_matter = ""
    lateinit var btn_add_groups: Button
    lateinit var btn_add_clients: Button
    lateinit var btn_add_teammembers: Button
    lateinit var btn_create: Button
    lateinit var btn_cancel_save: Button
    lateinit var btn_add_corp_clients: Button
    lateinit var ll_selected_groups: LinearLayout
    lateinit var ll_selected_clients: LinearLayout
    lateinit var ll_assigned_team_members: LinearLayout
    lateinit var selected_groups: LinearLayout
    lateinit var selected_clients: LinearLayout
    lateinit var selected_temp_clients: LinearLayout
    lateinit var ll_selected_temp_clients: LinearLayout
    lateinit var selected_tm: LinearLayout
    lateinit var ll_add_clients: LinearLayout
    lateinit var ll_assign_team_members: LinearLayout
    lateinit var selected_corp_clients: LinearLayout
    lateinit var ll_selected_corp_clients: LinearLayout
    lateinit var ll_matterDate: LinearLayout
    lateinit var tv_selected_corp_clients: TextView
    var progress_dialog: Dialog? = null
    var iscountry = true
    var selected_groups_list: ArrayList<GroupsModel> = ArrayList()
    var updated_groups_list: ArrayList<GroupsModel> = ArrayList()
    var selected_clients_list: ArrayList<ClientsModel> = ArrayList()
    var temporary_clients_list: ArrayList<ClientsModel> = ArrayList()
    var temporary_corpclients_list: ArrayList<ClientsModel> = ArrayList()
    var temporary_groups_list: ArrayList<GroupsModel> = ArrayList()
    var temporary_tm_list: ArrayList<TeamModel> = ArrayList()
    var old_clients_list: ArrayList<ClientsModel> = ArrayList()
    var selected_corp_clients_list: ArrayList<ClientsModel> = ArrayList()
    var selected_temp_clients_list: ArrayList<ClientsModel> = ArrayList()
    var selected_tm_list: ArrayList<TeamModel> = ArrayList()
    var owner_tm: ArrayList<TeamModel> = ArrayList()
    var groupsList: ArrayList<GroupsModel> = ArrayList()
    var ischecked_group = true
    lateinit var ll_save_buttons: LinearLayoutCompat
    var ischecked_client = true
    var ischecked_corp_client = true
    var ischecked_tm = true
    var documentsList: ArrayList<DocumentsModel> = ArrayList()
    var new_groupsList: ArrayList<ViewMatterModel> = ArrayList()
    var tmList: ArrayList<TeamModel> = ArrayList()
    var matter: Matter? = null
    lateinit var sp_country: ListView
    lateinit var clients_list_layout: LinearLayoutCompat
    lateinit var temp_client_layout: LinearLayout
    lateinit var ll_sp_country: LinearLayout
    lateinit var img_country_clear: ImageView
    lateinit var img_country_dropdown: ImageView
    lateinit var btn_add_temp_client: Button
    lateinit var tv_add_client: TextView
    lateinit var tv_temp_client: TextView
    lateinit var tv_corp_client: TextView
    lateinit var tv_temp_fname: TextView
    lateinit var tv_temp_lname: TextView
    lateinit var tv_temp_email: TextView
    lateinit var tv_temp_confirm_email: TextView
    lateinit var tv_temp_country: TextView
    lateinit var et_temp_country: TextView
    lateinit var tv_temp_phone: TextView
    lateinit var tv_temp_individual: TextView
    lateinit var tv_temp_entity: TextView
    lateinit var et_temp_fname: TextInputEditText
    lateinit var et_temp_lname: TextInputEditText
    lateinit var et_temp_email: TextInputEditText
    lateinit var et_temp_confirm_email: TextInputEditText
    lateinit var et_temp_phone: TextInputEditText
    lateinit var rv_display_upload_groups_docs: RecyclerView
    lateinit var rv_display_upload_client_docs: RecyclerView
    lateinit var rv_display_upload_tm_docs: RecyclerView

    var groupsAdapter: GroupsAdapter? = null
    var clientsAdapter: GroupsAdapter? = null
    var teamsAdapter: GroupsAdapter? = null
    private var existing_opponents: JSONArray? = null
    private var tag_list: ArrayList<String> = ArrayList()
    private var existing_tags_list: JSONArray? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.gct_layout, container, false)
        matter_date = view.findViewById(R.id.matter_date)
        ll_matterDate = view.findViewById(R.id.ll_matterDate)
        ll_matterDate.visibility = View.GONE
        at_add_groups = view.findViewById(R.id.at_add_groups)
        clients_list_layout = view.findViewById(R.id.clients_list_layout)
        clients_list_layout.visibility = View.GONE
        temp_client_layout = view.findViewById(R.id.temp_client_layout)
        temp_client_layout.visibility = View.GONE
        sp_corp_client = view.findViewById(R.id.sp_corp_client)

        tv_temp_fname = view.findViewById(R.id.tv_temp_fname)
        tv_temp_fname.setText(R.string.first_name)
        
        tv_temp_lname = view.findViewById(R.id.tv_temp_lname)
        tv_temp_lname.setText(R.string.last_name)
        
        tv_temp_email = view.findViewById(R.id.tv_temp_email)
        tv_temp_email.setText(R.string.email)
        
        tv_temp_confirm_email = view.findViewById(R.id.tv_temp_confirm_email)
        tv_temp_confirm_email.setText(R.string.confirm_email)
        
        tv_temp_country = view.findViewById(R.id.tv_temp_country)
        tv_temp_country.setText(R.string.country)
        sp_country = view.findViewById(R.id.sp_country)
        sp_country.visibility = View.GONE
        
        ll_sp_country = view.findViewById(R.id.ll_sp_country)
        img_country_clear = ll_sp_country.findViewById(R.id.img_clear_icon)
        img_country_dropdown = ll_sp_country.findViewById(R.id.img_dropdown_icon)
        et_temp_country = ll_sp_country.findViewById(R.id.tv_spinner_view)
        et_temp_country.setHint(R.string.select_country)
        response_email = view.findViewById(R.id.response_email)
        response_email.visibility = View.GONE
        response_cemail = view.findViewById(R.id.response_cemail)

        img_country_clear.setOnClickListener {
            AndroidUtils.DisplaySpinnerView(sp_country, et_temp_country, country_name, img_country_dropdown, img_country_clear, false)
            AddTempClientStatus()
            iscountry = true
        }
        ll_sp_country.setOnClickListener {
            AndroidUtils.display_listview(iscountry, sp_country)
            call_country_list()
            iscountry = !iscountry
        }
        
        tv_temp_phone = view.findViewById(R.id.tv_temp_phone)
        tv_temp_phone.setText(R.string.phone)
        
        et_temp_fname = view.findViewById(R.id.et_temp_fname)
        et_temp_fname.setHint(R.string.first_name)
        et_temp_fname.addTextChangedListener(Validation(et_temp_fname))
        
        et_temp_lname = view.findViewById(R.id.et_temp_lname)
        et_temp_lname.setHint(R.string.last_name)
        et_temp_lname.addTextChangedListener(Validation(et_temp_lname))
        
        et_temp_email = view.findViewById(R.id.et_temp_email)
        et_temp_email.setHint(R.string.email)
        et_temp_email.addTextChangedListener(Validation(et_temp_email))
        
        et_temp_confirm_email = view.findViewById(R.id.et_temp_confirm_email)
        et_temp_confirm_email.setHint(R.string.confirm_email)
        et_temp_confirm_email.addTextChangedListener(Validation(et_temp_confirm_email))
        
        et_temp_phone = view.findViewById(R.id.et_temp_phone)
        et_temp_phone.setHint(R.string.phone)
        et_temp_phone.addTextChangedListener(Validation(et_temp_phone))
        et_temp_phone.inputType = InputType.TYPE_CLASS_NUMBER
        val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(10))
        et_temp_phone.filters = filters
        
        tv_temp_individual = view.findViewById(R.id.tv_temp_individual)
        tv_temp_individual.setText(R.string.individual)
        tv_temp_individual.setTextColor(resources.getColor(R.color.white))
        
        tv_temp_entity = view.findViewById(R.id.tv_temp_entity)
        tv_temp_entity.background = context?.getDrawable(R.drawable.button_right_round_background)
        tv_temp_entity.setText(R.string.entity)
        
        btn_add_temp_client = view.findViewById(R.id.btn_add_temp_client)
        btn_add_temp_client.alpha = 0.5f
        btn_add_temp_client.isEnabled = false
        val commonFocusListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                AddTempClientStatus()
            }
        }
        et_temp_fname.onFocusChangeListener = commonFocusListener
        et_temp_lname.onFocusChangeListener = commonFocusListener
        
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                CheckEmailAlert()
                AddTempClientStatus()
            }
        }

        et_temp_email.addTextChangedListener(textWatcher)
        et_temp_confirm_email.addTextChangedListener(textWatcher)

        btn_add_temp_client.setOnClickListener { check_temp_values() }
        sp_country.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            country_name = countriesList[position].name ?: ""
            AndroidUtils.DisplaySpinnerView(sp_country, et_temp_country, country_name, img_country_dropdown, img_country_clear, true)
            AddTempClientStatus()
            iscountry = true
        }
        
        tv_temp_individual.setOnClickListener {
            if (et_temp_fname.text.toString().isNotEmpty() || et_temp_lname.text.toString().isNotEmpty() || et_temp_email.text.toString().isNotEmpty() || et_temp_confirm_email.text.toString().isNotEmpty() || et_temp_country.text.toString().isNotEmpty() || et_temp_phone.text.toString().isNotEmpty()) {
                Alert(false)
            } else {
                tv_temp_individual.setTextColor(resources.getColor(R.color.white))
                tv_temp_entity.setTextColor(resources.getColor(R.color.black))
                client_type = "consumer"
                tv_temp_individual.background = context?.getDrawable(R.drawable.button_left_green_round_background)
                tv_temp_entity.background = context?.getDrawable(R.drawable.button_right_round_background)
                tv_temp_lname.setText(R.string.last_name)
                et_temp_lname.setHint(R.string.last_name)
                tv_temp_fname.setText(R.string.first_name)
                et_temp_fname.setHint(R.string.first_name)
            }
        }

        tv_temp_entity.setOnClickListener {
            if (et_temp_fname.text.toString().isNotEmpty() || et_temp_lname.text.toString().isNotEmpty() || et_temp_email.text.toString().isNotEmpty() || et_temp_confirm_email.text.toString().isNotEmpty() || et_temp_country.text.toString().isNotEmpty() || et_temp_phone.text.toString().isNotEmpty()) {
                Alert(true)
            } else {
                tv_temp_individual.setTextColor(resources.getColor(R.color.black))
                tv_temp_entity.setTextColor(resources.getColor(R.color.white))
                client_type = "entity"
                tv_temp_individual.background = context?.getDrawable(R.drawable.button_left_round_background)
                tv_temp_entity.background = context?.getDrawable(R.drawable.button_right_green_round_background)
                tv_temp_lname.setText(R.string.contact_person)
                et_temp_lname.setHint(R.string.contact_person)
                tv_temp_fname.setText(R.string.firm_name)
                et_temp_fname.setHint(R.string.firm_name)
            }
        }

        tv_add_client = view.findViewById(R.id.tv_add_client)
        tv_add_client.setText(R.string.add_clients)
        tv_add_client.setTextColor(resources.getColor(R.color.white))

        tv_temp_client = view.findViewById(R.id.tv_temp_client)
        tv_temp_client.setText(R.string.temp_clients)
        tv_temp_client.background = context?.getDrawable(R.drawable.radiobutton_centre_background)

        tv_corp_client = view.findViewById(R.id.tv_corp_client)
        tv_corp_client.setText(R.string.corporate_clients)
        tv_corp_client.background = context?.getDrawable(R.drawable.button_right_round_background)

        at_add_groups.setHint(R.string.select_assign_groups)
        tv_selected_clients = view.findViewById(R.id.tv_selected_clients)
        tv_selected_clients.setText(R.string.selected_clients)
        selected_corp_clients = view.findViewById(R.id.selected_corp_clients)
        selected_corp_clients.visibility = View.GONE
        tv_selected_corp_clients = view.findViewById(R.id.tv_selected_corp_clients)
        tv_selected_corp_clients.setText(R.string.selected_corporate_clients)
        tv_selected_tm = view.findViewById(R.id.tv_selected_tm)
        tv_selected_tm.setText(R.string.assigned_team_members)
        tv_name = view.findViewById(R.id.tv_name)
        tv_name.setText(R.string.selected_groups)
        at_add_groups.setOnClickListener(this)
        add_groups = view.findViewById(R.id.add_groups)
        btn_cancel_save = view.findViewById(R.id.btn_cancel_save)
        cv_details = view.findViewById(R.id.cv_details)
        ll_save_buttons = view.findViewById(R.id.ll_save_buttons)
        ll_save_buttons.visibility = View.GONE
        ll_add_clients = view.findViewById(R.id.ll_add_clients)
        ll_add_clients.visibility = View.GONE
        rv_display_upload_groups_docs = view.findViewById(R.id.rv_display_upload_groups_docs)
        rv_display_upload_groups_docs.background = context?.getDrawable(R.drawable.rectangle_light_grey_bg)
        rv_display_upload_groups_docs.visibility = View.GONE
        rv_display_upload_client_docs = view.findViewById(R.id.rv_display_upload_client_docs)
        rv_display_upload_client_docs.background = context?.getDrawable(R.drawable.rectangle_light_grey_bg)
        rv_display_upload_client_docs.visibility = View.GONE

        rv_display_upload_corp_client_docs = view.findViewById(R.id.rv_display_upload_corp_client_docs)
        rv_display_upload_corp_client_docs.background = context?.getDrawable(R.drawable.rectangle_light_grey_bg)
        rv_display_upload_corp_client_docs.visibility = View.GONE

        rv_display_upload_tm_docs = view.findViewById(R.id.rv_display_upload_tm_docs)
        rv_display_upload_tm_docs.background = context?.getDrawable(R.drawable.rectangle_light_grey_bg)
        rv_display_upload_tm_docs.visibility = View.GONE
        ll_add_groups = view.findViewById(R.id.ll_add_groups)
        add_groups.setText(R.string.assign_group)
        add_clients = view.findViewById(R.id.add_clients)
        add_clients.setText(R.string.add_clients)
        cv_client_details = view.findViewById(R.id.cv_client_details)
        tv_assigned_team_members = view.findViewById(R.id.tv_assigned_team_members)
        tv_assigned_team_members.setText(R.string.assign_team_members)
        at_add_clients = view.findViewById(R.id.at_add_clients)
        at_add_clients.setHint(R.string.select_clients)
        at_add_corp_clients = view.findViewById(R.id.at_add_corp_clients)
        at_add_corp_clients.visibility = View.GONE
        at_add_corp_clients.setHint(R.string.select_corporate_client)
        matter_title_tv = view.findViewById(R.id.matter_title)
        matter_title_tv.visibility = View.VISIBLE
        at_add_clients.setOnClickListener(this)
        at_assigned_team_members = view.findViewById(R.id.at_assigned_team_members)
        at_assigned_team_members.setHint(R.string.select_assign_team_members)
        btn_add_groups = view.findViewById(R.id.btn_add_groups)
        btn_add_groups.setText(R.string.add)
        selected_groups = view.findViewById(R.id.selected_groups)
        selected_clients = view.findViewById(R.id.selected_clients)
        selected_clients.visibility = View.GONE
        selected_tm = view.findViewById(R.id.selected_tm)
        selected_tm.visibility = View.GONE
        btn_add_corp_clients = view.findViewById(R.id.btn_add_corp_clients)
        btn_add_corp_clients.setText(R.string.add)

        btn_add_clients = view.findViewById(R.id.btn_add_clients)
        btn_add_clients.setText(R.string.add)

        ll_assign_team_members = view.findViewById(R.id.ll_assign_team_members)
        ll_assign_team_members.visibility = View.GONE
        
        btn_create = view.findViewById(R.id.btn_create)
        btn_create.setOnClickListener(this)
        btn_add_teammembers = view.findViewById(R.id.btn_assigned_team_members)
        btn_add_teammembers.setText(R.string.add)
        AndroidUtils.ToggleButton(selected_groups_list.size, btn_add_groups)
        AndroidUtils.ToggleButton(selected_clients_list.size, btn_add_clients)
        AndroidUtils.ToggleButton(selected_tm_list.size, btn_add_teammembers)
        ll_selected_groups = view.findViewById(R.id.ll_selected_groups)
        ll_assigned_team_members = view.findViewById(R.id.ll_assigned_team_members)
        ll_selected_clients = view.findViewById(R.id.ll_selected_clients)
        ll_selected_temp_clients = view.findViewById(R.id.ll_selected_temp_clients)
        selected_temp_clients = view.findViewById(R.id.selected_temp_clients)
        tv_selected_temp_clients = view.findViewById(R.id.tv_selected_temp_clients)
        tv_selected_temp_clients.setText(R.string.selected_temp_clients)
        ll_selected_corp_clients = view.findViewById(R.id.ll_selected_corp_clients)

        tv_add_client.setOnClickListener { loadAddClient() }
        tv_temp_client.setOnClickListener { AddTemp() }
        tv_corp_client.setOnClickListener { AddCorporate() }
        
        at_add_groups.setOnClickListener {
            if (ischecked_group) {
                GroupsPopup()
                rv_display_upload_groups_docs.visibility = View.VISIBLE
            } else {
                rv_display_upload_groups_docs.visibility = View.GONE
                loadGroupsText()
            }
            ischecked_group = !ischecked_group
        }
        
        at_add_corp_clients.setOnClickListener {
            if (ischecked_corp_client) {
                sp_corp_client.visibility = View.VISIBLE
                call_corporate_clients()
            } else {
                sp_corp_client.visibility = View.GONE
            }
            ischecked_corp_client = !ischecked_corp_client
        }
        
        at_add_clients.setOnClickListener {
            if (ischecked_client) {
                if (clientsList.isEmpty()) {
                    callClientsWebservice()
                } else {
                    rv_display_upload_client_docs.visibility = View.VISIBLE
                    ClientssPopUp()
                }
            } else {
                rv_display_upload_client_docs.visibility = View.GONE
                loadClientsText()
            }
            ischecked_client = !ischecked_client
        }

        at_assigned_team_members.setOnClickListener {
            if (ischecked_tm) {
                if (!Constants.create_matter) {
                    rv_display_upload_tm_docs.visibility = View.VISIBLE
                    TeamPopUp()
                } else {
                    if (tmList.isEmpty()) {
                        // callTMWebservice()
                    } else {
                        rv_display_upload_tm_docs.visibility = View.VISIBLE
                        TeamPopUp()
                    }
                }
            } else {
                rv_display_upload_tm_docs.visibility = View.GONE
                loadTeamText()
            }
            ischecked_tm = !ischecked_tm
        }

        AndroidUtils.ToggleButton(temporary_corpclients_list.size, btn_add_corp_clients)
        sp_corp_client.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            temporary_corpclients_list.clear()
            val clientsModel = corp_clients_list[position]
            temporary_corpclients_list.add(clientsModel)
            at_add_corp_clients.text = clientsModel.client_name
            AndroidUtils.ToggleButton(temporary_corpclients_list.size, btn_add_corp_clients)
        }
        
        matter = parentFragment as Matter?
        
        btn_cancel_save.setOnClickListener {
            if (!Constants.create_matter) {
                matter?.loadViewUI()
            } else {
                AndroidUtils.showConfirmation(
                        requireActivity(),
                        requireContext().getString(R.string.leavepage),
                        requireContext().getString(R.string.changes_you_made_may_not_be_saved),
                        requireContext().getString(R.string.leave),
                        object : AndroidUtils.OnConfirmListener {
                            override fun onSave() {
                                matter?.loadViewUI()
                            }
                            override fun onCancel() {}
                        }
                )
            }
        }
        
        btn_create.setOnClickListener {
            cv_client_details.visibility = View.VISIBLE
            cv_details.visibility = View.VISIBLE
            if (!Constants.create_matter) {
                try {
                    update_matter()
                } catch (e: Exception) {
                    e.fillInStackTrace()
                }
            } else {
                saveGCTinformation()
            }
        }
        
        btn_add_clients.setOnClickListener {
            ischecked_client = true
            rv_display_upload_client_docs.visibility = View.GONE
            selected_clients_list.clear()
            selected_clients_list.addAll(temporary_clients_list)
            documentsList.clear()
            loadClients()
            if (!Constants.create_matter) {
                AndroidUtils.ToggleButton(temporary_clients_list.size, btn_create)
            }
        }
        ToggleClient()
        return view
    }
// END_OF_CHUNK
    override fun onResume() {
        super.onResume()
        if (Constants.create_matter) {
            btn_create.setText(R.string.create)
            btn_create.alpha = 0.5f
            btn_create.isEnabled = false
        } else {
            btn_create.setText(R.string.save)
        }
        
        btn_add_groups.setOnClickListener {
            ischecked_group = true
            rv_display_upload_groups_docs.visibility = View.GONE
            selected_groups_list.clear()
            selected_groups_list.addAll(temporary_groups_list)
            updateDisplay()
            loadSelectedGroups(Array(selected_groups_list.size) { "" })
            
            if (Constants.create_matter) {
                if (selected_groups_list.isNotEmpty()) {
                    callTMWebservice()
                } else {
                    selected_tm_list.clear()
                    tmList.clear()
                    at_assigned_team_members.text = ""
                }
            } else {
                callTMWebservice()
            }
            if (!Constants.create_matter) {
                btn_create.alpha = 1.0f
                btn_create.isEnabled = true
            }
            documentsList.clear()
        }

        btn_add_teammembers.setOnClickListener {
            ischecked_tm = true
            rv_display_upload_tm_docs.visibility = View.GONE
            selected_tm_list.clear()
            selected_tm_list.addAll(temporary_tm_list)
            loadSelectedTM(Array(selected_tm_list.size) { "" })
            if (!Constants.create_matter) {
                btn_create.alpha = 1.0f
                btn_create.isEnabled = true
            }
            documentsList.clear()
        }

        btn_add_corp_clients.setOnClickListener {
            selected_corp_clients_list.clear()
            selected_corp_clients_list.addAll(temporary_corpclients_list)
            loadSelectedCorp_Clients()
            sp_corp_client.visibility = View.GONE
            loadClients()
            if (!Constants.create_matter) {
                btn_create.alpha = 1.0f
                btn_create.isEnabled = true
            }
            documentsList.clear()
            ischecked_corp_client = true
        }

        if (Constants.create_matter) {
            callGroupsWebservice()
        } else {
            chosen_matter = Constants.MATTER_TYPE ?: ""
            load_existing_matter()
        }
    }

    private fun AddTemp() {
        istempclient = true
        iscorpclient = false
        isclient = false
        tv_temp_client.background = context?.getDrawable(R.drawable.radiobutton_centre_green_background)
        tv_corp_client.background = context?.getDrawable(R.drawable.button_right_round_background)
        tv_add_client.background = context?.getDrawable(R.drawable.button_left_round_background)
        tv_temp_client.setTextColor(resources.getColor(R.color.white))
        tv_add_client.setTextColor(resources.getColor(R.color.black))
        tv_corp_client.setTextColor(resources.getColor(R.color.black))
        temp_client_layout.visibility = View.VISIBLE
        clients_list_layout.visibility = View.GONE
        upload_corp_client_layout?.visibility = View.GONE
        cv_client_details.visibility = View.VISIBLE
    }

    private fun loadAddClient() {
        istempclient = false
        iscorpclient = false
        isclient = true
        tv_add_client.background = context?.getDrawable(R.drawable.button_left_green_round_background)
        tv_temp_client.background = context?.getDrawable(R.drawable.radiobutton_centre_background)
        tv_corp_client.background = context?.getDrawable(R.drawable.button_right_round_background)
        tv_add_client.setTextColor(resources.getColor(R.color.white))
        tv_temp_client.setTextColor(resources.getColor(R.color.black))
        tv_corp_client.setTextColor(resources.getColor(R.color.black))
        temp_client_layout.visibility = View.GONE
        clients_list_layout.visibility = View.VISIBLE
        upload_corp_client_layout?.visibility = View.GONE
    }

    private fun AddCorporate() {
        iscorpclient = true
        istempclient = false
        isclient = false
        tv_corp_client.background = context?.getDrawable(R.drawable.button_right_green_round_background)
        tv_temp_client.background = context?.getDrawable(R.drawable.radiobutton_centre_background)
        tv_add_client.background = context?.getDrawable(R.drawable.button_left_round_background)
        tv_corp_client.setTextColor(resources.getColor(R.color.white))
        tv_temp_client.setTextColor(resources.getColor(R.color.black))
        tv_add_client.setTextColor(resources.getColor(R.color.black))
        temp_client_layout.visibility = View.GONE
        clients_list_layout.visibility = View.GONE
        upload_corp_client_layout?.visibility = View.VISIBLE
    }

    private fun CheckEmailAlert() {
        if (!AndroidUtils.isValidEmail(et_temp_email.text.toString().trim())) {
            response_email.visibility = View.VISIBLE
            response_email.text = "Please enter valid email."
            response_email.setTextColor(resources.getColor(R.color.Red))
            is_error = true
        } else {
            response_email.visibility = View.GONE
        }
        if (et_temp_email.text.toString().trim() != et_temp_confirm_email.text.toString().trim()) {
            response_cemail.visibility = View.VISIBLE
            response_cemail.text = "Email does not match"
            response_cemail.setTextColor(resources.getColor(R.color.Red))
            is_error = true
        } else {
            response_cemail.visibility = View.GONE
            is_error = false
        }
    }
// END_OF_CHUNK
    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        try {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            if (httpResult?.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val result = JSONObject(httpResult.responseContent)
                    var error = result.getBoolean("error")
                    var msg = result.getString("msg")

                    if (httpResult.requestName == "groups") {
                        if (error) {
                            AndroidUtils.showAlert(msg, requireActivity())
                        } else {
                            val data = result.getJSONArray("data")
                            var clientGroups: JSONArray? = null
                            if (result.has("client_groups")) {
                                clientGroups = result.getJSONArray("client_groups")
                            }
                            loadGroupsData(data, clientGroups)
                        }
                    } else if (httpResult.requestName == "clients") {
                        if (error) {
                            AndroidUtils.showAlert(msg, requireActivity())
                        } else {
                            val data = result.getJSONArray("data")
                            loadClientsData(data)
                        }
                    } else if (httpResult.requestName == "attachment_corp_clients") {
                        if (error) {
                            AndroidUtils.showAlert(msg, requireActivity())
                        } else {
                            val data = result.getJSONArray("data")
                            loadCorpClientsData(data)
                        }
                    } else if (httpResult.requestName == "TeamMembers") {
                        if (error) {
                            AndroidUtils.showAlert(msg, requireActivity())
                        } else {
                            val data = result.getJSONArray("users")
                            loadTMData(data)
                        }
                    } else if (httpResult.requestName == "temp_client") {
                        if (error) {
                            AndroidUtils.showAlert(msg, requireActivity())
                        } else {
                            temp_name = et_temp_fname.text.toString() + " " + et_temp_lname.text.toString()
                            temp_rel_id = result.getString("relationship_id")
                            temp_client_id = result.getString("client_id")
                            if (!Constants.create_matter) {
                                val clientsModel = ClientsModel()
                                clientsModel.client_id = temp_client_id
                                clientsModel.client_name = temp_name
                                clientsModel.client_type = client_type
                                temporary_clients_list.add(clientsModel)
                                selected_temp_clients_list.add(clientsModel)
                                loadSelectedTemp_Clients()
                                loadClients()
                            }
                            val ad_dialog = AlertDialog.Builder(requireContext())
                            ad_dialog.setMessage("Invite has been sent successfully")
                            ad_dialog.setPositiveButton("OK") { dialogInterface, _ ->
                                if (Constants.create_matter) {
                                    val clientsModel = ClientsModel()
                                    clientsModel.client_id = temp_client_id
                                    clientsModel.client_name = temp_name
                                    clientsModel.client_type = client_type
                                    temporary_clients_list.add(clientsModel)
                                    selected_temp_clients_list.add(clientsModel)
                                    loadSelectedTemp_Clients()
                                    loadClients()
                                }
                                et_temp_fname.setText("")
                                et_temp_lname.setText("")
                                et_temp_email.setText("")
                                et_temp_confirm_email.setText("")
                                et_temp_phone.setText("")
                                et_temp_country.text = ""
                                dialogInterface.dismiss()
                            }
                            val dialog = ad_dialog.create()
                            dialog.show()
                            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            positiveButton.setTextColor(resources.getColor(R.color.blue))
                        }
                    } else if (httpResult.requestName == "matter_update") {
                        if (error) {
                            AndroidUtils.showAlert(msg, requireActivity())
                        } else {
                            val ad_dialog = AlertDialog.Builder(requireContext())
                            ad_dialog.setMessage(msg)
                            ad_dialog.setPositiveButton("OK") { dialogInterface, _ ->
                                matter?.loadViewUI()
                            }
                            val dialog = ad_dialog.create()
                            dialog.show()
                        }
                    } else if (httpResult.requestName == "chosen_member") {
                        if (error) {
                            AndroidUtils.showAlert(msg, requireActivity())
                        } else {
                            val data = result.optJSONObject("data")
                            if (data != null) {
                                existing_members = data.optJSONArray("members") ?: JSONArray()
                                existing_clients = data.optJSONArray("clients") ?: JSONArray()
                                existing_corp_clients = data.optJSONArray("corporate") ?: JSONArray()
                                if (Constants.allClientGroups.isEmpty()) {
                                    callGroupsWebservice()
                                } else {
                                    display_existing_members(existing_members ?: JSONArray(), existing_clients ?: JSONArray(), existing_corp_clients ?: JSONArray())
                                    callGroupsWebservice()
                                }
                            }
                        }
                    } else if (httpResult.requestName == "attachment_clients") {
                        if (error) {
                            AndroidUtils.showAlert(msg, requireActivity())
                        } else {
                            val data = result.getJSONArray("data")
                            loadClientsData(data)
                        }
                    } else if (httpResult.requestName == "attachment_members") {
                        if (error) {
                            AndroidUtils.showAlert(msg, requireActivity())
                        } else {
                            val data = result.getJSONArray("data")
                            loadTMData(data)
                        }
                    } else if (httpResult.requestName == "countries") {
                        val data = result.getJSONArray("data")
                        load_countries_data(data)
                    }
                } catch (e: Exception) {
                    e.fillInStackTrace()
                }
            } else {
                AndroidUtils.showAlert(httpResult?.responseContent ?: "Unknown Error", requireActivity())
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun load_countries_data(data: JSONArray) {
        try {
            countriesList.clear()
            for (i in 0 until data.length()) {
                val jsonObject = data.getJSONArray(i)
                val countriesDO = CountriesDO()
                countriesDO.name = jsonObject.getString(0)
                countriesDO.dial_code = jsonObject.getString(1)
                countriesDO.code = jsonObject.getString(2)
                countriesList.add(countriesDO)
            }
            load_countries()
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
    }

    private fun load_countries() {
        val countryAdapter = CommonSpinnerAdapter(requireActivity(), countriesList)
        sp_country.adapter = countryAdapter
    }

// END_OF_CHUNK
    private fun callGroupsWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(activity)
            val postdata = JSONObject()
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v3/matter/groups", "groups", postdata.toString())
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun callClientsWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(activity)
            val postdata = JSONObject()
            if (Constants.create_matter) {
                val group_acls = JSONArray()
                for (i in selected_groups_list.indices) {
                    val groupsModel = selected_groups_list[i]
                    group_acls.put(groupsModel.group_id)
                }
                postdata.put("group_acls", group_acls)
            } else {
                postdata.put("group_acls", Constants.ex_group_attachment)
            }
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.POST, "v3/matter/clients", "clients", postdata.toString())
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun callTMWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(activity)
            val postdata = JSONObject()
            if (Constants.create_matter) {
                val group_acls = JSONArray()
                for (i in selected_groups_list.indices) {
                    val groupsModel = selected_groups_list[i]
                    group_acls.put(groupsModel.group_id)
                }
                postdata.put("group_acls", group_acls)
            } else {
                postdata.put("group_acls", Constants.ex_group_attachment)
            }
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.POST, "v3/matter/members", "TeamMembers", postdata.toString())
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }
    
    private fun TeamPopUp() {
        try {
            for (i in tmList.indices) {
                tmList[i].isChecked = false
                for (j in selected_tm_list.indices) {
                    if (tmList[i].tm_id == selected_tm_list[j].tm_id) {
                        tmList[i].isChecked = true
                    }
                }
            }
            val dialogBuilder = AlertDialog.Builder(context)
            val inflater = requireActivity().layoutInflater
            val dialog = dialogBuilder.create()
            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            rv_display_upload_tm_docs.layoutManager = layoutManager
            ADAPTER_TAG = "TM"
            val documentsAdapter = GroupsAdapter(groupsList, clientsList, tmList, new_groupsList, ADAPTER_TAG, this)
            rv_display_upload_tm_docs.adapter = documentsAdapter
            teamsAdapter = documentsAdapter
            AndroidUtils.LoadList(rv_display_upload_tm_docs, context, tmList.size, false)
        } catch (e: Exception) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }
    
    fun ToggleClient() {
        if (!selected_clients_list.isEmpty() || !selected_temp_clients_list.isEmpty() || !selected_corp_clients_list.isEmpty()) {
            ll_add_groups.visibility = View.VISIBLE
            AndroidUtils.ToggleButton(temporary_groups_list.size, btn_add_groups)
            ischecked_group = true
            rv_display_upload_groups_docs.visibility = View.GONE
            
            if (selected_clients_list.isNotEmpty()) {
                loadSelectedClients(Array(selected_clients_list.size) { "" })
                ll_selected_clients.visibility = View.VISIBLE
            }
            if (selected_corp_clients_list.isNotEmpty()) {
                loadSelectedCorp_Clients()
                ll_selected_corp_clients.visibility = View.VISIBLE
            }
            if (selected_temp_clients_list.isNotEmpty()) {
                loadSelectedTemp_Clients()
                ll_selected_temp_clients.visibility = View.VISIBLE
            }
        } else {
            at_add_clients.text = ""
            at_add_groups.text = ""
            at_add_corp_clients.text = ""
            selected_clients.visibility = View.GONE
            ll_selected_clients.visibility = View.GONE
            selected_corp_clients.visibility = View.GONE
            ll_selected_corp_clients.visibility = View.GONE
            selected_temp_clients.visibility = View.GONE
            ll_selected_temp_clients.visibility = View.GONE
            ll_selected_groups.removeAllViews()
            rv_display_upload_groups_docs.visibility = View.GONE
            ll_assign_team_members.visibility = View.GONE
            ll_add_groups.visibility = View.GONE
            selected_groups_list.clear()
            temporary_tm_list.clear()
            temporary_groups_list.clear()
            selected_tm_list.clear()
            loadSelectedGroups(Array(selected_groups_list.size) { "" })
        }
        if (selected_clients_list.isEmpty() && selected_corp_clients_list.isEmpty() && selected_temp_clients_list.isEmpty()) {
            selected_tm.visibility = View.GONE
            ll_save_buttons.visibility = View.GONE
        }
        AddGctDetails()
    }

    private fun ClientssPopUp() {
        try {
            for (i in clientsList.indices) {
                for (j in selected_clients_list.indices) {
                    if (clientsList[i].client_id == selected_clients_list[j].client_id) {
                        val clientsModel = clientsList[i]
                        clientsModel.isChecked = true
                    }
                }
            }
            val dialogBuilder = AlertDialog.Builder(context)
            val inflater = requireActivity().layoutInflater
            val dialog = dialogBuilder.create()
            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            rv_display_upload_client_docs.layoutManager = layoutManager
            ADAPTER_TAG = "Clients"
            val documentsAdapter = GroupsAdapter(groupsList, clientsList, tmList, new_groupsList, ADAPTER_TAG, this)
            rv_display_upload_client_docs.adapter = documentsAdapter
            clientsAdapter = documentsAdapter
            AndroidUtils.LoadList(rv_display_upload_client_docs, context, clientsList.size, false)
        } catch (e: Exception) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }
// END_OF_CHUNK
    private fun loadTMData(data: JSONArray) {
        try {
            tmList.clear()
            for (i in 0 until data.length()) {
                val jsonObject = data.getJSONObject(i)
                val teamModel = TeamModel()
                teamModel.tm_id = jsonObject.getString("id")
                teamModel.tm_name = jsonObject.getString("name")
                
                // Do not add the user themself twice, etc.
                if (teamModel.tm_id == Constants.owner_id) {
                    continue
                }
                tmList.add(teamModel)
            }
            if (tmList.isEmpty()) {
                ll_assign_team_members.visibility = View.GONE
            } else {
                ll_assign_team_members.visibility = View.VISIBLE
            }

            for (i in selected_tm_list.indices.reversed()) {
                val selectedTM = selected_tm_list[i]
                var existsInTM = false
                
                // Skip checking owner
                if (selectedTM.tm_id == Constants.owner_id) {
                    continue
                }

                for (j in tmList.indices) {
                    if (selectedTM.tm_id == tmList[j].tm_id) {
                        tmList[j].isChecked = true
                        existsInTM = true
                        break
                    }
                }
                if (!existsInTM) {
                    selected_tm_list.removeAt(i)
                    temporary_tm_list.removeAt(i)
                }
            }

            if (!Constants.create_matter && Constants.ROLE == "SU") {
                val teamModel1 = TeamModel()
                teamModel1.tm_id = Constants.owner_id
                teamModel1.tm_name = Constants.owner_name
                owner_tm.clear()
                owner_tm.add(teamModel1)

                var ownerExists = false
                for (tm in selected_tm_list) {
                    if (tm.tm_id == Constants.owner_id) {
                        ownerExists = true
                        break
                    }
                }
                if (!ownerExists) {
                    temporary_tm_list.addAll(0, owner_tm)
                    selected_tm_list.addAll(0, owner_tm)
                }
            }

            loadSelectedTM(Array(selected_tm_list.size) { "" })
            loadTeamText()
            TeamPopUp()
            
        } catch (e: JSONException) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    private fun loadCorpClientsData(data: JSONArray) {
        try {
            corp_clients_list.clear()
            corp_clients_name.clear()
            
            for (i in 0 until data.length()) {
                val jsonObject = data.getJSONObject(i)
                val clientsModel = ClientsModel()
                clientsModel.client_id = jsonObject.getString("id")
                clientsModel.client_name = jsonObject.getString("name")
                clientsModel.client_type = jsonObject.getString("type")
                corp_clients_list.add(clientsModel)
                corp_clients_name.add(jsonObject.getString("name"))
            }

            corpAdapter = CommonSpinnerAdapter(requireActivity(), corp_clients_list)
            sp_corp_client.adapter = corpAdapter
            AndroidUtils.LoadList(sp_corp_client, context, corp_clients_list.size, false)
            
            if (corp_clients_list.isNotEmpty()) {
                callGroupsWebservice()
            }
            
        } catch (e: JSONException) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    private fun loadClientsData(data: JSONArray) {
        try {
            clientsList.clear()
            val filteredClientsList = ArrayList<ClientsModel>()

            for (i in 0 until data.length()) {
                val jsonObject = data.getJSONObject(i)
                val clientsModel = ClientsModel()
                clientsModel.client_id = jsonObject.getString("id")
                clientsModel.client_name = jsonObject.getString("name")
                clientsModel.client_type = jsonObject.getString("type")
                clientsList.add(clientsModel)
            }
            
            for (i in selected_clients_list.indices.reversed()) {
                val selectedClient = selected_clients_list[i]
                var existsInClients = false
                
                for (j in clientsList.indices) {
                    if (selectedClient.client_id == clientsList[j].client_id) {
                        clientsList[j].isChecked = true
                        existsInClients = true
                        break
                    }
                }
                if (!existsInClients) {
                    if (selectedClient.client_type == "corporate") {
                        val clientsModel = ClientsModel()
                        clientsModel.client_id = selectedClient.client_id
                        clientsModel.client_name = selectedClient.client_name
                        clientsModel.client_type = selectedClient.client_type
                        clientsList.add(clientsModel)
                    } else if (selectedClient.client_type == "consumer") {
                        val clientsModel = ClientsModel()
                        clientsModel.client_id = selectedClient.client_id
                        clientsModel.client_name = selectedClient.client_name
                        clientsModel.client_type = selectedClient.client_type
                        clientsList.add(clientsModel)
                    } else if (selectedClient.client_type == "entity") {
                        val clientsModel = ClientsModel()
                        clientsModel.client_id = selectedClient.client_id
                        clientsModel.client_name = selectedClient.client_name
                        clientsModel.client_type = selectedClient.client_type
                        clientsList.add(clientsModel)
                    } else {
                        selected_clients_list.removeAt(i)
                        temporary_clients_list.removeAt(i)
                    }
                }
            }

            loadSelectedClients(Array(selected_clients_list.size) { "" })
            loadClientsText()
            ClientssPopUp()

        } catch (e: JSONException) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    fun loadClients() {
        callClientsWebservice()
    }

    fun loadClientsText() {
        if (temporary_clients_list.isEmpty()) {
            at_add_clients.text = ""
        } else {
            val value = Array(temporary_clients_list.size) { "" }
            for (i in temporary_clients_list.indices) {
                value[i] = temporary_clients_list[i].client_name ?: ""
            }
            val str = value.joinToString(",")
            at_add_clients.text = str
        }
        AndroidUtils.ToggleButton(temporary_clients_list.size, btn_add_clients)
    }
// END_OF_CHUNK
    private fun loadTeamText() {
        val value = Array(temporary_tm_list.size) { "" }
        for (i in temporary_tm_list.indices) {
            value[i] = temporary_tm_list[i].tm_name ?: ""
        }
        val str = value.joinToString(",")
        at_assigned_team_members.text = str
        AddGctDetails()
        AndroidUtils.ToggleButton(temporary_tm_list.size, btn_add_teammembers)
    }

    private fun loadSelectedTM(value: Array<String>) {
        if (Constants.create_matter && selected_groups_list.isNotEmpty()) {
            ll_save_buttons.visibility = View.VISIBLE
        }
        ll_assign_team_members.visibility = if (tmList.isEmpty()) View.GONE else View.VISIBLE
        selected_tm.visibility = if (selected_tm_list.isEmpty()) View.GONE else View.VISIBLE

        ll_assigned_team_members.removeAllViews()

        for (i in selected_tm_list.indices) {
            val view_opponents = LayoutInflater.from(context).inflate(R.layout.edit_opponent_advocate, null)
            if (view_opponents != null) {
                val tv_opponent_name = view_opponents.findViewById<TextView>(R.id.tv_opponent_name)
                val iv_remove_opponent = view_opponents.findViewById<ImageView>(R.id.iv_remove_opponent)
                val iv_edit_opponent = view_opponents.findViewById<ImageView>(R.id.iv_edit_opponent)

                iv_edit_opponent.visibility = View.GONE

                val currentModel = selected_tm_list[i]
                tv_opponent_name?.text = currentModel.tm_name

                val isOwner = currentModel.tm_id == Constants.owner_id
                val isCurrentUser = currentModel.tm_id == Constants.USER_ID

                if (!Constants.create_matter && (isOwner || isCurrentUser)) {
                    iv_remove_opponent?.visibility = View.INVISIBLE
                } else {
                    iv_remove_opponent?.visibility = View.VISIBLE
                    iv_remove_opponent?.tag = i

                    iv_remove_opponent?.setOnClickListener { v ->
                        try {
                            val position = v.tag as Int
                            val teamModel = selected_tm_list.removeAt(position)
                            teamModel.isChecked = false

                            val iterator = temporary_tm_list.iterator()
                            while (iterator.hasNext()) {
                                val item = iterator.next()
                                if (item.tm_id == teamModel.tm_id) {
                                    iterator.remove()
                                    break
                                }
                            }

                            ll_assigned_team_members.removeViewAt(position)

                            for (j in 0 until ll_assigned_team_members.childCount) {
                                val iv_remove = ll_assigned_team_members.getChildAt(j).findViewById<ImageView>(R.id.iv_remove_opponent)
                                iv_remove?.tag = j
                            }

                            val stringBuilder = java.lang.StringBuilder()
                            for (model in selected_tm_list) {
                                stringBuilder.append(model.tm_name).append(",")
                            }
                            if (stringBuilder.isNotEmpty()) {
                                stringBuilder.deleteCharAt(stringBuilder.length - 1)
                            }
                            at_assigned_team_members.text = stringBuilder.toString()

                            selected_tm.visibility = if (selected_tm_list.isEmpty()) View.GONE else View.VISIBLE

                            if (selected_groups_list.isEmpty()) {
                                ll_assigned_team_members.removeAllViews()
                                selected_tm.visibility = View.GONE
                                temporary_tm_list.clear()
                            }

                            AddGctDetails()
                            AndroidUtils.ToggleButton(temporary_tm_list.size, btn_add_teammembers)

                        } catch (e: Exception) {
                            e.fillInStackTrace()
                            AndroidUtils.showAlert(e.message, activity)
                        }
                        btn_create.alpha = 1.0f
                        btn_create.isEnabled = true
                    }
                }
                ll_assigned_team_members.addView(view_opponents)
            }
        }
    }

    private fun loadSelectedTemp_Clients() {
        if (istempclient) {
            selected_temp_clients.visibility = View.VISIBLE
        }
        ll_selected_temp_clients.removeAllViews()

        for (i in selected_temp_clients_list.indices) {
            val view_opponents = LayoutInflater.from(context).inflate(R.layout.edit_opponent_advocate, null)
            if (view_opponents != null) {
                val tv_opponent_name = view_opponents.findViewById<TextView>(R.id.tv_opponent_name)
                val iv_remove_opponent = view_opponents.findViewById<ImageView>(R.id.iv_remove_opponent)
                val iv_edit_opponent = view_opponents.findViewById<ImageView>(R.id.iv_edit_opponent)
                
                iv_edit_opponent.visibility = View.GONE
                
                tv_opponent_name?.text = selected_temp_clients_list[i].client_name
                iv_remove_opponent?.tag = i
                iv_remove_opponent?.setOnClickListener { v ->
                    try {
                        val position = v.tag as Int
                        ll_selected_temp_clients.removeViewAt(position)
                        val clientsModel = selected_temp_clients_list.removeAt(position)
                        clientsModel.isChecked = false

                        for (j in 0 until ll_selected_temp_clients.childCount) {
                            val iv_remove = ll_selected_temp_clients.getChildAt(j).findViewById<ImageView>(R.id.iv_remove_opponent)
                            iv_remove?.tag = j
                        }
                        if (selected_temp_clients_list.isEmpty()) {
                            selected_temp_clients.visibility = View.GONE
                        } else {
                            selected_temp_clients.visibility = View.VISIBLE
                        }
                        loadClients()
                        documentsList.clear()
                        AndroidUtils.ToggleButton(selected_temp_clients_list.size, btn_add_temp_client)
                    } catch (e: Exception) {
                        e.fillInStackTrace()
                        AndroidUtils.showAlert(e.message, activity)
                    }
                }
                iv_remove_opponent?.visibility = View.VISIBLE
                ll_selected_temp_clients.addView(view_opponents)
            }
        }
    }

    private fun saveGCTinformation() {
        try {
            if (selected_clients_list.isEmpty() && selected_corp_clients_list.isEmpty() && selected_temp_clients_list.isEmpty()) {
                AndroidUtils.showAlert("Please add clients", requireActivity())
            } else if (selected_groups_list.isEmpty()) {
                AndroidUtils.showAlert("Please select group", requireActivity())
            } else if (selected_tm_list.isEmpty()) {
                AndroidUtils.showAlert("Please select team member", requireActivity())
            } else {
                updateMatterModelArrays()
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun updateMatterModelArrays() {
        val groupsArray = JSONArray()
        for (g in selected_groups_list) groupsArray.put(g.group_id)
        matterModel.group_acls = groupsArray

        val clientsArray = JSONArray()
        for (c in selected_clients_list) clientsArray.put(c.client_id)
        matterModel.clients = clientsArray

        val corpClientsArray = JSONArray()
        for (c in selected_corp_clients_list) corpClientsArray.put(c.client_id)
        matterModel.corp_clients_list = corpClientsArray

        val membersArray = JSONArray()
        for (m in selected_tm_list) membersArray.put(m.tm_id)
        matterModel.members = membersArray
    }

    fun AddGctDetails() {
        updateMatterModelArrays()
    }
// END_OF_CHUNK
    private fun loadSelectedCorp_Clients() {
        if (iscorpclient) {
            selected_corp_clients.visibility = View.VISIBLE
        }
        ll_selected_corp_clients.removeAllViews()
        val value = Array(selected_corp_clients_list.size) { "" }
        for (i in selected_corp_clients_list.indices) {
            value[i] = selected_corp_clients_list[i].client_name ?: ""
        }

        val str = value.joinToString(",")
        at_add_corp_clients.text = str
        ll_add_clients.visibility = View.VISIBLE
        clients_list_layout.visibility = View.VISIBLE

        for (i in selected_corp_clients_list.indices) {
            val view_opponents = LayoutInflater.from(context).inflate(R.layout.edit_opponent_advocate, null)
            if (view_opponents != null) {
                val tv_opponent_name = view_opponents.findViewById<TextView>(R.id.tv_opponent_name)
                val iv_remove_opponent = view_opponents.findViewById<ImageView>(R.id.iv_remove_opponent)
                val iv_edit_opponent = view_opponents.findViewById<ImageView>(R.id.iv_edit_opponent)
                
                iv_edit_opponent.visibility = View.GONE
                tv_opponent_name?.text = selected_corp_clients_list[i].client_name
                iv_remove_opponent?.tag = i
                iv_remove_opponent?.setOnClickListener { v ->
                    try {
                        val position = v.tag as Int
                        ll_selected_corp_clients.removeViewAt(position)
                        val clientsModel = selected_corp_clients_list.removeAt(position)
                        clientsModel.isChecked = false
                        
                        for (j in 0 until ll_selected_corp_clients.childCount) {
                            val iv_remove = ll_selected_corp_clients.getChildAt(j).findViewById<ImageView>(R.id.iv_remove_opponent)
                            iv_remove?.tag = j
                        }
                        
                        val stringBuilder = java.lang.StringBuilder()
                        for (model in selected_corp_clients_list) {
                            stringBuilder.append(model.client_name).append(",")
                        }
                        if (stringBuilder.isNotEmpty()) {
                            stringBuilder.deleteCharAt(stringBuilder.length - 1)
                        }
                        at_add_corp_clients.text = stringBuilder.toString()
                        
                        if (selected_corp_clients_list.isEmpty()) {
                            selected_corp_clients.visibility = View.GONE
                        } else {
                            selected_corp_clients.visibility = View.VISIBLE
                        }
                        loadClients()
                        documentsList.clear()
                        AddGctDetails()
                        AndroidUtils.ToggleButton(temporary_corpclients_list.size, btn_add_corp_clients)
                    } catch (e: Exception) {
                        e.fillInStackTrace()
                        AndroidUtils.showAlert(e.message, activity)
                    }
                }
                iv_remove_opponent?.visibility = View.VISIBLE
                ll_selected_corp_clients.addView(view_opponents)
            }
        }
    }

    private fun loadSelectedClients(value: Array<String>) {
        if (isclient) {
            selected_clients.visibility = View.VISIBLE
        }
        if (selected_clients_list.isNotEmpty()) {
            ll_selected_clients.visibility = View.VISIBLE
        }
        ll_selected_clients.removeAllViews()
        ll_add_clients.visibility = View.VISIBLE
        clients_list_layout.visibility = View.VISIBLE
        
        if (!Constants.create_matter) {
            if (selected_corp_clients_list.isNotEmpty()) {
                var isAlreadyPresent = false
                for (client in selected_clients_list) {
                    if (client.client_id == selected_corp_clients_list[0].client_id) {
                        isAlreadyPresent = true
                        break
                    }
                }
                if (!isAlreadyPresent) {
                    temporary_clients_list.add(selected_corp_clients_list[0])
                    selected_clients_list.add(selected_corp_clients_list[0])
                }
            }
        }

        for (i in selected_clients_list.indices) {
            val view_opponents = LayoutInflater.from(context).inflate(R.layout.edit_opponent_advocate, null)
            if (view_opponents != null) {
                val tv_opponent_name = view_opponents.findViewById<TextView>(R.id.tv_opponent_name)
                val iv_remove_opponent = view_opponents.findViewById<ImageView>(R.id.iv_remove_opponent)
                val iv_edit_opponent = view_opponents.findViewById<ImageView>(R.id.iv_edit_opponent)
                
                iv_edit_opponent.visibility = View.GONE
                if (!Constants.create_matter) {
                    iv_remove_opponent?.visibility = View.INVISIBLE
                } else {
                    iv_remove_opponent?.visibility = View.VISIBLE
                }
                
                tv_opponent_name?.text = selected_clients_list[i].client_name
                
                if (Constants.create_matter) {
                    iv_remove_opponent?.tag = i
                    iv_remove_opponent?.setOnClickListener { v ->
                        try {
                            val position = v.tag as Int
                            ll_selected_clients.removeViewAt(position)
                            val clientsModel = selected_clients_list.removeAt(position)
                            temporary_clients_list.removeAt(position)
                            clientsModel.isChecked = false
                            
                            for (j in 0 until ll_selected_clients.childCount) {
                                val iv_remove = ll_selected_clients.getChildAt(j).findViewById<ImageView>(R.id.iv_remove_opponent)
                                iv_remove?.tag = j
                            }
                            
                            val stringBuilder = java.lang.StringBuilder()
                            for (model in selected_clients_list) {
                                stringBuilder.append(model.client_name).append(",")
                            }
                            if (stringBuilder.isNotEmpty()) {
                                stringBuilder.deleteCharAt(stringBuilder.length - 1)
                            }
                            at_add_clients.text = stringBuilder.toString()
                            
                            if (selected_clients_list.isNotEmpty()) {
                                selected_clients.visibility = View.VISIBLE
                            } else {
                                selected_clients.visibility = View.GONE
                            }
                            
                            loadClients()
                            documentsList.clear()
                            AndroidUtils.ToggleButton(temporary_clients_list.size, btn_add_clients)
                            btn_create.alpha = 1.0f
                            btn_create.isEnabled = true
                        } catch (e: Exception) {
                            e.fillInStackTrace()
                            AndroidUtils.showAlert(e.message, activity)
                        }
                    }
                    iv_remove_opponent?.visibility = View.VISIBLE
                }
                ll_selected_clients.addView(view_opponents)
            }
        }
    }

    private fun loadGroupsData(data: JSONArray, clientGroups: JSONArray?) {
        try {
            groupsList.clear()
            selected_groups_list.clear()
            temporary_groups_list.clear()

            for (i in 0 until data.length()) {
                val jsonObject = data.getJSONObject(i)
                val groupId = jsonObject.getString("id")
                val groupName = jsonObject.getString("name")

                if (groupName != "AAM" && groupName != "SuperUser") {
                    val groupsModel = GroupsModel()
                    groupsModel.group_id = groupId
                    groupsModel.group_name = groupName
                    groupsModel.isChecked = true 
                    groupsList.add(groupsModel)
                }
            }

            allClientGroups.clear()
            Constants.allClientGroups.clear()
            if (clientGroups != null) {
                for (i in 0 until clientGroups.length()) {
                    val clientObj = clientGroups.getJSONObject(i)
                    val clientGroupModel = ClientGroupModel()
                    clientGroupModel.id = clientObj.getString("id")
                    clientGroupModel.name = clientObj.getString("name")

                    val groupsArray = clientObj.optJSONArray("groups")
                    val groupIds = ArrayList<String>()
                    if (groupsArray != null) {
                        for (j in 0 until groupsArray.length()) {
                            groupIds.add(groupsArray.getString(j))
                        }
                    }
                    clientGroupModel.groups = groupIds
                    allClientGroups.add(clientGroupModel)
                }
                Constants.allClientGroups.addAll(allClientGroups)

                for (group in groupsList) {
                    val clientsInGroup = ArrayList<ClientGroupModel>()
                    for (client in allClientGroups) {
                        if (client.groups?.contains(group.group_id ?: "") == true) {
                            clientsInGroup.add(client)
                        }
                    }
                    group.clientGroupModelList = clientsInGroup
                }
            }

            selected_groups_list.addAll(groupsList)
            temporary_groups_list.addAll(groupsList)
            selectedLanguage = BooleanArray(groupsList.size)
            Arrays.fill(selectedLanguage, true)

            loadSelectedGroups(Array(selected_groups_list.size) { "" })
            loadGroupsText()
            GroupsPopup()
            AddGctDetails()
            
            if (selected_groups_list.isNotEmpty()) {
                callTMWebservice()
            } else {
                selected_tm_list.clear()
                tmList.clear()
                at_assigned_team_members.text = ""
            }

        } catch (e: JSONException) {
            e.printStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }
// END_OF_CHUNK
    fun GroupsPopup() {
        try {
            for (i in groupsList.indices) {
                for (j in selected_groups_list.indices) {
                    if (groupsList[i].group_id == selected_groups_list[j].group_id) {
                        groupsList[i].isChecked = true
                    }
                }
            }
            
            val dialogBuilder = AlertDialog.Builder(context)
            val inflater = requireActivity().layoutInflater
            val dialog = dialogBuilder.create()
            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            rv_display_upload_groups_docs.layoutManager = layoutManager
            ADAPTER_TAG = "Groups"
            val documentsAdapter = GroupsAdapter(groupsList, clientsList, tmList, new_groupsList, ADAPTER_TAG, this)
            rv_display_upload_groups_docs.adapter = documentsAdapter
            groupsAdapter = documentsAdapter
            AndroidUtils.LoadList(rv_display_upload_groups_docs, context, groupsList.size, false)
            
            btn_add_groups.setOnClickListener {
                selected_groups_list.clear()
                selected_groups_list.addAll(temporary_groups_list)
                Add_Groups()
                ischecked_group = true
                rv_display_upload_groups_docs.visibility = View.GONE
                updateDisplay()
                documentsList.clear()
                loadSelectedGroups(Array(selected_groups_list.size) { "" })
                callTMWebservice()
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    fun Add_Groups() {
        detectListChanges()
        loadGroupsText()
    }

    private fun ClientListChanges(selected_clients_list: ArrayList<ClientsModel>) {
        val clientsModelArrayList = ArrayList(selected_clients_list)
        val originalsize = clientsModelArrayList.size
        clientsModelArrayList.removeAll(selected_clients_list)
        val newsize = clientsModelArrayList.size
        if (newsize > originalsize || newsize < originalsize) {
            tmList.clear()
            at_add_groups.text = ""
            at_assigned_team_members.text = ""
            selected_tm.visibility = View.GONE
            selected_groups.visibility = View.GONE
        }
    }

    private fun detectListChanges() {
        updated_groups_list.addAll(selected_groups_list)
        val originalSize = updated_groups_list.size
        updated_groups_list.removeAll(selected_groups_list)
        val newSize = updated_groups_list.size
        if (newSize > originalSize || newSize < originalSize) {
            ll_assigned_team_members.removeAllViews()
            selected_tm.visibility = View.GONE
        } else if (newSize == originalSize) {
            // items have been removed from the list
        } else if (newSize == 0 || originalSize == 0) {
            selected_groups.visibility = View.GONE
            ll_selected_groups.removeAllViews()
        }
        at_assigned_team_members.text = ""
        AddGctDetails()
    }

    fun loadGroupsText() {
        if (temporary_groups_list.isEmpty()) {
            at_add_groups.text = ""
        } else {
            val value = Array(temporary_groups_list.size) { "" }
            for (i in temporary_groups_list.indices) {
                value[i] = temporary_groups_list[i].group_name ?: ""
            }
            val str = value.joinToString(",")
            at_add_groups.text = str
        }
        AndroidUtils.ToggleButton(temporary_tm_list.size, btn_add_teammembers)
        AndroidUtils.ToggleButton(temporary_groups_list.size, btn_add_groups)
    }

    fun loadSelectedGroups(value: Array<String>) {
        if (selected_groups_list.isEmpty()) {
            at_add_groups.text = ""
            selected_groups.visibility = View.GONE
            ll_selected_groups.removeAllViews()
            
            // clear assigned TMs when no groups remain
            selected_tm_list.clear()
            temporary_tm_list.clear()
            ll_assigned_team_members.removeAllViews()
            selected_tm.visibility = View.GONE
        } else {
            ll_selected_groups.removeAllViews()
            selected_groups.visibility = View.VISIBLE
            AddGctDetails()

            for (i in selected_groups_list.indices) {
                val view_opponents = LayoutInflater.from(context).inflate(R.layout.edit_opponent_advocate, null)
                if (view_opponents != null) {
                    val tv_opponent_name = view_opponents.findViewById<TextView>(R.id.tv_opponent_name)
                    val iv_remove_opponent = view_opponents.findViewById<ImageView>(R.id.iv_remove_opponent)
                    val iv_edit_opponent = view_opponents.findViewById<ImageView>(R.id.iv_edit_opponent)
                    
                    iv_edit_opponent.visibility = View.GONE
                    
                    if (tv_opponent_name != null && iv_remove_opponent != null) {
                        val currentGroup = selected_groups_list[i]
                        tv_opponent_name.text = currentGroup.group_name
                        iv_remove_opponent.tag = i
                        iv_remove_opponent.setOnClickListener { v ->
                            val position = v.tag as Int
                            checkingRemovalLogic(position)
                        }
                        iv_remove_opponent.visibility = View.VISIBLE
                    }
                    ll_selected_groups.addView(view_opponents)
                }
            }
        }
    }
// END_OF_CHUNK
    fun checkingRemovalLogic(position: Int) {
        try {
            val groupsModel = selected_groups_list[position]
            var canRemove = true
            for (client in groupsModel.clientGroupModelList ?: emptyList()) {
                var hasOtherSelected = false
                for (selectedGroup in selected_groups_list) {
                    if (selectedGroup.group_id != groupsModel.group_id && client.groups?.contains(selectedGroup.group_id ?: "") == true) {
                        hasOtherSelected = true
                        break
                    }
                }
                if (!hasOtherSelected) {
                    canRemove = false
                    groupsModel.isChecked = true
                    groupsAdapter?.notifyDataSetChanged()
                    AndroidUtils.showAlert("This group cannot be removed — all selected clients must have at least one associated group.", activity)
                    break
                }
            }
            if (!canRemove) {
                return
            }

            selected_groups_list.removeAt(position)
            groupsModel.isChecked = false

            for (model in groupsList) {
                if (model.group_id == groupsModel.group_id) {
                    model.isChecked = false
                    break
                }
            }

            val iterator = temporary_groups_list.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (item.group_id == groupsModel.group_id) {
                    iterator.remove()
                    break
                }
            }

            ll_selected_groups.removeViewAt(position)

            for (j in 0 until ll_selected_groups.childCount) {
                val iv_remove = ll_selected_groups.getChildAt(j).findViewById<ImageView>(R.id.iv_remove_opponent)
                iv_remove?.tag = j
            }

            val stringBuilder = java.lang.StringBuilder()
            for (model in selected_groups_list) {
                stringBuilder.append(model.group_name).append(",")
            }
            if (stringBuilder.isNotEmpty()) {
                stringBuilder.deleteCharAt(stringBuilder.length - 1)
            }
            at_add_groups.text = stringBuilder.toString()

            documentsList.clear()
            updateDisplay()
            AndroidUtils.ToggleButton(temporary_groups_list.size, btn_add_groups)

            if (selected_groups_list.isEmpty()) {
                selected_tm_list.clear()
                temporary_tm_list.clear()
                ll_assigned_team_members.removeAllViews()
                selected_tm.visibility = View.GONE
            } else {
                callTMWebservice()
            }

            groupsAdapter?.notifyDataSetChanged()

        } catch (e: Exception) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    fun updateDisplay() {
        if (temporary_groups_list.isNotEmpty()) {
            ll_add_clients.visibility = View.VISIBLE
            clients_list_layout.visibility = View.VISIBLE
            if (tmList.isEmpty()) {
                ll_assign_team_members.visibility = View.GONE
            } else {
                ll_assign_team_members.visibility = View.VISIBLE
            }
            ll_save_buttons.visibility = View.VISIBLE
        } else {
            tmList.clear()
            ll_selected_groups.removeAllViews()
            ll_assigned_team_members.removeAllViews()
            ll_assign_team_members.visibility = View.GONE
            selected_groups.visibility = View.GONE
            selected_tm.visibility = View.GONE
            if (Constants.create_matter) {
                ll_save_buttons.visibility = View.GONE
            }
        }
    }

    private fun load_existing_matter() {
        progress_dialog = AndroidUtils.get_progress(activity)
        val postdata = JSONObject()
        WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "matter/" + chosen_matter + "/" + Constants.Matter_id + "/members", "chosen_member", postdata.toString())
    }

    private fun load_existing_member_list() {
        try {
            val postdata = JSONObject()
            postdata.put("attachment_type", "members")
            val clients = JSONArray()
            if (selected_clients_list.isNotEmpty()) {
                for (i in selected_clients_list.indices) {
                    val clientsModel = selected_clients_list[i]
                    val jsonObject = JSONObject()
                    jsonObject.put("id", clientsModel.client_id)
                    jsonObject.put("type", clientsModel.client_type)
                    clients.put(jsonObject)
                }
            }
            if (selected_corp_clients_list.isNotEmpty()) {
                for (i in selected_corp_clients_list.indices) {
                    val clientsModel = selected_corp_clients_list[i]
                    val jsonObject = JSONObject()
                    jsonObject.put("id", clientsModel.client_id)
                    jsonObject.put("type", "corporate")
                    clients.put(jsonObject)
                }
            }
            if (selected_temp_clients_list.isNotEmpty()) {
                for (i in selected_temp_clients_list.indices) {
                    val clientsModel = selected_temp_clients_list[i]
                    val jsonObject = JSONObject()
                    jsonObject.put("id", clientsModel.client_id)
                    jsonObject.put("type", "consumer")
                    clients.put(jsonObject)
                }
            }
            postdata.put("clients", clients)
            postdata.put("group_acls", Constants.ex_group_attachment)
            if (!Constants.create_matter) {
                selected_groups_list.clear()
                val groupsArray = Constants.ex_group_attachment
                if (groupsArray != null) {
                    for (i in 0 until groupsArray.length()) {
                        val groupId = groupsArray.getString(i)
                        val groupsModel = GroupsModel()
                        groupsModel.group_id = groupId
                        selected_groups_list.add(groupsModel)
                    }
                }
            }

            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.PUT, "matter/attachments", "attachment_members", postdata.toString())
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
    }

    private fun load_existing_clients_list() {
        try {
            val postdata = JSONObject()
            postdata.put("attachment_type", "clients")
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.PUT, "matter/attachments", "attachment_clients", postdata.toString())
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
    }

    private fun update_matter() {
        try {
            corp_client_id = ""
            val clients = JSONArray()
            val corp_clients = JSONArray()
            val members = JSONArray()
            for (i in selected_tm_list.indices) {
                try {
                    val teamModel = selected_tm_list[i]
                    val team_object = JSONObject()
                    team_object.put("id", teamModel.tm_id)
                    team_object.put("name", teamModel.tm_name)
                    members.put(team_object)
                } catch (e: Exception) {
                    e.fillInStackTrace()
                }
            }
            for (i in selected_corp_clients_list.indices) {
                try {
                    val clientsModel = selected_corp_clients_list[i]
                    val jsonObject = JSONObject()
                    jsonObject.put("id", clientsModel.client_id)
                    jsonObject.put("type", clientsModel.client_type)
                    jsonObject.put("name", clientsModel.client_name)
                    corp_clients.put(jsonObject)
                } catch (e: Exception) {
                    e.fillInStackTrace()
                }
            }
            for (i in selected_clients_list.indices) {
                try {
                    val clientsModel = selected_clients_list[i]
                    val jsonObject = JSONObject()
                    jsonObject.put("id", clientsModel.client_id)
                    jsonObject.put("type", clientsModel.client_type)
                    jsonObject.put("name", clientsModel.client_name)
                    clients.put(jsonObject)
                } catch (e: Exception) {
                    e.fillInStackTrace()
                }
            }
            progress_dialog = AndroidUtils.get_progress(activity)
            val postdata = JSONObject()
            postdata.put("clients", clients)
            postdata.put("members", members)
            postdata.put("corporate", corp_clients)
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.PUT, "matter/" + chosen_matter + "/" + Constants.Matter_id + "/members/update", "matter_update", postdata.toString())
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
    }
// END_OF_CHUNK
    private fun display_existing_members(members: JSONArray, clients: JSONArray, corp_clients: JSONArray) {
        selected_clients_list.clear()
        temporary_clients_list.clear()
        selected_tm_list.clear()
        temporary_tm_list.clear()
        temporary_corpclients_list.clear()
        selected_corp_clients_list.clear()
        old_clients_list.clear()

        try {
            for (p in 0 until clients.length()) {
                val clientsModel = ClientsModel()
                val jsonObject = clients.getJSONObject(p)
                clientsModel.client_id = jsonObject.getString("id")
                clientsModel.client_name = jsonObject.getString("name")
                clientsModel.client_type = jsonObject.getString("type")
                temporary_clients_list.add(clientsModel)
                selected_clients_list.add(clientsModel)
                old_clients_list.add(clientsModel)
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }

        try {
            for (p in 0 until corp_clients.length()) {
                val clientsModel = ClientsModel()
                val jsonObject = corp_clients.getJSONObject(p)
                clientsModel.client_id = jsonObject.getString("id")
                clientsModel.client_name = jsonObject.getString("name")
                clientsModel.client_type = jsonObject.getString("type")
                temporary_corpclients_list.add(clientsModel)
                selected_corp_clients_list.add(clientsModel)
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }

        try {
            owner_tm.clear()
            val teamModel1 = TeamModel()
            teamModel1.tm_id = Constants.owner_id
            teamModel1.tm_name = Constants.owner_name
            owner_tm.add(teamModel1)

            if (Constants.create_matter) {
                temporary_tm_list.addAll(0, owner_tm)
                selected_tm_list.addAll(0, owner_tm)
            }

            for (t in 0 until members.length()) {
                val teamModel = TeamModel()
                val jsonObject = members.getJSONObject(t)
                teamModel.tm_id = jsonObject.getString("id")
                teamModel.tm_name = jsonObject.getString("name")

                if (teamModel.tm_id == Constants.owner_id) {
                    if (!Constants.create_matter) {
                        var ownerExists = false
                        for (tm in selected_tm_list) {
                            if (tm.tm_id == Constants.owner_id) {
                                ownerExists = true
                                break
                            }
                        }
                        if (!ownerExists) {
                            temporary_tm_list.add(0, teamModel1)
                            selected_tm_list.add(0, teamModel1)
                        }
                    }
                    continue
                }

                temporary_tm_list.add(teamModel)
                selected_tm_list.add(teamModel)
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }

        try {
            if (selected_corp_clients_list.isNotEmpty()) {
                temporary_clients_list.addAll(selected_corp_clients_list)
            }
            selected_clients_list.addAll(selected_corp_clients_list)
            
            if (selected_clients_list.isNotEmpty()) {
                loadClientsText()
                loadSelectedClients(Array(selected_clients_list.size) { "" })
            }
            if (selected_tm_list.isNotEmpty()) {
                loadTeamText()
                loadSelectedTM(Array(selected_tm_list.size) { "" })
            }
            loadAddClient()
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun call_country_list() {
        val postdata = JSONObject()
        WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "countries", "countries", postdata.toString())
    }

    private fun add_temp_client() {
        try {
            val jsonObject = JSONObject()
            if (!Constants.create_matter) {
                jsonObject.put("group_acls", Constants.ex_group_attachment)
            } else {
                val group_acls = JSONArray()
                for (i in selected_groups_list.indices) {
                    val groupsModel = selected_groups_list[i]
                    group_acls.put(groupsModel.group_id)
                }
                jsonObject.put("group_acls", group_acls)
            }
            if (client_type == "entity") {
                jsonObject.put("fullname", et_temp_fname.text.toString())
                jsonObject.put("contact_person", et_temp_lname.text.toString())
                jsonObject.put("email", et_temp_email.text.toString())
                jsonObject.put("country", et_temp_country.text.toString())
                jsonObject.put("contact_phone", et_temp_phone.text.toString())
            } else {
                jsonObject.put("first_name", et_temp_fname.text.toString())
                jsonObject.put("last_name", et_temp_lname.text.toString())
                jsonObject.put("email", et_temp_email.text.toString())
                jsonObject.put("country", et_temp_country.text.toString())
            }
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.POST, "v3/relationship/temp-invite/$client_type", "temp_client", jsonObject.toString())
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun call_corporate_clients() {
        try {
            val postdata = JSONObject()
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v3/corporate/list", "attachment_corp_clients", postdata.toString())
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun check_temp_values() {
        if (!AndroidUtils.isValidEmail(et_temp_email.text.toString().trim())) {
            AndroidUtils.showAlert("Please Enter A Valid Email Address", requireActivity())
        } else if (et_temp_confirm_email.text.toString().trim() != et_temp_email.text.toString().trim()) {
            AndroidUtils.showAlert("Please Enter A Valid Confirm Email Address", requireActivity())
        } else {
            add_temp_client()
        }
    }

    private fun AddTempClientStatus() {
        is_fname_empty = et_temp_fname.text.toString().isEmpty()
        is_lname_empty = et_temp_lname.text.toString().isEmpty()
        is_email_empty = et_temp_email.text.toString().isEmpty()
        is_confirm_email_empty = et_temp_confirm_email.text.toString().isEmpty()
        if (is_fname_empty || is_lname_empty || is_email_empty || is_confirm_email_empty || et_temp_country.text.toString().isEmpty()) {
            btn_add_temp_client.alpha = 0.5f
            btn_add_temp_client.isEnabled = false
        } else {
            btn_add_temp_client.alpha = 1.0f
            btn_add_temp_client.isEnabled = true
        }
    }

    override fun onClick(v: View) {
        // Handle clicks if needed
    }

    private fun Alert(isEntity: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Your Data will be lost.")
            .setTitle("Confirm")
        builder.setPositiveButton("OK") { dialog, _ ->
            et_temp_fname.setText("")
            et_temp_lname.setText("")
            et_temp_country.text = ""
            et_temp_phone.setText("")
            et_temp_email.setText("")
            et_temp_confirm_email.setText("")
            
            if (isEntity) {
                tv_temp_individual.setTextColor(resources.getColor(R.color.black))
                tv_temp_entity.setTextColor(resources.getColor(R.color.white))
                client_type = "entity"
                tv_temp_individual.background = context?.getDrawable(R.drawable.button_left_round_background)
                tv_temp_entity.background = context?.getDrawable(R.drawable.button_right_green_round_background)
                tv_temp_lname.setText(R.string.contact_person)
                et_temp_lname.setHint(R.string.contact_person)
                tv_temp_fname.setText(R.string.firm_name)
                et_temp_fname.setHint(R.string.firm_name)
            } else {
                tv_temp_individual.setTextColor(resources.getColor(R.color.white))
                tv_temp_entity.setTextColor(resources.getColor(R.color.black))
                client_type = "consumer"
                tv_temp_individual.background = context?.getDrawable(R.drawable.button_left_green_round_background)
                tv_temp_entity.background = context?.getDrawable(R.drawable.button_right_round_background)
                tv_temp_lname.setText(R.string.last_name)
                et_temp_lname.setHint(R.string.last_name)
                tv_temp_fname.setText(R.string.first_name)
                et_temp_fname.setHint(R.string.first_name)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    companion object {
        fun newInstance(): GCT {
            return GCT()
        }
    }
}
