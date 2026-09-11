package com.digicoffer.lauditor.Matter.ViewModels

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
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
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.DescriptionValidation
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
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.switchmaterial.SwitchMaterial
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
import java.util.AbstractCollection
import java.util.ArrayList
import java.util.Calendar
import java.util.HashSet
import java.util.Iterator
import java.util.Locale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.feature.matter.presentation.screen.DocumentsScreen
import com.digicoffer.lauditor.feature.matter.presentation.viewmodel.MatterEditViewModel
import java.util.Objects

class MatterDocuments_En : Fragment(), AsyncTaskCompleteListener, DocumentsListAdapter.EventListener, View.OnClickListener, BottomSheetUploadFile.OnPhotoSelectedListner, ViewMatterAdapter.InterfaceListener {
    private var tv_added_tags: TextView? = null
    private var tv_selected_document: TextView? = null
    private var tv_tag_document_name: TextView? = null
    private var matter_date: TextView? = null
    private var tv_document_library: TextView? = null
    private var tv_device_drive: TextView? = null
    private var at_add_documents: TextView? = null
    private var add_groups: TextView? = null
    private var select_all: TextView? = null
    private var tv_enable_download: TextView? = null
    private var tv_disable_download: TextView? = null
    private var tv_multiple_doc: TextView? = null
    private var tv_selected_file: LinearLayout? = null
    private var llSelectedTags: LinearLayout? = null
    private var ll_added_tags: LinearLayout? = null
    private var ll_add_documents: LinearLayout? = null
    private var ll_selected_documents: LinearLayout? = null
    private var ll_select_doc: LinearLayout? = null
    private var ll_matterDate: LinearLayout? = null
    private var ll_select_file: LinearLayout? = null
    private var ll_uploaded_documents: RecyclerView? = null
    private var iv_remove_matter: ImageView? = null
    private var btn_browse: Button? = null
    private var btn_add_documents: Button? = null
    var rv_matter_list: RecyclerView? = null
    var isChangesOccured = false
    var dialog: AlertDialog? = null
    var rl_buttons: RelativeLayout? = null
    var subtag = ""
    var is_clicked_add = true
    var is_clicked_edit = true
    var adapter: DocumentsListAdapter? = null
    var chk_box_layout: LinearLayout? = null
    var ll_buttons: LinearLayout? = null
    var chk_select_all: CheckBox? = null
    private var totalUploads = 0
    private var completedUploads = 0
    var isselect_all_checked = true
    private var isAlertShown = false
    var isDocUploaded = false
    var matterModel = MatterModel()
    var filters: Array<InputFilter>? = null
    var filters1: Array<InputFilter>? = null
    var DOWNLOAD_TAG = false
    var isedit = false
    var ENCRYPTION_TAG = true
    var DECRYPTION_TAG = true
    var progressDialog: Dialog? = null
    var matter_id = ""
    var edit_position = 0
    var sharedDocumentsDo = DocumentsModel()
    private var mViewModel: NewModel? = null
    var matter_type = ""
    var documentsAdapter: DocumentsAdapter? = null
    var cv_client_details: CardView? = null
    var cv_add_opponent_advocate: CardView? = null
    var matter: Matter? = null
    var matterList = ArrayList<ViewMatterModel>()
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
    var isInitialLoad = true
    var search_matter: TextInputLayout? = null
    var tag_type_name: TextView? = null
    var tag_name: TextView? = null
    var header_name: TextView? = null
    var advocates_list = ArrayList<AdvocateModel>()
    private var imageView: ImageView? = null
    var selected_tm_list = ArrayList<TeamModel>()
    var ll_download: LinearLayoutCompat? = null
    var rv_display_upload_doc: RecyclerView? = null
    var selected_clients_list = ArrayList<ClientsModel>()
    var selected_temp_clients_list = ArrayList<ClientsModel>()
    var selected_corp_clients_list = ArrayList<ClientsModel>()
    var tags_list = ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel>()
    var bottommSheetUploadDocument: BottomSheetUploadFile? = null
    var btn_cancel_save: AppCompatButton? = null
    var btn_create: AppCompatButton? = null
    var btn_add_tags: AppCompatButton? = null
    var selectedDocument: BooleanArray? = null
    var corp_client_id = ""
    private var mSelectedBitmap: Bitmap? = null
    var cl_matter_document: ConstraintLayout? = null
    var matterArraylist: ArrayList<MatterModel>? = null
    private var mSelectedUri: File? = null
    var ADAPTER_TAG = "Documents"
    var documentsList = ArrayList<DocumentsModel>()
    var selected_documents_list = ArrayList<DocumentsModel>()
    var selected_upload_documents_list = ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel>()
    var tempSelectedDocuments = ArrayList<DocumentsModel>()
    var new_selected_doc = ArrayList<DocumentsModel>()
    var upload_documents_list = ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel>()
    var exisiting_group_acls: JSONArray? = null
    var existing_documents: JSONArray? = null
    var existing_corp_clients: JSONArray? = null
    var existing_temp_clients: JSONArray? = null
    var existing_documents_list: JSONArray? = null
    var et_search_matter: TextInputEditText? = null
    var ischecked_doc = true
    var existing_clients: JSONArray? = null
    var existing_members: JSONArray? = null
    var existing_groups_list: JSONArray? = null
    var existing_clients_list: JSONArray? = null
    var existing_tm_list: JSONArray? = null
    var selected_groups_list = ArrayList<GroupsModel>()
    private var progress_dialog: Dialog? = null
    var filename: String? = null
    var matter_title_tv: TextView? = null
    var tv_enable_encryption: TextView? = null
    var tv_disable_encryption: TextView? = null
    var tv_add_tag: TextView? = null
    var tv_edit_meta: TextView? = null
    var ll_upload_type: LinearLayoutCompat? = null
    var file: File? = null
    var isUpdateTag = false
    var viewMatterModel1: ViewMatterModel? = null
    var viewmatter: ViewMatter? = null
    private var groupsList = ArrayList<GroupsModel>()
    private var clientsList = ArrayList<ClientsModel>()
    private var tmList = ArrayList<TeamModel>()
    private var uploaded_document_name: String? = null
    private var upload_description: String? = null
    private var upload_exp_date: String? = null
    private var MergedList: AbstractCollection<DocumentsModel>? = null
    private var changedCollection = 0
    private var tag_list = ArrayList<String>()
    private var existing_tags_list: JSONArray? = null
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        matter = parentFragment as? Matter
        matterArraylist = matter?.matter_arraylist
        
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
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
                    DocumentsScreen(
                        viewModel = viewModel,
                        onBrowseClick = {
                            showPhotoOptions()
                        },
                        onViewDocument = { doc ->
                            val isEncrypted = doc.isAdded_encryption || doc.isIs_encrypted
                            val matterDoc = com.digicoffer.lauditor.Matter.Models.DocumentsModel().apply {
                                this.docid = doc.docid
                                this.name = doc.name
                                this.user_id = doc.user_id
                                this.doctype = doc.doctype
                                this.contentType = doc.contentType
                                this.viewUrl = doc.viewUrl
                                this.isIs_encrypted = doc.isIs_encrypted
                                this.isIs_password = doc.isIs_password
                                this.isAdded_encryption = doc.isAdded_encryption
                            }
                            if (isEncrypted) {
                                callDecryptApi(matterDoc.docid)
                            } else {
                                val url = Constants.base_URL + "v3/document/" + matterDoc.docid + "/view"
                                checkViewType(url, matterDoc)
                            }
                        },
                        onCancel = {
                            val parent = matter
                            parent?.loadViewUI()
                        }
                    )
                }
            }
        }
    }

    private fun handleLeaveClick() {
        val shouldWarn: Boolean
        if (Constants.create_matter) {
            shouldWarn = selected_documents_list.isNotEmpty() || upload_documents_list.isNotEmpty()
        } else {
            shouldWarn = isChangesOccured
        }
        if (shouldWarn) {
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
        } else {
            matter?.loadViewUI()
        }
    }

    private fun AddTag() {
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
        isselect_all_checked = true
        btn_create?.visibility = View.VISIBLE
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
        isselect_all_checked = true
        btn_create?.visibility = View.VISIBLE
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

    private fun Disable_allEncryptionBackground() {
        tv_enable_encryption?.background = requireContext().resources.getDrawable(R.drawable.button_left_background, null)
        tv_enable_encryption?.setTextColor(requireContext().getColor(R.color.black))
        tv_disable_encryption?.setTextColor(requireContext().getColor(R.color.white))
        tv_disable_encryption?.background = requireContext().resources.getDrawable(R.drawable.button_right_green_background, null)
        val tag = "dis_encrption"
        loadRecyclerview(tag, subtag)
    }

    private fun Enable_allEncryptionBackground() {
        tv_enable_encryption?.background = requireContext().resources.getDrawable(R.drawable.button_left_green_background, null)
        tv_enable_encryption?.setTextColor(requireContext().getColor(R.color.white))
        tv_disable_encryption?.setTextColor(requireContext().getColor(R.color.black))
        tv_disable_encryption?.background = requireContext().resources.getDrawable(R.drawable.button_right_background, null)
        val tag = "en_encrption"
        loadRecyclerview(tag, subtag)
    }

    private fun Disable_AllDownloadBackground() {
        tv_enable_download?.background = requireContext().resources.getDrawable(R.drawable.button_left_background, null)
        tv_enable_download?.setTextColor(requireContext().getColor(R.color.black))
        tv_disable_download?.setTextColor(requireContext().getColor(R.color.white))
        tv_disable_download?.background = requireContext().resources.getDrawable(R.drawable.button_right_green_background, null)
        val tag = "disable_download"
        loadRecyclerview(tag, subtag)
    }

    private fun Enable_ALlDownloadBackground() {
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
            if (item.isIsenabled) {
                allDisabled = false
            } else {
                allEnabled = false
            }
            if (!allDisabled && !allEnabled) break
        }

        if (allEnabled) {
            check_enabled_all(true)
        } else if (allDisabled) {
            check_enabled_all(false)
        }
    }

    fun checkEnableEncryption() {
        var allEnabled = true
        var allDisabled = true

        for (item in upload_documents_list) {
            if (item.isencrypted == true) {
                allDisabled = false
            } else {
                allEnabled = false
            }
            if (!allDisabled && !allEnabled) break
        }

        if (allEnabled) {
            check_encrypted_all(true)
        } else if (allDisabled) {
            check_encrypted_all(false)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_document_library -> loadDocumentLibraryUI()
            R.id.tv_device_drive -> loadDeviceDriveUI()
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
                            val matter_arr = JSONArray()
                            var docname = ""
                            val documentsModel = upload_documents_list[i]
                            filename = documentsModel.name
                            val new_file = documentsModel.file
                            val isenabled = documentsModel.isIsenabled
                            var doc_type = "pdf"
                            val content_string = new_file?.name?.replace(".", "/") ?: ""
                            val content_type_arr = content_string.split("/".toRegex()).toTypedArray()
                            if (content_type_arr.size >= 2) {
                                doc_type = content_type_arr[1]
                                docname = content_type_arr[0]
                            }

                            val matters = JSONArray()
                            matters.put(Constants.Matter_id)
                            val jsonObject = JSONObject()
                            uploaded_document_name = name
                            upload_description = upload_documents_list[i].description
                            upload_exp_date = upload_documents_list[i].expiration_date
                            jsonObject.put("name", name)
                            jsonObject.put("description", upload_description)
                            jsonObject.put("expiration_date", AndroidUtils.convertAnyDateToDDMMYYYY(upload_exp_date))
                            jsonObject.put("filename", docname)
                            jsonObject.put("matters", matters)
                            jsonObject.put("category", "client")
                            if (Constants.clientList.length() > 0) {
                                jsonObject.put("clients", Constants.clientList)
                            } else {
                                jsonObject.put("clients", Constants.corpclientList)
                            }
                            jsonObject.put("groups", Constants.ex_group_attachment)
                            jsonObject.put("downloadDisabled", upload_documents_list[i].isIsenabled)
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
                        if (progress_dialog != null && progress_dialog!!.isShowing) AndroidUtils.dismiss_dialog(progress_dialog)
                        e.fillInStackTrace()
                    }
                } else {
                    submitMatter()
                }
            } catch (e: Exception) {
                if (progress_dialog != null && progress_dialog!!.isShowing) AndroidUtils.dismiss_dialog(progress_dialog)
                e.fillInStackTrace()
            }
        }
    }

    var requestPermissions: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results: Map<String, Boolean> ->
            val permissionGranted = java.lang.Boolean.TRUE == results[READ_EXTERNAL_STORAGE]
            if (permissionGranted) {
                val intent = Intent()
                intent.type = "*/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 200)
            } else {
                AndroidUtils.showAlert("Permission denied", activity)
            }
        }

    private fun checkPermissionREAD_EXTERNAL_STORAGE(context: Context?) {
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context, READ_EXTERNAL_STORAGE)
                } else {
                    requestPermissions.launch(arrayOf(READ_EXTERNAL_STORAGE))
                }
            } else {
                val intent = Intent()
                intent.type = "*/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 200)
            }
        } else {
            val intent = Intent()
            intent.type = "*/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 200)
        }
    }

    private fun showDialog(msg: String, context: Context?, permission: String) {
        val alertBuilder = AlertDialog.Builder(context)
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle("Permission necessary")
        alertBuilder.setMessage("$msg permission is necessary")
        alertBuilder.setPositiveButton(android.R.string.yes) { _, _ ->
            requestPermissions.launch(arrayOf(permission))
        }
        val alert = alertBuilder.create()
        alert.show()
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
            e.fillInStackTrace()
        }
    }

    private fun loadDeviceDriveUI() {
        showPhotoOptions()
    }

    private fun showPhotoOptions() {
        cl_matter_document?.alpha = 0.5f
        bottommSheetUploadDocument = BottomSheetUploadFile(cl_matter_document)
        bottommSheetUploadDocument?.setOnPhotoSelectedListener(this@MatterDocuments_En)
        bottommSheetUploadDocument?.setTargetFragment(this@MatterDocuments_En, 1)
        bottommSheetUploadDocument?.show(parentFragmentManager, "BottomSheetUploadFile")
    }

    private fun loadDocumentLibraryUI() {
        isDocUploaded = true
        ll_uploaded_documents?.removeAllViews()
        ll_uploaded_documents?.visibility = View.GONE
        DocumentsText()
        loadSelectedDocuments(Array(selected_documents_list.size) { "" })
        tv_document_library?.background = requireContext().resources.getDrawable(R.drawable.button_left_green_background, null)
        tv_document_library?.setTextColor(Color.WHITE)
        tv_device_drive?.background = requireContext().resources.getDrawable(R.drawable.button_right_background, null)
        tv_device_drive?.setTextColor(Color.BLACK)
        ll_add_documents?.visibility = View.VISIBLE
        ll_select_doc?.visibility = View.GONE
    }

    @SuppressLint("Range")
    override fun getImagepath(imagepath: File?, ImageURI: Uri?) {
        try {
            val viewModel = ViewModelProvider(requireParentFragment()).get(MatterEditViewModel::class.java)
            if (imagepath != null && imagepath.exists()) {
                val name = if (ImageURI != null) queryName(requireContext(), ImageURI) else imagepath.name
                viewModel.addUploadFile(imagepath, name)
            } else if (ImageURI != null) {
                val file = getFile(requireContext(), ImageURI)
                val name = queryName(requireContext(), ImageURI)
                viewModel.addUploadFile(file, name)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cl_matter_document?.alpha = 1.0f
        bottommSheetUploadDocument?.dismiss()
    }

    private fun load_documents(file_name: String, file: File) {
        val viewModel = ViewModelProvider(requireParentFragment()).get(MatterEditViewModel::class.java)
        viewModel.addUploadFile(file, file_name)
    }

    fun getFile(context: Context, uri: Uri): File {
        val destinationFilename = File(context.filesDir.path + File.separatorChar + queryName(context, uri))
        try {
            context.contentResolver.openInputStream(uri).use { ins ->
                if (ins != null) {
                    createFileFromStream(ins, destinationFilename)
                }
            }
        } catch (ex: Exception) {
            Log.e("Save File", ex.message ?: "")
            ex.fillInStackTrace()
        }
        return destinationFilename
    }

    fun createFileFromStream(ins: InputStream, destination: File?) {
        try {
            Files.newOutputStream(destination!!.toPath()).use { os ->
                val buffer = ByteArray(4096)
                var length: Int
                while (ins.read(buffer).also { length = it } > 0) {
                    os.write(buffer, 0, length)
                }
                os.flush()
            }
        } catch (ex: Exception) {
            Log.e("Save File", ex.message ?: "")
            ex.fillInStackTrace()
        }
    }

    private fun queryName(context: Context, uri: Uri): String {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        if (returnCursor != null && returnCursor.moveToFirst()) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val name = returnCursor.getString(nameIndex)
            returnCursor.close()
            return name
        }
        return uri.lastPathSegment ?: "file"
    }

    override fun getImageBitmap(bitmap: Bitmap?) {
        if (bitmap != null) {
            imageView?.setImageBitmap(bitmap)
            mSelectedBitmap = bitmap
            mSelectedUri = null
            val filesDir = requireContext().filesDir
            val imageFile = File(filesDir, "bitmap.jpg")
            try {
                val os: OutputStream = Files.newOutputStream(imageFile.toPath())
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                os.flush()
                os.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            file = imageFile
            val documentsModel = DocumentsModel()
            documentsModel.name = file!!.name
            selected_documents_list.add(documentsModel)
            tempSelectedDocuments.add(documentsModel)
        }
    }

    override fun View_Details(viewMatterModel: ViewMatterModel, itemsArrayList: ArrayList<ViewMatterModel>) {}
    override fun DeleteMatter(viewMatterModel: ViewMatterModel, itemsArrayList: ArrayList<ViewMatterModel>) {}
    override fun Edit_Matter_Info(viewMatterModel: ViewMatterModel) {}
    override fun Update_Group(viewMatterModel: ViewMatterModel) {}
    override fun Close_Matter(viewMatterModel: ViewMatterModel) {}
    override fun ReopenMatter(viewMatterModel: ViewMatterModel) {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            val selectedImage = data?.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = requireContext().contentResolver.query(
                selectedImage!!,
                filePathColumn, null, null, null
            )!!
            cursor.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()
            file = File(picturePath)
            filename = file!!.name
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            123 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do your stuff
            } else {
                AndroidUtils.showAlert("GET_ACCOUNTS Denied", activity)
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    fun callDecryptApi(id: String?) {
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
        val isEncrypted = docModel.isAdded_encryption || docModel.isIs_encrypted

        if (isEncrypted) {
            dialog?.dismiss()
            callDecryptApi(docModel.docid)
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
            callOtherDocViewApi(docModel.docid)
        }
    }

    fun callOtherDocViewApi(id: String?) {
        try {
            progress_dialog = AndroidUtils.get_progress(activity)
            val jsonObject = JSONObject()
            jsonObject.put("docid", id)
            jsonObject.put("download", false)
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, Constants.base_URL + "v3/document/" + id + "/view", "Other Doc View", jsonObject.toString())
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                if (httpResult.requestType == "Upload Document") {
                    val result = JSONObject(httpResult.responseContent)
                    completedUploads++
                    val isError = result.optBoolean("error")
                    val msg = result.optString("msg")
                    if (!isError) {
                        val docid = result.getString("docid")
                        addDocsToMatter(docid)
                    }
                    if (completedUploads == totalUploads && !isAlertShown) {
                        isAlertShown = true
                        if (progress_dialog != null && progress_dialog!!.isShowing) AndroidUtils.dismiss_dialog(progress_dialog)
                        if (isError) {
                            AndroidUtils.showAlert(msg, activity)
                        } else {
                            update_document()
                            matterArraylist?.clear()
                            matter?.loadViewUI()
                        }
                    }
                } else {
                    if (progress_dialog != null && progress_dialog!!.isShowing) AndroidUtils.dismiss_dialog(progress_dialog)
                    val result = JSONObject(httpResult.responseContent)
                    if (httpResult.requestType == "Documents") {
                        val data = result.getJSONArray("documents")
                        loadDocumentsData(data)
                    } else if (httpResult.requestType == "Update Members") {
                        val isError = result.optBoolean("error")
                        val msg = result.optString("msg")
                        if (isError) {
                            AndroidUtils.showAlert(msg, activity)
                        }
                    } else if (httpResult.requestType == "Documents_List") {
                        val data = result.getJSONArray("documents")
                        loadDocumentsData(data)
                    } else if (httpResult.requestType == "matter_update") {
                        val msg = result.getString("msg")
                        AndroidUtils.showAlert("" + msg, activity, "Success")
                        matter?.loadViewUI()
                    } else if (httpResult.requestType == "Update Documents List") {
                        matterArraylist?.clear()
                        matter?.loadViewUI()
                        if (Constants.ROLE != "TM") {
                            if (selected_temp_clients_list.isNotEmpty()) {
                                triggerUpdateMembersForTempClients()
                            }
                        }
                    } else if (httpResult.requestType == "Chosen_Documents") {
                        val data = result.getJSONArray("documents")
                        display_doc(data)
                        if (selected_documents_list.isNotEmpty()) {
                            DocumentsText()
                            loadSelectedDocuments(Array(selected_documents_list.size) { "" })
                        }
                        isInitialLoad = false
                        isChangesOccured = false
                    } else if (httpResult.requestType == "Decrypt Doc" || httpResult.requestType == "Other Doc View") {
                        val error = result.getBoolean("error")
                        if (!error) {
                            val url = result.getJSONObject("data").getString("url")
                            display_doc(url, sharedDocumentsDo)
                            Log.d("TAG_Image", url)
                        } else {
                            val msg = result.optString("msg", "")
                            AndroidUtils.showAlert(msg, activity)
                        }
                    } else if (httpResult.requestType == "View Doc") {
                        val error = result.getBoolean("error")
                        if (!error) {
                            val url = result.getJSONObject("data").getString("url")
                            checkViewType(url, sharedDocumentsDo)
                            Log.d("TAG_Image", url)
                        } else {
                            val msg = result.optString("msg", "")
                            AndroidUtils.showAlert(msg, activity)
                        }
                    } else if (httpResult.requestType == "Matter List") {
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
                                e.fillInStackTrace()
                            }
                        }
                    } else if (httpResult.requestType == "Create Matter") {
                        val error = result.getBoolean("error")
                        val msg = result.getString("msg")
                        if (error) {
                            AndroidUtils.showAlert(msg, activity)
                        } else {
                            AndroidUtils.showAlert(msg, activity, "")
                            matter_id = result.getString("matter_id")
                            matterArraylist?.clear()
                            matter?.loadViewUI()
                            if (selected_documents_list.isNotEmpty()) UpdateDocWebservice()
                        }
                    }
                }
            } catch (e: JSONException) {
                e.fillInStackTrace()
                if (progress_dialog != null && progress_dialog!!.isShowing) AndroidUtils.dismiss_dialog(progress_dialog)
            }
        } else if (httpResult.result == WebServiceHelper.ServiceCallStatus.Failed) {
            if (progress_dialog != null && progress_dialog!!.isShowing) AndroidUtils.dismiss_dialog(progress_dialog)
            try {
                val result = JSONObject(httpResult.responseContent)
                if (result.optBoolean("error")) {
                    AndroidUtils.showErrorAlert(result.optString("msg"), activity)
                }
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        } else {
            if (progress_dialog != null && progress_dialog!!.isShowing) AndroidUtils.dismiss_dialog(progress_dialog)
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
            e.fillInStackTrace()
        }
    }

    private fun triggerUpdateMembersForTempClients() {
        for (i in selected_temp_clients_list.indices) {
            val tempClientId = selected_temp_clients_list[i].rel_id ?: ""
            callUpdateMembers(tempClientId)
        }
    }

    private fun callUpdateMembers(id: String?) {
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
            if (selected_corp_clients_list.isNotEmpty()) corp_client_id = selected_corp_clients_list[0].client_id ?: ""
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
            val tagsList = JSONArray()
            for (i in tag_list.indices) {
                tagsList.put(tag_list[i])
            }
            existing_tags_list = tagsList
            matterModel.tags_list = tagsList
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
            Log.d("Matter _Creation", "" + postdata)
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.POST, "matter/$Matter_type/create", "Create Matter", postdata.toString())
        } catch (e: JSONException) {
            e.fillInStackTrace()
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
            e.fillInStackTrace()
        }
    }

    private fun display_doc(existing_documents: JSONArray) {
        try {
            new_selected_doc.clear()
            selected_documents_list.clear()
            tempSelectedDocuments.clear()
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
                documentsModel.isIs_encrypted = jsonObject.optBoolean("is_encrypted")
                documentsModel.isIs_password = jsonObject.optBoolean("is_password")
                documentsModel.isAdded_encryption = jsonObject.optBoolean("added_encryption")
                selected_documents_list.add(documentsModel)
                tempSelectedDocuments.add(documentsModel)
                new_selected_doc.add(documentsModel)
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
    }

    private fun loadMattersList(matters: JSONArray) {
        matterList.clear()
        try {
            for (i in 0 until matters.length()) {
                val jsonObject = matters.getJSONObject(i)
                val viewMatterModel = ViewMatterModel()
                viewMatterModel.id = jsonObject.optString("id")
                viewMatterModel.title = jsonObject.optString("title")
                viewMatterModel.status = jsonObject.optString("status")
                matterList.add(viewMatterModel)
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun checkViewType(url: String, sharedDocumentsDo: DocumentsModel) {
        val isImage = File_Content_Type.isImage(sharedDocumentsDo.contentType)
        val isPDF = File_Content_Type.isPDF(sharedDocumentsDo.contentType)
        if (isImage || isPDF) {
            display_doc(url, sharedDocumentsDo)
        } else {
            callOtherDocViewApi(sharedDocumentsDo.docid)
        }
    }

    fun display_doc(url: String, sharedDocumentsDo: DocumentsModel) {
        val dialogBuilder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.view_documents, null)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_pdf)
        val iv_image = view.findViewById<ImageView>(R.id.doc_image)
        val idPDFView = view.findViewById<PDFView>(R.id.idPDFView)
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

    private fun loadDocumentsData(data: JSONArray) {
        try {
            documentsList.clear()
            for (i in 0 until data.length()) {
                val jsonObject = data.getJSONObject(i)
                val documentsModel = DocumentsModel()
                documentsModel.docid = jsonObject.optString("docid")
                documentsModel.name = jsonObject.optString("name")
                documentsModel.doctype = jsonObject.optString("doctype")
                documentsModel.user_id = jsonObject.optString("user_id")
                documentsModel.contentType = jsonObject.optString("contentType")
                documentsModel.viewUrl = jsonObject.optString("viewUrl")
                documentsModel.isIs_encrypted = jsonObject.optBoolean("is_encrypted")
                documentsModel.isIs_password = jsonObject.optBoolean("is_password")
                documentsModel.isAdded_encryption = jsonObject.optBoolean("added_encryption")
                documentsList.add(documentsModel)
            }
            if (documentsList.isEmpty()) {
                AndroidUtils.showAlert("No documents found for selected combinations.", requireActivity())
            } else {
                if (!Constants.create_matter) {
                    val iter = documentsList.iterator()
                    while (iter.hasNext()) {
                        val documentsModel = iter.next()
                        for (i in selected_documents_list.indices) {
                            val documentsModel1 = selected_documents_list[i]
                            if (documentsModel1.docid == documentsModel.docid) {
                                iter.remove()
                                break
                            }
                        }
                    }
                }
                DocumentsPopUp()
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
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
                        documentsList[i].isChecked = true
                    }
                }
            }

            val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            rv_display_upload_doc?.layoutManager = layoutManager
            documentsAdapter = DocumentsAdapter(documentsList, this, btn_create)
            rv_display_upload_doc?.adapter = documentsAdapter

            btn_add_documents?.setOnClickListener {
                selected_documents_list.clear()
                selected_documents_list.addAll(tempSelectedDocuments)

                DocumentsText()
                val value = arrayOfNulls<String>(selected_documents_list.size)
                loadSelectedDocuments(value)
                rv_display_upload_doc?.visibility = View.GONE
                ischecked_doc = true

                if (!Constants.create_matter && !isInitialLoad) {
                    isChangesOccured = true
                }
                Add_Documents()
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    fun Add_Documents() {
        if (upload_documents_list.isNotEmpty()) {
            if (!isInitialLoad) {
                isChangesOccured = true
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
            e.fillInStackTrace()
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
            jsonObject.put("is_encrypted", documentsModel.isIs_encrypted)
            jsonObject.put("is_password", documentsModel.isIs_password)
            jsonObject.put("added_encryption", documentsModel.isAdded_encryption)
            documents.put(jsonObject)
        }
        return documents
    }

    private fun loadSelectedDocuments(value: Array<String?>) {
        try {
            ll_select_doc?.visibility = View.VISIBLE
            ll_selected_documents?.removeAllViews()
            for (i in selected_documents_list.indices) {
                val view_groups = LayoutInflater.from(context).inflate(R.layout.displays_documents_list, null)
                val tv_group_name = view_groups.findViewById<TextView>(R.id.tv_document_name)
                val iv_cancel = view_groups.findViewById<ImageView>(R.id.iv_cancel)
                val iv_view = view_groups.findViewById<ImageView>(R.id.iv_view)
                iv_view.visibility = View.VISIBLE
                iv_view.setOnClickListener {
                    try {
                        val documentsModel = selected_documents_list[i]
                        val isEncrypted = documentsModel.isAdded_encryption || documentsModel.isIs_encrypted
                        if (isEncrypted) {
                            callDecryptApi(documentsModel.docid)
                        } else {
                            val url = Constants.base_URL + "v3/document/" + documentsModel.docid + "/view"
                            checkViewType(url, documentsModel)
                        }
                    } catch (e: Exception) {
                        e.fillInStackTrace()
                    }
                }
                tv_group_name.text = selected_documents_list[i].name
                iv_cancel.setOnClickListener {
                    try {
                        isChangesOccured = true
                        selected_documents_list.removeAt(i)
                        tempSelectedDocuments.clear()
                        tempSelectedDocuments.addAll(selected_documents_list)
                        val value1 = arrayOfNulls<String>(selected_documents_list.size)
                        DocumentsText()
                        if (selected_documents_list.isNotEmpty()) {
                            loadSelectedDocuments(value1)
                        } else {
                            ll_select_doc?.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        e.fillInStackTrace()
                    }
                }
                ll_selected_documents?.addView(view_groups)
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun UploadedDocView() {
        if (upload_documents_list.isNotEmpty()) {
            rv_display_upload_doc?.visibility = View.VISIBLE
            rl_buttons?.visibility = View.VISIBLE
            ll_buttons?.visibility = View.VISIBLE
            ll_upload_type?.visibility = View.VISIBLE
        } else {
            rv_display_upload_doc?.visibility = View.GONE
            rl_buttons?.visibility = View.GONE
            ll_buttons?.visibility = View.GONE
            ll_upload_type?.visibility = View.GONE
        }
    }

    private fun loadUploadedDocuments() {
        try {
            UploadedDocView()
            chk_select_all?.isChecked = false
            AndroidUtils.ToggleButton(upload_documents_list.size, btn_add_tags)
            val tag = "load_documents"
            loadRecyclerview(tag, subtag)
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun loadRecyclerview(tag: String, subtag: String) {
        adapter = DocumentsListAdapter(upload_documents_list, tag, subtag, this, this)
        rv_display_upload_doc?.layoutManager = GridLayoutManager(context, 1)
        rv_display_upload_doc?.adapter = adapter
        rv_display_upload_doc?.setHasFixedSize(true)
    }

    fun checkSwitchState(switchMaterial: SwitchMaterial) {
        val context = switchMaterial.context
        val trackColor: Int
        val thumbColor: Int

        if (switchMaterial.isChecked) {
            trackColor = ContextCompat.getColor(context, R.color.green)
            thumbColor = ContextCompat.getColor(context, R.color.white)
        } else {
            trackColor = ContextCompat.getColor(context, R.color.light_grey)
            thumbColor = ContextCompat.getColor(context, R.color.white)
        }
        switchMaterial.trackTintList = android.content.res.ColorStateList.valueOf(trackColor)
        switchMaterial.thumbTintList = android.content.res.ColorStateList.valueOf(thumbColor)
    }

    private fun open_add_tags_popup() {
        if (selected_upload_documents_list.isEmpty()) {
            AndroidUtils.showAlert("Please select at least one document.", requireActivity())
            return
        }

        tags_list.clear()
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = requireActivity().layoutInflater
        val view_add_tags = inflater.inflate(R.layout.add_tag, null)
        val iv_cancel_add_tag = view_add_tags.findViewById<ImageView>(R.id.close_edit_docs)
        val btn_cancel_add_tags = view_add_tags.findViewById<AppCompatButton>(R.id.btn_cancel_tag)
        val btn_save_tag = view_add_tags.findViewById<AppCompatButton>(R.id.btn_save_tag)

        llSelectedTags = view_add_tags.findViewById(R.id.ll_added_tags)

        tv_tag_type = view_add_tags.findViewById(R.id.tv_tag_type)
        tv_tag_name = view_add_tags.findViewById(R.id.tv_tag_name)
        tag_type_name = view_add_tags.findViewById(R.id.tag_type_name)
        tag_type_name?.setText(R.string.tag_type)
        val tag_name = view_add_tags.findViewById<TextView>(R.id.tag_name)
        tag_name?.setText(R.string.tag)
        header_name = view_add_tags.findViewById(R.id.header_name)
        header_name?.setText(R.string.add_tag)

        val dialog = dialogBuilder.create()
        val btn_add_tags1 = view_add_tags.findViewById<Button>(R.id.btn_add_tags)
        
        btn_add_tags1.setOnClickListener {
            val tagType = tv_tag_type?.text.toString().trim()
            val tagName = tv_tag_name?.text.toString().trim()
            if (tagType.isEmpty() && tagName.isEmpty()) {
                AndroidUtils.showAlert("Please check the Tag Type, Tag", requireActivity())
            } else if (tagType.isEmpty()) {
                AndroidUtils.showAlert("Please check the Tag Type", requireActivity())
            } else if (tagName.isEmpty()) {
                AndroidUtils.showAlert("Please check the Tag", requireActivity())
            } else {
                tv_tag_type?.text?.clear()
                tv_tag_name?.text?.clear()

                val model = com.digicoffer.lauditor.Documents.Models.DocumentsModel()
                model.tag_type = tagType
                model.tag_name = tagName
                tags_list.add(model)
                renderSelectedTags(context, llSelectedTags, tags_list, null, null, activity)
            }
        }

        btn_save_tag.setOnClickListener {
            save_edited_tags()
            val tagType = tv_tag_type?.text.toString().trim()
            val tagName = tv_tag_name?.text.toString().trim()
            
            if (tags_list.isEmpty() && tagType.isEmpty() && tagName.isEmpty()) {
                AndroidUtils.showAlert("Please add Tag type and Tag name", requireActivity())
            } else if (tagType.isNotEmpty() && tagName.isEmpty()) {
                AndroidUtils.showAlert("Please add Tag name", requireActivity())
            } else if (tagType.isEmpty() && tagName.isNotEmpty()) {
                AndroidUtils.showAlert("Please add Tag type", requireActivity())
            } else {
                if (tagType.isNotEmpty() && tagName.isNotEmpty()) {
                    val model = com.digicoffer.lauditor.Documents.Models.DocumentsModel()
                    model.tag_type = tagType
                    model.tag_name = tagName
                    tags_list.add(model)
                    save_edited_tags()
                }
                tv_tag_type?.text?.clear()
                tv_tag_name?.text?.clear()
                dialog.dismiss()
            }
        }

        iv_cancel_add_tag.setOnClickListener {
            val tagType = tv_tag_type?.text.toString().trim()
            val tagName = tv_tag_name?.text.toString().trim()
            if (tagType.isNotEmpty() || tagName.isNotEmpty() || tags_list.isNotEmpty()) {
                AndroidUtils.Delete_Popup(requireActivity(), dialog)
            } else {
                dialog.dismiss()
            }
        }

        btn_cancel_add_tags.setOnClickListener {
            val tagType = tv_tag_type?.text.toString().trim()
            val tagName = tv_tag_name?.text.toString().trim()
            if (tagType.isNotEmpty() || tagName.isNotEmpty() || tags_list.isNotEmpty()) {
                AndroidUtils.Delete_Popup(requireActivity(), dialog)
            } else {
                dialog.dismiss()
            }
        }
        
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setView(view_add_tags)
        dialog.show()
    }

    private fun renderSelectedTags(context: Context?, container: LinearLayout?, tags: ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel>?, sourceDocument: com.digicoffer.lauditor.Documents.Models.DocumentsModel?, dialog: Dialog?, activity: Activity?) {
        container?.removeAllViews()
        tags?.forEachIndexed { index, tagModel ->
            val tagView = LayoutInflater.from(context).inflate(R.layout.displays_documents_list, null)
            val tvTagName = tagView.findViewById<TextView>(R.id.tv_document_name)
            val ivRemove = tagView.findViewById<ImageView>(R.id.iv_cancel)
            tvTagName.text = "${tagModel.tag_type} - ${tagModel.tag_name}"

            ivRemove.setOnClickListener {
                if (sourceDocument != null) {
                    sourceDocument.tags?.remove(tagModel.tag_type)
                }
                tags.removeAt(index)
                renderSelectedTags(context, container, tags, sourceDocument, dialog, activity)
            }
            container?.addView(tagView)
        }
    }

    private fun save_edited_tags() {
        val newTagsObj = JSONObject()
        for (i in tags_list.indices) {
            try {
                val model = tags_list[i]
                newTagsObj.put(model.tag_type, model.tag_name)
            } catch (e: JSONException) {
                e.fillInStackTrace()
            }
        }
        for (i in selected_upload_documents_list.indices) {
            val selectedDoc = selected_upload_documents_list[i]
            for (j in upload_documents_list.indices) {
                val uploadDoc = upload_documents_list[j]
                if (selectedDoc.name == uploadDoc.name) {
                    val currentTags = uploadDoc.tags ?: JSONObject()
                    for (k in tags_list.indices) {
                        try {
                            val model = tags_list[k]
                            currentTags.put(model.tag_type, model.tag_name)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    uploadDoc.tags = currentTags
                    upload_documents_list[j] = uploadDoc
                }
            }
        }
        loadRecyclerview("edit_meta", "")
    }

    private fun edit_tags(documentsModel: com.digicoffer.lauditor.Documents.Models.DocumentsModel, position: Int) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = requireActivity().layoutInflater
        val view_edit_documents = inflater.inflate(R.layout.edit_document, null)

        val etDocName = view_edit_documents.findViewById<TextInputEditText>(R.id.edit_doc_name)
        val etDescription = view_edit_documents.findViewById<TextInputEditText>(R.id.edit_description)
        val btnExpDate = view_edit_documents.findViewById<AppCompatButton>(R.id.tv_expiration_date)

        val tv_document_name = view_edit_documents.findViewById<TextView>(R.id.tv_document_name)
        tv_document_name.setText(R.string.document_name)

        val description = view_edit_documents.findViewById<TextView>(R.id.description)
        description.setText(R.string.description)
        
        btnExpDate.hint = getString(R.string.expiration_date)
        val expiration_date_id = view_edit_documents.findViewById<TextView>(R.id.expiration_date_id)
        expiration_date_id.setText(R.string.expiration_date)

        val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(50))
        val filters1 = arrayOf<InputFilter>(InputFilter.LengthFilter(300))
        etDocName.filters = filters
        etDocName.maxLines = 5
        etDescription.filters = filters1
        etDescription.maxLines = 10

        val switchDownload = view_edit_documents.findViewById<View>(R.id.switch_download).findViewById<SwitchMaterial>(R.id.switch_action)
        val switchEncryption = view_edit_documents.findViewById<View>(R.id.switch_encryption).findViewById<SwitchMaterial>(R.id.switch_action)
        val tv_label = view_edit_documents.findViewById<View>(R.id.switch_encryption).findViewById<TextView>(R.id.tv_label)
        tv_label.setText(R.string.enable_encryption)

        val btnAddTag = view_edit_documents.findViewById<AppCompatButton>(R.id.btn_add_tag)
        llSelectedTags = view_edit_documents.findViewById(R.id.ll_selected_tags)

        val btnSave = view_edit_documents.findViewById<AppCompatButton>(R.id.btn_save_tag)
        AndroidUtils.ToggleButton(1, btnSave)
        val btnCancel = view_edit_documents.findViewById<AppCompatButton>(R.id.btn_cancel_edit_docs)
        val ivClose = view_edit_documents.findViewById<ImageView>(R.id.close_edit_docs)

        etDocName.setText(documentsModel.name)
        etDescription.setText(documentsModel.description)
        btnExpDate.text = documentsModel.expiration_date
        switchDownload.isChecked = documentsModel.isIsenabled
        checkSwitchState(switchDownload)
        switchEncryption.isChecked = documentsModel.isencrypted ?: false
        checkSwitchState(switchEncryption)

        switchDownload.setOnCheckedChangeListener { _, b ->
            documentsModel.isIsenabled = b
            checkSwitchState(switchDownload)
        }
        switchEncryption.setOnCheckedChangeListener { _, b ->
            documentsModel.isencrypted = b
            checkSwitchState(switchEncryption)
        }

        val tagList = ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel>()
        val tags = documentsModel.tags
        if (tags != null) {
            val keys = tags.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                try {
                    val tag = com.digicoffer.lauditor.Documents.Models.DocumentsModel()
                    tag.tag_type = key
                    tag.tag_name = tags.getString(key)
                    tagList.add(tag)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
        renderSelectedTags(context, llSelectedTags, tagList, documentsModel, null, activity)

        btnExpDate.setOnClickListener {
            AndroidUtils.showDatePicker(btnExpDate, false, false, true, null)
        }

        btnAddTag.setOnClickListener {
            documentsModel.name = etDocName.text.toString().trim()
            documentsModel.description = etDescription.text.toString().trim()
            documentsModel.expiration_date = btnExpDate.text.toString()
            documentsModel.isIsenabled = switchDownload.isChecked
            documentsModel.isencrypted = switchEncryption.isChecked

            selected_upload_documents_list.clear()
            selected_upload_documents_list.add(documentsModel)
            open_add_tags_popup()
        }

        val dialog = dialogBuilder.setView(view_edit_documents).setCancelable(false).create()

        btnSave.setOnClickListener {
            if (etDocName.text.toString().isEmpty() && etDescription.text.toString().isEmpty()) {
                AndroidUtils.showAlert("Please enter the Document name, Description", activity)
            } else if (etDescription.text.toString().isEmpty()) {
                AndroidUtils.showAlert("Please enter the Description", activity)
            } else if (etDocName.text.toString().isEmpty()) {
                AndroidUtils.showAlert("Please enter the Document name", activity)
            } else {
                documentsModel.name = etDocName.text.toString().trim()
                documentsModel.description = etDescription.text.toString().trim()
                documentsModel.expiration_date = btnExpDate.text.toString()
                documentsModel.file = documentsModel.file
                upload_documents_list[position] = documentsModel
                dialog.dismiss()
                loadUploadedDocuments()
            }
        }

        val closeListener = View.OnClickListener {
            val editedModel = com.digicoffer.lauditor.Documents.Models.DocumentsModel()
            editedModel.name = etDocName.text.toString().trim()
            editedModel.description = etDescription.text.toString().trim()
            editedModel.expiration_date = btnExpDate.text.toString()
            editedModel.isIsenabled = switchDownload.isChecked
            editedModel.isencrypted = switchEncryption.isChecked
            editedModel.tags = documentsModel.tags

            val changed = AndroidUtils.hasDocumentChanged(documentsModel, editedModel)
            if (changed) {
                AndroidUtils.Delete_Popup(requireActivity(), dialog)
            } else {
                dialog.dismiss()
            }
        }

        btnCancel.setOnClickListener(closeListener)
        ivClose.setOnClickListener(closeListener)
        
        dialog.setCanceledOnTouchOutside(false)
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
        isInitialLoad = true
        isChangesOccured = false
        if (Constants.MATTER_TYPE == "Legal") {
            matter_type = "legal"
        } else if (Constants.MATTER_TYPE == "General") {
            matter_type = "general"
        }
        val postdata = JSONObject()
        WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "matter/$matter_type/${Constants.Matter_id}/documents", "Chosen_Documents", postdata.toString())
    }

    private fun document_list() {
        try {
            val postdata = JSONObject()
            postdata.put("attachment_type", "documents")
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.PUT, "matter/attachments", "Documents_List", postdata.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    @Throws(JSONException::class)
    fun update_document() {
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
        val matterType = (Constants.MATTER_TYPE ?: "").lowercase(Locale.ROOT)
        val matterId = (Constants.Matter_id ?: "")
        WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.PATCH, "v2/matter/$matterType/$matterId", "matter_update", postdata.toString())
    }

    fun check_select_all(check_status: Boolean) {
        chk_select_all?.isChecked = check_status
    }

    fun isAnyOneSelected(check_status: Boolean) {
        val chk_item = if (check_status) 1 else 0
        AndroidUtils.ToggleButton(chk_item, btn_add_tags)
        chk_select_all?.isChecked = check_status
    }

    fun check_encrypted(check_encrypt: Boolean) {
        if (check_encrypt) EnableEncryptionBackground() else DisableEncryptionBackground()
    }

    fun check_enabled(check_encrypt: Boolean) {
        if (check_encrypt) EnableDownloadBackground() else DisableDownloadBackground()
    }

    fun check_encrypted_all(check_encrypt: Boolean) {
        if (check_encrypt) Enable_allEncryptionBackground() else Disable_allEncryptionBackground()
    }

    fun check_enabled_all(check_encrypt: Boolean) {
        if (check_encrypt) Enable_ALlDownloadBackground() else Disable_AllDownloadBackground()
    }

    override fun ViewTags(documentsModel: com.digicoffer.lauditor.Documents.Models.DocumentsModel, itemsArrayList: ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel>) {
        val dialogBuilder = AlertDialog.Builder(context)
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

        val iter = documentsModel.tags?.keys()
        if (iter != null) {
            while (iter.hasNext()) {
                val key = iter.next()
                val value = documentsModel.tags?.optString(key)
                val view_added_tags = inflater.inflate(R.layout.displays_documents_list, null)
                val tv_tag_name = view_added_tags.findViewById<TextView>(R.id.tv_document_name)
                val iv_remove_tag = view_added_tags.findViewById<ImageView>(R.id.iv_cancel)

                val tag_msg = "$key - $value"
                tv_tag_name.text = tag_msg

                iv_remove_tag.setOnClickListener {
                    ll_existing_tags.removeView(view_added_tags)
                    documentsModel.tags?.remove(key)
                }
                ll_existing_tags.addView(view_added_tags)
            }
        }

        val dialog = dialogBuilder.create()
        iv_close_existing_tags.setOnClickListener { dialog.dismiss() }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setView(view_edit_tags)
        dialog.show()
    }

    override fun EditDocuments(documentsModel: com.digicoffer.lauditor.Documents.Models.DocumentsModel, itemsArrayList: ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel>, position: Int) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(context).inflate(R.layout.edit_document, null)

        val etDocName = view.findViewById<TextInputEditText>(R.id.edit_doc_name)
        val etDescription = view.findViewById<TextInputEditText>(R.id.edit_description)
        val btnExpDate = view.findViewById<AppCompatButton>(R.id.tv_expiration_date)

        val tv_document_name = view.findViewById<TextView>(R.id.tv_document_name)
        tv_document_name.setText(R.string.document_name)

        val description = view.findViewById<TextView>(R.id.description)
        description.setText(R.string.description)

        btnExpDate.hint = getString(R.string.expiration_date)
        val expiration_date_id = view.findViewById<TextView>(R.id.expiration_date_id)
        expiration_date_id.setText(R.string.expiration_date)

        val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(50))
        val filters1 = arrayOf<InputFilter>(InputFilter.LengthFilter(300))
        etDescription.hint = getString(R.string.description)
        etDocName.maxLines = 5
        etDocName.filters = filters
        etDescription.maxLines = 10
        etDescription.filters = filters1

        etDocName.addTextChangedListener(Validation(etDocName))
        etDescription.addTextChangedListener(DescriptionValidation(etDescription))

        val switchDownload = view.findViewById<View>(R.id.switch_download).findViewById<SwitchMaterial>(R.id.switch_action)
        val switchEncryption = view.findViewById<View>(R.id.switch_encryption).findViewById<SwitchMaterial>(R.id.switch_action)
        val tv_label = view.findViewById<View>(R.id.switch_encryption).findViewById<TextView>(R.id.tv_label)
        tv_label.setText(R.string.enable_encryption)

        val btnAddTag = view.findViewById<AppCompatButton>(R.id.btn_add_tag)
        llSelectedTags = view.findViewById(R.id.ll_selected_tags)

        val btnSave = view.findViewById<AppCompatButton>(R.id.btn_save_tag)
        AndroidUtils.ToggleButton(1, btnSave)
        val btnCancel = view.findViewById<AppCompatButton>(R.id.btn_cancel_edit_docs)
        val ivClose = view.findViewById<ImageView>(R.id.close_edit_docs)

        etDocName.setText(documentsModel.name)
        etDescription.setText(documentsModel.description)
        btnExpDate.text = documentsModel.expiration_date
        switchDownload.isChecked = documentsModel.isIsenabled
        checkSwitchState(switchDownload)
        switchEncryption.isChecked = documentsModel.isencrypted ?: false
        checkSwitchState(switchEncryption)

        switchDownload.setOnCheckedChangeListener { _, b ->
            documentsModel.isIsenabled = b
            checkSwitchState(switchDownload)
        }
        switchEncryption.setOnCheckedChangeListener { _, b ->
            documentsModel.isencrypted = b
            checkSwitchState(switchEncryption)
        }

        val tagList = ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel>()
        val tags = documentsModel.tags
        if (tags != null) {
            val keys = tags.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                try {
                    val tag = com.digicoffer.lauditor.Documents.Models.DocumentsModel()
                    tag.tag_type = key
                    tag.tag_name = tags.getString(key)
                    tagList.add(tag)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
        renderSelectedTags(context, llSelectedTags, tagList, documentsModel, null, activity)

        btnExpDate.setOnClickListener {
            AndroidUtils.showDatePicker(btnExpDate, false, false, true, null)
        }

        btnAddTag.setOnClickListener {
            val model = itemsArrayList[position]
            model.name = etDocName.text.toString().trim()
            model.description = etDescription.text.toString().trim()
            model.expiration_date = btnExpDate.text.toString()
            model.isIsenabled = switchDownload.isChecked
            model.isencrypted = switchEncryption.isChecked

            selected_upload_documents_list.clear()
            selected_upload_documents_list.add(model)
            open_add_tags_popup()
        }

        val dialog = dialogBuilder.setView(view).setCancelable(false).create()

        btnSave.setOnClickListener {
            if (etDocName.text.toString().isEmpty() && etDescription.text.toString().isEmpty()) {
                AndroidUtils.showAlert("Please enter the Document name, Description", activity)
            } else if (etDescription.text.toString().isEmpty()) {
                AndroidUtils.showAlert("Please enter the Description", activity)
            } else if (etDocName.text.toString().isEmpty()) {
                AndroidUtils.showAlert("Please enter the Document name", activity)
            } else {
                val model = itemsArrayList[position]
                model.name = etDocName.text.toString().trim()
                model.description = etDescription.text.toString().trim()
                model.expiration_date = btnExpDate.text.toString()
                model.isIsenabled = switchDownload.isChecked
                model.isencrypted = switchEncryption.isChecked
                itemsArrayList[position] = model
                dialog.dismiss()
                loadRecyclerview("edit_meta", "")
            }
        }

        val closeListener = View.OnClickListener {
            val editedModel = com.digicoffer.lauditor.Documents.Models.DocumentsModel()
            editedModel.name = etDocName.text.toString().trim()
            editedModel.description = etDescription.text.toString().trim()
            editedModel.expiration_date = btnExpDate.text.toString()
            editedModel.isIsenabled = switchDownload.isChecked
            editedModel.isencrypted = switchEncryption.isChecked
            editedModel.tags = itemsArrayList[position].tags

            val changed = AndroidUtils.hasDocumentChanged(documentsModel, editedModel)
            if (changed) {
                AndroidUtils.Delete_Popup(requireActivity(), dialog)
            } else {
                dialog.dismiss()
            }
        }

        btnCancel.setOnClickListener(closeListener)
        ivClose.setOnClickListener(closeListener)
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
        AndroidUtils.LoadList(ll_uploaded_documents, context, upload_documents_list.size, false)
    }

    companion object {
        // Add any static methods or constants here if required
    }
}
