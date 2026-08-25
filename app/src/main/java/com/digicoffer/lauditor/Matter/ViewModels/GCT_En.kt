package com.digicoffer.lauditor.Matter.ViewModels

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
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
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.digicoffer.lauditor.Matter.Adapters.GroupsAdapter
import com.digicoffer.lauditor.Matter.Models.*
import com.digicoffer.lauditor.Matter.OldViewModels.MatterInformation
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Relationships.Model.CountriesDO
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.feature.matter.presentation.screen.GctScreen
import com.digicoffer.lauditor.feature.matter.presentation.viewmodel.MatterEditViewModel

class GCT_En : Fragment(), View.OnClickListener, AsyncTaskCompleteListener {

    var corp_client_id = ""
    var searchText = ""
    var entity_id = ""
    var value = ""
    var sp_corp_client: ListView? = null
    var CountryAdapter: CommonSpinnerAdapter<CountriesDO>? = null
    var isInitialLoad = true
    var ad_dialog_copy: AlertDialog? = null
    var isCancelClicked = false
    private var entityAdapter: ArrayAdapter<String>? = null
    var allClientGroups = ArrayList<ClientGroupModel>()
    var filteredClientsList = ArrayList<ClientsModel>()
    var is_fname_empty = true
    var is_lname_empty = true
    var is_email_empty = true
    var is_confirm_email_empty = true
    var matterModel = MatterModel()
    var response_email: TextView? = null
    var response_cemail: TextView? = null
    var isclient = true
    var iscorpclient = false
    var istempclient = false
    var matter_date: TextView? = null
    var at_add_groups: TextView? = null
    var at_add_clients: TextView? = null
    var at_assigned_team_members: TextView? = null
    var add_groups: TextView? = null
    var add_clients: TextView? = null
    var tv_assigned_team_members: TextView? = null
    var at_add_corp_clients: TextView? = null
    var ll_search_entity: LinearLayout? = null
    var upload_corp_client_layout: LinearLayout? = null
    var rv_display_upload_corp_client_docs: RecyclerView? = null
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
    var clientsList = ArrayList<ClientsModel>()
    var corp_clients_list = ArrayList<ClientsModel>()
    var corp_clients_name = ArrayList<String>()
    var matterArraylist: ArrayList<MatterModel>? = null
    var countriesList = ArrayList<CountriesDO>()
    var clientList = ArrayList<ClientModel>()
    var unSelectedClientList: MutableList<String> = ArrayList()
    private var selectedClient: ClientModel? = null

    var matter_title_tv: TextView? = null
    var tv_name: TextView? = null
    var tv_selected_clients: TextView? = null
    var tv_selected_tm: TextView? = null
    var tv_selected_temp_clients: TextView? = null
    var cv_details: ConstraintLayout? = null
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
    var ac_search_entity: AutoCompleteTextView? = null
    var cv_client_details: CardView? = null
    var ll_add_groups: LinearLayout? = null
    var matterInformation: MatterInformation? = null
    var btn_search_entity: AppCompatButton? = null
    var selected_documents_list = ArrayList<DocumentsModel>()
    var advocates_list = ArrayList<AdvocateModel>()
    var existing_groups_list: JSONArray? = null
    var existing_clients_list: JSONArray? = null
    var existing_tm_list: JSONArray? = null
    var existing_documents: JSONArray? = null
    var existing_documents_list: JSONArray? = null
    var ADAPTER_TAG = "Groups"
    var iv_remove_matter: ImageView? = null
    var chosen_matter = ""
    var btn_add_groups: Button? = null
    var btn_add_clients: Button? = null
    var btn_add_teammembers: Button? = null
    var btn_create: Button? = null
    var btn_cancel_save: Button? = null
    var btn_add_corp_clients: Button? = null
    var ll_selected_groups: LinearLayout? = null
    var ll_selected_clients: LinearLayout? = null
    var ll_assigned_team_members: LinearLayout? = null
    var selected_groups: LinearLayout? = null
    var selected_clients: LinearLayout? = null
    var selected_temp_clients: LinearLayout? = null
    var ll_selected_temp_clients: LinearLayout? = null
    var selected_tm: LinearLayout? = null
    var ll_add_clients: LinearLayout? = null
    var ll_assign_team_members: LinearLayout? = null
    var selected_corp_clients: LinearLayout? = null
    var ll_selected_corp_clients: LinearLayout? = null
    var ll_matterDate: LinearLayout? = null
    var tv_selected_corp_clients: TextView? = null
    var progress_dialog: Dialog? = null
    var isChangesOccured = false
    var iscountry = true
    var selected_groups_list = ArrayList<GroupsModel>()
    var updated_groups_list = ArrayList<GroupsModel>()
    var selected_clients_list = ArrayList<ClientsModel>()
    var temporary_clients_list = ArrayList<ClientsModel>()
    var temporary_corpclients_list = ArrayList<ClientsModel>()
    var temporary_groups_list = ArrayList<GroupsModel>()
    var temporary_tm_list = ArrayList<TeamModel>()
    var old_clients_list = ArrayList<ClientsModel>()
    var selected_corp_clients_list = ArrayList<ClientsModel>()
    var selected_temp_clients_list = ArrayList<ClientsModel>()
    var selected_tm_list = ArrayList<TeamModel>()
    var owner_tm = ArrayList<TeamModel>()
    var groupsList = ArrayList<GroupsModel>()
    var ischecked_group = true
    var ll_save_buttons: LinearLayoutCompat? = null
    var ischecked_client = true
    var ischecked_corp_client = true
    var ischecked_tm = true
    var documentsList = ArrayList<DocumentsModel>()
    var new_groupsList = ArrayList<ViewMatterModel>()
    var tmList = ArrayList<TeamModel>()
    var matter: Matter? = null
    var sp_country: ListView? = null
    var clients_list_layout: LinearLayoutCompat? = null
    var temp_client_layout: LinearLayout? = null
    var ll_sp_country: LinearLayout? = null
    var img_country_clear: ImageView? = null
    var img_country_dropdown: ImageView? = null
    var btn_add_temp_client: Button? = null
    var btn_cancel_temp_client: Button? = null
    var tv_client_type: TextView? = null
    var tv_warning_msg: TextView? = null
    var tv_add_client: TextView? = null
    var tv_add_clients: TextView? = null
    var tv_temp_client: TextView? = null
    var tv_corp_client: TextView? = null
    var tv_temp_fname: TextView? = null
    var tv_temp_lname: TextView? = null
    var tv_temp_email: TextView? = null
    var tv_temp_confirm_email: TextView? = null
    var tv_temp_country: TextView? = null
    var et_temp_country: TextView? = null
    var tv_temp_phone: TextView? = null
    var tv_temp_individual: TextView? = null
    var tv_temp_entity: TextView? = null
    var et_temp_fname: TextInputEditText? = null
    var et_temp_lname: TextInputEditText? = null
    var et_temp_email: TextInputEditText? = null
    var et_temp_confirm_email: TextInputEditText? = null
    var et_temp_phone: TextInputEditText? = null
    var rv_display_upload_groups_docs: RecyclerView? = null
    var rv_display_upload_client_docs: RecyclerView? = null
    var rv_display_upload_tm_docs: RecyclerView? = null

    var groupsAdapter: GroupsAdapter? = null
    var clientsAdapter: GroupsAdapter? = null
    var teamsAdapter: GroupsAdapter? = null
    private var existing_opponents: JSONArray? = null
    private var tag_list = ArrayList<String>()
    private var existing_tags_list: JSONArray? = null

    companion object {
        private var RELATIONSHIP_TAG = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        matter = parentFragment as? Matter

        return ComposeView(requireContext()).apply {
            setContent {
                val viewModel = ViewModelProvider(requireParentFragment()).get(MatterEditViewModel::class.java)
                
                LaunchedEffect(Unit) {
                    val mat = matter
                    val list = mat?.matter_arraylist
                    if (list != null && list.isNotEmpty()) {
                        viewModel.initializeFromLegacy(list[0])
                    } else {
                        viewModel.initialize(null)
                    }
                }

                com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme {
                    val mat = matter
                    val list = mat?.matter_arraylist
                    val editModel = if (list != null && list.isNotEmpty()) list[0] as? ViewMatterModel else null
                    GctScreen(
                        editModel = editModel,
                        onNavigateBack = {
                            val parent = matter
                            parent?.loadViewUI()
                        },
                        onNavigateNext = {
                            val parent = parentFragment as? Matter
                            parent?.loadDocuments()
                        },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
    private fun handleLeaveClick(): Boolean {
        if (!Constants.create_matter) {
            return isChangesOccured
        } else {
            return !selected_groups_list.isEmpty() || !selected_clients_list.isEmpty()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun callClientCheckRequests() {
        if (Constants.create_matter) {
            if (!Constants.allClientGroups.isEmpty()) {
                allClientGroups.clear()
                allClientGroups.addAll(Constants.allClientGroups)
            }
            if (!Constants.selected_temp_clients_list.isEmpty()) {
                val temp_clients = Constants.selected_temp_clients_list
                for (i in temp_clients.indices) {
                    val clientsModel = temp_clients[i]
                    addClientIfNotPresent(clientsModel)
                }
                loadselectedTempClients()
                loadClients()
            }
        }
    }

    private fun addClientIfNotPresent(newClient: ClientsModel) {
        var isPresent = false
        for (client in selected_temp_clients_list) {
            if (client.client_id == newClient.client_id) {
                isPresent = true
                break
            }
        }
        if (!isPresent) {
            selected_temp_clients_list.add(newClient)
        }
    }

    private fun CheckEmailAlert() {
        val email = et_temp_email?.text.toString().trim()
        val cemail = et_temp_confirm_email?.text.toString().trim()
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

        if (email.matches(emailPattern.toRegex())) {
            response_email?.visibility = View.GONE
            if (email == cemail) {
                response_cemail?.visibility = View.GONE
            } else if (cemail.isEmpty()) {
                response_cemail?.visibility = View.GONE
            } else {
                response_cemail?.visibility = View.VISIBLE
                response_cemail?.text = "Confirm Email Doesn't Match"
            }
        } else if (email.isEmpty()) {
            response_email?.visibility = View.GONE
        } else {
            response_email?.visibility = View.VISIBLE
            response_email?.text = "Invalid Email"
        }
    }

    private fun AddTemp() {
        temp_client_layout?.visibility = View.VISIBLE
        ll_search_entity?.visibility = View.GONE
        cv_details?.visibility = View.GONE
        clients_list_layout?.visibility = View.GONE
        tv_add_client?.setTextColor(resources.getColor(R.color.black))
        tv_temp_client?.setTextColor(resources.getColor(R.color.white))
        tv_corp_client?.setTextColor(resources.getColor(R.color.black))
        tv_temp_client?.background = context?.getDrawable(R.drawable.radiobutton_centre_green_background)
        tv_add_client?.background = context?.getDrawable(R.drawable.button_left_round_background)
        tv_corp_client?.background = context?.getDrawable(R.drawable.button_right_round_background)
        client_type = "consumer"
        tv_temp_individual?.setTextColor(resources.getColor(R.color.white))
        tv_temp_entity?.setTextColor(resources.getColor(R.color.black))
        tv_temp_individual?.background = context?.getDrawable(R.drawable.button_left_green_round_background)
        tv_temp_entity?.background = context?.getDrawable(R.drawable.button_right_round_background)
        tv_temp_lname?.setText(R.string.last_name)
        et_temp_lname?.setHint(R.string.last_name)
        tv_temp_fname?.setText(R.string.first_name)
        et_temp_fname?.setHint(R.string.first_name)
        et_temp_fname?.text = null
        et_temp_lname?.text = null
        et_temp_country?.text = ""
        et_temp_phone?.text = null
        et_temp_email?.text = null
        et_temp_confirm_email?.text = null
        tv_warning_msg?.visibility = View.GONE
        ac_search_entity?.setText("")
    }

    private fun AddCorporate() {
        temp_client_layout?.visibility = View.GONE
        clients_list_layout?.visibility = View.VISIBLE
        cv_details?.visibility = View.VISIBLE
        ll_search_entity?.visibility = View.GONE
        tv_add_client?.setTextColor(resources.getColor(R.color.black))
        tv_corp_client?.setTextColor(resources.getColor(R.color.white))
        tv_temp_client?.setTextColor(resources.getColor(R.color.black))
        tv_corp_client?.background = context?.getDrawable(R.drawable.button_right_green_round_background)
        tv_add_client?.background = context?.getDrawable(R.drawable.button_left_round_background)
        tv_temp_client?.background = context?.getDrawable(R.drawable.radiobutton_centre_background)
        iscorpclient = true
        isclient = false
        istempclient = false
        tv_warning_msg?.visibility = View.GONE
        ac_search_entity?.setText("")
        ToggleClient()
    }

    private fun searchEntity() {
        temp_client_layout?.visibility = View.GONE
        clients_list_layout?.visibility = View.GONE
        cv_details?.visibility = View.GONE
        ll_search_entity?.visibility = View.VISIBLE
        searchText = ac_search_entity?.text.toString().trim()
        if (searchText.isNotEmpty()) {
            callSearchEntityWebservice()
        } else {
            AndroidUtils.showToast("Please enter an Entity to Search", context)
        }
    }

    private fun Search_Entity() {
        temp_client_layout?.visibility = View.GONE
        clients_list_layout?.visibility = View.GONE
        cv_details?.visibility = View.GONE
        ll_search_entity?.visibility = View.VISIBLE
        tv_add_client?.setTextColor(resources.getColor(R.color.black))
        tv_temp_client?.setTextColor(resources.getColor(R.color.black))
        tv_corp_client?.setTextColor(resources.getColor(R.color.black))
        tv_temp_client?.background = context?.getDrawable(R.drawable.radiobutton_centre_background)
        tv_add_client?.background = context?.getDrawable(R.drawable.button_left_round_background)
        tv_corp_client?.background = context?.getDrawable(R.drawable.button_right_round_background)
    }

    private fun callSearchEntityWebservice() {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("search", searchText)
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v3/search/entity/$searchText", "Search Entity", jsonObject.toString())
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun loadEntityData(entity: JSONArray) {
        try {
            clientList.clear()
            for (i in 0 until entity.length()) {
                val jsonObject = entity.getJSONObject(i)
                val clientModel = ClientModel()
                clientModel.client_id = jsonObject.getString("id")
                clientModel.client_name = jsonObject.getString("name")
                clientList.add(clientModel)
            }
            if (clientList.isNotEmpty()) {
                loadFilteredEntityData()
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
    }

    private fun loadFilteredEntityData() {
        unSelectedClientList.clear()

        for (model in clientList) {
            if (!isClientSelected(model.client_id)) {
                unSelectedClientList.add(model.client_name ?: "")
            }
        }

        entityAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinnerdropdownview,
            R.id.spinnerDropDownTextview,
            ArrayList()
        )
        ac_search_entity?.setAdapter(entityAdapter)

        if (unSelectedClientList.isEmpty()) {
            entityAdapter?.clear()
            ac_search_entity?.dismissDropDown()
            return
        }

        ac_search_entity?.setText("")

        entityAdapter?.clear()
        entityAdapter?.addAll(unSelectedClientList)
        entityAdapter?.notifyDataSetChanged()

        ac_search_entity?.postDelayed({
            if ((entityAdapter?.count ?: 0) > 0) {
                ac_search_entity?.showDropDown()
            }
        }, 50)

        ac_search_entity?.setOnItemClickListener { parent, view, position, id ->
            val selectedClientName = entityAdapter?.getItem(position)
            ac_search_entity?.setText("")

            unSelectedClientList.clear()
            entityAdapter?.clear()
            entityAdapter?.notifyDataSetChanged()
            ac_search_entity?.setAdapter(null)
            entityAdapter = null
            ac_search_entity?.dismissDropDown()

            var selectedClient: ClientModel? = null
            for (model in clientList) {
                if (model.client_name == selectedClientName) {
                    selectedClient = model
                    break
                }
            }

            if (selectedClient == null) return@setOnItemClickListener

            if (selectedClient.send_request == true) {
                callClientRequests(selectedClient)
            } else {
                val clientsModel = ClientsModel()
                clientsModel.client_id = selectedClient.client_id
                clientsModel.client_name = selectedClient.client_name
                clientsModel.client_type = selectedClient.client_type

                if (clientsModel.client_type == "corporate") {
                    corp_client_id = clientsModel.client_id ?: ""
                }
                
                selected_corp_clients_list.clear()
                temporary_corpclients_list.clear()
                selected_corp_clients_list.add(clientsModel)
                temporary_corpclients_list.add(clientsModel)

                val valueArray = arrayOfNulls<String>(selected_corp_clients_list.size)
                for (i in selected_corp_clients_list.indices) {
                    valueArray[i] = selected_corp_clients_list[i].client_name
                }
                val str = valueArray.joinToString(",")
                at_add_corp_clients?.text = str

                AndroidUtils.ToggleButton(selected_corp_clients_list.size, btn_add_corp_clients)

                AddCorporate()
                loadClients()
                ac_search_entity?.setText("")
                tv_warning_msg?.visibility = View.GONE
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
                imm?.hideSoftInputFromWindow(ac_search_entity?.windowToken, 0)
                if (!Constants.create_matter && !isInitialLoad) {
                    isChangesOccured = true
                }
            }
        }
    }
    private fun callClientRequests(selectedClient: ClientModel) {
        progress_dialog = AndroidUtils.get_progress(activity)
        val postdata = JSONObject()
        try {
            if (selectedClient.client_type == "corporate") {
                postdata.put("entityId", selectedClient.client_id)
                postdata.put("description", "Description")
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "v3/corporate",
                    "Send Entity Request",
                    postdata.toString()
                )
            } else if (selectedClient.client_type == "entity") {
                postdata.put("entityId", selectedClient.client_id)
                postdata.put("description", "Description")
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "v2/relationship/request/entity",
                    "Send Entity Request",
                    postdata.toString()
                )
            } else {
                postdata.put("consumerId", selectedClient.client_id)
                postdata.put("description", "Description")
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "v2/relationship/request/consumer",
                    "Send Entity Request",
                    postdata.toString()
                )
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun isClientSelected(clientId: String?): Boolean {
        for (selectedClient in selected_clients_list) {
            if (selectedClient.client_id == clientId) {
                return true
            }
        }
        return false
    }

    private fun hasCorporateSelected(clientsModels: List<ClientsModel>): Boolean {
        for (client in clientsModels) {
            if (client.client_type == "corporate") {
                return true
            }
        }
        return false
    }

    private fun hasOtherClientsSelected(clientsModels: List<ClientsModel>): Boolean {
        for (client in clientsModels) {
            if (client.client_type != "corporate") {
                return true
            }
        }
        return false
    }

    private fun ToggleClient() {
        if (iscorpclient) {
            selected_corp_clients?.visibility = View.VISIBLE
            selected_temp_clients?.visibility = View.GONE
            selected_clients?.visibility = View.GONE
            ll_selected_corp_clients?.visibility = View.VISIBLE
            ll_selected_temp_clients?.visibility = View.GONE
            ll_selected_clients?.visibility = View.GONE
        } else if (isclient) {
            selected_corp_clients?.visibility = View.GONE
            selected_temp_clients?.visibility = View.GONE
            selected_clients?.visibility = View.VISIBLE
            ll_selected_corp_clients?.visibility = View.GONE
            ll_selected_temp_clients?.visibility = View.GONE
            ll_selected_clients?.visibility = View.VISIBLE
        } else {
            selected_corp_clients?.visibility = View.GONE
            selected_temp_clients?.visibility = View.VISIBLE
            selected_clients?.visibility = View.GONE
            ll_selected_corp_clients?.visibility = View.GONE
            ll_selected_temp_clients?.visibility = View.VISIBLE
            ll_selected_clients?.visibility = View.GONE
        }
    }

    private fun loadAddClient() {
        temp_client_layout?.visibility = View.GONE
        clients_list_layout?.visibility = View.VISIBLE
        cv_details?.visibility = View.VISIBLE
        ll_search_entity?.visibility = View.GONE
        tv_add_client?.setTextColor(resources.getColor(R.color.white))
        tv_corp_client?.setTextColor(resources.getColor(R.color.black))
        tv_temp_client?.setTextColor(resources.getColor(R.color.black))
        tv_corp_client?.background = context?.getDrawable(R.drawable.button_right_round_background)
        tv_add_client?.background = context?.getDrawable(R.drawable.button_left_green_round_background)
        tv_temp_client?.background = context?.getDrawable(R.drawable.radiobutton_centre_background)
        iscorpclient = false
        isclient = true
        istempclient = false
        ToggleClient()
    }

    private fun loadselectedTempClients() {
        temp_client_layout?.visibility = View.VISIBLE
        clients_list_layout?.visibility = View.GONE
        cv_details?.visibility = View.VISIBLE
        ll_search_entity?.visibility = View.GONE
        tv_add_client?.setTextColor(resources.getColor(R.color.black))
        tv_temp_client?.setTextColor(resources.getColor(R.color.white))
        tv_corp_client?.setTextColor(resources.getColor(R.color.black))
        tv_temp_client?.background = context?.getDrawable(R.drawable.radiobutton_centre_green_background)
        tv_add_client?.background = context?.getDrawable(R.drawable.button_left_round_background)
        tv_corp_client?.background = context?.getDrawable(R.drawable.button_right_round_background)
        iscorpclient = false
        isclient = false
        istempclient = true
        ToggleClient()
    }

    private fun callClientsWebservice() {
        try {
            val postdata = JSONObject()
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v3/clients", "Clients", postdata.toString())
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun Alert(isEntity: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Your Data will be lost.")
            .setTitle("Confirm")
        builder.setPositiveButton("OK") { dialog, _ ->
            et_temp_fname?.text = null
            et_temp_lname?.text = null
            et_temp_country?.text = ""
            et_temp_phone?.text = null
            et_temp_email?.text = null
            et_temp_confirm_email?.text = null
            
            if (isEntity) {
                tv_temp_individual?.setTextColor(resources.getColor(R.color.black))
                tv_temp_entity?.setTextColor(resources.getColor(R.color.white))
                client_type = "entity"
                tv_temp_individual?.background = context?.getDrawable(R.drawable.button_left_round_background)
                tv_temp_entity?.background = context?.getDrawable(R.drawable.button_right_green_round_background)
                tv_temp_lname?.setText(R.string.contact_person)
                et_temp_lname?.setHint(R.string.contact_person)
                tv_temp_fname?.setText(R.string.firm_name)
                et_temp_fname?.setHint(R.string.firm_name)
            } else {
                tv_temp_individual?.setTextColor(resources.getColor(R.color.white))
                tv_temp_entity?.setTextColor(resources.getColor(R.color.black))
                client_type = "consumer"
                tv_temp_individual?.background = context?.getDrawable(R.drawable.button_left_green_round_background)
                tv_temp_entity?.background = context?.getDrawable(R.drawable.button_right_round_background)
                tv_temp_lname?.setText(R.string.last_name)
                et_temp_lname?.setHint(R.string.last_name)
                tv_temp_fname?.setText(R.string.first_name)
                et_temp_fname?.setHint(R.string.first_name)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }



    private fun AddGctDetails() {
        try {
            if (Constants.create_matter) {
                if (selected_groups_list.isEmpty() && selected_clients_list.isEmpty() && selected_temp_clients_list.isEmpty() && selected_corp_clients_list.isEmpty() && selected_tm_list.isEmpty()) {
                    AndroidUtils.ToggleButton(0, btn_create)
                } else if (selected_groups_list.isEmpty()) {
                    AndroidUtils.ToggleButton(0, btn_create)
                } else if (selected_clients_list.isEmpty() && selected_temp_clients_list.isEmpty() && selected_corp_clients_list.isEmpty()) {
                    AndroidUtils.ToggleButton(0, btn_create)
                } else {
                    var clientHasGroup = true
                    for (client in Constants.allClientGroups) {
                        var foundGroup = false
                        for (groupId in client.groups ?: emptyList()) {
                            for (selectedGroup in selected_groups_list) {
                                if (selectedGroup.group_id == groupId) {
                                    foundGroup = true
                                    break
                                }
                            }
                            if (foundGroup) break
                        }
                        if (!foundGroup && selected_clients_list.any { it.client_id == client.id }) {
                            clientHasGroup = false
                            break
                        }
                    }

                    if (clientHasGroup) {
                        AndroidUtils.ToggleButton(1, btn_create)
                    } else {
                        AndroidUtils.ToggleButton(0, btn_create)
                    }
                }
            } else {
                AndroidUtils.ToggleButton(if (isChangesOccured) 1 else 0, btn_create)
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun TeamPopUp() {
        try {
            for (i in tmList.indices) {
                tmList[i].isChecked = false
            }

            for (i in tmList.indices) {
                for (j in temporary_tm_list.indices) {
                    if (tmList[i].tm_id == temporary_tm_list[j].tm_id) {
                        tmList[i].isChecked = true
                    }
                }
            }
            
            if (!Constants.create_matter) {
                val filteredTmList = ArrayList<TeamModel>()
                for (tm in tmList) {
                    var isInOwner = false
                    for (ownerTm in owner_tm) {
                        if (tm.tm_id == ownerTm.tm_id) {
                            isInOwner = true
                            break
                        }
                    }
                    if (!isInOwner) {
                        filteredTmList.add(tm)
                    }
                }
                tmList.clear()
                tmList.addAll(filteredTmList)
            }

            val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            rv_display_upload_tm_docs?.layoutManager = layoutManager
            ADAPTER_TAG = "Team"
            val documentsAdapter = GroupsAdapter(groupsList, clientsList, tmList, new_groupsList, ADAPTER_TAG, this)
            rv_display_upload_tm_docs?.adapter = documentsAdapter
            teamsAdapter = documentsAdapter
            AndroidUtils.LoadList(rv_display_upload_tm_docs, context, tmList.size, false)
            btn_add_teammembers?.setOnClickListener {
                selected_tm_list.clear()
                selected_tm_list.addAll(temporary_tm_list)
                Add_Teams()
                ischecked_tm = true
                rv_display_upload_tm_docs?.visibility = View.GONE
                updateDisplay()
                loadSelectedTM(arrayOfNulls(selected_tm_list.size))
                if (!Constants.create_matter && !isInitialLoad) {
                    isChangesOccured = true
                }
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    fun Add_Teams() {
        loadTeamText()
    }

    fun loadTeam() {
        loadSelectedTM(arrayOfNulls(selected_tm_list.size))
        ll_save_buttons?.visibility = View.VISIBLE
        AddGctDetails()
    }

    private fun callTMWebservice() {
        try {
            val postdata = JSONObject()
            progress_dialog = AndroidUtils.get_progress(activity)
            if (Constants.ROLE == "GH") {
                WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v3/members?module=matter", "Get Members", postdata.toString())
            } else {
                WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v3/members", "Get Members", postdata.toString())
            }
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog?.isShowing == true)
                AndroidUtils.dismiss_dialog(progress_dialog)
        }
    }

    private fun callGroupsWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(activity)
            val postdata = JSONObject()
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
            postdata.put("attachment_type", "groups")
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.PUT, "matter/attachments", "Groups", postdata.toString())
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
    }
    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progress_dialog != null && progress_dialog?.isShowing == true)
            AndroidUtils.dismiss_dialog(progress_dialog)
        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent)
                when (httpResult.requestType) {
                    "Groups" -> {
                        is_error = result.getBoolean("error")
                        if (!is_error) {
                            val groups = result.getJSONArray("groups")
                            val clientGroups = result.optJSONArray("client_groups")
                            loadGroupsData(groups, clientGroups)
                            if (groupsList.isEmpty())
                                ll_add_groups?.visibility = View.GONE
                        } else {
                            AndroidUtils.showAlert(result.optString("msg"), activity)
                            if (groupsList.isEmpty())
                                ll_add_groups?.visibility = View.GONE
                        }
                    }
                    "Send Entity Request" -> {
                        Log.d("Client_Msg", result.optString("msg"))
                    }
                    "Search Entity" -> {
                        is_error = result.getBoolean("error")
                        clientList.clear()
                        tv_warning_msg?.visibility = View.GONE
                        if (!is_error) {
                            val entity = result.getJSONArray("clients")
                            Log.i("Tag", "Info:$entity")
                            if (entity.length() == 0) {
                                tv_warning_msg?.visibility = View.VISIBLE
                                val negative_msg = "${ac_search_entity?.text} - not found. Please fill in the details below to send relationship invite."
                                tv_warning_msg?.text = negative_msg
                                AddTemp()
                            } else {
                                ac_search_entity?.setText("")
                                loadEntityData(entity)
                            }
                        } else {
                            ac_search_entity?.setText("")
                            AndroidUtils.showAlert(result.optString("msg"), activity)
                        }
                    }
                    "chosen_member" -> {
                        val matter = result.optJSONObject("matter")
                        if (matter != null) {
                            val members = matter.optJSONArray("members") ?: JSONArray()
                            val clients = matter.optJSONArray("clients") ?: JSONArray()
                            val corp_clients = matter.optJSONArray("corporate") ?: JSONArray()
                            display_existing_members(members, clients, corp_clients)
                        }
                    }
                    "attachment_members" -> {
                        is_error = result.getBoolean("error")
                        attachment_type = result.getString("attachment_type")
                        val members = result.getJSONArray("members")
                        loadMembers(members)
                        load_existing_clients_list()
                    }
                    "attachment_corp_clients" -> {
                        is_error = result.getBoolean("error")
                        val corporate = result.getJSONArray("relationships")
                        load_Corp_clients(corporate)
                    }
                    "countries" -> {
                        is_error = result.getBoolean("error")
                        val jsonArray = JSONObject(result.getString("data")).getJSONArray("countries")
                        countriesList.clear()
                        for (i in 1 until jsonArray.length()) {
                            val countriesDO = CountriesDO()
                            countriesDO.name = jsonArray.getJSONArray(i).get(1).toString()
                            countriesDO.value = jsonArray.getJSONArray(i).get(0).toString()
                            countriesList.add(countriesDO)
                        }
                        load_countries()
                    }
                    "temp_client" -> {
                        if (result.has("errors")) {
                            val errors = result.getJSONArray("errors")
                            var error_msg = ""
                            for (i in 0 until errors.length()) {
                                val error = errors.getJSONObject(i)
                                error_msg = error.getString("msg")
                            }
                            AndroidUtils.showAlert(error_msg, activity)
                        } else if (result.optBoolean("error")) {
                            val error_msg = result.optString("msg")
                            AndroidUtils.showAlert(error_msg, activity)
                        } else {
                            ac_search_entity?.setText("")
                            if (client_type == "entity") {
                                tv_warning_msg?.visibility = View.GONE
                                tv_warning_msg?.text = ""
                                success_msg = result.getString("msg")
                                temp_client_id = result.getString("profId")
                                temp_rel_id = result.optString("createdId")
                                temp_name = result.getString("name")
                            } else {
                                tv_warning_msg?.visibility = View.GONE
                                tv_warning_msg?.text = ""
                                success_msg = result.getString("msg")
                                temp_client_id = result.getString("conId")
                                temp_rel_id = result.optString("rel_id")
                                temp_name = result.getString("name")
                            }
                            AndroidUtils.showAlert(result.optString("msg"), activity)
                            val clientsModel = ClientsModel()
                            clientsModel.client_id = temp_client_id
                            clientsModel.rel_id = temp_rel_id
                            clientsModel.client_name = temp_name
                            clientsModel.client_type = client_type
                            selected_temp_clients_list.add(clientsModel)
                            Constants.selected_temp_clients_list.add(clientsModel)
                            addClientIfNotPresent(clientsModel)
                        }
                    }
                    "attachment_clients" -> {
                        is_error = result.getBoolean("error")
                        if (is_error) {
                            if (clientsList.isEmpty()) {
                                callClientsWebservice()
                            }
                        } else {
                            attachment_type = result.getString("attachment_type")
                            val members = result.getJSONArray("clients")
                            loadClientsList(members)
                        }
                    }
                    "matter_update" -> {
                        is_error = result.getBoolean("error")
                        val msg = result.getString("msg")
                        if (!is_error) {
                            if (isCancelClicked) {
                                matter?.loadViewUI()
                                AndroidUtils.showAlert(msg, activity, "Success")
                            } else {
                                AndroidUtils.showToast(msg, activity)
                                matter?.loadDocuments()
                            }
                        } else {
                            AndroidUtils.showAlert(msg, activity)
                        }
                        callClientCheckRequests()
                    }
                    "Clients" -> {
                        val data = result.getJSONObject("data")
                        val jsonArray = data.getJSONArray("relationships")
                        loadClientsList(jsonArray)
                    }
                    "Members" -> {
                        val members = result.getJSONArray("members")
                        loadMembers(members)
                    }
                    "Get Members" -> {
                        val data = result.getJSONObject("data")
                        val users = data.getJSONArray("users")
                        loadMembersList(users)
                    }
                }
            } catch (e: JSONException) {
                e.fillInStackTrace()
            }
        }
    }

    private fun load_Corp_clients(corp_clients: JSONArray) {
        try {
            corp_clients_list.clear()
            for (i in 0 until corp_clients.length()) {
                val jsonObject = corp_clients.getJSONObject(i)
                val clientsModel = ClientsModel()
                clientsModel.client_id = jsonObject.getString("id")
                clientsModel.client_name = jsonObject.getString("name")
                clientsModel.client_type = jsonObject.getString("type")
                corp_clients_list.add(clientsModel)
            }
            if (corp_clients_list.isEmpty()) {
                sp_corp_client?.visibility = View.GONE
            } else {
                sp_corp_client?.visibility = View.VISIBLE
            }

            selcted_corp_Clients = BooleanArray(corp_clients_list.size)
            corp_clients_popup()
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun loadMembersList(users: JSONArray) {
        try {
            tmList.clear()

            for (i in 0 until users.length()) {
                val jsonObject = users.getJSONObject(i)
                val groupsArray = jsonObject.optJSONArray("groups")
                var skipThisUser = false

                if (groupsArray != null) {
                    for (g in 0 until groupsArray.length()) {
                        val groupObj = groupsArray.getJSONObject(g)
                        val groupName = groupObj.optString("name", "").lowercase(Locale.ROOT)

                        if (groupName.contains("aam") || groupName.contains("superuser")) {
                            skipThisUser = true
                            break
                        }
                    }
                }

                if (skipThisUser) continue

                val teamModel = TeamModel()
                teamModel.tm_id = jsonObject.getString("id")
                teamModel.tm_name = jsonObject.getString("name")
                teamModel.groups = groupsArray

                if (!Constants.create_matter) {
                    if (jsonObject.getString("id") != Constants.owner_id &&
                        jsonObject.getString("id") != Constants.USER_ID) {
                        tmList.add(teamModel)
                    }
                } else {
                    if (jsonObject.getString("id") != Constants.USER_ID) {
                        tmList.add(teamModel)
                    }
                }
            }

            if (tmList.isEmpty()) {
                ll_assign_team_members?.visibility = View.GONE
                rv_display_upload_tm_docs?.visibility = View.GONE
            } else {
                if (Constants.CATEGORY != "solo") {
                    ll_assign_team_members?.visibility = View.VISIBLE
                }
            }

            if (!Constants.create_matter) {
                rv_display_upload_tm_docs?.visibility = View.GONE
            }

            if (tmList.isNotEmpty()) {
                selectedTM = BooleanArray(tmList.size)

                for (i in selected_tm_list.indices.reversed()) {
                    val selectedGroup = selected_tm_list[i]
                    var existsInTms = false

                    for (j in tmList.indices) {
                        if (selectedGroup.tm_id == tmList[j].tm_id) {
                            tmList[j].isChecked = true
                            existsInTms = true
                            break
                        }
                    }

                    if (!existsInTms) {
                        selected_tm_list.removeAt(i)
                        temporary_tm_list.removeAt(i)
                    }
                }
            }

            loadSelectedTM(arrayOfNulls(selected_tm_list.size))
            loadTeamText()
            TeamPopUp()

            if (!Constants.create_matter) {
                owner_tm.clear()
                val teamModel1 = TeamModel()
                teamModel1.tm_id = Constants.owner_id
                teamModel1.tm_name = Constants.owner_name
                owner_tm.add(teamModel1)

                var ownerExists = false
                for (tm in selected_tm_list) {
                    if (tm.tm_id == Constants.owner_id) {
                        ownerExists = true
                        break
                    }
                }
                if (!ownerExists) {
                    temporary_tm_list.add(0, owner_tm[0])
                    selected_tm_list.add(0, owner_tm[0])
                }

                Add_Teams()
                loadTeam()
                if (isInitialLoad) {
                    isChangesOccured = false
                }
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun loadMembers(members: JSONArray) {
        try {
            tmList.clear()

            for (i in 0 until members.length()) {
                val jsonObject = members.getJSONObject(i)
                val teamModel = TeamModel()
                teamModel.tm_id = jsonObject.getString("id")
                teamModel.tm_name = jsonObject.getString("name")
                teamModel.user_id = jsonObject.getString("user_id")

                if (!Constants.create_matter) {
                    if (jsonObject.getString("id") != Constants.owner_id && jsonObject.getString("id") != Constants.USER_ID) {
                        tmList.add(teamModel)
                    }
                } else {
                    if (jsonObject.getString("id") != Constants.USER_ID) {
                        tmList.add(teamModel)
                    }
                }
            }

            if (tmList.isEmpty()) {
                ll_assign_team_members?.visibility = View.GONE
                rv_display_upload_tm_docs?.visibility = View.GONE
            } else {
                if (Constants.CATEGORY != "solo") {
                    ll_assign_team_members?.visibility = View.VISIBLE
                }
            }

            if (!Constants.create_matter) {
                rv_display_upload_tm_docs?.visibility = View.GONE
            }

            if (tmList.isNotEmpty()) {
                selectedTM = BooleanArray(tmList.size)

                for (i in selected_tm_list.indices.reversed()) {
                    val selectedGroup = selected_tm_list[i]
                    var existsInTms = false

                    for (j in tmList.indices) {
                        if (selectedGroup.tm_id == tmList[j].tm_id) {
                            tmList[j].isChecked = true
                            existsInTms = true
                            break
                        }
                    }

                    if (!existsInTms) {
                        selected_tm_list.removeAt(i)
                        temporary_tm_list.removeAt(i)
                    }
                }
            }

            loadSelectedTM(arrayOfNulls(selected_tm_list.size))
            loadTeamText()
            TeamPopUp()

            if (!Constants.create_matter) {
                owner_tm.clear()
                val teamModel1 = TeamModel()
                teamModel1.tm_id = Constants.owner_id
                teamModel1.tm_name = Constants.owner_name
                owner_tm.add(teamModel1)

                var ownerExists = false
                for (tm in selected_tm_list) {
                    if (tm.tm_id == Constants.owner_id) {
                        ownerExists = true
                        break
                    }
                }
                if (!ownerExists) {
                    temporary_tm_list.add(0, owner_tm[0])
                    selected_tm_list.add(0, owner_tm[0])
                }

                Add_Teams()
                loadTeam()
                if (isInitialLoad) {
                    isChangesOccured = false
                }
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun loadClientsList(clients: JSONArray) {
        try {
            clientsList.clear()
            for (i in 0 until clients.length()) {
                val jsonObject = clients.getJSONObject(i)
                val clientsModel = ClientsModel()
                clientsModel.client_id = jsonObject.getString("id")
                clientsModel.client_name = jsonObject.getString("name")
                clientsModel.client_type = jsonObject.getString("type")
                clientsList.add(clientsModel)
            }
            if (clientsList.isEmpty()) {
                rv_display_upload_client_docs?.visibility = View.GONE
            } else {
                rv_display_upload_client_docs?.visibility = View.VISIBLE
            }

            selectedClients = BooleanArray(clientsList.size)

            if (!Constants.create_matter) {
                rv_display_upload_client_docs?.visibility = View.GONE
            }
            ClientssPopUp()
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }
    private fun corp_clients_popup() {
        ll_add_clients?.visibility = View.GONE
        try {
            corp_clients_name.clear()
            for (i in corp_clients_list.indices) {
                val clientsModel = corp_clients_list[i]
                corp_clients_name.add(clientsModel.client_name ?: "")
            }
            val adapter = CommonSpinnerAdapter(activity, corp_clients_name)
            sp_corp_client?.adapter = adapter
            AndroidUtils.LoadList(sp_corp_client, context, corp_clients_name.size, true)
            btn_add_corp_clients?.setOnClickListener {
                selected_corp_clients_list.clear()
                selected_corp_clients_list.addAll(temporary_corpclients_list)
                groupsList.clear()
                tmList.clear()
                at_add_groups?.text = ""
                at_assigned_team_members?.text = ""
                selected_tm?.visibility = View.GONE
                selected_groups?.visibility = View.GONE
                loadselected_corp_list()
                AddGctDetails()
                documentsList.clear()
                loadClients()
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    private fun loadselected_corp_list() {
        rv_display_upload_corp_client_docs?.visibility = View.GONE
        if (selected_corp_clients_list.isEmpty()) {
            selected_corp_clients?.visibility = View.GONE
            ll_selected_corp_clients?.visibility = View.GONE
        } else {
            ll_selected_corp_clients?.visibility = View.VISIBLE
            loadSelectedCorp_Clients()
        }
        sp_corp_client?.visibility = View.GONE
        ischecked_corp_client = true
    }

    private fun ClientssPopUp() {
        try {
            for (i in clientsList.indices) {
                val teamModel = clientsList[i]
                teamModel.isChecked = false
            }

            for (i in clientsList.indices) {
                for (j in temporary_clients_list.indices) {
                    if (clientsList[i].client_id == temporary_clients_list[j].client_id) {
                        val teamModel = clientsList[i]
                        teamModel.isChecked = true
                    }
                }
            }

            val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            rv_display_upload_client_docs?.layoutManager = layoutManager
            ADAPTER_TAG = "Clients"
            var documentsAdapter: GroupsAdapter? = null

            ll_add_clients?.visibility = View.GONE
            documentsAdapter = GroupsAdapter(groupsList, clientsList, tmList, new_groupsList, ADAPTER_TAG, this)

            rv_display_upload_client_docs?.adapter = documentsAdapter
            clientsAdapter = documentsAdapter
            AndroidUtils.LoadList(rv_display_upload_client_docs, context, clientsAdapter?.itemCount ?: 0, false)
        } catch (e: Exception) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    fun loadClientsText() {
        val value = arrayOfNulls<String>(temporary_clients_list.size)
        for (i in temporary_clients_list.indices) {
            value[i] = temporary_clients_list[i].client_name
        }

        val str = value.joinToString(",")
        at_add_clients?.text = str
        AndroidUtils.ToggleButton(temporary_clients_list.size, btn_add_clients)
    }

    fun loadClients() {
        if (Constants.create_matter) {
            if (selected_temp_clients_list.isNotEmpty() || selected_clients_list.isNotEmpty() || selected_corp_clients_list.isNotEmpty()) {
                AndroidUtils.ToggleButton(temporary_groups_list.size, btn_add_groups)
                ischecked_group = true
                rv_display_upload_groups_docs?.visibility = View.GONE
                if (selected_clients_list.isNotEmpty()) {
                    loadSelectedClients(arrayOfNulls(selected_clients_list.size))
                    ll_selected_clients?.visibility = View.VISIBLE
                }
                AddGctDetails()
            } else {
                at_add_clients?.text = ""
                at_add_groups?.text = ""
                at_add_corp_clients?.text = ""
                selected_clients?.visibility = View.GONE
                ll_selected_clients?.visibility = View.GONE
                ll_selected_groups?.removeAllViews()
                rv_display_upload_groups_docs?.visibility = View.GONE
                if (Constants.CATEGORY != "solo") {
                    ll_assign_team_members?.visibility = View.VISIBLE
                }
                ll_add_groups?.visibility = View.GONE
                selected_groups_list.clear()
                temporary_tm_list.clear()
                temporary_groups_list.clear()
                selected_tm_list.clear()
                loadSelectedGroups(arrayOfNulls(selected_groups_list.size))
            }
            if (selected_clients_list.isEmpty() && selected_corp_clients_list.isEmpty() && selected_temp_clients_list.isEmpty()) {
                selected_tm?.visibility = View.GONE
                ll_save_buttons?.visibility = View.VISIBLE
            }
            AddGctDetails()
        } else {
            ll_add_clients?.visibility = View.GONE
            if (tmList.isEmpty()) {
                ll_assign_team_members?.visibility = View.GONE
            } else {
                if (Constants.CATEGORY != "solo") {
                    ll_assign_team_members?.visibility = View.VISIBLE
                }
            }
            loadSelectedClients(arrayOfNulls(selected_clients_list.size))
            ll_save_buttons?.visibility = View.VISIBLE
        }
        ToggleClient()
    }

    fun Add_Clients() {
        if (!Constants.create_matter && !isInitialLoad) {
            isChangesOccured = true
        }
        loadClientsText()
    }

    fun loadTeamText() {
        val value = arrayOfNulls<String>(temporary_tm_list.size)
        for (i in temporary_tm_list.indices) {
            value[i] = temporary_tm_list[i].tm_name
        }

        val str = value.joinToString(",")
        at_assigned_team_members?.text = str
        AddGctDetails()
        AndroidUtils.ToggleButton(temporary_tm_list.size, btn_add_teammembers)
    }

    private fun loadSelectedTM(value: Array<String?>) {
        if (Constants.create_matter && selected_groups_list.isNotEmpty()) {
            ll_save_buttons?.visibility = View.VISIBLE
        }

        ll_assign_team_members?.visibility = if (tmList.isEmpty()) View.GONE else View.VISIBLE
        if (Constants.CATEGORY != "solo") {
            selected_tm?.visibility = if (selected_tm_list.isEmpty()) View.GONE else View.VISIBLE
        }
        ll_assigned_team_members?.removeAllViews()

        for (i in selected_tm_list.indices) {
            val view_opponents = LayoutInflater.from(context).inflate(R.layout.edit_opponent_advocate, null)
            if (view_opponents == null) continue

            val tv_opponent_name = view_opponents.findViewById<TextView>(R.id.tv_opponent_name)
            val iv_remove_opponent = view_opponents.findViewById<ImageView>(R.id.iv_remove_opponent)
            val iv_edit_opponent = view_opponents.findViewById<ImageView>(R.id.iv_edit_opponent)

            iv_edit_opponent?.visibility = View.GONE

            if (tv_opponent_name != null && iv_remove_opponent != null) {
                val currentModel = selected_tm_list[i]
                tv_opponent_name.text = currentModel.tm_name

                val isOwner = currentModel.tm_id == Constants.owner_id
                val isCurrentUser = currentModel.tm_id == Constants.USER_ID

                if (!Constants.create_matter && (isOwner || isCurrentUser)) {
                    iv_remove_opponent.visibility = View.INVISIBLE
                } else {
                    iv_remove_opponent.visibility = View.VISIBLE
                    iv_remove_opponent.tag = i

                    iv_remove_opponent.setOnClickListener { v ->
                        try {
                            val position = v.tag as Int

                            val teamModel = selected_tm_list.removeAt(position)
                            teamModel.isChecked = false

                            for (k in temporary_tm_list.indices) {
                                if (temporary_tm_list[k].tm_id == teamModel.tm_id) {
                                    temporary_tm_list.removeAt(k)
                                    break
                                }
                            }

                            ll_assigned_team_members?.removeViewAt(position)

                            for (j in 0 until (ll_assigned_team_members?.childCount ?: 0)) {
                                val iv_remove = ll_assigned_team_members?.getChildAt(j)?.findViewById<ImageView>(R.id.iv_remove_opponent)
                                iv_remove?.tag = j
                            }

                            val stringBuilder = java.lang.StringBuilder()
                            for (model in selected_tm_list) {
                                stringBuilder.append(model.tm_name).append(",")
                            }
                            if (stringBuilder.isNotEmpty()) {
                                stringBuilder.deleteCharAt(stringBuilder.length - 1)
                            }
                            at_assigned_team_members?.text = stringBuilder.toString()
                            if (Constants.CATEGORY != "solo") {
                                selected_tm?.visibility = if (selected_tm_list.isEmpty()) View.GONE else View.VISIBLE
                            }

                            AddGctDetails()
                            AndroidUtils.ToggleButton(temporary_tm_list.size, btn_add_teammembers)

                        } catch (e: Exception) {
                            e.fillInStackTrace()
                            AndroidUtils.showAlert(e.message, activity)
                        }
                        isChangesOccured = true
                    }
                }
            }

            ll_assigned_team_members?.addView(view_opponents)
        }
    }

    private fun loadSelectedTemp_Clients() {
        if (istempclient)
            selected_temp_clients?.visibility = View.VISIBLE
        ll_selected_temp_clients?.removeAllViews()

        for (i in selected_temp_clients_list.indices) {
            val view_opponents = LayoutInflater.from(context).inflate(R.layout.edit_opponent_advocate, null)
            if (view_opponents != null) {
                val tv_opponent_name = view_opponents.findViewById<TextView>(R.id.tv_opponent_name)
                val iv_remove_opponent = view_opponents.findViewById<ImageView>(R.id.iv_remove_opponent)
                val iv_edit_opponent = view_opponents.findViewById<ImageView>(R.id.iv_edit_opponent)
                iv_edit_opponent?.visibility = View.GONE
                if (tv_opponent_name != null && iv_remove_opponent != null) {
                    tv_opponent_name.text = selected_temp_clients_list[i].client_name
                    iv_remove_opponent.tag = i
                    iv_remove_opponent.setOnClickListener { v ->
                        try {
                            val position = v.tag as Int
                            ll_selected_temp_clients?.removeViewAt(position)
                            val clientsModel = selected_temp_clients_list.removeAt(position)
                            clientsModel.isChecked = false
                            for (j in 0 until (ll_selected_temp_clients?.childCount ?: 0)) {
                                val iv_remove = ll_selected_temp_clients?.getChildAt(j)?.findViewById<ImageView>(R.id.iv_remove_opponent)
                                iv_remove?.tag = j
                            }
                            if (selected_temp_clients_list.isEmpty()) {
                                selected_temp_clients?.visibility = View.GONE
                            } else {
                                selected_temp_clients?.visibility = View.VISIBLE
                            }
                            loadClients()
                            documentsList.clear()
                            AndroidUtils.ToggleButton(selected_temp_clients_list.size, btn_add_temp_client)
                        } catch (e: Exception) {
                            e.fillInStackTrace()
                            AndroidUtils.showAlert(e.message, activity)
                        }
                    }
                    iv_remove_opponent.visibility = View.VISIBLE
                }
                ll_selected_temp_clients?.addView(view_opponents)
            }
        }
    }

    private fun loadSelectedCorp_Clients() {
        if (iscorpclient) {
            selected_corp_clients?.visibility = View.VISIBLE
        }
        ll_selected_corp_clients?.removeAllViews()
        val value = arrayOfNulls<String>(selected_corp_clients_list.size)
        for (i in selected_corp_clients_list.indices) {
            value[i] = selected_corp_clients_list[i].client_name
        }

        val str = value.joinToString(",")
        at_add_corp_clients?.text = str
        ll_add_clients?.visibility = View.GONE

        for (i in selected_corp_clients_list.indices) {
            val view_opponents = LayoutInflater.from(context).inflate(R.layout.edit_opponent_advocate, null)
            if (view_opponents != null) {
                val tv_opponent_name = view_opponents.findViewById<TextView>(R.id.tv_opponent_name)
                val iv_remove_opponent = view_opponents.findViewById<ImageView>(R.id.iv_remove_opponent)
                val iv_edit_opponent = view_opponents.findViewById<ImageView>(R.id.iv_edit_opponent)
                iv_edit_opponent?.visibility = View.GONE
                if (tv_opponent_name != null && iv_remove_opponent != null) {
                    tv_opponent_name.text = selected_corp_clients_list[i].client_name
                    iv_remove_opponent.tag = i
                    iv_remove_opponent.setOnClickListener { v ->
                        try {
                            val position = v.tag as Int
                            ll_selected_corp_clients?.removeViewAt(position)
                            val clientsModel = selected_corp_clients_list.removeAt(position)
                            clientsModel.isChecked = false
                            for (j in 0 until (ll_selected_corp_clients?.childCount ?: 0)) {
                                val iv_remove = ll_selected_corp_clients?.getChildAt(j)?.findViewById<ImageView>(R.id.iv_remove_opponent)
                                iv_remove?.tag = j
                            }

                            val stringBuilder = java.lang.StringBuilder()
                            for (model in selected_corp_clients_list) {
                                stringBuilder.append(model.client_name).append(",")
                            }

                            if (stringBuilder.isNotEmpty()) {
                                stringBuilder.deleteCharAt(stringBuilder.length - 1)
                            }
                            val strn = stringBuilder.toString()
                            at_add_corp_clients?.text = strn
                            if (selected_corp_clients_list.isEmpty()) {
                                selected_corp_clients?.visibility = View.GONE
                            } else {
                                selected_corp_clients?.visibility = View.VISIBLE
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
                    iv_remove_opponent.visibility = View.VISIBLE
                }
                ll_selected_corp_clients?.addView(view_opponents)
            }
        }
    }

    private fun loadSelectedClients(value: Array<String?>) {
        selected_clients?.visibility = View.VISIBLE
        if (selected_clients_list.isNotEmpty()) {
            ll_selected_clients?.visibility = View.VISIBLE
        }
        ll_selected_clients?.removeAllViews()
        ll_add_clients?.visibility = View.GONE

        if (!Constants.create_matter)
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

        for (i in selected_clients_list.indices) {
            val view_opponents = LayoutInflater.from(context).inflate(R.layout.edit_opponent_advocate, null)
            if (view_opponents != null) {
                val tv_opponent_name = view_opponents.findViewById<TextView>(R.id.tv_opponent_name)
                val iv_remove_opponent = view_opponents.findViewById<ImageView>(R.id.iv_remove_opponent)
                val iv_edit_opponent = view_opponents.findViewById<ImageView>(R.id.iv_edit_opponent)
                iv_edit_opponent?.visibility = View.GONE

                if (tv_opponent_name != null && iv_remove_opponent != null) {
                    tv_opponent_name.text = selected_clients_list[i].client_name
                    iv_remove_opponent.tag = i
                    iv_remove_opponent.setOnClickListener { v ->
                        try {
                            val position = v.tag as Int
                            ll_selected_clients?.removeViewAt(position)
                            val clientsModel = selected_clients_list.removeAt(position)
                            if (clientsModel.client_type == "corporate") {
                                corp_client_id = ""
                                temporary_corpclients_list.clear()
                                selected_corp_clients_list.clear()
                            }
                            temporary_clients_list.removeAt(position)
                            clientsModel.isChecked = false
                            for (j in 0 until (ll_selected_clients?.childCount ?: 0)) {
                                val iv_remove = ll_selected_clients?.getChildAt(j)?.findViewById<ImageView>(R.id.iv_remove_opponent)
                                iv_remove?.tag = j
                            }

                            val stringBuilder = java.lang.StringBuilder()
                            for (model in selected_clients_list) {
                                stringBuilder.append(model.client_name).append(",")
                            }

                            if (stringBuilder.isNotEmpty()) {
                                stringBuilder.deleteCharAt(stringBuilder.length - 1)
                            }
                            val strn = stringBuilder.toString()
                            at_add_clients?.text = strn

                            if (selected_clients_list.isNotEmpty()) {
                                selected_clients?.visibility = View.VISIBLE
                            } else {
                                selected_clients?.visibility = View.GONE
                            }

                            isChangesOccured = true
                            loadFilteredEntityData()
                        } catch (e: Exception) {
                            e.fillInStackTrace()
                            AndroidUtils.showAlert(e.message, activity)
                        }
                    }
                    iv_remove_opponent.visibility = View.VISIBLE
                }
                ll_selected_clients?.addView(view_opponents)
            }
        }
    }

    private fun updatedDisplay() {
        if (selected_clients_list.isNotEmpty()) {
            selected_clients?.visibility = View.VISIBLE
        } else {
            selected_clients?.visibility = View.GONE
        }
    }

    private fun load_countries() {
        CountryAdapter = CommonSpinnerAdapter(activity, countriesList)
        Log.i("ArrayList", "Info:$countriesList")
        sp_country?.adapter = CountryAdapter
    }

    private fun call_country_list() {
        val postdata = JSONObject()
        WebServiceHelper.callHttpWebService(
            this,
            requireContext(),
            WebServiceHelper.RestMethodType.GET,
            "countries",
            "countries",
            postdata.toString()
        )
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
                        if (client.groups?.contains(group.group_id) == true) {
                            clientsInGroup.add(client)
                        }
                    }
                    group.clientGroupModelList = clientsInGroup
                }
            }

            selected_groups_list.addAll(groupsList)
            temporary_groups_list.addAll(groupsList)

            selectedLanguage = BooleanArray(groupsList.size)
            selectedLanguage?.fill(true)

            loadSelectedGroups(arrayOfNulls(selected_groups_list.size))
            loadGroupsText()
            GroupsPopup()
            AddGctDetails()

            if (selected_groups_list.isEmpty()) {
                selected_tm_list.clear()
                tmList.clear()
                at_assigned_team_members?.text = ""
            }

        } catch (e: JSONException) {
            e.printStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    fun GroupsPopup() {
        try {
            for (i in groupsList.indices) {
                for (j in selected_groups_list.indices) {
                    if (groupsList[i].group_id == selected_groups_list[j].group_id) {
                        val groupsModel = groupsList[i]
                        groupsModel.isChecked = true
                    }
                }
            }

            val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            rv_display_upload_groups_docs?.layoutManager = layoutManager
            ADAPTER_TAG = "Groups"
            val documentsAdapter = GroupsAdapter(groupsList, clientsList, tmList, new_groupsList, ADAPTER_TAG, this)
            rv_display_upload_groups_docs?.adapter = documentsAdapter
            groupsAdapter = documentsAdapter
            AndroidUtils.LoadList(rv_display_upload_groups_docs, context, groupsList.size, false)
            btn_add_groups?.setOnClickListener {
                selected_groups_list.clear()
                selected_groups_list.addAll(temporary_groups_list)
                Add_Groups()
                ischecked_group = true
                rv_display_upload_groups_docs?.visibility = View.GONE
                updateDisplay()
                documentsList.clear()
                loadSelectedGroups(arrayOfNulls(selected_groups_list.size))
                if (!Constants.create_matter && !isInitialLoad) {
                    isChangesOccured = true
                }
            }

        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }
    fun Add_Groups() {
        loadGroupsText()
        if (Constants.create_matter) {
            if (selected_groups_list.isNotEmpty()) {
                callGroupsWebservice()
                callTMWebservice()
            } else {
                at_add_clients?.text = ""
                at_assigned_team_members?.text = ""
                at_add_corp_clients?.text = ""
                selected_tm?.visibility = View.GONE
                selected_clients?.visibility = View.GONE
                ll_selected_clients?.visibility = View.GONE
                ll_assigned_team_members?.removeAllViews()
                ll_selected_corp_clients?.visibility = View.GONE
                selected_corp_clients?.visibility = View.GONE
                selected_temp_clients?.visibility = View.GONE
                ll_selected_temp_clients?.visibility = View.GONE
                ll_assign_team_members?.visibility = View.GONE
                ll_add_clients?.visibility = View.GONE
                selected_clients_list.clear()
                selected_tm_list.clear()
                selected_corp_clients_list.clear()
                selected_temp_clients_list.clear()
                temporary_clients_list.clear()
                temporary_corpclients_list.clear()
                temporary_tm_list.clear()
                Constants.selected_temp_clients_list.clear()
            }
            ToggleClient()
        } else {
            if (selected_groups_list.isNotEmpty()) {
                callGroupsWebservice()
            }
        }
    }

    private fun ClientListChanges() {
        var isRemoved = false
        val groupIds = HashSet<String>()
        for (selectedGroup in selected_groups_list) {
            groupIds.add(selectedGroup.group_id ?: "")
        }

        for (i in selected_clients_list.indices.reversed()) {
            val selectedClient = selected_clients_list[i]
            if (selectedClient.client_type != "corporate") {
                var existsInGroups = false
                for (clientGroupModel in allClientGroups) {
                    if (clientGroupModel.id == selectedClient.client_id) {
                        for (groupId in clientGroupModel.groups ?: emptyList()) {
                            if (groupIds.contains(groupId)) {
                                existsInGroups = true
                                break
                            }
                        }
                    }
                    if (existsInGroups) break
                }
                if (!existsInGroups) {
                    selected_clients_list.removeAt(i)
                    for (k in temporary_clients_list.indices) {
                        if (temporary_clients_list[k].client_id == selectedClient.client_id) {
                            temporary_clients_list.removeAt(k)
                            break
                        }
                    }
                    isRemoved = true
                }
            }
        }
        
        if (isRemoved) {
            val stringBuilder = java.lang.StringBuilder()
            for (model in selected_clients_list) {
                stringBuilder.append(model.client_name).append(",")
            }
            if (stringBuilder.isNotEmpty()) {
                stringBuilder.deleteCharAt(stringBuilder.length - 1)
            }
            at_add_clients?.text = stringBuilder.toString()
            loadSelectedClients(arrayOfNulls(selected_clients_list.size))
            if (selected_clients_list.isNotEmpty()) {
                selected_clients?.visibility = View.VISIBLE
            } else {
                selected_clients?.visibility = View.GONE
            }
        }
    }

    private fun detectListChanges() {
        val selectedIds = HashSet<String>()
        for (groupsModel in selected_groups_list) {
            selectedIds.add(groupsModel.group_id ?: "")
        }
        
        if (old_clients_list.isEmpty()) {
            old_clients_list.addAll(selected_clients_list)
        } else {
            ClientListChanges()
        }
    }

    fun loadGroupsText() {
        val value = arrayOfNulls<String>(temporary_groups_list.size)
        for (i in temporary_groups_list.indices) {
            value[i] = temporary_groups_list[i].group_name
        }
        val str = value.joinToString(",")
        at_add_groups?.text = str
        if (!Constants.create_matter) {
            AddGctDetails()
        }
        AndroidUtils.ToggleButton(temporary_groups_list.size, btn_add_groups)
    }

    private fun loadSelectedGroups(value: Array<String?>) {
        if (Constants.create_matter) {
            if (selected_groups_list.isEmpty()) {
                ll_add_clients?.visibility = View.GONE
                ll_assign_team_members?.visibility = View.GONE
                selected_groups?.visibility = View.GONE
            } else {
                ll_add_clients?.visibility = View.VISIBLE
                selected_groups?.visibility = View.VISIBLE
            }
        }
        ll_selected_groups?.removeAllViews()

        for (i in selected_groups_list.indices) {
            val view_opponents = LayoutInflater.from(context).inflate(R.layout.edit_opponent_advocate, null)
            val tv_opponent_name = view_opponents.findViewById<TextView>(R.id.tv_opponent_name)
            val iv_remove_opponent = view_opponents.findViewById<ImageView>(R.id.iv_remove_opponent)
            val iv_edit_opponent = view_opponents.findViewById<ImageView>(R.id.iv_edit_opponent)
            iv_edit_opponent?.visibility = View.GONE

            if (tv_opponent_name != null && iv_remove_opponent != null) {
                val currentModel = selected_groups_list[i]
                tv_opponent_name.text = currentModel.group_name

                var cantRemove = false
                if (!Constants.create_matter && exisiting_group_acls != null) {
                    for (k in 0 until (exisiting_group_acls?.length() ?: 0)) {
                        val aclObj = exisiting_group_acls?.getJSONObject(k)
                        val aclId = aclObj?.optString("id")
                        if (currentModel.group_id == aclId) {
                            cantRemove = true
                            break
                        }
                    }
                }

                if (cantRemove) {
                    iv_remove_opponent.visibility = View.INVISIBLE
                } else {
                    iv_remove_opponent.tag = i
                    iv_remove_opponent.visibility = View.VISIBLE
                    iv_remove_opponent.setOnClickListener { v ->
                        try {
                            val position = v.tag as Int
                            val groupsModel = selected_groups_list[position]

                            var hasAssignedClients = false
                            for (client in selected_clients_list) {
                                if (client.client_type != "corporate") {
                                    for (clientGroup in allClientGroups) {
                                        if (clientGroup.id == client.client_id) {
                                            if (clientGroup.groups?.contains(groupsModel.group_id) == true) {
                                                hasAssignedClients = true
                                                break
                                            }
                                        }
                                    }
                                    if (hasAssignedClients) break
                                }
                            }

                            if (hasAssignedClients) {
                                checkingRemovalLogic(groupsModel, position)
                            } else {
                                ll_selected_groups?.removeViewAt(position)
                                selected_groups_list.removeAt(position)
                                groupsModel.isChecked = false
                                for (k in temporary_groups_list.indices) {
                                    if (temporary_groups_list[k].group_id == groupsModel.group_id) {
                                        temporary_groups_list.removeAt(k)
                                        break
                                    }
                                }
                                
                                for (j in 0 until (ll_selected_groups?.childCount ?: 0)) {
                                    val iv_remove = ll_selected_groups?.getChildAt(j)?.findViewById<ImageView>(R.id.iv_remove_opponent)
                                    iv_remove?.tag = j
                                }

                                val stringBuilder = java.lang.StringBuilder()
                                for (model in selected_groups_list) {
                                    stringBuilder.append(model.group_name).append(",")
                                }
                                if (stringBuilder.isNotEmpty()) {
                                    stringBuilder.deleteCharAt(stringBuilder.length - 1)
                                }
                                at_add_groups?.text = stringBuilder.toString()
                                
                                if (Constants.create_matter && selected_groups_list.isEmpty()) {
                                    at_add_clients?.text = ""
                                    at_add_corp_clients?.text = ""
                                    at_assigned_team_members?.text = ""
                                    selected_tm?.visibility = View.GONE
                                    selected_clients?.visibility = View.GONE
                                    ll_selected_clients?.visibility = View.GONE
                                    ll_assigned_team_members?.removeAllViews()
                                    ll_assign_team_members?.visibility = View.GONE
                                    ll_add_clients?.visibility = View.GONE
                                    selected_groups?.visibility = View.GONE
                                    selected_clients_list.clear()
                                    selected_tm_list.clear()
                                    selected_corp_clients_list.clear()
                                    selected_temp_clients_list.clear()
                                    temporary_clients_list.clear()
                                    temporary_corpclients_list.clear()
                                    temporary_tm_list.clear()
                                    Constants.selected_temp_clients_list.clear()
                                }
                                
                                callGroupsWebservice()
                                isChangesOccured = true
                                AddGctDetails()
                                AndroidUtils.ToggleButton(temporary_groups_list.size, btn_add_groups)
                            }
                        } catch (e: Exception) {
                            e.fillInStackTrace()
                            AndroidUtils.showAlert(e.message, requireActivity())
                        }
                    }
                }
            }
            ll_selected_groups?.addView(view_opponents)
        }
    }

    private fun checkingRemovalLogic(groupsModel: GroupsModel, position: Int) {
        val remainingGroups = ArrayList<GroupsModel>(selected_groups_list)
        remainingGroups.remove(groupsModel)
        
        val remainingGroupIds = HashSet<String>()
        for (g in remainingGroups) {
            remainingGroupIds.add(g.group_id ?: "")
        }

        val clientsToRemove = ArrayList<ClientsModel>()
        
        for (client in selected_clients_list) {
            if (client.client_type != "corporate") {
                var canBeSupported = false
                for (clientGroup in allClientGroups) {
                    if (clientGroup.id == client.client_id) {
                        for (gid in clientGroup.groups ?: emptyList()) {
                            if (remainingGroupIds.contains(gid)) {
                                canBeSupported = true
                                break
                            }
                        }
                    }
                    if (canBeSupported) break
                }
                
                if (!canBeSupported) {
                    clientsToRemove.add(client)
                }
            }
        }

        if (clientsToRemove.isNotEmpty()) {
            val names = ArrayList<String>()
            for (c in clientsToRemove) {
                names.add(c.client_name ?: "")
            }
            val msg = "Removing this group will also remove the following clients: " + names.joinToString(", ") + ". Do you want to proceed?"
            
            AndroidUtils.showConfirmation(requireActivity(), "Warning", msg, "Proceed", object : AndroidUtils.OnConfirmListener {
                override fun onSave() {
                    for (c in clientsToRemove) {
                        for (k in selected_clients_list.indices.reversed()) {
                            if (selected_clients_list[k].client_id == c.client_id) {
                                selected_clients_list.removeAt(k)
                            }
                        }
                        for (k in temporary_clients_list.indices.reversed()) {
                            if (temporary_clients_list[k].client_id == c.client_id) {
                                temporary_clients_list.removeAt(k)
                            }
                        }
                    }

                    ll_selected_groups?.removeViewAt(position)
                    selected_groups_list.remove(groupsModel)
                    groupsModel.isChecked = false
                    for (k in temporary_groups_list.indices) {
                        if (temporary_groups_list[k].group_id == groupsModel.group_id) {
                            temporary_groups_list.removeAt(k)
                            break
                        }
                    }
                    
                    for (j in 0 until (ll_selected_groups?.childCount ?: 0)) {
                        val iv_remove = ll_selected_groups?.getChildAt(j)?.findViewById<ImageView>(R.id.iv_remove_opponent)
                        iv_remove?.tag = j
                    }

                    val stringBuilder = java.lang.StringBuilder()
                    for (model in selected_groups_list) {
                        stringBuilder.append(model.group_name).append(",")
                    }
                    if (stringBuilder.isNotEmpty()) {
                        stringBuilder.deleteCharAt(stringBuilder.length - 1)
                    }
                    at_add_groups?.text = stringBuilder.toString()
                    
                    val cb2 = java.lang.StringBuilder()
                    for (model in selected_clients_list) {
                        cb2.append(model.client_name).append(",")
                    }
                    if (cb2.isNotEmpty()) {
                        cb2.deleteCharAt(cb2.length - 1)
                    }
                    at_add_clients?.text = cb2.toString()
                    
                    loadSelectedClients(arrayOfNulls(selected_clients_list.size))
                    
                    if (Constants.create_matter && selected_groups_list.isEmpty()) {
                        at_add_clients?.text = ""
                        at_add_corp_clients?.text = ""
                        at_assigned_team_members?.text = ""
                        selected_tm?.visibility = View.GONE
                        selected_clients?.visibility = View.GONE
                        ll_selected_clients?.visibility = View.GONE
                        ll_assigned_team_members?.removeAllViews()
                        ll_assign_team_members?.visibility = View.GONE
                        ll_add_clients?.visibility = View.GONE
                        selected_groups?.visibility = View.GONE
                        selected_clients_list.clear()
                        selected_tm_list.clear()
                        selected_corp_clients_list.clear()
                        selected_temp_clients_list.clear()
                        temporary_clients_list.clear()
                        temporary_corpclients_list.clear()
                        temporary_tm_list.clear()
                        Constants.selected_temp_clients_list.clear()
                    }
                    
                    callGroupsWebservice()
                    isChangesOccured = true
                    AddGctDetails()
                    AndroidUtils.ToggleButton(temporary_groups_list.size, btn_add_groups)
                }

                override fun onCancel() {}
            })
        } else {
            ll_selected_groups?.removeViewAt(position)
            selected_groups_list.remove(groupsModel)
            groupsModel.isChecked = false
            for (k in temporary_groups_list.indices) {
                if (temporary_groups_list[k].group_id == groupsModel.group_id) {
                    temporary_groups_list.removeAt(k)
                    break
                }
            }
            
            for (j in 0 until (ll_selected_groups?.childCount ?: 0)) {
                val iv_remove = ll_selected_groups?.getChildAt(j)?.findViewById<ImageView>(R.id.iv_remove_opponent)
                iv_remove?.tag = j
            }

            val stringBuilder = java.lang.StringBuilder()
            for (model in selected_groups_list) {
                stringBuilder.append(model.group_name).append(",")
            }
            if (stringBuilder.isNotEmpty()) {
                stringBuilder.deleteCharAt(stringBuilder.length - 1)
            }
            at_add_groups?.text = stringBuilder.toString()
            
            if (Constants.create_matter && selected_groups_list.isEmpty()) {
                at_add_clients?.text = ""
                at_add_corp_clients?.text = ""
                at_assigned_team_members?.text = ""
                selected_tm?.visibility = View.GONE
                selected_clients?.visibility = View.GONE
                ll_selected_clients?.visibility = View.GONE
                ll_assigned_team_members?.removeAllViews()
                ll_assign_team_members?.visibility = View.GONE
                ll_add_clients?.visibility = View.GONE
                selected_groups?.visibility = View.GONE
                selected_clients_list.clear()
                selected_tm_list.clear()
                selected_corp_clients_list.clear()
                selected_temp_clients_list.clear()
                temporary_clients_list.clear()
                temporary_corpclients_list.clear()
                temporary_tm_list.clear()
                Constants.selected_temp_clients_list.clear()
            }
            
            callGroupsWebservice()
            isChangesOccured = true
            AddGctDetails()
            AndroidUtils.ToggleButton(temporary_groups_list.size, btn_add_groups)
        }
    }

    private fun updateDisplay() {
        if (selected_groups_list.isNotEmpty()) {
            selected_groups?.visibility = View.VISIBLE
        } else {
            selected_groups?.visibility = View.GONE
        }
    }

    private fun load_existing_matter() {
        isInitialLoad = true
        isChangesOccured = false
        progress_dialog = AndroidUtils.get_progress(activity)
        val postdata = JSONObject()
        WebServiceHelper.callHttpWebService(
            this,
            requireContext(),
            WebServiceHelper.RestMethodType.GET,
            "v2/matter/" + chosen_matter + "/" + Constants.Matter_id + "?fields=clients,corporate,members",
            "chosen_member",
            postdata.toString()
        )
    }

    private fun load_existing_member_list(members: JSONArray?) {
        try {
            selected_tm_list.clear()
            temporary_tm_list.clear()
            
            if (members != null) {
                for (i in 0 until members.length()) {
                    val tmObject = members.getJSONObject(i)
                    val teamModel = TeamModel()
                    teamModel.tm_id = tmObject.getString("id")
                    teamModel.tm_name = tmObject.getString("name")
                    selected_tm_list.add(teamModel)
                    temporary_tm_list.add(teamModel)
                }
            }
            
            val stringBuilder = java.lang.StringBuilder()
            for (model in selected_tm_list) {
                stringBuilder.append(model.tm_name).append(",")
            }
            if (stringBuilder.isNotEmpty()) {
                stringBuilder.deleteCharAt(stringBuilder.length - 1)
            }
            at_assigned_team_members?.text = stringBuilder.toString()
            
            if (selected_tm_list.isNotEmpty()) {
                selected_tm?.visibility = View.VISIBLE
            } else {
                selected_tm?.visibility = View.GONE
            }
            
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun load_existing_clients_list() {
        try {
            selected_clients_list.clear()
            temporary_clients_list.clear()
            selected_corp_clients_list.clear()
            temporary_corpclients_list.clear()
            
            if (existing_clients != null) {
                for (i in 0 until existing_clients!!.length()) {
                    val clientObj = existing_clients!!.getJSONObject(i)
                    val clientsModel = ClientsModel()
                    clientsModel.client_id = clientObj.getString("id")
                    clientsModel.client_name = clientObj.getString("name")
                    clientsModel.client_type = clientObj.getString("type")
                    selected_clients_list.add(clientsModel)
                    temporary_clients_list.add(clientsModel)
                }
            }
            
            if (existing_corp_clients != null) {
                for (i in 0 until existing_corp_clients!!.length()) {
                    val clientObj = existing_corp_clients!!.getJSONObject(i)
                    val clientsModel = ClientsModel()
                    clientsModel.client_id = clientObj.getString("id")
                    clientsModel.client_name = clientObj.getString("name")
                    clientsModel.client_type = "corporate"
                    selected_corp_clients_list.add(clientsModel)
                    temporary_corpclients_list.add(clientsModel)
                }
            }
            
            val stringBuilder = java.lang.StringBuilder()
            for (model in selected_clients_list) {
                stringBuilder.append(model.client_name).append(",")
            }
            if (stringBuilder.isNotEmpty()) {
                stringBuilder.deleteCharAt(stringBuilder.length - 1)
            }
            at_add_clients?.text = stringBuilder.toString()
            
            val stringBuilderCorp = java.lang.StringBuilder()
            for (model in selected_corp_clients_list) {
                stringBuilderCorp.append(model.client_name).append(",")
            }
            if (stringBuilderCorp.isNotEmpty()) {
                stringBuilderCorp.deleteCharAt(stringBuilderCorp.length - 1)
            }
            at_add_corp_clients?.text = stringBuilderCorp.toString()
            
            if (selected_clients_list.isNotEmpty()) {
                selected_clients?.visibility = View.VISIBLE
            } else {
                selected_clients?.visibility = View.GONE
            }
            
            if (selected_corp_clients_list.isNotEmpty()) {
                selected_corp_clients?.visibility = View.VISIBLE
            } else {
                selected_corp_clients?.visibility = View.GONE
            }
            
            loadSelectedClients(arrayOfNulls(selected_clients_list.size))
            loadSelectedCorp_Clients()
            
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    fun update_matter() {
        if (!Constants.create_matter) {
            try {
                val postdata = JSONObject()
                val groups = JSONArray()
                val clients = JSONArray()
                val members = JSONArray()

                for (i in selected_groups_list.indices) {
                    val jsonObject = JSONObject()
                    jsonObject.put("id", selected_groups_list[i].group_id)
                    groups.put(jsonObject)
                }

                for (i in selected_clients_list.indices) {
                    val jsonObject = JSONObject()
                    jsonObject.put("id", selected_clients_list[i].client_id)
                    jsonObject.put("type", selected_clients_list[i].client_type)
                    clients.put(jsonObject)
                }

                for (i in selected_corp_clients_list.indices) {
                    val jsonObject = JSONObject()
                    jsonObject.put("id", selected_corp_clients_list[i].client_id)
                    jsonObject.put("type", "corporate")
                    clients.put(jsonObject)
                }

                for (i in selected_tm_list.indices) {
                    val jsonObject = JSONObject()
                    jsonObject.put("id", selected_tm_list[i].tm_id)
                    members.put(jsonObject)
                }
                
                postdata.put("groups", groups)
                postdata.put("clients", clients)
                postdata.put("members", members)
                
                val matterType = (Constants.MATTER_TYPE ?: "").lowercase(Locale.ROOT)
                val matterId = (Constants.Matter_id ?: "")
                WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.PUT, "v3/matter/$matterType/$matterId", "matter_update", postdata.toString())
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        } else {
            matter?.loadDocuments()
        }
    }
    private fun AddTempClientStatus() {
        val is_fname_empty = et_temp_fname?.text?.toString()?.isEmpty() ?: true
        val is_lname_empty = et_temp_lname?.text?.toString()?.isEmpty() ?: true
        val is_email_empty = et_temp_email?.text?.toString()?.isEmpty() ?: true
        val is_confirm_email_empty = et_temp_confirm_email?.text?.toString()?.isEmpty() ?: true
        val is_country_empty = et_temp_country?.text?.toString()?.isEmpty() ?: true
        if (is_fname_empty || is_lname_empty || is_email_empty || is_confirm_email_empty || is_country_empty) {
            btn_add_temp_client?.alpha = 0.5f
            btn_add_temp_client?.isEnabled = false
        } else {
            btn_add_temp_client?.alpha = 1.0f
            btn_add_temp_client?.isEnabled = true
        }
    }

    private fun check_temp_values() {
        val email = et_temp_email?.text?.toString()?.trim() ?: ""
        val confirmEmail = et_temp_confirm_email?.text?.toString()?.trim() ?: ""
        if (!AndroidUtils.isValidEmail(email)) {
            AndroidUtils.showAlert("Please Enter A Valid Email Address", activity)
        } else if (confirmEmail != email) {
            AndroidUtils.showAlert("Please Enter A Valid Confirm Email Address", activity)
        } else {
            add_temp_client()
        }
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
                jsonObject.put("fullname", et_temp_fname?.text?.toString())
                jsonObject.put("contact_person", et_temp_lname?.text?.toString())
                jsonObject.put("email", et_temp_email?.text?.toString())
                jsonObject.put("country", et_temp_country?.text?.toString())
                jsonObject.put("contact_phone", et_temp_phone?.text?.toString())
            } else {
                jsonObject.put("first_name", et_temp_fname?.text?.toString())
                jsonObject.put("last_name", et_temp_lname?.text?.toString())
                jsonObject.put("email", et_temp_email?.text?.toString())
                jsonObject.put("country", et_temp_country?.text?.toString())
            }
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.POST,
                "v3/relationship/temp-invite/$client_type",
                "temp_client",
                jsonObject.toString()
            )
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun call_corporate_clients() {
        try {
            val postdata = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/corporate/list",
                "attachment_corp_clients",
                postdata.toString()
            )
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

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
                loadSelectedClients(arrayOfNulls<String>(selected_clients_list.size))
            }
            if (selected_tm_list.isNotEmpty()) {
                loadTeamText()
                loadSelectedTM(arrayOfNulls<String>(selected_tm_list.size))
            }
            loadAddClient()
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    override fun onClick(v: View) {
        // Empty implementation to satisfy View.OnClickListener
    }
}
