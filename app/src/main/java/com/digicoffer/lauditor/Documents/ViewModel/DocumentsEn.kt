package com.digicoffer.lauditor.Documents.ViewModel

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.digicoffer.lauditor.AuditTrails.Adapters.PaginationHelper
import com.digicoffer.lauditor.Relationships.ClientRelationship
import com.digicoffer.lauditor.Documents.DocumentsListAdpater.DocumentsListAdapter
import com.digicoffer.lauditor.Documents.DocumentsListAdpater.GroupsListAdapter
import com.digicoffer.lauditor.Documents.DocumentsListAdpater.View_documents_adapter
import com.digicoffer.lauditor.Documents.Models.ClientsModel
import com.digicoffer.lauditor.Documents.Models.DocumentsModel
import com.digicoffer.lauditor.Documents.Models.MattersModel
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel
import com.digicoffer.lauditor.Matter.ViewModels.Matter
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.FileSelection.BottomSheetUploadFile
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.DescriptionValidation
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.FileDownloader
import com.digicoffer.lauditor.CommonFiles.PdfUtils.File_Content_Type
import com.digicoffer.lauditor.CommonFiles.PdfUtils.PDFviewer
import com.digicoffer.lauditor.CommonFiles.PdfUtils.RetrievePDFfromUrl
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.Comparator
import java.util.Date
import java.util.Locale
import java.util.Objects
import java.util.regex.Pattern

class DocumentsEn : Fragment(), BottomSheetUploadFile.OnPhotoSelectedListner,
    AsyncTaskCompleteListener, DocumentsListAdapter.EventListener,
    View_documents_adapter.Eventlistner, GroupsListAdapter.OnCheckedChangeListener,
    AndroidUtils.OnModuleClickListener {

    lateinit var btn_browse: Button
    lateinit var btn_group_cancel: Button
    lateinit var btn_group_submit: Button
    lateinit var btn_group_view_cancel: Button
    lateinit var btn_group_view_submit: Button
    var isListFiltered: Boolean = false
    private var pendingHighlightIds: ArrayList<String>? = null
    private var selectedGroupsIds: ArrayList<String>? = null
    var isDownload: Boolean = false
    private var totalUploads: Int = 0
    private var completedUploads: Int = 0
    private var isAlertShown: Boolean = false
    var end_temp: Int = 0
    var filteredList: ArrayList<ViewDocumentsModel> = ArrayList()
    private var pagebuttons: MutableList<TextView> = ArrayList()
    var is_MergePdfClicked: Boolean = true
    var filedata: ByteArray? = null
    var doc_name: String? = null
    var currentPage: Int = 1
    var ReceivedFileName: String = ""
    var client_name: String? = null
    var Dialog: AlertDialog? = null
    var isedit: Boolean = false
    var isUpdateTag: Boolean = false
    var edit_position: Int = 0
    var ClientAdapter: CommonSpinnerAdapter<ClientsModel>? = null
    var Matteradapter: CommonSpinnerAdapter<MattersModel>? = null
    var llSelectedTags: LinearLayout? = null
    var ll_select_file: LinearLayout? = null
    lateinit var cl_document: ConstraintLayout

    var ischecked: Boolean = true
    var ischecked_group: Boolean = true
    var ischeckedClient_group: Boolean = true
    var ischecked_group_view: Boolean = true
    var isselect_all_checked: Boolean = true
    var isUploadDoc: Boolean = false
    var custom_spinner: TextView? = null
    var custom_spinner2: TextView? = null
    var custom_spinner3: TextView? = null
    var custom_spinner4: TextView? = null
    var custom_spinner5: TextView? = null
    var custom_spinner_group: TextView? = null
    var tv_select_groups_view: TextView? = null
    var tv_client_doc: TextView? = null
    var tv_firm_doc: TextView? = null
    var chk_box_layout: LinearLayout? = null
    var pageItems: ArrayList<ViewDocumentsModel> = ArrayList()
    var is_clicked_add: Boolean = true
    var is_clicked_edit: Boolean = true
    var array_group: JSONArray = JSONArray()
    var cv_view_doc: CardView? = null
    var list_docType_view: ListView? = null
    var ll_added_tags: LinearLayout? = null
    var ll_matter: LinearLayout? = null
    var ll_category: LinearLayout? = null
    var ll_groups: LinearLayout? = null
    var ll_client_name: LinearLayout? = null
    var ll_view_docs: LinearLayout? = null
    var ll_upload_docs: LinearLayout? = null
    var upload_group_layout: LinearLayout? = null
    var view_group_layout: LinearLayout? = null
    var ll_matter_view: LinearLayout? = null
    var ll_client_name_view: LinearLayout? = null
    var ll_categories_layout: LinearLayout? = null
    var ll_document_type_view: LinearLayout? = null
    var ll_upload_groups: LinearLayout? = null
    var ll_upload_client_group: LinearLayout? = null
    var tv_tag_type: TextInputEditText? = null
    var tv_tag_name: TextInputEditText? = null
    lateinit var tv_search_client_view: TextInputEditText
    var imageView: ImageView? = null
    var selectedLanguage: BooleanArray? = null
    var DOCUMENT_TYPE_TAG: String = "client"
    var CONTENT_TYPE: String = ""
    var UPLOAD_TAG: String = "Client"
    var VIEW_TAG: String = "Client"
    var adapter: DocumentsListAdapter? = null
    var adapter1: View_documents_adapter? = null
    var cv_view_documents: CardView? = null
    val view_docs_list: ArrayList<ViewDocumentsModel> = ArrayList()
    var siv_upload_document: ShapeableImageView? = null
    var siv_view_document: ShapeableImageView? = null
    val langList: ArrayList<Int> = ArrayList()
    val groupsList: ArrayList<DocumentsModel> = ArrayList()
    val tags_list: ArrayList<DocumentsModel> = ArrayList()
    var mSelectedUri: File? = null
    var subtag: String = ""
    val selected_documents_list: ArrayList<DocumentsModel> = ArrayList()
    var ll_page_navigaiton: LinearLayout? = null
    var DOWNLOAD_TAG: Boolean = false
    var ENCRYPTION_TAG: Boolean = true
    var DECRYPTION_TAG: Boolean = true
    var CATEGORY_TAG: String = ""
    var chk_select_all: CheckBox? = null
    var filename: String? = null
    var currentpoistion: Int = 0
    val clientsList: ArrayList<ClientsModel> = ArrayList()
    val CorpClientsList: ArrayList<ClientsModel> = ArrayList()
    val matterlist: ArrayList<MattersModel> = ArrayList()
    val updatedClients: ArrayList<ClientsModel> = ArrayList()
    val selected_groups_list: ArrayList<DocumentsModel> = ArrayList()
    val client_groups_list: ArrayList<DocumentsModel> = ArrayList()
    val selected_client_groups_list: ArrayList<DocumentsModel> = ArrayList()
    val docsList: ArrayList<DocumentsModel> = ArrayList()
    var rv_documents: RecyclerView? = null
    var rv_display_view_docs: RecyclerView? = null
    var rv_display_upload_groups_docs: RecyclerView? = null
    var rv_display_view_groups_docs: RecyclerView? = null
    var rv_upload_groups: RecyclerView? = null
    var progress_dialog: Dialog? = null

    var tv_matter: TextView? = null
    var select_documents: TextView? = null
    var tv_select_group_name: TextView? = null
    var upload_name: TextView? = null
    var view_name: TextView? = null
    var tag_type_name: TextView? = null
    var tag_name: TextView? = null
    var header_name: TextView? = null
    var header_name_group: TextView? = null
    var category_name: TextView? = null
    var tv_category: TextView? = null
    var tv_selected_file: TextView? = null
    var tv_select_upload_group_name: TextView? = null
    var tv_select_upload_groups: TextView? = null

    var header_name_edit: TextView? = null
    var tag_type_edit: TextView? = null
    var tag_edit: TextView? = null
    var tv_edit_tag_type: TextInputEditText? = null
    var tv_edit_tag_name: TextInputEditText? = null
    var btn_upload: Button? = null
    var btn_add_tags: Button? = null
    var btn_cancel: Button? = null
    var tv_add_tag: TextView? = null
    var tv_client: TextView? = null
    var tv_firm: TextView? = null
    var tv_enable_download: TextView? = null
    var tv_disable_download: TextView? = null
    var tv_enable_encryption: TextView? = null
    var tv_disable_encryption: TextView? = null
    var tv_edit_meta: TextView? = null
    var tv_name: TextView? = null
    var tv_client_view: TextView? = null
    var tv_firm_view: TextView? = null
    var tv_deleted_view: TextView? = null
    var tv_name_view: TextView? = null
    var matter_name: TextView? = null
    var category_name_id: TextView? = null
    var select_doc_type: TextView? = null
    var tv_document_name: TextView? = null
    var description: TextView? = null
    var file: File? = null
    var value: String = ""
    var entity_id: String = ""
    var matter_id: String = ""
    var client_id: String = ""
    var img_clear_icon1: ImageView? = null
    var img_dropdown_icon1: ImageView? = null
    var img_clear_icon2: ImageView? = null
    var img_dropdown_icon2: ImageView? = null
    var img_clear_icon3: ImageView? = null
    var img_dropdown_icon3: ImageView? = null
    var img_clear_icon4: ImageView? = null
    var img_dropdown_icon4: ImageView? = null
    var tv_tag_document_name: TextView? = null
    var tv_select_groups: TextView? = null
    var merge_pdf: TextView? = null
    var view1: View? = null
    var view2: View? = null
    var view3: View? = null
    var view4: View? = null
    var ll_hide_document_details: LinearLayout? = null
    var pageNumberLayout: LinearLayout? = null
    var ll_merge_pdf: LinearLayout? = null
    var sp_matter: Spinner? = null
    var sp_client: Spinner? = null
    var tv_search_client: Spinner? = null
    var sp_matter_view: Spinner? = null
    lateinit var tv_search_client_views: TextInputEditText
    var tl_selected_file: TextInputLayout? = null
    var ischecked_matter: Boolean = true
    var ischecked_matter2: Boolean = true
    var ischecked2: Boolean = true
    var filters: Array<InputFilter>? = null
    var filters1: Array<InputFilter>? = null

    // Deleted Banner UI components
    private var ll_deleted_banner: LinearLayout? = null
    private var tv_banner_deleted_message: TextView? = null
    private var isGridView: Boolean = false
    private var ll_view_toggle: LinearLayout? = null
    private var iv_list_view: ImageView? = null
    private var iv_grid_view: ImageView? = null
    private var tv_file_count_label: TextView? = null

    private var list_client: ListView? = null
    private var list_matter: ListView? = null
    private var list_client_view: ListView? = null
    private var list_matter_view: ListView? = null
    private var list_group: ListView? = null
    private var list_scroll: ScrollView? = null
    private var list_scroll2: ScrollView? = null
    private var list_scroll3: ScrollView? = null
    private var list_scroll4: ScrollView? = null
    private var list_scroll_group: ScrollView? = null
    private var iv_forward_button: ImageView? = null
    private var iv_backward_button: ImageView? = null
    private var count_file: Int = 0
    private var mViewModel: NewModel? = null
    private var spinnerLayout: RelativeLayout? = null
    private var ismatter_chosen: Boolean = false

    private var bottommSheetUploadDocument: BottomSheetUploadFile? = null
    private var mSelectedBitmap: Bitmap? = null

    // For selected items highlights
    private var selectedId: String = ""

    companion object {
        private var tempDocModel: ViewDocumentsModel? = null

        fun LoadingRecyclerview(recyclerView: RecyclerView, context: Context?, mode: Int) {
            if (mode == View_documents_adapter.VIEW_TYPE_GRID) {
                recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            } else {
                recyclerView.layoutManager = LinearLayoutManager(context)
            }
            recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_fall_down)
            recyclerView.scheduleLayoutAnimation()
        }

        fun replaceLastDotWithSlash(input: String): String {
            val lastIndex = input.lastIndexOf('.')
            if (lastIndex == -1) return input
            return input.substring(0, lastIndex) + '/' + input.substring(lastIndex + 1)
        }

        fun convertfiletostring(file: File): String {
            val stringBuilder = java.lang.StringBuilder()
            try {
                FileInputStream(file).use { fis ->
                    BufferedReader(InputStreamReader(fis)).use { bufferedReader ->
                        var line: String?
                        while (bufferedReader.readLine().also { line = it } != null) {
                            stringBuilder.append(line).append("\n")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return stringBuilder.toString()
        }

        fun getFile(context: Context, uri: Uri): File {
            val destinationFilename = File(
                context.filesDir.path
                        + File.separatorChar + queryName(context, uri)
            )
            try {
                context.contentResolver.openInputStream(uri)?.use { ins ->
                    createFileFromStream(ins, destinationFilename)
                }
            } catch (ex: java.lang.Exception) {
                Log.e("Save File", ex.message ?: "")
                ex.fillInStackTrace()
            }
            return destinationFilename
        }

        fun createFileFromStream(ins: InputStream, destination: File) {
            try {
                FileOutputStream(destination).use { os ->
                    val buffer = ByteArray(4096)
                    var length: Int
                    while (ins.read(buffer).also { length = it } > 0) {
                        os.write(buffer, 0, length)
                    }
                    os.flush()
                }
            } catch (ex: java.lang.Exception) {
                Log.e("Save File", ex.message ?: "")
                ex.fillInStackTrace()
            }
        }

        private fun queryName(context: Context, uri: Uri): String {
            val returnCursor = context.contentResolver.query(
                uri, null, null, null, null
            )
            if (returnCursor != null) {
                val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                returnCursor.moveToFirst()
                val name = returnCursor.getString(nameIndex)
                returnCursor.close()
                return name
            }
            return ""
        }
    }

    private val requestPermissions: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val permissionGranted = results[READ_EXTERNAL_STORAGE] == true
            if (permissionGranted) {
                BottomSheetUploadfile()
            }
        }

    override fun onCreateView(
        @NonNull inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.upload_document, container, false)
        try {
            mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
            filters = arrayOf(InputFilter.LengthFilter(50))
            filters1 = arrayOf(InputFilter.LengthFilter(300))

            tv_client_view = v.findViewById(R.id.tv_client_view)
            tv_client_view?.setText(R.string.client)
            ll_merge_pdf = v.findViewById(R.id.ll_merge_pdf)

            tv_deleted_view = v.findViewById(R.id.tv_deleted_view)
            tv_deleted_view?.setText(R.string.deleted)
            tv_firm_view = v.findViewById(R.id.tv_firm_view)
            tv_firm_view?.setText(R.string.firm)
            tv_search_client_view = v.findViewById(R.id.tv_search_client_view)
            tv_search_client_view.setHint(R.string.search)

            ll_page_navigaiton = v.findViewById(R.id.ll_page_navigaiton)
            ll_upload_groups = v.findViewById(R.id.ll_upload_groups)
            pageNumberLayout = ll_page_navigaiton?.findViewById(R.id.pageNumberLayout)
            iv_forward_button = ll_page_navigaiton?.findViewById(R.id.iv_forward_button)
            iv_backward_button = ll_page_navigaiton?.findViewById(R.id.iv_backward_button)
            tv_select_upload_group_name = v.findViewById(R.id.tv_select_upload_group_name)
            tv_select_upload_group_name?.setText(R.string.select_groups)
            tv_select_upload_groups = v.findViewById(R.id.tv_select_upload_groups)
            rv_upload_groups = v.findViewById(R.id.rv_upload_groups)
            ll_upload_client_group = v.findViewById(R.id.ll_upload_client_group)
            rv_upload_groups?.background = requireContext().getDrawable(R.drawable.rectangle_light_grey_bg)

            view1 = v.findViewById(R.id.customLayout1)
            custom_spinner = view1?.findViewById(R.id.tv_spinner_view)
            custom_spinner?.setHint(R.string.select_client_name)
            img_clear_icon1 = view1?.findViewById(R.id.img_clear_icon)
            img_dropdown_icon1 = view1?.findViewById(R.id.img_dropdown_icon)

            view2 = v.findViewById(R.id.customLayout2)
            custom_spinner2 = view2?.findViewById(R.id.tv_spinner_view)
            custom_spinner2?.setHint(R.string.select_matters)
            img_clear_icon2 = view2?.findViewById(R.id.img_clear_icon)
            img_dropdown_icon2 = view2?.findViewById(R.id.img_dropdown_icon)

            view3 = v.findViewById(R.id.customLayout3)
            custom_spinner3 = view3?.findViewById(R.id.tv_spinner_view)
            custom_spinner3?.setHint(R.string.select_client_name)
            img_clear_icon3 = view3?.findViewById(R.id.img_clear_icon)
            img_dropdown_icon3 = view3?.findViewById(R.id.img_dropdown_icon)

            view4 = v.findViewById(R.id.customLayout4)
            custom_spinner4 = view4?.findViewById(R.id.tv_spinner_view)
            custom_spinner4?.setHint(R.string.select_matters)
            img_clear_icon4 = view4?.findViewById(R.id.img_clear_icon)
            img_dropdown_icon4 = view4?.findViewById(R.id.img_dropdown_icon)

            list_client = v.findViewById(R.id.list_client)
            list_matter = v.findViewById(R.id.list_matter)
            list_client_view = v.findViewById(R.id.list_client_view)
            list_matter_view = v.findViewById(R.id.list_matter_view)

            tv_add_tag = v.findViewById(R.id.tv_add_tag)
            tv_edit_meta = v.findViewById(R.id.tv_edit_meta)
            btn_upload = v.findViewById(R.id.btn_upload)
            tv_firm = v.findViewById(R.id.tv_firm)
            tv_search_client_views = v.findViewById(R.id.tv_search_client_views)
            tv_search_client_views.setHint(R.string.search)

            tv_search_client_view.addTextChangedListener(Validation(tv_search_client_view))
            tv_search_client_views.addTextChangedListener(Validation(tv_search_client_views))

            tv_name = v.findViewById(R.id.tv_name)
            ll_category = v.findViewById(R.id.ll_category)
            tv_name_view = v.findViewById(R.id.tv_name_view)
            tv_name_view?.setText(R.string.client_name)
            matter_name = v.findViewById(R.id.matter_name)
            matter_name?.setText(R.string.matters)
            merge_pdf = v.findViewById(R.id.merge_pdf)
            merge_pdf?.setText(R.string.merge_pdf)
            merge_pdf?.typeface = Typeface.DEFAULT_BOLD
            merge_pdf?.background = requireActivity().getDrawable(R.drawable.rectangle_light_grey_bg)
            rv_display_view_docs = v.findViewById(R.id.rv_display_view_docs)
            select_doc_type = v.findViewById(R.id.select_doc_type)
            select_doc_type?.setText(R.string.select_groups)
            category_name_id = v.findViewById(R.id.category_name_id)
            category_name_id?.setText(R.string.sub_categories)
            ll_groups = v.findViewById(R.id.ll_groups)

            siv_upload_document = v.findViewById(R.id.upload_icon)
            siv_view_document = v.findViewById(R.id.view_icon)
            btn_upload?.setText(R.string.upload)
            btn_cancel = v.findViewById(R.id.btn_cancel)
            btn_add_tags = v.findViewById(R.id.btn_add_tag)
            btn_add_tags?.setText(R.string.add_tag)
            btn_add_tags?.visibility = GONE
            btn_browse = v.findViewById(R.id.btn_browse)
            btn_browse.setText(R.string.browse_small)

            upload_group_layout = v.findViewById(R.id.upload_group_layout)
            rv_display_upload_groups_docs = v.findViewById(R.id.rv_display_upload_groups_docs)
            rv_display_upload_groups_docs?.background = requireContext().getDrawable(R.drawable.rectangle_light_grey_bg)
            btn_group_cancel = v.findViewById(R.id.btn_group_cancel)
            btn_group_cancel.visibility = GONE
            btn_group_submit = v.findViewById(R.id.btn_group_submit)
            btn_group_submit.visibility = GONE

            view_group_layout = v.findViewById(R.id.view_group_layout)
            rv_display_view_groups_docs = v.findViewById(R.id.rv_display_view_groups_docs)
            rv_display_view_groups_docs?.background = requireContext().getDrawable(R.drawable.rectangle_light_grey_bg)
            btn_group_view_cancel = v.findViewById(R.id.btn_group_view_cancel)
            btn_group_view_submit = v.findViewById(R.id.btn_group_view_submit)

            tv_add_tag = v.findViewById(R.id.tv_add_tag)
            tv_add_tag?.setText(R.string.add_tag)
            tv_edit_meta = v.findViewById(R.id.tv_edit_meta)
            tv_edit_meta?.setText(R.string.edit_meta)
            tv_client = v.findViewById(R.id.tv_client)
            tv_client?.setText(R.string.client)
            tv_firm = v.findViewById(R.id.tv_firm)
            tv_firm?.setText(R.string.firm)
            tv_name = v.findViewById(R.id.tv_name)
            tv_name?.setText(R.string.client_name)
            tv_matter = v.findViewById(R.id.tv_matter)
            tv_matter?.setText(R.string.matters)
            tv_category = v.findViewById(R.id.tv_category)
            tv_category?.setHint(R.string.sub_categories)
            tv_enable_download = v.findViewById(R.id.tv_enable_download)
            tv_enable_download?.setText(R.string.enable_download)
            tv_enable_download?.textSize = 13f
            tv_disable_download = v.findViewById(R.id.tv_disable_download)
            tv_disable_download?.setText(R.string.disable_download)
            tv_disable_download?.textSize = 13f
            tv_enable_encryption = v.findViewById(R.id.tv_enable_encryption)
            tv_enable_encryption?.setText(R.string.enable_encryption)
            tv_enable_encryption?.textSize = 13f
            tv_disable_encryption = v.findViewById(R.id.tv_disable_encryption)
            tv_disable_encryption?.setText(R.string.disable_encryption)
            tv_disable_encryption?.textSize = 13f

            tv_select_groups = v.findViewById(R.id.tv_select_groups)
            tv_select_group_name = v.findViewById(R.id.tv_select_group_name)
            tv_select_group_name?.setText(R.string.select_groups)
            tv_select_groups?.text = ""
            tv_select_groups?.setHint(R.string.select_groups)

            tv_client_doc = v.findViewById(R.id.tv_client_doc)
            tv_client_doc?.textSize = DynamicUtils.twenty.toFloat()
            tv_client_doc?.setText(R.string.list_of_client_documents)

            tv_selected_file = v.findViewById(R.id.tv_selected_file)

            ll_client_name = v.findViewById(R.id.ll_client_name)
            ll_matter = v.findViewById(R.id.ll_matter)
            ll_category = v.findViewById(R.id.ll_category)
            ll_groups = v.findViewById(R.id.ll_groups)
            ll_upload_docs = v.findViewById(R.id.ll_upload_docs)

            ll_view_docs = v.findViewById(R.id.ll_view_docs)
            cl_document = v.findViewById(R.id.cl_document)
            ll_hide_document_details = v.findViewById(R.id.ll_hide_doc_details)
            ll_hide_document_details?.visibility = GONE

            category_name = v.findViewById(R.id.category_name)
            category_name?.setText(R.string.sub_categories)
            upload_name = v.findViewById(R.id.upload_name)
            upload_name?.setTextColor(requireContext().resources.getColor(R.color.white))
            upload_name?.setText(R.string.upload)
            view_name = v.findViewById(R.id.view_name)
            view_name?.setTextColor(requireContext().resources.getColor(R.color.white))
            view_name?.setText(R.string.view)
            select_documents = v.findViewById(R.id.select_documents)
            select_documents?.setText(R.string.select_document)

            chk_box_layout = v.findViewById(R.id.chk_box_layout)
            chk_box_layout?.alpha = 0.5f
            chk_select_all = v.findViewById(R.id.chk_select_all)
            chk_select_all?.background?.alpha = 50
            chk_select_all?.isEnabled = false
            rv_documents = v.findViewById(R.id.rv_documents)

            siv_upload_document?.background = requireContext().resources.getDrawable(R.color.green_count_color)
            siv_upload_document?.setImageDrawable(requireContext().resources.getDrawable(R.mipmap.green_background_icon))

            ll_matter_view = v.findViewById(R.id.ll_matter_view)
            ll_document_type_view = v.findViewById(R.id.ll_document_type_view)
            ll_client_name_view = v.findViewById(R.id.ll_client_name_view)
            ll_categories_layout = v.findViewById(R.id.ll_categories_layout)
            ll_client_name = v.findViewById(R.id.ll_client_name)
            category_name = v.findViewById(R.id.tv_category_name)
            category_name?.setHint(R.string.sub_categories)

            ll_hide_document_details = v.findViewById(R.id.ll_hide_doc_details)
            ll_hide_document_details?.visibility = GONE

            // Initialize views for List/Grid view toggling and Deleted Banner
            ll_view_toggle = v.findViewById(R.id.ll_view_toggle)
            iv_list_view = v.findViewById(R.id.iv_list_view)
            iv_grid_view = v.findViewById(R.id.iv_grid_view)

            iv_list_view?.background = requireContext().getDrawable(R.drawable.button_left_green_background)
            iv_grid_view?.background = requireContext().getDrawable(R.drawable.button_right_grey)
            iv_grid_view?.setColorFilter(requireContext().getColor(R.color.grey_medium))
            iv_list_view?.setColorFilter(requireContext().getColor(R.color.white))
            ll_view_toggle?.visibility = VISIBLE

            // List View button click
            iv_list_view?.setOnClickListener {
                isGridView = false
                iv_list_view?.background = requireContext().getDrawable(R.drawable.button_left_green_background)
                iv_grid_view?.background = requireContext().getDrawable(R.drawable.button_right_grey)
                iv_grid_view?.setColorFilter(requireContext().getColor(R.color.grey_medium))
                iv_list_view?.setColorFilter(requireContext().getColor(R.color.white))
                switchViewMode(View_documents_adapter.VIEW_TYPE_LIST)
            }

            // Grid View button click
            iv_grid_view?.setOnClickListener {
                isGridView = true
                iv_list_view?.background = requireContext().getDrawable(R.drawable.button_left_grey)
                iv_grid_view?.background = requireContext().getDrawable(R.drawable.button_right_green_background)
                iv_grid_view?.setColorFilter(requireContext().getColor(R.color.white))
                iv_list_view?.setColorFilter(requireContext().getColor(R.color.grey_medium))
                switchViewMode(View_documents_adapter.VIEW_TYPE_GRID)
            }

            ll_deleted_banner = v.findViewById(R.id.ll_deleted_banner)
            tv_banner_deleted_message = v.findViewById(R.id.tv_banner_deleted_message)
            val iv_banner_close = v.findViewById<ImageView>(R.id.iv_banner_close)
            val displayText = SpannableStringBuilder()
            val message = "Documents will be permanently deleted in 30 days"
            val spannableMessage = SpannableString(message)
            val start = message.indexOf("30 days")
            if (start != -1) {
                spannableMessage.setSpan(StyleSpan(Typeface.BOLD), start, start + "30 days".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            displayText.append(spannableMessage)
            tv_banner_deleted_message?.text = displayText
            if (ll_deleted_banner != null) {
                ll_deleted_banner?.visibility = GONE
            }
            iv_banner_close?.setOnClickListener {
                hideDeletedBanner()
            }

            ll_upload_docs?.visibility = GONE
            ll_view_docs?.visibility = VISIBLE
            tv_client?.setPadding(40, 40, 40, 40)
            tv_firm?.setPadding(40, 40, 40, 40)
            tv_client_view?.setPadding(40, 40, 40, 40)
            tv_firm_view?.setPadding(40, 40, 40, 40)
            tv_deleted_view?.setPadding(40, 40, 40, 40)

            if (Constants.isCreate) {
                upload_documents()
            } else {
                view_document()
            }

            view1?.setOnClickListener {
                if (clientsList.isEmpty()) {
                    callClientWebservice()
                }
                list_client?.visibility = if (ischecked) VISIBLE else GONE
                ischecked = !ischecked
            }

            img_clear_icon1?.setOnClickListener {
                if (custom_spinner?.text.toString().isNotEmpty()) {
                    client_id = ""
                    matter_id = ""
                    AndroidUtils.DisplaySpinnerView(list_client, custom_spinner, client_name, img_dropdown_icon1, img_clear_icon1, false)
                    AndroidUtils.DisplaySpinnerView(list_matter, custom_spinner2, matter_id, img_dropdown_icon2, img_clear_icon2, false)
                    EnableUpload()
                    ischecked = true
                    ll_upload_groups?.visibility = GONE
                    hideUploadDoc()
                }
            }

            img_clear_icon2?.setOnClickListener {
                if (custom_spinner2?.text.toString().isNotEmpty()) {
                    matter_id = ""
                    ischecked_matter = true
                    callClientGroupsWebservice()
                    hideMatterUpload()
                }
            }

            img_clear_icon3?.setOnClickListener {
                if (custom_spinner3?.text.toString().isNotEmpty()) {
                    selected_groups_list.clear()
                    client_id = ""
                    CATEGORY_TAG = "client"
                    matterlist.clear()
                    matter_id = ""
                    ismatter_chosen = true
                    view_docs_list.clear()
                    rv_display_view_docs?.removeAllViews()
                    rv_display_view_docs?.visibility = GONE
                    ll_page_navigaiton?.visibility = GONE
                    ischecked2 = true
                    hideViewDoc()
                }
            }

            img_clear_icon4?.setOnClickListener {
                if (custom_spinner3?.text.toString().isNotEmpty()) {
                    matter_id = ""
                    ischecked_matter2 = true
                    matterlist.clear()
                    ismatter_chosen = true
                    view_docs_list.clear()
                    rv_display_view_docs?.removeAllViews()
                    rv_display_view_docs?.visibility = GONE
                    ll_page_navigaiton?.visibility = GONE
                    callfilter_client_webservices()
                    hideMatterView()
                }
            }

            view2?.setOnClickListener {
                list_matter?.visibility = if (ischecked_matter) VISIBLE else GONE
                ischecked_matter = !ischecked_matter
            }

            view3?.setOnClickListener {
                if (clientsList.isEmpty()) {
                    callClientWebservice()
                }
                list_client_view?.visibility = if (ischecked2) VISIBLE else GONE
                ischecked2 = !ischecked2
            }

            view4?.setOnClickListener {
                list_matter_view?.visibility = if (ischecked_matter2) VISIBLE else GONE
                ischecked_matter2 = !ischecked_matter2
            }

            siv_upload_document?.setOnClickListener {
                upload_documents()
                client_name = ""
                client_id = ""
            }

            siv_view_document?.setOnClickListener {
                view_document()
            }

            tv_select_groups_view = v.findViewById(R.id.tv_select_groups_view)
            tv_select_groups_view?.text = ""
            tv_select_groups_view?.setHint(R.string.select_groups)

            upload_group_layout?.visibility = GONE
            view_group_layout?.visibility = GONE

            tv_select_groups?.setOnClickListener {
                if (ischecked_group) {
                    if (groupsList.isEmpty()) {
                        callGroupsWebservice()
                    } else {
                        GroupsPopup(upload_group_layout!!, groupsList, selected_groups_list, rv_display_upload_groups_docs!!, tv_select_groups!!)
                    }
                } else {
                    upload_group_layout?.visibility = GONE
                }
                ischecked_group = !ischecked_group
            }

            tv_select_upload_groups?.setOnClickListener {
                if (ischeckedClient_group) {
                    if (client_groups_list.isEmpty()) {
                        callClientGroupsWebservice()
                    } else {
                        GroupsPopup(ll_upload_client_group!!, client_groups_list, selected_client_groups_list, rv_upload_groups!!, tv_select_upload_groups!!)
                    }
                } else {
                    ll_upload_client_group?.visibility = GONE
                }
                ischeckedClient_group = !ischeckedClient_group
            }

            tv_select_groups_view?.setOnClickListener {
                if (ischecked_group_view) {
                    if (groupsList.isEmpty()) {
                        callGroupsWebservice()
                    } else {
                        GroupsPopup(view_group_layout!!, groupsList, selected_groups_list, rv_display_view_groups_docs!!, tv_select_groups_view!!)
                    }
                    view_group_layout?.visibility = VISIBLE
                } else {
                    view_group_layout?.visibility = GONE
                    if (selected_groups_list.isNotEmpty()) {
                        callfilter_client_webservices()
                    }
                }
                ischecked_group_view = !ischecked_group_view
            }

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

            tv_client?.setOnClickListener {
                tv_firm?.setTextColor(requireContext().resources.getColor(R.color.black))
                tv_client?.setTextColor(requireContext().resources.getColor(R.color.white))
                rv_documents?.removeAllViews()
                ischecked = true
                hideFirmBackground()
                client_name = ""
                client_id = ""
            }

            tv_firm?.setOnClickListener {
                hideClientBackground()
                client_name = ""
                client_id = ""
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

            btn_browse.setOnClickListener {
                checkPermissionREAD_EXTERNAL_STORAGE(requireContext())
            }

            btn_add_tags?.setOnClickListener {
                selected_documents_list.clear()
                val listItems = adapter?.list_item
                if (listItems != null) {
                    for (i in 0 until listItems.size) {
                        val documentsModel = listItems[i]
                        if (documentsModel.isChecked) {
                            selected_documents_list.add(documentsModel)
                        }
                    }
                }
                open_add_tags_popup()
            }

            tv_client_view?.setOnClickListener {
                hideviewFirmBackground()
                DOCUMENT_TYPE_TAG = "client"
                CATEGORY_TAG = "client"
            }

            tv_firm_view?.setOnClickListener {
                ismatter_chosen = false
                hideviewClientBackground()
                view_docs_list.clear()
                view_group_layout?.visibility = GONE
                ischecked_group_view = true
                ll_matter_view?.visibility = GONE
                DOCUMENT_TYPE_TAG = "firm"
            }

            tv_deleted_view?.setOnClickListener {
                ismatter_chosen = false
                hideviewFirmBackground()
                if (Constants.CATEGORY?.lowercase() == "solo") {
                    tv_client_doc?.setText(R.string.deleted_documents)
                } else {
                    tv_client_doc?.setText(R.string.list_of_documents_pending_approval)
                }
                ll_merge_pdf?.visibility = GONE
                ll_client_name_view?.visibility = GONE
                tv_client_view?.background = requireContext().resources.getDrawable(R.drawable.button_left_background)
                tv_client_view?.setTextColor(requireContext().resources.getColor(R.color.black))
                tv_firm_view?.background = requireContext().resources.getDrawable(R.drawable.button_right_background)
                tv_firm_view?.setTextColor(requireContext().resources.getColor(R.color.black))
                tv_deleted_view?.background = requireContext().resources.getDrawable(R.drawable.button_right_green_background)
                tv_deleted_view?.setTextColor(requireContext().resources.getColor(R.color.white))
                DOCUMENT_TYPE_TAG = "Deleted"
                ll_view_toggle?.visibility = VISIBLE
                view_group_layout?.visibility = GONE
                ll_matter_view?.visibility = GONE
                callDeletedDocumentWebservice()
            }

            btn_cancel = v.findViewById(R.id.btn_cancel)
            btn_cancel?.setOnClickListener {
                count_file = 0
                clear_upload()
                custom_spinner?.text = ""
                updateFilePickerLabel("")
                img_clear_icon1?.visibility = GONE
                img_dropdown_icon1?.visibility = VISIBLE
                view_document()
            }

            btn_upload?.setOnClickListener {
                callUploadDocumentWebservice()
            }

            ll_select_file = v.findViewById(R.id.ll_select_file)
            ll_select_file?.setOnClickListener {
                checkPermissionREAD_EXTERNAL_STORAGE(requireContext())
            }

            tv_selected_file_layout = v.findViewById(R.id.tv_selected_file)
            tv_selected_file_layout?.setOnClickListener {
                checkPermissionREAD_EXTERNAL_STORAGE(requireContext())
            }
            if (tv_selected_file_layout != null && tv_selected_file_layout!!.childCount > 1) {
                val child = tv_selected_file_layout!!.getChildAt(1)
                if (child is TextView) {
                    tv_file_count_label = child
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        client_id = ""
        matter_id = ""
        matterlist.clear()
        ll_client_name?.visibility = GONE
        list_client?.visibility = GONE
        list_client_view?.visibility = GONE
        list_matter?.visibility = GONE
        list_matter_view?.visibility = GONE

        if (getArguments() != null) {
            val doc_type = requireArguments().getString("document_type", "matter")
            currentModule = doc_type
            loadContentBasedOnModule()
        } else if (Constants.isCreate) {
            upload_documents()
        } else {
            loadMatterDocuments()
        }
        return v
    }

    private var tv_selected_file_layout: LinearLayout? = null
    private var currentModule: String = "matter"

    private fun updateFilePickerLabel(text: String) {
        val label = tv_file_count_label ?: return
        if (text.isEmpty()) {
            label.text = "Choose the files from your device or drag\n& drop them here"
        } else {
            label.text = text
        }
    }

    private fun switchViewMode(mode: Int) {
        if (adapter1 == null) return
        LoadingRecyclerview(rv_display_view_docs!!, context, mode)
        adapter1?.setViewType(mode)
        adapter1?.notifyDataSetChanged()
    }

    private fun Searchfilter(tv_search_client_view: TextInputEditText) {
        val query = tv_search_client_view.text.toString().trim().lowercase()
        val filteredList = ArrayList<ViewDocumentsModel>()
        if (view_docs_list.isNotEmpty()) {
            for (item in view_docs_list) {
                if ((item.name ?: "").lowercase().contains(query)) {
                    filteredList.add(item)
                }
            }
        }

        val ll_empty_state = view?.findViewById<LinearLayout>(R.id.ll_empty_state)
        val tv_empty_state_title = view?.findViewById<TextView>(R.id.tv_empty_state_title)
        val tv_empty_state_subtitle = view?.findViewById<TextView>(R.id.tv_empty_state_subtitle)

        if (filteredList.isEmpty()) {
            if (ll_empty_state != null) {
                ll_empty_state.visibility = VISIBLE
                tv_empty_state_title?.text = "No Matching Documents"
                tv_empty_state_subtitle?.text = "Try adjusting your search criteria"
            }
            pageItems.clear()
            adapter1?.setData(pageItems)
            rv_display_view_docs?.visibility = GONE
            ll_page_navigaiton?.visibility = GONE
            tv_no_document?.visibility = GONE
            return
        }

        ll_empty_state?.visibility = GONE
        rv_display_view_docs?.visibility = VISIBLE

        val startIndex = PaginationHelper.startIndexForCurrentPage(currentPage, 10)
        val endIndex = PaginationHelper.endIndexForCurrentPage(startIndex, filteredList.size, 10)
        if (filteredList.isEmpty()) {
            pageItems.clear()
            adapter1?.setData(pageItems)
        } else {
            pageItems = ArrayList(filteredList.subList(startIndex, endIndex))
            adapter1?.setData(pageItems)
        }
        currentPage = 1
        setupPagination(filteredList)
        UpdatePageButton(currentPage)
    }

    private var tv_no_document: TextView? = null

    private fun setupPagination(view_docs_list: ArrayList<ViewDocumentsModel>) {
        pageNumberLayout?.removeAllViews()
        pagebuttons.clear()
        val totalPages = PaginationHelper.calculateTotalNoOfPages(view_docs_list.size, 10)
        Log.d("total_pages", "$totalPages.." + view_docs_list.size)

        val ll_empty_state = view?.findViewById<LinearLayout>(R.id.ll_empty_state)
        val tv_empty_state_title = view?.findViewById<TextView>(R.id.tv_empty_state_title)
        val tv_empty_state_subtitle = view?.findViewById<TextView>(R.id.tv_empty_state_subtitle)

        if (view_docs_list.isEmpty()) {
            tv_no_document?.visibility = VISIBLE
            ll_page_navigaiton?.visibility = GONE

            if (ll_empty_state != null) {
                ll_empty_state.visibility = VISIBLE
                when (currentModule) {
                    "firm" -> {
                        tv_empty_state_title?.text = "No Firm Documents"
                        tv_empty_state_subtitle?.text = "Upload firm documents to get started"
                    }
                    "client" -> {
                        tv_empty_state_title?.text = "No Client Documents"
                        tv_empty_state_subtitle?.text = "Upload client documents to get started"
                    }
                    "matter" -> {
                        tv_empty_state_title?.text = "No Matter Documents"
                        tv_empty_state_subtitle?.text = "Upload matter documents to get started"
                    }
                    "delete" -> {
                        if ("solo" == Constants.CATEGORY) {
                            tv_empty_state_title?.text = "No Deleted Documents"
                            tv_empty_state_subtitle?.text = "Deleted documents will appear here"
                        } else {
                            tv_empty_state_title?.text = "No Documents Pending Approval"
                            tv_empty_state_subtitle?.text = "Documents pending approval will appear here"
                        }
                    }
                    else -> {
                        tv_empty_state_title?.text = "No Documents Found"
                        tv_empty_state_subtitle?.text = "Upload documents to get started"
                    }
                }
            }

            rv_display_view_docs?.visibility = GONE
            adapter1?.setData(ArrayList())

        } else {
            tv_no_document?.visibility = GONE
            ll_page_navigaiton?.visibility = VISIBLE
            ll_empty_state?.visibility = GONE
            rv_display_view_docs?.visibility = VISIBLE

            for (i in 1..totalPages) {
                val view_opponents = LayoutInflater.from(requireContext()).inflate(R.layout.page_number_layout, null)
                val pageButton = view_opponents.findViewById<Button>(R.id.page_number_button)
                pageButton.text = i.toString()
                val pageNumber = i
                pageButton.setOnClickListener {
                    currentPage = pageNumber
                    loadViewDocumentsRecyclerview(view_docs_list)
                    UpdatePageButton(currentPage)
                }
                pageNumberLayout?.addView(view_opponents)
                pagebuttons.add(pageButton)
            }
            iv_forward_button?.setOnClickListener {
                currentPage += 1
                val startIndex = PaginationHelper.startIndexForCurrentPage(currentPage, 10)
                val endIndex = PaginationHelper.endIndexForCurrentPage(startIndex, view_docs_list.size, 10)
                if (endIndex > end_temp) {
                    loadViewDocumentsRecyclerview(view_docs_list)
                    UpdatePageButton(currentPage)
                } else {
                    currentPage -= 1
                }
            }
            iv_backward_button?.setOnClickListener {
                currentPage -= 1
                if (currentPage > 0) {
                    loadViewDocumentsRecyclerview(view_docs_list)
                    UpdatePageButton(currentPage)
                } else {
                    currentPage = 1
                }
            }
        }
    }

    private fun UpdatePageButton(currentPage: Int) {
        for (i in pagebuttons.indices) {
            val pageButton = pagebuttons[i]
            if (currentPage >= 0) {
                if (i + 1 == currentPage) {
                    pageButton.setTextColor(requireActivity().getColor(R.color.white))
                    pageButton.background = requireActivity().getDrawable(R.drawable.blue_gradient_card)
                } else {
                    pageButton.setTextColor(requireActivity().getColor(R.color.blue))
                    pageButton.background = requireActivity().getDrawable(R.drawable.background_transparent)
                }
            }
        }
    }

    private fun scrollToHighlightedDocument(allDocs: ArrayList<ViewDocumentsModel>) {
        if (pendingHighlightIds == null || pendingHighlightIds!!.isEmpty()) return
        var highlightPosition = -1
        for (i in allDocs.indices) {
            if (pendingHighlightIds!!.contains(allDocs[i].id)) {
                highlightPosition = i
                break
            }
        }
        if (highlightPosition != -1) {
            val itemsPerPage = 10
            val targetPage = (highlightPosition / itemsPerPage) + 1
            if (targetPage != currentPage) {
                currentPage = targetPage
                val startIndex = PaginationHelper.startIndexForCurrentPage(currentPage, itemsPerPage)
                val endIndex = PaginationHelper.endIndexForCurrentPage(startIndex, allDocs.size, itemsPerPage)
                pageItems = ArrayList(allDocs.subList(startIndex, endIndex))
                adapter1?.setData(pageItems)
                UpdatePageButton(currentPage)
            }
        }
    }

    private fun view_document() {
        rv_documents?.visibility = GONE
        ll_buttons?.visibility = GONE
        tv_no_document?.visibility = GONE

        ll_view_docs?.visibility = VISIBLE
        rv_display_view_docs?.visibility = GONE
        rv_display_view_docs?.removeAllViews()
        view_group_layout?.visibility = GONE
        ll_upload_docs?.visibility = GONE
        rv_display_upload_groups_docs?.removeAllViews()
        clearClients()
        ismatter_chosen = false
        isUploadDoc = false

        if (ll_view_toggle != null) {
            ll_view_toggle?.visibility = VISIBLE
        }

        if (Constants.isFromNotification) {
            handleNotificationNavigation()
        } else if (getArguments() != null) {
            val doc_type = requireArguments().getString("document_type", "matter")
            currentModule = doc_type
            loadContentBasedOnModule()
        } else {
            loadMatterDocuments()
        }
        mViewModel?.setData(requireContext().getString(R.string.document_view))
    }

    private var ll_buttons: LinearLayout? = null

    private fun callViewDocumentWebservice() {
        try {
            val jsonObject = JSONObject()
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/documents/$DOCUMENT_TYPE_TAG", "VIEW_DOCUMENT", jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun callDeletedDocumentWebservice() {
        try {
            val jsonObject = JSONObject()
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "docs/deleted/list", "DELETED_DOCUMENT", jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun hideViewDoc() {
        hideClientView()
        hideMatterView()
        ll_page_navigaiton?.visibility = GONE
        tv_no_document?.visibility = GONE
    }

    private fun hideClientView() {
        custom_spinner3?.text = ""
        img_dropdown_icon3?.visibility = VISIBLE
        img_clear_icon3?.visibility = GONE
        list_client_view?.visibility = GONE
    }

    private fun hideMatterView() {
        custom_spinner4?.text = ""
        img_dropdown_icon4?.visibility = VISIBLE
        img_clear_icon4?.visibility = GONE
        list_matter_view?.visibility = GONE
    }

    private fun hideUploadDoc() {
        hideClientUpload()
        hideMatterUpload()
    }

    private fun hideClientUpload() {
        custom_spinner?.text = ""
        img_dropdown_icon1?.visibility = VISIBLE
        img_clear_icon1?.visibility = GONE
        list_client?.visibility = GONE
    }

    private fun hideMatterUpload() {
        custom_spinner2?.text = ""
        img_dropdown_icon2?.visibility = VISIBLE
        img_clear_icon2?.visibility = GONE
        list_matter?.visibility = GONE
    }

    private fun upload_documents() {
        isUploadDoc = true
        isUpdateTag = false
        clearClients()

        ll_upload_docs?.visibility = VISIBLE
        ll_view_docs?.visibility = GONE
        rv_documents?.removeAllViews()
        rv_display_upload_groups_docs?.removeAllViews()
        hideUploadDoc()

        if (ll_view_toggle != null) {
            ll_view_toggle?.visibility = GONE
        }

        hideDeletedBanner()

        mViewModel?.setData(requireContext().getString(R.string.upload_new))
        count_file = 0
        tv_select_groups?.text = ""
        tv_select_groups?.setHint(R.string.select_groups)

        updateFilePickerLabel("")

        if (currentModule == "firm") {
            UPLOAD_TAG = "Firm"
            ll_groups?.visibility = VISIBLE
            ll_category?.visibility = GONE
            ll_client_name?.visibility = GONE
            ll_matter?.visibility = GONE
            selected_groups_list.clear()
            tv_select_groups?.text = ""
            tv_select_groups?.setHint(R.string.select_groups)
            groupsList.clear()
            callGroupsWebservice()
        } else if (currentModule == "client") {
            UPLOAD_TAG = "Client"
            ll_category?.visibility = GONE
            ll_groups?.visibility = GONE
            ll_upload_groups?.visibility = GONE
            selected_client_groups_list.clear()
            client_groups_list.clear()
            rv_display_upload_groups_docs?.removeAllViews()
            client_id = ""
            matter_id = ""
            matterlist.clear()
            ll_client_name?.visibility = VISIBLE
            ll_matter?.visibility = GONE
            callClientWebservice()
        } else {
            UPLOAD_TAG = "Client"
            ll_category?.visibility = GONE
            ll_groups?.visibility = GONE
            ll_upload_groups?.visibility = GONE
            selected_client_groups_list.clear()
            client_groups_list.clear()
            rv_display_upload_groups_docs?.removeAllViews()
            client_id = ""
            matter_id = ""
            matterlist.clear()
            ll_client_name?.visibility = GONE
            ll_matter?.visibility = VISIBLE
            callLegalMatter()
        }
    }

    private fun callClientGroupsWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            val ClientGroups = JSONArray()
            val clients = JSONObject()
            for (j in 0 until clientsList.size) {
                if (clientsList[j].id == client_id) {
                    val clientsModel = clientsList[j]
                    clients.put("id", clientsModel.id)
                    clients.put("type", clientsModel.type)
                    ClientGroups.put(clients)
                }
            }
            jsonObject.put("clients", ClientGroups)
            jsonObject.put("matterid", matter_id)
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.PUT,
                "v3/documents/groupslist", "Client Groups", jsonObject.toString()
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
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/groups", "Groups", jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun open_add_tags_popup() {
        if (selected_documents_list.isNotEmpty()) {
            tags_list.clear()

            val selectedDoc = selected_documents_list[0]
            val existingTags = selectedDoc.tags

            Log.d("TagsDebug", "Loading existing tags for document: " + selectedDoc.name)
            Log.d("TagsDebug", "Existing tags JSON: " + (existingTags?.toString() ?: "null"))

            if (existingTags != null && existingTags.length() > 0) {
                val keys = existingTags.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    try {
                        val tagModel = DocumentsModel()
                        tagModel.tag_type = key
                        tagModel.tag_name = existingTags.getString(key)
                        tags_list.add(tagModel)
                        Log.d("TagsDebug", "Loaded tag: $key = " + existingTags.getString(key))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        val isedit_ref = booleanArrayOf(isedit)
        val edit_position_ref = intArrayOf(edit_position)
        val ll_added_tags_ref = arrayOf<LinearLayout?>(ll_added_tags)
        val tv_tag_type_ref = arrayOf<TextInputEditText?>(tv_tag_type)
        val tv_tag_name_ref = arrayOf<TextInputEditText?>(tv_tag_name)

        AndroidUtils.openAddTagsPopup(
            requireContext(),
            requireActivity(),
            requireActivity().layoutInflater,
            cl_document,
            isUpdateTag,
            tags_list,
            selected_documents_list,
            ll_added_tags_ref,
            tv_tag_type_ref,
            tv_tag_name_ref,
            isedit_ref,
            edit_position_ref
        ) { tagsList, dialog ->
            if (!isUpdateTag) {
                for (documentModel in selected_documents_list) {
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
                    if (tagsList != null) {
                        for (tagModel in tagsList) {
                            try {
                                combinedTags.put(tagModel.tag_type, tagModel.tag_name)
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                    documentModel.tags = combinedTags
                }
                for (i in docsList.indices) {
                    for (selectedDoc in selected_documents_list) {
                        if (docsList[i] === selectedDoc) {
                            docsList[i].tags = selectedDoc.tags
                            docsList[i].isChecked = false
                        }
                    }
                }
                is_clicked_add = true
                Hide_Add_EditMeta()
                dialog?.dismiss()
                if (llSelectedTags != null && selected_documents_list.isNotEmpty()) {
                    val editingDoc = selected_documents_list[0]
                    val mergedTagList = ArrayList<DocumentsModel>()
                    val json = editingDoc.tags
                    if (json != null) {
                        val keys = json.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            try {
                                val tag = DocumentsModel()
                                tag.tag_type = key
                                tag.tag_name = json.getString(key)
                                mergedTagList.add(tag)
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                    AndroidUtils.renderSelectedTags(
                        requireContext(),
                        llSelectedTags!!,
                        mergedTagList,
                        editingDoc,
                        cl_document,
                        requireActivity()
                    )
                }
            } else {
                callUpdateTag()
                is_clicked_add = true
                dialog?.dismiss()
            }
            ll_added_tags = ll_added_tags_ref[0]
            tv_tag_type = tv_tag_type_ref[0]
            tv_tag_name = tv_tag_name_ref[0]
            isedit = isedit_ref[0]
            edit_position = edit_position_ref[0]
        }
        ll_added_tags = ll_added_tags_ref[0]
        tv_tag_type = tv_tag_type_ref[0]
        tv_tag_name = tv_tag_name_ref[0]
    }

    private fun AddTag() {
        val count = docsList.size
        if (count == 0) {
            AndroidUtils.showAlert("Please select a document to add tags", requireActivity())
            return
        }
        btn_add_tags?.visibility = VISIBLE
        chk_box_layout?.alpha = 1.0f
        chk_select_all?.isEnabled = true
        chk_select_all?.background?.alpha = 255
        chk_select_all?.setOnClickListener {
            isselect_all_checked = !isselect_all_checked
            if (isselect_all_checked) {
                adapter?.selectOrDeselectAll(false)
            } else {
                adapter?.selectOrDeselectAll(true)
            }
        }
        val tag = "add_tag"
        loadRecyclerview(tag, subtag)
    }

    private fun EditMeta() {
        val tag = "edit_meta"
        loadRecyclerview(tag, subtag)
        Hide_Add_EditMeta()
    }

    private fun Hide_Add_EditMeta() {
        btn_add_tags?.visibility = GONE
        chk_box_layout?.alpha = 0.5f
        chk_select_all?.isEnabled = false
        chk_select_all?.isChecked = false
        chk_select_all?.background?.alpha = 50
        isselect_all_checked = true
        val tag = "hide_layout"
        loadRecyclerview(tag, subtag)
    }

    private fun EnableDownloadBackground() {
        tv_enable_download?.setTextColor(requireContext().resources.getColor(R.color.white))
        tv_enable_download?.background = requireContext().resources.getDrawable(R.drawable.button_left_green_background)
        tv_disable_download?.setTextColor(requireContext().resources.getColor(R.color.black))
        tv_disable_download?.background = requireContext().resources.getDrawable(R.drawable.button_right_background)
    }

    private fun DisableDownloadBackground() {
        tv_enable_download?.setTextColor(requireContext().resources.getColor(R.color.black))
        tv_enable_download?.background = requireContext().resources.getDrawable(R.drawable.button_left_background)
        tv_disable_download?.setTextColor(requireContext().resources.getColor(R.color.white))
        tv_disable_download?.background = requireContext().resources.getDrawable(R.drawable.button_right_green_background)
    }

    private fun EnableEncryptionBackground() {
        tv_enable_encryption?.setTextColor(requireContext().resources.getColor(R.color.white))
        tv_enable_encryption?.background = requireContext().resources.getDrawable(R.drawable.button_left_green_background)
        tv_disable_encryption?.setTextColor(requireContext().resources.getColor(R.color.black))
        tv_disable_encryption?.background = requireContext().resources.getDrawable(R.drawable.button_right_background)
    }

    private fun DisableEncryptionBackground() {
        tv_enable_encryption?.setTextColor(requireContext().resources.getColor(R.color.black))
        tv_enable_encryption?.background = requireContext().resources.getDrawable(R.drawable.button_left_background)
        tv_disable_encryption?.setTextColor(requireContext().resources.getColor(R.color.white))
        tv_disable_encryption?.background = requireContext().resources.getDrawable(R.drawable.button_right_green_background)
    }

    private fun hideFirmBackground() {
        tv_firm?.setTextColor(requireContext().resources.getColor(R.color.black))
        tv_firm?.background = requireContext().resources.getDrawable(R.drawable.button_right_background)
        tv_client?.background = requireContext().resources.getDrawable(R.drawable.button_left_green_background)
        tv_client?.setTextColor(requireContext().resources.getColor(R.color.white))
        ll_client_name?.visibility = VISIBLE
        ll_matter?.visibility = GONE
        callClientWebservice()
    }

    private fun hideClientBackground() {
        tv_client?.setTextColor(requireContext().resources.getColor(R.color.black))
        tv_client?.background = requireContext().resources.getDrawable(R.drawable.button_left_background)
        tv_firm?.background = requireContext().resources.getDrawable(R.drawable.button_right_green_background)
        tv_firm?.setTextColor(requireContext().resources.getColor(R.color.white))
        rv_documents?.removeAllViews()
        ll_client_name?.visibility = GONE
        ll_matter?.visibility = GONE
        upload_documents()
    }

    private fun hideviewFirmBackground() {
        tv_deleted_view?.background = requireContext().resources.getDrawable(R.drawable.button_right_background)
        tv_deleted_view?.setTextColor(requireContext().resources.getColor(R.color.black))
        tv_firm_view?.background = requireContext().resources.getDrawable(R.drawable.button_right_background)
        tv_firm_view?.setTextColor(requireContext().resources.getColor(R.color.black))
        tv_client_view?.background = requireContext().resources.getDrawable(R.drawable.button_left_green_background)
        tv_client_view?.setTextColor(requireContext().resources.getColor(R.color.white))
        tv_client_doc?.setText(R.string.list_of_client_documents)
        ll_merge_pdf?.visibility = VISIBLE
        ll_client_name_view?.visibility = VISIBLE
        hideDeletedBanner()
    }

    private fun hideviewClientBackground() {
        tv_deleted_view?.background = requireContext().resources.getDrawable(R.drawable.button_right_background)
        tv_deleted_view?.setTextColor(requireContext().resources.getColor(R.color.black))
        tv_client_view?.background = requireContext().resources.getDrawable(R.drawable.button_left_background)
        tv_client_view?.setTextColor(requireContext().resources.getColor(R.color.black))
        tv_firm_view?.background = requireContext().resources.getDrawable(R.drawable.button_right_green_background)
        tv_firm_view?.setTextColor(requireContext().resources.getColor(R.color.white))
        tv_client_doc?.setText(R.string.list_of_firm_documents)
        ll_merge_pdf?.visibility = GONE
        ll_client_name_view?.visibility = GONE
        hideDeletedBanner()
        callfilter_client_webservices()
    }

    private fun loadFirmDocuments() {
        currentModule = "firm"
        ismatter_chosen = false
        hideviewClientBackground()
        view_docs_list.clear()
        view_group_layout?.visibility = GONE
        ischecked_group_view = true
        ll_matter_view?.visibility = GONE
        DOCUMENT_TYPE_TAG = "firm"
    }

    private fun loadClientDocuments() {
        currentModule = "client"
        hideviewFirmBackground()
        DOCUMENT_TYPE_TAG = "client"
        CATEGORY_TAG = "client"
    }

    private fun loadMatterDocuments() {
        currentModule = "matter"
        hideviewFirmBackground()
        DOCUMENT_TYPE_TAG = "client"
        CATEGORY_TAG = "client"
    }

    private fun loadDeletedDocuments() {
        currentModule = "delete"
        ismatter_chosen = false
        hideviewFirmBackground()
        if (Constants.CATEGORY?.lowercase() == "solo") {
            tv_client_doc?.setText(R.string.deleted_documents)
        } else {
            tv_client_doc?.setText(R.string.list_of_documents_pending_approval)
        }
        ll_merge_pdf?.visibility = GONE
        ll_client_name_view?.visibility = GONE
        tv_client_view?.background = requireContext().resources.getDrawable(R.drawable.button_left_background)
        tv_client_view?.setTextColor(requireContext().resources.getColor(R.color.black))
        tv_firm_view?.background = requireContext().resources.getDrawable(R.drawable.button_right_background)
        tv_firm_view?.setTextColor(requireContext().resources.getColor(R.color.black))
        tv_deleted_view?.background = requireContext().resources.getDrawable(R.drawable.button_right_green_background)
        tv_deleted_view?.setTextColor(requireContext().resources.getColor(R.color.white))
        DOCUMENT_TYPE_TAG = "Deleted"
        ll_view_toggle?.visibility = VISIBLE
        view_group_layout?.visibility = GONE
        ll_matter_view?.visibility = GONE
        showDeletedBanner()
        callDeletedDocumentWebservice()
    }

    private fun showDeletedBanner() {
        if (ll_deleted_banner != null) {
            if (Constants.CATEGORY == "solo" && Constants.ROLE == "SU") {
                ll_deleted_banner?.visibility = VISIBLE
            }
        }
    }

    private fun hideDeletedBanner() {
        ll_deleted_banner?.visibility = GONE
    }

    private fun loadContentBasedOnModule() {
        when (currentModule) {
            "firm" -> loadFirmDocuments()
            "client" -> loadClientDocuments()
            "matter" -> loadMatterDocuments()
            "delete" -> loadDeletedDocuments()
            else -> loadMatterDocuments()
        }
    }

    private fun EnableUpload() {
        if (custom_spinner?.text.toString().isNotEmpty() && docsList.size > 0) {
            ll_hide_document_details?.visibility = VISIBLE
        } else {
            ll_hide_document_details?.visibility = GONE
        }
    }

    private fun callUploadDocumentWebservice() {
        val userList = ArrayList<String>()
        if (docsList.isEmpty()) {
            AndroidUtils.showAlert("Please select files to upload", requireActivity())
            return
        }
        try {
            completedUploads = 0
            isAlertShown = false
            totalUploads = docsList.size
            progress_dialog = AndroidUtils.get_progress(requireActivity())

            for (i in 0 until docsList.size) {
                val documentsModel = docsList[i]
                val requestObj = JSONObject()
                requestObj.put("name", documentsModel.name)
                requestObj.put("description", documentsModel.description)
                requestObj.put("filename", documentsModel.name)
                requestObj.put("expiration_date", documentsModel.expiration_date)
                requestObj.put("isdisabled", documentsModel.isIsenabled)
                requestObj.put("is_encrypted", documentsModel.isencrypted == true)

                if (UPLOAD_TAG == "Firm") {
                    requestObj.put("category", "firm")
                    val groups = JSONArray()
                    for (k in 0 until selected_groups_list.size) {
                        val documentsModel1 = selected_groups_list[k]
                        groups.put(documentsModel1.group_id)
                    }
                    requestObj.put("groups", groups)
                } else {
                    requestObj.put("category", "client")
                    requestObj.put("clients", client_id)
                    if (matter_id.isNotEmpty()) {
                        requestObj.put("matters", matter_id)
                    }
                    val groups = JSONArray()
                    for (k in 0 until selected_client_groups_list.size) {
                        val documentsModel1 = selected_client_groups_list[k]
                        groups.put(documentsModel1.group_id)
                    }
                    requestObj.put("groups", groups)
                }

                val new_file = documentsModel.file
                val file_name = new_file?.name ?: ""
                requestObj.put("filename", file_name)

                val content = if (new_file != null) convertfiletostring(new_file) else ""
                requestObj.put("file", content)

                val combinedTags = JSONObject()
                val existingTags = documentsModel.tags
                if (existingTags != null) {
                    val keys = existingTags.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        combinedTags.put(key, existingTags.get(key))
                    }
                }
                requestObj.put("tags", combinedTags)

                val userlist = JSONArray(userList)
                requestObj.put("viewmembers", userlist)

                WebServiceHelper.callHttpWebService(
                    this, requireContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "v3/document/upload", "Upload Document", requestObj.toString()
                )
            }
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            e.printStackTrace()
        }
    }

    private fun callLegalMatter() {
        try {
            val jsonObject = JSONObject()
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/matters", "Legal Matter", jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun callClientWebservice() {
        try {
            val jsonObject = JSONObject()
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/clients", "Clients List", jsonObject.toString()
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
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/corporate/list", "Corp Clients List", jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun callUpdateTag() {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val request = JSONObject()
            val combinedTags = JSONObject()
            for (tagModel in tags_list) {
                combinedTags.put(tagModel.tag_type, tagModel.tag_name)
            }
            request.put("tags", combinedTags)
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.PATCH,
                "v3/document/" + viewDocumentsModel?.id, "Update Tags", request.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun add_tags_listing(tagsList: ArrayList<DocumentsModel>?) {
        if (ll_added_tags != null) {
            ll_added_tags?.removeAllViews()
            if (tagsList != null) {
                for (i in 0 until tagsList.size) {
                    val tagModel = tagsList[i]
                    val view_added_tags = LayoutInflater.from(requireContext()).inflate(R.layout.displays_documents_list, null)
                    val tv_tag_name = view_added_tags.findViewById<TextView>(R.id.tv_document_name)
                    val iv_remove_tag = view_added_tags.findViewById<ImageView>(R.id.iv_cancel)
                    val tag_msg = tagModel.tag_type + " - " + tagModel.tag_name
                    tv_tag_name.text = tag_msg
                    iv_remove_tag.setOnClickListener {
                        ll_added_tags?.removeView(view_added_tags)
                        tagsList.removeAt(i)
                        add_tags_listing(tagsList)
                    }
                    ll_added_tags?.addView(view_added_tags)
                }
            }
        }
    }

    private fun clear_upload() {
        docsList.clear()
        adapter?.notifyDataSetChanged()
        EnableUpload()
    }

    fun checkPermissionREAD_EXTERNAL_STORAGE(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            BottomSheetUploadfile()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.launch(arrayOf(READ_EXTERNAL_STORAGE))
            } else {
                BottomSheetUploadfile()
            }
        } else {
            BottomSheetUploadfile()
        }
        return true
    }

    private fun BottomSheetUploadfile() {
        cl_document.alpha = 0.5f
        Constants.isDocEditor = false
        bottommSheetUploadDocument = BottomSheetUploadFile(cl_document)
        bottommSheetUploadDocument?.show(getParentFragmentManager(), "")
        bottommSheetUploadDocument?.setTargetFragment(this, 1)
    }

    @SuppressLint("Range")
    override fun getImagepath(imagepath: File?, ImageURI: Uri?) {
        try {
            if (imagepath == null) {
                mSelectedBitmap = null
                mSelectedUri = null
                val imageLoader = ImageLoader.getInstance()
                imageLoader.init(ImageLoaderConfiguration.createDefault(requireActivity()))
                
                val c = requireContext().contentResolver.query(ImageURI!!, null, null, null, null)
                if (c != null) {
                    c.moveToFirst()
                    val file_name = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    c.close()
                    
                    try {
                        file = getFile(requireContext(), ImageURI)
                        if (file != null && imageView != null) {
                            imageLoader.displayImage(Uri.fromFile(file).toString(), imageView)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    Hide_Add_EditMeta()
                    is_clicked_edit = true
                    is_clicked_add = true
                    count_file++
                    if (count_file > 0) {
                        updateFilePickerLabel("$count_file file" + (if (count_file > 1) "s" else "") + " selected")
                    } else {
                        updateFilePickerLabel("")
                    }
                    load_documents(docsList, file_name, file)
                }
            } else {
                file = getFile(requireContext(), ImageURI!!)
                Log.i("FILE", "Info:" + file.toString())
                val file_name = file!!.name
                count_file++
                Hide_Add_EditMeta()
                is_clicked_edit = true
                is_clicked_add = true
                if (count_file > 0) {
                    updateFilePickerLabel("$count_file file" + (if (count_file > 1) "s" else "") + " selected")
                } else {
                    updateFilePickerLabel("")
                }
                load_documents(docsList, file_name, file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getImageBitmap(bitmap: Bitmap?) {
        if (bitmap == null) return
        imageView?.setImageBitmap(bitmap)
        mSelectedBitmap = bitmap
        mSelectedUri = null
        val filesDir = requireContext().filesDir
        val imageFile = File(filesDir, "bitmap" + ".jpg")
        var os: OutputStream? = null
        try {
            os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.flush()
            os.close()
            file = imageFile
            count_file++
            updateFilePickerLabel("$count_file file" + (if (count_file > 1) "s" else "") + " selected")
            val documentsModel = DocumentsModel()
            documentsModel.name = file!!.name
            docsList.add(documentsModel)
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error writing bitmap", e)
        }
    }

    fun load_documents(docsList: ArrayList<DocumentsModel>, filename: String?, file: File?) {
        val documentsModel = DocumentsModel()
        documentsModel.name = filename
        documentsModel.description = ""
        documentsModel.file = file
        documentsModel.expiration_date = ""
        documentsModel.isIsenabled = true
        documentsModel.isencrypted = false
        docsList.add(documentsModel)
        val tag = "hide_layout"
        loadRecyclerview(tag, subtag)
        EnableUpload()
    }

    fun loadRecyclerview(tag: String, subtag: String) {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rv_documents?.layoutManager = layoutManager
        adapter = DocumentsListAdapter(docsList, tag, subtag, this, this)
        rv_documents?.adapter = adapter
    }

    fun clearListData() {
        docsList.clear()
        adapter?.notifyDataSetChanged()
        EnableUpload()
    }

    fun clearClients() {
        clientsList.clear()
        selected_client_groups_list.clear()
        selected_groups_list.clear()
        client_groups_list.clear()
        groupsList.clear()
        matterlist.clear()
    }

    private fun loadClients(data: JSONObject) {
        clientsList.clear()
        try {
            val clientsJsonArray = data.getJSONArray("clients")
            val allClientIdsBuilder = java.lang.StringBuilder()
            for (i in 0 until clientsJsonArray.length()) {
                val jsonObject = clientsJsonArray.getJSONObject(i)
                val clientsModel = ClientsModel()
                clientsModel.id = jsonObject.optString("id")
                clientsModel.name = jsonObject.optString("name")
                clientsModel.type = jsonObject.optString("type")
                clientsList.add(clientsModel)

                if (allClientIdsBuilder.isNotEmpty()) {
                    allClientIdsBuilder.append(",")
                }
                allClientIdsBuilder.append(jsonObject.optString("id"))
            }

            if (!isUploadDoc && clientsList.isNotEmpty()) {
                val allClientsModel = ClientsModel()
                allClientsModel.id = allClientIdsBuilder.toString()
                allClientsModel.name = "All Clients"
                allClientsModel.type = "all"
                clientsList.add(0, allClientsModel)
            }

            if (activity != null) {
                ClientAdapter = CommonSpinnerAdapter(requireActivity(), clientsList)
                if (isUploadDoc) {
                    list_client?.adapter = ClientAdapter
                    list_client?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                        val clientsModel = clientsList[position]
                        client_id = clientsModel.id ?: ""
                        AndroidUtils.DisplaySpinnerView(list_client, custom_spinner, clientsModel.name, img_dropdown_icon1, img_clear_icon1, true)
                        ischecked = true
                        selected_client_groups_list.clear()
                        client_groups_list.clear()
                        tv_select_upload_groups?.text = ""
                        tv_select_upload_groups?.setHint(R.string.select_groups)
                        ll_upload_client_group?.visibility = GONE
                        rv_upload_groups?.removeAllViews()
                        ll_upload_groups?.visibility = VISIBLE
                        callClientGroupsWebservice()
                        callMattersListWebservice()
                    }
                } else {
                    list_client_view?.adapter = ClientAdapter
                    list_client_view?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                        val clientsModel = clientsList[position]
                        client_id = clientsModel.id ?: ""
                        AndroidUtils.DisplaySpinnerView(list_client_view, custom_spinner3, clientsModel.name, img_dropdown_icon3, img_clear_icon3, true)
                        ischecked2 = true
                        CATEGORY_TAG = "client"
                        ll_matter_view?.visibility = VISIBLE
                        matterlist.clear()
                        matter_id = ""
                        ismatter_chosen = true
                        view_docs_list.clear()
                        rv_display_view_docs?.removeAllViews()
                        rv_display_view_docs?.visibility = GONE
                        ll_page_navigaiton?.visibility = GONE
                        callMattersListWebservice()
                        callfilter_client_webservices()
                    }

                    if (selectedId.isNotEmpty() && client_id.isEmpty()) {
                        for (i in clientsList.indices) {
                            if (clientsList[i].id == selectedId) {
                                val clientsModel = clientsList[i]
                                client_id = clientsModel.id ?: ""
                                AndroidUtils.DisplaySpinnerView(list_client_view, custom_spinner3, clientsModel.name, img_dropdown_icon3, img_clear_icon3, true)
                                CATEGORY_TAG = "client"
                                ll_matter_view?.visibility = VISIBLE
                                callMattersListWebservice()
                                break
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun callMattersListWebservice() {
        try {
            val jsonObject = JSONObject()
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/matters", "Legal Matter", jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun loadCorpClients(data: JSONObject) {
        try {
            val corpArray = data.getJSONArray("corporate")
            for (i in 0 until corpArray.length()) {
                val jsonObject = corpArray.getJSONObject(i)
                val clientsModel = ClientsModel()
                clientsModel.id = jsonObject.optString("id")
                clientsModel.name = jsonObject.optString("name")
                clientsModel.type = "corporate"
                clientsList.add(clientsModel)
            }
            if (activity != null) {
                ClientAdapter = CommonSpinnerAdapter(requireActivity(), clientsList)
                if (isUploadDoc) {
                    list_client?.adapter = ClientAdapter
                } else {
                    list_client_view?.adapter = ClientAdapter
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadMatters(matters: JSONArray) {
        matterlist.clear()
        try {
            var relatedMattersCount = 0
            val allMatterIdsBuilder = java.lang.StringBuilder()

            for (i in 0 until matters.length()) {
                val jsonObject = matters.getJSONObject(i)
                val clientsArray = jsonObject.getJSONArray("clients")
                var isMatched = false

                for (j in 0 until clientsArray.length()) {
                    val clientObj = clientsArray.getJSONObject(j)
                    if (clientObj.getString("id") == client_id) {
                        isMatched = true
                        break
                    }
                }

                if (isMatched) {
                    relatedMattersCount++
                    val mattersModel = MattersModel()
                    mattersModel.id = jsonObject.optString("id")
                    mattersModel.name = jsonObject.optString("title")
                    matterlist.add(mattersModel)

                    if (allMatterIdsBuilder.isNotEmpty()) {
                        allMatterIdsBuilder.append(",")
                    }
                    allMatterIdsBuilder.append(jsonObject.optString("id"))
                }
            }

            if (!isUploadDoc && relatedMattersCount > 1) {
                val allMattersModel = MattersModel()
                allMattersModel.id = allMatterIdsBuilder.toString()
                allMattersModel.name = "All Matters"
                matterlist.add(0, allMattersModel)
            }

            if (activity != null) {
                Matteradapter = CommonSpinnerAdapter(requireActivity(), matterlist)
                if (isUploadDoc) {
                    list_matter?.adapter = Matteradapter
                    list_matter?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                        val mattersModel = matterlist[position]
                        matter_id = mattersModel.id ?: ""
                        AndroidUtils.DisplaySpinnerView(list_matter, custom_spinner2, mattersModel.name, img_dropdown_icon2, img_clear_icon2, true)
                        ischecked_matter = true
                        selected_client_groups_list.clear()
                        client_groups_list.clear()
                        tv_select_upload_groups?.text = ""
                        tv_select_upload_groups?.setHint(R.string.select_groups)
                        ll_upload_client_group?.visibility = GONE
                        rv_upload_groups?.removeAllViews()
                        ll_upload_groups?.visibility = VISIBLE
                        callClientGroupsWebservice()
                    }
                } else {
                    list_matter_view?.adapter = Matteradapter
                    list_matter_view?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                        val mattersModel = matterlist[position]
                        matter_id = mattersModel.id ?: ""
                        AndroidUtils.DisplaySpinnerView(list_matter_view, custom_spinner4, mattersModel.name, img_dropdown_icon4, img_clear_icon4, true)
                        ischecked_matter2 = true
                        ismatter_chosen = true
                        view_docs_list.clear()
                        rv_display_view_docs?.removeAllViews()
                        rv_display_view_docs?.visibility = GONE
                        ll_page_navigaiton?.visibility = GONE
                        callfilter_client_webservices()
                    }

                    if (selectedId.isNotEmpty() && matter_id.isEmpty()) {
                        for (i in matterlist.indices) {
                            if (matterlist[i].id == selectedId) {
                                val mattersModel = matterlist[i]
                                matter_id = mattersModel.id ?: ""
                                AndroidUtils.DisplaySpinnerView(list_matter_view, custom_spinner4, mattersModel.name, img_dropdown_icon4, img_clear_icon4, true)
                                break
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(view: View) {
        // Implementation of AndroidUtils.OnModuleClickListener
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                if (httpResult.requestType == "View Encrypted Doc") {
                    if (progress_dialog != null && progress_dialog!!.isShowing) {
                        AndroidUtils.dismiss_dialog(progress_dialog)
                    }
                    val image_data = httpResult.responseContent
                    encrypted_doc(image_data)
                } else if (httpResult.requestType == "Upload Document") {
                    val result = JSONObject(httpResult.responseContent)
                    completedUploads++
                    val isError = result.optBoolean("error")
                    val msg = result.optString("msg")
                    if (completedUploads == totalUploads && !isAlertShown) {
                        isAlertShown = true
                        if (progress_dialog != null && progress_dialog!!.isShowing) {
                            AndroidUtils.dismiss_dialog(progress_dialog)
                        }
                        if (!isError) {
                            AndroidUtils.showAlert(msg, activity, "")
                            rv_documents?.removeAllViews()
                            view_document()
                            clearListData()
                        } else {
                            AndroidUtils.showAlert(msg, activity)
                        }
                    }
                } else {
                    if (progress_dialog != null && progress_dialog!!.isShowing) {
                        AndroidUtils.dismiss_dialog(progress_dialog)
                    }
                    val result = JSONObject(httpResult.responseContent)
                    if (httpResult.requestType == "Clients List") {
                        val data = result.getJSONObject("data")
                        try {
                            loadClients(data)
                            callCorpClientWebservice()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else if (httpResult.requestType == "Update Documents") {
                        if (!result.optBoolean("error")) {
                            Dialog?.dismiss()
                            rv_display_view_docs?.removeAllViews()
                            callfilter_client_webservices()
                            val msg = result.optString("msg")
                            AndroidUtils.showAlert(msg, activity, "Success")
                        } else {
                            val msg = result.optString("msg")
                            AndroidUtils.showAlert(msg, activity)
                        }
                    } else if (httpResult.requestType == "Corp Clients List") {
                        try {
                            loadCorpClients(result)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else if (httpResult.requestType == "Deleted Documents") {
                        isDownload = false
                        val jsonObject = result.optJSONObject("data")
                        if (result.has("data")) {
                            assert(jsonObject != null)
                            val url = jsonObject.optString("url")
                            checkViewType(url)
                        } else {
                            val msg = result.getString("msg")
                            AndroidUtils.showAlert_docs("Success!", msg, activity)
                            callDeletedDocumentWebservice()
                        }
                    } else if (httpResult.requestType == "Legal Matter") {
                        val matters = result.getJSONArray("matters")
                        loadMatters(matters)
                    } else if (httpResult.requestType == "Groups") {
                        val data = result.optJSONArray("data")
                        loadGroupsData(data, groupsList, false)
                    } else if (httpResult.requestType == "Client Groups") {
                        val data = result.optJSONArray("data")
                        loadGroupsData(data, client_groups_list, true)
                    } else if (httpResult.requestType == "Display FilterDocuments") {
                        val data = result.optJSONArray("data")
                        load_view_doc(data)
                        Log.d("TAG_VIEW_CLIENT", data.toString())
                    } else if (httpResult.requestType == "Display MergeFilterDocuments") {
                        val data = result.optJSONObject("data")
                        val array = data.optJSONArray("items")
                        load_view_doc(array)
                        Log.d("TAG_VIEW_CLIENT", data.toString())
                    } else if (httpResult.requestType == "VIEW_DOCUMENT") {
                        val docs = result.optJSONArray("docs")
                        loadViewDocuments(docs)
                        Log.d("TAG_view", docs.toString())
                    } else if (httpResult.requestType == "DELETED_DOCUMENT") {
                        val error = result.optBoolean("error")
                        if (!error) {
                            val docs = result.getJSONArray("documents")
                            load_view_doc(docs)
                            Log.d("TAG_view", docs.toString())
                        } else {
                            val msg = result.optString("msg")
                            AndroidUtils.showAlert(msg, activity)
                        }
                    } else if (httpResult.requestType == "Download Document") {
                        val iserror = result.getBoolean("error")
                        var msg = ""
                        if (iserror) {
                            msg = result.getString("msg")
                            AndroidUtils.showAlert(msg, activity)
                        } else {
                            rv_display_view_docs?.removeAllViews()
                            var url = ""
                            if (!is_MergePdfClicked) {
                                url = result.optString("url")
                            } else {
                                val data = result.optJSONObject("data")
                                assert(data != null)
                                url = data.optString("url")
                            }
                            AndroidUtils.showAlert(
                                "You have successfully downloaded the document.",
                                activity, "Success"
                            )
                            FileDownloader.downloadFile(requireContext(), url, "Download1")
                            callfilter_client_webservices()
                        }
                    } else if (httpResult.requestType == "Delete Documents") {
                        val iserror = result.getBoolean("error")
                        var msg = ""
                        if (iserror) {
                            msg = result.getString("msg")
                            AndroidUtils.showAlert(msg, activity)
                        } else {
                            rv_display_view_docs?.removeAllViews()
                            callfilter_client_webservices()
                            msg = result.optString("msg")
                            AndroidUtils.showAlert(msg, activity)
                        }
                    } else if (httpResult.requestType == "Delete Merge Documents") {
                        val iserror = result.getBoolean("error")
                        val msg = result.getString("msg")
                        if (iserror) {
                            AndroidUtils.showAlert(msg, activity)
                        } else {
                            AndroidUtils.showAlert(msg, activity)
                            rv_display_view_docs?.removeAllViews()
                            callfilter_client_webservices()
                        }
                    } else if (httpResult.requestType == "Decrypt Documents") {
                        val iserror = result.getBoolean("error")
                        var msg = ""
                        if (iserror) {
                            msg = result.getString("msg")
                            AndroidUtils.showAlert(msg, activity)
                        } else {
                            rv_display_view_docs?.removeAllViews()
                            callfilter_client_webservices()
                            msg = "You have successfully \n decrypted document information."
                            AndroidUtils.showAlert(msg, activity, "Success")
                        }
                    } else if (httpResult.requestType == "Decrypt Doc"
                        || httpResult.requestType == "Other Doc View") {
                        if (!result.getBoolean("error")) {
                            var url = ""
                            val jsonObject = result.optJSONObject("data")
                            assert(jsonObject != null)
                            url = jsonObject.optString("url")
                            if (isDownload) {
                                AndroidUtils.showAlert(
                                    "You have successfully downloaded the document.",
                                    activity, "Success"
                                )
                                FileDownloader.downloadFile(requireContext(), url, "Download1")
                            } else {
                                loadDisplayDocuments(url)
                            }
                            Log.d("TAG_Image", url)
                        } else {
                            AndroidUtils.showAlert(result.optString("msg"), activity)
                        }
                    } else if (httpResult.requestType == "Update Tags") {
                        val iserror = result.getBoolean("error")
                        var msg = ""
                        if (iserror) {
                            msg = result.getString("msg")
                            AndroidUtils.showAlert(msg, activity)
                        } else {
                            msg = result.getString("msg")
                            AndroidUtils.showAlert(msg, activity, "Success")
                        }
                        callfilter_client_webservices()
                    } else if (httpResult.requestType == "Encrypt Documents") {
                        var msg = result.optString("msg")
                        val error = result.getBoolean("error")
                        if (!error) {
                            msg = "You have successfully \n encrypted document information."
                            rv_display_view_docs?.removeAllViews()
                            callfilter_client_webservices()
                            AndroidUtils.showAlert(msg, activity, "Success")
                        } else {
                            AndroidUtils.showAlert(msg, activity)
                        }
                    } else if (httpResult.requestType == "Enabled Documents") {
                        val msg = result.optString("msg")
                        val error = result.getBoolean("error")
                        if (!error) {
                            rv_display_view_docs?.removeAllViews()
                            callfilter_client_webservices()
                            AndroidUtils.showAlert(msg, activity, "Success")
                        } else {
                            AndroidUtils.showAlert(msg, activity)
                        }
                    } else if (httpResult.requestType == "Disabled Documents") {
                        val msg = result.optString("msg")
                        val error = result.optBoolean("error")
                        if (!error) {
                            AndroidUtils.showAlert(msg, activity, "Success")
                            rv_display_view_docs?.removeAllViews()
                            callfilter_client_webservices()
                        } else {
                            AndroidUtils.showAlert(msg, activity)
                        }
                    } else if (httpResult.requestType == "Display Documents") {
                        if (!result.getBoolean("error")) {
                            val url = if (!is_MergePdfClicked) {
                                result.optString("url")
                            } else {
                                val jsonObject = result.optJSONObject("data")
                                assert(jsonObject != null)
                                jsonObject.optString("url")
                            }
                            checkViewType(url)
                            Log.d("TAG_Image", url)
                        } else {
                            AndroidUtils.showAlert(result.optString("msg"), activity)
                        }
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                AndroidUtils.showAlert(e.message, activity)
                if (progress_dialog != null && progress_dialog!!.isShowing) {
                    AndroidUtils.dismiss_dialog(progress_dialog)
                }
            }
        } else if (httpResult.result == WebServiceHelper.ServiceCallStatus.Failed) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            try {
                val result = JSONObject(httpResult.responseContent)
                AndroidUtils.showErrorAlert(result.optString("msg"), activity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            AndroidUtils.showErrorAlert(
                httpResult.responseContent.toString(), activity
            )
        }
    }

    private fun encrypted_doc(url: String) {
        val dialogBuilder = AlertDialog.Builder(activity)
        cl_document.alpha = 0.5f
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.view_documents, null)
        val iv_image = view.findViewById<ImageView>(R.id.doc_image)
        val header = view.findViewById<TextView>(R.id.header_name)
        val idPDFView = view.findViewById<PDFView>(R.id.idPDFView)
        header.text = doc_name
        val iv_close_edit_docs = view.findViewById<ImageView>(R.id.close_edit_docs)
        val list = ArrayList(Arrays.asList(*CONTENT_TYPE.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
        if (list.size > 1 && (list[1].lowercase() == "apng" || list[1].lowercase() == "avif"
                    || list[1].lowercase() == "gif" || list[1].lowercase() == "jpeg"
                    || list[1].lowercase() == "png" || list[1].lowercase() == "svg"
                    || list[1].lowercase() == "webp" || list[1].lowercase() == "jpg")) {
            idPDFView.visibility = GONE
            try {
                val imageData = url.toByteArray(StandardCharsets.ISO_8859_1)
                Log.d("Image Decode", "Decoded byte array length: " + imageData.size)
                var bitmap: Bitmap? = null
                try {
                    bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                    if (bitmap != null) {
                        Log.d("Image Decode", "Bitmap decoded successfully")
                    } else {
                        Log.e("Image Decode1", "Bitmap decoding returned null")
                    }
                } catch (e: Exception) {
                    Log.e("Image Decode2", "Error decoding byte array to Bitmap", e)
                }
                iv_image.setImageBitmap(bitmap)
                iv_image.visibility = VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            iv_image.visibility = GONE
            PDFviewer(idPDFView, url)
        }
        val dialog = dialogBuilder.create()
        iv_close_edit_docs.setOnClickListener { dialog.dismiss() }
        dialog.setOnDismissListener { cl_document.alpha = 1.0f }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun checkViewType(url: String) {
        val isImage = File_Content_Type.isImage(tempDocModel?.content_type)
        val isPDF = File_Content_Type.isPDF(tempDocModel?.content_type)
        val isEncrypted = tempDocModel?.added_encryption == true || tempDocModel?.is_encrypted == true
        if (isEncrypted) {
            callDecryptApi(tempDocModel?.id ?: "", false)
        } else if (!is_MergePdfClicked) {
            loadDisplayDocuments(url)
        } else if (!isPDF && !isImage) {
            callOtherDocViewApi(tempDocModel?.id ?: "")
        } else {
            loadDisplayDocuments(url)
        }
    }

    private fun loadDisplayDocuments(url: String) {
        val dialogBuilder = AlertDialog.Builder(activity)
        cl_document.alpha = 0.5f
        val isImage = File_Content_Type.isImage(tempDocModel?.content_type)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.view_documents, null)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_pdf)
        val iv_image = view.findViewById<ImageView>(R.id.doc_image)
        val idPDFView = view.findViewById<PDFView>(R.id.idPDFView)
        val header = view.findViewById<TextView>(R.id.header_name)
        val iv_close_edit_docs = view.findViewById<ImageView>(R.id.close_edit_docs)
        header.text = doc_name
        iv_image.visibility = GONE
        idPDFView.visibility = GONE
        val dialog = dialogBuilder.create()
        Dialog = dialog
        val pdfTask = arrayOfNulls<RetrievePDFfromUrl>(1)
        iv_close_edit_docs.setOnClickListener {
            try {
                idPDFView?.recycle()
                if (pdfTask[0] != null) {
                    pdfTask[0]?.cancelLoading()
                    pdfTask[0]?.cancel(true)
                }
            } catch (ignored: Exception) {
            }
            dialog.dismiss()
        }
        val lowerUrl = url.lowercase()
        val urlIsPDF = lowerUrl.contains("application/pdf") || lowerUrl.contains(".pdf")
        if (urlIsPDF) {
            idPDFView.visibility = VISIBLE
            progressBar.visibility = VISIBLE
            pdfTask[0] = RetrievePDFfromUrl(idPDFView, progressBar)
            pdfTask[0]?.execute(url)
        } else {
            if (isImage) {
                iv_image.visibility = VISIBLE
                Glide.with(requireContext()).load(url)
                    .placeholder(R.drawable.progress_animation)
                    .centerCrop().into(iv_image)
            } else {
                idPDFView.visibility = VISIBLE
                progressBar.visibility = VISIBLE
                pdfTask[0] = RetrievePDFfromUrl(idPDFView, progressBar)
                pdfTask[0]?.execute(url)
            }
        }
        dialog.setOnDismissListener { cl_document.alpha = 1.0f }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun handleDocumentDisplay(
        url: String, docModel: ViewDocumentsModel,
        idPDFView: PDFView, iv_image: ImageView
    ) {
        val isImage = File_Content_Type.isImage(docModel.content_type)
        val isPDF = File_Content_Type.isPDF(docModel.content_type)
        val isEncrypted = docModel.added_encryption || docModel.is_encrypted
        if (isEncrypted) {
            Dialog?.dismiss()
            callDecryptApi(docModel.id ?: "", false)
            return
        }
        if (isImage) {
            idPDFView.visibility = GONE
            iv_image.visibility = VISIBLE
            Glide.with(requireContext()).load(url)
                .placeholder(R.drawable.progress_animation)
                .centerCrop().into(iv_image)
        } else if (isPDF) {
            iv_image.visibility = GONE
            idPDFView.visibility = VISIBLE
        } else {
            Dialog?.dismiss()
            callOtherDocViewApi(docModel.id ?: "")
        }
    }

    private fun load_view_doc(docs: JSONArray?) {
        if (docs == null) return
        try {
            view_docs_list.clear()
            for (i in 0 until docs.length()) {
                val viewDocumentsModel = ViewDocumentsModel()
                val jsonObject = docs.getJSONObject(i)
                viewDocumentsModel.created = jsonObject.optString("created")
                viewDocumentsModel.content_type = jsonObject.optString("content_type")
                viewDocumentsModel.description = jsonObject.optString("description")
                viewDocumentsModel.expiration_date = jsonObject.optString("expiration_date")
                viewDocumentsModel.filename = jsonObject.optString("filename")
                viewDocumentsModel.id = jsonObject.optString("id")
                viewDocumentsModel.isdisabled = jsonObject.optBoolean("isdisabled")
                viewDocumentsModel.is_disabled = jsonObject.optBoolean("is_disabled")
                viewDocumentsModel.is_encrypted = jsonObject.optBoolean("is_encrypted")
                viewDocumentsModel.added_encryption = jsonObject.optBoolean("added_encryption")
                viewDocumentsModel.is_password = jsonObject.optBoolean("is_password")
                viewDocumentsModel.name = jsonObject.optString("name")
                viewDocumentsModel.origin = jsonObject.optString("origin")
                if (!is_MergePdfClicked) {
                    viewDocumentsModel.uploaded_by = jsonObject.optString("createdby")
                } else {
                    viewDocumentsModel.uploaded_by = jsonObject.optString("uploaded_by")
                }
                viewDocumentsModel.doc_type = jsonObject.optString("doctype")
                viewDocumentsModel.category = jsonObject.optString("category")
                viewDocumentsModel.deletedBy = jsonObject.optString("deletedBy")
                val rawDeletedOn = jsonObject.optString("deletedOn")
                val fixedDeletedOn = AndroidUtils.normalizeDeletedOn(rawDeletedOn)
                viewDocumentsModel.deletedOn = fixedDeletedOn
                viewDocumentsModel.tag = jsonObject.optJSONObject("tags")
                viewDocumentsModel.tagslist = jsonObject.optJSONArray("tag")
                view_docs_list.add(viewDocumentsModel)
            }

            Collections.sort(view_docs_list, object : Comparator<ViewDocumentsModel> {
                val sdf = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault())
                override fun compare(doc1: ViewDocumentsModel, doc2: ViewDocumentsModel): Int {
                    return try {
                        val date1 = sdf.parse(doc1.created)
                        val date2 = sdf.parse(doc2.created)
                        date2.compareTo(date1)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                        0
                    }
                }
            })

            if (pendingHighlightIds != null && pendingHighlightIds!!.isNotEmpty()) {
                val highlighted = ArrayList<ViewDocumentsModel>()
                val nonHighlighted = ArrayList<ViewDocumentsModel>()
                for (doc in view_docs_list) {
                    if (pendingHighlightIds!!.contains(doc.id)) {
                        highlighted.add(doc)
                    } else {
                        nonHighlighted.add(doc)
                    }
                }
                view_docs_list.clear()
                view_docs_list.addAll(highlighted)
                view_docs_list.addAll(nonHighlighted)
            }

            currentPage = 1
            setupPagination(view_docs_list)
            loadViewDocumentsRecyclerview(view_docs_list)
            UpdatePageButton(currentPage)
        } catch (e: JSONException) {
            e.printStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    private fun loadViewDocuments(docs: JSONArray?) {
        if (docs == null) return
        try {
            view_docs_list.clear()
            for (i in 0 until docs.length()) {
                val viewDocumentsModel = ViewDocumentsModel()
                val jsonObject = docs.getJSONObject(i)
                viewDocumentsModel.created = jsonObject.optString("created")
                viewDocumentsModel.content_type = jsonObject.optString("content_type")
                viewDocumentsModel.description = jsonObject.optString("description")
                viewDocumentsModel.expiration_date = jsonObject.optString("expiration_date")
                viewDocumentsModel.filename = jsonObject.optString("filename")
                viewDocumentsModel.id = jsonObject.optString("id")
                viewDocumentsModel.isdisabled = jsonObject.optBoolean("isdisabled")
                viewDocumentsModel.is_disabled = jsonObject.optBoolean("is_disabled")
                viewDocumentsModel.is_encrypted = jsonObject.optBoolean("is_encrypted")
                viewDocumentsModel.added_encryption = jsonObject.optBoolean("added_encryption")
                viewDocumentsModel.is_password = jsonObject.optBoolean("is_password")
                viewDocumentsModel.name = jsonObject.optString("name")
                viewDocumentsModel.origin = jsonObject.optString("origin")
                viewDocumentsModel.uploaded_by = jsonObject.optString("uploaded_by")
                viewDocumentsModel.doc_type = jsonObject.optString("doctype")
                viewDocumentsModel.deletedBy = jsonObject.optString("deletedBy")
                val rawDeletedOn = jsonObject.optString("deletedOn")
                val fixedDeletedOn = AndroidUtils.normalizeDeletedOn(rawDeletedOn)
                viewDocumentsModel.deletedOn = fixedDeletedOn
                viewDocumentsModel.tag = jsonObject.optJSONObject("tags")
                viewDocumentsModel.tagslist = jsonObject.optJSONArray("tag")
                view_docs_list.add(viewDocumentsModel)
                Log.d("VIEW_POSITION", view_docs_list[i].toString())
            }
            currentPage = 1
            loadViewDocumentsRecyclerview(view_docs_list)
        } catch (e: JSONException) {
            e.printStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    private fun loadViewDocumentsRecyclerview(view_docs_list: ArrayList<ViewDocumentsModel>) {
        try {
            val ll_empty_state = view?.findViewById<LinearLayout>(R.id.ll_empty_state)
            val tv_empty_state_title = view?.findViewById<TextView>(R.id.tv_empty_state_title)
            val tv_empty_state_subtitle = view?.findViewById<TextView>(R.id.tv_empty_state_subtitle)

            if (view_docs_list.isEmpty()) {
                if (ll_empty_state != null) {
                    ll_empty_state.visibility = VISIBLE
                    when (currentModule) {
                        "firm" -> {
                            tv_empty_state_title?.text = "No Firm Documents Yet!"
                            tv_empty_state_subtitle?.text = "Secure and organize your documents by start uploading it."
                        }
                        "client" -> {
                            tv_empty_state_title?.text = "No Client Documents Yet!"
                            tv_empty_state_subtitle?.text = "Secure and organize your documents by start uploading it."
                        }
                        "matter" -> {
                            tv_empty_state_title?.text = "No Matter Documents Yet!"
                            tv_empty_state_subtitle?.text = "Secure and organize your documents by start uploading it."
                        }
                        "delete" -> {
                            if ("solo" == Constants.CATEGORY?.lowercase()) {
                                tv_empty_state_title?.text = "No Deleted Documents Yet!"
                                tv_empty_state_subtitle?.text = "Deleted documents will appear here"
                            } else {
                                tv_empty_state_title?.text = "No Documents Pending Approval Yet!"
                                tv_empty_state_subtitle?.text = "Documents pending approval will appear here"
                            }
                        }
                        else -> {
                            tv_empty_state_title?.text = "No Documents Found"
                            tv_empty_state_subtitle?.text = "Upload documents to get started"
                        }
                    }
                }

                rv_display_view_docs?.visibility = GONE
                ll_page_navigaiton?.visibility = GONE
                tv_no_document?.visibility = GONE

                adapter1?.setData(ArrayList())
                return
            }

            ll_empty_state?.visibility = GONE
            tv_no_document?.visibility = GONE

            if (isGridView) {
                val spanCount = if (DynamicUtils.isTablet(requireContext())) 3 else 2
                rv_display_view_docs?.layoutManager = GridLayoutManager(requireContext(), spanCount)
            } else {
                rv_display_view_docs?.layoutManager = LinearLayoutManager(requireContext())
            }

            adapter1 = View_documents_adapter(
                view_docs_list, this, requireContext(),
                is_MergePdfClicked, DOCUMENT_TYPE_TAG, pendingHighlightIds
            )
            adapter1?.setViewType(
                if (isGridView) View_documents_adapter.VIEW_TYPE_GRID
                else View_documents_adapter.VIEW_TYPE_LIST
            )
            rv_display_view_docs?.adapter = adapter1
            AndroidUtils.LoadAnimation(rv_display_view_docs, requireContext())

            if (view_docs_list.isNotEmpty()) {
                val startIndex = PaginationHelper.startIndexForCurrentPage(currentPage, 10)
                val endIndex = PaginationHelper.endIndexForCurrentPage(startIndex, view_docs_list.size, 10)
                end_temp = endIndex
                pageItems = ArrayList(view_docs_list.subList(startIndex, endIndex))
                adapter1?.setData(pageItems)
                if (pendingHighlightIds != null && pendingHighlightIds!!.isNotEmpty()) {
                    scrollToHighlightedDocument(view_docs_list)
                }
                UpdatePageButton(1)

                rv_display_view_docs?.visibility = VISIBLE
                ll_page_navigaiton?.visibility = VISIBLE
            }

            rv_display_view_docs?.post {
                if (context == null) return@post
                if (adapter1 != null && adapter1!!.itemCount > 0) {
                    ll_empty_state?.visibility = GONE
                    rv_display_view_docs?.visibility = VISIBLE
                    ll_page_navigaiton?.visibility = VISIBLE
                }
            }

            if (tv_search_client_view.text.toString().isNotEmpty()) {
                Searchfilter(tv_search_client_view)
            }
            if (tv_search_client_views.text.toString().isNotEmpty()) {
                Searchfilter(tv_search_client_views)
            }
            tv_search_client_view.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(cs: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun onTextChanged(cs: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(editable: Editable) {
                    Searchfilter(tv_search_client_view)
                }
            })
            tv_search_client_views.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(cs: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun onTextChanged(cs: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(editable: Editable) {
                    Searchfilter(tv_search_client_views)
                }
            })

            pendingHighlightIds = null

        } catch (e: Exception) {
            e.printStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    private fun filterList(
        list: ArrayList<ViewDocumentsModel>,
        charString: String
    ): ArrayList<ViewDocumentsModel> {
        isListFiltered = false
        if (charString.isEmpty()) {
            return list
        } else {
            val filteredList = ArrayList<ViewDocumentsModel>()
            val pattern = Pattern.compile(
                Pattern.quote(charString), Pattern.CASE_INSENSITIVE
            )
            for (row in list) {
                if (pattern.matcher(AndroidUtils.isNull(row.name).lowercase()).find()
                    || pattern.matcher(AndroidUtils.isNull(row.filename).lowercase()).find()) {
                    filteredList.add(row)
                }
            }
            return if (filteredList.isEmpty()) {
                ArrayList()
            } else {
                isListFiltered = true
                filteredList
            }
        }
    }

    private fun loadGroupsData(
        data: JSONArray?, groupsList: ArrayList<DocumentsModel>,
        isclient_groups: Boolean
    ) {
        if (data == null) return
        groupsList.clear()
        selected_client_groups_list.clear()
        try {
            val allIds = java.lang.StringBuilder()

            for (i in 0 until data.length()) {
                val jsonObject = data.getJSONObject(i)
                val documentsModel = DocumentsModel()
                documentsModel.group_id = jsonObject.optString("id")
                documentsModel.group_name = jsonObject.optString("name")
                if (jsonObject.optString("name") != "AAM"
                    && jsonObject.optString("name") != "SuperUser") {
                    documentsModel.group_id = jsonObject.optString("id")
                    documentsModel.group_name = jsonObject.optString("name")
                    if (isclient_groups) {
                        documentsModel.isGroupChecked = true
                        selected_client_groups_list.add(documentsModel)
                    }
                    groupsList.add(documentsModel)

                    if (allIds.isNotEmpty()) allIds.append(",")
                    allIds.append(jsonObject.optString("id"))
                }
            }

            if (!isUploadDoc && !isclient_groups && groupsList.isNotEmpty()) {
                val allGroupsModel = DocumentsModel()
                allGroupsModel.group_id = allIds.toString()
                allGroupsModel.group_name = "All Groups"
                allGroupsModel.isGroupChecked = true
                groupsList.add(0, allGroupsModel)

                selected_groups_list.clear()
                selected_groups_list.add(allGroupsModel)
                tv_select_groups_view?.text = allGroupsModel.group_name
                callfilter_client_webservices()
            }

            if (selectedGroupsIds != null && selectedGroupsIds!!.isNotEmpty()) {
                selected_groups_list.clear()
                for (i in groupsList.indices) {
                    val group = groupsList[i]
                    if (selectedGroupsIds!!.contains(group.group_id)) {
                        group.isGroupChecked = true
                        selected_groups_list.add(group)
                    }
                }
            }
            selectedLanguage = BooleanArray(groupsList.size)
            if (isclient_groups) {
                GroupsPopup(
                    ll_upload_groups!!, client_groups_list, selected_client_groups_list,
                    rv_upload_groups!!, tv_select_upload_groups!!
                )
            } else {
                ll_upload_client_group?.visibility = GONE
                if (isUploadDoc) {
                    GroupsPopup(
                        upload_group_layout!!, groupsList, selected_groups_list,
                        rv_display_upload_groups_docs!!, tv_select_groups!!
                    )
                } else {
                    GroupsPopup(
                        view_group_layout!!, groupsList, selected_groups_list,
                        rv_display_view_groups_docs!!, tv_select_groups_view!!
                    )
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun GroupsPopup(
        upload_group_layout: LinearLayout,
        groupsList: ArrayList<DocumentsModel>,
        selected_groups_list: ArrayList<DocumentsModel>,
        rv_display_upload_groups_docs: RecyclerView,
        tv_select_groups: TextView
    ) {
        tv_select_groups.text = ""
        tv_select_groups.setHint(R.string.select_groups)
        ll_upload_groups?.visibility = GONE
        try {
            for (i in groupsList.indices) {
                for (j in selected_groups_list.indices) {
                    if (groupsList[i].group_id == selected_groups_list[j].group_id) {
                        val documentsModel = groupsList[i]
                        documentsModel.isChecked = true
                    }
                }
            }
            val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            rv_display_upload_groups_docs.layoutManager = layoutManager
            val documentsAdapter = GroupsListAdapter(
                groupsList,
                com.digicoffer.lauditor.Documents.Documents(),
                { documentsModel ->
                    if (documentsModel.isGroupChecked) {
                        selected_groups_list.add(documentsModel)
                    } else {
                        for (i in selected_groups_list.indices) {
                            if (selected_groups_list[i].group_id == documentsModel.group_id) {
                                selected_groups_list.removeAt(i)
                                break
                            }
                        }
                    }
                    tv_select_groups.text = ""
                    tv_select_groups.setHint(R.string.select_groups)
                    val value = Array(selected_groups_list.size) { "" }
                    for (i in selected_groups_list.indices) {
                        value[i] = selected_groups_list[i].group_name ?: ""
                    }
                    val str = TextUtils.join(",", value)
                    tv_select_groups.text = str
                }
            )
            rv_display_upload_groups_docs.adapter = documentsAdapter
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun ViewTags(
        documentsModel: DocumentsModel,
        itemsArrayList: ArrayList<DocumentsModel>
    ) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        cl_document.alpha = 0.5f
        val inflater = requireActivity().layoutInflater
        val view_edit_tags = inflater.inflate(R.layout.edit_existing_tags, null)
        val ll_existing_tags = view_edit_tags.findViewById<LinearLayout>(R.id.ll_view_tags)
        val iv_close_existing_tags = view_edit_tags.findViewById<ImageView>(R.id.close_edit_docs)
        val header_name = view_edit_tags.findViewById<TextView>(R.id.header_name)
        val tv_document_name = view_edit_tags.findViewById<TextView>(R.id.tv_document_name)
        tv_document_name.textSize = DynamicUtils.twenty.toFloat()
        tv_document_name.visibility = VISIBLE
        tv_document_name.text = documentsModel.name
        header_name.setText(R.string.view_tags)
        val tagsObj = documentsModel.tags
        if (tagsObj != null) {
            val iter = tagsObj.keys()
            while (iter.hasNext()) {
                val key = iter.next()
                val value = tagsObj.optString(key)
                val view_added_tags = inflater.inflate(R.layout.displays_documents_list, null)
                val tv_tag_name = view_added_tags.findViewById<TextView>(R.id.tv_document_name)
                val iv_remove_tag = view_added_tags.findViewById<ImageView>(R.id.iv_cancel)
                val tag_msg = "$key - $value"
                tv_tag_name.text = tag_msg
                iv_remove_tag.setOnClickListener {
                    ll_existing_tags.removeView(view_added_tags)
                    tagsObj.remove(key)
                }
                ll_existing_tags.addView(view_added_tags)
            }
        }
        val dialog = dialogBuilder.create()
        iv_close_existing_tags.setOnClickListener { dialog.dismiss() }
        dialog.setOnDismissListener { cl_document.alpha = 1.0f }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setView(view_edit_tags)
        dialog.show()
    }

    private fun getTagsFromDocument(model: DocumentsModel): ArrayList<DocumentsModel> {
        val list = ArrayList<DocumentsModel>()
        val tags = model.tags ?: return list
        val keys = tags.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            try {
                val tag = DocumentsModel()
                tag.tag_type = key
                tag.tag_name = tags.getString(key)
                list.add(tag)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return list
    }

    override fun EditDocuments(
        documentsModel: DocumentsModel,
        itemsArrayList: ArrayList<DocumentsModel>, position: Int
    ) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        cl_document.alpha = 0.5f
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.edit_document, null)
        val etDocName = view.findViewById<TextInputEditText>(R.id.edit_doc_name)
        val etDescription = view.findViewById<TextInputEditText>(R.id.edit_description)
        val btnExpDate = view.findViewById<AppCompatButton>(R.id.tv_expiration_date)
        val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(50))
        val filters1 = arrayOf<InputFilter>(InputFilter.LengthFilter(300))
        val tv_document_name = view.findViewById<TextView>(R.id.tv_document_name)
        tv_document_name.setText(R.string.document_name)
        val description = view.findViewById<TextView>(R.id.description)
        description.setText(R.string.description)
        btnExpDate.setHint(R.string.expiration_date)
        val expiration_date_id = view.findViewById<TextView>(R.id.expiration_date_id)
        expiration_date_id.setText(R.string.expiration_date)
        etDocName.setHint(R.string.document_name)
        etDescription.setHint(R.string.description)
        etDocName.setMaxLines(5)
        etDocName.filters = filters
        etDescription.setMaxLines(10)
        etDescription.filters = filters1
        etDocName.addTextChangedListener(Validation(etDocName))
        etDescription.addTextChangedListener(DescriptionValidation(etDescription))

        val switchDownload = view.findViewById<View>(R.id.switch_download).findViewById<SwitchMaterial>(R.id.switch_action)
        val switchEncryption = view.findViewById<View>(R.id.switch_encryption).findViewById<SwitchMaterial>(R.id.switch_action)
        val tv_label = view.findViewById<View>(R.id.switch_encryption).findViewById<TextView>(R.id.tv_label)
        tv_label.setText(R.string.enable_encryption)
        val btnAddTag = view.findViewById<AppCompatButton>(R.id.btn_add_tag)
        llSelectedTags = view.findViewById<LinearLayout>(R.id.ll_selected_tags)
        val btnSave = view.findViewById<AppCompatButton>(R.id.btn_save_tag)
        val btnCancel = view.findViewById<AppCompatButton>(R.id.btn_cancel_edit_docs)
        val ivClose = view.findViewById<ImageView>(R.id.close_edit_docs)

        etDocName.setText(documentsModel.name)
        etDescription.setText(documentsModel.description)
        btnExpDate.text = AndroidUtils.formatToMMMddYYYY(documentsModel.expiration_date)
        switchDownload.isChecked = documentsModel.isIsenabled
        AndroidUtils.checkSwitchState(switchDownload)
        switchEncryption.isChecked = documentsModel.isencrypted == true
        AndroidUtils.checkSwitchState(switchEncryption)

        switchDownload.setOnCheckedChangeListener { compoundButton, b ->
            documentsModel.isIsenabled = b
            AndroidUtils.checkSwitchState(switchDownload)
        }
        switchEncryption.setOnCheckedChangeListener { compoundButton, b ->
            documentsModel.isencrypted = b
            AndroidUtils.checkSwitchState(switchEncryption)
        }

        val tagList = ArrayList<DocumentsModel>()
        val tags = documentsModel.tags
        if (tags != null) {
            val keys = tags.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                try {
                    val tag = DocumentsModel()
                    tag.tag_type = key
                    tag.tag_name = tags.getString(key)
                    tagList.add(tag)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
        if (llSelectedTags != null) {
            AndroidUtils.renderSelectedTags(
                requireContext(),
                llSelectedTags!!,
                tagList,
                documentsModel,
                cl_document,
                requireActivity()
            )
        }
        btnExpDate.setOnClickListener {
            AndroidUtils.showDatePicker(btnExpDate, false, false, true, null)
        }
        btnAddTag.setOnClickListener {
            val model = itemsArrayList[position]
            model.name = etDocName.text.toString().trim()
            model.description = etDescription.text.toString().trim()
            model.expiration_date = AndroidUtils.convertAnyDateToDDMMYYYY(btnExpDate.text.toString())
            model.isIsenabled = switchDownload.isChecked
            model.isencrypted = switchEncryption.isChecked
            selected_documents_list.clear()
            selected_documents_list.add(model)
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
                model.expiration_date = AndroidUtils.convertAnyDateToDDMMYYYY(btnExpDate.text.toString())
                model.isIsenabled = switchDownload.isChecked
                model.isencrypted = switchEncryption.isChecked
                itemsArrayList[position] = model
                dialog.dismiss()
                loadRecyclerview("edit_meta", "")
            }
        }
        val closeListener = View.OnClickListener {
            val editedModel = DocumentsModel()
            editedModel.name = etDocName.text.toString().trim()
            editedModel.description = etDescription.text.toString().trim()
            editedModel.expiration_date = AndroidUtils.convertAnyDateToDDMMYYYY(btnExpDate.text.toString())
            editedModel.isIsenabled = switchDownload.isChecked
            editedModel.isencrypted = switchEncryption.isChecked
            editedModel.tags = itemsArrayList[position].tags
            val changed = AndroidUtils.hasDocumentChanged(documentsModel, editedModel)
            if (changed) {
                AndroidUtils.showConfirmationDialog(
                    requireContext(), "Alert!",
                    "Changes you made will not be saved. Do you want to continue?",
                    object : AndroidUtils.OnConfirmListener {
                        override fun onSave() {}
                        override fun onCancel() {
                            dialog.dismiss()
                        }
                    })
            } else {
                dialog.dismiss()
            }
        }
        btnCancel.setOnClickListener(closeListener)
        ivClose.setOnClickListener(closeListener)
        dialog.setOnDismissListener { cl_document.alpha = 1f }
        dialog.show()
    }

    private fun handleEditDocClose(
        name: String, description: String, exp_date: String,
        tv_doc_name: TextView, tv_description: TextView,
        tv_exp_date: TextView, dialog: Dialog
    ) {
        val docName = tv_doc_name.text.toString()
        val desc = tv_description.text.toString()
        val exp = AndroidUtils.convertAnyDateToDDMMYYYY(tv_exp_date.text.toString())
        if ((docName.isNotEmpty() && docName != name)
            || (desc.isNotEmpty() && desc != description)
            || (exp.isNotEmpty() && exp != exp_date)) {
            AndroidUtils.showConfirmationDialog(
                requireContext(), "Alert!",
                "Changes you made will not be saved. Do you want to continue?",
                object : AndroidUtils.OnConfirmListener {
                    override fun onSave() {}
                    override fun onCancel() {
                        dialog.dismiss()
                    }
                })
        } else {
            dialog.dismiss()
        }
    }

    override fun RemoveDocument(position: Int, tag: String) {
        if (position < 0 || position >= docsList.size) return
        docsList.removeAt(position)
        adapter?.notifyItemRemoved(position)
        loadRecyclerview(tag, "")
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

    override fun edit_document(viewDocumentsModel: ViewDocumentsModel?) {
        if (viewDocumentsModel == null) return
        val dialogBuilder = AlertDialog.Builder(requireContext())
        cl_document.alpha = 0.5f
        val inflater = requireActivity().layoutInflater
        val view_edit_documents = inflater.inflate(R.layout.edit_meta_data, null)
        val iv_cancel_edit_doc = view_edit_documents.findViewById<ImageView>(R.id.close_edit_docs)
        val btn_close_edit_docs = view_edit_documents.findViewById<AppCompatButton>(R.id.btn_cancel_edit_docs)
        val tv_doc_name = view_edit_documents.findViewById<TextInputEditText>(R.id.edit_doc_name)
        val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(50))
        val filters1 = arrayOf<InputFilter>(InputFilter.LengthFilter(300))
        tv_doc_name.setMaxLines(5)
        tv_doc_name.filters = filters
        val tv_document_name = view_edit_documents.findViewById<TextView>(R.id.tv_document_name)
        tv_document_name.setText(R.string.document_name)
        val tv_description = view_edit_documents.findViewById<TextInputEditText>(R.id.edit_description)
        tv_doc_name.setHint(R.string.document_name)
        tv_description.setMaxLines(10)
        tv_description.filters = filters1
        tv_doc_name.addTextChangedListener(Validation(tv_doc_name))
        tv_description.addTextChangedListener(DescriptionValidation(tv_description))
        val description = view_edit_documents.findViewById<TextView>(R.id.description)
        description.setText(R.string.description)
        val tv_exp_date = view_edit_documents.findViewById<AppCompatButton>(R.id.tv_expiration_date)
        tv_exp_date.setHint(R.string.expiration_date)
        val expiration_date_id = view_edit_documents.findViewById<TextView>(R.id.expiration_date_id)
        expiration_date_id.setText(R.string.expiration_date)
        tv_exp_date.setOnClickListener {
            AndroidUtils.showDatePicker(tv_exp_date, false, false, true, null)
        }
        if (viewDocumentsModel.expiration_date == "NA" || viewDocumentsModel.expiration_date == "") {
            tv_exp_date.text = ""
        } else {
            tv_exp_date.text = AndroidUtils.formatToMMMddYYYY(viewDocumentsModel.expiration_date)
        }
        tv_doc_name.setText(viewDocumentsModel.name)
        tv_description.setText(viewDocumentsModel.description)
        val btn_save_tag = view_edit_documents.findViewById<AppCompatButton>(R.id.btn_save_tag)
        val dialog = dialogBuilder.create()
        iv_cancel_edit_doc.setOnClickListener {
            handleEditDocClose(
                viewDocumentsModel.name ?: "",
                viewDocumentsModel.description ?: "",
                viewDocumentsModel.expiration_date ?: "",
                tv_doc_name, tv_description, tv_exp_date, dialog
            )
        }
        btn_close_edit_docs.setOnClickListener {
            handleEditDocClose(
                viewDocumentsModel.name ?: "",
                viewDocumentsModel.description ?: "",
                viewDocumentsModel.expiration_date ?: "",
                tv_doc_name, tv_description, tv_exp_date, dialog
            )
        }
        btn_save_tag.setOnClickListener {
            if (tv_doc_name.text.toString().isEmpty() && tv_description.text.toString().isEmpty()) {
                AndroidUtils.showAlert("Please enter the Document name, Description", activity)
            } else if (tv_description.text.toString().isEmpty()) {
                AndroidUtils.showAlert("Please enter the Description", activity)
            } else if (tv_document_name.text.toString().isEmpty()) {
                AndroidUtils.showAlert("Please enter the Document name", activity)
            } else {
                callUpdateDocumentWebservice(
                    tv_doc_name.text.toString(),
                    tv_description.text.toString(),
                    AndroidUtils.convertAnyDateToDDMMYYYY(tv_exp_date.text.toString()),
                    viewDocumentsModel.id ?: ""
                )
            }
        }
        dialog.setOnDismissListener { cl_document.alpha = 1.0f }
        dialog.setCancelable(false)
        dialog.setView(view_edit_documents)
        Dialog = dialog
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun callUpdateDocumentWebservice(
        name: String, description: String,
        expiration_date: String, id: String
    ) {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            jsonObject.put("name", name)
            jsonObject.put("description", description)
            jsonObject.put("expiration_date", expiration_date)
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.PUT,
                "v3/document/$id", "Update Documents", jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    override fun Display_Document(viewDocumentsModel: ViewDocumentsModel?) {
        if (viewDocumentsModel == null) return
        CONTENT_TYPE = viewDocumentsModel.content_type ?: ""
        Log.d("IMage_name_content", CONTENT_TYPE)
        tempDocModel = viewDocumentsModel
        if (DOCUMENT_TYPE_TAG == "Deleted") {
            Deleted_Document(viewDocumentsModel, "view")
        } else {
            callDisplayDocumentWebservice(viewDocumentsModel.id ?: "")
        }
        doc_name = ""
        doc_name = viewDocumentsModel.name
    }

    fun Deleted_Document(viewDocumentsModel: ViewDocumentsModel, delete: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            jsonObject.put("docid", viewDocumentsModel.id)
            jsonObject.put("doctype", viewDocumentsModel.doc_type)
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.POST,
                "docs/deleted/$delete", "Deleted Documents", jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    fun ViewEncryptedDoc(doc_id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            jsonObject.put("docid", doc_id)
            WebServiceHelper.callEmailHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.POST,
                "v3/decrypt", "View Encrypted Doc", jsonObject.toString()
            )
            Log.d("Token111", Constants.TOKEN ?: "")
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    interface OnConfirmClickListener {
        fun onYesClick()
        fun onNoClick()
    }

    fun showConfirmDialog(
        context: Context, message: String, title: String,
        listener: com.digicoffer.lauditor.Documents.Documents.OnConfirmClickListener
    ) {
        try {
            val dialogBuilder = AlertDialog.Builder(context)
            val view = LayoutInflater.from(context).inflate(R.layout.delete_relationship, null)
            val header_name = view.findViewById<TextView>(R.id.header_name)
            val close_documents = view.findViewById<ImageView>(R.id.close_documents)
            val tv_confirmation = view.findViewById<TextView>(R.id.tv_confirmation)
            val btn_yes = view.findViewById<AppCompatButton>(R.id.btn_yes)
            val btn_no = view.findViewById<AppCompatButton>(R.id.btn_No)
            header_name.setTextColor(Color.BLACK)
            header_name.text = title
            tv_confirmation.text = message
            if (message == "Are you sure to permanently delete document?"
                || message == "Are you sure you want to delete Document?") {
                btn_no.setTextColor(context.getColor(R.color.white))
                btn_yes.setTextColor(context.getColor(R.color.black))
                btn_yes.background = context.getDrawable(R.drawable.yes_button_red_button)
                btn_no.background = context.getDrawable(R.drawable.no_button_green_button)
            } else {
                btn_yes.setTextColor(context.getColor(R.color.white))
                btn_no.setTextColor(context.getColor(R.color.black))
                btn_no.background = context.getDrawable(R.drawable.yes_button_red_button)
                btn_yes.background = context.getDrawable(R.drawable.no_button_green_button)
            }
            val dialog = dialogBuilder.create()
            dialog.setView(view)
            close_documents.setOnClickListener {
                dialog.dismiss()
                listener.onNoClick()
            }
            btn_no.setOnClickListener {
                dialog.dismiss()
                listener.onNoClick()
            }
            btn_yes.setOnClickListener {
                dialog.dismiss()
                listener.onYesClick()
            }
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun ViewDialog(viewDocumentsModel: ViewDocumentsModel?, ViewType: String?) {
        if (viewDocumentsModel == null || ViewType == null) return
        val deleteMsg: String
        val title: String

        when (ViewType) {
            "disabled" -> {
                title = "Confirmation"
                deleteMsg = "Are you sure you want to enable download for this document?"
            }
            "enabled" -> {
                title = "Confirmation"
                deleteMsg = "Are you sure you want to disable download for this document?"
            }
            "encrypt" -> {
                title = "Alert!"
                deleteMsg = "Are you sure you want to encrypt Document?"
            }
            "decrypt" -> {
                title = "Alert!"
                deleteMsg = "Are you sure you want to decrypt Document?"
            }
            "deleted" -> {
                title = "Confirmation"
                deleteMsg = "Are you sure to permanently delete document?"
            }
            "download" -> {
                title = "Confirmation"
                deleteMsg = ("Downloading this document will remove it from the secure system.\n"
                        + "Do you wish to proceed with the download?")
            }
            "restore" -> {
                title = "Alert!"
                deleteMsg = "Are you sure to restore document?"
            }
            else -> {
                title = "Confirmation"
                deleteMsg = "Are you sure you want to delete Document?"
            }
        }

        showConfirmDialog(requireActivity(), deleteMsg, title,
            object : com.digicoffer.lauditor.Documents.Documents.OnConfirmClickListener {
                override fun onYesClick() {
                    when (ViewType) {
                        "disabled" -> {
                            viewDocumentsModel.isdisabled = false
                            viewDocumentsModel.is_disabled = false
                            updateDocumentInPageItems(viewDocumentsModel)
                            disabled_doc(viewDocumentsModel.id ?: "")
                        }
                        "enabled" -> {
                            viewDocumentsModel.isdisabled = true
                            viewDocumentsModel.is_disabled = true
                            updateDocumentInPageItems(viewDocumentsModel)
                            enabled_doc(viewDocumentsModel.id ?: "")
                        }
                        "encrypt" -> {
                            viewDocumentsModel.added_encryption = true
                            viewDocumentsModel.is_encrypted = true
                            updateDocumentInPageItems(viewDocumentsModel)
                            encryption_doc(viewDocumentsModel.id ?: "")
                        }
                        "decrypt" -> {
                            viewDocumentsModel.added_encryption = false
                            viewDocumentsModel.is_encrypted = false
                            updateDocumentInPageItems(viewDocumentsModel)
                            decryption_doc(viewDocumentsModel.id ?: "")
                        }
                        "download" -> {
                            Download_Document(viewDocumentsModel.id ?: "")
                        }
                        "deleted" -> {
                            Deleted_Document(viewDocumentsModel, "delete")
                        }
                        "restore" -> {
                            Deleted_Document(viewDocumentsModel, ViewType)
                        }
                        else -> {
                            callDeleteDocumentWebservice(viewDocumentsModel.id ?: "")
                        }
                    }
                }

                override fun onNoClick() {}
            })
    }

    private fun updateDocumentInPageItems(updatedModel: ViewDocumentsModel) {
        for (i in view_docs_list.indices) {
            if (view_docs_list[i].id == updatedModel.id) {
                view_docs_list[i] = updatedModel
                break
            }
        }
        for (i in pageItems.indices) {
            if (pageItems[i].id == updatedModel.id) {
                pageItems[i] = updatedModel
                break
            }
        }
        if (adapter1 != null) {
            adapter1?.setData(ArrayList(pageItems))
            adapter1?.notifyDataSetChanged()
        }
    }

    override fun Download_Document(viewDocumentsModel: ViewDocumentsModel?) {
        tempDocModel = viewDocumentsModel
    }

    fun Download_Document(docid: String) {
        try {
            val jsonObject = JSONObject()
            if (!is_MergePdfClicked) {
                progress_dialog = AndroidUtils.get_progress(requireActivity())
                WebServiceHelper.callHttpWebService(
                    this, requireContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/mergepdf/$docid/download",
                    "Download Document", jsonObject.toString()
                )
            } else if (tempDocModel?.added_encryption == true || tempDocModel?.is_encrypted == true) {
                callDecryptApi(docid, true)
            } else {
                progress_dialog = AndroidUtils.get_progress(requireActivity())
                WebServiceHelper.callHttpWebService(
                    this, requireContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/document/" + tempDocModel?.id + "/download",
                    "Download Document", jsonObject.toString()
                )
            }
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    override fun Update_Tag(viewDocumentsModel: ViewDocumentsModel?) {
        if (viewDocumentsModel == null) return
        this.viewDocumentsModel = viewDocumentsModel
        isUpdateTag = true
        tags_list.clear()
        val tagArray = viewDocumentsModel.tagslist
        if (tagArray != null) {
            val duplicateTypes = ArrayList<String>()
            for (i in 0 until tagArray.length()) {
                try {
                    val tagObject = tagArray.getJSONObject(i)
                    val tagType = tagObject.optString("key")
                    val tagName = tagObject.optString("value")
                    var isDuplicateType = false
                    for (existing in tags_list) {
                        if (existing.tag_type.equals(tagType.trim(), ignoreCase = true)) {
                            isDuplicateType = true
                            break
                        }
                    }
                    if (isDuplicateType) {
                        duplicateTypes.add(tagType)
                        continue
                    }
                    val tagModel = DocumentsModel()
                    tagModel.tag_type = tagType
                    tagModel.tag_name = tagName
                    tags_list.add(tagModel)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            if (duplicateTypes.isNotEmpty()) {
                val duplicateList = TextUtils.join(", ", duplicateTypes)
                AndroidUtils.showAlert(
                    "This tag type already exists. Please use a different tag type: $duplicateList",
                    activity
                )
            }
            open_add_tags_popup()
        }
    }

    private var viewDocumentsModel: ViewDocumentsModel? = null

    fun disabled_doc(id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            jsonObject.put("downloadDisabled", true)
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.PATCH,
                "v3/document/$id", "Disabled Documents", jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    fun enabled_doc(id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            jsonObject.put("downloadDisabled", false)
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.PATCH,
                "v3/document/$id", "Enabled Documents", jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    fun encryption_doc(id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.POST,
                "v3/document/encrypt/$id", "Encrypt Documents", jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    fun decryption_doc(id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            jsonObject.put("get_file", false)
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.POST,
                "v3/document/decrypt/$id", "Decrypt Documents", jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    fun callDecryptApi(id: String, isDownload: Boolean) {
        this.isDownload = isDownload
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            jsonObject.put("docid", id)
            jsonObject.put("download", isDownload)
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.POST,
                Constants.decryptUrl ?: "", "Decrypt Doc", jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    fun callOtherDocViewApi(id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                Constants.base_URL + "v3/document/" + id + "/view",
                "Other Doc View", jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun callDisplayDocumentWebservice(id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            if (!is_MergePdfClicked) {
                WebServiceHelper.callHttpWebService(
                    this, requireContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/mergepdf/$id/view",
                    "Display Documents", jsonObject.toString()
                )
            } else {
                WebServiceHelper.callHttpWebService(
                    this, requireContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/document/$id/view",
                    "Display Documents", jsonObject.toString()
                )
            }
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    fun callfilter_client_webservices() {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            jsonObject.put("showPdfDocs", false)

            if (CATEGORY_TAG == "client") {
                jsonObject.put("category", "client")

                if (client_id.contains(",")) {
                    val clientArray = JSONArray()
                    val ids = client_id.split(",")
                    for (id in ids) {
                        if (id.trim().isNotEmpty()) {
                            clientArray.put(id.trim())
                        }
                    }
                    jsonObject.put("clients", clientArray)
                } else {
                    jsonObject.put("clients", client_id)
                }

                jsonObject.put("groups", null)

                if (matter_id.isNotEmpty()) {
                    if (matter_id.contains(",")) {
                        val matterArray = JSONArray()
                        val ids = matter_id.split(",")
                        for (id in ids) {
                            if (id.trim().isNotEmpty()) {
                                matterArray.put(id.trim())
                            }
                        }
                        jsonObject.put("matters", matterArray)
                    } else {
                        jsonObject.put("matters", matter_id)
                    }
                }

            } else if (CATEGORY_TAG == "firm") {
                val groups = JSONArray()

                for (k in 0 until selected_groups_list.size) {
                    val documentsModel1 = selected_groups_list[k]
                    val groupId = documentsModel1.group_id
                    if (groupId != null) {
                        if (groupId.contains(",")) {
                            val ids = groupId.split(",")
                            for (id in ids) {
                                if (id.trim().isNotEmpty()) {
                                    groups.put(id.trim())
                                }
                            }
                        } else {
                            groups.put(groupId)
                        }
                    }
                }

                jsonObject.put("category", "firm")
                jsonObject.put("clients", "")
                jsonObject.put("matters", "")
                jsonObject.put("groups", groups)
                Log.d("Group_value_num", groups.toString())
                Log.d("Group_doc_view1", jsonObject.toString())
            }

            if (is_MergePdfClicked) {
                WebServiceHelper.callHttpWebService(
                    this, requireContext(),
                    WebServiceHelper.RestMethodType.PUT,
                    "v3/document/filter", "Display FilterDocuments", jsonObject.toString()
                )
            } else {
                WebServiceHelper.callHttpWebService(
                    this, requireContext(),
                    WebServiceHelper.RestMethodType.PUT,
                    "v3/mergepdf/filter", "Display MergeFilterDocuments",
                    jsonObject.toString()
                )
            }

            Log.d("Group_doc_view1", jsonObject.toString())
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun callDeleteDocumentWebservice(id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            var jsonObject = JSONObject()
            val jsonArray = JSONArray()
            if (is_MergePdfClicked) {
                jsonArray.put(id)
                jsonObject.put("docids", jsonArray)
                WebServiceHelper.callHttpWebService(
                    this, requireContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "v3/document/delete", "Delete Documents", jsonObject.toString()
                )
            } else {
                jsonObject = JSONObject()
                WebServiceHelper.callHttpWebService(
                    this, requireContext(),
                    WebServiceHelper.RestMethodType.DELETE,
                    "v3/mergepdf/$id", "Delete Merge Documents", jsonObject.toString()
                )
            }
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
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

    override fun onCheckedChanged(documentsModel: DocumentsModel) {
        // Implementation of GroupsListAdapter.OnCheckedChangeListener
    }

    private fun handleNotificationNavigation() {
        val bundle = Constants.notificationBundle ?: return
        val route = bundle.getString(Constants.NavKeys.ROUTE_NAME)
        client_id = ""
        selectedId = ""
        matter_id = ""

        if (route == null) {
            loadMatterDocuments()
            return
        }

        pendingHighlightIds = bundle.getStringArrayList(Constants.NavKeys.HIGHLIGHT_IDS)
        selectedGroupsIds = bundle.getStringArrayList(Constants.NavKeys.GROUPS_ID)
        client_id = bundle.getString(Constants.NavKeys.CLIENT_ID) ?: ""
        selectedId = bundle.getString(Constants.NavKeys.CLIENT_ID) ?: ""
        matter_id = bundle.getString(Constants.NavKeys.MATTER_ID) ?: ""
        if (matter_id.isNotEmpty()) {
            selectedId = bundle.getString(Constants.NavKeys.MATTER_ID) ?: ""
        }

        when (route) {
            "document_deleted_list" -> {
                currentModule = "delete"
                loadDeletedDocuments()
            }
            "document_firm_list" -> {
                currentModule = "firm"
                loadFirmDocuments()
                callfilter_client_webservices()
            }
            "document_matter_list" -> {
                currentModule = "matter"
                loadMatterDocuments()
                callfilter_client_webservices()
            }
            "document_client_list" -> {
                currentModule = "client"
                loadClientDocuments()
                callfilter_client_webservices()
            }
            else -> loadMatterDocuments()
        }
        Constants.isFromNotification = false
        Constants.notificationBundle.clear()
    }
}
