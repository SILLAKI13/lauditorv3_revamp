package com.digicoffer.lauditor;


import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.getInitials;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.KPICARDS;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.MYDAYCARDS;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;


import com.digicoffer.lauditor.Appointments.ViewModels.Appointments;
import com.digicoffer.lauditor.AuditTrails.AuditTrails;
import com.digicoffer.lauditor.Meetings.ViewModels.Meetings;
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO;
import com.digicoffer.lauditor.Meetings.ViewModels.MonthlyCalendar;
import com.digicoffer.lauditor.Meetings.ViewModels.WeeklyCalendar;
import com.digicoffer.lauditor.Chat.ViewModels.Chat;
import com.digicoffer.lauditor.Chat.ViewModels.MessagesList;
import com.digicoffer.lauditor.Relationships.ClientRelationship;
import com.digicoffer.lauditor.Dashboard.DahboardModels.MenuModels;
import com.digicoffer.lauditor.Dashboard.NewRevampViewModels.Dashboard;
import com.digicoffer.lauditor.Dashboard.NewRevampViewModels.DashboardItem;


import com.digicoffer.lauditor.DocEditor.DocEditor;
import com.digicoffer.lauditor.Documents.ViewModel.DocumentsEn;
import com.digicoffer.lauditor.FirmProfile.FirmProfile;
import com.digicoffer.lauditor.Groups.Groups;
import com.digicoffer.lauditor.Invoice.ViewModels.ViewInvoice;
import com.digicoffer.lauditor.LoginActivity.Models.Dashboard_Model;
import com.digicoffer.lauditor.LoginActivity.Models.FirmsDo;
import com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity;
import com.digicoffer.lauditor.Matter.ViewModels.Matter;
import com.digicoffer.lauditor.Members.Members;
import com.digicoffer.lauditor.Notifications.Models.Navigation;
import com.digicoffer.lauditor.Notifications.Notifications;
import com.digicoffer.lauditor.TimeSheets.ViewModels.TimeSheets;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnection;
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnectionService;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.CacheUtils.AppImageCache;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;
import com.digicoffer.lauditor.CommonFiles.PushNotifications.MyFirebaseMessagingService;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel;
import com.digicoffer.lauditor.CommonFiles.TermsAndCondition.TermsAndCondition;
import com.digicoffer.lauditor.Email.Email;


import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;


import org.json.JSONArray;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;


public class MainActivity extends AppCompatActivity
        implements com.digicoffer.lauditor.Dashboard.NewRevampViewModels.Dashboard.MenuHighlightListener, MonthlyCalendar.EventDetailsListener,
        WeeklyCalendar.EventDetailsListener,
        AsyncTaskCompleteListener {


    private static final int RC_SIGN_IN = 9001;
    private static final int AUTH_REQUEST_CODE = 1001;
    private static final int REQ_ONE_TAP = 2;


    // ── FIX: flag to distinguish firm-switch reload from a fresh launch ──
    // When true, handleDashboardResponse() reloads the dashboard in-place
    // instead of starting a new MainActivity (which triggers the login guard).
    private boolean isFirmSwitchInProgress = false;


    // ── FIX: holds the FCM navigation JSON during a cold start while we wait
    // for session validation (and a possible silent token refresh) to finish.
    // Only used on cold start; warm/background resumes are unaffected.
    private String pendingColdStartNavJson = null;


    // ── Colors resolved from color.xml at runtime — NO hardcoded values ──
    private int COLOR_EXPANDED_BG;
    private int COLOR_NORMAL_BG;
    private int COLOR_ACTIVE_TEXT;
    private int COLOR_ACTIVE_ICON;
    private int COLOR_NORMAL_TEXT;
    private int COLOR_NORMAL_ICON;


    /**
     * Currently active parent view — tracked so we can clear it on next selection
     */
    private View currentActiveParentView = null;


    // FAB
    ExtendedFloatingActionButton mAddFab;
    Boolean isAllFabsVisible;
    ImageView menu_open;
    FloatingActionMenu center_menu;
    com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton actionButton;


    // AppBar
    AppBarLayout appbar;
    AppBarLayout header_layout;
    ImageView iv_logo_dashboard, reminders;
    LinearLayout digi_logo_layout;
    TextView tv_pageName;
    TextView tv_headerName, tv_digilogo, tv_header_firm_name;


    // Drawer
    DrawerLayout dLayout;
    DrawerLayout navigationDrawer;
    ActionBarDrawerToggle dtoggle;
    NavigationView navView;
    LinearLayout nav_linear_layout;
    ImageView iv_Drawer, iv_profile;


    // Profile
    TextView person_icon;
    FrameLayout ll_notify;


    // Side-menu root views
    View sm_firmProfile;
    View sm_appointments;
    View sm_matter;
    View sm_documents;
    View sm_docEditor;
    View sm_relationships;
    View sm_timesheet;
    View sm_meetings;
    View sm_email;
    public View sm_messages;
    View sm_notification;
    View sm_audit;
    View sm_groups;
    View sm_team_member;
    View sm_invoice;
    View sm_logout;


    // Currently active sub-item view (for highlight tracking)
    private TextView currentActiveSubItem = null;


    // Bottom FAB buttons
    ImageView matters_bm, timesheets_bm, relationships_bm, groups_bm,
            team_members_bm, audit_bm, more_bm, firm_profile_bm, documents_bm;


    // Legacy
    Menu nav_Menu;
    MenuItem team_member_sm, audits_sm, meetings_sm, messages_sm,
            notifications_sm, logout_sm, email_sm;


    private Dialog progress_dialog;
    private Dialog progressDialog;
    String token_id;
    int itemId;
    private boolean showOneTapUI = true;
    private boolean isAuthInProgress = false;
    GoogleSignInOptions gso;
    Email email;
    ArrayList<Dashboard_Model> dashboardModels = new ArrayList<>();
    ArrayList<MenuModels> menuList = new ArrayList<>();
    RecyclerView recyclerView;
    private NewModel viewModel;
    private static ChatConnectionService chatConnectionService;
    private ChatConnection mConnection;
    private LoginActivity loginActivity;
    public androidx.appcompat.widget.LinearLayoutCompat ll_bottom_menu;


    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted)
                    AndroidUtils.showToast("Enable notifications to receive updates", MainActivity.this);
                fetchAndStoreFCMToken();
            });


    // ══════════════════════════════════════════════════════════════════════
    //  onCreate
    // ══════════════════════════════════════════════════════════════════════
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // ── FIX: capture whether this is a genuine cold start (process was
        // killed, Constants.TOKEN reset) BEFORE we restore it from prefs.
        // This flag drives whether we gate the pending FCM navigation behind
        // a session-validation round trip below.
        boolean wasColdStart = (Constants.TOKEN == null || Constants.TOKEN.isEmpty());


        if (wasColdStart) {
            boolean restored = Constants.restoreSessionFromPrefs(this);
            if (!restored) {
                // Truly no session — send to login, forward the FCM nav
                String navExtra = getIntent() != null
                        ? getIntent().getStringExtra("fcm_navigation") : null;
                if (navExtra == null && getIntent() != null)
                    navExtra = getIntent().getStringExtra("navigation");
                Intent loginIntent = new Intent(this, LoginActivity.class);
                if (navExtra != null && !navExtra.isEmpty())
                    loginIntent.putExtra("fcm_navigation", navExtra);
                startActivity(loginIntent);
                finish();
                return;
            }
        }


        // ── FIX: moved up (was previously a few lines below, after the
        // pending-nav assignment) so that base_URL/PROBIZ_TYPE are correctly
        // set before we potentially fire the session-validation request.
        Constants.check_url();
        Constants.base_URL = Constants.PROF_URL;
        Constants.PROBIZ_TYPE = "PROFESSIONAL";


        String coldStartNav = getIntent() != null
                ? getIntent().getStringExtra("fcm_navigation") : null;
        if (coldStartNav == null && getIntent() != null)
            coldStartNav = getIntent().getStringExtra("navigation");


        if (coldStartNav != null && !coldStartNav.isEmpty()) {
            if (wasColdStart) {
                // ── FIX: cold start + pending notification nav — do NOT set
                // Constants.pendingFcmNavigation yet. Hold it locally and
                // validate the restored session first (this transparently
                // refreshes the token via the existing TokenRefreshHelper
                // pipeline if it's expired) before dispatching navigation.
                pendingColdStartNavJson = coldStartNav;
                validateSessionBeforeNavigation();
            } else {
                Constants.pendingFcmNavigation = coldStartNav;
            }
        }


        setContentView(R.layout.activity_main);


        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.Blue_text_color));
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);


        COLOR_EXPANDED_BG = ContextCompat.getColor(this, R.color.lite_shaded_blue);
        COLOR_NORMAL_BG = ContextCompat.getColor(this, R.color.lite_grey);
        COLOR_ACTIVE_TEXT = ContextCompat.getColor(this, R.color.blue);
        COLOR_ACTIVE_ICON = ContextCompat.getColor(this, R.color.blue);
        COLOR_NORMAL_TEXT = ContextCompat.getColor(this, R.color.lite_grey);
        COLOR_NORMAL_ICON = ContextCompat.getColor(this, R.color.lite_grey);


        requestNotificationPermission();
        if (getSupportActionBar() != null) getSupportActionBar().hide();


        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment f = getSupportFragmentManager().findFragmentById(R.id.id_framelayout);
            if (f != null) {
                if (f instanceof Dashboard) {
                    handleDashboardState();
                } else {
                    checkMenuByFragment(f);
                    ensureSubMenuExpansionForFragment(f);
                }
            }
        });


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .edit().remove("current_fragment").apply();
                appbar.setVisibility(VISIBLE);
                if (dLayout.isDrawerOpen(GravityCompat.START)) {
                    dLayout.closeDrawer(GravityCompat.START);
                    return;
                }
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.id_framelayout);
                if (f instanceof Dashboard) {
                    handleDashboardState();
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
            }
        });


        Constants.mainActivity = this;
        Constants.isClient_chat = true;


        // Find views
        dLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.navigation);
        nav_linear_layout = findViewById(R.id.nav_linear_layout);


        sm_firmProfile = findViewById(R.id.sm_firmProfile);
        sm_appointments = findViewById(R.id.sm_appointments);
        sm_matter = findViewById(R.id.sm_matters);
        sm_documents = findViewById(R.id.sm_documents);
        sm_docEditor = findViewById(R.id.sm_docEditor);
        sm_relationships = findViewById(R.id.sm_relationships);
        sm_timesheet = findViewById(R.id.sm_timesheet);
        sm_groups = findViewById(R.id.sm_groups);
        sm_team_member = findViewById(R.id.sm_team_member);
        sm_meetings = findViewById(R.id.sm_meetings);
        sm_messages = findViewById(R.id.sm_messages);
        sm_notification = findViewById(R.id.sm_notification);
        sm_invoice = findViewById(R.id.sm_invoice);
        sm_email = findViewById(R.id.sm_email);
        sm_audit = findViewById(R.id.sm_audit);
        sm_logout = findViewById(R.id.sm_logout);


        boolean isSolo = "solo".equals(Constants.CATEGORY);
        boolean isGHorTM = "GH".equals(Constants.ROLE) || "TM".equals(Constants.ROLE);


        // ── Wire menu_row clicks ──────────────────────────────────────────
        if ("AAM".equals(Constants.ROLE)) {
            setRowClick(sm_firmProfile, v -> {
                Constants.isMyProfileClicked = false;
                collapseAllSubMenus();
                setActiveParent(sm_firmProfile);
                doNavigate(new FirmProfile());
            });
        } else if ((isSolo || isGHorTM)) {
            setRowClick(sm_firmProfile, v -> {
                Constants.isMyProfileClicked = true;
                collapseAllSubMenus();
                setActiveParent(sm_firmProfile);
                doNavigate(new FirmProfile());
            });
        } else {
            setRowClick(sm_firmProfile, v -> toggleSubMenu(sm_firmProfile));
        }


        setRowClick(sm_appointments, v -> {
            collapseAllSubMenus();
            setActiveParent(sm_appointments);
            doNavigate(new Appointments());
        });
        setRowClick(sm_matter, v -> toggleSubMenu(sm_matter));
        setRowClick(sm_documents, v -> toggleSubMenu(sm_documents));
        setRowClick(sm_docEditor, v -> {
            collapseAllSubMenus();
            setActiveParent(sm_docEditor);
            doNavigate(new DocEditor());
        });
        setRowClick(sm_relationships, v -> toggleSubMenu(sm_relationships));


        if (isSolo) {
            setRowClick(sm_timesheet, v -> {
                collapseAllSubMenus();
                setActiveParent(sm_timesheet);
                Constants.Timesheet_Card = "Myts";
                Constants.ts_card_clicked = false;
                Constants.is_ts_submitted = false;
                doNavigate(new TimeSheets());
            });
        } else {
            setRowClick(sm_timesheet, v -> toggleSubMenu(sm_timesheet));
        }


        setRowClick(sm_meetings, v -> {
            collapseAllSubMenus();
            setActiveParent(sm_meetings);
            doNavigate(new Meetings());
        });
        setRowClick(sm_email, v -> {
            collapseAllSubMenus();
            setActiveParent(sm_email);
            doNavigate(new Email());
            tv_pageName.setTextSize(DynamicUtils.eighteen);
            tv_pageName.setText(R.string.emails);
        });


        if (Constants.ROLE.equals("AAM")) {
            setRowClick(sm_messages, v -> {
                collapseAllSubMenus();
                setActiveParent(sm_messages);
                Constants.isClient_chat = false;
                doNavigate(new Chat());
            });
        } else if (isSolo) {
            setRowClick(sm_messages, v -> {
                collapseAllSubMenus();
                setActiveParent(sm_messages);
                Constants.isClient_chat = true;
                doNavigate(new Chat());
            });
        } else {
            // GH, TM, SU — all get Clients + Teams submenu
            setRowClick(sm_messages, v -> toggleSubMenu(sm_messages));
        }


        setRowClick(sm_notification, v -> {
            collapseAllSubMenus();
            setActiveParent(sm_notification);
            doNavigate(new Notifications());
        });
        setRowClick(sm_audit, v -> {
            collapseAllSubMenus();
            setActiveParent(sm_audit);
            doNavigate(new AuditTrails());
        });
        setRowClick(sm_groups, v -> {
            collapseAllSubMenus();
            setActiveParent(sm_groups);
            doNavigate(new Groups());
        });
        setRowClick(sm_team_member, v -> {
            collapseAllSubMenus();
            setActiveParent(sm_team_member);
            doNavigate(new Members());
        });
        setRowClick(sm_invoice, v -> {
            collapseAllSubMenus();
            setActiveParent(sm_invoice);
            doNavigate(new ViewInvoice());
        });
        setRowClick(sm_logout, v -> performLogout());


        // ── Setup icons + titles ──────────────────────────────────────────
        String firmProfileTitle;
        boolean hasSubMenu;
        if ("AAM".equals(Constants.ROLE)) {
            Constants.isMyProfileClicked = false;
            firmProfileTitle = getString(R.string.firm_profile);
            hasSubMenu = false;
        } else if (isSolo || isGHorTM) {
            Constants.isMyProfileClicked = true;
            firmProfileTitle = "My Profile";
            hasSubMenu = false;
        } else {
            Constants.isMyProfileClicked = true;
            firmProfileTitle = getString(R.string.profile);
            hasSubMenu = true;
        }
        boolean hasMessagesSubMenu = !"AAM".equals(Constants.ROLE) && !isSolo;
        setupMenuRow(sm_firmProfile, firmProfileTitle, R.drawable.profile, hasSubMenu);
        setupMenuRow(sm_appointments, getString(R.string.appointments), R.drawable.appointments, false);
        setupMenuRow(sm_matter, getString(R.string.matters), R.drawable.matters, true);
        setupMenuRow(sm_documents, getString(R.string.documents), R.drawable.document_icon, true);
        setupMenuRow(sm_docEditor, getString(R.string.doc_editor), R.drawable.doceditor_icon, false);
        setupMenuRow(sm_relationships, getString(R.string.relationships), R.drawable.relationship, true);
        setupMenuRow(sm_timesheet, getString(R.string.timesheets), R.drawable.timesheet, !isSolo);
        setupMenuRow(sm_meetings, getString(R.string.meetings), R.drawable.meetings, false);
        setupMenuRow(sm_email, getString(R.string.emails), R.drawable.email, false);
        setupMenuRow(sm_messages, getString(R.string.messages), R.drawable.messages, hasMessagesSubMenu);
        setupMenuRow(sm_notification, getString(R.string.notifications), R.drawable.notifications, false);
        setupMenuRow(sm_audit, getString(R.string.audit_trails), R.drawable.audit_trails, false);
        setupMenuRow(sm_groups, getString(R.string.groups), R.drawable.groups, false);
        setupMenuRow(sm_team_member, getString(R.string.members), R.drawable.members, false);
        setupMenuRow(sm_invoice, "Invoices", R.drawable.invoices, false);
        setupMenuRow(sm_logout, getString(R.string.logout), R.drawable.logout, false);


        // ══════════════════════════════════════════════════════════════════
        //  BUILD ALL SUBMENUS
        // ══════════════════════════════════════════════════════════════════
        if (("SU".equals(Constants.ROLE) && !isSolo)) {
            Constants.isMyProfileClicked = true;
            addSubItem(sm_firmProfile, "Firm Profile", v -> {
                Constants.Profile_View = "Bp";
                Constants.isMyProfileClicked = false;
                setActiveSubItem((TextView) v);
                navigateFromSub(sm_firmProfile, new FirmProfile());
            });
            addSubItem(sm_firmProfile, "My Profile", v -> {
                Constants.Profile_View = "Bp";
                Constants.isMyProfileClicked = true;
                setActiveSubItem((TextView) v);
                navigateFromSub(sm_firmProfile, new FirmProfile());
            });
            addSubItem(sm_firmProfile, "Practice Partner", v -> {
                Constants.Profile_View = "Pp";
                setActiveSubItem((TextView) v);
                navigateFromSub(sm_firmProfile, new FirmProfile());
            });
        }


        // Matters
        addSubItem(sm_matter, "Legal Matters", v -> {
            setActiveSubItem((TextView) v);
            Constants.Matter_CreateOrViewDetails = "View";
            Constants.MATTER_TYPE = "Legal";
            Constants.is_CreateMatter = false;
            Constants.create_matter = false;
            navigateFromSub(sm_matter, new Matter());
        });
        addSubItem(sm_matter, "General Matters", v -> {
            setActiveSubItem((TextView) v);
            Constants.Matter_CreateOrViewDetails = "View";
            Constants.MATTER_TYPE = "General";
            Constants.is_CreateMatter = false;
            Constants.create_matter = false;
            navigateFromSub(sm_matter, new Matter());
        });


        // Documents
        addSubItem(sm_documents, "Matter", v -> {
            setActiveSubItem((TextView) v);
            navigateFromSub(sm_documents, newDocumentFragment("matter"));
        });
        addSubItem(sm_documents, "Client", v -> {
            setActiveSubItem((TextView) v);
            navigateFromSub(sm_documents, newDocumentFragment("client"));
        });
        if (!isSolo) {
            addSubItem(sm_documents, "Firm", v -> {
                setActiveSubItem((TextView) v);
                navigateFromSub(sm_documents, newDocumentFragment("firm"));
            });
        }
        if (Constants.ROLE.equals("SU")) {
            addSubItem(sm_documents, "Deleted", v -> {
                setActiveSubItem((TextView) v);
                navigateFromSub(sm_documents, newDocumentFragment("delete"));
            });
        }


        // Relationships
        addSubItem(sm_relationships, "Individual", v -> {
            setActiveSubItem((TextView) v);
            Constants.Rel_Type = "Individual";
            navigateFromSub(sm_relationships, new ClientRelationship());
        });
        addSubItem(sm_relationships, "Business", v -> {
            setActiveSubItem((TextView) v);
            Constants.Rel_Type = "Entity";
            navigateFromSub(sm_relationships, new ClientRelationship());
        });
        addSubItem(sm_relationships, "Corporate", v -> {
            setActiveSubItem((TextView) v);
            Constants.Rel_Type = "Corporate";
            navigateFromSub(sm_relationships, new ClientRelationship());
        });
        if (Constants.ROLE.equals("SU")) {
            addSubItem(sm_relationships, "Deleted", v -> {
                setActiveSubItem((TextView) v);
                Constants.Rel_Type = "Deleted";
                navigateFromSub(sm_relationships, new ClientRelationship());
            });
        }


        // Timesheets
        if (!(isSolo)) {
            addSubItem(sm_timesheet, "Aggregated Timesheet", v -> {
                setActiveSubItem((TextView) v);
                Constants.Timesheet_Card = "Agts";
                Constants.ts_card_clicked = true;
                Constants.is_ts_submitted = false;
                navigateFromSub(sm_timesheet, new TimeSheets());
            });
            addSubItem(sm_timesheet, "My Timesheet", v -> {
                setActiveSubItem((TextView) v);
                Constants.Timesheet_Card = "Myts";
                Constants.ts_card_clicked = false;
                Constants.is_ts_submitted = false;
                navigateFromSub(sm_timesheet, new TimeSheets());
            });
        }


        // Messages
        if (!isSolo) {
            String role = Constants.ROLE;
            if (!"AAM".equals(role)) {
                addSubItem(sm_messages, "Clients", v -> {
                    setActiveSubItem((TextView) v);
                    Constants.isClient_chat = true;
                    navigateFromSub(sm_messages, new Chat());
                });
            }
            addSubItem(sm_messages, "Teams", v -> {
                setActiveSubItem((TextView) v);
                Constants.isClient_chat = false;
                navigateFromSub(sm_messages, new Chat());
            });
        }


        // Persist firm arrays
        getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit()
                .putString("firmNames", new JSONArray(Constants.Firm_names).toString())
                .putBoolean("requiresTermsAcceptance", Constants.requiresTermsAcceptance)
                .putString("termsVersion", Constants.termsVersion)
                .putString("firmIds", new JSONArray(Constants.Firm_ids).toString())
                .apply();


        // ── Bottom FAB ────────────────────────────────────────────────────
        String deviceSize = DynamicUtils.isNormalPhone(this);
        menu_open = new ImageView(this);
        menu_open.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.menu_icon_img));


        int menuWidth, menuHeight, paddingTB, subBtnSize;
        switch (deviceSize) {
            case "Small":
                menuWidth = 190;
                menuHeight = 90;
                paddingTB = -10;
                subBtnSize = 100;
                break;
            case "Medium":
                menuWidth = 230;
                menuHeight = 120;
                paddingTB = -10;
                subBtnSize = 120;
                break;
            default:
                menuWidth = 310;
                menuHeight = 150;
                paddingTB = -20;
                subBtnSize = 180;
                break;
        }
        menu_open.setPadding(0, paddingTB, 0, paddingTB);
        LinearLayout.LayoutParams fabLp = new LinearLayout.LayoutParams(menuWidth, menuHeight);
        AndroidUtils.adjustBottomMargin(getApplicationContext(), fabLp);
        actionButton = new com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton.Builder(this)
                .setContentView(menu_open)
                .setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.menu_desing))
                .setLayoutParams(new com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton.LayoutParams(fabLp))
                .setPosition(com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton.POSITION_BOTTOM_CENTER)
                .build();


        matters_bm = new ImageView(this);
        timesheets_bm = new ImageView(this);
        documents_bm = new ImageView(this);
        relationships_bm = new ImageView(this);
        groups_bm = new ImageView(this);
        team_members_bm = new ImageView(this);
        firm_profile_bm = new ImageView(this);
        audit_bm = new ImageView(this);
        more_bm = new ImageView(this);


        int cp = deviceSize.equals("Small") ? 8 : deviceSize.equals("Medium") ? 12 : 20;
        for (ImageView iv : new ImageView[]{matters_bm, timesheets_bm, documents_bm,
                relationships_bm, groups_bm, firm_profile_bm, more_bm, team_members_bm})
            iv.setPadding(cp, cp, cp, cp);


        hide_un_chosen_menu();


        SubActionButton.Builder rLSub = new SubActionButton.Builder(this);
        FrameLayout.LayoutParams subParams = new FrameLayout.LayoutParams(subBtnSize, subBtnSize);
        subParams.setMargins(0, 0, 0, dpToPx(this, 60));


        switch (Constants.ROLE) {
            case "SU":
            case "GH":
                center_menu = new FloatingActionMenu.Builder(this)
                        .addSubActionView(rLSub.setContentView(matters_bm).setLayoutParams(subParams).build())
                        .addSubActionView(rLSub.setContentView(timesheets_bm).build())
                        .addSubActionView(rLSub.setContentView(documents_bm).build())
                        .addSubActionView(rLSub.setContentView(relationships_bm).build())
                        .addSubActionView(rLSub.setContentView(groups_bm).build())
                        .attachTo(actionButton).setStartAngle(200).setEndAngle(340).build();
                break;
            case "AAM":
                center_menu = new FloatingActionMenu.Builder(this)
                        .addSubActionView(rLSub.setContentView(groups_bm).setLayoutParams(subParams).build())
                        .addSubActionView(rLSub.setContentView(team_members_bm).build())
                        .addSubActionView(rLSub.setContentView(audit_bm).build())
                        .addSubActionView(rLSub.setContentView(firm_profile_bm).build())
                        .attachTo(actionButton).setStartAngle(200).setEndAngle(340).build();
                break;
            default:
                center_menu = new FloatingActionMenu.Builder(this)
                        .addSubActionView(rLSub.setContentView(matters_bm).setLayoutParams(subParams).build())
                        .addSubActionView(rLSub.setContentView(timesheets_bm).build())
                        .addSubActionView(rLSub.setContentView(documents_bm).build())
                        .addSubActionView(rLSub.setContentView(relationships_bm).build())
                        .addSubActionView(rLSub.setContentView(more_bm).build())
                        .attachTo(actionButton).setStartAngle(200).setEndAngle(340).build();
                break;
        }


        center_menu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu m) {
                dLayout.close();
                menu_open.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.menu_down_icon));
            }


            @Override
            public void onMenuClosed(FloatingActionMenu m) {
                menu_open.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.menu_icon_img));
            }
        });


        matters_bm.setOnClickListener(v -> {
            Constants.Matter_CreateOrViewDetails = "View";
            Constants.MATTER_TYPE = "Legal";
            Constants.is_CreateMatter = false;
            Constants.create_matter = false;
            fabMenu();
            setActiveParent(sm_matter);
            doNavigate(new Matter());
            hide_un_chosen_menu();
            matters_bm.setImageDrawable(getDrawable(R.drawable.matter_white));
            matters_bm.setBackground(getDrawable(R.drawable.circular_button_background));
        });
        timesheets_bm.setOnClickListener(v -> {
            Constants.ts_card_clicked = false;
            fabMenu();
            setActiveParent(sm_timesheet);
            doNavigate(new TimeSheets());
            hide_un_chosen_menu();
            timesheets_bm.setBackground(getDrawable(R.drawable.circular_button_background));
            timesheets_bm.setImageDrawable(getDrawable(R.drawable.timesheets_white));
        });
        documents_bm.setOnClickListener(v -> {
            fabMenu();
            setActiveParent(sm_documents);
            doNavigate(new DocumentsEn());
            hide_un_chosen_menu();
            documents_bm.setBackground(getDrawable(R.drawable.circular_button_background));
            documents_bm.setImageDrawable(getDrawable(R.drawable.documents_white));
        });
        relationships_bm.setOnClickListener(v -> {
            Constants.Rel_Type = "Individual";
            fabMenu();
            setActiveParent(sm_relationships);
            doNavigate(new ClientRelationship());
            hide_un_chosen_menu();
            relationships_bm.setBackground(getDrawable(R.drawable.circular_button_background));
            relationships_bm.setImageDrawable(getDrawable(R.drawable.relationship_white));
        });
        groups_bm.setOnClickListener(v -> {
            fabMenu();
            setActiveParent(sm_groups);
            doNavigate(new Groups());
            hide_un_chosen_menu();
            groups_bm.setBackground(getDrawable(R.drawable.circular_button_background));
            groups_bm.setImageDrawable(getDrawable(R.drawable.groups_white));
        });
        team_members_bm.setOnClickListener(v -> {
            fabMenu();
            setActiveParent(sm_team_member);
            doNavigate(new Members());
            hide_un_chosen_menu();
            team_members_bm.setBackground(getDrawable(R.drawable.circular_button_background));
            team_members_bm.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.team_member_white));
        });
        firm_profile_bm.setOnClickListener(v -> {
            fabMenu();
            setActiveParent(sm_firmProfile);
            doNavigate(new FirmProfile());
            hide_un_chosen_menu();
            firm_profile_bm.setBackground(getDrawable(R.drawable.circular_button_background));
            firm_profile_bm.setImageDrawable(getDrawable(R.drawable.firm_profile_white));
            center_menu.close(true);
        });
        audit_bm.setOnClickListener(v -> {
            fabMenu();
            setActiveParent(sm_audit);
            doNavigate(new AuditTrails());
            hide_un_chosen_menu();
            audit_bm.setBackground(getDrawable(R.drawable.circular_button_background));
            audit_bm.setImageDrawable(getDrawable(R.drawable.audit_white));
        });
        more_bm.setOnClickListener(v -> {
            dLayout.openDrawer(GravityCompat.START);
            nav_linear_layout.setVisibility(VISIBLE);
            center_menu.close(true);
            hide_un_chosen_menu();
            more_bm.setBackground(getDrawable(R.drawable.circular_button_background));
            more_bm.setImageDrawable(getDrawable(R.drawable.more_white));
        });


        dLayout.setOnDragListener((v, e) -> {
            nav_linear_layout.setVisibility(VISIBLE);
            setMenuList();
            return false;
        });


        // ── AppBar wiring ─────────────────────────────────────────────────
        try {
            tv_pageName = findViewById(R.id.page_name);
            iv_logo_dashboard = findViewById(R.id.logo_dashboard);
            digi_logo_layout = findViewById(R.id.digi_logo_layout);
            reminders = findViewById(R.id.reminders);
            appbar = findViewById(R.id.appbar);
            appbar.setVisibility(VISIBLE);


            ll_notify = appbar.findViewById(R.id.ll_notify);
            ll_notify.setOnClickListener(v -> {
                setActiveParent(sm_notification);
                doNavigate(new Notifications());
            });


            Constants.notifyBadge = (TextView) ll_notify.findViewById(R.id.notify_badge);


            viewModel = new ViewModelProvider(this).get(NewModel.class);
            viewModel.getselectedItem().observe(this, item -> {
                if (item.equals(getString(R.string.lauditor))) {
                    tv_pageName.setTextSize(DynamicUtils.twentyFive);
                    tv_pageName.setText(R.string.appname);
                    iv_logo_dashboard.setImageDrawable(getDrawable(R.drawable.loading_animation_new));
                } else {
                    tv_pageName.setText(item);
                    tv_pageName.setTextSize(DynamicUtils.eighteen);
                    iv_logo_dashboard.setImageDrawable(getDrawable(R.drawable.lawyer_logo_nw));
                }
            });


            person_icon = findViewById(R.id.person_icon);
            iv_profile = findViewById(R.id.iv_profile);
            person_icon.setVisibility(VISIBLE);
            iv_profile.setVisibility(GONE);
            // Update the profile avatar / name in the appbar
            person_icon.setText(
                    (Constants.NAME != null && !Constants.NAME.isEmpty())
                            ? Constants.NAME.substring(0, 1) : "?");
// Profile image will be updated by profile() API response


            profile();
            setMenuList();


            iv_Drawer = findViewById(R.id.menu);
            actionButton.setVisibility(GONE);
            iv_Drawer.setOnClickListener(v -> {
                if (!dLayout.isDrawerOpen(GravityCompat.START)) {
                    dLayout.openDrawer(GravityCompat.START);
                    center_menu.close(true);
                    nav_linear_layout.setVisibility(VISIBLE);
                } else {
                    dLayout.close();
                }
            });


            reminders.setOnClickListener(v -> openEmailComposer());
            person_icon.setOnClickListener(v -> {
                dLayout.close();
                showProfilePopup(person_icon);
            });
            iv_profile.setOnClickListener(v -> {
                dLayout.close();
                showProfilePopup(person_icon);
            });
            digi_logo_layout.setOnClickListener(v -> navigateToDashboard());
            iv_logo_dashboard.setOnClickListener(v -> navigateToDashboard());
//            Constants.requiresTermsAcceptance = false;


            Fragment dashFrag = new com.digicoffer.lauditor.Dashboard.NewRevampViewModels.Dashboard();
            WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.id_framelayout, dashFrag);
            ft.commit();
            isAllFabsVisible = false;
            checkTermsAndConditions();

            final Intent launchIntent = getIntent();
            if (launchIntent != null) {
                String pendingNav = launchIntent.getStringExtra("fcm_navigation");
                if (pendingNav == null || pendingNav.isEmpty())
                    pendingNav = launchIntent.getStringExtra("navigation");
                if (pendingNav != null && !pendingNav.isEmpty()) {
                    // ── FIX: if this navigation is already being held for
                    // dispatch after cold-start session validation, do NOT
                    // let this block re-populate Constants.pendingFcmNavigation
                    // early — that would let onResume() fire it before the
                    // validation/refresh round-trip finishes, reintroducing
                    // the exact race this fix is meant to close.
                    if (!(wasColdStart && pendingColdStartNavJson != null)) {
                        Constants.pendingFcmNavigation = pendingNav;
                    }
                    launchIntent.removeExtra("fcm_navigation");
                    launchIntent.removeExtra("navigation");
                }
            }


        } catch (Resources.NotFoundException e) {
            e.fillInStackTrace();
        }
    }


    private void openEmailComposer() {
        String supportEmail = "support@lexiz.ai";
        String subject = "Support Request";


        Uri mailUri = Uri.parse("mailto:" + supportEmail + "?subject=" + Uri.encode(subject));
        Intent intent = new Intent(Intent.ACTION_SENDTO, mailUri);


        try {
            startActivity(Intent.createChooser(intent, "Send email"));
        } catch (android.content.ActivityNotFoundException e) {
            new AlertDialog.Builder(this)
                    .setTitle("No Email App Found")
                    .setMessage("Please email us at: " + supportEmail)
                    .setPositiveButton("Copy Email", (dialog, which) -> {
                        ClipboardManager clipboard =
                                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("email", supportEmail);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(this, "Email copied to clipboard", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("OK", null)
                    .show();
        }
    }


    // ══════════════════════════════════════════════════════════════════════
    //  Document fragment with Bundle arg
    // ══════════════════════════════════════════════════════════════════════
    private Fragment newDocumentFragment(String type) {
        DocumentsEn frag = new DocumentsEn();
        Bundle args = new Bundle();
        args.putString("document_type", type);
        frag.setArguments(args);
        return frag;
    }


    // ══════════════════════════════════════════════════════════════════════
    //  Wire click to menu_row (inner clickable child)
    // ══════════════════════════════════════════════════════════════════════
    private void setRowClick(View parentView, View.OnClickListener listener) {
        View row = parentView.findViewById(R.id.menu_row);
        if (row != null) row.setOnClickListener(listener);
        else parentView.setOnClickListener(listener);
    }


    // ══════════════════════════════════════════════════════════════════════
    //  Setup icon + title + arrow
    // ══════════════════════════════════════════════════════════════════════
    private void setupMenuRow(View parentView, String title, int iconResId, boolean hasSubMenu) {
        TextView tvTitle = parentView.findViewById(R.id.title);
        ImageView icon = parentView.findViewById(R.id.icon);
        ImageView arrow = parentView.findViewById(R.id.iv_arrow);
        if (tvTitle != null) tvTitle.setText(title);
        if (icon != null) {
            Drawable d = getDrawable(iconResId);
            if (d != null) {
                d.setTint(COLOR_NORMAL_ICON);
                icon.setImageDrawable(d);
            }
        }
        if (arrow != null) arrow.setVisibility(hasSubMenu ? VISIBLE : GONE);
    }


    // ══════════════════════════════════════════════════════════════════════
    //  Add sub-item TextView to submenu_container
    // ══════════════════════════════════════════════════════════════════════
    private void addSubItem(View parentView, String label, View.OnClickListener listener) {
        LinearLayout container = parentView.findViewById(R.id.submenu_container);
        if (container == null) {
            Log.e("SUBMENU", "container null: " + label);
            return;
        }


        TextView subItem = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.sub_menu_item, container, false);
        subItem.setText(label);
        subItem.setBackground(null);
        subItem.setTextColor(COLOR_NORMAL_TEXT);


        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(dpToPx(this, 4), dpToPx(this, 4), dpToPx(this, 4), dpToPx(this, 3));
        subItem.setLayoutParams(params);
        subItem.setPadding(
                dpToPx(this, DynamicUtils.sixtySeven), dpToPx(this, 10),
                dpToPx(this, 8), dpToPx(this, 10));


        subItem.setOnClickListener(listener);
        container.addView(subItem);
    }


    // ══════════════════════════════════════════════════════════════════════
    //  DYNAMIC HIGHLIGHT
    // ══════════════════════════════════════════════════════════════════════
    private void applySubItemHighlight(TextView subItem) {
        if (subItem == null) return;
        android.graphics.drawable.GradientDrawable pill = new android.graphics.drawable.GradientDrawable();
        pill.setColor(COLOR_EXPANDED_BG);
        pill.setCornerRadius(dpToPx(this, 8));
        subItem.setBackground(pill);
        subItem.setTextColor(COLOR_ACTIVE_TEXT);
    }


    private void removeSubItemHighlight(TextView subItem) {
        if (subItem == null) return;
        subItem.setBackground(null);
        subItem.setTextColor(COLOR_NORMAL_TEXT);
    }


    private void setRowHighlight(View menuRow, boolean active) {
        if (menuRow == null) return;
        if (active) {
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setColor(COLOR_EXPANDED_BG);
            bg.setCornerRadius(dpToPx(this, 10));
            menuRow.setBackground(bg);
        } else {
            menuRow.setBackground(null);
        }
    }


    private void setExpandedStyle(View parentView, boolean expanded) {
        View row = parentView.findViewById(R.id.menu_row);
        TextView title = parentView.findViewById(R.id.title);
        ImageView icon = parentView.findViewById(R.id.icon);
        setRowHighlight(row, expanded);
        if (title != null) title.setTextColor(expanded ? COLOR_ACTIVE_TEXT : COLOR_NORMAL_TEXT);
        if (icon != null) {
            Drawable d = icon.getDrawable();
            if (d != null) d.setTint(expanded ? COLOR_ACTIVE_ICON : COLOR_NORMAL_ICON);
        }
    }


    private void setActiveParent(View parentView) {
        if (currentActiveParentView != null && currentActiveParentView != parentView) {
            View prevRow = currentActiveParentView.findViewById(R.id.menu_row);
            TextView prevTitle = currentActiveParentView.findViewById(R.id.title);
            TextView prevCheck = currentActiveParentView.findViewById(R.id.checked_menu);
            ImageView prevIcon = currentActiveParentView.findViewById(R.id.icon);
            setRowHighlight(prevRow, false);
            if (prevTitle != null) prevTitle.setTextColor(COLOR_NORMAL_TEXT);
            if (prevCheck != null) prevCheck.setVisibility(View.INVISIBLE);
            if (prevIcon != null) {
                Drawable d = prevIcon.getDrawable();
                if (d != null) d.setTint(COLOR_NORMAL_ICON);
            }


            if (currentActiveSubItem != null) {
                LinearLayout oldContainer = currentActiveParentView.findViewById(R.id.submenu_container);
                if (oldContainer != null && oldContainer.indexOfChild(currentActiveSubItem) >= 0) {
                    removeSubItemHighlight(currentActiveSubItem);
                    currentActiveSubItem = null;
                }
            }
        }


        currentActiveParentView = parentView;
        View row = parentView.findViewById(R.id.menu_row);
        TextView title = parentView.findViewById(R.id.title);
        TextView check = parentView.findViewById(R.id.checked_menu);
        ImageView icon = parentView.findViewById(R.id.icon);
        setRowHighlight(row, true);
        if (title != null) title.setTextColor(COLOR_ACTIVE_TEXT);
        if (check != null) check.setVisibility(VISIBLE);
        if (icon != null) {
            Drawable d = icon.getDrawable();
            if (d != null) d.setTint(COLOR_ACTIVE_ICON);
        }
    }


    private void setActiveSubItem(TextView subItem) {
        if (currentActiveSubItem != null && currentActiveSubItem != subItem)
            removeSubItemHighlight(currentActiveSubItem);
        currentActiveSubItem = subItem;
        applySubItemHighlight(subItem);
    }


    private void clearActiveSubItem() {
        if (currentActiveSubItem != null) {
            removeSubItemHighlight(currentActiveSubItem);
            currentActiveSubItem = null;
        }
    }


    private void clearAllParentStyles() {
        for (View p : allParents()) {
            View row = p.findViewById(R.id.menu_row);
            TextView title = p.findViewById(R.id.title);
            TextView check = p.findViewById(R.id.checked_menu);
            ImageView icon = p.findViewById(R.id.icon);
            setRowHighlight(row, false);
            if (title != null) title.setTextColor(COLOR_NORMAL_TEXT);
            if (check != null) check.setVisibility(View.INVISIBLE);
            if (icon != null) {
                Drawable d = icon.getDrawable();
                if (d != null) d.setTint(COLOR_NORMAL_ICON);
            }
        }
        currentActiveParentView = null;
    }


    private View[] allParents() {
        return new View[]{sm_firmProfile, sm_appointments, sm_matter, sm_documents,
                sm_docEditor, sm_relationships, sm_timesheet, sm_meetings,
                sm_email, sm_messages, sm_notification, sm_invoice,
                sm_audit, sm_groups, sm_team_member, sm_logout};
    }


    // ══════════════════════════════════════════════════════════════════════
    //  Toggle submenu with animation + dynamic parent highlight
    // ══════════════════════════════════════════════════════════════════════
    private void toggleSubMenu(View parentView) {
        LinearLayout container = parentView.findViewById(R.id.submenu_container);
        ImageView arrow = parentView.findViewById(R.id.iv_arrow);
        if (container == null) return;


        boolean isOpen = container.getVisibility() == VISIBLE;


        if (isOpen) {
            setExpandedStyle(parentView, false);
            if (currentActiveSubItem != null && container.indexOfChild(currentActiveSubItem) >= 0)
                removeSubItemHighlight(currentActiveSubItem);


            final int startH = container.getMeasuredHeight();
            ValueAnimator anim = ValueAnimator.ofInt(startH, 0);
            anim.setDuration(200);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.addUpdateListener(a -> {
                container.getLayoutParams().height = (int) a.getAnimatedValue();
                container.requestLayout();
            });
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator a) {
                    container.setVisibility(GONE);
                    container.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    container.requestLayout();
                }
            });
            anim.start();
            if (arrow != null) arrow.animate().rotation(0f).setDuration(200).start();


        } else {
            collapseAllSubMenusExcept(parentView);
            setExpandedStyle(parentView, true);


            container.setVisibility(VISIBLE);
            container.measure(
                    View.MeasureSpec.makeMeasureSpec(nav_linear_layout.getWidth(), View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            final int targetH = container.getMeasuredHeight();
            container.getLayoutParams().height = 0;
            container.requestLayout();


            ValueAnimator anim = ValueAnimator.ofInt(0, targetH);
            anim.setDuration(200);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.addUpdateListener(a -> {
                container.getLayoutParams().height = (int) a.getAnimatedValue();
                container.requestLayout();
            });
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator a) {
                    container.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    container.requestLayout();
                    if (currentActiveSubItem != null && container.indexOfChild(currentActiveSubItem) >= 0)
                        applySubItemHighlight(currentActiveSubItem);
                }
            });
            anim.start();
            if (arrow != null) arrow.animate().rotation(180f).setDuration(200).start();
        }
    }


    // ══════════════════════════════════════════════════════════════════════
    //  Sub-item click → highlight parent + sub-item + navigate
    // ══════════════════════════════════════════════════════════════════════
    private void navigateFromSub(View parentView, Fragment fragment) {
        setActiveParent(parentView);
        Constants.isCreate = false;


        if (currentActiveSubItem != null) {
            String subText = currentActiveSubItem.getText().toString();


            if (parentView == sm_matter) {
                if ("Legal Matters".equals(subText)) Constants.MATTER_TYPE = "Legal";
                else if ("General Matters".equals(subText)) Constants.MATTER_TYPE = "General";
            } else if (parentView == sm_documents) {
                String docType = "matter";
                if ("Matter".equals(subText)) docType = "matter";
                else if ("Client".equals(subText)) docType = "client";
                else if ("Firm".equals(subText)) docType = "firm";
                else if ("Deleted".equals(subText)) docType = "delete";
                Bundle args = new Bundle();
                args.putString("document_type", docType);
                fragment.setArguments(args);
            } else if (parentView == sm_relationships) {
                if ("Individuals".equals(subText)) Constants.Rel_Type = "Individual";
                else if ("Business".equals(subText)) Constants.Rel_Type = "Entity";
                else if ("Corporate".equals(subText)) Constants.Rel_Type = "Corporate";
                else if ("Deleted".equals(subText)) Constants.Rel_Type = "Deleted";
            } else if (parentView == sm_timesheet) {
                if ("My Timesheet".equals(subText)) {
                    Constants.Timesheet_Card = "Myts";
                    Constants.ts_card_clicked = false;
                } else if ("Aggregated Timesheet".equals(subText)) {
                    Constants.Timesheet_Card = "Agts";
                    Constants.ts_card_clicked = true;
                }
            } else if (parentView == sm_messages) {
                if ("Clients".equals(subText)) Constants.isClient_chat = true;
                else if ("Teams".equals(subText)) Constants.isClient_chat = false;
            } else if (parentView == sm_firmProfile) {
                if ("Profile".equals(subText)) Constants.Profile_View = "Bp";
                else if ("Practice Partner".equals(subText)) Constants.Profile_View = "Pp";
            }
        }


        if (!Constants.is_active) {
            AndroidUtils.showRenewalPopup(this);
            return;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.id_framelayout, fragment)
                .addToBackStack("current_fragment")
                .commit();
        dLayout.closeDrawers();
    }


    // ══════════════════════════════════════════════════════════════════════
    //  Direct navigation (non-submenu items)
    // ══════════════════════════════════════════════════════════════════════
    private void doNavigate(Fragment fragment) {
        Constants.isCreate = false;
//        if (!Constants.is_active) {
//            AndroidUtils.showRenewalPopup(this);
//            return;
//        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.id_framelayout, fragment)
                .addToBackStack("current_fragment")
                .commit();
        dLayout.closeDrawers();
    }


    // ══════════════════════════════════════════════════════════════════════
    //  Collapse all submenus
    // ══════════════════════════════════════════════════════════════════════
    private void collapseAllSubMenus() {
        collapseAllSubMenusExcept(null);
    }


    private void collapseAllSubMenusExcept(View except) {
        View[] withSubMenus = {sm_firmProfile, sm_matter, sm_documents, sm_relationships, sm_timesheet, sm_messages};
        for (View p : withSubMenus) {
            if (p == except) continue;
            LinearLayout c = p.findViewById(R.id.submenu_container);
            ImageView arr = p.findViewById(R.id.iv_arrow);
            if (c != null && c.getVisibility() == VISIBLE) {
                if (currentActiveSubItem != null && c.indexOfChild(currentActiveSubItem) >= 0) {
                    removeSubItemHighlight(currentActiveSubItem);
                    currentActiveSubItem = null;
                }
                c.setVisibility(GONE);
                c.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                c.requestLayout();
                setExpandedStyle(p, false);
            }
            if (arr != null) arr.setRotation(0f);
        }
    }


    public void checkMenuByFragment(Fragment fragment) {
        View sel = null;
        if (fragment instanceof FirmProfile) sel = sm_firmProfile;
        else if (fragment instanceof Appointments) sel = sm_appointments;
        else if (fragment instanceof Matter) sel = sm_matter;
        else if (fragment instanceof DocumentsEn) sel = sm_documents;
        else if (fragment instanceof DocEditor) sel = sm_docEditor;
        else if (fragment instanceof ClientRelationship) sel = sm_relationships;
        else if (fragment instanceof TimeSheets) sel = sm_timesheet;
        else if (fragment instanceof Meetings) sel = sm_meetings;
        else if (fragment instanceof Email) sel = sm_email;
        else if (fragment instanceof Chat) sel = sm_messages;
        else if (fragment instanceof MessagesList) sel = sm_messages;
        else if (fragment instanceof Notifications) sel = sm_notification;
        else if (fragment instanceof AuditTrails) sel = sm_audit;
        else if (fragment instanceof Groups) sel = sm_groups;
        else if (fragment instanceof Members) sel = sm_team_member;
        else if (fragment instanceof ViewInvoice) sel = sm_invoice;


        boolean keepSubItem = (currentActiveSubItem != null
                && sel != null && sel == currentActiveParentView);


        clearAllParentStyles();
        if (!keepSubItem) clearActiveSubItem();
        if (sel != null) setActiveParent(sel);
    }


    public void navigation_items(Fragment fragment) {
//        if (!Constants.is_active) {
//            AndroidUtils.showRenewalPopup(this);
//            return;
//        }
        Constants.isCreate = false;
        checkMenuByFragment(fragment);
        ensureSubMenuExpansionForFragment(fragment);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.id_framelayout, fragment)
                .addToBackStack("current_fragment")
                .commit();
        dLayout.closeDrawers();
    }


    public void navigationToModules(Fragment fragment) {
//        if (!Constants.is_active) {
//            AndroidUtils.showRenewalPopup(this);
//            return;
//        }
        Constants.isCreate = true;
        checkMenuByFragment(fragment);
        ensureSubMenuExpansionForFragment(fragment);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.id_framelayout, fragment)
                .addToBackStack("current_fragment")
                .commit();
        dLayout.closeDrawers();
    }


    public void Add_Page(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.id_framelayout, fragment).addToBackStack("current_fragment").commit();
    }


    public void Remove_Page(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
    }


    private void navigateToDashboard() {
//        Constants.requiresTermsAcceptance = false;
        clearAllParentStyles();
        clearActiveSubItem();
        collapseAllSubMenus();
        center_menu.close(true);
        Constants.isCreate = false;
        menu_open.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.menu_icon_img));
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.id_framelayout, new com.digicoffer.lauditor.Dashboard.NewRevampViewModels.Dashboard())
                .addToBackStack("current_fragment").commit();
        dLayout.closeDrawers();
    }


    private void fabMenu() {
        clearAllParentStyles();
        Constants.isCreate = false;
        center_menu.close(true);
        menu_open.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.menu_icon_img));
    }


    // ══════════════════════════════════════════════════════════════════════
    //  Bottom FAB icon reset
    // ══════════════════════════════════════════════════════════════════════
    private void hide_un_chosen_menu() {
        for (ImageView iv : new ImageView[]{matters_bm, timesheets_bm, documents_bm,
                relationships_bm, groups_bm, team_members_bm, firm_profile_bm, audit_bm, more_bm})
            iv.setBackground(getDrawable(R.drawable.background_transparent));
        matters_bm.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.matter_menu_icon));
        timesheets_bm.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.timesheets_menu_icon));
        documents_bm.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.documents_menu_icon));
        relationships_bm.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.relationship_menu_icon));
        groups_bm.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.group_menu_icon));
        team_members_bm.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.teammember_menu_icon));
        firm_profile_bm.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.firm_profile_menu_icon));
        audit_bm.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.audit_menu_icon));
        more_bm.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.more_menu_icon));
    }


    // ══════════════════════════════════════════════════════════════════════
    //  Menu visibility — role + feature flags
    // ══════════════════════════════════════════════════════════════════════
    private void hideMenuItems() {
        sm_firmProfile.setVisibility(GONE);
        sm_appointments.setVisibility(GONE);
        sm_matter.setVisibility(GONE);
        sm_documents.setVisibility(GONE);
        sm_docEditor.setVisibility(GONE);
        sm_relationships.setVisibility(GONE);
        sm_timesheet.setVisibility(GONE);
        sm_meetings.setVisibility(GONE);
        sm_email.setVisibility(GONE);
        sm_messages.setVisibility(GONE);
        sm_notification.setVisibility(GONE);
        sm_invoice.setVisibility(GONE);
        sm_audit.setVisibility(GONE);
        sm_groups.setVisibility(GONE);
        sm_team_member.setVisibility(GONE);
        sm_logout.setVisibility(VISIBLE);
    }


    public void updateProfileInitial() {
        runOnUiThread(() -> {
            String initial;
            if (!TextUtils.isEmpty(Constants.NAME)) {
                initial = Constants.NAME.substring(0, 1).toUpperCase();
//            } else if (!TextUtils.isEmpty(Constants.ContactName)) {
//                initial = Constants.ContactName.substring(0, 1).toUpperCase();
            } else {
                initial = "";
            }
            // Replace tvHeaderInitial with whatever your actual header
            // profile circle TextView ID is in your MainActivity layout
            if (Constants.mainActivity.person_icon != null) {
                Constants.mainActivity.person_icon.setText(initial);
            }
        });
    }


    private void setDefaultMenuList() {
        hideMenuItems();
        boolean solo = "solo".equals(Constants.CATEGORY);
        switch (Constants.ROLE) {
            case "AAM":
                sm_firmProfile.setVisibility(VISIBLE);
                sm_meetings.setVisibility(VISIBLE);
                sm_email.setVisibility(GONE);
                sm_messages.setVisibility(VISIBLE);
                sm_notification.setVisibility(VISIBLE);
                sm_audit.setVisibility(VISIBLE);
                if (!solo) {
                    sm_groups.setVisibility(VISIBLE);
                    sm_team_member.setVisibility(VISIBLE);
                }
                break;
            case "SU":
                sm_firmProfile.setVisibility(VISIBLE);
                sm_appointments.setVisibility(VISIBLE);
                sm_matter.setVisibility(VISIBLE);
                sm_documents.setVisibility(VISIBLE);
                sm_relationships.setVisibility(VISIBLE);
                sm_timesheet.setVisibility(VISIBLE);
                sm_meetings.setVisibility(VISIBLE);
                sm_email.setVisibility(GONE);
                sm_messages.setVisibility(VISIBLE);
                sm_notification.setVisibility(VISIBLE);
                sm_audit.setVisibility(VISIBLE);
                if (DynamicUtils.isTablet(this)) sm_invoice.setVisibility(VISIBLE);
                if (!solo) {
                    sm_groups.setVisibility(VISIBLE);
                    sm_team_member.setVisibility(VISIBLE);
                }
                break;
            case "GH":
                sm_firmProfile.setVisibility(VISIBLE);
                sm_appointments.setVisibility(VISIBLE);
                sm_matter.setVisibility(VISIBLE);
                sm_documents.setVisibility(VISIBLE);
                sm_relationships.setVisibility(VISIBLE);
                sm_timesheet.setVisibility(VISIBLE);
                sm_meetings.setVisibility(VISIBLE);
                sm_email.setVisibility(GONE);
                sm_messages.setVisibility(VISIBLE);
                sm_notification.setVisibility(VISIBLE);
                sm_audit.setVisibility(VISIBLE);
                if (!solo) {
                    sm_groups.setVisibility(VISIBLE);
                    sm_team_member.setVisibility(VISIBLE);
                }
                break;
            default:
                sm_firmProfile.setVisibility(VISIBLE);
                sm_appointments.setVisibility(VISIBLE);
                sm_matter.setVisibility(VISIBLE);
                sm_documents.setVisibility(VISIBLE);
                sm_relationships.setVisibility(VISIBLE);
                sm_timesheet.setVisibility(VISIBLE);
                sm_meetings.setVisibility(VISIBLE);
                sm_email.setVisibility(GONE);
                sm_messages.setVisibility(VISIBLE);
                sm_notification.setVisibility(VISIBLE);
                break;
        }
    }


    private void setMenuList() {
        setDefaultMenuList();
    }


    private void feat(View item, String key) {
        item.setVisibility(Constants.isFeatureEnabled(key) ? VISIBLE : GONE);
    }


    // ══════════════════════════════════════════════════════════════════════
    //  Profile popup
    // ══════════════════════════════════════════════════════════════════════
    private void showProfilePopup(View anchorView) {
        View popupView = getLayoutInflater().inflate(R.layout.profile_popup, null);
        PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);


        LinearLayout btnLogout = popupView.findViewById(R.id.btnLogout);
        LinearLayout ll_role = popupView.findViewById(R.id.ll_role);
        LinearLayout layout_firm_parent = popupView.findViewById(R.id.layout_firm_parent);
        LinearLayout layout_firm = popupView.findViewById(R.id.layout_firm);
        TextView popup_person_icon = popupView.findViewById(R.id.popup_person_icon);
        ImageView iv_profile_popup = popupView.findViewById(R.id.iv_profile);
        TextView tvName = popupView.findViewById(R.id.tvName);
        TextView tvEmail = popupView.findViewById(R.id.tvEmail);
        TextView tvRoleTitle = popupView.findViewById(R.id.tvRoleTitle);
        TextView tvRoleCompany = popupView.findViewById(R.id.tvRoleCompany);
        TextView iconFirm = popupView.findViewById(R.id.iconFirm);
        TextView tvFirm = popupView.findViewById(R.id.tvFirm);
        TextView btnSwitchFirm = popupView.findViewById(R.id.btnSwitchFirm);


        AppImageCache.preload(this, Constants.firm_image);
        if (Constants.ROLE.equals("AAM")) {
            AndroidUtils.loadProfileImage(this, Constants.firm_image, iv_profile_popup, popup_person_icon, Constants.FIRM_NAME);
        } else {
            AndroidUtils.loadProfileImage(this, Constants.firm_image, iv_profile_popup, popup_person_icon, Constants.NAME);
        }
//        if (Constants.ROLE.equals("AAM")) {
//            tvName.setText("Hi, " + Constants.FIRM_NAME);
//        } else {
        tvName.setText("Hi, " + Constants.NAME);
//        }
        tvName.setOnClickListener(v -> {
            popupWindow.dismiss();
            dLayout.closeDrawers();
            collapseAllSubMenus();
            clearAllParentStyles();
            clearActiveSubItem();
            Constants.isMyProfileClicked = !Constants.ROLE.equals("AAM");
            setActiveParent(sm_firmProfile);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.id_framelayout, new FirmProfile())
                    .addToBackStack("current_fragment").commit();
        });


        tvEmail.setText(Constants.Email);


        if ("solo".equals(Constants.CATEGORY)) {
            ll_role.setVisibility(GONE);
        } else {
            ll_role.setVisibility(VISIBLE);
            tvRoleTitle.setText(AndroidUtils.getFullRoleName(Constants.ROLE));
            tvRoleCompany.setText(Constants.FIRM_NAME);
        }


        boolean multi = Constants.Firm_names != null && Constants.Firm_names.size() > 1;
        if (multi) {
            btnSwitchFirm.setVisibility(VISIBLE);
            layout_firm_parent.setVisibility(VISIBLE);
            if (Constants.Firm_name != null && !Constants.Firm_name.trim().isEmpty()) {
                iconFirm.setText(getInitials(Constants.Firm_name));
                iconFirm.setVisibility(VISIBLE);
            }
            layout_firm_parent.removeAllViews();
            float d = getResources().getDisplayMetrics().density;
            int sz = (int) (30 * d), m8 = (int) (8 * d), m10 = (int) (10 * d), p8 = (int) (8 * d);
            for (int i = 0; i < Constants.Firm_names.size(); i++) {
                final String name = Constants.Firm_names.get(i);
                final String id = Constants.Firm_ids.get(i);
                if (Constants.FIRM_NAME != null && name.equalsIgnoreCase(Constants.FIRM_NAME))
                    continue;


                TextView iconDyn = new TextView(this);
                iconDyn.setText(getInitials(name));
                iconDyn.setTextColor(Color.WHITE);
                iconDyn.setGravity(Gravity.CENTER);
                iconDyn.setBackground(getDrawable(R.drawable.circular_button_background));
                iconDyn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(sz, sz);
                ilp.setMargins(0, 0, m8, 0);
                iconDyn.setLayoutParams(ilp);


                TextView tvDyn = new TextView(this);
                tvDyn.setText(name);
                tvDyn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                tvDyn.setTextColor(Color.BLACK);
                try {
                    tvDyn.setTypeface(ResourcesCompat.getFont(this, R.font.gill_sans_regular));
                } catch (Exception ignore) {
                }
                LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tlp.setMargins(m10, 0, 0, 0);
                tvDyn.setLayoutParams(tlp);


                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(0, p8, 0, p8);
                row.addView(iconDyn);
                row.addView(tvDyn);
                layout_firm_parent.addView(row);


                View.OnClickListener cl = view -> {
                    Constants.Firm_id = id;
                    Constants.FIRM_NAME = name;
                    popupWindow.dismiss();
                    // ── FIX: set the flag so handleDashboardResponse reloads
                    // in-place rather than starting a new MainActivity ──
                    isFirmSwitchInProgress = true;
                    switchLogin();
                };
                iconDyn.setOnClickListener(cl);
                tvDyn.setOnClickListener(cl);
                row.setOnClickListener(cl);
            }
            if (layout_firm_parent.getChildCount() == 0) {
                btnSwitchFirm.setVisibility(GONE);
                layout_firm_parent.setVisibility(GONE);
            }
        } else {
            btnSwitchFirm.setVisibility(GONE);
            layout_firm_parent.setVisibility(GONE);
        }


        btnLogout.setOnClickListener(v -> {
            popupWindow.dismiss();
            performLogout();
        });


        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        ViewGroup rootView = (ViewGroup) getWindow().getDecorView();
        View dimView = new View(this);
        dimView.setBackgroundColor(ContextCompat.getColor(this, R.color.popup_dim_overlay));
        dimView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rootView.addView(dimView);


        popupWindow.setOnDismissListener(() -> rootView.removeView(dimView));
        popupWindow.showAsDropDown(anchorView, -150, 0);
    }


    // ══════════════════════════════════════════════════════════════════════
    //  Switch firm
    // ══════════════════════════════════════════════════════════════════════
    public void switchLogin() {
        try {
            Constants.check_url();
            JSONObject post = new JSONObject();
            post.put("email", Constants.Email);
            post.put("userid", Constants.Firm_id);
            post.put("plan", "lauditor");
            WebServiceHelper.callHttpWebService(MainActivity.this, MainActivity.this,
                    WebServiceHelper.RestMethodType.POST, "switch-firm", "SWITCH_FIRM", post.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }


    private void performLogout() {
        MyFirebaseMessagingService.logoutToken(this, getStoredFCMToken());
        MyFirebaseMessagingService.clearAllNotifications(this);
        Constants.isClient_chat = true;
        getDefaultSharedPreferences(getApplicationContext()).edit()
                .remove("EXTRA_CONTACT_JID").remove("CURRENTCHAT_JID")
                .remove("firmNames")
                .remove("requiresTermsAcceptance")
                .remove("termsVersion")
                .remove("firmIds").apply();
        Constants.Chat_id = "";
        Constants.fromjid = "";
        Constants.Firm_ids.clear();
        Constants.Firm_names.clear();
        Constants.isClient_chat = true;
        getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences("BIO", Context.MODE_PRIVATE).edit().clear().apply();
        Constants.is_biometric = false;
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }


    public Dialog showConfirmation() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        View dl = LayoutInflater.from(this).inflate(R.layout.groups_required_popup, null);
        Button btnCancel = dl.findViewById(R.id.btnCancel), btnSave = dl.findViewById(R.id.btnSave);
        ((TextView) dl.findViewById(R.id.tv_confirmation)).setText(R.string.group_required);
        ((TextView) dl.findViewById(R.id.tv_confirmContent)).setText(
                R.string.to_perform_this_action_you_need_to_be_part_of_at_least_one_group_contact_the_admin_via_the_team_chat);
        btnCancel.setText(R.string.close);
        btnSave.setText(R.string.go_to_team_chat);
        final AlertDialog dialog = b.create();
        dialog.setView(dl);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int m = (int) (20 * getResources().getDisplayMetrics().density + 0.5f);
            w.setLayout(getResources().getDisplayMetrics().widthPixels - 2 * m, WindowManager.LayoutParams.WRAP_CONTENT);
            WindowManager.LayoutParams wlp = w.getAttributes();
            wlp.gravity = Gravity.CENTER;
            w.setAttributes(wlp);
        }
        btnSave.setOnClickListener(v -> {
            dialog.dismiss();
            Constants.isClient_chat = false;
            clearAllParentStyles();
            hide_un_chosen_menu();
            center_menu.close(true);
            navigateFromSub(sm_messages, new Chat());
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        return dialog;
    }


    // ══════════════════════════════════════════════════════════════════════
    //  NOTIFICATION BADGE
    // ══════════════════════════════════════════════════════════════════════
    public void fetchNotificationCount() {
        WebServiceHelper.callHttpWebService(this, this,
                WebServiceHelper.RestMethodType.GET, "notification", "NOTIFICATION_COUNT", "");
    }


    public void updateNotificationBadge(int count) {
        runOnUiThread(() -> {
            TextView badge = Constants.notifyBadge;
            if (badge == null && ll_notify != null) {
                View v = ll_notify.findViewById(R.id.notify_badge);
                if (v instanceof TextView) {
                    badge = (TextView) v;
                    Constants.notifyBadge = badge;
                }
            }
            if (badge == null) {
                Log.e("NOTIF", "notify_badge is null");
                return;
            }
            if (count > 0) {
                badge.setText(String.valueOf(count));
                badge.setVisibility(VISIBLE);
            } else badge.setVisibility(GONE);
        });
    }


    // ══════════════════════════════════════════════════════════════════════
    //  API
    // ══════════════════════════════════════════════════════════════════════
    public void profile() {
        progress_dialog = AndroidUtils.get_progress(this);
        try {
            String URL;
            if (Constants.ROLE.equals("AAM")) {
                URL = "v3/firm/profile/pic";
            } else {
                URL = "v3/profile/pic";
            }
            WebServiceHelper.callHttpWebService(this, this, WebServiceHelper.RestMethodType.GET,
                    URL, "Profile", new JSONObject().toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }


    public void Dashboard() {
//        Constants.requiresTermsAcceptance = false;
        AndroidUtils.updateCachedUserData(this);
        MYDAYCARDS.clear();
        KPICARDS.clear();
        Constants.check_url();
        Constants.base_URL = Constants.PROF_URL;
        WebServiceHelper.callHttpWebService(MainActivity.this, MainActivity.this,
                WebServiceHelper.RestMethodType.GET, Constants.Dashboard, "Dashboard", new JSONObject().toString());
    }


    // ══════════════════════════════════════════════════════════════════════
    //  COLD-START SESSION VALIDATION (FIX)
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Fires a lightweight, already-authenticated GET (same endpoint used by
     * fetchNotificationCount()) purely to validate the token restored from
     * SharedPreferences during a cold start. We deliberately reuse the
     * existing HttpExecuteTask → TokenRefreshHelper pipeline:
     * - If the token is valid, this returns 200 and onAsyncTaskComplete()
     * dispatches the pending navigation immediately.
     * - If the token is expired, HttpExecuteTask detects the 401 and hands
     * off to TokenRefreshHelper.handleUnauthorized(), which refreshes the
     * token and transparently RETRIES this very request — so
     * onAsyncTaskComplete() still fires with Success once that completes,
     * and we dispatch navigation then.
     * - If the refresh itself fails, TokenRefreshHelper already clears
     * tokens and redirects to LoginActivity — no further handling needed
     * here.
     */
    private void validateSessionBeforeNavigation() {
        WebServiceHelper.callHttpWebService(this, this,
                WebServiceHelper.RestMethodType.GET, "notification",
                "SESSION_VALIDATE_COLD_START", "");
    }


    /**
     * Dispatches the FCM navigation that was held back during cold-start
     * session validation. Mirrors the existing onResume()/onNewIntent()
     * navigation dispatch logic exactly, so the destination screen behaves
     * identically to a normal (non cold-start) notification tap.
     */
    private void dispatchPendingColdStartNavigation() {
        final String nav = pendingColdStartNavJson;
        pendingColdStartNavJson = null;
        if (nav == null || nav.isEmpty()) return;


        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            try {
                JSONObject jo = new JSONObject(nav);
                Navigation navObj = new Navigation();
                navObj.setRoute_name(jo.optString("route_name", ""));
                JSONObject params = jo.optJSONObject("params");
                if (params != null) navObj.setParams(params);
                Log.d("FCM_NAV", "Cold-start validated — dispatching nav route: " + navObj.getRoute_name());
                AndroidUtils.setupNotificationHandler(this, navObj);
            } catch (Exception e) {
                Log.e("FCM_NAV", "Cold-start pending nav dispatch fail: " + e.getMessage());
            }
        }, 300);
    }


    @Override
    public void onClick(View view) {
    }


    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progress_dialog != null && progress_dialog.isShowing())
            AndroidUtils.dismiss_dialog(progress_dialog);


        // ── FIX: captured outside the try/catch so it's still available if
        // JSON parsing of the response body throws below.
        String requestTypeSafe = httpResult.getRequestType();


        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                String rt = httpResult.getRequestType();
                if ("Profile".equals(rt)) {
                    if (!result.optBoolean("error")) {
                        JSONObject data = result.optJSONObject("data");
                        if (data != null) {
                            Constants.firm_image = data.optString("imageUrl", "");
                            AndroidUtils.loadProfileImage(this, Constants.firm_image, iv_profile, person_icon);
                        }
                        fetchNotificationCount();
                    }
                } else if ("SWITCH_FIRM".equals(rt)) {
                    handleLoginResponse(result);
                } else if ("Dashboard".equals(rt)) {
                    handleDashboardResponse(result);
                } else if ("FCM_TOKEN_UPDATE".equals(rt)) {
                    Log.d("FCM", "Token registered");
                } else if ("NOTIFICATION_COUNT".equals(rt)) {
                    handleNotificationCountResponse(result);
                } else if ("SESSION_VALIDATE_COLD_START".equals(rt)) {
                    // ── FIX: token confirmed valid — either directly, or
                    // transparently after TokenRefreshHelper refreshed and
                    // retried this same request. Safe to dispatch the
                    // deferred cold-start FCM navigation now.
                    dispatchPendingColdStartNavigation();
                }
            } catch (Exception e) {
                e.printStackTrace();
                // ── FIX: status was Success even though the body wasn't
                // parseable — the session itself is fine, so don't strand
                // the user mid cold-start navigation over a body-parsing
                // hiccup on the validation ping.
                if ("SESSION_VALIDATE_COLD_START".equals(requestTypeSafe)) {
                    dispatchPendingColdStartNavigation();
                }
            }
        } else {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);


            // ── FIX: a non-401 failure (network blip, 500, etc.) on the
            // validation ping itself shouldn't permanently block the user's
            // notification navigation — the destination fragment's own API
            // calls still have the existing 401 retry safety net if the
            // token is genuinely bad.
            if ("SESSION_VALIDATE_COLD_START".equals(requestTypeSafe)) {
                Log.w("FCM_NAV", "Session validation ping failed (non-401) — dispatching pending navigation anyway");
                dispatchPendingColdStartNavigation();
            } else {
                try {
                    AndroidUtils.showErrorAlert(
                            new JSONObject(httpResult.getResponseContent()).optString("msg"), MainActivity.this);
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            }
        }
    }


    private void handleNotificationCountResponse(JSONObject result) {
        try {
            if (result.optBoolean("error")) {
                Log.w("NOTIF", "Notification API error: " + result.optString("msg"));
                return;
            }
            int unreadCount = 0;
            JSONArray notifications = null;
            JSONObject dataObj = result.optJSONObject("data");
            if (dataObj != null) notifications = dataObj.optJSONArray("notifications");
            if (notifications == null) notifications = result.optJSONArray("data");
            if (notifications == null) notifications = result.optJSONArray("notifications");
            if (notifications != null) {
                for (int i = 0; i < notifications.length(); i++) {
                    JSONObject n = notifications.optJSONObject(i);
                    if (n != null && "unread".equalsIgnoreCase(n.optString("status", "")))
                        unreadCount++;
                }
            }
            final int finalCount = unreadCount;
            updateNotificationBadge(finalCount);
        } catch (Exception e) {
            Log.e("NOTIF", "Failed to parse notification response: " + e.getMessage());
        }
    }


    private void rebuildSideMenuForCurrentRole() {
        boolean isSolo = "solo".equals(Constants.CATEGORY);
        boolean isGHorTM = "GH".equals(Constants.ROLE) || "TM".equals(Constants.ROLE);


        // ── 1. Clear ALL sub-item containers ──────────────────────────────────
        for (View parent : allParents()) {
            LinearLayout container = parent.findViewById(R.id.submenu_container);
            if (container != null) container.removeAllViews();
        }


        // ── 2. Rewire ALL click handlers ──────────────────────────────────────


        if ("AAM".equals(Constants.ROLE)) {
            setRowClick(sm_firmProfile, v -> {
                Constants.isMyProfileClicked = false;
                collapseAllSubMenus();
                setActiveParent(sm_firmProfile);
                doNavigate(new FirmProfile());
            });
        } else if (isSolo || isGHorTM) {
            setRowClick(sm_firmProfile, v -> {
                Constants.isMyProfileClicked = true;
                collapseAllSubMenus();
                setActiveParent(sm_firmProfile);
                doNavigate(new FirmProfile());
            });
        } else {
            setRowClick(sm_firmProfile, v -> toggleSubMenu(sm_firmProfile));
        }


        setRowClick(sm_appointments, v -> {
            collapseAllSubMenus();
            setActiveParent(sm_appointments);
            doNavigate(new Appointments());
        });


        setRowClick(sm_matter, v -> toggleSubMenu(sm_matter));


        setRowClick(sm_documents, v -> toggleSubMenu(sm_documents));


        setRowClick(sm_docEditor, v -> {
            collapseAllSubMenus();
            setActiveParent(sm_docEditor);
            doNavigate(new DocEditor());
        });


        setRowClick(sm_relationships, v -> toggleSubMenu(sm_relationships));


        if (isSolo) {
            setRowClick(sm_timesheet, v -> {
                collapseAllSubMenus();
                setActiveParent(sm_timesheet);
                Constants.Timesheet_Card = "Myts";
                Constants.ts_card_clicked = false;
                Constants.is_ts_submitted = false;
                doNavigate(new TimeSheets());
            });
        } else {
            setRowClick(sm_timesheet, v -> toggleSubMenu(sm_timesheet));
        }


        setRowClick(sm_meetings, v -> {
            collapseAllSubMenus();
            setActiveParent(sm_meetings);
            doNavigate(new Meetings());
        });


        setRowClick(sm_email, v -> {
            collapseAllSubMenus();
            setActiveParent(sm_email);
            doNavigate(new Email());
            tv_pageName.setTextSize(DynamicUtils.eighteen);
            tv_pageName.setText(R.string.emails);
        });


        if ("AAM".equals(Constants.ROLE)) {
            setRowClick(sm_messages, v -> {
                collapseAllSubMenus();
                setActiveParent(sm_messages);
                Constants.isClient_chat = false;
                doNavigate(new Chat());
            });
        } else if (isSolo) {
            setRowClick(sm_messages, v -> {
                collapseAllSubMenus();
                setActiveParent(sm_messages);
                Constants.isClient_chat = true;
                doNavigate(new Chat());
            });
        } else {
            setRowClick(sm_messages, v -> toggleSubMenu(sm_messages));
        }


        setRowClick(sm_notification, v -> {
            collapseAllSubMenus();
            setActiveParent(sm_notification);
            doNavigate(new Notifications());
        });


        setRowClick(sm_audit, v -> {
            collapseAllSubMenus();
            setActiveParent(sm_audit);
            doNavigate(new AuditTrails());
        });


        setRowClick(sm_groups, v -> {
            collapseAllSubMenus();
            setActiveParent(sm_groups);
            doNavigate(new Groups());
        });


        setRowClick(sm_team_member, v -> {
            collapseAllSubMenus();
            setActiveParent(sm_team_member);
            doNavigate(new Members());
        });


        setRowClick(sm_invoice, v -> {
            collapseAllSubMenus();
            setActiveParent(sm_invoice);
            doNavigate(new ViewInvoice());
        });


        setRowClick(sm_logout, v -> performLogout());


        // ── 3. Update row titles + arrow visibility for new role ──────────────


        String firmProfileTitle;
        boolean hasProfileSubMenu;
        if ("AAM".equals(Constants.ROLE)) {
            Constants.isMyProfileClicked = false;
            firmProfileTitle = getString(R.string.firm_profile);
            hasProfileSubMenu = false;
        } else if (isSolo || isGHorTM) {
            Constants.isMyProfileClicked = true;
            firmProfileTitle = "My Profile";
            hasProfileSubMenu = false;
        } else {
            Constants.isMyProfileClicked = true;
            firmProfileTitle = getString(R.string.profile);
            hasProfileSubMenu = true;
        }


        setupMenuRow(sm_firmProfile, firmProfileTitle, R.drawable.profile, hasProfileSubMenu);
        setupMenuRow(sm_appointments, getString(R.string.appointments), R.drawable.appointments, false);
        setupMenuRow(sm_matter, getString(R.string.matters), R.drawable.matters, true);
        setupMenuRow(sm_documents, getString(R.string.documents), R.drawable.document_icon, true);
        setupMenuRow(sm_docEditor, getString(R.string.doc_editor), R.drawable.doceditor_icon, false);
        setupMenuRow(sm_relationships, getString(R.string.relationships), R.drawable.relationship, true);
        setupMenuRow(sm_timesheet, getString(R.string.timesheets), R.drawable.timesheet, !(isSolo || isGHorTM));
        setupMenuRow(sm_meetings, getString(R.string.meetings), R.drawable.meetings, false);
        setupMenuRow(sm_email, getString(R.string.emails), R.drawable.email, false);
        boolean hasMessagesSubMenu = !"AAM".equals(Constants.ROLE) && !isSolo;
        setupMenuRow(sm_messages, getString(R.string.messages), R.drawable.messages, hasMessagesSubMenu);
        setupMenuRow(sm_notification, getString(R.string.notifications), R.drawable.notifications, false);
        setupMenuRow(sm_audit, getString(R.string.audit_trails), R.drawable.audit_trails, false);
        setupMenuRow(sm_groups, getString(R.string.groups), R.drawable.groups, false);
        setupMenuRow(sm_team_member, getString(R.string.members), R.drawable.members, false);
        setupMenuRow(sm_invoice, "Invoices", R.drawable.invoices, false);
        setupMenuRow(sm_logout, getString(R.string.logout), R.drawable.logout, false);


        // ── 4. Rebuild ALL sub-items for the new role ─────────────────────────


        // Firm Profile sub-items (only for SU non-solo)
        if ("SU".equals(Constants.ROLE) && !isSolo) {
            Constants.isMyProfileClicked = true;
            addSubItem(sm_firmProfile, "Firm Profile", v -> {
                Constants.Profile_View = "Bp";
                Constants.isMyProfileClicked = false;
                setActiveSubItem((TextView) v);
                navigateFromSub(sm_firmProfile, new FirmProfile());
            });
            addSubItem(sm_firmProfile, "My Profile", v -> {
                Constants.Profile_View = "Bp";
                Constants.isMyProfileClicked = true;
                setActiveSubItem((TextView) v);
                navigateFromSub(sm_firmProfile, new FirmProfile());
            });
            addSubItem(sm_firmProfile, "Practice Partner", v -> {
                Constants.Profile_View = "Pp";
                setActiveSubItem((TextView) v);
                navigateFromSub(sm_firmProfile, new FirmProfile());
            });
        }


        // Matter sub-items (always present)
        addSubItem(sm_matter, "Legal Matters", v -> {
            setActiveSubItem((TextView) v);
            Constants.Matter_CreateOrViewDetails = "View";
            Constants.MATTER_TYPE = "Legal";
            Constants.is_CreateMatter = false;
            Constants.create_matter = false;
            navigateFromSub(sm_matter, new Matter());
        });
        addSubItem(sm_matter, "General Matters", v -> {
            setActiveSubItem((TextView) v);
            Constants.Matter_CreateOrViewDetails = "View";
            Constants.MATTER_TYPE = "General";
            Constants.is_CreateMatter = false;
            Constants.create_matter = false;
            navigateFromSub(sm_matter, new Matter());
        });


        // Documents sub-items
        addSubItem(sm_documents, "Matter", v -> {
            setActiveSubItem((TextView) v);
            navigateFromSub(sm_documents, newDocumentFragment("matter"));
        });
        addSubItem(sm_documents, "Client", v -> {
            setActiveSubItem((TextView) v);
            navigateFromSub(sm_documents, newDocumentFragment("client"));
        });
        if (!isSolo) {
            addSubItem(sm_documents, "Firm", v -> {
                setActiveSubItem((TextView) v);
                navigateFromSub(sm_documents, newDocumentFragment("firm"));
            });
        }
        if (Constants.ROLE.equals("SU")) {
            addSubItem(sm_documents, "Deleted", v -> {
                setActiveSubItem((TextView) v);
                navigateFromSub(sm_documents, newDocumentFragment("delete"));
            });
        }


        // Relationships sub-items
        addSubItem(sm_relationships, "Individual", v -> {
            setActiveSubItem((TextView) v);
            Constants.Rel_Type = "Individual";
            navigateFromSub(sm_relationships, new ClientRelationship());
        });
        addSubItem(sm_relationships, "Business", v -> {
            setActiveSubItem((TextView) v);
            Constants.Rel_Type = "Entity";
            navigateFromSub(sm_relationships, new ClientRelationship());
        });
        addSubItem(sm_relationships, "Corporate", v -> {
            setActiveSubItem((TextView) v);
            Constants.Rel_Type = "Corporate";
            navigateFromSub(sm_relationships, new ClientRelationship());
        });
        if (Constants.ROLE.equals("SU")) {
            addSubItem(sm_relationships, "Deleted", v -> {
                setActiveSubItem((TextView) v);
                Constants.Rel_Type = "Deleted";
                navigateFromSub(sm_relationships, new ClientRelationship());
            });
        }


        // Timesheets
        if (!(isSolo)) {
            addSubItem(sm_timesheet, "Aggregated Timesheet", v -> {
                setActiveSubItem((TextView) v);
                Constants.Timesheet_Card = "Agts";
                Constants.ts_card_clicked = true;
                Constants.is_ts_submitted = false;
                navigateFromSub(sm_timesheet, new TimeSheets());
            });
            addSubItem(sm_timesheet, "My Timesheet", v -> {
                setActiveSubItem((TextView) v);
                Constants.Timesheet_Card = "Myts";
                Constants.ts_card_clicked = false;
                Constants.is_ts_submitted = false;
                navigateFromSub(sm_timesheet, new TimeSheets());
            });
        }


        // Messages sub-items (only when NOT solo/GH/TM)
        if (!isSolo) {
            String role = Constants.ROLE;
            if (!"AAM".equals(role)) {
                addSubItem(sm_messages, "Clients", v -> {
                    setActiveSubItem((TextView) v);
                    Constants.isClient_chat = true;
                    navigateFromSub(sm_messages, new Chat());
                });
            }
            addSubItem(sm_messages, "Teams", v -> {
                setActiveSubItem((TextView) v);
                Constants.isClient_chat = false;
                navigateFromSub(sm_messages, new Chat());
            });
        }


        // ── 5. Refresh menu item visibility for the new role ──────────────────
        setMenuList();


        // ── 6. Reset bottom FAB state ─────────────────────────────────────────
        center_menu.close(true);
        hide_un_chosen_menu();
        menu_open.setImageDrawable(
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.menu_icon_img));
    }


    /**
     * FIX: When this is called after a firm switch (isFirmSwitchInProgress == true),
     * we reload the dashboard fragment in-place instead of starting a new MainActivity.
     * Starting a new MainActivity was the root cause of the login-redirect bug because
     * the token check in the new instance could race against Constants being re-populated.
     */
    private void handleDashboardResponse(JSONObject result) {
        try {
            if (result.optBoolean("error") && result.optString("msg").toLowerCase().contains("token")) {
                Constants.Valid_Token = false;
                AndroidUtils.showAlert("Session expired. Please login again.", this);
                return;
            }
            Constants.Valid_Token = true;
            Dashboard_data(result.getJSONArray("cards"));
            saveXmppPreferences();
            new android.os.Handler().postDelayed(() -> {
                if (Constants.mainActivity != null)
                    Constants.mainActivity.registerPendingFCMToken();
            }, 500);


            if (isFirmSwitchInProgress) {
                isFirmSwitchInProgress = false;


                runOnUiThread(() -> {
                    // Full rebuild: click handlers + sub-items + titles + visibility
                    rebuildSideMenuForCurrentRole();


                    // Update appbar profile avatar for the new firm
                    if (Constants.ROLE.equals("AAM")) {
                        person_icon.setText(
                                (Constants.FIRM_NAME != null && !Constants.FIRM_NAME.isEmpty())
                                        ? Constants.FIRM_NAME.substring(0, 1) : "?");
                    } else {
                        person_icon.setText(
                                (Constants.NAME != null && !Constants.NAME.isEmpty())
                                        ? Constants.NAME.substring(0, 1) : "?");
                    }
                    AppImageCache.preload(this, Constants.firm_image);
                    AndroidUtils.loadProfileImage(this, Constants.firm_image, iv_profile, person_icon);


                    // Reset all highlight state
                    clearAllParentStyles();
                    clearActiveSubItem();
                    collapseAllSubMenus();
                    center_menu.close(true);


                    // Navigate to Dashboard in-place (no new Activity)
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.id_framelayout,
                                    new com.digicoffer.lauditor.Dashboard.NewRevampViewModels.Dashboard())
                            .commit();


                    // Refresh notification badge for the new firm
                    fetchNotificationCount();
                });
                return;
            }


            // ── Normal launch (not a firm switch) ──
            Intent mainIntent = new Intent(this, MainActivity.class);
            if (Constants.pendingFcmNavigation != null && !Constants.pendingFcmNavigation.isEmpty()) {
                mainIntent.putExtra("fcm_navigation", Constants.pendingFcmNavigation);
                Constants.pendingFcmNavigation = "";
            }
            startActivity(mainIntent);
            finish();


        } catch (Exception e) {
            e.printStackTrace();
            AndroidUtils.showAlert("Error loading dashboard", this);
        }
    }


    private void saveXmppPreferences() {
        String uid = Constants.UID;
        if (!Constants.ROLE.equalsIgnoreCase("admin")) uid = uid + "_" + Constants.USER_ID;
        getDefaultSharedPreferences(getApplicationContext()).edit()
                .putString("xmpp_jid", uid)
                .putString("xmpp_password", Constants.TOKEN)
                .putBoolean("xmpp_logged_in", true).apply();
        mConnection = new ChatConnection(this);
        chatConnectionService = new ChatConnectionService();
        new JsonTask().execute(Constants.base_URL + "user/create/");
    }


    @Override
    public void highlightMenuForCardType(int cardType) {
        runOnUiThread(() -> {
            clearAllParentStyles();
            clearActiveSubItem();
            View targetMenu = null;
            switch (cardType) {
                case DashboardItem.TYPE_MEETING:
                    targetMenu = sm_meetings;
                    break;
                case DashboardItem.TYPE_APPOINTMENT:
                    targetMenu = sm_appointments;
                    break;
                case DashboardItem.TYPE_MESSAGES:
                    targetMenu = sm_messages;
                    expandSubMenuIfNeeded(sm_messages);
                    break;
                case DashboardItem.TYPE_NOTIFICATION:
                    targetMenu = sm_notification;
                    break;
                case DashboardItem.TYPE_MATTER:
                    targetMenu = sm_matter;
                    expandSubMenuIfNeeded(sm_matter);
                    break;
                case DashboardItem.TYPE_BILLABLE:
                case DashboardItem.TYPE_APPROX_REVENUE:
                    targetMenu = sm_timesheet;
                    expandSubMenuIfNeeded(sm_timesheet);
                    break;
                case DashboardItem.TYPE_HIRING:
                    targetMenu = sm_groups;
                    break;
                case DashboardItem.TYPE_SUBSCRIPTION:
                    targetMenu = sm_firmProfile;
                    break;
                default:
                    break;
            }
            if (targetMenu != null) {
                setActiveParent(targetMenu);
                highlightSubItemForCard(targetMenu, cardType);
            }
        });
    }


    private void expandSubMenuIfNeeded(View parentView) {
        LinearLayout container = parentView.findViewById(R.id.submenu_container);
        ImageView arrow = parentView.findViewById(R.id.iv_arrow);
        if (container != null && container.getVisibility() != VISIBLE && container.getChildCount() > 0) {
            container.setVisibility(VISIBLE);
            if (arrow != null) arrow.setRotation(180f);
            setExpandedStyle(parentView, true);
        }
    }


    private void highlightSubItemForCard(View parentView, int cardType) {
        LinearLayout container = parentView.findViewById(R.id.submenu_container);
        if (container == null) return;
        TextView targetSubItem = null;
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof TextView) {
                TextView subItem = (TextView) child;
                String text = subItem.getText().toString();
                switch (cardType) {
                    case DashboardItem.TYPE_MESSAGES:
                        if (text.equals("Clients") || text.equals("Teams")) targetSubItem = subItem;
                        break;
                    case DashboardItem.TYPE_MATTER:
                        if (text.equals("Legal Matters") || text.equals("General Matters"))
                            targetSubItem = subItem;
                        break;
                    case DashboardItem.TYPE_BILLABLE:
                    case DashboardItem.TYPE_APPROX_REVENUE:
                        if (text.equals("My Timesheet") || text.equals("Aggregated Timesheet"))
                            targetSubItem = subItem;
                        break;
                }
                if (targetSubItem != null) break;
            }
        }
        if (targetSubItem != null) setActiveSubItem(targetSubItem);
    }


    private class JsonTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... p) {
            try {
                String uid = Constants.UID;
                if (!Constants.ROLE.equalsIgnoreCase("admin")) uid = uid + "_" + Constants.USER_ID;
                mConnection.loginUser(MainActivity.this, uid, Constants.TOKEN);
            } catch (Exception e) {
                Log.d("Chat", "fail");
                e.printStackTrace();
                if (chatConnectionService != null) chatConnectionService.stopSelf();
            }
            return "";
        }
    }


    private void Dashboard_data(JSONArray arr) throws Exception {
        MYDAYCARDS.clear();
        KPICARDS.clear();
        Set<String> skip = new HashSet<>(Arrays.asList("timesheets", "newclients", "groups", "teammembers"));
        for (int i = 0; i < arr.length(); i++) {
            JSONObject jo = arr.getJSONObject(i);
            String type = jo.getString("type");
            JSONArray opts = jo.getJSONArray("options");
            for (int j = 0; j < opts.length(); j++) {
                JSONObject o = opts.getJSONObject(j);
                String name = o.getString("name");
                if (skip.contains(name.toLowerCase())) continue;
                Dashboard_Model dm = new Dashboard_Model();
                dm.setName(name);
                dm.setSequence(o.getInt("sequence"));
                dashboardModels.add(dm);
                if ("MYDAY".equals(type)) MYDAYCARDS.add(dm);
                if ("KPI".equals(type)) KPICARDS.add(dm);
            }
        }
    }


    private void handleLoginResponse(JSONObject result) {
        try {
            Constants.Firm_ids.clear();
            Constants.Firm_names.clear();
            if (result.getBoolean("error")) {
                AndroidUtils.showAlert(result.optString("msg", "Failed"), MainActivity.this);
                // ── FIX: reset the flag on error so a retry doesn't get stuck ──
                isFirmSwitchInProgress = false;
                return;
            }
            Constants.forgot_pwd_request = false;
            JSONObject d = new JSONObject(result.getString("data"));
            Constants.jsonObject_dashboard = d;
            if (!d.getString("plan").equalsIgnoreCase("lauditor")) {
                AndroidUtils.showAlert("Account not found", this);
                isFirmSwitchInProgress = false;
                return;
            }
            String em = Objects.requireNonNull(Constants.Email);
            getDefaultSharedPreferences(getApplicationContext()).edit()
                    .putString("email", em.toLowerCase())
                    .putString("password", "")
                    .putBoolean("isLogin", true)
                    .putString("proBizType", Constants.PROBIZ_TYPE).apply();


//            Constants.TOKEN = result.getString("token");
            Constants.NAME = d.getString("name");
            Constants.LOGIN_METHOD = "email";
            Constants.termsVersion = d.getString("termsVersion");
            Constants.requiresTermsAcceptance = d.optBoolean("requiresTermsAcceptance");
            Constants.USER_ID = d.getString("user_id");
            Constants.UID = d.getString("uid");
            Constants.PK = d.getString("pk");
            Constants.PASSWORD_MODE = d.getString("password_mode");
            Constants.IS_ADMIN = d.getBoolean("admin");
            Constants.FIRM_NAME = d.getString("firm_name");
            Constants.ROLE = d.getString("role");
            Constants.CATEGORY = d.optString("category");
            Constants.Groups = d.getJSONArray("groups");
            Constants.Email = em.toLowerCase();
            Constants.FirmEmail = d.optString("email");
            Constants.Refresh_token = d.optString("refresh_token");
            Constants.TOKEN = d.optString("access_token");
            JSONObject sub = d.optJSONObject("subscription");
            if (sub != null) {
                JSONObject feat = sub.optJSONObject("features");
                if (feat != null) {
                    Constants.FEATURES.clear();
                    Iterator<String> k = feat.keys();
                    while (k.hasNext()) {
                        String key = k.next();
                        Constants.FEATURES.put(key, feat.optBoolean(key, false));
                    }
                }
                Constants.is_active = sub.optBoolean("is_active");
            }
            Constants.User_Allowed = d.optInt("user_allowed");
            Constants.isAdmin = Constants.ROLE.equals("AAM")
                    || (Constants.Groups.length() == 1 && Constants.Groups.getString(0).equals("AAM"));


            JSONArray firms = d.getJSONArray("firms");
            for (int i = 0; i < firms.length(); i++) {
                JSONObject o = firms.getJSONObject(i);
                String fn = o.getString("firmName");
                String fi = o.getString("id");
                FirmsDo fd = new FirmsDo();
                fd.setName(fn);
                fd.setValue(fi);
                Constants.Firm_names.add(fn);
                Constants.Firm_ids.add(fi);
            }


            // ── FIX: persist the new token to SharedPreferences immediately.
            // This ensures that even if the process is killed and recreated, the
            // token check in onCreate() will find a valid token and not redirect
            // the user to the login screen.
            getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit()
                    .putString("Token", Constants.TOKEN)
                    .putString("refresh_token", Constants.Refresh_token)
                    .putString("email", em.toLowerCase())
                    .putString("firm_id", Constants.Firm_id)
                    .putString("Json_key", String.valueOf(Constants.jsonObject_dashboard))
                    .putBoolean("requiresTermsAcceptance", Constants.requiresTermsAcceptance)
                    .putString("termsVersion", Constants.termsVersion)
                    .apply();


            Bio_metric_access();
            Constants.firm_image = ""; // reset stale image before fetching new firm's profile
            profile();                 // fetch new firm's profile image
            if (Constants.loginActivity != null) {
                Constants.loginActivity.syncDevice();
            }
            MyFirebaseMessagingService.logoutToken(this, getStoredFCMToken());
            MyFirebaseMessagingService.clearAllNotifications(this);
            registerPendingFCMToken();
            checkTermsAndConditionsForFirmSwitch();
            Constants.IS_MyDay = true;
        } catch (Exception e) {
            e.printStackTrace();
            isFirmSwitchInProgress = false;
        }
    }

    private void checkTermsAndConditionsForFirmSwitch() {
        Activity context = this;
        TermsAndCondition termsAndCondition = new TermsAndCondition(this);
        termsAndCondition.setOnTermsAndConditionListener(new TermsAndCondition.OnTermsAndConditionListener() {
            @Override
            public void onTermsAccepted() {
                Log.d("TermsCheck", "Terms Accepted");
                Dashboard();
            }

            @Override
            public void onTermsDeclined() {
//                AndroidUtils.showAlert("Please accept the Terms and Conditions to continue using your account. If not accepted, the account will be automatically removed after 30 days.", context, "Confirmation", () -> {
//                    finish();
//                    performLogout();
//                });
                AndroidUtils.showConfirmationDialog(context, "Confirmation", "Please agree to the T&C to proceed. ", "Ok", "Cancel", new AndroidUtils.OnConfirmListener() {
                    @Override
                    public void onSave() {
                        checkTermsAndConditionsForFirmSwitch();
                    }

                    @Override
                    public void onCancel() {
                        finish();
                        performLogout();
                    }
                }, true);
            }

            @Override
            public void onTermsCheckComplete(boolean needsToShow) {
                Log.d("TermsCheck", "needsToShow: " + needsToShow);
                if (!needsToShow) Dashboard();
            }
        });
        termsAndCondition.checkTermsWithData(Constants.termsVersion, Constants.requiresTermsAcceptance);
    }

    private void checkTermsAndConditions() {
        Activity context = this;
        TermsAndCondition termsAndCondition = new TermsAndCondition(this);
        termsAndCondition.setOnTermsAndConditionListener(new TermsAndCondition.OnTermsAndConditionListener() {
            @Override
            public void onTermsAccepted() {
                Log.d("TermsCheck", "Terms Accepted");
                runOnUiThread(() -> {
                    clearAllParentStyles();
                    clearActiveSubItem();
                    collapseAllSubMenus();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.id_framelayout,
                                    new com.digicoffer.lauditor.Dashboard.NewRevampViewModels.Dashboard())
                            .commit();
                });
            }

            @Override
            public void onTermsDeclined() {
//                AndroidUtils.showAlert("Please accept the Terms and Conditions to continue using your account. If not accepted, the account will be automatically removed after 30 days.", context, "Confirmation", () -> {
//                    finish();
//                    performLogout();
//                });
                AndroidUtils.showConfirmationDialog(context, "Confirmation", "Please agree to the T&C to proceed. ", "Ok", "Cancel", new AndroidUtils.OnConfirmListener() {
                    @Override
                    public void onSave() {
                        checkTermsAndConditions();
                    }

                    @Override
                    public void onCancel() {
                        finish();
                        performLogout();
                    }
                }, true);
            }

            @Override
            public void onTermsCheckComplete(boolean needsToShow) {
                Log.d("TermsCheck", "needsToShow: " + needsToShow);
            }
        });
        termsAndCondition.checkTermsWithData(Constants.termsVersion, Constants.requiresTermsAcceptance);
    }


    private void Bio_metric_access() {
        getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit()
                .putBoolean("requiresTermsAcceptance", Constants.requiresTermsAcceptance)
                .putString("termsVersion", Constants.termsVersion)
                .putString("email", Constants.Email.toLowerCase())
                .putString("password", "")
                .putString("Token", Constants.TOKEN)
                .putString("refresh_token", Constants.Refresh_token)
                .putString("login_method", Constants.LOGIN_METHOD)
                .putString("firm_id", Constants.Firm_id)
                .putString("Json_key", String.valueOf(Constants.jsonObject_dashboard))
                .putBoolean("Check_box", true)
                .apply();
        Constants.is_biometric = true;
    }


    // ══════════════════════════════════════════════════════════════════════
    //  Lifecycle
    // ══════════════════════════════════════════════════════════════════════
    @Override
    public void onResume() {
        super.onResume();
        // Fire any pending FCM navigation now that this fragment is fully visible
        if (Constants.pendingFcmNavigation != null && !Constants.pendingFcmNavigation.isEmpty()) {


            String nav = Constants.pendingFcmNavigation;
            Constants.pendingFcmNavigation = ""; // clear before posting to avoid repeat


            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                try {
                    org.json.JSONObject jo = new org.json.JSONObject(nav);
                    com.digicoffer.lauditor.Notifications.Models.Navigation navObj =
                            new com.digicoffer.lauditor.Notifications.Models.Navigation();
                    navObj.setRoute_name(jo.optString("route_name", ""));
                    org.json.JSONObject params = jo.optJSONObject("params");
                    if (params != null) navObj.setParams(params);
                    AndroidUtils.setupNotificationHandler(this, navObj);
                } catch (Exception e) {
                    android.util.Log.e("FCM_NAV", "Dashboard pending nav fail: " + e.getMessage());
                }
            }, 300);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        Log.d("Refresh_Token", Constants.Refresh_token);
        String navJson = intent.getStringExtra("fcm_navigation");
        if (navJson == null || navJson.isEmpty())
            navJson = intent.getStringExtra("navigation");


        if (navJson == null || navJson.isEmpty()) return;


        final String finalNavJson = navJson;


        // Clear the extras so a config-change re-delivery doesn't re-navigate
        intent.removeExtra("fcm_navigation");
        intent.removeExtra("navigation");


        // Post with delay to let any in-progress fragment transactions finish
        // and to ensure the fragment's onViewCreated/onResume completes before
        // we push the navigation fragment on top.
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            try {
                org.json.JSONObject jo = new org.json.JSONObject(finalNavJson);
                com.digicoffer.lauditor.Notifications.Models.Navigation nav =
                        new com.digicoffer.lauditor.Notifications.Models.Navigation();
                nav.setRoute_name(jo.optString("route_name", ""));
                org.json.JSONObject params = jo.optJSONObject("params");
                if (params != null) nav.setParams(params);
                android.util.Log.d("FCM_NAV", "onNewIntent route: " + nav.getRoute_name());
                AndroidUtils.setupNotificationHandler(this, nav);
            } catch (Exception e) {
                android.util.Log.e("FCM_NAV", "onNewIntent nav fail: " + e.getMessage());
            }
        }, 400); // 400 ms is enough for fragment manager to settle
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AndroidUtils.PERMISSION_REQUEST_CAMERA_AUDIO) {
            boolean ok = true;
            for (int r : grantResults)
                if (r != PackageManager.PERMISSION_GRANTED) {
                    ok = false;
                    break;
                }
            if (ok) {
                SharedPreferences p = getSharedPreferences("video_call_prefs", Context.MODE_PRIVATE);
                String url = p.getString("pending_avchat_url", null);
                if (url != null) {
                    p.edit().remove("pending_avchat_url").remove("has_pending_delegate").apply();
                    AndroidUtils.loadAVChatView(this, this, url);
                }
            } else {
                AndroidUtils.showToast("Camera and Microphone permissions are required for video chat", this);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (dtoggle != null && dtoggle.onOptionsItemSelected(item)) {
            actionButton.setVisibility(VISIBLE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onEventDetailsPassed(ArrayList<Event_Details_DO> list, String cal) {
    }


    // ══════════════════════════════════════════════════════════════════════
    //  FCM
    // ══════════════════════════════════════════════════════════════════════
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED)
                notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            else fetchAndStoreFCMToken();
        } else {
            fetchAndStoreFCMToken();
        }
    }


    private void fetchAndStoreFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("FCM", "fail:" + task.getException());
                return;
            }
            String t = task.getResult();
            SharedPreferences p = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String s = p.getString("fcm_token", "");
            p.edit().putString("fcm_token", t).apply();
            if (Constants.USER_ID != null && !Constants.USER_ID.isEmpty() && !t.equals(s))
                sendFCMTokenToBackend(t);
        });
    }


    public void registerPendingFCMToken() {
        String t = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("fcm_token", "");
        if (!t.isEmpty() && Constants.USER_ID != null && !Constants.USER_ID.isEmpty())
            sendFCMTokenToBackend(t);
    }


    private void sendFCMTokenToBackend(String token) {
        try {
            JSONObject p = new JSONObject();
            p.put("userId", Constants.USER_ID);
            p.put("fcmToken", token);
            p.put("platform", "android");
            WebServiceHelper.callHttpWebService(MainActivity.this, MainActivity.this,
                    WebServiceHelper.RestMethodType.POST,
                    Constants.Notification_Base_Url + "/register-token", "FCM_TOKEN_UPDATE", p.toString());
        } catch (Exception e) {
            Log.e("FCM", "fail:" + e.getMessage());
        }
    }


    public String getStoredFCMToken() {
        return getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("fcm_token", "");
    }


    private void handleNotificationNavigation(Intent intent) {
        if (intent == null) return;


        String nj = intent.getStringExtra("fcm_navigation");
        if (nj == null || nj.isEmpty()) nj = intent.getStringExtra("navigation");
        if (nj == null || nj.isEmpty()) return;


        final String finalNj = nj;
        Log.d("FCM_NAV", "Navigation JSON: " + finalNj);


        // Remove so a rotation/resume doesn't replay
        intent.removeExtra("fcm_navigation");
        intent.removeExtra("navigation");


        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            try {
                JSONObject jo = new JSONObject(finalNj);
                Navigation nav = new Navigation();
                nav.setRoute_name(jo.optString("route_name", ""));
                JSONObject params = jo.optJSONObject("params");
                if (params != null) nav.setParams(params);
                Log.d("FCM_NAV", "Route: " + nav.getRoute_name());
                AndroidUtils.setupNotificationHandler(this, nav);
            } catch (Exception e) {
                Log.e("FCM", "nav fail:" + e.getMessage());
            }
        }, 500); // wait for Dashboard fragment to complete its onCreate/onViewCreated
    }


    private int dpToPx(Context ctx, int dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density);
    }


    private void handleDashboardState() {
        collapseAllSubMenus();
        clearAllParentStyles();
        clearActiveSubItem();
        if (dLayout.isDrawerOpen(GravityCompat.START)) dLayout.closeDrawer(GravityCompat.START);
    }


    private void ensureSubMenuExpansionForFragment(Fragment fragment) {
        View parentView = null;
        if (fragment instanceof FirmProfile) parentView = sm_firmProfile;
        else if (fragment instanceof Appointments) parentView = sm_appointments;
        else if (fragment instanceof Matter) parentView = sm_matter;
        else if (fragment instanceof DocumentsEn) parentView = sm_documents;
        else if (fragment instanceof DocEditor) parentView = sm_docEditor;
        else if (fragment instanceof ClientRelationship) parentView = sm_relationships;
        else if (fragment instanceof TimeSheets) parentView = sm_timesheet;
        else if (fragment instanceof Meetings) parentView = sm_meetings;
        else if (fragment instanceof Email) parentView = sm_email;
        else if (fragment instanceof Chat || fragment instanceof MessagesList)
            parentView = sm_messages;
        else if (fragment instanceof Notifications) parentView = sm_notification;
        else if (fragment instanceof AuditTrails) parentView = sm_audit;
        else if (fragment instanceof Groups) parentView = sm_groups;
        else if (fragment instanceof Members) parentView = sm_team_member;
        else if (fragment instanceof ViewInvoice) parentView = sm_invoice;


        if (parentView != null) {
            LinearLayout container = parentView.findViewById(R.id.submenu_container);
            ImageView arrow = parentView.findViewById(R.id.iv_arrow);


            if (container != null && container.getChildCount() > 0) {
                collapseAllSubMenusExcept(parentView);
                if (container.getVisibility() != VISIBLE) {
                    container.setVisibility(VISIBLE);
                    if (arrow != null) arrow.setRotation(180f);
                    setExpandedStyle(parentView, true);


                    container.measure(
                            View.MeasureSpec.makeMeasureSpec(nav_linear_layout.getWidth(), View.MeasureSpec.AT_MOST),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    final int targetH = container.getMeasuredHeight();
                    container.getLayoutParams().height = 0;
                    container.requestLayout();


                    ValueAnimator anim = ValueAnimator.ofInt(0, targetH);
                    anim.setDuration(200);
                    anim.setInterpolator(new DecelerateInterpolator());
                    anim.addUpdateListener(a -> {
                        container.getLayoutParams().height = (int) a.getAnimatedValue();
                        container.requestLayout();
                    });
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator a) {
                            container.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            container.requestLayout();
                        }
                    });
                    anim.start();
                }
                highlightCorrectSubItem(parentView, fragment);
            }
        }
    }


    private void highlightCorrectSubItem(View parentView, Fragment fragment) {
        LinearLayout container = parentView.findViewById(R.id.submenu_container);
        if (container == null) return;


        String targetSubText = null;


        if (fragment instanceof Matter) {
            if ("Legal".equals(Constants.MATTER_TYPE)) targetSubText = "Legal Matters";
            else if ("General".equals(Constants.MATTER_TYPE)) targetSubText = "General Matters";
        } else if (fragment instanceof DocumentsEn) {
            Bundle args = fragment.getArguments();
            if (args != null) {
                String dt = args.getString("document_type");
                if ("matter".equals(dt)) targetSubText = "Matter";
                else if ("client".equals(dt)) targetSubText = "Client";
                else if ("firm".equals(dt)) targetSubText = "Firm";
                else if ("delete".equals(dt)) targetSubText = "Deleted";
            } else {
                targetSubText = "Matter";
            }
        } else if (fragment instanceof ClientRelationship) {
            if ("Individual".equals(Constants.Rel_Type)) targetSubText = "Individuals";
            else if ("Entity".equals(Constants.Rel_Type)) targetSubText = "Business";
            else if ("Corporate".equals(Constants.Rel_Type)) targetSubText = "Corporate";
            else if ("Deleted".equals(Constants.Rel_Type)) targetSubText = "Deleted";
        } else if (fragment instanceof TimeSheets) {
            if ("Myts".equals(Constants.Timesheet_Card)) targetSubText = "My Timesheet";
            else if ("Agts".equals(Constants.Timesheet_Card))
                targetSubText = "Aggregated Timesheet";
        } else if (fragment instanceof Chat) {
            targetSubText = Constants.isClient_chat ? "Clients" : "Teams";
        } else if (fragment instanceof FirmProfile) {
            if ("Bp".equals(Constants.Profile_View)) targetSubText = "Profile";
            else if ("Pp".equals(Constants.Profile_View)) targetSubText = "Practice Partner";
        }


        if (targetSubText != null) {
            for (int i = 0; i < container.getChildCount(); i++) {
                View child = container.getChildAt(i);
                if (child instanceof TextView) {
                    TextView subItem = (TextView) child;
                    String itemText = subItem.getText().toString();
                    boolean matches = targetSubText.equals(itemText);


                    // Fallback matching for relationship sub-items
                    if (!matches && fragment instanceof ClientRelationship) {
                        matches = ("Individuals".equals(itemText) && "Individual".equals(Constants.Rel_Type))
                                || ("Business".equals(itemText) && "Entity".equals(Constants.Rel_Type))
                                || ("Corporate".equals(itemText) && "Corporate".equals(Constants.Rel_Type))
                                || ("Deleted".equals(itemText) && "Deleted".equals(Constants.Rel_Type));
                    }
                    // Fallback matching for document sub-items
                    if (!matches && fragment instanceof DocumentsEn) {
                        String dt = getDocumentTypeFromFragment(fragment);
                        matches = ("Matter".equals(itemText) && "matter".equals(dt))
                                || ("Client".equals(itemText) && "client".equals(dt))
                                || ("Firm".equals(itemText) && "firm".equals(dt))
                                || ("Deleted".equals(itemText) && "delete".equals(dt));
                    }


                    if (matches) {
                        if (currentActiveSubItem != null && currentActiveSubItem != subItem)
                            removeSubItemHighlight(currentActiveSubItem);
                        currentActiveSubItem = subItem;
                        applySubItemHighlight(subItem);
                        break;
                    }
                }
            }
        }
    }


    private String getDocumentTypeFromFragment(Fragment fragment) {
        if (fragment instanceof DocumentsEn) {
            Bundle args = fragment.getArguments();
            if (args != null) return args.getString("document_type", "matter");
        }
        return "matter";
    }
}

