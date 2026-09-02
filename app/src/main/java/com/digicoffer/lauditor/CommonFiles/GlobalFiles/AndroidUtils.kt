package com.digicoffer.lauditor.CommonFiles.GlobalFiles

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.ViewParent
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AbsListView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.NumberPicker
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.digicoffer.lauditor.Appointments.ViewModels.Appointments
import com.digicoffer.lauditor.Meetings.ViewModels.Meetings
import com.digicoffer.lauditor.Meetings.MonthCalander.CalendarMonthView
import com.digicoffer.lauditor.Meetings.ViewModels.WeeklyCalendar
import com.digicoffer.lauditor.Chat.ViewModels.Chat
import com.digicoffer.lauditor.Relationships.ClientRelationship
import com.digicoffer.lauditor.DocEditor.DocEditor
import com.digicoffer.lauditor.Documents.ViewModel.DocumentsEn
import com.digicoffer.lauditor.Documents.Models.DocumentsModel
import com.digicoffer.lauditor.Notifications.Models.Navigation
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnection
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnectionService
import com.digicoffer.lauditor.CommonFiles.CacheUtils.AppImageCache
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.digicoffer.lauditor.Email.Email
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.text.DateFormatSymbols
import java.text.Format
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.ArrayList
import java.util.Arrays
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Objects
import java.util.TimeZone
import java.util.function.Supplier

class AndroidUtils {

    fun interface OnModuleClickListener {
        fun onClick(view: View)
    }

    interface OnModuleClickListeners {
        fun onCreateClick(view: View)
        fun onViewClick(view: View)
    }

    interface OnConfirmListener {
        fun onSave()
        fun onCancel()
    }

    class FooterWrapperAdapter(inner: RecyclerView.Adapter<*>, private val spacerHeightPx: Int) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val VIEW_TYPE_FOOTER = Int.MAX_VALUE - 1
        private val inner: RecyclerView.Adapter<RecyclerView.ViewHolder> = inner as RecyclerView.Adapter<RecyclerView.ViewHolder>

        init {
            this.inner.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    notifyDataSetChanged()
                }

                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    notifyItemRangeInserted(positionStart, itemCount)
                }

                override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                    notifyItemRangeRemoved(positionStart, itemCount)
                }

                override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                    notifyItemRangeChanged(positionStart, itemCount)
                }
            })
        }

        fun getRealItemCount(): Int {
            return inner.itemCount
        }

        fun getWrappedAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder> {
            return inner
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == inner.itemCount) VIEW_TYPE_FOOTER else inner.getItemViewType(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == VIEW_TYPE_FOOTER) {
                val spacer = View(parent.context)
                spacer.layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT, spacerHeightPx
                )
                object : RecyclerView.ViewHolder(spacer) {}
            } else {
                inner.onCreateViewHolder(parent, viewType)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (position < inner.itemCount) {
                inner.onBindViewHolder(holder, position)
            }
        }

        override fun getItemCount(): Int {
            return inner.itemCount + 1
        }

        override fun getItemId(position: Int): Long {
            return if (position == inner.itemCount) Long.MIN_VALUE else inner.getItemId(position)
        }
    }

    companion object {
        @JvmField
        var f_Calendar: Calendar = Calendar.getInstance()
        @JvmField
        var attempts: Int = 1
        private var isPasswordVisible: Boolean = false
        const val PERMISSION_REQUEST_CAMERA_AUDIO: Int = 123
        private const val TAG: String = "AndroidUtils"
        private var isPopupShowing: Boolean = false
        private const val PERMISSION_REQUEST_CODE: Int = 1001
        @JvmField
        var currentDialog: Dialog? = null
        @JvmField
        var currentWebView: WebView? = null
        @JvmField
        var isWebViewShowing: Boolean = false
        @JvmField
        var currentDelegate: VideoCallDelegate? = null
        @JvmField
        var currentUrl: String = ""

        @JvmField
        var displayDialog: Dialog? = null

        @JvmField
        var startCalendar: Calendar = Calendar.getInstance()
        @JvmField
        var endCalendar: Calendar = Calendar.getInstance()

        @JvmStatic
        fun logMsg(msg: String?) {
        }

        @JvmStatic
        fun setupEdgePaddingBehavior(rvOuter: RecyclerView) {
            val bottomPaddingPx = rvOuter.resources.getDimensionPixelSize(R.dimen.forty_dp)
            rvOuter.setPadding(
                rvOuter.paddingLeft,
                rvOuter.paddingTop,
                rvOuter.paddingRight,
                0
            )

            rvOuter.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING) {
                        recyclerView.setPadding(
                            recyclerView.paddingLeft,
                            recyclerView.paddingTop,
                            recyclerView.paddingRight,
                            bottomPaddingPx
                        )
                    } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        recyclerView.setPadding(
                            recyclerView.paddingLeft,
                            recyclerView.paddingTop,
                            recyclerView.paddingRight,
                            0
                        )
                    }
                }
            })
        }

        @JvmStatic
        fun setupBottomSpacerFooter(rvOuter: RecyclerView?, spacerHeightPx: Int): FooterWrapperAdapter? {
            if (rvOuter == null) return null
            val originalAdapter = rvOuter.adapter ?: return null
            val wrapped = FooterWrapperAdapter(originalAdapter, spacerHeightPx)
            rvOuter.adapter = wrapped
            rvOuter.setPadding(
                rvOuter.paddingLeft,
                rvOuter.paddingTop,
                rvOuter.paddingRight,
                0
            )
            return wrapped
        }

        @JvmStatic
        fun setupModuleView(
            moduleView: View?,
            text: String?,
            isCreate: Boolean,
            isTitleShow: Boolean,
            context: Context?,
            titleName: String?,
            listener: OnModuleClickListener?
        ) {
            if (moduleView == null) return
            val title_name = moduleView.findViewById<TextView>(R.id.title_name)
            val tvName = moduleView.findViewById<TextView>(R.id.tv_module_name)
            val ivIcon = moduleView.findViewById<ImageView>(R.id.iv_Icon)
            val ll_module = moduleView.findViewById<LinearLayout>(R.id.ll_module)
            tvName.text = text
            if (isTitleShow) {
                title_name.visibility = VISIBLE
            } else {
                title_name.visibility = GONE
            }
            if (isCreate) {
                if (context != null) {
                    ivIcon.setImageDrawable(context.getDrawable(R.drawable.simple_plus_icon))
                }
            } else {
                ivIcon.setColorFilter(Color.WHITE)
                if (context != null) {
                    ivIcon.setImageDrawable(context.getDrawable(R.drawable.eye_icon))
                }
            }
            title_name.text = titleName
            ll_module.setOnClickListener { v ->
                listener?.onClick(v)
            }
        }

        @JvmStatic
        fun setupModuleView(
            moduleView: View?,
            text: String?,
            isCreate: Boolean,
            isTitleShow: Boolean,
            isModuleShow: Boolean,
            context: Context?,
            titleName: String?,
            listener: OnModuleClickListener?
        ) {
            if (moduleView == null) return
            val title_name = moduleView.findViewById<TextView>(R.id.title_name)
            val tvName = moduleView.findViewById<TextView>(R.id.tv_module_name)
            val ivIcon = moduleView.findViewById<ImageView>(R.id.iv_Icon)
            val ll_module = moduleView.findViewById<LinearLayout>(R.id.ll_module)
            tvName.text = text
            if (isTitleShow) {
                title_name.visibility = VISIBLE
            } else {
                title_name.visibility = GONE
            }
            if (isCreate) {
                if (context != null) {
                    ivIcon.setImageDrawable(context.getDrawable(R.drawable.simple_plus_icon))
                }
            } else {
                ivIcon.setColorFilter(Color.WHITE)
                if (context != null) {
                    ivIcon.setImageDrawable(context.getDrawable(R.drawable.eye_icon))
                }
            }
            title_name.text = titleName
            if (isModuleShow) {
                ll_module.visibility = VISIBLE
            } else {
                val params = LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(DynamicUtils.twenty, DynamicUtils.twenty, DynamicUtils.twenty, 0)
                moduleView.layoutParams = params
                ll_module.visibility = GONE
            }
            ll_module.setOnClickListener { v ->
                listener?.onClick(v)
            }
        }

        @JvmStatic
        fun updateModuleTitle(moduleView: View?, title: String?) {
            if (moduleView == null) return
            val title_name = moduleView.findViewById<TextView>(R.id.title_name)
            if (title_name != null) {
                title_name.visibility = View.VISIBLE
                title_name.text = title
            }
        }

        @JvmStatic
        fun getChatTimeText(date: Date?): String {
            if (date == null) return ""
            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)
            return if (date.time >= today.timeInMillis) {
                getDateToString(date, "hh:mm a")
            } else {
                getDateToString(date, "dd MMM, yyyy")
            }
        }

        @JvmStatic
        fun setupNotificationHandler(ctx: Context?, navigation: Navigation?) {
            Constants.isFromNotification = true
            if (navigation == null || navigation.route_name == null) return
            val route = navigation.route_name
            val params = navigation.params
            Constants.notificationBundle.clear()
            Constants.notificationBundle.putString(Constants.NavKeys.ROUTE_NAME, route)
            try {
                if (params != null) {
                    if (params.has("event_id")) {
                        Constants.notificationBundle.putString(Constants.NavKeys.EVENT_ID, params.getString("event_id"))
                    }
                    if (params.has("meeting_id")) {
                        Constants.notificationBundle.putString(Constants.NavKeys.EVENT_ID, params.getString("meeting_id"))
                    }
                    if (params.has("matter_id")) {
                        Constants.notificationBundle.putString(Constants.NavKeys.MATTER_ID, params.getString("matter_id"))
                    }
                    if (params.has("appointment_id")) {
                        Constants.notificationBundle.putString(Constants.NavKeys.APPOINTMENT_ID, params.getString("appointment_id"))
                    }
                    if (params.has("client_id")) {
                        Constants.notificationBundle.putString(Constants.NavKeys.CLIENT_ID, params.getString("client_id"))
                    }
                    if (params.has("guid")) {
                        Constants.notificationBundle.putString(Constants.NavKeys.GUID, params.getString("guid"))
                    }
                    if (params.has("group_ids")) {
                        val ids = params.getJSONArray("group_ids")
                        val idList = ArrayList<String>()
                        for (i in 0 until ids.length()) {
                            idList.add(ids.getString(i))
                        }
                        Constants.notificationBundle.putStringArrayList(Constants.NavKeys.GROUPS_ID, idList)
                    }
                    if (params.has("highlight_ids")) {
                        val ids = params.getJSONArray("highlight_ids")
                        val idList = ArrayList<String>()
                        for (i in 0 until ids.length()) {
                            idList.add(ids.getString(i))
                        }
                        Constants.notificationBundle.putStringArrayList(Constants.NavKeys.HIGHLIGHT_IDS, idList)
                    }
                    if (params.has("relationship_id")) {
                        Constants.notificationBundle.putString(Constants.NavKeys.RELATIONSHIP_ID, params.getString("relationship_id"))
                    }
                }
                when (route) {
                    "appointment_list" -> {
                        Constants.isCreate = false
                        Constants.mainActivity?.navigation_items(Appointments())
                    }
                    "meeting_detail", "meeting_list" -> {
                        Constants.isCreate = false
                        Constants.mainActivity?.navigation_items(Meetings())
                    }
                    "document_matter_list" -> {
                        Constants.isCreate = false
                        val frag = DocumentsEn()
                        val b = Bundle()
                        b.putString("document_type", "matter")
                        frag.arguments = b
                        Constants.mainActivity?.navigation_items(frag)
                    }
                    "document_client_list" -> {
                        Constants.isCreate = false
                        val frag = DocumentsEn()
                        val b = Bundle()
                        b.putString("document_type", "client")
                        frag.arguments = b
                        Constants.mainActivity?.navigation_items(frag)
                    }
                    "document_firm_list" -> {
                        Constants.isCreate = false
                        val frag = DocumentsEn()
                        val b = Bundle()
                        b.putString("document_type", "firm")
                        frag.arguments = b
                        Constants.mainActivity?.navigation_items(frag)
                    }
                    "document_deleted_list" -> {
                        Constants.isCreate = false
                        val frag = DocumentsEn()
                        val b = Bundle()
                        b.putString("document_type", "delete")
                        frag.arguments = b
                        Constants.mainActivity?.navigation_items(frag)
                    }
                    "relationship_individual_list", "relationship_shared_with_me_individual_list" -> {
                        Constants.Rel_Type = "Individual"
                        Constants.mainActivity?.navigation_items(ClientRelationship())
                    }
                    "relationship_business_list", "relationship_shared_with_me_business_list" -> {
                        Constants.Rel_Type = "Entity"
                        Constants.mainActivity?.navigation_items(ClientRelationship())
                    }
                    "relationship_corporate_list", "relationship_shared_with_me_corporate_list" -> {
                        Constants.Rel_Type = "Corporate"
                        Constants.mainActivity?.navigation_items(ClientRelationship())
                    }
                    "relationship_deleted_list" -> {
                        Constants.Rel_Type = "Deleted"
                        Constants.mainActivity?.navigation_items(ClientRelationship())
                    }
                    "message_client_inbox" -> {
                        Constants.isClient_chat = true
                        Constants.mainActivity?.navigation_items(Chat())
                    }
                    "message_team_inbox" -> {
                        Constants.isClient_chat = false
                        Constants.mainActivity?.navigation_items(Chat())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun setupModuleView(
            moduleView: View?,
            text: String?,
            icon: Drawable?,
            isTitleShow: Boolean,
            listener: OnModuleClickListener?
        ) {
            if (moduleView == null) return
            val tvName = moduleView.findViewById<TextView>(R.id.tv_module_name)
            val ivIcon = moduleView.findViewById<ImageView>(R.id.iv_Icon)
            val title_name = moduleView.findViewById<TextView>(R.id.title_name)
            tvName.text = text
            ivIcon.setImageDrawable(icon)
            ivIcon.setColorFilter(Color.WHITE)
            if (isTitleShow) {
                title_name.visibility = VISIBLE
            } else {
                title_name.visibility = GONE
            }
            moduleView.setOnClickListener { v ->
                listener?.onClick(v)
            }
        }

        @JvmStatic
        fun formatTimestamp(isoTimestamp: String): String {
            return try {
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                isoFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = isoFormat.parse(isoTimestamp)
                val outputFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US)
                outputFormat.timeZone = TimeZone.getDefault()
                if (date != null) outputFormat.format(date) else isoTimestamp
            } catch (e: Exception) {
                e.printStackTrace()
                isoTimestamp
            }
        }

        @JvmStatic
        fun getFullRoleName(roleCode: String?): String {
            return when (roleCode) {
                "SU" -> "Super User"
                "GH" -> "Group Head"
                "TM" -> "Team Member"
                "AAM" -> "Admin"
                else -> "Unknown"
            }
        }

        @JvmStatic
        fun getInitials(value: String?): String {
            if (value == null || value.trim { it <= ' ' }.isEmpty()) return ""
            val trimmed = value.trim { it <= ' ' }
            return if (trimmed.length == 1) trimmed.uppercase(Locale.getDefault()) else trimmed.substring(0, 1).uppercase(Locale.getDefault())
        }

        @JvmStatic
        fun showValidationALert(title: String?, message: String?, context: Context?) {
            val dlgAlert = AlertDialog.Builder(context)
            dlgAlert.setMessage(message)
            dlgAlert.setTitle(title)
            dlgAlert.setPositiveButton("Ok", null)
            dlgAlert.setCancelable(true)
            dlgAlert.show()
            dlgAlert.setPositiveButton("Ok") { dialog, which -> }
        }

        @JvmStatic
        fun maskEmail(email: String?): String {
            if (email == null || !email.contains("@")) {
                return ""
            }
            val parts = email.split("@".toRegex(), 2).toTypedArray()
            val username = parts[0]
            val domain = parts[1]
            if (username.length <= 2) {
                return username.substring(0, 1) + "*@" + domain
            }
            val masked = StringBuilder()
            masked.append(username.substring(0, 2))
            for (i in 2 until username.length) {
                masked.append("*")
            }
            masked.append("@").append(domain)
            return masked.toString()
        }

        @JvmStatic
        fun isMaskedEmail(email: String?): Boolean {
            if (email == null || !email.contains("@")) return false
            val username = email.split("@".toRegex()).toTypedArray()[0]
            return username.contains("*")
        }

        @JvmStatic
        fun maskPhoneNumber(phone: String?): String {
            if (phone == null || phone.length < 3) {
                return ""
            }
            val visibleDigits = 3
            val maskLength = phone.length - visibleDigits
            val masked = StringBuilder()
            for (i in 0 until maskLength) {
                masked.append("*")
            }
            masked.append(phone.substring(maskLength))
            return masked.toString()
        }

        @JvmStatic
        fun LoadingRecyclerview(recyclerView: RecyclerView?, context: Context?) {
            if (recyclerView == null || context == null) return
            if (DynamicUtils.isTablet(context)) {
                recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            } else {
                recyclerView.layoutManager = LinearLayoutManager(context)
            }
            recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_fall_down)
            recyclerView.scheduleLayoutAnimation()
        }

        @JvmStatic
        fun LoadAnimation(recyclerView: RecyclerView?, context: Context?) {
            if (recyclerView == null || context == null) return
            recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_fall_down)
            recyclerView.scheduleLayoutAnimation()
        }

        @JvmStatic
        fun RefreshRecyclerView(recyclerView: RecyclerView?) {
            if (recyclerView != null) {
                recyclerView.post {
                    val lm = recyclerView.layoutManager
                    if (lm is StaggeredGridLayoutManager) {
                        lm.invalidateSpanAssignments()
                    }
                    recyclerView.requestLayout()
                }
            }
        }

        @JvmStatic
        fun showDialog(msg: String, context: Context, permission: String?) {
            val alertBuilder = AlertDialog.Builder(context)
            alertBuilder.setCancelable(true)
            alertBuilder.setTitle("Permission necessary")
            alertBuilder.setMessage("$msg permission is necessary")
            alertBuilder.setPositiveButton(android.R.string.yes) { dialog, which ->
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(permission),
                    123
                )
            }
            val alert = alertBuilder.create()
            alert.show()
        }

        @JvmStatic
        fun showConfirmationDialog(
            context: Context,
            titleText: String?,
            confirmText: String?,
            yesText: String,
            noText: String,
            listener: OnConfirmListener
        ) {
            showConfirmationDialog(context, titleText, confirmText, yesText, noText, listener, false)
        }

        @JvmStatic
        fun showConfirmationDialog(
            context: Context,
            titleText: String?,
            confirmText: String?,
            yesText: String,
            noText: String,
            listener: OnConfirmListener,
            isCancelActionNeeded: Boolean?
        ) {
            try {
                val dialogBuilder = AlertDialog.Builder(context)
                val view = LayoutInflater.from(context).inflate(R.layout.confirmation_popup, null)
                val header_name = view.findViewById<TextView>(R.id.header_name)
                val close_documents = view.findViewById<ImageView>(R.id.close_documents)
                val tv_confirmation = view.findViewById<TextView>(R.id.tv_confirmation)
                val btn_yes = view.findViewById<AppCompatButton>(R.id.btn_yes)
                val btn_no = view.findViewById<AppCompatButton>(R.id.btn_No)
                if (yesText.isEmpty()) {
                    btn_yes.setText(R.string.yes)
                } else {
                    btn_yes.text = yesText
                }
                if (noText.isEmpty()) {
                    btn_no.setText(R.string.no)
                } else {
                    btn_no.text = noText
                }
                header_name.setTextColor(context.getColor(R.color.blue))
                header_name.text = titleText
                tv_confirmation.text = confirmText
                val dialog = dialogBuilder.create()
                dialog.setView(view)
                close_documents.setOnClickListener { v ->
                    if (isCancelActionNeeded == true) {
                        listener.onCancel()
                    }
                    dialog.dismiss()
                }
                btn_no.setOnClickListener { v ->
                    dialog.dismiss()
                    listener.onCancel()
                }
                btn_yes.setOnClickListener { v ->
                    dialog.dismiss()
                    listener.onSave()
                }
                dialog.setCanceledOnTouchOutside(false)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun Delete_Popup(
            activity: Activity,
            delete_msg: String?,
            popup_name: String,
            id: String?,
            weeklyCalendar: WeeklyCalendar?,
            monthlyCalendar: CalendarMonthView?,
            meeting: View,
            isrecur: Boolean,
            isevent_delete_scope: Boolean
        ) {
            try {
                meeting.alpha = 0.5f
                val dialogBuilder = AlertDialog.Builder(activity)
                val inflater = activity.layoutInflater
                val dialogLayout = inflater.inflate(R.layout.alert_dialog_delete, null)
                val edit_event_dialog = dialogLayout.findViewById<TextView>(R.id.edit_event_dialog)
                val delete_event_msg = dialogLayout.findViewById<TextView>(R.id.delete_event_msg)
                delete_event_msg.text = delete_msg
                delete_event_msg.textSize = DynamicUtils.twenty.toFloat()
                delete_event_msg.maxLines = 10
                delete_event_msg.setTextColor(Color.BLACK)
                delete_event_msg.setTypeface(null, Typeface.NORMAL)
                val delete = dialogLayout.findViewById<Button>(R.id.delete_event)
                val btn_close_event = dialogLayout.findViewById<Button>(R.id.btn_close_event)
                btn_close_event.setTextColor(Color.RED)
                btn_close_event.setText(R.string.cancel)
                val dialog = dialogBuilder.create()
                btn_close_event.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View) {
                        dialog.dismiss()
                        meeting.alpha = 1.0f
                    }
                })
                delete.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(view: View) {
                        dialog.dismiss()
                        if (popup_name == "Delete_Event") {
                            val recurring_edit_choice = "this"
                            if (id != null) {
                                Log.d("event_id", id + "..." + recurring_edit_choice)
                                if (weeklyCalendar != null) {
                                    if (isrecur) {
                                        weeklyCalendar.delete_recurring_event(id, isevent_delete_scope)
                                    } else {
                                        weeklyCalendar.callDeleteEventwebservice(id, recurring_edit_choice, isevent_delete_scope)
                                    }
                                    meeting.alpha = 1.0f
                                }
                                if (monthlyCalendar != null) {
                                    if (isrecur) {
                                        monthlyCalendar.delete_recurring_event(id, isevent_delete_scope)
                                    } else {
                                        monthlyCalendar.callDeleteEventwebservice(id, recurring_edit_choice, isevent_delete_scope)
                                    }
                                    meeting.alpha = 1.0f
                                }
                            }
                        }
                    }
                })
                dialog.setView(dialogLayout)
                dialog.setCanceledOnTouchOutside(false)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        }

        @JvmStatic
        fun formatDateToReadable(inputDate: String?): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(inputDate)
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                outputFormat.timeZone = TimeZone.getDefault()
                if (date != null) outputFormat.format(date) else "Invalid Date"
            } catch (e: Exception) {
                e.printStackTrace()
                "Invalid Date"
            }
        }

        @JvmStatic
        private fun safeTrim(value: String?, maxLength: Int): String {
            return if (value == null) "" else value.substring(0, Math.min(value.length, maxLength))
        }

        @JvmStatic
        fun hasDocumentChanged(
            original: DocumentsModel,
            currentModel: DocumentsModel
        ): Boolean {
            val originalName = safeTrim(original.name, 50)
            val newName = safeTrim(currentModel.name, 50)
            val originalDesc = safeTrim(original.description, 300)
            val newDesc = safeTrim(currentModel.description, 300)
            val newExp = currentModel.expiration_date
            val newDownload = currentModel.isIsenabled
            val newEncrypt = currentModel.isencrypted
            val originalTags = if (original.tags != null) original.tags.toString() else ""
            val newTags = if (currentModel.tags != null) currentModel.tags.toString() else ""
            return originalName != newName ||
                    originalDesc != newDesc ||
                    original.expiration_date != newExp ||
                    original.isIsenabled != newDownload ||
                    original.isencrypted != newEncrypt ||
                    originalTags != newTags
        }

        @JvmStatic
        fun Delete_Popup(activity: Activity, PreviousDialog: Dialog?) {
            try {
                val dialogBuilder = AlertDialog.Builder(activity)
                val inflater = activity.layoutInflater
                val dialogLayout = inflater.inflate(R.layout.alert_dialog_delete, null)
                val edit_event_dialog = dialogLayout.findViewById<TextView>(R.id.edit_event_dialog)
                edit_event_dialog.setText(R.string.alert_)
                val delete_event_msg = dialogLayout.findViewById<TextView>(R.id.delete_event_msg)
                delete_event_msg.setText(R.string.are_you_sure_changes_are_not_saved)
                delete_event_msg.textSize = DynamicUtils.twenty.toFloat()
                delete_event_msg.setTextColor(Color.BLACK)
                delete_event_msg.setTypeface(null, Typeface.NORMAL)
                val delete = dialogLayout.findViewById<Button>(R.id.delete_event)
                val btn_close_event = dialogLayout.findViewById<Button>(R.id.btn_close_event)
                btn_close_event.setTextColor(Color.RED)
                btn_close_event.setText(R.string.cancel)
                val dialog = dialogBuilder.create()
                btn_close_event.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View) {
                        dialog.dismiss()
                    }
                })
                delete.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(view: View) {
                        dialog.dismiss()
                        PreviousDialog?.dismiss()
                    }
                })
                dialog.setView(dialogLayout)
                dialog.setCanceledOnTouchOutside(false)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        }

        @JvmStatic
        fun showConfirmationDialog(
            context: Context,
            titleText: String?,
            confirmText: String?,
            listener: OnConfirmListener
        ) {
            try {
                val dialogBuilder = AlertDialog.Builder(context)
                val view = LayoutInflater.from(context).inflate(R.layout.delete_relationship, null)
                val header_name = view.findViewById<TextView>(R.id.header_name)
                val close_documents = view.findViewById<ImageView>(R.id.close_documents)
                val tv_confirmation = view.findViewById<TextView>(R.id.tv_confirmation)
                val btn_yes = view.findViewById<AppCompatButton>(R.id.btn_yes)
                val btn_no = view.findViewById<AppCompatButton>(R.id.btn_No)
                header_name.setTextColor(context.getColor(R.color.blue))
                header_name.text = titleText
                tv_confirmation.text = confirmText
                val dialog = dialogBuilder.create()
                dialog.setView(view)
                close_documents.setOnClickListener { v ->
                    dialog.dismiss()
                    listener.onCancel()
                }
                btn_no.setOnClickListener { v ->
                    dialog.dismiss()
                    listener.onCancel()
                }
                btn_yes.setOnClickListener { v ->
                    dialog.dismiss()
                    listener.onSave()
                }
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun showConfirmationDialog(
            context: Context,
            titleText: String?,
            confirmText: String,
            confirmName: String?,
            listener: OnConfirmListener
        ) {
            try {
                val dialogBuilder = AlertDialog.Builder(context)
                val view = LayoutInflater.from(context).inflate(R.layout.delete_relationship, null)
                val header_name = view.findViewById<TextView>(R.id.header_name)
                val close_documents = view.findViewById<ImageView>(R.id.close_documents)
                val tv_confirmation = view.findViewById<TextView>(R.id.tv_confirmation)
                val btn_yes = view.findViewById<AppCompatButton>(R.id.btn_yes)
                val btn_no = view.findViewById<AppCompatButton>(R.id.btn_No)
                val spannable = SpannableString(confirmText)
                spannable.setSpan(
                    ForegroundColorSpan(context.getColor(R.color.black)),
                    0,
                    confirmText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                if (confirmName != null && !confirmName.isEmpty()) {
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
                header_name.setTextColor(context.getColor(R.color.blue))
                header_name.text = titleText
                val dialog = dialogBuilder.create()
                dialog.setView(view)
                close_documents.setOnClickListener { v ->
                    dialog.dismiss()
                    listener.onCancel()
                }
                btn_no.setOnClickListener { v ->
                    dialog.dismiss()
                    listener.onCancel()
                }
                btn_yes.setOnClickListener { v ->
                    dialog.dismiss()
                    listener.onSave()
                }
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun showToast(message: String?, context: Context?) {
            if (context == null || message.isNullOrBlank()) return
            val appContext = context.applicationContext ?: context
            Log.d("TOAST_DEBUG", "showToast called: msg='$message', context=$appContext, mainThread=${Looper.myLooper() == Looper.getMainLooper()}")
            Handler(Looper.getMainLooper()).post {
                try {
                    Toast.makeText(appContext, message, Toast.LENGTH_LONG).show()
                    Log.d("TOAST_DEBUG", "Toast.show() executed successfully on main looper")
                } catch (e: Throwable) {
                    Log.e("TOAST_DEBUG", "Error displaying toast: ${e.message}", e)
                }
            }
        }

        @JvmStatic
        fun storeSharedPreferenceString(key: String?, value: String?, activity: Activity) {
            val sharedPref = activity.getSharedPreferences("mypref", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString(key, value)
            editor.commit()
        }

        @JvmStatic
        fun getSharedPreferenceStringData(key: String?, activity: Context?): String? {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
            return sharedPref.getString(key, null)
        }

        @JvmStatic
        fun isNull(value: String?): String {
            return value ?: ""
        }

        @JvmStatic
        fun getRealPathFromURIPath(contentURI: Uri, activity: Activity): String {
            val cursor = activity.contentResolver.query(contentURI, null, null, null, null)
            var realPath = ""
            if (cursor == null) {
                realPath = contentURI.path ?: ""
            } else {
                cursor.moveToFirst()
                val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                if (idx != -1) {
                    realPath = cursor.getString(idx)
                }
            }
            cursor?.close()
            return realPath
        }

        @JvmStatic
        fun isSDCardPresent(): Boolean {
            return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        }

        @JvmStatic
        fun showError(message: String?, activity: Activity): Dialog {
            val dialogBuilder = AlertDialog.Builder(activity)
            val inflater = activity.layoutInflater
            val dialogLayout = inflater.inflate(R.layout.error_layout_de, null)
            val tv_error_msg = dialogLayout.findViewById<TextView>(R.id.tv_error_msg)
            tv_error_msg.text = message
            val iv_error_msg = dialogLayout.findViewById<ImageView>(R.id.iv_error_msg)
            val dialog = dialogBuilder.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val window = dialog.window
            if (window != null) {
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val marginInDp = 20
                val scale = activity.resources.displayMetrics.density
                val marginInPx = (marginInDp * scale + 0.5f).toInt()
                val screenWidth = activity.resources.displayMetrics.widthPixels
                val dialogWidth = screenWidth - 2 * marginInPx
                window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT)
                val layoutParams = window.attributes
                layoutParams.gravity = Gravity.TOP
                layoutParams.y = 200
                window.attributes = layoutParams
            }
            iv_error_msg.setOnClickListener { dialog.dismiss() }
            dialog.setView(dialogLayout)
            dialog.show()
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            Handler(Looper.getMainLooper()).postDelayed({ dialog.dismiss() }, 3000)
            return dialog
        }

        @JvmStatic
        fun showSuccess(message: String?, activity: Activity): Dialog {
            val dialogBuilder = AlertDialog.Builder(activity)
            val inflater = activity.layoutInflater
            val dialogLayout = inflater.inflate(R.layout.success_layout_de, null)
            val tv_error_msg = dialogLayout.findViewById<TextView>(R.id.tv_error_msg)
            tv_error_msg.text = message
            val iv_error_msg = dialogLayout.findViewById<ImageView>(R.id.iv_error_msg)
            val dialog = dialogBuilder.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val window = dialog.window
            if (window != null) {
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val marginInDp = 20
                val scale = activity.resources.displayMetrics.density
                val marginInPx = (marginInDp * scale + 0.5f).toInt()
                val screenWidth = activity.resources.displayMetrics.widthPixels
                val dialogWidth = screenWidth - 2 * marginInPx
                window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT)
                val layoutParams = window.attributes
                layoutParams.gravity = Gravity.TOP
                layoutParams.y = 200
                window.attributes = layoutParams
            }
            iv_error_msg.setOnClickListener { dialog.dismiss() }
            dialog.setView(dialogLayout)
            dialog.show()
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            Handler(Looper.getMainLooper()).postDelayed({ dialog.dismiss() }, 3000)
            return dialog
        }

        @JvmStatic
        fun showConfirmDialog(
            activity: Activity,
            titleText: String?,
            confirmText: String?,
            btnCancelText: String?,
            btnSaveText: String?,
            listener: OnConfirmListener
        ) {
            val dialogBuilder = AlertDialog.Builder(activity)
            val inflater = activity.layoutInflater
            val dialog = inflater.inflate(R.layout.confirm_layout_popup, null)
            val tvTitle = dialog.findViewById<TextView>(R.id.tv_confirmation)
            val tvMessage = dialog.findViewById<TextView>(R.id.tv_confirmContent)
            val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
            val btnSave = dialog.findViewById<Button>(R.id.btnSave)
            val cancelIcon = dialog.findViewById<ImageView>(R.id.cancelIcon)
            tvTitle.text = titleText
            tvMessage.text = confirmText
            btnCancel.text = btnCancelText
            btnSave.text = btnSaveText
            dialogBuilder.setView(dialog)
            dialogBuilder.setCancelable(false)
            val dialoglayout = dialogBuilder.create()
            dialoglayout.show()
            val window = dialoglayout.window
            if (window != null) {
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val marginInDp = 20
                val scale = activity.resources.displayMetrics.density
                val marginInPx = (marginInDp * scale + 0.5f).toInt()
                val screenWidth = activity.resources.displayMetrics.widthPixels
                val dialogWidth = screenWidth - 2 * marginInPx
                window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT)
                val layoutParams = window.attributes
                layoutParams.gravity = Gravity.CENTER
                window.attributes = layoutParams
            }
            btnCancel.setOnClickListener { v ->
                dialoglayout.dismiss()
                listener.onCancel()
            }
            cancelIcon.setOnClickListener { v ->
                dialoglayout.dismiss()
                listener.onCancel()
            }
            btnSave.setOnClickListener { v ->
                dialoglayout.dismiss()
                listener.onSave()
            }
            dialoglayout.show()
        }

        @JvmStatic
        fun showConfirmation(
            activity: Activity,
            titleText: String?,
            confirmText: String?,
            btnSaveText: String?,
            listener: OnConfirmListener?
        ): Dialog {
            val dialogBuilder = AlertDialog.Builder(activity)
            val inflater = activity.layoutInflater
            val dialogLayout = inflater.inflate(R.layout.confirm_layout, null)
            val tvConfirmation = dialogLayout.findViewById<TextView>(R.id.tv_confirmation)
            val tvConfirmContent = dialogLayout.findViewById<TextView>(R.id.tv_confirmContent)
            val btnCancel = dialogLayout.findViewById<Button>(R.id.btnCancel)
            val btnSave = dialogLayout.findViewById<Button>(R.id.btnSave)
            btnSave.text = btnSaveText
            tvConfirmation.text = titleText
            tvConfirmContent.text = confirmText
            dialogBuilder.setView(dialogLayout)
            dialogBuilder.setCancelable(false)
            val dialog = dialogBuilder.create()
            dialog.show()
            val window = dialog.window
            if (window != null) {
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val marginInDp = 20
                val scale = activity.resources.displayMetrics.density
                val marginInPx = (marginInDp * scale + 0.5f).toInt()
                val screenWidth = activity.resources.displayMetrics.widthPixels
                val dialogWidth = screenWidth - 2 * marginInPx
                window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT)
                val layoutParams = window.attributes
                layoutParams.gravity = Gravity.CENTER
                window.attributes = layoutParams
            }
            btnSave.setOnClickListener { v ->
                dialog.dismiss()
                listener?.onSave()
            }
            btnCancel.setOnClickListener { v ->
                dialog.dismiss()
                listener?.onCancel()
            }
            return dialog
        }

        @JvmStatic
        fun showEmailAlert(activity: Activity, email: Email?): Dialog {
            val dialogBuilder = AlertDialog.Builder(activity)
            val inflater = activity.layoutInflater
            val dialogLayout = inflater.inflate(R.layout.email_alert_popup, null)
            val tv_confirmContent = dialogLayout.findViewById<TextView>(R.id.tv_confirmContent)
            val btnCancel = dialogLayout.findViewById<TextView>(R.id.btnCancel)
            btnCancel.setText(R.string.cancel)
            val btnSave = dialogLayout.findViewById<TextView>(R.id.btnSave)
            btnSave.setText(R.string.ok)
            tv_confirmContent.setTextColor(activity.getColor(R.color.grey_medium))
            btnCancel.setTextColor(activity.getColor(R.color.light_blue))
            btnSave.setTextColor(activity.getColor(R.color.light_blue))
            tv_confirmContent.setText(R.string.email_no_subject_alert)
            val dialog = dialogBuilder.create()
            dialog.setView(dialogLayout)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            val window = dialog.window
            if (window != null) {
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val marginInDp = 20
                val scale = activity.resources.displayMetrics.density
                val marginInPx = (marginInDp * scale + 0.5f).toInt()
                val screenWidth = activity.resources.displayMetrics.widthPixels
                val dialogWidth = screenWidth - 2 * marginInPx
                window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT)
                val layoutParams = window.attributes
                layoutParams.gravity = Gravity.CENTER
                window.attributes = layoutParams
            }
            btnSave.setOnClickListener { v ->
                dialog.dismiss()
                email?.send_email()
            }
            btnCancel.setOnClickListener { v -> dialog.dismiss() }
            return dialog
        }

        @JvmStatic
        fun showSaveAsConfirmation(activity: Activity, docEditor: DocEditor, docName: String?): Dialog {
            val dialogBuilder = AlertDialog.Builder(activity)
            val inflater = activity.layoutInflater
            val dialogLayout = inflater.inflate(R.layout.save_as_confirmation_popup, null)
            val tv_saved = dialogLayout.findViewById<TextView>(R.id.tv_saved)
            val tv_confirmContent = dialogLayout.findViewById<TextView>(R.id.tv_confirmContent)
            val iv_cancel = dialogLayout.findViewById<ImageView>(R.id.iv_cancel)
            val btn_goBack = dialogLayout.findViewById<Button>(R.id.btn_goBack)
            val btn_viewDocument = dialogLayout.findViewById<Button>(R.id.btn_viewDocument)
            tv_saved.setText(R.string.saved)
            tv_confirmContent.gravity = Gravity.CENTER
            tv_confirmContent.text = "Congratulations! You have Successfully created a $docName Document"
            btn_goBack.setText(R.string.go_back)
            btn_viewDocument.setText(R.string.view_document)
            btn_viewDocument.setPadding(20, 10, 20, 10)
            val dialog = dialogBuilder.create()
            dialog.setView(dialogLayout)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            val window = dialog.window
            if (window != null) {
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val marginInDp = 20
                val scale = activity.resources.displayMetrics.density
                val marginInPx = (marginInDp * scale + 0.5f).toInt()
                val screenWidth = activity.resources.displayMetrics.widthPixels
                val dialogWidth = screenWidth - 2 * marginInPx
                window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT)
                val layoutParams = window.attributes
                layoutParams.gravity = Gravity.TOP
                layoutParams.y = 200
                window.attributes = layoutParams
            }
            btn_viewDocument.setOnClickListener { v ->
                dialog.dismiss()
                docEditor.loadView()
            }
            iv_cancel.setOnClickListener { v -> dialog.dismiss() }
            btn_goBack.setOnClickListener { v -> dialog.dismiss() }
            return dialog
        }

        @JvmStatic
        fun showAlert(message: String?, activity: Activity?, title: String?): Dialog? {
            return showAlert(message, activity, title ?: "", null)
        }

        @JvmStatic
        fun showAlert(message: String?, activity: Activity?, title: String, onOkClick: Runnable?): Dialog? {
            if (activity == null) return null
            val dialogBuilder = AlertDialog.Builder(activity)
            val inflater = activity.layoutInflater
            val dialogLayout = inflater.inflate(R.layout.alert_dialog, null)
            val tv_ok = dialogLayout.findViewById<TextView>(R.id.tv_ok)
            tv_ok.textSize = 17f
            tv_ok.setPadding(20, 20, 20, 20)
            tv_ok.setText(R.string.ok)
            tv_ok.setTextColor(activity.getColor(R.color.light_blue))
            val alert_content = dialogLayout.findViewById<TextView>(R.id.alert_content)
            alert_content.text = message
            alert_content.gravity = Gravity.CENTER
            val dialog = dialogBuilder.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val alert_title = dialogLayout.findViewById<TextView>(R.id.alert_title)
            if (title.isEmpty()) {
                alert_title.visibility = View.GONE
            } else {
                alert_title.visibility = VISIBLE
            }
            alert_title.text = title
            tv_ok.setOnClickListener { v ->
                dialog.dismiss()
                onOkClick?.run()
            }
            dialog.setView(dialogLayout)
            dialog.show()
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            displayDialog = dialog
            return dialog
        }

        @JvmStatic
        fun extractCleanErrorMessage(responseContent: String?): String {
            if (responseContent.isNullOrBlank()) {
                return "An error occurred. Please try again."
            }
            val trimmed = responseContent.trim()
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                try {
                    val json = JSONObject(trimmed)
                    if (json.has("msg") && !json.isNull("msg")) {
                        val msgStr = json.optString("msg", "").trim()
                        if (msgStr.isNotEmpty() && !msgStr.startsWith("{")) {
                            return msgStr
                        }
                    }
                    if (json.has("message") && !json.isNull("message")) {
                        val msgStr = json.optString("message", "").trim()
                        if (msgStr.isNotEmpty() && !msgStr.startsWith("{")) {
                            return msgStr
                        }
                    }
                    if (json.has("detail") && !json.isNull("detail")) {
                        val detailStr = json.optString("detail", "").trim()
                        if (detailStr.isNotEmpty() && !detailStr.startsWith("{")) {
                            return detailStr
                        }
                    }
                    if (json.has("error_description") && !json.isNull("error_description")) {
                        val descStr = json.optString("error_description", "").trim()
                        if (descStr.isNotEmpty() && !descStr.startsWith("{")) {
                            return descStr
                        }
                    }
                    if (json.has("error") && !json.isNull("error")) {
                        val errVal = json.get("error")
                        if (errVal is String && errVal.isNotEmpty() && !errVal.startsWith("{")) {
                            return errVal
                        }
                    }
                    if (json.has("errors") && !json.isNull("errors")) {
                        val errorsObj = json.optJSONObject("errors")
                        if (errorsObj != null) {
                            val keys = errorsObj.keys()
                            val errorList = mutableListOf<String>()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                val fieldErr = errorsObj.opt(key)
                                if (fieldErr is JSONArray && fieldErr.length() > 0) {
                                    errorList.add(fieldErr.optString(0))
                                } else if (fieldErr is String && fieldErr.isNotEmpty()) {
                                    errorList.add(fieldErr)
                                }
                            }
                            if (errorList.isNotEmpty()) {
                                return errorList.joinToString(", ")
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (trimmed.startsWith("<") || trimmed.contains("<!DOCTYPE", ignoreCase = true) || trimmed.contains("<html", ignoreCase = true)) {
                return "Server error. Please try again later."
            } else if (trimmed.contains("502 Bad Gateway", ignoreCase = true)) {
                return "Server is currently unavailable (502 Bad Gateway). Please try again later."
            }
            // Strip raw braces/quotes if any remain
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                return "An error occurred. Please try again."
            }
            return trimmed
        }

        @JvmStatic
        fun showAlert(message: String?, activity: Activity?): Dialog? {
            if (activity == null) return null
            val dialogBuilder = AlertDialog.Builder(activity)
            val inflater = activity.layoutInflater
            val dialogLayout = inflater.inflate(R.layout.alert_dialog, null)
            val tv_ok = dialogLayout.findViewById<TextView>(R.id.tv_ok)
            tv_ok.textSize = 17f
            tv_ok.setPadding(20, 20, 20, 20)
            tv_ok.setText(R.string.ok)
            tv_ok.setTextColor(activity.getColor(R.color.light_blue))
            val alert_content = dialogLayout.findViewById<TextView>(R.id.alert_content)
            alert_content.text = message
            alert_content.textSize = 17f
            alert_content.gravity = Gravity.CENTER
            val dialog = dialogBuilder.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            tv_ok.setOnClickListener { v -> dialog.dismiss() }
            dialog.setView(dialogLayout)
            dialog.show()
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            return dialog
        }

        @JvmStatic
        fun showAlert_docs(title: String?, message: String?, activity: Activity?): Dialog? {
            if (activity == null) return null
            val dialogBuilder = AlertDialog.Builder(activity)
            val inflater = activity.layoutInflater
            val dialogLayout = inflater.inflate(R.layout.alert_dialog, null)
            val alertTitle = dialogLayout.findViewById<TextView>(R.id.alert_title)
            alertTitle.text = title
            val alert_content = dialogLayout.findViewById<TextView>(R.id.alert_content)
            alert_content.text = message
            alert_content.textSize = 17f
            alert_content.gravity = Gravity.CENTER
            val tv_ok = dialogLayout.findViewById<TextView>(R.id.tv_ok)
            tv_ok.textSize = 17f
            tv_ok.setPadding(20, 20, 20, 20)
            tv_ok.setText(R.string.ok)
            tv_ok.setTextColor(activity.getColor(R.color.light_blue))
            val dialog = dialogBuilder.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            tv_ok.setOnClickListener { v -> dialog.dismiss() }
            dialog.setView(dialogLayout)
            dialog.show()
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            return dialog
        }

        @JvmStatic
        fun checkSwitchState(switchDownload: SwitchMaterial?) {
            if (switchDownload == null) return
            if (switchDownload.isChecked) {
                switchDownload.trackTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(switchDownload.context, R.color.dullBlueColor)
                )
            } else {
                switchDownload.trackTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(switchDownload.context, R.color.grey_color_dark)
                )
            }
        }

        @JvmStatic
        fun renderSelectedTags(
            context: Context,
            container: LinearLayout,
            tags: ArrayList<DocumentsModel>?,
            editingDoc: DocumentsModel,
            cl_document: ConstraintLayout?,
            activity: Activity
        ) {
            container.removeAllViews()

            if (tags == null || tags.isEmpty()) {
                container.visibility = View.GONE
                return
            }

            container.visibility = VISIBLE
            val inflater = LayoutInflater.from(context)

            for (i in tags.indices) {
                val tag = tags[i]
                val position = i

                val tagView = inflater.inflate(
                    R.layout.component_tag_item,
                    container,
                    false
                )

                val tv = tagView.findViewById<TextView>(R.id.tv_tag_name)
                val ivDelete = tagView.findViewById<ImageView>(R.id.iv_delete)
                val ivEdit = tagView.findViewById<ImageView>(R.id.iv_edit)

                tv.text = "${tag.tag_type} : ${tag.tag_name}"

                ivDelete.setOnClickListener { v ->
                    val removedTag = tags.removeAt(position)
                    val jsonTags = editingDoc.tags
                    if (jsonTags != null) {
                        jsonTags.remove(removedTag.tag_type)
                        editingDoc.tags = jsonTags
                    }
                    renderSelectedTags(context, container, tags, editingDoc, cl_document, activity)
                }

                ivEdit.setOnClickListener { v ->
                    open_edit_tag_popup(context, position, tags, editingDoc, cl_document, container, activity, true)
                }

                container.addView(tagView)
            }
        }

        @JvmStatic
        @SuppressLint("WrongViewCast")
        fun open_edit_tag_popup(
            context: Context,
            position: Int,
            tags: ArrayList<DocumentsModel>,
            editingDoc: DocumentsModel,
            cl_document: ConstraintLayout?,
            llSelectedTags: LinearLayout,
            activity: Activity,
            isUpdated: Boolean
        ) {
            val editTag = tags[position]
            val dialogBuilder = AlertDialog.Builder(context)
            if (cl_document != null) {
                cl_document.alpha = 0.5f
            }

            val view = LayoutInflater.from(context).inflate(R.layout.add_tag, null)
            val tv_tag_type = view.findViewById<TextInputEditText>(R.id.tv_tag_type)
            val tv_tag_name = view.findViewById<TextInputEditText>(R.id.tv_tag_name)
            val tag_type_name = view.findViewById<TextView>(R.id.tag_type_name)
            val tv_added_tags = view.findViewById<TextView>(R.id.tv_added_tags)
            tv_added_tags.visibility = GONE
            tag_type_name.setText(R.string.tag_type)
            val tag_name = view.findViewById<TextView>(R.id.tag_name)
            tag_name.setText(R.string.tag)
            tv_tag_type.setHint(R.string.tag_type)
            tv_tag_name.setHint(R.string.tag)
            tv_tag_name.addTextChangedListener(Validation(tv_tag_name))
            tv_tag_type.addTextChangedListener(Validation(tv_tag_type))
            val header_name = view.findViewById<TextView>(R.id.header_name)
            val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(30))
            val filters1 = arrayOf<InputFilter>(InputFilter.LengthFilter(100))

            tv_tag_type.filters = filters
            tv_tag_name.filters = filters1

            val btn_add = view.findViewById<AppCompatButton>(R.id.btn_add_tags)
            btn_add.visibility = GONE
            val btn_cancel = view.findViewById<AppCompatButton>(R.id.btn_cancel_tag)
            val iv_cancel = view.findViewById<ImageView>(R.id.close_edit_docs)

            tv_tag_type.setText(editTag.tag_type)
            tv_tag_name.setText(editTag.tag_name)

            val btn_save_tag = view.findViewById<AppCompatButton>(R.id.btn_save_tag)
            if (isUpdated) {
                header_name.setText(R.string.update_tag)
            } else {
                header_name.setText(R.string.add_tag)
            }

            if (isUpdated) {
                btn_save_tag.setText(R.string.update)
            } else {
                btn_save_tag.setText(R.string.add)
            }

            val dialog = dialogBuilder.create()

            btn_save_tag.setOnClickListener { v ->
                val type = tv_tag_type.text.toString().trim()
                val name = tv_tag_name.text.toString().trim()

                if (type.isEmpty() || name.isEmpty()) {
                    AndroidUtils.showAlert("Please check the Tag Type and Tag", activity)
                    return@setOnClickListener
                }

                val oldType = editTag.tag_type

                editTag.tag_type = type
                editTag.tag_name = name
                tags[position] = editTag

                var jsonTags = editingDoc.tags
                if (jsonTags == null) jsonTags = JSONObject()

                try {
                    jsonTags.remove(oldType)
                    jsonTags.put(type, name)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                editingDoc.tags = jsonTags
                dialog.dismiss()
                renderSelectedTags(context, llSelectedTags, tags, editingDoc, cl_document, activity)
            }

            btn_cancel.setOnClickListener { dialog.dismiss() }
            iv_cancel.setOnClickListener { dialog.dismiss() }

            dialog.setOnDismissListener {
                if (cl_document != null) {
                    cl_document.alpha = 1.0f
                }
            }

            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setView(view)
            dialog.show()
        }

        @JvmStatic
        fun extractDateTimeParts(fromDateTime: String?, toDateTime: String?): Array<String>? {
            if (fromDateTime == null || toDateTime == null) return null
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
                val dateOut = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                val timeOut = SimpleDateFormat("HH:mm", Locale.ENGLISH)

                val from = inputFormat.parse(fromDateTime)
                val to = inputFormat.parse(toDateTime)

                if (from == null || to == null) null else arrayOf(
                    dateOut.format(from),
                    timeOut.format(from),
                    timeOut.format(to)
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        @JvmStatic
        fun getAVChatUrl(roomId: String?, fromTime: String?, toTime: String?, date: String?, clientName: String?): String {
            val name = (Constants.NAME ?: "").replace(" ", "%20")
            return "${Constants.AVClientChatUrl}join-room?roomId=$roomId&name=$name&fromTime=$fromTime&toTime=$toTime&date=$date"
        }

        @JvmStatic
        private fun getString(loginType: String, token: String, jid: String): String {
            val name = ((Constants.NAME ?: "") + " ").replace(" ", "%20")
            return "${Constants.AVChatUrl}?logintype=$loginType&token=$token&jid=$jid&name=$name&hideclient=${Constants.isAdmin}&plan=lauditor&category=${Constants.CATEGORY}"
        }

        @JvmStatic
        fun parseAnyDate(dateString: String?): Date? {
            if (dateString == null) return null
            val possibleFormats = arrayOf(
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "dd-MM-yyyy HH:mm:ss",
                "dd/MM/yyyy HH:mm:ss",
                "EEE MMM dd HH:mm:ss z yyyy",
                "yyyy-MM-dd",
                "dd-MM-yyyy",
                "ddMMyyyy",
                "MM-dd-yyyy",
                "dd/MM/yyyy",
                "yyyy/MM/dd",
                "MM/dd/yyyy",
                "dd MMM yyyy",
                "MMM dd, yyyy"
            )

            for (format in possibleFormats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.getDefault())
                    sdf.isLenient = false
                    val date = sdf.parse(dateString)
                    if (date != null) {
                        return date
                    }
                } catch (ignored: Exception) {
                }
            }
            return null
        }

        @JvmStatic
        fun isWithinOneMinute(dateTime: String?): Boolean {
            val appointmentDate = parseAnyDate(dateTime) ?: return false
            val timeDiff = appointmentDate.time - System.currentTimeMillis()
            return timeDiff <= 60000
        }

        @JvmStatic
        fun isWithinOneHour(dateTime: String?): Boolean {
            val appointmentDate = parseAnyDate(dateTime) ?: return false
            val timeDiff = appointmentDate.time - System.currentTimeMillis()
            return timeDiff <= 3600000
        }

        @JvmStatic
        fun isWithinTwoHours(dateTime: String?): Boolean {
            val appointmentDate = parseAnyDate(dateTime) ?: return false
            val timeDiff = appointmentDate.time - System.currentTimeMillis()
            return timeDiff <= 7200000
        }

        @JvmStatic
        private fun isTablet(activity: Activity): Boolean {
            val density = activity.resources.displayMetrics.density
            val dpWidth = activity.resources.displayMetrics.widthPixels / density
            val screenLayout = activity.resources.configuration.screenLayout and android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK
            val isLargeScreen = screenLayout >= android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE
            return isLargeScreen || dpWidth >= 600
        }

        @JvmStatic
        fun loadAVChatView(context: Context, activity: Activity, url: String, delegate: VideoCallDelegate?) {
            if (isWebViewShowing) {
                Log.d(TAG, "WebView already showing, dismissing previous instance")
                if (currentDialog != null && currentDialog!!.isShowing) {
                    currentDialog!!.dismiss()
                }
                if (currentWebView != null) {
                    currentWebView!!.loadUrl("about:blank")
                    currentWebView!!.stopLoading()
                    currentWebView!!.clearHistory()
                    currentWebView!!.destroy()
                    currentWebView = null
                }
                isWebViewShowing = false
                currentDialog = null
            }

            currentDelegate = delegate
            currentUrl = url

            val permissions_local = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )

            if (!hasPermissions(permissions_local, context)) {
                val prefs = activity.getSharedPreferences("video_call_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("pending_avchat_url", url)
                    .putBoolean("has_pending_delegate", delegate != null)
                    .apply()

                requestPermissions(activity)
                Toast.makeText(context, "Please grant camera and microphone permissions for video call", Toast.LENGTH_LONG).show()
                return
            }

            showVideoCallWebView(context, activity, url, delegate)
        }

        @JvmStatic
        fun loadAVChatView(context: Context, activity: Activity, url: String) {
            loadAVChatView(context, activity, url, null)
        }

        @JvmStatic
        private fun showVideoCallWebView(context: Context, activity: Activity, url: String, delegate: VideoCallDelegate?) {
            try {
                val ad_dialog = Dialog(context, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
                ad_dialog.setCancelable(false)
                ad_dialog.setCanceledOnTouchOutside(false)

                val inflater = activity.layoutInflater
                val webViewLayout = inflater.inflate(R.layout.webview, null)
                val ll_view = webViewLayout.findViewById<RelativeLayout>(R.id.ll_view)
                val webView = webViewLayout.findViewById<WebView>(R.id.webView)
                val tv_contactName = webViewLayout.findViewById<TextView>(R.id.tv_contactName)
                val btn_close = webViewLayout.findViewById<ImageButton>(R.id.btn_back)
                ll_view.visibility = GONE
                tv_contactName.text = if (Constants.NAME != null) Constants.NAME else "Video Call"

                val webSettings = webView.settings
                webSettings.supportZoom()
                webSettings.builtInZoomControls = false
                webSettings.displayZoomControls = false
                webSettings.javaScriptEnabled = true
                webSettings.mediaPlaybackRequiresUserGesture = false
                webSettings.allowFileAccess = true
                webSettings.allowContentAccess = true
                webSettings.domStorageEnabled = true
                webSettings.databaseEnabled = true
                webSettings.loadWithOverviewMode = true
                webSettings.useWideViewPort = true
                webSettings.cacheMode = WebSettings.LOAD_DEFAULT
                webSettings.setGeolocationEnabled(true)

                webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptThirdPartyCookies(webView, true)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    WebView.setWebContentsDebuggingEnabled(true)
                }

                webView.addJavascriptInterface(object : Any() {
                    @JavascriptInterface
                    fun onCallEnded() {
                        Log.d(TAG, "onCallEnded called from JavaScript")
                        Handler(Looper.getMainLooper()).post {
                            delegate?.onCallEnded() ?: currentDelegate?.onCallEnded()
                            if (ad_dialog.isShowing) {
                                ad_dialog.dismiss()
                            }
                            cleanup()
                        }
                    }

                    @JavascriptInterface
                    fun onMeetingCancelled() {
                        Log.d(TAG, "onMeetingCancelled called from JavaScript")
                        Handler(Looper.getMainLooper()).post {
                            delegate?.onCallEnded() ?: currentDelegate?.onCallEnded()
                            if (ad_dialog.isShowing) {
                                ad_dialog.dismiss()
                            }
                            cleanup()
                        }
                    }

                    @JavascriptInterface
                    fun log(message: String) {
                        Log.d(TAG, "WebView-JS: $message")
                    }
                }, "Android")

                webView.webChromeClient = object : WebChromeClient() {
                    override fun onPermissionRequest(request: PermissionRequest?) {
                        Log.d(TAG, "onPermissionRequest: " + (request?.resources?.contentToString() ?: "null"))
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            request?.grant(request.resources)
                        }
                    }

                    override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
                        callback?.invoke(origin, true, false)
                    }

                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        Log.d(TAG, "WebView Console: " + (consoleMessage?.message() ?: "null"))
                        return true
                    }
                }

                webView.webViewClient = object : WebViewClient() {
                    private var isCallEnded = false
                    private var loadedUrl = ""

                    private fun triggerCallEnded() {
                        if (isCallEnded) return
                        isCallEnded = true

                        Handler(Looper.getMainLooper()).post {
                            delegate?.onCallEnded() ?: currentDelegate?.onCallEnded()
                            if (ad_dialog.isShowing) {
                                ad_dialog.dismiss()
                            }
                            cleanup()
                        }
                    }

                    override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        Log.d(TAG, "onPageStarted: $url")
                    }

                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                        Log.d(TAG, "onPageFinished: $url")

                        if (loadedUrl.isNotEmpty() && url != loadedUrl && !url.contains("about:blank")) {
                            Log.d(TAG, "Page navigated away from meeting URL — treating as call ended")
                            triggerCallEnded()
                            return
                        }

                        if (loadedUrl.isEmpty()) {
                            loadedUrl = url
                        }

                        val jsCode = """
                            (function() {
                               function hookEndCallButton() {
                                   var selectors = [
                                       '[data-testid="end-call"]',
                                       '[aria-label="End call"]',
                                       '[title="End call"]',
                                       '.end-call-btn',
                                       '#end-call',
                                       'button[class*="end"]',
                                       'button[class*="leave"]',
                                       'button[class*="hangup"]',
                                       '[class*="endCall"]',
                                       '[class*="leaveCall"]'
                                   ];
                                   var cancelSelectors = [
                                       '[data-testid="cancel-meeting"]',
                                       '[aria-label="Cancel meeting"]',
                                       '[title="Cancel meeting"]',
                                       '[aria-label="Cancel Meeting"]',
                                       '[title="Cancel Meeting"]',
                                       '.cancel-meeting-btn',
                                       '#cancel-meeting',
                                       'button[class*="cancel"]',
                                       '[class*="cancelMeeting"]',
                                       '[class*="cancelCall"]',
                                       'button[class*="decline"]',
                                       '[class*="declineCall"]',
                                       '[class*="rejectCall"]'
                                   ];
                                   function checkButtons() {
                                       for (var i = 0; i < selectors.length; i++) {
                                           var btns = document.querySelectorAll(selectors[i]);
                                           for (var j = 0; j < btns.length; j++) {
                                               var btn = btns[j];
                                               if (btn && !btn._androidHooked) {
                                                   btn._androidHooked = true;
                                                   btn.addEventListener('click', function() {
                                                       Android.log('End call button clicked');
                                                       Android.onCallEnded();
                                                   });
                                               }
                                           }
                                       }
                                       for (var ci = 0; ci < cancelSelectors.length; ci++) {
                                           var cancelBtns = document.querySelectorAll(cancelSelectors[ci]);
                                           for (var cj = 0; cj < cancelBtns.length; cj++) {
                                               var cancelBtn = cancelBtns[cj];
                                               if (cancelBtn && !cancelBtn._androidCancelHooked) {
                                                   cancelBtn._androidCancelHooked = true;
                                                   cancelBtn.addEventListener('click', function() {
                                                       Android.log('Cancel meeting button clicked');
                                                       Android.onMeetingCancelled();
                                                   });
                                               }
                                           }
                                       }
                                   }
                                   checkButtons();
                                   var observer = new MutationObserver(function() { checkButtons(); });
                                   observer.observe(document.body, { childList: true, subtree: true });
                                   window.addEventListener('unload', function() {
                                       Android.log('Window unload fired — meeting ended');
                                       Android.onCallEnded();
                                   });
                                   window.addEventListener('beforeunload', function() {
                                       Android.log('Window beforeunload fired — meeting ended');
                                       Android.onCallEnded();
                                   });
                                   window.addEventListener('message', function(event) {
                                       Android.log('postMessage received: ' + JSON.stringify(event.data));
                                       var data = event.data;
                                       if (data) {
                                           var str = typeof data === 'string' ? data.toLowerCase() : JSON.stringify(data).toLowerCase();
                                           if (str.indexOf('call-ended') !== -1 ||
                                               str.indexOf('callended') !== -1 ||
                                               str.indexOf('meeting-ended') !== -1 ||
                                               str.indexOf('meetingended') !== -1 ||
                                               str.indexOf('left') !== -1 ||
                                               str.indexOf('hangup') !== -1) {
                                               Android.log('Meeting end detected via postMessage');
                                               Android.onCallEnded();
                                           }
                                       }
                                   });
                                   Android.log('All hooks initialized');
                               }
                               if (document.readyState === 'loading') {
                                   document.addEventListener('DOMContentLoaded', hookEndCallButton);
                               } else {
                                   hookEndCallButton();
                               }
                            })();
                        """.trimIndent()

                        view.evaluateJavascript(jsCode, null)
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        Log.d(TAG, "shouldOverrideUrlLoading: $url")
                        if (loadedUrl.isNotEmpty() && url != loadedUrl && !url.contains(loadedUrl) && !loadedUrl.contains(url)) {
                            Log.d(TAG, "URL changed significantly — treating as call ended")
                            triggerCallEnded()
                            return true
                        }
                        view.loadUrl(url)
                        return false
                    }

                    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                        Log.e(TAG, "WebViewError: " + (error?.toString() ?: "Unknown"))
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && error != null) {
                            Toast.makeText(context, "Error loading page: " + error.description, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Log.d(TAG, "Loading URL: $url")
                webView.loadUrl(url)

                ad_dialog.setOnDismissListener { dialog ->
                    Log.d(TAG, "Dialog dismissed")
                    delegate?.onBackPressed() ?: currentDelegate?.onBackPressed()
                    cleanup()
                }

                btn_close.setOnClickListener { v ->
                    Log.d(TAG, "Close button clicked")
                    delegate?.onBackPressed() ?: currentDelegate?.onBackPressed()
                    if (ad_dialog.isShowing) {
                        ad_dialog.dismiss()
                    }
                    cleanup()
                }

                ad_dialog.setContentView(webViewLayout)
                ad_dialog.show()

                currentDialog = ad_dialog
                currentWebView = webView
                isWebViewShowing = true

            } catch (e: Exception) {
                Log.e(TAG, "Error showing WebView: " + e.message)
                e.printStackTrace()
                Toast.makeText(context, "Error starting video call", Toast.LENGTH_SHORT).show()
                cleanup()
            }
        }

        @JvmStatic
        fun handlePermissionResult(activity: Activity) {
            val prefs = activity.getSharedPreferences("video_call_prefs", Context.MODE_PRIVATE)
            val pendingUrl = prefs.getString("pending_avchat_url", "") ?: ""
            val hasPendingDelegate = prefs.getBoolean("has_pending_delegate", false)

            if (pendingUrl.isNotEmpty()) {
                prefs.edit().clear().apply()

                showVideoCallWebView(activity, activity, pendingUrl, object : VideoCallDelegate {
                    override fun onCallEnded() {
                        Log.d(TAG, "Permission result - Call ended")
                        activity.runOnUiThread {
                            Toast.makeText(activity, "Video call ended", Toast.LENGTH_SHORT).show()
                            activity.finish()
                        }
                    }

                    override fun onBackPressed() {
                        Log.d(TAG, "Permission result - Back pressed")
                        activity.runOnUiThread {
                            Toast.makeText(activity, "Video call closed", Toast.LENGTH_SHORT).show()
                            activity.finish()
                        }
                    }
                })
            }
        }

        @JvmStatic
        fun updateCachedUserData(context: Context) {
            try {
                val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val existingJson = prefs.getString("Json_key", "") ?: ""

                if (existingJson.isNotEmpty()) {
                    val userJson = JSONObject(existingJson)
                    userJson.put("requiresTermsAcceptance", Constants.requiresTermsAcceptance)
                    userJson.put("termsVersion", Constants.termsVersion)

                    prefs.edit().putString("Json_key", userJson.toString()).apply()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun hasPermissions(permissions: Array<String>, context: Context): Boolean {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
            return true
        }

        @JvmStatic
        fun requestPermissions(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                    PERMISSION_REQUEST_CAMERA_AUDIO
                )
            }
        }

        @JvmStatic
        fun cleanup() {
            Log.d(TAG, "Cleaning up resources")

            if (currentWebView != null) {
                currentWebView!!.loadUrl("about:blank")
                currentWebView!!.stopLoading()
                currentWebView!!.clearHistory()
                currentWebView!!.clearCache(true)
                currentWebView!!.destroy()
                currentWebView = null
            }

            if (currentDialog != null && currentDialog!!.isShowing) {
                currentDialog!!.dismiss()
            }

            currentDialog = null
            isWebViewShowing = false
            currentDelegate = null
            currentUrl = ""
        }

        @JvmStatic
        fun isWebViewShowing(): Boolean {
            return isWebViewShowing
        }

        @JvmStatic
        fun getCurrentUrl(): String {
            return currentUrl
        }

        @JvmStatic
        fun loadWebView(context: Context, activity: Activity) {
            val dialogBuilder = AlertDialog.Builder(activity)
            val ad_dialog = Dialog(context, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
            val inflater = activity.layoutInflater
            val webViewLayout = inflater.inflate(R.layout.webview, null)
            val webView = webViewLayout.findViewById<WebView>(R.id.webView)
            val tv_contactName = webViewLayout.findViewById<TextView>(R.id.tv_contactName)
            tv_contactName.text = Constants.NAME
            val btn_close = webViewLayout.findViewById<ImageButton>(R.id.btn_back)

            webView.settings.supportZoom()
            webView.settings.javaScriptEnabled = true
            webView.settings.mediaPlaybackRequiresUserGesture = false
            webView.settings.allowFileAccess = true
            webView.settings.allowContentAccess = true
            webView.settings.domStorageEnabled = true
            webView.settings.databaseEnabled = true
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptThirdPartyCookies(webView, true)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true)
            }

            val permissions_local = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )

            webView.webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest?) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        request?.grant(request?.resources)
                    }
                    if (!hasPermissions(permissions_local, context)) {
                        requestPermissions(activity)
                    }
                }
            }

            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    view.loadUrl(url)
                    return false
                }

                override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                    Log.e("WebViewError", "Error loading page: " + error.toString())
                }
            }

            val loginType = "pro"
            val token = Constants.TOKEN ?: ""
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            val jid = pref.getString("xmpp_jid", null) ?: ""
            val finalUrl = getString(loginType, token, jid)
            Log.e("AV", "AVLink: $finalUrl")
            webView.loadUrl(finalUrl)

            ad_dialog.setContentView(webViewLayout)
            ad_dialog.show()

            btn_close.setOnClickListener {
                ad_dialog.dismiss()
            }
        }

        @JvmStatic
        fun showAlertDialog(view: View?, activity: Activity) {
            val alertDialogBuilder = androidx.appcompat.app.AlertDialog.Builder(activity)

            alertDialogBuilder.setTitle("Permission")
            alertDialogBuilder.setMessage("Required Camera and Microphone permission for Video chat.")

            alertDialogBuilder.setPositiveButton("Cancel") { dialog, which ->
                dialog.cancel()
                Toast.makeText(activity, "You clicked on No", Toast.LENGTH_SHORT).show()
            }

            alertDialogBuilder.setNegativeButton("Ok") { dialog, which ->
                requestPermissions(activity)
            }

            alertDialogBuilder.setCancelable(false)

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        @JvmStatic
        fun showReDirectionPopup(activity: Activity, fragment: Fragment?, confirmText: String) {
            if (isPopupShowing) {
                return
            }
            isPopupShowing = true

            val dialogBuilder = AlertDialog.Builder(activity)
            val inflater = activity.layoutInflater
            val dialogLayout = inflater.inflate(R.layout.redirection_popup, null)

            val tv_confirmation = dialogLayout.findViewById<TextView>(R.id.tv_confirmation)
            tv_confirmation.setText(R.string.alert_)
            tv_confirmation.gravity = Gravity.CENTER
            tv_confirmation.textAlignment = View.TEXT_ALIGNMENT_CENTER

            val tv_confirmContent = dialogLayout.findViewById<TextView>(R.id.tv_confirmContent)
            val iv_close = dialogLayout.findViewById<ImageView>(R.id.iv_close)

            val clickHereText = "Click here"
            val spannableString = SpannableString(confirmText)

            val startIndex = confirmText.indexOf(clickHereText)
            val endIndex = startIndex + clickHereText.length

            val dialog = dialogBuilder.create()
            dialog.setView(dialogLayout)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    if (fragment != null) {
                        Constants.mainActivity?.navigationToModules(fragment)
                    }
                    dialog.dismiss()
                    isPopupShowing = false
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                    ds.color = activity.getColor(R.color.blue)
                    ds.bgColor = Color.TRANSPARENT
                }
            }

            spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            tv_confirmContent.text = spannableString
            tv_confirmContent.movementMethod = LinkMovementMethod.getInstance()
            tv_confirmContent.highlightColor = Color.TRANSPARENT

            tv_confirmContent.isFocusable = true
            tv_confirmContent.isClickable = true

            dialog.show()

            val window = dialog.window
            if (window != null) {
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val marginInDp = 20
                val scale = activity.resources.displayMetrics.density
                val marginInPx = (marginInDp * scale + 0.5f).toInt()

                val screenWidth = activity.resources.displayMetrics.widthPixels
                val dialogWidth = screenWidth - (2 * marginInPx)

                window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT)

                val layoutParams = window.attributes
                layoutParams.gravity = Gravity.CENTER
                window.attributes = layoutParams
            }

            iv_close.setOnClickListener {
                dialog.dismiss()
                isPopupShowing = false
            }

            dialog.setOnDismissListener {
                isPopupShowing = false
            }
        }

        @JvmStatic
        fun showRenewalPopup(activity: Activity) {}

        @JvmStatic
        fun launchPaySubscriptionPage(activity: Activity?) {
            if (activity == null) return
            val AUTH_REQUEST_CODE = 1001
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("${Constants.paymentUrl}=${(Constants.FirmEmail ?: "").lowercase(Locale.getDefault())}&users=${Constants.User_Allowed}")
            ActivityCompat.startActivityForResult(activity, intent, AUTH_REQUEST_CODE, null)
        }

        @JvmStatic
        fun showErrorAlert(message: String?, activity: Activity?): Dialog? {
            if (activity == null) return null
            val dialogBuilder = AlertDialog.Builder(activity)
            val inflater = activity.layoutInflater
            val dialogLayout = inflater.inflate(R.layout.alert_dialog, null)
            val ll_view = dialogLayout.findViewById<LinearLayout>(R.id.ll_view)
            ll_view.background = activity.getDrawable(R.drawable.rectangle_light_grey_bg)
            val tv_ok = dialogLayout.findViewById<TextView>(R.id.tv_ok)
            tv_ok.textSize = 17f
            tv_ok.setPadding(20, 20, 20, 20)
            tv_ok.setText(R.string.ok)

            tv_ok.setTextColor(activity.getColor(R.color.light_blue))
            val alert_content = dialogLayout.findViewById<TextView>(R.id.alert_content)
            alert_content.text = message
            alert_content.gravity = Gravity.CENTER
            val dialog = dialogBuilder.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            tv_ok.setOnClickListener {
                dialog.dismiss()
            }
            dialog.setView(dialogLayout)
            dialog.show()
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            return dialog
        }

        @JvmStatic
        fun get_progress(activity: Activity?): Dialog? {
            if (activity == null) return null
            if (currentDialog != null && currentDialog!!.isShowing) {
                currentDialog!!.dismiss()
            }

            currentDialog = Dialog(activity)
            currentDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            currentDialog!!.setContentView(R.layout.loading)
            currentDialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            currentDialog!!.setCancelable(false)
            currentDialog!!.setCanceledOnTouchOutside(false)

            currentDialog!!.show()
            return currentDialog!!
        }

        @JvmStatic
        fun dismiss_dialog(dialog: Dialog?) {
            if (dialog != null && dialog.isShowing) {
                dialog.dismiss()
            }
        }

        @JvmStatic
        fun stringToDateTimeDefault(dateTime: String?, format: String?): Date? {
            var result: Date? = null
            try {
                if (dateTime != null && format != null) {
                    val sdf = SimpleDateFormat(format, Locale.getDefault())
                    result = sdf.parse(dateTime)
                }
            } catch (ex: Exception) {
                ex.fillInStackTrace()
            }
            return result
        }

        @JvmStatic
        fun getDateToString(date: Date?, formats: String): String {
            var result = ""
            try {
                var d = date
                if (d == null) {
                    d = f_Calendar.time
                }
                val dateFormat = SimpleDateFormat(formats, Locale.getDefault())
                result = dateFormat.format(d)
            } catch (e: Exception) {
            }
            return result
        }

        @JvmStatic
        fun isNetworkAvailable(context: Context): Boolean {
            var isNetworkAvailable = false
            try {
                val objConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetworkInfo = objConnectivityManager.activeNetworkInfo
                isNetworkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected
            } catch (e: Exception) {
                AndroidUtils.logMsg("@ commonmethods isNetworkAvailable(): " + e.toString())
            }
            return isNetworkAvailable
        }

        @JvmStatic
        fun formatToMMMddYYYY(dateString: String?): String {
            if (dateString == null || dateString.trim { it <= ' ' }.isEmpty()) return ""
            val possibleFormats = arrayOf(
                "dd-MM-yyyy",
                "yyyy-MM-dd",
                "MM-dd-yyyy",
                "dd/MM/yyyy",
                "MM/dd/yyyy",
                "yyyy/MM/dd",
                "dd MMM yyyy",
                "MMM dd, yyyy",
                "MMM dd yyyy",
                "dd MMM, yyyy",
                "yyyyMMdd",
                "ddMMyyyy",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "dd-MM-yyyy HH:mm:ss",
                "dd/MM/yyyy HH:mm:ss"
            )

            for (format in possibleFormats) {
                try {
                    val inputSdf = SimpleDateFormat(format, Locale.US)
                    inputSdf.isLenient = false
                    val date = inputSdf.parse(dateString)
                    if (date != null) {
                        val outputSdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                        return outputSdf.format(date)
                    }
                } catch (ignored: Exception) {
                }
            }
            return ""
        }

        @JvmStatic
        fun normalizeDeletedOn(dateString: String?): String? {
            if (dateString == null || dateString.trim { it <= ' ' }.isEmpty() || dateString == "null") return null
            var date: Date? = null
            try {
                date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(dateString)
            } catch (ignored: Exception) {
            }
            if (date == null) {
                try {
                    date = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault()).parse(dateString)
                } catch (ignored: Exception) {
                }
            }
            if (date == null) return dateString
            return SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault()).format(date)
        }

        @JvmStatic
        fun validateStartAndEndDates(context: Context, startDateTextView: TextView, endDateTextView: TextView, dateFormat: String): Boolean {
            val startDateStr = startDateTextView.text.toString().trim()
            val endDateStr = endDateTextView.text.toString().trim()
            if (startDateStr.isEmpty() || endDateStr.isEmpty()) {
                return true
            }
            return try {
                val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
                val startDate = sdf.parse(startDateStr)
                val endDate = sdf.parse(endDateStr)
                if (endDate != null && startDate != null && endDate.before(startDate)) {
                    showAlert("End date should not be earlier than start date", context as Activity)
                    endDateTextView.text = ""
                    return false
                }
                if (startDate != null && endDate != null && startDate.after(endDate)) {
                    showAlert("Start date should not be later than end date", context as Activity)
                    startDateTextView.text = ""
                    return false
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                true
            }
        }

        @JvmStatic
        fun showDatePicker(textView: TextView?, isStartDate: Boolean) {
            showDatePicker(textView, isStartDate, null)
        }

        @JvmStatic
        fun showDatePicker(textView: TextView?, isStartDate: Boolean, onDateSet: Runnable?) {
            if (textView == null) return
            val calendar = Calendar.getInstance()
            if (textView.text != null && textView.text.toString().trim().isNotEmpty()) {
                val value = textView.text.toString().trim()
                val possibleFormats = arrayOf(
                    "dd-MM-yyyy", "yyyy-MM-dd", "MM-dd-yyyy",
                    "dd/MM/yyyy", "MM/dd/yyyy", "yyyy/MM/dd",
                    "MMM dd, yyyy", "dd MMM yyyy",
                    "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss"
                )
                for (format in possibleFormats) {
                    try {
                        val sdf = SimpleDateFormat(format, Locale.US)
                        val parsed = sdf.parse(value)
                        if (parsed != null) {
                            calendar.time = parsed
                            break
                        }
                    } catch (ignored: Exception) {
                    }
                }
            }

            val context = textView.context
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_date_picker, null)

            val npMonth = view.findViewById<NumberPicker>(R.id.np_month)
            val npDay = view.findViewById<NumberPicker>(R.id.np_day)
            val npYear = view.findViewById<NumberPicker>(R.id.np_year)
            val ivClose = view.findViewById<ImageView>(R.id.iv_close)
            val btnOk = view.findViewById<Button>(R.id.btn_ok)
            val btnCancel = view.findViewById<Button>(R.id.btn_cancel)

            val months = DateFormatSymbols(Locale.US).shortMonths
            val monthNames = Arrays.copyOf(months, 12)
            npMonth.minValue = 0
            npMonth.maxValue = 11
            npMonth.displayedValues = monthNames
            npMonth.value = calendar.get(Calendar.MONTH)
            npMonth.wrapSelectorWheel = true

            val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            npDay.minValue = 1
            npDay.maxValue = maxDay
            npDay.value = calendar.get(Calendar.DAY_OF_MONTH)
            npDay.wrapSelectorWheel = false

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            npYear.minValue = currentYear - 100
            npYear.maxValue = currentYear + 10
            npYear.value = calendar.get(Calendar.YEAR)
            npYear.wrapSelectorWheel = false

            val refreshDays = NumberPicker.OnValueChangeListener { picker, oldVal, newVal ->
                val temp = Calendar.getInstance()
                temp.set(Calendar.YEAR, npYear.value)
                temp.set(Calendar.MONTH, npMonth.value)
                val max = temp.getActualMaximum(Calendar.DAY_OF_MONTH)
                npDay.maxValue = max
                if (npDay.value > max) npDay.value = max
            }
            npMonth.setOnValueChangedListener(refreshDays)
            npYear.setOnValueChangedListener(refreshDays)

            val dialog = AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .create()

            Objects.requireNonNull(dialog.window)
                ?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            ivClose.setOnClickListener { dialog.dismiss() }

            btnCancel.setOnClickListener {
                textView.text = ""
                dialog.dismiss()
            }

            btnOk.setOnClickListener {
                calendar.set(Calendar.YEAR, npYear.value)
                calendar.set(Calendar.MONTH, npMonth.value)
                calendar.set(Calendar.DAY_OF_MONTH, npDay.value)

                val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                textView.text = displayFormat.format(calendar.time)

                onDateSet?.run()
                dialog.dismiss()
            }

            dialog.show()
        }

        @JvmStatic
        fun convertAnyDateToDDMMYYYY(dateString: String?): String {
            if (dateString == null) return ""
            val possibleFormats = arrayOf(
                "yyyy-MM-dd",
                "dd-MM-yyyy",
                "MM-dd-yyyy",
                "dd/MM/yyyy",
                "yyyy/MM/dd",
                "MM/dd/yyyy",
                "dd MMM yyyy",
                "MMM dd, yyyy",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "dd-MM-yyyy HH:mm:ss",
                "dd/MM/yyyy HH:mm:ss",
                "EEE MMM dd HH:mm:ss z yyyy"
            )

            for (format in possibleFormats) {
                try {
                    val input = SimpleDateFormat(format, Locale.getDefault())
                    input.isLenient = false
                    val date = input.parse(dateString)
                    if (date != null) {
                        val output = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        return output.format(date)
                    }
                } catch (ignored: Exception) {
                }
            }
            return ""
        }

        @JvmStatic
        fun convertAnyDateToYYYYMMDD(dateString: String?): String {
            if (dateString == null) return ""
            val possibleFormats = arrayOf(
                "yyyy-MM-dd",
                "dd-MM-yyyy",
                "ddMMyyyy",
                "MM-dd-yyyy",
                "dd/MM/yyyy",
                "yyyy/MM/dd",
                "MM/dd/yyyy",
                "dd MMM yyyy",
                "MMM dd, yyyy",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "dd-MM-yyyy HH:mm:ss",
                "dd/MM/yyyy HH:mm:ss",
                "EEE MMM dd HH:mm:ss z yyyy"
            )

            for (format in possibleFormats) {
                try {
                    val input = SimpleDateFormat(format, Locale.getDefault())
                    input.isLenient = false
                    val date = input.parse(dateString)
                    if (date != null) {
                        val output = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        return output.format(date)
                    }
                } catch (ignored: Exception) {
                }
            }
            return ""
        }

        @JvmStatic
        fun getDateSelectedFormt(original_format: String?, selected_formate: String?, date: String?): String? {
            val converted_date = stringToDateTimeDefault(date, original_format)
            return if (converted_date != null && selected_formate != null) getDateToString(converted_date, selected_formate) else ""
        }

        @JvmStatic
        fun getDocTypeValue(value: String?): String {
            var name = ""
            when (value) {
                "driver_license" -> name = "Driver Licence"
                "passport" -> name = "Passport"
                "aadhar" -> name = "AADHAR Card"
                "pancard" -> name = "PAN Card"
                "voterid" -> name = "Voter ID"
                "nic" -> name = "National Identity Document (DNI)"
                "ssc" -> name = "Social Security Card"
                "cpf" -> name = "Cadastro de Pessoas Físicas (CPF)"
            }
            return name
        }

        @JvmStatic
        fun get_affiliationType(value: String?): String {
            var affiliation_type = ""
            when (value) {
                "citz" -> affiliation_type = "Citizen"
                "dcitz" -> affiliation_type = "Dual Citizenship"
                "pr" -> affiliation_type = "Permanent Resident"
                "tvs" -> affiliation_type = "Temporary Resident - Student"
                "tvw" -> affiliation_type = "Temporary Resident - Work"
            }
            return affiliation_type
        }

        @JvmStatic
        fun getemailpattern(): String {
            return "[a-zA-Z0-9._-]+@[a-z-]+\\.+[a-z]+"
        }

        @JvmStatic
        fun getmobilepattern(): String {
            return "[0-9]"
        }

        @JvmStatic
        fun remove_credential_preference(context: Context?) {
        }

        @JvmStatic
        fun displayFile(url: String, context: Context) {
            val str_path = Environment.getExternalStorageDirectory().toString() + File.separator + url
            val photoURI = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", File(str_path))
            try {
                val pdfFile = File(str_path)
                val target = Intent(Intent.ACTION_VIEW)
                target.setDataAndType(photoURI, "application/pdf")
                target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val intent = Intent.createChooser(target, "Open File")
                context.startActivity(intent)
            } catch (e: Exception) {
                e.message
            }
        }

        @JvmStatic
        fun send_notification(context: Context, UID: String?, msg: String?, subject: String?) {
            if (ChatConnectionService.getState() == ChatConnection.ConnectionState.CONNECTED) {
                attempts = 0
                val intent = Intent(ChatConnectionService.SEND_MESSAGE)
                intent.putExtra(ChatConnectionService.BUNDLE_MESSAGE_BODY, msg)
                intent.putExtra(ChatConnectionService.BUNDLE_TO, UID)
                intent.putExtra(ChatConnectionService.BUNDLE_MESSAGE_SUBJECT, subject)
                context.sendBroadcast(intent)
            } else {
                attempts++
                if (ChatConnectionService.getState() != ChatConnection.ConnectionState.CONNECTED) {
                    reconnecXMPPServer(context)
                }
                val handler = Handler(Looper.getMainLooper())
                val finalAttempts = attempts
                handler.postDelayed({
                    if (finalAttempts < 5) {
                        send_notification(context, UID, msg, subject)
                    } else {
                        attempts = 0
                    }
                }, 5000)
            }
        }

        @JvmStatic
        fun getFileType(filename: String): String {
            var type = ""
            val name = filename.substring(filename.lastIndexOf(".") + 1)
            if (name.equals("png", ignoreCase = true) || name.equals("jpg", ignoreCase = true) || name.equals("JPEG", ignoreCase = true)) {
                type = "image/jpeg"
            } else if (name.equals("pdf", ignoreCase = true)) {
                type = "image/pdf"
            } else if (name.equals("docx", ignoreCase = true)) {
                type = "image/docx"
            }
            return type
        }

        @JvmStatic
        fun reconnecXMPPServer(context: Context) {
            val i1 = Intent(context, ChatConnectionService::class.java)
            context.stopService(i1)
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                context.startService(Intent(context, ChatConnectionService::class.java))
            }, 5000)
        }

        @JvmStatic
        fun display_listview(ischecked: Boolean, listView: ListView?) {
            if (listView == null) return
            if (ischecked) {
                listView.visibility = VISIBLE
            } else {
                listView.visibility = View.GONE
            }
        }

        @JvmStatic
        fun ToggleButton(ListSize: Int, button: View?) {
            if (button == null) return
            if (ListSize == 0) {
                button.alpha = 0.5f
                button.isEnabled = false
            } else {
                button.alpha = 1.0f
                button.isEnabled = true
            }
        }

        @JvmStatic
        fun DisplaySpinnerView(
            listView: ListView?, tv_listview: TextView?, tv_value: String?,
            img_dropdown: ImageView?, img_clear: ImageView?, img_dropdown_clicked: Boolean
        ) {
            if (listView == null || tv_listview == null || img_dropdown == null || img_clear == null) return
            var value = tv_value
            if (img_dropdown_clicked) {
                img_dropdown.visibility = View.GONE
                img_clear.visibility = VISIBLE
                listView.visibility = View.GONE
                tv_listview.text = value
            } else {
                img_dropdown.visibility = VISIBLE
                img_clear.visibility = View.GONE
                listView.visibility = View.GONE
                value = ""
                tv_listview.text = value
            }
        }

        @JvmStatic
        fun DisplaySpinnerView(
            listView: ListView?,
            tv_listview: TextView?,
            tv_value: String?,
            img_dropdown: ImageView?,
            img_clear: ImageView?,
            img_dropdown_clicked: Boolean,
            adapter: CommonSpinnerAdapter<*>?
        ) {
            if (listView == null || tv_listview == null || img_dropdown == null || img_clear == null) return
            DisplaySpinnerView(
                listView, tv_listview, tv_value,
                img_dropdown, img_clear,
                !img_dropdown_clicked,
                adapter, "Search"
            )
        }

        @JvmStatic
        fun DisplaySpinnerView(
            listView: ListView?,
            tv_listview: TextView?,
            tv_value: String?,
            img_dropdown: ImageView?,
            img_clear: ImageView?,
            img_dropdown_clicked: Boolean,
            adapter: CommonSpinnerAdapter<*>?,
            SearchHint: String
        ) {
            if (listView == null || tv_listview == null || img_dropdown == null || img_clear == null) return
            if (img_dropdown_clicked) {
                if (adapter != null) {
                    adapter.filter.filter("")

                    val itemCount = adapter.count

                    if (itemCount > 4) {
                        val existingSearch = listView.getTag(R.id.tag_search_bar) as View?
                        if (existingSearch == null) {
                            attachSearch(listView, adapter, SearchHint)
                        } else {
                            existingSearch.elevation = 10f
                            existingSearch.visibility = VISIBLE
                            if (existingSearch.parent is LinearLayout) {
                                (existingSearch.parent as LinearLayout).visibility = VISIBLE
                            }
                        }

                        val searchCardView = listView.getTag(R.id.tag_search_bar) as View?
                        if (searchCardView != null) {
                            searchCardView.visibility = VISIBLE
                            if (searchCardView.parent is LinearLayout) {
                                (searchCardView.parent as LinearLayout).visibility = VISIBLE
                            }
                            val etSearch = listView.getTag(R.id.tag_search_edittext) as EditText?
                            etSearch?.setText("")
                        }
                    } else {
                        val searchCardView = listView.getTag(R.id.tag_search_bar) as View?
                        if (searchCardView != null) {
                            searchCardView.visibility = GONE
                            if (searchCardView.parent is LinearLayout) {
                                (searchCardView.parent as LinearLayout).visibility = VISIBLE
                            }
                        }
                    }

                    listView.visibility = VISIBLE
                    LoadList(listView, listView.context, itemCount, true)
                }

                if (tv_value != null && tv_value.isNotEmpty()) {
                    img_dropdown.visibility = View.GONE
                    img_clear.visibility = VISIBLE
                    tv_listview.text = tv_value
                } else {
                    img_dropdown.visibility = VISIBLE
                    img_clear.visibility = View.GONE
                }
            } else {
                if (tv_value != null && tv_value.isNotEmpty()) {
                    tv_listview.text = tv_value
                    img_dropdown.visibility = View.GONE
                    img_clear.visibility = VISIBLE
                } else {
                    tv_listview.text = ""
                    img_dropdown.visibility = VISIBLE
                    img_clear.visibility = GONE
                }
                listView.visibility = View.GONE

                val searchCardView = listView.getTag(R.id.tag_search_bar) as View?
                if (searchCardView != null) {
                    searchCardView.visibility = GONE
                    val etSearch = listView.getTag(R.id.tag_search_edittext) as EditText?
                    etSearch?.setText("")
                    if (searchCardView.parent is LinearLayout) {
                        (searchCardView.parent as LinearLayout).visibility = GONE
                    }
                }

                adapter?.filter?.filter("")
            }
        }

        interface TagPopupCallback {
            fun onTagsSaved(combinedTags: JSONObject?, documentModel: Any?)
            fun onDismiss()
        }

        @JvmStatic
        fun isDuplicateTagType(
            enteredTagType: String,
            tags_list: ArrayList<DocumentsModel>,
            skipPosition: Int
        ): Boolean {
            for (i in tags_list.indices) {
                if (i == skipPosition) continue
                if (tags_list[i].tag_type.equals(enteredTagType.trim(), ignoreCase = true)) {
                    return true
                }
            }
            return false
        }

        @JvmStatic
        fun saveEditedTag(
            tag_type: String,
            tag_name: String,
            edit_position: Int,
            tags_list: ArrayList<DocumentsModel>,
            ll_added_tags: LinearLayout,
            tv_tag_type: TextInputEditText?,
            tv_tag_name: TextInputEditText?,
            activity: Activity
        ) {
            try {
                if (edit_position < 0 || edit_position >= tags_list.size) {
                    showAlert("Invalid position for editing tag.", activity)
                    return
                }

                if (isDuplicateTagType(tag_type, tags_list, edit_position)) {
                    showAlert("This tag type already exists. Please use a different tag type.", activity)
                    return
                }

                val documentsModel = tags_list[edit_position]
                documentsModel.tag_type = tag_type
                documentsModel.tag_name = tag_name
                tags_list[edit_position] = documentsModel

                val view_to_update = ll_added_tags.getChildAt(edit_position)
                if (view_to_update != null) {
                    val tv_edit_tag_document_name = view_to_update.findViewById<TextView>(R.id.tv_document_name)
                    tv_edit_tag_document_name.text = "$tag_type - $tag_name"
                }

                if (tv_tag_name != null && tv_tag_type != null) {
                    tv_tag_name.setText("")
                    tv_tag_type.setText("")
                }
            } catch (e: Exception) {
                Log.e("saveEditedTag", "Error saving edited tag", e)
                showAlert("An error occurred while saving the tag: " + e.message, activity)
            }
        }

        fun interface OnTagsSavedCallback<T> {
            fun onSave(tags_list: ArrayList<DocumentsModel>?, dialog: AlertDialog?)
        }

        @JvmStatic
        fun <T> openAddTagsPopup(
            context: Context,
            activity: Activity,
            inflater: LayoutInflater,
            backgroundView: View?,
            isUpdateTag: Boolean,
            tags_list: ArrayList<DocumentsModel>,
            selected_documents_list: ArrayList<T>,
            ll_added_tags_ref: Array<LinearLayout?>,
            tv_tag_type_ref: Array<TextInputEditText?>,
            tv_tag_name_ref: Array<TextInputEditText?>,
            isedit_ref: BooleanArray,
            edit_position_ref: IntArray,
            onSaved: OnTagsSavedCallback<T>
        ) {
            if (!isUpdateTag && selected_documents_list.isEmpty()) {
                showAlert("Please select atleast one document to add tags", activity)
                return
            }

            val dialogBuilder = AlertDialog.Builder(context)
            backgroundView?.alpha = 0.5f

            val view = inflater.inflate(R.layout.add_tag, null)
            val tv_tag_type = view.findViewById<TextInputEditText>(R.id.tv_tag_type)
            val tv_tag_name = view.findViewById<TextInputEditText>(R.id.tv_tag_name)
            tv_tag_type_ref[0] = tv_tag_type
            tv_tag_name_ref[0] = tv_tag_name

            val tag_type_name = view.findViewById<TextView>(R.id.tag_type_name)
            val tag_name_tv = view.findViewById<TextView>(R.id.tag_name)
            val tv_added_tags = view.findViewById<TextView>(R.id.tv_added_tags)
            val header_name = view.findViewById<TextView>(R.id.header_name)
            val ll_added_tags = view.findViewById<LinearLayout>(R.id.ll_added_tags)
            ll_added_tags_ref[0] = ll_added_tags

            tv_added_tags.setText(R.string.added_tags)
            tag_type_name.setText(R.string.tag_type)
            tag_name_tv.setText(R.string.tag)
            tv_tag_type.setHint(R.string.tag_type)
            tv_tag_name.setHint(R.string.tag)
            tv_tag_name.addTextChangedListener(Validation(tv_tag_name))
            tv_tag_type.addTextChangedListener(Validation(tv_tag_type))
            header_name.setText(if (isUpdateTag) R.string.update_tag else R.string.add_tag)

            val btn_add = view.findViewById<Button>(R.id.btn_add_tags)
            btn_add.setText(R.string.add)

            val filters_title = arrayOf<InputFilter>(InputFilter.LengthFilter(30))
            val filters_desc = arrayOf<InputFilter>(InputFilter.LengthFilter(100))
            tv_tag_type.filters = filters_title
            tv_tag_name.filters = filters_desc

            val btn_cancel = view.findViewById<AppCompatButton>(R.id.btn_cancel_tag)
            val btn_save_tag = view.findViewById<AppCompatButton>(R.id.btn_save_tag)
            val iv_cancel = view.findViewById<ImageView>(R.id.close_edit_docs)

            if (ll_added_tags.childCount > 0) {
                tv_added_tags.visibility = VISIBLE
            } else {
                tv_added_tags.visibility = GONE
            }

            val dialog = dialogBuilder.create()

            if (tags_list.isNotEmpty()) {
                addTagsListing(
                    context, activity, inflater, btn_save_tag, tags_list,
                    ll_added_tags, tv_added_tags, tv_tag_type, tv_tag_name,
                    isedit_ref, edit_position_ref
                )
            }

            btn_save_tag.isEnabled = false
            btn_save_tag.alpha = 0.5f

            val dismissListener = View.OnClickListener { v ->
                if (tags_list.isNotEmpty()) Delete_Popup(activity, dialog) else dialog.dismiss()
            }
            iv_cancel.setOnClickListener(dismissListener)
            btn_cancel.setOnClickListener(dismissListener)

            btn_add.setOnClickListener { v ->
                val typeText = tv_tag_type.text.toString()
                val nameText = tv_tag_name.text.toString()

                if (typeText.isEmpty() && nameText.isEmpty()) {
                    showAlert("Please check the Tag Type, Tag", activity)
                } else if (typeText.isEmpty()) {
                    showAlert("Please check the Tag Type", activity)
                } else if (nameText.isEmpty()) {
                    showAlert("Please check the Tag", activity)
                } else {
                    if (isedit_ref[0]) {
                        saveEditedTag(
                            typeText, nameText,
                            edit_position_ref[0], tags_list,
                            ll_added_tags, tv_tag_type, tv_tag_name, activity
                        )
                    } else {
                        if (isDuplicateTagType(typeText, tags_list, -1)) {
                            showAlert("This tag type already exists. Please use a different tag type.", activity)
                            return@setOnClickListener
                        }
                        addTagsListing(
                            context, activity, inflater, btn_save_tag, tags_list,
                            ll_added_tags, tv_added_tags, tv_tag_type, tv_tag_name,
                            isedit_ref, edit_position_ref
                        )
                    }
                    isedit_ref[0] = false
                    tv_tag_name.setText("")
                    tv_tag_type.setText("")
                    btn_save_tag.isEnabled = true
                    btn_save_tag.alpha = 1.0f
                }
            }

            btn_save_tag.setOnClickListener { v ->
                onSaved.onSave(tags_list, dialog)
            }

            dialog.setOnDismissListener { d ->
                backgroundView?.alpha = 1.0f
            }
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setView(view)
            dialog.show()
        }

        @JvmStatic
        fun addTagsListing(
            context: Context,
            activity: Activity,
            inflater: LayoutInflater,
            btn_save_tag: Button,
            tags_list: ArrayList<DocumentsModel>,
            ll_added_tags: LinearLayout,
            tv_added_tags: TextView,
            tv_tag_type: TextInputEditText,
            tv_tag_name: TextInputEditText,
            isedit_ref: BooleanArray,
            edit_position_ref: IntArray
        ) {
            ll_added_tags.removeAllViews()

            val typeText = tv_tag_type.text.toString()
            val nameText = tv_tag_name.text.toString()

            if (typeText.isNotEmpty() && nameText.isNotEmpty()) {
                val documentsModel = DocumentsModel()
                documentsModel.tag_type = typeText
                documentsModel.tag_name = nameText
                tags_list.add(documentsModel)
            }

            for (i in tags_list.indices) {
                val view_added_tags = inflater.inflate(R.layout.displays_documents_list, null)
                val tv_tag_document_name = view_added_tags.findViewById<TextView>(R.id.tv_document_name)
                val ll_tags = view_added_tags.findViewById<LinearLayout>(R.id.ll_tags)
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(10, 10, 10, 10)
                ll_tags.layoutParams = params

                val chk_box_layout = view_added_tags.findViewById<LinearLayoutCompat>(R.id.chk_box_layout)
                chk_box_layout.visibility = GONE
                val chk_selected_documents = view_added_tags.findViewById<CheckBox>(R.id.chk_selected_documents)
                chk_selected_documents.visibility = GONE

                val iv_edit_tag = view_added_tags.findViewById<ImageView>(R.id.iv_edit_meta)
                val iv_remove_tag = view_added_tags.findViewById<ImageView>(R.id.iv_cancel)
                iv_remove_tag.tag = i

                iv_remove_tag.setOnClickListener { v ->
                    val pos = v.tag as Int
                    ll_added_tags.removeViewAt(pos)
                    btn_save_tag.isEnabled = true
                    btn_save_tag.alpha = 1.0f
                    val model = tags_list[pos]
                    model.tag_name = ""
                    model.tag_type = ""
                    model.isChecked = false
                    tags_list[pos] = model
                    tags_list.removeAt(pos)
                    for (j in 0 until ll_added_tags.childCount) {
                        val iv_remove = ll_added_tags.getChildAt(j).findViewById<ImageView>(R.id.iv_cancel)
                        iv_remove?.tag = j
                    }
                    tv_added_tags.visibility = if (ll_added_tags.childCount > 0) VISIBLE else GONE
                }

                iv_edit_tag.tag = i
                iv_edit_tag.setOnClickListener { v ->
                    val pos = v.tag as Int
                    val model = tags_list[pos]
                    if (model != null) {
                        edit_position_ref[0] = pos
                        isedit_ref[0] = true
                        tv_tag_type.setText(model.tag_type)
                        tv_tag_name.setText(model.tag_name)
                    }
                }

                iv_edit_tag.visibility = VISIBLE
                tv_tag_document_name.text = "${tags_list[i].tag_type} - ${tags_list[i].tag_name}"
                ll_added_tags.addView(view_added_tags)
                tv_added_tags.visibility = if (ll_added_tags.childCount > 0) VISIBLE else GONE
            }
        }

        @JvmStatic
        fun showDatePicker(
            textView: TextView?,
            allowPastDates: Boolean,
            allowCurrentDate: Boolean,
            allowFutureDates: Boolean,
            onDateSet: Runnable?
        ) {
            if (textView == null) return
            val calendar = Calendar.getInstance()
            val minCalendar = Calendar.getInstance()
            val maxCalendar = Calendar.getInstance()

            if (allowPastDates && allowFutureDates) {
                minCalendar.set(Calendar.YEAR, 1900)
                minCalendar.set(Calendar.MONTH, 0)
                minCalendar.set(Calendar.DAY_OF_MONTH, 1)

                maxCalendar.set(Calendar.YEAR, 2100)
                maxCalendar.set(Calendar.MONTH, 11)
                maxCalendar.set(Calendar.DAY_OF_MONTH, 31)
            } else if (!allowPastDates && allowFutureDates) {
                minCalendar.time = Date()
                if (!allowCurrentDate) {
                    minCalendar.add(Calendar.DAY_OF_MONTH, 1)
                }
                maxCalendar.set(Calendar.YEAR, 2100)
                maxCalendar.set(Calendar.MONTH, 11)
                maxCalendar.set(Calendar.DAY_OF_MONTH, 31)
            } else if (allowPastDates && !allowFutureDates) {
                maxCalendar.time = Date()
                if (!allowCurrentDate) {
                    maxCalendar.add(Calendar.DAY_OF_MONTH, -1)
                }
                minCalendar.set(Calendar.YEAR, 1900)
                minCalendar.set(Calendar.MONTH, 0)
                minCalendar.set(Calendar.DAY_OF_MONTH, 1)
            } else if (!allowPastDates && !allowFutureDates && allowCurrentDate) {
                minCalendar.time = Date()
                maxCalendar.time = Date()
            } else {
                return
            }

            if (textView.text != null && textView.text.toString().trim().isNotEmpty()) {
                val value = textView.text.toString().trim()
                val possibleFormats = arrayOf(
                    "dd-MM-yyyy", "yyyy-MM-dd", "MM-dd-yyyy",
                    "dd/MM/yyyy", "MM/dd/yyyy", "yyyy/MM/dd",
                    "MMM dd, yyyy", "dd MMM yyyy",
                    "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss"
                )
                for (format in possibleFormats) {
                    try {
                        val sdf = SimpleDateFormat(format, Locale.US)
                        val parsed = sdf.parse(value)
                        if (parsed != null) {
                            calendar.time = parsed
                            break
                        }
                    } catch (ignored: Exception) {
                    }
                }
            }

            val context = textView.context
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_date_picker, null)

            val npMonth = view.findViewById<NumberPicker>(R.id.np_month)
            val npDay = view.findViewById<NumberPicker>(R.id.np_day)
            val npYear = view.findViewById<NumberPicker>(R.id.np_year)
            val ivClose = view.findViewById<ImageView>(R.id.iv_close)
            val btnOk = view.findViewById<Button>(R.id.btn_ok)
            val btnCancel = view.findViewById<Button>(R.id.btn_cancel)

            val months = DateFormatSymbols(Locale.US).shortMonths
            val monthNames = Arrays.copyOf(months, 12)

            val minYear = minCalendar.get(Calendar.YEAR)
            val maxYear = maxCalendar.get(Calendar.YEAR)
            npYear.minValue = minYear
            npYear.maxValue = maxYear

            var currentYear = calendar.get(Calendar.YEAR)
            if (currentYear < minYear) currentYear = minYear
            if (currentYear > maxYear) currentYear = maxYear
            npYear.value = currentYear
            npYear.wrapSelectorWheel = false

            npMonth.minValue = 0
            npMonth.maxValue = 11
            npMonth.displayedValues = monthNames
            npMonth.wrapSelectorWheel = true

            val updateDayRange = Runnable {
                val year = npYear.value
                val month = npMonth.value

                val temp = Calendar.getInstance()
                temp.set(year, month, 1)
                val maxDay = temp.getActualMaximum(Calendar.DAY_OF_MONTH)

                var minDay = 1
                if (year == minCalendar.get(Calendar.YEAR) && month == minCalendar.get(Calendar.MONTH)) {
                    minDay = minCalendar.get(Calendar.DAY_OF_MONTH)
                }

                var maxAllowedDay = maxDay
                if (year == maxCalendar.get(Calendar.YEAR) && month == maxCalendar.get(Calendar.MONTH)) {
                    maxAllowedDay = maxCalendar.get(Calendar.DAY_OF_MONTH)
                }

                npDay.minValue = minDay
                npDay.maxValue = maxAllowedDay

                var currentDay = calendar.get(Calendar.DAY_OF_MONTH)
                if (currentDay < minDay) currentDay = minDay
                if (currentDay > maxAllowedDay) currentDay = maxAllowedDay
                npDay.value = currentDay
            }

            var currentMonth = calendar.get(Calendar.MONTH)
            if (currentYear == minYear && currentMonth < minCalendar.get(Calendar.MONTH)) {
                currentMonth = minCalendar.get(Calendar.MONTH)
            }
            if (currentYear == maxYear && currentMonth > maxCalendar.get(Calendar.MONTH)) {
                currentMonth = maxCalendar.get(Calendar.MONTH)
            }
            npMonth.value = currentMonth

            val onValueChange = NumberPicker.OnValueChangeListener { picker, oldVal, newVal ->
                updateDayRange.run()
            }

            npYear.setOnValueChangedListener(onValueChange)
            npMonth.setOnValueChangedListener(onValueChange)

            updateDayRange.run()

            val dialog = AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .create()

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            ivClose.setOnClickListener { dialog.dismiss() }

            btnCancel.setOnClickListener {
                textView.text = ""
                onDateSet?.run()
                dialog.dismiss()
            }

            btnOk.setOnClickListener {
                val year = npYear.value
                val month = npMonth.value
                val day = npDay.value

                calendar.set(year, month, day)

                if (calendar.timeInMillis < minCalendar.timeInMillis) {
                    calendar.time = minCalendar.time
                }
                if (calendar.timeInMillis > maxCalendar.timeInMillis) {
                    calendar.time = maxCalendar.time
                }

                val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                textView.text = displayFormat.format(calendar.time)

                onDateSet?.run()
                dialog.dismiss()
            }

            dialog.show()
        }

        @JvmStatic
        fun attachSearch(listView: ListView, adapter: CommonSpinnerAdapter<*>?, SearchHint: String) {
            try {
                val context = listView.context

                val existing = listView.getTag(R.id.tag_search_bar)
                if (existing is View) {
                    existing.visibility = VISIBLE
                    val existingParent = existing.parent as View?
                    if (existingParent is LinearLayout) {
                        existingParent.visibility = VISIBLE
                    }
                    return
                }

                val searchCardView = LayoutInflater.from(context).inflate(R.layout.search_layout_new, null)
                val etSearch = searchCardView.findViewById<TextInputEditText>(R.id.et_Search)
                if (etSearch == null) {
                    Log.e("attachSearch", "Could not find et_Search in layout")
                    return
                }
                if (SearchHint.isNotEmpty()) {
                    etSearch.hint = SearchHint
                } else {
                    etSearch.setHint(R.string.search)
                }
                searchCardView.elevation = 10f
                val finalAdapter = adapter
                val maxHeightPx = context.resources.getDimensionPixelSize(R.dimen.one_fifty_dp)

                etSearch.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, i: Int, i1: Int, i2: Int) {}

                    override fun onTextChanged(s: CharSequence?, i: Int, i1: Int, i2: Int) {
                        if (finalAdapter != null && s != null) {
                            try {
                                finalAdapter.filter.filter(s) { count ->
                                    listView.post { updateListViewHeight(listView, count, maxHeightPx) }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })

                val cardParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                cardParams.setMargins(dpToPx(context, 5), dpToPx(context, 5), dpToPx(context, 5), dpToPx(context, 5))
                searchCardView.layoutParams = cardParams

                val lvParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dpToPx(context, DynamicUtils.thirty)
                )
                listView.layoutParams = lvParams

                val wrapper = LinearLayout(context)
                wrapper.orientation = LinearLayout.VERTICAL
                wrapper.setBackgroundColor(Color.TRANSPARENT)

                val parent = listView.parent as ViewGroup?
                if (parent == null) {
                    Log.e("attachSearch", "ListView has no parent — cannot attach search bar")
                    return
                }

                val index = parent.indexOfChild(listView)

                val wrapperParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                parent.removeView(listView)
                wrapper.addView(searchCardView)
                wrapper.addView(listView)
                listView.visibility = VISIBLE
                parent.addView(wrapper, index, wrapperParams)

                listView.post {
                    val count = adapter?.count ?: 0
                    updateListViewHeight(listView, count, maxHeightPx)
                }

                listView.setTag(R.id.tag_search_bar, searchCardView)
                listView.setTag(R.id.tag_search_edittext, etSearch)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        private fun updateListViewHeight(listView: ListView, filteredCount: Int, maxHeightPx: Int) {
            if (filteredCount <= 0) {
                val p = listView.layoutParams
                p.height = 0
                listView.layoutParams = p
                return
            }

            if (filteredCount <= 4) {
                setDynamicHeight(listView)
            } else {
                val p = listView.layoutParams
                p.height = maxHeightPx
                listView.layoutParams = p
            }
        }

        @JvmStatic
        fun getCurrency_list(): ArrayList<String> {
            val currency = ArrayList<String>()
            currency.add("USDollar(USD)")
            currency.add("Euro(EUR)")
            currency.add("JapaneseYen(JPY)")
            currency.add("Pound(GBP)")
            currency.add("AustralianDollar(AUD)")
            currency.add("CanadianDOllar(CAD)")
            currency.add("SwissFranc(CHF)")
            currency.add("KuwaitiDinar(KWD)")
            currency.add("BahrainiDinar(BHD)")
            currency.add("IndianRupee(INR)")
            return currency
        }

        @JvmStatic
        fun closeSpinnerDropdown(
            listView: ListView?,
            tv_listview: TextView?,
            selectedValue: String?,
            img_dropdown: ImageView?,
            img_clear: ImageView?,
            adapter: CommonSpinnerAdapter<*>?
        ) {
            if (listView == null || tv_listview == null || img_dropdown == null || img_clear == null) return
            tv_listview.text = selectedValue ?: ""

            img_dropdown.visibility = View.GONE
            img_clear.visibility = VISIBLE

            listView.visibility = View.GONE

            val tag = listView.getTag(R.id.tag_search_bar)
            if (tag is EditText) {
                tag.visibility = View.GONE
                tag.setText("")
            }

            val parent = listView.parent
            if (parent is LinearLayout) {
                parent.visibility = View.GONE
            }

            adapter?.filter?.filter("")
        }

        @JvmStatic
        fun LoadList(rv_documents: View?, context: Context?, count_file: Int, islistview: Boolean) {
            if (rv_documents == null || context == null) return
            val params = rv_documents.layoutParams
            if (islistview) {
                if (rv_documents is ListView) {
                    if (count_file <= 4) {
                        setDynamicHeight(rv_documents)
                    } else {
                        params.height = context.resources.getDimensionPixelSize(R.dimen.one_fifty_dp)
                    }
                } else if (rv_documents is RecyclerView) {
                    if (count_file <= 2) {
                        setDynamicHeight(rv_documents)
                    } else {
                        params.height = context.resources.getDimensionPixelSize(R.dimen.one_fifty_dp)
                        rv_documents.layoutParams = params
                    }
                }
            } else {
                if (count_file == 1) {
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                } else {
                    params.height = context.resources.getDimensionPixelSize(R.dimen.one_fifty_dp)
                }
            }
            rv_documents.layoutParams = params
        }

        @JvmStatic
        fun setDynamicHeight(view: View?) {
            if (view == null) return
            if (view is ListView) {
                val listAdapter = view.adapter
                if (listAdapter == null || listAdapter.count == 0) {
                    val params = view.layoutParams
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    view.layoutParams = params
                    return
                }

                var totalHeight = 0
                val desiredWidth = View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)

                for (i in 0 until listAdapter.count) {
                    val listItem = listAdapter.getView(i, null, view)

                    if (listItem.layoutParams == null) {
                        listItem.layoutParams = AbsListView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }

                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
                    totalHeight += listItem.measuredHeight
                }

                val params = view.layoutParams
                params.height = totalHeight + (view.dividerHeight * (listAdapter.count - 1))
                view.layoutParams = params
                view.requestLayout()

            } else if (view is RecyclerView) {
                val adapter = view.adapter
                if (adapter == null || adapter.itemCount == 0) {
                    val params = view.layoutParams
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    view.layoutParams = params
                    return
                }

                view.viewTreeObserver.addOnGlobalLayoutListener(
                    object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                            val currentAdapter = view.adapter ?: return
                            if (currentAdapter.itemCount == 0) return

                            var totalHeight = 0
                            val desiredWidth = View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)

                            for (i in 0 until currentAdapter.itemCount) {
                                val vh = currentAdapter.createViewHolder(view, currentAdapter.getItemViewType(i))
                                currentAdapter.bindViewHolder(vh, i)

                                val itemView = vh.itemView
                                if (itemView.layoutParams == null) {
                                    itemView.layoutParams = RecyclerView.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                }
                                itemView.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
                                totalHeight += itemView.measuredHeight
                            }

                            val params = view.layoutParams
                            params.height = totalHeight
                            view.layoutParams = params
                            view.requestLayout()
                        }
                    }
                )
            }
        }

        @JvmStatic
        fun CapitalizeFirstLetter(str: String?): String? {
            if (str == null || str.isEmpty()) {
                return str
            }
            return str.substring(0, 1).uppercase(Locale.getDefault()) + str.substring(1)
        }

        @JvmStatic
        fun NumberFilterwithStar(editText: TextInputEditText?, isPhoneNumber: Boolean) {
            if (editText == null) return
            val filter = InputFilter { source, start, end, dest, dstart, dend ->
                for (i in start until end) {
                    val c = source[i]
                    if (isPhoneNumber) {
                        if (!Character.isDigit(c) && c != '+' && c != '*') {
                            return@InputFilter ""
                        }
                    } else {
                        if (!Character.isDigit(c) && c != '*') {
                            return@InputFilter ""
                        }
                    }
                }
                null
            }

            if (isPhoneNumber) {
                editText.filters = arrayOf(filter, InputFilter.LengthFilter(15))
            } else {
                editText.filters = arrayOf(filter)
            }
        }

        @JvmStatic
        fun NumberFilter(editText: TextInputEditText?, isPhoneNumber: Boolean) {
            if (editText == null) return
            val filter = InputFilter { source, start, end, dest, dstart, dend ->
                for (i in start until end) {
                    val c = source[i]
                    if (isPhoneNumber) {
                        if (!Character.isDigit(c) && c != '+') {
                            return@InputFilter ""
                        }
                    } else {
                        if (!Character.isDigit(c)) {
                            return@InputFilter ""
                        }
                    }
                }
                null
            }
            if (isPhoneNumber) {
                editText.filters = arrayOf(filter, InputFilter.LengthFilter(10))
            } else {
                editText.filters = arrayOf(filter)
            }
        }

        @JvmStatic
        fun ZipFilter(editText: TextInputEditText?) {
            if (editText == null) return
            val filter = InputFilter { source, start, end, dest, dstart, dend ->
                for (i in start until end) {
                    if (!Character.isDigit(source[i])) {
                        return@InputFilter ""
                    }
                }
                null
            }
            editText.filters = arrayOf(filter, InputFilter.LengthFilter(6))
        }

        @JvmStatic
        fun password(et_login_password: TextInputEditText?, iv_toggle_password: ImageView?) {
            if (et_login_password == null || iv_toggle_password == null) return
            val isVisible = booleanArrayOf(false)
            iv_toggle_password.setOnClickListener { v ->
                isVisible[0] = !isVisible[0]
                if (isVisible[0]) {
                    et_login_password.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    iv_toggle_password.setImageResource(R.drawable.eye_open)
                } else {
                    et_login_password.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                    iv_toggle_password.setImageResource(R.drawable.eye_close)
                }
                et_login_password.setSelection(if (et_login_password.text != null) et_login_password.text!!.length else 0)
            }
        }

        @JvmStatic
        fun localDateToDate(localDate: LocalDate): Date {
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        }

        @JvmStatic
        fun isValidEmail(email: String?): Boolean {
            return email != null && email.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[A-Za-z]{2,}"))
        }

        @JvmStatic
        fun adjustBottomMargin(context: Context, params: ViewGroup.MarginLayoutParams) {
            val bottomMargin: Int
            if (Build.VERSION.SDK_INT >= 35) {
                bottomMargin = if (hasNavigationBar(context)) {
                    getNavigationBarHeight(context)
                } else {
                    20
                }
                params.setMargins(0, 0, 0, bottomMargin)
            } else {
                params.setMargins(0, 0, 0, 0)
            }
        }

        @JvmStatic
        private fun hasNavigationBar(context: Context): Boolean {
            var hasNav = false
            val res = context.resources
            val resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android")
            if (resourceId > 0) {
                hasNav = res.getBoolean(resourceId)
            }
            if (ViewConfiguration.get(context).hasPermanentMenuKey()) {
                hasNav = false
            }
            return hasNav
        }

        @JvmStatic
        private fun getNavigationBarHeight(context: Context): Int {
            var result = 0
            val res = context.resources
            val resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0 && hasNavigationBar(context)) {
                result = res.getDimensionPixelSize(resourceId)
            }
            return result
        }

        @JvmStatic
        fun dpToPx(context: Context, dp: Int): Int {
            return (dp * context.resources.displayMetrics.density + 0.5f).toInt()
        }

        @JvmStatic
        fun dpToPx1(context: Context, dp: Int): Int {
            return (dp * context.resources.displayMetrics.density).toInt()
        }

        @JvmStatic
        fun loadProfileImage(context: Context?, imageUrl: String?, imageView: ImageView?, fallbackTextView: TextView?, name: String?) {
            if (context == null || imageView == null || fallbackTextView == null) return
            AppImageCache.load(context, imageUrl, imageView, name, fallbackTextView)
        }

        @JvmStatic
        fun loadProfileImage(context: Context?, imageUrl: String?, imageView: ImageView?, fallbackTextView: TextView?) {
            if (context == null || imageView == null || fallbackTextView == null) return
            if (Constants.ROLE == "AAM") {
                AppImageCache.load(context, imageUrl, imageView, Constants.FIRM_NAME, fallbackTextView)
            } else {
                AppImageCache.load(context, imageUrl, imageView, Constants.NAME, fallbackTextView)
            }
        }

        @JvmStatic
        fun setupStepper(
            editText: TextView,
            tvMinus: TextView,
            tvPlus: TextView,
            defaultValue: Int,
            minValue: Int,
            maxValue: Int,
            step: Int
        ) {
            editText.hint = "₹ $defaultValue"

            val getRaw = Supplier {
                val raw = if (editText.text != null) editText.text.toString().replace("₹", "").trim() else ""
                raw
            }

            val syncButtons = Runnable {
                val raw = getRaw.get()
                var valNum: Int
                if (TextUtils.isEmpty(raw)) {
                    valNum = defaultValue
                } else {
                    try {
                        valNum = raw.toInt()
                    } catch (e: NumberFormatException) {
                        valNum = defaultValue
                    }
                    if (valNum < minValue) {
                        valNum = minValue
                        editText.text = "₹ $minValue"
                    }
                    if (valNum > maxValue) {
                        valNum = maxValue
                        editText.text = "₹ $maxValue"
                    }
                }
                tvMinus.isEnabled = valNum > minValue
                tvMinus.alpha = if (valNum > minValue) 1f else 0.5f
                tvPlus.isEnabled = valNum < maxValue
                tvPlus.alpha = if (valNum < maxValue) 1f else 0.5f
            }

            syncButtons.run()

            tvMinus.setOnClickListener { v ->
                val raw = getRaw.get()
                var valNum: Int
                try {
                    valNum = if (TextUtils.isEmpty(raw)) defaultValue else raw.toInt()
                } catch (e: NumberFormatException) {
                    valNum = defaultValue
                }
                editText.text = "₹ " + Math.max(minValue, valNum - step)
                syncButtons.run()
            }

            tvPlus.setOnClickListener { v ->
                val raw = getRaw.get()
                val valNum: Int
                try {
                    if (TextUtils.isEmpty(raw)) {
                        editText.text = "₹ $minValue"
                        syncButtons.run()
                        return@setOnClickListener
                    }
                    valNum = raw.toInt()
                } catch (e: NumberFormatException) {
                    return@setOnClickListener
                }
                editText.text = "₹ " + Math.min(maxValue, valNum + step)
                syncButtons.run()
            }

            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, i: Int, i1: Int, i2: Int) {}
                override fun onTextChanged(s: CharSequence?, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s == null) return
                    val current = s.toString()
                    if (current.isEmpty() || current.startsWith("₹")) {
                        syncButtons.run()
                        return
                    }
                    val digits = current.replace("₹", "").trim()
                    if (digits.isNotEmpty()) {
                        editText.removeTextChangedListener(this)
                        editText.text = "₹ $digits"
                        editText.addTextChangedListener(this)
                    }
                    syncButtons.run()
                }
            })
        }
    }
}

