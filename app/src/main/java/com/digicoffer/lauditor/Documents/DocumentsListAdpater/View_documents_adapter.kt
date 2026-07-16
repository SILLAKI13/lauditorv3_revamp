package com.digicoffer.lauditor.Documents.DocumentsListAdpater

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digicoffer.lauditor.Documents.Models.DocumentsModel
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel
import com.digicoffer.lauditor.Groups.Models.ActionModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.google.android.material.imageview.ShapeableImageView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.ArrayList
import java.util.Date
import java.util.HashMap

class View_documents_adapter(
    var itemsArrayList: ArrayList<ViewDocumentsModel>?,
    var eventlistner: Eventlistner,
    var cContext: Context,
    var is_MergePdfClicked: Boolean,
    var DOCUMENT_TYPE_TAG: String = "",
    private var highlightIds: ArrayList<String>?
) : RecyclerView.Adapter<View_documents_adapter.MyViewHolder>(), Filterable {

    var list_item: ArrayList<ViewDocumentsModel> = itemsArrayList ?: ArrayList()
    private var expandedPosition = -1
    var is_encrypted: Boolean = false
    var tags_list: ArrayList<DocumentsModel> = ArrayList()

    private var currentViewType = VIEW_TYPE_LIST
    private var recyclerView: RecyclerView? = null

    // Single active PopupWindow — only one open at a time
    private var activePopup: PopupWindow? = null

    private val pdfThumbnailCache = HashMap<String, Bitmap>()
    private val pdfLoadingIds = HashMap<String, Boolean>()

    companion object {
        const val VIEW_TYPE_LIST = 0
        const val VIEW_TYPE_GRID = 1
        const val VIEW_TYPE_DELETED = 2
    }

    fun setViewType(viewType: Int) {
        this.currentViewType = viewType
        expandedPosition = -1 // was: dismissActivePopup()
        notifyDataSetChanged()
    }

    fun getCurrentViewType(): Int {
        return currentViewType
    }

    fun setRecyclerView(rv: RecyclerView?) {
        this.recyclerView = rv
        if (rv != null) {
            rv.clipChildren = false
            rv.clipToPadding = false
        }
    }

    /**
     * Dismiss any currently visible popup without leaving ghost space.
     */
    private fun dismissActivePopup() {
        if (activePopup != null && activePopup!!.isShowing) {
            activePopup!!.dismiss()
        }
        activePopup = null
    }

    private fun safeNotifyItemChanged(position: Int) {
        val r = Runnable {
            if (position >= 0 && position < itemCount) {
                notifyItemChanged(position)
            }
        }
        if (recyclerView != null) {
            recyclerView!!.post(r)
        } else {
            Handler(Looper.getMainLooper()).post(r)
        }
    }

    private fun safeNotifyDataSetChanged() {
        val r = Runnable { notifyDataSetChanged() }
        if (recyclerView != null) {
            recyclerView!!.post(r)
        } else {
            Handler(Looper.getMainLooper()).post(r)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (!list_item.isEmpty()) {
                    if (charString.isEmpty()) {
                        itemsArrayList = list_item
                    } else {
                        val filteredList = ArrayList<ViewDocumentsModel>()
                        for (row in list_item) {
                            if (AndroidUtils.isNull(row.name)
                                    .lowercase()
                                    .contains(charString.lowercase())
                            ) {
                                filteredList.add(row)
                            }
                        }
                        itemsArrayList = filteredList
                    }
                }
                val filterResults = FilterResults()
                filterResults.count = itemsArrayList?.size ?: 0
                filterResults.values = itemsArrayList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                itemsArrayList = filterResults.values as ArrayList<ViewDocumentsModel>?
                notifyDataSetChanged()
            }
        }
    }

    interface Eventlistner {
        fun edit_document(viewDocumentsModel: ViewDocumentsModel?)
        fun Display_Document(viewDocumentsModel: ViewDocumentsModel?)
        fun ViewDialog(viewDocumentsModel: ViewDocumentsModel?, ViewType: String?)
        fun Download_Document(viewDocumentsModel: ViewDocumentsModel?)
        fun Update_Tag(viewDocumentsModel: ViewDocumentsModel?)
    }

    override fun getItemViewType(position: Int): Int {
        if (DOCUMENT_TYPE_TAG == "Deleted") return VIEW_TYPE_DELETED
        return currentViewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = when (viewType) {
            VIEW_TYPE_DELETED -> LayoutInflater.from(parent.context)
                .inflate(R.layout.deleted_doc_view, parent, false)
            VIEW_TYPE_GRID -> LayoutInflater.from(parent.context)
                .inflate(R.layout.view_documents_grid_item, parent, false)
            VIEW_TYPE_LIST -> LayoutInflater.from(parent.context)
                .inflate(R.layout.view_documents_list, parent, false)
            else -> LayoutInflater.from(parent.context)
                .inflate(R.layout.view_documents_list, parent, false)
        }
        return MyViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val viewDocumentsModel = itemsArrayList?.get(position) ?: return
        try {
            if (DOCUMENT_TYPE_TAG == "Deleted") {
                bindDeletedView(holder, viewDocumentsModel, position)
            } else if (currentViewType == VIEW_TYPE_GRID) {
                bindGridView(holder, viewDocumentsModel, position)
            } else {
                bindListView(holder, viewDocumentsModel, position)
            }
        } catch (e: Exception) {
            Log.e("BindError", "Exception in onBindViewHolder: " + e.message, e)
        }
    }

    override fun onViewRecycled(holder: MyViewHolder) {
        super.onViewRecycled(holder)
        holder.currentDocId = ""
        if (holder.wv_doc_preview_webview != null) {
            holder.wv_doc_preview_webview!!.stopLoading()
            holder.wv_doc_preview_webview!!.loadUrl("about:blank")
            holder.wv_doc_preview_webview!!.webViewClient = WebViewClient()
        }
        if (holder.wv_doc_preview_image != null) {
            Glide.with(cContext).clear(holder.wv_doc_preview_image!!)
        }
    }

    private fun bindDeletedView(
        holder: MyViewHolder,
        viewDocumentsModel: ViewDocumentsModel,
        position: Int
    ) {
        holder.tv_client_name?.text = viewDocumentsModel.name
        holder.tv_doc_description?.text = viewDocumentsModel.description
        holder.tv_created_date?.text = " " + viewDocumentsModel.created
        holder.tv_deleted_by_date?.text = viewDocumentsModel.deletedBy

        val exp_date = viewDocumentsModel.deletedOn
        val date_new = AndroidUtils.stringToDateTimeDefault(exp_date, "MMM dd, yyyy hh:mm a")
        val created = AndroidUtils.getDateToString(date_new, "MMM dd, yyyy")
        holder.tv_deleted_on_date?.text = created

        holder.tv_doc_type_name?.text = viewDocumentsModel.category

        val dis = viewDocumentsModel.is_disabled
        holder.cv_view_documents?.isEnabled = !dis
        holder.ll_view_icons?.alpha = if (dis) 0.5f else 1.0f

        holder.tv_doc_description?.let {
            it.paintFlags = it.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }

        holder.cv_view_documents?.setOnClickListener {
            eventlistner.Display_Document(viewDocumentsModel)
        }

        val itemActions = ArrayList<ActionModel>()
        itemActions.add(ActionModel("Restore"))
        itemActions.add(ActionModel("Delete"))

        if (holder.custom_spinner_cardview != null) {
            val isExpanded = position == expandedPosition

            holder.custom_spinner_cardview?.setOnClickListener(null)
            holder.sp_action?.onItemClickListener = null

            if (isExpanded && holder.action_list_card != null && holder.sp_action != null) {
                val itemAdapter = CommonSpinnerAdapter(cContext as Activity, itemActions)
                holder.sp_action?.adapter = itemAdapter
                holder.sp_action?.post { AndroidUtils.setDynamicHeight(holder.sp_action) }
                holder.action_list_card?.visibility = View.VISIBLE
                holder.sp_action?.visibility = View.VISIBLE
            } else {
                holder.sp_action?.adapter = null
                holder.action_list_card?.visibility = View.GONE
                holder.sp_action?.visibility = View.GONE
            }

            holder.custom_spinner_cardview?.setOnClickListener {
                val cur = holder.adapterPosition
                if (cur == RecyclerView.NO_POSITION) return@setOnClickListener

                val prev = expandedPosition
                expandedPosition = if (expandedPosition == cur) -1 else cur

                if (prev != -1 && prev != cur) safeNotifyItemChanged(prev)
                safeNotifyItemChanged(cur)
            }

            holder.sp_action?.onItemClickListener =
                android.widget.AdapterView.OnItemClickListener { parent, view, pos, id ->
                    val cur = holder.adapterPosition
                    if (cur == RecyclerView.NO_POSITION) return@OnItemClickListener

                    val actionName = itemActions[pos].name
                    expandedPosition = -1
                    safeNotifyItemChanged(cur)

                    Handler(Looper.getMainLooper()).post {
                        dispatchDeletedAction(actionName, viewDocumentsModel)
                    }
                }
        }

        applyHighlight(holder, viewDocumentsModel)
    }

    private fun bindListView(
        holder: MyViewHolder,
        viewDocumentsModel: ViewDocumentsModel,
        position: Int
    ) {
        holder.tv_client_name?.text = viewDocumentsModel.uploaded_by
        holder.tv_client_name_one?.text = viewDocumentsModel.uploaded_by
        holder.tv_doc_description?.text = viewDocumentsModel.description
        holder.tv_document_display_name?.text = viewDocumentsModel.name
        holder.tv_image_name?.text = viewDocumentsModel.name
        holder.tv_created_date?.text = viewDocumentsModel.created
        holder.tv_Expiration_date?.text = viewDocumentsModel.expiration_date

        is_encrypted = viewDocumentsModel.is_encrypted
        val showLock = is_encrypted || viewDocumentsModel.added_encryption
        val isDownloadDisabled = viewDocumentsModel.is_disabled

        if (!is_MergePdfClicked) {
            safeGone(holder.lock_open)
            safeGone(holder.lock_close)
            safeGone(holder.iv_edit_document)
            safeGone(holder.tv_Expiration_date)
            applyDownloadState(holder, isDownloadDisabled)
        } else {
            applyLockState(holder, showLock)
            applyDownloadState(holder, isDownloadDisabled)
            safeVisible(holder.iv_edit_document)
            safeVisible(holder.tv_Expiration_date)
        }

        applyDisabledState(holder, viewDocumentsModel.isdisabled)
        bindTags(holder, viewDocumentsModel.tagslist)

        holder.tv_client_name?.let {
            it.paintFlags = it.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
        holder.tv_doc_description?.let {
            it.paintFlags = it.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }

        holder.cv_view_documents?.setOnClickListener {
            eventlistner.Display_Document(viewDocumentsModel)
        }
        attachLegacyClickListeners(holder, viewDocumentsModel)
        val itemActions = buildStandardActions(showLock, isDownloadDisabled)

        if (holder.custom_spinner_cardview != null) {
            val isExpanded = position == expandedPosition

            holder.custom_spinner_cardview?.setOnClickListener(null)
            holder.sp_action?.onItemClickListener = null

            if (isExpanded && holder.action_list_card != null && holder.sp_action != null) {
                val itemAdapter = CommonSpinnerAdapter(cContext as Activity, itemActions)
                holder.sp_action?.adapter = itemAdapter
                holder.sp_action?.post { AndroidUtils.setDynamicHeight(holder.sp_action) }
                holder.action_list_card?.visibility = View.VISIBLE
                holder.sp_action?.visibility = View.VISIBLE
            } else {
                holder.sp_action?.adapter = null
                holder.action_list_card?.visibility = View.GONE
                holder.sp_action?.visibility = View.GONE
            }

            holder.custom_spinner_cardview?.setOnClickListener {
                val cur = holder.adapterPosition
                if (cur == RecyclerView.NO_POSITION) return@setOnClickListener

                val prev = expandedPosition
                expandedPosition = if (expandedPosition == cur) -1 else cur

                if (prev != -1 && prev != cur) safeNotifyItemChanged(prev)
                safeNotifyItemChanged(cur)
            }

            holder.sp_action?.onItemClickListener =
                android.widget.AdapterView.OnItemClickListener { parent, view, pos, id ->
                    val cur = holder.adapterPosition
                    if (cur == RecyclerView.NO_POSITION) return@OnItemClickListener

                    val actionName = itemActions[pos].name
                    expandedPosition = -1
                    safeNotifyItemChanged(cur)

                    Handler(Looper.getMainLooper()).post {
                        dispatchAction(actionName, viewDocumentsModel)
                    }
                }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun bindGridView(
        holder: MyViewHolder,
        viewDocumentsModel: ViewDocumentsModel,
        position: Int
    ) {
        resetThumbnailToLoadingState(holder)

        holder.tv_document_display_name?.text = viewDocumentsModel.name

        val createdDate = viewDocumentsModel.created
        if (createdDate != null && createdDate.contains(",")) {
            val parts = createdDate.split(",")
            holder.tv_created_date?.text =
                if (parts.size >= 2) parts[0].trim() + "," + parts[1].trim() else createdDate
        } else {
            holder.tv_created_date?.text = createdDate ?: ""
        }

        val docId = viewDocumentsModel.id ?: ""
        holder.currentDocId = docId

        val fileUrl = viewDocumentsModel.origin
        val contentType = viewDocumentsModel.content_type
        val isEncrypted = viewDocumentsModel.is_encrypted || viewDocumentsModel.added_encryption

        loadGridThumbnailWebView(holder, docId, fileUrl, contentType, isEncrypted)

        is_encrypted = viewDocumentsModel.is_encrypted
        val showLock = is_encrypted || viewDocumentsModel.added_encryption
        val isDownloadDisabled = viewDocumentsModel.is_disabled

        if (!is_MergePdfClicked) {
            safeGone(holder.lock_open)
            safeGone(holder.lock_close)
            applyDownloadState(holder, isDownloadDisabled)
        } else {
            applyLockState(holder, showLock)
            applyDownloadState(holder, isDownloadDisabled)
        }

        applyDisabledState(holder, viewDocumentsModel.isdisabled)

        holder.cv_view_documents?.setOnClickListener {
            eventlistner.Display_Document(viewDocumentsModel)
        }

        attachLegacyClickListeners(holder, viewDocumentsModel)

        val itemActions = buildStandardActions(showLock, isDownloadDisabled)

        if (holder.custom_spinner_cardview != null) {
            val isExpanded = position == expandedPosition

            holder.custom_spinner_cardview?.setOnClickListener(null)
            holder.sp_action?.onItemClickListener = null

            if (isExpanded && holder.action_list_card != null && holder.sp_action != null) {
                val itemAdapter = CommonSpinnerAdapter(cContext as Activity, itemActions)
                holder.sp_action?.adapter = itemAdapter
                holder.sp_action?.post { AndroidUtils.setDynamicHeight(holder.sp_action) }
                holder.action_list_card?.visibility = View.VISIBLE
                holder.sp_action?.visibility = View.VISIBLE
            } else {
                holder.sp_action?.adapter = null
                holder.action_list_card?.visibility = View.GONE
                holder.sp_action?.visibility = View.GONE
            }

            holder.custom_spinner_cardview?.setOnClickListener {
                val cur = holder.adapterPosition
                if (cur == RecyclerView.NO_POSITION) return@setOnClickListener

                val prev = expandedPosition
                expandedPosition = if (expandedPosition == cur) -1 else cur

                if (prev != -1 && prev != cur) safeNotifyItemChanged(prev)
                safeNotifyItemChanged(cur)
            }

            holder.sp_action?.onItemClickListener =
                android.widget.AdapterView.OnItemClickListener { parent, view, pos, id ->
                    val cur = holder.adapterPosition
                    if (cur == RecyclerView.NO_POSITION) return@OnItemClickListener

                    val actionName = itemActions[pos].name
                    expandedPosition = -1
                    safeNotifyItemChanged(cur)

                    Handler(Looper.getMainLooper()).post {
                        dispatchAction(actionName, viewDocumentsModel)
                    }
                }
        }
    }

    private fun buildStandardActions(showLock: Boolean, isDownloadDisabled: Boolean): ArrayList<ActionModel> {
        val actions = ArrayList<ActionModel>()
        actions.add(ActionModel("View"))
        actions.add(ActionModel("Edit Info"))
        if (isDownloadDisabled) {
            actions.add(ActionModel("Download"))
        }
        actions.add(ActionModel("Delete"))
        actions.add(ActionModel("Update Tags"))
        return actions
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadGridThumbnailWebView(
        holder: MyViewHolder,
        docId: String,
        fileUrl: String?,
        contentType: String?,
        isEncrypted: Boolean
    ) {
        holder.pb_doc_loading?.visibility = View.VISIBLE
        holder.view_thumb_placeholder?.visibility = View.VISIBLE
        holder.wv_doc_preview_webview?.visibility = View.GONE
        holder.wv_doc_preview_image?.visibility = View.GONE

        if (isEncrypted || fileUrl == null || fileUrl.trim().isEmpty()) {
            fetchDocumentUrlThenPreview(holder, docId, contentType, isEncrypted)
            return
        }
        showDocumentInWebView(holder, docId, fileUrl, contentType)
    }

    private fun fetchDocumentUrlThenPreview(
        holder: MyViewHolder,
        docId: String,
        contentType: String?,
        isEncrypted: Boolean
    ) {
        Thread {
            var fetchedUrl: String? = null
            var effectiveContentType = contentType
            try {
                if (isEncrypted) {
                    val apiUrl = java.net.URL(Constants.decryptUrl)
                    val conn = apiUrl.openConnection() as java.net.HttpURLConnection
                    val boundary = "----WebKitFormBoundary" + System.currentTimeMillis()
                    conn.requestMethod = "POST"
                    conn.doOutput = true
                    conn.doInput = true
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                    conn.setRequestProperty("Authorization", "Bearer " + Constants.TOKEN)
                    conn.setRequestProperty("x-client-type", "mobile")
                    conn.connectTimeout = 15000
                    conn.readTimeout = 30000

                    val out = java.io.DataOutputStream(conn.outputStream)
                    out.writeBytes("--$boundary\r\n")
                    out.writeBytes("Content-Disposition: form-data; name=\"docid\"\r\n\r\n")
                    out.writeBytes("$docId\r\n")
                    out.writeBytes("--$boundary\r\n")
                    out.writeBytes("Content-Disposition: form-data; name=\"download\"\r\n\r\n")
                    out.writeBytes("false\r\n")
                    out.writeBytes("--$boundary--\r\n")
                    out.flush()
                    out.close()

                    val code = conn.responseCode
                    val respCt = conn.contentType

                    if (code == 200) {
                        if (respCt != null && respCt.contains("application/pdf")) {
                            val pdfFile = File.createTempFile("enc_prev_$docId", ".pdf", cContext.cacheDir)
                            BufferedInputStream(conn.inputStream).use { `in` ->
                                FileOutputStream(pdfFile).use { fo ->
                                    val buf = ByteArray(4096)
                                    var n: Int
                                    while (`in`.read(buf).also { n = it } != -1) {
                                        fo.write(buf, 0, n)
                                    }
                                }
                            }
                            fetchedUrl = "localfile://" + pdfFile.absolutePath
                            effectiveContentType = "application/pdf"
                        } else {
                            val reader = java.io.BufferedReader(java.io.InputStreamReader(conn.inputStream))
                            val sb = java.lang.StringBuilder()
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                sb.append(line)
                            }
                            reader.close()
                            val resp = JSONObject(sb.toString())
                            if (!resp.optBoolean("error", true)) {
                                val data = resp.optJSONObject("data")
                                if (data != null) {
                                    fetchedUrl = data.optString("url")
                                    val ct = data.optString("content_type", contentType)
                                    if (!ct.isEmpty()) effectiveContentType = ct
                                }
                            }
                        }
                    }
                } else {
                    val apiUrl = java.net.URL(Constants.PROF_URL + "v3/document/" + docId + "/view")
                    val conn = apiUrl.openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.connectTimeout = 15000
                    conn.readTimeout = 15000
                    val token = Constants.TOKEN
                    if (token != null && !token.isEmpty()) {
                        conn.setRequestProperty("Authorization", "Bearer $token")
                    }
                    conn.connect()

                    val reader = java.io.BufferedReader(java.io.InputStreamReader(conn.inputStream))
                    val sb = java.lang.StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(line)
                    }
                    reader.close()

                    val resp = JSONObject(sb.toString())
                    if (!resp.optBoolean("error", true)) {
                        val data = resp.optJSONObject("data")
                        if (data != null) {
                            val rawUrl = data.optString("url")
                            val fileCt = data.optString("content_type", contentType)
                            if (!fileCt.isEmpty()) effectiveContentType = fileCt

                            val isImg = fileCt.lowercase().startsWith("image/")
                            val isPdf = fileCt.lowercase().contains("pdf") || rawUrl.lowercase().contains(".pdf")

                            if (isImg || isPdf) {
                                fetchedUrl = rawUrl
                            } else {
                                fetchedUrl = callDoc2PdfApiSync(rawUrl)
                                if (fetchedUrl == null) fetchedUrl = rawUrl
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FetchUrl", "fetchDocumentUrlThenPreview: " + e.message, e)
            }

            val finalUrl = fetchedUrl
            val finalContentType = effectiveContentType

            Handler(Looper.getMainLooper()).post {
                if (docId != holder.currentDocId) return@post
                if (finalUrl != null && !finalUrl.isEmpty()) {
                    showDocumentInWebView(holder, docId, finalUrl, finalContentType)
                } else {
                    showThumbFallback(holder)
                }
            }
        }.start()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun showDocumentInWebView(holder: MyViewHolder, docId: String, fileUrl: String?, contentType: String?) {
        var isImage = false
        if (contentType != null) isImage = contentType.lowercase().startsWith("image/")
        if (!isImage && fileUrl != null) {
            val lower = fileUrl.lowercase()
            isImage = lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                    lower.endsWith(".png") || lower.endsWith(".gif") ||
                    lower.endsWith(".webp") || lower.endsWith(".bmp")
        }

        var isPdf = false
        if (contentType != null) isPdf = contentType.lowercase().contains("pdf")
        if (!isPdf && fileUrl != null) {
            isPdf = fileUrl.lowercase().endsWith(".pdf") || fileUrl.startsWith("localfile://")
        }

        if (isImage) {
            holder.pb_doc_loading?.visibility = View.GONE
            holder.view_thumb_placeholder?.visibility = View.GONE

            if (holder.wv_doc_preview_image != null) {
                holder.wv_doc_preview_image?.visibility = View.VISIBLE
                holder.wv_doc_preview_webview?.visibility = View.GONE
                Glide.with(cContext)
                    .load(fileUrl)
                    .centerCrop()
                    .into(holder.wv_doc_preview_image!!)
            } else if (holder.wv_doc_preview_webview != null) {
                showImageInWebView(holder, docId, fileUrl!!)
            }
            return
        }

        if (isPdf) {
            val cached = pdfThumbnailCache[docId]
            if (cached != null) {
                displayPdfBitmap(holder, cached)
                return
            }
            if (java.lang.Boolean.TRUE == pdfLoadingIds[docId]) return
            pdfLoadingIds[docId] = true

            holder.pb_doc_loading?.visibility = View.VISIBLE
            holder.view_thumb_placeholder?.visibility = View.VISIBLE

            val urlForThread = fileUrl!!
            Thread {
                var bitmap: Bitmap? = null
                var tempFile: File? = null
                val isLocal = urlForThread.startsWith("localfile://")
                try {
                    if (isLocal) {
                        tempFile = File(urlForThread.replace("localfile://", ""))
                    } else {
                        tempFile = File.createTempFile("grid_prev_$docId", ".pdf", cContext.cacheDir)
                        val url = java.net.URL(urlForThread)
                        val conn = url.openConnection() as java.net.HttpURLConnection
                        conn.connectTimeout = 15000
                        conn.readTimeout = 30000
                        conn.connect()
                        BufferedInputStream(conn.inputStream).use { `in` ->
                            FileOutputStream(tempFile).use { fo ->
                                val buf = ByteArray(4096)
                                var n: Int
                                while (`in`.read(buf).also { n = it } != -1) {
                                    fo.write(buf, 0, n)
                                }
                            }
                        }
                    }

                    val pfd = android.os.ParcelFileDescriptor.open(tempFile, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = android.graphics.pdf.PdfRenderer(pfd)
                    val page = renderer.openPage(0)
                    val w = 600
                    val h = (w * (page.height.toFloat() / page.width)).toInt()
                    bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    renderer.close()
                    pfd.close()
                } catch (e: Exception) {
                    Log.e("GridPdfPreview", "PDF render failed: " + e.message)
                } finally {
                    if (tempFile != null && !isLocal) tempFile.delete()
                }

                val finalBitmap = bitmap
                Handler(Looper.getMainLooper()).post {
                    pdfLoadingIds[docId] = false
                    if (finalBitmap != null) pdfThumbnailCache[docId] = finalBitmap
                    if (docId == holder.currentDocId) {
                        if (finalBitmap != null) {
                            displayPdfBitmap(holder, finalBitmap)
                        } else {
                            showInGoogleDocsWebView(holder, docId, urlForThread)
                        }
                    }
                }
            }.start()
            return
        }

        showInGoogleDocsWebView(holder, docId, fileUrl!!)
    }

    private fun displayPdfBitmap(holder: MyViewHolder, bitmap: Bitmap) {
        holder.pb_doc_loading?.visibility = View.GONE
        holder.view_thumb_placeholder?.visibility = View.GONE

        val target = holder.wv_doc_preview_image
        if (target != null) {
            target.visibility = View.VISIBLE
            holder.wv_doc_preview_webview?.visibility = View.GONE
            target.setImageBitmap(bitmap)
            target.scaleType = ImageView.ScaleType.CENTER_CROP
        } else if (holder.wv_doc_preview_webview != null) {
            val baos = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos)
            val base64 = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.DEFAULT)
            val html = ("<!DOCTYPE html><html><head>"
                    + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                    + "<style>body{margin:0;padding:0;background:#fff;}"
                    + "img{width:100%;height:auto;display:block;}</style></head><body>"
                    + "<img src='data:image/png;base64," + base64 + "'/>"
                    + "</body></html>")
            configureWebView(holder.wv_doc_preview_webview!!)
            holder.wv_doc_preview_webview?.visibility = View.VISIBLE
            holder.wv_doc_preview_webview?.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun showImageInWebView(holder: MyViewHolder, docId: String, imageUrl: String) {
        if (holder.wv_doc_preview_webview == null) {
            showThumbFallback(holder)
            return
        }
        configureWebView(holder.wv_doc_preview_webview!!)
        holder.wv_doc_preview_webview?.webViewClient = buildWebViewClient(holder, docId)
        val html = ("<!DOCTYPE html><html><head>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<style>body{margin:0;padding:0;background:#fff;display:flex;"
                + "align-items:center;justify-content:center;min-height:100vh;}"
                + "img{max-width:100%;max-height:100%;object-fit:cover;}</style>"
                + "</head><body><img src='" + imageUrl + "'/></body></html>")
        holder.wv_doc_preview_webview?.visibility = View.VISIBLE
        holder.wv_doc_preview_webview?.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun showInGoogleDocsWebView(holder: MyViewHolder, docId: String, fileUrl: String) {
        if (holder.wv_doc_preview_webview == null) {
            showThumbFallback(holder)
            return
        }
        configureWebView(holder.wv_doc_preview_webview!!)
        holder.wv_doc_preview_webview?.webViewClient = buildWebViewClient(holder, docId)
        val encodedUrl = android.net.Uri.encode(fileUrl)
        val viewerUrl = "https://docs.google.com/viewer?embedded=true&url=$encodedUrl"
        holder.wv_doc_preview_webview?.visibility = View.VISIBLE
        holder.wv_doc_preview_webview?.loadUrl(viewerUrl)
        Handler(Looper.getMainLooper()).postDelayed({
            if (holder.pb_doc_loading != null && holder.pb_doc_loading!!.visibility == View.VISIBLE) {
                showThumbFallback(holder)
            }
        }, 12000)
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun configureWebView(wv: WebView) {
        val s = wv.settings
        s.javaScriptEnabled = true
        s.loadWithOverviewMode = true
        s.useWideViewPort = true
        s.builtInZoomControls = false
        s.displayZoomControls = false
        s.domStorageEnabled = true
        s.setSupportZoom(false)
        s.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        s.cacheMode = WebSettings.LOAD_DEFAULT
        s.allowFileAccess = true
        s.loadsImagesAutomatically = true
        wv.setBackgroundColor(ContextCompat.getColor(cContext, android.R.color.white))
        wv.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        wv.isHorizontalScrollBarEnabled = false
        wv.isVerticalScrollBarEnabled = false
        wv.setOnTouchListener { _, _ -> true }
    }

    private fun buildWebViewClient(holder: MyViewHolder, docId: String): WebViewClient {
        return object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                if (docId != holder.currentDocId) return
                holder.pb_doc_loading?.visibility = View.GONE
                holder.view_thumb_placeholder?.visibility = View.GONE
                view.visibility = View.VISIBLE
            }

            override fun onReceivedError(view: WebView, req: WebResourceRequest, err: WebResourceError) {
                showThumbFallback(holder)
            }

            @Deprecated("Deprecated in Java")
            override fun onReceivedError(view: WebView, code: Int, desc: String, url: String) {
                showThumbFallback(holder)
            }
        }
    }

    private fun resetThumbnailToLoadingState(holder: MyViewHolder) {
        holder.view_thumb_placeholder?.visibility = View.VISIBLE
        holder.pb_doc_loading?.visibility = View.VISIBLE
        if (holder.wv_doc_preview_webview != null) {
            holder.wv_doc_preview_webview?.visibility = View.GONE
            holder.wv_doc_preview_webview?.stopLoading()
            holder.wv_doc_preview_webview?.loadUrl("about:blank")
        }
        holder.wv_doc_preview_image?.visibility = View.GONE
    }

    private fun showThumbFallback(holder: MyViewHolder) {
        holder.pb_doc_loading?.visibility = View.GONE
        holder.wv_doc_preview_webview?.visibility = View.GONE
        holder.wv_doc_preview_image?.visibility = View.GONE
        holder.view_thumb_placeholder?.visibility = View.VISIBLE
    }

    private fun callDoc2PdfApiSync(fileUrl: String): String? {
        try {
            val apiUrl = java.net.URL(Constants.doctopdfUrl)
            val conn = apiUrl.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer " + Constants.TOKEN)
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 60000

            val body = JSONObject()
            body.put("url", fileUrl)
            val input = body.toString().toByteArray(charset("utf-8"))
            conn.outputStream.write(input, 0, input.size)
            conn.connect()

            val code = conn.responseCode
            val ctHdr = conn.contentType
            if (code == 200) {
                if (ctHdr != null && ctHdr.contains("application/pdf")) {
                    val pdfFile = File.createTempFile("doc2pdf_" + System.currentTimeMillis(), ".pdf", cContext.cacheDir)
                    BufferedInputStream(conn.inputStream).use { `in` ->
                        FileOutputStream(pdfFile).use { fo ->
                            val buf = ByteArray(4096)
                            var n: Int
                            while (`in`.read(buf).also { n = it } != -1) {
                                fo.write(buf, 0, n)
                            }
                        }
                    }
                    return "localfile://" + pdfFile.absolutePath
                } else {
                    val reader = java.io.BufferedReader(java.io.InputStreamReader(conn.inputStream))
                    val sb = java.lang.StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(line)
                    }
                    reader.close()
                    val resp = JSONObject(sb.toString())
                    if (!resp.optBoolean("error", true)) {
                        val data = resp.optJSONObject("data")
                        if (data != null) return data.optString("url")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FetchUrl", "doc2pdf failed: " + e.message)
        }
        return null
    }

    private fun dispatchDeletedAction(action: String, m: ViewDocumentsModel) {
        when (action) {
            "Restore" -> eventlistner.ViewDialog(m, "restore")
            "Delete" -> eventlistner.ViewDialog(m, "deleted")
        }
    }

    private fun dispatchAction(action: String, m: ViewDocumentsModel) {
        when (action) {
            "View" -> eventlistner.Display_Document(m)
            "Edit Info" -> eventlistner.edit_document(m)
            "Delete" -> eventlistner.ViewDialog(m, "delete")
            "Update Tags" -> eventlistner.Update_Tag(m)
            "Download" -> {
                eventlistner.Download_Document(m)
                eventlistner.ViewDialog(m, "download")
            }
        }
    }

    private fun applyLockState(holder: MyViewHolder, isEncrypted: Boolean) {
        if (isEncrypted) {
            safeVisible(holder.lock_close)
            safeGone(holder.lock_open)
        } else {
            safeVisible(holder.lock_open)
            safeGone(holder.lock_close)
        }
    }

    private fun applyDownloadState(holder: MyViewHolder, isDisabled: Boolean) {
        if (isDisabled) {
            safeGone(holder.iv_deleteDisabled)
            safeVisible(holder.iv_deleteEnabled)
        } else {
            safeVisible(holder.iv_deleteDisabled)
            safeGone(holder.iv_deleteEnabled)
        }
    }

    private fun applyDisabledState(holder: MyViewHolder, isDisabled: Boolean) {
        if (isDisabled) {
            safeSetEnabled(holder.lock_open, false)
            safeSetEnabled(holder.lock_close, false)
            safeSetEnabled(holder.iv_edit_document, false)
            holder.cv_view_documents?.isEnabled = false
            holder.ll_view_icons?.alpha = 0.5f
        } else {
            safeSetEnabled(holder.lock_open, true)
            safeSetEnabled(holder.lock_close, true)
            safeSetEnabled(holder.iv_edit_document, true)
            holder.cv_view_documents?.isEnabled = true
            holder.ll_view_icons?.alpha = 1.0f
        }
    }

    private fun bindTags(holder: MyViewHolder, tagArray: JSONArray?) {
        if (holder.ll_added_tags == null) return
        if (tagArray != null && tagArray.length() > 0) {
            val valueList = ArrayList<String>()
            for (i in 0 until tagArray.length()) {
                try {
                    val tagObject = tagArray.getJSONObject(i)
                    val tagType = tagObject.optString("key")
                    val tagName = tagObject.optString("value")
                    val tagModel = DocumentsModel()
                    tagModel.tag_type = tagType
                    tagModel.tag_name = tagName
                    tags_list.add(tagModel)
                    valueList.add("  $tagType-$tagName")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            holder.tv_added_tags?.text = String.format("%s", valueList.joinToString(", "))
            holder.ll_added_tags?.visibility = View.VISIBLE
        } else {
            holder.ll_added_tags?.visibility = View.GONE
        }
    }

    private fun attachLegacyClickListeners(holder: MyViewHolder, m: ViewDocumentsModel) {
        holder.lock_open?.setOnClickListener { eventlistner.ViewDialog(m, "encrypt") }
        holder.lock_close?.setOnClickListener { eventlistner.ViewDialog(m, "decrypt") }
        holder.iv_deleteEnabled?.setOnClickListener {
            eventlistner.Download_Document(m)
            eventlistner.ViewDialog(m, "enabled")
        }
        holder.iv_deleteDisabled?.setOnClickListener { eventlistner.ViewDialog(m, "disabled") }
        holder.iv_edit_document?.setOnClickListener { eventlistner.edit_document(m) }
        holder.uv_delete_document?.setOnClickListener { eventlistner.ViewDialog(m, "delete") }
        holder.tag_icon?.setOnClickListener { eventlistner.Update_Tag(m) }
        holder.download_icon?.setOnClickListener {
            eventlistner.Download_Document(m)
            eventlistner.ViewDialog(m, "download")
        }
    }

    private fun applyHighlight(holder: MyViewHolder, model: ViewDocumentsModel) {
        if (highlightIds != null && highlightIds!!.contains(model.id)) {
            holder.cv_view_documents?.setBackground(
                ContextCompat.getDrawable(holder.itemView.context, R.drawable.blue_stroke_card)
            )
            holder.cv_view_documents?.cardElevation = 8f
            Handler(Looper.getMainLooper()).postDelayed({
                holder.cv_view_documents?.setBackground(
                    ContextCompat.getDrawable(holder.itemView.context, R.drawable.rectangular_white_background)
                )
            }, 5000)
        } else {
            holder.cv_view_documents?.setCardBackgroundColor(
                cContext.resources.getColor(android.R.color.white)
            )
            holder.cv_view_documents?.cardElevation = 4f
        }
    }

    private fun safeVisible(v: View?) {
        v?.visibility = View.VISIBLE
    }

    private fun safeGone(v: View?) {
        v?.visibility = View.GONE
    }

    private fun safeSetEnabled(v: View?, e: Boolean) {
        v?.isEnabled = e
    }

    fun setData(newData: ArrayList<ViewDocumentsModel>) {
        itemsArrayList = newData
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return itemsArrayList?.size ?: 0
    }

    inner class MyViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
        var cv_view_documents: CardView? = null
        var ll_view_icons: LinearLayout? = null
        var tv_client_name: TextView? = null
        var tv_doc_description: TextView? = null
        var tv_created_date: TextView? = null

        var custom_spinner_cardview: ImageView? = null
        var action_list_card: CardView? = null
        var sp_action: ListView? = null

        var iv_doc_image: ImageView? = null
        var iv_edit_document: ImageView? = null
        var uv_delete_document: ImageView? = null
        var download_icon: ImageView? = null
        var tag_icon: ImageView? = null
        var lock_open: ImageView? = null
        var lock_close: ImageView? = null
        var iv_deleteEnabled: ImageView? = null
        var iv_deleteDisabled: ImageView? = null
        var ll_added_tags: LinearLayout? = null
        var tv_tags: TextView? = null
        var tv_added_tags: TextView? = null
        var tv_document_display_name: TextView? = null
        var tv_client_name_one: TextView? = null
        var tv_image_name: TextView? = null
        var tv_Expiration: TextView? = null
        var tv_Expiration_date: TextView? = null

        var wv_doc_preview_image: ImageView? = null
        var wv_doc_preview_webview: WebView? = null

        var pb_doc_loading: ProgressBar? = null
        var view_thumb_placeholder: View? = null
        var currentDocId: String = ""

        var siv_profile_icon: ShapeableImageView? = null
        var rl_deletedView: android.widget.RelativeLayout? = null
        var delete_icon: ImageView? = null
        var restore_icon: ImageView? = null
        var tv_deleted_on: TextView? = null
        var tv_deleted_on_date: TextView? = null
        var tv_doc_type: TextView? = null
        var tv_doc_type_name: TextView? = null
        var tv_deleted_by: TextView? = null
        var tv_deleted_by_date: TextView? = null

        init {
            when (viewType) {
                VIEW_TYPE_DELETED -> {
                    cv_view_documents = itemView.findViewById(R.id.cv_view_documents)
                    ll_view_icons = itemView.findViewById(R.id.ll_view_icons)
                    rl_deletedView = itemView.findViewById(R.id.rl_deletedView)
                    tv_client_name = itemView.findViewById(R.id.tv_client_name)
                    tv_client_name?.setTextSize(DynamicUtils.fifteen.toFloat())
                    tv_doc_description = itemView.findViewById(R.id.tv_doc_description)
                    tv_doc_description?.setTextSize(DynamicUtils.fifteen.toFloat())
                    tv_created_date = itemView.findViewById(R.id.tv_created_date)
                    tv_created_date?.setText(R.string.date)
                    tv_deleted_by = itemView.findViewById(R.id.tv_deleted_by)
                    tv_deleted_by?.setText(R.string.deleted_by)
                    tv_deleted_by_date = itemView.findViewById(R.id.tv_deleted_by_date)
                    tv_deleted_on = itemView.findViewById(R.id.tv_deleted_on)
                    tv_deleted_on?.setText(R.string.deleted_on)
                    tv_deleted_on_date = itemView.findViewById(R.id.tv_deleted_on_date)
                    tv_doc_type = itemView.findViewById(R.id.tv_doc_type)
                    tv_doc_type?.setText(R.string.document_type)
                    tv_doc_type_name = itemView.findViewById(R.id.tv_doc_type_name)
                    restore_icon = itemView.findViewById(R.id.restore_icon)
                    delete_icon = itemView.findViewById(R.id.delete_icon)
                    custom_spinner_cardview = itemView.findViewById(R.id.custom_spinner_cardview)
                    action_list_card = itemView.findViewById(R.id.action_list_card)
                    sp_action = itemView.findViewById(R.id.action_list)
                }
                VIEW_TYPE_GRID -> {
                    cv_view_documents = itemView.findViewById(R.id.cv_view_documents)
                    ll_view_icons = itemView.findViewById(R.id.ll_view_icons)
                    wv_doc_preview_image = itemView.findViewById(R.id.wv_doc_preview)
                    wv_doc_preview_webview = itemView.findViewById(R.id.wv_doc_preview_web)
                    iv_doc_image = itemView.findViewById(R.id.iv_doc_image)
                    pb_doc_loading = itemView.findViewById(R.id.pb_doc_loading)
                    view_thumb_placeholder = itemView.findViewById(R.id.view_thumb_placeholder)
                    tv_document_display_name = itemView.findViewById(R.id.tv_document_display_name)
                    tv_document_display_name?.maxLines = 2
                    tv_document_display_name?.ellipsize = TextUtils.TruncateAt.END
                    tv_created_date = itemView.findViewById(R.id.tv_created_date)
                    lock_open = itemView.findViewById(R.id.lock_open)
                    lock_close = itemView.findViewById(R.id.lock_close)
                    iv_deleteEnabled = itemView.findViewById(R.id.iv_deleteEnabled)
                    iv_deleteDisabled = itemView.findViewById(R.id.iv_deleteDisabled)
                    ll_added_tags = itemView.findViewById(R.id.ll_added_tags)
                    tv_added_tags = itemView.findViewById(R.id.tv_added_tags)
                    tv_tags = itemView.findViewById(R.id.tv_tags)
                    tv_tags?.setText(R.string.tags)
                    custom_spinner_cardview = itemView.findViewById(R.id.custom_spinner_cardview)
                    action_list_card = itemView.findViewById(R.id.action_list_card)
                    sp_action = itemView.findViewById(R.id.action_list)
                    tv_client_name = itemView.findViewById(R.id.tv_client_name)
                    tv_client_name_one = itemView.findViewById(R.id.tv_client_name_one)
                    tv_image_name = itemView.findViewById(R.id.tv_image_name)
                    tv_doc_description = itemView.findViewById(R.id.tv_doc_description)
                    tv_Expiration = itemView.findViewById(R.id.tv_Expiration)
                    tv_Expiration_date = itemView.findViewById(R.id.tv_Expiration_date)
                    download_icon = itemView.findViewById(R.id.download_icon)
                    iv_edit_document = itemView.findViewById(R.id.iv_edit_document)
                    uv_delete_document = itemView.findViewById(R.id.uv_delete_document)
                    tag_icon = itemView.findViewById(R.id.tag_icon)

                    if (wv_doc_preview_webview != null) {
                        val ws = wv_doc_preview_webview!!.settings
                        ws.javaScriptEnabled = true
                        ws.loadWithOverviewMode = true
                        ws.useWideViewPort = true
                        ws.builtInZoomControls = false
                        ws.displayZoomControls = false
                        ws.domStorageEnabled = true
                        ws.setSupportZoom(false)
                        ws.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        ws.loadsImagesAutomatically = true
                        wv_doc_preview_webview!!.setBackgroundColor(
                            ContextCompat.getColor(itemView.context, android.R.color.white)
                        )
                        wv_doc_preview_webview!!.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
                        wv_doc_preview_webview!!.isHorizontalScrollBarEnabled = false
                        wv_doc_preview_webview!!.isVerticalScrollBarEnabled = false
                        wv_doc_preview_webview!!.setOnTouchListener { _, _ -> true }
                    }

                    if (itemView is ViewGroup) {
                        itemView.clipChildren = false
                        itemView.clipToPadding = false
                    }
                }
                VIEW_TYPE_LIST -> {
                    cv_view_documents = itemView.findViewById(R.id.cv_view_documents)
                    ll_view_icons = itemView.findViewById(R.id.ll_view_icons)
                    ll_added_tags = itemView.findViewById(R.id.ll_added_tags)
                    iv_doc_image = itemView.findViewById(R.id.iv_doc_image)
                    tv_document_display_name = itemView.findViewById(R.id.tv_document_display_name)
                    tv_document_display_name?.maxLines = 2
                    tv_document_display_name?.ellipsize = TextUtils.TruncateAt.END
                    tv_client_name = itemView.findViewById(R.id.tv_client_name)
                    tv_client_name?.setTextSize(DynamicUtils.fifteen.toFloat())
                    tv_client_name_one = itemView.findViewById(R.id.tv_client_name_one)
                    tv_client_name_one?.setTextSize(DynamicUtils.fifteen.toFloat())
                    tv_image_name = itemView.findViewById(R.id.tv_image_name)
                    tv_image_name?.maxLines = 1
                    tv_image_name?.ellipsize = TextUtils.TruncateAt.END
                    tv_doc_description = itemView.findViewById(R.id.tv_doc_description)
                    tv_doc_description?.setTextSize(DynamicUtils.fifteen.toFloat())
                    tv_created_date = itemView.findViewById(R.id.tv_created_date)
                    tv_created_date?.setText(R.string.date)
                    tv_Expiration = itemView.findViewById(R.id.tv_Expiration)
                    tv_Expiration?.setText(R.string.expiration_)
                    tv_Expiration_date = itemView.findViewById(R.id.tv_Expiration_date)
                    tv_tags = itemView.findViewById(R.id.tv_tags)
                    tv_tags?.setText(R.string.tags)
                    tv_added_tags = itemView.findViewById(R.id.tv_added_tags)
                    lock_open = itemView.findViewById(R.id.lock_open)
                    lock_close = itemView.findViewById(R.id.lock_close)
                    iv_deleteEnabled = itemView.findViewById(R.id.iv_deleteEnabled)
                    iv_deleteDisabled = itemView.findViewById(R.id.iv_deleteDisabled)
                    download_icon = itemView.findViewById(R.id.download_icon)
                    iv_edit_document = itemView.findViewById(R.id.iv_edit_document)
                    uv_delete_document = itemView.findViewById(R.id.uv_delete_document)
                    tag_icon = itemView.findViewById(R.id.tag_icon)
                    custom_spinner_cardview = itemView.findViewById(R.id.custom_spinner_cardview)
                    action_list_card = itemView.findViewById(R.id.action_list_card)
                    sp_action = itemView.findViewById(R.id.action_list)
                }
            }
        }
    }
}
