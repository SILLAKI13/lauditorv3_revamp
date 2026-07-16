package com.digicoffer.lauditor.Email

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Documents.Documents
import com.digicoffer.lauditor.Documents.DocumentsListAdpater.GroupsListAdapter
import com.digicoffer.lauditor.Documents.Models.ClientsModel
import com.digicoffer.lauditor.Documents.Models.DocumentsModel
import com.digicoffer.lauditor.Documents.Models.MattersModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class emailadapter(
    private var messages: List<MessageModel>?,
    private val email: Email,
    private val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), AsyncTaskCompleteListener, Filterable {

    var itemsList: List<MessageModel> = messages ?: ArrayList()
    var dialog: AlertDialog? = null
    val matterlist = ArrayList<MattersModel>()
    val client_groups_list = ArrayList<DocumentsModel>()
    val selected_client_groups_list = ArrayList<DocumentsModel>()
    var tv_client_name: TextView? = null

    var ischecked = true
    var client_id = ""
    var matter_id = ""
    var client_name = ""
    var matterName = ""
    var msg_id = " "
    val baseUrl = Constants.EMAIL_UPLOAD_URL
    val token = Constants.TOKEN
    val msgId = Constants.msg_id
    val partId = Constants.part_id

    init {
        context_type = email.context
    }

    override fun getItemViewType(position: Int): Int {
        val msgList = messages
        if (msgList != null && msgList[position].getAttachment() != null) {
            val atts = msgList[position].getAttachment()
            if (atts != null && !atts.isEmpty() && msgList[position].isAttachment) {
                return VIEW_TYPE_ATTACHMENT
            }
        }
        return VIEW_TYPE_EMAIL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View
        return when (viewType) {
            VIEW_TYPE_EMAIL -> {
                view = inflater.inflate(R.layout.email_card_view, parent, false)
                EmailViewHolder(view)
            }
            VIEW_TYPE_ATTACHMENT -> {
                view = inflater.inflate(R.layout.attachment_item, parent, false)
                EmailViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                messages = if (charString.isEmpty()) {
                    itemsList
                } else {
                    val filteredList = ArrayList<MessageModel>()
                    for (row in itemsList) {
                        if (AndroidUtils.isNull(row.from).lowercase().contains(charString.lowercase())) {
                            filteredList.add(row)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.count = messages?.size ?: 0
                filterResults.values = messages
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                messages = filterResults.values as? ArrayList<MessageModel>
                notifyDataSetChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages?.get(position) ?: return
        when (holder.itemViewType) {
            VIEW_TYPE_EMAIL, VIEW_TYPE_ATTACHMENT -> {
                (holder as EmailViewHolder).bindEmail(message)
            }
        }
    }

    override fun getItemCount(): Int {
        return messages?.size ?: 0
    }

    override fun onClick(view: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {}

    inner class EmailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), AsyncTaskCompleteListener {
        val senderName: TextView = itemView.findViewById(R.id.sender_name)
        val subject: TextView = itemView.findViewById(R.id.subject)
        val gridRecyclerView: RecyclerView = itemView.findViewById(R.id.gridRecyclerView)
        var matter_name: TextView? = null
        var tvEmail: TextView? = null
        var rv_display_upload_groups_docs: RecyclerView? = null
        var rv_upload_groups: RecyclerView? = null
        var list_client: ListView? = null
        var list_matter: ListView? = null
        var ll_select_groups: LinearLayout? = null
        var ll_select_grp: LinearLayout? = null
        var ll_matter: LinearLayout? = null
        var ll_matter_view: LinearLayout? = null
        var ll_upload_client_group: LinearLayout? = null
        var ll_upload_groups: LinearLayout? = null
        var btn_group_cancel: Button? = null
        var cb_documents: CheckBox? = null
        var ll_client_name: LinearLayout? = null
        var ischecked_view = true
        var ischecked_matter = true
        var ischeckedClient_group = true
        private var custom_client: TextView? = null
        private var custom_matter: TextView? = null
        private var ll_custom_client: LinearLayout? = null
        var dropdown_icon: ImageView? = null
        var clear_icon: ImageView? = null
        var dropdown_icon2: ImageView? = null
        var clear_icon2: ImageView? = null
        var category = ""
        var btn_upload_new: AppCompatButton? = null
        var btn_cancel_save: AppCompatButton? = null
        var tv_select_groups: TextView? = null
        var tv_select_upload_group_name: TextView? = null
        var tv_select_upload_groups: TextView? = null
        var linearLayout2: LinearLayout? = null
        val groupsList = ArrayList<DocumentsModel>()
        var ischecked_group = true
        var btn_group_submit: Button? = null
        val clientsList = ArrayList<ClientsModel>()
        val CorpClientsList = ArrayList<ClientsModel>()
        var selectedClients = ArrayList<ClientsModel>()
        var constraint_root: LinearLayout? = null

        var selectedLanguage: BooleanArray? = null
        val selected_groups_list = ArrayList<DocumentsModel>()

        fun bindEmail(emailModel: MessageModel) {
            senderName.text = emailModel.from
            subject.text = emailModel.subject
            senderName.tag = emailModel.msgId

            if (Constants.ROLE == "AAM") {
                gridRecyclerView.visibility = GONE
            } else {
                gridRecyclerView.visibility = VISIBLE
            }
            if (emailModel.getAttachment() != null && !emailModel.getAttachment()!!.isEmpty()) {
                gridRecyclerView.layoutManager = GridLayoutManager(context_type, 2)
                gridRecyclerView.isNestedScrollingEnabled = false
                gridRecyclerView.setHasFixedSize(false)

                if (!emailModel.getAttachment()!!.isEmpty()) {
                    val adapterInstance = Attachmentadapter(context_type!!, emailModel.attachments)
                    gridRecyclerView.adapter = adapterInstance

                    adapterInstance.setOnAttachmentClickListener { position ->
                        if (!Constants.is_active) {
                            AndroidUtils.showRenewalPopup(activity)
                        } else {
                            Log.e("msgId", ":" + senderName.tag.toString())
                            Log.e("partId", ":" + emailModel.getAttachment()!![position].partId)
                            Constants.msg_id = senderName.tag.toString()
                            Constants.part_id = emailModel.getAttachment()!![position].partId
                            GridviewPopup(itemView)
                        }
                    }
                }
            }
        }

        private fun GridviewPopup(view: View) {
            val selectionGroupsView: RadioGroup
            val rbMatterView: RadioButton
            val rbClientView: RadioButton
            selected_groups_list.clear()
            val builder = AlertDialog.Builder(view.context)
            val inflater = LayoutInflater.from(view.context)
            val popupView = inflater.inflate(R.layout.document_upload, null)
            val tv_client_name = popupView.findViewById<TextView>(R.id.client_name)

            val group_name = popupView.findViewById<TextView>(R.id.group_name)
            group_name.setText(R.string.select_group)
            val client_namee = popupView.findViewById<TextView>(R.id.client_nameee)
            client_namee.setText(R.string.matter)
            client_namee.background = context_type!!.resources.getDrawable(R.drawable.button_left_green_background)

            client_namee.setTextColor(ContextCompat.getColor(context_type!!, R.color.white))
            val firm_namee = popupView.findViewById<TextView>(R.id.firm_nameee)
            firm_namee.setText(R.string.firm)
            firm_namee.background = context_type!!.resources.getDrawable(R.drawable.button_right_background)

            firm_namee.setTextColor(ContextCompat.getColor(context_type!!, R.color.black))
            if (Constants.CATEGORY != "solo") {
                firm_namee.visibility = VISIBLE
            } else {
                client_namee.setTextColor(ContextCompat.getColor(context_type!!, R.color.white))
                client_namee.background = context_type!!.resources.getDrawable(R.drawable.rectangular_button_green_count)
                firm_namee.visibility = GONE
            }
            ll_custom_client = popupView.findViewById(R.id.custom_client)
            custom_client = ll_custom_client?.findViewById(R.id.tv_spinner_view)
            dropdown_icon = ll_custom_client?.findViewById(R.id.img_dropdown_icon)
            clear_icon = ll_custom_client?.findViewById(R.id.img_clear_icon)
            matter_name = popupView.findViewById(R.id.matter_name)
            matter_name?.setText(R.string.matter)
            ll_matter = popupView.findViewById(R.id.ll_matter)
            ll_matter?.visibility = GONE

            ll_matter_view = popupView.findViewById(R.id.ll_matter_view)
            ll_matter_view?.visibility = GONE

            custom_matter = ll_matter?.findViewById(R.id.tv_spinner_view)
            dropdown_icon2 = ll_matter?.findViewById(R.id.img_dropdown_icon)
            clear_icon2 = ll_matter?.findViewById(R.id.img_clear_icon)
            list_matter = popupView.findViewById(R.id.list_matter)

            list_client = popupView.findViewById(R.id.list_client_email)
            btn_upload_new = popupView.findViewById(R.id.btn_upload_new)
            tv_select_groups = popupView.findViewById(R.id.tv_select_groups)
            rv_display_upload_groups_docs = popupView.findViewById(R.id.rv_display_upload_groups_docs)

            tv_select_upload_group_name = popupView.findViewById(R.id.tv_select_upload_group_name)
            tv_select_upload_group_name?.setText(R.string.select_groups)

            tv_select_upload_groups = popupView.findViewById(R.id.tv_select_upload_groups)
            custom_client?.setHint(R.string.select_client_name)
            custom_matter?.setHint(R.string.select_matters)
            tv_select_upload_groups?.setHint(R.string.select_groups)

            rv_upload_groups = popupView.findViewById(R.id.rv_upload_groups)
            rv_upload_groups?.background = context_type!!.getDrawable(R.drawable.rectangle_light_grey)
            ll_upload_client_group = popupView.findViewById(R.id.ll_upload_client_group)
            ll_upload_groups = popupView.findViewById(R.id.ll_upload_groups)

            btn_group_cancel = popupView.findViewById(R.id.btn_group_cancel)
            btn_group_cancel?.visibility = GONE
            btn_cancel_save = popupView.findViewById(R.id.btn_cancel_save)
            btn_group_submit = popupView.findViewById(R.id.btn_group_submit)
            btn_group_submit?.visibility = GONE
            cb_documents = popupView.findViewById(R.id.chk_selected)
            ll_select_groups = popupView.findViewById(R.id.ll_select_groups)
            ll_client_name = popupView.findViewById(R.id.ll_client_name)
            selectionGroupsView = popupView.findViewById(R.id.selectionGroups)
            selectionGroupsView.visibility = VISIBLE
            rbMatterView = popupView.findViewById(R.id.rbMatter)
            rbMatterView.isChecked = true
            rbClientView = popupView.findViewById(R.id.rbClient)
            linearLayout2 = popupView.findViewById(R.id.linearLayout2)
            ll_select_grp = popupView.findViewById(R.id.ll_select_grp)
            constraint_root = popupView.findViewById(R.id.constraint_root)
            val document_upload = popupView.findViewById<TextView>(R.id.document_upload)
            selected_client_groups_list.clear()
            matter_id = ""
            btn_upload_new?.setText(R.string.upload)
            rv_display_upload_groups_docs?.background = context_type!!.getDrawable(R.drawable.rectangle_light_grey)
            callLegalMatter()
            selectedClients = ArrayList()
            ll_client_name?.visibility = GONE
            tv_client_name.setText(R.string.client_name)
            builder.setView(popupView)
            dialog = builder.create()
            dialog?.show()
            btn_upload_new?.setOnClickListener {
                if (client_id.isNotEmpty() || !tv_select_groups?.text.toString().isEmpty()) {
                    if (Constants.isGmail) {
                        callUploadDocument(baseUrl ?: "", token ?: "", Constants.msg_id ?: "", Constants.part_id ?: "")
                    } else {
                        callUploadDocument(Constants.EMAIL_GET_URL ?: "", token ?: "", Constants.msg_id ?: "", Constants.part_id ?: "")
                    }
                }
            }
            btn_cancel_save?.setOnClickListener {
                dialog?.dismiss()
            }

            tv_select_upload_groups?.setOnClickListener {
                if (ischeckedClient_group) {
                    ll_upload_client_group?.visibility = VISIBLE
                } else {
                    ll_upload_client_group?.visibility = GONE
                }
                ischeckedClient_group = !ischeckedClient_group
            }
            rbMatterView.setOnClickListener {
                client_id = ""
                matter_id = ""
                callLegalMatter()
                ll_client_name?.visibility = GONE
                ll_matter_view?.visibility = VISIBLE
                client_namee.setText(R.string.matter)
            }
            rbClientView.setOnClickListener {
                client_id = ""
                matter_id = ""
                matterlist.clear()
                client_name = ""
                ll_matter_view?.visibility = GONE
                client_namee.setText(R.string.client)
                ll_client_name?.visibility = VISIBLE
                callClientWebservice()
            }
            client_namee.setOnClickListener {
                selectionGroupsView.visibility = VISIBLE
                ll_select_groups?.visibility = GONE
                ll_client_name?.visibility = GONE
                ll_select_grp?.visibility = GONE
                custom_client?.text = ""
                tv_select_groups?.text = ""
                selected_groups_list.clear()
                groupsList.clear()
                client_namee.background = context_type!!.resources.getDrawable(R.drawable.button_left_green_background)
                client_namee.setTextColor(ContextCompat.getColor(context_type!!, R.color.white))
                firm_namee.background = context_type!!.resources.getDrawable(R.drawable.button_right_background)
                firm_namee.setTextColor(ContextCompat.getColor(context_type!!, R.color.black))
                if (Constants.CATEGORY != "solo") {
                    firm_namee.visibility = VISIBLE
                } else {
                    client_namee.setTextColor(ContextCompat.getColor(context_type!!, R.color.white))
                    client_namee.background = context_type!!.resources.getDrawable(R.drawable.rectangular_button_green_count)
                    firm_namee.visibility = GONE
                }
                ll_matter_view?.visibility = VISIBLE
                ll_upload_groups?.visibility = GONE
                dropdown_icon?.visibility = VISIBLE
                client_namee.setText(R.string.matter)
                clear_icon?.visibility = GONE
                matterName = ""
                matter_id = ""
                custom_matter?.text = ""
                dropdown_icon2?.visibility = VISIBLE
                clear_icon2?.visibility = GONE
                rbMatterView.isChecked = true
                callLegalMatter()
            }

            firm_namee.setOnClickListener {
                selectionGroupsView.visibility = GONE
                ll_client_name?.visibility = GONE
                ll_matter_view?.visibility = GONE
                ll_select_groups?.visibility = VISIBLE
                ll_select_grp?.visibility = VISIBLE
                custom_client?.text = ""
                tv_select_groups?.text = ""
                selected_groups_list.clear()
                matter_id = ""
                rbMatterView.isChecked = true
                client_namee.setText(R.string.matter)
                groupsList.clear()
                firm_namee.background = context_type!!.resources.getDrawable(R.drawable.button_right_green_background)
                firm_namee.setTextColor(Color.WHITE)
                client_namee.background = context_type!!.resources.getDrawable(R.drawable.button_left_background)
                ll_upload_groups?.visibility = GONE
                ll_matter_view?.visibility = GONE
                client_namee.setTextColor(ContextCompat.getColor(context_type!!, R.color.black))
            }
            ll_custom_client?.setOnClickListener {
                if (clientsList.isEmpty()) {
                    list_client?.visibility = GONE
                } else {
                    AndroidUtils.display_listview(ischecked_view, list_client)
                }
                ischecked_view = !ischecked_view
            }
            ll_matter?.setOnClickListener {
                AndroidUtils.display_listview(ischecked_matter, list_matter)
                ischecked_matter = !ischecked_matter
            }
            clear_icon2?.setOnClickListener {
                matter_id = ""
                matterName = ""
                selected_client_groups_list.clear()
                AndroidUtils.DisplaySpinnerView(list_matter, custom_matter, matterName, dropdown_icon2, clear_icon2, false)
            }
            clear_icon?.setOnClickListener {
                client_id = ""
                selected_client_groups_list.clear()
                matter_id = ""
                ll_matter_view?.visibility = GONE
                ll_upload_groups?.visibility = GONE
                AndroidUtils.DisplaySpinnerView(list_client, custom_client, client_name, dropdown_icon, clear_icon, false)
            }
            tv_select_groups?.setOnClickListener {
                if (ischecked_group) {
                    if (groupsList.isEmpty()) {
                        callGroupsWebservice()
                    }
                    rv_display_upload_groups_docs?.visibility = VISIBLE
                    linearLayout2?.visibility = VISIBLE
                } else {
                    rv_display_upload_groups_docs?.visibility = GONE
                    linearLayout2?.visibility = GONE
                }
                ischecked_group = !ischecked_group
            }
        }

        private fun callClientGroupsWebservice() {
            try {
                progress_dialog = AndroidUtils.get_progress(context_type as Activity)
                val jsonObject = JSONObject()
                val clientGroups = JSONArray()
                val clients = JSONObject()
                for (j in 0 until clientsList.size) {
                    if (clientsList[j].id == client_id) {
                        val clientsModel = clientsList[j]
                        clients.put("id", clientsModel.id)
                        clients.put("type", clientsModel.type)
                        clientGroups.put(clients)
                    }
                }
                jsonObject.put("clients", clientGroups)
                jsonObject.put("matterid", matter_id)
                WebServiceHelper.callHttpWebService(this, context_type, WebServiceHelper.RestMethodType.PUT, "v3/documents/groupslist", "Client Groups", jsonObject.toString())
            } catch (e: Exception) {
                if (progress_dialog != null && progress_dialog!!.isShowing) {
                    AndroidUtils.dismiss_dialog(progress_dialog)
                }
            }
        }

        fun callUploadDocument(uploadUrl: String, token: String, msgId: String, partId: String) {
            try {
                progress_dialog = AndroidUtils.get_progress(context_type as Activity)
                try {
                    val clients = JSONArray()
                    for (clientsModel in selectedClients) {
                        if (clientsModel.id == client_id) {
                            val jsonObject_client = JSONObject()
                            jsonObject_client.put("id", clientsModel.id)
                            jsonObject_client.put("type", clientsModel.type)
                            clients.put(jsonObject_client)
                        }
                    }

                    val groups = JSONArray()
                    for (documentsModel in selected_groups_list) {
                        val groupId = documentsModel.group_id
                        if (groupId != null && groupId.isNotEmpty()) {
                            groups.put(groupId)
                        }
                    }

                    val jsonObject = JSONObject()
                    val category = if (clients.length() > 0) "client" else "firm"
                    val matter = JSONArray()
                    if (matter_id.isNotEmpty()) {
                        matter.put(matter_id)
                        matter.put(matter_id)
                    }
                    jsonObject.put("category", category)
                    jsonObject.put("clientids", clients)
                    jsonObject.put("matters", matter)
                    jsonObject.put("groupids", groups)
                    jsonObject.put("enableDownload", true)

                    Log.e("Generated JSON", jsonObject.toString())
                    val url = "$uploadUrl${Constants.mail_document}$token/$msgId?partid=$partId"

                    WebServiceHelper.callEmailHttpWebService(this, context_type, WebServiceHelper.RestMethodType.POST, url, "uploaded file", jsonObject.toString())
                    Log.d("json_value", url)

                } catch (e: Exception) {
                    Log.e("callUploadDocument", "Error occurred while constructing request: " + e.message)
                    e.fillInStackTrace()
                }
            } catch (e: Exception) {
                Log.e("callUploadDocument", "Error occurred while executing callUploadDocument: " + e.message)
                if (progress_dialog != null && progress_dialog!!.isShowing) {
                    AndroidUtils.dismiss_dialog(progress_dialog)
                }
                e.fillInStackTrace()
            }
        }

        private fun callClientWebservice() {
            try {
                progress_dialog = AndroidUtils.get_progress(context_type as Activity)
                val jsonObject = JSONObject()
                WebServiceHelper.callHttpWebService(this, context_type, WebServiceHelper.RestMethodType.GET, "v3/client/all/list", "Clients List", jsonObject.toString())
            } catch (e: Exception) {
                if (progress_dialog != null && progress_dialog!!.isShowing) {
                    AndroidUtils.dismiss_dialog(progress_dialog)
                }
            }
        }

        private fun callGroupsWebservice() {
            try {
                progress_dialog = AndroidUtils.get_progress(context_type as Activity)
                val jsonObject = JSONObject()
                WebServiceHelper.callHttpWebService(this, context_type, WebServiceHelper.RestMethodType.GET, "v3/groups", "Groups", jsonObject.toString())
            } catch (e: Exception) {
                if (progress_dialog != null && progress_dialog!!.isShowing) {
                    AndroidUtils.dismiss_dialog(progress_dialog)
                }
            }
        }

        private fun loadClients(data: JSONObject) throws JSONException {
            val relationships = data.getJSONArray("relationships")
            clientsList.clear()
            for (i in 0 until relationships.length()) {
                val jsonObject = relationships.getJSONObject(i)
                val clientsModel = ClientsModel()
                clientsModel.id = jsonObject.getString("id")
                clientsModel.name = jsonObject.getString("name")
                clientsModel.type = jsonObject.getString("type")
                clientsList.add(clientsModel)
            }
        }

        private fun loadCorpClients(data: JSONObject) throws JSONException {
            val relationships = data.getJSONArray("relationships")
            CorpClientsList.clear()
            for (i in 0 until relationships.length()) {
                val jsonObject = relationships.getJSONObject(i)
                val clientsModel = ClientsModel()
                clientsModel.id = jsonObject.getString("id")
                clientsModel.name = jsonObject.getString("name")
                if (jsonObject.getString("type") != "consumer") {
                    clientsModel.type = "corporate"
                }
                CorpClientsList.add(clientsModel)
            }
            clientsList.addAll(CorpClientsList)
            if (clientsList.isEmpty()) {
                ll_client_name?.visibility = GONE
            } else {
                initUI(clientsList)
            }
        }

        private fun loadGroupsData(data: JSONArray, groupsList: ArrayList<DocumentsModel>, isclient_groups: Boolean) {
            groupsList.clear()
            selected_client_groups_list.clear()
            try {
                for (i in 0 until data.length()) {
                    val jsonObject = data.getJSONObject(i)
                    val documentsModel = DocumentsModel()
                    documentsModel.group_id = jsonObject.optString("id")
                    documentsModel.group_name = jsonObject.optString("name")
                    if (jsonObject.optString("name") != "AAM" && jsonObject.optString("name") != "SuperUser") {
                        documentsModel.group_id = jsonObject.optString("id")
                        documentsModel.group_name = jsonObject.optString("name")
                        if (isclient_groups) {
                            documentsModel.isGroupChecked = true
                            selected_client_groups_list.add(documentsModel)
                        }
                        groupsList.add(documentsModel)
                    }
                }
                if (!selected_client_groups_list.isEmpty()) {
                    ll_upload_groups?.visibility = VISIBLE
                } else {
                    ll_upload_groups?.visibility = GONE
                }
                selectedLanguage = BooleanArray(groupsList.size)
                if (isclient_groups) {
                    GroupsPopup(rv_upload_groups!!, client_groups_list, selected_client_groups_list, rv_upload_groups!!, tv_select_upload_groups!!)
                } else {
                    GroupsPopup(ll_select_groups!!, groupsList, selected_groups_list, rv_display_upload_groups_docs!!, tv_select_groups!!)
                }
            } catch (e: JSONException) {
                e.fillInStackTrace()
                AndroidUtils.showAlert(e.message, context_type as Activity)
            }
        }

        @SuppressLint("MissingInflatedId")
        private fun GroupsPopup(upload_group_layout: View, groupsList: ArrayList<DocumentsModel>, selected_groups_list: ArrayList<DocumentsModel>, rv_display_upload_groups_docs: RecyclerView, tv_select_groups: TextView) {
            tv_select_groups.text = ""
            upload_group_layout.visibility = VISIBLE
            try {
                for (i in 0 until groupsList.size) {
                    for (j in 0 until selected_groups_list.size) {
                        if (groupsList[i].group_id == selected_groups_list[j].group_id) {
                            val documentsModel = groupsList[i]
                            documentsModel.isGroupChecked = true
                        }
                    }
                }

                val layoutManager = LinearLayoutManager(context_type, LinearLayoutManager.VERTICAL, false)
                rv_display_upload_groups_docs.layoutManager = layoutManager
                val documentsAdapter = GroupsListAdapter(groupsList, Documents::class.java.newInstance(), GroupsListAdapter.OnCheckedChangeListener { documentsModel ->
                    if (documentsModel.isGroupChecked) {
                        selected_groups_list.add(documentsModel)
                    } else {
                        for (i in 0 until selected_groups_list.size) {
                            if (selected_groups_list[i].group_id == documentsModel.group_id) {
                                selected_groups_list.removeAt(i)
                                break
                            }
                        }
                    }
                    tv_select_groups.text = ""
                    val value = Array(selected_groups_list.size) { "" }
                    for (i in 0 until selected_groups_list.size) {
                        value[i] = selected_groups_list[i].group_name ?: ""
                    }
                    val str = TextUtils.join(",", value)
                    tv_select_groups.text = str
                })
                val value = Array(selected_groups_list.size) { "" }
                for (i in 0 until selected_groups_list.size) {
                    value[i] = selected_groups_list[i].group_name ?: ""
                }
                val str = TextUtils.join(",", value)
                tv_select_groups.text = str
                rv_display_upload_groups_docs.adapter = documentsAdapter
                AndroidUtils.LoadList(rv_display_upload_groups_docs, context_type, groupsList.size, true)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        private fun callLegalMatter() {
            try {
                progress_dialog = AndroidUtils.get_progress(context_type as Activity)
                val jsonObject = JSONObject()
                WebServiceHelper.callHttpWebService(this, context_type, WebServiceHelper.RestMethodType.GET, "v2/matter/list", "Legal Matter", jsonObject.toString())
            } catch (e: Exception) {
                if (progress_dialog != null && progress_dialog!!.isShowing) {
                    AndroidUtils.dismiss_dialog(progress_dialog)
                }
                e.fillInStackTrace()
            }
        }

        private fun loadMatters(matters: JSONArray) throws JSONException {
            matterlist.clear()
            for (i in 0 until matters.length()) {
                val jsonObject = matters.getJSONObject(i)
                val mattersModel = MattersModel()
                mattersModel.id = jsonObject.optString("id")
                mattersModel.title = jsonObject.optString("title")
                mattersModel.type = jsonObject.optString("type")
                matterlist.add(0, mattersModel)
            }
            if (matters.length() == 0) {
                ll_matter_view?.visibility = GONE
                ll_matter?.visibility = GONE
            } else {
                ll_matter_view?.visibility = VISIBLE
                ll_matter?.visibility = VISIBLE
            }
            initMatter()
        }

        private fun initMatter() {
            val adapter = CommonSpinnerAdapter(context_type as Activity, matterlist)
            list_matter?.adapter = adapter
            list_matter?.setOnItemClickListener { parent, view, position, id ->
                matter_id = ""
                matter_id = matterlist[position].id ?: ""
                matterName = matterlist[position].title ?: ""
                Log.d("Matter_value_name", matterName)
                selected_client_groups_list.clear()
                AndroidUtils.DisplaySpinnerView(list_matter, custom_matter, matterName, dropdown_icon2, clear_icon2, true)
                ischecked_matter = true
            }
        }

        private fun initUI(clientsList: ArrayList<ClientsModel>) {
            val adapter = CommonSpinnerAdapter(context_type as Activity, clientsList)
            list_client?.adapter = adapter
            list_client?.setOnItemClickListener { parent, view, position, id ->
                client_id = ""
                client_id = clientsList[position].id ?: ""
                client_name = clientsList[position].name ?: ""
                Log.d("Client_value_name", client_name)
                if (!selectedClients.contains(clientsList[position])) {
                    selectedClients.add(clientsList[position])
                }
                matterlist.clear()
                selected_client_groups_list.clear()
                AndroidUtils.DisplaySpinnerView(list_matter, custom_matter, matterName, dropdown_icon2, clear_icon2, false)
                AndroidUtils.DisplaySpinnerView(list_client, custom_client, client_name, dropdown_icon, clear_icon, true)
                ischecked_view = true
            }
        }

        override fun onClick(view: View) {}

        private fun callCorpClientWebservice() {
            try {
                val jsonObject = JSONObject()
                WebServiceHelper.callHttpWebService(this, context_type, WebServiceHelper.RestMethodType.GET, "v3/corporate/list", "Corp Clients List", jsonObject.toString())
            } catch (e: Exception) {
                if (progress_dialog != null && progress_dialog!!.isShowing) {
                    AndroidUtils.dismiss_dialog(progress_dialog)
                }
            }
        }

        override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val result = JSONObject(httpResult.responseContent ?: "{}")
                    val requestType = httpResult.requestType ?: ""

                    if ("Clients List" == requestType) {
                        val data = result.getJSONObject("data")
                        try {
                            callCorpClientWebservice()
                            loadClients(data)
                        } catch (e: Exception) {
                            e.fillInStackTrace()
                        }
                    } else if (requestType == "Corp Clients List") {
                        try {
                            loadCorpClients(result)
                        } catch (e: Exception) {
                            e.fillInStackTrace()
                        }
                    } else if (requestType == "Groups") {
                        val data = result.getJSONArray("data")
                        loadGroupsData(data, groupsList, false)
                    } else if (requestType == "Client Groups") {
                        val data = result.getJSONArray("data")
                        loadGroupsData(data, client_groups_list, true)
                    } else if (requestType == "Legal Matter") {
                        val matters = result.getJSONArray("matters")
                        loadMatters(matters)
                    } else if (requestType == "uploaded file") {
                        if (result.has("message")) {
                            val message = result.optString("message")
                            AndroidUtils.showAlert_docs("Success!", message, email.activity)
                        } else {
                            val msg = result.getString("msg")
                            AndroidUtils.showToast(msg, context_type)
                        }
                        dialog?.dismiss()
                    }
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
            } else {
                try {
                    val result = JSONObject(httpResult.responseContent ?: "{}")
                    val requestType = httpResult.requestType ?: ""
                    if (requestType == "uploaded file") {
                        if (result.has("message")) {
                            val message = result.optString("message")
                            AndroidUtils.showAlert_docs("Success!", message, email.activity)
                        } else {
                            val msg = result.getString("msg")
                            AndroidUtils.showToast(msg, context_type)
                        }
                        dialog?.dismiss()
                    }
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_EMAIL = 0
        private const val VIEW_TYPE_ATTACHMENT = 1
        @JvmField
        var context_type: Context? = null
    }
}
