package com.digicoffer.lauditor.Relationships

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
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
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.CommonFiles.PdfUtils.File_Content_Type
import com.digicoffer.lauditor.CommonFiles.PdfUtils.RetrievePDFfromUrl
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Relationships.Adapter.RelationshipsAdapter
import com.digicoffer.lauditor.Relationships.Adapter.SharedDocumentsAdapter
import com.digicoffer.lauditor.Relationships.Adapter.UnshareDocumentAdapter
import com.digicoffer.lauditor.Relationships.Model.ProfileDo
import com.digicoffer.lauditor.Relationships.Model.SharedDocumentsDo
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import java.util.Objects

class ExchangeInformationFragment : Fragment(), AsyncTaskCompleteListener, SharedDocumentsAdapter.EventListener {

    private var tv_name: TextView? = null
    private var tv_email_val: TextView? = null
    private var tv_contact_val: TextView? = null
    private var tv_country_val: TextView? = null
    private var tv_phone_val: TextView? = null
    private var tv_address_val: TextView? = null
    private var tv_website_val: TextView? = null
    private var ll_individual_profile: LinearLayout? = null
    private var ll_entity_profile: LinearLayout? = null
    private var tv_ind_fname: TextView? = null
    private var tv_ind_lname: TextView? = null
    private var tv_ind_email: TextView? = null
    private var tv_ind_mobile: TextView? = null
    private var tv_ind_country: TextView? = null
    private var tv_ind_home: TextView? = null
    private var tv_ind_work: TextView? = null
    private var tv_ind_alt: TextView? = null

    private var tv_toggle_details: TextView? = null
    private var ll_profile_details: LinearLayout? = null

    private var btn_share: ImageView? = null
    private var iv_dropdown: ImageView? = null
    private var share_card: CardView? = null
    private var rb_share_with_me: TextView? = null
    private var rb_share_by_me: TextView? = null
    private var ll_doc_type_row: LinearLayout? = null
    private var rb_client_doc: TextView? = null
    private var rb_firm_doc: TextView? = null
    private var rv_shared_docs: RecyclerView? = null
    private var tv_no_docs: TextView? = null

    private var ll_share_byme_actions: LinearLayout? = null
    private var btn_action_cancel: AppCompatButton? = null
    private var btn_action_share: AppCompatButton? = null

    private var progress_dialog: Dialog? = null

    private var relationshipId = ""
    private var relationshipName = ""
    private var TAG_rel = ""
    private var clientId = ""
    private var isAccepted = false
    private var groupsArray = JSONArray()

    private var shared_tag = "withme"
    private var doc_nature = "withme"
    private var isProfileExpanded = true

    private var currentDocModel: SharedDocumentsDo? = null

    private val shared_list = ArrayList<SharedDocumentsDo>()
    private val shared_by_us_list = ArrayList<SharedDocumentsDo>()
    private val selected_sharedocsList = ArrayList<SharedDocumentsDo>()
    private val selected_unsharedocsList = ArrayList<SharedDocumentsDo>()
    private val selected_client_list = ArrayList<SharedDocumentsDo>()
    private val selected_firm_list = ArrayList<SharedDocumentsDo>()

    private var ad_dialog_docs: AlertDialog? = null
    private var ad_dialog_copy: AlertDialog? = null
    private var alertDialog_confirm: AlertDialog? = null
    private var chk_select_all_ref: CheckBox? = null
    private var currentSharedDocsAdapter: SharedDocumentsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) {
            relationshipId = args.getString(ARG_RELATIONSHIP_ID, "")
            relationshipName = args.getString(ARG_RELATIONSHIP_NAME, "")
            TAG_rel = args.getString(ARG_TAG, "")
            clientId = args.getString(ARG_CLIENT_ID, "")
            isAccepted = args.getBoolean(ARG_IS_ACCEPTED, false)
            val groupsJson = args.getString(ARG_GROUPS_JSON, "")
            try {
                if (!groupsJson.isNullOrEmpty()) {
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
        return inflater.inflate(R.layout.activity_exchange_information, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btn_header_back)
        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        bindProfileViews(view)
        bindSharedDocViews(view)
        callProfileWebservice(relationshipId)
        switchToShareWithMe()
    }

    @SuppressLint("SetTextI18n")
    private fun bindProfileViews(root: View) {
        tv_name = root.findViewById(R.id.tv_exc_name)
        ll_entity_profile = root.findViewById(R.id.ll_exc_entity_profile)
        ll_individual_profile = root.findViewById(R.id.ll_exc_individual_profile)
        ll_profile_details = root.findViewById(R.id.ll_exc_profile_details)
        tv_toggle_details = root.findViewById(R.id.tv_exc_toggle_details)
        iv_dropdown = root.findViewById(R.id.iv_dropdown)

        tv_email_val = root.findViewById(R.id.tv_exc_email_under_name)
        tv_contact_val = root.findViewById(R.id.tv_exc_contact_val)
        tv_country_val = root.findViewById(R.id.tv_exc_country_val)
        tv_phone_val = root.findViewById(R.id.tv_exc_phone_val)
        tv_address_val = root.findViewById(R.id.tv_exc_address_val)
        tv_website_val = root.findViewById(R.id.tv_exc_website_val)

        tv_ind_fname = root.findViewById(R.id.tv_exc_ind_fname)
        tv_ind_lname = root.findViewById(R.id.tv_exc_ind_lname)
        tv_ind_email = root.findViewById(R.id.tv_exc_ind_email)
        tv_ind_mobile = root.findViewById(R.id.tv_exc_ind_mobile)
        tv_ind_country = root.findViewById(R.id.tv_exc_ind_country)
        tv_ind_home = root.findViewById(R.id.tv_exc_ind_home)
        tv_ind_work = root.findViewById(R.id.tv_exc_ind_work)
        tv_ind_alt = root.findViewById(R.id.tv_exc_ind_alt)

        isProfileExpanded = false
        ll_profile_details?.visibility = GONE
        tv_toggle_details?.setText(R.string.view_details)
        iv_dropdown?.rotation = 0f

        tv_toggle_details?.setOnClickListener {
            if (isProfileExpanded) {
                ll_profile_details?.visibility = GONE
                tv_toggle_details?.setText(R.string.view_details)
                iv_dropdown?.rotation = 0f
                isProfileExpanded = false
            } else {
                ll_profile_details?.visibility = VISIBLE
                tv_toggle_details?.setText(R.string.hide_details)
                iv_dropdown?.rotation = 180f
                isProfileExpanded = true
            }
        }
        iv_dropdown?.setOnClickListener {
            if (isProfileExpanded) {
                ll_profile_details?.visibility = GONE
                tv_toggle_details?.setText(R.string.view_details)
                iv_dropdown?.rotation = 0f
                isProfileExpanded = false
            } else {
                ll_profile_details?.visibility = VISIBLE
                tv_toggle_details?.setText(R.string.hide_details)
                iv_dropdown?.rotation = 180f
                isProfileExpanded = true
            }
        }
    }

    private fun bindSharedDocViews(root: View) {
        share_card = root.findViewById(R.id.share_card)
        btn_share = root.findViewById(R.id.btn_exc_share)
        rb_share_with_me = root.findViewById(R.id.rb_exc_share_with_me)
        rb_share_by_me = root.findViewById(R.id.rb_exc_share_by_me)
        ll_doc_type_row = root.findViewById(R.id.ll_exc_doc_type_row)
        rb_client_doc = root.findViewById(R.id.rb_exc_client_doc)
        rb_firm_doc = root.findViewById(R.id.rb_exc_firm_doc)
        rv_shared_docs = root.findViewById(R.id.rv_exc_shared_docs)
        tv_no_docs = root.findViewById(R.id.tv_exc_no_docs)
        ll_share_byme_actions = root.findViewById(R.id.ll_exc_share_byme_actions)
        btn_action_cancel = root.findViewById(R.id.btn_exc_action_cancel)
        btn_action_share = root.findViewById(R.id.btn_exc_action_share)
        btn_action_share?.setText(R.string.unshare)

        rv_shared_docs?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        if (!isAccepted) share_card?.visibility = GONE
        ll_share_byme_actions?.visibility = GONE

        rb_share_with_me?.setOnClickListener { switchToShareWithMe() }

        rb_share_by_me?.setOnClickListener {
            shared_tag = "byme"
            doc_nature = "byme"
            highlightTab(rb_share_with_me!!, rb_share_by_me!!, false)
            ll_doc_type_row?.visibility = VISIBLE
            shared_list.clear()
            shared_by_us_list.clear()
            ll_share_byme_actions?.visibility = VISIBLE
            callSharedDocumentsWebservice(relationshipId, shared_tag, clientId)
        }

        rb_client_doc?.setOnClickListener {
            shared_tag = "client"
            if ("solo" != Constants.CATEGORY) {
                rb_client_doc?.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_left_green_round_background)
                rb_client_doc?.setTextColor(Color.WHITE)
                rb_firm_doc?.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_right_round_background)
                rb_firm_doc?.setTextColor(Color.BLACK)
            } else {
                rb_client_doc?.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounder_button_dark_blue)
                rb_client_doc?.setTextColor(Color.WHITE)
                rb_firm_doc?.visibility = GONE
            }
            callDocumentTypeWebservice(relationshipId, shared_tag, groupsArray, clientId)
        }

        rb_firm_doc?.setOnClickListener {
            shared_tag = "firm"
            rb_client_doc?.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_left_round_background)
            rb_client_doc?.setTextColor(Color.BLACK)
            rb_firm_doc?.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_right_green_round_background)
            rb_firm_doc?.setTextColor(Color.WHITE)
            callDocumentTypeWebservice(relationshipId, shared_tag, groupsArray, clientId)
        }

        btn_share?.setOnClickListener {
            val fragment = ShareDocumentsFragment.newInstance(
                relationshipId,
                clientId,
                TAG_rel,
                groupsArray.toString()
            )
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.id_framelayout, fragment)
                .addToBackStack("current_fragment")
                .commit()
        }

        btn_action_cancel?.setOnClickListener {
            ll_share_byme_actions?.visibility = GONE
            clearDocSelections()
            switchToShareWithMe()
        }

        btn_action_share?.setOnClickListener {
            if (currentSharedDocsAdapter == null) return@setOnClickListener
            val selectedDocs = currentSharedDocsAdapter!!.selectedList
            if (selectedDocs.isEmpty()) {
                AndroidUtils.showAlert("Please select at least one document", requireActivity())
                return@setOnClickListener
            }
            try {
                val remove = JSONArray()
                for (doc in selectedDocs) {
                    val o = JSONObject()
                    o.put("docid", doc.id)
                    o.put("doctype", "general")
                    remove.put(o)
                }
                showShareConfirmPopup(ArrayList(selectedDocs), remove)
            } catch (e: JSONException) {
                e.fillInStackTrace()
            }
        }
    }

    private fun switchToShareWithMe() {
        shared_tag = "withme"
        doc_nature = "withme"
        rb_share_with_me?.let { highlightTab(it, rb_share_by_me!!, true) }
        ll_doc_type_row?.visibility = GONE
        ll_share_byme_actions?.visibility = GONE
        shared_list.clear()
        currentSharedDocsAdapter = null
        callSharedDocumentsWebservice(relationshipId, shared_tag, clientId)
    }

    private fun highlightTab(active: TextView, inactive: TextView, isWithMe: Boolean) {
        if (isWithMe) {
            active.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_left_green_round_background)
            active.setTextColor(Color.WHITE)
            inactive.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_right_round_background)
            inactive.setTextColor(Color.BLACK)
        } else {
            active.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_left_round_background)
            active.setTextColor(Color.BLACK)
            inactive.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_right_green_round_background)
            inactive.setTextColor(Color.WHITE)
        }
    }

    private fun callProfileWebservice(id: String) {
        val json = JSONObject()
        if ("corporate" == TAG_rel) {
            WebServiceHelper.callHttpWebService(
                this, requireActivity(), WebServiceHelper.RestMethodType.GET,
                "v3/profile/$id", "Profile", json.toString()
            )
        } else {
            WebServiceHelper.callHttpWebService(
                this, requireActivity(), WebServiceHelper.RestMethodType.GET,
                "v2/relationship/$id/profile", "Profile", json.toString()
            )
        }
    }

    private fun callSharedDocumentsWebservice(id: String, tag: String, cid: String) {
        progress_dialog = AndroidUtils.get_progress(requireActivity())
        val json = JSONObject()
        if ("corporate" == TAG_rel) {
            WebServiceHelper.callHttpWebService(
                this, requireActivity(), WebServiceHelper.RestMethodType.GET,
                "v3/share/$id/$tag", "Shared Corp Documents", json.toString()
            )
        } else {
            WebServiceHelper.callHttpWebService(
                this, requireActivity(), WebServiceHelper.RestMethodType.GET,
                "v2/relationship/$id/docs/shared/$tag",
                "Shared Documents", json.toString()
            )
        }
    }

    private fun callDocumentTypeWebservice(id: String, tag: String, groups: JSONArray?, cid: String) {
        progress_dialog = AndroidUtils.get_progress(requireActivity())
        try {
            val json = JSONObject()
            if ("client" == tag) {
                json.put("category", "client")
                json.put("clients", cid)
                json.put("matters", "all")
            } else {
                val grps = JSONArray()
                if (groups != null) {
                    for (i in 0 until groups.length()) {
                        grps.put(groups.getJSONObject(i).getString("id"))
                    }
                }
                json.put("category", "firm")
                json.put("groups", grps)
            }
            WebServiceHelper.callHttpWebService(
                this, requireActivity(), WebServiceHelper.RestMethodType.PUT,
                "v3/document/filter", "Existing Documents", json.toString()
            )
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
    }

    fun View_doc(doc_id: String, docModel: SharedDocumentsDo) {
        progress_dialog = AndroidUtils.get_progress(requireActivity())
        this.currentDocModel = docModel
        val json = JSONObject()
        if ("withme" == shared_tag) {
            if ("corporate" == TAG_rel || "business" == TAG_rel) {
                WebServiceHelper.callHttpWebService(
                    this, requireActivity(), WebServiceHelper.RestMethodType.GET,
                    "v3/document/$doc_id/view", "View Other Doc", json.toString()
                )
            } else {
                WebServiceHelper.callHttpWebService(
                    this, requireActivity(), WebServiceHelper.RestMethodType.GET,
                    "v2/relationship/$relationshipId/$doc_id/view",
                    "View Doc With Us", json.toString()
                )
            }
        } else {
            WebServiceHelper.callHttpWebService(
                this, requireActivity(), WebServiceHelper.RestMethodType.GET,
                "v3/document/$doc_id/view", "View Other Doc", json.toString()
            )
        }
    }

    fun callDecryptApi(id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val json = JSONObject()
            json.put("docid", id)
            if ("withme" == shared_tag) {
                json.put("shared_doc", true)
            }
            WebServiceHelper.callHttpWebService(
                this, requireActivity(), WebServiceHelper.RestMethodType.POST,
                Constants.decryptUrl ?: "", "Decrypt Doc", json.toString()
            )
        } catch (e: JSONException) {
            dismissProgress()
        }
    }

    fun callOtherDocViewApi(id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(requireActivity())
            val json = JSONObject()
            WebServiceHelper.callHttpWebService(
                this, requireActivity(), WebServiceHelper.RestMethodType.GET,
                Constants.base_URL + "v3/document/" + id + "/view",
                "Other Doc View", json.toString()
            )
        } catch (e: Exception) {
            dismissProgress()
        }
    }

    @Throws(JSONException::class)
    fun callUnsharedDocumentWebservice(
        id: String,
        remove: JSONArray,
        message: String,
        cid: String,
        isRemove: Boolean
    ) {
        progress_dialog = AndroidUtils.get_progress(requireActivity())
        val json = JSONObject()
        val removeDoc = JSONArray()
        val addDoc = JSONArray()

        for (doc in selected_unsharedocsList) {
            val o = JSONObject()
            o.put("docid", doc.id)
            o.put("doctype", "general")
            removeDoc.put(o)
        }
        for (doc in selected_sharedocsList) {
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

        if (remove.length() != 0) {
            if ("byme" == shared_tag) {
                json.put("remove", if (isRemove) remove else removeDoc)
                json.put("add", addDoc)
            } else {
                json.put("remove", removeDoc)
                json.put("add", addDoc)
            }
            json.put("message", message)

            if ("corporate" == TAG_rel) {
                val corp = JSONObject()
                corp.put("relid", id)
                corp.put("remove", if (isRemove) remove else removeDoc)
                corp.put("add", addDoc)
                corp.put("message", message)
                WebServiceHelper.callHttpWebService(
                    this, requireActivity(), WebServiceHelper.RestMethodType.POST,
                    "v3/share", "UnshareDocuments", corp.toString()
                )
            } else {
                WebServiceHelper.callHttpWebService(
                    this, requireActivity(), WebServiceHelper.RestMethodType.PUT,
                    "v2/relationship/$id/docs/share",
                    "UnshareDocuments", json.toString()
                )
            }
        } else {
            AndroidUtils.showAlert("Please select at least one document", requireActivity())
        }
    }

    override fun onClick(view: View) { /* no-op */ }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        dismissProgress()
        try {
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                val result = JSONObject(httpResult.responseContent)
                val error = result.optBoolean("error", false)
                val msg = result.optString("msg", "")
                val type = httpResult.requestType

                when (type) {
                    "Profile" -> if (!error) loadProfile(result.getJSONObject("data"))
                    "Shared Corp Documents", "Shared Documents" -> {
                        if (!error) loadSharedWithMeDocs(result.getJSONObject("documents"))
                    }
                    "Existing Documents" -> if (!error) loadOtherDocs(result.getJSONArray("data"))
                    "View Doc With Us", "View Other Doc" -> {
                        if (!error) {
                            val url = if (type == "View Doc With Us") {
                                result.getString("url")
                            } else {
                                result.getJSONObject("data").getString("url")
                            }
                            enrichContentType(url)
                            checkViewType(url, currentDocModel!!)
                        } else {
                            AndroidUtils.showAlert(msg, requireActivity())
                        }
                    }
                    "Decrypt Doc", "Other Doc View" -> {
                        if (!error) {
                            val url = result.getJSONObject("data").getString("url")
                            display_doc(url, currentDocModel!!)
                        } else {
                            AndroidUtils.showAlert(msg, requireActivity())
                        }
                    }
                    "UnshareDocuments" -> {
                        AndroidUtils.showAlert(msg, requireActivity())
                        clearDocSelections()
                        ll_share_byme_actions?.visibility = GONE
                        if (ad_dialog_docs != null && ad_dialog_docs!!.isShowing) {
                            ad_dialog_docs!!.dismiss()
                        }
                        if (alertDialog_confirm != null && alertDialog_confirm!!.isShowing) {
                            alertDialog_confirm!!.dismiss()
                        }
                        callSharedDocumentsWebservice(relationshipId, shared_tag, clientId)
                    }
                    else -> Log.d("ExchangeInfoFrag", "Unhandled type: $type")
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

    @SuppressLint("SetTextI18n")
    private fun loadProfile(data: JSONObject) {
        val p = ProfileDo()
        if ("individuals" != TAG_rel) {
            p.fullname = data.optString("fullname")
            p.email = data.optString("email")
            p.contact_person = data.optString("contact_person")
            p.contact_phone = data.optString("contact_phone")
            p.website = data.optString("website")
            if (data.has("address")) {
                val addr = data.getJSONObject("address")
                p.country = addr.optString("country")
                p.house_flat_no = addr.optString("house_flat_no")
                p.street = addr.optString("street")
                p.city_town = addr.optString("city_town")
                p.state = addr.optString("state")
                p.zipcode = addr.optString("zipcode")
            }

            ll_individual_profile?.visibility = GONE
            ll_entity_profile?.visibility = VISIBLE

            tv_name?.text = p.fullname
            tv_email_val?.text = p.email
            tv_contact_val?.text = p.contact_person
            tv_country_val?.text = p.country
            val phone = p.contact_phone
            tv_phone_val?.text = if ("null" == phone) "" else phone

            val addr = StringBuilder()
            if (!isNullOrEmpty(p.house_flat_no)) addr.append(p.house_flat_no)
            if (!isNullOrEmpty(p.street)) addr.append(", ").append(p.street)
            if (!isNullOrEmpty(p.city_town)) addr.append(", ").append(p.city_town)
            if (!isNullOrEmpty(p.state)) addr.append(", ").append(p.state)
            if (!isNullOrEmpty(p.country)) addr.append(", ").append(p.country)
            if (!isNullOrEmpty(p.zipcode)) addr.append(" ").append(p.zipcode)
            tv_address_val?.text = addr.toString()
            tv_website_val?.text = p.website

        } else {
            p.first_name = data.optString("first_name")
            p.last_name = data.optString("last_name")
            p.fullname = data.optString("first_name") + " " + data.optString("last_name")
            p.email = data.optString("email")
            p.mobile = data.optString("mobile")
            val citizen = data.optJSONArray("citizen")
            if (citizen != null) {
                for (i in 0 until citizen.length()) {
                    val c = citizen.getJSONObject(i)
                    if ("citizen_primary" == c.optString("index")) {
                        p.country = c.optString("country")
                        p.home_address = c.optString("home_address")
                        p.work_phone = c.optString("work_phone")
                        p.alt_phone = c.optString("alt_phone")
                        break
                    }
                }
            }

            ll_entity_profile?.visibility = GONE
            ll_individual_profile?.visibility = VISIBLE

            tv_ind_fname?.text = p.first_name
            tv_ind_lname?.text = p.last_name
            tv_ind_email?.text = p.email
            val mob = p.mobile
            tv_ind_mobile?.text = if ("null" == mob) "" else mob
            tv_ind_country?.text = p.country
            tv_ind_home?.text = p.home_address
            tv_ind_work?.text = p.work_phone
            tv_ind_alt?.text = p.alt_phone
            tv_name?.text = p.fullname
            tv_email_val?.text = p.email
        }
    }

    private fun loadSharedWithMeDocs(documents: JSONObject) {
        try {
            shared_list.clear()
            processDocArray(documents.getJSONArray("general"))
            processDocArray(documents.getJSONArray("credential"))
            processDocArray(documents.getJSONArray("merged"))
            processDocArray(documents.getJSONArray("versioned"))
            processDocArray(documents.getJSONArray("identity"))
            processDocArray(documents.getJSONArray("personal"))

            shared_by_us_list.clear()
            shared_by_us_list.addAll(shared_list)

            if ("byme" == doc_nature) {
                val list = ArrayList<SharedDocumentsDo>()
                for (d in shared_list) {
                    d.isChecked = selected_unsharedocsList.contains(d)
                    list.add(d)
                }
                renderDocList(list)
            } else if ("withme" == doc_nature) {
                val list = ArrayList<SharedDocumentsDo>(shared_list)
                renderDocList(list)
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
    }

    private fun processDocArray(arr: JSONArray) {
        for (i in 0 until arr.length()) {
            shared_list.add(buildSharedDoc(arr.getJSONObject(i)))
        }
    }

    private fun loadOtherDocs(docs: JSONArray) {
        shared_list.clear()
        for (i in 0 until docs.length()) {
            shared_list.add(buildSharedDoc(docs.getJSONObject(i)))
        }

        val diff = ArrayList<SharedDocumentsDo>()
        for (d in shared_list) {
            d.isChecked = selected_sharedocsList.contains(d)
            if (!shared_by_us_list.contains(d)) {
                diff.add(d)
            }
        }

        openDocumentPickerPopup(
            if ("firm" == shared_tag || "client" == shared_tag) diff else shared_list
        )
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
            rv_shared_docs?.visibility = GONE
        } else {
            tv_no_docs?.visibility = GONE
            rv_shared_docs?.visibility = VISIBLE
            val adapter = SharedDocumentsAdapter(
                list, shared_tag, requireContext(), this,
                relationshipId, clientId, requireActivity(), buildBridgeAdapter(), ""
            )
            currentSharedDocsAdapter = adapter
            rv_shared_docs?.adapter = adapter
        }
    }

    private fun openDocumentPickerPopup(docList: ArrayList<SharedDocumentsDo>) {
        try {
            val builder = AlertDialog.Builder(requireContext())
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.shared_document_recyclerview_popup, null)

            val rv = view.findViewById<RecyclerView>(R.id.rv_relationship_documents)
            val etSearch = view.findViewById<TextInputEditText>(R.id.et_search_relationships)
            etSearch.addTextChangedListener(Validation(etSearch))

            val ll_buttons = view.findViewById<LinearLayout>(R.id.ll_buttons)
            val ll_select_all = view.findViewById<LinearLayout>(R.id.ll_select_all)
            val tv_select_all = view.findViewById<TextView>(R.id.tv_select_all)
            tv_select_all.setText(R.string.select_all)
            chk_select_all_ref = view.findViewById(R.id.chk_select_all)
            val tvHeader = view.findViewById<TextView>(R.id.header_name)
            val tvMsg = view.findViewById<TextView>(R.id.message)
            tvMsg.gravity = Gravity.CENTER
            tvMsg.setText(R.string.no_documents_to_show)
            tvMsg.textSize = DynamicUtils.twenty.toFloat()
            val btnShare = view.findViewById<AppCompatButton>(R.id.btn_send_request)
            val btnCancel = view.findViewById<AppCompatButton>(R.id.btn_relationships_cancel)
            val ivClose = view.findViewById<ImageView>(R.id.close_edit_docs)

            var header = ""
            var btnTextRes = R.string.share
            when (shared_tag) {
                "withme" -> {
                    header = "Documents Shared With Us"
                    ll_select_all.visibility = GONE
                    ll_buttons.visibility = GONE
                }
                "byme" -> {
                    header = "Documents Shared By Us"
                    ll_select_all.visibility = VISIBLE
                    ll_buttons.visibility = VISIBLE
                    btnTextRes = R.string.unshare
                }
                "client" -> {
                    ll_select_all.visibility = VISIBLE
                    header = "Client Documents"
                }
                else -> {
                    ll_select_all.visibility = VISIBLE
                    header = "Firm Documents"
                }
            }
            tvHeader.text = header
            btnShare.setText(btnTextRes)

            if (docList.isEmpty()) {
                ll_buttons.visibility = GONE
                etSearch.visibility = GONE
            } else {
                rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                val adapter = SharedDocumentsAdapter(
                    docList, shared_tag, requireContext(), this,
                    relationshipId, clientId, requireActivity(), buildBridgeAdapter(), ""
                )
                rv.adapter = adapter

                chk_select_all_ref?.setOnClickListener {
                    adapter.selectOrDeselectAll(chk_select_all_ref!!.isChecked)
                    if ("byme" == shared_tag) {
                        setSelected_unsharedocsList(docList)
                    } else {
                        setSelected_sharedocsList(docList)
                    }
                }

                etSearch.setHint(R.string.search_documents)
                etSearch.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        adapter.filter.filter(s.toString().trim())
                    }
                })

                val closeListener = View.OnClickListener {
                    etSearch.setText("")
                    clearDocSelections()
                    ad_dialog_docs?.dismiss()
                }
                btnCancel.setOnClickListener(closeListener)
                ivClose.setOnClickListener(closeListener)

                btnShare.setOnClickListener {
                    try {
                        val selectedDocs = adapter.selectedList
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
                        if (selectedDocs.isNotEmpty()) {
                            etSearch.setText("")
                            showShareConfirmPopup(ArrayList(selectedDocs), remove)
                        } else {
                            Log.d("ExchangeInfoFrag", "No document selected")
                        }
                    } catch (e: Exception) {
                        e.fillInStackTrace()
                    }
                }
            }

            val dlg = builder.create()
            ad_dialog_docs = dlg
            dlg.setView(view)
            dlg.setCanceledOnTouchOutside(false)
            dlg.show()

        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    private fun showShareConfirmPopup(updatedList: ArrayList<SharedDocumentsDo>, remove: JSONArray) {
        try {
            val builder = AlertDialog.Builder(requireContext())
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.share_document_popup, null)

            val tvShareDoc = view.findViewById<TextView>(R.id.tv_share_doc)
            val tvUnshareDoc = view.findViewById<TextView>(R.id.tv_unshare_doc)
            val rvShare = view.findViewById<RecyclerView>(R.id.rv_share_documents)
            val rvUnshare = view.findViewById<RecyclerView>(R.id.rv_unshare_documents)
            val llShare = view.findViewById<LinearLayout>(R.id.ll_share_doc)
            val llUnshare = view.findViewById<LinearLayout>(R.id.ll_unshare_doc)

            tvShareDoc.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            tvUnshareDoc.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            tvUnshareDoc.setText(R.string.selected_documents)
            tvShareDoc.setText(R.string.selected_documents)

            llUnshare.visibility = if (selected_unsharedocsList.isEmpty()) GONE else VISIBLE
            llShare.visibility = if (selected_sharedocsList.isEmpty()) GONE else VISIBLE

            setupSimpleRecycler(rvShare, selected_sharedocsList)
            setupSimpleRecycler(rvUnshare, selected_unsharedocsList)

            val etMsg = view.findViewById<TextInputEditText>(R.id.et_share_message)
            etMsg.visibility = VISIBLE
            val tvTitle = view.findViewById<TextView>(R.id.tv_share_documents)

            val btnCancel = view.findViewById<Button>(R.id.btn_cancel_share)
            val btnOk = view.findViewById<Button>(R.id.btn_ok_share)
            btnOk.setText(R.string.share)
            if ("byme" == shared_tag) {
                tvTitle.setText(R.string.documents_unshare)
                btnOk.setText(R.string.unshare)
            } else {
                tvTitle.setText(R.string.documents_share)
                btnOk.setText(R.string.share)
            }
            val dlg = builder.create()
            alertDialog_confirm = dlg
            dlg.setView(view)
            dlg.setCanceledOnTouchOutside(false)
            dlg.show()

            btnCancel.setOnClickListener { dlg.dismiss() }
            btnOk.setOnClickListener {
                try {
                    callUnsharedDocumentWebservice(
                        relationshipId, remove,
                        etMsg.text.toString().trim(),
                        clientId, false
                    )
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
                dlg.dismiss()
            }

        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun setupSimpleRecycler(rv: RecyclerView, list: ArrayList<SharedDocumentsDo>) {
        rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rv.adapter = UnshareDocumentAdapter(list, buildBridgeAdapter())
    }

    private fun checkViewType(url: String, doc: SharedDocumentsDo) {
        val isEncrypted = doc.added_encryption || doc.is_encrypted
        if (isEncrypted) {
            callDecryptApi(doc.id ?: "")
        } else if (!File_Content_Type.isPDF(doc.content_type) && !File_Content_Type.isImage(doc.content_type)) {
            callOtherDocViewApi(doc.id ?: "")
        } else {
            display_doc(url, doc)
        }
    }

    private fun display_doc(url: String, doc: SharedDocumentsDo) {
        val builder = AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.view_documents, null)
        val pdfView = view.findViewById<PDFView>(R.id.idPDFView)
        val ivImg = view.findViewById<ImageView>(R.id.doc_image)
        val pb = view.findViewById<android.widget.ProgressBar>(R.id.progress_pdf)
        val header = view.findViewById<TextView>(R.id.header_name)
        val ivClose = view.findViewById<ImageView>(R.id.close_edit_docs)
        header.text = doc.name

        val isImage = File_Content_Type.isImage(doc.content_type)
        val urlIsPDF = url.lowercase().contains("application/pdf") || url.lowercase().contains(".pdf")

        val dlg = builder.create()
        val task = arrayOfNulls<RetrievePDFfromUrl>(1)

        ivClose.setOnClickListener {
            try {
                pdfView?.recycle()
                if (task[0] != null) {
                    task[0]!!.cancelLoading()
                    task[0]!!.cancel(true)
                }
            } catch (ignored: Exception) {
            }
            dlg.dismiss()
        }

        if (urlIsPDF) {
            pdfView.visibility = VISIBLE
            pb.visibility = VISIBLE
            task[0] = RetrievePDFfromUrl(pdfView, pb)
            task[0]!!.execute(url)
        } else if (isImage) {
            ivImg.visibility = VISIBLE
            com.bumptech.glide.Glide.with(this).load(url)
                .placeholder(R.drawable.progress_animation).centerCrop().into(ivImg)
        } else {
            pdfView.visibility = VISIBLE
            pb.visibility = VISIBLE
            task[0] = RetrievePDFfromUrl(pdfView, pb)
            task[0]!!.execute(url)
        }

        dlg.setCancelable(false)
        dlg.setCanceledOnTouchOutside(false)
        dlg.setView(view)
        dlg.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dlg.show()
    }

    override fun CopyDocument(doc: SharedDocumentsDo) {
        val b = AlertDialog.Builder(requireContext())
        val v = LayoutInflater.from(requireContext()).inflate(R.layout.delete_relationship, null)
        (v.findViewById<View>(R.id.tv_confirmation) as TextView).text = "Are you sure you want to copy " + doc.name + "?"
        val btnYes = v.findViewById<AppCompatButton>(R.id.btn_yes)
        val btnNo = v.findViewById<AppCompatButton>(R.id.btn_No)
        val dlg = b.create()
        ad_dialog_copy = dlg
        dlg.setView(v)
        dlg.setCanceledOnTouchOutside(false)
        dlg.show()
        btnNo.setOnClickListener { dlg.dismiss() }
        btnYes.setOnClickListener {
            val json = JSONObject()
            WebServiceHelper.callHttpWebService(
                this, requireActivity(), WebServiceHelper.RestMethodType.GET,
                "v2/relationship/" + relationshipId + "/" + doc.id + "/copy",
                "Copy Document", json.toString()
            )
            dlg.dismiss()
        }
    }

    override fun viewDocument(doc: SharedDocumentsDo) {
        View_doc(doc.id ?: "", doc)
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
    }

    fun setSelected_unsharedocsList(list: ArrayList<SharedDocumentsDo>) {
        selected_unsharedocsList.clear()
        for (d in list) {
            if (d.isChecked) selected_unsharedocsList.add(d)
        }
    }

    fun check_select_all(allSelected: Boolean) {
        chk_select_all_ref?.isChecked = allSelected
    }

    private fun clearDocSelections() {
        shared_list.clear()
        selected_unsharedocsList.clear()
        selected_sharedocsList.clear()
        selected_firm_list.clear()
        selected_client_list.clear()
        currentSharedDocsAdapter = null
    }

    private fun buildBridgeAdapter(): RelationshipsAdapter {
        return object : RelationshipsAdapter(
            ArrayList(), requireContext(), requireActivity(),
            null,
            TAG_rel, null, ""
        ) {
            override fun View_doc(doc_id: String, docModel: SharedDocumentsDo) {
                currentDocModel = docModel
                this@ExchangeInformationFragment.View_doc(doc_id, docModel)
            }

            override fun callDecryptApi(id: String) {
                this@ExchangeInformationFragment.callDecryptApi(id)
            }

            override fun callOtherDocViewApi(id: String) {
                this@ExchangeInformationFragment.callOtherDocViewApi(id)
            }

            override fun callUnsharedDocumentWebservice(
                id: String, remove: JSONArray, message: String,
                cid: String, isRemove: Boolean
            ) {
                try {
                    this@ExchangeInformationFragment
                        .callUnsharedDocumentWebservice(id, remove, message, cid, isRemove)
                } catch (e: JSONException) {
                    e.fillInStackTrace()
                }
            }

            override fun setSelected_sharedocsList(list: ArrayList<SharedDocumentsDo>) {
                this@ExchangeInformationFragment.setSelected_sharedocsList(list)
            }

            override fun setSelected_unsharedocsList(list: ArrayList<SharedDocumentsDo>) {
                this@ExchangeInformationFragment.setSelected_unsharedocsList(list)
            }

            override fun check_select_all(allSelected: Boolean) {
                this@ExchangeInformationFragment.check_select_all(allSelected)
            }
        }
    }

    private fun dismissProgress() {
        if (progress_dialog != null && progress_dialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }
    }

    private fun isNullOrEmpty(s: String?): Boolean {
        return s == null || s.isEmpty() || "null" == s
    }

    private fun enrichContentType(url: String) {
        if (currentDocModel == null) return
        val name = currentDocModel!!.filename ?: return
        val ct = com.digicoffer.lauditor.Documents.Documents.replaceLastDotWithSlash(name)
        val parts = ct.split("/")
        if (parts.size >= 2) {
            val ext = parts[1]
            val isImg = ext.matches("(?i)apng|avif|gif|jpeg|png|svg|webp|jpg".toRegex())
            currentDocModel!!.doctype = (if (isImg) "image/" else "application/") + ext
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissProgress()
    }

    companion object {
        const val ARG_RELATIONSHIP_ID = "rel_id"
        const val ARG_RELATIONSHIP_NAME = "rel_name"
        const val ARG_TAG = "tag"
        const val ARG_CLIENT_ID = "client_id"
        const val ARG_IS_ACCEPTED = "is_accepted"
        const val ARG_GROUPS_JSON = "groups_json"

        @JvmStatic
        fun newInstance(
            relationshipId: String,
            relationshipName: String,
            tag: String,
            clientId: String,
            isAccepted: Boolean,
            groupsJson: String
        ): ExchangeInformationFragment {
            val args = Bundle()
            args.putString(ARG_RELATIONSHIP_ID, relationshipId)
            args.putString(ARG_RELATIONSHIP_NAME, relationshipName)
            args.putString(ARG_TAG, tag)
            args.putString(ARG_CLIENT_ID, clientId)
            args.putBoolean(ARG_IS_ACCEPTED, isAccepted)
            args.putString(ARG_GROUPS_JSON, groupsJson)

            val fragment = ExchangeInformationFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
