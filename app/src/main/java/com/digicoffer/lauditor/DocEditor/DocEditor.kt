package com.digicoffer.lauditor.DocEditor

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.CommonFiles.FileSelection.BottomSheetUploadFile
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.CommonFiles.PdfUtils.RetrievePDFfromUrl
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.ContentValidation
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.digicoffer.lauditor.DocEditor.Structure_Payload.DocumentPayloadBuilder
import com.digicoffer.lauditor.DocEditor.Structure_Payload.FieldContentModel
import com.digicoffer.lauditor.DocEditor.Structure_Payload.LaTeXUtils
import com.digicoffer.lauditor.Documents.Models.DocumentsModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.textfield.TextInputEditText
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.ArrayList
import java.util.Objects

class DocEditor : Fragment(), AsyncTaskCompleteListener, ViewDocAdapter.InterfaceListener,
    DocumentRecyclerAdapter.EventListeners, BottomSheetUploadFile.OnPhotoSelectedListner {

    private var tv_create: TextView? = null
    private var tv_view: TextView? = null
    private var tv_list_doc: TextView? = null
    private var tv_file: TextView? = null
    private var tv_insert: TextView? = null
    private var tv_doc_name: TextView? = null
    private var tv_title: TextView? = null
    private var tv_author: TextView? = null
    private var progress_dialog: Dialog? = null
    private var latexDocumentResponse = LatexDocumentResponse()
    private var ll_view_doc: LinearLayout? = null
    private var ll_create_view: LinearLayout? = null
    private var ll_create_document: LinearLayout? = null
    private var et_title: TextInputEditText? = null
    private var et_author: TextInputEditText? = null
    private var et_search_document: TextInputEditText? = null
    private var et_search: TextInputEditText? = null
    private var rv_documents: RecyclerView? = null
    private var mViewModel: NewModel? = null
    private var dialog: AlertDialog? = null
    private var tempTvImageName: TextView? = null
    private var tempImageView: ImageView? = null
    private var tempLlImageView: LinearLayout? = null
    private var tempFile: File? = null
    private val MAX_COLUMNS = 5
    private var NewDocName = ""
    private var isFileOption = true
    private var isInsertOption = true
    private var rv_viewDocEditor: RecyclerView? = null
    private var mSelectedBitmap: Bitmap? = null
    private var tv_image_name: TextView? = null
    private var ll_image_view: LinearLayout? = null
    private var mSelectedUri: File? = null
    private var imageView: ImageView? = null
    private var iv_eye_icon: ImageView? = null
    private var file: File? = null
    private var dialog1: AlertDialog? = null
    private var adapter: ViewDocAdapter? = null
    private var rv_open_document: RecyclerView? = null
    private var isView = false
    private var tv_switchCreate: LinearLayout? = null
    private var tv_switchView: LinearLayout? = null
    private val columnsManuallyAdded = booleanArrayOf(false)
    private var bottommSheetUploadDocument: BottomSheetUploadFile? = null
    private val docs = ArrayList<DocumentModel>()
    private var oldDocId = ""
    private var oldDocName = ""
    private val view_doc_list = ArrayList<DocListingModel>()
    private var pendingDeleteType: DeleteTargetType? = null
    private var pendingDeleteView: View? = null
    private var pendingColumnIndex = -1
    private var pendingRowLayout: LinearLayout? = null
    private var ImageLayout: View? = null
    private var filters: Array<InputFilter>? = null
    private var filters1: Array<InputFilter>? = null

    private enum class DeleteTargetType {
        ROW, TABLE, COLUMN
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.doc_editor, container, false)
        mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
        mViewModel?.setData(getString(R.string.doc_editor))
        tv_switchView = v.findViewById(R.id.tv_switchView)
        tv_switchCreate = v.findViewById(R.id.tv_switchCreate)
        tv_create = v.findViewById(R.id.tv_create)
        tv_create?.setText(R.string.create)
        tv_file = v.findViewById(R.id.tv_file)
        tv_file?.setText(R.string.file)
        tv_insert = v.findViewById(R.id.tv_insert)
        tv_insert?.setText(R.string.insert)
        tv_doc_name = v.findViewById(R.id.tv_doc_name)
        tv_doc_name?.setText(R.string.untitled_document)
        tv_title = v.findViewById(R.id.tv_title)
        tv_title?.setText(R.string.title)
        tv_author = v.findViewById(R.id.tv_author)
        tv_author?.setText(R.string.author)
        ll_create_view = v.findViewById(R.id.ll_create_view)
        ll_create_document = v.findViewById(R.id.ll_create_document)
        et_title = v.findViewById(R.id.et_title)
        et_title?.setHint(R.string.enter_the_title)
        et_author = v.findViewById(R.id.et_author)
        et_author?.setHint(R.string.enter_the_author)

        tv_view = v.findViewById(R.id.tv_view)
        tv_view?.setText(R.string.view)
        ll_view_doc = v.findViewById(R.id.ll_view_doc)
        tv_list_doc = v.findViewById(R.id.tv_list_doc)
        tv_list_doc?.setTextColor(Color.WHITE)
        tv_list_doc?.setText(R.string.list_of_created_documents)
        tv_list_doc?.visibility = View.GONE
        rv_viewDocEditor = v.findViewById(R.id.rv_viewDocEditor)
        rv_documents = v.findViewById(R.id.rv_documents)
        rv_documents?.visibility = View.GONE
        iv_eye_icon = v.findViewById(R.id.iv_eye_icon)
        iv_eye_icon?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.blue))
        et_search_document = v.findViewById(R.id.et_search_document)
        et_search_document?.setHint(R.string.search_document)

        loadCreate()
        et_title?.addTextChangedListener(ContentValidation(et_title!!, false))
        et_author?.addTextChangedListener(ContentValidation(et_author!!, false))
        filters = arrayOf(InputFilter.LengthFilter(255))
        filters1 = arrayOf(InputFilter.LengthFilter(50))
        et_title?.filters = filters
        et_author?.filters = filters1
        et_title?.maxLines = 1
        et_author?.maxLines = 1
        et_search_document?.addTextChangedListener(Validation(et_search_document!!))

        tv_create?.setOnClickListener {
            if (!Constants.is_active) {
                AndroidUtils.showRenewalPopup(requireActivity())
            } else {
                loadCreate()
            }
        }
        tv_view?.setOnClickListener {
            loadView()
        }
        AndroidUtils.setupModuleView(
            tv_switchCreate,
            getString(R.string.view_document),
            false, true, context, getString(R.string.create_document)
        ) {
            loadView()
        }
        AndroidUtils.setupModuleView(
            tv_switchView,
            getString(R.string.create_document),
            true, true, context, getString(R.string.list_of_created_documents)
        ) {
            if (!Constants.is_active) {
                AndroidUtils.showRenewalPopup(requireActivity())
            } else {
                loadCreate()
            }
        }
        tv_file?.setOnClickListener {
            isInsertOption = true
            tv_insert?.setTextColor(requireContext().getColor(R.color.black))
            if (isFileOption) {
                tv_file?.setTextColor(requireContext().getColor(R.color.blue))
                docs.clear()
                docs.add(DocumentModel(getString(R.string.new_), R.drawable.new_file_de))
                docs.add(DocumentModel(getString(R.string.open), R.drawable.open_file_de))
                docs.add(DocumentModel(getString(R.string.save), R.drawable.save))
                docs.add(DocumentModel(getString(R.string.save_as), R.drawable.save_as))
                docs.add(DocumentModel(getString(R.string.delete), R.drawable.delete_de))
                loadGridView()
            } else {
                tv_file?.setTextColor(requireContext().getColor(R.color.black))
                rv_documents?.visibility = View.GONE
            }
            isFileOption = !isFileOption
        }
        tv_insert?.setOnClickListener {
            isFileOption = true
            tv_file?.setTextColor(requireContext().getColor(R.color.black))
            if (isInsertOption) {
                tv_insert?.setTextColor(requireContext().getColor(R.color.blue))
                docs.clear()
                docs.add(DocumentModel(getString(R.string.overview), R.drawable.overview_de))
                docs.add(DocumentModel(getString(R.string.section), R.drawable.section))
                docs.add(DocumentModel(getString(R.string.sub_section), R.drawable.sub_section_de))
                docs.add(DocumentModel(getString(R.string.sub_sub_section), R.drawable.sub_sub_section_de))
                docs.add(DocumentModel(getString(R.string.paragraph), R.drawable.paragraph_de))
                docs.add(DocumentModel(getString(R.string.numbered_list), R.drawable.number_list_de))
                docs.add(DocumentModel(getString(R.string.bulleted_list), R.drawable.bulleted_list_de))
                docs.add(DocumentModel(getString(R.string.page_break), R.drawable.pagebreak_de))
                docs.add(DocumentModel(getString(R.string.image), R.drawable.image_de))
                docs.add(DocumentModel(getString(R.string.table), R.drawable.table_de))
                loadGridView()
            } else {
                tv_insert?.setTextColor(requireContext().getColor(R.color.black))
                rv_documents?.visibility = View.GONE
            }
            isInsertOption = !isInsertOption
        }
        iv_eye_icon?.setOnClickListener {
            if (oldDocId.isNotEmpty()) {
                ViewDoc("", oldDocId)
            } else {
                AndroidUtils.showError("Please save the document", requireActivity())
            }
        }
        return v
    }

    private fun loadSearch(et_search_document: TextInputEditText) {
        et_search_document.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                adapter?.filter?.filter(et_search_document.text.toString())
            }
        })
    }

    private fun loadGridView() {
        val layoutManager = GridLayoutManager(context, 4)
        rv_documents?.layoutManager = layoutManager
        rv_documents?.isNestedScrollingEnabled = false
        rv_documents?.setHasFixedSize(false)

        val adapter = DocumentRecyclerAdapter(requireContext(), docs, this)
        rv_documents?.adapter = adapter
        rv_documents?.visibility = View.VISIBLE
    }

    private fun loadCreate() {
        tv_switchCreate?.visibility = View.VISIBLE
        tv_switchView?.visibility = View.GONE
        tv_create?.setTextColor(requireContext().getColor(R.color.white))
        tv_view?.setTextColor(requireContext().getColor(R.color.black))
        tv_create?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_left_green_background))
        tv_view?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_right_background))
        ll_view_doc?.visibility = View.GONE
        ll_create_view?.visibility = View.VISIBLE
        et_search_document?.setText("")
        isFileOption = true
        isInsertOption = true
        isView = false
    }

    fun loadView() {
        tv_switchCreate?.visibility = View.GONE
        tv_switchView?.visibility = View.VISIBLE
        tv_create?.setTextColor(requireContext().getColor(R.color.black))
        tv_view?.setTextColor(requireContext().getColor(R.color.white))
        tv_create?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_left_background))
        tv_view?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_right_green_background))
        ll_view_doc?.visibility = View.VISIBLE
        ll_create_view?.visibility = View.GONE
        loadDocumentList()
        clearViews()
        oldDocId = ""
        oldDocName = ""
        clearNames()
        rv_documents?.visibility = View.GONE
        isFileOption = true
        isInsertOption = true
        tv_insert?.setTextColor(requireContext().getColor(R.color.black))
        tv_file?.setTextColor(requireContext().getColor(R.color.black))
        isView = true
    }

    private fun loadDocumentList() {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val postdata = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                Constants.DocEditorListingUrl ?: "",
                "Get DocEditor List",
                postdata.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun isFileSizeValid(file: File): Boolean {
        val MAX_SIZE_MB = 2
        val fileSizeInMB = file.length() / (1024 * 1024)
        return fileSizeInMB <= MAX_SIZE_MB
    }

    override fun getImagepath(imagepath: File?, ImageURI: Uri?) {
        if (imagepath != null) {
            mSelectedBitmap = null
            mSelectedUri = imagepath
            val uri = imagepath.toString()
            val imageLoader = ImageLoader.getInstance()
            imageLoader.init(ImageLoaderConfiguration.createDefault(requireActivity()))
            imageLoader.displayImage(Uri.fromFile(File(uri)).toString(), imageView)
            file = imagepath

            val c = requireContext().contentResolver.query(ImageURI!!, null, null, null, null)
            if (c != null) {
                c.moveToFirst()
                val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val file_name = c.getString(nameIndex)
                tempTvImageName?.text = file_name
                c.close()
            }
            if (isFileSizeValid(file!!)) {
                tempLlImageView?.visibility = View.VISIBLE
                callUploadImage(file!!)
            } else {
                AndroidUtils.showError("File size exceeds 2MB.", requireActivity())
                return
            }
        } else if (ImageURI != null) {
            try {
                file = getFile(requireContext(), ImageURI)
                Log.i("FILE", "Info:$file")
                val file_name = file!!.name
                tempTvImageName?.text = file_name
                if (isFileSizeValid(file!!)) {
                    tempLlImageView?.visibility = View.VISIBLE
                    callUploadImage(file!!)
                } else {
                    AndroidUtils.showError("File size exceeds 2MB.", requireActivity())
                    return
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        ll_create_view?.alpha = 1.0f
        bottommSheetUploadDocument?.dismiss()
    }

    override fun getImageBitmap(bitmap: Bitmap?) {
        if (bitmap != null) {
            imageView?.setImageBitmap(bitmap)
            mSelectedBitmap = bitmap
            mSelectedUri = null
            val filesDir = requireContext().filesDir
            val imageFile = File(filesDir, "bitmap.jpg")
            try {
                val os = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                os.flush()
                os.close()
                file = imageFile
                tempTvImageName?.text = file!!.name
                if (isFileSizeValid(file!!)) {
                    tempLlImageView?.visibility = View.VISIBLE
                    callUploadImage(file!!)
                } else {
                    AndroidUtils.showError("File size exceeds 2MB.", requireActivity())
                    return
                }
            } catch (e: Exception) {
                Log.e(javaClass.simpleName, "Error writing bitmap", e)
            }
        }
    }

    override fun onClick(view: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progress_dialog != null && progress_dialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }
        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                if (httpResult.requestType == "Delete DocEditor List") {
                    AndroidUtils.showAlert("Document deleted successfully", requireActivity())
                    if (isView) {
                        loadDocumentList()
                    } else {
                        clearViews()
                        clearNames()
                    }
                } else if (httpResult.requestType == "Open Document") {
                    val result = JSONArray(httpResult.responseContent)
                    val jsonObject = result.optJSONObject(0)
                    if (jsonObject.has("createdon")) {
                        latexDocumentResponse.createdon = Objects.requireNonNull(jsonObject.optJSONObject("createdon"))!!.optString("\$date")
                    }
                    latexDocumentResponse.document = jsonObject.optString("document")
                    latexDocumentResponse.docid = jsonObject.optString("docid")
                    latexDocumentResponse.page = jsonObject.optInt("page")
                    latexDocumentResponse.pageid = jsonObject.optString("pageid")
                    if (jsonObject.has("updatedon")) {
                        latexDocumentResponse.updatedon = Objects.requireNonNull(jsonObject.optJSONObject("updatedon"))!!.optString("\$date")
                    }
                    latexDocumentResponse.userid = jsonObject.optString("userid")
                    val documentLatex = jsonObject.optString("document")
                    loadDocumentFromPayload(documentLatex)
                    oldDocId = jsonObject.optString("docid")
                } else if (httpResult.requestType == "Upload DocEditor File") {
                    AndroidUtils.showSuccess("Image Uploaded Successfully", requireActivity())
                } else if (httpResult.requestType == "Save Document") {
                    val result = JSONObject(httpResult.responseContent)
                    val message = result.optString("message")
                    val docId = result.optString("id")
                    oldDocId = docId
                    latexDocumentResponse.docid = docId
                    if (docId.isEmpty()) {
                        AndroidUtils.showAlert(message, requireActivity())
                    } else {
                        triggerSavePayload(docId)
                        dialog?.dismiss()
                    }
                } else if (httpResult.requestType == "Save As Document") {
                    val result = JSONObject(httpResult.responseContent)
                    val message = result.optString("message")
                    val docId = result.optString("id")
                    if (docId.isEmpty()) {
                        AndroidUtils.showAlert(message, requireActivity())
                    } else {
                        AndroidUtils.showSaveAsConfirmation(requireActivity(), this, NewDocName)
                        dialog?.dismiss()
                    }
                } else if (httpResult.requestType == "Save DocEditor") {
                    val result = JSONObject(httpResult.responseContent)
                    val pageid = result.optString("id")
                    if (latexDocumentResponse.pageid != null && latexDocumentResponse.pageid!!.isNotEmpty()) {
                        AndroidUtils.showAlert_docs("Success", "Document updated successfully.", requireActivity())
                    } else {
                        AndroidUtils.showAlert_docs("Success", "Document saved successfully.", requireActivity())
                        latexDocumentResponse.pageid = pageid
                    }
                    if (oldDocName.isNotEmpty()) {
                        tv_doc_name?.text = oldDocName
                    } else {
                        tv_doc_name?.setText(R.string.untitled_document)
                    }
                } else if (httpResult.requestType == "Get DocEditor List") {
                    val result = JSONArray(httpResult.responseContent)
                    ListingDoc(result)
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
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

    private fun ListingDoc(list: JSONArray) {
        view_doc_list.clear()
        for (i in 0 until list.length()) {
            val docListingModel = DocListingModel()
            val jsonObject = list.optJSONObject(i)
            docListingModel.docid = jsonObject.optString("docid")
            docListingModel.documentname = jsonObject.optString("documentname")
            docListingModel.updatedon = jsonObject.optJSONObject("updatedon")
            docListingModel.`set$date`(docListingModel.updatedon?.optString("\$date"))
            view_doc_list.add(docListingModel)
        }
        if (isView) {
            rv_viewDocEditor?.let { loadRecyclerView(it) }
        } else {
            rv_open_document?.let { loadRecyclerView(it) }
        }
    }

    override fun OpenDoc(docListingModel: DocListingModel) {
        clearNames()
        oldDocName = docListingModel.documentname ?: ""
        val url = (Constants.Delete_doc ?: "") + docListingModel.docid
        callOpenDocument(url)
        CloseView()
    }

    override fun ViewDoc(documentname: String, docid: String) {
        val dialogBuilder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.view_documents, null)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_pdf)
        val idPDFView = view.findViewById<PDFView>(R.id.idPDFView)
        val header = view.findViewById<TextView>(R.id.header_name)
        val iv_close_edit_docs = view.findViewById<ImageView>(R.id.close_edit_docs)
        val dialog = dialogBuilder.create()

        val pdfTask = arrayOfNulls<RetrievePDFfromUrl>(1)

        iv_close_edit_docs.setOnClickListener {
            try {
                idPDFView?.recycle()
                pdfTask[0]?.cancelLoading()
                pdfTask[0]?.cancel(true)
            } catch (ignored: Exception) {
            }
            dialog.dismiss()
        }
        if (documentname.isEmpty()) {
            header.setText(R.string.preview)
        } else {
            header.text = documentname
        }
        val url = (Constants.OpenView_doc ?: "") + docid
        idPDFView.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE

        pdfTask[0] = RetrievePDFfromUrl(idPDFView, progressBar)
        pdfTask[0]?.execute(url)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    override fun DeleteDoc(docname: String, docid: String) {
        try {
            val dialogBuilder = AlertDialog.Builder(requireActivity())
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.delete_relationship, null)
            val header_name = view.findViewById<TextView>(R.id.header_name)
            header_name.setText(R.string.confirmation)
            val close_documents = view.findViewById<ImageView>(R.id.close_documents)
            header_name.setTextColor(Color.BLACK)
            val tv_confirmation = view.findViewById<TextView>(R.id.tv_confirmation)
            val delete_msg = "Are you sure you want to delete $docname document?"

            tv_confirmation.text = delete_msg
            val bt_yes = view.findViewById<AppCompatButton>(R.id.btn_yes)
            bt_yes.setBackgroundDrawable(requireContext().getDrawable(R.drawable.yes_button_red_button))
            val btn_no = view.findViewById<AppCompatButton>(R.id.btn_No)
            btn_no.setBackgroundDrawable(requireContext().getDrawable(R.drawable.no_button_green_button))

            val dialog = dialogBuilder.create()
            btn_no.setOnClickListener { dialog.dismiss() }
            close_documents.setOnClickListener { dialog.dismiss() }
            bt_yes.setOnClickListener {
                dialog.dismiss()
                callDeleteDocumentWebservice(docid)
            }
            dialog.setView(view)
            dialog.show()
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, requireActivity())
        }
    }

    private fun callOpenDocument(url: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                url,
                "Open Document",
                jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun callDeleteDocumentWebservice(docid: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.DELETE,
                (Constants.Delete_doc ?: "") + docid,
                "Delete DocEditor List",
                jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    override fun AddView(title: String, hint: String) {
        val staticViewsCount = 5
        var lastTagType: String? = null
        var overviewExists = false

        for (i in staticViewsCount until ll_create_document!!.childCount) {
            val child = ll_create_document!!.getChildAt(i)
            val tvContent = child.findViewById<TextView>(R.id.tv_content)
            if (tvContent != null && "Overview".equals(tvContent.tag?.toString() ?: "", ignoreCase = true)) {
                overviewExists = true
                break
            }
        }

        for (i in ll_create_document!!.childCount - 1 downTo staticViewsCount) {
            val child = ll_create_document!!.getChildAt(i)
            val tvContent = child.findViewById<TextView>(R.id.tv_content)
            if (tvContent != null && tvContent.tag != null) {
                lastTagType = tvContent.tag.toString().trim()
                break
            }
        }

        if (title.equals("Overview", ignoreCase = true)) {
            if (overviewExists) {
                AndroidUtils.showError("Only one Overview is allowed", requireActivity())
                return
            }
            if (ll_create_document!!.childCount > staticViewsCount) {
                AndroidUtils.showError("Overview should be the first item in the document", requireActivity())
                return
            }

            val inputViewProvider = ContentInputViewProvider(
                requireContext(),
                ll_create_document,
                object : ContentInputViewProvider.ContentActionListener {
                    override fun onRemoveClicked(view: View) {
                        checkConsecutivePageBreaks()
                    }

                    override fun onExpandClicked(titleTxt: String?, title: String?, content: String?, targetView: View) {
                        loadExpandView(titleTxt ?: "", title, content, targetView, false, true)
                    }
                },
                this
            )

            inputViewProvider.AddDocContent(title, title, hint)
            ll_create_document?.addView(inputViewProvider.getView(), staticViewsCount)
            return
        }

        if (title.equals("Sub Section", ignoreCase = true)) {
            if (!"Section".equals(lastTagType, ignoreCase = true) && !"Sub Section".equals(lastTagType, ignoreCase = true)) {
                AndroidUtils.showError("Invalid Selection. Please add Section before adding Sub Section or Sub Sub Section.", requireActivity())
                return
            }
        }

        if (title.equals("Sub Sub Section", ignoreCase = true)) {
            if (!"Sub Section".equals(lastTagType, ignoreCase = true) && !"Sub Sub Section".equals(lastTagType, ignoreCase = true)) {
                AndroidUtils.showError("Invalid Selection. Please add Sub Section before adding Sub Sub Section.", requireActivity())
                return
            }
        }

        val inputViewProvider = ContentInputViewProvider(
            requireContext(),
            ll_create_document,
            object : ContentInputViewProvider.ContentActionListener {
                override fun onRemoveClicked(view: View) {
                    checkConsecutivePageBreaks()
                }

                override fun onExpandClicked(titleTxt: String?, title: String?, content: String?, targetView: View) {
                    loadExpandView(titleTxt ?: "", title, content, targetView, true, true)
                }
            },
            this
        )

        inputViewProvider.AddDocContent(title, title, hint)
        ll_create_document?.addView(inputViewProvider.getView())
    }



    fun loadExpandView(
        titleTxt: String,
        tv_title: String?,
        tv_content: String?,
        targetView: View?,
        isTitleVisible: Boolean,
        isContentVisible: Boolean
    ) {
        val dialog = ExpandContentProvider(requireContext(), ll_create_view!!, isTitleVisible, isContentVisible)
        val actualCustomTitle = if (tv_title != null && tv_title.startsWith("$titleTxt - ")) {
            tv_title.substring(("$titleTxt - ").length).trim()
        } else {
            tv_title ?: ""
        }

        dialog.setTitles(titleTxt, actualCustomTitle, tv_content ?: "")

        dialog.setOnSaveClickListener(object : ExpandContentProvider.OnSaveClickListener {
            override fun onSave(title: String, content: String) {
                if (targetView != null) {
                    val tvContentTitle = targetView.findViewById<TextView>(R.id.tv_content)
                    val tvCaption = targetView.findViewById<TextView>(R.id.et_content)

                    if (tvContentTitle != null && title.isNotEmpty() && isTitleVisible) {
                        tvContentTitle.text = "$titleTxt - $title"
                    }
                    if (tvContentTitle != null && title.isEmpty()) {
                        tvContentTitle.text = titleTxt
                    }
                    if (tvCaption != null && isContentVisible) {
                        tvCaption.text = content
                    }
                }
                dialog.dismiss()
            }
        })
        dialog.show()
    }

    private fun callUploadImage(new_file: File) {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val postdata = JSONObject()
            WebServiceHelper.callHttpUploadWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.POST,
                Constants.DocEditorUploadImageURL ?: "",
                "Upload DocEditor File",
                new_file,
                postdata.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    fun AddDocListView(tagName: String?, items: MutableList<String>?) {
        val docListView = AddDocListView(requireContext(), ll_create_document, object : AddDocListView.ActionListener {
            override fun onRemove(view: View) {
                ll_create_document?.removeView(view)
                checkConsecutivePageBreaks()
            }
        })
        docListView.setContent(tagName, tagName)
        if (items != null && items.isNotEmpty()) {
            docListView.setListItems(items)
        }

        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 20)
        docListView.getView().layoutParams = layoutParams
        ll_create_document?.addView(docListView.getView())
    }

    fun AddPageBreak(name: String) {
        val staticViewsCount = 5
        val childCount = ll_create_document!!.childCount

        if (childCount == staticViewsCount) {
            AndroidUtils.showError("Page Break cannot be added at the beginning of the document.", requireActivity())
            return
        }

        val lastChild = ll_create_document!!.getChildAt(childCount - 1)
        val lastChildText = lastChild.findViewById<TextView>(R.id.tv_content)
        if (lastChildText != null && lastChildText.text.toString().equals(getString(R.string.new_page), ignoreCase = true)) {
            AndroidUtils.showError("Consecutive page breaks cannot be added.", requireActivity())
            return
        }

        val view_added_list = LayoutInflater.from(context).inflate(R.layout.page_break_layout, null)
        val tv_content = view_added_list.findViewById<TextView>(R.id.tv_content)
        val iv_remove = view_added_list.findViewById<ImageView>(R.id.iv_remove)

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 20)
        view_added_list.layoutParams = params

        iv_remove.setOnClickListener { ll_create_document?.removeView(view_added_list) }
        tv_content.setText(R.string.new_page)
        tv_content.tag = name
        ll_create_document?.addView(view_added_list)
        tv_content.isFocusable = true
        tv_content.isFocusableInTouchMode = true
        tv_content.requestFocus()
    }

    fun checkConsecutivePageBreaks() {
        val staticViewsCount = 5
        val childCount = ll_create_document!!.childCount

        if (childCount > staticViewsCount) {
            val firstDynamic = ll_create_document!!.getChildAt(staticViewsCount)
            val firstText = firstDynamic.findViewById<TextView>(R.id.tv_content)
            if (firstText != null && firstText.text.toString().equals(getString(R.string.new_page), ignoreCase = true)) {
                ll_create_document?.removeViewAt(staticViewsCount)
                return
            }
        }

        for (i in staticViewsCount + 1 until ll_create_document!!.childCount) {
            val prev = ll_create_document!!.getChildAt(i - 1)
            val curr = ll_create_document!!.getChildAt(i)

            val prevText = prev.findViewById<TextView>(R.id.tv_content)
            val currText = curr.findViewById<TextView>(R.id.tv_content)

            if (prevText != null && currText != null &&
                prevText.text.toString().equals(getString(R.string.new_page), ignoreCase = true) &&
                currText.text.toString().equals(getString(R.string.new_page), ignoreCase = true)
            ) {
                ll_create_document?.removeView(curr)
                break
            }
        }
    }

    private fun createRow(
        columnCount: IntArray,
        showDeleteIcon: Boolean,
        tableRows: MutableList<LinearLayout>,
        llTableContainer: LinearLayout,
        ll_column_headers: LinearLayout,
        btnAddColumn: Button
    ): LinearLayout {
        val row = LinearLayout(context)
        row.orientation = LinearLayout.HORIZONTAL
        row.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        row.gravity = Gravity.CENTER_VERTICAL

        for (i in 0 until columnCount[0]) {
            val cell = createCell()
            cell.requestFocus()
            cell.post {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showSoftInput(cell, InputMethodManager.SHOW_IMPLICIT)
            }
            row.addView(cell)
        }

        val deleteIcon = ImageView(context)
        deleteIcon.setImageResource(R.drawable.delete_de)
        deleteIcon.setPadding(10, 10, 10, 10)
        deleteIcon.scaleType = ImageView.ScaleType.CENTER_INSIDE
        deleteIcon.adjustViewBounds = true

        val sizeInDp = 30
        val sizeInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, sizeInDp.toFloat(), requireContext().resources.displayMetrics
        ).toInt()

        val iconParams = LinearLayout.LayoutParams(sizeInPx, sizeInPx)
        iconParams.setMargins(10, 10, 10, 10)
        deleteIcon.layoutParams = iconParams

        deleteIcon.setOnClickListener {
            if (rowHasContent(row)) {
                pendingDeleteType = DeleteTargetType.ROW
                pendingRowLayout = row
                ConfirmPopup("row", columnCount, tableRows, ll_column_headers, llTableContainer, btnAddColumn)
            } else {
                llTableContainer.removeView(row)
                tableRows.remove(row)
                if (tableRows.size == 1) {
                    val lastChild = tableRows[0].getChildAt(tableRows[0].childCount - 1)
                    if (lastChild is ImageView) {
                        lastChild.visibility = View.GONE
                    }
                }
            }
            updateHeaderMarginBasedOnRowDelete(tableRows, ll_column_headers)
        }

        row.addView(deleteIcon)
        deleteIcon.visibility = if (showDeleteIcon) View.VISIBLE else View.GONE
        return row
    }

    private fun createCell(): TextInputEditText {
        val inflater = LayoutInflater.from(requireContext())
        val cellView = inflater.inflate(
            R.layout.title_description_layout, LinearLayout(requireContext()), false
        ) as TextInputEditText
        cellView.requestFocus()
        cellView.post {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(cellView, InputMethodManager.SHOW_IMPLICIT)
        }
        cellView.addTextChangedListener(ContentValidation(cellView, true))
        cellView.maxLines = 1
        val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f)
        params.setMargins(4, 4, 4, 4)
        cellView.layoutParams = params
        cellView.setHint(R.string.enter_your_content)
        cellView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_light_grey_bg))
        return cellView
    }

    private fun updateRowDeleteIcons(tableRows: List<LinearLayout>) {
        for (row in tableRows) {
            val lastChild = row.getChildAt(row.childCount - 1)
            if (lastChild is ImageView) {
                lastChild.visibility = if (tableRows.size > 1) View.VISIBLE else View.GONE
            }
        }
    }

    private fun removeColumnAt(
        indexToRemove: Int,
        llColumnHeaders: LinearLayout,
        tableRows: List<LinearLayout>,
        columnCount: IntArray,
        btnAddColumn: Button
    ) {
        if (indexToRemove < 0) return
        val headerView = llColumnHeaders.getChildAt(indexToRemove)
        if (headerView != null) {
            llColumnHeaders.removeViewAt(indexToRemove)
        }
        for (row in tableRows) {
            if (row.childCount > indexToRemove) {
                row.removeViewAt(indexToRemove)
            }
        }
        for (j in 0 until llColumnHeaders.childCount) {
            llColumnHeaders.getChildAt(j).tag = j
        }
        columnCount[0]--
        updateColumnMinusIconsVisibility(llColumnHeaders, btnAddColumn)
        if (columnCount[0] < 4) {
            btnAddColumn.alpha = 1.0f
            btnAddColumn.isEnabled = true
        }
    }

    private fun rowHasContent(row: LinearLayout): Boolean {
        for (i in 0 until row.childCount) {
            val cell = row.getChildAt(i)
            if (cell is TextInputEditText) {
                val text = cell.text.toString().trim()
                if (text.isNotEmpty()) {
                    return true
                }
            }
        }
        return false
    }

    fun AddTable(title: String, tableDataIn: List<List<String>>?) {
        var tableData = tableDataIn
        val view_added_list = LayoutInflater.from(context).inflate(R.layout.table_layout, null)
        val ll_table_container: LinearLayout = view_added_list.findViewById(R.id.ll_add_more)
        val ll_column_headers: LinearLayout = view_added_list.findViewById(R.id.ll_column_headers)
        val tableRows = ArrayList<LinearLayout>()
        val columnCount = intArrayOf(if (tableData != null && tableData.isNotEmpty()) tableData[0].size else 1)
        val tv_content = view_added_list.findViewById<TextView>(R.id.tv_content)
        val iv_remove = view_added_list.findViewById<ImageView>(R.id.iv_remove)
        val btn_addColumn = view_added_list.findViewById<Button>(R.id.btn_addColumn)
        val btn_addRow = view_added_list.findViewById<Button>(R.id.btn_addRow)

        btn_addRow.setText(R.string.add_row)
        btn_addColumn.setText(R.string.add_column)

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 20)
        view_added_list.layoutParams = params

        tableRows.clear()
        tv_content.text = title
        tv_content.tag = title
        if (tableData == null) {
            tableData = ArrayList()
            val emptyRow = ArrayList<String>()
            emptyRow.add("")
            tableData.add(emptyRow)
        }
        if (tableData.isNotEmpty()) {
            columnCount[0] = tableData[0].size
        }

        for (i in 0 until columnCount[0]) {
            val headerCell = LinearLayout(context)
            headerCell.orientation = LinearLayout.HORIZONTAL
            headerCell.gravity = Gravity.END
            val headerParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f)
            headerParams.setMargins(4, 4, 4, 4)
            headerCell.layoutParams = headerParams

            val ivMinus = ImageView(context)
            ivMinus.setImageResource(R.drawable.minus_large_icon)
            ivMinus.adjustViewBounds = true

            val sizeInDp = 20
            val sizeInPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeInDp.toFloat(), requireContext().resources.displayMetrics
            ).toInt()

            val minusParams = LinearLayout.LayoutParams(sizeInPx, sizeInPx)
            ivMinus.layoutParams = minusParams
            ivMinus.tag = i

            ivMinus.setOnClickListener { colView ->
                val indexToRemove = colView.tag as Int
                var hasContent = false
                for (row in tableRows) {
                    if (row.childCount > indexToRemove) {
                        val cell = row.getChildAt(indexToRemove)
                        if (cell is TextInputEditText) {
                            val text = cell.text.toString().trim()
                            if (text.isNotEmpty()) {
                                hasContent = true
                                break
                            }
                        }
                    }
                }
                if (hasContent) {
                    pendingDeleteType = DeleteTargetType.COLUMN
                    pendingDeleteView = colView
                    pendingColumnIndex = indexToRemove
                    ConfirmPopup("column", columnCount, tableRows, ll_column_headers, ll_table_container, btn_addColumn)
                } else {
                    removeColumnAt(indexToRemove, ll_column_headers, tableRows, columnCount, btn_addColumn)
                }
            }

            headerCell.addView(ivMinus)
            ll_column_headers.addView(headerCell)
        }

        for (r in tableData.indices) {
            val rowData = tableData[r]
            val row = LinearLayout(context)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            row.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            for (c in rowData.indices) {
                val cell = createCell()
                cell.setText(rowData[c])
                row.addView(cell)
            }

            val deleteIcon = ImageView(context)
            deleteIcon.setImageResource(R.drawable.delete_de)
            deleteIcon.setPadding(10, 10, 10, 10)
            deleteIcon.scaleType = ImageView.ScaleType.CENTER_INSIDE
            deleteIcon.adjustViewBounds = true

            val sizeInDp = 30
            val sizeInPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeInDp.toFloat(), requireContext().resources.displayMetrics
            ).toInt()

            val iconParams = LinearLayout.LayoutParams(sizeInPx, sizeInPx)
            iconParams.setMargins(10, 10, 10, 10)
            deleteIcon.layoutParams = iconParams

            deleteIcon.setOnClickListener {
                if (rowHasContent(row)) {
                    pendingDeleteType = DeleteTargetType.ROW
                    pendingRowLayout = row
                    ConfirmPopup("row", columnCount, tableRows, ll_column_headers, ll_table_container, btn_addColumn)
                } else {
                    ll_table_container.removeView(row)
                    tableRows.remove(row)
                    if (tableRows.size == 1) {
                        val lastChild = tableRows[0].getChildAt(tableRows[0].childCount - 1)
                        if (lastChild is ImageView) {
                            lastChild.visibility = View.GONE
                        }
                    }
                }
                updateHeaderMarginBasedOnRowDelete(tableRows, ll_column_headers)
            }

            row.addView(deleteIcon)
            tableRows.add(row)
            ll_table_container.addView(row)
        }

        if (tableData.isNotEmpty()) {
            updateHeaderMarginBasedOnRowDelete(tableRows, ll_column_headers)
        }
        updateRowDeleteIcons(tableRows)
        updateColumnMinusIconsVisibility(ll_column_headers, btn_addColumn)

        iv_remove.setOnClickListener {
            pendingDeleteType = DeleteTargetType.TABLE
            pendingDeleteView = view_added_list
            ConfirmPopup("table", columnCount, tableRows, ll_column_headers, ll_table_container, btn_addColumn)
        }

        btn_addRow.setOnClickListener {
            val newRow = createRow(columnCount, true, tableRows, ll_table_container, ll_column_headers, btn_addColumn)
            tableRows.add(newRow)
            ll_table_container.addView(newRow)
            updateRowDeleteIcons(tableRows)
            updateColumnMinusIconsVisibility(ll_column_headers, btn_addColumn)
            btn_addColumn.isClickable = true
            updateAddColumnButtonState(btn_addColumn, columnCount[0])
            updateHeaderMarginBasedOnRowDelete(tableRows, ll_column_headers)
        }

        btn_addColumn.setOnClickListener {
            updateAddColumnButtonState(btn_addColumn, columnCount[0])
            val newColumnIndex = columnCount[0]
            columnCount[0]++

            val headerCell = LinearLayout(context)
            headerCell.orientation = LinearLayout.HORIZONTAL
            headerCell.gravity = Gravity.END
            val headerParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f)
            headerParams.setMargins(4, 4, 4, 4)
            headerCell.layoutParams = headerParams

            val newMinus = ImageView(context)
            newMinus.setImageResource(R.drawable.minus_large_icon)
            newMinus.adjustViewBounds = true

            val sizeInDp = 20
            val sizeInPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeInDp.toFloat(), requireContext().resources.displayMetrics
            ).toInt()

            val minusParams = LinearLayout.LayoutParams(sizeInPx, sizeInPx)
            newMinus.layoutParams = minusParams
            newMinus.tag = newColumnIndex

            newMinus.setOnClickListener { colView ->
                val indexToRemove = colView.tag as Int
                var hasContent = false
                for (row in tableRows) {
                    if (row.childCount > indexToRemove) {
                        val cell = row.getChildAt(indexToRemove)
                        if (cell is TextInputEditText) {
                            val text = cell.text.toString().trim()
                            if (text.isNotEmpty()) {
                                hasContent = true
                                break
                            }
                        }
                    }
                }
                if (hasContent) {
                    pendingDeleteType = DeleteTargetType.COLUMN
                    pendingDeleteView = colView
                    pendingColumnIndex = indexToRemove
                    ConfirmPopup("column", columnCount, tableRows, ll_column_headers, ll_table_container, btn_addColumn)
                } else {
                    removeColumnAt(indexToRemove, ll_column_headers, tableRows, columnCount, btn_addColumn)
                    if (columnCount[0] < 4) {
                        btn_addColumn.alpha = 1.0f
                        btn_addColumn.isEnabled = true
                    }
                }
            }

            headerCell.addView(newMinus)
            ll_column_headers.addView(headerCell)

            for (row in tableRows) {
                val newCell = createCell()
                row.addView(newCell, row.childCount - 1)
            }

            updateColumnMinusIconsVisibility(ll_column_headers, btn_addColumn)
            updateAddColumnButtonState(btn_addColumn, columnCount[0])
            updateHeaderMarginBasedOnRowDelete(tableRows, ll_column_headers)
        }

        ll_create_document?.addView(view_added_list)
        updateAddColumnButtonState(btn_addColumn, columnCount[0])
        btn_addRow.parent?.requestChildFocus(btn_addRow, btn_addColumn)
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    private fun updateHeaderMarginBasedOnRowDelete(tableRows: List<LinearLayout>, llColumnHeaders: LinearLayout) {
        var anyDeleteVisible = false
        for (row in tableRows) {
            val lastChild = row.getChildAt(row.childCount - 1)
            if (lastChild is ImageView && lastChild.visibility == View.VISIBLE) {
                anyDeleteVisible = true
                break
            }
        }

        val lp = llColumnHeaders.layoutParams as ViewGroup.MarginLayoutParams
        val endMarginPx = dpToPx(if (anyDeleteVisible) 35 else 4)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            lp.marginEnd = endMarginPx
        }
        lp.rightMargin = endMarginPx
        llColumnHeaders.layoutParams = lp
        llColumnHeaders.requestLayout()
    }

    fun ConfirmPopup(
        FieldName: String,
        columnCount: IntArray,
        tableRows: MutableList<LinearLayout>,
        ll_column_headers: LinearLayout,
        llTableContainer: LinearLayout,
        btn_addColumn: Button
    ) {
        try {
            val dialogBuilder = AlertDialog.Builder(requireActivity())
            ll_create_view?.alpha = 0.5f
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.delete_relationship, null)
            val header_name = view.findViewById<TextView>(R.id.header_name)
            header_name.setText(R.string.confirmation)
            val close_documents = view.findViewById<ImageView>(R.id.close_documents)
            header_name.setTextColor(Color.BLACK)
            val tv_confirmation = view.findViewById<TextView>(R.id.tv_confirmation)
            val delete_msg = "Are you sure you want to remove this $FieldName?"
            tv_confirmation.text = delete_msg
            val bt_yes = view.findViewById<AppCompatButton>(R.id.btn_yes)
            val btn_no = view.findViewById<AppCompatButton>(R.id.btn_No)
            val dialog = dialogBuilder.create()
            btn_no.setOnClickListener {
                dialog.dismiss()
                ll_create_view?.alpha = 1.0f
            }
            close_documents.setOnClickListener {
                dialog.dismiss()
                ll_create_view?.alpha = 1.0f
            }
            bt_yes.setOnClickListener {
                dialog.dismiss()
                ll_create_view?.alpha = 1.0f
                if (FieldName == "table") {
                    ll_create_document?.removeView(pendingDeleteView)
                    tableRows.clear()
                    columnCount[0] = 0
                    checkConsecutivePageBreaks()
                } else if (FieldName == "image") {
                    if (ImageLayout != null) {
                        ll_create_document?.removeView(ImageLayout)
                        ImageLayout = null
                        checkConsecutivePageBreaks()
                    }
                } else {
                    if (pendingDeleteType == DeleteTargetType.ROW && pendingRowLayout != null) {
                        llTableContainer.removeView(pendingRowLayout)
                        tableRows.remove(pendingRowLayout)
                        updateRowDeleteIcons(tableRows)
                        updateHeaderMarginBasedOnRowDelete(tableRows, ll_column_headers)
                    } else if (pendingDeleteType == DeleteTargetType.COLUMN &&
                        pendingDeleteView != null && pendingColumnIndex != -1
                    ) {
                        ll_column_headers.removeView(pendingDeleteView)
                        columnCount[0]--
                        for (row in tableRows) {
                            if (row.childCount > pendingColumnIndex) {
                                row.removeViewAt(pendingColumnIndex)
                            }
                        }
                        for (j in 0 until ll_column_headers.childCount) {
                            ll_column_headers.getChildAt(j).tag = j
                        }
                        updateColumnMinusIconsVisibility(ll_column_headers, btn_addColumn)
                        resetDeleteState()
                        updateHeaderMarginBasedOnRowDelete(tableRows, ll_column_headers)
                    }
                }
            }
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setView(view)
            dialog.show()
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, requireActivity())
        }
    }

    private fun updateAddColumnButtonState(btn_addColumn: Button, currentColumnCount: Int) {
        if (currentColumnCount >= 4) {
            btn_addColumn.alpha = 0.5f
            btn_addColumn.isEnabled = false
        } else {
            btn_addColumn.alpha = 1.0f
            btn_addColumn.isEnabled = true
        }
    }

    fun ConfirmPopup(FieldName: String) {
        try {
            val dialogBuilder = AlertDialog.Builder(requireActivity())
            ll_create_view?.alpha = 0.5f
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.delete_relationship, null)
            val header_name = view.findViewById<TextView>(R.id.header_name)
            header_name.setText(R.string.confirmation)
            val close_documents = view.findViewById<ImageView>(R.id.close_documents)
            header_name.setTextColor(Color.BLACK)
            val tv_confirmation = view.findViewById<TextView>(R.id.tv_confirmation)
            val delete_msg = "Are you sure you want to remove this $FieldName?"
            tv_confirmation.text = delete_msg
            val bt_yes = view.findViewById<AppCompatButton>(R.id.btn_yes)
            val btn_no = view.findViewById<AppCompatButton>(R.id.btn_No)

            val dialog = dialogBuilder.create()
            btn_no.setOnClickListener {
                dialog.dismiss()
                ll_create_view?.alpha = 1.0f
            }
            close_documents.setOnClickListener {
                dialog.dismiss()
                ll_create_view?.alpha = 1.0f
            }
            bt_yes.setOnClickListener {
                dialog.dismiss()
                ll_create_view?.alpha = 1.0f
                if (FieldName == "image") {
                    if (ImageLayout != null) {
                        ll_create_document?.removeView(ImageLayout)
                        ImageLayout = null
                        checkConsecutivePageBreaks()
                    }
                }
            }
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setView(view)
            dialog.show()
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, requireActivity())
        }
    }

    private fun updateColumnMinusIconsVisibility(ll_column_headers: LinearLayout, btn_addColumn: Button?) {
        val shouldShow = ll_column_headers.childCount > 1
        for (i in 0 until ll_column_headers.childCount) {
            val icon = ll_column_headers.getChildAt(i)
            icon.visibility = if (shouldShow) View.VISIBLE else View.GONE
        }
        val currentColumns = ll_column_headers.childCount
        if (btn_addColumn != null) {
            btn_addColumn.isEnabled = currentColumns < MAX_COLUMNS
            btn_addColumn.alpha = if (currentColumns < MAX_COLUMNS) 1.0f else 0.5f
        }
    }

    private fun resetDeleteState() {
        pendingDeleteType = null
        pendingDeleteView = null
        pendingColumnIndex = -1
        pendingRowLayout = null
    }

    fun AddImage(title: String, imagePath: String?, caption: String) {
        val view_added_list = LayoutInflater.from(context).inflate(R.layout.image_layout, null)
        val tv_content = view_added_list.findViewById<TextView>(R.id.tv_content)
        val iv_remove = view_added_list.findViewById<ImageView>(R.id.iv_remove)
        val iv_expand = view_added_list.findViewById<ImageView>(R.id.iv_expand)
        val btn_browse = view_added_list.findViewById<Button>(R.id.btn_browse)
        val tv_max_File = view_added_list.findViewById<TextView>(R.id.tv_max_File)
        ll_image_view = view_added_list.findViewById(R.id.ll_image_view)
        tv_image_name = ll_image_view?.findViewById(R.id.tv_image_name)
        tv_image_name?.text = ""
        tv_content.text = title
        tv_content.tag = caption
        tv_max_File.setText(R.string.max_file_size_2_mb)
        btn_browse.setText(R.string.browse_small)

        if (imagePath != null && imagePath.isNotEmpty()) {
            ll_image_view?.visibility = View.VISIBLE
            tv_image_name?.text = imagePath
            tv_image_name?.tag = imagePath
        } else {
            ll_image_view?.visibility = View.GONE
        }

        iv_remove.setOnClickListener {
            ImageLayout = view_added_list
            if (tv_image_name != null && tv_image_name!!.text.toString().isNotEmpty()) {
                ConfirmPopup("image")
            } else {
                ll_create_document?.removeView(view_added_list)
                checkConsecutivePageBreaks()
            }
        }
        iv_expand.setOnClickListener {
            val tv_current_title = view_added_list.findViewById<TextView>(R.id.tv_content)
            val fullTitle = tv_current_title?.text?.toString() ?: ""
            val customTitle = if (fullTitle.startsWith("Image - ")) fullTitle.substring("Image - ".length) else ""
            loadExpandView("Image", customTitle, "", view_added_list, true, false)
        }
        btn_browse.setOnClickListener {
            tempTvImageName = view_added_list.findViewById(R.id.tv_image_name)
            tempLlImageView = view_added_list.findViewById(R.id.ll_image_view)
            tempImageView = view_added_list.findViewById(R.id.imageView)
            checkPermissionREAD_EXTERNAL_STORAGE(requireContext())
        }

        val insertIndex = ll_create_document!!.childCount
        ll_create_document?.addView(view_added_list, insertIndex)
        view_added_list.isFocusable = true
        view_added_list.isFocusableInTouchMode = true
        view_added_list.requestFocus()
        view_added_list.post {
            view_added_list.parent?.requestChildFocus(view_added_list, view_added_list)
        }
    }

    private val requestPermissions: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            var permissionGranted = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                permissionGranted = results[android.Manifest.permission.READ_MEDIA_IMAGES] == true ||
                        results[android.Manifest.permission.READ_MEDIA_VIDEO] == true ||
                        results[android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] == true
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionGranted = results[android.Manifest.permission.READ_MEDIA_IMAGES] == true ||
                        results[android.Manifest.permission.READ_MEDIA_VIDEO] == true
            } else {
                permissionGranted = results[android.Manifest.permission.READ_EXTERNAL_STORAGE] == true
            }

            if (permissionGranted) {
                BottomSheetUploadfile()
            }
        }

    fun checkPermissionREAD_EXTERNAL_STORAGE(context: Context?): Boolean {
        if (context == null) return false
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions.launch(
                        arrayOf(
                            android.Manifest.permission.READ_MEDIA_IMAGES,
                            android.Manifest.permission.READ_MEDIA_VIDEO,
                            android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                        )
                    )
                } else {
                    BottomSheetUploadfile()
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions.launch(
                        arrayOf(
                            android.Manifest.permission.READ_MEDIA_IMAGES,
                            android.Manifest.permission.READ_MEDIA_VIDEO
                        )
                    )
                } else {
                    BottomSheetUploadfile()
                }
            } else {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions.launch(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))
                } else {
                    BottomSheetUploadfile()
                }
            }
            return false
        } else {
            return true
        }
    }

    fun validateFieldsFromLayout(ll_create_document: LinearLayout): Boolean {
        val staticViewsCount = 5
        for (i in staticViewsCount until ll_create_document.childCount) {
            val view = ll_create_document.getChildAt(i)
            val tvLabel = view.findViewById<TextView>(R.id.tv_content) ?: continue
            val tagName = tvLabel.tag?.toString()?.trim() ?: ""
            val userLabel = tvLabel.text.toString().trim()
            val labelForError = if (userLabel.equals(tagName, ignoreCase = true) || userLabel.isEmpty()) tagName else tagName

            if (tagName.equals("Overview", ignoreCase = true) ||
                tagName.equals("Section", ignoreCase = true) ||
                tagName.equals("Sub Section", ignoreCase = true) ||
                tagName.equals("Sub Sub Section", ignoreCase = true) ||
                tagName.equals("Paragraph", ignoreCase = true)
            ) {
                val etContent = view.findViewById<TextInputEditText>(R.id.et_content)
                if (etContent == null || etContent.text.toString().trim().isEmpty()) {
                    AndroidUtils.showError("$labelForError is mandatory. Please add the text.", requireActivity())
                    return false
                }
            } else if (tagName.equals("Table", ignoreCase = true)) {
                val tableContainer = view.findViewById<LinearLayout>(R.id.ll_add_more)
                var isAnyCellFilled = false

                for (rowIndex in 0 until tableContainer.childCount) {
                    val rowView = tableContainer.getChildAt(rowIndex)
                    if (rowView !is LinearLayout) continue
                    for (cellIndex in 0 until rowView.childCount) {
                        val cell = rowView.getChildAt(cellIndex)
                        if (cell is TextInputEditText) {
                            val text = cell.text.toString().trim()
                            if (text.isNotEmpty()) {
                                isAnyCellFilled = true
                                break
                            }
                        }
                    }
                    if (isAnyCellFilled) break
                }
                if (!isAnyCellFilled) {
                    AndroidUtils.showError("$labelForError column values are mandatory. Please add value.", requireActivity())
                    return false
                }
            } else if (tagName.equals("Image", ignoreCase = true)) {
                val tvImageName = view.findViewById<TextView>(R.id.tv_image_name)
                if (tvImageName == null || tvImageName.text.toString().trim().isEmpty()) {
                    AndroidUtils.showError("$labelForError is mandatory. Please add the image.", requireActivity())
                    return false
                }
            } else if (tagName.equals("Numbered List", ignoreCase = true) || tagName.equals("Bulleted List", ignoreCase = true)) {
                val listContainer = view.findViewById<LinearLayout>(R.id.ll_add_content)
                for (j in 0 until listContainer.childCount) {
                    val itemView = listContainer.getChildAt(j)
                    if (itemView != null) {
                        val etItem = itemView.findViewById<TextInputEditText>(R.id.et_list_item)
                        if (etItem != null && etItem.text.toString().trim().isEmpty()) {
                            AndroidUtils.showError("$labelForError is mandatory. All list items must be filled.", requireActivity())
                            return false
                        }
                    }
                }
            }
        }
        return true
    }

    private fun BottomSheetUploadfile() {
        Constants.isDocEditor = true
        ll_create_view?.alpha = 0.5f
        bottommSheetUploadDocument = BottomSheetUploadFile(ll_create_view, false)
        bottommSheetUploadDocument?.show(parentFragmentManager, "")
        bottommSheetUploadDocument?.setTargetFragment(this, 1)
    }

    fun triggerSavePayload(docId: String) {
        try {
            val fieldList = LaTeXUtils.collectFieldContentsFromLayout(requireContext(), ll_create_document!!).toMutableList()
            val title = et_title?.text.toString().trim()
            val author = et_author?.text.toString().trim()

            val titleModel = FieldContentModel()
            titleModel.fieldname = Constants.title
            titleModel.contentText = title

            val authorModel = FieldContentModel()
            authorModel.fieldname = Constants.author
            authorModel.contentText = author

            var hasOverview = false
            for (field in fieldList) {
                if (Constants.overview == field.fieldname) {
                    hasOverview = true
                    break
                }
            }

            if (!hasOverview) {
                for (i in 0 until ll_create_document!!.childCount) {
                    val view = ll_create_document!!.getChildAt(i)
                    val tvContentTitle = view.findViewById<TextView>(R.id.tv_content)
                    val etContent = view.findViewById<TextInputEditText>(R.id.et_content)

                    if (tvContentTitle != null && etContent != null) {
                        val tagObj = tvContentTitle.tag
                        if (tagObj != null && "Overview".equals(tagObj.toString().trim(), ignoreCase = true)) {
                            val overviewModel = FieldContentModel()
                            overviewModel.fieldname = Constants.overview
                            overviewModel.contentText = etContent.text.toString().trim()
                            fieldList.add(overviewModel)
                            break
                        }
                    }
                }
            }

            for (model in fieldList) {
                if (model.fieldname == Constants.docsection) {
                    model.title = extractCustomTitle(model.title, "Section - ")
                } else if (model.fieldname == Constants.subSection) {
                    model.title = extractCustomTitle(model.title, "Sub Section - ")
                } else if (model.fieldname == Constants.subSubSection) {
                    model.title = extractCustomTitle(model.title, "Sub Sub Section - ")
                } else if (model.fieldname != null && model.fieldname!!.contains(Constants.image)) {
                    model.title = extractCustomTitle(model.title, "Image - ")
                } else if (model.fieldname == Constants.paragraph) {
                    model.title = extractCustomTitle(model.title, "Paragraph - ")
                }
            }

            fieldList.add(0, authorModel)
            fieldList.add(0, titleModel)

            val userId = Constants.USER_ID ?: ""
            val docString = DocumentPayloadBuilder.createLatexPayload(fieldList, userId)
            sendPostPayload(requireContext(), docString, 1, docId)
        } catch (e: Exception) {
            AndroidUtils.showError("Failed to save document: " + e.message, requireActivity())
            e.printStackTrace()
        }
    }

    private fun hasValidTitle(title: String?): Boolean {
        return title != null && title.trim().isNotEmpty() && !"null".equals(title.trim(), ignoreCase = true)
    }

    fun loadNewDoc() {
        if (oldDocId.isNotEmpty()) {
            clearViews()
            clearNames()
        } else {
            val childCount = ll_create_document!!.childCount
            if (childCount <= 5) {
                AndroidUtils.showError("Please add atleast one segment from the 'insert' menu to create the document", requireActivity())
            } else {
                AndroidUtils.showConfirmation(
                    requireActivity(),
                    requireContext().getString(R.string.confirmation_),
                    requireContext().getString(R.string.changes_you_made_may_not_be_saved_do_you_want_to_save),
                    requireContext().getString(R.string.yes),
                    object : AndroidUtils.OnConfirmListener {
                        override fun onSave() {
                            SaveFile()
                        }

                        override fun onCancel() {
                            clearViews()
                            clearNames()
                        }
                    }
                )
            }
        }
    }

    fun clearNames() {
        oldDocName = ""
        tv_doc_name?.setText(R.string.untitled_document)
        latexDocumentResponse.createdon = ""
        latexDocumentResponse.document = ""
        latexDocumentResponse.docid = ""
        latexDocumentResponse.page = 0
        latexDocumentResponse.pageid = ""
        latexDocumentResponse.updatedon = ""
        latexDocumentResponse.userid = ""
    }

    private fun extractCustomTitle(title: String?, defaultPrefix: String): String? {
        if (title == null || title.trim().isEmpty()) return null
        val trimmed = title.trim()
        if (trimmed.startsWith(defaultPrefix)) {
            val customPart = trimmed.substring(defaultPrefix.length).trim()
            return if (customPart.isEmpty()) null else customPart
        }
        return trimmed
    }

    fun clearViews() {
        for (i in ll_create_document!!.childCount - 1 downTo 5) {
            ll_create_document?.removeViewAt(i)
        }
        oldDocId = ""
        et_title?.setText("")
        et_author?.setText("")
    }

    fun sendPostPayload(context: Context?, documentText: String, page: Int, docId: String) {
        try {
            val payload = JSONObject()
            payload.put("document", documentText)
            payload.put("page", page)

            val url = (Constants.saveLatexDoc ?: "") + docId
            if (latexDocumentResponse.pageid != null && latexDocumentResponse.pageid!!.isNotEmpty()) {
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.PATCH,
                    url,
                    "Save DocEditor",
                    payload.toString()
                )
            } else {
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.POST,
                    url,
                    "Save DocEditor",
                    payload.toString()
                )
            }
            Log.d("Payload", payload.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
            AndroidUtils.showError("Payload error: " + e.message, requireActivity())
        }
    }

    fun loadOpenDocument() {
        if (oldDocId.isNotEmpty()) {
            loadDocumentView()
        } else {
            val childCount = ll_create_document!!.childCount
            if (childCount <= 5) {
                loadDocumentView()
            } else {
                AndroidUtils.showConfirmation(
                    requireActivity(),
                    requireContext().getString(R.string.confirmation_),
                    requireContext().getString(R.string.changes_you_made_may_not_be_saved_do_you_want_to_save),
                    requireContext().getString(R.string.yes),
                    object : AndroidUtils.OnConfirmListener {
                        override fun onSave() {
                            SaveFile()
                        }

                        override fun onCancel() {
                            loadDocumentView()
                        }
                    }
                )
            }
        }
    }

    private fun loadDocumentView() {
        clearViews()
        clearNames()
        val dialogBuilder = AlertDialog.Builder(requireActivity())
        ll_create_view?.alpha = 0.5f
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.open_document_layout, activity?.findViewById<ViewGroup>(android.R.id.content), false)

        val tv_content = view.findViewById<TextView>(R.id.tv_content)
        tv_content.setText(R.string.open_document)

        et_search = view.findViewById(R.id.et_search)
        et_search?.setHint(R.string.search_document)
        et_search?.addTextChangedListener(Validation(et_search!!))
        rv_open_document = view.findViewById(R.id.rv_open_document)
        val iv_remove = view.findViewById<ImageView>(R.id.iv_remove)

        dialogBuilder.setView(view)
        val dialog = dialogBuilder.create()
        dialog1 = dialog
        dialog.setOnDismissListener {
            ll_create_view?.alpha = 1.0f
        }
        iv_remove.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()

        val window = dialog.window
        if (window != null) {
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val scale = requireActivity().resources.displayMetrics.density
            val horizontalMarginInDp = 20
            val verticalMarginInDp = 100

            val marginHorizontalPx = (horizontalMarginInDp * scale + 0.5f).toInt()
            val marginVerticalPx = (verticalMarginInDp * scale + 0.5f).toInt()

            val screenWidth = requireActivity().resources.displayMetrics.widthPixels
            val screenHeight = requireActivity().resources.displayMetrics.heightPixels

            val dialogWidth = screenWidth - (2 * marginHorizontalPx)
            val dialogHeight = screenHeight - (2 * marginVerticalPx)

            window.setLayout(dialogWidth, dialogHeight)
            val layoutParams = window.attributes
            layoutParams.gravity = Gravity.CENTER
            window.attributes = layoutParams
        }
        loadDocumentList()
    }

    private fun CloseView() {
        ll_create_view?.alpha = 1.0f
        dialog1?.dismiss()
    }

    private fun loadRecyclerView(recyclerView: RecyclerView) {
        if (isView) {
            recyclerView.layoutManager = LinearLayoutManager(context)
            et_search_document?.let { loadSearch(it) }
        } else {
            recyclerView.layoutManager = GridLayoutManager(context, 2)
            et_search?.let { loadSearch(it) }
        }
        adapter = ViewDocAdapter(requireContext(), view_doc_list, this, this, isView)
        recyclerView.adapter = adapter
    }

    fun loadDocumentFromPayload(latexPayload: String?) {
        val childCount = ll_create_document!!.childCount
        if (childCount > 5) {
            clearViews()
        }
        if (latexPayload == null || latexPayload.trim().isEmpty()) return
        var payload = latexPayload
        if (payload.contains("<ltk>")) {
            payload = payload.substring(payload.indexOf("<ltk>"))
        }

        val blocks = payload.split("<ltk>").toTypedArray()
        for (bBlock in blocks) {
            val block = bBlock.trim()
            if (block.isEmpty()) continue
            if (block.startsWith("\\documentclass") || block.startsWith("\\usepackage") ||
                block.startsWith("\\geometry") || block.startsWith("\\begin{document}") ||
                block.startsWith("\\maketitle") || block.startsWith("\\date{")
            ) {
                continue
            }
            if (oldDocName.isNotEmpty()) {
                tv_doc_name?.text = oldDocName
            }

            if (block.startsWith("\\title") && block.contains("{")) {
                var title = Utility_Methods.extractBetween(block, "\\title", "}")
                if (title != null) {
                    title = title.replaceFirst("^\\s*\\{".toRegex(), "").trim()
                    et_title?.setText(title)
                }
                continue
            }
            if (block.startsWith("\\author{")) {
                val author = Utility_Methods.extractBetween(block, "\\author{", "}")
                et_author?.setText(author)
                continue
            }
            if (block.startsWith("\\abstract")) {
                val content = block.replaceFirst("\\\\abstract".toRegex(), "").trim()
                AddView("Overview", content)
                continue
            }
            if (block.startsWith("\\section{")) {
                var label = Utility_Methods.extractBetween(block, "\\section{", "}")
                val displayLabel = if (label == null || label.trim().isEmpty()) {
                    label = "Section"
                    "Section"
                } else {
                    "Section - ${label.trim()}"
                }
                val content = block.replaceFirst("\\\\section\\{.*?\\}".toRegex(), "").trim()
                AddView("Section", "[[title]]" + displayLabel + "\n" + content)
                continue
            }
            if (block.startsWith("\\subsection{")) {
                var label = Utility_Methods.extractBetween(block, "\\subsection{", "}")
                val displayLabel = if (label == null || label.trim().isEmpty()) {
                    label = "Sub Section"
                    "Sub Section"
                } else {
                    "Sub Section - ${label.trim()}"
                }
                val content = block.replaceFirst("\\\\subsection\\{.*?\\}".toRegex(), "").trim()
                AddView("Sub Section", "[[title]]" + displayLabel + "\n" + content)
                continue
            }
            if (block.startsWith("\\subsubsection{")) {
                var label = Utility_Methods.extractBetween(block, "\\subsubsection{", "}")
                val displayLabel = if (label == null || label.trim().isEmpty()) {
                    label = "Sub Sub Section"
                    "Sub Sub Section"
                } else {
                    "Sub Sub Section - ${label.trim()}"
                }
                val content = block.replaceFirst("\\\\subsubsection\\{.*?\\}".toRegex(), "").trim()
                AddView("Sub Sub Section", "[[title]]" + displayLabel + "\n" + content)
                continue
            }
            if (block.startsWith("\\newpage")) {
                AddPageBreak("Page Break")
                continue
            }
            if (block.startsWith("\\begin{enumerate}")) {
                val items = Utility_Methods.extractListItems(block)
                AddDocListView("Numbered List", items.toMutableList())
                continue
            }
            if (block.startsWith("\\begin{itemize}")) {
                val items = Utility_Methods.extractListItems(block)
                AddDocListView("Bulleted List", items.toMutableList())
                continue
            }
            if (block.contains("\\begin{tabularx}")) {
                val table = Utility_Methods.extractTableData(block)
                AddTable("Table", table)
                continue
            }
            if (block.contains("\\includegraphics")) {
                val imagePath = Utility_Methods.extractImagePath(block)
                var caption = Utility_Methods.extractCaption(block)
                val displayLabel = if (caption == null || caption.trim().isEmpty() || caption == "Image") {
                    caption = "Image"
                    "Image"
                } else {
                    "Image - ${caption.trim()}"
                }
                if (caption.trim().isEmpty()) caption = "Image"
                val fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1)
                AddImage(displayLabel, fileName, caption)
                continue
            }
            if (block.startsWith("\\paragraph{")) {
                var label = Utility_Methods.extractBetween(block, "\\paragraph{", "}")
                val displayLabel = if (label == null || label.trim().isEmpty()) {
                    label = "Paragraph"
                    "Paragraph"
                } else {
                    "Paragraph - ${label.trim()}"
                }
                val content = block.replaceFirst("\\\\paragraph\\{.*?\\}".toRegex(), "").trim()
                AddView("Paragraph", "[[title]]" + displayLabel + "\n" + content)
                continue
            }
        }
    }

    fun SaveFile() {
        if (!Constants.is_active) {
            AndroidUtils.showRenewalPopup(requireActivity())
        } else {
            if (validateFieldsFromLayout(ll_create_document!!)) {
                if (latexDocumentResponse.pageid != null && latexDocumentResponse.pageid!!.isNotEmpty()) {
                    val staticViewsCount = 5
                    val childCount = ll_create_document!!.childCount
                    if (childCount == staticViewsCount) {
                        AndroidUtils.showError("Please add atleast one segment from the 'insert' menu to create the document", requireActivity())
                    } else {
                        triggerSavePayload(latexDocumentResponse.pageid!!)
                    }
                } else {
                    loadCreateFile(false)
                }
            }
        }
    }

    fun SaveAsFile() {
        if (!Constants.is_active) {
            AndroidUtils.showRenewalPopup(requireActivity())
        } else {
            if (latexDocumentResponse.docid != null && latexDocumentResponse.docid!!.isNotEmpty()) {
                loadCreateFile(true)
            } else {
                AndroidUtils.showError("Please save changes before making a copy", requireActivity())
            }
        }
    }

    fun DeleteFile() {
        if (latexDocumentResponse.docid != null && latexDocumentResponse.docid!!.isNotEmpty()) {
            DeleteDoc(oldDocName, oldDocId)
        } else {
            AndroidUtils.showError("Please select the document", requireActivity())
        }
    }

    fun loadCreateFile(isSaveAs: Boolean) {
        if (!Constants.is_active) {
            AndroidUtils.showRenewalPopup(requireActivity())
        } else {
            val staticViewsCount = 5
            val childCount = ll_create_document!!.childCount

            if (childCount == staticViewsCount) {
                AndroidUtils.showError("Please add atleast one segment from the 'insert' menu to create the document", requireActivity())
            } else {
                val dialogBuilder = AlertDialog.Builder(requireActivity())
                ll_create_view?.alpha = 0.5f
                val inflater = requireActivity().layoutInflater
                val view = inflater.inflate(R.layout.file_name_layout_de, null)
                val tv_file_name = view.findViewById<TextView>(R.id.tv_file_name)
                val et_file_name = view.findViewById<TextInputEditText>(R.id.et_file_name)
                val btn_save_file = view.findViewById<Button>(R.id.btn_save_file)
                val iv_cancel_icon = view.findViewById<ImageView>(R.id.iv_cancel_icon)
                val tv_error_msg = view.findViewById<TextView>(R.id.tv_error_msg)
                tv_error_msg.setTextColor(Color.RED)
                tv_error_msg.setText(R.string.filename_is_required)
                tv_file_name.setText(R.string.file_name)
                et_file_name.setHint(R.string.enter_document_name)
                et_file_name.addTextChangedListener(ContentValidation(et_file_name, true))
                filters1 = arrayOf(InputFilter.LengthFilter(25))
                et_file_name.filters = filters1
                val dialog = dialogBuilder.create()
                this.dialog = dialog

                dialog.setOnDismissListener {
                    ll_create_view?.alpha = 1.0f
                }
                et_file_name.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        if (s.length < 1) {
                            tv_error_msg.visibility = View.VISIBLE
                        } else {
                            tv_error_msg.visibility = View.GONE
                        }
                    }
                    override fun afterTextChanged(s: Editable) {}
                })
                btn_save_file.setOnClickListener {
                    if (et_file_name.text.toString().isEmpty()) {
                        tv_error_msg.visibility = View.VISIBLE
                    } else {
                        if (isSaveAs) {
                            callSaveAsDocFile(et_file_name.text.toString(), oldDocId)
                        } else {
                            callSaveDocFile(et_file_name.text.toString())
                        }
                    }
                }
                iv_cancel_icon.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.setView(view)
                dialog.show()

                val window = dialog.window
                if (window != null) {
                    val layoutParams = window.attributes
                    layoutParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    val marginInPx = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 50.0f, resources.displayMetrics
                    ).toInt()
                    layoutParams.y = marginInPx
                    window.attributes = layoutParams
                }
            }
        }
    }

    fun callSaveDocFile(DocName: String) {
        try {
            val payload = JSONObject()
            payload.put("documentname", DocName)
            oldDocName = DocName
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.POST,
                Constants.LatexDocFile ?: "",
                "Save Document",
                payload.toString()
            )
        } catch (e: JSONException) {
            e.printStackTrace()
            AndroidUtils.showError("Payload error: " + e.message, requireActivity())
        }
    }

    fun callSaveAsDocFile(DocName: String, docid: String) {
        try {
            val payload = JSONObject()
            payload.put("documentname", DocName)
            NewDocName = DocName
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.POST,
                (Constants.saveAsLatexDoc ?: "") + docid,
                "Save As Document",
                payload.toString()
            )
        } catch (e: JSONException) {
            e.printStackTrace()
            AndroidUtils.showError("Payload error: " + e.message, requireActivity())
        }
    }

    companion object {
        @JvmStatic
        @Throws(IOException::class)
        fun getFile(context: Context, uri: Uri): File {
            val destinationFilename = File(context.filesDir.path + File.separatorChar + queryName(context, uri))
            context.contentResolver.openInputStream(uri).use { ins ->
                if (ins != null) {
                    createFileFromStream(ins, destinationFilename)
                }
            }
            return destinationFilename
        }

        @JvmStatic
        private fun queryName(context: Context, uri: Uri): String {
            val returnCursor = context.contentResolver.query(uri, null, null, null, null)!!
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            val name = returnCursor.getString(nameIndex)
            returnCursor.close()
            return name
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
            }
        }
    }
}
