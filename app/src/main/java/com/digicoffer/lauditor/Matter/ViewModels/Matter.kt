package com.digicoffer.lauditor.Matter.ViewModels

import android.animation.ValueAnimator
import android.app.Dialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.gct_en
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.Matter.Models.GroupsModel
import com.digicoffer.lauditor.Matter.Models.HistoryModel
import com.digicoffer.lauditor.Matter.Models.MatterModel
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.google.android.material.imageview.ShapeableImageView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class Matter : Fragment(), AsyncTaskCompleteListener, View.OnClickListener {

    var siv_timeline_icon: ShapeableImageView? = null
    var siv_matter_icon: ShapeableImageView? = null
    var siv_groups: ShapeableImageView? = null
    var siv_documents: ShapeableImageView? = null

    private var tv_legal_matter: TextView? = null
    private var tv_general_matter: TextView? = null

    private var tv_create: TextView? = null
    private var tv_view: TextView? = null

    var create_matter_view: LinearLayout? = null
    var tv_create_matter1: LinearLayout? = null
    var tv_view_matter1: LinearLayout? = null
    var ll_matter_info: RelativeLayout? = null
    var ll_timeline: RelativeLayout? = null
    lateinit var ll_matter_type: LinearLayoutCompat
    lateinit var ll_create_view: LinearLayoutCompat

    private var mViewModel: NewModel? = null
    var progress_dialog: Dialog? = null

    var historyList = ArrayList<HistoryModel>()
    var viewMatterModel = ViewMatterModel()
    var header_name = ""
    var chk_viewMatter: ViewMatter? = null
    var jsonArray = JSONArray()
    var matter_info_txt: TextView? = null
    var matter_timeline_txt: TextView? = null
    var itemsArrayList = ArrayList<ViewMatterModel>()
    @JvmField
    var matter_arraylist = ArrayList<MatterModel>()

    private var progressTrack: View? = null
    private var progressFill: View? = null

    private var emptyStateView: LinearLayout? = null
    private var tvEmptyTitle: TextView? = null
    private var tvEmptySubtitle: TextView? = null

    companion object {
        private const val STEP_INFO = 0
        private const val STEP_GCT = 1
        private const val STEP_DOCUMENTS = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.create_matter, container, false)

        mViewModel = ViewModelProvider(requireActivity())[NewModel::class.java]

        matter_info_txt = view.findViewById(R.id.matter_info_txt)
        matter_info_txt?.setText(R.string.matter_information)
        matter_info_txt?.setTextColor(requireContext().getColor(R.color.black))

        val matter_gct_txt = view.findViewById<TextView>(R.id.matter_gct_txt)
        matter_gct_txt.setTextColor(requireContext().getColor(R.color.black))
        if ("solo" == Constants.CATEGORY) {
            matter_gct_txt.setText(R.string.client_s)
        } else {
            matter_gct_txt.setText(R.string.group_s_clients_amp_team_member_s)
        }

        val matter_doc_txt = view.findViewById<TextView>(R.id.matter_doc_txt)
        matter_doc_txt.setTextColor(requireContext().getColor(R.color.black))
        matter_doc_txt.setText(R.string.document_s)

        mViewModel?.setData("Matter")

        matter_timeline_txt = view.findViewById(R.id.matter_timeline_txt)
        matter_timeline_txt?.setTextColor(requireContext().getColor(R.color.black))

        ll_matter_info = view.findViewById(R.id.ll_matter_info)
        ll_timeline = view.findViewById(R.id.ll_timeline)
        siv_timeline_icon = view.findViewById(R.id.siv_timeline_icon)
        siv_matter_icon = view.findViewById(R.id.siv_matter_icon)
        siv_groups = view.findViewById(R.id.siv_groups)
        siv_documents = view.findViewById(R.id.siv_documents)

        create_matter_view = view.findViewById(R.id.create_matter_view)
        ll_matter_type = view.findViewById(R.id.ll_matter_type)
        ll_create_view = view.findViewById(R.id.ll_create_view)

        tv_legal_matter = view.findViewById(R.id.tv_legal_matter)
        tv_general_matter = view.findViewById(R.id.tv_general_matter)
        tv_legal_matter?.setText(R.string.legal_matter)
        tv_general_matter?.setText(R.string.general_matter)

        tv_create = view.findViewById(R.id.tv_create_matter)
        tv_create_matter1 = view.findViewById(R.id.tv_create_matter1)
        tv_view_matter1 = view.findViewById(R.id.tv_view_matter1)
        tv_view_matter1?.visibility = GONE

        progressTrack = view.findViewById(R.id.progress_track)
        progressFill = view.findViewById(R.id.progress_fill)

        emptyStateView = view.findViewById(R.id.empty_state_view)
        tvEmptyTitle = view.findViewById(R.id.tv_empty_title)
        tvEmptySubtitle = view.findViewById(R.id.tv_empty_subtitle)

        ll_timeline?.visibility = GONE
        ll_matter_info?.visibility = VISIBLE

        AndroidUtils.setupModuleView(
            tv_create_matter1,
            getString(R.string.create_matter),
            true,
            true,
            context,
            getString(R.string.list_of_legal_matters)
        ) {
            if (!Constants.is_active) {
                AndroidUtils.showRenewalPopup(requireActivity())
            } else {
                Constants.Matter_id = ""
                Constants.upload_documents_list.clear()
                Constants.create_matter = true
                Constants.Matter_CreateOrViewDetails = "Create"
                matter_arraylist.clear()
                hideEmptyState()
                loadCreateUI()
            }
        }

        AndroidUtils.setupModuleView(
            tv_view_matter1,
            getString(R.string.view_matter),
            false,
            false,
            context,
            getString(R.string.create_matter)
        ) { loadViewUI() }

        ll_create_view.visibility = GONE
        tv_create?.setText(R.string.create)
        tv_view = view.findViewById(R.id.tv_view_matter)
        tv_view?.setText(R.string.view)

        Constants.is_CreateMatter = Constants.isCreate

        val filterType = Constants.matterFilterType
        if (!filterType.isNullOrEmpty()) {
            Constants.matterFilterType = ""
            matter_arraylist.clear()
            if (filterType.equals("General", ignoreCase = true)) {
                loadGeneralMatter()
            } else {
                loadLegalMatter()
            }
        } else if (Constants.MATTER_TYPE == "General") {
            matter_arraylist.clear()
            loadGeneralMatter()
        } else {
            matter_arraylist.clear()
            loadLegalMatter()
        }

        tv_legal_matter?.setOnClickListener {
            Constants.Matter_id = ""
            matter_arraylist.clear()
            loadLegalMatter()
        }

        tv_general_matter?.setOnClickListener {
            Constants.Matter_id = ""
            matter_arraylist.clear()
            loadGeneralMatter()
        }

        tv_view?.setOnClickListener { loadViewUI() }

        siv_timeline_icon?.setOnClickListener { loadTimeline() }

        siv_matter_icon?.setOnClickListener {
            if (!Constants.create_matter) {
                loadMatterInformation()
            } else if (matter_arraylist.isNotEmpty() && Constants.Matter_CreateOrViewDetails.equals("Create", ignoreCase = true)) {
                loadMatterInformation()
            }
        }

        siv_groups?.setOnClickListener {
            if (!Constants.create_matter) {
                Matter_Gct()
            } else if (matter_arraylist.isEmpty() || (matter_arraylist[0].matter_title ?: "").isEmpty()) {
                AndroidUtils.showAlert("Please check the Matter \n Information section", activity, "Info")
            } else {
                val matterId = Constants.Matter_id
                if (!matterId.isNullOrEmpty()) {
                    loadGCT()
                } else {
                    val infoEn = Constants.matterInformation_en
                    if (infoEn != null) {
                        infoEn.CheckUnique()
                    }
                }
            }
        }

        siv_documents?.setOnClickListener {
            if (!Constants.create_matter) {
                Matter_Doc()
            } else if (matter_arraylist.isEmpty() || (matter_arraylist[0].matter_title ?: "").isEmpty()) {
                AndroidUtils.showAlert("Please check the Matter \n Information section", activity, "Info")
            } else {
                try {
                    val matterId = Constants.Matter_id
                    if (!matterId.isNullOrEmpty()) {
                        if (matter_arraylist.isNotEmpty()) {
                            for (matterModel in matter_arraylist) {
                                val isMemberValid = matterModel.members != null && matterModel.members.length() > 0
                                val isClientValid = matterModel.clients != null && matterModel.clients.length() > 0
                                val isCorpValid = matterModel.corp_client_id != null && (matterModel.corp_client_id ?: "").isNotEmpty()
                                val isTempClientsValid = matterModel.temp_clients_list != null && matterModel.temp_clients_list.length() > 0

                                if (isMemberValid || isClientValid || isCorpValid || isTempClientsValid) {
                                    gct_en?.update_matter()
                                } else {
                                    loadDocuments()
                                }
                            }
                        }
                    } else {
                        val infoEn = Constants.matterInformation_en
                        if (infoEn != null) {
                            infoEn.CheckUnique()
                        }
                    }
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
            }
        }

        matter_arraylist = ArrayList()
        return view
    }

    private fun String.equalsIgnoreCase(other: String?): Boolean {
        return this.equals(other, ignoreCase = true)
    }

    fun showEmptyState(isCreateMode: Boolean) {
        if (emptyStateView == null) return

        tvEmptyTitle?.text = "No Matters Yet!"
        tvEmptySubtitle?.text = "Secure and organize your matters by start creating it."

        val childContainer = view?.findViewById<View>(R.id.child_container)
        childContainer?.visibility = GONE

        emptyStateView?.visibility = VISIBLE
    }

    fun hideEmptyState() {
        if (emptyStateView == null) return

        emptyStateView?.visibility = GONE

        val childContainer = view?.findViewById<View>(R.id.child_container)
        childContainer?.visibility = VISIBLE
    }

    private fun updateProgressBar(step: Int, isViewTimeline: Boolean) {
        if (progressTrack == null || progressFill == null) return

        if (isViewTimeline) {
            progressTrack?.visibility = View.GONE
            progressFill?.visibility = View.GONE
            return
        }

        progressTrack?.visibility = View.VISIBLE
        progressFill?.visibility = View.VISIBLE

        progressTrack?.post {
            val totalWidth = progressTrack?.width ?: 0

            val targetWidth = when (step) {
                STEP_GCT -> totalWidth / 2
                STEP_DOCUMENTS -> totalWidth
                else -> 0
            }

            val startWidth = progressFill?.width ?: 0

            val anim = ValueAnimator.ofInt(startWidth, targetWidth)
            anim.duration = 300
            anim.addUpdateListener { animator ->
                val valWidth = animator.animatedValue as Int
                val p = progressFill?.layoutParams
                if (p != null) {
                    p.width = valWidth
                    progressFill?.layoutParams = p
                }
            }
            anim.start()
        }
    }

    fun getMatter_arraylist(): ArrayList<MatterModel> {
        return matter_arraylist
    }

    fun loadViewUI() {
        Constants.GeneratedMatterId = ""
        Constants.Matter_id = ""
        Constants.allClientGroups.clear()
        Constants.upload_documents_list.clear()
        Constants.selected_temp_clients_list.clear()
        Constants.is_CreateMatter = false

        tv_create?.background = requireContext().resources.getDrawable(R.drawable.button_left_background)
        tv_create?.setTextColor(Color.BLACK)
        tv_view?.background = requireContext().resources.getDrawable(R.drawable.button_right_green_count)
        tv_view?.setTextColor(Color.WHITE)

        create_matter_view?.visibility = GONE
        ll_matter_type.visibility = VISIBLE
        ll_create_view.visibility = GONE
        tv_create_matter1?.visibility = VISIBLE
        tv_view_matter1?.visibility = GONE

        updateProgressBar(STEP_INFO, true)

        callGroupsWebservice()
        viewMatter()

        if (Constants.MATTER_TYPE == "Legal") {
            mViewModel?.setData("View Legal Matter")
            AndroidUtils.updateModuleTitle(
                tv_create_matter1,
                getString(R.string.list_of_legal_matters)
            )
        } else {
            mViewModel?.setData("View General Matter")
            AndroidUtils.updateModuleTitle(
                tv_create_matter1,
                getString(R.string.list_of_general_matters)
            )
        }
    }

    fun View_Details(
        historyList: ArrayList<HistoryModel>,
        viewMatter: ViewMatter,
        header_name: String,
        viewMatterModel: ViewMatterModel
    ) {
        chk_viewMatter = viewMatter
        matter_arraylist.add(0, viewMatterModel)

        if (Constants.owner_id.isNullOrEmpty()) {
            val jsonObject1 = viewMatterModel.owner
            if (jsonObject1 != null) {
                Constants.owner_id = jsonObject1.optString("id")
                Constants.owner_name = jsonObject1.optString("name")
            }
        }

        this.historyList = historyList
        this.header_name = header_name
        this.viewMatterModel = viewMatterModel

        loadTimeline()
    }

    private fun loadTimeline() {
        Constants.create_matter = false
        Constants.Matter_id = viewMatterModel.id ?: ""

        ll_matter_type.visibility = GONE
        ll_create_view.visibility = GONE
        tv_create_matter1?.visibility = GONE
        tv_view_matter1?.visibility = VISIBLE
        ll_matter_info?.visibility = GONE
        ll_timeline?.visibility = VISIBLE

        siv_matter_icon?.setImageDrawable(requireContext().resources.getDrawable(R.drawable.single_document_icon))
        siv_matter_icon?.colorFilter = null
        updateIconStates(siv_timeline_icon)

        updateProgressBar(STEP_INFO, true)
        hideEmptyState()

        create_matter_view?.visibility = VISIBLE
        matter_info_txt?.setText(R.string.matter_information)
        matter_timeline_txt?.setText(R.string.timeline)

        val ft = childFragmentManager.beginTransaction()
        val childFragment = TimeLine(
            historyList, chk_viewMatter!!, header_name, this, viewMatterModel
        )
        ft.replace(R.id.child_container, childFragment)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.addToBackStack(null)
        ft.commit()
    }

    private fun viewMatter() {
        hideEmptyState()

        val ft = childFragmentManager.beginTransaction()
        val matterInformation = ViewMatter()
        ft.replace(R.id.child_container, matterInformation)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.addToBackStack(null)
        ft.commit()
    }

    private fun loadCreateUI() {
        Constants.GeneratedMatterId = ""
        Constants.selected_temp_clients_list.clear()
        Constants.is_CreateMatter = true
        Constants.allClientGroups.clear()

        create_matter_view?.visibility = VISIBLE
        ll_create_view.visibility = GONE
        tv_create_matter1?.visibility = GONE
        tv_view_matter1?.visibility = VISIBLE
        matter_info_txt?.setText(R.string.matter_information)

        if (Constants.Matter_CreateOrViewDetails?.equals("Create", ignoreCase = true) == true) {
            tv_create?.background = requireContext().resources.getDrawable(R.drawable.button_left_green_background)
            tv_create?.setTextColor(Color.WHITE)
            tv_view?.background = requireContext().resources.getDrawable(R.drawable.button_right_background)
            tv_view?.setTextColor(Color.BLACK)

            if (Constants.MATTER_TYPE == "Legal") {
                mViewModel?.setData("Create Legal Matter")
            } else {
                mViewModel?.setData("Create General Matter")
            }
            loadMatterInformation()
        }
    }

    fun loadLegalMatter() {
        Constants.MATTER_TYPE = "Legal"
        tv_legal_matter?.background = requireContext().resources.getDrawable(R.drawable.button_left_green_background)
        tv_legal_matter?.setTextColor(Color.WHITE)
        tv_general_matter?.background = requireContext().resources.getDrawable(R.drawable.button_right_background)
        tv_general_matter?.setTextColor(Color.BLACK)

        if (Constants.is_CreateMatter) {
            loadCreateUI()
        } else {
            loadViewUI()
        }
    }

    fun loadGeneralMatter() {
        Constants.MATTER_TYPE = "General"
        tv_legal_matter?.background = requireContext().resources.getDrawable(R.drawable.button_left_background)
        tv_legal_matter?.setTextColor(Color.BLACK)
        tv_general_matter?.background = requireContext().resources.getDrawable(R.drawable.button_right_green_count)
        tv_general_matter?.setTextColor(Color.WHITE)

        if (Constants.is_CreateMatter) {
            loadCreateUI()
        } else {
            loadViewUI()
        }
    }

    fun loadDocuments() {
        siv_matter_icon?.setImageDrawable(requireContext().resources.getDrawable(R.drawable.single_document_icon))
        siv_matter_icon?.colorFilter = null
        updateIconStates(siv_documents)

        updateProgressBar(STEP_DOCUMENTS, false)
        hideEmptyState()

        val ft = childFragmentManager.beginTransaction()
        val matterInformation = MatterDocuments_En()
        ft.replace(R.id.child_container, matterInformation)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.addToBackStack(null)
        ft.commit()
    }

    fun loadGCT() {
        siv_matter_icon?.setImageDrawable(requireContext().resources.getDrawable(R.drawable.single_document_icon))
        siv_matter_icon?.colorFilter = null
        updateIconStates(siv_groups)

        updateProgressBar(STEP_GCT, false)
        hideEmptyState()

        val ft = childFragmentManager.beginTransaction()
        val childFragment = GCT_En()
        ft.replace(R.id.child_container, childFragment)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.addToBackStack(null)
        ft.commit()
    }

    fun Matter_Notes() {
        siv_matter_icon?.setImageDrawable(requireContext().resources.getDrawable(R.mipmap.timeline_green))
        siv_groups?.setImageDrawable(requireContext().resources.getDrawable(R.mipmap.frame_white_background))
        siv_groups?.isClickable = true
        siv_documents?.setImageDrawable(requireContext().resources.getDrawable(R.mipmap.white_document))
        siv_documents?.isClickable = true
    }

    fun Matter_Gct() {
        siv_matter_icon?.setImageDrawable(requireContext().resources.getDrawable(R.drawable.single_document_icon))
        siv_matter_icon?.colorFilter = null
        updateIconStates(siv_groups)

        updateProgressBar(
            STEP_GCT,
            Constants.Matter_CreateOrViewDetails == "Edit Matter Info"
        )
        hideEmptyState()

        val ft = childFragmentManager.beginTransaction()
        val childFragment = GCT_En()
        ft.replace(R.id.child_container, childFragment)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.addToBackStack(null)
        ft.commit()
    }

    fun Matter_Doc() {
        siv_matter_icon?.setImageDrawable(requireContext().resources.getDrawable(R.drawable.single_document_icon))
        siv_matter_icon?.colorFilter = null
        updateIconStates(siv_documents)

        updateProgressBar(
            STEP_DOCUMENTS,
            Constants.Matter_CreateOrViewDetails == "Edit Matter Info"
        )
        hideEmptyState()

        val ft = childFragmentManager.beginTransaction()
        val matterInformation = MatterDocuments_En()
        ft.replace(R.id.child_container, matterInformation)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.addToBackStack(null)
        ft.commit()
    }

    fun loadMatterInformation() {
        ll_timeline?.visibility = GONE
        ll_matter_info?.visibility = VISIBLE

        siv_matter_icon?.setImageDrawable(requireContext().resources.getDrawable(R.drawable.single_document_icon_white))
        siv_matter_icon?.colorFilter = null

        updateIconStates(null)
        updateProgressBar(STEP_INFO, false)
        hideEmptyState()

        siv_groups?.isClickable = true
        siv_documents?.isClickable = true

        val ft = childFragmentManager.beginTransaction()
        val matterInformation = MatterInformation_En()
        ft.replace(R.id.child_container, matterInformation)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.addToBackStack(null)
        ft.commit()
    }

    fun View_Details(
        viewMatterModel: ViewMatterModel,
        viewMatter: ViewMatter,
        historyList: ArrayList<HistoryModel>,
        header_name: String
    ) {
        chk_viewMatter = viewMatter
        Constants.create_matter = false

        ll_matter_type.visibility = GONE
        ll_create_view.visibility = GONE
        tv_create_matter1?.visibility = GONE
        tv_view_matter1?.visibility = VISIBLE

        siv_matter_icon?.setImageDrawable(requireContext().resources.getDrawable(R.drawable.single_document_icon_white))
        siv_groups?.setImageDrawable(requireContext().resources.getDrawable(R.mipmap.frame_white_background))
        siv_groups?.isClickable = true
        siv_documents?.setImageDrawable(requireContext().resources.getDrawable(R.mipmap.white_document))
        siv_documents?.isClickable = true

        create_matter_view?.visibility = VISIBLE
        matter_info_txt?.setText(R.string.matter_information)
        matter_arraylist.add(0, viewMatterModel)
        Constants.Matter_id = viewMatterModel.id ?: ""

        updateProgressBar(STEP_INFO, false)
        hideEmptyState()

        loadMatterInformation()
    }

    private fun callGroupsWebservice() {
        progress_dialog = AndroidUtils.get_progress(activity)
        val postdata = JSONObject()
        WebServiceHelper.callHttpWebService(
            this,
            requireContext(),
            WebServiceHelper.RestMethodType.PUT,
            "matter/attachments",
            "Groups",
            postdata.toString()
        )
    }

    override fun onClick(view: View) {
        // reserved
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progress_dialog != null && progress_dialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }

        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent)
                if (httpResult.requestType == "Groups") {
                    val data = result.getJSONArray("data")
                    loadGroupsData(data)
                }
            } catch (e: JSONException) {
                e.fillInStackTrace()
            }
        }
    }

    private fun loadGroupsData(data: JSONArray) {
        try {
            Constants.groupsList_Access.clear()
            for (i in 0 until data.length()) {
                val jsonObject = data.getJSONObject(i)
                val groupsModel = GroupsModel()
                groupsModel.group_id = jsonObject.getString("id")
                groupsModel.group_name = jsonObject.getString("name")
                if (jsonObject.optString("name") != "AAM" && jsonObject.optString("name") != "SuperUser") {
                    Constants.groupsList_Access.add(groupsModel)
                }
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    private fun updateIconStates(activeIcon: ShapeableImageView?) {
        val navIcons = arrayOf(
            siv_timeline_icon,
            siv_groups,
            siv_documents
        )
        val navDrawables = intArrayOf(
            R.drawable.timeline_new,
            R.drawable.gct_new,
            R.drawable.documents_new
        )

        for (i in navIcons.indices) {
            val icon = navIcons[i] ?: continue
            val ctx = context ?: continue

            val isActive = (icon == activeIcon)

            icon.setBackgroundColor(
                ctx.getColor(
                    if (isActive) R.color.scheduled_text else R.color.white
                )
            )

            icon.setImageDrawable(ctx.resources.getDrawable(navDrawables[i]))

            icon.setColorFilter(
                ctx.getColor(if (isActive) R.color.white else R.color.black),
                PorterDuff.Mode.SRC_IN
            )
        }
    }
}
