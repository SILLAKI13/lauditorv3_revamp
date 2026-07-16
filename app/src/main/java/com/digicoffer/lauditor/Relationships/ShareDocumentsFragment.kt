package com.digicoffer.lauditor.Relationships

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Relationships.Adapter.RelationshipsAdapter
import com.digicoffer.lauditor.Relationships.Adapter.SharedDocumentsAdapter
import com.digicoffer.lauditor.Relationships.Adapter.UnshareDocumentAdapter
import com.digicoffer.lauditor.Relationships.Model.SharedDocumentsDo
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import java.util.Objects

class ShareDocumentsFragment : Fragment(), AsyncTaskCompleteListener, SharedDocumentsAdapter.EventListener {

    private var rb_client_doc: TextView? = null
    private var rb_firm_doc: TextView? = null
    private var search_wrapper: View? = null
    private var tl_search_layout: TextInputLayout? = null
    private var et_search: TextInputEditText? = null
    private var chk_select_all: CheckBox? = null
    private var tv_select_all_label: TextView? = null
    private var tv_selected_count: TextView? = null
    private var rv_documents: RecyclerView? = null
    private var tv_no_docs: TextView? = null
    private var btn_cancel: AppCompatButton? = null
    private var btn_share_action: AppCompatButton? = null
    private var ll_select_all_row: LinearLayout? = null

    private var progress_dialog: Dialog? = null

    private var relationshipId = ""
    private var clientId = ""
    private var TAG_rel = ""
    private var groupsArray = JSONArray()
    private var shared_tag = "client"

    private val shared_list = ArrayList<SharedDocumentsDo>()
    private val shared_by_us_list = ArrayList<SharedDocumentsDo>()
    private val selected_sharedocsList = ArrayList<SharedDocumentsDo>()
    private val selected_client_list = ArrayList<SharedDocumentsDo>()
    private val selected_firm_list = ArrayList<SharedDocumentsDo>()

    private var currentAdapter: SharedDocumentsAdapter? = null
    private var alertDialog_confirm: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) {
            relationshipId = args.getString(ARG_RELATIONSHIP_ID, "")
            clientId = args.getString(ARG_CLIENT_ID, "")
            TAG_rel = args.getString(ARG_TAG, "")
            val groupsJson = args.getString(ARG_GROUPS_JSON, "")
            try {
                if (groupsJson.isNotEmpty()) {
                    groupsArray = JSONArray(groupsJson)
                }
            } catch (ignored: JSONException) {
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_share_documents, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btn_header_back)
        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        val tvTitle = view.findViewById<TextView>(R.id.tv_header_title)
        tvTitle.setText(R.string.share_documents)

        bindViews(view)
        selectClientTab()
        callDocumentTypeWebservice(relationshipId, shared_tag, groupsArray, clientId)
    }

    private fun bindViews(root: View) {
        rb_client_doc = root.findViewById(R.id.rb_share_docs_client)
        rb_firm_doc = root.findViewById(R.id.rb_share_docs_firm)

        search_wrapper = root.findViewById(R.id.tl_share_docs_search)
        if (search_wrapper is TextInputLayout) {
            tl_search_layout = search_wrapper as TextInputLayout
        } else {
            tl_search_layout = search_wrapper?.findViewById(R.id.search_tm)
        }

        et_search = root.findViewById(R.id.et_Search)
        applySearchHint()

        chk_select_all = root.findViewById(R.id.chk_share_docs_select_all)
        tv_select_all_label = root.findViewById(R.id.tv_share_docs_select_all)
        tv_selected_count = root.findViewById(R.id.tv_share_docs_count)
        rv_documents = root.findViewById(R.id.rv_share_docs_list)
        tv_no_docs = root.findViewById(R.id.tv_share_docs_no_docs)
        btn_cancel = root.findViewById(R.id.btn_share_docs_cancel)
        btn_share_action = root.findViewById(R.id.btn_share_docs_share)
        btn_share_action?.setText(R.string.share)
        ll_select_all_row = root.findViewById(R.id.ll_share_docs_select_all_row)

        rv_documents?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        rb_client_doc?.setOnClickListener {
            shared_tag = "client"
            selectClientTab()
            callDocumentTypeWebservice(relationshipId, shared_tag, groupsArray, clientId)
        }

        rb_firm_doc?.setOnClickListener {
            shared_tag = "firm"
            selectFirmTab()
            callDocumentTypeWebservice(relationshipId, shared_tag, groupsArray, clientId)
        }

        chk_select_all?.setOnClickListener {
            currentAdapter?.let { adapter ->
                adapter.selectOrDeselectAll(chk_select_all!!.isChecked)
                setSelected_sharedocsList(shared_list)
                updateSelectedCount()
            }
        }

        et_search?.addTextChangedListener(Validation(et_search))
        et_search?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentAdapter?.filter?.filter(s.toString().trim())
            }
        })

        btn_cancel?.setOnClickListener { requireActivity().onBackPressed() }

        btn_share_action?.setOnClickListener {
            if (currentAdapter == null) return@setOnClickListener
            val selectedDocs = currentAdapter!!.selectedList
            if (selectedDocs.isEmpty()) {
                AndroidUtils.showAlert("Please select at least one document", requireActivity())
                return@setOnClickListener
            }

            val savedSelectedIds = ArrayList<String>()
            for (doc in selectedDocs) {
                savedSelectedIds.add(doc.id ?: "")
            }

            try {
                val remove = JSONArray()
                for (doc in selectedDocs) {
                    val o = JSONObject()
                    o.put("docid", doc.id)
                    o.put("doctype", "general")
                    if ("firm" == shared_tag) {
                        o.put("matters", doc.matter_details)
                    }
                    remove.put(o)
                }
                showShareConfirmPopup(ArrayList(selectedDocs), remove, savedSelectedIds)
            } catch (e: JSONException) {
                e.fillInStackTrace()
            }
        }
    }

    private fun applySearchHint() {
        tl_search_layout?.hint = getString(R.string.search_documents)
        et_search?.setHint(R.string.search_documents)
    }

    private fun selectClientTab() {
        if ("solo" != Constants.CATEGORY) {
            rb_firm_doc?.visibility = VISIBLE
            rb_client_doc?.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_left_green_round_background)
            rb_client_doc?.setTextColor(Color.WHITE)
            rb_firm_doc?.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_right_round_background)
            rb_firm_doc?.setTextColor(Color.BLACK)
        } else {
            rb_client_doc?.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounder_button_green)
            rb_client_doc?.setTextColor(Color.WHITE)
            rb_firm_doc?.visibility = GONE
        }
    }

    private fun selectFirmTab() {
        rb_client_doc?.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_left_round_background)
        rb_client_doc?.setTextColor(Color.BLACK)
        rb_firm_doc?.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_right_green_round_background)
        rb_firm_doc?.setTextColor(Color.WHITE)
    }

    private fun callDocumentTypeWebservice(id: String, tag: String, groups: JSONArray?, cid: String) {
        progress_dialog = AndroidUtils.get_progress(requireActivity())
        try {
            val json = JSONObject()
            if ("client" == tag) {
                json.put("category", "client")
                json.put("clients", cid)
                json.put("matters", "all")
                json.put("exclude_already_shared", true)
                json.put("relationship_id", id)
            } else {
                val grps = JSONArray()
                if (groups != null) {
                    for (i in 0 until groups.length()) {
                        grps.put(groups.getJSONObject(i).getString("id"))
                    }
                }
                json.put("category", "firm")
            }
            WebServiceHelper.callHttpWebService(
                this, requireActivity(), WebServiceHelper.RestMethodType.PUT,
                "v3/document/filter", "Existing Documents", json.toString()
            )
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
    }

    private fun callShareDocumentsWebservice(
        id: String,
        remove: JSONArray,
        addDoc: JSONArray,
        message: String
    ) {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val json = JSONObject()

            if ("corporate" == TAG_rel) {
                val corp = JSONObject()
                corp.put("relid", id)
                corp.put("remove", JSONArray())
                corp.put("add", addDoc)
                corp.put("message", message)
                WebServiceHelper.callHttpWebService(
                    this, requireActivity(), WebServiceHelper.RestMethodType.POST,
                    "v3/share", "ShareDocuments", corp.toString()
                )
            } else {
                json.put("remove", JSONArray())
                json.put("add", addDoc)
                json.put("message", message)
                WebServiceHelper.callHttpWebService(
                    this, requireActivity(), WebServiceHelper.RestMethodType.PUT,
                    "v2/relationship/$id/docs/share",
                    "ShareDocuments", json.toString()
                )
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
            dismissProgress()
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        dismissProgress()
        try {
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                val result = JSONObject(httpResult.responseContent)
                val error = result.optBoolean("error", false)
                val msg = result.optString("msg", "")
                val type = httpResult.requestType

                when (type) {
                    "Existing Documents" -> if (!error) loadDocs(result.getJSONArray("data"))
                    "ShareDocuments" -> {
                        AndroidUtils.showAlert(msg, requireActivity())
                        if (!error) {
                            if (alertDialog_confirm != null && alertDialog_confirm!!.isShowing) {
                                alertDialog_confirm!!.dismiss()
                            }
                            requireActivity().onBackPressed()
                        }
                    }
                    else -> Log.d("ShareDocsFragment", "Unhandled type: $type")
                }
            } else {
                try {
                    val err = JSONObject(httpResult.responseContent)
                    if (err.optBoolean("error")) {
                        AndroidUtils.showErrorAlert(err.optString("msg"), requireActivity())
                    }
                } catch (ignored: Exception) {
                    AndroidUtils.showErrorAlert(
                        httpResult.responseContent.toString(), requireActivity()
                    )
                }
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
    }

    private fun loadDocs(docs: JSONArray) {
        shared_list.clear()
        shared_by_us_list.clear()
        for (i in 0 until docs.length()) {
            shared_list.add(buildSharedDoc(docs.getJSONObject(i)))
        }

        val displayList = ArrayList<SharedDocumentsDo>()
        for (d in shared_list) {
            d.isChecked = selected_sharedocsList.contains(d)
            if (!shared_by_us_list.contains(d)) {
                displayList.add(d)
            }
        }

        renderDocList(if (displayList.isEmpty()) shared_list else displayList)
        updateSelectedCount()
    }

    private fun buildSharedDoc(o: JSONObject): SharedDocumentsDo {
        val d = SharedDocumentsDo()
        d.content_type = o.optString("content_type")
        d.created = o.optString("created")
        d.description = o.optString("description")
        d.expiration_date = o.optString("expiration_date")
        d.filename = o.optString("filename")
        d.id = o.optString("id")
        d.is_disabled = o.optBoolean("is_disabled")
        d.is_encrypted = o.optBoolean("is_encrypted")
        d.added_encryption = o.optBoolean("added_encryption")
        d.is_password = o.optBoolean("is_password")
        d.name = o.optString("name")
        d.uploaded_by = o.optString("uploaded_by")
        if (o.has("matter_details")) {
            d.matter_details = o.getJSONArray("matter_details")
            val m = d.matter_details?.optJSONObject(0)
            if (m != null) {
                d.matter_details_name = m.optString("name")
                d.matter_details_id = m.optString("id")
                if (!d.matter_details_name.isNullOrEmpty()) {
                    d.has_Confidential = true
                }
            }
        } else {
            d.has_Confidential = false
        }
        return d
    }

    private fun renderDocList(list: ArrayList<SharedDocumentsDo>) {
        if (list.isEmpty()) {
            tv_no_docs?.visibility = VISIBLE
            rv_documents?.visibility = GONE
            ll_select_all_row?.visibility = GONE
            search_wrapper?.visibility = GONE
        } else {
            tv_no_docs?.visibility = GONE
            rv_documents?.visibility = VISIBLE
            ll_select_all_row?.visibility = VISIBLE
            search_wrapper?.visibility = VISIBLE
            applySearchHint()

            val adapter = SharedDocumentsAdapter(
                list, shared_tag, requireContext(), this,
                relationshipId, clientId, requireActivity(), buildBridgeAdapter(), ""
            )
            currentAdapter = adapter
            rv_documents?.adapter = adapter
        }
    }

    private fun restoreSelections(savedIds: ArrayList<String>) {
        if (currentAdapter == null) return

        for (doc in shared_list) {
            if (savedIds.contains(doc.id)) {
                doc.isChecked = true
            }
        }

        currentAdapter?.notifyDataSetChanged()
        updateSelectedCount()
    }

    private fun showShareConfirmPopup(
        selectedDocs: ArrayList<SharedDocumentsDo>,
        remove: JSONArray,
        savedSelectedIds: ArrayList<String>
    ) {
        try {
            val builder = AlertDialog.Builder(requireContext())
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.share_document_popup, null)

            val tvShareDoc = view.findViewById<TextView>(R.id.tv_share_doc)
            val tvUnshareDoc = view.findViewById<TextView>(R.id.tv_unshare_doc)
            val rvShare = view.findViewById<RecyclerView>(R.id.rv_share_documents)
            val llShare = view.findViewById<LinearLayout>(R.id.ll_share_doc)
            val llUnshare = view.findViewById<LinearLayout>(R.id.ll_unshare_doc)

            tvShareDoc.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            tvUnshareDoc.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            tvShareDoc.setText(R.string.selected_documents)
            tvUnshareDoc.setText(R.string.selected_documents)

            llUnshare.visibility = GONE
            llShare.visibility = VISIBLE

            rvShare.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            rvShare.adapter = UnshareDocumentAdapter(selectedDocs, buildBridgeAdapter())

            val etMsg = view.findViewById<TextInputEditText>(R.id.et_share_message)
            etMsg.visibility = VISIBLE
            val tvPopupTitle = view.findViewById<TextView>(R.id.tv_share_documents)
            tvPopupTitle.setText(R.string.documents_share_unshare)

            val btnCancel = view.findViewById<Button>(R.id.btn_cancel_share)
            val btnOk = view.findViewById<Button>(R.id.btn_ok_share)
            btnOk.setText(R.string.share)
            if ("byme" == shared_tag) {
                tvPopupTitle.setText(R.string.documents_unshare)
                btnOk.setText(R.string.unshare)
            } else {
                tvPopupTitle.setText(R.string.documents_share)
                btnOk.setText(R.string.share)
            }

            val dlg = builder.create()
            alertDialog_confirm = dlg
            dlg.setView(view)
            dlg.setCanceledOnTouchOutside(false)
            dlg.show()
            dlg.window?.setBackgroundDrawableResource(android.R.color.transparent)

            btnCancel.setOnClickListener {
                restoreSelections(savedSelectedIds)
                dlg.dismiss()
            }

            dlg.setOnDismissListener {
                restoreSelections(savedSelectedIds)
            }

            btnOk.setOnClickListener {
                dlg.setOnDismissListener(null)
                val addDoc = JSONArray()
                try {
                    for (doc in selectedDocs) {
                        val o = JSONObject()
                        val matters = JSONArray()
                        if (doc.has_Confidential) {
                            matters.put(doc.matter_details_id)
                        }
                        o.put("docid", doc.id)
                        o.put("doctype", "general")
                        o.put("matters", matters)
                        addDoc.put(o)
                    }
                    val message = etMsg.text.toString().trim()
                    callShareDocumentsWebservice(relationshipId, remove, addDoc, message)
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
                dlg.dismiss()
            }

        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun setSelected_sharedocsList(list: ArrayList<SharedDocumentsDo>) {
        selected_sharedocsList.clear()
        if ("client" == shared_tag) {
            selected_client_list.clear()
            for (d in list) {
                if (d.isChecked) selected_client_list.add(d)
            }
        } else if ("firm" == shared_tag) {
            selected_firm_list.clear()
            for (d in list) {
                if (d.isChecked) selected_firm_list.add(d)
            }
        }
        selected_sharedocsList.addAll(selected_client_list)
        selected_sharedocsList.addAll(selected_firm_list)
        updateSelectedCount()
    }

    fun setSelected_unsharedocsList(list: ArrayList<SharedDocumentsDo>) {
        // Not used in share flow but required by bridge adapter
    }

    fun check_select_all(allSelected: Boolean) {
        chk_select_all?.isChecked = allSelected
        updateSelectedCount()
    }

    private fun updateSelectedCount() {
        var count = 0
        currentAdapter?.let { adapter ->
            val sel = adapter.selectedList
            count = sel.size
        }
        if (count > 0) {
            tv_selected_count?.text = "$count selected"
            tv_selected_count?.visibility = VISIBLE
            btn_share_action?.text = "Share ($count)"
        } else {
            tv_selected_count?.visibility = GONE
            btn_share_action?.text = "Share"
        }
    }

    override fun CopyDocument(doc: SharedDocumentsDo) { /* not used */ }
    override fun viewDocument(doc: SharedDocumentsDo) { /* not used */ }
    override fun onClick(view: View) { /* no-op */ }

    private fun buildBridgeAdapter(): RelationshipsAdapter {
        return object : RelationshipsAdapter(
            ArrayList(), requireContext(), requireActivity(),
            null,
            TAG_rel, null, ""
        ) {
            override fun setSelected_sharedocsList(list: ArrayList<SharedDocumentsDo>) {
                this@ShareDocumentsFragment.setSelected_sharedocsList(list)
            }

            override fun setSelected_unsharedocsList(list: ArrayList<SharedDocumentsDo>) {
                this@ShareDocumentsFragment.setSelected_unsharedocsList(list)
            }

            override fun check_select_all(allSelected: Boolean) {
                this@ShareDocumentsFragment.check_select_all(allSelected)
            }
        }
    }

    private fun dismissProgress() {
        if (progress_dialog != null && progress_dialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissProgress()
    }

    companion object {
        const val ARG_RELATIONSHIP_ID = "rel_id"
        const val ARG_CLIENT_ID = "client_id"
        const val ARG_TAG = "tag"
        const val ARG_GROUPS_JSON = "groups_json"

        @JvmStatic
        fun newInstance(
            relationshipId: String,
            clientId: String,
            tag: String,
            groupsJson: String
        ): ShareDocumentsFragment {
            val args = Bundle()
            args.putString(ARG_RELATIONSHIP_ID, relationshipId)
            args.putString(ARG_CLIENT_ID, clientId)
            args.putString(ARG_TAG, tag)
            args.putString(ARG_GROUPS_JSON, groupsJson)

            val fragment = ShareDocumentsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
