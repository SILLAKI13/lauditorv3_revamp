package com.digicoffer.lauditor

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Appointments.ViewModels.Appointments
import com.digicoffer.lauditor.AuditTrails.AuditTrails
import com.digicoffer.lauditor.Chat.ViewModels.Chat
import com.digicoffer.lauditor.Chat.ViewModels.MessagesList
import com.digicoffer.lauditor.CommonFiles.CacheUtils.AppImageCache
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnection
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnectionService
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.KPICARDS
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.MYDAYCARDS
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.CommonFiles.PushNotifications.MyFirebaseMessagingService
import com.digicoffer.lauditor.CommonFiles.TermsAndCondition.TermsAndCondition
import com.digicoffer.lauditor.Dashboard.DahboardModels.MenuModels
import com.digicoffer.lauditor.Dashboard.NewRevampViewModels.Dashboard
import com.digicoffer.lauditor.Dashboard.NewRevampViewModels.DashboardItem
import com.digicoffer.lauditor.DocEditor.DocEditor
import com.digicoffer.lauditor.Documents.ViewModel.DocumentsEn
import com.digicoffer.lauditor.Email.Email
import com.digicoffer.lauditor.FirmProfile.FirmProfile
import com.digicoffer.lauditor.Groups.Groups
import com.digicoffer.lauditor.Invoice.ViewModels.ViewInvoice
import com.digicoffer.lauditor.LoginActivity.Models.Dashboard_Model
import com.digicoffer.lauditor.LoginActivity.Models.FirmsDo
import com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity
import com.digicoffer.lauditor.Matter.ViewModels.Matter
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO
import com.digicoffer.lauditor.Meetings.ViewModels.Meetings
import com.digicoffer.lauditor.Meetings.ViewModels.MonthlyCalendar
import com.digicoffer.lauditor.Meetings.ViewModels.WeeklyCalendar
import com.digicoffer.lauditor.Members.Members
import com.digicoffer.lauditor.Notifications.Models.Navigation
import com.digicoffer.lauditor.Notifications.Notifications
import com.digicoffer.lauditor.Relationships.ClientRelationship
import com.digicoffer.lauditor.TimeSheets.ViewModels.TimeSheets
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity(), Dashboard.MenuHighlightListener, MonthlyCalendar.EventDetailsListener,
    WeeklyCalendar.EventDetailsListener, AsyncTaskCompleteListener, View.OnClickListener {

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val AUTH_REQUEST_CODE = 1001
        private const val REQ_ONE_TAP = 2
        
        @JvmStatic
        var chatConnectionService: ChatConnectionService? = null
    }

    private var isFirmSwitchInProgress = false
    private var pendingColdStartNavJson: String? = null

    private var COLOR_EXPANDED_BG = 0
    private var COLOR_NORMAL_BG = 0
    private var COLOR_ACTIVE_TEXT = 0
    private var COLOR_ACTIVE_ICON = 0
    private var COLOR_NORMAL_TEXT = 0
    private var COLOR_NORMAL_ICON = 0

    private var currentActiveParentView: View? = null

    // FAB
    var mAddFab: ExtendedFloatingActionButton? = null
    var isAllFabsVisible: Boolean? = null
    lateinit var menu_open: ImageView
    lateinit var center_menu: FloatingActionMenu
    lateinit var actionButton: com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton

    // AppBar
    lateinit var appbar: AppBarLayout
    var header_layout: AppBarLayout? = null
    lateinit var iv_logo_dashboard: ImageView
    lateinit var reminders: ImageView
    lateinit var digi_logo_layout: LinearLayout
    lateinit var tv_pageName: TextView
    var tv_headerName: TextView? = null
    var tv_digilogo: TextView? = null
    var tv_header_firm_name: TextView? = null

    // Drawer
    lateinit var dLayout: DrawerLayout
    var navigationDrawer: DrawerLayout? = null
    var dtoggle: ActionBarDrawerToggle? = null
    lateinit var navView: NavigationView
    lateinit var nav_linear_layout: LinearLayout
    lateinit var iv_Drawer: ImageView
    lateinit var iv_profile: ImageView

    // Profile
    lateinit var person_icon: TextView
    lateinit var ll_notify: FrameLayout

    // Side-menu root views
    lateinit var sm_firmProfile: View
    lateinit var sm_appointments: View
    lateinit var sm_matter: View
    lateinit var sm_documents: View
    lateinit var sm_docEditor: View
    lateinit var sm_relationships: View
    lateinit var sm_timesheet: View
    lateinit var sm_meetings: View
    lateinit var sm_email: View
    lateinit var sm_messages: View
    lateinit var sm_notification: View
    lateinit var sm_audit: View
    lateinit var sm_groups: View
    lateinit var sm_team_member: View
    lateinit var sm_invoice: View
    lateinit var sm_logout: View

    private var currentActiveSubItem: TextView? = null

    // Bottom FAB buttons
    lateinit var matters_bm: ImageView
    lateinit var timesheets_bm: ImageView
    lateinit var relationships_bm: ImageView
    lateinit var groups_bm: ImageView
    lateinit var team_members_bm: ImageView
    lateinit var audit_bm: ImageView
    lateinit var more_bm: ImageView
    lateinit var firm_profile_bm: ImageView
    lateinit var documents_bm: ImageView

    // Legacy
    var nav_Menu: Menu? = null
    var team_member_sm: MenuItem? = null
    var audits_sm: MenuItem? = null
    var meetings_sm: MenuItem? = null
    var messages_sm: MenuItem? = null
    var notifications_sm: MenuItem? = null
    var logout_sm: MenuItem? = null
    var email_sm: MenuItem? = null

    private var progress_dialog: Dialog? = null
    private var progressDialog: Dialog? = null
    var token_id: String? = null
    var itemId = 0
    private var showOneTapUI = true
    private var isAuthInProgress = false
    var gso: GoogleSignInOptions? = null
    var email: Email? = null
    var dashboardModels = ArrayList<Dashboard_Model>()
    var menuList = ArrayList<MenuModels>()
    var recyclerView: RecyclerView? = null
    private var viewModel: NewModel? = null
    private var mConnection: ChatConnection? = null
    private var loginActivity: LoginActivity? = null
    var ll_bottom_menu: androidx.appcompat.widget.LinearLayoutCompat? = null

    private val notifPermissionLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) AndroidUtils.showToast("Enable notifications to receive updates", this@MainActivity)
        fetchAndStoreFCMToken()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Constants.is_active = true
        val wasColdStart = Constants.TOKEN.isNullOrEmpty()
        
        if (wasColdStart) {
            val restored = Constants.restoreSessionFromPrefs(this)
            if (!restored) {
                var navExtra = intent?.getStringExtra("fcm_navigation")
                if (navExtra == null) navExtra = intent?.getStringExtra("navigation")
                
                val loginIntent = Intent(this, LoginActivity::class.java)
                if (!navExtra.isNullOrEmpty()) loginIntent.putExtra("fcm_navigation", navExtra)
                startActivity(loginIntent)
                finish()
                return
            }
        }
        
        Constants.check_url()
        Constants.base_URL = Constants.PROF_URL
        Constants.PROBIZ_TYPE = "PROFESSIONAL"
        
        var coldStartNav = intent?.getStringExtra("fcm_navigation")
        if (coldStartNav == null) coldStartNav = intent?.getStringExtra("navigation")
        
        if (!coldStartNav.isNullOrEmpty()) {
            if (wasColdStart) {
                pendingColdStartNavJson = coldStartNav
                validateSessionBeforeNavigation()
            } else {
                Constants.pendingFcmNavigation = coldStartNav
            }
        }
        
        setContentView(R.layout.activity_main)
        
        window.statusBarColor = ContextCompat.getColor(this, R.color.Blue_text_color)
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true
            
        COLOR_EXPANDED_BG = ContextCompat.getColor(this, R.color.lite_shaded_blue)
        COLOR_NORMAL_BG = ContextCompat.getColor(this, R.color.lite_grey)
        COLOR_ACTIVE_TEXT = ContextCompat.getColor(this, R.color.blue)
        COLOR_ACTIVE_ICON = ContextCompat.getColor(this, R.color.blue)
        COLOR_NORMAL_TEXT = ContextCompat.getColor(this, R.color.lite_grey)
        COLOR_NORMAL_ICON = ContextCompat.getColor(this, R.color.lite_grey)
        
        requestNotificationPermission()
        supportActionBar?.hide()
        
        supportFragmentManager.addOnBackStackChangedListener {
            val f = supportFragmentManager.findFragmentById(R.id.id_framelayout)
            if (f != null) {
                if (f is Dashboard) {
                    handleDashboardState()
                } else {
                    checkMenuByFragment(f)
                    ensureSubMenuExpansionForFragment(f)
                }
            }
        }
        
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                PreferenceManager.getDefaultSharedPreferences(applicationContext)
                    .edit().remove("current_fragment").apply()
                appbar.visibility = View.VISIBLE
                if (dLayout.isDrawerOpen(GravityCompat.START)) {
                    dLayout.closeDrawer(GravityCompat.START)
                    return
                }
                val f = supportFragmentManager.findFragmentById(R.id.id_framelayout)
                if (f is Dashboard) {
                    handleDashboardState()
                    return
                }
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        })
        
        Constants.mainActivity = this
        Constants.isClient_chat = true
        
        dLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.navigation)
        nav_linear_layout = findViewById(R.id.nav_linear_layout)
        
        sm_firmProfile = findViewById(R.id.sm_firmProfile)
        sm_appointments = findViewById(R.id.sm_appointments)
        sm_matter = findViewById(R.id.sm_matters)
        sm_documents = findViewById(R.id.sm_documents)
        sm_docEditor = findViewById(R.id.sm_docEditor)
        sm_relationships = findViewById(R.id.sm_relationships)
        sm_timesheet = findViewById(R.id.sm_timesheet)
        sm_groups = findViewById(R.id.sm_groups)
        sm_team_member = findViewById(R.id.sm_team_member)
        sm_meetings = findViewById(R.id.sm_meetings)
        sm_messages = findViewById(R.id.sm_messages)
        sm_notification = findViewById(R.id.sm_notification)
        sm_invoice = findViewById(R.id.sm_invoice)
        sm_email = findViewById(R.id.sm_email)
        sm_audit = findViewById(R.id.sm_audit)
        sm_logout = findViewById(R.id.sm_logout)
        
        val isSolo = "solo" == Constants.CATEGORY
        val isGHorTM = "GH" == Constants.ROLE || "TM" == Constants.ROLE
        
        if ("AAM" == Constants.ROLE) {
            setRowClick(sm_firmProfile) {
                Constants.isMyProfileClicked = false
                collapseAllSubMenus()
                setActiveParent(sm_firmProfile)
                doNavigate(FirmProfile())
            }
        } else if (isSolo || isGHorTM) {
            setRowClick(sm_firmProfile) {
                Constants.isMyProfileClicked = true
                collapseAllSubMenus()
                setActiveParent(sm_firmProfile)
                doNavigate(FirmProfile())
            }
        } else {
            setRowClick(sm_firmProfile) { toggleSubMenu(sm_firmProfile) }
        }
        
        setRowClick(sm_appointments) {
            collapseAllSubMenus()
            setActiveParent(sm_appointments)
            doNavigate(Appointments())
        }
        setRowClick(sm_matter) { toggleSubMenu(sm_matter) }
        setRowClick(sm_documents) { toggleSubMenu(sm_documents) }
        setRowClick(sm_docEditor) {
            collapseAllSubMenus()
            setActiveParent(sm_docEditor)
            doNavigate(DocEditor())
        }
        setRowClick(sm_relationships) { toggleSubMenu(sm_relationships) }
        
        if (isSolo) {
            setRowClick(sm_timesheet) {
                collapseAllSubMenus()
                setActiveParent(sm_timesheet)
                Constants.Timesheet_Card = "Myts"
                Constants.ts_card_clicked = false
                Constants.is_ts_submitted = false
                doNavigate(TimeSheets())
            }
        } else {
            setRowClick(sm_timesheet) { toggleSubMenu(sm_timesheet) }
        }
        
        setRowClick(sm_meetings) {
            collapseAllSubMenus()
            setActiveParent(sm_meetings)
            doNavigate(Meetings())
        }
        setRowClick(sm_email) {
            collapseAllSubMenus()
            setActiveParent(sm_email)
            doNavigate(Email())
            tv_pageName.textSize = DynamicUtils.eighteen.toFloat()
            tv_pageName.setText(R.string.emails)
        }
        
        if (Constants.ROLE == "AAM") {
            setRowClick(sm_messages) {
                collapseAllSubMenus()
                setActiveParent(sm_messages)
                Constants.isClient_chat = false
                doNavigate(Chat())
            }
        } else if (isSolo) {
            setRowClick(sm_messages) {
                collapseAllSubMenus()
                setActiveParent(sm_messages)
                Constants.isClient_chat = true
                doNavigate(Chat())
            }
        } else {
            setRowClick(sm_messages) { toggleSubMenu(sm_messages) }
        }
        
        setRowClick(sm_notification) {
            collapseAllSubMenus()
            setActiveParent(sm_notification)
            doNavigate(Notifications())
        }
        setRowClick(sm_audit) {
            collapseAllSubMenus()
            setActiveParent(sm_audit)
            doNavigate(AuditTrails())
        }
        setRowClick(sm_groups) {
            collapseAllSubMenus()
            setActiveParent(sm_groups)
            doNavigate(Groups())
        }
        setRowClick(sm_team_member) {
            collapseAllSubMenus()
            setActiveParent(sm_team_member)
            doNavigate(Members())
        }
        setRowClick(sm_invoice) {
            collapseAllSubMenus()
            setActiveParent(sm_invoice)
            doNavigate(ViewInvoice())
        }
        setRowClick(sm_logout) { performLogout() }
        
        val firmProfileTitle: String
        val hasSubMenu: Boolean
        if ("AAM" == Constants.ROLE) {
            Constants.isMyProfileClicked = false
            firmProfileTitle = getString(R.string.firm_profile)
            hasSubMenu = false
        } else if (isSolo || isGHorTM) {
            Constants.isMyProfileClicked = true
            firmProfileTitle = "My Profile"
            hasSubMenu = false
        } else {
            Constants.isMyProfileClicked = true
            firmProfileTitle = getString(R.string.profile)
            hasSubMenu = true
        }
        val hasMessagesSubMenu = "AAM" != Constants.ROLE && !isSolo
        setupMenuRow(sm_firmProfile, firmProfileTitle, R.drawable.profile, hasSubMenu)
        setupMenuRow(sm_appointments, getString(R.string.appointments), R.drawable.appointments, false)
        setupMenuRow(sm_matter, getString(R.string.matters), R.drawable.matters, true)
        setupMenuRow(sm_documents, getString(R.string.documents), R.drawable.document_icon, true)
        setupMenuRow(sm_docEditor, getString(R.string.doc_editor), R.drawable.doceditor_icon, false)
        setupMenuRow(sm_relationships, getString(R.string.relationships), R.drawable.relationship, true)
        setupMenuRow(sm_timesheet, getString(R.string.timesheets), R.drawable.timesheet, !isSolo)
        setupMenuRow(sm_meetings, getString(R.string.meetings), R.drawable.meetings, false)
        setupMenuRow(sm_email, getString(R.string.emails), R.drawable.email, false)
        setupMenuRow(sm_messages, getString(R.string.messages), R.drawable.messages, hasMessagesSubMenu)
        setupMenuRow(sm_notification, getString(R.string.notifications), R.drawable.notifications, false)
        setupMenuRow(sm_audit, getString(R.string.audit_trails), R.drawable.audit_trails, false)
        setupMenuRow(sm_groups, getString(R.string.groups), R.drawable.groups, false)
        setupMenuRow(sm_team_member, getString(R.string.members), R.drawable.members, false)
        setupMenuRow(sm_invoice, "Invoices", R.drawable.invoices, false)
        setupMenuRow(sm_logout, getString(R.string.logout), R.drawable.logout, false)
        
        if ("SU" == Constants.ROLE && !isSolo) {
            Constants.isMyProfileClicked = true
            addSubItem(sm_firmProfile, "Firm Profile") { v ->
                Constants.Profile_View = "Bp"
                Constants.isMyProfileClicked = false
                setActiveSubItem(v as TextView)
                navigateFromSub(sm_firmProfile, FirmProfile())
            }
            addSubItem(sm_firmProfile, "My Profile") { v ->
                Constants.Profile_View = "Bp"
                Constants.isMyProfileClicked = true
                setActiveSubItem(v as TextView)
                navigateFromSub(sm_firmProfile, FirmProfile())
            }
            addSubItem(sm_firmProfile, "Practice Partner") { v ->
                Constants.Profile_View = "Pp"
                setActiveSubItem(v as TextView)
                navigateFromSub(sm_firmProfile, FirmProfile())
            }
        }
        
        addSubItem(sm_matter, "Legal Matters") { v ->
            setActiveSubItem(v as TextView)
            Constants.Matter_CreateOrViewDetails = "View"
            Constants.MATTER_TYPE = "Legal"
            Constants.is_CreateMatter = false
            Constants.create_matter = false
            navigateFromSub(sm_matter, Matter())
        }
        addSubItem(sm_matter, "General Matters") { v ->
            setActiveSubItem(v as TextView)
            Constants.Matter_CreateOrViewDetails = "View"
            Constants.MATTER_TYPE = "General"
            Constants.is_CreateMatter = false
            Constants.create_matter = false
            navigateFromSub(sm_matter, Matter())
        }
        
        addSubItem(sm_documents, "Matter") { v ->
            setActiveSubItem(v as TextView)
            navigateFromSub(sm_documents, newDocumentFragment("matter"))
        }
        addSubItem(sm_documents, "Client") { v ->
            setActiveSubItem(v as TextView)
            navigateFromSub(sm_documents, newDocumentFragment("client"))
        }
        if (!isSolo) {
            addSubItem(sm_documents, "Firm") { v ->
                setActiveSubItem(v as TextView)
                navigateFromSub(sm_documents, newDocumentFragment("firm"))
            }
        }
        if (Constants.ROLE == "SU") {
            addSubItem(sm_documents, "Deleted") { v ->
                setActiveSubItem(v as TextView)
                navigateFromSub(sm_documents, newDocumentFragment("delete"))
            }
        }
        
        addSubItem(sm_relationships, "Individual") { v ->
            setActiveSubItem(v as TextView)
            Constants.Rel_Type = "Individual"
            navigateFromSub(sm_relationships, ClientRelationship())
        }
        addSubItem(sm_relationships, "Business") { v ->
            setActiveSubItem(v as TextView)
            Constants.Rel_Type = "Entity"
            navigateFromSub(sm_relationships, ClientRelationship())
        }
        addSubItem(sm_relationships, "Corporate") { v ->
            setActiveSubItem(v as TextView)
            Constants.Rel_Type = "Corporate"
            navigateFromSub(sm_relationships, ClientRelationship())
        }
        if (Constants.ROLE == "SU") {
            addSubItem(sm_relationships, "Deleted") { v ->
                setActiveSubItem(v as TextView)
                Constants.Rel_Type = "Deleted"
                navigateFromSub(sm_relationships, ClientRelationship())
            }
        }
        
        if (!isSolo) {
            addSubItem(sm_timesheet, "Aggregated Timesheet") { v ->
                setActiveSubItem(v as TextView)
                Constants.Timesheet_Card = "Agts"
                Constants.ts_card_clicked = true
                Constants.is_ts_submitted = false
                navigateFromSub(sm_timesheet, TimeSheets())
            }
            addSubItem(sm_timesheet, "My Timesheet") { v ->
                setActiveSubItem(v as TextView)
                Constants.Timesheet_Card = "Myts"
                Constants.ts_card_clicked = false
                Constants.is_ts_submitted = false
                navigateFromSub(sm_timesheet, TimeSheets())
            }
        }
        
        if (!isSolo) {
            val role = Constants.ROLE
            if ("AAM" != role) {
                addSubItem(sm_messages, "Clients") { v ->
                    setActiveSubItem(v as TextView)
                    Constants.isClient_chat = true
                    navigateFromSub(sm_messages, Chat())
                }
            }
            addSubItem(sm_messages, "Teams") { v ->
                setActiveSubItem(v as TextView)
                Constants.isClient_chat = false
                navigateFromSub(sm_messages, Chat())
            }
        }
        
        getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit()
            .putString("firmNames", JSONArray(Constants.Firm_names).toString())
            .putBoolean("requiresTermsAcceptance", Constants.requiresTermsAcceptance)
            .putString("termsVersion", Constants.termsVersion)
            .putString("firmIds", JSONArray(Constants.Firm_ids).toString())
            .apply()
            
        val deviceSize = DynamicUtils.isNormalPhone(this)
        menu_open = ImageView(this)
        menu_open.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.menu_icon_img))
        
        val menuWidth: Int
        val menuHeight: Int
        val paddingTB: Int
        val subBtnSize: Int
        when (deviceSize) {
            "Small" -> {
                menuWidth = 190
                menuHeight = 90
                paddingTB = -10
                subBtnSize = 100
            }
            "Medium" -> {
                menuWidth = 230
                menuHeight = 120
                paddingTB = -10
                subBtnSize = 120
            }
            else -> {
                menuWidth = 310
                menuHeight = 150
                paddingTB = -20
                subBtnSize = 180
            }
        }
        menu_open.setPadding(0, paddingTB, 0, paddingTB)
        val fabLp = LinearLayout.LayoutParams(menuWidth, menuHeight)
        AndroidUtils.adjustBottomMargin(applicationContext, fabLp)
        actionButton = com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton.Builder(this)
            .setContentView(menu_open)
            .setBackgroundDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.menu_desing))
            .setLayoutParams(com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton.LayoutParams(fabLp))
            .setPosition(com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton.POSITION_BOTTOM_CENTER)
            .build()
            
        matters_bm = ImageView(this)
        timesheets_bm = ImageView(this)
        documents_bm = ImageView(this)
        relationships_bm = ImageView(this)
        groups_bm = ImageView(this)
        team_members_bm = ImageView(this)
        firm_profile_bm = ImageView(this)
        audit_bm = ImageView(this)
        more_bm = ImageView(this)
        
        val cp = if (deviceSize == "Small") 8 else if (deviceSize == "Medium") 12 else 20
        for (iv in arrayOf(matters_bm, timesheets_bm, documents_bm, relationships_bm, groups_bm, firm_profile_bm, more_bm, team_members_bm)) {
            iv.setPadding(cp, cp, cp, cp)
        }
        
        hide_un_chosen_menu()
        
        val rLSub = SubActionButton.Builder(this)
        val subParams = FrameLayout.LayoutParams(subBtnSize, subBtnSize)
        subParams.setMargins(0, 0, 0, dpToPx(this, 60))
        
        when (Constants.ROLE) {
            "SU", "GH" -> {
                center_menu = FloatingActionMenu.Builder(this)
                    .addSubActionView(rLSub.setContentView(matters_bm).setLayoutParams(subParams).build())
                    .addSubActionView(rLSub.setContentView(timesheets_bm).build())
                    .addSubActionView(rLSub.setContentView(documents_bm).build())
                    .addSubActionView(rLSub.setContentView(relationships_bm).build())
                    .addSubActionView(rLSub.setContentView(groups_bm).build())
                    .attachTo(actionButton).setStartAngle(200).setEndAngle(340).build()
            }
            "AAM" -> {
                center_menu = FloatingActionMenu.Builder(this)
                    .addSubActionView(rLSub.setContentView(groups_bm).setLayoutParams(subParams).build())
                    .addSubActionView(rLSub.setContentView(team_members_bm).build())
                    .addSubActionView(rLSub.setContentView(audit_bm).build())
                    .addSubActionView(rLSub.setContentView(firm_profile_bm).build())
                    .attachTo(actionButton).setStartAngle(200).setEndAngle(340).build()
            }
            else -> {
                center_menu = FloatingActionMenu.Builder(this)
                    .addSubActionView(rLSub.setContentView(matters_bm).setLayoutParams(subParams).build())
                    .addSubActionView(rLSub.setContentView(timesheets_bm).build())
                    .addSubActionView(rLSub.setContentView(documents_bm).build())
                    .addSubActionView(rLSub.setContentView(relationships_bm).build())
                    .addSubActionView(rLSub.setContentView(more_bm).build())
                    .attachTo(actionButton).setStartAngle(200).setEndAngle(340).build()
            }
        }
        
        center_menu.setStateChangeListener(object : FloatingActionMenu.MenuStateChangeListener {
            override fun onMenuOpened(m: FloatingActionMenu) {
                dLayout.close()
                menu_open.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.menu_down_icon))
            }
            override fun onMenuClosed(m: FloatingActionMenu) {
                menu_open.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.menu_icon_img))
            }
        })
        
        matters_bm.setOnClickListener {
            Constants.Matter_CreateOrViewDetails = "View"
            Constants.MATTER_TYPE = "Legal"
            Constants.is_CreateMatter = false
            Constants.create_matter = false
            fabMenu()
            setActiveParent(sm_matter)
            doNavigate(Matter())
            hide_un_chosen_menu()
            matters_bm.setImageDrawable(getDrawable(R.drawable.matter_white))
            matters_bm.background = getDrawable(R.drawable.circular_button_background)
        }
        timesheets_bm.setOnClickListener {
            Constants.ts_card_clicked = false
            fabMenu()
            setActiveParent(sm_timesheet)
            doNavigate(TimeSheets())
            hide_un_chosen_menu()
            timesheets_bm.background = getDrawable(R.drawable.circular_button_background)
            timesheets_bm.setImageDrawable(getDrawable(R.drawable.timesheets_white))
        }
        documents_bm.setOnClickListener {
            fabMenu()
            setActiveParent(sm_documents)
            doNavigate(DocumentsEn())
            hide_un_chosen_menu()
            documents_bm.background = getDrawable(R.drawable.circular_button_background)
            documents_bm.setImageDrawable(getDrawable(R.drawable.documents_white))
        }
        relationships_bm.setOnClickListener {
            Constants.Rel_Type = "Individual"
            fabMenu()
            setActiveParent(sm_relationships)
            doNavigate(ClientRelationship())
            hide_un_chosen_menu()
            relationships_bm.background = getDrawable(R.drawable.circular_button_background)
            relationships_bm.setImageDrawable(getDrawable(R.drawable.relationship_white))
        }
        groups_bm.setOnClickListener {
            fabMenu()
            setActiveParent(sm_groups)
            doNavigate(Groups())
            hide_un_chosen_menu()
            groups_bm.background = getDrawable(R.drawable.circular_button_background)
            groups_bm.setImageDrawable(getDrawable(R.drawable.groups_white))
        }
        team_members_bm.setOnClickListener {
            fabMenu()
            setActiveParent(sm_team_member)
            doNavigate(Members())
            hide_un_chosen_menu()
            team_members_bm.background = getDrawable(R.drawable.circular_button_background)
            team_members_bm.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.team_member_white))
        }
        firm_profile_bm.setOnClickListener {
            fabMenu()
            setActiveParent(sm_firmProfile)
            doNavigate(FirmProfile())
            hide_un_chosen_menu()
            firm_profile_bm.background = getDrawable(R.drawable.circular_button_background)
            firm_profile_bm.setImageDrawable(getDrawable(R.drawable.firm_profile_white))
            center_menu.close(true)
        }
        audit_bm.setOnClickListener {
            fabMenu()
            setActiveParent(sm_audit)
            doNavigate(AuditTrails())
            hide_un_chosen_menu()
            audit_bm.background = getDrawable(R.drawable.circular_button_background)
            audit_bm.setImageDrawable(getDrawable(R.drawable.audit_white))
        }
        more_bm.setOnClickListener {
            dLayout.openDrawer(GravityCompat.START)
            nav_linear_layout.visibility = View.VISIBLE
            center_menu.close(true)
            hide_un_chosen_menu()
            more_bm.background = getDrawable(R.drawable.circular_button_background)
            more_bm.setImageDrawable(getDrawable(R.drawable.more_white))
        }
        
        dLayout.setOnDragListener { _, _ ->
            nav_linear_layout.visibility = View.VISIBLE
            setMenuList()
            false
        }
        
        try {
            tv_pageName = findViewById(R.id.page_name)
            iv_logo_dashboard = findViewById(R.id.logo_dashboard)
            digi_logo_layout = findViewById(R.id.digi_logo_layout)
            reminders = findViewById(R.id.reminders)
            appbar = findViewById(R.id.appbar)
            appbar.visibility = View.VISIBLE
            
            ll_notify = appbar.findViewById(R.id.ll_notify)
            ll_notify.setOnClickListener {
                setActiveParent(sm_notification)
                doNavigate(Notifications())
            }
            
            Constants.notifyBadge = ll_notify.findViewById(R.id.notify_badge)
            
            viewModel = ViewModelProvider(this).get(NewModel::class.java)
            viewModel?.getselectedItem()?.observe(this) { item: String ->
                if (item == getString(R.string.lauditor)) {
                    tv_pageName.textSize = DynamicUtils.twentyFive.toFloat()
                    tv_pageName.setText(R.string.appname)
                    iv_logo_dashboard.setImageDrawable(getDrawable(R.drawable.loading_animation_new))
                } else {
                    tv_pageName.text = item
                    tv_pageName.textSize = DynamicUtils.eighteen.toFloat()
                    iv_logo_dashboard.setImageDrawable(getDrawable(R.drawable.lawyer_logo_nw))
                }
            }
            
            person_icon = findViewById(R.id.person_icon)
            iv_profile = findViewById(R.id.iv_profile)
            person_icon.visibility = View.VISIBLE
            iv_profile.visibility = View.GONE
            
            val n = Constants.NAME
            person_icon.text = if (!n.isNullOrEmpty()) n.substring(0, 1) else "?"
            
            profile()
            setMenuList()
            
            iv_Drawer = findViewById(R.id.menu)
            actionButton.visibility = View.GONE
            iv_Drawer.setOnClickListener {
                if (!dLayout.isDrawerOpen(GravityCompat.START)) {
                    dLayout.openDrawer(GravityCompat.START)
                    center_menu.close(true)
                    nav_linear_layout.visibility = View.VISIBLE
                } else {
                    dLayout.close()
                }
            }
            
            reminders.setOnClickListener { openEmailComposer() }
            person_icon.setOnClickListener {
                dLayout.close()
                showProfilePopup(person_icon)
            }
            iv_profile.setOnClickListener {
                dLayout.close()
                showProfilePopup(person_icon)
            }
            digi_logo_layout.setOnClickListener { navigateTocallDashboard() }
            iv_logo_dashboard.setOnClickListener { navigateTocallDashboard() }
            
            val dashFrag: Fragment = com.digicoffer.lauditor.Dashboard.NewRevampViewModels.Dashboard()
            WindowCompat.setDecorFitsSystemWindows(window, true)
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.id_framelayout, dashFrag)
            ft.commit()
            isAllFabsVisible = false
            checkTermsAndConditions()
            
            val launchIntent = intent
            if (launchIntent != null) {
                var pendingNav = launchIntent.getStringExtra("fcm_navigation")
                if (pendingNav.isNullOrEmpty()) pendingNav = launchIntent.getStringExtra("navigation")
                if (!pendingNav.isNullOrEmpty()) {
                    if (!(wasColdStart && pendingColdStartNavJson != null)) {
                        Constants.pendingFcmNavigation = pendingNav
                    }
                    launchIntent.removeExtra("fcm_navigation")
                    launchIntent.removeExtra("navigation")
                }
            }
            
        } catch (e: Resources.NotFoundException) {
            e.fillInStackTrace()
        }
    }
    
    private fun openEmailComposer() {
        val supportEmail = "support@lexiz.ai"
        val subject = "Support Request"
        val mailUri = Uri.parse("mailto:$supportEmail?subject=${Uri.encode(subject)}")
        val intent = Intent(Intent.ACTION_SENDTO, mailUri)
        try {
            startActivity(Intent.createChooser(intent, "Send email"))
        } catch (e: android.content.ActivityNotFoundException) {
            AlertDialog.Builder(this)
                .setTitle("No Email App Found")
                .setMessage("Please email us at: $supportEmail")
                .setPositiveButton("Copy Email") { _, _ ->
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("email", supportEmail)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "Email copied to clipboard", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("OK", null)
                .show()
        }
    }

    private fun newDocumentFragment(type: String): Fragment {
        val frag = DocumentsEn()
        val args = Bundle()
        args.putString("document_type", type)
        frag.arguments = args
        return frag
    }

    private fun setRowClick(parentView: View, listener: View.OnClickListener) {
        val row = parentView.findViewById<View>(R.id.menu_row)
        if (row != null) row.setOnClickListener(listener) else parentView.setOnClickListener(listener)
    }

    private fun setupMenuRow(parentView: View, title: String, iconResId: Int, hasSubMenu: Boolean) {
        val tvTitle = parentView.findViewById<TextView>(R.id.title)
        val icon = parentView.findViewById<ImageView>(R.id.icon)
        val arrow = parentView.findViewById<ImageView>(R.id.iv_arrow)
        tvTitle?.text = title
        if (icon != null) {
            val d = getDrawable(iconResId)
            if (d != null) {
                d.setTint(COLOR_NORMAL_ICON)
                icon.setImageDrawable(d)
            }
        }
        arrow?.visibility = if (hasSubMenu) View.VISIBLE else View.GONE
    }

    private fun addSubItem(parentView: View, label: String, listener: View.OnClickListener) {
        val container = parentView.findViewById<LinearLayout>(R.id.submenu_container)
        if (container == null) {
            Log.e("SUBMENU", "container null: $label")
            return
        }
        val subItem = LayoutInflater.from(this).inflate(R.layout.sub_menu_item, container, false) as TextView
        subItem.text = label
        subItem.background = null
        subItem.setTextColor(COLOR_NORMAL_TEXT)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(dpToPx(this, 4), dpToPx(this, 4), dpToPx(this, 4), dpToPx(this, 3))
        subItem.layoutParams = params
        subItem.setPadding(
            dpToPx(this, DynamicUtils.sixtySeven), dpToPx(this, 10),
            dpToPx(this, 8), dpToPx(this, 10)
        )
        subItem.setOnClickListener(listener)
        container.addView(subItem)
    }

    private fun applySubItemHighlight(subItem: TextView?) {
        if (subItem == null) return
        val pill = GradientDrawable()
        pill.setColor(COLOR_EXPANDED_BG)
        pill.cornerRadius = dpToPx(this, 8).toFloat()
        subItem.background = pill
        subItem.setTextColor(COLOR_ACTIVE_TEXT)
    }

    private fun removeSubItemHighlight(subItem: TextView?) {
        if (subItem == null) return
        subItem.background = null
        subItem.setTextColor(COLOR_NORMAL_TEXT)
    }

    private fun setRowHighlight(menuRow: View?, active: Boolean) {
        if (menuRow == null) return
        if (active) {
            val bg = GradientDrawable()
            bg.setColor(COLOR_EXPANDED_BG)
            bg.cornerRadius = dpToPx(this, 10).toFloat()
            menuRow.background = bg
        } else {
            menuRow.background = null
        }
    }

    private fun setExpandedStyle(parentView: View, expanded: Boolean) {
        val row = parentView.findViewById<View>(R.id.menu_row)
        val title = parentView.findViewById<TextView>(R.id.title)
        val icon = parentView.findViewById<ImageView>(R.id.icon)
        setRowHighlight(row, expanded)
        title?.setTextColor(if (expanded) COLOR_ACTIVE_TEXT else COLOR_NORMAL_TEXT)
        if (icon != null) {
            val d = icon.drawable
            d?.setTint(if (expanded) COLOR_ACTIVE_ICON else COLOR_NORMAL_ICON)
        }
    }

    private fun setActiveParent(parentView: View) {
        if (currentActiveParentView != null && currentActiveParentView !== parentView) {
            val prevRow = currentActiveParentView!!.findViewById<View>(R.id.menu_row)
            val prevTitle = currentActiveParentView!!.findViewById<TextView>(R.id.title)
            val prevCheck = currentActiveParentView!!.findViewById<TextView>(R.id.checked_menu)
            val prevIcon = currentActiveParentView!!.findViewById<ImageView>(R.id.icon)
            setRowHighlight(prevRow, false)
            prevTitle?.setTextColor(COLOR_NORMAL_TEXT)
            prevCheck?.visibility = View.INVISIBLE
            if (prevIcon != null) {
                val d = prevIcon.drawable
                d?.setTint(COLOR_NORMAL_ICON)
            }
            if (currentActiveSubItem != null) {
                val oldContainer = currentActiveParentView!!.findViewById<LinearLayout>(R.id.submenu_container)
                if (oldContainer != null && oldContainer.indexOfChild(currentActiveSubItem) >= 0) {
                    removeSubItemHighlight(currentActiveSubItem)
                    currentActiveSubItem = null
                }
            }
        }
        currentActiveParentView = parentView
        val row = parentView.findViewById<View>(R.id.menu_row)
        val title = parentView.findViewById<TextView>(R.id.title)
        val check = parentView.findViewById<TextView>(R.id.checked_menu)
        val icon = parentView.findViewById<ImageView>(R.id.icon)
        setRowHighlight(row, true)
        title?.setTextColor(COLOR_ACTIVE_TEXT)
        check?.visibility = View.VISIBLE
        if (icon != null) {
            val d = icon.drawable
            d?.setTint(COLOR_ACTIVE_ICON)
        }
    }

    private fun setActiveSubItem(subItem: TextView) {
        if (currentActiveSubItem != null && currentActiveSubItem !== subItem)
            removeSubItemHighlight(currentActiveSubItem)
        currentActiveSubItem = subItem
        applySubItemHighlight(subItem)
    }

    private fun clearActiveSubItem() {
        if (currentActiveSubItem != null) {
            removeSubItemHighlight(currentActiveSubItem)
            currentActiveSubItem = null
        }
    }

    private fun clearAllParentStyles() {
        for (p in allParents()) {
            val row = p.findViewById<View>(R.id.menu_row)
            val title = p.findViewById<TextView>(R.id.title)
            val check = p.findViewById<TextView>(R.id.checked_menu)
            val icon = p.findViewById<ImageView>(R.id.icon)
            setRowHighlight(row, false)
            title?.setTextColor(COLOR_NORMAL_TEXT)
            check?.visibility = View.INVISIBLE
            if (icon != null) {
                val d = icon.drawable
                d?.setTint(COLOR_NORMAL_ICON)
            }
        }
        currentActiveParentView = null
    }

    private fun allParents(): Array<View> {
        return arrayOf(
            sm_firmProfile, sm_appointments, sm_matter, sm_documents,
            sm_docEditor, sm_relationships, sm_timesheet, sm_meetings,
            sm_email, sm_messages, sm_notification, sm_invoice,
            sm_audit, sm_groups, sm_team_member, sm_logout
        )
    }

    private fun toggleSubMenu(parentView: View) {
        val container = parentView.findViewById<LinearLayout>(R.id.submenu_container)
        val arrow = parentView.findViewById<ImageView>(R.id.iv_arrow)
        if (container == null) return
        val isOpen = container.visibility == View.VISIBLE
        if (isOpen) {
            setExpandedStyle(parentView, false)
            if (currentActiveSubItem != null && container.indexOfChild(currentActiveSubItem) >= 0)
                removeSubItemHighlight(currentActiveSubItem)
            val startH = container.measuredHeight
            val anim = ValueAnimator.ofInt(startH, 0)
            anim.duration = 200
            anim.interpolator = DecelerateInterpolator()
            anim.addUpdateListener { a ->
                container.layoutParams.height = a.animatedValue as Int
                container.requestLayout()
            }
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(a: Animator) {
                    container.visibility = View.GONE
                    container.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    container.requestLayout()
                }
            })
            anim.start()
            arrow?.animate()?.rotation(0f)?.setDuration(200)?.start()
        } else {
            collapseAllSubMenusExcept(parentView)
            setExpandedStyle(parentView, true)
            container.visibility = View.VISIBLE
            container.measure(
                View.MeasureSpec.makeMeasureSpec(nav_linear_layout.width, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val targetH = container.measuredHeight
            container.layoutParams.height = 0
            container.requestLayout()
            val anim = ValueAnimator.ofInt(0, targetH)
            anim.duration = 200
            anim.interpolator = DecelerateInterpolator()
            anim.addUpdateListener { a ->
                container.layoutParams.height = a.animatedValue as Int
                container.requestLayout()
            }
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(a: Animator) {
                    container.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    container.requestLayout()
                    if (currentActiveSubItem != null && container.indexOfChild(currentActiveSubItem) >= 0)
                        applySubItemHighlight(currentActiveSubItem)
                }
            })
            anim.start()
            arrow?.animate()?.rotation(180f)?.setDuration(200)?.start()
        }
    }

    private fun navigateFromSub(parentView: View, fragment: Fragment) {
        setActiveParent(parentView)
        Constants.isCreate = false
        if (currentActiveSubItem != null) {
            val subText = currentActiveSubItem!!.text.toString()
            if (parentView === sm_matter) {
                if ("Legal Matters" == subText) Constants.MATTER_TYPE = "Legal"
                else if ("General Matters" == subText) Constants.MATTER_TYPE = "General"
            } else if (parentView === sm_documents) {
                var docType = "matter"
                when (subText) {
                    "Matter" -> docType = "matter"
                    "Client" -> docType = "client"
                    "Firm" -> docType = "firm"
                    "Deleted" -> docType = "delete"
                }
                val args = Bundle()
                args.putString("document_type", docType)
                fragment.arguments = args
            } else if (parentView === sm_relationships) {
                when (subText) {
                    "Individuals" -> Constants.Rel_Type = "Individual"
                    "Business" -> Constants.Rel_Type = "Entity"
                    "Corporate" -> Constants.Rel_Type = "Corporate"
                    "Deleted" -> Constants.Rel_Type = "Deleted"
                }
            } else if (parentView === sm_timesheet) {
                if ("My Timesheet" == subText) {
                    Constants.Timesheet_Card = "Myts"
                    Constants.ts_card_clicked = false
                } else if ("Aggregated Timesheet" == subText) {
                    Constants.Timesheet_Card = "Agts"
                    Constants.ts_card_clicked = true
                }
            } else if (parentView === sm_messages) {
                if ("Clients" == subText) Constants.isClient_chat = true
                else if ("Teams" == subText) Constants.isClient_chat = false
            } else if (parentView === sm_firmProfile) {
                if ("Profile" == subText) Constants.Profile_View = "Bp"
                else if ("Practice Partner" == subText) Constants.Profile_View = "Pp"
            }
        }
//        if (!Constants.is_active) {
//            AndroidUtils.showRenewalPopup(this)
//            return
//        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.id_framelayout, fragment)
            .addToBackStack("current_fragment")
            .commit()
        dLayout.closeDrawers()
    }

    private fun doNavigate(fragment: Fragment) {
        Constants.isCreate = false
        supportFragmentManager.beginTransaction()
            .replace(R.id.id_framelayout, fragment)
            .addToBackStack("current_fragment")
            .commit()
        dLayout.closeDrawers()
    }

    private fun collapseAllSubMenus() {
        collapseAllSubMenusExcept(null)
    }

    private fun collapseAllSubMenusExcept(except: View?) {
        val withSubMenus = arrayOf(sm_firmProfile, sm_matter, sm_documents, sm_relationships, sm_timesheet, sm_messages)
        for (p in withSubMenus) {
            if (p === except) continue
            val c = p.findViewById<LinearLayout>(R.id.submenu_container)
            val arr = p.findViewById<ImageView>(R.id.iv_arrow)
            if (c != null && c.visibility == View.VISIBLE) {
                if (currentActiveSubItem != null && c.indexOfChild(currentActiveSubItem) >= 0) {
                    removeSubItemHighlight(currentActiveSubItem)
                    currentActiveSubItem = null
                }
                c.visibility = View.GONE
                c.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                c.requestLayout()
                setExpandedStyle(p, false)
            }
            arr?.rotation = 0f
        }
    }

    fun checkMenuByFragment(fragment: Fragment) {
        var sel: View? = null
        if (fragment is FirmProfile) sel = sm_firmProfile
        else if (fragment is Appointments) sel = sm_appointments
        else if (fragment is Matter) sel = sm_matter
        else if (fragment is DocumentsEn) sel = sm_documents
        else if (fragment is DocEditor) sel = sm_docEditor
        else if (fragment is ClientRelationship) sel = sm_relationships
        else if (fragment is TimeSheets) sel = sm_timesheet
        else if (fragment is Meetings) sel = sm_meetings
        else if (fragment is Email) sel = sm_email
        else if (fragment is Chat) sel = sm_messages
        else if (fragment is MessagesList) sel = sm_messages
        else if (fragment is Notifications) sel = sm_notification
        else if (fragment is AuditTrails) sel = sm_audit
        else if (fragment is Groups) sel = sm_groups
        else if (fragment is Members) sel = sm_team_member
        else if (fragment is ViewInvoice) sel = sm_invoice
        
        val keepSubItem = (currentActiveSubItem != null && sel != null && sel === currentActiveParentView)
        clearAllParentStyles()
        if (!keepSubItem) clearActiveSubItem()
        if (sel != null) setActiveParent(sel)
    }

    fun navigation_items(fragment: Fragment) {
        Constants.isCreate = false
        checkMenuByFragment(fragment)
        ensureSubMenuExpansionForFragment(fragment)
        supportFragmentManager.beginTransaction()
            .replace(R.id.id_framelayout, fragment)
            .addToBackStack("current_fragment")
            .commit()
        dLayout.closeDrawers()
    }

    fun navigationToModules(fragment: Fragment) {
        Constants.isCreate = true
        checkMenuByFragment(fragment)
        ensureSubMenuExpansionForFragment(fragment)
        supportFragmentManager.beginTransaction()
            .replace(R.id.id_framelayout, fragment)
            .addToBackStack("current_fragment")
            .commit()
        dLayout.closeDrawers()
    }

    fun Add_Page(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .add(R.id.id_framelayout, fragment).addToBackStack("current_fragment").commit()
    }

    fun Remove_Page(fragment: Fragment) {
        supportFragmentManager.beginTransaction().remove(fragment).commit()
    }

    private fun navigateTocallDashboard() {
        clearAllParentStyles()
        clearActiveSubItem()
        collapseAllSubMenus()
        center_menu.close(true)
        Constants.isCreate = false
        menu_open.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.menu_icon_img))
        supportFragmentManager.beginTransaction()
            .replace(R.id.id_framelayout, com.digicoffer.lauditor.Dashboard.NewRevampViewModels.Dashboard())
            .addToBackStack("current_fragment").commit()
        dLayout.closeDrawers()
    }

    private fun fabMenu() {
        clearAllParentStyles()
        Constants.isCreate = false
        center_menu.close(true)
        menu_open.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.menu_icon_img))
    }

    private fun hide_un_chosen_menu() {
        for (iv in arrayOf(matters_bm, timesheets_bm, documents_bm, relationships_bm, groups_bm, team_members_bm, firm_profile_bm, audit_bm, more_bm)) {
            iv.background = getDrawable(R.drawable.background_transparent)
        }
        matters_bm.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.matter_menu_icon))
        timesheets_bm.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.timesheets_menu_icon))
        documents_bm.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.documents_menu_icon))
        relationships_bm.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.relationship_menu_icon))
        groups_bm.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.group_menu_icon))
        team_members_bm.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.teammember_menu_icon))
        firm_profile_bm.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.firm_profile_menu_icon))
        audit_bm.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.audit_menu_icon))
        more_bm.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.more_menu_icon))
    }

    private fun hideMenuItems() {
        sm_firmProfile.visibility = View.GONE
        sm_appointments.visibility = View.GONE
        sm_matter.visibility = View.GONE
        sm_documents.visibility = View.GONE
        sm_docEditor.visibility = View.GONE
        sm_relationships.visibility = View.GONE
        sm_timesheet.visibility = View.GONE
        sm_meetings.visibility = View.GONE
        sm_email.visibility = View.GONE
        sm_messages.visibility = View.GONE
        sm_notification.visibility = View.GONE
        sm_invoice.visibility = View.GONE
        sm_audit.visibility = View.GONE
        sm_groups.visibility = View.GONE
        sm_team_member.visibility = View.GONE
        sm_logout.visibility = View.VISIBLE
    }

    fun updateProfileInitial() {
        runOnUiThread {
            val n = Constants.NAME
            val initial = if (!n.isNullOrEmpty()) n.substring(0, 1).uppercase() else ""
            if (Constants.mainActivity?.person_icon != null) {
                Constants.mainActivity?.person_icon?.text = initial
            }
        }
    }

    private fun setDefaultMenuList() {
        hideMenuItems()
        val solo = "solo" == Constants.CATEGORY
        when (Constants.ROLE) {
            "AAM" -> {
                sm_firmProfile.visibility = View.VISIBLE
                sm_meetings.visibility = View.VISIBLE
                sm_email.visibility = View.GONE
                sm_messages.visibility = View.VISIBLE
                sm_notification.visibility = View.VISIBLE
                sm_audit.visibility = View.VISIBLE
                if (!solo) {
                    sm_groups.visibility = View.VISIBLE
                    sm_team_member.visibility = View.VISIBLE
                }
            }
            "SU" -> {
                sm_firmProfile.visibility = View.VISIBLE
                sm_appointments.visibility = View.VISIBLE
                sm_matter.visibility = View.VISIBLE
                sm_documents.visibility = View.VISIBLE
                sm_relationships.visibility = View.VISIBLE
                sm_timesheet.visibility = View.VISIBLE
                sm_meetings.visibility = View.VISIBLE
                sm_email.visibility = View.GONE
                sm_messages.visibility = View.VISIBLE
                sm_notification.visibility = View.VISIBLE
                sm_audit.visibility = View.VISIBLE
                if (DynamicUtils.isTablet(this)) sm_invoice.visibility = View.VISIBLE
                if (!solo) {
                    sm_groups.visibility = View.VISIBLE
                    sm_team_member.visibility = View.VISIBLE
                }
            }
            "GH" -> {
                sm_firmProfile.visibility = View.VISIBLE
                sm_appointments.visibility = View.VISIBLE
                sm_matter.visibility = View.VISIBLE
                sm_documents.visibility = View.VISIBLE
                sm_relationships.visibility = View.VISIBLE
                sm_timesheet.visibility = View.VISIBLE
                sm_meetings.visibility = View.VISIBLE
                sm_email.visibility = View.GONE
                sm_messages.visibility = View.VISIBLE
                sm_notification.visibility = View.VISIBLE
                sm_audit.visibility = View.VISIBLE
                if (!solo) {
                    sm_groups.visibility = View.VISIBLE
                    sm_team_member.visibility = View.VISIBLE
                }
            }
            else -> {
                sm_firmProfile.visibility = View.VISIBLE
                sm_appointments.visibility = View.VISIBLE
                sm_matter.visibility = View.VISIBLE
                sm_documents.visibility = View.VISIBLE
                sm_relationships.visibility = View.VISIBLE
                sm_timesheet.visibility = View.VISIBLE
                sm_meetings.visibility = View.VISIBLE
                sm_email.visibility = View.GONE
                sm_messages.visibility = View.VISIBLE
                sm_notification.visibility = View.VISIBLE
            }
        }
    }

    private fun setMenuList() {
        setDefaultMenuList()
    }

    private fun feat(item: View, key: String) {
        item.visibility = if (Constants.isFeatureEnabled(key)) View.VISIBLE else View.GONE
    }

    private fun showProfilePopup(anchorView: View) {
        val popupView = layoutInflater.inflate(R.layout.profile_popup, null)
        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT, true
        )
        val btnLogout = popupView.findViewById<LinearLayout>(R.id.btnLogout)
        val ll_role = popupView.findViewById<LinearLayout>(R.id.ll_role)
        val layout_firm_parent = popupView.findViewById<LinearLayout>(R.id.layout_firm_parent)
        val layout_firm = popupView.findViewById<LinearLayout>(R.id.layout_firm)
        val popup_person_icon = popupView.findViewById<TextView>(R.id.popup_person_icon)
        val iv_profile_popup = popupView.findViewById<ImageView>(R.id.iv_profile)
        val tvName = popupView.findViewById<TextView>(R.id.tvName)
        val tvEmail = popupView.findViewById<TextView>(R.id.tvEmail)
        val tvRoleTitle = popupView.findViewById<TextView>(R.id.tvRoleTitle)
        val tvRoleCompany = popupView.findViewById<TextView>(R.id.tvRoleCompany)
        val iconFirm = popupView.findViewById<TextView>(R.id.iconFirm)
        val tvFirm = popupView.findViewById<TextView>(R.id.tvFirm)
        val btnSwitchFirm = popupView.findViewById<TextView>(R.id.btnSwitchFirm)

        AppImageCache.preload(this, Constants.firm_image)
        if (Constants.ROLE == "AAM") {
            AndroidUtils.loadProfileImage(this, Constants.firm_image, iv_profile_popup, popup_person_icon, Constants.FIRM_NAME)
        } else {
            AndroidUtils.loadProfileImage(this, Constants.firm_image, iv_profile_popup, popup_person_icon, Constants.NAME)
        }
        tvName.text = "Hi, ${Constants.NAME}"
        tvName.setOnClickListener {
            popupWindow.dismiss()
            dLayout.closeDrawers()
            collapseAllSubMenus()
            clearAllParentStyles()
            clearActiveSubItem()
            Constants.isMyProfileClicked = Constants.ROLE != "AAM"
            setActiveParent(sm_firmProfile)
            supportFragmentManager.beginTransaction()
                .replace(R.id.id_framelayout, FirmProfile())
                .addToBackStack("current_fragment").commit()
        }
        tvEmail.text = Constants.Email

        if ("solo" == Constants.CATEGORY) {
            ll_role.visibility = View.GONE
        } else {
            ll_role.visibility = View.VISIBLE
            tvRoleTitle.text = AndroidUtils.getFullRoleName(Constants.ROLE)
            tvRoleCompany.text = Constants.FIRM_NAME
        }

        val multi = Constants.Firm_names != null && Constants.Firm_names.size > 1
        if (multi) {
            btnSwitchFirm.visibility = View.VISIBLE
            layout_firm_parent.visibility = View.VISIBLE
            if (!Constants.Firm_name.isNullOrBlank()) {
                iconFirm.text = AndroidUtils.getInitials(Constants.Firm_name)
                iconFirm.visibility = View.VISIBLE
            }
            layout_firm_parent.removeAllViews()
            val d = resources.displayMetrics.density
            val sz = (30 * d).toInt()
            val m8 = (8 * d).toInt()
            val m10 = (10 * d).toInt()
            val p8 = (8 * d).toInt()
            for (i in Constants.Firm_names.indices) {
                val name = Constants.Firm_names[i]
                val id = Constants.Firm_ids[i]
                if (Constants.FIRM_NAME != null && name.equals(Constants.FIRM_NAME, ignoreCase = true)) continue
                
                val iconDyn = TextView(this)
                iconDyn.text = AndroidUtils.getInitials(name)
                iconDyn.setTextColor(Color.WHITE)
                iconDyn.gravity = Gravity.CENTER
                iconDyn.background = getDrawable(R.drawable.circular_button_background)
                iconDyn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                val ilp = LinearLayout.LayoutParams(sz, sz)
                ilp.setMargins(0, 0, m8, 0)
                iconDyn.layoutParams = ilp
                
                val tvDyn = TextView(this)
                tvDyn.text = name
                tvDyn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                tvDyn.setTextColor(Color.BLACK)
                try {
                    tvDyn.typeface = ResourcesCompat.getFont(this, R.font.gill_sans_regular)
                } catch (ignore: Exception) {}
                val tlp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                tlp.setMargins(m10, 0, 0, 0)
                tvDyn.layoutParams = tlp
                
                val row = LinearLayout(this)
                row.orientation = LinearLayout.HORIZONTAL
                row.gravity = Gravity.CENTER_VERTICAL
                row.setPadding(0, p8, 0, p8)
                row.addView(iconDyn)
                row.addView(tvDyn)
                layout_firm_parent.addView(row)
                
                val cl = View.OnClickListener {
                    Constants.Firm_id = id
                    Constants.FIRM_NAME = name
                    popupWindow.dismiss()
                    isFirmSwitchInProgress = true
                    switchLogin()
                }
                iconDyn.setOnClickListener(cl)
                tvDyn.setOnClickListener(cl)
                row.setOnClickListener(cl)
            }
            if (layout_firm_parent.childCount == 0) {
                btnSwitchFirm.visibility = View.GONE
                layout_firm_parent.visibility = View.GONE
            }
        } else {
            btnSwitchFirm.visibility = View.GONE
            layout_firm_parent.visibility = View.GONE
        }
        
        btnLogout.setOnClickListener {
            popupWindow.dismiss()
            performLogout()
        }
        
        popupWindow.isOutsideTouchable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rootView = window.decorView as ViewGroup
        val dimView = View(this)
        dimView.setBackgroundColor(ContextCompat.getColor(this, R.color.popup_dim_overlay))
        dimView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        rootView.addView(dimView)
        popupWindow.setOnDismissListener { rootView.removeView(dimView) }
        popupWindow.showAsDropDown(anchorView, -150, 0)
    }

    fun switchLogin() {
        try {
            Constants.check_url()
            val post = JSONObject()
            post.put("email", Constants.Email)
            post.put("userid", Constants.Firm_id)
            post.put("plan", "lauditor")
            WebServiceHelper.callHttpWebService(
                this, this, WebServiceHelper.RestMethodType.POST, "switch-firm", "SWITCH_FIRM", post.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog?.isShowing == true) AndroidUtils.dismiss_dialog(progress_dialog)
        }
    }

    private fun performLogout() {
        MyFirebaseMessagingService.logoutToken(this, getStoredFCMToken())
        MyFirebaseMessagingService.clearAllNotifications(this)
        Constants.isClient_chat = true
        PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
            .remove("EXTRA_CONTACT_JID").remove("CURRENTCHAT_JID")
            .remove("firmNames").remove("requiresTermsAcceptance")
            .remove("termsVersion").remove("firmIds").apply()
        Constants.Chat_id = ""
        Constants.fromjid = ""
        Constants.Firm_ids.clear()
        Constants.Firm_names.clear()
        Constants.isClient_chat = true
        getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit().clear().apply()
        getSharedPreferences("BIO", Context.MODE_PRIVATE).edit().clear().apply()
        Constants.is_biometric = false
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }

    fun showConfirmation(): Dialog {
        val b = AlertDialog.Builder(this)
        val dl = LayoutInflater.from(this).inflate(R.layout.groups_required_popup, null)
        val btnCancel = dl.findViewById<Button>(R.id.btnCancel)
        val btnSave = dl.findViewById<Button>(R.id.btnSave)
        dl.findViewById<TextView>(R.id.tv_confirmation).setText(R.string.group_required)
        dl.findViewById<TextView>(R.id.tv_confirmContent).setText(R.string.to_perform_this_action_you_need_to_be_part_of_at_least_one_group_contact_the_admin_via_the_team_chat)
        btnCancel.setText(R.string.close)
        btnSave.setText(R.string.go_to_team_chat)
        val dialog = b.create()
        dialog.setView(dl)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val w = dialog.window
        if (w != null) {
            w.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val m = (20 * resources.displayMetrics.density + 0.5f).toInt()
            w.setLayout(resources.displayMetrics.widthPixels - 2 * m, WindowManager.LayoutParams.WRAP_CONTENT)
            val wlp = w.attributes
            wlp.gravity = Gravity.CENTER
            w.attributes = wlp
        }
        btnSave.setOnClickListener {
            dialog.dismiss()
            Constants.isClient_chat = false
            clearAllParentStyles()
            hide_un_chosen_menu()
            center_menu.close(true)
            navigateFromSub(sm_messages, Chat())
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        return dialog
    }

    fun fetchNotificationCount() {
        WebServiceHelper.callHttpWebService(
            this, this, WebServiceHelper.RestMethodType.GET, "notification", "NOTIFICATION_COUNT", ""
        )
    }

    fun updateNotificationBadge(count: Int) {
        runOnUiThread {
            var badge = Constants.notifyBadge
            if (badge == null && ::ll_notify.isInitialized) {
                val v = ll_notify.findViewById<View>(R.id.notify_badge)
                if (v is TextView) {
                    badge = v
                    Constants.notifyBadge = badge
                }
            }
            if (badge == null) {
                Log.e("NOTIF", "notify_badge is null")
                return@runOnUiThread
            }
            if (count > 0) {
                badge.text = count.toString()
                badge.visibility = View.VISIBLE
            } else badge.visibility = View.GONE
        }
    }

    fun profile() {
        progress_dialog = AndroidUtils.get_progress(this)
        try {
            val URL = if (Constants.ROLE == "AAM") "v3/firm/profile/pic" else "v3/profile/pic"
            WebServiceHelper.callHttpWebService(
                this, this, WebServiceHelper.RestMethodType.GET, URL, "Profile", JSONObject().toString()
            )
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    fun callDashboard() {
        AndroidUtils.updateCachedUserData(this)
        MYDAYCARDS.clear()
        KPICARDS.clear()
        Constants.check_url()
        Constants.base_URL = Constants.PROF_URL
        WebServiceHelper.callHttpWebService(
            this, this, WebServiceHelper.RestMethodType.GET, Constants.Dashboard ?: "", "Dashboard", JSONObject().toString()
        )
    }

    private fun validateSessionBeforeNavigation() {
        WebServiceHelper.callHttpWebService(
            this, this, WebServiceHelper.RestMethodType.GET, "notification", "SESSION_VALIDATE_COLD_START", ""
        )
    }

    private fun dispatchPendingColdStartNavigation() {
        val nav = pendingColdStartNavJson
        pendingColdStartNavJson = null
        if (nav.isNullOrEmpty()) return
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val jo = JSONObject(nav)
                val navObj = Navigation()
                navObj.route_name = jo.optString("route_name", "")
                val params = jo.optJSONObject("params")
                if (params != null) navObj.params = params
                Log.d("FCM_NAV", "Cold-start validated — dispatching nav route: ${navObj.route_name}")
                AndroidUtils.setupNotificationHandler(this, navObj)
            } catch (e: Exception) {
                Log.e("FCM_NAV", "Cold-start pending nav dispatch fail: ${e.message}")
            }
        }, 300)
    }

    override fun onClick(view: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progress_dialog?.isShowing == true) AndroidUtils.dismiss_dialog(progress_dialog)
        val requestTypeSafe = httpResult.requestType
        
        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent)
                when (httpResult.requestType) {
                    "Profile" -> {
                        if (!result.optBoolean("error")) {
                            val data = result.optJSONObject("data")
                            if (data != null) {
                                Constants.firm_image = data.optString("imageUrl", "")
                                AndroidUtils.loadProfileImage(this, Constants.firm_image, iv_profile, person_icon)
                            }
                            fetchNotificationCount()
                        }
                    }
                    "SWITCH_FIRM" -> handleLoginResponse(result)
                    "Dashboard" -> handleDashboardResponse(result)
                    "FCM_TOKEN_UPDATE" -> Log.d("FCM", "Token registered")
                    "NOTIFICATION_COUNT" -> handleNotificationCountResponse(result)
                    "SESSION_VALIDATE_COLD_START" -> dispatchPendingColdStartNavigation()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if ("SESSION_VALIDATE_COLD_START" == requestTypeSafe) dispatchPendingColdStartNavigation()
            }
        } else {
            if (progress_dialog?.isShowing == true) AndroidUtils.dismiss_dialog(progress_dialog)
            if ("SESSION_VALIDATE_COLD_START" == requestTypeSafe) {
                Log.w("FCM_NAV", "Session validation ping failed (non-401) — dispatching pending navigation anyway")
                dispatchPendingColdStartNavigation()
            } else {
                try {
                    AndroidUtils.showErrorAlert(JSONObject(httpResult.responseContent).optString("msg"), this)
                } catch (e: Exception) {
                    e.fillInStackTrace()
                }
            }
        }
    }

    private fun handleNotificationCountResponse(result: JSONObject) {
        try {
            if (result.optBoolean("error")) {
                Log.w("NOTIF", "Notification API error: ${result.optString("msg")}")
                return
            }
            var unreadCount = 0
            var notifications: JSONArray? = null
            val dataObj = result.optJSONObject("data")
            if (dataObj != null) notifications = dataObj.optJSONArray("notifications")
            if (notifications == null) notifications = result.optJSONArray("data")
            if (notifications == null) notifications = result.optJSONArray("notifications")
            if (notifications != null) {
                for (i in 0 until notifications.length()) {
                    val n = notifications.optJSONObject(i)
                    if (n != null && "unread".equals(n.optString("status", ""), ignoreCase = true)) unreadCount++
                }
            }
            updateNotificationBadge(unreadCount)
        } catch (e: Exception) {
            Log.e("NOTIF", "Failed to parse notification response: ${e.message}")
        }
    }

    private fun rebuildSideMenuForCurrentRole() {
        val isSolo = "solo" == Constants.CATEGORY
        val isGHorTM = "GH" == Constants.ROLE || "TM" == Constants.ROLE

        for (parent in allParents()) {
            val container = parent.findViewById<LinearLayout>(R.id.submenu_container)
            container?.removeAllViews()
        }

        if ("AAM" == Constants.ROLE) {
            setRowClick(sm_firmProfile) {
                Constants.isMyProfileClicked = false
                collapseAllSubMenus()
                setActiveParent(sm_firmProfile)
                doNavigate(FirmProfile())
            }
        } else if (isSolo || isGHorTM) {
            setRowClick(sm_firmProfile) {
                Constants.isMyProfileClicked = true
                collapseAllSubMenus()
                setActiveParent(sm_firmProfile)
                doNavigate(FirmProfile())
            }
        } else {
            setRowClick(sm_firmProfile) { toggleSubMenu(sm_firmProfile) }
        }

        setRowClick(sm_appointments) {
            collapseAllSubMenus()
            setActiveParent(sm_appointments)
            doNavigate(Appointments())
        }
        setRowClick(sm_matter) { toggleSubMenu(sm_matter) }
        setRowClick(sm_documents) { toggleSubMenu(sm_documents) }
        setRowClick(sm_docEditor) {
            collapseAllSubMenus()
            setActiveParent(sm_docEditor)
            doNavigate(DocEditor())
        }
        setRowClick(sm_relationships) { toggleSubMenu(sm_relationships) }

        if (isSolo) {
            setRowClick(sm_timesheet) {
                collapseAllSubMenus()
                setActiveParent(sm_timesheet)
                Constants.Timesheet_Card = "Myts"
                Constants.ts_card_clicked = false
                Constants.is_ts_submitted = false
                doNavigate(TimeSheets())
            }
        } else {
            setRowClick(sm_timesheet) { toggleSubMenu(sm_timesheet) }
        }

        setRowClick(sm_meetings) {
            collapseAllSubMenus()
            setActiveParent(sm_meetings)
            doNavigate(Meetings())
        }
        setRowClick(sm_email) {
            collapseAllSubMenus()
            setActiveParent(sm_email)
            doNavigate(Email())
            tv_pageName.textSize = DynamicUtils.eighteen.toFloat()
            tv_pageName.setText(R.string.emails)
        }

        if ("AAM" == Constants.ROLE) {
            setRowClick(sm_messages) {
                collapseAllSubMenus()
                setActiveParent(sm_messages)
                Constants.isClient_chat = false
                doNavigate(Chat())
            }
        } else if (isSolo) {
            setRowClick(sm_messages) {
                collapseAllSubMenus()
                setActiveParent(sm_messages)
                Constants.isClient_chat = true
                doNavigate(Chat())
            }
        } else {
            setRowClick(sm_messages) { toggleSubMenu(sm_messages) }
        }

        setRowClick(sm_notification) {
            collapseAllSubMenus()
            setActiveParent(sm_notification)
            doNavigate(Notifications())
        }
        setRowClick(sm_audit) {
            collapseAllSubMenus()
            setActiveParent(sm_audit)
            doNavigate(AuditTrails())
        }
        setRowClick(sm_groups) {
            collapseAllSubMenus()
            setActiveParent(sm_groups)
            doNavigate(Groups())
        }
        setRowClick(sm_team_member) {
            collapseAllSubMenus()
            setActiveParent(sm_team_member)
            doNavigate(Members())
        }
        setRowClick(sm_invoice) {
            collapseAllSubMenus()
            setActiveParent(sm_invoice)
            doNavigate(ViewInvoice())
        }
        setRowClick(sm_logout) { performLogout() }

        val firmProfileTitle: String
        val hasProfileSubMenu: Boolean
        if ("AAM" == Constants.ROLE) {
            Constants.isMyProfileClicked = false
            firmProfileTitle = getString(R.string.firm_profile)
            hasProfileSubMenu = false
        } else if (isSolo || isGHorTM) {
            Constants.isMyProfileClicked = true
            firmProfileTitle = "My Profile"
            hasProfileSubMenu = false
        } else {
            Constants.isMyProfileClicked = true
            firmProfileTitle = getString(R.string.profile)
            hasProfileSubMenu = true
        }

        setupMenuRow(sm_firmProfile, firmProfileTitle, R.drawable.profile, hasProfileSubMenu)
        setupMenuRow(sm_appointments, getString(R.string.appointments), R.drawable.appointments, false)
        setupMenuRow(sm_matter, getString(R.string.matters), R.drawable.matters, true)
        setupMenuRow(sm_documents, getString(R.string.documents), R.drawable.document_icon, true)
        setupMenuRow(sm_docEditor, getString(R.string.doc_editor), R.drawable.doceditor_icon, false)
        setupMenuRow(sm_relationships, getString(R.string.relationships), R.drawable.relationship, true)
        setupMenuRow(sm_timesheet, getString(R.string.timesheets), R.drawable.timesheet, !(isSolo || isGHorTM))
        setupMenuRow(sm_meetings, getString(R.string.meetings), R.drawable.meetings, false)
        setupMenuRow(sm_email, getString(R.string.emails), R.drawable.email, false)
        val hasMessagesSubMenu = "AAM" != Constants.ROLE && !isSolo
        setupMenuRow(sm_messages, getString(R.string.messages), R.drawable.messages, hasMessagesSubMenu)
        setupMenuRow(sm_notification, getString(R.string.notifications), R.drawable.notifications, false)
        setupMenuRow(sm_audit, getString(R.string.audit_trails), R.drawable.audit_trails, false)
        setupMenuRow(sm_groups, getString(R.string.groups), R.drawable.groups, false)
        setupMenuRow(sm_team_member, getString(R.string.members), R.drawable.members, false)
        setupMenuRow(sm_invoice, "Invoices", R.drawable.invoices, false)
        setupMenuRow(sm_logout, getString(R.string.logout), R.drawable.logout, false)

        if ("SU" == Constants.ROLE && !isSolo) {
            Constants.isMyProfileClicked = true
            addSubItem(sm_firmProfile, "Firm Profile") { v ->
                Constants.Profile_View = "Bp"
                Constants.isMyProfileClicked = false
                setActiveSubItem(v as TextView)
                navigateFromSub(sm_firmProfile, FirmProfile())
            }
            addSubItem(sm_firmProfile, "My Profile") { v ->
                Constants.Profile_View = "Bp"
                Constants.isMyProfileClicked = true
                setActiveSubItem(v as TextView)
                navigateFromSub(sm_firmProfile, FirmProfile())
            }
            addSubItem(sm_firmProfile, "Practice Partner") { v ->
                Constants.Profile_View = "Pp"
                setActiveSubItem(v as TextView)
                navigateFromSub(sm_firmProfile, FirmProfile())
            }
        }

        addSubItem(sm_matter, "Legal Matters") { v ->
            setActiveSubItem(v as TextView)
            Constants.Matter_CreateOrViewDetails = "View"
            Constants.MATTER_TYPE = "Legal"
            Constants.is_CreateMatter = false
            Constants.create_matter = false
            navigateFromSub(sm_matter, Matter())
        }
        addSubItem(sm_matter, "General Matters") { v ->
            setActiveSubItem(v as TextView)
            Constants.Matter_CreateOrViewDetails = "View"
            Constants.MATTER_TYPE = "General"
            Constants.is_CreateMatter = false
            Constants.create_matter = false
            navigateFromSub(sm_matter, Matter())
        }

        addSubItem(sm_documents, "Matter") { v ->
            setActiveSubItem(v as TextView)
            navigateFromSub(sm_documents, newDocumentFragment("matter"))
        }
        addSubItem(sm_documents, "Client") { v ->
            setActiveSubItem(v as TextView)
            navigateFromSub(sm_documents, newDocumentFragment("client"))
        }
        if (!isSolo) {
            addSubItem(sm_documents, "Firm") { v ->
                setActiveSubItem(v as TextView)
                navigateFromSub(sm_documents, newDocumentFragment("firm"))
            }
        }
        if (Constants.ROLE == "SU") {
            addSubItem(sm_documents, "Deleted") { v ->
                setActiveSubItem(v as TextView)
                navigateFromSub(sm_documents, newDocumentFragment("delete"))
            }
        }

        addSubItem(sm_relationships, "Individual") { v ->
            setActiveSubItem(v as TextView)
            Constants.Rel_Type = "Individual"
            navigateFromSub(sm_relationships, ClientRelationship())
        }
        addSubItem(sm_relationships, "Business") { v ->
            setActiveSubItem(v as TextView)
            Constants.Rel_Type = "Entity"
            navigateFromSub(sm_relationships, ClientRelationship())
        }
        addSubItem(sm_relationships, "Corporate") { v ->
            setActiveSubItem(v as TextView)
            Constants.Rel_Type = "Corporate"
            navigateFromSub(sm_relationships, ClientRelationship())
        }
        if (Constants.ROLE == "SU") {
            addSubItem(sm_relationships, "Deleted") { v ->
                setActiveSubItem(v as TextView)
                Constants.Rel_Type = "Deleted"
                navigateFromSub(sm_relationships, ClientRelationship())
            }
        }

        if (!isSolo) {
            addSubItem(sm_timesheet, "Aggregated Timesheet") { v ->
                setActiveSubItem(v as TextView)
                Constants.Timesheet_Card = "Agts"
                Constants.ts_card_clicked = true
                Constants.is_ts_submitted = false
                navigateFromSub(sm_timesheet, TimeSheets())
            }
            addSubItem(sm_timesheet, "My Timesheet") { v ->
                setActiveSubItem(v as TextView)
                Constants.Timesheet_Card = "Myts"
                Constants.ts_card_clicked = false
                Constants.is_ts_submitted = false
                navigateFromSub(sm_timesheet, TimeSheets())
            }
        }

        if (!isSolo) {
            val role = Constants.ROLE
            if ("AAM" != role) {
                addSubItem(sm_messages, "Clients") { v ->
                    setActiveSubItem(v as TextView)
                    Constants.isClient_chat = true
                    navigateFromSub(sm_messages, Chat())
                }
            }
            addSubItem(sm_messages, "Teams") { v ->
                setActiveSubItem(v as TextView)
                Constants.isClient_chat = false
                navigateFromSub(sm_messages, Chat())
            }
        }

        setMenuList()

        center_menu.close(true)
        hide_un_chosen_menu()
        menu_open.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.menu_icon_img))
    }

    private fun handleDashboardResponse(result: JSONObject) {
        try {
            if (result.optBoolean("error") && result.optString("msg").lowercase().contains("token")) {
                Constants.Valid_Token = false
                AndroidUtils.showAlert("Session expired. Please login again.", this)
                return
            }
            Constants.Valid_Token = true
            Dashboard_data(result.getJSONArray("cards"))
            saveXmppPreferences()
            Handler(Looper.getMainLooper()).postDelayed({
                Constants.mainActivity?.registerPendingFCMToken()
            }, 500)

            if (isFirmSwitchInProgress) {
                isFirmSwitchInProgress = false
                runOnUiThread {
                    rebuildSideMenuForCurrentRole()
                    if (Constants.ROLE == "AAM") {
                        val f = Constants.FIRM_NAME
                        person_icon.text = if (!f.isNullOrEmpty()) f.substring(0, 1) else "?"
                    } else {
                        val n = Constants.NAME
                        person_icon.text = if (!n.isNullOrEmpty()) n.substring(0, 1) else "?"
                    }
                    AppImageCache.preload(this, Constants.firm_image)
                    AndroidUtils.loadProfileImage(this, Constants.firm_image, iv_profile, person_icon)

                    clearAllParentStyles()
                    clearActiveSubItem()
                    collapseAllSubMenus()
                    center_menu.close(true)

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.id_framelayout, com.digicoffer.lauditor.Dashboard.NewRevampViewModels.Dashboard())
                        .commit()

                    fetchNotificationCount()
                }
                return
            }

            val mainIntent = Intent(this, MainActivity::class.java)
            if (!Constants.pendingFcmNavigation.isNullOrEmpty()) {
                mainIntent.putExtra("fcm_navigation", Constants.pendingFcmNavigation)
                Constants.pendingFcmNavigation = ""
            }
            startActivity(mainIntent)
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            AndroidUtils.showAlert("Error loading dashboard", this)
        }
    }

    private fun saveXmppPreferences() {
        var uid = Constants.UID
        if (!Constants.ROLE.equals("admin", ignoreCase = true)) uid = uid + "_" + Constants.USER_ID
        PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
            .putString("xmpp_jid", uid)
            .putString("xmpp_password", Constants.TOKEN)
            .putBoolean("xmpp_logged_in", true).apply()
        mConnection = ChatConnection(this)
        chatConnectionService = ChatConnectionService()
        JsonTask().execute(Constants.base_URL + "user/create/")
    }

    override fun highlightMenuForCardType(cardType: Int) {
        runOnUiThread {
            clearAllParentStyles()
            clearActiveSubItem()
            var targetMenu: View? = null
            when (cardType) {
                DashboardItem.TYPE_MEETING -> targetMenu = sm_meetings
                DashboardItem.TYPE_APPOINTMENT -> targetMenu = sm_appointments
                DashboardItem.TYPE_MESSAGES -> {
                    targetMenu = sm_messages
                    expandSubMenuIfNeeded(sm_messages)
                }
                DashboardItem.TYPE_NOTIFICATION -> targetMenu = sm_notification
                DashboardItem.TYPE_MATTER -> {
                    targetMenu = sm_matter
                    expandSubMenuIfNeeded(sm_matter)
                }
                DashboardItem.TYPE_BILLABLE, DashboardItem.TYPE_APPROX_REVENUE -> {
                    targetMenu = sm_timesheet
                    expandSubMenuIfNeeded(sm_timesheet)
                }
                DashboardItem.TYPE_HIRING -> targetMenu = sm_groups
                DashboardItem.TYPE_SUBSCRIPTION -> targetMenu = sm_firmProfile
            }
            if (targetMenu != null) {
                setActiveParent(targetMenu)
                highlightSubItemForCard(targetMenu, cardType)
            }
        }
    }

    private fun expandSubMenuIfNeeded(parentView: View) {
        val container = parentView.findViewById<LinearLayout>(R.id.submenu_container)
        val arrow = parentView.findViewById<ImageView>(R.id.iv_arrow)
        if (container != null && container.visibility != View.VISIBLE && container.childCount > 0) {
            container.visibility = View.VISIBLE
            arrow?.rotation = 180f
            setExpandedStyle(parentView, true)
        }
    }

    private fun highlightSubItemForCard(parentView: View, cardType: Int) {
        val container = parentView.findViewById<LinearLayout>(R.id.submenu_container) ?: return
        var targetSubItem: TextView? = null
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is TextView) {
                val text = child.text.toString()
                when (cardType) {
                    DashboardItem.TYPE_MESSAGES -> if (text == "Clients" || text == "Teams") targetSubItem = child
                    DashboardItem.TYPE_MATTER -> if (text == "Legal Matters" || text == "General Matters") targetSubItem = child
                    DashboardItem.TYPE_BILLABLE, DashboardItem.TYPE_APPROX_REVENUE -> if (text == "My Timesheet" || text == "Aggregated Timesheet") targetSubItem = child
                }
                if (targetSubItem != null) break
            }
        }
        if (targetSubItem != null) setActiveSubItem(targetSubItem)
    }

    private inner class JsonTask : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg p: String?): String {
            try {
                var uid = Constants.UID
                if (!Constants.ROLE.equals("admin", ignoreCase = true)) uid = uid + "_" + Constants.USER_ID
                ChatConnection.loginUser(this@MainActivity, uid, Constants.TOKEN)
            } catch (e: Exception) {
                Log.d("Chat", "fail")
                e.printStackTrace()
                chatConnectionService?.stopSelf()
            }
            return ""
        }
    }

    private fun Dashboard_data(arr: JSONArray) {
        MYDAYCARDS.clear()
        KPICARDS.clear()
        val skip = hashSetOf("timesheets", "newclients", "groups", "teammembers")
        for (i in 0 until arr.length()) {
            val jo = arr.getJSONObject(i)
            val type = jo.getString("type")
            val opts = jo.getJSONArray("options")
            for (j in 0 until opts.length()) {
                val o = opts.getJSONObject(j)
                val name = o.getString("name")
                if (skip.contains(name.lowercase())) continue
                val dm = Dashboard_Model()
                dm.name = name
                dm.sequence = o.getInt("sequence")
                dashboardModels.add(dm)
                if ("MYDAY" == type) MYDAYCARDS.add(dm)
                if ("KPI" == type) KPICARDS.add(dm)
            }
        }
    }

    private fun handleLoginResponse(result: JSONObject) {
        try {
            Constants.Firm_ids.clear()
            Constants.Firm_names.clear()
            if (result.getBoolean("error")) {
                AndroidUtils.showAlert(result.optString("msg", "Failed"), this)
                isFirmSwitchInProgress = false
                return
            }
            Constants.forgot_pwd_request = false
            val d = JSONObject(result.getString("data"))
            Constants.jsonObject_dashboard = d
            if (!d.getString("plan").equals("lauditor", ignoreCase = true)) {
                AndroidUtils.showAlert("Account not found", this)
                isFirmSwitchInProgress = false
                return
            }
            val em = Constants.Email!!
            PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
                .putString("email", em.lowercase())
                .putString("password", "")
                .putBoolean("isLogin", true)
                .putString("proBizType", Constants.PROBIZ_TYPE).apply()

            Constants.NAME = d.getString("name")
            Constants.LOGIN_METHOD = "email"
            Constants.termsVersion = d.getString("termsVersion")
            Constants.requiresTermsAcceptance = d.optBoolean("requiresTermsAcceptance")
            Constants.USER_ID = d.getString("user_id")
            Constants.UID = d.getString("uid")
            Constants.PK = d.getString("pk")
            Constants.PASSWORD_MODE = d.getString("password_mode")
            Constants.IS_ADMIN = d.getBoolean("admin")
            Constants.FIRM_NAME = d.getString("firm_name")
            Constants.ROLE = d.getString("role")
            Constants.CATEGORY = d.optString("category")
            Constants.Groups = d.getJSONArray("groups")
            Constants.Email = em.lowercase()
            Constants.FirmEmail = d.optString("email")
            Constants.Refresh_token = d.optString("refresh_token")
            Constants.TOKEN = d.optString("access_token")
            val sub = d.optJSONObject("subscription")
            if (sub != null) {
                val feat = sub.optJSONObject("features")
                if (feat != null) {
                    Constants.FEATURES.clear()
                    val k = feat.keys()
                    while (k.hasNext()) {
                        val key = k.next()
                        Constants.FEATURES[key] = feat.optBoolean(key, false)
                    }
                }
                Constants.is_active = true
            }
            Constants.User_Allowed = d.optInt("user_allowed")
            Constants.isAdmin = Constants.ROLE == "AAM" || (Constants.Groups.length() == 1 && Constants.Groups.getString(0) == "AAM")

            val firms = d.getJSONArray("firms")
            for (i in 0 until firms.length()) {
                val o = firms.getJSONObject(i)
                val fn = o.getString("firmName")
                val fi = o.getString("id")
                val fd = FirmsDo()
                fd.setName(fn)
                fd.value = fi
                Constants.Firm_names.add(fn)
                Constants.Firm_ids.add(fi)
            }

            getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit()
                .putString("Token", Constants.TOKEN)
                .putString("refresh_token", Constants.Refresh_token)
                .putString("email", em.lowercase())
                .putString("firm_id", Constants.Firm_id)
                .putString("Json_key", Constants.jsonObject_dashboard.toString())
                .putBoolean("requiresTermsAcceptance", Constants.requiresTermsAcceptance)
                .putString("termsVersion", Constants.termsVersion)
                .apply()

            Bio_metric_access()
            Constants.firm_image = ""
            profile()
            Constants.loginActivity?.syncDevice()
            MyFirebaseMessagingService.logoutToken(this, getStoredFCMToken())
            MyFirebaseMessagingService.clearAllNotifications(this)
            registerPendingFCMToken()
            checkTermsAndConditionsForFirmSwitch()
            Constants.IS_MyDay = true
        } catch (e: Exception) {
            e.printStackTrace()
            isFirmSwitchInProgress = false
        }
    }

    private fun checkTermsAndConditionsForFirmSwitch() {
        val context: Activity = this
        val termsAndCondition = TermsAndCondition(this)
        termsAndCondition.setOnTermsAndConditionListener(object : TermsAndCondition.OnTermsAndConditionListener {
            override fun onTermsAccepted() {
                Log.d("TermsCheck", "Terms Accepted")
                callDashboard()
            }
            override fun onTermsDeclined() {
                AndroidUtils.showConfirmationDialog(context, "Confirmation", "Please agree to the T&C to proceed. ", "Ok", "Cancel", object : AndroidUtils.OnConfirmListener {
                    override fun onSave() {
                        checkTermsAndConditionsForFirmSwitch()
                    }
                    override fun onCancel() {
                        finish()
                        performLogout()
                    }
                }, true)
            }
            override fun onTermsCheckComplete(needsToShow: Boolean) {
                Log.d("TermsCheck", "needsToShow: $needsToShow")
                if (!needsToShow) callDashboard()
            }
        })
        termsAndCondition.checkTermsWithData(Constants.termsVersion, Constants.requiresTermsAcceptance)
    }

    private fun checkTermsAndConditions() {
        val context: Activity = this
        val termsAndCondition = TermsAndCondition(this)
        termsAndCondition.setOnTermsAndConditionListener(object : TermsAndCondition.OnTermsAndConditionListener {
            override fun onTermsAccepted() {
                Log.d("TermsCheck", "Terms Accepted")
                runOnUiThread {
                    clearAllParentStyles()
                    clearActiveSubItem()
                    collapseAllSubMenus()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.id_framelayout, com.digicoffer.lauditor.Dashboard.NewRevampViewModels.Dashboard())
                        .commit()
                }
            }
            override fun onTermsDeclined() {
                AndroidUtils.showConfirmationDialog(context, "Confirmation", "Please agree to the T&C to proceed. ", "Ok", "Cancel", object : AndroidUtils.OnConfirmListener {
                    override fun onSave() {
                        checkTermsAndConditions()
                    }
                    override fun onCancel() {
                        finish()
                        performLogout()
                    }
                }, true)
            }
            override fun onTermsCheckComplete(needsToShow: Boolean) {
                Log.d("TermsCheck", "needsToShow: $needsToShow")
            }
        })
        termsAndCondition.checkTermsWithData(Constants.termsVersion, Constants.requiresTermsAcceptance)
    }

    private fun Bio_metric_access() {
        getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit()
            .putBoolean("requiresTermsAcceptance", Constants.requiresTermsAcceptance)
            .putString("termsVersion", Constants.termsVersion)
            .putString("email", Constants.Email?.lowercase() ?: "")
            .putString("password", "")
            .putString("Token", Constants.TOKEN)
            .putString("refresh_token", Constants.Refresh_token)
            .putString("login_method", Constants.LOGIN_METHOD)
            .putString("firm_id", Constants.Firm_id)
            .putString("Json_key", Constants.jsonObject_dashboard.toString())
            .putBoolean("Check_box", true)
            .apply()
        Constants.is_biometric = true
    }

    override fun onResume() {
        super.onResume()
        if (!Constants.pendingFcmNavigation.isNullOrEmpty()) {
            val nav = Constants.pendingFcmNavigation
            Constants.pendingFcmNavigation = ""
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val jo = JSONObject(nav)
                    val navObj = Navigation()
                    navObj.route_name = jo.optString("route_name", "")
                    val params = jo.optJSONObject("params")
                    if (params != null) navObj.params = params
                    AndroidUtils.setupNotificationHandler(this, navObj)
                } catch (e: Exception) {
                    Log.e("FCM_NAV", "Dashboard pending nav fail: ${e.message}")
                }
            }, 300)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d("Refresh_Token", Constants.Refresh_token ?: "")
        var navJson = intent.getStringExtra("fcm_navigation")
        if (navJson.isNullOrEmpty()) navJson = intent.getStringExtra("navigation")
        if (navJson.isNullOrEmpty()) return
        val finalNavJson = navJson
        intent.removeExtra("fcm_navigation")
        intent.removeExtra("navigation")
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val jo = JSONObject(finalNavJson)
                val nav = Navigation()
                nav.route_name = jo.optString("route_name", "")
                val params = jo.optJSONObject("params")
                if (params != null) nav.params = params
                Log.d("FCM_NAV", "onNewIntent route: ${nav.route_name}")
                AndroidUtils.setupNotificationHandler(this, nav)
            } catch (e: Exception) {
                Log.e("FCM_NAV", "onNewIntent nav fail: ${e.message}")
            }
        }, 400)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AndroidUtils.PERMISSION_REQUEST_CAMERA_AUDIO) {
            var ok = true
            for (r in grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    ok = false
                    break
                }
            }
            if (ok) {
                val p = getSharedPreferences("video_call_prefs", Context.MODE_PRIVATE)
                val url = p.getString("pending_avchat_url", null)
                if (url != null) {
                    p.edit().remove("pending_avchat_url").remove("has_pending_delegate").apply()
                    AndroidUtils.loadAVChatView(this, this, url)
                }
            } else {
                AndroidUtils.showToast("Camera and Microphone permissions are required for video chat", this)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (dtoggle != null && dtoggle!!.onOptionsItemSelected(item)) {
            actionButton.visibility = View.VISIBLE
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onEventDetailsPassed(list: ArrayList<Event_Details_DO>, cal: String) {}

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else fetchAndStoreFCMToken()
        } else {
            fetchAndStoreFCMToken()
        }
    }

    private fun fetchAndStoreFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCM", "fail:${task.exception}")
                return@addOnCompleteListener
            }
            val t = task.result
            val p = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val s = p.getString("fcm_token", "")
            p.edit().putString("fcm_token", t).apply()
            if (!Constants.USER_ID.isNullOrEmpty() && t != s) {
                sendFCMTokenToBackend(t)
            }
        }
    }

    fun registerPendingFCMToken() {
        val t = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("fcm_token", "") ?: ""
        if (t.isNotEmpty() && !Constants.USER_ID.isNullOrEmpty()) {
            sendFCMTokenToBackend(t)
        }
    }

    private fun sendFCMTokenToBackend(token: String) {
        try {
            val p = JSONObject()
            p.put("userId", Constants.USER_ID)
            p.put("fcmToken", token)
            p.put("platform", "android")
            WebServiceHelper.callHttpWebService(
                this, this, WebServiceHelper.RestMethodType.POST,
                "${Constants.Notification_Base_Url}/register-token", "FCM_TOKEN_UPDATE", p.toString()
            )
        } catch (e: Exception) {
            Log.e("FCM", "fail:${e.message}")
        }
    }

    fun getStoredFCMToken(): String {
        return getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("fcm_token", "") ?: ""
    }

    private fun handleNotificationNavigation(intent: Intent?) {
        if (intent == null) return
        var nj = intent.getStringExtra("fcm_navigation")
        if (nj.isNullOrEmpty()) nj = intent.getStringExtra("navigation")
        if (nj.isNullOrEmpty()) return
        val finalNj = nj
        Log.d("FCM_NAV", "Navigation JSON: $finalNj")
        intent.removeExtra("fcm_navigation")
        intent.removeExtra("navigation")
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val jo = JSONObject(finalNj)
                val nav = Navigation()
                nav.route_name = jo.optString("route_name", "")
                val params = jo.optJSONObject("params")
                if (params != null) nav.params = params
                Log.d("FCM_NAV", "Route: ${nav.route_name}")
                AndroidUtils.setupNotificationHandler(this, nav)
            } catch (e: Exception) {
                Log.e("FCM", "nav fail:${e.message}")
            }
        }, 500)
    }

    private fun dpToPx(ctx: Context, dp: Int): Int {
        return (dp * ctx.resources.displayMetrics.density).toInt()
    }

    private fun handleDashboardState() {
        collapseAllSubMenus()
        clearAllParentStyles()
        clearActiveSubItem()
        if (dLayout.isDrawerOpen(GravityCompat.START)) dLayout.closeDrawer(GravityCompat.START)
    }

    private fun ensureSubMenuExpansionForFragment(fragment: Fragment) {
        var parentView: View? = null
        if (fragment is FirmProfile) parentView = sm_firmProfile
        else if (fragment is Appointments) parentView = sm_appointments
        else if (fragment is Matter) parentView = sm_matter
        else if (fragment is DocumentsEn) parentView = sm_documents
        else if (fragment is DocEditor) parentView = sm_docEditor
        else if (fragment is ClientRelationship) parentView = sm_relationships
        else if (fragment is TimeSheets) parentView = sm_timesheet
        else if (fragment is Meetings) parentView = sm_meetings
        else if (fragment is Email) parentView = sm_email
        else if (fragment is Chat || fragment is MessagesList) parentView = sm_messages
        else if (fragment is Notifications) parentView = sm_notification
        else if (fragment is AuditTrails) parentView = sm_audit
        else if (fragment is Groups) parentView = sm_groups
        else if (fragment is Members) parentView = sm_team_member
        else if (fragment is ViewInvoice) parentView = sm_invoice

        if (parentView != null) {
            val container = parentView.findViewById<LinearLayout>(R.id.submenu_container)
            val arrow = parentView.findViewById<ImageView>(R.id.iv_arrow)
            if (container != null && container.childCount > 0) {
                collapseAllSubMenusExcept(parentView)
                if (container.visibility != View.VISIBLE) {
                    container.visibility = View.VISIBLE
                    arrow?.rotation = 180f
                    setExpandedStyle(parentView, true)
                    container.measure(
                        View.MeasureSpec.makeMeasureSpec(nav_linear_layout.width, View.MeasureSpec.AT_MOST),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                    val targetH = container.measuredHeight
                    container.layoutParams.height = 0
                    container.requestLayout()
                    val anim = ValueAnimator.ofInt(0, targetH)
                    anim.duration = 200
                    anim.interpolator = DecelerateInterpolator()
                    anim.addUpdateListener { a ->
                        container.layoutParams.height = a.animatedValue as Int
                        container.requestLayout()
                    }
                    anim.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(a: Animator) {
                            container.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                            container.requestLayout()
                        }
                    })
                    anim.start()
                }
                highlightCorrectSubItem(parentView, fragment)
            }
        }
    }

    private fun highlightCorrectSubItem(parentView: View, fragment: Fragment) {
        val container = parentView.findViewById<LinearLayout>(R.id.submenu_container) ?: return
        var targetSubText: String? = null
        if (fragment is Matter) {
            if ("Legal" == Constants.MATTER_TYPE) targetSubText = "Legal Matters"
            else if ("General" == Constants.MATTER_TYPE) targetSubText = "General Matters"
        } else if (fragment is DocumentsEn) {
            val args = fragment.arguments
            if (args != null) {
                when (args.getString("document_type")) {
                    "matter" -> targetSubText = "Matter"
                    "client" -> targetSubText = "Client"
                    "firm" -> targetSubText = "Firm"
                    "delete" -> targetSubText = "Deleted"
                }
            } else {
                targetSubText = "Matter"
            }
        } else if (fragment is ClientRelationship) {
            when (Constants.Rel_Type) {
                "Individual" -> targetSubText = "Individuals"
                "Entity" -> targetSubText = "Business"
                "Corporate" -> targetSubText = "Corporate"
                "Deleted" -> targetSubText = "Deleted"
            }
        } else if (fragment is TimeSheets) {
            if ("Myts" == Constants.Timesheet_Card) targetSubText = "My Timesheet"
            else if ("Agts" == Constants.Timesheet_Card) targetSubText = "Aggregated Timesheet"
        } else if (fragment is Chat) {
            targetSubText = if (Constants.isClient_chat) "Clients" else "Teams"
        } else if (fragment is FirmProfile) {
            if ("Bp" == Constants.Profile_View) targetSubText = "Profile"
            else if ("Pp" == Constants.Profile_View) targetSubText = "Practice Partner"
        }

        if (targetSubText != null) {
            for (i in 0 until container.childCount) {
                val child = container.getChildAt(i)
                if (child is TextView) {
                    val itemText = child.text.toString()
                    var matches = targetSubText == itemText
                    if (!matches && fragment is ClientRelationship) {
                        matches = ("Individuals" == itemText && "Individual" == Constants.Rel_Type) ||
                                ("Business" == itemText && "Entity" == Constants.Rel_Type) ||
                                ("Corporate" == itemText && "Corporate" == Constants.Rel_Type) ||
                                ("Deleted" == itemText && "Deleted" == Constants.Rel_Type)
                    }
                    if (!matches && fragment is DocumentsEn) {
                        val dt = getDocumentTypeFromFragment(fragment)
                        matches = ("Matter" == itemText && "matter" == dt) ||
                                ("Client" == itemText && "client" == dt) ||
                                ("Firm" == itemText && "firm" == dt) ||
                                ("Deleted" == itemText && "delete" == dt)
                    }
                    if (matches) {
                        if (currentActiveSubItem != null && currentActiveSubItem !== child)
                            removeSubItemHighlight(currentActiveSubItem)
                        currentActiveSubItem = child
                        applySubItemHighlight(child)
                        break
                    }
                }
            }
        }
    }

    private fun getDocumentTypeFromFragment(fragment: Fragment): String {
        if (fragment is DocumentsEn) {
            val args = fragment.arguments
            if (args != null) return args.getString("document_type", "matter")
        }
        return "matter"
    }
}
