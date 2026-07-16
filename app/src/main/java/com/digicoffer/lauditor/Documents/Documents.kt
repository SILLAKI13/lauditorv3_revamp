package com.digicoffer.lauditor.Documents

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
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
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
import com.bumptech.glide.Glide
import com.digicoffer.lauditor.AuditTrails.Adapters.PaginationHelper
import com.digicoffer.lauditor.Documents.DocumentsListAdpater.DocumentsListAdapter
import com.digicoffer.lauditor.Documents.DocumentsListAdpater.GroupsListAdapter
import com.digicoffer.lauditor.Documents.DocumentsListAdpater.View_documents_adapter
import com.digicoffer.lauditor.Documents.Models.ClientsModel
import com.digicoffer.lauditor.Documents.Models.DocumentsModel
import com.digicoffer.lauditor.Documents.Models.MattersModel
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel
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
import java.util.Calendar
import java.util.Collections
import java.util.Comparator
import java.util.Date
import java.util.Locale
import java.util.Objects
import java.util.regex.Pattern

class Documents : Fragment(), BottomSheetUploadFile.OnPhotoSelectedListner,
    AsyncTaskCompleteListener, DocumentsListAdapter.EventListener,
    View_documents_adapter.Eventlistner, GroupsListAdapter.OnCheckedChangeListener {

    private var btn_browse: Button? = null
    private var btn_group_cancel: Button? = null
    private var btn_group_submit: Button? = null
    private var btn_group_view_cancel: Button? = null
    private var btn_group_view_submit: Button? = null
    private var isListFiltered = false
    private var isDownload = false
    private var totalUploads = 0
    private var completedUploads = 0
    private var isAlertShown = false
    private var end_temp = 0
    private val filteredList = ArrayList<ViewDocumentsModel>()
    private val pagebuttons = ArrayList<TextView>()
    private var is_MergePdfClicked = true
    private var filedata: ByteArray? = null
    private var doc_name: String? = null
    private var currentPage = 1
    private var ReceivedFileName = ""
    private var client_name: String? = null
    private var Dialog: AlertDialog? = null
    private var isedit = false
    private var isUpdateTag = false
    private var edit_position = 0
    private var viewDocumentsModel_download: ViewDocumentsModel? = null
    private var viewDocumentsModel: ViewDocumentsModel? = null
    private var pageItems = ArrayList<ViewDocumentsModel>()
    private var isUploadDoc = false
    private var custom_spinner: TextView? = null
    private var custom_spinner2: TextView? = null
    private var custom_spinner3: TextView? = null
    private var custom_spinner4: TextView? = null
    private var custom_spinner_group: TextView? = null
    private var tv_select_groups_view: TextView? = null
    private var tv_client_doc: TextView? = null
    private var tv_firm_doc: TextView? = null
    private var cv_view_doc: CardView? = null
    private var chk_box_layout: LinearLayout? = null
    private var isselect_all_checked = true
    private var array_group = JSONArray()
    private var is_clicked_add = true
    private var ischecked_group = true
    private var ischeckedClient_group = true
    private var ischecked_group_view = true
    private var is_clicked_edit = true
    private var ischecked = true
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
    private var count_file = 0
    private var mViewModel: NewModel? = null
    private var spinnerLayout: RelativeLayout? = null
    private var ismatter_chosen = false

    private var bottommSheetUploadDocument: BottomSheetUploadFile? = null
    private var mSelectedBitmap: Bitmap? = null
    private var cl_document: ConstraintLayout? = null
    private var ll_added_tags: LinearLayout? = null
    private var ll_matter: LinearLayout? = null
    private var ll_category: LinearLayout? = null
    private var ll_groups: LinearLayout? = null
    private var ll_client_name: LinearLayout? = null
    private var ll_view_docs: LinearLayout? = null
    private var ll_upload_docs: LinearLayout? = null
    private var upload_group_layout: LinearLayout? = null
    private var view_group_layout: LinearLayout? = null
    private var ll_matter_view: LinearLayout? = null
    private var ll_client_name_view: LinearLayout? = null
    private var ll_categories_layout: LinearLayout? = null
    private var ll_document_type_view: LinearLayout? = null
    private var ll_upload_groups: LinearLayout? = null
    private var ll_upload_client_group: LinearLayout? = null
    private var tv_tag_type: TextInputEditText? = null
    private var tv_tag_name: TextInputEditText? = null
    private var tv_search_client_view: TextInputEditText? = null
    private var imageView: ImageView? = null
    private var selectedLanguage: BooleanArray? = null
    private var DOCUMENT_TYPE_TAG = "client"
    private var CONTENT_TYPE = ""
    private var UPLOAD_TAG = "Client"
    private var VIEW_TAG = "Client"
    private var adapter: DocumentsListAdapter? = null
    private var adapter1: View_documents_adapter? = null
    private var cv_view_documents: CardView? = null
    private val view_docs_list = ArrayList<ViewDocumentsModel>()
    private var siv_upload_document: ShapeableImageView? = null
    private var siv_view_document: ShapeableImageView? = null
    private val langList = ArrayList<Int>()
    private val groupsList = ArrayList<DocumentsModel>()
    private val tags_list = ArrayList<DocumentsModel>()
    private var mSelectedUri: File? = null
    private var subtag = ""
    private val selected_documents_list = ArrayList<DocumentsModel>()
    private var ll_page_navigaiton: LinearLayout? = null
    private var DOWNLOAD_TAG = false
    private var ENCRYPTION_TAG = true
    private var DECRYPTION_TAG = true
    private var CATEGORY_TAG = ""
    private var chk_select_all: CheckBox? = null
    private var filename: String? = null
    private var currentpoistion = 0
    private val clientsList = ArrayList<ClientsModel>()
    private val CorpClientsList = ArrayList<ClientsModel>()
    private val matterlist = ArrayList<MattersModel>()
    private val updatedClients = ArrayList<ClientsModel>()
    private val selected_groups_list = ArrayList<DocumentsModel>()
    private val client_groups_list = ArrayList<DocumentsModel>()
    private val selected_client_groups_list = ArrayList<DocumentsModel>()
    private val docsList = ArrayList<DocumentsModel>()
    private var rv_documents: RecyclerView? = null
    private var rv_display_view_docs: RecyclerView? = null
    private var rv_display_upload_groups_docs: RecyclerView? = null
    private var rv_display_view_groups_docs: RecyclerView? = null
    private var rv_upload_groups: RecyclerView? = null
    private var progress_dialog: Dialog? = null

    private var tv_matter: TextView? = null
    private var select_documents: TextView? = null
    private var tv_select_group_name: TextView? = null
    private var upload_name: TextView? = null
    private var view_name: TextView? = null
    private var tag_type_name: TextView? = null
    private var tag_name: TextView? = null
    private var header_name: TextView? = null
    private var header_name_group: TextView? = null
    private var category_name: TextView? = null
    private var tv_category: TextView? = null
    private var tv_selected_file: TextView? = null
    private var tv_select_upload_group_name: TextView? = null
    private var tv_select_upload_groups: TextView? = null

    // edit_tag..
    private var header_name_edit: TextView? = null
    private var tag_type_edit: TextView? = null
    private var tag_edit: TextView? = null
    private var tv_edit_tag_type: TextInputEditText? = null
    private var tv_edit_tag_name: TextInputEditText? = null
    private var btn_upload: Button? = null
    private var btn_add_tags: Button? = null
    private var btn_cancel: Button? = null
    private var tv_add_tag: TextView? = null
    private var tv_client: TextView? = null
    private var tv_firm: TextView? = null
    private var tv_enable_download: TextView? = null
    private var tv_disable_download: TextView? = null
    private var tv_enable_encryption: TextView? = null
    private var tv_disable_encryption: TextView? = null
    private var tv_edit_meta: TextView? = null
    private var tv_name: TextView? = null
    private var tv_client_view: TextView? = null
    private var tv_firm_view: TextView? = null
    private var tv_deleted_view: TextView? = null
    private var tv_name_view: TextView? = null
    private var matter_name: TextView? = null
    private var category_name_id: TextView? = null
    private var select_doc_type: TextView? = null
    private var tv_document_name: TextView? = null
    private var description: TextView? = null
    private var file: File? = null
    private var value = ""
    private var entity_id = ""
    private var matter_id = ""
    private var client_id = ""
    private var img_clear_icon1: ImageView? = null
    private var img_dropdown_icon1: ImageView? = null
    private var img_clear_icon2: ImageView? = null
    private var img_dropdown_icon2: ImageView? = null
    private var img_clear_icon3: ImageView? = null
    private var img_dropdown_icon3: ImageView? = null
    private var img_clear_icon4: ImageView? = null
    private var img_dropdown_icon4: ImageView? = null
    private var tv_tag_document_name: TextView? = null
    private var tv_select_groups: TextView? = null
    private var merge_pdf: TextView? = null
    private var view1: View? = null
    private var view2: View? = null
    private var view3: View? = null
    private var view4: View? = null
    private var ll_hide_document_details: LinearLayout? = null
    private var pageNumberLayout: LinearLayout? = null
    private var ll_merge_pdf: LinearLayout? = null
    private var sp_matter: Spinner? = null
    private var sp_client: Spinner? = null
    private var tv_search_client: Spinner? = null
    private var sp_matter_view: Spinner? = null
    private var tv_search_client_views: TextInputEditText? = null
    private var tl_selected_file: TextInputLayout? = null
    private var ischecked_matter = true
    private var ischecked_matter2 = true
    private var ischecked2 = true
    private var filters: Array<InputFilter>? = null
    private var filters1: Array<InputFilter>? = null

    // Define ActivityResultLauncher
    private val requestPermissions: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            var permissionGranted = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                permissionGranted = results[READ_MEDIA_IMAGES] == true ||
                        results[READ_MEDIA_VIDEO] == true ||
                        results[READ_MEDIA_VISUAL_USER_SELECTED] == true
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionGranted = results[READ_MEDIA_IMAGES] == true ||
                        results[READ_MEDIA_VIDEO] == true
            } else {
                permissionGranted = results[READ_EXTERNAL_STORAGE] == true
            }

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
            tv_search_client_view?.setHint(R.string.search)

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

            // Spinner layout1
            view1 = v.findViewById(R.id.customLayout1)
            custom_spinner = view1?.findViewById(R.id.tv_spinner_view)
            custom_spinner?.setHint(R.string.select_client_name)
            img_clear_icon1 = view1?.findViewById(R.id.img_clear_icon)
            img_dropdown_icon1 = view1?.findViewById(R.id.img_dropdown_icon)

            // Spinner layout2
            view2 = v.findViewById(R.id.customLayout2)
            custom_spinner2 = view2?.findViewById(R.id.tv_spinner_view)
            custom_spinner2?.setHint(R.string.select_matters)
            img_clear_icon2 = view2?.findViewById(R.id.img_clear_icon)
            img_dropdown_icon2 = view2?.findViewById(R.id.img_dropdown_icon)

            // Spinner layout3
            view3 = v.findViewById(R.id.customLayout3)
            custom_spinner3 = view3?.findViewById(R.id.tv_spinner_view)
            custom_spinner3?.setHint(R.string.select_client_name)
            img_clear_icon3 = view3?.findViewById(R.id.img_clear_icon)
            img_dropdown_icon3 = view3?.findViewById(R.id.img_dropdown_icon)

            // Spinner layout4
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
            tv_search_client_views?.setHint(R.string.search)

            tv_search_client_view?.addTextChangedListener(Validation(tv_search_client_view!!))
            tv_search_client_views?.addTextChangedListener(Validation(tv_search_client_views!!))

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
            btn_browse?.setText(R.string.browse_small)

            upload_group_layout = v.findViewById(R.id.upload_group_layout)
            rv_display_upload_groups_docs = v.findViewById(R.id.rv_display_upload_groups_docs)
            rv_display_upload_groups_docs?.background = requireContext().getDrawable(R.drawable.rectangle_light_grey_bg)
            btn_group_cancel = v.findViewById(R.id.btn_group_cancel)
            btn_group_cancel?.visibility = GONE
            btn_group_submit = v.findViewById(R.id.btn_group_submit)
            btn_group_submit?.visibility = GONE

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
            upload_name?.setTextColor(requireContext().getResources().getColor(R.color.white))
            upload_name?.setText(R.string.upload)
            view_name = v.findViewById(R.id.view_name)
            view_name?.setTextColor(requireContext().getResources().getColor(R.color.white))
            view_name?.setText(R.string.view)
            select_documents = v.findViewById(R.id.select_documents)
            select_documents?.setText(R.string.select_document)

            chk_box_layout = v.findViewById(R.id.chk_box_layout)
            chk_box_layout?.alpha = 0.5f
            chk_select_all = v.findViewById(R.id.chk_select_all)
            chk_select_all?.background?.alpha = 50
            chk_select_all?.isEnabled = false
            rv_documents = v.findViewById(R.id.rv_documents)

            // Enable the upload documents as the default view...
            siv_upload_document?.background = requireContext().getResources().getDrawable(R.color.green_count_color)
            siv_upload_document?.setImageDrawable(requireContext().getResources().getDrawable(R.mipmap.green_background_icon))

            ll_matter_view = v.findViewById(R.id.ll_matter_view)
            ll_document_type_view = v.findViewById(R.id.ll_document_type_view)
            ll_client_name_view = v.findViewById(R.id.ll_client_name_view)
            ll_categories_layout = v.findViewById(R.id.ll_categories_layout)
            ll_client_name = v.findViewById(R.id.ll_client_name)
            category_name = v.findViewById(R.id.tv_category_name)
            category_name?.setHint(R.string.sub_categories)

            ll_hide_document_details = v.findViewById(R.id.ll_hide_doc_details)
            ll_hide_document_details?.visibility = GONE

            // Make View Documents as Default View.....
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
                if (ischecked) {
                    list_client?.visibility = VISIBLE
                } else {
                    list_client?.visibility = GONE
                }
                ischecked = !ischecked
            }
            img_clear_icon1?.setOnClickListener {
                if (!custom_spinner?.text.toString().isEmpty()) {
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
                if (!custom_spinner2?.text.toString().isEmpty()) {
                    matter_id = ""
                    ischecked_matter = true
                    callClientGroupsWebservice()
                    hideMatterUpload()
                }
            }
            img_clear_icon3?.setOnClickListener {
                if (!custom_spinner3?.text.toString().isEmpty()) {
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
                if (!custom_spinner3?.text.toString().isEmpty()) {
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
                if (ischecked_matter) {
                    list_matter?.visibility = VISIBLE
                } else {
                    list_matter?.visibility = GONE
                }
                ischecked_matter = !ischecked_matter
            }
            view3?.setOnClickListener {
                if (clientsList.isEmpty()) {
                    callClientWebservice()
                }
                if (ischecked2) {
                    list_client_view?.visibility = VISIBLE
                } else {
                    list_client_view?.visibility = GONE
                }
                ischecked2 = !ischecked2
            }
            view4?.setOnClickListener {
                if (ischecked_matter2) {
                    list_matter_view?.visibility = VISIBLE
                } else {
                    list_matter_view?.visibility = GONE
                }
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
                    if (!selected_groups_list.isEmpty()) {
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
                tv_firm?.setTextColor(requireContext().getResources().getColor(R.color.black))
                tv_client?.setTextColor(requireContext().getResources().getColor(R.color.white))
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

            btn_browse?.setOnClickListener {
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
                tv_client_doc?.setText(R.string.list_of_documents_pending_approval)
                ll_merge_pdf?.visibility = GONE
                ll_client_name_view?.visibility = GONE
                tv_client_view?.background = requireContext().getResources().getDrawable(R.drawable.button_left_background)
                tv_client_view?.setTextColor(Color.BLACK)
                if (Constants.ROLE == "SU") {
                    tv_firm_view?.background = requireContext().getResources().getDrawable(R.drawable.radiobutton_centre_background)
                } else {
                    tv_firm_view?.background = requireContext().getResources().getDrawable(R.drawable.button_right_background)
                }
                tv_firm_view?.setTextColor(Color.BLACK)
                tv_deleted_view?.background = requireContext().getResources().getDrawable(R.drawable.button_right_green_background)
                tv_deleted_view?.setTextColor(Color.WHITE)
                DOCUMENT_TYPE_TAG = "Deleted"
                CATEGORY_TAG = "Deleted"
                callDeletedDocumentWebservice()
            }
            merge_pdf?.setOnClickListener {
                client_id = ""
                tv_select_groups_view?.text = ""
                tv_select_groups_view?.setHint(R.string.select_groups)
                tv_search_client_view?.text?.clear()
                tv_search_client_views?.text?.clear()
                category_name?.text = ""
                groupsList.clear()
                selected_groups_list.clear()
                matterlist.clear()
                matter_id = ""
                ismatter_chosen = true
                rv_display_view_docs?.visibility = GONE
                ischecked2 = true
                hideViewDoc()
                if (is_MergePdfClicked) {
                    merge_pdf?.setTextColor(requireContext().getResources().getColor(R.color.white))
                    merge_pdf?.background = requireContext().getResources().getDrawable(R.drawable.rectangular_button_green_count)
                } else {
                    merge_pdf?.setTextColor(requireContext().getResources().getColor(R.color.black))
                    merge_pdf?.background = requireContext().getResources().getDrawable(R.drawable.rectangular_light_grey_background)
                }
                is_MergePdfClicked = !is_MergePdfClicked
            }

            btn_cancel?.setOnClickListener {
                count_file = 0
                clear_upload()
                custom_spinner?.text = ""
                ll_matter?.visibility = GONE
                tv_selected_file?.text = ""
                img_clear_icon1?.visibility = GONE
                img_dropdown_icon1?.visibility = VISIBLE
                tv_selected_file?.text = ""
                view_document()
            }
            btn_upload?.setOnClickListener {
                callUploadDocumentWebservice()
            }
            if (Constants.ROLE == "SU") {
                tv_deleted_view?.visibility = VISIBLE
            } else {
                tv_deleted_view?.visibility = GONE
            }

        } catch (e: Exception) {
            e.fillInStackTrace()
        }
        return v
    }

    private fun Searchfilter(tv_search_client_view: TextInputEditText) {
        val query = tv_search_client_view.text.toString().trim().lowercase(Locale.getDefault())
        val filteredList = ArrayList<ViewDocumentsModel>()
        if (!view_docs_list.isEmpty()) {
            for (item in view_docs_list) {
                val name = item.name ?: ""
                if (name.lowercase(Locale.getDefault()).contains(query)) {
                    filteredList.add(item)
                }
            }
        }

        val startIndex = PaginationHelper.startIndexForCurrentPage(currentPage, 10)
        val endIndex = PaginationHelper.endIndexForCurrentPage(startIndex, filteredList.size, 10)
        pageItems = ArrayList(filteredList.subList(startIndex, endIndex))
        adapter1?.setData(pageItems)
        currentPage = 1

        setupPagination(filteredList)
        UpdatePageButton(currentPage)
    }

    private fun setupPagination(view_docs_list: ArrayList<ViewDocumentsModel>) {
        pageNumberLayout?.removeAllViews()
        pagebuttons.clear()

        val totalPages = PaginationHelper.calculateTotalNoOfPages(view_docs_list.size, 10)
        Log.d("total_pages", "" + totalPages + ".." + view_docs_list.size)
        if (view_docs_list.isEmpty()) {
            ll_page_navigaiton?.visibility = GONE
        } else {
            ll_page_navigaiton?.visibility = VISIBLE
        }

        for (i in 1..totalPages) {
            val view_opponents = LayoutInflater.from(getContext()).inflate(R.layout.page_number_layout, null)
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

    private fun UpdatePageButton(currentPage: Int) {
        for (i in 0 until pagebuttons.size) {
            val pageButton = pagebuttons[i]
            if (currentPage >= 0) {
                if (i + 1 == currentPage) {
                    pageButton.setTextColor(requireActivity().getColor(R.color.white))
                    pageButton.setBackgroundColor(requireActivity().getColor(R.color.green_count_color))
                } else {
                    pageButton.setTextColor(requireActivity().getColor(R.color.white))
                    pageButton.setBackgroundColor(requireActivity().getColor(R.color.Blue_text_color))
                }
            }
        }
    }

    private fun view_document() {
        siv_view_document?.setImageDrawable(requireContext().getResources().getDrawable(R.mipmap.green_count_backgroung_icon))
        siv_view_document?.background = requireContext().getResources().getDrawable(R.color.green_count_color)
        siv_upload_document?.setImageDrawable(requireContext().getResources().getDrawable(R.mipmap.uploadwhiteupload1))
        siv_upload_document?.background = requireContext().getResources().getDrawable(R.color.white)
        ll_client_name_view?.visibility = VISIBLE
        DOCUMENT_TYPE_TAG = "client"
        CATEGORY_TAG = "client"
        ll_view_docs?.visibility = VISIBLE
        rv_display_view_docs?.visibility = GONE
        rv_display_view_docs?.removeAllViews()
        view_group_layout?.visibility = GONE
        ll_upload_docs?.visibility = GONE
        ll_matter_view?.visibility = GONE
        rv_display_upload_groups_docs?.removeAllViews()
        clearClients()
        ismatter_chosen = false
        isUploadDoc = false
        if (UPLOAD_TAG == "Firm") {
            hideviewClientBackground()
        } else {
            hideviewFirmBackground()
        }
        mViewModel?.setData(requireContext().getString(R.string.document_view))
    }

    private fun callViewDocumentWebservice() {
        try {
            val jsonObject = JSONObject()
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v3/documents/" + DOCUMENT_TYPE_TAG, "VIEW_DOCUMENT", jsonObject.toString())
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
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "docs/deleted/list", "DELETED_DOCUMENT", jsonObject.toString())
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun hideviewFirmBackground() {
        tv_client_doc?.setText(R.string.list_of_client_documents)
        VIEW_TAG = "Client"
        ischecked2 = true
        tv_search_client_view?.text?.clear()
        ll_matter_view?.visibility = GONE
        ll_client_name_view?.visibility = VISIBLE
        ll_categories_layout?.visibility = GONE
        ll_document_type_view?.visibility = GONE
        tv_search_client_view?.visibility = VISIBLE
        tv_search_client_views?.visibility = GONE
        ll_merge_pdf?.visibility = VISIBLE
        view_docs_list.clear()
        rv_display_view_docs?.removeAllViews()
        rv_display_view_docs?.visibility = GONE
        hideViewDoc()

        tv_deleted_view?.background = requireContext().getResources().getDrawable(R.drawable.button_right_background)
        tv_deleted_view?.setTextColor(Color.BLACK)
        if (Constants.ROLE == "SU") {
            tv_firm_view?.background = requireContext().getResources().getDrawable(R.drawable.radiobutton_centre_background)
        } else {
            tv_firm_view?.background = requireContext().getResources().getDrawable(R.drawable.button_right_background)
        }
        tv_firm_view?.setTextColor(Color.BLACK)
        tv_client_view?.background = requireContext().getResources().getDrawable(R.drawable.button_left_green_background)
        tv_client_view?.setTextColor(Color.WHITE)
    }

    private fun hideViewDoc() {
        hideClientView()
        hideMatterView()
        ll_page_navigaiton?.visibility = GONE
    }

    private fun hideClientView() {
        custom_spinner3?.text = ""
        img_dropdown_icon3?.visibility = VISIBLE
        img_clear_icon3?.visibility = GONE
        list_client_view?.visibility = GONE
        ll_matter_view?.visibility = GONE
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
        ll_matter?.visibility = GONE
    }

    private fun hideMatterUpload() {
        custom_spinner2?.text = ""
        img_dropdown_icon2?.visibility = VISIBLE
        img_clear_icon2?.visibility = GONE
        list_matter?.visibility = GONE
    }

    private fun hideviewClientBackground() {
        tv_client_doc?.setText(R.string.list_of_firm_documents)
        VIEW_TAG = "Firm"
        CATEGORY_TAG = "firm"
        view_docs_list.clear()
        rv_display_view_docs?.removeAllViews()
        rv_display_view_docs?.visibility = GONE
        tv_search_client_views?.text?.clear()
        ll_merge_pdf?.visibility = VISIBLE
        ll_client_name_view?.visibility = GONE
        ll_document_type_view?.visibility = VISIBLE
        tv_search_client_view?.visibility = GONE
        tv_search_client_views?.visibility = VISIBLE
        ll_categories_layout?.visibility = VISIBLE
        tv_select_groups_view?.text = ""
        tv_select_groups_view?.setHint(R.string.select_groups)
        groupsList.clear()
        clearListData()
        hideViewDoc()
        if (Constants.ROLE == "SU") {
            tv_firm_view?.background = requireContext().getResources().getDrawable(R.drawable.radiobutton_centre_green_background)
        } else {
            tv_firm_view?.background = requireContext().getResources().getDrawable(R.drawable.button_right_green_background)
        }
        tv_firm_view?.setTextColor(Color.WHITE)
        tv_client_view?.background = requireContext().getResources().getDrawable(R.drawable.button_left_background)
        tv_client_view?.setTextColor(Color.BLACK)
        tv_deleted_view?.background = requireContext().getResources().getDrawable(R.drawable.button_right_background)
        tv_deleted_view?.setTextColor(Color.BLACK)
    }

    private fun upload_documents() {
        isUploadDoc = true
        isUpdateTag = false
        clearClients()
        tv_client?.background = requireContext().getResources().getDrawable(R.drawable.button_left_green_background)
        siv_view_document?.setImageDrawable(requireContext().getResources().getDrawable(R.mipmap.eye_whitebackground_icon))
        siv_view_document?.background = requireContext().getResources().getDrawable(R.color.white)
        siv_upload_document?.background = requireContext().getResources().getDrawable(R.color.green_count_color)
        siv_upload_document?.setImageDrawable(requireContext().getResources().getDrawable(R.mipmap.green_background_icon))
        ll_upload_docs?.visibility = VISIBLE
        ll_view_docs?.visibility = GONE
        rv_documents?.removeAllViews()
        rv_display_upload_groups_docs?.removeAllViews()
        hideFirmBackground()

        hideUploadDoc()

        mViewModel?.setData(requireContext().getString(R.string.document_upload))
        count_file = 0
        tv_select_groups?.text = ""
        tv_select_groups?.setHint(R.string.select_groups)
        tv_selected_file?.text = ""
        tv_selected_file?.setHint(R.string.select_documents)
        UPLOAD_TAG = "Client"
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
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.PUT, "v3/documents/groupslist", "Client Groups", jsonObject.toString())
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
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v3/groups", "Groups", jsonObject.toString())
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun open_add_tags_popup() {
        if (!selected_documents_list.isEmpty()) {
            tags_list.clear()
        }
        if (isUpdateTag || !selected_documents_list.isEmpty()) {
            val dialogBuilder = AlertDialog.Builder(requireContext())
            cl_document?.alpha = 0.5f
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
            tv_tag_name?.addTextChangedListener(Validation(tv_tag_name!!))
            tv_tag_type?.addTextChangedListener(Validation(tv_tag_type!!))
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
            val btn_cancel_popup = view.findViewById<AppCompatButton>(R.id.btn_cancel_tag)
            val btn_save_tag = view.findViewById<AppCompatButton>(R.id.btn_save_tag)
            val iv_cancel = view.findViewById<ImageView>(R.id.close_edit_docs)
            ll_added_tags = view.findViewById(R.id.ll_added_tags)
            if (isUpdateTag && !tags_list.isEmpty()) {
                add_tags_listing(btn_save_tag)
            }
            val dialog = dialogBuilder.create()
            iv_cancel.setOnClickListener {
                if (!tags_list.isEmpty()) {
                    AndroidUtils.Delete_Popup(requireActivity(), dialog)
                } else {
                    dialog.dismiss()
                }
            }
            btn_cancel_popup.setOnClickListener {
                if (!tags_list.isEmpty()) {
                    AndroidUtils.Delete_Popup(requireActivity(), dialog)
                } else {
                    dialog.dismiss()
                }
            }
            btn_save_tag.isEnabled = false
            btn_save_tag.alpha = 0.5f

            btn_add.setOnClickListener {
                if (tv_tag_type?.text.toString().isEmpty() && tv_tag_name?.text.toString().isEmpty()) {
                    AndroidUtils.showAlert("Please check the Tag Type, Tag", requireActivity())
                } else if (tv_tag_type?.text.toString().isEmpty()) {
                    AndroidUtils.showAlert("Please check the Tag Type", requireActivity())
                } else if (tv_tag_name?.text.toString().isEmpty()) {
                    AndroidUtils.showAlert("Please check the Tag", requireActivity())
                } else {
                    if (isedit) {
                        save_edited_tags(tv_tag_type?.text.toString(), tv_tag_name?.text.toString())
                    } else {
                        add_tags_listing(btn_save_tag)
                    }
                    isedit = false
                    tv_tag_name?.setText("")
                    tv_tag_type?.setText("")
                    btn_save_tag.isEnabled = true
                    btn_save_tag.alpha = 1.0f
                }
            }
            btn_save_tag.setOnClickListener {
                if (!tags_list.isEmpty()) {
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

                            for (tagModel in tags_list) {
                                try {
                                    combinedTags.put(tagModel.tag_type, tagModel.tag_name)
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                            documentModel.tags = combinedTags
                        }

                        for (i in 0 until docsList.size) {
                            for (selectedDocument in selected_documents_list) {
                                if (docsList[i].name == selectedDocument.name) {
                                    docsList[i].tags = selectedDocument.tags
                                    docsList[i].isChecked = false
                                }
                            }
                        }

                        is_clicked_add = true
                        Hide_Add_EditMeta()
                        dialog.dismiss()
                    } else {
                        callUpdateTag()
                        is_clicked_add = true
                        dialog.dismiss()
                    }
                }
            }
            dialog.setOnDismissListener { cl_document?.alpha = 1.0f }
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setView(view)
            dialog.show()
        } else {
            AndroidUtils.showAlert("Please select atleast one document to add tags", requireActivity())
        }
    }

    private fun callUpdateTag() {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            val combinedTags = JSONObject()

            for (tagModel in tags_list) {
                try {
                    combinedTags.put(tagModel.tag_type, tagModel.tag_name)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            viewDocumentsModel?.tag = combinedTags

            jsonObject.put("name", viewDocumentsModel?.name)
            jsonObject.put("tags", combinedTags)

            if (!is_MergePdfClicked) {
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.PUT,
                    "v3/mergepdf/" + viewDocumentsModel?.id + "/tags",
                    "Update Tags",
                    jsonObject.toString()
                )
            } else {
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.PUT,
                    "v3/document/tags/" + viewDocumentsModel?.id,
                    "Update Tags",
                    jsonObject.toString()
                )
            }

        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            e.printStackTrace()
        }
    }

    private fun add_tags_listing(btn_save_tag: Button) {
        ll_added_tags?.removeAllViews()
        if (!tv_tag_type?.text.toString().isEmpty() && !tv_tag_name?.text.toString().isEmpty()) {
            val documentsModel = DocumentsModel()
            documentsModel.tag_type = tv_tag_type?.text.toString()
            documentsModel.tag_name = tv_tag_name?.text.toString()
            tags_list.add(documentsModel)
        }
        for (i in 0 until tags_list.size) {
            val view_added_tags = LayoutInflater.from(requireContext()).inflate(R.layout.displays_documents_list, null)
            tv_tag_document_name = view_added_tags.findViewById(R.id.tv_document_name)
            val chk_box_layout: LinearLayoutCompat = view_added_tags.findViewById(R.id.chk_box_layout)
            chk_box_layout.visibility = GONE
            val chk_selected_documents: CheckBox = view_added_tags.findViewById(R.id.chk_selected_documents)
            chk_selected_documents.visibility = GONE
            val iv_edit_tag: ImageView = view_added_tags.findViewById(R.id.iv_edit_meta)
            val iv_remove_tag: ImageView = view_added_tags.findViewById(R.id.iv_cancel)
            iv_remove_tag.tag = i
            iv_remove_tag.setOnClickListener { view ->
                val position = view.tag as Int
                ll_added_tags?.removeViewAt(position)
                btn_save_tag.isEnabled = true
                btn_save_tag.alpha = 1.0f
                val documentsModel1 = tags_list[position]
                documentsModel1.tag_name = ""
                documentsModel1.tag_type = ""
                documentsModel1.isChecked = false
                tags_list[position] = documentsModel1
                tags_list.removeAt(position)
                for (j in 0 until (ll_added_tags?.childCount ?: 0)) {
                    val iv_remove = ll_added_tags?.getChildAt(j)?.findViewById<ImageView>(R.id.iv_cancel)
                    iv_remove?.tag = j
                }
            }
            iv_edit_tag.tag = i
            iv_edit_tag.setOnClickListener { view ->
                var position = 0
                if (view.tag is Int) {
                    position = view.tag as Int
                    val documentsModel1 = tags_list[position]
                    if (documentsModel1 != null) {
                        edit_position = position
                        isedit = true
                        tv_tag_type?.setText(documentsModel1.tag_type)
                        tv_tag_name?.setText(documentsModel1.tag_name)
                    }
                }
            }
            iv_edit_tag.visibility = VISIBLE
            val tagName = tags_list[i].tag_type + " - " + tags_list[i].tag_name
            tv_tag_document_name?.text = tagName
            ll_added_tags?.addView(view_added_tags)
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun edit_tags(tag_type: String, tag_name: String, position: Int, view_tag: View, tv_tag_document_name: TextView) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        cl_document?.alpha = 0.5f
        val inflater = requireActivity().layoutInflater
        val view_edit_tags = inflater.inflate(R.layout.edit_tag, null)
        tv_edit_tag_type = view_edit_tags.findViewById(R.id.tv_edit_tag_type)
        tv_edit_tag_name = view_edit_tags.findViewById(R.id.tv_edit_tag_name)
        tag_type_edit = view_edit_tags.findViewById(R.id.tag_type_edit)
        tag_type_edit?.setText(R.string.tag_type)
        tag_edit = view_edit_tags.findViewById(R.id.tag_edit)
        tag_edit?.setText(R.string.tag)
        tv_edit_tag_type?.setHint(R.string.tag_type)
        tv_edit_tag_name?.setHint(R.string.tag_type)
        val btn_cancel_popup = view_edit_tags.findViewById<AppCompatButton>(R.id.btn_edit_cancel_tag)
        header_name_edit = view_edit_tags.findViewById(R.id.header_name)
        header_name_edit?.setText(R.string.edit_tag)
        val btn_save_edited_tag = view_edit_tags.findViewById<AppCompatButton>(R.id.btn_edit_save_tag)
        val iv_close_edit_tags = view_edit_tags.findViewById<ImageView>(R.id.close_edit_docs)
        tv_edit_tag_type?.setText(tag_type)
        tv_edit_tag_name?.setText(tag_name)
        val dialog = dialogBuilder.create()
        btn_cancel_popup.setOnClickListener { dialog.dismiss() }
        btn_save_edited_tag.setOnClickListener {
            save_edited_tags(tv_edit_tag_type?.text.toString(), tv_edit_tag_name?.text.toString())
        }
        iv_close_edit_tags.setOnClickListener { dialog.dismiss() }
        dialog.setOnDismissListener { cl_document?.alpha = 1.0f }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setView(view_edit_tags)
        dialog.show()
    }

    private fun save_edited_tags(tag_type: String, tag_name: String) {
        try {
            if (edit_position < 0 || edit_position >= tags_list.size) {
                AndroidUtils.showAlert("Invalid position for editing tag.", requireActivity())
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

            tv_tag_name?.setText("")
            tv_tag_type?.setText("")
        } catch (e: Exception) {
            Log.e("save_edited_tags", "Error saving edited tag", e)
            AndroidUtils.showAlert("An error occurred while saving the tag: " + e.message, requireActivity())
        }
    }

    private fun hideFirmBackground() {
        UPLOAD_TAG = "Client"
        ll_category?.visibility = GONE
        ll_groups?.visibility = GONE
        ll_client_name?.visibility = VISIBLE
        ll_upload_groups?.visibility = GONE
        selected_client_groups_list.clear()
        client_groups_list.clear()
        rv_display_upload_groups_docs?.removeAllViews()
        hideUploadDoc()
        tv_client?.background = requireContext().getResources().getDrawable(R.drawable.button_left_green_background)
        tv_client?.setTextColor(requireContext().getResources().getColor(R.color.white))
        tv_firm?.background = requireContext().getResources().getDrawable(R.drawable.button_right_background)
        tv_firm?.setTextColor(requireContext().getResources().getColor(R.color.black))
    }

    private fun clearClients() {
        count_file = 0
        tv_selected_file?.text = ""
        tv_selected_file?.setHint(R.string.select_documents)
        selected_documents_list.clear()
        clearListData()
    }

    private fun hideClientBackground() {
        UPLOAD_TAG = "Firm"
        ll_upload_groups?.visibility = GONE
        selected_client_groups_list.clear()
        upload_group_layout?.visibility = GONE
        ischecked_group = true
        ll_matter?.visibility = GONE
        ll_category?.visibility = VISIBLE
        ll_groups?.visibility = VISIBLE
        selected_groups_list.clear()
        tv_select_groups?.text = ""
        tv_select_groups?.setHint(R.string.select_groups)
        groupsList.clear()
        ll_client_name?.visibility = GONE
        rv_documents?.removeAllViews()
        tv_client?.background = requireContext().getResources().getDrawable(R.drawable.button_left_background)
        tv_firm?.background = requireContext().getResources().getDrawable(R.drawable.button_right_green_background)
        tv_firm?.setTextColor(requireContext().getResources().getColor(R.color.white))
        tv_client?.setTextColor(requireContext().getResources().getColor(R.color.black))
    }

    private fun clearListData() {
        ll_hide_document_details?.visibility = GONE
        selected_groups_list.clear()
        selected_documents_list.clear()
        langList.clear()
        tags_list.clear()
        docsList.clear()
        groupsList.clear()
    }

    private fun clear_upload() {
        ll_hide_document_details?.visibility = GONE
        tv_selected_file?.setHint(R.string.select_documents)
        selected_groups_list.clear()
        selected_documents_list.clear()
        langList.clear()
        tags_list.clear()
        docsList.clear()
        groupsList.clear()
        tv_select_groups_view?.text = ""
        tv_select_groups_view?.setHint(R.string.select_groups)
    }

    private fun callUploadDocumentWebservice() {
        try {
            if (UPLOAD_TAG == "Client" && client_id.isEmpty()) {
                AndroidUtils.showAlert("Please check the Client Name", requireActivity())
            } else if (UPLOAD_TAG == "Firm" && selected_groups_list.isEmpty()) {
                AndroidUtils.showAlert("Please check the Groups", requireActivity())
            } else if (UPLOAD_TAG == "Client" && (!client_groups_list.isEmpty() && selected_client_groups_list.isEmpty())) {
                AndroidUtils.showAlert("Please check the Client Groups", requireActivity())
            } else {
                if (docsList.isEmpty()) {
                    AndroidUtils.showAlert("Please Select Atleast one document", requireActivity())
                } else {
                    progress_dialog = AndroidUtils.get_progress(requireActivity())
                    totalUploads = docsList.size
                    completedUploads = 0
                    isAlertShown = false
                    if (UPLOAD_TAG == "Client") {
                        for (i in 0 until docsList.size) {
                            val jsonObject = JSONObject()
                            val clients = JSONArray()
                            val clients_jobject = JSONObject()
                            val groups = JSONArray()
                            for (k in 0 until selected_client_groups_list.size) {
                                val documentsModel1 = selected_client_groups_list[k]
                                groups.put(documentsModel1.group_id)
                            }
                            var docname = ""
                            val documentsModel = docsList[i]
                            filename = documentsModel.name
                            val new_file = documentsModel.file ?: continue
                            var doc_type = "pdf"
                            val content_string = new_file.name.replace(".", "/")
                            val content_type = content_string.split("/")
                            if (content_type.size >= 2) {
                                doc_type = content_type[1]
                                docname = content_type[0]
                            }

                            for (j in 0 until clientsList.size) {
                                if (clientsList[j].id == client_id) {
                                    val clientsModel = clientsList[j]
                                    clients_jobject.put("id", clientsModel.id)
                                    clients_jobject.put("type", clientsModel.type)
                                    clients.put(clients_jobject)
                                }
                            }
                            if (!matter_id.isEmpty()) {
                                val matter = JSONArray()
                                matter.put(matter_id)
                                jsonObject.put("matters", matter)
                            }
                            jsonObject.put("name", docsList[i].name)
                            jsonObject.put("description", docsList[i].description)
                            jsonObject.put("expiration_date", docsList[i].expiration_date)
                            jsonObject.put("filename", docname)
                            jsonObject.put("category", "client")
                            jsonObject.put("clients", clients)
                            jsonObject.put("groups", groups)
                            jsonObject.put("custom_encrypt", docsList[i].isencrypted)
                            jsonObject.put("downloadDisabled", DOWNLOAD_TAG)
                            if (docsList[i].tags == null) {
                                jsonObject.put("tags", "")
                            } else {
                                jsonObject.put("tags", docsList[i].tags)
                            }

                            if (isImageExtension(doc_type)) {
                                jsonObject.put("content_type", "image/$doc_type")
                            } else {
                                jsonObject.put("content_type", "application/$doc_type")
                            }
                            Log.d("Content_type", doc_type)
                            WebServiceHelper.callHttpUploadWebService(this, requireContext(), WebServiceHelper.RestMethodType.POST, "v3/document/upload", "Upload Document", new_file, jsonObject.toString())
                        }
                    } else {
                        for (i in 0 until docsList.size) {
                            currentpoistion++
                            val jsonObject = JSONObject()
                            val clients = JSONArray()
                            val clients_jobject = JSONObject()
                            var docname = ""
                            val documentsModel = docsList[i]
                            filename = documentsModel.name
                            val groups = JSONArray()
                            for (k in 0 until selected_groups_list.size) {
                                val documentsModel1 = selected_groups_list[k]
                                groups.put(documentsModel1.group_id)
                            }
                            val new_file = documentsModel.file ?: continue
                            var doc_type = "pdf"
                            val content_string = new_file.name.replace(".", "/")
                            val content_type = content_string.split("/")
                            if (content_type.size >= 2) {
                                doc_type = content_type[1]
                                docname = content_type[0]
                            }

                            for (j in 0 until clientsList.size) {
                                if (clientsList[j].id == client_id) {
                                    val clientsModel = clientsList[j]
                                    clients_jobject.put("id", clientsModel.id)
                                    clients_jobject.put("type", clientsModel.type)
                                    clients.put(clients_jobject)
                                }
                            }

                            jsonObject.put("name", docsList[i].name)
                            jsonObject.put("description", docsList[i].description)
                            jsonObject.put("filename", docname)
                            jsonObject.put("expiration_date", docsList[i].expiration_date)
                            jsonObject.put("category", "firm")
                            jsonObject.put("clients", "")
                            jsonObject.put("custom_encrypt", docsList[i].isencrypted)
                            jsonObject.put("groups", groups)
                            jsonObject.put("downloadDisabled", DOWNLOAD_TAG)
                            if (docsList[i].tags == null) {
                                jsonObject.put("tags", "")
                            } else {
                                jsonObject.put("tags", docsList[i].tags)
                            }

                            if (isImageExtension(doc_type)) {
                                jsonObject.put("content_type", "image/$doc_type")
                            } else {
                                jsonObject.put("content_type", "application/$doc_type")
                            }
                            Log.d("Content_type", "image/$doc_type")
                            WebServiceHelper.callHttpUploadWebService(this, requireContext(), WebServiceHelper.RestMethodType.POST, "v3/document/upload", "Upload Document", new_file, jsonObject.toString())
                        }
                    }
                }
            }
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            e.fillInStackTrace()
        }
    }

    private fun callClientWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v3/client/all/list", "Clients List", jsonObject.toString())
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun callCorpClientWebservice() {
        try {
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v3/corporate/list", "Corp Clients List", jsonObject.toString())
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    fun checkPermissionREAD_EXTERNAL_STORAGE(context: Context): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (ContextCompat.checkSelfPermission(context, READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, READ_MEDIA_VISUAL_USER_SELECTED) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions.launch(arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED))
                } else {
                    BottomSheetUploadfile()
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions.launch(arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO))
                } else {
                    BottomSheetUploadfile()
                }
            } else {
                if (ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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

    private fun BottomSheetUploadfile() {
        cl_document?.alpha = 0.5f
        Constants.isDocEditor = false
        bottommSheetUploadDocument = BottomSheetUploadFile(cl_document)
        bottommSheetUploadDocument?.show(getParentFragmentManager(), "")
        bottommSheetUploadDocument?.setTargetFragment(this, 1)
    }

    override fun getImagepath(imagepath: File?, ImageURI: Uri?) {
        if (imagepath == null) {
            mSelectedBitmap = null
            mSelectedUri = imagepath
            val uri = imagepath.toString()
            val imageLoader = ImageLoader.getInstance()
            imageLoader.init(ImageLoaderConfiguration.createDefault(requireActivity()))
            if (imageView != null) {
                imageLoader.displayImage(Uri.fromFile(File(uri)).toString(), imageView)
            }
            file = imagepath

            val c = requireContext().contentResolver.query(ImageURI!!, null, null, null, null)
            c?.moveToFirst()
            val file_name = c?.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME)) ?: "unknown"
            c?.close()

            Hide_Add_EditMeta()
            is_clicked_edit = true
            is_clicked_add = true
            count_file++
            if (count_file > 0) {
                tv_selected_file?.text = "$count_file files"
            }
            if (count_file == 0) {
                tv_selected_file?.text = ""
                tv_selected_file?.setHint(R.string.select_documents)
            }

            load_documents(docsList, file_name, file!!)
        } else {
            try {
                file = getFile(requireContext(), ImageURI!!)
                Log.i("FILE", "Info:" + file.toString())
                val file_name = file!!.name
                count_file++
                Hide_Add_EditMeta()
                is_clicked_edit = true
                is_clicked_add = true
                if (count_file > 0) {
                    tv_selected_file?.text = "$count_file files"
                }
                if (count_file == 0) {
                    tv_selected_file?.text = ""
                    tv_selected_file?.setHint(R.string.select_documents)
                }
                load_documents(docsList, file_name, file!!)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        cl_document?.alpha = 1.0f
        bottommSheetUploadDocument?.dismiss()
    }

    fun remove_file(ischecked: Boolean) {
        if (ischecked) {
            count_file--
            if (count_file < 0) {
                count_file = 0
            }
            tv_selected_file?.text = "$count_file files"
            if (count_file == 0) {
                ll_hide_document_details?.visibility = GONE
            }
        }
    }

    private fun load_documents(docsList: ArrayList<DocumentsModel>, file_name: String, file: File) {
        var doc_type = ""
        var docname = ""
        val content_string = file_name.replace(".", "/")
        val content_type = content_string.split("/")
        if (content_type.size >= 2) {
            doc_type = content_type[1]
            docname = content_type[0]
        }
        val documentsModel = DocumentsModel()
        documentsModel.name = docname
        documentsModel.filename = file_name
        documentsModel.content_type = doc_type
        documentsModel.description = docname
        documentsModel.file = file
        documentsModel.isIsenabled = false
        documentsModel.isChecked = false
        docsList.add(documentsModel)
        if (!docsList.isEmpty()) {
            ll_hide_document_details?.visibility = VISIBLE
            DisableDownloadBackground()
            tv_enable_encryption?.background = requireContext().getResources().getDrawable(R.drawable.button_left_background)
            tv_enable_encryption?.setTextColor(requireContext().getColor(R.color.black))
            tv_disable_encryption?.setTextColor(requireContext().getColor(R.color.white))
            tv_disable_encryption?.background = requireContext().getResources().getDrawable(R.drawable.button_right_green_background)
        } else if (docsList.isEmpty()) {
            ll_hide_document_details?.visibility = GONE
        }
        EditMeta()
    }

    private fun loadRecyclerview(tag: String, subtag: String) {
        rv_documents?.layoutManager = LinearLayoutManager(requireContext())
        adapter = DocumentsListAdapter(docsList, tag, subtag, this, this)
        rv_documents?.adapter = adapter
        count_file = docsList.size
        AndroidUtils.LoadList(rv_documents, requireContext(), docsList.size, false)
        if (count_file == 0) {
            rv_documents?.visibility = GONE
            tv_selected_file?.text = ""
            tv_selected_file?.setHint(R.string.select_documents)
            ll_hide_document_details?.visibility = GONE
        } else {
            rv_documents?.visibility = VISIBLE
            tv_selected_file?.text = "$count_file files"
            ll_hide_document_details?.visibility = VISIBLE
        }

        chk_select_all?.setOnClickListener {
            chk_select_all?.isChecked = isselect_all_checked
            isselect_all_checked = !isselect_all_checked
            adapter?.selectOrDeselectAll(chk_select_all!!.isChecked)
        }
    }

    private fun DisableEncryptionBackground() {
        tv_enable_encryption?.background = requireContext().getResources().getDrawable(R.drawable.button_left_background)
        tv_enable_encryption?.setTextColor(requireContext().getColor(R.color.black))
        tv_disable_encryption?.setTextColor(requireContext().getColor(R.color.white))
        tv_disable_encryption?.background = requireContext().getResources().getDrawable(R.drawable.button_right_green_background)
    }

    private fun EnableEncryptionBackground() {
        ENCRYPTION_TAG = true
        tv_enable_encryption?.background = requireContext().getResources().getDrawable(R.drawable.button_left_green_background)
        tv_enable_encryption?.setTextColor(requireContext().getColor(R.color.white))
        tv_disable_encryption?.setTextColor(requireContext().getColor(R.color.black))
        tv_disable_encryption?.background = requireContext().getResources().getDrawable(R.drawable.button_right_background)
        adapter?.EncryptAllorDecryptAll(true)
        val tag = "en_encrption"
        loadRecyclerview(tag, subtag)
    }

    private fun DisableDownloadBackground() {
        DOWNLOAD_TAG = false
        tv_enable_download?.background = requireContext().getResources().getDrawable(R.drawable.button_left_background)
        tv_enable_download?.setTextColor(requireContext().getColor(R.color.black))
        tv_disable_download?.setTextColor(requireContext().getColor(R.color.white))
        tv_disable_download?.background = requireContext().getResources().getDrawable(R.drawable.button_right_green_background)
        val tag = "disable_download"
        loadRecyclerview(tag, subtag)
    }

    private fun EnableDownloadBackground() {
        DOWNLOAD_TAG = true
        tv_disable_download?.background = requireContext().getResources().getDrawable(R.drawable.button_right_background)
        tv_enable_download?.setTextColor(requireContext().getColor(R.color.white))
        tv_disable_download?.setTextColor(requireContext().getColor(R.color.black))
        tv_enable_download?.background = requireContext().getResources().getDrawable(R.drawable.button_left_green_background)
        val tag = "enable_download"
        loadRecyclerview(tag, subtag)
    }

    private fun AddTag() {
        chk_box_layout?.alpha = 1.0f
        chk_select_all?.isEnabled = true
        btn_upload?.visibility = GONE
        btn_add_tags?.visibility = VISIBLE
        tv_edit_meta?.setTextColor(requireContext().getResources().getColor(R.color.black))
        tv_add_tag?.setTextColor(requireContext().getResources().getColor(R.color.white))
        tv_edit_meta?.background = requireContext().getResources().getDrawable(R.drawable.button_right_background)
        tv_add_tag?.background = requireContext().getResources().getDrawable(R.drawable.button_left_green_background)
        val tag = "add_tag"
        Constants.DocTagType = tag
        for (i in 0 until docsList.size) {
            docsList[i].isChecked = false
        }
        loadRecyclerview(tag, subtag)
    }

    private fun Hide_Add_EditMeta() {
        chk_box_layout?.alpha = 0.5f
        chk_select_all?.isEnabled = false
        chk_select_all?.isChecked = false
        isselect_all_checked = true
        btn_add_tags?.visibility = GONE
        btn_upload?.visibility = VISIBLE
        tv_edit_meta?.setTextColor(requireContext().getResources().getColor(R.color.black))
        tv_add_tag?.setTextColor(requireContext().getResources().getColor(R.color.black))
        tv_add_tag?.background = requireContext().getResources().getDrawable(R.drawable.button_left_background)
        tv_edit_meta?.background = requireContext().getResources().getDrawable(R.drawable.button_right_background)
        val tag = "Hide_Add_Edit_tag"
        Constants.DocTagType = tag
        for (i in 0 until docsList.size) {
            docsList[i].isChecked = false
        }
        loadRecyclerview(tag, subtag)
    }

    private fun EditMeta() {
        chk_box_layout?.alpha = 0.5f
        chk_select_all?.isEnabled = false
        chk_select_all?.isChecked = false
        isselect_all_checked = true
        btn_upload?.visibility = VISIBLE
        btn_add_tags?.visibility = GONE
        tv_edit_meta?.setTextColor(requireContext().getResources().getColor(R.color.white))
        tv_add_tag?.setTextColor(requireContext().getResources().getColor(R.color.black))
        tv_add_tag?.background = requireContext().getResources().getDrawable(R.drawable.button_left_background)
        tv_edit_meta?.background = requireContext().getResources().getDrawable(R.drawable.button_right_green_background)
        val tag = "edit_meta"
        Constants.DocTagType = tag
        for (i in 0 until docsList.size) {
            docsList[i].isChecked = false
        }
        loadRecyclerview(tag, subtag)
    }

    override fun getImageBitmap(bitmap: Bitmap?) {
        if (bitmap != null) {
            if (imageView != null) {
                imageView?.setImageBitmap(bitmap)
            }
            mSelectedBitmap = bitmap
            mSelectedUri = null
            val filesDir = requireContext().filesDir
            val imageFile = File(filesDir, "bitmap.jpg")
            var os: OutputStream? = null
            try {
                os = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                os.flush()
                os.close()
                file = imageFile
                tv_selected_file?.text = file!!.name
                val documentsModel = DocumentsModel()
                documentsModel.name = file!!.name
                docsList.add(documentsModel)
            } catch (e: Exception) {
                Log.e(javaClass.simpleName, "Error writing bitmap", e)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = requireContext().contentResolver.query(selectedImage!!,
                filePathColumn, null, null, null)
            assert(cursor != null)
            cursor!!.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()
            file = File(picturePath)
            filename = file!!.name
            tv_selected_file?.text = file!!.name
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            123 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                } else {
                    AndroidUtils.showAlert("GET_ACCOUNTS Denied", requireActivity())
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onClick(view: View) {
        // Empty implementation
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progress_dialog != null && progress_dialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }
        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                if (httpResult.requestType == "View Encrypted Doc") {
                    val image_data = httpResult.responseContent
                    encrypted_doc(image_data)
                } else {
                    val result = JSONObject(httpResult.responseContent)
                    if (httpResult.requestType == "Clients List") {
                        val data = result.getJSONObject("data")
                        try {
                            loadClients(data)
                            callCorpClientWebservice()
                        } catch (e: Exception) {
                            e.fillInStackTrace()
                        }
                    } else if (httpResult.requestType == "Corp Clients List") {
                        try {
                            loadCorpClients(result)
                        } catch (e: Exception) {
                            e.fillInStackTrace()
                        }
                    } else if (httpResult.requestType == "Deleted Documents") {
                        isDownload = false
                        val jsonObject = result.optJSONObject("data")
                        if (result.has("data")) {
                            assert(jsonObject != null)
                            val url = jsonObject.optString("url")
                            checkViewType(url)
                            Log.d("TAG_Image", url)
                        } else {
                            val msg = result.getString("msg")
                            AndroidUtils.showAlert(msg, requireActivity())
                            callDeletedDocumentWebservice()
                        }
                    } else if (httpResult.requestType == "Legal Matter") {
                        val matters = result.getJSONArray("matterList")
                        loadMatters(matters)
                        if (isUploadDoc) {
                            callClientGroupsWebservice()
                        }
                    } else if (httpResult.requestType == "Upload Document") {
                        completedUploads++
                        val isError = result.optBoolean("error")
                        val msg = result.optString("msg")
                        if (completedUploads == totalUploads && !isAlertShown) {
                            isAlertShown = true
                            if (!isError) {
                                AndroidUtils.showAlert(msg, requireActivity(), "")
                                rv_documents?.removeAllViews()
                                view_document()
                                clearListData()
                            } else {
                                AndroidUtils.showAlert(msg, requireActivity())
                            }
                        }
                    } else if (httpResult.requestType == "Groups") {
                        val data = result.optJSONArray("data")
                        loadGroupsData(data, groupsList, false)
                    } else if (httpResult.requestType == "Client Groups") {
                        val data = result.optJSONArray("data")
                        loadGroupsData(data, client_groups_list, true)
                    } else if (httpResult.requestType == "Display FilterDocuments") {
                        val data = result.optJSONArray("data")
                        if (ismatter_chosen) {
                            callLegalMatter()
                        }
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
                            AndroidUtils.showAlert(msg, requireActivity())
                        }
                    } else if (httpResult.requestType == "Update Documents") {
                        if (!result.optBoolean("error")) {
                            Dialog?.dismiss()
                            rv_display_view_docs?.removeAllViews()
                            callfilter_client_webservices()
                            val msg = "You have successfully \n updated document information."
                            AndroidUtils.showAlert(msg, requireActivity(), "Success")
                        } else {
                            val msg = result.optString("msg")
                            AndroidUtils.showAlert(msg, requireActivity())
                        }
                    } else if (httpResult.requestType == "Download Document") {
                        val iserror = result.getBoolean("error")
                        var msg = ""
                        if (iserror) {
                            msg = result.getString("msg")
                            AndroidUtils.showAlert(msg, requireActivity())
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
                            AndroidUtils.showAlert("You have successfully downloaded the document.", requireActivity(), "Success")
                            FileDownloader.downloadFile(requireContext(), url, "Download1")
                            callfilter_client_webservices()
                        }
                    } else if (httpResult.requestType == "Delete Documents") {
                        val iserror = result.getBoolean("error")
                        var msg = ""
                        if (iserror) {
                            msg = result.getString("msg")
                            AndroidUtils.showAlert(msg, requireActivity())
                        } else {
                            rv_display_view_docs?.removeAllViews()
                            callfilter_client_webservices()
                            msg = result.optString("msg")
                            AndroidUtils.showAlert(msg, requireActivity(), "Success")
                        }
                    } else if (httpResult.requestType == "Delete Merge Documents") {
                        val iserror = result.getBoolean("error")
                        val msg = result.getString("msg")
                        if (iserror) {
                            AndroidUtils.showAlert(msg, requireActivity())
                        } else {
                            AndroidUtils.showAlert(msg, requireActivity(), "Success")
                            rv_display_view_docs?.removeAllViews()
                            callfilter_client_webservices()
                        }
                    } else if (httpResult.requestType == "Decrypt Documents") {
                        val iserror = result.getBoolean("error")
                        var msg = ""
                        if (iserror) {
                            msg = result.getString("msg")
                            AndroidUtils.showAlert(msg, requireActivity())
                        } else {
                            rv_display_view_docs?.removeAllViews()
                            callfilter_client_webservices()
                            msg = "You have successfully \n decrypted document information."
                            AndroidUtils.showAlert(msg, requireActivity(), "Success")
                        }
                    } else if (httpResult.requestType == "Decrypt Doc" || httpResult.requestType == "Other Doc View") {
                        if (!result.getBoolean("error")) {
                            var url = ""
                            val jsonObject = result.optJSONObject("data")
                            assert(jsonObject != null)
                            url = jsonObject.optString("url")
                            if (isDownload) {
                                AndroidUtils.showAlert("You have successfully downloaded the document.", requireActivity(), "Success")
                                FileDownloader.downloadFile(requireContext(), url, "Download1")
                            } else {
                                loadDisplayDocuments(url)
                            }
                            Log.d("TAG_Image", url)
                        } else {
                            AndroidUtils.showAlert(result.optString("msg"), requireActivity())
                        }
                    } else if (httpResult.requestType == "Update Tags") {
                        val iserror = result.getBoolean("error")
                        var msg = ""
                        if (iserror) {
                            msg = result.getString("msg")
                            AndroidUtils.showAlert(msg, requireActivity())
                        } else {
                            msg = "You have successfully \n updated metadata for the document."
                            AndroidUtils.showAlert(msg, requireActivity(), "Success")
                        }
                        callfilter_client_webservices()
                    } else if (httpResult.requestType == "Encrypt Documents") {
                        var msg = result.optString("msg")
                        val error = result.getBoolean("error")
                        if (!error) {
                            msg = "You have successfully \n encrypted document information."
                            rv_display_view_docs?.removeAllViews()
                            callfilter_client_webservices()
                            AndroidUtils.showAlert(msg, requireActivity(), "")
                        } else {
                            AndroidUtils.showAlert(msg, requireActivity())
                        }
                    } else if (httpResult.requestType == "Enabled Documents") {
                        val msg = result.optString("msg")
                        val error = result.getBoolean("error")
                        if (!error) {
                            rv_display_view_docs?.removeAllViews()
                            callfilter_client_webservices()
                        }
                        AndroidUtils.showAlert(msg, requireActivity())
                    } else if (httpResult.requestType == "Disabled Documents") {
                        val msg = result.optString("msg")
                        val error = result.optBoolean("error")
                        if (!error) {
                            rv_display_view_docs?.removeAllViews()
                            callfilter_client_webservices()
                        }
                        AndroidUtils.showAlert(msg, requireActivity())
                    } else if (httpResult.requestType == "Display Documents") {
                        if (!result.getBoolean("error")) {
                            val url: String
                            if (!is_MergePdfClicked) {
                                url = result.optString("url")
                            } else {
                                val jsonObject = result.optJSONObject("data")
                                assert(jsonObject != null)
                                url = jsonObject.optString("url")
                            }
                            checkViewType(url)
                            Log.d("TAG_Image", url)
                        } else {
                            AndroidUtils.showAlert(result.optString("msg"), requireActivity())
                        }
                    }
                }
            } catch (e: JSONException) {
                e.fillInStackTrace()
                AndroidUtils.showAlert(e.message, requireActivity())
            }
        } else if (httpResult.result == WebServiceHelper.ServiceCallStatus.Failed) {
            try {
                val result = JSONObject(httpResult.responseContent)
                AndroidUtils.showErrorAlert(result.optString("msg"), requireActivity())
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        } else {
            AndroidUtils.showErrorAlert(httpResult.responseContent.toString(), requireActivity())
        }
    }

    private fun encrypted_doc(url: String) {
        val dialogBuilder = AlertDialog.Builder(requireActivity())
        cl_document?.alpha = 0.5f
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.view_documents, null)
        val iv_image = view.findViewById<ImageView>(R.id.doc_image)
        val header = view.findViewById<TextView>(R.id.header_name)
        val idPDFView = view.findViewById<PDFView>(R.id.idPDFView)
        header.text = doc_name
        val iv_close_edit_docs = view.findViewById<ImageView>(R.id.close_edit_docs)

        val list = CONTENT_TYPE.split("/")
        if (list.size >= 2 && isImageExtension(list[1])) {
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
                e.fillInStackTrace()
            }
        } else {
            iv_image.visibility = GONE
            PDFviewer(idPDFView, url)
        }
        val dialog = dialogBuilder.create()
        iv_close_edit_docs.setOnClickListener { dialog.dismiss() }
        dialog.setOnDismissListener { cl_document?.alpha = 1.0f }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setView(view)
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
        val dialogBuilder = AlertDialog.Builder(requireActivity())
        cl_document?.alpha = 0.5f
        val isImage = File_Content_Type.isImage(tempDocModel?.content_type)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.view_documents, null)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_pdf)
        val iv_image = view.findViewById<ImageView>(R.id.doc_image)
        val idPDFView = view.findViewById<PDFView>(R.id.idPDFView)
        val webView = view.findViewById<WebView>(R.id.doc_webview)
        val header = view.findViewById<TextView>(R.id.header_name)
        val iv_close_edit_docs = view.findViewById<ImageView>(R.id.close_edit_docs)
        header.text = doc_name

        iv_image.visibility = GONE
        idPDFView.visibility = GONE
        webView.visibility = GONE

        val dialog = dialogBuilder.create()
        Dialog = dialog

        val pdfTask = arrayOfNulls<RetrievePDFfromUrl>(1)

        iv_close_edit_docs.setOnClickListener {
            try {
                idPDFView?.recycle()
                if (pdfTask[0] != null) {
                    pdfTask[0]!!.cancelLoading()
                    pdfTask[0]!!.cancel(true)
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
            pdfTask[0]!!.execute(url)
        } else {
            if (isImage) {
                iv_image.visibility = VISIBLE
                Glide.with(requireContext())
                    .load(url)
                    .placeholder(R.drawable.progress_animation)
                    .centerCrop()
                    .into(iv_image)
            } else {
                idPDFView.visibility = VISIBLE
                progressBar.visibility = VISIBLE

                pdfTask[0] = RetrievePDFfromUrl(idPDFView, progressBar)
                pdfTask[0]!!.execute(url)
            }
        }
        dialog.setOnDismissListener { cl_document?.alpha = 1.0f }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setView(view)
        dialog.show()
    }

    private fun handleDocumentDisplay(url: String, docModel: ViewDocumentsModel, idPDFView: PDFView, iv_image: ImageView) {
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

            Glide.with(requireContext())
                .load(url)
                .placeholder(R.drawable.progress_animation)
                .centerCrop()
                .into(iv_image)
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
                    try {
                        if (DOCUMENT_TYPE_TAG != "Deleted") {
                            val date1 = sdf.parse(doc1.created)
                            val date2 = sdf.parse(doc2.created)
                            return date2.compareTo(date1)
                        } else {
                            val date1 = sdf.parse(doc1.created)
                            val date2 = sdf.parse(doc2.created)
                            return date2.compareTo(date1)
                        }
                    } catch (e: ParseException) {
                        e.printStackTrace()
                        return 0
                    }
                }
            })

            currentPage = 1
            setupPagination(view_docs_list)
            loadViewDocumentsRecyclerview(view_docs_list)
            UpdatePageButton(currentPage)

        } catch (e: JSONException) {
            e.printStackTrace()
            AndroidUtils.showAlert(e.message, requireActivity())
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
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, requireActivity())
        }
    }

    private fun loadViewDocumentsRecyclerview(view_docs_list: ArrayList<ViewDocumentsModel>) {
        try {
            if (view_docs_list.isEmpty()) {
                rv_display_view_docs?.removeAllViews()
                rv_display_view_docs?.visibility = GONE
                ll_page_navigaiton?.visibility = GONE
            } else {
                rv_display_view_docs?.layoutManager = GridLayoutManager(requireContext(), 1)
                adapter1 = View_documents_adapter(view_docs_list, this, requireContext(), is_MergePdfClicked, DOCUMENT_TYPE_TAG, ArrayList())
                rv_display_view_docs?.adapter = adapter1
                if (!view_docs_list.isEmpty()) {
                    val startIndex = PaginationHelper.startIndexForCurrentPage(currentPage, 10)
                    val endIndex = PaginationHelper.endIndexForCurrentPage(startIndex, view_docs_list.size, 10)
                    end_temp = endIndex
                    pageItems = ArrayList(view_docs_list.subList(startIndex, endIndex))
                    adapter1?.setData(pageItems)
                    UpdatePageButton(1)
                    rv_display_view_docs?.visibility = VISIBLE
                    ll_page_navigaiton?.visibility = VISIBLE
                }
                if (!tv_search_client_view?.text.toString().isEmpty()) {
                    Searchfilter(tv_search_client_view!!)
                }
                if (!tv_search_client_views?.text.toString().isEmpty()) {
                    Searchfilter(tv_search_client_views!!)
                }
                tv_search_client_view?.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                    override fun afterTextChanged(editable: Editable) {
                        Searchfilter(tv_search_client_view!!)
                    }
                })
                tv_search_client_views?.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                    override fun afterTextChanged(editable: Editable) {
                        Searchfilter(tv_search_client_views!!)
                    }
                })
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, requireActivity())
        }
    }

    private fun filterList(list: ArrayList<ViewDocumentsModel>, charString: String): ArrayList<ViewDocumentsModel> {
        isListFiltered = false
        if (charString.isEmpty()) {
            return list
        } else {
            val filteredList = ArrayList<ViewDocumentsModel>()
            val pattern = Pattern.compile(Pattern.quote(charString), Pattern.CASE_INSENSITIVE)

            for (row in list) {
                if (pattern.matcher(AndroidUtils.isNull(row.name).lowercase()).find() ||
                    pattern.matcher(AndroidUtils.isNull(row.filename).lowercase()).find()
                ) {
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

    private fun loadGroupsData(data: JSONArray?, groupsList: ArrayList<DocumentsModel>, isclient_groups: Boolean) {
        if (data == null) return
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
            if (isclient_groups) {
                GroupsPopup(ll_upload_groups!!, client_groups_list, selected_client_groups_list, rv_upload_groups!!, tv_select_upload_groups!!)
            } else {
                ll_upload_client_group?.visibility = GONE
                if (isUploadDoc) {
                    GroupsPopup(upload_group_layout!!, groupsList, selected_groups_list, rv_display_upload_groups_docs!!, tv_select_groups!!)
                } else {
                    GroupsPopup(view_group_layout!!, groupsList, selected_groups_list, rv_display_view_groups_docs!!, tv_select_groups_view!!)
                }
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, requireActivity())
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
        upload_group_layout.visibility = VISIBLE
        try {
            for (i in 0 until groupsList.size) {
                for (j in 0 until selected_groups_list.size) {
                    if (groupsList[i].group_id == selected_groups_list[j].group_id) {
                        val documentsModel = groupsList[i]
                        documentsModel.isChecked = true
                    }
                }
            }

            val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            rv_display_upload_groups_docs.layoutManager = layoutManager

            val documentsAdapter = GroupsListAdapter(groupsList, this, GroupsListAdapter.OnCheckedChangeListener { documentsModel ->
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
                tv_select_groups.setHint(R.string.select_groups)
                val value = arrayOfNulls<String>(selected_groups_list.size)
                for (i in 0 until selected_groups_list.size) {
                    value[i] = selected_groups_list[i].group_name
                }
                val str = TextUtils.join(",", value)
                tv_select_groups.text = str
                EnableUpload()
            })

            val value = arrayOfNulls<String>(selected_groups_list.size)
            for (i in 0 until selected_groups_list.size) {
                value[i] = selected_groups_list[i].group_name
            }
            val str = TextUtils.join(",", value)
            tv_select_groups.text = str
            rv_display_upload_groups_docs.adapter = documentsAdapter
            AndroidUtils.LoadList(rv_display_upload_groups_docs, requireContext(), groupsList.size, true)

        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun loadClientGroups() {
        try {
            val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            rv_upload_groups?.layoutManager = layoutManager

            val documentsAdapter = GroupsListAdapter(client_groups_list, this, GroupsListAdapter.OnCheckedChangeListener { documentsModel ->
                if (documentsModel.isGroupChecked) {
                    selected_client_groups_list.add(documentsModel)
                } else {
                    for (i in 0 until selected_client_groups_list.size) {
                        if (selected_client_groups_list[i].group_id == documentsModel.group_id) {
                            selected_client_groups_list.removeAt(i)
                            break
                        }
                    }
                }
                val value = arrayOfNulls<String>(selected_client_groups_list.size)
                for (i in 0 until selected_client_groups_list.size) {
                    value[i] = selected_client_groups_list[i].group_name
                }
                val str = TextUtils.join(",", value)
                tv_select_upload_groups?.text = str
            })

            rv_upload_groups?.adapter = documentsAdapter
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun loadMatters(matters: JSONArray?) {
        if (matters == null) return
        if (!isUploadDoc) {
            val mattersModel1 = MattersModel()
            mattersModel1.id = "all"
            mattersModel1.title = "All Documents"
            mattersModel1.type = "consumer"
            matterlist.add(0, mattersModel1)
        }
        for (i in 0 until matters.length()) {
            val jsonObject = matters.getJSONObject(i)
            val mattersModel = MattersModel()
            mattersModel.id = jsonObject.optString("id")
            mattersModel.title = jsonObject.optString("title")
            mattersModel.type = jsonObject.optString("type")
            matterlist.add(mattersModel)
        }
        if (matterlist.isEmpty()) {
            ll_matter_view?.visibility = GONE
            ll_matter?.visibility = GONE
        } else {
            ll_matter_view?.visibility = VISIBLE
            ll_matter?.visibility = VISIBLE
        }
        initMatter()
    }

    private fun initMatter() {
        val adapter = CommonSpinnerAdapter(requireActivity(), this.matterlist)
        Log.i("ArrayList", "Info:$matterlist")
        list_matter?.adapter = adapter
        custom_spinner2?.text = ""
        custom_spinner4?.text = ""
        list_matter_view?.adapter = adapter
        AndroidUtils.LoadList(list_matter, requireContext(), matterlist.size, true)
        AndroidUtils.LoadList(list_matter_view, requireContext(), matterlist.size, true)

        list_matter?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            matter_id = matterlist[position].id ?: ""
            val matter_name_str = matterlist[position].title ?: ""
            Log.d("Matter_value_name", matter_name_str)
            custom_spinner2?.text = matter_name_str
            list_matter?.visibility = GONE
            ischecked_matter = true
            img_dropdown_icon2?.visibility = GONE
            img_clear_icon2?.visibility = VISIBLE
            callClientGroupsWebservice()
        }

        list_matter_view?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            matter_id = matterlist[position].id ?: ""
            val matter_name_str = matterlist[position].title ?: ""
            Log.d("Matter_value_name", matter_name_str)
            custom_spinner4?.text = matter_name_str
            list_matter_view?.visibility = GONE
            ischecked_matter2 = true
            ismatter_chosen = false
            callfilter_client_webservices()
            rv_display_view_docs?.visibility = VISIBLE
            img_dropdown_icon4?.visibility = GONE
            img_clear_icon4?.visibility = VISIBLE
        }
    }

    private fun initUI(clientsList: ArrayList<ClientsModel>) {
        val adapter = CommonSpinnerAdapter(requireActivity(), this.clientsList)
        list_client?.adapter = adapter
        list_client_view?.adapter = adapter
        AndroidUtils.LoadList(list_client, requireContext(), clientsList.size, true)
        AndroidUtils.LoadList(list_client_view, requireContext(), clientsList.size, true)

        list_client?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            client_id = clientsList[position].id ?: ""
            client_name = clientsList[position].name ?: ""
            Log.d("Client_value_name", client_name!!)
            ll_matter?.visibility = VISIBLE
            matter_id = ""
            matterlist.clear()
            callLegalMatter()
            Log.d("Matter_list_number", "" + matterlist.size)
            AndroidUtils.DisplaySpinnerView(list_client, custom_spinner, client_name, img_dropdown_icon1, img_clear_icon1, true)
            EnableUpload()
            img_dropdown_icon2?.visibility = VISIBLE
            img_clear_icon2?.visibility = GONE
            img_dropdown_icon1?.visibility = GONE
            img_clear_icon1?.visibility = VISIBLE
            ischecked = true
        }
        list_client_view?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            selected_groups_list.clear()
            client_id = clientsList[position].id ?: ""
            val client_name_str = clientsList[position].name ?: ""
            Log.d("Client_value_name", client_name_str)
            custom_spinner3?.text = client_name_str
            currentPage = 1
            ll_matter_view?.visibility = VISIBLE
            CATEGORY_TAG = "client"
            custom_spinner4?.text = ""
            matterlist.clear()
            matter_id = ""
            ismatter_chosen = true
            callfilter_client_webservices()
            rv_display_view_docs?.visibility = VISIBLE
            list_client_view?.visibility = GONE
            ischecked2 = true
            img_dropdown_icon3?.visibility = GONE
            img_clear_icon3?.visibility = VISIBLE
            img_dropdown_icon4?.visibility = VISIBLE
            img_clear_icon4?.visibility = GONE
        }
    }

    private fun loadCorpClients(data: JSONObject) {
        val relationships = data.getJSONArray("relationships")
        CorpClientsList.clear()
        for (i in 0 until relationships.length()) {
            val jsonObject = relationships.getJSONObject(i)
            val clientsModel = ClientsModel()
            clientsModel.id = jsonObject.optString("id")
            clientsModel.name = jsonObject.optString("name")
            if (jsonObject.optString("type") != "consumer") {
                clientsModel.type = "corporate"
            }
            CorpClientsList.add(clientsModel)
        }
        clientsList.addAll(CorpClientsList)
        initUI(clientsList)
    }

    private fun loadClients(data: JSONObject) {
        val relationships = data.getJSONArray("relationships")
        clientsList.clear()
        for (i in 0 until relationships.length()) {
            val jsonObject = relationships.getJSONObject(i)
            val clientsModel = ClientsModel()
            clientsModel.id = jsonObject.optString("id")
            clientsModel.name = jsonObject.optString("name")
            clientsModel.type = jsonObject.optString("type")
            clientsList.add(clientsModel)
        }
    }

    private fun EnableUpload() {
        if (!custom_spinner?.text.toString().isEmpty() || !selected_groups_list.isEmpty()) {
            Enable_Button(btn_upload!!, true)
        } else {
            Enable_Button(btn_upload!!, false)
        }
    }

    private fun callLegalMatter() {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            Log.d("Client_id", client_id)
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v3/matter/all/$client_id", "Legal Matter", jsonObject.toString())
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            e.fillInStackTrace()
        }
    }

    override fun ViewTags(documentsModel: DocumentsModel, itemsArrayList: ArrayList<DocumentsModel>) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        cl_document?.alpha = 0.5f
        val inflater = requireActivity().layoutInflater
        val view_edit_tags = inflater.inflate(R.layout.edit_existing_tags, null)
        val ll_existing_tags = view_edit_tags.findViewById<LinearLayout>(R.id.ll_view_tags)
        val iv_close_existing_tags = view_edit_tags.findViewById<ImageView>(R.id.close_edit_docs)
        val header_name_view = view_edit_tags.findViewById<TextView>(R.id.header_name)
        val tv_document_name_view = view_edit_tags.findViewById<TextView>(R.id.tv_document_name)
        tv_document_name_view.textSize = DynamicUtils.twenty.toFloat()

        tv_document_name_view.visibility = VISIBLE
        tv_document_name_view.text = documentsModel.name
        header_name_view.setText(R.string.view_tags)

        val tags = documentsModel.tags
        if (tags != null) {
            val iter = tags.keys()
            while (iter.hasNext()) {
                val key = iter.next()
                val value = tags.optString(key)
                val view_added_tags = inflater.inflate(R.layout.displays_documents_list, null)
                val tv_tag_name_view = view_added_tags.findViewById<TextView>(R.id.tv_document_name)
                val iv_remove_tag = view_added_tags.findViewById<ImageView>(R.id.iv_cancel)

                val tag_msg = "$key - $value"
                tv_tag_name_view.text = tag_msg

                iv_remove_tag.setOnClickListener {
                    ll_existing_tags.removeView(view_added_tags)
                    tags.remove(key)
                }

                ll_existing_tags.addView(view_added_tags)
            }
        }

        val dialog = dialogBuilder.create()
        iv_close_existing_tags.setOnClickListener { dialog.dismiss() }
        dialog.setOnDismissListener { cl_document?.alpha = 1.0f }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setView(view_edit_tags)
        dialog.show()
    }

    override fun EditDocuments(documentsModel: DocumentsModel, itemsArrayList: ArrayList<DocumentsModel>, position: Int) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        cl_document?.alpha = 0.5f
        val inflater = requireActivity().layoutInflater
        val view_edit_documents = inflater.inflate(R.layout.edit_meta_data, null)
        val response_docname = view_edit_documents.findViewById<TextView>(R.id.response_docname)
        val response_docdes = view_edit_documents.findViewById<TextView>(R.id.response_docdes)
        val iv_cancel_edit_doc = view_edit_documents.findViewById<ImageView>(R.id.close_edit_docs)
        val btn_close_edit_docs = view_edit_documents.findViewById<AppCompatButton>(R.id.btn_cancel_edit_docs)
        val tv_doc_name = view_edit_documents.findViewById<TextInputEditText>(R.id.edit_doc_name)
        tv_doc_name.setMaxLines(5)

        tv_doc_name.filters = filters
        val tv_document_name_view = view_edit_documents.findViewById<TextView>(R.id.tv_document_name)
        tv_document_name_view.setText(R.string.document_name)

        val tv_description_view = view_edit_documents.findViewById<TextInputEditText>(R.id.edit_description)
        val description_label = view_edit_documents.findViewById<TextView>(R.id.description)
        description_label.setText(R.string.description)

        val tv_exp_date = view_edit_documents.findViewById<AppCompatButton>(R.id.tv_expiration_date)
        tv_exp_date.setHint(R.string.expiration_date)
        val expiration_date_id_view = view_edit_documents.findViewById<TextView>(R.id.expiration_date_id)
        expiration_date_id_view.setText(R.string.expiration_date)

        tv_doc_name.setText(documentsModel.name)
        tv_description_view.setText(documentsModel.description)
        tv_exp_date.setText(documentsModel.expiration_date)
        tv_description_view.setMaxLines(10)
        tv_description_view.filters = filters1
        tv_doc_name.addTextChangedListener(Validation(tv_doc_name))
        tv_description_view.addTextChangedListener(DescriptionValidation(tv_description_view))

        tv_exp_date.setOnClickListener {
            val myCalendar = Calendar.getInstance()
            val date = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
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
            datePickerDialog.setOnCancelListener {
                Log.d("DatePickerDialog", "Dialog canceled")
                tv_exp_date.text = ""
            }
            datePickerDialog.show()
        }

        val btn_save_tag = view_edit_documents.findViewById<AppCompatButton>(R.id.btn_save_tag)
        Enable_Button(btn_save_tag, !tv_doc_name.text.toString().isEmpty() && !tv_description_view.text.toString().isEmpty())
        val dialog = dialogBuilder.create()
        iv_cancel_edit_doc.setOnClickListener {
            if (!tv_doc_name.text.toString().isEmpty() || !tv_description_view.text.toString().isEmpty()) {
                AndroidUtils.Delete_Popup(requireActivity(), dialog)
            } else {
                dialog.dismiss()
            }
        }
        btn_close_edit_docs.setOnClickListener {
            if (!tv_doc_name.text.toString().isEmpty() || !tv_description_view.text.toString().isEmpty()) {
                AndroidUtils.Delete_Popup(requireActivity(), dialog)
            } else {
                dialog.dismiss()
            }
        }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Enable_Button(btn_save_tag, !tv_doc_name.text.toString().isEmpty() && !tv_description_view.text.toString().isEmpty())
                if (tv_doc_name.text.toString().isEmpty()) {
                    response_docname.visibility = VISIBLE
                    response_docname.text = "Document Name is required"
                } else {
                    response_docname.visibility = GONE
                }
                if (tv_description_view.text.toString().isEmpty()) {
                    response_docdes.visibility = VISIBLE
                    response_docdes.text = "Description is required"
                } else {
                    response_docdes.visibility = GONE
                }
            }
        }
        tv_doc_name.addTextChangedListener(textWatcher)
        tv_description_view.addTextChangedListener(textWatcher)

        btn_save_tag.setOnClickListener {
            for (i in 0 until itemsArrayList.size) {
                if (i == position) {
                    val documentsModel1 = itemsArrayList[i]
                    documentsModel1.name = tv_doc_name.text.toString()
                    documentsModel1.description = tv_description_view.text.toString()
                    documentsModel1.expiration_date = tv_exp_date.text.toString()
                    itemsArrayList[i] = documentsModel1
                    dialog.dismiss()
                    val tag = "edit_meta"
                    loadRecyclerview(tag, "")
                }
            }
        }
        dialog.setOnDismissListener { cl_document?.alpha = 1.0f }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setView(view_edit_documents)
        dialog.show()
    }

    private fun handleEditDocClose(name: String, description: String, exp_date: String, tv_doc_name: TextView, tv_description: TextView, tv_exp_date: TextView, dialog: Dialog) {
        val docName = tv_doc_name.text.toString()
        val desc = tv_description.text.toString()
        val exp = tv_exp_date.text.toString()
        if ((!docName.isEmpty() && docName != name) || (!desc.isEmpty() && desc != description) || (!exp.isEmpty() && exp != exp_date)) {
            AndroidUtils.Delete_Popup(requireActivity(), dialog)
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
        cl_document?.alpha = 0.5f
        val inflater = requireActivity().layoutInflater
        val view_edit_documents = inflater.inflate(R.layout.edit_meta_data, null)
        val response_docname = view_edit_documents.findViewById<TextView>(R.id.response_docname)
        val response_docdes = view_edit_documents.findViewById<TextView>(R.id.response_docdes)
        val iv_cancel_edit_doc = view_edit_documents.findViewById<ImageView>(R.id.close_edit_docs)
        val btn_close_edit_docs = view_edit_documents.findViewById<AppCompatButton>(R.id.btn_cancel_edit_docs)
        val tv_doc_name = view_edit_documents.findViewById<TextInputEditText>(R.id.edit_doc_name)
        tv_doc_name.setMaxLines(5)
        tv_doc_name.filters = filters
        val tv_document_name_view = view_edit_documents.findViewById<TextView>(R.id.tv_document_name)
        tv_document_name_view.setText(R.string.document_name)
        val tv_description_view = view_edit_documents.findViewById<TextInputEditText>(R.id.edit_description)
        tv_description_view.setMaxLines(10)
        tv_description_view.filters = filters1

        tv_doc_name.addTextChangedListener(Validation(tv_doc_name))
        tv_description_view.addTextChangedListener(DescriptionValidation(tv_description_view))
        val description_label = view_edit_documents.findViewById<TextView>(R.id.description)
        description_label.setText(R.string.description)
        val tv_exp_date = view_edit_documents.findViewById<AppCompatButton>(R.id.tv_expiration_date)
        tv_exp_date.setHint(R.string.expiration_date)
        val expiration_date_id_view = view_edit_documents.findViewById<TextView>(R.id.expiration_date_id)
        expiration_date_id_view.setText(R.string.expiration_date)

        tv_exp_date.setOnClickListener {
            val myCalendar = Calendar.getInstance()
            val date = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "dd-MM-yyyy"
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                tv_exp_date.setText(sdf.format(myCalendar.time))
            }
            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.setOnCancelListener {
                Log.d("DatePickerDialog", "Dialog canceled")
                tv_exp_date.text = ""
            }
            val cancelButton = datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            cancelButton?.setText("Clear")
            datePickerDialog.show()
        }

        if (viewDocumentsModel.expiration_date == "NA" || viewDocumentsModel.expiration_date == "") {
            tv_exp_date.text = ""
        } else {
            val exp_date = viewDocumentsModel.expiration_date
            val date_new = AndroidUtils.stringToDateTimeDefault(exp_date, "MMM dd, yyyy")
            val created = AndroidUtils.getDateToString(date_new, "dd-MM-yyyy")
            tv_exp_date.text = created
        }
        tv_doc_name.setText(viewDocumentsModel.name)
        tv_description_view.setText(viewDocumentsModel.description)
        val btn_save_tag = view_edit_documents.findViewById<AppCompatButton>(R.id.btn_save_tag)
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Enable_Button(btn_save_tag, !tv_doc_name.text.toString().isEmpty() && !tv_description_view.text.toString().isEmpty())
                if (tv_doc_name.text.toString().isEmpty()) {
                    response_docname.visibility = VISIBLE
                    response_docname.text = "Document Name is required"
                } else {
                    response_docname.visibility = GONE
                }
                if (tv_description_view.text.toString().isEmpty()) {
                    response_docdes.visibility = VISIBLE
                    response_docdes.text = "Description is required"
                } else {
                    response_docdes.visibility = GONE
                }
            }
        }
        tv_doc_name.addTextChangedListener(textWatcher)
        tv_description_view.addTextChangedListener(textWatcher)
        val dialog = dialogBuilder.create()
        iv_cancel_edit_doc.setOnClickListener {
            handleEditDocClose(viewDocumentsModel.name ?: "", viewDocumentsModel.description ?: "", viewDocumentsModel.expiration_date ?: "", tv_doc_name, tv_description_view, tv_exp_date, dialog)
        }
        btn_close_edit_docs.setOnClickListener {
            handleEditDocClose(viewDocumentsModel.name ?: "", viewDocumentsModel.description ?: "", viewDocumentsModel.expiration_date ?: "", tv_doc_name, tv_description_view, tv_exp_date, dialog)
        }

        btn_save_tag.setOnClickListener {
            Dialog = dialog
            callUpdateDocumentWebservice(tv_doc_name.text.toString(), tv_description_view.text.toString(), tv_exp_date.text.toString(), viewDocumentsModel.id ?: "")
        }
        dialog.setOnDismissListener { cl_document?.alpha = 1.0f }
        dialog.setCancelable(false)
        dialog.setView(view_edit_documents)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun callUpdateDocumentWebservice(name: String, description: String, expiration_date: String, id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            jsonObject.put("name", name)
            jsonObject.put("description", description)
            jsonObject.put("expiration_date", expiration_date)
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.PUT, "v3/document/$id", "Update Documents", jsonObject.toString())
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
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.POST, "docs/deleted/$delete", "Deleted Documents", jsonObject.toString())
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
            WebServiceHelper.callEmailHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.POST, "v3/decrypt", "View Encrypted Doc", jsonObject.toString())
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

    fun showConfirmDialog(context: Context, message: String, title: String, listener: OnConfirmClickListener) {
        try {
            val dialogBuilder = AlertDialog.Builder(context)
            val view = LayoutInflater.from(context).inflate(R.layout.delete_relationship, null)

            val header_name_view = view.findViewById<TextView>(R.id.header_name)
            val close_documents = view.findViewById<ImageView>(R.id.close_documents)
            val tv_confirmation = view.findViewById<TextView>(R.id.tv_confirmation)
            val btn_yes = view.findViewById<AppCompatButton>(R.id.btn_yes)
            val btn_no = view.findViewById<AppCompatButton>(R.id.btn_No)

            header_name_view.setTextColor(Color.BLACK)
            header_name_view.text = title
            tv_confirmation.text = message

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
        var title = "Alert ! "
        when (ViewType) {
            "disabled" -> deleteMsg = "Are you sure you want to Enable Download document?"
            "enabled" -> deleteMsg = "Are you sure you want to Disabled Download document?"
            "encrypt" -> deleteMsg = "Are you sure you want to encrypt Document?"
            "decrypt" -> deleteMsg = "Are you sure you want to decrypt Document?"
            "deleted" -> deleteMsg = "Are you sure to permanently delete document?"
            "download" -> {
                title = "Confirmation"
                deleteMsg = "Downloading this document will remove it from the secure system.\nDo you wish to proceed with the download?"
            }
            "restore" -> deleteMsg = "Are you sure to restore document?"
            else -> deleteMsg = "Are you sure you want to delete Document?"
        }

        showConfirmDialog(requireActivity(), deleteMsg, title, object : OnConfirmClickListener {
            override fun onYesClick() {
                when (ViewType) {
                    "disabled" -> enabled_doc(viewDocumentsModel.id ?: "")
                    "enabled" -> disabled_doc(viewDocumentsModel.id ?: "")
                    "encrypt" -> encryption_doc(viewDocumentsModel.id ?: "")
                    "decrypt" -> decryption_doc(viewDocumentsModel.id ?: "")
                    "download" -> Download_Document(viewDocumentsModel.id ?: "")
                    "deleted" -> Deleted_Document(viewDocumentsModel, "delete")
                    "restore" -> Deleted_Document(viewDocumentsModel, ViewType ?: "")
                    else -> callDeleteDocumentWebservice(viewDocumentsModel.id ?: "")
                }
            }

            override fun onNoClick() {
                // nothing extra
            }
        })
    }

    override fun Download_Document(viewDocumentsModel: ViewDocumentsModel?) {
        if (viewDocumentsModel != null) {
            tempDocModel = viewDocumentsModel
        }
    }

    fun Download_Document(docid: String) {
        try {
            val jsonObject = JSONObject()
            if (!is_MergePdfClicked) {
                progress_dialog = AndroidUtils.get_progress(requireActivity())
                WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v3/mergepdf/$docid/download", "Download Document", jsonObject.toString())
            } else if (tempDocModel?.added_encryption == true || tempDocModel?.is_encrypted == true) {
                callDecryptApi(docid, true)
            } else {
                progress_dialog = AndroidUtils.get_progress(requireActivity())
                WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v3/document/" + tempDocModel?.id + "/download", "Download Document", jsonObject.toString())
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
            for (i in 0 until tagArray.length()) {
                try {
                    val tagObject = tagArray.getJSONObject(i)

                    val tagType = tagObject.optString("key")
                    val tagName = tagObject.optString("value")

                    val tagModel = DocumentsModel()
                    tagModel.tag_type = tagType
                    tagModel.tag_name = tagName

                    tags_list.add(tagModel)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            open_add_tags_popup()
        }
    }

    fun disabled_doc(id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            jsonObject.put("downloadDisabled", false)
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.PATCH, "v3/document/$id", "Disabled Documents", jsonObject.toString())
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
            jsonObject.put("downloadDisabled", true)
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.PATCH, "v3/document/$id", "Enabled Documents", jsonObject.toString())
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
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.POST, "v3/document/encrypt/$id", "Encrypt Documents", jsonObject.toString())
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
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.POST, "v3/document/decrypt/$id", "Decrypt Documents", jsonObject.toString())
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
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.POST, Constants.decryptUrl ?: "", "Decrypt Doc", jsonObject.toString())
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
            WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, Constants.base_URL + "v3/document/$id/view", "Other Doc View", jsonObject.toString())
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
                WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v3/mergepdf/$id/view", "Display Documents", jsonObject.toString())
            } else {
                WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.GET, "v3/document/$id/view", "Display Documents", jsonObject.toString())
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
                jsonObject.put("clients", client_id)
                jsonObject.put("groups", null)
                if (!matter_id.isEmpty()) {
                    jsonObject.put("matters", matter_id)
                }
            } else if (CATEGORY_TAG == "firm") {
                val groups = JSONArray()
                for (k in 0 until selected_groups_list.size) {
                    val documentsModel1 = selected_groups_list[k]
                    groups.put(documentsModel1.group_id)
                }
                jsonObject.put("category", "firm")
                jsonObject.put("clients", "")
                jsonObject.put("matters", "")
                jsonObject.put("groups", groups)
                Log.d("Group_value_num", groups.toString())
                Log.d("Group_doc_view1", jsonObject.toString())
            }
            if (is_MergePdfClicked) {
                WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.PUT, "v3/document/filter", "Display FilterDocuments", jsonObject.toString())
            } else {
                WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.PUT, "v3/mergepdf/filter", "Display MergeFilterDocuments", jsonObject.toString())
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
                WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.POST, "v3/document/delete", "Delete Documents", jsonObject.toString())
            } else {
                jsonObject = JSONObject()
                WebServiceHelper.callHttpWebService(this, requireContext(), WebServiceHelper.RestMethodType.DELETE, "v3/mergepdf/$id", "Delete Merge Documents", jsonObject.toString())
            }
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun callclientfirmWebServices() {
        try {
            if (selected_groups_list.isEmpty()) {
                AndroidUtils.showAlert("Please select atleast one group", requireActivity())
            } else {
                if (VIEW_TAG == "Firm") {
                    for (i in 0 until docsList.size) {
                        currentpoistion++
                        val jsonObject = JSONObject()
                        var docname = ""
                        val documentsModel = docsList[i]
                        filename = documentsModel.name
                        val groups = JSONArray()
                        for (k in 0 until selected_groups_list.size) {
                            val documentsModel1 = selected_groups_list[k]
                            groups.put(documentsModel1.group_id)
                        }
                        val new_file = documentsModel.file ?: continue
                        var doc_type = "pdf"
                        val content_string = new_file.name.replace(".", "/")
                        val content_type = content_string.split("/")
                        if (content_type.size >= 2) {
                            doc_type = content_type[1]
                            docname = content_type[0]
                        }

                        jsonObject.put("category", "firm")
                        jsonObject.put("matters", "")
                        jsonObject.put("groups", groups)
                        jsonObject.put("showPdfDocs", false)

                        if (isImageExtension(doc_type)) {
                            jsonObject.put("content_type", "image/$doc_type")
                        } else {
                            jsonObject.put("content_type", "application/$doc_type")
                        }
                        rv_display_view_docs?.visibility = VISIBLE
                    }
                }
            }
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            e.fillInStackTrace()
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
        // Empty implementation
    }

    private fun getContType(docType: String): Boolean {
        return when (docType) {
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> true
            "application/msword" -> true
            "application/pdf" -> true
            "image/png" -> false
            "image/gif" -> false
            "image/jpg" -> false
            "image/jpeg" -> false
            "application/vnd.ms-excel" -> true
            "application/vnd.ms-powerpoint" -> true
            "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> true
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> true
            "text/csv" -> true
            "application/rtf" -> true
            "text/rtf" -> true
            else -> true
        }
    }



    companion object {
        private val IMAGE_EXTENSIONS = setOf("apng", "avif", "gif", "jpeg", "png", "svg", "webp", "jpg")

        @JvmStatic
        fun isImageExtension(ext: String): Boolean {
            return IMAGE_EXTENSIONS.contains(ext.lowercase(Locale.getDefault()))
        }

        @JvmStatic
        var tempDocModel: ViewDocumentsModel? = null

        @JvmStatic
        fun replaceLastDotWithSlash(input: String): String {
            val lastIndex = input.lastIndexOf('.')
            if (lastIndex == -1) {
                return input
            }
            return input.substring(0, lastIndex) + '/' + input.substring(lastIndex + 1)
        }

        @JvmStatic
        @Throws(IOException::class)
        fun convertfiletostring(file: File): String {
            val stringBuilder = StringBuilder()
            val fis = FileInputStream(file)
            val bufferedReader = BufferedReader(InputStreamReader(fis))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line).append("\n")
            }
            bufferedReader.close()
            return stringBuilder.toString()
        }

        @JvmStatic
        @Throws(IOException::class)
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

        @JvmStatic
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
            } catch (ex: Exception) {
                Log.e("Save File", ex.message ?: "")
                ex.fillInStackTrace()
            }
        }

        @JvmStatic
        private fun queryName(context: Context, uri: Uri): String {
            val returnCursor = context.contentResolver.query(uri, null, null, null, null)
            assert(returnCursor != null)
            val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            val name = returnCursor.getString(nameIndex)
            returnCursor.close()
            return name
        }
    }
}
