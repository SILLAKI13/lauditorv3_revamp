package com.digicoffer.lauditor.Email

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Documents.Documents
import com.digicoffer.lauditor.Documents.DocumentsListAdpater.GroupsListAdapter
import com.digicoffer.lauditor.Documents.Models.ClientsModel
import com.digicoffer.lauditor.Documents.Models.DocumentsModel
import com.digicoffer.lauditor.Documents.Models.MattersModel
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel
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
import java.net.HttpURLConnection
import java.util.ArrayList
import java.util.Objects
import java.util.Stack

class Email : Fragment(), AsyncTaskCompleteListener {

    val CorpClientsList = ArrayList<ClientsModel>()
    private var URL = ""
    var msgId = ""
    var adapter: EmailAdapter? = null
    var matter_id = ""
    var ischeckedClient_group = true
    var ISCHECK_EMAIL = false
    var toEmail = ""
    var subject = ""
    var email: Email? = null
    var emailModel = EmailModel()
    val matterlist = ArrayList<MattersModel>()
    var body = ""
    val documentFilenames: List<String> = ArrayList()

    private val selectedDocumentName = ""
    var doc_count = 0
    var ischecked_group = true
    var selectedLanguage: BooleanArray? = null
    var btn_group_cancel: Button? = null
    var btn_group_submit: Button? = null
    var btn_continue: Button? = null
    var btn_create: AppCompatButton? = null
    var btn_cancel_save: AppCompatButton? = null

    var et_Search_email_document: TextInputEditText? = null
    var tv_select_groups: TextView? = null
    var tv_select_upload_group_name: TextView? = null
    var tv_select_upload_groups: TextView? = null
    val selected_groups_list = ArrayList<DocumentsModel>()
    val view_docs_list = ArrayList<ViewDocumentsModel>()
    var ISCHECK_AUTH = false
    var ischeck_label = false
    var ischeck_auth = false
    var nextPageToken = ""
    var ll_attach_grp: LinearLayout? = null

    val groupsList = ArrayList<DocumentsModel>()
    val client_groups_list = ArrayList<DocumentsModel>()
    val selected_client_groups_list = ArrayList<DocumentsModel>()
    var arrow_left: ImageView? = null
    var clear_search: ImageView? = null
    var ischecked = true
    var ischecked_matter = true
    private var custom_client: TextView? = null
    private var custom_matter: TextView? = null
    private var ll_custom_client: LinearLayout? = null
    var dropdown_icon: ImageView? = null
    var clear_icon: ImageView? = null
    var dropdown_icon2: ImageView? = null
    var clear_icon2: ImageView? = null
    private val currentPosition = 1
    var attachmentsArray = initializeAttachmentsArray()

    var composeDialog: AlertDialog? = null

    private fun initializeAttachmentsArray(): JSONArray {
        val attachmentsArray = JSONArray()
        try {
            val attachment1 = JSONObject()
            attachment1.put("document_name", "Document 1")
            attachment1.put("name", "attachment1.txt")
            attachment1.put("size", "10 KB")
            attachmentsArray.put(attachment1)

            val attachment2 = JSONObject()
            attachment2.put("document_name", "Document 2")
            attachment2.put("name", "attachment2.pdf")
            attachment2.put("size", "100 KB")
            attachmentsArray.put(attachment2)
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
        return attachmentsArray
    }

    var et_Search: TextInputEditText? = null
    var rv_documents_email: RecyclerView? = null
    var ischecked_group_view = false
    val clientsList = ArrayList<ClientsModel>()

    val pageStack = Stack<List<MessageModel>>()

    var emailcheck = false
    val messages: MutableList<MessageModel> = ArrayList()
    val totalMessageArray = ArrayList<List<MessageModel>>()
    val nextMessages = ArrayList<List<MessageModel>>()
    var pre_next_position = 0
    private var requestType: String? = null
    var httpURLConnection: HttpURLConnection? = null
    var inbox_textViews: TextView? = null
    var first_button: AppCompatButton? = null
    var search_email: AppCompatButton? = null
    var sends_button: AppCompatButton? = null
    var to_input: EditText? = null
    var subject_input: EditText? = null
    var message_inputs: EditText? = null
    var client_list_view: ListView? = null
    var list_matter: ListView? = null
    var grp_name: TextView? = null
    var matter_name: TextView? = null
    var rb_google: RadioButton? = null
    var rb_outlook: RadioButton? = null
    var client_id = ""
    var clientname = ""
    var matterName = ""
    var rv_display_upload_groups_docs: RecyclerView? = null
    var rv_upload_groups: RecyclerView? = null
    var CATEGORY_TAG = ""
    var linearLayout2: LinearLayout? = null
    var ll_mail_provider: LinearLayout? = null
    var ll_matter: LinearLayout? = null
    var ll_matter_view: LinearLayout? = null
    var array_group = JSONArray()
    var yourGridView: GridView? = null
    var composeDocuments: ImageView? = null

    var overlay: LinearLayout? = null
    var ll_upload_groups: LinearLayout? = null
    var ll_upload_client_group: LinearLayout? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.email_layout, container, false)
        composeDocuments = view.findViewById(R.id.compose)
        composeDocuments?.setAlpha(0.3f)
        btn_continue = view.findViewById(R.id.btn_continue)
        btn_continue?.setText(R.string.continue_)
        rb_google = view.findViewById(R.id.rb_google)
        rb_google?.setText(R.string.google)
        rb_outlook = view.findViewById(R.id.rb_outlook)
        rb_outlook?.setText(R.string.outlook)
        ll_mail_provider = view.findViewById(R.id.ll_mail_provider)
        ll_mail_provider?.visibility = VISIBLE
        Constants.isGmail = true
        val arrow_right = view.findViewById<ImageView>(R.id.arrow_right)
        arrow_left = view.findViewById(R.id.arrow_left)
        arrow_left?.setAlpha(0.3f)
        clear_search = view.findViewById(R.id.clear_search)
        clear_search?.visibility = GONE
        inbox_textViews = view.findViewById(R.id.inbox_textViews)
        first_button = view.findViewById(R.id.first_button)
        first_button?.setAlpha(0.3f)
        et_Search = view.findViewById(R.id.et_Search)
        et_Search?.addTextChangedListener(et_Search?.let { Validation(it) })
        email = this
        search_email = view.findViewById(R.id.search_email)
        search_email?.setAlpha(0.3f)
        sends_button = view.findViewById(R.id.sends_button)
        overlay = view.findViewById(R.id.overlay)

        composeDocuments?.setOnClickListener {
            if (!Constants.is_active) {
                AndroidUtils.showRenewalPopup(requireActivity())
            } else {
                Constants.composAttachDocAry = ArrayList()
                openComposePopup()
            }
        }
        first_button?.setOnClickListener {
            if (!totalMessageArray.isEmpty()) {
                pre_next_position = 0
                updateRecyclerView(totalMessageArray[pre_next_position])
                arrow_left?.setAlpha(0.3f)
                first_button?.setAlpha(0.3f)
                arrow_right?.setAlpha(1.0f)
            }
        }

        rb_google?.setOnClickListener {
            rb_outlook?.isChecked = false
            Constants.isGmail = true
        }
        rb_outlook?.setOnClickListener {
            rb_google?.isChecked = false
            Constants.isGmail = false
        }
        btn_continue?.setOnClickListener {
            Constants.check_url()
            callLabel()
            ll_mail_provider?.visibility = GONE
        }
        arrow_right?.setOnClickListener {
            if (isMorePagesAvailable()) {
                pre_next_position++
                callMessageListnext()
                arrow_left?.setAlpha(1.0f)
                first_button?.setAlpha(1.0f)
            } else {
                Toast.makeText(requireContext(), "No more pages available", Toast.LENGTH_SHORT).show()
            }
        }

        arrow_left?.setOnClickListener {
            if (pre_next_position > 0) {
                pre_next_position--
                val previousPageMessages = totalMessageArray[pre_next_position]
                updateRecyclerView(previousPageMessages)

                if (pre_next_position == 0) {
                    first_button?.setAlpha(0.3f)
                    arrow_left?.setAlpha(0.3f)
                    Toast.makeText(requireContext(), "You are already on the first page", Toast.LENGTH_SHORT).show()
                } else {
                    first_button?.setAlpha(1.0f)
                    arrow_left?.setAlpha(1.0f)
                }
            }
        }

        return view
    }

    private fun isMorePagesAvailable(): Boolean {
        return !TextUtils.isEmpty(nextPageToken)
    }

    @SuppressLint("MissingInflatedId")
    private fun openComposePopup() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.compose, null)
        val attachmentImageView = view.findViewById<ImageView>(R.id.attachments)
        val cross_icon = view.findViewById<ImageView>(R.id.close_comp_id)
        to_input = view.findViewById(R.id.to_input)
        subject_input = view.findViewById(R.id.subject_input)
        message_inputs = view.findViewById(R.id.message_inputss)
        sends_button = view.findViewById(R.id.sends_button)
        overlay?.visibility = VISIBLE
        yourGridView = view.findViewById(R.id.compose_gridview)
        yourGridView?.visibility = GONE

        sends_button?.setAlpha(0.4f)
        builder.setView(view)
        composeDialog = builder.create()
        composeDialog?.setCanceledOnTouchOutside(false)
        composeDialog?.show()
        if (Constants.ROLE == "AAM") {
            attachmentImageView.visibility = GONE
        } else {
            attachmentImageView.visibility = VISIBLE
        }
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                validateEmail()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        }
        to_input?.addTextChangedListener(textWatcher)
        subject_input?.addTextChangedListener(textWatcher)
        message_inputs?.addTextChangedListener(textWatcher)

        sends_button?.setOnClickListener {
            if (subject_input?.text.toString().isEmpty() && message_inputs?.text.toString().isEmpty()) {
                AndroidUtils.showEmailAlert(requireActivity(), email)
            } else {
                send_email()
            }
        }

        attachmentImageView.setOnClickListener {
            val attachmentDialogBuilder = AlertDialog.Builder(requireContext())
            val attachmentInflater = requireActivity().layoutInflater
            val attachmentView = attachmentInflater.inflate(R.layout.attach_document, null)
            val client_name = attachmentView.findViewById<TextView>(R.id.client_name)
            client_name.setText(R.string.client_name)
            CATEGORY_TAG = "client"
            matter_name = attachmentView.findViewById(R.id.matter_name)
            matter_name?.setText(R.string.matter)
            ll_matter = attachmentView.findViewById(R.id.ll_matter)
            ll_matter?.visibility = GONE
            val selectionGroups: RadioGroup
            val rbMatter: RadioButton
            val rbClient: RadioButton
            rbMatter = attachmentView.findViewById(R.id.rbMatter)
            rbMatter.isChecked = true
            rbClient = attachmentView.findViewById(R.id.rbClient)
            selectionGroups = attachmentView.findViewById(R.id.selectionGroups)
            selectionGroups.visibility = VISIBLE
            ll_matter_view = attachmentView.findViewById(R.id.ll_matter_view)
            ll_matter_view?.visibility = VISIBLE

            custom_matter = ll_matter?.findViewById(R.id.tv_spinner_view)
            dropdown_icon2 = ll_matter?.findViewById(R.id.img_dropdown_icon)
            clear_icon2 = ll_matter?.findViewById(R.id.img_clear_icon)
            list_matter = attachmentView.findViewById(R.id.list_matter)

            ll_custom_client = attachmentView.findViewById(R.id.custom_client)
            custom_client = ll_custom_client?.findViewById(R.id.tv_spinner_view)
            dropdown_icon = ll_custom_client?.findViewById(R.id.img_dropdown_icon)
            clear_icon = ll_custom_client?.findViewById(R.id.img_clear_icon)

            val compose_client_name = attachmentView.findViewById<TextView>(R.id.compose_client_name)
            compose_client_name.setText(R.string.matter)
            compose_client_name.background = requireContext().resources.getDrawable(R.drawable.button_left_green_background)

            compose_client_name.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            val compose_firm_name = attachmentView.findViewById<TextView>(R.id.compose_firm_name)
            compose_firm_name.setText(R.string.firm)
            rv_documents_email = attachmentView.findViewById(R.id.rv_documents_email)
            rv_documents_email?.visibility = GONE
            client_list_view = attachmentView.findViewById(R.id.client_list_view)
            rv_display_upload_groups_docs = attachmentView.findViewById(R.id.rv_display_upload_groups_docs)
            rv_display_upload_groups_docs?.background = requireActivity().getDrawable(R.drawable.rectangle_light_grey)
            client_list_view?.visibility = GONE
            btn_group_cancel = attachmentView.findViewById(R.id.btn_group_cancel)
            btn_group_cancel?.visibility = GONE
            btn_group_submit = attachmentView.findViewById(R.id.btn_group_submit)
            btn_group_submit?.visibility = GONE
            tv_select_groups = attachmentView.findViewById(R.id.tv_select_groups)
            tv_select_groups?.visibility = GONE
            ll_attach_grp = attachmentView.findViewById(R.id.ll_attach_grp)
            grp_name = attachmentView.findViewById(R.id.grp_name)
            grp_name?.setText(R.string.select_group)
            et_Search_email_document = attachmentView.findViewById(R.id.et_Search_email_document)
            val ll_select_groups_layout = attachmentView.findViewById<LinearLayout>(R.id.ll_select_groups)
            ll_select_groups_layout.visibility = GONE
            val ll_client_name_layout = attachmentView.findViewById<LinearLayout>(R.id.ll_client_name)
            linearLayout2 = attachmentView.findViewById(R.id.linearLayout2)
            btn_create = attachmentView.findViewById(R.id.btn_create)
            btn_cancel_save = attachmentView.findViewById(R.id.btn_cancel_save)
            btn_create?.setText(R.string.attach)
            btn_create?.setAlpha(0.5f)
            btn_create?.setEnabled(false)
            ll_client_name_layout.visibility = GONE
            attachmentDialogBuilder.setView(attachmentView)
            val attachmentDialog = attachmentDialogBuilder.create()
            attachmentDialog.show()
            callLegalMatter()
            custom_client?.setHint(R.string.select_client_name)
            custom_matter?.setHint(R.string.select_matters)
            tv_select_groups?.setHint(R.string.select_groups)
            if (Constants.CATEGORY != "solo") {
                compose_firm_name.visibility = VISIBLE
            } else {
                compose_client_name.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                compose_client_name.background = requireContext().resources.getDrawable(R.drawable.rectangular_button_green_count)
                compose_firm_name.visibility = GONE
            }
            compose_client_name.setOnClickListener {
                CATEGORY_TAG = "client"
                selectionGroups.visibility = VISIBLE
                ll_select_groups_layout.visibility = GONE
                ll_matter_view?.visibility = VISIBLE
                ll_client_name_layout.visibility = GONE
                rbMatter.isChecked = true
                client_name.setText(R.string.matter)
                custom_client?.text = ""
                custom_matter?.text = ""
                client_id = ""
                clear_icon?.visibility = GONE
                dropdown_icon?.visibility = VISIBLE

                matter_id = ""
                clear_icon2?.visibility = GONE
                dropdown_icon2?.visibility = VISIBLE
                view_docs_list.clear()
                rv_documents_email?.clearFocus()
                ll_attach_grp?.visibility = GONE
                rv_documents_email?.visibility = GONE
                groupsList.clear()
                client_list_view?.visibility = GONE
                view_docs_list.clear()
                rv_documents_email?.clearFocus()
                ll_attach_grp?.visibility = GONE
                rv_documents_email?.visibility = GONE
                compose_client_name.background = requireContext().resources.getDrawable(R.drawable.button_left_green_background)
                compose_client_name.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                compose_firm_name.background = requireContext().resources.getDrawable(R.drawable.button_right_background)
                compose_firm_name.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                clear_icon?.visibility = GONE
                if (Constants.CATEGORY != "solo") {
                    compose_firm_name.visibility = VISIBLE
                } else {
                    compose_client_name.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    compose_client_name.background = requireContext().resources.getDrawable(R.drawable.rectangular_button_green_count)
                    compose_firm_name.visibility = GONE
                }
                btn_create?.setAlpha(0.5f)
                btn_create?.setEnabled(false)
                callLegalMatter()
            }
            compose_firm_name.setOnClickListener {
                selectionGroups.visibility = GONE
                CATEGORY_TAG = "firm"
                ll_client_name_layout.visibility = GONE
                ll_matter_view?.visibility = GONE
                ll_select_groups_layout.visibility = VISIBLE
                view_docs_list.clear()
                groupsList.clear()
                clientsList.clear()
                rv_documents_email?.visibility = GONE
                tv_select_groups?.visibility = VISIBLE
                client_list_view?.visibility = GONE
                rv_display_upload_groups_docs?.visibility = GONE
                linearLayout2?.visibility = GONE
                ischecked_group = true
                selected_groups_list.clear()
                tv_select_groups?.text = ""
                rv_documents_email?.clearFocus()
                ll_attach_grp?.visibility = VISIBLE
                linearLayout2?.visibility = GONE
                btn_create?.setAlpha(0.5f)
                btn_create?.setEnabled(false)
                compose_firm_name.background = requireContext().resources.getDrawable(R.drawable.button_right_green_background)
                compose_firm_name.setTextColor(Color.WHITE)
                compose_client_name.background = requireContext().resources.getDrawable(R.drawable.button_left_background)
                compose_client_name.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }

            rbMatter.setOnClickListener {
                client_id = ""
                matter_id = ""
                matterName = ""
                callLegalMatter()
                ll_client_name_layout.visibility = GONE
                ll_matter_view?.visibility = VISIBLE
                client_name.setText(R.string.matter)
                custom_client?.text = ""
                client_id = ""
                clear_icon?.visibility = GONE
                dropdown_icon?.visibility = VISIBLE
                view_docs_list.clear()
                rv_documents_email?.clearFocus()
                ll_attach_grp?.visibility = GONE
                rv_documents_email?.visibility = GONE
            }
            rbClient.setOnClickListener {
                client_id = ""
                matter_id = ""
                clientname = ""
                custom_matter?.text = ""
                clear_icon2?.visibility = GONE
                dropdown_icon2?.visibility = VISIBLE
                view_docs_list.clear()
                rv_documents_email?.clearFocus()
                ll_attach_grp?.visibility = GONE
                rv_documents_email?.visibility = GONE
                ll_matter_view?.visibility = GONE
                client_name.setText(R.string.client)
                ll_client_name_layout.visibility = VISIBLE
                client_name.setText(R.string.client)
                callClientWebservice()
            }
            ll_custom_client?.setOnClickListener {
                AndroidUtils.display_listview(ischecked, client_list_view)
                ischecked = !ischecked
            }
            clear_icon?.setOnClickListener {
                client_id = ""
                view_docs_list.clear()
                matterlist.clear()
                matter_id = ""
                matterName = ""
                custom_matter?.text = ""
                matterlist.clear()
                ll_matter_view?.visibility = GONE
                rv_documents_email?.visibility = GONE
                dropdown_icon2?.visibility = VISIBLE
                clear_icon2?.visibility = GONE
                selected_client_groups_list.clear()
                ischecked_matter = true
                AndroidUtils.DisplaySpinnerView(client_list_view, custom_client, clientname, dropdown_icon, clear_icon, false)
            }
            ll_matter?.setOnClickListener {
                AndroidUtils.display_listview(ischecked_matter, list_matter)
                ischecked_matter = !ischecked_matter
            }
            clear_icon2?.setOnClickListener {
                AndroidUtils.DisplaySpinnerView(list_matter, custom_matter, matterName, dropdown_icon2, clear_icon2, false)
                matter_id = ""
            }
            tv_select_groups?.setOnClickListener {
                if (ischecked_group) {
                    if (groupsList.isEmpty()) {
                        callGroupsWebservice()
                    } else {
                        GroupsPopup(groupsList, selected_groups_list, rv_display_upload_groups_docs!!, tv_select_groups!!)
                    }
                } else {
                    callfilter_client_webservices()
                    rv_display_upload_groups_docs?.visibility = GONE
                }
                ischecked_group = !ischecked_group
            }
            btn_cancel_save?.setOnClickListener {
                attachmentDialog.dismiss()
            }

            btn_create?.setOnClickListener {
                if (!Constants.composAttachDocAry.isEmpty()) {
                    Log.e("SelectedDocument:", "" + Constants.composAttachDocAry[Constants.composAttachDocAry.size - 1].name)
                    attachmentDialog.dismiss()
                    if (Constants.composAttachDocAry.size > 4) {
                        val high = Constants.composAttachDocAry.size / 2
                        val layoutParams = yourGridView?.layoutParams
                        if (layoutParams != null) {
                            layoutParams.height = layoutParams.height + (high * 40)
                            yourGridView?.layoutParams = layoutParams
                        }
                    }
                    yourGridView?.visibility = VISIBLE
                    val griddocument = Griddocument(requireContext(), Constants.composAttachDocAry, selectedDocumentName)
                    yourGridView?.adapter = griddocument
                    validateEmail()
                }
            }

            client_list_view?.setOnItemClickListener { parent, view, position, id ->
                client_id = clientsList[position].id ?: ""
                clientname = clientsList[position].name ?: ""
                Log.d("Client_value_name", clientname)
                callfilter_client_webservices()
                ischecked = true
                AndroidUtils.DisplaySpinnerView(client_list_view, custom_client, clientname, dropdown_icon, clear_icon, true)
            }
        }

        cross_icon.setOnClickListener {
            composeDialog?.dismiss()
            overlay?.visibility = GONE
        }
    }

    private fun validateEmail() {
        if (AndroidUtils.isValidEmail(to_input?.text.toString())) {
            sends_button?.setAlpha(1.0f)
            sends_button?.setEnabled(true)
        } else {
            sends_button?.setAlpha(0.5f)
            sends_button?.setEnabled(false)
        }
    }

    fun view_document(doc_id: String) {
        try {
            doc_count++
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            val baseUrl = "https://api.staging.digicoffer.com/professional/v3/document/"
            val url = baseUrl + doc_id + "/view"
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/document/$doc_id/view",
                "view_document",
                jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    fun send_email() {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())

            val emailJson = JSONObject()
            val emailArray = to_input?.text.toString().split(",")
            val emailJsonArray = JSONArray()
            for (email in emailArray) {
                val trimmedEmail = email.trim()
                if (!trimmedEmail.isEmpty()) {
                    emailJsonArray.put(trimmedEmail)
                }
            }
            emailJson.put("toEmail", emailJsonArray)
            if (!subject_input?.text.toString().isEmpty()) {
                emailJson.put("subject", subject_input?.text.toString())
            } else {
                emailJson.put("subject", "(no Subject)")
            }
            emailJson.put("body", message_inputs?.text.toString())

            val documentsArray = JSONArray()
            try {
                for (i in 0 until Constants.composAttachDocAry.size) {
                    val documentObject = JSONObject()
                    documentObject.put("filename", Constants.composAttachDocAry[i].name)
                    documentObject.put("id", Constants.composAttachDocAry[i].id)
                    documentObject.put("name", Constants.composAttachDocAry[i].name)
                    documentObject.put("path", Constants.composAttachDocAry[i].url)
                    documentsArray.put(documentObject)
                }
            } catch (e: JSONException) {
                e.fillInStackTrace()
            }
            emailJson.put("documents", documentsArray)

            var url = ""
            if (Constants.isGmail) {
                url = Constants.EMAIL_BASE_URL + Constants.sending_mail
            } else {
                url = Constants.EMAIL_GET_URL + Constants.sending_mail
            }
            val emailUrl = url + Constants.TOKEN
            WebServiceHelper.callEmailHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.POST,
                emailUrl,
                "sending_email",
                emailJson.toString()
            )
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    fun callfilter_client_webservices() {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            if (CATEGORY_TAG == "client") {
                jsonObject.put("category", "client")
                jsonObject.put("clients", client_id)
                if (!matter_id.isEmpty()) jsonObject.put("matters", matter_id)
                jsonObject.put("showPdfDocs", false)
                jsonObject.put("groups", null)
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.PUT,
                    "v3/document/filter",
                    "Display clientDocuments",
                    jsonObject.toString()
                )
                Log.d("Group_doc_view1", jsonObject.toString())
            } else if (CATEGORY_TAG == "firm") {
                val value = Array(selected_groups_list.size) { "" }
                val value_id = Array(selected_groups_list.size) { "" }
                for (i in 0 until selected_groups_list.size) {
                    value[i] = selected_groups_list[i].group_name ?: ""
                    value_id[i] = selected_groups_list[i].group_id ?: ""
                }

                array_group = JSONArray(value_id)
                jsonObject.put("category", "firm")
                jsonObject.put("clients", "")
                jsonObject.put("matters", "")
                jsonObject.put("showPdfDocs", false)
                jsonObject.put("groups", array_group)
                Log.d("Group_value_num", array_group.toString())

                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.PUT,
                    "v3/document/filter",
                    "Display firmDocuments",
                    jsonObject.toString()
                )
                Log.d("Group_doc_view1", jsonObject.toString())
            }
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun load_view_doc(docs: JSONArray) {
        try {
            view_docs_list.clear()
            for (i in 0 until docs.length()) {
                val viewDocumentsModel = ViewDocumentsModel()
                val jsonObject = docs.getJSONObject(i)

                viewDocumentsModel.created = jsonObject.getString("created")
                viewDocumentsModel.content_type = jsonObject.getString("content_type")
                viewDocumentsModel.description = jsonObject.getString("description")
                viewDocumentsModel.expiration_date = jsonObject.getString("expiration_date")
                viewDocumentsModel.filename = jsonObject.getString("filename")
                viewDocumentsModel.id = jsonObject.getString("id")
                viewDocumentsModel.is_disabled = jsonObject.getBoolean("is_disabled")
                viewDocumentsModel.is_encrypted = jsonObject.getBoolean("is_encrypted")
                viewDocumentsModel.is_password = jsonObject.getBoolean("is_password")
                viewDocumentsModel.name = jsonObject.getString("name")
                viewDocumentsModel.uploaded_by = jsonObject.getString("uploaded_by")

                var isAlreadyAttached = false
                val count = Constants.composAttachDocAry?.size ?: 0
                for (k in 0 until count) {
                    if (Constants.composAttachDocAry[k].id == viewDocumentsModel.id) {
                        isAlreadyAttached = true
                        break
                    }
                }

                viewDocumentsModel.setIsChecked(isAlreadyAttached)
                view_docs_list.add(viewDocumentsModel)

                Log.d("VIEW_POSITION", view_docs_list[i].toString())
            }
            loadViewDocumentsRecyclerview()
        } catch (e: JSONException) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, requireActivity())
        }
    }

    private fun loadViewDocumentsRecyclerview() {
        try {
            if (view_docs_list.isEmpty()) {
                rv_documents_email?.removeAllViews()
                rv_documents_email?.visibility = GONE
            } else {
                rv_documents_email?.setHasFixedSize(false)
                rv_documents_email?.visibility = VISIBLE

                val adapterInstance = view_document_emailadapter(view_docs_list, requireContext(), requireActivity(), btn_create!!)
                AndroidUtils.LoadingRecyclerview(rv_documents_email, requireContext())
                rv_documents_email?.adapter = adapterInstance
                rv_documents_email?.clearFocus()

                et_Search_email_document?.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable) {
                        adapterInstance.getFilter().filter(s.toString())
                    }
                })
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun loadRowDatas(jsonObject: JSONObject) {
        val messagesArray = jsonObject.getJSONArray("messages")
        val messagesList = ArrayList<MessageModel>()
        for (i in 0 until messagesArray.length()) {
            val messageObject = messagesArray.getJSONObject(i)
            val message = MessageModel()
            message.setMsgId(messageObject.getString("msgId"))
            message.setSubject(messageObject.getString("subject"))
            message.from = messageObject.getString("from")
            message.setTo(messageObject.getString("to"))

            val attachmentsArray = messageObject.getJSONArray("attachments")
            val attachmentsList = ArrayList<AttachmentModel>()
            for (j in 0 until attachmentsArray.length()) {
                val attachmentObject = attachmentsArray.getJSONObject(j)
                val attachment = AttachmentModel()
                if (!attachmentObject.getString("filename").isEmpty() && !attachmentObject.getString("partId").isEmpty()) {
                    Log.e("Email_Res", "File-" + attachmentObject.getString("filename") + ": Partid-" + attachmentObject.getString("partId"))
                    attachment.setPartId(attachmentObject.getString("partId"))
                    attachment.setMimeType(attachmentObject.getString("mimeType"))
                    attachment.setFilename(attachmentObject.getString("filename"))
                    val headersArray = attachmentObject.getJSONArray("headers")
                    val headersList = ArrayList<Header>()
                    for (k in 0 until headersArray.length()) {
                        val headerObject = headersArray.getJSONObject(k)
                        val header = Header()
                        header.name = headerObject.getString("name")
                        header.value = headerObject.getString("value")
                        headersList.add(header)
                    }
                    attachment.setHeaders(headersList)

                    val bodyObject = attachmentObject.getJSONObject("body")
                    val bodyModel = Body()
                    bodyModel.size = bodyObject.getInt("size")
                    attachment.setBody(bodyModel)

                    attachmentsList.add(attachment)
                    message.setAttachments(attachmentsList)
                }
            }
            messagesList.add(message)
        }
        totalMessageArray.add(pre_next_position, messagesList)
        nextPageToken = jsonObject.optString("nextPageToken")
        Log.d("Nextpagetoken", nextPageToken)
        if (messagesList.size > 0) {
            updateRecyclerView(messagesList)
        }
    }

    private fun loadRowDatasFromGraph(jsonObject: JSONObject) {
        val messagesArray = jsonObject.getJSONArray("value")

        messages.clear()
        progress_dialog = AndroidUtils.get_progress(requireActivity())

        for (i in 0 until messagesArray.length()) {
            val messageObject = messagesArray.getJSONObject(i)
            val message = MessageModel()

            message.setMsgId(messageObject.optString("id"))
            message.setSubject(messageObject.optString("subject"))

            val fromObj = messageObject.optJSONObject("from")
            if (fromObj != null) {
                val emailAddress = fromObj.optJSONObject("emailAddress")
                if (emailAddress != null) {
                    message.from = emailAddress.optString("name")
                }
            }

            val toRecipients = messageObject.optJSONArray("toRecipients")
            if (toRecipients != null && toRecipients.length() > 0) {
                val toEmailObj = toRecipients.getJSONObject(0).optJSONObject("emailAddress")
                if (toEmailObj != null) {
                    message.setTo(toEmailObj.optString("name"))
                }
            }

            messages.add(message)

            if (messageObject.optBoolean("hasAttachments", false)) {
                callMessageAttachmentList(message.getMsgId() ?: "")
            }
        }

        totalMessageArray.add(pre_next_position, messages)
        nextPageToken = jsonObject.optString("@odata.nextLink")

        updateRecyclerView(messages)
    }

    override fun onClick(view: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progress_dialog != null && progress_dialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }

        val success = httpResult.result.toString()
        Log.d("Succ", success)

        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent ?: "{}")

                if (httpResult.requestType == "Label") {
                    if (Constants.isGmail) {
                        val data = result.getJSONObject("data")
                        if (data.length() > 0) {
                            loadLabelData(data)
                            ISCHECK_EMAIL = true
                            callMessageList()
                        }
                    } else {
                        loadOutlookLabelData(result)
                        ISCHECK_EMAIL = true
                        callMessageList()
                    }
                } else if (httpResult.requestType == "messages_rows") {
                    if (Constants.isGmail) {
                        loadRowDatas(result)
                    } else {
                        loadRowDatasFromGraph(result)
                    }
                } else if (httpResult.requestType.startsWith("messages_attachment_")) {
                    val msgIdStr = httpResult.requestType.replace("messages_attachment_", "")

                    try {
                        val responseObj = result.optJSONObject("attachments")
                        val attachmentsArray = responseObj?.optJSONArray("value")

                        val attachmentsList = ArrayList<AttachmentModel>()
                        if (attachmentsArray != null) {
                            for (j in 0 until attachmentsArray.length()) {
                                val attObj = attachmentsArray.getJSONObject(j)
                                val attachment = AttachmentModel()
                                attachment.setPartId(attObj.optString("id"))
                                attachment.setFilename(attObj.optString("name"))
                                attachment.setMimeType(attObj.optString("contentType"))
                                if (attObj.has("size")) {
                                    attachment.size = attObj.optInt("size")
                                }
                                attachmentsList.add(attachment)
                            }
                        }

                        for (i in 0 until messages.size) {
                            val m = messages[i]
                            if (m.getMsgId() == msgIdStr) {
                                m.setAttachments(attachmentsList)
                                adapter?.notifyItemChanged(i)
                                break
                            }
                        }
                    } catch (e: JSONException) {
                        Log.e("Attachment Parsing", "Error: " + e.message)
                    }
                } else if (httpResult.requestType == "Display clientDocuments" || httpResult.requestType == "Display firmDocuments") {
                    val data = result.getJSONArray("data")
                    load_view_doc(data)
                } else if (httpResult.requestType == "Groups") {
                    val data = result.getJSONArray("data")
                    loadGroupsData(data, groupsList, false)
                } else if (httpResult.requestType == "Clients List") {
                    val data = result.getJSONObject("data")
                    loadClients(data)
                    callCorpClientWebservice()
                } else if (httpResult.requestType == "Legal Matter") {
                    val matters = result.getJSONArray("matters")
                    loadMatters(matters)
                } else if (httpResult.requestType == "Corp Clients List") {
                    loadCorpClients(result)
                } else if (httpResult.requestType == "view_document") {
                    val data = result.getJSONObject("data")
                    val url = data.getString("url")
                    Constants.composAttachDocAry[Constants.tempPos].url = url
                } else if (httpResult.requestType == "sending_email") {
                    composeDialog?.dismiss()
                    overlay?.visibility = GONE
                    AndroidUtils.showAlert_docs("Success!", result.getString("message"), requireActivity())
                } else if (httpResult.requestType == "auth") {
                    val url = result.getString("url")
                    ISCHECK_AUTH = true
                    emaiAPI()
                    launchAuthUrl(url)
                }
            } catch (e: JSONException) {
                e.fillInStackTrace()
            }
        } else if (httpResult.result == WebServiceHelper.ServiceCallStatus.Failed) {
            if (httpResult.requestType == "Label" || httpResult.requestType == "auth") {
                emaiAPI()
            }

            if (httpResult.requestType == "sending_email") {
                AndroidUtils.showAlert("Mail not sent successfully", requireActivity())
                composeDialog?.dismiss()
                overlay?.visibility = GONE
            }
        } else {
            AndroidUtils.showErrorAlert(
                httpResult.responseContent,
                requireActivity()
            )
        }
    }

    private fun callClientGroupsWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
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
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.PUT,
                "v3/documents/groupslist",
                "Client Groups",
                jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun callGroupsWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/groups",
                "Groups",
                jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun loadLabelData(data: JSONObject) {
        val emailModelLocal = EmailModel()
        emailModelLocal.id = data.optString("id")
        emailModelLocal.name = data.optString("name")
        emailModelLocal.type = data.optString("type")
        emailModelLocal.messagesTotal = data.optInt("messagesTotal")
        emailModelLocal.messagesUnread = data.optInt("messagesUnread")
        emailModelLocal.threadsTotal = data.optInt("threadsTotal")
        emailModelLocal.threadsUnread = data.optInt("threadsUnread")
        Log.e("Email Inbox", emailModelLocal.messagesTotal.toString())
        inbox_textViews?.text = "Inbox " + emailModelLocal.messagesTotal
        composeDocuments?.setAlpha(1.0f)
    }

    private fun loadOutlookLabelData(data: JSONObject) {
        emailModel = EmailModel()
        emailModel.id = data.optString("id")
        emailModel.name = data.optString("displayName")
        emailModel.type = data.optString("type")
        emailModel.messagesTotal = data.optInt("totalItemCount")
        emailModel.messagesUnread = data.optInt("unreadItemCount")
        emailModel.threadsTotal = data.optInt("sizeInBytes")
        emailModel.threadsUnread = data.optInt("threadsUnread")
        Log.e("Email Inbox", emailModel.messagesTotal.toString())
        inbox_textViews?.text = "Inbox " + emailModel.messagesTotal
        composeDocuments?.setAlpha(1.0f)
    }

    private fun launchAuthUrl(authUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(authUrl)
        startActivityForResult(intent, AUTH_REQUEST_CODE)
        ischeck_auth = true
    }

    fun emaiauth() {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            if (Constants.isGmail) {
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.GET,
                    Constants.EMAIL_BASE_URL + "gmail/authurl?authtoken=" + Constants.TOKEN,
                    "auth",
                    jsonObject.toString()
                )
            } else {
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.GET,
                    Constants.EMAIL_BASE_URL + "authurl?authtoken=" + Constants.TOKEN,
                    "auth",
                    jsonObject.toString()
                )
            }
            Log.d("C12Token", Constants.EMAIL_BASE_URL + Constants.isGoogle + "/authurl?authtoken=" + Constants.TOKEN)
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            e.fillInStackTrace()
        }
    }

    fun callMessageList() {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()

            if (Constants.isGmail) {
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.GET,
                    Constants.EMAIL_BASE_URL + Constants.gmail_messages + Constants.TOKEN + "?rows=10",
                    "messages_rows",
                    jsonObject.toString()
                )
            } else {
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.GET,
                    Constants.EMAIL_GET_URL + "messages/" + Constants.TOKEN + "?labelid=" + emailModel.id + "&rows=10",
                    "messages_rows",
                    jsonObject.toString()
                )
            }
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            e.fillInStackTrace()
            Log.e("API Error", "Failed to call API: " + e.message)
        }
    }

    fun callMessageAttachmentList(msgId: String) {
        try {
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                Constants.EMAIL_GET_URL + "message/detail/" + Constants.TOKEN + "/" + msgId,
                "messages_attachment_$msgId",
                jsonObject.toString()
            )
        } catch (e: Exception) {
            Log.e("API Error", "Failed to call API: " + e.message)
        }
    }

    private fun parseAndAssignAttachments(msgId: String, response: String) {
        try {
            val jsonObject = JSONObject(response)
            val attachmentsObj = jsonObject.getJSONObject("attachments")
            val valuesArray = attachmentsObj.getJSONArray("value")

            val attachmentList = ArrayList<AttachmentModel>()

            for (i in 0 until valuesArray.length()) {
                val attach = valuesArray.getJSONObject(i)
                val attachment = AttachmentModel()
                attachment.setPartId(attach.getString("id"))
                attachment.setFilename(attach.getString("name"))
                attachment.contentType = attach.getString("contentType")
                attachment.size = attach.getInt("size")

                attachmentList.add(attachment)
            }

            for (message in messages) {
                if (message.getMsgId() == msgId) {
                    message.setAttachments(attachmentList)
                    break
                }
            }

            adapter?.notifyDataSetChanged()
        } catch (e: JSONException) {
            Log.e("Parse Error", "Failed to parse attachments: " + e.message)
        }
    }

    private fun loadClients(data: JSONObject) {
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

    private fun loadCorpClients(data: JSONObject) {
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
        if (!clientsList.isEmpty()) {
            initUI(clientsList)
        } else {
            client_list_view?.visibility = GONE
            ischecked = true
        }
    }

    private fun initUI(clientsList: ArrayList<ClientsModel>) {
        val adapterLocal = CommonSpinnerAdapter(requireActivity(), clientsList)
        client_list_view?.adapter = adapterLocal
        AndroidUtils.LoadList(client_list_view, requireContext(), clientsList.size, true)
    }

    private fun callLegalMatter() {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v2/matter/list",
                "Legal Matter",
                jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            e.fillInStackTrace()
        }
    }

    private fun loadMatters(matters: JSONArray) {
        matterlist.clear()
        for (i in 0 until matters.length()) {
            val jsonObject = matters.getJSONObject(i)
            val mattersModel = MattersModel()
            mattersModel.id = jsonObject.optString("id")
            mattersModel.title = jsonObject.optString("title")
            mattersModel.type = jsonObject.optString("type")
            matterlist.add(mattersModel)
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
        val adapterLocal = CommonSpinnerAdapter(requireActivity(), matterlist)
        list_matter?.adapter = adapterLocal
        AndroidUtils.LoadList(list_matter, requireContext(), matterlist.size, true)
        list_matter?.setOnItemClickListener { parent, view, position, id ->
            matter_id = ""
            matter_id = matterlist[position].id ?: ""
            matterName = matterlist[position].title ?: ""
            Log.d("Matter_value_name", matterName)
            callfilter_client_webservices()
            AndroidUtils.DisplaySpinnerView(list_matter, custom_matter, matterName, dropdown_icon2, clear_icon2, true)
            ischecked_matter = true
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
            selectedLanguage = BooleanArray(groupsList.size)
            GroupsPopup(groupsList, selected_groups_list, rv_display_upload_groups_docs!!, tv_select_groups!!)
        } catch (e: JSONException) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, requireActivity())
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun GroupsPopup(
        groupsList: ArrayList<DocumentsModel>,
        selected_groups_list: ArrayList<DocumentsModel>,
        rv_display_upload_groups_docs: RecyclerView,
        tv_select_groups: TextView
    ) {
        tv_select_groups.text = ""
        try {
            for (i in 0 until groupsList.size) {
                for (j in 0 until selected_groups_list.size) {
                    if (groupsList[i].group_id == selected_groups_list[j].group_id) {
                        val documentsModel = groupsList[i]
                        documentsModel.isGroupChecked = true
                    }
                }
            }

            val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            rv_display_upload_groups_docs.layoutManager = layoutManager
            rv_display_upload_groups_docs.visibility = VISIBLE
            val documentsAdapter = GroupsListAdapter(
                groupsList,
                Documents::class.java.newInstance(),
                GroupsListAdapter.OnCheckedChangeListener { documentsModel ->
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
                }
            )
            val value = Array(selected_groups_list.size) { "" }
            for (i in 0 until selected_groups_list.size) {
                value[i] = selected_groups_list[i].group_name ?: ""
            }
            val str = TextUtils.join(",", value)
            tv_select_groups.text = str
            rv_display_upload_groups_docs.adapter = documentsAdapter
            AndroidUtils.LoadList(rv_display_upload_groups_docs, requireContext(), groupsList.size, true)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun callMessageListnext() {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                Constants.EMAIL_BASE_URL + Constants.gmail_messages + Constants.TOKEN + "?rows=10&nextpagetoken=" + nextPageToken,
                "messages_rows",
                jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            e.fillInStackTrace()
            Log.e("API Error", "Failed to call API: " + e.message)
        }
    }

    fun callLabel() {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            if (Constants.isGmail) {
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.GET,
                    Constants.EMAIL_BASE_URL + "gmail/label/" + Constants.TOKEN + "?labelid=INBOX",
                    "Label",
                    jsonObject.toString()
                )
            } else {
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.GET,
                    Constants.EMAIL_GET_URL + "label/" + Constants.TOKEN + "?labelid=INBOX",
                    "Label",
                    jsonObject.toString()
                )
            }
            Log.d("Label_value", Constants.EMAIL_BASE_URL + "gmail/label/" + Constants.TOKEN + "?labelid=INBOX")
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            e.fillInStackTrace()
        }
    }

    fun emaiAPI() {
        if (!ISCHECK_AUTH) {
            emaiauth()
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                if (!ISCHECK_EMAIL) {
                    callLabel()
                }
            }, 10000)
        }
    }

    private fun callClientWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/client/all/list",
                "Clients List",
                jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun callCorpClientWebservice() {
        try {
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/corporate/list",
                "Corp Clients List",
                jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun updateRecyclerView(messagesList: List<MessageModel>) {
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.recyclerView)
        adapter = EmailAdapter(messagesList, this, requireActivity())
        recyclerView.adapter = adapter
        AndroidUtils.LoadingRecyclerview(recyclerView, requireContext())
        search_email?.setOnClickListener {
            adapter?.filter?.filter(et_Search?.text.toString())
        }
        et_Search?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                search_email?.setAlpha(0.3f)
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length == 0) {
                    clear_search?.visibility = GONE
                    search_email?.setAlpha(0.3f)
                } else {
                    clear_search?.visibility = VISIBLE
                    search_email?.setAlpha(1.0f)
                }
            }
        })
        clear_search?.setOnClickListener {
            et_Search?.setText("")
            search_email?.setAlpha(0.3f)
            adapter?.filter?.filter(et_Search?.text.toString())
        }
    }

    companion object {
        private const val AUTH_REQUEST_CODE = 1001
        private const val VIEW_TYPE_DOCUMENT = 0
        private const val VIEW_TYPE_ATTACHMENT = 1

        @JvmField
        var progress_dialog: Dialog? = null
    }
}
