package com.digicoffer.lauditor.Dashboard.NewRevampViewModels

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Appointments.ViewModels.Appointments
import com.digicoffer.lauditor.Chat.ViewModels.Chat
import com.digicoffer.lauditor.CommonFiles.ChatService.ConversationMetaApi
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.VitacapeExtention
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.unreadclient_list
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.unreadcount_from_list
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.unreadteam_list
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.Dashboard.DahboardModels.UnreadCountModel
import com.digicoffer.lauditor.Groups.Groups
import com.digicoffer.lauditor.Matter.ViewModels.Matter
import com.digicoffer.lauditor.Meetings.ViewModels.Meetings
import com.digicoffer.lauditor.Notifications.Models.Navigation
import com.digicoffer.lauditor.Notifications.Notifications
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.TimeSheets.ViewModels.TimeSheets
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class Dashboard : Fragment(), AsyncTaskCompleteListener, View.OnClickListener {

    companion object {
        private const val REQ_DASHBOARD = "DASHBOARD_ALL"
        private const val AUTH_REQUEST_CODE = 1001
    }

    private val teamchatlist = ArrayList<String>()
    private val clientchatlist = ArrayList<String>()
    private val totalchatlist = ArrayList<String>()

    private lateinit var rvOuter: RecyclerView
    private var progressDialog: Dialog? = null

    private lateinit var outerAdapter: DashboardOuterAdapter
    private lateinit var mViewModel: NewModel
    private var menuHighlightListener: MenuHighlightListener? = null

    private val mainHandler = Handler(Looper.getMainLooper())

    private var isDashboardApiComplete = false
    private var isChatListApiComplete = false
    private var isFirstLoad = true
    private var hasRetriedDashboardStartup = false
    private var isRetryingDashboard = false
    private var dashboardRequestStartTime = 0L

    override fun onCreateView(
        inf: LayoutInflater,
        c: ViewGroup?,
        s: Bundle?
    ): View? {
        return inf.inflate(R.layout.fragment_dashboard, c, false)
    }

    @SuppressLint("MissingInflatedId")
    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        try {
            mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
            mViewModel.setData(getString(R.string.lauditor))

            bindViews(v)
            setupRecyclerView()

            Constants.dashboard_en = this
            Constants.recyclerView = rvOuter

            hasRetriedDashboardStartup = false
            isRetryingDashboard = false

            loadDashboard()
        } catch (e: Exception) {
            Log.e(TAG, "onViewCreated", e)
        }
    }

    override fun onDestroyView() {
        dismissProgress()
        mainHandler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    private fun showProgress() {
        try {
            val act = activity
            if (progressDialog == null && isFirstLoad && act != null && !act.isFinishing && !act.isDestroyed) {
                progressDialog = Dialog(act).apply {
                    requestWindowFeature(Window.FEATURE_NO_TITLE)
                    setContentView(R.layout.loading)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    setCancelable(false)
                    setCanceledOnTouchOutside(false)
                }
                progressDialog?.show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "showProgress error", e)
        }
    }

    private fun dismissProgress() {
        try {
            progressDialog?.let { dialog ->
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
            progressDialog = null
        } catch (e: Exception) {
            Log.e(TAG, "dismissProgress error", e)
        }
    }

    private fun checkAndDismissProgress() {
        if (isDashboardApiComplete && isChatListApiComplete) {
            dismissProgress()
            isFirstLoad = false
        }
    }

    private fun bindViews(v: View) {
        rvOuter = v.findViewById(R.id.rv_outer)
        rvOuter.alpha = 0f
        rvOuter.visibility = View.VISIBLE
    }

    private fun resetApiFlags() {
        isDashboardApiComplete = false
        isChatListApiComplete = false
        hasRetriedDashboardStartup = false
        isRetryingDashboard = false
    }

    private fun setupRecyclerView() {
        val llm = LinearLayoutManager(requireContext())
        llm.initialPrefetchItemCount = 10
        llm.isItemPrefetchEnabled = true

        rvOuter.layoutManager = llm
        rvOuter.setHasFixedSize(true)
        rvOuter.isNestedScrollingEnabled = true
        rvOuter.setItemViewCacheSize(50)
        rvOuter.isDrawingCacheEnabled = true
        rvOuter.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH

        val animator = CustomItemAnimator()
        animator.addDuration = 400
        animator.moveDuration = 350
        animator.changeDuration = 300
        rvOuter.itemAnimator = animator

        rvOuter.recycledViewPool.setMaxRecycledViews(0, 25)
        AndroidUtils.setupEdgePaddingBehavior(rvOuter)

        outerAdapter = DashboardOuterAdapter(requireContext(), cardActionListener)
        rvOuter.adapter = outerAdapter
        AndroidUtils.setupBottomSpacerFooter(
            rvOuter,
            resources.getDimensionPixelSize(R.dimen.twentyeight_dp)
        )
    }

    private inner class CustomItemAnimator : DefaultItemAnimator() {
        override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
            val view = holder.itemView
            view.alpha = 0f
            view.translationY = 50f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(350)
                .setInterpolator(DecelerateInterpolator(1.5f))
                .start()
            return super.animateAdd(holder)
        }

        override fun animateChange(
            oldHolder: RecyclerView.ViewHolder?,
            newHolder: RecyclerView.ViewHolder?,
            fromX: Int,
            fromY: Int,
            toX: Int,
            toY: Int
        ): Boolean {
            if (newHolder != null) {
                val view = newHolder.itemView
                view.alpha = 0f
                view.scaleX = 0.95f
                view.scaleY = 0.95f
                view.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .setInterpolator(OvershootInterpolator(0.8f))
                    .start()
            }
            return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY)
        }
    }

    private fun loadDashboard() {
        if (isFirstLoad) {
            showProgress()
        }
        dashboardRequestStartTime = System.currentTimeMillis()
        Log.d("STARTUP_NETWORK_TRACE", "DASHBOARD_INITIAL_START (isFirstLoad=$isFirstLoad, hasRetried=$hasRetriedDashboardStartup)")

        try {
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                Constants.dashboardAllEndpoint!!,
                REQ_DASHBOARD,
                JSONObject().toString()
            )
        } catch (e: Exception) {
            Log.e(TAG, "loadDashboard", e)
            isDashboardApiComplete = true
            checkAndDismissProgress()
        }
    }

    private fun loadDashboardWithSwipe() {
        hasRetriedDashboardStartup = false
        isRetryingDashboard = false
        dashboardRequestStartTime = System.currentTimeMillis()
        try {
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                Constants.dashboardAllEndpoint!!,
                REQ_DASHBOARD,
                JSONObject().toString()
            )
        } catch (e: Exception) {
            Log.e(TAG, "loadDashboardWithSwipe", e)
            isDashboardApiComplete = true
            checkAndDismissProgress()
        }
    }

    private fun callchatlist() {
        try {
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                Constants.chatlistEndpoint!!,
                "CHAT_LIST",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            Log.e(TAG, "callchatlist", e)
            isChatListApiComplete = true
            checkAndDismissProgress()
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        runOnMain {
            if (!isAdded) return@runOnMain
            val reqType = httpResult.requestType
            val elapsed = System.currentTimeMillis() - dashboardRequestStartTime

            if (httpResult.result != WebServiceHelper.ServiceCallStatus.Success) {
                Log.e(TAG, "API error: " + httpResult.result)

                // Check for explicit no-internet condition (Pre-flight network check failed)
                val isNoInternet = (httpResult.result == WebServiceHelper.ServiceCallStatus.Exception) &&
                        (httpResult.responseContent == Constants.NO_INTERNET_MSG || httpResult.responseContent.contains(Constants.NO_INTERNET_MSG, ignoreCase = true))

                // Check for startup transport exception retry (INITIAL DASHBOARD_ALL only, status_code == 0 / Exception)
                val isTransportException = (httpResult.result == WebServiceHelper.ServiceCallStatus.Exception) && (httpResult.status_code == 0)
                if (REQ_DASHBOARD.equals(reqType, ignoreCase = true) && isFirstLoad && isTransportException && !hasRetriedDashboardStartup && !isNoInternet) {
                    hasRetriedDashboardStartup = true
                    isRetryingDashboard = true
                    Log.d("STARTUP_NETWORK_TRACE", "DASHBOARD_INITIAL_FAILURE: status_code=${httpResult.status_code}, error=${httpResult.errorMessage}, elapsed=${elapsed}ms -> scheduling retry in 1200ms")

                    mainHandler.postDelayed({
                        if (isAdded && !isDetached) {
                            Log.d("STARTUP_NETWORK_TRACE", "DASHBOARD_RETRY_START")
                            dashboardRequestStartTime = System.currentTimeMillis()
                            try {
                                WebServiceHelper.callHttpWebService(
                                    this@Dashboard, requireContext(),
                                    WebServiceHelper.RestMethodType.GET,
                                    Constants.dashboardAllEndpoint!!,
                                    REQ_DASHBOARD,
                                    JSONObject().toString()
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "DASHBOARD_RETRY launch error", e)
                                Log.d("STARTUP_NETWORK_TRACE", "DASHBOARD_RETRY_FAILURE (launch exception): ${e.message}")
                                activity?.let { AndroidUtils.showAlert("Something went wrong, please try again.", it) }
                                isDashboardApiComplete = true
                                isRetryingDashboard = false
                                checkAndDismissProgress()
                            }
                        }
                    }, 1200L)
                    return@runOnMain
                }

                if (isNoInternet) {
                    Log.d("STARTUP_NETWORK_TRACE", "DASHBOARD_NO_INTERNET: status_code=${httpResult.status_code}, response=${httpResult.responseContent}, elapsed=${elapsed}ms -> SHOW_NO_INTERNET_ALERT")
                } else if (REQ_DASHBOARD.equals(reqType, ignoreCase = true) && isRetryingDashboard) {
                    Log.d("STARTUP_NETWORK_TRACE", "DASHBOARD_RETRY_FAILURE: status_code=${httpResult.status_code}, error=${httpResult.errorMessage}, elapsed=${elapsed}ms -> FINAL_ERROR_DIALOG")
                    isRetryingDashboard = false
                } else if (REQ_DASHBOARD.equals(reqType, ignoreCase = true)) {
                    Log.d("STARTUP_NETWORK_TRACE", "FINAL_ERROR_DIALOG: reqType=$reqType, status_code=${httpResult.status_code}, error=${httpResult.errorMessage}, elapsed=${elapsed}ms")
                }

                val alertMessage = if (isNoInternet) {
                    Constants.NO_INTERNET_MSG
                } else {
                    "Something went wrong, please try again."
                }

                activity?.let { AndroidUtils.showAlert(alertMessage, it) }

                if (REQ_DASHBOARD.equals(reqType, ignoreCase = true)) {
                    isDashboardApiComplete = true
                } else if ("CHAT_LIST".equals(reqType, ignoreCase = true)) {
                    isChatListApiComplete = true
                }
                checkAndDismissProgress()
                return@runOnMain
            }

            // Success branch
            if (REQ_DASHBOARD.equals(reqType, ignoreCase = true)) {
                if (isRetryingDashboard) {
                    Log.d("STARTUP_NETWORK_TRACE", "DASHBOARD_RETRY_SUCCESS (elapsed=${elapsed}ms)")
                    isRetryingDashboard = false
                } else {
                    Log.d("STARTUP_NETWORK_TRACE", "DASHBOARD_SUCCESS (elapsed=${elapsed}ms)")
                }
            }

            try {
                val result = JSONObject(httpResult.responseContent)

                if ("CHAT_LIST".equals(reqType, ignoreCase = true)) {
                    handleChatList(result)
                    isChatListApiComplete = true
                    checkAndDismissProgress()
                } else if (REQ_DASHBOARD.equals(reqType, ignoreCase = true)) {
                    handleDashboard(result)
                    isDashboardApiComplete = true
                    checkAndDismissProgress()
                }

            } catch (e: JSONException) {
                Log.e(TAG, "JSON parse error", e)
                activity?.let { AndroidUtils.showAlert("Something went wrong, please try again.", it) }

                if (REQ_DASHBOARD.equals(reqType, ignoreCase = true)) {
                    isDashboardApiComplete = true
                } else if ("CHAT_LIST".equals(reqType, ignoreCase = true)) {
                    isChatListApiComplete = true
                }
                checkAndDismissProgress()
            }
        }
    }

    @Throws(JSONException::class)
    private fun handleChatList(result: JSONObject) {
        val team_array = result.optJSONArray("team") ?: JSONArray()
        val client_array = result.optJSONArray("clients") ?: JSONArray()

        unreadteam_list.clear()
        teamchatlist.clear()

        for (i in 0 until team_array.length()) {
            val m = UnreadCountModel()
            Constants.listid1 = team_array.optString(i)
            m.fromjid = Constants.listid1
            unreadteam_list.add(m)
            teamchatlist.add(Constants.listid1!!)
        }

        unreadclient_list.clear()
        clientchatlist.clear()

        for (i in 0 until client_array.length()) {
            val m = UnreadCountModel()
            Constants.listid = client_array.optString(i)
            m.fromjid = Constants.listid
            unreadclient_list.add(m)
            clientchatlist.add(Constants.listid!!)
        }

        totalchatlist.clear()
        for (id in teamchatlist) {
            totalchatlist.add(id + VitacapeExtention)
        }
        for (id in clientchatlist) {
            totalchatlist.add(id + VitacapeExtention)
        }

        val totalArray = JSONArray()
        for (item in totalchatlist) {
            totalArray.put(item)
        }
        Constants.totalchatclientlist = totalArray

        ConversationMetaApi.fetch(requireContext(), totalchatlist, object : ConversationMetaApi.MetaCallback {
            override fun onMetaReady(metaMap: Map<String, ConversationMetaApi.ConversationMeta>) {
                Constants.unreadList.clear()
                unreadcount_from_list.clear()

                for (jid in totalchatlist) {
                    val guid = jid.replace(VitacapeExtention!!, "")

                    val meta = metaMap[guid] ?: continue

                    val unreadCountModel = UnreadCountModel()
                    unreadCountModel.fromjid = guid
                    unreadCountModel.count = meta.unreadCount
                    Constants.unreadList.add(unreadCountModel)
                    unreadcount_from_list.add(unreadCountModel)
                }
            }
        })
    }

    @Throws(JSONException::class)
    private fun handleDashboard(result: JSONObject) {
        if (result.optBoolean("error", false)) {
            val msg = result.optString("msg", "Something went wrong")
            Log.e(TAG, "Server error: $msg")
            activity?.let { AndroidUtils.showAlert(msg, it) }
            return
        }

        Log.d("Dashboard_Result", result.toString())

        Thread {
            try {
                val all = DashboardParser.parse(result)
                val sections = buildSections(all)

                runOnMain {
                    outerAdapter.submitSections(sections)
                    animateDashboardPremium()
                    callchatlist()
                }
            } catch (e: Exception) {
                Log.e(TAG, "DashboardParser.parse error", e)
                runOnMain { activity?.let { AndroidUtils.showAlert("Something went wrong, please try again.", it) } }
            }
        }.start()
    }

    private fun animateDashboardPremium() {
        if (!::rvOuter.isInitialized) return

        rvOuter.animate()
            .alpha(1f)
            .setDuration(500)
            .setInterpolator(DecelerateInterpolator())
            .start()

        rvOuter.postDelayed({
            val llm = rvOuter.layoutManager as? LinearLayoutManager ?: return@postDelayed

            val first = llm.findFirstVisibleItemPosition()
            val last = llm.findLastVisibleItemPosition()

            if (first == -1) return@postDelayed

            for (i in first..last) {
                val child = llm.findViewByPosition(i) ?: continue
                animatePremiumCard(child, i - first)
            }
        }, 50)
    }

    private fun animatePremiumCard(card: View, position: Int) {
        val delay: Long = when (position) {
            0 -> 100
            1 -> 180
            2 -> 260
            else -> Math.min(260 + (position - 2) * 50L, 500L)
        }

        card.alpha = 0f
        card.translationY = 60f
        card.scaleX = 0.92f
        card.scaleY = 0.92f

        card.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(delay)
            .setDuration(450)
            .setInterpolator(OvershootInterpolator(0.6f))
            .start()

        card.animate()
            .translationZ(8f)
            .setStartDelay(delay)
            .setDuration(300)
            .withEndAction {
                card.animate()
                    .translationZ(0f)
                    .setDuration(300)
                    .start()
            }
            .start()
    }

    private fun buildSections(all: List<DashboardItem>): List<DashboardSection> {
        val role = Constants.ROLE

        val today = DashboardSection(DashboardSection.SECTION_TODAY, "Today's Activities")
        val analytics = DashboardSection(DashboardSection.SECTION_ANALYTICS, "Analytics Overview")
        val metrics = DashboardSection(DashboardSection.SECTION_METRICS, "Business Metrics")

        for (item in all) {
            if (!shouldShowCard(item.type, role)) continue
            when (item.type) {
                DashboardItem.TYPE_MEETING,
                DashboardItem.TYPE_APPOINTMENT,
                DashboardItem.TYPE_MESSAGES,
                DashboardItem.TYPE_NOTIFICATION -> today.items.add(item)

                DashboardItem.TYPE_APPOINTMENT_TREND,
                DashboardItem.TYPE_REVENUE_TREND,
                DashboardItem.TYPE_MATTER,
                DashboardItem.TYPE_STORAGE -> analytics.items.add(item)

                DashboardItem.TYPE_BILLABLE,
                DashboardItem.TYPE_APPROX_REVENUE,
                DashboardItem.TYPE_SUBSCRIPTION,
                DashboardItem.TYPE_HIRING -> metrics.items.add(item)
            }
        }

        val result = ArrayList<DashboardSection>()
        if (today.items.isNotEmpty()) result.add(today)
        if (analytics.items.isNotEmpty()) result.add(analytics)
        if (metrics.items.isNotEmpty()) result.add(metrics)
        return result
    }

    private fun shouldShowCard(type: Int, role: String?): Boolean {
        val allowed = ArrayList<Int>()
        when (role) {
            "SU" -> {
                allowed.addAll(
                    listOf(
                        DashboardItem.TYPE_MEETING, DashboardItem.TYPE_APPOINTMENT,
                        DashboardItem.TYPE_MESSAGES, DashboardItem.TYPE_NOTIFICATION,
                        DashboardItem.TYPE_APPOINTMENT_TREND, DashboardItem.TYPE_REVENUE_TREND,
                        DashboardItem.TYPE_MATTER, DashboardItem.TYPE_STORAGE,
                        DashboardItem.TYPE_BILLABLE, DashboardItem.TYPE_APPROX_REVENUE,
                        DashboardItem.TYPE_SUBSCRIPTION
                    )
                )
                if ("solo" != Constants.CATEGORY) allowed.add(DashboardItem.TYPE_HIRING)
            }
            "AAM" -> allowed.addAll(
                listOf(
                    DashboardItem.TYPE_MEETING, DashboardItem.TYPE_APPOINTMENT,
                    DashboardItem.TYPE_MESSAGES, DashboardItem.TYPE_NOTIFICATION,
                    DashboardItem.TYPE_APPOINTMENT_TREND, DashboardItem.TYPE_REVENUE_TREND,
                    DashboardItem.TYPE_STORAGE, DashboardItem.TYPE_APPROX_REVENUE,
                    DashboardItem.TYPE_SUBSCRIPTION
                )
            )
            "GH" -> allowed.addAll(
                listOf(
                    DashboardItem.TYPE_MEETING, DashboardItem.TYPE_APPOINTMENT,
                    DashboardItem.TYPE_MESSAGES, DashboardItem.TYPE_NOTIFICATION,
                    DashboardItem.TYPE_APPOINTMENT_TREND, DashboardItem.TYPE_REVENUE_TREND,
                    DashboardItem.TYPE_MATTER, DashboardItem.TYPE_STORAGE,
                    DashboardItem.TYPE_BILLABLE, DashboardItem.TYPE_APPROX_REVENUE
                )
            )
            "TM" -> allowed.addAll(
                listOf(
                    DashboardItem.TYPE_MEETING, DashboardItem.TYPE_APPOINTMENT,
                    DashboardItem.TYPE_MESSAGES, DashboardItem.TYPE_NOTIFICATION,
                    DashboardItem.TYPE_APPOINTMENT_TREND, DashboardItem.TYPE_REVENUE_TREND,
                    DashboardItem.TYPE_MATTER, DashboardItem.TYPE_BILLABLE
                )
            )
        }
        return allowed.contains(type)
    }

    fun refreshChatCounts() {
        runOnMain {
            if (::outerAdapter.isInitialized) outerAdapter.notifyDataSetChanged()
        }
    }

    interface MenuHighlightListener {
        fun highlightMenuForCardType(cardType: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MenuHighlightListener) {
            menuHighlightListener = context
        }
    }

    private val cardActionListener = object : DashboardCardAdapter.CardActionListener {
        override fun onNavigate(cardType: Int, navigation: Navigation?) {
            handleNavigation(cardType, navigation, null)
        }

        override fun onNavigate(cardType: Int, navigation: Navigation?, subType: String?) {
            handleNavigation(cardType, navigation, subType)
        }

        override fun onPaySubscription(planLabel: String?, validityText: String?) {
            launchPaySubscriptionPage(planLabel, validityText)
        }
    }

    private fun handleNavigation(
        cardType: Int,
        navigation: Navigation?,
        subType: String?
    ) {
        try {
            menuHighlightListener?.highlightMenuForCardType(cardType)

            if (navigation != null && !navigation.route_name.isNullOrEmpty()) {
                Constants.isFromNotification = true
                AndroidUtils.setupNotificationHandler(requireContext(), navigation)
                return
            }

            when (cardType) {
                DashboardItem.TYPE_MATTER -> {
                    Constants.is_CreateMatter = false
                    Constants.isCreate = false
                    Constants.matterFilterType = subType ?: ""
                    Page_Navigation(Matter())
                }
                DashboardItem.TYPE_BILLABLE,
                DashboardItem.TYPE_APPROX_REVENUE -> Page_Navigation(TimeSheets())
                DashboardItem.TYPE_HIRING -> Page_Navigation(Groups())
                DashboardItem.TYPE_NOTIFICATION -> Page_Navigation(Notifications())
                DashboardItem.TYPE_MESSAGES -> Page_Navigation(Chat())
                DashboardItem.TYPE_MEETING -> Page_Navigation(Meetings())
                DashboardItem.TYPE_APPOINTMENT -> Page_Navigation(Appointments())
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleNavigation error", e)
        }
    }

    private fun runOnMain(r: Runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run()
        } else {
            mainHandler.post(r)
        }
    }

    fun Page_Navigation(fragment: Fragment) {
        Constants.mainActivity?.navigation_items(fragment)
    }

    fun launchPaySubscriptionPage(email: String?, userCount: String?) {
        startActivityForResult(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://staging.payment.digicoffer.com/renew?useremail=$email&users=$userCount")
            ),
            AUTH_REQUEST_CODE
        )
    }

    override fun onClick(view: View) { /* no-op */ }
}
