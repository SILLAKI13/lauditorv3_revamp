package com.digicoffer.lauditor.Matter.OldViewModels

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digicoffer.lauditor.CommonFiles.FileSelection.BottomSheetUploadFile
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.CommonFiles.PdfUtils.File_Content_Type
import com.digicoffer.lauditor.CommonFiles.PdfUtils.RetrievePDFfromUrl
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.digicoffer.lauditor.Documents.DocumentsListAdpater.DocumentsListAdapter
import com.digicoffer.lauditor.Matter.Adapters.DocumentsAdapter
import com.digicoffer.lauditor.Matter.Adapters.ViewMatterAdapter
import com.digicoffer.lauditor.Matter.Models.AdvocateModel
import com.digicoffer.lauditor.Matter.Models.ClientsModel
import com.digicoffer.lauditor.Matter.Models.DocumentsModel
import com.digicoffer.lauditor.Matter.Models.GroupsModel
import com.digicoffer.lauditor.Matter.Models.MatterModel
import com.digicoffer.lauditor.Matter.Models.TeamModel
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.Matter.ViewModels.Matter
import com.digicoffer.lauditor.Matter.ViewModels.ViewMatter
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.AbstractCollection
import java.util.ArrayList
import java.util.Calendar
import java.util.HashSet
import java.util.Iterator
import java.util.Locale
import java.util.Objects

class MatterDocuments : Fragment(), AsyncTaskCompleteListener, DocumentsListAdapter.EventListener, View.OnClickListener, BottomSheetUploadFile.OnPhotoSelectedListner {

    private var tv_selected_document: TextView? = null
    private var tv_tag_document_name: TextView? = null
    private var matter_date: TextView? = null
    private var tv_document_library: TextView? = null
    private var tv_device_drive: TextView? = null
    private var at_add_documents: TextView? = null
    private var tv_selected_file: TextView? = null
    private var add_groups: TextView? = null
    private var select_all: TextView? = null
    private var tv_enable_download: TextView? = null
    private var tv_disable_download: TextView? = null
    private var tv_multiple_doc: TextView? = null
    private var ll_added_tags: LinearLayout? = null
    private var ll_add_documents: LinearLayout? = null
    private var ll_selected_documents: LinearLayout? = null
    private var ll_select_doc: LinearLayout? = null
    private var ll_matterDate: LinearLayout? = null
    private var ll_uploaded_documents: RecyclerView? = null
    private var btn_browse: Button? = null
    private var btn_add_documents: Button? = null
    var rv_matter_list: RecyclerView? = null
    var dialog: AlertDialog? = null
    var rl_buttons: RelativeLayout? = null
    var subtag: String = ""
    var is_clicked_add: Boolean = true
    var is_clicked_edit: Boolean = true
    var adapter: DocumentsListAdapter? = null
    var chk_box_layout: LinearLayout? = null
    var chk_select_all: CheckBox? = null
    private var totalUploads: Int = 0
    private var completedUploads: Int = 0
    var isselect_all_checked: Boolean = true
    private var isAlertShown: Boolean = false
    var isDocUploaded: Boolean = false
    var matterModel: MatterModel = MatterModel()
    var filters: Array<InputFilter>? = null
    var filters1: Array<InputFilter>? = null
    var DOWNLOAD_TAG: Boolean = false
    var isedit: Boolean = false
    var ENCRYPTION_TAG: Boolean = true
    var DECRYPTION_TAG: Boolean = true
    var progressDialog: Dialog? = null
    var matter_id: String = ""
    var edit_position: Int = 0
    var sharedDocumentsDo: DocumentsModel = DocumentsModel()
    private var mViewModel: NewModel? = null
    var matter_type: String = ""
    var documentsAdapter: DocumentsAdapter? = null
    var cv_client_details: CardView? = null
    var cv_add_opponent_advocate: CardView? = null
    var matter: Matter? = null
    var matterList: ArrayList<ViewMatterModel> = ArrayList()
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
    private var existing_opponents: JSONArray? = null
    var tv_tag_type: TextInputEditText? = null
    var tv_tag_name: TextInputEditText? = null
    var search_matter: TextInputLayout? = null
    var tag_type_name: TextView? = null
    var tag_name: TextView? = null
    var header_name: TextView? = null
    var advocates_list: ArrayList<AdvocateModel> = ArrayList()
    private var imageView: ImageView? = null
    var selected_tm_list: ArrayList<TeamModel> = ArrayList()
    var ll_download: LinearLayoutCompat? = null
    var rv_display_upload_doc: RecyclerView? = null
    var selected_clients_list: ArrayList<ClientsModel> = ArrayList()
    var selected_temp_clients_list: ArrayList<ClientsModel> = ArrayList()
    var selected_corp_clients_list: ArrayList<ClientsModel> = ArrayList()
    var tags_list: ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel> = ArrayList()
    var bottommSheetUploadDocument: BottomSheetUploadFile? = null
    var btn_cancel_save: AppCompatButton? = null
    var btn_create: AppCompatButton? = null
    var btn_add_tags: AppCompatButton? = null
    var selectedDocument: BooleanArray? = null
    var corp_client_id: String = ""
    private var mSelectedBitmap: Bitmap? = null
    var cl_matter_document: ConstraintLayout? = null
    var matterArraylist: ArrayList<MatterModel>? = null
    private var mSelectedUri: File? = null
    var ADAPTER_TAG: String = "Documents"
    var documentsList: ArrayList<DocumentsModel> = ArrayList()
    var selected_documents_list: ArrayList<DocumentsModel> = ArrayList()
    var selected_upload_documents_list: ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel> = ArrayList()
    var tempSelectedDocuments: ArrayList<DocumentsModel> = ArrayList()
    var new_selected_doc: ArrayList<DocumentsModel> = ArrayList()
    var upload_documents_list: ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel> = ArrayList()
    var exisiting_group_acls: JSONArray? = null
    var existing_documents: JSONArray? = null
    var existing_corp_clients: JSONArray? = null
    var existing_temp_clients: JSONArray? = null
    var existing_documents_list: JSONArray? = null
    var et_search_matter: TextInputEditText? = null
    var ischecked_doc: Boolean = true
    var existing_clients: JSONArray? = null
    var existing_members: JSONArray? = null
    var existing_groups_list: JSONArray? = null
    var existing_clients_list: JSONArray? = null
    var existing_tm_list: JSONArray? = null
    var selected_groups_list: ArrayList<GroupsModel> = ArrayList()
    private var progress_dialog: Dialog? = null
    var filename: String? = null
    var matter_title_tv: TextView? = null
    var tv_enable_encryption: TextView? = null
    var tv_disable_encryption: TextView? = null
    var tv_add_tag: TextView? = null
    var tv_edit_meta: TextView? = null
    var ll_upload_type: LinearLayoutCompat? = null
    var file: File? = null
    var isUpdateTag: Boolean = false
    var viewMatterModel1: ViewMatterModel? = null
    var viewmatter: ViewMatter? = null
    private var groupsList: ArrayList<GroupsModel> = ArrayList()
    private var clientsList: ArrayList<ClientsModel> = ArrayList()
    private var tmList: ArrayList<TeamModel> = ArrayList()
    private var uploaded_document_name: String? = null
    private var upload_description: String? = null
    private var upload_exp_date: String? = null
    private var MergedList: AbstractCollection<DocumentsModel>? = null
    private var changedCollection: Int = 0
    private var tag_list: ArrayList<String> = ArrayList()
    private var existing_tags_list: JSONArray? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.documents_matter, container, false)
        val myCalendar = Calendar.getInstance()
        ll_matterDate = view.findViewById(R.id.ll_matterDate)
        ll_matterDate?.visibility = View.GONE
        ll_upload_type = view.findViewById(R.id.ll_upload_type)
        ll_download = view.findViewById(R.id.ll_download)
        tv_multiple_doc = view.findViewById(R.id.tv_multiple_doc)
        tv_multiple_doc?.setText(R.string.upload_multiple_document_s)
        tv_selected_document = view.findViewById(R.id.tv_selected_document)
        tv_selected_document?.setText(R.string.selected_documents)
        
        rl_buttons = view.findViewById(R.id.rl_buttons)
        tv_edit_meta = view.findViewById(R.id.tv_edit_meta)
        tv_add_tag = view.findViewById(R.id.tv_add_tag)
        tv_add_tag?.setText(R.string.add_tag)
        tv_edit_meta = view.findViewById(R.id.tv_edit_meta)
        tv_edit_meta?.setText(R.string.edit_meta)
        btn_add_tags = view.findViewById(R.id.btn_add_tag)
        btn_add_tags?.setText(R.string.add_tag)
        btn_add_tags?.visibility = View.GONE
        tv_enable_download = view.findViewById(R.id.tv_enable_download)
        tv_enable_download?.setText(R.string.enable_download)
        tv_enable_download?.textSize = 13f
        tv_disable_download = view.findViewById(R.id.tv_disable_download)
        tv_disable_download?.setText(R.string.disable_download)
        tv_disable_download?.textSize = 13f
        tv_enable_encryption = view.findViewById(R.id.tv_enable_encryption)
        tv_enable_encryption?.setText(R.string.enable_encryption)
        tv_enable_encryption?.textSize = 13f
        tv_disable_encryption = view.findViewById(R.id.tv_disable_encryption)
        tv_disable_encryption?.setText(R.string.disable_encryption)
        tv_disable_encryption?.textSize = 13f
        tv_enable_download?.setPadding(20, 20, 20, 20)
        tv_disable_download?.setPadding(20, 20, 20, 20)
        tv_enable_encryption?.setPadding(20, 20, 20, 20)
        tv_disable_encryption?.setPadding(20, 20, 20, 20)
        matter_date = view.findViewById(R.id.matter_date)
        chk_box_layout = view.findViewById(R.id.chk_box_layout)
        chk_box_layout?.alpha = 0.5f
        chk_select_all = view.findViewById(R.id.chk_select_all)
        chk_select_all?.background?.alpha = 50
        chk_select_all?.isEnabled = false
        cl_matter_document = view.findViewById(R.id.cl_matter_document)
        tv_document_library = view.findViewById(R.id.tv_document_library)
        tv_document_library?.setText(R.string.document_library)
        tv_document_library?.setOnClickListener(this)
        et_search_matter = view.findViewById(R.id.et_Search)
        cv_client_details = view.findViewById(R.id.cv_client_details)
        cv_add_opponent_advocate = view.findViewById(R.id.cv_add_opponent_advocate)
        tv_device_drive = view.findViewById(R.id.tv_device_drive)
        tv_device_drive?.setText(R.string.device_drive)
        add_groups = view.findViewById(R.id.add_groups)
        add_groups?.setText(R.string.add_documents)
        select_all = view.findViewById(R.id.select_all)
        select_all?.setText(R.string.select_document)
        rv_matter_list = view.findViewById(R.id.rv_matter_list)
        tv_device_drive?.setOnClickListener(this)
        at_add_documents = view.findViewById(R.id.at_add_documents)
        at_add_documents?.setHint(R.string.select_document)
        at_add_documents?.setOnClickListener(this)
        tv_selected_file = view.findViewById(R.id.tv_selected_file)
        tv_selected_file?.setHint(R.string.select_document)
        tv_selected_file?.setHintTextColor(requireContext().getColor(R.color.grey_color_dark))
        tv_selected_file?.gravity = Gravity.CENTER_VERTICAL
        tv_selected_file?.text = ""
        tv_selected_file?.background = context?.resources?.getDrawable(R.drawable.rectangle_light_grey_bg, null)
        tv_selected_file?.setPadding(20, 0, 0, 0)
        ll_add_documents = view.findViewById(R.id.ll_add_documents)
        ll_selected_documents = view.findViewById(R.id.ll_selected_documents)
        ll_uploaded_documents = view.findViewById(R.id.ll_uploaded_documents)
        ll_select_doc = view.findViewById(R.id.ll_select_doc)
        btn_browse = view.findViewById(R.id.btn_browse)
        btn_browse?.setText(R.string.browse_small)
        btn_browse?.setOnClickListener(this)
        btn_add_documents = view.findViewById(R.id.btn_add_documents)
        btn_cancel_save = view.findViewById(R.id.btn_cancel_save)
        btn_cancel_save?.setOnClickListener(this)
        btn_create = view.findViewById(R.id.btn_submit)
        btn_create?.setText(R.string.submit)
        matter_title_tv = view.findViewById(R.id.matter_title)
        matter_title_tv?.textSize = DynamicUtils.twenty.toFloat()
        rv_display_upload_doc = view.findViewById(R.id.rv_display_upload_doc)
        rv_display_upload_doc?.background = context?.getDrawable(R.drawable.rectangle_light_grey_bg)
        btn_create?.setOnClickListener(this)
        rv_display_upload_doc?.visibility = View.GONE
        btn_create?.setOnClickListener {
            if (Constants.create_matter) {
                submitMatter()
            } else {
                try {
                    if (isDocUploaded) {
                        update_document()
                    } else {
                        if (upload_documents_list.isNotEmpty()) {
                            submitMatterInformation()
                        } else {
                            update_document()
                        }
                    }
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
            }
        }
        at_add_documents?.setOnClickListener {
            try {
                if (ischecked_doc) {
                    if (documentsList.isEmpty()) {
                        if (!Constants.create_matter)
                            document_list()
                        else
                            DocumentsPopUp()
                    } else {
                        DocumentsPopUp()
                    }
                } else {
                    rv_display_upload_doc?.visibility = View.GONE
                }
            } catch (e: JSONException) {
                throw RuntimeException(e)
            }
            ischecked_doc = !ischecked_doc
        }
        loadDocumentLibraryUI()
        matter = parentFragment as Matter?
        matterArraylist = matter?.matter_arraylist
        if (Constants.create_matter) {
            if (matterArraylist != null && matterArraylist!!.isNotEmpty()) {
                for (i in matterArraylist!!.indices) {
                    matterModel = matterArraylist!![i]
                    matter_title_tv?.text = matterModel.matter_title
                    if (matterModel.clients_list != null && matterModel.clients != null && matterModel.groups_list != null && matterModel.group_acls != null) {
                        exisiting_group_acls = matterModel.group_acls
                        existing_clients = matterModel.clients
                        existing_corp_clients = matterModel.corp_clients_list
                        existing_groups_list = matterModel.groups_list
                        existing_clients_list = matterModel.clients_list
                        existing_temp_clients = matterModel.temp_clients_list
                        if (matterModel.documents != null) {
                            existing_documents = matterModel.documents
                        }
                        if (matterModel.documents_list != null) {
                            existing_documents_list = matterModel.documents_list
                        }
                        try {
                            val groupAcls = exisiting_group_acls
                            if (groupAcls != null) {
                                for (g in 0 until groupAcls.length()) {
                                    val groupsModel = GroupsModel()
                                    val jsonObject = groupAcls.optJSONObject(g)
                                    if (jsonObject != null) {
                                        groupsModel.group_id = jsonObject.optString("id")
                                        groupsModel.group_name = jsonObject.optString("name")
                                        groupsModel.isChecked = jsonObject.optBoolean("isChecked")
                                        selected_groups_list.add(groupsModel)
                                    }
                                }
                            }
                            val docs = existing_documents
                            if (docs != null) {
                                for (d in 0 until docs.length()) {
                                    val documentsModel = DocumentsModel()
                                    val jsonObject = docs.optJSONObject(d)
                                    if (jsonObject != null) {
                                        documentsModel.docid = jsonObject.optString("docid")
                                        documentsModel.name = jsonObject.optString("name")
                                        documentsModel.user_id = jsonObject.optString("user_id")
                                        documentsModel.doctype = jsonObject.optString("doctype")
                                        documentsModel.tags_list = jsonObject.optJSONObject("tags") ?: JSONObject()
                                        documentsModel.contentType = jsonObject.optString("contentType")
                                        documentsModel.viewUrl = jsonObject.optString("viewUrl")
                                        documentsModel.is_encrypted = jsonObject.optBoolean("is_encrypted")
                                        documentsModel.is_password = jsonObject.optBoolean("is_password")
                                        documentsModel.isAdded_encryption = jsonObject.optBoolean("added_encryption")
                                        selected_documents_list.add(documentsModel)
                                        tempSelectedDocuments.add(documentsModel)
                                    }
                                }
                            }
                            val tempClients = existing_temp_clients
                            if (tempClients != null && tempClients.length() > 0) {
                                for (m in 0 until tempClients.length()) {
                                    val clientsModel = ClientsModel()
                                    val jsonObject = tempClients.optJSONObject(m)
                                    if (jsonObject != null) {
                                        clientsModel.client_id = jsonObject.optString("id")
                                        clientsModel.client_name = jsonObject.optString("name")
                                        clientsModel.rel_id = jsonObject.optString("rel_id")
                                        clientsModel.client_type = jsonObject.optString("type")
                                        selected_temp_clients_list.add(clientsModel)
                                    }
                                }
                            }
                            val corpClients = existing_corp_clients
                            if (corpClients != null) {
                                for (m in 0 until corpClients.length()) {
                                    val clientsModel = ClientsModel()
                                    val jsonObject = corpClients.optJSONObject(m)
                                    if (jsonObject != null) {
                                        clientsModel.client_id = jsonObject.optString("id")
                                        clientsModel.client_name = jsonObject.optString("name")
                                        clientsModel.client_type = jsonObject.optString("type")
                                        selected_corp_clients_list.add(clientsModel)
                                    }
                                }
                            }
                            val docsList = existing_documents_list
                            if (docsList != null) {
                                documentsList.clear()
                                for (ed in 0 until docsList.length()) {
                                    val documentsModel = DocumentsModel()
                                    val jsonObject = docsList.optJSONObject(ed)
                                    if (jsonObject != null) {
                                        documentsModel.docid = jsonObject.optString("docid")
                                        documentsModel.name = jsonObject.optString("name")
                                        documentsModel.user_id = jsonObject.optString("user_id")
                                        documentsModel.doctype = jsonObject.optString("doctype")
                                        documentsModel.tags_list = jsonObject.optJSONObject("tags") ?: JSONObject()
                                        documentsModel.contentType = jsonObject.optString("contentType")
                                        documentsModel.viewUrl = jsonObject.optString("viewUrl")
                                        documentsModel.is_encrypted = jsonObject.optBoolean("is_encrypted")
                                        documentsModel.is_password = jsonObject.optBoolean("is_password")
                                        documentsModel.isAdded_encryption = jsonObject.optBoolean("added_encryption")
                                        documentsList.add(documentsModel)
                                    }
                                }
                                val groupsL = existing_groups_list
                                if (groupsL != null) {
                                    for (k in 0 until groupsL.length()) {
                                        val groupsModel = GroupsModel()
                                        val jsonObject = groupsL.optJSONObject(k)
                                        if (jsonObject != null) {
                                            groupsModel.group_id = jsonObject.optString("id")
                                            groupsModel.group_name = jsonObject.optString("name")
                                            groupsList.add(groupsModel)
                                        }
                                    }
                                }
                                val clientsL = existing_clients
                                if (clientsL != null) {
                                    for (m in 0 until clientsL.length()) {
                                        val clientsModel = ClientsModel()
                                        val jsonObject = clientsL.optJSONObject(m)
                                        if (jsonObject != null) {
                                            clientsModel.client_id = jsonObject.optString("id")
                                            clientsModel.client_name = jsonObject.optString("name")
                                            clientsModel.client_type = jsonObject.optString("type")
                                            selected_clients_list.add(clientsModel)
                                        }
                                    }
                                }
                                val clientsListArr = existing_clients_list
                                if (clientsListArr != null) {
                                    for (c in 0 until clientsListArr.length()) {
                                        val clientsModel = ClientsModel()
                                        val jsonObject = clientsListArr.optJSONObject(c)
                                        if (jsonObject != null) {
                                            clientsModel.client_id = jsonObject.optString("id")
                                            clientsModel.client_name = jsonObject.optString("name")
                                            clientsModel.client_type = jsonObject.optString("type")
                                            clientsList.add(clientsModel)
                                        }
                                    }
                                }
                            }
                            if (matterModel.members != null) {
                                existing_members = matterModel.members
                                try {
                                    val membersArr = existing_members
                                    if (membersArr != null) {
                                        for (t in 0 until membersArr.length()) {
                                            val teamModel = TeamModel()
                                            val jsonObject = membersArr.optJSONObject(t)
                                            if (jsonObject != null) {
                                                teamModel.tm_id = jsonObject.optString("id")
                                                teamModel.tm_name = jsonObject.optString("name")
                                                teamModel.user_id = jsonObject.optString("user_id")
                                                selected_tm_list.add(teamModel)
                                            }
                                        }
                                    }
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                            if (matterModel.members_list != null) {
                                existing_tm_list = matterModel.members_list
                                try {
                                    val tmListArr = existing_tm_list
                                    if (tmListArr != null) {
                                        for (d in 0 until tmListArr.length()) {
                                            val teamModel = TeamModel()
                                            val jsonObject = tmListArr.optJSONObject(d)
                                            if (jsonObject != null) {
                                                teamModel.tm_id = jsonObject.optString("id")
                                                teamModel.tm_name = jsonObject.optString("name")
                                                teamModel.user_id = jsonObject.optString("user_id")
                                                tmList.add(teamModel)
                                            }
                                        }
                                    }
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                            val matList = matterArraylist
                            if (matList != null && matList.size > i && matList[i].opponent_advocate != null) {
                                existing_opponents = matList[i].opponent_advocate
                                try {
                                    val opps = existing_opponents
                                    if (opps != null) {
                                        for (j in 0 until opps.length()) {
                                            try {
                                                val jsonObject = opps.optJSONObject(j)
                                                if (jsonObject != null) {
                                                    val advocateModel = AdvocateModel()
                                                    advocateModel.advocate_name = jsonObject.optString("name")
                                                    advocateModel.number = jsonObject.optString("phone")
                                                    advocateModel.email = jsonObject.optString("email")
                                                    advocates_list.add(advocateModel)
                                                }
                                            } catch (e: JSONException) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            if (matterModel.tags_list != null) {
                                existing_tags_list = matterModel.tags_list
                            }
                            tag_list.clear()
                            val tagsL = existing_tags_list
                            if (tagsL != null && tagsL.length() > 0) {
                                for (j in 0 until tagsL.length()) {
                                    try {
                                        tag_list.add(tagsL.optString(j))
                                    } catch (e: JSONException) {
                                        throw RuntimeException(e)
                                    }
                                }
                            }
                            if (matterModel.matter_title != null) {
                                matter_title = matterModel.matter_title as String
                            }
                            if (matterModel.case_number != null) {
                                case_number = matterModel.case_number
                            }
                            if (matterModel.case_type != null) {
                                case_type = matterModel.case_type
                            }
                            if (matterModel.description != null) {
                                description = matterModel.description
                            }
                            if (matterModel.date_of_filing != null) {
                                dof = matterModel.date_of_filing
                            }
                            if (matterModel.start_date != null) {
                                start_date = matterModel.start_date
                            }
                            if (matterModel.end_date != null) {
                                end_date = matterModel.end_date
                            }
                            if (matterModel.court != null) {
                                court = matterModel.court
                            }
                            if (matterModel.judge != null) {
                                judge = matterModel.judge
                            }
                            if (matterModel.case_priority != null) {
                                case_priority = matterModel.case_priority
                            }
                            if (matterModel.status != null) {
                                case_status = matterModel.status
                            }
                            if (selected_documents_list.isNotEmpty()) {
                                DocumentsText()
                                loadSelectedDocuments(arrayOfNulls<String>(selected_documents_list.size))
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            ll_matterDate?.visibility = View.GONE
            ll_upload_type?.visibility = View.GONE
            btn_create?.setText(R.string.submit)
        } else {
            matter_title_tv?.text = Constants.Matter_title
            ll_upload_type?.visibility = View.VISIBLE
            btn_create?.isEnabled = false
            btn_create?.alpha = 0.5f
            btn_create?.setText(R.string.save)
            matter_date?.text = Constants.matterDate
            existing_document()
        }

        DisableDownloadBackground()
        tv_enable_download?.setOnClickListener {
            EnableDownloadBackground()
            adapter?.EnableAllorDisableAll(true)
        }
        tv_disable_download?.setOnClickListener {
            DisableDownloadBackground()
            adapter?.EnableAllorDisableAll(false)
        }
        tv_enable_encryption?.setOnClickListener {
            ENCRYPTION_TAG = true
            if (ENCRYPTION_TAG) {
                EnableEncryptionBackground()
            }
            ENCRYPTION_TAG = !ENCRYPTION_TAG
        }
        tv_disable_encryption?.setOnClickListener {
            DECRYPTION_TAG = true
            if (DECRYPTION_TAG) {
                DisableEncryptionBackground()
                adapter?.EncryptAllorDecryptAll(false)
                val tag = "dis_encrption"
                loadRecyclerview(tag, subtag)
            }
            DECRYPTION_TAG = !DECRYPTION_TAG
        }
        if (Constants.create_matter) {
            callDocumentsWebService()
        }
        rv_display_upload_doc?.visibility = View.GONE
        chk_select_all?.setOnClickListener {
            chk_select_all?.isChecked = isselect_all_checked
            isselect_all_checked = !isselect_all_checked
            adapter?.selectOrDeselectAll(chk_select_all?.isChecked == true)
        }
        tv_add_tag?.setOnClickListener {
            if (is_clicked_add) {
                AddTag()
            } else {
                Hide_Add_EditMeta()
            }
            is_clicked_edit = true
            is_clicked_add = !is_clicked_add
        }
        tv_edit_meta?.setOnClickListener {
            if (is_clicked_edit) {
                EditMeta()
            } else {
                Hide_Add_EditMeta()
            }
            is_clicked_add = true
            is_clicked_edit = !is_clicked_edit
        }
        btn_add_tags?.setOnClickListener {
            selected_upload_documents_list.clear()
            if (adapter != null) {
                for (i in 0 until adapter!!.list_item.size) {
                    val documentsModel = adapter!!.list_item[i]
                    if (documentsModel.isChecked) {
                        selected_upload_documents_list.add(documentsModel)
                    }
                }
            }
            open_add_tags_popup()
        }
        return view
    }

    private fun AddTag() {
        chk_box_layout?.alpha = 1f
        chk_select_all?.isEnabled = true
        btn_create?.visibility = View.GONE
        btn_add_tags?.visibility = View.VISIBLE
        tv_edit_meta?.setTextColor(requireContext().resources.getColor(R.color.black, null))
        tv_add_tag?.setTextColor(requireContext().resources.getColor(R.color.white, null))
        tv_edit_meta?.background = requireContext().resources.getDrawable(R.drawable.button_right_background, null)
        tv_add_tag?.background = requireContext().resources.getDrawable(R.drawable.button_left_green_background, null)
        val tag = "add_tag"
        Constants.DocTagType = tag
        for (i in upload_documents_list.indices) {
            upload_documents_list[i].isChecked = false
        }
        loadRecyclerview(tag, subtag)
    }

    private fun Hide_Add_EditMeta() {
        chk_box_layout?.alpha = 0.5f
        chk_select_all?.isEnabled = false
        chk_select_all?.isChecked = false
        isselect_all_checked = true
        btn_create?.visibility = View.VISIBLE
        btn_add_tags?.visibility = View.GONE
        tv_edit_meta?.setTextColor(requireContext().resources.getColor(R.color.black, null))
        tv_add_tag?.setTextColor(requireContext().resources.getColor(R.color.black, null))
        tv_add_tag?.background = requireContext().resources.getDrawable(R.drawable.button_left_background, null)
        tv_edit_meta?.background = requireContext().resources.getDrawable(R.drawable.button_right_background, null)
        val tag = "Hide_Add_Edit_tag"
        Constants.DocTagType = tag
        for (i in upload_documents_list.indices) {
            upload_documents_list[i].isChecked = false
        }
        loadRecyclerview(tag, subtag)
    }

    private fun EditMeta() {
        chk_box_layout?.alpha = 0.5f
        chk_select_all?.isEnabled = false
        chk_select_all?.isChecked = false
        isselect_all_checked = true
        btn_create?.visibility = View.VISIBLE
        btn_add_tags?.visibility = View.GONE
        tv_edit_meta?.setTextColor(requireContext().resources.getColor(R.color.white, null))
        tv_add_tag?.setTextColor(requireContext().resources.getColor(R.color.black, null))
        tv_add_tag?.background = requireContext().resources.getDrawable(R.drawable.button_left_background, null)
        tv_edit_meta?.background = requireContext().resources.getDrawable(R.drawable.button_right_green_background, null)
        val tag = "edit_meta"
        Constants.DocTagType = tag
        for (i in upload_documents_list.indices) {
            upload_documents_list[i].isChecked = false
        }
        loadRecyclerview(tag, subtag)
    }

    private fun DisableEncryptionBackground() {
        tv_enable_encryption?.background = requireContext().resources.getDrawable(R.drawable.button_left_background, null)
        tv_enable_encryption?.setTextColor(requireContext().getColor(R.color.black))
        tv_disable_encryption?.setTextColor(requireContext().getColor(R.color.white))
        tv_disable_encryption?.background = requireContext().resources.getDrawable(R.drawable.button_right_green_background, null)
    }

    private fun EnableEncryptionBackground() {
        ENCRYPTION_TAG = true
        tv_enable_encryption?.background = requireContext().resources.getDrawable(R.drawable.button_left_green_background, null)
        tv_enable_encryption?.setTextColor(requireContext().getColor(R.color.white))
        tv_disable_encryption?.setTextColor(requireContext().getColor(R.color.black))
        tv_disable_encryption?.background = requireContext().resources.getDrawable(R.drawable.button_right_background, null)
        adapter?.EncryptAllorDecryptAll(true)
        val tag = "en_encrption"
        loadRecyclerview(tag, subtag)
    }

    private fun DisableDownloadBackground() {
        DOWNLOAD_TAG = false
        tv_enable_download?.background = requireContext().resources.getDrawable(R.drawable.button_left_background, null)
        tv_enable_download?.setTextColor(requireContext().getColor(R.color.black))
        tv_disable_download?.setTextColor(requireContext().getColor(R.color.white))
        tv_disable_download?.background = requireContext().resources.getDrawable(R.drawable.button_right_green_background, null)
        val tag = "disable_download"
        loadRecyclerview(tag, subtag)
    }

    private fun EnableDownloadBackground() {
        DOWNLOAD_TAG = true
        tv_disable_download?.background = requireContext().resources.getDrawable(R.drawable.button_right_background, null)
        tv_enable_download?.setTextColor(requireContext().getColor(R.color.white))
        tv_disable_download?.setTextColor(requireContext().getColor(R.color.black))
        tv_enable_download?.background = requireContext().resources.getDrawable(R.drawable.button_left_green_background, null)
        val tag = "enable_download"
        loadRecyclerview(tag, subtag)
    }

    fun checkEnableDownloadStatus() {
        var allEnabled = true
        var allDisabled = true

        for (item in upload_documents_list) {
            if (item.isEnabled) {
                allDisabled = false // At least one is encrypted
            } else {
                allEnabled = false // At least one is decrypted
            }

            if (!allDisabled && !allEnabled) {
                break // No need to check further
            }
        }

        if (allEnabled) {
            check_enabled(true) // Enable encryption background
        } else if (allDisabled) {
            check_enabled(false) // Disable encryption background
        }
    }

    fun checkEnableEncryption() {
        var allEnabled = true
        var allDisabled = true

        for (item in upload_documents_list) {
            if (item.isEnabled) {
                allDisabled = false // At least one is encrypted
            } else {
                allEnabled = false // At least one is decrypted
            }

            if (!allDisabled && !allEnabled) {
                break // No need to check further
            }
        }

        if (allEnabled) {
            check_enabled(true) // Enable encryption background
        } else if (allDisabled) {
            check_enabled(false) // Disable encryption background
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_document_library -> loadDocumentLibraryUI()
            R.id.tv_device_drive -> loadDeviceDriveUI()
            R.id.btn_cancel_save -> {
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
            R.id.btn_browse -> checkPermissionREAD_EXTERNAL_STORAGE(context)
            else -> throw IllegalStateException("Unexpected value: " + view.id)
        }
    }

    private fun submitMatterInformation() {
        if (selected_documents_list.isEmpty() && upload_documents_list.isEmpty()) {
            // AndroidUtils.showAlert("Please add atleast one document from existing documents or upload a new one",getContext());
        } else {
            try {
                if (upload_documents_list.isNotEmpty()) {
                    try {
                        totalUploads = upload_documents_list.size
                        completedUploads = 0
                        isAlertShown = false
                        progress_dialog = AndroidUtils.get_progress(activity)
                        for (i in upload_documents_list.indices) {
                            val name = upload_documents_list[i].name
                            val new_clients = JSONArray()
                            val new_groups = JSONArray()
                            val clients_jobject = JSONObject()
                            val matter = JSONArray()
                            var docname = ""
                            val documentsModel = upload_documents_list[i]
                            filename = documentsModel.name
                            val new_file = documentsModel.file
                            val isenabled = documentsModel.isEnabled
                            var doc_type = "pdf"
                            val content_string = new_file?.name?.replace(".", "/") ?: ""
                            val content_type = content_string.split("/").toTypedArray()
                            if (content_type.size >= 2) {
                                doc_type = content_type[1]
                                docname = content_type[0]
                            }
                            val matters = JSONArray()
                            matters.put(Constants.Matter_id)
                            val jsonObject = JSONObject()
                            uploaded_document_name = name
                            upload_description = upload_documents_list[i].description
                            upload_exp_date = upload_documents_list[i].expiration_date
                            jsonObject.put("name", name)
                            jsonObject.put("description", upload_description)
                            jsonObject.put("expiration_date", upload_exp_date)
                            jsonObject.put("filename", docname)
                            jsonObject.put("matters", matters)
                            jsonObject.put("category", "client")
                            if (Constants.clientList.length() > 0) {
                                jsonObject.put("clients", Constants.clientList)
                            } else {
                                jsonObject.put("clients", Constants.corpclientList)
                            }
                            jsonObject.put("groups", Constants.ex_group_attachment)
                            jsonObject.put("downloadDisabled", isenabled)
                            jsonObject.put("custom_encrypt", upload_documents_list[i].isencrypted)
                            if (upload_documents_list[i].tags == null) {
                                jsonObject.put("tags", "")
                            } else {
                                jsonObject.put("tags", upload_documents_list[i].tags)
                            }

                            if (doc_type.equals("apng", ignoreCase = true) || doc_type.equals("avif", ignoreCase = true) || doc_type.equals("gif", ignoreCase = true) || doc_type.equals("jpeg", ignoreCase = true) || doc_type.equals("png", ignoreCase = true) || doc_type.equals("svg", ignoreCase = true) || doc_type.equals("webp", ignoreCase = true) || doc_type.equals("jpg", ignoreCase = true)) {
                                jsonObject.put("content_type", "image/$doc_type")
                            } else {
                                jsonObject.put("content_type", "application/$doc_type")
                            }
                            if (new_file != null) {
                                WebServiceHelper.callHttpUploadWebService(this, requireContext(), WebServiceHelper.RestMethodType.POST, "v3/document/upload", "Upload Document", new_file, jsonObject.toString())
                            }
                        }
                    } catch (e: JSONException) {
                        if (progress_dialog != null && progress_dialog!!.isShowing)
                            AndroidUtils.dismiss_dialog(progress_dialog)
                        e.printStackTrace()
                    }
                } else {
                    submitMatter()
                }
            } catch (e: Exception) {
                if (progress_dialog != null && progress_dialog!!.isShowing)
                    AndroidUtils.dismiss_dialog(progress_dialog)
                e.printStackTrace()
            }
        }
    }

    // Define ActivityResultLauncher
    var requestPermissions: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            var permissionGranted = false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                permissionGranted = java.lang.Boolean.TRUE == results[READ_MEDIA_IMAGES] ||
                        java.lang.Boolean.TRUE == results[READ_MEDIA_VIDEO] ||
                        java.lang.Boolean.TRUE == results[READ_MEDIA_VISUAL_USER_SELECTED]
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionGranted = java.lang.Boolean.TRUE == results[READ_MEDIA_IMAGES] ||
                        java.lang.Boolean.TRUE == results[READ_MEDIA_VIDEO]
            } else {
                permissionGranted = java.lang.Boolean.TRUE == results[READ_EXTERNAL_STORAGE]
            }

            if (permissionGranted) {
                BottomSheetUploadfile()
            }
        }

    fun checkPermissionREAD_EXTERNAL_STORAGE(context: Context?): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (ContextCompat.checkSelfPermission(context!!, READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, READ_MEDIA_VISUAL_USER_SELECTED) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions.launch(arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED))
                } else {
                    BottomSheetUploadfile()
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context!!, READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions.launch(arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO))
                } else {
                    BottomSheetUploadfile()
                }
            } else {
                if (ContextCompat.checkSelfPermission(context!!, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions.launch(arrayOf(READ_EXTERNAL_STORAGE))
                } else {
                    BottomSheetUploadfile()
                }
            }
            return false
        } else {
            return true
        }
    }

    fun DocumentsText() {
        AndroidUtils.ToggleButton(tempSelectedDocuments.size, btn_add_documents)
        if (tempSelectedDocuments.isEmpty()) {
            at_add_documents?.text = ""
        } else {
            val value = arrayOfNulls<String>(tempSelectedDocuments.size)
            for (i in tempSelectedDocuments.indices) {
                value[i] = tempSelectedDocuments[i].name
            }
            at_add_documents?.text = java.lang.String.join(", ", *value)
        }
    }

    private fun callDocumentsWebService() {
        try {
            val group_acls = JSONArray()
            val clients = JSONArray()
            val postdata = JSONObject()
            for (i in selected_groups_list.indices) {
                val groupsModel = selected_groups_list[i]
                group_acls.put(groupsModel.group_id)
            }
            for (i in selected_clients_list.indices) {
                val clientsModel = selected_clients_list[i]
                clients.put(clientsModel.client_id)
            }
            if (selected_temp_clients_list.isNotEmpty()) {
                for (i in selected_temp_clients_list.indices) {
                    val clientsModel = selected_temp_clients_list[i]
                    clients.put(clientsModel.client_id)
                }
            }
            if (selected_corp_clients_list.isNotEmpty()) {
                for (i in selected_corp_clients_list.indices) {
                    val clientsModel = selected_corp_clients_list[i]
                    clients.put(clientsModel.client_id)
                }
            }
            postdata.put("clients", clients)
            postdata.put("group_acls", group_acls)
            postdata.put("attachment_type", "documents")
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.PUT, "matter/attachments", "Documents", postdata.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun loadDeviceDriveUI() {
        ll_selected_documents?.removeAllViews()
        isDocUploaded = upload_documents_list.isNotEmpty()
        tv_selected_document?.visibility = View.GONE
        tv_document_library?.background = context?.resources?.getDrawable(R.drawable.button_left_background, null)
        tv_document_library?.setTextColor(Color.BLACK)
        tv_device_drive?.background = context?.resources?.getDrawable(R.drawable.button_right_green_background, null)
        tv_device_drive?.setTextColor(Color.WHITE)
        ll_add_documents?.visibility = View.GONE
        ll_select_doc?.visibility = View.VISIBLE
        UploadedDocView()
    }

    private fun loadDocumentLibraryUI() {
        isDocUploaded = true
        ll_uploaded_documents?.removeAllViews()
        ll_uploaded_documents?.visibility = View.GONE
        DocumentsText()
        loadSelectedDocuments(arrayOfNulls<String>(selected_documents_list.size))
        tv_document_library?.background = context?.resources?.getDrawable(R.drawable.button_left_green_background, null)
        tv_document_library?.setTextColor(Color.WHITE)
        tv_device_drive?.background = context?.resources?.getDrawable(R.drawable.button_right_background, null)
        tv_device_drive?.setTextColor(Color.BLACK)
        ll_add_documents?.visibility = View.VISIBLE
        ll_select_doc?.visibility = View.GONE
    }

    private fun BottomSheetUploadfile() {
        cl_matter_document?.alpha = 0.5f
        Constants.isDocEditor = false
        bottommSheetUploadDocument = BottomSheetUploadFile(cl_matter_document)
        bottommSheetUploadDocument?.show(parentFragmentManager, "")
        bottommSheetUploadDocument?.setTargetFragment(this@MatterDocuments, 1)
    }

    @SuppressLint("Range")
    @Throws(IOException::class)
    override fun getImagepath(imagepath: File?, ImageURI: Uri?) {
        if (ImageURI != null) {
            if (imagepath == null) {
                mSelectedBitmap = null
                mSelectedUri = imagepath
                val uri = imagepath.toString()
                val imageLoader = ImageLoader.getInstance()
                imageLoader.init(ImageLoaderConfiguration.createDefault(requireActivity()))
                imageLoader.displayImage(Uri.fromFile(File(uri)).toString(), imageView)
                file = imagepath
                val c = requireContext().contentResolver.query(ImageURI, null, null, null, null)
                if (c != null) {
                    if (c.moveToFirst()) {
                        val file_name = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        load_documents(file_name, file)
                    }
                    c.close()
                }
            } else {
                file = getFile(requireContext(), ImageURI)
                Log.i("FILE", "Info:$file")
                val file_name = file!!.name
                load_documents(file_name, file)
            }
        }
        cl_matter_document?.alpha = 1.0f
        bottommSheetUploadDocument?.dismiss()
    }

    private fun load_documents(file_name: String, file: File?) {
        var doc_type = ""
        var docname = ""
        val content_string = file_name.replace(".", "/")
        val content_type = content_string.split("/").toTypedArray()
        if (content_type.size >= 2) {
            doc_type = content_type[1]
            docname = content_type[0]
        }
        val documentsModel = com.digicoffer.lauditor.Documents.Models.DocumentsModel()
        documentsModel.name = docname
        documentsModel.filename = file_name
        documentsModel.content_type = doc_type
        documentsModel.description = docname
        documentsModel.file = file
        documentsModel.isEnabled = false
        documentsModel.isChecked = false
        upload_documents_list.add(documentsModel)
        if (upload_documents_list.isNotEmpty()) {
            DisableDownloadBackground()
            tv_enable_encryption?.background = context?.resources?.getDrawable(R.drawable.button_left_background, null)
            tv_enable_encryption?.setTextColor(requireContext().getColor(R.color.black))
            tv_disable_encryption?.setTextColor(requireContext().getColor(R.color.white))
            tv_disable_encryption?.background = context?.resources?.getDrawable(R.drawable.button_right_green_background, null)
        }
        EditMeta()
        is_clicked_edit = true
        is_clicked_add = true
        loadUploadedDocuments()
    }
    override fun getImageBitmap(bitmap: Bitmap?) {
        imageView?.setImageBitmap(bitmap)
        mSelectedBitmap = bitmap
        mSelectedUri = null
        val filesDir = requireContext().filesDir
        val imageFile = File(filesDir, "bitmap.jpg")
        val os: OutputStream
        try {
            os = Files.newOutputStream(imageFile.toPath())
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.flush()
            os.close()
            file = imageFile
            val documentsModel = DocumentsModel()
            documentsModel.name = file!!.name
            selected_documents_list.add(documentsModel)
            tempSelectedDocuments.add(documentsModel)
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error writing bitmap", e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            val selectedImage = data?.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = requireContext().contentResolver.query(
                selectedImage!!,
                filePathColumn, null, null, null
            )
            cursor!!.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()
            file = File(picturePath)
            filename = file!!.name
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            123 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do your stuff
            } else {
                AndroidUtils.showAlert("GET_ACCOUNTS Denied", activity)
            }
            else -> super.onRequestPermissionsResult(
                requestCode, permissions,
                grantResults
            )
        }
    }

    fun callDecryptApi(id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(activity)
            val jsonObject = JSONObject()
            jsonObject.put("docid", id)
            jsonObject.put("download", false)
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.POST, Constants.decryptUrl ?: "", "Decrypt Doc", jsonObject.toString())
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun handleDocumentDisplay(url: String, docModel: DocumentsModel, idPDFView: PDFView, iv_image: ImageView) {
        val isImage = File_Content_Type.isImage(docModel.contentType)
        val isPDF = File_Content_Type.isPDF(docModel.contentType)
        val isEncrypted = docModel.isAdded_encryption || docModel.is_encrypted

        if (isEncrypted) {
            dialog?.dismiss()
            callDecryptApi(docModel.docid ?: "")
            return
        }

        if (isImage) {
            idPDFView.visibility = View.GONE
            iv_image.visibility = View.VISIBLE

            Glide.with(requireContext())
                .load(url)
                .placeholder(R.drawable.progress_animation)
                .centerCrop()
                .into(iv_image)
        } else if (isPDF) {
            iv_image.visibility = View.GONE
            idPDFView.visibility = View.VISIBLE
        } else {
            dialog?.dismiss()
            callOtherDocViewApi(docModel.docid ?: "")
        }
    }

    fun callOtherDocViewApi(id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(activity)
            val jsonObject = JSONObject()
            jsonObject.put("docid", id)
            jsonObject.put("download", false)
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "${Constants.base_URL ?: ""}v3/document/$id/view", "Other Doc View", jsonObject.toString())
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progress_dialog != null && progress_dialog!!.isShowing)
            AndroidUtils.dismiss_dialog(progress_dialog)
        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent)

                when (httpResult.requestType) {
                    "Documents" -> {
                        val data = result.getJSONArray("documents")
                        loadDocumentsData(data)
                    }
                    "Update Members" -> {
                        val isError = result.optBoolean("error")
                        val msg = result.optString("msg")
                        if (isError) {
                            AndroidUtils.showAlert(msg, activity)
                        }
                    }
                    "Documents_List" -> {
                        val data = result.getJSONArray("documents")
                        loadDocumentsData(data)
                    }
                    "matter_update" -> {
                        val msg = result.getString("msg")
                        AndroidUtils.showAlert("" + msg, activity, "")
                        matter?.loadViewUI()
                    }
                    "Update Documents List" -> {
                        val msg = result.getString("msg")
                        matterArraylist?.clear()
                        matter?.loadViewUI()
                        if (Constants.ROLE != "TM")
                            if (selected_temp_clients_list.isNotEmpty()) {
                                triggerUpdateMembersForTempClients()
                            }
                    }
                    "Chosen_Documents" -> {
                        val data = result.getJSONArray("documents")
                        display_doc(data)
                        if (selected_documents_list.isNotEmpty()) {
                            DocumentsText()
                            loadSelectedDocuments(arrayOfNulls<String>(selected_documents_list.size))
                        }
                    }
                    "Upload Document" -> {
                        completedUploads++
                        val isError = result.optBoolean("error")
                        val msg = result.optString("msg")

                        if (!isError) {
                            val docid = result.getString("docid")
                            addDocsToMatter(docid)
                        }
                        if (completedUploads == totalUploads && !isAlertShown) {
                            isAlertShown = true

                            if (isError) {
                                AndroidUtils.showAlert(msg, activity)
                            } else {
                                update_document()
                            }
                        }
                    }
                    "Decrypt Doc", "Other Doc View" -> {
                        val error = result.getBoolean("error")
                        if (!error) {
                            val url = result.getJSONObject("data").getString("url")
                            display_doc(url, sharedDocumentsDo)
                            Log.d("TAG_Image", url)
                        } else {
                            val msg = result.optString("msg", "")
                            AndroidUtils.showAlert(msg, activity)
                        }
                    }
                    "View Doc" -> {
                        val error = result.getBoolean("error")
                        if (!error) {
                            val url = result.getJSONObject("data").getString("url")
                            checkViewType(url, sharedDocumentsDo)
                            Log.d("TAG_Image", url)
                        } else {
                            val msg = result.optString("msg", "")
                            AndroidUtils.showAlert(msg, activity)
                        }
                    }
                    "Matter List" -> {
                        val error = result.getBoolean("error")
                        if (error) {
                            val msg = result.getString("msg")
                            AndroidUtils.showAlert(msg, activity)
                        } else {
                            val matters = result.getJSONArray("matters")
                            try {
                                loadMattersList(matters)
                            } catch (e: Exception) {
                                AndroidUtils.showAlert(e.message, activity)
                                e.printStackTrace()
                            }
                        }
                    }
                    "Create Matter" -> {
                        val error = result.getBoolean("error")
                        val msg = result.getString("msg")
                        if (error) {
                            AndroidUtils.showAlert(msg, activity)
                        } else {
                            AndroidUtils.showAlert(msg, activity, "")
                            matter_id = result.getString("matter_id")
                            matterArraylist?.clear()
                            matter?.loadViewUI()
                            if (selected_documents_list.isNotEmpty())
                                UpdateDocWebservice()
                        }
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else if (httpResult.result == WebServiceHelper.ServiceCallStatus.Failed) {
            try {
                val result = JSONObject(httpResult.responseContent)
                if (result.optBoolean("error")) {
                    AndroidUtils.showErrorAlert(result.optString("msg"), activity)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            AndroidUtils.showErrorAlert(httpResult.responseContent.toString(), activity)
        }
    }

    fun UpdateDocWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(activity)
            val postdata = JSONObject()
            val documents = JSONArray()
            for (i in selected_documents_list.indices) {
                val documentsModel = selected_documents_list[i]
                documents.put(documentsModel.docid)
            }
            postdata.put("documents", documents)
            postdata.put("matter_id", matter_id)
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.PATCH, "v3/update", "Update Documents List", postdata.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerUpdateMembersForTempClients() {
        for (i in selected_temp_clients_list.indices) {
            val tempClientId = selected_temp_clients_list[i].rel_id
            callUpdateMembers(tempClientId ?: "")
        }
    }

    private fun callUpdateMembers(id: String) {
        try {
            val postdata = JSONObject()
            val members = JSONArray()
            if (selected_temp_clients_list.isNotEmpty()) {
                for (i in selected_tm_list.indices) {
                    val teamModel = selected_tm_list[i]
                    members.put(teamModel.tm_id)
                }
            }
            postdata.put("members", members)
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.PUT, "v2/relationship/$id/members", "Update Members", postdata.toString())
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun submitMatter() {
        try {
            if (selected_corp_clients_list.isNotEmpty())
                corp_client_id = selected_corp_clients_list[0].client_id ?: ""
            var Matter_type = "legal"
            val postdata = JSONObject()
            val clients = JSONArray()
            val documents = JSONArray()
            val group_acls = JSONArray()
            val members = JSONArray()
            val opponent_advocates = JSONArray()

            for (i in selected_clients_list.indices) {
                val jsonObject = JSONObject()
                val clientsModel = selected_clients_list[i]
                jsonObject.put("id", clientsModel.client_id)
                jsonObject.put("type", clientsModel.client_type)
                clients.put(jsonObject)
            }
            if (selected_temp_clients_list.isNotEmpty()) {
                for (i in selected_temp_clients_list.indices) {
                    val jsonObject = JSONObject()
                    val clientsModel = selected_temp_clients_list[i]
                    jsonObject.put("id", clientsModel.client_id)
                    jsonObject.put("type", clientsModel.client_type)
                    clients.put(jsonObject)
                }
            }
            if (corp_client_id.isNotEmpty()) {
                val jsonObject = JSONObject()
                jsonObject.put("id", corp_client_id)
                jsonObject.put("type", "corporate")
                clients.put(jsonObject)
            }
            for (i in selected_documents_list.indices) {
                val jsonObject = JSONObject()
                val documentsModel = selected_documents_list[i]
                jsonObject.put("docid", documentsModel.docid)
                jsonObject.put("doctype", documentsModel.doctype)
                jsonObject.put("user_id", documentsModel.user_id)
                documents.put(jsonObject)
            }
            matterModel.documents = documents
            existing_tags_list = JSONArray()
            for (i in tag_list.indices) {
                existing_tags_list?.put(tag_list[i])
            }
            matterModel.tags_list = existing_tags_list ?: JSONArray()
            for (i in selected_groups_list.indices) {
                val groupsModel = selected_groups_list[i]
                group_acls.put(groupsModel.group_id)
            }

            for (i in selected_tm_list.indices) {
                val teamModel = selected_tm_list[i]
                val jsonObject = JSONObject()
                jsonObject.put("id", teamModel.tm_id)
                members.put(jsonObject)
            }

            for (i in advocates_list.indices) {
                val jsonObject = JSONObject()
                val advocateModel = advocates_list[i]
                jsonObject.put("name", advocateModel.advocate_name)
                jsonObject.put("email", advocateModel.email)
                jsonObject.put("phone", advocateModel.number)
                opponent_advocates.put(jsonObject)
            }
            val tagsObject = JSONObject()

            for (i in tag_list.indices) {
                try {
                    tagsObject.put(i.toString(), tag_list[i])
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            postdata.put("title", matter_title)
            postdata.put("affidavit_filing_date", "")
            postdata.put("affidavit_isfiled", "")
            postdata.put("description", description)
            postdata.put("priority", case_priority)
            postdata.put("status", case_status)
            postdata.put("clients", clients)
            postdata.put("corporate", corp_client_id)
            postdata.put("documents", documents)
            postdata.put("group_acls", group_acls)
            postdata.put("members", members)
            postdata.put("opponent_advocates", opponent_advocates)
            postdata.put("tags", tagsObject)
            if (Constants.MATTER_TYPE == "Legal") {
                postdata.put("judges", judge)
                postdata.put("date_of_filling", dof)
                postdata.put("court_name", court)
                postdata.put("case_number", case_number)
                postdata.put("case_type", case_type)
                Matter_type = "legal"
            } else {
                postdata.put("startdate", start_date)
                postdata.put("closedate", end_date)
                postdata.put("matter_number", case_number)
                postdata.put("matter_type", case_type)
                Matter_type = "general"
            }
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.POST, "matter/$Matter_type/create", "Create Matter", postdata.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    private fun addDocsToMatter(docid: String) {
        try {
            val documentsModel = DocumentsModel()
            documentsModel.docid = docid
            documentsModel.name = uploaded_document_name
            documentsModel.user_id = Constants.USER_ID
            documentsModel.doctype = "general"
            documentsModel.description = upload_description
            documentsModel.expiration_date = upload_exp_date
            selected_documents_list.add(documentsModel)
            tempSelectedDocuments.add(documentsModel)
            isDocUploaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun display_doc(existing_documents: JSONArray) {
        try {
            new_selected_doc.clear()
            for (d in 0 until existing_documents.length()) {
                val documentsModel = DocumentsModel()
                val jsonObject = existing_documents.getJSONObject(d)
                documentsModel.docid = jsonObject.optString("docid")
                documentsModel.name = jsonObject.optString("name")
                documentsModel.user_id = jsonObject.optString("user_id")
                documentsModel.doctype = jsonObject.optString("doctype")
                documentsModel.tags_list = jsonObject.optJSONObject("tags") ?: JSONObject()
                documentsModel.contentType = jsonObject.optString("contentType")
                documentsModel.viewUrl = jsonObject.optString("viewUrl")
                documentsModel.is_encrypted = jsonObject.optBoolean("is_encrypted")
                documentsModel.is_password = jsonObject.optBoolean("is_password")
                documentsModel.isAdded_encryption = jsonObject.optBoolean("added_encryption")
                selected_documents_list.add(documentsModel)
                tempSelectedDocuments.add(documentsModel)
                new_selected_doc.add(documentsModel)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun loadDocumentsData(data: JSONArray) {
        documentsList.clear()
        try {
            val existingDocIds = HashSet<String>()
            for (i in 0 until data.length()) {
                val jsonObject = data.getJSONObject(i)
                val documentsModel = DocumentsModel()
                documentsModel.docid = jsonObject.optString("docid")
                documentsModel.name = jsonObject.optString("name")
                documentsModel.user_id = jsonObject.optString("user_id")
                documentsModel.doctype = jsonObject.optString("doctype")
                documentsModel.tags_list = jsonObject.optJSONObject("tags") ?: JSONObject()
                documentsModel.contentType = jsonObject.optString("contentType")
                documentsModel.viewUrl = jsonObject.optString("viewUrl")
                documentsModel.is_encrypted = jsonObject.optBoolean("is_encrypted")
                documentsModel.is_password = jsonObject.optBoolean("is_password")
                documentsModel.isAdded_encryption = jsonObject.optBoolean("added_encryption")
                documentsList.add(documentsModel)
                existingDocIds.add(documentsModel.docid ?: "")
            }
            if (!Constants.create_matter && new_selected_doc != null) {
                for (newDoc in new_selected_doc) {
                    val newDocId = newDoc.docid
                    if (newDocId != null && !existingDocIds.contains(newDocId)) {
                        documentsList.add(newDoc)
                        existingDocIds.add(newDocId)
                    }
                }
            }
            for (i in selected_documents_list.indices.reversed()) {
                val selectedDoc = selected_documents_list[i]
                var existsInList = false
                for (doc in documentsList) {
                    if (selectedDoc.docid == doc.docid) {
                        doc.isChecked = true
                        existsInList = true
                        break
                    }
                }
                if (!existsInList) {
                    selected_documents_list.removeAt(i)
                    tempSelectedDocuments.removeAt(i)
                }
            }
            if (documentsList.isNotEmpty()) {
                DocumentsPopUp()
                if (!Constants.create_matter) {
                    rv_display_upload_doc?.visibility = View.VISIBLE
                } else {
                    rv_display_upload_doc?.visibility = View.GONE
                }
            } else {
                at_add_documents?.text = ""
                tempSelectedDocuments.clear()
                selected_documents_list.clear()
                rv_display_upload_doc?.visibility = View.GONE
                ll_selected_documents?.removeAllViews()
            }
            DocumentsText()
            loadSelectedDocuments(arrayOfNulls<String>(selected_documents_list.size))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun DocumentsPopUp() {
        try {
            if (documentsList.isNotEmpty()) {
                rv_display_upload_doc?.visibility = View.VISIBLE
            } else {
                rv_display_upload_doc?.visibility = View.GONE
            }
            for (i in documentsList.indices) {
                for (j in selected_documents_list.indices) {
                    if (documentsList[i].docid == selected_documents_list[j].docid) {
                        val documentsModel = documentsList[i]
                        documentsModel.isChecked = true
                    }
                }
            }
            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            rv_display_upload_doc?.layoutManager = layoutManager
            documentsAdapter = DocumentsAdapter(documentsList, this, btn_create)
            rv_display_upload_doc?.adapter = documentsAdapter
            btn_add_documents?.setOnClickListener {
                selected_documents_list.clear()
                selected_documents_list.addAll(tempSelectedDocuments)
                DocumentsText()
                loadSelectedDocuments(arrayOfNulls<String>(selected_documents_list.size))
                rv_display_upload_doc?.visibility = View.GONE
                ischecked_doc = true
                Add_Documents()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    private fun loadRecyclerview(tag: String, subtag: String) {
        ll_uploaded_documents?.layoutManager = LinearLayoutManager(context)
        adapter = DocumentsListAdapter(upload_documents_list, tag, subtag, this, this)
        ll_uploaded_documents?.adapter = adapter
        AndroidUtils.LoadList(ll_uploaded_documents, context, upload_documents_list.size, false)
        if (upload_documents_list.isEmpty()) {
            ll_uploaded_documents?.visibility = View.GONE
        } else {
            ll_uploaded_documents?.visibility = View.VISIBLE
        }
        chk_select_all?.setOnClickListener {
            chk_select_all?.isChecked = isselect_all_checked
            isselect_all_checked = !isselect_all_checked
            adapter?.selectOrDeselectAll(chk_select_all?.isChecked == true)
        }
    }

    private fun loadUploadedDocuments() {
        if (selected_documents_list.isEmpty()) {
            ll_selected_documents?.removeAllViews()
        }
        tv_selected_document?.visibility = View.GONE
        if (upload_documents_list.isNotEmpty()) {
            if (!Constants.create_matter) {
                btn_create?.alpha = 1.0f
                btn_create?.isEnabled = true
                ll_uploaded_documents?.removeAllViews()
            }
        }
        UploadedDocView()
    }

    fun EnableAllorDisableAll(isChecked: Boolean): Boolean {
        for (item in upload_documents_list) {
            item.isEnabled = isChecked
        }
        return isChecked
    }

    private fun updateSingleDocumentView(position: Int) {
        val child = ll_uploaded_documents?.getChildAt(position)
        if (child != null) {
            val enableIcon = child.findViewById<ImageView>(R.id.enable_download_icon)
            val disableIcon = child.findViewById<ImageView>(R.id.disable_download_icon)
            val btn_view_tags = child.findViewById<Button>(R.id.btn_view_tags)
            btn_view_tags.setText(R.string.view_tags)
            if (upload_documents_list[position].isEnabled) {
                enableIcon.visibility = View.VISIBLE
                disableIcon.visibility = View.GONE
            } else {
                enableIcon.visibility = View.GONE
                disableIcon.visibility = View.VISIBLE
            }
            val docTags = upload_documents_list[position].tags
            if (docTags != null && docTags.length() > 0) {
                btn_view_tags.visibility = View.VISIBLE
            } else {
                btn_view_tags.visibility = View.GONE
            }
        }
    }

    fun ViewTags(documentsModel: com.digicoffer.lauditor.Documents.Models.DocumentsModel, position: Int) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view_edit_tags = inflater.inflate(R.layout.edit_existing_tags, null)
        val ll_existing_tags = view_edit_tags.findViewById<LinearLayout>(R.id.ll_view_tags)
        val iv_close_existing_tags = view_edit_tags.findViewById<ImageView>(R.id.close_edit_docs)
        val header_name = view_edit_tags.findViewById<TextView>(R.id.header_name)
        val tv_document_name = view_edit_tags.findViewById<TextView>(R.id.tv_document_name)
        tv_document_name.textSize = DynamicUtils.twenty.toFloat()
        tv_document_name.visibility = View.VISIBLE
        tv_document_name.text = documentsModel.name
        header_name.setText(R.string.view_tags)
        val dialog = dialogBuilder.create()
        val tags = documentsModel.tags
        if (tags != null) {
            val iter = tags.keys()
            while (iter.hasNext()) {
                val key = iter.next()
                val value = tags.optString(key)
                val view_added_tags = inflater.inflate(R.layout.displays_documents_list, null)
                val tv_tag_name = view_added_tags.findViewById<TextView>(R.id.tv_document_name)
                val iv_remove_tag = view_added_tags.findViewById<ImageView>(R.id.iv_cancel)
                val tag_msg = "$key - $value"
                tv_tag_name.text = tag_msg
                iv_remove_tag.tag = iter
                iv_remove_tag.setOnClickListener {
                    ll_existing_tags.removeView(view_added_tags)
                    tags.remove(key)
                    if (tags.length() == 0) {
                        dialog.dismiss()
                        updateSingleDocumentView(position)
                    }
                }
                ll_existing_tags.addView(view_added_tags)
            }
        }
        iv_close_existing_tags.setOnClickListener { dialog.dismiss() }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setView(view_edit_tags)
        dialog.show()
    }
    fun View_doc(doc_id: String) {
        progress_dialog = AndroidUtils.get_progress(activity)
        val jsonObject = JSONObject()
        WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v3/document/$doc_id/view", "View Doc", jsonObject.toString())
    }

    fun Add_Documents() {
        if (selected_documents_list.isNotEmpty() || upload_documents_list.isNotEmpty()) {
            if (!Constants.create_matter) {
                btn_create?.isEnabled = true
                btn_create?.alpha = 1.0f
            }
        }
        DocumentsText()
        try {
            val documents = getJsonArray()
            matterModel.documents = documents
            if (matterArraylist != null && matterArraylist!!.isNotEmpty()) {
                matterArraylist!![0] = matterModel
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(JSONException::class)
    private fun getJsonArray(): JSONArray {
        val documents = JSONArray()
        for (i in selected_documents_list.indices) {
            val jsonObject = JSONObject()
            val documentsModel = selected_documents_list[i]
            jsonObject.put("docid", documentsModel.docid)
            jsonObject.put("doctype", documentsModel.doctype)
            jsonObject.put("user_id", documentsModel.user_id)
            jsonObject.put("name", documentsModel.name)
            jsonObject.put("tags", documentsModel.tags_list)
            jsonObject.put("contentType", documentsModel.contentType)
            jsonObject.put("viewUrl", documentsModel.viewUrl)
            jsonObject.put("is_encrypted", documentsModel.is_encrypted)
            jsonObject.put("is_password", documentsModel.is_password)
            jsonObject.put("added_encryption", documentsModel.isAdded_encryption)
            documents.put(jsonObject)
        }
        return documents
    }

    fun loadSelectedDocuments(value: Array<String?>) {
        SelectedDocView()
        ll_uploaded_documents?.removeAllViews()
        ll_selected_documents?.removeAllViews()
        for (i in selected_documents_list.indices) {
            val view_opponents = LayoutInflater.from(context).inflate(R.layout.edit_opponent_advocate, null)
            if (view_opponents != null) {
                val tv_opponent_name = view_opponents.findViewById<TextView>(R.id.tv_opponent_name)
                val iv_remove_opponent = view_opponents.findViewById<ImageView>(R.id.iv_remove_opponent)
                val iv_edit_opponent = view_opponents.findViewById<ImageView>(R.id.iv_edit_opponent)
                val iv_view_icon = view_opponents.findViewById<ImageView>(R.id.iv_edit_view)
                iv_view_icon.visibility = View.VISIBLE
                iv_edit_opponent.visibility = View.GONE
                iv_view_icon.tag = i
                iv_view_icon.setOnClickListener { v ->
                    val position = v.tag as Int
                    sharedDocumentsDo = selected_documents_list[position]
                    View_doc(selected_documents_list[position].docid ?: "")
                }
                if (tv_opponent_name != null && iv_remove_opponent != null) {
                    tv_opponent_name.text = selected_documents_list[i].name
                    iv_remove_opponent.tag = i
                    iv_remove_opponent.setOnClickListener { v ->
                        try {
                            val position = v.tag as Int
                            ll_selected_documents?.removeViewAt(position)
                            val documentsModel1 = tempSelectedDocuments.removeAt(position)
                            documentsModel1.isChecked = false
                            val documentsModel = selected_documents_list.removeAt(position)
                            documentsModel.isChecked = false
                            if (ll_selected_documents != null) {
                                for (j in 0 until ll_selected_documents!!.childCount) {
                                    val iv_remove = ll_selected_documents!!.getChildAt(j).findViewById<ImageView>(R.id.iv_remove_opponent)
                                    val iv_view = ll_selected_documents!!.getChildAt(j).findViewById<ImageView>(R.id.iv_edit_view)
                                    if (iv_remove != null) {
                                        iv_remove.tag = j
                                        iv_view.tag = j
                                    }
                                }
                            }
                            val str = value.filterNotNull().joinToString(",")
                            at_add_documents?.text = str
                            val stringBuilder = java.lang.StringBuilder()
                            for (model in selected_documents_list) {
                                stringBuilder.append(model.name).append(",")
                            }
                            if (stringBuilder.length > 0) {
                                stringBuilder.deleteCharAt(stringBuilder.length - 1)
                            }
                            val strn = stringBuilder.toString()
                            at_add_documents?.text = strn
                            if (!Constants.create_matter) {
                                btn_create?.alpha = 1.0f
                                btn_create?.isEnabled = true
                            }
                            try {
                                val documents = getJsonArray()
                                matterModel.documents = documents
                                if (matterArraylist != null && matterArraylist!!.isNotEmpty()) {
                                    matterArraylist!![0] = matterModel
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            SelectedDocView()
                            AndroidUtils.ToggleButton(selected_documents_list.size, btn_add_documents)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            AndroidUtils.showAlert(e.message, activity)
                        }
                    }
                    iv_remove_opponent.visibility = View.VISIBLE
                }
                ll_selected_documents?.addView(view_opponents)
            }
        }
    }

    private fun checkViewType(url: String, sharedDocumentsDo: DocumentsModel) {
        val isImage = File_Content_Type.isImage(sharedDocumentsDo.contentType)
        val isPDF = File_Content_Type.isPDF(sharedDocumentsDo.contentType)
        val isEncrypted = sharedDocumentsDo.isAdded_encryption || sharedDocumentsDo.is_encrypted

        if (isEncrypted) {
            callDecryptApi(sharedDocumentsDo.docid ?: "")
        } else if (!isPDF && !isImage) {
            callOtherDocViewApi(sharedDocumentsDo.docid ?: "")
        } else {
            display_doc(url, sharedDocumentsDo)
        }
    }

    private fun display_doc(url: String, sharedDocumentsDo: DocumentsModel) {
        val dialogBuilder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.view_documents, null)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_pdf)
        val iv_image = view.findViewById<ImageView>(R.id.doc_image)
        val idPDFView = view.findViewById<PDFView>(R.id.idPDFView)
        val webView = view.findViewById<WebView>(R.id.doc_webview)
        val header = view.findViewById<TextView>(R.id.header_name)
        val iv_close_edit_docs = view.findViewById<ImageView>(R.id.close_edit_docs)
        val isImage = File_Content_Type.isImage(sharedDocumentsDo.contentType)
        header.text = sharedDocumentsDo.name
        val dialog = dialogBuilder.create()
        val pdfTask = arrayOfNulls<RetrievePDFfromUrl>(1)

        iv_close_edit_docs.setOnClickListener {
            try {
                if (idPDFView != null) idPDFView.recycle()
                if (pdfTask[0] != null) {
                    pdfTask[0]?.cancelLoading()
                    pdfTask[0]?.cancel(true)
                }
            } catch (ignored: Exception) {
            }
            dialog.dismiss()
        }
        val lowerUrl = url.lowercase(Locale.getDefault())
        val urlIsPDF = lowerUrl.contains("application/pdf") || lowerUrl.contains(".pdf")
        if (urlIsPDF) {
            idPDFView.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            pdfTask[0] = RetrievePDFfromUrl(idPDFView, progressBar)
            pdfTask[0]?.execute(url)
        } else {
            if (isImage) {
                iv_image.visibility = View.VISIBLE
                Glide.with(requireContext())
                    .load(url)
                    .placeholder(R.drawable.progress_animation)
                    .centerCrop()
                    .into(iv_image)
            } else {
                idPDFView.visibility = View.VISIBLE
                progressBar.visibility = View.VISIBLE
                pdfTask[0] = RetrievePDFfromUrl(idPDFView, progressBar)
                pdfTask[0]?.execute(url)
            }
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setView(view)
        dialog.show()
    }

    private fun SelectedDocView() {
        if (selected_documents_list.isEmpty()) {
            ll_selected_documents?.removeAllViews()
            tv_selected_document?.visibility = View.GONE
        } else {
            tv_selected_document?.visibility = View.VISIBLE
        }
    }

    private fun UploadedDocView() {
        if (upload_documents_list.isEmpty()) {
            ll_uploaded_documents?.removeAllViews()
            ll_download?.visibility = View.GONE
            rl_buttons?.visibility = View.GONE
        } else {
            ll_download?.visibility = View.VISIBLE
            rl_buttons?.visibility = View.VISIBLE
        }
    }

    private fun loadMattersList(matters: JSONArray) {
        try {
            matterList.clear()
            for (i in 0 until matters.length()) {
                val jsonObject = matters.getJSONObject(i)
                val viewMatterModel = ViewMatterModel()
                viewMatterModel.id = jsonObject.getString("id")
                if (jsonObject.has("caseNumber")) {
                    viewMatterModel.caseNumber = jsonObject.getString("caseNumber")
                }
                if (jsonObject.has("caseType")) {
                    viewMatterModel.casetype = jsonObject.getString("caseType")
                }
                viewMatterModel.clients = jsonObject.optJSONArray("clients") ?: JSONArray()
                if (jsonObject.has("courtName")) {
                    viewMatterModel.courtName = jsonObject.optString("courtName")
                }
                if (jsonObject.has("date_of_filling")) {
                    viewMatterModel.date_of_filling = jsonObject.optString("date_of_filling")
                }
                if (jsonObject.has("closedate")) {
                    viewMatterModel.closedate = jsonObject.optString("closedate")
                }
                if (jsonObject.has("matterNumber")) {
                    viewMatterModel.matterNumber = jsonObject.optString("matterNumber")
                }
                if (jsonObject.has("matterType")) {
                    viewMatterModel.matterType = jsonObject.optString("matterType")
                }
                if (jsonObject.has("startdate")) {
                    viewMatterModel.startdate = jsonObject.optString("startdate")
                }
                if (jsonObject.has("timesheets")) {
                    viewMatterModel.timesheets = jsonObject.optJSONArray("timesheets") ?: JSONArray()
                }
                viewMatterModel.description = jsonObject.optString("description")
                viewMatterModel.documents = jsonObject.optJSONArray("documents") ?: JSONArray()
                viewMatterModel.groupAcls = jsonObject.optJSONArray("groupAcls") ?: JSONArray()
                viewMatterModel.groups = jsonObject.optJSONArray("groups") ?: JSONArray()
                if (jsonObject.has("hearingDateDetails")) {
                    viewMatterModel.hearingDateDetails = jsonObject.optJSONObject("hearingDateDetails") ?: JSONObject()
                }
                viewMatterModel.is_editable = jsonObject.optBoolean("is_editable")
                if (jsonObject.has("judges")) {
                    viewMatterModel.judges = jsonObject.optString("judges")
                }
                if (jsonObject.has("matterClosedDate")) {
                    viewMatterModel.matterClosedDate = jsonObject.optString("matterClosedDate")
                }
                viewMatterModel.members = jsonObject.optJSONArray("members") ?: JSONArray()
                if (jsonObject.has("nextHearingDate")) {
                    viewMatterModel.nextHearingDate = jsonObject.optString("nextHearingDate")
                }
                if (jsonObject.has("opponentAdvocates")) {
                    viewMatterModel.opponentAdvocates = jsonObject.optJSONArray("opponentAdvocates") ?: JSONArray()
                }
                viewMatterModel.owner = jsonObject.optJSONObject("owner") ?: JSONObject()
                viewMatterModel.priority = jsonObject.optString("priority")
                viewMatterModel.status = jsonObject.optString("status")
                val tagsObject = jsonObject.optJSONObject("tags")
                val tagsArray = JSONArray()
                if (tagsObject != null) {
                    val keys = tagsObject.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        try {
                            tagsArray.put(tagsObject.getString(key))
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
                viewMatterModel.tags_list = tagsArray
                if (jsonObject.has("tempClients")) {
                    viewMatterModel.tempClients = jsonObject.optJSONArray("tempClients") ?: JSONArray()
                }
                if (jsonObject.has("temporaryClients")) {
                    viewMatterModel.temporaryClients = jsonObject.optJSONArray("temporaryClients") ?: JSONArray()
                }
                viewMatterModel.title = jsonObject.getString("title")
                matterList.add(viewMatterModel)
            }
            loadMatterRecyclerview()
        } catch (e: JSONException) {
            AndroidUtils.showAlert(e.message, activity)
            e.printStackTrace()
        }
    }

    fun loadMatterRecyclerview() {
        try {
            if (context != null && rv_matter_list != null) {
                rv_matter_list?.removeAllViews()
                rv_matter_list?.layoutManager = GridLayoutManager(context, 1)
                val viewMatterAdapter = ViewMatterAdapter(matterList, requireContext(), this as ViewMatterAdapter.InterfaceListener)
                rv_matter_list?.adapter = viewMatterAdapter
                et_search_matter?.addTextChangedListener(object : TextWatcher {
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                    override fun afterTextChanged(s: Editable) {
                        viewMatterAdapter.filter.filter(s)
                    }
                })
            }
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    private fun open_add_tags_popup() {
        if (selected_upload_documents_list.isNotEmpty()) {
            tags_list.clear()
        }
        if (isUpdateTag || selected_upload_documents_list.isNotEmpty()) {
            val dialogBuilder = AlertDialog.Builder(requireContext())
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.add_tag, null)
            tv_tag_type = view.findViewById(R.id.tv_tag_type)
            tv_tag_name = view.findViewById(R.id.tv_tag_name)
            tag_type_name = view.findViewById(R.id.tag_type_name)
            tag_type_name?.setText(R.string.tag_type)
            tag_name = view.findViewById(R.id.tag_name)
            tag_name?.setText(R.string.tag)
            tv_tag_type?.setHint(R.string.tag_type)
            tv_tag_name?.setHint(R.string.tag)
            tv_tag_name?.addTextChangedListener(Validation(tv_tag_name))
            tv_tag_type?.addTextChangedListener(Validation(tv_tag_type))
            header_name = view.findViewById(R.id.header_name)
            if (isUpdateTag) {
                header_name?.setText(R.string.update_tag)
            } else {
                header_name?.setText(R.string.add_tag)
            }
            val btn_add = view.findViewById<Button>(R.id.btn_add_tags)
            btn_add.setText(R.string.add)
            filters = arrayOf(InputFilter.LengthFilter(30))
            filters1 = arrayOf(InputFilter.LengthFilter(100))
            tv_tag_type?.filters = filters
            tv_tag_name?.filters = filters1
            val btn_cancel = view.findViewById<AppCompatButton>(R.id.btn_cancel_tag)
            val btn_save_tag = view.findViewById<AppCompatButton>(R.id.btn_save_tag)
            val iv_cancel = view.findViewById<ImageView>(R.id.close_edit_docs)
            ll_added_tags = view.findViewById(R.id.ll_added_tags)
            if (isUpdateTag && tags_list.isNotEmpty()) {
                add_tags_listing()
            }
            val dialog = dialogBuilder.create()
            iv_cancel.setOnClickListener {
                if (tags_list.isNotEmpty()) AndroidUtils.Delete_Popup(requireActivity(), dialog) else dialog.dismiss()
            }
            btn_cancel.setOnClickListener {
                if (tags_list.isNotEmpty()) AndroidUtils.Delete_Popup(requireActivity(), dialog) else dialog.dismiss()
            }
            btn_save_tag.isEnabled = false
            btn_save_tag.alpha = 0.5f
            btn_add.setOnClickListener {
                if (tv_tag_type?.text.toString().isEmpty() && tv_tag_name?.text.toString().isEmpty()) {
                    AndroidUtils.showAlert("Please check the Tag Type, Tag", activity)
                } else if (tv_tag_type?.text.toString().isEmpty()) {
                    AndroidUtils.showAlert("Please check the Tag Type", activity)
                } else if (tv_tag_name?.text.toString().isEmpty()) {
                    AndroidUtils.showAlert("Please check the Tag", activity)
                } else {
                    if (isedit) {
                        save_edited_tags(tv_tag_type?.text.toString(), tv_tag_name?.text.toString())
                    } else {
                        add_tags_listing()
                    }
                    isedit = false
                    tv_tag_name?.setText("")
                    tv_tag_type?.setText("")
                    btn_save_tag.isEnabled = true
                    btn_save_tag.alpha = 1.0f
                }
            }
            btn_save_tag.setOnClickListener {
                if (tags_list.isNotEmpty()) {
                    if (!isUpdateTag) {
                        for (documentModel in selected_upload_documents_list) {
                            val combinedTags = JSONObject()
                            val existingTags = documentModel.tags
                            if (existingTags != null) {
                                val keys = existingTags.keys()
                                while (keys.hasNext()) {
                                    val key = keys.next()
                                    try {
                                        combinedTags.put(key, existingTags.get(key))
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            for (tagModel in tags_list) {
                                try {
                                    combinedTags.put(tagModel.tag_type, tagModel.tag_name)
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                            documentModel.tags = combinedTags
                        }
                        for (i in upload_documents_list.indices) {
                            for (selectedDocument in selected_upload_documents_list) {
                                if (upload_documents_list[i].name == selectedDocument.name) {
                                    upload_documents_list[i].tags = selectedDocument.tags
                                    upload_documents_list[i].isChecked = false
                                }
                            }
                        }
                        is_clicked_add = true
                        Hide_Add_EditMeta()
                        dialog.dismiss()
                    } else {
                        is_clicked_add = true
                        dialog.dismiss()
                    }
                }
            }
            dialog.setOnDismissListener {}
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setView(view)
            dialog.show()
        } else {
            AndroidUtils.showAlert("Please select atleast one document to add tags", activity)
        }
    }
    private fun add_tags_listing() {
        ll_added_tags?.removeAllViews()
        if (tv_tag_name?.text.toString().isNotEmpty() && tv_tag_type?.text.toString().isNotEmpty()) {
            val documentsModel = com.digicoffer.lauditor.Documents.Models.DocumentsModel()
            documentsModel.tag_type = tv_tag_type?.text.toString()
            documentsModel.tag_name = tv_tag_name?.text.toString()
            tags_list.add(documentsModel)
        }

        for (i in tags_list.indices) {
            val view_added_tags = LayoutInflater.from(context).inflate(R.layout.displays_documents_list, null)
            tv_tag_document_name = view_added_tags.findViewById(R.id.tv_document_name)
            val iv_edit_tag = view_added_tags.findViewById<ImageView>(R.id.iv_edit_meta)
            val iv_remove_tag = view_added_tags.findViewById<ImageView>(R.id.iv_cancel)
            iv_remove_tag.tag = i
            iv_remove_tag.setOnClickListener { view ->
                if (view.tag is Int) {
                    val position = view.tag as Int
                    val viewToRemove = ll_added_tags?.getChildAt(position)
                    ll_added_tags?.removeView(viewToRemove)
                    val documentsModel1 = tags_list[position]
                    documentsModel1.tag_name = ""
                    documentsModel1.tag_type = ""
                    tags_list[position] = documentsModel1
                    tags_list.removeAt(position)
                }
            }
            iv_edit_tag.tag = i
            iv_edit_tag.setOnClickListener { view ->
                if (view.tag is Int) {
                    val position = view.tag as Int
                    val documentsModel1 = tags_list[position]
                    if (documentsModel1 != null) {
                        edit_position = position
                        isedit = true
                        tv_tag_type?.setText(documentsModel1.tag_type)
                        tv_tag_name?.setText(documentsModel1.tag_name)
                    }
                }
            }
            iv_edit_tag.visibility = View.VISIBLE
            val tag_name = tags_list[i].tag_type + " - " + tags_list[i].tag_name
            tv_tag_document_name?.text = tag_name
            ll_added_tags?.addView(view_added_tags)
        }
    }

    private fun save_edited_tags(tag_type: String, tag_name: String) {
        try {
            if (edit_position < 0 || edit_position >= tags_list.size) {
                AndroidUtils.showAlert("Invalid position for editing tag.", activity)
                return
            }

            val documentsModel = tags_list[edit_position]
            documentsModel.tag_type = tag_type
            documentsModel.tag_name = tag_name
            tags_list[edit_position] = documentsModel

            val view_to_update = ll_added_tags?.getChildAt(edit_position)
            if (view_to_update != null) {
                val tv_edit_tag_document_name = view_to_update.findViewById<TextView>(R.id.tv_document_name)
                tv_edit_tag_document_name.text = "$tag_type - $tag_name"
            }

            if (tv_tag_name != null && tv_tag_type != null) {
                tv_tag_name?.setText("")
                tv_tag_type?.setText("")
            }
        } catch (e: Exception) {
            Log.e("save_edited_tags", "Error saving edited tag", e)
            AndroidUtils.showAlert("An error occurred while saving the tag: " + e.message, activity)
        }
    }

    private fun EditDocuments(name: String, description: String, file: File?, position: Int, v: View) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = requireActivity().layoutInflater
        val view_edit_documents = inflater.inflate(R.layout.edit_meta_data, null)
        val header_name = view_edit_documents.findViewById<TextView>(R.id.header_name)
        header_name.setText(R.string.edit_document)
        val iv_cancel_edit_doc = view_edit_documents.findViewById<ImageView>(R.id.close_edit_docs)
        val btn_close_edit_docs = view_edit_documents.findViewById<AppCompatButton>(R.id.btn_cancel_edit_docs)
        val tv_doc_name = view_edit_documents.findViewById<TextInputEditText>(R.id.edit_doc_name)
        val tv_description = view_edit_documents.findViewById<TextInputEditText>(R.id.edit_description)
        val tv_document_name = view_edit_documents.findViewById<TextView>(R.id.tv_document_name)
        tv_document_name.setText(R.string.document_name)
        val description1 = view_edit_documents.findViewById<TextView>(R.id.description)
        description1.setText(R.string.description)
        tv_doc_name.maxLines = 5
        filters = arrayOf(InputFilter.LengthFilter(50))
        filters1 = arrayOf(InputFilter.LengthFilter(300))
        tv_doc_name.filters = filters
        tv_description.maxLines = 10
        tv_description.filters = filters1

        tv_doc_name.addTextChangedListener(Validation(tv_doc_name))
        tv_description.addTextChangedListener(Validation(tv_description))
        val tv_exp_date = view_edit_documents.findViewById<AppCompatButton>(R.id.tv_expiration_date)
        tv_exp_date.setHint(R.string.expiration_date)
        val expiration_date_id = view_edit_documents.findViewById<TextView>(R.id.expiration_date_id)
        expiration_date_id.setText(R.string.expiration_date)
        tv_doc_name.setText(name)
        tv_description.setText(description)

        val btn_save_tag = view_edit_documents.findViewById<AppCompatButton>(R.id.btn_save_tag)
        val dialog = dialogBuilder.create()
        iv_cancel_edit_doc.setOnClickListener { handleEditDocClose(name, description, tv_doc_name, tv_description, dialog) }
        btn_close_edit_docs.setOnClickListener { handleEditDocClose(name, description, tv_doc_name, tv_description, dialog) }
        tv_exp_date.setOnClickListener {
            val myCalendar = Calendar.getInstance()
            val date = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "dd-MM-yyyy"
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                tv_exp_date.text = sdf.format(myCalendar.time)
            }
            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.setOnCancelListener { tv_exp_date.text = "" }
            val cancelButton = datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            if (cancelButton != null) {
                cancelButton.text = "Clear"
            }
            datePickerDialog.show()
        }
        btn_save_tag.setOnClickListener {
            try {
                val documentsModel = com.digicoffer.lauditor.Documents.Models.DocumentsModel()
                documentsModel.name = tv_doc_name.text.toString()
                documentsModel.description = tv_description.text.toString()
                documentsModel.expiration_date = tv_exp_date.text.toString()
                documentsModel.file = file
                upload_documents_list[position] = documentsModel
                dialog.dismiss()
                loadUploadedDocuments()
            } catch (e: Exception) {
                e.printStackTrace()
                AndroidUtils.showAlert(e.message, activity)
            }
        }
        dialog.setCancelable(false)
        dialog.setView(view_edit_documents)
        dialog.show()
    }

    private fun handleEditDocClose(name: String, description: String, tv_doc_name: TextView, tv_description: TextView, dialog: Dialog) {
        val docName = tv_doc_name.text.toString()
        val desc = tv_description.text.toString()

        if ((docName.isNotEmpty() && docName != name) || (desc.isNotEmpty() && desc != description)) {
            AndroidUtils.Delete_Popup(requireActivity(), dialog)
        } else {
            dialog.dismiss()
        }
    }

    private fun existing_document() {
        if (Constants.MATTER_TYPE == "Legal") {
            matter_type = "legal"
        } else if (Constants.MATTER_TYPE == "General") {
            matter_type = "general"
        }
        val postdata = JSONObject()
        WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "matter/$matter_type/${Constants.Matter_id}/documents", "Chosen_Documents", postdata.toString())
    }

    @Throws(JSONException::class)
    private fun document_list() {
        val postdata = JSONObject()
        postdata.put("attachment_type", "documents")
        WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.PUT, "matter/attachments", "Documents_List", postdata.toString())
    }

    @Throws(JSONException::class)
    private fun update_document() {
        progress_dialog = AndroidUtils.get_progress(activity)
        val documents = JSONArray()
        for (i in selected_documents_list.indices) {
            val jsonObject = JSONObject()
            val documentsModel = selected_documents_list[i]
            jsonObject.put("docid", documentsModel.docid)
            jsonObject.put("doctype", documentsModel.doctype)
            jsonObject.put("user_id", documentsModel.user_id)
            documents.put(jsonObject)
        }
        val postdata = JSONObject()
        postdata.put("documents", documents)
        WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.PUT, "matter/$matter_type/${Constants.Matter_id}/documents/update", "matter_update", postdata.toString())
    }

    fun check_select_all(check_status: Boolean) {
        chk_select_all?.isChecked = check_status
    }

    fun check_encrypted(check_encrypt: Boolean) {
        if (check_encrypt) {
            EnableEncryptionBackground()
        } else {
            DisableEncryptionBackground()
        }
    }

    fun check_enabled(check_encrypt: Boolean) {
        if (check_encrypt) {
            EnableDownloadBackground()
        } else {
            DisableDownloadBackground()
        }
    }

    override fun ViewTags(documentsModel: com.digicoffer.lauditor.Documents.Models.DocumentsModel, itemsArrayList: ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel>) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view_edit_tags = inflater.inflate(R.layout.edit_existing_tags, null)
        val ll_existing_tags = view_edit_tags.findViewById<LinearLayout>(R.id.ll_view_tags)
        val iv_close_existing_tags = view_edit_tags.findViewById<ImageView>(R.id.close_edit_docs)
        val header_name = view_edit_tags.findViewById<TextView>(R.id.header_name)
        val tv_document_name = view_edit_tags.findViewById<TextView>(R.id.tv_document_name)
        tv_document_name.textSize = DynamicUtils.twenty.toFloat()
        tv_document_name.visibility = View.VISIBLE
        tv_document_name.text = documentsModel.name
        header_name.setText(R.string.view_tags)
        val dialog = dialogBuilder.create()
        val tags = documentsModel.tags
        if (tags != null) {
            val iter = tags.keys()
            while (iter.hasNext()) {
                val key = iter.next()
                val value = tags.optString(key)
                val view_added_tags = inflater.inflate(R.layout.displays_documents_list, null)
                val tv_tag_name = view_added_tags.findViewById<TextView>(R.id.tv_document_name)
                val iv_remove_tag = view_added_tags.findViewById<ImageView>(R.id.iv_cancel)
                val tag_msg = "$key - $value"
                tv_tag_name.text = tag_msg
                iv_remove_tag.setOnClickListener {
                    ll_existing_tags.removeView(view_added_tags)
                    tags.remove(key)
                }
                ll_existing_tags.addView(view_added_tags)
            }
        }
        iv_close_existing_tags.setOnClickListener { dialog.dismiss() }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setView(view_edit_tags)
        dialog.show()
    }

    override fun EditDocuments(documentsModel: com.digicoffer.lauditor.Documents.Models.DocumentsModel, itemsArrayList: ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel>, position: Int) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = requireActivity().layoutInflater
        val view_edit_documents = inflater.inflate(R.layout.edit_meta_data, null)
        val response_docname = view_edit_documents.findViewById<TextView>(R.id.response_docname)
        val response_docdes = view_edit_documents.findViewById<TextView>(R.id.response_docdes)
        val iv_cancel_edit_doc = view_edit_documents.findViewById<ImageView>(R.id.close_edit_docs)
        val btn_close_edit_docs = view_edit_documents.findViewById<AppCompatButton>(R.id.btn_cancel_edit_docs)
        val tv_doc_name = view_edit_documents.findViewById<TextInputEditText>(R.id.edit_doc_name)
        val tv_document_name = view_edit_documents.findViewById<TextView>(R.id.tv_document_name)
        tv_document_name.setText(R.string.document_name)
        val tv_description = view_edit_documents.findViewById<TextInputEditText>(R.id.edit_description)
        val description = view_edit_documents.findViewById<TextView>(R.id.description)
        description.setText(R.string.description)
        filters = arrayOf(InputFilter.LengthFilter(50))
        filters1 = arrayOf(InputFilter.LengthFilter(300))
        tv_doc_name.filters = filters
        tv_description.filters = filters1
        tv_description.maxLines = 10
        val tv_exp_date = view_edit_documents.findViewById<AppCompatButton>(R.id.tv_expiration_date)
        tv_exp_date.setHint(R.string.expiration_date)
        val expiration_date_id = view_edit_documents.findViewById<TextView>(R.id.expiration_date_id)
        expiration_date_id.setText(R.string.expiration_date)
        tv_doc_name.setText(documentsModel.name)
        tv_description.setText(documentsModel.description)
        tv_exp_date.text = documentsModel.expiration_date
        tv_doc_name.addTextChangedListener(Validation(tv_doc_name))
        tv_description.addTextChangedListener(Validation(tv_description))

        tv_exp_date.setOnClickListener {
            val myCalendar = Calendar.getInstance()
            val date = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "dd-MM-yyyy"
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                tv_exp_date.text = sdf.format(myCalendar.time)
            }
            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.setOnCancelListener { tv_exp_date.text = "" }
            val cancelButton = datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            if (cancelButton != null) {
                cancelButton.text = "Clear"
            }
            datePickerDialog.show()
        }
        val btn_save_tag = view_edit_documents.findViewById<AppCompatButton>(R.id.btn_save_tag)
        Enable_Button(btn_save_tag, tv_doc_name.text.toString().isNotEmpty() && tv_description.text.toString().isNotEmpty())
        val dialog = dialogBuilder.create()
        iv_cancel_edit_doc.setOnClickListener {
            if (tv_doc_name.text.toString().isNotEmpty() || tv_description.text.toString().isNotEmpty()) {
                AndroidUtils.Delete_Popup(requireActivity(), dialog)
            } else dialog.dismiss()
        }
        btn_close_edit_docs.setOnClickListener {
            if (tv_doc_name.text.toString().isNotEmpty() || tv_description.text.toString().isNotEmpty()) {
                AndroidUtils.Delete_Popup(requireActivity(), dialog)
            } else dialog.dismiss()
        }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Enable_Button(btn_save_tag, tv_doc_name.text.toString().isNotEmpty() && tv_description.text.toString().isNotEmpty())
                if (tv_doc_name.text.toString().isEmpty()) {
                    response_docname.visibility = View.VISIBLE
                    response_docname.text = "Document Name is required"
                } else {
                    response_docname.visibility = View.GONE
                }
                if (tv_description.text.toString().isEmpty()) {
                    response_docdes.visibility = View.VISIBLE
                    response_docdes.text = "Description is required"
                } else {
                    response_docdes.visibility = View.GONE
                }
            }
        }
        tv_doc_name.addTextChangedListener(textWatcher)
        tv_description.addTextChangedListener(textWatcher)

        btn_save_tag.setOnClickListener {
            for (i in itemsArrayList.indices) {
                if (i == position) {
                    val documentsModel1 = itemsArrayList[i]
                    documentsModel1.name = tv_doc_name.text.toString()
                    documentsModel1.description = tv_description.text.toString()
                    documentsModel1.expiration_date = tv_exp_date.text.toString()
                    itemsArrayList[i] = documentsModel1
                    dialog.dismiss()
                    val tag = "edit_meta"
                    loadRecyclerview(tag, "")
                }
            }
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setView(view_edit_documents)
        dialog.show()
    }

    private fun Enable_Button(btn: Button, istrue: Boolean) {
        if (!istrue) {
            btn.isEnabled = false
            btn.alpha = 0.5f
        } else {
            btn.isEnabled = true
            btn.alpha = 1.0f
        }
    }

    override fun RemoveDocument(position: Int, tag: String) {
        if (position < 0 || position >= upload_documents_list.size) return
        upload_documents_list.removeAt(position)
        adapter?.notifyItemRemoved(position)
        adapter?.notifyItemRangeChanged(position, adapter?.itemCount ?: 0)
        Constants.upload_documents_list = upload_documents_list
        UploadedDocView()
    }

    companion object {
        fun getFile(context: Context, uri: Uri): File {
            val destinationFilename = File(context.filesDir.path + File.separatorChar + queryName(context, uri))
            try {
                context.contentResolver.openInputStream(uri).use { ins ->
                    createFileFromStream(ins!!, destinationFilename)
                }
            } catch (ex: Exception) {
                Log.e("Save File", ex.message!!)
                ex.printStackTrace()
            }
            return destinationFilename
        }

        fun createFileFromStream(ins: InputStream, destination: File?) {
            try {
                OutputStream.nullOutputStream().use { os ->
                    val buffer = ByteArray(4096)
                    var length: Int
                    while (ins.read(buffer).also { length = it } > 0) {
                        os.write(buffer, 0, length)
                    }
                    os.flush()
                }
            } catch (ex: Exception) {
                Log.e("Save File", ex.message!!)
                ex.printStackTrace()
            }
        }

        private fun queryName(context: Context, uri: Uri): String {
            val returnCursor = context.contentResolver.query(uri, null, null, null, null)
            val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            val name = returnCursor.getString(nameIndex)
            returnCursor.close()
            return name
        }

        fun getDocumentsModel(documentsModel: DocumentsModel): DocumentsModel {
            return documentsModel
        }
    }
}
