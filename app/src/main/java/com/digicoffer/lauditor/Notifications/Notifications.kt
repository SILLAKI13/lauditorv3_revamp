package com.digicoffer.lauditor.Notifications

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NotificationCountApi
import com.digicoffer.lauditor.Notifications.Models.Navigation
import com.digicoffer.lauditor.Notifications.Models.NotificationsDo
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList

class Notifications : Fragment(), AsyncTaskCompleteListener, View.OnClickListener, NotificationsAdapter.EventListener {

    private var rv_notifications: RecyclerView? = null
    private var tv_notification_count: TextView? = null
    private var btn_delete_all: ImageView? = null
    private var ib_read: ImageView? = null
    private var chk_select_all: CheckBox? = null
    private var et_Search: EditText? = null
    private var layout_empty_state: LinearLayout? = null

    private var mViewModel: NewModel? = null
    private var adapter: NotificationsAdapter? = null
    private var progress_dialog: Dialog? = null
    private var selectedNotification: NotificationsDo? = null
    private var isAllSelected = false
    private val notificationList = ArrayList<NotificationsDo>()
    private var pendingHighlightIds = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.notifications, container, false)

        mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
        mViewModel?.setData("Notifications")

        bindViews(v)
        setupSearch()
        setupSelectAll()
        setupActionButtons()
        loadRecyclerView()
        callWebservice()
        handleNotificationNavigation()
        return v
    }

    private fun bindViews(v: View) {
        tv_notification_count = v.findViewById(R.id.tv_notification_count)
        btn_delete_all = v.findViewById(R.id.btn_delete_all)
        ib_read = v.findViewById(R.id.ib_read)
        chk_select_all = v.findViewById(R.id.chk_select_all)
        et_Search = v.findViewById(R.id.et_Search)
        rv_notifications = v.findViewById(R.id.rv_list1)
        layout_empty_state = v.findViewById(R.id.layout_empty_state)

        setActionButtonsEnabled(false)
    }

    private fun loadRecyclerView() {
        val spanCount = if (com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils.isTablet(requireContext())) 2 else 1
        rv_notifications?.layoutManager = GridLayoutManager(requireContext(), spanCount)
        adapter = NotificationsAdapter(notificationList, this, this, pendingHighlightIds)
        rv_notifications?.adapter = adapter
        AndroidUtils.setupBottomSpacerFooter(
            rv_notifications,
            resources.getDimensionPixelSize(R.dimen.twentyeight_dp)
        )
        rv_notifications?.layoutAnimation =
            AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_fall_down)
        rv_notifications?.scheduleLayoutAnimation()
    }

    private fun setupSearch() {
        et_Search?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                adapter?.filter?.filter(s?.toString() ?: "")
            }
        })
    }

    private fun setupSelectAll() {
        chk_select_all?.setOnClickListener {
            if (adapter == null) return@setOnClickListener
            isAllSelected = !isAllSelected
            updateSelectAllIcon(isAllSelected)
            adapter?.selectOrDeselectAll(isAllSelected)
            adapter?.let { ad -> load_list(ad.getList_item()) }
        }
    }

    private fun updateSelectAllIcon(selected: Boolean) {
        chk_select_all?.isChecked = selected
    }

    private fun setupActionButtons() {
        btn_delete_all?.setOnClickListener {
            if (adapter == null) return@setOnClickListener
            AndroidUtils.showConfirmationDialog(
                requireContext(),
                "Confirmation",
                "Are you sure you want to delete the selected notifications?",
                object : AndroidUtils.OnConfirmListener {
                    override fun onSave() {
                        adapter?.let { ad -> deleteSelectedNotifications(ad.getList_item()) }
                    }

                    override fun onCancel() {}
                }
            )
        }

        ib_read?.setOnClickListener {
            adapter?.let { ad -> readSelectedNotifications(ad.getList_item()) }
        }
    }

    fun load_list(list: ArrayList<NotificationsDo>) {
        var hasSelection = false
        for (n in list) {
            if (n.isChecked) {
                hasSelection = true
                break
            }
        }
        setActionButtonsEnabled(hasSelection)
    }

    private fun setActionButtonsEnabled(enabled: Boolean) {
        btn_delete_all?.alpha = if (enabled) 1.0f else 0.4f
        btn_delete_all?.isEnabled = enabled
        ib_read?.alpha = if (enabled) 1.0f else 0.4f
        ib_read?.isEnabled = enabled
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            layout_empty_state?.visibility = View.VISIBLE
            rv_notifications?.visibility = View.GONE
        } else {
            layout_empty_state?.visibility = View.GONE
            rv_notifications?.visibility = View.VISIBLE
        }
    }

    private fun callWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(activity)
            WebServiceHelper.callHttpWebService(
                this, requireContext(), WebServiceHelper.RestMethodType.GET,
                "notification", "NOTIFICATIONS", JSONObject().toString()
            )
        } catch (e: Exception) {
            dismissProgress()
        }
    }

    fun callDeleteNotificationsWebservice(id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(activity)
            val arr = JSONArray()
            arr.put(id)
            WebServiceHelper.callHttpWebService(
                this, requireContext(), WebServiceHelper.RestMethodType.PUT,
                "notification/delete", "DELETE_NOTIFICATIONS", arr.toString()
            )
        } catch (e: Exception) {
            dismissProgress()
        }
    }

    fun readSingleNotification(id: String?, item: NotificationsDo) {
        try {
            progress_dialog = AndroidUtils.get_progress(activity)
            val arr = JSONArray()
            arr.put(id)
            selectedNotification = item
            WebServiceHelper.callHttpWebService(
                this, requireContext(), WebServiceHelper.RestMethodType.PUT,
                "notification/read", "READ_SINGLE", arr.toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            dismissProgress()
        }
    }

    private fun readSelectedNotifications(list: ArrayList<NotificationsDo>) {
        try {
            val arr = JSONArray()
            for (n in list) {
                if (n.isChecked) arr.put(n.id)
            }
            if (arr.length() == 0) {
                AndroidUtils.showToast("Select at least 1 notification", activity)
                return
            }
            progress_dialog = AndroidUtils.get_progress(activity)
            WebServiceHelper.callHttpWebService(
                this, requireContext(), WebServiceHelper.RestMethodType.PUT,
                "notification/read", "READ_NOTIFICATIONS", arr.toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteSelectedNotifications(list: ArrayList<NotificationsDo>) {
        try {
            val arr = JSONArray()
            for (n in list) {
                if (n.isChecked) arr.put(n.id)
            }
            if (arr.length() == 0) return
            progress_dialog = AndroidUtils.get_progress(activity)
            WebServiceHelper.callHttpWebService(
                this, requireContext(), WebServiceHelper.RestMethodType.PUT,
                "notification/delete", "DELETE_NOTIFICATIONS", arr.toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openNotificationNavigation(nav: Navigation?) {
        if (nav == null) {
            callWebservice()
            return
        }
        AndroidUtils.setupNotificationHandler(requireContext(), nav)
    }

    fun loadNotificationsData(data: JSONObject) {
        try {
            val jsonArray = data.getJSONArray("notifications")
            notificationList.clear()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val n = NotificationsDo()

                n.id = obj.getString("id")
                n.message = obj.getString("message")
                n.timestamp = obj.getString("timestamp")
                n.status = obj.getString("status")

                if (obj.has("priority")) {
                    n.priority = obj.getInt("priority")
                }

                if (obj.has("navigation") && !obj.isNull("navigation")) {
                    val navObj = obj.getJSONObject("navigation")
                    val nav = Navigation()
                    nav.route_name = navObj.getString("route_name")
                    if (navObj.has("params")) {
                        nav.params = navObj.getJSONObject("params")
                    }
                    n.navigation = nav
                }

                notificationList.add(n)
            }

            updateUI()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateUI() {
        tv_notification_count?.text = notificationList.size.toString()

        toggleEmptyState(notificationList.isEmpty())

        isAllSelected = false
        updateSelectAllIcon(false)
        setActionButtonsEnabled(false)

        if (adapter == null) {
            loadRecyclerView()
        } else {
            adapter = NotificationsAdapter(notificationList, this, this, pendingHighlightIds)
            rv_notifications?.adapter = adapter
            AndroidUtils.setupBottomSpacerFooter(
                rv_notifications,
                resources.getDimensionPixelSize(R.dimen.twentyeight_dp)
            )
            rv_notifications?.scheduleLayoutAnimation()
        }
    }

    private fun handleNotificationNavigation() {
        val bundle = Constants.notificationBundle
        pendingHighlightIds = bundle.getString(Constants.NavKeys.APPOINTMENT_ID) ?: ""
        Constants.isFromNotification = false
        Constants.notificationBundle.clear()
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo?) {
        dismissProgress()
        if (httpResult == null) return

        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent ?: "")
                val type = httpResult.requestType

                when (type) {
                    "NOTIFICATIONS" -> {
                        if (!result.getBoolean("error")) {
                            val response = JSONObject(result.getString("data"))
                            setActionButtonsEnabled(false)
                            loadNotificationsData(response)
                        } else {
                            AndroidUtils.showValidationALert(
                                "Alert",
                                result.getString("msg"), requireContext()
                            )
                        }
                    }

                    "READ_SINGLE" -> {
                        if (!result.getBoolean("error") && selectedNotification != null) {
                            selectedNotification?.status = "read"
                            openNotificationNavigation(selectedNotification?.navigation)
                            selectedNotification = null
                        }
                    }

                    "DELETE_NOTIFICATIONS" -> {
                        AndroidUtils.showAlert(
                            result.getString("msg"), activity,
                            if (result.getBoolean("error")) null else ""
                        )
                        callWebservice()
                        refreshNotificationCount()
                    }

                    "READ_NOTIFICATIONS" -> {
                        AndroidUtils.showAlert(
                            result.getString("msg"), activity,
                            if (result.getBoolean("error")) null else ""
                        )
                        callWebservice()
                        refreshNotificationCount()
                    }
                }

            } catch (e: Exception) {
                AndroidUtils.logMsg(e.message)
            }
        } else {
            AndroidUtils.showAlert(httpResult.responseContent, activity)
        }
    }

    override fun onEvent(notification_id: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Alert")
            .setMessage("Are you sure you want to delete this notification?")
            .setPositiveButton("Yes") { dialog, which ->
                callDeleteNotificationsWebservice(notification_id)
            }
            .setNegativeButton("No", null)
            .setCancelable(true)
            .show()
    }

    override fun onSelectAllChanged(allSelected: Boolean) {
        isAllSelected = allSelected
        updateSelectAllIcon(allSelected)
    }

    override fun onClick(view: View?) {}

    private fun dismissProgress() {
        if (progress_dialog != null && progress_dialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }
    }

    private fun refreshNotificationCount() {
        NotificationCountApi(requireContext()).fetchNotificationCount()
    }
}
