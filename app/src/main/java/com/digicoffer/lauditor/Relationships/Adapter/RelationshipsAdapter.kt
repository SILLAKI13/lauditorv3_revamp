package com.digicoffer.lauditor.Relationships.Adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.CommonFiles.PdfUtils.File_Content_Type
import com.digicoffer.lauditor.CommonFiles.PdfUtils.RetrievePDFfromUrl
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.digicoffer.lauditor.Documents.Documents
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.Members.Adapters.GroupsAdapter
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Relationships.ClientRelationship
import com.digicoffer.lauditor.Relationships.GroupRecyclerViewFragment
import com.digicoffer.lauditor.Relationships.MemberModel
import com.digicoffer.lauditor.Relationships.Model.ProfileDo
import com.digicoffer.lauditor.Relationships.Model.RelationshipsModel
import com.digicoffer.lauditor.Relationships.Model.SharedDocumentsDo
import com.digicoffer.lauditor.Relationships.ExchangeInformationFragment
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import java.util.Objects
import java.util.concurrent.atomic.AtomicBoolean

open class RelationshipsAdapter : RecyclerView.Adapter<RelationshipsAdapter.MyViewHolder>, Filterable, AsyncTaskCompleteListener, SharedDocumentsAdapter.EventListener {
    override fun onClick(view: View) {}
    var relationshipsList: ArrayList<RelationshipsModel> = ArrayList()
    var itemsList: ArrayList<RelationshipsModel> = ArrayList()
    var citizenList: ArrayList<ProfileDo> = ArrayList()
    var position: Int = 0
    var membersAdapter: MemberSelectionAdapter? = null
    var groupsList1: ArrayList<ViewGroupModel>? = null
    var isCancel: Boolean = false
    @JvmField var popupView: View? = null
    var context: Context
    @JvmField var et_search_groups: EditText? = null
    var btn_Delete: AppCompatButton? = null
    var acls: JSONArray = JSONArray()
    var oldGrouplist: ArrayList<ViewGroupModel> = ArrayList()
    var groupsAdapter: GroupsAdapter? = null
    var new_groups: JSONArray = JSONArray()
    var chk_select_all: CheckBox? = null
    var dialog: AlertDialog? = null
    @JvmField var isUpdated: AtomicBoolean = AtomicBoolean(false)
    var sharedDocumentsDo: SharedDocumentsDo? = null
    var updated_shared_list: ArrayList<SharedDocumentsDo> = ArrayList()
    var shared_by_us_list: ArrayList<SharedDocumentsDo> = ArrayList()
    var firm_list: ArrayList<SharedDocumentsDo> = ArrayList()
    var alertDialog: AlertDialog? = null
    var progress_dialog: Dialog? = null
    private var openActionPosition: Int = -1
    @JvmField var btn_send_request: AppCompatButton? = null
    var mcontext: Context
    var tvShareDocuments: TextView? = null
    var updatedMembersList: ArrayList<ViewGroupModel> = ArrayList()
    var updateMembersList: ArrayList<MemberModel> = ArrayList()
    var selectedMembersList: ArrayList<MemberModel> = ArrayList()
    @JvmField var selected_sharedocsList: ArrayList<SharedDocumentsDo> = ArrayList()
    var selected_client_sharedocsList: ArrayList<SharedDocumentsDo> = ArrayList()
    var selected_firm_sharedocsList: ArrayList<SharedDocumentsDo> = ArrayList()
    @JvmField var selected_unsharedocsList: ArrayList<SharedDocumentsDo> = ArrayList()
    var shared_list: ArrayList<SharedDocumentsDo> = ArrayList()
    var updatedshared_list: ArrayList<SharedDocumentsDo> = ArrayList()
    var mholder: MyViewHolder? = null
    var relationshipsModel_new: RelationshipsModel? = null
    var groupsList: ArrayList<ViewGroupModel> = ArrayList()
    var ad_dialog: AlertDialog? = null
    var ad_dialog_delete: AlertDialog? = null
    var ad_dialog_copy: AlertDialog? = null
    var ad_dialog_docs: AlertDialog? = null
    var mParent: ViewGroup? = null
    var shared_tag: String = ""
    var doc_nature: String = ""
    private var shared_relationship_id: String = ""
    private var shared_client_id: String = ""
    var mActivity: FragmentActivity
    var eventListener: EventListener?
    private var relationshipmodel_profile: RelationshipsModel? = null
    private var FLAG: String = ""
    var view: View? = null
    var groupname: String = ""
    var groupid: String = ""
    var route: String = ""
    private var mViewModel: NewModel? = null
    private var TAG: String
    private var highLightId: String = ""
    var clientRelationship: ClientRelationship? = null
    private var expandedPosition: Int = -1
    private var isPopupView: Boolean = false

    constructor(
        relationshipsList: ArrayList<RelationshipsModel>,
        context: Context,
        activity: FragmentActivity,
        listener: EventListener?,
        TAG: String,
        clientRelationship: ClientRelationship?,
        highLightId: String
    ) {
        this.relationshipsList = relationshipsList
        this.clientRelationship = clientRelationship
        this.TAG = TAG
        this.itemsList = relationshipsList
        this.mcontext = context
        this.mActivity = activity
        this.eventListener = listener
        this.isPopupView = false
        this.highLightId = highLightId
        this.context = context
    }

    constructor(
        relationshipsList: ArrayList<RelationshipsModel>,
        context: Context,
        activity: FragmentActivity,
        listener: EventListener?,
        TAG: String,
        clientRelationship: ClientRelationship?,
        highLightId: String,
        route: String,
        isPopupView: Boolean
    ) {
        this.relationshipsList = relationshipsList
        this.clientRelationship = clientRelationship
        this.TAG = TAG
        this.itemsList = relationshipsList
        this.mcontext = context
        this.mActivity = activity
        this.eventListener = listener
        this.isPopupView = isPopupView
        this.route = route
        this.highLightId = highLightId
        this.context = context
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val query = charSequence.toString().trim()
                val resultList: ArrayList<RelationshipsModel>
                if (query.isEmpty()) {
                    resultList = ArrayList(itemsList)
                } else {
                    resultList = ArrayList()
                    for (row in itemsList) {
                        if (AndroidUtils.isNull(row.name)
                                .lowercase(Locale.ROOT)
                                .contains(query.lowercase(Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }
                }
                val results = FilterResults()
                results.values = resultList
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence, results: FilterResults) {
                relationshipsList = results.values as ArrayList<RelationshipsModel>
                notifyDataSetChanged()
            }
        }
    }

    private fun Send_Invite(relationshipsModel: RelationshipsModel) {
        try {
            val dialogBuilder = AlertDialog.Builder(mcontext)
            val inflater = mActivity.layoutInflater
            val view = inflater.inflate(R.layout.delete_relationship, null)
            val tv_confirmation = view.findViewById<TextView>(R.id.tv_confirmation)
            val close_documents = view.findViewById<ImageView>(R.id.close_documents)
            val invite_txt = "Are you sure you want send relationship invite to " + relationshipsModel.name + "."
            tv_confirmation.text = invite_txt
            val bt_yes = view.findViewById<AppCompatButton>(R.id.btn_yes)
            val btn_no = view.findViewById<AppCompatButton>(R.id.btn_No)
            close_documents.setOnClickListener { ad_dialog_copy?.dismiss() }
            btn_no.setOnClickListener { ad_dialog_copy?.dismiss() }
            bt_yes.setOnClickListener {
                callInviteTempClients(relationshipsModel)
                ad_dialog_copy?.dismiss()
            }
            val dialog = dialogBuilder.create()
            ad_dialog_copy = dialog
            dialog.setView(view)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, mActivity)
        }
    }

    override fun CopyDocument(sharedDocumentsDo: SharedDocumentsDo) {
        try {
            val dialogBuilder = AlertDialog.Builder(mcontext)
            val inflater = mActivity.layoutInflater
            val view = inflater.inflate(R.layout.delete_relationship, null)
            val tv_confirmation = view.findViewById<TextView>(R.id.tv_confirmation)
            tv_confirmation.text = "Are you sure you want to copy " + sharedDocumentsDo.name + "?"
            val bt_yes = view.findViewById<AppCompatButton>(R.id.btn_yes)
            val btn_no = view.findViewById<AppCompatButton>(R.id.btn_No)
            btn_no.setTextColor(mcontext.getColor(R.color.white))
            bt_yes.setTextColor(mcontext.getColor(R.color.black))
            bt_yes.background = mcontext.getDrawable(R.drawable.yes_button_red_button)
            btn_no.background = mcontext.getDrawable(R.drawable.no_button_green_button)
            btn_no.setOnClickListener { ad_dialog_copy?.dismiss() }
            bt_yes.setOnClickListener {
                val action = "copy"
                callCopyDocumentWebservice(sharedDocumentsDo, action)
                ad_dialog_copy?.dismiss()
            }
            val dialog = dialogBuilder.create()
            ad_dialog_copy = dialog
            dialog.setView(view)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, mActivity)
        }
    }

    override fun viewDocument(sharedDocumentsDo: SharedDocumentsDo) {
        val action = "view"
        callCopyDocumentWebservice(sharedDocumentsDo, action)
    }

    private fun callCopyDocumentWebservice(sharedDocumentsDo: SharedDocumentsDo, action: String) {
        val jsonObject = JSONObject()
        val request = if (Objects.equals(action, "copy")) {
            "Copy Document"
        } else {
            "View Document"
        }
        WebServiceHelper.callHttpWebService(
            this, mcontext, WebServiceHelper.RestMethodType.GET,
            "v2/relationship/" + shared_relationship_id + "/" + sharedDocumentsDo.id + "/" + action,
            request, jsonObject.toString()
        )
    }

    fun interface EventListener {
        fun RefreshViewRelationshipsData()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        mParent = parent
        view = LayoutInflater.from(parent.context).inflate(R.layout.view_relationships_design, parent, false)
        mViewModel = ViewModelProvider(mActivity).get(NewModel::class.java)
        return MyViewHolder(view!!)
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val relationshipsModel = relationshipsList[position]
        mholder = holder

        holder.iv_initiated.visibility = GONE

        if (openActionPosition == position) {
            holder.action_list_card.visibility = VISIBLE
        } else {
            holder.action_list_card.visibility = GONE
        }

        holder.custom_spinner_cardview.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener
            val currentModel = relationshipsList[currentPosition]

            if (openActionPosition == currentPosition) {
                openActionPosition = -1
                holder.action_list_card.visibility = GONE
            } else {
                val previousOpenPosition = openActionPosition
                openActionPosition = currentPosition

                if (previousOpenPosition != -1) {
                    notifyItemChanged(previousOpenPosition)
                }

                showActionList(currentModel, holder)
            }
        }

        if (isPopupView) {
            holder.cv_relationships_details.visibility = GONE
            holder.ll_expandable_layout.visibility = VISIBLE

            if (route == "relationship_shared_with_me_individual_list" ||
                route == "relationship_shared_with_me_corporate_list" ||
                route == "relationship_shared_with_me_business_list"
            ) {
                mViewModel?.setData("Share Documents")
                holder.rb_share_document.background = mcontext.resources.getDrawable(R.drawable.button_right_green_count)
                holder.rb_share_document.setTextColor(Color.WHITE)
                holder.rb_profile.background = mcontext.resources.getDrawable(R.drawable.button_left_background)
                holder.rb_profile.setTextColor(Color.BLACK)
                holder.et_search_relationships.visibility = GONE
                holder.rg_shared_status.visibility = VISIBLE
                loadshared_with_us()
                unsharedDocumentation(holder, shared_tag, relationshipsModel)
            } else {
                unhideProfileDetails(relationshipsModel, holder)
            }
        } else {
            holder.cv_relationships_details.visibility = VISIBLE
            holder.ll_expandable_layout.visibility = GONE

            val isExpandable = relationshipsList[position].isExpandable
            if (isExpandable) {
                highLightId = ""
                unhideProfileDetails(relationshipsModel, holder)
                holder.ll_expandable_layout.visibility = VISIBLE
            } else {
                citizenList.clear()
                shared_list.clear()
                holder.ll_expandable_layout.visibility = GONE
            }
        }

        holder.btn_accept.visibility = GONE
        holder.btn_accept.setOnClickListener { callAcceptRequest(relationshipsModel.id ?: "") }

        holder.ll_icons.visibility = GONE
        holder.custom_spinner_cardview.visibility = VISIBLE
        holder.iv_activate_relationships.visibility = GONE

        if (relationshipsModel.status?.lowercase(Locale.ROOT) == "inactive") {
            holder.tv_initiated.text = mcontext.getString(R.string.inactive)
            holder.tv_initiated.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.cancelled_text))
            holder.tv_initiated.setBackgroundResource(R.drawable.cancelled_badge)
            holder.tv_initiated.visibility = VISIBLE

            holder.iv_tm_relationships.visibility = GONE
            holder.iv_activate_relationships.visibility = VISIBLE
            holder.iv_send_invite.visibility = GONE
            holder.rb_share_document.visibility = GONE
            holder.rb_profile.background = mcontext.resources.getDrawable(R.drawable.rectangular_button_green_count)
        } else {
            if (relationshipsModel.isAccepted) {
                holder.tv_initiated.text = mcontext.getString(R.string.active)
                holder.tv_initiated.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.completed_text))
                holder.tv_initiated.setBackgroundResource(R.drawable.completed_badge)
                holder.tv_initiated.visibility = VISIBLE

                holder.iv_send_invite.visibility = GONE
                if (Constants.CATEGORY != "solo") {
                    holder.iv_tm_relationships.visibility = VISIBLE
                } else {
                    holder.iv_tm_relationships.visibility = GONE
                }
            } else {
                holder.tv_initiated.text = mcontext.getString(R.string.pending)
                holder.tv_initiated.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.pending_text))
                holder.tv_initiated.setBackgroundResource(R.drawable.pending_badge)
                holder.tv_initiated.visibility = VISIBLE

                holder.iv_send_invite.visibility = GONE
                holder.iv_tm_relationships.visibility = GONE
                holder.rb_share_document.visibility = GONE
                holder.rb_profile.background = mcontext.resources.getDrawable(R.drawable.rectangular_button_green_count)
            }
        }

        holder.deletedBy.visibility = GONE
        holder.iv_restore_relationships.visibility = GONE

        if (TAG == "temp" && relationshipsModel.istemp && relationshipsModel.clientType != "Entity") {
            holder.tv_initiated.visibility = GONE
            holder.iv_initiated.visibility = GONE

            holder.iv_send_invite.visibility = VISIBLE
            AndroidUtils.ToggleButton(1, holder.iv_groups_relationships)
            holder.rb_share_document.visibility = GONE
            holder.rb_profile.background = mcontext.resources.getDrawable(R.drawable.rectangular_button_green_count)
        }

        if (Constants.ROLE != "GH" && Constants.ROLE != "TM") {
            holder.iv_groups_relationships.visibility = GONE
        } else {
            holder.iv_groups_relationships.visibility = GONE
            holder.iv_tm_relationships.visibility = GONE
        }

        if (relationshipsModel.isDisabled) {
            AndroidUtils.ToggleButton(0, holder.iv_send_invite)
            AndroidUtils.ToggleButton(0, holder.iv_groups_relationships)
            AndroidUtils.ToggleButton(0, holder.iv_tm_relationships)
        } else {
            if (relationshipsModel.canAccess) {
                AndroidUtils.ToggleButton(1, holder.iv_send_invite)
                AndroidUtils.ToggleButton(1, holder.iv_groups_relationships)
                AndroidUtils.ToggleButton(1, holder.iv_tm_relationships)
            } else {
                AndroidUtils.ToggleButton(0, holder.iv_send_invite)
                AndroidUtils.ToggleButton(0, holder.iv_groups_relationships)
                AndroidUtils.ToggleButton(0, holder.iv_tm_relationships)
            }
        }

        if (relationshipsModel.canAccept) {
            holder.btn_accept.visibility = VISIBLE
            holder.iv_groups_relationships.visibility = GONE
            holder.iv_tm_relationships.visibility = GONE
            holder.iv_initiated.visibility = GONE
            holder.tv_initiated.visibility = GONE
        }

        Log.i("Tag", "Relationship:" + relationshipsModel.adminName)
        holder.tv_relationship_name.text = relationshipsModel.name
        holder.deletedBy.text = "Deleted By " + relationshipsModel.deletedBy
        holder.tv_created_date.text = "Created " + relationshipsModel.created
        holder.tv_consumer.text = relationshipsModel.clientType

        if (TAG == "deleted") {
            holder.tv_initiated.visibility = INVISIBLE
            holder.iv_initiated.visibility = GONE

            holder.deletedBy.visibility = VISIBLE
            holder.iv_restore_relationships.visibility = VISIBLE
            holder.iv_activate_relationships.visibility = GONE
            holder.iv_groups_relationships.visibility = GONE
            holder.iv_tm_relationships.visibility = GONE
            holder.rb_share_document.visibility = GONE
        }

        holder.iv_groups_relationships.setOnClickListener {
            try {
                groupsList.clear()
                updatedMembersList.clear()
                callGroupsWebservice(holder, relationshipsModel)
            } catch (e: Exception) {
                AndroidUtils.showAlert(e.message, mActivity)
                e.printStackTrace()
            }
        }

        holder.iv_tm_relationships.setOnClickListener {
            try {
                callMembersWebservice(holder, relationshipsModel)
            } catch (e: Exception) {
                AndroidUtils.showAlert(e.message, mActivity)
                e.printStackTrace()
            }
        }

        holder.iv_send_invite.setOnClickListener {
            AndroidUtils.showConfirmationDialog(
                mcontext, "Confirmation",
                "Are you sure you want send relationship invite to " + relationshipsModel.name + " ?",
                relationshipsModel.name,
                object : AndroidUtils.OnConfirmListener {
                    override fun onSave() {
                        callInviteTempClients(relationshipsModel)
                    }

                    override fun onCancel() {}
                }
            )
        }

        holder.iv_restore_relationships.setOnClickListener {
            AndroidUtils.showConfirmationDialog(
                mcontext, "Confirmation",
                "Are you sure to restore " + relationshipsModel.name + " relationship?",
                relationshipsModel.name,
                object : AndroidUtils.OnConfirmListener {
                    override fun onSave() {
                        callRestoreRelationshipWebservice(relationshipsModel.id ?: "")
                    }

                    override fun onCancel() {}
                }
            )
        }

        holder.iv_activate_relationships.setOnClickListener {
            AndroidUtils.showConfirmationDialog(
                mcontext, "Confirmation",
                "Are you sure to activate " + relationshipsModel.name + " relationship?",
                relationshipsModel.name,
                object : AndroidUtils.OnConfirmListener {
                    override fun onSave() {
                        callRestoreRelationshipWebservice(relationshipsModel.id ?: "")
                    }

                    override fun onCancel() {}
                }
            )
        }

        FLAG = "first_click"

        holder.iv_delete_relationships.setOnClickListener { deleteRelationships(relationshipsModel) }

        holder.rb_profile.setOnClickListener {
            mViewModel?.setData("Profile")
            unhideProfileDetails(relationshipsModel, holder)
        }

        holder.cv_relationships_details.setOnClickListener {
            if (TAG != "deleted") {
                launchExchangeInformation(relationshipsModel)
                highLightId = ""
            }
        }

        holder.rb_share_document.setOnClickListener {
            mViewModel?.setData("Share Documents")
            holder.rb_share_document.background = mcontext.resources.getDrawable(R.drawable.button_right_green_count)
            holder.rb_share_document.setTextColor(Color.WHITE)
            holder.rb_profile.background = mcontext.resources.getDrawable(R.drawable.button_left_background)
            holder.rb_profile.setTextColor(Color.BLACK)
            holder.et_search_relationships.visibility = GONE
            holder.rg_shared_status.visibility = VISIBLE
            loadshared_with_us()
            unsharedDocumentation(holder, shared_tag, relationshipsModel)
        }

        holder.iv_share_docs.setOnClickListener {
            shared_tag = "byme"
            doc_nature = "share_doc"
            unsharedDocumentation(holder, shared_tag, relationshipsModel)
            holder.rb_shared_with_us.background = mcontext.resources.getDrawable(R.drawable.button_left_background)
            holder.rb_shared_with_us.setTextColor(Color.BLACK)
            holder.rb_shared_by_us.background = mcontext.resources.getDrawable(R.drawable.button_right_background)
            holder.rb_shared_by_us.setTextColor(Color.BLACK)
            holder.rg_document_type.visibility = VISIBLE
            if (Constants.CATEGORY != "solo") {
                holder.rb_firm_document.visibility = VISIBLE
                holder.rb_client_document.background = mcontext.resources.getDrawable(R.drawable.button_left_background)
                holder.rb_client_document.setTextColor(Color.BLACK)
                holder.rb_firm_document.background = mcontext.resources.getDrawable(R.drawable.button_right_background)
                holder.rb_firm_document.setTextColor(Color.BLACK)
            } else {
                holder.rb_client_document.background = mcontext.resources.getDrawable(R.drawable.rounder_button_grey)
                holder.rb_client_document.setTextColor(Color.BLACK)
                holder.rb_firm_document.visibility = GONE
            }
        }

        holder.rb_shared_with_us.setOnClickListener {
            try {
                loadshared_with_us()
                unsharedDocumentation(holder, shared_tag, relationshipsModel)
            } catch (e: Resources.NotFoundException) {
                AndroidUtils.showAlert(e.message, mActivity)
            }
        }

        holder.rb_shared_by_us.setOnClickListener {
            try {
                holder.rb_shared_with_us.background = mcontext.resources.getDrawable(R.drawable.button_left_background)
                holder.rb_shared_with_us.setTextColor(Color.BLACK)
                holder.rb_shared_by_us.background = mcontext.resources.getDrawable(R.drawable.button_right_green_count)
                holder.rb_shared_by_us.setTextColor(Color.WHITE)
                shared_tag = "byme"
                doc_nature = "byme"
                unsharedDocumentation(holder, shared_tag, relationshipsModel)
            } catch (e: Resources.NotFoundException) {
                AndroidUtils.showAlert(e.message, mActivity)
            }
        }

        holder.rb_client_document.setOnClickListener {
            if (Constants.CATEGORY != "solo") {
                holder.rb_firm_document.visibility = VISIBLE
                holder.rb_client_document.background = mcontext.resources.getDrawable(R.drawable.button_left_green_background)
                holder.rb_client_document.setTextColor(Color.WHITE)
                holder.rb_firm_document.background = mcontext.resources.getDrawable(R.drawable.button_right_background)
                holder.rb_firm_document.setTextColor(Color.BLACK)
            } else {
                holder.rb_client_document.background = mcontext.resources.getDrawable(R.drawable.rounder_button_green)
                holder.rb_client_document.setTextColor(Color.WHITE)
                holder.rb_firm_document.visibility = GONE
            }
            shared_tag = "client"
            try {
                callDocumentTypeWebservice(
                    relationshipsModel.id ?: "", shared_tag, holder,
                    relationshipsModel.groups, relationshipsModel.client_id ?: ""
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        holder.rb_firm_document.setOnClickListener {
            holder.rb_client_document.background = mcontext.resources.getDrawable(R.drawable.button_left_background)
            holder.rb_client_document.setTextColor(Color.BLACK)
            holder.rb_firm_document.background = mcontext.resources.getDrawable(R.drawable.button_right_green_count)
            holder.rb_firm_document.setTextColor(Color.WHITE)
            shared_tag = "firm"
            callDocumentTypeWebservice(
                relationshipsModel.id ?: "", shared_tag, holder,
                relationshipsModel.groups, relationshipsModel.client_id ?: ""
            )
        }
    }

    private fun launchExchangeInformation(relationshipsModel: RelationshipsModel) {
        val fragment = ExchangeInformationFragment.newInstance(
            relationshipsModel.id ?: "",
            relationshipsModel.name ?: "",
            TAG,
            relationshipsModel.client_id ?: "",
            relationshipsModel.isAccepted,
            if (relationshipsModel.groups != null) relationshipsModel.groups.toString() else "[]"
        )

        (mcontext as AppCompatActivity).supportFragmentManager
            .beginTransaction()
            .replace(R.id.id_framelayout, fragment)
            .addToBackStack("current_fragment")
            .commit()
    }

    private fun clear_search() {
        mholder?.et_Search?.setText("")
        mholder?.et_search_relationships?.setText("")
    }

    private fun loadshared_with_us() {
        shared_tag = "withme"
        doc_nature = "withme"
        mholder?.rb_shared_with_us?.background = mcontext.resources.getDrawable(R.drawable.button_left_green_background)
        mholder?.rb_shared_with_us?.setTextColor(Color.WHITE)
        mholder?.rb_shared_by_us?.background = mcontext.resources.getDrawable(R.drawable.button_right_background)
        mholder?.rb_shared_by_us?.setTextColor(Color.BLACK)
    }

    private fun showActionList(relationshipsModel: RelationshipsModel, holder: MyViewHolder) {
        val itemActions = ArrayList<String>()

        if (relationshipsModel.isAccepted && relationshipsModel.status?.lowercase(Locale.ROOT) != "inactive") {
            itemActions.add("Exchange Information")
            if (Constants.ROLE == "GH" || Constants.ROLE == "TM") {
                itemActions.add("Manage Groups")
            }
            if (Constants.CATEGORY != "solo") {
                itemActions.add("Manage Team Members")
            }
            if (TAG != "deleted") {
                itemActions.add("Delete Relationship")
            }
        } else if (relationshipsModel.status?.lowercase(Locale.ROOT) == "inactive") {
            itemActions.add("Activate Relationship")
            itemActions.add("Delete Relationship")
        } else if (TAG == "temp" && relationshipsModel.istemp) {
            itemActions.add("Send Invite")
            itemActions.add("Delete Relationship")
        } else if (!relationshipsModel.isAccepted) {
            itemActions.add("Exchange Information")
            itemActions.add("Delete Relationship")
        }

        if (TAG == "deleted") {
            itemActions.add("Restore Relationship")
            itemActions.add("Permanently Delete")
        }

        val itemAdapter = CommonSpinnerAdapter((mcontext as Activity), itemActions)
        holder.sp_action.adapter = itemAdapter
        holder.sp_action.post { AndroidUtils.setDynamicHeight(holder.sp_action) }

        holder.action_list_card.visibility = VISIBLE

        holder.sp_action.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            val selectedAction = itemActions[i]
            handleAction(relationshipsModel, selectedAction, holder)
            holder.action_list_card.visibility = GONE
            openActionPosition = -1
        }
    }

    private fun handleAction(relationshipsModel: RelationshipsModel, action: String, holder: MyViewHolder) {
        when (action) {
            "Exchange Information" -> launchExchangeInformation(relationshipsModel)
            "Manage Groups" -> callGroupsWebservice(holder, relationshipsModel)
            "Manage Team Members" -> callMembersWebservice(holder, relationshipsModel)
            "Delete Relationship" -> deleteRelationships(relationshipsModel)
            "Share Documents" -> {
                mViewModel?.setData("Share Documents")
                holder.rb_share_document.background = mcontext.resources.getDrawable(R.drawable.button_right_green_count)
                holder.rb_share_document.setTextColor(Color.WHITE)
                holder.rb_profile.background = mcontext.resources.getDrawable(R.drawable.button_left_background)
                holder.rb_profile.setTextColor(Color.BLACK)
                holder.et_search_relationships.visibility = GONE
                holder.rg_shared_status.visibility = VISIBLE
                loadshared_with_us()
                unsharedDocumentation(holder, shared_tag, relationshipsModel)
            }
            "Activate Relationship" -> callRestoreRelationshipWebservice(relationshipsModel.id ?: "")
            "Send Invite" -> callInviteTempClients(relationshipsModel)
            "Accept Request" -> callAcceptRequest(relationshipsModel.id ?: "")
            "Reject Request" -> deleteRelationships(relationshipsModel)
            "Restore Relationship" -> callRestoreRelationshipWebservice(relationshipsModel.id ?: "")
            "Permanently Delete" -> callTerminateRelationshipWebservice(relationshipsModel.id ?: "")
        }
    }

    private fun showActionDialog(relationshipsModel: RelationshipsModel, actions: ArrayList<String>) {
        val builder = AlertDialog.Builder(mcontext)
        builder.setTitle("Actions")

        val actionArray = actions.toTypedArray()

        builder.setItems(actionArray) { _, which ->
            val selectedAction = actionArray[which]
            handleAction(relationshipsModel, selectedAction)
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun handleAction(relationshipsModel: RelationshipsModel, action: String) {
        when (action) {
            "Exchange Information" -> launchExchangeInformation(relationshipsModel)
            "Manage Groups" -> callGroupsWebservice(mholder!!, relationshipsModel)
            "Manage Team Members" -> callMembersWebservice(mholder!!, relationshipsModel)
            "Delete Relationship" -> deleteRelationships(relationshipsModel)
            "Share Documents" -> {
                mViewModel?.setData("Share Documents")
                mholder?.rb_share_document?.background = mcontext.resources.getDrawable(R.drawable.button_right_green_count)
                mholder?.rb_share_document?.setTextColor(Color.WHITE)
                mholder?.rb_profile?.background = mcontext.resources.getDrawable(R.drawable.button_left_background)
                mholder?.rb_profile?.setTextColor(Color.BLACK)
                mholder?.et_search_relationships?.visibility = GONE
                mholder?.rg_shared_status?.visibility = VISIBLE
                loadshared_with_us()
                unsharedDocumentation(mholder!!, shared_tag, relationshipsModel)
            }
            "Activate Relationship" -> callRestoreRelationshipWebservice(relationshipsModel.id ?: "")
            "Send Invite" -> callInviteTempClients(relationshipsModel)
            "Accept Request" -> callAcceptRequest(relationshipsModel.id ?: "")
            "Reject Request" -> deleteRelationships(relationshipsModel)
            "Restore Relationship" -> callRestoreRelationshipWebservice(relationshipsModel.id ?: "")
            "Permanently Delete" -> callTerminateRelationshipWebservice(relationshipsModel.id ?: "")
        }
    }

    private fun callDocumentTypeWebservice(
        id: String,
        shared_tag: String,
        holder: MyViewHolder,
        jsonArray: JSONArray?,
        client_id: String
    ) {
        progress_dialog = AndroidUtils.get_progress(mActivity)
        try {
            mholder = holder
            shared_relationship_id = id
            shared_client_id = client_id
            val jsonObject = JSONObject()
            if (shared_tag == "client") {
                jsonObject.put("category", "client")
                jsonObject.put("clients", client_id)
                jsonObject.put("matters", "all")
            } else {
                val groups = JSONArray()
                if (jsonArray != null) {
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject1 = jsonArray.getJSONObject(i)
                        groups.put(jsonObject1.getString("id"))
                    }
                }
                jsonObject.put("category", "firm")
                jsonObject.put("groups", groups)
            }
            WebServiceHelper.callHttpWebService(
                this, mcontext, WebServiceHelper.RestMethodType.PUT,
                "v3/document/filter", "Existing Documents", jsonObject.toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun unsharedDocumentation(
        holder: MyViewHolder,
        shared_tag: String,
        relationshipsModel: RelationshipsModel
    ) {
        holder.cv_Profile.visibility = GONE
        holder.rg_shared_status.visibility = VISIBLE
        holder.ll_documents.visibility = VISIBLE
        holder.nestedScrollView.visibility = GONE
        holder.rg_document_type.visibility = GONE
        holder.ll_documents.visibility = GONE
        holder.iv_share_docs.visibility = VISIBLE
        shared_list.clear()
        callSharedDocumentsWebservice(relationshipsModel.id ?: "", shared_tag, holder, relationshipsModel.client_id ?: "")
    }

    private fun callInviteTempClients(relationshipsModel: RelationshipsModel) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("client_id", relationshipsModel.client_id)
            jsonObject.put("client_type", relationshipsModel.clientType)
            WebServiceHelper.callHttpWebService(
                this, mcontext, WebServiceHelper.RestMethodType.POST,
                "v3/convert-temp-clients", "Invite Temp Clients", jsonObject.toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun View_doc(doc_id: String, sharedDocumentsDo: SharedDocumentsDo) {
        progress_dialog = AndroidUtils.get_progress(mActivity)
        this.sharedDocumentsDo = sharedDocumentsDo
        val jsonObject = JSONObject()
        if (shared_tag == "withme") {
            if (TAG == "corporate" || TAG == "business") {
                WebServiceHelper.callHttpWebService(
                    this, mcontext, WebServiceHelper.RestMethodType.GET,
                    "v3/document/" + doc_id + "/view", "View Other Doc", jsonObject.toString()
                )
            } else {
                WebServiceHelper.callHttpWebService(
                    this, mcontext, WebServiceHelper.RestMethodType.GET,
                    "v2/relationship/" + shared_relationship_id + "/" + doc_id + "/view",
                    "View Doc With Us", jsonObject.toString()
                )
            }
        } else {
            WebServiceHelper.callHttpWebService(
                this, mcontext, WebServiceHelper.RestMethodType.GET,
                "v3/document/" + doc_id + "/view", "View Other Doc", jsonObject.toString()
            )
        }
    }

    open fun callDecryptApi(id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity)
            val jsonObject = JSONObject()
            jsonObject.put("docid", id)
            if (shared_tag == "withme") {
                jsonObject.put("shared_doc", true)
            }
            WebServiceHelper.callHttpWebService(
                this, mcontext, WebServiceHelper.RestMethodType.POST,
                Constants.decryptUrl ?: "", "Decrypt Doc", jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun checkViewType(url: String, CONTENT_TYPE: String, sharedDocumentsDo: SharedDocumentsDo) {
        val isImage = File_Content_Type.isImage(sharedDocumentsDo.content_type)
        val isPDF = File_Content_Type.isPDF(sharedDocumentsDo.content_type)
        val isEncrypted = sharedDocumentsDo.added_encryption || sharedDocumentsDo.is_encrypted

        if (isEncrypted) {
            callDecryptApi(sharedDocumentsDo.id ?: "")
        } else if (!isPDF && !isImage) {
            callOtherDocViewApi(sharedDocumentsDo.id ?: "")
        } else {
            display_doc(url, sharedDocumentsDo)
        }
    }

    private fun display_doc(url: String, sharedDocumentsDo: SharedDocumentsDo) {
        val dialogBuilder = AlertDialog.Builder(mActivity)
        val inflater = mActivity.layoutInflater
        val view = inflater.inflate(R.layout.view_documents, null)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_pdf)
        val iv_image = view.findViewById<ImageView>(R.id.doc_image)
        val idPDFView = view.findViewById<PDFView>(R.id.idPDFView)
        val webView = view.findViewById<WebView>(R.id.doc_webview)
        val header = view.findViewById<TextView>(R.id.header_name)
        val iv_close_edit_docs = view.findViewById<ImageView>(R.id.close_edit_docs)
        val isImage = File_Content_Type.isImage(sharedDocumentsDo.content_type)
        header.text = sharedDocumentsDo.name
        val dialog = dialogBuilder.create()

        var pdfTask: RetrievePDFfromUrl? = null

        iv_close_edit_docs.setOnClickListener {
            try {
                idPDFView?.recycle()
                pdfTask?.let {
                    it.cancelLoading()
                    it.cancel(true)
                }
            } catch (ignored: Exception) {
            }
            dialog.dismiss()
        }
        val lowerUrl = url.lowercase(Locale.ROOT)
        val urlIsPDF = lowerUrl.contains("application/pdf") || lowerUrl.contains(".pdf")
        if (urlIsPDF) {
            idPDFView.visibility = VISIBLE
            progressBar.visibility = VISIBLE

            pdfTask = RetrievePDFfromUrl(idPDFView, progressBar)
            pdfTask.execute(url)
        } else {
            if (isImage) {
                iv_image.visibility = VISIBLE
                Glide.with(mcontext)
                    .load(url)
                    .placeholder(R.drawable.progress_animation)
                    .centerCrop()
                    .into(iv_image)
            } else {
                idPDFView.visibility = VISIBLE
                progressBar.visibility = VISIBLE

                pdfTask = RetrievePDFfromUrl(idPDFView, progressBar)
                pdfTask.execute(url)
            }
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun handleDocumentDisplay(url: String, docModel: SharedDocumentsDo, idPDFView: PDFView, iv_image: ImageView) {
        val isImage = File_Content_Type.isImage(docModel.content_type)
        val isPDF = File_Content_Type.isPDF(docModel.content_type)
        val isEncrypted = docModel.added_encryption || docModel.is_encrypted

        if (isEncrypted) {
            dialog?.dismiss()
            callDecryptApi(docModel.id ?: "")
            return
        }
        val lowerUrl = url.lowercase(Locale.ROOT)
        val urlIsImage = lowerUrl.contains("image") ||
                lowerUrl.contains(".jpg") ||
                lowerUrl.contains(".jpeg") ||
                lowerUrl.contains(".png") ||
                lowerUrl.contains(".webp")
        if (isImage || urlIsImage) {
            idPDFView.visibility = GONE
            iv_image.visibility = VISIBLE

            Glide.with(mcontext)
                .load(url)
                .placeholder(R.drawable.progress_animation)
                .centerCrop()
                .into(iv_image)
        } else if (isPDF) {
            iv_image.visibility = GONE
            idPDFView.visibility = VISIBLE
            RetrievePDFfromUrl(idPDFView, null).execute(url)
        } else {
            dialog?.dismiss()
            callOtherDocViewApi(docModel.id ?: "")
        }
    }

    open fun callOtherDocViewApi(id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity)
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this, mcontext, WebServiceHelper.RestMethodType.GET,
                Constants.base_URL + "v3/document/" + id + "/view", "Other Doc View", jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    private fun openshared_with_us(shared_list: ArrayList<SharedDocumentsDo>) {
        mholder?.rv_shared_with_us?.removeAllViews()
        if (shared_tag == "withme") {
            mholder?.ll_doc_button?.visibility = GONE
        } else {
            mholder?.ll_doc_button?.visibility = VISIBLE
        }
        val layoutManager = LinearLayoutManager(mcontext, LinearLayoutManager.VERTICAL, false)
        mholder?.rv_shared_with_us?.layoutManager = layoutManager
        val documentsAdapter = SharedDocumentsAdapter(
            shared_list, shared_tag, mcontext, this,
            shared_relationship_id, shared_client_id, mActivity, this, highLightId
        )
        mholder?.rv_shared_with_us?.adapter = documentsAdapter
    }

    private fun applyHighlight(holder: MyViewHolder, model: RelationshipsModel) {
        if (highLightId != null && model.id != null && highLightId!!.contains(model.id!!)) {
            holder.cv_relationships_details.background = mcontext.resources.getDrawable(R.drawable.blue_stroke_card)
            holder.cv_relationships_details.cardElevation = 8f

            holder.cv_relationships_details.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.blue_stroke_card)

            Handler(Looper.getMainLooper()).postDelayed({
                holder.cv_relationships_details.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.rectangular_white_background)
            }, 5000)
        } else {
            holder.cv_relationships_details.setCardBackgroundColor(mcontext.resources.getColor(android.R.color.white))
            holder.cv_relationships_details.cardElevation = 4f
        }
    }

    private fun open_tm_popup() {
        try {
            val isUpdated = AtomicBoolean(false)

            val dialogBuilder = AlertDialog.Builder(mcontext)
            val inflater = mActivity.layoutInflater
            val view = inflater.inflate(R.layout.update_member_layout, null)

            val rv_assign_members = view.findViewById<RecyclerView>(R.id.rv_assign_members)
            membersAdapter = MemberSelectionAdapter(updateMembersList) { selected, userInteracted ->
                selectedMembersList.clear()
                selectedMembersList.addAll(selected)
                isUpdated.set(userInteracted)
            }

            rv_assign_members.layoutManager = LinearLayoutManager(mcontext)
            rv_assign_members.adapter = membersAdapter
            AndroidUtils.LoadList(rv_assign_members, mcontext, membersAdapter!!.itemCount, false)

            val tl_search_members = view.findViewById<View>(R.id.tl_search_members)
            val et_search_members = tl_search_members.findViewById<TextInputEditText>(R.id.et_Search)
            et_search_members.addTextChangedListener(Validation(et_search_members))
            et_search_members.setHint(R.string.search_members)
            et_search_members.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    membersAdapter!!.filter.filter(s.toString())
                    AndroidUtils.LoadList(rv_assign_members, mcontext, membersAdapter!!.itemCount, false)
                }

                override fun afterTextChanged(s: Editable) {}
            })

            val tv_update_member = view.findViewById<TextView>(R.id.tv_update_member)
            if (!relationshipsModel_new!!.name.isNullOrEmpty()) {
                tv_update_member.text = mcontext.getString(R.string.update_member_access) + " - " + relationshipsModel_new!!.name
            } else {
                tv_update_member.text = mcontext.getString(R.string.update_member_access)
            }

            val btnSendRequest = view.findViewById<AppCompatButton>(R.id.btn_create)
            val btnCancel = view.findViewById<AppCompatButton>(R.id.btn_cancel_save)
            val ivCancel = view.findViewById<ImageView>(R.id.close_details)

            val dialog = dialogBuilder.create()
            dialog.setView(view)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            ad_dialog_docs = dialog

            val closeDialogListener = View.OnClickListener {
                if (isUpdated.get()) {
                    ConfirmPopup(relationshipsModel_new!!.name ?: "", relationshipsModel_new!!.id ?: "", true, false, null)
                }
                dialog.dismiss()
            }

            btnSendRequest.setOnClickListener {
                ConfirmPopup(relationshipsModel_new!!.name ?: "", relationshipsModel_new!!.id ?: "", false, false, null)
            }

            btnCancel.setOnClickListener(closeDialogListener)
            ivCancel.setOnClickListener(closeDialogListener)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun callUpdateMembers(shared_relationship_id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity)
            val jsonObject = JSONObject()
            val jsonArray = JSONArray()
            for (member in selectedMembersList) {
                jsonArray.put(member.id)
            }
            jsonObject.put("members", jsonArray)
            Log.d("Payload", jsonObject.toString())
            if (TAG == "corporate") {
                WebServiceHelper.callHttpWebService(
                    this, mcontext, WebServiceHelper.RestMethodType.PATCH,
                    "v3/relationship/" + shared_relationship_id + "/members", "Update Members", jsonObject.toString()
                )
            } else {
                WebServiceHelper.callHttpWebService(
                    this, mcontext, WebServiceHelper.RestMethodType.PUT,
                    "v2/relationship/" + shared_relationship_id + "/members", "Update Members", jsonObject.toString()
                )
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun ConfirmPopup(
        rel_name: String,
        shared_client_id: String,
        isCancel: Boolean,
        isGroup: Boolean,
        list_item: ArrayList<ViewGroupModel>?
    ) {
        try {
            val dialogBuilder = AlertDialog.Builder(mcontext)
            val inflater = mActivity.layoutInflater
            val view = inflater.inflate(R.layout.delete_relationship, null)
            val tv_confirmation = view.findViewById<TextView>(R.id.tv_confirmation)
            val header_name = view.findViewById<TextView>(R.id.header_name)
            header_name.setTextColor(mcontext.getColor(R.color.blue))
            val close_documents = view.findViewById<ImageView>(R.id.close_documents)
            var invite_txt = ""
            if (isCancel) {
                header_name.setText(R.string.alert_)
                invite_txt = "Changes you made will not be saved. Do you want to save?"
                tv_confirmation.text = invite_txt
            } else {
                tv_confirmation.setTextColor(mcontext.getColor(R.color.black))
                header_name.setText(R.string.confirmation)
                val message = if (isGroup) {
                    "Are you sure you want to modify the Group access for $rel_name?"
                } else {
                    "Are you sure you want to modify the Member access for $rel_name?"
                }
                val spannable = SpannableString(message)

                val start = message.indexOf(rel_name)
                if (start >= 0) {
                    spannable.setSpan(StyleSpan(Typeface.BOLD), start, start + rel_name.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannable.setSpan(ForegroundColorSpan(Color.BLACK), start, start + rel_name.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                tv_confirmation.text = spannable
            }

            val bt_yes = view.findViewById<AppCompatButton>(R.id.btn_yes)
            val btn_no = view.findViewById<AppCompatButton>(R.id.btn_No)
            btn_no.setTextColor(mcontext.getColor(R.color.white))
            bt_yes.setTextColor(mcontext.getColor(R.color.black))
            bt_yes.background = mcontext.getDrawable(R.drawable.yes_button_red_button)
            btn_no.background = mcontext.getDrawable(R.drawable.no_button_green_button)
            val dialog = dialogBuilder.create()
            ad_dialog_copy = dialog
            dialog.setView(view)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            close_documents.setOnClickListener {
                if (isGroup) {
                    clientRelationship?.ll_view_rel?.isEnabled = true
                    clientRelationship?.ll_view_rel?.alpha = 1.0f
                    et_search_groups?.setText("")
                } else {
                    ad_dialog_docs?.dismiss()
                }
                dialog.dismiss()
            }
            btn_no.setOnClickListener {
                if (isGroup) {
                    clientRelationship?.ll_view_rel?.isEnabled = true
                    clientRelationship?.ll_view_rel?.alpha = 1.0f
                    et_search_groups?.setText("")
                } else {
                    ad_dialog_docs?.dismiss()
                }
                dialog.dismiss()
            }
            bt_yes.setOnClickListener {
                if (isGroup) {
                    Constants.mainActivity?.Remove_Page(GroupRecyclerViewFragment(mcontext, groupsList, clientRelationship, relationshipsModel_new, this))
                    callUpdateGroups(shared_client_id, list_item!!)
                    clientRelationship?.ll_view_rel?.isEnabled = true
                    clientRelationship?.ll_view_rel?.alpha = 1.0f
                    et_search_groups?.setText("")
                    popupView?.visibility = GONE
                    groupsList.clear()
                } else {
                    callUpdateMembers(shared_client_id)
                    ad_dialog_docs?.dismiss()
                }
                dialog.dismiss()
            }
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, mActivity)
        }
    }

    private fun openSharedPopupWindow(sharedList: ArrayList<SharedDocumentsDo>) {
        try {
            val dialogBuilder = AlertDialog.Builder(mcontext)
            val inflater = mActivity.layoutInflater
            val view = inflater.inflate(R.layout.shared_document_recyclerview_popup, null)
            val rvRelationshipGroups = view.findViewById<RecyclerView>(R.id.rv_relationship_documents)
            val etSearchRelationships = view.findViewById<TextInputEditText>(R.id.et_search_relationships)
            etSearchRelationships.addTextChangedListener(Validation(etSearchRelationships))
            val llButtons = view.findViewById<LinearLayoutCompat>(R.id.ll_buttons)
            val ll_select_all = view.findViewById<LinearLayout>(R.id.ll_select_all)
            val tv_select_all = view.findViewById<TextView>(R.id.tv_select_all)
            tv_select_all.setText(R.string.select_all)
            chk_select_all = view.findViewById(R.id.chk_select_all)
            val tvHeaderName = view.findViewById<TextView>(R.id.header_name)
            val tvMessage = view.findViewById<TextView>(R.id.message)
            tvMessage.gravity = Gravity.CENTER
            tvMessage.setText(R.string.no_documents_to_show)
            tvMessage.textSize = DynamicUtils.twenty.toFloat()
            val btnSendRequest = view.findViewById<AppCompatButton>(R.id.btn_send_request)
            val btnRelationshipsCancel = view.findViewById<AppCompatButton>(R.id.btn_relationships_cancel)
            val ivCancel = view.findViewById<ImageView>(R.id.close_edit_docs)

            val headerName: String
            var buttonTextResId = R.string.share
            when (shared_tag) {
                "withme" -> {
                    headerName = "Documents Shared With Us"
                    ll_select_all.visibility = GONE
                    llButtons.visibility = GONE
                }
                "byme" -> {
                    headerName = "Documents Shared By Us"
                    ll_select_all.visibility = VISIBLE
                    llButtons.visibility = VISIBLE
                    buttonTextResId = R.string.unshare
                }
                "client" -> {
                    ll_select_all.visibility = VISIBLE
                    headerName = "Client Documents"
                }
                else -> {
                    ll_select_all.visibility = VISIBLE
                    headerName = "Firm Documents"
                }
            }

            tvHeaderName.text = headerName
            btnSendRequest.setText(buttonTextResId)

            if (sharedList.isEmpty()) {
                llButtons.visibility = GONE
                etSearchRelationships.visibility = GONE
                val dialog = dialogBuilder.create()
                ad_dialog_docs = dialog
                dialog.setView(view)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
            } else {
                rvRelationshipGroups.layoutManager = LinearLayoutManager(mcontext, LinearLayoutManager.VERTICAL, false)

                val documentsAdapter = SharedDocumentsAdapter(
                    sharedList, shared_tag, mcontext, this,
                    shared_relationship_id, shared_client_id, mActivity, this, highLightId
                )
                rvRelationshipGroups.adapter = documentsAdapter

                chk_select_all?.setOnClickListener {
                    documentsAdapter.selectOrDeselectAll(chk_select_all!!.isChecked)
                    if (shared_tag == "byme") {
                        setSelected_unsharedocsList(sharedList)
                    } else {
                        setSelected_sharedocsList(sharedList)
                    }
                }
                etSearchRelationships.hint = mcontext.getString(R.string.search_documents)
                etSearchRelationships.addTextChangedListener(object : TextWatcher {
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                    override fun afterTextChanged(s: Editable) {
                        documentsAdapter.filter.filter(s.toString().trim())
                    }
                })

                val closeDialogListener = View.OnClickListener {
                    etSearchRelationships.setText("")
                    if (shared_tag == "byme") {
                        selected_unsharedocsList.clear()
                        mholder?.ll_expandable_layout?.visibility = GONE
                        clientRelationship?.viewRelationshipsData()
                    } else if (shared_tag == "client") {
                        selected_sharedocsList.removeAll(selected_client_sharedocsList)
                        selected_client_sharedocsList.clear()
                        mholder?.ll_expandable_layout?.visibility = GONE
                        clientRelationship?.viewRelationshipsData()
                    } else if (shared_tag == "firm") {
                        selected_sharedocsList.removeAll(selected_firm_sharedocsList)
                        selected_firm_sharedocsList.clear()
                        mholder?.ll_expandable_layout?.visibility = GONE
                        clientRelationship?.viewRelationshipsData()
                    }
                    ad_dialog_docs?.dismiss()
                }

                btnRelationshipsCancel.setOnClickListener(closeDialogListener)
                ivCancel.setOnClickListener(closeDialogListener)

                btnSendRequest.setOnClickListener {
                    try {
                        updated_shared_list.clear()
                        val remove = JSONArray()

                        val selectedDocs = documentsAdapter.selectedList

                        for (doc in selectedDocs) {
                            val jsonObj = JSONObject()
                            jsonObj.put("docid", doc.id)
                            jsonObj.put("doctype", "general")

                            if (shared_tag == "firm") {
                                jsonObj.put("matters", doc.matter_details)
                            }

                            remove.put(jsonObj)
                        }

                        updated_shared_list.addAll(selectedDocs)

                        if (!updated_shared_list.isEmpty()) {
                            etSearchRelationships.setText("")
                            remove_popup(updated_shared_list, remove)
                        } else {
                            Log.d("Selected_list_size", "Please select at least one document")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val dialog = dialogBuilder.create()
                ad_dialog_docs = dialog
                dialog.setView(view)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
            }
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, mActivity)
            e.printStackTrace()
        }
    }

    private fun remove_popup(updatedSharedList: ArrayList<SharedDocumentsDo>, remove: JSONArray) {
        try {
            val dialogBuilder = AlertDialog.Builder(mcontext)
            val inflater = mActivity.layoutInflater
            val view = inflater.inflate(R.layout.share_document_popup, null)

            val tvShareDoc = view.findViewById<TextView>(R.id.tv_share_doc)
            val tvUnshareDoc = view.findViewById<TextView>(R.id.tv_unshare_doc)
            val rvShareDocuments = view.findViewById<RecyclerView>(R.id.rv_share_documents)
            val rvUnshareDocuments = view.findViewById<RecyclerView>(R.id.rv_unshare_documents)
            val llShareDoc = view.findViewById<LinearLayout>(R.id.ll_share_doc)
            val llUnshareDoc = view.findViewById<LinearLayout>(R.id.ll_unshare_doc)

            tvShareDoc.setTextColor(mcontext.resources.getColor(R.color.black))
            tvUnshareDoc.setTextColor(mcontext.resources.getColor(R.color.black))

            val tvShareDocumentsText = view.findViewById<TextView>(R.id.tv_share_documents).findViewById<TextView>(R.id.header_name)
            tvShareDocuments = tvShareDocumentsText

            if (shared_tag == "byme") {
                tvUnshareDoc.setText(R.string.unshare_documents)
                tvShareDoc.setText(R.string.unshare_documents)
                tvShareDocumentsText.text = "Documents Unshare"
            } else {
                tvUnshareDoc.setText(R.string.share_documents)
                tvShareDoc.setText(R.string.share_documents)
                tvShareDocumentsText.text = "Documents Share"
            }

            updatedSharedList.addAll(if (shared_tag == "byme") selected_unsharedocsList else selected_sharedocsList)

            llUnshareDoc.visibility = if (selected_unsharedocsList.isEmpty()) GONE else VISIBLE
            llShareDoc.visibility = if (selected_sharedocsList.isEmpty()) GONE else VISIBLE

            setupRecyclerView(rvShareDocuments, selected_sharedocsList)
            setupRecyclerView(rvUnshareDocuments, selected_unsharedocsList)

            val etShareMessage = view.findViewById<TextInputEditText>(R.id.et_share_message)
            etShareMessage.visibility = VISIBLE

            val btnCancelShare = view.findViewById<Button>(R.id.btn_cancel_share)
            val btnOkShare = view.findViewById<Button>(R.id.btn_ok_share)

            if (shared_tag == "byme") {
                tvShareDocumentsText.text = "Documents Unshare"
                btnOkShare.text = "Unshare"
            } else {
                tvShareDocumentsText.text = "Documents Share"
                btnOkShare.text = "Share"
            }
            btnCancelShare.setOnClickListener { alertDialog?.dismiss() }
            btnOkShare.setOnClickListener {
                try {
                    callUnsharedDocumentWebservice(
                        shared_relationship_id, remove,
                        etShareMessage.text.toString().trim(), shared_client_id, false
                    )
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
                alertDialog?.dismiss()
            }

            val dialog = dialogBuilder.create()
            alertDialog = dialog
            dialog.setView(view)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView, documentList: ArrayList<SharedDocumentsDo>) {
        val layoutManager = LinearLayoutManager(mcontext, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        val documentsAdapter = UnshareDocumentAdapter(documentList, this)
        recyclerView.adapter = documentsAdapter
    }

    @Throws(JSONException::class)
    open fun callUnsharedDocumentWebservice(
        id: String,
        remove: JSONArray,
        message: String,
        clientId: String,
        isRemove: Boolean
    ) {
        progress_dialog = AndroidUtils.get_progress(mActivity)
        val jsonObject = JSONObject()
        val removeDoc = JSONArray()
        val addDoc = JSONArray()

        for (doc in selected_unsharedocsList) {
            val docObject = JSONObject()
            docObject.put("docid", doc.id)
            docObject.put("doctype", "general")
            removeDoc.put(docObject)
        }

        for (doc in selected_sharedocsList) {
            val docObject = JSONObject()
            val matter_details = JSONArray()
            if (doc.has_Confidential) {
                matter_details.put(doc.matter_details_id)
            }
            docObject.put("docid", doc.id)
            docObject.put("doctype", "general")
            docObject.put("matters", matter_details)
            addDoc.put(docObject)
        }

        if (remove.length() != 0) {
            if (Objects.equals(shared_tag, "byme")) {
                if (isRemove) {
                    jsonObject.put("remove", remove)
                } else {
                    jsonObject.put("remove", removeDoc)
                }
                jsonObject.put("add", addDoc)
            } else {
                jsonObject.put("remove", removeDoc)
                jsonObject.put("add", addDoc)
            }

            jsonObject.put("message", message)

            if (TAG == "corporate") {
                val corporate = JSONObject()
                corporate.put("relid", id)
                if (isRemove) {
                    corporate.put("remove", remove)
                } else {
                    corporate.put("remove", removeDoc)
                }
                corporate.put("add", addDoc)
                corporate.put("message", message)
                WebServiceHelper.callHttpWebService(
                    this, mcontext, WebServiceHelper.RestMethodType.POST,
                    "v3/share", "UnshareDocuments", corporate.toString()
                )
            } else {
                WebServiceHelper.callHttpWebService(
                    this, mcontext, WebServiceHelper.RestMethodType.PUT,
                    "v2/relationship/$id/docs/share", "UnshareDocuments", jsonObject.toString()
                )
            }
        } else {
            AndroidUtils.showAlert("Please select at least one document", mActivity)
        }
    }

    private fun callSharedDocumentsWebservice(id: String, shared_tag: String, holder: MyViewHolder, client_id: String) {
        progress_dialog = AndroidUtils.get_progress(mActivity)
        mholder = holder
        shared_relationship_id = id
        shared_client_id = client_id
        val jsonObject = JSONObject()
        if (TAG == "corporate") {
            WebServiceHelper.callHttpWebService(
                this, mcontext, WebServiceHelper.RestMethodType.GET,
                "v3/share/$id/$shared_tag", "Shared Corp Documents", jsonObject.toString()
            )
        } else {
            WebServiceHelper.callHttpWebService(
                this, mcontext, WebServiceHelper.RestMethodType.GET,
                "v2/relationship/$id/docs/shared/$shared_tag", "Shared Documents", jsonObject.toString()
            )
        }
    }

    private fun unhideProfileDetails(relationshipsModel: RelationshipsModel, holder: MyViewHolder) {
        mholder = holder
        holder.rb_share_document.background = mcontext.resources.getDrawable(R.drawable.button_right_background)
        holder.rb_share_document.setTextColor(Color.BLACK)
        if (!relationshipsModel.isAccepted) {
            holder.rb_profile.background = mcontext.resources.getDrawable(R.drawable.rectangular_button_green_count)
        } else {
            holder.rb_profile.background = mcontext.resources.getDrawable(R.drawable.button_left_green_background)
        }
        holder.rb_profile.setTextColor(Color.WHITE)
        holder.rg_shared_status.visibility = GONE
        relationshipmodel_profile = relationshipsModel
        holder.nestedScrollView.visibility = GONE
        holder.ll_documents.visibility = GONE
        holder.iv_share_docs.visibility = GONE
        holder.rg_document_type.visibility = GONE
        callProfileWebservice(relationshipsModel.id ?: "")
        shared_list.clear()
        shared_tag = ""
    }

    private fun callProfileWebservice(id: String) {
        val jsonObject = JSONObject()
        if (TAG == "corporate") {
            WebServiceHelper.callHttpWebService(
                this, mcontext, WebServiceHelper.RestMethodType.GET,
                "v3/profile/$id", "Profile", jsonObject.toString()
            )
        } else {
            WebServiceHelper.callHttpWebService(
                this, mcontext, WebServiceHelper.RestMethodType.GET,
                "v2/relationship/$id/profile", "Profile", jsonObject.toString()
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun deleteRelationships(relationshipsModel: RelationshipsModel) {
        try {
            val dialogBuilder = AlertDialog.Builder(mcontext)
            val inflater = mActivity.layoutInflater
            val view = inflater.inflate(R.layout.delete_relationship, null)
            val close_documents = view.findViewById<ImageView>(R.id.close_documents)
            val tv_confirmation = view.findViewById<TextView>(R.id.tv_confirmation)
            var confirmText = ""
            var confirmName = ""
            if (TAG == "deleted") {
                confirmName = relationshipsModel.deletedBy ?: ""
                confirmText = "Are you sure you want to delete the relationship request sent by $confirmName?"
            } else {
                confirmName = relationshipsModel.name ?: ""
                confirmText = "Are you sure you want to delete the relationship request sent to $confirmName?"
            }

            val spannable = SpannableString(confirmText)

            spannable.setSpan(
                ForegroundColorSpan(mcontext.getColor(R.color.black)),
                0,
                confirmText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            if (confirmName.isNotEmpty()) {
                val start = confirmText.indexOf(confirmName)
                if (start >= 0) {
                    val end = start + confirmName.length

                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            tv_confirmation.text = spannable

            val header_name = view.findViewById<TextView>(R.id.header_name)
            header_name.setText(R.string.confirmation)
            header_name.setTextColor(mcontext.getColor(R.color.blue))
            val bt_yes = view.findViewById<AppCompatButton>(R.id.btn_yes)
            val btn_no = view.findViewById<AppCompatButton>(R.id.btn_No)
            btn_no.setTextColor(mcontext.getColor(R.color.white))
            bt_yes.setTextColor(mcontext.getColor(R.color.black))
            bt_yes.background = mcontext.getDrawable(R.drawable.yes_button_red_button)
            btn_no.background = mcontext.getDrawable(R.drawable.no_button_green_button)

            btn_no.setOnClickListener { ad_dialog_delete?.dismiss() }
            close_documents.setOnClickListener { ad_dialog_delete?.dismiss() }
            bt_yes.setOnClickListener {
                if (TAG == "deleted") {
                    callTerminateRelationshipWebservice(relationshipsModel.id ?: "")
                } else {
                    callDeleteRelationshipWebservice(relationshipsModel.id ?: "")
                }
                ad_dialog_delete?.dismiss()
            }
            val dialog = dialogBuilder.create()
            ad_dialog_delete = dialog
            dialog.setView(view)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, mActivity)
        }
    }

    private fun callRestoreRelationshipWebservice(id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity)
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this, mcontext, WebServiceHelper.RestMethodType.POST,
                "v2/relationship/$id/terminate/restore", "Archive Relationship", jsonObject.toString()
            )
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, mActivity)
        }
    }

    private fun callTerminateRelationshipWebservice(id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity)
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this, mcontext, WebServiceHelper.RestMethodType.DELETE,
                "v2/relationship/$id/archive", "Delete_Relationship", jsonObject.toString()
            )
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, mActivity)
        }
    }

    private fun callDeleteRelationshipWebservice(id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity)
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this, mcontext, WebServiceHelper.RestMethodType.DELETE,
                "v2/relationship/$id/delete", "Delete_Relationship", jsonObject.toString()
            )
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, mActivity)
        }
    }

    private fun callGroupsWebservice(holder: MyViewHolder, relationshipsModel: RelationshipsModel) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity)
            val groups = relationshipsModel.groups
            if (groups != null) {
                for (j in 0 until groups.length()) {
                    val viewGroupModel = ViewGroupModel()
                    val jsonObject = groups.getJSONObject(j)
                    viewGroupModel.group_id = jsonObject.getString("id")
                    viewGroupModel.group_name = jsonObject.getString("name")
                    viewGroupModel.isCan_delete = jsonObject.optBoolean("can_delete")
                    viewGroupModel.isCan_assign_docs = jsonObject.optBoolean("can_assign_docs")
                    updatedMembersList.add(viewGroupModel)
                }
            }
            relationshipsModel_new = relationshipsModel
            mholder = holder
            val postdata = JSONObject()
            WebServiceHelper.callHttpWebService(
                this, mcontext, WebServiceHelper.RestMethodType.GET,
                "v3/groups", "Get Groups", postdata.toString()
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun callMembersWebservice(holder: MyViewHolder, relationshipsModel: RelationshipsModel) {
        progress_dialog = AndroidUtils.get_progress(mActivity)
        relationshipsModel_new = relationshipsModel
        mholder = holder
        val postdata = JSONObject()
        WebServiceHelper.callHttpWebService(
            this, mcontext, WebServiceHelper.RestMethodType.GET,
            "v2/relationship/" + relationshipsModel.id + "/members",
            "Get Members", postdata.toString()
        )
    }

    override fun getItemCount(): Int {
        return relationshipsList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progress_dialog != null && progress_dialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }
        try {
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                val result = JSONObject(httpResult.responseContent)
                val error = result.optBoolean("error", false)
                val msg = result.optString("msg", "")
                val type = httpResult.requestType

                if (type == "Get Groups") {
                    if (!error) loadViewGroups(result.getJSONArray("data"))
                } else if (type == "Get Members") {
                    if (!error) {
                        val members = result.optJSONArray("members")
                        if (members == null) {
                            Log.d("Get Members", "members array is null in response")
                            AndroidUtils.showAlert("No team members found for " + relationshipsModel_new!!.name + ". Add team members first.", mActivity)
                            return
                        }
                        loadViewMembers(members)
                    } else {
                        AndroidUtils.showAlert(msg, mActivity)
                    }
                } else if (type == "Update Members") {
                    if (error) {
                        AndroidUtils.showAlert(msg, mActivity)
                    } else {
                        AndroidUtils.showAlert(msg, mActivity)
                    }
                    eventListener?.RefreshViewRelationshipsData()
                } else if (type == "Update Group Access") {
                    if (!isCancel) {
                        AndroidUtils.showAlert(msg, mActivity)
                    }
                    if (!error) {
                        groupsList.clear()
                        relationshipsList.clear()

                        if (type == "Update Group Access") {
                            ad_dialog?.dismiss()
                        } else {
                            ad_dialog_delete?.dismiss()
                        }

                        eventListener?.RefreshViewRelationshipsData()
                    }
                    clear_search()
                } else if (type == "Delete Relationship") {
                    if (!error) {
                        groupsList.clear()
                        relationshipsList.clear()

                        if (type == "Update Group Access") {
                            ad_dialog?.dismiss()
                        } else {
                            ad_dialog_delete?.dismiss()
                        }

                        eventListener?.RefreshViewRelationshipsData()
                    }
                    clear_search()
                } else if (type == "Profile") {
                    if (!error) {
                        mholder?.rg_profile?.visibility = VISIBLE
                        loadProfile(result.getJSONObject("data"))
                    }
                    if (TAG == "individuals" && mholder?.tv_individual_first_name?.text.toString().isEmpty()) {
                        callProfileWebservice(relationshipsModel_new!!.id ?: "")
                    } else if (TAG == "individuals" && mholder?.tv_first_name?.text.toString().isEmpty()) {
                        callProfileWebservice(relationshipsModel_new!!.id ?: "")
                    }

                    clear_search()
                } else if (type == "Shared Corp Documents" || type == "Shared Documents") {
                    if (!error) {
                        loadSharedwithmeDocuments(result.getJSONObject("documents"))
                        clear_search()
                    }
                } else if (type == "Copy Document") {
                    ad_dialog_copy?.dismiss()
                    if (error) {
                        AndroidUtils.showAlert(msg, mActivity)
                    } else {
                        AndroidUtils.showAlert(msg, mActivity)
                    }
                    clear_search()
                } else if (type == "UnshareDocuments") {
                    AndroidUtils.showAlert(msg, mActivity)
                    shared_list.clear()
                    selected_unsharedocsList.clear()
                    selected_sharedocsList.clear()
                    selected_firm_sharedocsList.clear()
                    selected_client_sharedocsList.clear()
                    if (ad_dialog_docs != null && ad_dialog_docs!!.isShowing) {
                        ad_dialog_docs!!.dismiss()
                    }
                    clear_search()
                    mholder?.ll_expandable_layout?.visibility = GONE
                } else if (type == "Invite Temp Clients") {
                    AndroidUtils.showAlert(msg, mActivity)
                    clientRelationship?.callViewRelationshipWebservice("", "")
                    if (ad_dialog_docs != null && ad_dialog_docs!!.isShowing) {
                        ad_dialog_docs!!.dismiss()
                    }
                    clear_search()
                } else if (type == "View Doc With Us" || type == "View Other Doc") {
                    if (!error) {
                        val url = if (type == "View Doc With Us") {
                            result.getString("url")
                        } else {
                            result.getJSONObject("data").getString("url")
                        }

                        var ReceivedFileName = ""
                        val content_type = Documents.replaceLastDotWithSlash(sharedDocumentsDo!!.filename ?: "")
                        val content_type1 = content_type.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                        if (content_type1.size >= 2) {
                            ReceivedFileName = content_type1[1]
                        }

                        if (ReceivedFileName.equals("apng", ignoreCase = true) || ReceivedFileName.equals("avif", ignoreCase = true) ||
                            ReceivedFileName.equals("gif", ignoreCase = true) || ReceivedFileName.equals("jpeg", ignoreCase = true) ||
                            ReceivedFileName.equals("png", ignoreCase = true) || ReceivedFileName.equals("svg", ignoreCase = true) ||
                            ReceivedFileName.equals("webp", ignoreCase = true) || ReceivedFileName.equals("jpg", ignoreCase = true)
                        ) {
                            sharedDocumentsDo!!.doctype = "image/$ReceivedFileName"
                        } else {
                            sharedDocumentsDo!!.doctype = "application/$ReceivedFileName"
                        }

                        checkViewType(url, sharedDocumentsDo!!.doctype ?: "", sharedDocumentsDo!!)
                        Log.d("TAG_Image", url)
                    } else {
                        AndroidUtils.showAlert(msg, mActivity)
                    }
                    clear_search()
                } else if (type == "Decrypt Doc" || type == "Other Doc View") {
                    if (!error) {
                        val url = result.getJSONObject("data").getString("url")
                        display_doc(url, sharedDocumentsDo!!)
                        Log.d("TAG_Image", url)
                    } else {
                        AndroidUtils.showAlert(msg, mActivity)
                    }
                    clear_search()
                } else if (type == "Existing Documents") {
                    if (!error) {
                        loadOtherDocs(result.getJSONArray("data"))
                    }
                    clear_search()
                } else if (type == "Remove Groups") {
                    if (result.has("counts")) {
                        val jsonObject = result.getJSONObject("counts")
                        val doc_count = jsonObject.optString("documents")
                        val count = doc_count.toInt()

                        val newGrouplist = ArrayList<ViewGroupModel>()

                        for (i in 0 until groupsList.size) {
                            if (!groupsList[i].isChecked) {
                                newGrouplist.add(groupsList[i])
                            }
                        }

                        oldGrouplist.clear()
                        for (i in 0 until groupsList.size) {
                            if (!groupsList[i].isChecked) {
                                oldGrouplist.add(groupsList[i])
                            }
                        }

                        if (count > 0) {
                            GroupsAssignPopup(groupid, groupname, doc_count, newGrouplist)
                        } else {
                            for (i in 0 until groupsList.size) {
                                val g = groupsList[i]

                                if (g.id == groupid) {
                                    g.isChecked = false
                                    g.isCan_assign_docs = false
                                }
                            }

                            if (GroupRecyclerViewFragment.groupsAdapter != null) {
                                GroupRecyclerViewFragment.groupsAdapter!!.notifyDataSetChanged()
                            }

                            load_selected_groups(groupsList)
                        }
                    }
                } else if (type == "Alter Groups") {
                    val iserror = result.optBoolean("error")
                    if (iserror) {
                        val successmsg = result.getString("msg")
                        AndroidUtils.showErrorAlert(successmsg, mActivity)
                    } else {
                        val successmsg = result.getString("msg")
                        AndroidUtils.showAlert(successmsg, mActivity)
                        UpdateGroups()
                    }
                } else if (type == "Update Groups") {
                    val iserror = result.optBoolean("error")
                    if (!iserror) {
                        val updatemsg = result.getString("msg")
                        GroupRecyclerViewFragment.groupsAdapter!!.notifyDataSetChanged()
                        Constants.isAlterPopup = false
                    } else {
                        AndroidUtils.showAlert(result.getString("msg"), mActivity)
                    }
                } else if (type == "Accept Request") {
                    val iserror = result.optBoolean("error")
                    if (!iserror) {
                        val successmsg = result.getString("msg")
                        AndroidUtils.showAlert(successmsg, mActivity)
                        clientRelationship?.callViewRelationshipWebservice("", "")
                    } else {
                        val successmsg = result.getString("msg")
                        AndroidUtils.showErrorAlert(successmsg, mActivity)
                    }
                } else if (type == "Delete_Relationship") {
                    val iserror = result.optBoolean("error")
                    if (!iserror) {
                        val successmsg = result.getString("msg")
                        AndroidUtils.showAlert(successmsg, mActivity)
                        clientRelationship?.callViewRelationshipWebservice("", "")
                    } else {
                        val successmsg = result.getString("msg")
                        AndroidUtils.showErrorAlert(successmsg, mActivity)
                    }
                } else if (type == "Archive Relationship") {
                    val iserror = result.optBoolean("error")
                    if (!iserror) {
                        val successmsg = result.getString("msg")
                        AndroidUtils.showAlert(successmsg, mActivity)
                        clientRelationship?.callViewRelationshipWebservice("", "")
                    } else {
                        val successmsg = result.getString("msg")
                        AndroidUtils.showErrorAlert(successmsg, mActivity)
                    }
                }
            } else if (httpResult.result == WebServiceHelper.ServiceCallStatus.Failed) {
                try {
                    val result = JSONObject(httpResult.responseContent)
                    if (result.optBoolean("error")) {
                        AndroidUtils.showErrorAlert(result.optString("msg"), mActivity)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                AndroidUtils.showErrorAlert(httpResult.responseContent.toString(), mActivity)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun loadNewGroups(list_item: ArrayList<ViewGroupModel>) {
        val acls = JSONArray()
        if (!list_item.isEmpty()) {
            for (i in 0 until list_item.size) {
                val viewGroupModel = list_item[i]
                if (viewGroupModel.isChecked) {
                    acls.put(viewGroupModel.id)
                }
            }
        }
        if (acls.length() == 0) {
            btn_Delete?.alpha = 0.5f
            btn_Delete?.isEnabled = false
        } else {
            btn_Delete?.alpha = 1.0f
            btn_Delete?.isEnabled = true
        }
    }

    private fun GroupsAssignPopup(
        group_id: String,
        group_name: String,
        doc_count: String,
        groupsList1: ArrayList<ViewGroupModel>
    ) {
        try {
            val dialogBuilder = AlertDialog.Builder(mcontext)
            val inflater = mActivity.layoutInflater
            val view = inflater.inflate(R.layout.assign_to_other_group, null)

            val tv_update_group = view.findViewById<TextView>(R.id.tv_update_group)
            tv_update_group.setText(R.string.update_group)
            tv_update_group.setTextColor(mcontext.getColor(R.color.blue))

            btn_Delete = view.findViewById(R.id.Delete)
            btn_Delete!!.setText(R.string.delete)
            btn_Delete!!.alpha = 0.5f
            btn_Delete!!.isEnabled = false
            val tv_warning_msg = view.findViewById<TextView>(R.id.tv_warning_msg)
            val tv_assign_group = view.findViewById<TextView>(R.id.tv_assign_group)
            tv_assign_group.setText(R.string.assign_to_another_active_groups)
            tv_assign_group.setTextColor(mcontext.getColor(R.color.blue))
            Constants.isAlterPopup = true
            val rv_groups_view = view.findViewById<RecyclerView>(R.id.rv_groups_view)

            val layoutManager = LinearLayoutManager(mcontext, LinearLayoutManager.VERTICAL, false)
            rv_groups_view.layoutManager = layoutManager

            val groupsAdapter1 = GroupsAdapter(groupsList1, this, clientRelationship)
            rv_groups_view.adapter = groupsAdapter1
            val et_search_members = view.findViewById<TextInputEditText>(R.id.tv_search_groups)
            et_search_members.addTextChangedListener(Validation(et_search_members))

            val groupName = SpannableString(group_name)
            groupName.setSpan(ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.black)), 0, groupName.length, 0)
            groupName.setSpan(AbsoluteSizeSpan(18, true), 0, groupName.length, 0)
            groupName.setSpan(StyleSpan(Typeface.BOLD), 0, groupName.length, 0)

            val documentText = SpannableString("$doc_count Documents.")
            documentText.setSpan(ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.blue)), 0, documentText.length, 0)
            documentText.setSpan(AbsoluteSizeSpan(18, true), 0, documentText.length, 0)
            documentText.setSpan(StyleSpan(Typeface.BOLD), 0, documentText.length, 0)

            val msgBuilder = SpannableStringBuilder()
            msgBuilder.append("This ")
                .append("'").append(groupName).append("'")
                .append(" group currently contains ")
                .append(documentText)
                .append(". Before updating, please assign them to another active group.")

            tv_warning_msg.setText(msgBuilder, TextView.BufferType.SPANNABLE)

            val btn_Cancel = view.findViewById<AppCompatButton>(R.id.Cancel)

            btn_Cancel.setOnClickListener {
                unCheckList(groupsList1)
                groupsAdapter1.notifyDataSetChanged()
                ad_dialog_delete?.dismiss()
                Constants.isAlterPopup = false
            }
            val iv_close = view.findViewById<ImageView>(R.id.iv_close)
            iv_close.setOnClickListener {
                unCheckList(groupsList1)
                groupsAdapter1.notifyDataSetChanged()
                ad_dialog_delete?.dismiss()
                Constants.isAlterPopup = false
            }
            btn_Delete!!.setOnClickListener {
                ad_dialog_delete?.dismiss()
                AlterGroups(group_id, groupsList1)
            }
            et_search_members.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable) {
                    groupsAdapter1.filter.filter(s)
                }
            })
            groupsAdapter1.notifyDataSetChanged()
            rv_groups_view.refreshDrawableState()

            val dialog = dialogBuilder.create()
            ad_dialog_delete = dialog
            dialog.setView(view)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, mActivity)
        }
    }

    private fun unCheckList(groupsList1: ArrayList<ViewGroupModel>) {
        if (!groupsList1.isEmpty()) {
            for (i in 0 until groupsList1.size) {
                val viewGroupModel = groupsList1[i]
                viewGroupModel.isChecked = false
            }
        }
    }

    @Throws(JSONException::class)
    private fun loadOtherDocs(docs: JSONArray) {
        shared_list.clear()
        firm_list.clear()

        for (i in 0 until docs.length()) {
            val docs_new = docs.getJSONObject(i)
            val sharedDocumentsDo = createSharedDocumentsDo(docs_new)
            shared_list.add(sharedDocumentsDo)
        }

        val selectedlist = ArrayList<SharedDocumentsDo>()
        for (sharedDoc in shared_list) {
            sharedDoc.isChecked = selected_sharedocsList.contains(sharedDoc)
            selectedlist.add(sharedDoc)
        }
        val differentList = ArrayList<SharedDocumentsDo>()

        for (sharedDoc in selectedlist) {
            if (!shared_by_us_list.contains(sharedDoc)) {
                differentList.add(sharedDoc)
            }
        }

        Log.d("Shared_firm_list", firm_list.size.toString() + "...." + shared_by_us_list.size)
        if (shared_tag == "firm" || shared_tag == "client") {
            openSharedPopupWindow(differentList)
        } else {
            openSharedPopupWindow(shared_list)
        }
    }

    @Throws(JSONException::class)
    private fun createSharedDocumentsDo(docs_new: JSONObject): SharedDocumentsDo {
        val sharedDocumentsDo = SharedDocumentsDo()

        sharedDocumentsDo.content_type = docs_new.optString("content_type")
        sharedDocumentsDo.created = docs_new.optString("created")
        sharedDocumentsDo.description = docs_new.optString("description")
        sharedDocumentsDo.expiration_date = docs_new.optString("expiration_date")
        sharedDocumentsDo.filename = docs_new.optString("filename")
        sharedDocumentsDo.id = docs_new.optString("id")
        sharedDocumentsDo.is_disabled = docs_new.optBoolean("is_disabled")
        sharedDocumentsDo.is_encrypted = docs_new.optBoolean("is_encrypted")
        sharedDocumentsDo.added_encryption = docs_new.optBoolean("added_encryption")
        sharedDocumentsDo.is_password = docs_new.optBoolean("is_password")
        sharedDocumentsDo.name = docs_new.optString("name")
        sharedDocumentsDo.uploaded_by = docs_new.optString("uploaded_by")

        if (docs_new.has("matter_details")) {
            sharedDocumentsDo.matter_details = docs_new.getJSONArray("matter_details")
            val jsonObject = sharedDocumentsDo.matter_details?.optJSONObject(0)
            if (jsonObject != null) {
                sharedDocumentsDo.matter_details_name = jsonObject.optString("name")
                sharedDocumentsDo.matter_details_id = jsonObject.optString("id")
                if (!sharedDocumentsDo.matter_details_name.isNullOrEmpty()) {
                    sharedDocumentsDo.has_Confidential = true
                }
            }
        } else {
            sharedDocumentsDo.has_Confidential = false
        }

        return sharedDocumentsDo
    }

    private fun loadSharedwithmeDocuments(documents: JSONObject) {
        try {
            shared_by_us_list.clear()

            val generalArray = documents.getJSONArray("general")
            processDocumentArray(generalArray)

            processDocumentArray(documents.getJSONArray("credential"))
            processDocumentArray(documents.getJSONArray("merged"))
            processDocumentArray(documents.getJSONArray("versioned"))
            processDocumentArray(documents.getJSONArray("identity"))
            processDocumentArray(documents.getJSONArray("personal"))

            if (highLightId != null && highLightId.isNotEmpty()) {
                var highlightDoc: SharedDocumentsDo? = null
                var indexToMove = -1

                for (i in 0 until shared_list.size) {
                    if (shared_list[i].id == highLightId) {
                        highlightDoc = shared_list[i]
                        indexToMove = i
                        break
                    }
                }

                if (highlightDoc != null && indexToMove != -1) {
                    shared_list.removeAt(indexToMove)
                    shared_list.add(0, highlightDoc)
                }
            }

            if (doc_nature != "share_doc") {
                val unselectedlist = ArrayList<SharedDocumentsDo>()
                for (sharedDoc in shared_list) {
                    sharedDoc.isChecked = selected_unsharedocsList.contains(sharedDoc)
                    unselectedlist.add(sharedDoc)
                }
                openSharedPopupWindow(unselectedlist)
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun processDocumentArray(documentArray: JSONArray) {
        try {
            for (i in 0 until documentArray.length()) {
                val documentObj = documentArray.getJSONObject(i)
                val sharedDocumentsDo = SharedDocumentsDo()

                sharedDocumentsDo.content_type = documentObj.optString("content_type")
                sharedDocumentsDo.created = documentObj.optString("created")
                sharedDocumentsDo.description = documentObj.optString("description")
                sharedDocumentsDo.expiration_date = documentObj.optString("expiration_date")
                sharedDocumentsDo.filename = documentObj.optString("filename")
                sharedDocumentsDo.id = documentObj.optString("id")
                sharedDocumentsDo.is_disabled = documentObj.optBoolean("is_disabled")
                sharedDocumentsDo.is_encrypted = documentObj.optBoolean("is_encrypted")
                sharedDocumentsDo.added_encryption = documentObj.optBoolean("added_encryption")
                sharedDocumentsDo.is_password = documentObj.optBoolean("is_password")
                sharedDocumentsDo.name = documentObj.optString("name")
                sharedDocumentsDo.uploaded_by = documentObj.optString("uploaded_by")

                if (documentObj.has("matter_details")) {
                    sharedDocumentsDo.matter_details = documentObj.getJSONArray("matter_details")
                    val jsonObject = sharedDocumentsDo.matter_details?.optJSONObject(0)
                    if (jsonObject != null) {
                        sharedDocumentsDo.matter_details_name = jsonObject.optString("name")
                        sharedDocumentsDo.matter_details_id = jsonObject.optString("id")
                        if (!sharedDocumentsDo.matter_details_name.isNullOrEmpty()) {
                            sharedDocumentsDo.has_Confidential = true
                        }
                    }
                } else {
                    sharedDocumentsDo.has_Confidential = false
                }
                shared_list.add(sharedDocumentsDo)
            }
            shared_by_us_list.addAll(shared_list)
            Log.d("Shared_by_us_list", "" + shared_by_us_list.size)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun DocArray(general: JSONObject) {}

    @Throws(JSONException::class)
    private fun DocArray(jsonArray: JSONArray, arrayName: String) {
        for (i in 0 until jsonArray.length()) {
            val `object` = jsonArray.getJSONObject(i)
            println("Parsed from $arrayName: $`object`")
        }
    }

    private fun loadDocumentsRecyclerview() {
        if (shared_list.isEmpty()) {
            AndroidUtils.showAlert("No Documents to display", mActivity)
            mholder?.et_Search?.visibility = GONE
        } else {
            mholder?.rv_documents?.removeAllViews()
            mholder?.rv_documents?.layoutManager = GridLayoutManager(mcontext, 1)

            val adapter = SharedDocumentsAdapter(
                shared_list, shared_tag, mcontext, this,
                shared_relationship_id, shared_client_id, mActivity, this, highLightId
            )
            mholder?.rv_documents?.adapter = adapter

            mholder?.et_Search?.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable) {
                    adapter.filter.filter(mholder!!.et_Search.text.toString().trim())
                }
            })
        }
    }

    @Throws(JSONException::class)
    private fun loadProfile(data: JSONObject) {
        try {
            val profileDo = ProfileDo()
            if (TAG != "individuals") {
                profileDo.fullname = data.optString("fullname")
                profileDo.uid = data.optString("uid")
                profileDo.contact_person = data.optString("contact_person")
                profileDo.contact_phone = data.optString("contact_phone")
                profileDo.email = data.optString("email")
                profileDo.website = data.optString("website")
                if (data.has("address")) {
                    val address = data.getJSONObject("address")
                    profileDo.country = address.optString("country")
                    profileDo.address = data.optJSONObject("address")
                    profileDo.house_flat_no = address.optString("house_flat_no")
                    profileDo.street = address.optString("street")
                    profileDo.city_town = address.optString("city_town")
                    profileDo.state = address.optString("state")
                    profileDo.zipcode = address.optString("zipcode")
                }
            } else {
                profileDo.first_name = data.optString("first_name")
                profileDo.last_name = data.optString("last_name")
                profileDo.middle_name = data.optString("middle_name")
                profileDo.dob = data.optString("dob")
                profileDo.email = data.optString("email")
                profileDo.mobile = data.optString("mobile")
                profileDo.citizen = data.optJSONArray("citizen")
                val citizen = data.optJSONArray("citizen")
                profileDo.website = data.optString("website")
                for (i in 0 until Objects.requireNonNull(citizen).length()) {
                    val jsonObject = citizen.getJSONObject(i)
                    if (jsonObject.getString("index") == "citizen_primary") {
                        profileDo.index = jsonObject.optString("index")
                        profileDo.country = jsonObject.optString("country")
                        profileDo.affiliation_type = jsonObject.optString("work_address")
                        profileDo.home_address = jsonObject.optString("home_address")
                        profileDo.work_phone = jsonObject.optString("work_phone")
                        profileDo.alt_phone = jsonObject.optString("alt_phone")
                        break
                    }
                }
            }

            FLAG = "second_click"

            if (TAG == "individuals") {
                mholder?.ll_individual_profile?.visibility = VISIBLE
                mholder?.ll_entity_profile?.visibility = GONE
                mholder?.tv_individual_first_name?.text = profileDo.first_name
                mholder?.tv_individual_last_name?.text = profileDo.last_name
                mholder?.tv_individual_email?.text = profileDo.email
                if (profileDo.mobile != "null") {
                    mholder?.tv_individual_mobile?.text = profileDo.mobile
                } else {
                    mholder?.tv_individual_mobile?.text = ""
                }
                mholder?.tv_individual_country?.text = profileDo.country
                mholder?.tv_individual_home_address?.text = profileDo.home_address
                mholder?.tv_individual_work_phone?.text = profileDo.work_phone
                mholder?.tv_individual_alt_phone?.text = profileDo.alt_phone
            } else {
                mholder?.ll_entity_profile?.visibility = VISIBLE
                mholder?.ll_individual_profile?.visibility = GONE

                mholder?.tv_first_name?.text = profileDo.fullname
                mholder?.tv_email?.text = profileDo.email
                mholder?.tv_country?.text = profileDo.country
                if (profileDo.contact_phone != "null") {
                    mholder?.tv_individual_mobile?.text = profileDo.contact_phone
                } else {
                    mholder?.tv_individual_mobile?.text = ""
                }
                mholder?.tv_contact_name?.text = profileDo.contact_person
                mholder?.tv_mobile?.text = profileDo.contact_phone
                mholder?.tv_website?.text = ""
                mholder?.tv_billing_currency?.text = ""
            }
            mholder?.cv_Profile?.visibility = VISIBLE
        } catch (e: JSONException) {
            e.printStackTrace()
            AndroidUtils.showAlert(e.message, mActivity)
        }
    }

    fun checkRemoveGroups(groups_id: String, groupname: String, groupsList: ArrayList<ViewGroupModel>) {
        try {
            this.groupname = groupname
            this.groupid = groups_id
            groupsList1 = groupsList
            progress_dialog = AndroidUtils.get_progress(mActivity)
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this, mcontext, WebServiceHelper.RestMethodType.GET,
                "v3/relationship/groups/" + relationshipsModel_new!!.client_id + "/" + groups_id,
                "Remove Groups", jsonObject.toString()
            )
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, mActivity)
        }
    }

    fun UpdateGroups() {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity)
            val jsonObject = JSONObject()
            if (!groupsList.isEmpty()) {
                for (i in 0 until groupsList.size) {
                    val viewGroupModel = groupsList[i]
                    if (viewGroupModel.isChecked) {
                        if (viewGroupModel.id == groupid) {
                            groupsList[i].isChecked = false
                            viewGroupModel.isCan_assign_docs = false
                            this.acls.remove(i)
                        } else {
                            this.acls.put(viewGroupModel.id)
                            viewGroupModel.isCan_assign_docs = true
                            groupsList[i].isChecked = true
                        }
                    }
                }
            }
            GroupRecyclerViewFragment.groupsAdapter?.notifyDataSetChanged()
            load_selected_groups(groupsList)
            jsonObject.put("acls", this.acls)
            if (TAG == "corporate") {
                WebServiceHelper.callHttpWebService(
                    this, mcontext, WebServiceHelper.RestMethodType.PATCH,
                    "v3/acl/" + relationshipsModel_new!!.id + "/update", "Update Groups", jsonObject.toString()
                )
            } else {
                WebServiceHelper.callHttpWebService(
                    this, mcontext, WebServiceHelper.RestMethodType.PUT,
                    "v2/relationship/" + relationshipsModel_new!!.id + "/acls", "Update Groups", jsonObject.toString()
                )
            }

        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, mActivity)
        }
    }

    fun AlterGroups(groups_id: String, groupsList: ArrayList<ViewGroupModel>) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity)
            val jsonObject = JSONObject()
            if (!groupsList.isEmpty()) {
                for (i in 0 until groupsList.size) {
                    val viewGroupModel = groupsList[i]
                    if (viewGroupModel.isChecked) {
                        acls.put(viewGroupModel.id)
                    }
                }
            }
            jsonObject.put("new_groups", acls)
            WebServiceHelper.callHttpWebService(
                this, mcontext, WebServiceHelper.RestMethodType.PATCH,
                "v3/relationship/groups/" + relationshipsModel_new!!.client_id + "/" + groups_id,
                "Alter Groups", jsonObject.toString()
            )
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, mActivity)
        }
    }

    private fun callAcceptRequest(id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity)
            val jsonObject = JSONObject()
            if (TAG == "corporate") {
                jsonObject.put("response", "yes")
                WebServiceHelper.callHttpWebService(
                    this, mcontext, WebServiceHelper.RestMethodType.PUT,
                    "v3/corporate/" + id + "/accept", "Accept Request", jsonObject.toString()
                )
            } else {
                WebServiceHelper.callHttpWebService(
                    this, mcontext, WebServiceHelper.RestMethodType.POST,
                    "v2/relationship/" + id + "/accept", "Accept Request", jsonObject.toString()
                )
            }
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, mActivity)
        }
    }

    fun loadSelectedNewGroups(list_item: ArrayList<ViewGroupModel>) {
        if (!list_item.isEmpty()) {
            for (i in 0 until list_item.size) {
                val viewGroupModel = list_item[i]
                if (viewGroupModel.isChecked) {
                    new_groups.put(viewGroupModel.id)
                }
            }
        }
    }

    fun load_selected_groups(list_item: ArrayList<ViewGroupModel>) {
        val acls = JSONArray()
        if (!list_item.isEmpty()) {
            for (i in 0 until list_item.size) {
                val viewGroupModel = list_item[i]
                if (viewGroupModel.isChecked) {
                    acls.put(viewGroupModel.id)
                }
            }
        }
        if (acls.length() == 0) {
            btn_send_request?.alpha = 0.5f
            btn_send_request?.isEnabled = false
            isUpdated.set(false)
        } else {
            btn_send_request?.alpha = 1.0f
            btn_send_request?.isEnabled = true
            isUpdated.set(true)
        }
    }

    @Throws(JSONException::class)
    private fun loadViewMembers(members: JSONArray) {
        try {
            selectedMembersList.clear()
            val membersList = relationshipsModel_new!!.membersList
            if (membersList != null) {
                for (i in 0 until membersList.length()) {
                    val jsonObject = membersList.getJSONObject(i)
                    val selected = MemberModel()
                    selected.id = jsonObject.getString("id")
                    selected.name = jsonObject.getString("name")
                    selectedMembersList.add(selected)
                }
            }

            updateMembersList.clear()
            for (j in 0 until members.length()) {
                val jsonObject = members.getJSONObject(j)
                val id = jsonObject.getString("id")
                val name = jsonObject.getString("name")

                val member = MemberModel()
                member.id = id
                member.name = name
                member.isChecked = false

                for (selected in selectedMembersList) {
                    if (selected.id == id) {
                        member.isChecked = true
                        break
                    }
                }

                updateMembersList.add(member)
            }
            if (!updateMembersList.isEmpty()) {
                open_tm_popup()
            } else {
                AndroidUtils.showAlert(
                    "No team members found for " + relationshipsModel_new!!.name +
                            ". Please add team members to this group first.",
                    mActivity
                )
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    @Throws(JSONException::class)
    private fun loadViewGroups(data: JSONArray) {
        var viewGroupModel: ViewGroupModel
        groupsList.clear()
        for (i in 0 until data.length()) {
            val jsonObject = data.getJSONObject(i)
            viewGroupModel = ViewGroupModel()
            viewGroupModel.id = jsonObject.getString("id")
            val date = jsonObject.getString("created")
            val date_new = AndroidUtils.stringToDateTimeDefault(date, "yyyy-MM-dd'T'HH:mm:ss.SSS")
            val created = AndroidUtils.getDateToString(date_new, "MMM dd YYYY")
            viewGroupModel.created = created
            val members = jsonObject.getJSONArray("members")
            viewGroupModel.members = members
            viewGroupModel.description = jsonObject.getString("description")
            viewGroupModel.name = jsonObject.getString("name")
            val group_head = jsonObject.getJSONObject("groupHead")
            viewGroupModel.group_head_id = group_head.getString("id")
            viewGroupModel.group_head_name = group_head.getString("name")
            viewGroupModel.owner_name = group_head.getString("name")
            for (j in 0 until data.length()) {
                for (k in 0 until updatedMembersList.size) {
                    if (viewGroupModel.id.matches(updatedMembersList[k].group_id.toRegex())) {
                        viewGroupModel.isChecked = true
                        if (updatedMembersList[k].isCan_assign_docs) {
                            viewGroupModel.isCan_assign_docs = true
                        } else {
                            viewGroupModel.isCan_assign_docs = false
                        }
                        if (!updatedMembersList[k].isCan_delete) {
                            viewGroupModel.isCan_delete = false
                        } else {
                            viewGroupModel.isCan_delete = true
                        }
                    }
                }
            }
            if (jsonObject.getString("name") != "AAM" && jsonObject.getString("name") != "SuperUser") {
                groupsList.add(viewGroupModel)
            }
        }
        Log.i("ArrayList", "info" + updatedMembersList.size)
        val mtag = "VG"
        loadGroupsRecylerview()
    }

    private fun loadGroupsRecylerview() {
        clientRelationship?.ll_view_rel?.isEnabled = false
        clientRelationship?.ll_view_rel?.isClickable = false
        clientRelationship?.ll_view_rel?.alpha = 0.5f
        Constants.mainActivity?.Add_Page(GroupRecyclerViewFragment(mcontext, groupsList, clientRelationship, relationshipsModel_new, this))
    }

    @Throws(JSONException::class)
    fun callUpdateGroupAccess(
        id: String, list_item: ArrayList<ViewGroupModel>,
        isCancel: Boolean
    ) {
        this.isCancel = isCancel
        if (isCancel) {
            if (isUpdated.get()) {
                ConfirmPopup(relationshipsModel_new!!.name ?: "", relationshipsModel_new!!.id ?: "", true, true, list_item)
            } else {
                Constants.mainActivity?.Remove_Page(GroupRecyclerViewFragment(mcontext, groupsList, clientRelationship, relationshipsModel_new, this))
                clientRelationship?.ll_view_rel?.isEnabled = true
                clientRelationship?.ll_view_rel?.alpha = 1.0f
                et_search_groups?.setText("")
                groupsList.clear()
                popupView?.visibility = GONE
            }
        } else {
            if (isUpdated.get()) {
                ConfirmPopup(relationshipsModel_new!!.name ?: "", relationshipsModel_new!!.id ?: "", false, true, list_item)
            } else {
                Constants.mainActivity?.Remove_Page(GroupRecyclerViewFragment(mcontext, groupsList, clientRelationship, relationshipsModel_new, this))
                callUpdateGroups(id, list_item)
                clientRelationship?.ll_view_rel?.isEnabled = true
                clientRelationship?.ll_view_rel?.alpha = 1.0f
                et_search_groups?.setText("")
                groupsList.clear()
                popupView?.visibility = GONE
            }
        }
    }

    fun callUpdateGroups(id: String, list_item: ArrayList<ViewGroupModel>) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity)
            val postData = JSONObject()
            val acls = JSONArray()
            if (!list_item.isEmpty()) {
                for (i in 0 until list_item.size) {
                    val viewGroupModel = list_item[i]
                    if (viewGroupModel.isChecked) {
                        acls.put(viewGroupModel.id)
                    }
                }
                postData.put("acls", acls)
                if (TAG == "corporate") {
                    WebServiceHelper.callHttpWebService(
                        this, mcontext, WebServiceHelper.RestMethodType.PATCH,
                        "v3/acl/" + id + "/update", "Update Group Access", postData.toString()
                    )
                } else {
                    WebServiceHelper.callHttpWebService(
                        this, mcontext, WebServiceHelper.RestMethodType.PUT,
                        "v2/relationship/" + id + "/acls", "Update Group Access", postData.toString()
                    )
                }
            } else {
                AndroidUtils.showAlert("No Groups Available", mActivity)
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    open fun setSelected_sharedocsList(list_item: ArrayList<SharedDocumentsDo>) {
        selected_sharedocsList.clear()
        if (shared_tag == "client") {
            selected_client_sharedocsList.clear()
            for (i in 0 until list_item.size) {
                val groupModel = list_item[i]
                if (groupModel.isChecked) {
                    selected_client_sharedocsList.add(groupModel)
                }
            }
        } else if (shared_tag == "firm") {
            selected_firm_sharedocsList.clear()
            for (i in 0 until list_item.size) {
                val groupModel = list_item[i]
                if (groupModel.isChecked) {
                    selected_firm_sharedocsList.add(groupModel)
                }
            }
        }
        selected_sharedocsList.addAll(selected_client_sharedocsList)
        selected_sharedocsList.addAll(selected_firm_sharedocsList)
        Log.d("selected_sharedocsList", "" + selected_sharedocsList.size)
    }

    open fun setSelected_unsharedocsList(list_item: ArrayList<SharedDocumentsDo>) {
        selected_unsharedocsList.clear()
        for (i in 0 until list_item.size) {
            val groupModel = list_item[i]
            if (groupModel.isChecked) {
                selected_unsharedocsList.add(groupModel)
            }
        }
        Log.d("selected_unsharedocsList", "" + selected_unsharedocsList.size)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deletedBy: TextView = itemView.findViewById(R.id.deletedBy)
        val tv_relationship_name: TextView = itemView.findViewById(R.id.tv_relationship_name)
        val tv_created_date: TextView = itemView.findViewById(R.id.tv_created_date)
        val tv_consumer: TextView = itemView.findViewById(R.id.tv_consumer)
        val tv_initiated: TextView = itemView.findViewById(R.id.tv_initiated)
        val tv_first_name: TextView = itemView.findViewById(R.id.tv_first_name)
        val tv_email: TextView = itemView.findViewById(R.id.tv_email)
        val tv_country: TextView = itemView.findViewById(R.id.tv_country)
        val tv_contact_name: TextView = itemView.findViewById(R.id.tv_contact_name)
        val tv_mobile: TextView = itemView.findViewById(R.id.tv_mobile)
        val tv_website: TextView = itemView.findViewById(R.id.tv_website)
        val tv_billing_currency: TextView = itemView.findViewById(R.id.tv_billing_currency)
        val iv_restore_relationships: ImageView = itemView.findViewById(R.id.iv_restore_relationships)
        val iv_initiated: ImageView = itemView.findViewById(R.id.iv_initiated)
        val iv_activate_relationships: ImageView = itemView.findViewById(R.id.iv_activate_relationships)
        val iv_groups_relationships: ImageView = itemView.findViewById(R.id.iv_groups_relationships)
        val iv_tm_relationships: ImageView = itemView.findViewById(R.id.iv_tm_relationships)
        val iv_delete_relationships: ImageView = itemView.findViewById(R.id.iv_delete_relationships)
        val iv_send_invite: ImageView = itemView.findViewById(R.id.iv_send_invite)
        val rg_profile: LinearLayout = itemView.findViewById(R.id.profile)
        val rg_shared_status: LinearLayout = itemView.findViewById(R.id.shared_status)
        val rg_shared_type: LinearLayout = itemView.findViewById(R.id.shared_type)
        val rg_document_type: LinearLayout = itemView.findViewById(R.id.document_type)
        val ll_icons: LinearLayout = itemView.findViewById(R.id.ll_icons)
        val cv_Profile: CardView = itemView.findViewById(R.id.cv_profile)
        val btn_accept: Button = itemView.findViewById(R.id.accept)
        val email: TextView = itemView.findViewById(R.id.email)
        val first_name: TextView = itemView.findViewById(R.id.first_name)
        val country: TextView = itemView.findViewById(R.id.country)
        val contact_name: TextView = itemView.findViewById(R.id.contact_name)
        val mobile: TextView = itemView.findViewById(R.id.mobile)
        val website: TextView = itemView.findViewById(R.id.website)
        val billing_currency: TextView = itemView.findViewById(R.id.billing_currency)
        val individual_last_name: TextView = itemView.findViewById(R.id.individual_last_name)
        val individual_first_name: TextView = itemView.findViewById(R.id.individual_first_name)
        val individual_email: TextView = itemView.findViewById(R.id.individual_email)
        val individual_mobile: TextView = itemView.findViewById(R.id.individual_mobile)
        val individual_country: TextView = itemView.findViewById(R.id.individual_country)
        val individual_home_address: TextView = itemView.findViewById(R.id.individual_home_address)
        val individual_work_phone: TextView = itemView.findViewById(R.id.individual_work_phone)
        val individual_alt_phone: TextView = itemView.findViewById(R.id.individual_alt_phone)
        val tv_individual_first_name: TextView = itemView.findViewById(R.id.tv_individual_first_name)
        val tv_individual_last_name: TextView = itemView.findViewById(R.id.tv_individual_last_name)
        val tv_individual_email: TextView = itemView.findViewById(R.id.tv_individual_email)
        val tv_individual_mobile: TextView = itemView.findViewById(R.id.tv_individual_mobile)
        val tv_individual_country: TextView = itemView.findViewById(R.id.tv_individual_country)
        val tv_individual_home_address: TextView = itemView.findViewById(R.id.tv_individual_home_address)
        val tv_individual_work_phone: TextView = itemView.findViewById(R.id.tv_individual_work_phone)
        val tv_individual_alt_phone: TextView = itemView.findViewById(R.id.tv_individual_alt_phone)
        val ll_documents: LinearLayout = itemView.findViewById(R.id.ll_documents)
        val ll_expandable_layout: LinearLayout = itemView.findViewById(R.id.ll_expandable_layout)
        val nestedScrollView: LinearLayout = itemView.findViewById(R.id.nestedScrollView)
        val ll_shared_with_us: LinearLayout = itemView.findViewById(R.id.ll_shared_with_us)
        val ll_doc_button: LinearLayout = ll_shared_with_us.findViewById(R.id.ll_doc_button)
        val ll_individual_profile: LinearLayout = itemView.findViewById(R.id.ll_individual_profile)
        val ll_entity_profile: LinearLayout = itemView.findViewById(R.id.ll_entity_profile)
        val rv_documents: RecyclerView = itemView.findViewById(R.id.rv_shared_documents)
        val rv_shared_with_us: RecyclerView = ll_shared_with_us.findViewById(R.id.rv_doc_share)
        val cv_relationships_details: CardView = itemView.findViewById(R.id.cv_relationships_details)
        val rb_share_document: TextView = itemView.findViewById(R.id.rb_share_button)
        val tv_more_details: TextView = itemView.findViewById(R.id.tv_more_details)
        val rb_profile: TextView = itemView.findViewById(R.id.rb_profile)
        val rb_shared_by_us: TextView = itemView.findViewById(R.id.rb_shared_by_us)
        val rb_shared_with_us: TextView = itemView.findViewById(R.id.rb_shared_with_us)
        val rb_client_document: TextView = itemView.findViewById(R.id.rb_client_document)
        val rb_firm_document: TextView = itemView.findViewById(R.id.rb_firm_document)
        val et_Search: TextInputEditText = itemView.findViewById(R.id.et_search_documents)
        val et_search_relationships: TextInputEditText = itemView.findViewById(R.id.et_search_relationships)
        val iv_share_docs: com.google.android.material.imageview.ShapeableImageView = itemView.findViewById(R.id.iv_share_docs)
        val custom_spinner_cardview: ImageView = itemView.findViewById(R.id.custom_spinner_cardview)
        val action_list_card: CardView = itemView.findViewById(R.id.action_list_card)
        val sp_action: ListView = itemView.findViewById(R.id.list_client)

        init {
            tv_relationship_name.textSize = DynamicUtils.twenty.toFloat()
            tv_more_details.visibility = GONE
            email.setText(R.string.email__)
            first_name.setText(R.string.first_name__)
            country.setText(R.string.country__)
            contact_name.setText(R.string.contact_name__)
            mobile.setText(R.string.mobile__)
            website.setText(R.string.website__)
            billing_currency.setText(R.string.billing_currency__)
            tv_initiated.textSize = 13f

            iv_groups_relationships.visibility = GONE
            iv_activate_relationships.visibility = GONE
            iv_delete_relationships.setColorFilter(mcontext.getColor(R.color.blue))
            iv_delete_relationships.imageTintList = ColorStateList.valueOf(mcontext.getColor(R.color.blue))
            iv_delete_relationships.visibility = VISIBLE

            individual_first_name.setText(R.string.first_name__)
            individual_last_name.setText(R.string.last_name__)
            individual_mobile.setText(R.string.mobile__)
            individual_email.setText(R.string.email__)
            individual_country.setText(R.string.country__)
            individual_home_address.setText(R.string.home_address)
            individual_work_phone.setText(R.string.work_phone)
            individual_alt_phone.setText(R.string.alt_phone)
            btn_accept.background = mcontext.getDrawable(R.drawable.no_button_green_button)
            btn_accept.setText(R.string.accept)
            rb_profile.setText(R.string.profile)
            et_search_relationships.setHint(R.string.search_documents)
            et_search_relationships.visibility = GONE
            et_search_relationships.addTextChangedListener(Validation(et_search_relationships))
            rb_share_document.setText(R.string.share_document)
            rb_shared_by_us.setText(R.string.shared_by_us)
            rb_shared_with_us.setText(R.string.shared_with_us)

            et_Search.addTextChangedListener(Validation(et_Search))
            et_Search.setHint(R.string.search_documents)
            et_Search.visibility = GONE
            rb_client_document.setText(R.string.client_document)
            rb_firm_document.setText(R.string.firm_document)
        }
    }

    open fun check_select_all(check_status: Boolean) {
        chk_select_all?.isChecked = check_status
    }
}
